package com.example.opotracker.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

private const val URL_RELEASES = "https://github.com/PacoMorales03/OpoTracker/releases/latest"

/**
 * A diferencia de Android (que descarga e instala el APK solo), en desktop solo avisamos y
 * abrimos la página de releases en el navegador: instalar un .exe por encima del que está en
 * marcha es mucho más delicado, así que se deja en manos del usuario.
 */
@Composable
fun ActualizacionDesktopBanner() {
    var visible by remember { mutableStateOf(true) }
    if (!visible) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Comprueba si hay una versión más reciente de OpoTracker en GitHub",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                runCatching { Desktop.getDesktop().browse(URI(URL_RELEASES)) }
            }) {
                Text("Ver releases")
            }
            TextButton(onClick = { visible = false }) {
                Text("Ocultar")
            }
        }
    }
}
