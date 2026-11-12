package com.compilandia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ícones desenhados. No Wasm não há fonte de emoji: um caractere que falta vira quadrado,
 * então botão e marcador usam um destes, nunca emoji.
 */
enum class Icone { VOLTAR, FECHAR, CERTO, ABRIR, RECOLHER }

@Composable
fun IconeDesenhado(icone: Icone, cor: Color, modifier: Modifier = Modifier, tamanho: Dp = 20.dp) {
    Canvas(modifier.size(tamanho)) { desenha(icone, cor) }
}

/** A setinha de abrir e fechar uma lista: para baixo fechada, para cima aberta. */
@Composable
fun SetaDeAbrir(aberta: Boolean, cor: Color = Palette.inkSoft, modifier: Modifier = Modifier) =
    IconeDesenhado(if (aberta) Icone.RECOLHER else Icone.ABRIR, cor, modifier, 12.dp)

private fun DrawScope.desenha(icone: Icone, cor: Color) {
    val w = size.width
    val h = size.height
    val traco = w * if (icone == Icone.ABRIR || icone == Icone.RECOLHER) 0.16f else 0.12f
    fun linha(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(cor, Offset(w * x1, h * y1), Offset(w * x2, h * y2), traco, StrokeCap.Round)
    when (icone) {
        Icone.VOLTAR -> { linha(0.82f, 0.5f, 0.2f, 0.5f); linha(0.2f, 0.5f, 0.46f, 0.24f); linha(0.2f, 0.5f, 0.46f, 0.76f) }
        Icone.FECHAR -> { linha(0.24f, 0.24f, 0.76f, 0.76f); linha(0.76f, 0.24f, 0.24f, 0.76f) }
        Icone.CERTO -> { linha(0.2f, 0.54f, 0.42f, 0.74f); linha(0.42f, 0.74f, 0.8f, 0.3f) }
        Icone.ABRIR -> { linha(0.18f, 0.32f, 0.5f, 0.68f); linha(0.5f, 0.68f, 0.82f, 0.32f) }
        Icone.RECOLHER -> { linha(0.18f, 0.68f, 0.5f, 0.32f); linha(0.5f, 0.32f, 0.82f, 0.68f) }
    }
}

/** A locomotiva que anda pelas setas do autômato. [centro] é o meio dela; [l] a largura. */
fun DrawScope.locomotiva(centro: Offset, l: Float, corpo: Color = Palette.red, contorno: Color = Palette.ink) {
    val alto = l * 0.55f
    val x0 = centro.x - l / 2
    val y0 = centro.y - alto / 2
    val borda = Stroke(l * 0.06f)
    // Caldeira, cabine e chaminé.
    val caldeira = Size(l * 0.62f, alto * 0.55f)
    val pCaldeira = Offset(x0, y0 + alto * 0.25f)
    drawRoundRect(corpo, pCaldeira, caldeira, CornerRadius(l * 0.08f))
    drawRoundRect(contorno, pCaldeira, caldeira, CornerRadius(l * 0.08f), style = borda)
    val cabine = Size(l * 0.38f, alto * 0.8f)
    val pCabine = Offset(x0 + l * 0.62f, y0)
    drawRoundRect(corpo, pCabine, cabine, CornerRadius(l * 0.06f))
    drawRoundRect(contorno, pCabine, cabine, CornerRadius(l * 0.06f), style = borda)
    drawRect(Palette.yellow, Offset(x0 + l * 0.7f, y0 + alto * 0.12f), Size(l * 0.22f, alto * 0.24f))
    drawRect(contorno, Offset(x0 + l * 0.12f, y0), Size(l * 0.12f, alto * 0.25f))
    // Rodas.
    for (fx in floatArrayOf(0.18f, 0.48f, 0.8f)) {
        val c = Offset(x0 + l * fx, y0 + alto * 0.86f)
        drawCircle(contorno, l * 0.12f, c)
        drawCircle(Palette.paper, l * 0.05f, c)
    }
}
