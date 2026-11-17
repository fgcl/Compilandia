package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * O que se mexe na sala de aula, por cima do desenho parado (SalaViva.kt chama a cada quadro):
 * a aula de agora pulsa no quadro, o avião de papel cruza a sala, o ventilador gira, o relógio anda,
 * o café solta vapor. E o besouro, pequeno e deitado no chão, que passeia para ser esmagado.
 * [t] é o tempo em segundos.
 */

private object CoresDoAmbiente {
    val amarelo = Color(0xFFF5B700)
    val papel = Color(0xFFFBFAF6)
    val papelSombra = Color(0xFFD9D7CF)
    val ponteiro = Color(0xFFC8432A)
    val pa = Color(0xFF2FA9CC)
    val casco = Color(0xFFD8452B)
    val cascoLuz = Color(0xFFFF8A6A)
    val cascoEscuro = Color(0xFF8E2716)
    val preto = Color(0xFF1F2628)
}

fun DrawScope.ambienteDaSala(iso: Iso, t: Float, tem: (String) -> Boolean) {
    aulaDeAgora(iso, t)
    if (tem("luminaria")) ventiladorGirando(iso, t)
    if (tem("radio")) segundos(iso, t)
    if (tem("cafe")) vapor(iso, t)
    aviaoDePapel(iso, t)
}

/** O nó amarelo do quadro (a aula de agora) solta um anel que cresce e some. */
private fun DrawScope.aulaDeAgora(iso: Iso, t: Float) {
    val c = iso.p(4.4f, 0.07f, 1.7f)
    val k = (t % 1.6f) / 1.6f
    val r = iso.lado * (0.011f + 0.02f * k)
    drawCircle(CoresDoAmbiente.amarelo.copy(alpha = 0.7f * (1f - k)), r, c, style = Stroke(iso.lado * 0.003f))
}

/** As pás do ventilador de parede giram: um borrão claro e as pás meio transparentes. */
private fun DrawScope.ventiladorGirando(iso: Iso, t: Float) {
    val c = P3(10.55f, 0.36f, 3.75f)
    val giro = t * 9f
    for (k in 0 until 3) {
        val a = giro + k * 2.1f
        fun ponta(b: Float) = iso.p(c + P3(0.35f * cos(b), 0f, 0.35f * sin(b)))
        val pa = Path().apply {
            val m = iso.p(c)
            moveTo(m.x, m.y)
            ponta(a).let { lineTo(it.x, it.y) }
            ponta(a + 0.7f).let { lineTo(it.x, it.y) }
            close()
        }
        drawPath(pa, CoresDoAmbiente.pa.copy(alpha = 0.55f))
    }
}

/** O ponteiro dos segundos anda aos saltos, um por segundo. */
private fun DrawScope.segundos(iso: Iso, t: Float) {
    val c = P3(0.8f, 0.07f, 3.45f)
    val a = PI.toFloat() / 2 - (t.toInt() % 60) * (2 * PI.toFloat() / 60)
    val ponta = iso.p(c + P3(0.25f * cos(a), 0f, 0.25f * sin(a)))
    drawLine(CoresDoAmbiente.ponteiro, iso.p(c), ponta, iso.lado * 0.0012f)
}

/** Três fios de vapor saindo da garrafa e das canecas. */
private fun DrawScope.vapor(iso: Iso, t: Float) {
    val bocas = listOf(P3(0.3f, 7.3f, 1.36f), P3(0.45f, 7.6f, 0.98f), P3(0.25f, 7.65f, 0.98f))
    for ((k, base) in bocas.withIndex()) {
        val fase = (t * 0.5f + k * 0.33f) % 1f
        val q = iso.p(base + P3(0f, 0f, fase * 0.9f))
        val dx = iso.lado * 0.006f * sin(fase * 9f + k)
        val raio = iso.lado * (0.004f + 0.006f * fase)
        drawCircle(Color.White.copy(alpha = 0.55f * (1f - fase)), raio, q + Offset(dx, 0f))
    }
}

