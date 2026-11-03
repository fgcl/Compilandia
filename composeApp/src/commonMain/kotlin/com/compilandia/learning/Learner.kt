package com.compilandia.learning

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.ln
import kotlin.random.Random

@Serializable
data class SkillState(
    val bkt: BKT = BKT(),
    val memory: Memory = Memory(),
    val attempts: Int = 0,
    val correct: Int = 0,
    val recent: List<Double> = emptyList(),
    val lastLevel: Int = 1,
    val confused: Int = 0,
    val lessonsSeen: List<String> = emptyList(),
) {
    val mastery get() = bkt.pKnow
    val recentAccuracy get() = if (recent.isEmpty()) 0.5 else recent.average()
}

@Serializable
data class Attempt(
    val skill: String,
    val game: String,
    val level: Int,
    val score: Double,
    val confidence: Double? = null,
    val predicted: Double,
    val durationMs: Long,
    val hints: Int,
    val ts: Long,
)

@Serializable
data class JournalEntry(val ts: Long, val prompt: String, val text: String, val skill: String? = null)

@Serializable
data class Feedback(val ts: Long, val kind: String, val skill: String, val target: String)

@Serializable
data class LearnerState(
    val name: String = "",
    val skills: Map<String, SkillState> = emptyMap(),
    val styleArms: Map<String, BetaArm> = emptyMap(),
    val skillStyleArms: Map<String, BetaArm> = emptyMap(),
    val gameArms: Map<String, BetaArm> = emptyMap(),
    val net: TinyNet = TinyNet.init(),
    val history: List<Attempt> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    val feedback: List<Feedback> = emptyList(),
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastActiveDay: Long = -1,
    val onboardingDone: Boolean = false,
    val askConfidence: Boolean = true,
    val soundOn: Boolean = true,
    val bossesDefeated: List<String> = emptyList(),
    val examDay: Long = -1,              // dia (epoch day) da prova; -1 = sem prova marcada
    val guideRead: List<Int> = emptyList(),
    val theme: String = "OFICINA",
    val themeVersion: Int = 0,           // 1 = já passou pela troca de tema padrão da v4
    val fontScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val music: Boolean = false,
    // ---- reforma: voz, sala e metas do dia (saves antigos caem nos valores padrão)
    val voiceOn: Boolean = true,
    val captionsOn: Boolean = false,
    val gears: Int = 0,
    val gearsSeeded: Boolean = false,    // quem já jogava antes da sala ganha engrenagens uma vez
    val roomItems: List<String> = emptyList(),
    val episodesSeen: List<String> = emptyList(),
    val streakFreezes: Int = 0,
    val dailyDay: Long = -1,
    val dailyXp: Int = 0,
    val dailyReviews: Int = 0,
    val dailyBestCombo: Int = 0,
    val dailyClaimed: List<String> = emptyList(),
    // ---- segunda reforma: laboratório, álbum, corrida e desafio do dia
    val labUltimo: String = "",
    val bugsPegos: Map<String, Long> = emptyMap(),
    val esmagadosDia: Long = -1,
    val esmagados: Int = 0,
    val melhorCorrida: Int = 0,
    val desafioDia: Long = -1,
    val desafioPalpites: Int = 0,
    val desafioVenceu: Boolean = false,
    val desafioSequencia: Int = 0,
)

/** O próximo passo sugerido: ver a aula animada do assunto ou jogar. */
data class NextStep(val skill: Skill, val episode: Boolean, val game: GameId, val level: Int)

/** Meta do dia. Cumprida, rende [QUEST_GEARS] engrenagens uma vez. */
data class Quest(val id: String, val title: String, val progress: Float)

const val QUEST_GEARS = 10
const val DAILY_XP_GOAL = 20
const val FREEZE_PRICE = 50
const val BOSS_GEARS = 40
const val ESMAGAR_DIA = 5
const val BUG_GEARS = 15
const val DESAFIO_GEARS = 20

data class Mission(val skill: Skill, val game: GameId, val level: Int, val reason: String, val predicted: Double)

data class CalibrationStats(
    val samples: Int,
    val brier: Double,
    val bias: Double,                 // >0 = excesso de confiança; <0 = subconfiança
    val buckets: List<Triple<String, Double, Int>>, // (rótulo, taxa de acerto, n)
)

