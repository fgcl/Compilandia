package com.compilandia.ui.cast.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/* O desenho do professor careca (ProfessorFigure), num quadro de 200 x 400, com luz vinda da esquerda. */
private object Pele {
    val luz = Color(0xFFF3C9A2)
    val base = Color(0xFFDDA37A)
    val sombra = Color(0xFFB97F59)
}

private val CAMISA = Color(0xFFBCD6EE)
private val CAMISA_SOMBRA = Color(0xFF86A9CB)
private val GRAVATA = Color(0xFF1F4F86)
private val CALCA = Color(0xFF39424A)
private val CALCA_LUZ = Color(0xFF55606A)
private val CALCA_SOMBRA = Color(0xFF262C31)
private val SAPATO = Color(0xFF231F1C)
private val CABELO = Color(0xFF6B5646)
private val TINTA = Color(0xFF1F2628)

internal fun DrawScope.pernas() {
    for (x in listOf(62f, 102f)) {
        drawRoundRect(
            Brush.horizontalGradient(listOf(CALCA_LUZ, CALCA, CALCA_SOMBRA), x, x + 36f),
            Offset(x, 248f), Size(36f, 124f), CornerRadius(10f),
        )
        drawRoundRect(SAPATO, Offset(x - 4f, 364f), Size(44f, 16f), CornerRadius(8f))
        drawRoundRect(Color.White.copy(alpha = 0.25f), Offset(x + 2f, 366f), Size(18f, 4f), CornerRadius(2f))
    }
    drawRoundRect(Color(0xFF2A2522), Offset(58f, 242f), Size(84f, 12f), CornerRadius(4f))
    drawRoundRect(Color(0xFFC9A54A), Offset(93f, 243f), Size(14f, 10f), CornerRadius(2f))
}

internal fun DrawScope.tronco() {
    braco(30f, CAMISA.copy(alpha = 1f))
    braco(148f, CAMISA_SOMBRA)
    val corpo = Path().apply {
        moveTo(60f, 140f); cubicTo(64f, 126f, 136f, 126f, 140f, 140f)
        lineTo(146f, 246f); lineTo(54f, 246f); close()
    }
    drawPath(corpo, Brush.horizontalGradient(listOf(Color(0xFFE2EEF8), CAMISA, CAMISA_SOMBRA), 54f, 146f))
    drawLine(CAMISA_SOMBRA, Offset(100f, 150f), Offset(100f, 244f), 1.5f)
    for (y in listOf(176f, 204f, 232f)) drawCircle(Color.White, 2.5f, Offset(104f, y))
    drawRoundRect(CAMISA_SOMBRA.copy(alpha = 0.5f), Offset(112f, 168f), Size(20f, 18f), CornerRadius(3f))
    drawLine(GRAVATA, Offset(118f, 162f), Offset(118f, 176f), 3f)
    gravata()
    val gola = listOf(Path().apply { moveTo(84f, 130f); lineTo(100f, 150f); lineTo(80f, 150f); close() },
        Path().apply { moveTo(116f, 130f); lineTo(100f, 150f); lineTo(120f, 150f); close() })
    gola.forEach { drawPath(it, Color(0xFFF4F8FC)) }
}

private fun DrawScope.braco(x: Float, manga: Color) {
    drawRoundRect(Brush.horizontalGradient(listOf(manga, CAMISA_SOMBRA), x, x + 22f), Offset(x, 140f), Size(22f, 62f),
        CornerRadius(11f))
    drawRoundRect(Brush.horizontalGradient(listOf(Pele.luz, Pele.base, Pele.sombra), x + 2f, x + 20f),
        Offset(x + 2f, 194f), Size(18f, 48f), CornerRadius(9f))
    drawRoundRect(manga.copy(alpha = 0.9f), Offset(x - 1f, 190f), Size(24f, 9f), CornerRadius(4f))
    drawCircle(Brush.radialGradient(listOf(Pele.luz, Pele.base), Offset(x + 8f, 244f), 14f), 12f, Offset(x + 11f, 248f))
    if (x > 100f) {
        drawRoundRect(Color(0xFF1F5F99), Offset(x + 5f, 236f), Size(7f, 30f), CornerRadius(3f))
        drawRoundRect(Color.White, Offset(x + 5f, 236f), Size(7f, 6f), CornerRadius(2f))
    }
}

