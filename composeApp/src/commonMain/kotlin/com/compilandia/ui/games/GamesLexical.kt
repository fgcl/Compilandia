@file:OptIn(ExperimentalLayoutApi::class)

package com.compilandia.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.DFA
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.RegexBank
import com.compilandia.engine.TokenClass
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.ChipRow
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.DfaView
import com.compilandia.ui.FeedbackBanner
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.ProgressBar
import com.compilandia.ui.RichText
import com.compilandia.ui.visuals.phases
import com.compilandia.ui.visuals.tokenColor
import kotlinx.coroutines.delay

// ============================================================ Esteira das Fases

@Composable
fun PipelineGame(ctx: GameCtx) {
    Rounds(ctx, 2) { round, done ->
        if (round == 0) PipelineOrder(ctx, done) else PipelineMatch(ctx, done)
    }
}

@Composable
private fun PipelineOrder(ctx: GameCtx, done: (Double) -> Unit) {
    val stages = phases.drop(1)
    val shuffled = remember { stages.shuffled(ctx.rnd) }
    val placed = remember { mutableStateListOf<Int>() } // índices em stages
    var mistakes by remember { mutableIntStateOf(0) }
    var wrongFlash by remember { mutableStateOf<String?>(null) }
    val dd = com.compilandia.ui.rememberDragDrop()
    fun place(p: com.compilandia.ui.visuals.Phase) {
        val idx = stages.indexOf(p)
        if (idx == placed.size) { placed += idx; wrongFlash = null; Juice.correct() }
        else { mistakes++; wrongFlash = "Ainda não! Antes de **${p.name}** vem outra fase."; Juice.wrong() }
    }
    Text("🏭 Arraste as fases para a ESTEIRA na ordem em que o código passa por elas:", style = MaterialTheme.typography.titleSmall)
    com.compilandia.ui.DropZone(dd, "esteira", Modifier.fillMaxWidth()) { hovered ->
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (hovered) Palette.yellow.copy(alpha = 0.35f) else Palette.paperDark)
                .border(if (hovered) 4.dp else 2.dp, if (hovered) Palette.orange else Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).padding(12.dp),
        ) {
            Text("📄 código-fonte", style = MaterialTheme.typography.labelLarge)
            for (i in placed) Text("   ⬇\n${stages[i].emoji} ${stages[i].name}", style = MaterialTheme.typography.bodyMedium)
            if (placed.size < stages.size) Text("   ⬇\n   📥 solte a próxima fase aqui", color = Palette.inkSoft)
        }
    }
    HintBox(ctx, "Primeiro o texto vira tokens, depois os tokens viram árvore. Só depois disso dá para checar tipos.", "Otimização vem depois do código intermediário e antes do código final.")
    wrongFlash?.let { FeedbackBanner(false, it) }
    ChipRow {
        for (p in shuffled) {
            val idx = stages.indexOf(p)
            if (idx in placed) continue
            com.compilandia.ui.Draggable(dd, p, onTap = { place(p) }, onDrop = { place(p) }) {
                com.compilandia.ui.Tile("${p.emoji} ${p.name}", Palette.orange.copy(alpha = 0.25f), mono = false)
            }
        }
    }
    if (placed.size == stages.size) {
        FeedbackBanner(true, "Pipeline montado! Léxico → Sintático → Semântico → Intermediário → Otimização → Código.")
        BigButton("Continuar ▶", Modifier.fillMaxWidth()) { done((1.0 - mistakes * 0.12).coerceAtLeast(0.0)) }
    }
}

@Composable
private fun PipelineMatch(ctx: GameCtx, done: (Double) -> Unit) {
    val items = remember { phases.drop(1).shuffled(ctx.rnd).take(4) }
    val options = remember { items.shuffled(ctx.rnd) }
    var current by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf<Boolean?>(null) }
    if (current >= items.size) {
        LaunchedEffect(Unit) { done(correct.toDouble() / items.size) }
        return
    }
    val it = items[current]
    Text("Qual fase fabricou este produto? (${current + 1}/${items.size})", style = MaterialTheme.typography.titleSmall)
    CodeBlock(it.example, "entrada: posicao = inicial + taxa * 60")
    for (o in options) BigButton("${o.emoji} ${o.name}", Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink, small = true, enabled = answered == null) {
        val ok = o == it
        if (ok) correct++
        answered = ok
        if (ok) Juice.correct() else Juice.wrong()
    }
    answered?.let { ok ->
        FeedbackBanner(ok, if (ok) "Isso! ${it.name} produz ${it.output}." else "Era **${it.name}**: ela produz ${it.output}.")
        BigButton("Próximo ▶", Modifier.fillMaxWidth()) { answered = null; current++ }
    }
}

