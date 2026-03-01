package com.eventmaster.sales.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da entidade Order — TDD.
 * Cobre: criação, adição de itens, cálculos de total/taxa,
 * transições de estado válidas e inválidas.
 */
class OrderTest {

    private OrderItem sampleItem() {
        return new OrderItem("event-1", "VIP", new BigDecimal("100.00"), 2);
    }

    // ===== Criação =====

    @Nested
    @DisplayName("Criação de pedido")
    class Creation {

        @Test
        @DisplayName("Deve criar pedido com status CREATED")
        void shouldCreateWithStatusCreated() {
            Order order = new Order("customer-1");
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertNotNull(order.getId());
            assertEquals("customer-1", order.getCustomerId());
            assertTrue(order.getItems().isEmpty());
        }

        @Test
        @DisplayName("Deve rejeitar customerId vazio")
        void shouldRejectBlankCustomerId() {
            assertThrows(IllegalArgumentException.class, () -> new Order(""));
            assertThrows(IllegalArgumentException.class, () -> new Order(null));
        }
    }

    // ===== Itens =====

    @Nested
    @DisplayName("Adição de itens")
    class Items {

        @Test
        @DisplayName("Deve adicionar item ao pedido CREATED")
        void shouldAddItem() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            assertEquals(1, order.getItems().size());
        }

        @Test
        @DisplayName("Deve rejeitar item nulo")
        void shouldRejectNullItem() {
            Order order = new Order("customer-1");
            assertThrows(IllegalArgumentException.class, () -> order.addItem(null));
        }

        @Test
        @DisplayName("Não deve adicionar item a pedido CONFIRMED")
        void shouldNotAddItemToConfirmedOrder() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            assertThrows(IllegalStateException.class, () -> order.addItem(sampleItem()));
        }
    }

    // ===== Cálculos =====

    @Nested
    @DisplayName("Cálculos de valor")
    class Calculations {

        @Test
        @DisplayName("Deve calcular total corretamente")
        void shouldCalculateTotal() {
            Order order = new Order("customer-1");
            order.addItem(new OrderItem("e1", "VIP", new BigDecimal("100.00"), 2));
            order.addItem(new OrderItem("e1", "NORMAL", new BigDecimal("50.00"), 3));
            // 200 + 150 = 350
            assertEquals(new BigDecimal("350.00"), order.total());
        }

        @Test
        @DisplayName("Deve calcular taxa de serviço (10%)")
        void shouldCalculateServiceFee() {
            Order order = new Order("customer-1");
            order.addItem(new OrderItem("e1", "VIP", new BigDecimal("200.00"), 1));
            // 200 * 0.10 = 20
            assertEquals(0, new BigDecimal("20.00").compareTo(order.serviceFee()));
        }

        @Test
        @DisplayName("Deve calcular total com taxa")
        void shouldCalculateTotalWithFee() {
            Order order = new Order("customer-1");
            order.addItem(new OrderItem("e1", "VIP", new BigDecimal("200.00"), 1));
            // 200 + 20 = 220
            assertEquals(0, new BigDecimal("220.000").compareTo(order.totalWithFee()));
        }

        @Test
        @DisplayName("Total de pedido vazio deve ser zero")
        void emptyOrderTotalShouldBeZero() {
            Order order = new Order("customer-1");
            assertEquals(BigDecimal.ZERO, order.total());
        }
    }

    // ===== Transições de estado =====

    @Nested
    @DisplayName("Transições de estado")
    class StateTransitions {

        @Test
        @DisplayName("CREATED -> CONFIRMED")
        void shouldConfirm() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("Não deve confirmar pedido sem itens")
        void shouldNotConfirmEmptyOrder() {
            Order order = new Order("customer-1");
            assertThrows(IllegalStateException.class, order::confirm);
        }

        @Test
        @DisplayName("CONFIRMED -> PAID")
        void shouldMarkAsPaid() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            order.markAsPaid("PIX");
            assertEquals(OrderStatus.PAID, order.getStatus());
            assertEquals("PIX", order.getPaymentMethod());
        }

        @Test
        @DisplayName("Não deve pagar pedido CREATED (não confirmado)")
        void shouldNotPayCreatedOrder() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            assertThrows(IllegalStateException.class, () -> order.markAsPaid("PIX"));
        }

        @Test
        @DisplayName("CREATED -> CANCELLED")
        void shouldCancelCreatedOrder() {
            Order order = new Order("customer-1");
            order.cancel();
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("CONFIRMED -> CANCELLED")
        void shouldCancelConfirmedOrder() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            order.cancel();
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Não deve cancelar pedido PAID")
        void shouldNotCancelPaidOrder() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            order.markAsPaid("PIX");
            assertThrows(IllegalStateException.class, order::cancel);
        }

        @Test
        @DisplayName("Não deve cancelar pedido já cancelado")
        void shouldNotCancelAlreadyCancelled() {
            Order order = new Order("customer-1");
            order.cancel();
            assertThrows(IllegalStateException.class, order::cancel);
        }

        @Test
        @DisplayName("Não deve confirmar pedido já confirmado")
        void shouldNotConfirmTwice() {
            Order order = new Order("customer-1");
            order.addItem(sampleItem());
            order.confirm();
            assertThrows(IllegalStateException.class, order::confirm);
        }
    }
}
