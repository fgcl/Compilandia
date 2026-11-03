package com.compilandia.ui.screens

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

@Composable
fun LessonScreen(app: AppState, s: Screen.Lesson) {
    val lesson: Lesson? = remember(s) {
        Library.lesson(s.skill, s.style) ?: Library.lessons.firstOrNull { it.skill == s.skill && it.style !in s.tried }
    }
    val color = Palette.region(s.skill.region)
    if (lesson == null) {
        Column(Modifier.fillMaxSize()) {
            Header(s.skill.title, "aula", color)
            CenterText("Ainda não há aula para este tópico.\nTente praticar no minijogo! 🎮")
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
        app.say("Sem problema! Vamos de outro jeito: ${next.emoji} ${next.title}")
        app.replace(s.copy(style = next, tried = s.tried + lesson.style))
    }

    Column(Modifier.fillMaxSize()) {
        Header("${lesson.style.emoji} ${lesson.title}", "${s.skill.title} · ${lesson.style.title}", color)
        ProgressBar(shown.toFloat() / beats.size, color, Modifier.fillMaxWidth(), 6.dp)
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            beats.take(shown).forEachIndexed { i, b ->
                AnimatedVisibility(true, enter = fadeIn() + expandVertically()) {
                    BeatView(b, i, answered, revealed)
                }
            }
            if (finished) {
                PaperCard(color = Palette.yellow.copy(alpha = 0.2f)) {
                    Text("E aí, como foi?", style = MaterialTheme.typography.titleMedium)
                    Text("Seja sincero(a): eu uso isso para escolher como explicar da próxima vez.", style = MaterialTheme.typography.bodySmall)
                    BigButton("👍 Entendi! Quero explicar com minhas palavras", Modifier.fillMaxWidth(), color = Palette.green) {
                        app.learner.recordLessonViewed(s.skill, lesson.style, true)
                        app.commit()
                        Juice.correct()
                        if (s.returnToGame) app.back() else app.replace(Screen.Feynman(s.skill))
                    }
                    BigButton("🎮 Entendi, quero praticar direto", Modifier.fillMaxWidth(), color = Palette.orange) {
                        app.learner.recordLessonViewed(s.skill, lesson.style, true)
                        app.commit()
                        if (s.returnToGame) app.back() else
                            app.replace(Screen.Play(s.skill, app.learner.chooseGame(s.skill), app.learner.chooseLevel(s.skill, nowMs()), seed = nowMs()))
                    }
                    BigButton("😕 Não entendi — explica de outro jeito", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink) { notUnderstood() }
                }
            }
            VSpace(80)
        }
        Row(
            Modifier.fillMaxWidth().background(Palette.paperDark).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            BigButton("😕 Não entendi", small = true, color = Palette.white, textColor = Palette.ink) { notUnderstood() }
            BigButton("😴 Chato", small = true, color = Palette.white, textColor = Palette.ink) {
                app.learner.boredLesson(s.skill, lesson.style, nowMs())
                app.commit()
                app.say("Bora aprender jogando então! Errar no jogo também ensina.")
                app.replace(Screen.Play(s.skill, app.learner.chooseGame(s.skill), 1, seed = nowMs()))
            }
            Spacer(Modifier.weight(1f))
            if (!finished) BigButton(if (blocking) "responda ↑" else "Continuar ▶", small = true, enabled = !blocking, color = color) {
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
                if (!run) BigButton("▶ Rodar no MARS de bolso", small = true, color = Palette.teal) { run = true }
                else com.compilandia.ui.visuals.MipsRunView(b.code)
            }
        }
        is Beat.Callout -> PaperCard(color = calloutColor(b.kind).copy(alpha = 0.15f), border = calloutColor(b.kind)) {
            Text("${b.kind.emoji} ${b.kind.title}", style = MaterialTheme.typography.titleSmall, color = calloutColor(b.kind))
            RichText(b.text, style = MaterialTheme.typography.bodyMedium)
        }
        is Beat.Predict -> PaperCard(border = Palette.orange) {
            Text("🔮 Preveja antes de ver", style = MaterialTheme.typography.labelMedium, color = Palette.orange)
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
                (if (chosen == b.correct) "**Isso!** " else "**Boa tentativa — e errar aqui AJUDA a lembrar.** ") + b.explanation,
            )
        }
        is Beat.Reveal -> PaperCard(border = Palette.teal) {
            Text("🧠 Pense primeiro, depois toque", style = MaterialTheme.typography.labelMedium, color = Palette.teal)
            RichText(b.prompt, style = MaterialTheme.typography.titleSmall)
            if (revealed[index] == true) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.teal.copy(alpha = 0.12f)).padding(10.dp)) {
                    RichText(b.answer, style = MaterialTheme.typography.bodyMedium)
                }
            } else BigButton("Pensei! Mostrar resposta", small = true, color = Palette.teal) { revealed[index] = true; Sound.play(Sfx.WHOOSH) }
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
        if (n < b.steps.size) BigButton("próximo passo", small = true, color = Palette.blue) { n++ }
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
        Header("✍️ Explique com suas palavras", skill.title, color)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeechBubble("🦉", "Profª Gramática", "Quem consegue explicar, entendeu de verdade. Escreva como se fosse mandar um áudio para um amigo — sem copiar, sem olhar. Frases tortas valem!")
            PaperCard { RichText(card?.feynmanPrompt ?: "Explique \"${skill.title}\" para um colega que faltou à aula.", style = MaterialTheme.typography.titleSmall) }
            OutlinedTextField(text, { text = it; checked = false }, Modifier.fillMaxWidth(), minLines = 5, label = { Text("Minha explicação") })
            if (checked && card != null) {
                val lower = com.compilandia.learning.normalizeForMatch(text)
                val hits = card.keyTerms.filter { lower.contains(com.compilandia.learning.normalizeForMatch(it)) }
                PaperCard(color = Palette.paper) {
                    Text("Espelho da sua explicação", style = MaterialTheme.typography.titleSmall)
                    ChipRow { for (t in card.keyTerms) Chip((if (t in hits) "✓ " else "○ ") + t, selected = t in hits, color = Palette.green) }
                    Text(
                        when {
                            hits.size == card.keyTerms.size -> "Você usou todas as ideias-chave. Excelente! 🏆"
                            hits.size * 2 >= card.keyTerms.size -> "Muito bom! Que tal encaixar as ideias que faltaram (○)?"
                            else -> "Ótimo começo. Releia os termos com ○ — consegue explicar como eles se ligam ao que você escreveu?"
                        }, style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("🪝 ${card.hook}", style = MaterialTheme.typography.bodySmall, color = Palette.purple)
                    if (card.commonMistakes.isNotEmpty()) {
                        Text("Erros comuns para evitar:", style = MaterialTheme.typography.labelMedium)
                        for (m in card.commonMistakes) RichText("• $m", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Palette.paperDark).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BigButton("Pular", small = true, color = Palette.white, textColor = Palette.ink) {
                app.replace(Screen.Play(skill, app.learner.chooseGame(skill), app.learner.chooseLevel(skill, nowMs()), seed = nowMs()))
            }
            Spacer(Modifier.weight(1f))
            if (!checked) BigButton("Conferir", small = true, color = color, enabled = text.trim().length >= 15) { checked = true; Sound.play(Sfx.CLICK) }
            else BigButton("Salvar e praticar 🎮", small = true, color = Palette.orange) {
                app.learner.addJournal(card?.feynmanPrompt ?: skill.title, text.trim(), skill, nowMs())
                app.commit()
                app.say("+20 XP por explicar! Explicar é a forma mais forte de estudar.")
                app.replace(Screen.Play(skill, app.learner.chooseGame(skill), app.learner.chooseLevel(skill, nowMs()), seed = nowMs()))
            }
        }
    }
}

