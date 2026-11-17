package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/*
 * A Oficina do Byte sem os móveis da loja: o chão, as duas paredes, as faixas de alerta e os quatro móveis que são
 * botões (o quadro de ferramentas, a mesa com o monitor, o gaveteiro e as caixas), mais a plataforma do Byte.
 * As medidas seguem a arte aprovada: RoomMoveis.kt mede os toques nela, e RoomScreen.kt põe o Byte em (6, 5).
 */

internal object CoresDaSala {
    val chao = Color(0xFF2C2E2D)
    val parede = Color(0xFF41474A)
    val amarelo = Color(0xFFF2B01E)
    val madeira = Color(0xFFE6D2A8)
    val gaveteiro = Color(0xFF4A575E)
    val etiqueta = Color(0xFFEFE8D6)
    val cortica = Color(0xFFD9B57F)
    val metal = Color(0xFF8E979C)
    val vermelho = Color(0xFFC9472F)
    val papelao = Color(0xFFD8C29A)
    val tela = Color(0xFF1E2A2E)
    val escuro = Color(0xFF1B1E1F)
    val branco = Color(0xFFF2EEE3)
}

private const val ALTURA_PAREDE = 8.8f
private const val ESPESSURA = 0.5f

/** O centro da plataforma do Byte, no chão. */
val CENTRO_DA_PLATAFORMA = P3(5.97f, 4.97f)

fun DrawScope.salaVazia(iso: Iso) {
    chaoEParedes(iso)
    faixas(iso)
    quadroDeFerramentas(iso)
    plataforma(iso)
    gaveteiro(iso)
    mesa(iso)
    caixas(iso)
}

private fun DrawScope.chaoEParedes(iso: Iso) {
    val c = CoresDaSala
    caixa(iso, P3(-ESPESSURA, -ESPESSURA, -0.6f), P3(10f + ESPESSURA, 10f + ESPESSURA, 0.6f), c.chao)
    caixa(iso, P3(-ESPESSURA, -ESPESSURA), P3(ESPESSURA, ESPESSURA, ALTURA_PAREDE), c.parede)
    caixa(iso, P3(-ESPESSURA, 0f), P3(ESPESSURA, 10f, ALTURA_PAREDE), c.parede)
    caixa(iso, P3(0f, -ESPESSURA), P3(10f, ESPESSURA, ALTURA_PAREDE), c.parede)
    // As emendas dos painéis, em três por parede.
    for (k in listOf(3.4f, 6.8f)) {
        placaEmU(iso, 0.01f, Offset(k, 0f), Offset(k + 0.06f, ALTURA_PAREDE), c.parede.tom(-0.45f))
        placaEmV(iso, 0.01f, Offset(k, 0f), Offset(k + 0.06f, ALTURA_PAREDE), c.parede.tom(-0.3f))
    }
}

/** As faixas de alerta no chão, junto das paredes: listras amarelas inclinadas. */
private fun DrawScope.faixas(iso: Iso) {
    val passo = 0.7f
    var s = 6.6f
    while (s < 9.9f) {
        poligono(CoresDaSala.amarelo, iso.p(0f, s), iso.p(0f, s + 0.35f), iso.p(0.5f, s + 0.65f), iso.p(0.5f, s + 0.3f))
        poligono(CoresDaSala.amarelo, iso.p(s, 0f), iso.p(s + 0.35f, 0f), iso.p(s + 0.65f, 0.5f), iso.p(s + 0.3f, 0.5f))
        s += passo
    }
    s = 1.2f
    while (s < 3.0f) {
        poligono(CoresDaSala.amarelo, iso.p(0f, s), iso.p(0f, s + 0.35f), iso.p(0.5f, s + 0.65f), iso.p(0.5f, s + 0.3f))
        s += passo
    }
}

/** O quadro de ferramentas na parede da esquerda: cortiça furada, engrenagem, martelo e um esquadro. */
private fun DrawScope.quadroDeFerramentas(iso: Iso) {
    val c = CoresDaSala
    val face = 0.17f
    caixa(iso, P3(0f, 3.8f, 3.6f), P3(face, 5.0f, 3.0f), c.cortica)
    for (v in 0 until 12) for (z in 0 until 7) {
        drawCircle(c.escuro, iso.lado * 0.0035f, iso.p(face, 4.05f + v * 0.4f, 3.85f + z * 0.4f))
    }
    circuloNaParede(iso, P3(face, 6.1f, 5.1f), 0.95f, c.metal)
    circuloNaParede(iso, P3(face, 6.1f, 5.1f), 0.35f, c.escuro)
    placaEmU(iso, face, Offset(7.55f, 3.9f), Offset(7.75f, 5.9f), c.vermelho)
    circuloNaParede(iso, P3(face, 7.65f, 6.0f), 0.32f, c.vermelho)
    placaEmU(iso, face, Offset(4.3f, 3.9f), Offset(4.5f, 5.0f), c.amarelo)
    placaEmU(iso, face, Offset(4.3f, 3.9f), Offset(5.3f, 4.1f), c.amarelo)
    placaEmU(iso, face, Offset(4.1f, 5.2f), Offset(4.45f, 5.55f), c.vermelho)
}

