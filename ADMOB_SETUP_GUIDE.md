# 📱 Guia de Configuração AdMob - FalaSério

## 🎯 Objetivo

Substituir os IDs de teste do AdMob pelos IDs reais de produção antes do lançamento na Play Store.

---

## 📋 Passo 1: Criar Conta e App no AdMob

### 1.1 Acesse o AdMob
🔗 https://apps.admob.com/

### 1.2 Faça login com sua conta Google

### 1.3 Clique em "Apps" → "Adicionar App"

**Configurações:**
- Nome do app: **Fala Sério**
- Plataforma: **Android**
- App na Play Store? **Sim** (ou Não se ainda não publicou)
- Package name: `br.com.webstorage.falaserio`

### 1.4 Copie o **App ID** gerado
Formato: `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`

---

## 📋 Passo 2: Criar Unidades de Anúncio

### 2.1 Banner Ad (Tela de Créditos)

1. No seu app → "Unidades de anúncio" → "Adicionar unidade"
2. Selecione: **Banner**
3. Nome: `FalaSério Banner - Credits`
4. Configurações:
   - Tipo: Banner padrão (320x50)
   - Atualização: 60 segundos
5. Copie o **ID da unidade**
   - Formato: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`

### 2.2 Rewarded Ad (Ganhar Crédito Grátis)

1. "Unidades de anúncio" → "Adicionar unidade"
2. Selecione: **Premiado**
3. Nome: `FalaSério Rewarded - Free Credit`
4. Recompensa:
   - Nome: `Crédito Grátis`
   - Valor: `1`
5. Copie o **ID da unidade**

---

## 📋 Passo 3: Atualizar Código

### 3.1 AndroidManifest.xml

```bash
# Abrir arquivo
nano app/src/main/AndroidManifest.xml
```

**Substituir linha 41:**
```xml
<!-- ANTES (TESTE) -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />

<!-- DEPOIS (PRODUÇÃO) -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" />
```

### 3.2 AdsConfig.kt

```bash
# Abrir arquivo
nano app/src/main/kotlin/br/com/webstorage/falaserio/domain/ads/AdsConfig.kt
```

**Substituir linhas 61-66 no objeto Production:**

```kotlin
// ANTES (TESTE)
object Production {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    const val REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
}

// DEPOIS (SEUS IDs REAIS)
object Production {
    const val APP_ID = "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"  // App ID
    const val BANNER = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"  // Banner Unit
    const val INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"  // (Futuro)
    const val REWARDED = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"  // Rewarded Unit
    const val REWARDED_INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"  // (Futuro)
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"  // (Futuro, pode deixar teste)
}
```

**Alterar linha 49 para modo PRODUÇÃO:**
```kotlin
// ANTES
const val IS_TEST_MODE = true

// DEPOIS
const val IS_TEST_MODE = false  // PRODUÇÃO
```

---

## 📋 Passo 4: Configurar Mediação (Opcional mas Recomendado)

### Por que usar mediação?
- Aumenta receita em até 30%
- Preenche mais impressões
- Competição entre redes

### Redes recomendadas:
1. **AdMob** (padrão)
2. **Meta Audience Network**
3. **AppLovin**
4. **Unity Ads**

### Como configurar:
1. No AdMob → "Mediação" → "Criar grupo de mediação"
2. Selecione as redes
3. Configure waterfalls (ordem de prioridade)
4. Adicione App IDs de cada rede

---

## 📋 Passo 5: Testar Anúncios

### 5.1 Build de Debug (com IDs de produção)

```bash
# Build e instalar
./gradlew clean installDebug

# Abrir app no dispositivo
# Ir em: Tela de Créditos → Ver se banner aparece
# Tentar: Assistir anúncio → Verificar rewarded ad
```

### 5.2 Verificar Logs

```bash
# No Android Studio ou terminal
adb logcat | grep -i "admob"

# Procurar por:
# - "Ad loaded successfully"
# - "Ad failed to load" (se houver erro)
```

### 5.3 Modo de Teste

Para testar sem afetar métricas:
1. AdMob Console → "Configurações" → "Dispositivos de teste"
2. Adicionar seu dispositivo (Android ID)
3. Anúncios serão marcados como "Test Ad"

---

## 📋 Passo 6: Validações Finais

### ✅ Checklist

- [ ] App ID substituído no `AndroidManifest.xml`
- [ ] IDs de produção em `AdsConfig.kt` objeto `Production`
- [ ] `IS_TEST_MODE = false` configurado
- [ ] Banner aparece na tela de Créditos
- [ ] Rewarded ad funciona ao clicar "Assistir Anúncio"
- [ ] Crédito é adicionado após assistir
- [ ] Sem crashes ao carregar ads
- [ ] Sem ads de teste aparecendo

---

## 🚨 Problemas Comuns

### "Ad failed to load: 3"
**Causa:** App não está publicado na Play Store ainda  
**Solução:** 
- Use modo de teste enquanto desenvolve
- Publique versão alpha/beta no Play Console

### "Invalid Ad Unit ID"
**Causa:** ID copiado incorretamente  
**Solução:** Verificar se copiou ID completo sem espaços

### "No fill"
**Causa:** Sem anúncios disponíveis para seu perfil  
**Solução:** 
- Normal em apps novos (leva alguns dias)
- Configure mediação para mais fill rate

### Banner não aparece
**Causa:** AdMob ainda não ativado  
**Solução:**
- Aguardar aprovação do AdMob (até 24h)
- Verificar se app está ativo no Console

---

## 📊 Monitoramento

### Métricas Importantes

**No AdMob Console:**
- **Impressões:** Quantas vezes ads foram mostrados
- **Cliques:** Quantos cliques nos ads
- **CTR:** Taxa de clique (ideal: 1-3%)
- **eCPM:** Ganho por 1000 impressões
- **Receita:** Total ganho

**Metas iniciais:**
- 1000 impressões/dia = ~R$ 5-10/dia
- 10.000 impressões/dia = ~R$ 50-100/dia

---

## 💰 Otimização de Receita

### Dicas para maximizar ganhos:

1. **Use Rewarded Ads estrategicamente**
   - Ofereça valor claro (1 crédito grátis)
   - Posicione em momentos-chave (sem créditos)

2. **Banner Ads com moderação**
   - Só nas telas estáticas (Créditos, Histórico)
   - Nunca na tela de gravação

3. **Configure Mediação**
   - Múltiplas redes = mais fill rate
   - Competição = CPMs maiores

4. **Teste A/B**
   - Testar posições diferentes
   - Testar formatos diferentes

---

## 📝 IDs de Exemplo (SEUS IDs REAIS)

Copie seus IDs aqui para referência:

```
App ID:
ca-app-pub-________________~__________

Banner Unit ID:
ca-app-pub-________________/__________

Rewarded Unit ID:
ca-app-pub-________________/__________
```

---

## 🔗 Links Úteis

- [AdMob Console](https://apps.admob.com/)
- [Documentação AdMob Android](https://developers.google.com/admob/android/quick-start)
- [Políticas de Anúncios](https://support.google.com/admob/answer/6128543)
- [Mediação AdMob](https://support.google.com/admob/answer/3063564)
- [eCPM Calculator](https://www.omnicalculator.com/finance/cpm)

---

**Status:** ⚠️ PENDENTE - IDs de teste ainda ativos  
**Próximo passo:** Criar conta AdMob e substituir IDs  
**Tempo estimado:** 30-60 minutos
