package com.compilandia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.compilandia.fabrica.Blocos
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.tutor.FabGram
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.fabgram.Desenhos
import com.compilandia.ui.fabgram.Pincel
import com.compilandia.ui.fabgram.PacoteAtual
import com.compilandia.ui.fabgram.Pacotes
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** Fotos da arte nova da fábrica: cada bloco parado e trabalhando, o menu, a loja e o painel de configurar. */
@OptIn(ExperimentalTestApi::class)
class FabricaVisualTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots/fabrica").apply { mkdirs() }
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun ComposeUiTest.anda(ms: Long) { mainClock.advanceTimeBy(ms); Thread.sleep(40) }

    @Test fun arteDeCadaBloco() = runDesktopComposeUiTest(260, 150, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val tipo = androidx.compose.runtime.mutableStateOf(Blocos.todos.first())
        setContent {
            val medidor = rememberTextMeasurer()
            Row(Modifier.background(PacoteAtual.pacote.chao).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (ativo in listOf(false, true)) Canvas(Modifier.size(112.dp, 128.dp)) {
                    val cel = 128f * density / 2.2f
                    val t = tipo.value
                    val b = FabGram.Bloco(1, t, 0, 0, nt = "S")
                    val w = t.w * cel; val h = t.h * cel
                    with(Desenhos.de(t)) { pinta(Pincel(cel / 32f, 0.35f, false, ativo, medidor), b, (size.width - w) / 2, (size.height - h) / 2, w, h) }
                }
            }
        }
        for (k in Pacotes.todos) {
            PacoteAtual.pacote = k
            for (t in Blocos.todos) { tipo.value = t; anda(100); foto("bloco_${k.id}_${t.id}") }
        }
        PacoteAtual.pacote = Pacotes.METAL
    }

    private fun estado() = AppState(
        Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300, gears = 40, fabGramMissoes = 99), Random(1)),
    )

    @Test fun menuLojaEConfigurar() = runDesktopComposeUiTest(1200, 800, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(1500); anda(500)
        foto("1_livre")
        onNodeWithTag("fabgram-bloco-braco").performClick(); anda(300)
        foto("2_loja")
        onNodeWithTag("fabgram-loja").assertExists()
        onNodeWithTag("fabgram-palco").assertExists()
        onNodeWithText("Comprar por 15 ⚙").performClick(); anda(300)
        foto("2b_comprada")
        assertTrue("braco" in app.learner.state.fabGramBlocos)
        onNodeWithTag("fabgram-aba-producao").performClick(); anda(200)
        onNodeWithTag("fabgram-bloco-prensa").performClick(); anda(200)
        onNodeWithTag("fabgram-palco").performTouchInput { click(Offset(width * 0.06f, height * 0.45f)) }
        anda(500)
        foto("3_configurar")
        onNodeWithTag("fabgram-config").assertExists()
        onNodeWithTag("fabgram-cfg-poe-a").performClick(); anda(100)
        onNodeWithTag("fabgram-cfg-poe-b").performClick(); anda(100)
        foto("4_regra_montada")
    }

    /** Uma folha com todas as máquinas do pacote, no chão dele: o que a prancha mostra para escolher. */
    @Test fun folhaDeCadaPacote() = runDesktopComposeUiTest(980, 430, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val pacote = androidx.compose.runtime.mutableStateOf(Pacotes.METAL)
        val tipos = listOf(Blocos.PRENSA, Blocos.COFRE, Blocos.BRACO, Blocos.ARMARIO, Blocos.SEPARADOR, Blocos.ROTEADOR,
            Blocos.JUNCAO, Blocos.FONTE, Blocos.MONTADORA, Blocos.CESTO)
        setContent {
            val medidor = rememberTextMeasurer()
            Canvas(Modifier.size(980.dp, 430.dp)) {
                val k = pacote.value
                PacoteAtual.pacote = k
                drawRect(k.chao)
                val cel = 88f * density
                var x = 16f * density
                var y = 16f * density
                for ((i, t) in tipos.withIndex()) {
                    val w = t.w * cel; val h = t.h * cel
                    if (x + w > size.width - 8) { x = 16f * density; y += 2 * cel + 24f * density }
                    val b = FabGram.Bloco(1, t, 0, 0, nt = "S", prod = com.compilandia.engine.Production(0, "S", listOf("a")))
                    with(Desenhos.de(t)) { pinta(Pincel(cel / 32f, 0.35f, false, i % 2 == 0, medidor, pk = k), b, x, y + (2 * cel - h) / 2, w, h) }
                    x += w + 18f * density
                }
            }
        }
        for (k in Pacotes.todos) { pacote.value = k; anda(150); foto("folha_${k.id}") }
        PacoteAtual.pacote = Pacotes.METAL
    }

    /** A fábrica de a a b b rodando, com caixas e peças nas esteiras, em cada pacote. */
    @Test fun fabricaEmCadaPacote() = runDesktopComposeUiTest(900, 760, testTimeout = 4.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(1500)
        onNodeWithTag("fabgram-painel").performClick(); anda(300)
        com.compilandia.ui.fabgram.EstadoParaTeste.atual!!.zoom = 1.9f
        com.compilandia.ui.fabgram.EstadoParaTeste.atual!!.pan = androidx.compose.ui.geometry.Offset(0f, 120f)
        anda(200)
        for (k in Pacotes.todos) {
            PacoteAtual.pacote = k
            onNodeWithText("Ligar").performClick()
            repeat(40) { anda(60) }
            foto("cena_${k.id}")
        }
        PacoteAtual.pacote = Pacotes.METAL
    }
}
