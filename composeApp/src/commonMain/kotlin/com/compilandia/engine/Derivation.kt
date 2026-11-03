package com.compilandia.engine

import kotlin.random.Random

/**
 * Reconhecedor de Earley: decide se uma sequência de símbolos (pode misturar
 * terminais e não-terminais!) deriva a cadeia alvo. Funciona com QUALQUER GLC,
 * inclusive ambígua e com recursão à esquerda. É o "juiz" dos jogos de derivação.
 */
class Earley(val grammar: Grammar) {
    private data class EItem(val lhs: String, val rhs: List<String>, val dot: Int, val origin: Int)

    fun derives(form: List<String>, target: List<String>): Boolean {
        val startSym = "§S"
        val prods = HashMap<String, List<List<String>>>()
        for (nt in grammar.nonTerminals) prods[nt] = grammar.productionsOf(nt).map { it.rhs }
        prods[startSym] = listOf(form)
        val nullable = FirstFollow(grammar).nullable
        val n = target.size
        val chart = List(n + 1) { LinkedHashSet<EItem>() }
        chart[0] += EItem(startSym, form, 0, 0)
        for (i in 0..n) {
            val work = ArrayDeque(chart[i])
            while (work.isNotEmpty()) {
                val it = work.removeFirst()
                val next = it.rhs.getOrNull(it.dot)
                if (next == null) {
                    // completar
                    for (p in chart[it.origin].toList()) {
                        if (p.rhs.getOrNull(p.dot) == it.lhs) {
                            val ni = p.copy(dot = p.dot + 1)
                            if (chart[i].add(ni)) work += ni
                        }
                    }
                } else if (prods.containsKey(next) && next != startSym) {
                    // prever
                    for (rhs in prods.getValue(next)) {
                        val ni = EItem(next, rhs, 0, i)
                        if (chart[i].add(ni)) work += ni
                    }
                    if (next in nullable) {
                        val ni = it.copy(dot = it.dot + 1)
                        if (chart[i].add(ni)) work += ni
                    }
                } else if (i < n && next == target[i]) {
                    chart[i + 1] += it.copy(dot = it.dot + 1)
                }
            }
        }
        return chart[n].any { it.lhs == startSym && it.dot == form.size && it.origin == 0 }
    }

    fun accepts(target: List<String>) = derives(listOf(grammar.start), target)
}

/** Estado de uma derivação mais à esquerda feita pelo jogador. */
data class SententialForm(val symbols: List<String>) {
    fun leftmostNT(g: Grammar): Int = symbols.indexOfFirst { g.isNonTerminal(it) }
    fun rightmostNT(g: Grammar): Int = symbols.indexOfLast { g.isNonTerminal(it) }
    fun apply(index: Int, p: Production): SententialForm =
        SententialForm(symbols.subList(0, index) + p.rhs + symbols.subList(index + 1, symbols.size))
    fun text(): String = if (symbols.isEmpty()) EPS else symbols.joinToString(" ")
}

object DerivationHelper {
    /** A forma sentencial ainda pode chegar no alvo? (prefixo de terminais + Earley no resto) */
    fun viable(g: Grammar, earley: Earley, form: List<String>, target: List<String>): Boolean {
        var i = 0
        while (i < form.size && !g.isNonTerminal(form[i])) {
            if (i >= target.size || form[i] != target[i]) return false
            i++
        }
        // sufixo de terminais também precisa bater (poda barata)
        var j = 0
        while (j < form.size - i && !g.isNonTerminal(form[form.size - 1 - j])) {
            val ti = target.size - 1 - j
            if (ti < i || form[form.size - 1 - j] != target[ti]) return false
            j++
        }
        return earley.derives(form.subList(i, form.size), target.subList(i, target.size))
    }

    /** Produções do não-terminal mais à esquerda que mantêm o alvo alcançável. */
    fun goodChoices(g: Grammar, earley: Earley, form: SententialForm, target: List<String>): List<Production> {
        val idx = form.leftmostNT(g)
        if (idx < 0) return emptyList()
        return g.productionsOf(form.symbols[idx]).filter { viable(g, earley, form.apply(idx, it).symbols, target) }
    }

