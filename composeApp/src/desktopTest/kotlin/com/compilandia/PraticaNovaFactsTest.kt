package com.compilandia

import com.compilandia.content.episodes.PILHAS_SLR_PRATICA
import com.compilandia.content.episodes.TABELA_SLR_PRATICA
import com.compilandia.engine.EOF
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** O que a aula "Rode a tabela SLR" mostra precisa ser o que o motor calcula. */
class PraticaNovaFactsTest {
    private val t = LRTable(Grammar.parse("E -> E + n | n"), LRKind.SLR1)

    @Test fun aTabelaDaAulaEADoMotor() {
        val cols = TABELA_SLR_PRATICA.first().drop(1)
        assertEquals(t.automaton.states.size, TABELA_SLR_PRATICA.size - 1)
        for (linha in TABELA_SLR_PRATICA.drop(1)) {
            val s = linha[0].toInt()
            cols.forEachIndexed { k, col ->
                val esperado = if (col == "E") t.gotoTable[s to "E"]?.toString().orEmpty() else when (val a = t.action[s to col]) {
                    null -> ""
                    is LRAction.Shift -> "s${a.state}"
                    is LRAction.Reduce -> "r${a.prodId}"
                    LRAction.Accept -> "ok"
                }
                assertEquals(esperado, linha[k + 1], "estado $s, coluna $col")
            }
        }
        assertEquals(EOF, "$")
        assertEquals("E → E + n", t.grammar.productions[1].toString())
        assertEquals("E → n", t.grammar.productions[2].toString())
    }

    @Test fun asPilhasDaAulaSaoAsDoMotor() {
        val r = LRParser.parse(t, listOf("n", "+", "n"))
        assertTrue(r.accepted)
        assertEquals(PILHAS_SLR_PRATICA, r.steps.map { it.states.joinToString(" ") })
    }
}
