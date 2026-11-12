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
import androidx.compose.foundation.layout.RowScope

/**
 * Painel do cérebro enxuto (mural, rodada 5): três números no alto, o que fazer hoje em duas frases,
 * três conquistas e o domínio dos assuntos que você já tocou. A explicação de como o jogo aprende
 * (calibração, rede neural, estilos, jogos) fica recolhida.
 */
@Composable
fun BrainScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    val now = remember { nowMs() }
    val cal = remember(ls) { l.calibration() }
    val ach = remember(ls) { com.compilandia.learning.Achievements.all(l) }
    val tocados = remember(ls) { Skill.entries.filter { l.skill(it).attempts > 0 } }
    val fraca = remember(ls) { tocados.minOfOrNull { l.skill(it).memory.retrievability(now) } }
    var todasConquistas by remember { mutableStateOf(false) }
    var como by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Header(tx("toolscreens_painel_cerebro"), null, Palette.blue)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Numero(l.aiAccuracy()?.let { "${(it * 100).toInt()}%" } ?: "–", tx("painel_rede_acerta"))
                Numero("${ach.count { it.unlocked }}/${ach.size}", tx("painel_conquistas"))
                Numero(fraca?.let { "${(it * 100).toInt()}%" } ?: "–", tx("painel_memoria_fraca"))
            }
            PaperCard(Modifier.fillMaxWidth()) {
                Rotulo(tx("painel_hoje"))
                for (s in remember(ls) { Coach.insights(l, now).take(2) }) RichText(semEmoji(s), style = MaterialTheme.typography.bodyMedium)
            }
            PaperCard(Modifier.fillMaxWidth()) {
                Rotulo(tx("toolscreens_conquistas_1_2", ach.count { it.unlocked }, ach.size))
                val lista = ach.sortedByDescending { it.unlocked }
                for (a in if (todasConquistas) lista else lista.take(3)) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(15.dp)).background(if (a.unlocked) Palette.hazard else Palette.paperDark), contentAlignment = Alignment.Center) {
                        Text("${ach.indexOf(a) + 1}", color = if (a.unlocked) Palette.hazardDark else Palette.inkSoft, style = MaterialTheme.typography.labelLarge)
                    }
                    Column {
                        Text(a.title, style = MaterialTheme.typography.labelLarge, color = if (a.unlocked) Palette.ink else Palette.inkSoft)
                        if (todasConquistas) Text(a.description, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    }
                }
                Mais(if (todasConquistas) tx("painel_ver_menos") else tx("painel_ver_todas_1", ach.size)) { todasConquistas = !todasConquistas }
            }
            PaperCard(Modifier.fillMaxWidth()) {
                Rotulo(tx("toolscreens_dominio_memoria_por_assunto"))
                for (s in tocados.ifEmpty { Skill.entries.take(3) }) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(s.title, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    ProgressBar(l.skill(s).mastery.toFloat(), Palette.region(s.region), Modifier.width(120.dp), 8.dp)
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.paperDark).clickable { como = !como }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tx("painel_como"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                com.compilandia.ui.SetaDeAbrir(como)
            }
            if (como) {
    PaperCard {
        Text(tx("toolscreens_calibracao_voce_sabe_que_sabe"), style = MaterialTheme.typography.titleSmall)
        if (cal.samples == 0) Text(tx("toolscreens_responda_pergunta_confianca_antes_dos"), style = MaterialTheme.typography.bodySmall)
        else {
            for ((label, acc, n) in cal.buckets) if (n > 0) {
                Text(tx("toolscreens_1_acertou_2_3_vezes", label, (acc * 100).toInt(), n), style = MaterialTheme.typography.bodySmall)
                ProgressBar(acc.toFloat(), Palette.purple, Modifier.fillMaxWidth(), 8.dp)
            }
            Text(tx("toolscreens_indice_brier_1_0_perfeito", (cal.brier * 100).toInt() / 100.0) +
                (if (cal.bias > 0.15) tx("toolscreens_tendencia_confianca_demais") else if (cal.bias < -0.15) tx("toolscreens_tendencia_confianca_menos") else tx("toolscreens_bem_calibrado")),
                style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        }
    }
    PaperCard {
        Text(tx("toolscreens_rede_neural"), style = MaterialTheme.typography.titleSmall)
        RichText(
            tx("toolscreens_rede_10_entradas_12_neuronios") +
                tx("toolscreens_ela_olha_seu_dominio_estimado") +
                tx("toolscreens_jogo_usa_isso_para_escolher"),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(tx("toolscreens_treinada_com_1_jogadas_erro", ls.net.trainedSamples, (ls.net.runningLoss * 100).toInt() / 100.0), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        l.aiAccuracy()?.let { Text(tx("toolscreens_acertou_1_das_previsoes_recentes", (it * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft) }
    }
    PaperCard {
        Text(tx("toolscreens_estilos_explicacao_que_funcionam_para"), style = MaterialTheme.typography.titleSmall)
        for ((st, v) in l.styleRanking()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(st.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(170.dp))
                ProgressBar(v.toFloat(), Palette.pink, Modifier.weight(1f), 8.dp)
            }
        }
        Text(tx("toolscreens_calculado_com_thompson_sampling_jogo"), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
    }
    PaperCard {
        Text(tx("toolscreens_jogos_favoritos"), style = MaterialTheme.typography.titleSmall)
        for ((g, v) in l.gameRanking().filter { it.first != GameId.FLASHCARDS }.take(6)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(170.dp))
                ProgressBar(v.toFloat(), Palette.orange, Modifier.weight(1f), 8.dp)
            }
        }
    }
            }
            VSpace(30)
        }
    }
}

@Composable
private fun RowScope.Numero(valor: String, rotulo: String) {
    Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Palette.white).padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.headlineMedium, color = Palette.hazard)
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun Rotulo(t: String) = Text(t.uppercase(), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)

@Composable
private fun Mais(t: String, onClick: () -> Unit) = Text(t, color = Palette.inkSoft, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp))

/** Os conselhos vêm com um emoji na frente; no painel enxuto, só o texto. */
private fun semEmoji(s: String): String {
    var i = 0
    while (i < s.length && (s[i].isSurrogate() || s[i].code in 0x2190..0x2BFF || s[i].code == 0xFE0F || s[i].code == 0x200D)) i++
    return s.substring(i).trimStart()
}
