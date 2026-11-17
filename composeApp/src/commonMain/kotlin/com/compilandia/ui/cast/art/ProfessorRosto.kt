package com.compilandia.ui.cast.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin

/* O rosto do professor de papel (olhos, sobrancelhas, nariz, boca, óculos) e os efeitos de alegria e contentamento. */

private const val EX = 13.1f
private const val EY = CABECA_Y + 2.1f
private const val BOCA_Y = EY + 21f

internal fun DrawScope.rosto(r: Rosto) {
    when (r) {
        Rosto.NORMAL -> for (s in LADOS) drawCircle(Papel.aro, 2.7f, Offset(CX + s * EX, EY))
        Rosto.PISCA -> for (s in LADOS) arco(s, cima = false)
        Rosto.ALEGRIA -> for (s in LADOS) arco(s, cima = true)
        Rosto.CONTENTE -> for (s in LADOS) arco(s, cima = false)
    }
    val alto = if (r == Rosto.ALEGRIA) 4f else if (r == Rosto.CONTENTE) 2f else 0f
    for (s in LADOS) sobrancelha(s, alto)
    drawOval(Papel.peleS, Offset(CX - 4.4f, EY + 8f), Size(8.8f, 7.2f))
    when (r) {
        Rosto.ALEGRIA -> bocaAberta()
        Rosto.CONTENTE -> sorriso(10.5f, 10.5f)
        else -> sorriso(8.4f, 6.3f)
    }
    if (r == Rosto.ALEGRIA || r == Rosto.CONTENTE) {
        for (s in LADOS) drawOval(Papel.bochecha, Offset(CX + s * 22f - 6.3f, EY + 9.9f), Size(12.6f, 7.6f))
    }
    oculos()
}

private val LADOS = listOf(-1f, 1f)

private fun DrawScope.arco(s: Float, cima: Boolean) {
    val x = CX + s * EX - 5.25f
    val p = Path().apply {
        moveTo(x, EY + if (cima) 2.1f else -1f)
        relativeQuadraticTo(5.25f, if (cima) -7.35f else 6.3f, 10.5f, 0f)
    }
    drawPath(p, Papel.aro, style = Stroke(2.8f, cap = StrokeCap.Round))
}

private fun DrawScope.sobrancelha(s: Float, alto: Float) {
    val p = Path().apply {
        moveTo(CX + s * EX - 7.35f, EY - 11.55f - alto)
        relativeQuadraticTo(7.35f, -4.2f, 14.7f, s * 0.5f)
    }
    drawPath(p, Papel.cabelo, style = Stroke(3.4f, cap = StrokeCap.Round))
}

private fun DrawScope.sorriso(meia: Float, fundo: Float) {
    val p = Path().apply { moveTo(CX - meia, BOCA_Y - 2f); relativeQuadraticTo(meia, fundo, meia * 2, 0f) }
    drawPath(p, Papel.boca, style = Stroke(2.8f, cap = StrokeCap.Round))
}

private fun DrawScope.bocaAberta() {
    val boca = Path().apply { moveTo(CX - 11.5f, BOCA_Y - 2f); relativeQuadraticTo(11.5f, 19f, 23f, 0f); close() }
    drawPath(boca, Papel.boca)
    val lingua = Path().apply { moveTo(CX - 6.3f, BOCA_Y + 5f); relativeQuadraticTo(6.3f, 4.2f, 12.6f, 0f); close() }
    drawPath(lingua, Papel.lingua)
    drawLine(Color.White, Offset(CX - 9.4f, BOCA_Y - 1.4f), Offset(CX + 9.4f, BOCA_Y - 1.4f), 2.5f, StrokeCap.Round)
}

private fun DrawScope.oculos() {
    for (s in LADOS) {
        val c = Offset(CX + s * EX, EY)
        drawCircle(Papel.lente, 10.5f, c)
        drawCircle(Papel.aro, 10.5f, c, style = Stroke(2.7f))
    }
    val ponte = Path().apply { moveTo(CX - EX + 10.5f, EY - 1f); relativeQuadraticTo(EX - 10.5f, -4f,
        2 * (EX - 10.5f), 0f) }
    drawPath(ponte, Papel.aro, style = Stroke(2.5f))
}

/** Estrelas amarelas que piscam em volta, na alegria. */
internal fun DrawScope.brilhos(t: Float) {
    val alfa = if (sin(t * 14f) > 0f) 1f else 0.35f
    for ((x, y) in listOf(28f to 70f, 172f to 58f, 20f to 150f, 180f to 136f, 100f to 18f)) {
        val p = Path().apply {
            moveTo(x, y - 9f); lineTo(x + 2.5f, y - 2.5f); lineTo(x + 9f, y); lineTo(x + 2.5f, y + 2.5f)
            lineTo(x, y + 9f); lineTo(x - 2.5f, y + 2.5f); lineTo(x - 9f, y); lineTo(x - 2.5f, y - 2.5f); close()
        }
        drawPath(p, Papel.brilho.copy(alpha = alfa))
    }
}

/** Dois corações subindo devagar, no contentamento. */
internal fun DrawScope.coracoes(t: Float) {
    val sobe = (t * 0.45f) % 1f
    val alfa = sin(sobe * PI.toFloat())
    for ((x, y) in listOf(160f to 60f, 40f to 84f)) {
        val yy = y - sobe * 26f
        val p = Path().apply {
            moveTo(x, yy); relativeCubicTo(-5f, -7f, -14f, 0f, -7f, 7f); relativeLineTo(7f, 7f); relativeLineTo(7f, -7f)
            relativeCubicTo(7f, -7f, -2f, -14f, -7f, -7f); close()
        }
        drawPath(p, Papel.coracao.copy(alpha = alfa))
    }
}
