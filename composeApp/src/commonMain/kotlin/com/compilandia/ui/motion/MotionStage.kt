package com.compilandia.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.compilandia.content.Box
import com.compilandia.content.Camera
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.TITULO_ID
import com.compilandia.content.Tint
import com.compilandia.content.RECORD_HEAD
import com.compilandia.content.RECORD_ROW
import com.compilandia.content.recordRowY
import com.compilandia.content.Wire
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

const val STAGE_W = 360f

/** Altura segura: em telas baixas o palco encolhe para caber. */
const val STAGE_H = 410f

fun tintColor(t: Tint): Color = when (t) {
    Tint.NONE -> Palette.white
    Tint.TEAL -> Palette.teal
    Tint.ORANGE -> Palette.orange
    Tint.YELLOW -> Palette.yellow
    Tint.RED -> Palette.red
    Tint.GREEN -> Palette.green
    Tint.BLUE -> Palette.blue
    Tint.PINK -> Palette.pink
    Tint.DARK -> Palette.paperDark
}

/** Quanto descer as peças para o conjunto ficar no meio do palco. O título fica no alto. */
private fun centering(shot: Shot, stageH: Float): Float {
    // Com câmera, as coordenadas valem como estão: é ela que enquadra.
    if (shot.camera != null) return 0f
    val corpo = shot.boxes.filter { it.id != TITULO_ID }
    if (corpo.isEmpty()) return 0f
    val topo = corpo.minOf { it.y - it.h / 2 }
    val base = corpo.maxOf { it.y + it.h / 2 }
    val alvo = (56f + stageH - (topo + base)) / 2 - 8f
    // Palco mais alto que o padrão (celular comprido): o conjunto pode descer até o meio dele.
    return alvo.coerceIn(0f, 70f + max(0f, stageH - STAGE_H))
}

/**
 * Altura lógica do palco da aula numa área de largura [w] e altura [h] (em qualquer unidade).
 * Quando a largura limita a escala, o palco fica mais alto que 410 e as peças descem para o meio (T3).
 */
fun alturaLogica(w: Float, h: Float): Float = max(STAGE_H, h / minOf(w / STAGE_W, h / STAGE_H))

/**
 * Quanto as peças descem a mais num palco de altura [stageH] do que no palco padrão.
 * O Byte em cena soma isso à posição dele para continuar ao lado das peças.
 */
fun descidaExtra(shot: Shot, stageH: Float): Float {
    val c = shot.camera
    if (c != null) return if (c.zoom == 1f) 0f else (stageH - STAGE_H) / 2
    return centering(shot, stageH) - centering(shot, STAGE_H)
}

/** Onde uma peça aparece na tela, em pixels. Fora daqui só os testes precisam disso. */
fun stagePoint(shot: Shot, id: String, width: Float, height: Float, stageH: Float = STAGE_H): Offset {
    val b = shot.boxes.first { it.id == id }
    val s = minOf(width / STAGE_W, height / stageH)
    val x = if (b.look == Look.CODE) b.x + b.w / 2 else b.x
    val p = olhar(shot.camera, stageH, Offset(x, b.y + centering(shot, stageH)))
    return Offset((width - STAGE_W * s) / 2 + p.x * s, p.y * s)
}

/** Leva um ponto do palco para onde a câmera o mostra. */
private fun olhar(c: Camera?, stageH: Float, p: Offset): Offset {
    if (c == null || c.zoom == 1f) return p
    return Offset(STAGE_W / 2 + (p.x - c.x) * c.zoom, stageH / 2 + (p.y - c.y) * c.zoom)
}

private class Live(
    val box: Box,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val alpha: Float,
    val scale: Float,
    val oldText: String?,
    val textMix: Float,
    val tint: Color,
    /** De 0 a 1 durante o nascimento da peça; 1 para quem já estava lá. */
    val age: Float = 1f,
    /** A forma de antes, quando a peça troca de desenho (bolinha vira caixa). Some enquanto a nova aparece. */
    val oldLook: Box? = null,
    val lookMix: Float = 1f,
)

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

