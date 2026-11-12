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
import androidx.compose.runtime.LaunchedEffect
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
import com.compilandia.textos.tx

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
        Text(tx("gamesgrammar_alvo"), style = MaterialTheme.typography.titleSmall)
        SymbolsText(g, target, size = 17)
    }
    PaperCard(color = Palette.paper) {
        Text(tx("gamesgrammar_forma_sentencial_atual"), style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
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
        Text(tx("gamesgrammar_1_passos_2_becos", history.size, mistakes), style = MaterialTheme.typography.labelSmall, color = Palette.inkSoft)
    }
    HintBox(
        ctx,
        tx("gamesgrammar_olhe_proximo_terminal_alvo_que"),
        tx("gamesgrammar_boas_escolhas_agora") + (if (idx >= 0) DerivationHelper.goodChoices(g, earley, form, target).joinToString(" ou ") { it.rhsText() } else "—"),
    )
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (finished) {
        FeedbackBanner(true, tx("gamesgrammar_derivacao_completa_em_1_passos", history.size))
        TreeView(DerivationHelper.treeOf(g, history.toList()).toUi(g))
        BotaoDoJogo(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { onDone(mistakes, history.toList()) }
    } else if (idx >= 0) {
        Text(tx("gamesgrammar_expandir_1_com", form.symbols[idx]), style = MaterialTheme.typography.titleSmall)
        for (p in g.productionsOf(form.symbols[idx])) {
            BigButton("${p.lhs} → ${p.rhsText()}", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink, small = true) {
                val nf = form.apply(idx, p)
                val wouldRepeat = forbidden != null && nf.leftmostNT(g) < 0 && (history + p) == forbidden
                if (wouldRepeat) {
                    mistakes++; msg = false to tx("gamesgrammar_essa_mesma_derivacao_primeira_arvore"); Juice.wrong()
                } else if (DerivationHelper.viable(g, earley, nf.symbols, target) && nf.symbols.count { !g.isNonTerminal(it) } <= target.size) {
                    form = nf; history += p; msg = null; Sound.play(Sfx.CLICK)
                } else {
                    mistakes++
                    msg = false to tx("gamesgrammar_beco_sem_saida_1_nao", nf.text())
                    Juice.wrong()
                }
            }
        }
        if (history.isNotEmpty()) BigButton(tx("gamesgrammar_desfazer"), small = true, color = Palette.white, textColor = Palette.ink) {
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
            Text(tx("gamesgrammar_arvore_1_2"), style = MaterialTheme.typography.headlineSmall)
            DerivationBoard(ctx, g, target, null) { m, d -> totalMistakes += m; first = d }
        }
        second == null -> {
            Text(tx("gamesgrammar_arvore_2_2_tem_que"), style = MaterialTheme.typography.headlineSmall)
            HintBox(ctx, tx("gamesgrammar_na_primeira_arvore_qual_operador"))
            androidx.compose.runtime.key("second") {
                DerivationBoard(ctx, g, target, first) { m, d -> totalMistakes += m; second = d }
            }
        }
        else -> {
            FeedbackBanner(true, tx("gamesgrammar_duas_derivacoes_mais_esquerda_diferentes"))
            Text(tx("comum_arvore_1"), style = MaterialTheme.typography.titleSmall)
            TreeView(DerivationHelper.treeOf(g, first!!).toUi(g))
            Text(tx("comum_arvore_2"), style = MaterialTheme.typography.titleSmall)
            TreeView(DerivationHelper.treeOf(g, second!!).toUi(g))
            // J8: com números no lugar dos ids, as duas árvores calculam valores diferentes.
            val passos = remember { com.compilandia.engine.Reacoes.duasArvores(DerivationHelper.treeOf(g, first!!), DerivationHelper.treeOf(g, second!!)) }
            Reacao(passos) { ctx.finish((1.0 - 0.1 * totalMistakes).coerceAtLeast(0.0)) }
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

    Text(if (leftRec) tx("gamesgrammar_paciente_com_recursao_esquerda") else tx("gamesgrammar_paciente_precisando_fatoracao"), style = MaterialTheme.typography.titleSmall)
    PaperCard(color = Palette.red.copy(alpha = 0.06f), border = Palette.red) { GrammarText(g) }
    HintBox(
        ctx,
        if (leftRec) tx("gamesgrammar_receita_vira") else tx("gamesgrammar_receita_1_2_vira_1"),
        tx("gamesgrammar_comece_pelos_espacos_producao_principal"),
    )
    Text(tx("gamesgrammar_gramatica_curada_preencha_espacos"), style = MaterialTheme.typography.titleSmall)
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
                ) { Text(v ?: "_", fontFamily = LocalFonts.current.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }
        }
    }
    Text(tx("gamesgrammar_pecas"), style = MaterialTheme.typography.labelMedium)
    ChipRow {
        for ((id, s) in pieces) if (id !in used) Chip(s, selected = selected == id, mono = true, color = Palette.purple, enabled = result == null) { selected = if (selected == id) null else id }
    }
    if (result == null) BotaoDoJogo(tx("gamesgrammar_operar"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = slots.all { s -> s.all { it != null } }) {
        val good = skeleton.indices.count { correctSlot(skeleton, slots, it) }
        result = good.toDouble() / skeleton.size
        if (result == 1.0) Juice.correct() else Juice.wrong()
    } else {
        FeedbackBanner(result == 1.0, if (result == 1.0) tx("gamesgrammar_cirurgia_perfeita_nova_gramatica_gera") else tx("gamesgrammar_quase_versao_certa"))
        if (result != 1.0) PaperCard(color = Palette.green.copy(alpha = 0.08f), border = Palette.green) { GrammarText(target) }
        val ll = LL1Table(target).isLL1
        Text(if (ll) tx("gamesgrammar_agora_ela_ll_1") else tx("gamesgrammar_ainda_nao_ll_1_mas"), style = MaterialTheme.typography.bodySmall)
        // J8: a gramática que você montou vai para o Byte com a pilha e analisa frases da original.
        val passos = remember(g) {
            val minhas = skeleton.indices.map { i -> skeleton[i].lhs to slots[i].map { it ?: "?" }.filter { it != EPS } }
            val operada = com.compilandia.engine.Reacoes.montaGramatica(target.start, unchanged, minhas)
            val rnd = kotlin.random.Random(g.productions.size * 31L + skeleton.size)
            val frases = (0 until 12).mapNotNull { com.compilandia.engine.SentenceGenerator.generate(g, rnd, maxTokens = 7) }.distinct().sortedBy { it.size }
            if (operada == null) emptyList() else com.compilandia.engine.Reacoes.cirurgia(g, operada, listOfNotNull(frases.firstOrNull(), frases.getOrNull(frases.size / 2), frases.lastOrNull()).distinct())
        }
        Reacao(passos) { done(result!!) }
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
        tx("gamesgrammar_first_olhe_inicio_cada_producao"),
        tx("gamesgrammar_follow_procure_no_lado_direito"),
        tx("gamesgrammar_anulaveis_podem_virar") + ff.nullable.ifEmpty { setOf(tx("lessonspart2a_nenhum")) }.joinToString(),
    )
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("", header = true, w = 48); Cell("FIRST", header = true, w = 140); if (withFollow) Cell("FOLLOW", header = true, w = 140) }
        for (nt in g.nonTerminals) Row {
            Cell(nt, header = true, w = 48)
            for (k in listOfNotNull("F:$nt", if (withFollow) "W:$nt" else null)) {
                val mine = sets[k].orEmpty()
                val ok = mine == truth(k)
                Cell(
                    "{ " + mine.joinToString(", ") + " }" + if (checked && !ok) "\n→ { " + truth(k).joinToString(", ") + " }" else "",
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
        Text(tx("gamesgrammar_ingredientes_para_1_2", if (active.startsWith("F")) "FIRST" else "FOLLOW", active.drop(2)), style = MaterialTheme.typography.labelMedium)
        val ingredients = g.terminals + if (active.startsWith("F")) listOf(EPS) else listOf(EOF)
        ChipRow {
            for (t in ingredients) {
                val on = t in sets[active].orEmpty()
                Chip(t, selected = on, mono = true, color = Palette.teal) {
                    sets[active] = if (on) sets[active].orEmpty() - t else sets[active].orEmpty() + t
                }
            }
        }
        BotaoDoJogo(tx("gamesgrammar_destilar"), Modifier.fillMaxWidth(), color = Palette.teal) { checked = true; if (keys.all { sets[it].orEmpty() == truth(it) }) Juice.correct() else Juice.wrong() }
    } else {
        val score = keys.count { sets[it].orEmpty() == truth(it) }.toDouble() / keys.size
        FeedbackBanner(score == 1.0, if (score == 1.0) tx("gamesgrammar_pocao_perfeita_todos_conjuntos_certos") else tx("gamesgrammar_1_dos_caldeiroes_certos_respostas", (score * 100).toInt()))
        BotaoDoJogo(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { done(score) }
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
        tx("gamesgrammar_para_cada_coloque_em_m"),
        tx("gamesgrammar_se_pode_virar_coloque_tambem"),
        "FIRST/FOLLOW: " + t.grammar.nonTerminals.joinToString("  ") { "$it: F{${t.ff.first.getValue(it).joinToString(",")}} W{${t.ff.follow.getValue(it).joinToString(",")}}" },
    )
    // Sem frase de instrução (mural, rodada 10): a primeira célula pisca uma vez e mostra onde tocar.
    val pisca = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        if (com.compilandia.ui.ThemeState.reduceMotion) return@LaunchedEffect
        repeat(2) { pisca.animateTo(0.7f, androidx.compose.animation.core.tween(380)); pisca.animateTo(0f, androidx.compose.animation.core.tween(380)) }
    }
    val primeira = g.nonTerminals.first() to t.columns.first()
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
                        key == primeira && sel == null && pisca.value > 0f -> Palette.yellow.copy(alpha = pisca.value)
                        else -> null
                    },
                    onClick = { if (!checked) sel = key },
                )
            }
        }
    }
    val s = sel
    if (!checked && s != null) {
        Text(tx("gamesgrammar_m_1_2", s.first, s.second), style = MaterialTheme.typography.labelMedium)
        ChipRow {
            for (p in g.productionsOf(s.first)) {
                val on = p.id in mine[s].orEmpty()
                Chip("${p.lhs}→${p.rhsText()}", selected = on, mono = true, color = Palette.purple) {
                    mine[s] = if (on) mine[s].orEmpty() - p.id else mine[s].orEmpty() + p.id
                }
            }
        }
    }

    if (!checked) {
        Text(tx("gamesgrammar_entao_gramatica_ll_1"), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(tx("gamesgrammar_sim_ll_1"), selected = verdictLL1 == true, color = Palette.green) { verdictLL1 = true }
            Chip(tx("gamesgrammar_nao_tem_conflito"), selected = verdictLL1 == false, color = Palette.red) { verdictLL1 = false }
        }
        BotaoDoJogo(tx("comum_conferir_2"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = verdictLL1 != null) { checked = true }
    } else {
        val relevant = (t.filledCells().keys + mine.filterValues { it.isNotEmpty() }.keys).toSet()
        val good = relevant.count { k -> mine[k].orEmpty() == t.cell(k.first, k.second).map { it.id }.toSet() }
        val cellScore = if (relevant.isEmpty()) 1.0 else good.toDouble() / relevant.size
        val verdictOk = verdictLL1 == t.isLL1
        FeedbackBanner(cellScore == 1.0 && verdictOk,
            tx("gamesgrammar_celulas_1_2", good, relevant.size) + (if (verdictOk) tx("gamesgrammar_veredito_certo") else tx("gamesgrammar_veredito_errado")) +
                (if (t.isLL1) tx("gamesgrammar_nenhuma_celula_tem_duas_producoes") else tx("gamesgrammar_ha_celulas_com_2_producoes")))
        BotaoDoJogo(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish(0.8 * cellScore + 0.2 * (if (verdictOk) 1.0 else 0.0)) }
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
        FeedbackBanner(true, if (last is LLAction.Accept) tx("gamesgrammar_aceito_pilha_esvaziou_junto_com") else tx("gamesgrammar_erro_detectado_corretamente_entrada_nao"))
        BotaoDoJogo(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { done((1.0 - 0.12 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    cur!!
    BeltStage(cur.stack, cur.input)
    Text(tx("gamesgrammar_topo_1_proximo_2", cur.stack.last(), cur.input.first()), style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, tx("gamesgrammar_topo_terminal_igual_ao_token"),
        "M[${cur.stack.last()}, ${cur.input.first()}] = " + (if (g.isNonTerminal(cur.stack.last())) t.cell(cur.stack.last(), cur.input.first()).joinToString().ifEmpty { tx("comum_vazio_minusc") } else tx("gamesgrammar_nao_se_aplica_topo_terminal")))
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
            msg = if (stepMistakes >= 2) { step++; stepMistakes = 0; false to tx("gamesgrammar_acao_certa_era_1_ja", right.label()) }
            else false to tx("gamesgrammar_hmm_nao_olhe_topo_pilha")
        }
    }
    val top = cur.stack.last()
    ChipRow {
        if (!g.isNonTerminal(top) && top != EOF) Chip(tx("gamesgrammar_casar_1", top), color = Palette.teal) { choose(LLAction.Match(top)) }
        if (g.isNonTerminal(top)) for (p in g.productionsOf(top)) Chip(tx("gamesgrammar_expandir_1_2", top, p.rhsText()), mono = true, color = Palette.purple) { choose(LLAction.Expand(p)) }
        Chip(tx("gamesgrammar_aceitar"), color = Palette.green) { choose(LLAction.Accept) }
        Chip(tx("gamesgrammar_erro"), color = Palette.red) { choose(LLAction.Error("")) }
    }
}
