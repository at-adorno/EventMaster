# EventMaster — Relatório Técnico
## Papel 2: Domínio e Arquitetura Limpa (Core Backend)

---

## 1. Modelagem DDD — Contextos Delimitados

O sistema EventMaster foi decomposto em dois **Bounded Contexts** principais:

### 1.1 Contexto de Catálogo de Eventos
- **Responsabilidade:** gerenciar eventos, locais, datas, tipos de ingresso e disponibilidade (estoque).
- **Entidades principais:** `Event`, `Venue`, `TicketType`.
- **Escopo:** esse contexto é de responsabilidade do Papel 1/3 e se comunica com o contexto de Vendas via interface (porta).

### 1.2 Contexto de Vendas e Pagamentos (implementado)
- **Responsabilidade:** gerenciar pedidos de compra de ingressos, aplicar regras de negócio (cálculo de taxas, transições de estado) e processar pagamentos.
- **Aggregate Root:** `Order` — concentra as regras de validação e transições de estado.
- **Entidades/VOs:** `OrderItem`, `OrderStatus`.
- **Regras de negócio encapsuladas no domínio:**
  - Não permite adicionar itens a pedidos já confirmados.
  - Não permite confirmar pedidos vazios.
  - Taxa de serviço de 10% calculada no domínio.
  - Transições de estado controladas: `CREATED → CONFIRMED → PAID` e `CREATED/CONFIRMED → CANCELLED`.

```
┌─────────────────────────┐     ┌─────────────────────────────┐
│  Catálogo de Eventos    │     │  Vendas e Pagamentos        │
│  (Bounded Context 1)    │     │  (Bounded Context 2)        │
│                         │     │                             │
│  • Event                │     │  • Order  (Aggregate Root)  │
│  • Venue                │◄───►│  • OrderItem                │
│  • TicketType           │     │  • OrderStatus              │
│  • Disponibilidade      │     │  • PaymentGateway (Strategy)│
└─────────────────────────┘     └─────────────────────────────┘
```

---

## 2. Clean Architecture — Decisões e Justificativas

A arquitetura do microsserviço `sales-service` segue a **Arquitetura Limpa** (Robert C. Martin), organizando o código em camadas concêntricas com a regra de dependência apontando para dentro:

```
┌──────────────────────────────────────────────┐
│  Infrastructure (adapters)                    │
│  ├── web/         (REST Controller, DTOs)     │
│  ├── persistence/ (InMemoryOrderRepository)   │
│  └── payment/     (Pix, CreditCard, Boleto)   │
├──────────────────────────────────────────────┤
│  Application (use cases + ports)              │
│  ├── port/in/     (CreateOrderUseCase, ...)   │
│  ├── port/out/    (OrderRepositoryPort, ...)  │
│  └── service/     (implementações dos UseCases│
├──────────────────────────────────────────────┤
│  Domain (entities + regras de negócio)        │
│  ├── Order, OrderItem, OrderStatus            │
│  └── Regras de transição de estado            │
└──────────────────────────────────────────────┘
```

### Justificativas:
| Decisão | Motivo |
|---|---|
| **Domínio sem dependências externas** | A camada `domain` não importa Spring nem nenhum framework. Isso garante testabilidade e portabilidade. |
| **Portas (interfaces) na camada Application** | `OrderRepositoryPort` e `PaymentGatewayPort` permitem trocar implementações (ex: de in-memory para JPA) sem alterar o domínio. Princípio DIP (SOLID). |
| **Adaptadores na camada Infrastructure** | Controllers, repositórios e gateways de pagamento são "plugins" substituíveis. |
| **Commands como Records imutáveis** | `CreateOrderCommand` e `ProcessPaymentCommand` são DTOs de entrada imutáveis, evitando efeitos colaterais. |

---

## 3. Princípios SOLID Aplicados

| Princípio | Aplicação no Projeto |
|---|---|
| **S** — Single Responsibility | `Order` gerencia apenas estado e regras do pedido. `CreateOrderService` apenas orquestra criação. `ProcessPaymentService` apenas orquestra pagamento. |
| **O** — Open/Closed | Novos métodos de pagamento são adicionados criando novas implementações de `PaymentGatewayPort`, sem alterar `ProcessPaymentService`. |
| **L** — Liskov Substitution | Todas as implementações de `PaymentGatewayPort` (Pix, CreditCard, Boleto) são intercambiáveis. |
| **I** — Interface Segregation | Portas de entrada (`CreateOrderUseCase`) e saída (`OrderRepositoryPort`) são interfaces coesas e específicas. |
| **D** — Dependency Inversion | Os Use Cases dependem de abstrações (`OrderRepositoryPort`), não de implementações concretas. A injeção é feita pelo Spring. |

