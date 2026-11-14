package com.compilandia.content

import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.textos.tx

/*
 * A Byteiro inteira em BNF, para a prova de LL(1) do relatório (capítulo 4; revisor aluno do bloco 6).
 * Cada { α } da EBNF do capítulo 2 vira X -> α X | ε, e cada [ α ] vira Y -> α | ε.
 * O FIRST, o FOLLOW e a tabela saem do motor; o teste confere que não há conflito.
 */
internal const val TP_BYTEIRO_BNF = """Programa -> programa id Bloco
Bloco -> { Cmds }
Cmds -> Cmd Cmds | ε
Cmd -> Decl | id <- Expr ; | Bloco
    | se ( Expr ) Bloco Senao
    | enquanto ( Expr ) Bloco
    | escreva ( Arg Args ) ;
    | leia ( id ) ;
Decl -> Tipo id Ids ;
Ids -> , id Ids | ε
Tipo -> inteiro | logico | caractere
Senao -> senao Bloco | ε
Args -> , Arg Args | ε
Arg -> string | Expr
Expr -> ExprE Ou
Ou -> ou ExprE Ou | ε
ExprE -> Rel E2
E2 -> e Rel E2 | ε
Rel -> Add RelR
RelR -> oprel Add | ε
Add -> Mul AddR
AddR -> + Mul AddR | - Mul AddR | ε
Mul -> Un MulR
MulR -> * Un MulR | / Un MulR | % Un MulR | ε
Un -> nao Un | - Un | Prim
Prim -> ( Expr ) | id | num | char
    | verdadeiro | falso"""

/** Uma linha por não-terminal: "**Cmd**: FIRST = { ... }; FOLLOW = { ... }", na ordem da gramática. */
internal fun tabelaFirstFollowByteiro(): List<String> {
    val g = Grammar.parse(TP_BYTEIRO_BNF)
    val ff = FirstFollow(g)
    fun conj(s: Set<String>) = s.joinToString(" ", "{ ", " }") { "`$it`" }
    return g.nonTerminals.map { nt ->
        "**$nt**: FIRST = ${conj(ff.first.getValue(nt))}; FOLLOW = ${conj(ff.follow.getValue(nt))}"
    }
}

/** Os blocos do capítulo 4 com a gramática inteira e a tabela. */
internal val tpByteiroInteira: List<Beat> get() = listOf(
    Beat.Text(tx("tpguideb_byteiro_inteira_intro")),
    Beat.Code(TP_BYTEIRO_BNF, tx("tpguideb_byteiro_inteira_bnf")),
    Beat.Steps(tx("tpguideb_byteiro_inteira_tabela"), tabelaFirstFollowByteiro()),
    Beat.Callout(CalloutKind.EXAM_TIP, tx("tpguideb_byteiro_inteira_prova")),
)
