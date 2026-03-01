package com.eventmaster.sales.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do OrderItem.
 */
class OrderItemTest {

    @Test
    @DisplayName("Deve criar item e calcular subtotal")
    void shouldCreateAndCalculateSubtotal() {
        OrderItem item = new OrderItem("event-1", "VIP", new BigDecimal("50.00"), 3);
        assertEquals(new BigDecimal("150.00"), item.subtotal());
        assertEquals("event-1", item.getEventId());
        assertEquals("VIP", item.getTicketType());
    }

    @Test
    @DisplayName("Deve rejeitar eventId vazio")
    void shouldRejectBlankEventId() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("", "VIP", new BigDecimal("50.00"), 1));
    }

    @Test
    @DisplayName("Deve rejeitar preço zero ou negativo")
    void shouldRejectInvalidPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("e1", "VIP", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("e1", "VIP", new BigDecimal("-10"), 1));
    }

    @Test
    @DisplayName("Deve rejeitar quantidade zero ou negativa")
    void shouldRejectInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("e1", "VIP", new BigDecimal("50.00"), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("e1", "VIP", new BigDecimal("50.00"), -1));
    }
}
