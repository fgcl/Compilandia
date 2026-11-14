package com.compilandia.content

import com.compilandia.engine.DFA
import com.compilandia.engine.Grammar
import com.compilandia.engine.Minimizer
import com.compilandia.engine.SmallNFA
import com.compilandia.learning.Skill
import com.compilandia.tutor.ProvaAutomatos
import com.compilandia.tutor.ProvaSlr
import com.compilandia.tutor.QuestaoDeProva
import com.compilandia.tutor.Sim
import kotlin.random.Random

/**
 * Questão de prova (seção 3 da revisão): a pessoa produz a resposta inteira, e não só reconhece.
 * Três questões: a primeira resolvida pelo Byte, a segunda pela metade, a terceira sem ajuda e com conflito.
 * LL(1), SLR(1), AFN→AFD (tabela Dtran) e minimização (escadinha de pares).
 */
object Prova {
    const val PROBLEMAS = 3

    private val assuntos = setOf(Skill.LL1_TABLE, Skill.SLR_TABLE, Skill.NFA_TO_DFA, Skill.DFA_MINIMIZATION)

    fun disponivel(s: Skill): Boolean = s in assuntos

    /** Do caso fácil ao difícil: a terceira questão sempre traz o caso que derruba na prova. */
    fun problemas(s: Skill, semente: Long): List<Sim> {
        val rnd = Random(semente)
        return when (s) {
            Skill.LL1_TABLE -> listOf(Guiado.GRAMATICAS_LL[0], Guiado.GRAMATICAS_LL.drop(1).random(rnd), Guiado.GRAMATICAS_NAO_LL.random(rnd))
                .map { QuestaoDeProva.ll1(Grammar.parse(it)) }
            Skill.SLR_TABLE -> ProvaSlr.GRAMATICAS.map { ProvaSlr.questao(Grammar.parse(it)) }
            Skill.NFA_TO_DFA -> AFNS.map { id -> ProvaAutomatos.subconjuntos(SmallNFA.bank.first { it.id == id }) }
            Skill.DFA_MINIMIZATION -> afdsDaMinimizacao().map { ProvaAutomatos.minimizacao(it) }
            else -> emptyList()
        }
    }

    /** Sem ε, com ε, e com ε que muda o conjunto e move vazio ((ab|a)c, o da aula). */
    val AFNS = listOf("ab", "abc", "ou")

    /** O AFD do livro (três rodadas), um com estados redundantes e um que precisa do estado morto. */
    fun afdsDaMinimizacao(): List<DFA> = listOf(
        Minimizer.bank[0].second,
        Minimizer.bank[2].second,
        Minimizer.rawFromRegex("ab*"),
    )

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
