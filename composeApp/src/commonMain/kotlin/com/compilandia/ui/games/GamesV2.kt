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

    Mono("AFN: ${nfa.title}", bold = true)
    val cur = steps.getOrNull(k)
    AutomatonView(nfa.stateCount, nfa.start, nfa.accepting, nfaEdges(nfa), highlight = picked.toSet()) { s ->
        if (cur != null && !revealed) { if (s in picked) picked.remove(s) else picked.add(s); Sound.play(Sfx.CLICK) }
    }
    // tabela até aqui
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { Cell("AFD", header = true, w = 52); Cell("conjunto", header = true, w = 92); for (c in nfa.alphabet) Cell("$c", header = true, w = 52) }
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
        FeedbackBanner(mistakes == 0, "Fábrica concluída! Cada estado do AFD é um CONJUNTO de estados do AFN. O AFD completo tem ${res.states.size} estados.")
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.15 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val fromName = dfaLetter(res.states.indexOf(cur.from))
    RichText("Calcule **ε-fecho(move($fromName = ${setText(cur.from)}, '${cur.symbol}'))** — toque nos estados do AFN que formam o resultado.", style = MaterialTheme.typography.titleSmall)
    Text("Selecionados: ${setText(picked.sorted().toCollection(LinkedHashSet()))}", style = MaterialTheme.typography.bodyMedium)
    HintBox(ctx, "Primeiro o MOVE: de cada estado de $fromName, para onde vai uma seta '${cur.symbol}'? = ${setText(cur.moved)}", "Depois o ε-FECHO: siga também todas as setas ε a partir desses estados (e deles mesmos).")
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (!revealed) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton("∅ nenhum", small = true, color = Palette.white, textColor = Palette.ink) { picked.clear() }
        BigButton("Confirmar ✔", Modifier.weight(1f), small = true, color = Palette.teal) {
            val ok = picked.toSet() == cur.to
            if (!ok) mistakes++
            msg = ok to (if (ok) "Isso! " else "Era ${setText(cur.to)}. ") + "move = ${setText(cur.moved)}, depois ε-fecho = ${setText(cur.to)}."
            if (ok) Juice.correct() else Juice.wrong()
            revealed = true
        }
    } else BigButton("Próxima transição ▶", Modifier.fillMaxWidth()) { k++; picked.clear(); revealed = false; msg = null }
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
        Mono("entrada: ${ch.input.replace("\n", "⏎")}", bold = true)
        Mono("esperado: ${ch.expected.joinToString(" ")}", color = Palette.teal)
    }
    HintBox(ctx, ch.hint, "Lembre: 1) maior casamento; 2) empate → regra de CIMA.")
    Text("scanner.l — regras (de cima para baixo):", style = MaterialTheme.typography.titleSmall)
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
        Text("Regras disponíveis para adicionar (vão para o fim):", style = MaterialTheme.typography.labelMedium)
        ChipRow { for (r in ch.extra) if (r !in rules) Chip(r.display(), mono = true, color = Palette.green) { rules.add(r) } }
    }
    BigButton("▶ flex scanner.l && ./a.out", Modifier.fillMaxWidth(), color = Palette.ink) {
        runs++
        result = FlexChallenges.check(ch, rules.toList())
        if (result!!.first) Juice.levelUp() else Juice.wrong()
    }
    result?.let { (ok, r) ->
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
            Text("saída: " + r.tokens.joinToString(" ") { it.token } + (r.error?.let { "  ✘ $it" } ?: ""), color = if (ok) Palette.green else Palette.red, fontFamily = LocalFonts.current.mono, fontSize = 12.sp)
            Text("lexemas: " + r.tokens.joinToString(" ") { "\"${it.text}\"" }, color = Color(0xFF9CA3AF), fontFamily = LocalFonts.current.mono, fontSize = 11.sp)
        }
        if (ok) {
            FeedbackBanner(true, "Scanner certo em $runs execução(ões)!")
            BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * (runs - 1)).coerceIn(0.4, 1.0)) }
        } else if (runs >= 4) BigButton("Desistir (ver dica e encerrar)", Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) { ctx.finish(0.2) }
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
    Text(if (phase == 0) "🚨 Em qual token o parser LL(1) PERCEBE o erro?" else "Recuperação", style = MaterialTheme.typography.titleSmall)
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
    HintBox(ctx, "O LL(1) lê da esquerda para a direita e para no PRIMEIRO token que nenhuma frase válida poderia ter naquela posição.", "No modo pânico, o parser joga tokens fora até achar um sincronizador (um token do FOLLOW do não-terminal, como ';').")
    when (phase) {
        0 -> BigButton("É este! ✔", Modifier.fillMaxWidth(), color = Palette.red, enabled = tapped != null) {
            val ok = tapped == errPos
            score += if (ok) 0.5 else 0.0
            if (ok) Juice.correct() else Juice.wrong()
            tapped = null; phase = 1
        }
        1 -> {
            FeedbackBanner(true, "O erro é detectado em '${input[errPos]}' (token ${errPos + 1}). Propriedade do prefixo viável: tudo antes dele ainda poderia ser um programa válido.")
            RichText("Agora o **modo pânico**: toque no token onde o parser VOLTA a trabalhar normalmente (o sincronizador).", style = MaterialTheme.typography.bodyMedium)
            BigButton("Volta aqui ✔", Modifier.fillMaxWidth(), color = Palette.teal, enabled = tapped != null) {
                val ok = tapped == ev.position
                score += if (ok) 0.5 else 0.0
                if (ok) Juice.correct() else Juice.wrong()
                phase = 2
            }
        }
        else -> {
            for (e in trace.events) RichText("• ${e.message}", style = MaterialTheme.typography.bodySmall)
            FeedbackBanner(score >= 0.99, "Em vez de parar no primeiro erro, o compilador se recupera e continua — assim ele lista TODOS os erros numa compilação só.")
            BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done(score) }
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
        FeedbackBanner(mistakes == 0, "Programa terminou com ${p.result}. Pico da pilha: ${p.events.maxOf { it.stack.size }} registros de ativação.")
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
        return
    }
    val before = if (i == 0) emptyList() else p.events[i - 1].stack
    FrameStack(before)
    val right = p.events[i]
    fun label(e: RtEvent) = when (e) {
        is RtEvent.Call -> "📥 chamar ${e.frame.fn}(${e.frame.args.values.joinToString()})"
        is RtEvent.Return -> "📤 ${e.fn} retorna ${e.value}"
    }
    val options = remember(i) {
        val top = before.lastOrNull()
        val distract = mutableListOf<String>()
        when (right) {
            is RtEvent.Call -> {
                val a = right.frame.args.values.firstOrNull()
                if (a != null) distract += "📥 chamar ${right.frame.fn}(${a + 1})"
                if (top != null) distract += "📤 ${top.fn} retorna ${(top.args.values.firstOrNull() ?: 1)}"
            }
            is RtEvent.Return -> {
                distract += "📤 ${right.fn} retorna ${right.value + 1}"
                distract += if (right.value != 0) "📤 ${right.fn} retorna ${right.value * 2}" else "📥 chamar ${right.fn}(0)"
            }
        }
        (distract.distinct().filter { it != label(right) }.take(2) + label(right)).shuffled(ctx.rnd)
    }
    Text("O que acontece agora?", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, "Olhe o registro do TOPO: em qual linha ele está? Se ele chama alguém, EMPILHA. Se termina, DESEMPILHA e devolve o valor.", "fat(n) = n * fat(n-1); fat(1) = 1.")
    msg?.let { FeedbackBanner(it.first, it.second) }
    for (o in options) BigButton(o, Modifier.fillMaxWidth(), small = true, color = Palette.white, textColor = Palette.ink) {
        if (o == label(right)) { msg = true to "Isso! " + o; i++; Juice.correct() }
        else { mistakes++; msg = false to "Não. Pense no registro do topo da pilha."; Juice.wrong() }
    }
}

