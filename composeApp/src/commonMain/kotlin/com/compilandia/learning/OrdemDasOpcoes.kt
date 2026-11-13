package com.compilandia.learning

import kotlin.random.Random

/**
 * Em que ordem mostrar as alternativas de uma pergunta escrita à mão.
 *
 * Quem escreve pergunta tende a pôr a certa no meio: nas 137 "Preveja" de 4 opções, a certa era
 * a segunda 84 vezes e a última nenhuma. Quem joga aprende o padrão e acerta sem entender.
 * O conserto é o de Moodle, Canvas e TAO: o texto guarda a ordem em que foi escrito, e a tela
 * embaralha na hora de mostrar. As regras de redação de itens (Haladyna, Downing e Rodriguez, 2002)
 * pedem ordem "lógica ou aleatória", e daí as duas exceções:
 *
 * - Opções que são todas números ficam em ordem crescente, como numa régua.
 * - Opção que fala das outras ("nenhuma", "as duas", "depende") fica no fim, como o "pin" do TAO.
 *   Embaralhada, ela pode aparecer antes das opções a que se refere.
 *
 * Só muda a ordem na tela. A resposta e o índice da certa continuam os do texto.
 */
object OrdemDasOpcoes {
    /** Muda a cada abertura do jogo, para a mesma pergunta não ter sempre a mesma ordem. Os testes fixam. */
    var sal: Long = Random.nextLong()

    private val coringas = listOf(
        "nenhum", "nenhuma", "todas as anteriores", "todas acima", "as duas", "os dois", "ambas", "ambos", "depende", "não dá", "nao da",
        "none", "all of", "both", "it depends", "can't tell", "cannot tell",
    )

    /** Índices de [opcoes] na ordem de mostrar. [chave] identifica a pergunta (o enunciado serve). */
    fun de(opcoes: List<String>, chave: String, sal: Long = this.sal): List<Int> {
        if (opcoes.size < 2) return opcoes.indices.toList()
        val numeros = opcoes.map { numero(it) }
        if (numeros.all { it != null }) return opcoes.indices.sortedBy { numeros[it]!! }
        val (fixas, soltas) = opcoes.indices.partition { coringa(opcoes[it]) }
        val rnd = Random(chave.hashCode().toLong() * 31 + sal)
        return soltas.shuffled(rnd) + fixas
    }

    private fun numero(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

    private fun coringa(s: String): Boolean {
        val t = s.trim().lowercase()
        return coringas.any { t.startsWith(it) }
    }
}
