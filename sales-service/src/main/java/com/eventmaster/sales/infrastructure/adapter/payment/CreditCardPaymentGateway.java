package com.eventmaster.sales.infrastructure.adapter.payment;

import com.eventmaster.sales.application.port.out.PaymentGatewayPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CreditCardPaymentGateway implements PaymentGatewayPort {

    @Override
    // SUA ADIÇÃO: O Circuit Breaker protegendo a chamada do cartão
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallbackCreditCard")
    public boolean processPayment(String orderId, BigDecimal amount) {
        System.out.println("[Gateway] Processando Cartão de Crédito para pedido: " + orderId);
        // Simulação de uma chamada externa instável
        return true; 
    }

    // SUA ADIÇÃO: O método de fallback caso o circuito abra ou dê erro
    public boolean fallbackCreditCard(String orderId, BigDecimal amount, Exception ex) {
        System.err.println("[Circuit Breaker] API de Cartão indisponível! Erro: " + ex.getMessage());
        return false; // Retorna falso para forçar o ProcessPaymentService a acionar o SAGA Rollback
    }

    @Override
    public String paymentMethod() {
        return "CREDIT_CARD";
    }
}