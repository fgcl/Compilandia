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
import com.compilandia.fabrica.Papel
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.Falha

/**
 * Um bloco no palco: a arte do tipo (Desenhos.kt) e, por cima, os sinais do estado dele:
 * sem saída, sem regra, selecionado, trabalhando, e os defeitos que o Laboratório achou na regra.
 */
internal fun DrawScope.bloco(cena: Cena, b: Bloco, brilhos: MutableList<Triple<Offset, Float, Color>>) {
    val cel = cena.cel; val u = cena.u
    val x = b.x * cel; val y = b.y * cel; val w = b.w * cel; val h = b.h * cel
    val ativo = cena.trabalhando(b.id)
    val alerta = b.tipo == Papel.SEPARADOR && cena.tem(b.nt, Falha.CONFLITO_LL, Falha.CONFLITO_SLR)
    val pincel = pincelDo(cena, b, ativo, alerta)
    with(Desenhos.de(b.def)) { pinta(pincel, b, x, y, w, h) }
    if (b.tipo == Papel.MAQUINA || b.tipo == Papel.SEPARADOR) {
        brilhos += Triple(Offset(x + w / 2, y + h / 2), if (ativo) 26 * u else 12 * u, corDaCategoria(b.def))
    }
    if (b.tipo == Papel.MAQUINA) sinaisDaMaquina(cena, b, x, y, w, h)
    if (precisaSair(b) && cena.pl.saidaDe(b) == null) lampada(u, x + w - 6 * u, y + 6 * u, VERMELHO)
    if (b.id == cena.sel) drawRect(AC2, Offset(x - u, y - u), Size(w + 2 * u, h + 2 * u), style = Stroke(2 * u))
    if (cena.fantasma?.first == b.id) ret(x, y, w, h, VEU_ARRASTO)
    if (ativo) lampada(u, x + 6 * u, y + 6 * u, VERDE)
}

/** O pincel com o que acontece dentro do bloco agora: quanto do trabalho foi, por onde sai, a frase montada. */
private fun pincelDo(cena: Cena, b: Bloco, ativo: Boolean, alerta: Boolean): Pincel {
    val saindo = cena.saindoDe(b)
    val progresso = when (b.tipo) {
        Papel.MAQUINA -> cena.progresso(b.id)
        Papel.FONTE -> saindo?.second?.takeIf { it < SAIDA_DA_FONTE }?.let { it / SAIDA_DA_FONTE }
        else -> null
    }
    val saida = if (b.tipo == Papel.SEPARADOR) saindo?.first?.marca ?: saindo?.let { 1 } else null
    val frase = if (b.tipo == Papel.MONTADORA) cena.frase() else emptyList()
    return Pincel(
        cena.u, cena.relogio, cena.reduz, ativo, cena.medidor, alerta,
        progresso = progresso, saida = saida, frase = frase,
    )
}

private const val SAIDA_DA_FONTE = 0.35f

/** Sem esteira de saída, o bloco não tem para onde mandar o que faz. */
private fun precisaSair(b: Bloco): Boolean = when (b.tipo) {
    Papel.FONTE, Papel.SEPARADOR, Papel.ROTEADOR -> true
    Papel.MAQUINA -> b.prod?.rhs?.isNotEmpty() == true
    else -> false
}

private fun DrawScope.lampada(u: Float, cx: Float, cy: Float, cor: Color) {
    drawCircle(OL, 3.5f * u, Offset(cx, cy))
    drawCircle(cor, 2.5f * u, Offset(cx, cy))
}

/** O número da máquina, o "?" de quem ainda não tem regra e os defeitos do Laboratório. */
private fun DrawScope.sinaisDaMaquina(cena: Cena, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = cena.u
    val p = b.prod
    if (p == null) {
        val a = if (cena.pisca || cena.reduz) 1f else 0.5f
        drawCircle(OL, 8 * u, Offset(x + w / 2, y + h / 2 - u))
        drawCircle(AC2.copy(alpha = a), 7 * u, Offset(x + w / 2, y + h / 2 - u))
        rotulo(cena.pincel(), "?", x + w / 2, y + h / 2 - u, 10 * u, OL)
        return
    }
    // nos desenhos novos as fichas da regra ficam em cima; o número vai para o canto de baixo
    val seloY = if (Variantes.escolhida(b.def.id) != null) y + h - 7 * u else y + 7 * u
    FabGram.numero(cena.pl.g, p)?.let { numero(cena, it, x + 7 * u, seloY, 5.5f * u) }
    // a regra que começa por uma letra pisca (recursão à esquerda, direta ou indireta)
    val comecaPorLetra = p.rhs.firstOrNull()?.let { cena.pl.g.isNonTerminal(it) } == true
    val recursiva = comecaPorLetra && cena.tem(b.nt, Falha.RECURSAO, Falha.RECURSAO_INDIRETA)
    if (recursiva && (cena.pisca || cena.reduz)) drawRect(AC, Offset(x, y), Size(w, h), style = Stroke(2 * u))
    if (cena.tem(b.nt, Falha.INALCANCAVEL)) poeira(cena, b, x, y, w, h)
    if (cena.tem(b.nt, Falha.IMPRODUTIVO)) fumaca(cena, x + w / 2, y)
}

