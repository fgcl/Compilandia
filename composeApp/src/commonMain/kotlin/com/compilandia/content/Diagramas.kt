package com.compilandia.content

import com.compilandia.engine.DFA
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.nextSymbol
import com.compilandia.engine.render
import com.compilandia.textos.tx

/*
 * Diagramas de tabela: a mesma coisa da prova desenhada como caixas com compartimentos.
 * Estado de AFD com a linha da tabela dentro, conjunto de itens como no bison --graph,
 * nó da AST como struct. Tudo sai do motor; aqui só se decide onde cada caixa fica.
 */

/** Altura do nome e de cada linha de uma caixa-registro, em unidades do palco. */
const val RECORD_HEAD = 22f
const val RECORD_ROW = 18f

/** Altura de uma caixa-registro com [linhas] linhas. */
fun alturaRegistro(linhas: Int) = RECORD_HEAD + RECORD_ROW * linhas + 4f

/** Centro vertical da linha [row] de uma caixa-registro centrada em [cy] com altura [h]. */
fun recordRowY(cy: Float, h: Float, row: Int) = cy - h / 2 + RECORD_HEAD + RECORD_ROW * row + RECORD_ROW / 2

/** Largura que cabe a linha mais comprida, em fonte mono do tamanho [size]. */
fun larguraRegistro(nome: String, linhas: List<String>, size: Float = 13f): Float {
    val maior = (linhas.map { it.length * 0.86f } + (nome.length.toFloat())).maxOrNull() ?: 4f
    return (maior * size * 0.62f + 16f).coerceIn(64f, 340f)
}

/** Uma caixa-registro pronta. [w] nulo mede pelo texto. */
fun registro(id: String, nome: String, linhas: List<String>, x: Float, y: Float, w: Float? = null, tint: Tint = Tint.NONE, size: Float = 14f, lit: Int = -1) =
    Box(id, nome, x, y, w ?: larguraRegistro(nome, linhas, size), alturaRegistro(linhas.size), Look.RECORD, tint, size = size, rows = linhas, lit = lit)

private fun nomeEstado(dfa: DFA, i: Int, nomes: List<String>?): String = nomes?.getOrNull(i) ?: "q$i"

/** Cor do nome do estado: início em verde-água, final em amarelo. */
private fun tintEstado(dfa: DFA, i: Int) = when {
    i in dfa.accepting -> Tint.YELLOW
    i == dfa.start -> Tint.TEAL
    else -> Tint.NONE
}

/**
 * AFD em caixas: cada estado tem uma linha por letra ("b → q2"), e o fio sai da linha.
 * Ids iguais aos de [bolinhasDoAfd], para uma virar a outra no palco.
 */
fun caixasDoAfd(dfa: DFA, pos: List<Pair<Float, Float>>, nomes: List<String>? = null, w: Float = 104f): Pair<List<Box>, List<Wire>> {
    val boxes = mutableListOf<Box>()
    val wires = mutableListOf<Wire>()
    for (i in 0 until dfa.stateCount) {
        val (x, y) = pos[i]
        val extra = buildList { if (i == dfa.start) add(tx("diagramas_inicio")); if (i in dfa.accepting) add("final") }.joinToString(" ")
        val linhas = dfa.alphabet.map { c -> val d = dfa.delta[i to c]; "$c → ${if (d == null) "∅" else nomeEstado(dfa, d, nomes)}" }
        boxes += registro("q$i", (nomeEstado(dfa, i, nomes) + " " + extra).trim(), linhas, x, y, w, tintEstado(dfa, i), 14f)
        dfa.alphabet.forEachIndexed { r, c -> dfa.delta[i to c]?.let { d -> wires += Wire("q$i", "q$d", fromRow = r) } }
    }
    return boxes to wires
}

/** O mesmo AFD em bolinhas, com os mesmos ids das caixas. */
fun bolinhasDoAfd(dfa: DFA, pos: List<Pair<Float, Float>>, nomes: List<String>? = null): Pair<List<Box>, List<Wire>> {
    val boxes = estados(pos, dfa.accepting, (0 until dfa.stateCount).map { nomeEstado(dfa, it, nomes) })
    val porPar = linkedMapOf<Pair<Int, Int>, MutableList<Char>>()
    for ((k, d) in dfa.delta) porPar.getOrPut(k.first to d) { mutableListOf() } += k.second
    val wires = porPar.map { (par, letras) ->
        val volta = par.first != par.second && (par.second to par.first) in porPar
        Wire("q${par.first}", "q${par.second}", letras.sorted().joinToString(","), bend = if (volta) 22f else 0f)
    }
    return boxes to wires
}

