package com.compilandia.ui.fx

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val FOGO = Color(0xFFFF7A28)
private val BRASA = Color(0xFFF5B700)
private val CIANO = Color(0xFF00E5C8)
private val SANGUE = Color(0xFFFF3B30)

/** Efeitos de tela cheia. Todos respeitam "Reduzir animações". */
object Fx {
    data class Portao(val rotulo: String, val cor: Color, val depois: () -> Unit)

    var glitchTick by mutableIntStateOf(0)
    var portao by mutableStateOf<Portao?>(null)
    private val carimbos = listOf("TOKEN INESPERADO", "ERRO DE COMPILAÇÃO", "FALHA NA PILHA", "CONFLITO", "SEGMENTATION FAULT")
    var carimbo by mutableStateOf("")
        private set

    fun glitch() {
        if (ThemeState.reduceMotion) return
        carimbo = carimbos.random()
        Sound.play(Sfx.GLITCH)
        glitchTick++
    }

    /** Fecha o portão, troca de tela com ele fechado e abre de novo. */
    fun porta(rotulo: String, cor: Color, depois: () -> Unit) {
        if (ThemeState.reduceMotion) depois() else portao = Portao(rotulo, cor, depois)
    }
}

@Composable
fun BoxScope.FxOverlay() {
    if (ThemeState.reduceMotion) return
    if (Juice.overclock && Juice.inArena) Calor()
    Glitch()
    Fx.portao?.let { PortaoDeOficina(it) { Fx.portao = null } }
}

/** A tela esquenta: bordas em brasa e fagulhas subindo. */
@Composable
private fun BoxScope.Calor() {
    val inf = rememberInfiniteTransition()
    val pulso by inf.animateFloat(0.55f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse))
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart))
    val intensidade = ((Juice.combo - 4) / 6f).coerceIn(0.35f, 1f)
    Canvas(Modifier.fillMaxSize()) {
        val a = 0.5f * pulso * intensidade
        val borda = 70.dp.toPx() * (0.7f + 0.5f * intensidade)
        drawRect(Brush.verticalGradient(listOf(FOGO.copy(alpha = a), Color.Transparent), 0f, borda), size = Size(size.width, borda))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, FOGO.copy(alpha = a)), size.height - borda, size.height), Offset(0f, size.height - borda), Size(size.width, borda))
        drawRect(Brush.horizontalGradient(listOf(FOGO.copy(alpha = a), Color.Transparent), 0f, borda), size = Size(borda, size.height))
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, FOGO.copy(alpha = a)), size.width - borda, size.width), Offset(size.width - borda, 0f), Size(borda, size.height))
        val rnd = Random(7)
        repeat(26) {
            val x = rnd.nextFloat() * size.width
            val fase = (t + rnd.nextFloat()) % 1f
            val y = size.height * (1f - fase)
            val r = (1.2f + rnd.nextFloat() * 2.4f) * density
            drawCircle((if (it % 3 == 0) BRASA else FOGO).copy(alpha = (1f - fase) * intensidade), r, Offset(x + sin(fase * 9f + it) * 12f * density, y))
        }
    }
}

/** Errou: a tela falha como código quebrado por um instante. */
@Composable
private fun BoxScope.Glitch() {
    val p = remember { Animatable(1f) }
    val c = remember { Animatable(0f) }
    LaunchedEffect(Fx.glitchTick) {
        if (Fx.glitchTick == 0) return@LaunchedEffect
        launch {
            c.snapTo(2.3f)
            c.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        }
        p.snapTo(0f)
        p.animateTo(1f, tween(620, easing = LinearEasing))
    }
    val v = p.value
    if (v >= 1f) return
    val sementeBase = Fx.glitchTick * 31
    Canvas(Modifier.fillMaxSize()) {
        // nos primeiros quadros a imagem rasga; depois sobra só a varredura
        val forte = (1f - v * 2.6f).coerceIn(0f, 1f)
        val rnd = Random(sementeBase + (v * 14).toInt())
        repeat(9) {
            val y = rnd.nextFloat() * size.height
            val h = (6f + rnd.nextFloat() * 42f) * density
            val dx = (rnd.nextFloat() - 0.5f) * 90f * density * forte
            drawRect(CIANO.copy(alpha = 0.32f * forte), Offset(dx, y), Size(size.width, h))
            drawRect(SANGUE.copy(alpha = 0.32f * forte), Offset(-dx, y + 4f * density), Size(size.width, h * 0.6f))
        }
        var y = 0f
        val passo = 5.dp.toPx()
        while (y < size.height) {
            drawRect(Color.Black.copy(alpha = 0.2f * (1f - v)), Offset(0f, y), Size(size.width, 2.dp.toPx()))
            y += passo
        }
    }
    if (Juice.inArena && v < 0.85f) {
        Box(
            Modifier.align(Alignment.Center).scale(c.value).rotate(-4f).clip(RoundedCornerShape(6.dp)).background(Color(0xFFC8432A)).padding(horizontal = 14.dp, vertical = 9.dp),
        ) { Text(Fx.carimbo, color = Color.White, fontFamily = LocalFonts.current.display, fontWeight = FontWeight.Bold, fontSize = 27.sp, maxLines = 1) }
    }
}

