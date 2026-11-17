package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.round
import kotlin.math.sqrt

/**
 * As formas que os desenhos pedem ao pacote. Cada uma sai do jeito do [Pacote]:
 * cheia, só o contorno (com brilho no neon), em degraus de pixel ou com o canto redondo.
 */

private const val PASSO_PIXEL = 2f
private const val BRILHO_LARGURA = 3.5f
private const val BRILHO_ALFA = 0.28f
private const val FUNDO_DO_CONTORNO = 0.10f
private const val LINHA_NO_CONTORNO = 1.4f

/** Encaixa [v] na grade de pixel do pacote (só no pacote pixel). */
internal fun Pincel.s(v: Float): Float = if (pk.pixel) round(v / (PASSO_PIXEL * u)) * (PASSO_PIXEL * u) else v

/** A largura do halo do neon em volta da linha. */
private fun Pincel.brilho(): Float = pk.traco * u * BRILHO_LARGURA

/** A cor que vai por cima de uma forma: no contorno o miolo é vazio, então a tinta é a cor da linha. */
internal fun Pincel.tintaSobre(): Color = if (pk.contorno) pk.linha else pk.tinta

internal fun DrawScope.forma(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) {
    val u = p.u
    val canto = CornerRadius(p.pk.raio * u)
    val o = Offset(x, y)
    val t = Size(w, h)
    when {
        p.pk.contorno -> {
            drawRoundRect(cor.copy(alpha = cor.alpha * FUNDO_DO_CONTORNO), o, t, canto)
            if (p.pk.brilho) drawRoundRect(cor.copy(alpha = BRILHO_ALFA), o, t, canto, Stroke(p.brilho()))
            drawRoundRect(cor, o, t, canto, Stroke(p.pk.traco * u))
        }
        p.pk.pixel -> detalhe(p, x, y, w, h, cor)
        else -> drawRoundRect(cor, o, t, canto)
    }
}

/** Retângulo sempre cheio e reto: detalhe pequeno (grade de pistão, faixa), que no contorno vira traço fino. */
internal fun DrawScope.detalhe(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) {
    val minimo = PASSO_PIXEL * p.u
    if (p.pk.pixel) drawRect(cor, Offset(p.s(x), p.s(y)), Size(maxOf(p.s(w), minimo), maxOf(p.s(h), minimo)))
    else drawRect(cor, Offset(x, y), Size(w, h))
}

internal fun DrawScope.disco(p: Pincel, cx: Float, cy: Float, r: Float, cor: Color) {
    val u = p.u
    when {
        p.pk.contorno -> {
            drawCircle(cor.copy(alpha = cor.alpha * FUNDO_DO_CONTORNO), r, Offset(cx, cy))
            if (p.pk.brilho) {
                drawCircle(cor.copy(alpha = BRILHO_ALFA), r, Offset(cx, cy), style = Stroke(p.brilho()))
            }
            drawCircle(cor, r, Offset(cx, cy), style = Stroke(p.pk.traco * u))
        }
        p.pk.pixel -> discoDePixel(p, cx, cy, r, cor)
        else -> drawCircle(cor, r, Offset(cx, cy))
    }
}

/** O círculo em degraus: uma faixa de 2 pixels por linha, da largura que o círculo tem ali. */
private fun DrawScope.discoDePixel(p: Pincel, cx: Float, cy: Float, r: Float, cor: Color) {
    val passo = PASSO_PIXEL * p.u
    var dy = -r + passo / 2
    while (dy < r) {
        val meia = p.s(sqrt((r * r - dy * dy).coerceAtLeast(0f)))
        if (meia > 0f) drawRect(cor, Offset(p.s(cx - meia), p.s(cy + dy - passo / 2)), Size(2 * meia, passo))
        dy += passo
    }
}

internal fun DrawScope.linha(p: Pincel, a: Offset, b: Offset, cor: Color, esp: Float) {
    if (p.pk.pixel) { linhaDePixel(p, a, b, cor, esp); return }
    val cap = if (p.pk.pixel) StrokeCap.Square else StrokeCap.Round
    if (p.pk.brilho) drawLine(cor.copy(alpha = BRILHO_ALFA), a, b, esp * BRILHO_LARGURA, cap)
    drawLine(cor, a, b, if (p.pk.contorno) minOf(esp, p.pk.traco * p.u * LINHA_NO_CONTORNO) else esp, cap)
}

/** A linha em escadinha: quadradinhos de pixel um depois do outro, sem nada liso. */
private fun DrawScope.linhaDePixel(p: Pincel, a: Offset, b: Offset, cor: Color, esp: Float) {
    val passo = PASSO_PIXEL * p.u
    val lado = maxOf(p.s(esp), passo)
    val n = ((b - a).getDistance() / passo).toInt().coerceAtLeast(1)
    for (i in 0..n) {
        val c = a + (b - a) * (i / n.toFloat())
        drawRect(cor, Offset(p.s(c.x - lado / 2), p.s(c.y - lado / 2)), Size(lado, lado))
    }
}

/** A caixa (não terminal) com a cara do pacote: papelão nos cheios, quadrado traçado nos de contorno. */
internal fun DrawScope.caixaP(p: Pincel, cx: Float, cy: Float, lado: Float) {
    val u = p.u
    if (p.pk.contorno) { forma(p, cx - lado / 2, cy - lado / 2, lado, lado, p.pk.nucleo); return }
    if (p.pk.pixel) {
        detalhe(p, cx - lado / 2, cy - lado / 2, lado, lado, CAIXA_OL)
        detalhe(p, cx - lado / 2 + 2 * u, cy - lado / 2 + 2 * u, lado - 4 * u, lado - 4 * u, CAIXA)
        return
    }
    caixa(cx, cy, lado, u)
}

/** Contorno tracejado: as caixas internas da planta azul. */
internal fun DrawScope.tracejado(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) {
    val u = p.u
    val traco = PathEffect.dashPathEffect(floatArrayOf(3 * u, 2 * u))
    drawRect(cor, Offset(x, y), Size(w, h), style = Stroke(0.8f * u, pathEffect = traco))
}
