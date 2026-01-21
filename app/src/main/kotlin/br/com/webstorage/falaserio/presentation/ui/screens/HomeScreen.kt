package br.com.webstorage.falaserio.presentation.ui.screens

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.webstorage.falaserio.presentation.ui.theme.FalaSerioTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.webstorage.falaserio.presentation.ui.theme.Accent
import br.com.webstorage.falaserio.presentation.ui.theme.ErrorColor
import br.com.webstorage.falaserio.presentation.ui.theme.Primary
import br.com.webstorage.falaserio.presentation.ui.theme.Secondary
import br.com.webstorage.falaserio.presentation.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToCredits: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    // Estados do ViewModel
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDuration.collectAsStateWithLifecycle()
    val amplitude by viewModel.currentAmplitude.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()

    // Permissão de áudio
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Animações
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = tween(300),
        label = "buttonScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) Secondary else Primary,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎤 Fala Sério",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Créditos
                    TextButton(onClick = onNavigateToCredits) {
                        Text(
                            text = if (credits == Int.MAX_VALUE) "∞" else "$credits",
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Créditos",
                            tint = Accent
                        )
                    }

                    // Histórico
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Histórico"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Título e subtítulo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Revelador da Verdade",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pressione para gravar e analisar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Visualização de amplitude / Resultado
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                buttonColor.copy(alpha = amplitude * 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isAnalyzing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = Accent
                        )
                    }

                    uiState.metrics != null -> {
                        // Mostrar resultado
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.metrics!!.overallStressScore.toInt()}%",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    uiState.metrics!!.overallStressScore >= 70 -> Secondary
                                    uiState.metrics!!.overallStressScore >= 40 -> Accent
                                    else -> Primary
                                }
                            )
                            Text(
                                text = uiState.metrics!!.getStressLevel(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    isRecording -> {
                        // Mostrar duração
                        Text(
                            text = formatDuration(recordingDuration),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    }

                    else -> {
                        Text(
                            text = "PRONTO",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // Botão de gravação
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    modifier = Modifier
                        .size(80.dp)
                        .scale(buttonScale),
                    containerColor = buttonColor,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Parar" else "Gravar",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRecording) "Toque para parar" else "Toque para gravar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Erro
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Aviso legal
            Text(
                text = "⚠️ Apenas para entretenimento. Sem validade científica.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return "%02d:%02d".format(minutes, seconds)
}

// ========== PREVIEWS ==========

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    FalaSerioTheme {
        HomeScreenContent(
            isRecording = false,
            recordingDuration = 0L,
            amplitude = 0f,
            isAnalyzing = false,
            stressScore = null,
            stressLevel = null,
            error = null,
            credits = 5,
            onRecordClick = {},
            onNavigateToHistory = {},
            onNavigateToCredits = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenRecordingPreview() {
    FalaSerioTheme {
        HomeScreenContent(
            isRecording = true,
            recordingDuration = 5000L,
            amplitude = 0.7f,
            isAnalyzing = false,
            stressScore = null,
            stressLevel = null,
            error = null,
            credits = 5,
            onRecordClick = {},
            onNavigateToHistory = {},
            onNavigateToCredits = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenResultPreview() {
    FalaSerioTheme {
        HomeScreenContent(
            isRecording = false,
            recordingDuration = 0L,
            amplitude = 0f,
            isAnalyzing = false,
            stressScore = 72f,
            stressLevel = "STRESS ALTO",
            error = null,
            credits = 4,
            onRecordClick = {},
            onNavigateToHistory = {},
            onNavigateToCredits = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    isRecording: Boolean,
    recordingDuration: Long,
    amplitude: Float,
    isAnalyzing: Boolean,
    stressScore: Float?,
    stressLevel: String?,
    error: String?,
    credits: Int,
    onRecordClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCredits: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = tween(300),
        label = "buttonScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) Secondary else Primary,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fala Serio",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onNavigateToCredits) {
                        Text(
                            text = if (credits == Int.MAX_VALUE) "infinito" else "$credits",
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Creditos",
                            tint = Accent
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Historico"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Revelador da Verdade",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pressione para gravar e analisar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                buttonColor.copy(alpha = amplitude * 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isAnalyzing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = Accent
                        )
                    }
                    stressScore != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stressScore.toInt()}%",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    stressScore >= 70 -> Secondary
                                    stressScore >= 40 -> Accent
                                    else -> Primary
                                }
                            )
                            Text(
                                text = stressLevel ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    isRecording -> {
                        Text(
                            text = formatDuration(recordingDuration),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    }
                    else -> {
                        Text(
                            text = "PRONTO",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FloatingActionButton(
                    onClick = onRecordClick,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(buttonScale),
                    containerColor = buttonColor,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Parar" else "Gravar",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isRecording) "Toque para parar" else "Toque para gravar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                error?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = "Apenas para entretenimento. Sem validade cientifica.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
