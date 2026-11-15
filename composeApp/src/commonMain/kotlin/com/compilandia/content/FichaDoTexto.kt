package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.Txt

/**
 * Cartões da ficha que cobram o que só a aula de texto ("Ler com calma") ensina (padrão 7 da revisão).
 * A revisão relâmpago mostra a marca "Vem da aula Ler com calma" nesses cartões.
 * FichaDoTextoTest confere cada marca: o termo está num cartão e na aula de texto do assunto,
 * e não aparece em nenhuma aula animada dele. Os Bugs do álbum não podem usar esses termos.
 */
object FichaDoTexto {
    /** [noCartao] acha o cartão; [noTexto] mostra onde a aula de texto ensina, quando ela diz com outras palavras. */
    data class Marca(val noCartao: String, val noTexto: String = noCartao)

    private fun m(vararg termos: String) = termos.map { Marca(it) }

    val marcas: Map<Skill, List<Marca>> = mapOf(
        Skill.PHASES to m("front-end"),
        Skill.TOKENS to m("ponteiro", "padrão"),
        Skill.REGEX_DFA to m("Thompson", "subconjuntos", "2ⁿ", "concatenação", "aⁿbⁿ"),
        Skill.NFA_TO_DFA to m("Dtran", "exponencial"),
        Skill.DFA_MINIMIZATION to m("Hopcroft", "Myhill", "n(n−1)/2", "testemunha"),
        Skill.LEX_FLEX to m("yyleng", "yylval", "yytext", "lex.yy.c", "seções"),
        Skill.CFG_DERIVATION to listOf(Marca("componentes", "G = (N, T, P, S)"), Marca("produto")),
        Skill.AMBIGUITY to m("indecidível"),
        Skill.LEFT_RECURSION to m("ciclos"),
        Skill.LL1_TABLE to m("Leftmost"),
        Skill.LL1_PARSING to m("backtracking"),
        Skill.ERROR_RECOVERY to m("correção global", "viável", "yyerrok", "error ';'", "lógico"),
        Skill.SHIFT_REDUCE to m("Rightmost"),
        Skill.LALR to m("⊂", "a A d"),
        Skill.YACC_BISON to m("%nonassoc"),
        Skill.ATTRIBUTES_TYPES to m("inttofloat"),
        Skill.SCOPE_SYMBOLS to m("sombreamento", "dinâmico"),
        Skill.AST_BUILD to listOf(Marca("2n + 1", "2 × (nº de operadores) + 1"), Marca("Enquanto")),
        Skill.TAC to m("quádrupla", "tripla"),
        Skill.OPTIMIZATION to m("bloco básico", "líder", "redução de força", "propagação", "subexpressão comum"),
        Skill.CODEGEN_MIPS to m("syscall"),
        Skill.RUNTIME to m("vínculo", "heap", "\$ra"),
        Skill.REG_ALLOC to m("Chaitin", "use ∪", "callee", "caller", "ingênuo"),
    )

    /** O cartão (pergunta e resposta, em qualquer idioma) cobra algo que só a aula de texto ensina. */
    fun doTexto(s: Skill, pergunta: String, resposta: String): Boolean {
        val t = Txt.paraPt(pergunta) + "\n" + Txt.paraPt(resposta)
        return marcas[s].orEmpty().any { t.contains(it.noCartao, ignoreCase = true) }
    }
}
