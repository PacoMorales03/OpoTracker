package com.example.opotracker.ui.planificacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opotracker.data.ASPECTOS_PLANIFICACION
import com.example.opotracker.data.MAX_ASPECTOS_PLANIFICACION
import com.example.opotracker.data.PlanificacionDiariaEntity
import com.example.opotracker.data.aspectoActivo
import com.example.opotracker.data.conAspectoAlternado
import com.example.opotracker.ui.common.CircleCheck
import com.example.opotracker.ui.common.IconoLlama
import com.example.opotracker.ui.common.fraseDelDia
import com.example.opotracker.ui.theme.Fuego
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanificacionScreen(viewModel: PlanificacionViewModel = viewModel()) {
    val plan = viewModel.plan
    val racha by viewModel.racha.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CabeceraMotivacional(racha = racha) }
        item {
            SelectorFecha(
                fecha = viewModel.fechaSeleccionada,
                esHoy = viewModel.fechaSeleccionada == LocalDate.now(),
                onAnterior = viewModel::irDiaAnterior,
                onSiguiente = viewModel::irDiaSiguiente,
                onHoy = viewModel::irHoy,
                onFechaElegida = viewModel::irAFecha,
            )
        }
        if (plan == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                TarjetaHoras(
                    titulo = "Horas previstas de estudio hoy",
                    valor = plan.horasPrevistas,
                    clave = plan.fecha + "_previstas",
                    onValorChange = { nuevoValor -> viewModel.actualizar(plan.copy(horasPrevistas = nuevoValor)) },
                )
            }
            item { TarjetaAspectos(plan = plan, onChange = viewModel::actualizar) }
            item { TarjetaMetas(plan = plan, onChange = viewModel::actualizar) }
            item {
                CampoTexto(
                    titulo = "Observaciones / Tareas pendientes",
                    valor = plan.observaciones,
                    clave = plan.fecha + "_obs",
                    minLines = 3,
                    onValorChange = { viewModel.actualizar(plan.copy(observaciones = it)) },
                )
            }
            item {
                TarjetaHoras(
                    titulo = "Horas reales de estudio hoy",
                    valor = plan.horasReales,
                    clave = plan.fecha + "_reales",
                    onValorChange = { nuevoValor -> viewModel.actualizar(plan.copy(horasReales = nuevoValor)) },
                )
            }
        }
    }
}

@Composable
private fun CabeceraMotivacional(racha: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = fraseDelDia(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.padding(start = 12.dp))
            RachaBadge(racha = racha)
        }
    }
}

/** Racha al estilo Duolingo/TikTok: una llamita con el número de días, independiente de las insignias. */
@Composable
private fun RachaBadge(racha: Int) {
    val activa = racha > 0
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activa) Fuego else MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconoLlama(
            tint = if (activa) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.padding(start = 4.dp))
        Text(
            text = racha.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (activa) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    fecha: LocalDate,
    esHoy: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onHoy: () -> Unit,
    onFechaElegida: (LocalDate) -> Unit,
) {
    val formateador = remember { DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES")) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onAnterior) {
            Text("‹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.clickable { mostrarCalendario = true },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formateador.format(fecha).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (!esHoy) {
                TextButton(onClick = onHoy, contentPadding = PaddingValues(0.dp)) {
                    Text("Volver a hoy")
                }
            }
        }
        IconButton(onClick = onSiguiente) {
            Text("›", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }

    if (mostrarCalendario) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        onFechaElegida(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = estado)
        }
    }
}

@Composable
private fun TarjetaHoras(titulo: String, valor: Float, clave: Any, onValorChange: (Float) -> Unit) {
    var texto by remember(clave) { mutableStateOf(if (valor == 0f) "" else valor.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(top = 10.dp))
            OutlinedTextField(
                value = texto,
                onValueChange = { nuevo ->
                    if (nuevo.isEmpty() || nuevo.matches(Regex("^\\d{0,2}([.,]\\d{0,2})?$"))) {
                        texto = nuevo
                        onValorChange(nuevo.replace(',', '.').toFloatOrNull() ?: 0f)
                    }
                },
                label = { Text("Horas") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(140.dp),
            )
        }
    }
}

@Composable
private fun TarjetaAspectos(plan: PlanificacionDiariaEntity, onChange: (PlanificacionDiariaEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Aspectos a trabajar (máx. $MAX_ASPECTOS_PLANIFICACION)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
            ASPECTOS_PLANIFICACION.forEachIndexed { index, etiqueta ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    CircleCheck(
                        checked = plan.aspectoActivo(index),
                        onToggle = { onChange(plan.conAspectoAlternado(index)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaMetas(plan: PlanificacionDiariaEntity, onChange: (PlanificacionDiariaEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Metas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(top = 10.dp))
            CampoTextoInterno(
                label = "Objetivo 1",
                valor = plan.objetivo1,
                clave = plan.fecha + "_obj1",
                onValorChange = { onChange(plan.copy(objetivo1 = it)) },
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
            CampoTextoInterno(
                label = "Objetivo 2",
                valor = plan.objetivo2,
                clave = plan.fecha + "_obj2",
                onValorChange = { onChange(plan.copy(objetivo2 = it)) },
            )
        }
    }
}

@Composable
private fun CampoTexto(
    titulo: String,
    valor: String,
    clave: Any,
    minLines: Int = 1,
    onValorChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(top = 10.dp))
            CampoTextoInterno(label = null, valor = valor, clave = clave, minLines = minLines, onValorChange = onValorChange)
        }
    }
}

@Composable
private fun CampoTextoInterno(
    label: String?,
    valor: String,
    clave: Any,
    minLines: Int = 1,
    onValorChange: (String) -> Unit,
) {
    var texto by remember(clave) { mutableStateOf(valor) }
    OutlinedTextField(
        value = texto,
        onValueChange = {
            texto = it
            onValorChange(it)
        },
        label = label?.let { etiqueta -> { Text(etiqueta) } },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
    )
}
