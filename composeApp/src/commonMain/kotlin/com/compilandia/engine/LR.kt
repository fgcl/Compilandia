package com.compilandia.engine

import com.compilandia.textos.tx

/** Item LR: produção com um ponto (•). `lookahead` só é usado em LR(1)/LALR(1). */
data class LRItem(val prodId: Int, val dot: Int, val lookahead: String? = null) {
    fun core() = LRItem(prodId, dot)
}

fun LRItem.render(g: Grammar): String {
    val p = g.productions[prodId]
    val parts = p.rhs.toMutableList()
    parts.add(dot, "•")
    val la = lookahead?.let { ", $it" } ?: ""
    return "${p.lhs} → ${parts.joinToString(" ")}$la"
}

fun LRItem.nextSymbol(g: Grammar): String? = g.productions[prodId].rhs.getOrNull(dot)
fun LRItem.isComplete(g: Grammar): Boolean = dot >= g.productions[prodId].rhs.size

data class LRState(val id: Int, val kernel: Set<LRItem>, val items: Set<LRItem>)

/** Coleção canônica de conjuntos de itens LR(0) ou LR(1). Usa sempre a gramática AUMENTADA. */
class LRAutomaton private constructor(
    val grammar: Grammar,            // aumentada
    val states: List<LRState>,
    val transitions: Map<Pair<Int, String>, Int>,
    val isLR1: Boolean,
) {
    companion object {
        fun lr0(original: Grammar): LRAutomaton = build(original.augmented(), lr1 = false)
        fun lr1(original: Grammar): LRAutomaton = build(original.augmented(), lr1 = true)

        fun closure0(g: Grammar, items: Set<LRItem>): Set<LRItem> {
            val out = LinkedHashSet(items)
            val work = ArrayDeque(items)
            while (work.isNotEmpty()) {
                val it = work.removeFirst()
                val b = it.nextSymbol(g) ?: continue
                if (!g.isNonTerminal(b)) continue
                for (p in g.productionsOf(b)) {
                    val ni = LRItem(p.id, 0)
                    if (out.add(ni)) work += ni
                }
            }
            return out
        }

        fun closure1(g: Grammar, ff: FirstFollow, items: Set<LRItem>): Set<LRItem> {
            val out = LinkedHashSet(items)
            val work = ArrayDeque(items)
            while (work.isNotEmpty()) {
                val it = work.removeFirst()
                val b = it.nextSymbol(g) ?: continue
                if (!g.isNonTerminal(b)) continue
                val rhs = g.productions[it.prodId].rhs
                val beta = rhs.subList(it.dot + 1, rhs.size) + listOf(it.lookahead!!)
                val las = ff.firstOfString(beta) - EPS
                for (p in g.productionsOf(b)) for (la in las) {
                    val ni = LRItem(p.id, 0, la)
                    if (out.add(ni)) work += ni
                }
            }
            return out
        }

        fun goto(g: Grammar, items: Set<LRItem>, x: String): Set<LRItem> =
            items.filter { it.nextSymbol(g) == x }.map { it.copy(dot = it.dot + 1) }.toSet()

        private fun build(g: Grammar, lr1: Boolean): LRAutomaton {
            val ff = FirstFollow(g)
            val clo: (Set<LRItem>) -> Set<LRItem> = if (lr1) { s -> closure1(g, ff, s) } else { s -> closure0(g, s) }
            val startKernel = setOf(LRItem(0, 0, if (lr1) EOF else null))
            val states = mutableListOf(LRState(0, startKernel, clo(startKernel)))
            val index = mutableMapOf(startKernel to 0)
            val trans = mutableMapOf<Pair<Int, String>, Int>()
            var i = 0
            val symbols = g.nonTerminals + g.terminals
            while (i < states.size) {
                val st = states[i]
                // ordem: símbolos na ordem em que aparecem após o ponto (fica mais didático)
                val order = st.items.mapNotNull { it.nextSymbol(g) }.distinct().sortedBy { symbols.indexOf(it) }
                for (x in order) {
                    val k = goto(g, st.items, x)
                    if (k.isEmpty()) continue
                    val target = index.getOrPut(k) {
                        states += LRState(states.size, k, clo(k)); states.size - 1
                    }
                    trans[i to x] = target
                }
                i++
            }
            return LRAutomaton(g, states, trans, lr1)
        }
    }

    /** LALR(1): funde estados LR(1) com o mesmo núcleo (core). */
    fun mergeLALR(): LRAutomaton {
        require(isLR1)
        val coreOf = states.map { s -> s.items.map { it.core() }.toSet() }
        val groups = LinkedHashMap<Set<LRItem>, MutableList<Int>>()
        coreOf.forEachIndexed { i, c -> groups.getOrPut(c) { mutableListOf() } += i }
        val newIdOf = IntArray(states.size)
        val merged = groups.values.mapIndexed { newId, members ->
            members.forEach { newIdOf[it] = newId }
            LRState(
                newId,
                members.flatMap { states[it].kernel }.toSet(),
                members.flatMap { states[it].items }.toSet()
            )
        }
        val trans = transitions.entries.associate { (k, v) -> (newIdOf[k.first] to k.second) to newIdOf[v] }
        return LRAutomaton(grammar, merged, trans, true)
    }
}

