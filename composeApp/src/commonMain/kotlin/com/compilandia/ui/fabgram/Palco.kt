package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.compilandia.tutor.Falha
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Modelo
import com.compilandia.tutor.FabGram.TipoBloco
import com.compilandia.tutor.Laudo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Pixel com frente (Y01) + luz e sombra projetada (Z4). O palco é sempre a oficina escura,
// em qualquer tema: é a cena do jogo, como a sala.
private val CHAO = listOf(Color(0xFF23262B), Color(0xFF25282D), Color(0xFF212428))
private val LINHA = Color(0xFF292C31)
private val OL = Color(0xFF111316)
private val TOPO = Color(0xFF5B636E)
private val TOPO_L = Color(0xFF7C858F)
private val TOPO_D = Color(0xFF454C55)
private val FRENTE = Color(0xFF363C44)
private val FRENTE_D = Color(0xFF2A2F35)
private val EST = Color(0xFF2B2F36)
private val EST_L = Color(0xFF444A53)
private val AC = Color(0xFFE8A33A)
private val AC2 = Color(0xFFFFCF6E)
private val CAIXA = Color(0xFFD9B77E)
private val CAIXA_D = Color(0xFFA8844F)
private val CAIXA_OL = Color(0xFF5E4320)
private val PECA = Color(0xFFE3E8EC)
private val VERDE = Color(0xFF5EE08A)
private val TELA = Color(0xFF0E3B38)
private val TELA_L = Color(0xFF5FF2D2)
private val VERMELHO = Color(0xFFD9533B)

internal fun corSinal(f: Falha): Color = when (f) {
    Falha.IMPRODUTIVO -> Color(0xFF9A9A94)
    Falha.INALCANCAVEL -> Color(0xFFB9AE92)
    Falha.RECURSAO, Falha.RECURSAO_INDIRETA -> AC
    Falha.PREFIXO -> Color(0xFFFFD64A)
    Falha.CONFLITO_LL, Falha.CONFLITO_SLR -> VERMELHO
    Falha.AMBIGUA -> Color(0xFF8FB8FF)
}

private fun hsh(a: Int, b: Int): Float { val s = sin(a * 127.1 + b * 311.7) * 43758.5453; return (s - kotlin.math.floor(s)).toFloat() }

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

