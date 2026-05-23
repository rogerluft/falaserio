package br.com.webstorage.falaserio.presentation.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.webstorage.falaserio.FalaSerioApp
import br.com.webstorage.falaserio.domain.model.VsaMetrics
import br.com.webstorage.falaserio.presentation.ui.components.DisclaimerDialog
import br.com.webstorage.falaserio.presentation.ui.theme.Accent
import br.com.webstorage.falaserio.presentation.ui.theme.ErrorColor
import br.com.webstorage.falaserio.presentation.ui.theme.Primary
import br.com.webstorage.falaserio.presentation.ui.theme.Secondary
import br.com.webstorage.falaserio.presentation.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToCredits: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // States from ViewModel
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDuration.collectAsStateWithLifecycle()
    val amplitude by viewModel.currentAmplitude.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val hasCalibration by viewModel.hasCalibration.collectAsStateWithLifecycle()
    val isCalibrating by viewModel.isCalibrating.collectAsStateWithLifecycle()

    var showDisclaimer by remember { mutableStateOf(false) }
    var showCalibrationInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showDisclaimer = FalaSerioApp.isFirstRun(context)
    }

    if (showDisclaimer) {
        DisclaimerDialog(
            onDismiss = {
                FalaSerioApp.setFirstRunComplete(context)
                showDisclaimer = false
            }
        )
    }

    // Audio Permission
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // File Pick Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectAudio(it, context) }
    }

    // Gradient background
    val darkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    // Pulsing micro-animations
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎤 Fala Sério!",
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    // Credits display
                    TextButton(
                        onClick = onNavigateToCredits,
                        colors = ButtonDefaults.textButtonColors(contentColor = Accent)
                    ) {
                        Text(
                            text = if (credits == Int.MAX_VALUE) "∞" else "$credits",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Créditos",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // History button
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Histórico",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkGradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Veracidade Vocal VSA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analise o stress da voz e descubra se é verdade!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Calibration Status Panel
                CalibrationPanel(
                    hasCalibration = hasCalibration,
                    isCalibrating = isCalibrating,
                    onCalibrateClick = { showCalibrationInfoDialog = true },
                    onClearClick = { viewModel.clearCalibration() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dynamic Equalizer / Pulsing circle
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    (if (isCalibrating) Accent else if (isRecording) Secondary else Primary)
                                        .copy(alpha = 0.15f + amplitude * 0.45f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when {
                            uiState.isAnalyzing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(72.dp),
                                    color = Accent,
                                    strokeWidth = 6.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Analisando frequências...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }

                            uiState.metrics != null -> {
                                val truthScore = uiState.metrics!!.overallStressScore.toInt()
                                Text(
                                    text = "$truthScore%",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        truthScore >= 80 -> Primary // Verdade Divina (Green)
                                        truthScore >= 60 -> Accent // Provável Verdade (Cyan)
                                        truthScore >= 40 -> Color.Yellow // Plausível
                                        else -> ErrorColor // Pinóquio / Forrest Gump (Red)
                                    }
                                )
                                Text(
                                    text = uiState.metrics!!.getStressLevel(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            isRecording -> {
                                Text(
                                    text = formatDuration(recordingDuration),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCalibrating) Accent else Secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCalibrating) "GRAVANDO AMOSTRA" else "ESCUTANDO...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                                // Mini visualizer
                                Spacer(modifier = Modifier.height(12.dp))
                                WaveEqualizer(amplitude = amplitude, isRecording = true)
                            }

                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "PRONTO",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Metrics contribution (soma/subtrai)
                AnimatedVisibility(
                    visible = uiState.metrics != null && !uiState.isAnalyzing,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.metrics?.let {
                        MetricsBreakdownCard(metrics = it)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recording & Selection Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // File picker button
                        IconButton(
                            onClick = { filePickerLauncher.launch("audio/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f)),
                            enabled = !isRecording && !uiState.isAnalyzing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Selecionar Áudio",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Mic Button (Record)
                        FloatingActionButton(
                            onClick = {
                                if (!audioPermission.status.isGranted) {
                                    audioPermission.launchPermissionRequest()
                                } else if (isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    viewModel.startRecording()
                                }
                            },
                            modifier = Modifier.size(80.dp),
                            containerColor = if (isRecording) Secondary else Primary,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Parar" else "Gravar",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Cancel / Clear button
                        IconButton(
                            onClick = {
                                if (isRecording) viewModel.cancelRecording()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f)),
                            enabled = isRecording
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = if (isRecording) Color.White else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isRecording) "Toque no botão central para finalizar" else "Toque para gravar ou escolha um arquivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )

                    // Error text
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legal Disclaimer
                Text(
                    text = "⚠️ Software de entretenimento humorístico. Resultados fictícios sem comprovação de verdade científica.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Calibration Dialog
    if (showCalibrationInfoDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationInfoDialog = false },
            title = {
                Text(
                    "🎙️ Calibração de Voz",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Para calibrar o detector, grave uma amostra de 5 segundos de sua voz falando a seguinte frase modelo:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "\"Eu juro falar a verdade e nada mais que a verdade.\"",
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Isso extrairá sua frequência vocal de base para tornar as análises futuras muito mais precisas!",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCalibrationInfoDialog = false
                        viewModel.startCalibration()
                    }
                ) {
                    Text("Começar Gravação", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalibrationInfoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CalibrationPanel(
    hasCalibration: Boolean,
    isCalibrating: Boolean,
    onCalibrateClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCalibrating) Accent else if (hasCalibration) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasCalibration) Icons.Default.CheckCircle else Icons.Default.MicNone,
                    contentDescription = "Status",
                    tint = if (hasCalibration) Primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (hasCalibration) "Voz Calibrada" else "Voz Não Calibrada",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (hasCalibration) "Usando timbre personalizado" else "Recomendado para análises",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
            Row {
                if (hasCalibration) {
                    TextButton(onClick = onClearClick) {
                        Text("Limpar", color = ErrorColor, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Button(
                    onClick = onCalibrateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasCalibration) Color.White.copy(alpha = 0.1f) else Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (hasCalibration) "Recalibrar" else "Calibrar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricsBreakdownCard(metrics: VsaMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Detalhamento da Análise",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            MetricContributor(
                label = "Micro-Tremor",
                value = "%.1f Hz".format(metrics.microTremor),
                weight = 15f,
                passed = !metrics.microTremorIndicatesStress
            )
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))
            MetricContributor(
                label = "Variação de Pitch",
                value = "%.1f %%".format(metrics.pitchVariation),
                weight = 10f,
                passed = !metrics.pitchIndicatesStress
            )
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))
            MetricContributor(
                label = "Jitter (Instabilidade)",
                value = "%.2f %%".format(metrics.jitter),
                weight = 10f,
                passed = !metrics.jitterIndicatesStress
            )
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))
            MetricContributor(
                label = "Shimmer (Oscilação de Amplitude)",
                value = "%.1f %%".format(metrics.shimmer),
                weight = 7.5f,
                passed = !metrics.shimmerIndicatesStress
            )
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))
            MetricContributor(
                label = "HNR (Clareza Harmônica)",
                value = "%.1f dB".format(metrics.hnr),
                weight = 7.5f,
                passed = !metrics.hnrIndicatesStress
            )
        }
    }
}

@Composable
fun MetricContributor(label: String, value: String, weight: Float, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            Text(value, color = Color.LightGray.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (passed) "+$weight" else "-$weight",
                fontWeight = FontWeight.Bold,
                color = if (passed) Primary else ErrorColor,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "Status",
                tint = if (passed) Primary else ErrorColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WaveEqualizer(amplitude: Float, isRecording: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(30.dp)
    ) {
        val bars = 11
        for (i in 0 until bars) {
            // Animating height based on index and current amplitude
            val delayMs = i * 60
            val infiniteTransition = rememberInfiniteTransition(label = "bar_$i")
            val animatedFactor by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300 + delayMs % 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight_$i"
            )
            
            val barHeight = if (isRecording) {
                (2.dp + (24.dp * (animatedFactor * amplitude)))
            } else {
                2.dp
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isRecording) Secondary else Color.Gray)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return "%02d:%02d".format(minutes, seconds)
}
