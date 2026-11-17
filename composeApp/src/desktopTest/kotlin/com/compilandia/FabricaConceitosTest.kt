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
import com.compilandia.ui.fabgram.PacoteAtual
import com.compilandia.ui.fabgram.Pacotes
import com.compilandia.ui.fabgram.Pincel
import com.compilandia.ui.fabgram.Variantes
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

/** Fotos dos desenhos novos das máquinas (Variantes.kt), nos pacotes que o dono aprovou: metal e desenho. */
@OptIn(ExperimentalTestApi::class)
class FabricaConceitosTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots/conceitos").apply { mkdirs() }
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun ComposeUiTest.anda(ms: Long) { repeat((ms / 50).toInt().coerceAtLeast(1)) { mainClock.advanceTimeBy(50); Thread.sleep(5) } }

    /** Uma regra de verdade para cada máquina, para o desenho mostrar fichas que fazem sentido. */
    private val exemplo = mapOf(
        "prensa" to ("S" to listOf("a", "S", "b")), "cofre" to ("S" to listOf("a")), "braco" to ("E" to listOf("T")),
        "armario" to ("S" to listOf("if", "E", "then", "S", "else", "S")),
    )

    private val pacotes = listOf(Pacotes.METAL, Pacotes.DESENHO)

    @Test fun cadaMaquinaEmCadaVersao() = runDesktopComposeUiTest(300, 170, testTimeout = 4.minutes) {
        mainClock.autoAdvance = false
        val alvo = mutableStateOf(Triple(Blocos.PRENSA, "", Pacotes.METAL))
        setContent {
            val medidor = rememberTextMeasurer()
            val (t, variante, k) = alvo.value
            Row(Modifier.background(k.chao).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (ativo in listOf(false, true)) Canvas(Modifier.size(128.dp, 150.dp)) {
                    PacoteAtual.pacote = k
                    if (variante.isEmpty()) Variantes.escolhas.remove(t.id) else Variantes.escolhas[t.id] = variante
                    val cel = 150f * density / 2.3f
                    val (nt, rhs) = exemplo[t.id] ?: ("S" to emptyList())
                    val h0 = if (t == Blocos.SEPARADOR) 2 else t.h
                    val b = FabGram.Bloco(1, t, 0, 0, h = h0, nt = nt, prod = if (t.maquina) Production(0, nt, rhs) else null)
                    val w = t.w * cel; val h = h0 * cel
                    with(Desenhos.de(t)) { pinta(Pincel(cel / 32f, 0.6f, false, ativo, medidor, pk = k), b, (size.width - w) / 2, (size.height - h) / 2, w, h) }
                }
            }
        }
        for (k in pacotes) for ((tipo, vs) in Variantes.porTipo) for (v in listOf("") + vs.map { it.id }) {
            alvo.value = Triple(Blocos.porId(tipo)!!, v, k)
            anda(100)
            foto("${k.id}_${tipo}_${v.ifEmpty { "atual" }}")
        }
        Variantes.escolhas.clear(); PacoteAtual.pacote = Pacotes.METAL
    }

    @Test fun fabricaInteiraEmCadaLinha() = runDesktopComposeUiTest(900, 760, testTimeout = 4.minutes) {
        mainClock.autoAdvance = false
        val app = AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300, fabGramMissoes = 99), Random(1)))
        app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(1500)
        onNodeWithTag("fabgram-painel").performClick(); anda(300)
        EstadoParaTeste.atual!!.zoom = 1.9f
        EstadoParaTeste.atual!!.pan = androidx.compose.ui.geometry.Offset(0f, 170f)
        for (k in pacotes) for (linha in listOf(Variantes.DIAGRAMA, Variantes.INDUSTRIAL)) {
            PacoteAtual.pacote = k
            Variantes.escolhas.clear()
            for (tipo in Variantes.porTipo.keys) Variantes.escolhas[tipo] = linha
            onNodeWithText("Ligar").performClick()
            anda(2200)
            foto("cena_${k.id}_$linha")
        }
        Variantes.escolhas.clear(); PacoteAtual.pacote = Pacotes.METAL
    }
}
