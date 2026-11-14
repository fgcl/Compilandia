package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.titulo
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.ParseNode
import com.compilandia.engine.TypeChecker
import com.compilandia.textos.tx

/*
 * Atributos: a regra escrita como na prova, ao lado da árvore, e a pergunta "S ou L-atribuída?"
 * (revisor professor do bloco 5). O valor da raiz sai do TypeChecker do motor.
 */

/** A conta da SDD de valor. */
const val ATTR_SDD_CONTA = "2 + 3"

private fun ramo(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

/** Etiqueta de atributo à direita de um nó. */
private fun valor(id: String, t: String, alvo: Box) = Box(id, t, alvo.x + 40f, alvo.y, 56f, 20f, Look.LABEL, Tint.YELLOW, size = 14f)

/** As três opções das duas perguntas: S, só L, nenhuma. */
private fun opcoesSl() = listOf("ns" to tx("attr_sdd_op_s"), "nl" to tx("attr_sdd_op_l"), "nn" to tx("attr_sdd_op_n"))
    .mapIndexed { i, (id, t) -> Box(id, t, 62f + i * 118f, 380f, 112f, 44f, Look.TOKEN, Tint.YELLOW, size = 12f) }

/** Os valores de 2, 3 e 2 + 3, calculados pelo motor. */
fun valoresSdd(): Triple<Int, Int, Int> {
    val e = ExprParser.parse(ATTR_SDD_CONTA)
    val at = TypeChecker.evaluate(e, ExprGenerator.env)
    fun v(x: Expr) = at.getValue(x.id).value!!.toInt()
    return Triple(v(e.children[0]), v(e.children[1]), v(e))
}

internal fun EpisodeBuilder.attrRegraEscrita() {
    attrSddS()
    attrSddL()
}

/** E → E1 + T com E.val = E1.val + T.val: só sintetizado, S-atribuída. */
private fun EpisodeBuilder.attrSddS() {
    val (a, b, soma) = valoresSdd()
    val (bx, ws) = arvore(ramo("E", ramo("E", ramo("T", ramo("$a"))), ramo("+"), ramo("T", ramo("$b"))), 180f, passoY = 46f, largura = 240f)
    val n = bx.associateBy { it.id }
    cena(tx("attr_sdd_01")) {
        clear()
        add(titulo(tx("attr_sdd_titulo")))
        add(codigo("sd", listOf(tx("attr_sdd_e1"), tx("attr_sdd_e2"), tx("attr_sdd_e3")), 70f, x = 16f, passo = 28f, size = 12f))
        add(bx); for (w in ws) wire(w)
        add(valor("v00", "= $a", n.getValue("t00")), valor("v2", "= $b", n.getValue("t2")))
        hot("sd0")
    }
    val erradas = listOf(a * b, "$a$b".toInt()).filter { it != soma }
    pergunta(tx("attr_sdd_q1"), setOf("rs"), erradas.indices.map { "r$it" }.toSet(), tx("attr_sdd_q1_dica"), tx("attr_sdd_q1_ok")) {
        add(valor("v0", "= $a", n.getValue("t0")))
        // A certa fica no meio: nem sempre a primeira opção.
        add(Box("rs", "$soma", 180f, 380f, 80f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f))
        erradas.forEachIndexed { i, x -> add(Box("r$i", "$x", 70f + i * 220f, 380f, 80f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f)) }
        hot("t", "sd0")
    }
    pergunta(tx("attr_sdd_q2"), setOf("ns"), setOf("nl", "nn"), tx("attr_sdd_q2_dica"), tx("attr_sdd_q2_ok")) {
        dropAll("r")
        add(valor("vr", "= $soma", n.getValue("t")))
        add(opcoesSl())
        hot("sd0", "sd1", "sd2")
    }
}

/** D → T L com L.tipo = T.tipo: o L herda do irmão da esquerda, L-atribuída e não S. */
private fun EpisodeBuilder.attrSddL() {
    val (bx, ws) = arvore(ramo("D", ramo("T", ramo("int")), ramo("L", ramo("a"))), 180f, passoY = 56f, largura = 200f)
    val n = bx.associateBy { it.id }
    pergunta(tx("attr_sdd_q3"), setOf("nl"), setOf("ns", "nn"), tx("attr_sdd_q3_dica"), tx("attr_sdd_q3_ok")) {
        clear()
        add(titulo(tx("attr_sdd_titulo")))
        add(codigo("sd", listOf(tx("attr_sdd_d1"), tx("attr_sdd_d2"), tx("attr_sdd_d3")), 70f, x = 16f, passo = 28f, size = 12f))
        add(bx); for (w in ws) wire(w)
        add(valor("vt", "int", n.getValue("t0")), valor("vl", "int", n.getValue("t1")))
        wire("t0", "t1", hot = true, bend = -30f)
        add(opcoesSl())
        hot("sd0", "t1")
    }
}
