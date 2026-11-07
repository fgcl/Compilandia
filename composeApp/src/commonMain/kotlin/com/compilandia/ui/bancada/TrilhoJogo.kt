package com.compilandia.ui.bancada

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.DFA
import com.compilandia.engine.Trilho
import com.compilandia.textos.tx
import com.compilandia.ui.AutomatonView
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState

/** O estado de uma fase do Trilho (J3): os trilhos ligados, os estados finais e a corrida no palco. */
class TrilhoEstado(val fase: Trilho.Fase) : EstadoBancada(fase.pedido) {
    val trilhos = mutableStateMapOf<Pair<Int, Char>, Int>()
    val finais = mutableStateListOf<Int>()
    /** As palavras da última rodada, na ordem da bancada de testes. */
    var palavras by mutableStateOf(fase.testes)
    var corrida by mutableStateOf<Trilho.Corrida?>(null)

    init { linha.carrega(1, tocar = false) }

    fun dfa(): DFA = Trilho.montado(fase, trilhos.toMap(), finais.toSet())

    /** Tocar na célula troca o destino: sem trilho, q0, q1, ..., sem trilho de novo. */
    fun gira(q: Int, c: Char) {
        val k = q to c
        val v = trilhos[k]
        when {
            v == null -> trilhos[k] = 0
            v + 1 >= fase.estados -> trilhos.remove(k)
            else -> trilhos[k] = v + 1
        }
        corrida = null
        mexeu()
    }

    fun alternaFinal(q: Int) {
        if (q in finais) finais.remove(q) else finais += q
        corrida = null
        mexeu()
    }

    fun limpa() {
        trilhos.clear()
        finais.clear()
        corrida = null
        mexeu()
        frase = fase.pedido
    }

