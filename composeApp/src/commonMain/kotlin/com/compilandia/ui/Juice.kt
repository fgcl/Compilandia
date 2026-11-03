package com.compilandia.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.resources.Res
import com.compilandia.resources.emote_face_sad
import com.compilandia.resources.emote_heart
import com.compilandia.resources.emote_idea
import com.compilandia.resources.emote_star
import com.compilandia.resources.fx_magic_02
import com.compilandia.resources.fx_spark_05
import com.compilandia.resources.fx_star_04
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * "Juice": a camada que faz o jogo PARECER um jogo — confete, tremida, combo, mascote reagindo,
 * som e vibração. Todo acerto/erro dos minijogos passa por aqui.
 */
const val OVERCLOCK = 5

/** O multiplicador de uma sequência de acertos. */
fun multDe(c: Int): Int = when {
    c >= 12 -> 8
    c >= 8 -> 5
    c >= OVERCLOCK -> 3
    c >= 3 -> 2
    else -> 1
}

object Juice {
    enum class Mood { HAPPY, HURT, IDEA, LOVE }
    data class Burst(val id: Long, val big: Boolean, val seed: Int)

    val bursts = mutableStateListOf<Burst>()
    var combo by mutableIntStateOf(0)
    var bestCombo by mutableIntStateOf(0)
    var shakeTick by mutableIntStateOf(0)
    var hapticTick by mutableIntStateOf(0)
    var mood by mutableStateOf<Mood?>(null)
    var moodTick by mutableIntStateOf(0)
    /** Contadores que o palco dos jogos escuta. */
    var hitTick by mutableIntStateOf(0)
    var missTick by mutableIntStateOf(0)
    /** Com o palco na tela, o Byte já aparece nele: o mascote de canto fica escondido. */
    var arenas by mutableIntStateOf(0)
    val inArena: Boolean get() = arenas > 0
    /** Palcos que já mostram a sequência do jeito deles. Sem nenhum, o selo aparece no topo. */
    var palcos by mutableIntStateOf(0)
    /** Fala curta pedida agora (acerto em sequência, erro). */
    var bark by mutableStateOf<com.compilandia.content.Fala?>(null)
    private var counter = 0L

    /** Cinco acertos seguidos: a tela esquenta e o multiplicador dispara. */
    val overclock: Boolean get() = combo >= OVERCLOCK
    val mult: Int get() = multDe(combo)

    fun correct() {
        combo++
        if (combo > bestCombo) bestCombo = combo
        // o tom sobe a cada acerto seguido: é o que faz a sequência virar música
        val tom = 1f + combo.coerceAtMost(14) * 0.05f
        Sound.play(if (combo >= 3 && combo % 3 == 0) Sfx.COMBO else Sfx.CORRECT, tom)
        if (combo == OVERCLOCK) Sound.play(Sfx.IMPACT, 1.2f)
        if (!inArena) burst(big = combo >= 5)
        if (combo % 3 == 0) {
            react(Mood.HAPPY)
            if (inArena) bark = com.compilandia.content.Falas.acerto.random()
        }
        hitTick++
        hapticTick++
    }

    fun wrong() {
        if (combo >= 3 && inArena) bark = com.compilandia.content.Falas.erro.random()
        combo = 0
        Sound.play(Sfx.WRONG)
        com.compilandia.ui.fx.Fx.glitch()
        shakeTick++
        missTick++
        hapticTick++
        react(Mood.HURT)
    }

    fun levelUp() { Sound.play(Sfx.LEVEL_UP); burst(true); react(Mood.LOVE) }
    fun victory() { Sound.play(Sfx.VICTORY); burst(true); burst(true); react(Mood.HAPPY) }
    fun idea() { Sound.play(Sfx.WHOOSH); react(Mood.IDEA) }
    fun click() = Sound.play(Sfx.CLICK)
    fun drop() = Sound.play(Sfx.DROP)
    fun pick() = Sound.play(Sfx.PICK)

    fun burst(big: Boolean) {
        if (ThemeState.reduceMotion) return
        bursts += Burst(counter++, big, Random.nextInt())
        while (bursts.size > 4) bursts.removeAt(0)
    }

    fun react(m: Mood) { mood = m; moodTick++ }
}

private val confettiColors = listOf(Color(0xFFFF7A45), Color(0xFFFACC15), Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF7C3AED))

