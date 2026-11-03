package com.compilandia.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.BugFigure
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.pegboard
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val BUG_SEGMENTS = 5
private val BOLT = Color(0xFF8FF5E2)

/**
 * O palco dos minijogos: cada acerto dispara um raio no Bug, cada erro faz o Byte murchar.
 * Ouve o Juice, então funciona com todos os jogos sem mexer em nenhum deles.
 * [hits] esperados dão o tamanho da barra de vida; o Bug só cai de vez em [defeated].
 */
@Composable
fun GameArena(defeated: Boolean, escaped: Boolean, modifier: Modifier = Modifier, expectedHits: Int = 6) {
    var hits by remember { mutableIntStateOf(0) }
    var mood by remember { mutableStateOf(Mood.NEUTRAL) }
    var hop by remember { mutableIntStateOf(0) }
    var hurt by remember { mutableIntStateOf(0) }
    val bolt = remember { Animatable(1f) }
    val startHit = remember { Juice.hitTick }
    val startMiss = remember { Juice.missTick }
    val seed = remember(Juice.hitTick) { Random.nextInt() }
    androidx.compose.runtime.DisposableEffect(Unit) {
        Juice.palcos++
        onDispose { Juice.palcos-- }
    }

    LaunchedEffect(Juice.hitTick) {
        if (Juice.hitTick == startHit) return@LaunchedEffect
        hits++
        hop++
        mood = Mood.HAPPY
        bolt.snapTo(0f)
        delay(120)
        hurt++
        bolt.animateTo(1f, tween(520, easing = LinearEasing))
        mood = Mood.NEUTRAL
    }
    LaunchedEffect(Juice.missTick) {
        if (Juice.missTick == startMiss) return@LaunchedEffect
        mood = Mood.SAD
        delay(900)
        mood = Mood.NEUTRAL
    }
    val left = when {
        defeated -> 0
        else -> (BUG_SEGMENTS - hits * BUG_SEGMENTS / expectedHits.coerceAtLeast(1)).coerceAtLeast(1)
    }
    val down = if (defeated) 1f else 0f

    Box(modifier.fillMaxWidth().height(150.dp).background(Palette.hazardDark).pegboard(Palette.drawerHi.copy(alpha = 0.12f))) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            ByteFigure(
                Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 2.dp).height(132.dp),
                mood = if (defeated) Mood.HAPPY else if (escaped) Mood.SAD else mood,
                lookX = 0.8f, hop = hop, armsUp = if (defeated) 1f else 0f,
            )
            if (!escaped) BugFigure(Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 8.dp).width(150.dp), hurt, down)
            HpBar(left, Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 12.dp).width(132.dp).height(11.dp))
            if (Juice.combo >= 2 && !Juice.overclock) ComboPill(Juice.combo, Modifier.align(Alignment.TopStart).padding(10.dp))
            if (Juice.overclock && !ThemeState.reduceMotion) Chamas(Juice.mult, Modifier.fillMaxSize())
            if (!ThemeState.reduceMotion) Canvas(Modifier.fillMaxSize()) {
                val p = bolt.value
                if (p >= 1f) return@Canvas
                val a = Offset(size.width * 0.24f, size.height * 0.46f)
                val b = Offset(size.width * 0.80f, size.height * 0.58f)
                if (p < 0.34f) {
                    // raio: uma linha quebrada que treme
                    val rnd = Random(seed + (p * 24).toInt())
                    val path = Path().apply {
                        moveTo(a.x, a.y)
                        for (k in 1..6) {
                            val t = k / 7f
                            lineTo(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t + (rnd.nextFloat() - 0.5f) * 44f * density)
                        }
                        lineTo(b.x, b.y)
                    }
                    val grosso = if (Juice.overclock) 2f else 1f
                    drawPath(path, BOLT.copy(alpha = 0.3f), style = Stroke(10f * density * grosso, cap = StrokeCap.Round))
                    drawPath(path, BOLT, style = Stroke(3f * density * grosso, cap = StrokeCap.Round))
                    // clarão curto no impacto
                    if (p < 0.1f) drawRect(Color.White.copy(alpha = 0.28f * (1f - p / 0.1f)))
                }
                // onda de choque no Bug
                val k = ((p - 0.14f) / 0.86f).coerceIn(0f, 1f)
                drawCircle(BOLT.copy(alpha = (1f - k) * 0.9f), (10f + 70f * k) * density, b, style = Stroke((1f - k) * 7f * density))
                val rnd = Random(seed)
                for (i in 0 until 12) {
                    val ang = rnd.nextFloat() * 6.28f
                    val v = (30f + rnd.nextFloat() * 70f) * density
                    val pos = b + Offset(cos(ang), sin(ang)) * (v * k) + Offset(0f, 34f * density * k * k)
                    drawCircle(if (i % 2 == 0) Palette.yellow else BOLT, (2.5f - 2f * k) * density, pos)
                }
            }
        }
    }
    HazardStripe()
}

/** Overclock: labaredas no chão da arena e o multiplicador batendo no ritmo. */
@Composable
private fun Chamas(mult: Int, modifier: Modifier) {
    val inf = rememberInfiniteTransition()
    val t by inf.animateFloat(0f, 6.283f, infiniteRepeatable(tween(760, easing = LinearEasing), RepeatMode.Restart))
    val fogo = Color(0xFFFF7A28)
    val brasa = Color(0xFFF5B700)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val n = 15
            val w = size.width / n
            for (i in 0 until n) {
                val h = size.height * (0.13f + 0.1f * (0.5f + 0.5f * sin(t * 2 + i * 1.7f)))
                val x = i * w
                val base = size.height
                drawPath(Path().apply {
                    moveTo(x, base)
                    quadraticTo(x + w * 0.1f, base - h * 0.6f, x + w * 0.5f + sin(t + i) * w * 0.2f, base - h)
                    quadraticTo(x + w * 0.9f, base - h * 0.6f, x + w, base)
                    close()
                }, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(brasa, fogo), base - h, base))
            }
        }
        val bate = 1f + 0.12f * (0.5f + 0.5f * sin(t * 2))
        Text(
            "×$mult", Modifier.align(Alignment.TopCenter).padding(top = 4.dp).scale(bate),
            color = brasa, fontFamily = com.compilandia.ui.LocalFonts.current.display, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 62.sp,
        )
    }
}

@Composable
private fun HpBar(left: Int, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 0 until BUG_SEGMENTS) {
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (i < left) Color(0xFFC8432A) else Color(0x33C8432A)))
        }
    }
}

@Composable
private fun ComboPill(c: Int, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(5.dp)).background(if (c >= 6) Palette.hazard else Color(0xFFFFA477)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text("COMBO x$c", color = Palette.hazardDark, style = MaterialTheme.typography.labelMedium)
    }
}

@Suppress("unused") private val keep = Modifier.offset(0.dp, 0.dp).size(0.dp)
