package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.compilandia.tutor.FabGram.Bloco
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Peças de desenho que as máquinas novas (Conceitos*.kt) dividem. A regra da máquina aparece no desenho:
 * a prensa de S → a S b mostra as fichas a, S e b na saída. Quem olha a fábrica lê a gramática.
 */

/** Os símbolos que a máquina solta; no ícone do menu, sem regra, uns de exemplo do tamanho certo. */
internal fun simbolosDe(b: Bloco, quantos: Int): List<String> =
    b.prod?.rhs ?: listOf("a", "S", "b", "c", "A", "d").take(quantos)

/** A letra é caixa (quadrada, papelão); o resto é peça (redonda, clara). Letra = começa maiúscula. */
internal fun ehCaixa(s: String) = s.firstOrNull()?.isUpperCase() == true

/** Uma ficha da regra: caixa ou peça, com o símbolo escrito. */
internal fun DrawScope.ficha(p: Pincel, cx: Float, cy: Float, lado: Float, s: String) {
    val largura = larguraDaFicha(s, lado)
    val canto = Offset(cx - largura / 2, cy - lado / 2)
    when {
        ehCaixa(s) && s.length == 1 -> caixaP(p, cx, cy, lado)
        ehCaixa(s) -> pilula(p, canto, Size(largura, lado), CAIXA_OL, CAIXA, 0f)
        else -> pilula(p, canto, Size(largura, lado), p.pk.linha, PECA, lado / 2)
    }
    val fonte = lado * if (s.length > 2) FONTE_LONGA else FONTE_DA_FICHA
    rotulo(p, s, cx, cy, fonte, if (p.pk.contorno) p.pk.linha else OL)
}

/** Uma pílula com borda: [borda] por fora, [miolo] por dentro, cantos de raio [raio]. */
private fun DrawScope.pilula(p: Pincel, canto: Offset, tam: Size, borda: Color, miolo: Color, raio: Float) {
    val u = p.u
    drawRoundRect(borda, canto, tam, CornerRadius(raio))
    drawRoundRect(miolo, canto + Offset(u, u), Size(tam.width - 2 * u, tam.height - 2 * u), CornerRadius(raio))
}

/** Símbolo de uma letra cabe no círculo; "then" e "else" viram pílula do tamanho do texto. */
internal fun larguraDaFicha(s: String, lado: Float): Float =
    if (s.length <= 1) lado else lado * (PILULA_BASE + PILULA_POR_LETRA * s.length)

private const val PILULA_BASE = 0.35f
private const val PILULA_POR_LETRA = 0.42f

private const val FONTE_LONGA = 0.5f

private const val FONTE_DA_FICHA = 0.62f

/** Uma fileira de fichas centrada em ([cx], [cy]); ε vira um traço vazio. */
internal fun DrawScope.fileira(p: Pincel, simbolos: List<String>, cx: Float, cy: Float, lado: Float) {
    if (simbolos.isEmpty()) { rotulo(p, "ε", cx, cy, lado * 0.9f, p.pk.claro); return }
    val larguras = simbolos.map { larguraDaFicha(it, lado) }
    var x = cx - (larguras.sum() + 2 * p.u * (simbolos.size - 1)) / 2
    for ((i, s) in simbolos.withIndex()) {
        ficha(p, x + larguras[i] / 2, cy, lado, s)
        x += larguras[i] + 2 * p.u
    }
}

/** Um funil visto de cima: boca larga em cima, estreita embaixo, com a borda clara. */
internal fun DrawScope.funil(p: Pincel, cx: Float, topo: Float, largura: Float, altura: Float, cor: Color) {
    val u = p.u
    val caminho = Path().apply {
        moveTo(cx - largura / 2, topo); lineTo(cx + largura / 2, topo)
        lineTo(cx + largura / 4, topo + altura); lineTo(cx - largura / 4, topo + altura); close()
    }
    drawPath(caminho, cor)
    drawPath(caminho, p.pk.linha, style = Stroke(p.pk.traco.coerceAtLeast(1f) * u))
    detalhe(p, cx - largura / 2 + 2 * u, topo + u, largura - 4 * u, u, p.pk.claro)
}

/** Engrenagem de [dentes] dentes, girada [graus]. */
internal fun DrawScope.engrenagem(p: Pincel, c: Offset, r: Float, dentes: Int, graus: Float, cor: Color) {
    val cx = c.x; val cy = c.y
    val caminho = Path()
    val n = dentes * 2
    for (i in 0 until n) {
        val a = graus * PI.toFloat() / MEIA_VOLTA + i * PI.toFloat() / dentes
        val raio = if (i % 2 == 0) r else r * DENTE
        val px = cx + cos(a) * raio; val py = cy + sin(a) * raio
        if (i == 0) caminho.moveTo(px, py) else caminho.lineTo(px, py)
    }
    caminho.close()
    drawPath(caminho, cor)
    drawPath(caminho, p.pk.linha, style = Stroke(p.pk.traco.coerceAtLeast(1f) * p.u))
    disco(p, cx, cy, r * MIOLO, p.pk.linha)
}

