# Copilot Instructions - FalaSério

**FalaSério** is an Android entertainment app for Voice Stress Analysis (VSA) using custom DSP algorithms written in pure Kotlin.

## Architecture Overview

### Clean Architecture Layers
- **presentation/** - Jetpack Compose UI with MVVM pattern (HomeScreen, HistoryScreen, CreditsScreen)
- **domain/** - Business logic including DSP algorithms, billing, and use cases
- **data/** - Room database (local/) and repositories
- **di/** - Hilt dependency injection modules (AudioModule, DatabaseModule, BillingModule)

### Critical Components
- `VsaAnalyzer.kt` - Core DSP engine (363 lines) calculating 5 stress metrics from WAV files
- `AudioRecorderImpl.kt` - Records 44.1kHz 16-bit PCM mono WAV files
- `MainViewModel` - Orchestrates recording → analysis → credits deduction flow
- `CreditsRepository` - Manages in-app purchase credits and unlimited subscriptions

## Build & Development

```bash
# Build debug APK
./gradlew assembleDebug

# Clean build (fixes duplicate class issues common with Compose/Hilt)
./gradlew clean assembleDebug

# Install on device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run specific test class
./gradlew testDebugUnitTest --tests "br.com.webstorage.falaserio.ClassName"
```

**Version scheme:** Auto-generated from git (`versionCode = git commit count`, `versionName = 0.1.5-alpha+<git-hash>`)

## Tech Stack Versions

Defined in `gradle/libs.versions.toml`:
- Kotlin 2.1.0 with new Compose Compiler Plugin (`kotlin.plugin.compose`)
- Compose BOM 2025.01.00
- Hilt 2.57.2 (use `@HiltViewModel`, `@AndroidEntryPoint`, `@HiltAndroidApp`)
- Room 2.6.1 with KSP 2.1.0-1.0.29 (NOT kapt)
- AGP 8.7.0
- minSdk 24 / targetSdk 35

## DSP Algorithm (VsaAnalyzer.kt)

**Critical:** All audio processing happens on `Dispatchers.Default` (CPU-intensive work).

### Processing Pipeline
1. Read WAV file (skip 44-byte header, parse 16-bit PCM little-endian)
2. Extract overlapping frames (4096 samples, 50% overlap = 2048 hop size)
3. Apply Hamming window to each frame
4. Calculate 5 metrics:
   - **Micro-Tremor** (8-12Hz): FFT on amplitude envelope to detect muscle oscillations
   - **Pitch Variation**: Autocorrelation for F0 detection (80-400Hz), calculate CV (std/mean × 100)
   - **Jitter**: Period variation between consecutive pitch cycles
   - **Shimmer**: Amplitude variation between frames
   - **HNR**: Harmonic-to-Noise Ratio (higher = clearer voice)
5. Compute weighted stress score: `0.30×tremor + 0.20×pitch + 0.20×jitter + 0.15×shimmer + 0.15×hnr`
6. Add ±5% random factor for entertainment (intentional non-determinism)

**Audio Format:** WAV 44.1kHz 16-bit PCM mono only.

**Known Issue:** Random factor makes same audio produce different results. Documented in `IMPROVEMENTS_AND_SUGGESTIONS.md` but kept for entertainment value.

## Dependency Injection Patterns

All major classes use constructor injection with `@Inject`:
- ViewModels: `@HiltViewModel` + `@Inject constructor()`
- Activities: `@AndroidEntryPoint` on MainActivity
- Application: `@HiltAndroidApp` on FalaSerioApp
- Singletons: `@Singleton` on VsaAnalyzer, Room DAOs

**EntryPoint pattern:** See `FalaSerioApp.onCreate()` for accessing repositories before Hilt context is ready.

## Credits & Monetization

### Credit Flow
1. New users get 3 free credits (`CreditsRepository.initializeForNewUser()`)
2. `MainViewModel.startRecording()` checks credits BEFORE recording starts
3. After successful analysis, credits are deducted in `analyzeRecording()`
4. Unlimited subscription (`isUnlimited = true`) sets credits to `Int.MAX_VALUE`

### Products (ProductInfo.kt)
- **INAPPs**: `pack_10_credits`, `pack_20_credits`, `lifetime_unlimited`, `perpetual_100`
- **SUBS**: `subscriber_30`, `subscriber_50` (monthly, no ads)

**AdMob:** Test App ID in manifest (`ca-app-pub-3940256099942544~3347511713`). Replace before production.

## Data Layer

### Room Database (v1)
- `HistoryEntity` - Stores analysis results with timestamp, questionText, metrics as JSON
- `CreditsEntity` - Single row tracking available credits and subscription status
- **Migration Strategy:** `fallbackToDestructiveMigration()` for dev (⚠️ change for prod)

### Repositories
Both use `Flow<T>` for reactive updates:
- `HistoryRepository.getAllHistory()` returns `Flow<List<HistoryEntity>>`
- `CreditsRepository.getCredits()` returns `Flow<CreditsEntity?>`

## Compose Navigation

NavGraph defines 3 routes:
- `"home"` - Main screen with recording + analysis
- `"history"` - List of past analyses (LazyColumn with HistoryEntity items)
- `"credits"` - Purchase screen with billing integration

## Testing

**Current state:** Only 1 test file (`RecordingLoopOptimizationTest.kt`). Unit tests needed for:
1. VsaAnalyzer (deterministic metrics, edge cases)
2. CreditsRepository (credit deduction logic)
3. MainViewModel (state transitions)

Test dependencies: JUnit 4.13.2, MockK 1.13.13, Coroutines Test 1.8.1

## Common Pitfalls

1. **Hilt not working?** Check `@HiltAndroidApp` on Application class and `@AndroidEntryPoint` on Activity
2. **Compose compiler errors?** Use `kotlin.plugin.compose` plugin (NOT `composeOptions.kotlinCompilerExtensionVersion`)
3. **Duplicate class errors?** Run `./gradlew clean` - common with Hilt + Room + KSP
4. **Room schema errors?** KSP, not kapt - ensure `ksp("androidx.room:room-compiler")`
5. **Audio recording silent?** Check RECORD_AUDIO permission granted at runtime (Accompanist Permissions)
6. **Credits not updating?** CreditsRepository uses Flow - collect in ViewModel with `viewModelScope.launch`

## Code Style

- Package structure: `br.com.webstorage.falaserio.<layer>.<feature>`
- ViewModels expose `StateFlow<T>` (never `MutableStateFlow`)
- Use `withContext(Dispatchers.Default)` for CPU-intensive work (DSP algorithms)
- Compose screens receive ViewModel instances, not individual state parameters
- Repository functions return Flow, not LiveData
- All database operations are suspend functions

## Documentation References

- `README.md` - Metrics explanation, tech stack, feature list
- `CLAUDE.md` - Build commands, architecture diagrams, DSP pipeline
- `IMPROVEMENTS_AND_SUGGESTIONS.md` - Known issues (random factor, missing tests)
- `CODE_REVIEW_REPORT.md` - Detailed code review findings
- `DEVELOPER_MONETIZATION_GUIDE.md` - Billing integration details
