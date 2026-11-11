package com.compilandia.tutor

import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EPS
import com.compilandia.engine.Grammar
import com.compilandia.engine.ParseNode
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Planta
import kotlin.math.abs

/** Um trecho do caminho de um item: anda pelos [pontos] entre [t0] e [t1] segundos. */
/** [marca] é o número da máquina que o separador carimbou na caixa (1, 2, ...), ou nulo. */
data class Trecho(val simbolo: String, val caixa: Boolean, val pontos: List<Pair<Float, Float>>, val t0: Float, val t1: Float, val marca: Int? = null)

/** A máquina [bloco] trabalha entre [t0] e [t1]. */
data class Trabalho(val bloco: Int, val t0: Float, val t1: Float)

/** A peça [simbolo] chega na casa [casa] da frase no instante [t]. */
data class Chegada(val casa: Int, val simbolo: String, val t: Float)

/** Por que um item parou no meio do caminho. */
enum class Motivo {
    /** O bloco não tem esteira saindo dele. */
    SEM_SAIDA,
    /** A esteira acaba antes do destino. */
    ACABA,
    /** A esteira entra num bloco que não é o destino. */
    BATE,
    /** A esteira fecha um círculo sem passar pelo destino. */
    VOLTA,
}

/** O item [simbolo] parou em [cel] a caminho de [destino]. */
data class Quebra(val simbolo: String, val caixa: Boolean, val cel: Cel, val motivo: Motivo, val destino: Bloco)

data class Corrida(
    /** A frase pedida. */
    val pedida: List<String>,
    /** A frase que a fábrica monta: a pedida, ou a mais perto que ela sabe fazer. */
    val feita: List<String>?,
    /** A gramática faz a frase pedida. */
    val aceita: Boolean,
    /** Duas árvores para a mesma frase. */
    val ambigua: Boolean,
    val arvore: ParseNode?,
    val trechos: List<Trecho>,
    val trabalhos: List<Trabalho>,
    val chegadas: List<Chegada>,
    val duracao: Float,
    /** Onde a fábrica travou, se travou. */
    val quebra: Quebra? = null,
) {
    /** A frase saiu inteira e certa da montadora. */
    val saiu: Boolean get() = aceita && quebra == null
}

const val FABGRAM_VEL = 7f          // células por segundo
const val FABGRAM_TRABALHO = 0.6f   // segundos de máquina por caixa

fun FabGram.corrida(pl: Planta, frase: List<String>): Corrida {
    val g = pl.g
    val dv = DerivationHelper.leftmostDerivations(g, frase, limit = 2)
    if (dv.isNotEmpty()) return Animador(pl, frase, frase, true, dv.size > 1).roda(DerivationHelper.treeOf(g, dv[0]))
    val perto = maisPerto(g, frase)
    val arv = perto?.let { DerivationHelper.leftmostDerivations(g, it, limit = 1).firstOrNull() }?.let { DerivationHelper.treeOf(g, it) }
    return Animador(pl, frase, perto, false, false).roda(arv)
}

/** Para onde vai o item que sai de [de] rumo a [para]: as células da esteira, ou onde ele trava. */
internal sealed interface Rota {
    data class Chega(val celulas: List<Cel>) : Rota
    data class Trava(val celulas: List<Cel>, val motivo: Motivo) : Rota
}

internal fun Planta.rota(de: Bloco, para: Bloco): Rota {
    var c = saidaDe(de) ?: return Rota.Trava(emptyList(), Motivo.SEM_SAIDA)
    val caminho = mutableListOf<Cel>()
    val vistas = HashSet<Cel>()
    while (true) {
        if (!vistas.add(c)) return Rota.Trava(caminho, Motivo.VOLTA)
        caminho += c
        if (para.encosta(c)) return Rota.Chega(caminho)
        val prox = c + esteiras.getValue(c)
        if (blocoEm(prox) != null) return Rota.Trava(caminho, Motivo.BATE)
        if (prox !in esteiras) return Rota.Trava(caminho, Motivo.ACABA)
        c = prox
    }
}

private class Animador(val pl: Planta, val pedida: List<String>, val feita: List<String>?, val aceita: Boolean, val ambigua: Boolean) {
    val trechos = mutableListOf<Trecho>()
    val trabalhos = mutableListOf<Trabalho>()
    val chegadas = mutableListOf<Chegada>()
    var t = 0f
    var quebra: Quebra? = null

