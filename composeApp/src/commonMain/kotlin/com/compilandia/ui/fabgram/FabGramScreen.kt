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

/** Estado da tela: a gramática escrita, a planta montada, a missão do tutorial e a corrida que está passando. */
internal class EstadoFabrica(texto0: String, frase0: String) {
    var texto by mutableStateOf(texto0)
    var frase by mutableStateOf(frase0)
    var planta by mutableStateOf<Planta?>(null)
    var corrida by mutableStateOf<Corrida?>(null)
    var t by mutableFloatStateOf(0f)
    var sel by mutableStateOf<Int?>(null)
    var ferramenta by mutableStateOf(Ferramenta.MOVER)
    var painel by mutableStateOf<Boolean?>(null)
    /** A missão do tutorial em jogo; nulo é o modo livre. */
    var missao by mutableStateOf<MissaoPronta?>(null)
    var zoom by mutableFloatStateOf(1f)
    var pan by mutableStateOf(Offset.Zero)
    val desfazer = mutableStateListOf<Planta>()
    val refazer = mutableStateListOf<Planta>()
    /** Dicas pedidas no desafio: cada uma tira uma estrela. */
    var dicas by mutableStateOf(0)
    var dicaGuia by mutableStateOf<Map<FabGram.Cel, FabGram.Dir>>(emptyMap())
    /** Engrenagens que a missão cumprida rendeu agora. */
    var ganhou by mutableStateOf(0)
    /** O robô testou as frases extras do desafio: nulo enquanto testa. */
    var extrasOk by mutableStateOf<Boolean?>(null)
    /** A primeira frase extra que não saiu. */
    var extraFalhou by mutableStateOf<String?>(null)
    val terminou: Boolean get() = corrida?.let { t >= it.duracao } == true
    val cumpriu: Boolean get() = missao != null && terminou && corrida?.saiu == true && (missao?.missao?.extras.isNullOrEmpty() || extrasOk == true)
    /** O tutorial mostra o gesto até o jogador mexer na fábrica. */
    val mostrarMao: Boolean get() = missao.let { m -> m != null && !m.missao.desafio && desfazer.isEmpty() && corrida == null && (m.guiaEsteiras.isNotEmpty() || m.guiaBloco != null) }
    /** O Byte aparece no canto enquanto há um gesto para ensinar ou a primeira missão espera o Ligar. */
    val mostrarByte: Boolean get() = mostrarMao || (missao?.missao == Missao.LIGAR && corrida == null)
    /** Desafio: estrelas pelo número de esteiras usadas. */
    val estrelas: Int get() = (missao?.estrelas(planta?.esteiras?.size ?: 0) ?: 0).let { if (it == 0) 0 else maxOf(1, it - dicas) }

    /** Troca a planta guardando a anterior para o Desfazer. */
    fun troca(pl: Planta) {
        planta?.let { desfazer += it; if (desfazer.size > 60) desfazer.removeAt(0) }
        refazer.clear()
        planta = pl; corrida = null
    }
    fun volta() { val a = desfazer.removeLastOrNull() ?: return; planta?.let { refazer += it }; planta = a; corrida = null }
    fun avanca() { val a = refazer.removeLastOrNull() ?: return; planta?.let { desfazer += it }; planta = a; corrida = null }
    fun recomeca(pl: Planta?) { desfazer.clear(); refazer.clear(); planta = pl; corrida = null; sel = null; zoom = 1f; pan = Offset.Zero }

    fun abre(m: Missao) {
        val p = FabGram.prepara(m)
        missao = p; texto = m.gramatica; frase = m.frase; dicas = 0; dicaGuia = emptyMap(); ganhou = 0
        ferramenta = when (m) { Missao.MOVER, Missao.LIGAR -> Ferramenta.MOVER; else -> Ferramenta.ESTEIRA }
        recomeca(p.planta)
    }

    fun livre() { missao = null; ferramenta = Ferramenta.MOVER }
}