// ============================================================ Montadora MIPS

@Composable
fun MipsForgeGame(ctx: GameCtx) {
    val src = remember {
        val pool = when (ctx.level) {
            1 -> listOf("a + b", "c * d", "a - 1")
            2 -> listOf("a + b * c", "( a + b ) * c", "d - b * 2")
            else -> listOf("a * b + c * d", "( a + 1 ) * ( b - c )", "a + b + c + d", "d * ( a - b ) / 2")
        }
        pool[ctx.rnd.nextInt(pool.size)]
    }
    val tac = remember { TacGen.generate(ExprParser.parse(src), "r") }
    var k by remember { mutableIntStateOf(0) }       // instrução TAC atual
    val built = remember { mutableStateListOf<String>() }
    val currentLines = remember { mutableStateListOf<String>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Mono("r = $src     (a=4, b=2, c=3, d=5)", bold = true)
    PaperCard(color = Palette.paper) {
        Text("TAC", style = MaterialTheme.typography.labelMedium)
        tac.forEachIndexed { j, ins -> Mono((if (j == k) "▶ " else "  ") + ins, bold = j == k, color = if (j < k) Palette.inkSoft else Palette.ink) }
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        Text(".text\nmain:", fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        for (l in built + currentLines) Text("  $l", fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = Color(0xFFE8E6F0))
        if (k < tac.size) Text("  _", fontFamily = LocalFonts.current.mono, color = Palette.yellow)
    }
    if (k >= tac.size) {
        val st = Mips.run(built.toList(), Mips.intEnv).last()
        FeedbackBanner(true, "Rodando no \"MARS de bolso\": r = ${st.mem["r"]}. Seu assembly funciona! 🎉")
        BigButton("Concluir ▶", Modifier.fillMaxWidth()) { ctx.finish((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
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
    RichText("Traduza **$ins** (um tijolo por vez, na ordem):", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx, "Padrão: carrega os operandos em registradores (lw ou li para constantes), faz a conta no registrador, guarda o resultado na memória (sw).", "lw = load word (memória → registrador); sw = store word (registrador → memória).")
    msg?.let { FeedbackBanner(it.first, it.second) }
    val dd = com.compilandia.ui.rememberDragDrop()
    fun tryLine(line: String) {
        val expected = want[currentLines.size]
        if (line == expected) {
            currentLines += line; msg = null; Juice.pick()
            if (currentLines.size == want.size) {
                built += currentLines; currentLines.clear(); k++; msg = true to "Instrução traduzida!"; Juice.correct()
            }
        } else {
            mistakes++; Juice.wrong()
            msg = false to when {
                line.startsWith("sw") && expected.startsWith("lw") -> "Primeiro precisa CARREGAR os valores (lw) para um registrador."
                line.startsWith("lw") && expected.startsWith("sw") -> "A conta já está no registrador; agora GUARDE o resultado (sw)."
                else -> "Não é essa ainda. Siga: carregar → operar → guardar."
            }
        }
    }
    com.compilandia.ui.DropZone(dd, "cpu", Modifier.fillMaxWidth()) { hovered ->
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).background(if (hovered) Palette.yellow.copy(alpha = 0.5f) else Palette.paperDark)
                .border(if (hovered) 4.dp else 2.dp, if (hovered) Palette.orange else Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("🔩 solte o tijolo aqui (${currentLines.size + 1}/${want.size})", fontWeight = FontWeight.Bold, color = Palette.ink) }
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
    RichText("Escreva o método de **$nt** do parser descendente recursivo (toque nas linhas na ordem).", style = MaterialTheme.typography.titleSmall)
    HintBox(ctx,
        "Uma produção vira uma sequência: terminal → casaToken(\"x\"); não-terminal → chamada X();",
        "Com várias produções, escolha pelo token atual: if (tok em FIRST da produção). A produção ε vira o else que não faz nada.",
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
                            } else { mistakes++; msg = "Ainda não é essa linha. Pense na ordem dos símbolos da produção."; Juice.wrong() }
                        }.padding(10.dp)
                ) { Mono(line.text, bold = true, size = 14) }
            }
        }
    } else {
        FeedbackBanner(true, "Método pronto! Repare: a pilha do analisador LL(1) virou a pilha de CHAMADAS do Java.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - 0.1 * mistakes).coerceAtLeast(0.0)) }
    }
}

