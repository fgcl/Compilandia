package com.compilandia.ui.room

import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.mutableStateOf
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
import com.compilandia.learning.FREEZE_PRICE
import com.compilandia.learning.Quest
import com.compilandia.learning.RoomItem
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.resources.Res
import com.compilandia.resources.prop_gear
import com.compilandia.resources.room_base
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.compilandia.textos.tx

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
    var gaveta by remember { mutableStateOf(false) }
    var metasAbertas by remember { mutableStateOf(false) }
    val ladoALado = com.compilandia.ui.LocalJanela.current.ladoALado
    val cumpridas = quests.count { it.progress >= 1f }
    // T2: as três metas viram um anel; o detalhe abre ao toque.
    // Na janela larga a lista fica sempre aberta: sem seta e sem toque.
    val metas: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(8.dp))
                .then(if (ladoALado) Modifier else Modifier.clickable { metasAbertas = !metasAbertas }).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetasAnel(quests.map { it.progress.coerceIn(0f, 1f) }.average().toFloat(), Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(tx("sala_metas", cumpridas, quests.size), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft, modifier = Modifier.weight(1f))
            if (!ladoALado) com.compilandia.ui.SetaDeAbrir(metasAbertas)
        }
    }
    val proximo: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 14.dp, vertical = 10.dp)) {
            // O Continuar segue a trilha do assunto que o modelo do aluno escolheu.
            val passo = remember(ls) { com.compilandia.content.Trilha.proximo(l, next.skill, now) }
            Text(tx("sala_proximo", next.skill.title, passo?.titulo ?: next.game.title), style = MaterialTheme.typography.titleMedium, maxLines = if (ladoALado) 3 else 2)
            Spacer(Modifier.height(8.dp))
            BigButton(tx("comum_continuar"), Modifier.fillMaxWidth()) {
                if (passo != null) com.compilandia.ui.screens.abrirPasso(app, next.skill, passo)
                else app.go(Screen.Play(next.skill, next.game, next.level, seed = nowMs()))
            }
        }
    }
    val sala: @Composable (Modifier) -> Unit = { mod ->
        BoxWithConstraints(mod.clipToBounds(), contentAlignment = Alignment.Center) {
            // A sala passa um pouco das laterais: as paredes externas não têm nada para tocar.
            val side = minOf(maxWidth * 1.26f, maxHeight * 1.04f)
            RoomScene(app, side, desafioPendente = ls.desafioDia != now / 86_400_000L) { gaveta = true }
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
            RoomHud(app)
            if (ladoALado) {
                // T1: janela larga ou celular deitado. A sala à esquerda; à direita, de cima para baixo: o próximo passo, os lugares e as metas.
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    sala(Modifier.weight(1f).fillMaxHeight())
                    Column(Modifier.width(if (com.compilandia.ui.LocalJanela.current.faixa == com.compilandia.ui.Faixa.EXPANDIDA) 340.dp else 280.dp).fillMaxHeight().background(Palette.paper)) {
                        proximo()
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            metas()
                            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (q in quests) QuestChip(q, Modifier.fillMaxWidth(), Palette.paperDark)
                            }
                        }
                    }
                }
            } else {
                sala(Modifier.weight(1f).fillMaxWidth())
                Column(Modifier.fillMaxWidth().background(Palette.paper)) {
                    proximo()
                    metas()
                    if (metasAbertas) Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (q in quests) QuestChip(q, Modifier.weight(1f), Palette.paperDark)
                    }
                }
            }
        }
        if (gaveta) GavetaJogar(app, now) { gaveta = false }
    }
}

