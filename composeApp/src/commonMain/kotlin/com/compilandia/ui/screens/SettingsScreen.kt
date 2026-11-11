package com.compilandia.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeMode

/**
 * Ajustes em lista enxuta (mural, rodada 4): quatro grupos, uma linha por ajuste, o controle à direita.
 * Nada de explicação na tela; o que precisa de mais (prova, lembrete, sobre, apagar) abre embaixo da linha.
 */
@Composable
fun SettingsScreen(app: AppState) {
    var aberto by remember { mutableStateOf<String?>(null) }
    fun alterna(k: String) { aberto = if (aberto == k) null else k }
    val ls = app.ls
    Column(Modifier.fillMaxSize()) {
        Header(tx("toolscreens_ajustes"), null, Palette.drawer)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Grupo(tx("ajustes_aparencia")) {
                Linha(tx("toolscreens_tema")) { Temas(app) }
                Linha(tx("toolscreens_tamanho_texto")) {
                    Segmentos(listOf("A" to 0.9f, "A+" to 1f, "A++" to 1.15f, "A+++" to 1.3f), ls.fontScale) { app.learner.setFontScale(it); app.commit() }
                }
                Interruptor(tx("toolscreens_reduzir_animacoes_sem_confete_tremida"), ls.reduceMotion) { app.learner.setReduceMotion(it); app.commit() }
            }
            Grupo(tx("ajustes_som_voz")) {
                Interruptor(tx("toolscreens_sons"), ls.soundOn) { app.learner.setSound(it); app.commit() }
                Interruptor(tx("toolscreens_voz_byte"), ls.voiceOn) { app.learner.setVoice(it); app.commit() }
                Interruptor(tx("toolscreens_legenda_mesmo_com_voz"), ls.captionsOn) { app.learner.setCaptions(it); app.commit() }
            }
            Grupo(tx("ajustes_estudo")) {
                val dias = app.learner.daysToExam(nowMs())
                Linha(tx("toolscreens_data_prova"), onClick = { alterna("prova") }) {
                    Valor(if (dias == null) tx("ajustes_marcar") else tx("ajustes_em_dias_1", dias))
                }
                if (aberto == "prova") Abre { Prova(app, dias) }
                Linha(tx("lembrete_ajuste"), onClick = { alterna("lembrete") }) {
                    Valor(if (ls.lembreteLigado) tx("lembrete_hora_chip", ls.lembreteHora) else tx("lembrete_desligado"))
                }
                if (aberto == "lembrete") Abre { LembreteAjuste(app, cartao = false) }
                Interruptor(tx("toolscreens_perguntar_minha_confianca_antes_dos"), ls.askConfidence) { app.learner.setAskConfidence(it); app.commit() }
                Linha(tx("ajustes_idioma")) { Idiomas(app) }
            }
            Grupo(tx("ajustes_conta")) {
                if (ls.journal.isNotEmpty()) Linha(tx("diario_ver"), onClick = { app.go(Screen.Journal) }) { Valor("") }
                Linha(tx("toolscreens_rever_introducao"), onClick = { app.go(Screen.Onboarding) }) { Valor("") }
                Linha(tx("ajustes_sobre"), onClick = { alterna("sobre") }) { Valor("") }
                if (aberto == "sobre") Abre { Text(tx("toolscreens_sobre_creditos"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft) }
                Linha(tx("toolscreens_apagar_todo_progresso"), cor = Palette.red, onClick = { alterna("apagar") }) {}
                if (aberto == "apagar") Abre { Apagar(app) { aberto = null } }
            }
        }
    }
}

/** Um grupo: o nome em caixa alta e as linhas numa placa só. */
@Composable
private fun Grupo(nome: String, linhas: @Composable ColumnScope.() -> Unit) {
    Text(nome.uppercase(), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft, modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 6.dp))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.white), content = linhas)
}

