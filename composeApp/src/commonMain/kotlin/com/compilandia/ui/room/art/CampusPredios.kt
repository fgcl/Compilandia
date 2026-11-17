package com.compilandia.ui.room.art

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/*
 * Os prédios do campus (Campus.kt), com janelas em faixas, telhado com platibanda e luz de diorama.
 * O bloco das salas tem o telhado na cor de ação do jogo: é ali que a câmera entra.
 */

internal object CoresDosPredios {
    val parede = Color(0xFFEDE8DC)
    val telhado = Color(0xFFC9CCC8)
    val alvo = Color(0xFFC8432A)
    val vidro = Color(0xFF3E5566)
    val vidroLuz = Color(0xFF7FA0B5)
    val tijolo = Color(0xFFB5623E)
    val piramide = Color(0xFFF2F1EC)
}

private enum class Tipo { SALAS, COMUM, CAPELA }

private data class Predio(val o: P3, val t: P3, val tipo: Tipo = Tipo.COMUM)

private val PREDIOS = listOf(
    Predio(P3(9f, 8.6f), P3(9.2f, 1.9f, 1.7f), Tipo.SALAS),
    Predio(P3(8.2f, 10.5f), P3(1.4f, 5.2f, 1.7f), Tipo.SALAS),
    Predio(P3(10.6f, 10.5f), P3(1.2f, 5.2f, 1.3f), Tipo.SALAS),
    Predio(P3(8.2f, 14.3f), P3(3.6f, 1.3f, 1.3f), Tipo.SALAS),
    Predio(P3(12.2f, 12.4f), P3(3.4f, 4.2f, 1.9f)),
    Predio(P3(4.8f, 12.6f), P3(2.8f, 1.6f, 1.2f)),
    Predio(P3(4.8f, 14.6f), P3(2.6f, 2.0f, 1.1f)),
    Predio(P3(10f, 17.2f), P3(1.4f, 1.2f, 0.9f), Tipo.CAPELA),
    Predio(P3(33.6f, 8.8f), P3(2.4f, 3.3f, 1.8f)),
    Predio(P3(31.6f, 13f), P3(1.2f, 3.4f, 1.3f)),
    Predio(P3(34.5f, 15.4f), P3(2.2f, 1.6f, 1.3f)),
    Predio(P3(30.2f, 22.8f), P3(3.2f, 1.8f, 1.3f)),
    Predio(P3(27f, 24f), P3(2.6f, 2.2f, 1.3f)),
    Predio(P3(0.4f, 9f), P3(1.6f, 8f, 1.4f)),
)

/** O prédio redondo e o de telhado em pirâmide, que aparecem no satélite. */
private val REDONDO = P3(19.6f, 19.6f)
private val PIRAMIDE = P3(26f, 13f) to P3(4.4f, 3.2f, 1.4f)

internal fun DrawScope.predios(iso: Iso) {
    val itens = PREDIOS.map { p -> (p.o.u + p.o.v + (p.t.u + p.t.v) / 2) to { predio(iso, p) } } +
        listOf(
            (REDONDO.u + REDONDO.v) to { redondo(iso) },
            (PIRAMIDE.first.u + PIRAMIDE.first.v + 3.8f) to { piramide(iso) },
        )
    for ((_, desenha) in itens.sortedBy { it.first }) desenha()
}

private fun DrawScope.predio(iso: Iso, p: Predio) {
    val c = CoresDosPredios
    val parede = if (p.tipo == Tipo.CAPELA) c.tijolo else c.parede
    sombra(iso, p.o + P3(p.t.u / 2 + 0.6f, p.t.v / 2 + 0.6f), maxOf(p.t.u, p.t.v) * 0.75f, 0.25f)
    caixaLuz(iso, p.o, p.t, parede)
    if (p.tipo != Tipo.CAPELA) janelas(iso, p.o, p.t)
    if (p.tipo == Tipo.CAPELA) {
        telhadoEmPiramide(iso, p.o + P3(0f, 0f, p.t.z), p.t, 0.9f, c.tijolo.tom(-0.2f))
        return
    }
    val teto = if (p.tipo == Tipo.SALAS) c.alvo else c.telhado
    val topo = p.o + P3(0.12f, 0.12f, p.t.z)
    caixaLuz(iso, topo, P3(p.t.u - 0.24f, p.t.v - 0.24f, 0.04f), teto)
}

