package com.compilandia.content

import com.compilandia.content.episodes.MIPS_E
import com.compilandia.engine.MiniLang
import com.compilandia.textos.tx

/*
 * O e com curto-circuito no exemplo resolvido de TAC (professor do bloco 5): if (a < b && c < d) em TAC
 * e em MIPS, no mesmo esquema da aula de MIPS (ifFalse = slt + beq com $zero). Tudo sai do MiniLang.
 */
internal const val TAC_OU = "if (a < b || c < d) { x = 1; }"

internal fun tacCurtoCircuito(): List<Beat> = listOf(
    Beat.Say(Speaker.PROF, tx("tac_curto_exercicio")),
    Beat.Code(MiniLang.tac(MIPS_E).joinToString("\n") { if (it.endsWith(":")) it else "    $it" }, tx("tac_curto_legenda_tac")),
    Beat.Code(MiniLang.corpo(MIPS_E).joinToString("\n") { if (it.endsWith(":")) it else "    $it" }, tx("tac_curto_legenda_mips")),
    Beat.Predict(
        tx("tac_curto_preveja"),
        listOf(tx("tac_curto_op_nao"), tx("tac_curto_op_sim_antes"), tx("tac_curto_op_sim_ignora")),
        0,
        tx("tac_curto_explica"),
    ),
    Beat.Reveal(tx("tac_curto_ou"), tx("tac_curto_ou_resp", MiniLang.tac(TAC_OU).joinToString("; "))),
)
