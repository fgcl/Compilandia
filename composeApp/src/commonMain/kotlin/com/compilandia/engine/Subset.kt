package com.compilandia.engine

/**
 * AFN pequeno (com ou sem ε) para o jogo/aula da CONSTRUÇÃO DE SUBCONJUNTOS.
 * Diferente do [NFA] de Thompson, aceita vários estados finais e rótulos legíveis.
 */
data class SmallNFA(
    val id: String,
    val title: String,
    val stateCount: Int,
    val start: Int,
    val accepting: Set<Int>,
    val edges: List<Triple<Int, Char?, Int>>,   // null = ε
) {
    val alphabet: List<Char> = edges.mapNotNull { it.second }.distinct().sorted()

    fun epsClosure(states: Set<Int>): Set<Int> {
        val out = states.toMutableSet()
        val work = ArrayDeque(states)
        while (work.isNotEmpty()) {
            val s = work.removeFirst()
            for ((a, c, b) in edges) if (a == s && c == null && out.add(b)) work += b
        }
        return out.sorted().toCollection(LinkedHashSet())
    }

    fun move(states: Set<Int>, c: Char): Set<Int> =
        edges.filter { it.first in states && it.second == c }.map { it.third }.sorted().toCollection(LinkedHashSet())

    fun accepts(input: String): Boolean {
        var cur = epsClosure(setOf(start))
        for (ch in input) cur = epsClosure(move(cur, ch))
        return cur.any { it in accepting }
    }

    /** Um passo da tabela de transição do AFD: D[from, símbolo] = to. */
    data class Step(val from: Set<Int>, val symbol: Char, val moved: Set<Int>, val to: Set<Int>)

    data class Result(val states: List<Set<Int>>, val steps: List<Step>, val accepting: Set<Int>) {
        fun toDFA(alphabet: List<Char>): DFA {
            val delta = steps.filter { it.to.isNotEmpty() }
                .associate { (states.indexOf(it.from) to it.symbol) to states.indexOf(it.to) }
            return DFA(states.size, 0, accepting, alphabet, delta)
        }
    }

    /** Construção de subconjuntos (Livro do Dragão, alg. 3.20), registrando cada passo. */
    fun subsetConstruction(): Result {
        val first = epsClosure(setOf(start))
        val states = mutableListOf(first)
        val steps = mutableListOf<Step>()
        var i = 0
        while (i < states.size) {
            val t = states[i]
            for (c in alphabet) {
                val m = move(t, c)
                val u = epsClosure(m)
                steps += Step(t, c, m, u)
                if (u.isNotEmpty() && u !in states) states += u
            }
            i++
        }
        val acc = states.indices.filter { idx -> states[idx].any { it in accepting } }.toSet()
        return Result(states, steps, acc)
    }

    companion object {
        private fun e(a: Int, c: Char?, b: Int) = Triple(a, c, b)

        val bank: List<SmallNFA> = listOf(
            SmallNFA("abb", "(a|b)*abb — termina em abb", 4, 0, setOf(3),
                listOf(e(0, 'a', 0), e(0, 'b', 0), e(0, 'a', 1), e(1, 'b', 2), e(2, 'b', 3))),
            SmallNFA("ab", "(a|b)*ab — termina em ab", 3, 0, setOf(2),
                listOf(e(0, 'a', 0), e(0, 'b', 0), e(0, 'a', 1), e(1, 'b', 2))),
            SmallNFA("aa", "contém aa", 3, 0, setOf(2),
                listOf(e(0, 'a', 0), e(0, 'b', 0), e(0, 'a', 1), e(1, 'a', 2), e(2, 'a', 2), e(2, 'b', 2))),
            SmallNFA("abc", "ab* | c  (com ε)", 5, 0, setOf(2, 4),
                listOf(e(0, null, 1), e(0, null, 3), e(1, 'a', 2), e(2, 'b', 2), e(3, 'c', 4))),
            SmallNFA("eps", "a*b*  (com ε)", 2, 0, setOf(1),
                listOf(e(0, 'a', 0), e(0, null, 1), e(1, 'b', 1))),
            SmallNFA("pen", "penúltimo é a", 3, 0, setOf(2),
                listOf(e(0, 'a', 0), e(0, 'b', 0), e(0, 'a', 1), e(1, 'a', 2), e(1, 'b', 2))),
            SmallNFA("ou", "(ab | a)c  (com ε)", 6, 0, setOf(5),
                listOf(e(0, 'a', 1), e(1, 'b', 2), e(0, 'a', 3), e(2, null, 4), e(3, null, 4), e(4, 'c', 5))),
        )
    }
}

fun setText(s: Set<Int>): String = if (s.isEmpty()) "∅" else s.joinToString(",", "{", "}")
