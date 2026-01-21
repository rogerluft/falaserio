# Estrutura do Projeto - FalaSério

Visao geral da arquitetura e organizacao do codigo.

---

## Arquitetura Clean Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   Screens   │  │  ViewModels │  │  Navigation │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
├─────────────────────────────────────────────────────────┤
│                      DOMAIN                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   UseCases  │  │   Models    │  │   Billing   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
├─────────────────────────────────────────────────────────┤
│                       DATA                               │
│  ┌─────────────┐  ┌─────────────┐                       │
│  │    Room     │  │ Repository  │                       │
│  └─────────────┘  └─────────────┘                       │
└─────────────────────────────────────────────────────────┘
```

---

## Estrutura de Pastas

```
app/src/main/kotlin/br/com/webstorage/falaserio/
│
├── presentation/           # Camada de UI
│   ├── ui/
│   │   ├── screens/       # Telas do app
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HistoryScreen.kt
│   │   │   └── CreditsScreen.kt
│   │   └── theme/         # Tema Material 3
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── viewmodel/         # ViewModels (MVVM)
│   │   ├── MainViewModel.kt
│   │   ├── HistoryViewModel.kt
│   │   └── CreditsViewModel.kt
│   └── navigation/        # Navegacao Compose
│       └── NavGraph.kt
│
├── domain/                 # Regras de negocio
│   ├── audio/             # Processamento de audio
│   │   ├── AudioRecorder.kt
│   │   └── VsaAnalyzer.kt
│   ├── model/             # Modelos de dominio
│   │   └── VsaMetrics.kt
│   ├── usecase/           # Casos de uso
│   │   └── AnalyzeAudioUseCase.kt
│   └── billing/           # Monetizacao
│       ├── BillingManager.kt
│       └── ProductInfo.kt
│
├── data/                   # Camada de dados
│   ├── local/             # Banco de dados Room
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── HistoryDao.kt
│   │   │   └── CreditsDao.kt
│   │   └── entity/
│   │       ├── HistoryEntity.kt
│   │       └── CreditsEntity.kt
│   └── repository/        # Repositorios
│       ├── HistoryRepository.kt
│       └── CreditsRepository.kt
│
├── di/                     # Injecao de dependencia (Hilt)
│   ├── AudioModule.kt
│   ├── DatabaseModule.kt
│   └── VsaModule.kt
│
└── FalaSerioApp.kt        # Application class
```

---

## Componentes Principais

### Presentation Layer

| Componente | Responsabilidade |
|------------|-----------------|
| `HomeScreen` | Tela principal com botao de gravacao |
| `HistoryScreen` | Lista de analises anteriores |
| `CreditsScreen` | Compra de creditos e saldo |
| `MainViewModel` | Estado da gravacao e analise |
| `NavGraph` | Rotas: home, history, credits |

### Domain Layer

| Componente | Responsabilidade |
|------------|-----------------|
| `AudioRecorder` | Gravacao de audio WAV |
| `VsaAnalyzer` | Algoritmo de analise de stress |
| `VsaMetrics` | Modelo com 5 metricas |
| `AnalyzeAudioUseCase` | Orquestra gravacao + analise |
| `BillingManager` | Integra Google Play Billing |

### Data Layer

| Componente | Responsabilidade |
|------------|-----------------|
| `AppDatabase` | Banco Room |
| `HistoryDao` | CRUD do historico |
| `CreditsDao` | CRUD dos creditos |
| `HistoryRepository` | Abstrai acesso ao historico |
| `CreditsRepository` | Abstrai acesso aos creditos |

---

## Fluxo de Dados

### Gravacao e Analise

```
┌──────────┐    ┌─────────────┐    ┌─────────────┐
│HomeScreen│───>│MainViewModel│───>│AnalyzeAudio │
└──────────┘    └─────────────┘    │   UseCase   │
                                   └──────┬──────┘
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    │                     │                     │
              ┌─────▼─────┐        ┌─────▼─────┐        ┌─────▼─────┐
              │AudioRecord│        │VsaAnalyzer│        │HistoryRepo│
              └───────────┘        └───────────┘        └───────────┘
                    │                     │                     │
                    │              ┌──────▼──────┐              │
                    │              │  VsaMetrics │              │
                    │              └─────────────┘              │
                    │                                           │
              ┌─────▼─────┐                              ┌──────▼──────┐
              │  WAV File │                              │    Room     │
              └───────────┘                              └─────────────┘
```

### Compra de Creditos

```
┌────────────┐    ┌───────────────┐    ┌────────────────┐
│CreditsScr  │───>│CreditsViewMdl│───>│ BillingManager │
└────────────┘    └───────────────┘    └───────┬────────┘
                                               │
                                        ┌──────▼──────┐
                                        │ Google Play │
                                        │   Billing   │
                                        └──────┬──────┘
                                               │
                                        ┌──────▼──────┐
                                        │CreditsRepo  │
                                        └──────┬──────┘
                                               │
                                        ┌──────▼──────┐
                                        │    Room     │
                                        └─────────────┘
```

---

## Configuracoes Importantes

### Versoes (gradle/libs.versions.toml)

```toml
kotlin = "2.1.0"
compose-bom = "2025.01.00"
hilt = "2.57.2"
room = "2.6.1"
ksp = "2.1.0-1.0.29"
agp = "8.7.0"
```

### SDK (app/build.gradle.kts)

```kotlin
minSdk = 24        // Android 7.0
targetSdk = 35     // Android 15
compileSdk = 35
```

### Permissoes (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="com.android.vending.BILLING"/>
```

---

## Algoritmo VSA

### Pipeline de Processamento

```
Audio WAV (44.1kHz, 16-bit, mono)
         │
         ▼
┌─────────────────┐
│ Extract Frames  │  4096 samples, 50% overlap
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Hamming Window  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Calculate FFT   │
└────────┬────────┘
         │
    ┌────┴────┬────────┬────────┬────────┐
    │         │        │        │        │
    ▼         ▼        ▼        ▼        ▼
┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐
│Tremor │ │ Pitch │ │Jitter │ │Shimmer│ │  HNR  │
│  30%  │ │  20%  │ │  20%  │ │  15%  │ │  15%  │
└───┬───┘ └───┬───┘ └───┬───┘ └───┬───┘ └───┬───┘
    │         │        │        │        │
    └────┬────┴────────┴────────┴────────┘
         │
         ▼
┌─────────────────┐
│  Stress Score   │  0-100%
└─────────────────┘
```

### Metricas

| Metrica | Peso | Descricao |
|---------|------|-----------|
| Micro-Tremor | 30% | Vibracoes 8-12Hz na voz |
| Pitch Variation | 20% | Variacao de frequencia fundamental |
| Jitter | 20% | Irregularidade ciclo-a-ciclo |
| Shimmer | 15% | Variacao de amplitude |
| HNR | 15% | Razao harmonico/ruido |

---

## Produtos de Monetizacao

| ID | Tipo | Creditos | Descricao |
|----|------|----------|-----------|
| `pack_10_credits` | INAPP | +10 | Pacote basico |
| `pack_20_credits` | INAPP | +20 | Popular |
| `subscriber_30` | SUBS | 30/mes | Assinatura |
| `subscriber_50` | SUBS | 50/mes | Assinatura+ |
| `lifetime_unlimited` | INAPP | Infinito | Vitalicio |
| `perpetual_100` | INAPP | +100 | Mega pacote |

---

*Documento atualizado em: Janeiro 2026*
