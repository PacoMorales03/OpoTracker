package com.example.opotracker.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ActualizacionBanner(viewModel: ActualizacionViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) { viewModel.comprobarActualizacion() }

    when (val estadoActual = estado) {
        is EstadoActualizacion.SinComprobar,
        is EstadoActualizacion.Comprobando,
        is EstadoActualizacion.AlDia -> Unit

        is EstadoActualizacion.Disponible -> Card(
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
                    text = "Hay una actualización de OpoTracker disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { viewModel.descargarEInstalar(estadoActual.info) }) {
                    Text("Actualizar")
                }
            }
        }

        is EstadoActualizacion.Descargando -> Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Descargando actualización...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (estadoActual.progreso > 0f) {
                    LinearProgressIndicator(
                        progress = { estadoActual.progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        is EstadoActualizacion.ListaParaInstalar -> {
            var puedeInstalar by remember { mutableStateOf(puedeInstalarApks(contexto)) }
            var instalacionLanzada by remember { mutableStateOf(false) }
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, evento ->
                    if (evento == Lifecycle.Event.ON_RESUME) {
                        puedeInstalar = puedeInstalarApks(contexto)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(puedeInstalar) {
                if (puedeInstalar && !instalacionLanzada) {
                    instalacionLanzada = true
                    contexto.startActivity(intentInstalarApk(estadoActual.uriApk))
                }
            }

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
                        text = if (puedeInstalar) {
                            "Instalando la actualización..."
                        } else {
                            "Permite instalar apps de OpoTracker para continuar"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        val intent = if (puedeInstalar) {
                            intentInstalarApk(estadoActual.uriApk)
                        } else {
                            intentAjustesInstalarDesconocidos(contexto.packageName)
                        }
                        contexto.startActivity(intent)
                    }) {
                        Text(if (puedeInstalar) "Instalar" else "Permitir")
                    }
                }
            }
        }

        is EstadoActualizacion.Error -> Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "No se ha podido comprobar la actualización",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { viewModel.comprobarActualizacion() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}
