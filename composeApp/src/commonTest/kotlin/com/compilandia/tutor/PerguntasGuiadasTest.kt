package com.compilandia.tutor

import com.compilandia.content.Guiado
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.render
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** "Fazer com ajuda" em recursão, fatoração, itens LR(0) e ambiguidade: a resposta certa sai do motor. */
class PerguntasGuiadasTest {
    private fun asks(s: Sim) = s.passos.mapIndexedNotNull { i, p -> p.ask?.let { i to it } }.toMap()

    private fun tresOpcoes(a: SimAsk, onde: String) {
        assertEquals(3, a.opcoes.size, "$onde: ${a.opcoes}")
        assertEquals(3, a.opcoes.toSet().size, "$onde repete opção: ${a.opcoes}")
    }

    private fun alternativas(regra: String) = regra.substringAfter("→ ").split(" | ").toSet()

    @Test fun osQuatroAssuntosGanharamFazerComAjuda() {
        for (s in listOf(Skill.LR0_ITEMS, Skill.AMBIGUITY, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)) {
            assertTrue(Guiado.disponivel(s), "$s sem Fazer com ajuda")
        }
    }

    @Test fun recursaoPerguntaOPedacoERegrasDoMotor() {
        for (txt in PerguntasGuiadas.RECURSAO) {
            val g = Grammar.parse(txt)
            val a = asks(assertNotNull(Sims.recursao(g)))
            assertEquals(setOf(1, 3, 4), a.keys, txt)
            a.values.forEach { tresOpcoes(it, txt) }
            val nt = GrammarTransform.leftRecursiveNTs(g).first()
            val final = GrammarTransform.eliminateImmediateLeftRecursion(g)
            val novo = final.nonTerminals.first { it !in g.nonTerminals }
            assertEquals(final.productionsOf(nt).map { it.rhsText() }.toSet(), alternativas(a.getValue(3).let { it.opcoes[it.certa] }), txt)
            assertEquals(final.productionsOf(novo).map { it.rhsText() }.toSet(), alternativas(a.getValue(4).let { it.opcoes[it.certa] }), txt)
        }
    }

    @Test fun fatoracaoPerguntaOComecoERegrasDoMotor() {
        for (txt in PerguntasGuiadas.FATORACAO) {
            val g = Grammar.parse(txt)
            val a = asks(assertNotNull(Sims.fatorar(g), txt))
            assertEquals(setOf(1, 2, 3), a.keys, txt)
            a.values.forEach { tresOpcoes(it, txt) }
            val final = GrammarTransform.leftFactor(g)
            val novo = final.nonTerminals.first { it !in g.nonTerminals }
            val nt = novo.dropLast(1)
            assertEquals(final.productionsOf(nt).map { it.rhsText() }.toSet(), alternativas(a.getValue(2).let { it.opcoes[it.certa] }), txt)
            assertEquals(final.productionsOf(novo).map { it.rhsText() }.toSet(), alternativas(a.getValue(3).let { it.opcoes[it.certa] }), txt)
            // O começo certo é o prefixo que o motor tirou.
            val pre = final.productionsOf(nt).first { novo in it.rhs }.rhs.dropLast(1).joinToString(" ")
            assertEquals(pre, a.getValue(1).let { it.opcoes[it.certa] }, txt)
        }
    }

    @Test fun itensLr0CertosEstaoNoEstadoEErradosNao() {
        for (txt in PerguntasGuiadas.LR0) {
            val g = Grammar.parse(txt)
            val aut = LRAutomaton.lr0(g)
            val a = asks(Sims.lr0(g))
            assertTrue(a.size >= 3, "$txt tem ${a.size} perguntas")
            for ((k, ask) in a) {
                tresOpcoes(ask, "$txt estado $k")
                val itens = aut.states[k].items.map { it.render(aut.grammar) }.toSet()
                assertTrue(ask.opcoes[ask.certa] in itens, "$txt estado $k: ${ask.opcoes[ask.certa]} fora de $itens")
                ask.opcoes.filterIndexed { i, _ -> i != ask.certa }.forEach { assertTrue(it !in itens, "$txt estado $k: errada $it está no estado") }
            }
        }
    }

    @Test fun contasDuasArvoresComOsValoresCertos() {
        val esperado = mapOf(
            "9 - 5 - 2" to setOf(2, 6), "2 + 3 * 4" to setOf(14, 20), "8 - 2 * 3" to setOf(2, 18), "2 * 3 + 4" to setOf(10, 14),
            "1 + 2 + 3" to setOf(6), "7 - 4 + 2" to setOf(5, 1), "2 * 3 * 4" to setOf(24),
        )
        assertEquals(esperado.keys, PerguntasGuiadas.CONTAS.toSet())
        for ((frase, valores) in esperado) {
            val sim = assertNotNull(PerguntasGuiadas.contas(frase), frase)
            val a = asks(sim)
            assertEquals(3, a.size, frase)
            a.values.forEach { tresOpcoes(it, frase) }
            val (primeira, v1, v2) = a.keys.sorted().map { a.getValue(it).let { q -> q.opcoes[q.certa] } }
            assertEquals(valores, setOf(v1.toInt(), v2.toInt()), frase)
            assertTrue(primeira in listOf(frase.take(5), frase.takeLast(5)), "$frase: $primeira")
        }
    }
}
