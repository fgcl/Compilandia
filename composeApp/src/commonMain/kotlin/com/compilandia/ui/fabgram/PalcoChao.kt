package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.compilandia.tutor.FABGRAM_VEL
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir

/** O chão e as esteiras, no jeito do pacote de textura em uso. */

private const val PLANTA_MAIOR = 4
private const val CELULA = 32f
private const val ALFA_CONSTRUINDO = 0.18f

internal fun DrawScope.chao(cena: Cena) {
    val p = cena.pincel(); val k = p.pk
    val cel = cena.cel; val u = cena.u
    val construindo = cena.ferramenta != Ferramenta.MOVER
    val junta = if (construindo) k.distribuicao.copy(alpha = ALFA_CONSTRUINDO) else k.junta
    for (x in cena.gr.x0 until cena.gr.x0 + cena.gr.cols) for (y in cena.gr.y0 until cena.gr.y0 + cena.gr.rows) {
        val ox = x * cel; val oy = y * cel
        ret(ox, oy, cel, cel, if (hsh(x / 2, y / 2) > 0.5f) k.chao else k.chaoAlt)
        when {
            k.brilho -> drawCircle(junta, u, Offset(ox, oy))
            k.contorno -> quadriculado(cena, x, y, junta)
            k.pixel -> pedrinhas(p, x, y, ox, oy)
            else -> juntas(cena, x, y, ox, oy, junta)
        }
    }
}

/** Planta azul: papel quadriculado, com linha mais forte a cada 4 células. */
private fun DrawScope.quadriculado(cena: Cena, x: Int, y: Int, junta: Color) {
    val u = cena.u; val cel = cena.cel
    val forteX = x.mod(PLANTA_MAIOR) == 0
    val forteY = y.mod(PLANTA_MAIOR) == 0
    ret(x * cel, y * cel, cel, if (forteY) u else u / 2, if (forteY) junta.copy(alpha = LINHA_FORTE) else junta)
    ret(x * cel, y * cel, if (forteX) u else u / 2, cel, if (forteX) junta.copy(alpha = LINHA_FORTE) else junta)
}

private const val LINHA_FORTE = 0.45f

/** Metal e desenho: junta fina entre as células e mais forte entre as placas de 2×2. */
private fun DrawScope.juntas(cena: Cena, x: Int, y: Int, ox: Float, oy: Float, junta: Color) {
    val u = cena.u; val cel = cena.cel
    val forte = cena.pincel().pk.escuro.copy(alpha = 0.6f)
    ret(ox, oy, cel, u, if (y.mod(2) == 0) forte else junta)
    ret(ox, oy, u, cel, if (x.mod(2) == 0) forte else junta)
}

/** Pixel: o chão de areia escura do Mindustry, com umas pedrinhas soltas. */
private fun DrawScope.pedrinhas(p: Pincel, x: Int, y: Int, ox: Float, oy: Float) {
    val d = 2 * p.u
    for (i in 0 until 3) {
        val h = hsh(x * 7 + i, y * 13 - i)
        if (h < 0.55f) continue
        detalhe(p, ox + (hsh(x + i, y) * 14).toInt() * d, oy + (hsh(y + i, x) * 14).toInt() * d, d, d, p.pk.junta)
    }
}

/** Esteiras: cada célula liga o centro ao lado de onde vem e ao lado para onde vai, então curvas se fecham. */
internal fun DrawScope.esteiras(cena: Cena) {
    val p = cena.pincel()
    val passo = if (cena.reduz) 0f else (cena.relogio * FABGRAM_VEL) % 1f
    for ((c, d) in cena.pl.esteiras) {
        val lados = ladosDaEsteira(cena, c, d)
        if (p.pk.contorno) trilhos(cena, p, c, lados) else leito(cena, p, c, lados)
        val mx = c.x * cena.cel + cena.cel / 2; val my = c.y * cena.cel + cena.cel / 2
        for (i in 0 until 2) {
            val f = ((passo + i / 2f) % 1f) - 0.5f
            rotate(d.graus(), Offset(mx, my)) { chevron(p, mx + f * cena.cel, my, p.pk.seta) }
        }
        fimDaEsteira(cena, p, c, d)
    }
}

