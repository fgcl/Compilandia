package com.compilandia.engine

/*
 * O DAG de um bloco básico (Dragão, seção 8.5.1), pedido do revisor professor do bloco 5.
 * Folha: o valor que a variável tinha na entrada do bloco (b0, c0...).
 * Nó interno: um operador com dois filhos. Antes de criar um, procura um nó com o mesmo operador
 * e os mesmos filhos: se achar, a linha só pendura mais um nome nele (subexpressão comum).
 * Cada linha é "x = a op b" ou "x = a".
 */
object DagDoBloco {
    data class No(val id: Int, val op: String?, val filhos: List<Int>, val folha: String?, val nomes: MutableList<String> = mutableListOf()) {
        val interno get() = op != null
    }

    data class Dag(val nos: List<No>, /** Para cada linha, o nó que ela nomeia. */ val noDaLinha: List<Int>, /** Linhas que reaproveitaram um nó: linha → a linha que o criou. */ val reusos: Map<Int, Int>) {
        val internos get() = nos.filter { it.interno }
    }

    fun monta(bloco: List<String>): Dag {
        val nos = mutableListOf<No>()
        val atual = mutableMapOf<String, Int>()   // variável → nó que tem o valor dela agora
        val criadoPor = mutableMapOf<Int, Int>()  // nó interno → linha que o criou
        val noDaLinha = mutableListOf<Int>()
        val reusos = mutableMapOf<Int, Int>()
        fun folha(v: String): Int = atual.getOrPut(v) { nos.size.also { nos += No(it, null, emptyList(), v + if (v[0].isLetter()) "0" else "") } }
        bloco.forEachIndexed { i, linha ->
            val (alvo, conta) = linha.split(" = ", limit = 2)
            val p = conta.trim().split(" ")
            val n = if (p.size == 1) folha(p[0]) else {
                val filhos = listOf(folha(p[0]), folha(p[2]))
                val achado = nos.firstOrNull { it.op == p[1] && it.filhos == filhos }
                if (achado != null) { reusos[i] = criadoPor.getValue(achado.id); achado.id }
                else nos.size.also { nos += No(it, p[1], filhos, null); criadoPor[it] = i }
            }
            // O nome sai do nó antigo: dali em diante, a variável é o valor novo.
            atual[alvo]?.let { nos[it].nomes.remove(alvo) }
            nos[n].nomes += alvo
            atual[alvo] = n
            noDaLinha += n
        }
        return Dag(nos, noDaLinha, reusos)
    }
}
