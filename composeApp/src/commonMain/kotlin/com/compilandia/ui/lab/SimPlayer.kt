package com.compilandia.ui.lab

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.tutor.Sim
import com.compilandia.ui.BigButton
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.motion.STAGE_W
import com.compilandia.ui.pegboard
import kotlinx.coroutines.delay

const val TIMELINE_TAG = "linha-do-tempo"

/**
 * Texto que aparece letra por letra, com os bipes do Byte. Serve para fala que nasce na hora,
 * que não tem áudio pronto. O espaço do texto inteiro fica reservado: nada pula na tela.
 */
@Composable
fun FalaDigitada(texto: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge) {
    var n by remember(texto) { mutableIntStateOf(if (ThemeState.reduceMotion) texto.length else 0) }
    LaunchedEffect(texto) {
        while (n < texto.length) {
            delay(22)
            n++
            val c = texto[n - 1]
            if (n % 3 == 0 && c.isLetter()) Sound.play(Sfx.BLIP, 0.9f + (c.code % 7) * 0.09f)
        }
    }
    val invisivel = Palette.ink.copy(alpha = 0f)
    Text(buildAnnotatedString {
        append(texto.take(n))
        withStyle(SpanStyle(color = invisivel)) { append(texto.drop(n)) }
    }, modifier, style = style, color = Palette.ink)
}

/** O palco com a linha do tempo: arrasta para voltar, para, acelera e pede para prever. */
@Composable
fun SimPlayer(sim: Sim, modifier: Modifier = Modifier, comecaEm: Int = 0) {
    var k by remember(sim) { mutableIntStateOf(comecaEm.coerceIn(0, sim.passos.lastIndex)) }
    var tocando by remember(sim) { mutableStateOf(comecaEm == 0) }
    var veloc by remember { mutableFloatStateOf(1f) }
    var prever by remember { mutableStateOf(false) }
    var resposta by remember(sim, k) { mutableStateOf<Int?>(null) }
    var erros by remember(sim) { mutableIntStateOf(0) }
    var hop by remember { mutableIntStateOf(0) }

    val passo = sim.passos[k]
    val ask = passo.ask.takeIf { prever }
    val perguntando = ask != null && resposta == null
    val shot = if (perguntando && k > 0) sim.passos[k - 1].shot else passo.shot

    LaunchedEffect(sim, k, tocando, perguntando, veloc) {
        if (!tocando || perguntando) return@LaunchedEffect
        if (k >= sim.passos.lastIndex) { tocando = false; return@LaunchedEffect }
        delay(((1500 + passo.say.length * 36) / veloc).toLong())
        k++
    }
    LaunchedEffect(k) { if (passo.erro) Juice.wrong() }

    Column(modifier) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f))) {
            val u = minOf(maxWidth / STAGE_W, maxHeight / com.compilandia.ui.motion.STAGE_H)
            val margem = (maxWidth - u * STAGE_W) / 2
            MotionStage(shot, Modifier.fillMaxSize().testTag("palco-lab"))
            val a = shot.actor
            if (a != null) {
                val w by animateDpAsState(86.dp * a.scale, spring(dampingRatio = 0.6f, stiffness = 260f))
                val h = w * 1.2f
                val x by animateDpAsState(margem + u * a.x - w / 2, spring(dampingRatio = 0.62f, stiffness = 220f))
                val y by animateDpAsState(u * a.y - h, spring(dampingRatio = 0.62f, stiffness = 220f))
                ByteFigure(Modifier.offset(x, y).width(w), if (passo.erro) Mood.SAD else if (perguntando) Mood.THINK else Mood.NEUTRAL, hop = hop)
            }
        }
        HazardStripe()
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinhaDoTempo(k, sim.passos.size, sim.passos.mapIndexedNotNull { i, p -> i.takeIf { p.erro } }.toSet()) { novo ->
                k = novo
                tocando = false
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotaoPlay(tocando) {
                    if (k >= sim.passos.lastIndex) k = 0
                    tocando = !tocando
                    Juice.click()
                }
                Etiqueta(if (veloc == 1f) "1x" else if (veloc == 2f) "2x" else "0,5x", false) {
                    veloc = when (veloc) { 1f -> 2f; 2f -> 0.5f; else -> 1f }
                }
                Etiqueta("Prever", prever) { prever = !prever; Juice.click() }
                Spacer(Modifier.weight(1f))
                Text("${k + 1}/${sim.passos.size}", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            }
            Box(Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                if (ask != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (perguntando) ask.pergunta else ask.porque, style = MaterialTheme.typography.bodyLarge)
                        if (perguntando) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ask.opcoes.forEachIndexed { i, o ->
                                BigButton(o, Modifier.weight(1f), small = true, color = Palette.drawer) {
                                    resposta = i
                                    if (i == ask.certa) { Sound.play(Sfx.CORRECT); hop++ } else { erros++; Juice.wrong() }
                                }
                            }
                        }
                    }
                } else {
                    FalaDigitada(passo.say)
                }
            }
        }
    }
}

