package com.compilandia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.content.EpMood
import com.compilandia.content.Episode
import com.compilandia.content.Fala
import com.compilandia.content.Passo
import com.compilandia.content.Speaker
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Abertura
import com.compilandia.content.episodes.Aberturas
import com.compilandia.content.episodes.Episodes
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.cast.BugFigure
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.cast.ProfFigure
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.motion.STAGE_W
import com.compilandia.ui.motion.tintColor
import com.compilandia.ui.pegboard
import com.compilandia.ui.voice.Narrator
import kotlinx.coroutines.delay
import kotlin.math.sin
import com.compilandia.textos.tx

const val STAGE_TAG = "palco"
const val FITA_TAG = "fita"

/** A fala escrita na faixa do narrador. O teste mede a largura dela. */
const val FALA_TAG = "fala"
private val NARRATOR_H = 176.dp
private val NARRATOR_SMALL = 54.dp
private val NARRATOR_CAPTION = 104.dp
/** Largura do Byte narrando embaixo, sozinho ou dividindo a faixa com a Profª. */
private val NARRADOR_W = 146.dp
private val NARRADOR_COM_PROF = 96.dp
private val PROF_W = 100.dp

/**
 * A aula animada: palco em cima, Byte narrando embaixo. Uma ideia por cena, e quem manda no ritmo é você.
 * Quando a cena pede, o Byte entra no palco; quando há pergunta, o desenho para e espera seu toque.
 */