/** O canvas já está escalado para unidades do palco: o texto é medido nessas unidades, sem densidade. */
private fun DrawScope.u(v: Float): TextUnit = (v / density).sp
private fun easeInOut(t: Float) = if (t < .5f) 4 * t * t * t else 1 - ((-2 * t + 2).let { it * it * it }) / 2
private fun easeBack(t: Float): Float { val c = 1.70158f; val s = t - 1; return 1 + (c + 1) * s * s * s + c * s * s }
private fun clamp01(t: Float) = t.coerceIn(0f, 1f)
private fun mix(a: Color, b: Color, t: Float) = Color(lerp(a.red, b.red, t), lerp(a.green, b.green, t), lerp(a.blue, b.blue, t), lerp(a.alpha, b.alpha, t))

/**
 * O palco. Recebe uma imagem de cada vez e anima a diferença: o que continua desliza, o que nasce
 * salta, o que sai cai. É o mesmo truque do "movimento mágico" de apresentações, com dados de verdade.
 * [tappable] são as peças tocáveis agora; [marked] são as já resolvidas.
 */
@Composable
fun MotionStage(
    shot: Shot,
    modifier: Modifier = Modifier,
    tappable: Set<String> = emptySet(),
    marked: Set<String> = emptySet(),
    stageH: Float = STAGE_H,
    center: Boolean = true,
    onTap: (String) -> Unit = {},
) {
    val tm = rememberTextMeasurer(cacheSize = 160)
    val fonts = LocalFonts.current
    var from by remember { mutableStateOf(shot) }
    var to by remember { mutableStateOf(shot) }
    val p = remember { Animatable(1f) }
    LaunchedEffect(shot) {
        if (shot != to) {
            from = to
            to = shot
            p.snapTo(0f)
            p.animateTo(1f, tween(if (ThemeState.reduceMotion) 120 else shot.ms, easing = LinearEasing))
        }
    }
    val pulse by rememberInfiniteTransition().animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
    )
    val corre by rememberInfiniteTransition().animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
    )
    Canvas(
        modifier.pointerInput(to, tappable) {
            if (tappable.isNotEmpty()) detectTapGestures { pos ->
                val s = minOf(size.width / STAGE_W, size.height / stageH)
                val left = (size.width - STAGE_W * s) / 2
                val down = if (center) centering(to, stageH) else 0f
                val zoom = to.camera?.zoom ?: 1f
                fun Box.cx() = if (look == Look.CODE) x + w / 2 else x
                fun dist(b: Box): Float {
                    val c = olhar(to.camera, stageH, Offset(b.cx(), b.y + down))
                    val dx = kotlin.math.abs(left + c.x * s - pos.x) - b.w * zoom * s / 2
                    val dy = kotlin.math.abs(c.y * s - pos.y) - b.h * zoom * s / 2
                    return max(dx, 0f) + max(dy, 0f)
                }
                val hit = to.boxes.filter { it.id in tappable }.minByOrNull { dist(it) }
                if (hit != null && dist(hit) < 14f * s) onTap(hit.id)
            }
        },
    ) {
        val s = minOf(size.width / STAGE_W, size.height / stageH)
        val t = p.value
        val down = if (center) lerp(centering(from, stageH), centering(to, stageH), easeInOut(clamp01(t / 0.72f))) else 0f
        val live = interpolate(from, to, t, down)
        val cam = mixCam(from.camera, to.camera, easeInOut(t), stageH)
        scale(s, (size.width - STAGE_W * s) / 2) {
            drawContext.canvas.save()
            drawContext.canvas.translate(STAGE_W / 2, stageH / 2)
            drawContext.canvas.scale(cam.zoom, cam.zoom)
            drawContext.canvas.translate(-cam.x, -cam.y)
            drawWires(from, to, t, live, tm, fonts.mono, corre)
            // Caixas ficam atrás de tudo: as peças caem dentro delas.
            for (l in live.values.sortedBy { if (it.box.look == Look.BIN) 0 else 1 }) {
                drawBox(l, tm, fonts.display, fonts.mono, pulse, l.box.id in tappable, l.box.id in marked)
            }
            drawContext.canvas.restore()
        }
    }
}

