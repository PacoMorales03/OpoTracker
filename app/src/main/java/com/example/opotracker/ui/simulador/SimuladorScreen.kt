package com.example.opotracker.ui.simulador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.ui.common.StarRatingDisplay
import com.example.opotracker.ui.common.StarRatingInput
import com.example.opotracker.ui.common.formatDuration
import com.example.opotracker.ui.common.formatFecha

@Composable
fun SimuladorScreen(viewModel: SimuladorViewModel = viewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Simulador") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Historial") })
        }
        when (tab) {
            0 -> SimuladorContent(viewModel)
            else -> HistorialContent(viewModel)
        }
    }
}

@Composable
private fun SimuladorContent(viewModel: SimuladorViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (viewModel.fase) {
            SimFase.INACTIVO -> EstadoInactivo(onIniciar = viewModel::iniciarSimulacion)
            SimFase.SELECCIONANDO -> EstadoSeleccion(
                opciones = viewModel.opciones,
                onSeleccionar = viewModel::seleccionarTema,
            )
            SimFase.FINALIZANDO -> {
                EstadoEnCurso(viewModel)
                FinalizarDialog(
                    onDismiss = viewModel::cancelarFinalizacion,
                    onGuardar = viewModel::guardarResultado,
                )
            }
            else -> EstadoEnCurso(viewModel)
        }
    }
}

@Composable
private fun EstadoInactivo(onIniciar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🎲", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.padding(top = 12.dp))
        Text(
            text = "Pulsa para sortear 3 bolas,\ncomo el día del examen",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Button(onClick = onIniciar) {
            Text("Iniciar simulación")
        }
    }
}

@Composable
private fun EstadoSeleccion(opciones: List<Int>, onSeleccionar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🎱", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.padding(top = 12.dp))
        Text(
            text = "Elige el tema que vas a desarrollar",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            opciones.forEach { tema ->
                BolaOpcion(numero = tema, onClick = { onSeleccionar(tema) })
            }
        }
    }
}

@Composable
private fun BolaOpcion(numero: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun EstadoEnCurso(viewModel: SimuladorViewModel) {
    val tema = viewModel.temaActual ?: return
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Tema $tema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = formatDuration(viewModel.elapsedSeconds),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.padding(top = 24.dp))
            when (viewModel.fase) {
                SimFase.LISTO -> Button(onClick = viewModel::iniciarCronometro) { Text("Iniciar") }
                SimFase.CORRIENDO -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = viewModel::pausar) { Text("Pausar") }
                    Button(onClick = viewModel::finalizar) { Text("Finalizar") }
                }
                SimFase.PAUSADO -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = viewModel::iniciarCronometro) { Text("Reanudar") }
                    Button(onClick = viewModel::finalizar) { Text("Finalizar") }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun FinalizarDialog(
    onDismiss: () -> Unit,
    onGuardar: (sensacion: Float, observaciones: String) -> Unit,
) {
    var sensacion by remember { mutableFloatStateOf(0f) }
    var observaciones by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cómo ha ido?") },
        text = {
            Column {
                Text("Sensación", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.padding(top = 6.dp))
                StarRatingInput(rating = sensacion, onRatingChange = { sensacion = it })
                Spacer(modifier = Modifier.padding(top = 16.dp))
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(sensacion, observaciones) },
                enabled = sensacion > 0f,
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun HistorialContent(viewModel: SimuladorViewModel) {
    val historial by viewModel.historial.collectAsState()

    if (historial.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Todavía no hay simulacros guardados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val agrupado = historial.groupBy { it.temaNumero }.toSortedMap()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(agrupado.entries.toList(), key = { it.key }) { (temaNumero, entradas) ->
            GrupoTemaCard(temaNumero = temaNumero, entradas = entradas)
        }
    }
}

@Composable
private fun GrupoTemaCard(temaNumero: Int, entradas: List<SimulacionEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tema $temaNumero · ${entradas.size} simulacro${if (entradas.size == 1) "" else "s"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            entradas.forEach { entrada ->
                Spacer(modifier = Modifier.padding(top = 10.dp))
                EntradaHistorial(entrada)
            }
        }
    }
}

@Composable
private fun EntradaHistorial(entrada: SimulacionEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatFecha(entrada.fechaMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRatingDisplay(rating = entrada.sensacion)
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = formatDuration(entrada.duracionSegundos),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (entrada.observaciones.isNotBlank()) {
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = entrada.observaciones,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
