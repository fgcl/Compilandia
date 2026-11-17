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