/** Sai do centro do palco e vai até o alvo, ou volta. Sem pulo no primeiro quadro. */
private fun mixCam(a: Camera?, b: Camera?, t: Float, stageH: Float): Camera {
    val p = a ?: Camera(STAGE_W / 2, stageH / 2, 1f)
    val q = b ?: Camera(STAGE_W / 2, stageH / 2, 1f)
    return Camera(lerp(p.x, q.x, t), lerp(p.y, q.y, t), lerp(p.zoom, q.zoom, t))
}

private inline fun DrawScope.scale(k: Float, left: Float, block: DrawScope.() -> Unit) {
    drawContext.canvas.save()
    drawContext.canvas.translate(left, 0f)
    drawContext.canvas.scale(k, k)
    block()
    drawContext.canvas.restore()
}

private fun interpolate(from: Shot, to: Shot, t: Float, down: Float): Map<String, Live> {
    val old = from.boxes.associateBy { it.id }
    fun dy(b: Box) = if (b.id == TITULO_ID) 0f else down
    val news = to.boxes.filter { it.id !in old }
    val gone = from.boxes.filter { b -> to.boxes.none { it.id == b.id } }
    val out = linkedMapOf<String, Live>()
    for (b in gone) {
        val k = clamp01(t / 0.45f)
        out[b.id] = Live(b, b.x, b.y + dy(b) + 26f * k * k, b.w, b.h, 1f - k, 1f - 0.2f * k, null, 1f, tintColor(b.tint))
    }
    val moveT = easeInOut(clamp01(t / 0.72f))
    var bornIndex = 0
    for (b in to.boxes) {
        val o = old[b.id]
        if (o == null) {
            // nascem em sequência: o olho acompanha a ordem
            val start = 0.18f + 0.62f * (bornIndex.toFloat() / max(news.size, 1))
            bornIndex++
            val k = clamp01((t - start) / 0.28f)
            val bang = b.look == Look.BANG
            out[b.id] = Live(b, b.x, b.y + dy(b), b.w, b.h, if (bang) 1f else clamp01(k * 2.2f), if (bang) 1f else easeBack(k).coerceAtLeast(0f), null, 1f, tintColor(b.tint), t)
        } else {
            val troca = o.look != b.look
            out[b.id] = Live(
                b, lerp(o.x, b.x, moveT), lerp(o.y, b.y, moveT) + dy(b), lerp(o.w, b.w, moveT), lerp(o.h, b.h, moveT), 1f, 1f,
                if (o.text != b.text && !troca) o.text else null, if (troca) 1f else clamp01(t / 0.5f), mix(tintColor(o.tint), tintColor(b.tint), moveT),
                oldLook = if (troca) o else null, lookMix = if (troca) moveT else 1f,
            )
        }
    }
    return out
}

private fun DrawScope.textAt(tm: TextMeasurer, text: String, c: Offset, style: TextStyle, alpha: Float, left: Boolean = false) {
    if (text.isEmpty() || alpha <= 0.01f) return
    val l = tm.measure(text, style.copy(color = style.color.copy(alpha = alpha)))
    drawText(l, topLeft = Offset(if (left) c.x else c.x - l.size.width / 2f, c.y - l.size.height / 2f))
}