/**
 * O "cérebro" pedagógico do jogo. Combina:
 *  - BKT (o que você sabe agora),
 *  - curva de esquecimento estilo FSRS (quando revisar),
 *  - rede neural TinyNet (qual dificuldade te desafia na medida),
 *  - Thompson Sampling (qual explicação/jogo funciona melhor PARA VOCÊ),
 *  - calibração metacognitiva (você sabe o que sabe?).
 */
class Learner(initial: LearnerState = LearnerState(), private val rnd: Random = Random.Default) {
    var state: LearnerState = initial
        private set

    fun skill(s: Skill): SkillState = state.skills[s.name] ?: SkillState()
    fun mastery(s: Skill): Double = skill(s).mastery
    fun isMastered(s: Skill) = mastery(s) >= 0.95
    fun prereqsMet(s: Skill) = s.prereqs.all { mastery(it) >= 0.6 }

    fun regionProgress(r: Region): Double {
        val ss = Skill.entries.filter { it.region == r }
        return ss.sumOf { (mastery(it) - 0.1).coerceAtLeast(0.0) / 0.85 }.coerceAtMost(ss.size.toDouble()) / ss.size
    }

    // ---------- Rede neural: features ----------
    fun features(s: Skill, level: Int, confidence: Double?, nowMs: Long): DoubleArray {
        val st = skill(s)
        val pre = if (s.prereqs.isEmpty()) 1.0 else s.prereqs.map { mastery(it) }.average()
        return doubleArrayOf(
            st.mastery,
            st.memory.retrievability(nowMs),
            st.memory.difficulty / 10.0,
            ln(1.0 + st.attempts) / 4.0,
            st.recentAccuracy,
            (level - 1) / 4.0,
            confidence ?: 0.5,
            pre,
            st.confused / (st.confused + 3.0),
            1.0, // bias
        )
    }

    /** Mistura a previsão da rede com o BKT enquanto a rede ainda tem poucos dados. */
    fun predict(s: Skill, level: Int, nowMs: Long, confidence: Double? = null): Double {
        val net = state.net.predict(features(s, level, confidence, nowMs))
        val bkt = skill(s).bkt.pCorrect() * (1.15 - 0.12 * (level - 1)).coerceIn(0.3, 1.1)
        val w = (state.net.trainedSamples / 40.0).coerceIn(0.0, 0.85)
        return (w * net + (1 - w) * bkt).coerceIn(0.02, 0.98)
    }

    /** Dificuldade desejável: nível cuja chance prevista fica mais perto de 75%. */
    fun chooseLevel(s: Skill, nowMs: Long): Int =
        (1..5).minBy { abs(predict(s, it, nowMs) - 0.75) + 0.02 * abs(it - skill(s).lastLevel) }

    // ---------- Bandits ----------
    fun chooseStyle(s: Skill, exclude: Set<Style> = emptySet()): Style {
        val options = Style.entries.filter { it !in exclude }.ifEmpty { Style.entries }
        return options.maxBy { st ->
            val g = state.styleArms[st.name] ?: BetaArm()
            val l = state.skillStyleArms["${s.name}:${st.name}"] ?: BetaArm()
            0.5 * g.sample(rnd) + 0.5 * l.sample(rnd)
        }
    }

    fun chooseGame(s: Skill, exclude: Set<GameId> = emptySet()): GameId {
        val options = GameId.forSkill(s).filter { it !in exclude }.ifEmpty { GameId.forSkill(s) }
        return options.maxBy { (state.gameArms[it.name] ?: BetaArm()).sample(rnd) }
    }

    fun styleRanking(): List<Pair<Style, Double>> =
        Style.entries.map { it to (state.styleArms[it.name] ?: BetaArm()).mean() }.sortedByDescending { it.second }

    fun gameRanking(): List<Pair<GameId, Double>> =
        GameId.entries.map { it to (state.gameArms[it.name] ?: BetaArm()).mean() }.sortedByDescending { it.second }

