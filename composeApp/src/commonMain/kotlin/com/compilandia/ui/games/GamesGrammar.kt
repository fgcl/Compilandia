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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EOF
import com.compilandia.engine.EPS
import com.compilandia.engine.Earley
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.GrammarGenerator
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Parser
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LLAction
import com.compilandia.engine.Production
import com.compilandia.engine.SentenceGenerator
import com.compilandia.engine.SententialForm
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
import com.compilandia.ui.SymbolsText
import com.compilandia.ui.TreeView
import com.compilandia.ui.toUi
import com.compilandia.ui.visuals.Cell
import com.compilandia.ui.visuals.StackAndInput

// ============================================================ Derivação Turbo

fun derivationGrammarFor(level: Int): Grammar = when (level) {
    1 -> listOf(GrammarBank.anbn, GrammarBank.parens)
    2 -> listOf(GrammarBank.decl, GrammarBank.stmts)
    3 -> listOf(GrammarBank.exprLL1, GrammarBank.decl)
    else -> listOf(GrammarBank.exprLeftRec, GrammarBank.exprLL1)
}.let { it[(level * 31 + it.size) % it.size].grammar }

@Composable
fun DerivationRaceGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done ->
        val g = remember { listOf(derivationGrammarFor(ctx.level), derivationGrammarFor((ctx.level % 5) + 1)).random(ctx.rnd) }
        val target = remember { SentenceGenerator.generate(g, ctx.rnd, maxDepth = 3 + ctx.level, maxTokens = 3 + 2 * ctx.level) ?: listOf("id") }
        DerivationBoard(ctx, g, target, forbidden = null) { mistakes, _ -> done((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
    }
}

/**
 * Tabuleiro de derivação mais à esquerda. [forbidden]: uma derivação que não pode ser repetida
 * (usado no jogo das Duas Árvores).
 */
@Composable
fun DerivationBoard(
    ctx: GameCtx,
    g: Grammar,
    target: List<String>,
    forbidden: List<Production>?,
    onDone: (mistakes: Int, derivation: List<Production>) -> Unit,
) {
    val earley = remember(g) { Earley(g) }
    var form by remember { mutableStateOf(SententialForm(listOf(g.start))) }
    val history = remember { mutableStateListOf<Production>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val idx = form.leftmostNT(g)
    val finished = idx < 0 && form.symbols == target

    GrammarText(g)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🎯 Alvo: ", style = MaterialTheme.typography.titleSmall)
        SymbolsText(g, target, size = 17)
    }
    PaperCard(color = Palette.paper) {
        Text("Forma sentencial atual:", style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            form.symbols.forEachIndexed { k, s ->
                Box(
                    Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (k == idx) Palette.yellow else if (g.isNonTerminal(s)) Palette.purple.copy(alpha = 0.12f) else Palette.teal.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) { SymbolsText(g, listOf(s), size = 18) }
            }
            if (form.symbols.isEmpty()) Mono(EPS, size = 18)
        }
        Text("${history.size} passos · ${mistakes} becos", style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
    }
    HintBox(
        ctx,
        "Olhe o próximo terminal do ALVO que ainda não apareceu: qual produção pode começar com ele?",
        "Boas escolhas agora: " + (if (idx >= 0) DerivationHelper.goodChoices(g, earley, form, target).joinToString(" ou ") { it.rhsText() } else "—"),
    )
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (finished) {
        FeedbackBanner(true, "Derivação completa em ${history.size} passos! Aqui está a árvore:")
        TreeView(DerivationHelper.treeOf(g, history.toList()).toUi(g))
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { onDone(mistakes, history.toList()) }
    } else if (idx >= 0) {
        Text("Expandir ${form.symbols[idx]} com:", style = MaterialTheme.typography.titleSmall)
        for (p in g.productionsOf(form.symbols[idx])) {
            BigButton("${p.lhs} → ${p.rhsText()}", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink, small = true) {
                val nf = form.apply(idx, p)
                val wouldRepeat = forbidden != null && nf.leftmostNT(g) < 0 && (history + p) == forbidden
                if (wouldRepeat) {
                    mistakes++; msg = false to "Essa é a MESMA derivação da primeira árvore. Ache outro caminho!"; Juice.wrong()
                } else if (DerivationHelper.viable(g, earley, nf.symbols, target) && nf.symbols.count { !g.isNonTerminal(it) } <= target.size) {
                    form = nf; history += p; msg = null; Sound.play(Sfx.CLICK)
                } else {
                    mistakes++
                    msg = false to "Beco sem saída: de \"${nf.text()}\" não dá mais para chegar no alvo."
                    Juice.wrong()
                }
            }
        }
        if (history.isNotEmpty()) BigButton("↶ desfazer", small = true, color = Palette.white, textColor = Palette.ink) {
            history.removeAt(history.lastIndex)
            var f = SententialForm(listOf(g.start))
            for (p in history) f = f.apply(f.leftmostNT(g), p)
            form = f
        }
    }
}

// ============================================================ Duas Árvores

@Composable
fun TwoTreesGame(ctx: GameCtx) {
    val pick = remember {
        val options = listOf(
            GrammarBank.exprAmbiguous.grammar to listOf("id", "+", "id", "*", "id"),
            GrammarBank.exprAmbiguous.grammar to listOf("id", "+", "id", "+", "id"),
            GrammarBank.ifElse.grammar to listOf("if", "c", "then", "if", "c", "then", "x", "else", "x"),
            Grammar.parse("S -> S S | a") to listOf("a", "a", "a"),
        )
        options[if (ctx.level <= 2) ctx.rnd.nextInt(2) else ctx.rnd.nextInt(options.size)]
    }
    val (g, target) = pick
    var first by remember { mutableStateOf<List<Production>?>(null) }
    var totalMistakes by remember { mutableIntStateOf(0) }
    var second by remember { mutableStateOf<List<Production>?>(null) }
    when {
        first == null -> {
            Text("Árvore 1 de 2", style = MaterialTheme.typography.headlineSmall)
            DerivationBoard(ctx, g, target, null) { m, d -> totalMistakes += m; first = d }
        }
        second == null -> {
            Text("Árvore 2 de 2 — tem que ser DIFERENTE!", style = MaterialTheme.typography.headlineSmall)
            HintBox(ctx, "Na primeira árvore, qual operador ficou mais perto da raiz? Tente colocar o OUTRO lá em cima.")
            androidx.compose.runtime.key("second") {
                DerivationBoard(ctx, g, target, first) { m, d -> totalMistakes += m; second = d }
            }
        }
        else -> {
            FeedbackBanner(true, "Duas derivações mais à esquerda diferentes = duas árvores = **gramática ambígua**. É por isso que compiladores reescrevem a gramática ou usam precedência (%left no Bison)!")
            Text("Árvore 1", style = MaterialTheme.typography.titleSmall)
            TreeView(DerivationHelper.treeOf(g, first!!).toUi(g))
            Text("Árvore 2", style = MaterialTheme.typography.titleSmall)
            TreeView(DerivationHelper.treeOf(g, second!!).toUi(g))
            BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * totalMistakes).coerceAtLeast(0.0)) }
        }
    }
}

