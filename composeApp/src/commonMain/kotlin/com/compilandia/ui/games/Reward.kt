package com.compilandia.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.resources.Res
import com.compilandia.resources.prop_crate_closed
import com.compilandia.resources.prop_crate_open
import com.compilandia.resources.prop_gear
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.sin

private val RAY = Color(0xFFF5B700)

/** Fim de partida: a caixa treme, abre, as engrenagens saltam e os números sobem. É o único lugar com efeito forte. */
@Composable
fun RewardHero(stars: Int, xp: Int, gears: Int, title: String, modifier: Modifier = Modifier, sequencia: Int = 0, bonus: Int = 0) {
    val still = ThemeState.reduceMotion
    var open by remember { mutableStateOf(still) }
    val shake = remember { Animatable(0f) }
    val burst = remember { Animatable(if (still) 1f else 0f) }
    val pop = remember { Animatable(if (still) 1f else 0.6f) }
    val spin = remember { Animatable(0f) }
    var shownXp by remember { mutableIntStateOf(if (still) xp else 0) }
    var shownGears by remember { mutableIntStateOf(if (still) gears else 0) }
    var starsShown by remember { mutableIntStateOf(if (still) stars else 0) }
    var somou by remember { mutableStateOf(still) }
    LaunchedEffect(Unit) {
        if (still) return@LaunchedEffect
        launch { spin.animateTo(360f, tween(30000, easing = LinearEasing)) }
        delay(260)
        for (x in listOf(8f, -8f, 6f, -6f, 3f, 0f)) shake.animateTo(x, tween(55))
        open = true
        Sound.play(Sfx.STAR)
        launch { pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
        launch { burst.animateTo(1f, tween(1100, easing = LinearEasing)) }
        for (i in 1..stars) {
            delay(180)
            starsShown = i
            Sound.play(Sfx.CLICK)
        }
        val steps = 24
        for (i in 1..steps) {
            shownXp = xp * i / steps
            shownGears = (gears - bonus) * i / steps
            if (i % 2 == 0) Sound.play(Sfx.TICK, 0.8f + i * 0.05f)
            delay(22)
        }
        // A sequência entra por cima: cada engrenagem a mais sobe o tom.
        for (i in 1..bonus) {
            delay(90)
            shownGears = gears - bonus + i
            Sound.play(Sfx.TICK, 1.1f + i * 0.09f)
        }
        if (bonus > 0) { Sound.play(Sfx.COMBO); somou = true }
    }
    Box(modifier.fillMaxWidth().height(330.dp).clipToBounds(), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxWidth().height(330.dp).rotate(spin.value)) {
            val c = Offset(size.width / 2, size.height * 0.58f)
            val r = size.width
            for (i in 0 until 10) {
                rotate(i * 36f, c) {
                    drawPath(Path().apply { moveTo(c.x, c.y); lineTo(c.x - r * 0.12f, c.y - r); lineTo(c.x + r * 0.12f, c.y - r); close() }, RAY.copy(alpha = 0.09f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..3) Star(Modifier.size(if (i == 2) 62.dp else 50.dp), filled = i <= starsShown)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.offset(y = 66.dp))
        Box(Modifier.offset(x = shake.value.dp, y = 104.dp).size(180.dp).scale(pop.value), contentAlignment = Alignment.Center) {
            Image(painterResource(if (open) Res.drawable.prop_crate_open else Res.drawable.prop_crate_closed), null, Modifier.size(170.dp))
        }
        // engrenagens saltando da caixa
        val b = burst.value
        if (open && b > 0f && b < 1f) {
            for (i in 0 until 9) {
                val ang = -1.45f + (i - 4) * 0.32f
                val v = 118f + (i % 3) * 34f
                val x = cos(ang) * v * b
                val y = sin(ang) * v * b + 180f * b * b
                Image(
                    painterResource(Res.drawable.prop_gear), null,
                    Modifier.offset(x.dp, (176f + y).dp).size(30.dp).rotate(b * 540f).alpha((1.6f - 1.6f * b).coerceIn(0f, 1f)),
                )
            }
        }
        if (bonus > 0 && somou) Text(
            "Sequência de $sequencia: +$bonus", color = Palette.orange, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-38).dp),
        )
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("+$shownXp XP", color = Palette.teal, style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Image(painterResource(Res.drawable.prop_gear), "Engrenagens", Modifier.size(26.dp))
                Text("+$shownGears", color = Palette.yellow, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") Modifier.background(Color.Transparent)
}

@Composable
private fun Star(modifier: Modifier, filled: Boolean) {
    val s = remember { Animatable(0f) }
    LaunchedEffect(filled) { if (filled) s.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium)) }
    val empty = Palette.drawerLo
    val full = Palette.hazard
    Canvas(modifier) {
        fun star(c: Color, k: Float) {
            val u = size.width / 24f * k
            val o = Offset(size.width / 2 - 12f * u, size.height / 2 - 12f * u)
            val pts = listOf(12f to 1f, 15.2f to 8.2f, 23f to 9f, 17.1f to 14.2f, 18.8f to 21.9f, 12f to 18f, 5.2f to 21.9f, 6.9f to 14.2f, 1f to 9f, 8.8f to 8.2f)
            drawPath(Path().apply {
                pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(o.x + x * u, o.y + y * u) else lineTo(o.x + x * u, o.y + y * u) }
                close()
            }, c)
        }
        star(empty, 1f)
        if (s.value > 0f) star(full, s.value)
    }
}
