package com.eventmaster.sales.infrastructure.adapter.persistence;

import com.eventmaster.sales.application.port.out.OrderRepositoryPort;
import com.eventmaster.sales.domain.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação em memória do repositório de pedidos.
 * Pode ser substituída futuramente por JPA/MongoDB sem alterar o domínio.
 */
@Repository
public class InMemoryOrderRepository implements OrderRepositoryPort {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }
}