// ============================================================ Cirurgia de Gramática

private val surgeryLR = listOf("E -> E + T | T\nT -> id", "S -> S a | b", "L -> L , id | id", "A -> A x | A y | z", "E -> E - T | T\nT -> num")
private val surgeryLF = listOf("A -> a b | a c", "C -> id ( ) | id", "S -> a B c | a B d\nB -> b", "S -> if c then S | if c then S else S | x", "D -> int id ; | int id = num ;")

@Composable
fun GrammarSurgeryGame(ctx: GameCtx) {
    Rounds(ctx, 2) { round, done ->
        val useLR = remember { if (ctx.skill.name == "LEFT_FACTORING") false else if (ctx.skill.name == "LEFT_RECURSION") true else round == 0 }
        val src = remember { (if (useLR) surgeryLR else surgeryLF).let { it[ctx.rnd.nextInt(it.size)] } }
        SurgeryRound(ctx, Grammar.parse(src), useLR, done)
    }
}

@Composable
private fun SurgeryRound(ctx: GameCtx, g: Grammar, leftRec: Boolean, done: (Double) -> Unit) {
    val target = remember(g) { if (leftRec) GrammarTransform.eliminateImmediateLeftRecursion(g) else GrammarTransform.leftFactor(g) }
    // só as produções que mudaram viram "esqueleto"
    val unchanged = remember(g) { target.productions.filter { tp -> g.productions.any { it.lhs == tp.lhs && it.rhs == tp.rhs } } }
    val skeleton = remember(g) { target.productions.filter { it !in unchanged } }
    val slots = remember(g) { skeleton.map { p -> mutableStateListOf<String?>().apply { repeat(p.rhs.size.coerceAtLeast(1)) { add(null) } } } }
    val pieces = remember(g) {
        val all = skeleton.flatMap { if (it.rhs.isEmpty()) listOf(EPS) else it.rhs }
        val distract = (g.terminals + g.nonTerminals).shuffled(ctx.rnd).take(2)
        (all + distract).shuffled(ctx.rnd).mapIndexed { i, s -> i to s }
    }
    val used = remember(g) { mutableStateMapOf<Int, Pair<Int, Int>>() } // peça -> (produção, slot)
    var selected by remember(g) { mutableStateOf<Int?>(null) }
    var result by remember(g) { mutableStateOf<Double?>(null) }

    Text(if (leftRec) "🩺 Paciente com RECURSÃO À ESQUERDA" else "🩺 Paciente precisando de FATORAÇÃO", style = MaterialTheme.typography.titleSmall)
    PaperCard(color = Palette.red.copy(alpha = 0.06f), border = Palette.red) { GrammarText(g) }
    HintBox(
        ctx,
        if (leftRec) "Receita: A → A α | β  vira  A → β A'  e  A' → α A' | ε" else "Receita: A → α β1 | α β2  vira  A → α A'  e  A' → β1 | β2",
        "Comece pelos espaços da produção principal: o que vem primeiro nela?",
    )
    Text("Gramática curada (preencha os espaços):", style = MaterialTheme.typography.titleSmall)
    if (unchanged.isNotEmpty()) for (p in unchanged) Mono("${p.lhs} → ${p.rhsText()}", color = Palette.inkSoft)
    skeleton.forEachIndexed { pi, p ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Mono("${p.lhs} → ", bold = true, color = Palette.nonTerminal, size = 16)
            slots[pi].forEachIndexed { si, v ->
                val ok = result != null
                Box(
                    Modifier.padding(2.dp).width(52.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (v == null) Palette.paperDark else Palette.white)
                        .border(2.dp, if (ok) (if (correctSlot(skeleton, slots, pi)) Palette.green else Palette.red) else Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(enabled = result == null) {
                            val sel = selected
                            if (v != null) {
                                // devolve a peça
                                used.entries.firstOrNull { it.value == (pi to si) }?.let { used.remove(it.key) }
                                slots[pi][si] = null
                            } else if (sel != null) {
                                slots[pi][si] = pieces.first { it.first == sel }.second
                                used[sel] = pi to si
                                selected = null
                                Sound.play(Sfx.CLICK)
                            }
                        }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(v ?: "＿", fontFamily = LocalFonts.current.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }
        }
    }
    Text("Peças:", style = MaterialTheme.typography.labelMedium)
    ChipRow {
        for ((id, s) in pieces) if (id !in used) Chip(s, selected = selected == id, mono = true, color = Palette.purple, enabled = result == null) { selected = if (selected == id) null else id }
    }
    if (result == null) BigButton("Operar ✔", Modifier.fillMaxWidth(), color = Palette.teal, enabled = slots.all { s -> s.all { it != null } }) {
        val good = skeleton.indices.count { correctSlot(skeleton, slots, it) }
        result = good.toDouble() / skeleton.size
        if (result == 1.0) Juice.correct() else Juice.wrong()
    } else {
        FeedbackBanner(result == 1.0, if (result == 1.0) "Cirurgia perfeita! A nova gramática gera a MESMA linguagem, sem o problema." else "Quase! A versão certa é:")
        if (result != 1.0) PaperCard(color = Palette.green.copy(alpha = 0.08f), border = Palette.green) { GrammarText(target) }
        val ll = LL1Table(target).isLL1
        Text(if (ll) "E agora ela é LL(1) ✅" else "Ainda não é LL(1), mas melhorou.", style = MaterialTheme.typography.bodySmall)
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done(result!!) }
    }
}

/** A produção pi está certa se o conteúdo bate com ALGUMA alternativa do mesmo lado esquerdo (ordem das alternativas não importa). */
private fun correctSlot(skeleton: List<Production>, slots: List<List<String?>>, pi: Int): Boolean {
    val lhs = skeleton[pi].lhs
    val mine = slots[pi].map { it ?: "?" }.let { if (it == listOf(EPS)) emptyList() else it }
    val sameLhs = skeleton.indices.filter { skeleton[it].lhs == lhs }
    val mineAll = sameLhs.map { i -> slots[i].map { it ?: "?" }.let { if (it == listOf(EPS)) emptyList() else it } }.sortedBy { it.joinToString() }
    val truthAll = sameLhs.map { skeleton[it].rhs }.sortedBy { it.joinToString() }
    return mine in truthAll && mineAll == truthAll
}

// ============================================================ Alquimia FIRST/FOLLOW

@Composable
fun AlchemyGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done ->
        val g = remember {
            if (ctx.level >= 4 && ctx.rnd.nextBoolean()) GrammarBank.exprLL1.grammar
            else GrammarGenerator.random(ctx.rnd, nts = if (ctx.level <= 1) 2 else if (ctx.level <= 3) 3 else 4, allowEpsilon = ctx.level >= 2)
        }
        AlchemyRound(ctx, g, withFollow = ctx.level >= 2 || ctx.skill.name == "FOLLOW", done)
    }
}

