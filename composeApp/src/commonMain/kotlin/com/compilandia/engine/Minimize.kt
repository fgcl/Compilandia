package com.compilandia.engine

/**
 * Minimização de AFD pelo MÉTODO DA TABELA (pares distinguíveis), do jeito que se faz à mão na prova:
 *  1. marque todo par (final, não-final);
 *  2. repita: marque {p,q} se, para algum símbolo c, o par {δ(p,c), δ(q,c)} já está marcado;
 *  3. pares não marcados são EQUIVALENTES → viram um estado só.
 * O AFD é completado com um estado morto (∅) quando falta alguma transição.
 */
object Minimizer {
    data class Mark(val p: Int, val q: Int, val round: Int, val reason: String)

    data class Result(
        val dfa: DFA,                       // AFD completo usado na tabela
        val deadState: Int?,                // índice do estado morto adicionado (se houve)
        val marks: List<Mark>,              // em ordem de marcação
        val classes: List<Set<Int>>,        // classes de equivalência
        val minimized: DFA,
    ) {
        val distinguishable: Set<Pair<Int, Int>> get() = marks.map { it.p to it.q }.toSet()
        fun stateName(i: Int) = if (i == deadState) "∅" else "q$i"
        val rounds: Int get() = marks.maxOfOrNull { it.round } ?: 0
    }

    /** Completa o AFD com um estado morto se faltar transição. */
    fun complete(d: DFA): Pair<DFA, Int?> {
        val missing = (0 until d.stateCount).any { s -> d.alphabet.any { c -> d.delta[s to c] == null } }
        if (!missing) return d to null
        val dead = d.stateCount
        val delta = d.delta.toMutableMap()
        for (s in 0..dead) for (c in d.alphabet) if (delta[s to c] == null) delta[s to c] = dead
        return DFA(d.stateCount + 1, d.start, d.accepting, d.alphabet, delta) to dead
    }

    fun run(input: DFA): Result {
        val (d, dead) = complete(input)
        val n = d.stateCount
        fun key(a: Int, b: Int) = if (a < b) a to b else b to a
        val marked = LinkedHashMap<Pair<Int, Int>, Mark>()
        fun nm(i: Int) = if (i == dead) "∅" else "q$i"
        for (p in 0 until n) for (q in p + 1 until n) {
            if ((p in d.accepting) != (q in d.accepting))
                marked[p to q] = Mark(p, q, 1, "${nm(if (p in d.accepting) p else q)} é final e ${nm(if (p in d.accepting) q else p)} não")
        }
        var round = 1
        var changed = true
        while (changed) {
            changed = false; round++
            val snapshot = marked.keys.toSet()
            for (p in 0 until n) for (q in p + 1 until n) {
                if ((p to q) in marked) continue
                for (c in d.alphabet) {
                    val a = d.delta.getValue(p to c); val b = d.delta.getValue(q to c)
                    if (a != b && key(a, b) in snapshot) {
                        marked[p to q] = Mark(p, q, round, "com '$c': ${nm(p)}→${nm(a)} e ${nm(q)}→${nm(b)}, e {${nm(minOf(a, b))},${nm(maxOf(a, b))}} já está marcado")
                        changed = true
                        break
                    }
                }
            }
        }
        // classes por união dos pares NÃO marcados
        val parent = IntArray(n) { it }
        fun find(x: Int): Int = if (parent[x] == x) x else find(parent[x]).also { parent[x] = it }
        for (p in 0 until n) for (q in p + 1 until n) if ((p to q) !in marked) parent[find(q)] = find(p)
        val classes = (0 until n).groupBy { find(it) }.values.map { it.toSet() }.sortedBy { it.min() }
        // AFD mínimo (sem o estado morto, se ele ficou sozinho)
        val keep = classes.filter { dead == null || dead !in it || it.size > 1 }
        val idx = HashMap<Int, Int>()
        keep.forEachIndexed { i, cl -> cl.forEach { idx[it] = i } }
        val delta = HashMap<Pair<Int, Char>, Int>()
        keep.forEachIndexed { i, cl ->
            val rep = cl.first()
            for (c in d.alphabet) idx[d.delta.getValue(rep to c)]?.let { delta[i to c] = it }
        }
        val acc = keep.indices.filter { i -> keep[i].any { it in d.accepting } }.toSet()
        val min = DFA(keep.size, idx.getValue(d.start), acc, d.alphabet, delta)
        return Result(d, dead, marked.values.toList(), classes, min)
    }

    /** AFD "cru" (subconjuntos sem minimizar) — bom para treinar, pois costuma ter estados sobrando. */
    fun rawFromRegex(regex: String): DFA {
        val r = Regex0.parse(regex)
        val alphabet = regex.filter { it.isLetterOrDigit() }.toSet().sorted()
        val nfa = NFA.thompson(r)
        val startSet = nfa.epsClosure(setOf(nfa.start))
        val sets = mutableListOf(startSet)
        val delta = mutableMapOf<Pair<Int, Char>, Int>()
        var i = 0
        while (i < sets.size) {
            for (c in alphabet) {
                val t = nfa.epsClosure(nfa.move(sets[i], c))
                if (t.isEmpty()) continue
                var j = sets.indexOf(t)
                if (j < 0) { sets += t; j = sets.size - 1 }
                delta[i to c] = j
            }
            i++
        }
        return DFA(sets.size, 0, sets.indices.filter { nfa.accept in sets[it] }.toSet(), alphabet, delta)
    }

    /** AFDs de treino: cada um tem estados equivalentes para juntar. */
    val bank: List<Pair<String, DFA>> by lazy {
        val out = mutableListOf<Pair<String, DFA>>()
        // Exemplo clássico do Livro do Dragão (fig. 3.36): A e C são equivalentes
        out += "(a|b)*abb — AFD do livro" to DFA(5, 0, setOf(4), listOf('a', 'b'), mapOf(
            (0 to 'a') to 1, (0 to 'b') to 2, (1 to 'a') to 1, (1 to 'b') to 3,
            (2 to 'a') to 1, (2 to 'b') to 2, (3 to 'a') to 1, (3 to 'b') to 4,
            (4 to 'a') to 1, (4 to 'b') to 2))
        out += "quantidade par de a's (com sobra)" to DFA(4, 0, setOf(0, 2), listOf('a'), mapOf(
            (0 to 'a') to 1, (1 to 'a') to 2, (2 to 'a') to 3, (3 to 'a') to 0))
        out += "termina em b (redundante)" to DFA(4, 0, setOf(1, 3), listOf('a', 'b'), mapOf(
            (0 to 'a') to 2, (0 to 'b') to 1, (1 to 'a') to 2, (1 to 'b') to 3,
            (2 to 'a') to 0, (2 to 'b') to 3, (3 to 'a') to 0, (3 to 'b') to 1))
        for (re in listOf("ab*", "(ab)*", "a*b*", "(a|b)*a")) {
            val d = rawFromRegex(re)
            if (d.stateCount in 3..6 && run(d).minimized.stateCount < d.stateCount) out += "$re (via subconjuntos)" to d
        }
        out
    }
}