private const val DENTE = 0.78f
private const val MEIA_VOLTA = 180f
private const val MIOLO = 0.28f

/** Faixa de perigo amarela e preta, em diagonal. */
internal fun DrawScope.listras(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    drawRect(p.pk.distribuicao, Offset(x, y), Size(w, h))
    var i = -h
    while (i < w) {
        val faixa = Path().apply {
            moveTo(x + i, y + h); lineTo(x + i + h, y)
            lineTo(x + i + h + 2 * u, y); lineTo(x + i + 2 * u, y + h); close()
        }
        drawPath(faixa, p.pk.linha)
        i += 5 * u
    }
}

/** A boca de entrada num lado do bloco: um recorte escuro com uma seta para dentro. */
internal fun DrawScope.boca(p: Pincel, cx: Float, cy: Float, larga: Float, curta: Float) {
    drawRoundRect(p.pk.linha, Offset(cx - larga / 2, cy - curta / 2), Size(larga, curta), CornerRadius(p.u))
}

/** Base comum dos conceitos: uma chapa com sombra, menor que a célula, para a silhueta da máquina aparecer. */
internal fun DrawScope.chapa(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) {
    val u = p.u; val k = p.pk
    val canto = CornerRadius(k.raio * u)
    drawRoundRect(k.sombra, Offset(x + 2 * u, y + 3 * u), Size(w, h), canto)
    drawRoundRect(cor, Offset(x, y), Size(w, h), canto)
    drawRoundRect(k.linha, Offset(x, y), Size(w, h), canto, Stroke(k.traco.coerceAtLeast(1f) * u))
    detalhe(p, x + 2 * u, y + u, w - 4 * u, u, k.claro.copy(alpha = 0.7f))
}

/** O corpo do Mindustry: retângulo de cantos cortados a 45°, a borda na cor da categoria ([acento]). */
internal fun DrawScope.chanfrado(p: Pincel, x: Float, y: Float, w: Float, h: Float, acento: Color) {
    val u = p.u; val c = minOf(w, h) * CORTE
    fun oct(m: Float) = Path().apply {
        moveTo(x + c + m, y + m); lineTo(x + w - c - m, y + m)
        lineTo(x + w - m, y + c + m); lineTo(x + w - m, y + h - c - m)
        lineTo(x + w - c - m, y + h - m); lineTo(x + c + m, y + h - m)
        lineTo(x + m, y + h - c - m); lineTo(x + m, y + c + m); close()
    }
    translate(2 * u, 3 * u) { drawPath(oct(0f), p.pk.sombra) }
    drawPath(oct(0f), p.pk.linha)
    drawPath(oct(u), acento)
    drawPath(oct(2.5f * u), p.pk.corpo)
    drawPath(oct(5 * u), p.pk.fundo)
}

private const val CORTE = 0.22f

/** Uma laje de forma livre (o corpo do shapez): [pontos] em frações do bloco, preenchida e com contorno grosso. */
internal fun DrawScope.laje(p: Pincel, x: Float, y: Float, w: Float, h: Float, pontos: List<Pair<Float, Float>>) {
    val u = p.u
    val caminho = Path().apply {
        pontos.forEachIndexed { i, (fx, fy) ->
            if (i == 0) moveTo(x + fx * w, y + fy * h) else lineTo(x + fx * w, y + fy * h)
        }
        close()
    }
    translate(2 * u, 3 * u) { drawPath(caminho, p.pk.sombra) }
    // a laje é clara, como papel, em todo pacote: o desenho por cima é que tem cor
    drawPath(caminho, if (p.pk.corpo.luminance() > CLARO) p.pk.corpo else p.pk.claro.copy(alpha = 1f))
    drawPath(caminho, p.pk.linha, style = Stroke(LAJE_TRACO * u))
}

private const val LAJE_TRACO = 2f
private const val CLARO = 0.5f

/** Seta de fluxo desenhada em cima da máquina: de [a] para [b], com ponta. */
internal fun DrawScope.seta(p: Pincel, a: Offset, b: Offset, cor: Color) {
    val u = p.u
    linha(p, a, b, cor, 2.2f * u)
    val d = (b - a) / (b - a).getDistance()
    val n = Offset(-d.y, d.x)
    val ponta = Path().apply {
        moveTo(b.x + d.x * 2 * u, b.y + d.y * 2 * u)
        lineTo(b.x - d.x * 4 * u + n.x * 4 * u, b.y - d.y * 4 * u + n.y * 4 * u)
        lineTo(b.x - d.x * 4 * u - n.x * 4 * u, b.y - d.y * 4 * u - n.y * 4 * u); close()
    }
    drawPath(ponta, cor)
}

/** Respiro de calor: grade escura que fica laranja quando a máquina trabalha. */
internal fun DrawScope.respiro(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val quente = if (p.ativo) QUENTE else FRIO
    detalhe(p, x, y, w, h, p.pk.linha)
    detalhe(p, x + p.u, y + p.u, w - 2 * p.u, h - 2 * p.u, p.pk.nucleo.copy(alpha = quente))
}

private const val QUENTE = 0.9f
private const val FRIO = 0.25f
