package com.eventmaster.sales.application.service;

import com.eventmaster.sales.application.port.in.ProcessPaymentUseCase;
import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import com.eventmaster.sales.application.port.out.MessageBrokerPort; // SUA INJEÇÃO
import com.eventmaster.sales.application.dto.OrderEvent; // SEU DTO
import com.eventmaster.sales.application.dto.CompensationEvent; // SEU DTO
import com.eventmaster.sales.domain.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final OrderRepositoryPort orderRepository;
    private final Map<String, PaymentGatewayPort> strategies;
    private final MessageBrokerPort messageBroker; // SUA DECLARAÇÃO

    // O Spring agora vai injetar o repositório, as estratégias e o seu KafkaAdapter
    public ProcessPaymentService(OrderRepositoryPort orderRepository,
                                  List<PaymentGatewayPort> paymentGateways,
                                  MessageBrokerPort messageBroker) {
        this.orderRepository = orderRepository;
        this.strategies = paymentGateways.stream()
                .collect(Collectors.toMap(PaymentGatewayPort::paymentMethod, Function.identity()));
        this.messageBroker = messageBroker;
    }

    @Override
    public Order execute(ProcessPaymentCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + command.orderId()));

        PaymentGatewayPort gateway = strategies.get(command.paymentMethod());
        if (gateway == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + command.paymentMethod());
        }

        // ==========================================
        // INÍCIO DA SUA ORQUESTRAÇÃO SAGA
        // ==========================================
        try {
            boolean approved = gateway.processPayment(order.getId(), order.totalWithFee());
            if (!approved) {
                throw new RuntimeException("Pagamento recusado pelo gateway para o pedido: " + order.getId());
            }

            order.markAsPaid(command.paymentMethod());
            orderRepository.save(order);

            // 1. SAGA (Caminho Feliz): Publica evento de sucesso
            messageBroker.publishEvent("order-completed-topic", new OrderEvent(order.getId(), "CONFIRMED"));

            return order;

        } catch (Exception e) {
            // 2. SAGA (Rollback): O pagamento falhou ou o Circuit Breaker abriu!
            System.err.println("[SAGA] Falha no pagamento para a ordem " + order.getId() + ". Iniciando Rollback...");
            
            // Publica o evento para o Catálogo devolver o ingresso.
            // (Usamos o ID da ordem como referência do ingresso para simplificar)
            messageBroker.publishEvent("ticket-compensation-topic", new CompensationEvent(order.getId(), "RELEASE_LOCK"));
            
            throw new RuntimeException("Falha no processamento. Reserva do ingresso cancelada. Motivo: " + e.getMessage());
        }
    }
}