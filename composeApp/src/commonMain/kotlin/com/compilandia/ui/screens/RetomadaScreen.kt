package com.compilandia.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.compilandia.content.Falas
import com.compilandia.content.Retomada
import com.compilandia.content.Retomadas
import com.compilandia.content.Trilha
import com.compilandia.platform.nowMs
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.Header
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.voice.Narrator

/**
 * R1: a retomada. O Byte lembra o que a pessoa viu da última vez e faz três perguntas rápidas
 * das ideias mais esquecidas (R3). Depois, o Continuar segue de onde ela parou.
 */
@Composable
fun RetomadaScreen(app: AppState, r: Retomada) {
    val l = app.learner
    var k by remember { mutableIntStateOf(0) }
    var resposta by remember { mutableStateOf<Boolean?>(null) }
    var acertos by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { Narrator.say(Falas.retomada) }
    Column(Modifier.fillMaxSize()) {
        Header(tx("retomada_titulo"), tx("retomada_sub"), Palette.hazard)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ByteFigure(Modifier.width(64.dp), mood = if (k >= r.itens.size) Mood.HAPPY else Mood.NEUTRAL, animate = !ThemeState.reduceMotion)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(Retomadas.abertura(r), style = MaterialTheme.typography.titleMedium)
                    Text(Falas.retomada.text, style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
                }
            }
            if (k < r.itens.size) {
                val item = r.itens[k]
                Text(tx("retomada_n", k + 1, r.itens.size) + " · " + item.skill.title, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                PaperCard(Modifier.fillMaxWidth(), border = Palette.region(item.skill.region)) {
                    Text(tx("retomada_pergunta"), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                    RichText(item.afirma.texto, style = MaterialTheme.typography.titleMedium)
                }
                val resp = resposta
                if (resp == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (diz in listOf(true, false)) {
                            BigButton(if (diz) tx("retomada_verdade") else tx("retomada_mentira"), Modifier.weight(1f), color = if (diz) Palette.teal else Palette.white, textColor = Palette.ink) {
                                val certo = diz == item.afirma.verdade
                                resposta = diz
                                if (certo) { acertos++; Juice.correct() } else Juice.wrong()
                                l.revisaIdeia(item.id, item.skill, if (certo) 1.0 else 0.0, nowMs())
                                app.commit()
                            }
                        }
                    }
                } else {
                    val certo = resp == item.afirma.verdade
                    FeedbackBanner(certo, (if (item.afirma.verdade) tx("retomada_era_verdade") else tx("retomada_era_mentira")) + " " + item.afirma.porque)
                    BigButton(if (k + 1 < r.itens.size) tx("retomada_proxima") else tx("retomada_fim"), Modifier.fillMaxWidth()) {
                        resposta = null
                        k++
                    }
                }
            } else {
                PaperCard(Modifier.fillMaxWidth(), border = Palette.green) {
                    Text(tx("retomada_resultado", acertos, r.itens.size), style = MaterialTheme.typography.titleMedium)
                    Text(tx("retomada_resultado_sub"), style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
                }
                BigButton(tx("retomada_seguir"), Modifier.fillMaxWidth(), color = Palette.hazard, textColor = Palette.hazardDark) {
                    Narrator.stop()
                    app.back()
                    val agora = nowMs()
                    val next = l.nextStep(agora)
                    val passo = Trilha.proximo(l, next.skill, agora)
                    if (passo != null) abrirPasso(app, next.skill, passo) else app.go(Screen.Play(next.skill, next.game, next.level, seed = agora))
                }
            }
            if (k < r.itens.size) {
                Text(
                    tx("retomada_pular"), color = Palette.inkSoft, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { Narrator.stop(); app.back() }.padding(4.dp),
                )
            }
        }
    }
}
