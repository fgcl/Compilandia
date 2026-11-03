package com.compilandia.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.ScopeGen
import com.compilandia.engine.TacGen
import com.compilandia.engine.Type
import com.compilandia.engine.TypeChecker
import com.compilandia.learning.Skill
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.TreeView
import com.compilandia.ui.toUi

private fun randomExpr(ctx: GameCtx, allowBool: Boolean): Expr {
    repeat(20) {
        val src = ExprGenerator.random(ctx.rnd, (1 + ctx.level / 2).coerceAtMost(3), allowBool)
        val e = try { ExprParser.parse(src) } catch (e: Exception) { null }
        if (e != null && !e.isLeaf) return e
    }
    return ExprParser.parse("a + x * 2")
}

// ============================================================ Árvore Anotada

@Composable
fun AttributeTreeGame(ctx: GameCtx) {
    if (ctx.skill == Skill.OPTIMIZATION) { FoldingHunt(ctx); return }
    Rounds(ctx, 2) { _, done -> AttributeRound(ctx, done) }
}

@Composable
private fun AttributeRound(ctx: GameCtx, done: (Double) -> Unit) {
    val e = remember { randomExpr(ctx, allowBool = ctx.level >= 3) }
    val attrs = remember { TypeChecker.evaluate(e, ExprGenerator.env) }
    val all = remember { e.postorder() }
    val filled = remember { mutableStateMapOf<Int, Boolean>().apply { if (ctx.level <= 2) all.filter { it.isLeaf }.forEach { put(it.id, true) } } }
    var selected by remember { mutableStateOf<Expr?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val done0 = all.all { filled[it.id] == true }

    Text("Tabela de símbolos: a, b, c → int · x, y → float · ok → bool", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Mono("expressão: ${e.render()}", bold = true)
    TreeView(
        e.toUi(
            sub = { n -> if (filled[n.id] == true) attrs[n.id]?.let { "${it.type.label}" + (if (it.coerced) " ↑" else "") } else null },
            color = { n -> when { n == selected -> Palette.yellow; filled[n.id] == true -> Palette.nodeDone; else -> Palette.nodeNonTerminal } },
        ),
    ) { key ->
        val n = all.firstOrNull { it.id == key } ?: return@TreeView
        if (filled[n.id] == true) return@TreeView
        if (n.children.any { filled[it.id] != true }) {
            mistakes++; msg = false to "Atributo SINTETIZADO depende dos filhos: calcule os filhos de '${n.text}' primeiro!"; Juice.wrong()
        } else { selected = n; msg = null; Sound.play(Sfx.CLICK) }
    }
    HintBox(ctx, "Folhas: literal com ponto é float; variável → consulte a tabela de símbolos.", "int com float → float (com coerção inttofloat). Comparação → bool. bool em conta aritmética → erro.")
    msg?.let { FeedbackBanner(it.first, it.second) }
    val sel = selected
    if (sel != null) {
        PaperCard(border = Palette.purple) {
            Text("Tipo do nó '${sel.text}'?", style = MaterialTheme.typography.titleSmall)
            ChipRow {
                for (t in Type.entries) Chip(t.label, color = Palette.purple) {
                    val right = attrs.getValue(sel.id)
                    if (t == right.type) {
                        filled[sel.id] = true; selected = null; msg = true to right.rule; Juice.correct()
                    } else { mistakes++; msg = false to "Não é ${t.label}. Pense: ${if (sel.isLeaf) "o que diz a tabela/literal?" else "o que dá ${sel.children.joinToString(" ${sel.text} ") { attrs.getValue(it.id).type.label }}?"}"; Juice.wrong() }
                }
            }
        }
    } else if (!done0) Text("Toque num nó cujos filhos já estão prontos (verdes).", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    if (done0) {
        val root = attrs.getValue(e.id)
        FeedbackBanner(true, "Raiz: tipo ${root.type.label}, valor ${TypeChecker.formatValue(root)}. Você fez uma avaliação em PÓS-ORDEM — é assim que um analisador LR avalia uma SDD S-atribuída.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
    }
}

@Composable
private fun FoldingHunt(ctx: GameCtx) {
    val e = remember {
        val srcs = listOf("a + 2 * 3", "( 4 - 1 ) * b + 2 * 5", "x * ( 2 + 3 ) - 1 * 4", "a + b * ( 6 / 2 )", "( 1 + 2 ) * ( a + 3 * 3 )")
        ExprParser.parse(srcs[ctx.rnd.nextInt(srcs.size)])
    }
    fun constant(n: Expr): Boolean = if (n.isLeaf) n.kind == Expr.Kind.NUM else n.children.all { constant(it) }
    val truth = remember { e.postorder().filter { !it.isLeaf && constant(it) }.map { it.id }.toSet() }
    val picked = remember { mutableStateMapOf<Int, Boolean>() }
    var checked by remember { mutableStateOf(false) }
    Text("⚡ Dobra de constantes: toque nos OPERADORES que o compilador pode calcular já em tempo de compilação.", style = MaterialTheme.typography.titleSmall)
    Mono(e.render(), bold = true)
    TreeView(e.toUi(color = { n ->
        when {
            checked && n.id in truth -> Palette.green.copy(alpha = 0.4f)
            checked && picked[n.id] == true -> Palette.red.copy(alpha = 0.3f)
            picked[n.id] == true -> Palette.yellow
            else -> if (n.isLeaf) Palette.nodeLeaf else Palette.nodeNonTerminal
        }
    })) { key -> if (!checked) picked[key] = picked[key] != true }
    HintBox(ctx, "Um nó é constante se TODAS as folhas abaixo dele são números (nenhuma variável).")
    if (!checked) BigButton("Dobrar! ✔", Modifier.fillMaxWidth(), color = Palette.teal) { checked = true }
    else {
        val mine = picked.filterValues { it }.keys
        val score = if ((mine + truth).isEmpty()) 1.0 else mine.intersect(truth).size.toDouble() / (mine + truth).size
        FeedbackBanner(score == 1.0, "Depois da dobra: ${TacGen.constantFold(e).render()}")
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(score) }
    }
}

// ============================================================ Esconde-Esconde do Escopo

@Composable
fun ScopeDetectiveGame(ctx: GameCtx) {
    val prog = remember { ScopeGen.random(ctx.rnd) }
    val uses = remember { prog.uses.map { it.index } }
    var k by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var answered by remember { mutableStateOf(false) }
    val cur = uses.getOrNull(k)
    val mono = LocalFonts.current.mono
    Text("🔦 A variável destacada em amarelo enxerga QUAL declaração?", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, "Suba a partir do uso: procure a declaração no MESMO bloco (antes do uso); se não achar, vá para o bloco de fora. É uma PILHA de tabelas de símbolos.")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(8.dp)) {
        prog.lines.forEachIndexed { idx, line ->
            val target = cur?.let { prog.lines[it].resolvesTo }
            val bg = when {
                idx == cur -> Palette.yellow.copy(alpha = 0.55f)
                answered && idx == target -> Palette.green.copy(alpha = 0.5f)
                line.declId != null -> Palette.purple.copy(alpha = 0.25f)
                else -> Color.Transparent
            }
            Text(
                "  ".repeat(line.indent) + line.text, fontFamily = mono, fontSize = 15.sp, color = Color(0xFFE8E6F0),
                modifier = Modifier.fillMaxWidth().background(bg).clickable(enabled = cur != null && !answered && line.declId != null) {
                    val right = prog.lines[cur!!].resolvesTo
                    val ok = idx == right
                    if (ok) correct++
                    answered = true
                    msg = ok to if (ok) "Isso! O escopo mais interno que já declarou '${prog.lines[cur].use}'." else
                        if (right == null) "Nenhuma declaração visível: '${prog.lines[cur].use}' NÃO foi declarada aqui!" else "Não: essa declaração não é a mais próxima visível (ou ainda não estava declarada / já saiu de escopo)."
                    if (ok) Juice.correct() else Juice.wrong()
                }.padding(vertical = 3.dp, horizontal = 4.dp),
            )
        }
    }
    if (cur == null) {
        FeedbackBanner(correct == uses.size, "Você resolveu $correct de ${uses.size} usos.")
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(if (uses.isEmpty()) 1.0 else correct.toDouble() / uses.size) }
        return
    }
    if (!answered) BigButton("🚫 Não foi declarada!", Modifier.fillMaxWidth(), color = Palette.red) {
        val ok = prog.lines[cur].resolvesTo == null
        if (ok) correct++
        answered = true
        msg = ok to if (ok) "Exato: erro semântico — variável não declarada." else "Tem sim uma declaração visível (destacada em verde)."
        if (ok) Juice.correct() else Juice.wrong()
    }
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (answered) BigButton("Próximo uso ▶", Modifier.fillMaxWidth()) { k++; answered = false; msg = null }
}

// ============================================================ Linha de Montagem TAC

@Composable
fun TacAssemblyGame(ctx: GameCtx) {
    if (ctx.skill == Skill.OPTIMIZATION && ctx.level >= 2) { FoldingHunt(ctx); return }
    Rounds(ctx, 2) { _, done -> TacRound(ctx, done) }
}

@Composable
private fun TacRound(ctx: GameCtx, done: (Double) -> Unit) {
    val e = remember { randomExpr(ctx, allowBool = false) }
    val code = remember { TacGen.generate(e, "r") }
    val shuffled = remember { code.shuffled(ctx.rnd) }
    val placed = remember { mutableStateListOf<TacGen.Instr>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val dd = com.compilandia.ui.rememberDragDrop()
    fun tryPlace(ins: TacGen.Instr) {
        val defined = placed.map { it.target }.toSet()
        val needs = listOfNotNull(ins.a, ins.b).filter { it.startsWith("t") && it.drop(1).all { c -> c.isDigit() } }
        val missing = needs.filter { it !in defined }
        val isFinal = ins.target == "r"
        when {
            missing.isNotEmpty() -> { mistakes++; msg = false to "Ainda não dá: ${missing.joinToString()} não foi calculado."; Juice.wrong() }
            isFinal && placed.size < code.size - 1 -> { mistakes++; msg = false to "A atribuição final vem por último!"; Juice.wrong() }
            else -> { placed += ins; msg = null; Juice.correct() }
        }
    }
    Mono("r = ${e.render()}", bold = true, size = 16)
    TreeView(e.toUi())
    HintBox(ctx, "Um temporário (t1, t2…) só pode ser usado DEPOIS da instrução que o calcula.", "Comece pelas folhas mais fundas da árvore: elas viram as primeiras instruções.")
    com.compilandia.ui.DropZone(dd, "programa", Modifier.fillMaxWidth()) { hovered ->
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Palette.codeBg)
                .border(if (hovered) 4.dp else 0.dp, Palette.yellow, RoundedCornerShape(14.dp)).padding(12.dp)
        ) {
            Text("programa.tac  ⬅ solte as peças aqui", color = Palette.yellow, style = MaterialTheme.typography.labelMedium)
            placed.forEachIndexed { i, ins -> Mono("${i + 1}: $ins", color = Color(0xFFE8E6F0)) }
            if (placed.size < code.size) Mono("${placed.size + 1}: _", color = Color(0xFF9CA3AF))
        }
    }
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (placed.size < code.size) {
        Text("🔧 Peças da esteira (arraste para o programa):", style = MaterialTheme.typography.labelMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (ins in shuffled) if (ins !in placed) {
                com.compilandia.ui.Draggable(dd, ins, Modifier.fillMaxWidth(), onTap = { tryPlace(ins) }, onDrop = { tryPlace(ins) }) {
                    Box(Modifier.fillMaxWidth()) { com.compilandia.ui.Tile("⠿  $ins", Palette.purple.copy(alpha = 0.18f), big = true) }
                }
            }
        }
    } else {
        FeedbackBanner(true, "Programa montado! Cada instrução tem no máximo um operador — é por isso que se chama código de TRÊS endereços.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
    }
}
