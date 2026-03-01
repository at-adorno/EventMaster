package com.eventmaster.sales.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representa um item do pedido — um ingresso para um evento específico.
 */
public class OrderItem {

    private final String id;
    private final String eventId;
    private final String ticketType;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderItem(String eventId, String ticketType, BigDecimal unitPrice, int quantity) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId não pode ser vazio");
        }
        if (ticketType == null || ticketType.isBlank()) {
            throw new IllegalArgumentException("ticketType não pode ser vazio");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice deve ser maior que zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity deve ser maior que zero");
        }
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.ticketType = ticketType;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTicketType() { return ticketType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
