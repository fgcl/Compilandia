package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/*
 * Os 12 móveis da loja, desenhados por código no mesmo mundo da sala (ver Iso.kt).
 * Cada um tem o [MovelArte.centro], para o ícone da loja e para o pulo ao chegar, e o [MovelArte.tamanho] em unidades.
 * Móvel novo entra aqui: um desenho, um centro e um tamanho, sem render nenhum.
 */
class MovelArte(val centro: P3, val tamanho: Float, val desenha: DrawScope.(Iso) -> Unit)

private object Cores {
    val salmao = Color(0xFFE9A88A)
    val verde = Color(0xFF5FA88E)
    val creme = Color(0xFFF4E7C3)
    val marrom = Color(0xFFA9774E)
    val livros = listOf(Color(0xFF6FD3C6), Color(0xFFC9472F), Color(0xFF7FB0F0), Color(0xFFF2B01E))
    val luz = Color(0x33FFE6A0)
}

/** Na ordem de pintura: de trás para a frente. */
val moveisArte: Map<String, MovelArte> = linkedMapOf(
    "quadro" to MovelArte(P3(8.2f, 0f, 4.8f), 3.2f) { quadro(it) },
    "planta" to MovelArte(P3(6.1f, 0.8f, 1.1f), 2.4f) { planta(it) },
    "sofa" to MovelArte(P3(8.25f, 1.1f, 0.9f), 3.6f) { sofa(it) },
    "luminaria" to MovelArte(P3(9.5f, 2.9f, 2.8f), 4.6f) { luminaria(it) },
    "lixeira" to MovelArte(P3(1.9f, 2.4f, 0.5f), 1.6f) { lixeira(it) },
    "caixasom" to MovelArte(P3(1.05f, 4.25f, 3.8f), 1.6f) { caixaDeSom(it) },
    "abajur" to MovelArte(P3(1.2f, 7.0f, 4.0f), 1.9f) { abajur(it) },
    "cadeira" to MovelArte(P3(3.5f, 5.8f, 1.5f), 2.8f) { cadeira(it) },
    "estante" to MovelArte(P3(1.6f, 9.15f, 1.7f), 3.4f) { estante(it) },
    "radio" to MovelArte(P3(1.6f, 9.1f, 3.8f), 1.9f) { radio(it) },
    "cafe" to MovelArte(P3(4.5f, 9.0f, 1.2f), 2.8f) { cafe(it) },
    "ursinho" to MovelArte(P3(3.2f, 9.3f, 0.6f), 1.7f) { ursinho(it) },
)

/** O quadro do autômato na parede da direita: moldura escura com borda amarela, estados e setas. */
private fun DrawScope.quadro(iso: Iso) {
    val v = 0.05f
    placaEmV(iso, v, Offset(7.2f, 3.4f), Offset(9.2f, 6.2f), CoresDaSala.amarelo)
    placaEmV(iso, v + 0.01f, Offset(7.32f, 3.55f), Offset(9.08f, 6.05f), CoresDaSala.escuro)
    val estados = listOf(7.7f to 5.5f, 8.6f to 5.5f, 8.15f to 4.7f, 8.6f to 4.0f)
    for (k in 0 until estados.size - 1) {
        val (a, b) = estados[k] to estados[k + 1]
        drawLine(CoresDaSala.branco, iso.p(a.first, v, a.second), iso.p(b.first, v, b.second), iso.lado * 0.002f)
    }
    for ((u, z) in estados) drawCircle(CoresDaSala.amarelo, iso.lado * 0.009f, iso.p(u, v, z))
    for ((u, z) in estados) drawCircle(CoresDaSala.escuro, iso.lado * 0.006f, iso.p(u, v, z))
}

private fun DrawScope.planta(iso: Iso) {
    cilindro(iso, P3(6.1f, 0.8f), 0.45f, 0.9f, CoresDaSala.madeira)
    val base = iso.p(6.1f, 0.8f, 0.9f)
    for ((dv, dz) in listOf(-0.7f to 1.4f, -0.3f to 2.0f, 0.1f to 2.3f, 0.5f to 1.9f, 0.8f to 1.3f)) {
        val ponta = iso.p(6.1f, 0.8f + dv, 0.9f + dz)
        val w = iso.lado * 0.007f
        poligono(Cores.verde, Offset(base.x - w, base.y), ponta, Offset(base.x + w, base.y))
    }
}

