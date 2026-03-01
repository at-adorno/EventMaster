package com.eventmaster.sales.infrastructure.adapter.payment;

import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy concreta — Pagamento via Boleto Bancário.
 * Simulação simplificada (sempre aprova).
 */
@Component
public class BoletoPaymentGateway implements PaymentGatewayPort {

    @Override
    public boolean processPayment(String orderId, BigDecimal amount) {
        // Simulação: boleto é aprovado
        return true;
    }

    @Override
    public String paymentMethod() {
        return "BOLETO";
    }
}
