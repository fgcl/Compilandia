package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.compilandia.tutor.Falha
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Modelo
import com.compilandia.tutor.FabGram.TipoBloco
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Altura da faixa da frente, em pixels da arte. */
private const val F = 8f

private fun lerp(a: Color, b: Color, f: Float) = Color(a.red + (b.red - a.red) * f, a.green + (b.green - a.green) * f, a.blue + (b.blue - a.blue) * f, 1f)
private fun px(v: Float, density: Float) = TextUnit(v / density, TextUnitType.Sp)

/** Corpo do bloco: sombra pontilhada, topo com luz em cima e à esquerda e a faixa da frente. */
private fun DrawScope.corpo(x: Float, y: Float, w: Float, h: Float, u: Float, corTopo: Color) {
    val passoS = 2 * u
    var sy = 0f
    while (sy < h) { var sx = if (((sy / passoS).toInt()) % 2 == 0) 0f else passoS; while (sx < w) { r(x + sx + 6 * u, y + sy + 6 * u, passoS, passoS, Color.Black.copy(alpha = 0.3f)); sx += passoS * 2 }; sy += passoS }
    r(x, y, w, h, OL)
    r(x + u, y + h - F * u, w - 2 * u, (F - 1) * u, if (corTopo == TOPO) FRENTE else lerp(corTopo, Color.Black, 0.35f)); r(x + u, y + h - F * u, w - 2 * u, u, FRENTE_D)
    r(x + u, y + u, w - 2 * u, h - (F + 1) * u, corTopo); r(x + u, y + u, w - 2 * u, u, lerp(corTopo, Color.White, 0.25f)); r(x + u, y + u, u, h - (F + 1) * u, lerp(corTopo, Color.White, 0.18f))
    r(x + u, y + h - (F + 1) * u, w - 2 * u, u, lerp(corTopo, Color.Black, 0.25f))
}

internal fun DrawScope.bloco(cena: Cena, b: Bloco, brilhos: MutableList<Triple<Offset, Float, Color>>) {
    val cel = cena.cel; val u = cena.u
    val x = b.x * cel; val y = b.y * cel; val w = b.w * cel; val h = b.h * cel
    val movendo = cena.fantasma?.first == b.id
    val corTopo = when {
        b.tipo == TipoBloco.FONTE -> Color(0xFF8A6A3E); b.modelo == Modelo.COFRE -> Color(0xFF5A6F8A)
        b.tipo == TipoBloco.CESTO -> Color(0xFF7A3426); b.tipo == TipoBloco.MONTADORA -> Color(0xFF4F6B58); else -> TOPO
    }
    corpo(x, y, w, h, u, corTopo)
    val th = h - F * u
    val cx = x + w / 2; val cy = y + th / 2
    val ativo = cena.trabalhando(b.id)
    when (b.tipo) {
        TipoBloco.FONTE -> for ((a, bb) in listOf(-7f to -4f, 6f to -6f, 0f to 5f)) caixa(cx + a * u * 2, cy + bb * u * 2, 22 * u, u)
        TipoBloco.SEPARADOR -> separador(cena, b, cx, cy, brilhos)
        TipoBloco.MONTADORA -> montadora(cena, x, cy, w)
        TipoBloco.CESTO -> {
            r(x + 5 * u, y + 5 * u, w - 10 * u, th - 10 * u, Color(0xFF3A1810))
            val c = cena.c
            if (c != null && cena.t >= c.duracao && !c.saiu) caixa(cx, cy, 22 * u, u)
        }
        TipoBloco.MAQUINA -> maquina(cena, b, x, y, w, th, ativo, brilhos)
    }
    // sem esteira de saída: a máquina não tem para onde mandar o que faz
    val precisaSair = b.tipo == TipoBloco.FONTE || b.tipo == TipoBloco.SEPARADOR || (b.tipo == TipoBloco.MAQUINA && b.prod?.rhs?.isNotEmpty() == true)
    if (precisaSair && cena.pl.saidaDe(b) == null) {
        drawCircle(OL, 5 * u, Offset(x + w - 5 * u, y + 5 * u)); drawCircle(VERMELHO, 4 * u, Offset(x + w - 5 * u, y + 5 * u))
    }
    if (b.id == cena.sel) drawRect(AC2, Offset(x - u, y - u), Size(w + 2 * u, h + 2 * u), style = Stroke(2 * u))
    if (movendo) r(x, y, w, h, Color(0x88101215))
    if (ativo) r(x + 4 * u, y + 4 * u, 3 * u, 3 * u, VERDE)
}

