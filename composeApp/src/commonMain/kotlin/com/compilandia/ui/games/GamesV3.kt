package com.compilandia.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.AstTools
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.MiniLang
import com.compilandia.engine.Minimizer
import com.compilandia.engine.Mips
import com.compilandia.engine.MipsVM
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.TacGen
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.AutomatonView
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.GraphView
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.TreeView
import com.compilandia.ui.regColors
import com.compilandia.ui.toUi
import com.compilandia.ui.visuals.Cell
import com.compilandia.ui.visuals.MipsRunView
import com.compilandia.ui.visuals.PairTable

// ============================================================ Tabela dos Pares (minimização)

@Composable
fun MinTableGame(ctx: GameCtx) {
    val idx = remember { if (ctx.level <= 2) ctx.rnd.nextInt(minOf(3, Minimizer.bank.size)) else ctx.rnd.nextInt(Minimizer.bank.size) }
    val (title, dfa) = Minimizer.bank[idx]
    val r = remember { Minimizer.run(dfa) }
    val d = r.dfa
    val truth = remember { r.distinguishable.map { minOf(it.first, it.second) to maxOf(it.first, it.second) }.toSet() }
    val marks = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }
    var checked by remember { mutableStateOf(false) }
    Mono("AFD: $title", bold = true)
    AutomatonView(d.stateCount, d.start, d.accepting, d.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { r.stateName(it) })
    RichText("Toque nas células para marcar **X** nos pares DISTINGUÍVEIS. Os que ficarem em branco são equivalentes e viram um estado só.", style = MaterialTheme.typography.titleSmall)
    HintBox(
        ctx,
        "Rodada 1: todo par com um estado final e outro não-final é distinguível.",
        "Depois: {p,q} é distinguível se, com algum símbolo, eles vão para um par já marcado. Repita até não mudar nada.",
        "Transições: " + d.alphabet.joinToString("  ") { c -> "'$c': " + (0 until d.stateCount).joinToString(" ") { "${r.stateName(it)}→${r.stateName(d.delta.getValue(it to c))}" } },
    )
    PairTable(d.stateCount, r::stateName) { a, b ->
        val key = b to a // (menor, maior)
        val on = marks[key] == true
        val right = key in truth
        Cell(
            if (on) "X" else "", w = 44,
            color = when {
                checked && on == right -> if (on) Palette.green.copy(alpha = 0.3f) else null
                checked -> Palette.red.copy(alpha = 0.35f)
                on -> Palette.orange.copy(alpha = 0.4f)
                else -> null
            },
            onClick = { if (!checked) { marks[key] = !on; Sound.play(Sfx.CLICK) } },
        )
    }
    if (!checked) BigButton("Conferir tabela ✔", Modifier.fillMaxWidth(), color = Palette.teal) { checked = true }
    else {
        val total = d.stateCount * (d.stateCount - 1) / 2
        val wrong = (0 until d.stateCount).sumOf { i -> (0 until i).count { j -> (marks[j to i] == true) != ((j to i) in truth) } }
        val score = (total - wrong).toDouble() / total
        FeedbackBanner(wrong == 0, if (wrong == 0) "Tabela perfeita!" else "$wrong célula(s) erradas (em vermelho).")
        RichText("Classes equivalentes: " + r.classes.joinToString("  ") { cl -> cl.joinToString(",", "{", "}") { r.stateName(it) } } +
            " → AFD mínimo com **${r.minimized.stateCount}** estados.", style = MaterialTheme.typography.bodyMedium)
        val m = r.minimized
        AutomatonView(m.stateCount, m.start, m.accepting, m.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { "m$it" })
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(score) }
    }
}

// ============================================================ Fábrica de AST

@Composable
fun AstFactoryGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done -> AstRound(ctx, done) }
}

