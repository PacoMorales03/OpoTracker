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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.data.TOTAL_TEMAS
import com.example.opotracker.ui.common.StarRatingDisplay
import com.example.opotracker.ui.common.StarRatingInput
import com.example.opotracker.ui.common.TiempoInput
import com.example.opotracker.ui.common.duracionATexto
import com.example.opotracker.ui.common.formatDuration
import com.example.opotracker.ui.common.formatFecha
import com.example.opotracker.ui.common.textoADuracion
import kotlinx.coroutines.launch

private enum class ModoSimulador { TEMAS, UNIDADES }

@Composable
fun SimuladorScreen(
    viewModel: SimuladorViewModel = viewModel(),
    unidadViewModel: UnidadSimuladorViewModel = viewModel(),
) {
    var modo by rememberSaveable { mutableStateOf(ModoSimulador.TEMAS) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onMensaje: (String) -> Unit = { mensaje -> scope.launch { snackbarHostState.showSnackbar(mensaje) } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "¿Qué quieres simular?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModoBoton(
                        texto = "Temas",
                        seleccionado = modo == ModoSimulador.TEMAS,
                        onClick = { modo = ModoSimulador.TEMAS },
                        modifier = Modifier.weight(1f),
                    )
                    ModoBoton(
                        texto = "Unidades",
                        seleccionado = modo == ModoSimulador.UNIDADES,
                        onClick = { modo = ModoSimulador.UNIDADES },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Simulador") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Historial") })
            }
            when (modo) {
                ModoSimulador.TEMAS -> when (tab) {
                    0 -> SimuladorContent(viewModel = viewModel, onMensaje = onMensaje)
                    else -> HistorialContent(viewModel)
                }
                ModoSimulador.UNIDADES -> when (tab) {
                    0 -> SimuladorUdContent(viewModel = unidadViewModel, onMensaje = onMensaje)
                    else -> HistorialUdContent(unidadViewModel)
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ModoBoton(texto: String, seleccionado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (seleccionado) {
        Button(onClick = onClick, modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(texto) }
    }
}

@Composable
private fun SimuladorContent(viewModel: SimuladorViewModel, onMensaje: (String) -> Unit) {
    val temasMarcados by viewModel.temasMarcados.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (viewModel.fase) {
            SimFase.INACTIVO -> EstadoInactivo(
                temasMarcados = temasMarcados,
                onIniciar = viewModel::iniciarSimulacion,
                onSeleccionarManual = viewModel::seleccionarTemaManual,
                onMensaje = onMensaje,
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
private fun EstadoInactivo(
    temasMarcados: List<Int>,
    onIniciar: () -> Unit,
    onSeleccionarManual: (Int) -> Unit,
    onMensaje: (String) -> Unit,
) {
    var mostrarSelectorManual by remember { mutableStateOf(false) }
    var mostrarTemasMarcados by remember { mutableStateOf(false) }
    val haySeleccion = temasMarcados.isNotEmpty()

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Pulsa para sortear hasta 2 temas entre los temas marcados:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        TextButton(onClick = { mostrarTemasMarcados = true }) {
            Text("Ver temas marcados")
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Button(
            onClick = {
                if (haySeleccion) {
                    onIniciar()
                } else {
                    onMensaje("Añade algún tema al simulador en el apartado de Temario")
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
            text = "Selecciona un tema en específico para realizar una simulación:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

    if (mostrarTemasMarcados) {
        TemasMarcadosDialog(
            temasMarcados = temasMarcados,
            onDismiss = { mostrarTemasMarcados = false },
        )
    }
}

@Composable
private fun TemasMarcadosDialog(temasMarcados: List<Int>, onDismiss: () -> Unit) {
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
                        text = "Temas marcados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
                if (temasMarcados.isEmpty()) {
                    Text(
                        text = "Todavía no has marcado ningún tema. Ve al apartado de Temario y activa \"Añadir simulacro\" en los que quieras incluir en el sorteo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.heightIn(max = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(temasMarcados, key = { it }) { numero ->
                            TemaNumeroCirculo(numero = numero, onClick = {})
                        }
                    }
                }
            }
        }
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
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Elige el tema que vas a desarrollar",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
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
    var editando by remember { mutableStateOf<SimulacionEntity?>(null) }
    var borrando by remember { mutableStateOf<SimulacionEntity?>(null) }
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
            val agrupado = historial.groupBy { it.temaNumero }.toSortedMap()
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
        SimulacionFormDialog(
            entradaExistente = null,
            onDismiss = { mostrarAnadir = false },
            onGuardar = { tema, duracion, sensacion, observaciones ->
                viewModel.crearSimulacionManual(tema, duracion, sensacion, observaciones)
                mostrarAnadir = false
            },
        )
    }

    editando?.let { entrada ->
        SimulacionFormDialog(
            entradaExistente = entrada,
            onDismiss = { editando = null },
            onGuardar = { _, duracion, sensacion, observaciones ->
                viewModel.editarSimulacion(entrada, duracion, sensacion, observaciones)
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

/**
 * Formulario de simulacro, compartido entre "añadir" (sin cronómetro, con el tiempo puesto a
 * mano) y "editar" (todos los campos, incluido el tiempo, son editables). Si [entradaExistente]
 * es null se muestra un selector de tema; si no, el tema queda fijo.
 */
@Composable
private fun SimulacionFormDialog(
    entradaExistente: SimulacionEntity?,
    onDismiss: () -> Unit,
    onGuardar: (temaNumero: Int, duracionSegundos: Long, sensacion: Float, observaciones: String) -> Unit,
) {
    var temaSeleccionado by remember { mutableStateOf(entradaExistente?.temaNumero) }
    var mostrarSelectorTema by remember { mutableStateOf(false) }
    val (minutosIniciales, segundosIniciales) = duracionATexto(entradaExistente?.duracionSegundos ?: 0L)
    var minutos by remember { mutableStateOf(minutosIniciales) }
    var segundos by remember { mutableStateOf(segundosIniciales) }
    var sensacion by remember { mutableFloatStateOf(entradaExistente?.sensacion ?: 0f) }
    var observaciones by remember { mutableStateOf(entradaExistente?.observaciones ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entradaExistente == null) "Añadir simulacro" else "Editar simulacro") },
        text = {
            Column {
                if (entradaExistente == null) {
                    Text("Tema", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                    OutlinedButton(onClick = { mostrarSelectorTema = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(temaSeleccionado?.let { "Tema $it" } ?: "Elegir tema")
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
                onClick = {
                    temaSeleccionado?.let { tema ->
                        onGuardar(tema, textoADuracion(minutos, segundos), sensacion, observaciones)
                    }
                },
                enabled = temaSeleccionado != null && sensacion > 0f,
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )

    if (mostrarSelectorTema) {
        SelectorManualDialog(
            onSeleccionar = { tema ->
                temaSeleccionado = tema
                mostrarSelectorTema = false
            },
            onDismiss = { mostrarSelectorTema = false },
        )
    }
}
