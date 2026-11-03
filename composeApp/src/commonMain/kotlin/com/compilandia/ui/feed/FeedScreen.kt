package com.compilandia.ui.feed

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.content.Episode
import com.compilandia.content.episodes.Episodes
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.Header
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.pegboard
import com.compilandia.ui.voice.Narrator
import kotlinx.coroutines.delay

/** Aulas de 30 segundos numa fila vertical. Os assuntos fracos vêm primeiro. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(app: AppState) {
    val fila = remember { Episodes.clipes.sortedBy { app.learner.mastery(it.skill) + (it.title.hashCode() and 7) * 0.02 } }
    val pager = rememberPagerState { fila.size }
    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        Header("30 segundos", "${pager.currentPage + 1} de ${fila.size}", Palette.drawer)
        VerticalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth().testTag("feed")) { i ->
            Clipe(app, fila[i], ativo = pager.currentPage == i)
        }
    }
}

@Composable
private fun Clipe(app: AppState, ep: Episode, ativo: Boolean) {
    var k by remember(ep) { mutableIntStateOf(0) }
    var rodada by remember(ep) { mutableIntStateOf(0) }
    var acabou by remember(ep) { mutableStateOf(false) }
    var hop by remember { mutableIntStateOf(0) }
    LaunchedEffect(ativo, rodada) {
        if (!ativo) return@LaunchedEffect
        acabou = false
        for (i in ep.passos.indices) {
            k = i
            val f = ep.passos[i].fala
            if (Narrator.canSpeak(f)) Narrator.say(f) else delay(1400L + f.text.length * 48L)
        }
        acabou = true
    }
    val passo = ep.passos[k]
    Box(Modifier.fillMaxSize().pegboard(Palette.drawerHi.copy(alpha = 0.14f))) {
        Column(Modifier.fillMaxSize()) {
            Text(ep.title, Modifier.fillMaxWidth().padding(start = 16.dp, end = 72.dp, top = 12.dp), style = MaterialTheme.typography.headlineSmall)
            Box(Modifier.weight(1f).fillMaxWidth().padding(end = 62.dp)) { MotionStage(passo.shot, Modifier.fillMaxSize()) }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 72.dp, bottom = 34.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ByteFigure(Modifier.width(70.dp), if (acabou) Mood.HAPPY else Mood.NEUTRAL, hop = hop)
                Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Palette.white).padding(10.dp)) {
                    RichText(passo.fala.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Lateral("Entendi", Palette.green) { hop++; Juice.correct(); app.learner.addXp(2); app.commit() }
            Lateral("Não entendi", Palette.yellow) {
                app.learner.naoEntendiFala(ep.skill, passo.fala.id, nowMs()); app.commit()
                app.go(Screen.Episode(ep.skill))
            }
            Lateral("De novo", Palette.teal) { Narrator.stop(); rodada++ }
        }
        if (acabou) Dica(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
    }
}

@Composable
private fun Lateral(rotulo: String, cor: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Column(Modifier.width(58.dp).clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).background(Palette.white), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
                val w = size.width
                val traco = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                val caminho = androidx.compose.ui.graphics.Path()
                when (rotulo) {
                    "Entendi" -> {
                        caminho.moveTo(w * 0.12f, w * 0.55f); caminho.lineTo(w * 0.4f, w * 0.82f); caminho.lineTo(w * 0.9f, w * 0.2f)
                        drawPath(caminho, cor, style = traco)
                    }
                    "Não entendi" -> {
                        caminho.moveTo(w * 0.22f, w * 0.3f)
                        caminho.cubicTo(w * 0.25f, -w * 0.05f, w * 0.85f, 0f, w * 0.78f, w * 0.34f)
                        caminho.cubicTo(w * 0.74f, w * 0.52f, w * 0.5f, w * 0.5f, w * 0.5f, w * 0.68f)
                        drawPath(caminho, cor, style = traco)
                        drawCircle(cor, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.92f))
                    }
                    else -> {
                        drawArc(cor, -60f, 300f, false, androidx.compose.ui.geometry.Offset(w * 0.1f, w * 0.1f), androidx.compose.ui.geometry.Size(w * 0.8f, w * 0.8f), style = traco)
                        caminho.moveTo(w * 0.72f, 0f); caminho.lineTo(w * 0.78f, w * 0.26f); caminho.lineTo(w * 0.5f, w * 0.26f)
                        drawPath(caminho, cor, style = traco)
                    }
                }
            }
        }
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun Dica(modifier: Modifier) {
    val y by rememberInfiniteTransition().animateFloat(0f, -7f, infiniteRepeatable(tween(520), RepeatMode.Reverse))
    Text("Deslize para o próximo", modifier.offset(y = (if (ThemeState.reduceMotion) 0f else y).dp), style = MaterialTheme.typography.labelMedium, color = Palette.hazard)
}

@Suppress("unused") private val keepH = Modifier.height(0.dp)
