package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Um termo do glossário tocável (A8). [formas] são as grafias que o texto pode usar, separadas por |. */
data class Termo(val id: String, val nome: String, val formas: List<String>, val definicao: String, val skill: Skill)

/** Glossário tocável: os termos que ficam sublinhados em legendas, aulas de texto e Laboratório. */
object Glossario {
    private fun t(id: String, nome: String, formas: String, def: String, s: Skill) = Termo(id, nome, formas.split("|").map { it.trim() }.filter { it.isNotEmpty() }, def, s)

    val termos: List<Termo> by lazy {
        listOf(
            t("token", tx("gl_token_nome"), tx("gl_token_formas"), tx("gl_token_def"), Skill.TOKENS),
            t("lexema", tx("gl_lexema_nome"), tx("gl_lexema_formas"), tx("gl_lexema_def"), Skill.TOKENS),
            t("automato", tx("gl_automato_nome"), tx("gl_automato_formas"), tx("gl_automato_def"), Skill.REGEX_DFA),
            t("afd", tx("gl_afd_nome"), tx("gl_afd_formas"), tx("gl_afd_def"), Skill.REGEX_DFA),
            t("afn", tx("gl_afn_nome"), tx("gl_afn_formas"), tx("gl_afn_def"), Skill.NFA_TO_DFA),
            t("epsilon", tx("gl_epsilon_nome"), tx("gl_epsilon_formas"), tx("gl_epsilon_def"), Skill.FIRST),
            t("gramatica", tx("gl_gramatica_nome"), tx("gl_gramatica_formas"), tx("gl_gramatica_def"), Skill.CFG_DERIVATION),
            t("terminal", tx("gl_terminal_nome"), tx("gl_terminal_formas"), tx("gl_terminal_def"), Skill.CFG_DERIVATION),
            t("naoterminal", tx("gl_naoterminal_nome"), tx("gl_naoterminal_formas"), tx("gl_naoterminal_def"), Skill.CFG_DERIVATION),
            t("derivacao", tx("gl_derivacao_nome"), tx("gl_derivacao_formas"), tx("gl_derivacao_def"), Skill.CFG_DERIVATION),
            t("ambigua", tx("gl_ambigua_nome"), tx("gl_ambigua_formas"), tx("gl_ambigua_def"), Skill.AMBIGUITY),
            t("recursao", tx("gl_recursao_nome"), tx("gl_recursao_formas"), tx("gl_recursao_def"), Skill.LEFT_RECURSION),
            t("fatoracao", tx("gl_fatoracao_nome"), tx("gl_fatoracao_formas"), tx("gl_fatoracao_def"), Skill.LEFT_FACTORING),
            t("first", tx("gl_first_nome"), tx("gl_first_formas"), tx("gl_first_def"), Skill.FIRST),
            t("follow", tx("gl_follow_nome"), tx("gl_follow_formas"), tx("gl_follow_def"), Skill.FOLLOW),
            t("ll1", tx("gl_ll1_nome"), tx("gl_ll1_formas"), tx("gl_ll1_def"), Skill.LL1_TABLE),
            t("lookahead", tx("gl_lookahead_nome"), tx("gl_lookahead_formas"), tx("gl_lookahead_def"), Skill.LL1_TABLE),
            t("handle", tx("gl_handle_nome"), tx("gl_handle_formas"), tx("gl_handle_def"), Skill.SHIFT_REDUCE),
            t("shift", tx("gl_shift_nome"), tx("gl_shift_formas"), tx("gl_shift_def"), Skill.SHIFT_REDUCE),
            t("reduce", tx("gl_reduce_nome"), tx("gl_reduce_formas"), tx("gl_reduce_def"), Skill.SHIFT_REDUCE),
            t("item", tx("gl_item_nome"), tx("gl_item_formas"), tx("gl_item_def"), Skill.LR0_ITEMS),
            t("closure", tx("gl_closure_nome"), tx("gl_closure_formas"), tx("gl_closure_def"), Skill.LR0_ITEMS),
            t("goto", tx("gl_goto_nome"), tx("gl_goto_formas"), tx("gl_goto_def"), Skill.LR0_ITEMS),
            t("conflito", tx("gl_conflito_nome"), tx("gl_conflito_formas"), tx("gl_conflito_def"), Skill.SLR_TABLE),
            t("lalr", tx("gl_lalr_nome"), tx("gl_lalr_formas"), tx("gl_lalr_def"), Skill.LALR),
            t("precedencia", tx("gl_precedencia_nome"), tx("gl_precedencia_formas"), tx("gl_precedencia_def"), Skill.YACC_BISON),
            t("atributo", tx("gl_atributo_nome"), tx("gl_atributo_formas"), tx("gl_atributo_def"), Skill.ATTRIBUTES_TYPES),
            t("escopo", tx("gl_escopo_nome"), tx("gl_escopo_formas"), tx("gl_escopo_def"), Skill.SCOPE_SYMBOLS),
            t("tabelasimbolos", tx("gl_tabelasimbolos_nome"), tx("gl_tabelasimbolos_formas"), tx("gl_tabelasimbolos_def"), Skill.SCOPE_SYMBOLS),
            t("ast", tx("gl_ast_nome"), tx("gl_ast_formas"), tx("gl_ast_def"), Skill.AST_BUILD),
            t("tresenderecos", tx("gl_tresenderecos_nome"), tx("gl_tresenderecos_formas"), tx("gl_tresenderecos_def"), Skill.TAC),
            t("temporario", tx("gl_temporario_nome"), tx("gl_temporario_formas"), tx("gl_temporario_def"), Skill.TAC),
            t("registrador", tx("gl_registrador_nome"), tx("gl_registrador_formas"), tx("gl_registrador_def"), Skill.CODEGEN_MIPS),
            t("mips", tx("gl_mips_nome"), tx("gl_mips_formas"), tx("gl_mips_def"), Skill.CODEGEN_MIPS),
            t("registroativacao", tx("gl_registroativacao_nome"), tx("gl_registroativacao_formas"), tx("gl_registroativacao_def"), Skill.RUNTIME),
            t("spill", tx("gl_spill_nome"), tx("gl_spill_formas"), tx("gl_spill_def"), Skill.REG_ALLOC),
            t("cifrao", tx("gl_cifrao_nome"), tx("gl_cifrao_formas"), tx("gl_cifrao_def"), Skill.FOLLOW),
        )
    }

