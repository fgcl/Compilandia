package com.compilandia.ui.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.compilandia.content.Beat
import com.compilandia.content.VisualKind
import com.compilandia.engine.DFA
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EOF
import com.compilandia.engine.EPS
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Parser
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LLAction
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniBison
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.ScopeGen
import com.compilandia.engine.SententialForm
import com.compilandia.engine.SetStep
import com.compilandia.engine.TacGen
import com.compilandia.engine.TokenClass
import com.compilandia.engine.TypeChecker
import com.compilandia.engine.render
import com.compilandia.engine.tokensOf
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.DfaView
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.SymbolsText
import com.compilandia.ui.TreeView
import com.compilandia.ui.VSpace
import com.compilandia.ui.toUi
import kotlin.random.Random

/** Mostra um widget interativo. Erros (gramática inválida) viram mensagem amigável. */
@Composable
fun VisualBeat(v: Beat.Visual) {
    PaperCard(color = Palette.white, border = Palette.purple) {
        if (v.caption.isNotBlank()) RichText("🎬 " + v.caption, style = MaterialTheme.typography.titleSmall)
        val error = remember(v) {
            try {
                when (v.kind) {
                    VisualKind.DFA -> DFA.fromRegex(v.input)
                    VisualKind.ATTR_TREE, VisualKind.TAC -> ExprParser.parse(v.input)
                    VisualKind.BISON -> MiniBison.compile(v.grammar)
                    VisualKind.PIPELINE, VisualKind.SCOPE, VisualKind.TOKENIZE, VisualKind.CALL_STACK -> Unit
                    VisualKind.NFA_SUBSET -> com.compilandia.engine.SmallNFA.bank.first { it.id == v.input }
                    VisualKind.FLEX -> com.compilandia.engine.MiniFlex.parseSpec(v.grammar).rules.forEach { it.regex }
                    VisualKind.MINIMIZE -> com.compilandia.engine.Minimizer.bank[v.input.toInt()]
                    VisualKind.AST, VisualKind.REGALLOC -> ExprParser.parse(v.input)
                    VisualKind.MIPS_RUN -> com.compilandia.engine.MipsVM(v.grammar)
                    VisualKind.MINILANG -> com.compilandia.engine.MiniLang.compile(v.input, com.compilandia.engine.MiniLang.sampleEnv)
                    VisualKind.MIPS -> com.compilandia.engine.Mips.run(com.compilandia.engine.Mips.fromTac(TacGen.generate(ExprParser.parse(v.input), "r")), com.compilandia.engine.Mips.intEnv)
                    else -> Grammar.parse(v.grammar)
                }
                null
            } catch (e: Exception) { e.message ?: "erro" }
        }
        if (error != null) { Text("⚠️ $error", color = Palette.red); return@PaperCard }
        when (v.kind) {
            VisualKind.PIPELINE -> PipelineView()
            VisualKind.TOKENIZE -> TokenizeView(v.input)
            VisualKind.DFA -> DfaPlayground(v.input)
            VisualKind.DERIVATION -> DerivationView(Grammar.parse(v.grammar), v.input)
            VisualKind.PARSE_TREE -> ParseTreesView(Grammar.parse(v.grammar), v.input)
            VisualKind.FIRST_FOLLOW -> FirstFollowView(Grammar.parse(v.grammar))
            VisualKind.LL1_TABLE -> LL1TableView(LL1Table(Grammar.parse(v.grammar)))
            VisualKind.LL1_TRACE -> LL1TraceView(Grammar.parse(v.grammar), v.input)
            VisualKind.LR0_AUTOMATON -> LR0View(Grammar.parse(v.grammar))
            VisualKind.SLR_TABLE -> LRTableView(LRTable(Grammar.parse(v.grammar), LRKind.SLR1))
            VisualKind.LR_TRACE -> LRTraceView(Grammar.parse(v.grammar), v.input)
            VisualKind.LEFT_RECURSION -> BeforeAfter(Grammar.parse(v.grammar), GrammarTransform.eliminateImmediateLeftRecursion(Grammar.parse(v.grammar)), "eliminando recursão à esquerda")
            VisualKind.LEFT_FACTORING -> BeforeAfter(Grammar.parse(v.grammar), GrammarTransform.leftFactor(Grammar.parse(v.grammar)), "fatorando à esquerda")
            VisualKind.ATTR_TREE -> AttrTreeView(v.input)
            VisualKind.TAC -> TacView(v.input)
            VisualKind.SCOPE -> ScopeView()
            VisualKind.BISON -> BisonView(v.grammar, v.input)
            VisualKind.NFA_SUBSET -> NfaSubsetView(v.input)
            VisualKind.FLEX -> FlexView(v.grammar, v.input)
            VisualKind.PANIC -> PanicView(Grammar.parse(v.grammar), v.input)
            VisualKind.CALL_STACK -> CallStackView(v.input)
            VisualKind.MIPS -> MipsView(v.input)
            VisualKind.DESCENT_CODE -> DescentCodeView(Grammar.parse(v.grammar), v.input)
            VisualKind.LALR_TABLE -> LalrTableView(Grammar.parse(v.grammar))
            VisualKind.RIGHTMOST -> RightmostView(Grammar.parse(v.grammar), v.input)
            VisualKind.MINIMIZE -> MinimizeView(v.input.toInt())
            VisualKind.AST -> AstView(v.input)
            VisualKind.REGALLOC -> RegAllocView(v.input)
            VisualKind.MIPS_RUN -> MipsRunView(v.grammar)
            VisualKind.MINILANG -> MiniLangView(v.input)
        }
    }
}