    fun roda(arv: ParseNode?): Corrida {
        if (arv != null) processa(arv, pl.fonte, 0)
        return Corrida(pedida, feita, aceita, ambigua, arv, trechos, trabalhos, chegadas.sortedBy { it.t }, t, quebra)
    }

    private fun anda(sim: String, caixa: Boolean, pts: List<Pair<Float, Float>>, marca: Int? = null) {
        var len = 0f
        for (i in 1 until pts.size) len += abs(pts[i].first - pts[i - 1].first) + abs(pts[i].second - pts[i - 1].second)
        val d = len / FABGRAM_VEL
        trechos += Trecho(sim, caixa, pts, t, t + d, marca)
        t += d
    }

    /** Leva o item de [de] até [para]. Falso se ele travou no caminho. */
    private fun leva(sim: String, caixa: Boolean, de: Bloco, para: Bloco, marca: Int? = null): Boolean {
        val centro = listOf(de.cx to de.cy)
        return when (val r = pl.rota(de, para)) {
            is Rota.Chega -> { anda(sim, caixa, centro + r.celulas.map { it.x + 0.5f to it.y + 0.5f } + (para.cx to para.cy), marca); true }
            is Rota.Trava -> {
                anda(sim, caixa, centro + r.celulas.map { it.x + 0.5f to it.y + 0.5f }, marca)
                quebra = Quebra(sim, caixa, r.celulas.lastOrNull() ?: Cel(de.x, de.y), r.motivo, para)
                false
            }
        }
    }

    /** A caixa [no] sai de [de]; [casa0] é a casa da frase onde cai a primeira peça dela. */
    private fun processa(no: ParseNode, de: Bloco, casa0: Int): Boolean {
        val nt = no.symbol
        val sep = pl.separadorDe(nt)
        if (!leva(nt, true, de, sep)) return false
        val filhos = no.children.filter { it.symbol != EPS }
        val p = pl.g.productionsOf(nt).first { pr -> pr.rhs == filhos.map { it.symbol } }
        val m = pl.maquinaDe(p)
        if (!leva(nt, true, sep, m, FabGram.numero(pl.g, p))) return false
        trabalhos += Trabalho(m.id, t, t + FABGRAM_TRABALHO)
        t += FABGRAM_TRABALHO
        val pendentes = mutableListOf<Pair<ParseNode, Int>>()
        var casa = casa0
        for (f in filhos) {
            if (pl.g.isNonTerminal(f.symbol)) pendentes += f to casa
            else {
                if (!leva(f.symbol, false, m, pl.montadora)) return false
                chegadas += Chegada(casa, f.symbol, t)
            }
            casa += f.leaves().size
        }
        // as caixas saem depois das peças, na ordem da frase: a da esquerda primeiro
        for ((f, c) in pendentes) if (!processa(f, m, c)) return false
        return true
    }
}

/**
 * Quando a fábrica não faz a frase, mostra a mais perto que ela faz (contraexemplo).
 * Percorre as derivações mais à esquerda em largura, até o tamanho da frase mais dois.
 */
fun FabGram.maisPerto(g: Grammar, frase: List<String>, limite: Int = 20000): List<String>? {
    val maxT = frase.size + 2
    var melhor: List<String>? = null
    var dist = Int.MAX_VALUE
    val fila = ArrayDeque<List<String>>()
    fila.addLast(listOf(g.start))
    val vistos = HashSet<List<String>>()
    var passos = 0
    while (fila.isNotEmpty() && passos < limite) {
        val f = fila.removeFirst()
        passos++
        val i = f.indexOfFirst { g.isNonTerminal(it) }
        if (i < 0) {
            val d = edicao(f, frase)
            if (d < dist || (d == dist && melhor != null && f.size < melhor.size)) { dist = d; melhor = f }
            if (d == 0) break
            continue
        }
        for (p in g.productionsOf(f[i])) {
            val n = f.subList(0, i) + p.rhs + f.subList(i + 1, f.size)
            if (n.count { !g.isNonTerminal(it) } > maxT || n.size > maxT + 6) continue
            if (vistos.add(n)) fila.addLast(n)
        }
    }
    return melhor
}

fun FabGram.edicao(a: List<String>, b: List<String>): Int {
    val d = Array(a.size + 1) { IntArray(b.size + 1) }
    for (i in 0..a.size) d[i][0] = i
    for (j in 0..b.size) d[0][j] = j
    for (i in 1..a.size) for (j in 1..b.size)
        d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
    return d[a.size][b.size]
}
