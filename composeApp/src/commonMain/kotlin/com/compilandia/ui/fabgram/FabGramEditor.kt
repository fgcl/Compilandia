package com.compilandia.ui.fabgram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.rememberTextMeasurer
import com.compilandia.tutor.Corrida
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.Laudo
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sign

/** O que o dedo faz no palco. */
internal enum class Ferramenta { MOVER, ESTEIRA, APAGAR }

/**
 * Onde a grade fica na tela: [cols] × [rows] células de [cel] px; a célula ([x0], [y0]) começa em ([ox], [oy]).
 * As coordenadas da planta podem ser negativas: a grade cobre a tela toda.
 */
internal data class Grade(val cols: Int, val rows: Int, val cel: Float, val ox: Float, val oy: Float, val x0: Int, val y0: Int) {
    fun celDe(o: Offset) = Cel(floor((o.x - ox) / cel).toInt() + x0, floor((o.y - oy) / cel).toInt() + y0)
    fun dentro(c: Cel) = c.x in x0 until x0 + cols && c.y in y0 until y0 + rows
    val area: FabGram.Area get() = FabGram.Area(x0, y0, x0 + cols, y0 + rows)
}

/**
 * A fábrica fica no meio da área livre (a tela menos [topo] e [baixo], onde ficam a frase e a barra),
 * com uma célula de folga, e a grade se estende até as bordas da tela.
 */
internal fun gradePara(pl: Planta, w: Float, h: Float, topo: Float, baixo: Float, celMax: Float): Grade {
    val livre = (h - topo - baixo).coerceAtLeast(h * 0.5f)
    val cel = minOf(w / (pl.largura + 2), livre / (pl.altura + 2), celMax)
    val pcx = (pl.x0 + pl.x1) / 2f
    val pcy = (pl.y0 + pl.y1) / 2f
    val scx = w / 2f
    val scy = topo + livre / 2f
    val x0 = floor(pcx - scx / cel).toInt()
    val y0 = floor(pcy - scy / cel).toInt()
    return Grade(ceil(w / cel).toInt() + 2, ceil(h / cel).toInt() + 2, cel, scx + (x0 - pcx) * cel, scy + (y0 - pcy) * cel, x0, y0)
}

/**
 * O palco que se edita. Enquanto o dedo arrasta, a mudança fica num rascunho;
 * ao soltar, [onPlanta] recebe a planta nova. Um toque sem arrastar em cima de um bloco chama [onToque].
 */
@Composable
internal fun PalcoEditavel(
    planta: Planta, ferramenta: Ferramenta, laudo: Laudo?, corrida: Corrida?, t: Float, relogio: Float,
    sel: Int?, reduz: Boolean, nomes: NomesBlocos, onPlanta: (Planta) -> Unit, onToque: (FabGram.Bloco?) -> Unit,
) {
    val medidor = rememberTextMeasurer()
    var rascunho by remember { mutableStateOf<Planta?>(null) }
    var fantasma by remember { mutableStateOf<Pair<Int, Cel>?>(null) }
    val atual by rememberUpdatedState(planta)
    val aoMudar by rememberUpdatedState(onPlanta)
    val aoTocar by rememberUpdatedState(onToque)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val topo = with(LocalDensity.current) { 64.dp.toPx() }
        val baixo = with(LocalDensity.current) { 72.dp.toPx() }
        val celMax = with(LocalDensity.current) { 88.dp.toPx() }
        val gr = remember(planta.x0, planta.y0, planta.x1, planta.y1, w, h) { gradePara(planta, w, h, topo, baixo, celMax) }
        Canvas(
            Modifier.fillMaxSize().clipToBounds().testTag("fabgram-palco").pointerInput(ferramenta, gr) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val base = atual
                    val c0 = gr.celDe(down.position)
                    var ult = c0
                    var moveu = false
                    var r = base
                    val bloco = base.blocoEm(c0)
                    drag(down.id) { ch ->
                        val c = gr.celDe(ch.position)
                        ch.consume()
                        if (c == ult || !gr.dentro(c)) return@drag
                        moveu = true
                        when (ferramenta) {
                            Ferramenta.MOVER -> if (bloco != null) fantasma = bloco.id to Cel(bloco.x + c.x - c0.x, bloco.y + c.y - c0.y)
                            Ferramenta.ESTEIRA -> { r = desenha(r, ult, c); rascunho = r }
                            Ferramenta.APAGAR -> { r = r.semEsteira(c); rascunho = r }
                        }
                        ult = c
                    }
                    val alvo = fantasma
                    fantasma = null
                    rascunho = null
                    when {
                        ferramenta == Ferramenta.MOVER && alvo != null -> base.move(alvo.first, alvo.second.x, alvo.second.y, gr.area)?.let(aoMudar)
                        moveu && ferramenta != Ferramenta.MOVER -> aoMudar(r)
                        !moveu && bloco != null -> aoTocar(bloco)
                        !moveu && ferramenta == Ferramenta.ESTEIRA && gr.dentro(c0) ->
                            aoMudar(if (c0 in base.esteiras) base.gira(c0) else base.comEsteira(c0, Dir.DIREITA) ?: base)
                        !moveu && ferramenta == Ferramenta.APAGAR -> aoMudar(base.semEsteira(c0))
                        !moveu -> aoTocar(null)
                    }
                }
            },
        ) {
            val pl = rascunho ?: planta
            desenhaFabrica(Cena(pl, gr, laudo, corrida, t, relogio, sel, reduz, ferramenta, fantasma, medidor, nomes))
        }
    }
}

/**
 * Estende a esteira de [de] até [ate], um passo de cada vez (primeiro na horizontal).
 * Cada passo aponta a célula de trás para a nova; assim a curva sai sozinha.
 * Começar dentro de um bloco faz a primeira célula ser a saída dele.
 */
internal fun desenha(pl: Planta, de: Cel, ate: Cel): Planta {
    var r = pl
    var c = de
    while (c != ate) {
        val dx = (ate.x - c.x).sign
        val d = if (dx != 0) Dir.de(dx, 0)!! else Dir.de(0, (ate.y - c.y).sign)!!
        r = r.comEsteira(c, d) ?: r
        c += d
        r = r.comEsteira(c, d) ?: r
    }
    return r
}
