package com.eventmaster.sales.application.service;

import com.eventmaster.sales.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.eventmaster.sales.application.port.in.CreateOrderUseCase.CreateOrderCommand.ItemData;
import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.domain.entity.Order;
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
 * Testes unitários do CreateOrderService — TDD.
 * Usa um stub in-memory para isolar o teste do repositório.
 */
class CreateOrderServiceTest {

    private CreateOrderService service;
    private StubOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubOrderRepository();
        service = new CreateOrderService(repository);
    }

    @Test
    @DisplayName("Deve criar pedido com itens e status CONFIRMED")
    void shouldCreateOrderWithItemsAndConfirm() {
        var command = new CreateOrderCommand("customer-1", List.of(
                new ItemData("event-1", "VIP", new BigDecimal("100.00"), 2)
        ));

        Order order = service.execute(command);

        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("200.00"), order.total());
        assertTrue(repository.findById(order.getId()).isPresent());
    }

    @Test
    @DisplayName("Deve criar pedido com múltiplos itens")
    void shouldCreateOrderWithMultipleItems() {
        var command = new CreateOrderCommand("customer-1", List.of(
                new ItemData("event-1", "VIP", new BigDecimal("100.00"), 1),
                new ItemData("event-1", "NORMAL", new BigDecimal("50.00"), 2)
        ));

        Order order = service.execute(command);

        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("200.00"), order.total());
    }

    @Test
    @DisplayName("Deve rejeitar pedido sem itens")
    void shouldRejectEmptyItems() {
        var command = new CreateOrderCommand("customer-1", List.of());
        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
    }

    @Test
    @DisplayName("Deve rejeitar pedido com itens nulos")
    void shouldRejectNullItems() {
        var command = new CreateOrderCommand("customer-1", null);
        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
    }

    // --- Stub simples para testes ---
    private static class StubOrderRepository implements OrderRepositoryPort {
        private final List<Order> orders = new ArrayList<>();

        @Override
        public void save(Order order) {
            orders.add(order);
        }

        @Override
        public Optional<Order> findById(String orderId) {
            return orders.stream().filter(o -> o.getId().equals(orderId)).findFirst();
        }
    }
}
