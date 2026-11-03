package com.compilandia.engine

import kotlin.random.Random

object GrammarTransform {
    private fun fresh(g: Set<String>, base: String): String {
        var n = "$base'"
        while (n in g) n += "'"
        return n
    }

    fun hasImmediateLeftRecursion(g: Grammar, nt: String) =
        g.productionsOf(nt).any { it.rhs.firstOrNull() == nt }

    fun leftRecursiveNTs(g: Grammar): List<String> = g.nonTerminals.filter { hasImmediateLeftRecursion(g, it) }

    /**
     * Elimina recursão imediata à esquerda:
     *   A → A α1 | ... | A αm | β1 | ... | βn
     * vira
     *   A  → β1 A' | ... | βn A'
     *   A' → α1 A' | ... | αm A' | ε
     */
    fun eliminateImmediateLeftRecursion(g: Grammar): Grammar {
        val names = g.nonTerminals.toMutableSet()
        val out = mutableListOf<Production>()
        for (a in g.nonTerminals) {
            val ps = g.productionsOf(a)
            val alphas = ps.filter { it.rhs.firstOrNull() == a }.map { it.rhs.drop(1) }
            val betas = ps.filter { it.rhs.firstOrNull() != a }.map { it.rhs }
            if (alphas.isEmpty()) { out += ps; continue }
            val a2 = fresh(names, a); names += a2
            for (b in betas) out += Production(0, a, b + a2)
            for (al in alphas) out += Production(0, a2, al + a2)
            out += Production(0, a2, emptyList())
        }
        // o laço já deixa as produções de A' logo após as de A
        return Grammar(g.start, out)
    }

    /** Fatoração à esquerda (uma rodada por NT, repetida até estabilizar). */
    fun leftFactor(g: Grammar): Grammar {
        var cur = g
        repeat(10) {
            val names = cur.nonTerminals.toMutableSet()
            val out = mutableListOf<Production>()
            var changed = false
            for (a in cur.nonTerminals) {
                val ps = cur.productionsOf(a)
                val groups = ps.groupBy { it.rhs.firstOrNull() }
                val target = groups.entries.firstOrNull { it.key != null && it.value.size > 1 }
                if (target == null) { out += ps; continue }
                changed = true
                val group = target.value
                var prefixLen = 1
                while (group.all { it.rhs.size > prefixLen } &&
                    group.map { it.rhs[prefixLen] }.distinct().size == 1) prefixLen++
                val prefix = group[0].rhs.take(prefixLen)
                val a2 = fresh(names, a); names += a2
                for (p in ps) if (p !in group) out += p
                out += Production(0, a, prefix + a2)
                for (p in group) out += Production(0, a2, p.rhs.drop(prefixLen))
            }
            if (!changed) return cur
            cur = Grammar(cur.start, out)
        }
        return cur
    }

    fun needsLeftFactoring(g: Grammar): Boolean =
        g.nonTerminals.any { nt -> g.productionsOf(nt).mapNotNull { it.rhs.firstOrNull() }.let { it.size != it.distinct().size } }
}

/** Gramáticas clássicas usadas nos jogos e nas aulas. */
object GrammarBank {
    data class Named(val id: String, val title: String, val text: String, val note: String) {
        val grammar: Grammar by lazy { Grammar.parse(text) }
    }

