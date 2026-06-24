# Assistente Financeiro Frontend

Interface React responsiva para apresentar o MVP do Assistente Financeiro via WhatsApp.

## Requisitos

- Node.js instalado
- Backend Spring Boot rodando em `http://localhost:8080`
- Usuario demo com `usuarioId=1`

## Como rodar

```bash
npm install
npm run dev
```

Depois acesse a URL exibida pelo Vite, normalmente `http://localhost:5173`.

## Telas

- Dashboard mensal consumindo `GET /dashboard/mensal?usuarioId=1&ano=2026&mes=5`
- Categorias consumindo `GET /categorias?usuarioId=1`
- Chat WhatsApp simulado consumindo `POST /webhook/whatsapp`
- Lancamentos consumindo `GET /lancamentos?usuarioId=1`
- Simulador consumindo `POST /simulacoes`

O telefone usado no chat simulado e `34999999999`.
