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
import com.compilandia.textos.tx

fun nfaEdges(n: SmallNFA) = n.edges.map { Triple(it.first, it.second?.toString() ?: "ε", it.third) }

fun dfaLetter(i: Int) = ('A' + i).toString()

// ------------------------------------------------------------------ AFN → AFD

@Composable
fun NfaSubsetView(id: String) {
    val nfa = remember(id) { SmallNFA.bank.first { it.id == id } }
    val res = remember(nfa) { nfa.subsetConstruction() }
    Mono(tx("comum_afn_1", nfa.title), bold = true)
    Stepper(res.steps.size + 1) { i ->
        val cur = res.steps.getOrNull(i - 1)
        AutomatonView(nfa.stateCount, nfa.start, nfa.accepting, nfaEdges(nfa), highlight = cur?.from ?: nfa.epsClosure(setOf(nfa.start)))
        if (cur == null) {
            RichText(tx("visuals2_estado_inicial_afd_fecho_1", nfa.start, setText(res.states[0])), style = MaterialTheme.typography.bodyMedium)
        } else {
            val from = dfaLetter(res.states.indexOf(cur.from))
            val toIdx = res.states.indexOf(cur.to)
            RichText(
                tx("visuals2_1_2_com_3_move", from, setText(cur.from), cur.symbol, setText(cur.moved), setText(cur.to)) +
                    if (cur.to.isEmpty()) tx("visuals2_estado_morto_sem_transicao") else tx("visuals2_estado_1", dfaLetter(toIdx)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        // tabela do AFD construída até aqui
        val shown = res.steps.take(i)
        val known = res.states.filter { st -> st == res.states[0] || shown.any { it.to == st } }
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row { Cell(tx("comum_afd"), header = true, w = 56); Cell(tx("comum_kt_conjunto"), header = true, w = 96); for (c in nfa.alphabet) Cell("$c", header = true, w = 56) }
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
        Text(tx("visuals2_estado_final_contem_final_afn"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        if (i == res.steps.size) {
            Text(tx("visuals2_afd_resultante"), style = MaterialTheme.typography.titleSmall)
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
    OutlinedTextField(text, { text = it }, label = { Text(tx("visuals2_entrada_para_yylex")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    ChipRow {
        for (lx in r.tokens) Column(
            Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.teal.copy(alpha = 0.12f)).border(1.dp, Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Mono(lx.token, bold = true, color = Palette.purple)
            Mono("\"${lx.text}\"", size = 12)
            Text(tx("visuals2_regra_1", lx.rule + 1), fontSize = 10.sp, color = Palette.inkSoft)
        }
    }
    r.error?.let { Text(it, color = Palette.red) }
    Text(tx("solta_visuals2_1"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
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
    if (errPos != null) Text(tx("visuals2_erro_detectado_no_token_1", errPos + 1, toks[errPos]), style = MaterialTheme.typography.bodySmall, color = Palette.red)
    Stepper(tr.steps.size) { i ->
        val s = tr.steps[i]
        StackAndInput(g, s.stack, s.input)
        val a = s.action
        Text(if (a is LLAction.Error) a.message else a.label(), style = MaterialTheme.typography.bodyMedium, color = if (a is LLAction.Error) Palette.red else Palette.ink)
    }
    Text(tx("visuals2_recuperacoes_feitas"), style = MaterialTheme.typography.titleSmall)
    for (e in tr.events) RichText("• " + e.message, style = MaterialTheme.typography.bodySmall)
    Text(if (tr.reachedEnd) tx("visuals2_parser_continuou_ate_fim_pode") else tx("visuals2_parser_desistiu"), style = MaterialTheme.typography.bodySmall, color = Palette.teal)
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
        Text(tx("visuals2_topo_pilha"), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
        frames.asReversed().forEachIndexed { k, f ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(if (k == 0 && highlightTop) Palette.yellow.copy(alpha = 0.5f) else Palette.paperDark)
                    .border(2.dp, Palette.ink, RoundedCornerShape(8.dp)).padding(8.dp)
            ) {
                Mono("${f.fn}(${f.args.entries.joinToString { "${it.key}=${it.value}" }})", bold = true)
                Text(tx("visuals2_retorna_para_1_vinculo_controle", f.returnTo, frames.getOrNull(frames.size - 2 - k)?.fn ?: "—"), fontSize = 11.sp, color = Palette.inkSoft)
            }
        }
        if (frames.isEmpty()) Text(tx("visuals2_vazia"), color = Palette.inkSoft)
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
                is RtEvent.Call -> tx("visuals2_chama_1_2_empilha_registro", e.frame.fn, e.frame.args.values.joinToString())
                is RtEvent.Return -> tx("visuals2_1_retorna_2_desempilha_registro", e.fn, e.value)
            }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
        )
        FrameStack(e.stack)
        if (i == p.events.lastIndex) Text(tx("visuals2_resultado_final_1_profundidade_maxima", p.result, p.events.maxOf { it.stack.size }), color = Palette.teal)
    }
}

// ------------------------------------------------------------------ MIPS

@Composable
fun MipsView(src: String) {
    val e = remember(src) { ExprParser.parse(src) }
    val tac = remember(e) { TacGen.generate(e, "r") }
    val asm = remember(tac) { Mips.fromTac(tac) }
    val states = remember(asm) { Mips.run(asm, Mips.intEnv) }
    Mono(tx("visuals2_r_1_4_b_2", e.render()), bold = true)
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
            Text(tx("comum_text_main"), fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Color(0xFF9CA3AF))
            asm.forEachIndexed { k, line ->
                Text("  $line", fontFamily = LocalFonts.current.mono, fontSize = 13.sp,
                    color = if (k == i) Palette.yellow else if (k < i) Color(0xFFE8E6F0) else Color(0xFF6B7280),
                    fontWeight = if (k == i) FontWeight.Bold else FontWeight.Normal)
            }
        }
        Text(tx("comum_registradores"), style = MaterialTheme.typography.labelMedium)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            for (r in listOf("\$t0", "\$t1", "\$t2", "\$t3")) Cell("$r\n${st.regs[r] ?: "—"}", w = 64)
        }
        Text(tx("visuals2_memoria_data"), style = MaterialTheme.typography.labelMedium)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            for (v in vars) Cell("$v\n${st.mem[v] ?: "?"}", w = 56, color = if (asm[i].endsWith(", $v") && asm[i].startsWith("sw")) Palette.yellow.copy(alpha = 0.6f) else null)
        }
        if (i == asm.lastIndex) Text(tx("visuals2_r_1_isso_que_mars", st.mem["r"]), color = Palette.teal, fontWeight = FontWeight.Bold)
    }
}

// ------------------------------------------------------------------ Descida recursiva

@Composable
fun DescentCodeView(g: Grammar, input: String) {
    val t = remember(g) { LL1Table(g) }
    val code = remember(t) { RecursiveDescentGen.program(t) }
    GrammarText(g)
    CodeBlock(code.joinToString("\n") { "    ".repeat(it.indent) + it.text }, tx("visuals2_parser_java_metodo_por_nao"))
    if (input.isNotBlank()) {
        val calls = remember(t, input) { RecursiveDescentGen.callTrace(t, tokensOf(input)) }
        Mono(tx("comum_entrada_1", input), bold = true)
        Stepper(calls.size) { i ->
            val (stack, what) = calls[i]
            Text(what, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(tx("visuals2_pilha_chamadas_pilha_escondida_java"), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                stack.forEachIndexed { k, f ->
                    Box(Modifier.padding(end = 3.dp).clip(RoundedCornerShape(6.dp)).background(if (k == stack.lastIndex) Palette.yellow else Palette.paperDark)
                        .border(1.dp, Palette.ink, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp)) { Mono("$f()", bold = true, size = 13) }
                }
                if (stack.isEmpty()) Text(tx("visuals2_vazia"), color = Palette.inkSoft)
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
        tx("visuals2_comparacao_slr_1_tem_1", slr.rawConflictCells.size, lalr.rawConflictCells.size) +
            tx("visuals2_mesmos_1_estados_lr_0", lalr.automaton.states.size),
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
    if (tree == null) { Text(tx("comum_frase_nao_pertence_linguagem"), color = Palette.red); return }
    val forms = remember(tree) { rightmostForms(g, tree) }
    Stepper(forms.size) { i ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (k in 0..i) Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (k == 0) "   " else "⇒ ", fontFamily = LocalFonts.current.mono)
                SymbolsText(g, forms[k].first, size = if (k == i) 17 else 14)
            }
            if (i > 0) Text(tx("visuals2_expandiu_nao_terminal_mais_direita", forms[i].second), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            if (i == forms.lastIndex) {
                PaperCard(color = Palette.yellow.copy(alpha = 0.15f)) {
                    RichText(tx("visuals2_leia_baixo_para_cima_cada"), style = MaterialTheme.typography.bodySmall)
                }
                TreeView(tree.toUi(g))
            }
        }
    }
}

@Suppress("unused") private fun keepW() = Modifier.width(1.dp)
