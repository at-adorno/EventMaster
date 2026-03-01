package com.eventmaster.sales.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "eventId é obrigatório")
        String eventId,

        @NotBlank(message = "ticketType é obrigatório")
        String ticketType,

        @NotNull(message = "unitPrice é obrigatório")
        @Positive(message = "unitPrice deve ser positivo")
        BigDecimal unitPrice,

        @Min(value = 1, message = "quantity deve ser no mínimo 1")
        int quantity
) {}
