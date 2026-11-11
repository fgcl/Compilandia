package com.compilandia.ui.bancada

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.compilandia.content.Etapa
import com.compilandia.content.Robo
import com.compilandia.content.Robos
import com.compilandia.content.Speaker
import com.compilandia.content.Trilha
import com.compilandia.engine.Veredito
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.Header
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalJanela
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeState
import com.compilandia.ui.VSpace
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.screens.abrirPasso
import kotlinx.coroutines.delay

/**
 * J1: a linha do tempo de uma corrida. Toca sozinha, para, volta um passo e muda a velocidade.
 * Cada jogo diz quantos quadros a corrida tem; o palco desenha o quadro [atual].
 */
class LinhaDoTempo {
    var total by mutableIntStateOf(1)
    var atual by mutableIntStateOf(0)
    var tocando by mutableStateOf(false)
    var vel by mutableIntStateOf(0)

    fun carrega(n: Int, tocar: Boolean) {
        total = n.coerceAtLeast(1)
        atual = 0
        tocando = tocar && total > 1
    }

    /** Quanto dura um quadro, na velocidade escolhida. */
    fun passoMs(): Long = BASE_MS / VELOCIDADES[vel]

    companion object {
        const val BASE_MS = 760L
        val VELOCIDADES = listOf(1, 2, 4)
    }
}

/** O que muda em todo jogo de montar: o texto do Byte, o humor e a trava da dica. */
open class EstadoBancada(pedido: String) {
    var frase by mutableStateOf(pedido)
    var humor by mutableStateOf(Mood.NEUTRAL)
    var veredito by mutableStateOf<Veredito?>(null)
    var noPalco by mutableIntStateOf(0)
    var dicas by mutableIntStateOf(0)
    /** A trava da dica (regra 5): a próxima só abre depois de rodar de novo. */
    var rodouDesdeDica by mutableStateOf(true)
    val linha = LinhaDoTempo()

    fun mexeu() {
        veredito = null
        humor = Mood.NEUTRAL
    }

    protected fun aplica(v: Veredito) {
        veredito = v.takeIf { it.falha != -1 }
        frase = v.frase
        humor = when {
            v.falha == -1 -> Mood.THINK
            v.passou -> Mood.HAPPY
            else -> Mood.SAD
        }
        rodouDesdeDica = true
    }

    /** Mostra a dica seguinte. [terceira] é a que aponta o lugar exato, calculada na hora. */
    fun dica(estaticas: List<String>, terceira: () -> String) {
        if (dicas > 0 && !rodouDesdeDica) {
            frase = tx("bancada_dica_trava")
            humor = Mood.THINK
            return
        }
        val n = dicas.coerceAtMost(2)
        frase = tx("bancada_dica_n", n + 1) + " " + if (n < estaticas.size) estaticas[n] else terceira()
        dicas = (dicas + 1).coerceAtMost(3)
        rodouDesdeDica = false
        humor = Mood.THINK
    }
}

