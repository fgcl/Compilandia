package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/*
 * O que fica nas paredes e no chão da sala de aula. Quatro coisas são botões (RoomMoveis.kt):
 * o quadro abre o mapa de aulas, a tela de projeção o Laboratório, o mural a Loja e o computador o Jogar.
 */

/** Onde ficam os botões e as coisas que se mexem, no mundo da sala. */
object NaAula {
    val QUADRO = P3(1.6f, 0f, 1.3f) to P3(5f, 0.2f, 1.8f)
    val TELA = P3(7.2f, 0f, 1.5f) to P3(2.7f, 0.25f, 2.5f)
    val MURAL = P3(0f, 1.9f, 1.3f) to P3(0.2f, 2.4f, 1.4f)
    val COMPUTADOR = P3(8.9f, 1.7f, 0.8f) to P3(1.1f, 0.8f, 0.85f)
    val BYTE = P3(8.45f, 2.3f, 0.82f)
    val PROFESSOR = P3(6.95f, 1.0f)
    val TELA_DO_MONITOR = P3(9.42f, 2.36f, 1.32f)
    val CENTRO_DA_TELA = P3(8.55f, 0.1f, 2.65f)
    val ABAJUR = P3(8.3f, 1.9f, 1.5f)
}

/** Quadro branco com o mapa de aulas, crucifixo em cima e tela de projeção ao lado. */
internal fun DrawScope.paredeDaDireita(iso: Iso) {
    val c = CoresDaAula
    degrade(faceV(iso, 0.03f, 1.5f, 6.7f, 1.2f, 3.2f), iso.p(1.5f, 0f, 3.2f), iso.p(6.7f, 0f, 1.2f),
        listOf(c.aluminio.tom(0.3f), c.aluminio))
    degrade(faceV(iso, 0.05f, 1.6f, 6.6f, 1.3f, 3.1f), iso.p(1.6f, 0f, 3.1f), iso.p(6.6f, 0f, 1.3f),
        listOf(Color.White, Color(0xFFE6EAEA)))
    val brilho = listOf(iso.p(2.7f, 0.06f, 3.1f), iso.p(3.5f, 0.06f, 3.1f), iso.p(2.8f, 0.06f, 1.3f), iso.p(2.0f,
        0.06f, 1.3f))
    drawPath(caminho(brilho), Color.White.copy(alpha = 0.7f))
    caixaLuz(iso, P3(1.6f, 0f, 1.16f), P3(5f, 0.2f, 0.06f), c.aluminio)
    mapaNoQuadro(iso)
    crucifixo(iso)
    telaDeProjecao(iso)
}

/** A trilha de aulas desenhada no quadro: feitas em verde, a de agora em amarelo, as próximas em cinza. */
private fun DrawScope.mapaNoQuadro(iso: Iso) {
    val nos = listOf(2.0f to 2.7f, 2.8f to 1.8f, 3.6f to 2.5f, 4.4f to 1.7f, 5.2f to 2.6f, 6.1f to 2.0f)
    val pts = nos.map { (u, z) -> iso.p(u, 0.06f, z) }
    val w = iso.lado * 0.0028f
    val tracos = PathEffect.dashPathEffect(floatArrayOf(w * 2.5f, w * 2f))
    for (k in 0 until pts.size - 1) drawLine(Color(0xFF4C585C), pts[k], pts[k + 1], w, pathEffect = tracos)
    val r = iso.lado * 0.011f
    pts.forEachIndexed { k, q ->
        val cor = when { k < 3 -> Color(0xFF2E8B57); k == 3 -> CoresDaAula.amarelo; else -> Color(0xFFD5D8D6) }
        drawCircle(Color.Black.copy(alpha = 0.12f), r, q + Offset(r * 0.25f, r * 0.3f))
        drawCircle(cor, r, q)
        drawCircle(Color(0xFF26292B), r, q, style = Stroke(w * 0.8f))
        drawCircle(Color.White.copy(alpha = 0.45f), r * 0.35f, q - Offset(r * 0.3f, r * 0.3f))
    }
}

