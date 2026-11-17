package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.compilandia.tutor.FabGram.Bloco
import kotlin.math.sin

/** Separador, fonte e montadora no desenho "diagrama", com o que acontece dentro delas (como em Maquinas.kt). */

private const val APAGADA = 0.22f

/** Desvio: uma seta por saída, na cor do número da máquina. Passando caixa, só a seta da saída escolhida acende. */
internal fun DrawScope.separador(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cel = CELULA_ARTE * u
    val n = (h / cel).toInt().coerceAtLeast(1)
    laje(p, x, y, w, h, listOf(0.08f to 0.02f, 0.80f to 0.02f, 0.96f to 0.5f / n, 0.80f to 0.98f, 0.08f to 0.98f))
    val origem = Offset(x + w * 0.30f, y + h / 2)
    for (i in 0 until n) {
        val cor = if (n > 1) CORES_NUMERO[i % CORES_NUMERO.size] else p.pk.distribuicao
        val acesa = p.saida == null || p.saida == i + 1
        seta(p, origem, Offset(x + w * 0.80f, y + i * cel + cel / 2), cor.copy(alpha = if (acesa) 1f else APAGADA))
    }
    val pulo = if (p.saida != null && !p.reduz) sin(p.relogio * 20f) * 1.2f * u else 0f
    ficha(p, origem.x, origem.y + pulo, 9 * u, b.nt ?: "S")
}

/** Pilha de caixas com a letra inicial. Soltando, a caixa de cima escorrega pela seta e outra aparece no lugar. */
internal fun DrawScope.fonte(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_2X2.map { (fx, fy) -> fx to 1f - fy })
    val letra = b.nt ?: "S"
    for ((dx, dy) in listOf(-8f to 4f, 8f to 4f)) caixaP(p, cx + dx * u, y + h * 0.42f + dy * u, 14 * u)
    val f = p.progresso
    val topoY = y + h * 0.42f - 8 * u
    if (f == null) {
        caixaP(p, cx, topoY, 14 * u); rotulo(p, letra, cx, topoY, 9 * u, TINTA_CAIXA)
    } else {
        // a de cima desce pela seta; uma nova cai no lugar dela
        val k = suave(f)
        val cy = mistura(topoY, y + h * 0.86f, k)
        caixaP(p, cx, cy, 14 * u); rotulo(p, letra, cx, cy, 9 * u, TINTA_CAIXA)
        val nova = fase(f, 0.5f, 1f)
        if (nova > 0f) { caixaP(p, cx, topoY - (1f - nova) * 8 * u, 14 * u * nova) }
    }
    seta(p, Offset(cx, y + h * 0.68f), Offset(cx, y + h * 0.84f), p.pk.nucleo)
}

/**
 * Impressora de frase: as peças caem pelo funil de cima e o cabeçote imprime cada uma na fita de papel,
 * na casa dela. Com a frase inteira, um selo verde aparece no canto.
 */
internal fun DrawScope.montadora(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    corpoDaImpressora(p, x, y, w, h)
    val frase = p.frase.ifEmpty { listOf("a", "a", "b", "b") }
    val lado = minOf(9 * u, (w * 0.74f) / frase.size - 2 * u).coerceAtLeast(4 * u)
    val xs = posicoes(p, frase.map { it ?: "a" }, cx, lado)
    val fitaY = y + h * 0.80f
    for ((i, s) in frase.withIndex()) {
        if (s != null) ficha(p, xs[i], fitaY, lado, s)
        else vaga(p, xs[i], fitaY, lado)
    }
    // o cabeçote para em cima da próxima casa vazia
    val proxima = frase.indexOfFirst { it == null }
    val cabecoteX = if (proxima >= 0) xs[proxima] else xs.last()
    forma(p, cabecoteX - 5 * u, y + h * 0.50f, 10 * u, 8 * u, p.pk.ok)
    linha(p, Offset(cabecoteX, y + h * 0.58f), Offset(cabecoteX, y + h * 0.66f), p.pk.linha, 1.5f * u)
    val pronta = p.frase.isNotEmpty() && p.frase.none { it == null }
    if (pronta) selo(p, x + w - 10 * u, y + 10 * u)
}

/** O corpo da impressora: funil em cima, a máquina larga, o trilho do cabeçote e a fita de papel saindo embaixo. */
private fun DrawScope.corpoDaImpressora(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    funil(p, cx, y + h * 0.02f, w * 0.34f, h * 0.16f, p.pk.claro)
    laje(p, x, y + h * 0.16f, w, h * 0.52f, listOf(0.03f to 0.10f, 0.97f to 0.10f, 1f to 0.9f, 0f to 0.9f))
    linha(p, Offset(x + w * 0.10f, y + h * 0.48f), Offset(x + w * 0.90f, y + h * 0.48f), p.pk.escuro, 2 * u)
    // a fita: papel claro que sai por baixo da máquina, com sombra
    val canto = Offset(x + w * 0.08f, y + h * 0.66f)
    val fita = Size(w * 0.84f, h * 0.28f)
    drawRoundRect(p.pk.sombra, canto + Offset(u, 2 * u), fita, CornerRadius(2 * u))
    drawRoundRect(PECA, canto, fita, CornerRadius(2 * u))
    drawRoundRect(p.pk.linha, canto, fita, CornerRadius(2 * u), Stroke(u))
}

/** Selo verde de "frase pronta": círculo com um visto. */
private fun DrawScope.selo(p: Pincel, cx: Float, cy: Float) {
    val u = p.u
    disco(p, cx, cy, 7 * u, p.pk.linha); disco(p, cx, cy, 6 * u, p.pk.ok)
    val visto = Path().apply {
        moveTo(cx - 3 * u, cy); lineTo(cx - u, cy + 2.5f * u); lineTo(cx + 3.5f * u, cy - 2.5f * u)
    }
    drawPath(visto, p.pk.linha, style = Stroke(1.8f * u))
}
