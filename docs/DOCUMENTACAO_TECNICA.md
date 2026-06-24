# Documentacao Tecnica

## Visao Geral

O Assistente Financeiro e uma aplicacao web dividida em duas partes:

- Backend: API REST em Java Spring Boot.
- Frontend: SPA em React + Vite.

O backend centraliza regras de negocio, persistencia e processamento de mensagens de WhatsApp. O frontend consome a API e oferece telas para login, perfil, dashboard, categorias, lancamentos, simulador e WhatsApp simulado.

## Arquitetura

O projeto segue uma organizacao em camadas:

```text
Controller -> Service -> Repository -> Banco de Dados
```

### Controllers

Recebem requisicoes HTTP, validam DTOs de entrada e delegam o processamento para os services.

Principais controllers:

- `AuthController`
- `UsuarioController`
- `DashboardController`
- `CategoriaController`
- `LancamentoController`
- `SimulacaoCompraController`
- `WhatsappWebhookController`

### Services

Concentram regras de negocio e coordenam operacoes entre repositorios.

Principais services:

- `UsuarioService`: cadastro, login simples, perfil e normalizacao de telefone.
- `CategoriaService`: CRUD de categorias por usuario.
- `LancamentoService`: CRUD de lancamentos e validacao de categoria do usuario.
- `DashboardService`: consolidacao mensal de entradas, saidas e saldo.
- `WhatsappService`: interpretacao de mensagens e criacao de lancamentos.
- `SimulacaoCompraService`: calculo de impacto de compras.

### Repositories

Interfaces Spring Data JPA responsaveis pelo acesso ao banco PostgreSQL.

### DTOs

Objetos usados para entrada e saida da API, evitando expor diretamente as entidades em todos os contratos HTTP.

## Entidades Principais

### Usuario

Representa uma pessoa que usa o sistema.

Campos principais:

- `id`
- `nome`
- `email`
- `senha`
- `telefoneWhatsapp`
- `dataCriacao`

O telefone WhatsApp identifica o usuario no fluxo de mensagens.

### Categoria

Classifica lancamentos financeiros.

Campos principais:

- `id`
- `nome`
- `cor`
- `usuario`

Cada usuario possui suas proprias categorias.

### Lancamento

Representa entrada ou saida financeira.

Campos principais:

- `id`
- `descricao`
- `valor`
- `data`
- `tipo`
- `formaPagamento`
- `usuario`
- `categoria`

### MensagemWhatsapp

Registra mensagens recebidas no fluxo WhatsApp.

Campos principais:

- `id`
- `telefone`
- `texto`
- `tipoMensagem`
- `intencao`
- `status`
- `recebidaEm`
- `resposta`
- `usuario`

### SimulacaoCompra

Representa uma simulacao de compra e seu impacto no saldo mensal.

## Regras Principais

### Multiusuario

Cada usuario possui seus proprios lancamentos, categorias e dashboard. As consultas recebem `usuarioId` para filtrar os dados.

### Telefone WhatsApp

O telefone e normalizado removendo:

- `whatsapp:`
- `+`
- espacos
- parenteses
- hifen
- pontos

O sistema aceita telefone com 12 ou 13 digitos iniciando com `55`. Quando o telefone vem sem `55`, com 10 ou 11 digitos, o backend adiciona `55`.

Na busca por WhatsApp, o sistema tenta localizar o usuario pelo numero normalizado e tambem por variacoes com e sem nono digito.

### Categorias Padrao

Ao registrar um usuario, o sistema cria categorias padrao como Alimentacao, Moradia, Transporte, Lazer, Saude, WhatsApp, Salario, Mercado, Pix e outras. A criacao evita duplicidade para o mesmo usuario.

### Lancamentos

Todo lancamento pertence a um usuario e a uma categoria. O service valida se a categoria informada pertence ao usuario informado.

### Dashboard

O dashboard mensal calcula:

- total de entradas;
- total de saidas;
- saldo mensal;
- gastos por categoria.

### WhatsApp

O `WhatsappService` identifica a intencao da mensagem:

- cadastrar gasto;
- cadastrar entrada;
- consultar saldo;
- consultar gastos do mes;
- simulacao;
- mensagem desconhecida.

Para gastos e entradas, o sistema extrai valor, monta descricao, detecta forma de pagamento e tenta detectar a categoria.

## Fluxo do Sistema

### Cadastro e Login

1. Usuario se cadastra em `/auth/register`.
2. Backend normaliza telefone e cria categorias padrao.
3. Usuario faz login em `/auth/login`.
4. Frontend salva o usuario no `localStorage`.

### Lancamento Manual

1. Frontend envia `POST /lancamentos`.
2. Backend valida usuario e categoria.
3. Lancamento e salvo.
4. Dashboard passa a considerar o novo lancamento.

### Lancamento via WhatsApp/Twilio

1. Twilio envia `From` e `Body` para `/webhook/twilio/whatsapp`.
2. Backend normaliza o telefone.
3. Backend busca o usuario pelo telefone e variacoes com/sem nono digito.
4. Backend interpreta a mensagem.
5. Backend cria o lancamento para o usuario encontrado.
6. API responde em TwiML.

## Frontend

O frontend React possui telas principais:

- Login/Cadastro
- Perfil
- Dashboard
- Categorias
- WhatsApp simulado
- Lancamentos
- Simulador

O usuario autenticado e armazenado no `localStorage`. Ao editar perfil, o retorno atualizado da API substitui o usuario salvo localmente.

## Banco de Dados

O projeto usa PostgreSQL com `spring.jpa.hibernate.ddl-auto=update` para facilitar o MVP. Para uma versao de producao, recomenda-se usar Flyway ou Liquibase.

## Limitacoes Tecnicas do MVP

- Login sem JWT.
- Senhas armazenadas com BCrypt, ainda sem fluxo de recuperacao de senha.
- Configuracao por variaveis de ambiente para deploy, ainda sem perfis avancados por ambiente.
- Sem migrations versionadas.
- Cobertura automatizada de testes ainda basica.
- Intepretacao de texto limitada a padroes simples.
