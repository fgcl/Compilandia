package com.compilandia.engine

/**
 * "MARS de bolso": um simulador MIPS-32 didático.
 *
 * Suporta: seções .data (.word, .asciiz, .space) e .text, rótulos, comentários (#),
 * li la lw sw move add addi addu addiu sub subu mul div(2 e 3 operandos) mflo mfhi rem
 * and or xor nor not neg slt slti sltu sgt sge sle seq sne
 * beq bne blt bgt ble bge beqz bnez b j jal jr nop syscall (1 int, 4 string, 10 exit, 11 char).
 * Memória por palavras; $sp começa no topo de uma pilha simulada.
 */
class MipsVM(source: String, private val maxSteps: Int = 20_000) {
    class AsmError(val line: Int, msg: String) : Exception(if (line >= 0) "linha ${line + 1}: $msg" else msg)

    data class Inst(val op: String, val args: List<String>, val line: Int, val text: String)

    data class Snapshot(val pc: Int, val regs: Map<String, Long>, val output: String, val lastLine: Int)

    val program = mutableListOf<Inst>()
    private val labels = HashMap<String, Int>()          // rótulo de código → índice da instrução
    val dataAddr = LinkedHashMap<String, Long>()         // rótulo de dados → endereço
    val mem = HashMap<Long, Long>()                       // endereço (múltiplo de 4) → palavra
    private val bytes = HashMap<Long, Int>()              // strings (.asciiz) byte a byte
    val regs = HashMap<String, Long>()
    var hi = 0L; var lo = 0L
    val output = StringBuilder()
    var pc = 0
    var steps = 0
    var halted = false
    val trace = mutableListOf<Snapshot>()

    companion object {
        const val DATA_BASE = 0x10010000L
        const val STACK_TOP = 0x7FFFEFFCL
        private val REG_ALIASES: Map<String, String> = buildMap {
            put("\$0", "\$zero"); put("\$1", "\$at"); put("\$2", "\$v0"); put("\$3", "\$v1")
            for (i in 0..3) put("\$${4 + i}", "\$a$i")
            for (i in 0..7) put("\$${8 + i}", "\$t$i")
            for (i in 0..7) put("\$${16 + i}", "\$s$i")
            put("\$24", "\$t8"); put("\$25", "\$t9"); put("\$29", "\$sp"); put("\$30", "\$fp"); put("\$31", "\$ra")
        }
    }

    init { assemble(source) }

    private fun assemble(src: String) {
        var inData = false
        var dataPtr = DATA_BASE
        src.lines().forEachIndexed { ln, raw ->
            var line = stripComment(raw).trim()
            if (line.isEmpty()) return@forEachIndexed
            if (line.startsWith(".data")) { inData = true; return@forEachIndexed }
            if (line.startsWith(".text")) { inData = false; return@forEachIndexed }
            if (line.startsWith(".globl") || line.startsWith(".global") || line.startsWith(".align")) return@forEachIndexed
            var label: String? = null
            val colon = labelColon(line)
            if (colon > 0) { label = line.substring(0, colon).trim(); line = line.substring(colon + 1).trim() }
            if (inData) {
                if (label != null) dataAddr[label] = dataPtr
                if (line.isEmpty()) return@forEachIndexed
                when {
                    line.startsWith(".word") -> {
                        for (v in line.removePrefix(".word").split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
                            mem[dataPtr] = parseImm(v, ln); dataPtr += 4
                        }
                    }
                    line.startsWith(".asciiz") || line.startsWith(".ascii") -> {
                        val s = unescape(line.substringAfter('"').substringBeforeLast('"'))
                        for (ch in s) { bytes[dataPtr] = ch.code; dataPtr++ }
                        if (line.startsWith(".asciiz")) { bytes[dataPtr] = 0; dataPtr++ }
                        while (dataPtr % 4 != 0L) dataPtr++
                    }
                    line.startsWith(".space") -> { dataPtr += parseImm(line.removePrefix(".space").trim(), ln); while (dataPtr % 4 != 0L) dataPtr++ }
                    else -> throw AsmError(ln, "diretiva de dados desconhecida: $line")
                }
            } else {
                if (label != null) labels[label] = program.size
                if (line.isEmpty()) return@forEachIndexed
                val op = line.substringBefore(' ').substringBefore('\t').lowercase()
                val rest = line.substring(op.length).trim()
                val args = splitArgs(rest)
                program += Inst(op, args, ln, line)
            }
        }
        regs["\$sp"] = STACK_TOP
        regs["\$gp"] = 0x10008000L
        val main = labels["main"]
        pc = main ?: 0
    }

