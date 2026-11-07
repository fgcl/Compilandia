package com.compilandia.ui.bancada

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.Fabrica
import com.compilandia.engine.Frases
import com.compilandia.engine.Veredito
import com.compilandia.engine.tokensOf
import com.compilandia.textos.tx
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.SymbolsText
import com.compilandia.ui.ThemeState
import com.compilandia.ui.TreeView
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.toUi

/** O estado de uma fase da Fábrica de frases (J6): as máquinas ligadas e a montagem no palco. */
class FabricaEstado(val fase: Fabrica.Fase) : EstadoBancada(fase.pedido) {
    val ligadas = mutableStateListOf<Int>()
    var frases by mutableStateOf(fase.testes.map { tokensOf(it) })
    var montagem by mutableStateOf<Fabrica.Montagem?>(null)
    /** As primeiras frases que saem da fábrica, para a esteira de saída. */
    var esteira by mutableStateOf<List<Pair<List<String>, Int>>>(emptyList())

    init { linha.carrega(1, tocar = false) }

    fun ligadasSet() = ligadas.toSet()

    fun alterna(i: Int) {
        if (i in ligadas) ligadas.remove(i) else ligadas += i
        montagem = null
        esteira = emptyList()
        mexeu()
    }

    fun limpa() {
        ligadas.clear()
        montagem = null
        esteira = emptyList()
        mexeu()
        frase = fase.pedido
    }

    fun rodar(tocar: Boolean = true): Veredito {
        val l = ligadasSet()
        val v = Fabrica.confere(fase, l)
        frases = v.testes.map { if (it.entrada == com.compilandia.engine.EPS) emptyList() else tokensOf(it.entrada) }
        esteira = if (v.falha == -1) emptyList() else Fabrica.esteira(fase, l)
        aplica(v)
        if (v.falha != -1) mostra(v.falha ?: 0, tocar)
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        val m = Fabrica.monta(fase, ligadasSet(), frases[i])
        montagem = m
        linha.carrega(m.quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = m.quadros.size - 1
    }

    fun pedeDica() = dica(fase.dicas) { Fabrica.ondeOlhar(fase, ligadasSet()) ?: fase.dicas.last() }
}

@Composable
fun FabricaJogo(e: FabricaEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val m = e.montagem
    val q = m?.quadros?.getOrElse(e.linha.atual) { m.quadros.last() }
    Arranjo(
        palco = {
            PalcoFabrica(e, m, q)
            ControlesPalco(e.linha, q?.nota ?: tx("fabrica_toque_maquina"))
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("fabrica_maquinas"), style = MaterialTheme.typography.titleSmall)
            Text(tx("fabrica_toque_maquina"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            Maquinas(e, q?.peca)
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

/** As máquinas da mesa. Ligada fica amarela; a que acabou de trabalhar no palco fica com borda acesa. */
@Composable
private fun Maquinas(e: FabricaEstado, trabalhando: Int?) {
    val mono = LocalFonts.current.mono
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        e.fase.prods.forEachIndexed { i, p ->
            val liga = i in e.ligadas
            val fundo = if (liga) Palette.hazard else Palette.codeBg
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp)).background(fundo)
                    .border(3.dp, if (i == trabalhando) Palette.teal else if (liga) Palette.hazardDark else Palette.onAccent.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .clickable { com.compilandia.platform.Sound.play(com.compilandia.platform.Sfx.CLICK); e.alterna(i) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(p.toString(), Modifier.weight(1f), fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (liga) Palette.on(Palette.hazard) else Palette.onAccent)
                Text(if (liga) tx("fabrica_ligada") else tx("fabrica_desligada"), style = MaterialTheme.typography.labelSmall, color = if (liga) Palette.on(Palette.hazard) else Palette.onAccent.copy(alpha = 0.6f))
            }
        }
    }
}

/**
 * O palco: a forma da frase na esteira, a máquina que trabalhou, e no fim a árvore.
 * Quando a frase sai de dois jeitos, as duas árvores aparecem uma embaixo da outra.
 */
@Composable
private fun PalcoFabrica(e: FabricaEstado, m: Fabrica.Montagem?, q: Fabrica.Quadro?) {
    val mono = LocalFonts.current.mono
    Column(
        Modifier.fillMaxWidth().heightIn(min = 200.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val humor = when {
                q == null || m == null -> Mood.NEUTRAL
                m.arvores.isEmpty() && e.linha.atual == m.quadros.lastIndex -> Mood.SAD
                m.arvores.size >= 2 && e.linha.atual == m.quadros.lastIndex -> Mood.SAD
                else -> Mood.NEUTRAL
            }
            ByteFigure(Modifier.width(44.dp), mood = humor, animate = !ThemeState.reduceMotion)
            Column(Modifier.padding(start = 10.dp)) {
                Text(tx("fabrica_na_esteira"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                val forma = q?.forma
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.white).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    if (forma != null && m?.grammar != null) SymbolsText(m.grammar, forma, size = 17)
                    else Text(e.fase.inicial, fontFamily = mono, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Palette.nonTerminal)
                }
            }
        }
        // No último quadro, a árvore (ou as duas). Antes disso, a derivação até aqui.
        if (m != null && q != null) {
            val fim = e.linha.atual == m.quadros.lastIndex
            if (fim && m.arvores.isNotEmpty() && m.grammar != null) {
                m.arvores.forEachIndexed { k, t ->
                    if (m.arvores.size > 1) Text(if (k == 0) tx("comum_arvore_1") else tx("comum_arvore_2"), color = Palette.yellow, style = MaterialTheme.typography.labelMedium)
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper)) {
                        val ui = remember(t) { t.toUi(m.grammar) }
                        TreeView(ui)
                    }
                }
            } else {
                val passos = m.quadros.take(e.linha.atual + 1).filter { it.arvore == q.arvore }.map { Frases.texto(it.forma) }
                Text(passos.joinToString("  ⇒  "), color = Palette.onAccent, fontFamily = mono, fontSize = 13.sp)
            }
        }
        // A esteira de saída: as primeiras frases que a fábrica faz, das curtas para as longas.
        if (e.esteira.isNotEmpty()) {
            Text(tx("fabrica_saem"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for ((w, n) in e.esteira) {
                    val certa = e.fase.alvo(w) && (!e.fase.semAmbiguidade || n < 2)
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(if (certa) Palette.green else Palette.red).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { Text(Frases.texto(w), fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(if (certa) Palette.green else Palette.red)) }
                }
            }
        }
    }
}
