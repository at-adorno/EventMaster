package com.eventmaster.sales.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank(message = "orderId é obrigatório")
        String orderId,

        @NotBlank(message = "paymentMethod é obrigatório")
        String paymentMethod // PIX, CREDIT_CARD, BOLETO
) {}
