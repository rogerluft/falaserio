package br.com.webstorage.falaserio.domain.ads

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    CONFIGURAÇÃO CENTRALIZADA DE ANÚNCIOS                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  Este arquivo centraliza TODA a configuração de anúncios do app.             ║
 * ║  Para modificar comportamento de ads, edite APENAS este arquivo.             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * ## DOCUMENTAÇÃO OFICIAL
 *
 * - AdMob Quick Start: https://developers.google.com/admob/android/quick-start
 * - Banner Ads: https://developers.google.com/admob/android/banner
 * - Interstitial Ads: https://developers.google.com/admob/android/interstitial
 * - Rewarded Ads: https://developers.google.com/admob/android/rewarded
 * - Test Ads: https://developers.google.com/admob/android/test-ads
 *
 * ## COMO CONFIGURAR PARA PRODUÇÃO
 *
 * 1. Crie uma conta AdMob: https://admob.google.com
 * 2. Registre seu app e obtenha o APP_ID
 * 3. Crie Ad Units (Banner, Interstitial, Rewarded)
 * 4. Substitua os IDs de teste pelos IDs reais abaixo
 * 5. Atualize o AndroidManifest.xml com o APP_ID real
 *
 * ## IDs DE TESTE DO GOOGLE (para desenvolvimento)
 *
 * - App ID: ca-app-pub-3940256099942544~3347511713
 * - Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded: ca-app-pub-3940256099942544/5224354917
 * - Rewarded Interstitial: ca-app-pub-3940256099942544/5354046379
 * - Native Advanced: ca-app-pub-3940256099942544/2247696110
 *
 * ⚠️  IMPORTANTE: Nunca use IDs de teste em produção!
 * ⚠️  IMPORTANTE: Nunca clique em seus próprios anúncios!
 */
object AdsConfig {

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÃO DE AMBIENTE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Define se está em modo de teste.
     *
     * - true: Usa IDs de teste do Google (desenvolvimento)
     * - false: Usa IDs reais de produção
     *
     * ⚠️ ANTES DE PUBLICAR: Mude para false!
     */
    const val IS_TEST_MODE = true

    // ═══════════════════════════════════════════════════════════════════════════
    // IDs DE TESTE (Google Official Test IDs)
    // Documentação: https://developers.google.com/admob/android/test-ads
    // ═══════════════════════════════════════════════════════════════════════════

    private object TestIds {
        const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
        const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // IDs DE PRODUÇÃO (Substitua pelos seus IDs reais do AdMob)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * IDs reais de produção.
     *
     * ## Como obter:
     * 1. Acesse https://admob.google.com
     * 2. Vá em Apps > Seu App > Ad Units
     * 3. Copie os Ad Unit IDs
     *
     * ## Formato do ID:
     * ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
     *              ↑ Publisher ID    ↑ Ad Unit ID
     */
    private object ProductionIds {
        // TODO: Substitua pelo seu App ID real do AdMob
        const val APP_ID = "ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"

        // TODO: Substitua pelos seus Ad Unit IDs reais
        const val BANNER = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
        const val INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
        const val REWARDED = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
        const val REWARDED_INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
        const val NATIVE = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SELETORES DE ID (Automático baseado em IS_TEST_MODE)
    // ═══════════════════════════════════════════════════════════════════════════

    /** App ID para AndroidManifest.xml */
    val appId: String get() = if (IS_TEST_MODE) TestIds.APP_ID else ProductionIds.APP_ID

    /** Ad Unit ID para banners */
    val bannerId: String get() = if (IS_TEST_MODE) TestIds.BANNER else ProductionIds.BANNER

    /** Ad Unit ID para intersticiais */
    val interstitialId: String get() = if (IS_TEST_MODE) TestIds.INTERSTITIAL else ProductionIds.INTERSTITIAL

    /** Ad Unit ID para rewarded (assistir para ganhar) */
    val rewardedId: String get() = if (IS_TEST_MODE) TestIds.REWARDED else ProductionIds.REWARDED

    /** Ad Unit ID para rewarded interstitial */
    val rewardedInterstitialId: String get() = if (IS_TEST_MODE) TestIds.REWARDED_INTERSTITIAL else ProductionIds.REWARDED_INTERSTITIAL

    /** Ad Unit ID para native ads */
    val nativeId: String get() = if (IS_TEST_MODE) TestIds.NATIVE else ProductionIds.NATIVE

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÃO DE COMPORTAMENTO DOS ADS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Configuração de frequência e comportamento dos anúncios.
     */
    object Behavior {

        /**
         * Intervalo mínimo entre intersticiais (em segundos).
         *
         * Recomendação Google: mínimo 60 segundos entre ads.
         * Valor padrão: 120 segundos (2 minutos)
         */
        const val INTERSTITIAL_INTERVAL_SECONDS = 120

