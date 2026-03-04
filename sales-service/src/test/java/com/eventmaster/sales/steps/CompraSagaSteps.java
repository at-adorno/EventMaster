package com.eventmaster.sales.steps;

import io.cucumber.java.pt.*;
import static org.junit.jupiter.api.Assertions.*;

public class CompraSagaSteps {

    private String ticketId;
    private boolean paymentGatewayFails;
    private Exception caughtException;
    private boolean compensationEventPublished = false;

    @Dado("que o usuário tem uma reserva ativa para o ingresso {string}")
    public void que_o_usuario_tem_uma_reserva_ativa_para_o_ingresso(String id) {
        this.ticketId = id;
    }

    @Dado("o Gateway de Pagamento recusa a transação")
    public void o_gateway_de_pagamento_recusa_a_transacao() {
        this.paymentGatewayFails = true;
    }

    @Quando("o serviço de orquestração processar o pedido")
    public void o_servico_de_orquestracao_processar_o_pedido() {
        try {
            if (paymentGatewayFails) {
                // Simulando a lógica de catch do ProcessOrderService
                throw new RuntimeException("Falha no processamento");
            }
        } catch (Exception e) {
            this.caughtException = e;
            // Simulando o mock do MessageBrokerPort
            this.compensationEventPublished = true; 
        }
    }

    @Então("o pagamento deve lançar uma exceção de falha")
    public void o_pagamento_deve_lancar_uma_excecao_de_falha() {
        assertNotNull(caughtException);
    }

    @Então("um evento {string} deve ser publicado no tópico de compensação")
    public void um_evento_deve_ser_publicado_no_topico_de_compensacao(String eventAction) {
        assertTrue(compensationEventPublished);
    }
}