private fun DrawScope.crucifixo(iso: Iso) {
    val c = CoresDaAula.bronze
    fun barra(u: Float, u2: Float, z: Float, z2: Float, d: Float, cor: Color) =
        drawPath(caminho(faceV(iso, d, u, u2, z, z2)), cor)
    barra(4.11f, 4.21f, 3.31f, 4.31f, 0.03f, Color.Black.copy(alpha = 0.12f))
    barra(3.86f, 4.46f, 3.91f, 3.99f, 0.03f, Color.Black.copy(alpha = 0.12f))
    barra(4.05f, 4.15f, 3.35f, 4.35f, 0.06f, c)
    barra(3.8f, 4.4f, 3.95f, 4.03f, 0.06f, c)
    val corpo = listOf(iso.p(4.1f, 0.08f, 3.88f), iso.p(3.88f, 0.08f, 3.98f), iso.p(4.32f, 0.08f, 3.98f))
    drawPath(caminho(corpo), c.tom(0.25f))
    drawPath(caminho(faceV(iso, 0.08f, 4.07f, 4.13f, 3.5f, 3.9f)), c.tom(0.25f))
    drawCircle(c.tom(0.25f), iso.lado * 0.0035f, iso.p(4.1f, 0.08f, 3.93f))
}

/** A tela que desce do teto, com a gramática do Bison projetada: é o Laboratório. */
private fun DrawScope.telaDeProjecao(iso: Iso) {
    val c = CoresDaAula
    degrade(faceV(iso, 0.06f, 7.2f, 9.9f, 1.55f, 3.75f), iso.p(7.2f, 0f, 3.75f), iso.p(9.9f, 0f, 1.55f),
        listOf(Color.White, Color(0xFFE9ECEC)))
    caixaLuz(iso, P3(7.2f, 0.02f, 1.45f), P3(2.7f, 0.08f, 0.1f), Color(0xFF6B7276))
    caixaLuz(iso, P3(7.0f, 0f, 3.75f), P3(3.1f, 0.25f, 0.24f), Color(0xFF5A6064))
    val linhas = listOf(0.62f, 0.95f, 0.5f, 0.8f, 0.4f)
    linhas.forEachIndexed { k, l ->
        val z = 3.35f - k * 0.34f
        val u = 7.5f + if (k in 1..3) 0.3f else 0f
        drawPath(caminho(faceV(iso, 0.07f, u, u + l * 2f, z - 0.09f, z)), c.tela.copy(alpha = 0.75f))
    }
    val luz = listOf(iso.p(7.2f, 0.07f, 3.75f), iso.p(9.9f, 0.07f, 3.75f), iso.p(9.9f, 0.07f, 1.55f), iso.p(7.2f,
        0.07f, 1.55f))
    degrade(luz, iso.p(8.5f, 0f, 3.6f), iso.p(8.5f, 0f, 1.6f), listOf(Color(0x22FFF6C8), Color(0x00FFF6C8)))
}

/** O mural de cortiça com cartazes (a Loja) e a porta aberta com a placa de saída. */
internal fun DrawScope.paredeDaEsquerda(iso: Iso) {
    val c = CoresDaAula
    drawPath(caminho(faceU(iso, 0.03f, 1.9f, 4.3f, 1.3f, 2.7f)), c.molduraMural)
    degrade(faceU(iso, 0.05f, 2.0f, 4.2f, 1.38f, 2.62f), iso.p(0f, 2f, 2.6f), iso.p(0f, 4.2f, 1.4f),
        listOf(c.cortica.tom(0.1f), c.cortica.tom(-0.1f)))
    val cartazes = listOf(
        Triple(2.25f to 2.95f, 1.55f to 2.45f, Color(0xFFE07A2E)),
        Triple(3.1f to 3.7f, 1.5f to 2.3f, Color(0xFF1F5F99)),
        Triple(3.82f to 4.1f, 2.05f to 2.45f, Color(0xFFF5D547)),
    )
    for ((v, z, cor) in cartazes) {
        drawPath(caminho(faceU(iso, 0.06f, v.first + 0.04f, v.second + 0.04f, z.first - 0.05f, z.second - 0.05f)),
            Color.Black.copy(alpha = 0.15f))
        degrade(faceU(iso, 0.07f, v.first, v.second, z.first, z.second), iso.p(0f, v.first, z.second), iso.p(0f,
            v.second, z.first), listOf(cor.tom(0.15f), cor))
        drawCircle(Color(0xFFC8432A), iso.lado * 0.003f, iso.p(0.08f, (v.first + v.second) / 2, z.second - 0.06f))
    }
    porta(iso)
}

