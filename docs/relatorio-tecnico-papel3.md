```markdown
# EventMaster — Relatório Técnico
## Papel 3: Sistemas Distribuídos e Dados (Integration & Data)

---

## 1. Padrão SAGA e Consistência Distribuída

Para garantir a consistência de dados entre o estoque de ingressos (Catálogo) e o processamento financeiro (Vendas) sem utilizar transações distribuídas bloqueantes (Two-Phase Commit), implementamos o **Padrão SAGA via Orquestração**.

### 1.1 Orquestração vs Coreografia
- **Decisão:** Optamos pela Orquestração centralizada no `ProcessPaymentService`.
- **Justificativa:** Em sistemas de venda de ingressos, o estado da transação é crítico. O orquestrador atua como o "Maestro", comandando os passos e sabendo exatamente em qual etapa a compra parou, facilitando o rastreamento e o tratamento de erros.

### 1.2 Fluxo de Compensação (Rollback)
- **Caminho Feliz:** Pagamento aprovado $\rightarrow$ Publica `OrderEvent` no tópico `order-completed-topic`.
- **Falha/Compensação:** Pagamento recusado (ou gateway fora do ar) $\rightarrow$ Publica `CompensationEvent` (ação: `RELEASE_LOCK`) no tópico `ticket-compensation-topic`. O serviço de Catálogo consome este evento e devolve o ingresso para o estoque disponível.



```text
 ┌───────────────────────┐         ┌─────────────────────────┐
 │   Catálogo/Estoque    │         │   Vendas e Pagamentos   │
 │   (Bounded Context 1) │         │   (Bounded Context 2)   │
 └───────────▲───────────┘         └────────────┬────────────┘
             │                                  │
             │ 1. Reserva Ingresso              │ 2. Processa Pagamento
             │ (Lock)                           │ (CreditCard/Pix)
             │                                  ▼
 ┌───────────┴───────────┐         ┌─────────────────────────┐
 │     Message Broker    │◄────────┤  Orquestrador (SAGA)    │
 │     (Apache Kafka)    │ 3. Pub  │  ProcessPaymentService  │
 └───────────────────────┘         └─────────────────────────┘
      ▲             │
      │ 4. Consome  │ (Se falhar: ticket-compensation-topic)
      └─────────────┘ (Se sucesso: order-completed-topic)

```

---

## 2. Topologia de Processamento de Dados

O sistema lida com diferentes naturezas de dados, exigindo a separação do processamento para garantir performance e escalabilidade sob alta carga.

| Tipo de Processamento | Tecnologia | Caso de Uso no EventMaster | Benefício Arquitetural |
| --- | --- | --- | --- |
| **Stream (Tempo Real)** | Apache Kafka | Eventos do SAGA (`OrderEvent`, `CompensationEvent`) e Fila de Espera. | Atua como um *buffer* de alta performance. Absorve picos de concorrência (ex: abertura de vendas) sem derrubar o banco transacional principal. |
| **Batch (Em Lotes)** | Rotinas Agendadas (Cron) | Conciliação bancária e consolidação de relatórios financeiros diários. | Roda de madrugada em horários de baixo tráfego, otimizando o uso de recursos de computação na nuvem e reduzindo custos. |

---

## 3. Padrões de Resiliência — Circuit Breaker

Para evitar que a instabilidade de APIs financeiras externas cause um efeito cascata no EventMaster (esgotamento de *threads*), implementamos o padrão **Circuit Breaker** utilizando a biblioteca *Resilience4j*.

### Implementação:

* **Alvo:** O Circuit Breaker foi aplicado exclusivamente no adaptador `CreditCardPaymentGateway`, que depende de comunicação de rede com terceiros.
* **Configuração:**
* Janela de observação: 10 requisições (`slidingWindowSize`).
* Limite de falha: 50% (`failureRateThreshold`).
* Tempo de espera no estado aberto: 10 segundos (`waitDurationInOpenState`).


* **Comportamento (Fail Fast):** Se o circuito **abrir**, novas requisições falham instantaneamente acionando o método `fallbackCreditCard`. Esse fallback retorna `false`, o que instrui o Orquestrador SAGA a acionar imediatamente o rollback do ingresso, protegendo o sistema.

---

## 4. Testes — Pirâmide de Testes (BDD e Integração)

Complementando os testes unitários da camada de domínio, o foco do Papel 3 foi garantir que as integrações e fluxos de exceção funcionassem corretamente utilizando **Testes de Comportamento (BDD)** e validação de **Mocks**.

| Ferramenta / Abordagem | O que valida |
| --- | --- |
| **Cucumber (Gherkin)** | Validação das jornadas de negócio em linguagem ubíqua (Cenários: *Compra com Sucesso* vs *Falha no Pagamento e Acionamento de Compensação*). |
| **Test Doubles (Mocks)** | Criação do `MockMessageBroker` injetado na suíte de testes unitários (`ProcessPaymentServiceTest`) para validar isoladamente se os eventos do SAGA estão sendo engatilhados corretamente nas transições de sucesso e falha. |
| **Integração Spring** | Contexto do `@SpringBootTest` para garantir que as anotações do Resilience4j (`@CircuitBreaker`) e a injeção de dependências do Kafka atuem como esperado. |

---

## 5. Impacto na Escalabilidade e Manutenção

| Decisão | Benefício |
| --- | --- |
| **Mensageria Assíncrona** | Desacopla o tempo de resposta do sistema. O usuário não precisa esperar a confirmação síncrona de sistemas lentos; tudo flui por eventos. |
| **Consistência Eventual** | A aceitação de que os dados podem estar momentaneamente defasados entre serviços (mas convergirão) permite que o sistema escale horizontalmente ao infinito. |
| **Prevenção de Falhas em Cascata** | O Circuit Breaker garante que a queda da operadora de cartão de crédito não tire do ar a venda de ingressos via Pix ou Boleto. |
| **Isolamento de Infraestrutura** | A implementação do Kafka (`KafkaMessageAdapter`) está restrita à camada de infraestrutura. Se o projeto migrar para RabbitMQ ou AWS SQS, o domínio (`ProcessPaymentService`) permanece intocado. |

---

## 6. Estrutura de Pacotes (Adições e Integração)

```
sales-service/src/
├── main/java/com/eventmaster/sales/
│   ├── application/
│   │   ├── dto/
│   │   │   ├── OrderEvent.java                (Record imutável)
│   │   │   └── CompensationEvent.java         (Record imutável)
│   │   └── port/out/
│   │       └── MessageBrokerPort.java         (Porta de saída do Kafka)
│   └── infrastructure/
│       └── adapter/
│           ├── messaging/
│           │   ├── KafkaMessageAdapter.java   (Publisher)
│           │   └── KafkaConsumerAdapter.java  (Consumer do Rollback)
│           └── payment/
│               └── CreditCardPaymentGateway.java (+ Anotação @CircuitBreaker)
│
└── test/
    ├── java/com/eventmaster/sales/
    │   ├── application/service/
    │   │   └── ProcessPaymentServiceTest.java (+ MockMessageBroker integrado)
    │   └── steps/
    │       ├── CompraSagaSteps.java           (Step definitions)
    │       └── CucumberSpringConfiguration.java
    └── resources/
        └── features/
            └── compra_saga.feature            (Cenários BDD em Gherkin)

```