package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale

/*
 * Luz e sombra do estilo diorama (a referência que o dono mandou): cada face com um degradê suave, a quina de cima
 * acesa, sombra macia no chão e cantos escurecidos. Tudo por cima das primitivas do Iso.kt.
 */

fun caminho(pts: List<Offset>) = Path().apply {
    moveTo(pts[0].x, pts[0].y)
    for (k in 1 until pts.size) lineTo(pts[k].x, pts[k].y)
    close()
}

/** Um polígono com degradê linear de [de] até [ate]; se os dois pontos coincidem, a primeira cor, lisa. */
fun DrawScope.degrade(pts: List<Offset>, de: Offset, ate: Offset, cores: List<Color>) {
    if ((ate - de).getDistance() < 0.5f) drawPath(caminho(pts), cores.first())
    else drawPath(caminho(pts), Brush.linearGradient(cores, de, ate))
}

/** Os quatro cantos de uma face de [u] constante, de (v, z) a (v2, z2). */
fun faceU(iso: Iso, u: Float, v: Float, v2: Float, z: Float, z2: Float) =
    listOf(iso.p(u, v, z), iso.p(u, v2, z), iso.p(u, v2, z2), iso.p(u, v, z2))

/** Os quatro cantos de uma face de [v] constante, de (u, z) a (u2, z2). */
fun faceV(iso: Iso, v: Float, u: Float, u2: Float, z: Float, z2: Float) =
    listOf(iso.p(u, v, z), iso.p(u2, v, z), iso.p(u2, v, z2), iso.p(u, v, z2))

/** O retângulo deitado em [z], de (u, v) a (u2, v2). */
fun faceZ(iso: Iso, z: Float, a: P3, b: P3) =
    listOf(iso.p(a.u, a.v, z), iso.p(b.u, a.v, z), iso.p(b.u, b.v, z), iso.p(a.u, b.v, z))

/** Caixa com luz: as faces escurecem para baixo, o topo clareia para o fundo e as quinas da frente brilham. */
fun DrawScope.caixaLuz(iso: Iso, o: P3, t: P3, cor: Color) {
    val (u, v, z) = o
    val (u2, v2, z2) = o + t
    degrade(faceV(iso, v2, u, u2, z, z2), iso.p(u, v2, z2), iso.p(u, v2, z), listOf(cor.tom(-0.03f), cor.tom(-0.17f)))
    degrade(faceU(iso, u2, v, v2, z, z2), iso.p(u2, v, z2), iso.p(u2, v, z), listOf(cor.tom(-0.27f), cor.tom(-0.42f)))
    degrade(faceZ(iso, z2, o, o + t), iso.p(u, v, z2), iso.p(u2, v2, z2), listOf(cor.tom(0.2f), cor.tom(0.07f)))
    val w = iso.lado * 0.0012f
    drawLine(cor.tom(0.4f), iso.p(u, v2, z2), iso.p(u2, v2, z2), w)
    drawLine(cor.tom(0.25f), iso.p(u2, v2, z2), iso.p(u2, v, z2), w)
}

/** Sombra macia no chão: um círculo com degradê, achatado como o chão isométrico. */
fun DrawScope.sombra(iso: Iso, c: P3, r: Float, forca: Float = 0.2f) {
    val m = iso.p(c)
    val rx = r * Iso.DX * RAIZ2 * iso.lado
    scale(1f, Iso.DY / Iso.DX, m) {
        drawCircle(Brush.radialGradient(listOf(Color.Black.copy(alpha = forca), Color.Transparent), m, rx), rx, m)
    }
}

private const val RAIZ2 = 1.414f

/** Uma faixa de canto: escura junto da parede e sumindo em [largura]. [pts] é o polígono, de [de] para [ate]. */
fun DrawScope.cantoEscuro(pts: List<Offset>, de: Offset, ate: Offset, forca: Float) =
    degrade(pts, de, ate, listOf(Color.Black.copy(alpha = forca), Color.Transparent))
