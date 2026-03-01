package com.eventmaster.sales.application.service;

import com.eventmaster.sales.application.port.in.CreateOrderUseCase;
import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.domain.entity.Order;
import com.eventmaster.sales.domain.entity.OrderItem;
import org.springframework.stereotype.Service;

/**
 * Implementação do caso de uso de criação de pedidos.
 * Princípio SRP: responsável apenas por orquestrar a criação.
 */
@Service
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public CreateOrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order execute(CreateOrderCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item");
        }

        Order order = new Order(command.customerId());

        for (CreateOrderCommand.ItemData itemData : command.items()) {
            OrderItem item = new OrderItem(
                    itemData.eventId(),
                    itemData.ticketType(),
                    itemData.unitPrice(),
                    itemData.quantity()
            );
            order.addItem(item);
        }

        order.confirm();
        orderRepository.save(order);

        return order;
    }
}
