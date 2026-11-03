package com.compilandia.ui.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EPS
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.Mips
import com.compilandia.engine.PanicMode
import com.compilandia.engine.ParseNode
import com.compilandia.engine.RecursiveDescentGen
import com.compilandia.engine.RtEvent
import com.compilandia.engine.RtProgram
import com.compilandia.engine.RuntimeSim
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.TacGen
import com.compilandia.engine.setText
import com.compilandia.engine.tokensOf
import com.compilandia.engine.LLAction
import com.compilandia.ui.AutomatonView
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.ChipRow
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.SymbolsText
import com.compilandia.ui.TreeView
import com.compilandia.ui.toUi

fun nfaEdges(n: SmallNFA) = n.edges.map { Triple(it.first, it.second?.toString() ?: "ε", it.third) }

fun dfaLetter(i: Int) = ('A' + i).toString()

// ------------------------------------------------------------------ AFN → AFD

@Composable
fun NfaSubsetView(id: String) {
    val nfa = remember(id) { SmallNFA.bank.first { it.id == id } }
    val res = remember(nfa) { nfa.subsetConstruction() }
    Mono("AFN: ${nfa.title}", bold = true)
    Stepper(res.steps.size + 1) { i ->
        val cur = res.steps.getOrNull(i - 1)
        AutomatonView(nfa.stateCount, nfa.start, nfa.accepting, nfaEdges(nfa), highlight = cur?.from ?: nfa.epsClosure(setOf(nfa.start)))
        if (cur == null) {
            RichText("Estado inicial do AFD: **A = ε-fecho({${nfa.start}}) = ${setText(res.states[0])}** (amarelo).", style = MaterialTheme.typography.bodyMedium)
        } else {
            val from = dfaLetter(res.states.indexOf(cur.from))
            val toIdx = res.states.indexOf(cur.to)
            RichText(
                "De **$from = ${setText(cur.from)}** com '**${cur.symbol}**': move = ${setText(cur.moved)}, ε-fecho = **${setText(cur.to)}**" +
                    if (cur.to.isEmpty()) " → estado morto (sem transição)." else " → estado **${dfaLetter(toIdx)}**.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        // tabela do AFD construída até aqui
        val shown = res.steps.take(i)
        val known = res.states.filter { st -> st == res.states[0] || shown.any { it.to == st } }
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row { Cell("AFD", header = true, w = 56); Cell("conjunto", header = true, w = 96); for (c in nfa.alphabet) Cell("$c", header = true, w = 56) }
            for (st in known) Row {
                val idx = res.states.indexOf(st)
                Cell((if (idx in res.accepting) "*" else "") + dfaLetter(idx), header = true, w = 56)
                Cell(setText(st), w = 96)
                for (c in nfa.alphabet) {
                    val step = shown.firstOrNull { it.from == st && it.symbol == c }
                    Cell(step?.let { if (it.to.isEmpty()) "∅" else dfaLetter(res.states.indexOf(it.to)) } ?: "", w = 56,
                        color = if (step == cur && cur != null) Palette.yellow.copy(alpha = 0.6f) else null)
                }
            }
        }
        Text("* = estado final (contém um final do AFN)", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        if (i == res.steps.size) {
            Text("AFD resultante:", style = MaterialTheme.typography.titleSmall)
            val dfa = res.toDFA(nfa.alphabet)
            AutomatonView(dfa.stateCount, 0, dfa.accepting, dfa.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { dfaLetter(it) })
        }
    }
}

// ------------------------------------------------------------------ Flex

@Composable
fun FlexView(spec: String, input: String) {
    val flex = remember(spec) { MiniFlex.parseSpec(spec) }
    var text by remember(spec) { mutableStateOf(input.ifBlank { "if x1 >= 42" }) }
    val r = remember(flex, text) { flex.scan(text) }
    CodeBlock(spec, "scanner.l")
    OutlinedTextField(text, { text = it }, label = { Text("entrada para o yylex()") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    ChipRow {
        for (lx in r.tokens) Column(
            Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.teal.copy(alpha = 0.12f)).border(1.dp, Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Mono(lx.token, bold = true, color = Palette.purple)
            Mono("\"${lx.text}\"", size = 12)
            Text("regra ${lx.rule + 1}", fontSize = 10.sp, color = Palette.inkSoft)
        }
    }
    r.error?.let { Text("❌ $it", color = Palette.red) }
    Text("Regras: o MAIOR lexema vence; empate → a regra que vem primeiro.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
}

// ------------------------------------------------------------------ Modo pânico

@Composable
fun PanicView(g: Grammar, input: String) {
    val t = remember(g) { LL1Table(g) }
    val toks = remember(input) { tokensOf(input) }
    val errPos = remember(g, input) { PanicMode.firstErrorPosition(t, toks) }
    val tr = remember(g, input) { PanicMode.trace(t, toks) }
    GrammarText(g)
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        toks.forEachIndexed { k, s ->
            Box(
                Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp)).background(if (k == errPos) Palette.red.copy(alpha = 0.3f) else Palette.white)
                    .border(1.dp, Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp)
            ) { Mono(s, bold = k == errPos) }
        }
    }
    if (errPos != null) Text("O erro é DETECTADO no token ${errPos + 1} ('${toks[errPos]}'): é o primeiro que não pode continuar nenhuma frase válida.", style = MaterialTheme.typography.bodySmall, color = Palette.red)
    Stepper(tr.steps.size) { i ->
        val s = tr.steps[i]
        StackAndInput(g, s.stack, s.input)
        val a = s.action
        Text(if (a is LLAction.Error) "🚨 ${a.message}" else a.label(), style = MaterialTheme.typography.bodyMedium, color = if (a is LLAction.Error) Palette.red else Palette.ink)
    }
    Text("Recuperações feitas:", style = MaterialTheme.typography.titleSmall)
    for (e in tr.events) RichText("• " + e.message, style = MaterialTheme.typography.bodySmall)
    Text(if (tr.reachedEnd) "✅ O parser continuou até o fim e pôde relatar todos os erros de uma vez." else "O parser desistiu.", style = MaterialTheme.typography.bodySmall, color = Palette.teal)
}

// ------------------------------------------------------------------ Pilha de chamadas

fun runtimeProgram(id: String): RtProgram = when (id) {
    "fat4" -> RuntimeSim.fatorial(4)
    "fib3" -> RuntimeSim.fib(3)
    "chain1" -> RuntimeSim.chain(1)
    "chain2" -> RuntimeSim.chain(2)
    else -> RuntimeSim.fatorial(3)
}

@Composable
fun FrameStack(frames: List<com.compilandia.engine.Frame>, highlightTop: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("⬆ topo da pilha", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
        frames.asReversed().forEachIndexed { k, f ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(if (k == 0 && highlightTop) Palette.yellow.copy(alpha = 0.5f) else Palette.paperDark)
                    .border(2.dp, Palette.ink, RoundedCornerShape(8.dp)).padding(8.dp)
            ) {
                Mono("${f.fn}(${f.args.entries.joinToString { "${it.key}=${it.value}" }})", bold = true)
                Text("retorna para: ${f.returnTo} · vínculo de controle → ${frames.getOrNull(frames.size - 2 - k)?.fn ?: "—"}", fontSize = 11.sp, color = Palette.inkSoft)
            }
        }
        if (frames.isEmpty()) Text("(vazia)", color = Palette.inkSoft)
    }
}

@Composable
fun CallStackView(id: String) {
    val p = remember(id) { runtimeProgram(id) }
    CodeBlock(p.code, p.title)
    Stepper(p.events.size) { i ->
        val e = p.events[i]
        Text(
            when (e) {
                is RtEvent.Call -> "📥 chama ${e.frame.fn}(${e.frame.args.values.joinToString()}) → EMPILHA um registro de ativação"
                is RtEvent.Return -> "📤 ${e.fn} retorna ${e.value} → DESEMPILHA o registro"
            }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
        )
        FrameStack(e.stack)
        if (i == p.events.lastIndex) Text("Resultado final: ${p.result}. Profundidade máxima: ${p.events.maxOf { it.stack.size }} registros.", color = Palette.teal)
    }
}

// ------------------------------------------------------------------ MIPS

@Composable
fun MipsView(src: String) {
    val e = remember(src) { ExprParser.parse(src) }
    val tac = remember(e) { TacGen.generate(e, "r") }
    val asm = remember(tac) { Mips.fromTac(tac) }
    val states = remember(asm) { Mips.run(asm, Mips.intEnv) }
    Mono("r = ${e.render()}    (a=4, b=2, c=3, d=5)", bold = true)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text("TAC", style = MaterialTheme.typography.labelMedium)
            for (t in tac) Mono(t.toString(), size = 12)
        }
    }
    Stepper(asm.size) { i ->
        val st = states[i]
        val vars = (tac.map { it.target } + listOf("a", "b", "c", "d")).distinct()
        Column(Modifier.clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp).fillMaxWidth()) {
            Text(".text\nmain:", fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Color(0xFF9CA3AF))
            asm.forEachIndexed { k, line ->
                Text("  $line", fontFamily = LocalFonts.current.mono, fontSize = 13.sp,
                    color = if (k == i) Palette.yellow else if (k < i) Color(0xFFE8E6F0) else Color(0xFF6B7280),
                    fontWeight = if (k == i) FontWeight.Bold else FontWeight.Normal)
            }
        }
        Text("Registradores", style = MaterialTheme.typography.labelMedium)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            for (r in listOf("\$t0", "\$t1", "\$t2", "\$t3")) Cell("$r\n${st.regs[r] ?: "—"}", w = 64)
        }
        Text("Memória (.data)", style = MaterialTheme.typography.labelMedium)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            for (v in vars) Cell("$v\n${st.mem[v] ?: "?"}", w = 56, color = if (asm[i].endsWith(", $v") && asm[i].startsWith("sw")) Palette.yellow.copy(alpha = 0.6f) else null)
        }
        if (i == asm.lastIndex) Text("✅ r = ${st.mem["r"]} — é isso que o MARS mostraria na memória.", color = Palette.teal, fontWeight = FontWeight.Bold)
    }
}