/** A tabela de transição do AFD: ids d l<linha> c<coluna>, com → no início e * no final. */
fun tabelaDoAfd(dfa: DFA, y0: Float, nomes: List<String>? = null, cw: Float = 64f, ch: Float = 34f, prefix: String = "d"): List<Box> {
    val linhas = listOf(listOf("δ") + dfa.alphabet.map { it.toString() }) + (0 until dfa.stateCount).map { i ->
        val marca = (if (i == dfa.start) "→" else "") + (if (i in dfa.accepting) "*" else "")
        listOf(marca + nomeEstado(dfa, i, nomes)) + dfa.alphabet.map { c -> dfa.delta[i to c]?.let { nomeEstado(dfa, it, nomes) } ?: "∅" }
    }
    return tabela(prefix, linhas, y0, cw, ch, size = 14f)
}

/** Id da célula da tabela do AFD para o estado [estado] lendo a letra de índice [letra]. */
fun celulaAfd(estado: Int, letra: Int, prefix: String = "d") = "${prefix}l${estado + 1}c${letra + 1}"

/**
 * Coleção LR(0) em caixas, como o bison --graph: o nome do estado em cima e os itens dentro.
 * O fio do símbolo X sai do primeiro item com o ponto antes de X.
 */
fun caixasDeItens(aut: LRAutomaton, pos: List<Pair<Float, Float>>, estados: List<Int> = aut.states.indices.toList(), size: Float = 14f): Pair<List<Box>, List<Wire>> {
    val g = aut.grammar
    val boxes = mutableListOf<Box>()
    val wires = mutableListOf<Wire>()
    estados.forEachIndexed { k, sid ->
        val st = aut.states[sid]
        val itens = st.items.toList()
        val completo = itens.any { it.nextSymbol(g) == null }
        val (x, y) = pos[k]
        boxes += registro("I$sid", "I$sid", itens.map { it.render(g) }, x, y, tint = if (completo) Tint.ORANGE else Tint.NONE, size = size)
        for ((chave, dest) in aut.transitions) {
            if (chave.first != sid || dest !in estados) continue
            val linha = itens.indexOfFirst { it.nextSymbol(g) == chave.second }
            wires += Wire("I$sid", "I$dest", chave.second, fromRow = linha)
        }
    }
    return boxes to wires
}

/** Renumera o AFD na ordem em que se lê: q0 é o início e os outros vêm por largura, letra por letra. */
fun DFA.emOrdem(): DFA {
    val ordem = mutableListOf(start)
    var i = 0
    while (i < ordem.size) {
        for (c in alphabet) delta[ordem[i] to c]?.let { if (it !in ordem) ordem += it }
        i++
    }
    val novo = ordem.withIndex().associate { it.value to it.index }
    return DFA(
        ordem.size, 0, accepting.mapNotNull { novo[it] }.toSet(), alphabet,
        delta.entries.filter { it.key.first in novo && it.value in novo }.associate { (novo.getValue(it.key.first) to it.key.second) to novo.getValue(it.value) },
    )
}

/**
 * Um estado como o relatório do bison -v escreve: os itens do núcleo e, embaixo, o que fazer com cada token.
 * Se o estado só reduz uma regra, a linha vira "$default reduz".
 */
