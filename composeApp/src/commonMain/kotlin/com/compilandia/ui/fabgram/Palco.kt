package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.compilandia.tutor.Corrida
import com.compilandia.tutor.Falha
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.Laudo
import kotlin.math.abs
import kotlin.math.sin

// Pixel com frente (Y01) + luz e sombra projetada (Z4). O palco é sempre a oficina escura,
// em qualquer tema: é a cena do jogo, como a sala.
internal val CHAO = listOf(Color(0xFFB4B2AA), Color(0xFFB8B6AE), Color(0xFFB0AEA6))
internal val LINHA = Color(0xFFA6A49C)
internal val OL = Color(0xFF0F1114)
internal val TOPO = Color(0xFF5B636E)
internal val TOPO_L = Color(0xFF7C858F)
internal val TOPO_D = Color(0xFF454C55)
internal val FRENTE = Color(0xFF363C44)
internal val FRENTE_D = Color(0xFF2A2F35)
internal val EST = Color(0xFF30353D)
internal val EST_L = Color(0xFF4C535D)
internal val AC = Color(0xFFE8A33A)
internal val AC2 = Color(0xFFFFCF6E)
internal val CAIXA = Color(0xFFD9B77E)
internal val CAIXA_D = Color(0xFFA8844F)
internal val CAIXA_OL = Color(0xFF5E4320)
internal val PECA = Color(0xFFE3E8EC)
internal val VERDE = Color(0xFF5EE08A)
internal val TELA = Color(0xFF0E3B38)
internal val TELA_L = Color(0xFF5FF2D2)
internal val VERMELHO = Color(0xFFE0553C)
internal val PLACA = Color(0xFFF1EAD8)

internal fun corSinal(f: Falha): Color = when (f) {
    Falha.IMPRODUTIVO -> Color(0xFF9A9A94)
    Falha.INALCANCAVEL -> Color(0xFFB9AE92)
    Falha.RECURSAO, Falha.RECURSAO_INDIRETA -> AC
    Falha.PREFIXO -> Color(0xFFFFD64A)
    Falha.CONFLITO_LL, Falha.CONFLITO_SLR -> VERMELHO
    Falha.AMBIGUA -> Color(0xFF8FB8FF)
}

internal data class NomesBlocos(val fonte: String, val separador: (String) -> String, val montadora: String, val cesto: String)

/** Tudo que um quadro do palco precisa. */
internal class Cena(
    val pl: Planta, val gr: Grade, val laudo: Laudo?, val c: Corrida?, val t: Float, val relogio: Float,
    val sel: Int?, val reduz: Boolean, val ferramenta: Ferramenta, val fantasma: Pair<Int, Cel>?,
    val medidor: TextMeasurer, val nomes: NomesBlocos,
    /** O que a missão pede para montar: esteiras e o lugar de um bloco. */
    val guia: Map<Cel, Dir> = emptyMap(), val guiaBloco: Pair<Int, Cel>? = null,
) {
    val cel: Float get() = gr.cel
    /** Um "pixel" da arte: a célula tem 32. */
    val u: Float get() = gr.cel / 32f
    val pisca: Boolean get() = !reduz && ((relogio * 2.5f).toInt() % 2 == 0)
    fun tem(nt: String?, vararg f: Falha) = laudo?.problemas.orEmpty().any { it.nt == nt && it.falha in f }
    fun trabalhando(id: Int) = c?.trabalhos?.any { it.bloco == id && t in it.t0..it.t1 } == true
}

internal fun hsh(a: Int, b: Int): Float { val s = sin(a * 127.1 + b * 311.7) * 43758.5453; return (s - kotlin.math.floor(s)).toFloat() }

internal fun DrawScope.r(x: Float, y: Float, w: Float, h: Float, cor: Color) = drawRect(cor, Offset(x, y), Size(w, h))

