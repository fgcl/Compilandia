package com.compilandia.tutor

import com.compilandia.content.Box
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.titulo
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRTable
import com.compilandia.engine.SetStep
import com.compilandia.learning.Skill
import com.compilandia.textos.tx
import kotlin.math.min

/**
 * Questão de prova de SLR(1): itens LR(0), FOLLOW, tabela ACTION/GOTO e veredito.
 * A tabela sai do [LRTable] do motor, com as regras numeradas como na prova.
 */
object ProvaSlr {
    /** As três questões, na ordem da revisão: E → E + n | n, depois S → ( S ) | id, depois a dos ponteiros, que tem conflito. */
    val GRAMATICAS = listOf("E -> E + n | n", "S -> ( S ) | id", "S -> L = R | R\nL -> * R | id\nR -> L")

    fun questao(g: Grammar): Sim {
        val t = LRTable(g, LRKind.SLR1)
        val regras = regras(t)
        val abre = SimStep(Shot(listOf(titulo(tx("trilha_etapa_prova"))) + codigo("r", regras, 110f, x = 60f, passo = 30f, size = 15f)), tx("prova_slr_abre"))
        val tabela = tabela(t, regras)
        val corpo = Sims.lr0(g).passos + Sims.conjuntos(g, SetStep.Kind.FOLLOW).passos + tabela
        return Sim(tx("trilha_etapa_prova"), listOf(abre) + corpo + veredito(t, corpo.last().shot), Skill.SLR_TABLE)
    }

    /** As regras da gramática aumentada, numeradas como os r da tabela. */
    private fun regras(t: LRTable) = t.grammar.productions.mapIndexed { i, p -> "$i. ${p.lhs} → ${p.rhsText()}" }

    private fun tabela(t: LRTable, regras: List<String>): List<SimStep> {
        val n = t.automaton.states.size
        val cols = t.terminals + t.nonTerminals
        val cw = min(48f, 340f / (cols.size + 1))
        val ch = min(30f, 250f / (n + 1))
        val x0 = 180f - cols.size * cw / 2
        val y0 = 56f
        val tam = min(12f, ch * 0.5f)
        fun xy(l: Int, c: Int) = (x0 + c * cw) to (y0 + l * ch)
        val fixas = mutableListOf<Box>(titulo(tx("prova_slr_tabela"), y = 20f, size = 20f))
        cols.forEachIndexed { c, s -> val (x, y) = xy(0, c + 1); fixas += Box("h$c", s, x, y, cw - 3f, ch - 3f, Look.CELL, Tint.DARK, size = tam) }
        for (l in 0 until n) { val (x, y) = xy(l + 1, 0); fixas += Box("r$l", "$l", x, y, cw - 3f, ch - 3f, Look.CELL, Tint.ORANGE, size = tam) }
        fixas += codigo("rg", regras.drop(1), y0 + (n + 1) * ch + 12f, x = 24f, passo = 16f, size = 11f)
        val cheias = mutableListOf<Box>()
        val todas = t.rawActions.values.flatten().map { it.short() }.distinct()
        val passos = mutableListOf(SimStep(Shot(fixas.toList()), tx("prova_slr_linhas")))
        var k = 0
        for (l in 0 until n) for ((c, a) in t.terminals.withIndex()) {
            val acts = t.rawActions[l to a].orEmpty()
            if (acts.isEmpty()) continue
            val texto = acts.joinToString("/") { it.short() }
            val (x, y) = xy(l + 1, c + 1)
            val conflito = acts.size > 1
            cheias += Box("c$l-$c", texto, x, y, cw - 3f, ch - 3f, Look.TOKEN, if (conflito) Tint.RED else Tint.TEAL, hot = true, size = min(tam, (cw - 4f) / (texto.length * 0.62f)))
            val diz = porque(t, l, a, acts)
            val (ops, idx) = opcoes(texto, todas.shuffled(kotlin.random.Random(k)) + tx("sims_deixar_vazia"), k)
            val ask = if (k % 2 == 0 || conflito) SimAsk(tx("prova_slr_casa", l, a), ops, idx, diz) else null
            passos += SimStep(Shot(fixas + cheias.map { if (it === cheias.last()) it else it.copy(hot = false) }), diz, ask, erro = conflito)
            k++
        }
        passos += goto(t, fixas + cheias.map { it.copy(hot = false) }, ::xy, cw, ch, tam, n)
        return passos
    }

    private fun porque(t: LRTable, l: Int, a: String, acts: List<LRAction>): String = when {
        acts.size > 1 -> tx("prova_slr_conflito", l, a)
        acts[0] is LRAction.Shift -> tx("prova_slr_shift", l, a, (acts[0] as LRAction.Shift).state)
        acts[0] is LRAction.Reduce -> (acts[0] as LRAction.Reduce).prodId.let { p ->
            val pr = t.grammar.productions[p]
            tx("prova_slr_reduce", l, "${pr.lhs} → ${pr.rhsText()} •", a, pr.lhs)
        }
        else -> tx("prova_slr_aceita")
    }

    /** As colunas de não-terminal: o GOTO são as setas do autômato com não-terminal. Uma casa vira pergunta. */
    private fun goto(t: LRTable, base: List<Box>, xy: (Int, Int) -> Pair<Float, Float>, cw: Float, ch: Float, tam: Float, n: Int): SimStep {
        val nts = t.nonTerminals
        val caixas = t.gotoTable.mapNotNull { (key, dest) ->
            val c = nts.indexOf(key.second).takeIf { it >= 0 } ?: return@mapNotNull null
            val (x, y) = xy(key.first + 1, t.terminals.size + c + 1)
            Box("g${key.first}-$c", "$dest", x, y, cw - 3f, ch - 3f, Look.TOKEN, Tint.BLUE, hot = true, size = tam)
        }
        val (primeira, dest) = t.gotoTable.entries.first { it.key.second in nts }.let { it.key to it.value }
        val (ops, idx) = opcoes("$dest", (0 until n).map { "$it" }.shuffled(kotlin.random.Random(n)), n)
        val diz = tx("prova_slr_goto", primeira.first, primeira.second, dest)
        return SimStep(Shot(base + caixas), diz, SimAsk(tx("prova_slr_goto_q", primeira.first, primeira.second), ops, idx, diz))
    }

    /** "É SLR(1)?": a errada que mais cai é achar que recursão à esquerda atrapalha o LR. */
    private fun veredito(t: LRTable, shot: Shot): SimStep {
        val sim = tx("prova_slr_sim")
        val rec = tx("prova_slr_nao_recursao")
        val c = t.conflicts.firstOrNull()
        val (certa, erradas, porque) = if (c == null) Triple(sim, listOf(rec, tx("prova_nao_vazia")), tx("prova_slr_porque_sim"))
        else Triple(tx("prova_slr_nao_casa", c.state, c.terminal), listOf(sim, rec), tx("prova_slr_porque_nao", c.state, c.terminal))
        val (ops, idx) = opcoes(certa, erradas, t.terminals.size)
        return SimStep(shot, porque, SimAsk(tx("prova_slr_pergunta"), ops, idx, porque), erro = c != null)
    }
}
