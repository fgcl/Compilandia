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
import com.compilandia.textos.tx

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
    Mono(tx("comum_afd_1", title), bold = true)
    AutomatonView(d.stateCount, d.start, d.accepting, d.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { r.stateName(it) })
    RichText(tx("gamesv3_toque_nas_celulas_para_marcar"), style = MaterialTheme.typography.titleSmall)
    HintBox(
        ctx,
        tx("gamesv3_rodada_1_todo_par_com"),
        tx("gamesv3_depois_p_q_distinguivel_se"),
        tx("gamesv3_transicoes") + d.alphabet.joinToString("  ") { c -> "'$c': " + (0 until d.stateCount).joinToString(" ") { "${r.stateName(it)}→${r.stateName(d.delta.getValue(it to c))}" } },
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
    if (!checked) BigButton(tx("gamesv3_conferir_tabela"), Modifier.fillMaxWidth(), color = Palette.teal) { checked = true }
    else {
        val total = d.stateCount * (d.stateCount - 1) / 2
        val wrong = (0 until d.stateCount).sumOf { i -> (0 until i).count { j -> (marks[j to i] == true) != ((j to i) in truth) } }
        val score = (total - wrong).toDouble() / total
        FeedbackBanner(wrong == 0, if (wrong == 0) tx("gamesv3_tabela_perfeita") else tx("gamesv3_1_celula_erradas_em_vermelho", wrong))
        RichText(tx("gamesv3_classes_equivalentes") + r.classes.joinToString("  ") { cl -> cl.joinToString(",", "{", "}") { r.stateName(it) } } +
            tx("gamesv3_afd_minimo_com_1_estados", r.minimized.stateCount), style = MaterialTheme.typography.bodyMedium)
        val m = r.minimized
        AutomatonView(m.stateCount, m.start, m.accepting, m.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { "m$it" })
        BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish(score) }
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
        res ?: ExprParser.parse(tx("comum_b_c"))
    }
    val order = remember { preorder(e) }
    var k by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<String?>(null) }
    val src = remember { e.render().removeSurrounding("(", ")") }
    Mono(tx("gamesv3_codigo_1", src), bold = true, size = 16)
    if (ctx.hints > 0) AstTools.concreteTree(src)?.let { TreeView(it.toUi(AstTools.concreteGrammar)) }
    HintBox(ctx, tx("gamesv3_arvore_derivacao_acima_ast_ela"), tx("gamesv3_raiz_operador_que_feito_por"))
    fun javaWith(n: Expr): String {
        val idx = order.indexOf(n)
        if (idx >= k) return "▢"
        return when (n.kind) {
            Expr.Kind.NUM -> tx("comum_new_num_1", n.text)
            Expr.Kind.VAR -> "new Var(\"${n.text}\")"
            Expr.Kind.BINOP -> "new BinOp('${n.text}', ${javaWith(n.children[0])}, ${javaWith(n.children[1])})"
        }
    }
    CodeBlock(tx("gamesv3_expr_arvore") + javaWith(e) + ";", tx("gamesv3_parser_monta_isto"))
    if (k >= order.size) {
        FeedbackBanner(true, tx("gamesv3_ast_completa_1_nos_contra", AstTools.countNodes(e), AstTools.concreteTree(src)?.let { AstTools.countNodes(it) } ?: "?"))
        TreeView(e.toUi())
        BigButton(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val target = order[k]
    val options = remember(k) {
        val pool = (order.map { AstTools.label(it) } + listOf("BinOp('+')", "BinOp('*')", "BinOp('-')", "Var(a)", "Num(2)")).distinct()
        (listOf(AstTools.label(target)) + pool.filter { it != AstTools.label(target) }.shuffled(ctx.rnd).take(3)).shuffled(ctx.rnd)
    }
    RichText(tx("gamesv3_qual_no_vai_no_proximo"), style = MaterialTheme.typography.titleSmall)
    msg?.let { FeedbackBanner(false, it) }
    ChipRow {
        for (o in options) Chip(o, mono = true, color = Palette.purple) {
            if (o == AstTools.label(target)) { k++; msg = null; Juice.correct() }
            else { mistakes++; msg = tx("gamesv3_nao_pense_neste_ponto_expressao"); Juice.wrong() }
        }
    }
}

private fun preorder(e: Expr): List<Expr> = listOf(e) + e.children.flatMap { preorder(it) }

// ============================================================ Coloração de Registradores

@Composable
fun RegColoringGame(ctx: GameCtx) {
    val src = remember {
        val pool = when (ctx.level) {
            1 -> listOf(tx("comum_b_c_d"), tx("comum_b_c_d_2_2"))
            2 -> listOf(tx("gamesv3_b_c_d"), tx("gamesv3_b_c_d_2"))
            else -> listOf(tx("gamesv3_b_c_d_c"), tx("gamesv3_b_c_d_b_d"), tx("gamesv3_b_c_d_b"))
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

    Mono(tx("comum_r_1", src), bold = true)
    if (phase == 0) {
        RichText(tx("gamesv3_passo_1_vivacidade_marque_quais"), style = MaterialTheme.typography.titleSmall)
        HintBox(ctx, tx("gamesv3_trabalhe_baixo_para_cima_depois"), tx("gamesv3_in_use_out_def_temporario"))
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row { Cell(tx("gamesv3_instrucao"), header = true, w = 130); for (t in temps) Cell(t, header = true, w = 44) }
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
        BigButton(tx("gamesv3_conferir_vivacidade"), Modifier.fillMaxWidth(), color = Palette.teal) {
            val cells = code.indices.flatMap { i -> temps.map { i to it } }
            val ok = cells.count { (i, t) -> (live[i to t] == true) == (t in lv.liveOut[i]) }
            liveScore = ok.toDouble() / cells.size
            if (ok == cells.size) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else if (phase == 1 || phase == 2) {
        if (phase == 1) FeedbackBanner(liveScore == 1.0, tx("gamesv3_vivacidade_1_correto", (liveScore * 100).toInt()) + code.indices.joinToString("; ") { "${it + 1}: " + lv.liveOut[it].sorted().joinToString(",", "{", "}") })
        RichText(tx("gamesv3_passo_2_coloracao_use_so", k), style = MaterialTheme.typography.titleSmall)
        GraphView(temps, g, fill = { v -> colors[v]?.let { regColors[it % regColors.size] } ?: Palette.white }, sub = { v -> colors[v]?.let { "\$s$it" } }, selected = sel) { v -> if (phase == 1) sel = v }
        HintBox(ctx, tx("gamesv3_comece_pelo_no_com_mais"))
        if (phase == 1) {
            ChipRow { for (c in 0 until k) Chip("\$s$c", color = Palette.purple) { sel?.let { colors[it] = c; Sound.play(Sfx.CLICK) } } }
            BigButton(tx("gamesv3_alocar"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = temps.all { it in colors }) {
                val clashes = g.entries.sumOf { (v, nbs) -> nbs.count { colors[it] == colors[v] } } / 2
                colorMistakes = clashes
                if (clashes == 0) Juice.levelUp() else Juice.wrong()
                phase = 2
            }
        } else {
            val valid = colorMistakes == 0
            FeedbackBanner(valid, if (valid) tx("gamesv3_alocacao_valida_com_1_registrador", k) else tx("gamesv3_1_conflito_temporarios_vivos_ao", colorMistakes))
            val alloc = RegAlloc.color(g, k)
            val before = Mips.fromTac(code); val after = RegAlloc.mips(code, alloc)
            RichText(tx("gamesv3_acessos_memoria_1_tudo_na", RegAlloc.memoryAccesses(before), RegAlloc.memoryAccesses(after)), style = MaterialTheme.typography.bodyMedium)
            CodeBlock(after.joinToString("\n"), tx("gamesv3_mips_com_alocacao"))
            BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish(0.5 * liveScore + 0.5 * (if (valid) 1.0 else 0.3)) }
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

    CodeBlock(src, tx("comum_programa_4_b_7"))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        lines.forEachIndexed { i, l ->
            val isBlank = i in blanks
            val shown = when {
                !isBlank -> l.text
                answers[i] != null -> answers[i]!!
                i == blanks.getOrNull(current) -> tx("gamesv3_complete")
                else -> "▢▢▢"
            }
            val indent = if (l.text.endsWith(":") || l.text.startsWith(".")) "" else "    "
            Text(indent + shown, fontFamily = LocalFonts.current.mono, fontSize = 12.sp,
                color = if (isBlank) Palette.yellow else Color(0xFFE8E6F0), fontWeight = if (isBlank) FontWeight.Bold else FontWeight.Normal)
        }
    }
    if (current >= blanks.size) {
        FeedbackBanner(mistakes == 0, tx("gamesv3_desvios_completos_agora_rode_no"))
        MipsRunView(MiniLang.asmText(lines), highlightBranches = true)
        BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
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
    RichText(tx("gamesv3_qual_instrucao_vai_no_marcado"), style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, tx("gamesv3_no_if_while_desvio_pula"), tx("gamesv3_no_fim_corpo_while_volte"))
    msg?.let { FeedbackBanner(it.first, it.second) }
    for (o in options) BigButton(o, Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) {
        if (o == right) { answers[bi] = o; current++; msg = true to lines[bi].why; Juice.correct() }
        else { mistakes++; msg = false to tx("gamesv3_nao_1", lines[bi].why); Juice.wrong() }
    }
}

