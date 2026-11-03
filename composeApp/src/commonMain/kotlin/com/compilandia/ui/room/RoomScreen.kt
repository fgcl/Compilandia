package com.compilandia.ui.room

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.learning.DAILY_XP_GOAL
import com.compilandia.learning.FREEZE_PRICE
import com.compilandia.learning.Quest
import com.compilandia.learning.RoomItem
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.resources.Res
import com.compilandia.resources.prop_gear
import com.compilandia.resources.room_base
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.GearIcon
import com.compilandia.ui.Header
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.Screen
import com.compilandia.ui.ToolTag
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.voice.Narrator
import com.compilandia.content.Falas
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

// Centro da plataforma de recarga e pontos de toque, como fração da largura da sala.
private const val PAD_X = 0.542f
private const val PAD_Y = 0.664f

/** Tela inicial: a oficina do Byte. Tocar num móvel abre o que ele guarda; o resto da sala se compra jogando. */
@Composable
fun RoomScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    val now = remember(ls) { nowMs() }
    val next = remember(ls) { l.nextStep(now) }
    val quests = remember(ls) { l.quests(now) }
    val due = remember(ls) { l.dueSkills(now) }
    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        RoomHud(app)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
            // A sala passa um pouco das laterais: as paredes externas não têm nada para tocar.
            val side = minOf(maxWidth * 1.26f, maxHeight * 1.04f)
            RoomScene(app, side, due.size)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (q in quests) QuestChip(q, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val hoje = now / 86_400_000L
            ToolTag("Desafio", highlighted = ls.desafioDia != hoje) { app.go(Screen.Desafio) }
            ToolTag("Corrida") { app.go(Screen.Corrida) }
            ToolTag("Álbum") { app.go(Screen.Album) }
            ToolTag("30 s") { app.go(Screen.Feed) }
        }
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("Próximo", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            Text(
                next.skill.title + if (next.episode) ", aula animada" else ", ${next.game.title}",
                style = MaterialTheme.typography.titleMedium, maxLines = 1,
            )
            Spacer(Modifier.height(8.dp))
            BigButton("Continuar", Modifier.fillMaxWidth()) {
                if (next.episode) app.go(Screen.Episode(next.skill))
                else app.go(Screen.Play(next.skill, next.game, next.level, seed = nowMs()))
            }
        }
    }
}

@Composable
private fun RoomHud(app: AppState) {
    val ls = app.ls
    Row(
        Modifier.fillMaxWidth().background(Palette.hazardDark).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Flame(Modifier.size(20.dp, 24.dp))
        HudText("${ls.streakDays}")
        Image(painterResource(Res.drawable.prop_gear), "Engrenagens", Modifier.size(24.dp))
        HudText("${ls.gears}")
        Spacer(Modifier.weight(1f))
        Ring((ls.dailyXp.toFloat() / DAILY_XP_GOAL).coerceIn(0f, 1f), Modifier.size(32.dp))
        Column {
            Text("${ls.dailyXp}/$DAILY_XP_GOAL", color = Color(0xFFF2EEE3), style = MaterialTheme.typography.labelMedium)
        }
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).clickable { app.go(Screen.Settings) }, contentAlignment = Alignment.Center) {
            GearIcon(Color(0xFFF2EEE3))
        }
    }
}

@Composable
private fun HudText(t: String) = Text(t, color = Color(0xFFF2EEE3), style = MaterialTheme.typography.titleMedium)

@Composable
fun Flame(modifier: Modifier) {
    val c = Palette.orange
    Canvas(modifier) {
        val u = size.width / 22f
        drawPath(Path().apply {
            moveTo(11 * u, 0f)
            cubicTo(13 * u, 6 * u, 20 * u, 8 * u, 20 * u, 16 * u)
            cubicTo(20 * u, 22 * u, 15 * u, 26 * u, 11 * u, 26 * u)
            cubicTo(7 * u, 26 * u, 2 * u, 22 * u, 2 * u, 16 * u)
            cubicTo(2 * u, 12 * u, 4 * u, 10 * u, 6 * u, 8 * u)
            cubicTo(6 * u, 11 * u, 7 * u, 12 * u, 8 * u, 12 * u)
            cubicTo(9 * u, 8 * u, 9 * u, 4 * u, 11 * u, 0f)
        }, c)
    }
}

