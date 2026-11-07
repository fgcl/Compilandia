package com.compilandia.learning

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.ln
import kotlin.random.Random
import com.compilandia.textos.tx

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
    /** "pt" ou "en". Vazio: o jogo decide na abertura (save antigo fica em português, jogo novo segue o aparelho). */
    val idioma: String = "",
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
    // ---- quarta reforma: trilha do assunto. Chave "ASSUNTO/ETAPA", valor "feita" ou "pulada".
    val etapas: Map<String, String> = emptyMap(),
    /** Aula animada seguindo sozinha (botão play ao lado do Continuar). */
    val autoAvancar: Boolean = false,
    // ---- quarta reforma, revisão espaçada (R1 a R4)
    /** R1: a última vez que o jogo foi usado. A retomada aparece quando a pessoa volta depois de 2 dias. */
    val ultimaVisitaMs: Long = 0,
    /** R2: quando cada formato de revisão foi usado. Chave "ASSUNTO/FORMATO". */
    val formatosRevisao: Map<String, Long> = emptyMap(),
    /** R3: memória por ideia. A ideia sem registro herda a memória do assunto. */
    val ideias: Map<String, Memory> = emptyMap(),
    /** R4: lembrete no celular. Desligado até a pessoa ligar. [lembreteHora] vai de 0 a 23. */
    val lembreteLigado: Boolean = false,
    val lembreteHora: Int = 19,
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
    fun setIdioma(v: String) { state = state.copy(idioma = v) }
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
            Quest("meta", tx("learner_meta_dia"), state.dailyXp.toFloat() / DAILY_XP_GOAL),
            Quest("revisar", tx("learner_revisar_1_assunto"), state.dailyReviews.toFloat()),
            Quest("combo", tx("learner_3_acertos_seguidos"), state.dailyBestCombo / 3f),
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

    /** Marca uma etapa da trilha. Pular fica registrado como sinal: quem pula a mesma etapa em vários assuntos acha ela chata. */
    fun marcaEtapa(s: Skill, etapa: String, feita: Boolean, nowMs: Long) {
        val k = "${s.name}/$etapa"
        if (state.etapas[k] == "feita") return
        state = state.copy(
            etapas = state.etapas + (k to if (feita) "feita" else "pulada"),
            feedback = if (feita) state.feedback else (state.feedback + Feedback(nowMs, "pulou", s.name, etapa.substringBefore(':'))).takeLast(200),
        )
    }

    fun setAutoAvancar(v: Boolean) { state = state.copy(autoAvancar = v) }

    // ---- R1 a R4: revisão espaçada

    /** Dias desde a última visita gravada; null quando é a primeira. */
    fun diasLonge(nowMs: Long): Double? = state.ultimaVisitaMs.takeIf { it > 0 }?.let { (nowMs - it) / Memory.DAY_MS }

    fun marcaVisita(nowMs: Long) { state = state.copy(ultimaVisitaMs = nowMs) }

    /** R2: o formato de revisão [formato] foi usado agora no assunto [s]. */
    fun usouFormato(s: Skill, formato: String, nowMs: Long) {
        state = state.copy(formatosRevisao = state.formatosRevisao + ("${s.name}/$formato" to nowMs))
    }

    fun formatoUsadoEm(s: Skill, formato: String): Long = state.formatosRevisao["${s.name}/$formato"] ?: 0L

    /** R3: a memória de uma ideia. Sem registro, vale a do assunto (o save antigo continua valendo). */
    fun memoriaIdeia(id: String, s: Skill): Memory = state.ideias[id] ?: skill(s).memory

    /** R3: a pessoa lembrou (ou não) de uma ideia. Só a ideia muda; o assunto inteiro fica como está. */
    fun revisaIdeia(id: String, s: Skill, score: Double, nowMs: Long) {
        val m = memoriaIdeia(id, s).review(nowMs, score)
        state = state.copy(ideias = state.ideias + (id to m))
        touchDay(nowMs)
    }

    fun setLembrete(ligado: Boolean, hora: Int = state.lembreteHora) {
        state = state.copy(lembreteLigado = ligado, lembreteHora = hora.coerceIn(0, 23))
    }

    /** A pessoa tocou num termo do glossário: guardado como sinal para a revisão por ideia. */
    fun consultouTermo(s: Skill, termo: String, nowMs: Long) {
        state = state.copy(feedback = (state.feedback + Feedback(nowMs, "glossario", s.name, termo)).takeLast(200))
    }

    fun etapa(s: Skill, etapa: String): String? = state.etapas["${s.name}/$etapa"]

    /** Etapa pulada em três assuntos ou mais vira opcional para esta pessoa: continua na trilha, mas o Continuar passa por ela. */
    fun etapaOpcional(tipo: String): Boolean =
        state.feedback.filter { it.kind == "pulou" && it.target == tipo }.map { it.skill }.distinct().size >= 3

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
                reason = tx("learner_revisao_sua_memoria_1_esta", s.title, (r * 100).toInt())
            } else if (prereqsMet(s) && !isMastered(s)) {
                score += 1.5 + (1 - st.mastery)
                reason = if (st.attempts == 0) tx("learner_novo_territorio_1", s.title) else tx("learner_fronteira_1_esta_quase_la", s.title, (st.mastery * 100).toInt())
            } else if (!prereqsMet(s)) {
                score -= 2.0
                reason = tx("learner_ainda_faltam_bases_para_1", s.title)
            } else {
                score += 0.2
                reason = tx("learner_manutencao_1", s.title)
            }
            if (s in plan) { score += 1.6; reason = tx("learner_prova_em_1_dia_reforcando", dExam, s.title) }
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
            tx("learner_chutei") to 0.0..0.4, tx("comum_acho_que_sei") to 0.4..0.8, tx("comum_tenho_certeza") to 0.8..1.01,
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
                cal.bias > 0.2 -> out += tx("learner_voce_tem_se_sentido_mais")
                cal.bias < -0.2 -> out += tx("learner_voce_sabe_mais_que_pensa")
                else -> out += tx("learner_sua_calibracao_esta_otima_voce")
            }
        }
        val due = l.dueSkills(nowMs)
        if (due.isNotEmpty()) {
            val (s, r) = due.first()
            out += tx("learner_1_esta_sendo_esquecido_memoria", s.title, (r * 100).toInt())
        }
        val styles = l.styleRanking().filter { (st.styleArms[it.first.name]?.let { a -> a.alpha + a.beta } ?: 2.0) > 3 }
        if (styles.isNotEmpty()) out += tx("learner_1_seu_estilo_explicacao_favorito", styles.first().first.emoji, styles.first().first.title)
        val bored = st.feedback.filter { it.kind == "chato" }.groupBy { it.target }.maxByOrNull { it.value.size }
        if (bored != null && bored.value.size >= 2) {
            GameId.entries.firstOrNull { it.name == bored.key }?.let { out += tx("learner_percebi_que_1_nao_te", it.title) }
        }
        val weak = Skill.entries.filter { l.skill(it).attempts >= 3 && l.mastery(it) < 0.4 }
        if (weak.isNotEmpty()) out += tx("learner_1_esta_dificil_errar_aqui", weak.first().title)
        l.aiAccuracy()?.let { out += tx("learner_minha_rede_neural_acertou_1", (it * 100).toInt()) }
        if (st.streakDays >= 2) out += tx("learner_1_dias_seguidos_estudar_pouco", st.streakDays)
        if (out.isEmpty()) out += tx("learner_jogue_algumas_missoes_eu_comeco")
        return out
    }
}

/** Normaliza texto para comparar termos-chave (sem acentos, minúsculo). */
fun normalizeForMatch(t: String): String {
    val from = tx("learner_aaaaaeeeeiiiiooooouuuucn")
    val to = "aaaaaeeeeiiiiooooouuuucn"
    return t.lowercase().map { c -> val i = from.indexOf(c); if (i >= 0) to[i] else c }.joinToString("")
}
