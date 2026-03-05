## Projeto EventMaster: Sistema de Gestão de Eventos Escalável

> [!abstract] Resumo do Projeto Transição de um sistema monolítico para uma arquitetura de microsserviços resiliente, utilizando **arquitetura limpa**, **DDD** e padrões de segurança.

### 1. Visão Geral da Transição

A migração visa resolver problemas críticos de latência, inconsistência e segurança. Utilizamos o padrão de estrangulamento para garantir que o negócio continue operando enquanto extraímos as funcionalidades.
<br>

### 2. A Arquitetura Monolítica (Legado)

No modelo original, todos os módulos eram fortemente acoplados, partilhando o mesmo tempo de execução e base de dados.

![[monolitica.drawio 1.svg|501]]

<div style="page-break-after: always;"></div>
  

### 3. Arquitetura Base

Para que o sistema suporte picos de acesso sem colapsar, implementamos três componentes críticos:

#### API Gateway 

O Gateway é a única porta de entrada. Ele resolve o problema da exposição direta:
- **Segurança Centralizada:** Valida o JWT e barra ataques de força bruta antes que cheguem aos serviços.
- **Rate Limiting:** Impede que um único usuário ou bot sobrecarregue o sistema com milhares de requisições por segundo.

#### Circuit Breaker

Essencial na comunicação com o `Pagamento`. Se o gateway de pagamento externo ficar lento ou cair:
- O circuito abre, impedindo que novas requisições fiquem "penduradas" consumindo memória.
- O sistema retorna um erro amigável imediatamente ("Sistema de pagamentos temporariamente instável").
- Evita o efeito cascata, onde a lentidão de um serviço externo derruba todo o EventMaster.

#### Service Discovery

Na arquitetura de microsserviços, endereços IPs mudam constantemente:
- O Service Discovery mantém um registro dinâmico de todas as instâncias ativas.


<div style="page-break-after: always;"></div>
  

### 4. Nova Arquitetura: Microsserviços e Eventos

A nova estrutura utiliza um API Gateway como ponto central de segurança e um Message Broker para coreografia de eventos.

![[microsservicos.drawio.svg]]

<br><br>
### 5. Fluxo de Autenticação (OAuth 2.0 + JWT)

O API Gateway atua como o Authorization Server.

![[autentica.svg|697]]

<br>
### 6. Domain-Driven Design (DDD)

- **Contexto de Borda:** Segurança e Gatekeeping.
- **Contexto de Clientes:** Cadastro e CRM.
- **Contexto de Catálogo:** Consulta de eventos.
- **Contexto de Vendas:** Transações e Inventário.

<br>
### 7. Resiliência: SAGA Coreografada

Não existe um "mestre" central. Os serviços reagem a eventos:

1. `Venda/Reserva` reserva e emite evento.
2. `Pagamento` cobra e emite resultado.
3. Em caso de erro, o `Venda/Reserva` escuta o falha e executa a compensação (libera o ingresso).

<br>
### 8. Performance e Monitoramento

- **Stream Processing:** Ingestão de logs de acesso via Broker para detecção de anomalias em tempo real.
- **Batch Processing:** Consolidação de relatórios financeiros diários (processamento pesado fora do horário de pico).

<br>
### 9. Segurança (OWASP Top 10)

> [!important] Implementações de Segurança
> 
> - **A01:2021-Broken Access Control:** O Gateway valida as permissões e garante que um utilizador só acesse ao seu próprio perfil.
>     
> - **A03:2021-Injection:** Validação rigorosa de inputs no cadastro de clientes para evitar XSS e Injeção de SQL.
>     
> - **Zero Trust:** Comunicação via mTLS entre o Gateway e os microsserviços internos, garantindo que nenhum tráfego interno seja considerado confiável por padrão.
>     
