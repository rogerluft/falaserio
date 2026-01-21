# FalaSério Gemini Guidelines

This document serves as the authoritative guide for the Gemini agent when working on the **FalaSério** project.

---

## 🛑 UNBREAKABLE RULES (NON-NEGOTIABLE)

1.  **NO PLACEHOLDERS:** Never use placeholders like `your_api_key`, `...`, or `<insert code here>`. Provide complete, working code.
2.  **NO HARDCODED SECRETS:** Do not hardcode API keys, secrets, or credentials. Use BuildConfig or environment variables.
3.  **NO UNAUTHORIZED CODE CHANGES:** You are **strictly prohibited** from altering any file without an explicit request.
4.  **ALWAYS ASK BEFORE GIT OPERATIONS:** You must ask for permission before performing any git action.
5.  **NO GUESSWORK:** If you are unsure about a requirement, ASK. Do not assume.

---

## 📱 Project Overview

**FalaSério** is an Android Voice Stress Analysis (VSA) entertainment app built with Clean Architecture.

### Tech Stack
- **Language:** Kotlin 2.1.0
- **UI:** Jetpack Compose (BOM 2025.01.00)
- **DI:** Hilt 2.57.2
- **Database:** Room 2.6.1
- **Build:** Gradle KTS with KSP 2.1.0-1.0.29
- **Min SDK:** 24 / **Target SDK:** 35

---

## 🏗️ Architecture (Clean Architecture)

```
presentation/          # UI (Compose + ViewModels)
├── ui/screens/       # HomeScreen, HistoryScreen, CreditsScreen
├── viewmodel/        # MainViewModel, HistoryViewModel, CreditsViewModel
└── navigation/       # NavGraph with 3 routes

domain/               # Business logic
├── audio/           # VsaAnalyzer (DSP algorithms), AudioRecorder
├── model/           # VsaMetrics (5 stress metrics)
├── usecase/         # AnalyzeAudioUseCase
└── billing/         # BillingManager, MonetizationConfig, MonetizationManager

data/                 # Data layer
├── local/           # Room (AppDatabase, DAOs, Entities)
└── repository/      # CreditsRepository, HistoryRepository

di/                   # Hilt modules
├── AudioModule      # Provides AudioRecorder
├── DatabaseModule   # Provides Room + DAOs
└── VsaModule        # Provides VsaAnalyzer
```

---

## 🎵 DSP Pipeline (VsaAnalyzer.kt)

The core analysis flow:
1. Read WAV file (44.1kHz, 16-bit PCM)
2. Extract frames (4096 samples, 50% overlap)
3. Apply Hamming window
4. Calculate 5 metrics: Micro-Tremor, Pitch Variation, Jitter, Shimmer, HNR
5. Compute weighted stress score (0-100%)

### Audio Technical Details
- **Audio Format:** WAV 44.1kHz 16-bit PCM mono
- **Frame Size:** 4096 samples (~93ms)
- **Hop Size:** 2048 samples (50% overlap)
- **Pitch Range:** 80-400Hz (human voice)
- **Stress Score:** Weighted average (Tremor 30%, Pitch 20%, Jitter 20%, Shimmer 15%, HNR 15%)

---

## 💰 Monetization System

Products are centralized in `MonetizationConfig.kt`:

| ID | Type | Description |
|----|------|-------------|
| `pack_10_credits` | INAPP | +10 credits |
| `pack_20_credits` | INAPP | +20 credits |
| `subscriber_30` | SUBS | 30/month + no ads |
| `subscriber_50` | SUBS | 50/month + no ads |
| `lifetime_unlimited` | INAPP | Unlimited forever |
| `perpetual_100` | INAPP | 100 + no ads |

### Adding Products
1. Add to `MonetizationConfig.ALL_PRODUCTS`
2. Configure in Google Play Console
3. No other code changes needed

---

## 🔧 Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug

# Run tests
./gradlew test
```

---

## 🚫 Code Review Focus Areas

When reviewing PRs, pay special attention to:

1. **Memory leaks** in audio recording/playback
2. **Permissions** handling (RECORD_AUDIO)
3. **Billing** state management
4. **Compose** recomposition optimization
5. **DSP** algorithm correctness
6. **Thread safety** in repositories

---

## 📝 Coding Standards

- Follow Kotlin official style guide
- Use `sealed class` for state management
- Prefer `StateFlow` over `LiveData`
- Use `@Composable` functions with lowercase prefixes for internal composables
- Document public APIs with KDoc
- Handle all errors gracefully with user-friendly messages