fun linhasDoEstado(t: com.compilandia.engine.LRTable, sid: Int): List<String> {
    val g = t.grammar
    val st = t.automaton.states[sid]
    val itens = st.kernel.map { it.core() }.distinct().map { it.render(g) }
    val acoes = t.terminals.mapNotNull { a -> t.action[sid to a]?.let { a to it } }
    val reduz = acoes.map { it.second }.filterIsInstance<com.compilandia.engine.LRAction.Reduce>().distinct()
    val empilha = acoes.filter { it.second is com.compilandia.engine.LRAction.Shift }
    fun regra(p: Int) = g.productions[p].toString()
    val linhas = mutableListOf<String>()
    for ((a, act) in empilha) linhas += tx("diagramas_1_empilha_2", a, (act as com.compilandia.engine.LRAction.Shift).state)
    if (acoes.any { it.second == com.compilandia.engine.LRAction.Accept }) linhas += tx("diagramas_end_aceita")
    if (reduz.size == 1) linhas += tx("diagramas_default_reduz_1", regra(reduz[0].prodId))
    else for ((a, act) in acoes) if (act is com.compilandia.engine.LRAction.Reduce) linhas += tx("diagramas_1_reduz_2", a, regra(act.prodId))
    for (nt in t.nonTerminals) t.gotoTable[sid to nt]?.let { linhas += tx("diagramas_1_vai_2", nt, it) }
    return itens + linhas
}

/** Índice da linha de [linhasDoEstado] que começa com [prefixo], ou -1. */
fun linhaQueComeca(linhas: List<String>, prefixo: String) = linhas.indexOfFirst { it.startsWith(prefixo) }

/**
 * A AST como structs de C: número vira "Node num" com val, operador vira "Node op" com esq e dir.
 * O ponteiro sai da linha dele. Ids pelo caminho no nó, como em [arvore].
 */
fun structsDaExpr(e: com.compilandia.engine.Expr, y0: Float, passoY: Float = 112f, largura: Float = 330f, dx: Float = 0f, size: Float = 12f, folhaW: Float = 78f, opW: Float = 86f): Pair<List<Box>, List<Wire>> {
    fun no(x: com.compilandia.engine.Expr): com.compilandia.engine.ParseNode = com.compilandia.engine.ParseNode(x.text, x.children.map { no(it) })
    val (bs, ws) = arvore(no(e), y0, passoY, largura)
    val porId = mutableMapOf<String, com.compilandia.engine.Expr>()
    fun marca(x: com.compilandia.engine.Expr, id: String) { porId[id] = x; x.children.forEachIndexed { i, c -> marca(c, "$id$i") } }
    marca(e, "t")
    val boxes = bs.map { b ->
        val x = porId.getValue(b.id)
        if (x.children.isEmpty()) {
            val campo = if (x.kind == com.compilandia.engine.Expr.Kind.NUM) "val: ${x.text}" else tx("diagramas_nome_1", x.text)
            registro(b.id, tx("diagramas_node"), listOf(campo), b.x + dx, b.y, folhaW, Tint.TEAL, size)
        } else registro(b.id, tx("diagramas_node"), listOf("op: '${x.text}'", "esq ●", "dir ●"), b.x + dx, b.y, opW, Tint.YELLOW, size)
    }
    val wires = ws.map { w -> w.copy(arrow = true, fromRow = 1 + w.to.last().digitToInt()) }
    return boxes to wires
}

/** Um passo da descida recursiva em C: chamar, casar ou voltar, e a pilha de chamadas depois dele. */
data class PassoDescida(val acao: String, val alvo: String, val pilha: List<String>, val lidos: Int)

/**
 * Roda de verdade a descida recursiva de expr → termo ('+' termo)*, termo → fator ('*' fator)*, fator → NUM
 * e anota cada chamada, cada token casado e cada volta.
 */
fun rastroDescida(tokens: List<String>): List<PassoDescida> {
    val out = mutableListOf<PassoDescida>()
    val pilha = mutableListOf("main")
    var k = 0
    fun tok() = tokens.getOrNull(k)
    fun chama(f: String, corpo: () -> Unit) { pilha += f; out += PassoDescida("chamar", f, pilha.toList(), k); corpo(); pilha.removeAt(pilha.lastIndex); out += PassoDescida("voltar", f, pilha.toList(), k) }
    fun casa() { val t = tok() ?: error("faltou token"); k++; out += PassoDescida("casar", t, pilha.toList(), k) }
    fun fator() = chama("fator") { if (tok()?.first()?.isDigit() == true) casa() else error("esperava número em ${tok()}") }
    fun termo() = chama("termo") { fator(); while (tok() == "*") { casa(); fator() } }
    fun expr() = chama("expr") { termo(); while (tok() == "+") { casa(); termo() } }
    expr()
    return out
}
