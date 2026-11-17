package com.compilandia.fabrica

/**
 * A loja da fábrica: a árvore de pesquisa do Mindustry, paga em engrenagens.
 * Comprar destrava o tipo para sempre; construir no chão não custa nada (a dificuldade está em montar,
 * não em juntar dinheiro). O que se compra aqui fica guardado no save, pelo [BlocoTipo.id].
 */
object Loja {
    enum class Situacao {
        /** Já pode construir. */
        TEM,
        /** Dá para comprar agora. */
        COMPRA,
        /** Falta engrenagem. */
        CARO,
        /** Falta destravar o bloco que vem antes na árvore. */
        TRANCADO,
    }

    fun tem(t: BlocoTipo, comprados: Collection<String>): Boolean = t.preco == 0 || t.id in comprados

    fun situacao(t: BlocoTipo, comprados: Collection<String>, engrenagens: Int): Situacao = when {
        tem(t, comprados) -> Situacao.TEM
        t.requer?.let { !tem(it, comprados) } == true -> Situacao.TRANCADO
        engrenagens < t.preco -> Situacao.CARO
        else -> Situacao.COMPRA
    }
}
