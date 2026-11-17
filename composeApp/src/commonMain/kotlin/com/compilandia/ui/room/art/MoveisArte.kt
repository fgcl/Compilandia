package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * As 12 peças da loja, agora coisas de sala de aula, no mesmo mundo da sala (SalaDeAula.kt).
 * Cada uma tem o [MovelArte.centro], para o ícone da loja e para o pulo ao chegar, e o [MovelArte.tamanho] em unidades.
 * Os ids continuam os da oficina, para quem já comprou não perder nada: o "sofa" virou o ar-condicionado.
 * Cada peça fica onde nada do que é pintado depois cobre ela (as peças são pintadas por cima da sala).
 */
class MovelArte(val centro: P3, val tamanho: Float, val atras: Boolean = false, val desenha: DrawScope.(Iso) -> Unit)

private object Cores {
    val vaso = Color(0xFFC0643C)
    val folha = Color(0xFF3F8F5A)
    val folhaLuz = Color(0xFF6DBB7A)
    val estofado = Color(0xFF2B3136)
    val lixeira = Color(0xFF8E979C)
    val bug = Color(0xFFC8432A)
    val cartaz = Color(0xFF1F2628)
    val amarelo = Color(0xFFF5B700)
    val branco = Color(0xFFF7F6F2)
    val madeira = Color(0xFFA9774E)
    val livros = listOf(Color(0xFF2FA9CC), Color(0xFFC8432A), Color(0xFF1F5F99), Color(0xFFF5B700), Color(0xFF3F8F5A))
    val ursinho = Color(0xFFB98352)
    val garrafa = Color(0xFFC8432A)
    val caneca = Color(0xFFF7F6F2)
    val escuro = Color(0xFF26292B)
}

/** Na ordem de pintura: de trás para a frente. */
val moveisArte: Map<String, MovelArte> = linkedMapOf(
    "sofa" to MovelArte(P3(0.18f, 1.4f, 3.9f), 2.0f) { arCondicionado(it) },
    "radio" to MovelArte(P3(0.8f, 0f, 3.45f), 1.0f) { relogio(it) },
    "caixasom" to MovelArte(P3(6.95f, 0f, 3.7f), 0.9f) { caixaDeSom(it) },
    "luminaria" to MovelArte(P3(10.55f, 0f, 3.6f), 1.2f) { ventilador(it) },
    "estante" to MovelArte(P3(0.3f, 1.0f, 1.2f), 2.0f) { estante(it) },
    "ursinho" to MovelArte(P3(0.3f, 1.0f, 2.75f), 0.9f) { ursinho(it) },
    "quadro" to MovelArte(P3(0f, 4.9f, 2.05f), 1.4f) { cartazDoAutomato(it) },
    "lixeira" to MovelArte(P3(0.45f, 2.25f, 0.4f), 0.9f) { lixeira(it) },
    "planta" to MovelArte(P3(10.55f, 0.55f, 1.0f), 1.3f) { planta(it) },
    "cadeira" to MovelArte(P3(9.3f, 1.4f, 0.8f), 1.3f, atras = true) { cadeiraDoProfessor(it) },
    "abajur" to MovelArte(P3(8.15f, 1.95f, 1.3f), 0.9f) { abajur(it) },
    "cafe" to MovelArte(P3(0.35f, 7.45f, 0.9f), 1.4f) { cafe(it) },
)

/** Um círculo de pé numa parede de v constante (a da direita), como polígono. */
private fun circuloEmV(iso: Iso, c: P3, r: Float) = (0 until 20).map { k ->
    val a = 2 * PI * k / 20
    iso.p(c.u + r * cos(a).toFloat(), c.v, c.z + r * sin(a).toFloat())
}

private fun DrawScope.arCondicionado(iso: Iso) {
    caixaLuz(iso, P3(0f, 0.6f, 3.6f), P3(0.36f, 1.6f, 0.55f), Cores.branco)
    for (k in 0 until 4) {
        val z = 3.66f + k * 0.05f
        drawLine(Cores.lixeira, iso.p(0.37f, 0.7f, z), iso.p(0.37f, 2.1f, z), iso.lado * 0.0012f)
    }
    drawCircle(Color(0xFF3F8F5A), iso.lado * 0.002f, iso.p(0.37f, 2.05f, 4.05f))
}

