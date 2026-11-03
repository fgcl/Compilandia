package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PRATICA
import com.compilandia.content.Tint
import com.compilandia.content.ToyCorte
import com.compilandia.content.ToyDerivacao
import com.compilandia.content.ToyEmpilhaReduz
import com.compilandia.content.ToyMips
import com.compilandia.content.ToyPilhaLL
import com.compilandia.content.ToyTipos
import com.compilandia.content.ToyTrem
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.Grammar
import com.compilandia.engine.tokensOf
import com.compilandia.learning.Skill
import com.compilandia.tutor.Sims

/** Temporada Na prática: a pessoa pilota, e quem calcula é o motor. */
val praticas by lazy {
    listOf(
        episodio(Skill.PHASES, "A câmera entra na máquina", PRATICA) {
            val nomes = listOf("Léxica", "Sintática", "Semântica", "Intermediário", "Otimização", "Geração")
            val fases = nomes.mapIndexed { i, n -> Box("f$i", n, 180f, 76f + i * 56f, 210f, 46f, Look.TOKEN, Tint.NONE, size = 17f) }
            cena("Esta é a linha de montagem inteira. Vamos entrar na primeira máquina.") {
                add(fases)
                tint("f0", Tint.TEAL)
            }
            cena("Dentro da fase léxica, a linha chega letra por letra.") {
                text("f0", "")
                text("f1", "")
                val letras = "x>=1"
                letras.forEachIndexed { i, c -> add(Box("c$i", c.toString(), 156f + i * 16f, 76f, 14f, 22f, Look.CELL, Tint.DARK, size = 9f)) }
                camera(180f, 76f, 4f)
            }
            pergunta(
                "Dois destes caracteres formam um token só. Toque nos dois.",
                setOf("c1", "c2"), setOf("c0", "c3"),
                "Procure o operador de comparação.",
                "Isso. Maior ou igual é um token só.",
            ) { hot() }
            cena("Os tokens saem daqui e seguem para a próxima máquina.") {
                dropAll("c")
                text("f0", "Léxica")
                text("f1", "Sintática")
                camera()
                hot("f1"); tint("f1", Tint.ORANGE)
            }
            cena("Dentro da sintática, os mesmos tokens viram uma árvore.") {
                text("f0", "")
                text("f1", "")
                text("f2", "")
                add(Box("r", ">=", 180f, 124f, 26f, 15f, Look.NODE, Tint.ORANGE, size = 8f))
                add(Box("a", "x", 160f, 140f, 18f, 13f, Look.NODE, Tint.TEAL, size = 7f))
                add(Box("b", "1", 200f, 140f, 18f, 13f, Look.NODE, Tint.TEAL, size = 7f))
                wire("r", "a", arrow = false); wire("r", "b", arrow = false)
                camera(180f, 132f, 4f)
            }
            cena("Cada máquina recebe o trabalho da anterior e entrega para a próxima.", who = PROF, mood = EpMood.HAPPY) {
                drop("r", "a", "b")
                text("f0", "Léxica")
                text("f1", "Sintática")
                text("f2", "Semântica")
                camera()
                hot()
            }
        },

        episodio(Skill.FIRST, "Símbolos correndo nos fios", PRATICA) {
            val no = { id: String, t: String, y: Float -> Box(id, t, 60f, y, 58f, 58f, Look.STATE, Tint.ORANGE, size = 20f) }
            val conj = { id: String, y: Float -> Box(id, "", 220f, y, 210f, 50f, Look.CELL, Tint.DARK, size = 15f) }
            cena("Três regras em cadeia. E começa com T, e T começa com F.") {
                add(no("F", "F", 90f), no("T", "T", 220f), no("E", "E", 350f))
                add(conj("cF", 90f), conj("cT", 220f), conj("cE", 350f))
                wire("F", "T", bend = 0f); wire("T", "E", bend = 0f)
            }
            cena("F abre com id, ou com abre parêntese. Eles entram primeiro.") { text("cF", "id   ("); hot("F", "cF") }
            cena("T começa com F. Então tudo que abre F corre pelo fio até T.") { flow("F", "T", "id", "("); hot("T") }
            cena("Chegou. Agora os mesmos símbolos seguem de T para E.") { text("cT", "id   ("); flow("T", "E", "id", "("); hot("E") }
            pergunta(
                "Quem chega no FIRST de E? Toque nos dois.",
                setOf("a", "b"), setOf("c"),
                "Só correm pelo fio os símbolos que já estão em T.",
                "Isso. FIRST escorre de quem está no começo da regra.",
            ) {
                clearWires()
                wire("F", "T"); wire("T", "E")
                add(Box("a", "id", 150f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("b", "(", 220f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("c", "+", 290f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
            }
            cena("Quando nada mais corre, acabou. É assim que o algoritmo sabe parar.", who = PROF, mood = EpMood.HAPPY) {
                drop("a", "b", "c")
                text("cE", "id   (")
                hot("cE")
            }
        },

        episodio(Skill.TOKENS, "Corte você mesmo", PRATICA) {
            cena("Agora você é o scanner. A linha é x maior ou igual a dez.") {
                add(titulo("x>=10;"))
                add(fileira("t", listOf("x" to Tint.TEAL, ">=" to Tint.ORANGE, "10" to Tint.YELLOW, ";" to Tint.BLUE), 180f, w = 62f))
            }
            cena("Mais uma letra estica o pedaço. Cortar fecha o token.") { hot("t1") }
            cena("Lembra da regra? Estique enquanto couber.", who = PROF) { hot() }
            brinquedo(
                "Corte a linha inteira. Estique, e corte na hora certa.",
                "O motor recusou. Pense no maior pedaço possível.",
                "Linha cortada. Foram quatro tokens.",
            ) { ToyCorte("x>=10;", "Corte a linha inteira") }
            cena("Scanner de verdade faz isso milhões de vezes por segundo.", mood = EpMood.HAPPY) {
                add(titulo("x>=10;"))
                hot("t0", "t1", "t2", "t3")
            }
        },

        episodio(Skill.REGEX_DFA, "Pilote o trem", PRATICA) {
            cena("Esta regex pede um a, quantos a mais quiser, e um b no fim.") {
                add(titulo("a a* b"))
                add(fileira("p", listOf("a" to Tint.TEAL, "a*" to Tint.ORANGE, "b" to Tint.TEAL), 180f, w = 74f, h = 54f, size = 20f))
            }
            cena("O autômato tem um estado para cada situação. Cada letra é uma seta.") { hot("p1") }
            cena("O laço no meio é a estrela: dá para ficar ali lendo a.", who = PROF) { hot("p1") }
            brinquedo(
                "Agora pilote. Chegue no círculo duplo com exatamente três letras.",
                "Essa letra não deu certo. Olhe as setas do estado aceso.",
                "Chegou no círculo duplo. A frase foi aceita.",
            ) { val d = DFA.fromRegex("aa*b"); ToyTrem(d, Sims.posicoes(d.stateCount), "Chegue no círculo duplo com 3 letras", 3) }
            cena("Pilotar é rodar o autômato de cabeça. É isso que a prova pede.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CFG_DERIVATION, "Derive até chegar", PRATICA) {
            cena("A frase alvo é id mais id. As regras estão aqui.") {
                add(titulo("id + id"))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id"), 110f, x = 120f, passo = 32f, size = 17f))
            }
            cena("Troque sempre o não terminal mais à esquerda.") { hot("g0") }
            cena("O X decide se a frase continua. Mais uma soma, ou épsilon para acabar.", who = PROF) { hot("g1") }
            brinquedo(
                "Sua vez. Escolha as regras até chegar na frase.",
                "Essa escolha saiu da frase. Comece de novo.",
                "Chegou. Isso é uma derivação mais à esquerda.",
            ) { ToyDerivacao(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id"), tokensOf("id + id"), "Chegue em id + id") }
            cena("Quando você escolhe certo sem pensar, a tabela LL(1) já está na sua cabeça.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.LEFT_RECURSION, "Rode a versão consertada", PRATICA) {
            cena("Esta gramática já está consertada. Sem recursão à esquerda, a pilha anda.") {
                add(titulo("n + n"))
                add(codigo("g", listOf("E -> n X", "X -> + n X | ε"), 120f, x = 110f, passo = 34f, size = 18f))
            }
            cena("Topo não terminal: troca pela regra. Topo igual à entrada: casa.") { hot("g0") }
            cena("Repare no X à direita. A recursão foi para o fim, onde não trava.", who = PROF) { hot("g1") }
            brinquedo(
                "Opere a pilha até aceitar.",
                "Olhe o topo. Ele é terminal ou não terminal?",
                "Aceitou. Com a recursão à direita, o parser sempre lê um token antes de voltar.",
            ) { ToyPilhaLL(Grammar.parse("E -> n X\nX -> + n X | ε"), tokensOf("n + n"), "Leve a pilha até o fim") }
            cena("A linguagem é a mesma de antes. Só a forma da regra mudou.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.LL1_PARSING, "Opere a pilha", PRATICA) {
            cena("Pilha, entrada e duas ações. Parece pouco, e é tudo que o parser faz.") {
                add(titulo("id + id"))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id"), 110f, x = 120f, passo = 32f, size = 17f))
            }
            cena("Topo não terminal: consulta a tabela e troca pela regra.") { hot("g0") }
            cena("Topo terminal: precisa bater com a entrada, e os dois saem.") { hot("g2") }
            brinquedo(
                "Opere até a pilha esvaziar.",
                "Olhe o topo. Ele é terminal ou não terminal?",
                "Pilha vazia e entrada no fim. A frase vale.",
            ) { ToyPilhaLL(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id"), tokensOf("id + id"), "Leve a pilha até o fim") }
            cena("Sem recursão, sem volta atrás. Por isso o LL(1) é tão rápido.", who = PROF, mood = EpMood.HAPPY) {}
        },

        episodio(Skill.SHIFT_REDUCE, "Opere a esteira", PRATICA) {
            cena("Agora o sentido é o contrário: da frase até o símbolo inicial.") {
                add(titulo("n + n"))
                add(codigo("g", listOf("E -> E + n | n"), 130f, x = 90f, passo = 32f, size = 18f))
            }
            cena("Empilhar puxa o próximo token. Reduzir troca o topo pelo lado esquerdo da regra.") { hot("g0") }
            cena("Quando sobrar só E, e a entrada acabar, a frase vale.", who = PROF) {}
            brinquedo(
                "Sua vez na esteira. Empilhe e reduza até sobrar E.",
                "Olhe o topo. Ele já forma uma regra inteira?",
                "Sobrou E e a entrada acabou. Aceito.",
            ) { ToyEmpilhaReduz(Grammar.parse("E -> E + n | n"), tokensOf("n + n"), "Reduza até sobrar E") }
            cena("Foi uma derivação mais à direita, de trás para frente.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.YACC_BISON, "Por que o vezes espera", PRATICA) {
            cena("Sem declarar nada, os níveis da gramática já dão a precedência.") {
                add(titulo("id + id * id"))
                add(codigo("g", listOf("E -> E + T | T", "T -> T * F | F", "F -> id"), 100f, x = 110f, passo = 32f, size = 17f))
            }
            cena("Regra mais funda roda antes. T fica abaixo de E, então o vezes vem primeiro.") { hot("g1") }
            cena("Com Bison, o left faz o mesmo sem esses níveis.", who = PROF) {}
            brinquedo(
                "Rode a esteira. Repare quando o vezes faz a máquina esperar.",
                "Antes de reduzir, olhe o que vem na entrada.",
                "Aceito. O vezes reduziu antes do mais.",
            ) { ToyEmpilhaReduz(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> id"), tokensOf("id + id * id"), "Reduza até sobrar E") }
            cena("Esperar é empilhar. Foi por isso que a conta saiu na ordem certa.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.ATTRIBUTES_TYPES, "Suba os tipos", PRATICA) {
            cena("Aqui a é inteiro e x é real. Vamos anotar a árvore.") {
                add(titulo("a + x * 2"))
                add(rotulo("r", "a: int     x: float", 180f, 130f, Tint.YELLOW, 18f))
            }
            cena("Comece pelas folhas. Depois, cada pai olha os filhos.") {}
            cena("Se um dos filhos é real, o resultado sobe como real.", who = PROF) {}
            brinquedo(
                "Diga o tipo de cada nó aceso, de baixo para cima.",
                "Tipo errado. Olhe os filhos desse nó.",
                "Árvore anotada. O tipo subiu até a raiz.",
            ) { ToyTipos("a + x * 2", ExprGenerator.env, "Diga o tipo do nó aceso") }
            cena("Isso que você fez é atributo sintetizado, na ordem de baixo para cima.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CODEGEN_MIPS, "Monte o MIPS", PRATICA) {
            cena("MIPS só faz conta em registrador. Toda soma tem três tempos.") {
                add(titulo("a + b"))
                add(fileira("p", listOf("lw" to Tint.TEAL, "add" to Tint.ORANGE, "sw" to Tint.YELLOW), 180f, w = 84f, h = 54f, size = 18f))
            }
            cena("Lw traz da memória. Add faz a conta. Sw guarda o resultado.") { hot("p0", "p1", "p2") }
            cena("Esqueceu o lw, a conta usa lixo. Esqueceu o sw, o resultado some.", who = PROF) { hot() }
            brinquedo(
                "Monte as instruções na ordem.",
                "Ordem errada. Primeiro carrega, depois calcula, depois guarda.",
                "Programa pronto. Foi assim que o gerador de código fez.",
            ) { ToyMips("a + b", "Escolha a próxima instrução") }
            cena("Cada linha de três endereços vira esse mesmo trio.", mood = EpMood.HAPPY) {}
        },
    )
}
