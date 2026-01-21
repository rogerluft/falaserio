package br.com.webstorage.falaserio.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.webstorage.falaserio.domain.ads.AdsConfig
import br.com.webstorage.falaserio.domain.billing.MonetizationConfig
import br.com.webstorage.falaserio.domain.billing.ProductType
import br.com.webstorage.falaserio.presentation.ui.theme.ErrorColor
import br.com.webstorage.falaserio.presentation.ui.theme.Primary
import br.com.webstorage.falaserio.presentation.ui.theme.SuccessColor

/**
 * Nome dos arquivos de configuração.
 */
private const val MONETIZATION_CONFIG_FILE = "MonetizationConfig.kt"
private const val ADS_CONFIG_FILE = "AdsConfig.kt"

/**
 * Tela de gerenciamento de monetização (apenas para desenvolvimento).
 *
 * Esta tela permite aos desenvolvedores:
 * - Visualizar todos os produtos configurados
 * - Ver validações de configuração
 * - Verificar IDs, tipos e propriedades
 * - Identificar problemas de configuração
 * - Gerenciar configurações de anúncios (AdMob)
 *
 * NOTA: Esta tela deve ser acessível apenas em builds de desenvolvimento/debug.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetizationManagementScreen(
    onNavigateBack: () -> Unit
) {
    val products = remember { MonetizationConfig.getProductsSorted() }
    val productValidationResults = remember { MonetizationConfig.validateAllProducts() }
    val adsValidationResults = remember { AdsConfig.validate() }
    val hasProductErrors = productValidationResults.isNotEmpty()
    val hasAdsErrors = adsValidationResults.isNotEmpty()
    val hasAnyErrors = hasProductErrors || hasAdsErrors

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciamento de Monetizacao") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (hasAnyErrors) ErrorColor.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Produtos") },
                    icon = { Icon(Icons.Default.AttachMoney, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Anuncios") },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (hasAdsErrors) {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, null)
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> ProductsTab(
                    products = products,
                    validationResults = productValidationResults,
                    hasErrors = hasProductErrors
                )
                1 -> AdsTab(
                    validationErrors = adsValidationResults,
                    hasErrors = hasAdsErrors
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TAB DE PRODUTOS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProductsTab(
    products: List<MonetizationConfig.Product>,
    validationResults: Map<String, List<String>>,
    hasErrors: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status geral
        item {
            StatusCard(
                title = "Status dos Produtos",
                totalItems = products.size,
                itemLabel = "produtos",
                hasErrors = hasErrors,
                errorCount = validationResults.size
            )
        }

        // Instruções
        item {
            ProductInstructionsCard()
        }

        // Mensagem de validação
        if (hasErrors) {
            item {
                ValidationWarningCard("Problemas de configuracao detectados!")
            }
        }

        // Lista de produtos
        items(products) { product ->
            ProductDetailCard(
                product = product,
                errors = validationResults[product.id] ?: emptyList()
            )
        }

        // Footer
        item {
            FooterText("Para adicionar/editar/remover produtos, modifique o arquivo $MONETIZATION_CONFIG_FILE")
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TAB DE ANÚNCIOS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AdsTab(
    validationErrors: List<String>,
    hasErrors: Boolean
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status
        item {
            AdsStatusCard(hasErrors = hasErrors, errorCount = validationErrors.size)
        }

        // Links úteis
        item {
            AdsLinksCard(onLinkClick = { url -> uriHandler.openUri(url) })
        }

        // Instruções
        item {
            AdsInstructionsCard()
        }

        // Erros de validação
        if (hasErrors) {
            item {
                ValidationWarningCard("Problemas na configuracao de Ads!")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        validationErrors.forEach { error ->
                            Text(
                                "- $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorColor
                            )
                        }
                    }
                }
            }
        }

        // Configuração atual
        item {
            AdsCurrentConfigCard()
        }

        // Comportamento
        item {
            AdsBehaviorCard()
        }

        // Footer
        item {
            FooterText("Para modificar configuracoes de Ads, edite o arquivo $ADS_CONFIG_FILE")
        }
    }
}

@Composable
private fun AdsStatusCard(hasErrors: Boolean, errorCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasErrors) ErrorColor.copy(alpha = 0.1f) else SuccessColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (hasErrors) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasErrors) ErrorColor else SuccessColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Configuracao de Anuncios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (hasErrors) "$errorCount problema(s)" else "Tudo OK",
                        color = if (hasErrors) ErrorColor else SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Modo", style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (AdsConfig.IS_TEST_MODE) "TESTE" else "PRODUCAO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (AdsConfig.IS_TEST_MODE) Primary else ErrorColor
                    )
                }
                Column {
                    Text("Consentimento", style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (AdsConfig.Privacy.REQUIRE_CONSENT) "Obrigatorio" else "Opcional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdsLinksCard(onLinkClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Links Uteis - Documentacao Oficial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinkItem("AdMob Console", "https://admob.google.com", onLinkClick)
            LinkItem("Quick Start Guide", "https://developers.google.com/admob/android/quick-start", onLinkClick)
            LinkItem("Banner Ads", "https://developers.google.com/admob/android/banner", onLinkClick)
            LinkItem("Interstitial Ads", "https://developers.google.com/admob/android/interstitial", onLinkClick)
            LinkItem("Rewarded Ads", "https://developers.google.com/admob/android/rewarded", onLinkClick)
            LinkItem("Test Ads IDs", "https://developers.google.com/admob/android/test-ads", onLinkClick)
            LinkItem("Privacy & Consent", "https://developers.google.com/admob/android/privacy", onLinkClick)
        }
    }
}

@Composable
private fun LinkItem(label: String, url: String, onClick: (String) -> Unit) {
    Text(
        text = "- $label",
        style = MaterialTheme.typography.bodySmall,
        color = Primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .clickable { onClick(url) }
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun AdsInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Como Configurar para Producao",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InstructionItem("1. Crie conta no AdMob: admob.google.com")
            InstructionItem("2. Registre o app e obtenha o APP_ID")
            InstructionItem("3. Crie Ad Units (Banner, Interstitial, Rewarded)")
            InstructionItem("4. Em AdsConfig.kt, substitua IDs em ProductionIds")
            InstructionItem("5. Mude IS_TEST_MODE para false")
            InstructionItem("6. Atualize AndroidManifest.xml com APP_ID real")
            InstructionItem("7. Teste com conta de teste antes de publicar!")
        }
    }
}

@Composable
private fun AdsCurrentConfigCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Configuracao Atual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PropertyRow("App ID", AdsConfig.appId.take(35) + "...")
                PropertyRow("Banner ID", AdsConfig.bannerId.takeLast(15))
                PropertyRow("Interstitial ID", AdsConfig.interstitialId.takeLast(15))
                PropertyRow("Rewarded ID", AdsConfig.rewardedId.takeLast(15))
            }
        }
    }
}

@Composable
private fun AdsBehaviorCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Comportamento dos Anuncios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PropertyRow("Intervalo Interstitial", "${AdsConfig.Behavior.INTERSTITIAL_INTERVAL_SECONDS}s")
                PropertyRow("Acoes antes de Ad", "${AdsConfig.Behavior.ACTIONS_BEFORE_INTERSTITIAL}")
                PropertyRow("Creditos por Rewarded", "${AdsConfig.Behavior.REWARDED_CREDITS}")
                PropertyRow("Limite diario Rewarded", "${AdsConfig.Behavior.DAILY_REWARDED_LIMIT}")
                PropertyRow("Banner na Home", if (AdsConfig.Behavior.SHOW_BANNER_ON_HOME) "Sim" else "Nao")
                PropertyRow("Banner no Historico", if (AdsConfig.Behavior.SHOW_BANNER_ON_HISTORY) "Sim" else "Nao")
                PropertyRow("Banner em Creditos", if (AdsConfig.Behavior.SHOW_BANNER_ON_CREDITS) "Sim" else "Nao")
                PropertyRow("Delay inicial", "${AdsConfig.Behavior.INITIAL_AD_DELAY_SECONDS}s")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPONENTES COMPARTILHADOS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StatusCard(
    title: String,
    totalItems: Int,
    itemLabel: String,
    hasErrors: Boolean,
    errorCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasErrors) ErrorColor.copy(alpha = 0.1f) else SuccessColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (hasErrors) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasErrors) ErrorColor else SuccessColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (hasErrors) "Com problemas" else "Tudo OK",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasErrors) ErrorColor else SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total", style = MaterialTheme.typography.bodySmall)
                    Text("$totalItems $itemLabel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (hasErrors) {
                    Column {
                        Text("Problemas", style = MaterialTheme.typography.bodySmall)
                        Text("$errorCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ErrorColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationWarningCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = ErrorColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ErrorColor)
        }
    }
}

@Composable
private fun ProductInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Como Gerenciar Produtos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            InstructionItem("Adicionar: Adicione nova entrada em $MONETIZATION_CONFIG_FILE")
            InstructionItem("Editar: Modifique as propriedades do produto existente")
            InstructionItem("Remover: Remova a entrada da lista (cuidado com compras existentes)")
            InstructionItem("Popular: Defina isPopular = true para destacar na loja")
            InstructionItem("Ordem: Use displayOrder para controlar a ordem de exibicao")
        }
    }
}

@Composable
private fun InstructionItem(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun FooterText(text: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProductDetailCard(
    product: MonetizationConfig.Product,
    errors: List<String>
) {
    val hasErrors = errors.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasErrors) ErrorColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (product.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, "Popular", tint = Primary, modifier = Modifier.size(16.dp))
                    }
                }
                Chip(product.type.name)
            }

            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Propriedades
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PropertyRow("Creditos", when {
                    product.isUnlimited -> "Ilimitado"
                    product.credits > 0 -> "${product.credits}"
                    product.monthlyCredits > 0 -> "${product.monthlyCredits}/mes"
                    else -> "0"
                })
                PropertyRow("Remove Ads", if (product.hideAds) "Sim" else "Nao")
                PropertyRow("Assinatura", if (product.isSubscription) "Sim (${product.subscriptionType})" else "Nao")
                PropertyRow("Ordem", "${product.displayOrder}")
            }

            // Erros
            if (hasErrors) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorColor.copy(alpha = 0.1f), MaterialTheme.shapes.small)
                        .padding(12.dp)
                ) {
                    Text("Problemas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ErrorColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    errors.forEach { error ->
                        Text("- $error", style = MaterialTheme.typography.bodySmall, color = ErrorColor, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Chip(text: String) {
    Surface(shape = MaterialTheme.shapes.small, color = Primary.copy(alpha = 0.2f)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}
