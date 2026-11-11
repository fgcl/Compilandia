package com.compilandia.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.DFA
import com.compilandia.engine.DfaCheck
import com.compilandia.ui.BigButton
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.cast.BugFigure
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import com.compilandia.textos.tx

/** Um desafio: a frase em português e a regex que confere. */
private data class Encomenda(val pedido: String, val regex: String)

private val encomendas = listOf(
    listOf(Encomenda(tx("gamefactory_aceite_seguido_quantos_b_quiser"), "ab*"), Encomenda(tx("gamefactory_aceite_so_quem_comeca_com"), "a(a|b)*")),
    listOf(Encomenda(tx("gamefactory_aceite_repeticoes_ab"), "(ab)*"), Encomenda(tx("gamefactory_aceite_quem_termina_em_b"), "(a|b)*b")),
    listOf(Encomenda(tx("gamefactory_aceite_so_quem_termina_em"), "(a|b)*ab"), Encomenda(tx("gamefactory_aceite_numero_par"), "(aa)*")),
    listOf(Encomenda(tx("gamefactory_aceite_quem_tem_ab_em"), "(a|b)*ab(a|b)*"), Encomenda(tx("gamefactory_aceite_antes_b"), "a*b*")),
    listOf(Encomenda(tx("gamefactory_aceite_quem_termina_em_abb"), "(a|b)*abb"), Encomenda(tx("gamefactory_aceite_quem_tem_no_penultimo"), "(a|b)*a(a|b)")),
)

/**
 * Fábrica de Autômatos: a pessoa monta o autômato com o dedo e robôs testam.
 * Errou, aparece a menor frase que furou: contraexemplo ensina mais que "está errado".
 * Quanto menos peças, mais pontos.
 */
@Composable
fun AutomatonFactoryGame(ctx: GameCtx) {
    val nivel = (ctx.level - 1).coerceIn(0, encomendas.lastIndex)
    Rounds(ctx, 2) { round, done ->
        val e = remember { encomendas[nivel][(round + ctx.rnd.nextInt(2)) % 2] }
        Bancada(e, ctx, done)
    }
}

