# ⚠️ Gemini CLI - Modo Manual Ativado

## 🎯 O Que Mudou?

O **Gemini CLI** foi configurado para **executar APENAS sob demanda** (manual).

### ❌ Antes (Automático)
- ✗ Executava automaticamente quando você abria uma PR
- ✗ Executava automaticamente quando abria/reabria uma issue
- ✗ Gastava quota da API mesmo sem você pedir
- ✗ Falhava com erro de quota esgotada

### ✅ Agora (Manual)
- ✓ Só executa quando você chamar explicitamente com `@gemini-cli`
- ✓ Você tem controle total sobre quando usar
- ✓ Não gasta quota sem necessidade
- ✓ Jules e GitHub Copilot continuam funcionando normalmente

---

## 🤖 Seus Revisores Ativos

| Bot | Status | Como Funciona |
|-----|--------|---------------|
| **Jules** | ✅ Ativo | Responde quando você menciona `@jules` |
| **GitHub Copilot** | ✅ Ativo | Revisa automaticamente todas as PRs |
| **Gemini CLI** | ⏸️ Manual | Só executa quando você chamar `@gemini-cli` |

---

## 📝 Como Usar o Gemini Manualmente

Se você **quiser** que o Gemini faça uma revisão, basta comentar na PR:

### Revisão Completa
```
@gemini-cli /review
```

### Revisão com Foco Específico
```
@gemini-cli /review please focus on security issues
```

### Comando Customizado
```
@gemini-cli analyze the performance of this code
```

---

## 🔄 Como Reabilitar Execução Automática

Se você quiser que o Gemini volte a executar automaticamente, edite o arquivo:
`.github/workflows/gemini-dispatch.yml`

### Passo 1: Descomente a Condição (linha ~45-60)

**Encontre:**
```yaml
  dispatch:
    # GEMINI DESABILITADO PARA EXECUÇÃO AUTOMÁTICA
    # Só executa quando explicitamente chamado com @gemini-cli
    # Para reabilitar revisões automáticas, descomente as linhas abaixo
    if: |-
      (
        github.event.sender.type == 'User' &&
        startsWith(github.event.comment.body || github.event.review.body || github.event.issue.body, '@gemini-cli') &&
        contains(fromJSON('["OWNER", "MEMBER", "COLLABORATOR"]'), github.event.comment.author_association || github.event.review.author_association || github.event.issue.author_association)
      )
```

**Substitua por:**
```yaml
  dispatch:
    # For PRs: only if not from a fork
    # For issues: only on open/reopen
    # For comments: only if user types @gemini-cli and is OWNER/MEMBER/COLLABORATOR
    if: |-
      (
        github.event_name == 'pull_request' &&
        github.event.pull_request.head.repo.fork == false
      ) || (
        github.event_name == 'issues' &&
        contains(fromJSON('["opened", "reopened"]'), github.event.action)
      ) || (
        github.event.sender.type == 'User' &&
        startsWith(github.event.comment.body || github.event.review.body || github.event.issue.body, '@gemini-cli') &&
        contains(fromJSON('["OWNER", "MEMBER", "COLLABORATOR"]'), github.event.comment.author_association || github.event.review.author_association || github.event.issue.author_association)
      )
```

### Passo 2: Descomente o Script (linha ~92-112)

**Encontre:**
```javascript
// AUTOMAÇÃO DESABILITADA - Gemini só executa sob demanda
// Para reabilitar, descomente as linhas abaixo:
// if (eventType === 'pull_request.opened') {
//   core.setOutput('command', 'review');
// } else if (['issues.opened', 'issues.reopened'].includes(eventType)) {
//   core.setOutput('command', 'triage');
// } else

if (request.startsWith("@gemini-cli /review")) {
```

**Substitua por:**
```javascript
if (eventType === 'pull_request.opened') {
  core.setOutput('command', 'review');
} else if (['issues.opened', 'issues.reopened'].includes(eventType)) {
  core.setOutput('command', 'triage');
} else if (request.startsWith("@gemini-cli /review")) {
```

---

## ⚠️ Problema de Quota

Se você reabilitar e encontrar o erro:
```
TerminalQuotaError: You have exhausted your daily quota on this model.
```

**Soluções:**

1. **Aguardar reset da quota** (geralmente 24 horas)
2. **Usar um modelo diferente** - Configure `vars.GEMINI_MODEL` nas variáveis do repositório
3. **Aumentar quota** - Upgrade do plano da API Gemini
4. **Usar Vertex AI** - Configure `vars.GOOGLE_GENAI_USE_VERTEXAI=true`

---

## 📊 Logs e Debugging

Quando o Gemini executa (manualmente), você pode ver os logs:

1. O bot comenta: "🤖 Hi @username, I've received your request..."
2. Clique no link "[in the logs]" no comentário
3. Veja o que o Gemini fez e se houve erros

---

## ✅ Benefícios da Configuração Atual

1. **Controle Total** - Você decide quando usar o Gemini
2. **Economia de Quota** - Não gasta API calls desnecessárias
3. **Menos Ruído** - PRs não ficam cheias de comentários automáticos
4. **Jules e Copilot Funcionam** - Seus revisores principais não são afetados
5. **Disponível Quando Precisar** - Basta mencionar `@gemini-cli`

---

## ❓ FAQ

**P: Por que desabilitar o Gemini?**
R: Estava executando automaticamente, esgotando a quota diária e causando erros. Agora você controla quando usar.

**P: Jules e Copilot continuam funcionando?**
R: Sim! Nada mudou para eles. Continuam revisando automaticamente.

**P: Como sei se o Gemini está funcionando quando eu chamar?**
R: Ele comenta na PR dizendo "I've received your request" e fornece link dos logs.

**P: Posso desabilitar completamente?**
R: Sim, delete ou renomeie os arquivos em `.github/workflows/gemini-*.yml`

**P: Tem custo usar o Gemini manualmente?**
R: Depende da sua configuração. Se estiver usando API key gratuita, tem limite de quota diária.

---

**Mudança implementada em:** 2026-02-04  
**Razão:** Evitar execução automática e erros de quota esgotada  
**Reversível:** Sim, seguindo instruções acima