/** A tela de um jogo de montar: barra das fases e o jogo da fase. */
@Composable
fun BancadaScreen(app: AppState, scr: Screen.Bancada) {
    val r = scr.robo
    val l = app.learner
    var fase by remember { mutableIntStateOf(if (scr.fase in 0 until r.fases) scr.fase else Robos.primeiraAberta(l, r)) }
    val ls = app.ls
    LaunchedEffect(fase) { com.compilandia.ui.voice.Narrator.say(com.compilandia.content.Fala(Speaker.BYTE, r.pedido(fase))) }
    Column(Modifier.fillMaxSize()) {
        Header(r.titulo, r.skill.title, Palette.region(r.skill.region))
        Row(
            Modifier.fillMaxWidth().background(Palette.paperDark).horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (i in 0 until r.fases) {
                val feita = remember(ls, i) { Robos.feita(l, r, i) }
                Chip((if (feita) "✓ " else "") + tx("bancada_fase", i + 1) + ": " + r.nomeFase(i), selected = i == fase, color = Palette.hazard) { fase = i }
            }
        }
        val passou: () -> Unit = {
            Robos.marca(l, r, fase, nowMs())
            app.commit()
        }
        val seguir: () -> Unit = {
            if (fase < r.fases - 1) fase++
            else {
                val prox = Trilha.proximo(l, r.skill)
                if (prox != null && prox.etapa != Etapa.PROGRAMAR) abrirPasso(app, r.skill, prox, troca = true) else app.back()
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            androidx.compose.runtime.key(r, fase) {
                when (r) {
                    Robo.MIPS -> {
                        val e = remember { RoboMipsEstado(com.compilandia.engine.RoboMips.fases[fase]) }
                        RoboMipsJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                    Robo.TRILHO -> {
                        val e = remember { TrilhoEstado(com.compilandia.engine.Trilho.fases[fase]) }
                        TrilhoJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                    Robo.PILHA -> {
                        val e = remember { PilhaEstado(com.compilandia.engine.RoboPilha.fases[fase]) }
                        PilhaJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                    Robo.GUINDASTE -> {
                        val e = remember { GuindasteEstado(com.compilandia.engine.Guindaste.fases[fase]) }
                        GuindasteJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                    Robo.FABRICA -> {
                        val e = remember { FabricaEstado(com.compilandia.engine.Fabrica.fases[fase]) }
                        FabricaJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                    Robo.CARTEIRO -> {
                        val e = remember { CarteiroEstado(com.compilandia.engine.Carteiro.fases[fase]) }
                        CarteiroJogo(e, ultima = fase == r.fases - 1, onPassou = passou, onSeguir = seguir)
                    }
                }
            }
        }
    }
}

/** A lista dos jogos de montar, aberta pela gaveta Jogar da sala. */
@Composable
fun BancadasScreen(app: AppState) {
    val l = app.learner
    val ls = app.ls
    Column(Modifier.fillMaxSize()) {
        Header(tx("bancada_titulo"), null, Palette.hazard)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (r in Robo.entries) {
                val feitas = remember(ls, r) { Robos.feitas(l, r) }
                PaperCard(Modifier.fillMaxWidth().clickable { app.go(Screen.Bancada(r)) }, border = if (feitas == r.fases) Palette.green else Palette.region(r.skill.region)) {
                    Text(r.skill.region.title + " · " + r.skill.title, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
                    Text(r.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(r.subtitulo, style = MaterialTheme.typography.bodySmall)
                    Text(tx("bancada_fases_feitas", feitas, r.fases), style = MaterialTheme.typography.labelMedium, color = if (feitas == r.fases) Palette.green else Palette.inkSoft)
                }
            }
        }
    }
}

// ------------------------------------------------------------ peças comuns

/** Palco e painel: lado a lado na janela larga, um embaixo do outro no celular em pé. */
@Composable
internal fun Arranjo(palco: @Composable ColumnScope.() -> Unit, painel: @Composable ColumnScope.() -> Unit) {
    val j = LocalJanela.current
    if (j.ladoALado) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = palco)
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = painel)
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            palco()
            painel()
        }
    }
}

/** A linha do tempo anda sozinha enquanto [LinhaDoTempo.tocando]. */
@Composable
internal fun TocaLinha(l: LinhaDoTempo) {
    LaunchedEffect(l.tocando, l.atual, l.total, l.vel) {
        if (!l.tocando) return@LaunchedEffect
        if (l.atual >= l.total - 1) {
            l.tocando = false
            return@LaunchedEffect
        }
        delay(l.passoMs())
        l.atual = (l.atual + 1).coerceAtMost(l.total - 1)
    }
}

private enum class Icone { INICIO, ANTES, TOCAR, PAUSAR, DEPOIS }

@Composable
private fun BotaoIcone(icone: Icone, descricao: String, onClick: () -> Unit) {
    val cor = Palette.onAccent
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.10f))
            .clickable { Sound.play(Sfx.CLICK); onClick() }.semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            fun triangulo(x0: Float, x1: Float, praDireita: Boolean) = Path().apply {
                if (praDireita) { moveTo(x0, 0f); lineTo(x1, h / 2); lineTo(x0, h) } else { moveTo(x1, 0f); lineTo(x0, h / 2); lineTo(x1, h) }
                close()
            }
            when (icone) {
                Icone.TOCAR -> drawPath(triangulo(w * 0.15f, w * 0.95f, true), cor)
                Icone.PAUSAR -> {
                    drawRect(cor, Offset(w * 0.15f, 0f), androidx.compose.ui.geometry.Size(w * 0.25f, h))
                    drawRect(cor, Offset(w * 0.6f, 0f), androidx.compose.ui.geometry.Size(w * 0.25f, h))
                }
                Icone.DEPOIS -> {
                    drawPath(triangulo(0f, w * 0.7f, true), cor)
                    drawRect(cor, Offset(w * 0.75f, 0f), androidx.compose.ui.geometry.Size(w * 0.2f, h))
                }
                Icone.ANTES -> {
                    drawRect(cor, Offset(w * 0.05f, 0f), androidx.compose.ui.geometry.Size(w * 0.2f, h))
                    drawPath(triangulo(w * 0.3f, w, false), cor)
                }
                Icone.INICIO -> {
                    drawRect(cor, Offset(0f, 0f), androidx.compose.ui.geometry.Size(w * 0.16f, h))
                    drawPath(triangulo(w * 0.18f, w * 0.6f, false), cor)
                    drawPath(triangulo(w * 0.58f, w, false), cor)
                }
            }
        }
    }
}