@Composable
private fun AlchemyRound(ctx: GameCtx, g: Grammar, withFollow: Boolean, done: (Double) -> Unit) {
    val ff = remember(g) { FirstFollow(g) }
    val sets = remember(g) { mutableStateMapOf<String, Set<String>>() } // "F:A" ou "W:A"
    var active by remember(g) { mutableStateOf("F:${g.start}") }
    var checked by remember(g) { mutableStateOf(false) }
    val keys = g.nonTerminals.map { "F:$it" } + if (withFollow) g.nonTerminals.map { "W:$it" } else emptyList()
    fun truth(k: String): Set<String> = if (k.startsWith("F:")) ff.first.getValue(k.drop(2)) else ff.follow.getValue(k.drop(2))

    GrammarText(g)
    HintBox(
        ctx,
        "FIRST(A): olhe o INÍCIO de cada produção de A. Terminal entra direto; não-terminal → copie o FIRST dele (e, se ele pode sumir, olhe o próximo).",
        "FOLLOW: procure A no LADO DIREITO das produções e veja quem vem logo depois. O inicial sempre tem $. Se A está no fim, herda o FOLLOW do lado esquerdo.",
        "Anuláveis (podem virar ε): " + ff.nullable.ifEmpty { setOf("nenhum") }.joinToString(),
    )
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("", header = true, w = 48); Cell("⚗️ FIRST", header = true, w = 140); if (withFollow) Cell("🧪 FOLLOW", header = true, w = 140) }
        for (nt in g.nonTerminals) Row {
            Cell(nt, header = true, w = 48)
            for (k in listOfNotNull("F:$nt", if (withFollow) "W:$nt" else null)) {
                val mine = sets[k].orEmpty()
                val ok = mine == truth(k)
                Cell(
                    "{ " + mine.joinToString(", ") + " }" + if (checked && !ok) "\n✔ { " + truth(k).joinToString(", ") + " }" else "",
                    w = 140,
                    color = when {
                        checked -> if (ok) Palette.green.copy(alpha = 0.25f) else Palette.red.copy(alpha = 0.2f)
                        k == active -> Palette.yellow.copy(alpha = 0.6f)
                        else -> null
                    },
                    onClick = { if (!checked) active = k },
                )
            }
        }
    }
    if (!checked) {
        Text("Ingredientes para ${if (active.startsWith("F")) "FIRST" else "FOLLOW"}(${active.drop(2)}):", style = MaterialTheme.typography.labelMedium)
        val ingredients = g.terminals + if (active.startsWith("F")) listOf(EPS) else listOf(EOF)
        ChipRow {
            for (t in ingredients) {
                val on = t in sets[active].orEmpty()
                Chip(t, selected = on, mono = true, color = Palette.teal) {
                    sets[active] = if (on) sets[active].orEmpty() - t else sets[active].orEmpty() + t
                }
            }
        }
        BigButton("Destilar ✔", Modifier.fillMaxWidth(), color = Palette.teal) { checked = true; if (keys.all { sets[it].orEmpty() == truth(it) }) Juice.correct() else Juice.wrong() }
    } else {
        val score = keys.count { sets[it].orEmpty() == truth(it) }.toDouble() / keys.size
        FeedbackBanner(score == 1.0, if (score == 1.0) "Poção perfeita! Todos os conjuntos certos." else "${(score * 100).toInt()}% dos caldeirões certos. As respostas corretas estão marcadas com ✔.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done(score) }
    }
}

