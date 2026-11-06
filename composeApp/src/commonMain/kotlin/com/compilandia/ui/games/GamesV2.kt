package com.compilandia.ui.games

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
import androidx.compose.foundation.layout.height
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
import com.compilandia.engine.ExprParser
import com.compilandia.engine.FlexChallenges
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.LL1Table
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.Mips
import com.compilandia.engine.PanicMode
import com.compilandia.engine.RecursiveDescentGen
import com.compilandia.engine.RtEvent
import com.compilandia.engine.RuntimeSim
import com.compilandia.engine.SentenceGenerator
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.TacGen
import com.compilandia.engine.setText
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.AutomatonView
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.visuals.Cell
import com.compilandia.ui.visuals.FrameStack
import com.compilandia.ui.visuals.dfaLetter
import com.compilandia.ui.visuals.nfaEdges
import com.compilandia.textos.tx

// ============================================================ Fábrica de Subconjuntos

@Composable
fun SubsetFactoryGame(ctx: GameCtx) {
    val nfa = remember {
        val ids = when { ctx.level <= 1 -> listOf("ab", "abb"); ctx.level == 2 -> listOf("aa", "pen", "ab"); else -> listOf("abc", "eps", "ou", "abb") }
        val pick = ids[ctx.rnd.nextInt(ids.size)]
        SmallNFA.bank.first { it.id == pick }
    }
    val res = remember { nfa.subsetConstruction() }
    val steps = remember { res.steps.take(4 + ctx.level) }
    var k by remember { mutableIntStateOf(0) }
    val picked = remember { mutableStateListOf<Int>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var revealed by remember { mutableStateOf(false) }

    Mono(tx("comum_afn_1", nfa.title), bold = true)
    val cur = steps.getOrNull(k)
    AutomatonView(nfa.stateCount, nfa.start, nfa.accepting, nfaEdges(nfa), highlight = picked.toSet()) { s ->
        if (cur != null && !revealed) { if (s in picked) picked.remove(s) else picked.add(s); Sound.play(Sfx.CLICK) }
    }
    // tabela até aqui
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell(tx("comum_afd"), header = true, w = 52); Cell(tx("comum_kt_conjunto"), header = true, w = 92); for (c in nfa.alphabet) Cell("$c", header = true, w = 52) }
        val done = steps.take(k)
        val known = res.states.filter { st -> st == res.states[0] || done.any { it.to == st } }
        for (st in known) Row {
            val idx = res.states.indexOf(st)
            Cell((if (idx in res.accepting) "*" else "") + dfaLetter(idx), header = true, w = 52)
            Cell(setText(st), w = 92)
            for (c in nfa.alphabet) {
                val s = done.firstOrNull { it.from == st && it.symbol == c }
                Cell(s?.let { if (it.to.isEmpty()) "∅" else dfaLetter(res.states.indexOf(it.to)) } ?: if (cur?.from == st && cur.symbol == c) "❓" else "", w = 52,
                    color = if (cur?.from == st && cur.symbol == c) Palette.yellow.copy(alpha = 0.6f) else null)
            }
        }
    }
    if (cur == null) {
        FeedbackBanner(mistakes == 0, tx("gamesv2_fabrica_concluida_cada_estado_afd", res.states.size))
        BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val fromName = dfaLetter(res.states.indexOf(cur.from))
    RichText(tx("gamesv2_calcule_fecho_move_1_2", fromName, setText(cur.from), cur.symbol), style = MaterialTheme.typography.titleSmall)
    Text(tx("gamesv2_selecionados_1", setText(picked.sorted().toCollection(LinkedHashSet()))), style = MaterialTheme.typography.bodyMedium)
    HintBox(ctx, tx("gamesv2_primeiro_move_cada_estado_1", fromName, cur.symbol, setText(cur.moved)), tx("gamesv2_depois_fecho_siga_tambem_todas"))
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (!revealed) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton(tx("gamesv2_nenhum"), small = true, color = Palette.white, textColor = Palette.ink) { picked.clear() }
        BigButton(tx("gamesv2_confirmar"), Modifier.weight(1f), small = true, color = Palette.teal) {
            val ok = picked.toSet() == cur.to
            if (!ok) mistakes++
            msg = ok to (if (ok) tx("gamesv2_isso") else "Era ${setText(cur.to)}. ") + tx("gamesv2_move_1_depois_fecho_2", setText(cur.moved), setText(cur.to))
            if (ok) Juice.correct() else Juice.wrong()
            revealed = true
        }
    } else BigButton(tx("gamesv2_proxima_transicao"), Modifier.fillMaxWidth()) { k++; picked.clear(); revealed = false; msg = null }
}

