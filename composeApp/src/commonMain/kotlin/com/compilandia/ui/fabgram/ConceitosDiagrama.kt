package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.compilandia.tutor.FabGram.Bloco

/**
 * A linha "diagrama", no jeito do shapez: o corpo é uma laje recortada (os dentes ficam onde o item sai),
 * a função aparece como um ícone grande e o caminho do item vem desenhado em setas por cima.
 * Toda máquina de regra mostra a caixa que entra embaixo e as fichas que saem em cima.
 */

private val LAJE_2X2 = listOf(
    0.04f to 0.14f, 0.30f to 0.14f, 0.30f to 0.05f, 0.70f to 0.05f, 0.70f to 0.14f, 0.96f to 0.14f,
    0.96f to 0.88f, 0.62f to 0.88f, 0.62f to 0.97f, 0.38f to 0.97f, 0.38f to 0.88f, 0.04f to 0.88f,
)
private val LAJE_1X1 = listOf(
    0.08f to 0.16f, 0.34f to 0.16f, 0.34f to 0.06f, 0.66f to 0.06f, 0.66f to 0.16f, 0.92f to 0.16f,
    0.92f to 0.86f, 0.64f to 0.86f, 0.64f to 0.96f, 0.36f to 0.96f, 0.36f to 0.86f, 0.08f to 0.86f,
)

/** Desempacotadora: a caixa entra por baixo, a tampa abre e as fichas da regra saem por cima. */
internal fun DrawScope.prensaDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_2X2)
    ficha(p, cx, y + h * 0.80f, 11 * u, b.nt ?: "S")
    seta(p, Offset(cx, y + h * 0.71f), Offset(cx, y + h * 0.64f), p.pk.linha)
    caixaAberta(p, cx, y + h * 0.52f, 12 * u)
    seta(p, Offset(cx, y + h * 0.38f), Offset(cx, y + h * 0.31f), p.pk.producao)
    fileira(p, simbolosDe(b, 3), cx, y + h * 0.20f, 12 * u)
}

/** Uma caixa de papelão de tampa aberta: as duas abas para fora. */
private fun DrawScope.caixaAberta(p: Pincel, cx: Float, cy: Float, lado: Float) {
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

/** Abridor: uma caixa entra, uma peça sai (ou nada, na regra ε: a caixa vira fumaça). */
internal fun DrawScope.cofreDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_1X1)
    ficha(p, cx, y + h * 0.70f, 8 * u, b.nt ?: "S")
    seta(p, Offset(cx, y + h * 0.58f), Offset(cx, y + h * 0.46f), p.pk.producao)
    val s = simbolosDe(b, 1)
    if (s.isEmpty()) for (i in -1..1) disco(p, cx + i * 4 * u, y + h * 0.30f - (i * i) * 2 * u, 3 * u, p.pk.escuro)
    else ficha(p, cx, y + h * 0.30f, 8 * u, s[0])
}

/** Carimbadora: a caixa de baixo leva um carimbo e sai com a letra nova em cima. */
internal fun DrawScope.bracoDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_1X1.map { (fx, fy) -> fx to (fy * 0.5f + if (fy > 0.5f) 0.5f else 0f) })
    ficha(p, cx, y + h * 0.80f, 10 * u, b.nt ?: "A")
    val descida = if (p.ativo && !p.reduz) 3 * u * ((kotlin.math.sin(p.relogio * 8f) + 1f) / 2f) else 0f
    // o carimbo: cabo e base, com a letra nova gravada
    linha(p, Offset(cx, y + h * 0.40f + descida), Offset(cx, y + h * 0.52f + descida), p.pk.linha, 3 * u)
    forma(p, cx - 9 * u, y + h * 0.52f + descida, 18 * u, 7 * u, p.pk.producao)
    seta(p, Offset(cx, y + h * 0.36f), Offset(cx, y + h * 0.27f), p.pk.producao)
    ficha(p, cx, y + h * 0.17f, 10 * u, simbolosDe(b, 1).firstOrNull() ?: "B")
}

/** Linha longa: a caixa entra pela esquerda e as fichas saem em duas fileiras. */
internal fun DrawScope.armarioDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    laje(p, x, y, w, h, LAJE_2X2.map { (fx, fy) -> fy to fx })
    ficha(p, x + w * 0.16f, y + h / 2, 10 * u, b.nt ?: "S")
    seta(p, Offset(x + w * 0.26f, y + h / 2), Offset(x + w * 0.40f, y + h / 2), p.pk.producao)
    val s = simbolosDe(b, 6)
    val metade = (s.size + 1) / 2
    fileira(p, s.take(metade), x + w * 0.68f, y + h * 0.36f, 9 * u)
    fileira(p, s.drop(metade), x + w * 0.68f, y + h * 0.64f, 9 * u)
}

