package com.example.opotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.opotracker.navigation.OpoTrackerApp
import com.example.opotracker.ui.theme.OpoTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpoTrackerTheme {
                OpoTrackerApp()
            }
        }
    }
}
