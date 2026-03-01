package com.eventmaster.sales.application.port.in;

import com.eventmaster.sales.domain.entity.Order;
import java.math.BigDecimal;
import java.util.List;

/**
 * Porta de entrada — contrato para criação de pedidos.
 */
public interface CreateOrderUseCase {

    /**
     * Cria um novo pedido de ingressos.
     */
    Order execute(CreateOrderCommand command);

    /**
     * Comando imutável com os dados necessários para criar um pedido.
     */
    record CreateOrderCommand(
            String customerId,
            List<ItemData> items
    ) {
        public record ItemData(
                String eventId,
                String ticketType,
                BigDecimal unitPrice,
                int quantity
        ) {}
    }
}
