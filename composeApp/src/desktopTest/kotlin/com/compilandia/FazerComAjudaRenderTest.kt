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
    private val assuntos = listOf(Skill.LR0_ITEMS, Skill.AMBIGUITY, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)

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
        shot("prova_tela", w = 360, h = 760) { App(stateWith(Screen.FazerComAjuda(Skill.LL1_TABLE, semente = 42, prova = true))) }
        val ps = com.compilandia.content.Prova.problemas(Skill.LL1_TABLE, 42)
        for ((p, sim) in ps.withIndex()) {
            val exigir = com.compilandia.content.Prova.exigir(sim, p)
            val pergs = sim.passos.indices.filter { sim.passos[it].ask != null }
            // Uma pergunta da tabela e o veredito.
            for (k in listOf(pergs[pergs.size - 2], pergs.last())) {
                shot("prova_q${p + 1}_p$k", w = 360, h = 760) {
                    CompilandiaTheme { SimPlayer(sim, Modifier.fillMaxSize().background(Palette.paper), comecaEm = k, exigir = exigir.ifEmpty { pergs.toSet() }) }
                }
            }
        }
        shot("prova_trilha", w = 360, h = 900) { App(stateWith(Screen.Trilha(Skill.LL1_TABLE))) }
    }

    @Test fun cartaoDoTexto() {
        shot("ficha_do_texto", w = 360, h = 760) { App(stateWith(Screen.Flashcards(listOf(Skill.OPTIMIZATION)))) }
    }
}
