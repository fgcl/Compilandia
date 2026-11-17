package com.compilandia.ui.fabgram

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.compilandia.engine.tokensOf
import com.compilandia.textos.tx
import com.compilandia.tutor.Corrida
import com.compilandia.tutor.Diagnostico
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.Laudo
import com.compilandia.tutor.Missao
import com.compilandia.tutor.MissaoPronta
import com.compilandia.tutor.carrega
import com.compilandia.tutor.corrida
import com.compilandia.tutor.planta
import com.compilandia.tutor.prepara
import com.compilandia.tutor.salva
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.LocalJanela
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A tela abre no tutorial, na missão em que o jogador parou, ou na missão que a trilha pediu. */
private fun estadoInicial(app: AppState): EstadoFabrica {
    val ls = app.ls
    val e = EstadoFabrica(
        ls.fabGramTexto.ifBlank { FabGram.exemplos[0].first },
        ls.fabGramFrase.ifBlank { FabGram.exemplos[0].second },
    )
    val pedida = app.fabGramPedida?.let { n -> Missao.entries.firstOrNull { m -> m.name == n } }
    app.fabGramPedida = null
    if (pedida != null) e.abre(pedida)
    else if (ls.fabGramMissoes < Missao.entries.size && ls.fabGramPlanta.isBlank()) e.abre(Missao.entries[ls.fabGramMissoes])
    return e
}

/**
 * Texto novo no painel: a planta segue, a não ser que o texto tenha saído da própria planta
 * (o jogador mexeu numa máquina) ou seja o de uma missão. Texto com erro não apaga a fábrica.
 */
private fun plantaDoTexto(app: AppState, e: EstadoFabrica, l: Laudo) {
    if (e.missao?.missao?.gramatica == e.texto || e.textoDaPlanta == e.texto) return
    val g = l.g ?: return
    val salva = if (app.ls.fabGramTexto == e.texto) FabGram.carrega(app.ls.fabGramPlanta) else null
    e.recomeca(runCatching { salva ?: FabGram.planta(g) }.getOrNull())
}

/** Fábrica de Gramáticas: o palco é a tela; a gramática fica num painel que abre e fecha. */
@Composable
fun FabGramScreen(app: AppState) {
    val reduz = ThemeState.reduceMotion
    val e = remember { estadoInicial(app) }
    val escopo = rememberCoroutineScope()
    val ctl = remember { ControleFabrica(app, e, escopo) }
    val laudo by produceState<Laudo?>(null, e.texto) { value = withContext(Dispatchers.Default) { Diagnostico.analisa(e.texto) } }
    LaunchedEffect(laudo) { laudo?.let { plantaDoTexto(app, e, it) } }
    val relogio = relogioDoPalco(reduz)
    TempoDaCorrida(e, reduz)
    LaunchedEffect(e.cumpriu) {
        val m = e.missao ?: return@LaunchedEffect
        if (!e.cumpriu) return@LaunchedEffect
        e.ganhou = app.learner.cumpreMissaoFabrica(m.missao.name, m.missao.ordinal, if (m.missao.desafio) e.estrelas else 0)
        app.commit()
    }
    val largo = LocalJanela.current.ladoALado
    val painelAberto = e.painel ?: (largo && e.missao == null)
    Box(Modifier.fillMaxSize().background(FUNDO_TELA)) {
        Column(Modifier.fillMaxSize()) {
            Cabecalho(e, painelAberto)
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Palco(app, e, ctl, laudo, relogio, reduz, Modifier.weight(1f).fillMaxHeight())
                if (largo && painelAberto) Column(Modifier.width(340.dp).fillMaxHeight().background(Palette.paper)) {
                    Painel(e, laudo, app.ls.fabGramMissoes, app.ls.fabGramEstrelas) { e.painel = false }
                }
            }
        }
        if (!largo && painelAberto) PainelDeBaixo(app, e, laudo)
    }
}

@Composable
private fun relogioDoPalco(reduz: Boolean): Float {
    var relogio by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reduz) {
        if (reduz) return@LaunchedEffect
        var antes = 0L
        while (true) withFrameNanos { n -> if (antes != 0L) relogio += (n - antes) / NANO; antes = n }
    }
    return relogio
}

