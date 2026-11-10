package com.compilandia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.compilandia.content.Library
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.CompilandiaTheme
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.games.GameCtx
import com.compilandia.ui.games.GameHost
import com.compilandia.ui.screens.BeatView
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.random.Random
import kotlin.test.Test

/**
 * Renderiza de verdade (Skia, sem tela) todas as telas, todos os minijogos em vários níveis
 * e todos os "beats" de todas as aulas. Qualquer exceção em composição derruba o teste.
 * As imagens vão para build/screenshots (o CI publica).
 */
open class RenderBase {
    protected val out = File("build/screenshots").apply { mkdirs() }

    init { com.compilandia.ui.voice.Narrator.silent = true }

    protected fun shot(name: String?, w: Int = 412, h: Int = 900, content: @Composable () -> Unit) {
        val scene = ImageComposeScene(w * 2, h * 2, Density(2f)) { content() }
        try {
            scene.render(0)
            // Sem foto, o teste só confere que nada quebra; o quadro do meio só importa para a imagem salva.
            if (name != null) scene.render(500_000_000)
            val img = scene.render(1_000_000_000)
            if (name != null) File(out, "$name.png").writeBytes(img.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        } finally {
            scene.close()
            // A memória nativa do Skia só volta com a coleta: sem isso, centenas de cenas estouram a RAM.
            if (++cenas % 25 == 0) System.gc()
        }
    }

    protected var cenas = 0

    protected fun stateWith(vararg screens: Screen): AppState {
        val l = Learner(LearnerState(name = "Felipe", onboardingDone = true, xp = 420, streakDays = 3), Random(1))
        val now = System.currentTimeMillis()
        repeat(6) { l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 2, if (it % 2 == 0) 1.0 else 0.5, 0.9, 30_000, 0, now - 3 * 86_400_000L) }
        l.recordAttempt(Skill.FIRST, GameId.FIRST_FOLLOW_ALCHEMY, 2, 0.3, 0.9, 60_000, 1, now)
        l.recordLessonViewed(Skill.FIRST, Style.VISUAL, true)
        val app = AppState(l)
        for (s in screens) app.stack.add(s)
        return app
    }

    @Composable
    protected fun gameFrame(game: GameId, skill: Skill, level: Int, hints: Int) {
        AppState(Learner(LearnerState(onboardingDone = true), Random(2)))
        CompilandiaTheme {
            Column(Modifier.fillMaxSize().background(Palette.paper).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GameHost(game, GameCtx(skill, level, Random(level * 100 + game.ordinal), hints) {})
            }
        }
    }

}

