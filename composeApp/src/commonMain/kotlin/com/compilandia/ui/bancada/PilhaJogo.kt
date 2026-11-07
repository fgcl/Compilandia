package com.compilandia.ui.bancada

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.EOF
import com.compilandia.engine.RoboPilha
import com.compilandia.engine.Veredito
import com.compilandia.textos.tx
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood

/** O estado de uma fase do Byte com a pilha (J4): a tabela que a pessoa preenche e a corrida no palco. */
class PilhaEstado(val fase: RoboPilha.Fase) : EstadoBancada(fase.pedido) {
    /** Célula → id da produção. Célula fora do mapa está vazia. */
    val tabela = mutableStateMapOf<Pair<String, String>, Int>()
    var frases by mutableStateOf(fase.testes.map { com.compilandia.engine.tokensOf(it) })
    var corrida by mutableStateOf<RoboPilha.Corrida?>(null)
    /** A célula que a bancada apontou quando todas as frases passaram mas a tabela ainda tem sobra. */
    var celulaApontada by mutableStateOf<Pair<String, String>?>(null)

    init { linha.carrega(1, tocar = false) }

    fun tabelaAtual(): Map<Pair<String, String>, Int> = tabela.toMap()

    /** Tocar na célula troca a regra: vazia, a primeira regra do não-terminal, a segunda, ..., vazia de novo. */
    fun gira(nt: String, col: String) {
        val ids = fase.g.productionsOf(nt).map { it.id }
        val k = nt to col
        val i = tabela[k]?.let { ids.indexOf(it) } ?: -1
        if (i + 1 >= ids.size) tabela.remove(k) else tabela[k] = ids[i + 1]
        corrida = null
        celulaApontada = null
        mexeu()
    }

    fun limpa() {
        tabela.clear()
        corrida = null
        celulaApontada = null
        mexeu()
        frase = fase.pedido
    }

