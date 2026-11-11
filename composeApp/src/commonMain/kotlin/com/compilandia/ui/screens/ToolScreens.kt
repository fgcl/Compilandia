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
import com.compilandia.textos.tx

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
        Header(tx("toolscreens_revisao_relampago"), tx("toolscreens_lembre_antes_virar_esforco_memoria"), Palette.teal)
        if (cards.isEmpty()) { CenterText(tx("toolscreens_ainda_nao_ha_cartoes_volte")); return }
        ProgressBar(i.toFloat() / cards.size, Palette.teal, Modifier.fillMaxWidth(), 6.dp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (i >= cards.size) {
                Text(tx("toolscreens_revisao_concluida"), style = MaterialTheme.typography.headlineMedium)
                Text(tx("toolscreens_cada_cartao_que_voce_tentou"), style = MaterialTheme.typography.bodyLarge)
                for ((s, scores) in perSkill) Text("• ${s.title}: ${(scores.average() * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                BigButton(tx("toolscreens_voltar_ao_mapa"), Modifier.fillMaxWidth()) { app.home() }
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
            if (!revealed) Text(tx("toolscreens_pense_na_resposta_verdade_toque"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
            else {
                Text(tx("toolscreens_pergunta_1", qa.first), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                Text(tx("toolscreens_como_foi_lembrar"), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((label, sc, c) in listOf(Triple(tx("toolscreens_errei"), 0.0, Palette.red), Triple(tx("toolscreens_dificil"), 0.6, Palette.yellow), Triple(tx("toolscreens_facil"), 1.0, Palette.green))) {
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

// ---------------------------------------------------------------- Diário

/** T4: o diário saiu do jogo. Quem escreveu antes ainda lê as anotações aqui, a partir de Ajustes. */
@Composable
fun JournalScreen(app: AppState) {
    Column(Modifier.fillMaxSize()) {
        Header(tx("diario_titulo"), null, Palette.drawer)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(tx("diario_aviso"), style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
            for (e in app.ls.journal.asReversed()) PaperCard(color = Palette.paper) {
                Text(e.prompt, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                e.skill?.let { s -> Skill.entries.firstOrNull { it.name == s }?.let { Text(it.title, style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft) } }
                Text(e.text, style = MaterialTheme.typography.bodyMedium)
            }
            VSpace(30)
        }
    }
}

// ---------------------------------------------------------------- Ajustes

/**
 * R4: o quadro do lembrete nos Ajustes. Lembrete local, desligado até a pessoa ligar, só no celular.
 * [disponivel] vem de fora para o teste do computador capturar também a versão do celular.
 */
@Composable
internal fun LembreteAjuste(app: AppState, disponivel: Boolean = com.compilandia.platform.Lembrete.disponivel, cartao: Boolean = true) {
    if (cartao) PaperCard { Text(tx("lembrete_ajuste"), style = MaterialTheme.typography.titleSmall); LembreteCorpo(app, disponivel) }
    else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { LembreteCorpo(app, disponivel) }
}

@Composable
private fun LembreteCorpo(app: AppState, disponivel: Boolean) {
        if (disponivel) {
            Text(tx("lembrete_ajuste_sub"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(tx("lembrete_desligado"), selected = !app.ls.lembreteLigado, color = Palette.inkSoft) {
                    app.learner.setLembrete(false); app.commit()
                    com.compilandia.platform.Lembrete.agenda(false, app.ls.lembreteHora)
                }
                Chip(tx("lembrete_ligado"), selected = app.ls.lembreteLigado, color = Palette.teal) {
                    com.compilandia.platform.Lembrete.pedePermissao { ok ->
                        if (ok) {
                            app.learner.setLembrete(true); app.commit()
                            com.compilandia.platform.Lembrete.agenda(true, app.ls.lembreteHora)
                        } else app.say(tx("lembrete_sem_permissao"))
                    }
                }
            }
            if (app.ls.lembreteLigado) {
                Text(tx("lembrete_hora", app.ls.lembreteHora), style = MaterialTheme.typography.labelLarge)
                com.compilandia.ui.ChipRow {
                    for (h in listOf(8, 12, 15, 19, 21)) Chip(tx("lembrete_hora_chip", h), selected = app.ls.lembreteHora == h, color = Palette.teal) {
                        app.learner.setLembrete(true, h); app.commit()
                        com.compilandia.platform.Lembrete.agenda(true, h)
                    }
                }
            }
        } else Text(tx("lembrete_so_celular"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
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
