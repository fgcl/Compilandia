package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

/*
 * O campus da PUC em Poços de Caldas visto de cima, em diorama, como a sala. A planta saiu da imagem de satélite do
 * endereço (Av. Padre Francis Cletus Cox) e do OpenStreetMap: pinheiral ao norte, mata ao sul, o bloco das salas com
 * os pátios, o prédio redondo, o de telhado em pirâmide, a capela, a quadra e os estacionamentos.
 * Uma unidade do mundo mede uns 11 m. O chão vai de 0 a [Campus.U] e de 0 a [Campus.V].
 */
object Campus {
    const val U = 38f
    const val V = 28f
    const val LAJE = 1.6f

    /** O telhado do prédio das salas, onde a câmera entra. */
    val ALVO = P3(13.5f, 9.6f, 1.7f)

    /** O lado do mundo em px por lado do quadrado, e onde fica o canto do fundo. */
    private const val ESCALA = 0.33f
    private const val ORIGEM_X = 0.4266f
    private const val ORIGEM_Y = 0.2f

    fun iso(lado: Float) = Iso(ORIGEM_X * lado, ORIGEM_Y * lado, lado * ESCALA)
}

internal object CoresDoCampus {
    val grama = Color(0xFF9CC27A)
    val gramaEscura = Color(0xFF7FAA62)
    val terra = Color(0xFF8A6446)
    val rua = Color(0xFF5E6468)
    val calcada = Color(0xFFD9D5C8)
    val estacionamento = Color(0xFFB4B7B2)
    val pinho = Color(0xFF2F6B4A)
    val copa = Color(0xFF5E9E52)
    val copaLuz = Color(0xFF9CCB76)
    val tronco = Color(0xFF6B4A33)
    val quadra = Color(0xFFC8432A)
    val quadraBorda = Color(0xFF2E7D4F)
    val carros = listOf(Color(0xFFC8432A), Color(0xFFF5B700), Color(0xFF1F5F99), Color(0xFFF7F5EE), Color(0xFF4C585C))
}

fun DrawScope.campus(iso: Iso) {
    sombra(iso, P3(Campus.U / 2 + 2f, Campus.V / 2 + 2f, -Campus.LAJE - 3f), Campus.U * 0.8f, 0.4f)
    chao(iso)
    ruas(iso)
    estacionamentos(iso)
    quadra(iso)
    val r = Random(7)
    val fundo = pinheiral(r) + mata(r, 0f, 0f, 8f, 8f, 40) + mata(r, 13f, 12f, 8f, 2f, 10)
    for (a in fundo.sortedBy { it.u + it.v }) arvore(iso, a)
    predios(iso)
    for (a in mataDoSul(r).sortedBy { it.u + it.v }) arvore(iso, a)
}

/** A laje do diorama: grama em cima, terra nos lados. */
private fun DrawScope.chao(iso: Iso) {
    val c = CoresDoCampus
    val (u, v, h) = Triple(Campus.U, Campus.V, -Campus.LAJE)
    degrade(faceV(iso, v, 0f, u, h, 0f), iso.p(0f, v, 0f), iso.p(0f, v, h),
        listOf(c.terra.tom(0.05f), c.terra.tom(-0.3f)))
    degrade(faceU(iso, u, 0f, v, h, 0f), iso.p(u, 0f, 0f), iso.p(u, 0f, h),
        listOf(c.terra.tom(-0.25f), c.terra.tom(-0.5f)))
    drawPath(caminho(faceV(iso, v, 0f, u, -0.25f, 0f)), c.gramaEscura.tom(-0.1f))
    drawPath(caminho(faceU(iso, u, 0f, v, -0.25f, 0f)), c.gramaEscura.tom(-0.3f))
    degrade(faceZ(iso, 0f, P3(0f, 0f), P3(u, v)), iso.p(0f, 0f), iso.p(u, v),
        listOf(c.grama.tom(0.15f), c.grama.tom(-0.08f)))
    val r = Random(3)
    repeat(26) {
        val q = P3(r.nextFloat() * u, r.nextFloat() * v)
        sombra(iso, q, 1.5f + r.nextFloat() * 2.5f, 0.05f)
    }
    val w = iso.lado * 0.004f
    drawLine(Color.White.copy(alpha = 0.5f), iso.p(0f, v), iso.p(u, v), w)
    drawLine(Color.White.copy(alpha = 0.3f), iso.p(u, v), iso.p(u, 0f), w)
}

private val RUAS = listOf(
    listOf(0f to 6.5f, 6f to 6.8f, 14f to 7.2f, 22f to 6.8f, 26f to 6f, 32f to 5.4f, 36f to 4.2f, 38f to 3f),
    listOf(22f to 6.8f, 22.5f to 10f, 23f to 16f, 22.8f to 22f, 23.4f to 28f),
    listOf(23f to 16f, 29f to 17.5f, 35f to 18f, 38f to 18.2f),
    listOf(36.8f to 4.4f, 37.4f to 12f, 36.6f to 22f, 36f to 28f),
)

