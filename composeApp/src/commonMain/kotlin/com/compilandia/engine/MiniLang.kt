package com.compilandia.engine

import com.compilandia.textos.tx

/*
 * Minilinguagem com if/while → TAC e MIPS (jogo dos desvios, aula de MIPS, aulas de texto).
 * Um esquema de desvio só, o mesmo do Guia do TP (professor do bloco 5):
 *   TAC:  t1 = a < b          MIPS:  slt $t0, $t0, $t1
 *         ifFalse t1 goto L          beq $t0, $zero, L
 * A condição vira 0 ou 1, e o desvio pula quando ela é falsa.
 * && e || têm curto-circuito: a segunda comparação só roda se a primeira não decidir.
 */
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
        fun expect(t: String) { require(peek() == t) { tx("mipsvm_esperava_1_mas_veio_2", t, peek()) }; pos++ }
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

    private val instrucao = mapOf(
        "+" to "add", "-" to "sub", "*" to "mul", "/" to "div",
        "<" to "slt", ">" to "sgt", "<=" to "sle", ">=" to "sge", "==" to "seq", "!=" to "sne",
    )

    /** Rótulos com o mesmo nome no TAC e no MIPS, para as duas listagens ficarem lado a lado. */
    private class Rotulos {
        private var n = 0
        fun se() = n++.let { "senao$it" to "fimse$it" }
        fun enquanto() = n++.let { "enquanto$it" to "fimenq$it" }
        fun entra() = "entra${n++}"
    }

    private class Mips(val vars: LinkedHashSet<String>) {
        val text = mutableListOf<AsmLine>()
        val rot = Rotulos()
        var temp = 0
        fun add(t: String, blank: Boolean = false, why: String = "") { text += AsmLine(t, blank, why) }
        fun reg() = "\$t${temp++ % 8}"

        fun expr(e: Expr): String = when (e.kind) {
            Expr.Kind.NUM -> reg().also { add("li $it, ${e.text}") }
            Expr.Kind.VAR -> { vars += e.text; reg().also { add("lw $it, ${e.text}") } }
            Expr.Kind.BINOP -> {
                val a = expr(e.children[0]); val b = expr(e.children[1])
                add("${instrucao.getValue(e.text)} $a, $a, $b"); a
            }
        }

        /** Pula para [falso] quando [c] é falsa. */
        fun cond(c: Expr, falso: String) {
            when (c.text) {
                "&&" -> { cond(c.children[0], falso); cond(c.children[1], falso) }
                "||" -> { val dentro = rot.entra(); condVerdade(c.children[0], dentro); cond(c.children[1], falso); add("$dentro:") }
                else -> {
                    require(c.kind == Expr.Kind.BINOP && c.text in TypeChecker.rel) { tx("mipsvm_condicao_precisa_ser_comparacao") }
                    temp = 0; val r = expr(c)
                    add("beq $r, \$zero, $falso", true, tx("mipsvm_se_condicao_1_for_falsa", c.render(), falso))
                }
            }
        }

        /** Lado esquerdo do ||: verdadeiro já basta, e pula para dentro do corpo. */
        fun condVerdade(c: Expr, dentro: String) {
            require(c.kind == Expr.Kind.BINOP && c.text in TypeChecker.rel) { tx("mipsvm_condicao_precisa_ser_comparacao") }
            temp = 0; val r = expr(c)
            add("bne $r, \$zero, $dentro", true, tx("mipsvm_se_condicao_verdadeira", c.render(), dentro))
        }

        fun stmts(list: List<Stmt>) { for (s in list) stmt(s) }

        fun stmt(s: Stmt) {
            when (s) {
                is Stmt.Assign -> { vars += s.name; temp = 0; add("sw ${expr(s.expr)}, ${s.name}") }
                is Stmt.Print -> { temp = 0; add("move \$a0, ${expr(s.expr)}"); listOf("li \$v0, 1", "syscall", "li \$a0, 10", "li \$v0, 11", "syscall").forEach { add(it) } }
                is Stmt.If -> {
                    val (senao, fim) = rot.se()
                    cond(s.cond, if (s.els.isEmpty()) fim else senao)
                    stmts(s.then)
                    if (s.els.isNotEmpty()) { add("j $fim", true, tx("mipsvm_terminou_entao_pula_senao")); add("$senao:"); stmts(s.els) }
                    add("$fim:")
                }
                is Stmt.While -> {
                    val (topo, fim) = rot.enquanto()
                    add("$topo:"); cond(s.cond, fim); stmts(s.body)
                    add("j $topo", true, tx("mipsvm_fim_corpo_volta_para_testar")); add("$fim:")
                }
            }
        }
    }

    fun compile(src: String, initial: Map<String, Long> = emptyMap()): List<AsmLine> {
        val g = Mips(LinkedHashSet(initial.keys))
        g.stmts(parse(src))
        val data = listOf(AsmLine(".data")) + g.vars.map { AsmLine(tx("mipsvm_1_word_2", it, initial[it] ?: 0)) } + listOf(AsmLine(".text"), AsmLine("main:"))
        return data + g.text + listOf(AsmLine("li \$v0, 10"), AsmLine("syscall"))
    }

    /** Só o corpo do main, sem .data, sem a saída e com o rótulo encostado à esquerda: o que cabe no palco. */
    fun corpo(src: String): List<String> = compile(src).map { it.text }.let { l -> l.subList(l.indexOf("main:") + 1, l.size - 2) }

    /** O mesmo programa em TAC, com os rótulos do MIPS. */
    fun tac(src: String): List<String> {
        val out = mutableListOf<String>()
        val rot = Rotulos()
        var t = 0
        fun gen(e: Expr): String {
            if (e.isLeaf) return e.text
            val a = gen(e.children[0]); val b = gen(e.children[1])
            return "t${++t}".also { out += "$it = $a ${e.text} $b" }
        }
        fun cond(c: Expr, falso: String) {
            when (c.text) {
                "&&" -> { cond(c.children[0], falso); cond(c.children[1], falso) }
                "||" -> { val dentro = rot.entra(); out += "if ${gen(c.children[0])} goto $dentro"; cond(c.children[1], falso); out += "$dentro:" }
                else -> out += "ifFalse ${gen(c)} goto $falso"
            }
        }
        lateinit var stmts: (List<Stmt>) -> Unit
        fun stmt(s: Stmt) {
            when (s) {
                is Stmt.Assign -> out += "${s.name} = ${gen(s.expr)}"
                is Stmt.Print -> out += "print ${gen(s.expr)}"
                is Stmt.If -> {
                    val (senao, fim) = rot.se()
                    cond(s.cond, if (s.els.isEmpty()) fim else senao); stmts(s.then)
                    if (s.els.isNotEmpty()) { out += "goto $fim"; out += "$senao:"; stmts(s.els) }
                    out += "$fim:"
                }
                is Stmt.While -> { val (topo, fim) = rot.enquanto(); out += "$topo:"; cond(s.cond, fim); stmts(s.body); out += "goto $topo"; out += "$fim:" }
            }
        }
        stmts = { l -> l.forEach { stmt(it) } }
        stmts(parse(src))
        return out
    }

    fun asmText(lines: List<AsmLine>) = lines.joinToString("\n") { if (it.text.endsWith(":") || it.text.startsWith(".")) it.text else "    " + it.text }

    val samples = listOf(
        "x = 0;\nif (a < b) { x = 1; } else { x = 2; }",
        "m = a;\nif (b > m) { m = b; }\nprint(m);",
        "i = 0;\ns = 0;\nwhile (i < 4) { s = s + i; i = i + 1; }\nprint(s);",
        "f = 1;\nn = 4;\nwhile (n > 1) { f = f * n; n = n - 1; }\nprint(f);",
        "x = 10;\nwhile (x != 0) { x = x - 3; if (x < 0) { x = 0; } }\nprint(x);",
        "x = 0;\nif (a < b && b < 9) { x = 1; }",
    )
    val sampleEnv = mapOf("a" to 4L, "b" to 7L)
}
