package com.compilandia.engine

import com.compilandia.textos.tx

/**
 * Alocação de registradores por COLORAÇÃO DE GRAFOS (ideia de Chaitin), num bloco básico de TAC:
 *  1. análise de vivacidade (de trás para frente): in = use ∪ (out − def);
 *  2. grafo de interferência: quem é definido numa instrução interfere com tudo vivo depois dela;
 *  3. colorir com k registradores (simplifica nós de grau < k; se travar, derrama/spill).
 * Alocamos os temporários (t1, t2, …); variáveis do programa continuam na memória.
 */
object RegAlloc {
    fun isTemp(x: String?) = x != null && x.length >= 2 && x[0] == 't' && x.drop(1).all { it.isDigit() }

    data class Liveness(val liveIn: List<Set<String>>, val liveOut: List<Set<String>>)

    fun liveness(code: List<TacGen.Instr>): Liveness {
        val n = code.size
        val ins = MutableList(n) { emptySet<String>() }
        val outs = MutableList(n) { emptySet<String>() }
        var live = emptySet<String>()
        for (i in n - 1 downTo 0) {
            outs[i] = live
            val c = code[i]
            val use = listOfNotNull(c.a, c.b).filter { isTemp(it) }.toSet()
            val def = if (isTemp(c.target)) setOf(c.target) else emptySet()
            live = use + (live - def)
            ins[i] = live
        }
        return Liveness(ins, outs)
    }

    fun temps(code: List<TacGen.Instr>): List<String> =
        code.flatMap { listOfNotNull(it.target, it.a, it.b) }.filter { isTemp(it) }.distinct()

    fun interference(code: List<TacGen.Instr>): Map<String, Set<String>> {
        val lv = liveness(code)
        val g = temps(code).associateWith { mutableSetOf<String>() }
        code.forEachIndexed { i, c ->
            if (isTemp(c.target)) for (o in lv.liveOut[i]) if (o != c.target) { g.getValue(c.target) += o; g.getValue(o) += c.target }
        }
        return g
    }

    data class Allocation(val colors: Map<String, Int>, val spilled: Set<String>, val order: List<String>)

    /** Coloração por simplificação (pilha) + seleção. Devolve cores 0..k-1 e os derramados. */
    fun color(graph: Map<String, Set<String>>, k: Int): Allocation {
        val g = graph.mapValues { it.value.toMutableSet() }.toMutableMap()
        val stack = ArrayDeque<String>()
        val spilled = mutableSetOf<String>()
        while (g.isNotEmpty()) {
            val easy = g.keys.sorted().firstOrNull { g.getValue(it).size < k }
            val pick = easy ?: g.keys.maxBy { g.getValue(it).size }   // candidato a spill
            stack.addLast(pick)
            for (nb in g.getValue(pick)) g[nb]?.remove(pick)
            g.remove(pick)
        }
        val colors = HashMap<String, Int>()
        val order = mutableListOf<String>()
        while (stack.isNotEmpty()) {
            val v = stack.removeLast()
            val used = graph.getValue(v).mapNotNull { colors[it] }.toSet()
            val c = (0 until k).firstOrNull { it !in used }
            if (c == null) spilled += v else colors[v] = c
            order += v
        }
        return Allocation(colors, spilled, order)
    }

    /** Menor k que colore sem spill. */
    fun chromatic(graph: Map<String, Set<String>>): Int =
        (1..graph.size.coerceAtLeast(1)).first { color(graph, it).spilled.isEmpty() }

    fun isValidColoring(graph: Map<String, Set<String>>, colors: Map<String, Int>): Boolean =
        graph.keys.all { it in colors } && graph.all { (v, nbs) -> nbs.all { colors[it] != colors[v] } }

    /** MIPS usando $s0..$s(k-1) para os temporários e $t8/$t9 como rascunho. */
    fun mips(code: List<TacGen.Instr>, alloc: Allocation): List<String> {
        val out = mutableListOf<String>()
        fun reg(x: String) = "\$s${alloc.colors.getValue(x)}"
        fun operand(x: String, scratch: String): String = when {
            isTemp(x) && x in alloc.colors -> reg(x)
            x.first().isDigit() -> { out += "li $scratch, $x"; scratch }
            else -> { out += "lw $scratch, $x"; scratch }
        }
        for (c in code) {
            if (c.op == null) {
                val r = operand(c.a, "\$t8")
                if (isTemp(c.target) && c.target in alloc.colors) out += tx("regalloc_move_1_2", reg(c.target), r) else out += "sw $r, ${c.target}"
            } else {
                val a = operand(c.a, "\$t8"); val b = operand(c.b!!, "\$t9")
                val op = when (c.op) { "+" -> "add"; "-" -> "sub"; "*" -> "mul"; else -> "div" }
                if (isTemp(c.target) && c.target in alloc.colors) out += "$op ${reg(c.target)}, $a, $b"
                else { out += "$op \$t8, $a, $b"; out += "sw \$t8, ${c.target}" }
            }
        }
        return out
    }

    fun memoryAccesses(asm: List<String>) = asm.count { it.startsWith("lw ") || it.startsWith("sw ") }
}
