# Git Workflow - FalaSério

Guia completo de operacoes Git para o projeto.

---

## Fluxo Basico Diario

```
1. git pull          # Atualizar codigo local
2. [fazer mudancas]  # Editar codigo
3. git add .         # Preparar mudancas
4. git commit -m ""  # Salvar mudancas localmente
5. git push          # Enviar para GitHub
```

---

## Operacoes Detalhadas

### Pull (Baixar atualizacoes)

**Via Android Studio:**
- `Git > Pull` ou `Ctrl+T`

**Via terminal:**
```bash
git pull origin master
```

**Quando usar:**
- Inicio do dia de trabalho
- Antes de comecar nova feature
- Apos alguem fazer push no repositorio

### Commit (Salvar mudancas)

**Via Android Studio:**
1. `Alt+9` para abrir painel Git
2. Selecione arquivos em "Unversioned Files" ou "Changes"
3. `Ctrl+K` para abrir janela de commit
4. Escreva mensagem descritiva
5. Clique "Commit"

**Via terminal:**
```bash
git add .
git commit -m "feat: adicionar nova funcionalidade"
```

**Convencao de mensagens:**
```
feat: nova funcionalidade
fix: correcao de bug
docs: documentacao
style: formatacao (sem mudanca de codigo)
refactor: refatoracao
test: testes
chore: manutencao (build, deps)
```

### Push (Enviar para GitHub)

**Via Android Studio:**
- `Git > Push` ou `Ctrl+Shift+K`

**Via terminal:**
```bash
git push origin master
```

### Fetch (Ver atualizacoes sem aplicar)

**Via Android Studio:**
- `Git > Fetch`

**Via terminal:**
```bash
git fetch origin
```

**Diferenca Fetch vs Pull:**
- `fetch`: Baixa info do remoto, NAO aplica
- `pull`: Baixa E aplica (fetch + merge)

---

## Branches

### Ver branch atual

**Android Studio:** Canto inferior direito

**Terminal:**
```bash
git branch
```

### Criar nova branch

**Android Studio:**
1. Clique no nome da branch (canto inferior direito)
2. "New Branch"
3. Digite o nome

**Terminal:**
```bash
git checkout -b feature/minha-feature
```

### Trocar de branch

**Android Studio:**
1. Clique no nome da branch
2. Selecione a branch desejada

**Terminal:**
```bash
git checkout master
git checkout feature/minha-feature
```

### Merge (Juntar branches)

**Android Studio:**
1. Esteja na branch destino (ex: master)
2. `Git > Merge`
3. Selecione a branch a ser mesclada

**Terminal:**
```bash
git checkout master
git merge feature/minha-feature
```

---

## Resolucao de Conflitos

### Quando acontece?

- Duas pessoas editaram o mesmo arquivo
- Voce editou um arquivo que foi modificado no remoto

### Como resolver no Android Studio

1. Apos pull/merge com conflito, aparece janela
2. Opcoes:
   - **Accept Yours**: Manter sua versao
   - **Accept Theirs**: Manter versao remota
   - **Merge**: Abrir editor de merge

3. No editor de merge:
   - Esquerda: Sua versao
   - Centro: Resultado
   - Direita: Versao remota
   - Use setas para aceitar trechos

### Via terminal

1. Abra o arquivo com conflito
2. Procure marcadores:
   ```
   <<<<<<< HEAD
   seu codigo
   =======
   codigo remoto
   >>>>>>> origin/master
   ```
3. Edite manualmente
4. Remova os marcadores
5. `git add arquivo`
6. `git commit`

---

## Desfazer Mudancas

### Descartar mudancas nao commitadas

**Um arquivo:**
```bash
git checkout -- arquivo.kt
```

**Todos os arquivos:**
```bash
git checkout -- .
```

### Desfazer ultimo commit (manter mudancas)

```bash
git reset --soft HEAD~1
```

### Desfazer ultimo commit (descartar mudancas)

```bash
git reset --hard HEAD~1
```

### Reverter commit ja publicado

```bash
git revert <hash-do-commit>
```

---

## Stash (Guardar mudancas temporariamente)

### Guardar

```bash
git stash
```

### Listar stashes

```bash
git stash list
```

### Recuperar

```bash
git stash pop        # Aplica e remove da lista
git stash apply      # Aplica e mantem na lista
```

### Descartar

```bash
git stash drop
```

---

## Visualizar Historico

### Log simples

```bash
git log --oneline -10
```

### Log com grafico

```bash
git log --oneline --graph --all
```

### Ver mudancas de um commit

```bash
git show <hash>
```

### Ver diff

```bash
git diff                 # Mudancas nao staged
git diff --staged        # Mudancas staged
git diff HEAD~1          # Comparar com commit anterior
```

---

## .gitignore

### Arquivos ignorados neste projeto

```
# Build
/build/
app/build/

# IDE
.idea/
*.iml

# Gradle
.gradle/
local.properties

# OS
.DS_Store
Thumbs.db

# Secrets
*.jks
*.keystore
google-services.json
```

### Adicionar arquivo ao ignore

1. Edite `.gitignore`
2. Adicione o padrao
3. Commit a mudanca

### Remover arquivo ja rastreado

```bash
git rm --cached arquivo
```

---

## Problemas Comuns

### "Your local changes would be overwritten"

**Solucao 1:** Commit suas mudancas primeiro
```bash
git add .
git commit -m "wip: salvando mudancas"
git pull
```

**Solucao 2:** Stash
```bash
git stash
git pull
git stash pop
```

### "Merge conflict"

Veja secao [Resolucao de Conflitos](#resolucao-de-conflitos)

### "Detached HEAD"

Voce esta em um commit especifico, nao em uma branch.

```bash
git checkout master
```

### Push rejeitado

O remoto tem commits que voce nao tem.

```bash
git pull --rebase
git push
```

---

## Comandos Uteis

```bash
# Status atual
git status

# Ver remotos configurados
git remote -v

# Ver todas as branches (local e remoto)
git branch -a

# Limpar branches locais mescladas
git branch --merged | grep -v master | xargs git branch -d

# Ver quem modificou cada linha
git blame arquivo.kt

# Buscar commit por mensagem
git log --grep="palavra"

# Buscar commit por codigo
git log -S "codigo"
```

---

*Documento atualizado em: Janeiro 2026*
