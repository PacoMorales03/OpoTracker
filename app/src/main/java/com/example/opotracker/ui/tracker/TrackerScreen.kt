package com.example.opotracker.ui.tracker

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.TOTAL_ITEMS_POR_TEMA
import com.example.opotracker.data.TemaEntity
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.data.completados
import com.example.opotracker.ui.common.CircleCheck
import com.example.opotracker.ui.unidades.NombrarUnidadDialog
import com.example.opotracker.ui.unidades.UnidadCard
import kotlin.math.roundToInt

private enum class ModoOpoTracker { TEMARIO, UNIDADES }

@Composable
fun TrackerScreen(viewModel: TrackerViewModel = viewModel()) {
    val temas by viewModel.temas.collectAsState()
    val simulaciones by viewModel.simulaciones.collectAsState()
    val unidades by viewModel.unidades.collectAsState()
    val simulacrosUd by viewModel.simulacrosUd.collectAsState()
    val contexto = ContextoInsignias(
        temas = temas,
        simulaciones = simulaciones,
        unidades = unidades,
        simulacrosUd = simulacrosUd,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    var modo by rememberSaveable { mutableStateOf(ModoOpoTracker.TEMARIO) }
    var mostrarCrearUnidad by remember { mutableStateOf(false) }
    var renombrandoUnidad by remember { mutableStateOf<UnidadDidacticaEntity?>(null) }
    var borrandoUnidad by remember { mutableStateOf<UnidadDidacticaEntity?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.insigniaGanada.collect { titulo ->
            snackbarHostState.showSnackbar("¡Insignia conseguida: $titulo!")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ProgresoGeneralCard(temas = temas, contexto = contexto)
            }
            item {
                ModoOpoTrackerToggle(modo = modo, onModoChange = { modo = it })
            }
            when (modo) {
                ModoOpoTracker.TEMARIO -> {
                    items(temas, key = { "tema_${it.numero}" }) { tema ->
                        TemaCard(tema = tema, onChange = viewModel::onTemaChanged)
                    }
                }
                ModoOpoTracker.UNIDADES -> {
                    if (unidades.isEmpty()) {
                        item { EstadoVacioUnidadesEmbebido(onCrear = { mostrarCrearUnidad = true }) }
                    } else {
                        items(unidades, key = { "ud_${it.id}" }) { unidad ->
                            UnidadCard(
                                unidad = unidad,
                                onChange = viewModel::actualizarUnidad,
                                onRenombrar = { renombrandoUnidad = unidad },
                                onEliminar = { borrandoUnidad = unidad },
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }

        if (modo == ModoOpoTracker.UNIDADES) {
            FloatingActionButton(
                onClick = { mostrarCrearUnidad = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Añadir unidad didáctica")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (mostrarCrearUnidad) {
        NombrarUnidadDialog(
            titulo = "Nueva unidad didáctica",
            valorInicial = "",
            onConfirmar = { nombre ->
                viewModel.crearUnidad(nombre)
                mostrarCrearUnidad = false
            },
            onDismiss = { mostrarCrearUnidad = false },
        )
    }

    renombrandoUnidad?.let { unidad ->
        NombrarUnidadDialog(
            titulo = "Renombrar unidad didáctica",
            valorInicial = unidad.nombre,
            onConfirmar = { nombre ->
                viewModel.actualizarUnidad(unidad.copy(nombre = nombre.trim()))
                renombrandoUnidad = null
            },
            onDismiss = { renombrandoUnidad = null },
        )
    }

    borrandoUnidad?.let { unidad ->
        AlertDialog(
            onDismissRequest = { borrandoUnidad = null },
            title = { Text("¿Eliminar \"${unidad.nombre}\"?") },
            text = { Text("Se borrará esta unidad didáctica. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarUnidad(unidad)
                    borrandoUnidad = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { borrandoUnidad = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun ModoOpoTrackerToggle(modo: ModoOpoTracker, onModoChange: (ModoOpoTracker) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModoBotonTracker(
            texto = "Temario",
            seleccionado = modo == ModoOpoTracker.TEMARIO,
            onClick = { onModoChange(ModoOpoTracker.TEMARIO) },
            modifier = Modifier.weight(1f),
        )
        ModoBotonTracker(
            texto = "Unidades",
            seleccionado = modo == ModoOpoTracker.UNIDADES,
            onClick = { onModoChange(ModoOpoTracker.UNIDADES) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ModoBotonTracker(texto: String, seleccionado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (seleccionado) {
        Button(onClick = onClick, modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(texto) }
    }
}

@Composable
private fun EstadoVacioUnidadesEmbebido(onCrear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Todavía no has añadido ninguna unidad didáctica.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(top = 16.dp))
        Button(onClick = onCrear) {
            Text("Añadir unidad didáctica")
        }
    }
}

@Composable
private fun ProgresoGeneralCard(temas: List<TemaEntity>, contexto: ContextoInsignias) {
    val totalItems = temas.size * TOTAL_ITEMS_POR_TEMA
    val completados = temas.sumOf { it.completados() }
    val progreso = if (totalItems == 0) 0f else completados.toFloat() / totalItems
    val porcentaje = (progreso * 100).roundToInt()
    val conseguidas = INSIGNIAS.count { it.conseguida(contexto) }
    var mostrarInsignias by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Progreso general",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "$porcentaje%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarInsignias = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Insignias · $conseguidas/${INSIGNIAS.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Ver insignias",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }

    if (mostrarInsignias) {
        InsigniasDialog(
            contexto = contexto,
            onDismiss = { mostrarInsignias = false },
        )
    }
}

@Composable
private fun InsigniasDialog(
    contexto: ContextoInsignias,
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
                        text = "Insignias",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.padding(top = 4.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(INSIGNIAS, key = { it.id }) { insignia ->
                        InsigniaRow(insignia = insignia, conseguida = insignia.conseguida(contexto))
                    }
                }
            }
        }
    }
}

@Composable
private fun InsigniaRow(insignia: Insignia, conseguida: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (conseguida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (conseguida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (conseguida) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column {
            Text(
                text = insignia.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (conseguida) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = insignia.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
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
                    checked = tema.enSimulacro,
                    onToggle = { onChange(tema.copy(enSimulacro = !tema.enSimulacro)) },
                )
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
            maxLines = 1,
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        CircleCheck(checked = checked, onToggle = onToggle, size = 20.dp)
    }
}
