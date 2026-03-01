package com.eventmaster.sales.infrastructure.adapter.payment;

import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy concreta — Pagamento via Cartão de Crédito.
 * Simulação simplificada (sempre aprova).
 */
@Component
public class CreditCardPaymentGateway implements PaymentGatewayPort {

    @Override
    public boolean processPayment(String orderId, BigDecimal amount) {
        // Simulação: cartão de crédito é aprovado
        return true;
    }

    @Override
    public String paymentMethod() {
        return "CREDIT_CARD";
    }
}
