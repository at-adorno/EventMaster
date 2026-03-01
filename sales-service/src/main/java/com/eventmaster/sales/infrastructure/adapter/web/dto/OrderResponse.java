package com.eventmaster.sales.infrastructure.adapter.web.dto;

import com.eventmaster.sales.domain.entity.Order;
import com.eventmaster.sales.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String id,
        String customerId,
        String status,
        String paymentMethod,
        BigDecimal total,
        BigDecimal serviceFee,
        BigDecimal totalWithFee,
        List<ItemResponse> items
) {
    public static OrderResponse from(Order order) {
        List<ItemResponse> items = order.getItems().stream()
                .map(i -> new ItemResponse(i.getEventId(), i.getTicketType(),
                        i.getUnitPrice(), i.getQuantity(), i.subtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                order.total(),
                order.serviceFee(),
                order.totalWithFee(),
                items
        );
    }

    public record ItemResponse(
            String eventId,
            String ticketType,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {}
}
