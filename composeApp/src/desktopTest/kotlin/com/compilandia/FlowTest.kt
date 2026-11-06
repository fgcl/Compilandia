@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.content.Shot
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.RoomItem
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Juice
import com.compilandia.ui.Screen
import com.compilandia.ui.motion.stagePoint
import com.compilandia.ui.screens.STAGE_TAG
import com.compilandia.ui.voice.Narrator
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Joga de verdade: toca nos botões e nas peças do palco. Captura de tela não prova que o toque funciona. */
class FlowTest {
    init { Narrator.silent = true }

    private fun estado(s: LearnerState = LearnerState(name = "Felipe", onboardingDone = true)) = AppState(Learner(s, Random(1)))

    private fun ComposeUiTest.anda(ms: Long = 1500) = mainClock.advanceTimeBy(ms)

    private fun ComposeUiTest.toca(shot: Shot, id: String) {
        onNodeWithTag(STAGE_TAG).performTouchInput { click(stagePoint(shot, id, width.toFloat(), height.toFloat())) }
        anda(300)
    }

    @Test fun daSalaAoFimDoEpisodioEDaiAoJogo() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        setContent { App(app) }
        anda()
        onNodeWithText("Continuar").performClick()
        anda()
        val s = app.screen as Screen.Episode
        onNodeWithText("Pular").performClick()   // o Bug abre a aula; tocar em Pular vai direto
        anda()
        for (p in Episodes.of(s.skill)!!.passos) {
            p.ask?.let { a -> for (id in a.right) toca(p.shot, id) }
            anda()
            onNodeWithText("Continuar").performClick()
            anda()
        }
        assertTrue(s.skill.name in app.ls.episodesSeen, "o episódio precisa contar como visto")
        assertTrue(app.ls.gears > 0, "ver a aula rende engrenagens")
        // A trilha (A1): depois da aula vem a próxima etapa, que é aula da temporada ou o jogo.
        onNodeWithText("Próxima etapa", substring = true).performScrollTo().performClick()
        anda()
        val depois = app.screen
        assertTrue(depois is Screen.Play || (depois is Screen.Episode && depois.id != null), "depois da aula vem a próxima etapa, veio $depois")
    }

    @Test fun todaPerguntaDeTodoEpisodioRespondeAoToque() = runDesktopComposeUiTest(412, 900, testTimeout = 8.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        val comPergunta = Episodes.all.filter { e -> e.passos.any { it.ask != null } }
        var atual by mutableStateOf(comPergunta.first())
        setContent {
            androidx.compose.runtime.key(atual.id) {
                com.compilandia.ui.CompilandiaTheme {
                    com.compilandia.ui.screens.EpisodeScreen(app, Screen.Episode(atual.skill, id = atual.id), onStep = atual.passos.indexOfFirst { it.ask != null }, abertura = false)
                }
            }
        }
        for (e in comPergunta) {
            atual = e
            anda(200)
            val a = e.passos.first { it.ask != null }
            val hits = Juice.hitTick
            for (id in a.ask!!.right) toca(a.shot, id)
            anda(100)
            assertEquals(hits + 1, Juice.hitTick, "${e.skill}: tocar na resposta certa precisa acertar (${a.ask!!.right})")
        }
    }

    @Test fun tocarErradoNaoBloqueia() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val e = Episodes.of(Skill.TOKENS)!!
        val k = e.passos.indexOfFirst { it.ask != null }
        val p = e.passos[k]
        val app = estado()
        setContent { com.compilandia.ui.CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(app, Screen.Episode(e.skill), onStep = k) } }
        anda()
        onNodeWithText("Dica").assertExists()
        val errada = p.ask!!.wrong.first()
        val erros = Juice.missTick
        repeat(3) { toca(p.shot, errada) }
        anda()
        assertEquals(erros + 3, Juice.missTick)
        onNodeWithText("Continuar").performClick()
        anda()
    }

    @Test fun comprarColocaNaSala() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(LearnerState(name = "Felipe", onboardingDone = true, gears = 100, gearsSeeded = true))
        setContent { App(app) }
        anda()
        onNodeWithText("Loja").performClick()
        anda()
        onNodeWithText("${RoomItem.ABAJUR.price}").performClick()
        anda(2500)
        assertTrue(RoomItem.ABAJUR.id in app.ls.roomItems)
        assertEquals(70, app.ls.gears)
        assertTrue(app.screen is Screen.Room, "depois de comprar, volta para ver a peça entrar")
    }

    @Test fun moveisAbremAsFerramentas() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        setContent { App(app) }
        anda()
        for ((rotulo, tela) in listOf("Mapa" to Screen.Map, "Laboratório" to Screen.Lab, "Loja" to Screen.Shop)) {
            onNodeWithText(rotulo).performClick()
            anda()
            assertEquals(tela, app.screen, "tocar em $rotulo")
            app.back()
            anda()
        }
    }

    @Test fun acertoNoJogoAtingeOBug() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false))
        app.stack.add(Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3))
        setContent { App(app) }
        anda()
        assertTrue(Juice.inArena)
        repeat(4) { Juice.correct(); anda(400) }
        Juice.wrong()
        anda()
        assertEquals(0, Juice.combo)
    }
}
