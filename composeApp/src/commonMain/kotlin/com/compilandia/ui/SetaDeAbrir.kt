package com.compilandia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * A setinha de abrir e fechar uma lista: para baixo fechada, para cima aberta.
 * Desenhada, e não um caractere: no Wasm, o glifo que falta na fonte vira quadrado.
 */
@Composable
fun SetaDeAbrir(aberta: Boolean, cor: Color = Palette.inkSoft, modifier: Modifier = Modifier) {
    Canvas(modifier.size(12.dp)) {
        val w = size.width
        val h = size.height
        val alto = if (aberta) h * 0.32f else h * 0.68f
        val baixo = if (aberta) h * 0.68f else h * 0.32f
        val traco = w * 0.16f
        drawLine(cor, Offset(w * 0.18f, baixo), Offset(w * 0.5f, alto), traco, StrokeCap.Round)
        drawLine(cor, Offset(w * 0.5f, alto), Offset(w * 0.82f, baixo), traco, StrokeCap.Round)
    }
}
