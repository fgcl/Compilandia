@file:OptIn(ExperimentalTestApi::class)

package com.compilandia

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.compilandia.content.Robo
import com.compilandia.learning.GameId
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.ui.App
import com.compilandia.ui.LISTA_MAX
import com.compilandia.ui.Screen
import kotlin.test.Test
import kotlin.test.fail

/**
 * Regra: nenhum botão fica esticado de borda a borda em monitor grande.
 *
 * Cada tela é aberta numa janela de 1920 x 1000 e todo nó clicável é medido. Um nó mais largo que [LISTA_MAX]
 * e baixo (menos de [ALTURA_DE_BOTAO]) é um botão esticado: o teste falha e diz qual tela e qual texto.
 * Nó alto e largo (a cena da sala, o fundo escuro que fecha uma gaveta) não é botão e passa.
 *
 * Para consertar: ponha o botão (ou a coluna/gaveta dele) dentro de `widthIn(max = LISTA_MAX)`,
 * `larguraDeLeitura()` ou `ColunaCentral`. Tela nova entra na lista [telas]; gaveta ou diálogo novo, em [abertos].
 */
class BotaoEsticadoTest : RenderBase() {
    private val largura = 1920
    private val altura = 1000

    private val telas: List<Pair<String, Screen?>> = listOf(
        "sala" to null, "mapa" to Screen.Map, "loja" to Screen.Shop,
        "regiao" to Screen.RegionView(Region.TEMPLO_TOPDOWN), "trilha" to Screen.Trilha(Skill.FOLLOW),
        "aula_texto" to Screen.Lesson(Skill.FIRST, Style.VISUAL),
        "jogo" to Screen.Play(GameId.SHIFT_REDUCE_RUSH.skills.first(), GameId.SHIFT_REDUCE_RUSH, 2, seed = 3),
        "jogo_tokens" to Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3),
        "cartoes" to Screen.Flashcards(listOf(Skill.FIRST)), "painel" to Screen.Brain, "diario" to Screen.Journal,
        "ajustes" to Screen.Settings, "laboratorio" to Screen.Lab, "lab_bison" to Screen.LabBison(),
        "fabgram" to Screen.FabGram, "exemplo" to Screen.Exemplo(Skill.FIRST), "album" to Screen.Album,
        "caca_bug" to Screen.CacaBug("else-solto"), "corrida" to Screen.Corrida, "desafio" to Screen.Desafio,
        "feed" to Screen.Feed, "chefao" to Screen.BossFight(Region.TEMPLO_TOPDOWN), "guia" to Screen.Guide,
        "guia_capitulo" to Screen.GuideChapter(1), "seu_compilador" to Screen.SeuCompilador,
        "bancadas" to Screen.Bancadas, "bancada" to Screen.Bancada(Robo.MIPS),
        "fazer_com_ajuda" to Screen.FazerComAjuda(Skill.FIRST, semente = 42),
        "aula" to Screen.Episode(Skill.PHASES, abertura = false), "aula_abertura_bug" to Screen.Episode(Skill.TOKENS),
    )

    /** Gavetas e diálogos que só aparecem depois de um toque: (nome, tela, texto em que tocar). */
    private val abertos: List<Triple<String, Screen?, String>> = listOf(
        Triple("sala_gaveta_jogar", null, "Jogar"),
    )

    @Test fun nenhumBotaoEsticadoEmTelaLarga() {
        val achados = mutableListOf<String>()
        for ((nome, tela) in telas) achados += mede(nome, tela, null)
        for ((nome, tela, toque) in abertos) achados += mede(nome, tela, toque)
        if (achados.isNotEmpty()) {
            val max = LISTA_MAX.value.toInt()
            val cabeca = "Botão esticado em ${largura}x$altura (> $max dp). Use widthIn(max = LISTA_MAX):"
            fail(cabeca + "\n" + achados.joinToString("\n"))
        }
    }

    private fun mede(nome: String, tela: Screen?, toque: String?): List<String> {
        val achados = mutableListOf<String>()
        runDesktopComposeUiTest(largura, altura) {
            // A sala tem animação sem fim: com o relógio automático, o teste nunca fica ocioso.
            mainClock.autoAdvance = false
            val st = if (tela == null) stateWith() else stateWith(tela)
            st.learner.setAskConfidence(false); st.commit()
            setContent { App(st) }
            mainClock.advanceTimeBy(1_500)
            if (toque != null) {
                onAllNodesWithText(toque).onFirst().performClick()
                mainClock.advanceTimeBy(1_500)
            }
            achados += esticados(nome, onAllNodes(hasClickAction()))
        }
        return achados
    }

    private fun esticados(nome: String, nos: SemanticsNodeInteractionCollection): List<String> =
        nos.fetchSemanticsNodes().mapNotNull { n ->
            val r = n.boundsInRoot
            val w = r.width / n.layoutInfo.density.density
            val h = r.height / n.layoutInfo.density.density
            if (w <= LISTA_MAX.value + 1 || h >= ALTURA_DE_BOTAO) return@mapNotNull null
            val texto = n.config.getOrNull(SemanticsProperties.Text)?.joinToString() ?: "?"
            "  $nome: \"$texto\" com ${w.toInt()} x ${h.toInt()} dp"
        }

    private companion object {
        /** Acima disso o nó clicável é área (cena, fundo de gaveta), não botão. */
        const val ALTURA_DE_BOTAO = 120f
    }
}
