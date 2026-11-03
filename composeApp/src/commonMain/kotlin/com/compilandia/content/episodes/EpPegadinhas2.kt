package com.compilandia.content.episodes

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.alvo
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill

/** Pegadinhas de prova: do FIRST ao Bison. */
val pegadinhasMeio by lazy {
    listOf(
        episodio(Skill.FIRST, "FIRST de uma regra inteira", PEGADINHA) {
            cena("S começa com A. A pode sumir, e B também.") {
                add(titulo("FIRST de S"))
                add(codigo("g", listOf("S -> A B c", "A -> a | ε", "B -> b | ε"), 70f, x = 116f, passo = 28f, size = 16f))
            }
            cena("Se A não sumir, o a abre a frase. Primeiro candidato.") {
                add(fileira("f", listOf("a" to Tint.TEAL, "b" to Tint.TEAL, "c" to Tint.TEAL, "ε" to Tint.PINK), 240f, w = 64f, h = 54f, size = 20f))
                hot("g1")
            }
            pergunta(
                "Toque em todos os símbolos que entram no FIRST de S.",
                setOf("f0", "f1", "f2"), setOf("f3"),
                "A e B podem sumir, mas o c fica. Então a frase nunca é vazia.",
                "Isso: a, b e c. O épsilon fica de fora, porque o c nunca some.",
            ) { hot() }
            cena("O erro comum é parar no primeiro símbolo. Quem pode sumir manda olhar o próximo.", who = PROF) { drop("f3") }
            cena("Só entra épsilon se tudo à direita puder sumir.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.FOLLOW, "O épsilon que muda o FOLLOW", PEGADINHA) {
            cena("B pode sumir. Isso muda quem vem depois de A.") {
                add(titulo("FOLLOW de B"))
                add(codigo("g", listOf("S -> A b", "A -> a B", "B -> d | ε"), 70f, x = 120f, passo = 28f, size = 16f))
            }
            cena("B está no fim da regra de A. Depois de B vem o que vem depois de A.") {
                hot("g1")
                add(Box("p", "FOLLOW de A entra no FOLLOW de B", 180f, 180f, 330f, 40f, Look.CELL, Tint.YELLOW, size = 15f))
            }
            cena("E depois de A vem b, olhando a primeira regra.") { hot("g0") }
            pergunta(
                "Então, o que entra no FOLLOW de B? Toque.",
                setOf("t0"), setOf("t1", "t2"),
                "Não olhe para dentro de B. Olhe o que vem depois dele.",
                "Isso. O b. E nada de épsilon: FOLLOW nunca tem épsilon.",
            ) {
                hot()
                add(fileira("t", listOf("b" to Tint.TEAL, "d" to Tint.TEAL, "ε" to Tint.PINK), 260f, w = 70f, h = 56f, size = 20f))
            }
            palavra("FOLLOW", "FIRST olha para dentro. FOLLOW olha para fora.", who = PROF, mood = EpMood.HAPPY)
        },

        episodio(Skill.LL1_TABLE, "Duas regras, uma casa só", PEGADINHA) {
            val base = listOf(listOf("", "a", "b", "$"), listOf("S", "", "", ""), listOf("A", "", "", ""))
            cena("Esta gramática parece inocente. Vamos montar a tabela.") {
                add(titulo("S -> A b   A -> b | ε", size = 20f))
                add(tabela("m", base, 130f, cw = 80f, ch = 56f, size = 15f))
            }
            cena("A pode virar b. Essa regra vai na coluna do b.") { text("ml2c2", "b"); hot("ml2c2") }
            cena("A também pode sumir. Vai nas colunas do FOLLOW de A, e depois de A vem b.") { hot("ml2c2") }
            pergunta(
                "Toque na casa que recebeu duas regras.",
                setOf("ml2c2"), setOf("ml1c2", "ml2c1", "ml2c3"),
                "O FIRST da primeira opção e o FOLLOW de A têm o mesmo símbolo.",
                "Isso. Com b na entrada, o parser não sabe qual escolher.",
            ) { hot() }
            cena("Duas regras na casa: a gramática não é LL(1). E ela nem tem recursão à esquerda.", who = PROF) {
                text("ml2c2", "b / ε"); tint("ml2c2", Tint.RED); hot("ml2c2")
            }
            cena("Na prova, confira sempre: FIRST da opção não pode bater com o FOLLOW de quem some.", mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.RECURSIVE_DESCENT, "Quem avança o token", PEGADINHA) {
            cena("Na descida recursiva, cada regra vira uma função. Olhe a do F.") {
                add(titulo("void F()"))
                add(codigo("c", listOf("if (tk == ID) casa(ID);", "else if (tk == ABRE) {", "  casa(ABRE);", "  E();", "  casa(FECHA);", "} else erro();"), 74f, x = 34f, passo = 30f, size = 14f))
            }
            cena("A função só olha o token atual para decidir. Olhar não gasta nada.") { hot("c0", "c1") }
            pergunta(
                "Em qual linha a entrada anda de verdade? Toque na primeira.",
                setOf("c0"), setOf("c1", "c5"),
                "Procure a função que confere o token e passa para o próximo.",
                "Isso. Quem avança é o casa.",
            ) { hot() }
            cena("Chamar E não avança nada por conta própria. Quem avança lá dentro é outro casa.") { hot("c3") }
            cena("Se ninguém chamar casa, o programa fica parado no mesmo token, para sempre.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.ERROR_RECOVERY, "Parar ou seguir", PEGADINHA) {
            cena("Um compilador que para no primeiro erro faz você recompilar dez vezes.") {
                add(titulo("Dez erros, dez compilações"))
                add(fileira("t", listOf("id" to Tint.TEAL, "=" to Tint.ORANGE, "=" to Tint.RED, "num" to Tint.TEAL, ";" to Tint.BLUE, "id" to Tint.TEAL), 150f, w = 50f))
            }
            cena("No modo pânico, ele joga tokens fora até achar um bom ponto de recomeço.") { hot("t2", "t3") }
            pergunta(
                "Qual token é um bom ponto de recomeço nesta linha? Toque nele.",
                setOf("t4"), setOf("t0", "t3"),
                "Procure o token que costuma fechar um comando.",
                "Isso. O ponto e vírgula marca o fim do comando.",
            ) { hot() }
            cena("Recuperar não conserta o programa. Só deixa o compilador achar os próximos erros.", who = PROF) {
                dropAll("t")
                add(fileira("t", listOf(";" to Tint.BLUE, "id" to Tint.TEAL), 150f, w = 60f))
            }
            cena("Depois do erro, o compilador não gera o programa. Ele só lista tudo de uma vez.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.SHIFT_REDUCE, "Reduzir cedo demais", PEGADINHA) {
            cena("Na pilha temos E mais T, e vem um vezes. O topo já forma uma regra.") {
                add(titulo("E + T   vem *", size = 24f))
                add(pilha("p", listOf("E", "+", "T"), 240f, 300f, w = 92f, h = 40f))
                add(codigo("g", listOf("E -> E + T | T", "T -> T * F | F", "F -> id"), 80f, x = 40f, passo = 26f, size = 13f))
                add(Box("in", "*", 90f, 240f, 56f, 50f, Look.TOKEN, Tint.YELLOW, size = 20f))
            }
            pergunta(
                "A máquina reduz E mais T, ou empilha o vezes? Toque no desenho.",
                setOf("sobe"), setOf("red"),
                "Se reduzir agora, a soma roda antes do vezes.",
                "Isso. Empilha, porque o vezes liga mais forte.",
            ) {
                add(alvo("sobe", 240f, 174f, 92f, 40f, "*"))
                add(Box("red", "reduzir", 90f, 320f, 110f, 44f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            cena("Quem decide é o que vem na entrada. Ver um lado direito no topo não basta.") {
                drop("sobe", "red", "in")
                add(pilha("p", listOf("E", "+", "T", "*"), 240f, 300f, w = 92f, h = 40f))
            }
            palavra("HANDLE", "Handle é o pedaço certo de reduzir agora. Nem todo lado direito é handle.", who = PROF, mood = EpMood.HAPPY)
            cena("Reduzir antes da hora faz a conta na ordem errada.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.LR0_ITEMS, "O fecho que puxa regras", PEGADINHA) {
            cena("O ponto marca até onde a regra foi lida. Aqui, nada ainda.") {
                add(titulo("Fecho"))
                add(Box("i0", "E -> . E + n", 180f, 110f, 200f, 40f, Look.CELL, Tint.YELLOW, size = 16f))
            }
            cena("O ponto está antes de E. Então posso estar prestes a ler um E inteiro.") { hot("i0") }
            pergunta(
                "Quem entra no fecho? Toque nos itens certos.",
                setOf("a", "b"), setOf("c"),
                "Entram as regras de E, todas com o ponto no começo.",
                "Isso. O fecho puxa as regras, com o ponto no começo.",
            ) {
                hot()
                add(Box("a", "E -> . E + n", 180f, 180f, 200f, 40f, Look.CELL, size = 16f))
                add(Box("b", "E -> . n", 180f, 230f, 200f, 40f, Look.CELL, size = 16f))
                add(Box("c", "E -> n .", 180f, 280f, 200f, 40f, Look.CELL, size = 16f))
            }
            cena("Item com o ponto no fim é outra coisa: é o momento de reduzir.", who = PROF) { drop("a"); tint("c", Tint.ORANGE); hot("c") }
            palavra("CLOSURE", "Ponto antes de não terminal, puxa. Ponto no fim, reduz.", mood = EpMood.HAPPY)
        },

        episodio(Skill.SLR_TABLE, "Conflito não é ambiguidade", PEGADINHA) {
            cena("Esta gramática de ponteiros não é ambígua. Mesmo assim, o SLR tropeça.") {
                add(titulo("Conflito sem ambiguidade", size = 22f))
                add(codigo("g", listOf("S -> L = R | R", "L -> * R | id", "R -> L"), 70f, x = 110f, passo = 28f, size = 16f))
            }
            cena("Depois de ler L, esse estado tem duas opções. Seguir com o igual, ou reduzir para R.") {
                add(Box("a", "S -> L . = R", 180f, 190f, 210f, 40f, Look.CELL, Tint.TEAL, size = 15f))
                add(Box("b", "R -> L .", 180f, 240f, 210f, 40f, Look.CELL, Tint.ORANGE, size = 15f))
            }
            pergunta(
                "O igual está no FOLLOW de R. Com igual na entrada, que conflito aparece? Toque.",
                setOf("sr"), setOf("rr"),
                "Uma ideia empilha o igual. A outra reduz.",
                "Isso. Empilhar ou reduzir: conflito shift reduce.",
            ) {
                add(Box("sr", "shift reduce", 106f, 310f, 148f, 50f, Look.TOKEN, Tint.RED, size = 15f))
                add(Box("rr", "reduce reduce", 262f, 310f, 148f, 50f, Look.TOKEN, Tint.NONE, size = 15f))
            }
            cena("O SLR olha só o FOLLOW, que é um conjunto grosso. O LALR olha mais fino e resolve.", who = PROF) { drop("sr", "rr") }
            palavra("CONFLITO", "Conflito diz que a técnica não deu conta. Não prova que a gramática é ambígua.", mood = EpMood.HAPPY)
        },

        episodio(Skill.LALR, "Juntar estados tem preço", PEGADINHA) {
            cena("O LR(1) tem muitos estados. O LALR junta os que têm o mesmo miolo.") {
                add(titulo("Juntar estados"))
                add(Box("a", "A -> c .   com d", 100f, 150f, 150f, 46f, Look.CELL, Tint.TEAL, size = 13f))
                add(Box("b", "B -> c .   com e", 100f, 206f, 150f, 46f, Look.CELL, Tint.TEAL, size = 13f))
                add(Box("c", "A -> c .   com e", 260f, 150f, 150f, 46f, Look.CELL, Tint.ORANGE, size = 13f))
                add(Box("d", "B -> c .   com d", 260f, 206f, 150f, 46f, Look.CELL, Tint.ORANGE, size = 13f))
            }
            cena("Separados, cada estado sabe o que fazer. Com d reduz um, com e reduz outro.") { hot("a", "b") }
            cena("O miolo é igual, então o LALR junta tudo num estado só.") {
                move("c", 180f, 150f); move("a", 180f, 150f)
                move("d", 180f, 206f); move("b", 180f, 206f)
            }
            pergunta(
                "Depois de juntar, com d na entrada, quem pode reduzir? Toque nas duas regras.",
                setOf("x", "y"), setOf("z"),
                "Junte as listas: as duas regras ficaram com d e com e.",
                "Isso. Duas reduções com o mesmo token: conflito reduce reduce.",
            ) {
                drop("a", "b", "c", "d")
                add(Box("x", "A -> c .   com d, e", 180f, 150f, 230f, 46f, Look.CELL, Tint.RED, size = 14f))
                add(Box("y", "B -> c .   com d, e", 180f, 206f, 230f, 46f, Look.CELL, Tint.RED, size = 14f))
                add(Box("z", "S -> a . A d", 180f, 262f, 230f, 46f, Look.CELL, size = 14f))
            }
            cena("Essa é a única briga que o LALR pode criar. Shift reduce novo ele não cria.", who = PROF, mood = EpMood.HAPPY) { drop("z") }
        },

        episodio(Skill.YACC_BISON, "Quem fica embaixo manda", PEGADINHA) {
            cena("Esta especificação tem dois left. A ordem deles decide a conta.") {
                add(titulo("2 + 3 * 4"))
                add(codigo("c", listOf("%left '*'", "%left '+'"), 90f, x = 120f, passo = 32f, size = 18f))
            }
            cena("Quanto mais embaixo no arquivo, mais forte. Aqui o mais ficou embaixo.") { hot("c1") }
            pergunta(
                "Com essa ordem, quanto dá dois mais três vezes quatro? Toque.",
                setOf("v20"), setOf("v14"),
                "Quem liga mais forte roda primeiro. Quem ficou embaixo?",
                "Isso: vinte. O mais rodou primeiro, e a conta saiu errada.",
            ) {
                hot()
                add(Box("v14", "14", 110f, 230f, 100f, 60f, Look.TOKEN, Tint.TEAL, size = 22f))
                add(Box("v20", "20", 250f, 230f, 100f, 60f, Look.TOKEN, Tint.ORANGE, size = 22f))
            }
            cena("Troque as linhas: mais em cima, vezes embaixo. Agora dá catorze.") {
                drop("v20", "v14")
                text("c0", "%left '+'"); text("c1", "%left '*'")
                add(Box("v", "14", 180f, 230f, 100f, 60f, Look.TOKEN, Tint.GREEN, size = 22f))
                hot("c1")
            }
            palavra("PRECEDÊNCIA", "A pegadinha é achar que o primeiro manda. Quem manda é o último.", who = PROF, mood = EpMood.HAPPY)
        },
    )
}

@Suppress("unused") private val keepActor = Actor(0f, 0f)
