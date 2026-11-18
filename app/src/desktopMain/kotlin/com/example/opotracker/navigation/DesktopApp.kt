package com.example.opotracker.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.opotracker.ui.common.IconoCalendario
import com.example.opotracker.ui.planificacion.PlanificacionScreen
import com.example.opotracker.ui.simulador.SimuladorScreen
import com.example.opotracker.ui.tracker.TrackerScreen
import com.example.opotracker.ui.update.ActualizacionDesktopBanner

private enum class PantallaDesktop { TRACKER, SIMULADOR, PLANIFICACION }

/**
 * Equivalente de escritorio de `OpoTrackerApp` (navigation-compose, androidMain): al no haber
 * soporte multiplataforma estable para navigation-compose todavía, aquí la navegación es un
 * simple estado local en vez de un NavHost, pero reutiliza exactamente las mismas pantallas.
 */
@Composable
fun OpoTrackerDesktopApp() {
    var pantalla by remember { mutableStateOf(PantallaDesktop.TRACKER) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pantalla == PantallaDesktop.TRACKER,
                    onClick = { pantalla = PantallaDesktop.TRACKER },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("OpoTracker") },
                )
                NavigationBarItem(
                    selected = pantalla == PantallaDesktop.SIMULADOR,
                    onClick = { pantalla = PantallaDesktop.SIMULADOR },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    label = { Text("Simulador") },
                )
                NavigationBarItem(
                    selected = pantalla == PantallaDesktop.PLANIFICACION,
                    onClick = { pantalla = PantallaDesktop.PLANIFICACION },
                    icon = { IconoCalendario() },
                    label = { Text("Plan diario") },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ActualizacionDesktopBanner()
            when (pantalla) {
                PantallaDesktop.TRACKER -> TrackerScreen()
                PantallaDesktop.SIMULADOR -> SimuladorScreen()
                PantallaDesktop.PLANIFICACION -> PlanificacionScreen()
            }
        }
    }
}
