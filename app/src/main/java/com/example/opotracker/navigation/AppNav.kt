package com.example.opotracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.opotracker.ui.simulador.SimuladorScreen
import com.example.opotracker.ui.tracker.TrackerScreen
import com.example.opotracker.ui.unidades.UnidadesScreen

private const val ROUTE_TRACKER = "tracker"
private const val ROUTE_UNIDADES = "unidades"
private const val ROUTE_SIMULADOR = "simulador"

@Composable
fun OpoTrackerApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination

                NavigationBarItem(
                    selected = currentRoute?.hierarchy?.any { it.route == ROUTE_TRACKER } == true,
                    onClick = {
                        navController.navigate(ROUTE_TRACKER) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("OpoTracker") },
                )
                NavigationBarItem(
                    selected = currentRoute?.hierarchy?.any { it.route == ROUTE_UNIDADES } == true,
                    onClick = {
                        navController.navigate(ROUTE_UNIDADES) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    label = { Text("Unidades") },
                )
                NavigationBarItem(
                    selected = currentRoute?.hierarchy?.any { it.route == ROUTE_SIMULADOR } == true,
                    onClick = {
                        navController.navigate(ROUTE_SIMULADOR) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    label = { Text("Simulador") },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_TRACKER,
            modifier = Modifier.padding(padding),
        ) {
            composable(ROUTE_TRACKER) { TrackerScreen() }
            composable(ROUTE_UNIDADES) { UnidadesScreen() }
            composable(ROUTE_SIMULADOR) { SimuladorScreen() }
        }
    }
}