    private fun labelColon(line: String): Int {
        // "rotulo:" no começo (fora de aspas)
        val q = line.indexOf('"')
        val c = line.indexOf(':')
        return if (c > 0 && (q < 0 || c < q) && line.substring(0, c).trim().matches(Regex("[A-Za-z_.][A-Za-z0-9_.]*"))) c else -1
    }

    private fun stripComment(s: String): String {
        var inQ = false
        for (i in s.indices) { if (s[i] == '"') inQ = !inQ; if (s[i] == '#' && !inQ) return s.substring(0, i) }
        return s
    }

    private fun unescape(s: String) = s.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\")

    private fun splitArgs(s: String): List<String> {
        if (s.isEmpty()) return emptyList()
        val out = mutableListOf<String>(); val cur = StringBuilder(); var inQ = false
        for (ch in s) {
            if (ch == '"') inQ = !inQ
            if (ch == ',' && !inQ) { out += cur.toString().trim(); cur.clear() } else cur.append(ch)
        }
        out += cur.toString().trim()
        return out.filter { it.isNotEmpty() }
    }

    private fun parseImm(s: String, ln: Int): Long {
        val t = s.trim()
        return when {
            t.startsWith("0x") || t.startsWith("-0x") -> (if (t.startsWith("-")) -1 else 1) * t.removePrefix("-").removePrefix("0x").toLong(16)
            t.length == 3 && t[0] == '\'' -> t[1].code.toLong()
            else -> t.toLongOrNull() ?: throw AsmError(ln, "número inválido: '$t'")
        }
    }

    private fun regName(r: String, ln: Int): String {
        val n = REG_ALIASES[r] ?: r
        if (!n.startsWith("\$")) throw AsmError(ln, "esperava um registrador, veio '$r'")
        return n
    }

    private fun get(r: String, ln: Int): Long { val n = regName(r, ln); return if (n == "\$zero") 0 else regs[n] ?: 0 }
    private fun set(r: String, v: Long, ln: Int) { val n = regName(r, ln); if (n != "\$zero") regs[n] = v.toInt().toLong() }

    /** Endereço de "rotulo", "8($sp)", "rotulo($t0)" ou "($t0)". */
    private fun address(a: String, ln: Int): Long {
        val m = Regex("^(-?[A-Za-z0-9_]*)\\((\\$[A-Za-z0-9]+)\\)$").find(a)
        if (m != null) {
            val base = get(m.groupValues[2], ln)
            val off = m.groupValues[1].let { if (it.isEmpty()) 0 else it.toLongOrNull() ?: dataAddr[it] ?: throw AsmError(ln, "rótulo '$it' não existe") }
            return base + off
        }
        return dataAddr[a] ?: throw AsmError(ln, "rótulo de dados '$a' não existe (declare em .data: $a: .word 0)")
    }

    private fun value(x: String, ln: Int): Long = if (x.startsWith("\$")) get(x, ln) else parseImm(x, ln)

    private fun jump(label: String, ln: Int) { pc = labels[label] ?: throw AsmError(ln, "rótulo '$label' não existe") }