@Composable
private fun LinhaDoTempo(k: Int, total: Int, erros: Set<Int>, onSeek: (Int) -> Unit) {
    val trilho = Palette.drawerLo
    val cheio = Palette.hazard
    val tique = Palette.ink.copy(alpha = 0.45f)
    val erro = Palette.red
    val bola = Palette.ink
    fun indice(x: Float, w: Float) = ((x / w) * (total - 1) + 0.5f).toInt().coerceIn(0, total - 1)
    Canvas(
        Modifier.fillMaxWidth().height(28.dp).testTag(TIMELINE_TAG)
            .pointerInput(total) { detectTapGestures { onSeek(indice(it.x, size.width.toFloat())) } }
            .pointerInput(total) { detectHorizontalDragGestures { c, _ -> onSeek(indice(c.position.x, size.width.toFloat())) } },
    ) {
        val cy = size.height / 2
        val h = 9.dp.toPx()
        val r = 11.dp.toPx()
        val w = size.width
        val p = if (total > 1) k.toFloat() / (total - 1) else 0f
        drawRoundRect(trilho, Offset(0f, cy - h / 2), Size(w, h), CornerRadius(h / 2))
        drawRoundRect(cheio, Offset(0f, cy - h / 2), Size(w * p, h), CornerRadius(h / 2))
        if (total <= 40) for (i in 0 until total) {
            val x = if (total > 1) w * i / (total - 1) else 0f
            val ruim = i in erros
            drawRect(if (ruim) erro else tique, Offset(x - 1.dp.toPx(), cy - (if (ruim) 11.dp else 7.dp).toPx()), Size((if (ruim) 3.dp else 2.dp).toPx(), (if (ruim) 22.dp else 14.dp).toPx()))
        }
        val x = (w * p).coerceIn(r, w - r)
        drawCircle(cheio, r, Offset(x, cy))
        drawCircle(bola, r * 0.55f, Offset(x, cy))
    }
}

@Composable
private fun BotaoPlay(tocando: Boolean, onClick: () -> Unit) {
    val fg = Palette.on(Palette.action)
    Box(Modifier.size(44.dp).clip(CircleShape).background(Palette.action).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(20.dp)) {
            if (tocando) {
                drawRect(fg, Offset(size.width * 0.18f, 0f), Size(size.width * 0.22f, size.height))
                drawRect(fg, Offset(size.width * 0.6f, 0f), Size(size.width * 0.22f, size.height))
            } else {
                drawPath(Path().apply { moveTo(size.width * 0.2f, 0f); lineTo(size.width, size.height / 2); lineTo(size.width * 0.2f, size.height); close() }, fg)
            }
        }
    }
}

@Composable
fun Etiqueta(texto: String, ligada: Boolean, onClick: () -> Unit) {
    val bg = if (ligada) Palette.hazard else Palette.white
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(bg).clickable { onClick() }.padding(horizontal = 11.dp, vertical = 8.dp)) {
        Text(texto, color = if (ligada) Palette.hazardDark else Palette.ink, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