private fun DrawScope.drawBox(l: Live, tm: TextMeasurer, display: FontFamily, mono: FontFamily, pulse: Float, tappable: Boolean, marked: Boolean) {
    val antes = l.oldLook
    if (antes != null) {
        // A forma velha se desfaz enquanto a nova se monta no mesmo lugar e no tamanho do meio do caminho.
        drawBox(Live(antes, l.x, l.y, l.w, l.h, l.alpha * (1f - l.lookMix), l.scale, null, 1f, l.tint), tm, display, mono, pulse, false, false)
        drawBox(Live(l.box, l.x, l.y, l.w, l.h, l.alpha * l.lookMix, l.scale, null, 1f, l.tint), tm, display, mono, pulse, tappable, marked)
        return
    }
    val b = l.box
    if (l.alpha <= 0.01f) return
    val w = l.w * l.scale
    val h = l.h * l.scale
    val tl = if (b.look == Look.CODE) Offset(l.x, l.y - h / 2) else Offset(l.x - w / 2, l.y - h / 2)
    val breath = if (b.hot && !ThemeState.reduceMotion) sin(pulse * 2 * PI.toFloat()) * 0.5f + 0.5f else 0f
    if (b.hot || tappable) {
        val grow = if (tappable) 3f + 7f * pulse else 3f + 4f * breath
        val a = if (tappable) (1f - pulse) * 0.8f else 0.6f
        // Linha de código mora colada na vizinha: o anel cresce para os lados, nunca por cima da outra linha.
        val gy = if (b.look == Look.CODE) 1f else grow
        drawRoundRect(Palette.yellow.copy(alpha = a * l.alpha), tl - Offset(grow, gy), Size(w + 2 * grow, h + 2 * gy), CornerRadius(if (b.look == Look.CODE) 6f else 10f), style = Stroke(2.5f))
    }
    val fg = if (l.tint == Palette.white || l.tint == Palette.paperDark) Palette.ink else Palette.on(l.tint)
    fun txt(s: String, base: TextStyle) {
        val mixT = l.textMix
        if (l.oldText != null) textAt(tm, l.oldText, Offset(l.x, l.y), base, l.alpha * (1f - mixT))
        textAt(tm, s, Offset(l.x, l.y), base, l.alpha * mixT)
    }
    when (b.look) {
        Look.CELL -> {
            drawRoundRect(l.tint.copy(alpha = l.alpha), tl, Size(w, h), CornerRadius(5f))
            drawRoundRect(Color.Black.copy(alpha = 0.22f * l.alpha), Offset(tl.x, tl.y + h - 3f), Size(w, 3f), CornerRadius(3f))
            txt(b.text, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = fg))
        }
        Look.TOKEN, Look.TRAIN -> {
            val fill = if (b.look == Look.TRAIN) Palette.yellow else l.tint
            if (b.look == Look.TRAIN) drawCircle(Palette.yellow.copy(alpha = 0.3f * l.alpha), max(w, h) * 0.8f, Offset(l.x, l.y))
            drawRoundRect(fill.copy(alpha = l.alpha), tl, Size(w, h), CornerRadius(8f))
            drawRoundRect(Color.Black.copy(alpha = 0.2f * l.alpha), Offset(tl.x, tl.y + h - 4f), Size(w, 4f), CornerRadius(4f))
            txt(b.text, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = if (b.look == Look.TRAIN) Palette.hazardDark else fg))
        }
        Look.NODE -> {
            drawRoundRect(l.tint.copy(alpha = l.alpha), tl, Size(w, h), CornerRadius(h / 2))
            txt(b.text, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = fg))
        }
        Look.STATE, Look.ACCEPT -> {
            val r = w / 2
            drawCircle(l.tint.copy(alpha = l.alpha), r, Offset(l.x, l.y))
            drawCircle(Palette.ink.copy(alpha = l.alpha), r, Offset(l.x, l.y), style = Stroke(2.5f))
            if (b.look == Look.ACCEPT) drawCircle(Palette.ink.copy(alpha = l.alpha), r - 5f, Offset(l.x, l.y), style = Stroke(2f))
            txt(b.text, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = fg))
        }
        Look.LABEL -> {
            val c = if (b.tint == Tint.NONE) Palette.inkSoft else tintColor(b.tint)
            txt(b.text, TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = u(b.size), color = c))
        }
        Look.TITLE -> txt(b.text, TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = Palette.ink))
        Look.CODE -> {
            if (b.hot) drawRoundRect(Palette.yellow.copy(alpha = 0.22f * l.alpha), tl, Size(w, h), CornerRadius(4f))
            textAt(tm, b.text, Offset(tl.x + 6f, l.y), TextStyle(fontFamily = mono, fontSize = u(b.size), fontWeight = FontWeight.Bold, color = if (b.tint == Tint.NONE) Palette.ink else tintColor(b.tint)), l.alpha, left = true)
        }
        Look.GAP -> {
            val c = if (marked) Palette.green else Palette.yellow
            drawRoundRect(c.copy(alpha = (if (tappable || marked) 0.95f else 0.3f) * l.alpha), tl, Size(w, h), CornerRadius(w / 2))
        }
        Look.GHOST -> {
            val c = if (marked) Palette.green else Palette.yellow
            drawRoundRect(c.copy(alpha = 0.14f * l.alpha), tl, Size(w, h), CornerRadius(8f))
            drawRoundRect(c.copy(alpha = l.alpha), tl, Size(w, h), CornerRadius(8f), style = Stroke(2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), pulse * 16f)))
            textAt(tm, b.text, Offset(l.x, l.y), TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = c), l.alpha)
        }
        Look.BANG -> drawBang(b, l, tm, display)
        Look.RECORD -> drawRecord(l, tl, w, h, tm, mono)
        Look.BIN -> {
            drawRoundRect(l.tint.copy(alpha = 0.9f * l.alpha), tl, Size(w, h), CornerRadius(8f))
            drawRoundRect(Palette.ink.copy(alpha = 0.35f * l.alpha), tl, Size(w, h), CornerRadius(8f), style = Stroke(2f))
            textAt(tm, b.text, Offset(l.x, l.y + h / 2 - 14f), TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = Palette.inkSoft), l.alpha)
        }
    }
    if (marked && b.look != Look.GAP) drawRoundRect(Palette.green.copy(alpha = l.alpha), tl - Offset(2f, 2f), Size(w + 4f, h + 4f), CornerRadius(9f), style = Stroke(3f))
}

