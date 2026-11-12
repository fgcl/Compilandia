package com.compilandia.ui.screens

import com.compilandia.ui.larguraDeLeitura
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.compilandia.content.Etapa
import com.compilandia.content.EstadoEtapa
import com.compilandia.content.PassoTrilha
import com.compilandia.content.SeuCompilador
import com.compilandia.content.Trilha
import com.compilandia.learning.Skill
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.MasteryRing
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.Screen
import com.compilandia.ui.VSpace

/** Abre a tela de uma etapa. [troca] substitui a tela atual em vez de empilhar (fim de aula indo para a próxima etapa). */
fun abrirPasso(app: AppState, s: Skill, p: PassoTrilha, troca: Boolean = false) {
    val l = app.learner
    val bug = Trilha.passos(s).firstOrNull { it.etapa == Etapa.BUG }
    val alvo = when (p.etapa) {
        Etapa.BUG -> Screen.Episode(s)
        // Quem já passou pelo Bug, ou pulou, vai direto para a aula.
        Etapa.VER -> Screen.Episode(s, abertura = bug != null && Trilha.estado(l, s, bug) == EstadoEtapa.PENDENTE)
        Etapa.PRATICA, Etapa.PEGADINHA -> Screen.Episode(s, id = p.episodio?.id)
        Etapa.FAZER -> Screen.FazerComAjuda(s, semente = nowMs())
        Etapa.PROGRAMAR -> com.compilandia.content.Robo.de(s)?.let { Screen.Bancada(it) } ?: return
        Etapa.FABRICA -> {
            // Derivação abre o tutorial onde parou; ambiguidade e recursão abrem o desafio do assunto.
            val m = Trilha.missaoDaFabrica(s) ?: return
            val tutorial = com.compilandia.tutor.Missao.entries.take(com.compilandia.tutor.TUTORIAL)
            app.fabGramPedida = if (m.desafio) m.name else (tutorial.getOrNull(l.state.fabGramMissoes) ?: tutorial.first()).name
            Screen.FabGram
        }
        Etapa.REVISAO -> {
            // R2: grava o formato usado, para a próxima revisão escolher outro.
            val f = p.formato ?: com.compilandia.content.FormatoRevisao.CLIPE
            l.usouFormato(s, f.name, nowMs())
            app.commit()
            val jogo = Screen.Play(s, l.chooseGame(s), l.chooseLevel(s, nowMs()), seed = nowMs())
            when (f) {
                com.compilandia.content.FormatoRevisao.CLIPE, com.compilandia.content.FormatoRevisao.PEGADINHA ->
                    p.episodio?.let { Screen.Episode(s, id = it.id) } ?: jogo
                com.compilandia.content.FormatoRevisao.PREVER -> Screen.Exemplo(s)
                com.compilandia.content.FormatoRevisao.BUG -> com.compilandia.content.Ideias.de(s)
                    .minByOrNull { com.compilandia.content.Ideias.retencao(l, it, nowMs()) }?.let { Screen.CacaBug(it.especie.id) } ?: jogo
                com.compilandia.content.FormatoRevisao.ROBO -> com.compilandia.content.Robo.de(s)?.let { r ->
                    // Uma fase já feita, que gira a cada revisão.
                    val feitas = (0 until r.fases).filter { com.compilandia.content.Robos.feita(l, r, it) }
                    Screen.Bancada(r, feitas.getOrNull(l.skill(s).memory.reviews % feitas.size.coerceAtLeast(1)) ?: -1)
                } ?: jogo
                com.compilandia.content.FormatoRevisao.JOGO -> jogo
            }
        }
        Etapa.DESAFIO -> Screen.Play(s, l.chooseGame(s), l.chooseLevel(s, nowMs()), seed = nowMs())
    }
    if (troca) app.replace(alvo) else app.go(alvo)
}