// ============================================================ Detetive de Erros

private data class Case(val code: String, val phase: Int, val why: String)
private val detectiveBins = listOf("🔪 Léxico", "🌳 Sintático", "🔍 Semântico", "💥 Execução")
private val detectiveCases = listOf(
    Case("x = 3 @ 4;", 0, "'@' não pertence a nenhum padrão de token: o scanner nem sabe o que é isso."),
    Case("nome = \"Maria;", 0, "A string nunca fecha as aspas: o léxico não consegue formar o token string."),
    Case("total = 1 # 2;", 0, "'#' é um caractere inválido na linguagem: erro léxico."),
    Case("if (x > ) y = 1;", 1, "Todos os tokens são válidos, mas falta um operando depois de '>': a ESTRUTURA está errada."),
    Case("while x < 3 { x++; }", 1, "Faltam os parênteses exigidos pela gramática do while."),
    Case("y = (a + b;", 1, "Parêntese aberto e nunca fechado: só uma gramática (não uma regex) percebe isso."),
    Case("int x int y;", 1, "Faltou ';' entre as declarações: a sequência de tokens não casa com nenhuma regra."),
    Case("int x; x = \"oi\";", 2, "A frase está bem formada, mas atribuir string a int viola as regras de TIPO."),
    Case("y = z + 1;  // z nunca foi declarada", 2, "Sintaxe perfeita; mas a tabela de símbolos não tem 'z': erro semântico."),
    Case("int a; float a;", 2, "Declarar duas vezes o mesmo nome no mesmo escopo: semântico."),
    Case("f(1, 2);  // f foi declarada com 1 parâmetro", 2, "Número de argumentos errado: checagem semântica."),
    Case("v[10] = 1;  // v tem 5 posições", 3, "Em C o compilador normalmente não sabe o índice em tempo de compilação: estoura na execução."),
    Case("a = b / c;  // c vale 0 na hora", 3, "Divisão por zero com valores que só existem quando o programa roda: erro de execução."),
    Case("whille (x) { }", 2, "'whille' vira um IDENTIFICADOR (não é palavra-chave!) e 'whille(x)' parece chamada de função não declarada: semântico. O léxico não tem como saber que foi erro de digitação."),
    Case("int 2x = 5;", 1, "O scanner gera NUM(2) e ID(x) — tokens válidos! Mas 'int NUM ID' não casa com a regra de declaração: sintático."),
)