/** Anel das metas do dia: enche conforme a média das três. */
@Composable
private fun MetasAnel(progresso: Float, modifier: Modifier) {
    val trilho = Palette.paperDark
    val cor = if (progresso >= 1f) Palette.green else Palette.hazard
    androidx.compose.foundation.Canvas(modifier) {
        val traco = size.minDimension * 0.18f
        drawArc(trilho, 0f, 360f, false, style = Stroke(traco))
        drawArc(cor, -90f, 360f * progresso, false, style = Stroke(traco, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}

/** A gaveta Jogar: o que não é a trilha fica aqui, a um toque. */
@Composable
private fun GavetaJogar(app: AppState, now: Long, onFechar: () -> Unit) {
    val hoje = now / 86_400_000L
    Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f)).clickable(remember { MutableInteractionSource() }, null) { onFechar() }) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Palette.paper).clickable(remember { MutableInteractionSource() }, null) {}.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(tx("sala_jogar"), style = MaterialTheme.typography.headlineSmall)
            BigButton(tx("desafioscreen_desafio_dia"), Modifier.fillMaxWidth(), color = if (app.ls.desafioDia != hoje) Palette.hazard else Palette.drawer, small = true) { onFechar(); app.go(Screen.Desafio) }
            BigButton(tx("sala_programar"), Modifier.fillMaxWidth(), color = Palette.drawer, small = true) { onFechar(); app.go(Screen.Bancadas) }
            BigButton(tx("sala_corrida"), Modifier.fillMaxWidth(), color = Palette.drawer, small = true) { onFechar(); app.go(Screen.Corrida) }
            BigButton(tx("comum_fabrica_gramaticas"), Modifier.fillMaxWidth(), color = Palette.drawer, small = true) { onFechar(); app.go(Screen.FabGram) }
            BigButton(tx("albumscreen_album_bugs"), Modifier.fillMaxWidth(), color = Palette.drawer, small = true) { onFechar(); app.go(Screen.Album) }
            BigButton(tx("sala_aulas_30"), Modifier.fillMaxWidth(), color = Palette.drawer, small = true) { onFechar(); app.go(Screen.Feed) }
            Text(
                tx("sala_fechar"), color = Palette.inkSoft, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onFechar() }.padding(8.dp),
            )
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
        Image(painterResource(Res.drawable.prop_gear), tx("comum_engrenagens"), Modifier.size(24.dp))
        HudText("${ls.gears}")
        Spacer(Modifier.weight(1f))
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
private fun QuestChip(q: Quest, modifier: Modifier, fundo: Color = Palette.paper) {
    Column(modifier.clip(RoundedCornerShape(6.dp)).background(fundo).padding(horizontal = 8.dp, vertical = 5.dp)) {
        Text(q.title, style = MaterialTheme.typography.labelSmall, color = Palette.ink, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        ProgressBar(q.progress, if (q.progress >= 1f) Palette.green else Palette.hazard, Modifier.fillMaxWidth(), 5.dp)
    }
}

@Composable
private fun RoomScene(app: AppState, side: Dp, desafioPendente: Boolean, onJogar: () -> Unit) {
    val l = app.learner
    val ls = app.ls
    val scope = rememberCoroutineScope()
    var hop by remember { mutableIntStateOf(0) }
    Box(Modifier.requiredSize(side)) {
        Image(painterResource(Res.drawable.room_base), tx("roomscreen_oficina_byte"), Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        // Só o que você já tem. O que falta comprar fica na Loja, não no meio da sala.
        for ((id, layer) in roomLayers) {
            val item = RoomItem.entries.firstOrNull { it.id == id } ?: continue
            if (l.owns(item)) RoomLayerImage(layer, side, popIn = app.justBought == id) { if (app.justBought == id) app.justBought = null }
        }
        // os móveis são os botões: gaveteiro, monitor, quadro e caixas
        MoveisDaSala(app, side, desafioPendente, onJogar)
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

/** Loja da oficina: cada peça entra na sala assim que você compra. */
@Composable
fun ShopScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    Column(Modifier.fillMaxSize().background(Palette.paper)) {
        Header(tx("roomscreen_loja_oficina"), tx("roomscreen_voce_tem_1_engrenagens", ls.gears), Palette.drawer)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PaperCard(Modifier.fillMaxWidth(), border = Palette.orange) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Flame(Modifier.size(24.dp, 28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tx("roomscreen_trava_sequencia"), style = MaterialTheme.typography.titleSmall)
                        Text(tx("roomscreen_guarda_sua_sequencia_se_faltar", ls.streakFreezes), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
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
                        if (owned) Text(tx("roomscreen_na_sala"), color = Palette.green, style = MaterialTheme.typography.labelMedium)
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
