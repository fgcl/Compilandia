package com.compilandia.engine

import com.compilandia.textos.tx

// ======================================================================
//  Recuperação de erros (modo pânico) no analisador LL(1)
// ======================================================================

data class RecoveryEvent(val position: Int, val message: String, val skipped: List<String>, val popped: String?)

data class RecoveryTrace(val steps: List<LLStep>, val events: List<RecoveryEvent>, val reachedEnd: Boolean)

object PanicMode {
    /**
     * Modo pânico (Dragão, 4.4.5): com o não-terminal A no topo e M[A,a] vazia,
     * descarta tokens até achar um em FIRST(A) (continua) ou em FOLLOW(A) ∪ {$} (desempilha A).
     * Terminal no topo diferente da entrada: desempilha (finge que ele estava lá).
     */
    fun trace(t: LL1Table, input: List<String>, maxSteps: Int = 600): RecoveryTrace {
        val g = t.grammar
        val stack = mutableListOf(EOF, g.start)
        val inp = input + EOF
        var pos = 0
        val steps = mutableListOf<LLStep>()
        val events = mutableListOf<RecoveryEvent>()
        while (steps.size < maxSteps) {
            val top = stack.last(); val a = inp[pos]
            if (top == EOF && a == EOF) { steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Accept); return RecoveryTrace(steps, events, true) }
            if (top == EOF) {
                events += RecoveryEvent(pos, tx("backend_sobrou_entrada_depois_fim_programa"), inp.subList(pos, inp.size - 1), null)
                steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Error(tx("backend_sobrou_entrada")))
                return RecoveryTrace(steps, events, true)
            }
            if (!g.isNonTerminal(top)) {
                if (top == a) { steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Match(a)); stack.removeAt(stack.lastIndex); pos++ }
                else {
                    events += RecoveryEvent(pos, tx("backend_esperava_1_mas_veio_2", top, a, top), emptyList(), top)
                    steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Error(tx("backend_esperava_1", top)))
                    stack.removeAt(stack.lastIndex)
                }
                continue
            }
            val cell = t.cell(top, a)
            if (cell.isNotEmpty()) {
                steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Expand(cell.first()))
                stack.removeAt(stack.lastIndex)
                for (s in cell.first().rhs.asReversed()) stack += s
                continue
            }
            // erro com não-terminal no topo: pânico
            val first = t.ff.first.getValue(top) - EPS
            val sync = t.ff.follow.getValue(top) + EOF
            val skipped = mutableListOf<String>()
            steps += LLStep(stack.toList(), inp.drop(pos), LLAction.Error(tx("backend_m_1_2_vazia", top, a)))
            while (inp[pos] !in first && inp[pos] !in sync) { skipped += inp[pos]; pos++ }
            if (inp[pos] in first) {
                events += RecoveryEvent(pos, tx("backend_erro_em_1_descartei_2", top, skipped.ifEmpty { listOf("nada") }.joinToString(" "), inp[pos], top), skipped, null)
            } else {
                events += RecoveryEvent(pos, tx("backend_erro_em_1_descartei_2_2", top, skipped.ifEmpty { listOf("nada") }.joinToString(" "), inp[pos], top, top), skipped, top)
                stack.removeAt(stack.lastIndex)
            }
        }
        return RecoveryTrace(steps, events, false)
    }

    /** Posição (índice do token) onde o analisador LL(1) DETECTA o primeiro erro, ou null. */
    fun firstErrorPosition(t: LL1Table, input: List<String>): Int? {
        val tr = LL1Parser.trace(t, input)
        val last = tr.last()
        if (last.action !is LLAction.Error) return null
        return input.size + 1 - last.input.size
    }
}

// ======================================================================
//  Ambientes de execução: registros de ativação
// ======================================================================

data class Frame(val fn: String, val args: Map<String, Int>, val locals: Map<String, Int>, val returnTo: String)

sealed class RtEvent {
    abstract val stack: List<Frame>    // pilha DEPOIS do evento
    data class Call(val frame: Frame, override val stack: List<Frame>) : RtEvent()
    data class Return(val fn: String, val value: Int, override val stack: List<Frame>) : RtEvent()
}

data class RtProgram(val id: String, val title: String, val code: String, val events: List<RtEvent>, val result: Int)

object RuntimeSim {
    private class Rec { val events = mutableListOf<RtEvent>(); val stack = mutableListOf<Frame>() }

    private fun Rec.call(fn: String, args: Map<String, Int>, ret: String, body: () -> Int): Int {
        val f = Frame(fn, args, emptyMap(), ret)
        stack += f; events += RtEvent.Call(f, stack.toList())
        val v = body()
        stack.removeAt(stack.lastIndex); events += RtEvent.Return(fn, v, stack.toList())
        return v
    }