@Composable
private fun AstRound(ctx: GameCtx, done: (Double) -> Unit) {
    val e = remember {
        var res: Expr? = null
        repeat(30) {
            if (res == null) {
                val src = ExprGenerator.random(ctx.rnd, (1 + ctx.level / 2).coerceAtMost(3), false)
                val p = try { ExprParser.parse(src) } catch (x: Exception) { null }
                if (p != null && !p.isLeaf && AstTools.concreteTree(src) != null) res = p
            }
        }
        res ?: ExprParser.parse("a + b * c")
    }
    val order = remember { preorder(e) }
    var k by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<String?>(null) }
    val src = remember { e.render().removeSurrounding("(", ")") }
    Mono("código: $src", bold = true, size = 16)
    if (ctx.hints > 0) AstTools.concreteTree(src)?.let { TreeView(it.toUi(AstTools.concreteGrammar)) }
    HintBox(ctx, "Árvore de derivação acima: a AST é ela \"espremida\" — só operadores e operandos.", "A raiz é o operador que é feito POR ÚLTIMO (o de menor precedência, fora dos parênteses).")
    fun javaWith(n: Expr): String {
        val idx = order.indexOf(n)
        if (idx >= k) return "▢"
        return when (n.kind) {
            Expr.Kind.NUM -> "new Num(${n.text})"
            Expr.Kind.VAR -> "new Var(\"${n.text}\")"
            Expr.Kind.BINOP -> "new BinOp('${n.text}', ${javaWith(n.children[0])}, ${javaWith(n.children[1])})"
        }
    }
    CodeBlock("Expr arvore = " + javaWith(e) + ";", "o parser monta isto")
    if (k >= order.size) {
        FeedbackBanner(true, "AST completa! ${AstTools.countNodes(e)} nós, contra ${AstTools.concreteTree(src)?.let { AstTools.countNodes(it) } ?: "?"} na árvore de derivação.")
        TreeView(e.toUi())
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val target = order[k]
    val options = remember(k) {
        val pool = (order.map { AstTools.label(it) } + listOf("BinOp('+')", "BinOp('*')", "BinOp('-')", "Var(a)", "Num(2)")).distinct()
        (listOf(AstTools.label(target)) + pool.filter { it != AstTools.label(target) }.shuffled(ctx.rnd).take(3)).shuffled(ctx.rnd)
    }
    RichText("Qual nó vai no próximo ▢?", style = MaterialTheme.typography.titleSmall)
    msg?.let { FeedbackBanner(false, it) }
    ChipRow {
        for (o in options) Chip(o, mono = true, color = Palette.purple) {
            if (o == AstTools.label(target)) { k++; msg = null; Juice.correct() }
            else { mistakes++; msg = "Não. Pense: neste ponto da expressão, qual operação ou operando vem primeiro (de fora para dentro)?"; Juice.wrong() }
        }
    }
}

private fun preorder(e: Expr): List<Expr> = listOf(e) + e.children.flatMap { preorder(it) }

// ============================================================ Coloração de Registradores

@Composable
fun RegColoringGame(ctx: GameCtx) {
    val src = remember {
        val pool = when (ctx.level) {
            1 -> listOf("a * b + c * d", "( a + b ) * ( c - d )")
            2 -> listOf("a * b + c * d - a", "( a + b ) * ( c + d ) * 2")
            else -> listOf("a * b + c * d + a * c", "( a + b ) * ( c - d ) + b * d", "a * ( b + c ) - ( d - a ) * b")
        }
        pool[ctx.rnd.nextInt(pool.size)]
    }
    val code = remember { TacGen.generate(ExprParser.parse(src), "r") }
    val lv = remember { RegAlloc.liveness(code) }
    val temps = remember { RegAlloc.temps(code) }
    val g = remember { RegAlloc.interference(code) }
    val k = remember { RegAlloc.chromatic(g) }
    var phase by remember { mutableIntStateOf(0) }
    val live = remember { mutableStateMapOf<Pair<Int, String>, Boolean>() }
    var liveScore by remember { mutableStateOf(0.0) }
    val colors = remember { mutableStateMapOf<String, Int>() }
    var sel by remember { mutableStateOf<String?>(null) }
    var colorMistakes by remember { mutableIntStateOf(0) }

    Mono("r = $src", bold = true)
    if (phase == 0) {
        RichText("**Passo 1 — vivacidade:** marque quais temporários estão VIVOS logo DEPOIS de cada instrução (ainda serão usados mais abaixo).", style = MaterialTheme.typography.titleSmall)
        HintBox(ctx, "Trabalhe de BAIXO para CIMA: depois da última instrução nada está vivo.", "in = use ∪ (out − def): um temporário fica vivo de onde é definido até o último uso.")
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row { Cell("instrução", header = true, w = 130); for (t in temps) Cell(t, header = true, w = 44) }
            code.forEachIndexed { i, ins ->
                Row {
                    Cell(ins.toString(), w = 130)
                    for (t in temps) {
                        val on = live[i to t] == true
                        Cell(if (on) "●" else "", w = 44, color = if (on) Palette.teal.copy(alpha = 0.3f) else null, onClick = { live[i to t] = !on })
                    }
                }
            }
        }
        BigButton("Conferir vivacidade ✔", Modifier.fillMaxWidth(), color = Palette.teal) {
            val cells = code.indices.flatMap { i -> temps.map { i to it } }
            val ok = cells.count { (i, t) -> (live[i to t] == true) == (t in lv.liveOut[i]) }
            liveScore = ok.toDouble() / cells.size
            if (ok == cells.size) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else if (phase == 1 || phase == 2) {
        if (phase == 1) FeedbackBanner(liveScore == 1.0, "Vivacidade: ${(liveScore * 100).toInt()}%. Correto: " + code.indices.joinToString("; ") { "${it + 1}: " + lv.liveOut[it].sorted().joinToString(",", "{", "}") })
        RichText("**Passo 2 — coloração:** use só **$k** registrador(es). Toque num temporário e depois num registrador. Vizinhos (vivos juntos) não podem dividir registrador!", style = MaterialTheme.typography.titleSmall)
        GraphView(temps, g, fill = { v -> colors[v]?.let { regColors[it % regColors.size] } ?: Palette.white }, sub = { v -> colors[v]?.let { "\$s$it" } }, selected = sel) { v -> if (phase == 1) sel = v }
        HintBox(ctx, "Comece pelo nó com MAIS vizinhos. Nós sem aresta entre si podem repetir registrador.")
        if (phase == 1) {
            ChipRow { for (c in 0 until k) Chip("\$s$c", color = Palette.purple) { sel?.let { colors[it] = c; Sound.play(Sfx.CLICK) } } }
            BigButton("Alocar ✔", Modifier.fillMaxWidth(), color = Palette.teal, enabled = temps.all { it in colors }) {
                val clashes = g.entries.sumOf { (v, nbs) -> nbs.count { colors[it] == colors[v] } } / 2
                colorMistakes = clashes
                if (clashes == 0) Juice.levelUp() else Juice.wrong()
                phase = 2
            }
        } else {
            val valid = colorMistakes == 0
            FeedbackBanner(valid, if (valid) "Alocação válida com $k registrador(es)!" else "$colorMistakes conflito(s): temporários vivos ao mesmo tempo dividindo registrador.")
            val alloc = RegAlloc.color(g, k)
            val before = Mips.fromTac(code); val after = RegAlloc.mips(code, alloc)
            RichText("Acessos à memória: **${RegAlloc.memoryAccesses(before)}** (tudo na memória) → **${RegAlloc.memoryAccesses(after)}** (com registradores).", style = MaterialTheme.typography.bodyMedium)
            CodeBlock(after.joinToString("\n"), "MIPS com alocação")
            BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(0.5 * liveScore + 0.5 * (if (valid) 1.0 else 0.3)) }
        }
    }
}