/** Uma linha: o nome à esquerda, o controle à direita. Com [onClick], a linha inteira é tocável. */
@Composable
private fun Linha(nome: String, cor: Color = Palette.ink, onClick: (() -> Unit)? = null, controle: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).then(if (onClick != null) Modifier.clickable { Sound.play(Sfx.CLICK); onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(nome, style = MaterialTheme.typography.bodyLarge, color = cor, modifier = Modifier.weight(1f))
        controle()
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(Palette.paperDark))
}

@Composable
private fun Interruptor(nome: String, valor: Boolean, muda: (Boolean) -> Unit) = Linha(nome, onClick = { muda(!valor) }) { Switch(valor, muda) }

@Composable
private fun Valor(texto: String) {
    Text("$texto  ›".trimStart(), style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
}

@Composable
private fun Abre(conteudo: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(Palette.paperDark.copy(alpha = 0.5f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = conteudo)
}

/** Os cinco temas em bolinhas: metade fundo, metade a cor de ação do tema. */
@Composable
private fun Temas(app: AppState) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (t in ThemeMode.entries) {
            val sel = app.ls.theme == t.name
            val cs = ThemePreviewColors(t)
            Box(
                Modifier.size(30.dp).clip(CircleShape).border(if (sel) 3.dp else 1.dp, if (sel) Palette.hazard else Palette.inkSoft.copy(alpha = 0.4f), CircleShape)
                    .clickable { app.learner.setTheme(t.name); app.commit(); Sound.play(Sfx.CLICK) }.padding(if (sel) 4.dp else 1.dp).testTag("tema-${t.name.lowercase()}"),
            ) {
                Canvas(Modifier.fillMaxSize().clip(CircleShape)) {
                    drawRect(cs[0])
                    drawPath(Path().apply { moveTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close() }, cs[3])
                }
            }
        }
    }
}

@Composable
private fun <T> Segmentos(opcoes: List<Pair<String, T>>, atual: T, escolhe: (T) -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.paperDark).padding(2.dp)) {
        for ((rotulo, v) in opcoes) {
            val on = v == atual
            Text(
                rotulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                color = if (on) Palette.hazardDark else Palette.inkSoft,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) Palette.hazard else Color.Transparent)
                    .clickable { Sound.play(Sfx.CLICK); escolhe(v) }.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

/** S2: o idioma. A troca reabre o jogo, porque as aulas são montadas uma vez só. */
@Composable
private fun Idiomas(app: AppState) {
    val opcoes = com.compilandia.textos.Txt.idiomas.map { (if (it == "en") "EN" else "PT") to it }
    Segmentos(opcoes, com.compilandia.textos.Txt.idioma) { lingua ->
        if (com.compilandia.textos.Txt.idioma != lingua) {
            app.learner.setIdioma(lingua); app.commit()
            if (!com.compilandia.platform.Idioma.reabrir()) app.say(com.compilandia.textos.Txt.em(lingua, "ajustes_idioma_reabra"))
        }
    }
}

@Composable
private fun Prova(app: AppState, dias: Long?) {
    val hoje = nowMs() / 86_400_000L
    if (dias != null) Text(tx("toolscreens_prova_em_1_dia_jogo", dias), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for ((rotulo, delta) in listOf("-7" to -7, "-1" to -1, "+1" to 1, "+7" to 7)) BigButton(rotulo, Modifier.weight(1f), small = true, color = Palette.white, textColor = Palette.ink) {
            val base = dias ?: 14
            app.learner.setExamDay(hoje + (base + delta).coerceAtLeast(0)); app.commit()
        }
    }
    if (dias != null) Text(tx("toolscreens_remover_prova"), color = Palette.red, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { app.learner.setExamDay(-1); app.commit() })
}

@Composable
private fun Apagar(app: AppState, fecha: () -> Unit) {
    Text(tx("toolscreens_tem_certeza_isso_apaga_xp"), color = Palette.red, style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton(tx("toolscreens_cancelar"), Modifier.weight(1f), color = Palette.white, textColor = Palette.ink, small = true) { fecha() }
        BigButton(tx("comum_apagar"), Modifier.weight(1f), color = Palette.red, small = true) {
            app.learner.reset(); app.commit()
            app.say(tx("toolscreens_progresso_apagado_novo_comeco"))
            fecha()
        }
    }
}
