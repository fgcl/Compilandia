package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/*
 * A sala de aula da tela inicial, no estilo diorama: uma laje flutuando, duas paredes grossas e luz suave.
 * Saiu da foto que o dono mandou de uma sala da PUC Poços: piso de cerâmica cinza, paredes brancas, cadeiras
 * azul-piscina com prancheta branca, quadro branco, tela de projeção, crucifixo, mural e porta com placa de saída.
 * O mundo: u corre ao longo da parede da direita (a do quadro), v ao longo da da esquerda (a da porta).
 */
object Aula {
    const val U = 11f
    const val V = 8f
    const val ALTURA = 4.6f
    const val PAREDE = 0.4f
    const val LAJE = 0.7f
}

internal object CoresDaAula {
    val piso = Color(0xFFD3D3CE)
    val rejunte = Color(0xFFB9BAB4)
    val paredeLuz = Color(0xFFF1EFEA)
    val paredeSombra = Color(0xFFDCDAD3)
    val topo = Color(0xFFFAF9F6)
    val laje = Color(0xFFE4E2DC)
    val rodape = Color(0xFFBFC0BB)
    val azul = Color(0xFF2FA9CC)
    val prancheta = Color(0xFFF3F4F2)
    val metal = Color(0xFFB4BABD)
    val preto = Color(0xFF26292B)
    val madeira = Color(0xFF8A6F55)
    val cortica = Color(0xFFC99A66)
    val molduraMural = Color(0xFF6E3B2B)
    val aluminio = Color(0xFFA9AFB2)
    val verdeSaida = Color(0xFF1E9150)
    val bronze = Color(0xFF3A3029)
    val tela = Color(0xFF0B5F55)
    val amarelo = Color(0xFFF5B700)
}

/** O fundo da sala, sem as peças da loja, sem a mesa (frenteDaSala) e sem o professor (um composable por cima). */
fun DrawScope.salaDeAula(iso: Iso) {
    sombraDaLaje(iso)
    laje(iso)
    piso(iso)
    paredes(iso)
    luzDaPorta(iso)
    paredeDaDireita(iso)
    paredeDaEsquerda(iso)
    moveisDaAula(iso, fundo = true)
}

/** A sombra do diorama, embaixo da laje: larga e macia. */
private fun DrawScope.sombraDaLaje(iso: Iso) =
    sombra(iso, P3(Aula.U / 2 + 1f, Aula.V / 2 + 1f, -Aula.LAJE - 1.2f), Aula.U * 0.95f, 0.45f)

private fun DrawScope.laje(iso: Iso) {
    val c = CoresDaAula.laje
    val (e, h) = -Aula.PAREDE to -Aula.LAJE
    degrade(faceV(iso, Aula.V, e, Aula.U, h, 0f), iso.p(0f, Aula.V, 0f), iso.p(0f, Aula.V, h), listOf(c, c.tom(-0.12f)))
    degrade(
        faceU(iso, Aula.U, e, Aula.V, h, 0f), iso.p(Aula.U, 0f, 0f), iso.p(Aula.U, 0f, h),
        listOf(c.tom(-0.2f), c.tom(-0.32f)),
    )
    val w = iso.lado * 0.0014f
    drawLine(Color.White.copy(alpha = 0.8f), iso.p(e, Aula.V), iso.p(Aula.U, Aula.V), w)
    drawLine(Color.White.copy(alpha = 0.5f), iso.p(Aula.U, Aula.V), iso.p(Aula.U, e), w)
}