/** Desvio: a caixa entra e o caminho se abre numa seta colorida por saída, cada uma com o número da máquina. */
internal fun DrawScope.separadorDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cel = CELULA_ARTE * u
    val n = (h / cel).toInt().coerceAtLeast(1)
    laje(p, x, y, w, h, listOf(0.08f to 0.02f, 0.80f to 0.02f, 0.96f to 0.5f / n, 0.80f to 0.98f, 0.08f to 0.98f))
    val origem = Offset(x + w * 0.30f, y + h / 2)
    for (i in 0 until n) {
        val cor = CORES_NUMERO[i % CORES_NUMERO.size]
        seta(p, origem, Offset(x + w * 0.80f, y + i * cel + cel / 2), if (n > 1) cor else p.pk.distribuicao)
    }
    ficha(p, origem.x, origem.y, 9 * u, b.nt ?: "S")
}

private const val CELULA_ARTE = 32f

/** Rotatória: em cada casa, duas setas em volta, dizendo "isto devolve". */
internal fun DrawScope.roteadorDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cel = CELULA_ARTE * u
    laje(p, x, y, w, h, listOf(0.02f to 0.10f, 0.98f to 0.10f, 0.98f to 0.90f, 0.02f to 0.90f))
    for (i in 0 until b.w.coerceAtLeast(1)) {
        val c = Offset(x + i * cel + cel / 2, y + h / 2)
        rotate(p.giro * 6f, c) {
            for (inicio in listOf(200f, 20f)) {
                val canto = c - Offset(8 * u, 8 * u)
                drawArc(p.pk.distribuicao, inicio, 130f, false, canto, Size(16 * u, 16 * u), style = Stroke(2.2f * u))
            }
            seta(p, c + Offset(-7 * u, -4 * u), c + Offset(-4 * u, -8 * u), p.pk.distribuicao)
            seta(p, c + Offset(7 * u, 4 * u), c + Offset(4 * u, 8 * u), p.pk.distribuicao)
        }
    }
}

/** Pilha de caixas com a letra inicial, e a seta de saída. */
internal fun DrawScope.fonteDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_2X2.map { (fx, fy) -> fx to 1f - fy })
    for ((dx, dy) in listOf(-8f to 4f, 8f to 4f, 0f to -8f)) caixaP(p, cx + dx * u, y + h * 0.42f + dy * u, 14 * u)
    rotulo(p, b.nt ?: "S", cx, y + h * 0.42f - 8 * u, 9 * u, TINTA_CAIXA)
    seta(p, Offset(cx, y + h * 0.68f), Offset(cx, y + h * 0.84f), p.pk.nucleo)
}

/** Mesa da frase: as fichas caem nas casas, uma do lado da outra, e um sublinhado diz "frase pronta". */
internal fun DrawScope.montadoraDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_2X2)
    seta(p, Offset(cx, y + h * 0.18f), Offset(cx, y + h * 0.34f), p.pk.ok)
    for (i in 0 until 4) forma(p, x + w * 0.14f + i * 12 * u, y + h * 0.48f, 9 * u, 9 * u, p.pk.escuro)
    linha(p, Offset(x + w * 0.14f, y + h * 0.72f), Offset(x + w * 0.86f, y + h * 0.72f), p.pk.ok, 2 * u)
}

/** Lixeira, como a do shapez: grande e na cor de perigo. */
internal fun DrawScope.cestoDiagrama(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, listOf(0.06f to 0.08f, 0.94f to 0.04f, 0.97f to 0.92f, 0.05f to 0.96f))
    val corpo = Path().apply {
        moveTo(cx - 11 * u, y + h * 0.36f); lineTo(cx + 11 * u, y + h * 0.36f)
        lineTo(cx + 8 * u, y + h * 0.80f); lineTo(cx - 8 * u, y + h * 0.80f); close()
    }
    drawPath(corpo, p.pk.alerta.copy(alpha = 0.18f)); drawPath(corpo, p.pk.alerta, style = Stroke(2.2f * u))
    linha(p, Offset(cx - 14 * u, y + h * 0.30f), Offset(cx + 14 * u, y + h * 0.30f), p.pk.alerta, 2.4f * u)
    forma(p, cx - 4 * u, y + h * 0.22f, 8 * u, 4 * u, p.pk.alerta)
    for (i in -1..1) {
        linha(p, Offset(cx + i * 5 * u, y + h * 0.42f), Offset(cx + i * 4 * u, y + h * 0.72f), p.pk.alerta, 1.6f * u)
    }
}