/**
 * Caixa com compartimentos, como um diagrama de classes ou um estado do bison --graph:
 * o nome numa faixa colorida e uma linha por item embaixo. A linha acesa fica amarela.
 */
private fun DrawScope.drawRecord(l: Live, tl: Offset, w: Float, h: Float, tm: TextMeasurer, mono: FontFamily) {
    val b = l.box
    val a = l.alpha
    val corpo = Palette.paper
    val borda = if (b.hot) Palette.yellow else Palette.drawer
    val faixa = if (b.tint == Tint.NONE) Palette.drawer else l.tint
    val k = h / (RECORD_HEAD + RECORD_ROW * b.rows.size + 4f).coerceAtLeast(1f)
    drawRoundRect(Color.Black.copy(alpha = 0.25f * a), tl + Offset(0f, 3f), Size(w, h), CornerRadius(7f))
    drawRoundRect(corpo.copy(alpha = a), tl, Size(w, h), CornerRadius(7f))
    drawRoundRect(faixa.copy(alpha = a), tl, Size(w, RECORD_HEAD * k), CornerRadius(7f, 7f))
    drawRect(faixa.copy(alpha = a), Offset(tl.x, tl.y + RECORD_HEAD * k - 7f), Size(w, 7f))
    val nomeCor = if (b.tint == Tint.NONE || b.tint == Tint.DARK) Palette.ink else Palette.on(l.tint)
    textAt(tm, b.text, Offset(tl.x + 7f, tl.y + RECORD_HEAD * k / 2), TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = nomeCor), a, left = true)
    b.rows.forEachIndexed { i, r ->
        val y = tl.y + (RECORD_HEAD + RECORD_ROW * i) * k
        drawLine(Palette.drawerLo.copy(alpha = a), Offset(tl.x, y), Offset(tl.x + w, y), 1.2f)
        val acesa = i == b.lit
        if (acesa) drawRect(Palette.yellow.copy(alpha = 0.22f * a), Offset(tl.x + 1f, y + 1f), Size(w - 2f, RECORD_ROW * k - 1f))
        textAt(tm, r, Offset(tl.x + 7f, y + RECORD_ROW * k / 2), TextStyle(fontFamily = mono, fontWeight = if (acesa) FontWeight.Bold else FontWeight.Normal, fontSize = u(b.size * 0.86f), color = if (acesa) Palette.yellow else Palette.ink), a, left = true)
    }
    drawRoundRect(borda.copy(alpha = a), tl, Size(w, h), CornerRadius(7f), style = Stroke(if (b.hot) 2.8f else 2f))
}

