package com.compilandia.content

import com.compilandia.textos.tx

/*
 * O fim de cada capítulo do Guia do TP (revisor aluno do bloco 6): "Na sua linguagem", com três perguntas
 * que levam o exemplo da Byteiro para a linguagem do enunciado, e "Teste desta etapa", com o que rodar e o
 * que deve sair. As mensagens de erro são as que o próprio guia gera; AulasConsertadasTest confere.
 * O capítulo 1 já tem o teste dele logo depois do Main.
 */

/** Os textos de cada capítulo, com as chaves por extenso para o TextosTest achar. */
private class Fechamento(val perguntas: List<String>, val teste: String?, val codigo: String?)

private fun fechamentos() = mapOf(
    1 to Fechamento(listOf(tx("tp_nsl_1_1"), tx("tp_nsl_1_2"), tx("tp_nsl_1_3")), null, null),
    2 to Fechamento(listOf(tx("tp_nsl_2_1"), tx("tp_nsl_2_2"), tx("tp_nsl_2_3")), tx("tp_teste_2"), null),
    3 to Fechamento(listOf(tx("tp_nsl_3_1"), tx("tp_nsl_3_2"), tx("tp_nsl_3_3")), tx("tp_teste_3"), tx("tp_teste_codigo_3")),
    4 to Fechamento(listOf(tx("tp_nsl_4_1"), tx("tp_nsl_4_2"), tx("tp_nsl_4_3")), tx("tp_teste_4"), null),
    5 to Fechamento(listOf(tx("tp_nsl_5_1"), tx("tp_nsl_5_2"), tx("tp_nsl_5_3")), tx("tp_teste_5"), tx("tp_teste_codigo_5")),
    6 to Fechamento(listOf(tx("tp_nsl_6_1"), tx("tp_nsl_6_2"), tx("tp_nsl_6_3")), tx("tp_teste_6"), tx("tp_teste_codigo_6")),
    7 to Fechamento(listOf(tx("tp_nsl_7_1"), tx("tp_nsl_7_2"), tx("tp_nsl_7_3")), tx("tp_teste_7"), null),
    8 to Fechamento(listOf(tx("tp_nsl_8_1"), tx("tp_nsl_8_2"), tx("tp_nsl_8_3")), tx("tp_teste_8"), tx("tp_teste_codigo_8")),
    9 to Fechamento(listOf(tx("tp_nsl_9_1"), tx("tp_nsl_9_2"), tx("tp_nsl_9_3")), tx("tp_teste_9"), tx("tp_teste_codigo_9")),
    10 to Fechamento(listOf(tx("tp_nsl_10_1"), tx("tp_nsl_10_2"), tx("tp_nsl_10_3")), tx("tp_teste_10"), null),
    11 to Fechamento(listOf(tx("tp_nsl_11_1"), tx("tp_nsl_11_2"), tx("tp_nsl_11_3")), tx("tp_teste_11"), null),
)

internal fun fechamentoDoCapitulo(n: Int): List<Beat> = buildList {
    val f = fechamentos()[n] ?: return@buildList
    add(Beat.Steps(tx("tp_nsl_titulo"), f.perguntas))
    f.teste?.let { add(Beat.Text(it)) }
    f.codigo?.let { add(Beat.Code(it, tx("tp_teste_titulo"))) }
    estendaByteiro()[n]?.let { (pergunta, resposta) -> add(Beat.Reveal(pergunta, resposta)) }
}

/**
 * "Estenda a Byteiro" (professor do bloco 6): o mesmo comando novo, `repita ... ate`, atravessa os capítulos,
 * um passo por capítulo, com a resposta escondida. PedidosProfessoresTest confere a gramática, o molde e a contagem de erros.
 */
private fun estendaByteiro() = mapOf(
    1 to (tx("tp_estenda_1") to tx("tp_estenda_1_r")),
    2 to (tx("tp_estenda_2") to tx("tp_estenda_2_r")),
    3 to (tx("tp_estenda_3") to tx("tp_estenda_3_r")),
    4 to (tx("tp_estenda_4") to tx("tp_estenda_4_r")),
    5 to (tx("tp_estenda_5") to tx("tp_estenda_5_r")),
    6 to (tx("tp_estenda_6") to tx("tp_estenda_6_r")),
    7 to (tx("tp_estenda_7") to tx("tp_estenda_7_r")),
    8 to (tx("tp_estenda_8") to tx("tp_estenda_8_r")),
    9 to (tx("tp_estenda_9") to tx("tp_estenda_9_r")),
    10 to (tx("tp_estenda_10") to tx("tp_estenda_10_r")),
)