    fun step(): Boolean {
        if (halted || pc >= program.size) { halted = true; return false }
        if (++steps > maxSteps) throw AsmError(-1, "mais de $maxSteps instruções: laço infinito?")
        val ins = program[pc]
        val a = ins.args; val ln = ins.line
        pc++
        fun need(n: Int) { if (a.size < n) throw AsmError(ln, "'${ins.op}' precisa de $n operandos") }
        when (ins.op) {
            "nop" -> {}
            "li" -> { need(2); set(a[0], parseImm(a[1], ln), ln) }
            "la" -> { need(2); set(a[0], address(a[1], ln), ln) }
            "lw" -> { need(2); val ad = address(a[1], ln); set(a[0], mem[ad] ?: 0, ln) }
            "sw" -> { need(2); mem[address(a[1], ln)] = get(a[0], ln) }
            "move" -> { need(2); set(a[0], get(a[1], ln), ln) }
            "add", "addu" -> { need(3); set(a[0], get(a[1], ln) + value(a[2], ln), ln) }
            "addi", "addiu" -> { need(3); set(a[0], get(a[1], ln) + parseImm(a[2], ln), ln) }
            "sub", "subu" -> { need(3); set(a[0], get(a[1], ln) - value(a[2], ln), ln) }
            "mul" -> { need(3); set(a[0], get(a[1], ln) * value(a[2], ln), ln) }
            "mult" -> { need(2); lo = get(a[0], ln) * get(a[1], ln); hi = 0 }
            "div" -> {
                if (a.size == 2) { val d = get(a[1], ln); if (d == 0L) throw AsmError(ln, "divisão por zero"); lo = get(a[0], ln) / d; hi = get(a[0], ln) % d }
                else { need(3); val d = value(a[2], ln); if (d == 0L) throw AsmError(ln, "divisão por zero"); set(a[0], get(a[1], ln) / d, ln) }
            }
            "rem" -> { need(3); val d = value(a[2], ln); if (d == 0L) throw AsmError(ln, "divisão por zero"); set(a[0], get(a[1], ln) % d, ln) }
            "mflo" -> set(a[0], lo, ln)
            "mfhi" -> set(a[0], hi, ln)
            "and", "andi" -> { need(3); set(a[0], get(a[1], ln) and value(a[2], ln), ln) }
            "or", "ori" -> { need(3); set(a[0], get(a[1], ln) or value(a[2], ln), ln) }
            "xor", "xori" -> { need(3); set(a[0], get(a[1], ln) xor value(a[2], ln), ln) }
            "nor" -> { need(3); set(a[0], (get(a[1], ln) or value(a[2], ln)).inv(), ln) }
            "not" -> { need(2); set(a[0], get(a[1], ln).inv(), ln) }
            "neg" -> { need(2); set(a[0], -get(a[1], ln), ln) }
            "slt", "slti", "sltu" -> { need(3); set(a[0], if (get(a[1], ln) < value(a[2], ln)) 1 else 0, ln) }
            "sgt" -> { need(3); set(a[0], if (get(a[1], ln) > value(a[2], ln)) 1 else 0, ln) }
            "sge" -> { need(3); set(a[0], if (get(a[1], ln) >= value(a[2], ln)) 1 else 0, ln) }
            "sle" -> { need(3); set(a[0], if (get(a[1], ln) <= value(a[2], ln)) 1 else 0, ln) }
            "seq" -> { need(3); set(a[0], if (get(a[1], ln) == value(a[2], ln)) 1 else 0, ln) }
            "sne" -> { need(3); set(a[0], if (get(a[1], ln) != value(a[2], ln)) 1 else 0, ln) }
            "beq" -> { need(3); if (get(a[0], ln) == value(a[1], ln)) jump(a[2], ln) }
            "bne" -> { need(3); if (get(a[0], ln) != value(a[1], ln)) jump(a[2], ln) }
            "blt" -> { need(3); if (get(a[0], ln) < value(a[1], ln)) jump(a[2], ln) }
            "bgt" -> { need(3); if (get(a[0], ln) > value(a[1], ln)) jump(a[2], ln) }
            "ble" -> { need(3); if (get(a[0], ln) <= value(a[1], ln)) jump(a[2], ln) }
            "bge" -> { need(3); if (get(a[0], ln) >= value(a[1], ln)) jump(a[2], ln) }
            "beqz" -> { need(2); if (get(a[0], ln) == 0L) jump(a[1], ln) }
            "bnez" -> { need(2); if (get(a[0], ln) != 0L) jump(a[1], ln) }
            "b", "j" -> { need(1); jump(a[0], ln) }
            "jal" -> { need(1); regs["\$ra"] = pc.toLong(); jump(a[0], ln) }
            "jr" -> { need(1); pc = get(a[0], ln).toInt(); if (pc < 0 || pc > program.size) throw AsmError(ln, "jr para endereço inválido") }
            "jalr" -> { need(1); regs["\$ra"] = pc.toLong(); pc = get(a[0], ln).toInt() }
            "syscall" -> when (get("\$v0", ln)) {
                1L -> output.append(get("\$a0", ln))
                4L -> { var p = get("\$a0", ln); while (true) { val b = bytes[p] ?: 0; if (b == 0) break; output.append(b.toChar()); p++ } }
                11L -> output.append(get("\$a0", ln).toInt().toChar())
                5L -> set("\$v0", 0, ln)          // read_int: o simulador lê sempre 0
                10L -> halted = true
                else -> throw AsmError(ln, "syscall ${get("\$v0", ln)} não suportada no simulador")
            }
            else -> throw AsmError(ln, "instrução '${ins.op}' não suportada no MARS de bolso")
        }
        trace += Snapshot(pc, HashMap(regs), output.toString(), ln)
        return !halted
    }

