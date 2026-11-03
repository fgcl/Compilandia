package com.compilandia.content

import com.compilandia.learning.Skill

/** Uma frase para julgar: é verdade ou é conversa do Bug? */
data class Afirma(val texto: String, val verdade: Boolean, val porque: String)

/**
 * Cada erro comum de prova é um Bug com nome. Para colar a figurinha, a pessoa julga as frases dele.
 * [mentira] é o que o Bug diz; [verdade] desmonta o erro. Nomear o erro funciona: refutação tem g = 0,41.
 */
data class Especie(val id: String, val nome: String, val skill: Skill, val mentira: String, val verdade: String, val testes: List<Afirma>)

private fun v(t: String, p: String = "Isso mesmo.") = Afirma(t, true, p)
private fun f(t: String, p: String) = Afirma(t, false, p)

object Album {
    val especies = listOf(
        Especie("fase-unica", "Bug da Fase Única", Skill.PHASES, "O compilador faz tudo de uma vez só.", "São fases em sequência. A saída de uma é a entrada da outra.", listOf(
            v("A análise léxica entrega tokens para a sintática."),
            f("O erro de tipo é pego na fase léxica.", "Tipo é assunto da semântica."),
            f("A otimização vem antes da análise sintática.", "Só dá para otimizar depois de entender o código."),
        )),
        Especie("erro-trocado", "Bug do Erro Trocado", Skill.PHASES, "Todo erro é erro de sintaxe.", "Cada fase pega um tipo de erro.", listOf(
            v("Variável não declarada é erro semântico."),
            v("Faltar ponto e vírgula é erro sintático."),
            f("Um @ perdido no código é erro semântico.", "O scanner pega: nenhum token começa com @."),
        )),
        Especie("lexema-gemeo", "Bug do Lexema Gêmeo", Skill.TOKENS, "Token e lexema são a mesma coisa.", "Lexema é o texto. Token é a classe: id, num, if.", listOf(
            v("Em conta = 10, conta é lexema e id é token."),
            v("Dois lexemas diferentes podem ter o mesmo token."),
            f("Cada lexema tem um token só dele.", "x e y têm o mesmo token: id."),
        )),
        Especie("corte-curto", "Bug do Corte Curto", Skill.TOKENS, ">= são dois tokens: > e =.", "O scanner pega o maior pedaço que casa.", listOf(
            v("ifa é um identificador."),
            f("Em x>=10, o scanner fecha > assim que o vê.", "Ele tenta esticar e fecha >=."),
            f("ifa vira o token if seguido de a.", "Maior casamento: fica ifa inteiro."),
        )),
        Especie("palavra-comum", "Bug da Palavra Comum", Skill.LEX_FLEX, "if é um nome como outro qualquer.", "No empate de tamanho, ganha a regra que vem primeiro. A reservada precisa vir antes.", listOf(
            v("No Flex, a regra que vem primeiro ganha o empate."),
            v("O maior casamento decide antes da ordem das regras."),
            f("yytext guarda a classe do token.", "yytext guarda o lexema."),
        )),
        Especie("estrela-cheia", "Bug da Estrela Cheia", Skill.REGEX_DFA, "a* exige pelo menos um a.", "A estrela aceita zero ou mais. A cadeia vazia entra.", listOf(
            v("a+ exige pelo menos um a."),
            v("A cadeia vazia casa com a*."),
            f("(ab)* aceita aba.", "Só repetições inteiras de ab."),
        )),
        Especie("regex-conta", "Bug da Regex que Conta", Skill.REGEX_DFA, "Regex confere parênteses balanceados.", "Autômato finito não conta sem limite. Para isso existe gramática.", listOf(
            v("a^n b^n não é linguagem regular."),
            v("Gramática livre de contexto confere parênteses."),
            f("Um AFD tem memória infinita.", "Ele só lembra o estado atual."),
        )),
        Especie("fecho-esquecido", "Bug do Fecho Esquecido", Skill.NFA_TO_DFA, "O início do AFD é só o início do AFN.", "É o fecho dele: tudo que se alcança sem ler nada.", listOf(
            v("Cada estado do AFD é um conjunto de estados do AFN."),
            v("Um conjunto é final se tiver um estado final do AFN."),
            f("Depois de ler uma letra, não preciso fechar de novo.", "Sempre feche depois do move."),
        )),
        Especie("gemeos", "Bug dos Gêmeos", Skill.DFA_MINIMIZATION, "Estados com o mesmo número de setas são iguais.", "Só são gêmeos se nenhuma cadeia separa os dois.", listOf(
            v("Um estado final e um não final nunca são gêmeos."),
            v("A tabela de pares começa separando finais de não finais."),
            f("O autômato mínimo aceita outra linguagem.", "A linguagem é a mesma, com menos estados."),
        )),
        Especie("terminal-trocado", "Bug do Terminal Trocado", Skill.CFG_DERIVATION, "Terminal é o que ainda vai virar outra coisa.", "Não terminal é o que se troca. Terminal fica até o fim.", listOf(
            v("As folhas da árvore de uma frase são terminais."),
            v("Uma frase só tem terminais."),
            f("Forma sentencial nunca tem não terminal.", "Ela pode ter os dois tipos de símbolo."),
        )),
        Especie("else-solto", "Bug do Else Solto", Skill.AMBIGUITY, "O else é do if de fora.", "O else fica com o if mais perto.", listOf(
            v("if a then if b then x else y tem duas árvores nessa gramática."),
            v("O Bison resolve esse caso empilhando o else."),
            f("Reescrever a gramática nunca resolve o else solto.", "Dá para reescrever separando comandos fechados e abertos."),
        )),
        Especie("duas-contas", "Bug das Duas Contas", Skill.AMBIGUITY, "Ambiguidade é só detalhe de desenho.", "Duas árvores dão dois resultados: 2 + 3 * 4 vira 14 ou 20.", listOf(
            v("Gramática ambígua tem uma frase com duas árvores."),
            v("Duas derivações pela esquerda diferentes provam ambiguidade."),
            f("Toda gramática de expressões é ambígua.", "Com níveis E, T e F ela deixa de ser."),
        )),
        Especie("laco-eterno", "Bug do Laço Eterno", Skill.LEFT_RECURSION, "Recursão à esquerda só deixa o parser lento.", "Ela trava o parser descendente: ele chama a mesma regra sem ler nada.", listOf(
            v("Parser LR aceita recursão à esquerda."),
            v("E -> E + T | T vira E -> T X e X -> + T X | ε."),
            f("Recursão à direita trava o LL(1).", "À direita, ele lê um token antes de voltar."),
        )),
        Especie("comeco-igual", "Bug do Começo Igual", Skill.LEFT_FACTORING, "Fatorar muda a linguagem.", "A linguagem é a mesma. Só adia a escolha.", listOf(
            v("A -> a b | a c vira A -> a X e X -> b | c."),
            v("Fatorar acrescenta um nível na árvore."),
            f("Fatorar resolve qualquer conflito LL(1).", "Conflito com FOLLOW, por exemplo, continua lá."),
        )),
        Especie("primeiro-so", "Bug do Primeiro Só", Skill.FIRST, "FIRST é só o primeiro símbolo da regra.", "Se ele é não terminal, vale o FIRST dele. Se ele some, olha o próximo.", listOf(
            v("Se B some em A -> B C, FIRST de C entra em FIRST de A."),
            v("ε entra em FIRST de A se A pode virar vazio."),
            f("FIRST de um terminal é vazio.", "FIRST de terminal é ele mesmo."),
        )),
        Especie("follow-vazio", "Bug do FOLLOW Vazio", Skill.FOLLOW, "ε pode entrar no FOLLOW.", "FOLLOW nunca tem ε. Ele guarda o que vem depois, e o fim é $.", listOf(
            v("$ entra no FOLLOW do símbolo inicial."),
            v("FOLLOW de A recebe o FIRST do que vem depois de A, sem ε."),
            f("Se A fecha a regra de B, FOLLOW de A entra em FOLLOW de B.", "É ao contrário: FOLLOW de B entra em FOLLOW de A."),
        )),
        Especie("casa-dupla", "Bug da Casa Dupla", Skill.LL1_TABLE, "Duas regras na mesma casa é normal.", "Casa com duas regras é conflito. A gramática não é LL(1).", listOf(
            v("A regra entra na coluna de cada símbolo do FIRST do seu lado direito."),
            v("Se o lado direito pode sumir, a regra entra nas colunas do FOLLOW."),
            f("Toda gramática sem recursão à esquerda é LL(1).", "S -> A b, A -> b | ε não tem recursão e não é LL(1)."),
        )),
        Especie("pilha-invertida", "Bug da Pilha Invertida", Skill.LL1_PARSING, "O lado direito entra na pilha na ordem escrita.", "Entra de trás para frente, para o primeiro símbolo ficar no topo.", listOf(
            v("Terminal no topo precisa casar com a entrada."),
            v("Casa vazia na tabela significa erro."),
            f("Entrada acabou com a pilha cheia: frase aceita.", "Só aceita com pilha e entrada no cifrão."),
        )),
        Especie("token-avancado", "Bug do Token Avançado", Skill.RECURSIVE_DESCENT, "Toda função lê um token antes de decidir.", "A função olha o token atual e só avança quando casa.", listOf(
            v("Cada não terminal vira uma função."),
            v("casaToken confere e avança a entrada."),
            f("Descida recursiva roda E -> E + T como está.", "Ela entraria em recursão infinita."),
        )),
        Especie("para-tudo", "Bug do Para Tudo", Skill.ERROR_RECOVERY, "No primeiro erro, o compilador tem que parar.", "Ele se recupera e segue, para mostrar vários erros.", listOf(
            v("O modo pânico descarta tokens até um sincronizador."),
            v("Ponto e vírgula costuma ser bom sincronizador."),
            f("Recuperar do erro conserta o programa.", "Só permite achar os próximos erros."),
        )),
        Especie("reduz-cedo", "Bug do Reduz Cedo", Skill.SHIFT_REDUCE, "Viu lado direito na pilha? Reduza na hora.", "Depende de quem vem. Com E + T na pilha e * na entrada, empilha.", listOf(
            v("Reduzir troca o topo pelo lado esquerdo da regra."),
            v("Shift-reduce faz a derivação mais à direita ao contrário."),
            f("Handle é qualquer pedaço igual a um lado direito.", "Handle é o pedaço certo de reduzir agora."),
        )),
        Especie("fecho-curto", "Bug do Fecho Curto", Skill.LR0_ITEMS, "Closure só copia o item.", "Se o ponto está antes de um não terminal, as regras dele entram.", listOf(
            v("O ponto marca até onde a regra foi lida."),
            v("Item com o ponto no fim pede redução."),
            f("Todo estado LR(0) tem um item só.", "O fecho costuma puxar vários."),
        )),
        Especie("follow-cego", "Bug do FOLLOW Cego", Skill.SLR_TABLE, "O SLR reduz em qualquer token.", "Só nos tokens do FOLLOW do lado esquerdo.", listOf(
            v("LR(0) reduz sem olhar a entrada."),
            v("Conflito shift-reduce é dúvida entre empilhar e reduzir."),
            f("Se o SLR tem conflito, a gramática é ambígua.", "S -> L = R mostra o contrário."),
        )),
        Especie("estado-repetido", "Bug do Estado Repetido", Skill.LALR, "LALR tem os estados do LR(1).", "LALR funde estados de mesmo miolo. Fica do tamanho do SLR.", listOf(
            v("Juntar estados pode criar conflito reduce-reduce."),
            v("Juntar estados não cria shift-reduce novo."),
            f("SLR e LALR usam os mesmos símbolos de previsão.", "O SLR usa FOLLOW. O LALR é mais fino."),
        )),
        Especie("precedencia-trocada", "Bug da Precedência Trocada", Skill.YACC_BISON, "O primeiro %left é o mais forte.", "Quanto mais embaixo no arquivo, mais forte.", listOf(
            v("%left faz 8 - 3 - 2 agrupar pela esquerda."),
            v("Sem declaração, em shift-reduce o Bison empilha."),
            f("Em reduce-reduce, o Bison escolhe a última regra.", "Ele escolhe a primeira do arquivo."),
        )),
        Especie("herdado", "Bug do Herdado", Skill.ATTRIBUTES_TYPES, "Todo atributo sobe na árvore.", "Sintetizado sobe. Herdado desce ou vem do lado.", listOf(
            v("S-atribuída só usa sintetizados."),
            v("O tipo de uma expressão costuma ser sintetizado."),
            f("L-atribuída não pode ter herdados.", "Pode, vindos do pai ou da esquerda."),
        )),
        Especie("inteiro-esticado", "Bug do Inteiro Esticado", Skill.ATTRIBUTES_TYPES, "int + float dá erro.", "O compilador converte o int para float e segue.", listOf(
            v("int + float resulta em float."),
            v("Coerção é uma conversão automática."),
            f("float cabe em int sem perder nada.", "A parte depois da vírgula se perde."),
        )),
        Especie("escopo-vazado", "Bug do Escopo Vazado", Skill.SCOPE_SYMBOLS, "Variável de dentro do bloco vale fora dele.", "Cada bloco abre um escopo. Ao fechar, os nomes dele somem.", listOf(
            v("A busca começa pelo escopo mais interno."),
            v("Declarar x dentro esconde o x de fora."),
            f("Usar variável não declarada é erro léxico.", "É erro semântico."),
        )),
        Especie("arvore-gorda", "Bug da Árvore Gorda", Skill.AST_BUILD, "AST é a árvore de derivação inteira.", "AST guarda só o que importa. Parênteses e regras de passagem somem.", listOf(
            v("( a + b ) não tem nó de parêntese na AST."),
            v("A forma da AST já mostra a precedência."),
            f("A AST tem mais nós que a árvore de derivação.", "Ela é mais enxuta."),
        )),
        Especie("conta-longa", "Bug da Conta Longa", Skill.TAC, "Uma linha de TAC tem várias contas.", "No máximo uma conta por linha. O resto vai para temporários.", listOf(
            v("a + b * c vira duas linhas."),
            v("t1 = b * c vem antes de t2 = a + t1."),
            f("Temporário é variável que a pessoa declarou.", "Quem cria é o compilador."),
        )),
        Especie("otimizacao-mentirosa", "Bug da Otimização Mentirosa", Skill.OPTIMIZATION, "Otimizar pode mudar o resultado.", "Otimização precisa manter o que o programa faz.", listOf(
            v("2 * 3 pode virar 6 ainda na compilação."),
            v("Código que nunca roda pode sair."),
            f("x / x sempre pode virar 1.", "Com x zero, o programa dava erro."),
        )),
        Especie("recursao-unica", "Bug da Recursão Única", Skill.RUNTIME, "Função recursiva tem um registro só.", "Cada chamada empilha um registro novo.", listOf(
            v("O registro guarda parâmetros, locais e endereço de volta."),
            v("Ao retornar, o registro sai da pilha."),
            f("Variável local sobrevive depois do retorno.", "Ela sai junto com o registro."),
        )),
        Especie("load-esquecido", "Bug do Load Esquecido", Skill.CODEGEN_MIPS, "MIPS soma direto da memória.", "MIPS só opera em registradores. Primeiro lw, depois a conta, depois sw.", listOf(
            v("lw copia da memória para o registrador."),
            v("sw guarda o registrador na memória."),
            f("add soma dois endereços de memória direto.", "add só usa registradores."),
        )),
        Especie("registrador-infinito", "Bug do Registrador Infinito", Skill.REG_ALLOC, "Sempre há registrador para todo mundo.", "São poucos. Quem não cabe vai para a memória: isso é spill.", listOf(
            v("Temporários vivos ao mesmo tempo interferem."),
            v("Colorir o grafo é dar registradores diferentes a vizinhos."),
            f("Temporários que não interferem precisam de registradores diferentes.", "Eles podem dividir o mesmo."),
        )),
    )

    fun of(id: String): Especie? = especies.firstOrNull { it.id == id }
}