    // ---------- Eventos ----------
    fun recordAttempt(
        s: Skill, game: GameId, level: Int, score: Double, confidence: Double?,
        durationMs: Long, hints: Int, nowMs: Long,
    ): Attempt {
        val predicted = predict(s, level, nowMs, confidence)
        val x = features(s, level, confidence, nowMs)
        val st = skill(s)
        // dicas reduzem o crédito: o objetivo é recuperar da memória, não reconhecer
        val credit = (score * (1.0 - 0.15 * hints)).coerceIn(0.0, 1.0)
        val newSt = st.copy(
            bkt = st.bkt.update(credit),
            memory = st.memory.review(nowMs, credit),
            attempts = st.attempts + 1,
            correct = st.correct + if (score >= 0.6) 1 else 0,
            recent = (st.recent + score).takeLast(8),
            lastLevel = level,
        )
        val att = Attempt(s.name, game.name, level, score, confidence, predicted, durationMs, hints, nowMs)
        val gameArm = (state.gameArms[game.name] ?: BetaArm()).reward(0.6 + 0.4 * score.coerceAtMost(1.0))
        val gained = (10 * level * (0.3 + score)).toInt() + if (confidence != null) calibrationBonus(confidence, score) else 0
        rollDay(nowMs)
        val wasDue = st.memory.isDue(nowMs)
        state = state.copy(
            skills = state.skills + (s.name to newSt),
            net = state.net.train(x, if (score >= 0.6) 1.0 else 0.0),
            history = (state.history + att).takeLast(400),
            gameArms = state.gameArms + (game.name to gameArm),
            xp = state.xp + gained,
            gears = state.gears + gearsFor(score, level),
            dailyXp = state.dailyXp + gained,
            dailyReviews = state.dailyReviews + if (wasDue) 1 else 0,
        )
        touchDay(nowMs)
        return att
    }

    /** Bônus de XP por SE CONHECER: acertar com confiança alta ou admitir dúvida e errar. */
    private fun calibrationBonus(conf: Double, score: Double): Int {
        val err = abs(conf - score)
        return when { err < 0.25 -> 5; err < 0.5 -> 2; else -> 0 }
    }

    fun recordLessonViewed(s: Skill, style: Style, understood: Boolean) {
        val key = "${s.name}:${style.name}"
        val r = if (understood) 1.0 else 0.0
        val st = skill(s)
        state = state.copy(
            styleArms = state.styleArms + (style.name to (state.styleArms[style.name] ?: BetaArm()).reward(r)),
            skillStyleArms = state.skillStyleArms + (key to (state.skillStyleArms[key] ?: BetaArm()).reward(r)),
            skills = state.skills + (s.name to st.copy(
                lessonsSeen = (st.lessonsSeen + style.name).distinct(),
                confused = st.confused + if (understood) 0 else 1,
            )),
            xp = state.xp + if (understood) 15 else 3,
        )
    }

    /** "Não entendi": penaliza o estilo atual e devolve o próximo mais promissor. */
    fun notUnderstood(s: Skill, style: Style, alreadyTried: Set<Style>, nowMs: Long): Style {
        recordLessonViewed(s, style, understood = false)
        state = state.copy(feedback = (state.feedback + Feedback(nowMs, "nao_entendi", s.name, style.name)).takeLast(200))
        return chooseStyle(s, alreadyTried + style)
    }

    /** "Achei chato" numa aula: o estilo não prendeu a atenção (sinal mais fraco que "não entendi"). */
    fun boredLesson(s: Skill, style: Style, nowMs: Long) {
        val key = "${s.name}:${style.name}"
        state = state.copy(
            styleArms = state.styleArms + (style.name to (state.styleArms[style.name] ?: BetaArm()).reward(0.25)),
            skillStyleArms = state.skillStyleArms + (key to (state.skillStyleArms[key] ?: BetaArm()).reward(0.25)),
            feedback = (state.feedback + Feedback(nowMs, "aula_chata", s.name, style.name)).takeLast(200),
        )
    }

    /** "Achei chato": penaliza o jogo e escolhe outra mecânica para a MESMA habilidade. */
    fun bored(s: Skill, game: GameId, nowMs: Long): GameId {
        val arm = (state.gameArms[game.name] ?: BetaArm()).reward(0.0)
        state = state.copy(
            gameArms = state.gameArms + (game.name to arm),
            feedback = (state.feedback + Feedback(nowMs, "chato", s.name, game.name)).takeLast(200),
        )
        return chooseGame(s, setOf(game))
    }