internal fun DrawScope.desenhaFabrica(
    pl: FabGram.Planta, laudo: Laudo?, c: FabGram.Corrida?, t: Float, relogio: Float, sel: Int?, reduz: Boolean,
    medidor: TextMeasurer, nomes: NomesBlocos,
) {
    val cel = size.width / pl.largura
    val u = cel / 32f // um "pixel" da arte
    fun r(x: Float, y: Float, w: Float, h: Float, cor: Color) = drawRect(cor, Offset(x, y), Size(w, h))
    fun disco(x: Float, y: Float, raio: Float, cor: Color) = drawCircle(cor, raio, Offset(x, y))

    // ---- chão
    for (x in 0 until pl.largura) for (y in 0 until pl.altura) {
        r(x * cel, y * cel, cel, cel, CHAO[(hsh(x, y) * 3).toInt().coerceIn(0, 2)])
        r(x * cel, y * cel, cel, u, LINHA); r(x * cel, y * cel, u, cel, LINHA)
    }

    // ---- sinais vindos do Laboratório, por letra
    val sinais = laudo?.problemas.orEmpty()
    fun tem(nt: String?, vararg f: Falha) = sinais.any { it.nt == nt && it.falha in f }
    val pisca = !reduz && ((relogio * 2.5f).toInt() % 2 == 0)

    // ---- esteiras: trilho fino, ripas e setinha; o elevador é um poço com plataforma
    val passo = if (reduz) 0f else (relogio * FabGram.VEL) % 1f
    for (e in pl.esteiras) {
        val recursiva = e.papel == FabGram.Papel.RETORNO && pl.linhaDe.entries.any { (nt, yA) -> e.celulas.first().y == yA + 4 && tem(nt, Falha.RECURSAO, Falha.RECURSAO_INDIRETA) }
        e.celulas.forEachIndexed { i, cl ->
            val d = e.dir[i]
            val x = cl.x * cel; val y = cl.y * cel
            if (e.papel == FabGram.Papel.ELEVADOR) {
                r(x + 6 * u, y, 3 * u, cel, OL); r(x + 23 * u, y, 3 * u, cel, OL)
                r(x + 7 * u, y, u, cel, TOPO_L); r(x + 24 * u, y, u, cel, TOPO_L)
                if (cl.y % 2 == 0) r(x + 9 * u, y + 15 * u, 14 * u, 2 * u, EST_L)
                return@forEachIndexed
            }
            val trilho = if (recursiva && (pisca || reduz)) AC else EST
            if (d.x != 0) {
                r(x + 3 * u, y + 24 * u, cel, 3 * u, Color.Black.copy(alpha = 0.25f))
                r(x, y + 8 * u, cel, cel - 12 * u, OL); r(x, y + 9 * u, cel, cel - 17 * u, trilho); r(x, y + cel - 8 * u, cel, 4 * u, FRENTE_D)
                var k = 2f + passo * 6f * d.x
                while (k < 32f) { if (k >= 0f) r(x + k * u, y + 11 * u, 2 * u, cel - 21 * u, EST_L); k += 6f }
                r(x + (16 + d.x * 2 - 1) * u, y + 13 * u, 2 * u, 4 * u, AC)
            } else {
                r(x + 6 * u, y, cel - 12 * u, cel, OL); r(x + 7 * u, y, cel - 14 * u, cel, trilho)
                var k = 2f + passo * 6f * d.y
                while (k < 32f) { if (k >= 0f) r(x + 9 * u, y + k * u, cel - 18 * u, 2 * u, EST_L); k += 6f }
                r(x + 14 * u, y + (16 + d.y * 2 - 1) * u, 4 * u, 2 * u, AC)
            }
        }
    }

    // ---- blocos: topo, faixa da frente e sombra pontilhada para baixo e à direita
    val F = 8f
    val brilhos = mutableListOf<Triple<Offset, Float, Color>>()
    fun trabalhando(id: Int) = c?.trabalhos?.any { it.bloco == id && t in it.t0..it.t1 } == true
    for (b in pl.blocos) {
        val x = b.x * cel; val y = b.y * cel; val w = b.w * cel; val h = b.h * cel
        // sombra pontilhada
        val passoS = 2 * u
        var sy = 0f
        while (sy < h) { var sx = if (((sy / passoS).toInt()) % 2 == 0) 0f else passoS; while (sx < w) { r(x + sx + 6 * u, y + sy + 6 * u, passoS, passoS, Color.Black.copy(alpha = 0.28f)); sx += passoS * 2 }; sy += passoS }
        val corTopo = when { b.tipo == TipoBloco.FONTE -> Color(0xFF8A6A3E); b.modelo == Modelo.COFRE -> Color(0xFF5A6F8A); b.tipo == TipoBloco.CESTO -> Color(0xFF8A3A2A); else -> TOPO }
        r(x, y, w, h, OL)
        r(x + u, y + h - F * u, w - 2 * u, (F - 1) * u, if (corTopo == TOPO) FRENTE else lerp(corTopo, Color.Black, 0.35f)); r(x + u, y + h - F * u, w - 2 * u, u, FRENTE_D)
        r(x + u, y + u, w - 2 * u, h - (F + 1) * u, corTopo); r(x + u, y + u, w - 2 * u, u, lerp(corTopo, Color.White, 0.25f)); r(x + u, y + u, u, h - (F + 1) * u, lerp(corTopo, Color.White, 0.18f))
        r(x + u, y + h - (F + 1) * u, w - 2 * u, u, lerp(corTopo, Color.Black, 0.25f))
        val th = h - F * u
        val cx = x + w / 2; val cy = y + th / 2
        val ativo = trabalhando(b.id)
        when {
            b.tipo == TipoBloco.FONTE -> for ((a, bb) in listOf(-7f to -4f, 6f to -6f, 0f to 5f)) { caixa(cx + a * u * 2, cy + bb * u * 2, 22 * u, u) }
            b.tipo == TipoBloco.SEPARADOR -> {
                val emperra = tem(b.nt, Falha.CONFLITO_LL, Falha.CONFLITO_SLR)
                for (j in 0 until 6) { r(cx - j * u, cy - (6 - j) * u, (2 * j + 1) * u, u, if (emperra) VERMELHO else AC); r(cx - j * u, cy + (6 - j) * u, (2 * j + 1) * u, u, if (emperra) VERMELHO else AC) }
                if (emperra) { r(cx - 8 * u, cy - u, 16 * u, 2 * u, OL) }
                if (tem(b.nt, Falha.PREFIXO)) { disco(x + w - 6 * u, y + 6 * u, 3 * u, Color(0xFFFFD64A)); brilhos += Triple(Offset(x + w - 6 * u, y + 6 * u), 10 * u, Color(0xFFFFD64A)) }
                brilhos += Triple(Offset(cx, cy), 9 * u, AC)
            }
            b.tipo == TipoBloco.FILTRO -> { r(cx - u, y + 3 * u, 2 * u, th - 6 * u, OL); caixa(cx - 6 * u, cy + 4 * u, 9 * u, u); disco(cx + 6 * u, cy - 4 * u, 3 * u, PECA) }
            b.tipo == TipoBloco.MONTADORA -> {
                val chegou = c?.chegadas?.count { it.t <= t } ?: 0
                for (i in 0 until 3) { r(x + 8 * u + i * 16 * u, cy - 6 * u, 12 * u, 12 * u, OL); r(x + 9 * u + i * 16 * u, cy - 5 * u, 10 * u, 10 * u, if (i < chegou) TOPO_D else FRENTE_D); if (i < chegou) disco(x + 14 * u + i * 16 * u, cy, 4 * u, PECA) }
                if (laudo?.problemas?.any { it.falha == Falha.AMBIGUA } == true || c?.ambigua == true) {
                    for (k in 0 until 2) { r(x + (6 + k * 9) * u, y + 4 * u, 7 * u, 9 * u, OL); r(x + (7 + k * 9) * u, y + 5 * u, 5 * u, 7 * u, Color(0xFF8FB8FF)) }
                }
            }
            b.tipo == TipoBloco.CESTO -> {
                r(x + 5 * u, y + 5 * u, w - 10 * u, th - 10 * u, Color(0xFF3A1810))
                if (c != null && t >= c.duracao && !c.aceita) caixa(cx, cy, 22 * u, u)
            }
        }
        if (b.tipo == TipoBloco.MAQUINA) {
            val giro = if (ativo && !reduz) relogio * 9f else 0.4f
            when (b.modelo) {
                Modelo.PRENSA -> {
                    r(x + 4 * u, cy - 3 * u, w - 8 * u, 6 * u, OL); r(x + 5 * u, cy - 2 * u, w - 10 * u, 4 * u, TOPO_D)
                    disco(cx, cy, 11 * u, OL); disco(cx, cy, 10 * u, TOPO_D)
                    for (i in 0 until 4) { val q = giro + i * PI.toFloat() / 2; for (j in 1 until 9) r(cx + cos(q) * j * u - u / 2, cy + sin(q) * j * u - u / 2, 2 * u, 2 * u, TOPO_L) }
                    disco(cx, cy, 2.5f * u, AC)
                    r(cx - 8 * u, y + th + u, 16 * u, (F - 3) * u, OL); for (i in 0 until 4) r(cx - 7 * u + i * 4 * u, y + th + u, 2 * u, (F - 3) * u, AC)
                    brilhos += Triple(Offset(cx, cy), if (ativo) 26 * u else 16 * u, AC)
                }
                Modelo.ARMARIO -> {
                    r(x + 5 * u, y + 5 * u, w - 10 * u, th - 12 * u, OL); r(x + 6 * u, y + 6 * u, w - 12 * u, th - 14 * u, TELA)
                    val regra = b.prod!!.lhs + "→" + b.prod.rhs.joinToString(" ")
                    val est = TextStyle(color = TELA_L, fontSize = (cel * 0.16f / density).sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    val lay = medidor.measure(regra, est, constraints = androidx.compose.ui.unit.Constraints(maxWidth = (w - 14 * u).toInt().coerceAtLeast(1)))
                    drawText(lay, topLeft = Offset(x + 7 * u, y + 8 * u))
                    for (k in 0 until b.w * 3) disco(x + (8 + k * 9) * u, y + th - 4 * u, 1.5f * u, listOf(AC, VERMELHO, VERDE)[k % 3])
                    brilhos += Triple(Offset(cx, y + 10 * u), if (ativo) 22 * u else 14 * u, TELA_L)
                }
                Modelo.BRACO -> {
                    disco(cx, y + th - 10 * u, 5 * u, OL); disco(cx, y + th - 10 * u, 4 * u, AC)
                    val a = if (ativo && !reduz) sin(relogio * 8f) * 4f * u else 0f
                    for (j in 0 until 14) r(cx - u + j * 0.3f * u, y + th - (12 + j) * u, 3 * u, 2 * u, AC2)
                    for (j in 0 until 8) r(cx + 3 * u - j * 0.7f * u + a, y + th - (26 + j * 0.3f) * u, 3 * u, 2 * u, AC2)
                    caixa(cx, y + 11 * u, 12 * u, u)
                }
                Modelo.COFRE -> {
                    for (k in 0 until 48) { val q = k / 48f * 2 * PI.toFloat(); r(cx + cos(q) * 8 * u, cy + sin(q) * 8 * u, u, u, AC) }
                    for (i in 0 until 2) { val q = giro + i * PI.toFloat() / 2; for (j in -7..7) r(cx + cos(q) * j * u, cy + sin(q) * j * u, u, u, AC2) }
                    brilhos += Triple(Offset(cx, cy), if (ativo) 18 * u else 10 * u, AC)
                }
                null -> {}
            }
            // poeira e luz apagada: nenhuma esteira traz caixa para esta letra
            if (tem(b.nt, Falha.INALCANCAVEL)) {
                r(x, y, w, h, Color(0x99121519))
                for (k in 0 until 30) r(x + hsh(k, b.id) * w, y + hsh(b.id, k) * h, 1.5f * u, u, Color(0x88CDC6B4))
            }
            // fumaça: a caixa desta letra nunca vira só peças
            if (tem(b.nt, Falha.IMPRODUTIVO)) {
                for (k in 0 until 5) {
                    val ph = if (reduz) k / 5f else ((relogio * 0.5f + k / 5f) % 1f)
                    disco(cx + sin(k * 2.3f + ph * 3) * 6 * u, y - ph * 24 * u, (4 + ph * 9) * u, Color(0xFF969691).copy(alpha = 0.4f * (1 - ph)))
                }
            }
            if (b.id == sel) {
                drawRect(AC2, Offset(x - u, y - u), Size(w + 2 * u, h + 2 * u), style = androidx.compose.ui.graphics.drawscope.Stroke(2 * u))
            }
            if (ativo) r(x + w - 7 * u, y + 4 * u, 3 * u, 3 * u, VERDE)
        }
    }

    // ---- itens andando
    if (c != null) for (tr in c.trechos) {
        if (t < tr.t0 || t > tr.t1) continue
        val (px, py) = noCaminho(tr.pontos, (t - tr.t0) / (tr.t1 - tr.t0).coerceAtLeast(1e-3f))
        val ox = px * cel; val oy = py * cel
        if (tr.caixa) caixa(ox, oy, 13 * u, u) else { disco(ox + u, oy + 2 * u, 6 * u, Color.Black.copy(alpha = 0.3f)); disco(ox, oy, 6 * u, OL); disco(ox, oy, 5 * u, PECA) }
        val est = TextStyle(color = if (tr.caixa) Color(0xFF3A2810) else Color(0xFF141619), fontSize = (cel * 0.24f / density).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        val lay = medidor.measure(tr.simbolo, est)
        drawText(lay, topLeft = Offset(ox - lay.size.width / 2f, oy - lay.size.height / 2f))
    }

    // ---- plaquinha com o nome embaixo de cada bloco
    if (cel >= 22f * density) for (b in pl.blocos) {
        val nome = when (b.tipo) {
            TipoBloco.FONTE -> nomes.fonte; TipoBloco.SEPARADOR -> nomes.separador; TipoBloco.FILTRO -> nomes.filtro
            TipoBloco.MONTADORA -> nomes.montadora; TipoBloco.CESTO -> nomes.cesto
            TipoBloco.MAQUINA -> when (b.modelo) { Modelo.PRENSA -> nomes.prensa; Modelo.BRACO -> nomes.braco; Modelo.ARMARIO -> nomes.armario; else -> nomes.cofre }
        }
        val est = TextStyle(color = Color(0xFFF1EAD8), fontSize = (cel * 0.2f / density).sp, fontWeight = FontWeight.Bold)
        val lay = medidor.measure(nome, est)
        val px = b.cx * cel - lay.size.width / 2f; val py = (b.y + b.h) * cel + u
        r(px - 3 * u, py, lay.size.width + 6 * u, lay.size.height.toFloat(), Color(0xE00E1013))
        drawText(lay, topLeft = Offset(px, py))
    }

    // ---- luz: brilho somado nas partes acesas e borda escurecida
    for ((o, raio, cor) in brilhos) drawCircle(Brush.radialGradient(listOf(cor.copy(alpha = 0.33f), cor.copy(alpha = 0f)), center = o, radius = raio * 2), raio * 2, o, blendMode = BlendMode.Plus)
    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0x80050310)), center = center, radius = size.maxDimension * 0.75f))
}

private fun lerp(a: Color, b: Color, f: Float) = Color(a.red + (b.red - a.red) * f, a.green + (b.green - a.green) * f, a.blue + (b.blue - a.blue) * f, 1f)

private fun DrawScope.caixa(cx: Float, cy: Float, s: Float, u: Float) {
    drawRect(Color.Black.copy(alpha = 0.3f), Offset(cx - s / 2 + 2 * u, cy - s / 2 + 3 * u), Size(s, s))
    drawRect(CAIXA_OL, Offset(cx - s / 2, cy - s / 2), Size(s, s))
    drawRect(CAIXA, Offset(cx - s / 2 + u, cy - s / 2 + u), Size(s - 2 * u, s - 2 * u))
    drawRect(Color(0xFFEFD8AB), Offset(cx - s / 2 + u, cy - s / 2 + u), Size(s - 2 * u, u))
    drawRect(CAIXA_D, Offset(cx - s / 2 + u, cy - u / 2), Size(s - 2 * u, u))
}
