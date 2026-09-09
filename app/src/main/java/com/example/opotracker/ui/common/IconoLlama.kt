package com.example.opotracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/** Icono de llama dibujado a mano (estilo racha de Duolingo/TikTok), para no depender de un set de iconos concreto. */
@Composable
fun IconoLlama(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.50f, h * 0.02f)
            cubicTo(w * 0.90f, h * 0.35f, w * 0.85f, h * 0.62f, w * 0.62f, h * 0.55f)
            cubicTo(w * 0.72f, h * 0.75f, w * 0.60f, h * 0.98f, w * 0.42f, h * 0.98f)
            cubicTo(w * 0.10f, h * 0.98f, w * 0.02f, h * 0.68f, w * 0.20f, h * 0.45f)
            cubicTo(w * 0.22f, h * 0.58f, w * 0.32f, h * 0.60f, w * 0.35f, h * 0.50f)
            cubicTo(w * 0.28f, h * 0.30f, w * 0.35f, h * 0.12f, w * 0.50f, h * 0.02f)
            close()
        }
        drawPath(path, color = tint)
    }
}
