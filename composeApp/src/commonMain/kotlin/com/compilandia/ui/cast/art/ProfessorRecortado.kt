package com.compilandia.ui.cast.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform

/*
 * O professor careca em papel recortado (o estilo que o dono escolheu entre seis no painel): cada peça é um
 * polígono de bordas retas, como cortado à tesoura, com uma sombra curta embaixo, e o cotovelo preso por tachinha.
 * O desenho mora num quadro de 200 x 320, com o pé em y = 300; PoseDoProfessor.kt diz como cada junta está.
 */
internal object Papel {
    val pele = Color(0xFFF1BF98)
    val peleS = Color(0xFFD29670)
    val camisa = Color(0xFFA9CCEB)
    val camisaS = Color(0xFF8DB5DB)
    val gravata = Color(0xFF22508A)
    val calca = Color(0xFF3A434D)
    val sapato = Color(0xFF2A2522)
    val cabelo = Color(0xFF6E5A4C)
    val aro = Color(0xFF1F2628)
    val boca = Color(0xFF8A3F31)
    val lingua = Color(0xFFE07C6B)
    val bochecha = Color(0x8CEE8E78)
    val gola = Color(0xFFF4F8FC)
    val cinto = Color(0xFF2A2522)
    val fivela = Color(0xFFC9A54A)
    val caneta = Color(0xFFC8432A)
    val tacha = Color(0xFFB9A27A)
    val lente = Color(0x47C8E6FA)
    val brilho = Color(0xFFF5B700)
    val sombra = Color(0x38000000)
}

/** Um polígono a partir de pares x, y. */
private fun poli(vararg xy: Float) = Path().apply {
    moveTo(xy[0], xy[1])
    for (k in 2 until xy.size step 2) lineTo(xy[k], xy[k + 1])
    close()
}

internal const val CX = 100f
private const val QUADRIL = 200f
internal const val PE = 300f
private const val OMBRO = 130f
private const val COTOVELO = 170f
internal const val CABECA_Y = 80f
private const val RX = 30.45f
private const val RY = 34.65f

/** As peças, já cortadas, no quadro de 200 x 320. */
private object Pecas {
    fun perna(x: Float) = poli(x - 11, QUADRIL - 4, x + 11, QUADRIL - 3, x + 10, QUADRIL + 87, x - 9, QUADRIL + 88)
    fun sapato(x: Float) = Path().apply {
        moveTo(x - 14, PE)
        quadraticTo(x - 15, PE - 14, x - 1, PE - 14)
        quadraticTo(x + 14, PE - 14, x + 14, PE)
        close()
    }
    val corpo = poli(73f, 122f, 129f, 121f, 134f, 203f, 66f, 205f)
    val golaE = poli(87f, 121f, 100f, 134f, 85f, 136f)
    val golaD = poli(113f, 121f, 100f, 134f, 115f, 136f)
    val gravata = poli(95f, 133f, 105f, 133f, 103f, 141f, 107f, 185f, 100f, 194f, 93f, 185f, 97f, 141f)
    fun manga(x: Float) = poli(x - 9, OMBRO - 4, x + 9, OMBRO - 4, x + 7, OMBRO + 42, x - 7, OMBRO + 42)
    fun ante(x: Float) = poli(x - 7, COTOVELO - 3, x + 7, COTOVELO - 3, x + 6, COTOVELO + 34, x - 6, COTOVELO + 34)
    val cabeca = poli(
        CX - RX, CABECA_Y, CX - RX * 0.8f, CABECA_Y - RY * 0.78f, CX - RX * 0.2f, CABECA_Y - RY,
        CX + RX * 0.55f, CABECA_Y - RY * 0.9f, CX + RX, CABECA_Y - RY * 0.25f, CX + RX * 0.9f, CABECA_Y + RY * 0.7f,
        CX + RX * 0.2f, CABECA_Y + RY, CX - RX * 0.7f, CABECA_Y + RY * 0.78f,
    )
    fun franja(s: Float) = Path().apply {
        moveTo(CX + s * RX, CABECA_Y + 8.4f)
        quadraticTo(CX + s * RX * 1.12f, CABECA_Y - 10.5f, CX + s * RX * 0.72f, CABECA_Y - 23.1f)
        quadraticTo(CX + s * RX * 0.84f, CABECA_Y - 6.3f, CX + s * RX * 0.82f, CABECA_Y + 10.5f)
        close()
    }
}

