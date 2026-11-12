@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot

import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.screens.Cola
import com.compilandia.ui.screens.gramaticaDaAula
import com.compilandia.ui.screens.partesDaAula
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/** Mural, rodada 13: a cola do assunto e o botão Gramática da aula. */
class AulaExtrasTest {
    init { Narrator.silent = true }
    private val saida = File("build/screenshots").apply { mkdirs() }
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun estado(vararg telas: Screen) = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1))).also { a -> telas.forEach { a.stack.add(it) } }

    @Test fun todaAulaTemCola() {
        val semCola = Skill.entries.filter { s -> Episodes.all.any { it.skill == s && it.temporada == Temporada.BASICO } && Cola.receita(s).size < 2 }
        assertTrue(semCola.isEmpty(), "aulas sem receita para a cola: $semCola")
    }

    /** Mural, rodada 14: toda aula básica se divide em por quê, exemplo, receita e sua vez. */
    @Test fun todaAulaTemAsQuatroPartes() {
        val basicas = Episodes.all.filter { it.temporada == Temporada.BASICO }
        val sem = basicas.filter { e -> partesDaAula(e.passos, e.inicioDoExemplo).let { p -> p == null || p.size != 4 || p.any { it.isEmpty() } || p.last().last != e.passos.lastIndex } }
        assertTrue(sem.isEmpty(), "aulas sem as quatro partes: ${sem.map { it.skill }}")
    }

    @Test fun aGramaticaDaAulaEAQueEstaEmUso() {
        val ep = Episodes.of(Skill.LL1_PARSING)!!
        val i = ep.passos.indexOfFirst { p -> p.shot.boxes.any { it.text == "id + id" } }
        assertTrue(gramaticaDaAula(ep, i).orEmpty().contains("E -> T X"), "o primeiro exemplo do LL(1) mostra a gramática")
        assertTrue(gramaticaDaAula(ep, 0) == null, "antes de a gramática aparecer, não há botão")
    }

    @Test fun painelDaGramaticaAbreNaAula() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(Screen.Episode(Skill.FOLLOW, abertura = false))
        setContent { App(app) }
        mainClock.advanceTimeBy(1500)
        onNodeWithTag("botao-gramatica").performClick()
        mainClock.advanceTimeBy(900)
        onNodeWithTag("painel-gramatica", useUnmergedTree = true).assertExists()
        foto("aula_painel_gramatica")
    }

    @Test fun colaAbreNaTrilha() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(Screen.Trilha(Skill.FIRST))
        setContent { App(app) }
        mainClock.advanceTimeBy(1200)
        onNodeWithTag("abre-cola").performClick()
        mainClock.advanceTimeBy(1500)
        onNodeWithTag("cola").assertExists()
        // O diálogo é uma segunda raiz: a foto é a da cola.
        ImageIO.write(onAllNodes(isRoot(), useUnmergedTree = false).fetchSemanticsNodes().let { onAllNodes(isRoot(), useUnmergedTree = false)[it.lastIndex] }.captureToImage().toAwtImage(), "png", File(saida, "trilha_cola.png"))
        // Rodada 15: da trilha, a cola leva direto a um jogo do assunto.
        onNodeWithTag("cola-praticar").performClick()
        mainClock.advanceTimeBy(1500)
        assertTrue(app.stack.last() is Screen.Play, "Praticar na cola abre um jogo, mas a tela é ${app.stack.last()}")
        assertTrue((app.stack.last() as Screen.Play).skill == Skill.FIRST)
    }
}
