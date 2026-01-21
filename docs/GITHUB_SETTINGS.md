# Configuracoes do GitHub - FalaSério

Guia para configurar repositorio privado e liberar acesso a ferramentas.

---

## Indice

1. [Tornar Repositorio Privado](#1-tornar-repositorio-privado)
2. [Liberar Acesso ao Claude Code](#2-liberar-acesso-ao-claude-code)
3. [Liberar Acesso ao Android Studio](#3-liberar-acesso-ao-android-studio)
4. [Tokens e Chaves de Acesso](#4-tokens-e-chaves-de-acesso)
5. [Colaboradores e Permissoes](#5-colaboradores-e-permissoes)

---

## 1. Tornar Repositorio Privado

### Via Interface Web

1. Acesse [github.com/rogerluft/falaserio](https://github.com/rogerluft/falaserio)
2. Clique em **Settings** (engrenagem)
3. Role ate **Danger Zone** no final da pagina
4. Clique em **Change repository visibility**
5. Selecione **Make private**
6. Digite o nome do repositorio para confirmar
7. Clique **I understand, change repository visibility**

### Consideracoes

- Repositorios privados gratuitos tem limite de colaboradores
- GitHub Pro/Team tem colaboradores ilimitados
- Forks publicos continuarao publicos (se houver)

---

## 2. Liberar Acesso ao Claude Code

### 2.1 Via GitHub App (Recomendado)

1. Acesse [github.com/apps/claude](https://github.com/apps/claude)
2. Clique em **Install**
3. Selecione sua conta ou organizacao
4. Escolha:
   - **All repositories**: Acesso a todos os repos
   - **Only select repositories**: Selecione `falaserio`
5. Clique **Install**

### 2.2 Via Personal Access Token (PAT)

1. Acesse [github.com/settings/tokens](https://github.com/settings/tokens)
2. Clique **Generate new token** > **Fine-grained token**
3. Configure:
   - **Token name**: `claude-code-falaserio`
   - **Expiration**: 90 days (ou custom)
   - **Repository access**: Only select repositories > `falaserio`
4. Permissoes necessarias:
   ```
   Contents: Read and write
   Metadata: Read-only
   Pull requests: Read and write
   Issues: Read and write
   ```
5. Clique **Generate token**
6. Copie o token (so aparece uma vez!)

### 2.3 Configurar Token no Claude Code

```bash
# Via variavel de ambiente
export GITHUB_TOKEN="ghp_xxxxxxxxxxxx"

# Ou via git credential
git config --global credential.helper store
```

### 2.4 Verificar Acesso

No Claude Code, tente:
```bash
git clone https://github.com/rogerluft/falaserio.git
```

---

## 3. Liberar Acesso ao Android Studio

### 3.1 Configurar Credenciais Git

1. **Abra Android Studio**
2. `File > Settings > Version Control > Git`
3. Verifique o caminho do Git
4. `Version Control > GitHub`
5. Clique **Add account**

### 3.2 Autenticar via Browser (Recomendado)

1. Selecione **Log In via GitHub**
2. Abre navegador automaticamente
3. Autorize o Android Studio
4. Volte ao Android Studio

### 3.3 Autenticar via Token

1. Selecione **Log In with Token**
2. Cole o Personal Access Token criado anteriormente
3. Clique **Add Account**

### 3.4 Verificar Conexao

1. `Git > Clone`
2. Busque por `falaserio`
3. O repositorio deve aparecer na lista

---

## 4. Tokens e Chaves de Acesso

### 4.1 Tipos de Tokens

| Tipo | Uso | Validade |
|------|-----|----------|
| **Fine-grained PAT** | Acesso granular a repos especificos | Ate 1 ano |
| **Classic PAT** | Acesso amplo (legacy) | Ate indefinido |
| **OAuth App** | Integracao de apps terceiros | Permanente |
| **GitHub App** | Instalacao em repos | Permanente |

### 4.2 Criar Fine-grained Token (Recomendado)

```
1. github.com/settings/tokens?type=beta
2. Generate new token
3. Configurar:
   - Nome descritivo
   - Expiracao
   - Repositorios especificos
   - Permissoes minimas necessarias
```

### 4.3 Escopos para Desenvolvimento

**Minimo para Claude Code / Android Studio:**
```
repo (Full control) - ou granular:
  - contents: read/write
  - metadata: read
  - pull_requests: read/write
```

**Para CI/CD (GitHub Actions):**
```
repo
workflow
packages (se usar GitHub Packages)
```

### 4.4 Seguranca

- NUNCA commite tokens no repositorio
- Use variaveis de ambiente ou secrets
- Revogue tokens comprometidos imediatamente
- Use expiracao curta quando possivel

---

## 5. Colaboradores e Permissoes

### 5.1 Adicionar Colaborador

1. `Settings > Collaborators`
2. Clique **Add people**
3. Busque pelo username ou email
4. Selecione o nivel de acesso

### 5.2 Niveis de Acesso

| Nivel | Permissoes |
|-------|------------|
| **Read** | Ver codigo, issues, PRs |
| **Triage** | + Gerenciar issues/PRs |
| **Write** | + Push, merge PRs |
| **Maintain** | + Gerenciar repo (exceto delete) |
| **Admin** | Controle total |

### 5.3 Branch Protection

Para proteger a branch `master`:

1. `Settings > Branches`
2. **Add branch protection rule**
3. Branch name: `master`
4. Opcoes recomendadas:
   - [x] Require pull request before merging
   - [x] Require status checks to pass
   - [x] Require conversation resolution

### 5.4 Deploy Keys (Para Servidores)

Para servidores que precisam apenas clonar:

1. `Settings > Deploy keys`
2. **Add deploy key**
3. Cole a chave publica SSH
4. Marque "Allow write access" se necessario

---

## Checklist de Seguranca

- [ ] Repositorio privado configurado
- [ ] Tokens com escopo minimo
- [ ] Tokens com expiracao definida
- [ ] Branch protection ativada
- [ ] Secrets em variaveis de ambiente
- [ ] `.gitignore` com arquivos sensiveis
- [ ] Nenhuma credencial no historico git

---

## Ferramentas que Precisam de Acesso

| Ferramenta | Tipo de Acesso | Como Configurar |
|------------|---------------|-----------------|
| Claude Code | GitHub App ou PAT | Secao 2 |
| Android Studio | OAuth ou PAT | Secao 3 |
| GitHub Actions | GITHUB_TOKEN automatico | Ja configurado |
| Dependabot | Automatico | Ja configurado |
| CodeQL | Automatico | Ja configurado |

---

*Documento atualizado em: Janeiro 2026*
