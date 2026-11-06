package com.compilandia.engine

import kotlin.random.Random
import com.compilandia.textos.tx

enum class Type(val label: String) { INT("int"), FLOAT("float"), BOOL("bool"), ERROR("erro") }

/** Nó de AST para expressões com atributos sintetizados (tipo e valor). */
data class Expr(
    val id: Int,
    val kind: Kind,
    val text: String,              // operador, nome ou literal
    val children: List<Expr> = emptyList(),
) {
    enum class Kind { NUM, VAR, BINOP }
    val isLeaf get() = children.isEmpty()
    fun postorder(): List<Expr> = children.flatMap { it.postorder() } + this
    fun render(): String = when (kind) {
        Kind.BINOP -> "(${children[0].render()} $text ${children[1].render()})"
        else -> text
    }
}

data class Attr(val type: Type, val value: Double?, val coerced: Boolean, val rule: String)

/**
 * Definição dirigida pela sintaxe (SDD) para expressões:
 *   E.tipo  = max(E1.tipo, E2.tipo)   (int < float), com coerção inttofloat
 *   E.valor = E1.valor op E2.valor
 * Todos os atributos são SINTETIZADOS → avaliáveis em pós-ordem (S-atribuída).
 */
object TypeChecker {
    val arith = setOf("+", "-", "*", "/")
    val rel = setOf("<", ">", "==", "<=", ">=", "!=")
    val logic = setOf("&&", "||")

    fun evaluate(root: Expr, env: Map<String, Pair<Type, Double>>): Map<Int, Attr> {
        val out = HashMap<Int, Attr>()
        fun go(e: Expr): Attr {
            val a = when (e.kind) {
                Expr.Kind.NUM -> {
                    val t = if (e.text.contains('.')) Type.FLOAT else Type.INT
                    Attr(t, e.text.toDouble(), false, tx("semantics_literal_1_2", e.text, t.label))
                }
                Expr.Kind.VAR -> {
                    val v = env[e.text]
                    if (v == null) Attr(Type.ERROR, null, false, tx("semantics_1_nao_foi_declarada", e.text))
                    else Attr(v.first, v.second, false, tx("semantics_busca_1_na_tabela_simbolos", e.text, v.first.label))
                }
                Expr.Kind.BINOP -> {
                    val l = go(e.children[0]); val r = go(e.children[1])
                    combine(e.text, l, r)
                }
            }
            out[e.id] = a
            return a
        }
        go(root)
        return out
    }

    fun combine(op: String, l: Attr, r: Attr): Attr {
        if (l.type == Type.ERROR || r.type == Type.ERROR) return Attr(Type.ERROR, null, false, tx("semantics_erro_se_propaga_para_cima"))
        return when (op) {
            in arith -> {
                if (l.type == Type.BOOL || r.type == Type.BOOL) return Attr(Type.ERROR, null, false, tx("semantics_nao_para_fazer_1_com", op))
                val t = if (l.type == Type.FLOAT || r.type == Type.FLOAT) Type.FLOAT else Type.INT
                val coerced = t == Type.FLOAT && (l.type == Type.INT || r.type == Type.INT)
                val lv = l.value!!; val rv = r.value!!
                val v = when (op) {
                    "+" -> lv + rv; "-" -> lv - rv; "*" -> lv * rv
                    else -> if (rv == 0.0) return Attr(Type.ERROR, null, false, tx("semantics_divisao_por_zero_erro_em"))
                    else if (t == Type.INT) (lv.toLong() / rv.toLong()).toDouble() else lv / rv
                }
                val why = if (coerced) tx("semantics_int_misturado_com_float_converte") else "${l.type.label} $op ${r.type.label} → ${t.label}"
                Attr(t, v, coerced, why)
            }
            in rel -> {
                if (l.type == Type.BOOL || r.type == Type.BOOL) return Attr(Type.ERROR, null, false, tx("semantics_comparacao_1_exige_numeros", op))
                val lv = l.value!!; val rv = r.value!!
                val b = when (op) { "<" -> lv < rv; ">" -> lv > rv; "<=" -> lv <= rv; ">=" -> lv >= rv; "==" -> lv == rv; else -> lv != rv }
                Attr(Type.BOOL, if (b) 1.0 else 0.0, false, tx("semantics_comparacao_sempre_produz_bool"))
            }
            in logic -> {
                if (l.type != Type.BOOL || r.type != Type.BOOL) return Attr(Type.ERROR, null, false, tx("semantics_1_exige_dois_bool", op))
                val b = if (op == "&&") l.value!! != 0.0 && r.value!! != 0.0 else l.value!! != 0.0 || r.value!! != 0.0
                Attr(Type.BOOL, if (b) 1.0 else 0.0, false, tx("semantics_logica_entre_bool_bool"))
            }
            else -> Attr(Type.ERROR, null, false, tx("semantics_operador_desconhecido"))
        }
    }

    fun formatValue(a: Attr): String = when {
        a.value == null -> "—"
        a.type == Type.BOOL -> if (a.value != 0.0) "true" else "false"
        a.type == Type.INT -> a.value.toLong().toString()
        else -> {
            val v = (a.value * 100).let { kotlin.math.round(it) } / 100
            v.toString()
        }
    }
}

/** Analisador descendente recursivo de expressões — o mesmo que você escreveria na prova! */
object ExprParser {
    private var nextId = 0