/** De vez em quando um avião de papel cruza a sala, da última fileira até o quadro, numa curva. */
private fun DrawScope.aviaoDePapel(iso: Iso, t: Float) {
    val ciclo = t % VOO_CICLO
    if (ciclo > VOO_DURA) return
    val k = ciclo / VOO_DURA
    val de = P3(1.2f, 7.4f, 1.2f)
    val ate = P3(5.2f, 0.6f, 2.6f)
    val arco = sin(k * PI.toFloat()) * 1.4f
    val no = P3(de.u + (ate.u - de.u) * k, de.v + (ate.v - de.v) * k, de.z + (ate.z - de.z) * k + arco)
    val m = iso.p(no)
    val frente = iso.p(ate) - iso.p(de)
    val angulo = (kotlin.math.atan2(frente.y, frente.x) * 180 / PI).toFloat()
    sombra(iso, P3(no.u, no.v), 0.35f, 0.18f * (1f - k * 0.5f))
    val s = iso.lado * 0.022f
    rotate(angulo, m) {
        fun asa(lado: Float) = Path().apply {
            moveTo(m.x + s, m.y)
            lineTo(m.x - s, m.y + lado * s * 0.55f)
            lineTo(m.x - s * 0.7f, m.y)
            close()
        }
        drawPath(asa(1f), CoresDoAmbiente.papelSombra)
        drawPath(asa(-1f), CoresDoAmbiente.papel)
    }
}

private const val VOO_CICLO = 17f
private const val VOO_DURA = 2.6f

/**
 * O besouro da sala, deitado no chão: casco vermelho com brilho, cabeça, antenas e seis patas que andam em tripé.
 * [rumo] é a direção na tela, em graus; [passo] faz as patas andarem.
 */
fun DrawScope.besouro(c: Offset, r: Float, rumo: Float, passo: Float) {
    val cor = CoresDoAmbiente
    drawOval(Color.Black.copy(alpha = 0.22f), Offset(c.x - r * 1.2f, c.y - r * 0.3f), Size(r * 2.4f, r * 1.1f))
    scale(1f, ACHATADO, c) {
        rotate(rumo, c) {
            patasEAntenas(c, r, passo)
            drawCircle(cor.preto, r * 0.45f, c + Offset(r * 1.05f, 0f))
            for (lado in listOf(-1f, 1f)) drawCircle(Color.White, r * 0.1f, c + Offset(r * 1.25f, lado * r * 0.2f))
            val luz = c + Offset(-r * 0.2f, -r * 0.35f)
            val casco = Brush.radialGradient(listOf(cor.cascoLuz, cor.casco, cor.cascoEscuro), luz, r * 1.4f)
            drawOval(casco, Offset(c.x - r * 1.05f, c.y - r * 0.85f), Size(r * 1.9f, r * 1.7f))
            drawLine(cor.cascoEscuro, c + Offset(r * 0.8f, 0f), c + Offset(-r * 1.0f, 0f), r * 0.08f)
            val pintas = listOf(-0.4f to -0.45f, -0.4f to 0.45f, 0.3f to -0.4f, 0.3f to 0.4f)
            for ((dx, dy) in pintas) drawCircle(cor.preto, r * 0.16f, c + Offset(dx * r, dy * r))
            drawOval(Color.White.copy(alpha = 0.5f), Offset(c.x - r * 0.4f, c.y - r * 0.65f), Size(r * 0.7f, r * 0.25f))
        }
    }
}

/** As seis patas, que andam em tripé, e as duas antenas. */
private fun DrawScope.patasEAntenas(c: Offset, r: Float, passo: Float) {
    val cor = CoresDoAmbiente
    for (lado in listOf(-1f, 1f)) for (k in 0 until 3) {
        val mexe = sin(passo + k * PI.toFloat() + if (lado > 0) PI.toFloat() else 0f) * r * 0.25f
        val raiz = c + Offset((k - 1) * r * 0.6f, 0f)
        drawLine(cor.preto, raiz, raiz + Offset((k - 1) * r * 0.4f + mexe, lado * r * 1.25f), r * 0.14f)
    }
    for (lado in listOf(-1f, 1f)) {
        val p = Path().apply {
            moveTo(c.x + r * 1.2f, c.y + lado * r * 0.15f)
            quadraticTo(c.x + r * 1.9f, c.y + lado * r * 0.2f, c.x + r * 2.1f, c.y + lado * r * 0.65f)
        }
        drawPath(p, cor.preto, style = Stroke(r * 0.1f))
    }
}

private const val ACHATADO = 0.62f
