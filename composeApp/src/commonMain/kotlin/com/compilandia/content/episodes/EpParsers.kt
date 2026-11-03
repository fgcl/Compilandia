package com.compilandia.content.episodes

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill

/** Mostra pilha e entrada de um parser: entrada no alto, pilha na base, em peças que deslizam. */
private fun EpisodeBuilder.parser(stack: List<String>, input: List<String>, total: Int) {
    dropAll("p")
    dropAll("i")
    add(pilha("p", stack, 74f, 300f, w = 84f, h = 32f, tint = Tint.ORANGE))
    val cell = 44f
    val x0 = 180f - (total - 1) * cell / 2
    input.forEachIndexed { k, t ->
        val pos = total - input.size + k
        add(Box("i$pos", t, x0 + pos * cell, 92f, cell - 5f, 38f, Look.TOKEN, Tint.TEAL, size = 15f))
    }
}

val topDown by lazy {
    listOf(
        // ─────────────────────────────── TABELA LL(1) ───────────────────────────────
        episodio(Skill.LL1_TABLE, "A tabela de decisões") {
            val base = listOf(
                listOf("", "id", "+", "(", ")", "$"),
                listOf("E", "", "", "", "", ""),
                listOf("X", "", "", "", "", ""),
                listOf("T", "", "", "", "", ""),
            )
            cena("A tabela LL(1) junta tudo. Linha é não-terminal, coluna é o próximo token.") {
                add(titulo("Tabela preditiva"))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )"), 60f, x = 90f, passo = 26f, size = 15f))
                add(tabela("m", base, 170f, cw = 56f, ch = 42f, size = 12f))
            }
            cena("A regra de E começa com id ou com abre parêntese. Escrevo nessas colunas.") {
                hot("g0", "ml1c1", "ml1c3")
                text("ml1c1", "T X"); text("ml1c3", "T X")
            }
            cena("A regra de X que começa com mais vai na coluna do mais.") {
                hot("g1", "ml2c2")
                text("ml2c2", "+ T X")
            }
            pergunta(
                "X também pode sumir. Essa regra vai nas colunas do FOLLOW de X. Toque nas duas casas.",
                setOf("ml2c4", "ml2c5"), setOf("ml2c1", "ml2c3"),
                "O FOLLOW de X é fecha parêntese e cifrão.",
                "Isso. Épsilon vai nas colunas do FOLLOW.",
            ) { hot("g1") }
            cena("Falta o T: id na coluna do id, parêntese na coluna do parêntese.") {
                text("ml2c4", "ε"); text("ml2c5", "ε")
                text("ml3c1", "id"); text("ml3c3", "( E )")
                hot("ml3c1", "ml3c3")
            }
            cena("Se uma casa receber duas regras, a gramática não é LL(1).", who = PROF) { hot() }
            cena("Casa vazia é erro de sintaxe. Tabela pronta, o parser só consulta.", mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── PREDITIVO ───────────────────────────────
        episodio(Skill.LL1_PARSING, "A pilha decide") {
            val n = 4
            cena("O parser preditivo tem uma pilha e a entrada. Ele só olha o topo e o próximo token.") {
                add(titulo("id + id"))
                add(rotulo("rs", "pilha", 74f, 326f), rotulo("ra", "topo: E, token: id", 240f, 200f, Tint.YELLOW, 17f))
                parser(listOf("$", "E"), listOf("id", "+", "id", "$"), n)
                hot("p1", "i0")
            }
            cena("Topo é não-terminal: olho a tabela. Troco E pelo lado direito, de trás para a frente.") {
                parser(listOf("$", "X", "T"), listOf("id", "+", "id", "$"), n)
                text("ra", "E -> T X")
            }
            cena("Agora T está no topo. Com id na entrada, a tabela manda trocar T por id.") {
                parser(listOf("$", "X", "id"), listOf("id", "+", "id", "$"), n)
                text("ra", "T -> id")
                hot("p2", "i0")
            }
            pergunta(
                "Topo é id e o token é id. O que eu faço? Toque na ação.",
                setOf("casar"), setOf("expandir"),
                "Terminal no topo igual ao token da entrada: é par.",
                "Isso. Casou, os dois saem.",
            ) {
                text("ra", "topo: id, token: id")
                add(Box("casar", "Casar", 190f, 260f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 17f), Box("expandir", "Expandir", 310f, 260f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena("O jogo segue igual: expande não-terminal e casa terminal, até o fim.") {
                drop("casar", "expandir")
                parser(listOf("$", "X"), listOf("+", "id", "$"), n)
                text("ra", "casou id")
                hot()
            }
            cena("Sobrou cifrão na pilha e cifrão na entrada: frase aceita.", mood = EpMood.HAPPY) {
                parser(listOf("$"), listOf("$"), n)
                text("ra", "aceita")
                hot("p0", "i3")
            }
        },

        // ─────────────────────────────── DESCIDA RECURSIVA ───────────────────────────────
        episodio(Skill.RECURSIVE_DESCENT, "Um método por regra") {
            val src = listOf(
                "void E() { T(); X(); }",
                "void X() {",
                "  if (tok == '+') {",
                "    casa('+'); T(); X();",
                "  }",
                "}",
                "void T() {",
                "  if (tok == ID) casa(ID);",
                "  else { casa('('); E(); casa(')'); }",
                "}",
            )
            cena("Dá para escrever o parser à mão. Cada não-terminal vira um método.") {
                add(titulo("Descida recursiva"))
                add(codigo("c", src, 64f, passo = 27f, size = 12f))
            }
            cena("A regra de E pede T e depois X. O método chama os dois, nessa ordem.") { hot("c0") }
            cena("Terminal vira casa. Ele confere o token e anda para o próximo.") { hot("c3") }
            pergunta(
                "Se o token não for mais, o método X não faz nada. Que regra é essa? Toque nela.",
                setOf("r1"), setOf("r0"),
                "Não fazer nada é o mesmo que sumir da frase.",
                "Isso. Não fazer nada é a regra épsilon.",
            ) {
                hot("c2")
                add(Box("r0", "X -> + T X", 110f, 350f, 150f, 44f, Look.TOKEN, Tint.TEAL, size = 15f), Box("r1", "X -> ε", 270f, 350f, 130f, 44f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            cena("Os ifs usam o FIRST para escolher a regra. A pilha agora é a de chamadas do programa.") {
                drop("r0", "r1")
                hot("c7", "c8")
            }
            cena("É esse o parser que o trabalho prático pede.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── RECUPERAÇÃO DE ERROS ───────────────────────────────
        episodio(Skill.ERROR_RECOVERY, "Cair e levantar") {
            val itens = listOf("x", "=", "*", "3", ";", "y", "=", "2", ";")
            cena("Um erro não pode derrubar o compilador. Ele avisa e tenta seguir.") {
                add(titulo("Modo pânico"))
                add(fileira("k", itens.map { it to Tint.TEAL }, 130f, w = 34f, h = 42f, gap = 4f))
            }
            cena("Aqui o parser trava: depois do igual não cabe um vezes.", mood = EpMood.SURPRISED) {
                tint("k2", Tint.RED)
                hot("k2")
                add(rotulo("e", "erro aqui", 110f, 180f, Tint.RED, 17f))
            }
            pergunta(
                "No modo pânico ele descarta tokens até um ponto seguro. Toque onde ele se segura.",
                setOf("k4"), setOf("k3", "k5", "k7"),
                "Procure o token que encerra o comando.",
                "Isso. O ponto e vírgula é o sincronizador.",
            ) { hot() }
            cena("Tudo até ali vai fora, e a análise recomeça no comando seguinte.") {
                drop("k2", "k3", "e")
                tint("k4", Tint.GREEN)
                hot("k5")
            }
            cena("Bons sincronizadores saem do FOLLOW de cada não-terminal.", who = PROF) { hot() }
            cena("Assim uma compilação só aponta vários erros de uma vez.", mood = EpMood.HAPPY) {}
        },
    )
}

val bottomUp by lazy {
    listOf(
        // ─────────────────────────────── SHIFT-REDUCE ───────────────────────────────
        episodio(Skill.SHIFT_REDUCE, "Empilha e reduz") {
            fun esteira(itens: List<Pair<Int, String>>) {
                dropAll("i")
                for ((pos, t) in itens) add(Box("i$pos", t, 40f + pos * 46f, 130f, 40f, 40f, Look.TOKEN, Tint.TEAL, size = 16f))
            }
            cena("Agora a gente monta a árvore de baixo para cima. A entrada chega numa esteira.") {
                add(titulo("E -> E + n | n"))
                esteira(listOf(0 to "n", 1 to "+", 2 to "n", 3 to "$"))
                add(rotulo("rp", "pilha", 300f, 372f))
                actor(Actor(190f, 250f, 0.9f))
            }
            cena("Empilhar é puxar o próximo token para a pilha.") {
                esteira(listOf(1 to "+", 2 to "n", 3 to "$"))
                add(pilha("p", listOf("n"), 300f, 340f, h = 34f))
            }
            cena("No topo tem n, o lado direito de uma regra. Isso é um handle. Eu reduzo: n vira E.") {
                dropAll("p")
                add(pilha("p", listOf("E"), 300f, 340f, h = 34f, tint = Tint.ORANGE))
                hot("p0")
            }
            cena("Empilho mais duas vezes.") {
                esteira(listOf(3 to "$"))
                dropAll("p")
                add(Box("p0", "E", 300f, 340f, 70f, 34f, Look.TOKEN, Tint.ORANGE))
                add(pilha("q", listOf("+", "n"), 300f, 303f, h = 34f))
                hot()
            }
            pergunta(
                "A pilha tem E mais n. Empilho ou reduzo? Toque na resposta.",
                setOf("red"), setOf("emp"),
                "Olhe o topo. E mais n é o lado direito de uma regra?",
                "Isso. Handle no topo, então reduz.",
            ) {
                add(Box("emp", "Empilhar", 90f, 300f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 16f), Box("red", "Reduzir", 90f, 356f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 16f))
                hot("p0", "q0", "q1")
            }
            cena("Sobrou só E, e a entrada acabou. Frase aceita.", mood = EpMood.HAPPY) {
                drop("emp", "red")
                dropAll("q")
                hot("p0")
            }
            cena("Lidas de trás para a frente, as reduções formam uma derivação mais à direita.", who = PROF) { actor(null) }
        },

        // ─────────────────────────────── ITENS LR(0) ───────────────────────────────
        episodio(Skill.LR0_ITEMS, "O ponto que anda") {
            fun item(id: String, t: String, y: Float, c: Tint = Tint.NONE) = Box(id, t, 180f, y, 230f, 44f, Look.TOKEN, c, size = 17f)
            cena("Como o parser lembra onde está? Com um ponto dentro da regra.") {
                add(titulo("Item LR(0)"))
                add(item("a", "E -> • E + n", 120f))
            }
            cena("O que está antes do ponto eu já vi. O que está depois, ainda espero.") {
                text("a", "E -> E • + n")
                hot("a")
            }
            cena("Se o ponto está antes de um E, posso ver qualquer regra de E. Isso é o fechamento.") {
                text("a", "E -> • E + n")
                add(item("b", "E -> • n", 180f, Tint.TEAL))
                hot("b")
            }
            pergunta(
                "Neste estado chega um n. Qual item sobrevive? Toque nele.",
                setOf("b"), setOf("a"),
                "O ponto só anda sobre o símbolo que vem logo depois dele.",
                "Isso. O ponto passa por cima do n.",
            ) { hot() }
            cena("Andar o ponto por um símbolo é o goto. Ele leva a um novo estado.") {
                drop("a")
                text("b", "E -> n •")
                move("b", 180f, 250f)
                add(item("a2", "estado novo", 150f, Tint.YELLOW))
                wire("a2", "b")
            }
            cena("Ponto no fim quer dizer: a regra acabou, dá para reduzir.", mood = EpMood.HAPPY) { hot("b") }
            cena("Cada conjunto de itens é um estado. Juntos formam o autômato LR.", who = PROF) { hot() }
        },

        // ─────────────────────────────── SLR ───────────────────────────────
        episodio(Skill.SLR_TABLE, "Quando reduzir?") {
            cena("O autômato vira uma tabela. Linha é estado, coluna é o próximo símbolo.") {
                add(titulo("Tabela SLR(1)"))
                add(tabela("m", listOf(listOf("", "n", "+", "$"), listOf("0", "s2", "", ""), listOf("1", "", "s3", "ok"), listOf("2", "", "r2", "r2")), 80f, cw = 72f, ch = 44f, size = 14f))
            }
            cena("Letra s manda empilhar. Letra r manda reduzir.") { hot("ml1c1", "ml3c2") }
            cena("Mas reduzir quando? O SLR só reduz se o próximo token estiver no FOLLOW.", mood = EpMood.THINK) {
                add(Box("f", "FOLLOW(E) = { +, $ }", 180f, 300f, 240f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
                hot("f")
            }
            pergunta(
                "O estado 2 terminou a regra de E. Em qual coluna não posso reduzir? Toque nela.",
                setOf("ml3c1"), setOf("ml3c2", "ml3c3"),
                "Procure o símbolo que não está no FOLLOW de E.",
                "Isso. O n não está no FOLLOW, então a casa fica vazia.",
            ) { hot() }
            cena("Se uma casa pede empilhar e reduzir ao mesmo tempo, há um conflito.", mood = EpMood.SURPRISED) {
                text("f", "shift ou reduce?")
                tint("f", Tint.RED)
            }
            cena("Gramática sem conflito na tabela é SLR(1).", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── LALR ───────────────────────────────
        episodio(Skill.LALR, "Mais precisão, mesma tabela") {
            fun cx(id: String, t: String, x: Float, y: Float, c: Tint = Tint.NONE) = Box(id, t, x, y, 160f, 54f, Look.TOKEN, c, size = 14f)
            cena("O FOLLOW é meio grosseiro. Ele vale para a gramática inteira, não para cada estado.", mood = EpMood.THINK) {
                add(titulo("LR(1)"))
                add(cx("a", "E -> n • , +", 100f, 120f, Tint.TEAL), cx("b", "E -> n • , $", 266f, 120f, Tint.TEAL))
            }
            cena("O LR(1) guarda no item qual token pode vir depois. É preciso, mas gera muitos estados.") {
                add(cx("c", "T -> id • , +", 100f, 190f), cx("d", "T -> id • , $", 266f, 190f))
                hot("a", "b")
            }
            pergunta(
                "O miolo é o item sem o token da vírgula. Toque no item com o mesmo miolo do destacado.",
                setOf("b"), setOf("c", "d"),
                "Compare a regra e o ponto. Ignore o que vem depois da vírgula.",
                "Isso. A regra e o ponto são iguais, só muda o token.",
            ) { hot("a") }
            cena("O LALR junta estados de miolo igual e une os tokens.") {
                drop("c", "d", "b")
                text("a", "E -> n • , + $")
                move("a", 180f, 150f)
                tint("a", Tint.GREEN)
                hot("a")
            }
            cena("Fica quase tão forte quanto o LR(1), com tabela do tamanho da SLR.") {
                add(Box("r", "Bison usa LALR(1)", 180f, 240f, 240f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
                hot("r")
            }
            cena("Juntar estados pode criar conflito de redução, mas nunca de empilhar com reduzir.", who = PROF) { hot() }
        },

        // ─────────────────────────────── BISON ───────────────────────────────
        episodio(Skill.YACC_BISON, "A fábrica de parsers") {
            val spec = listOf(
                "%left '+' '-'",
                "%left '*' '/'",
                "%%",
                "expr: expr '+' expr { \$\$ = \$1 + \$3; }",
                "    | expr '*' expr { \$\$ = \$1 * \$3; }",
                "    | NUM ;",
            )
            cena("O Bison lê a gramática e escreve o parser para você.") {
                add(titulo("Arquivo .y"))
                add(codigo("s", spec, 70f, passo = 30f, size = 13f))
            }
            cena("Cada regra tem uma ação. Cifrão um é o valor do primeiro símbolo. Cifrão três, do terceiro.") { hot("s3") }
            cena("Cifrão cifrão é o resultado da regra. Ele sobe pela árvore.") { hot("s4") }
            cena("Essa gramática é ambígua. Quem resolve são as linhas de cima.", mood = EpMood.THINK) { hot("s0", "s1") }
            pergunta(
                "Quanto mais abaixo no arquivo, maior a precedência. Quem ganha? Toque na linha.",
                setOf("s1"), setOf("s0"),
                "Procure a linha mais perto das regras.",
                "Isso. O vezes está mais abaixo, então ele vem antes do mais.",
            ) { hot() }
            cena("O left diz que contas iguais juntam da esquerda para a direita.") {
                add(Box("e1", "1 + 2 * 3 = 7", 180f, 300f, 200f, 42f, Look.TOKEN, Tint.GREEN, size = 16f))
                add(Box("e2", "5 - 2 - 1 = 2", 180f, 350f, 200f, 42f, Look.TOKEN, Tint.TEAL, size = 16f))
                hot("s0")
            }
            cena("Sem nenhuma linha de conflito na saída, sua gramática está pronta.", mood = EpMood.HAPPY) { hot() }
        },
    )
}
