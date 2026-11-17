package com.compilandia

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.Missao
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.fabgram.EstadoParaTeste
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Um jogador de verdade: joga cada missão só pela tela (tocar, arrastar, apertar Ligar),
 * seguindo o que o chão mostra, e fotografa cada passo para quem revisa olhar.
 */
@OptIn(ExperimentalTestApi::class)
class FabricaJogadorTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots/jogador").apply { mkdirs() }
    private fun ComposeUiTest.foto(nome: String) = ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    private fun ComposeUiTest.anda(ms: Long) { repeat((ms / 50).toInt().coerceAtLeast(1)) { mainClock.advanceTimeBy(50); Thread.sleep(8) } }
    private val e get() = EstadoParaTeste.atual!!

    private fun centro(c: Cel): Offset {
        val g = e.grade!!
        return Offset(g.ox + (c.x - g.x0 + 0.5f) * g.cel, g.oy + (c.y - g.y0 + 0.5f) * g.cel)
    }

    private fun ComposeUiTest.toca(c: Cel) {
        onNodeWithTag("fabgram-palco").performTouchInput { down(centro(c)); up() }
        anda(300)
    }

    /** Arrasta o dedo passando pelo centro de cada célula, como um jogador desenhando a esteira. */
    private fun ComposeUiTest.arrasta(cs: List<Cel>) {
        onNodeWithTag("fabgram-palco").performTouchInput {
            down(centro(cs.first()))
            for (c in cs.drop(1)) { moveTo(centro(c)); advanceEventTime(40) }
            up()
        }
        anda(300)
    }

    private fun ComposeUiTest.liga(): Boolean {
        onNodeWithText("Ligar").performClick()
        repeat(40) { anda(250); if (e.terminou) return@repeat }
        anda(1500)
        return e.corrida?.saiu == true
    }

    private fun estado(missoes: Int = 0, gears: Int = 0) = AppState(
        Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300, gears = gears, fabGramMissoes = missoes), Random(1)),
    )

    private fun ComposeUiTest.abre(m: Missao, app: AppState) {
        app.fabGramPedida = m.name
        if (app.stack.lastOrNull() == Screen.FabGram) { app.back(); anda(300) }
        app.go(Screen.FabGram); anda(1500)
    }

    /** As esteiras que o chão pede, na ordem, e mais um passo para dentro do bloco de destino. */
    private fun caminhoDoFantasma(): List<Cel> {
        val g = e.missao!!.guiaEsteiras.entries.toList()
        val ultimo = g.last()
        return g.map { it.key } + (ultimo.key + ultimo.value)
    }

    @Test fun jogaOTutorialInteiroPelaTela() = runDesktopComposeUiTest(412, 900, testTimeout = 6.minutes) {
        mainClock.autoAdvance = false
        val app = estado(); app.stack.add(Screen.Map)
        setContent { App(app) }
        anda(500)
        val notas = mutableListOf<String>()
        for (m in Missao.entries.filter { !it.desafio }) {
            abre(m, app)
            val n = m.ordinal + 1
            foto("${n}a_${m.name.lowercase()}_inicio")
            when (m) {
                Missao.LIGAR -> {}
                Missao.MOVER -> {
                    val (id, alvo) = e.missao!!.guiaBloco!!
                    val b = e.planta!!.blocos.first { it.id == id }
                    onNodeWithTag("fabgram-palco").performTouchInput {
                        down(centro(Cel(b.x, b.y))); moveTo(centro(Cel((b.x + alvo.x) / 2, b.y))); advanceEventTime(50); moveTo(centro(alvo)); up()
                    }
                    anda(300)
                }
                Missao.REGRA -> {
                    val maq = e.planta!!.blocos.first { it.id == e.missao!!.guiaRegra!!.first }
                    toca(Cel(maq.x, maq.y)); foto("${n}b_painel_regra")
                    onNodeWithTag("fabgram-cfg-poe-a").performClick(); anda(200); onNodeWithTag("fabgram-cfg-poe-b").performClick(); anda(200)
                    foto("${n}c_regra_montada")
                    onNodeWithText("Pronto").performClick(); anda(300)
                }
                Missao.CONSTRUIR -> {
                    val (_, onde) = e.missao!!.guiaConstruir!!
                    toca(onde); foto("${n}b_construiu")
                    onNodeWithTag("fabgram-cfg-poe-a").performClick(); anda(200); onNodeWithTag("fabgram-cfg-poe-b").performClick(); anda(200)
                    onNodeWithText("Pronto").performClick(); anda(300)
                }
                else -> arrasta(caminhoDoFantasma())
            }
            foto("${n}d_${m.name.lowercase()}_antes_de_ligar")
            val saiu = liga()
            foto("${n}e_${m.name.lowercase()}_fim")
            if (!saiu) notas += "$m: não saiu (${e.corrida?.quebra})"
        }
        File(saida, "notas.txt").writeText(notas.joinToString("\n").ifEmpty { "todas as missões saíram" })
        assertTrue(notas.isEmpty(), notas.joinToString("\n"))
    }

    /** No Mindustry, arrastar uma esteira por cima de outra que corre de lado põe uma junção no cruzamento. */
    @Test fun esteiraCruzandoEsteira() = runDesktopComposeUiTest(1200, 800, testTimeout = 4.minutes) {
        mainClock.autoAdvance = false
        val app = estado(missoes = 99); app.stack.add(Screen.FabGram)
        setContent { App(app) }
        anda(1500)
        val pl = e.planta!!
        // uma esteira horizontal longa da planta: cruza ela de cima para baixo
        val reta = pl.esteiras.entries.first { (c, d) -> d == Dir.DIREITA && pl.esteiras[c + Dir.DIREITA] == Dir.DIREITA && pl.esteiras[c - Dir.DIREITA] == Dir.DIREITA && pl.blocoEm(c + Dir.CIMA) == null && pl.blocoEm(c + Dir.BAIXO) == null && (c + Dir.CIMA) !in pl.esteiras && (c + Dir.BAIXO) !in pl.esteiras }.key
        foto("x1_antes")
        onNodeWithTag("fabgram-esteira").performClick(); anda(200)
        arrasta(listOf(Cel(reta.x, reta.y - 2), Cel(reta.x, reta.y - 1), reta, Cel(reta.x, reta.y + 1), Cel(reta.x, reta.y + 2)))
        foto("x2_cruzou")
        val depois = e.planta!!
        File(saida, "cruzamento.txt").writeText(
            "célula do cruzamento: ${depois.esteiras[reta]} bloco=${depois.blocoEm(reta)?.def}\n",
        )
        val saiu = liga()
        foto("x3_ligada")
        assertTrue(saiu, "a fábrica parou de funcionar depois de cruzar a esteira: ${e.corrida?.quebra}")
    }
}