/** Pinta cada cor como ela é, ou toda na cor da sombra (para a sombra curta embaixo de cada peça). */
private class Tinta(val sombra: Boolean) {
    fun c(cor: Color) = if (sombra) Papel.sombra else cor
}

private fun DrawScope.junta(j: Junta, pivo: Offset, desenha: DrawScope.() -> Unit) = withTransform({
    translate(j.dx, j.dy)
    rotate(j.giro, pivo)
    scale(j.sx, j.sy, pivo)
}, desenha)

/** Uma peça de papel: primeiro a sombra deslocada, depois a peça. */
private fun DrawScope.camada(desenha: DrawScope.(Tinta) -> Unit) {
    translate(1.6f, 2.6f) { desenha(Tinta(true)) }
    desenha(Tinta(false))
}

/** O professor inteiro na pose [p]; [t] anima os brilhos da alegria. */
fun DrawScope.professorRecortado(p: PoseProf, t: Float) {
    drawOval(Papel.sombra, Offset(CX - 46 * p.sombra, PE - 5), Size(92 * p.sombra, 14f))
    junta(p.tudo, Offset(CX, PE)) {
        for ((x, j) in listOf(CX - 12 to p.pernaE, CX + 12 to p.pernaD)) camada { perna(it, x, j) }
        camada { tronco(it, p.tronco) }
        camada { cabeca(it, p) }
        camada { braco(it, CX - 30, p.bracoE, p.anteE) }
        camada { braco(it, CX + 30, p.bracoD, p.anteD) }
    }
    if (p.brilhos) brilhos(t)
}

private fun DrawScope.perna(tt: Tinta, x: Float, j: Junta) = junta(j, Offset(x, QUADRIL)) {
    drawPath(Pecas.perna(x), tt.c(Papel.calca))
    drawPath(Pecas.sapato(x), tt.c(Papel.sapato))
}

private fun DrawScope.tronco(tt: Tinta, j: Junta) = junta(j, Offset(CX, QUADRIL)) {
    drawRect(tt.c(Papel.peleS), Offset(92f, 108f), Size(16f, 18f))
    drawPath(Pecas.corpo, tt.c(Papel.camisa))
    if (tt.sombra) return@junta
    drawRect(Papel.camisaS, Offset(109f, 150f), Size(15f, 13f))
    drawRect(Papel.caneta, Offset(112f, 144f), Size(3.5f, 12f))
    drawRect(Papel.cinto, Offset(67f, 193f), Size(66f, 9f))
    drawRect(Papel.fivela, Offset(94f, 193f), Size(12f, 9f))
    drawPath(Pecas.golaE, Papel.gola)
    drawPath(Pecas.golaD, Papel.gola)
    drawPath(Pecas.gravata, Papel.gravata)
}

private fun DrawScope.braco(tt: Tinta, x: Float, ombro: Float, cotovelo: Float) = withTransform({
    rotate(ombro, Offset(x, OMBRO))
}) {
    drawPath(Pecas.manga(x), tt.c(Papel.camisa))
    withTransform({ rotate(cotovelo, Offset(x, COTOVELO)) }) {
        drawPath(Pecas.ante(x), tt.c(Papel.pele))
        drawCircle(tt.c(Papel.pele), 9f, Offset(x, COTOVELO + 42))
    }
    if (!tt.sombra) drawCircle(Papel.tacha, 2.2f, Offset(x, COTOVELO))
}

private fun DrawScope.cabeca(tt: Tinta, p: PoseProf) = junta(p.cabeca, Offset(CX, CABECA_Y + RY)) {
    for (s in listOf(-1f, 1f)) {
        drawOval(tt.c(Papel.pele), Offset(CX + s * RX * 0.97f - 6.8f, CABECA_Y - 4.75f), Size(13.6f, 20f))
    }
    drawPath(Pecas.cabeca, tt.c(Papel.pele))
    if (tt.sombra) return@junta
    for (s in listOf(-1f, 1f)) drawPath(Pecas.franja(s), Papel.cabelo)
    rosto(p.rosto)
}
