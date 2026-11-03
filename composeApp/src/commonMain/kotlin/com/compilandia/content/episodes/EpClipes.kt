package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Temporada.CLIPE
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.titulo
import com.compilandia.learning.Skill

private fun EpisodeBuilder.grande(t: String, y: Float = 200f, tint: Tint = Tint.YELLOW) = add(Box("big", t, 180f, y, 300f, 74f, Look.TOKEN, tint, size = 24f))

/** Aulas de 30 segundos para o feed. As duas primeiras falas também respondem às perguntas no Laboratório. */
val clipesTodos by lazy {
    listOf(
        episodio(Skill.PHASES, "O que um compilador faz", CLIPE) {
            cena("Um compilador traduz o seu código para outra língua, que a máquina roda.") {
                add(titulo("Compilador"))
                add(Box("a", "x = 1 + 2;", 180f, 130f, 200f, 50f, Look.CELL, size = 17f))
                add(Box("b", "01101000", 180f, 290f, 200f, 50f, Look.CELL, Tint.TEAL, size = 17f))
                wire("a", "b")
            }
            cena("Ele confere tudo antes. Se achar erro, ele não gera nada.") { hot("a") }
        },
        episodio(Skill.PHASES, "Compilador ou interpretador", CLIPE) {
            cena("O compilador traduz tudo antes e entrega um programa pronto.") {
                add(titulo("Tudo de uma vez"))
                add(fileira("c", listOf("linha 1" to Tint.TEAL, "linha 2" to Tint.TEAL, "linha 3" to Tint.TEAL), 200f, w = 96f))
            }
            cena("O interpretador lê e executa na hora, linha por linha.") {
                add(titulo("Uma de cada vez"))
                tint("c1", Tint.NONE); tint("c2", Tint.NONE); hot("c0")
            }
        },
        episodio(Skill.TOKENS, "Token e lexema", CLIPE) {
            cena("Lexema é o texto que está no código, como conta.") {
                add(titulo("Lexema"))
                add(Box("a", "conta", 180f, 200f, 150f, 60f, Look.CELL, Tint.YELLOW, size = 22f))
            }
            cena("Token é a classe dele, como identificador.") {
                add(titulo("Token"))
                add(Box("b", "identificador", 180f, 280f, 200f, 50f, Look.TOKEN, Tint.TEAL, size = 18f))
                wire("a", "b")
            }
        },
        episodio(Skill.TOKENS, "Maior casamento", CLIPE) {
            cena("O scanner fica com o pedaço mais comprido que puder.") {
                add(titulo("Maior casamento"))
                add(fileira("c", listOf("x" to Tint.NONE, ">" to Tint.YELLOW, "=" to Tint.YELLOW, "1" to Tint.NONE), 200f, w = 62f, h = 62f, size = 24f))
                hot("c1")
            }
            cena("Maior ou igual é um token só, e não dois.") { hot("c1", "c2") }
        },
        episodio(Skill.TOKENS, "Palavra-chave ou nome", CLIPE) {
            cena("If casa com a regra de nome e com a palavra reservada.") {
                add(titulo("Empate"))
                add(Box("a", "if", 180f, 140f, 100f, 56f, Look.CELL, Tint.YELLOW, size = 24f))
                add(fileira("r", listOf("nome" to Tint.TEAL, "reservada" to Tint.PINK), 270f, w = 130f, h = 50f))
            }
            cena("Quando o tamanho empata, a reservada ganha.") { hot("r1"); tint("r0", Tint.DARK) }
        },
        episodio(Skill.REGEX_DFA, "O que a estrela faz", CLIPE) {
            cena("A estrela repete o que vem antes, zero ou mais vezes.") {
                add(titulo("a*"))
                add(fileira("c", listOf("a" to Tint.YELLOW), 200f, w = 62f, h = 62f, size = 24f))
            }
            cena("Então a seguido de estrela aceita vazio, um a, dois a, e assim por diante.") {
                add(fileira("c", listOf("a" to Tint.YELLOW, "a" to Tint.YELLOW, "a" to Tint.YELLOW), 200f, w = 62f, h = 62f, size = 24f))
            }
        },
        episodio(Skill.NFA_TO_DFA, "AFN e AFD", CLIPE) {
            val pos = listOf(70f to 200f, 280f to 120f, 280f to 280f)
            cena("No AFD, cada letra leva a um só lugar.") {
                add(titulo("AFD"))
                add(estados(pos))
                wire("q0", "q1", "a")
            }
            cena("No AFN, a mesma letra pode levar a vários lugares.") {
                add(titulo("AFN"))
                wire("q0", "q2", "a", hot = true)
            }
        },
        episodio(Skill.NFA_TO_DFA, "Épsilon-fecho", CLIPE) {
            val pos = listOf(60f to 200f, 180f to 200f, 300f to 200f)
            cena("O fecho junta um estado com tudo que se alcança sem ler nada.") {
                add(titulo("Fecho"))
                add(estados(pos))
                wire("q0", "q1", "ε"); wire("q1", "q2", "ε")
                hot("q0")
            }
            cena("É o passeio de graça pelas setas de épsilon.") { hot("q0", "q1", "q2") }
        },
        episodio(Skill.CFG_DERIVATION, "Gramática em 30 segundos", CLIPE) {
            cena("Uma gramática é uma lista de trocas.") {
                add(titulo("S -> a S b | c"))
                add(fileira("f", listOf("S" to Tint.ORANGE), 200f, w = 56f, h = 56f, size = 22f))
            }
            cena("Comece no símbolo inicial e troque até sobrar só terminal. O que sobrar é uma frase.") {
                add(fileira("f", listOf("a" to Tint.TEAL, "c" to Tint.TEAL, "b" to Tint.TEAL), 200f, w = 56f, h = 56f, size = 22f))
            }
        },
        episodio(Skill.CFG_DERIVATION, "Terminal e não terminal", CLIPE) {
            cena("Terminal é peça final, como id. Ele fica na frase.") {
                add(titulo("Terminal"))
                add(Box("t", "id", 110f, 200f, 110f, 60f, Look.TOKEN, Tint.TEAL, size = 22f))
            }
            cena("Não terminal ainda vai virar outra coisa, como E.") {
                add(titulo("Não terminal"))
                add(Box("n", "E", 250f, 200f, 110f, 60f, Look.TOKEN, Tint.ORANGE, size = 22f))
            }
        },
        episodio(Skill.AMBIGUITY, "O que é ambiguidade", CLIPE) {
            cena("Uma gramática é ambígua quando a mesma frase tem duas árvores.") {
                add(titulo("2 + 3 * 4"))
                grande("(2 + 3) * 4 = 20", 170f, Tint.ORANGE)
            }
            cena("Duas árvores dão dois sentidos, e o compilador precisa de um.") {
                add(Box("b", "2 + (3 * 4) = 14", 180f, 260f, 300f, 74f, Look.TOKEN, Tint.TEAL, size = 24f))
            }
        },
        episodio(Skill.LEFT_RECURSION, "Por que a recursão trava", CLIPE) {
            cena("Na regra E vai para E mais T, o parser troca E por E. Ele não lê nada.") {
                add(titulo("E -> E + T"))
                add(fileira("f", listOf("E" to Tint.RED, "+" to Tint.TEAL, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 22f))
                hot("f0")
            }
            cena("Ele repete isso para sempre. Por isso o LL(1) não aceita.") {
                add(fileira("f", listOf("E" to Tint.RED, "+" to Tint.TEAL, "T" to Tint.ORANGE, "+" to Tint.TEAL, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 22f))
                hot("f0")
            }
        },
        episodio(Skill.LEFT_FACTORING, "Fatorar é adiar a escolha", CLIPE) {
            cena("Quando duas opções começam igual, o parser não sabe qual pegar.") {
                add(titulo("A -> a b | a c"))
                add(fileira("f", listOf("a" to Tint.YELLOW, "?" to Tint.RED), 200f, w = 74f, h = 60f, size = 22f))
            }
            cena("Fatorar lê o começo igual uma vez e decide depois.") {
                add(titulo("A -> a X"))
                add(fileira("f", listOf("a" to Tint.YELLOW, "X" to Tint.ORANGE), 200f, w = 74f, h = 60f, size = 22f))
            }
        },
        episodio(Skill.FIRST, "O que é FIRST", CLIPE) {
            cena("FIRST é o que pode aparecer primeiro ao expandir um símbolo.") {
                add(titulo("FIRST"))
                add(fileira("f", listOf("id" to Tint.YELLOW, "T" to Tint.ORANGE, "+" to Tint.TEAL), 200f, w = 70f, h = 56f, size = 20f))
                hot("f0")
            }
            cena("Com ele, o parser decide qual regra seguir olhando um token.") {}
        },
        episodio(Skill.FOLLOW, "O que é FOLLOW", CLIPE) {
            cena("FOLLOW é o que pode vir logo depois de um símbolo.") {
                add(titulo("FOLLOW"))
                add(fileira("f", listOf("T" to Tint.ORANGE, "+" to Tint.YELLOW, "E" to Tint.ORANGE), 200f, w = 70f, h = 56f, size = 20f))
                hot("f1")
            }
            cena("Ele serve para decidir quando um símbolo pode sumir.") {}
        },
        episodio(Skill.FIRST, "Para que serve o épsilon", CLIPE) {
            cena("Épsilon é a cadeia vazia: uma regra que não escreve nada.") {
                add(titulo("X -> + T X | ε"))
                grande("ε")
            }
            cena("É assim que uma lista consegue terminar.") { text("big", "+ T + T + T  fim") }
        },
        episodio(Skill.LL1_TABLE, "O que quer dizer LL(1)", CLIPE) {
            cena("Lê da esquerda, deriva pela esquerda, e olha um token.") {
                add(titulo("L  L  (1)"))
                add(fileira("f", listOf("esquerda" to Tint.TEAL, "esquerda" to Tint.ORANGE, "um token" to Tint.YELLOW), 200f, w = 104f, h = 56f, size = 15f))
            }
            cena("Se uma casa da tabela tem duas regras, não é LL(1).") {
                dropAll("f")
                grande("duas regras", tint = Tint.RED)
            }
        },
        episodio(Skill.RECURSIVE_DESCENT, "Descida recursiva", CLIPE) {
            cena("Cada não terminal vira uma função.") {
                add(titulo("E -> T X"))
                add(codigo("c", listOf("void E() {", "  T();", "  X();", "}"), 140f, x = 110f, passo = 34f, size = 18f))
            }
            cena("A função olha o token, escolhe a regra e chama as outras.") { hot("c1", "c2") }
        },
        episodio(Skill.ERROR_RECOVERY, "Modo pânico", CLIPE) {
            cena("Achou erro? O parser joga tokens fora até achar um ponto seguro.") {
                add(titulo("Modo pânico"))
                add(fileira("f", listOf("=" to Tint.RED, "=" to Tint.RED, "2" to Tint.DARK, ";" to Tint.YELLOW), 200f, w = 62f, h = 56f, size = 20f))
            }
            cena("O ponto e vírgula é o mais comum. Daí ele continua procurando erros.") { hot("f3") }
        },
        episodio(Skill.SHIFT_REDUCE, "O que é handle", CLIPE) {
            cena("Handle é o pedaço do topo da pilha que é a hora certa de reduzir.") {
                add(titulo("Handle"))
                add(fileira("f", listOf("$" to Tint.NONE, "E" to Tint.ORANGE, "+" to Tint.YELLOW, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 20f))
                hot("f1", "f2", "f3")
            }
            cena("Aqui, E mais T vira E.") {
                drop("f2", "f3")
                hot("f1")
            }
        },
        episodio(Skill.SHIFT_REDUCE, "Empilhar ou reduzir", CLIPE) {
            cena("Empilhar puxa o próximo token para a pilha.") {
                add(titulo("Empilhar"))
                add(pilha("p", listOf("E", "+"), 180f, 300f, w = 110f, h = 46f))
                add(Box("n", "id", 300f, 160f, 76f, 46f, Look.TOKEN, Tint.YELLOW, size = 17f))
            }
            cena("Reduzir troca o topo pelo lado esquerdo de uma regra.") {
                add(titulo("Reduzir"))
                move("n", 180f, 202f); text("n", "T")
            }
        },
        episodio(Skill.LR0_ITEMS, "O ponto do item", CLIPE) {
            cena("Um item é uma regra com um ponto.") {
                add(titulo("Item"))
                grande("E -> E . + T")
            }
            cena("O ponto mostra até onde a regra já foi lida.") { text("big", "E -> E + . T") }
        },
        episodio(Skill.SLR_TABLE, "Conflito shift-reduce", CLIPE) {
            cena("No mesmo estado, com o mesmo token, a tabela manda empilhar e reduzir.") {
                add(titulo("Duas ordens"))
                add(fileira("f", listOf("empilhar" to Tint.TEAL, "reduzir" to Tint.ORANGE), 200f, w = 130f, h = 60f, size = 17f))
            }
            cena("O Bison escolhe empilhar, e avisa.") { hot("f0"); tint("f1", Tint.DARK) }
        },
        episodio(Skill.LALR, "O que o LALR faz", CLIPE) {
            cena("O LR(1) sabe muito, mas tem estados demais.") {
                add(titulo("LR(1)"))
                add(fileira("f", listOf("1" to Tint.TEAL, "2" to Tint.TEAL, "3" to Tint.TEAL, "4" to Tint.TEAL), 200f, w = 60f, h = 56f))
            }
            cena("O LALR junta os estados parecidos e guarda o que pode vir depois.") {
                add(titulo("LALR"))
                move("f1", 150f, 200f); move("f0", 150f, 200f); move("f3", 212f, 200f); move("f2", 212f, 200f)
            }
        },
        episodio(Skill.YACC_BISON, "Precedência no Bison", CLIPE) {
            cena("O left diz quem liga mais forte e para qual lado agrupa.") {
                add(titulo("%left"))
                add(codigo("c", listOf("%left '+'", "%left '*'"), 170f, x = 120f, passo = 44f, size = 20f))
            }
            cena("Quanto mais embaixo no arquivo, mais forte.") { hot("c1") }
        },
        episodio(Skill.ATTRIBUTES_TYPES, "Atributo sintetizado e herdado", CLIPE) {
            cena("Sintetizado sobe dos filhos para o pai.") {
                add(titulo("Sobe"))
                add(Box("p", "pai", 180f, 120f, 110f, 50f, Look.NODE, Tint.YELLOW, size = 17f))
                add(Box("f", "filho", 180f, 280f, 110f, 50f, Look.NODE, Tint.TEAL, size = 17f))
                wire("f", "p", hot = true)
            }
            cena("Herdado desce do pai ou vem do irmão.") {
                add(titulo("Desce"))
                clearWires(); wire("p", "f", hot = true)
            }
        },
        episodio(Skill.ATTRIBUTES_TYPES, "Coerção de tipo", CLIPE) {
            cena("Inteiro mais real: o compilador converte o inteiro sozinho.") {
                add(titulo("2 + 1.5"))
                add(fileira("f", listOf("int" to Tint.TEAL, "+" to Tint.NONE, "float" to Tint.ORANGE), 200f, w = 84f, h = 56f, size = 17f))
            }
            cena("Isso é coerção. O resultado é real.") { text("f0", "float"); tint("f0", Tint.ORANGE) }
        },
        episodio(Skill.SCOPE_SYMBOLS, "Tabela de símbolos", CLIPE) {
            cena("A tabela guarda cada nome, com o tipo e o escopo.") {
                add(titulo("Tabela de símbolos"))
                add(fileira("a", listOf("x" to Tint.YELLOW, "int" to Tint.TEAL, "global" to Tint.ORANGE), 170f, w = 96f, h = 50f))
                add(fileira("b", listOf("y" to Tint.YELLOW, "float" to Tint.TEAL, "local" to Tint.ORANGE), 230f, w = 96f, h = 50f))
            }
            cena("Todo uso de variável passa por ela.") { hot("a0") }
        },
        episodio(Skill.AST_BUILD, "AST ou árvore de derivação", CLIPE) {
            cena("A árvore de derivação guarda cada regra usada.") {
                add(titulo("(a + b)"))
                grande("( E )   E + T   T   F", tint = Tint.ORANGE)
            }
            cena("A AST guarda só o que importa para o significado.") { text("big", "a + b"); tint("big", Tint.TEAL) }
        },
        episodio(Skill.TAC, "Três endereços", CLIPE) {
            cena("Cada linha tem no máximo uma conta.") {
                add(titulo("a + b * c"))
                add(codigo("c", listOf("t1 = b * c"), 180f, x = 96f, passo = 44f, size = 20f))
            }
            cena("Conta maior quebra em temporários: t1, t2, t3.") {
                add(codigo("c", listOf("t1 = b * c", "t2 = a + t1"), 180f, x = 96f, passo = 44f, size = 20f))
                hot("c1")
            }
        },
        episodio(Skill.OPTIMIZATION, "Dobra de constantes", CLIPE) {
            cena("Dois vezes três não precisa esperar o programa rodar.") {
                add(titulo("x = 2 * 3"))
                grande("2 * 3")
            }
            cena("O compilador faz a conta e escreve seis.") { text("big", "6"); tint("big", Tint.GREEN) }
        },
        episodio(Skill.RUNTIME, "Registro de ativação", CLIPE) {
            cena("Cada chamada de função ganha um bloco na pilha.") {
                add(titulo("Pilha de chamadas"))
                add(pilha("p", listOf("main", "f"), 180f, 300f, w = 170f, h = 50f))
            }
            cena("Nele ficam os parâmetros, as variáveis locais e o endereço de volta.") { hot("p1") }
        },
        episodio(Skill.CODEGEN_MIPS, "Registrador e memória", CLIPE) {
            cena("Registrador é rápido, mas são poucos.") {
                add(titulo("t0  t1  t2"))
                add(fileira("r", listOf("t0" to Tint.YELLOW, "t1" to Tint.YELLOW, "t2" to Tint.YELLOW), 160f, w = 84f, h = 50f))
            }
            cena("Em MIPS, lw traz da memória, e sw guarda de volta.") {
                add(codigo("c", listOf("lw  \$t0, x", "sw  \$t0, y"), 240f, x = 90f, passo = 40f, size = 18f))
            }
        },
        episodio(Skill.REG_ALLOC, "O que é spill", CLIPE) {
            cena("Faltou registrador? Um valor vai para a memória.") {
                add(titulo("Spill"))
                add(fileira("r", listOf("t1" to Tint.TEAL, "t2" to Tint.TEAL, "t3" to Tint.RED), 170f, w = 84f, h = 56f))
            }
            cena("Isso é spill. Funciona, mas fica mais lento.") { move("r2", 180f, 300f); text("r2", "memória"); hot("r2") }
        },
    )
}
