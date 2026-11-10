package com.compilandia.ui.fabgram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.textos.tx
import com.compilandia.tutor.Diagnostico
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.Laudo
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.bancada.Arranjo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Fábrica de Gramáticas: a gramática escrita e a fábrica são a mesma coisa. */
@Composable
fun FabGramScreen(app: AppState) {
    val fonts = LocalFonts.current
    val reduz = ThemeState.reduceMotion
    var texto by remember { mutableStateOf(app.ls.fabGramTexto.ifBlank { FabGram.exemplos[0].first }) }
    var frase by remember { mutableStateOf(app.ls.fabGramFrase.ifBlank { FabGram.exemplos[0].second }) }
    val laudo by produceState<Laudo?>(null, texto) { value = withContext(Dispatchers.Default) { Diagnostico.analisa(texto) } }
    val planta = remember(laudo) { laudo?.g?.let { runCatching { FabGram.planta(it) }.getOrNull() } }
    var corrida by remember { mutableStateOf<FabGram.Corrida?>(null) }
    var t by remember { mutableFloatStateOf(0f) }
    var sel by remember { mutableStateOf<Int?>(null) }
    val escopo = rememberCoroutineScope()

    // relógio do palco: anda sempre (esteiras), e a corrida anda junto
    var relogio by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reduz) {
        if (reduz) return@LaunchedEffect
        var antes = 0L
        while (true) withFrameNanos { n -> if (antes != 0L) relogio += (n - antes) / 1e9f; antes = n }
    }
    LaunchedEffect(corrida, reduz) {
        val c = corrida ?: return@LaunchedEffect
        if (reduz) { t = c.duracao; return@LaunchedEffect }
        t = 0f
        var antes = 0L
        while (t < c.duracao) withFrameNanos { n -> if (antes != 0L) t = (t + (n - antes) / 1e9f).coerceAtMost(c.duracao); antes = n }
    }

    fun liga() {
        val pl = planta ?: return
        val toks = com.compilandia.engine.tokensOf(frase)
        if (toks.isEmpty()) return
        app.learner.setFabGram(texto, frase); app.commit()
        escopo.launch { corrida = withContext(Dispatchers.Default) { FabGram.corrida(pl, toks) } }
    }

    Column(Modifier.fillMaxSize().background(Palette.paper)) {
        Header(tx("comum_fabrica_gramaticas"), tx("fabgram_sub"), Palette.drawer)
        Arranjo(
            palco = {
                val c = corrida
                Casas(c, if (c == null) com.compilandia.engine.tokensOf(frase) else c.pedida, if (c == null) 0f else t)
                val pl = planta
                if (pl != null) {
                    val medidor = rememberTextMeasurer()
                    val nomes = NomesBlocos(
                        tx("fabgram_fonte"), tx("fabgram_separador"), tx("fabgram_filtro"), tx("fabgram_prensa"), tx("fabgram_braco"),
                        tx("fabgram_armario"), tx("fabgram_cofre"), tx("fabgram_montadora"), tx("fabgram_cesto"),
                    )
                    Box(Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                        Canvas(
                            Modifier.fillMaxWidth().aspectRatio(pl.largura.toFloat() / pl.altura).clip(RoundedCornerShape(8.dp)).testTag("fabgram-palco")
                                .pointerInput(pl) {
                                    detectTapGestures { o ->
                                        val cel = size.width / pl.largura.toFloat()
                                        val x = (o.x / cel).toInt(); val y = (o.y / cel).toInt()
                                        sel = pl.blocos.firstOrNull { it.tipo == FabGram.TipoBloco.MAQUINA && x in it.x until it.x + it.w && y in it.y until it.y + it.h }?.id
                                    }
                                },
                        ) {
                            desenhaFabrica(pl, laudo, c, if (c == null) 0f else t, relogio, sel, reduz, medidor, nomes)
                        }
                    }
                }
            },
            painel = {
                Text(tx("fabgram_gramatica"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
                BasicTextField(
                    texto, { texto = it; corrida = null; sel = null },
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(10.dp).testTag("fabgram-texto"),
                    textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 15.sp, color = androidx.compose.ui.graphics.Color(0xFFE8E4D8)),
                    cursorBrush = SolidColor(Palette.yellow),
                )
                Text(tx("fabgram_ajuda_texto"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
                Text(tx("fabgram_exemplos"), style = MaterialTheme.typography.labelLarge, color = Palette.ink)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((g, f) in FabGram.exemplos) {
                        Text(
                            g.lines().first(), fontFamily = fonts.mono, fontSize = 13.sp, color = Palette.ink,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Palette.paperDark)
                                .clickable { texto = g; frase = f; corrida = null; sel = null }.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
                Text(tx("fabgram_frase"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicTextField(
                        frase, { frase = it.take(40); corrida = null },
                        Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(horizontal = 10.dp, vertical = 10.dp).testTag("fabgram-frase"),
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFFE8E4D8)),
                        cursorBrush = SolidColor(Palette.yellow),
                    )
                    BigButton(tx("fabgram_ligar"), enabled = planta != null, small = true) { liga() }
                }
                // o que aconteceu
                val l = laudo
                val c = corrida
                val msg = when {
                    l?.erro != null -> tx("fabgram_erro_1", l.erro)
                    com.compilandia.engine.tokensOf(frase).isEmpty() -> tx("fabgram_vazia")
                    c == null || t < c.duracao -> null
                    c.aceita && c.ambigua -> tx("fabgram_ambigua")
                    c.aceita -> tx("fabgram_fez")
                    c.feita != null -> tx("fabgram_nao_faz_2", c.pedida.joinToString(" "), c.feita.joinToString(" ").ifEmpty { "ε" })
                    else -> tx("fabgram_nenhuma_1", c.pedida.joinToString(" "))
                }
                if (msg != null) Text(msg, color = Palette.ink, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("fabgram-msg"))
                val m = planta?.blocos?.firstOrNull { it.id == sel }
                Text(
                    if (m?.prod != null) tx("fabgram_maquina_1", m.prod.lhs + " → " + (m.prod.rhs.joinToString(" ").ifEmpty { "ε" })) else tx("fabgram_toque"),
                    color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium,
                )
                // sinais: cada defeito do Laboratório vira um sinal físico nas máquinas
                if (l?.g != null) {
                    Text(tx("fabgram_sinais"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
                    if (l.problemas.isEmpty()) Text(tx("fabgram_sem_sinais"), color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
                    for (p in l.problemas) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.padding(top = 4.dp).size(10.dp).clip(RoundedCornerShape(2.dp)).background(corSinal(p.falha)))
                            Column {
                                Text(nomeSinal(p.falha), fontWeight = FontWeight.Bold, color = Palette.ink, style = MaterialTheme.typography.bodyMedium)
                                Text(p.aponta, color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
        )
    }
}

/** A frase pedida no alto: cada casa se enche quando a peça chega à montadora. */
@Composable
private fun Casas(c: FabGram.Corrida?, pedida: List<String>, t: Float) {
    val fonts = LocalFonts.current
    val chegou = c?.chegadas?.filter { it.t <= t }.orEmpty()
    val n = maxOf(pedida.size, chegou.maxOfOrNull { it.casa + 1 } ?: 0)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until n) {
            val veio = chegou.firstOrNull { it.casa == i }?.simbolo
            val pede = pedida.getOrNull(i)
            val fim = c != null && t >= c.duracao
            val falta = fim && veio == null
            val bg = when {
                veio != null && veio == pede -> Palette.green
                veio != null -> Palette.red
                falta -> Palette.red.copy(alpha = 0.35f)
                else -> Palette.paperDark
            }
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(veio ?: pede ?: "·", fontFamily = fonts.mono, fontSize = 18.sp, color = if (veio != null) Palette.on(bg) else Palette.inkSoft)
            }
        }
    }
}

internal data class NomesBlocos(
    val fonte: String, val separador: String, val filtro: String, val prensa: String, val braco: String,
    val armario: String, val cofre: String, val montadora: String, val cesto: String,
)

@Composable
private fun nomeSinal(f: com.compilandia.tutor.Falha): String = when (f) {
    com.compilandia.tutor.Falha.IMPRODUTIVO -> tx("fabgram_sinal_fumaca")
    com.compilandia.tutor.Falha.INALCANCAVEL -> tx("fabgram_sinal_poeira")
    com.compilandia.tutor.Falha.RECURSAO, com.compilandia.tutor.Falha.RECURSAO_INDIRETA -> tx("fabgram_sinal_pisca")
    com.compilandia.tutor.Falha.PREFIXO -> tx("fabgram_sinal_lampada")
    com.compilandia.tutor.Falha.CONFLITO_LL, com.compilandia.tutor.Falha.CONFLITO_SLR -> tx("fabgram_sinal_emperra")
    com.compilandia.tutor.Falha.AMBIGUA -> tx("fabgram_sinal_fichas")
}
