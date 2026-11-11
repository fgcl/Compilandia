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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.compilandia.tutor.carrega
import com.compilandia.tutor.corrida
import com.compilandia.tutor.planta
import com.compilandia.tutor.salva
import com.compilandia.ui.AppState
import com.compilandia.ui.Header
import com.compilandia.ui.LocalJanela
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado da tela: a gramática escrita, a planta montada e a corrida que está passando. */
internal class EstadoFabrica(texto0: String, frase0: String) {
    var texto by mutableStateOf(texto0)
    var frase by mutableStateOf(frase0)
    var planta by mutableStateOf<Planta?>(null)
    var corrida by mutableStateOf<Corrida?>(null)
    var t by mutableFloatStateOf(0f)
    var sel by mutableStateOf<Int?>(null)
    var ferramenta by mutableStateOf(Ferramenta.MOVER)
    var painel by mutableStateOf<Boolean?>(null)
    val terminou: Boolean get() = corrida?.let { t >= it.duracao } == true
}

/** Fábrica de Gramáticas: o palco é a tela; a gramática fica num painel que abre e fecha. */
@Composable
fun FabGramScreen(app: AppState) {
    val reduz = ThemeState.reduceMotion
    val ls = app.ls
    val e = remember { EstadoFabrica(ls.fabGramTexto.ifBlank { FabGram.exemplos[0].first }, ls.fabGramFrase.ifBlank { FabGram.exemplos[0].second }) }
    val laudo by produceState<Laudo?>(null, e.texto) { value = withContext(Dispatchers.Default) { Diagnostico.analisa(e.texto) } }
    LaunchedEffect(laudo) {
        val g = laudo?.g
        e.planta = g?.let { runCatching { (if (app.ls.fabGramTexto == e.texto) FabGram.carrega(it, app.ls.fabGramPlanta) else null) ?: FabGram.planta(it) }.getOrNull() }
    }
    val escopo = rememberCoroutineScope()
    var relogio by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reduz) {
        if (reduz) return@LaunchedEffect
        var antes = 0L
        while (true) withFrameNanos { n -> if (antes != 0L) relogio += (n - antes) / 1e9f; antes = n }
    }
    LaunchedEffect(e.corrida, reduz) {
        val c = e.corrida ?: return@LaunchedEffect
        if (reduz) { e.t = c.duracao; return@LaunchedEffect }
        e.t = 0f
        var antes = 0L
        while (e.t < c.duracao) withFrameNanos { n -> if (antes != 0L) e.t = (e.t + (n - antes) / 1e9f).coerceAtMost(c.duracao); antes = n }
    }
    fun guarda(pl: Planta) { e.planta = pl; e.corrida = null; app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl)); app.commit() }
    fun liga() {
        val pl = e.planta ?: return
        val toks = tokensOf(e.frase)
        if (toks.isEmpty()) return
        app.learner.setFabGram(e.texto, e.frase); app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl)); app.commit()
        escopo.launch { e.corrida = withContext(Dispatchers.Default) { FabGram.corrida(pl, toks) } }
    }
    val largo = LocalJanela.current.ladoALado
    val painelAberto = e.painel ?: largo
    val nomes = NomesBlocos(tx("fabgram_fonte"), { tx("fabgram_separador_1", it) }, tx("fabgram_montadora"), tx("fabgram_cesto"))

    Box(Modifier.fillMaxSize().background(Color(0xFF16181B))) {
        Column(Modifier.fillMaxSize()) {
            Header(tx("comum_fabrica_gramaticas"), null, Palette.drawer) {
                Text(
                    tx("fabgram_gramatica"), color = Palette.onAccent, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = if (painelAberto) 0.28f else 0.14f))
                        .clickable { e.painel = !painelAberto }.padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-painel"),
                )
            }
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val pl = e.planta
                    if (pl != null) PalcoEditavel(
                        pl, e.ferramenta, laudo, e.corrida, if (e.corrida == null) 0f else e.t, relogio, e.sel, reduz, nomes,
                        onPlanta = { guarda(it) }, onToque = { e.sel = it?.takeIf { b -> b.tipo == FabGram.TipoBloco.MAQUINA }?.id },
                    )
                    Column(Modifier.align(Alignment.TopCenter).padding(8.dp).widthIn(max = 640.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Casas(e.corrida, e.corrida?.pedida ?: tokensOf(e.frase), if (e.corrida == null) 0f else e.t)
                        Aviso(e, laudo)
                    }
                    Ferramentas(e, Modifier.align(Alignment.BottomCenter), onArrumar = { laudo?.g?.let { guarda(FabGram.planta(it)) } }, onLigar = { liga() })
                }
                if (largo && painelAberto) Column(Modifier.width(340.dp).fillMaxHeight().background(Palette.paper)) { Painel(e, laudo) { e.painel = false } }
            }
        }
        if (!largo && painelAberto) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { e.painel = false })
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.72f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(Palette.paper),
            ) { Painel(e, laudo) { e.painel = false } }
        }
    }
}
