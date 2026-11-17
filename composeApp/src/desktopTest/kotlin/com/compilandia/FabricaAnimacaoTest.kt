package com.compilandia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.compilandia.engine.Production
import com.compilandia.fabrica.Blocos
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.tutor.FabGram
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.fabgram.Desenhos
import com.compilandia.ui.fabgram.EstadoParaTeste
import com.compilandia.ui.fabgram.Pacotes
import com.compilandia.ui.fabgram.Pincel
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

/** A animação de dentro de cada máquina, quadro a quadro, e a fábrica rodando: o que a prancha mostrou ao dono. */
@OptIn(ExperimentalTestApi::class)
class FabricaAnimacaoTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots/animacao").apply { mkdirs() }
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun ComposeUiTest.anda(ms: Long) { repeat((ms / 50).toInt().coerceAtLeast(1)) { mainClock.advanceTimeBy(50); Thread.sleep(5) } }

    /** Uma regra de verdade para cada máquina, para o desenho mostrar fichas que fazem sentido. */
    private val exemplo = mapOf(
        "prensa" to ("S" to listOf("a", "S", "b")), "cofre" to ("S" to listOf("a")), "braco" to ("E" to listOf("T")),
        "armario" to ("S" to listOf("if", "E", "then", "S", "else", "S")),
    )

    /** Quadros da animação de dentro de cada máquina: o progresso do trabalho vai de 0 a 1. */
    @Test fun animacaoDeCadaMaquina() = runDesktopComposeUiTest(220, 200, testTimeout = 4.minutes) {
        mainClock.autoAdvance = false
        val alvo = mutableStateOf(Triple(Blocos.PRENSA, 0f, 0))
        setContent {
            val medidor = rememberTextMeasurer()
            val (t, f, quadro) = alvo.value
            Canvas(Modifier.size(220.dp, 200.dp).background(Pacotes.METAL.chao)) {
                val cel = 180f * density / 2.4f
                val (nt, rhs) = exemplo[t.id] ?: ("S" to emptyList())
                val h0 = if (t == Blocos.SEPARADOR) 2 else t.h
                val b = FabGram.Bloco(1, t, 0, 0, h = h0, nt = nt, prod = if (t.maquina) Production(0, nt, rhs) else null)
                val w = t.w * cel; val h = h0 * cel
                val frase = listOf("a", "a", "b", "b").mapIndexed { i, s -> if (i < (f * 5).toInt()) s else null }
                val pincel = Pincel(cel / 32f, quadro * 0.05f, false, f > 0f, medidor, pk = Pacotes.METAL,
                    progresso = if (t.maquina || t == Blocos.FONTE) f else null,
                    saida = if (t == Blocos.SEPARADOR) 1 + (quadro / 12) % 2 else null,
                    frase = if (t == Blocos.MONTADORA) frase else emptyList())
                with(Desenhos.de(t)) { pinta(pincel, b, (size.width - w) / 2, (size.height - h) / 2, w, h) }
            }
        }
        val tipos = listOf(Blocos.PRENSA, Blocos.COFRE, Blocos.BRACO, Blocos.ARMARIO, Blocos.SEPARADOR, Blocos.FONTE, Blocos.MONTADORA)
        for (t in tipos) for (q in 0 until QUADROS) {
            alvo.value = Triple(t, q / (QUADROS - 1f), q)
            anda(50)
            foto("anim_${t.id}_${q.toString().padStart(2, '0')}")
        }
    }

    /** A fábrica de a a b b rodando de verdade, um quadro a cada 1/8 de segundo, para virar animação. */
    @Test fun fabricaRodando() = runDesktopComposeUiTest(900, 760, testTimeout = 5.minutes) {
        mainClock.autoAdvance = false
        val app = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300, fabGramMissoes = 99), Random(1)))
        app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(1500)
        onNodeWithTag("fabgram-painel").performClick(); anda(300)
        EstadoParaTeste.atual!!.zoom = 1.9f
        EstadoParaTeste.atual!!.pan = androidx.compose.ui.geometry.Offset(0f, 170f)
        anda(200)
        onNodeWithText("Ligar").performClick()
        repeat(8) { anda(50); Thread.sleep(20) }
        val e = EstadoParaTeste.atual!!
        var q = 0
        while (!e.terminou && q < 260) { mainClock.advanceTimeBy(125); Thread.sleep(6); foto("rodando_${q.toString().padStart(3, '0')}"); q++ }
    }
}

private const val QUADROS = 24
