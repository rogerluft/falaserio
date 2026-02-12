# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---


## [1.0.0-rc1] - 2026-02-12

### Play Store Release Candidate

#### Segurança e Assinatura
- APK release assinado e validado com `apksigner` (Signature Scheme v2)
- Guia atualizado para uso do `apksigner` (KEYSTORE_GUIDE.md)
- Scripts de ambiente para keystore (setup-keystore-env.sh/ps1)

#### Build
- Build release 100% Play Store ready (ProGuard/R8, lint, assinatura, versionamento)
- APK testado e validado em dispositivo real

#### Documentação
- CHECKLIST_LANCAMENTO.md atualizado com status e próximos passos
- Guia de keystore revisado

---
## [0.1.5-alpha] - 2026-01-21

### Compose Previews e Auto-Versioning

Adicionado suporte a previews do Jetpack Compose e versionamento automatico baseado em Git.

### Adicionado

- **Auto-Versioning via Git** - Versionamento automatico no build.gradle.kts:
  - `versionCode` = numero total de commits (`git rev-list --count HEAD`)
  - `versionName` = versao + hash do commit (ex: `0.1.5-alpha+abc1234`)
  - Elimina necessidade de incrementar versao manualmente
  - Cada build tem identificador unico rastreavel

- **HomeScreen Previews** - 3 estados visuais:
  - Idle (pronto para gravar)
  - Recording (gravando com amplitude)
  - Result (mostrando resultado de stress)

- **CreditsScreen Previews** - 2 estados visuais:
  - Normal (com creditos limitados)
  - Unlimited (acesso ilimitado)

- **Build Version Indicator** - Indicador de versao no rodape da CreditsScreen
  - Exibe: versionName, versionCode e buildType
  - Permite verificar se o build corresponde ao codigo

### Tecnico

- Funcoes `getGitCommitCount()` e `getGitHash()` em build.gradle.kts
- Criados composables stateless `*Content` para suporte a preview
- Imports de `@Preview` e `FalaSerioTheme` adicionados

---

## [0.1.4-alpha] - 2026-01-21

### Modulo de Gestao de Monetizacao Aprimorado

Melhorias significativas no modulo de gestao de monetizacao para desenvolvedores.

### Adicionado

#### Configuracao Centralizada de Anuncios (AdsConfig.kt)

- **AdsConfig.kt** - Novo arquivo de configuracao centralizada de ads
    - Documentacao completa com links para docs oficiais do AdMob
    - IDs de teste do Google pre-configurados
    - Placeholders para IDs de producao
    - `IS_TEST_MODE` toggle para alternar entre teste/producao
    - Configuracoes de comportamento:
        - Intervalo entre intersticiais (120s default)
        - Acoes antes de mostrar ad (3 default)
        - Creditos por rewarded ad (1 default)
        - Limite diario de rewarded ads (5 default)
        - Toggles de banner por tela (Home, History, Credits)
        - Delay inicial antes do primeiro ad (30s)
    - Configuracoes de privacidade (GDPR/LGPD)
    - Validacao automatica de configuracao
    - Funcao `getStatusSummary()` para debug

#### Tela de Gestao Aprimorada (MonetizationManagementScreen.kt)

- **Nova arquitetura com Tabs**
    - Tab "Produtos" - gestao de produtos (existente)
    - Tab "Anuncios" - nova gestao de ads
    - Badge de alerta quando ha erros de configuracao

- **Tab de Anuncios** inclui:
    - Status card com modo (Teste/Producao) e consentimento
    - Card de links uteis com documentacao oficial do AdMob
    - Instrucoes passo-a-passo para configurar producao
    - Visualizacao dos IDs configurados
    - Visualizacao do comportamento dos ads
    - Validacao automatica com exibicao de erros

#### Documentacao

- **MONETIZATION_MODULE_ANALYSIS.md** - Analise completa do modulo
    - Arquitetura atual documentada
    - Funcionalidades cobertas vs limitacoes
    - Produtos configurados
    - Melhorias planejadas (roadmap)
    - Referencias e links uteis