---

## 4. Design Pattern — Strategy

O **padrão Strategy** (GoF) foi aplicado para desacoplar os métodos de pagamento:

```
         ┌─────────────────────┐
         │ PaymentGatewayPort  │  ← Interface (Strategy)
         │  + processPayment() │
         │  + paymentMethod()  │
         └──────┬──────────────┘
                │
    ┌───────────┼───────────────┐
    │           │               │
┌───▼───┐  ┌───▼────┐  ┌───────▼──┐
│  Pix  │  │ Credit │  │  Boleto  │  ← Strategies concretas
│Gateway│  │  Card  │  │ Gateway  │
└───────┘  └────────┘  └──────────┘
```

**Como funciona:**
1. O Spring injeta automaticamente todas as implementações de `PaymentGatewayPort`.
2. `ProcessPaymentService` indexa as strategies por `paymentMethod()`.
3. No momento do pagamento, a strategy correta é selecionada pelo método informado pelo cliente (PIX, CREDIT_CARD, BOLETO).
4. Para adicionar um novo método (ex: PayPal), basta criar uma nova classe `@Component` que implemente `PaymentGatewayPort`.

---

## 5. Testes — Pirâmide de Testes (TDD)

### Base da pirâmide: Testes Unitários
Todos os testes foram escritos seguindo TDD, cobrindo:

| Classe de Teste | O que valida |
|---|---|
| `OrderTest` | Criação de pedidos, transições de estado (CREATED→CONFIRMED→PAID→CANCELLED), rejeições de transições inválidas |
| `OrderItemTest` | Criação de itens, cálculo de subtotal, validações de entrada |
| `CreateOrderServiceTest` | Caso de uso de criação com stubs isolados |
| `ProcessPaymentServiceTest` | Seleção da Strategy correta, pagamento aprovado/recusado, pedido inexistente, método não suportado |

### Isolamento:
- Os testes de Use Case usam **stubs** e **spies** manuais (sem mocks de framework), garantindo isolamento total da infraestrutura.
- A camada de domínio é testada sem nenhuma dependência externa.

---

## 6. Impacto na Escalabilidade e Manutenção

| Aspecto | Benefício |
|---|---|
| **Microsserviço isolado** | O `sales-service` pode ser escalado horizontalmente independente do catálogo. |
| **Domínio puro** | Regras de negócio não dependem de framework, facilitando migração e testes. |
| **Strategy para pagamentos** | Novos métodos de pagamento não exigem alteração no código existente (OCP). |
| **Portas e Adaptadores** | Trocar o repositório in-memory por JPA ou o gateway simulado por um real é uma mudança pontual na camada de infraestrutura. |
| **Testes automatizados** | Garantem que refatorações e evoluções não quebrem regras de negócio existentes. |

---

## 7. Estrutura de Pacotes

```
sales-service/src/main/java/com/eventmaster/sales/
├── domain/
│   └── entity/
│       ├── Order.java              (Aggregate Root)
│       ├── OrderItem.java          (Entidade)
│       └── OrderStatus.java        (Enum)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateOrderUseCase.java
│   │   │   └── ProcessPaymentUseCase.java
│   │   └── out/
│   │       ├── OrderRepositoryPort.java
│   │       └── PaymentGatewayPort.java
│   └── service/
│       ├── CreateOrderService.java
│       └── ProcessPaymentService.java
└── infrastructure/
    └── adapter/
        ├── payment/
        │   ├── PixPaymentGateway.java
        │   ├── CreditCardPaymentGateway.java
        │   └── BoletoPaymentGateway.java
        ├── persistence/
        │   └── InMemoryOrderRepository.java
        └── web/
            ├── OrderController.java
            └── dto/
                ├── CreateOrderRequest.java
                ├── OrderItemRequest.java
                ├── PaymentRequest.java
                └── OrderResponse.java
```