/** Navegador de passos ◀ ▶ reutilizável. */
@Composable
fun Stepper(count: Int, start: Int = 0, content: @Composable (Int) -> Unit) {
    var i by remember(count) { mutableIntStateOf(start.coerceIn(0, (count - 1).coerceAtLeast(0))) }
    if (count == 0) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton("◀", small = true, color = Palette.white, textColor = Palette.ink, enabled = i > 0) { i-- }
        Text("passo ${i + 1} de $count", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        BigButton("▶", small = true, color = Palette.teal, enabled = i < count - 1) { i++ }
        BigButton("⏭", small = true, color = Palette.white, textColor = Palette.ink, enabled = i < count - 1) { i = count - 1 }
    }
    content(i)
}

// ------------------------------------------------------------------ Pipeline

data class Phase(val emoji: String, val name: String, val output: String, val example: String)

val phases = listOf(
    Phase("📄", "Código-fonte", "texto", "posicao = inicial + taxa * 60"),
    Phase("🔪", "Análise léxica (scanner)", "tokens", "<id,1> <=> <id,2> <+> <id,3> <*> <num,60>"),
    Phase("🌳", "Análise sintática (parser)", "árvore sintática", "=(id1, +(id2, *(id3, 60)))"),
    Phase("🔍", "Análise semântica", "árvore anotada (tipos)", "=(id1, +(id2, *(id3, inttofloat(60))))"),
    Phase("🧩", "Geração de código intermediário", "código de 3 endereços", "t1 = inttofloat(60)\nt2 = id3 * t1\nt3 = id2 + t2\nid1 = t3"),
    Phase("⚡", "Otimização", "código melhor", "t1 = id3 * 60.0\nid1 = id2 + t1"),
    Phase("🖥️", "Geração de código", "código de máquina", "LDF R2, id3\nMULF R2, R2, #60.0\nLDF R1, id2\nADDF R1, R1, R2\nSTF id1, R1"),
)

