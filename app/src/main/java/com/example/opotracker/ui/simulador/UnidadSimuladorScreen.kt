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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.opotracker.data.SimulacroUdEntity
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.ui.common.StarRatingDisplay
import com.example.opotracker.ui.common.StarRatingInput
import com.example.opotracker.ui.common.TiempoInput
import com.example.opotracker.ui.common.duracionATexto
import com.example.opotracker.ui.common.formatDuration
import com.example.opotracker.ui.common.formatFecha
import com.example.opotracker.ui.common.textoADuracion

@Composable
fun SimuladorUdContent(viewModel: UnidadSimuladorViewModel, onMensaje: (String) -> Unit) {
    val unidades by viewModel.unidades.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (viewModel.fase) {
            SimFase.INACTIVO -> EstadoInactivoUd(
                unidades = unidades,
                onIniciar = viewModel::iniciarSimulacion,
                onSeleccionarManual = viewModel::seleccionarUnidadManual,
                onMensaje = onMensaje,
            )
            SimFase.SELECCIONANDO -> EstadoSeleccionUd(
                opciones = viewModel.opciones,
                nombreDe = viewModel::nombreDe,
                onSeleccionar = viewModel::seleccionarUnidad,
                onCancelar = viewModel::cancelarSeleccion,
            )
            SimFase.FINALIZANDO -> {
                EstadoEnCursoUd(viewModel)
                FinalizarSimulacroUdDialog(
                    onDismiss = viewModel::cancelarFinalizacion,
                    onGuardar = viewModel::guardarResultado,
                )
            }
            else -> EstadoEnCursoUd(viewModel)
        }
    }
}

