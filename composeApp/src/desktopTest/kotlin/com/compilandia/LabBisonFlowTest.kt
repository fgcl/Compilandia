@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.engine.MiniBison
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.ui.AppState
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Screen
import com.compilandia.ui.lab.BisonExemplos
import com.compilandia.ui.lab.LabBisonScreen
import com.compilandia.ui.lab.arvoreDaConta
import com.compilandia.ui.lab.contaCerta
import com.compilandia.ui.lab.pontoDePrecedencia
import com.compilandia.ui.voice.Narrator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/** O Laboratório Bison: a árvore mostra quem é calculado primeiro, e pôr %left conserta a conta. */
class LabBisonFlowTest {
    init { Narrator.silent = true }

    @Test fun aArvoreDaCalculadoraPoeAVezesEmbaixo() {
        val b = MiniBison.compile(BisonExemplos.calculadora)
        val r = b.run("2+3*4")
        val raiz = b.arvoreDaConta("2+3*4", r.tree!!)
        assertEquals("+", raiz.label)
        assertEquals("14", raiz.sub)
        assertEquals(listOf("2", "*"), raiz.children.map { it.label })
        assertEquals("12", raiz.children[1].sub)
    }

    @Test fun semPrecedenciaAContaSaiErradaEOPontoDePrecedenciaConserta() {
        val sem = MiniBison.compile(BisonExemplos.semPrecedencia)
        assertEquals(14L, sem.run("2*3+4").value)
        assertEquals(10L, contaCerta("2*3+4"))
        val com = MiniBison.compile(pontoDePrecedencia(BisonExemplos.semPrecedencia))
        assertEquals(0, com.conflicts.size)
        assertEquals(10L, com.run("2*3+4").value)
    }

    @Test fun tocarEmPorLeftZeraOsConflitos() = runDesktopComposeUiTest(412, 900) {
        val app = AppState(Learner(LearnerState(onboardingDone = true), Random(1))).also { it.stack.add(Screen.LabBison(BisonExemplos.semPrecedencia)) }
        setContent { CompilandiaTheme { LabBisonScreen(app, BisonExemplos.semPrecedencia) } }
        onNodeWithText("A conta certa dá 10.").assertExists()
        onNodeWithTag("lab-bison-por-left").performClick()
        waitForIdle()
        onNodeWithText("0 conflitos").assertExists()
        onNodeWithText("A conta certa dá 10.").assertDoesNotExist()
    }
}