@Composable
fun PipelineView() {
    Stepper(phases.size) { i ->
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (k in 0..i) {
                val p = phases[k]
                val current = k == i
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (current) Palette.yellow.copy(alpha = 0.35f) else Palette.paper)
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(p.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(p.name, style = MaterialTheme.typography.titleSmall)
                        if (current) Mono(p.example, size = 13)
                        else Text("→ ${p.output}", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Léxico

fun tokenColor(c: TokenClass): Color = when (c) {
    TokenClass.KEYWORD -> Palette.purple
    TokenClass.ID -> Palette.blue
    TokenClass.NUM -> Palette.orange
    TokenClass.OP -> Palette.red
    TokenClass.DELIM -> Palette.inkSoft
    TokenClass.STRING -> Palette.green
}

@Composable
fun TokenizeView(src: String) {
    val tokens = remember(src) { try { MiniLexer.tokenize(src) } catch (e: Exception) { emptyList() } }
    CodeBlock(src)
    Stepper(tokens.size) { i ->
        Column {
            Text("O scanner lê da esquerda para a direita, sempre pegando o MAIOR lexema possível:", style = MaterialTheme.typography.bodySmall)
            VSpace(6)
            ChipRow {
                tokens.forEachIndexed { k, t ->
                    Column(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(if (k <= i) tokenColor(t.cls).copy(alpha = 0.18f) else Palette.paperDark)
                            .border(2.dp, if (k == i) Palette.ink else Color.Transparent, RoundedCornerShape(8.dp)).padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Mono(if (k <= i) t.lexeme else "?", bold = true)
                        Text(if (k <= i) t.cls.label else "", fontSize = 10.sp, color = tokenColor(t.cls))
                    }
                }
            }
        }
    }
}

@Composable
fun DfaPlayground(regex: String) {
    val dfa = remember(regex) { DFA.fromRegex(regex) }
    var input by remember(regex) { mutableStateOf(dfa.samples(Random(regex.hashCode()), 1).firstOrNull()?.first ?: "") }
    var pos by remember(regex, input) { mutableIntStateOf(0) }
    val path = remember(dfa, input) { dfa.run(input) }
    Mono("regex: $regex", bold = true)
    DfaView(dfa, current = path.getOrNull(pos)?.takeIf { it >= 0 }, lastEdge = if (pos > 0) path[pos - 1] to path[pos] else null)
    Text("Duplo círculo = estado de aceitação. A seta solta aponta o inicial.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    OutlinedTextField(input, { s -> input = s.filter { it in dfa.alphabet } }, label = { Text("cadeia para testar (alfabeto: ${dfa.alphabet.joinToString("")})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Mono(input.take(pos).ifEmpty { "·" } + " | " + input.drop(pos), bold = true, modifier = Modifier.weight(1f))
        BigButton("passo", small = true, color = Palette.teal, enabled = pos < input.length && path[pos] >= 0) { pos++ }
        BigButton("↺", small = true, color = Palette.white, textColor = Palette.ink) { pos = 0 }
    }
    val st = path.getOrNull(pos) ?: -1
    when {
        st < 0 -> Text("💥 Sem transição: o autômato TRAVOU → rejeita.", color = Palette.red, fontWeight = FontWeight.Bold)
        pos == input.length -> Text(if (st in dfa.accepting) "✅ Terminou em q$st (aceitação): ACEITA" else "❌ Terminou em q$st (não é de aceitação): REJEITA",
            color = if (st in dfa.accepting) Palette.green else Palette.red, fontWeight = FontWeight.Bold)
        else -> Text("Estado atual: q$st. Próximo símbolo: '${input[pos]}'", style = MaterialTheme.typography.bodyMedium)
    }
}

// ------------------------------------------------------------------ Gramáticas

@Composable
fun DerivationView(g: Grammar, input: String) {
    val target = remember(input) { tokensOf(input) }
    val deriv = remember(g, input) { DerivationHelper.leftmostDerivations(g, target, limit = 1).firstOrNull() }
    GrammarText(g)
    if (deriv == null) { Text("Essa cadeia não é gerada pela gramática.", color = Palette.red); return }
    val forms = remember(deriv) {
        var f = SententialForm(listOf(g.start))
        val out = mutableListOf(f)
        for (p in deriv) { f = f.apply(f.leftmostNT(g), p); out += f }
        out
    }
    Stepper(forms.size) { i ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (k in 0..i) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (k == 0) "   " else "⇒ ", fontFamily = LocalFonts.current.mono)
                    SymbolsText(g, forms[k].symbols, size = if (k == i) 17 else 14)
                }
            }
            if (i > 0) Text("usou: ${deriv[i - 1]}  (sempre no não-terminal MAIS À ESQUERDA)", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            if (i == forms.size - 1) {
                VSpace(6)
                Text("Árvore de derivação:", style = MaterialTheme.typography.titleSmall)
                TreeView(DerivationHelper.treeOf(g, deriv).toUi(g))
            }
        }
    }
}

@Composable
fun ParseTreesView(g: Grammar, input: String) {
    val ds = remember(g, input) { DerivationHelper.leftmostDerivations(g, tokensOf(input), limit = 2) }
    GrammarText(g)
    Mono("frase: $input", bold = true)
    when (ds.size) {
        0 -> Text("A frase não pertence à linguagem.", color = Palette.red)
        1 -> { Text("Uma única árvore 👍", fontWeight = FontWeight.Bold); TreeView(DerivationHelper.treeOf(g, ds[0]).toUi(g)) }
        else -> {
            Text("⚠️ DUAS árvores diferentes para a mesma frase → gramática AMBÍGUA", color = Palette.red, fontWeight = FontWeight.Bold)
            Text("Árvore 1", style = MaterialTheme.typography.titleSmall); TreeView(DerivationHelper.treeOf(g, ds[0]).toUi(g))
            Text("Árvore 2", style = MaterialTheme.typography.titleSmall); TreeView(DerivationHelper.treeOf(g, ds[1]).toUi(g))
        }
    }
}

@Composable
fun BeforeAfter(before: Grammar, after: Grammar, what: String) {
    Text("Antes", style = MaterialTheme.typography.titleSmall)
    GrammarText(before)
    Text("⬇ $what", color = Palette.purple, fontWeight = FontWeight.Bold)
    Text("Depois", style = MaterialTheme.typography.titleSmall)
    GrammarText(after)
    val ll = remember(after) { LL1Table(after).isLL1 }
    Text(if (ll) "✅ O resultado é LL(1)." else "ℹ️ O resultado ainda não é LL(1) (pode precisar de outra transformação).",
        style = MaterialTheme.typography.bodySmall, color = if (ll) Palette.green else Palette.inkSoft)
}

// ------------------------------------------------------------------ FIRST / FOLLOW

@Composable
fun SetsTable(g: Grammar, first: Map<String, Set<String>>, follow: Map<String, Set<String>>, highlight: String? = null) {
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("", header = true, w = 60); Cell("FIRST", header = true, w = 150); Cell("FOLLOW", header = true, w = 150) }
        for (nt in g.nonTerminals) Row {
            Cell(nt, header = true, w = 60, color = if (nt == highlight) Palette.yellow else Palette.paperDark)
            Cell("{ " + first[nt].orEmpty().joinToString(", ") + " }", w = 150)
            Cell("{ " + follow[nt].orEmpty().joinToString(", ") + " }", w = 150)
        }
    }
}