sealed class LRAction {
    data class Shift(val state: Int) : LRAction()
    data class Reduce(val prodId: Int) : LRAction()
    data object Accept : LRAction()

    fun short(): String = when (this) {
        is Shift -> "s$state"
        is Reduce -> "r$prodId"
        Accept -> "acc"
    }
}

/** Precedência ao estilo Yacc/Bison: nível maior = liga mais forte. */
enum class Assoc { LEFT, RIGHT, NONASSOC }
data class Precedence(val level: Int, val assoc: Assoc)

data class LRConflict(
    val state: Int,
    val terminal: String,
    val actions: List<LRAction>,
    val kind: String,            // "shift/reduce" ou "reduce/reduce"
    val resolution: String?,     // como foi resolvido (precedência, padrão do Bison, ...)
)

enum class LRKind { LR0, SLR1, LALR1, LR1 }

/** Tabela ACTION/GOTO para LR(0), SLR(1), LALR(1) ou LR(1). */
class LRTable(
    original: Grammar,
    val kind: LRKind,
    val tokenPrec: Map<String, Precedence> = emptyMap(),
    val ruleExplicitPrec: Map<Int, String> = emptyMap(), // prodId (na aumentada) -> token do %prec
    val resolveConflicts: Boolean = false,
) {
    val automaton: LRAutomaton = when (kind) {
        LRKind.LR0, LRKind.SLR1 -> LRAutomaton.lr0(original)
        LRKind.LR1 -> LRAutomaton.lr1(original)
        LRKind.LALR1 -> LRAutomaton.lr1(original).mergeLALR()
    }
    val grammar: Grammar = automaton.grammar
    val ff = FirstFollow(grammar)
    val terminals: List<String> = grammar.terminals + EOF
    val nonTerminals: List<String> = grammar.nonTerminals.drop(1)

    /** Todas as ações candidatas (antes de resolver). */
    val rawActions: Map<Pair<Int, String>, List<LRAction>>
    /** Ação escolhida por célula (após resolução, se habilitada). */
    val action: Map<Pair<Int, String>, LRAction>
    val gotoTable: Map<Pair<Int, String>, Int>
    val conflicts: List<LRConflict>
    /** Como cada célula com mais de uma ação foi decidida, inclusive as que a precedência resolveu em silêncio. */
    val resolutions: Map<Pair<Int, String>, String>

    init {
        val g = grammar
        val raw = LinkedHashMap<Pair<Int, String>, MutableList<LRAction>>()
        fun add(s: Int, t: String, a: LRAction) {
            val l = raw.getOrPut(s to t) { mutableListOf() }
            if (a !in l) l += a
        }
        for (st in automaton.states) {
            for (it in st.items) {
                val x = it.nextSymbol(g)
                if (x != null) {
                    if (!g.isNonTerminal(x)) automaton.transitions[st.id to x]?.let { add(st.id, x, LRAction.Shift(it)) }
                } else if (it.prodId == 0) {
                    add(st.id, EOF, LRAction.Accept)
                } else {
                    val lhs = g.productions[it.prodId].lhs
                    val las: Collection<String> = when (kind) {
                        LRKind.LR0 -> terminals
                        LRKind.SLR1 -> ff.follow.getValue(lhs)
                        LRKind.LR1, LRKind.LALR1 -> listOf(it.lookahead!!)
                    }
                    for (a in las) add(st.id, a, LRAction.Reduce(it.prodId))
                }
            }
        }
        rawActions = raw
        gotoTable = automaton.transitions.filterKeys { g.isNonTerminal(it.second) }

        val chosen = LinkedHashMap<Pair<Int, String>, LRAction>()
        val conflicts = mutableListOf<LRConflict>()
        val decisoes = LinkedHashMap<Pair<Int, String>, String>()
        for ((key, acts) in raw) {
            if (acts.size == 1) { chosen[key] = acts[0]; continue }
            val shifts = acts.filterIsInstance<LRAction.Shift>()
            val reduces = acts.filterIsInstance<LRAction.Reduce>().sortedBy { it.prodId }
            val kindTxt = if (shifts.isNotEmpty()) "shift/reduce" else "reduce/reduce"
            var resolution: String? = null
            var pick: LRAction? = null
            if (resolveConflicts) {
                if (shifts.isNotEmpty() && reduces.size == 1) {
                    val r = reduces[0]
                    val tp = tokenPrec[key.second]
                    val rp = rulePrecedence(r.prodId)
                    if (tp != null && rp != null) {
                        pick = when {
                            tp.level > rp.level -> shifts[0].also { resolution = tx("lr_1_tem_precedencia_maior_que", key.second) }
                            tp.level < rp.level -> r.also { resolution = tx("lr_regra_tem_precedencia_maior_que", key.second) }
                            tp.assoc == Assoc.LEFT -> r.also { resolution = tx("lr_mesma_precedencia_left_reduce_associa") }
                            tp.assoc == Assoc.RIGHT -> shifts[0].also { resolution = tx("lr_mesma_precedencia_right_shift_associa") }
                            else -> null.also { resolution = "%nonassoc → erro de sintaxe" }
                        }
                    } else {
                        pick = shifts[0]
                        resolution = tx("lr_sem_precedencia_declarada_bison_escolhe")
                    }
                } else if (shifts.isEmpty()) {
                    pick = reduces[0]
                    resolution = tx("lr_reduce_reduce_bison_escolhe_regra")
                } else {
                    pick = shifts[0]
                    resolution = tx("lr_varios_reduces_shift_bison_prefere")
                }
                decisoes[key] = resolution!!
                val stillWarn = resolution!!.startsWith("sem precedência") || shifts.isEmpty()
                if (stillWarn) {
                    conflicts += LRConflict(key.first, key.second, acts.toList(), kindTxt, resolution)
                }
                if (pick != null) chosen[key] = pick
            } else {
                conflicts += LRConflict(key.first, key.second, acts.toList(), kindTxt, null)
                chosen[key] = shifts.firstOrNull() ?: reduces[0]
            }
        }
        action = chosen
        this.conflicts = conflicts
        resolutions = decisoes
    }

    /** Conflitos que existiam antes de aplicar precedência (para mostrar ao jogador). */
    val rawConflictCells: List<Pair<Int, String>> get() = rawActions.filter { it.value.size > 1 }.keys.toList()

    fun rulePrecedence(prodId: Int): Precedence? {
        ruleExplicitPrec[prodId]?.let { return tokenPrec[it] }
        val last = grammar.productions[prodId].rhs.lastOrNull { !grammar.isNonTerminal(it) } ?: return null
        return tokenPrec[last]
    }

    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
}