    fun rodar(tocar: Boolean = true): Veredito {
        val t = tabelaAtual()
        frases = RoboPilha.frases(fase, t).map { it.first }
        val v = RoboPilha.confere(fase, t)
        aplica(v)
        if (v.falha != -1) mostra(v.falha ?: 0, tocar)
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        if (i >= frases.size) {
            // A linha da célula sobrando: o palco só acende a célula.
            corrida = null
            celulaApontada = RoboPilha.celulaDiferente(fase, tabelaAtual())
            linha.carrega(1, tocar = false)
            return
        }
        celulaApontada = null
        val c = RoboPilha.corre(fase, tabelaAtual(), frases[i])
        corrida = c
        linha.carrega(c.quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = c.quadros.size - 1
    }

    fun pedeDica() = dica(fase.dicas) { RoboPilha.ondeOlhar(fase, tabelaAtual()) ?: fase.dicas.last() }
}

@Composable
fun PilhaJogo(e: PilhaEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val c = e.corrida
    val q = c?.quadros?.getOrElse(e.linha.atual) { c.quadros.last() }
    Arranjo(
        palco = {
            PalcoPilha(e, c, q)
            ControlesPalco(e.linha, q?.nota ?: tx("robopilha_toque_celula"))
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("robopilha_gramatica"), style = MaterialTheme.typography.titleSmall)
            GrammarText(e.fase.g)
            Text(tx("robopilha_tabela"), style = MaterialTheme.typography.titleSmall)
            TabelaLL(e, q)
            Text(tx("robopilha_toque_celula"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            BotoesBancada(
                onRodar = {
                    val v = e.rodar()
                    if (v.passou) comemora(onPassou) else if (v.falha != -1) lamenta()
                },
                onDica = { e.pedeDica() },
                onLimpar = { e.limpa() },
            )
            ListaTestes(e.veredito, e.noPalco) { e.mostra(it) }
        },
    )
}

private val COR_PILHA = Color(0xFF3A3F44)

/** O palco: a entrada em cima, a pilha nas costas do Byte e a célula da tabela que ele consultou. */
@Composable
private fun PalcoPilha(e: PilhaEstado, c: RoboPilha.Corrida?, q: RoboPilha.Quadro?) {
    val g = e.fase.g
    val mono = LocalFonts.current.mono
    Column(
        Modifier.fillMaxWidth().heightIn(min = 200.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // A entrada: o que já foi lido fica apagado; o símbolo da vez, aceso.
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(tx("robopilha_entrada"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 6.dp))
            val entrada = (c?.frase ?: emptyList()) + EOF
            val lidos = q?.lidos ?: 0
            entrada.forEachIndexed { k, s ->
                val fundo = when {
                    k < lidos -> Color.White.copy(alpha = 0.08f)
                    k == lidos -> Palette.yellow
                    else -> Color.White.copy(alpha = 0.18f)
                }
                Box(Modifier.heightIn(min = 34.dp).widthIn(min = 30.dp).clip(RoundedCornerShape(6.dp)).background(fundo).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                    Text(s, fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (k == lidos) Palette.on(Palette.yellow) else Palette.onAccent.copy(alpha = if (k < lidos) 0.4f else 1f))
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // A pilha: o topo em cima. O Byte fica ao lado, na altura do topo.
            val pilha = q?.pilha ?: listOf(EOF, g.start)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(tx("robopilha_pilha"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                Column(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(COR_PILHA).padding(6.dp).widthIn(min = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    pilha.asReversed().take(9).forEachIndexed { k, s ->
                        val topo = k == 0
                        val cor = when { s == EOF -> Palette.orange; g.isNonTerminal(s) -> Palette.nodeNonTerminal; else -> Palette.nodeLeaf }
                        Box(
                            Modifier.widthIn(min = 52.dp).height(28.dp).clip(RoundedCornerShape(5.dp)).background(cor)
                                .border(if (topo) 3.dp else 0.dp, if (topo) Palette.yellow else cor, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(s, fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.inkOnLight) }
                    }
                    if (pilha.size > 9) Text("…", color = Palette.onAccent)
                }
            }
            val sobe by animateDpAsState(if (q?.acao == RoboPilha.Acao.EXPANDE || q?.acao == RoboPilha.Acao.CASA) (-6).dp else 0.dp, tween(if (ThemeState.reduceMotion) 0 else 220), label = "byteSobe")
            val humor = when (q?.acao) {
                RoboPilha.Acao.ACEITA -> Mood.HAPPY
                RoboPilha.Acao.VAZIA, RoboPilha.Acao.NAO_CASA, RoboPilha.Acao.SOBROU, RoboPilha.Acao.LONGA -> Mood.SAD
                else -> Mood.NEUTRAL
            }
            ByteFigure(Modifier.width(56.dp).offset(y = sobe), mood = humor, animate = !ThemeState.reduceMotion)
            Spacer(Modifier.weight(1f))
            CartaoCelula(e, q)
        }
    }
}

/** A célula que o Byte acabou de ler: M[X, a] → regra. Vermelha quando está vazia. */
@Composable
private fun CartaoCelula(e: PilhaEstado, q: RoboPilha.Quadro?) {
    val mono = LocalFonts.current.mono
    val cel = q?.celula ?: e.celulaApontada
    val vazia = q?.acao == RoboPilha.Acao.VAZIA
    val borda = when { cel == null -> Palette.onAccent.copy(alpha = 0.3f); vazia || e.celulaApontada != null -> Palette.red; else -> Palette.yellow }
    Column(
        Modifier.widthIn(min = 120.dp).clip(RoundedCornerShape(10.dp)).border(3.dp, borda, RoundedCornerShape(10.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(tx("robopilha_celula"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(cel?.let { "M[${it.first}, ${it.second}]" } ?: "M[ , ]", fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.onAccent)
        val regra = when {
            q?.prod != null -> q.prod.toString()
            vazia -> tx("robopilha_celula_vazia")
            e.celulaApontada != null -> e.tabela[e.celulaApontada]?.let { e.fase.g.productions[it].toString() } ?: tx("robopilha_celula_vazia")
            else -> ""
        }
        Text(regra, fontFamily = mono, fontSize = 14.sp, color = if (vazia) Palette.red else Palette.yellow, textAlign = TextAlign.Center)
    }
}

/** A tabela LL(1): uma linha por não-terminal, uma coluna por terminal. É ela que a pessoa preenche. */
@Composable
private fun TabelaLL(e: PilhaEstado, q: RoboPilha.Quadro?) {
    val f = e.fase
    val mono = LocalFonts.current.mono
    val acesa = q?.celula ?: e.celulaApontada
    val quebra = if (q?.acao == RoboPilha.Acao.VAZIA || e.celulaApontada != null) acesa else null
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.white).border(1.5.dp, Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("M", Modifier.weight(0.7f), fontFamily = mono, color = Palette.inkSoft, textAlign = TextAlign.Center)
            for (c in f.colunas) Text(c, Modifier.weight(1f), fontFamily = mono, fontWeight = FontWeight.Bold, color = Palette.terminal, textAlign = TextAlign.Center)
        }
        for (nt in f.g.nonTerminals) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(0.7f).height(44.dp).clip(RoundedCornerShape(8.dp)).background(Palette.paperDark), contentAlignment = Alignment.Center) {
                    Text(nt, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Palette.nonTerminal)
                }
                for (c in f.colunas) {
                    val pid = e.tabela[nt to c]
                    val k = nt to c
                    val fundo = when { k == quebra -> Palette.red; k == acesa -> Palette.yellow; pid == null -> Palette.white; else -> Palette.teal.copy(alpha = 0.18f) }
                    val tinta = when { k == quebra -> Palette.on(Palette.red); k == acesa -> Palette.on(Palette.yellow); else -> Palette.ink }
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp)).background(fundo)
                            .border(2.dp, if (pid == null) Palette.ink.copy(alpha = 0.2f) else Palette.teal, RoundedCornerShape(8.dp))
                            .clickable { com.compilandia.platform.Sound.play(com.compilandia.platform.Sfx.CLICK); e.gira(nt, c) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pid != null) Text(f.g.productions[pid].rhsText(), fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tinta, textAlign = TextAlign.Center, lineHeight = 13.sp)
                    }
                }
            }
        }
    }
}