    fun run(): MipsVM { while (step()) { }; return this }

    /** Valor de uma variável declarada em .data. */
    fun word(label: String): Long? = dataAddr[label]?.let { mem[it] ?: 0 }
}

// ======================================================================
//  Minilinguagem com if/while → MIPS (para o jogo dos desvios)
// ======================================================================

object MiniLang {
    sealed class Stmt {
        data class Assign(val name: String, val expr: Expr) : Stmt()
        data class If(val cond: Expr, val then: List<Stmt>, val els: List<Stmt>) : Stmt()
        data class While(val cond: Expr, val body: List<Stmt>) : Stmt()
        data class Print(val expr: Expr) : Stmt()
    }

    /** Linha de assembly gerada; [blankable] = linhas de desvio que o jogo pode esconder. */
    data class AsmLine(val text: String, val blankable: Boolean = false, val why: String = "")

    fun parse(src: String): List<Stmt> {
        val toks = MiniLexer.tokenize(src).map { it.lexeme }
        var pos = 0
        fun peek() = toks.getOrNull(pos)
        fun expect(t: String) { require(peek() == t) { "esperava '$t' mas veio '${peek()}'" }; pos++ }
        fun exprUntil(stop: Set<String>): Expr {
            val start = pos; var depth = 0
            while (pos < toks.size && !(depth == 0 && toks[pos] in stop)) { if (toks[pos] == "(") depth++; if (toks[pos] == ")") depth--; pos++ }
            return ExprParser.parse(toks.subList(start, pos).joinToString(" "))
        }
        lateinit var block: () -> List<Stmt>
        fun stmt(): Stmt = when (peek()) {
            "if" -> { pos++; expect("("); val c = exprUntil(setOf(")")); expect(")"); val t = block(); val e = if (peek() == "else") { pos++; block() } else emptyList(); Stmt.If(c, t, e) }
            "while" -> { pos++; expect("("); val c = exprUntil(setOf(")")); expect(")"); Stmt.While(c, block()) }
            "print" -> { pos++; expect("("); val e = exprUntil(setOf(")")); expect(")"); expect(";"); Stmt.Print(e) }
            else -> { val n = peek()!!; pos++; expect("="); val e = exprUntil(setOf(";")); expect(";"); Stmt.Assign(n, e) }
        }
        block = { expect("{"); val out = mutableListOf<Stmt>(); while (peek() != "}") out += stmt(); expect("}"); out }
        val out = mutableListOf<Stmt>()
        while (pos < toks.size) out += stmt()
        return out
    }

    private val inverse = mapOf("<" to "bge", "<=" to "bgt", ">" to "ble", ">=" to "blt", "==" to "bne", "!=" to "beq")