@Composable
fun Cell(text: String, header: Boolean = false, w: Int = 70, color: Color? = null, textColor: Color = Palette.ink, onClick: (() -> Unit)? = null) {
    Box(
        Modifier.width(w.dp).border(1.dp, Palette.ink.copy(alpha = 0.35f))
            .background(color ?: if (header) Palette.paperDark else Palette.white)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = LocalFonts.current.mono, fontSize = 12.sp, fontWeight = if (header) FontWeight.Bold else FontWeight.Normal, color = textColor)
    }
}

@Composable
fun FirstFollowView(g: Grammar) {
    val ff = remember(g) { FirstFollow(g) }
    GrammarText(g)
    Text("Ponto fixo: repete as regras até nada mudar.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Stepper(ff.steps.size + 1) { i ->
        val first = g.nonTerminals.associateWith { mutableSetOf<String>() }
        val follow = g.nonTerminals.associateWith { mutableSetOf<String>() }
        for (s in ff.steps.take(i)) (if (s.kind == SetStep.Kind.FIRST) first else follow).getValue(s.nonTerminal) += s.added
        val cur = ff.steps.getOrNull(i - 1)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (cur != null) {
                RichText("**${cur.kind}(${cur.nonTerminal})** ganha **{ ${cur.added.joinToString(", ")} }**", style = MaterialTheme.typography.bodyMedium)
                Text(cur.reason, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            } else Text("Começa tudo vazio…", style = MaterialTheme.typography.bodyMedium)
            SetsTable(g, first, follow, cur?.nonTerminal)
        }
    }
}