class RenderSmokeTest : RenderBase() {
    @Test fun screens() {
        shot("01_onboarding") { App(AppState(Learner(LearnerState(), Random(1)))) }
        shot("02_sala_inicio") { App(stateWith()) }
        shot("02_sala_cheia") {
            val st = stateWith()
            for (i in com.compilandia.learning.RoomItem.entries) { st.learner.state.let { } }
            App(AppState(Learner(LearnerState(name = "Felipe", onboardingDone = true, xp = 420, gears = 75, streakDays = 3, roomItems = com.compilandia.learning.RoomItem.entries.map { it.id }), Random(1))))
        }
        shot("02_loja") { App(stateWith(Screen.Shop)) }
        fun jogo(g: GameId) = stateWith(Screen.Play(g.skills.first(), g, 2, seed = 3)).also { it.learner.setAskConfidence(false); it.commit() }
        shot("03_jogo_arena") { App(jogo(GameId.TOKEN_SLICER)) }
        shot("03_jogo_esteira") { App(jogo(GameId.SHIFT_REDUCE_RUSH)) }
        shot("03_jogo_torre") { App(jogo(GameId.STACK_TOWER)) }
        shot("02_mapa") { App(stateWith(Screen.Map)) }
        shot("03_regiao_templo") { App(stateWith(Screen.RegionView(Region.TEMPLO_TOPDOWN))) }
        shot("04_aula_first_visual") { App(stateWith(Screen.Lesson(Skill.FIRST, Style.VISUAL))) }
        shot("05_aula_bison_historia") { App(stateWith(Screen.Lesson(Skill.YACC_BISON, Style.STORY))) }
        shot("06_feynman") { App(stateWith(Screen.Feynman(Skill.FOLLOW))) }
        shot("07_jogo_confianca") { App(stateWith(Screen.Play(Skill.TOKENS, GameId.TOKEN_SLICER, 2, seed = 3))) }
        shot("08_flashcards") { App(stateWith(Screen.Flashcards(listOf(Skill.FIRST, Skill.FOLLOW)))) }
        shot("09_painel") { App(stateWith(Screen.Brain)) }
        shot("10_diario") { App(stateWith(Screen.Journal)) }
        shot("11_laboratorio") { App(stateWith(Screen.Lab)) }
        shot("12_ajustes") { App(stateWith(Screen.Settings)) }
        shot("13_chefao") { App(stateWith(Screen.BossFight(Region.TEMPLO_TOPDOWN))) }
        shot("14_guia") { App(stateWith(Screen.Guide)) }
        if (Library.guide.isNotEmpty()) shot("15_guia_cap1") { App(stateWith(Screen.GuideChapter(Library.guide.first().number))) }
        shot("16_regiao_floresta") { App(stateWith(Screen.RegionView(Region.FLORESTA_LEXICA))) }
        shot("17_trilha_follow") { App(stateWith(Screen.Trilha(Skill.FOLLOW))) }
        shot("18_seu_compilador") { App(stateWith(Screen.SeuCompilador)) }
        shot("19_fazer_com_ajuda") { App(stateWith(Screen.FazerComAjuda(Skill.FIRST, semente = 42))) }
        shot("19_fazer_com_ajuda_tac") { App(stateWith(Screen.FazerComAjuda(Skill.TAC, semente = 42))) }
        com.compilandia.ui.GlossarioUi.aberto = com.compilandia.content.Glossario.termos.first { it.id == "follow" }
        shot("52_glossario") { App(stateWith(Screen.Episode(Skill.FOLLOW, abertura = false))) }
        com.compilandia.ui.GlossarioUi.aberto = null
        shot("20_album_vazio") { App(stateWith(Screen.Album)) }
        shot("20_album") {
            val st = stateWith(Screen.Album)
            for (e in com.compilandia.content.Album.especies.take(7)) st.learner.pegaBug(e.id, 1000L)
            st.commit()
            App(st)
        }
        shot("21_caca_bug") { App(stateWith(Screen.CacaBug("else-solto"))) }
        shot("22_corrida") { App(stateWith(Screen.Corrida)) }
        shot("23_desafio") { App(stateWith(Screen.Desafio)) }
        shot("24_feed") { App(stateWith(Screen.Feed)) }
        shot("25_outro_exemplo") { App(stateWith(Screen.Exemplo(Skill.LL1_PARSING))) }
        shot("26_abertura_bug") { App(stateWith(Screen.Episode(Skill.FOLLOW))) }
        shot("27_fabgram") { App(stateWith(Screen.FabGram)) }
        for (t in com.compilandia.ui.ThemeMode.entries) {
            val st = stateWith(Screen.Lesson(Skill.FOLLOW, Style.VISUAL))
            st.learner.setTheme(t.name); st.commit()
            shot("tema_${t.name.lowercase()}") { App(st) }
            fun themed(vararg sc: Screen) = stateWith(*sc).also { it.learner.setTheme(t.name); it.commit() }
            shot("tema_${t.name.lowercase()}_mapa") { App(themed()) }
            shot("tema_${t.name.lowercase()}_regiao") { App(themed(Screen.RegionView(Region.TEMPLO_TOPDOWN))) }
            shot("tema_${t.name.lowercase()}_ajustes") { App(themed(Screen.Settings)) }
            shot("tema_${t.name.lowercase()}_painel") { App(themed(Screen.Brain)) }
            shot("tema_${t.name.lowercase()}_jogo") { App(themed(Screen.Play(GameId.ATTRIBUTE_TREE.skills.first(), GameId.ATTRIBUTE_TREE, 2, seed = 3))) }
        }
        com.compilandia.ui.ThemeState.mode = com.compilandia.ui.ThemeMode.OFICINA
    }

