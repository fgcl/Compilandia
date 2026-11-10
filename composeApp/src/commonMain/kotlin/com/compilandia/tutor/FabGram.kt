package com.compilandia.tutor

import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.Grammar
import com.compilandia.engine.ParseNode
import com.compilandia.engine.Production
import kotlin.math.sign

/**
 * Fábrica de Gramáticas: cada regra é uma máquina numa grade vista de cima.
 *
 * A planta sai da gramática. Cada letra (não terminal) tem uma linha:
 * um desvio no elevador da esquerda manda a caixa ao separador da letra,
 * a esteira de cima leva a caixa até a máquina da regra, a máquina solta as peças
 * em fila, a esteira de baixo leva tudo ao filtro da linha.
 * No filtro, a peça (terminal) desce pelo coletor até a montadora;
 * a caixa (não terminal) volta pela esteira de retorno até o elevador.
 *
 * Fatos vêm do motor: a derivação sai de [DerivationHelper], nunca de um palpite.
 */
object FabGram {

    /** O desenho da máquina segue o papel da regra. */
    enum class Modelo(val w: Int, val h: Int) {
        /** ε ou regra de uma peça só. */
        COFRE(1, 1),
        /** Regra que só troca a letra: A -> B. */
        BRACO(1, 2),
        /** Regra com duas ou três peças. */
        PRENSA(2, 2),
        /** Regra longa, com quatro peças ou mais. */
        ARMARIO(2, 2),
    }

    enum class TipoBloco { FONTE, SEPARADOR, MAQUINA, FILTRO, MONTADORA, CESTO }

    /** Papel de uma esteira, para a cor e para a seta. */
    enum class Papel { ENTRADA, DISTRIBUI, SAIDA, LINHA, RETORNO, COLETOR, ELEVADOR }

    data class Cel(val x: Int, val y: Int)

    data class Bloco(
        val id: Int,
        val tipo: TipoBloco,
        val x: Int, val y: Int, val w: Int, val h: Int,
        val nt: String? = null,
        val prod: Production? = null,
        val modelo: Modelo? = null,
    ) {
        val cx: Float get() = x + w / 2f
        val cy: Float get() = y + h / 2f
    }

    /** Uma esteira é uma fila de células; [dir] diz para onde cada célula empurra. */
    data class Esteira(val celulas: List<Cel>, val dir: List<Cel>, val papel: Papel)

    class Planta(
        val g: Grammar,
        val largura: Int,
        val altura: Int,
        val blocos: List<Bloco>,
        val esteiras: List<Esteira>,
        /** Linha (y da esteira de cima) de cada letra. */
        val linhaDe: Map<String, Int>,
        val xFiltro: Int,
        val xColetor: Int,
        val yMontadora: Int,
    ) {
        fun maquinaDe(p: Production): Bloco = blocos.first { it.prod?.id == p.id }
        fun separadorDe(nt: String): Bloco = blocos.first { it.tipo == TipoBloco.SEPARADOR && it.nt == nt }
        fun filtroDe(nt: String): Bloco = blocos.first { it.tipo == TipoBloco.FILTRO && it.nt == nt }
        val fonte: Bloco get() = blocos.first { it.tipo == TipoBloco.FONTE }
        val montadora: Bloco get() = blocos.first { it.tipo == TipoBloco.MONTADORA }
        val cesto: Bloco get() = blocos.first { it.tipo == TipoBloco.CESTO }
    }

    fun modelo(p: Production, g: Grammar): Modelo = when {
        p.rhs.isEmpty() -> Modelo.COFRE
        p.rhs.size == 1 && g.isNonTerminal(p.rhs[0]) -> Modelo.BRACO
        p.rhs.size == 1 -> Modelo.COFRE
        p.rhs.size >= 4 -> Modelo.ARMARIO
        else -> Modelo.PRENSA
    }

    /** As letras na ordem em que a fábrica mostra: a inicial primeiro. */
    fun letras(g: Grammar): List<String> {
        val ordem = mutableListOf(g.start)
        for (p in g.productions) if (p.lhs !in ordem) ordem += p.lhs
        return ordem
    }

    private const val PASSO_LINHA = 5
    private const val X0 = 4

