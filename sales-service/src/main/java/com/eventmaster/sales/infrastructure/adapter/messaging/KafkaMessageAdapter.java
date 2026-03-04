package com.eventmaster.sales.infrastructure.adapter.messaging;

import com.eventmaster.sales.application.port.out.MessageBrokerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageAdapter implements MessageBrokerPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaMessageAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishEvent(String topic, Object eventPayload) {
        // Envia a mensagem assincronamente (Processamento Stream)
        kafkaTemplate.send(topic, eventPayload);
        System.out.println("[Kafka] Evento publicado no tópico: " + topic);
    }
}