private fun DrawScope.sofa(iso: Iso) {
    val c = Cores.salmao
    caixa(iso, P3(6.9f, 0.3f, 1.1f), P3(2.7f, 0.45f, 1.2f), c)
    caixa(iso, P3(6.9f, 0.3f, 0.3f), P3(0.35f, 1.7f, 1.4f), c)
    caixa(iso, P3(7.25f, 0.75f, 0.3f), P3(2.0f, 1.25f, 0.8f), c.tom(0.05f))
    caixa(iso, P3(9.25f, 0.3f, 0.3f), P3(0.35f, 1.7f, 1.4f), c)
    caixa(iso, P3(9.4f, 1.8f), P3(0.15f, 0.15f, 0.3f), CoresDaSala.metal)
    caixa(iso, P3(7.0f, 1.8f), P3(0.15f, 0.15f, 0.3f), CoresDaSala.metal)
}

/** A luminária de pé: base, haste, cúpula acesa e a luz no chão. */
private fun DrawScope.luminaria(iso: Iso) {
    val (u, v) = 9.5f to 2.9f
    disco(iso, P3(u, v), 1.6f, Cores.luz)
    disco(iso, P3(u, v), 0.5f, CoresDaSala.metal.tom(0.2f))
    caixa(iso, P3(u - 0.05f, v - 0.05f), P3(0.1f, 0.1f, 4.6f), CoresDaSala.metal)
    val (a, b) = iso.p(u, v, 5.4f) to iso.p(u, v, 4.4f)
    val (w1, w2) = iso.lado * 0.018f to iso.lado * 0.03f
    poligono(Cores.creme, Offset(a.x - w1, a.y), Offset(a.x + w1, a.y), Offset(b.x + w2, b.y), Offset(b.x - w2, b.y))
}

private fun DrawScope.lixeira(iso: Iso) = cilindro(iso, P3(1.9f, 2.4f), 0.45f, 1.0f, CoresDaSala.metal.tom(-0.25f))

private fun DrawScope.caixaDeSom(iso: Iso) {
    caixa(iso, P3(0.8f, 4.0f, 3.3f), P3(0.5f, 0.5f, 1.0f), CoresDaSala.escuro.tom(0.15f))
    drawCircle(CoresDaSala.metal, iso.lado * 0.006f, iso.p(1.05f, 4.51f, 3.95f))
    drawCircle(CoresDaSala.metal, iso.lado * 0.003f, iso.p(1.05f, 4.51f, 4.45f))
}

private fun DrawScope.abajur(iso: Iso) {
    val (u, v) = 1.2f to 7.0f
    disco(iso, P3(u, v, 3.3f), 0.4f, CoresDaSala.metal)
    caixa(iso, P3(u - 0.04f, v - 0.04f, 3.3f), P3(0.08f, 0.08f, 0.8f), CoresDaSala.metal)
    val (a, b) = iso.p(u, v, 4.7f) to iso.p(u, v, 3.95f)
    val (w1, w2) = iso.lado * 0.012f to iso.lado * 0.022f
    poligono(Cores.creme, Offset(a.x - w1, a.y), Offset(a.x + w1, a.y), Offset(b.x + w2, b.y), Offset(b.x - w2, b.y))
}

/** A cadeira de escritório virada para a mesa: pé em estrela, coluna, assento e encosto. */
private fun DrawScope.cadeira(iso: Iso) {
    val (u, v) = 3.5f to 5.8f
    disco(iso, P3(u, v, 0.1f), 0.8f, CoresDaSala.metal.tom(-0.2f))
    caixa(iso, P3(u - 0.07f, v - 0.07f, 0.1f), P3(0.14f, 0.14f, 1.3f), CoresDaSala.metal)
    caixa(iso, P3(u - 0.6f, v - 0.6f, 1.4f), P3(1.2f, 1.2f, 0.3f), Cores.salmao)
    caixa(iso, P3(u + 0.45f, v - 0.6f, 1.7f), P3(0.3f, 1.2f, 1.9f), Cores.salmao)
}