    fun fatorial(n: Int): RtProgram {
        val r = Rec()
        fun fat(k: Int, ret: String): Int = r.call("fat", mapOf("n" to k), ret) { if (k <= 1) 1 else k * fat(k - 1, tx("backend_fat_linha_3")) }
        val v = r.call("main", emptyMap(), "SO") { fat(n, tx("backend_main_linha_6")) }
        return RtProgram("fat$n", tx("backend_fatorial_recursivo_1", n),
            "1 int fat(int n) {\n2   if (n <= 1) return 1;\n3   return n * fat(n - 1);\n4 }\n5 int main() {\n6   return fat($n);\n7 }", r.events, v)
    }

    fun fib(n: Int): RtProgram {
        val r = Rec()
        fun fib(k: Int, ret: String): Int = r.call("fib", mapOf("n" to k), ret) { if (k < 2) k else fib(k - 1, tx("backend_fib_linha_3a")) + fib(k - 2, tx("backend_fib_linha_3b")) }
        val v = r.call("main", emptyMap(), "SO") { fib(n, tx("backend_main_linha_6")) }
        return RtProgram("fib$n", tx("backend_fibonacci_recursivo_1", n),
            "1 int fib(int n) {\n2   if (n < 2) return n;\n3   return fib(n-1) + fib(n-2);\n4 }\n5 int main() {\n6   return fib($n);\n7 }", r.events, v)
    }

    fun chain(x: Int): RtProgram {
        val r = Rec()
        fun h(a: Int, ret: String) = r.call("h", mapOf("a" to a), ret) { a + 1 }
        fun g(b: Int, ret: String) = r.call("g", mapOf("b" to b), ret) { h(b * 2, tx("backend_g_linha_5")) }
        fun f(c: Int, ret: String) = r.call("f", mapOf("c" to c), ret) { g(c, tx("backend_f_linha_8")) + h(c, tx("backend_f_linha_8b")) }
        val v = r.call("main", emptyMap(), "SO") { f(x, tx("backend_main_linha_11")) }
        return RtProgram("chain$x", tx("backend_f_chama_g_que_chama"),
            "1 int h(int a) {\n2   return a + 1;\n3 }\n4 int g(int b) {\n5   return h(b * 2);\n6 }\n7 int f(int c) {\n8   return g(c) + h(c);\n9 }\n10 int main() {\n11  return f($x);\n12 }", r.events, v)
    }

    fun forLevel(level: Int, seed: Int): RtProgram = when (level) {
        1 -> chain(1 + seed % 3)
        2 -> fatorial(3)
        3 -> listOf(fatorial(4), chain(2))[seed % 2]
        else -> listOf(fib(3), fatorial(5))[seed % 2]
    }
}

// ======================================================================
//  Geração de código MIPS a partir do TAC + simulador ("MARS de bolso")
// ======================================================================

object Mips {
    /**
     * Estilo do trabalho prático: cada variável/temporário mora na memória (.data);
     * cada instrução TAC vira  lw / lw / op / sw.
     */
    fun fromTac(code: List<TacGen.Instr>): List<String> {
        val out = mutableListOf<String>()
        fun load(reg: String, x: String) {
            out += if (x.first().isDigit() || x.first() == '-') "li $reg, $x" else "lw $reg, $x"
        }
        for (ins in code) {
            if (ins.op == null) {
                load("\$t0", ins.a); out += "sw \$t0, ${ins.target}"
            } else {
                load("\$t1", ins.a); load("\$t2", ins.b!!)
                val op = when (ins.op) { "+" -> "add"; "-" -> "sub"; "*" -> "mul"; "/" -> "div"; else -> "?" }
                out += "$op \$t3, \$t1, \$t2"
                out += "sw \$t3, ${ins.target}"
            }
        }
        return out
    }

    data class State(val regs: Map<String, Long>, val mem: Map<String, Long>)

    /** Executa li/lw/sw/add/sub/mul/div/move. Devolve o estado após cada instrução. */
    fun run(program: List<String>, initialMem: Map<String, Long>): List<State> {
        val regs = mutableMapOf<String, Long>()
        val mem = initialMem.toMutableMap()
        val states = mutableListOf<State>()
        for (line in program) {
            val p = line.trim().split(Regex("[ ,]+")).filter { it.isNotEmpty() }
            fun r(x: String) = regs[x] ?: 0L
            when (p[0]) {
                "li" -> regs[p[1]] = p[2].toLong()
                "lw" -> regs[p[1]] = mem[p[2]] ?: error(tx("solta_backend_1", p[2]))
                "sw" -> mem[p[2]] = r(p[1])
                "add" -> regs[p[1]] = r(p[2]) + r(p[3])
                "sub" -> regs[p[1]] = r(p[2]) - r(p[3])
                "mul" -> regs[p[1]] = r(p[2]) * r(p[3])
                "div" -> regs[p[1]] = if (r(p[3]) == 0L) error("divisão por zero") else r(p[2]) / r(p[3])
                "move" -> regs[p[1]] = r(p[2])
                else -> error("instrução desconhecida: ${p[0]}")
            }
            states += State(regs.toMap(), mem.toMap())
        }
        return states
    }

