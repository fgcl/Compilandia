package com.compilandia.engine

import com.compilandia.textos.tx

/**
 * Árvore sintática ABSTRATA (AST) com classes, como num compilador de verdade, comparada
 * com a árvore de derivação (concreta). A AST joga fora o que só servia para a gramática:
 * não-terminais "corrente" (E → T → F), parênteses e ε.
 */
object AstTools {
    /** Gramática das expressões usada para mostrar a árvore CONCRETA. */
    val concreteGrammar: Grammar = Grammar.parse(
        """
        E -> E + T | E - T | T
        T -> T * F | T / F | F
        F -> ( E ) | id | num
        """.trimIndent()
    )

    private val slr by lazy { LRTable(concreteGrammar, LRKind.SLR1) }

    /** Árvore concreta da expressão, com os lexemas reais nas folhas (id → a, num → 3). */
    fun concreteTree(src: String): ParseNode? {
        val toks = MiniLexer.tokenize(src)
        val terms = toks.map { when (it.cls) { TokenClass.ID -> "id"; TokenClass.NUM -> "num"; else -> it.lexeme } }
        val tree = LRParser.parse(slr, terms).tree ?: return null
        var k = 0
        fun relabel(n: ParseNode): ParseNode =
            if (n.isLeaf) { if (n.symbol == EPS) n else { val lx = toks[k++].lexeme; ParseNode(if (n.symbol == "id" || n.symbol == "num") lx else n.symbol) } }
            else ParseNode(n.symbol, n.children.map { relabel(it) })
        return relabel(tree)
    }

    /** Código Java que constrói a AST. */
    fun toJava(e: Expr): String = when (e.kind) {
        Expr.Kind.NUM -> tx("comum_new_num_1", e.text)
        Expr.Kind.VAR -> "new Var(\"${e.text}\")"
        Expr.Kind.BINOP -> "new BinOp('${e.text}', ${toJava(e.children[0])}, ${toJava(e.children[1])})"
    }

    /** Rótulo de um nó como "classe" (usado no jogo do Construtor de AST). */
    fun label(e: Expr): String = when (e.kind) {
        Expr.Kind.NUM -> "Num(${e.text})"
        Expr.Kind.VAR -> "Var(${e.text})"
        Expr.Kind.BINOP -> "BinOp('${e.text}')"
    }

    /** Classes Java da AST (mostradas nas aulas e no jogo). */
    val javaClasses: String = """
        abstract class Expr { }
        class Num extends Expr { int valor;  Num(int v) { valor = v; } }
        class Var extends Expr { String nome; Var(String n) { nome = n; } }
        class BinOp extends Expr {
            char op; Expr esq, dir;
            BinOp(char op, Expr esq, Expr dir) { this.op = op; this.esq = esq; this.dir = dir; }
        }
    """.trimIndent()

    fun countNodes(n: ParseNode): Int = 1 + n.children.sumOf { countNodes(it) }
    fun countNodes(e: Expr): Int = 1 + e.children.sumOf { countNodes(it) }

    /** Gera o parser descendente recursivo (com precedência em cascata) que CONSTRÓI a AST. */
    val javaParserBuildingAst: String = tx("ast_expr_expr_t_t_expr")
}