private fun DrawScope.plataforma(iso: Iso) {
    val c = CoresDaSala
    disco(iso, CENTRO_DA_PLATAFORMA, 1.75f, c.amarelo)
    disco(iso, CENTRO_DA_PLATAFORMA, 1.45f, c.escuro)
    disco(iso, CENTRO_DA_PLATAFORMA, 1.1f, c.branco, traco = 0.005f)
}

/** O gaveteiro do mapa, com a gaveta amarela aberta. */
private fun DrawScope.gaveteiro(iso: Iso) {
    val c = CoresDaSala
    val frente = 3.46f
    caixa(iso, P3(3.1f, 0.2f), P3(1.9f, 3.25f, 8.0f), c.gaveteiro)
    for (k in 0 until 8) {
        val z = 0.3f + k * 0.97f
        placaEmV(iso, frente, Offset(3.15f, z), Offset(4.95f, z + 0.05f), c.gaveteiro.tom(-0.4f))
        placaEmV(iso, frente, Offset(3.55f, z + 0.35f), Offset(4.55f, z + 0.62f), c.etiqueta)
    }
    caixa(iso, P3(3.2f, 3.45f, 5.3f), P3(1.7f, 1.6f, 0.6f), c.amarelo)
    placaEmV(iso, 5.06f, Offset(3.45f, 5.45f), Offset(4.55f, 5.72f), c.etiqueta)
}

/** A mesa do laboratório: quatro pés, o tampo, o monitor com código e o teclado. */
private fun DrawScope.mesa(iso: Iso) {
    val c = CoresDaSala
    for ((u, v) in listOf(0.6f to 3.9f, 0.6f to 7.2f, 2.2f to 3.9f, 2.2f to 7.2f)) {
        caixa(iso, P3(u, v), P3(0.3f, 0.3f, 3.0f), c.madeira.tom(-0.05f))
    }
    caixa(iso, P3(1.0f, 4.6f, 2.0f), P3(1.5f, 1.8f, 1.0f), c.madeira)
    drawCircle(c.metal, iso.lado * 0.004f, iso.p(2.51f, 5.5f, 2.5f))
    caixa(iso, P3(0.5f, 3.8f, 3.0f), P3(2.1f, 3.8f, 0.3f), c.madeira)
    caixa(iso, P3(0.9f, 4.8f, 3.3f), P3(0.4f, 0.6f, 0.5f), c.metal)
    caixa(iso, P3(0.8f, 4.1f, 3.8f), P3(0.25f, 2.0f, 1.5f), c.metal.tom(-0.3f))
    val tela = 1.06f
    placaEmU(iso, tela, Offset(4.25f, 3.95f), Offset(5.95f, 5.15f), c.tela)
    val linhas = listOf(Color(0xFF6FD3C6), c.amarelo, Color(0xFF7FB0F0), c.branco, Color(0xFF6FD3C6))
    linhas.forEachIndexed { k, cor ->
        val v0 = 4.45f + (k % 3) * 0.2f
        val z = 4.9f - k * 0.18f
        placaEmU(iso, tela + 0.01f, Offset(v0, z), Offset(v0 + 0.5f + (k % 2) * 0.35f, z + 0.07f), cor)
    }
    caixa(iso, P3(1.55f, 4.3f, 3.3f), P3(0.6f, 1.6f, 0.08f), c.metal.tom(0.25f))
}

/** As caixas da loja, uma aberta e outra fechada. */
private fun DrawScope.caixas(iso: Iso) {
    val c = CoresDaSala
    caixa(iso, P3(6.4f, 7.4f), P3(1.6f, 1.6f, 1.4f), c.papelao)
    val z = 1.4f
    val abas = c.papelao.tom(-0.15f)
    poligono(abas, iso.p(6.4f, 9.0f, z), iso.p(8.0f, 9.0f, z), iso.p(8.0f, 9.7f, z + 0.8f), iso.p(6.4f, 9.7f, z + 0.8f))
    val aba2 = c.papelao.tom(0.05f)
    poligono(aba2, iso.p(8.0f, 7.4f, z), iso.p(8.0f, 9.0f, z), iso.p(8.8f, 9.0f, z + 0.6f), iso.p(8.8f, 7.4f, z + 0.6f))
    caixa(iso, P3(7.9f, 8.7f), P3(1.2f, 1.1f, 1.2f), c.papelao)
    placaEmV(iso, 9.81f, Offset(8.4f, 0.9f), Offset(8.7f, 1.2f), c.papelao.tom(-0.25f))
}
