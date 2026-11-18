package com.example.opotracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Pareja (minutos, segundos) como texto, lista para un [TiempoInput]. */
fun duracionATexto(segundosTotales: Long): Pair<String, String> {
    val minutos = segundosTotales / 60
    val segundos = segundosTotales % 60
    return minutos.toString() to segundos.toString()
}

fun textoADuracion(minutos: String, segundos: String): Long {
    val m = minutos.toLongOrNull()?.coerceAtLeast(0) ?: 0L
    val s = (segundos.toLongOrNull() ?: 0L).coerceIn(0, 59)
    return m * 60 + s
}

@Composable
fun TiempoInput(
    minutos: String,
    segundos: String,
    onMinutosChange: (String) -> Unit,
    onSegundosChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = minutos,
            onValueChange = { texto -> if (texto.length <= 3 && texto.all { it.isDigit() }) onMinutosChange(texto) },
            label = { Text("Minutos") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = segundos,
            onValueChange = { texto -> if (texto.length <= 2 && texto.all { it.isDigit() }) onSegundosChange(texto) },
            label = { Text("Segundos") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
    }
}