/** Os lados ligados: para onde a esteira empurra e de onde chega coisa (outra esteira ou um bloco). */
private fun ladosDaEsteira(cena: Cena, c: Cel, d: Dir): List<Dir> = Dir.entries.filter { s ->
    val n = c + s
    s == d || cena.pl.esteiras[n]?.let { n + it == c } == true || (cena.pl.blocoEm(n) != null && c - d == n)
}

/** Esteira cheia (metal, pixel, desenho): leito com borda escura e trilhos claros. */
private fun DrawScope.leito(cena: Cena, p: Pincel, c: Cel, lados: List<Dir>) {
    val u = cena.u; val cel = cena.cel; val k = p.pk
    val mx = c.x * cel + cel / 2; val my = c.y * cel + cel / 2
    val meia = 9 * u
    val borda = if (k.id == Pacotes.DESENHO.id) k.traco * u else u
    val m = Offset(mx, my)
    for (s in lados) braco(p, m, s, k.sombra, meia + borda, Offset(2 * u, 3 * u))
    for (s in lados) braco(p, m, s, k.linha, meia + borda)
    detalhe(p, mx - meia - borda, my - meia - borda, 2 * (meia + borda), 2 * (meia + borda), k.linha)
    for (s in lados) braco(p, m, s, k.esteira, meia - u)
    detalhe(p, mx - meia + u, my - meia + u, 2 * meia - 2 * u, 2 * meia - 2 * u, k.esteira)
    for (s in lados) for (lado in listOf(-1f, 1f)) {
        val trilho = lado * (meia - 2 * u)
        braco(p, m, s, k.esteiraBorda, u / 2, Offset(if (s.dx == 0) trilho else 0f, if (s.dx != 0) trilho else 0f))
    }
}

/** Planta e neon: só os dois trilhos, em linha. */
private fun DrawScope.trilhos(cena: Cena, p: Pincel, c: Cel, lados: List<Dir>) {
    val u = cena.u; val cel = cena.cel
    val mx = c.x * cel + cel / 2; val my = c.y * cel + cel / 2
    val meia = 7 * u
    for (s in lados) for (lado in listOf(-1f, 1f)) {
        val n = Offset(if (s.dx == 0) lado * meia else 0f, if (s.dx != 0) lado * meia else 0f)
        val fim = Offset(mx + s.dx * cel / 2, my + s.dy * cel / 2)
        linha(p, Offset(mx, my) + n - Offset(s.dx * meia, s.dy * meia), fim + n, p.pk.esteiraBorda, p.pk.traco * u)
    }
}

/** Um braço do centro da célula até a borda do lado [s], com meia largura [esp]. */
private fun DrawScope.braco(p: Pincel, m: Offset, s: Dir, cor: Color, esp: Float, o: Offset = Offset.Zero) {
    val meio = CELULA * p.u / 2
    if (s.dx != 0) detalhe(p, minOf(m.x, m.x + s.dx * meio) + o.x, m.y - esp + o.y, meio, 2 * esp, cor)
    else detalhe(p, m.x - esp + o.x, minOf(m.y, m.y + s.dy * meio) + o.y, 2 * esp, meio, cor)
}

/** Esteira que acaba no nada: faixa de perigo na ponta. */
private fun DrawScope.fimDaEsteira(cena: Cena, p: Pincel, c: Cel, d: Dir) {
    val prox = c + d
    if (prox in cena.pl.esteiras || cena.pl.blocoEm(prox) != null) return
    val u = cena.u; val cel = cena.cel
    val ex = c.x * cel + cel / 2 + d.dx * (cel / 2 - 3 * u)
    val ey = c.y * cel + cel / 2 + d.dy * (cel / 2 - 3 * u)
    for (i in -2..1) {
        val cor = if (i % 2 == 0) p.pk.alerta else p.pk.linha
        if (d.dx != 0) detalhe(p, ex - u, ey + i * 4 * u, 3 * u, 4 * u, cor)
        else detalhe(p, ex + i * 4 * u, ey - u, 4 * u, 3 * u, cor)
    }
}
