package com.compilandia.content.episodes

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.TokenClass
import com.compilandia.learning.Skill

private fun tintOf(c: TokenClass) = when (c) {
    TokenClass.ID -> Tint.TEAL
    TokenClass.NUM -> Tint.YELLOW
    TokenClass.OP -> Tint.ORANGE
    TokenClass.KEYWORD -> Tint.PINK
    TokenClass.DELIM -> Tint.BLUE
    TokenClass.STRING -> Tint.GREEN
}

val lexicos by lazy {
    listOf(
        // ─────────────────────────────── FASES ───────────────────────────────
        episodio(Skill.PHASES, "A linha de montagem") {
            val nomes = listOf("Léxica", "Sintática", "Semântica", "Intermediário", "Otimização", "Geração")
            val fases = nomes.mapIndexed { i, n -> Box("f$i", n, 90f, 86f + i * 50f, 150f, 42f, Look.TOKEN, Tint.NONE, size = 15f) }
            cena("Um compilador é uma linha de montagem. Cada fase faz uma coisa só.") {
                add(titulo("Seis fases"))
                add(fases)
            }
            cena("Entra o seu código. Para a máquina, ainda é só uma fila de letras.") {
                add(Box("p", "x = a + 2", 270f, 86f, 150f, 42f, Look.CELL, size = 14f))
            }
            cena("A fase léxica corta as letras em tokens.") {
                hot("f0"); tint("f0", Tint.TEAL)
                text("p", "x  =  a  +  2")
                wire("f0", "p")
            }
            cena("A sintática monta uma árvore com esses tokens.") {
                hot("f1"); tint("f1", Tint.TEAL)
                move("p", 270f, 136f)
                clearWires(); wire("f1", "p")
                text("p", "árvore")
            }
            cena("A semântica confere o sentido, como os tipos de cada variável.") {
                hot("f2"); tint("f2", Tint.TEAL)
                move("p", 270f, 186f)
                clearWires(); wire("f2", "p")
                text("p", "árvore com tipos")
            }
            pergunta(
                "Em int x igual a oi, entre aspas, o texto não cabe num inteiro. Qual fase reclama? Toque nela.",
                setOf("f2"), setOf("f0", "f1", "f3", "f4", "f5"),
                "A escrita está certa e a frase tem forma de comando. O problema é o sentido.",
                "Isso. Tipo errado é assunto da semântica.",
            ) {
                drop("p")
                add(Box("q", "int x = \"oi\";", 270f, 120f, 150f, 42f, Look.CELL, size = 14f))
                hot()
            }
            cena("Depois vem o código intermediário, a otimização e o código de máquina.") {
                drop("q")
                for (i in 3..5) tint("f$i", Tint.TEAL)
                hot("f3", "f4", "f5")
                add(Box("p", "lw  add  sw", 270f, 336f, 150f, 42f, Look.CELL, size = 14f))
            }
            cena("Decore a ordem. O mapa do jogo segue essas mesmas fases.", mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── TOKENS ───────────────────────────────
        episodio(Skill.TOKENS, "Cortar a linha") {
            val src = "conta = 10 + x1;"
            val toks = MiniLexer.tokenize(src)
            cena("Para o computador, esta linha é só uma fila de letras.") {
                add(titulo("Scanner"))
                add(fita(src, 92f))
            }
            cena("O scanner anda por ela e junta as letras em palavras. Cada palavra é um token.") {
                add(fileira("tk", toks.map { it.lexeme to tintOf(it.cls) }, 190f, w = 50f, h = 36f, size = 14f))
                add(rotulo("e", "entram letras", 90f, 136f), rotulo("s", "saem tokens", 90f, 238f, Tint.YELLOW))
            }
            cena("Cada token cai na caixa da classe dele: nome, número ou operador.") {
                drop("e", "s")
                add(
                    Box("bID", "ID", 70f, 310f, 110f, 120f, Look.BIN),
                    Box("bNUM", "NUM", 180f, 310f, 100f, 120f, Look.BIN),
                    Box("bOP", "OP", 290f, 310f, 110f, 120f, Look.BIN),
                )
                move("tk0", 70f, 262f); move("tk4", 70f, 306f)
                move("tk2", 180f, 262f)
                move("tk1", 262f, 262f); move("tk3", 318f, 262f); move("tk5", 290f, 306f)
                actor(Actor(180f, 200f, 0.8f))
            }
            cena("Os espaços caem fora aqui. Nenhuma fase depois vê espaço.") {
                dropAll("c")
                add(fita("conta=10+x1;", 92f))
                actor(null)
            }
            pergunta(
                "Sua vez. Toque nos dois cortes desta linha.",
                setOf("g1", "g3"), setOf("g2", "g4"),
                "O maior ou igual fica inteiro, e o número também.",
                "Isso. Três tokens: x, maior ou igual, e dez.",
            ) {
                clear()
                add(titulo("Onde corta?"))
                add(fita("x>=10", 150f, cell = 52f))
                for (i in 1..4) add(Box("g$i", "", 76f + (i - 0.5f) * 52f, 150f, 10f, 92f, Look.GAP))
            }
            cena("Essa é a regra do maior casamento. Por isso conta2 é um nome só.") {
                clear()
                add(titulo("Maior casamento"))
                add(fileira("a", listOf(">" to Tint.NONE, "=" to Tint.NONE), 110f, w = 44f))
                add(fileira("b", listOf(">=" to Tint.ORANGE), 180f, w = 100f))
                add(fileira("d", listOf("conta2" to Tint.TEAL), 250f, w = 130f))
            }
            cena("O texto exato é o lexema. A classe dele é o token.") {
                clear()
                add(titulo("Lexema e token"))
                add(Box("l", "x1", 110f, 150f, 110f, 56f, Look.CELL, size = 20f))
                add(Box("t", "ID", 260f, 150f, 110f, 56f, Look.TOKEN, Tint.TEAL, size = 20f))
                wire("l", "t")
                add(rotulo("ll", "lexema", 110f, 200f), rotulo("tl", "token", 260f, 200f, Tint.TEAL))
            }
            cena("Entram letras, saem tokens. Agora quem corta é você.", mood = EpMood.HAPPY) { hot("t") }
        },

        // ─────────────────────────────── REGEX E AFD ───────────────────────────────
        episodio(Skill.REGEX_DFA, "O trem do autômato") {
            val pos = listOf(80f to 200f, 280f to 200f)
            cena("Uma expressão regular descreve um formato. Esta diz: um a, depois quantos b quiser.") {
                add(titulo("a b*"))
            }
            cena("O autômato é a máquina que confere esse formato. Cada círculo é um estado.") {
                add(estados(pos, finais = setOf(1)))
                wire("q0", "q1", "a")
                wire("q1", "q1", "b")
            }
            cena("O trem começa no primeiro estado e lê uma letra por vez.") {
                add(fita("abb", 320f, cell = 40f))
                actor(Actor(80f, 182f, 0.72f))
                hot("c0")
            }
            cena("Leu a. O trem anda para o outro estado.") {
                actor(Actor(280f, 182f, 0.72f))
                hot("c1")
                hotWire("q0", "q1")
            }
            cena("Leu b. Dá a volta no laço e fica no mesmo estado.") {
                hot("c2")
                hotWire("q1", "q1")
            }
            cena("Acabou a cadeia num círculo duplo. Cadeia aceita.", mood = EpMood.HAPPY) {
                hot("q1")
            }
            pergunta(
                "E a cadeia b a? O trem sai do primeiro estado lendo b. Toque na resposta.",
                setOf("nao"), setOf("sim"),
                "Olhe as setas que saem do primeiro estado. Alguma tem b?",
                "Certo. Sem seta para b, a máquina rejeita.",
            ) {
                dropAll("c")
                actor(Actor(80f, 182f, 0.72f))
                add(fita("ba", 320f, cell = 40f))
                add(Box("sim", "Aceita", 110f, 90f, 120f, 50f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("nao", "Rejeita", 250f, 90f, 120f, 50f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena("É assim que o scanner reconhece nomes e números.", mood = EpMood.HAPPY) {
                drop("sim", "nao")
                actor(null)
            }
        },

        // ─────────────────────────────── AFN → AFD ───────────────────────────────
        episodio(Skill.NFA_TO_DFA, "De muitos caminhos para um") {
            val pos = listOf(70f to 130f, 180f to 130f, 290f to 130f)
            cena("Num AFN, a mesma letra pode levar a dois lugares. Lendo a, o estado zero tem duas saídas.") {
                add(titulo("AFN: termina em ab"))
                add(estados(pos, finais = setOf(2), nomes = listOf("0", "1", "2")))
                wire("q0", "q0", "a b")
                wire("q0", "q1", "a", hot = true)
                wire("q1", "q2", "b")
            }
            cena("Computador não adivinha. Então a gente anda com um conjunto de estados.", mood = EpMood.THINK) {
                add(Box("s0", "{0}", 70f, 240f, 86f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            cena("Do conjunto zero, lendo a, posso estar em zero ou um. Nasce o conjunto zero e um.") {
                add(Box("s1", "{0,1}", 180f, 240f, 86f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
                wire("s0", "s1", "a")
                hot("s1")
            }
            pergunta(
                "Do conjunto zero e um, lendo b, para onde vamos? Toque no conjunto certo.",
                setOf("o2"), setOf("o1", "o3"),
                "Veja cada estado do conjunto. Com b, o zero fica no zero e o um vai para o dois.",
                "Isso. Juntando os dois caminhos, dá zero e dois.",
            ) {
                add(Box("o1", "{0}", 80f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
                add(Box("o2", "{0,2}", 180f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
                add(Box("o3", "{2}", 280f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
            }
            cena("Cada conjunto novo vira um estado do AFD. Se ele contém um final, é final.") {
                drop("o1", "o2", "o3")
                add(Box("s2", "{0,2}", 290f, 240f, 86f, 46f, Look.ACCEPT, Tint.GREEN, size = 13f))
                wire("s1", "s2", "b")
                hot("s2")
            }
            cena("Quando o AFN tem seta épsilon, a gente passa por ela de graça. Isso é o épsilon fecho.", who = PROF) { hot() }
            cena("Pronto: nenhum chute, um caminho só para cada letra.", mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── MINIMIZAÇÃO ───────────────────────────────
        episodio(Skill.DFA_MINIMIZATION, "Juntar os gêmeos") {
            val pares = tabela(
                "t",
                listOf(listOf("", "A", "B", "C"), listOf("B", ""), listOf("C", "", ""), listOf("D", "", "", "")),
                110f, cw = 62f, ch = 46f, size = 16f,
            )
            cena("Às vezes dois estados fazem a mesma coisa. Dá para juntar os dois.") {
                add(titulo("Tabela de pares"))
                add(rotulo("info", "D é o único final", 180f, 62f))
                add(pares)
            }
            cena("Primeira rodada: final e não final são diferentes. Marco todo par com D.") {
                for (c in 1..3) { text("tl3c$c", "X"); tint("tl3c$c", Tint.ORANGE) }
                hot("tl3c1", "tl3c2", "tl3c3")
            }
            cena("Agora olho para onde cada par vai. Com b, A vai para C, e B vai para D.", mood = EpMood.THINK) {
                add(Box("par", "(C,D) já tem X", 180f, 320f, 190f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f))
                hot("tl1c1")
            }
            cena("Se os destinos são diferentes, a origem também é. Marco A com B.") {
                text("tl1c1", "X"); tint("tl1c1", Tint.ORANGE)
                text("tl2c1", "X"); tint("tl2c1", Tint.ORANGE)
                hot("tl1c1", "tl2c1")
            }
            pergunta(
                "Sobrou uma casa em branco. Toque nela: esses dois são gêmeos.",
                setOf("tl2c2"), setOf("tl1c1", "tl2c1", "tl3c1", "tl3c2", "tl3c3"),
                "Procure a casa sem X na tabela.",
                "Isso. B e C vão sempre para o mesmo lugar.",
            ) {
                drop("par")
                hot()
            }
            cena("Casa em branco no fim quer dizer estados iguais. B e C viram um só.", mood = EpMood.HAPPY) {
                add(Box("m", "B + C", 180f, 330f, 120f, 44f, Look.TOKEN, Tint.GREEN, size = 16f))
                hot("m")
            }
        },

        // ─────────────────────────────── FLEX ───────────────────────────────
        episodio(Skill.LEX_FLEX, "O Flex escreve o scanner") {
            cena("Fazer autômato na mão cansa. O Flex gera o scanner a partir de uma lista de regras.") {
                add(titulo("Arquivo .l"))
                add(codigo("r", listOf("\"if\"      return IF;", "[a-z]+    return ID;", "[0-9]+    return NUM;", "[ \\t\\n]  ; /* ignora */"), 80f, passo = 34f, size = 14f))
            }
            cena("Cada linha tem um padrão e uma ação. Casou o padrão, roda a ação.") { hot("r1") }
            pergunta(
                "O texto if casa com a primeira e com a segunda regra, do mesmo tamanho. Quem ganha? Toque na regra.",
                setOf("r0"), setOf("r1"),
                "Com tamanhos iguais, a ordem do arquivo decide.",
                "Isso. No empate, vence a regra de cima.",
            ) {
                add(fileira("e", listOf("if" to Tint.YELLOW), 260f, w = 90f))
                hot()
            }
            cena("E o texto ifa? Agora a segunda regra casa três letras. A maior vence.", mood = EpMood.THINK) {
                drop("e0")
                add(fileira("e", listOf("ifa" to Tint.TEAL), 260f, w = 90f))
                hot("r1")
            }
            cena("Guarde duas regras: primeiro o maior casamento. Empatou, vale a de cima.") {
                add(rotulo("a", "1. maior casamento", 180f, 320f, Tint.YELLOW, 17f), rotulo("b", "2. empate: a de cima", 180f, 350f, Tint.YELLOW, 17f))
                hot()
            }
            cena("Por isso palavras reservadas vêm antes do identificador no arquivo.", mood = EpMood.HAPPY) { hot("r0") }
        },
    )
}
