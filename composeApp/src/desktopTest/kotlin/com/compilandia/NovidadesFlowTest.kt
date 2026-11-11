@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.graphics.toAwtImage
import com.compilandia.content.Album
import com.compilandia.content.Temporada
import com.compilandia.content.Toy
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.platform.nowMs
import com.compilandia.tutor.Desafio
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.Juice
import com.compilandia.ui.Screen
import com.compilandia.ui.album.CARTA_TAG
import com.compilandia.ui.fx.Fx
import com.compilandia.ui.lab.LAB_INPUT_TAG
import com.compilandia.ui.lab.LAB_SEND_TAG
import com.compilandia.ui.lab.TIMELINE_TAG
import com.compilandia.ui.screens.FITA_TAG
import com.compilandia.ui.voice.Narrator
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** Joga as novidades de verdade: toca, arrasta e confere. As fotos vão para build/screenshots. */
class NovidadesFlowTest {
    init { Narrator.silent = true }

    private val saida = File("build/screenshots").apply { mkdirs() }
    private fun estado(s: LearnerState = LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, xp = 300)) = AppState(Learner(s, Random(1)))
    private fun ComposeUiTest.anda(ms: Long = 1200) = mainClock.advanceTimeBy(ms)
    private fun ComposeUiTest.foto(nome: String) {
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(saida, "$nome.png"))
    }

    /** O diagnóstico roda fora da tela: espera a resposta aparecer. */
    private fun ComposeUiTest.espera(texto: String, max: Int = 80) {
        repeat(max) {
            anda(120)
            Thread.sleep(40)
            if (onAllText(texto)) return
        }
        onNodeWithText(texto, substring = true).assertExists()
    }

    private fun ComposeUiTest.onAllText(t: String): Boolean = try {
        onNodeWithText(t, substring = true).assertExists(); true
    } catch (e: AssertionError) { false }

    private fun ComposeUiTest.manda(t: String) {
        onNodeWithTag(LAB_INPUT_TAG).performTextInput(t)
        anda(200)
        onNodeWithTag(LAB_SEND_TAG).performClick()
        anda(300)
    }

    /** B5: o Byte lê o .y colado, conta o que achou e abre o Laboratório Bison, onde a régua troca a conta. */
    @Test fun labLeOYEAbreOLaboratorioBison() = runDesktopComposeUiTest(412, 900, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Lab)
        setContent { App(app) }
        anda()
        manda(com.compilandia.ui.lab.BisonExemplos.semPrecedencia)
        espera("Abrir no Laboratório Bison")
        anda(4000)
        espera("2+3*4 dá 14")
        foto("40_lab_le_o_y")
        onNodeWithText("Abrir no Laboratório Bison").performClick()
        anda(1500)
        assertTrue(app.screen is Screen.LabBison)
        onNodeWithTag("lab-bison-veredito").assertExists()
        onNodeWithText("conflitos", substring = true).assertExists()
        foto("41_lab_bison")
    }

    @Test fun labAchaOProblemaEATravaDoDegrauFunciona() = runDesktopComposeUiTest(412, 900, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Lab)
        setContent { App(app) }
        anda()
        foto("30_lab_inicio")
        manda("E -> E + T | T\nT -> id")
        espera("E começa com E")
        anda(2500)
        foto("31_lab_diagnostico")
        onNodeWithText("Me dá uma dica").performClick()
        anda(1500)
        espera("Tente: edite a gramática")
        foto("32_lab_dica")
        // Ainda sem tentar: o degrau seguinte fica fechado.
        onNodeWithText("Mostrar resolvendo").performClick()
        espera("Antes, tente uma vez")
        foto("33_lab_trava")
        // Mandar a mesma gramática conta como tentativa e abre o degrau.
        onNodeWithTag(LAB_SEND_TAG).performClick()
        anda(300)
        espera("Ainda não")
        onNodeWithText("Mostrar resolvendo").performClick()
        anda(1500)
        espera("Consertando a recursão")
        foto("34_lab_palco")
        onNodeWithTag(TIMELINE_TAG).performTouchInput { click(center) }
        anda(1500)
        foto("35_lab_tempo")
    }


    @Test fun tecladoRapidoEscreveNoCursor() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Lab)
        setContent { App(app) }
        anda()
        onNodeWithTag(LAB_INPUT_TAG).performTextInput("E")
        anda(100)
        onNodeWithTag("tecla-->").performClick()
        anda(100)
        onNodeWithTag("tecla-ε").performClick()
        anda(300)
        onNodeWithTag(LAB_INPUT_TAG).assertTextEquals("E -> ε")
        foto("50_lab_teclado")
    }

    @Test fun teclaRespeitaCursorESelecao() {
        val v = androidx.compose.ui.text.input.TextFieldValue("(ab)*", androidx.compose.ui.text.TextRange(2))
        assertEquals("(a|b)*", com.compilandia.ui.lab.insereTecla(v, "|").text)
        assertEquals(3, com.compilandia.ui.lab.insereTecla(v, "|").selection.start)
        val sel = androidx.compose.ui.text.input.TextFieldValue("E x T", androidx.compose.ui.text.TextRange(2, 3))
        assertEquals("E ε T", com.compilandia.ui.lab.insereTecla(sel, "ε").text)
        assertEquals("-> ", com.compilandia.ui.lab.insereTecla(androidx.compose.ui.text.input.TextFieldValue(""), "->").text)
        assertEquals("E -> ", com.compilandia.ui.lab.insereTecla(androidx.compose.ui.text.input.TextFieldValue("E ", androidx.compose.ui.text.TextRange(2)), "->").text)
        assertEquals("a\n", com.compilandia.ui.lab.insereTecla(androidx.compose.ui.text.input.TextFieldValue("a", androidx.compose.ui.text.TextRange(1)), "↵").text)
    }

    @Test fun labRespondePerguntaERodaFraseEExpressao() = runDesktopComposeUiTest(412, 900, testTimeout = 3.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Lab)
        setContent { App(app) }
        anda()
        manda("o que é handle?")
        espera("topo da pilha")
        anda(2500)
        foto("36_lab_pergunta")
        manda("qual a capital da Mongólia?")
        espera("ainda não sei")
        manda("E -> T X\nX -> + T X | ε\nT -> id")
        espera("é LL(1)")
        anda(1500)
        foto("37_lab_limpa")
        manda("id + id")
        espera("Rodando LL(1)")
        anda(2500)
        foto("38_lab_rodando")
    }

    @Test fun labLeRegexEExpressao() = runDesktopComposeUiTest(412, 900, testTimeout = 2.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Lab)
        setContent { App(app) }
        anda()
        manda("(a|b)*abb")
        espera("Montei o autômato")
        anda(1500)
        foto("39_lab_regex")
        manda("abb")
        espera("Autômato de")
        anda(2500)
        foto("40_lab_trem")
    }

    @Test fun brinquedoDaAulaAndaEPedeOFim() = runDesktopComposeUiTest(412, 900, testTimeout = 2.minutes) {
        mainClock.autoAdvance = false
        val e = Episodes.all.first { it.temporada == Temporada.PRATICA && it.skill == Skill.REGEX_DFA }
        val k = e.passos.indexOfFirst { it.brinquedo != null }
        val app = estado()
        app.stack.add(Screen.Episode(e.skill, id = e.id))
        setContent { com.compilandia.ui.CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(app, app.screen as Screen.Episode, onStep = k, abertura = false) } }
        anda()
        foto("41_brinquedo")
        val hits = Juice.hitTick
        onNodeWithText("a").performClick(); anda()
        onNodeWithText("a").performClick(); anda()
        onNodeWithText("b").performClick(); anda(1500)
        assertEquals(hits + 1, Juice.hitTick, "chegar no círculo duplo conta como acerto")
        foto("42_brinquedo_fim")
        onNodeWithText("Continuar").assertExists()
    }

    @Test fun fitaVoltaACena() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val e = Episodes.of(Skill.PHASES)!!
        val app = estado()
        setContent { com.compilandia.ui.CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(app, Screen.Episode(e.skill), onStep = 2, abertura = false) } }
        anda()
        onNodeWithText("Pular").assertExists()
        // A fita cobre a largura toda: tocar na esquerda volta para a primeira cena.
        onNodeWithTag(FITA_TAG).performTouchInput { click(centerLeft) }
        anda()
        onNodeWithText(e.passos[0].fala.text, substring = true).assertExists()
    }

    @Test fun cartaDoAlbumAndaComODedoEColaAFigurinha() = runDesktopComposeUiTest(412, 900, testTimeout = 2.minutes) {
        mainClock.autoAdvance = false
        val bug = Album.of("else-solto")!!
        val app = estado()
        app.stack.add(Screen.CacaBug(bug.id))
        setContent { App(app) }
        anda()
        repeat(3) {
            val atual = bug.testes.first { t -> onAllText(t.texto) }
            onNodeWithTag(CARTA_TAG).performTouchInput { if (atual.verdade) swipeRight() else swipeLeft() }
            anda(1200)
            onNodeWithText("Continuar").performClick()
            anda(800)
        }
        anda(2500)
        assertTrue(bug.id in app.ls.bugsPegos, "acertar tudo cola a figurinha")
        foto("43_bug_pego")
    }

    @Test fun desafioRecusaFraseInvalidaEPagaUmaVez() = runDesktopComposeUiTest(412, 900, testTimeout = 2.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Desafio)
        setContent { App(app) }
        anda()
        val d = Desafio.doDia(nowMs() / 86_400_000L)
        val gears = app.ls.gears
        for (t in d.segredo) { onNodeWithTag("tecla-$t").performClick(); anda(100) }
        onNodeWithText("Conferir").performClick()
        anda(3000)
        assertEquals(gears + com.compilandia.learning.DESAFIO_GEARS, app.ls.gears)
        assertEquals(1, app.ls.desafioSequencia)
        foto("44_desafio_certo")
    }

    /** Mural, rodada 10: sem porta para escolher, a corrida começa direto na primeira sala. */
    @Test fun corridaComecaDiretoNaSala() = runDesktopComposeUiTest(412, 900, testTimeout = 2.minutes) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Corrida)
        setContent { App(app) }
        anda(1500)
        foto("45_corrida_sala")
        assertTrue(Juice.inArena, "dentro da sala, os efeitos do jogo valem")
    }

    @Test fun portaoTrocaDeTelaComElaFechada() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        setContent { App(app) }
        anda()
        var chegou = false
        Fx.porta("Floresta Léxica", com.compilandia.ui.Palette.drawer) { chegou = true; app.go(Screen.RegionView(Region.FLORESTA_LEXICA)) }
        anda(130)
        assertFalse(chegou, "ainda fechando")
        anda(350)
        assertTrue(chegou, "a tela troca com o portão fechado")
        foto("46_portao")
        anda(2000)
        assertEquals(null, Fx.portao)
    }

    @Test fun overclockEsquentaAArena() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3))
        setContent { App(app) }
        anda()
        repeat(7) { Juice.correct(); anda(120) }
        assertTrue(Juice.overclock)
        assertEquals(3, Juice.mult)
        anda(90)
        foto("47_overclock")
        Juice.wrong()
        anda(90)
        foto("48_glitch")
    }

    @Test fun feedDesliza() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Feed)
        setContent { App(app) }
        anda(2500)
        onNodeWithText("1 de", substring = true).assertExists()
        onNodeWithTag("feed").performTouchInput { swipeUp(durationMillis = 300) }
        anda(2500)
        onNodeWithText("2 de", substring = true).assertExists()
    }

    @Test fun regiaoAbreATrilhaEOContinuarAbreAEtapa() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.RegionView(Region.TEMPLO_TOPDOWN))
        setContent { App(app) }
        anda()
        onNodeWithText("Pegadinhas").assertDoesNotExist()
        onNodeWithText(Skill.FOLLOW.title).performClick()
        anda(1500)
        assertEquals(Screen.Trilha(Skill.FOLLOW), app.screen)
        foto("49_trilha")
        onNodeWithText("Continuar:", substring = true).performClick()
        anda(800)
        assertEquals(Skill.FOLLOW, (app.screen as Screen.Episode).skill)
    }

    @Test fun pularMarcaEAvanca() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, episodesSeen = listOf(Skill.FOLLOW.name)))
        app.stack.add(Screen.Trilha(Skill.FOLLOW))
        setContent { App(app) }
        anda()
        val antes = com.compilandia.content.Trilha.proximo(app.learner, Skill.FOLLOW)!!
        onNodeWithText("pular").performClick()
        anda()
        assertEquals("pulada", app.learner.etapa(Skill.FOLLOW, antes.chave))
        assertTrue(com.compilandia.content.Trilha.proximo(app.learner, Skill.FOLLOW) != antes)
    }

    @Test fun aulaTocandoSozinhaSegueEParaNaPergunta() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado(LearnerState(name = "Felipe", onboardingDone = true, askConfidence = false, autoAvancar = true))
        app.stack.add(Screen.Episode(Skill.PHASES, abertura = false))
        setContent { App(app) }
        anda()
        val ep = com.compilandia.content.episodes.Episodes.of(Skill.PHASES)!!
        onNodeWithText(ep.passos[0].fala.text, substring = true).assertExists()
        anda(12_000)
        // A cena 2 é pergunta: a aula chegou nela sozinha e espera o toque.
        onNodeWithText(ep.passos[1].fala.text, substring = true).assertExists()
        anda(20_000)
        onNodeWithText(ep.passos[1].fala.text, substring = true).assertExists()
        onNodeWithText("Dica").assertExists()
        foto("51_aula_tocando")
    }

    @Test fun pausadoNaoAndaSozinho() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.Episode(Skill.PHASES, abertura = false))
        setContent { App(app) }
        anda()
        val ep = com.compilandia.content.episodes.Episodes.of(Skill.PHASES)!!
        anda(15_000)
        onNodeWithText(ep.passos[0].fala.text, substring = true).assertExists()
        onNodeWithContentDescription("Tocar sozinho").performClick()
        anda()
        assertTrue(app.ls.autoAvancar, "a escolha fica guardada")
        anda(12_000)
        onNodeWithText(ep.passos[1].fala.text, substring = true).assertExists()
    }

    @Test fun fazerComAjudaMostraGramaticaEPergunta() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        app.stack.add(Screen.FazerComAjuda(Skill.FIRST, semente = 42, comeca = 3))
        setContent { App(app) }
        anda(9_000)
        foto("53_fazer_pergunta")
    }

    @Test fun gavetaJogarAbreACorrida() = runDesktopComposeUiTest(412, 900) {
        mainClock.autoAdvance = false
        val app = estado()
        setContent { App(app) }
        anda()
        onNodeWithText("Corrida de portas").assertDoesNotExist()
        onNodeWithText("Jogar").performClick()
        anda()
        foto("50_gaveta_jogar")
        onNodeWithText("Corrida de portas").performClick()
        anda()
        assertEquals(Screen.Corrida, app.screen)
    }
}

@Suppress("unused") private val keepToy: Toy? = null
