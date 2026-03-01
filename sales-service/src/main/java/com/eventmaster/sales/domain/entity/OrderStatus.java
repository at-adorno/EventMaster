package com.eventmaster.sales.domain.entity;

/**
 * Estados possíveis de um pedido de compra de ingressos.
 * Transições válidas:
 *   CREATED -> CONFIRMED -> PAID
 *   CREATED -> CANCELLED
 *   CONFIRMED -> CANCELLED
 */
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PAID,
    CANCELLED
}