private fun DrawScope.ruas(iso: Iso) {
    val c = CoresDoCampus
    val traco = PathEffect.dashPathEffect(floatArrayOf(iso.lado * 0.012f, iso.lado * 0.012f))
    for ((k, rua) in RUAS.withIndex()) {
        val pts = rua.map { (u, v) -> iso.p(u, v) }
        val linha = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (q in pts.drop(1)) lineTo(q.x, q.y)
        }
        val largura = iso.lado * Iso.DX * if (k == 0) 1.1f else 0.85f
        drawPath(linha, c.calcada, style = Stroke(largura * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(linha, c.rua, style = Stroke(largura, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(linha, Color.White.copy(alpha = 0.6f), style = Stroke(iso.lado * 0.002f, pathEffect = traco))
    }
}

/** Os três estacionamentos, com as vagas e os carros. */
private fun DrawScope.estacionamentos(iso: Iso) {
    val vagas = listOf(
        P3(23.8f, 9f) to P3(7.8f, 4.2f),
        P3(15.8f, 12.4f) to P3(2.8f, 4.2f),
        P3(21.2f, 17.6f) to P3(1.4f, 5.2f),
    )
    val r = Random(11)
    for ((o, t) in vagas) {
        drawPath(caminho(faceZ(iso, 0.01f, o, o + t)), CoresDoCampus.estacionamento)
        var u = o.u + 0.5f
        while (u < o.u + t.u - 0.3f) {
            drawLine(Color.White.copy(alpha = 0.7f), iso.p(u, o.v, 0.02f), iso.p(u, o.v + 0.9f, 0.02f),
                iso.lado * 0.0015f)
            if (r.nextFloat() < 0.75f) carro(iso, P3(u + 0.08f, o.v + 0.15f), r)
            u += 0.6f
        }
    }
}

private fun DrawScope.carro(iso: Iso, o: P3, r: Random) {
    val cor = CoresDoCampus.carros[r.nextInt(CoresDoCampus.carros.size)]
    caixaLuz(iso, o, P3(0.42f, 0.7f, 0.18f), cor)
    caixaLuz(iso, o + P3(0.04f, 0.18f, 0.18f), P3(0.34f, 0.36f, 0.12f), Color(0xFF3E5566))
}

private fun DrawScope.quadra(iso: Iso) {
    val c = CoresDoCampus
    drawPath(caminho(faceZ(iso, 0.01f, P3(13.6f, 17.6f), P3(16.2f, 20.4f))), c.quadraBorda)
    drawPath(caminho(faceZ(iso, 0.02f, P3(13.9f, 17.9f), P3(15.9f, 20.1f))), c.quadra)
    val w = iso.lado * 0.002f
    drawLine(Color.White, iso.p(13.9f, 19f, 0.03f), iso.p(15.9f, 19f, 0.03f), w)
    drawPath(caminho(faceZ(iso, 0.02f, P3(16.4f, 18.4f), P3(17.4f, 19.6f))), Color(0xFF3C7FC0))
}

/** Uma árvore: [pinho] em cone, senão copa redonda. */
private data class Arvore(val u: Float, val v: Float, val pinho: Boolean, val r: Float)

private fun pinheiral(r: Random) = (0 until 260).mapNotNull {
    val u = 6f + r.nextFloat() * 30.5f
    val v = r.nextFloat() * 4.6f
    if (u > 23f && v > 3.6f) null else Arvore(u, v, true, 0.5f + r.nextFloat() * 0.25f)
}

private fun mata(r: Random, u0: Float, v0: Float, du: Float, dv: Float, n: Int) =
    (0 until n).map { Arvore(u0 + r.nextFloat() * du, v0 + r.nextFloat() * dv, false, 0.55f + r.nextFloat() * 0.3f) }

private fun mataDoSul(r: Random) = (0 until 150).mapNotNull {
    val u = r.nextFloat() * 20f
    val v = 20.5f + r.nextFloat() * 7.3f
    if (u > 4f + (v - 20.5f) * 2.2f && v < 24f) null else Arvore(u, v, false, 0.55f + r.nextFloat() * 0.3f)
}

private fun DrawScope.arvore(iso: Iso, a: Arvore) {
    val c = CoresDoCampus
    sombra(iso, P3(a.u + 0.3f, a.v + 0.3f), a.r * 1.4f, 0.22f)
    val base = iso.p(a.u, a.v)
    val s = iso.lado * Iso.DX * a.r
    if (a.pinho) {
        val topo = base - Offset(0f, s * 4.2f)
        poligono(c.pinho, topo, base + Offset(s * 1.1f, -s * 0.4f), base + Offset(0f, -s * 0.1f))
        poligono(c.pinho.tom(0.18f), topo, base + Offset(-s * 1.1f, -s * 0.4f), base + Offset(0f, -s * 0.1f))
    } else {
        drawLine(c.tronco, base, base - Offset(0f, s * 1.2f), s * 0.35f)
        val m = base - Offset(0f, s * 1.8f)
        val luz = Brush.radialGradient(listOf(c.copaLuz, c.copa, c.copa.tom(-0.25f)), m - Offset(s * 0.5f, s * 0.6f),
            s * 2f)
        drawCircle(luz, s * 1.25f, m)
    }
}
