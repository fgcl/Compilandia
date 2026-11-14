package com.compilandia.tutor

import com.compilandia.content.Shot
import com.compilandia.content.codigo
import com.compilandia.content.titulo
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.SetStep
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/**
 * A questão de prova de LL(1) inteira, na ordem da prova: gramática, FIRST, FOLLOW, tabela e veredito.
 * Cada parte é a simulação que o Laboratório já usa; o veredito sai da tabela do motor.
 */
object QuestaoDeProva {
    fun ll1(g: Grammar): Sim {
        val t = LL1Table(g)
        val linhas = g.toText().lines().map { it.replace("→", "->") }
        val abre = SimStep(Shot(listOf(titulo(tx("trilha_etapa_prova"))) + codigo("g", linhas, 120f, x = 50f, passo = 34f, size = 16f)), tx("prova_abre"))
        val partes = listOf(Sims.conjuntos(g, SetStep.Kind.FIRST), Sims.conjuntos(g, SetStep.Kind.FOLLOW), Sims.tabelaLL(g))
        val corpo = partes.flatMap { it.passos }
        return Sim(tx("trilha_etapa_prova"), listOf(abre) + corpo + veredito(t, corpo.last().shot), Skill.LL1_TABLE)
    }

    /** "É LL(1)?": as erradas são as duas confusões de prova (casa vazia e regra com ε não tiram o LL(1)). */
    private fun veredito(t: LL1Table, shot: Shot): SimStep {
        val sim = tx("prova_sim")
        val vazia = tx("prova_nao_vazia")
        val eps = tx("prova_nao_eps")
        val conflito = t.conflicts.firstOrNull()
        val (certa, erradas, porque) = if (conflito == null) Triple(sim, listOf(vazia, eps), tx("prova_porque_sim"))
        else Triple(tx("prova_nao_casa", conflito.first, conflito.second), listOf(sim, vazia), tx("prova_porque_nao", conflito.first, conflito.second))
        val (ops, idx) = opcoes(certa, erradas, t.columns.size)
        return SimStep(shot, porque, SimAsk(tx("prova_pergunta"), ops, idx, porque), erro = conflito != null)
    }
}