private fun DrawScope.poeira(cena: Cena, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    ret(x, y, w, h, VEU_POEIRA)
    for (k in 0 until POEIRA) ret(x + hsh(k, b.id) * w, y + hsh(b.id, k) * h, 1.5f * cena.u, cena.u, GRAO_POEIRA)
}

private fun DrawScope.fumaca(cena: Cena, cx: Float, topo: Float) {
    for (k in 0 until FUMACA) {
        val ph = if (cena.reduz) k / FUMACA.toFloat() else ((cena.relogio * 0.5f + k / FUMACA.toFloat()) % 1f)
        val o = Offset(cx + kotlin.math.sin(k * 2.3f + ph * 3) * 6 * cena.u, topo - ph * 24 * cena.u)
        drawCircle(FUMACA_COR.copy(alpha = 0.4f * (1 - ph)), (4 + ph * 9) * cena.u, o)
    }
}

private const val POEIRA = 30
private const val FUMACA = 5

private fun px(v: Float, density: Float) = TextUnit(v / density, TextUnitType.Sp)

/** A plaqueta: a regra da máquina dentro dela, embaixo; o nome dos outros blocos, logo abaixo do bloco. */
internal fun DrawScope.placa(cena: Cena, b: Bloco) {
    val cel = cena.cel; val u = cena.u
    if (cel < 18f * density) return
    // a plaquinha creme fica dentro da máquina grande; na de uma célula ela iria cobrir o desenho
    val maq = b.tipo == Papel.MAQUINA && b.w > 1 && b.h > 1
    val texto = textoDaPlaca(cena, b) ?: return
    val est = TextStyle(
        color = if (maq) PacoteAtual.pacote.placaTinta else PLACA,
        fontSize = px(cel * if (maq) 0.21f else 0.19f, density),
        fontWeight = FontWeight.Bold, fontFamily = if (maq) FontFamily.Monospace else FontFamily.Default,
    )
    val maxW = if (maq) (b.w * cel - 4 * u) else (b.w * cel + cel)
    val lay = cena.medidor.measure(
        texto, est, overflow = TextOverflow.Ellipsis, softWrap = false, maxLines = 1,
        constraints = Constraints(maxWidth = maxW.toInt().coerceAtLeast(1)),
    )
    val px0 = b.cx * cel - lay.size.width / 2f
    val py = if (maq) (b.y + b.h) * cel - 5 * u - lay.size.height else (b.y + b.h) * cel + 2 * u
    if (maq) {
        ret(px0 - 2 * u, py - u / 2, lay.size.width + 4 * u, lay.size.height + u, PacoteAtual.pacote.linha)
        ret(px0 - 1.5f * u, py, lay.size.width + 3 * u, lay.size.height.toFloat(), PacoteAtual.pacote.placa)
    } else {
        ret(px0 - 3 * u, py, lay.size.width + 6 * u, lay.size.height.toFloat(), FUNDO_PLACA)
    }
    drawText(lay, topLeft = Offset(px0, py))
}

private fun textoDaPlaca(cena: Cena, b: Bloco): String? = when (b.tipo) {
    // os desenhos novos (Variantes.kt) mostram a regra nas fichas; a plaquinha ficaria por cima
    Papel.MAQUINA -> if (Variantes.escolhida(b.def.id) != null) null
    else b.prod?.let { it.lhs + "→" + it.rhs.joinToString(" ").ifEmpty { "ε" } }
    Papel.FONTE -> cena.nomes.fonte
    Papel.SEPARADOR -> cena.nomes.separador(b.nt ?: "?")
    Papel.MONTADORA -> cena.nomes.montadora
    Papel.CESTO -> cena.nomes.cesto
    Papel.ROTEADOR -> cena.nomes.roteador
    Papel.JUNCAO -> null
}
