package com.compilandia.ui.screens

import com.compilandia.ui.larguraDeLeitura
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Beat
import com.compilandia.content.Lesson
import com.compilandia.content.Library
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.SpeechBubble
import com.compilandia.ui.VSpace
import com.compilandia.ui.visuals.VisualBeat
import com.compilandia.textos.tx

@Composable
fun LessonScreen(app: AppState, s: Screen.Lesson) {
    val lesson: Lesson? = remember(s) {
        Library.lesson(s.skill, s.style) ?: Library.lessons.firstOrNull { it.skill == s.skill && it.style !in s.tried }
    }
    val color = Palette.region(s.skill.region)
    if (lesson == null) {
        Column(Modifier.fillMaxSize()) {
            Header(s.skill.title, tx("lessonscreen_aula"), color)
            CenterText(tx("lessonscreen_ainda_nao_ha_aula_para"))
        }
        return
    }
    val beats = lesson.beats
    var shown by remember { mutableIntStateOf(1) }
    val answered = remember { mutableStateMapOf<Int, Int>() }   // índice do beat Predict -> opção escolhida
    val revealed = remember { mutableStateMapOf<Int, Boolean>() }
    val scroll = rememberScrollState()
    LaunchedEffect(shown) { scroll.animateScrollTo(scroll.maxValue) }
    val blocking = beats.take(shown).withIndex().any { (i, b) -> (b is Beat.Predict && i !in answered) || (b is Beat.Reveal && revealed[i] != true) }
    val finished = shown >= beats.size && !blocking

    fun notUnderstood() {
        val next = app.learner.notUnderstood(s.skill, lesson.style, s.tried, nowMs())
        app.commit()
        Sound.play(Sfx.WHOOSH)
        app.say(tx("lessonscreen_sem_problema_vamos_outro_jeito", next.emoji, next.title))
        app.replace(s.copy(style = next, tried = s.tried + lesson.style))
    }

    Column(Modifier.fillMaxSize()) {
        Header("${lesson.style.emoji} ${lesson.title}", "${s.skill.title} · ${lesson.style.title}", color)
        ProgressBar(shown.toFloat() / beats.size, color, Modifier.fillMaxWidth(), 6.dp)
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(16.dp).larguraDeLeitura(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // A6: a aula de texto também anima. O palco repassa as cenas da aula animada do assunto, sem som.
            PalcoDaAula(s.skill)
            beats.take(shown).forEachIndexed { i, b ->
                AnimatedVisibility(true, enter = fadeIn() + expandVertically()) {
                    BeatView(b, i, answered, revealed)
                }
            }
            if (finished) {
                PaperCard(color = Palette.yellow.copy(alpha = 0.2f)) {
                    Text(tx("lessonscreen_ai_como_foi"), style = MaterialTheme.typography.titleMedium)
                    Text(tx("lessonscreen_seja_sincero_eu_uso_isso"), style = MaterialTheme.typography.bodySmall)
                    BigButton(tx("lessonscreen_entendi_quero_explicar_com_minhas"), Modifier.fillMaxWidth(), color = Palette.green) {
                        app.learner.recordLessonViewed(s.skill, lesson.style, true)
                        app.commit()
                        Juice.correct()
                        if (s.returnToGame) app.back() else app.replace(Screen.Feynman(s.skill))
                    }
                    BigButton(tx("lessonscreen_entendi_quero_praticar_direto"), Modifier.fillMaxWidth(), color = Palette.orange) {
                        app.learner.recordLessonViewed(s.skill, lesson.style, true)
                        app.commit()
                        if (s.returnToGame) app.back() else
                            app.replace(Screen.Play(s.skill, app.learner.chooseGame(s.skill), app.learner.chooseLevel(s.skill, nowMs()), seed = nowMs()))
                    }
                    BigButton(tx("lessonscreen_nao_entendi_explica_outro_jeito"), Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink) { notUnderstood() }
                }
            }
            VSpace(80)
        }
        Row(
            Modifier.fillMaxWidth().background(Palette.paperDark).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            BigButton(tx("lessonscreen_nao_entendi"), small = true, color = Palette.white, textColor = Palette.ink) { notUnderstood() }
            BigButton(tx("lessonscreen_chato"), small = true, color = Palette.white, textColor = Palette.ink) {
                app.learner.boredLesson(s.skill, lesson.style, nowMs())
                app.commit()
                app.say(tx("lessonscreen_bora_aprender_jogando_entao_errar"))
                app.replace(Screen.Play(s.skill, app.learner.chooseGame(s.skill), 1, seed = nowMs()))
            }
            Spacer(Modifier.weight(1f))
            if (!finished) BigButton(if (blocking) tx("comum_responda") else tx("comum_continuar_2"), small = true, enabled = !blocking, color = color) {
                Sound.play(Sfx.CLICK); shown++
            }
        }
    }
}

