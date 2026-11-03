@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.content.Speaker
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.screens.FALA_TAG
import com.compilandia.ui.voice.Narrator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A fala quebrada no meio da palavra ("Lida / s de / trás") veio da faixa estreita com a Profª no canto.
 * Aqui toda cena com a Profª, de toda aula, roda num celular de 360dp com legenda ligada,
 * e a fala precisa ter pelo menos 140dp de largura e caber na faixa.
 */
class FaixaNarradorTest {
    init { Narrator.silent = true }

    @Test fun falaDaProfCabeNoCelularEstreito() = runDesktopComposeUiTest(360, 760) {
        mainClock.autoAdvance = false
        val app = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true), Random(1)))
        app.learner.setCaptions(true); app.commit()
        val casos = Episodes.all.flatMap { e -> e.passos.indices.filter { e.passos[it].fala.who == Speaker.PROF }.map { e to it } }
        assertTrue(casos.size > 20, "esperava várias cenas com a Profª, achei ${casos.size}")
        var atual by mutableStateOf(casos.first())
        setContent {
            val (e, k) = atual
            key(e.id, k) { com.compilandia.ui.CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(app, Screen.Episode(e.skill, id = e.id), onStep = k, abertura = false) } }
        }
        val ruins = mutableListOf<String>()
        for (c in casos) {
            atual = c
            mainClock.advanceTimeBy(1500)
            val b = onNodeWithTag(FALA_TAG).fetchSemanticsNode().boundsInRoot
            if (b.width < 140f || b.height > 150f) ruins += "${c.first.title} #${c.second}: ${b.width.toInt()}x${b.height.toInt()} \"${c.first.passos[c.second].fala.text}\""
        }
        assertTrue(ruins.isEmpty(), "falas apertadas:\n" + ruins.joinToString("\n"))
    }
}
