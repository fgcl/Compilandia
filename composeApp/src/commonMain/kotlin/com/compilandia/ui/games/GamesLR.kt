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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.Assoc
import com.compilandia.engine.BisonChallenges
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.LRItem
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniBison
import com.compilandia.engine.Precedence
import com.compilandia.engine.SentenceGenerator
import com.compilandia.engine.render
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.TreeView
import com.compilandia.ui.toUi
import com.compilandia.ui.visuals.Cell
import com.compilandia.ui.visuals.LRTableView
import com.compilandia.ui.visuals.StackAndInput
import kotlinx.coroutines.delay

// ============================================================ Esteira Shift-Reduce

@Composable
fun ShiftReduceGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done ->
        val g = remember {
            when {
                ctx.level <= 1 -> GrammarBank.slrNotLL.grammar
                ctx.level == 2 -> Grammar.parse("E -> E + n | n")
                else -> GrammarBank.exprLeftRec.grammar
            }
        }
        val input = remember { SentenceGenerator.generate(g, ctx.rnd, maxDepth = 2 + ctx.level, maxTokens = 1 + 2 * ctx.level) ?: listOf("b") }
        ShiftReduceRound(ctx, g, input, done)
    }
}

@Composable
private fun ShiftReduceRound(ctx: GameCtx, g: Grammar, input: List<String>, done: (Double) -> Unit) {
    val t = remember(g) { LRTable(g, LRKind.SLR1) }
    val res = remember(g, input) { LRParser.parse(t, input) }
    var step by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var stepMistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var timeLeft by remember(step) { mutableFloatStateOf(1f) }
    val timed = ctx.level >= 4
    val cur = res.steps.getOrNull(step)
    GrammarText(g)
    if (cur == null) {
        FeedbackBanner(true, "ACEITO! Repare: as reduções, lidas de trás para frente, são uma derivação mais à DIREITA.")
        res.tree?.let { TreeView(it.toUi(t.grammar)) }
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    fun choose(a: LRAction?) {
        val right = cur.action
        if (a == right) {
            step++; stepMistakes = 0; Juice.correct()
            msg = true to (cur.note)
        } else {
            mistakes++; stepMistakes++; Juice.wrong()
            msg = if (stepMistakes >= 2) { step++; stepMistakes = 0; false to "Era: ${cur.note}" }
            else false to when (right) {
                is LRAction.Shift -> "Ainda não há um handle completo no topo. Empilhe mais!"
                is LRAction.Reduce -> "O topo da pilha já forma o lado direito de uma produção (handle) e o próximo token está no FOLLOW dela."
                LRAction.Accept -> "Só sobrou o símbolo inicial e a entrada acabou!"
                null -> "Nenhuma ação vale aqui: é erro de sintaxe."
            }
        }
    }
    if (timed) LaunchedEffect(step) {
        var ms = 0L
        val total = 14000L
        while (ms < total) { delay(100); ms += 100; timeLeft = 1f - ms.toFloat() / total }
        mistakes++; step++; msg = false to "⏰ Tempo! Era: ${cur.note}"
    }
    if (timed) ProgressBar(timeLeft, Palette.orange, Modifier.fillMaxWidth(), 8.dp)
    // A torre mostra cada símbolo com o estado que veio junto; o estado do topo decide a ação.
    val stackView = listOf("estado ${cur.states.first()}") + cur.symbols.zip(cur.states.drop(1)).map { (sym, st) -> "$sym  ${st}" }
    BeltStage(stackView, cur.input)
    HintBox(
        ctx,
        "Olhe o estado do topo (${cur.states.last()}) e o token '${cur.input.first()}': ACTION[${cur.states.last()}, ${cur.input.first()}] = ${t.action[cur.states.last() to cur.input.first()]?.short() ?: "vazio"}",
        "Abaixo, a tabela SLR inteira.",
    )
    if (ctx.hints >= 2) LRTableView(t, setOf(cur.states.last() to cur.input.first()))
    msg?.let { FeedbackBanner(it.first, it.second) }
    val nextState = t.action[cur.states.last() to cur.input.first()].let { if (it is LRAction.Shift) it.state else -1 }
    BigButton("Empilhar ${cur.input.first()}", Modifier.fillMaxWidth(), color = Palette.teal) {
        choose(if (nextState >= 0) LRAction.Shift(nextState) else LRAction.Shift(-99))
    }
    for (p in t.grammar.productions.drop(1)) BigButton("Reduzir $p", Modifier.fillMaxWidth(), color = Palette.orange) { choose(LRAction.Reduce(p.id)) }
    ChipRow {
        Chip("Aceitar", color = Palette.green) { choose(LRAction.Accept) }
        Chip("Erro", color = Palette.red) { choose(null) }
    }
}

// ============================================================ Construtor de Estados

@Composable
fun ItemBuilderGame(ctx: GameCtx) {
    val g = remember {
        when {
            ctx.level <= 2 -> listOf(Grammar.parse("S -> ( S ) | x"), Grammar.parse("E -> E + T | T\nT -> id")).random(ctx.rnd)
            else -> listOf(GrammarBank.exprLeftRec.grammar, GrammarBank.lalrNotSlr.grammar).random(ctx.rnd)
        }
    }
    val a = remember { LRAutomaton.lr0(g) }
    val candidates = remember { a.states.filter { it.items.size > it.kernel.size }.shuffled(ctx.rnd).take(2) }
    Rounds(ctx, candidates.size.coerceAtLeast(1)) { round, done ->
        ItemRound(ctx, a, candidates.getOrElse(round) { a.states[0] }.id, done)
    }
}

@Composable
private fun ItemRound(ctx: GameCtx, a: LRAutomaton, stateId: Int, done: (Double) -> Unit) {
    val g = a.grammar
    val st = a.states[stateId]
    val closureOnly = remember { st.items - st.kernel }
    val options = remember {
        val zeroItems = g.productions.map { LRItem(it.id, 0) }.filter { it !in st.kernel }
        val distract = g.productions.filter { it.rhs.isNotEmpty() }.map { LRItem(it.id, 1) }.filter { it !in st.items }.shuffled(ctx.rnd).take(2)
        (zeroItems + distract).distinct().shuffled(ctx.rnd)
    }
    val picked = remember { mutableStateMapOf<LRItem, Boolean>() }
    var phase by remember { mutableIntStateOf(0) }
    var closureScore by remember { mutableFloatStateOf(0f) }
    val gotoSym = remember { a.transitions.keys.filter { it.first == stateId }.map { it.second }.shuffled(ctx.rnd).firstOrNull() }
    val gotoTarget = gotoSym?.let { a.transitions[stateId to it] }
    val gotoOptions = remember { (listOfNotNull(gotoTarget) + a.states.map { it.id }.filter { it != gotoTarget && it != stateId }.shuffled(ctx.rnd).take(2)).shuffled(ctx.rnd) }
    var gotoPick by remember { mutableStateOf<Int?>(null) }

    GrammarText(g)
    PaperCard(color = Palette.paper) {
        Text("Núcleo (kernel) do estado:", style = MaterialTheme.typography.labelMedium)
        for (it in st.kernel) Mono("★ " + it.render(g), bold = true)
    }
    if (phase == 0) {
        Text("Quais itens o FECHAMENTO acrescenta?", style = MaterialTheme.typography.titleSmall)
        HintBox(ctx, "Para cada item com • antes de um NÃO-TERMINAL B, adicione B → •γ para TODA produção de B. Repita para os novos itens!", "Itens com o • no meio (como X → a • b) nunca são adicionados pelo fechamento.")
        for (o in options) {
            val on = picked[o] == true
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (on) Palette.purple.copy(alpha = 0.15f) else Palette.white)
                    .border(2.dp, if (on) Palette.purple else Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { picked[o] = !on; Sound.play(Sfx.CLICK) }.padding(10.dp)
            ) { Mono((if (on) "☑ " else "☐ ") + o.render(g), size = 15) }
        }
        BigButton("Fechar estado ✔", Modifier.fillMaxWidth(), color = Palette.teal) {
            val mine = picked.filterValues { it }.keys
            val inter = mine.intersect(closureOnly).size
            val union = (mine + closureOnly).size
            closureScore = if (union == 0) 1f else inter.toFloat() / union
            if (closureScore == 1f) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else {
        FeedbackBanner(closureScore == 1f, if (closureScore == 1f) "Fechamento perfeito!" else "O estado completo é:")
        PaperCard { for (it in st.items) Mono((if (it in st.kernel) "★ " else "  ") + it.render(g), bold = it in st.kernel) }
        if (gotoSym == null || gotoTarget == null) {
            BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done(closureScore.toDouble()) }
        } else {
            Text("Agora: GOTO(I$stateId, $gotoSym) = qual estado? (avance o • sobre $gotoSym e feche)", style = MaterialTheme.typography.titleSmall)
            for (opt in gotoOptions) {
                val sel = gotoPick == opt
                val color = when { gotoPick == null -> Palette.white; opt == gotoTarget -> Palette.green.copy(alpha = 0.25f); sel -> Palette.red.copy(alpha = 0.2f); else -> Palette.white }
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(color).border(2.dp, Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable(enabled = gotoPick == null) { gotoPick = opt; if (opt == gotoTarget) Juice.correct() else Juice.wrong() }.padding(10.dp)
                ) { for (it in a.states[opt].kernel) Mono(it.render(g), size = 14) }
            }
            if (gotoPick != null) BigButton("Continuar ▶", Modifier.fillMaxWidth()) {
                done(0.6 * closureScore + 0.4 * (if (gotoPick == gotoTarget) 1.0 else 0.0))
            }
        }
    }
}

// ============================================================ Caça-Conflitos

@Composable
fun ConflictHunterGame(ctx: GameCtx) {
    val choice = remember {
        val opts = if (ctx.level <= 2) listOf(GrammarBank.exprAmbiguous, GrammarBank.ifElse)
        else listOf(GrammarBank.exprAmbiguous, GrammarBank.ifElse, GrammarBank.lalrNotSlr, GrammarBank.exprLeftRec)
        opts[ctx.rnd.nextInt(opts.size)]
    }
    val g = choice.grammar
    val t = remember { LRTable(g, LRKind.SLR1) }
    val conflicts = remember { t.rawConflictCells.toSet() }
    val hidden = remember {
        val decoys = t.rawActions.keys.filter { it !in conflicts && (t.rawActions[it]?.any { a -> a is LRAction.Reduce } == true) }.shuffled(ctx.rnd).take(3)
        (conflicts + decoys).toSet()
    }
    val marks = remember { mutableStateMapOf<Pair<Int, String>, Boolean>() }
    var phase by remember { mutableIntStateOf(0) }
    var huntScore by remember { mutableFloatStateOf(0f) }

    Text("Gramática: ${choice.title}", style = MaterialTheme.typography.titleSmall)
    GrammarText(g)
    if (phase == 0) {
        Text("Algumas células da tabela SLR estão escondidas (?). Toque nas que têm CONFLITO (duas ações).", style = MaterialTheme.typography.bodyMedium)
        HintBox(
            ctx,
            "Conflito shift/reduce: o estado tem um item A → α • (reduz) E um item com • antes do terminal t, com t ∈ FOLLOW(A).",
            "FOLLOW: " + t.grammar.nonTerminals.drop(1).joinToString("  ") { "$it{${t.ff.follow.getValue(it).joinToString(",")}}" },
        )
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row { Cell("", header = true, w = 40); for (c in t.terminals) Cell(c, header = true, w = 58) }
            for (s in t.automaton.states.indices) Row {
                Cell("$s", header = true, w = 40)
                for (c in t.terminals) {
                    val key = s to c
                    if (key in hidden) Cell(if (marks[key] == true) "⚔️" else "?", w = 58, color = if (marks[key] == true) Palette.red.copy(alpha = 0.3f) else Palette.yellow.copy(alpha = 0.4f), onClick = { marks[key] = marks[key] != true })
                    else Cell(t.rawActions[key].orEmpty().joinToString("/") { it.short() }, w = 58)
                }
            }
        }
        StatesPeek(t)
        BigButton(if (marks.values.none { it }) "Não há conflitos ✔" else "Conferir ✔", Modifier.fillMaxWidth(), color = Palette.teal) {
            val mine = marks.filterValues { it }.keys
            val good = hidden.count { (it in mine) == (it in conflicts) }
            huntScore = good.toFloat() / hidden.size.coerceAtLeast(1)
            if (huntScore == 1f) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else {
        FeedbackBanner(huntScore == 1f, "Você acertou ${(huntScore * 100).toInt()}% das células escondidas. Conflitos reais: " +
            (if (conflicts.isEmpty()) "nenhum — a gramática é SLR(1)!" else conflicts.joinToString { "[${it.first}, ${it.second}]" }))
        LRTableView(t, conflicts)
        ConflictResolution(ctx, choice.id, g, conflicts, huntScore.toDouble())
    }
}

@Composable
private fun StatesPeek(t: LRTable) {
    var open by remember { mutableStateOf(false) }
    Text(if (open) "▲ esconder estados" else "▼ ver estados LR(0) (itens)", color = Palette.purple, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { open = !open })
    if (open) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (st in t.automaton.states) {
            Text("I${st.id}", style = MaterialTheme.typography.labelMedium)
            for (it in st.items) Mono("  " + it.render(t.grammar), size = 12)
        }
    }
}

@Composable
private fun ConflictResolution(ctx: GameCtx, gid: String, g: Grammar, conflicts: Set<Pair<Int, String>>, huntScore: Double) {
    // Pergunta extra conforme a gramática
    data class Q(val text: String, val options: List<String>, val right: Int, val why: String)
    val q = remember {
        when (gid) {
            "ambig" -> {
                val prec = mapOf("+" to Precedence(1, Assoc.LEFT), "*" to Precedence(2, Assoc.LEFT))
                val tp = LRTable(g, LRKind.SLR1, prec, resolveConflicts = true)
                val cell = conflicts.firstOrNull { it.second == "*" } ?: conflicts.first()
                val act = tp.action[cell]
                val items = tp.automaton.states[cell.first].items.filter { it.dot >= tp.grammar.productions[it.prodId].rhs.size }.joinToString { it.render(tp.grammar) }
                Q("Com  %left '+'  e depois  %left '*'  (o * liga mais forte), o que o Bison faz no estado ${cell.first} vendo '${cell.second}'? (item completo: $items)",
                    listOf("SHIFT", "REDUCE"), if (act is LRAction.Shift) 0 else 1,
                    "Comparamos a precedência do token '${cell.second}' com a da regra (último terminal dela). " + (if (act is LRAction.Shift) "O token é mais forte → SHIFT." else "A regra é mais forte ou igual com %left → REDUCE."))
            }
            "ifelse" -> Q("Sem declarar precedência, como o Bison resolve o 'else pendurado'?", listOf("SHIFT: o else fica com o if mais próximo", "REDUCE: o else fica com o if de fora", "Recusa compilar"), 0,
                "Por padrão o Bison prefere SHIFT (e avisa o conflito). Isso cola o else no if mais interno — exatamente o que C e Java fazem.")
            "lalr" -> Q("Esse conflito existe no SLR porque FOLLOW(R) contém '='. E no LALR(1)?", listOf("Some: o lookahead do item R → L• naquele estado é só $", "Continua igual", "Vira reduce/reduce"), 0,
                "O LALR usa lookaheads específicos por estado, mais precisos que o FOLLOW global. Por isso o Bison (LALR) aceita essa gramática.")
            else -> Q("Esta gramática não tem conflitos SLR. Isso significa que…", listOf("ela não é ambígua (gramáticas SLR nunca são ambíguas)", "ela é LL(1) também", "o Bison vai reclamar"), 0,
                "Toda gramática LR (sem conflitos) é não ambígua. Mas ela pode NÃO ser LL(1) — esta tem recursão à esquerda!")
        }
    }
    var pick by remember { mutableStateOf<Int?>(null) }
    PaperCard(border = Palette.purple) {
        RichText("🦬 " + q.text, style = MaterialTheme.typography.titleSmall)
        q.options.forEachIndexed { i, o ->
            BigButton(o, Modifier.fillMaxWidth(), small = true, enabled = pick == null || i == pick || i == q.right,
                color = when { pick == null -> Palette.white; i == q.right -> Palette.green; else -> Palette.red }) {
                if (pick == null) { pick = i; if (i == q.right) Juice.correct() else Juice.wrong() }
            }
        }
        if (pick != null) {
            FeedbackBanner(pick == q.right, q.why)
            BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(0.6 * huntScore + 0.4 * (if (pick == q.right) 1.0 else 0.0)) }
        }
    }
}