/** A trilha de um assunto (A1): uma linha de etapas e um botão só. */
@Composable
fun TrilhaScreen(app: AppState, s: Skill) {
    val cor = Palette.region(s.region)
    val janela = com.compilandia.ui.LocalJanela.current
    Column(Modifier.fillMaxSize()) {
        Header(s.region.title, s.region.tagline, cor)
        if (janela.faixa == com.compilandia.ui.Faixa.EXPANDIDA) {
            // T1: janela larga. Os assuntos da região à esquerda, a trilha à direita.
            Row(Modifier.weight(1f).fillMaxWidth()) {
                AssuntosDaRegiao(app, s.region, s, Modifier.width(340.dp).fillMaxHeight()) { if (it != s) app.replace(Screen.Trilha(it)) }
                TrilhaCorpo(app, s, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            TrilhaCorpo(app, s, Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** O miolo da trilha: anel, etapas e o botão de seguir. A tela da trilha e a região larga usam o mesmo. */
@Composable
fun TrilhaCorpo(app: AppState, s: Skill, modifier: Modifier) {
    val l = app.learner
    val ls = app.ls
    val cor = Palette.region(s.region)
    val passos = remember(s) { Trilha.passos(s) }
    val proximo = remember(ls, s) { Trilha.proximo(l, s, nowMs()) }
    val feitas = remember(ls, s) { Trilha.feitas(l, s) }
    Column(modifier) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Column(Modifier.larguraDeLeitura()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MasteryRing(l.mastery(s), cor)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.headlineSmall)
                        Text(tx("trilha_etapas_feitas", feitas, passos.size), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                    }
                }
                VSpace(16)
                passos.forEachIndexed { i, p ->
                    EtapaLinha(app, s, p, ultima = i == passos.lastIndex, atual = p == proximo)
                }
                VSpace(24)
            }
        }
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Column(Modifier.larguraDeLeitura()) {
                if (proximo != null) {
                    // R2: a revisão não é uma etapa da lista, então o formato da vez aparece aqui.
                    if (proximo.etapa == Etapa.REVISAO) {
                        Text(proximo.subtitulo, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                        VSpace(6)
                    }
                    BigButton(tx("trilha_continuar", proximo.titulo), Modifier.fillMaxWidth()) { abrirPasso(app, s, proximo) }
                } else {
                    Text(tx("trilha_completa"), style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
                    VSpace(8)
                    val outro = remember(ls, s) { Trilha.proximoAssunto(l, s) }
                    if (outro != null) {
                        BigButton(tx("trilha_proximo_assunto", outro.title), Modifier.fillMaxWidth()) { app.replace(Screen.Trilha(outro)) }
                    } else {
                        BigButton(tx("trilha_treinar"), Modifier.fillMaxWidth()) { abrirPasso(app, s, passos.last()) }
                    }
                }
            }
        }
    }
}

/** A coluna de assuntos da região, na janela larga. O escolhido fica aceso. */
@Composable
fun AssuntosDaRegiao(app: AppState, region: com.compilandia.learning.Region, escolhido: Skill?, modifier: Modifier, onEscolhe: (Skill) -> Unit) {
    val l = app.learner
    val ls = app.ls
    val cor = Palette.region(region)
    val skills = remember(region) { Skill.entries.filter { it.region == region } }
    Column(modifier.background(Palette.paperDark).verticalScroll(rememberScrollState()).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (k in skills) {
            val aceso = k == escolhido
            val feitas = remember(ls, k) { Trilha.feitas(l, k) }
            val total = remember(k) { Trilha.passos(k).size }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (aceso) Palette.paper else Palette.paperDark)
                    .border(2.dp, if (aceso) Palette.hazard else Palette.paperDark, RoundedCornerShape(10.dp))
                    .clickable { onEscolhe(k) }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MasteryRing(l.mastery(k), cor, size = 38.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(k.title, style = MaterialTheme.typography.titleSmall)
                    Text(tx("trilha_etapas_feitas", feitas, total), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
                }
            }
        }
    }
}

@Composable
private fun EtapaLinha(app: AppState, s: Skill, p: PassoTrilha, ultima: Boolean, atual: Boolean) {
    val l = app.learner
    val estado = Trilha.estado(l, s, p)
    val opcional = Trilha.opcional(l, p)
    val corBola = when {
        estado == EstadoEtapa.FEITA -> Palette.green
        atual -> Palette.hazard
        else -> Palette.paperDark
    }
    Row(Modifier.fillMaxWidth().clickable { abrirPasso(app, s, p) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(if (estado == EstadoEtapa.PULADA) Palette.paper else corBola)
                    .border(2.dp, if (estado == EstadoEtapa.PULADA) Palette.inkSoft else corBola, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when { estado == EstadoEtapa.FEITA -> "✓"; estado == EstadoEtapa.PULADA -> "–"; else -> "${Trilha.passos(s).indexOf(p) + 1}" },
                    color = if (estado == EstadoEtapa.FEITA || atual) Palette.on(corBola) else Palette.ink,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!ultima) Box(Modifier.width(2.dp).height(40.dp).background(if (estado == EstadoEtapa.FEITA) Palette.green else Palette.paperDark))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(p.titulo, style = MaterialTheme.typography.titleSmall, color = if (estado == EstadoEtapa.PENDENTE && !atual) Palette.inkSoft else Palette.ink)
            val nota = when {
                estado == EstadoEtapa.PULADA -> tx("trilha_pulada")
                opcional && estado == EstadoEtapa.PENDENTE -> tx("trilha_opcional")
                else -> null
            }
            Text(listOfNotNull(p.subtitulo.ifBlank { null }, nota).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            if (p.etapa == Etapa.VER) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        tx("trilha_ler_com_calma"), color = Palette.teal, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { app.go(Screen.Lesson(s, l.chooseStyle(s))) }.padding(vertical = 4.dp),
                    )
                    // A cola do assunto: a receita e o exemplo resolvido, sem rever a aula inteira.
                    var cola by remember { mutableStateOf(false) }
                    if (Cola.receita(s).isNotEmpty()) Text(
                        tx("trilha_cola"), color = Palette.teal, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { cola = true }.padding(vertical = 4.dp).testTag("abre-cola"),
                    )
                    if (cola) ColaDialog(
                        s, onFechar = { cola = false }, onAula = { cola = false; app.go(Screen.Episode(s)) },
                        onPraticar = { cola = false; app.go(Screen.Play(s, l.chooseGame(s), l.chooseLevel(s, nowMs()), seed = nowMs())) },
                    )
                    Trilha.outroJeito(s)?.let { e ->
                        Text(
                            tx("trilha_outro_jeito", e.title), color = Palette.teal, style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable { app.go(Screen.Episode(s, id = e.id)) }.padding(vertical = 4.dp),
                        )
                    }
                }
            }
            if (atual && p.etapa != Etapa.VER && p.etapa != Etapa.DESAFIO) {
                Text(
                    tx("trilha_pular"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable {
                        Sound.play(Sfx.CLICK)
                        l.marcaEtapa(s, p.chave, feita = false, nowMs())
                        app.commit()
                        app.say(tx("trilha_pulou_aviso"))
                    }.padding(vertical = 4.dp),
                )
            }
        }
    }
}

/** A trilha "Seu compilador" (A5): as aulas em C e o Guia do TP, intercalados. */
@Composable
fun SeuCompiladorScreen(app: AppState) {
    val ls = app.ls
    val paradas = remember { SeuCompilador.paradas() }
    Column(Modifier.fillMaxSize()) {
        Header(tx("seucompilador_titulo"), null, Palette.teal)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (p in paradas) {
                val feita = p.episodio?.let { it.id in ls.episodesSeen } ?: (p.capitulo!!.number in ls.guideRead)
                PaperCard(
                    Modifier.fillMaxWidth().clickable {
                        if (p.episodio != null) app.go(Screen.Episode(p.episodio.skill, id = p.episodio.id))
                        else app.go(Screen.GuideChapter(p.capitulo!!.number))
                    },
                    border = if (feita) Palette.green else Palette.ink,
                ) {
                    Text(
                        if (p.episodio != null) tx("seucompilador_aula_c") else tx("seucompilador_capitulo", p.capitulo!!.number),
                        style = MaterialTheme.typography.labelMedium, color = if (p.episodio != null) Palette.teal else Palette.hazard,
                    )
                    Text(p.titulo, style = MaterialTheme.typography.titleSmall)
                    if (feita) Text("✓", color = Palette.green, fontWeight = FontWeight.Bold)
                }
            }
            VSpace(24)
        }
    }
}

