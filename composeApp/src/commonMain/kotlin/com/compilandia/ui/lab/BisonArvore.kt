package com.compilandia.ui.lab

import com.compilandia.engine.ActionEval
import com.compilandia.engine.MiniBison
import com.compilandia.engine.ParseNode
import com.compilandia.ui.Palette
import com.compilandia.ui.UiNode

/**
 * Até onde o parser chegou: quantos tokens empilhou, quantas reduções fez e se o passo atual é uma redução.
 * As folhas nascem na ordem em que são empilhadas e os operadores na ordem das reduções (de baixo para cima).
 */
data class Progresso(val empilhados: Int, val reduzidos: Int, val reduzAgora: Boolean)

/** O progresso no começo do passo [passo]: conta as ações dos passos anteriores. */
fun progressoNoPasso(quadros: List<MiniBison.Quadro>, passo: Int): Progresso {
    val antes = quadros.take(passo).map { it.acao }
    return Progresso(antes.count { it is com.compilandia.engine.LRAction.Shift }, antes.count { it is com.compilandia.engine.LRAction.Reduce },
        quadros.getOrNull(passo)?.acao is com.compilandia.engine.LRAction.Reduce)
}

/**
 * A árvore da conta: cada operador vira um nó com o valor que ele calculou embaixo,
 * e cada número é uma folha. É a mesma árvore que o parser montou, sem os `e` do meio,
 * para que a precedência apareça no desenho: quem fica embaixo é calculado primeiro.
 * Com [progresso], o que o parser ainda não montou fica fantasma e o nó que nasce neste passo acende.
 */
fun MiniBison.arvoreDaConta(entrada: String, raiz: ParseNode, progresso: Progresso? = null): UiNode {
    val numeros = runCatching { lex(entrada) }.getOrDefault(emptyList()).filter { it.first == "NUM" }.map { it.second }.iterator()
    val prods = table.grammar.productions
    var folhas = 0
    var reducoes = 0

    fun valorDe(n: ParseNode, filhos: List<Long?>): Long? {
        val p = prods.firstOrNull { it.lhs == n.symbol && it.rhs == n.children.map { c -> c.symbol } } ?: return null
        val act = actions[p.id - 1]
        return if (act.isNullOrBlank()) filhos.firstOrNull() else runCatching { ActionEval.eval(act, filhos.map { it ?: 0L }) }.getOrNull()
    }

    fun op(s: String) = s.removeSurrounding("'")
    fun fantasma(u: UiNode) = u.copy(color = u.color.copy(alpha = 0.18f), textColor = u.textColor.copy(alpha = 0.35f), sub = null)
    fun acende(u: UiNode) = u.copy(color = Palette.nodeActive)
    fun no(rotulo: String, filhos: List<UiNode>, valor: Long?) =
        UiNode(rotulo, filhos, Palette.nodeNonTerminal, Palette.nodeTextNonTerminal, valor?.toString())

    fun folha(n: ParseNode): Pair<UiNode, Long?> {
        val k = folhas++
        val v = if (n.symbol == "NUM" && numeros.hasNext()) numeros.next() else null
        val u = UiNode(if (n.symbol == "NUM") v?.toString() ?: "NUM" else op(n.symbol), color = Palette.nodeLeaf, textColor = Palette.nodeTextTerminal)
        return (if (progresso != null && k >= progresso.empilhados) fantasma(u) else u) to v
    }

    fun vai(n: ParseNode): Pair<UiNode, Long?> {
        if (n.isLeaf) return folha(n)
        val filhos = n.children.map { vai(it) }
        val r = reducoes++
        val v = valorDe(n, filhos.map { it.second })
        val simbolos = n.children.map { it.symbol }
        val ehTerminal = { s: String -> s.startsWith("'") }
        val (desenho, proprio) = when {
            filhos.size == 1 -> filhos[0].first to false
            simbolos.size == 3 && simbolos[0] == "'('" && simbolos[2] == "')'" -> filhos[1].first to false
            simbolos.size == 3 && ehTerminal(simbolos[1]) && !ehTerminal(simbolos[0]) -> no(op(simbolos[1]), listOf(filhos[0].first, filhos[2].first), v) to true
            simbolos.size == 2 && ehTerminal(simbolos[0]) -> no(op(simbolos[0]), listOf(filhos[1].first), v) to true
            else -> no(n.symbol, filhos.map { it.first }, v) to true
        }
        val final = when {
            progresso == null || r < progresso.reduzidos -> desenho
            r == progresso.reduzidos && progresso.reduzAgora -> acende(desenho)
            proprio -> fantasma(desenho)
            else -> desenho
        }
        return final to v
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
