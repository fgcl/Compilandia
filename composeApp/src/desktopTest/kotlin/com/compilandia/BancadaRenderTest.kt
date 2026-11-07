package com.compilandia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.compilandia.content.Robo
import com.compilandia.engine.Fabrica
import com.compilandia.engine.Guindaste
import com.compilandia.engine.RoboMips
import com.compilandia.engine.RoboPilha
import com.compilandia.engine.Trilho
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.bancada.FabricaEstado
import com.compilandia.ui.bancada.FabricaJogo
import com.compilandia.ui.bancada.GuindasteEstado
import com.compilandia.ui.bancada.GuindasteJogo
import com.compilandia.ui.bancada.PilhaEstado
import com.compilandia.ui.bancada.PilhaJogo
import com.compilandia.ui.bancada.RoboMipsEstado
import com.compilandia.ui.bancada.RoboMipsJogo
import com.compilandia.ui.bancada.TrilhoEstado
import com.compilandia.ui.bancada.TrilhoJogo
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * J1, J2 e J3: renderiza os jogos de montar no começo, no meio de uma corrida que falha,
 * com a fase passada, na janela larga e no celular deitado. As capturas ficam em build/screenshots/bancada.
 */
class BancadaRenderTest {
    private val out = File("build/screenshots/bancada").apply { mkdirs() }

    init { com.compilandia.ui.voice.Narrator.silent = true }

    private fun shot(name: String, w: Int = 412, h: Int = 900, content: @Composable () -> Unit) {
        val scene = ImageComposeScene(w * 2, h * 2, Density(2f)) { content() }
        try {
            scene.render(0)
            scene.render(400_000_000)
            File(out, "$name.png").writeBytes(scene.render(800_000_000).encodeToData(EncodedImageFormat.PNG)!!.bytes)
        } finally {
            scene.close()
        }
    }

