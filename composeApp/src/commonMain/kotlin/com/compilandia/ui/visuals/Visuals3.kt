package com.compilandia.ui.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.AstTools
import com.compilandia.engine.ExprParser
import com.compilandia.engine.MiniLang
import com.compilandia.engine.Minimizer
import com.compilandia.engine.Mips
import com.compilandia.engine.MipsVM
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.TacGen
import com.compilandia.ui.AutomatonView
import com.compilandia.ui.BigButton
import com.compilandia.ui.CodeBlock
import com.compilandia.ui.GraphView
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Mono
import com.compilandia.ui.Palette
import com.compilandia.ui.RichText
import com.compilandia.ui.TreeView
import com.compilandia.ui.regColors
import com.compilandia.ui.toUi
import com.compilandia.textos.tx

// ------------------------------------------------------------------ Minimização (tabela de pares)

@Composable
fun PairTable(n: Int, name: (Int) -> String, cell: @Composable (Int, Int) -> Unit) {
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        for (i in 1 until n) Row {
            Cell(name(i), header = true, w = 44)
            for (j in 0 until i) cell(i, j)
        }
        Row { Cell("", header = true, w = 44); for (j in 0 until n - 1) Cell(name(j), header = true, w = 44) }
    }
}

@Composable
fun MinimizeView(idx: Int) {
    val (title, dfa) = remember(idx) { Minimizer.bank[idx.coerceIn(0, Minimizer.bank.lastIndex)] }
    val r = remember(dfa) { Minimizer.run(dfa) }
    Mono(tx("comum_afd_1", title), bold = true)
    val d = r.dfa
    AutomatonView(d.stateCount, d.start, d.accepting, d.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { r.stateName(it) })
    if (r.deadState != null) Text(tx("visuals3_completamos_com_estado_morto_para"), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    Stepper(r.marks.size + 1) { i ->
        val shown = r.marks.take(i)
        val cur = r.marks.getOrNull(i - 1)
        PairTable(d.stateCount, r::stateName) { a, b ->
            val m = shown.firstOrNull { (it.p == b && it.q == a) || (it.p == a && it.q == b) }
            Cell(m?.let { "X${it.round}" } ?: "", w = 44, color = when { m != null && m == cur -> Palette.yellow; m != null -> Palette.red.copy(alpha = 0.2f); else -> null })
        }
        if (cur == null) RichText(tx("visuals3_comecamos_com_tabela_vazia_rodada"), style = MaterialTheme.typography.bodyMedium)
        else RichText("Rodada ${cur.round}: marca **{${r.stateName(cur.p)}, ${r.stateName(cur.q)}}** — ${cur.reason}.", style = MaterialTheme.typography.bodyMedium)
        if (i == r.marks.size) {
            RichText(tx("visuals3_nenhum_par_novo_pode_ser") +
                r.classes.joinToString("  ") { cl -> cl.joinToString(",", "{", "}") { r.stateName(it) } }, style = MaterialTheme.typography.bodyMedium)
            Text(tx("visuals3_afd_minimo_1_estados", r.minimized.stateCount), style = MaterialTheme.typography.titleSmall)
            val m = r.minimized
            AutomatonView(m.stateCount, m.start, m.accepting, m.delta.map { (k, t) -> Triple(k.first, k.second.toString(), t) }, stateLabel = { "m$it" })
        }
    }
}

// ------------------------------------------------------------------ AST

@Composable
fun AstView(src: String) {
    val concrete = remember(src) { AstTools.concreteTree(src) }
    val ast = remember(src) { ExprParser.parse(src) }
    Mono(tx("comum_expressao_1", src), bold = true)
    if (concrete != null) {
        Text(tx("visuals3_arvore_derivacao_concreta_1_nos", AstTools.countNodes(concrete)), style = MaterialTheme.typography.titleSmall)
        TreeView(concrete.toUi(AstTools.concreteGrammar))
    }
    Text(tx("visuals3_arvore_sintatica_abstrata_ast_1", AstTools.countNodes(ast)), style = MaterialTheme.typography.titleSmall, color = Palette.teal)
    TreeView(ast.toUi())
    RichText(tx("visuals3_ast_jogou_fora_correntes_t"), style = MaterialTheme.typography.bodySmall)
    CodeBlock(AstTools.toJava(ast), tx("visuals3_java_parser_devolve_este_objeto"))
    CodeBlock(AstTools.javaClasses, tx("visuals3_classes_ast"))
}

// ------------------------------------------------------------------ Alocação de registradores

@Composable
fun RegAllocView(src: String) {
    val code = remember(src) { TacGen.generate(ExprParser.parse(src), "r") }
    val lv = remember(code) { RegAlloc.liveness(code) }
    val g = remember(code) { RegAlloc.interference(code) }
    val k = remember(g) { RegAlloc.chromatic(g) }
    val alloc = remember(g) { RegAlloc.color(g, k) }
    Mono(tx("comum_r_1", ExprParser.parse(src).render()), bold = true)
    Text(tx("visuals3_1_vivacidade_baixo_para_cima"), style = MaterialTheme.typography.titleSmall)
    Stepper(code.size) { s ->
        val i = code.size - 1 - s
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            code.forEachIndexed { j, ins ->
                Row {
                    Cell(ins.toString(), w = 130, color = if (j == i) Palette.yellow.copy(alpha = 0.5f) else null)
                    Cell(if (j >= i) lv.liveOut[j].sorted().joinToString(",", "{", "}") else "", w = 90)
                }
            }
        }
        val c = code[i]
        Text(tx("visuals3_in_use_out_def_usa", listOfNotNull(c.a, c.b).filter { RegAlloc.isTemp(it) }.ifEmpty { listOf("—") }.joinToString(), c.target), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    }
    Text(tx("visuals3_2_grafo_interferencia_aresta_vivos", k), style = MaterialTheme.typography.titleSmall)
    GraphView(RegAlloc.temps(code), g, fill = { v -> alloc.colors[v]?.let { regColors[it % regColors.size] } ?: Palette.paperDark }, sub = { v -> alloc.colors[v]?.let { "\$s$it" } ?: "spill" })
    val before = Mips.fromTac(code); val after = RegAlloc.mips(code, alloc)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) { Text(tx("visuals3_tudo_na_memoria_1_lw", RegAlloc.memoryAccesses(before)), fontSize = 12.sp, fontWeight = FontWeight.Bold); for (l in before) Mono(l, size = 11) }
        Column(Modifier.weight(1f)) { Text(tx("visuals3_com_registradores_1_lw_sw", RegAlloc.memoryAccesses(after)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.teal); for (l in after) Mono(l, size = 11) }
    }
}

