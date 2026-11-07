package com.compilandia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.DFA
import com.compilandia.engine.EPS
import com.compilandia.engine.Expr
import com.compilandia.engine.Grammar
import com.compilandia.engine.ParseNode
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Nó genérico de árvore para desenho. */
data class UiNode(
    val label: String,
    val children: List<UiNode> = emptyList(),
    val color: Color = Palette.white,
    val textColor: Color = Palette.ink,
    val sub: String? = null,     // texto pequeno abaixo (atributos)
    val key: Int = -1,           // identificador para toques
)

fun ParseNode.toUi(g: Grammar?, highlight: Set<ParseNode> = emptySet()): UiNode {
    val isNt = g?.isNonTerminal(symbol) ?: children.isNotEmpty()
    val color = when {
        this in highlight -> Palette.yellow
        symbol == EPS -> Palette.paperDark
        isNt -> Palette.nodeNonTerminal
        else -> Palette.nodeLeaf
    }
    return UiNode(symbol, children.map { it.toUi(g, highlight) }, color, if (isNt) Palette.nodeTextNonTerminal else Palette.nodeTextTerminal)
}

fun Expr.toUi(sub: (Expr) -> String? = { null }, color: (Expr) -> Color = { if (it.isLeaf) Palette.nodeLeaf else Palette.nodeNonTerminal }): UiNode =
    UiNode(text, children.map { it.toUi(sub, color) }, color(this), Palette.inkOnLight, sub(this), id)

private class Placed(val node: UiNode, val x: Float, val y: Float)

/** Desenha uma árvore (sintática ou AST). Toque num nó chama [onTap] com a `key` do nó. */
@Composable
fun TreeView(root: UiNode, modifier: Modifier = Modifier, onTap: ((Int) -> Unit)? = null) {
    val tm = rememberTextMeasurer()
    val density = LocalDensity.current
    val hGap = 58f; val vGap = 72f; val r = 22f
    val placed = remember(root) {
        val out = mutableListOf<Placed>()
        val edges = mutableListOf<Pair<Int, Int>>()
        var leafX = 0f
        fun layout(n: UiNode, depth: Int): Int {
            val idx: Int
            if (n.children.isEmpty()) {
                idx = out.size; out += Placed(n, leafX, depth * vGap); leafX += hGap
            } else {
                val kids = n.children.map { layout(it, depth + 1) }
                val x = (out[kids.first()].x + out[kids.last()].x) / 2
                idx = out.size; out += Placed(n, x, depth * vGap)
                kids.forEach { edges += idx to it }
            }
            return idx
        }
        layout(root, 0)
        out to edges
    }
    val (nodes, edges) = placed
    val widthPx = (nodes.maxOf { it.x } + 2 * hGap)
    val heightPx = (nodes.maxOf { it.y } + vGap + 20f)
    val scale = with(density) { 1.dp.toPx() }
    Box(modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Canvas(
            Modifier.width((widthPx).dp).height((heightPx).dp).then(
                if (onTap != null) Modifier.pointerInput(root) {
                    detectTapGestures { p ->
                        val hit = nodes.minByOrNull { val dx = (it.x + hGap) * scale - p.x; val dy = (it.y + 30f) * scale - p.y; dx * dx + dy * dy }
                        if (hit != null) {
                            val dx = (hit.x + hGap) * scale - p.x; val dy = (hit.y + 30f) * scale - p.y
                            if (sqrt(dx * dx + dy * dy) < 40f * scale) onTap(hit.node.key)
                        }
                    }
                } else Modifier
            )
        ) {
            val s = scale
            fun pos(p: Placed) = Offset((p.x + hGap) * s, (p.y + 30f) * s)
            for ((a, b) in edges) drawLine(Palette.ink.copy(alpha = 0.6f), pos(nodes[a]), pos(nodes[b]), strokeWidth = 2.5f * s)
            for (p in nodes) {
                val c = pos(p)
                val label = p.node.label
                val w = max(2 * r, label.length * 9f + 14f) * s
                val h = 2 * r * s * 0.9f
                drawRoundRect(p.node.color, Offset(c.x - w / 2, c.y - h / 2), Size(w, h), CornerRadius(h / 2))
                drawRoundRect(Palette.ink, Offset(c.x - w / 2, c.y - h / 2), Size(w, h), CornerRadius(h / 2), style = Stroke(2f * s))
                centeredText(tm, label, c, TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.node.textColor))
                p.node.sub?.let { centeredText(tm, it, Offset(c.x, c.y + h / 2 + 10f * s), TextStyle(fontSize = 11.sp, color = Palette.orange, fontWeight = FontWeight.Bold)) }
            }
        }
    }
}

fun DrawScope.centeredText(tm: TextMeasurer, text: String, center: Offset, style: TextStyle) {
    val layout = tm.measure(text, style)
    drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}

