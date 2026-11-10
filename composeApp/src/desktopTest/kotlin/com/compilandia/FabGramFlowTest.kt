package com.compilandia

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

/** Fábrica de Gramáticas: liga a fábrica, a caixa anda pelas máquinas e a frase se monta. */
@OptIn(ExperimentalTestApi::class)
class FabGramFlowTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots/fabgram").apply { mkdirs() }
    private fun estado() = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300), Random(1)))
    private fun ComposeUiTest.anda(ms: Long) = mainClock.advanceTimeBy(ms)
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun ComposeUiTest.espera(texto: String) {
        repeat(120) {
            anda(150); Thread.sleep(30)
            try { onNodeWithText(texto, substring = true).assertExists(); return } catch (_: AssertionError) {}
        }
        onNodeWithText(texto, substring = true).assertExists()
    }

    private fun ComposeUiTest.liga(gramatica: String?, frase: String) {
        if (gramatica != null) { onNodeWithTag("fabgram-texto").performTextClearance(); onNodeWithTag("fabgram-texto").performTextInput(gramatica); anda(600) }
        onNodeWithTag("fabgram-frase").performTextClearance(); onNodeWithTag("fabgram-frase").performTextInput(frase); anda(300)
        repeat(20) { anda(100); Thread.sleep(30) } // diagnóstico fora da tela
        onNodeWithText("Ligar").performClick()
        repeat(10) { anda(50); Thread.sleep(30) }  // corrida calculada fora da tela
    }

    @Test fun fraseCertaSaiInteira() = runDesktopComposeUiTest(1200, 800, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(800)
        liga(null, "a a b b")
        anda(1800); foto("1_andando")
        anda(2500); foto("2_mais_adiante")
        espera("A frase saiu inteira da montadora.")
        foto("3_pronta")
        assertEquals("a a b b", app.learner.state.fabGramFrase)
    }

    @Test fun fraseErradaMostraAMaisPerto() = runDesktopComposeUiTest(412, 900, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(800)
        liga(null, "a b b")
        espera("O mais perto que sai é a b.")
        foto("4_errada_celular")
    }

    @Test fun sinaisDoLaboratorioNasMaquinas() = runDesktopComposeUiTest(1200, 800, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(800)
        liga("E -> E + T | T\nT -> T * F | F\nF -> ( E ) | id\nX -> x", "id + id")
        espera("Poeira")
        espera("Esteira piscando")
        foto("5_sinais")
    }
}
