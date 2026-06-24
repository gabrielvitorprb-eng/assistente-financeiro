# Testes Manuais

Tabela de casos sugeridos para validacao manual do MVP.

| Funcionalidade | Entrada | Resultado esperado | Status |
| --- | --- | --- | --- |
| Cadastro de usuario | Nome, email, senha e telefone WhatsApp valido | Usuario criado, telefone normalizado e categorias padrao criadas | Pendente |
| Login | Email e senha cadastrados | API retorna `usuarioId`, nome, email e telefone WhatsApp | Pendente |
| Login invalido | Email inexistente ou senha incorreta | API retorna erro informando credenciais invalidas | Pendente |
| Consulta de perfil | `GET /usuarios/{id}` | Retorna dados do usuario informado | Pendente |
| Edicao de perfil | Alterar nome, email e telefone WhatsApp | Dados salvos e retorno atualizado no frontend/localStorage | Pendente |
| Normalizacao de telefone com 13 digitos | `5534997895652` | Telefone salvo como `5534997895652` | Pendente |
| Normalizacao de telefone com 12 digitos | `553497895652` | Telefone salvo como `553497895652` | Pendente |
| Normalizacao sem codigo do pais | `34997895652` | Telefone salvo com prefixo `55` | Pendente |
| Duplicidade de email | Cadastrar ou editar usuario com email de outro usuario | API retorna erro de email ja cadastrado | Pendente |
| Duplicidade de telefone | Cadastrar ou editar usuario com telefone de outro usuario | API retorna erro de telefone ja cadastrado | Pendente |
| Listar categorias | `GET /categorias?usuarioId={id}` | Retorna categorias do usuario logado | Pendente |
| Criar categoria | Nome, cor e `usuarioId` | Categoria criada para o usuario | Pendente |
| Editar categoria | Novo nome ou cor | Categoria atualizada | Pendente |
| Excluir categoria | ID da categoria | Categoria removida ou erro se houver restricao de uso | Pendente |
| Criar lancamento manual | Descricao, valor, data, tipo, forma de pagamento, usuario e categoria | Lancamento salvo para o usuario correto | Pendente |
| Editar lancamento | Alterar dados de lancamento existente | Lancamento atualizado | Pendente |
| Excluir lancamento | ID do lancamento | Lancamento removido | Pendente |
| Dashboard mensal | `usuarioId`, ano e mes | Retorna entradas, saidas, saldo e gastos por categoria | Pendente |
| WhatsApp simulado - gasto | `gastei 30 reais com lanche` | Cria saida na categoria detectada e retorna mensagem de sucesso | Pendente |
| WhatsApp simulado - entrada | `recebi 2000 salario` | Cria entrada na categoria detectada e retorna mensagem de sucesso | Pendente |
| WhatsApp simulado - saldo | `qual meu saldo?` | Retorna resumo de entradas, saidas e saldo do mes | Pendente |
| Webhook Twilio com nono digito | `From=whatsapp:+5534997895652`, `Body=gastei 30 reais com lanche` | Localiza usuario e cria lancamento | Pendente |
| Webhook Twilio sem nono digito | `From=whatsapp:+553497895652`, `Body=gastei 30 reais com lanche` | Localiza usuario pela variacao e cria lancamento | Pendente |
| Webhook de telefone nao cadastrado | `From=whatsapp:+5500000000000` | Retorna mensagem informando telefone nao cadastrado | Pendente |
| Simulador de compra | Valor, forma de pagamento, parcelas, ano, mes e usuario | Retorna saldo antes, saldo depois e mensagem de impacto | Pendente |
| Persistencia no frontend | Login e recarregar pagina | Usuario continua salvo no `localStorage` | Pendente |
| Sair do sistema | Botao Sair | Usuario removido do `localStorage` e tela de login exibida | Pendente |

## Comandos de Verificacao

Backend:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm run build
```

## Exemplo de Teste Twilio

```powershell
curl.exe -X POST "http://localhost:8080/webhook/twilio/whatsapp" -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "From=whatsapp:+553497895652" --data-urlencode "Body=gastei 30 reais com lanche"
```