### Arquivos Novos

```
app/src/main/kotlin/br/com/webstorage/falaserio/domain/ads/
    AdsConfig.kt                    # Configuracao centralizada de ads

MONETIZATION_MODULE_ANALYSIS.md     # Documentacao do modulo
```

### Arquivos Modificados

```
app/src/main/kotlin/br/com/webstorage/falaserio/presentation/ui/screens/
    MonetizationManagementScreen.kt  # Adicionada tab de Anuncios
```

### Colaboradores

| Contribuidor | Papel |
|--------------|-------|
| Claudio (Claude AI) | Implementacao e documentacao |
| Roger Luft | Especificacao e revisao |

---

## [0.1.0-alpha] - 2025-01-08

### 🎉 Release Inicial - Arquitetura Completa

Primeira versão funcional do FalaSério com toda a arquitetura Clean Architecture + MVVM
implementada.

### Adicionado

#### 🏗️ Infraestrutura

- **Gradle Configuration**
    - Kotlin 2.1.0 com Compose Compiler Plugin (novo método!)
    - Compose BOM 2025.01.00 (versão mais recente)
    - KSP 2.1.0-1.0.29 para Room
    - Hilt 2.51 para DI
    - Room 2.6.1 para persistência
    - Billing Library 7.0.0 para monetização
    - minSdk 24, targetSdk 35

- **Hilt Modules**
    - `AudioModule.kt` - Provides AudioRecorder com @ApplicationContext
    - `DatabaseModule.kt` - Provides Room Database + DAOs
    - `VsaModule.kt` - Provides VsaAnalyzer + UseCase

#### 🎤 Camada de Áudio

- **AudioRecorder Interface** - Contrato para gravação
- **AudioRecorderImpl** - Implementação com AudioRecord
    - 44.1kHz sample rate
    - 16-bit PCM mono
    - Buffer 4096 samples
    - StateFlows para isRecording, duration, amplitude
    - Salva arquivos WAV com header correto

#### 🔬 Análise VSA (Voice Stress Analysis)

- **VsaAnalyzer.kt** - 363 linhas de DSP puro em Kotlin!
    - `readWavFile()` - Parser de WAV 16-bit PCM
    - `extractFrames()` - Windowing com Hamming
    - `fft()` - Transformada de Fourier (DFT)
    - `calculateMicroTremor()` - Detecção 8-12Hz via FFT
    - `calculatePitchVariation()` - Autocorrelation pitch detection
    - `calculateJitter()` - Variação período ciclo-a-ciclo
    - `calculateShimmer()` - Variação amplitude ciclo-a-ciclo
    - `calculateHNR()` - Harmonic-to-Noise Ratio

- **VsaMetrics.kt** - Data class com 5 métricas
    - Thresholds científicos para cada métrica
    - Propriedades booleanas `indicatesStress`
    - `getStressLevel()` retorna texto localizado
    - Score ponderado com ±5% randomness

- **AnalyzeAudioUseCase.kt** - Use case wrapper

#### 💾 Camada de Dados

- **Room Database v1**
    - `HistoryEntity` - Gravações com todas métricas
    - `CreditsEntity` - Estado de assinatura/créditos
    - `HistoryDao` - CRUD com Flow
    - `CreditsDao` - Operações atômicas

- **Repositories**
    - `HistoryRepository` - Salva análises + deleta arquivos
    - `CreditsRepository` - Lógica de créditos/assinaturas

#### 💳 Monetização

- **BillingManager.kt** - Google Play Billing 7.0.0
    - 4 produtos INAPP configurados
    - 2 assinaturas configuradas
    - Query de produtos assíncrono
    - Consumo de compras

- **ProductInfo.kt** - Data class para produtos

#### 🎨 Apresentação

