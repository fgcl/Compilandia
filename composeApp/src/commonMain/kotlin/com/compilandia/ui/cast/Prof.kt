package com.compilandia.ui.cast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.compilandia.content.Speaker
import com.compilandia.ui.voice.Narrator
import kotlin.math.sin

private val BODY = Color(0xFF8A5A2B)
private val BELLY = Color(0xFFE8C08A)
private val DARK = Color(0xFF1F2628)
private val BEAK = Color(0xFFF5B700)

/** Profª Gramática: coruja chapada, mesmo estilo do Byte. O bico abre com a fala. */
@Composable
fun ProfFigure(modifier: Modifier = Modifier, lookX: Float = 0f, animate: Boolean = true) {
    val clock = rememberRigClock(animate) { Narrator.speaker == Speaker.PROF }
    Canvas(modifier.aspectRatio(1f)) {
        val u = size.width / 300f
        val t = clock.time.floatValue
        val talk = clock.talking.floatValue
        val bob = sin(t * 2f) * 3f + sin(t * 8f) * 2f * talk
        scale(u, u, Offset.Zero) {
            translate(0f, bob) {
                rotate(sin(t * 1.1f) * 1.5f + sin(t * 6f) * 1.5f * talk, Offset(150f, 240f)) {
                    // tufos e corpo
                    drawPath(Path().apply { moveTo(66f, 96f); lineTo(58f, 44f); lineTo(112f, 74f); close() }, BODY)
                    drawPath(Path().apply { moveTo(234f, 96f); lineTo(242f, 44f); lineTo(188f, 74f); close() }, BODY)
                    drawRoundRect(BODY, Offset(50f, 62f), Size(200f, 222f), CornerRadius(96f))
                    drawOval(BELLY, Offset(92f, 170f), Size(116f, 104f))
                    for (k in 0..2) drawArc(BODY, 20f, 140f, false, Offset(112f + k * 26f, 200f), Size(24f, 20f), style = Stroke(5f))
                    // asas
                    val flap = sin(t * 8f) * 5f * talk
                    rotate(8f + flap, Offset(58f, 150f)) { drawRoundRect(Color(0xFF6E4620), Offset(38f, 142f), Size(38f, 104f), CornerRadius(19f)) }
                    rotate(-8f - flap, Offset(242f, 150f)) { drawRoundRect(Color(0xFF6E4620), Offset(224f, 142f), Size(38f, 104f), CornerRadius(19f)) }
                    // olhos
                    val blink = clock.blink.floatValue
                    for (x in listOf(112f, 188f)) {
                        drawCircle(Color.White, 40f, Offset(x, 122f))
                        drawOval(DARK, Offset(x - 17f + lookX * 8f, 122f - 19f * blink), Size(34f, 38f * blink))
                        drawCircle(Color.White, 6f, Offset(x - 6f + lookX * 8f, 114f))
                    }
                    // óculos
                    for (x in listOf(112f, 188f)) drawCircle(DARK, 42f, Offset(x, 122f), style = Stroke(7f))
                    drawLine(DARK, Offset(150f, 118f), Offset(150f, 118f), 7f)
                    // bico
                    val open = (clock.mouthH.floatValue - 9f).coerceAtLeast(0f) * 0.55f
                    drawPath(Path().apply { moveTo(132f, 156f); lineTo(168f, 156f); lineTo(150f, 178f); close() }, BEAK)
                    drawPath(Path().apply { moveTo(136f, 166f + open * 0.3f); lineTo(164f, 166f + open * 0.3f); lineTo(150f, 180f + open); close() }, Color(0xFFD99A0A))
                }
            }
        }
    }
}
