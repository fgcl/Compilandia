package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.compilandia.tutor.FabGram.Bloco
import kotlin.math.sin

/**
 * A linha "industrial", no jeito do Mindustry: corpo octogonal de cantos cortados, respiros que ficam
 * laranja quando a máquina trabalha e uma peça de cima que gira. As fichas da regra aparecem numa janela.
 */

/** Janela escura com as fichas da regra dentro. */
private fun DrawScope.janela(p: Pincel, simbolos: List<String>, cx: Float, cy: Float, lado: Float) {
    val u = p.u
    val largura = maxOf(simbolos.size, 1) * (lado + 2 * u) + 4 * u
    val topo = Offset(cx - largura / 2, cy - lado / 2 - 3 * u)
    drawRoundRect(p.pk.linha, topo, Size(largura, lado + 6 * u), CornerRadius(2 * u))
    drawRoundRect(p.pk.tela, topo + Offset(u, u), Size(largura - 2 * u, lado + 4 * u), CornerRadius(u))
    fileira(p, simbolos, cx, cy, lado)
}

/** Respiros no meio de cada lado do bloco. */
private fun DrawScope.respirosDosLados(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    respiro(p, x + w / 2 - 6 * u, y + 2 * u, 12 * u, 4 * u)
    respiro(p, x + w / 2 - 6 * u, y + h - 6 * u, 12 * u, 4 * u)
    respiro(p, x + 2 * u, y + h / 2 - 6 * u, 4 * u, 12 * u)
    respiro(p, x + w - 6 * u, y + h / 2 - 6 * u, 4 * u, 12 * u)
}

/** Prensa industrial: engrenagem grande girando no meio, respiros quentes e as fichas na janela de baixo. */
internal fun DrawScope.prensaIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    chanfrado(p, x, y, w, h, p.pk.producao)
    respirosDosLados(p, x, y, w, h)
    engrenagem(p, Offset(cx, y + h * 0.40f), 13 * u, 10, p.giro * 25f, p.pk.producao)
    janela(p, simbolosDe(b, 3), cx, y + h * 0.76f, 8 * u)
}

/** Cofre industrial: um volante de cofre girando no octógono pequeno, e a peça na janela. */
internal fun DrawScope.cofreIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    chanfrado(p, x, y, w, h, p.pk.producao)
    engrenagem(p, Offset(cx, y + h * 0.42f), 8 * u, 6, p.giro * 40f, p.pk.producao)
    val s = simbolosDe(b, 1)
    if (s.isNotEmpty()) ficha(p, x + w - 8 * u, y + h - 8 * u, 7 * u, s[0])
    else rotulo(p, "ε", x + w - 8 * u, y + h - 8 * u, 8 * u, p.pk.claro)
}

/** Braço numa torre: base octogonal embaixo, braço que gira até a caixa de cima com a letra nova. */
internal fun DrawScope.bracoIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    chanfrado(p, x, y + h / 2, w, h / 2, p.pk.producao)
    forma(p, x + 4 * u, y + 4 * u, w - 8 * u, h / 2 - 4 * u, p.pk.fundo)
    val base = Offset(cx, y + h * 0.75f)
    val ang = -90f + if (p.ativo && !p.reduz) sin(p.relogio * 6f) * 25f else 0f
    rotate(ang, base) {
        linha(p, base, base + Offset(30 * u, 0f), p.pk.linha, 7 * u)
        linha(p, base, base + Offset(30 * u, 0f), p.pk.producao, 4.5f * u)
    }
    disco(p, base.x, base.y, 7 * u, p.pk.linha); disco(p, base.x, base.y, 5.5f * u, p.pk.claro)
    ficha(p, cx, y + h * 0.18f, 10 * u, simbolosDe(b, 1).firstOrNull() ?: "B")
}

/** Armário industrial: esteira interna andando e duas fileiras de fichas na janela. */
internal fun DrawScope.armarioIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    chanfrado(p, x, y, w, h, p.pk.producao)
    respirosDosLados(p, x, y, w, h)
    val passo = if (p.ativo && !p.reduz) (p.relogio * 12f) % 6f else 0f
    forma(p, x + 10 * u, y + 12 * u, w - 20 * u, 10 * u, p.pk.esteira)
    var i = 0f
    while (i < w - 22 * u) {
        detalhe(p, x + 11 * u + i + passo * u, y + 13 * u, u, 8 * u, p.pk.esteiraBorda)
        i += 6 * u
    }
    val s = simbolosDe(b, 6)
    janela(p, s.take(3), cx, y + h * 0.56f, 7 * u)
    if (s.size > 3) janela(p, s.drop(3), cx, y + h * 0.78f, 7 * u)
}