- **Theme**
    - `Color.kt` - Paleta VSA (verde/vermelho/amarelo)
    - `Theme.kt` - Material 3 + Dynamic Colors
    - `Typography.kt` - Escala tipográfica completa

- **Navigation**
    - `NavGraph.kt` - 3 rotas: Home, History, Credits

- **Screens**
    - `HomeScreen.kt` - Gravação com animações
    - `HistoryScreen.kt` - Lista de análises
    - `CreditsScreen.kt` - Loja de créditos

- **ViewModels**
    - `MainViewModel.kt` - Gravação + Análise
    - `HistoryViewModel.kt` - CRUD histórico
    - `CreditsViewModel.kt` - Compras + Ads

#### 📱 App

- `FalaSerioApp.kt` - @HiltAndroidApp
- `MainActivity.kt` - @AndroidEntryPoint + Compose
- `AndroidManifest.xml` - Permissões + AdMob meta

### Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ HomeScreen  │  │HistoryScreen│  │CreditsScreen│         │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │
│         │                │                │                 │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐         │
│  │MainViewModel│  │HistoryVM   │  │CreditsVM   │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │
└─────────┼────────────────┼────────────────┼─────────────────┘
          │                │                │
┌─────────┼────────────────┼────────────────┼─────────────────┐
│         │           DOMAIN                │                 │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐         │
│  │AnalyzeUCase │  │ VsaAnalyzer │  │BillingMgr  │          │
│  └──────┬──────┘  └─────────────┘  └─────────────┘         │
│         │                                                   │
│  ┌──────▼──────┐                                           │
│  │AudioRecorder│                                           │
│  └─────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
          │
┌─────────┼───────────────────────────────────────────────────┐
│         │              DATA                                 │
│  ┌──────▼──────┐  ┌─────────────┐  ┌─────────────┐         │
│  │HistoryRepo  │  │CreditsRepo  │  │ AppDatabase │         │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │
│         │                │                │                 │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐         │
│  │ HistoryDao  │  │ CreditsDao  │  │   Room DB   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### Colaboradores

| Contribuidor        | Papel                       |
|---------------------|-----------------------------|
| Andarilho dos Véus  | Arquiteto / Product Owner   |
| Claudio (Claude AI) | Desenvolvedor Principal     |
| Roginho             | Executor / QA               |
| GeGe (Gemini AI)    | Consultora de Imports/Stack |

### Estatísticas

- **Arquivos Kotlin criados:** 24
- **Linhas de código:** ~2.500
- **Maior arquivo:** VsaAnalyzer.kt (363 linhas)
- **Módulos Hilt:** 3
- **Entidades Room:** 2
- **Telas Compose:** 3
- **ViewModels:** 3
- **Produtos Billing:** 6

---

## [0.1.1-alpha] - 2026-01-13

### 🔍 Auditoria de Código - Billing System

Análise completa do sistema de billing usando LSP Kotlin e revisão manual.

### 🐛 Bugs Identificados

#### 🔴 CRÍTICOS

| Bug | Arquivo | Linha | Descrição |
|-----|---------|-------|-----------|
| TYPO | `CreditsViewModel.kt` | 83 | `"SUBSCRIber_50"` deveria ser `"SUBSCRIBER_50"` |
| Race Condition | `BillingManager.kt` | 112-115 | Callback invocado antes de `consumeAsync` completar |
| Callback Sobrescrito | `BillingManager.kt` | 33 | `purchaseCallback` pode ser sobrescrito em compras simultâneas |

#### 🟡 MÉDIOS

| Bug | Arquivo | Descrição |
|-----|---------|-----------|
| Créditos Iniciais | `FalaSerioApp.kt` | `initializeForNewUser()` nunca é chamado - usuário novo pode ter 0 créditos |
| Restauração | `BillingManager.kt` | Falta método `restorePurchases()` para reinstalação |

### 🔧 Correções Aplicadas (v0.1.2-alpha)

