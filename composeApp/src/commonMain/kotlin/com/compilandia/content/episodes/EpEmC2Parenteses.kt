package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.PassoDescida
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.EM_C
import com.compilandia.content.Tint
import com.compilandia.content.ToyChamadas
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.pilha
import com.compilandia.content.rastroDescida
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Em C 2: uma função por regra. Desde o pedido do revisor professor do bloco 6, o fator aceita parêntese
 * e a aula roda 2 * ( 3 + 4 ): a pilha mostra expr duas vezes, e a aula fecha com a ponte para o E' do Templo.
 */

/** A conta que a Em C 2 roda. */
val EMC2_PARENTESES = listOf("2", "*", "(", "3", "+", "4", ")")

private val CODIGO_EXPR = listOf(
    "void expr(void) {",
    "  termo();",
    "  while (tok.tipo == '+') {",
    "    casa('+');",
    "    termo();",
    "  }",
    "}",
)

/** termo resumido e o fator inteiro: a linha 5 é a chamada recursiva. */
private val CODIGO_TERMO_FATOR = listOf(
    "void termo(void) {",
    "  fator(); while (tok.tipo == '*') ...",
    "}",
    "void fator(void) {",
    "  if (tok.tipo == '(') {",
    "    casa('('); expr(); casa(')');",
    "  } else casa(NUM);",
    "}",
)

/** O passo em que o fator chama o segundo expr: a pilha tem expr duas vezes. */
fun segundoExpr(r: List<PassoDescida>) = r.indexOfFirst { p -> p.acao == "chamar" && p.alvo == "expr" && p.pilha.count { it == "expr" } == 2 }

private class PalcoDescida(val b: EpisodeBuilder) {
    val rastro = rastroDescida(EMC2_PARENTESES)
    fun passo(acao: String, alvo: String) = rastro.indexOfFirst { it.acao == acao && it.alvo == alvo }
    fun mostra(k: Int) = with(b) {
        val p = rastro[k].pilha
        dropAll("f"); dropAll("e")
        add(pilha("f", p.map { "$it()" }, 100f, 370f, 140f, 34f, Tint.ORANGE).mapIndexed { i, x -> if (i == p.lastIndex) x.copy(tint = Tint.YELLOW) else x })
        add(EMC2_PARENTESES.mapIndexed { i, t -> Box("e$i", t, 180f + (i - 3) * 44f, 70f, 40f, 36f, Look.TOKEN, if (i < rastro[k].lidos) Tint.DARK else Tint.NONE, size = 16f) })
    }
}

internal val emC2 by lazy {
    episodio(Skill.RECURSIVE_DESCENT, tx("epemc_em_c_2_funcao_por"), EM_C) {
        val palco = PalcoDescida(this)
        codigoDaDescida()
        rodaComParenteses(palco)
        brinquedo(
            tx("epemc_agora_voce_processador_chame_case"),
            tx("epemc_chame_quando_topo_precisa_outra"),
            tx("epemc_voce_rodou_descida_recursiva_inteira"),
        ) { ToyChamadas(listOf("1", "*", "2", "+", "3"), tx("epemc_rode_1_2_3_ate")) }
    }
}

/** As três funções na tela: expr com o while, termo igual, e o fator que abre parêntese. */
private fun EpisodeBuilder.codigoDaDescida() {
    cena(tx("epemc_na_descida_recursiva_em_c")) {
        add(titulo("parser.c"))
        add(codigo("s", CODIGO_EXPR, 70f, x = 40f, passo = 26f, size = 14f))
    }
    cena(tx("epemc_expr_chama_termo_while_repete")) { hot("s1", "s2") }
    cena(tx("epemc_termo_igual_com_vezes_fator")) {
        add(codigo("t", CODIGO_TERMO_FATOR, 250f, x = 14f, passo = 21f, size = 12f))
        hot("t4", "t5")
    }
}

/** A pilha de 2 * ( 3 + 4 ): o fator chama expr, e o while explica o E' do Templo. */
private fun EpisodeBuilder.rodaComParenteses(palco: PalcoDescida) {
    val k = segundoExpr(palco.rastro)
    cena(tx("emc2p_rodar")) {
        dropAll("s"); dropAll("t")
        add(rotulo("rp", tx("comum_pilha_chamadas"), 100f, 400f))
        add(codigo("g", listOf(tx("comum_expr_termo_termo"), tx("comum_termo_fator_fator"), tx("emc2p_g_fator")), 180f, x = 180f, passo = 30f, size = 11f))
        palco.mostra(0)
    }
    cena(tx("epemc_expr_chama_termo_termo_chama")) { palco.mostra(2) }
    cena(tx("epemc_no_vezes_quem_casa_termo"), who = PROF) { palco.mostra(palco.passo("casar", "*")) }
    pergunta(tx("emc2p_q"), setOf("f${palco.rastro[k].pilha.lastIndex - 1}"), setOf("f1", "f2"), tx("emc2p_q_dica"), tx("emc2p_q_ok")) { palco.mostra(k) }
    cena(tx("emc2p_fecha")) { palco.mostra(palco.passo("casar", ")")); hot() }
    cena(tx("emc2p_ponte"), who = PROF) {
        clear()
        add(titulo(tx("emc2p_titulo_ponte")))
        add(Box("tp", "E' → + T E' | ε", 180f, 110f, 260f, 44f, Look.CELL, size = 18f))
        add(codigo("w", listOf("while (tok.tipo == '+') {", "  casa('+'); termo();", "}"), 200f, x = 50f, passo = 28f, size = 14f))
        hot("tp", "w0")
    }
}