private fun DrawScope.porta(iso: Iso) {
    val (a, b) = PORTA_V to PORTA_V2
    drawPath(caminho(faceU(iso, 0.03f, a - 0.1f, b + 0.1f, 0f, 2.9f)), Color(0xFFF7F7F4))
    degrade(faceU(iso, 0.05f, a, b, 0f, 2.8f), iso.p(0f, a, 2.8f), iso.p(0f, b, 0f), listOf(Color(0xFFE4F2F6),
        Color(0xFFFFFDF2)))
    val folha = listOf(iso.p(0.04f, b, 0f), iso.p(1.0f, b - 0.25f, 0f), iso.p(1.0f, b - 0.25f, 2.75f), iso.p(0.04f, b,
        2.75f))
    degrade(folha, iso.p(1f, b, 2.7f), iso.p(0f, b, 0f), listOf(Color(0xFFFFFFFF), Color(0xFFDDE1E1)))
    val vidro = listOf(iso.p(0.25f, b - 0.07f, 1.5f), iso.p(0.8f, b - 0.21f, 1.5f), iso.p(0.8f, b - 0.21f, 2.5f),
        iso.p(0.25f, b - 0.07f, 2.5f))
    drawPath(caminho(vidro), Color(0xFFCFE6EE))
    drawCircle(Color(0xFF9AA0A2), iso.lado * 0.003f, iso.p(0.85f, b - 0.22f, 1.2f))
    caixaLuz(iso, P3(0f, a + 0.25f, 3.05f), P3(0.12f, 0.7f, 0.3f), CoresDaAula.verdeSaida)
    drawPath(caminho(faceU(iso, 0.125f, a + 0.38f, a + 0.55f, 3.1f, 3.3f)), Color.White)
    drawPath(caminho(faceU(iso, 0.125f, a + 0.65f, a + 0.85f, 3.17f, 3.23f)), Color.White)
    caixaLuz(iso, P3(0f, a - 0.45f, 1.3f), P3(0.04f, 0.16f, 0.24f), Color(0xFFF7F7F4))
}

/**
 * Cadeiras e a mesa do professor, pintadas de trás para a frente. A sala sai em duas camadas: o [fundo] (o que fica
 * atrás da mesa) e a frente (a mesa e as cadeiras diante dela). Entre as duas entra a cadeira do professor, que a
 * loja vende e que fica atrás da mesa (RoomScreen.kt).
 */
internal fun DrawScope.moveisDaAula(iso: Iso, fundo: Boolean) {
    val itens = mutableListOf<Pair<Float, () -> Unit>>()
    for (u in COLUNAS) for (v in FILEIRAS) itens += (u + v + 0.6f) to { cadeira(iso, u, v) }
    itens += PROFUNDIDADE_DA_MESA to { mesa(iso) }
    for ((k, desenha) in itens.sortedBy { it.first }) if ((k < PROFUNDIDADE_DA_MESA) == fundo) desenha()
}

/** A frente da sala: a mesa do professor e o que está diante dela. */
fun DrawScope.frenteDaSala(iso: Iso) = moveisDaAula(iso, fundo = false)

private const val PROFUNDIDADE_DA_MESA = 11.2f

val COLUNAS = listOf(0.9f, 2.55f, 4.2f, 5.85f, 7.5f, 9.15f)
val FILEIRAS = listOf(3.7f, 5.1f, 6.5f)

/** A cadeira da foto: concha azul-piscina, pernas de tubo e a prancheta branca à direita. */
fun DrawScope.cadeira(iso: Iso, u: Float, v: Float) {
    val c = CoresDaAula
    sombra(iso, P3(u + 0.55f, v + 0.25f), 0.75f, 0.16f)
    val w = iso.lado * 0.0035f
    for ((a, b) in listOf(0.06f to 0.06f, 0.66f to 0.06f, 0.06f to 0.58f, 0.66f to 0.58f)) {
        drawLine(c.metal, iso.p(u + a, v + b, 0f), iso.p(u + a, v + b, 0.46f), w)
    }
    drawLine(c.metal.tom(-0.1f), iso.p(u + 0.06f, v + 0.06f, 0.12f), iso.p(u + 0.06f, v + 0.58f, 0.12f), w * 0.8f)
    drawLine(c.metal.tom(-0.1f), iso.p(u + 0.66f, v + 0.06f, 0.12f), iso.p(u + 0.66f, v + 0.58f, 0.12f), w * 0.8f)
    drawLine(c.metal, iso.p(u + 0.72f, v + 0.12f, 0.46f), iso.p(u + 0.86f, v + 0.02f, 0.78f), w)
    caixaLuz(iso, P3(u, v, 0.45f), P3(0.72f, 0.62f, 0.06f), c.azul)
    drawLine(c.metal, iso.p(u + 0.12f, v + 0.6f, 0.5f), iso.p(u + 0.12f, v + 0.64f, 0.62f), w)
    drawLine(c.metal, iso.p(u + 0.6f, v + 0.6f, 0.5f), iso.p(u + 0.6f, v + 0.64f, 0.62f), w)
    encosto(iso, u, v)
    caixaLuz(iso, P3(u + 0.62f, v - 0.36f, 0.78f), P3(0.58f, 0.86f, 0.04f), c.prancheta)
}