private fun DrawScope.gravata() {
    val no = Path().apply { moveTo(93f, 148f); lineTo(107f, 148f); lineTo(104f, 160f); lineTo(96f, 160f); close() }
    val corpo = Path().apply { moveTo(96f, 160f); lineTo(104f, 160f); lineTo(110f, 222f); lineTo(100f,
        234f); lineTo(90f, 222f); close() }
    drawPath(no, GRAVATA)
    drawPath(corpo, Brush.horizontalGradient(listOf(Color(0xFF3A74B3), GRAVATA), 90f, 110f))
    for (k in 0 until 4) drawLine(Color.White.copy(alpha = 0.25f), Offset(92f, 176f + k * 14f),
        Offset(108f, 168f + k * 14f), 2f)
}

internal fun DrawScope.cabeca(piscada: Float) {
    drawRoundRect(Brush.horizontalGradient(listOf(Pele.base, Pele.sombra), 88f, 112f), Offset(88f, 108f),
        Size(24f, 26f), CornerRadius(8f))
    for (x in listOf(55f, 145f)) drawOval(Brush.radialGradient(listOf(Pele.luz, Pele.sombra), Offset(x, 84f), 14f),
        Offset(x - 9f, 70f), Size(18f, 26f))
    drawOval(Brush.radialGradient(listOf(Color(0xFFFBDCBE), Pele.luz, Pele.base, Pele.sombra), Offset(82f, 52f), 72f),
        Offset(58f, 22f), Size(84f, 100f))
    for (x in listOf(58f, 130f)) {
        drawOval(Brush.verticalGradient(listOf(CABELO, Color(0xFF8C7562)), 60f, 104f), Offset(x, 58f), Size(12f, 42f))
    }
    drawOval(Color.White.copy(alpha = 0.55f), Offset(70f, 32f), Size(26f, 12f))
    drawOval(Color.White.copy(alpha = 0.8f), Offset(76f, 34f), Size(10f, 5f))
    rosto(piscada)
}

private fun DrawScope.rosto(piscada: Float) {
    for (x in listOf(82f, 118f)) {
        drawRoundRect(CABELO, Offset(x - 10f, 64f), Size(20f, 4f), CornerRadius(2f))
        drawOval(Color.White, Offset(x - 7f, 76f - 5f * piscada), Size(14f, 10f * piscada))
        drawCircle(TINTA, 3.6f * piscada, Offset(x + 1f, 76f))
        drawCircle(Color.White, 1.2f * piscada, Offset(x, 74.5f))
    }
    for (x in listOf(82f, 118f)) {
        drawRoundRect(Color(0x33BFE3F5), Offset(x - 14f, 66f), Size(28f, 22f), CornerRadius(7f))
        drawRoundRect(TINTA, Offset(x - 14f, 66f), Size(28f, 22f), CornerRadius(7f), style = Stroke(3f))
        drawLine(Color.White.copy(alpha = 0.6f), Offset(x - 9f, 70f), Offset(x - 4f, 70f), 2f)
    }
    drawLine(TINTA, Offset(96f, 74f), Offset(104f, 74f), 3f)
    drawPath(Path().apply { moveTo(100f, 82f); quadraticTo(94f, 94f, 100f, 96f) }, Pele.sombra, style = Stroke(2.5f))
    drawPath(Path().apply { moveTo(86f, 104f); quadraticTo(100f, 114f, 114f, 104f) }, Color(0xFF8A4B3A),
        style = Stroke(3f))
    drawOval(Color(0x22C8432A), Offset(64f, 88f), Size(16f, 10f))
    drawOval(Color(0x22C8432A), Offset(120f, 88f), Size(16f, 10f))
}

val SOMBRA_DO_PROFESSOR = Color.Black.copy(alpha = 0.2f)