// ============================================================ Sudoku LL(1)

@Composable
fun LL1SudokuGame(ctx: GameCtx) {
    val g = remember {
        when {
            ctx.level <= 1 -> GrammarBank.decl.grammar
            ctx.level == 2 -> listOf(GrammarBank.exprLL1.grammar, GrammarBank.parens.grammar, GrammarBank.anbn.grammar).random(ctx.rnd)
            ctx.level == 3 -> GrammarGenerator.random(ctx.rnd, 3, true, requireLL1 = true)
            else -> listOf(GrammarBank.stmts.grammar, GrammarBank.exprLeftRec.grammar, GrammarGenerator.random(ctx.rnd, 3, true, requireLL1 = false)).random(ctx.rnd)
        }
    }
    val t = remember { LL1Table(g) }
    val mine = remember { mutableStateMapOf<Pair<String, String>, Set<Int>>() }
    var sel by remember { mutableStateOf<Pair<String, String>?>(null) }
    var checked by remember { mutableStateOf(false) }
    var verdictLL1 by remember { mutableStateOf<Boolean?>(null) }

    GrammarText(g)
    HintBox(
        ctx,
        "Para cada A → α: coloque-a em M[A, a] para todo a em FIRST(α).",
        "Se α pode virar ε, coloque também em M[A, b] para todo b em FOLLOW(A).",
        "FIRST/FOLLOW: " + t.grammar.nonTerminals.joinToString("  ") { "$it: F{${t.ff.first.getValue(it).joinToString(",")}} W{${t.ff.follow.getValue(it).joinToString(",")}}" },
    )
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("M", header = true, w = 48); for (c in t.columns) Cell(c, header = true, w = 86) }
        for (nt in g.nonTerminals) Row {
            Cell(nt, header = true, w = 48)
            for (c in t.columns) {
                val key = nt to c
                val ids = mine[key].orEmpty()
                val truthIds = t.cell(nt, c).map { it.id }.toSet()
                Cell(
                    ids.joinToString("\n") { "→" + g.productions[it].rhsText() },
                    w = 86,
                    color = when {
                        checked && ids == truthIds -> if (ids.isEmpty()) null else Palette.green.copy(alpha = 0.25f)
                        checked -> Palette.red.copy(alpha = 0.25f)
                        key == sel -> Palette.yellow.copy(alpha = 0.6f)
                        else -> null
                    },
                    onClick = { if (!checked) sel = key },
                )
            }
        }
    }
    val s = sel
    if (!checked && s != null) {
        Text("M[${s.first}, ${s.second}] =", style = MaterialTheme.typography.labelMedium)
        ChipRow {
            for (p in g.productionsOf(s.first)) {
                val on = p.id in mine[s].orEmpty()
                Chip("${p.lhs}→${p.rhsText()}", selected = on, mono = true, color = Palette.purple) {
                    mine[s] = if (on) mine[s].orEmpty() - p.id else mine[s].orEmpty() + p.id
                }
            }
        }
    } else if (!checked) Text("Toque numa célula para preenchê-la.", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)

    if (!checked) {
        Text("E então, a gramática é LL(1)?", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Sim, é LL(1)", selected = verdictLL1 == true, color = Palette.green) { verdictLL1 = true }
            Chip("Não: tem conflito", selected = verdictLL1 == false, color = Palette.red) { verdictLL1 = false }
        }
        BigButton("Conferir ✔", Modifier.fillMaxWidth(), color = Palette.teal, enabled = verdictLL1 != null) { checked = true }
    } else {
        val relevant = (t.filledCells().keys + mine.filterValues { it.isNotEmpty() }.keys).toSet()
        val good = relevant.count { k -> mine[k].orEmpty() == t.cell(k.first, k.second).map { it.id }.toSet() }
        val cellScore = if (relevant.isEmpty()) 1.0 else good.toDouble() / relevant.size
        val verdictOk = verdictLL1 == t.isLL1
        FeedbackBanner(cellScore == 1.0 && verdictOk,
            "Células: $good/${relevant.size}. " + (if (verdictOk) "Veredito certo: " else "Veredito errado: ") +
                (if (t.isLL1) "nenhuma célula tem duas produções → LL(1)." else "há células com 2+ produções → NÃO é LL(1)."))
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish(0.8 * cellScore + 0.2 * (if (verdictOk) 1.0 else 0.0)) }
    }
}