    private fun app(vararg telas: Screen): AppState {
        val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1))
        return AppState(l).also { for (s in telas) it.stack.add(s) }
    }

    @Composable
    private fun moldura(titulo: String, sub: String, conteudo: @Composable () -> Unit) {
        AppState.current = app()
        CompilandiaTheme {
            Column(Modifier.fillMaxSize().background(Palette.paper)) {
                Header(titulo, sub, Palette.region(Skill.CODEGEN_MIPS.region))
                Box(Modifier.weight(1f).fillMaxWidth()) { conteudo() }
            }
        }
    }

    private val tamanhos = listOf("412" to (412 to 900), "360" to (360 to 760), "1200" to (1200 to 800), "deitado" to (780 to 360))

    @Test fun telas() {
        shot("lista") { App(app(Screen.Bancadas)) }
        shot("mips_inicio") { App(app(Screen.Bancada(Robo.MIPS))) }
        shot("trilho_inicio") { App(app(Screen.Bancada(Robo.TRILHO))) }
        shot("trilha_regex") { App(app(Screen.Trilha(Skill.REGEX_DFA))) }
        shot("pilha_inicio") { App(app(Screen.Bancada(Robo.PILHA))) }
        shot("guindaste_inicio") { App(app(Screen.Bancada(Robo.GUINDASTE))) }
        shot("fabrica_inicio") { App(app(Screen.Bancada(Robo.FABRICA))) }
    }

    @Test fun roboMips() {
        val soma = RoboMips.fases.first { it.id == "soma" }
        // Erro de propósito: soma a com a. A bancada mostra a = 2, b = 3 dando 4.
        fun errado() = RoboMipsEstado(soma).apply {
            listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t0", "sw \$t2, x").forEach { poe(it) }
            val v = rodar(tocar = false)
            assertFalse(v.passou)
            linha.atual = quadros.indexOfFirst { it.somador != null }
        }
        for ((rotulo, wh) in tamanhos) shot("mips_falha_$rotulo", wh.first, wh.second) { moldura("Robô MIPS", soma.nome) { RoboMipsJogo(errado(), false, {}, {}) } }

        val certo = RoboMipsEstado(soma).apply {
            soma.solucao.forEach { poe(it) }
            assertTrue(rodar(tocar = false).passou)
            linha.atual = 1
        }
        shot("mips_passou") { moldura("Robô MIPS", soma.nome) { RoboMipsJogo(certo, false, {}, {}) } }

        val laco = RoboMips.fases.first { it.id == "laco" }
        val noLaco = RoboMipsEstado(laco).apply {
            laco.solucao.forEach { poe(it) }
            rodar(tocar = false)
            mostra(0, tocar = false)
            linha.atual = quadros.indexOfFirst { it.nota.startsWith("j") || it.somador?.contains("=") == true && it.linha == 2 }.coerceAtLeast(3)
        }
        shot("mips_laco") { moldura("Robô MIPS", laco.nome) { RoboMipsJogo(noLaco, true, {}, {}) } }

        val dica = RoboMipsEstado(soma).apply { poe("lw \$t0, a"); pedeDica() }
        shot("mips_dica") { moldura("Robô MIPS", soma.nome) { RoboMipsJogo(dica, false, {}, {}) } }
        val vazio = RoboMipsEstado(soma).apply { rodar() }
        assertEquals(-1, vazio.veredito?.falha ?: -1)
    }

    @Test fun trilho() {
        val f = Trilho.fases.first { it.id == "termina1" }
        // Faltou o trilho de q1 lendo 0: "10" descarrila.
        fun quase() = TrilhoEstado(f).apply {
            gira(0, '0') // q0 lendo 0 vai para q0
            gira(0, '1'); gira(0, '1') // q0 lendo 1 vai para q1
            gira(1, '1'); gira(1, '1') // q1 lendo 1 vai para q1
            alternaFinal(1)
            val v = rodar(tocar = false)
            assertFalse(v.passou)
            linha.atual = linha.total - 1
        }
        for ((rotulo, wh) in tamanhos) shot("trilho_falha_$rotulo", wh.first, wh.second) { moldura("Trilho", f.nome) { TrilhoJogo(quase(), false, {}, {}) } }

        val certo = TrilhoEstado(f).apply {
            for ((k, d) in f.solucao) repeat(d + 1) { gira(k.first, k.second) }
            for (q in f.finaisSolucao) alternaFinal(q)
            assertTrue(rodar(tocar = false).passou)
            linha.atual = 2
        }
        shot("trilho_passou") { moldura("Trilho", f.nome) { TrilhoJogo(certo, false, {}, {}) } }

        val id = Trilho.fases.first { it.id == "ident" }
        val ident = TrilhoEstado(id).apply {
            for ((k, d) in id.solucao) repeat(d + 1) { gira(k.first, k.second) }
            for (q in id.finaisSolucao) alternaFinal(q)
            rodar(tocar = false)
            mostra(palavras.indexOf("1x"), tocar = false)
            linha.atual = linha.total - 1
        }
        shot("trilho_ident_descarrila") { moldura("Trilho", id.nome) { TrilhoJogo(ident, true, {}, {}) } }

        val tres = Trilho.fases.first { it.id == "contem00" }
        shot("trilho_tres_estados") { moldura("Trilho", tres.nome) { TrilhoJogo(TrilhoEstado(tres), false, {}, {}) } }
    }

    @Test fun pilha() {
        val f = RoboPilha.fases.first { it.id == "pilha_lista" }
        // Faltou R → ε: com "id" o Byte para em M[R, $].
        fun semEps() = PilhaEstado(f).apply {
            for ((k, p) in f.solucao) if (!f.g.productions[p].isEpsilon) { gira(k.first, k.second); while (tabela[k] != p) gira(k.first, k.second) }
            assertFalse(rodar(tocar = false).passou)
            linha.atual = linha.total - 1
        }
        for ((rotulo, wh) in tamanhos) shot("pilha_falha_$rotulo", wh.first, wh.second) { moldura("Byte com a pilha", f.nome) { PilhaJogo(semEps(), false, {}, {}) } }
        val ex = RoboPilha.fases.first { it.id == "pilha_expressao" }
        val certo = PilhaEstado(ex).apply {
            for ((k, p) in ex.solucao) { gira(k.first, k.second); while (tabela[k] != p) gira(k.first, k.second) }
            assertTrue(rodar(tocar = false).passou)
            mostra(3, tocar = false)
            linha.atual = 6
        }
        shot("pilha_expressao_meio") { moldura("Byte com a pilha", ex.nome) { PilhaJogo(certo, true, {}, {}) } }
    }

    @Test fun guindaste() {
        val f = Guindaste.fases.first { it.id == "guindaste_lista" }
        fun invertido() = GuindasteEstado(f).apply {
            f.solucao.reversed().forEach { poe(it.carta) }
            assertFalse(rodar(tocar = false).passou)
            linha.atual = linha.total - 1
        }
        for ((rotulo, wh) in tamanhos) shot("guindaste_falha_$rotulo", wh.first, wh.second) { moldura("Guindaste", f.nome) { GuindasteJogo(invertido(), false, {}, {}) } }
        val vz = Guindaste.fases.first { it.id == "guindaste_vezes" }
        val certo = GuindasteEstado(vz).apply {
            vz.solucao.forEachIndexed { i, r -> poe(r.carta); r.olha?.let { o -> for (s in vz.colunas) if (s !in o) alternaOlha(i, s) } }
            assertTrue(rodar(tocar = false).passou)
            mostra(3, tocar = false)
            linha.atual = quadrosTroca(this)
        }
        shot("guindaste_vezes_troca") { moldura("Guindaste", vz.nome) { GuindasteJogo(certo, true, {}, {}) } }
    }

    private fun quadrosTroca(e: GuindasteEstado) = e.corrida!!.quadros.indexOfFirst { it.acao == Guindaste.Acao.TROCA && it.mao == 3 }.coerceAtLeast(1)

    @Test fun fabrica() {
        val f = Fabrica.fases.first { it.id == "fabrica_soma" }
        fun ambigua() = FabricaEstado(f).apply {
            alterna(0); alterna(1)
            assertFalse(rodar(tocar = false).passou)
            linha.atual = linha.total - 1
        }
        for ((rotulo, wh) in tamanhos) shot("fabrica_ambigua_$rotulo", wh.first, wh.second, ) { moldura("Fábrica de frases", f.nome) { FabricaJogo(ambigua(), false, {}, {}) } }
        val a = Fabrica.fases.first { it.id == "fabrica_anbn" }
        val meio = FabricaEstado(a).apply {
            alterna(0); alterna(1)
            assertTrue(rodar(tocar = false).passou)
            mostra(3, tocar = false)
            linha.atual = 2
        }
        shot("fabrica_anbn_meio") { moldura("Fábrica de frases", a.nome) { FabricaJogo(meio, false, {}, {}) } }
        val sobra = FabricaEstado(a).apply { alterna(0); alterna(1); alterna(4); rodar(tocar = false); linha.atual = linha.total - 1 }
        shot("fabrica_sobra") { moldura("Fábrica de frases", a.nome) { FabricaJogo(sobra, false, {}, {}) } }
    }
}