    fun planta(g: Grammar): Planta {
        val blocos = mutableListOf<Bloco>()
        val esteiras = mutableListOf<Esteira>()
        var id = 0
        val nts = letras(g)
        val linhaDe = mutableMapOf<String, Int>()
        // largura de cada linha, para alinhar os filtros numa coluna só
        val fimDe = mutableMapOf<String, Int>()
        for (nt in nts) {
            var x = X0
            for (p in g.productionsOf(nt)) { x += modelo(p, g).w + 2 }
            fimDe[nt] = x
        }
        val xFiltro = (fimDe.values.maxOrNull() ?: X0) + 1
        val xColetor = xFiltro + 1
        blocos += Bloco(id++, TipoBloco.FONTE, 0, 0, 2, 2, nt = g.start)
        nts.forEachIndexed { r, nt ->
            val yA = 3 + r * PASSO_LINHA
            linhaDe[nt] = yA
            blocos += Bloco(id++, TipoBloco.SEPARADOR, 3, yA, 1, 1, nt = nt)
            esteiras += reta(Cel(2, yA), Cel(2, yA), Cel(1, 0), Papel.ENTRADA)
            var x = X0
            var ultimo = X0
            for (p in g.productionsOf(nt)) {
                val m = modelo(p, g)
                blocos += Bloco(id++, TipoBloco.MAQUINA, x, yA + 1, m.w, m.h, nt = nt, prod = p, modelo = m)
                // saída: lateral direita, na última fileira da máquina, descendo até a esteira de baixo
                if (p.rhs.isNotEmpty()) esteiras += reta(Cel(x + m.w, yA + m.h), Cel(x + m.w, yA + 2), Cel(0, 1), Papel.SAIDA)
                ultimo = x
                x += m.w + 2
            }
            esteiras += reta(Cel(X0, yA), Cel(ultimo, yA), Cel(1, 0), Papel.DISTRIBUI)
            esteiras += reta(Cel(X0, yA + 3), Cel(xFiltro - 1, yA + 3), Cel(1, 0), Papel.LINHA)
            blocos += Bloco(id++, TipoBloco.FILTRO, xFiltro, yA + 3, 1, 1, nt = nt)
            esteiras += reta(Cel(xFiltro, yA + 4), Cel(2, yA + 4), Cel(-1, 0), Papel.RETORNO)
        }
        val yUlt = 3 + (nts.size - 1) * PASSO_LINHA
        val yMont = yUlt + PASSO_LINHA
        esteiras += reta(Cel(1, 2), Cel(1, yUlt + 4), Cel(0, 1), Papel.ELEVADOR)
        esteiras += reta(Cel(xColetor, 3 + 3), Cel(xColetor, yMont - 1), Cel(0, 1), Papel.COLETOR)
        blocos += Bloco(id++, TipoBloco.MONTADORA, xColetor - 1, yMont, 2, 2)
        blocos += Bloco(id++, TipoBloco.CESTO, xColetor - 4, yMont, 2, 2)
        return Planta(g, xColetor + 1, yMont + 3, blocos, esteiras, linhaDe, xFiltro, xColetor, yMont)
    }

    private fun reta(a: Cel, b: Cel, d: Cel, papel: Papel): Esteira {
        val dx = (b.x - a.x).sign
        val dy = (b.y - a.y).sign
        val n = maxOf(kotlin.math.abs(b.x - a.x), kotlin.math.abs(b.y - a.y))
        val cs = (0..n).map { Cel(a.x + dx * it, a.y + dy * it) }
        return Esteira(cs, cs.map { d }, papel)
    }

    // ------------------------------------------------------------------ corrida

    /** Um trecho do caminho de um item: anda de [de] a [ate] entre [t0] e [t1] segundos. */
    data class Trecho(val simbolo: String, val caixa: Boolean, val pontos: List<Pair<Float, Float>>, val t0: Float, val t1: Float)

    /** A máquina [bloco] trabalha entre [t0] e [t1]. */
    data class Trabalho(val bloco: Int, val t0: Float, val t1: Float)

    /** A peça [simbolo] chega na casa [casa] da frase no instante [t]. */
    data class Chegada(val casa: Int, val simbolo: String, val t: Float)

    data class Corrida(
        /** A frase pedida. */
        val pedida: List<String>,
        /** A frase que a fábrica monta: a pedida, ou a mais perto que ela sabe fazer. */
        val feita: List<String>?,
        val aceita: Boolean,
        /** Duas árvores para a mesma frase. */
        val ambigua: Boolean,
        val arvore: ParseNode?,
        val trechos: List<Trecho>,
        val trabalhos: List<Trabalho>,
        val chegadas: List<Chegada>,
        val duracao: Float,
    )

    const val VEL = 7f          // células por segundo
    const val TRABALHO = 0.6f   // segundos de máquina por caixa

    fun corrida(pl: Planta, frase: List<String>): Corrida {
        val g = pl.g
        val dv = DerivationHelper.leftmostDerivations(g, frase, limit = 2)
        if (dv.isNotEmpty()) {
            val arv = DerivationHelper.treeOf(g, dv[0])
            return anima(pl, frase, frase, true, dv.size > 1, arv)
        }
        val perto = maisPerto(g, frase)
        val arv = perto?.let { DerivationHelper.leftmostDerivations(g, it, limit = 1).firstOrNull() }?.let { DerivationHelper.treeOf(g, it) }
        return anima(pl, frase, perto, false, false, arv)
    }

