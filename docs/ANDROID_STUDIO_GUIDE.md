# Guia Android Studio - FalaSério

Este documento contem instrucoes detalhadas para desenvolvimento no Android Studio.

---

## Indice

1. [Compose Previews (Desenvolvimento Visual)](#1-compose-previews-desenvolvimento-visual)
2. [Git no Android Studio](#2-git-no-android-studio)
3. [Gradle Sync](#3-gradle-sync)
4. [Build e Run](#4-build-e-run)
5. [Logcat e Debug](#5-logcat-e-debug)
6. [Atalhos Uteis](#6-atalhos-uteis)

---

## 1. Compose Previews (Desenvolvimento Visual)

### O que sao Previews?

Previews permitem visualizar componentes Compose sem rodar o app no dispositivo. Voce ve as mudancas em tempo real enquanto edita o codigo.

### Arquivos com Previews configurados

| Arquivo | Previews disponiveis |
|---------|---------------------|
| `HomeScreen.kt` | Idle, Recording, Result |
| `CreditsScreen.kt` | Normal, Unlimited |

### Como usar

1. **Abrir arquivo com @Preview**
   - Navegue ate `app/src/main/kotlin/.../presentation/ui/screens/`
   - Abra `HomeScreen.kt` ou `CreditsScreen.kt`

2. **Ativar painel de Preview**
   - No canto superior direito do editor, clique em **Split** (icone com duas colunas)
   - Ou use: `View > Tool Windows > Preview`

3. **Modos de visualizacao**
   - **Code**: Apenas codigo
   - **Split**: Codigo + Preview lado a lado (recomendado)
   - **Design**: Apenas preview

4. **Interagir com Preview**
   - Clique no icone **Play** no preview para modo interativo
   - Clique em elementos para testar interacoes

5. **Atualizar Preview**
   - Salve o arquivo (Ctrl+S) - atualiza automaticamente
   - Clique em **Refresh** se nao atualizar
   - Use **Build & Refresh** para reconstruir

### Estrutura de um Preview

```kotlin
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MeuComponentePreview() {
    FalaSerioTheme {
        MeuComponenteContent(
            // parametros mockados
        )
    }
}
```

### Modificando o visual

| Para mudar | Arquivo |
|------------|---------|
| Cores primarias | `presentation/ui/theme/Color.kt` |
| Tema claro/escuro | `presentation/ui/theme/Theme.kt` |
| Fontes e tamanhos | `presentation/ui/theme/Type.kt` |
| Espacamentos | Diretamente nas Screens (padding, Spacer) |
| Textos | Nas Screens ou em `res/values/strings.xml` |

---

## 2. Git no Android Studio

### 2.1 Commit

1. **Abrir painel Git**
   - `View > Tool Windows > Git` ou `Alt+9`

2. **Ver mudancas**
   - Arquivos modificados aparecem na aba **Local Changes**
   - Clique duplo para ver o diff

3. **Fazer commit**
   - Selecione os arquivos desejados
   - Clique em **Commit** (icone de checkmark) ou `Ctrl+K`
   - Escreva a mensagem de commit
   - Clique **Commit** ou **Commit and Push**

### 2.2 Push

1. **Push simples**
   - `Git > Push` ou `Ctrl+Shift+K`
   - Revise os commits a serem enviados
   - Clique **Push**

2. **Push apos commit**
   - Na janela de commit, use **Commit and Push**

### 2.3 Pull

1. **Pull simples**
   - `Git > Pull` ou `Ctrl+T`
   - Selecione a branch remota
   - Clique **Pull**

2. **Merge vs Rebase**
   - **Merge** (recomendado): Cria commit de merge, mantem historico linear
   - **Rebase**: Reescreve historico, mais limpo mas mais complexo

### 2.4 Fetch

- Baixa atualizacoes do remoto SEM aplicar
- `Git > Fetch`
- Util para ver o que mudou antes de fazer pull

### 2.5 Branches

1. **Ver branch atual**
   - Canto inferior direito do Android Studio

2. **Criar nova branch**
   - Clique no nome da branch > **New Branch**

3. **Trocar de branch**
   - Clique no nome da branch > Selecione a branch desejada

### 2.6 Resolver conflitos

1. Apos pull/merge com conflitos, aparece janela de resolucao
2. Escolha:
   - **Accept Yours**: Manter sua versao
   - **Accept Theirs**: Manter versao remota
   - **Merge**: Resolver manualmente

---

## 3. Gradle Sync

### O que e?

Sincroniza as configuracoes do projeto (dependencias, versoes, plugins) com o Android Studio.

### Quando usar

- Apos modificar `build.gradle.kts` ou `settings.gradle.kts`
- Apos adicionar/remover dependencias
- Quando o IDE mostrar erros de importacao
- Apos clonar o projeto

### Como sincronizar

1. **Automatico**
   - O Android Studio sugere "Sync Now" quando detecta mudancas

2. **Manual**
   - `File > Sync Project with Gradle Files`
   - Ou clique no icone do elefante com seta (toolbar)
   - Ou `Ctrl+Shift+O`

### Diferenca: Sync Gradle vs Git Pull

| Operacao | O que faz |
|----------|-----------|
| **Gradle Sync** | Sincroniza configuracoes de build com o IDE |
| **Git Pull** | Baixa codigo do repositorio remoto |

**Importante**: Apos um Git Pull que modifica arquivos Gradle, faca Sync!

### Problemas comuns

1. **Sync falhou**
   - `File > Invalidate Caches / Restart`
   - Delete pasta `.gradle` e `.idea` e reabra o projeto

2. **Dependencia nao encontrada**
   - Verifique conexao com internet
   - Verifique se o repositorio Maven esta configurado

---

## 4. Build e Run

### Build Debug APK

1. **Via menu**
   - `Build > Build Bundle(s) / APK(s) > Build APK(s)`

2. **Via terminal**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Localizacao do APK**
   - `app/build/outputs/apk/debug/app-debug.apk`

### Run no dispositivo

1. **Conectar dispositivo USB**
   - Ative "Depuracao USB" no celular
   - Conecte via cabo USB
   - Aceite a autorizacao no celular

2. **Selecionar dispositivo**
   - No dropdown ao lado do botao Run, selecione seu dispositivo

3. **Executar**
   - Clique em **Run** (triangulo verde) ou `Shift+F10`

### Clean Build

Quando houver problemas de cache ou classes duplicadas:

```bash
./gradlew clean assembleDebug
```

Ou via menu: `Build > Clean Project` seguido de `Build > Rebuild Project`

---

## 5. Logcat e Debug

### Abrir Logcat

- `View > Tool Windows > Logcat` ou `Alt+6`

### Filtrar logs do app

No campo de filtro, use:
```
package:br.com.webstorage.falaserio
```

Para debug especificamente:
```
package:br.com.webstorage.falaserio.debug
```

### Niveis de log

| Nivel | Uso |
|-------|-----|
| **Verbose** | Tudo (muito ruido) |
| **Debug** | Informacoes de debug |
| **Info** | Informacoes gerais |
| **Warn** | Avisos |
| **Error** | Erros |

### Adicionar logs no codigo

```kotlin
import android.util.Log

Log.d("MinhaTag", "Mensagem de debug")
Log.e("MinhaTag", "Mensagem de erro", exception)
```

### Breakpoints e Debug

1. Clique na margem esquerda do editor para adicionar breakpoint
2. Execute com **Debug** (icone de inseto) em vez de Run
3. Use a janela Debug para inspecionar variaveis

---

## 6. Atalhos Uteis

### Navegacao

| Atalho | Acao |
|--------|------|
| `Ctrl+Shift+N` | Buscar arquivo por nome |
| `Ctrl+N` | Buscar classe por nome |
| `Ctrl+Shift+F` | Buscar texto em todo projeto |
| `Ctrl+B` | Ir para definicao |
| `Ctrl+Alt+B` | Ir para implementacao |
| `Alt+F7` | Encontrar usos |
| `Ctrl+E` | Arquivos recentes |

### Edicao

| Atalho | Acao |
|--------|------|
| `Ctrl+Space` | Autocomplete |
| `Ctrl+Shift+Enter` | Completar statement |
| `Alt+Enter` | Quick fix / sugestoes |
| `Ctrl+Alt+L` | Formatar codigo |
| `Ctrl+Alt+O` | Organizar imports |
| `Ctrl+D` | Duplicar linha |
| `Ctrl+Y` | Deletar linha |
| `Ctrl+/` | Comentar linha |
| `Ctrl+Shift+/` | Comentar bloco |

### Refatoracao

| Atalho | Acao |
|--------|------|
| `Shift+F6` | Renomear |
| `Ctrl+Alt+M` | Extrair metodo |
| `Ctrl+Alt+V` | Extrair variavel |
| `Ctrl+Alt+C` | Extrair constante |

### Build e Run

| Atalho | Acao |
|--------|------|
| `Shift+F10` | Run |
| `Shift+F9` | Debug |
| `Ctrl+F9` | Build |
| `Ctrl+Shift+F10` | Run configuracao atual |

### Git

| Atalho | Acao |
|--------|------|
| `Ctrl+K` | Commit |
| `Ctrl+Shift+K` | Push |
| `Ctrl+T` | Pull/Update |
| `Alt+9` | Abrir painel Git |

---

## Dicas Extras

### Versionamento automatico

Este projeto usa versionamento automatico baseado em Git:
- `versionCode` = numero de commits
- `versionName` = "0.1.5-alpha+{hash}"

Apos cada commit, a versao e incrementada automaticamente no proximo build.

### Verificar versao no app

A versao aparece no rodape da tela de Creditos:
```
v0.1.5-alpha+abc1234 (42) - debug
```

### Live Templates

Digite abreviações e pressione Tab:
- `logd` -> `Log.d(TAG, "")`
- `todo` -> `// TODO: `
- `comp` -> estrutura de Composable

---

*Documento atualizado em: Janeiro 2026*