    fun parse(src: String): Expr {
        val toks = MiniLexer.tokenize(src).map { it.lexeme }
        var pos = 0
        fun peek() = toks.getOrNull(pos)
        fun id() = nextId++
        lateinit var orExpr: () -> Expr
        fun primary(): Expr {
            val t = peek() ?: error("Expressão incompleta")
            pos++
            return when {
                t == "(" -> { val e = orExpr(); require(peek() == ")") { tx("comum_falta") }; pos++; e }
                t[0].isDigit() -> Expr(id(), Expr.Kind.NUM, t)
                t[0].isLetter() || t[0] == '_' -> Expr(id(), Expr.Kind.VAR, t)
                else -> error("Token inesperado '$t'")
            }
        }
        fun level(ops: Set<String>, next: () -> Expr): Expr {
            var l = next()
            while (peek() in ops) { val op = peek()!!; pos++; l = Expr(id(), Expr.Kind.BINOP, op, listOf(l, next())) }
            return l
        }
        val term = { level(setOf("*", "/"), ::primary) }
        val arith = { level(setOf("+", "-"), term) }
        val rel = { level(TypeChecker.rel, arith) }
        val and = { level(setOf("&&"), rel) }
        orExpr = { level(setOf("||"), and) }
        val e = orExpr()
        require(pos == toks.size) { tx("comum_sobrou_1", toks.drop(pos).joinToString(" ")) }
        return e
    }
}

/** Gera código de três endereços (TAC) a partir da AST. */
object TacGen {
    data class Instr(val target: String, val a: String, val op: String?, val b: String?) {
        override fun toString() = if (op == null) "$target = $a" else "$target = $a $op $b"
    }

    fun generate(e: Expr, assignTo: String? = null): List<Instr> {
        val out = mutableListOf<Instr>()
        var t = 0
        fun gen(n: Expr): String {
            if (n.isLeaf) return n.text
            val a = gen(n.children[0]); val b = gen(n.children[1])
            val tmp = "t${++t}"
            out += Instr(tmp, a, n.text, b)
            return tmp
        }
        val r = gen(e)
        if (assignTo != null) out += Instr(assignTo, r, null, null)
        return out
    }

    /** Dobra de constantes: avalia em tempo de compilação o que só depende de literais. */
    fun constantFold(e: Expr): Expr {
        if (e.isLeaf) return e
        val l = constantFold(e.children[0]); val r = constantFold(e.children[1])
        if (l.kind == Expr.Kind.NUM && r.kind == Expr.Kind.NUM && e.text in TypeChecker.arith) {
            val a = TypeChecker.evaluate(e.copy(children = listOf(l, r)), emptyMap())[e.id]
            if (a != null && a.type != Type.ERROR) return Expr(e.id, Expr.Kind.NUM, TypeChecker.formatValue(a))
        }
        return e.copy(children = listOf(l, r))
    }
}

object ExprGenerator {
    val env: Map<String, Pair<Type, Double>> = mapOf(
        "a" to (Type.INT to 4.0), "b" to (Type.INT to 2.0), "c" to (Type.INT to 3.0),
        "x" to (Type.FLOAT to 1.5), "y" to (Type.FLOAT to 0.5), "ok" to (Type.BOOL to 1.0),
    )

    fun random(rnd: Random, depth: Int, allowBool: Boolean = false): String {
        fun leaf(): String = when (rnd.nextInt(3)) {
            0 -> listOf("1", "2", "3", "5", "2.5")[rnd.nextInt(5)]
            else -> listOf("a", "b", "c", "x", "y")[rnd.nextInt(5)]
        }
        fun go(d: Int): String {
            if (d <= 0 || rnd.nextInt(4) == 0) return leaf()
            val op = listOf("+", "-", "*", "+", "*")[rnd.nextInt(5)]
            val l = go(d - 1); val r = go(d - 1)
            val s = "$l $op $r"
            return if (rnd.nextBoolean() && d < depth) "( $s )" else s
        }
        var s = go(depth)
        if (allowBool && rnd.nextInt(3) == 0) s = "$s < ${go(1)}"
        if (allowBool && rnd.nextInt(5) == 0) s = "ok + $s"   // erro de tipo proposital
        return s
    }
}

/** Escopos estáticos aninhados — tabela de símbolos como pilha. */
object ScopeGen {
    data class Line(val indent: Int, val text: String, val declId: Int? = null, val use: String? = null, val resolvesTo: Int? = null)
    data class Program(val lines: List<Line>) {
        val uses get() = lines.withIndex().filter { it.value.use != null }
    }

    fun random(rnd: Random): Program {
        val names = listOf("x", "y", "z")
        val lines = mutableListOf<Line>()
        var declCounter = 0
        // pilha de escopos: cada escopo mapeia nome -> índice da linha da declaração
        val scopes = ArrayDeque<MutableMap<String, Int>>()
        fun lookup(n: String): Int? { for (s in scopes.reversed()) s[n]?.let { return it }; return null }
        fun block(depth: Int) {
            lines += Line(depth, "{")
            scopes.addLast(mutableMapOf())
            val nDecl = 1 + rnd.nextInt(2)
            repeat(nDecl) {
                val n = names[rnd.nextInt(names.size)]
                if (n in scopes.last()) return@repeat
                val t = if (rnd.nextBoolean()) "int" else "float"
                lines += Line(depth + 1, "$t $n;", declId = declCounter++)
                scopes.last()[n] = lines.lastIndex
            }
            repeat(1 + rnd.nextInt(2)) {
                val n = names[rnd.nextInt(names.size)]
                lines += Line(depth + 1, "$n = $n + 1;", use = n, resolvesTo = lookup(n))
            }
            if (depth < 2 && rnd.nextInt(3) != 0) block(depth + 1)
            if (rnd.nextBoolean()) {
                val n = names[rnd.nextInt(names.size)]
                lines += Line(depth + 1, "print($n);", use = n, resolvesTo = lookup(n))
            }
            scopes.removeLast()
            lines += Line(depth, "}")
        }
        block(0)
        return Program(lines)
    }
}
