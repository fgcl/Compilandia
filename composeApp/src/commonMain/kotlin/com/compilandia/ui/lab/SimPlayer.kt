package com.compilandia.ui.lab

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
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
import com.compilandia.textos.tx

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

/**
 * O palco com a linha do tempo: arrasta para voltar, para, acelera e pede para prever.
 * Modo guiado (A3): [exigir] diz em quais passos a pessoa responde; nos outros passos com pergunta, o Byte resolve e mostra o porquê.
 * [onFim] avisa quando a simulação chega ao último passo.
 */
@Composable
fun SimPlayer(sim: Sim, modifier: Modifier = Modifier, comecaEm: Int = 0, exigir: Set<Int>? = null, onFim: () -> Unit = {}) {
    var k by remember(sim) { mutableIntStateOf(comecaEm.coerceIn(0, sim.passos.lastIndex)) }
    var tocando by remember(sim) { mutableStateOf(comecaEm == 0) }
    var veloc by remember { mutableFloatStateOf(1f) }
    var prever by remember { mutableStateOf(false) }
    var resposta by remember(sim, k) { mutableStateOf<Int?>(null) }
    var erros by remember(sim) { mutableIntStateOf(0) }
    var hop by remember { mutableIntStateOf(0) }

    val passo = sim.passos[k]
    val guiado = exigir != null
    val ask = passo.ask.takeIf { if (guiado) k in exigir!! else prever }
    // Passo resolvido pelo Byte no modo guiado: mostra a pergunta, a resposta e o porquê.
    val resolvido = if (guiado && ask == null) passo.ask else null
    val perguntando = ask != null && resposta == null
    var dicas by remember(sim, k) { mutableIntStateOf(0) }
    val voz = androidx.compose.runtime.rememberCoroutineScope()
    var avisouFim by remember(sim) { mutableStateOf(false) }
    LaunchedEffect(sim, k, perguntando) {
        if (k >= sim.passos.lastIndex && !perguntando && !avisouFim) { delay(900); avisouFim = true; onFim() }
    }
    val shot = if (perguntando && k > 0) sim.passos[k - 1].shot else passo.shot

    LaunchedEffect(sim, k, tocando, perguntando, veloc) {
        if (!tocando || perguntando) return@LaunchedEffect
        if (k >= sim.passos.lastIndex) { tocando = false; return@LaunchedEffect }
        val extra = if (resolvido != null) resolvido.porque.length * 40 + 1800 else 0
        delay(((1500 + passo.say.length * 36 + extra) / veloc).toLong())
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
                if (!guiado) Etiqueta(tx("simplayer_prever"), prever) { prever = !prever; Juice.click() }
                if (guiado && perguntando) Etiqueta(tx("guiado_dica"), dicas > 0) { dicas = minOf(dicas + 1, 2); Juice.click() }
                Spacer(Modifier.weight(1f))
                Text("${k + 1}/${sim.passos.size}", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            }
            Box(
                Modifier.fillMaxWidth().heightIn(min = 72.dp).then(
                    // Modo guiado: pergunta e resposta num cartão próprio, com letra maior.
                    if (guiado && (ask != null || resolvido != null)) Modifier.clip(RoundedCornerShape(10.dp)).background(Palette.white)
                        .border(2.dp, if (perguntando) Palette.hazard else Palette.green, RoundedCornerShape(10.dp)).padding(12.dp)
                    else Modifier,
                ),
            ) {
                if (ask != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (perguntando) ask.pergunta else ask.porque, style = if (guiado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
                        // Dica em degraus: o primeiro tira uma opção errada; o segundo mostra o porquê.
                        if (perguntando && dicas >= 2) Text(ask.porque, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                        val tirada = if (dicas >= 1) ask.opcoes.indices.firstOrNull { it != ask.certa } else null
                        fun responde(i: Int) {
                            resposta = i
                            if (i == ask.certa) { Sound.play(Sfx.CORRECT); hop++ } else { erros++; Juice.wrong() }
                            if (guiado) voz.launch { com.compilandia.ui.voice.Narrator.say(if (i == ask.certa) com.compilandia.content.Falas.acerto.random() else com.compilandia.content.Falas.erro.random()) }
                        }
                        if (perguntando && guiado) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ask.opcoes.forEachIndexed { i, o -> if (i != tirada) BigButton(o, Modifier.fillMaxWidth(), color = Palette.drawer) { responde(i) } }
                        } else if (perguntando) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ask.opcoes.forEachIndexed { i, o ->
                                if (i == tirada) return@forEachIndexed
                                BigButton(o, Modifier.weight(1f), small = true, color = Palette.drawer) { responde(i) }
                            }
                        }
                    }
                } else if (resolvido != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tx("guiado_byte_resolve", resolvido.pergunta, resolvido.opcoes[resolvido.certa]), style = MaterialTheme.typography.titleMedium)
                        Text(resolvido.porque, style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
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
