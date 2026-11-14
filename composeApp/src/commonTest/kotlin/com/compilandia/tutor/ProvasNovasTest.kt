package com.compilandia.tutor

import com.compilandia.content.Prova
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRTable
import com.compilandia.engine.Minimizer
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.setText
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Questões de prova de SLR, AFN→AFD e minimização: cada resposta certa é a do motor. */
class ProvasNovasTest {
    private fun asks(s: Sim) = s.passos.mapNotNull { it.ask }

    private fun tresOpcoes(s: Sim, onde: String) = asks(s).forEach {
        assertEquals(3, it.opcoes.toSet().size, "$onde: ${it.pergunta} ${it.opcoes}")
    }

    @Test fun slrTabelaEVeredito() {
        val ps = Prova.problemas(Skill.SLR_TABLE, 1)
        assertEquals(3, ps.size)
        for ((i, txt) in ProvaSlr.GRAMATICAS.withIndex()) {
            val t = LRTable(Grammar.parse(txt), LRKind.SLR1)
            val sim = ps[i]
            tresOpcoes(sim, txt)
            // Cada casa perguntada tem as ações do motor.
            for (a in asks(sim).filter { it.pergunta.startsWith("O que vai em ACTION") }) {
                val (st, term) = Regex("""ACTION\[(\d+), (.+)]""").find(a.pergunta)!!.destructured
                assertEquals(t.rawActions.getValue(st.toInt() to term).joinToString("/") { it.short() }, a.opcoes[a.certa], "$txt ${a.pergunta}")
            }
            val v = asks(sim).last()
            assertEquals(t.conflicts.isEmpty(), v.opcoes[v.certa].startsWith("Sim"), txt)
        }
        assertTrue(LRTable(Grammar.parse(ProvaSlr.GRAMATICAS[2]), LRKind.SLR1).conflicts.isNotEmpty(), "a terceira tem conflito")
    }

    @Test fun dtranCadaCasaEOFechoDoMove() {
        val ps = Prova.problemas(Skill.NFA_TO_DFA, 1)
        for ((i, id) in Prova.AFNS.withIndex()) {
            val nfa = SmallNFA.bank.first { it.id == id }
            val r = nfa.subsetConstruction()
            val sim = ps[i]
            tresOpcoes(sim, id)
            val casas = asks(sim).filter { it.pergunta.startsWith("Dtran") }
            assertEquals(r.steps.map { setText(it.to) }, casas.map { it.opcoes[it.certa] }, id)
            assertEquals(setText(r.states[0]), asks(sim).first().let { it.opcoes[it.certa] })
        }
        // O terceiro AFN traz o fecho que muda o conjunto e um move vazio.
        val ou = SmallNFA.bank.first { it.id == "ou" }.subsetConstruction()
        assertTrue(ou.steps.any { it.to != it.moved && it.moved.isNotEmpty() } && ou.steps.any { it.to.isEmpty() })
    }

    @Test fun escadinhaComAsRodadasDoMotor() {
        val ps = Prova.problemas(Skill.DFA_MINIMIZATION, 1)
        for ((i, d) in Prova.afdsDaMinimizacao().withIndex()) {
            val res = Minimizer.run(d)
            val sim = ps[i]
            tresOpcoes(sim, "afd $i")
            val porPar = asks(sim).filter { it.pergunta.startsWith("Lendo") }
            assertEquals(res.marks.count { it.round > 1 }, porPar.size, "afd $i")
            val fim = asks(sim).last()
            val juntam = res.classes.any { it.size > 1 }
            assertEquals(!juntam, fim.opcoes[fim.certa] == "Nenhum", "afd $i: ${fim.opcoes[fim.certa]}")
        }
        assertEquals(3, Minimizer.run(Prova.afdsDaMinimizacao()[0]).rounds, "o AFD do livro tem três rodadas")
        assertTrue(Minimizer.run(Prova.afdsDaMinimizacao()[2]).deadState != null, "o terceiro precisa do estado morto")
    }
}
