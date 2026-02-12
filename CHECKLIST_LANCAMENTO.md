# ✅ Checklist de Lançamento - FalaSério v1.0
**Status geral:** 🟡 85% pronto para lançamento

---

## 🔴 CRÍTICO - BLOQUEADORES DE LANÇAMENTO

### 1. ⚠️ Substituir IDs de Teste do AdMob
**Status:** ❌ NÃO FEITO  
**Prioridade:** CRÍTICA  
**Tempo estimado:** 30 min  

**Arquivos a modificar:**
- `app/src/main/AndroidManifest.xml` (linha 41)
- `app/src/main/kotlin/.../domain/ads/AdsConfig.kt` (linhas 61-66)

**Ações necessárias:**
```kotlin
// TROCAR NO AndroidManifest.xml:
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" /> <!-- SEU ID REAL -->

// TROCAR NO AdsConfig.kt:
object Production {
    const val APP_ID = "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
    const val BANNER = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    const val REWARDED = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    // ... etc
}

// E ALTERNAR O MODO:
const val IS_TEST_MODE = false // PRODUÇÃO
```

**Como obter os IDs:**
1. Acesse [AdMob Console](https://apps.admob.com/)
2. Crie o app "Fala Sério"
3. Crie unidades de anúncio: Banner, Rewarded
4. Copie os IDs gerados

---

### 2. 🔐 Configurar Assinatura do APK (Release)
**Status:** ❌ NÃO FEITO  
**Prioridade:** CRÍTICA  
**Tempo estimado:** 20 min  

**Arquivos a criar/modificar:**
- Criar arquivo `keystore.jks` (não commitar!)
- Modificar `app/build.gradle.kts`

**Ações necessárias:**

```bash
# 1. Gerar keystore (fazer UMA VEZ, GUARDAR SENHA!)
keytool -genkey -v -keystore falaserio-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias falaserio

# 2. Mover para pasta segura (NÃO commitar no git!)
mkdir ~/keystores
mv falaserio-release.jks ~/keystores/
```

```kotlin
// 3. Adicionar em app/build.gradle.kts (ANTES de buildTypes):
android {
    signingConfigs {
        create("release") {
            // Usar variáveis de ambiente para segurança
            storeFile = file(System.getenv("FALASERIO_KEYSTORE_PATH") 
                ?: "${System.getProperty("user.home")}/keystores/falaserio-release.jks")
            storePassword = System.getenv("FALASERIO_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("FALASERIO_KEY_ALIAS") ?: "falaserio"
            keyPassword = System.getenv("FALASERIO_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release") // ADICIONAR ESTA LINHA
            isMinifyEnabled = true
            isShrinkResources = true
            // ... resto igual
        }
    }
}
```

```bash
# 4. Criar arquivo .env local (NÃO commitar!)
export FALASERIO_KEYSTORE_PATH="/home/usuario/keystores/falaserio-release.jks"
export FALASERIO_KEYSTORE_PASSWORD="sua_senha_aqui"
export FALASERIO_KEY_ALIAS="falaserio"
export FALASERIO_KEY_PASSWORD="sua_senha_aqui"
```

**⚠️ GUARDAR COM SEGURANÇA:**
- Senha do keystore
- Arquivo `.jks` (fazer backup em local seguro)
- Se perder, NUNCA poderá atualizar o app na Play Store!

---

### 3. 📝 Política de Privacidade
**Status:** ⚠️ URL CONFIGURADA MAS PRECISA SER CRIADA  
**Prioridade:** CRÍTICA  
**Tempo estimado:** 2 horas  

**URL configurada:** `https://rogerluft.com.br/falaserio/privacy`  
**Arquivo:** `domain/ads/AdsConfig.kt` linha 224

**Ações necessárias:**
1. Criar página web em `rogerluft.com.br/falaserio/privacy`
2. Incluir os tópicos obrigatórios:
   - ✅ Coleta de dados (áudio gravado LOCAL, não enviado)
   - ✅ Uso de AdMob (coleta de dados para anúncios)
   - ✅ Uso de Google Play Billing (compras in-app)
   - ✅ Permissões necessárias (microfone)
   - ✅ Armazenamento local (Room database)
   - ✅ Direitos do usuário (LGPD/GDPR)
   - ✅ Contato

**Template sugerido:** [Termly Privacy Policy Generator](https://termly.io/products/privacy-policy-generator/)

---

### 4. 🛒 Configurar Produtos no Google Play Console
**Status:** ❌ NÃO FEITO  
**Prioridade:** CRÍTICA  
**Tempo estimado:** 1 hora  

**Produtos configurados no código** (ver `MonetizationConfig.kt`):

**INAPPs (Consumíveis):**
- `pack_10_credits` - 10 créditos (preço sugerido: R$ 4,90)
- `pack_20_credits` - 20 créditos (preço sugerido: R$ 8,90)
- `perpetual_100` - 100 créditos + sem ads (preço sugerido: R$ 24,90)

**INAPPs (Não-consumíveis):**
- `lifetime_unlimited` - Ilimitado para sempre (preço sugerido: R$ 49,90)

**Assinaturas (Mensais):**
- `subscriber_30` - 30 análises/mês + sem ads (preço sugerido: R$ 9,90/mês)
- `subscriber_50` - 50 análises/mês + sem ads (preço sugerido: R$ 14,90/mês)

**Ações necessárias:**
1. Acesse [Google Play Console](https://play.google.com/console)
2. Crie app "Fala Sério"
3. Vá em "Monetização" → "Produtos in-app"
4. Crie cada produto com IDs EXATAMENTE iguais aos do código
5. Configure preços, descrições, imagens

---

## 🟡 IMPORTANTE - RECOMENDADO ANTES DO LANÇAMENTO

### 5. 🧪 Testes Mínimos
**Status:** ❌ APENAS 1 TESTE  
**Prioridade:** ALTA  
**Tempo estimado:** 4 horas  

**Arquivo existente:** `RecordingLoopOptimizationTest.kt` (apenas 1)

**Testes mínimos recomendados:**

```kotlin
// VsaAnalyzerTest.kt
- testAnalyzeValidWavFile()
- testAnalyzeInvalidFile()
- testAnalyzeEmptyFile()
- testMetricsRanges() // Verificar se valores estão em range esperado

// CreditsRepositoryTest.kt
- testInitializeForNewUser()
- testDeductCredit()
- testUnlimitedSubscription()

// MainViewModelTest.kt
- testStartRecordingWithoutCredits()
- testStartRecordingWithCredits()
- testAnalyzeAfterRecording()
```

**Como implementar:**
```bash
# Criar arquivos em app/src/test/kotlin/...
./gradlew test  # Rodar testes
```

---

### 6. 📱 Assets da Play Store
**Status:** ❌ NÃO CRIADOS  
**Prioridade:** ALTA  
**Tempo estimado:** 3 horas  

**Assets necessários:**
- ✅ Ícone app 512x512 (existe em `res/mipmap`)
- ❌ Feature Graphic 1024x500
- ❌ Screenshots (mínimo 2, máximo 8):
  - Telefone: 1080x1920 ou 1080x2340
  - Tablet 7": 1200x1920
  - Tablet 10": 1920x1200
- ❌ Video promocional (opcional, mas recomendado)

**Screenshots sugeridos:**
1. Tela inicial (botão "Toque para gravar")
2. Gravando (amplitude animada)
3. Resultado da análise (com métricas)
4. Histórico de análises
5. Tela de créditos/compras

**Pasta:** Criar `screenshots/playstore/` com os assets

---

### 7. 📄 Descrição da Play Store
**Status:** ⚠️ RASCUNHO NO README  
**Prioridade:** ALTA  
**Tempo estimado:** 1 hora  

**Criar arquivo:** `PLAYSTORE_DESCRIPTION.md`

**Conteúdo necessário:**
```markdown
# Título (30 caracteres)
Fala Sério - Detector de Mentiras

# Descrição curta (80 caracteres)
Detector de stress vocal por IA. Será que estão falando a verdade?

# Descrição completa (4000 caracteres)
[Usar conteúdo do README.md adaptado para usuários finais]

# Tags/Categorias
- Entretenimento
- Casual
- Grátis com compras no app
```

---

### 8. 🔒 ProGuard/R8 para Produção
**Status:** ⚠️ REGRAS BÁSICAS EXISTEM  
**Prioridade:** MÉDIA  
**Tempo estimado:** 30 min  

**Arquivo:** `app/proguard-rules.pro` (já existe com regras básicas)

**Validação necessária:**
```bash
# 1. Build release e testar
./gradlew assembleRelease

# 2. Instalar APK release em dispositivo
adb install app/build/outputs/apk/release/app-release.apk

# 3. Testar TODAS funcionalidades:
- Gravação de áudio ✓
- Análise VSA ✓
- Salvar histórico ✓
- Compras in-app ✓
- Anúncios ✓

# 4. Se algo quebrar, adicionar regras no proguard-rules.pro
```

---

### 9. ⚠️ Aviso Legal Visível
**Status:** ✅ EXISTE MAS PODE MELHORAR  
**Prioridade:** MÉDIA  
**Tempo estimado:** 15 min  

**Atual:** String em `strings.xml` + disclaimer no README

**Recomendação:** Adicionar dialog na primeira execução:
```kotlin
// Mostrar uma vez em FalaSerioApp.onCreate()
if (isFirstRun()) {
    showDisclaimerDialog(
        "Este app é apenas para entretenimento. " +
        "Os resultados NÃO têm validade científica ou legal."
    )
}
```

---

## 🟢 OPCIONAL - PÓS-LANÇAMENTO

### 10. 🎨 Melhorias de UI/UX
**Status:** ✅ FUNCIONAL  
**Prioridade:** BAIXA  
**Pode ser feito após lançamento**

Sugestões:
- Animações mais suaves
- Tela de onboarding
- Tutorial interativo
- Dark mode personalizado
- Mais temas de cores

---

### 11. 📊 Analytics (Firebase/Google Analytics)
**Status:** ❌ NÃO IMPLEMENTADO  
**Prioridade:** BAIXA  
**Pode ser feito após lançamento**

```gradle
// Adicionar em app/build.gradle.kts
implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
```

Eventos úteis:
- `recording_started`
- `recording_completed`
- `analysis_viewed`
- `credit_purchased`
- `ad_watched`

---

### 12. 🌍 Internacionalização (i18n)
**Status:** ❌ APENAS PT-BR  
**Prioridade:** BAIXA  
**Pode ser feito após lançamento**

Idiomas sugeridos:
- Inglês (mercado global)
- Espanhol (América Latina)

---

## 📋 RESUMO EXECUTIVO

### Para lançar versão 1.0 MÍNIMA:
```
CRÍTICO (fazer ANTES do lançamento):
☐ Substituir IDs de teste do AdMob por IDs reais
☐ Configurar assinatura do APK (keystore)
☐ Criar política de privacidade online
☐ Configurar produtos no Google Play Console

IMPORTANTE (MUITO recomendado):
☐ Adicionar testes básicos (VsaAnalyzer, Credits, ViewModel)
☐ Criar assets da Play Store (screenshots, feature graphic)
☐ Escrever descrição da Play Store
☐ Testar APK release com ProGuard

OPCIONAL (pode lançar sem):
☐ Dialog de disclaimer na primeira execução
☐ Analytics (Firebase)
☐ Internacionalização
```

### Tempo estimado total (mínimo):
- **Crítico:** ~4 horas
- **Importante:** ~8 horas
- **TOTAL para v1.0:** ~12 horas de trabalho

---

## 🚀 ORDEM DE EXECUÇÃO RECOMENDADA

1. **Criar keystore** (20 min) - Fazer PRIMEIRO, é pré-requisito
2. **Configurar Google Play Console** (1h) - Criar app e produtos
3. **Obter IDs do AdMob** (30 min) - Criar unidades de anúncio
4. **Substituir IDs de teste** (30 min) - Trocar em código
5. **Criar política de privacidade** (2h) - Página web
6. **Build release e testar** (1h) - `./gradlew assembleRelease`
7. **Screenshots** (3h) - Capturar e editar
8. **Descrição Play Store** (1h) - Escrever conteúdo
9. **Testes básicos** (4h) - Cobrir casos críticos
10. **Upload para Play Store** (1h) - Submeter para revisão

---

## 🔗 LINKS ÚTEIS

- [Google Play Console](https://play.google.com/console)
- [AdMob Console](https://apps.admob.com/)
- [Play Store Listing Guidelines](https://support.google.com/googleplay/android-developer/answer/9866151)
- [App Signing by Google Play](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Privacy Policy Generator](https://www.termsfeed.com/privacy-policy-generator/)
- [Screenshot Guidelines](https://support.google.com/googleplay/android-developer/answer/9866151#screenshots)

---

**Última atualização:** 12 de fevereiro de 2026  
**Versão atual:** 0.1.5-alpha  
**Target:** 1.0 (primeira versão pública)
