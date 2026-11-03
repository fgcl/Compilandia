package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.alvo
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill

/** Pegadinhas de prova: da fase léxica à ponte da transformação. */
val pegadinhasFrente by lazy {
    listOf(
        episodio(Skill.PHASES, "Quem pega este erro?", PEGADINHA) {
            val caixas = listOf(
                Box("bl", "léxico", 62f, 300f, 104f, 92f, Look.BIN, Tint.TEAL, size = 15f),
                Box("bs", "sintático", 180f, 300f, 104f, 92f, Look.BIN, Tint.ORANGE, size = 15f),
                Box("bm", "semântico", 298f, 300f, 104f, 92f, Look.BIN, Tint.PINK, size = 15f),
            )
            val fases = setOf("bl", "bs", "bm")
            cena("Três erros parecidos caem na prova. Cada um é pego por uma fase diferente.") {
                add(titulo("Quem pega?"))
                add(caixas)
            }
            pergunta(
                "Primeiro: int x arroba igual a dois. Esse caractere não existe na linguagem. Quem reclama?",
                setOf("bl"), fases - "bl",
                "Nenhum token começa com arroba. Quem monta os tokens?",
                "Isso. O scanner para antes de qualquer outra fase.",
            ) { add(Box("c", "int x@ = 2;", 180f, 140f, 240f, 50f, Look.CELL, size = 18f)); hot("c") }
            pergunta(
                "Agora falta o ponto e vírgula. Os tokens estão certos. Quem reclama?",
                setOf("bs"), fases - "bs",
                "Toda palavra existe. O que falta é uma peça na frase.",
                "Sintática. A forma do comando está errada.",
            ) { text("c", "x = 2  y = 3;"); hot("c") }
            pergunta(
                "Por último, y mais um, com y nunca declarado. A frase tem forma certa. Quem reclama?",
                setOf("bm"), fases - "bm",
                "O problema não é a forma. É o nome y, que não existe.",
                "Semântica. Só ela consulta a tabela de símbolos.",
            ) { text("c", "x = y + 1;"); hot("c") }
            cena("Regra de bolso: letra errada é léxico, forma errada é sintaxe, sentido errado é semântica.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.TOKENS, "Por que ifa não é if", PEGADINHA) {
            cena("Olhe esta linha: ifa igual a um. O scanner começa pelo i.") {
                add(titulo("ifa = 1;"))
                add(fita("ifa=1;", 150f, cell = 46f))
                hot("c0")
            }
            cena("Depois do i vem o f. O pedaço agora é if, que é palavra reservada.") { hot("c0", "c1"); tint("c0", Tint.YELLOW); tint("c1", Tint.YELLOW) }
            pergunta(
                "Ele fecha o token agora? Toque na última letra desse token.",
                setOf("c2"), setOf("c1", "c3"),
                "O scanner estica enquanto der. O a ainda cabe num nome.",
                "Isso. O a ainda cabe, então o token é ifa.",
            ) { hot() }
            cena("Maior casamento: fica o pedaço mais comprido. Por isso ifa é um nome comum.") {
                tint("c2", Tint.YELLOW); hot("c0", "c1", "c2")
                add(fileira("t", listOf("ifa" to Tint.TEAL, "=" to Tint.ORANGE, "1" to Tint.YELLOW, ";" to Tint.BLUE), 270f, w = 62f))
            }
            cena("A palavra reservada só ganha no empate. If sozinho tem o mesmo tamanho nas duas regras.", who = PROF) { hot() }
            palavra("MAIOR CASAMENTO", "Ordem de decisão: primeiro o tamanho, depois a reservada.", mood = EpMood.HAPPY)
        },

        episodio(Skill.REGEX_DFA, "Estrela, mais e interrogação", PEGADINHA) {
            val linhas = listOf(
                listOf("", "vazia", "a", "aa"),
                listOf("a*", "", "", ""),
                listOf("a+", "", "", ""),
                listOf("a?", "", "", ""),
            )
            cena("Três símbolos parecidos, que a prova adora trocar.") {
                add(titulo("a*   a+   a?"))
                add(tabela("m", linhas, 110f, cw = 76f, ch = 54f, size = 15f))
            }
            cena("A estrela aceita zero ou mais. Vale vazio, um a e dois a.") {
                hot("ml1c0"); text("ml1c1", "sim"); text("ml1c2", "sim"); text("ml1c3", "sim")
                tint("ml1c1", Tint.GREEN); tint("ml1c2", Tint.GREEN); tint("ml1c3", Tint.GREEN)
            }
            pergunta(
                "O mais exige pelo menos um. Toque na casa em que a mais recusa a cadeia.",
                setOf("ml2c1"), setOf("ml2c2", "ml2c3"),
                "Pelo menos um a. Qual cadeia não tem nenhum?",
                "Isso. A cadeia vazia fica de fora.",
            ) { hot("ml2c0") }
            cena("A interrogação é zero ou um. Vale vazio, vale um a, e dois a passa do limite.") {
                text("ml2c1", "não"); text("ml2c2", "sim"); text("ml2c3", "sim")
                tint("ml2c1", Tint.RED); tint("ml2c2", Tint.GREEN); tint("ml2c3", Tint.GREEN)
                text("ml3c1", "sim"); text("ml3c2", "sim"); text("ml3c3", "não")
                tint("ml3c1", Tint.GREEN); tint("ml3c2", Tint.GREEN); tint("ml3c3", Tint.RED)
                hot("ml3c0")
            }
            cena("Estrela é zero ou mais. Mais é um ou mais. Interrogação é zero ou um.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.NFA_TO_DFA, "O fecho esquecido", PEGADINHA) {
            val pos = listOf(60f to 130f, 180f to 130f, 300f to 130f)
            cena("Este autômato anda de q0 para q1 sem ler nada. A seta com épsilon é de graça.") {
                add(titulo("Começo do AFD"))
                add(estados(pos, setOf(2)))
                wire("q0", "q1", "ε"); wire("q1", "q2", "a")
            }
            pergunta(
                "Qual é o estado inicial do AFD? Toque em todos os estados do AFN que entram nele.",
                setOf("q0", "q1"), setOf("q2"),
                "Comece em q0 e ande pelas setas de épsilon. Sem ler letra nenhuma.",
                "Isso. Inicial é q0 mais tudo que se alcança sem ler.",
            ) { hot() }
            cena("O erro comum é escrever só q0. Sem o fecho, a seta com a some, e o AFD aceita menos.") {
                add(Box("s", "{ q0, q1 }", 180f, 260f, 170f, 50f, Look.TOKEN, Tint.YELLOW, size = 18f))
                add(rotulo("r", "estado inicial do AFD", 180f, 302f))
            }
            cena("A receita tem dois passos: anda com a letra, depois fecha com épsilon.", who = PROF) {
                add(Box("p", "move  depois  fecho", 180f, 344f, 240f, 40f, Look.CELL, Tint.TEAL, size = 15f))
                hot("p")
            }
            palavra("FECHO", "Nunca pule o fecho. Ele vale no começo e depois de cada letra.", mood = EpMood.HAPPY)
        },

        episodio(Skill.LEX_FLEX, "A ordem das regras no Flex", PEGADINHA) {
            cena("Neste arquivo ponto l, o nome vem antes do if.") {
                add(titulo("Arquivo .l"))
                add(codigo("r", listOf("[a-z]+   { return ID; }", "\"if\"     { return IF; }"), 110f, x = 56f, passo = 40f, size = 16f))
            }
            cena("Chega a palavra if. As duas regras casam duas letras. Deu empate.") {
                hot("r0", "r1")
                add(Box("in", "if", 180f, 220f, 90f, 50f, Look.CELL, Tint.YELLOW, size = 22f))
            }
            pergunta(
                "No empate, o Flex fica com a primeira regra do arquivo. Qual regra ganha aqui?",
                setOf("r0"), setOf("r1"),
                "Olhe qual linha vem primeiro.",
                "Isso. O if vira ID, e a regra de baixo nunca roda.",
            ) { hot() }
            cena("O Flex até avisa que a regra de baixo nunca casa. Quem lê o aviso acha o erro.", who = PROF) {
                add(Box("ok", "if vira ID", 180f, 300f, 170f, 44f, Look.TOKEN, Tint.RED, size = 17f))
            }
            cena("O conserto é subir a palavra reservada. Reservada em cima, nome embaixo.", mood = EpMood.HAPPY) {
                move("r1", 56f, 110f); move("r0", 56f, 150f)
                text("ok", "if vira IF"); tint("ok", Tint.GREEN)
            }
        },

        episodio(Skill.AMBIGUITY, "O else solto", PEGADINHA) {
            val i1 = Box("i1", "if 1", 116f, 110f, 74f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val i2 = Box("i2", "if 2", 180f, 200f, 74f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val e = Box("e", "else", 244f, 290f, 74f, 40f, Look.NODE, Tint.TEAL, size = 15f)
            cena("Dois ifs e um else só. De qual if é esse else?") {
                add(titulo("if a if b x else y", size = 22f))
                add(i1, i2, e)
                wire("i1", "i2", arrow = false)
            }
            cena("Uma leitura liga o else ao if de dentro.") { wire("i2", "e", arrow = false, hot = true) }
            cena("A outra liga o else ao if de fora. Duas árvores, a gramática é ambígua.") {
                clearWires(); wire("i1", "i2", arrow = false); wire("i1", "e", arrow = false, hot = true)
                move("e", 60f, 200f)
            }
            pergunta(
                "A regra das linguagens: o else fica com o if mais perto. Toque nesse if.",
                setOf("i2"), setOf("i1"),
                "Ande para trás a partir do else. Qual if aparece primeiro?",
                "Isso. O else fica com o if de dentro.",
            ) { hot() }
            cena("O Bison resolve empilhando o else. Assim ele gruda no if de dentro.", who = PROF) {
                clearWires(); wire("i1", "i2", arrow = false); wire("i2", "e", arrow = false, hot = true)
                move("e", 244f, 290f)
            }
            cena("Se quiser o if de fora, use chaves. Elas fecham o if de dentro.", mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CFG_DERIVATION, "Duas derivações, uma árvore", PEGADINHA) {
            cena("Olhe esta gramática pequena. A frase é a b.") {
                add(titulo("a b"))
                add(codigo("g", listOf("S -> A B", "A -> a", "B -> b"), 74f, x = 120f, passo = 26f, size = 15f))
            }
            cena("Pela esquerda: S vira A B, depois a B, depois a b.") {
                add(Box("e", "S  A B  a B  a b", 180f, 190f, 300f, 42f, Look.CELL, Tint.TEAL, size = 15f))
            }
            cena("Pela direita: S vira A B, depois A b, depois a b.") {
                add(Box("d", "S  A B  A b  a b", 180f, 240f, 300f, 42f, Look.CELL, Tint.ORANGE, size = 15f))
            }
            pergunta(
                "São duas derivações diferentes. Isso é ambiguidade?",
                setOf("nao"), setOf("sim"),
                "Ambiguidade pede duas árvores, ou duas derivações do mesmo lado.",
                "Isso. As duas montam a mesma árvore. Só muda a ordem.",
            ) {
                add(Box("sim", "é ambígua", 106f, 310f, 140f, 50f, Look.TOKEN, Tint.RED, size = 16f))
                add(Box("nao", "não é", 254f, 310f, 140f, 50f, Look.TOKEN, Tint.GREEN, size = 16f))
            }
            cena("Para provar ambiguidade, compare duas derivações pela esquerda, ou duas pela direita.", who = PROF, mood = EpMood.HAPPY) { drop("sim", "nao") }
        },

        episodio(Skill.LEFT_RECURSION, "A recursão escondida", PEGADINHA) {
            cena("Nenhuma regra aqui começa com ela mesma. Parece que não tem recursão.") {
                add(titulo("Sem recursão?"))
                add(codigo("g", listOf("S -> A a | b", "A -> S c | d"), 80f, x = 104f, passo = 30f, size = 17f))
            }
            cena("Siga a primeira opção. S vira A a.") {
                add(fileira("f", listOf("A" to Tint.ORANGE, "a" to Tint.TEAL), 210f, w = 56f))
            }
            pergunta(
                "Agora troque o A por S c. Quem reaparece no começo? Toque nele.",
                setOf("f0"), setOf("f1", "f2"),
                "Olhe o primeiro símbolo da frase.",
                "Isso. S chegou em S sem ler nada. É recursão indireta.",
            ) {
                drop("f0", "f1")
                add(fileira("f", listOf("S" to Tint.RED, "c" to Tint.TEAL, "a" to Tint.TEAL), 210f, w = 56f))
            }
            cena("O conserto começa colocando as opções de A dentro de S. Assim a recursão aparece.") {
                dropAll("f")
                add(Box("n", "S -> S c a | d a | b", 180f, 230f, 280f, 46f, Look.CELL, Tint.YELLOW, size = 17f))
                hot("n")
            }
            cena("Agora é recursão direta, e a receita de sempre resolve.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.LEFT_FACTORING, "Quando fatorar não basta", PEGADINHA) {
            cena("Aqui as opções de A começam diferente: uma com a, outra com B.") {
                add(titulo("Sem prefixo?"))
                add(codigo("g", listOf("A -> a b | B", "B -> a c"), 80f, x = 110f, passo = 30f, size = 17f))
            }
            cena("Mas B também começa com a. O começo igual está escondido uma regra abaixo.") { hot("g1") }
            pergunta(
                "Com a na entrada, quantas opções de A valem? Toque no número.",
                setOf("n2"), setOf("n1"),
                "A primeira opção começa com a. E o B começa com o quê?",
                "Isso. Duas opções: é conflito, mesmo sem prefixo à vista.",
            ) {
                hot()
                add(Box("n1", "uma", 110f, 230f, 110f, 56f, Look.TOKEN, Tint.TEAL, size = 18f))
                add(Box("n2", "duas", 250f, 230f, 110f, 56f, Look.TOKEN, Tint.ORANGE, size = 18f))
            }
            cena("Primeiro coloque B dentro de A. Depois fatore.") {
                drop("n1", "n2")
                add(codigo("c", listOf("A -> a b | a c", "A -> a X", "X -> b | c"), 220f, x = 110f, passo = 34f, size = 17f))
                hot("c0")
            }
            cena("Fatorar só enxerga o que está escrito. Às vezes é preciso abrir a regra antes.", who = PROF, mood = EpMood.HAPPY) { hot("c1", "c2") }
        },
    )
}

@Suppress("unused") private val keepAlvo = ::alvo