private fun DrawScope.estante(iso: Iso) {
    val m = CoresDaSala.madeira
    val pes = listOf(0.4f to 8.5f, 2.6f to 8.5f, 0.4f to 9.6f, 2.6f to 9.6f)
    for ((u, v) in pes) caixa(iso, P3(u, v), P3(0.2f, 0.2f, 3.4f), m)
    caixa(iso, P3(0.4f, 8.5f, 0.3f), P3(2.4f, 1.3f, 0.15f), m)
    Cores.livros.forEachIndexed { k, cor -> caixa(iso, P3(0.7f + k * 0.45f, 8.7f, 1.85f), P3(0.3f, 0.9f, 0.9f), cor) }
    caixa(iso, P3(0.4f, 8.5f, 1.7f), P3(2.4f, 1.3f, 0.15f), m)
    caixa(iso, P3(0.4f, 8.5f, 3.25f), P3(2.4f, 1.3f, 0.15f), m)
}

private fun DrawScope.radio(iso: Iso) {
    caixa(iso, P3(1.0f, 8.7f, 3.4f), P3(1.2f, 0.8f, 0.8f), CoresDaSala.metal.tom(0.15f))
    placaEmV(iso, 9.51f, Offset(1.15f, 3.55f), Offset(1.65f, 4.05f), CoresDaSala.escuro.tom(0.2f))
    drawCircle(CoresDaSala.amarelo, iso.lado * 0.004f, iso.p(1.9f, 9.51f, 3.85f))
    caixa(iso, P3(1.3f, 9.0f, 4.2f), P3(0.6f, 0.1f, 0.25f), CoresDaSala.escuro)
}

/** O cantinho do café: mesinha, cafeteira e uma xícara. */
private fun DrawScope.cafe(iso: Iso) {
    val m = CoresDaSala.madeira
    val pes = listOf(3.7f to 8.4f, 5.2f to 8.4f, 3.7f to 9.5f, 5.2f to 9.5f)
    for ((u, v) in pes) caixa(iso, P3(u, v), P3(0.15f, 0.15f, 1.6f), m)
    caixa(iso, P3(3.6f, 8.3f, 1.6f), P3(1.9f, 1.4f, 0.15f), m)
    caixa(iso, P3(3.8f, 8.45f, 1.75f), P3(0.7f, 0.6f, 1.0f), CoresDaSala.escuro.tom(0.1f))
    caixa(iso, P3(3.95f, 8.6f, 2.75f), P3(0.4f, 0.3f, 0.12f), CoresDaSala.vermelho)
    cilindro(iso, P3(5.0f, 9.2f, 1.75f), 0.18f, 0.3f, CoresDaSala.branco)
}

private fun DrawScope.ursinho(iso: Iso) {
    val (u, v) = 3.2f to 9.3f
    val r = iso.lado
    cilindro(iso, P3(u, v), 0.42f, 0.55f, Cores.marrom)
    val cabeca = iso.p(u, v, 1.05f)
    drawCircle(Cores.marrom, r * 0.012f, Offset(cabeca.x - r * 0.011f, cabeca.y - r * 0.011f))
    drawCircle(Cores.marrom, r * 0.012f, Offset(cabeca.x + r * 0.011f, cabeca.y - r * 0.011f))
    drawCircle(Cores.marrom.tom(0.08f), r * 0.017f, cabeca)
    drawCircle(Cores.creme, r * 0.007f, Offset(cabeca.x, cabeca.y + r * 0.006f))
    drawCircle(CoresDaSala.escuro, r * 0.0025f, Offset(cabeca.x - r * 0.006f, cabeca.y - r * 0.004f))
    drawCircle(CoresDaSala.escuro, r * 0.0025f, Offset(cabeca.x + r * 0.006f, cabeca.y - r * 0.004f))
}