    private fun anima(pl: Planta, pedida: List<String>, feita: List<String>?, aceita: Boolean, ambigua: Boolean, arv: ParseNode?): Corrida {
        val trechos = mutableListOf<Trecho>()
        val trabalhos = mutableListOf<Trabalho>()
        val chegadas = mutableListOf<Chegada>()
        var t = 0f
        if (arv == null) return Corrida(pedida, feita, aceita, ambigua, null, trechos, trabalhos, chegadas, 0f)
        fun anda(sim: String, caixa: Boolean, pts: List<Pair<Float, Float>>) {
            var len = 0f
            for (i in 1 until pts.size) len += kotlin.math.abs(pts[i].first - pts[i - 1].first) + kotlin.math.abs(pts[i].second - pts[i - 1].second)
            val d = len / VEL
            trechos += Trecho(sim, caixa, pts, t, t + d)
            t += d
        }
        var casa = 0
        // a caixa da letra inicial sai da fonte e desce o elevador até o separador
        fun ateSeparador(nt: String, de: Pair<Float, Float>, viaRetorno: Int?): List<Pair<Float, Float>> {
            val y = pl.linhaDe.getValue(nt) + 0.5f
            val pts = mutableListOf(de)
            if (viaRetorno != null) { pts += (2.5f to viaRetorno + 0.5f) }
            pts += (1.5f to (viaRetorno?.plus(0.5f) ?: 2.5f))
            pts += (1.5f to y)
            pts += (3.5f to y)
            return pts
        }
        fun processa(no: ParseNode, de: List<Pair<Float, Float>>) {
            val nt = no.symbol
            anda(nt, true, de)
            val filhos = no.children.filter { it.symbol != com.compilandia.engine.EPS }
            val p = pl.g.productionsOf(nt).first { pr -> pr.rhs == filhos.map { it.symbol } }
            val m = pl.maquinaDe(p)
            val yA = pl.linhaDe.getValue(nt)
            anda(nt, true, listOf(3.5f to yA + 0.5f, m.x + 0.5f to yA + 0.5f, m.x + 0.5f to m.cy))
            trabalhos += Trabalho(m.id, t, t + TRABALHO)
            t += TRABALHO
            val pendentes = mutableListOf<Pair<ParseNode, List<Pair<Float, Float>>>>()
            for (f in filhos) {
                val saida = listOf(m.cx to m.cy, m.x + m.w + 0.5f to m.y + m.h - 0.5f, m.x + m.w + 0.5f to yA + 3.5f, pl.xFiltro + 0.5f to yA + 3.5f)
                if (!pl.g.isNonTerminal(f.symbol)) {
                    val mont = pl.montadora
                    anda(f.symbol, false, saida + listOf(pl.xColetor + 0.5f to yA + 3.5f, pl.xColetor + 0.5f to mont.cy))
                    chegadas += Chegada(casa++, f.symbol, t)
                } else {
                    anda(f.symbol, true, saida + listOf(pl.xFiltro + 0.5f to yA + 4.5f))
                    pendentes += f to ateSeparador(f.symbol, pl.xFiltro + 0.5f to yA + 4.5f, yA + 4)
                }
            }
            // as caixas voltam e entram na ordem da frase: a da esquerda primeiro
            for ((f, cam) in pendentes) processa(f, cam)
        }
        processa(arv, ateSeparador(arv.symbol, pl.fonte.cx to pl.fonte.cy, null))
        return Corrida(pedida, feita, aceita, ambigua, arv, trechos, trabalhos, chegadas.sortedBy { it.t }, t)
    }

    /**
     * Quando a fábrica não faz a frase, mostra a mais perto que ela faz (contraexemplo).
     * Percorre as derivações mais à esquerda em largura, até o tamanho da frase mais dois.
     */
    fun maisPerto(g: Grammar, frase: List<String>, limite: Int = 20000): List<String>? {
        val maxT = frase.size + 2
        var melhor: List<String>? = null
        var dist = Int.MAX_VALUE
        var fila = ArrayDeque<List<String>>()
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

    fun edicao(a: List<String>, b: List<String>): Int {
        val d = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 0..a.size) d[i][0] = i
        for (j in 0..b.size) d[0][j] = j
        for (i in 1..a.size) for (j in 1..b.size)
            d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
        return d[a.size][b.size]
    }

    /** Exemplos do menu. As gramáticas não se traduzem. */
    val exemplos: List<Pair<String, String>> = listOf(
        "S -> a S b | ε" to "a a b b",
        "S -> ( S ) S | ε" to "( ) ( )",
        "E -> E + T | T\nT -> id" to "id + id",
        "E -> E + E | id" to "id + id + id",
        "S -> if E then S else S | x\nE -> id" to "if id then x else x",
    )
}