/** Separador como o sorter do Mindustry: moldura funda e, por saída, uma lâmpada com a cor do número. */
internal fun DrawScope.separadorIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cel = 32 * u
    val n = (h / cel).toInt().coerceAtLeast(1)
    chanfrado(p, x, y, w, h, p.pk.distribuicao)
    for (i in 0 until n) {
        val cy = y + i * cel + cel / 2
        disco(p, x + w - 8 * u, cy, 4 * u, p.pk.linha)
        disco(p, x + w - 8 * u, cy, 3 * u, if (n > 1) CORES_NUMERO[i % CORES_NUMERO.size] else p.pk.distribuicao)
    }
    ficha(p, x + w * 0.40f, y + h / 2, 11 * u, b.nt ?: "S")
}

/** Roteador do Mindustry: moldura com cantos claros e um miolo que acende quando passa coisa. */
internal fun DrawScope.roteadorIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cel = 32 * u
    for (i in 0 until b.w.coerceAtLeast(1)) {
        val cx = x + i * cel
        chanfrado(p, cx, y, cel, h, p.pk.distribuicao)
        val c = Offset(cx + cel / 2, y + h / 2)
        // quatro saídas: uma seta curta apontando para cada lado, girando devagar
        rotate(p.giro * 3f, c) {
            for (lado in 0 until 4) rotate(lado * 90f, c) { chevron(p, c.x + 8 * u, c.y, p.pk.distribuicao) }
        }
        disco(p, c.x, c.y, 4 * u, p.pk.linha)
        disco(p, c.x, c.y, 3 * u, p.pk.distribuicao.copy(alpha = if (p.ativo) 1f else 0.7f))
    }
}

/** Silo: anel de respiros em volta, tampa girando e a caixa com a letra inicial no meio. */
internal fun DrawScope.fonteIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val c = Offset(x + w / 2, y + h / 2)
    chanfrado(p, x, y, w, h, p.pk.nucleo)
    disco(p, c.x, c.y, 22 * u, p.pk.linha); disco(p, c.x, c.y, 21 * u, p.pk.escuro)
    rotate(p.giro * 10f, c) {
        for (i in 0 until 8) rotate(i * 45f, c) { respiro(p, c.x - 2 * u, c.y - 20 * u, 4 * u, 6 * u) }
    }
    caixaP(p, c.x, c.y, 16 * u)
    rotulo(p, b.nt ?: "S", c.x, c.y, 10 * u, TINTA_CAIXA)
}

/** Montadora: janela comprida com as casas da frase e uma luz verde de pronto. */
internal fun DrawScope.montadoraIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chanfrado(p, x, y, w, h, p.pk.ok)
    drawRoundRect(p.pk.linha, Offset(x + 8 * u, y + h / 2 - 9 * u), Size(w - 16 * u, 18 * u), CornerRadius(2 * u))
    for (i in 0 until 4) forma(p, x + 11 * u + i * 11 * u, y + h / 2 - 6 * u, 9 * u, 12 * u, p.pk.tela)
    disco(p, x + w / 2, y + 10 * u, 3 * u, p.pk.ok.copy(alpha = if (p.ativo) 1f else 0.45f))
    respiro(p, x + w / 2 - 6 * u, y + h - 7 * u, 12 * u, 4 * u)
}

/** Incinerador: boca redonda com fogo que pulsa e uma grelha por cima. */
internal fun DrawScope.cestoIndustrial(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val c = Offset(x + w / 2, y + h / 2)
    chanfrado(p, x, y, w, h, p.pk.alerta)
    val pulso = if (p.reduz) 0.5f else (sin(p.relogio * 5f) + 1f) / 2f
    drawCircle(Brush.radialGradient(listOf(p.pk.distribuicao, p.pk.alerta, p.pk.linha), c, 18 * u), 18 * u, c)
    drawCircle(p.pk.alerta.copy(alpha = 0.25f * pulso), 22 * u, c)
    for (i in -2..2) detalhe(p, c.x + i * 6 * u - u, c.y - 16 * u, 2 * u, 32 * u, p.pk.linha)
}
