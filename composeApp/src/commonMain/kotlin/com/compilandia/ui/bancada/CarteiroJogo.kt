package com.compilandia.ui.bancada

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.Carteiro
import com.compilandia.engine.Veredito
import com.compilandia.textos.tx
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood

/** O estado de uma fase do Carteiro do escopo (J7): em que sala está cada carta e a entrega no palco. */
class CarteiroEstado(val fase: Carteiro.Fase) : EstadoBancada(fase.pedido) {
    /** carta → sala. Carta fora do mapa está na mão. */
    val montagem = mutableStateMapOf<String, String>()
    var entrega by mutableStateOf<Carteiro.Entrega?>(null)

    init { linha.carrega(1, tocar = false) }

    fun poe(carta: String, sala: String?) {
        if (sala == null) montagem.remove(carta) else montagem[carta] = sala
        entrega = null
        mexeu()
    }

    fun limpa() {
        montagem.clear()
        entrega = null
        mexeu()
        frase = fase.pedido
    }

    fun rodar(tocar: Boolean = true): Veredito {
        val v = Carteiro.confere(fase, montagem.toMap())
        aplica(v)
        if (v.falha != -1) mostra(v.falha ?: 0, tocar) else entrega = null
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        val e = Carteiro.entrega(fase, montagem.toMap(), fase.usos[i])
        entrega = e
        linha.carrega(e.quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = e.quadros.size - 1
    }

    fun pedeDica() = dica(fase.dicas) { Carteiro.ondeOlhar(fase, montagem.toMap()) ?: fase.dicas.last() }
}

@Composable
fun CarteiroJogo(e: CarteiroEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val ent = e.entrega
    val q = ent?.quadros?.getOrElse(e.linha.atual) { ent.quadros.last() }
    Arranjo(
        palco = {
            PalcoCarteiro(e, q, fim = ent != null && e.linha.atual == ent.quadros.lastIndex)
            ControlesPalco(e.linha, q?.nota ?: tx("carteiro_nota_escolha"))
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("carteiro_cartas"), style = MaterialTheme.typography.titleSmall)
            Text(tx("carteiro_cartas_ajuda"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            Cartas(e)
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

/** Cada carta com as salas para escolher. A sala escolhida fica amarela. */
@Composable
private fun Cartas(e: CarteiroEstado) {
    val f = e.fase
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (c in f.cartas) {
            PaperCard(Modifier.fillMaxWidth(), border = if (c.id in e.montagem) Palette.hazardDark else Palette.ink.copy(alpha = 0.3f)) {
                Text(c.texto, fontFamily = LocalFonts.current.mono, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.ink)
                ChipRow {
                    Chip(tx("carteiro_mao"), selected = c.id !in e.montagem, color = Palette.inkSoft) { e.poe(c.id, null) }
                    for (s in f.salas) Chip(Carteiro.nomeSala(f, s.id), selected = e.montagem[c.id] == s.id, color = Palette.hazard, mono = true) { e.poe(c.id, s.id) }
                }
            }
        }
    }
}

/**
 * O palco é o próprio programa: cada sala é um bloco com borda, dentro do bloco de fora.
 * A sala onde o Byte está acende, e a linha do uso que ele entrega fica amarela.
 */
@Composable
private fun PalcoCarteiro(e: CarteiroEstado, q: Carteiro.Quadro?, fim: Boolean) {
    val f = e.fase
    val montagem = e.montagem.toMap()
    val linhas = Carteiro.programa(f, montagem)
    val mono = LocalFonts.current.mono
    val usoAtual = q?.uso
    val nomeAtual = usoAtual?.let { u -> f.usos.first { it.id == u }.nome }
    // As linhas de cada sala, para desenhar bloco dentro de bloco.
    @Composable
    fun bloco(salaId: String) {
        val s = f.sala(salaId)
        val aqui = q?.sala == salaId
        val semNada = fim && aqui && q?.achou == null
        val borda = when {
            semNada -> Palette.red
            aqui -> Palette.teal
            else -> Palette.onAccent.copy(alpha = 0.25f)
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(if (aqui) 3.dp else 1.dp, borda, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (s.cabeca.isEmpty()) "// " + Carteiro.nomeSala(f, salaId) else s.cabeca,
                    Modifier.weight(1f), fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (s.cabeca.isEmpty()) Palette.onAccent.copy(alpha = 0.6f) else Palette.onAccent,
                )
                if (aqui && nomeAtual != null) {
                    ByteFigure(Modifier.width(30.dp), mood = if (semNada) Mood.SAD else if (fim) Mood.HAPPY else Mood.THINK, animate = !ThemeState.reduceMotion)
                    Text(
                        nomeAtual, Modifier.padding(start = 4.dp).clip(RoundedCornerShape(6.dp)).background(Palette.hazard).padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(Palette.hazard),
                    )
                }
            }
            for (l in linhas.filter { it.sala == salaId && it.texto != s.cabeca && it.texto != "}" }) {
                val doUso = l.uso != null && f.usos.any { it.id == usoAtual && it.linha == l.texto && it.sala == salaId }
                val achada = l.carta != null && fim && q?.achou == l.carta
                val fundo = when {
                    achada -> Palette.green
                    doUso -> Palette.hazard
                    l.carta != null -> Palette.white.copy(alpha = 0.12f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
                Text(
                    l.texto, Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(fundo).padding(horizontal = 4.dp, vertical = 1.dp),
                    fontFamily = mono, fontSize = 14.sp,
                    fontWeight = if (l.carta != null) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        achada -> Palette.on(Palette.green)
                        doUso -> Palette.on(Palette.hazard)
                        l.carta != null -> Palette.yellow
                        else -> Palette.onAccent
                    },
                )
            }
            for (filha in f.filhas(salaId)) bloco(filha.id)
            if (s.cabeca.isNotEmpty()) Text("}", fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.onAccent)
        }
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(tx("carteiro_programa"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        for (s in f.filhas(null)) bloco(s.id)
    }
}
