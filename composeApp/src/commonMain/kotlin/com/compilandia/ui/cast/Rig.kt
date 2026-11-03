package com.compilandia.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import com.compilandia.ui.ThemeState
import com.compilandia.ui.voice.Narrator
import kotlin.math.exp
import kotlin.random.Random

/** Humor do personagem: muda olhos, boca e postura. */
enum class Mood { NEUTRAL, HAPPY, SAD, SURPRISED, THINK }

/**
 * Formas de boca, nesta ordem no arquivo de índice:
 * 0 fechada, 1 A aberta, 2 E larga, 3 O redonda, 4 F nos dentes, 5 L/T, 6 CH.
 * Largura e altura em unidades do desenho.
 */
val mouthShapes: List<Pair<Float, Float>> = listOf(
    58f to 9f, 54f to 40f, 70f to 24f, 34f to 36f, 56f to 13f, 48f to 19f, 42f to 27f,
)

/** Relógio de animação de um personagem: tempo, piscada e boca suavizada. Leia só dentro do Canvas. */
class RigClock {
    val time = mutableFloatStateOf(0f)
    val blink = mutableFloatStateOf(1f)
    val mouthW = mutableFloatStateOf(mouthShapes[0].first)
    val mouthH = mutableFloatStateOf(mouthShapes[0].second)
    val talking = mutableFloatStateOf(0f)
}

/**
 * Anima um personagem a cada quadro. [active] diz se a fala atual é deste personagem.
 * A boca persegue a forma alvo com suavização, como um rig de verdade: nada de troca seca.
 */
@Composable
fun rememberRigClock(animate: Boolean = true, active: () -> Boolean): RigClock {
    val clock = remember { RigClock() }
    if (animate) LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        var last = start
        var nextBlink = start + 1500L + Random.nextLong(2000)
        while (true) {
            val now = withFrameMillis { it }
            val dt = (now - last).coerceAtLeast(1L)
            last = now
            clock.time.floatValue = if (ThemeState.reduceMotion) 0f else (now - start) / 1000f
            val since = now - nextBlink
            clock.blink.floatValue = when {
                since < 0 -> 1f
                since < 70 -> 1f - since / 70f
                since < 150 -> (since - 70) / 80f
                else -> { nextBlink = now + 1800L + Random.nextLong(3200); 1f }
            }
            val on = active()
            val (w, h) = mouthShapes[if (on) Narrator.viseme().coerceIn(0, mouthShapes.lastIndex) else 0]
            val k = 1f - exp(-dt / 38f)
            clock.mouthW.floatValue += (w - clock.mouthW.floatValue) * k
            clock.mouthH.floatValue += (h - clock.mouthH.floatValue) * k
            clock.talking.floatValue += ((if (on) 1f else 0f) - clock.talking.floatValue) * k
        }
    }
    return clock
}
