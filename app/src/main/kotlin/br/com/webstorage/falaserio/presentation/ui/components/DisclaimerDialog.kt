package br.com.webstorage.falaserio.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Dialog de aviso legal exibido na primeira execução do app.
 * 
 * Informa ao usuário que o app é apenas para entretenimento
 * e não tem validade científica ou legal.
 */
@Composable
fun DisclaimerDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Aviso",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "⚠️ Aviso Importante",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Este aplicativo é APENAS para entretenimento.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Os resultados da análise de voz NÃO possuem:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BulletPoint("Validade científica comprovada")
                    BulletPoint("Validade legal ou jurídica")
                    BulletPoint("Precisão para detectar mentiras")
                    BulletPoint("Uso profissional recomendado")
                }
                
                Text(
                    text = "Use apenas para diversão com amigos e família!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendi e Concordo")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