/** Cada letra cai da altura, gira um pouco, bate no chão e levanta poeira. */
private fun DrawScope.drawBang(b: Box, l: Live, tm: TextMeasurer, display: FontFamily) {
    val style = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = u(b.size), color = Palette.yellow)
    val letras = b.text.map { tm.measure(it.toString(), style) }
    val total = letras.sumOf { it.size.width }.toFloat()
    var x = l.x - total / 2
    val still = ThemeState.reduceMotion
    b.text.forEachIndexed { i, ch ->
        val medida = letras[i]
        val inicio = i * 0.075f
        val k = if (still) 1f else clamp01((l.age - inicio) / 0.34f)
        val cai = 1f - (1f - k) * (1f - k)
        val gira = (if (i % 2 == 0) -16f else 14f) * (1f - k)
        val y = l.y - medida.size.height / 2f - 170f * (1f - cai)
        if (k > 0f) {
            val cx = x + medida.size.width / 2f
            val cy = y + medida.size.height / 2f
            drawContext.canvas.save()
            drawContext.canvas.translate(cx, cy)
            drawContext.canvas.rotate(gira)
            drawContext.canvas.translate(-cx, -cy)
            drawText(medida, topLeft = Offset(x, y), alpha = clamp01(k * 3f))
            drawContext.canvas.restore()
        }
        val poeira = clamp01((l.age - inicio - 0.34f) / 0.3f)
        if (!still && poeira > 0f && poeira < 1f) {
            val chao = l.y + b.size * 0.42f
            for (j in -1..1) {
                val dx = j * 26f * poeira + j * 3f
                drawCircle(Palette.inkSoft.copy(alpha = 0.5f * (1f - poeira)), 6f * (1f - poeira) + 2f, Offset(x + medida.size.width / 2f + dx, chao - 9f * poeira))
            }
        }
        x += medida.size.width
        @Suppress("UNUSED_EXPRESSION") ch
    }
}

private fun edgePoint(c: Offset, other: Offset, l: Live): Offset {
    val d = other - c
    val len = sqrt(d.x * d.x + d.y * d.y)
    if (len < 1f) return c
    val u = d / len
    return if (l.box.look == Look.STATE || l.box.look == Look.ACCEPT) {
        c + u * (l.w / 2)
    } else {
        val kx = if (u.x == 0f) Float.MAX_VALUE else (l.w / 2) / kotlin.math.abs(u.x)
        val ky = if (u.y == 0f) Float.MAX_VALUE else (l.h / 2) / kotlin.math.abs(u.y)
        c + u * minOf(kx, ky)
    }
}

private fun DrawScope.drawWires(from: Shot, to: Shot, t: Float, live: Map<String, Live>, tm: TextMeasurer, mono: FontFamily, corre: Float) {
    val wasThere = from.wires.map { it.from to it.to }.toSet()
    val fade = clamp01((t - 0.55f) / 0.45f)
    for (w in to.wires) {
        val a = live[w.from] ?: continue
        val b = live[w.to] ?: continue
        val p = if ((w.from to w.to) in wasThere) 1f else fade
        if (p <= 0f) continue
        val color = (if (w.hot) Palette.yellow else Palette.ink.copy(alpha = 0.6f))
        drawWire(w, a, b, p, color, tm, mono, corre)
    }
}