@Composable
fun EpisodeScreen(app: AppState, s: Screen.Episode, onStep: Int = 0, abertura: Boolean = true) {
    val ep = remember(s) { s.id?.let { Episodes.byId(it) } ?: Episodes.of(s.skill) }
    if (ep == null) {
        LaunchedEffect(Unit) { app.replace(Screen.Lesson(s.skill, app.learner.chooseStyle(s.skill), returnToGame = s.returnToGame)) }
        return
    }
    val abre = remember(ep) { if (abertura && s.abertura && onStep == 0 && !s.returnToGame && ep.temporada == Temporada.BASICO && ep.id !in app.ls.episodesSeen) Aberturas.of(ep.skill) else null }
    var abrindo by remember(ep) { mutableStateOf(abre != null) }
    if (abrindo && abre != null) {
        BugAbertura(abre) {
            abrindo = false
            app.learner.marcaEtapa(ep.skill, com.compilandia.content.Etapa.BUG.name, feita = true, nowMs())
            app.commit()
        }
        return
    }
    var index by remember { mutableIntStateOf(onStep.coerceIn(0, ep.passos.lastIndex)) }
    var maisLonge by remember { mutableIntStateOf(onStep.coerceIn(0, ep.passos.lastIndex)) }
    var finished by remember { mutableStateOf(false) }
    val passo = ep.passos[index]
    val ask = passo.ask
    val brinq = passo.brinquedo
    val toy = remember(index) { brinq?.novo?.invoke() }
    var toyTick by remember(index) { mutableIntStateOf(0) }
    var found by remember(index) { mutableStateOf(emptySet<String>()) }
    var misses by remember(index) { mutableIntStateOf(0) }
    var solved by remember(index) { mutableStateOf(ask == null && toy == null) }
    var speaking by remember(index) { mutableStateOf<Fala?>(passo.fala) }
    var reaction by remember(index) { mutableStateOf<Mood?>(null) }
    var hop by remember { mutableIntStateOf(0) }
    var armsUp by remember(index) { mutableStateOf(0f) }
    var replay by remember(index) { mutableIntStateOf(0) }
    var legendaForcada by remember(index) { mutableStateOf(false) }
    // Play/pause: tocando, a cena segue sozinha quando a fala acaba. Perguntas e brinquedos esperam o toque.
    var tocando by remember { mutableStateOf(app.ls.autoAvancar) }
    var prontoParaSeguir by remember(index) { mutableStateOf(false) }
    var falouAudio by remember(index) { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { Juice.combo = 0 } }

    LaunchedEffect(index, finished) {
        if (!finished) {
            falouAudio = Narrator.canSpeak(passo.fala)
            Narrator.say(passo.fala)
            if (ask == null && brinq == null) {
                speaking = null
                prontoParaSeguir = true
            }
        }
    }

    fun next() {
        Sound.play(Sfx.CLICK)
        Narrator.stop()
        if (index < ep.passos.lastIndex) {
            index++
            maisLonge = maxOf(maisLonge, index)
        } else {
            finished = true
            val novo = if (ep.temporada == Temporada.BASICO) app.learner.markEpisode(s.skill) else app.learner.markEpisodeId(ep.id)
            if (novo) app.commit()
            Juice.levelUp()
        }
    }

    fun irPara(i: Int) {
        if (i == index || i > maisLonge) return
        Narrator.stop()
        Sound.play(Sfx.TICK)
        index = i
    }

    fun aperta(tecla: String) {
        val t = toy ?: return
        val b = brinq ?: return
        if (solved) return
        val valeu = t.aperta(tecla)
        toyTick++
        if (t.pronto) {
            solved = true
            reaction = Mood.HAPPY
            armsUp = 1f
            hop++
            Juice.correct()
            speaking = b.acerto
        } else if (valeu) {
            Sound.play(Sfx.DROP)
        } else {
            misses++
            Juice.wrong()
            reaction = Mood.SAD
            speaking = b.dica
            replay++
            if (misses >= 4) solved = true
        }
    }

    fun tap(id: String) {
        val a = ask ?: return
        if (solved) return
        if (id in a.right) {
            found = found + id
            Sound.play(Sfx.DROP)
            if (found.containsAll(a.right)) {
                solved = true
                reaction = Mood.HAPPY
                armsUp = 1f
                hop++
                Juice.correct()
                speaking = a.got
            }
        } else {
            misses++
            Juice.wrong()
            reaction = Mood.SAD
            speaking = a.hint
            if (misses >= 3) { found = a.right; solved = true }
        }
    }
    LaunchedEffect(speaking, replay) {
        val f = speaking
        if (f != null && f != passo.fala) {
            falouAudio = Narrator.canSpeak(f)
            Narrator.say(f)
            // Depois do acerto, a aula pode seguir sozinha.
            if (solved && (f == ask?.got || f == brinq?.acerto)) prontoParaSeguir = true
        }
    }
    LaunchedEffect(prontoParaSeguir, tocando, finished) {
        if (!tocando || !prontoParaSeguir || finished) return@LaunchedEffect
        // Com voz, uma pausa curta depois da fala; sem voz, o tempo de ler a legenda.
        val texto = (if (solved && ask != null) ask.got.text else if (solved && brinq != null) brinq.acerto.text else passo.fala.text)
        delay(if (falouAudio) 700L else 1200L + texto.length * 55L)
        if (tocando && !finished) next()
    }
    LaunchedEffect(reaction, misses) {
        if (reaction == Mood.SAD) {
            delay(1400)
            reaction = null
        }
    }

    val mood = reaction ?: when (passo.mood) {
        EpMood.HAPPY -> Mood.HAPPY
        EpMood.THINK -> Mood.THINK
        EpMood.SURPRISED -> Mood.SURPRISED
        EpMood.NEUTRAL -> Mood.NEUTRAL
    }

    val janela = com.compilandia.ui.LocalJanela.current
    val ladoALado = janela.ladoALado
    val shot = if (toy != null) remember(toyTick, toy) { toy.shot } else passo.shot
    val inScene = shot.actor != null
    val line = speaking ?: passo.fala
    val showText = !Narrator.canSpeak(line) || app.ls.captionsOn || legendaForcada
    val profFala = line.who == Speaker.PROF

    // A fala escrita (ou as barrinhas do som) e o "não entendi". Igual nos dois arranjos.
    val falaEscrita: @Composable (Modifier, Boolean) -> Unit = { mod, estreito ->
        Column(mod, verticalArrangement = if (ladoALado) Arrangement.Top else Arrangement.Center) {
            val talking = Narrator.speaker != null
            Box(Modifier.weight(1f, fill = false).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                if (showText) {
                    RichText(line.text, Modifier.fillMaxWidth().testTag(FALA_TAG), style = if (estreito) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge)
                } else if (talking) SoundBars(Modifier.size(90.dp, 56.dp))
            }
            // Sensor de texto ruim: guarda exatamente qual fala não foi entendida.
            Text(
                tx("episodescreen_nao_entendi"), color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End).clickable {
                    app.learner.naoEntendiFala(s.skill, line.id, nowMs()); app.commit()
                    legendaForcada = true
                    replay++
                    app.say(tx("episodescreen_anotado_veja_com_calma_toque"))
                }.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
    // Embaixo de tudo: brinquedo, dica ou play/pause e Continuar.
    val controles: @Composable () -> Unit = {
        if (!finished) Column(
            Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (toy != null && brinq != null && !solved) {
                PainelBrinquedo(toy, toyTick) { aperta(it) }
            } else if (ask != null && !solved) {
                BigButton(tx("comum_dica"), Modifier.fillMaxWidth(), color = Palette.drawer) { speaking = ask.hint; replay++ }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoTocar(tocando) {
                        tocando = !tocando
                        app.learner.setAutoAvancar(tocando)
                        app.commit()
                        app.say(if (tocando) tx("aula_tocando_aviso") else tx("aula_pausado_aviso"))
                    }
                    BigButton(tx("comum_continuar"), Modifier.weight(1f)) { next() }
                }
            }
        }
    }
    // Mural, rodada 13: a gramática da aula a um toque, por cima do palco. Fecha ao mudar de cena.
    val gramatica = remember(ep, index) { gramaticaDaAula(ep, index) }
    var gramaticaAberta by remember { mutableStateOf(false) }
    LaunchedEffect(index) { gramaticaAberta = false }
    val palco: @Composable (Modifier, Float) -> Unit = { mod, alturaLogica ->
        Box(mod.background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f))) {
            MotionStage(shot, Modifier.fillMaxSize().testTag(STAGE_TAG), if (ask != null && !solved) ask.right + ask.wrong else emptySet(), found, stageH = alturaLogica) { tap(it) }
            PainelGramatica(gramatica, gramaticaAberta) { gramaticaAberta = false }
        }
    }

    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        EpisodeTop(app.ls.captionsOn,
            gramatica = if (gramatica != null) ({ BotaoGramatica(gramaticaAberta) { gramaticaAberta = !gramaticaAberta } }) else null,
            onClose = { Narrator.stop(); app.back() },
            onCaptions = { app.learner.setCaptions(!app.ls.captionsOn); app.commit() },
            onSkip = {
                Narrator.stop()
                if (ep.temporada == Temporada.BASICO) app.learner.markEpisode(s.skill) else app.learner.markEpisodeId(ep.id)
                app.commit()
                goPractice(app, s)
            })
        if (!ladoALado) FitaDeCenas(ep.passos, index, maisLonge, ep.inicioDoExemplo) { irPara(it) }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val altura = maxHeight
            val largura = maxWidth
            // Lado a lado: o palco à esquerda, a fita, a fala e os botões num painel à direita.
            val painelW = if (!ladoALado) 0.dp else if (janela.faixa == com.compilandia.ui.Faixa.EXPANDIDA) (largura * 0.36f).coerceIn(340.dp, 440.dp) else (largura * 0.42f).coerceIn(280.dp, 360.dp)
            val palcoW = largura - painelW
            val narradorBase = when {
                ladoALado -> 0.dp
                !inScene -> NARRATOR_H
                showText -> NARRATOR_CAPTION
                else -> NARRATOR_SMALL
            }
            val narrator by animateDpAsState(narradorBase, spring(stiffness = 300f))
            val palcoH = altura - narrator
            // O palco cresce até a fala. Se a largura limita, a altura lógica passa de 410 e as peças descem para o meio.
            val k = minOf(palcoW / STAGE_W, palcoH / com.compilandia.ui.motion.STAGE_H)
            val alturaLogica = com.compilandia.ui.motion.alturaLogica(palcoW.value, palcoH.value)
            val esquerda = (palcoW - k * STAGE_W) / 2
            val descida = com.compilandia.ui.motion.descidaExtra(shot, alturaLogica)
            // Num janela baixa, o Byte do painel encolhe para a fala caber.
            val byteDoPainel = if (altura < 420.dp) 84.dp else 120.dp
            val narradorW by animateDpAsState(
                if (ladoALado) byteDoPainel else if (profFala) NARRADOR_COM_PROF else NARRADOR_W,
                spring(dampingRatio = 0.7f, stiffness = 300f),
            )
            // Onde o Byte fica quando narra: embaixo à esquerda (empilhado) ou no alto do painel (lado a lado).
            var topoDoPainel by remember { mutableStateOf(44.dp) }
            if (ladoALado) {
                Row(Modifier.fillMaxSize()) {
                    palco(Modifier.width(palcoW).fillMaxHeight(), alturaLogica)
                    Column(Modifier.width(painelW).fillMaxHeight().background(Palette.paper)) {
                        FitaDeCenas(ep.passos, index, maisLonge, ep.inicioDoExemplo) { irPara(it) }
                        val dens = androidx.compose.ui.platform.LocalDensity.current
                        Column(
                            Modifier.weight(1f).fillMaxWidth().onPlaced { topoDoPainel = with(dens) { it.positionInParent().y.toDp() } }
                                .padding(horizontal = 14.dp),
                        ) {
                            // O lugar do Byte e da Profª no alto; a fala embaixo, com a largura toda do painel.
                            Spacer(Modifier.height(narradorW * 1.2f + 6.dp))
                            falaEscrita(Modifier.weight(1f).fillMaxWidth(), false)
                        }
                        controles()
                    }
                }
            } else {
                palco(Modifier.fillMaxWidth().height(palcoH), alturaLogica)
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(narrator).background(Palette.paper)) {
                    HazardStripe()
                    // Com a Profª no canto, o Byte encolhe e olha para ela. A fala fica com o meio da faixa.
                    val inicio = if (inScene) 14.dp else narradorW + 8.dp
                    val fimDaFala = if (profFala) PROF_W + 8.dp else 14.dp
                    falaEscrita(Modifier.fillMaxSize().padding(start = inicio, end = fimDaFala, top = 4.dp, bottom = 2.dp), inScene || profFala)
                }
            }
            // O mesmo Byte narra e entra no palco: ele só muda de lugar.
            val actor = shot.actor
            val size by animateDpAsState(if (actor != null) 86.dp * actor.scale * k.value else narradorW, spring(dampingRatio = 0.6f, stiffness = 260f))
            val figH = size * 1.2f
            val x by animateDpAsState(
                when {
                    actor != null -> esquerda + k * actor.x - size / 2
                    ladoALado -> palcoW + 10.dp
                    else -> 4.dp
                },
                spring(dampingRatio = 0.62f, stiffness = 220f),
            )
            val y by animateDpAsState(
                when {
                    actor != null -> k * (actor.y + descida) - figH
                    ladoALado -> topoDoPainel + 4.dp
                    else -> altura - figH - 2.dp
                },
                spring(dampingRatio = 0.62f, stiffness = 220f),
            )
            ByteFigure(
                Modifier.offset(x, y).width(size), mood,
                lookY = if (actor == null && !profFala && !ladoALado) -0.5f else 0f,
                lookX = if (actor == null) (if (profFala) 1f else 0.5f) else 0f,
                hop = hop, armsUp = armsUp,
            )
            if (ladoALado) {
                ProfPop(profFala, Modifier.align(Alignment.TopEnd).offset(y = topoDoPainel).padding(end = 10.dp, top = 4.dp))
            } else {
                ProfPop(profFala, Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 2.dp))
            }
            if (finished) EpisodeEnd(app, s, ep, onReplay = { index = 0; finished = false }) { goPractice(app, s) }
        }
        if (!ladoALado) controles()
    }
}