    /**
     * T1: as telas principais em três larguras (360, 700 e 1200 dp) e no celular deitado.
     * As capturas ficam em build/screenshots/larguras.
     */
    @Test fun larguras() {
        val pasta = File(out, "larguras").apply { mkdirs() }
        val tamanhos = listOf("360" to (360 to 760), "412alto" to (412 to 900), "700" to (700 to 1000), "1200" to (1200 to 800), "deitado" to (780 to 360))
        val telas: List<Pair<String, () -> AppState>> = listOf(
            "sala" to { stateWith() },
            "mapa" to { stateWith(Screen.Map) },
            "regiao" to { stateWith(Screen.RegionView(Region.TEMPLO_TOPDOWN)) },
            "trilha" to { stateWith(Screen.Trilha(Skill.FOLLOW)) },
            "aula" to { stateWith(Screen.Episode(Skill.FOLLOW, abertura = false)) },
            "aula_texto" to { stateWith(Screen.Lesson(Skill.FIRST, Style.VISUAL)) },
            "jogo" to { stateWith(Screen.Play(GameId.SHIFT_REDUCE_RUSH.skills.first(), GameId.SHIFT_REDUCE_RUSH, 2, seed = 3)).also { it.learner.setAskConfidence(false); it.commit() } },
            "laboratorio" to { stateWith(Screen.Lab) },
            "ajustes" to { stateWith(Screen.Settings) },
            "fazer_com_ajuda" to { stateWith(Screen.FazerComAjuda(Skill.FIRST, semente = 42)) },
            "fabgram" to { stateWith(Screen.FabGram) },
        )
        val so = System.getenv("TELAS")?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        for ((tela, cria) in telas) {
            if (so.isNotEmpty() && tela !in so) continue
            for ((rotulo, wh) in tamanhos) {
                val st = cria()
                shot("larguras/${tela}_$rotulo", wh.first, wh.second) { App(st) }
            }
        }
        // Aula com o Byte dentro do palco: a posição dele depende da escala do palco.
        val ep = com.compilandia.content.episodes.Episodes.all.first { e -> e.passos.any { it.shot.actor != null && it.shot.camera == null } }
        val comAtor = ep.passos.indexOfFirst { it.shot.actor != null && it.shot.camera == null }
        val prof = com.compilandia.content.episodes.Episodes.all.first { e -> e.passos.any { it.fala.who == com.compilandia.content.Speaker.PROF } }
        val comProf = prof.passos.indexOfFirst { it.fala.who == com.compilandia.content.Speaker.PROF }
        if (so.isEmpty() || "aula" in so) for ((rotulo, wh) in tamanhos) {
            shot("larguras/aula_byte_no_palco_$rotulo", wh.first, wh.second) {
                CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(stateWith(), Screen.Episode(ep.skill, id = ep.id), onStep = comAtor, abertura = false) }
            }
            shot("larguras/aula_prof_$rotulo", wh.first, wh.second) {
                val st = stateWith().also { it.learner.setCaptions(true); it.commit() }
                CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(st, Screen.Episode(prof.skill, id = prof.id), onStep = comProf, abertura = false) }
            }
        }
    }

    /** Laboratório Bison: cada exemplo, cada aba, um passo no meio da conta e um .y quebrado. */
    @Test fun labBison() {
        val ex = com.compilandia.ui.lab.BisonExemplos
        val casos = listOf(
            Triple("lab_bison_passos", ex.calculadora, 0 to 6),
            Triple("lab_bison_conflitos", ex.calculadora, 1 to 0),
            Triple("lab_bison_estados", ex.calculadora, 2 to 6),
            Triple("lab_bison_sem_prec", ex.semPrecedencia, 1 to 0),
            Triple("lab_bison_potencia", ex.potencia, 0 to 10),
            Triple("lab_bison_unario", ex.menosUnario, 0 to 3),
            Triple("lab_bison_quebrado", "%token NUM\ne: e '+' ;", 0 to 0),
        )
        for ((nome, spec, abaPasso) in casos) {
            shot(nome, h = 1600) {
                val app = stateWith(Screen.LabBison(spec))
                CompilandiaTheme { com.compilandia.ui.lab.LabBisonScreen(app, spec, abaPasso.first, abaPasso.second) }
            }
        }
    }

    @Test fun premioEEsteira() {
        shot("80_premio") {
            AppState(Learner(LearnerState(onboardingDone = true), Random(2)))
            CompilandiaTheme {
                Column(Modifier.fillMaxSize().background(Palette.paper).padding(16.dp)) { com.compilandia.ui.games.RewardHero(3, 28, 12, "Bug eliminado") }
            }
        }
        shot("81_arena") {
            AppState(Learner(LearnerState(onboardingDone = true), Random(2)))
            CompilandiaTheme {
                Column(Modifier.fillMaxSize().background(Palette.paper)) { com.compilandia.ui.games.GameArena(defeated = false, escaped = false) }
            }
        }
        shot("82_maquina") {
            AppState(Learner(LearnerState(onboardingDone = true), Random(2)))
            CompilandiaTheme {
                Column(Modifier.fillMaxSize().background(Palette.paper)) { com.compilandia.ui.games.MachinePanel(3, false, "Templo Top-Down") }
            }
        }
    }

}

/** Aulas de texto, Guia do TP e todos os minijogos: classe própria para rodar ao lado das outras. */
class RenderAulasTest : RenderBase() {
    @Test fun everyGameEveryLevel() {
        val timed = setOf(GameId.PHASE_DETECTIVE, GameId.REGEX_SORTER, GameId.SHIFT_REDUCE_RUSH)
        for (g in GameId.entries) {
            if (g == GameId.FLASHCARDS) continue
            for (skill in g.skills) for (level in 1..5) {
                if (g in timed && level >= 3) continue
                val name = if (skill == g.skills.first() && level == 2) "game_${g.name.lowercase()}" else null
                try {
                    shot(name, h = 1400) { gameFrame(g, skill, level, hints = if (level == 2) 1 else 0) }
                } catch (e: Throwable) {
                    throw AssertionError("falhou ao renderizar ${g.name} (skill=${skill.name}, nível=$level): $e", e)
                }
            }
        }
    }

