package com.compilandia

import com.compilandia.content.episodes.ARVORE_MENOS_MENOS
import com.compilandia.content.episodes.CONTA_MENOS
import com.compilandia.engine.ParseNode
import kotlin.test.Test
import kotlin.test.assertEquals

/** A árvore de a - b - c da aula de AST pende para a esquerda, e as contas da fala (5 contra 9) batem. */
class AstMenosMenosTest {
    private fun conta(n: ParseNode, v: Map<String, Int>): Int =
        if (n.children.isEmpty()) v.getValue(n.symbol) else conta(n.children[0], v) - conta(n.children[1], v)

    @Test fun oMenosJuntaPelaEsquerda() {
        val (a, b, c) = CONTA_MENOS
        val v = mapOf("a" to a, "b" to b, "c" to c)
        assertEquals(a - b - c, conta(ARVORE_MENOS_MENOS, v))
        assertEquals(5, conta(ARVORE_MENOS_MENOS, v))
        val direita = ParseNode("-", listOf(ParseNode("a"), ParseNode("-", listOf(ParseNode("b"), ParseNode("c")))))
        assertEquals(9, conta(direita, v))
        // O menos de baixo é o da esquerda: a - b.
        assertEquals(listOf("a", "b"), ARVORE_MENOS_MENOS.children[0].children.map { it.symbol })
    }
}
