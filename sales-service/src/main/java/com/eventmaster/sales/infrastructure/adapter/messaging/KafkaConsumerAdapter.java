package com.eventmaster.sales.infrastructure.adapter.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerAdapter {

    // Escuta o tópico de compensação que definimos no ProcessOrderService
    @KafkaListener(topics = "ticket-compensation-topic", groupId = "sales-group")
    public void consumeCompensationEvent(String message) {
        System.out.println("=================================================");
        System.out.println("[SAGA - Consumer] Evento de Compensação Recebido!");
        System.out.println("Mensagem: " + message);
        System.out.println("[SAGA - Consumer] Ação: Liberando a trava do ingresso no banco de dados...");
        System.out.println("=================================================");
        
        // Aqui, em um cenário real, você faria a chamada para o banco de dados 
        // ou para o serviço de Catálogo para alterar o status do ingresso para "DISPONÍVEL" novamente.
    }
}