package com.compilandia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.compilandia.content.Etapa
import com.compilandia.content.FormatoRevisao
import com.compilandia.content.Retomadas
import com.compilandia.content.Revisao
import com.compilandia.content.Trilha
import com.compilandia.engine.Assoc
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.PanicMode
import com.compilandia.engine.PassoReacao
import com.compilandia.engine.Precedence
import com.compilandia.engine.Reacoes
import com.compilandia.engine.TokenClass
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeState
import com.compilandia.ui.games.Reacao
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** R1, R2, R4 e J8: retomada, revisão na trilha, lembrete e reação dos jogos de classificar. Capturas em build/screenshots/revisao. */
class RevisaoRenderTest {
    private val out = File("build/screenshots/revisao").apply { mkdirs() }
    private val t0 = System.currentTimeMillis() - 4 * 86_400_000L

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

    private fun voltou(): AppState {
        val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1))
        l.markEpisode(Skill.FIRST); l.markEpisode(Skill.FOLLOW)
        l.recordAttempt(Skill.FIRST, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, t0)
        l.recordAttempt(Skill.FOLLOW, GameId.FIRST_FOLLOW_ALCHEMY, 1, 0.6, null, 1000, 0, t0)
        l.marcaVisita(t0)
        return AppState(l)
    }

    @Test fun retomada() {
        val app = voltou()
        assertTrue(app.screen is Screen.Retomada, "quem volta depois de 4 dias abre na retomada")
        for ((nome, wh) in listOf("412" to (412 to 900), "360" to (360 to 760), "1200" to (1200 to 800), "deitado" to (780 to 360))) {
            shot("retomada_$nome", wh.first, wh.second) { App(voltou()) }
        }
        val l = Learner(LearnerState(onboardingDone = true), Random(1))
        assertTrue(AppState(l).screen is Screen.Room, "primeira visita vai para a sala")
        assertNotNull(Retomadas.monta(voltou().learner.also { it.marcaVisita(t0) }, System.currentTimeMillis()))
    }

    @Composable
    private fun palco(titulo: String, passos: List<PassoReacao>) {
        CompilandiaTheme {
            Column(Modifier.fillMaxSize().background(Palette.paper)) {
                Header(titulo, null, Palette.hazard)
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) { Reacao(passos) {} }
            }
        }
    }

    @Test fun reacoes() {
        AppState(Learner(LearnerState(onboardingDone = true), Random(1)))
        ThemeState.reduceMotion = true   // mostra todos os passos de uma vez
        try {
            shot("reacao_porteiro") { palco("Porteiro Regex", Reacoes.porteiro("(a|b)*abb", listOf("abb" to false, "ab" to true), listOf("babb", "ba", "aabb"))) }
            shot("reacao_esteira", 412, 1100) { palco("Esteira das Fases", Reacoes.esteira(listOf("Léxica", "Sintática", "Semântica", "Intermediário", "Otimização", "Geração"))) }
            shot("reacao_detetive") { palco("Detetive de Erros", Reacoes.detetive("int x; x = \"oi\";", 2, 1, listOf("Léxico", "Sintático", "Semântico", "Execução"))) }
            val toks = MiniLexer.tokenize("while (x < 10) x = x + 1;")
            val cls = toks.indices.associateWith { toks[it].cls }.toMutableMap().apply { this[0] = TokenClass.ID }
            shot("reacao_fatiador", 412, 1100) { palco("Fatiador de Tokens", Reacoes.fatiador(toks, cls)) }
            val g = GrammarBank.exprAmbiguous.grammar
            shot("reacao_conflito") { palco("Caça-Conflitos", Reacoes.conflito(LRTable(g, LRKind.SLR1), g)) }
            val lr = Grammar.parse("E -> E + T | T\nT -> id")
            shot("reacao_cirurgia") { palco("Cirurgia de Gramática", Reacoes.cirurgia(lr, Grammar.parse("E -> T E'\nE' -> + T E'\nT -> id"), listOf(listOf("id"), listOf("id", "+", "id")))) }
            shot("reacao_cirurgia_certa") { palco("Cirurgia de Gramática", Reacoes.cirurgia(lr, GrammarTransform.eliminateImmediateLeftRecursion(lr), listOf(listOf("id"), listOf("id", "+", "id")))) }
            val p = com.compilandia.engine.ScopeGen.random(Random(3))
            shot("reacao_escopo") { palco("Esconde-Esconde do Escopo", Reacoes.escopo(p, emptyMap())) }
            // As mesmas entradas do ReacoesTest: a + primeiro dá 20, o * primeiro dá 14.
            val frase = listOf("id", "+", "id", "*", "id")
            fun arvore(mais: Int, vezes: Int) =
                LRParser.parse(LRTable(g, LRKind.SLR1, mapOf("+" to Precedence(mais, Assoc.LEFT), "*" to Precedence(vezes, Assoc.LEFT)), resolveConflicts = true), frase).tree!!
            shot("reacao_arvores") { palco("Duas Árvores", Reacoes.duasArvores(arvore(1, 2), arvore(2, 1))) }
            val gp = Grammar.parse("P -> S ; P | ε\nS -> id = E | print E\nE -> T E'\nE' -> + T E' | ε\nT -> id | num")
            val entrada = listOf("id", "=", "=", "num", ";", "id", "=", "num", ";")
            shot("reacao_panico", 412, 1100) { palco("Sala do Pânico", Reacoes.panico(entrada, PanicMode.trace(LL1Table(gp), entrada))) }
        } finally {
            ThemeState.reduceMotion = false
        }
    }

    /** R2: trilha completa e memória vencida. O formato da vez aparece acima do Continuar e troca na revisão seguinte. */
    @Test fun revisaoNaTrilha() {
        val s = Skill.FIRST
        val agora = System.currentTimeMillis()
        val antes = agora - 60L * 86_400_000L
        fun pendente(usados: List<FormatoRevisao>): AppState {
            val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1))
            l.markEpisode(s)
            for (p in Trilha.passos(s)) l.marcaEtapa(s, p.chave, feita = true, antes)
            l.recordAttempt(s, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, antes)
            // Usados antes da última revisão: contam para o rodízio, mas não fecham a rodada de agora.
            usados.forEachIndexed { i, f -> l.usouFormato(s, f.name, antes - 10_000L + i) }
            l.marcaVisita(agora)
            return AppState(l).also { it.go(Screen.Trilha(s)) }
        }
        val formatos = Revisao.formatos(s)
        assertTrue(formatos.size >= 2, "FIRST tem mais de um formato de revisão: $formatos")
        val primeiro = Trilha.proximo(pendente(emptyList()).learner, s, agora)
        assertEquals(Etapa.REVISAO, primeiro?.etapa)
        assertEquals(formatos[0], primeiro?.formato)
        assertEquals(formatos[1], Trilha.proximo(pendente(formatos.take(1)).learner, s, agora)?.formato)
        shot("revisao_trilha_1") { App(pendente(emptyList())) }
        shot("revisao_trilha_2") { App(pendente(formatos.take(1))) }
    }

    /** Rodada 6 do mural: na região, o assunto feito e esquecido mostra "Revisar" na placa e embaixo, ao lado do Continuar. */
    @Test fun revisaoNaRegiao() {
        val agora = System.currentTimeMillis()
        val antes = agora - 60L * 86_400_000L
        val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false), Random(1))
        for (s in listOf(Skill.FIRST, Skill.FOLLOW)) {
            l.markEpisode(s)
            for (p in Trilha.passos(s)) l.marcaEtapa(s, p.chave, feita = true, antes)
            l.recordAttempt(s, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, antes)
        }
        l.marcaVisita(agora)
        assertTrue(l.skill(Skill.FIRST).memory.isDue(agora), "FIRST devia estar vencido")
        val app = AppState(l).also { it.go(Screen.RegionView(Skill.FIRST.region)) }
        shot("revisao_regiao") { App(app) }
    }

    /** R4: o quadro do lembrete como aparece no celular, desligado e ligado. No computador ele só mostra o aviso. */
    @Test fun lembreteNoCelular() {
        for (ligado in listOf(false, true)) {
            // O estado já nasce pronto: commit() gravaria o save de verdade no disco.
            val app = AppState(Learner(LearnerState(onboardingDone = true, lembreteLigado = ligado, lembreteHora = 19), Random(1)))
            shot("lembrete_celular_" + if (ligado) "ligado" else "desligado", 412, 340) {
                CompilandiaTheme {
                    Column(Modifier.fillMaxSize().background(Palette.paper).padding(12.dp)) {
                        com.compilandia.ui.screens.LembreteAjuste(app, disponivel = true)
                    }
                }
            }
        }
    }
}
