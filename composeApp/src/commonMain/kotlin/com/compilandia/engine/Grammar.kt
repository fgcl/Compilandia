package com.compilandia.engine

const val EPS = "ε"
const val EOF = "$"

/** Uma produção A → X1 X2 ... Xn. Lado direito vazio significa A → ε. */
data class Production(val id: Int, val lhs: String, val rhs: List<String>) {
    val isEpsilon: Boolean get() = rhs.isEmpty()
    fun rhsText(): String = if (rhs.isEmpty()) EPS else rhs.joinToString(" ")
    override fun toString(): String = "$lhs → ${rhsText()}"
}

/**
 * Gramática livre de contexto.
 *
 * Formato de texto aceito por [Grammar.parse]:
 * ```
 * E  -> E + T | T
 * T  -> T * F | F
 * F  -> ( E ) | id
 * ```
 * - Símbolos separados por espaço.
 * - `ε`, `eps` ou `epsilon` representam a cadeia vazia.
 * - Não-terminais são os que aparecem do lado esquerdo; o primeiro é o inicial.
 */
class Grammar(val start: String, productions: List<Production>) {
    val productions: List<Production> = productions.mapIndexed { i, p -> p.copy(id = i) }

    val nonTerminals: List<String> = this.productions.map { it.lhs }.distinct()
    private val ntSet = nonTerminals.toSet()

    val terminals: List<String> = this.productions
        .flatMap { it.rhs }
        .filter { it !in ntSet }
        .distinct()

    fun isNonTerminal(s: String) = s in ntSet
    fun isTerminal(s: String) = s !in ntSet && s != EPS

    fun productionsOf(nt: String): List<Production> = productions.filter { it.lhs == nt }

    fun toText(): String = nonTerminals.joinToString("\n") { nt ->
        "$nt → " + productionsOf(nt).joinToString(" | ") { it.rhsText() }
    }

    override fun toString(): String = toText()

    /** Gramática aumentada S' → S usada nos analisadores LR. */
    fun augmented(): Grammar {
        var s = "$start'"
        while (s in ntSet) s += "'"
        return Grammar(s, listOf(Production(0, s, listOf(start))) + productions)
    }

    companion object {
        private val EPS_WORDS = setOf(EPS, "eps", "epsilon", "ϵ", "λ")

        fun parse(text: String): Grammar {
            val prods = mutableListOf<Production>()
            var start: String? = null
            var lastLhs: String? = null
            for (raw in text.lines()) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue
                val arrowIdx = findArrow(line)
                val lhs: String
                val rhsAll: String
                if (arrowIdx == null) {
                    // linha de continuação: "| a B"
                    require(line.startsWith("|") && lastLhs != null) { "Linha sem '->': $line" }
                    lhs = lastLhs
                    rhsAll = line.substring(1)
                } else {
                    lhs = line.substring(0, arrowIdx.first).trim()
                    rhsAll = line.substring(arrowIdx.second)
                }
                require(lhs.isNotEmpty() && !lhs.contains(' ')) { "Lado esquerdo inválido: '$lhs'" }
                if (start == null) start = lhs
                lastLhs = lhs
                for (alt in splitAlternatives(rhsAll)) {
                    val symbols = alt.trim().split(Regex("\\s+")).filter { it.isNotEmpty() && it !in EPS_WORDS }
                    prods += Production(prods.size, lhs, symbols)
                }
            }
            requireNotNull(start) { "Gramática vazia" }
            return Grammar(start, prods)
        }

        private fun findArrow(line: String): Pair<Int, Int>? {
            for (arrow in listOf("->", "→", "::=", ":")) {
                val i = line.indexOf(arrow)
                if (i > 0) return i to i + arrow.length
            }
            return null
        }

        /** Divide por `|` que não esteja entre aspas (ex.: '|' como terminal). */
        private fun splitAlternatives(s: String): List<String> {
            val out = mutableListOf<String>()
            val cur = StringBuilder()
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '\'' && i + 2 < s.length && s[i + 2] == '\'') {
                    cur.append(s, i, i + 3); i += 3; continue
                }
                if (c == '|') { out += cur.toString(); cur.clear() } else cur.append(c)
                i++
            }
            out += cur.toString()
            return out
        }
    }
}

/** Divide uma cadeia de entrada em símbolos terminais (separados por espaço). */
fun tokensOf(input: String): List<String> = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