/** Os controles da linha do tempo e a legenda do passo, numa faixa embaixo do palco. */
@Composable
internal fun ControlesPalco(l: LinhaDoTempo, nota: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(nota, color = Palette.onAccent, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BotaoIcone(Icone.INICIO, tx("bancada_voltar_inicio")) { l.tocando = false; l.atual = 0 }
            BotaoIcone(Icone.ANTES, tx("bancada_passo_antes")) { l.tocando = false; l.atual = (l.atual - 1).coerceAtLeast(0) }
            if (l.tocando) BotaoIcone(Icone.PAUSAR, tx("bancada_pausar")) { l.tocando = false }
            else BotaoIcone(Icone.TOCAR, tx("bancada_tocar")) {
                if (l.atual >= l.total - 1) l.atual = 0
                l.tocando = l.total > 1
            }
            BotaoIcone(Icone.DEPOIS, tx("bancada_passo_depois")) { l.tocando = false; l.atual = (l.atual + 1).coerceAtMost(l.total - 1) }
            Spacer(Modifier.weight(1f))
            Mono("${l.atual + 1}/${l.total}", color = Palette.onAccent.copy(alpha = 0.7f), size = 13)
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.hazard).clickable { l.vel = (l.vel + 1) % LinhaDoTempo.VELOCIDADES.size }
                    .semantics { contentDescription = tx("bancada_velocidade") }.padding(horizontal = 10.dp, vertical = 8.dp),
            ) { Mono("${LinhaDoTempo.VELOCIDADES[l.vel]}x", color = Palette.on(Palette.hazard), size = 14, bold = true) }
        }
    }
}

/** O Byte falando: o pedido da fase, a dica ou o contraexemplo. */
@Composable
internal fun FalaDoByte(texto: String, humor: Mood) {
    val borda = when (humor) { Mood.HAPPY -> Palette.green; Mood.SAD -> Palette.red; else -> Palette.ink.copy(alpha = 0.3f) }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.white).border(2.dp, borda, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ByteFigure(Modifier.width(52.dp), mood = humor, animate = !ThemeState.reduceMotion)
        Spacer(Modifier.width(10.dp))
        RichText(texto, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

/** Rodar, Dica e Limpar. */
@Composable
internal fun BotoesBancada(onRodar: () -> Unit, onDica: () -> Unit, onLimpar: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton(tx("bancada_rodar"), Modifier.weight(1.4f), small = true, color = Palette.hazard) { onRodar() }
        BigButton(tx("bancada_dica"), Modifier.weight(1f), small = true, color = Palette.white, textColor = Palette.ink) { onDica() }
        BigButton(tx("bancada_limpar"), Modifier.weight(1f), small = true, color = Palette.white, textColor = Palette.ink) { onLimpar() }
    }
}

/** Depois que a fase passa: seguir para a próxima ou concluir. */
@Composable
internal fun BotaoSeguir(ultima: Boolean, onSeguir: () -> Unit) {
    BigButton(if (ultima) tx("bancada_concluir") else tx("bancada_proxima_fase"), Modifier.fillMaxWidth(), color = Palette.green) { onSeguir() }
}

/** A bancada de testes. Tocar num teste roda o Byte com ele no palco. */
@Composable
internal fun ListaTestes(v: Veredito?, noPalco: Int, onToca: (Int) -> Unit) {
    Text(tx("bancada_testes"), style = MaterialTheme.typography.titleSmall)
    if (v == null || v.testes.isEmpty()) {
        Text(tx("bancada_testes_vazio"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        return
    }
    Text(tx("bancada_rever"), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        v.testes.forEachIndexed { i, t ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(if (i == noPalco) Palette.paperDark else Palette.white)
                    .border(1.dp, if (t.passou) Palette.green.copy(alpha = 0.5f) else Palette.red, RoundedCornerShape(8.dp))
                    .clickable { onToca(i) }.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(if (t.passou) "✓" else "✗", color = if (t.passou) Palette.green else Palette.red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Column(Modifier.weight(1f)) {
                    Mono("${t.entrada}  →  ${t.obtido}", size = 13)
                    if (!t.passou) Text(tx("bancada_esperava", t.esperado), color = Palette.red, style = MaterialTheme.typography.bodySmall)
                    if (t.achado) Text(tx("bancada_achado"), color = Palette.teal, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    VSpace(8)
}

/** Fase que passou: grava, solta o confete e fala. */
internal fun comemora(onPassou: () -> Unit) {
    onPassou()
    Juice.correct()
}

internal fun lamenta() {
    Juice.wrong()
}