@Composable
private fun TempoDaCorrida(e: EstadoFabrica, reduz: Boolean) {
    LaunchedEffect(e.corrida, reduz) {
        val c = e.corrida ?: return@LaunchedEffect
        if (reduz) { e.t = c.duracao; return@LaunchedEffect }
        e.t = 0f
        var antes = 0L
        while (e.t < c.duracao) withFrameNanos { n ->
            if (antes != 0L) e.t = (e.t + (n - antes) / NANO).coerceAtMost(c.duracao)
            antes = n
        }
    }
}

private const val NANO = 1e9f

@Composable
private fun Cabecalho(e: EstadoFabrica, painelAberto: Boolean) {
    Header(tx("comum_fabrica_gramaticas"), null, Palette.drawer) {
        Text(
            tx("fabgram_gramatica"), color = Palette.onAccent, style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = if (painelAberto) 0.28f else 0.14f))
                .clickable { e.painel = !painelAberto }.padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-painel"),
        )
    }
}

@Composable
private fun nomesDosBlocos() = NomesBlocos(
    tx("fabgram_fonte"), { tx("fabgram_separador_1", it) }, tx("fabgram_montadora"), tx("fabgram_cesto"), tx("fabgram_roteador"),
)

/** O palco com tudo que flutua sobre ele: a frase e o Ligar no alto, o menu embaixo, e os cartões. */
@Composable
private fun Palco(app: AppState, e: EstadoFabrica, ctl: ControleFabrica, laudo: Laudo?, relogio: Float, reduz: Boolean, modifier: Modifier) {
    Box(modifier) {
        e.planta?.let { pl ->
            PalcoEditavel(
                pl, e, laudo, relogio, reduz, nomesDosBlocos(),
                onPlanta = { ctl.guarda(it) }, onToque = { ctl.toca(it) }, onConstruir = { c, area -> ctl.constroi(c, area) },
            )
        }
        Topo(e, ctl, laudo, Modifier.align(Alignment.TopCenter))
        if (e.mostrarByte) com.compilandia.ui.cast.ByteFigure(
            Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 170.dp).width(54.dp),
            mood = com.compilandia.ui.cast.Mood.HAPPY,
        )
        MenuConstrucao(e, app.ls.fabGramBlocos, Modifier.align(Alignment.BottomCenter)) { ctl.arruma(laudo?.g) }
        Cartoes(app, e, ctl, Modifier.align(Alignment.Center))
    }
}

@Composable
private fun Topo(e: EstadoFabrica, ctl: ControleFabrica, laudo: Laudo?, modifier: Modifier) {
    Column(
        modifier.padding(8.dp).widthIn(max = 640.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f, fill = false)) { Casas(e.corrida, e.corrida?.pedida ?: tokensOf(e.frase), if (e.corrida == null) 0f else e.t) }
            BigButton(tx("fabgram_ligar"), enabled = e.planta != null, small = true) { ctl.liga() }
        }
        if (e.missao != null) CartaoMissao(e) { prox ->
            if (prox != null) e.abre(prox)
            else { e.livre(); laudo?.g?.let { e.recomeca(FabGram.planta(it)) } }
        }
        Aviso(e, laudo)
    }
}

/** A loja e o painel de configurar, no meio do palco. */
@Composable
private fun Cartoes(app: AppState, e: EstadoFabrica, ctl: ControleFabrica, modifier: Modifier) {
    e.oferta?.let { t ->
        CartaoLoja(t, app.ls.fabGramBlocos, app.ls.gears, modifier, onComprar = { ctl.compra(t) }, onFechar = { e.oferta = null })
    }
    val pl = e.planta ?: return
    val b = pl.blocos.firstOrNull { it.id == e.config } ?: return
    ConfigBloco(pl, b, modifier, onPronto = { ctl.guarda(it); e.config = null }, onFechar = { e.config = null })
}

@Composable
private fun PainelDeBaixo(app: AppState, e: EstadoFabrica, laudo: Laudo?) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { e.painel = false })
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(Palette.paper),
        ) { Painel(e, laudo, app.ls.fabGramMissoes, app.ls.fabGramEstrelas) { e.painel = false } }
    }
}

private val FUNDO_TELA = Color(0xFF16181B)