/** Fazer com ajuda (A3): o mesmo palco do Laboratório, com o Byte respondendo cada vez menos. */
@Composable
fun FazerComAjudaScreen(app: AppState, scr: Screen.FazerComAjuda) {
    val s = scr.skill
    var p by remember { androidx.compose.runtime.mutableIntStateOf(scr.comeca.coerceIn(0, 3)) }
    var terminou by remember(p) { androidx.compose.runtime.mutableStateOf(false) }
    val serie = remember(s, scr.semente) { com.compilandia.content.Guiado.problemas(s, scr.semente) }
    val sim = serie.getOrNull(p)
    // A abertura do problema tem voz; o que o motor gera na hora fica em legenda.
    LaunchedEffect(p) { com.compilandia.ui.voice.Narrator.say(com.compilandia.content.Falas.guiado[p.coerceIn(0, 3)]) }
    Column(Modifier.fillMaxSize()) {
        Header(tx("trilha_etapa_fazer"), s.title, Palette.region(s.region))
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(tx("guiado_problema", p + 1), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            Text(when (p) { 0 -> tx("guiado_nivel_0"); 1 -> tx("guiado_nivel_1"); 2 -> tx("guiado_nivel_2"); else -> tx("guiado_nivel_3") }, style = MaterialTheme.typography.titleSmall)
        }
        if (sim == null) {
            LaunchedEffect(Unit) { app.back() }
            return@Column
        }
        androidx.compose.runtime.key(p) {
            com.compilandia.ui.lab.SimPlayer(
                sim, Modifier.weight(1f).fillMaxWidth(),
                exigir = com.compilandia.content.Guiado.exigir(sim, p),
                onFim = { terminou = true },
            )
        }
        if (terminou) Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 14.dp, vertical = 10.dp)) {
            if (p < com.compilandia.content.Guiado.PROBLEMAS - 1) {
                BigButton(tx("guiado_proximo"), Modifier.fillMaxWidth()) { p++ }
            } else {
                BigButton(tx("guiado_concluir"), Modifier.fillMaxWidth()) {
                    app.learner.marcaEtapa(s, Etapa.FAZER.name, feita = true, nowMs())
                    app.commit()
                    val prox = Trilha.proximo(app.learner, s)
                    if (prox != null) abrirPasso(app, s, prox, troca = true) else app.back()
                }
            }
        }
    }
}

