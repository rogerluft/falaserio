# Guia de Monetizacao - FalaSério

Guia completo para configurar, gerenciar e operar a monetizacao do app via Google Play.

---

## Indice

1. [Visao Geral da Monetizacao](#1-visao-geral-da-monetizacao)
2. [Google Play Console - Configuracao Inicial](#2-google-play-console---configuracao-inicial)
3. [Criar Produtos no Google Play](#3-criar-produtos-no-google-play)
4. [Configurar Assinaturas](#4-configurar-assinaturas)
5. [Integrar com o App](#5-integrar-com-o-app)
6. [Testes de Compra](#6-testes-de-compra)
7. [Submissao para Aprovacao](#7-submissao-para-aprovacao)
8. [Gestao de Clientes e Vendas](#8-gestao-de-clientes-e-vendas)
9. [Relatorios e Analytics](#9-relatorios-e-analytics)
10. [Problemas Comuns](#10-problemas-comuns)

---

## 1. Visao Geral da Monetizacao

### Modelo de Negocios FalaSério

| Tipo | Produto | Preco Sugerido | Beneficio |
|------|---------|----------------|-----------|
| Consumivel | 10 Creditos | R$ 4,99 | +10 analises |
| Consumivel | 20 Creditos | R$ 7,99 | +20 analises (popular) |
| Assinatura | Mensal 30 | R$ 9,90/mes | 30/mes + sem anuncios |
| Assinatura | Mensal 50 | R$ 14,90/mes | 50/mes + sem anuncios |
| Permanente | Vitalicio | R$ 29,99 | Ilimitado para sempre |
| Permanente | Mega Pack | R$ 19,99 | 100 creditos + sem anuncios |

### Arquivos de Configuracao no App

```
domain/billing/
├── MonetizationConfig.kt    # IDs dos produtos e configuracoes
├── BillingManager.kt        # Integracao com Google Play Billing
└── ProductInfo.kt           # Modelos de dados
```

---

## 2. Google Play Console - Configuracao Inicial

### 2.1 Acessar o Console

1. Acesse [play.google.com/console](https://play.google.com/console)
2. Faca login com conta de desenvolvedor
3. Selecione o app **FalaSério**

### 2.2 Requisitos para Monetizacao

- [ ] Conta de desenvolvedor verificada (R$ 125 taxa unica)
- [ ] Perfil de pagamento configurado
- [ ] Conta bancaria vinculada
- [ ] Informacoes fiscais preenchidas
- [ ] App publicado (pelo menos em teste interno)

### 2.3 Configurar Perfil de Pagamento

1. `Configuracoes > Perfil de pagamento`
2. Preencha dados bancarios
3. Configure informacoes fiscais (CPF/CNPJ)
4. Aguarde verificacao (1-3 dias)

---

## 3. Criar Produtos no Google Play

### 3.1 Acessar Produtos

1. No Play Console, selecione o app
2. `Monetizar > Produtos no app`

### 3.2 Criar Produto Consumivel (Creditos)

1. Clique **Criar produto**
2. Preencha:

```
ID do produto: pack_10_credits
Nome: 10 Creditos
Descricao: Pacote com 10 analises de voz
Preco: R$ 4,99
```

3. Clique **Salvar**
4. Clique **Ativar**

### 3.3 IDs dos Produtos (MonetizationConfig.kt)

```kotlin
object MonetizationConfig {
    // Consumiveis
    const val PACK_10_CREDITS = "pack_10_credits"
    const val PACK_20_CREDITS = "pack_20_credits"
    const val PERPETUAL_100 = "perpetual_100"
    const val LIFETIME_UNLIMITED = "lifetime_unlimited"

    // Assinaturas
    const val SUBSCRIBER_30 = "subscriber_30"
    const val SUBSCRIBER_50 = "subscriber_50"
}
```

### 3.4 Tabela de Produtos a Criar

| ID | Tipo | Nome no Console | Preco |
|----|------|-----------------|-------|
| `pack_10_credits` | Produto gerenciado | 10 Creditos | R$ 4,99 |
| `pack_20_credits` | Produto gerenciado | 20 Creditos | R$ 7,99 |
| `perpetual_100` | Produto gerenciado | 100 Creditos + Sem Ads | R$ 19,99 |
| `lifetime_unlimited` | Produto gerenciado | Acesso Vitalicio | R$ 29,99 |
| `subscriber_30` | Assinatura | Plano Mensal 30 | R$ 9,90/mes |
| `subscriber_50` | Assinatura | Plano Mensal 50 | R$ 14,90/mes |

---

## 4. Configurar Assinaturas

### 4.1 Acessar Assinaturas

1. `Monetizar > Assinaturas`
2. Clique **Criar assinatura**

### 4.2 Criar Assinatura

```
ID do produto: subscriber_30
Nome: Plano Mensal 30
Descricao: 30 analises por mes + sem anuncios

Plano base:
- Periodo: 1 mes
- Preco: R$ 9,90
- Periodo de teste: 7 dias (opcional)
- Preco promocional: R$ 4,90 primeiro mes (opcional)
```

### 4.3 Configurar Periodo de Carencia

- **Grace period**: 7 dias (tempo para renovar pagamento falho)
- **Account hold**: 30 dias (suspensao antes de cancelar)

### 4.4 Politica de Cancelamento

Configure em `Assinatura > Configuracoes`:
- Reembolso pro-rata: Sim/Nao
- Acesso apos cancelamento: Ate fim do periodo pago

---

## 5. Integrar com o App

### 5.1 Adicionar Dependencia

Em `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.android.billingclient:billing-ktx:7.1.1")
}
```

### 5.2 Permissao no Manifest

Em `AndroidManifest.xml`:
```xml
<uses-permission android:name="com.android.vending.BILLING"/>
```

### 5.3 Estrutura do BillingManager

```kotlin
// domain/billing/BillingManager.kt
class BillingManager @Inject constructor(
    private val context: Context
) {
    private var billingClient: BillingClient? = null

    fun startConnection() { ... }
    fun queryProducts() { ... }
    fun launchPurchaseFlow(activity: Activity, product: ProductDetails) { ... }
    fun handlePurchase(purchase: Purchase) { ... }
    fun acknowledgePurchase(purchase: Purchase) { ... }
}
```

### 5.4 Fluxo de Compra

```
1. Usuario clica em "Comprar"
2. App chama BillingManager.launchPurchaseFlow()
3. Google Play mostra tela de pagamento
4. Usuario confirma
5. Google retorna Purchase ao app
6. App verifica e concede beneficio
7. App chama acknowledgePurchase()
```

### 5.5 Verificar Compras Pendentes

```kotlin
// No startup do app
billingClient.queryPurchasesAsync(
    QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()
) { result, purchases ->
    purchases.forEach { purchase ->
        if (!purchase.isAcknowledged) {
            // Conceder beneficio e acknowledgar
        }
    }
}
```

---

## 6. Testes de Compra

### 6.1 Configurar Testadores

1. `Configuracoes > Testes de licenca`
2. Adicione emails dos testadores
3. Esses usuarios podem comprar sem cobrar

### 6.2 Tracks de Teste

| Track | Uso |
|-------|-----|
| Internal testing | Equipe interna (ate 100 pessoas) |
| Closed testing | Beta privado (lista de emails) |
| Open testing | Beta publico |

### 6.3 Criar Versao de Teste

1. `Testar > Testes internos`
2. **Criar nova versao**
3. Upload do APK/AAB
4. Adicione testadores
5. Publique

### 6.4 Testar Compras

1. Instale o app via Play Store (link de teste)
2. Faca login com conta de testador
3. Tente comprar - nao sera cobrado
4. Verifique se creditos foram adicionados

### 6.5 Cartoes de Teste

Google fornece cartoes de teste:
- **Sempre aprova**: 4111 1111 1111 1111
- **Sempre recusa**: Qualquer outro

---

## 7. Submissao para Aprovacao

### 7.1 Checklist Pre-Submissao

- [ ] Todos os produtos criados e ativos
- [ ] Precos configurados para todos os paises
- [ ] Politica de privacidade publicada
- [ ] Termos de uso (se assinatura)
- [ ] Screenshots atualizados
- [ ] Descricao do app completa
- [ ] Classificacao de conteudo definida
- [ ] App testado internamente

### 7.2 Submeter para Revisao

1. `Producao > Criar nova versao`
2. Upload do AAB assinado
3. Preencha notas da versao
4. **Revisar e publicar**

### 7.3 Tempo de Aprovacao

- Primeira submissao: 3-7 dias
- Atualizacoes: 1-3 dias
- Rejeicao: Corrigir e resubmeter

### 7.4 Motivos Comuns de Rejeicao

| Problema | Solucao |
|----------|---------|
| Falta politica de privacidade | Adicionar link na ficha |
| Descricao enganosa | Revisar texto |
| Monetizacao nao clara | Explicar modelo de creditos |
| Crash no startup | Testar em multiplos devices |
| Permissoes excessivas | Remover permissoes nao usadas |

---

## 8. Gestao de Clientes e Vendas

### 8.1 Dashboard de Vendas

1. `Monetizar > Relatorios financeiros`
2. Visualize:
   - Receita total
   - Vendas por produto
   - Assinantes ativos
   - Cancelamentos

### 8.2 Gestao de Assinaturas

1. `Monetizar > Assinaturas > Gerenciar`
2. Acoes disponiveis:
   - Ver assinantes
   - Cancelar assinatura
   - Emitir reembolso
   - Estender periodo

### 8.3 Reembolsos

**Automatico (ate 48h):**
- Usuario solicita via Play Store
- Google processa automaticamente

**Manual:**
1. `Historico de pedidos`
2. Busque pelo pedido
3. Clique em **Reembolsar**

### 8.4 Disputas de Cobranca

1. `Historico de pedidos > Disputas`
2. Responda com evidencias
3. Google decide em 7-14 dias

### 8.5 Comunicacao com Clientes

- Nao ha acesso direto aos emails
- Use notificacoes push para comunicar
- Configure email de suporte na ficha do app

---

## 9. Relatorios e Analytics

### 9.1 Metricas Principais

| Metrica | Onde Ver | Significado |
|---------|----------|-------------|
| MRR | Assinaturas | Receita mensal recorrente |
| ARPU | Financeiro | Receita media por usuario |
| Churn | Assinaturas | Taxa de cancelamento |
| LTV | Financeiro | Valor vitalicio do cliente |
| Conversion | Funil | Taxa de conversao |

### 9.2 Exportar Dados

1. `Fazer download de relatorios`
2. Selecione periodo
3. Escolha formato (CSV)
4. Configure entrega automatica (opcional)

### 9.3 Integrar com Firebase

```kotlin
// Logar eventos de compra
Firebase.analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
    param(FirebaseAnalytics.Param.CURRENCY, "BRL")
    param(FirebaseAnalytics.Param.VALUE, 4.99)
    param(FirebaseAnalytics.Param.ITEM_ID, "pack_10_credits")
}
```

### 9.4 Funil de Conversao

```
Usuarios totais
    |
    v
Abriram tela de creditos (Event: view_credits)
    |
    v
Clicaram em produto (Event: select_product)
    |
    v
Iniciaram checkout (Event: begin_checkout)
    |
    v
Compraram (Event: purchase)
```

---

## 10. Problemas Comuns

### 10.1 "Item not available"

**Causa:** Produto nao ativo ou app nao publicado

**Solucao:**
1. Verifique se produto esta "Ativo" no Console
2. Publique pelo menos em teste interno
3. Aguarde 24h para propagacao

### 10.2 "Purchase failed"

**Causa:** Configuracao de billing incorreta

**Solucao:**
1. Verifique applicationId no build.gradle
2. Confirme que AAB foi enviado ao Play
3. Teste com conta de testador licenciado

### 10.3 Compra nao reconhecida

**Causa:** Falta de acknowledgePurchase

**Solucao:**
```kotlin
if (!purchase.isAcknowledged) {
    val params = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()
    billingClient.acknowledgePurchase(params) { result ->
        if (result.responseCode == BillingResponseCode.OK) {
            // Sucesso
        }
    }
}
```

### 10.4 Assinatura nao renova

**Causa:** Problema de pagamento

**Solucao:**
1. Configure grace period (7 dias)
2. Configure account hold (30 dias)
3. Envie notificacao push lembrando

### 10.5 Restaurar compras nao funciona

**Causa:** queryPurchasesAsync nao retorna historico

**Solucao:**
- Consumiveis: Nao sao restauraveis (design intencional)
- Assinaturas: queryPurchasesAsync retorna ativas
- Permanentes (nao consumidos): queryPurchasesAsync retorna

---

## Checklist de Lancamento

### Antes de Publicar

- [ ] Produtos criados e ativos no Console
- [ ] Precos configurados
- [ ] BillingManager implementado
- [ ] Testes internos aprovados
- [ ] Politica de privacidade
- [ ] Fluxo de compra testado
- [ ] Restaurar compras funciona
- [ ] Analytics de compra configurado

### Apos Publicar

- [ ] Monitorar primeiras vendas
- [ ] Verificar taxa de conclusao de compra
- [ ] Acompanhar reviews sobre compras
- [ ] Configurar alertas de disputa

---

*Documento atualizado em: Janeiro 2026*