@Composable
private fun EstadoInactivoUd(
    unidades: List<UnidadDidacticaEntity>,
    onIniciar: () -> Unit,
    onSeleccionarManual: (Long) -> Unit,
    onMensaje: (String) -> Unit,
) {
    var mostrarSelectorManual by remember { mutableStateOf(false) }
    var mostrarMarcadas by remember { mutableStateOf(false) }
    val marcadas = unidades.filter { it.enSimulacro }
    val haySeleccion = marcadas.isNotEmpty()

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Pulsa para sortear hasta 3 unidades didácticas entre las marcadas:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        TextButton(onClick = { mostrarMarcadas = true }) {
            Text("Ver unidades didácticas marcadas")
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Button(
            onClick = {
                if (haySeleccion) {
                    onIniciar()
                } else {
                    onMensaje("Añade alguna unidad didáctica al simulador en el apartado de Unidades")
                }
            },
            modifier = Modifier.width(280.dp),
            colors = if (haySeleccion) {
                ButtonDefaults.buttonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        ) {
            Text("Iniciar simulación")
        }

        Spacer(modifier = Modifier.padding(top = 32.dp))
        Text(
            text = "Selecciona una unidad didáctica en específico para realizar una simulación:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 12.dp))
        OutlinedButton(
            onClick = { mostrarSelectorManual = true },
            modifier = Modifier.width(280.dp),
            enabled = unidades.isNotEmpty(),
        ) {
            Text("Elegir unidad didáctica manualmente")
        }
    }

    if (mostrarSelectorManual) {
        SelectorManualUdDialog(
            unidades = unidades,
            onSeleccionar = { id ->
                mostrarSelectorManual = false
                onSeleccionarManual(id)
            },
            onDismiss = { mostrarSelectorManual = false },
        )
    }

    if (mostrarMarcadas) {
        UnidadesMarcadasDialog(marcadas = marcadas, onDismiss = { mostrarMarcadas = false })
    }
}

@Composable
private fun UnidadesMarcadasDialog(marcadas: List<UnidadDidacticaEntity>, onDismiss: () -> Unit) {
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
                        text = "Unidades didácticas marcadas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
                if (marcadas.isEmpty()) {
                    Text(
                        text = "Todavía no has marcado ninguna unidad didáctica. Ve al apartado de Unidades y activa \"Añadir simulacro\" en las que quieras incluir en el sorteo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(marcadas, key = { it.id }) { unidad ->
                            Text(
                                text = "• ${unidad.nombre}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorManualUdDialog(
    unidades: List<UnidadDidacticaEntity>,
    onSeleccionar: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
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
                        text = "Elige una unidad didáctica",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
                if (unidades.isEmpty()) {
                    Text(
                        text = "Todavía no has añadido ninguna unidad didáctica.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(unidades, key = { it.id }) { unidad ->
                            Text(
                                text = unidad.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeleccionar(unidad.id) }
                                    .padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoSeleccionUd(
    opciones: List<Long>,
    nombreDe: (Long) -> String,
    onSeleccionar: (Long) -> Unit,
    onCancelar: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Elige la unidad didáctica que vas a desarrollar",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            opciones.forEach { id ->
                OpcionUnidad(nombre = nombreDe(id), onClick = { onSeleccionar(id) })
            }
        }
        Spacer(modifier = Modifier.padding(top = 24.dp))
        TextButton(onClick = onCancelar) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun OpcionUnidad(nombre: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = nombre.trim().take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.padding(top = 6.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EstadoEnCursoUd(viewModel: UnidadSimuladorViewModel) {
    val id = viewModel.unidadActualId ?: return
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
                text = viewModel.nombreDe(id),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
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
private fun FinalizarSimulacroUdDialog(
    onDismiss: () -> Unit,
    onGuardar: (sensacion: Float, comentarios: String) -> Unit,
) {
    var sensacion by remember { mutableFloatStateOf(0f) }
    var comentarios by remember { mutableStateOf("") }

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
                    value = comentarios,
                    onValueChange = { comentarios = it },
                    label = { Text("Comentarios") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(sensacion, comentarios) },
                enabled = sensacion > 0f,
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
fun HistorialUdContent(viewModel: UnidadSimuladorViewModel) {
    val historial by viewModel.historial.collectAsState()
    val unidades by viewModel.unidades.collectAsState()
    var editando by remember { mutableStateOf<SimulacroUdEntity?>(null) }
    var borrando by remember { mutableStateOf<SimulacroUdEntity?>(null) }
    var mostrarAnadir by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (historial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Todavía no hay simulacros guardados.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val agrupado = historial.groupBy { it.unidadId to it.unidadNombre }
                .toSortedMap(compareBy { it.second })
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                agrupado.forEach { (clave, entradas) ->
                    item(key = "header_${clave.first}") {
                        Text(
                            text = "${clave.second} · ${entradas.size} simulacro${if (entradas.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                        )
                    }
                    items(entradas, key = { it.id }) { entrada ->
                        EntradaCardUd(
                            entrada = entrada,
                            onEditar = { editando = entrada },
                            onEliminar = { borrando = entrada },
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { mostrarAnadir = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Añadir simulacro")
        }
    }

    if (mostrarAnadir) {
        SimulacroUdFormDialog(
            entradaExistente = null,
            unidades = unidades,
            onDismiss = { mostrarAnadir = false },
            onGuardar = { unidadId, duracion, sensacion, comentarios ->
                viewModel.crearSimulacroManual(unidadId, duracion, sensacion, comentarios)
                mostrarAnadir = false
            },
        )
    }

    editando?.let { entrada ->
        SimulacroUdFormDialog(
            entradaExistente = entrada,
            unidades = unidades,
            onDismiss = { editando = null },
            onGuardar = { _, duracion, sensacion, comentarios ->
                viewModel.editarSimulacro(entrada, duracion, sensacion, comentarios)
                editando = null
            },
        )
    }

    borrando?.let { entrada ->
        AlertDialog(
            onDismissRequest = { borrando = null },
            title = { Text("¿Eliminar simulacro?") },
            text = { Text("Se borrará este simulacro de \"${entrada.unidadNombre}\". No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarSimulacro(entrada)
                    borrando = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { borrando = null }) { Text("Cancelar") }
            },
        )
    }
}

private const val LINEAS_COMENTARIOS_COLAPSADO = 2

@Composable
private fun EntradaCardUd(entrada: SimulacroUdEntity, onEditar: () -> Unit, onEliminar: () -> Unit) {
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
            if (entrada.comentarios.isNotBlank()) {
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Text(
                    text = entrada.comentarios,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expandido) Int.MAX_VALUE else LINEAS_COMENTARIOS_COLAPSADO,
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

/**
 * Formulario de simulacro de Unidad Didáctica, compartido entre "añadir" (sin cronómetro, con el
 * tiempo puesto a mano) y "editar" (todos los campos, incluido el tiempo, son editables). Si
 * [entradaExistente] es null se muestra un selector de unidad; si no, la unidad queda fija.
 */
@Composable
private fun SimulacroUdFormDialog(
    entradaExistente: SimulacroUdEntity?,
    unidades: List<UnidadDidacticaEntity>,
    onDismiss: () -> Unit,
    onGuardar: (unidadId: Long, duracionSegundos: Long, sensacion: Float, comentarios: String) -> Unit,
) {
    var unidadSeleccionada by remember {
        mutableStateOf(entradaExistente?.let { it.unidadId to it.unidadNombre })
    }
    var mostrarSelectorUnidad by remember { mutableStateOf(false) }
    val (minutosIniciales, segundosIniciales) = duracionATexto(entradaExistente?.duracionSegundos ?: 0L)
    var minutos by remember { mutableStateOf(minutosIniciales) }
    var segundos by remember { mutableStateOf(segundosIniciales) }
    var sensacion by remember { mutableFloatStateOf(entradaExistente?.sensacion ?: 0f) }
    var comentarios by remember { mutableStateOf(entradaExistente?.comentarios ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entradaExistente == null) "Añadir simulacro" else "Editar simulacro") },
        text = {
            Column {
                if (entradaExistente == null) {
                    Text("Unidad didáctica", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                    OutlinedButton(
                        onClick = { mostrarSelectorUnidad = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = unidades.isNotEmpty(),
                    ) {
                        Text(unidadSeleccionada?.second ?: "Elegir unidad didáctica")
                    }
                    Spacer(modifier = Modifier.padding(top = 16.dp))
                }
                Text("Tiempo", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.padding(top = 6.dp))
                TiempoInput(
                    minutos = minutos,
                    segundos = segundos,
                    onMinutosChange = { minutos = it },
                    onSegundosChange = { segundos = it },
                )
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Text("Sensación", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.padding(top = 6.dp))
                StarRatingInput(rating = sensacion, onRatingChange = { sensacion = it })
                Spacer(modifier = Modifier.padding(top = 16.dp))
                OutlinedTextField(
                    value = comentarios,
                    onValueChange = { comentarios = it },
                    label = { Text("Comentarios") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    unidadSeleccionada?.let { (id, _) ->
                        onGuardar(id, textoADuracion(minutos, segundos), sensacion, comentarios)
                    }
                },
                enabled = unidadSeleccionada != null && sensacion > 0f,
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )

    if (mostrarSelectorUnidad) {
        SelectorManualUdDialog(
            unidades = unidades,
            onSeleccionar = { id ->
                unidadSeleccionada = id to (unidades.find { it.id == id }?.nombre ?: "?")
                mostrarSelectorUnidad = false
            },
            onDismiss = { mostrarSelectorUnidad = false },
        )
    }
}