private fun DrawScope.encosto(iso: Iso, u: Float, v: Float) {
    val c = CoresDaAula.azul
    val frente = listOf(iso.p(u + 0.02f, v + 0.62f, 0.6f), iso.p(u + 0.7f, v + 0.62f, 0.6f), iso.p(u + 0.7f, v + 0.7f,
        1.16f), iso.p(u + 0.02f, v + 0.7f, 1.16f))
    degrade(frente, iso.p(u, v + 0.7f, 1.16f), iso.p(u, v + 0.62f, 0.6f), listOf(c.tom(0.08f), c.tom(-0.16f)))
    val lado = listOf(iso.p(u + 0.7f, v + 0.56f, 0.6f), iso.p(u + 0.7f, v + 0.62f, 0.6f), iso.p(u + 0.7f, v + 0.7f,
        1.16f), iso.p(u + 0.7f, v + 0.64f, 1.16f))
    drawPath(caminho(lado), c.tom(-0.35f))
    drawLine(c.tom(0.4f), iso.p(u + 0.02f, v + 0.7f, 1.16f), iso.p(u + 0.7f, v + 0.7f, 1.16f), iso.lado * 0.0018f)
}

/**
 * A mesa do professor, na frente à direita, como na foto: tampo de madeira, pés pretos e painel na frente.
 * O professor senta atrás dela, entre a mesa e o quadro, então o monitor fica virado para ele e daqui se vê as costas.
 */
private fun DrawScope.mesa(iso: Iso) {
    val c = CoresDaAula
    sombra(iso, P3(9.2f, 2.3f), 1.5f, 0.2f)
    for ((u, v) in listOf(7.95f to 1.75f, 10.18f to 1.75f, 7.95f to 2.58f, 10.18f to 2.58f)) {
        caixaLuz(iso, P3(u, v, 0f), P3(0.07f, 0.07f, 0.76f), c.preto)
    }
    drawPath(caminho(faceV(iso, 2.62f, 8.0f, 10.2f, 0.3f, 0.74f)), c.preto.tom(0.08f))
    caixaLuz(iso, P3(7.9f, 1.7f, 0.76f), P3(2.4f, 1.0f, 0.06f), c.madeira)
    caixaLuz(iso, P3(9.0f, 1.8f, 0.82f), P3(0.8f, 0.28f, 0.03f), Color(0xFFE3E4E0))
    caixaLuz(iso, P3(9.95f, 1.85f, 0.82f), P3(0.09f, 0.14f, 0.03f), Color(0xFFE3E4E0))
    caixaLuz(iso, P3(8.0f, 1.8f, 0.82f), P3(0.4f, 0.55f, 0.05f), Color(0xFFC8432A))
    monitor(iso)
}

/** O monitor visto de trás: base, haste, a carcaça escura e a luz da tela vazando pelas bordas (a tela é do professor). */
private fun DrawScope.monitor(iso: Iso) {
    val c = CoresDaAula
    caixaLuz(iso, P3(9.25f, 2.22f, 0.82f), P3(0.36f, 0.28f, 0.03f), c.preto)
    caixaLuz(iso, P3(9.4f, 2.4f, 0.85f), P3(0.06f, 0.07f, 0.22f), c.preto)
    val tela = P3(8.95f, 2.32f, 1.0f)
    drawPath(caminho(faceV(iso, tela.v, tela.u - 0.03f, tela.u + 0.98f, tela.z - 0.03f, tela.z + 0.65f)), c.tela.tom(0.5f))
    caixaLuz(iso, tela, P3(0.95f, 0.08f, 0.62f), Color(0xFF3A4044))
    drawCircle(Color(0xFF6B7276), iso.lado * 0.0025f, iso.p(9.42f, 2.41f, 1.31f))
}