// ============================================================ Oficina Flex

@Composable
fun FlexWorkshopGame(ctx: GameCtx) {
    val ch = remember { FlexChallenges.all[((ctx.level - 1) + ctx.rnd.nextInt(2)).coerceIn(0, FlexChallenges.all.lastIndex)] }
    val rules = remember { mutableStateListOf<MiniFlex.Rule>().apply { addAll(ch.rules) } }
    var runs by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Pair<Boolean, MiniFlex.Result>?>(null) }
    PaperCard(color = Palette.paper, border = Palette.green) {
        Text("🦎 ${ch.title}", style = MaterialTheme.typography.headlineSmall)
        RichText(ch.story, style = MaterialTheme.typography.bodyMedium)
        Mono(tx("comum_entrada_1", ch.input.replace("\n", "⏎")), bold = true)
        Mono(tx("gamesv2_esperado_1", ch.expected.joinToString(" ")), color = Palette.teal)
    }
    HintBox(ctx, ch.hint, "Lembre: 1) maior casamento; 2) empate → regra de CIMA.")
    Text(tx("gamesv2_scanner_l_regras_cima_para"), style = MaterialTheme.typography.titleSmall)
    rules.forEachIndexed { i, r ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.codeBg).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${i + 1}", color = Palette.yellow, fontFamily = LocalFonts.current.mono, modifier = Modifier.width(20.dp))
            Text(r.display(), color = Color(0xFFE8E6F0), fontFamily = LocalFonts.current.mono, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("▲", color = Palette.onAccent, fontSize = 18.sp, modifier = Modifier.padding(4.dp).clickable(enabled = i > 0) { val x = rules.removeAt(i); rules.add(i - 1, x); Sound.play(Sfx.CLICK) })
            Text("▼", color = Palette.onAccent, fontSize = 18.sp, modifier = Modifier.padding(4.dp).clickable(enabled = i < rules.lastIndex) { val x = rules.removeAt(i); rules.add(i + 1, x); Sound.play(Sfx.CLICK) })
            Text("✖", color = Palette.red, fontSize = 16.sp, modifier = Modifier.padding(4.dp).clickable { rules.removeAt(i); Sound.play(Sfx.CLICK) })
        }
    }
    if (ch.extra.isNotEmpty()) {
        Text(tx("gamesv2_regras_disponiveis_para_adicionar_vao"), style = MaterialTheme.typography.labelMedium)
        ChipRow { for (r in ch.extra) if (r !in rules) Chip(r.display(), mono = true, color = Palette.green) { rules.add(r) } }
    }
    BigButton(tx("gamesv2_flex_scanner_l_out"), Modifier.fillMaxWidth(), color = Palette.ink) {
        runs++
        result = FlexChallenges.check(ch, rules.toList())
        if (result!!.first) Juice.levelUp() else Juice.wrong()
    }
    result?.let { (ok, r) ->
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
            Text(tx("gamesv2_saida") + r.tokens.joinToString(" ") { it.token } + (r.error?.let { "  ✘ $it" } ?: ""), color = if (ok) Palette.green else Palette.red, fontFamily = LocalFonts.current.mono, fontSize = 12.sp)
            Text(tx("gamesv2_lexemas") + r.tokens.joinToString(" ") { "\"${it.text}\"" }, color = Color(0xFF9CA3AF), fontFamily = LocalFonts.current.mono, fontSize = 11.sp)
        }
        if (ok) {
            FeedbackBanner(true, tx("gamesv2_scanner_certo_em_1_execucao", runs))
            BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * (runs - 1)).coerceIn(0.4, 1.0)) }
        } else if (runs >= 4) BigButton(tx("gamesv2_desistir_ver_dica_encerrar"), Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) { ctx.finish(0.2) }
    }
}

// ============================================================ Sala do Pânico

private val panicGrammar = Grammar.parse("P -> S ; P | ε\nS -> id = E | print E\nE -> T E'\nE' -> + T E' | ε\nT -> id | num")

@Composable
fun PanicRoomGame(ctx: GameCtx) {
    Rounds(ctx, 2) { _, done -> PanicRound(ctx, done) }
}