/** Fábrica de Gramáticas: o palco é a tela; a gramática fica num painel que abre e fecha. */
@Composable
fun FabGramScreen(app: AppState) {
    val reduz = ThemeState.reduceMotion
    val ls = app.ls
    val e = remember {
        EstadoFabrica(ls.fabGramTexto.ifBlank { FabGram.exemplos[0].first }, ls.fabGramFrase.ifBlank { FabGram.exemplos[0].second }).also {
            // a trilha pode pedir uma missão; quem chega pela primeira vez cai no tutorial, na missão em que parou
            val pedida = app.fabGramPedida?.let { n -> Missao.entries.firstOrNull { m -> m.name == n } }
            app.fabGramPedida = null
            if (pedida != null) it.abre(pedida)
            else if (ls.fabGramMissoes < Missao.entries.size && ls.fabGramPlanta.isBlank()) it.abre(Missao.entries[ls.fabGramMissoes])
        }
    }
    val laudo by produceState<Laudo?>(null, e.texto) { value = withContext(Dispatchers.Default) { Diagnostico.analisa(e.texto) } }
    LaunchedEffect(laudo) {
        val l = laudo ?: return@LaunchedEffect
        if (e.missao?.missao?.gramatica == e.texto) return@LaunchedEffect
        val g = l.g ?: run { e.planta = null; return@LaunchedEffect }
        e.recomeca(runCatching { (if (app.ls.fabGramTexto == e.texto) FabGram.carrega(g, app.ls.fabGramPlanta) else null) ?: FabGram.planta(g) }.getOrNull())
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
    LaunchedEffect(e.cumpriu) {
        val m = e.missao ?: return@LaunchedEffect
        if (e.cumpriu) {
            e.ganhou = app.learner.cumpreMissaoFabrica(m.missao.name, m.missao.ordinal, if (m.missao.desafio) e.estrelas else 0)
            app.commit()
        }
    }
    fun guarda(pl: Planta) {
        e.troca(pl)
        if (e.missao == null) { app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl)); app.commit() }
    }
    fun liga() {
        val pl = e.planta ?: return
        val toks = tokensOf(e.frase)
        if (toks.isEmpty()) return
        if (e.missao == null) { app.learner.setFabGram(e.texto, e.frase); app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl)); app.commit() }
        val m = e.missao
        e.extrasOk = null; e.extraFalhou = null
        escopo.launch {
            e.corrida = withContext(Dispatchers.Default) { FabGram.corrida(pl, toks) }
            if (m != null && m.missao.extras.isNotEmpty()) {
                val falha = withContext(Dispatchers.Default) { m.missao.extras.firstOrNull { !FabGram.corrida(pl, tokensOf(it)).saiu } }
                e.extraFalhou = falha; e.extrasOk = falha == null
            }
        }
    }
    val largo = LocalJanela.current.ladoALado
    val painelAberto = e.painel ?: (largo && e.missao == null)
    val nomes = NomesBlocos(tx("fabgram_fonte"), { tx("fabgram_separador_1", it) }, tx("fabgram_montadora"), tx("fabgram_cesto"), tx("fabgram_roteador"))

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
                        pl, e, laudo, relogio, reduz, nomes,
                        onPlanta = { guarda(it) }, onToque = { e.sel = it?.takeIf { b -> b.tipo == FabGram.TipoBloco.MAQUINA }?.id },
                    )
                    Column(Modifier.align(Alignment.TopCenter).padding(8.dp).widthIn(max = 640.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f, fill = false)) { Casas(e.corrida, e.corrida?.pedida ?: tokensOf(e.frase), if (e.corrida == null) 0f else e.t) }
                            BigButton(tx("fabgram_ligar"), enabled = e.planta != null, small = true) { liga() }
                        }
                        if (e.missao != null) CartaoMissao(e) { prox -> if (prox == null) { e.livre(); laudo?.g?.let { e.recomeca(FabGram.planta(it)) } } else e.abre(prox) }
                        Aviso(e, laudo)
                    }
                    if (e.mostrarByte) com.compilandia.ui.cast.ByteFigure(
                        Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 64.dp).width(54.dp),
                        mood = com.compilandia.ui.cast.Mood.HAPPY,
                    )
                    Ferramentas(e, Modifier.align(Alignment.BottomCenter), onArrumar = {
                        val m = e.missao
                        if (m != null) e.abre(m.missao) else laudo?.g?.let { guarda(FabGram.planta(it)); e.zoom = 1f; e.pan = Offset.Zero }
                    })
                }
                if (largo && painelAberto) Column(Modifier.width(340.dp).fillMaxHeight().background(Palette.paper)) { Painel(e, laudo, ls.fabGramMissoes, ls.fabGramEstrelas) { e.painel = false } }
            }
        }
        if (!largo && painelAberto) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { e.painel = false })
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.72f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(Palette.paper),
            ) { Painel(e, laudo, ls.fabGramMissoes, ls.fabGramEstrelas) { e.painel = false } }
        }
    }
}