/** Desenha um AFD. [current] = estado destacado; [lastEdge] = transição recém-usada. */
@Composable
fun DfaView(
    dfa: DFA,
    modifier: Modifier = Modifier,
    current: Int? = null,
    lastEdge: Pair<Int, Int>? = null,
    traveler: Triple<Int, Int, Float>? = null,
    onTapState: ((Int) -> Unit)? = null,
) {
    val edges = remember(dfa) { dfa.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) } }
    AutomatonView(dfa.stateCount, dfa.start, dfa.accepting, edges, modifier, setOfNotNull(current), lastEdge, onTapState = onTapState, traveler = traveler)
}

/** Desenha um autômato genérico (AFD ou AFN; rótulo "ε" para transições vazias). */
@Composable
fun AutomatonView(
    n: Int,
    start: Int,
    accepting: Set<Int>,
    edges: List<Triple<Int, String, Int>>,
    modifier: Modifier = Modifier,
    highlight: Set<Int> = emptySet(),
    lastEdge: Pair<Int, Int>? = null,
    stateLabel: (Int) -> String = { "q$it" },
    traveler: Triple<Int, Int, Float>? = null,   // (de, para, progresso 0..1): desenha o trenzinho
    onTapState: ((Int) -> Unit)? = null,
) {
    val tm = rememberTextMeasurer()
    val sizeDp = if (n <= 3) 240 else if (n <= 5) 290 else 330
    Box(modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(
            Modifier.size(sizeDp.dp).then(
                if (onTapState != null) Modifier.pointerInput(n, edges) {
                    detectTapGestures { p ->
                        val ps = statePositions(n, size.width.toFloat(), size.height.toFloat())
                        val hit = ps.indices.minByOrNull { val d = ps[it] - p; d.x * d.x + d.y * d.y }
                        if (hit != null) {
                            val d = ps[hit] - p
                            if (sqrt(d.x * d.x + d.y * d.y) < size.width * 0.12f) onTapState(hit)
                        }
                    }
                } else Modifier
            )
        ) {
            val ps = statePositions(n, size.width, size.height)
            val r = size.minDimension * 0.075f
            val labels = LinkedHashMap<Pair<Int, Int>, MutableList<String>>()
            for ((a, c, b) in edges) labels.getOrPut(a to b) { mutableListOf() } += c
            for ((pair, cs) in labels) {
                val (a, b) = pair
                val hl = lastEdge == pair
                val col = if (hl) Palette.orange else if (cs.all { it == "ε" }) Palette.purple.copy(alpha = 0.8f) else Palette.ink.copy(alpha = 0.75f)
                val text = cs.sorted().joinToString(",")
                if (a == b) {
                    val c = ps[a]
                    // o estado inicial tem a seta de início à esquerda: o laço dele vai para cima
                    val out = if (a == start) Offset(0f, -1f) else (c - Offset(size.width / 2, size.height / 2)).let { v -> val l = max(1f, sqrt(v.x * v.x + v.y * v.y)); if (l < 2f) Offset(0f, -1f) else Offset(v.x / l, v.y / l) }
                    val lc = c + out * (r * 1.6f)
                    drawCircle(col, r * 0.75f, lc, style = Stroke(if (hl) 5f else 3f))
                    val estilo = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = col)
                    // Rótulo comprido ("0-9,a-z") ao lado do laço: afasta pela metade da largura que passa do laço.
                    val sobra = max(0f, tm.measure(text, estilo).size.width / 2f - r * 0.6f) * kotlin.math.abs(out.x)
                    centeredText(tm, text, c + out * (r * 2.9f + sobra), estilo)
                } else {
                    val bidir = labels.containsKey(b to a)
                    curvedArrow(ps[a], ps[b], r, if (bidir) 0.25f else 0.08f, col, if (hl) 5f else 3f)?.let { mid ->
                        centeredText(tm, text, mid, TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = col, background = Palette.paper))
                    }
                }
            }
            val s0 = ps[start]
            drawLine(Palette.ink, s0 - Offset(r * 2.2f, 0f), s0 - Offset(r, 0f), strokeWidth = 4f)
            for (i in 0 until n) {
                val c = ps[i]
                drawCircle(if (i in highlight) Palette.yellow else Palette.white, r, c)
                drawCircle(Palette.ink, r, c, style = Stroke(4f))
                if (i in accepting) drawCircle(Palette.ink, r * 0.78f, c, style = Stroke(3f))
                centeredText(tm, stateLabel(i), c, TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (i in highlight) Palette.inkOnLight else Palette.ink))
            }
            if (traveler != null && traveler.first in ps.indices && traveler.second in ps.indices) {
                val (a, b, t) = traveler
                val pa = ps[a]; val pb = ps[b]
                val pos = if (a == b) pa + Offset(0f, -r * 1.6f) + Offset((kotlin.math.sin(t * 6.28f) * r * 0.7f), -(kotlin.math.cos(t * 6.28f) * r * 0.7f))
                          else pa + (pb - pa) * t + Offset(0f, -r * 1.1f)
                centeredText(tm, "🚂", pos, TextStyle(fontSize = 26.sp))
            }
        }
    }
}

