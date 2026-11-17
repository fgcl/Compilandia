package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * O corpo comum a todo bloco, um por pacote. A [faixa] é a cor da categoria (distribuição, produção, núcleo):
 * em todo pacote ela aparece, para o jogador reconhecer de longe o que é cada bloco.
 */
internal fun DrawScope.chassi(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    when {
        p.pk.pixel -> chassiPixel(p, x, y, w, h, faixa)
        p.pk.contorno -> chassiContorno(p, x, y, w, h, faixa)
        p.pk.id == Pacotes.DESENHO.id -> chassiDesenho(p, x, y, w, h, faixa)
        else -> chassiMetal(p, x, y, w, h, faixa)
    }
}

/** Metal visto de cima: bisel com luz em cima e à esquerda, fundo rebaixado e parafusos nos cantos. */
private fun DrawScope.chassiMetal(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    val u = p.u; val k = p.pk
    drawRoundRect(k.sombra, Offset(x + 3 * u, y + 4 * u), Size(w, h), CornerRadius(4 * u))
    drawRoundRect(k.linha, Offset(x, y), Size(w, h), CornerRadius(4 * u))
    drawRoundRect(k.corpo, Offset(x + u, y + u), Size(w - 2 * u, h - 2 * u), CornerRadius(3 * u))
    ret(x + 2 * u, y + u, w - 4 * u, u, k.claro); ret(x + u, y + 2 * u, u, h - 4 * u, k.claro)
    ret(x + 2 * u, y + h - 2 * u, w - 4 * u, u, k.escuro); ret(x + w - 2 * u, y + 2 * u, u, h - 4 * u, k.escuro)
    drawRoundRect(k.fundo, Offset(x + 4 * u, y + 4 * u), Size(w - 8 * u, h - 8 * u), CornerRadius(2 * u))
    ret(x + 4 * u, y + h - 4 * u, w - 8 * u, u, faixa)
    val m = 2.5f * u
    for ((px, py) in listOf(x + m to y + m, x + w - m to y + m, x + m to y + h - m, x + w - m to y + h - m)) {
        drawCircle(k.escuro, 1.2f * u, Offset(px, py))
        drawCircle(k.claro, 0.6f * u, Offset(px - 0.3f * u, py - 0.3f * u))
    }
}

/** Pixel: tudo em degraus de 2, luz e sombra em faixas retas, cantos com um pixel escuro, como no Mindustry. */
private fun DrawScope.chassiPixel(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    val u = p.u; val k = p.pk; val d = 2 * u
    detalhe(p, x + d, y + d, w, h, k.sombra)
    detalhe(p, x, y, w, h, k.linha)
    detalhe(p, x + d, y + d, w - 2 * d, h - 2 * d, k.corpo)
    detalhe(p, x + d, y + d, w - 2 * d, d, k.claro); detalhe(p, x + d, y + d, d, h - 2 * d, k.claro)
    detalhe(p, x + d, y + h - 2 * d, w - 2 * d, d, k.escuro); detalhe(p, x + w - 2 * d, y + d, d, h - 2 * d, k.escuro)
    detalhe(p, x + 2 * d, y + 2 * d, w - 4 * d, h - 4 * d, k.fundo)
    detalhe(p, x + 2 * d, y + h - 3 * d, w - 4 * d, d, faixa)
    val direita = x + w - 2 * d
    val baixo = y + h - 2 * d
    for ((px, py) in listOf(x + d to y + d, direita to y + d, x + d to baixo, direita to baixo)) {
        detalhe(p, px, py, d, d, k.escuro)
    }
}

/** Planta azul e neon: só linhas. A borda de fora na cor da categoria, um tracejado por dentro. */
private fun DrawScope.chassiContorno(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    val u = p.u
    forma(p, x + u, y + u, w - 2 * u, h - 2 * u, if (p.pk.brilho) faixa else p.pk.linha)
    if (!p.pk.brilho) tracejado(p, x + 4 * u, y + 4 * u, w - 8 * u, h - 8 * u, p.pk.escuro)
    linha(p, Offset(x + 6 * u, y + h - 4 * u), Offset(x + w - 6 * u, y + h - 4 * u), faixa, 1.5f * u)
}

/** Desenho animado: corpo na cor da categoria, contorno grosso, miolo branco e um brilho no canto. */
private fun DrawScope.chassiDesenho(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    val u = p.u; val k = p.pk
    val canto = CornerRadius(k.raio * u)
    drawRoundRect(k.sombra, Offset(x + 2 * u, y + 4 * u), Size(w, h), canto)
    drawRoundRect(faixa, Offset(x + u, y + u), Size(w - 2 * u, h - 2 * u), canto)
    drawRoundRect(k.linha, Offset(x + u, y + u), Size(w - 2 * u, h - 2 * u), canto, Stroke(k.traco * u))
    val miolo = Offset(x + 5 * u, y + 5 * u)
    val tamanho = Size(w - 10 * u, h - 12 * u)
    val cantoMiolo = CornerRadius((k.raio - 3) * u)
    drawRoundRect(k.fundo, miolo, tamanho, cantoMiolo)
    drawRoundRect(k.linha, miolo, tamanho, cantoMiolo, Stroke(1.2f * u))
    drawOval(k.claro.copy(alpha = 0.55f), Offset(x + 3 * u, y + 2.5f * u), Size(6 * u, 2.5f * u))
}
