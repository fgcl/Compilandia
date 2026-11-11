@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.voice.Narrator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Nenhuma tela é um beco: aberta por cima da sala, cada uma tem o botão voltar no canto de cima,
 * e ele leva de volta à sala. (O mapa e a corrida não tinham, e no navegador a pessoa ficava presa.)
 * A aula animada tem o próprio fechar, e o Laboratório tem a conversa; ficam fora da lista.
 */
class NavegacaoTest {
    init { Narrator.silent = true }

    private val telas = listOf(
        Screen.Map, Screen.Corrida, Screen.Shop, Screen.RegionView(Region.FLORESTA_LEXICA), Screen.Trilha(Skill.FOLLOW),
        Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3), Screen.Brain, Screen.Journal, Screen.Settings,
        Screen.Lab, Screen.LabBison(), Screen.FabGram, Screen.Album, Screen.Desafio, Screen.Feed, Screen.Guide,
        Screen.GuideChapter(1), Screen.SeuCompilador, Screen.Bancadas, Screen.Flashcards(listOf(Skill.FIRST)),
        Screen.BossFight(Region.TEMPLO_TOPDOWN), Screen.Exemplo(Skill.FIRST),
    )

    @Test fun todaTelaTemVoltar() {
        val presas = mutableListOf<String>()
        for (t in telas) runDesktopComposeUiTest(412, 900) {
            mainClock.autoAdvance = false
            val app = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1)))
            app.stack.add(t)
            setContent { App(app) }
            mainClock.advanceTimeBy(800)
            val voltar = onAllNodesWithTag("voltar").fetchSemanticsNodes()
            if (voltar.isEmpty()) presas += t.toString()
            else {
                onAllNodesWithTag("voltar")[0].performClick()
                mainClock.advanceTimeBy(800)
                assertEquals(Screen.Room, app.screen, "o voltar de $t leva para a sala")
            }
        }
        assertTrue(presas.isEmpty(), "telas sem voltar: $presas")
    }
}
