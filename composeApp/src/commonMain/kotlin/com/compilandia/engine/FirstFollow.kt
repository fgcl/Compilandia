package com.compilandia.engine

import com.compilandia.textos.tx

/** Um passo do cálculo de ponto fixo — usado para animar/explicar o algoritmo. */
data class SetStep(
    val kind: Kind,
    val nonTerminal: String,
    val added: Set<String>,
    val production: Production,
    val reason: String,
) {
    enum class Kind { FIRST, FOLLOW }
}

/**
 * Calcula NULLABLE, FIRST e FOLLOW por iteração de ponto fixo,
 * registrando cada adição em [steps] (com a justificativa em português).
 */
class FirstFollow(val grammar: Grammar) {
    val nullable: Set<String>
    val first: Map<String, Set<String>>
    val follow: Map<String, Set<String>>
    val steps: List<SetStep>
    /** Número de "voltas" do ponto fixo para FIRST e para FOLLOW. */
    val firstRounds: Int
    val followRounds: Int

    init {
        val g = grammar
        val steps = mutableListOf<SetStep>()

        // NULLABLE
        val nullable = mutableSetOf<String>()
        var changed = true
        while (changed) {
            changed = false
            for (p in g.productions) {
                if (p.lhs !in nullable && p.rhs.all { it in nullable }) {
                    nullable += p.lhs; changed = true
                }
            }
        }
        this.nullable = nullable

        // FIRST
        val first = g.nonTerminals.associateWith { mutableSetOf<String>() }
        var rounds = 0
        changed = true
        while (changed) {
            changed = false; rounds++
            for (p in g.productions) {
                val target = first.getValue(p.lhs)
                val add = mutableSetOf<String>()
                val why = StringBuilder()
                var allNullable = true
                for (x in p.rhs) {
                    if (g.isNonTerminal(x)) {
                        val fx = first.getValue(x) - EPS
                        if ((fx - target).isNotEmpty()) {
                            add += fx - target
                            why.append(tx("firstfollow_first_1_entra_em_first", x, p.lhs))
                        }
                        if (x !in nullable) { allNullable = false; break }
                        why.append(tx("firstfollow_1_pode_sumir_olho_proximo", x))
                    } else {
                        if (x !in target) { add += x; why.append(tx("firstfollow_1_comeca_producao", x)) }
                        allNullable = false; break
                    }
                }
                if (allNullable && EPS !in target) { add += EPS; why.append(tx("firstfollow_tudo_direita_pode_virar")) }
                add -= target
                if (add.isNotEmpty()) {
                    target += add; changed = true
                    steps += SetStep(SetStep.Kind.FIRST, p.lhs, add, p, why.toString().trim())
                }
            }
        }
        firstRounds = rounds
        this.first = first

        // FOLLOW
        val follow = g.nonTerminals.associateWith { mutableSetOf<String>() }
        follow.getValue(g.start) += EOF
        steps += SetStep(
            SetStep.Kind.FOLLOW, g.start, setOf(EOF), g.productions.first { it.lhs == g.start },
            tx("firstfollow_1_simbolo_inicial_depois_dele", g.start, EOF)
        )
        rounds = 0
        changed = true
        while (changed) {
            changed = false; rounds++
            for (p in g.productions) {
                for ((i, b) in p.rhs.withIndex()) {
                    if (!g.isNonTerminal(b)) continue
                    val beta = p.rhs.subList(i + 1, p.rhs.size)
                    val fb = firstOfString(beta)
                    val target = follow.getValue(b)
                    val add1 = (fb - EPS) - target
                    if (add1.isNotEmpty()) {
                        target += add1; changed = true
                        steps += SetStep(
                            SetStep.Kind.FOLLOW, b, add1, p,
                            tx("firstfollow_em_1_depois_2_vem", p, b, beta.joinToString(" "), b)
                        )
                    }
                    if (EPS in fb) {
                        val add2 = follow.getValue(p.lhs) - target
                        if (add2.isNotEmpty()) {
                            target += add2; changed = true
                            val tail = if (beta.isEmpty()) tx("firstfollow_1_esta_no_fim", b) else tx("firstfollow_que_vem_depois_1_pode", b)
                            steps += SetStep(
                                SetStep.Kind.FOLLOW, b, add2, p,
                                tx("firstfollow_em_1_2_follow_3", p, tail, p.lhs, b)
                            )
                        }
                    }
                }
            }
        }
        followRounds = rounds
        this.follow = follow
        this.steps = steps
    }

    /** FIRST de uma sequência de símbolos (pode conter ε). */
    fun firstOfString(symbols: List<String>): Set<String> {
        val out = mutableSetOf<String>()
        for (x in symbols) {
            if (grammar.isNonTerminal(x)) {
                out += first.getValue(x) - EPS
                if (x !in nullable) return out
            } else {
                out += x
                return out
            }
        }
        out += EPS
        return out
    }

    fun firstOfSymbol(x: String): Set<String> =
        if (grammar.isNonTerminal(x)) first.getValue(x) else setOf(x)
}
