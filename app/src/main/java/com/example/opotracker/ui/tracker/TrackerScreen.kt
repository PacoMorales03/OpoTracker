package com.example.opotracker.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.TOTAL_ITEMS_POR_TEMA
import com.example.opotracker.data.TemaEntity
import com.example.opotracker.data.completados
import com.example.opotracker.ui.common.CircleCheck

@Composable
fun TrackerScreen(viewModel: TrackerViewModel = viewModel()) {
    val temas by viewModel.temas.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(temas, key = { it.numero }) { tema ->
            TemaCard(tema = tema, onChange = viewModel::onTemaChanged)
        }
    }
}

@Composable
private fun TemaCard(tema: TemaEntity, onChange: (TemaEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tema ${tema.numero}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { tema.completados().toFloat() / TOTAL_ITEMS_POR_TEMA },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.padding(top = 14.dp))
            CheckRow(
                label = "Lecturas",
                content = {
                    CircleCheck(checked = tema.leido1, onToggle = { onChange(tema.copy(leido1 = !tema.leido1)) })
                    CircleCheck(checked = tema.leido2, onToggle = { onChange(tema.copy(leido2 = !tema.leido2)) })
                    CircleCheck(checked = tema.leido3, onToggle = { onChange(tema.copy(leido3 = !tema.leido3)) })
                },
            )

            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MiniCheck(label = "Resumen", checked = tema.resumen) { onChange(tema.copy(resumen = !tema.resumen)) }
                MiniCheck(label = "Personalización", checked = tema.personalizacion) { onChange(tema.copy(personalizacion = !tema.personalizacion)) }
                MiniCheck(label = "Estudio", checked = tema.estudio) { onChange(tema.copy(estudio = !tema.estudio)) }
            }

            Spacer(modifier = Modifier.padding(top = 14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Repasos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onChange(tema.copy(repasos = (tema.repasos - 1).coerceAtLeast(0))) },
                        enabled = tema.repasos > 0,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) { Text("−1") }
                    Text(
                        text = tema.repasos.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.widthIn(min = 20.dp),
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { onChange(tema.copy(repasos = tema.repasos + 1)) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) { Text("+1") }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.wrapContentWidth(),
        )
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

@Composable
private fun MiniCheck(label: String, checked: Boolean, onToggle: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp),
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        CircleCheck(checked = checked, onToggle = onToggle, size = 20.dp)
    }
}
