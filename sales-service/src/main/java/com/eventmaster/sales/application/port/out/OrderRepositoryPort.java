package com.eventmaster.sales.application.port.out;

import com.eventmaster.sales.domain.entity.Order;
import java.util.Optional;

/**
 * Porta de saída — contrato para persistência de pedidos.
 * A implementação concreta ficará na camada de infraestrutura.
 */
public interface OrderRepositoryPort {

    void save(Order order);

    Optional<Order> findById(String orderId);
}
