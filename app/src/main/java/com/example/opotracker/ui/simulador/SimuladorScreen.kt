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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.data.TOTAL_TEMAS
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
            SimFase.INACTIVO -> EstadoInactivo(
                onIniciar = viewModel::iniciarSimulacion,
                onSeleccionarManual = viewModel::seleccionarTemaManual,
            )
            SimFase.SELECCIONANDO -> EstadoSeleccion(
                opciones = viewModel.opciones,
                onSeleccionar = viewModel::seleccionarTema,
                onCancelar = viewModel::cancelarSeleccion,
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
private fun EstadoInactivo(onIniciar: () -> Unit, onSeleccionarManual: (Int) -> Unit) {
    var mostrarSelectorManual by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Pulsa para sortear 3 bolas,\nselecciona 1 para iniciar el simulacro",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Button(onClick = onIniciar, modifier = Modifier.width(280.dp)) {
            Text("Iniciar simulación")
        }
        Spacer(modifier = Modifier.padding(top = 12.dp))
        OutlinedButton(onClick = { mostrarSelectorManual = true }, modifier = Modifier.width(280.dp)) {
            Text("Elegir tema manualmente")
        }
    }

    if (mostrarSelectorManual) {
        SelectorManualDialog(
            onSeleccionar = { tema ->
                mostrarSelectorManual = false
                onSeleccionarManual(tema)
            },
            onDismiss = { mostrarSelectorManual = false },
        )
    }
}

@Composable
private fun SelectorManualDialog(onSeleccionar: (Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Elige un tema",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.heightIn(max = 360.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items((1..TOTAL_TEMAS).toList(), key = { it }) { numero ->
                        TemaNumeroCirculo(numero = numero, onClick = { onSeleccionar(numero) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TemaNumeroCirculo(numero: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun EstadoSeleccion(opciones: List<Int>, onSeleccionar: (Int) -> Unit, onCancelar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        Spacer(modifier = Modifier.padding(top = 24.dp))
        TextButton(onClick = onCancelar) {
            Text("Cancelar")
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
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
            if (viewModel.fase != SimFase.FINALIZANDO) {
                Spacer(modifier = Modifier.padding(top = 16.dp))
                TextButton(onClick = viewModel::cancelarEnCurso) {
                    Text("Cancelar")
                }
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
    var editando by remember { mutableStateOf<SimulacionEntity?>(null) }
    var borrando by remember { mutableStateOf<SimulacionEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        agrupado.forEach { (temaNumero, entradas) ->
            item(key = "header_$temaNumero") {
                Text(
                    text = "Tema $temaNumero · ${entradas.size} simulacro${if (entradas.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
            }
            items(entradas, key = { it.id }) { entrada ->
                EntradaCard(
                    entrada = entrada,
                    onEditar = { editando = entrada },
                    onEliminar = { borrando = entrada },
                )
            }
        }
    }

    editando?.let { entrada ->
        EditarSimulacionDialog(
            entrada = entrada,
            onDismiss = { editando = null },
            onGuardar = { sensacion, observaciones ->
                viewModel.editarSimulacion(entrada, sensacion, observaciones)
                editando = null
            },
        )
    }

    borrando?.let { entrada ->
        AlertDialog(
            onDismissRequest = { borrando = null },
            title = { Text("¿Eliminar simulacro?") },
            text = { Text("Se borrará este simulacro del tema ${entrada.temaNumero}. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarSimulacion(entrada)
                    borrando = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { borrando = null }) { Text("Cancelar") }
            },
        )
    }
}

private const val LINEAS_OBSERVACIONES_COLAPSADO = 2

@Composable
private fun EntradaCard(entrada: SimulacionEntity, onEditar: () -> Unit, onEliminar: () -> Unit) {
    var expandido by remember(entrada.id) { mutableStateOf(false) }
    var truncable by remember(entrada.id) { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Text(
                    text = entrada.observaciones,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expandido) Int.MAX_VALUE else LINEAS_OBSERVACIONES_COLAPSADO,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { if (it.hasVisualOverflow) truncable = true },
                )
                if (truncable) {
                    Text(
                        text = if (expandido) "Ver menos" else "Ver más",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { expandido = !expandido },
                    )
                }
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.padding(start = 4.dp))
                IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditarSimulacionDialog(
    entrada: SimulacionEntity,
    onDismiss: () -> Unit,
    onGuardar: (sensacion: Float, observaciones: String) -> Unit,
) {
    var sensacion by remember { mutableFloatStateOf(entrada.sensacion) }
    var observaciones by remember { mutableStateOf(entrada.observaciones) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar simulacro") },
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
