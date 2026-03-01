package com.eventmaster.sales.infrastructure.adapter.payment;

import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy concreta — Pagamento via Pix.
 * Simulação simplificada (sempre aprova).
 */
@Component
public class PixPaymentGateway implements PaymentGatewayPort {

    @Override
    public boolean processPayment(String orderId, BigDecimal amount) {
        // Simulação: Pix é aprovado instantaneamente
        return true;
    }

    @Override
    public String paymentMethod() {
        return "PIX";
    }
}
