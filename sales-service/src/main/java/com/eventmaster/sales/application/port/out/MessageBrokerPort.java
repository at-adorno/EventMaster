package com.eventmaster.sales.application.port.out;

public interface MessageBrokerPort {
    void publishEvent(String topic, Object eventPayload);
} 