    /** Todas as formas, da mais longa para a mais curta: "tabela de símbolos" casa antes de "tabela". */
    private val formas: List<Pair<String, Termo>> by lazy { termos.flatMap { t -> t.formas.map { it to t } }.sortedByDescending { it.first.length } }

    private fun letra(c: Char) = c.isLetterOrDigit() || c == '-' || c == '(' || c == ')'

    /**
     * Onde os termos aparecem em [texto]: (início, fim, termo), sem sobreposição. Palavra inteira; sigla em maiúsculas
     * só casa com maiúsculas, palavra comum casa sem olhar maiúscula. Cada termo é marcado uma vez só por texto.
     */
    fun achar(texto: String): List<Triple<Int, Int, Termo>> {
        val achados = mutableListOf<Triple<Int, Int, Termo>>()
        val usados = mutableSetOf<String>()
        for ((f, t) in formas) {
            if (t.id in usados) continue
            val sigla = f.any { it.isUpperCase() } && f == f.uppercase()
            var de = 0
            while (true) {
                val i = texto.indexOf(f, de, ignoreCase = !sigla)
                if (i < 0) break
                val fim = i + f.length
                val antes = i == 0 || !letra(texto[i - 1])
                val depois = fim >= texto.length || !texto[fim].isLetterOrDigit()
                if (antes && depois && achados.none { i < it.second && fim > it.first }) {
                    achados += Triple(i, fim, t); usados += t.id; break
                }
                de = i + 1
            }
        }
        return achados.sortedBy { it.first }
    }
}
