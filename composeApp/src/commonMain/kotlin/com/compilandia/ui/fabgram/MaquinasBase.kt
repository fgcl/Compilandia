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
 * Peças de desenho que as máquinas (Maquinas.kt, Maquinas2.kt) dividem. A regra da máquina aparece no desenho:
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
    rotulo(p, s, cx, cy, fonte, OL)
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


/** As lajes recortadas, em frações do bloco: os dentes ficam onde o item entra e sai. */
internal val LAJE_2X2 = listOf(
    0.04f to 0.14f, 0.30f to 0.14f, 0.30f to 0.05f, 0.70f to 0.05f, 0.70f to 0.14f, 0.96f to 0.14f,
    0.96f to 0.88f, 0.62f to 0.88f, 0.62f to 0.97f, 0.38f to 0.97f, 0.38f to 0.88f, 0.04f to 0.88f,
)
internal val LAJE_1X1 = listOf(
    0.08f to 0.16f, 0.34f to 0.16f, 0.34f to 0.06f, 0.66f to 0.06f, 0.66f to 0.16f, 0.92f to 0.16f,
    0.92f to 0.86f, 0.64f to 0.86f, 0.64f to 0.96f, 0.36f to 0.96f, 0.36f to 0.86f, 0.08f to 0.86f,
)

/** Uma caixa de papelão de tampa aberta: as duas abas para fora. */
internal fun DrawScope.caixaAberta(p: Pincel, cx: Float, cy: Float, lado: Float) {
    val u = p.u
    caixaP(p, cx, cy, lado)
    for (lado2 in listOf(-1f, 1f)) {
        val aba = Path().apply {
            moveTo(cx + lado2 * lado / 2, cy - lado / 2)
            lineTo(cx + lado2 * (lado / 2 + 5 * u), cy - lado / 2 - 5 * u)
            lineTo(cx + lado2 * 2 * u, cy - lado / 2 - 5 * u)
            lineTo(cx, cy - lado / 2); close()
        }
        drawPath(aba, CAIXA); drawPath(aba, CAIXA_OL, style = Stroke(u))
    }
}

internal const val CELULA_ARTE = 32f
