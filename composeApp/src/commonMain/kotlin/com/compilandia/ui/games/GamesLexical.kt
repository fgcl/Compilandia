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
import com.compilandia.textos.tx

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
        else { mistakes++; wrongFlash = tx("gameslexical_ainda_nao_antes_1_vem", p.name); Juice.wrong() }
    }
    Text(tx("gameslexical_arraste_fases_para_esteira_na"), style = MaterialTheme.typography.titleSmall)
    com.compilandia.ui.DropZone(dd, "esteira", Modifier.fillMaxWidth()) { hovered ->
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (hovered) Palette.yellow.copy(alpha = 0.35f) else Palette.paperDark)
                .border(if (hovered) 4.dp else 2.dp, if (hovered) Palette.orange else Palette.ink.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).padding(12.dp),
        ) {
            Text(tx("gameslexical_codigo_fonte"), style = MaterialTheme.typography.labelLarge)
            for (i in placed) Text("   ↓\n${stages[i].name}", style = MaterialTheme.typography.bodyMedium)
            if (placed.size < stages.size) Text(tx("gameslexical_solte_proxima_fase_aqui"), color = Palette.inkSoft)
        }
    }
    HintBox(ctx, tx("gameslexical_primeiro_texto_vira_tokens_depois"), tx("gameslexical_otimizacao_vem_depois_codigo_intermediario"))
    wrongFlash?.let { FeedbackBanner(false, it) }
    ChipRow {
        for (p in shuffled) {
            val idx = stages.indexOf(p)
            if (idx in placed) continue
            com.compilandia.ui.Draggable(dd, p, onTap = { place(p) }, onDrop = { place(p) }) {
                com.compilandia.ui.Tile(p.name, Palette.orange.copy(alpha = 0.25f), mono = false)
            }
        }
    }
    if (placed.size == stages.size) {
        FeedbackBanner(true, tx("gameslexical_pipeline_montado_lexico_sintatico_semantico"))
        // J8: uma linha de verdade passa pela esteira montada.
        val passos = remember { com.compilandia.engine.Reacoes.esteira(placed.map { stages[it].name }) }
        Reacao(passos) { done((1.0 - mistakes * 0.12).coerceAtLeast(0.0)) }
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
    Text(tx("gameslexical_qual_fase_fabricou_este_produto", current + 1, items.size), style = MaterialTheme.typography.titleSmall)
    CodeBlock(it.example, tx("gameslexical_entrada_posicao_inicial_taxa_60"))
    for (o in options) BigButton(o.name, Modifier.fillMaxWidth(), color = Palette.white, textColor = Palette.ink, small = true, enabled = answered == null) {
        val ok = o == it
        if (ok) correct++
        answered = ok
        if (ok) Juice.correct() else Juice.wrong()
    }
    answered?.let { ok ->
        FeedbackBanner(ok, if (ok) tx("gameslexical_isso_1_produz_2", it.name, it.output) else tx("gameslexical_era_1_ela_produz_2", it.name, it.output))
        BotaoDoJogo(tx("gameslexical_proximo"), Modifier.fillMaxWidth()) { answered = null; current++ }
    }
}

// ============================================================ Detetive de Erros