/** O portão da oficina: fecha com faíscas, mostra para onde vai e abre na tela nova. */
@Composable
private fun BoxScope.PortaoDeOficina(pedido: Fx.Portao, onFim: () -> Unit) {
    val fecha = remember { Animatable(0f) }
    val faisca = remember { Animatable(0f) }
    val placa = remember { Animatable(0f) }
    LaunchedEffect(pedido) {
        Sound.play(Sfx.GATE)
        fecha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        launch { faisca.animateTo(1f, tween(520, easing = LinearEasing)) }
        launch { placa.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
        pedido.depois()
        delay(620)
        placa.animateTo(0f, tween(120))
        Sound.play(Sfx.GATE, 1.3f)
        fecha.animateTo(0f, tween(340, easing = FastOutSlowInEasing))
        onFim()
    }
    val steel = Palette.drawer
    val steelLo = Palette.drawerLo
    val hi = Palette.drawerHi
    val hz = Palette.hazard
    val dk = Palette.hazardDark
    Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, null) { }) {
        Canvas(Modifier.fillMaxSize()) {
            val h = size.height / 2 * fecha.value
            folha(0f, h, steel, steelLo, hi, hz, dk, cima = true)
            folha(size.height - h, h, steel, steelLo, hi, hz, dk, cima = false)
            val f = faisca.value
            if (f > 0f && f < 1f) {
                val c = Offset(size.width / 2, size.height / 2)
                val rnd = Random(3)
                repeat(46) {
                    val a = rnd.nextFloat() * 6.283f
                    val v = (70f + rnd.nextFloat() * 320f) * density
                    val x = rnd.nextFloat() * size.width
                    val base = if (it % 2 == 0) Offset(x, c.y) else c
                    val q = base + Offset(cos(a), sin(a)) * (v * f) + Offset(0f, 200f * density * f * f)
                    drawLine(hz.copy(alpha = 1f - f), q, q + Offset(cos(a), sin(a)) * (9f * density), 2.dp.toPx())
                }
            }
        }
        if (placa.value > 0.02f) Box(
            Modifier.align(Alignment.Center).scale(placa.value).clip(RoundedCornerShape(6.dp)).background(Palette.tag).padding(horizontal = 16.dp, vertical = 11.dp),
        ) { Text(pedido.rotulo, color = Palette.tagInk, style = MaterialTheme.typography.headlineSmall, maxLines = 1) }
    }
    @Suppress("UNUSED_EXPRESSION") pedido.cor
}

private fun DrawScope.folha(y: Float, h: Float, steel: Color, lo: Color, hi: Color, hz: Color, dk: Color, cima: Boolean) {
    if (h <= 0f) return
    drawRect(Brush.verticalGradient(listOf(steel, lo), y, y + h), Offset(0f, y), Size(size.width, h))
    val nervura = 42.dp.toPx()
    var ly = y + (if (cima) h % nervura else nervura)
    while (ly < y + h) {
        drawRect(hi.copy(alpha = 0.35f), Offset(0f, ly), Size(size.width, 2.dp.toPx()))
        ly += nervura
    }
    val faixa = 18.dp.toPx()
    val fy = if (cima) y + h - faixa else y
    drawRect(dk, Offset(0f, fy), Size(size.width, faixa))
    val w = 18.dp.toPx()
    var x = -faixa
    while (x < size.width) {
        drawPath(Path().apply { moveTo(x, fy + faixa); lineTo(x + faixa, fy); lineTo(x + faixa + w, fy); lineTo(x + w, fy + faixa); close() }, hz)
        x += w * 2
    }
}

/** O golpe final no chefão: onda de choque, engrenagens voando e o letreiro. */
@Composable
fun BoxScope.ExplosaoChefao() {
    val p = remember { Animatable(0f) }
    val letreiro = remember { Animatable(2.4f) }
    LaunchedEffect(Unit) {
        Sound.play(Sfx.IMPACT)
        if (ThemeState.reduceMotion) { p.snapTo(1f); letreiro.snapTo(1f); return@LaunchedEffect }
        launch { p.animateTo(1f, tween(1900, easing = LinearEasing)) }
        delay(240)
        Sound.play(Sfx.IMPACT, 0.7f)
        letreiro.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    val v = p.value
    val glow = Palette.yellow
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height * 0.34f)
        if (v < 0.12f) drawRect(Color.White.copy(alpha = (1f - v / 0.12f) * 0.85f))
        for (i in 0..2) {
            val k = ((v - i * 0.12f) / 0.6f).coerceIn(0f, 1f)
            if (k > 0f && k < 1f) drawCircle(
                listOf(glow, Color(0xFF8FF5E2), Color.White)[i].copy(alpha = 1f - k), size.maxDimension * 0.8f * k, c,
                style = androidx.compose.ui.graphics.drawscope.Stroke((1f - k) * 16.dp.toPx()),
            )
        }
        val rnd = Random(11)
        repeat(40) {
            val a = rnd.nextFloat() * 6.283f
            val vel = (130f + rnd.nextFloat() * 420f) * density
            val q = c + Offset(cos(a), sin(a)) * (vel * v) + Offset(0f, 420f * density * v * v)
            val r = (3f + rnd.nextFloat() * 6f) * density * (1f - v)
            drawCircle(if (it % 2 == 0) glow else Palette.orange, r, q)
        }
    }
    Text(
        "COMPILOU!", Modifier.align(Alignment.TopCenter).padding(top = 150.dp).scale(letreiro.value),
        color = Palette.yellow, fontFamily = LocalFonts.current.display, fontWeight = FontWeight.Bold, fontSize = 62.sp,
    )
}
