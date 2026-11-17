package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * O corpo dos blocos quadrados (cesto, roteador, junção e o ícone da esteira): metal visto de cima, bisel com luz
 * em cima e à esquerda, fundo rebaixado e parafusos nos cantos. A [faixa] tem a cor da categoria
 * (distribuição, produção, núcleo), para o jogador reconhecer de longe o que é cada bloco.
 */
internal fun DrawScope.chassi(p: Pincel, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
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