@Composable
private fun Bancada(e: Encomenda, ctx: GameCtx, done: (Double) -> Unit) {
    val alvo = remember(e) { DFA.fromRegex(e.regex) }
    val alfabeto = remember(e) { alvo.alphabet }
    val minimo = remember(e) { DfaCheck.pecasMinimas(alvo) }
    val pos = remember { mutableStateListOf(Offset(0.16f, 0.5f)) }
    val finais = remember { mutableStateListOf<Int>() }
    val setas = remember { mutableStateMapOf<Pair<Int, Char>, Int>() }
    var sel by remember { mutableStateOf<Int?>(0) }
    var de by remember { mutableStateOf<Int?>(null) }
    var furo by remember { mutableStateOf<String?>(null) }
    var furoAceitou by remember { mutableStateOf(false) }
    var tentativas by remember { mutableIntStateOf(0) }
    var ok by remember { mutableStateOf(false) }
    val tm = rememberTextMeasurer()
    val mono = LocalFonts.current.mono
    val cAro = Palette.ink
    val cSel = Palette.hazard
    val cFio = Palette.ink.copy(alpha = 0.75f)
    val cDe = Palette.teal
    val cFundo = Palette.white
    val raioU = 0.095f

    fun montado() = DFA(pos.size, 0, finais.toSet(), alfabeto, setas.toMap())

    Text(e.pedido, style = MaterialTheme.typography.titleMedium)
    Text(tx("gamefactory_toque_num_circulo_depois_em"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Box(Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg)) {
        Canvas(
            Modifier.fillMaxWidth().height(290.dp)
                .pointerInput(Unit) {
                    fun acha(o: Offset): Int? {
                        val r = size.width * raioU * 1.3f
                        return pos.indices.minByOrNull { hypot(pos[it].x * size.width - o.x, pos[it].y * size.height - o.y) }
                            ?.takeIf { hypot(pos[it].x * size.width - o.x, pos[it].y * size.height - o.y) < r }
                    }
                    detectTapGestures { o ->
                        val q = acha(o)
                        if (q == null) { sel = null; de = null; return@detectTapGestures }
                        val origem = de
                        if (origem != null) { sel = q } else { sel = q; de = q }
                        if (origem != null) { /* a letra vem pelos botões */ }
                    }
                }
                .pointerInput(Unit) {
                    var q: Int? = null
                    detectDragGestures(
                        onDragStart = { o -> q = pos.indices.firstOrNull { hypot(pos[it].x * size.width - o.x, pos[it].y * size.height - o.y) < size.width * raioU * 1.3f } },
                        onDrag = { c, d ->
                            c.consume()
                            q?.let { pos[it] = Offset(((pos[it].x * size.width + d.x) / size.width).coerceIn(0.1f, 0.9f), ((pos[it].y * size.height + d.y) / size.height).coerceIn(0.14f, 0.86f)) }
                        },
                    )
                },
        ) {
            val r = size.width * raioU * 0.8f
            fun c(i: Int) = Offset(pos[i].x * size.width, pos[i].y * size.height)
            // seta de início
            val c0 = c(0)
            drawLine(cSel, c0 - Offset(r + 20.dp.toPx(), 0f), c0 - Offset(r + 3.dp.toPx(), 0f), 3.dp.toPx())
            val grupos = setas.entries.groupBy({ it.key.first to it.value }, { it.key.second })
            for ((par, letras) in grupos) {
                val (a, b) = par
                if (a !in pos.indices || b !in pos.indices) continue
                val txt = letras.sorted().joinToString(",")
                val rotulo = tm.measure(txt, TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = cSel))
                if (a == b) {
                    val t = c(a) - Offset(0f, r)
                    drawPath(Path().apply { moveTo(t.x - 9.dp.toPx(), t.y); cubicTo(t.x - 30.dp.toPx(), t.y - 38.dp.toPx(), t.x + 30.dp.toPx(), t.y - 38.dp.toPx(), t.x + 9.dp.toPx(), t.y) }, cFio, style = Stroke(2.5f * density))
                    drawText(rotulo, topLeft = t - Offset(rotulo.size.width / 2f, 44.dp.toPx()))
                } else {
                    val volta = (b to a) in grupos
                    val ca = c(a)
                    val cb = c(b)
                    val ang = atan2(cb.y - ca.y, cb.x - ca.x)
                    val n = Offset(-sin(ang), cos(ang)) * (if (volta) 12.dp.toPx() else 0f)
                    val s = ca + Offset(cos(ang), sin(ang)) * r + n
                    val f = cb - Offset(cos(ang), sin(ang)) * r + n
                    drawLine(cFio, s, f, 2.5f * density)
                    val ar = 9.dp.toPx()
                    drawPath(Path().apply {
                        moveTo(f.x, f.y)
                        lineTo(f.x - ar * cos(ang - 0.45f), f.y - ar * sin(ang - 0.45f))
                        lineTo(f.x - ar * cos(ang + 0.45f), f.y - ar * sin(ang + 0.45f))
                        close()
                    }, cFio)
                    val m = (s + f) / 2f + n
                    drawText(rotulo, topLeft = m - Offset(rotulo.size.width / 2f, rotulo.size.height / 2f))
                }
            }
            for (i in pos.indices) {
                val p = c(i)
                drawCircle(cFundo, r, p)
                drawCircle(if (i == sel) cSel else if (i == de) cDe else cAro, r, p, style = Stroke(3.dp.toPx()))
                if (i in finais) drawCircle(cAro, r - 6.dp.toPx(), p, style = Stroke(2.dp.toPx()))
                val nome = tm.measure("q$i", TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = cAro))
                drawText(nome, topLeft = p - Offset(nome.size.width / 2f, nome.size.height / 2f))
            }
        }
    }
    val s = sel
    val d = de
    if (d != null && s != null) {
        Text(tx("gamefactory_letra_seta_q_1_para", d, s), style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (c in alfabeto) BigButton("$c", Modifier.weight(1f), small = true, color = Palette.hazard, textColor = Palette.hazardDark) {
                setas[d to c] = s
                Juice.drop()
                de = null
            }
            BigButton(tx("gamefactory_tirar"), Modifier.weight(1f), small = true, color = Palette.white, textColor = Palette.ink) {
                setas.keys.filter { it.first == d && setas[it] == s }.forEach { setas.remove(it) }
                de = null
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BigButton(tx("gamefactory_novo_estado"), Modifier.weight(1f), small = true, color = Palette.drawer, enabled = pos.size < 7) {
            val i = pos.size
            pos += Offset(0.3f + (i % 3) * 0.26f, if (i % 2 == 0) 0.3f else 0.72f)
            Juice.drop()
        }
        BigButton(if (s != null && s in finais) tx("gamefactory_tirar_duplo") else tx("gamefactory_circulo_duplo"), Modifier.weight(1f), small = true, color = Palette.drawer, enabled = s != null) {
            if (s != null) { if (s in finais) finais.remove(s) else finais += s; Juice.click() }
        }
    }
    HintBox(ctx, tx("gamefactory_cada_estado_guarda_lembranca_que"), tx("gamefactory_pense_no_que_falta_ler"), tx("gamefactory_solucao_minima_1_estado", minimo))
    val f = furo
    if (f != null) Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.red.copy(alpha = 0.16f)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BugFigure(Modifier.height(44.dp))
        Column {
            Text(if (f.isEmpty()) tx("gamefactory_frase_vazia") else f, style = MaterialTheme.typography.titleSmall, fontFamily = mono)
            Text(if (furoAceitou) tx("gamefactory_entrou_nao_devia") else tx("gamefactory_devia_entrar_ficou_fora"), style = MaterialTheme.typography.bodySmall)
        }
    }
    if (ok) Text(tx("gamefactory_passou_nos_robos_com_1", pos.size, minimo), style = MaterialTheme.typography.titleSmall, color = Palette.green)
    if (!ok) BotaoDoJogo(tx("gamefactory_testar_nos_robos"), Modifier.fillMaxWidth()) {
        tentativas++
        val m = montado()
        val c = DfaCheck.diferenca(m, alvo)
        if (c == null) {
            ok = true
            furo = null
            Juice.correct()
        } else {
            furo = c
            furoAceitou = m.accepts(c)
            Juice.wrong()
        }
    } else BotaoDoJogo(tx("comum_continuar"), Modifier.fillMaxWidth()) {
        val folga = (pos.size - minimo).coerceAtLeast(0)
        done((1.0 - 0.15 * folga - 0.1 * (tentativas - 1).coerceAtLeast(0)).coerceIn(0.5, 1.0))
    }
}
