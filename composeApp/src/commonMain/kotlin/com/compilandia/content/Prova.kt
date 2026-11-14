package com.compilandia.content

import com.compilandia.engine.Grammar
import com.compilandia.learning.Skill
import com.compilandia.tutor.QuestaoDeProva
import com.compilandia.tutor.Sim
import kotlin.random.Random

/**
 * Questão de prova (seção 3 da revisão): a pessoa produz a resposta inteira, e não só reconhece.
 * Três questões: a primeira resolvida pelo Byte, a segunda pela metade, a terceira sem ajuda e com conflito.
 * Só a tabela LL(1) tem, por enquanto; SLR e AFN→AFD ficam para depois.
 */
object Prova {
    const val PROBLEMAS = 3

    fun disponivel(s: Skill): Boolean = s == Skill.LL1_TABLE

    /** A primeira é a gramática das aulas; a segunda, outra LL(1) do banco; a terceira, uma com conflito. */
    fun problemas(s: Skill, semente: Long): List<Sim> {
        if (!disponivel(s)) return emptyList()
        val rnd = Random(semente)
        val textos = listOf(Guiado.GRAMATICAS_LL[0], Guiado.GRAMATICAS_LL.drop(1).random(rnd), Guiado.GRAMATICAS_NAO_LL.random(rnd))
        return textos.map { QuestaoDeProva.ll1(Grammar.parse(it)) }
    }

    /** Na primeira o Byte responde tudo; na segunda, a pessoa responde uma pergunta sim, outra não; na terceira, todas. */
    fun exigir(sim: Sim, p: Int): Set<Int> {
        val perguntas = sim.passos.indices.filter { sim.passos[it].ask != null }
        return when (p) {
            0 -> emptySet()
            1 -> perguntas.filterIndexed { i, _ -> i % 2 == 1 }.toSet() + perguntas.last()
            else -> perguntas.toSet()
        }
    }
}
