package com.compilandia.ui.cast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.compilandia.content.Speaker
import com.compilandia.ui.voice.Narrator
import kotlin.math.PI
import kotlin.math.sin

private val YELLOW = Color(0xFFF5B700)
private val YELLOW_LO = Color(0xFFD99A0A)
private val DARK = Color(0xFF1F2628)
private val STEEL = Color(0xFF5E7076)
private val RED = Color(0xFFC8432A)
private val GLOW = Color(0xFF8FF5E2)

/**
 * O Byte aprovado no board: robô chapado, cor lisa, rosto aceso numa tela escura.
 * Tudo é desenhado aqui para a boca seguir a fala e o corpo reagir ao que acontece.
 *
 * [lookX] e [lookY] vão de -1 a 1: para onde ele olha. [hop] é um contador: cada aumento dá um pulo.
 * [armsUp] de 0 a 1 levanta os braços (comemoração). [animate] falso desenha parado, para listas longas.
 */
@Composable
fun ByteFigure(
    modifier: Modifier = Modifier,
    mood: Mood = Mood.NEUTRAL,
    lookX: Float = 0f,
    lookY: Float = 0f,
    hop: Int = 0,
    armsUp: Float = 0f,
    animate: Boolean = true,
) {
    val clock = rememberRigClock(animate) { Narrator.speaker == Speaker.BYTE || Narrator.speaker == Speaker.NARRADOR }
    val jump = remember { Animatable(0f) }
    LaunchedEffect(hop) {
        if (hop > 0) {
            jump.snapTo(1f)
            jump.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
    }
    val lx by animateFloatAsState(lookX)
    val ly by animateFloatAsState(lookY)
    val arms by animateFloatAsState(armsUp, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    val moodBlend by animateFloatAsState(if (mood == Mood.NEUTRAL) 0f else 1f)
    Canvas(modifier.aspectRatio(400f / 480f)) {
        val u = size.width / 400f
        val t = clock.time.floatValue
        val talk = clock.talking.floatValue
        val bob = sin(t * 2.4f) * 3f + sin(t * 9f) * 2f * talk
        val hopY = -70f * jump.value
        val stretch = 1f + 0.10f * jump.value
        scale(u, u, Offset.Zero) {
            translate(0f, hopY) {
                scale(1f / stretch, stretch, Offset(200f, 465f)) {
                    drawByte(t, bob, talk, clock, mood, moodBlend, lx, ly, arms)
                }
            }
        }
    }
}

private fun DrawScope.drawByte(t: Float, bob: Float, talk: Float, clock: RigClock, mood: Mood, blend: Float, lx: Float, ly: Float, arms: Float) {
    fun r(x: Float, y: Float, w: Float, h: Float, rad: Float, c: Color) =
        drawRoundRect(c, Offset(x, y), Size(w, h), CornerRadius(rad))

    // esteiras
    r(70f, 425f, 90f, 40f, 20f, DARK)
    r(240f, 425f, 90f, 40f, 20f, DARK)
    val roll = (t * 40f) % 18f
    for (side in listOf(70f, 240f)) for (k in 0..4) {
        drawCircle(STEEL, 6f, Offset(side + 8f + ((k * 18f + roll) % 78f), 445f))
    }

    val bodyLift = bob * 0.5f
    translate(0f, bodyLift) {
        // braços, com o ombro como pivô
        val wave = if (arms > 0.01f) sin(t * 12f) * 8f else 0f
        rotate(20f - 150f * arms + wave, Offset(106f, 312f)) {
            r(72f, 305f, 34f, 84f, 17f, YELLOW)
            r(76f, 382f, 26f, 14f, 6f, DARK)
        }
        rotate(-20f + 150f * arms - wave, Offset(294f, 312f)) {
            r(294f, 305f, 34f, 84f, 17f, YELLOW)
            r(298f, 382f, 26f, 14f, 6f, DARK)
        }
        // corpo com painel de perigo
        r(120f, 290f, 160f, 140f, 26f, YELLOW)
        r(120f, 402f, 160f, 28f, 14f, YELLOW_LO)
        r(120f, 290f, 160f, 124f, 26f, YELLOW)
        r(150f, 330f, 100f, 62f, 8f, DARK)
        for (k in 0..2) {
            val x = 156f + k * 30f
            drawPath(Path().apply { moveTo(x, 386f); lineTo(x + 28f, 336f); lineTo(x + 40f, 336f); lineTo(x + 12f, 386f); close() }, YELLOW)
        }
    }

    translate(0f, bob) {
        val tilt = sin(t * 1.3f) * 1.2f + sin(t * 7f) * 1.4f * talk + lx * 2.5f
        rotate(tilt, Offset(200f, 280f)) {
            // antena com mola
            val sway = sin(t * 3.1f) * 6f + sin(t * 11f) * 5f * talk + lx * 6f
            drawLine(STEEL, Offset(200f, 92f), Offset(200f + sway, 52f), 12f, StrokeCap.Round)
            drawCircle(RED, 16f, Offset(200f + sway, 44f))
            drawCircle(Color.White.copy(alpha = 0.45f), 5f, Offset(195f + sway, 39f))
            // orelhas e cabeça
            r(44f, 150f, 22f, 66f, 10f, STEEL)
            r(334f, 150f, 22f, 66f, 10f, STEEL)
            r(60f, 90f, 280f, 190f, 46f, YELLOW)
            r(60f, 240f, 280f, 40f, 30f, YELLOW_LO)
            r(60f, 90f, 280f, 176f, 46f, YELLOW)
            r(92f, 118f, 216f, 134f, 20f, DARK)
            drawFace(clock, mood, blend, lx, ly, talk)
        }
    }
}

private fun DrawScope.drawFace(clock: RigClock, mood: Mood, blend: Float, lx: Float, ly: Float, talk: Float) {
    val ex = lx * 12f
    val ey = ly * 8f
    val blink = clock.blink.floatValue
    val leftC = Offset(158f + ex, 176f + ey)
    val rightC = Offset(242f + ex, 176f + ey)
    val stroke = Stroke(12f, cap = StrokeCap.Round)
    when (mood) {
        Mood.HAPPY -> {
            for (c in listOf(leftC, rightC)) {
                drawPath(Path().apply { moveTo(c.x - 24f, c.y + 8f); quadraticTo(c.x, c.y - 30f, c.x + 24f, c.y + 8f) }, GLOW, style = stroke)
            }
        }
        Mood.SURPRISED -> {
            drawCircle(GLOW, 25f, leftC, style = Stroke(11f))
            drawCircle(GLOW, 25f, rightC, style = Stroke(11f))
            drawCircle(GLOW, 8f, leftC)
            drawCircle(GLOW, 8f, rightC)
        }
        Mood.SAD -> {
            for ((c, s) in listOf(leftC to -1f, rightC to 1f)) {
                drawOval(GLOW, Offset(c.x - 19f, c.y + 2f - 14f * blink), Size(38f, 28f * blink))
                drawLine(GLOW, Offset(c.x - 26f, c.y - 22f + s * 9f), Offset(c.x + 26f, c.y - 22f - s * 9f), 10f, StrokeCap.Round)
            }
        }
        Mood.THINK -> {
            // Pensando: os dois olhos abertos, puxados para cima e para o lado, e uma sobrancelha erguida.
            val up = Offset(6f, -6f)
            for (c in listOf(leftC + up, rightC + up)) {
                drawOval(GLOW, Offset(c.x - 21f, c.y - 25f * blink), Size(42f, 50f * blink))
            }
            drawLine(GLOW, Offset(rightC.x - 18f, rightC.y - 46f), Offset(rightC.x + 22f, rightC.y - 54f), 9f, StrokeCap.Round)
        }
        Mood.NEUTRAL -> {
            drawOval(GLOW, Offset(leftC.x - 22f, leftC.y - 27f * blink), Size(44f, 54f * blink))
            drawOval(GLOW, Offset(rightC.x - 22f, rightC.y - 27f * blink), Size(44f, 54f * blink))
        }
    }
    // Brilho do olho: só nos olhos cheios e abertos, para não boiar fora do olho quando ele pisca.
    if ((mood == Mood.NEUTRAL || mood == Mood.THINK) && blink > 0.7f) {
        val d = if (mood == Mood.THINK) Offset(6f, -6f) else Offset.Zero
        for (c in listOf(leftC + d, rightC + d)) drawOval(DARK.copy(alpha = 0.5f), Offset(c.x - 3f, c.y - 20f * blink), Size(10f, 12f * blink))
    }

    // boca: falando, segue a fala; calado, mostra o humor
    val mx = 200f + ex * 0.5f
    val my = 228f + ey * 0.4f
    if (talk > 0.15f || mood == Mood.NEUTRAL || mood == Mood.THINK) {
        val w = clock.mouthW.floatValue
        val h = clock.mouthH.floatValue
        drawRoundRect(GLOW, Offset(mx - w / 2, my - h / 2), Size(w, h), CornerRadius(minOf(h, w) / 2))
    } else when (mood) {
        Mood.HAPPY -> {
            drawPath(Path().apply { moveTo(mx - 36f, my - 8f); quadraticTo(mx, my + 26f, mx + 36f, my - 8f) }, GLOW, style = Stroke(11f, cap = StrokeCap.Round))
        }
        Mood.SAD -> {
            drawPath(Path().apply { moveTo(mx - 30f, my + 10f); quadraticTo(mx, my - 18f, mx + 30f, my + 10f) }, GLOW, style = Stroke(11f, cap = StrokeCap.Round))
        }
        else -> drawOval(GLOW, Offset(mx - 13f, my - 16f), Size(26f, 32f))
    }
    @Suppress("UNUSED_EXPRESSION") blend
}

@Suppress("unused") private const val TWO_PI = (2 * PI).toFloat()