private fun DrawScope.separador(cena: Cena, b: Bloco, cx: Float, cy: Float, brilhos: MutableList<Triple<Offset, Float, Color>>) {
    val u = cena.u
    val emperra = cena.tem(b.nt, Falha.CONFLITO_LL, Falha.CONFLITO_SLR)
    val cor = if (emperra) VERMELHO else AC
    for (j in 0 until 8) { r(cx - j * u, cy - (8 - j) * u, (2 * j + 1) * u, u, cor); r(cx - j * u, cy + (8 - j) * u, (2 * j + 1) * u, u, cor) }
    r(cx - 8 * u, cy, 17 * u, u, cor)
    val est = TextStyle(color = OL, fontSize = px(cena.cel * 0.32f, density), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    val lay = cena.medidor.measure(b.nt.orEmpty(), est)
    drawCircle(cor, 7 * u, Offset(cx, cy))
    drawText(lay, topLeft = Offset(cx - lay.size.width / 2f, cy - lay.size.height / 2f))
    if (cena.tem(b.nt, Falha.PREFIXO)) { drawCircle(Color(0xFFFFD64A), 3 * u, Offset(cx + 11 * u, cy - 9 * u)); brilhos += Triple(Offset(cx + 11 * u, cy - 9 * u), 10 * u, Color(0xFFFFD64A)) }
    brilhos += Triple(Offset(cx, cy), 9 * u, cor)
}

private fun DrawScope.montadora(cena: Cena, x: Float, cy: Float, w: Float) {
    val u = cena.u
    val c = cena.c
    val chegou = c?.chegadas?.count { it.t <= cena.t } ?: 0
    val n = 3
    val passo = (w - 12 * u) / n
    for (i in 0 until n) {
        val sx = x + 6 * u + i * passo
        r(sx, cy - 6 * u, passo - 3 * u, 12 * u, OL); r(sx + u, cy - 5 * u, passo - 5 * u, 10 * u, if (i < chegou) TOPO_D else FRENTE_D)
        if (i < chegou) drawCircle(PECA, 4 * u, Offset(sx + (passo - 3 * u) / 2, cy))
    }
    if (cena.laudo?.problemas?.any { it.falha == Falha.AMBIGUA } == true || c?.ambigua == true) {
        for (k in 0 until 2) { r(x + (6 + k * 9) * u, cy - 15 * u, 7 * u, 9 * u, OL); r(x + (7 + k * 9) * u, cy - 14 * u, 5 * u, 7 * u, Color(0xFF8FB8FF)) }
    }
}

private fun DrawScope.maquina(cena: Cena, b: Bloco, x: Float, y: Float, w: Float, th: Float, ativo: Boolean, brilhos: MutableList<Triple<Offset, Float, Color>>) {
    val u = cena.u
    val cx = x + w / 2; val cy = y + th / 2
    val giro = if (ativo && !cena.reduz) cena.relogio * 9f else 0.4f
    when (b.modelo) {
        Modelo.PRENSA -> {
            r(x + 4 * u, cy - 3 * u, w - 8 * u, 6 * u, OL); r(x + 5 * u, cy - 2 * u, w - 10 * u, 4 * u, TOPO_D)
            drawCircle(OL, 13 * u, Offset(cx, cy)); drawCircle(TOPO_D, 12 * u, Offset(cx, cy))
            for (i in 0 until 4) { val q = giro + i * PI.toFloat() / 2; for (j in 1 until 11) r(cx + cos(q) * j * u - u, cy + sin(q) * j * u - u, 2 * u, 2 * u, TOPO_L) }
            drawCircle(AC, 3 * u, Offset(cx, cy))
            brilhos += Triple(Offset(cx, cy), if (ativo) 26 * u else 14 * u, AC)
        }
        Modelo.ARMARIO -> {
            r(x + 5 * u, y + 5 * u, w - 10 * u, th - 12 * u, OL); r(x + 6 * u, y + 6 * u, w - 12 * u, th - 14 * u, TELA)
            for (k in 0 until 4) r(x + 9 * u, y + (10 + k * 7) * u, (w - 18 * u) * (0.4f + 0.15f * ((k + b.id) % 4)), 2 * u, TELA_L.copy(alpha = if (ativo && cena.pisca) 1f else 0.6f))
            for (k in 0 until b.w * 3) drawCircle(listOf(AC, VERMELHO, VERDE)[k % 3], 1.5f * u, Offset(x + (8 + k * 9) * u, y + th - 4 * u))
            brilhos += Triple(Offset(cx, y + 14 * u), if (ativo) 22 * u else 12 * u, TELA_L)
        }
        Modelo.BRACO -> {
            drawCircle(OL, 5 * u, Offset(cx, y + th - 10 * u)); drawCircle(AC, 4 * u, Offset(cx, y + th - 10 * u))
            val a = if (ativo && !cena.reduz) sin(cena.relogio * 8f) * 4f * u else 0f
            for (j in 0 until 14) r(cx - u + j * 0.3f * u, y + th - (12 + j) * u, 3 * u, 2 * u, AC2)
            for (j in 0 until 8) r(cx + 3 * u - j * 0.7f * u + a, y + th - (26 + j * 0.3f) * u, 3 * u, 2 * u, AC2)
            caixa(cx, y + 11 * u, 12 * u, u)
        }
        Modelo.COFRE -> {
            for (k in 0 until 48) { val q = k / 48f * 2 * PI.toFloat(); r(cx + cos(q) * 8 * u, cy + sin(q) * 8 * u, u, u, AC) }
            for (i in 0 until 2) { val q = giro + i * PI.toFloat() / 2; for (j in -7..7) r(cx + cos(q) * j * u, cy + sin(q) * j * u, u, u, AC2) }
            brilhos += Triple(Offset(cx, cy), if (ativo) 18 * u else 10 * u, AC)
        }
        null -> {}
    }
    val p = b.prod
    p?.let { com.compilandia.tutor.FabGram.numero(cena.pl.g, it) }?.let { numero(cena, it, x + 6 * u, y + 6 * u, 5.5f * u) }
    // esteira piscando virou máquina piscando: a regra que começa pela própria letra (ou por outra letra, na indireta)
    val recursiva = p != null && cena.tem(b.nt, Falha.RECURSAO, Falha.RECURSAO_INDIRETA) && p.rhs.firstOrNull()?.let { cena.pl.g.isNonTerminal(it) } == true
    if (recursiva && (cena.pisca || cena.reduz)) drawRect(AC, Offset(x, y), Size(w, th + F * u), style = Stroke(2 * u))
    if (cena.tem(b.nt, Falha.INALCANCAVEL)) {
        r(x, y, w, th + F * u, Color(0x99121519))
        for (k in 0 until 30) r(x + hsh(k, b.id) * w, y + hsh(b.id, k) * th, 1.5f * u, u, Color(0x88CDC6B4))
    }
    if (cena.tem(b.nt, Falha.IMPRODUTIVO)) for (k in 0 until 5) {
        val ph = if (cena.reduz) k / 5f else ((cena.relogio * 0.5f + k / 5f) % 1f)
        drawCircle(Color(0xFF969691).copy(alpha = 0.4f * (1 - ph)), (4 + ph * 9) * u, Offset(cx + sin(k * 2.3f + ph * 3) * 6 * u, y - ph * 24 * u))
    }
}

/** A plaqueta na frente do bloco: a regra da máquina, ou o nome do bloco. */
internal fun DrawScope.placa(cena: Cena, b: Bloco) {
    val cel = cena.cel; val u = cena.u
    if (cel < 18f * density) return
    val texto = when (b.tipo) {
        TipoBloco.MAQUINA -> b.prod!!.let { it.lhs + "→" + it.rhs.joinToString(" ").ifEmpty { "ε" } }
        TipoBloco.FONTE -> cena.nomes.fonte
        TipoBloco.SEPARADOR -> cena.nomes.separador(b.nt.orEmpty())
        TipoBloco.MONTADORA -> cena.nomes.montadora
        TipoBloco.CESTO -> cena.nomes.cesto
    }
    val maq = b.tipo == TipoBloco.MAQUINA
    val est = TextStyle(color = if (maq) OL else PLACA, fontSize = px(cel * if (maq) 0.21f else 0.19f, density), fontWeight = FontWeight.Bold, fontFamily = if (maq) FontFamily.Monospace else FontFamily.Default)
    val maxW = if (maq) (b.w * cel - 2 * u) else (b.w * cel + cel)
    val lay = cena.medidor.measure(texto, est, overflow = TextOverflow.Ellipsis, softWrap = false, maxLines = 1, constraints = Constraints(maxWidth = maxW.toInt().coerceAtLeast(1)))
    val px0 = b.cx * cel - lay.size.width / 2f
    if (maq) {
        // plaquinha creme presa na frente da máquina
        val py = (b.y + b.h) * cel - F * u + (F * u - lay.size.height) / 2f
        r(px0 - 2 * u, py - u / 2, lay.size.width + 4 * u, lay.size.height + u, OL)
        r(px0 - 1.5f * u, py, lay.size.width + 3 * u, lay.size.height.toFloat(), PLACA)
        drawText(lay, topLeft = Offset(px0, py))
    } else {
        val py = (b.y + b.h) * cel + u
        r(px0 - 3 * u, py, lay.size.width + 6 * u, lay.size.height.toFloat(), Color(0xE00E1013))
        drawText(lay, topLeft = Offset(px0, py))
    }
}
