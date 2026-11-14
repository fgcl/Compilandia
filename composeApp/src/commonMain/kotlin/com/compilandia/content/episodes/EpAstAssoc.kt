package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ParseNode
import com.compilandia.textos.tx

/** A AST de a - b - c: o menos junta pela esquerda (revisor professor do bloco 5). As contas saem de [CONTA_MENOS]. */
internal val ARVORE_MENOS_MENOS = ParseNode("-", listOf(ParseNode("-", listOf(ParseNode("a"), ParseNode("b"))), ParseNode("c")))

/** 10 - 3 - 2 nas duas árvores: pela esquerda dá 5, pela direita daria 9. */
internal val CONTA_MENOS = Triple(10, 3, 2)

/** O erro que mais custa ponto na AST: pendurar a - b - c para a direita. Fecha a Sua vez da aula de AST. */
internal fun EpisodeBuilder.astMenosMenos() {
    cena(tx("ast_l_12")) {
        clear()
        add(titulo("AST"))
        add(fita("a-b-c", 100f, cell = 40f, prefix = "m"))
    }
    pergunta(tx("ast_l_13"), setOf("m1"), setOf("m3", "m5"), tx("ast_l_13_dica"), tx("ast_l_13_ok")) {
        add(Box("m5", tx("ast_l_13_o3"), 180f, 190f, 240f, 40f, Look.TOKEN, Tint.YELLOW, size = 13f))
    }
    cena(tx("ast_l_14"), mood = EpMood.HAPPY) {
        drop("m5")
        val (b, w) = arvore(ARVORE_MENOS_MENOS, 170f, passoY = 56f, largura = 240f)
        add(b); for (x in w) wire(x)
        val (p, q, r) = CONTA_MENOS
        add(rotulo("mc", "$p - $q - $r = ${p - q - r}", 180f, 360f, Tint.GREEN, 17f))
        hot("m1")
    }
}
