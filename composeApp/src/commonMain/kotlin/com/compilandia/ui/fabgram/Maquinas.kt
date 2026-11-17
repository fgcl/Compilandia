package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.compilandia.tutor.FabGram.Bloco
import kotlin.math.sin

/**
 * As máquinas de regra, no desenho "diagrama" que o dono escolheu (inspirado no shapez), e o que acontece
 * dentro delas. Parada, a máquina mostra a regra:
 * a caixa que entra embaixo e as fichas que saem em cima. Trabalhando ([Pincel.progresso] de 0 a 1),
 * a caixa sobe, treme, a tampa abre e as fichas saltam uma a uma para o lugar delas.
 */

/** De 0 a 1 enquanto [f] vai de [a] a [b]; antes é 0, depois é 1. */
internal fun fase(f: Float, a: Float, b: Float): Float = ((f - a) / (b - a)).coerceIn(0f, 1f)

/** Começa e termina devagar. */
internal fun suave(x: Float): Float = x * x * (3 - 2 * x)

internal fun mistura(a: Float, b: Float, k: Float): Float = a + (b - a) * k

/** O centro de cada ficha de uma fileira centrada em [cx], como a [fileira] desenha. */
internal fun posicoes(p: Pincel, simbolos: List<String>, cx: Float, lado: Float): List<Float> {
    val larguras = simbolos.map { larguraDaFicha(it, lado) }
    var x = cx - (larguras.sum() + 2 * p.u * (simbolos.size - 1)) / 2
    return larguras.map { l -> (x + l / 2).also { x += l + 2 * p.u } }
}

/** Uma caixa de papelão com a letra, a tampa abrindo de 0 (fechada) a 1 (aberta). */
private fun DrawScope.caixaComLetra(p: Pincel, cx: Float, cy: Float, lado: Float, letra: String, abertura: Float) {
    if (abertura > 0f) {
        val u = p.u
        for (lado2 in listOf(-1f, 1f)) {
            val pontaX = cx + lado2 * mistura(2 * u, lado / 2 + 5 * u, abertura)
            val ponta = Offset(pontaX, cy - lado / 2 - abertura * 6 * u)
            val aba = Path().apply {
                moveTo(cx + lado2 * lado / 2, cy - lado / 2); lineTo(ponta.x, ponta.y)
                lineTo(cx, cy - lado / 2); close()
            }
            drawPath(aba, CAIXA); drawPath(aba, CAIXA_OL, style = Stroke(u))
        }
    }
    caixaP(p, cx, cy, lado)
    if (abertura < 1f) rotulo(p, letra, cx, cy, lado * 0.62f, TINTA_CAIXA.copy(alpha = 1f - abertura))
}

/**
 * As fichas da regra saltando do [centro] para o lugar delas, uma depois da outra, entre [inicio] e o fim do trabalho.
 * O lugar que ainda não recebeu ficha mostra só o contorno.
 */
private fun DrawScope.fichasSaltando(
    p: Pincel, simbolos: List<String>, centro: Offset, alvoY: Float, lado: Float, f: Float,
) {
    val xs = posicoes(p, simbolos, centro.x, lado)
    val inicio = SALTO_COMECA
    val cada = (1f - inicio) / simbolos.size.coerceAtLeast(1)
    for ((i, s) in simbolos.withIndex()) {
        val k = suave(fase(f, inicio + i * cada, inicio + (i + 1) * cada))
        if (k <= 0f) { vaga(p, xs[i], alvoY, lado); continue }
        val y = mistura(centro.y, alvoY, k) - sin(k * kotlin.math.PI.toFloat()) * 6 * p.u
        ficha(p, mistura(centro.x, xs[i], k), y, lado * (0.6f + 0.4f * k), s)
    }
}

private const val SALTO_COMECA = 0.5f
private const val VAGA = 0.45f

/** O lugar de uma ficha que ainda não chegou: só um contorno fino, para não parecer um buraco. */
internal fun DrawScope.vaga(p: Pincel, cx: Float, cy: Float, lado: Float) =
    drawCircle(p.pk.linha.copy(alpha = VAGA), lado / 2, Offset(cx, cy), style = Stroke(p.u))

/** Tremida da caixa enquanto a máquina mexe nela. */
private fun Pincel.tremor(f: Float): Float = if (reduz || f !in 0.3f..0.5f) 0f else sin(relogio * 60f) * 1.2f * u

/** Desempacotadora: a caixa sobe pelo funil, treme, abre a tampa e as fichas da regra saltam para fora. */
internal fun DrawScope.prensa(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_2X2)
    val simbolos = simbolosDe(b, 3)
    val f = p.progresso
    if (f == null) {
        ficha(p, cx, y + h * 0.80f, 11 * u, b.nt ?: "S")
        seta(p, Offset(cx, y + h * 0.71f), Offset(cx, y + h * 0.64f), p.pk.linha)
        caixaAberta(p, cx, y + h * 0.52f, 12 * u)
        seta(p, Offset(cx, y + h * 0.38f), Offset(cx, y + h * 0.31f), p.pk.producao)
        fileira(p, simbolos, cx, y + h * 0.20f, 12 * u)
        return
    }
    val cy = mistura(y + h * 0.80f, y + h * 0.52f, suave(fase(f, 0f, 0.3f)))
    val some = fase(f, SALTO_COMECA, 0.7f)
    if (some < 1f) caixaComLetra(p, cx + p.tremor(f), cy, 12 * u * (1f - some * 0.5f), b.nt ?: "S", fase(f, 0.3f, 0.5f))
    fichasSaltando(p, simbolos, Offset(cx, y + h * 0.48f), y + h * 0.20f, 12 * u, f)
}

