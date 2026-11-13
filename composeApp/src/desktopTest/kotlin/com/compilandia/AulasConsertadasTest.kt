package com.compilandia

import com.compilandia.content.episodes.ARVORE_MENOS_NOVA
import com.compilandia.content.episodes.ARVORE_MENOS_VELHA
import com.compilandia.content.episodes.GRAMATICA_FOLLOW_3
import com.compilandia.content.episodes.tabelaSlrDaAula
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.ParseNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Confere contra o motor o conteúdo novo das aulas consertadas na revisão. */
class AulasConsertadasTest {
    /** Toda subárvore usa uma regra da gramática (número vira n), e as folhas formam a frase. */
    private fun confere(arvore: ParseNode, gramatica: String) {
        val g = Grammar.parse(gramatica)
        fun simbolo(s: String) = if (s.all { it.isDigit() }) "n" else s
        fun anda(no: ParseNode) {
            if (no.isLeaf) return
            val rhs = no.children.map { simbolo(it.symbol) }.filter { it != "ε" }
            assertTrue(g.productionsOf(no.symbol).any { it.rhs == rhs }, "${no.symbol} -> $rhs não é regra de: $gramatica")
            no.children.forEach(::anda)
        }
        anda(arvore)
        assertEquals(listOf("9", "-", "5", "-", "2"), arvore.leaves())
    }

    @Test fun arvoreVelhaPendeParaAEsquerda() {
        confere(ARVORE_MENOS_VELHA, "E -> E - n | n")
        assertEquals("E", ARVORE_MENOS_VELHA.children.first().symbol, "o filho da esquerda é o E que cresce")
    }

    @Test fun arvoreNovaPendeParaADireita() {
        confere(ARVORE_MENOS_NOVA, "E -> n E'\nE' -> - n E' | ε")
        assertEquals("E'", ARVORE_MENOS_NOVA.children.last().symbol, "o filho da direita é o E' que cresce")
    }

    /** A aula pergunta em qual coluna o estado 2 não reduz: n vazia, + e $ com r2. */
    @Test fun tabelaSlrInteira() {
        val t = tabelaSlrDaAula()
        assertEquals(listOf("", "n", "+", "$", "E"), t[0])
        assertEquals(6, t.size, "cabeçalho e os 5 estados de E -> E + n | n")
        assertEquals(listOf("2", "", "r2", "r2", ""), t[3])
        assertEquals("1", t[1][4], "GOTO de E no estado 0")
        assertTrue(t.drop(1).any { "ok" in it }, "a linha que aceita")
    }

    /** O caso difícil do FOLLOW: C some, então o d também entra no FOLLOW de B (a aula pede c e d, e não $ nem a). */
    @Test fun followComVizinhoQueSome() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_FOLLOW_3.joinToString("\n")))
        assertEquals(setOf("c", "d"), f.follow["B"])
        assertTrue("ε" in f.firstOfSymbol("C"), "C precisa ser anulável, senão o d não entra")
    }
}