// ============================================================ Oficina Bison

private val snippets = listOf(
    "%left '+' '-'\n", "%left '*' '/'\n", "%right '^'\n", "%nonassoc '<'\n", "%right UMINUS\n",
    "     | expr '^' expr { \$\$ = \$1 ^ \$3; }\n", "     | '-' expr %prec UMINUS { \$\$ = -\$2; }\n",
    "term : term '*' fator { \$\$ = \$1 * \$3; } | fator ;\n", "fator : NUM ;\n",
)

@Composable
fun BisonWorkshopGame(ctx: GameCtx) {
    val ch = remember {
        val idx = ((ctx.level - 1) + ctx.rnd.nextInt(2)).coerceIn(0, BisonChallenges.all.lastIndex)
        BisonChallenges.all[idx]
    }
    var spec by remember { mutableStateOf(TextFieldValue(ch.starterSpec)) }
    var runs by remember { mutableIntStateOf(0) }
    var verdict by remember { mutableStateOf<BisonChallenges.Verdict?>(null) }
    var gaveUp by remember { mutableStateOf(false) }
    PaperCard(color = Palette.paper, border = Palette.orange) {
        Text("🦬 ${ch.title}", style = MaterialTheme.typography.headlineSmall)
        RichText(ch.story, style = MaterialTheme.typography.bodyMedium)
        Text("Testes: " + ch.tests.joinToString("   ") { "${it.first} → ${it.second ?: "erro"}" }, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    }
    HintBox(ctx, ch.hint, "Regra de ouro do Bison: em conflito, compara a precedência do TOKEN que chega com a da REGRA (dada pelo último terminal da regra).")
    OutlinedTextField(
        spec, { spec = it }, Modifier.fillMaxWidth(), label = { Text("calc.y") },
        textStyle = TextStyle(fontFamily = LocalFonts.current.mono, fontSize = 13.sp), minLines = 8,
    )
    Text("Blocos (inserem no cursor):", style = MaterialTheme.typography.labelMedium)
    ChipRow {
        for (s in snippets) Chip(s.trim().take(28), mono = true, color = Palette.orange) {
            val sel = spec.selection.start.coerceIn(0, spec.text.length)
            val text = spec.text.substring(0, sel) + s + spec.text.substring(sel)
            spec = TextFieldValue(text, TextRange(sel + s.length))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton("▶ bison calc.y && ./calc", Modifier.weight(1f), color = Palette.ink) {
            runs++
            verdict = BisonChallenges.check(ch, spec.text)
            if (verdict!!.passed) Juice.levelUp() else Juice.wrong()
        }
        BigButton("↺", color = Palette.white, textColor = Palette.ink) { spec = TextFieldValue(ch.starterSpec) }
    }
    verdict?.let { v ->
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
            for ((ok, line) in v.lines) Text((if (ok) "✔ " else "✘ ") + line, color = if (ok) Palette.green else Palette.red, fontFamily = LocalFonts.current.mono, fontSize = 12.sp)
        }
        val compiled = try { MiniBison.compile(spec.text) } catch (e: Exception) { null }
        if (compiled != null && compiled.conflicts.isNotEmpty()) {
            Text("Detalhes dos conflitos:", style = MaterialTheme.typography.labelMedium)
            for (c in compiled.conflicts.take(3)) Mono("estado ${c.state}, token ${c.terminal}: ${c.actions.joinToString(" vs ") { it.short() }} → ${c.resolution}", size = 11)
        }
        if (v.passed) {
            FeedbackBanner(true, "Todos os testes passaram em $runs execução(ões)! Você acabou de programar um gerador de analisadores. 🎉")
            BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.08 * (runs - 1)).coerceIn(0.5, 1.0)) }
        }
    }
    if (verdict?.passed != true) {
        if (!gaveUp && runs >= 3) BigButton("🏳️ Mostrar a dica completa e encerrar", Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) { gaveUp = true }
        if (gaveUp) {
            FeedbackBanner(false, ch.hint)
            BigButton("Encerrar (vou revisar depois)", Modifier.fillMaxWidth()) { ctx.finish(0.2) }
        }
    }
}

