# EventMaster — Sistema de Gestão de Eventos

Este repositório contém o esqueleto do microsserviço de **Vendas e Pagamentos** do sistema EventMaster. O projeto foi desenhado sob os preceitos de **Clean Architecture**, **Domain-Driven Design (DDD)** e **Sistemas Distribuídos** (Padrão SAGA e Circuit Breaker).
Elaborado pelos alunos: André, Fabrício e Willy.

A documentação completa das decisões arquiteturais tomadas pelo grupo encontra-se em:
[Relatório Técnico](docs/relatorio-tecnico.md)

---

## Tecnologias Utilizadas
- **Linguagem / Framework:** Java 21, Spring Boot 3
- **Mensageria (Eventos SAGA):** Apache Kafka via Docker
- **Resiliência:** Resilience4j (Circuit Breaker)
- **Testes:** JUnit 5, Mockito, Cucumber (BDD)

---

## Como Executar o Projeto

### 1. Subir a Infraestrutura (Kafka e Zookeeper)
O microsserviço depende do Kafka para processamento de filas e eventos de compensação (Rollback SAGA).
```bash
docker-compose up -d
```
*(Para verificar se estão rodando, use `docker ps`)*

### 2. Rodar os Testes (Validação 100% Funcional)
A aplicação possui cobertura completa das regras de negócio (TDD) e testes de comportamento de integração (BDD).
```bash
cd sales-service
mvn clean test
```
*Garantia: Todos os cenários de compra, transição estrita de status, cálculos de taxas e estornos via Mock Kafka passarão com sucesso.*

### 3. Executar o Microsserviço Localmente
Com o Kafka rodando via Docker, inicie o Spring Boot:
```bash
cd sales-service
mvn spring-boot:run
```
O servidor estará rodando em `http://localhost:8080`.

---

## Testando a API (Exemplos via cURL)

**1. Criar um Pedido**
Reserva os ingressos e cria a *Order* no status `CREATED` ou `CONFIRMED`.
```bash
curl -s -X POST http://localhost:8080/api/orders \
-H "Content-Type: application/json" \
-d '{ 
  "customerId": "cliente-01", 
  "items": [
    {"eventId": "evento-x", "ticketType": "VIP", "unitPrice": 100, "quantity": 2}
  ] 
}'
```

*(Pegue o `id` gerado na resposta para usar no próximo passo)*

**2. Pagar o Pedido**
Métodos disponíveis: `PIX`, `BOLETO` ou `CREDIT_CARD`.
```bash
curl -s -X POST http://localhost:8080/api/orders/pay \
-H "Content-Type: application/json" \
-d '{
  "orderId": "<COLE-O-ID-AQUI>", 
  "paymentMethod": "PIX"
}'
```

*(Nota: Tentar pagar via `CREDIT_CARD` várias vezes seguidas acionará o Circuit Breaker simulado, testando a resiliência).*
