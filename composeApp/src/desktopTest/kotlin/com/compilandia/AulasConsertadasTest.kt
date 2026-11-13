package com.compilandia

import com.compilandia.content.episodes.ARVORE_MENOS_NOVA
import com.compilandia.content.episodes.ARVORE_MENOS_VELHA
import com.compilandia.content.episodes.GRAMATICA_EXPR
import com.compilandia.content.episodes.GRAMATICA_FIRST_3
import com.compilandia.content.episodes.GRAMATICA_FOLLOW_3
import com.compilandia.content.episodes.GRAMATICA_LL1_3
import com.compilandia.content.episodes.GRAMATICA_LL1_4
import com.compilandia.content.episodes.GRAMATICA_PEG_FOLLOW
import com.compilandia.content.episodes.tabelaLl1DaAula
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Table
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

    /** FIRST, caso difícil: B some sem ε escrito, e na ordem das regras a primeira rodada deixa S só com o d. */
    @Test fun firstComAnulavelSemEpsilonEscrito() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_FIRST_3.joinToString("\n")))
        assertEquals(setOf("b", "c", "ε"), f.first["B"])
        assertEquals(setOf("b", "c", "d"), f.first["S"])
        val deS = f.steps.filter { it.kind == com.compilandia.engine.SetStep.Kind.FIRST && it.nonTerminal == "S" }
        assertEquals(setOf("d"), deS.first().added, "a primeira coisa que entra em S é o d")
        assertTrue(f.firstRounds >= 3, "o b e o c chegam em rodadas seguintes")
    }

    /** As perguntas novas do começo das aulas: o que vem depois de E no SLR, e a casa M[X, id] vazia na descida. */
    @Test fun perguntasDoComeco() {
        assertEquals(setOf("+", "$"), FirstFollow(Grammar.parse("E -> E + n | n")).follow["E"])
        val g = FirstFollow(Grammar.parse(GRAMATICA_EXPR.joinToString("\n")))
        assertTrue("id" !in g.first.getValue("X") && "id" !in g.follow.getValue("X"), "M[X, id] tem de ser vazia")
        assertTrue("+" in g.first.getValue("X") && ")" in g.follow.getValue("X"))
    }

    /** Tabela LL(1), caso difícil: S -> A B não tem ε escrito, mas vai em a, b e também no $ (FOLLOW de S). */
    @Test fun tabelaComRegraAnulavelSemEpsilon() {
        val t = tabelaLl1DaAula(GRAMATICA_LL1_3)
        assertEquals(listOf("", "a", "b", "$"), t[0])
        assertEquals(listOf("S", "A B", "A B", "A B"), t[1])
        val m = LL1Table(Grammar.parse(GRAMATICA_LL1_3.joinToString("\n")))
        assertTrue(m.isLL1, "nenhuma casa com duas regras")
        assertEquals(setOf("$"), m.ff.follow["S"])
    }

    /** Conflito FIRST × FIRST: as duas regras de S na casa (S, a); a fatoração da aula conserta. */
    @Test fun tabelaComConflitoFirstFirst() {
        val m = LL1Table(Grammar.parse(GRAMATICA_LL1_4.joinToString("\n")))
        assertEquals(listOf("S" to "a"), m.conflicts)
        assertEquals(2, m.cell("S", "a").size)
        assertTrue(tabelaLl1DaAula(GRAMATICA_LL1_4)[1].drop(2).all { it.isEmpty() }, "as outras casas ficam vazias")
        val fatorada = Grammar.parse("S -> a S'\nS' -> b | c")
        assertTrue(LL1Table(fatorada).isLL1)
        assertEquals(fatorada.toText(), GrammarTransform.leftFactor(Grammar.parse(GRAMATICA_LL1_4.joinToString("\n"))).toText())
    }

    /** Pegadinha do FOLLOW: B pode sumir, então o FOLLOW de A tem o d de B e o c que vem depois (não tem $ nem ε). */
    @Test fun pegadinhaDoFollowComVizinhoQueSome() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_PEG_FOLLOW.joinToString("\n")))
        assertEquals(setOf("d", "c"), f.follow["A"])
    }
}
