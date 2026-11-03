package com.compilandia.ui.screens

import com.compilandia.resources.Res
import com.compilandia.resources.medal1
import com.compilandia.resources.medal2
import com.compilandia.resources.medal3
import com.compilandia.resources.medal4
import com.compilandia.resources.medal5
import com.compilandia.resources.medal6
import com.compilandia.resources.medal7
import com.compilandia.resources.medal8
import com.compilandia.resources.medal9
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Library
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import com.compilandia.learning.Coach
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.ui.ThemeState
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.SpeechBubble
import com.compilandia.ui.VSpace

// ---------------------------------------------------------------- Revisão relâmpago

/** Flashcards com recuperação ativa + autoavaliação, alimentando a curva de esquecimento. */
@Composable
fun FlashcardScreen(app: AppState, skills: List<Skill>) {
    val cards = remember {
        skills.flatMap { s -> Library.card(s)?.flashcards.orEmpty().map { s to it } }.shuffled().take(10)
            .ifEmpty { Library.cards.flatMap { c -> c.flashcards.map { c.skill to it } }.shuffled().take(8) }
    }
    var i by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    val perSkill = remember { mutableMapOf<Skill, MutableList<Double>>() }
    Column(Modifier.fillMaxSize()) {
        Header("⚡ Revisão relâmpago", "Lembre ANTES de virar. Esforço = memória.", Palette.teal)
        if (cards.isEmpty()) { CenterText("Ainda não há cartões. Volte quando as aulas estiverem carregadas!"); return }
        ProgressBar(i.toFloat() / cards.size, Palette.teal, Modifier.fillMaxWidth(), 6.dp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (i >= cards.size) {
                Text("Revisão concluída! 🧠", style = MaterialTheme.typography.headlineMedium)
                Text("Cada cartão que você tentou lembrar ficou mais forte — inclusive os que você errou.", style = MaterialTheme.typography.bodyLarge)
                for ((s, scores) in perSkill) Text("• ${s.title}: ${(scores.average() * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                BigButton("🗺️ Voltar ao mapa", Modifier.fillMaxWidth()) { app.home() }
                return@Column
            }
            val (skill, qa) = cards[i]
            Text(skill.title, style = MaterialTheme.typography.labelLarge, color = Palette.ink)
            Box(
                Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(22.dp))
                    .background(if (revealed) Palette.teal.copy(alpha = 0.12f) else Palette.white)
                    .border(3.dp, Palette.ink, RoundedCornerShape(22.dp))
                    .clickable { if (!revealed) { revealed = true; Sound.play(Sfx.WHOOSH) } }.padding(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                RichText(if (revealed) qa.second else qa.first, style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center))
            }
            if (!revealed) Text("Pense na resposta (de verdade!) e toque no cartão.", color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
            else {
                Text("Pergunta: ${qa.first}", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                Text("Como foi lembrar?", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((label, sc, c) in listOf(Triple("😵 Errei", 0.0, Palette.red), Triple("😓 Difícil", 0.6, Palette.yellow), Triple("😎 Fácil", 1.0, Palette.green))) {
                        BigButton(label, Modifier.weight(1f), color = c, small = true) {
                            perSkill.getOrPut(skill) { mutableListOf() } += sc
                            app.learner.recordAttempt(skill, GameId.FLASHCARDS, 1, sc, null, 5000, 0, nowMs())
                            app.commit()
                            revealed = false; i++
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Painel do cérebro

@Composable
fun BrainScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    val now = remember { nowMs() }
    val cal = remember(ls) { l.calibration() }
    Column(Modifier.fillMaxSize()) {
        Header("🧠 Painel do cérebro", "O que a IA aprendeu sobre como VOCÊ aprende", Palette.blue)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PaperCard(color = Palette.yellow.copy(alpha = 0.15f)) {
                Text("🦉 Conselhos personalizados", style = MaterialTheme.typography.titleSmall)
                for (s in remember(ls) { Coach.insights(l, now) }) RichText(s, style = MaterialTheme.typography.bodyMedium)
            }
            PaperCard {
                val ach = remember(ls) { com.compilandia.learning.Achievements.all(l) }
                Text("🏅 Conquistas (${ach.count { it.unlocked }}/${ach.size})", style = MaterialTheme.typography.titleSmall)
                for (a in ach) Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                        if (a.unlocked) androidx.compose.foundation.Image(org.jetbrains.compose.resources.painterResource(medalFor(ach.indexOf(a))), a.title, Modifier.size(24.dp, 42.dp))
                        else Text("🔒", fontSize = 20.sp)
                    }
                    Column {
                        Text(a.title, style = MaterialTheme.typography.labelLarge, color = if (a.unlocked) Palette.ink else Palette.inkSoft)
                        Text(a.description, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    }
                }
            }
            PaperCard {
                Text("🎯 Calibração (você sabe o que sabe?)", style = MaterialTheme.typography.titleSmall)
                if (cal.samples == 0) Text("Responda à pergunta de confiança antes dos jogos para ver isto.", style = MaterialTheme.typography.bodySmall)
                else {
                    for ((label, acc, n) in cal.buckets) if (n > 0) {
                        Text("$label → acertou ${(acc * 100).toInt()}% ($n vezes)", style = MaterialTheme.typography.bodySmall)
                        ProgressBar(acc.toFloat(), Palette.purple, Modifier.fillMaxWidth(), 8.dp)
                    }
                    Text("Índice de Brier: ${(cal.brier * 100).toInt() / 100.0} (0 = perfeito). " +
                        (if (cal.bias > 0.15) "Tendência: confiança DEMAIS." else if (cal.bias < -0.15) "Tendência: confiança de MENOS." else "Bem calibrado!"),
                        style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                }
            }
            PaperCard {
                Text("🤖 A rede neural", style = MaterialTheme.typography.titleSmall)
                RichText(
                    "Uma rede de **10 entradas → 12 neurônios → 1 saída** roda no seu celular e é treinada a cada partida. " +
                        "Ela olha seu domínio estimado (BKT), quanto você lembra (curva de esquecimento), acertos recentes, nível do desafio e sua confiança, e prevê sua chance de acerto. " +
                        "O jogo usa isso para escolher desafios na faixa de ~75% de chance: difícil o bastante para ensinar, possível o bastante para não frustrar.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("Treinada com ${ls.net.trainedSamples} jogadas · erro médio (log-loss): ${(ls.net.runningLoss * 100).toInt() / 100.0}", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                l.aiAccuracy()?.let { Text("Acertou ${(it * 100).toInt()}% das previsões recentes.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft) }
            }
            PaperCard {
                Text("📖 Estilos de explicação que funcionam para você", style = MaterialTheme.typography.titleSmall)
                for ((st, v) in l.styleRanking()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${st.emoji} ${st.title}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(170.dp))
                        ProgressBar(v.toFloat(), Palette.pink, Modifier.weight(1f), 8.dp)
                    }
                }
                Text("Calculado com Thompson Sampling: o jogo testa estilos e aposta mais nos que você marca como \"entendi\".", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
            }
            PaperCard {
                Text("🎮 Jogos favoritos", style = MaterialTheme.typography.titleSmall)
                for ((g, v) in l.gameRanking().filter { it.first != GameId.FLASHCARDS }.take(6)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${g.emoji} ${g.title}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(170.dp))
                        ProgressBar(v.toFloat(), Palette.orange, Modifier.weight(1f), 8.dp)
                    }
                }
            }
            PaperCard {
                Text("🗺️ Domínio e memória por assunto", style = MaterialTheme.typography.titleSmall)
                for (s in Skill.entries) {
                    val st = l.skill(s)
                    Text(s.title, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("domínio", fontSize = 10.sp, modifier = Modifier.width(48.dp))
                        ProgressBar(st.mastery.toFloat(), Palette.region(s.region), Modifier.weight(1f), 7.dp)
                        Text("memória", fontSize = 10.sp, modifier = Modifier.width(48.dp))
                        ProgressBar(st.memory.retrievability(now).toFloat(), Palette.teal, Modifier.weight(1f), 7.dp)
                    }
                }
            }
            VSpace(30)
        }
    }
}

// ---------------------------------------------------------------- Diário

private val reflectionPrompts = listOf(
    "O que eu aprendi hoje, em uma frase?",
    "Onde eu travei? O que exatamente eu não entendi?",
    "Qual analogia ou exemplo me fez entender algo?",
    "Se eu fosse explicar isso para um calouro, por onde começaria?",
    "O que eu errei hoje e o que esse erro me ensinou?",
    "Como isso se conecta com algo que eu já sabia (programação, matemática…)?",
)

@Composable
fun JournalScreen(app: AppState) {
    var prompt by remember { mutableStateOf(reflectionPrompts.random()) }
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Header("📓 Diário de bordo", "Refletir transforma experiência em aprendizado", Palette.pink)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeechBubble("🦉", "Profª Gramática", "Escrever sobre o que você aprendeu (e sobre o que NÃO entendeu) é metacognição pura. Dois minutos bastam.")
            PaperCard {
                Text(prompt, style = MaterialTheme.typography.titleSmall)
                Text("trocar pergunta ↻", color = Palette.pink, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { prompt = reflectionPrompts.random() })
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), minLines = 4, label = { Text("Escreva livremente") })
                BigButton("Salvar (+20 XP)", Modifier.fillMaxWidth(), color = Palette.pink, enabled = text.isNotBlank()) {
                    app.learner.addJournal(prompt, text.trim(), null, nowMs()); app.commit()
                    text = ""; app.say("Salvo no diário! ✍️")
                }
            }
            Text("Entradas anteriores", style = MaterialTheme.typography.headlineSmall)
            if (app.ls.journal.isEmpty()) Text("Nada ainda.", color = Palette.inkSoft)
            for (e in app.ls.journal.asReversed()) PaperCard(color = Palette.paper) {
                Text(e.prompt, style = MaterialTheme.typography.labelMedium, color = Palette.pink)
                e.skill?.let { s -> Skill.entries.firstOrNull { it.name == s }?.let { Text(it.title, style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft) } }
                Text(e.text, style = MaterialTheme.typography.bodyMedium)
            }
            VSpace(30)
        }
    }
}

