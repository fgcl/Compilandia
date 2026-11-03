package com.compilandia.engine

import kotlin.random.Random

/**
 * Expressões regulares → AFN (Thompson) → AFD (subconjuntos) → AFD mínimo.
 * Sintaxe: letras/dígitos literais, | (união), concatenação, *, +, ?, parênteses.
 */
sealed class Regex0 {
    data class Ch(val c: Char) : Regex0()
    data object Empty : Regex0()
    data class Cat(val a: Regex0, val b: Regex0) : Regex0()
    data class Alt(val a: Regex0, val b: Regex0) : Regex0()
    data class Star(val a: Regex0) : Regex0()

    companion object {
        fun parse(src: String): Regex0 {
            var pos = 0
            val s = src.filter { !it.isWhitespace() }
            fun peek() = s.getOrNull(pos)
            lateinit var alt: () -> Regex0
            fun atom(): Regex0 {
                val c = peek() ?: error("Expressão incompleta")
                return when (c) {
                    '(' -> { pos++; val r = if (peek() == ')') Empty else alt(); require(peek() == ')') { "Falta ')'" }; pos++; r }
                    'ε' -> { pos++; Empty }
                    else -> { require(c !in ")|*+?") { "Símbolo inesperado '$c'" }; pos++; Ch(c) }
                }
            }
            fun post(): Regex0 {
                var r = atom()
                while (true) {
                    r = when (peek()) {
                        '*' -> { pos++; Star(r) }
                        '+' -> { pos++; Cat(r, Star(r)) }
                        '?' -> { pos++; Alt(r, Empty) }
                        else -> return r
                    }
                }
            }
            fun cat(): Regex0 {
                var r = post()
                while (peek() != null && peek() != '|' && peek() != ')') r = Cat(r, post())
                return r
            }
            alt = {
                var r = cat()
                while (peek() == '|') { pos++; r = Alt(r, cat()) }
                r
            }
            val r = alt()
            require(pos == s.length) { "Sobrou '${s.substring(pos)}'" }
            return r
        }
    }
}

/** AFN com transições ε (Thompson). */
class NFA(val stateCount: Int, val start: Int, val accept: Int, val edges: List<Triple<Int, Char?, Int>>) {
    fun epsClosure(states: Set<Int>): Set<Int> {
        val out = states.toMutableSet()
        val work = ArrayDeque(states)
        while (work.isNotEmpty()) {
            val s = work.removeFirst()
            for ((a, c, b) in edges) if (a == s && c == null && out.add(b)) work += b
        }
        return out
    }

    fun move(states: Set<Int>, c: Char): Set<Int> = edges.filter { it.first in states && it.second == c }.map { it.third }.toSet()

    companion object {
        fun thompson(r: Regex0): NFA {
            val edges = mutableListOf<Triple<Int, Char?, Int>>()
            var n = 0
            fun newState() = n++
            fun build(r: Regex0): Pair<Int, Int> = when (r) {
                is Regex0.Ch -> { val a = newState(); val b = newState(); edges += Triple(a, r.c, b); a to b }
                Regex0.Empty -> { val a = newState(); val b = newState(); edges += Triple(a, null, b); a to b }
                is Regex0.Cat -> { val (a1, b1) = build(r.a); val (a2, b2) = build(r.b); edges += Triple(b1, null, a2); a1 to b2 }
                is Regex0.Alt -> {
                    val s = newState(); val (a1, b1) = build(r.a); val (a2, b2) = build(r.b); val f = newState()
                    edges += Triple(s, null, a1); edges += Triple(s, null, a2); edges += Triple(b1, null, f); edges += Triple(b2, null, f)
                    s to f
                }
                is Regex0.Star -> {
                    val s = newState(); val (a1, b1) = build(r.a); val f = newState()
                    edges += Triple(s, null, a1); edges += Triple(s, null, f); edges += Triple(b1, null, a1); edges += Triple(b1, null, f)
                    s to f
                }
            }
            val (s, f) = build(r)
            return NFA(n, s, f, edges)
        }
    }
}