@Composable
fun PhaseDetectiveGame(ctx: GameCtx) {
    val count = 5 + ctx.level
    val cases = remember { detectiveCases.shuffled(ctx.rnd).take(count) }
    var i by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf<Int?>(null) }
    var timeLeft by remember(i) { mutableFloatStateOf(1f) }
    val timed = ctx.level >= 3
    if (i >= cases.size) {
        LaunchedEffect(Unit) { ctx.finish(correct.toDouble() / cases.size) }
        return
    }
    val c = cases[i]
    if (timed && answer == null) {
        LaunchedEffect(i) {
            val total = (16 - ctx.level * 2) * 1000L
            var t = 0L
            while (t < total && answer == null) { delay(100); t += 100; timeLeft = 1f - t.toFloat() / total }
            if (answer == null) { answer = -1; Juice.wrong() }
        }
    }
    Text("Caso ${i + 1}/${cases.size}: quem pega este erro?", style = MaterialTheme.typography.titleSmall)
    if (timed) ProgressBar(timeLeft, if (timeLeft > 0.3f) Palette.teal else Palette.red, Modifier.fillMaxWidth(), 8.dp)
    val dd = com.compilandia.ui.rememberDragDrop()
    fun choose(k: Int) { if (answer != null) return; answer = k; if (k == c.phase) { correct++; Juice.correct() } else Juice.wrong() }
    HintBox(ctx, "Léxico = caractere/palavra impossível. Sintático = tokens OK mas estrutura errada. Semântico = estrutura OK mas significado errado (tipos, declarações). Execução = depende de valores reais.")
    Text("🕵️ Arraste o cartão do suspeito até o setor que pega esse erro:", style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
    if (answer == null) androidx.compose.runtime.key(i) {
        com.compilandia.ui.Draggable(dd, c, Modifier.fillMaxWidth(), onDrop = { z -> choose(z.toInt()) }) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Palette.codeBg).border(3.dp, Palette.yellow, RoundedCornerShape(14.dp)).padding(12.dp)) {
                Text("📁 CASO ${i + 1}", color = Palette.yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(c.code, fontFamily = LocalFonts.current.mono, color = Color(0xFFE8E6F0), fontSize = 15.sp)
            }
        }
    } else CodeBlock(c.code)
    for (row in listOf(0, 2)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (k in row until row + 2) {
            com.compilandia.ui.DropZone(dd, "$k", Modifier.weight(1f).height(78.dp)) { hovered ->
                val bg = when {
                    answer != null && k == c.phase -> Palette.green.copy(alpha = 0.35f)
                    answer == k -> Palette.red.copy(alpha = 0.3f)
                    hovered -> Palette.yellow.copy(alpha = 0.6f)
                    else -> Palette.white
                }
                Box(
                    Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(16.dp)).background(bg)
                        .border(if (hovered) 4.dp else 2.dp, if (hovered) Palette.orange else Palette.ink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable(enabled = answer == null) { choose(k) },
                    contentAlignment = Alignment.Center,
                ) { Text(detectiveBins[k], fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Palette.ink) }
            }
        }
    }
    answer?.let { a ->
        FeedbackBanner(a == c.phase, (if (a == -1) "Tempo esgotado! " else "") + "**${detectiveBins[c.phase]}** — ${c.why}")
        BigButton("Próximo caso ▶", Modifier.fillMaxWidth()) { answer = null; i++ }
    }
}

@Composable
private fun BinButton(label: String, k: Int, answer: Int?, right: Int, modifier: Modifier, onClick: () -> Unit) {
    val color = when {
        answer == null -> Palette.white
        k == right -> Palette.green
        k == answer -> Palette.red
        else -> Palette.white
    }
    BigButton(label, modifier.height(64.dp), color = color, enabled = answer == null || k == right || k == answer) {
        if (answer == null) onClick()
    }
}

// ============================================================ Fatiador de Tokens

@Composable
fun TokenSlicerGame(ctx: GameCtx) {
    Rounds(ctx, 3) { _, done -> TokenSlicerRound(ctx, done) }
}

