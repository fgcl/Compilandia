package com.compilandia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.compilandia.content.Guiado
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.lab.SimPlayer
import kotlin.test.Test

/** Fotos do "Fazer com ajuda" dos quatro assuntos novos, da Questão de prova e do cartão marcado da revisão relâmpago. */
class FazerComAjudaRenderTest : RenderBase() {
    private val assuntos = listOf(Skill.LR0_ITEMS, Skill.AMBIGUITY, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING, Skill.REG_ALLOC)

    @Test fun perguntasEmTelaEstreita() {
        for (s in assuntos) {
            shot("fca_${s.name.lowercase()}_tela", w = 360, h = 760) { App(stateWith(Screen.FazerComAjuda(s, semente = 42))) }
            val sim = Guiado.problema(s, 3, 42)!!
            val todas = Guiado.exigir(sim, 3)
            for (k in todas.sorted().take(4)) {
                shot("fca_${s.name.lowercase()}_p$k", w = 360, h = 760) {
                    CompilandiaTheme { SimPlayer(sim, Modifier.fillMaxSize().background(Palette.paper), comecaEm = k, exigir = todas) }
                }
            }
        }
    }

    @Test fun questaoDeProva() {
        for (s in listOf(Skill.LL1_TABLE, Skill.SLR_TABLE, Skill.NFA_TO_DFA, Skill.DFA_MINIMIZATION, Skill.TAC, Skill.REG_ALLOC)) {
            val nome = s.name.lowercase()
            shot("prova_${nome}_tela", w = 360, h = 760) { App(stateWith(Screen.FazerComAjuda(s, semente = 42, prova = true))) }
            val ps = com.compilandia.content.Prova.problemas(s, 42)
            for ((p, sim) in ps.withIndex()) {
                val pergs = sim.passos.indices.filter { sim.passos[it].ask != null }
                // A primeira pergunta, uma do meio e a última.
                for (k in listOf(pergs.first(), pergs[pergs.size / 2], pergs.last()).distinct()) {
                    shot("prova_${nome}_q${p + 1}_p$k", w = 360, h = 760) {
                        CompilandiaTheme { SimPlayer(sim, Modifier.fillMaxSize().background(Palette.paper), comecaEm = k, exigir = pergs.toSet()) }
                    }
                }
            }
        }
        shot("prova_trilha", w = 360, h = 900) { App(stateWith(Screen.Trilha(Skill.LL1_TABLE))) }
    }

    @Test fun cartaoDoTexto() {
        shot("ficha_do_texto", w = 360, h = 760) { App(stateWith(Screen.Flashcards(listOf(Skill.OPTIMIZATION)))) }
    }
}
