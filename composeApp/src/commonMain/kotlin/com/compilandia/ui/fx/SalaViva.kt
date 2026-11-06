package com.compilandia.ui.fx

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.BugFigure
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.compilandia.textos.tx

private val MONITOR = Color(0xFF8FF5E2)
private val SOLDA = Color(0xFFF5B700)
private val LAMPADA = Color(0xFFFFD98A)

/** A oficina respira: monitor pisca, solda solta faísca, poeira sobe e um Bug passeia. */
@Composable
fun SalaViva(app: AppState, side: Dp) {
    if (ThemeState.reduceMotion) return
    val inf = rememberInfiniteTransition()
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart))
    val temLuz = "luminaria" in app.ls.roomItems
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        // brilho do monitor, com o tremor de uma tela velha
        val tremor = 0.22f + 0.1f * sin(t * 90f) + 0.06f * sin(t * 241f)
        val cm = Offset(w * 0.31f, w * 0.39f)
        drawCircle(Brush.radialGradient(listOf(MONITOR.copy(alpha = tremor), Color.Transparent), cm, w * 0.13f), w * 0.13f, cm)
        if (temLuz) {
            val cl = Offset(w * 0.76f, w * 0.36f)
            val a = 0.2f + 0.05f * sin(t * 30f)
            drawCircle(Brush.radialGradient(listOf(LAMPADA.copy(alpha = a), Color.Transparent), cl, w * 0.17f), w * 0.17f, cl)
        }
        // solda: rajadas curtas no quadro de ferramentas
        val ciclo = (t * 7f) % 1f
        if (ciclo < 0.3f) {
            val base = Offset(w * 0.215f, w * 0.335f)
            val rnd = Random((t * 7f).toInt() + 1)
            repeat(12) {
                val a = 0.6f + rnd.nextFloat() * 2f
                val v = (0.018f + rnd.nextFloat() * 0.05f) * w
                val k = ciclo / 0.3f
                val q = base + Offset(cos(a), sin(a)) * (v * k) + Offset(0f, w * 0.03f * k * k)
                drawLine(SOLDA.copy(alpha = 1f - k), q, q + Offset(cos(a), sin(a)) * (w * 0.006f), w * 0.0022f)
            }
            drawCircle(Color.White.copy(alpha = 0.7f * (1f - ciclo / 0.3f)), w * 0.008f, base)
        }
        // poeira subindo devagar pela luz
        val rnd = Random(5)
        repeat(14) {
            val x = 0.2f + rnd.nextFloat() * 0.62f
            val y0 = 0.3f + rnd.nextFloat() * 0.45f
            val fase = (t + rnd.nextFloat()) % 1f
            val y = y0 - fase * 0.2f
            drawCircle(Color.White.copy(alpha = 0.22f * sin(fase * 3.14f)), w * 0.0022f, Offset(w * (x + 0.012f * sin(fase * 8f + it)), w * y))
        }
    }
    BugPasseando(app, side)
}

/** De vez em quando um Bug atravessa o chão. Tocou, esmagou: uma engrenagem, até o limite do dia. */
@Composable
private fun BugPasseando(app: AppState, side: Dp) {
    var vivo by remember { mutableStateOf(false) }
    var esmagado by remember { mutableIntStateOf(0) }
    val p = remember { Animatable(0f) }
    val poca = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L + Random.nextLong(9000))
            vivo = true
            p.snapTo(0f)
            p.animateTo(1f, tween(9000, easing = LinearEasing))
            vivo = false
        }
    }
    // caminho no chão, na direção das tábuas
    val x0 = 0.26f
    val y0 = 0.74f
    val dx = 0.5f
    val dy = -0.24f
    val fx = x0 + dx * p.value
    val fy = y0 + dy * p.value
    val w = side * 0.075f
    if (vivo) {
        Box(
            Modifier.offset(side * fx - w / 2, side * fy - w / 2).width(w).rotate(64f)
                .clickable(remember { MutableInteractionSource() }, null) {
                    vivo = false
                    esmagado++
                    Sound.play(Sfx.IMPACT, 1.5f)
                    val pagou = app.learner.esmaga(nowMs())
                    app.commit()
                    if (pagou > 0) app.say(tx("salaviva_bug_esmagado_1_engrenagem"))
                },
        ) { BugFigure(Modifier.width(w)) }
    }
    LaunchedEffect(esmagado) {
        if (esmagado > 0) { poca.snapTo(0f); poca.animateTo(1f, tween(600)) }
    }
    if (poca.value < 1f) {
        val onde = Offset(fx, fy)
        Canvas(Modifier.fillMaxSize()) {
            val k = poca.value
            val c = Offset(size.width * onde.x, size.width * onde.y)
            val rnd = Random(9)
            repeat(14) {
                val a = rnd.nextFloat() * 6.283f
                val v = (0.02f + rnd.nextFloat() * 0.05f) * size.width
                drawCircle(Color(0xFFC8432A).copy(alpha = 1f - k), size.width * 0.006f * (1f - k), c + Offset(cos(a), sin(a)) * (v * k))
            }
            drawOval(Color(0xFF1F2628).copy(alpha = 0.6f * (1f - k)), c - Offset(size.width * 0.03f, size.width * 0.015f), Size(size.width * 0.06f, size.width * 0.03f))
        }
    }
}
