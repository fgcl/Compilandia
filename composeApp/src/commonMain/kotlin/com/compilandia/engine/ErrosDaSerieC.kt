package com.compilandia.engine

/*
 * Os erros comuns das aulas Em C 2 e Em C 3 (pedido do revisor professor do bloco 6), rodados de verdade.
 * Em C 2: escrever E → E + T ao pé da letra, com o expr chamando a si mesmo antes de ler um token.
 * Em C 3: chamar novo(...) sem guardar o nó em e, e o nó do mais se perde.
 */
object ErrosDaSerieC {
    /** Quantas chamadas o expr fez e quantos tokens leu até parar; [estourou] quando passou do [limite]. */
    data class Descida(val chamadas: Int, val lidos: Int, val estourou: Boolean)

    /**
     * Roda `void expr(void) { expr(); casa('+'); termo(); }` em [tokens]. Cada chamada conta; o limite faz o papel
     * da pilha de chamadas, que no C acaba e derruba o programa.
     */
    fun exprComRecursaoAEsquerda(tokens: List<String>, limite: Int = LIMITE): Descida {
        var chamadas = 0
        var lidos = 0
        fun termo() { if (lidos < tokens.size) lidos++ }
        fun expr(): Boolean {
            if (++chamadas > limite) return false
            if (!expr()) return false
            if (tokens.getOrNull(lidos) == "+") lidos++
            termo()
            return true
        }
        val terminou = expr()
        return Descida(chamadas, lidos, !terminou)
    }

    /** Uma árvore de soma: folha (só [folha]) ou nó do mais com dois filhos. */
    data class No(val folha: String?, val esq: No? = null, val dir: No? = null) {
        /** O texto com parênteses só onde o filho também é um mais: (1 + 2) + 3. */
        fun texto(): String = folha ?: "${esq?.parenteses()} + ${dir?.parenteses()}"
        private fun parenteses(): String = if (folha != null) folha else "(${texto()})"
    }

    /**
     * O expr da Em C 3 sobre "1 + 2 + 3": `e = termo(); while (+) { casa('+'); e = novo('+', e, termo()); }`.
     * Com [guardaNo] falso, a linha fica só `novo('+', e, termo());`, sem o `e =`.
     */
    fun exprMontaArvore(tokens: List<String>, guardaNo: Boolean = true): No {
        var k = 0
        var e = No(tokens[k++])
        while (tokens.getOrNull(k) == "+") {
            k++
            val novo = No(null, e, No(tokens[k++]))
            if (guardaNo) e = novo
        }
        return e
    }

    /** O tamanho da pilha que o exemplo usa: bem menos que a de um programa, mas o efeito é o mesmo. */
    const val LIMITE = 1000
}