    val intEnv: Map<String, Long> = mapOf("a" to 4, "b" to 2, "c" to 3, "d" to 5)
}

// ======================================================================
//  Descida recursiva: gera o código (estilo trabalho prático) de uma gramática LL(1)
// ======================================================================

data class CodeLine(val indent: Int, val text: String)

object RecursiveDescentGen {
    fun fnName(nt: String) = nt.replace("'", "_")

    fun function(t: LL1Table, nt: String): List<CodeLine> {
        val g = t.grammar
        val out = mutableListOf(CodeLine(0, "void ${fnName(nt)}() {"))
        val prods = g.productionsOf(nt)
        fun body(p: Production, ind: Int) {
            if (p.rhs.isEmpty()) out += CodeLine(ind, tx("backend_nao_consome_nada"))
            for (s in p.rhs) out += CodeLine(ind, if (g.isNonTerminal(s)) "${fnName(s)}();" else "casaToken(\"$s\");")
        }
        if (prods.size == 1) body(prods[0], 1)
        else {
            val nonEps = prods.filter { it.rhs.isNotEmpty() }
            val eps = prods.firstOrNull { it.rhs.isEmpty() }
            nonEps.forEachIndexed { i, p ->
                val f = (t.ff.firstOfString(p.rhs) - EPS).sorted()
                val cond = f.joinToString(" || ") { "tok == \"$it\"" }
                out += CodeLine(1, (if (i == 0) "if (" else "} else if (") + cond + ") {")
                body(p, 2)
            }
            if (eps != null) {
                out += CodeLine(1, "} else {")
                out += CodeLine(2, tx("backend_nao_consome_nada_tok_deve", nt))
                out += CodeLine(1, "}")
            } else {
                out += CodeLine(1, "} else {")
                out += CodeLine(2, "erro(\"esperava ${nonEps.flatMap { t.ff.firstOfString(it.rhs) - EPS }.distinct().sorted().joinToString(" ou ")}\");")
                out += CodeLine(1, "}")
            }
        }
        out += CodeLine(0, "}")
        return out
    }

    fun program(t: LL1Table): List<CodeLine> =
        listOf(CodeLine(0, "void casaToken(String esperado) {"), CodeLine(1, "if (tok == esperado) tok = proximoToken();"), CodeLine(1, "else erro(\"esperava \" + esperado);"), CodeLine(0, "}")) +
            t.grammar.nonTerminals.flatMap { function(t, it) + CodeLine(0, "") }

    /** Pilha de chamadas da descida recursiva em cada passo (derivada do traço LL(1)). */
    fun callTrace(t: LL1Table, input: List<String>): List<Pair<List<String>, String>> {
        val tr = LL1Parser.trace(t, input)
        val out = mutableListOf<Pair<List<String>, String>>()
        // Reconstrói a pilha de chamadas: cada Expand empilha uma chamada; quando o lado direito é consumido, ela retorna.
        data class Fr(val name: String, var remaining: Int)
        val calls = mutableListOf<Fr>()
        fun popFinished() {
            while (calls.isNotEmpty() && calls.last().remaining == 0) {
                val f = calls.removeAt(calls.lastIndex)
                out += calls.map { it.name } to tx("backend_return_1", f.name)
                if (calls.isNotEmpty()) calls.last().remaining--
            }
        }
        for (s in tr) {
            when (val a = s.action) {
                is LLAction.Expand -> {
                    calls += Fr(fnName(a.production.lhs), a.production.rhs.size)
                    out += calls.map { it.name } to tx("backend_chama_1", fnName(a.production.lhs))
                    popFinished()
                }
                is LLAction.Match -> {
                    out += calls.map { it.name } to "casaToken(\"${a.terminal}\")"
                    if (calls.isNotEmpty()) calls.last().remaining--
                    popFinished()
                }
                LLAction.Accept -> out += emptyList<String>() to tx("backend_fim_tok_aceito")
                is LLAction.Error -> out += calls.map { it.name } to tx("comum_erro_1", a.message)
            }
        }
        return out
    }
}