    val exprLeftRec = Named(
        "expr", "Expressões (recursiva à esquerda)",
        """
        E -> E + T | T
        T -> T * F | F
        F -> ( E ) | id
        """.trimIndent(),
        "A gramática clássica do Livro do Dragão. Ótima para LR, proibida para LL(1)."
    )
    val exprLL1 = Named(
        "exprll", "Expressões (versão LL(1))",
        """
        E -> T E'
        E' -> + T E' | ε
        T -> F T'
        T' -> * F T' | ε
        F -> ( E ) | id
        """.trimIndent(),
        "Mesma linguagem, sem recursão à esquerda: agora o LL(1) consegue."
    )
    val exprAmbiguous = Named(
        "ambig", "Expressões ambíguas",
        """
        E -> E + E | E * E | ( E ) | id
        """.trimIndent(),
        "Curta e bonita, mas ambígua: 'id + id * id' tem duas árvores."
    )
    val parens = Named(
        "parens", "Parênteses balanceados",
        "S -> ( S ) S | ε",
        "Gera (), (()), ()() ... Uma linguagem que NENHUMA expressão regular reconhece."
    )
    val anbn = Named(
        "anbn", "aⁿbⁿ",
        "S -> a S b | ε",
        "O exemplo clássico de linguagem livre de contexto que não é regular."
    )
    val ifElse = Named(
        "ifelse", "if-then-else (dangling else)",
        """
        S -> if c then S | if c then S else S | x
        """.trimIndent(),
        "O famoso 'else pendurado': ambígua; o Yacc resolve com shift."
    )
    val stmts = Named(
        "stmts", "Comandos simples",
        """
        P -> begin L end
        L -> S ; L | S
        S -> id = E | print E
        E -> id | num
        """.trimIndent(),
        "Lista de comandos; precisa de fatoração para ser LL(1)."
    )
    val decl = Named(
        "decl", "Declarações",
        """
        D -> T L ;
        T -> int | float
        L -> id R
        R -> , id R | ε
        """.trimIndent(),
        "Declarações estilo C: int a, b, c;  — já é LL(1)."
    )
    val slrNotLL = Named(
        "slr", "Lista com recursão à esquerda",
        """
        S -> S a | b
        """.trimIndent(),
        "b a a a ... — SLR(1) sem esforço, LL(1) impossível sem transformar."
    )
    val lalrNotSlr = Named(
        "lalr", "Atribuição com ponteiros (LALR mas não SLR)",
        """
        S -> L = R | R
        L -> * R | id
        R -> L
        """.trimIndent(),
        "Exemplo famoso: o SLR(1) dá conflito shift/reduce em '=', o LALR(1) não."
    )

    val all = listOf(exprLeftRec, exprLL1, exprAmbiguous, parens, anbn, ifElse, stmts, decl, slrNotLL, lalrNotSlr)
}

/** Gera gramáticas pequenas e aleatórias (todas úteis: alcançáveis e produtivas). */
object GrammarGenerator {
    private val ntNames = listOf("S", "A", "B", "C")
    private val tNames = listOf("a", "b", "c", "d")

    fun random(rnd: Random, nts: Int = 3, allowEpsilon: Boolean = true, requireLL1: Boolean? = null): Grammar {
        repeat(400) {
            val g = attempt(rnd, nts, allowEpsilon) ?: return@repeat
            if (requireLL1 == null || LL1Table(g).isLL1 == requireLL1) return g
        }
        return GrammarBank.exprLL1.grammar
    }

    private fun attempt(rnd: Random, ntCount: Int, allowEpsilon: Boolean): Grammar? {
        val nts = ntNames.take(ntCount)
        val prods = mutableListOf<Production>()
        for ((i, nt) in nts.withIndex()) {
            val alts = 1 + rnd.nextInt(2) + (if (i == 0) 1 else 0)
            val seen = mutableSetOf<List<String>>()
            repeat(alts) {
                val rhs: List<String> = if (allowEpsilon && i > 0 && rnd.nextInt(4) == 0) emptyList() else {
                    val len = 1 + rnd.nextInt(3)
                    List(len) {
                        if (rnd.nextInt(10) < 4) nts[rnd.nextInt(nts.size)] else tNames[rnd.nextInt(tNames.size)]
                    }
                }
                if (seen.add(rhs)) prods += Production(0, nt, rhs)
            }
        }
        val g = Grammar(nts[0], prods)
        if (g.nonTerminals.size != nts.size) return null
        // produtivo
        val productive = mutableSetOf<String>()
        var ch = true
        while (ch) { ch = false; for (p in g.productions) if (p.lhs !in productive && p.rhs.all { !g.isNonTerminal(it) || it in productive }) { productive += p.lhs; ch = true } }
        if (productive.size != nts.size) return null
        // alcançável
        val reach = mutableSetOf(g.start); ch = true
        while (ch) { ch = false; for (p in g.productions) if (p.lhs in reach) for (s in p.rhs) if (g.isNonTerminal(s) && reach.add(s)) ch = true }
        if (reach.size != nts.size) return null
        if (g.terminals.size < 2) return null
        // evita ciclos A ⇒+ A sem consumir nada e recursão à esquerda indireta exagerada
        if (g.productions.any { it.rhs.size == 1 && g.isNonTerminal(it.rhs[0]) }) return null   // sem produções unitárias (A → B): confundem mais do que ensinam
        return g
    }
}