// ============================================================ Labirinto de Desvios

@Composable
fun BranchMazeGame(ctx: GameCtx) {
    val src = remember {
        val pool = if (ctx.level <= 2) MiniLang.samples.take(2) else MiniLang.samples
        pool[ctx.rnd.nextInt(pool.size)]
    }
    val lines = remember { MiniLang.compile(src, MiniLang.sampleEnv) }
    val blanks = remember { lines.indices.filter { lines[it].blankable } }
    val answers = remember { mutableStateMapOf<Int, String>() }
    var current by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val labels = remember { lines.map { it.text }.filter { it.endsWith(":") && it != "main:" }.map { it.removeSuffix(":") } }

    CodeBlock(src, "programa (a = 4, b = 7)")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        lines.forEachIndexed { i, l ->
            val isBlank = i in blanks
            val shown = when {
                !isBlank -> l.text
                answers[i] != null -> answers[i]!!
                i == blanks.getOrNull(current) -> "▢▢▢ ← complete"
                else -> "▢▢▢"
            }
            val indent = if (l.text.endsWith(":") || l.text.startsWith(".")) "" else "    "
            Text(indent + shown, fontFamily = LocalFonts.current.mono, fontSize = 12.sp,
                color = if (isBlank) Palette.yellow else Color(0xFFE8E6F0), fontWeight = if (isBlank) FontWeight.Bold else FontWeight.Normal)
        }
    }
    if (current >= blanks.size) {
        FeedbackBanner(mistakes == 0, "Desvios completos! Agora rode no MARS de bolso e veja o resultado:")
        MipsRunView(MiniLang.asmText(lines), highlightBranches = true)
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val bi = blanks[current]
    val right = lines[bi].text
    val options = remember(current) {
        val parts = right.split(" ")
        val opts = mutableSetOf(right)
        if (parts[0] == "j") for (l in labels) opts += "j $l"
        else {
            val rest = right.substringAfter(" ")
            for (op in listOf("blt", "bge", "bgt", "ble", "beq", "bne").shuffled(ctx.rnd)) { if (opts.size >= 4) break; opts += "$op $rest" }
        }
        opts.toList().take(4).shuffled(ctx.rnd)
    }
    RichText("Qual instrução vai no ▢ marcado?", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, "No if/while o desvio pula quando a condição é FALSA: para (a < b) usamos bge (a ≥ b).", "No fim do corpo do while, volte para o rótulo do TESTE; no fim do então, pule o senão.")
    msg?.let { FeedbackBanner(it.first, it.second) }
    for (o in options) BigButton(o, Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) {
        if (o == right) { answers[bi] = o; current++; msg = true to lines[bi].why; Juice.correct() }
        else { mistakes++; msg = false to "Não… ${lines[bi].why}"; Juice.wrong() }
    }
}

