# Instrucoes para Agentes e Desenvolvedores - FalaSério

Este documento contem especificacoes tecnicas detalhadas para agentes de IA e desenvolvedores que trabalharao no projeto.

---

## Indice

1. [Visao Geral do Projeto](#1-visao-geral-do-projeto)
2. [Stack Tecnologico](#2-stack-tecnologico)
3. [Arquitetura](#3-arquitetura)
4. [Padroes de Codigo](#4-padroes-de-codigo)
5. [Fluxos Principais](#5-fluxos-principais)
6. [Comandos de Build](#6-comandos-de-build)
7. [Estrutura de Arquivos](#7-estrutura-de-arquivos)
8. [APIs e Integrações](#8-apis-e-integracoes)
9. [Testes](#9-testes)
10. [Regras e Restricoes](#10-regras-e-restricoes)

---

## 1. Visao Geral do Projeto

### Descricao

**FalaSério** e um aplicativo Android de entretenimento que simula analise de estresse vocal (Voice Stress Analysis - VSA). O usuario grava sua voz e recebe uma "pontuacao de estresse" baseada em metricas de audio.

### Proposito

- **Entretenimento**: NAO tem validade cientifica
- **Monetizacao**: Sistema de creditos para analises
- **Publico**: Brasil (idioma portugues)

### Funcionalidades Core

1. Gravacao de audio (WAV 44.1kHz, 16-bit, mono)
2. Analise de voz com 5 metricas DSP
3. Historico de analises (Room database)
4. Sistema de creditos com Google Play Billing
5. Anuncios recompensados (AdMob) - futuro

---

## 2. Stack Tecnologico

### Versoes Obrigatorias

```toml
# gradle/libs.versions.toml
kotlin = "2.1.0"
compose-bom = "2025.01.00"
hilt = "2.57.2"
room = "2.6.1"
ksp = "2.1.0-1.0.29"
agp = "8.7.0"
billing = "7.1.1"
```

### SDK Android

```kotlin
minSdk = 24        // Android 7.0 Nougat
targetSdk = 35     // Android 15
compileSdk = 35
```

### Dependencias Principais

| Categoria | Biblioteca | Versao |
|-----------|-----------|--------|
| UI | Jetpack Compose BOM | 2025.01.00 |
| DI | Hilt | 2.57.2 |
| DB | Room | 2.6.1 |
| Async | Kotlin Coroutines | 1.8.1 |
| Billing | Google Play Billing | 7.1.1 |
| Permissoes | Accompanist Permissions | 0.34.0 |
| Navegacao | Navigation Compose | 2.8.5 |

---

## 3. Arquitetura

### Clean Architecture (3 camadas)

```
┌─────────────────────────────────────────┐
│           PRESENTATION                   │
│  Screens (Compose) + ViewModels (MVVM)  │
├─────────────────────────────────────────┤
│              DOMAIN                      │
│  UseCases + Models + Business Logic     │
├─────────────────────────────────────────┤
│               DATA                       │
│  Repositories + Room + DataSources      │
└─────────────────────────────────────────┘
```

### Injecao de Dependencia

- Framework: **Hilt**
- Modulos em: `di/`
- Anotacoes: `@HiltViewModel`, `@Inject`, `@Module`, `@Provides`

### Estado da UI

- Pattern: **StateFlow** + **collectAsStateWithLifecycle**
- ViewModels expoe `StateFlow<T>`
- Screens coletam como `val state by viewModel.state.collectAsStateWithLifecycle()`

---

## 4. Padroes de Codigo

### Nomenclatura

```kotlin
// Classes
class MainViewModel          // PascalCase
class AudioRecorder          // Substantivo

// Funcoes
fun startRecording()         // camelCase, verbo
suspend fun analyzeAudio()   // suspend para coroutines

// Variaveis
val isRecording: Boolean     // camelCase
private val _state           // underscore para MutableStateFlow privado

// Constantes
const val SAMPLE_RATE = 44100  // SCREAMING_SNAKE_CASE

// Packages
br.com.webstorage.falaserio.presentation.viewmodel  // lowercase
```

### Estrutura de ViewModel

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    // Estado privado mutavel
    private val _state = MutableStateFlow(ExampleState())

    // Estado publico imutavel
    val state: StateFlow<ExampleState> = _state.asStateFlow()

    // Acoes
    fun onAction(action: ExampleAction) {
        viewModelScope.launch {
            // processar acao
        }
    }
}
```

### Estrutura de Screen (Compose)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExampleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { /* TopAppBar */ }
    ) { padding ->
        // Conteudo
    }
}

// Preview separado
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExampleScreenPreview() {
    FalaSerioTheme {
        ExampleScreenContent(/* parametros mockados */)
    }
}

// Componente stateless para preview
@Composable
private fun ExampleScreenContent(/* parametros */) {
    // UI sem ViewModel
}
```

### Estrutura de Repository

```kotlin
class ExampleRepository @Inject constructor(
    private val dao: ExampleDao
) {
    fun getAll(): Flow<List<Example>> = dao.getAll()

    suspend fun insert(item: Example) = dao.insert(item)

    suspend fun delete(id: Long) = dao.delete(id)
}
```

---

## 5. Fluxos Principais

### 5.1 Gravacao e Analise

```
HomeScreen
    │
    ├── [Usuario clica gravar]
    │       │
    │       ▼
    │   MainViewModel.startRecording()
    │       │
    │       ▼
    │   AudioRecorder.start()
    │       │
    │       ├── [Grava audio em tempo real]
    │       │
    │       ▼
    │   [Usuario clica parar]
    │       │
    │       ▼
    │   MainViewModel.stopRecording()
    │       │
    │       ▼
    │   AnalyzeAudioUseCase.invoke(wavFile)
    │       │
    │       ▼
    │   VsaAnalyzer.analyze(wavFile)
    │       │
    │       ├── Extrai frames (4096 samples, 50% overlap)
    │       ├── Aplica Hamming window
    │       ├── Calcula FFT
    │       ├── Extrai 5 metricas
    │       │
    │       ▼
    │   VsaMetrics (resultado)
    │       │
    │       ▼
    │   HistoryRepository.save(metrics)
    │       │
    │       ▼
    │   CreditsRepository.decrement()
    │
    └── [Exibe resultado na UI]
```

### 5.2 Compra de Creditos

```
CreditsScreen
    │
    ├── [Lista produtos do Google Play]
    │       │
    │       ▼
    │   BillingManager.queryProducts()
    │       │
    │       ▼
    │   [Usuario clica em produto]
    │       │
    │       ▼
    │   CreditsViewModel.purchaseProduct(activity, product)
    │       │
    │       ▼
    │   BillingManager.launchPurchaseFlow()
    │       │
    │       ▼
    │   [Google Play processa pagamento]
    │       │
    │       ▼
    │   PurchasesUpdatedListener.onPurchasesUpdated()
    │       │
    │       ▼
    │   BillingManager.handlePurchase()
    │       │
    │       ├── Verifica purchase.purchaseState
    │       ├── Concede creditos
    │       ├── acknowledgePurchase()
    │       │
    │       ▼
    │   CreditsRepository.addCredits(amount)
    │
    └── [Atualiza UI]
```

---

## 6. Comandos de Build

### Desenvolvimento

```bash
# Build debug
./gradlew assembleDebug

# Instalar no dispositivo
./gradlew installDebug

# Build limpo
./gradlew clean assembleDebug
```

### Testes

```bash
# Testes unitarios
./gradlew test

# Testes instrumentados
./gradlew connectedDebugAndroidTest

# Testes especificos
./gradlew testDebugUnitTest --tests "*.VsaAnalyzerTest"
```

### Release

```bash
# APK release
./gradlew assembleRelease

# Bundle para Play Store
./gradlew bundleRelease
```

### Qualidade

```bash
# Lint
./gradlew lint

# Todas as verificacoes
./gradlew check
```

---

## 7. Estrutura de Arquivos

```
app/src/main/
├── kotlin/br/com/webstorage/falaserio/
│   │
│   ├── FalaSerioApp.kt              # Application class (@HiltAndroidApp)
│   │
│   ├── presentation/
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt    # Tela principal (gravacao)
│   │   │   │   ├── HistoryScreen.kt # Lista de analises
│   │   │   │   └── CreditsScreen.kt # Compra de creditos
│   │   │   └── theme/
│   │   │       ├── Color.kt         # Paleta de cores
│   │   │       ├── Theme.kt         # Tema Material 3
│   │   │       └── Type.kt          # Tipografia
│   │   ├── viewmodel/
│   │   │   ├── MainViewModel.kt     # Gravacao e analise
│   │   │   ├── HistoryViewModel.kt  # Lista historico
│   │   │   └── CreditsViewModel.kt  # Compras
│   │   └── navigation/
│   │       └── NavGraph.kt          # Rotas: home, history, credits
│   │
│   ├── domain/
│   │   ├── audio/
│   │   │   ├── AudioRecorder.kt     # Gravacao WAV
│   │   │   └── VsaAnalyzer.kt       # Algoritmo DSP
│   │   ├── model/
│   │   │   └── VsaMetrics.kt        # 5 metricas + score
│   │   ├── usecase/
│   │   │   └── AnalyzeAudioUseCase.kt
│   │   └── billing/
│   │       ├── BillingManager.kt    # Google Play Billing
│   │       ├── MonetizationConfig.kt # IDs dos produtos
│   │       └── ProductInfo.kt       # Modelos
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt       # Room database
│   │   │   ├── dao/
│   │   │   │   ├── HistoryDao.kt
│   │   │   │   └── CreditsDao.kt
│   │   │   └── entity/
│   │   │       ├── HistoryEntity.kt
│   │   │       └── CreditsEntity.kt
│   │   └── repository/
│   │       ├── HistoryRepository.kt
│   │       └── CreditsRepository.kt
│   │
│   └── di/
│       ├── AudioModule.kt           # Provides AudioRecorder
│       ├── DatabaseModule.kt        # Provides Room, DAOs
│       └── VsaModule.kt             # Provides VsaAnalyzer
│
├── res/
│   ├── values/
│   │   ├── strings.xml              # Textos em portugues
│   │   └── themes.xml               # Tema XML (fallback)
│   └── ...
│
└── AndroidManifest.xml              # Permissoes e config
```

---

## 8. APIs e Integracoes

### 8.1 Google Play Billing

**Biblioteca:** `com.android.billingclient:billing-ktx:7.1.1`

**IDs dos Produtos:**
```kotlin
// Consumiveis
"pack_10_credits"      // +10 creditos
"pack_20_credits"      // +20 creditos
"perpetual_100"        // +100 creditos + sem ads
"lifetime_unlimited"   // Ilimitado

// Assinaturas
"subscriber_30"        // 30/mes + sem ads
"subscriber_50"        // 50/mes + sem ads
```

### 8.2 AdMob (Futuro)

**Biblioteca:** `com.google.android.gms:play-services-ads:23.6.0`

**Tipos de Anuncio:**
- Rewarded: +1 credito por video assistido
- Interstitial: Entre analises (usuarios free)

### 8.3 Room Database

**Versao:** 2.6.1

**Entidades:**
```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val stressScore: Float,
    val metrics: String  // JSON serializado
)

@Entity(tableName = "credits")
data class CreditsEntity(
    @PrimaryKey val id: Int = 1,
    val amount: Int,
    val isUnlimited: Boolean,
    val noAds: Boolean
)
```

---

## 9. Testes

### 9.1 Estrutura de Testes

```
app/src/test/                        # Testes unitarios
├── VsaAnalyzerTest.kt
├── CreditsRepositoryTest.kt
└── MainViewModelTest.kt

app/src/androidTest/                 # Testes instrumentados
├── DatabaseTest.kt
└── BillingFlowTest.kt
```

### 9.2 Exemplo de Teste Unitario

```kotlin
class VsaAnalyzerTest {

    private lateinit var analyzer: VsaAnalyzer

    @Before
    fun setup() {
        analyzer = VsaAnalyzer()
    }

    @Test
    fun `analyze returns valid metrics for sample audio`() {
        val wavFile = File("test_audio.wav")
        val result = analyzer.analyze(wavFile)

        assertThat(result.overallStressScore).isBetween(0f, 100f)
        assertThat(result.microTremor).isBetween(0f, 100f)
    }
}
```

### 9.3 Mocking com MockK

```kotlin
@Test
fun `startRecording updates state`() = runTest {
    val mockRecorder = mockk<AudioRecorder>()
    coEvery { mockRecorder.start() } returns Unit

    viewModel.startRecording()

    assertThat(viewModel.isRecording.value).isTrue()
}
```

---

## 10. Regras e Restricoes

### 10.1 O que NAO fazer

- **NAO** hardcode strings (use strings.xml)
- **NAO** use `!!` (use safe calls ou elvis)
- **NAO** bloqueie a Main thread
- **NAO** commite secrets (tokens, keys)
- **NAO** use callbacks aninhados (prefira coroutines)
- **NAO** crie God classes (separe responsabilidades)

### 10.2 Obrigatorios

- **SEMPRE** use Hilt para DI
- **SEMPRE** use StateFlow para estado
- **SEMPRE** trate erros com try-catch ou Result
- **SEMPRE** documente funcoes publicas complexas
- **SEMPRE** faca @Preview para componentes Compose
- **SEMPRE** verifique permissoes antes de usar recursos

### 10.3 Convencoes de Commit

```
feat: nova funcionalidade
fix: correcao de bug
docs: documentacao
style: formatacao
refactor: refatoracao
test: testes
chore: manutencao

Exemplo:
feat: add rewarded ad support for free credits

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

### 10.4 Prioridades de Desenvolvimento

1. **Funcionalidade**: Deve funcionar corretamente
2. **Crash-free**: Nunca pode crashar
3. **Performance**: Rapido e responsivo
4. **UX**: Intuitivo e bonito
5. **Codigo limpo**: Legivel e manutenivel

---

## Contexto para Agentes

### Ao receber uma tarefa:

1. **Leia** os arquivos relevantes antes de modificar
2. **Entenda** a arquitetura antes de adicionar codigo
3. **Siga** os padroes existentes no projeto
4. **Teste** as mudancas (build deve passar)
5. **Documente** mudancas significativas

### Arquivos de referencia:

- `CLAUDE.md` - Instrucoes gerais do projeto
- `docs/PROJECT_STRUCTURE.md` - Arquitetura detalhada
- `docs/MONETIZATION_GUIDE.md` - Sistema de compras

### Comandos uteis:

```bash
# Verificar se compila
./gradlew assembleDebug

# Verificar lint
./gradlew lint

# Executar testes
./gradlew test
```

---

*Documento atualizado em: Janeiro 2026*