    fun compile(src: String, initial: Map<String, Long> = emptyMap()): List<AsmLine> {
        val prog = parse(src)
        val vars = LinkedHashSet<String>(initial.keys)
        var temp = 0; var label = 0
        val text = mutableListOf<AsmLine>()
        fun collect(e: Expr) { if (e.kind == Expr.Kind.VAR) vars += e.text; e.children.forEach { collect(it) } }
        fun expr(e: Expr): String {   // devolve um registrador com o valor
            return when (e.kind) {
                Expr.Kind.NUM -> { val r = "\$t${temp++ % 8}"; text += AsmLine("li $r, ${e.text}"); r }
                Expr.Kind.VAR -> { vars += e.text; val r = "\$t${temp++ % 8}"; text += AsmLine("lw $r, ${e.text}"); r }
                Expr.Kind.BINOP -> {
                    val a = expr(e.children[0]); val b = expr(e.children[1])
                    val op = when (e.text) { "+" -> "add"; "-" -> "sub"; "*" -> "mul"; "/" -> "div"; "<" -> "slt"; ">" -> "sgt"; "<=" -> "sle"; ">=" -> "sge"; "==" -> "seq"; else -> "sne" }
                    text += AsmLine("$op $a, $a, $b"); a
                }
            }
        }
        fun cond(c: Expr, falseLabel: String) {
            require(c.kind == Expr.Kind.BINOP && c.text in inverse) { "condição precisa ser uma comparação" }
            val a = expr(c.children[0]); val b = expr(c.children[1])
            text += AsmLine("${inverse.getValue(c.text)} $a, $b, $falseLabel", true, "se a condição (${c.render()}) for FALSA, pula para $falseLabel — por isso o desvio usa o teste INVERSO de '${c.text}'")
        }
        fun stmts(list: List<Stmt>) {
            for (s in list) when (s) {
                is Stmt.Assign -> { vars += s.name; temp = 0; val r = expr(s.expr); text += AsmLine("sw $r, ${s.name}") }
                is Stmt.Print -> { temp = 0; val r = expr(s.expr); text += AsmLine("move \$a0, $r"); text += AsmLine("li \$v0, 1"); text += AsmLine("syscall"); text += AsmLine("li \$a0, 10"); text += AsmLine("li \$v0, 11"); text += AsmLine("syscall") }
                is Stmt.If -> {
                    val n = label++; val lElse = "senao$n"; val lEnd = "fimse$n"
                    temp = 0; cond(s.cond, if (s.els.isEmpty()) lEnd else lElse)
                    stmts(s.then)
                    if (s.els.isNotEmpty()) {
                        text += AsmLine("j $lEnd", true, "terminou o ENTÃO: pula o SENÃO")
                        text += AsmLine("$lElse:")
                        stmts(s.els)
                    }
                    text += AsmLine("$lEnd:")
                }
                is Stmt.While -> {
                    val n = label++; val lTop = "enquanto$n"; val lEnd = "fimenq$n"
                    text += AsmLine("$lTop:")
                    temp = 0; cond(s.cond, lEnd)
                    stmts(s.body)
                    text += AsmLine("j $lTop", true, "fim do corpo: volta para testar a condição de novo")
                    text += AsmLine("$lEnd:")
                }
            }
        }
        prog.forEach { st -> if (st is Stmt.Assign) collect(st.expr) }
        stmts(prog)
        val data = listOf(AsmLine(".data")) + vars.map { AsmLine("$it: .word ${initial[it] ?: 0}") } + listOf(AsmLine(".text"), AsmLine("main:"))
        return data + text + listOf(AsmLine("li \$v0, 10"), AsmLine("syscall"))
    }

    fun asmText(lines: List<AsmLine>) = lines.joinToString("\n") { if (it.text.endsWith(":") || it.text.startsWith(".")) it.text else "    " + it.text }

    val samples = listOf(
        "x = 0;\nif (a < b) { x = 1; } else { x = 2; }",
        "m = a;\nif (b > m) { m = b; }\nprint(m);",
        "i = 0;\ns = 0;\nwhile (i < 4) { s = s + i; i = i + 1; }\nprint(s);",
        "f = 1;\nn = 4;\nwhile (n > 1) { f = f * n; n = n - 1; }\nprint(f);",
        "x = 10;\nwhile (x != 0) { x = x - 3; if (x < 0) { x = 0; } }\nprint(x);",
    )
    val sampleEnv = mapOf("a" to 4L, "b" to 7L)
}