/** Embaixo do palco: o objetivo, as teclas e o que o motor disse da última jogada. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PainelBrinquedo(toy: com.compilandia.content.Toy, tick: Int, aperta: (String) -> Unit) {
    Text(toy.aviso ?: toy.meta, style = MaterialTheme.typography.titleSmall, color = if (toy.aviso != null) Palette.yellow else Palette.ink)
    val teclas = remember(tick, toy) { toy.teclas }
    val curtas = teclas.size <= 4 && teclas.all { it.length <= 16 }
    if (curtas) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (t in teclas) BigButton(t, Modifier.weight(1f), color = Palette.drawer) { aperta(t) }
    } else FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (t in teclas) BigButton(t, small = true, color = Palette.drawer) { aperta(t) }
    }
}

/**
 * A fita de cenas: uma miniatura real de cada cena. Tocar ou arrastar volta a aula.
 * Para a frente só vale até onde a pessoa já chegou.
 */
@Composable
private fun FitaDeCenas(passos: List<Passo>, index: Int, maisLonge: Int, marcado: Int?, onSeek: (Int) -> Unit) {
    val fundo = Palette.paperDark
    val luz = Palette.hazard
    val apagada = Palette.drawerLo
    val n = passos.size
    fun cena(x: Float, w: Float) = (x / w * n).toInt().coerceIn(0, n - 1)
    // Mural, rodada 14: a fita em quatro partes, com o nome de cada uma em cima.
    val partes = remember(passos) { partesDaAula(passos, marcado) }
    val divisa = Palette.hazard.copy(alpha = 0.7f)
    if (partes != null) RotulosDasPartes(partes, index, maisLonge, onSeek)
    Canvas(
        Modifier.fillMaxWidth().height(34.dp).background(Palette.paper).padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag(FITA_TAG)
            .pointerInput(n) { detectTapGestures { onSeek(cena(it.x, size.width.toFloat())) } }
            .pointerInput(n) { detectHorizontalDragGestures { c, _ -> onSeek(cena(c.position.x, size.width.toFloat())) } },
    ) {
        val gap = 3.dp.toPx()
        val w = (size.width - gap * (n - 1)) / n
        passos.forEachIndexed { i, p ->
            val x = i * (w + gap)
            val vista = i <= maisLonge
            drawRoundRect(fundo, Offset(x, 0f), Size(w, size.height), CornerRadius(4.dp.toPx()))
            if (vista) {
                val k = minOf(w / STAGE_W, size.height / 410f)
                val esq = x + (w - STAGE_W * k) / 2
                for (b in p.shot.boxes) {
                    if (b.look == com.compilandia.content.Look.LABEL || b.look == com.compilandia.content.Look.TITLE) continue
                    val cor = tintColor(b.tint).copy(alpha = if (i == index) 1f else 0.65f)
                    drawRect(cor, Offset(esq + (b.x - b.w / 2) * k, (b.y - b.h / 2) * k), Size((b.w * k).coerceAtLeast(1f), (b.h * k).coerceAtLeast(1f)))
                }
            } else {
                drawRoundRect(apagada, Offset(x + w * 0.25f, size.height * 0.4f), Size(w * 0.5f, size.height * 0.2f), CornerRadius(2f))
            }
            if (i == index) drawRoundRect(luz, Offset(x, 0f), Size(w, size.height), CornerRadius(4.dp.toPx()), style = Stroke(2.dp.toPx()))
            if (partes != null && i > 0 && partes.any { it.first == i }) drawRect(divisa, Offset(x - gap / 2 - 1.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
        }
    }
}

/**
 * Antes da aula, o Bug ataca: 10 segundos para tentar sozinho.
 * Errar aqui ajuda: quem tenta antes aprende mais com a explicação que vem depois.
 */
@Composable
private fun BugAbertura(a: Abertura, onDone: () -> Unit) {
    var resposta by remember { mutableStateOf<Int?>(null) }
    var resta by remember { mutableIntStateOf(10) }
    var hurt by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { Sound.play(Sfx.GLITCH); Narrator.say(a.fala) }
    LaunchedEffect(resposta) {
        if (resposta != null) return@LaunchedEffect
        while (resta > 0) {
            delay(1000)
            resta--
            Sound.play(Sfx.TICK)
        }
        resposta = -1
    }
    LaunchedEffect(resposta) { if (resposta != null) Narrator.say(a.depois) }
    val alarme by rememberInfiniteTransition().animateFloat(0.1f, 0.42f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
    val vermelho = Palette.action
    Column(
        Modifier.fillMaxSize().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(tx("comum_pular"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { Narrator.stop(); onDone() }.padding(6.dp))
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(26.dp)).background(Palette.codeBg), contentAlignment = Alignment.Center) {
                Text("$resta", color = Palette.hazard, style = MaterialTheme.typography.headlineSmall)
            }
        }
        Text(a.manchete, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        Box(contentAlignment = Alignment.Center) {
            if (resposta == null && !com.compilandia.ui.ThemeState.reduceMotion) Canvas(Modifier.size(230.dp)) {
                drawCircle(vermelho.copy(alpha = alarme), size.minDimension / 2)
            }
            BugFigure(Modifier.width(170.dp), hurt = hurt, down = if (resposta == a.certa) 1f else 0f)
        }
        Text(a.desafio, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, fontFamily = com.compilandia.ui.LocalFonts.current.mono)
        val r = resposta
        if (r == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                a.opcoes.forEachIndexed { i, o ->
                    BigButton(o, Modifier.weight(1f), color = Palette.drawer) {
                        resposta = i
                        if (i == a.certa) { Juice.correct(); hurt++ } else Juice.wrong()
                    }
                }
            }
        } else {
            Text(if (r == a.certa) tx("episodescreen_acertou_primeira") else tx("episodescreen_errar_aqui_ajuda_aula_colar"), style = MaterialTheme.typography.titleMedium, color = if (r == a.certa) Palette.green else Palette.yellow)
            RichText(a.depois.text, style = MaterialTheme.typography.bodyLarge)
            BigButton(tx("episodescreen_agora_aula"), Modifier.fillMaxWidth()) { Narrator.stop(); onDone() }
        }
    }
}

@Composable
private fun ProfPop(visible: Boolean, modifier: Modifier) {
    AnimatedVisibility(visible, modifier, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
        ProfFigure(Modifier.size(PROF_W), lookX = -0.5f)
    }
}

private fun goPractice(app: AppState, s: Screen.Episode) {
    if (s.returnToGame) {
        app.back()
        return
    }
    val l = app.learner
    app.replace(Screen.Play(s.skill, l.chooseGame(s.skill), l.chooseLevel(s.skill, nowMs()), seed = nowMs()))
}

@Composable
private fun EpisodeTop(captions: Boolean, gramatica: (@Composable () -> Unit)?, onClose: () -> Unit, onCaptions: () -> Unit, onSkip: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Palette.white).clickable { onClose() }, contentAlignment = Alignment.Center) {
            Text("✕", color = Palette.ink, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.weight(1f))
        if (gramatica != null) { gramatica(); Spacer(Modifier.width(6.dp)) }
        Box(
            Modifier.clip(RoundedCornerShape(6.dp)).background(if (captions) Palette.hazard else Palette.white).clickable { onCaptions() }.padding(horizontal = 7.dp, vertical = 5.dp),
        ) { Text("CC", color = if (captions) Palette.hazardDark else Palette.ink, style = MaterialTheme.typography.labelMedium) }
        Spacer(Modifier.width(6.dp))
        Text(tx("comum_pular"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { onSkip() }.padding(4.dp))
    }
}

