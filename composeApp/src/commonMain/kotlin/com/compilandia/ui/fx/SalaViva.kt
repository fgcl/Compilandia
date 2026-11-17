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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.BugFigure
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.NaAula
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.compilandia.textos.tx

private val MONITOR = Color(0xFF8FF5E2)
private val LAMPADA = Color(0xFFFFD98A)

/** A sala respira: o monitor pisca, o projetor tremula, a poeira sobe na luz da porta e um Bug passeia. */
@Composable
fun SalaViva(app: AppState, side: Dp) {
    if (ThemeState.reduceMotion) return
    val inf = rememberInfiniteTransition()
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart))
    val temAbajur = "abajur" in app.ls.roomItems
    Canvas(Modifier.fillMaxSize()) {
        val iso = Iso.aula(size.width)
        val w = size.width
        val tremor = 0.22f + 0.1f * sin(t * 90f) + 0.06f * sin(t * 241f)
        brilho(MONITOR.copy(alpha = tremor), iso.p(NaAula.TELA_DO_MONITOR), w * 0.07f)
        brilho(LAMPADA.copy(alpha = 0.1f + 0.03f * sin(t * 50f)), iso.p(NaAula.CENTRO_DA_TELA), w * 0.12f)
        if (temAbajur) brilho(LAMPADA.copy(alpha = 0.3f + 0.05f * sin(t * 30f)), iso.p(NaAula.ABAJUR), w * 0.08f)
        poeira(iso, t)
    }
    BugPasseando(app, side)
}

private fun DrawScope.brilho(cor: Color, c: Offset, r: Float) =
    drawCircle(Brush.radialGradient(listOf(cor, Color.Transparent), c, r), r, c)

/** Poeira subindo devagar na luz que entra pela porta. */
private fun DrawScope.poeira(iso: Iso, t: Float) {
    val rnd = Random(5)
    repeat(14) {
        val u = rnd.nextFloat() * 3f
        val v = 5.6f + rnd.nextFloat() * 1.6f
        val fase = (t + rnd.nextFloat()) % 1f
        val q = iso.p(u, v, 0.4f + fase * 2.4f)
        val cor = Color.White.copy(alpha = 0.5f * sin(fase * 3.14f))
        drawCircle(cor, size.width * 0.0022f, q + Offset(size.width * 0.01f * sin(fase * 8f + it), 0f))
    }
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
    // caminho no chão, na frente da última fileira de cadeiras
    val iso = Iso.aula(1f)
    val de = iso.p(0.6f, 7.6f)
    val ate = iso.p(10.4f, 7.6f)
    val fx = de.x + (ate.x - de.x) * p.value
    val fy = de.y + (ate.y - de.y) * p.value
    val w = side * 0.075f
    if (vivo) {
        Box(
            Modifier.offset(side * fx - w / 2, side * fy - w / 2).width(w).rotate(119f)
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