/** Ponto no caminho [pts] na fração [u] (0..1) do comprimento. */
private fun noCaminho(pts: List<Pair<Float, Float>>, u: Float): Pair<Float, Float> {
    var total = 0f
    for (i in 1 until pts.size) total += abs(pts[i].first - pts[i - 1].first) + abs(pts[i].second - pts[i - 1].second)
    var alvo = total * u.coerceIn(0f, 1f)
    for (i in 1 until pts.size) {
        val a = pts[i - 1]; val b = pts[i]
        val l = abs(b.first - a.first) + abs(b.second - a.second)
        if (alvo <= l || i == pts.size - 1) { val f = if (l == 0f) 1f else (alvo / l).coerceIn(0f, 1f); return (a.first + (b.first - a.first) * f) to (a.second + (b.second - a.second) * f) }
        alvo -= l
    }
    return pts.last()
}

internal fun DrawScope.desenhaFabrica(cena: Cena) {
    drawRect(CHAO[0])
    translate(cena.gr.ox - cena.gr.x0 * cena.cel, cena.gr.oy - cena.gr.y0 * cena.cel) {
        chao(cena)
        esteiras(cena)
        val brilhos = mutableListOf<Triple<Offset, Float, Color>>()
        for (b in cena.pl.blocos) bloco(cena, b, brilhos)
        guia(cena)
        fantasma(cena)
        itens(cena)
        for (b in cena.pl.blocos) placa(cena, b)
        for ((o, raio, cor) in brilhos) drawCircle(Brush.radialGradient(listOf(cor.copy(alpha = 0.22f), cor.copy(alpha = 0f)), center = o, radius = raio * 2), raio * 2, o, blendMode = BlendMode.Plus)
    }
    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0x40050310)), center = center, radius = size.maxDimension * 0.75f))
}

private fun DrawScope.chao(cena: Cena) {
    val cel = cena.cel; val u = cena.u
    val grade = if (cena.ferramenta == Ferramenta.MOVER) LINHA else Color(0xFF8F8D85)
    for (x in cena.gr.x0 until cena.gr.x0 + cena.gr.cols) for (y in cena.gr.y0 until cena.gr.y0 + cena.gr.rows) {
        r(x * cel, y * cel, cel, cel, CHAO[(hsh(x, y) * 3).toInt().coerceIn(0, 2)])
        r(x * cel, y * cel, cel, u, grade); r(x * cel, y * cel, u, cel, grade)
    }
}

/** Esteiras: cada célula liga o centro ao lado de onde vem e ao lado para onde vai, então curvas e junções se fecham. */
private fun DrawScope.esteiras(cena: Cena) {
    val pl = cena.pl; val cel = cena.cel; val u = cena.u
    val passo = if (cena.reduz) 0f else (cena.relogio * com.compilandia.tutor.FABGRAM_VEL) % 1f
    val meia = 9 * u
    for ((c, d) in pl.esteiras) {
        val x = c.x * cel; val y = c.y * cel
        val mx = x + cel / 2; val my = y + cel / 2
        val lados = Dir.entries.filter { s -> s == d || (c + s).let { n -> pl.esteiras[n]?.let { n + it == c } == true || pl.blocoEm(n)?.let { c - d == n } == true } }
        fun braco(s: Dir, cor: Color, esp: Float, dx: Float = 0f, dy: Float = 0f) {
            if (s.dx != 0) r(minOf(mx, mx + s.dx * cel / 2) + dx, my - esp + dy, cel / 2, 2 * esp, cor)
            else r(mx - esp + dx, minOf(my, my + s.dy * cel / 2) + dy, 2 * esp, cel / 2, cor)
        }
        for (s in lados) braco(s, Color.Black.copy(alpha = 0.25f), meia + u, 2 * u, 3 * u)
        for (s in lados) braco(s, OL, meia + u)
        r(mx - meia - u, my - meia - u, 2 * meia + 2 * u, 2 * meia + 2 * u, OL)
        for (s in lados) braco(s, EST, meia - u)
        r(mx - meia + u, my - meia + u, 2 * meia - 2 * u, 2 * meia - 2 * u, EST)
        // ripas que andam no sentido da esteira
        val reta = d.opposite() in lados
        var k = passo * 8f - 8f
        while (k < 32f) {
            val pos = k * u
            if ((reta || k >= 12f) && k > -2f) {
                if (d.dx != 0) { val px = if (d.dx > 0) x + pos else x + cel - pos - 2 * u; r(px, my - meia + 2 * u, 2 * u, 2 * meia - 4 * u, EST_L) }
                else { val py = if (d.dy > 0) y + pos else y + cel - pos - 2 * u; r(mx - meia + 2 * u, py, 2 * meia - 4 * u, 2 * u, EST_L) }
            }
            k += 8f
        }
        seta(mx, my, d, u)
        // fim de esteira: faixa de perigo
        val prox = c + d
        if (prox !in pl.esteiras && pl.blocoEm(prox) == null) {
            val ex = mx + d.dx * (cel / 2 - 3 * u); val ey = my + d.dy * (cel / 2 - 3 * u)
            for (i in -2..1) {
                val cor = if (i % 2 == 0) AC else OL
                if (d.dx != 0) r(ex - u, ey + i * 4 * u, 3 * u, 4 * u, cor) else r(ex + i * 4 * u, ey - u, 4 * u, 3 * u, cor)
            }
        }
    }
}