@Composable
private fun SoundBars(modifier: Modifier) {
    val t by rememberInfiniteTransition().animateFloat(0f, 6.28f, infiniteRepeatable(tween(900), RepeatMode.Restart))
    val c = Palette.teal
    Canvas(modifier) {
        val n = 7
        val w = size.width / (n * 1.7f)
        for (i in 0 until n) {
            val h = size.height * (0.35f + 0.55f * (0.5f + 0.5f * sin(t * 2 + i * 1.3f)))
            drawRoundRect(c, Offset(i * w * 1.7f, (size.height - h) / 2), Size(w, h), CornerRadius(w / 2))
        }
    }
}

@Composable
private fun EpisodeEnd(app: AppState, s: Screen.Episode, ep: Episode, onReplay: () -> Unit, onPractice: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Palette.paper).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var hop by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) { hop++ }
        ByteFigure(Modifier.width(110.dp), Mood.HAPPY, hop = hop, armsUp = 1f)
        Text(ep.title, style = MaterialTheme.typography.headlineMedium)
        CenaConfusa(app, s, ep)
        Text(tx("episodescreen_agora_com_voce_testar_logo"), style = MaterialTheme.typography.bodyLarge, color = Palette.inkSoft)
        // Fim de aula da trilha: o botão leva à próxima etapa. Em C e Tabelas voltam para onde a pessoa estava.
        val naTrilha = !s.returnToGame && ep.temporada in setOf(Temporada.BASICO, Temporada.PRATICA, Temporada.PEGADINHA)
        val proximo = remember(app.ls) { if (naTrilha) com.compilandia.content.Trilha.proximo(app.learner, s.skill) else null }
        when {
            s.returnToGame -> BigButton(tx("episodescreen_voltar_ao_jogo"), Modifier.fillMaxWidth()) { onPractice() }
            proximo != null -> BigButton(tx("episodio_proxima_etapa", proximo.titulo), Modifier.fillMaxWidth()) {
                Narrator.stop()
                abrirPasso(app, s.skill, proximo, troca = true)
            }
            naTrilha -> BigButton(tx("episodescreen_praticar_agora"), Modifier.fillMaxWidth()) { onPractice() }
            // A7: o clipe da revisão rápida termina num desafio, que é o que atualiza a memória.
            ep.temporada == Temporada.CLIPE -> BigButton(tx("revisao_desafio"), Modifier.fillMaxWidth()) { onPractice() }
            else -> BigButton(tx("comum_continuar"), Modifier.fillMaxWidth()) { Narrator.stop(); app.back() }
        }
        val temExemplo = remember(s.skill) { com.compilandia.tutor.Sims.exemplo(s.skill, kotlin.random.Random(1)) != null }
        if (temExemplo) BigButton(tx("episodescreen_outro_exemplo"), Modifier.fillMaxWidth(), color = Palette.hazard, textColor = Palette.hazardDark) { app.go(Screen.Exemplo(s.skill)) }
        BigButton(tx("episodescreen_ver_novo"), Modifier.fillMaxWidth(), color = Palette.drawer) { onReplay() }
        Text(
            tx("episodescreen_ler_aula_completa"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { app.replace(Screen.Lesson(s.skill, app.learner.chooseStyle(s.skill), returnToGame = s.returnToGame)) }.padding(8.dp),
        )
    }
}

