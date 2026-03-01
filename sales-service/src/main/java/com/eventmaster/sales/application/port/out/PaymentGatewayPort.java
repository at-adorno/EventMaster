package com.eventmaster.sales.application.port.out;

import java.math.BigDecimal;

/**
 * Porta de saída — contrato para processamento de pagamentos.
 * Cada implementação representa um gateway (Pix, Cartão, Boleto).
 * Segue o padrão Strategy.
 */
public interface PaymentGatewayPort {

    /**
     * Processa o pagamento e retorna true se aprovado.
     */
    boolean processPayment(String orderId, BigDecimal amount);

    /**
     * Identifica o método de pagamento (PIX, CREDIT_CARD, BOLETO).
     */
    String paymentMethod();
}