/** Faixas de vidro nas duas faces que aparecem, uma por andar, com montantes. */
private fun DrawScope.janelas(iso: Iso, o: P3, t: P3) {
    val c = CoresDosPredios
    val andares = (t.z / ANDAR).toInt().coerceAtLeast(1)
    for (k in 0 until andares) {
        val z = o.z + k * ANDAR + 0.22f
        val (u, v) = o.u + t.u to o.v + t.v
        val frente = faceV(iso, v + 0.01f, o.u + 0.15f, u - 0.15f, z, z + 0.3f)
        degrade(frente, iso.p(o.u, v, z + 0.3f), iso.p(o.u, v, z), listOf(c.vidroLuz, c.vidro))
        val lado = faceU(iso, u + 0.01f, o.v + 0.15f, v - 0.15f, z, z + 0.3f)
        degrade(lado, iso.p(u, o.v, z + 0.3f), iso.p(u, o.v, z), listOf(c.vidro.tom(0.1f), c.vidro.tom(-0.25f)))
        var m = o.u + 0.6f
        while (m < u - 0.2f) {
            drawLine(c.parede, iso.p(m, v + 0.02f, z), iso.p(m, v + 0.02f, z + 0.3f), iso.lado * 0.0012f)
            m += 0.6f
        }
    }
}

private const val ANDAR = 0.6f

private fun DrawScope.redondo(iso: Iso) {
    val c = CoresDosPredios
    sombra(iso, REDONDO + P3(0.8f, 0.8f), 2.6f, 0.25f)
    cilindro(iso, REDONDO, 1.9f, 1.3f, c.parede)
    cilindro(iso, REDONDO + P3(0f, 0f, 0.45f), 1.92f, 0.3f, c.vidro)
    disco(iso, REDONDO + P3(0f, 0f, 1.3f), 1.9f, c.telhado.tom(0.1f))
    disco(iso, REDONDO + P3(0f, 0f, 1.31f), 1.0f, c.telhado.tom(-0.08f))
}

private fun DrawScope.piramide(iso: Iso) {
    val (o, t) = PIRAMIDE
    sombra(iso, o + P3(t.u / 2 + 0.8f, t.v / 2 + 0.8f), 3.2f, 0.25f)
    caixaLuz(iso, o, t, CoresDosPredios.parede)
    janelas(iso, o, t)
    telhadoEmPiramide(iso, o + P3(0f, 0f, t.z), t, 1.5f, CoresDosPredios.piramide)
}

/** Quatro águas subindo até o meio; aparecem as duas da frente e um pouco das de trás. */
private fun DrawScope.telhadoEmPiramide(iso: Iso, o: P3, t: P3, h: Float, cor: Color) {
    val ponta = iso.p(o.u + t.u / 2, o.v + t.v / 2, o.z + h)
    val (u2, v2) = o.u + t.u to o.v + t.v
    poligono(cor.tom(0.15f), iso.p(o.u, o.v, o.z), iso.p(u2, o.v, o.z), ponta)
    poligono(cor.tom(0.08f), iso.p(o.u, o.v, o.z), iso.p(o.u, v2, o.z), ponta)
    poligono(cor.tom(-0.05f), iso.p(o.u, v2, o.z), iso.p(u2, v2, o.z), ponta)
    poligono(cor.tom(-0.25f), iso.p(u2, o.v, o.z), iso.p(u2, v2, o.z), ponta)
}
