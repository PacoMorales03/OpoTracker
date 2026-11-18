package com.example.opotracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private const val MAX_ESTRELLAS = 5

/** Interactive 1-5 star rating that supports half-star precision (tap the left/right half of a star). */
@Composable
fun StarRatingInput(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 34.dp,
) {
    Row(modifier = modifier) {
        for (index in 0 until MAX_ESTRELLAS) {
            val fill = (rating - index).coerceIn(0f, 1f)
            Star(
                fillFraction = fill,
                modifier = Modifier
                    .size(starSize)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val mitad = offset.x < size.width / 2f
                            onRatingChange(index + if (mitad) 0.5f else 1f)
                        }
                    },
            )
        }
    }
}

/** Read-only star rating for showing a saved score. */
@Composable
fun StarRatingDisplay(rating: Float, modifier: Modifier = Modifier, starSize: Dp = 16.dp) {
    Row(modifier = modifier) {
        for (index in 0 until MAX_ESTRELLAS) {
            val fill = (rating - index).coerceIn(0f, 1f)
            Star(fillFraction = fill, modifier = Modifier.size(starSize))
        }
    }
}

@Composable
private fun Star(fillFraction: Float, modifier: Modifier = Modifier) {
    val fillColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier) {
        val path = estrellaPath(size.width, size.height)
        drawPath(path, color = outlineColor, style = Stroke(width = 1.5.dp.toPx()))
        if (fillFraction > 0f) {
            clipRect(right = size.width * fillFraction) {
                drawPath(path, color = fillColor)
            }
        }
    }
}

private fun estrellaPath(width: Float, height: Float): Path {
    val path = Path()
    val cx = width / 2f
    val cy = height / 2f
    val outerRadius = minOf(cx, cy)
    val innerRadius = outerRadius * 0.42f
    val puntas = 5
    val angleStep = Math.PI / puntas
    var angle = -Math.PI / 2
    for (i in 0 until puntas * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += angleStep
    }
    path.close()
    return path
}
