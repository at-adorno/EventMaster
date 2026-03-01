package com.eventmaster.sales.application.service;

import com.eventmaster.sales.application.port.in.ProcessPaymentUseCase;
import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import com.eventmaster.sales.domain.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementação do caso de uso de processamento de pagamento.
 * Utiliza o padrão Strategy: seleciona o gateway de pagamento
 * correto com base no método informado pelo cliente.
 *
 * Princípio OCP: para adicionar um novo método de pagamento,
 * basta criar uma nova implementação de PaymentGatewayPort.
 */
@Service
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final OrderRepositoryPort orderRepository;
    private final Map<String, PaymentGatewayPort> strategies;

    /**
     * O Spring injeta todas as implementações de PaymentGatewayPort.
     * Cada uma é indexada pelo seu paymentMethod().
     */
    public ProcessPaymentService(OrderRepositoryPort orderRepository,
                                  List<PaymentGatewayPort> paymentGateways) {
        this.orderRepository = orderRepository;
        this.strategies = paymentGateways.stream()
                .collect(Collectors.toMap(PaymentGatewayPort::paymentMethod, Function.identity()));
    }

    @Override
    public Order execute(ProcessPaymentCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + command.orderId()));

        PaymentGatewayPort gateway = strategies.get(command.paymentMethod());
        if (gateway == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + command.paymentMethod());
        }

        boolean approved = gateway.processPayment(order.getId(), order.totalWithFee());
        if (!approved) {
            throw new RuntimeException("Pagamento recusado para o pedido: " + order.getId());
        }

        order.markAsPaid(command.paymentMethod());
        orderRepository.save(order);

        return order;
    }
}
