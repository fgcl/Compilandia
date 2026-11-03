package com.compilandia.engine

/** Tabela preditiva LL(1): M[A, a] = produções. Mais de uma = conflito. */
class LL1Table(val grammar: Grammar) {
    val ff = FirstFollow(grammar)
    val columns: List<String> = grammar.terminals + EOF
    private val cells: Map<Pair<String, String>, List<Production>>

    /** Explicação de por que cada produção foi parar em cada célula. */
    val reasons: Map<Pair<String, String>, List<String>>

    init {
        val cells = mutableMapOf<Pair<String, String>, MutableList<Production>>()
        val reasons = mutableMapOf<Pair<String, String>, MutableList<String>>()
        for (p in grammar.productions) {
            val f = ff.firstOfString(p.rhs)
            for (a in f - EPS) {
                cells.getOrPut(p.lhs to a) { mutableListOf() } += p
                reasons.getOrPut(p.lhs to a) { mutableListOf() } += "'$a' ∈ FIRST(${p.rhsText()})"
            }
            if (EPS in f) {
                for (b in ff.follow.getValue(p.lhs)) {
                    cells.getOrPut(p.lhs to b) { mutableListOf() } += p
                    reasons.getOrPut(p.lhs to b) { mutableListOf() } +=
                        "${p.rhsText()} pode virar ε e '$b' ∈ FOLLOW(${p.lhs})"
                }
            }
        }
        this.cells = cells
        this.reasons = reasons
    }

    fun cell(nt: String, terminal: String): List<Production> = cells[nt to terminal].orEmpty()

    val conflicts: List<Pair<String, String>> get() = cells.filter { it.value.size > 1 }.keys.toList()
    val isLL1: Boolean get() = conflicts.isEmpty()

    /** Todas as células preenchidas (para os jogos conferirem a resposta do jogador). */
    fun filledCells(): Map<Pair<String, String>, List<Production>> = cells
}

data class LLStep(
    val stack: List<String>,      // topo = último elemento
    val input: List<String>,      // inclui $ no fim
    val action: LLAction,
)

sealed class LLAction {
    data class Match(val terminal: String) : LLAction()
    data class Expand(val production: Production) : LLAction()
    data object Accept : LLAction()
    data class Error(val message: String) : LLAction()

    fun label(): String = when (this) {
        is Match -> "casar '$terminal'"
        is Expand -> "expandir $production"
        Accept -> "aceitar"
        is Error -> "erro: $message"
    }
}

/** Analisador preditivo dirigido por tabela, gravando cada passo. */
object LL1Parser {
    fun trace(table: LL1Table, input: List<String>, maxSteps: Int = 500): List<LLStep> {
        val g = table.grammar
        val stack = mutableListOf(EOF, g.start)
        val inp = input + EOF
        var pos = 0
        val steps = mutableListOf<LLStep>()
        while (steps.size < maxSteps) {
            val top = stack.last()
            val a = inp[pos]
            val snapshotStack = stack.toList()
            val snapshotInput = inp.subList(pos, inp.size).toList()
            val action: LLAction = when {
                top == EOF && a == EOF -> LLAction.Accept
                top == EOF -> LLAction.Error("pilha vazia, mas ainda há entrada ('$a')")
                !g.isNonTerminal(top) ->
                    if (top == a) LLAction.Match(a) else LLAction.Error("esperava '$top', veio '$a'")
                else -> {
                    val cell = table.cell(top, a)
                    when (cell.size) {
                        0 -> LLAction.Error("M[$top, $a] está vazia")
                        else -> LLAction.Expand(cell.first())
                    }
                }
            }
            steps += LLStep(snapshotStack, snapshotInput, action)
            when (action) {
                is LLAction.Match -> { stack.removeAt(stack.lastIndex); pos++ }
                is LLAction.Expand -> {
                    stack.removeAt(stack.lastIndex)
                    for (s in action.production.rhs.asReversed()) stack += s
                }
                else -> return steps
            }
        }
        return steps
    }

    fun accepts(table: LL1Table, input: List<String>): Boolean =
        trace(table, input).lastOrNull()?.action == LLAction.Accept
}