/** Os símbolos viajam pelo fio em fila, e somem ao chegar. */
private fun DrawScope.drawFlow(w: Wire, start: Offset, ctrl: Offset, end: Offset, tm: TextMeasurer, mono: FontFamily, corre: Float, alpha: Float) {
    val n = w.flow.size
    w.flow.forEachIndexed { i, sim ->
        val fase = if (ThemeState.reduceMotion) (i + 1f) / (n + 1f) else (corre + i.toFloat() / n) % 1f
        val at = quad(start, ctrl, end, fase)
        val some = clamp01(minOf(fase, 1f - fase) * 8f) * alpha
        val l = tm.measure(sim, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(12f), color = Palette.inkOnLight.copy(alpha = some)))
        val tl = Offset(at.x - l.size.width / 2f, at.y - l.size.height / 2f)
        drawRoundRect(Palette.yellow.copy(alpha = some), tl - Offset(5f, 2f), Size(l.size.width + 10f, l.size.height + 4f), CornerRadius(5f))
        drawText(l, topLeft = tl)
    }
}

private fun DrawScope.drawWire(w: Wire, a: Live, b: Live, p: Float, color: Color, tm: TextMeasurer, mono: FontFamily, corre: Float) {
    val ca = Offset(a.x, a.y)
    val cb = Offset(b.x, b.y)
    val alpha = p * minOf(a.alpha, b.alpha)
    val stroke = if (w.hot) 3.5f else 2.2f
    if (w.fromRow >= 0 && a.box.look == Look.RECORD) {
        drawRowWire(w, a, b, alpha, color, stroke, p, tm, mono, corre)
        return
    }
    if (w.from == w.to) {
        // laço acima do estado
        val top = ca - Offset(0f, a.h / 2)
        val path = Path().apply {
            moveTo(top.x - 10f, top.y + 2f)
            cubicTo(top.x - 30f, top.y - 34f, top.x + 30f, top.y - 34f, top.x + 10f, top.y + 2f)
        }
        drawPath(path, color.copy(alpha = color.alpha * alpha), style = Stroke(stroke, cap = StrokeCap.Round))
        arrowHead(Offset(top.x + 10f, top.y + 2f), 2.2f, color.copy(alpha = color.alpha * alpha))
        wireLabel(w.label, Offset(top.x, top.y - 34f), tm, mono, alpha, w.hot)
        return
    }
    val start = edgePoint(ca, cb, a)
    val end = edgePoint(cb, ca, b)
    val mid = (start + end) / 2f
    val d = end - start
    val len = sqrt(d.x * d.x + d.y * d.y).coerceAtLeast(1f)
    val n = Offset(-d.y / len, d.x / len)
    val ctrl = mid + n * w.bend
    val tip = if (p < 1f) quad(start, ctrl, end, p) else end
    val path = Path().apply { moveTo(start.x, start.y); quadraticTo(lerp(start.x, ctrl.x, p), lerp(start.y, ctrl.y, p), tip.x, tip.y) }
    drawPath(path, color.copy(alpha = color.alpha * alpha), style = Stroke(stroke, cap = StrokeCap.Round))
    if (w.arrow) {
        val ang = atan2(end.y - ctrl.y, end.x - ctrl.x)
        arrowHead(tip, ang, color.copy(alpha = color.alpha * alpha))
    }
    wireLabel(w.label, quad(start, ctrl, end, 0.5f), tm, mono, alpha, w.hot)
    if (w.flow.isNotEmpty() && p >= 1f) drawFlow(w, start, ctrl, end, tm, mono, corre, alpha)
}

