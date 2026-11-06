package com.compilandia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Mission
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.Storage
import com.compilandia.textos.tx

sealed class Screen {
    data object Onboarding : Screen()
    data object Room : Screen()
    data object Shop : Screen()
    data object Map : Screen()
    data class Episode(val skill: Skill, val returnToGame: Boolean = false, val id: String? = null, val abertura: Boolean = true) : Screen()
    /** A trilha de um assunto (A1). */
    data class Trilha(val skill: Skill) : Screen()
    /** As aulas em C e o Guia do TP numa trilha só (A5). */
    data object SeuCompilador : Screen()
    /** Fazer com ajuda (A3): quatro problemas em que o Byte ajuda cada vez menos. */
    data class FazerComAjuda(val skill: Skill, val semente: Long = 0, val comeca: Int = 0) : Screen()
    data class RegionView(val region: Region) : Screen()
    data class Lesson(
        val skill: Skill,
        val style: Style,
        val tried: Set<Style> = emptySet(),
        /** Se veio de um jogo ("Não entendi" durante a partida), volta para ele depois. */
        val returnToGame: Boolean = false,
    ) : Screen()
    data class Feynman(val skill: Skill) : Screen()
    data class Play(
        val skill: Skill,
        val game: GameId,
        val level: Int,
        val queue: List<Mission> = emptyList(),
        val seed: Long = 0,
    ) : Screen()
    data class Flashcards(val skills: List<Skill>) : Screen()
    data object Brain : Screen()
    data object Journal : Screen()
    data object Settings : Screen()
    data object Lab : Screen()
    /** Laboratório Bison: abre com o .y colado na conversa, ou com a calculadora. */
    data class LabBison(val spec: String? = null) : Screen()
    data class Exemplo(val skill: Skill) : Screen()
    data object Album : Screen()
    data class CacaBug(val id: String) : Screen()
    data object Corrida : Screen()
    data object Desafio : Screen()
    data object Feed : Screen()
    data class BossFight(val region: Region) : Screen()
    data object Guide : Screen()
    data class GuideChapter(val number: Int) : Screen()
}

/** Estado global da UI: pilha de navegação + aprendiz (persistido a cada mudança). */
class AppState(val learner: Learner) {
    var ls: LearnerState by mutableStateOf(learner.state)
        private set
    val stack = mutableStateListOf<Screen>(if (learner.state.onboardingDone) Screen.Room else Screen.Onboarding)
    var toast: String? by mutableStateOf(null)
    val screen: Screen get() = stack.last()
    /** Última compra: a sala anima a chegada dessa peça. */
    var justBought: String? by mutableStateOf(null)

    init {
        current = this
        applyPrefs()
    }

    private fun applyPrefs() {
        Sound.enabled = ls.soundOn
        com.compilandia.ui.voice.Narrator.enabled = ls.soundOn && ls.voiceOn
        ThemeState.mode = ThemeMode.entries.firstOrNull { it.name == ls.theme } ?: ThemeMode.OFICINA
        ThemeState.fontScale = ls.fontScale
        ThemeState.reduceMotion = ls.reduceMotion
    }

    fun go(s: Screen) { Sound.play(Sfx.CLICK); stack.add(s) }
    fun replace(s: Screen) { if (stack.size > 1) stack.removeAt(stack.lastIndex); stack.add(s) }
    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
    fun home() { while (stack.size > 1) stack.removeAt(stack.lastIndex); if (stack.last() != Screen.Room) stack[0] = Screen.Room }

    /** Chame depois de qualquer alteração no aprendiz. */
    fun commit() {
        val paid = learner.claimQuests(com.compilandia.platform.nowMs())
        ls = learner.state
        applyPrefs()
        if (paid > 0) say(tx("appstate_meta_dia_cumprida_1_engrenagens", paid * com.compilandia.learning.QUEST_GEARS))
        Storage.save(learner.save())
    }

    fun say(msg: String) { toast = msg }

    companion object {
        lateinit var current: AppState
    }
}
