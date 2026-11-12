package com.compilandia

import com.compilandia.content.Robo
import com.compilandia.learning.GameId
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.ui.App
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Screen
import java.io.File
import kotlin.test.Test

/**
 * O site usa a janela toda: num monitor de 1920 px a tela pensada para celular esticava texto e botões
 * de borda a borda. Aqui cada tela é desenhada num monitor grande e num notebook, e a imagem vai para build/screenshots/larga.
 */
class TelaLargaTest : RenderBase() {
    /** Monitor grande e notebook comum. */
    private val tamanhos = listOf("1920" to (1920 to 1000), "1366" to (1366 to 720))

    @Test fun todasAsTelasEmJanelaLarga() {
        File(out, "larga").mkdirs()
        for ((rotulo, wh) in tamanhos) fotografa(rotulo, wh.first, wh.second)
    }

    private fun fotografa(rotulo: String, w: Int, h: Int) {
        val jogo = GameId.SHIFT_REDUCE_RUSH
        val telas: List<Pair<String, Screen?>> = listOf(
            "sala" to null, "mapa" to Screen.Map, "loja" to Screen.Shop, "regiao" to Screen.RegionView(Region.TEMPLO_TOPDOWN),
            "trilha" to Screen.Trilha(Skill.FOLLOW), "aula_texto" to Screen.Lesson(Skill.FIRST, Style.VISUAL),
            "jogo" to Screen.Play(jogo.skills.first(), jogo, 2, seed = 3), "jogo_tokens" to Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3),
            "cartoes" to Screen.Flashcards(listOf(Skill.FIRST)), "painel" to Screen.Brain, "diario" to Screen.Journal,
            "ajustes" to Screen.Settings, "laboratorio" to Screen.Lab, "lab_bison" to Screen.LabBison(), "fabgram" to Screen.FabGram,
            "exemplo" to Screen.Exemplo(Skill.FIRST), "album" to Screen.Album, "caca_bug" to Screen.CacaBug("else-solto"),
            "corrida" to Screen.Corrida, "desafio" to Screen.Desafio, "feed" to Screen.Feed, "chefao" to Screen.BossFight(Region.TEMPLO_TOPDOWN),
            "guia" to Screen.Guide, "guia_capitulo" to Screen.GuideChapter(1), "seu_compilador" to Screen.SeuCompilador,
            "bancadas" to Screen.Bancadas, "bancada" to Screen.Bancada(Robo.MIPS), "fazer_com_ajuda" to Screen.FazerComAjuda(Skill.FIRST, semente = 42),
        )
        for ((nome, tela) in telas) {
            val st = if (tela == null) stateWith() else stateWith(tela)
            st.learner.setAskConfidence(false); st.commit()
            shot("larga/${nome}_$rotulo", w, h) { App(st) }
        }
        val aula = Screen.Episode(Skill.PHASES, abertura = false)
        shot("larga/aula_$rotulo", w, h) { CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(stateWith(), aula, abertura = false) } }
        shot("larga/aula_fim_$rotulo", w, h) { CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(stateWith(), aula, abertura = false, noFim = true) } }
        shot("larga/aula_abertura_bug_$rotulo", w, h) { CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(stateWith(), Screen.Episode(Skill.TOKENS)) } }
    }
}
