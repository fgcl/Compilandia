package com.compilandia.ui.bancada

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.RoboMips
import com.compilandia.textos.tx
import com.compilandia.ui.ChipRow
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood

/** O estado de uma fase do Robô MIPS (J2). Fica fora da tela para os testes montarem e rodarem. */
class RoboMipsEstado(val fase: RoboMips.Fase) : EstadoBancada(fase.pedido) {
    val programa = mutableStateListOf<String>()
    var quadros by mutableStateOf(RoboMips.roda(fase, emptyList(), fase.testes.first()).quadros.take(1))

    init { linha.carrega(1, tocar = false) }

    fun poe(carta: String) {
        if (programa.size >= RoboMips.LINHAS_MAX) return
        programa += carta
        mexeu()
    }

    fun tira(i: Int) { programa.removeAt(i); mexeu() }

    fun sobe(i: Int) {
        if (i <= 0) return
        val t = programa[i - 1]
        programa[i - 1] = programa[i]
        programa[i] = t
        mexeu()
    }

    fun limpa() { programa.clear(); mexeu(); frase = fase.pedido }

    /** Roda todos os testes; o palco mostra o primeiro que falhou, ou o primeiro de todos. */
    fun rodar(tocar: Boolean = true): com.compilandia.engine.Veredito {
        val v = RoboMips.confere(fase, programa.toList())
        aplica(v)
        if (v.falha != -1) mostra(v.falha ?: 0, tocar)
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        quadros = RoboMips.roda(fase, programa.toList(), fase.testes[i]).quadros
        linha.carrega(quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = 0
    }

    fun pedeDica() = dica(fase.dicas) {
        val p = RoboMips.proximaLinha(fase, programa.toList())
        if (p != null) tx("robomips_dica_linha", p.first + 1, p.second) else tx("robomips_dica_linha_sobra", fase.solucao.size)
    }
}

private val COR_MEMORIA = Color(0xFF7A5A2A)
private val COR_REGISTRADOR = Color(0xFF2A5A55)
private val COR_CAIXOTE = Color(0xFFC98B3A)
private val COR_PRATELEIRA = Color(0xFF6B5A3A)

@Composable
fun RoboMipsJogo(e: RoboMipsEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val q = e.quadros.getOrElse(e.linha.atual) { e.quadros.last() }
    Arranjo(
        palco = {
            PalcoMips(e, q)
            ControlesPalco(e.linha, q.nota)
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("robomips_cartas"), style = MaterialTheme.typography.titleSmall)
            ChipRow {
                for (c in e.fase.cartas) Carta(c) { e.poe(c) }
            }
            Text(tx("robomips_programa"), style = MaterialTheme.typography.titleSmall)
            Programa(e, q.linha)
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

@Composable
private fun Carta(texto: String, onClick: () -> Unit) {
    val rotulo = RoboMips.eRotulo(texto)
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.codeBg)
            .border(2.dp, if (rotulo) Palette.teal else Palette.hazard, RoundedCornerShape(8.dp))
            .clickable { com.compilandia.platform.Sound.play(com.compilandia.platform.Sfx.CLICK); onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(texto, color = if (rotulo) Palette.teal else Palette.onAccent, fontFamily = LocalFonts.current.mono, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/** O programa montado, com a carta que o Byte está executando acesa. */
@Composable
private fun Programa(e: RoboMipsEstado, acesa: Int) {
    val mono = LocalFonts.current.mono
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (e.programa.isEmpty()) Text(tx("robomips_toque_cartas"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(6.dp))
        e.programa.forEachIndexed { i, linha ->
            val viva = i == acesa
            val rotulo = RoboMips.eRotulo(linha)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (viva) Palette.hazard.copy(alpha = 0.28f) else Color.Transparent).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${i + 1}", fontFamily = mono, fontSize = 12.sp, color = Palette.onAccent.copy(alpha = 0.5f), modifier = Modifier.width(22.dp))
                Text(
                    if (rotulo) linha else "  $linha", fontFamily = mono, fontSize = 14.sp, modifier = Modifier.weight(1f),
                    color = when { viva -> Palette.yellow; rotulo -> Palette.teal; else -> Palette.onAccent },
                    fontWeight = if (viva) FontWeight.Bold else FontWeight.Normal,
                )
                if (i > 0) MiniBotao("↑", tx("robomips_subir")) { e.sobe(i) }
                MiniBotao("×", tx("robomips_tirar")) { e.tira(i) }
            }
        }
    }
}

@Composable
private fun MiniBotao(t: String, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).clickable { onClick() }.semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) { Text(t, color = Palette.onAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

/**
 * O palco: a prateleira da memória em cima, os registradores embaixo e o somador à direita.
 * O Byte anda até a caixa do quadro e leva o caixote com o valor.
 */
@Composable
private fun PalcoMips(e: RoboMipsEstado, q: RoboMips.Quadro) {
    val f = e.fase
    val mono = LocalFonts.current.mono
    BoxWithConstraints(Modifier.fillMaxWidth().height(270.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg)) {
        val w = maxWidth
        fun xCaixa(i: Int): Dp = w * (0.13f + i * 0.21f)
        val xSomador = w * 0.83f
        val rotulo = Palette.onAccent.copy(alpha = 0.7f)
        Text(tx("robomips_memoria"), color = rotulo, style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(10.dp, 4.dp))
        Box(Modifier.offset(6.dp, 76.dp).width(w * 0.64f).height(5.dp).clip(RoundedCornerShape(2.dp)).background(COR_PRATELEIRA))
        f.variaveis.forEachIndexed { i, v -> Caixa(v, q.mem[v] ?: 0, xCaixa(i), 22.dp, COR_MEMORIA, q.acende == v, nomeEmBaixo = true) }
        Text(tx("robomips_registradores"), color = rotulo, style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(w * 0.66f, 204.dp))
        RoboMips.REGISTRADORES.forEachIndexed { i, r -> Caixa(r, q.regs[r] ?: 0, xCaixa(i), 186.dp, COR_REGISTRADOR, q.acende == r, nomeEmBaixo = true) }
        Box(
            Modifier.offset(xSomador - 48.dp, 22.dp).size(96.dp, 52.dp).clip(RoundedCornerShape(12.dp))
                .border(3.dp, if (q.somador != null) Palette.yellow else Palette.orange, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(q.somador ?: tx("robomips_somador"), color = if (q.somador != null) Palette.yellow else Palette.orange, fontFamily = mono, fontSize = 13.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
        val (ax, ay) = when (q.lugar) {
            RoboMips.Lugar.MEMORIA -> xCaixa(f.variaveis.indexOf(q.nome).coerceAtLeast(0)) to 100.dp
            RoboMips.Lugar.REGISTRADOR -> xCaixa(RoboMips.REGISTRADORES.indexOf(q.nome).coerceAtLeast(0)) to 126.dp
            RoboMips.Lugar.SOMADOR -> xSomador to 100.dp
            RoboMips.Lugar.PORTA -> xSomador to 130.dp
        }
        val dur = if (ThemeState.reduceMotion) 0 else (e.linha.passoMs() * 0.6).toInt()
        val bx by animateDpAsState(ax - 24.dp, tween(dur), label = "byteX")
        val by by animateDpAsState(ay, tween(dur), label = "byteY")
        Box(Modifier.offset(bx, by).width(48.dp)) {
            ByteFigure(Modifier.width(48.dp), mood = if (e.humor == Mood.SAD && e.linha.atual == e.linha.total - 1) Mood.SAD else Mood.NEUTRAL, animate = !ThemeState.reduceMotion)
            q.carrega?.let {
                Box(
                    Modifier.offset(7.dp, (-14).dp).size(34.dp, 22.dp).clip(RoundedCornerShape(4.dp)).background(COR_CAIXOTE),
                    contentAlignment = Alignment.Center,
                ) { Text("$it", fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)) }
            }
        }
    }
}

@Composable
private fun Caixa(nome: String, valor: Long, x: Dp, y: Dp, cor: Color, acesa: Boolean, nomeEmBaixo: Boolean) {
    val mono = LocalFonts.current.mono
    Column(Modifier.offset(x - 28.dp, y).width(56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(cor)
                .border(if (acesa) 3.dp else 0.dp, if (acesa) Palette.yellow else cor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("$valor", color = Color.White, fontFamily = mono, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        if (nomeEmBaixo) Text(nome, color = Palette.onAccent.copy(alpha = 0.8f), fontFamily = mono, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
