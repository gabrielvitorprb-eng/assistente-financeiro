# Assistente Financeiro

Sistema web para controle de gastos pessoais com API em Java Spring Boot, banco PostgreSQL e frontend em React + Vite. O projeto tambem possui um fluxo de registro de lancamentos por mensagens simuladas de WhatsApp e webhook compativel com Twilio.

## Objetivo

Facilitar o controle financeiro pessoal permitindo que o usuario registre entradas, saidas, categorias e consulte um dashboard mensal. O MVP tambem permite que mensagens recebidas de um numero de WhatsApp sejam associadas ao usuario correto pelo telefone cadastrado.

## Problema Resolvido

Muitas pessoas registram gastos de forma manual, dispersa ou depois que ja esqueceram os detalhes. O Assistente Financeiro centraliza os lancamentos e permite uma experiencia mais rapida: o usuario pode informar gastos em linguagem natural, como `gastei 30 reais com lanche`, e o sistema cadastra o lancamento na conta correta.

## Tecnologias Usadas

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- BCrypt para criptografia de senhas
- PostgreSQL
- Maven
- React
- Vite
- JavaScript
- HTML/CSS
- Render para hospedagem do backend
- Vercel para hospedagem do frontend
- Neon para banco PostgreSQL gerenciado

## Principais Funcionalidades

- Cadastro e login simples de usuarios.
- Edicao de perfil com nome, email e telefone WhatsApp.
- Normalizacao de telefone WhatsApp com ou sem nono digito.
- Categorias financeiras por usuario.
- Lancamentos de entrada e saida.
- Dashboard mensal com entradas, saidas, saldo e gastos por categoria.
- Simulador de compra.
- Registro de lancamentos por WhatsApp simulado.
- Webhook Twilio para mensagens `application/x-www-form-urlencoded`.

## Requisitos Para Rodar

- Java 21 ou superior.
- Maven Wrapper do projeto (`mvnw` ou `mvnw.cmd`).
- Node.js e npm.
- PostgreSQL em execucao.
- Banco PostgreSQL criado para o projeto.

## Configuracao do Banco PostgreSQL

Crie um banco e um usuario para o projeto. Exemplo:

```sql
CREATE DATABASE assistente_financeiro;
CREATE USER assistente_user WITH PASSWORD 'sua_senha_aqui';
GRANT ALL PRIVILEGES ON DATABASE assistente_financeiro TO assistente_user;
```

O backend le as configuracoes por variaveis de ambiente. Um exemplo sem senha real esta em:

```text
src/main/resources/application-example.properties
```

Configuracao usada como referencia:

```properties
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/assistente_financeiro}
spring.datasource.username=${DB_USERNAME:assistente_user}
spring.datasource.password=${DB_PASSWORD:sua_senha_aqui}
spring.jpa.hibernate.ddl-auto=update
server.port=${PORT:8080}
app.frontend-url=${FRONTEND_URL:http://localhost:5173}
```

Variaveis esperadas no backend:

| Variavel | Exemplo | Descricao |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://host.neon.tech/db?sslmode=require` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | `neondb_owner` | Usuario do banco |
| `DB_PASSWORD` | `senha_configurada_no_provedor` | Senha do banco |
| `FRONTEND_URL` | `https://seu-projeto.vercel.app` | Origem permitida no CORS em producao |
| `PORT` | definido automaticamente pelo Render | Porta usada pelo servidor |

## Como Rodar o Backend

Na raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Em Linux/macOS:

```bash
./mvnw spring-boot:run
```

A API ficara disponivel em:

```text
http://localhost:8080
```

## Como Rodar o Frontend

Entre na pasta do frontend:

```powershell
cd frontend
npm install
npm run dev
```

O Vite exibira a URL local, normalmente:

```text
http://localhost:5173
```

Em desenvolvimento, o frontend pode usar o proxy local pelo caminho `/api`.
Tambem e possivel criar `frontend/.env` com:

```env
VITE_API_URL=http://localhost:8080
```

Na Vercel, `VITE_API_URL` deve apontar para a URL publica do backend no Render.

## Principais Endpoints da API

### Autenticacao e Usuario

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| POST | `/auth/register` | Cadastra novo usuario |
| POST | `/auth/login` | Login simples |
| GET | `/usuarios/{id}` | Consulta perfil do usuario |
| PUT | `/usuarios/{id}` | Atualiza perfil do usuario |

### Dashboard

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| GET | `/dashboard/mensal?usuarioId={id}&ano={ano}&mes={mes}` | Retorna resumo mensal |

