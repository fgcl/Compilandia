package com.compilandia

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.Txt
import com.compilandia.textos.tx
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Screen
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * S2: o jogo inteiro em inglês. As aulas são montadas uma vez por processo, então este teste
 * precisa de uma JVM só dele: roda pela tarefa desktopTestIngles, que liga IDIOMA=en.
 * No desktopTest comum ele não faz nada.
 */
class InglesTest {
    private val ativo = System.getenv("IDIOMA") == "en"

    init {
        if (ativo) {
            Txt.idioma = "en"
            com.compilandia.ui.voice.Narrator.silent = true
        }
    }

    private val out = File("build/screenshots/ingles").apply { if (ativo) mkdirs() }

    private fun shot(name: String?, w: Int = 412, h: Int = 900, content: @Composable () -> Unit) {
        val scene = ImageComposeScene(w * 2, h * 2, Density(2f)) { content() }
        try {
            scene.render(0)
            scene.render(500_000_000)
            val img = scene.render(1_000_000_000)
            if (name != null) File(out, "$name.png").writeBytes(img.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        } finally {
            scene.close()
            if (++cenas % 25 == 0) System.gc()
        }
    }

    private var cenas = 0

    private fun estado(vararg telas: Screen): AppState {
        val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, xp = 420, streakDays = 3), Random(1))
        val app = AppState(l)
        for (s in telas) app.stack.add(s)
        return app
    }

    /** Os nomes que nascem uma vez (assuntos, regiões) e o texto da tela saem em inglês. */
    @Test fun nomesEmIngles() {
        if (!ativo) return
        assertEquals("Continue", tx("comum_continuar"))
        assertEquals("FOLLOW set", Skill.FOLLOW.title)
        assertEquals("Lexical Forest", Region.FLORESTA_LEXICA.title)
        // O id da aula e da fala continua nascendo do português: save e áudio valem nos dois idiomas.
        val ep = Episodes.all.first { it.temporada != com.compilandia.content.Temporada.BASICO }
        assertTrue(ep.id.startsWith(ep.skill.name + "/"))
        assertTrue(ep.id.substringAfter('/').isNotBlank())
    }

    /**
     * Nenhuma fala das aulas ficou em português. Fala montada por código (com valores do motor)
     * tem de passar por tx(); se passou por fora, ela aparece aqui.
     */
    @Test fun falasDasAulasEmIngles() {
        if (!ativo) return
        val emPortugues = Txt.traduzidas("en").filter { Txt.pt(it) != Txt.em("en", it) }.map { Txt.pt(it) }.toHashSet()
        val sobras = mutableListOf<String>()
        for (e in Episodes.all + Episodes.clipes) {
            for (p in e.passos) {
                val falas = listOfNotNull(p.fala, p.ask?.hint, p.ask?.got, p.brinquedo?.dica, p.brinquedo?.acerto)
                for (f in falas) if (f.text in emPortugues) sobras += "${e.id}: ${f.text}"
            }
        }
        assertTrue(sobras.isEmpty(), "falas ainda em português (${sobras.size}):\n" + sobras.take(10).joinToString("\n"))
    }

    /** As telas principais e um passo de cada aula renderizam em inglês sem quebrar. */
    @Test fun telasEmIngles() {
        if (!ativo) return
        shot("sala") { App(estado()) }
        shot("mapa") { App(estado(Screen.Map)) }
        shot("regiao") { App(estado(Screen.RegionView(Region.TEMPLO_TOPDOWN))) }
        shot("trilha") { App(estado(Screen.Trilha(Skill.FOLLOW))) }
        shot("aula_texto") { App(estado(Screen.Lesson(Skill.FIRST, Style.VISUAL))) }
        shot("ajustes") { App(estado(Screen.Settings)) }
        shot("laboratorio") { App(estado(Screen.Lab)) }
        shot("guia") { App(estado(Screen.GuideChapter(com.compilandia.content.Library.guide.first().number))) }
        shot("fazer_com_ajuda") { App(estado(Screen.FazerComAjuda(Skill.FIRST, semente = 42))) }
        shot("jogo") { App(estado(Screen.Play(GameId.SHIFT_REDUCE_RUSH.skills.first(), GameId.SHIFT_REDUCE_RUSH, 2, seed = 3))) }
        shot("album") { App(estado(Screen.Album)) }
        shot("painel") { App(estado(Screen.Brain)) }
        for (e in Episodes.all + Episodes.clipes) {
            val slug = e.id.lowercase().replace('/', '_').filter { it.isLetterOrDigit() || it == '_' }.take(40)
            val meio = e.passos.size / 2
            for (k in listOf(0, meio).distinct()) {
                val nome = if (e.skill == Skill.FOLLOW || e.skill == Skill.TOKENS || e.skill == Skill.LL1_TABLE) "ep_${slug}_$k" else null
                try {
                    shot(nome) {
                        CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(estado(), Screen.Episode(e.skill, id = e.id), onStep = k, abertura = false) }
                    }
                } catch (t: Throwable) {
                    throw AssertionError("falhou em inglês: ${e.id}, passo $k: $t", t)
                }
            }
        }
    }

    /** O Laboratório entende perguntas escritas em inglês (palavras-chave e palavras de pergunta). */
    @Test fun laboratorioEntendeIngles() {
        if (!ativo) return
        for (q in listOf("what is a handle?", "what is FOLLOW?", "explain left recursion", "what is a shift-reduce conflict?", "how does register allocation work?")) {
            val r = com.compilandia.tutor.Respostas.busca(q)
            assertTrue(r != null, "o Laboratório não achou resposta para: $q")
        }
    }

    /**
     * Os textos do palco (caixas e linhas de registro) também saem em inglês. Acento do português
     * ou palavra como "não" e "que" num texto de palco é sinal de frase que ficou no código.
     */
    @Test fun palcoEmIngles() {
        if (!ativo) return
        val portugues = Regex("[ãõçâêô]|\\b(não|que|para|uma|com|está|então|são)\\b", RegexOption.IGNORE_CASE)
        val sobras = linkedSetOf<String>()
        for (e in Episodes.all + Episodes.clipes) for (p in e.passos) for (b in p.shot.boxes) {
            for (t in listOf(b.text) + b.rows) if (portugues.containsMatchIn(t)) sobras += "${e.id}: $t"
        }
        assertTrue(sobras.isEmpty(), "textos de palco em português (${sobras.size}):\n" + sobras.take(15).joinToString("\n"))
    }
}