@Composable
fun BeatView(b: Beat, index: Int, answered: MutableMap<Int, Int>, revealed: MutableMap<Int, Boolean>) {
    when (b) {
        is Beat.Say -> SpeechBubble(
            b.who.emoji, b.who.displayName, b.text,
            color = if (b.who == com.compilandia.content.Speaker.BYTE) Palette.yellow.copy(alpha = 0.25f) else Palette.white,
            rightSide = b.who == com.compilandia.content.Speaker.BYTE,
        )
        is Beat.Text -> RichText(b.text)
        is Beat.Code -> {
            CodeBlock(b.code, b.caption)
            if (b.code.contains("main:") && listOf("class ", "void ", "public ").none { it in b.code }) {
                var run by remember { mutableStateOf(false) }
                if (!run) BigButton(tx("lessonscreen_rodar_no_mars_bolso"), small = true, color = Palette.teal) { run = true }
                else com.compilandia.ui.visuals.MipsRunView(b.code)
            }
        }
        is Beat.Callout -> PaperCard(color = calloutColor(b.kind).copy(alpha = 0.15f), border = calloutColor(b.kind)) {
            Text("${b.kind.emoji} ${b.kind.title}", style = MaterialTheme.typography.titleSmall, color = calloutColor(b.kind))
            RichText(b.text, style = MaterialTheme.typography.bodyMedium)
        }
        is Beat.Predict -> PaperCard(border = Palette.orange) {
            Text(tx("lessonscreen_preveja_antes_ver"), style = MaterialTheme.typography.labelMedium, color = Palette.orange)
            RichText(b.question, style = MaterialTheme.typography.titleSmall)
            val chosen = answered[index]
            b.options.forEachIndexed { k, opt ->
                val bg = when {
                    chosen == null -> Palette.white
                    k == b.correct -> Palette.green.copy(alpha = 0.25f)
                    k == chosen -> Palette.red.copy(alpha = 0.2f)
                    else -> Palette.white
                }
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg)
                        .border(2.dp, Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable(enabled = chosen == null) {
                            answered[index] = k
                            if (k == b.correct) Juice.correct() else Juice.wrong()
                        }.padding(10.dp)
                ) { RichText(opt, style = MaterialTheme.typography.bodyMedium) }
            }
            if (chosen != null) FeedbackBanner(
                chosen == b.correct,
                (if (chosen == b.correct) "**Isso!** " else tx("lessonscreen_boa_tentativa_errar_aqui_ajuda")) + b.explanation,
            )
        }
        is Beat.Reveal -> PaperCard(border = Palette.teal) {
            Text(tx("lessonscreen_pense_primeiro_depois_toque"), style = MaterialTheme.typography.labelMedium, color = Palette.teal)
            RichText(b.prompt, style = MaterialTheme.typography.titleSmall)
            if (revealed[index] == true) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.teal.copy(alpha = 0.12f)).padding(10.dp)) {
                    RichText(b.answer, style = MaterialTheme.typography.bodyMedium)
                }
            } else BigButton(tx("lessonscreen_pensei_mostrar_resposta"), small = true, color = Palette.teal) { revealed[index] = true; Sound.play(Sfx.WHOOSH) }
        }
        is Beat.Steps -> StepsBeat(b)
        is Beat.Visual -> VisualBeat(b)
    }
}

