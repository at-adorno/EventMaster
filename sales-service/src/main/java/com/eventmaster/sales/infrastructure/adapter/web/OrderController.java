package com.eventmaster.sales.infrastructure.adapter.web;

import com.eventmaster.sales.application.port.in.CreateOrderUseCase;
import com.eventmaster.sales.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.eventmaster.sales.application.port.in.ProcessPaymentUseCase;
import com.eventmaster.sales.application.port.in.ProcessPaymentUseCase.ProcessPaymentCommand;
import com.eventmaster.sales.domain.entity.Order;
import com.eventmaster.sales.infrastructure.adapter.web.dto.CreateOrderRequest;
import com.eventmaster.sales.infrastructure.adapter.web.dto.OrderResponse;
import com.eventmaster.sales.infrastructure.adapter.web.dto.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adaptador de entrada (REST) — camada mais externa da Clean Architecture.
 * Converte DTOs em Commands e delega para os Use Cases.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           ProcessPaymentUseCase processPaymentUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var command = new CreateOrderCommand(
                request.customerId(),
                request.items().stream()
                        .map(i -> new CreateOrderCommand.ItemData(
                                i.eventId(), i.ticketType(), i.unitPrice(), i.quantity()))
                        .toList()
        );

        Order order = createOrderUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/pay")
    public ResponseEntity<OrderResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        var command = new ProcessPaymentCommand(request.orderId(), request.paymentMethod());
        Order order = processPaymentUseCase.execute(command);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // --- Exception Handlers ---

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }
}