### Categorias

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| GET | `/categorias?usuarioId={id}` | Lista categorias do usuario |
| POST | `/categorias` | Cria categoria |
| GET | `/categorias/{id}` | Busca categoria |
| PUT | `/categorias/{id}` | Atualiza categoria |
| DELETE | `/categorias/{id}` | Exclui categoria |

### Lancamentos

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| GET | `/lancamentos?usuarioId={id}` | Lista lancamentos do usuario |
| POST | `/lancamentos` | Cria lancamento |
| GET | `/lancamentos/{id}` | Busca lancamento |
| PUT | `/lancamentos/{id}` | Atualiza lancamento |
| DELETE | `/lancamentos/{id}` | Exclui lancamento |

### WhatsApp e Twilio

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| POST | `/webhook/whatsapp` | Processa mensagem simulada via JSON |
| POST | `/webhook/twilio/whatsapp` | Processa webhook Twilio via formulario |

### Simulador

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| POST | `/simulacoes` | Simula impacto de uma compra no saldo |

## Fluxo Sugerido Para Demonstracao

1. Iniciar PostgreSQL.
2. Subir backend com `.\mvnw.cmd spring-boot:run`.
3. Subir frontend com `npm run dev`.
4. Criar um usuario no frontend com nome, email, senha e telefone WhatsApp.
5. Conferir se as categorias padrao foram criadas.
6. Registrar um lancamento manual.
7. Abrir o dashboard e verificar entradas, saidas e saldo.
8. Abrir o WhatsApp simulado e enviar: `gastei 30 reais com lanche`.
9. Testar webhook Twilio:

```powershell
curl.exe -X POST "http://localhost:8080/webhook/twilio/whatsapp" -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "From=whatsapp:+553497895652" --data-urlencode "Body=gastei 30 reais com lanche"
```

## Deploy em Producao

### 1. Criar banco PostgreSQL no Neon

1. Acesse o Neon e crie um novo projeto PostgreSQL.
2. Copie a string de conexao do banco.
3. Use a versao JDBC da URL no backend, por exemplo:

```text
jdbc:postgresql://host-do-neon/neondb?sslmode=require
```

4. Guarde separadamente usuario e senha do banco para configurar no Render.

### 2. Publicar backend Spring Boot no Render

O projeto possui um `render.yaml` com uma configuracao base para o backend.
No Render, configure as variaveis:

```text
DATABASE_URL=jdbc:postgresql://host-do-neon/neondb?sslmode=require
DB_USERNAME=usuario_do_neon
DB_PASSWORD=senha_do_neon
FRONTEND_URL=https://seu-frontend.vercel.app
```

Comandos usados pelo Render:

```bash
./mvnw clean package -DskipTests
java -jar target/assistentefinanceiro-0.0.1-SNAPSHOT.jar
```

O Render define a variavel `PORT` automaticamente.

### 3. Publicar frontend React/Vite na Vercel

Na Vercel, configure o projeto apontando para a pasta `frontend`.

Variavel obrigatoria:

```text
VITE_API_URL=https://sua-api-no-render.onrender.com
```

Build command:

```bash
npm run build
```

Output directory:

```text
dist
```

Depois de publicar o frontend, copie a URL da Vercel e atualize `FRONTEND_URL` no Render.

### 4. Observacoes de banco

O projeto continua usando PostgreSQL com `spring.jpa.hibernate.ddl-auto=update` para o MVP.
Nao foram criadas migrations complexas neste momento. Como melhoria futura, recomenda-se adotar Flyway ou Liquibase para versionar alteracoes de schema com mais controle.

## Diagramas do Projeto

- [Diagrama de caso de uso](docs/diagramas/caso-de-uso.md)
- [Diagrama de classes](docs/diagramas/classes.md)
- [Diagrama de componentes](docs/diagramas/componentes.md)
- [Diagrama de banco de dados](docs/diagramas/banco-de-dados.md)

## Observacoes Sobre o MVP

- O login ainda e simples e nao usa JWT.
- As senhas sao armazenadas com BCrypt.
- A integracao com Twilio real ainda nao esta configurada.
- O processamento de WhatsApp aceita apenas texto.
- Nao ha suporte a audio ou imagem.
- O foco do MVP e demonstrar arquitetura, cadastro multiusuario, dashboard, lancamentos e fluxo por WhatsApp.

## Melhorias Futuras

- Implementar autenticacao com JWT.
- Criar testes automatizados mais completos para services e controllers.
- Criar migrations com Flyway ou Liquibase.
- Configurar Twilio real em ambiente de homologacao.
- Melhorar interpretacao de linguagem natural.
- Adicionar relatorios por periodo e exportacao de dados.
- Criar perfis de ambiente para desenvolvimento, teste e producao.