/** Abridor: a caixa sobe e abre; sai uma peça, ou, na regra ε, ela vira fumaça. */
internal fun DrawScope.cofre(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_1X1)
    val s = simbolosDe(b, 1)
    val f = p.progresso
    if (f == null) {
        ficha(p, cx, y + h * 0.70f, 8 * u, b.nt ?: "S")
        seta(p, Offset(cx, y + h * 0.58f), Offset(cx, y + h * 0.46f), p.pk.producao)
        if (s.isEmpty()) fumaca(p, cx, y + h * 0.30f, 0.5f) else ficha(p, cx, y + h * 0.30f, 8 * u, s[0])
        return
    }
    val cy = mistura(y + h * 0.70f, y + h * 0.46f, suave(fase(f, 0f, 0.3f)))
    if (f < 0.6f) caixaComLetra(p, cx + p.tremor(f), cy, 9 * u, b.nt ?: "S", fase(f, 0.3f, 0.5f))
    if (s.isEmpty()) { if (f > 0.5f) fumaca(p, cx, y + h * 0.40f, fase(f, 0.5f, 1f)) }
    else fichasSaltando(p, s, Offset(cx, y + h * 0.46f), y + h * 0.28f, 8 * u, f)
}

/** Nuvenzinha da regra ε: a caixa some sem soltar nada. [k] de 0 a 1 faz a nuvem subir e sumir. */
private fun DrawScope.fumaca(p: Pincel, cx: Float, cy: Float, k: Float) {
    val u = p.u
    val cor = p.pk.claro.copy(alpha = 1f - k * 0.8f)
    for (i in -1..1) disco(p, cx + i * 4 * u * (1 + k), cy - k * 6 * u - (i * i) * 2 * u, (2.5f + 2 * k) * u, cor)
}

/** Carimbadora: o carimbo desce, a etiqueta da caixa troca de letra e a caixa sobe para a saída. */
internal fun DrawScope.braco(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val cx = x + w / 2
    laje(p, x, y, w, h, LAJE_1X1.map { (fx, fy) -> fx to (fy * 0.5f + if (fy > 0.5f) 0.5f else 0f) })
    val nova = simbolosDe(b, 1).firstOrNull() ?: "B"
    val f = p.progresso ?: -1f
    val trabalhando = f >= 0f
    val desce = if (trabalhando) suave(fase(f, 0.2f, 0.4f)) * (1f - suave(fase(f, 0.45f, 0.6f))) else 0f
    val trocou = trabalhando && f > 0.42f
    val sobe = if (trabalhando) suave(fase(f, 0.6f, 1f)) else 0f
    // a caixa: embaixo com a letra velha; depois do carimbo, com a nova, subindo para a saída
    val cy = mistura(y + h * 0.80f, y + h * 0.17f, sobe)
    if (trabalhando) caixaComLetra(p, cx, cy, 10 * u, if (trocou) nova else (b.nt ?: "A"), 0f)
    else { ficha(p, cx, y + h * 0.80f, 10 * u, b.nt ?: "A"); ficha(p, cx, y + h * 0.17f, 10 * u, nova) }
    val base = y + h * 0.40f + desce * (h * 0.30f)
    linha(p, Offset(cx, base - 10 * u), Offset(cx, base), p.pk.linha, 3 * u)
    forma(p, cx - 9 * u, base, 18 * u, 7 * u, p.pk.producao)
    rotulo(p, nova, cx, base + 3.5f * u, 6 * u, p.pk.linha)
}

/** Linha longa: a caixa entra pela esquerda, abre no meio e as fichas saem em duas fileiras. */
internal fun DrawScope.armario(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    laje(p, x, y, w, h, LAJE_2X2.map { (fx, fy) -> fy to fx })
    val s = simbolosDe(b, 6)
    val metade = (s.size + 1) / 2
    val f = p.progresso
    val cimaY = y + h * 0.36f; val baixoY = y + h * 0.64f; val colunaX = x + w * 0.66f
    if (f == null) {
        ficha(p, x + w * 0.16f, y + h / 2, 10 * u, b.nt ?: "S")
        seta(p, Offset(x + w * 0.26f, y + h / 2), Offset(x + w * 0.40f, y + h / 2), p.pk.producao)
        fileira(p, s.take(metade), colunaX, cimaY, 9 * u); fileira(p, s.drop(metade), colunaX, baixoY, 9 * u)
        return
    }
    val cx = mistura(x + w * 0.16f, x + w * 0.36f, suave(fase(f, 0f, 0.3f)))
    if (f < 0.6f) caixaComLetra(p, cx, y + h / 2 + p.tremor(f), 10 * u, b.nt ?: "S", fase(f, 0.3f, 0.5f))
    val centro = Offset(x + w * 0.36f, y + h / 2)
    fichasSaltando(p, s.take(metade), centro, cimaY, 9 * u, mistura(0f, 1f, fase(f, 0f, 0.8f)))
    fichasSaltando(p, s.drop(metade), centro, baixoY, 9 * u, fase(f, 0.2f, 1f))
}
