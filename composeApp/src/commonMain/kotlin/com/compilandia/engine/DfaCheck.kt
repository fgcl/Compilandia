package com.compilandia.engine

/** Compara dois autômatos sobre o mesmo alfabeto. Estado -1 é o buraco: nada mais casa dali. */
object DfaCheck {
    /**
     * A menor cadeia em que os dois discordam, ou null se aceitam a mesma linguagem.
     * Busca em largura no par de estados: a primeira discordância achada é a mais curta.
     */
    fun diferenca(a: DFA, b: DFA): String? {
        val alfabeto = (a.alphabet + b.alphabet).distinct().sorted()
        val inicio = a.start to b.start
        val pai = HashMap<Pair<Int, Int>, Pair<Pair<Int, Int>, Char>?>()
        pai[inicio] = null
        val fila = ArrayDeque(listOf(inicio))
        while (fila.isNotEmpty()) {
            val atual = fila.removeFirst()
            if ((atual.first in a.accepting) != (atual.second in b.accepting)) {
                val sb = StringBuilder()
                var p = atual
                while (true) {
                    val volta = pai[p] ?: break
                    sb.append(volta.second)
                    p = volta.first
                }
                return sb.reverse().toString()
            }
            for (c in alfabeto) {
                val prox = a.step(atual.first, c) to b.step(atual.second, c)
                if (prox !in pai) {
                    pai[prox] = atual to c
                    fila += prox
                }
            }
        }
        return null
    }

    /** Quantos estados bastam, sem contar o buraco, que não precisa ser desenhado. */
    fun pecasMinimas(d: DFA): Int {
        val m = d.minimize()
        val vivos = (0 until m.stateCount).filter { s ->
            // um estado só serve se alguma cadeia sai dele e chega num final
            val vistos = mutableSetOf(s)
            val fila = ArrayDeque(listOf(s))
            var chega = false
            while (fila.isNotEmpty()) {
                val q = fila.removeFirst()
                if (q in m.accepting) chega = true
                for (c in m.alphabet) { val r = m.step(q, c); if (r >= 0 && vistos.add(r)) fila += r }
            }
            chega
        }
        return vivos.size.coerceAtLeast(1)
    }
}
