package com.eventmaster.sales.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root — Pedido de compra de ingressos.
 * Contém as regras de negócio para transições de estado e cálculos.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private String paymentMethod;
    private final LocalDateTime createdAt;

    public Order(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId não pode ser vazio");
        }
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Adiciona um item ao pedido. Só é permitido enquanto o pedido estiver CREATED.
     */
    public void addItem(OrderItem item) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido " + status);
        }
        if (item == null) {
            throw new IllegalArgumentException("item não pode ser nulo");
        }
        items.add(item);
    }

    /**
     * Calcula o total do pedido (soma dos subtotais de cada item).
     */
    public BigDecimal total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula a taxa de serviço (10% sobre o total).
     */
    public BigDecimal serviceFee() {
        return total().multiply(new BigDecimal("0.10"));
    }

    /**
     * Total com taxa de serviço incluída.
     */
    public BigDecimal totalWithFee() {
        return total().add(serviceFee());
    }

    /**
     * Confirma o pedido (CREATED -> CONFIRMED).
     */
    public void confirm() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Só é possível confirmar um pedido CREATED, estado atual: " + status);
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Não é possível confirmar um pedido sem itens");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Marca o pedido como pago (CONFIRMED -> PAID).
     */
    public void markAsPaid(String paymentMethod) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Só é possível pagar um pedido CONFIRMED, estado atual: " + status);
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("paymentMethod não pode ser vazio");
        }
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.PAID;
    }

    /**
     * Cancela o pedido. Pode ser cancelado se estiver CREATED ou CONFIRMED.
     */
    public void cancel() {
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Não é possível cancelar um pedido já pago");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