private fun statePositions(n: Int, w: Float, h: Float): List<Offset> {
    val cx = w / 2; val cy = h / 2
    val rad = min(w, h) * 0.34f
    if (n == 1) return listOf(Offset(cx, cy))
    if (n == 2) return listOf(Offset(w * 0.25f, cy), Offset(w * 0.75f, cy))
    return List(n) { i ->
        val ang = PI + 2 * PI * i / n
        Offset(cx + rad * cos(ang).toFloat(), cy + rad * sin(ang).toFloat())
    }
}

/** Seta curva de a até b; devolve o ponto médio (para o rótulo). */
private fun DrawScope.curvedArrow(a: Offset, b: Offset, r: Float, bend: Float, color: Color, width: Float): Offset? {
    val d = b - a
    val len = sqrt(d.x * d.x + d.y * d.y)
    if (len < 1f) return null
    val nrm = Offset(-d.y / len, d.x / len)
    val ctrl = (a + b) / 2f + nrm * (len * bend)
    // pontos na borda dos círculos
    fun toward(from: Offset, to: Offset, dist: Float): Offset { val v = to - from; val l = sqrt(v.x * v.x + v.y * v.y); return from + v * (dist / l) }
    val start = toward(a, ctrl, r)
    val end = toward(b, ctrl, r)
    val path = Path().apply { moveTo(start.x, start.y); quadraticTo(ctrl.x, ctrl.y, end.x, end.y) }
    drawPath(path, color, style = Stroke(width))
    val ang = atan2((end - ctrl).y, (end - ctrl).x)
    val ah = r * 0.55f
    val p1 = end - Offset(cos(ang - 0.45f) * ah, sin(ang - 0.45f) * ah)
    val p2 = end - Offset(cos(ang + 0.45f) * ah, sin(ang + 0.45f) * ah)
    drawPath(Path().apply { moveTo(end.x, end.y); lineTo(p1.x, p1.y); lineTo(p2.x, p2.y); close() }, color)
    return start * 0.25f + ctrl * 0.5f + end * 0.25f
}

/** Grafo não direcionado (ex.: grafo de interferência). [colors]: cor de preenchimento por nó. */
@Composable
fun GraphView(
    nodes: List<String>,
    edges: Map<String, Set<String>>,
    modifier: Modifier = Modifier,
    fill: (String) -> Color = { Palette.white },
    sub: (String) -> String? = { null },
    selected: String? = null,
    onTap: ((String) -> Unit)? = null,
) {
    val tm = rememberTextMeasurer()
    val n = nodes.size
    Box(modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(
            Modifier.size(280.dp).then(
                if (onTap != null) Modifier.pointerInput(nodes) {
                    detectTapGestures { p ->
                        val ps = statePositions(n, size.width.toFloat(), size.height.toFloat())
                        val hit = ps.indices.minByOrNull { val d = ps[it] - p; d.x * d.x + d.y * d.y }
                        if (hit != null) { val d = ps[hit] - p; if (sqrt(d.x * d.x + d.y * d.y) < size.width * 0.13f) onTap(nodes[hit]) }
                    }
                } else Modifier
            )
        ) {
            if (n == 0) return@Canvas
            val ps = statePositions(n, size.width, size.height)
            val r = size.minDimension * 0.085f
            for ((a, nbs) in edges) for (b in nbs) {
                val i = nodes.indexOf(a); val j = nodes.indexOf(b)
                if (i in 0 until j) drawLine(Palette.ink.copy(alpha = 0.6f), ps[i], ps[j], strokeWidth = 4f)
            }
            nodes.forEachIndexed { i, v ->
                drawCircle(fill(v), r, ps[i])
                drawCircle(if (v == selected) Palette.orange else Palette.ink, r, ps[i], style = Stroke(if (v == selected) 8f else 4f))
                centeredText(tm, v, ps[i], TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (fill(v) == Palette.white) Palette.ink else Palette.inkOnLight))
                sub(v)?.let { centeredText(tm, it, ps[i] + Offset(0f, r * 1.5f), TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.purple)) }
            }
        }
    }
}

/** Cores dos registradores na coloração. */
val regColors = listOf(Color(0xFF93C5FD), Color(0xFFFCA5A5), Color(0xFF86EFAC), Color(0xFFFDE68A), Color(0xFFD8B4FE), Color(0xFFFDBA74))