    fun addJournal(prompt: String, text: String, s: Skill?, nowMs: Long) {
        state = state.copy(
            journal = (state.journal + JournalEntry(nowMs, prompt, text, s?.name)).takeLast(200),
            xp = state.xp + 20,
        )
    }

    fun setName(n: String) { state = state.copy(name = n) }
    fun defeatBoss(r: Region) { state = state.copy(bossesDefeated = (state.bossesDefeated + r.name).distinct(), xp = state.xp + 150, gears = state.gears + BOSS_GEARS) }
    fun setExamDay(day: Long) { state = state.copy(examDay = day) }
    fun markGuideRead(n: Int) { state = state.copy(guideRead = (state.guideRead + n).distinct(), xp = state.xp + if (n in state.guideRead) 0 else 25) }
    /** Apaga o progresso, mantendo nome e preferências. */
    fun reset() {
        state = LearnerState(name = state.name, onboardingDone = true, askConfidence = state.askConfidence, soundOn = state.soundOn,
            theme = state.theme, themeVersion = state.themeVersion, fontScale = state.fontScale, reduceMotion = state.reduceMotion)
    }

    /** Dias até a prova (null se não marcada ou já passou). */
    fun daysToExam(nowMs: Long): Long? {
        if (state.examDay < 0) return null
        val d = state.examDay - nowMs / 86_400_000L
        return if (d < 0) null else d
    }

    /**
     * Plano da prova: habilidades mais fracas (domínio × memória) primeiro.
     * Quanto mais perto da prova, mais revisão e menos assunto novo.
     */
    fun examPlan(nowMs: Long): List<Skill> =
        Skill.entries.sortedBy { mastery(it) * 0.6 + skill(it).memory.retrievability(nowMs) * 0.4 }.take(5)
    fun finishOnboarding() { state = state.copy(onboardingDone = true) }
    fun setAskConfidence(v: Boolean) { state = state.copy(askConfidence = v) }
    fun setSound(v: Boolean) { state = state.copy(soundOn = v) }
    fun setVoice(v: Boolean) { state = state.copy(voiceOn = v) }
    fun setCaptions(v: Boolean) { state = state.copy(captionsOn = v) }
    fun setTheme(t: String) { state = state.copy(theme = t, themeVersion = 1) }
    fun setFontScale(f: Float) { state = state.copy(fontScale = f) }
    fun setReduceMotion(v: Boolean) { state = state.copy(reduceMotion = v) }

    private fun touchDay(nowMs: Long) {
        val day = nowMs / 86_400_000L
        val missed = day - state.lastActiveDay - 1
        // Dias perdidos consomem travas. Sem trava suficiente, a sequência recomeça.
        val saved = state.lastActiveDay >= 0 && missed in 1..state.streakFreezes.toLong()
        val streak = when {
            day == state.lastActiveDay -> state.streakDays
            missed == 0L || saved -> state.streakDays + 1
            else -> 1
        }
        state = state.copy(
            lastActiveDay = day, streakDays = streak,
            streakFreezes = if (saved) state.streakFreezes - missed.toInt() else state.streakFreezes,
        )
    }

    private fun rollDay(nowMs: Long) {
        val day = nowMs / 86_400_000L
        if (state.dailyDay != day) {
            state = state.copy(dailyDay = day, dailyXp = 0, dailyReviews = 0, dailyBestCombo = 0, dailyClaimed = emptyList())
        }
    }

    // ---------- Sala, engrenagens e metas ----------
    fun gearsFor(score: Double, level: Int): Int = 3 + (score * 9).toInt() + level

    fun noteCombo(combo: Int, nowMs: Long) {
        rollDay(nowMs)
        if (combo > state.dailyBestCombo) state = state.copy(dailyBestCombo = combo)
    }

    fun quests(nowMs: Long): List<Quest> {
        rollDay(nowMs)
        return listOf(
            Quest("meta", "Meta do dia", state.dailyXp.toFloat() / DAILY_XP_GOAL),
            Quest("revisar", "Revisar 1 assunto", state.dailyReviews.toFloat()),
            Quest("combo", "3 acertos seguidos", state.dailyBestCombo / 3f),
        ).map { it.copy(progress = it.progress.coerceIn(0f, 1f)) }
    }

