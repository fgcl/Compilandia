package com.compilandia.content.episodes

import com.compilandia.content.Fala
import com.compilandia.content.Speaker
import com.compilandia.learning.Skill

/** Antes da aula, o Bug ataca: 10 segundos para tentar sozinho. Errar aqui prepara a explicação. */
data class Abertura(val skill: Skill, val manchete: String, val desafio: String, val fala: Fala, val opcoes: List<String>, val certa: Int, val depois: Fala)

private fun ab(s: Skill, manchete: String, desafio: String, fala: String, opcoes: List<String>, certa: Int, depois: String) =
    Abertura(s, manchete, desafio, Fala(Speaker.BYTE, fala), opcoes, certa, Fala(Speaker.BYTE, depois))

object Aberturas {
    val todas = listOf(
        ab(Skill.PHASES, "O Bug embaralhou as fases", "léxica  ?  semântica", "Antes da aula: qual fase vem no meio?", listOf("sintática", "otimização", "geração"), 0, "A sintática. Os tokens viram árvore antes de checar o sentido."),
        ab(Skill.TOKENS, "O Bug colou os tokens", "x>=10", "Antes da aula: quantos tokens tem esta linha?", listOf("2", "3", "5"), 1, "Três: x, maior ou igual, e dez."),
        ab(Skill.REGEX_DFA, "O Bug soltou uma regex", "a*b", "Antes da aula: qual cadeia casa com ela?", listOf("ba", "aab", "abb"), 1, "Vale a a b. Quantos a quiser, e um b no fim."),
        ab(Skill.NFA_TO_DFA, "O Bug duplicou as setas", "q0 a q1\nq0 a q2", "Antes da aula: lendo a, para onde o autômato vai?", listOf("q1", "q2", "os dois"), 2, "Para os dois. O AFD guarda esse conjunto como um estado só."),
        ab(Skill.DFA_MINIMIZATION, "O Bug clonou um estado", "q1 = q2", "Antes da aula: dois estados fazem tudo igual. O que fazer?", listOf("apagar", "juntar", "deixar"), 1, "Juntar. Um estado só faz o mesmo trabalho."),
        ab(Skill.LEX_FLEX, "O Bug trocou as regras", "[a-z]+  antes de  \"if\"", "Antes da aula: a palavra if vira qual token?", listOf("ID", "IF", "erro"), 0, "Vira ID. No empate, ganha a regra que vem primeiro."),
        ab(Skill.CFG_DERIVATION, "O Bug comeu uma troca", "S -> a S b | c", "Antes da aula: qual frase sai dessa regra?", listOf("a c b", "a b c", "c c"), 0, "Vale a c b. S vira a S b, e o S de dentro vira c."),
        ab(Skill.AMBIGUITY, "O Bug fez duas árvores", "2 + 3 * 4", "Antes da aula: se o mais rodar primeiro, dá quanto?", listOf("14", "20", "24"), 1, "Vinte. Duas árvores, dois resultados."),
        ab(Skill.LEFT_RECURSION, "O Bug prendeu o parser", "E -> E + T | T", "Antes da aula: o que o parser descendente faz aqui?", listOf("lê o +", "gira sem ler", "aceita"), 1, "Gira sem ler nada. Ele troca E por E para sempre."),
        ab(Skill.LEFT_FACTORING, "O Bug grudou dois começos", "A -> a b | a c", "Antes da aula: olhando só o a, dá para escolher a opção?", listOf("sim", "não", "às vezes"), 1, "Não dá. As duas começam igual."),
        ab(Skill.FIRST, "O Bug comeu o FIRST", "S -> a b | c", "Antes da aula: o que pode abrir uma frase de S?", listOf("a e c", "a e b", "só a"), 0, "O a e o c. Cada opção abre com um deles."),
        ab(Skill.FOLLOW, "O Bug comeu o FOLLOW", "E -> T + E | T", "Antes da aula: o que pode vir logo depois de T?", listOf("+", "id", "ε"), 0, "O mais. Olhe o que está à direita de T na regra."),
        ab(Skill.LL1_TABLE, "O Bug riscou a tabela", "E -> id | ( E )", "Antes da aula: com id na entrada, qual opção de E vale?", listOf("id", "( E )", "nenhuma"), 0, "A opção id. Cada token aponta para uma regra."),
        ab(Skill.LL1_PARSING, "O Bug derrubou a pilha", "topo: id   entrada: id", "Antes da aula: topo e entrada iguais. O que fazer?", listOf("casar", "expandir", "erro"), 0, "Casar. Os dois saem, e a máquina anda."),
        ab(Skill.RECURSIVE_DESCENT, "O Bug apagou uma função", "E -> T X", "Antes da aula: quantas funções a função E precisa chamar?", listOf("1", "2", "3"), 1, "Duas: T e X."),
        ab(Skill.ERROR_RECOVERY, "O Bug plantou um erro", "x = = 2;  y = 3;", "Antes da aula: onde o parser recomeça?", listOf("no segundo =", "depois do ;", "no fim"), 1, "Depois do ponto e vírgula. Ali começa outro comando."),
        ab(Skill.SHIFT_REDUCE, "O Bug travou a esteira", "pilha: E + T   entrada: fim", "Antes da aula: a entrada acabou. O que a máquina faz?", listOf("reduz", "empilha", "para"), 0, "Reduz. Não há mais nada para empilhar."),
        ab(Skill.LR0_ITEMS, "O Bug moveu o ponto", "E -> E . + T", "Antes da aula: o que a máquina espera ler agora?", listOf("+", "E", "T"), 0, "O mais. O ponto fica logo antes dele."),
        ab(Skill.SLR_TABLE, "O Bug cruzou duas ações", "empilhar / reduzir", "Antes da aula: uma casa com as duas ações é o quê?", listOf("conflito", "erro léxico", "normal"), 0, "Conflito. A tabela deu duas ordens de uma vez."),
        ab(Skill.LALR, "O Bug juntou estados", "LR(1)   LALR   SLR", "Antes da aula: qual deles costuma ter mais estados?", listOf("LR(1)", "LALR", "SLR"), 0, "O LR(1). O LALR nasce juntando estados dele."),
        ab(Skill.YACC_BISON, "O Bug trocou os left", "%left '*'\n%left '+'", "Antes da aula: quem liga mais forte aqui?", listOf("+", "*", "empate"), 0, "O mais. Quanto mais embaixo, mais forte."),
        ab(Skill.ATTRIBUTES_TYPES, "O Bug apagou um tipo", "2 + 1.5", "Antes da aula: qual é o tipo do resultado?", listOf("int", "float", "erro"), 1, "Float. O compilador converte o inteiro antes da conta."),
        ab(Skill.SCOPE_SYMBOLS, "O Bug escondeu um x", "int x; { int x; x = 1; }", "Antes da aula: esse x igual a um mexe em qual x?", listOf("o de dentro", "o de fora", "nos dois"), 0, "No de dentro. A busca começa pelo bloco mais próximo."),
        ab(Skill.AST_BUILD, "O Bug podou a árvore", "( a + b )", "Antes da aula: os parênteses entram na AST?", listOf("sim", "não", "só um"), 1, "Não. A forma da árvore já mostra o agrupamento."),
        ab(Skill.TAC, "O Bug quebrou a conta", "a + b * c", "Antes da aula: quantas linhas de três endereços dá isso?", listOf("1", "2", "3"), 1, "Duas. Uma conta por linha."),
        ab(Skill.OPTIMIZATION, "O Bug deixou conta à toa", "x = 2 * 3;", "Antes da aula: o compilador pode fazer essa conta sozinho?", listOf("sim", "não", "só em C"), 0, "Pode. Ele já escreve seis no código."),
        ab(Skill.RUNTIME, "O Bug encheu a pilha", "f(3) chama f(2)", "Antes da aula: quem sai primeiro da pilha?", listOf("f(3)", "f(2)", "nenhum"), 1, "F de dois. O último a entrar sai primeiro."),
        ab(Skill.CODEGEN_MIPS, "O Bug roubou um registrador", "add \$t0, \$t1, x", "Antes da aula: essa instrução vale em MIPS?", listOf("sim", "não", "só com sw"), 1, "Não vale. O add só usa registradores. O x precisa vir antes com lw."),
        ab(Skill.REG_ALLOC, "O Bug sumiu com um registrador", "t1 e t2 vivos juntos", "Antes da aula: eles podem dividir um registrador?", listOf("sim", "não", "às vezes"), 1, "Não podem. Um apagaria o outro."),
    )

    fun of(s: Skill): Abertura? = todas.firstOrNull { it.skill == s }

    fun falas(): List<Fala> = todas.flatMap { listOf(it.fala, it.depois) }
}
