package com.compilandia.ui.room.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * A sala da oficina em projeção isométrica, desenhada por código (o Blender saiu do projeto).
 * Coordenadas do mundo: u corre ao longo da parede da direita, v ao longo da parede da esquerda, z sobe.
 * O chão vai de 0 a 10 nos dois eixos; (0, 0) é o canto do fundo. As medidas saíram da arte aprovada (room_base),
 * para os toques nos móveis e a plataforma do Byte ficarem onde já estavam.
 */

/** Um ponto do mundo, ou um tamanho (du, dv, dz). */
data class P3(val u: Float, val v: Float, val z: Float = 0f) {
    operator fun plus(o: P3) = P3(u + o.u, v + o.v, z + o.z)
}

/** Projeção: (u, v, z) → ponto na tela. [lado] é o lado do quadrado da sala em px. */
class Iso(private val ox: Float, private val oy: Float, val lado: Float) {
    fun p(u: Float, v: Float, z: Float = 0f) = Offset(ox + (u - v) * DX * lado, oy + ((u + v) * DY - z * DZ) * lado)
    fun p(q: P3) = p(q.u, q.v, q.z)

    companion object {
        const val DX = 0.0445f
        const val DY = 0.0248f
        const val DZ = 0.040f

        /** Onde fica, na arte aprovada, o canto do fundo do chão (fração do lado). */
        private const val CANTO_X = 0.497f
        private const val CANTO_Y = 0.393f

        fun sala(lado: Float) = Iso(CANTO_X * lado, CANTO_Y * lado, lado)

        /** Uma projeção em que o ponto [c] do mundo cai em [alvo]: o ícone de um móvel, centrado. */
        fun centrada(c: P3, alvo: Offset, lado: Float) =
            Iso(alvo.x - (c.u - c.v) * DX * lado, alvo.y - ((c.u + c.v) * DY - c.z * DZ) * lado, lado)
    }
}

/** Luz vinda da esquerda: a face da esquerda quase na cor, a da direita na sombra, o topo mais claro. */
private const val FACE_ESQ = -0.08f
private const val FACE_DIR = -0.32f
private const val TOPO = 0.12f

/** Um círculo de raio 1 no chão vira uma elipse de semieixos DX·√2 e DY·√2. */
private const val RAIZ_DE_2 = 1.414f
private const val LADOS_DO_CIRCULO = 18

/** Mais claro (f > 0) ou mais escuro (f < 0). */
fun Color.tom(f: Float): Color = if (f >= 0) lerp(this, Color.White, f) else lerp(this, Color.Black, -f)

/** Um triângulo ou um quadrilátero (quando vem o [d]). */
fun DrawScope.poligono(cor: Color, a: Offset, b: Offset, c: Offset, d: Offset? = null) =
    poligono(cor, listOfNotNull(a, b, c, d))

fun DrawScope.poligono(cor: Color, pts: List<Offset>) {
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (k in 1 until pts.size) lineTo(pts[k].x, pts[k].y)
        close()
    }
    drawPath(path, cor)
}

/**
 * Uma caixa no mundo, do canto [o] com o tamanho [t]: aparecem o topo, a face da esquerda (v + dv) e a da direita
 * (u + du). Quase todo móvel é feito de caixas.
 */
fun DrawScope.caixa(iso: Iso, o: P3, t: P3, cor: Color) {
    val (u, v, z) = o
    val (u2, v2, z2) = o + t
    poligono(cor.tom(FACE_ESQ), iso.p(u, v2, z), iso.p(u2, v2, z), iso.p(u2, v2, z2), iso.p(u, v2, z2))
    poligono(cor.tom(FACE_DIR), iso.p(u2, v, z), iso.p(u2, v2, z), iso.p(u2, v2, z2), iso.p(u2, v, z2))
    poligono(cor.tom(TOPO), iso.p(u, v, z2), iso.p(u2, v, z2), iso.p(u2, v2, z2), iso.p(u, v2, z2))
}

/** Um círculo deitado (no chão ou num tampo), de raio [r] em unidades do mundo; [traco] > 0 desenha só o aro. */
fun DrawScope.disco(iso: Iso, c: P3, r: Float, cor: Color, traco: Float = 0f) {
    val m = iso.p(c)
    val rx = r * Iso.DX * RAIZ_DE_2 * iso.lado
    val ry = r * Iso.DY * RAIZ_DE_2 * iso.lado
    val estilo = if (traco > 0f) Stroke(traco * iso.lado) else Fill
    drawOval(cor, Offset(m.x - rx, m.y - ry), Size(rx * 2, ry * 2), style = estilo)
}

/** Um cilindro em pé, de raio [r] e altura [h]: o corpo e a tampa. */
fun DrawScope.cilindro(iso: Iso, c: P3, r: Float, h: Float, cor: Color) {
    val base = iso.p(c)
    val topo = iso.p(c.u, c.v, c.z + h)
    val rx = r * Iso.DX * RAIZ_DE_2 * iso.lado
    disco(iso, c, r, cor.tom(FACE_DIR))
    drawRect(cor.tom(FACE_ESQ), Offset(base.x - rx, topo.y), Size(rx * 2, base.y - topo.y))
    disco(iso, P3(c.u, c.v, c.z + h), r, cor.tom(TOPO))
}

/** Um círculo de pé numa parede de u constante (a da esquerda), como polígono. */
fun DrawScope.circuloNaParede(iso: Iso, c: P3, r: Float, cor: Color) {
    val pts = (0 until LADOS_DO_CIRCULO).map { k ->
        val a = 2 * PI * k / LADOS_DO_CIRCULO
        iso.p(c.u, c.v + r * cos(a).toFloat(), c.z + r * sin(a).toFloat())
    }
    poligono(cor, pts)
}

/** Um retângulo numa parede de u constante, do canto [a] (v, z) ao canto [b]. */
fun DrawScope.placaEmU(iso: Iso, u: Float, a: Offset, b: Offset, cor: Color) =
    poligono(cor, iso.p(u, a.x, a.y), iso.p(u, b.x, a.y), iso.p(u, b.x, b.y), iso.p(u, a.x, b.y))

/** Um retângulo numa parede de v constante, do canto [a] (u, z) ao canto [b]. */
fun DrawScope.placaEmV(iso: Iso, v: Float, a: Offset, b: Offset, cor: Color) =
    poligono(cor, iso.p(a.x, v, a.y), iso.p(b.x, v, a.y), iso.p(b.x, v, b.y), iso.p(a.x, v, b.y))