private data class Case(val code: String, val phase: Int, val why: String)
private val detectiveBins = listOf(tx("gameslexical_lexico"), tx("gameslexical_sintatico"), tx("gameslexical_semantico"), tx("gameslexical_execucao"))
private val detectiveCases = listOf(
    Case("x = 3 @ 4;", 0, tx("gameslexical_nao_pertence_nenhum_padrao_token")),
    Case("nome = \"Maria;", 0, tx("gameslexical_string_nunca_fecha_aspas_lexico")),
    Case("total = 1 # 2;", 0, tx("gameslexical_caractere_invalido_na_linguagem_erro")),
    Case("if (x > ) y = 1;", 1, tx("gameslexical_todos_tokens_sao_validos_mas")),
    Case("while x < 3 { x++; }", 1, tx("gameslexical_faltam_parenteses_exigidos_pela_gramatica")),
    Case("y = (a + b;", 1, tx("gameslexical_parentese_aberto_nunca_fechado_so")),
    Case("int x int y;", 1, tx("gameslexical_faltou_entre_declaracoes_sequencia_tokens")),
    Case("int x; x = \"oi\";", 2, tx("gameslexical_frase_esta_bem_formada_mas")),
    Case("y = z + 1;  // z nunca foi declarada", 2, tx("gameslexical_sintaxe_perfeita_mas_tabela_simbolos")),
    Case("int a; float a;", 2, tx("gameslexical_declarar_duas_vezes_mesmo_nome")),
    Case(tx("gameslexical_f_1_2_f_foi"), 2, tx("gameslexical_numero_argumentos_errado_checagem_semantica")),
    Case(tx("gameslexical_v_10_1_v_tem"), 3, tx("gameslexical_em_c_compilador_normalmente_nao")),
    Case("a = b / c;  // c vale 0 na hora", 3, tx("gameslexical_divisao_por_zero_com_valores")),
    Case("whille (x) { }", 2, tx("gameslexical_whille_vira_identificador_nao_palavra")),
    Case("int 2x = 5;", 1, tx("gameslexical_scanner_gera_num_2_id")),
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
    val palpites = remember { mutableMapOf<Int, Int>() }
    if (i >= cases.size) {
        // J8: o compilador roda o caso em que você errou (ou o último) fase por fase.
        val passos = remember {
            val k = cases.indices.firstOrNull { palpites[it] != cases[it].phase } ?: cases.lastIndex
            com.compilandia.engine.Reacoes.detetive(cases[k].code, cases[k].phase, palpites[k] ?: -1, detectiveBins)
        }
        Reacao(passos) { ctx.finish(correct.toDouble() / cases.size) }
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
    Text(tx("gameslexical_caso_1_2_quem_pega", i + 1, cases.size), style = MaterialTheme.typography.titleSmall)
    if (timed) ProgressBar(timeLeft, if (timeLeft > 0.3f) Palette.teal else Palette.red, Modifier.fillMaxWidth(), 8.dp)
    val dd = com.compilandia.ui.rememberDragDrop()
    fun choose(k: Int) { if (answer != null) return; answer = k; palpites[i] = k; if (k == c.phase) { correct++; Juice.correct() } else Juice.wrong() }
    HintBox(ctx, tx("gameslexical_lexico_caractere_palavra_impossivel_sintatic"))
    Text(tx("gameslexical_arraste_cartao_suspeito_ate_setor"), style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
    if (answer == null) androidx.compose.runtime.key(i) {
        com.compilandia.ui.Draggable(dd, c, Modifier.fillMaxWidth(), onDrop = { z -> choose(z.toInt()) }) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Palette.codeBg).border(3.dp, Palette.yellow, RoundedCornerShape(14.dp)).padding(12.dp)) {
                Text(tx("gameslexical_caso_1", i + 1), color = Palette.yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
        FeedbackBanner(a == c.phase, (if (a == -1) tx("gameslexical_tempo_esgotado") else "") + "**${detectiveBins[c.phase]}** — ${c.why}")
        BotaoDoJogo(tx("gameslexical_proximo_caso"), Modifier.fillMaxWidth()) { answer = null; i++ }
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
        when (phase) { 0 -> tx("gameslexical_toque_nos_espacos_entre_caracteres"); 1 -> tx("gameslexical_toque_em_cada_token_para"); else -> tx("gameslexical_resultado") },
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
        HintBox(ctx, tx("gameslexical_regra_maior_casamento_scanner_engole"), tx("gameslexical_palavras_como_iff_ou_whilex"))
        BotaoDoJogo(tx("gameslexical_cortar"), Modifier.fillMaxWidth(), color = Palette.red) {
            val mine = playerTokens()
            val hit = mine.count { it in truthSpans }
            cutScore = (2f * hit / (mine.size + truthSpans.size)).coerceIn(0f, 1f)
            if (cutScore > 0.99f) Juice.correct() else Juice.wrong()
            phase = 1
        }
    } else {
        val spans = if (cutScore > 0.99f) truthSpans else truthSpans // classificação sempre sobre os tokens corretos
        if (cutScore < 0.99f && phase == 1) FeedbackBanner(false, tx("gameslexical_cortes_certos_eram_estes_1", (cutScore * 100).toInt()))
        else if (phase == 1) FeedbackBanner(true, tx("gameslexical_cortes_perfeitos_agora_classifique"))
        val dd = com.compilandia.ui.rememberDragDrop()
        val classOptions = TokenClass.entries.filter { it != TokenClass.STRING }
        if (phase == 1) Text(tx("gameslexical_arraste_cada_token_para_caixa"), style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
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
                        Text(chosen?.label ?: "?", fontSize = 10.sp, color = chosen?.let { tokenColor(it) } ?: Palette.inkSoft)
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
                    Text(cl.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.ink)
                    Text("${classes.values.count { it == cl }}", fontSize = 11.sp, color = Palette.inkSoft)
                }
            }
        }
        if (phase == 1) BotaoDoJogo(tx("gameslexical_conferir_classes"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = classes.size == spans.size) {
            classScore = spans.indices.count { classes[it] == truth[it].cls }.toFloat() / spans.size
            if (classScore > 0.99f) Juice.correct() else Juice.wrong()
            phase = 2
        } else {
            FeedbackBanner(classScore > 0.8f, tx("gameslexical_cortes_1_classes_2_total", (cutScore * 100).toInt(), (classScore * 100).toInt(), truth.size))
            // J8: os tokens seguem com a classe que você deu: tabela de símbolos e parser.
            val passos = remember { com.compilandia.engine.Reacoes.fatiador(truth, classes.toMap()) }
            Reacao(passos) { done(0.6 * cutScore + 0.4 * classScore) }
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

    Mono(tx("gameslexical_regex_1_2", entry.regex, entry.description), bold = true)
    val move = remember(pos) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(pos) { move.snapTo(0f); move.animateTo(1f, androidx.compose.animation.core.tween(550)) }
    val trav = if (pos > 0 && path[pos] >= 0) Triple(path[pos - 1], path[pos], move.value) else if (path[pos] >= 0) Triple(path[pos], path[pos], 1f) else null
    DfaView(dfa, current = path[pos].takeIf { it >= 0 }, lastEdge = if (pos > 0 && path[pos] >= 0) path[pos - 1] to path[pos] else null, traveler = trav) { tapped ->
        if (atEnd || verdict != null) return@DfaView
        if (stuck) { mistakes++; msg = false to tx("gameslexical_nao_existe_transicao_q_1", path[pos], input[pos]); Juice.wrong(); return@DfaView }
        if (tapped == path[pos + 1]) { pos++; msg = null; Juice.correct() }
        else { mistakes++; msg = false to tx("gameslexical_com_1_q_2_trem", input[pos], path[pos], tapped); Juice.wrong() }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(tx("gameslexical_cadeia"), style = MaterialTheme.typography.titleSmall)
        input.forEachIndexed { k, ch ->
            Box(
                Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp)).background(when { k < pos -> Palette.paperDark; k == pos -> Palette.yellow; else -> Palette.white })
                    .border(1.dp, Palette.ink, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Mono(ch.toString(), bold = true, size = 18) }
        }
        if (input.isEmpty()) Mono(tx("gameslexical_vazia"), bold = true)
    }
    HintBox(ctx, tx("gameslexical_ache_circulo_amarelo_estado_atual"), tx("gameslexical_se_nao_houver_seta_com"))
    msg?.let { FeedbackBanner(it.first, it.second) }
    if (!atEnd && verdict == null && stuck) BigButton(tx("gameslexical_travou_sem_transicao"), Modifier.fillMaxWidth(), color = Palette.red) {
        pos = input.length; msg = true to tx("gameslexical_isso_sem_transicao_rejeita_na"); Juice.correct()
        verdict = false
    }
    if ((atEnd) && verdict == null) {
        Text(tx("gameslexical_fim_cadeia_trem_parou_em", path.getOrNull(pos)), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((label, v) in listOf(tx("gameslexical_aceita") to true, tx("gameslexical_rejeita") to false)) BigButton(label, Modifier.weight(1f), color = Palette.white, textColor = Palette.ink) {
                val ok = dfa.accepts(input) == v
                if (!ok) mistakes++
                verdict = v
                msg = ok to if (dfa.accepts(input)) tx("gameslexical_parou_num_estado_aceitacao_circulo") else tx("gameslexical_nao_parou_num_circulo_duplo")
                if (ok) Juice.correct() else Juice.wrong()
            }
        }
    }
    if (verdict != null) BotaoDoJogo(tx("gameslexical_proximo_trem"), Modifier.fillMaxWidth()) { done((1.0 - mistakes * 0.2).coerceAtLeast(0.0)) }
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
    val respostas = remember { mutableListOf<Pair<String, Boolean?>>() }
    if (i >= queue.size) {
        // J8: o porteiro vira autômato e julga as palavras em que você errou e outras novas.
        val passos = remember {
            val novas = dfa.samples(kotlin.random.Random(ctx.rnd.nextLong()), 8, maxLen = 3 + ctx.level).map { it.first }
            com.compilandia.engine.Reacoes.porteiro(entry.regex, respostas, novas)
        }
        last?.let { FeedbackBanner(it.first, it.second) }
        Reacao(passos) { ctx.finish(correct.toDouble() / queue.size) }
        return
    }
    val (s, accept) = queue[i]
    fun judge(letIn: Boolean?) {
        val ok = letIn == accept
        respostas += s to letIn
        if (ok) correct++
        last = ok to tx("gameslexical_1_2_com_3", s.ifEmpty { "ε" }, if (accept) tx("gameslexical_casa") else tx("gameslexical_nao_casa"), entry.regex)
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
        Text(tx("gameslexical_balada_regex"), color = Palette.yellow, style = MaterialTheme.typography.headlineSmall)
        Mono(tx("gameslexical_so_entra_quem_casa_com", entry.regex), color = Palette.onAccent, bold = true)
        Text("(${entry.description})", color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
    Text(tx("gameslexical_fila_1_pessoas", queue.size - i), style = MaterialTheme.typography.labelMedium)
    if (timed) ProgressBar(timeLeft, Palette.orange, Modifier.fillMaxWidth(), 8.dp)
    Text(tx("gameslexical_arraste_pessoa_direita_entra_esquerda"), style = MaterialTheme.typography.labelLarge, color = Palette.inkSoft)
    Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
        // próxima da fila aparecendo atrás
        if (i + 1 < queue.size) Box(
            Modifier.fillMaxWidth(0.82f).height(190.dp).clip(RoundedCornerShape(22.dp)).background(Palette.paperDark).border(2.dp, Palette.ink.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
        )
        androidx.compose.runtime.key(i) {
            com.compilandia.ui.SwipeCard(i, tx("gameslexical_barra"), tx("gameslexical_entra"), Modifier.fillMaxWidth(0.9f), onSwipe = { right -> judge(right) }) {
                Column(
                    Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(22.dp)).background(Palette.yellow)
                        .border(3.dp, Palette.inkOnLight, RoundedCornerShape(22.dp)).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Text(s.ifEmpty { tx("gameslexical_cadeia_vazia") }, fontFamily = LocalFonts.current.mono, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Palette.inkOnLight)
                    Text("${i + 1}/${queue.size}", fontSize = 12.sp, color = Palette.inkOnLight.copy(alpha = 0.6f))
                }
            }
        }
    }
    HintBox(ctx, tx("gameslexical_leia_regex_como_receita_zero"), tx("gameslexical_tente_andar_com_cadeia_pela"))
    if (ctx.hints >= 2) DfaView(dfa)
    last?.let { FeedbackBanner(it.first, it.second) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BigButton(tx("gameslexical_barra"), Modifier.weight(1f), color = Palette.red, small = true) { judge(false) }
        BigButton(tx("gameslexical_entra"), Modifier.weight(1f), color = Palette.green, small = true) { judge(true) }
    }
}

