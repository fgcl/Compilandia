package com.compilandia

import com.compilandia.content.episodes.BISON_CALC
import com.compilandia.content.episodes.BISON_MENOS
import com.compilandia.content.episodes.BISON_MENOS_LEFT
import com.compilandia.content.episodes.BISON_SPEC_1
import com.compilandia.content.episodes.ENTRADA_ERRO_2
import com.compilandia.content.episodes.ENTRADA_PREDITIVO_2
import com.compilandia.content.episodes.ENTRADA_SR_2
import com.compilandia.content.episodes.GRAMATICA_ERRO_2
import com.compilandia.content.episodes.GRAMATICA_PREDITIVO_2
import com.compilandia.content.episodes.GRAMATICA_SR_2
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Parser
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LLAction
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniBison
import com.compilandia.engine.PanicMode
import com.compilandia.engine.RecursiveDescentGen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A9: o segundo exemplo das aulas longas dos analisadores afirma o que o motor calcula. */
class A9ParsersFactsTest {
    private fun gram(linhas: List<String>) = Grammar.parse(linhas.joinToString("\n"))

    /** Preditivo: L vira id R com o id no topo; vírgula escolhe R -> , id R; cifrão escolhe R -> ε; aceita em sete passos. */
    @Test fun preditivoNaLista() {
        val t = LL1Table(gram(GRAMATICA_PREDITIVO_2))
        assertTrue(t.isLL1)
        val tr = LL1Parser.trace(t, ENTRADA_PREDITIVO_2)
        assertEquals(7, tr.size, "sete passos: ${tr.map { it.action }}")
        assertEquals(listOf("$", "L"), tr[0].stack)
        assertEquals("id R", (tr[0].action as LLAction.Expand).production.rhsText())
        assertEquals(listOf("$", "R", "id"), tr[1].stack, "o id fica no topo")
        assertEquals(LLAction.Match("id"), tr[1].action)
        assertEquals(listOf("$", "R"), tr[2].stack)
        assertEquals(",", tr[2].input.first())
        assertEquals(", id R", (tr[2].action as LLAction.Expand).production.rhsText())
        assertEquals(LLAction.Match(","), tr[3].action)
        assertEquals(LLAction.Match("id"), tr[4].action)
        assertEquals(listOf("$", "R"), tr[5].stack)
        assertEquals(listOf("$"), tr[5].input)
        assertTrue((tr[5].action as LLAction.Expand).production.isEpsilon)
        assertEquals(LLAction.Accept, tr[6].action)
        // a receita: com X no topo e fecha parêntese, a tabela troca X por épsilon
        val g1 = LL1Table(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id | ( E )"))
        assertTrue(g1.cell("X", ")").single().isEpsilon)
    }

    /** Descida recursiva: L casa id e chama R; R testa a vírgula e chama R de novo; R roda duas vezes em "id , id". */
    @Test fun descidaNaLista() {
        val t = LL1Table(gram(GRAMATICA_PREDITIVO_2))
        val l = RecursiveDescentGen.function(t, "L").map { it.text }
        assertEquals(listOf("void L() {", "casaToken(\"id\");", "R();", "}"), l)
        val r = RecursiveDescentGen.function(t, "R").map { it.text }
        assertEquals("if (tok == \",\") {", r[1])
        assertEquals(listOf("casaToken(\",\");", "casaToken(\"id\");", "R();"), r.subList(2, 5))
        assertTrue(r.contains("} else {"), "a regra épsilon fica no else: $r")
        val chamadasDeR = LL1Parser.trace(t, ENTRADA_PREDITIVO_2).count { (it.action as? LLAction.Expand)?.production?.lhs == "R" }
        assertEquals(2, chamadasDeR)
        // a segunda chamada vê o fim da entrada
        val ultima = LL1Parser.trace(t, ENTRADA_PREDITIVO_2).last { (it.action as? LLAction.Expand)?.production?.lhs == "R" }
        assertEquals(listOf("$"), ultima.input)
        // a receita: sem regra épsilon, o último else avisa o erro
        val semEps = RecursiveDescentGen.function(LL1Table(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id | ( E )")), "T").map { it.text.trim() }
        assertTrue(semEps.any { it.startsWith("erro(") }, "$semEps")
    }

    /** Modo pânico: trava no segundo igual, descarta só ele e segue no num; no segundo erro desempilha Val; chega ao fim. */
    @Test fun panicoNosComandos() {
        val t = LL1Table(gram(GRAMATICA_ERRO_2))
        assertTrue(t.isLL1)
        assertEquals(setOf("id", "num"), t.ff.first["Val"])
        assertEquals(setOf(";"), t.ff.follow["Val"])
        assertEquals(2, PanicMode.firstErrorPosition(t, ENTRADA_ERRO_2), "trava no segundo igual (k2)")
        val tr = PanicMode.trace(t, ENTRADA_ERRO_2)
        assertTrue(tr.reachedEnd)
        assertEquals(LLAction.Accept, tr.steps.last().action, "leu até o fim")
        assertEquals(2, tr.events.size, "${tr.events}")
        val (e1, e2) = tr.events
        assertEquals(listOf("="), e1.skipped)
        assertEquals(3, e1.position, "para no num (k3)")
        assertNull(e1.popped, "num está no FIRST de Val: Val fica")
        assertEquals(emptyList(), e2.skipped)
        assertEquals(7, e2.position, "o ponto e vírgula (k7)")
        assertEquals("Val", e2.popped)
    }

    /** Empilha-reduz em ( id ): empilha, empilha, reduz id, empilha o fecha, reduz ( S ), aceita. Seis passos. */
    @Test fun empilhaReduzNosParenteses() {
        val t = LRTable(gram(GRAMATICA_SR_2), LRKind.SLR1)
        val r = LRParser.parse(t, ENTRADA_SR_2)
        assertTrue(r.accepted)
        val acoes = r.steps.map { s -> s.action.let { if (it is LRAction.Shift) "S" else if (it is LRAction.Reduce) "R" else "A" } }
        assertEquals(listOf("S", "S", "R", "S", "R", "A"), acoes)
        assertEquals(listOf("(", "id"), r.steps[2].symbols, "reduz com ( id na pilha")
        assertEquals(listOf("(", "S"), r.steps[3].symbols, "com ( S, empilha")
        assertTrue(r.steps[3].action is LRAction.Shift)
        assertEquals(listOf("(", "S", ")"), r.steps[4].symbols, "as três peças viram S")
        assertEquals(listOf("S"), r.steps[5].symbols)
        // o porquê: E -> E + n é recursiva à esquerda, e o preditivo não sai do lugar
        assertTrue(Grammar.parse("E -> E + n | n").let { g -> g.productions.any { it.lhs == "E" && it.rhs.firstOrNull() == "E" } })
    }

    /** Bison: a calculadora tem 14 estados e 8 colunas; o menos sem precedência dá 1 conflito e 4; com %left, 0 e 2. */
    @Test fun bisonNasContas() {
        val calc = MiniBison.compile(BISON_CALC.joinToString("\n"))
        assertEquals(14, calc.table.automaton.states.size)
        assertEquals(8, calc.table.terminals.size)
        assertEquals(112, calc.table.automaton.states.size * calc.table.terminals.size)

        val um = MiniBison.compile(BISON_SPEC_1.joinToString("\n"))
        assertTrue(um.conflicts.isEmpty())
        assertEquals(7L, um.run("1 + 2 * 3").value)
        assertEquals(2L, um.run("5 - 2 - 1").value)

        val sem = MiniBison.compile(BISON_MENOS.joinToString("\n"))
        assertEquals(1, sem.conflicts.size)
        assertEquals("shift/reduce", sem.conflicts.single().kind)
        assertEquals("'-'", sem.conflicts.single().terminal)
        assertEquals(4L, sem.run("5 - 2 - 1").value, "empilhar primeiro junta pela direita")

        val left = MiniBison.compile(BISON_MENOS_LEFT.joinToString("\n"))
        assertTrue(left.conflicts.isEmpty())
        assertEquals(2L, left.run("5 - 2 - 1").value)
    }
}
