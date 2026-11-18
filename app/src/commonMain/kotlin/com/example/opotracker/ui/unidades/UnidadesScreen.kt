package com.example.opotracker.ui.unidades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.opotracker.data.TOTAL_ITEMS_POR_UD
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.data.completadas
import com.example.opotracker.ui.common.CircleCheck

/**
 * Bloques de UI de Unidades Didácticas, pensados para incrustarse dentro de la pantalla
 * de OpoTracker (junto al temario), no como pantalla independiente.
 */
@Composable
fun NombrarUnidadDialog(
    titulo: String,
    valorInicial: String,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var texto by remember { mutableStateOf(valorInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(texto) }, enabled = texto.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
fun UnidadCard(
    unidad: UnidadDidacticaEntity,
    onChange: (UnidadDidacticaEntity) -> Unit,
    onRenombrar: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = unidad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRenombrar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Renombrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.padding(start = 2.dp))
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { unidad.completadas().toFloat() / TOTAL_ITEMS_POR_UD },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.padding(top = 14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                MiniCheck(label = "Hecha", checked = unidad.hecha) { onChange(unidad.copy(hecha = !unidad.hecha)) }
                MiniCheck(label = "Revisada", checked = unidad.revisada) { onChange(unidad.copy(revisada = !unidad.revisada)) }
                MiniCheck(label = "Guión", checked = unidad.guion) { onChange(unidad.copy(guion = !unidad.guion)) }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                MiniCheck(label = "Presentación", checked = unidad.presentacion) { onChange(unidad.copy(presentacion = !unidad.presentacion)) }
                MiniCheck(label = "Lista", checked = unidad.lista) { onChange(unidad.copy(lista = !unidad.lista)) }
            }

            Spacer(modifier = Modifier.padding(top = 14.dp))
            ContadorRow(
                label = "Revisión",
                valor = unidad.revision,
                onDecrementar = { onChange(unidad.copy(revision = (unidad.revision - 1).coerceAtLeast(0))) },
                onIncrementar = { onChange(unidad.copy(revision = unidad.revision + 1)) },
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
            ContadorRow(
                label = "Práctica",
                valor = unidad.practica,
                onDecrementar = { onChange(unidad.copy(practica = (unidad.practica - 1).coerceAtLeast(0))) },
                onIncrementar = { onChange(unidad.copy(practica = unidad.practica + 1)) },
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Añadir simulacro",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CircleCheck(
                    checked = unidad.enSimulacro,
                    onToggle = { onChange(unidad.copy(enSimulacro = !unidad.enSimulacro)) },
                )
            }
        }
    }
}

@Composable
private fun ContadorRow(label: String, valor: Int, onDecrementar: () -> Unit, onIncrementar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDecrementar,
                enabled = valor > 0,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) { Text("−1") }
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(min = 20.dp),
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onIncrementar,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) { Text("+1") }
        }
    }
}

@Composable
private fun MiniCheck(label: String, checked: Boolean, onToggle: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        CircleCheck(checked = checked, onToggle = onToggle, size = 20.dp)
    }
}