@Composable
private fun Ring(p: Float, modifier: Modifier) {
    val track = Palette.drawerLo
    val fill = Palette.hazard
    Canvas(modifier) {
        val w = size.width * 0.16f
        val r = size.width / 2 - w / 2
        val c = Offset(size.width / 2, size.height / 2)
        drawCircle(track, r, c, style = Stroke(w))
        drawArc(fill, -90f, 360f * p, false, Offset(w / 2, w / 2), androidx.compose.ui.geometry.Size(r * 2, r * 2), style = Stroke(w, cap = StrokeCap.Round))
    }
}

@Composable
private fun QuestChip(q: Quest, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(6.dp)).background(Palette.paper).padding(horizontal = 8.dp, vertical = 5.dp)) {
        Text(q.title, style = MaterialTheme.typography.labelSmall, color = Palette.ink, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        ProgressBar(q.progress, if (q.progress >= 1f) Palette.green else Palette.hazard, Modifier.fillMaxWidth(), 5.dp)
    }
}

@Composable
private fun RoomScene(app: AppState, side: Dp, dueCount: Int) {
    val l = app.learner
    val ls = app.ls
    val scope = rememberCoroutineScope()
    var hop by remember { mutableIntStateOf(0) }
    val nextGhosts = remember(ls) { RoomItem.entries.filter { !l.owns(it) }.take(3) }
    Box(Modifier.requiredSize(side)) {
        Image(painterResource(Res.drawable.room_base), "Oficina do Byte", Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        for ((id, layer) in roomLayers) {
            val item = RoomItem.entries.firstOrNull { it.id == id } ?: continue
            when {
                l.owns(item) -> RoomLayerImage(layer, side, popIn = app.justBought == id) { if (app.justBought == id) app.justBought = null }
                item in nextGhosts -> GhostLayer(layer, side, item, ls.gears >= item.price) { app.go(Screen.Shop) }
            }
        }
        // o Byte mora na plataforma de recarga, que pulsa como um carregador
        val charge by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse))
        val glow = Color(0xFFFFD98A)
        Canvas(Modifier.fillMaxSize()) {
            val rx = size.width * (0.078f + 0.006f * charge)
            val ry = rx * 0.6f
            drawOval(
                glow.copy(alpha = 0.25f + 0.45f * charge), Offset(size.width * PAD_X - rx, size.width * PAD_Y - ry),
                androidx.compose.ui.geometry.Size(rx * 2, ry * 2), style = Stroke(size.width * 0.005f),
            )
        }
        val w = side * 0.17f
        ByteFigure(
            Modifier.offset(side * PAD_X - w / 2, side * PAD_Y - w * 1.2f + side * 0.008f).width(w)
                .clickable(remember { MutableInteractionSource() }, null) {
                    hop++
                    Juice.click()
                    scope.launch { Narrator.say(Falas.toqueNoByte.random()) }
                },
            mood = if (ls.streakDays >= 2) Mood.HAPPY else Mood.NEUTRAL, hop = hop,
        )
        com.compilandia.ui.fx.SalaViva(app, side)
        Pin(side, 0.62f, 0.27f, "Mapa") { app.go(Screen.Map) }
        Pin(side, 0.27f, 0.40f, "Laboratório") { app.go(Screen.Lab) }
        Pin(side, 0.16f, 0.30f, "Painel") { app.go(Screen.Brain) }
        Pin(side, 0.68f, 0.13f, "Diário") { app.go(Screen.Journal) }
        Pin(side, 0.61f, 0.86f, "Loja") { app.go(Screen.Shop) }
        if (dueCount > 0) Pin(side, 0.16f, 0.66f, "Revisar $dueCount") {
            app.go(Screen.Flashcards(l.dueSkills(nowMs()).map { it.first }))
        }
    }
}