/** T4: no lugar do diário, uma pergunta de um toque. A cena tocada vira "não entendi" e abre a explicação em outro estilo. */
@Composable
private fun CenaConfusa(app: AppState, s: Screen.Episode, ep: Episode) {
    var marcada by remember(ep) { mutableIntStateOf(-1) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(tx("fim_cena_confusa"), style = MaterialTheme.typography.titleSmall)
        Text(tx("fim_cena_confusa_sub"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ep.passos.forEachIndexed { i, passo ->
                val sel = i == marcada
                Column(
                    Modifier.width(78.dp).clip(RoundedCornerShape(7.dp)).background(if (sel) Palette.hazard.copy(alpha = 0.25f) else Palette.white)
                        .border(2.dp, if (sel) Palette.hazard else Palette.paperDark, RoundedCornerShape(7.dp))
                        .clickable {
                            marcada = i
                            app.learner.naoEntendiFala(s.skill, passo.fala.id, nowMs())
                            app.commit()
                        }.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(5.dp)).background(Palette.codeBg)) {
                        com.compilandia.ui.motion.MotionStage(passo.shot, Modifier.fillMaxSize().padding(2.dp))
                    }
                    Text("${i + 1}", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
                }
            }
        }
        if (marcada >= 0) {
            Text(tx("fim_cena_anotada", marcada + 1), style = MaterialTheme.typography.bodySmall)
            BigButton(tx("trilha_ler_com_calma"), Modifier.fillMaxWidth(), color = Palette.hazard, textColor = Palette.hazardDark, small = true) {
                Narrator.stop()
                app.go(Screen.Lesson(s.skill, app.learner.chooseStyle(s.skill), returnToGame = s.returnToGame))
            }
        }
    }
}

/** Play/pause da aula: um triângulo para tocar sozinho, duas barras para pausar. */
@Composable
private fun BotaoTocar(tocando: Boolean, onClick: () -> Unit) {
    val cor = if (tocando) Palette.hazard else Palette.drawer
    val tinta = Palette.on(cor)
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(cor).clickable { onClick() }
            .semantics { contentDescription = if (tocando) tx("aula_pausar") else tx("aula_tocar") },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) {
            if (tocando) {
                val w = size.width * 0.3f
                drawRect(tinta, Offset(size.width * 0.12f, 0f), Size(w, size.height))
                drawRect(tinta, Offset(size.width * 0.58f, 0f), Size(w, size.height))
            } else {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.18f, 0f); lineTo(size.width, size.height / 2); lineTo(size.width * 0.18f, size.height); close()
                }
                drawPath(p, tinta)
            }
        }
    }
}