- [x] Corrigir typo `SUBSCRIber_50` → `SUBSCRIBER_50`
- [x] Aguardar `consumeAsync` antes de invocar callback
- [x] Adicionar `Mutex` para prevenir compras simultâneas
- [x] Chamar `initializeForNewUser()` no `FalaSerioApp.onCreate()`
- [x] Implementar `restorePurchases()` no `BillingManager`

### 🛠️ Ferramentas Utilizadas

- **Kotlin LSP** (fwcd/kotlin-language-server v1.3.13)
- **Claude Code Ultrathink** para análise profunda
- **Operações LSP**: documentSymbol, hover, findReferences

### Auditores

| Auditor | Papel |
|---------|-------|
| Claudio (Claude AI) | Análise de código |
| Roginho | Revisão e validação |

---

## [0.1.3-alpha] - 2026-01-19

### 🔧 Hotfix - Crash no Startup (AdMob)

App crashava imediatamente ao iniciar devido a configuração inválida do AdMob.

### 🐛 Bug Corrigido

| Severidade | Arquivo | Problema | Solução |
|------------|---------|----------|---------|
| 🔴 CRÍTICO | `AndroidManifest.xml` | AdMob Application ID era placeholder (`ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`) causando crash fatal no boot | Substituído por ID de teste oficial do Google |
| 🟡 MÉDIO | `gradlew` | Arquivo corrompido com texto espúrio "mas " antes do shebang | Removido texto, restaurado `#!/bin/sh` |

### 📝 Detalhes Técnicos

**Erro no Logcat:**
```
FATAL EXCEPTION: main
java.lang.RuntimeException: Unable to get provider com.google.android.gms.ads.MobileAdsInitProvider
Caused by: java.lang.IllegalStateException: Invalid application ID
```

**Diff AndroidManifest.xml:**
```diff
-        <!-- AdMob App ID (substitua pelo seu) -->
+        <!-- AdMob App ID - ID de teste para desenvolvimento -->
         <meta-data
             android:name="com.google.android.gms.ads.APPLICATION_ID"
-            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
+            android:value="ca-app-pub-3940256099942544~3347511713" />
```

**Diff gradlew:**
```diff
-mas #!/bin/sh
+#!/bin/sh
```

### ⚠️ TODO para Produção

- [ ] Substituir AdMob App ID de teste (`ca-app-pub-3940256099942544~3347511713`) pelo ID real da conta AdMob paga
- [ ] Configurar Ad Unit IDs reais para banner/interstitial/rewarded

### 🔍 Diagnóstico

Ferramenta utilizada: `adb logcat -s AndroidRuntime:E`

### Colaboradores

| Contribuidor | Papel |
|--------------|-------|
| Claudio (Claude AI) | Diagnóstico e correção |
| Roginho | Reporte do bug |

---

## [Unreleased]

### Planejado

- [ ] Integração AdMob (Rewarded Ads)
- [ ] Testes unitários (JUnit5 + MockK)
- [ ] Testes instrumentados (Compose Testing)
- [ ] CI/CD com GitHub Actions
- [ ] Publicação Play Store (Closed Testing)
- [ ] Widget de análise rápida
- [ ] Compartilhamento de resultados
- [ ] Análise offline completa

---

## Notas de Desenvolvimento

### Por que Kotlin 2.1.0?

- Novo Compose Compiler Plugin automático
- Melhor performance de compilação
- Suporte completo a K2 compiler

### Por que KSP ao invés de KAPT?

- 2x mais rápido que KAPT
- Suporte nativo para Room 2.6+
- Melhor integração com Kotlin 2.x

### Por que Clean Architecture?

- Separação clara de responsabilidades
- Testabilidade independente por camada
- Facilidade de manutenção e evolução
- Padrão da indústria Android

### Por que DSP em Kotlin puro?

- Sem dependência de bibliotecas nativas
- Controle total sobre algoritmos
- Portabilidade garantida
- Facilidade de debug*

*A Sinergia Entre Humanos e IAs Produz Maravilhas*
