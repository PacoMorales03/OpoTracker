package com.example.opotracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Icono de calendario dibujado a mano, para no depender de un set de iconos concreto. */
@Composable
fun IconoCalendario(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val grosor = w * 0.09f

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.2f),
            size = Size(w * 0.84f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = grosor),
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.08f, h * 0.42f),
            end = Offset(w * 0.92f, h * 0.42f),
            strokeWidth = grosor * 0.7f,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.08f),
            end = Offset(w * 0.3f, h * 0.3f),
            strokeWidth = grosor,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.7f, h * 0.08f),
            end = Offset(w * 0.7f, h * 0.3f),
            strokeWidth = grosor,
            cap = StrokeCap.Round,
        )
    }
}