    /** Paga as metas recém-cumpridas e devolve quantas foram. */
    fun claimQuests(nowMs: Long): Int {
        val done = quests(nowMs).filter { it.progress >= 1f && it.id !in state.dailyClaimed }
        if (done.isEmpty()) return 0
        state = state.copy(gears = state.gears + done.size * QUEST_GEARS, dailyClaimed = state.dailyClaimed + done.map { it.id })
        return done.size
    }

    fun owns(item: RoomItem) = item.id in state.roomItems

    fun buy(item: RoomItem): Boolean {
        if (owns(item) || state.gears < item.price) return false
        state = state.copy(gears = state.gears - item.price, roomItems = state.roomItems + item.id)
        return true
    }

    fun buyFreeze(): Boolean {
        if (state.gears < FREEZE_PRICE) return false
        state = state.copy(gears = state.gears - FREEZE_PRICE, streakFreezes = state.streakFreezes + 1)
        return true
    }

    /** A primeira peça que dá para comprar agora: é o prêmio mostrado no fim da partida. */
    fun affordableItem(): RoomItem? = RoomItem.entries.firstOrNull { !owns(it) && state.gears >= it.price }

    /** Devolve true na primeira vez que a aula animada termina. */
    fun markEpisode(s: Skill): Boolean {
        if (s.name in state.episodesSeen) return false
        state = state.copy(episodesSeen = state.episodesSeen + s.name, xp = state.xp + 15, gears = state.gears + 5)
        return true
    }

    /** Aula de temporada nova: mesmo prêmio do básico, uma vez só. */
    fun markEpisodeId(id: String): Boolean {
        if (id in state.episodesSeen) return false
        state = state.copy(episodesSeen = state.episodesSeen + id, xp = state.xp + 15, gears = state.gears + 5)
        return true
    }

    fun setLabUltimo(t: String) { state = state.copy(labUltimo = t) }
    fun addXp(n: Int) { state = state.copy(xp = state.xp + n) }

    /** A fala que a pessoa disse que não entendeu. É o sensor de texto ruim que nenhum índice substitui. */
    fun naoEntendiFala(s: Skill, falaId: String, nowMs: Long) {
        state = state.copy(feedback = (state.feedback + Feedback(nowMs, "fala_confusa", s.name, falaId)).takeLast(200))
    }

    /** Bônus do overclock: o multiplicador da melhor sequência vira engrenagem. */
    fun bonusOverclock(g: Int) { if (g > 0) state = state.copy(gears = state.gears + g) }

    /** O Bug da sala rende uma engrenagem, até [ESMAGAR_DIA] por dia. Devolve quanto pagou. */
    fun esmaga(nowMs: Long): Int {
        val dia = nowMs / 86_400_000L
        val feitos = if (state.esmagadosDia == dia) state.esmagados else 0
        val paga = if (feitos < ESMAGAR_DIA) 1 else 0
        state = state.copy(esmagadosDia = dia, esmagados = feitos + 1, gears = state.gears + paga)
        return paga
    }

    /** Colou a figurinha. Só paga na primeira vez. */
    fun pegaBug(id: String, nowMs: Long): Boolean {
        val novo = id !in state.bugsPegos
        state = state.copy(bugsPegos = state.bugsPegos + (id to nowMs), gears = state.gears + if (novo) BUG_GEARS else 0, xp = state.xp + if (novo) 20 else 5)
        return novo
    }

    fun fimCorrida(salas: Int, engrenagens: Int) {
        state = state.copy(melhorCorrida = maxOf(state.melhorCorrida, salas), gears = state.gears + engrenagens, xp = state.xp + salas * 8)
    }

    fun fimDesafio(dia: Long, palpites: Int, venceu: Boolean) {
        val emDia = state.desafioDia == dia - 1 && state.desafioVenceu
        state = state.copy(
            desafioDia = dia, desafioPalpites = palpites, desafioVenceu = venceu,
            desafioSequencia = if (venceu) (if (emDia) state.desafioSequencia + 1 else 1) else 0,
            gears = state.gears + if (venceu) DESAFIO_GEARS else 0, xp = state.xp + if (venceu) 25 else 5,
        )
    }

