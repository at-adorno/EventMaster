package com.eventmaster.sales.application.port.in;

import com.eventmaster.sales.domain.entity.Order;

/**
 * Porta de entrada — contrato para processamento de pagamento de um pedido.
 */
public interface ProcessPaymentUseCase {

    /**
     * Processa o pagamento de um pedido existente.
     */
    Order execute(ProcessPaymentCommand command);

    /**
     * Comando imutável com os dados necessários para processar o pagamento.
     */
    record ProcessPaymentCommand(
            String orderId,
            String paymentMethod // PIX, CREDIT_CARD, BOLETO
    ) {}
}