/** Sobreposição global: partículas, combo e mascote. Coloque por cima de tudo no App. */
@Composable
fun BoxScope.JuiceOverlay() {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Juice.hapticTick) { if (Juice.hapticTick > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    for (b in Juice.bursts.toList()) key(b.id) { ConfettiBurst(b) { Juice.bursts.remove(b) } }

    // fala curta do Byte durante o jogo
    LaunchedEffect(Juice.bark) { Juice.bark?.let { com.compilandia.ui.voice.Narrator.say(it) } }

    // combo
    val c = Juice.combo
    AnimatedVisibility(
        visible = c >= 2 && Juice.inArena && Juice.palcos == 0, modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp),
        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(), exit = fadeOut(),
    ) {
        val pulse by animateFloatAsState(if (c % 2 == 0) 1.12f else 1f, spring(Spring.DampingRatioHighBouncy))
        Box(
            Modifier.scale(pulse).clip(RoundedCornerShape(50)).background(Brush3(c)).border(3.dp, Palette.onAccent, RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) { Text("🔥 COMBO x$c", color = Palette.onAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = LocalFonts.current.display) }
    }

    // mascote
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Juice.moodTick) {
        if (Juice.moodTick == 0) return@LaunchedEffect
        visible = true
        kotlinx.coroutines.delay(1300)
        visible = false
    }
    AnimatedVisibility(
        visible = visible && Juice.mood != null && !Juice.inArena, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 70.dp),
        enter = slideInHorizontally { it } + fadeIn(), exit = slideOutHorizontally { it } + fadeOut(),
    ) {
        val m = Juice.mood ?: Juice.Mood.HAPPY
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(imageResource(emoteFor(m)), null, Modifier.size(40.dp), filterQuality = FilterQuality.None)
            com.compilandia.ui.cast.ByteFigure(
                Modifier.size(84.dp),
                mood = if (m == Juice.Mood.HURT) com.compilandia.ui.cast.Mood.SAD else com.compilandia.ui.cast.Mood.HAPPY,
                hop = Juice.moodTick,
            )
        }
    }
}

private fun Brush3(c: Int): Color = when { c >= 9 -> Color(0xFFEC4899); c >= 6 -> Color(0xFF7C3AED); c >= 3 -> Color(0xFFFF7A45); else -> Color(0xFF14B8A6) }

private fun emoteFor(m: Juice.Mood): DrawableResource = when (m) {
    Juice.Mood.HAPPY -> Res.drawable.emote_star
    Juice.Mood.HURT -> Res.drawable.emote_face_sad
    Juice.Mood.IDEA -> Res.drawable.emote_idea
    Juice.Mood.LOVE -> Res.drawable.emote_heart
}

@Composable
private fun ConfettiBurst(b: Juice.Burst, onDone: () -> Unit) {
    val star = painterResource(Res.drawable.fx_star_04)
    val spark = painterResource(Res.drawable.fx_spark_05)
    val magic = painterResource(Res.drawable.fx_magic_02)
    val t = remember { Animatable(0f) }
    LaunchedEffect(b.id) { t.animateTo(1f, tween(if (b.big) 1400 else 900, easing = LinearEasing)); onDone() }
    val rnd = remember { Random(b.seed) }
    val n = if (b.big) 36 else 16
    val parts = remember {
        List(n) {
            val ang = rnd.nextDouble(0.0, 2 * PI)
            val speed = rnd.nextDouble(0.35, 1.0).toFloat()
            Triple(ang, speed, rnd.nextInt(confettiColors.size)) to (rnd.nextInt(3) to rnd.nextFloat() * 360f)
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val p = t.value
        val cx = size.width / 2; val cy = size.height * 0.42f
        val reach = size.minDimension * (if (b.big) 0.55f else 0.35f)
        for ((a, extra) in parts) {
            val (ang, speed, ci) = a
            val (kind, rot) = extra
            val dist = reach * speed * (1 - (1 - p) * (1 - p))
            val x = cx + (cos(ang) * dist).toFloat()
            val y = cy + (sin(ang) * dist).toFloat() + size.height * 0.25f * p * p     // gravidade
            val s = (if (b.big) 46f else 34f) * (1f - 0.5f * p) * density
            val painter = when (kind) { 0 -> star; 1 -> spark; else -> magic }
            translate(x - s / 2, y - s / 2) {
                rotate(rot + 360f * p, pivot = androidx.compose.ui.geometry.Offset(s / 2, s / 2)) {
                    with(painter) { draw(Size(s, s), alpha = 1f - p, colorFilter = ColorFilter.tint(confettiColors[ci])) }
                }
            }
        }
    }
}

/** Deslocamento de "tremida" para aplicar no conteúdo raiz. */
@Composable
fun shakeOffset(): IntOffset {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Juice.shakeTick) {
        if (Juice.shakeTick == 0 || ThemeState.reduceMotion) return@LaunchedEffect
        for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) a.animateTo(x, tween(45))
    }
    return IntOffset(a.value.roundToInt(), 0)
}

@Suppress("unused") private val keepOffset = Modifier.offset(0.dp, 0.dp)