/** O fio sai da lateral da linha que o causa: "b → q2" liga na linha do b. */
private fun DrawScope.drawRowWire(w: Wire, a: Live, b: Live, alpha: Float, color: Color, stroke: Float, p: Float, tm: TextMeasurer, mono: FontFamily, corre: Float) {
    val c = color.copy(alpha = color.alpha * alpha)
    val ry = recordRowY(a.y, a.h, w.fromRow)
    if (w.from == w.to) {
        // laço na lateral que dá para o meio do palco, saindo e voltando para a mesma linha
        val lado = if (a.x > STAGE_W / 2) -1f else 1f
        val x = a.x + lado * a.w / 2
        val path = Path().apply { moveTo(x, ry - 4f); cubicTo(x + lado * 26f, ry - 16f, x + lado * 26f, ry + 16f, x + lado * 2f, ry + 5f) }
        drawPath(path, c, style = Stroke(stroke, cap = StrokeCap.Round))
        arrowHead(Offset(x + lado * 2f, ry + 5f), if (lado > 0) PI.toFloat() * 0.85f else PI.toFloat() * 0.15f, c)
        wireLabel(w.label, Offset(x + lado * 22f, ry), tm, mono, alpha, w.hot)
        return
    }
    // Destino em cima ou embaixo: sai pelo lado do meio do palco, para o fio não fugir da tela.
    val direita = if (kotlin.math.abs(b.x - a.x) < a.w / 2) a.x < STAGE_W / 2 else b.x >= a.x
    val start = Offset(if (direita) a.x + a.w / 2 else a.x - a.w / 2, ry)
    val alvo = Offset(b.x, b.y)
    val end = edgePoint(alvo, start, b)
    // sai na horizontal e faz a curva até o destino
    val ctrl = Offset(start.x + (if (direita) 1f else -1f) * maxOf(30f, kotlin.math.abs(end.x - start.x) * 0.6f) + w.bend, start.y)
    val tip = if (p < 1f) quad(start, ctrl, end, p) else end
    val path = Path().apply { moveTo(start.x, start.y); quadraticTo(lerp(start.x, ctrl.x, p), lerp(start.y, ctrl.y, p), tip.x, tip.y) }
    drawCircle(c, 3.5f, start)
    drawPath(path, c, style = Stroke(stroke, cap = StrokeCap.Round))
    if (w.arrow) arrowHead(tip, atan2(end.y - ctrl.y, end.x - ctrl.x), c)
    wireLabel(w.label, quad(start, ctrl, end, 0.5f), tm, mono, alpha, w.hot)
    if (w.flow.isNotEmpty() && p >= 1f) drawFlow(w, start, ctrl, end, tm, mono, corre, alpha)
}

private fun quad(a: Offset, c: Offset, b: Offset, t: Float): Offset {
    val u = 1 - t
    return a * (u * u) + c * (2 * u * t) + b * (t * t)
}

private fun DrawScope.arrowHead(tip: Offset, ang: Float, c: Color) {
    val s = 9f
    val p = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(tip.x - s * cos(ang - 0.45f), tip.y - s * sin(ang - 0.45f))
        lineTo(tip.x - s * cos(ang + 0.45f), tip.y - s * sin(ang + 0.45f))
        close()
    }
    drawPath(p, c)
}

private fun DrawScope.wireLabel(label: String, at: Offset, tm: TextMeasurer, mono: FontFamily, alpha: Float, hot: Boolean) {
    if (label.isEmpty()) return
    val l = tm.measure(label, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = u(12f), color = (if (hot) Palette.yellow else Palette.ink).copy(alpha = alpha)))
    val tl = Offset(at.x - l.size.width / 2f, at.y - l.size.height / 2f)
    drawRoundRect(Palette.paperDark.copy(alpha = 0.9f * alpha), tl - Offset(3f, 1f), Size(l.size.width + 6f, l.size.height + 2f), CornerRadius(4f))
    drawText(l, topLeft = tl)
}
