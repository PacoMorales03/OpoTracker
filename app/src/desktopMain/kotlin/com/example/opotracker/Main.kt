package com.example.opotracker

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.example.opotracker.data.AppDatabaseHolder
import com.example.opotracker.data.getDatabaseBuilder
import com.example.opotracker.navigation.OpoTrackerDesktopApp
import com.example.opotracker.ui.theme.OpoTrackerTheme

fun main() {
    AppDatabaseHolder.inicializar { getDatabaseBuilder() }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "OpoTracker",
            state = rememberWindowState(size = DpSize(480.dp, 900.dp)),
        ) {
            OpoTrackerTheme {
                OpoTrackerDesktopApp()
            }
        }
    }
}