        /**
         * Número de ações do usuário antes de mostrar intersticial.
         *
         * Exemplo: mostrar ad a cada 3 análises de áudio.
         * Valor padrão: 3
         */
        const val ACTIONS_BEFORE_INTERSTITIAL = 3

        /**
         * Créditos ganhos ao assistir rewarded ad.
         *
         * Valor padrão: 1 crédito por vídeo assistido
         */
        const val REWARDED_CREDITS = 1

        /**
         * Limite diário de rewarded ads por usuário.
         *
         * Evita abuso do sistema de créditos gratuitos.
         * Valor padrão: 5 por dia
         */
        const val DAILY_REWARDED_LIMIT = 5

        /**
         * Mostrar banner na tela principal?
         *
         * true: Banner fixo na parte inferior
         * false: Sem banner na home
         */
        const val SHOW_BANNER_ON_HOME = true

        /**
         * Mostrar banner na tela de histórico?
         */
        const val SHOW_BANNER_ON_HISTORY = true

        /**
         * Mostrar banner na tela de créditos?
         *
         * Nota: Geralmente desativado para não atrapalhar compras
         */
        const val SHOW_BANNER_ON_CREDITS = false

        /**
         * Delay antes de mostrar primeiro ad (em segundos).
         *
         * Dá tempo do usuário explorar o app antes de ver ads.
         * Valor padrão: 30 segundos
         */
        const val INITIAL_AD_DELAY_SECONDS = 30
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÃO DE CONSENTIMENTO (GDPR/LGPD)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Configurações de privacidade e consentimento.
     *
     * Documentação:
     * - UMP SDK: https://developers.google.com/admob/android/privacy
     * - GDPR: https://developers.google.com/admob/android/eu-consent
     */
    object Privacy {

        /**
         * Requer consentimento explícito do usuário?
         *
         * true: Mostra diálogo de consentimento (GDPR/LGPD)
         * false: Assume consentimento (apenas para testes)
         *
         * ⚠️ Para publicar na EU/Brasil, deve ser true!
         */
        const val REQUIRE_CONSENT = true

        /**
         * Mostrar opção de personalização de ads?
         *
         * true: Usuário pode escolher ads personalizados ou não
         * false: Apenas aceitar/recusar
         */
        const val SHOW_PERSONALIZATION_OPTION = true

        /**
         * URL da política de privacidade.
         *
         * Obrigatório para apps com ads personalizados.
         */
        const val PRIVACY_POLICY_URL = "https://rogerluft.com.br/falaserio/privacy"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDAÇÃO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Valida a configuração atual.
     *
     * @return Lista de erros encontrados (vazia se tudo OK)
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Verifica se IDs de produção foram configurados
        if (!IS_TEST_MODE) {
            if (ProductionIds.APP_ID.contains("XXXX")) {
                errors.add("APP_ID de produção não configurado")
            }
            if (ProductionIds.BANNER.contains("XXXX")) {
                errors.add("BANNER ID de produção não configurado")
            }
            if (ProductionIds.INTERSTITIAL.contains("XXXX")) {
                errors.add("INTERSTITIAL ID de produção não configurado")
            }
            if (ProductionIds.REWARDED.contains("XXXX")) {
                errors.add("REWARDED ID de produção não configurado")
            }
        }

        // Verifica configurações de comportamento
        if (Behavior.INTERSTITIAL_INTERVAL_SECONDS < 60) {
            errors.add("Intervalo de interstitial muito curto (mínimo 60s recomendado)")
        }

        if (Behavior.REWARDED_CREDITS < 1) {
            errors.add("Créditos de rewarded deve ser pelo menos 1")
        }

        // Verifica privacidade
        if (Privacy.REQUIRE_CONSENT && Privacy.PRIVACY_POLICY_URL.isBlank()) {
            errors.add("URL de política de privacidade obrigatória quando requer consentimento")
        }

        return errors
    }

    /**
     * Retorna status formatado da configuração.
     */
    fun getStatusSummary(): String {
        return buildString {
            appendLine("=== AdsConfig Status ===")
            appendLine("Modo: ${if (IS_TEST_MODE) "TESTE" else "PRODUÇÃO"}")
            appendLine("App ID: ${appId.take(30)}...")
            appendLine("Banner: ${if (Behavior.SHOW_BANNER_ON_HOME) "Ativo" else "Desativado"}")
            appendLine("Interstitial Interval: ${Behavior.INTERSTITIAL_INTERVAL_SECONDS}s")
            appendLine("Rewarded Credits: ${Behavior.REWARDED_CREDITS}")
            appendLine("Daily Limit: ${Behavior.DAILY_REWARDED_LIMIT}")

            val errors = validate()
            if (errors.isEmpty()) {
                appendLine("Status: ✅ OK")
            } else {
                appendLine("Status: ⚠️ ${errors.size} problema(s)")
                errors.forEach { appendLine("  - $it") }
            }
        }
    }
}
