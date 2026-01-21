# Documentacao - FalaSério

Bem-vindo a documentacao completa do projeto FalaSério.

---

## Documentos Disponiveis

### Desenvolvimento

| Documento | Descricao |
|-----------|-----------|
| [ANDROID_STUDIO_GUIDE.md](ANDROID_STUDIO_GUIDE.md) | Compose Previews, Git no AS, Gradle Sync, Build, Logcat, Atalhos |
| [GIT_WORKFLOW.md](GIT_WORKFLOW.md) | Commit, Push, Pull, Branches, Conflitos, Stash |
| [BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md) | Build, Instalacao USB, Testes, Play Store |

### Arquitetura e Projeto

| Documento | Descricao |
|-----------|-----------|
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Arquitetura Clean, Estrutura de pastas, Fluxo de dados |
| [AGENT_INSTRUCTIONS.md](AGENT_INSTRUCTIONS.md) | Instrucoes detalhadas para agentes IA e desenvolvedores |

### Configuracao e Infraestrutura

| Documento | Descricao |
|-----------|-----------|
| [GITHUB_SETTINGS.md](GITHUB_SETTINGS.md) | Repo privado, Tokens, Acesso Claude Code e Android Studio |
| [MONETIZATION_GUIDE.md](MONETIZATION_GUIDE.md) | Google Play Console, Produtos, Assinaturas, Gestao |

### Planejamento

| Documento | Descricao |
|-----------|-----------|
| [IMPROVEMENTS_AND_SUGGESTIONS.md](IMPROVEMENTS_AND_SUGGESTIONS.md) | Problemas, Melhorias, Debitos tecnicos, Roadmap |

---

## Acesso Rapido

### Desenvolvimento Visual (Compose Previews)

1. Abra `HomeScreen.kt` ou `CreditsScreen.kt`
2. Clique em **Split** no canto superior direito
3. Edite o codigo e veja mudancas em tempo real

### Comandos Git Essenciais

```bash
git pull                         # Baixar atualizacoes
git add . && git commit -m "msg" # Salvar mudancas
git push                         # Enviar para GitHub
```

### Build Rapido

```bash
./gradlew assembleDebug     # Build debug
./gradlew installDebug      # Instalar no dispositivo
./gradlew clean             # Limpar cache
```

### Criar Produto no Google Play

1. Play Console > Monetizar > Produtos no app
2. Criar produto com ID do `MonetizationConfig.kt`
3. Ativar produto
4. Aguardar 24h para propagacao

---

## Para Novos Desenvolvedores

### Leitura obrigatoria:

1. [AGENT_INSTRUCTIONS.md](AGENT_INSTRUCTIONS.md) - Entenda o projeto
2. [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Arquitetura
3. [ANDROID_STUDIO_GUIDE.md](ANDROID_STUDIO_GUIDE.md) - Ferramentas

### Primeiro build:

```bash
git clone https://github.com/rogerluft/falaserio.git
cd falaserio
./gradlew assembleDebug
```

---

## Para Agentes de IA

### Contexto rapido:

- **Projeto:** App Android de entretenimento (analise de voz)
- **Arquitetura:** Clean Architecture + MVVM
- **Stack:** Kotlin 2.1, Compose 2025, Hilt, Room
- **Monetizacao:** Google Play Billing (creditos)

### Arquivos de referencia:

- `CLAUDE.md` (raiz) - Instrucoes gerais
- `docs/AGENT_INSTRUCTIONS.md` - Especificacoes detalhadas

### Antes de modificar:

1. Leia o arquivo que vai modificar
2. Entenda o padrao existente
3. Siga as convencoes do projeto
4. Teste com `./gradlew assembleDebug`

---

## Links Uteis

### Internos

- [CLAUDE.md](../CLAUDE.md) - Instrucoes para Claude Code
- [CHANGELOG.md](../CHANGELOG.md) - Historico de versoes

### Externos

- [Play Console](https://play.google.com/console) - Publicacao
- [Firebase Console](https://console.firebase.google.com) - Analytics
- [AdMob](https://admob.google.com) - Anuncios

---

## Checklist de Lancamento

- [ ] Produtos criados no Google Play Console
- [ ] Precos configurados
- [ ] Politica de privacidade publicada
- [ ] Screenshots atualizados
- [ ] Testes internos aprovados
- [ ] Build release assinado
- [ ] Revisao de seguranca
- [ ] Versao submetida para aprovacao

---

*Documentacao mantida pela equipe FalaSério*
*Ultima atualizacao: Janeiro 2026*
