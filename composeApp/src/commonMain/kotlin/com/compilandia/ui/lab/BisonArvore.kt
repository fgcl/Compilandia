package com.compilandia.ui.lab

import com.compilandia.engine.ActionEval
import com.compilandia.engine.MiniBison
import com.compilandia.engine.ParseNode
import com.compilandia.ui.Palette
import com.compilandia.ui.UiNode

/**
 * A árvore da conta: cada operador vira um nó com o valor que ele calculou embaixo,
 * e cada número é uma folha. É a mesma árvore que o parser montou, sem os `e` do meio,
 * para que a precedência apareça no desenho: quem fica embaixo é calculado primeiro.
 */
fun MiniBison.arvoreDaConta(entrada: String, raiz: ParseNode): UiNode {
    val numeros = runCatching { lex(entrada) }.getOrDefault(emptyList()).filter { it.first == "NUM" }.map { it.second }.iterator()
    val prods = table.grammar.productions

    fun valorDe(n: ParseNode, filhos: List<Long?>): Long? {
        val p = prods.firstOrNull { it.lhs == n.symbol && it.rhs == n.children.map { c -> c.symbol } } ?: return null
        val act = actions[p.id - 1]
        return if (act.isNullOrBlank()) filhos.firstOrNull() else runCatching { ActionEval.eval(act, filhos.map { it ?: 0L }) }.getOrNull()
    }

    fun op(s: String) = s.removeSurrounding("'")
    fun no(rotulo: String, filhos: List<UiNode>, valor: Long?) =
        UiNode(rotulo, filhos, Palette.nodeNonTerminal, Palette.nodeTextNonTerminal, valor?.toString())

    fun vai(n: ParseNode): Pair<UiNode, Long?> {
        if (n.isLeaf) {
            if (n.symbol == "NUM") {
                val v = if (numeros.hasNext()) numeros.next() else null
                return UiNode(v?.toString() ?: "NUM", color = Palette.nodeLeaf, textColor = Palette.nodeTextTerminal) to v
            }
            return UiNode(op(n.symbol), color = Palette.nodeLeaf, textColor = Palette.nodeTextTerminal) to null
        }
        val filhos = n.children.map { vai(it) }
        val valores = filhos.map { it.second }
        val v = valorDe(n, valores)
        val simbolos = n.children.map { it.symbol }
        val ehTerminal = { s: String -> s.startsWith("'") }
        val desenho = when {
            filhos.size == 1 -> filhos[0].first
            simbolos.size == 3 && simbolos[0] == "'('" && simbolos[2] == "')'" -> filhos[1].first
            simbolos.size == 3 && ehTerminal(simbolos[1]) && !ehTerminal(simbolos[0]) -> no(op(simbolos[1]), listOf(filhos[0].first, filhos[2].first), v)
            simbolos.size == 2 && ehTerminal(simbolos[0]) -> no(op(simbolos[0]), listOf(filhos[1].first), v)
            else -> no(n.symbol, filhos.map { it.first }, v)
        }
        return desenho to v
    }
    return vai(raiz).first
}

/** A conta feita como na matemática, para comparar com o que o .y calculou. Nulo se a entrada não é só conta. */
fun contaCerta(entrada: String): Long? {
    if (entrada.isBlank() || !Regex("^[0-9+\\-*/^() ]+$").matches(entrada)) return null
    return runCatching { ActionEval.eval("\$\$ = $entrada", emptyList()) }.getOrNull()
}

/** Uma linha de %left por operador das regras, na ordem em que aparecem: o ponto de partida da régua. */
fun pontoDePrecedencia(spec: String): String {
    val linhas = spec.lines()
    val meio = linhas.indexOfFirst { it.trim() == "%%" }
    if (meio < 0) return spec
    val ops = Regex("'([^'])'").findAll(linhas.drop(meio + 1).joinToString("\n")).map { it.value }
        .filter { it != "'('" && it != "')'" }.distinct().toList()
    if (ops.isEmpty()) return spec
    return (linhas.take(meio) + ops.map { "%left $it" } + linhas.drop(meio)).joinToString("\n")
}
