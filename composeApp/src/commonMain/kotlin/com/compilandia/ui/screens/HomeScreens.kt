package com.compilandia.ui.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Library
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.BigButton
import com.compilandia.ui.BoltIcon
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.Drawer
import com.compilandia.ui.DrawerBin
import com.compilandia.ui.GearIcon
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Header
import com.compilandia.ui.MasteryRing
import com.compilandia.ui.Palette
import com.compilandia.ui.OpenDrawer
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.ToolTag
import com.compilandia.ui.VSpace
import com.compilandia.ui.XpGauge
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.pegboard
import kotlin.math.sqrt
import com.compilandia.textos.tx

fun playerLevel(xp: Int): Int = sqrt(xp / 60.0).toInt() + 1
fun xpForLevel(level: Int): Int = (level - 1) * (level - 1) * 60

// ---------------------------------------------------------------- Onboarding

@Composable
fun OnboardingScreen(app: AppState) {
    val lines = com.compilandia.content.Falas.boas
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(app.ls.name) }
    var hop by remember { mutableIntStateOf(0) }
    val line = lines[page]
    val asksName = page == 3
    androidx.compose.runtime.LaunchedEffect(page) {
        hop++
        com.compilandia.ui.voice.Narrator.say(line)
    }
    Column(Modifier.fillMaxSize().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f))) {
        ProgressBar((page + 1f) / lines.size, Palette.hazard, Modifier.fillMaxWidth(), 8.dp)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Row(verticalAlignment = Alignment.Bottom) {
                ByteFigure(Modifier.width(210.dp), if (page == 0 || page == 4) Mood.HAPPY else Mood.NEUTRAL, lookX = if (line.who == com.compilandia.content.Speaker.PROF) 0.7f else 0f, hop = hop)
                androidx.compose.animation.AnimatedVisibility(page in 2..3) { com.compilandia.ui.cast.ProfFigure(Modifier.size(150.dp), lookX = -0.5f) }
            }
        }
        HazardStripe()
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(line.who.displayName, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            RichText(line.text, style = MaterialTheme.typography.bodyLarge)
            if (asksName) OutlinedTextField(name, { name = it.take(24) }, label = { Text(tx("homescreens_seu_nome")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            BigButton(if (page < lines.lastIndex) tx("comum_continuar") else tx("homescreens_abrir_oficina"), Modifier.fillMaxWidth()) {
                com.compilandia.ui.voice.Narrator.stop()
                if (page < lines.lastIndex) page++ else {
                    app.learner.setName(name.trim())
                    app.learner.finishOnboarding()
                    app.commit()
                    app.stack.clear(); app.stack.add(Screen.Room)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Mapa

@Composable
fun MapScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    val now = remember { nowMs() }
    val due = remember(ls) { l.dueSkills(now) }
    val recommended = remember(ls) { l.nextMission(now).skill.region }
    var openRegion by remember(recommended) { mutableStateOf(recommended) }
    val lvl = playerLevel(ls.xp)
    val levelProgress = (ls.xp - xpForLevel(lvl)).toFloat() / (xpForLevel(lvl + 1) - xpForLevel(lvl))
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().background(Palette.paper).pegboard(Palette.drawerHi.copy(alpha = 0.22f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ByteFigure(Modifier.height(70.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tx("homescreens_compilandia"), style = MaterialTheme.typography.displaySmall)
                    Text(tx("homescreens_bancada_1", ls.name.ifBlank { "aventureiro(a)" }), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                    Text(tx("homescreens_nivel_1_2_dia_seguidos", lvl, ls.streakDays), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                }
                XpGauge(ls.xp, levelProgress)
            }
            HazardStripe()
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val dExam = l.daysToExam(now)
                if (dExam != null) PaperCard(color = Palette.red.copy(alpha = 0.08f), border = Palette.red) {
                    Text(if (dExam == 0L) tx("homescreens_prova_hoje") else tx("homescreens_prova_em_1_dia", dExam), style = MaterialTheme.typography.titleSmall, color = Palette.red)
                    val plan = remember(ls) { l.examPlan(now) }
                    RichText(
                        when {
                            dExam <= 1 -> tx("homescreens_modo_vespera_revisao_relampago_so")
                            dExam <= 7 -> tx("homescreens_faca_1_missoes_intercaladas_por", if (dExam <= 3) 3 else 2, plan.take(3).joinToString { it.title })
                            else -> tx("homescreens_ritmo_tranquilo_1_missao_por", plan.take(3).joinToString { it.title })
                        }, style = MaterialTheme.typography.bodySmall,
                    )
                }
                Region.entries.forEachIndexed { i, r ->
                    RegionDrawer(app, r, i + 1, open = r == openRegion, recommended = r == recommended) {
                        Sound.play(Sfx.CLICK)
                        openRegion = r
                    }
                }
                // A5: a série em C e o Guia do TP numa trilha só, aberta depois da Floresta Léxica.
                val compiladorAberto = remember(ls) { com.compilandia.content.SeuCompilador.aberta(l) }
                Drawer(
                    tx("seucompilador_titulo"), note = if (compiladorAberto) null else tx("seucompilador_trancado"), dimmed = !compiladorAberto,
                ) {
                    if (compiladorAberto) app.go(Screen.SeuCompilador) else app.say(tx("seucompilador_trancado"))
                }
            }
        }
        // Barra fixa: as ferramentas ficam sempre à mão, sem rolar as gavetas.
        Column(Modifier.fillMaxWidth().background(Palette.paperDark).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToolTag(if (due.isEmpty()) tx("homescreens_revisao") else tx("comum_revisar_1", due.size), highlighted = due.isNotEmpty()) {
                    val skills = due.map { it.first }.ifEmpty { Skill.entries.filter { l.skill(it).attempts > 0 }.ifEmpty { listOf(Skill.PHASES) } }
                    app.go(Screen.Flashcards(skills))
                }
                ToolTag(tx("comum_laboratorio")) { app.go(Screen.Lab) }
                ToolTag(tx("comum_painel")) { app.go(Screen.Brain) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BigButton(
                    tx("homescreens_missao_4_desafios"), Modifier.weight(1f),
                    leading = { BoltIcon(Palette.on(Palette.action)) },
                ) {
                    val mix = l.dailyMix(nowMs(), 4)
                    val first = mix.first()
                    app.go(Screen.Play(first.skill, first.game, first.level, mix.drop(1), nowMs()))
                }
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Palette.tag).clickable { app.go(Screen.Settings) },
                    contentAlignment = Alignment.Center,
                ) { GearIcon(Palette.tagInk) }
            }
        }
    }
}

@Composable
private fun RegionDrawer(app: AppState, r: Region, number: Int, open: Boolean, recommended: Boolean, onOpen: () -> Unit) {
    val skills = Skill.entries.filter { it.region == r }
    val percent = (app.learner.regionProgress(r) * 100).toInt()
    val bossDown = r.name in app.ls.bossesDefeated
    if (open) {
        val cor = Palette.region(r)
        OpenDrawer("$number ${r.title}", tx("homescreens_entrar"), onClick = { com.compilandia.ui.fx.Fx.porta(r.title, cor) { app.go(Screen.RegionView(r)) } }) {
            DrawerBin("$percent%", if (recommended) tx("homescreens_proxima") else tx("comum_dominio"))
            DrawerBin("${skills.size}", tx("homescreens_assuntos"))
            DrawerBin("${GameId.entries.count { g -> g != GameId.FLASHCARDS && g.skills.any { it in skills } }}", tx("homescreens_jogos"))
        }
    } else {
        Drawer(
            "$number ${r.title}",
            note = if (bossDown) tx("homescreens_vencida") else if (percent > 0) "$percent%" else null,
            dimmed = skills.none { app.learner.prereqsMet(it) },
            onClick = onOpen,
        )
    }
}

// ---------------------------------------------------------------- Região

@Composable
fun RegionScreen(app: AppState, region: Region) {
    val l = app.learner
    val ls = app.ls
    val color = Palette.region(region)
    val skills = Skill.entries.filter { it.region == region }
    // O destaque vai para o primeiro assunto com trilha por terminar.
    val nextHere = remember(ls) { skills.firstOrNull { !com.compilandia.content.Trilha.completa(l, it) } }
    val faixa = com.compilandia.ui.LocalJanela.current.faixa
    var escolhido by remember { mutableStateOf(nextHere ?: skills.first()) }
    val extras: @Composable () -> Unit = {
        if (region == com.compilandia.learning.Region.OFICINA_BISON) {
            PaperCard(Modifier.fillMaxWidth(), color = Palette.white, border = Palette.teal) {
                Text(tx("comum_laboratorio_bison"), style = MaterialTheme.typography.titleSmall)
                Text(tx("homescreens_mexa_no_y_arraste_precedencia"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                BigButton(tx("homescreens_abrir_laboratorio"), Modifier.fillMaxWidth(), color = Palette.teal) { app.go(Screen.LabBison()) }
            }
        }
        val boss = com.compilandia.learning.Boss.of(region)
        val beaten = region.name in ls.bossesDefeated
        PaperCard(Modifier.fillMaxWidth(), color = Palette.white, border = Palette.yellow) {
            Text(if (beaten) tx("comum_maquina_religada") else tx("homescreens_chefao_1", boss.bossName), style = MaterialTheme.typography.titleSmall)
            BigButton(if (beaten) tx("homescreens_religar_novo") else tx("homescreens_enfrentar"), Modifier.fillMaxWidth(), color = Palette.hazard) { app.go(Screen.BossFight(region)) }
        }
    }
    Column(Modifier.fillMaxSize()) {
        Header(region.title, region.tagline, color)
        // Janela baixa (celular deitado): a faixa com o desenho sai para sobrar lugar para a lista.
        if (com.compilandia.ui.LocalJanela.current.altura >= 480.dp) com.compilandia.ui.SceneBanner(com.compilandia.ui.regionArt(region), 110) {
            ByteFigure(Modifier.align(Alignment.BottomStart).padding(start = 16.dp).height(96.dp), lookX = 0.6f)
        }
        if (faixa == com.compilandia.ui.Faixa.EXPANDIDA) {
            // T1: janela larga. Os assuntos à esquerda; a trilha do escolhido à direita.
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.width(380.dp).fillMaxHeight().background(Palette.paperDark)) {
                    AssuntosDaRegiao(app, region, escolhido, Modifier.weight(1f).fillMaxWidth()) { escolhido = it }
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { extras() }
                }
                TrilhaCorpo(app, escolhido, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (s in skills) SkillRow(app, s, color, s == nextHere, etapasAbertas = faixa == com.compilandia.ui.Faixa.MEDIA)
                extras()
                VSpace(24)
            }
        }
    }
}

/** Um assunto na região: anel de domínio, quantas etapas faltam e um botão só. Tocar no cartão abre a trilha. */
@Composable
private fun SkillRow(app: AppState, s: Skill, color: Color, isNext: Boolean, etapasAbertas: Boolean = false) {
    val l = app.learner
    val ls = app.ls
    val passos = remember(s) { com.compilandia.content.Trilha.passos(s) }
    val feitas = remember(ls) { com.compilandia.content.Trilha.feitas(l, s) }
    val proximo = remember(ls) { com.compilandia.content.Trilha.proximo(l, s) }
    PaperCard(
        Modifier.fillMaxWidth().clickable { app.go(Screen.Trilha(s)) },
        color = if (isNext) Palette.hazard.copy(alpha = 0.14f) else Palette.white, border = if (isNext) Palette.hazard else Palette.ink,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MasteryRing(l.mastery(s), color)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(s.title, style = MaterialTheme.typography.titleSmall)
                Text(tx("trilha_etapas_feitas", feitas, passos.size), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
            }
        }
        // Faixa média: as etapas aparecem abertas, cada uma tocável.
        if (etapasAbertas) androidx.compose.foundation.layout.FlowRow(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (p in passos) {
                val estado = com.compilandia.content.Trilha.estado(l, s, p)
                val feita = estado == com.compilandia.content.EstadoEtapa.FEITA
                val atual = p == proximo
                Text(
                    (if (feita) "✓ " else "") + p.titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (atual) Palette.on(Palette.hazard) else if (feita) Palette.green else Palette.ink,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (atual) Palette.hazard else Palette.paperDark)
                        .clickable { abrirPasso(app, s, p) }.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        if (proximo != null) {
            BigButton(tx("trilha_continuar", proximo.titulo), Modifier.fillMaxWidth(), color = if (isNext) Palette.action else Palette.drawer, small = true) {
                abrirPasso(app, s, proximo)
            }
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}