    @Test fun everyGuideChapter() {
        for (c in Library.guide) shot(if (c.number == 8) "guide_ch8" else null, h = 3000) {
            AppState(Learner(LearnerState(onboardingDone = true), Random(3)))
            CompilandiaTheme {
                Column(Modifier.fillMaxSize().background(Palette.paper).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    c.beats.forEachIndexed { i, b -> BeatView(b, i, mutableMapOf(), mutableMapOf()) }
                }
            }
        }
    }

    @Test fun everyLessonBeat() {
        for (lesson in Library.lessons) {
            val name = if (lesson.style == Style.VISUAL && (lesson.skill.ordinal % 4 == 0 || lesson.skill in setOf(Skill.NFA_TO_DFA, Skill.LEX_FLEX, Skill.ERROR_RECOVERY, Skill.RUNTIME, Skill.CODEGEN_MIPS))) "lesson_${lesson.skill.name.lowercase()}_visual" else null
            shot(name, h = 2400) {
                AppState(Learner(LearnerState(onboardingDone = true), Random(3)))
                CompilandiaTheme {
                    Column(Modifier.fillMaxSize().background(Palette.paper).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        lesson.beats.forEachIndexed { i, b -> BeatView(b, i, mutableMapOf(), mutableMapOf()) }
                    }
                }
            }
        }
    }

}

/**
 * Todos os passos de todos os episódios, divididos em [PARTES] classes que o Gradle roda em paralelo.
 * EPISODIOS=TABELAS,EM_C ./gradlew :composeApp:desktopTest --tests '*Episodios*' renderiza só essas temporadas
 * e salva a foto de cada passo delas. Sem o filtro, todo passo é renderizado, mas só sai foto do passo do meio
 * de cada aula e de todos os passos dos assuntos de [VITRINE]: codificar mais de mil PNGs pesava na bateria.
 */
abstract class RenderEpisodios(private val parte: Int) : RenderBase() {
    /** Renderiza todos os passos de todos os episódios: nenhum pode quebrar. */
    @Test fun everyEpisodeStep() {
        var n = -1
        // EPISODIOS=TABELAS,EM_C renderiza só essas temporadas (ou ids que contenham o texto): bom para olhar uma aula nova.
        // EPISODIOS=BASICO fotografa todos os passos das 29 aulas animadas.
        val filtro = System.getenv("EPISODIOS")?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        for (e in com.compilandia.content.episodes.Episodes.all + com.compilandia.content.episodes.Episodes.clipes) {
            if (filtro.isNotEmpty() && filtro.none { it == e.temporada.name || e.id.contains(it) }) continue
            val slug = e.id.lowercase().replace('/', '_').filter { it.isLetterOrDigit() || it == '_' }.take(40)
            e.passos.indices.forEach { k ->
                // Cada parte fotografa um passo a cada PARTES: as quatro rodam ao mesmo tempo.
                if (++n % PARTES != parte) return@forEach
                val novo = e.temporada != com.compilandia.content.Temporada.BASICO
                val todos = filtro.isNotEmpty() || (e.skill in VITRINE && !novo)
                val base = if (novo) "epn_$slug" else "ep_${e.skill.name.lowercase()}"
                val name = when {
                    todos -> "${base}_$k"
                    k == e.passos.size / 2 -> base
                    else -> null
                }
                try {
                    shot(name) {
                        val app = stateWith()
                        CompilandiaTheme { com.compilandia.ui.screens.EpisodeScreen(app, Screen.Episode(e.skill, id = e.id), onStep = k, abertura = false) }
                    }
                } catch (t: Throwable) {
                    throw AssertionError("falhou no episódio ${e.title}, passo $k: $t", t)
                }
            }
        }
    }


    companion object {
        const val PARTES = 4
        /** Assuntos com foto de todo passo mesmo sem filtro: quatro palcos diferentes (fita, pilha, autômato e árvores). */
        val VITRINE = setOf(
            com.compilandia.learning.Skill.TOKENS, com.compilandia.learning.Skill.SHIFT_REDUCE,
            com.compilandia.learning.Skill.REGEX_DFA, com.compilandia.learning.Skill.AMBIGUITY,
        )
    }
}

class EpisodiosParte0Test : RenderEpisodios(0)
class EpisodiosParte1Test : RenderEpisodios(1)
class EpisodiosParte2Test : RenderEpisodios(2)
class EpisodiosParte3Test : RenderEpisodios(3)
