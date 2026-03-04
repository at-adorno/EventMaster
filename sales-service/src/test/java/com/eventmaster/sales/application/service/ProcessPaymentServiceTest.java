package com.eventmaster.sales.application.service;

import com.eventmaster.sales.application.port.in.ProcessPaymentUseCase.ProcessPaymentCommand;
import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import com.eventmaster.sales.domain.entity.Order;
import com.eventmaster.sales.domain.entity.OrderItem;
import com.eventmaster.sales.domain.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do ProcessPaymentService — TDD.
 * Valida a seleção da Strategy correta e transições de estado.
 */
class ProcessPaymentServiceTest {

    private ProcessPaymentService service;
    private StubOrderRepository repository;
    private SpyPaymentGateway pixGateway;
    private SpyPaymentGateway cardGateway;
    private MockMessageBroker messageBroker; // O seu Mock

    @BeforeEach
    void setUp() {
        repository = new StubOrderRepository();
        pixGateway = new SpyPaymentGateway("PIX", true);
        cardGateway = new SpyPaymentGateway("CREDIT_CARD", true);
        messageBroker = new MockMessageBroker(); // Instanciando o Mock
        
        // Passando o Mock como 3º parâmetro
        service = new ProcessPaymentService(repository, List.of(pixGateway, cardGateway), messageBroker);
    }

    private Order createConfirmedOrder() {
        Order order = new Order("customer-1");
        order.addItem(new OrderItem("event-1", "VIP", new BigDecimal("100.00"), 2));
        order.confirm();
        repository.save(order);
        return order;
    }

    @Test
    @DisplayName("Deve processar pagamento via PIX com sucesso")
    void shouldProcessPixPayment() {
        Order order = createConfirmedOrder();
        var command = new ProcessPaymentCommand(order.getId(), "PIX");

        Order result = service.execute(command);

        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals("PIX", result.getPaymentMethod());
        assertTrue(pixGateway.wasCalled());
        
        // Bônus: garante que o SAGA publicou o evento de sucesso!
        assertTrue(messageBroker.eventPublished);
    }

    @Test
    @DisplayName("Deve processar pagamento via CREDIT_CARD com sucesso")
    void shouldProcessCreditCardPayment() {
        Order order = createConfirmedOrder();
        var command = new ProcessPaymentCommand(order.getId(), "CREDIT_CARD");

        Order result = service.execute(command);

        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertTrue(cardGateway.wasCalled());
        assertFalse(pixGateway.wasCalled());
    }

    @Test
    @DisplayName("Deve rejeitar método de pagamento não suportado")
    void shouldRejectUnsupportedPaymentMethod() {
        Order order = createConfirmedOrder();
        var command = new ProcessPaymentCommand(order.getId(), "BITCOIN");

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
    }

    @Test
    @DisplayName("Deve rejeitar pedido inexistente")
    void shouldRejectNonExistentOrder() {
        var command = new ProcessPaymentCommand("non-existent", "PIX");
        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
    }

    @Test
    @DisplayName("Deve lançar exceção quando pagamento é recusado e acionar Rollback do SAGA")
    void shouldThrowWhenPaymentDeclined() {
        repository = new StubOrderRepository();
        var failingGateway = new SpyPaymentGateway("PIX", false);
        messageBroker = new MockMessageBroker(); // Reinstanciando o Mock para este contexto isolado
        
        // Passando o Mock como 3º parâmetro
        service = new ProcessPaymentService(repository, List.of(failingGateway), messageBroker);

        Order order = createConfirmedOrder();
        var command = new ProcessPaymentCommand(order.getId(), "PIX");

        assertThrows(RuntimeException.class, () -> service.execute(command));
        
        // Bônus da sua entrega: garante que o evento SAGA de rollback foi disparado!
        assertTrue(messageBroker.eventPublished);
    }

    // --- Stubs / Spies ---

    private static class StubOrderRepository implements OrderRepositoryPort {
        private final List<Order> orders = new ArrayList<>();

        @Override
        public void save(Order order) {
            orders.removeIf(o -> o.getId().equals(order.getId()));
            orders.add(order);
        }

        @Override
        public Optional<Order> findById(String orderId) {
            return orders.stream().filter(o -> o.getId().equals(orderId)).findFirst();
        }
    }

    private static class SpyPaymentGateway implements PaymentGatewayPort {
        private final String method;
        private final boolean approves;
        private boolean called = false;

        SpyPaymentGateway(String method, boolean approves) {
            this.method = method;
            this.approves = approves;
        }

        @Override
        public boolean processPayment(String orderId, BigDecimal amount) {
            called = true;
            return approves;
        }

        @Override
        public String paymentMethod() {
            return method;
        }

        boolean wasCalled() {
            return called;
        }
    }

    // --- Mock do Kafka (SAGA) ---
    static class MockMessageBroker implements com.eventmaster.sales.application.port.out.MessageBrokerPort {
        public boolean eventPublished = false;
        public Object lastPayload = null;

        @Override
        public void publishEvent(String topic, Object eventPayload) {
            this.eventPublished = true;
            this.lastPayload = eventPayload;
        }
    }
}