    fun nextStep(nowMs: Long): NextStep {
        val m = nextMission(nowMs)
        return NextStep(m.skill, m.skill.name !in state.episodesSeen, m.game, m.level)
    }

    // ---------- Recomendação ----------
    /**
     * Escolhe a próxima missão misturando três forças:
     *  1. revisão espaçada (memória abaixo de 90%),
     *  2. fronteira do conhecimento (pré-requisitos ok, domínio baixo),
     *  3. intercalação (evita repetir a mesma habilidade em sequência).
     */
    fun nextMission(nowMs: Long, avoid: Set<Skill> = emptySet()): Mission {
        val recentSkills = state.history.takeLast(3).map { it.skill }
        val dExam = daysToExam(nowMs)
        val plan = if (dExam != null && dExam <= 10) examPlan(nowMs).toSet() else emptySet()
        val scored = Skill.entries.filter { it !in avoid }.map { s ->
            val st = skill(s)
            val r = st.memory.retrievability(nowMs)
            var score = 0.0
            var reason: String
            if (st.memory.isDue(nowMs)) {
                score += 2.0 + (0.9 - r) * 3
                reason = "Revisão: sua memória de \"${s.title}\" está em ${(r * 100).toInt()}% — revisar agora fixa mais"
            } else if (prereqsMet(s) && !isMastered(s)) {
                score += 1.5 + (1 - st.mastery)
                reason = if (st.attempts == 0) "Novo território: \"${s.title}\"" else "Fronteira: \"${s.title}\" está quase lá (${(st.mastery * 100).toInt()}%)"
            } else if (!prereqsMet(s)) {
                score -= 2.0
                reason = "Ainda faltam bases para \"${s.title}\""
            } else {
                score += 0.2
                reason = "Manutenção de \"${s.title}\""
            }
            if (s in plan) { score += 1.6; reason = "📅 Prova em $dExam dia(s): reforçando \"${s.title}\", um dos seus pontos mais fracos" }
            if (s.name in recentSkills) score -= 1.2
            score += s.ordinal * -0.03 // leve preferência pela ordem do curso
            score += rnd.nextDouble() * 0.3
            Triple(s, score, reason)
        }
        val (s, _, reason) = scored.maxBy { it.second }
        val level = chooseLevel(s, nowMs)
        return Mission(s, chooseGame(s), level, reason, predict(s, level, nowMs))
    }

    /** Treino intercalado: N missões de habilidades diferentes. */
    fun dailyMix(nowMs: Long, n: Int = 4): List<Mission> {
        val out = mutableListOf<Mission>()
        val used = mutableSetOf<Skill>()
        repeat(n) {
            val m = nextMission(nowMs, used)
            out += m; used += m.skill
        }
        return out
    }

    fun dueSkills(nowMs: Long): List<Pair<Skill, Double>> =
        Skill.entries.filter { skill(it).memory.isDue(nowMs) }.map { it to skill(it).memory.retrievability(nowMs) }.sortedBy { it.second }

    // ---------- Metacognição ----------
    fun calibration(): CalibrationStats {
        val xs = state.history.filter { it.confidence != null }
        if (xs.isEmpty()) return CalibrationStats(0, 0.0, 0.0, emptyList())
        val brier = xs.map { val c = it.confidence!!; val y = if (it.score >= 0.6) 1.0 else 0.0; (c - y) * (c - y) }.average()
        val bias = xs.map { it.confidence!! }.average() - xs.map { if (it.score >= 0.6) 1.0 else 0.0 }.average()
        val buckets = listOf(
            "Chutei" to 0.0..0.4, "Acho que sei" to 0.4..0.8, "Tenho certeza" to 0.8..1.01,
        ).map { (label, range) ->
            val b = xs.filter { it.confidence!! >= range.start && it.confidence < range.endInclusive }
            Triple(label, if (b.isEmpty()) 0.0 else b.count { it.score >= 0.6 }.toDouble() / b.size, b.size)
        }
        return CalibrationStats(xs.size, brier, bias, buckets)
    }