// ------------------------------------------------------------------ MARS de bolso

@Composable
fun MipsRunView(asm: String, highlightBranches: Boolean = false) {
    val vm = remember(asm) { try { MipsVM(asm) } catch (e: Exception) { null } }
    var steps by remember(asm) { mutableIntStateOf(0) }
    if (vm == null) { Text((try { MipsVM(asm); "" } catch (e: Exception) { e.message }).orEmpty(), color = Palette.red); return }
    // reexecuta do zero até 'steps' (simples e determinístico)
    val state = remember(asm, steps) {
        val v = MipsVM(asm)
        var err: String? = null
        try { repeat(steps) { if (!v.step()) return@repeat } } catch (e: Exception) { err = e.message }
        v to err
    }
    val (cur, err) = state
    val curLine = cur.trace.lastOrNull()?.lastLine
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(10.dp)) {
        asm.lines().forEachIndexed { ln, l ->
            val isBranch = highlightBranches && Regex("^\\s*(b\\w*|j|jal|jr)\\s").containsMatchIn(l)
            Text(l, fontFamily = LocalFonts.current.mono, fontSize = 12.sp,
                color = when { ln == curLine -> Palette.yellow; isBranch -> Color(0xFFF9A8D4); else -> Color(0xFFE8E6F0) },
                fontWeight = if (ln == curLine) FontWeight.Bold else FontWeight.Normal)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BigButton(tx("visuals3_passo"), small = true, color = Palette.teal, enabled = !cur.halted && err == null) { steps++ }
        BigButton(tx("visuals3_rodar_tudo"), small = true, color = Palette.orange, enabled = !cur.halted && err == null) { steps += 5000 }
        BigButton("↺", small = true, color = Palette.white, textColor = Palette.ink) { steps = 0 }
    }
    Text(tx("visuals3_instrucoes_executadas_1", cur.steps) + if (cur.halted) tx("visuals3_programa_terminou") else "", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
    err?.let { Text(it, color = Palette.red, fontWeight = FontWeight.Bold) }
    val regs = cur.regs.filterKeys { it != "\$gp" }.entries.sortedBy { it.key }.associate { it.key to it.value }
    if (regs.isNotEmpty()) Row(Modifier.horizontalScroll(rememberScrollState())) {
        for ((r, v) in regs) Cell("$r\n${if (r == "\$sp" || r == "\$ra") v.toString(16) else v.toString()}", w = 70)
    }
    if (cur.dataAddr.isNotEmpty()) Row(Modifier.horizontalScroll(rememberScrollState())) {
        for (name in cur.dataAddr.keys) cur.word(name)?.let { Cell("$name\n$it", w = 64) }
    }
    if (cur.output.isNotEmpty()) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF0B0B12)).padding(8.dp)) {
        Text(tx("visuals3_console"), color = Palette.green, fontSize = 11.sp)
        Text(cur.output.toString(), fontFamily = LocalFonts.current.mono, color = Palette.green, fontSize = 13.sp)
    }
}

@Composable
fun MiniLangView(src: String) {
    val lines = remember(src) { MiniLang.compile(src, MiniLang.sampleEnv) }
    CodeBlock(src, tx("comum_programa_4_b_7"))
    Text(tx("visuals3_compilado_para_mips_desvios_em"), style = MaterialTheme.typography.titleSmall)
    MipsRunView(MiniLang.asmText(lines), highlightBranches = true)
}
