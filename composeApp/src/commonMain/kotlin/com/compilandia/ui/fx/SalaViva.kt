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
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.NaAula
import com.compilandia.ui.room.art.P3
import com.compilandia.ui.room.art.ambienteDaSala
import com.compilandia.ui.room.art.besouro
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.compilandia.textos.tx

private val MONITOR = Color(0xFF8FF5E2)
private val LAMPADA = Color(0xFFFFD98A)

/** Um ciclo longo para o relógio e o avião de papel: 10 minutos, múltiplo de 60 s e do voo. */
private const val CICLO_S = 595f

/**
 * A sala respira: o monitor pisca, o projetor tremula, a poeira sobe na luz da porta, a aula de agora pulsa no quadro,
 * passa um avião de papel, e as peças compradas se mexem (ventilador, relógio, café). E um besouro passeia.
 */
@Composable
fun SalaViva(app: AppState, side: Dp) {
    if (ThemeState.reduceMotion) return
    val inf = rememberInfiniteTransition()
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart))
    val segundos by inf.animateFloat(0f, CICLO_S, infiniteRepeatable(tween((CICLO_S * 1000).toInt(), easing = LinearEasing)))
    val itens = app.ls.roomItems
    val temAbajur = "abajur" in itens
    Canvas(Modifier.fillMaxSize()) {
        val iso = Iso.aula(size.width)
        val w = size.width
        val tremor = 0.22f + 0.1f * sin(t * 90f) + 0.06f * sin(t * 241f)
        brilho(MONITOR.copy(alpha = tremor), iso.p(NaAula.TELA_DO_MONITOR), w * 0.07f)
        brilho(LAMPADA.copy(alpha = 0.1f + 0.03f * sin(t * 50f)), iso.p(NaAula.CENTRO_DA_TELA), w * 0.12f)
        if (temAbajur) brilho(LAMPADA.copy(alpha = 0.3f + 0.05f * sin(t * 30f)), iso.p(NaAula.ABAJUR), w * 0.08f)
        poeira(iso, t)
        ambienteDaSala(iso, segundos) { it in itens }
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

/** O caminho do besouro no chão da sala: entra pela porta, passa na frente das carteiras e volta pelo corredor. */
private val ROTA = listOf(
    P3(0.3f, 6.3f), P3(1.6f, 7.6f), P3(6f, 7.7f), P3(10.5f, 7.4f),
    P3(10.6f, 3.3f), P3(7f, 3.05f), P3(3f, 3.1f), P3(0.4f, 3f),
)

/** Raio do besouro em fração do lado da sala, e quantos passos ele dá na volta inteira. */
private const val BESOURO = 0.011f
private const val PASSOS = 260f

/** Onde o besouro está em [k] (0 a 1 da rota), em fração do quadrado, e para onde aponta, em graus na tela. */
private fun naRota(k: Float): Pair<Offset, Float> {
    val iso = Iso.aula(1f)
    val pts = ROTA.map { iso.p(it) }
    val trechos = pts.zipWithNext { a, b -> (b - a).getDistance() }
    var falta = trechos.sum() * k.coerceIn(0f, 1f)
    for ((i, d) in trechos.withIndex()) {
        val (a, b) = pts[i] to pts[i + 1]
        if (falta <= d || i == trechos.lastIndex) {
            val f = if (d > 0f) (falta / d).coerceIn(0f, 1f) else 0f
            val rumo = (atan2(b.y - a.y, b.x - a.x) * 180f / PI).toFloat()
            return (a + (b - a) * f) to rumo
        }
        falta -= d
    }
    return pts.last() to 0f
}

/** De vez em quando um besouro atravessa o chão. Tocou, esmagou: uma engrenagem, até o limite do dia. */
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
            p.animateTo(1f, tween(16000, easing = LinearEasing))
            vivo = false
        }
    }
    val (onde, rumo) = naRota(p.value)
    val fx = onde.x
    val fy = onde.y
    val alvo = 44.dp
    if (vivo) {
        Canvas(Modifier.fillMaxSize()) { besouro(onde * size.width, size.width * BESOURO, rumo, p.value * PASSOS) }
        Box(
            Modifier.offset(side * fx - alvo / 2, side * fy - alvo / 2).size(alvo)
                .clickable(remember { MutableInteractionSource() }, null) {
                    vivo = false
                    esmagado++
                    Sound.play(Sfx.IMPACT, 1.5f)
                    val pagou = app.learner.esmaga(nowMs())
                    app.commit()
                    if (pagou > 0) app.say(tx("salaviva_bug_esmagado_1_engrenagem"))
                },
        )
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
