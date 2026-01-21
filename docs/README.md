# Documentacao - FalaSério

Bem-vindo a documentacao do projeto FalaSério.

## Documentos Disponiveis

| Documento | Descricao |
|-----------|-----------|
| [ANDROID_STUDIO_GUIDE.md](ANDROID_STUDIO_GUIDE.md) | Guia completo do Android Studio: Compose Previews, Git, Gradle Sync, Build, Logcat, Atalhos |
| [GIT_WORKFLOW.md](GIT_WORKFLOW.md) | Fluxo de trabalho Git: commit, push, pull, branches, conflitos, stash |
| [BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md) | Compilacao, testes, instalacao USB, publicacao Play Store |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Arquitetura do projeto, estrutura de pastas, fluxo de dados |

## Acesso Rapido

### Desenvolvimento Visual (Compose Previews)

1. Abra `HomeScreen.kt` ou `CreditsScreen.kt`
2. Clique em **Split** no canto superior direito
3. Edite o codigo e veja mudancas em tempo real

### Comandos Git Essenciais

```bash
git pull                    # Baixar atualizacoes
git add . && git commit -m "msg"  # Salvar mudancas
git push                    # Enviar para GitHub
```

### Build Rapido

```bash
./gradlew assembleDebug     # Build debug
./gradlew installDebug      # Instalar no dispositivo
./gradlew clean             # Limpar cache
```

## Links Uteis

- [CLAUDE.md](../CLAUDE.md) - Instrucoes para Claude Code
- [CHANGELOG.md](../CHANGELOG.md) - Historico de versoes
- [Play Console](https://play.google.com/console) - Publicacao

---

*Documentacao mantida pela equipe FalaSério*