@Composable
private fun TokenSlicerRound(ctx: GameCtx, done: (Double) -> Unit) {
    val line = remember { MiniLexer.randomLine(ctx.rnd, ctx.level) }
    val truth = remember(line) { MiniLexer.tokenize(line) }
    val cuts = remember { mutableStateListOf<Int>() } // posição i = corte antes do caractere i
    var phase by remember { mutableIntStateOf(0) }     // 0 = cortar, 1 = classificar, 2 = resultado
    val classes = remember { mutableStateMapOf<Int, TokenClass>() }
    var cutScore by remember { mutableFloatStateOf(0f) }
    var classScore by remember { mutableFloatStateOf(0f) }

    // segmentação do jogador: espaços sempre separam
    fun playerTokens(): List<Pair<Int, Int>> {
        val out = mutableListOf<Pair<Int, Int>>()
        var s = -1
        for (i in 0..line.length) {
            val boundary = i == line.length || line[i] == ' ' || i in cuts
            if (boundary && s >= 0) { out += s to i; s = -1 }
            if (i < line.length && line[i] != ' ' && s < 0) s = i
        }
        return out
    }
    val truthSpans = truth.map { it.start to it.end }

    Text(
        when (phase) { 0 -> "✂️ Toque nos espaços entre os caracteres para cortar"; 1 -> "🏷️ Toque em cada token para escolher a classe"; else -> "Resultado" },
        style = MaterialTheme.typography.titleSmall,
    )
    val mono = LocalFonts.current.mono
    if (phase == 0) {
        FlowRow(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in line.indices) {
                if (i > 0 && line[i] != ' ' && line[i - 1] != ' ') {
                    val on = i in cuts
                    Box(
                        Modifier.width(14.dp).height(40.dp).clickable { if (on) cuts.remove(i) else cuts.add(i); Sound.play(Sfx.CLICK) },
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.width(if (on) 4.dp else 2.dp).height(if (on) 38.dp else 22.dp).background(if (on) Palette.red else Palette.inkSoft.copy(alpha = 0.3f))) }
                }
                if (line[i] == ' ') Box(Modifier.width(12.dp).height(40.dp))
                else Box(
                    Modifier.height(40.dp).clip(RoundedCornerShape(6.dp)).background(Palette.white).border(1.dp, Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(line[i].toString(), fontFamily = mono, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
        HintBox(ctx, "Regra do maior casamento: o scanner engole o MAIOR lexema possível. `>=` é UM token; `count2` é UM identificador.", "Palavras como `iff` ou `whilex` NÃO são palavras-chave: são identificadores!")
        BigButton("Cortar! ✂️", Modifier.fillMaxWidth(), color = Palette.red) {
            val mine = playerTokens()
            val hit = mine.count { it in truthSpans }
            cutScore = (2f * hit / (mine.size + truthSpans.size)).coerceIn(0f, 1f)
            if (cutScore > 0.99f) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else {
        val spans = if (cutScore > 0.99f) truthSpans else truthSpans // classificação sempre sobre os tokens corretos
        if (cutScore < 0.99f && phase == 1) FeedbackBanner(false, "Os cortes certos eram estes (${(cutScore * 100).toInt()}% de acerto nos cortes). Agora classifique:")
        else if (phase == 1) FeedbackBanner(true, "Cortes perfeitos! Agora classifique:")
        val dd = com.compilandia.ui.rememberDragDrop()
        val classOptions = TokenClass.entries.filter { it != TokenClass.STRING }
        if (phase == 1) Text("Arraste cada token para a caixa da classe dele (ou toque para alternar):", style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
        ChipRow {
            spans.forEachIndexed { k, (a, b) ->
                val lex = line.substring(a, b)
                val chosen = classes[k]
                val right = truth[k].cls
                val bg = when {
                    phase == 2 && chosen == right -> Palette.green.copy(alpha = 0.25f)
                    phase == 2 -> Palette.red.copy(alpha = 0.2f)
                    chosen != null -> tokenColor(chosen).copy(alpha = 0.2f)
                    else -> Palette.white
                }
                com.compilandia.ui.Draggable(dd, k, enabled = phase == 1,
                    onTap = { classes[k] = classOptions[((chosen?.let { classOptions.indexOf(it) } ?: -1) + 1) % classOptions.size]; Sound.play(Sfx.CLICK) },
                    onDrop = { z -> classes[k] = TokenClass.valueOf(z) }) {
                    Column(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(bg).border(2.dp, Palette.ink.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Mono(lex, bold = true, size = 16)
                        Text(chosen?.let { "${it.emoji} ${it.label}" } ?: "?", fontSize = 10.sp, color = chosen?.let { tokenColor(it) } ?: Palette.inkSoft)
                        if (phase == 2 && chosen != right) Text("➜ ${right.label}", fontSize = 10.sp, color = Palette.green, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (phase == 1) ChipRow {
            for (cl in classOptions) com.compilandia.ui.DropZone(dd, cl.name) { hovered ->
                Column(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(if (hovered) tokenColor(cl).copy(alpha = 0.45f) else tokenColor(cl).copy(alpha = 0.15f))
                        .border(if (hovered) 4.dp else 2.dp, tokenColor(cl), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(cl.emoji, fontSize = 22.sp)
                    Text(cl.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.ink)
                    Text("${classes.values.count { it == cl }}", fontSize = 11.sp, color = Palette.inkSoft)
                }
            }
        }
        if (phase == 1) BigButton("Conferir classes ✔", Modifier.fillMaxWidth(), color = Palette.teal, enabled = classes.size == spans.size) {
            classScore = spans.indices.count { classes[it] == truth[it].cls }.toFloat() / spans.size
            if (classScore > 0.99f) Juice.correct() else Juice.wrong()
            phase = 2
        } else {
            FeedbackBanner(classScore > 0.8f, "Cortes: ${(cutScore * 100).toInt()}% · Classes: ${(classScore * 100).toInt()}%. Total de tokens: ${truth.size} (espaços somem — o scanner os descarta).")
            BigButton("Próxima linha ▶", Modifier.fillMaxWidth()) { done(0.6 * cutScore + 0.4 * classScore) }
        }
    }
}

// ============================================================ Trem do Autômato

@Composable
fun DfaTrainGame(ctx: GameCtx) {
    Rounds(ctx, 3) { _, done -> DfaTrainRound(ctx, done) }
}

@Composable
private fun DfaTrainRound(ctx: GameCtx, done: (Double) -> Unit) {
    val pool = RegexBank.entries.let { if (ctx.level <= 2) it.take(5) else it }
    val entry = remember { pool[ctx.rnd.nextInt(pool.size)] }
    val dfa = remember { DFA.fromRegex(entry.regex) }
    val input = remember { dfa.samples(ctx.rnd, 1, maxLen = 2 + ctx.level).firstOrNull()?.first ?: "ab" }
    val path = remember { dfa.run(input) }
    var pos by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var verdict by remember { mutableStateOf<Boolean?>(null) }
    var msg by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val stuck = pos < input.length && path[pos + 1] < 0
    val atEnd = pos == input.length || (pos < path.size && path[pos] < 0)

    Mono("regex: ${entry.regex}   (${entry.description})", bold = true)
    val move = remember(pos) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(pos) { move.snapTo(0f); move.animateTo(1f, androidx.compose.animation.core.tween(550)) }
    val trav = if (pos > 0 && path[pos] >= 0) Triple(path[pos - 1], path[pos], move.value) else if (path[pos] >= 0) Triple(path[pos], path[pos], 1f) else null
    DfaView(dfa, current = path[pos].takeIf { it >= 0 }, lastEdge = if (pos > 0 && path[pos] >= 0) path[pos - 1] to path[pos] else null, traveler = trav) { tapped ->
        if (atEnd || verdict != null) return@DfaView
        if (stuck) { mistakes++; msg = false to "Não existe transição de q${path[pos]} com '${input[pos]}'. Use o botão 'travou'!"; Juice.wrong(); return@DfaView }
        if (tapped == path[pos + 1]) { pos++; msg = null; Juice.correct() }
        else { mistakes++; msg = false to "Com '${input[pos]}', de q${path[pos]} o trem NÃO vai para q$tapped. Siga a seta com esse rótulo."; Juice.wrong() }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Cadeia: ", style = MaterialTheme.typography.titleSmall)
        input.forEachIndexed { k, ch ->
            Box(
                Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp)).background(when { k < pos -> Palette.paperDark; k == pos -> Palette.yellow; else -> Palette.white })
                    .border(1.dp, Palette.ink, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Mono(ch.toString(), bold = true, size = 18) }
        }
        if (input.isEmpty()) Mono("ε (vazia)", bold = true)
    }
    HintBox(ctx, "Ache o círculo amarelo (estado atual) e procure a seta que sai dele com o símbolo amarelo da cadeia.", "Se não houver seta com aquele símbolo, o AFD trava e REJEITA.")
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (!atEnd && verdict == null && stuck) BigButton("💥 Travou! (sem transição)", Modifier.fillMaxWidth(), color = Palette.red) {
        pos = input.length; msg = true to "Isso! Sem transição = rejeita na hora."; Juice.correct()
        verdict = false
    }
    if ((atEnd) && verdict == null) {
        Text("Fim da cadeia! O trem parou em q${path.getOrNull(pos)}. E aí?", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((label, v) in listOf("✅ Aceita" to true, "❌ Rejeita" to false)) BigButton(label, Modifier.weight(1f), color = Palette.white, textColor = Palette.ink) {
                val ok = dfa.accepts(input) == v
                if (!ok) mistakes++
                verdict = v
                msg = ok to if (dfa.accepts(input)) "Parou num estado de ACEITAÇÃO (círculo duplo): aceita." else "Não parou num círculo duplo: rejeita."
                if (ok) Juice.correct() else Juice.wrong()
            }
        }
    }
    if (verdict != null) BigButton("Próximo trem ▶", Modifier.fillMaxWidth()) { done((1.0 - mistakes * 0.2).coerceAtLeast(0.0)) }
}

// ============================================================ Porteiro Regex

@Composable
fun RegexSorterGame(ctx: GameCtx) {
    val entry = remember { RegexBank.entries[ctx.rnd.nextInt(RegexBank.entries.size)] }
    val dfa = remember { DFA.fromRegex(entry.regex) }
    val queue = remember { dfa.samples(ctx.rnd, 6 + ctx.level, maxLen = 3 + ctx.level) }
    var i by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var last by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var timeLeft by remember(i) { mutableFloatStateOf(1f) }
    val timed = ctx.level >= 3
    if (i >= queue.size) {
        LaunchedEffect(Unit) { ctx.finish(correct.toDouble() / queue.size) }
        return
    }
    val (s, accept) = queue[i]
    fun judge(letIn: Boolean?) {
        val ok = letIn == accept
        if (ok) correct++
        last = ok to "\"${s.ifEmpty { "ε" }}\" ${if (accept) "CASA" else "NÃO casa"} com ${entry.regex}"
        if (ok) Juice.correct() else Juice.wrong()
        i++
    }
    if (timed) LaunchedEffect(i) {
        val total = (9 - ctx.level) * 1000L
        var t = 0L
        while (t < total) { delay(100); t += 100; timeLeft = 1f - t.toFloat() / total }
        judge(null)
    }
    PaperCard(color = Palette.codeBg) {
        Text("🚪 Balada REGEX", color = Palette.yellow, style = MaterialTheme.typography.headlineSmall)
        Mono("só entra quem casa com:  ${entry.regex}", color = Palette.onAccent, bold = true)
        Text("(${entry.description})", color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
    Text("Fila: ${queue.size - i} pessoas", style = MaterialTheme.typography.labelMedium)
    if (timed) ProgressBar(timeLeft, Palette.orange, Modifier.fillMaxWidth(), 8.dp)
    Text("👉 Arraste a pessoa: DIREITA = entra · ESQUERDA = barra", style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
    Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
        // próxima da fila aparecendo atrás
        if (i + 1 < queue.size) Box(
            Modifier.fillMaxWidth(0.82f).height(190.dp).clip(RoundedCornerShape(22.dp)).background(Palette.paperDark).border(2.dp, Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
        )
        androidx.compose.runtime.key(i) {
            com.compilandia.ui.SwipeCard(i, "BARRA ⛔", "ENTRA ✅", Modifier.fillMaxWidth(0.9f), onSwipe = { right -> judge(right) }) {
                Column(
                    Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(22.dp)).background(Palette.yellow)
                        .border(3.dp, Palette.inkOnLight, RoundedCornerShape(22.dp)).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Text("🧍", fontSize = 54.sp)
                    Text(s.ifEmpty { "ε (cadeia vazia)" }, fontFamily = LocalFonts.current.mono, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Palette.inkOnLight)
                    Text("${i + 1}/${queue.size}", fontSize = 12.sp, color = Palette.inkOnLight.copy(alpha = 0.6f))
                }
            }
        }
    }
    HintBox(ctx, "Leia a regex como uma receita: * = zero ou mais vezes, + = uma ou mais, | = ou.", "Tente 'andar' com a cadeia pela regex da esquerda para a direita.")
    if (ctx.hints >= 2) DfaView(dfa)
    last?.let { FeedbackBanner(it.first, it.second) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BigButton("⛔", Modifier.weight(1f), color = Palette.red, small = true) { judge(false) }
        BigButton("✅", Modifier.weight(1f), color = Palette.green, small = true) { judge(true) }
    }
}