@Composable
private fun RoomLayerImage(layer: RoomLayer, side: Dp, popIn: Boolean, onDone: () -> Unit) {
    val s = remember { Animatable(if (popIn) 0f else 1f) }
    LaunchedEffect(popIn) {
        if (popIn) {
            Juice.levelUp()
            s.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            onDone()
        }
    }
    Image(
        painterResource(layer.art), null,
        Modifier.offset(side * layer.x, side * layer.y).size(side * layer.w, side * layer.h).scale(s.value),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun GhostLayer(layer: RoomLayer, side: Dp, item: RoomItem, canBuy: Boolean, onClick: () -> Unit) {
    val a by rememberInfiniteTransition().animateFloat(0.45f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse))
    Box(Modifier.offset(side * layer.x, side * layer.y).size(side * layer.w, side * layer.h).clickable(remember { MutableInteractionSource() }, null) { onClick() }) {
        Image(painterResource(layer.ghost), item.title, Modifier.fillMaxSize().alpha(if (canBuy) a else 0.55f), contentScale = ContentScale.FillBounds)
        Row(
            Modifier.align(Alignment.Center).clip(RoundedCornerShape(5.dp)).background(Palette.hazard).padding(horizontal = 5.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(painterResource(Res.drawable.prop_gear), null, Modifier.size(13.dp))
            Text("${item.price}", color = Palette.hazardDark, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Pin(side: Dp, fx: Float, fy: Float, label: String, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition().animateFloat(1f, 1.5f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))
    Row(
        Modifier.offset(side * fx, side * fy).clip(RoundedCornerShape(6.dp)).clickable { onClick() }.padding(3.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(11.dp).scale(pulse).clip(CircleShape).background(Palette.hazard))
        Text(
            label, color = Color(0xFFF2EEE3), fontFamily = LocalFonts.current.display, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Palette.hazardDark.copy(alpha = 0.85f)).padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

/** Loja da oficina: cada peça entra na sala assim que você compra. */
@Composable
fun ShopScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    Column(Modifier.fillMaxSize().background(Palette.paper)) {
        Header("Loja da oficina", "Você tem ${ls.gears} engrenagens", Palette.drawer)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PaperCard(Modifier.fillMaxWidth(), border = Palette.orange) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Flame(Modifier.size(24.dp, 28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Trava da sequência", style = MaterialTheme.typography.titleSmall)
                        Text("Guarda sua sequência se faltar um dia. Você tem ${ls.streakFreezes}.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    }
                    BigButton("$FREEZE_PRICE", small = true, enabled = ls.gears >= FREEZE_PRICE, color = Palette.hazard) {
                        if (l.buyFreeze()) { app.commit(); Sound.play(Sfx.STAR) }
                    }
                }
            }
            for (item in RoomItem.entries) {
                val owned = l.owns(item)
                val layer = roomLayers[item.id]
                PaperCard(Modifier.fillMaxWidth(), border = if (owned) Palette.green else Palette.ink) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (layer != null) Image(painterResource(layer.art), null, Modifier.size(56.dp), contentScale = ContentScale.Fit)
                        Spacer(Modifier.width(10.dp))
                        Text(item.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (owned) Text("na sala", color = Palette.green, style = MaterialTheme.typography.labelMedium)
                        else BigButton("${item.price}", small = true, enabled = ls.gears >= item.price, color = Palette.hazard) {
                            if (l.buy(item)) {
                                app.justBought = item.id
                                app.commit()
                                app.back()
                            }
                        }
                    }
                }
            }
        }
    }
}