// ------------------------------------------------------------------ Descida recursiva

@Composable
fun DescentCodeView(g: Grammar, input: String) {
    val t = remember(g) { LL1Table(g) }
    val code = remember(t) { RecursiveDescentGen.program(t) }
    GrammarText(g)
    CodeBlock(code.joinToString("\n") { "    ".repeat(it.indent) + it.text }, "Parser.java (um método por não-terminal)")
    if (input.isNotBlank()) {
        val calls = remember(t, input) { RecursiveDescentGen.callTrace(t, tokensOf(input)) }
        Mono("entrada: $input", bold = true)
        Stepper(calls.size) { i ->
            val (stack, what) = calls[i]
            Text(what, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Pilha de chamadas (a pilha \"escondida\" do Java):", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                stack.forEachIndexed { k, f ->
                    Box(Modifier.padding(end = 3.dp).clip(RoundedCornerShape(6.dp)).background(if (k == stack.lastIndex) Palette.yellow else Palette.paperDark)
                        .border(1.dp, Palette.ink, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp)) { Mono("$f()", bold = true, size = 13) }
                }
                if (stack.isEmpty()) Text("(vazia)", color = Palette.inkSoft)
            }
        }
    }
}

// ------------------------------------------------------------------ LALR e derivação à direita

@Composable
fun LalrTableView(g: Grammar) {
    val lalr = remember(g) { LRTable(g, LRKind.LALR1) }
    val slr = remember(g) { LRTable(g, LRKind.SLR1) }
    LRTableView(lalr)
    Text(
        "Comparação: SLR(1) tem ${slr.rawConflictCells.size} célula(s) em conflito; LALR(1) tem ${lalr.rawConflictCells.size}. " +
            "Mesmos ${lalr.automaton.states.size} estados do LR(0), mas lookaheads mais precisos que o FOLLOW.",
        style = MaterialTheme.typography.bodySmall,
    )
}

/** Derivação mais à direita, reconstruída a partir da árvore. */
fun rightmostForms(g: Grammar, tree: ParseNode): List<Pair<List<String>, String>> {
    // forma sentencial = lista de nós (folhas da fronteira atual)
    var frontier = listOf(tree)
    val out = mutableListOf(frontier.map { it.symbol } to "")
    while (true) {
        val idx = frontier.indexOfLast { g.isNonTerminal(it.symbol) && it.children.isNotEmpty() }
        if (idx < 0) break
        val n = frontier[idx]
        val kids = n.children.filter { it.symbol != EPS }
        frontier = frontier.subList(0, idx) + kids + frontier.subList(idx + 1, frontier.size)
        out += frontier.map { it.symbol } to "${n.symbol} → ${if (kids.isEmpty()) EPS else kids.joinToString(" ") { it.symbol }}"
    }
    return out
}

@Composable
fun RightmostView(g: Grammar, input: String) {
    val tree = remember(g, input) { DerivationHelper.leftmostDerivations(g, tokensOf(input), 1).firstOrNull()?.let { DerivationHelper.treeOf(g, it) } }
    GrammarText(g)
    if (tree == null) { Text("A frase não pertence à linguagem.", color = Palette.red); return }
    val forms = remember(tree) { rightmostForms(g, tree) }
    Stepper(forms.size) { i ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (k in 0..i) Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (k == 0) "   " else "⇒ ", fontFamily = LocalFonts.current.mono)
                SymbolsText(g, forms[k].first, size = if (k == i) 17 else 14)
            }
            if (i > 0) Text("expandiu o não-terminal MAIS À DIREITA: ${forms[i].second}", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            if (i == forms.lastIndex) {
                PaperCard(color = Palette.yellow.copy(alpha = 0.15f)) {
                    RichText("Leia de baixo para cima: cada passo ao contrário é uma **redução** do analisador shift-reduce. LR = Left-to-right, **Rightmost** derivation (ao contrário).", style = MaterialTheme.typography.bodySmall)
                }
                TreeView(tree.toUi(g))
            }
        }
    }
}

@Suppress("unused") private fun keepW() = Modifier.width(1.dp)