private fun Dir.opposite(): Dir = Dir.entries[(ordinal + 2) % 4]

private fun DrawScope.seta(mx: Float, my: Float, d: Dir, u: Float) {
    for (j in 0 until 4) {
        val ax = mx + d.dx * (j - 1) * u; val ay = my + d.dy * (j - 1) * u
        val l = (4 - j) * u
        if (d.dx != 0) r(ax, ay - l, u * 1.2f, 2 * l, AC) else r(ax - l, ay, 2 * l, u * 1.2f, AC)
    }
}

/** O fantasma da missão: esteira tracejada com a seta, e o contorno de onde o bloco deve ficar. */
private fun DrawScope.guia(cena: Cena) {
    val cel = cena.cel; val u = cena.u
    val a = if (cena.reduz) 0.9f else 0.55f + 0.35f * ((sin(cena.relogio * 4f) + 1f) / 2f)
    val traco = Stroke(2 * u, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4 * u, 3 * u)))
    for ((c, d) in cena.guia) {
        if (cena.pl.esteiras[c] == d) continue
        val x = c.x * cel; val y = c.y * cel
        drawRect(AZUL_GUIA.copy(alpha = 0.18f * a), Offset(x + 3 * u, y + 3 * u), Size(cel - 6 * u, cel - 6 * u))
        drawRect(AZUL_GUIA.copy(alpha = a), Offset(x + 3 * u, y + 3 * u), Size(cel - 6 * u, cel - 6 * u), style = traco)
        val mx = x + cel / 2; val my = y + cel / 2
        for (j in 0 until 5) {
            val ax = mx + d.dx * (j - 2) * 1.6f * u; val ay = my + d.dy * (j - 2) * 1.6f * u
            val l = (5 - j) * 1.3f * u
            if (d.dx != 0) r(ax, ay - l, 1.6f * u, 2 * l, AZUL_GUIA.copy(alpha = a)) else r(ax - l, ay, 2 * l, 1.6f * u, AZUL_GUIA.copy(alpha = a))
        }
    }
    val (id, alvo) = cena.guiaBloco ?: return
    val b = cena.pl.blocos.firstOrNull { it.id == id } ?: return
    if (b.x == alvo.x && b.y == alvo.y) return
    drawRect(AZUL_GUIA.copy(alpha = 0.18f * a), Offset(alvo.x * cel, alvo.y * cel), Size(b.w * cel, b.h * cel))
    drawRect(AZUL_GUIA.copy(alpha = a), Offset(alvo.x * cel, alvo.y * cel), Size(b.w * cel, b.h * cel), style = traco)
}

private val AZUL_GUIA = Color(0xFF2F7FE0)

private fun DrawScope.fantasma(cena: Cena) {
    val (id, alvo) = cena.fantasma ?: return
    val b = cena.pl.blocos.firstOrNull { it.id == id } ?: return
    val ok = cena.pl.move(id, alvo.x, alvo.y, cena.gr.area) != null
    val cor = if (ok) VERDE else VERMELHO
    val cel = cena.cel
    drawRect(cor.copy(alpha = 0.22f), Offset(alvo.x * cel, alvo.y * cel), Size(b.w * cel, b.h * cel))
    drawRect(cor, Offset(alvo.x * cel, alvo.y * cel), Size(b.w * cel, b.h * cel), style = Stroke(2 * cena.u))
}

