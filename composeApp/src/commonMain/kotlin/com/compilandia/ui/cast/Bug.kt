package com.compilandia.ui.cast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin

private val SHELL = Color(0xFFC8432A)
private val SHELL_LO = Color(0xFF9A2E1B)
private val DARK = Color(0xFF1F2628)
private val STEEL = Color(0xFF7F939A)
private val TIP = Color(0xFFF5B700)

/**
 * O Bug: besouro chapado que rói o compilador.
 * [hurt] é um contador: cada aumento dá um tranco. [down] de 0 a 1 tomba o bicho quando ele perde.
 */
@Composable
fun BugFigure(modifier: Modifier = Modifier, hurt: Int = 0, down: Float = 0f, casco: Color = SHELL, cascoEscuro: Color = SHELL_LO) {
    val clock = rememberRigClock { false }
    val hit = remember { Animatable(0f) }
    LaunchedEffect(hurt) {
        if (hurt > 0) {
            hit.snapTo(1f)
            hit.animateTo(0f, tween(420))
        }
    }
    Canvas(modifier.aspectRatio(1.2f)) {
        val u = size.width / 360f
        val t = clock.time.floatValue
        val h = hit.value
        val shake = sin(h * 40f) * 12f * h
        val creep = sin(t * 3f) * 4f
        scale(u, u, Offset.Zero) {
            translate(shake + creep * (1f - down), 0f) {
                rotate(180f * down, Offset(180f, 170f)) {
                    // pernas
                    for (k in 0..2) {
                        val y = 130f + k * 52f
                        val step = sin(t * 8f + k * 2f) * 8f * (1f - down)
                        val lift = sin(t * 8f + k * 2f + 1.5f) * 5f
                        drawPath(Path().apply { moveTo(100f, y); lineTo(44f, y - 18f + step); lineTo(24f, y + 34f + lift) }, DARK, style = Stroke(12f, cap = StrokeCap.Round))
                        drawPath(Path().apply { moveTo(260f, y); lineTo(316f, y - 18f - step); lineTo(336f, y + 34f - lift) }, DARK, style = Stroke(12f, cap = StrokeCap.Round))
                    }
                    // casco
                    drawOval(cascoEscuro, Offset(78f, 86f), Size(204f, 196f))
                    drawOval(casco, Offset(78f, 80f), Size(204f, 186f))
                    drawRoundRect(DARK, Offset(173f, 130f), Size(14f, 140f), CornerRadius(7f))
                    drawCircle(DARK, 17f, Offset(128f, 190f))
                    drawCircle(DARK, 12f, Offset(146f, 232f))
                    drawCircle(DARK, 17f, Offset(232f, 190f))
                    drawCircle(DARK, 12f, Offset(214f, 232f))
                    // cabeça
                    drawOval(DARK, Offset(104f, 42f), Size(152f, 112f))
                    drawRoundRect(Color(0xFF14191B), Offset(124f, 62f), Size(112f, 62f), CornerRadius(16f))
                    // antenas
                    val sway = sin(t * 5f) * 6f
                    drawLine(STEEL, Offset(148f, 56f), Offset(118f + sway, 8f), 7f, StrokeCap.Round)
                    drawLine(STEEL, Offset(212f, 56f), Offset(242f - sway, 8f), 7f, StrokeCap.Round)
                    drawCircle(TIP, 11f, Offset(118f + sway, 8f))
                    drawCircle(TIP, 11f, Offset(242f - sway, 8f))
                    // olhos: bravos; apanhando, em X
                    val eye = Color(0xFFFF9A85)
                    if (h > 0.2f || down > 0.3f) {
                        for (c in listOf(Offset(154f, 92f), Offset(206f, 92f))) {
                            drawLine(eye, c + Offset(-12f, -12f), c + Offset(12f, 12f), 7f, StrokeCap.Round)
                            drawLine(eye, c + Offset(-12f, 12f), c + Offset(12f, -12f), 7f, StrokeCap.Round)
                        }
                    } else {
                        val blink = clock.blink.floatValue
                        drawOval(eye, Offset(140f, 92f - 12f * blink), Size(28f, 24f * blink))
                        drawOval(eye, Offset(192f, 92f - 12f * blink), Size(28f, 24f * blink))
                        drawLine(DARK, Offset(136f, 76f), Offset(170f, 88f), 7f, StrokeCap.Round)
                        drawLine(DARK, Offset(224f, 76f), Offset(190f, 88f), 7f, StrokeCap.Round)
                    }
                }
            }
        }
    }
}