private fun DrawScope.relogio(iso: Iso) {
    val c = P3(0.8f, 0.05f, 3.45f)
    drawPath(caminho(circuloEmV(iso, c, 0.36f)), Cores.escuro)
    drawPath(caminho(circuloEmV(iso, c + P3(0f, 0.01f, 0f), 0.3f)), Cores.branco)
    val m = iso.p(c + P3(0f, 0.02f, 0f))
    drawLine(Cores.escuro, m, iso.p(c + P3(0f, 0.02f, 0.22f)), iso.lado * 0.002f)
    drawLine(Cores.escuro, m, iso.p(c + P3(0.15f, 0.02f, 0f)), iso.lado * 0.0025f)
}

private fun DrawScope.caixaDeSom(iso: Iso) {
    caixaLuz(iso, P3(6.75f, 0f, 3.35f), P3(0.4f, 0.3f, 0.6f), Cores.escuro)
    drawCircle(Cores.lixeira, iso.lado * 0.006f, iso.p(6.95f, 0.31f, 3.55f))
    drawCircle(Cores.lixeira, iso.lado * 0.003f, iso.p(6.95f, 0.31f, 3.82f))
}

/** O ventilador de parede, com a grade e as pás. */
private fun DrawScope.ventilador(iso: Iso) {
    caixaLuz(iso, P3(10.45f, 0f, 3.3f), P3(0.2f, 0.25f, 0.2f), Cores.branco)
    val c = P3(10.55f, 0.35f, 3.75f)
    drawPath(caminho(circuloEmV(iso, c, 0.42f)), Cores.branco.tom(-0.1f))
    for (k in 0 until 3) {
        val a = k * 2.1f
        fun ponta(b: Float) = iso.p(c + P3(0.35f * cos(b), 0f, 0.35f * sin(b)))
        val pa = listOf(iso.p(c), ponta(a), ponta(a + 0.7f))
        drawPath(caminho(pa), Color(0xFF2FA9CC))
    }
    drawPath(caminho(circuloEmV(iso, c, 0.42f)), Cores.lixeira, style = Stroke(iso.lado * 0.0018f))
    drawCircle(Cores.branco, iso.lado * 0.004f, iso.p(c))
}

/** A estante encostada na parede da esquerda, com livros de compiladores. */
private fun DrawScope.estante(iso: Iso) {
    sombra(iso, P3(0.5f, 1.0f), 1.0f, 0.18f)
    caixaLuz(iso, P3(0f, 0.3f, 0f), P3(0.6f, 1.4f, 2.4f), Cores.madeira)
    for (k in 0 until 3) {
        val z = 0.25f + k * 0.75f
        drawPath(caminho(faceU(iso, 0.61f, 0.38f, 1.62f, z, z + 0.6f)), Cores.madeira.tom(-0.5f))
        for (j in 0 until 6) {
            val v = 0.42f + j * 0.19f
            val alto = 0.45f + ((j + k) % 3) * 0.05f
            val cor = Cores.livros[(j + k * 2) % Cores.livros.size]
            val lombada = faceU(iso, 0.62f, v, v + 0.16f, z, z + alto)
            degrade(lombada, iso.p(0.62f, v, z + alto), iso.p(0.62f, v + 0.16f, z), listOf(cor.tom(0.15f), cor.tom(-0.2f)))
        }
    }
}

private fun DrawScope.ursinho(iso: Iso) {
    val c = Cores.ursinho
    val corpo = iso.p(0.3f, 1.0f, 2.62f)
    val r = iso.lado * 0.012f
    drawCircle(c.tom(-0.1f), r * 1.1f, corpo)
    drawCircle(c, r * 0.8f, corpo + Offset(0f, -r * 1.5f))
    for (s in listOf(-1f, 1f)) drawCircle(c.tom(-0.15f), r * 0.35f, corpo + Offset(s * r * 0.7f, -r * 2.2f))
    drawCircle(c.tom(0.35f), r * 0.32f, corpo + Offset(r * 0.15f, -r * 1.25f))
    for (s in listOf(-1f, 1f)) drawCircle(Cores.escuro, r * 0.1f, corpo + Offset(s * r * 0.3f + r * 0.1f, -r * 1.65f))
}

/** O cartaz do autômato, entre o mural e a porta: estados em amarelo e as setas. */
private fun DrawScope.cartazDoAutomato(iso: Iso) {
    drawPath(caminho(faceU(iso, 0.04f, 4.5f, 5.35f, 1.45f, 2.65f)), Cores.amarelo)
    drawPath(caminho(faceU(iso, 0.05f, 4.55f, 5.3f, 1.5f, 2.6f)), Cores.cartaz)
    val estados = listOf(4.75f to 2.3f, 5.1f to 2.25f, 4.92f to 1.95f, 5.1f to 1.7f)
    for (k in 0 until estados.size - 1) {
        val (a, b) = estados[k] to estados[k + 1]
        drawLine(Cores.branco, iso.p(0.06f, a.first, a.second), iso.p(0.06f, b.first, b.second), iso.lado * 0.0015f)
    }
    for ((v, z) in estados) drawCircle(Cores.amarelo, iso.lado * 0.006f, iso.p(0.06f, v, z))
}