    fun rodar(tocar: Boolean = true): com.compilandia.engine.Veredito {
        val d = dfa()
        palavras = Trilho.palavras(fase, d).map { it.first }
        val v = Trilho.confere(fase, d)
        aplica(v)
        mostra(v.falha ?: 0, tocar)
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        val c = Trilho.corre(fase, dfa(), palavras[i])
        corrida = c
        linha.carrega(c.quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = c.quadros.size - 1
    }

    fun pedeDica() = dica(fase.dicas) { Trilho.ondeOlhar(fase, dfa()) ?: fase.dicas.last() }
}

@Composable
fun TrilhoJogo(e: TrilhoEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val c = e.corrida
    val q = c?.quadros?.getOrElse(e.linha.atual) { c.quadros.last() }
    Arranjo(
        palco = {
            PalcoTrilho(e, c, q)
            ControlesPalco(e.linha, q?.nota ?: tx("trilho_toque_celula"))
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("trilho_tabela"), style = MaterialTheme.typography.titleSmall)
            Tabela(e, q)
            Text(tx("trilho_toque_celula") + " " + tx("trilho_toque_final"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            BotoesBancada(
                onRodar = {
                    val v = e.rodar()
                    if (v.passou) comemora(onPassou) else lamenta()
                },
                onDica = { e.pedeDica() },
                onLimpar = { e.limpa() },
            )
            ListaTestes(e.veredito, e.noPalco) { e.mostra(it) }
        },
    )
}

/** O autômato montado, com o trem andando, e a fita com a palavra embaixo. */
@Composable
private fun PalcoTrilho(e: TrilhoEstado, c: Trilho.Corrida?, q: Trilho.Quadro?) {
    val f = e.fase
    val arestas = e.trilhos.entries.map { (k, dest) -> Triple(k.first, f.rotulo(k.second), dest) }.sortedBy { it.second }
    val anda = remember { Animatable(1f) }
    LaunchedEffect(e.linha.atual, c) {
        if (q?.veioDe != null && !ThemeState.reduceMotion) {
            anda.snapTo(0f)
            anda.animateTo(1f, tween((e.linha.passoMs() * 0.7).toInt()))
        } else anda.snapTo(1f)
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.white).border(2.dp, Palette.ink.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(f.nome, style = MaterialTheme.typography.titleSmall, color = Palette.inkSoft)
        AutomatonView(
            f.estados, 0, e.finais.toSet(), arestas,
            highlight = setOfNotNull(q?.estado),
            lastEdge = q?.veioDe?.let { it to q.estado },
            traveler = q?.let { Triple(it.veioDe ?: it.estado, it.estado, if (it.veioDe != null) anda.value else 1f) },
            onTapState = { e.alternaFinal(it) },
        )
        Fita(c, q)
    }
}

/** A palavra, letra por letra: lida, a próxima, e em vermelho a que ficou sem trilho. */
@Composable
private fun Fita(c: Trilho.Corrida?, q: Trilho.Quadro?) {
    val mono = LocalFonts.current.mono
    Row(
        Modifier.horizontalScroll(rememberScrollState()).height(44.dp).padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (c == null) return@Row
        Text(tx("trilho_palavra"), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft, modifier = Modifier.padding(end = 6.dp))
        val letras = c.palavra.ifEmpty { "ε" }
        letras.forEachIndexed { k, ch ->
            val lidas = q?.lidas ?: 0
            val quebrou = q?.semTrilho == true && k == lidas
            val fundo = when {
                quebrou -> Palette.red
                c.palavra.isNotEmpty() && k < lidas -> Palette.paperDark
                c.palavra.isNotEmpty() && k == lidas && q?.let { it.lidas < c.palavra.length } == true -> Palette.yellow
                else -> Palette.white
            }
            Box(
                Modifier.size(32.dp, 36.dp).clip(RoundedCornerShape(6.dp)).background(fundo).border(1.dp, Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("$ch", fontFamily = mono, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Palette.on(fundo)) }
        }
    }
}

/** A tabela de transição: uma linha por estado, uma coluna por símbolo. É ela que liga os trilhos. */
@Composable
private fun Tabela(e: TrilhoEstado, q: Trilho.Quadro?) {
    val f = e.fase
    val mono = LocalFonts.current.mono
    // A célula que o trem acabou de usar fica acesa.
    val usada: Pair<Int, Char>? = q?.veioDe?.let { de ->
        val palavra = e.corrida?.palavra ?: return@let null
        val letra = palavra.getOrNull(q.lidas - 1) ?: return@let null
        de to f.classe(letra)
    }
    val quebra: Pair<Int, Char>? = if (q?.semTrilho == true) e.corrida?.palavra?.getOrNull(q.lidas)?.let { q.estado to f.classe(it) } else null
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.white).border(1.5.dp, Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tx("trilho_estado"), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            for (c in f.alfabeto) Text(tx("trilho_lendo", f.rotulo(c)), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft, textAlign = TextAlign.Center)
        }
        for (s in 0 until f.estados) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                val final = s in e.finais
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp)).background(if (final) Palette.hazard else Palette.paperDark)
                        .clickable { e.alternaFinal(s) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (final) "q$s · " + tx("trilho_final") else "q$s", fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = Palette.on(if (final) Palette.hazard else Palette.paperDark),
                    )
                }
                for (c in f.alfabeto) {
                    val d = e.trilhos[s to c]
                    val acesa = usada == (s to c)
                    val quebrou = quebra == (s to c)
                    val fundo = when { quebrou -> Palette.red; acesa -> Palette.yellow; d == null -> Palette.white; else -> Palette.teal.copy(alpha = 0.18f) }
                    val tinta = when { quebrou -> Palette.on(Palette.red); acesa -> Palette.on(Palette.yellow); else -> Palette.ink }
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp)).background(fundo)
                            .border(2.dp, if (d == null) Palette.ink.copy(alpha = 0.2f) else Palette.teal, RoundedCornerShape(8.dp))
                            .clickable { com.compilandia.platform.Sound.play(com.compilandia.platform.Sfx.CLICK); e.gira(s, c) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (d == null) Text(tx("trilho_resultado_sem_trilho"), style = MaterialTheme.typography.labelSmall, color = tinta.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                        else Text("q$d", fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = tinta)
                    }
                }
            }
        }
    }
}
