package com.compilandia.tutor

import com.compilandia.content.Etapa
import com.compilandia.content.Guiado
import com.compilandia.content.Prova
import com.compilandia.content.Trilha
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Questão de prova de LL(1): do FIRST ao veredito, e o veredito sai da tabela do motor. */
class QuestaoDeProvaTest {
    @Test fun aTrilhaDaTabelaTemAQuestaoAntesDoDesafio() {
        val etapas = Trilha.passos(Skill.LL1_TABLE).map { it.etapa }
        assertTrue(Etapa.PROVA in etapas)
        assertEquals(etapas.indexOf(Etapa.DESAFIO) - 1, etapas.indexOf(Etapa.PROVA))
        val comProva = setOf(Skill.LL1_TABLE, Skill.SLR_TABLE, Skill.NFA_TO_DFA, Skill.DFA_MINIMIZATION)
        assertEquals(comProva, Skill.entries.filter { s -> Trilha.passos(s).any { it.etapa == Etapa.PROVA } }.toSet())
    }

    @Test fun tresQuestoesATerceiraComConflito() {
        for (semente in listOf(1L, 42L, 777L, 2024L)) {
            val ps = Prova.problemas(Skill.LL1_TABLE, semente)
            assertEquals(3, ps.size)
            for ((i, sim) in ps.withIndex()) {
                val fim = sim.passos.last().ask!!
                val certa = fim.opcoes[fim.certa]
                assertEquals(3, fim.opcoes.toSet().size, "veredito com três opções: ${fim.opcoes}")
                if (i < 2) assertTrue(certa.startsWith("Sim"), "questão ${i + 1} é LL(1): $certa")
                else assertTrue(certa.startsWith("Não: a casa"), "a terceira tem conflito: $certa")
                // A parte do FIRST, a do FOLLOW e a tabela estão todas lá, nessa ordem.
                val titulos = sim.passos.mapNotNull { p -> p.shot.boxes.firstOrNull { it.id == com.compilandia.content.TITULO_ID }?.text }.distinct()
                assertTrue(titulos.indexOf("FIRST") in 1 until titulos.indexOf("FOLLOW"), "ordem das partes: $titulos")
            }
        }
    }

    @Test fun vereditoBateComOMotor() {
        for (txt in Guiado.GRAMATICAS_LL + Guiado.GRAMATICAS_NAO_LL) {
            val g = Grammar.parse(txt)
            val t = LL1Table(g)
            val a = QuestaoDeProva.ll1(g).passos.last().ask!!
            val certa = a.opcoes[a.certa]
            if (t.isLL1) assertTrue(certa.startsWith("Sim"), txt)
            else assertTrue(t.conflicts.first().let { (nt, c) -> "($nt, $c)" in certa }, "$txt: $certa")
        }
    }

    @Test fun aAjudaSomeNasTresQuestoes() {
        val ps = Prova.problemas(Skill.LL1_TABLE, 42)
        assertTrue(Prova.exigir(ps[0], 0).isEmpty())
        val perguntas = ps[1].passos.indices.filter { ps[1].passos[it].ask != null }
        val metade = Prova.exigir(ps[1], 1)
        assertTrue(metade.size in perguntas.size / 3..perguntas.size * 2 / 3 + 1, "metade: ${metade.size} de ${perguntas.size}")
        assertTrue(perguntas.last() in metade, "o veredito é sempre da pessoa na segunda")
        assertEquals(ps[2].passos.indices.filter { ps[2].passos[it].ask != null }.toSet(), Prova.exigir(ps[2], 2))
    }
}
