package com.compilandia.engine

/*
 * Blocos básicos e código morto num TAC com rótulos e desvios (o que o MiniLang.tac gera).
 * Líder: a primeira linha, cada rótulo (alguém pode pular para lá) e a linha logo depois de um desvio.
 * Um bloco vai de um líder até a linha antes do próximo. Dentro dele, as linhas rodam sempre todas, em ordem.
 */
object BlocosBasicos {
    private fun rotulo(l: String) = l.endsWith(":")
    private fun desvio(l: String) = l.startsWith("goto ") || l.startsWith("ifFalse ") || l.startsWith("if ")

    fun lideres(code: List<String>): Set<Int> {
        // sortedSetOf só existe na JVM e quebra a versão do navegador: junta e ordena no fim
        val out = mutableSetOf(0)
        code.forEachIndexed { i, l ->
            if (rotulo(l)) out += i
            if (desvio(l) && i + 1 < code.size) out += i + 1
        }
        return out.sorted().toSet()
    }

    /** Os blocos, como faixas de linhas. */
    fun blocos(code: List<String>): List<IntRange> {
        val ls = lideres(code).toList()
        return ls.mapIndexed { k, i -> i until (ls.getOrNull(k + 1) ?: code.size) }
    }

    /**
     * Código morto num bloco sem desvio: a linha "x = ..." cujo x ninguém lê depois e que não está viva no fim.
     * Anda de baixo para cima, como a vivacidade.
     */
    fun mortas(bloco: List<String>, vivasNoFim: Set<String>): Set<Int> {
        val vivas = vivasNoFim.toMutableSet()
        val out = mutableSetOf<Int>()
        for (i in bloco.indices.reversed()) {
            val (alvo, conta) = bloco[i].split(" = ", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            if (alvo !in vivas) { out += i; continue }
            vivas -= alvo
            vivas += conta.split(" ").filter { it.firstOrNull()?.isLetter() == true }
        }
        return out
    }
}
