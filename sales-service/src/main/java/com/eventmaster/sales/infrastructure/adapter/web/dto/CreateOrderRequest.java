package com.eventmaster.sales.infrastructure.adapter.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "customerId é obrigatório")
        String customerId,

        @NotEmpty(message = "items não pode ser vazio")
        @Valid
        List<OrderItemRequest> items
) {}
