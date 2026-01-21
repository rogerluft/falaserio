# Build e Deploy - FalaSério

Guia para compilar, testar e publicar o aplicativo.

---

## Indice

1. [Build Debug](#1-build-debug)
2. [Build Release](#2-build-release)
3. [Instalacao via USB](#3-instalacao-via-usb)
4. [Testes](#4-testes)
5. [Publicacao na Play Store](#5-publicacao-na-play-store)

---

## 1. Build Debug

### Via Android Studio

1. `Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. Aguarde a compilacao
3. Clique em "locate" no popup para encontrar o APK

### Via Terminal

```bash
# Build simples
./gradlew assembleDebug

# Build limpo (resolve problemas de cache)
./gradlew clean assembleDebug
```

### Localizacao do APK

```
app/build/outputs/apk/debug/app-debug.apk
```

### Versao automatica

O projeto usa versionamento automatico:
- `versionCode` = numero de commits git
- `versionName` = "0.1.5-alpha+{git-hash}"

Apos cada commit, o proximo build tera versao incrementada.

---

## 2. Build Release

### Pré-requisitos

1. **Keystore configurado**
   - Arquivo `.jks` ou `.keystore`
   - NAO comitar no repositorio!

2. **Configurar signing**

   Em `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("caminho/para/keystore.jks")
               storePassword = "senha"
               keyAlias = "alias"
               keyPassword = "senha"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

   **Recomendado:** Use `local.properties` para senhas:
   ```properties
   KEYSTORE_PASSWORD=senha
   KEY_PASSWORD=senha
   ```

### Build Release

```bash
./gradlew assembleRelease
```

### Localizacao

```
app/build/outputs/apk/release/app-release.apk
```

### Build Bundle (AAB) para Play Store

```bash
./gradlew bundleRelease
```

Localizacao:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 3. Instalacao via USB

### Preparar dispositivo

1. **Ativar modo desenvolvedor**
   - `Configuracoes > Sobre o telefone`
   - Toque 7x em "Numero da versao"

2. **Ativar depuracao USB**
   - `Configuracoes > Opcoes do desenvolvedor`
   - Ative "Depuracao USB"

3. **Conectar cabo USB**
   - Use cabo de dados (nao apenas carregamento)
   - Aceite a autorizacao no celular

### Via Android Studio

1. Selecione o dispositivo no dropdown (ao lado do botao Run)
2. Clique em **Run** (triangulo verde) ou `Shift+F10`

### Via Terminal

```bash
# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Instalar substituindo versao anterior
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Build e instalar em um comando
./gradlew installDebug
```

### Verificar dispositivo conectado

```bash
adb devices
```

Deve mostrar algo como:
```
List of devices attached
ABC123DEF456    device
```

### Problemas comuns

**Dispositivo nao aparece:**
- Verifique cabo USB
- Reinstale drivers ADB
- Tente outra porta USB

**Unauthorized:**
- Desconecte e reconecte
- Aceite autorizacao no celular
- Revogue autorizacoes e tente novamente

---

## 4. Testes

### Testes Unitarios

```bash
# Todos os testes
./gradlew test

# Apenas testes debug
./gradlew testDebugUnitTest

# Classe especifica
./gradlew testDebugUnitTest --tests "br.com.webstorage.falaserio.MeuTeste"
```

### Testes Instrumentados

Requer dispositivo/emulador conectado:

```bash
./gradlew connectedDebugAndroidTest
```

### Cobertura de testes

```bash
./gradlew testDebugUnitTestCoverage
```

Relatorio em: `app/build/reports/coverage/`

### Lint

```bash
./gradlew lint
```

Relatorio em: `app/build/reports/lint-results.html`

---

## 5. Publicacao na Play Store

### Checklist pre-publicacao

- [ ] Versao release assinada
- [ ] Testes passando
- [ ] Lint sem erros criticos
- [ ] Screenshots atualizados
- [ ] Descricao do app
- [ ] Politica de privacidade
- [ ] Classificacao de conteudo

### Gerar AAB

```bash
./gradlew bundleRelease
```

### Upload via Play Console

1. Acesse [Play Console](https://play.google.com/console)
2. Selecione o app
3. `Producao > Criar nova versao`
4. Upload do arquivo `.aab`
5. Preencha notas da versao
6. Revisar e publicar

### Tracks de teste

| Track | Uso |
|-------|-----|
| Internal testing | Equipe interna (ate 100 testadores) |
| Closed testing | Beta fechado (lista de emails) |
| Open testing | Beta aberto (qualquer um pode entrar) |
| Production | Versao publica |

### Staged Rollout

Para producao, recomenda-se rollout gradual:
- 5% -> 10% -> 25% -> 50% -> 100%

Permite detectar problemas antes de afetar todos os usuarios.

---

## Comandos Rapidos

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Bundle release
./gradlew bundleRelease

# Instalar debug no dispositivo
./gradlew installDebug

# Limpar build
./gradlew clean

# Testes unitarios
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Todas as verificacoes
./gradlew check
```

---

## Estrutura de Build

```
app/build/
├── outputs/
│   ├── apk/
│   │   ├── debug/
│   │   │   └── app-debug.apk
│   │   └── release/
│   │       └── app-release.apk
│   └── bundle/
│       └── release/
│           └── app-release.aab
├── reports/
│   ├── lint-results.html
│   └── tests/
└── intermediates/
    └── [arquivos temporarios]
```

---

*Documento atualizado em: Janeiro 2026*