// ------------------------------------------------------------------ LL(1)

@Composable
fun LL1TableView(t: LL1Table) {
    GrammarText(t.grammar)
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("M", header = true, w = 54); for (c in t.columns) Cell(c, header = true, w = 92) }
        for (nt in t.grammar.nonTerminals) Row {
            Cell(nt, header = true, w = 54)
            for (c in t.columns) {
                val ps = t.cell(nt, c)
                Cell(
                    ps.joinToString("\n") { "→ " + it.rhsText() }, w = 92,
                    color = when { ps.size > 1 -> Palette.red.copy(alpha = 0.25f); ps.isEmpty() -> Palette.white; else -> Palette.teal.copy(alpha = 0.12f) },
                )
            }
        }
    }
    Text(if (t.isLL1) "✅ Nenhuma célula com duas produções: a gramática é LL(1)." else "❌ Células vermelhas têm 2+ produções: CONFLITO, não é LL(1).",
        color = if (t.isLL1) Palette.green else Palette.red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun LL1TraceView(g: Grammar, input: String) {
    val t = remember(g) { LL1Table(g) }
    val steps = remember(g, input) { LL1Parser.trace(t, tokensOf(input)) }
    GrammarText(g)
    Stepper(steps.size) { i ->
        val s = steps[i]
        StackAndInput(g, s.stack, s.input)
        val a = s.action
        Text(
            when (a) {
                is LLAction.Expand -> "Topo é ${s.stack.last()}, olho '${s.input.first()}': M[${s.stack.last()}, ${s.input.first()}] = ${a.production}. Troca o topo pelo lado direito (de trás pra frente)."
                is LLAction.Match -> "Topo e entrada são '${a.terminal}': casa e avança!"
                LLAction.Accept -> "Pilha só com \$ e entrada acabou: ACEITA ✅"
                is LLAction.Error -> "Erro: ${a.message} ❌"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun StackAndInput(g: Grammar, stack: List<String>, input: List<String>, stackLabel: String = "Pilha (topo à direita)") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stackLabel, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            stack.forEachIndexed { k, s ->
                Box(
                    Modifier.padding(end = 3.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (k == stack.lastIndex) Palette.yellow else Palette.paperDark)
                        .border(1.dp, Palette.ink, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp)
                ) { SymbolsText(g, listOf(s), size = 14) }
            }
        }
        Text("Entrada", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            input.forEachIndexed { k, s ->
                Box(
                    Modifier.padding(end = 3.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (k == 0) Palette.teal.copy(alpha = 0.3f) else Palette.white)
                        .border(1.dp, Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp)
                ) { Mono(s, bold = k == 0) }
            }
        }
    }
}

// ------------------------------------------------------------------ LR

@Composable
fun LR0View(g: Grammar) {
    val t = remember(g) { LRTable(g, LRKind.SLR1) }
    val a = t.automaton
    GrammarText(g)
    Text("Gramática aumentada: ${a.grammar.productions[0]}", style = MaterialTheme.typography.bodySmall)
    Stepper(a.states.size) { i ->
        val st = a.states[i]
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Estado I$i", style = MaterialTheme.typography.titleMedium)
            for (it in st.items) {
                val kernel = it in st.kernel
                Mono((if (kernel) "★ " else "  ") + it.render(a.grammar), bold = kernel, color = if (kernel) Palette.ink else Palette.inkSoft)
            }
            Text("★ = núcleo (kernel). O resto veio do fechamento (closure).", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            val outs = a.transitions.filterKeys { it.first == i }
            if (outs.isNotEmpty()) ChipRow { for ((k, v) in outs) Chip("goto(I$i, ${k.second}) = I$v", mono = true, color = Palette.teal) }
            val red = st.items.filter { it.dot >= a.grammar.productions[it.prodId].rhs.size }
            for (r in red) Text("item completo: ${r.render(a.grammar)} → reduzir", color = Palette.orange, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LRTableView(t: LRTable, highlightCells: Set<Pair<Int, String>> = emptySet()) {
    GrammarText(t.grammar)
    Text("Produções numeradas: " + t.grammar.productions.drop(1).joinToString("   ") { "r${it.id}: $it" }, style = MaterialTheme.typography.bodySmall)
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row {
            Cell("", header = true, w = 44)
            for (c in t.terminals) Cell(c, header = true, w = 62, color = Palette.orange.copy(alpha = 0.25f))
            for (n in t.nonTerminals) Cell(n, header = true, w = 54, color = Palette.purple.copy(alpha = 0.2f))
        }
        for (s in t.automaton.states.indices) Row {
            Cell("$s", header = true, w = 44)
            for (c in t.terminals) {
                val acts = t.rawActions[s to c].orEmpty()
                Cell(
                    acts.joinToString("/") { it.short() }, w = 62,
                    color = when {
                        (s to c) in highlightCells -> Palette.yellow
                        acts.size > 1 -> Palette.red.copy(alpha = 0.3f)
                        else -> null
                    },
                )
            }
            for (n in t.nonTerminals) Cell(t.gotoTable[s to n]?.toString() ?: "", w = 54)
        }
    }
    val c = t.rawConflictCells
    Text(
        if (c.isEmpty()) "✅ Sem conflitos: a gramática é ${t.kind.name.replace("1", "(1)")}." else "❌ ${c.size} célula(s) em conflito (em vermelho).",
        color = if (c.isEmpty()) Palette.green else Palette.red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun LRTraceView(g: Grammar, input: String) {
    val t = remember(g) { LRTable(g, LRKind.SLR1) }
    val r = remember(g, input) { LRParser.parse(t, tokensOf(input)) }
    GrammarText(g)
    Stepper(r.steps.size) { i ->
        val s = r.steps[i]
        val stackView = s.states.zip(listOf("") + s.symbols).flatMap { (st, sym) -> if (sym.isEmpty()) listOf("$st") else listOf(sym, "$st") }
        StackAndInput(t.grammar, stackView, s.input, "Pilha (estados e símbolos, topo à direita)")
        Text(
            when (val a = s.action) {
                is LRAction.Shift -> "SHIFT: ${s.note}"
                is LRAction.Reduce -> "REDUCE: ${s.note}"
                LRAction.Accept -> "ACCEPT ✅"
                null -> "ERRO ❌ ${s.note}"
            }, style = MaterialTheme.typography.bodyMedium,
        )
        if (i == r.steps.lastIndex && r.tree != null) TreeView(r.tree.toUi(t.grammar))
    }
}

// ------------------------------------------------------------------ Semântica

@Composable
fun AttrTreeView(src: String) {
    val e = remember(src) { ExprParser.parse(src) }
    val attrs = remember(e) { TypeChecker.evaluate(e, ExprGenerator.env) }
    val order = remember(e) { e.postorder() }
    Text("Tabela de símbolos: a,b,c: int (4,2,3) · x,y: float (1.5, 0.5) · ok: bool", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Mono("expressão: $src", bold = true)
    Stepper(order.size + 1) { i ->
        val done = order.take(i).map { it.id }.toSet()
        TreeView(e.toUi(sub = { n -> if (n.id in done) attrs[n.id]?.let { "${it.type.label}:${TypeChecker.formatValue(it)}" } else null },
            color = { n -> if (n.id in done) Palette.nodeActive else if (n.isLeaf) Palette.nodeLeaf else Palette.nodeNonTerminal }))
        val cur = order.getOrNull(i - 1)
        if (cur != null) Text("Nó '${cur.text}': ${attrs[cur.id]?.rule}", style = MaterialTheme.typography.bodyMedium)
        else Text("Atributos sintetizados sobem das folhas para a raiz (pós-ordem).", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TacView(src: String) {
    val e = remember(src) { ExprParser.parse(src) }
    val code = remember(e) { TacGen.generate(e, "r") }
    TreeView(e.toUi())
    Stepper(code.size) { i ->
        Column {
            code.take(i + 1).forEachIndexed { k, ins -> Mono(ins.toString(), bold = k == i, color = if (k == i) Palette.purple else Palette.ink) }
            VSpace(4)
            Text("Cada instrução tem no máximo UM operador e TRÊS endereços.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        }
    }
    val folded = remember(e) { TacGen.constantFold(e) }
    if (folded.render() != e.render()) Text("Otimizando (dobra de constantes): ${folded.render()}", color = Palette.teal, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun ScopeView() {
    var seed by remember { mutableIntStateOf(1) }
    val prog = remember(seed) { ScopeGen.random(Random(seed)) }
    var sel by remember(seed) { mutableStateOf<Int?>(null) }
    Text("Toque num USO de variável para ver a declaração que ele enxerga (escopo estático: o mais interno que já foi declarado).", style = MaterialTheme.typography.bodySmall)
    Column(Modifier.clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        prog.lines.forEachIndexed { idx, line ->
            val target = sel?.let { prog.lines[it].resolvesTo }
            val bg = when { idx == sel -> Palette.yellow.copy(alpha = 0.5f); idx == target -> Palette.green.copy(alpha = 0.5f); else -> Color.Transparent }
            Text(
                "  ".repeat(line.indent) + line.text,
                fontFamily = LocalFonts.current.mono, fontSize = 14.sp, color = Color(0xFFE8E6F0),
                modifier = Modifier.fillMaxWidth().background(bg).then(if (line.use != null) Modifier.clickable { sel = idx } else Modifier),
            )
        }
    }
    sel?.let { s ->
        val l = prog.lines[s]
        Text(if (l.resolvesTo == null) "'${l.use}' NÃO foi declarada em nenhum escopo visível → erro semântico!" else "'${l.use}' refere-se à declaração destacada em verde.",
            fontWeight = FontWeight.Bold, color = if (l.resolvesTo == null) Palette.red else Palette.green)
    }
    BigButton("🎲 outro programa", small = true, color = Palette.teal) { seed++ }
}

@Composable
fun BisonView(spec: String, input: String) {
    val b = remember(spec) { MiniBison.compile(spec) }
    var text by remember(spec) { mutableStateOf(input.ifBlank { "2+3*4" }) }
    val r = remember(b, text) { b.run(text) }
    CodeBlock(spec, "especificação .y")
    if (b.conflicts.isNotEmpty()) Text("⚠️ bison: ${b.conflicts.size} conflito(s) — " + b.conflicts.take(2).joinToString("; ") { "estado ${it.state}, ${it.terminal}: ${it.resolution ?: it.kind}" },
        color = Palette.red, style = MaterialTheme.typography.bodySmall)
    else if (b.resolvedByPrecedence > 0) Text("✅ ${b.resolvedByPrecedence} conflito(s) resolvido(s) por precedência/associatividade.", color = Palette.green, style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(text, { text = it }, label = { Text("entrada") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    if (r.accepted) {
        Text("Resultado: ${r.value}", style = MaterialTheme.typography.titleLarge, color = Palette.teal)
        Text("Reduções (cada uma executa uma ação):", style = MaterialTheme.typography.labelMedium)
        for (line in r.reductions) Mono(line, size = 12)
        r.tree?.let { TreeView(it.toUi(null)) }
    } else Text("❌ ${r.error}", color = Palette.red)
}

@Suppress("unused")
private fun Modifier.maxW() = this.widthIn(max = 600.dp)

internal val EOF_SYMBOL = EOF
internal val EPS_SYMBOL = EPS