@Composable
private fun PanicRound(ctx: GameCtx, done: (Double) -> Unit) {
    val t = remember { LL1Table(panicGrammar) }
    val input = remember {
        var res: List<String>? = null
        repeat(50) {
            if (res == null) {
                val s = SentenceGenerator.generate(panicGrammar, ctx.rnd, maxDepth = 6, maxTokens = 10 + ctx.level) ?: return@repeat
                if (s.size < 4) return@repeat
                val m = SentenceGenerator.mutate(panicGrammar, s, ctx.rnd)
                if (PanicMode.firstErrorPosition(t, m) != null) res = m
            }
        }
        res ?: listOf("id", "=", "=", "num", ";", "id", "=", "num", ";")
    }
    val errPos = remember { PanicMode.firstErrorPosition(t, input)!! }
    val trace = remember { PanicMode.trace(t, input) }
    val ev = trace.events.first()
    var phase by remember { mutableIntStateOf(0) }
    var tapped by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0.0) }

    GrammarText(panicGrammar)
    Text(if (phase == 0) tx("gamesv2_em_qual_token_parser_ll") else tx("gamesv2_recuperacao"), style = MaterialTheme.typography.titleSmall)
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        (input + "$").forEachIndexed { k, s ->
            val bg = when {
                phase > 0 && k == errPos -> Palette.red.copy(alpha = 0.35f)
                phase > 1 && k < ev.position && k >= errPos && s in ev.skipped -> Palette.paperDark
                phase > 1 && k == ev.position -> Palette.green.copy(alpha = 0.35f)
                k == tapped -> Palette.yellow
                else -> Palette.white
            }
            Box(
                Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp)).background(bg).border(1.dp, Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable(enabled = phase == 0 || phase == 1) { tapped = k; Sound.play(Sfx.CLICK) }.padding(horizontal = 8.dp, vertical = 6.dp)
            ) { Mono(s, bold = true, size = 15) }
        }
    }
    HintBox(ctx, tx("gamesv2_ll_1_le_esquerda_para"), tx("gamesv2_no_modo_panico_parser_joga"))
    when (phase) {
        0 -> BigButton(tx("gamesv2_este"), Modifier.fillMaxWidth(), color = Palette.red, enabled = tapped != null) {
            val ok = tapped == errPos
            score += if (ok) 0.5 else 0.0
            if (ok) Juice.correct() else Juice.wrong()
            tapped = null; phase = 1
        }
        1 -> {
            FeedbackBanner(true, tx("gamesv2_erro_detectado_em_1_token", input[errPos], errPos + 1))
            RichText(tx("gamesv2_agora_modo_panico_toque_no"), style = MaterialTheme.typography.bodyMedium)
            BigButton(tx("gamesv2_volta_aqui"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = tapped != null) {
                val ok = tapped == ev.position
                score += if (ok) 0.5 else 0.0
                if (ok) Juice.correct() else Juice.wrong()
                phase = 2
            }
        }
        else -> {
            for (e in trace.events) RichText("• ${e.message}", style = MaterialTheme.typography.bodySmall)
            FeedbackBanner(score >= 0.99, tx("gamesv2_em_vez_parar_no_primeiro"))
            BigButton(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { done(score) }
        }
    }
}

// ============================================================ Pilha de Chamadas

@Composable
fun CallStackGame(ctx: GameCtx) {
    val p = remember { RuntimeSim.forLevel(ctx.level, ctx.rnd.nextInt(10)) }
    var i by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    CodeBlock(p.code, p.title)
    if (i >= p.events.size) {
        FeedbackBanner(mistakes == 0, tx("gamesv2_programa_terminou_com_1_pico", p.result, p.events.maxOf { it.stack.size }))
        BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val before = if (i == 0) emptyList() else p.events[i - 1].stack
    FrameStack(before)
    val right = p.events[i]
    fun label(e: RtEvent) = when (e) {
        is RtEvent.Call -> tx("gamesv2_chamar_1_2", e.frame.fn, e.frame.args.values.joinToString())
        is RtEvent.Return -> tx("gamesv2_1_retorna_2", e.fn, e.value)
    }
    val options = remember(i) {
        val top = before.lastOrNull()
        val distract = mutableListOf<String>()
        when (right) {
            is RtEvent.Call -> {
                val a = right.frame.args.values.firstOrNull()
                if (a != null) distract += tx("gamesv2_chamar_1_2", right.frame.fn, a + 1)
                if (top != null) distract += tx("gamesv2_1_retorna_2", top.fn, (top.args.values.firstOrNull() ?: 1))
            }
            is RtEvent.Return -> {
                distract += tx("gamesv2_1_retorna_2", right.fn, right.value + 1)
                distract += if (right.value != 0) tx("gamesv2_1_retorna_2", right.fn, right.value * 2) else tx("gamesv2_chamar_1_0", right.fn)
            }
        }
        (distract.distinct().filter { it != label(right) }.take(2) + label(right)).shuffled(ctx.rnd)
    }
    Text(tx("gamesv2_que_acontece_agora"), style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, tx("gamesv2_olhe_registro_topo_em_qual"), "fat(n) = n * fat(n-1); fat(1) = 1.")
    msg?.let { FeedbackBanner(it.first, it.second) }
    for (o in options) BigButton(o, Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) {
        if (o == label(right)) { msg = true to tx("gamesv2_isso") + o; i++; Juice.correct() }
        else { mistakes++; msg = false to tx("gamesv2_nao_pense_no_registro_topo"); Juice.wrong() }
    }
}

// ============================================================ Montadora MIPS

@Composable
fun MipsForgeGame(ctx: GameCtx) {
    val src = remember {
        val pool = when (ctx.level) {
            1 -> listOf(tx("comum_b_2"), "c * d", tx("gamesv2_1"))
            2 -> listOf(tx("comum_b_c"), tx("comum_b_c_3"), "d - b * 2")
            else -> listOf(tx("comum_b_c_d"), tx("gamesv2_1_b_c"), tx("comum_b_c_d_2"), tx("gamesv2_d_b_2"))
        }
        pool[ctx.rnd.nextInt(pool.size)]
    }
    val tac = remember { TacGen.generate(ExprParser.parse(src), "r") }
    var k by remember { mutableIntStateOf(0) }       // instrução TAC atual
    val built = remember { mutableStateListOf<String>() }
    val currentLines = remember { mutableStateListOf<String>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Mono(tx("gamesv2_r_1_4_b_2", src), bold = true)
    PaperCard(color = Palette.paper) {
        Text("TAC", style = MaterialTheme.typography.labelMedium)
        tac.forEachIndexed { j, ins -> Mono((if (j == k) "▶ " else "  ") + ins, bold = j == k, color = if (j < k) Palette.inkSoft else Palette.ink) }
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        Text(tx("comum_text_main"), fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        for (l in built + currentLines) Text("  $l", fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = Color(0xFFE8E6F0))
        if (k < tac.size) Text("  _", fontFamily = LocalFonts.current.mono, color = Palette.yellow)
    }
    if (k >= tac.size) {
        val st = Mips.run(built.toList(), Mips.intEnv).last()
        FeedbackBanner(true, tx("gamesv2_rodando_no_mars_bolso_r", st.mem["r"]))
        BigButton(tx("comum_concluir"), Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val ins = tac[k]
    val want = remember(k) { Mips.fromTac(listOf(ins)) }
    val palette = remember(k) {
        val wrong = mutableListOf<String>()
        for (w in want) {
            when {
                w.startsWith("lw ") -> wrong += w.replace("lw ", "sw ")
                w.startsWith("sw ") -> wrong += w.replace("sw ", "lw ")
                w.startsWith("add ") -> wrong += w.replace("add ", "sub ")
                w.startsWith("sub ") -> wrong += w.replace("sub ", "add ")
                w.startsWith("mul ") -> wrong += w.replace("mul ", "add ")
                w.startsWith("div ") -> wrong += w.replace("div ", "mul ")
                w.startsWith("li ") -> wrong += w.replace("li ", "lw ")
            }
        }
        (want + wrong.distinct().filter { it !in want }.take(3)).shuffled(ctx.rnd)
    }
    RichText(tx("gamesv2_traduza_1_tijolo_por_vez", ins), style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, tx("gamesv2_padrao_carrega_operandos_em_registradores"), tx("gamesv2_lw_load_word_memoria_registrador"))
    msg?.let { FeedbackBanner(it.first, it.second) }
    val dd = com.compilandia.ui.rememberDragDrop()
    fun tryLine(line: String) {
        val expected = want[currentLines.size]
        if (line == expected) {
            currentLines += line; msg = null; Juice.pick()
            if (currentLines.size == want.size) {
                built += currentLines; currentLines.clear(); k++; msg = true to tx("gamesv2_instrucao_traduzida"); Juice.correct()
            }
        } else {
            mistakes++; Juice.wrong()
            msg = false to when {
                line.startsWith("sw") && expected.startsWith("lw") -> tx("gamesv2_primeiro_precisa_carregar_valores_lw")
                line.startsWith("lw") && expected.startsWith("sw") -> tx("gamesv2_conta_ja_esta_no_registrador")
                else -> tx("gamesv2_nao_essa_ainda_siga_carregar")
            }
        }
    }
    com.compilandia.ui.DropZone(dd, "cpu", Modifier.fillMaxWidth()) { hovered ->
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).background(if (hovered) Palette.yellow.copy(alpha = 0.5f) else Palette.paperDark)
                .border(if (hovered) 4.dp else 2.dp, if (hovered) Palette.orange else Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(tx("gamesv2_solte_tijolo_aqui_1_2", currentLines.size + 1, want.size), fontWeight = FontWeight.Bold, color = Palette.ink) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (line in palette) if (line !in currentLines) {
            com.compilandia.ui.Draggable(dd, line, Modifier.fillMaxWidth(), onTap = { tryLine(line) }, onDrop = { tryLine(line) }) {
                Box(Modifier.fillMaxWidth()) { com.compilandia.ui.Tile("⠿  $line", Palette.orange.copy(alpha = 0.18f), big = true) }
            }
        }
    }
}

// ============================================================ Programador Descendente

@Composable
fun DescentCoderGame(ctx: GameCtx) {
    val g = remember {
        when (ctx.level) {
            1 -> GrammarBank.anbn.grammar
            2 -> listOf(GrammarBank.decl.grammar, GrammarBank.parens.grammar).random(ctx.rnd)
            else -> GrammarBank.exprLL1.grammar
        }
    }
    val t = remember { LL1Table(g) }
    val targets = remember { g.nonTerminals.sortedByDescending { g.productionsOf(it).size }.take(2).shuffled(ctx.rnd) }
    Rounds(ctx, targets.size) { round, done -> DescentRound(ctx, g, t, targets[round], done) }
}

@Composable
private fun DescentRound(ctx: GameCtx, g: Grammar, t: LL1Table, nt: String, done: (Double) -> Unit) {
    val code = remember { RecursiveDescentGen.function(t, nt) }
    val head = code.first(); val tail = code.last()
    val middle = remember { code.subList(1, code.size - 1) }
    val shuffled = remember { middle.withIndex().toList().shuffled(ctx.rnd) }
    val placed = remember { mutableStateListOf<Int>() }   // índices em middle
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<String?>(null) }
    GrammarText(g)
    RichText(tx("gamesv2_escreva_metodo_1_parser_descendente", nt), style = MaterialTheme.typography.titleSmall)
    HintBox(ctx,
        tx("gamesv2_producao_vira_sequencia_terminal_casatoken"),
        tx("gamesv2_com_varias_producoes_escolha_pelo"),
        "FIRST: " + g.productionsOf(nt).joinToString("  ") { "${it.rhsText()}: {${(t.ff.firstOfString(it.rhs)).joinToString(",")}}" })
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        val mono = LocalFonts.current.mono
        Text(head.text, fontFamily = mono, fontSize = 13.sp, color = Palette.yellow)
        for (idx in placed) { val l = middle[idx]; Text("    ".repeat(l.indent) + l.text, fontFamily = mono, fontSize = 13.sp, color = Color(0xFFE8E6F0)) }
        if (placed.size < middle.size) Text("    _", fontFamily = mono, color = Palette.yellow)
        else Text(tail.text, fontFamily = mono, fontSize = 13.sp, color = Palette.yellow)
    }
    msg?.let { FeedbackBanner(false, it) }
    if (placed.size < middle.size) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((idx, line) in shuffled) if (idx !in placed) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.white).border(2.dp, Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable {
                            val expectedText = middle[placed.size].text
                            if (line.text == expectedText) {
                                // linhas idênticas são intercambiáveis
                                placed += idx; msg = null; Sound.play(Sfx.CLICK)
                            } else { mistakes++; msg = tx("gamesv2_ainda_nao_essa_linha_pense"); Juice.wrong() }
                        }.padding(10.dp)
                ) { Mono(line.text, bold = true, size = 14) }
            }
        }
    } else {
        FeedbackBanner(true, tx("gamesv2_metodo_pronto_repare_pilha_analisador"))
        BigButton(tx("comum_continuar_2"), Modifier.fillMaxWidth()) { done((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
    }
}