/** AFD completo ou parcial. Estado -1 = "morto" (sem transição). */
data class DFA(
    val stateCount: Int,
    val start: Int,
    val accepting: Set<Int>,
    val alphabet: List<Char>,
    val delta: Map<Pair<Int, Char>, Int>,
) {
    fun step(s: Int, c: Char): Int = if (s < 0) -1 else delta[s to c] ?: -1

    fun run(input: String): List<Int> {
        val path = mutableListOf(start)
        var s = start
        for (c in input) { s = step(s, c); path += s }
        return path
    }

    fun accepts(input: String): Boolean = run(input).last() in accepting

    companion object {
        fun fromNFA(nfa: NFA, alphabet: List<Char>): DFA {
            val startSet = nfa.epsClosure(setOf(nfa.start))
            val sets = mutableListOf(startSet)
            val idx = mutableMapOf(startSet to 0)
            val delta = mutableMapOf<Pair<Int, Char>, Int>()
            var i = 0
            while (i < sets.size) {
                for (c in alphabet) {
                    val t = nfa.epsClosure(nfa.move(sets[i], c))
                    if (t.isEmpty()) continue
                    val j = idx.getOrPut(t) { sets += t; sets.size - 1 }
                    delta[i to c] = j
                }
                i++
            }
            val acc = sets.indices.filter { nfa.accept in sets[it] }.toSet()
            return DFA(sets.size, 0, acc, alphabet, delta).minimize()
        }

        fun fromRegex(regex: String): DFA {
            val r = Regex0.parse(regex)
            val alphabet = regex.filter { it.isLetterOrDigit() }.toSet().sorted()
            return fromNFA(NFA.thompson(r), alphabet)
        }
    }

    /** Minimização por refinamento de partições (Moore). Estados mortos são descartados. */
    fun minimize(): DFA {
        // completa com estado morto D = stateCount
        val dead = stateCount
        fun st(s: Int, c: Char) = delta[s to c] ?: dead
        var part = IntArray(stateCount + 1) { if (it in accepting) 1 else 0 }
        while (true) {
            val sig = HashMap<List<Int>, Int>()
            val next = IntArray(stateCount + 1)
            for (s in 0..stateCount) {
                val key = listOf(part[s]) + alphabet.map { c -> if (s == dead) part[dead] else part[st(s, c)] }
                next[s] = sig.getOrPut(key) { sig.size }
            }
            if (sig.size == part.distinct().size) { part = next; break }
            part = next
        }
        val deadBlock = part[dead]
        // renumera a partir do estado inicial em BFS (desenho estável)
        val order = mutableListOf(part[start])
        var k = 0
        while (k < order.size) {
            val b = order[k++]
            val rep = (0 until stateCount).first { part[it] == b }
            for (c in alphabet) { val t = part[st(rep, c)]; if (t != deadBlock && t !in order) order += t }
        }
        val newId = order.withIndex().associate { it.value to it.index }
        val nd = mutableMapOf<Pair<Int, Char>, Int>()
        for (s in 0 until stateCount) {
            val from = newId[part[s]] ?: continue
            for (c in alphabet) { val to = newId[part[st(s, c)]] ?: continue; nd[from to c] = to }
        }
        val acc = (0 until stateCount).filter { it in accepting }.mapNotNull { newId[part[it]] }.toSet()
        return DFA(order.size, 0, acc, alphabet, nd)
    }

    /** Cadeias de exemplo (aceitas e rejeitadas) para os jogos. */
    fun samples(rnd: Random, count: Int, maxLen: Int = 6): List<Pair<String, Boolean>> {
        val out = LinkedHashMap<String, Boolean>()
        var guard = 0
        while (out.size < count && guard++ < 2000) {
            val wantAccept = rnd.nextBoolean()
            val s = if (wantAccept) randomAccepted(rnd, maxLen) else randomString(rnd, maxLen)
            if (s != null && s !in out) out[s] = accepts(s)
        }
        return out.entries.map { it.key to it.value }
    }

    private fun randomString(rnd: Random, maxLen: Int): String =
        buildString { repeat(rnd.nextInt(maxLen + 1)) { append(alphabet[rnd.nextInt(alphabet.size)]) } }

    private fun randomAccepted(rnd: Random, maxLen: Int): String? {
        repeat(30) {
            var s = start
            val sb = StringBuilder()
            val len = rnd.nextInt(maxLen + 1)
            for (i in 0 until len) {
                val opts = alphabet.filter { delta[s to it] != null }
                if (opts.isEmpty()) break
                val c = opts[rnd.nextInt(opts.size)]
                sb.append(c); s = delta.getValue(s to c)
            }
            if (s in accepting) return sb.toString()
        }
        return null
    }
}

object RegexBank {
    data class Entry(val regex: String, val description: String)
    val entries = listOf(
        Entry("ab*", "um 'a' seguido de quantos 'b' quiser"),
        Entry("(a|b)*abb", "termina em abb (exemplo do Livro do Dragão)"),
        Entry("a(a|b)*b", "começa com a e termina com b"),
        Entry("(ab)*", "repetições de 'ab'"),
        Entry("0|1(0|1)*", "binários sem zeros à esquerda"),
        Entry("(0|1)*1", "binários ímpares"),
        Entry("a*b*", "a's antes, b's depois"),
        Entry("(a|b)*a(a|b)", "penúltimo símbolo é a"),
        Entry("(aa)*", "quantidade par de a's"),
        Entry("x(y|z)+", "x seguido de pelo menos um y ou z"),
    )
}