private fun DrawScope.lixeira(iso: Iso) {
    sombra(iso, P3(0.45f, 2.25f), 0.55f, 0.2f)
    cilindro(iso, P3(0.45f, 2.25f), 0.28f, 0.75f, Cores.lixeira)
    drawCircle(Cores.bug, iso.lado * 0.006f, iso.p(0.55f, 2.5f, 0.42f))
    drawCircle(Cores.escuro, iso.lado * 0.002f, iso.p(0.53f, 2.48f, 0.47f))
}

private fun DrawScope.planta(iso: Iso) {
    sombra(iso, P3(10.55f, 0.55f), 0.6f, 0.22f)
    cilindro(iso, P3(10.55f, 0.55f), 0.32f, 0.6f, Cores.vaso)
    val base = iso.p(10.55f, 0.55f, 0.6f)
    val folhas = listOf(-0.6f to 1.1f, -0.3f to 1.6f, 0.05f to 1.9f, 0.35f to 1.55f, 0.6f to 1.0f, 0.15f to 1.3f)
    folhas.forEachIndexed { k, (dv, dz) ->
        val ponta = iso.p(10.55f + dv * 0.4f, 0.55f + dv, 0.6f + dz)
        val w = iso.lado * 0.008f
        val cor = if (k % 2 == 0) Cores.folha else Cores.folhaLuz
        poligono(cor, Offset(base.x - w, base.y), ponta, Offset(base.x + w, base.y))
    }
}

/** A luminária da mesa do professor, de braço articulado, acesa. */
private fun DrawScope.abajur(iso: Iso) {
    caixaLuz(iso, P3(8.0f, 2.42f, 0.82f), P3(0.22f, 0.22f, 0.04f), Cores.escuro)
    val (a, b, c) = Triple(iso.p(8.11f, 2.53f, 0.86f), iso.p(8.12f, 2.4f, 1.45f), iso.p(8.3f, 2.2f, 1.3f))
    drawLine(Cores.escuro, a, b, iso.lado * 0.0025f)
    drawLine(Cores.escuro, b, c, iso.lado * 0.0025f)
    val r = iso.lado * 0.012f
    val cupula = listOf(Offset(-r, -r * 0.3f), Offset(r, -r * 0.8f), Offset(r * 1.4f, r * 0.6f),
        Offset(-r * 0.6f, r * 0.9f))
    poligono(Cores.amarelo, cupula.map { c + it })
}

/** A cadeira giratória do professor, atrás da mesa, virada para a turma: o encosto fica do lado do quadro. */
private fun DrawScope.cadeiraDoProfessor(iso: Iso) {
    val (u, v) = 9.3f to 1.4f
    sombra(iso, P3(u, v), 0.6f, 0.22f)
    for (k in 0 until 5) {
        val a = k * 1.256f
        val pe = iso.p(u + 0.38f * cos(a), v + 0.38f * sin(a), 0.05f)
        drawLine(Cores.escuro, iso.p(u, v, 0.08f), pe, iso.lado * 0.003f)
    }
    caixaLuz(iso, P3(u - 0.03f, v - 0.03f, 0.08f), P3(0.06f, 0.06f, 0.4f), Cores.lixeira)
    caixaLuz(iso, P3(u - 0.3f, v - 0.4f, 0.6f), P3(0.6f, 0.1f, 0.8f), Cores.estofado)
    caixaLuz(iso, P3(u - 0.32f, v - 0.3f, 0.48f), P3(0.64f, 0.6f, 0.12f), Cores.estofado)
}

/** A garrafa térmica e as canecas numa mesinha perto da porta. */
private fun DrawScope.cafe(iso: Iso) {
    sombra(iso, P3(0.35f, 7.45f), 0.7f, 0.2f)
    caixaLuz(iso, P3(0f, 7.1f, 0f), P3(0.7f, 0.7f, 0.85f), Cores.madeira)
    cilindro(iso, P3(0.3f, 7.3f, 0.85f), 0.12f, 0.45f, Cores.garrafa)
    cilindro(iso, P3(0.3f, 7.3f, 1.3f), 0.07f, 0.06f, Cores.escuro)
    cilindro(iso, P3(0.45f, 7.6f, 0.85f), 0.08f, 0.13f, Cores.caneca)
    cilindro(iso, P3(0.25f, 7.65f, 0.85f), 0.08f, 0.13f, Cores.amarelo)
}
