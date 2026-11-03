package com.compilandia.ui.screens

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
            if (asksName) OutlinedTextField(name, { name = it.take(24) }, label = { Text("Seu nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            BigButton(if (page < lines.lastIndex) "Continuar" else "Abrir a oficina", Modifier.fillMaxWidth()) {
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
                    Text("Compilândia", style = MaterialTheme.typography.displaySmall)
                    Text("Bancada de ${ls.name.ifBlank { "aventureiro(a)" }}", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                    Text("Nível $lvl · ${ls.streakDays} dia(s) seguidos", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                }
                XpGauge(ls.xp, levelProgress)
            }
            HazardStripe()
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val dExam = l.daysToExam(now)
                if (dExam != null) PaperCard(color = Palette.red.copy(alpha = 0.08f), border = Palette.red) {
                    Text(if (dExam == 0L) "📅 A prova é HOJE!" else "📅 Prova em $dExam dia(s)", style = MaterialTheme.typography.titleSmall, color = Palette.red)
                    val plan = remember(ls) { l.examPlan(now) }
                    RichText(
                        when {
                            dExam <= 1 -> "Modo véspera: revisão relâmpago e só os pontos fracos. Durma bem — o sono consolida a memória!"
                            dExam <= 7 -> "Faça ${if (dExam <= 3) 3 else 2} missões intercaladas por dia. Foco: **${plan.take(3).joinToString { it.title }}**."
                            else -> "Ritmo tranquilo: 1 missão por dia + revisões que aparecerem. Pontos a reforçar: **${plan.take(3).joinToString { it.title }}**."
                        }, style = MaterialTheme.typography.bodySmall,
                    )
                }
                Region.entries.forEachIndexed { i, r ->
                    RegionDrawer(app, r, i + 1, open = r == openRegion, recommended = r == recommended) {
                        Sound.play(Sfx.CLICK)
                        openRegion = r
                    }
                }
                Drawer("Guia do Trabalho Prático") { app.go(Screen.Guide) }
            }
        }
        // Barra fixa: as ferramentas ficam sempre à mão, sem rolar as gavetas.
        Column(Modifier.fillMaxWidth().background(Palette.paperDark).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToolTag(if (due.isEmpty()) "Revisão" else "Revisar ${due.size}", highlighted = due.isNotEmpty()) {
                    val skills = due.map { it.first }.ifEmpty { Skill.entries.filter { l.skill(it).attempts > 0 }.ifEmpty { listOf(Skill.PHASES) } }
                    app.go(Screen.Flashcards(skills))
                }
                ToolTag("Laboratório") { app.go(Screen.Lab) }
                ToolTag("Painel") { app.go(Screen.Brain) }
                ToolTag("Diário") { app.go(Screen.Journal) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BigButton(
                    "Missão: 4 desafios", Modifier.weight(1f),
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
        OpenDrawer("$number ${r.title}", "entrar →", onClick = { com.compilandia.ui.fx.Fx.porta(r.title, cor) { app.go(Screen.RegionView(r)) } }) {
            DrawerBin("$percent%", if (recommended) "próxima" else "domínio")
            DrawerBin("${skills.size}", "assuntos")
            DrawerBin("${GameId.entries.count { g -> g != GameId.FLASHCARDS && g.skills.any { it in skills } }}", "jogos")
        }
    } else {
        Drawer(
            "$number ${r.title}",
            note = if (bossDown) "vencida" else if (percent > 0) "$percent%" else null,
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
    val nextHere = remember(ls) { skills.firstOrNull { it.name !in ls.episodesSeen } ?: skills.minByOrNull { l.mastery(it) } }
    Column(Modifier.fillMaxSize()) {
        Header(region.title, region.tagline, color)
        com.compilandia.ui.SceneBanner(com.compilandia.ui.regionArt(region), 110) {
            ByteFigure(Modifier.align(Alignment.BottomStart).padding(start = 16.dp).height(96.dp), lookX = 0.6f)
        }
        var temporada by remember { mutableStateOf(Temporada.BASICO) }
        // Só aparece a temporada que tem aula aqui. Com cinco etiquetas, a fila rola de lado.
        val temporadas = remember(region) {
            listOf(Temporada.BASICO) + listOf(Temporada.PEGADINHA, Temporada.PRATICA, Temporada.TABELAS, Temporada.EM_C).filter { Episodes.daRegiao(region, it).isNotEmpty() }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp).padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (t in temporadas) ToolTag(t.titulo, highlighted = t == temporada) { temporada = t; Sound.play(Sfx.CLICK) }
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (temporada != Temporada.BASICO) {
                val lista = Episodes.daRegiao(region, temporada)
                if (lista.isEmpty()) Text("Esta região ainda não tem aulas nesta temporada.", color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
                var proxima = true
                for (e in lista) {
                    val visto = e.id in ls.episodesSeen
                    AulaRow(app, e, destaque = !visto && proxima, visto)
                    if (!visto) proxima = false
                }
                VSpace(24)
                return@Column
            }
            for (s in skills) SkillRow(app, s, color, s == nextHere)
            if (region == com.compilandia.learning.Region.OFICINA_BISON) {
                PaperCard(Modifier.fillMaxWidth(), color = Palette.white, border = Palette.teal) {
                    Text("Laboratório Bison", style = MaterialTheme.typography.titleSmall)
                    Text("Mexa no .y, arraste a precedência e rode passo a passo.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    BigButton("Abrir o laboratório", Modifier.fillMaxWidth(), color = Palette.teal) { app.go(Screen.LabBison()) }
                }
            }
            val boss = com.compilandia.learning.Boss.of(region)
            val beaten = region.name in ls.bossesDefeated
            PaperCard(Modifier.fillMaxWidth(), color = Palette.white, border = Palette.yellow) {
                Text(if (beaten) "Máquina religada" else "Chefão: ${boss.bossName}", style = MaterialTheme.typography.titleSmall)
                BigButton(if (beaten) "Religar de novo" else "Enfrentar", Modifier.fillMaxWidth(), color = Palette.hazard) { app.go(Screen.BossFight(region)) }
            }
            VSpace(24)
        }
    }
}

/** Uma aula da temporada: miniatura de verdade, título, duração e o que ela pede. */
@Composable
private fun AulaRow(app: AppState, e: com.compilandia.content.Episode, destaque: Boolean, visto: Boolean) {
    val shot = e.passos[e.passos.size / 2].shot
    PaperCard(Modifier.fillMaxWidth().clickable { app.go(Screen.Episode(e.skill, id = e.id)) }, color = if (destaque) Palette.hazard.copy(alpha = 0.14f) else Palette.white, border = if (destaque) Palette.hazard else Palette.ink) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(96.dp, 74.dp).clip(RoundedCornerShape(7.dp)).background(Palette.codeBg)) {
                com.compilandia.ui.motion.MotionStage(shot, Modifier.fillMaxSize().padding(3.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(e.title, style = MaterialTheme.typography.titleSmall)
                val tipo = when {
                    e.passos.any { it.brinquedo != null } -> "você pilota"
                    e.passos.any { it.ask != null } -> "você prevê"
                    else -> "assista"
                }
                Text("${e.segundos} s · $tipo", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            }
            if (visto) Text("visto", color = Palette.green, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SkillRow(app: AppState, s: Skill, color: Color, isNext: Boolean) {
    val l = app.learner
    val st = l.skill(s)
    val seen = s.name in app.ls.episodesSeen
    var expanded by remember { mutableStateOf(false) }
    PaperCard(Modifier.fillMaxWidth(), color = if (isNext) Palette.hazard.copy(alpha = 0.14f) else Palette.white, border = if (isNext) Palette.hazard else Palette.ink) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MasteryRing(st.mastery, color)
            Spacer(Modifier.width(10.dp))
            Text(s.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(if (expanded) "menos" else "mais", color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { expanded = !expanded }.padding(6.dp))
        }
        BigButton(
            if (seen) "Praticar" else "Aula animada", Modifier.fillMaxWidth(), color = if (isNext) Palette.action else Palette.drawer, small = true,
        ) {
            if (seen) app.go(Screen.Play(s, l.chooseGame(s), l.chooseLevel(s, nowMs()), seed = nowMs()))
            else app.go(Screen.Episode(s))
        }
        if (expanded) {
            ChipRow {
                Chip("Aula animada", color = color) { app.go(Screen.Episode(s)) }
                for (style in Library.stylesFor(s)) Chip(style.title, color = color, selected = style.name in st.lessonsSeen) { app.go(Screen.Lesson(s, style)) }
            }
            ChipRow { for (g in GameId.forSkill(s)) Chip(g.title, color = Palette.orange) { app.go(Screen.Play(s, g, l.chooseLevel(s, nowMs()), seed = nowMs())) } }
            Text("Memória: ${(st.memory.retrievability(nowMs()) * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}
