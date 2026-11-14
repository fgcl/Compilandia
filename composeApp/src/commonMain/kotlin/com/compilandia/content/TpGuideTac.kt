package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.textos.tx

// Guia do TP, cap. 8: o TAC da prova ao lado do MIPS do gerador, e as bordas do MARS (revisor professor do bloco 6).
// O TAC daqui é o que o TacGen do motor gera; TpGuideTacTest confere.

/** O TAC de `soma <- soma + n * 2`, linha por linha, na ordem do TacGen. */
val TP_TAC_SOMA = listOf("t1 = n * 2", "t2 = soma + t1", "soma = t2")

/** Onde começa a coluna do MIPS: estreita, para caber no celular. */
const val TP_TAC_COLUNA = 18

/** O MIPS que o gerador escreve para a mesma linha, com a linha de TAC que termina em cada instrução. */
private val MIPS_SOMA = listOf(
    "" to $$"lw   $t0, soma_1", "" to $$"lw   $t1, n_0", "" to $$"li   $t2, 2",
    TP_TAC_SOMA[0] to $$"mul  $t1, $t1, $t2", TP_TAC_SOMA[1] to $$"add  $t0, $t0, $t1", TP_TAC_SOMA[2] to $$"sw   $t0, soma_1",
)

internal val tpCap8TacExpr = Code(
    (listOf("# soma <- soma + n * 2;", "# TAC".padEnd(TP_TAC_COLUNA) + "# MIPS (saida.asm)") +
        MIPS_SOMA.map { (tac, mips) -> tac.padEnd(TP_TAC_COLUNA) + mips }).joinToString("\n"),
    tx("tptac_legenda_expr"),
)

internal val tpCap8TacEnquanto = Code(
    $$"""
    # enquanto (n > 0) { ... }
    # TAC                      # MIPS
    enquanto0:                 enquanto0:
                                 lw   $t0, n_0
                                 li   $t1, 0
      t1 = n > 0                 sgt  $t0, $t0, $t1
      ifFalse t1 goto fimenq1    beq  $t0, $zero, fimenq1
      ...                        ...
      goto enquanto0             j    enquanto0
    fimenq1:                   fimenq1:
    """.trimIndent(),
    tx("tptac_legenda_enquanto"),
)

internal val tpCap8BordasMars = Callout(WATCH_OUT, tx("tptac_bordas_mars"))

internal val tpCap8BordasConserto = Code(
    $$"""
    // argumento(): dobre a barra, para o \ do fim não engolir a aspa
    String texto = s.lexema.replace("\\", "\\\\");
    strings.append(r).append(": .asciiz \"").append(texto).append("\"\n");

    // declara(), na TabelaSimbolos: rótulo só com ASCII; o _N continua deixando ele único
    String base = nome.replaceAll("[^A-Za-z0-9_]", "_");          // média -> m_dia
    Simbolo s = new Simbolo(nome, tipo, base + "_" + (contador++), linha);
    """.trimIndent(),
    tx("tptac_legenda_conserto"),
)