private fun DrawScope.itens(cena: Cena) {
    val c = cena.c ?: return
    val cel = cena.cel; val u = cena.u
    val fim = c.trechos.lastOrNull()
    for (tr in c.trechos) {
        val parado = c.quebra != null && tr === fim && cena.t >= tr.t1
        if (!parado && (cena.t < tr.t0 || cena.t > tr.t1)) continue
        val (px, py) = noCaminho(tr.pontos, (cena.t - tr.t0) / (tr.t1 - tr.t0).coerceAtLeast(1e-3f))
        val ox = px * cel; val oy = py * cel
        if (tr.caixa) caixa(ox, oy, 16 * u, u) else { drawCircle(Color.Black.copy(alpha = 0.3f), 8 * u, Offset(ox + u, oy + 2 * u)); drawCircle(OL, 8 * u, Offset(ox, oy)); drawCircle(PECA, 7 * u, Offset(ox, oy)) }
        val est = TextStyle(color = if (tr.caixa) Color(0xFF3A2810) else Color(0xFF141619), fontSize = (cel * 0.3f / density).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        val lay = cena.medidor.measure(tr.simbolo, est)
        drawText(lay, topLeft = Offset(ox - lay.size.width / 2f, oy - lay.size.height / 2f))
        // o carimbo do separador: o número da máquina para onde a caixa vai
        tr.marca?.let { numero(cena, it, ox + 8 * u, oy - 8 * u, 5.5f * u) }
    }
    val q = c.quebra ?: return
    if (cena.t < c.duracao) return
    val qx = q.cel.x * cel; val qy = q.cel.y * cel
    val a = if (cena.pisca || cena.reduz) 1f else 0.45f
    drawRect(VERMELHO.copy(alpha = a), Offset(qx - u, qy - u), Size(cel + 2 * u, cel + 2 * u), style = Stroke(3 * u))
    drawCircle(VERMELHO, 6 * u, Offset(qx + cel - 2 * u, qy + 2 * u))
    r(qx + cel - 2.6f * u, qy - 1.5f * u, 1.2f * u, 4 * u, Color.White); r(qx + cel - 2.6f * u, qy + 3.6f * u, 1.2f * u, 1.2f * u, Color.White)
}

/** Uma bolinha com o número da máquina; o mesmo desenho na máquina e na caixa carimbada. */
internal fun DrawScope.numero(cena: Cena, n: Int, cx: Float, cy: Float, raio: Float) {
    drawCircle(OL, raio + cena.u, Offset(cx, cy))
    drawCircle(CORES_NUMERO[(n - 1) % CORES_NUMERO.size], raio, Offset(cx, cy))
    val est = TextStyle(color = Color.White, fontSize = (raio * 1.35f / density).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    val lay = cena.medidor.measure("$n", est)
    drawText(lay, topLeft = Offset(cx - lay.size.width / 2f, cy - lay.size.height / 2f))
}

private val CORES_NUMERO = listOf(Color(0xFF2F7FE0), Color(0xFFD9533B), Color(0xFF2E9E5B), Color(0xFF8E5BD0), Color(0xFFCC8A12))

private val Float.sp get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)

internal fun DrawScope.caixa(cx: Float, cy: Float, s: Float, u: Float) {
    drawRect(Color.Black.copy(alpha = 0.3f), Offset(cx - s / 2 + 2 * u, cy - s / 2 + 3 * u), Size(s, s))
    drawRect(CAIXA_OL, Offset(cx - s / 2, cy - s / 2), Size(s, s))
    drawRect(CAIXA, Offset(cx - s / 2 + u, cy - s / 2 + u), Size(s - 2 * u, s - 2 * u))
    drawRect(Color(0xFFEFD8AB), Offset(cx - s / 2 + u, cy - s / 2 + u), Size(s - 2 * u, u))
    drawRect(CAIXA_D, Offset(cx - s / 2 + u, cy + s / 2 - 3 * u), Size(s - 2 * u, u))
}
