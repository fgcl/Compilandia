package com.compilandia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Beat
import com.compilandia.content.Library
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.SpeechBubble
import com.compilandia.ui.VSpace

private val guideColor get() = Palette.teal

@Composable
fun GuideScreen(app: AppState) {
    val chapters = Library.guide
    Column(Modifier.fillMaxSize()) {
        Header("🛠️ Guia do Trabalho Prático", "Construa um compilador completo, do .txt ao MIPS", guideColor)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpeechBubble("🦉", "Profª Gramática", "Na PUC Minas o trabalho prático de Compiladores pede um compilador INTEIRO: léxico com autômatos, gramática LL(1), descida recursiva, tabela de símbolos, tipos e geração de assembly MIPS. Este guia te leva por cada etapa com uma linguagem de exemplo.")
            if (chapters.isEmpty()) Text("Guia em construção.", color = Palette.inkSoft)
            ProgressBar(app.ls.guideRead.size.toFloat() / chapters.size.coerceAtLeast(1), guideColor, Modifier.fillMaxWidth())
            for (c in chapters) {
                val read = c.number in app.ls.guideRead
                PaperCard(Modifier.fillMaxWidth().clickable { app.go(Screen.GuideChapter(c.number)) }, border = guideColor, color = if (read) guideColor.copy(alpha = 0.07f) else Palette.white) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (read) "✅" else "${c.number}", fontSize = 22.sp, modifier = Modifier.width(36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall)
                            Text(c.summary, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                        }
                    }
                }
            }
            VSpace(30)
        }
    }
}

@Composable
fun GuideChapterScreen(app: AppState, number: Int) {
    val c = Library.guide.firstOrNull { it.number == number }
    if (c == null) { Column(Modifier.fillMaxSize()) { Header("Guia", null, guideColor); CenterText("Capítulo não encontrado") }; return }
    var shown by remember { mutableIntStateOf(1) }
    val answered = remember { mutableStateMapOf<Int, Int>() }
    val revealed = remember { mutableStateMapOf<Int, Boolean>() }
    val scroll = rememberScrollState()
    LaunchedEffect(shown) { scroll.animateScrollTo(scroll.maxValue) }
    val blocking = c.beats.take(shown).withIndex().any { (i, b) -> (b is Beat.Predict && i !in answered) || (b is Beat.Reveal && revealed[i] != true) }
    val finished = shown >= c.beats.size && !blocking
    Column(Modifier.fillMaxSize()) {
        Header("${c.number}. ${c.title}", "Guia do TP · relacionado a: ${c.skill.title}", guideColor)
        ProgressBar(shown.toFloat() / c.beats.size, guideColor, Modifier.fillMaxWidth(), 6.dp)
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            c.beats.take(shown).forEachIndexed { i, b -> BeatView(b, i, answered, revealed) }
            if (finished) {
                PaperCard(color = guideColor.copy(alpha = 0.08f), border = guideColor) {
                    RichText("Capítulo concluído! Quer **praticar** \"${c.skill.title}\" num minijogo agora? Fazer logo depois de ler fixa muito mais.", style = MaterialTheme.typography.bodyMedium)
                    BigButton("🎮 Praticar", Modifier.fillMaxWidth(), color = Palette.orange) {
                        app.learner.markGuideRead(c.number); app.commit()
                        app.replace(Screen.Play(c.skill, app.learner.chooseGame(c.skill), app.learner.chooseLevel(c.skill, nowMs()), seed = nowMs()))
                    }
                    val next = Library.guide.firstOrNull { it.number == c.number + 1 }
                    if (next != null) BigButton("📘 Próximo capítulo: ${next.title}", Modifier.fillMaxWidth(), color = guideColor) {
                        app.learner.markGuideRead(c.number); app.commit()
                        app.replace(Screen.GuideChapter(next.number))
                    }
                    BigButton("Voltar ao índice", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink) {
                        app.learner.markGuideRead(c.number); app.commit(); app.back()
                    }
                }
            }
            VSpace(80)
        }
        Row(Modifier.fillMaxWidth().background(Palette.paperDark).padding(10.dp)) {
            Spacer(Modifier.weight(1f))
            if (!finished) BigButton(if (blocking) "responda ↑" else "Continuar ▶", small = true, enabled = !blocking, color = guideColor) { Sound.play(Sfx.CLICK); shown++ }
        }
    }
}