// ---------------------------------------------------------------- Ajustes

@Composable
fun SettingsScreen(app: AppState) {
    var confirmReset by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Header("⚙️ Ajustes", null, Palette.drawer)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PaperCard {
                Text("🎨 Tema", style = MaterialTheme.typography.titleSmall)
                for (t in com.compilandia.ui.ThemeMode.entries) {
                    val sel = app.ls.theme == t.name
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (sel) Palette.hazard.copy(alpha = 0.18f) else Palette.white)
                            .border(2.dp, if (sel) Palette.hazard else Palette.ink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { app.learner.setTheme(t.name); app.commit(); Sound.play(Sfx.CLICK) }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val prev = ThemePreviewColors(t)
                        Row(Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))) {
                            for (c in prev) Box(Modifier.size(18.dp, 30.dp).background(c))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.labelLarge)
                            Text(t.description, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                        }
                        if (sel) Text("✔", color = if (ThemeState.colors.dark) Palette.hazard else Palette.ink, fontSize = 20.sp)
                    }
                }
            }
            PaperCard {
                Text("🔠 Tamanho do texto", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((label, f) in listOf("A" to 0.9f, "A+" to 1f, "A++" to 1.15f, "A+++" to 1.3f)) {
                        Chip(label, selected = app.ls.fontScale == f, color = Palette.purple) { app.learner.setFontScale(f); app.commit() }
                    }
                }
                Text("Exemplo: FIRST(E) = { (, id }", style = MaterialTheme.typography.bodyLarge)
            }
            SettingRow("🐢 Reduzir animações (sem confete/tremida)", app.ls.reduceMotion) { app.learner.setReduceMotion(it); app.commit() }
            SettingRow("Perguntar minha confiança antes dos jogos", app.ls.askConfidence) { app.learner.setAskConfidence(it); app.commit() }
            SettingRow("Sons", app.ls.soundOn) { app.learner.setSound(it); app.commit() }
            SettingRow("Voz do Byte", app.ls.voiceOn) { app.learner.setVoice(it); app.commit() }
            SettingRow("Legenda mesmo com voz", app.ls.captionsOn) { app.learner.setCaptions(it); app.commit() }
            PaperCard {
                val today = nowMs() / 86_400_000L
                val d = app.learner.daysToExam(nowMs())
                Text("📅 Data da prova", style = MaterialTheme.typography.titleSmall)
                Text(if (d == null) "Nenhuma prova marcada." else "Prova em $d dia(s). O jogo prioriza seus pontos fracos nos últimos 10 dias.", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((label, delta) in listOf("-7" to -7, "-1" to -1, "+1" to 1, "+7" to 7)) BigButton(label, Modifier.weight(1f), small = true, color = Palette.white, textColor = Palette.ink) {
                        val base = d ?: 14
                        app.learner.setExamDay(today + (base + delta).coerceAtLeast(0)); app.commit()
                    }
                }
                if (d != null) Text("remover prova", color = Palette.red, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { app.learner.setExamDay(-1); app.commit() })
            }
            BigButton("📘 Rever a introdução", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink) { app.go(Screen.Onboarding) }
            if (!confirmReset) BigButton("🗑️ Apagar todo o progresso", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.red) { confirmReset = true }
            else {
                Text("Tem certeza? Isso apaga XP, memória, diário e a rede neural treinada.", color = Palette.red)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BigButton("Cancelar", Modifier.weight(1f), color = Palette.white, textColor = Palette.ink) { confirmReset = false }
                    BigButton("Apagar", Modifier.weight(1f), color = Palette.red) {
                        app.learner.reset(); app.commit()
                        app.say("Progresso apagado. Um novo começo! 🌱")
                        confirmReset = false
                    }
                }
            }
            Text("Compilândia — feito para quem acha que \"não é bom em compiladores\". Fontes: Atkinson Hyperlegible, Space Mono e Titan One (SIL Open Font License). Arte e sons: Kenney.nl (CC0).",
                style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(value, onChange)
    }
}

fun ThemePreviewColors(t: com.compilandia.ui.ThemeMode): List<androidx.compose.ui.graphics.Color> {
    val c = com.compilandia.ui.ThemeState.colorsFor(t)
    return listOf(c.paper, c.drawer, c.ink, c.hazard, c.action)
}

private val medals = listOf(
    Res.drawable.medal1, Res.drawable.medal2, Res.drawable.medal3,
    Res.drawable.medal4, Res.drawable.medal5, Res.drawable.medal6,
    Res.drawable.medal7, Res.drawable.medal8, Res.drawable.medal9,
)
fun medalFor(i: Int) = medals[i % medals.size]