    /**
     * Enumera árvores de derivação (até [limit]) via derivações mais à esquerda.
     * Duas derivações mais à esquerda diferentes = duas árvores = AMBIGUIDADE.
     */
    fun leftmostDerivations(g: Grammar, target: List<String>, limit: Int = 4, maxLen: Int = 40): List<List<Production>> {
        val earley = Earley(g)
        val results = mutableListOf<List<Production>>()
        fun dfs(form: SententialForm, path: MutableList<Production>) {
            if (results.size >= limit || path.size > maxLen) return
            val idx = form.leftmostNT(g)
            if (idx < 0) { if (form.symbols == target) results += path.toList(); return }
            for (p in g.productionsOf(form.symbols[idx])) {
                val nf = form.apply(idx, p)
                if (nf.symbols.count { !g.isNonTerminal(it) } > target.size) continue
                if (!viable(g, earley, nf.symbols, target)) continue
                path += p; dfs(nf, path); path.removeAt(path.lastIndex)
                if (results.size >= limit) return
            }
        }
        if (earley.accepts(target)) dfs(SententialForm(listOf(g.start)), mutableListOf())
        return results
    }

    /** Constrói a árvore a partir de uma derivação mais à esquerda. */
    fun treeOf(g: Grammar, derivation: List<Production>): ParseNode {
        var k = 0
        fun build(sym: String): ParseNode {
            if (!g.isNonTerminal(sym)) return ParseNode(sym)
            val p = derivation[k++]
            val kids = if (p.rhs.isEmpty()) listOf(ParseNode(EPS)) else p.rhs.map { build(it) }
            return ParseNode(sym, kids)
        }
        return build(g.start)
    }
}

/** Gera frases aleatórias da linguagem (para alimentar os jogos com entradas infinitas). */
object SentenceGenerator {
    fun generate(g: Grammar, rnd: Random, maxDepth: Int = 7, maxTokens: Int = 12, tries: Int = 60): List<String>? {
        val minDepth = minDepths(g)
        repeat(tries) {
            val out = mutableListOf<String>()
            var ok = true
            fun expand(sym: String, depth: Int) {
                if (!ok) return
                if (!g.isNonTerminal(sym)) { out += sym; if (out.size > maxTokens) ok = false; return }
                val options = g.productionsOf(sym)
                val choice = if (depth >= maxDepth) {
                    options.minByOrNull { p -> p.rhs.sumOf { minDepth[it] ?: 0 } }!!
                } else options[rnd.nextInt(options.size)]
                for (s in choice.rhs) expand(s, depth + 1)
            }
            expand(g.start, 0)
            if (ok && out.isNotEmpty()) return out
        }
        return null
    }

    private fun minDepths(g: Grammar): Map<String, Int> {
        val d = HashMap<String, Int>()
        var changed = true
        while (changed) {
            changed = false
            for (p in g.productions) {
                val cost = 1 + p.rhs.sumOf { if (g.isNonTerminal(it)) (d[it] ?: 1000) else 0 }
                if (cost < (d[p.lhs] ?: Int.MAX_VALUE)) { d[p.lhs] = cost; changed = true }
            }
        }
        return d
    }

    /** Cadeia "quase certa": muta uma frase válida para gerar casos de erro. */
    fun mutate(g: Grammar, sentence: List<String>, rnd: Random): List<String> {
        val terms = g.terminals
        val s = sentence.toMutableList()
        when (rnd.nextInt(3)) {
            0 -> if (s.size > 1) s.removeAt(rnd.nextInt(s.size))
            1 -> s.add(rnd.nextInt(s.size + 1), terms[rnd.nextInt(terms.size)])
            else -> s[rnd.nextInt(s.size)] = terms[rnd.nextInt(terms.size)]
        }
        return s
    }
}