// ============================================================ Torre da Pilha

@Composable
fun StackTowerGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done ->
        val g = remember {
            when {
                ctx.level <= 1 -> GrammarBank.anbn.grammar
                ctx.level == 2 -> listOf(GrammarBank.parens.grammar, GrammarBank.decl.grammar).random(ctx.rnd)
                else -> GrammarBank.exprLL1.grammar
            }
        }
        val input = remember {
            val s = SentenceGenerator.generate(g, ctx.rnd, maxDepth = 2 + ctx.level, maxTokens = 2 + ctx.level * 2) ?: listOf("id")
            if (ctx.level >= 4 && ctx.rnd.nextInt(3) == 0) SentenceGenerator.mutate(g, s, ctx.rnd) else s
        }
        StackTowerRound(ctx, g, input, done)
    }
}

@Composable
private fun StackTowerRound(ctx: GameCtx, g: Grammar, input: List<String>, done: (Double) -> Unit) {
    val t = remember(g) { LL1Table(g) }
    val trace = remember(g, input) { LL1Parser.trace(t, input) }
    var step by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var stepMistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val cur = trace.getOrNull(step)
    val finished = cur == null || (step > 0 && trace[step - 1].action.let { it is LLAction.Accept || it is LLAction.Error })

    GrammarText(g)
    if (finished) {
        val last = trace.last().action
        FeedbackBanner(true, if (last is LLAction.Accept) "Aceito! A pilha esvaziou junto com a entrada." else "Erro detectado corretamente: a entrada NÃO pertence à linguagem.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    cur!!
    BeltStage(cur.stack, cur.input)
    Text("Topo: ${cur.stack.last()} · Próximo: ${cur.input.first()}", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, "Topo terminal igual ao token → casar. Topo não-terminal → olhe M[topo, token].",
        "M[${cur.stack.last()}, ${cur.input.first()}] = " + (if (g.isNonTerminal(cur.stack.last())) t.cell(cur.stack.last(), cur.input.first()).joinToString().ifEmpty { "vazio" } else "não se aplica: o topo é terminal"))
    msg?.let { FeedbackBanner(it.first, it.second) }
    fun choose(a: LLAction) {
        val right = cur.action
        val ok = when {
            a is LLAction.Error && right is LLAction.Error -> true
            else -> a == right
        }
        if (ok) { step++; stepMistakes = 0; msg = true to right.label(); Juice.correct() }
        else {
            mistakes++; stepMistakes++; Juice.wrong()
            msg = if (stepMistakes >= 2) { step++; stepMistakes = 0; false to "A ação certa era: ${right.label()} (já apliquei para você)." }
            else false to "Hmm, não. Olhe o topo da pilha e o próximo token de novo."
        }
    }
    val top = cur.stack.last()
    ChipRow {
        if (!g.isNonTerminal(top) && top != EOF) Chip("✅ casar '$top'", color = Palette.teal) { choose(LLAction.Match(top)) }
        if (g.isNonTerminal(top)) for (p in g.productionsOf(top)) Chip("expandir $top→${p.rhsText()}", mono = true, color = Palette.purple) { choose(LLAction.Expand(p)) }
        Chip("🏁 aceitar", color = Palette.green) { choose(LLAction.Accept) }
        Chip("🚨 erro", color = Palette.red) { choose(LLAction.Error("")) }
    }
}