data class LRStep(
    val states: List<Int>,
    val symbols: List<String>,
    val input: List<String>,
    val action: LRAction?,     // null = erro
    val note: String,
)

/** Nó de árvore sintática genérico. */
data class ParseNode(val symbol: String, val children: List<ParseNode> = emptyList(), val value: String? = null) {
    val isLeaf get() = children.isEmpty()
    fun leaves(): List<String> = if (isLeaf) (if (symbol == EPS) emptyList() else listOf(symbol)) else children.flatMap { it.leaves() }
    fun size(): Int = 1 + children.sumOf { it.size() }
    fun depth(): Int = 1 + (children.maxOfOrNull { it.depth() } ?: 0)
}

data class LRParseResult(val steps: List<LRStep>, val accepted: Boolean, val tree: ParseNode?)

/** Analisador shift-reduce dirigido pela tabela. */
object LRParser {
    fun parse(table: LRTable, input: List<String>, maxSteps: Int = 1000): LRParseResult {
        val g = table.grammar
        val states = mutableListOf(0)
        val symbols = mutableListOf<String>()
        val nodes = mutableListOf<ParseNode>()
        val inp = input + EOF
        var pos = 0
        val steps = mutableListOf<LRStep>()
        while (steps.size < maxSteps) {
            val s = states.last()
            val a = inp[pos]
            val act = table.action[s to a]
            val snapS = states.toList(); val snapSym = symbols.toList(); val snapIn = inp.subList(pos, inp.size).toList()
            when (act) {
                null -> {
                    steps += LRStep(snapS, snapSym, snapIn, null, tx("lr_action_1_2_vazia_erro", s, a))
                    return LRParseResult(steps, false, null)
                }
                is LRAction.Shift -> {
                    steps += LRStep(snapS, snapSym, snapIn, act, tx("lr_empilha_1_vai_para_estado", a, act.state))
                    states += act.state; symbols += a; nodes += ParseNode(a); pos++
                }
                is LRAction.Reduce -> {
                    val p = g.productions[act.prodId]
                    val n = p.rhs.size
                    val gotoFrom = states[states.size - 1 - n]
                    val next = table.gotoTable[gotoFrom to p.lhs]
                    steps += LRStep(snapS, snapSym, snapIn, act, tx("lr_reduz_por_1_desempilha_2", p, n, gotoFrom, p.lhs, next ?: "?"))
                    val kids = if (n == 0) listOf(ParseNode(EPS)) else nodes.subList(nodes.size - n, nodes.size).toList()
                    repeat(n) { states.removeAt(states.lastIndex); symbols.removeAt(symbols.lastIndex); nodes.removeAt(nodes.lastIndex) }
                    if (next == null) return LRParseResult(steps, false, null)
                    states += next; symbols += p.lhs; nodes += ParseNode(p.lhs, kids)
                }
                LRAction.Accept -> {
                    steps += LRStep(snapS, snapSym, snapIn, act, tx("lr_aceita_entrada_pertence_linguagem"))
                    return LRParseResult(steps, true, nodes.lastOrNull())
                }
            }
        }
        return LRParseResult(steps, false, null)
    }
}
