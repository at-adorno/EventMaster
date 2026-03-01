# EventMaster — Sistema de Gestão de Eventos (Microsserviço de Vendas)

Este repositório contém o esqueleto do microsserviço de **Vendas/Pagamentos** do sistema EventMaster, seguindo Clean Architecture, DDD e princípios SOLID.

## Visão Geral
- **Tecnologia:** Java 21, Spring Boot 3, Maven
- **Arquitetura:** Clean Architecture (Domain, Application, Infrastructure)
- **Padrão GoF:** Strategy para métodos de pagamento
- **Testes:** TDD, cobertura unitária completa

## Estrutura de Pastas
```
sales-service/
├── src/main/java/com/eventmaster/sales/
│   ├── domain/entity/         # Entidades e regras de negócio
│   ├── application/port/in/   # Portas de entrada (casos de uso)
│   ├── application/port/out/  # Portas de saída (interfaces de infraestrutura)
│   ├── application/service/   # Implementações dos casos de uso
│   └── infrastructure/adapter/# Adaptadores (REST, persistência, pagamentos)
│
├── src/test/java/com/eventmaster/sales/ # Testes unitários (TDD)
└── pom.xml
```

## Como rodar
1. **Pré-requisitos:** Java 21, Maven 3.8+
2. **Build e testes:**
   ```sh
   cd sales-service
   mvn clean test
   ```
3. **Executar localmente:**
   ```sh
   mvn spring-boot:run
   ```
4. **APIs disponíveis:**
   - `POST /api/orders` — cria pedido de ingressos
   - `POST /api/orders/pay` — processa pagamento (PIX, CREDIT_CARD, BOLETO)

## Exemplos de Requisição
### Criar Pedido
```json
POST /api/orders
{
  "customerId": "cliente-123",
  "items": [
    { "eventId": "evento-1", "ticketType": "VIP", "unitPrice": 100.0, "quantity": 2 }
  ]
}
```

### Pagar Pedido
```json
POST /api/orders/pay
{
  "orderId": "<id do pedido>",
  "paymentMethod": "PIX"
}
```

## Testes
- Todos os testes unitários podem ser executados com `mvn test`.
- Cobertura de regras de negócio, transições de estado e seleção de strategy.

## Documentação
- Relatório técnico detalhado: [`docs/relatorio-tecnico-papel2.md`](docs/relatorio-tecnico-papel2.md)

## Próximos Passos
- Integração com API Gateway, autenticação e mensageria (outros papéis do projeto)
- Persistência real (JPA/MongoDB) e gateways de pagamento reais