/** Cerâmica de 1 x 1 com tons que variam pouco, o rejunte e uma luz que cai do fundo para a frente. */
private fun DrawScope.piso(iso: Iso) {
    val c = CoresDaAula
    for (i in 0 until Aula.U.toInt()) for (j in 0 until Aula.V.toInt()) {
        val tom = ((i * 7 + j * 13) % 5 - 2) * 0.012f
        drawPath(caminho(faceZ(iso, 0f, P3(i.toFloat(), j.toFloat()), P3(i + 1f, j + 1f))), c.piso.tom(tom))
    }
    val w = iso.lado * 0.0012f
    for (i in 1 until Aula.U.toInt()) drawLine(c.rejunte, iso.p(i.toFloat(), 0f), iso.p(i.toFloat(), Aula.V), w)
    for (j in 1 until Aula.V.toInt()) drawLine(c.rejunte, iso.p(0f, j.toFloat()), iso.p(Aula.U, j.toFloat()), w)
    val tudo = faceZ(iso, 0f, P3(0f, 0f), P3(Aula.U, Aula.V))
    val luz = listOf(Color.White.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.08f))
    degrade(tudo, iso.p(0f, 0f), iso.p(Aula.U, Aula.V), luz)
    cantoEscuro(faceZ(iso, 0f, P3(0f, 0f), P3(Aula.U, 0.8f)), iso.p(4f, 0f), iso.p(4f, 0.8f), 0.16f)
    cantoEscuro(faceZ(iso, 0f, P3(0f, 0f), P3(0.8f, Aula.V)), iso.p(0f, 4f), iso.p(0.8f, 4f), 0.2f)
}

/** As faces de dentro, os rodapés, os topos grossos e as pontas que aparecem na frente. */
private fun DrawScope.paredes(iso: Iso) {
    val c = CoresDaAula
    val (e, a) = -Aula.PAREDE to Aula.ALTURA
    val luz = listOf(c.paredeLuz.tom(0.3f), c.paredeLuz.tom(-0.04f))
    degrade(faceV(iso, 0f, 0f, Aula.U, 0f, a), iso.p(0f, 0f, a), iso.p(Aula.U, 0f, 0f), luz)
    val sombra = listOf(c.paredeSombra, c.paredeSombra.tom(-0.08f))
    degrade(faceU(iso, 0f, 0f, Aula.V, 0f, a), iso.p(0f, 0f, a), iso.p(0f, Aula.V, 0f), sombra)
    drawPath(caminho(faceV(iso, 0.01f, 0f, Aula.U, 0f, 0.18f)), c.rodape)
    drawPath(caminho(faceU(iso, 0.01f, 0f, Aula.V, 0f, 0.18f)), c.rodape.tom(-0.08f))
    cantoEscuro(faceV(iso, 0f, 0f, 0.7f, 0f, a), iso.p(0f, 0f, 2f), iso.p(0.7f, 0f, 2f), 0.1f)
    cantoEscuro(faceU(iso, 0f, 0f, 0.7f, 0f, a), iso.p(0f, 0f, 2f), iso.p(0f, 0.7f, 2f), 0.14f)
    drawPath(caminho(faceZ(iso, a, P3(e, e), P3(Aula.U, 0f))), c.topo)
    drawPath(caminho(faceZ(iso, a, P3(e, 0f), P3(0f, Aula.V))), c.topo)
    val pontaDir = listOf(c.laje.tom(-0.16f), c.laje.tom(-0.3f))
    degrade(faceU(iso, Aula.U, e, 0f, -Aula.LAJE, a), iso.p(Aula.U, e, a), iso.p(Aula.U, e, 0f), pontaDir)
    val pontaEsq = listOf(c.laje.tom(0.02f), c.laje.tom(-0.1f))
    degrade(faceV(iso, Aula.V, e, 0f, -Aula.LAJE, a), iso.p(e, Aula.V, a), iso.p(e, Aula.V, 0f), pontaEsq)
    val w = iso.lado * 0.0012f
    drawLine(c.rodape, iso.p(0f, 0f, a), iso.p(Aula.U, 0f, a), w)
    drawLine(c.rodape, iso.p(0f, 0f, a), iso.p(0f, Aula.V, a), w)
}

/** A luz que entra pela porta aberta e se deita no chão. */
private fun DrawScope.luzDaPorta(iso: Iso) {
    val pts = listOf(iso.p(0f, PORTA_V), iso.p(0f, PORTA_V2), iso.p(3.4f, PORTA_V2 + 1.1f), iso.p(3.4f, PORTA_V + 0.9f))
    degrade(pts, iso.p(0f, 6.2f), iso.p(3.4f, 7.2f), listOf(Color(0x55FFF4DA), Color(0x00FFF4DA)))
}

internal const val PORTA_V = 5.7f
internal const val PORTA_V2 = 6.9f
internal val CENTRO_DO_CHAO = P3(Aula.U / 2, Aula.V / 2)

/** Para o ponto de luz do monitor e do projetor nas animações da sala. */
internal fun Offset.dentro(lado: Float) = Offset(x / lado, y / lado)