@Composable
private fun StepsBeat(b: Beat.Steps) {
    var n by remember { mutableIntStateOf(1) }
    PaperCard(border = Palette.blue) {
        Text("🪜 ${b.title}", style = MaterialTheme.typography.titleSmall)
        b.steps.take(n).forEachIndexed { i, st ->
            Row {
                Text("${i + 1}.", fontWeight = FontWeight.Bold, color = Palette.blue, modifier = Modifier.width(26.dp))
                RichText(st, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (n < b.steps.size) BigButton(tx("lessonscreen_proximo_passo"), small = true, color = Palette.blue) { n++ }
    }
}

fun calloutColor(k: com.compilandia.content.CalloutKind) = when (k) {
    com.compilandia.content.CalloutKind.NO_DUMB_QUESTIONS -> Palette.blue
    com.compilandia.content.CalloutKind.BRAIN_POWER -> Palette.purple
    com.compilandia.content.CalloutKind.WATCH_OUT -> Palette.red
    com.compilandia.content.CalloutKind.MNEMONIC -> Palette.pink
    com.compilandia.content.CalloutKind.EXAM_TIP -> Palette.orange
    com.compilandia.content.CalloutKind.FIRESIDE -> Palette.green
    com.compilandia.content.CalloutKind.REAL_WORLD -> Palette.teal
}

// ---------------------------------------------------------------- Feynman

/**
 * Técnica Feynman / autoexplicação: escrever com as próprias palavras.
 * Conferimos quais termos-chave apareceram — sem nota, só um espelho.
 */
@Composable
fun FeynmanScreen(app: AppState, skill: Skill) {
    val card = Library.card(skill)
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val color = Palette.region(skill.region)
    Column(Modifier.fillMaxSize()) {
        Header(tx("lessonscreen_explique_com_suas_palavras"), skill.title, color)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp).larguraDeLeitura(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeechBubble("🦉", tx("comum_profa_gramatica"), tx("lessonscreen_quem_consegue_explicar_entendeu_verdade"))
            PaperCard { RichText(card?.feynmanPrompt ?: tx("lessonscreen_explique_1_para_colega_que", skill.title), style = MaterialTheme.typography.titleSmall) }
            OutlinedTextField(text, { text = it; checked = false }, Modifier.fillMaxWidth(), minLines = 5, label = { Text(tx("lessonscreen_minha_explicacao")) })
            if (checked && card != null) {
                val lower = com.compilandia.learning.normalizeForMatch(text)
                val hits = card.keyTerms.filter { lower.contains(com.compilandia.learning.normalizeForMatch(it)) }
                PaperCard(color = Palette.paper) {
                    Text(tx("lessonscreen_espelho_sua_explicacao"), style = MaterialTheme.typography.titleSmall)
                    ChipRow { for (t in card.keyTerms) Chip((if (t in hits) "✓ " else "○ ") + t, selected = t in hits, color = Palette.green) }
                    Text(
                        when {
                            hits.size == card.keyTerms.size -> tx("lessonscreen_voce_usou_todas_ideias_chave")
                            hits.size * 2 >= card.keyTerms.size -> tx("lessonscreen_muito_bom_que_tal_encaixar")
                            else -> tx("lessonscreen_otimo_comeco_releia_termos_com")
                        }, style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("🪝 ${card.hook}", style = MaterialTheme.typography.bodySmall, color = Palette.purple)
                    if (card.commonMistakes.isNotEmpty()) {
                        Text(tx("lessonscreen_erros_comuns_para_evitar"), style = MaterialTheme.typography.labelMedium)
                        for (m in card.commonMistakes) RichText("• $m", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Palette.paperDark).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BigButton(tx("comum_pular"), small = true, color = Palette.white, textColor = Palette.ink) {
                app.replace(Screen.Play(skill, app.learner.chooseGame(skill), app.learner.chooseLevel(skill, nowMs()), seed = nowMs()))
            }
            Spacer(Modifier.weight(1f))
            if (!checked) BigButton(tx("comum_conferir"), small = true, color = color, enabled = text.trim().length >= 15) { checked = true; Sound.play(Sfx.CLICK) }
            else BigButton(tx("lessonscreen_salvar_praticar"), small = true, color = Palette.orange) {
                app.learner.addJournal(card?.feynmanPrompt ?: skill.title, text.trim(), skill, nowMs())
                app.commit()
                app.say(tx("lessonscreen_20_xp_por_explicar_explicar"))
                app.replace(Screen.Play(skill, app.learner.chooseGame(skill), app.learner.chooseLevel(skill, nowMs()), seed = nowMs()))
            }
        }
    }
}

/** O palco no topo da aula de texto: as cenas da aula animada passando sozinhas. Toque pausa. */
@Composable
private fun PalcoDaAula(skill: com.compilandia.learning.Skill) {
    val ep = remember(skill) { com.compilandia.content.episodes.Episodes.of(skill) } ?: return
    var k by remember(skill) { mutableIntStateOf(0) }
    var tocando by remember { mutableStateOf(!com.compilandia.ui.ThemeState.reduceMotion) }
    LaunchedEffect(k, tocando) {
        if (!tocando) return@LaunchedEffect
        kotlinx.coroutines.delay(2600L + ep.passos[k].fala.text.length * 30L)
        k = (k + 1) % ep.passos.size
    }
    PaperCard(Modifier.fillMaxWidth(), color = Palette.paperDark, border = Palette.drawer) {
        Box(
            Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(Palette.codeBg)
                .clickable { tocando = !tocando },
        ) {
            com.compilandia.ui.motion.MotionStage(ep.passos[k].shot, Modifier.fillMaxSize().padding(4.dp))
        }
        RichText(ep.passos[k].fala.text, style = MaterialTheme.typography.bodyMedium)
        Text(
            if (tocando) tx("palco_texto_tocando", k + 1, ep.passos.size) else tx("palco_texto_pausado", k + 1, ep.passos.size),
            style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft,
        )
    }
}

