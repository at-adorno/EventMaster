# language: pt
Funcionalidade: Orquestração SAGA de Compra de Ingressos

  Cenário: Falha no pagamento e acionamento de compensação (Rollback SAGA)
    Dado que o usuário tem uma reserva ativa para o ingresso "VIP-002"
    Mas o Gateway de Pagamento recusa a transação
    Quando o serviço de orquestração processar o pedido
    Então o pagamento deve lançar uma exceção de falha
    E um evento "RELEASE_LOCK" deve ser publicado no tópico de compensação