    /** Precisão da IA: quão bem a rede neural tem previsto seus resultados recentes. */
    fun aiAccuracy(): Double? {
        val xs = state.history.takeLast(30)
        if (xs.size < 5) return null
        return xs.count { (it.predicted >= 0.5) == (it.score >= 0.6) }.toDouble() / xs.size
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        /** Saves antigos sempre têm um tema gravado: CLARO era o padrão e ESCURO virou OFICINA. */
        private fun migrate(s: LearnerState): LearnerState {
            val themed = if (s.themeVersion == 0 && (s.theme == "CLARO" || s.theme == "ESCURO")) s.copy(theme = "OFICINA", themeVersion = 1) else s
            return if (themed.gearsSeeded) themed else themed.copy(gearsSeeded = true, gears = themed.gears + themed.xp / 8)
        }

        fun load(text: String?): Learner = try {
            // Jogo novo já nasce acertado: as engrenagens de boas-vindas são só para save antigo.
            if (text.isNullOrBlank()) Learner(LearnerState(gearsSeeded = true)) else Learner(migrate(json.decodeFromString(LearnerState.serializer(), text)))
        } catch (e: Exception) {
            Learner()
        }
    }

    fun save(): String = json.encodeToString(LearnerState.serializer(), state)
}

/** Treinador: transforma os números do modelo em conselhos em português. */
object Coach {
    fun insights(l: Learner, nowMs: Long): List<String> {
        val out = mutableListOf<String>()
        val st = l.state
        val cal = l.calibration()
        if (cal.samples >= 5) {
            when {
                cal.bias > 0.2 -> out += "🎭 Você tem se sentido mais confiante do que seus resultados mostram (ilusão de competência). Antes de responder, tente explicar o PORQUÊ em voz alta."
                cal.bias < -0.2 -> out += "🌱 Você sabe MAIS do que pensa! Sua confiança está abaixo do seu desempenho. Confie mais no seu raciocínio."
                else -> out += "🎯 Sua calibração está ótima: você sabe o que sabe. Isso é metacognição de verdade."
            }
        }
        val due = l.dueSkills(nowMs)
        if (due.isNotEmpty()) {
            val (s, r) = due.first()
            out += "⏰ \"${s.title}\" está sendo esquecido (memória ~${(r * 100).toInt()}%). Uma revisão rápida agora vale mais que uma hora na véspera da prova."
        }
        val styles = l.styleRanking().filter { (st.styleArms[it.first.name]?.let { a -> a.alpha + a.beta } ?: 2.0) > 3 }
        if (styles.isNotEmpty()) out += "${styles.first().first.emoji} Seu estilo de explicação favorito até agora: ${styles.first().first.title}. Vou priorizá-lo — mas misturar estilos também ajuda a memória."
        val bored = st.feedback.filter { it.kind == "chato" }.groupBy { it.target }.maxByOrNull { it.value.size }
        if (bored != null && bored.value.size >= 2) {
            GameId.entries.firstOrNull { it.name == bored.key }?.let { out += "😴 Percebi que \"${it.title}\" não te empolga. Vou mostrar menos." }
        }
        val weak = Skill.entries.filter { l.skill(it).attempts >= 3 && l.mastery(it) < 0.4 }
        if (weak.isNotEmpty()) out += "🧗 \"${weak.first().title}\" está difícil. É normal: é aí que o cérebro mais cresce. Tente a aula em outro estilo e depois um nível mais fácil."
        l.aiAccuracy()?.let { out += "🤖 Minha rede neural acertou ${(it * 100).toInt()}% das previsões sobre você nas últimas tentativas." }
        if (st.streakDays >= 2) out += "🔥 ${st.streakDays} dias seguidos! Constância vence intensidade."
        if (out.isEmpty()) out += "👋 Jogue algumas missões e eu começo a entender como VOCÊ aprende melhor."
        return out
    }
}

/** Normaliza texto para comparar termos-chave (sem acentos, minúsculo). */
fun normalizeForMatch(t: String): String {
    val from = "áàâãäéèêëíìîïóòôõöúùûüçñ"
    val to = "aaaaaeeeeiiiiooooouuuucn"
    return t.lowercase().map { c -> val i = from.indexOf(c); if (i >= 0) to[i] else c }.joinToString("")
}
