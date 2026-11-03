package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ParseNode
import com.compilandia.learning.Skill

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

val gramaticas by lazy {
    listOf(
        // ─────────────────────────────── DERIVAÇÃO ───────────────────────────────
        episodio(Skill.CFG_DERIVATION, "Regras de troca") {
            cena("Uma gramática é uma lista de trocas. O E pode virar E mais E, ou pode virar n.") {
                add(titulo("Gramática"))
                add(codigo("g", listOf("E -> E + E", "E -> n"), 74f, x = 90f, passo = 30f, size = 17f))
            }
            cena("Letra maiúscula é não-terminal: ainda vai virar outra coisa. O resto é terminal e fica.") {
                add(fileira("w", listOf("E" to Tint.ORANGE), 200f, w = 44f))
                hot("w0")
            }
            cena("Troco o E pela primeira regra.") {
                drop("w0")
                add(fileira("w", listOf("E" to Tint.ORANGE, "+" to Tint.TEAL, "E" to Tint.ORANGE), 200f, w = 44f))
                hot("g0")
            }
            pergunta(
                "Numa derivação mais à esquerda, qual E eu troco agora? Toque nele.",
                setOf("w0"), setOf("w2"),
                "O nome já conta: mais à esquerda.",
                "Esse mesmo. Sempre o não-terminal mais à esquerda.",
            ) { hot() }
            cena("Ele vira n. Depois troco o outro, e sobra a frase n mais n.") {
                drop("w0", "w1", "w2")
                add(fileira("w", listOf("n" to Tint.TEAL, "+" to Tint.TEAL, "n" to Tint.TEAL), 200f, w = 44f))
                hot("g1")
            }
            cena("Só sobrou terminal: a frase pertence à linguagem. O desenho dessas trocas é a árvore.", mood = EpMood.HAPPY) {
                dropAll("w"); hot()
                val (b, w) = arvore(no("E", no("E", no("n")), no("+"), no("E", no("n"))), 170f, passoY = 58f, largura = 240f)
                add(b); for (x in w) wire(x)
            }
        },

        // ─────────────────────────────── AMBIGUIDADE ───────────────────────────────
        episodio(Skill.AMBIGUITY, "Duas árvores, uma frase") {
            val mais = no("E", no("E", no("n")), no("+"), no("E", no("E", no("n")), no("*"), no("E", no("n"))))
            val vezes = no("E", no("E", no("E", no("n")), no("+"), no("E", no("n"))), no("*"), no("E", no("n")))
            cena("Esta gramática aceita mais e vezes. Vou montar a frase n mais n vezes n.") {
                add(titulo("n + n * n"))
                add(codigo("g", listOf("E -> E + E | E * E | n"), 66f, x = 60f, size = 16f))
            }
            cena("Nesta árvore o vezes fica embaixo. Ele acontece primeiro, como na matemática.") {
                val (b, w) = arvore(mais, 112f, passoY = 50f, largura = 300f)
                add(b); for (x in w) wire(x)
            }
            cena("Mas dá para montar outra árvore, com o mais embaixo.", mood = EpMood.SURPRISED) {
                dropAll("t")
                val (b, w) = arvore(vezes, 112f, passoY = 50f, largura = 300f, prefix = "u")
                add(b); for (x in w) wire(x)
            }
            cena("Mesma frase, duas árvores. Isso é ambiguidade.", who = PROF) { hot("u") }
            pergunta(
                "Nesta árvore, o que roda primeiro? Toque na resposta.",
                setOf("m"), setOf("v"),
                "Quem está mais fundo na árvore acontece antes.",
                "Certo. Aqui o mais roda primeiro, e o resultado sai errado.",
            ) {
                hot()
                add(Box("m", "o +", 110f, 374f, 110f, 42f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("v", "o *", 250f, 374f, 110f, 42f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena("Compilador não pode ter dúvida. Conserte com precedência ou reescrevendo a gramática.", mood = EpMood.HAPPY) { drop("m", "v") }
        },

        // ─────────────────────────────── RECURSÃO À ESQUERDA ───────────────────────────────
        episodio(Skill.LEFT_RECURSION, "O laço sem fim") {
            cena("Olhe esta regra. O E começa com o próprio E.") {
                add(titulo("E -> E + n | n"))
                add(Box("e1", "E", 180f, 130f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
            }
            cena("Um parser de cima para baixo procura E. Para isso, procura E. Para isso, procura E.", mood = EpMood.SURPRISED) {
                add(Box("e2", "E", 180f, 196f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e3", "E", 180f, 262f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e4", "...", 180f, 328f, 60f, 46f, Look.TOKEN, Tint.RED, size = 20f))
                wire("e1", "e2"); wire("e2", "e3"); wire("e3", "e4")
            }
            pergunta(
                "Qual destas regras tem recursão à esquerda? Toque nela.",
                setOf("r0"), setOf("r1", "r2"),
                "Procure a regra cujo lado direito começa com o próprio lado esquerdo.",
                "Isso. A começa com A, então o parser não sai do lugar.",
            ) {
                clear()
                add(titulo("Qual trava?"))
                add(Box("r0", "A -> A b", 180f, 100f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
                add(Box("r1", "A -> b A", 180f, 170f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
                add(Box("r2", "A -> c", 180f, 240f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
            }
            cena("O conserto: a parte sem recursão vem primeiro. O resto vira uma regra nova.") {
                clear()
                add(titulo("Antes"))
                add(codigo("a", listOf("E -> E + n | n"), 70f, x = 80f, size = 17f))
                add(rotulo("d", "Depois", 180f, 130f, Tint.YELLOW, 22f))
                add(codigo("b", listOf("E  -> n E'", "E' -> + n E' | ε"), 160f, x = 80f, passo = 32f, size = 17f))
            }
            cena("A linguagem é a mesma: n, depois zero ou mais vezes mais n. O laço saiu.", mood = EpMood.HAPPY) { hot("b1") }
        },

        // ─────────────────────────────── FATORAÇÃO ───────────────────────────────
        episodio(Skill.LEFT_FACTORING, "Pôr em evidência") {
            cena("Estas duas regras começam igual. Vendo o a, o parser não sabe qual escolher.", mood = EpMood.THINK) {
                add(titulo("Qual das duas?"))
                add(fileira("x", listOf("a" to Tint.YELLOW, "b" to Tint.NONE), 110f, w = 60f, h = 46f))
                add(fileira("y", listOf("a" to Tint.YELLOW, "c" to Tint.NONE), 170f, w = 60f, h = 46f))
            }
            pergunta(
                "Toque na parte que as duas regras têm em comum.",
                setOf("x0"), setOf("x1", "y1"),
                "Compare o começo das duas linhas.",
                "Isso. O a é o prefixo comum.",
            ) {}
            cena("Faço como na matemática: ponho o a em evidência.", who = PROF) {
                drop("y0")
                move("x0", 120f, 150f)
                move("x1", 240f, 120f)
                move("y1", 240f, 180f)
            }
            cena("A escolha fica para depois do a. Aí basta olhar a próxima letra.") {
                clear()
                add(titulo("Fatorada"))
                add(codigo("b", listOf("A  -> a A'", "A' -> b | c"), 90f, x = 90f, passo = 34f, size = 18f))
                hot("b1")
            }
            cena("Com um símbolo só de olhada, o parser sempre sabe o que fazer.", mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── FIRST ───────────────────────────────
        episodio(Skill.FIRST, "Com o que isso começa?") {
            val gram = listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )")
            fun saco(id: String, nome: String, x: Float) = Box(id, nome, x, 300f, 104f, 90f, Look.BIN, Tint.NONE, size = 15f)
            cena("O conjunto FIRST responde: com qual token isso pode começar?") {
                add(titulo("FIRST"))
                add(codigo("g", gram, 62f, x = 90f, passo = 28f, size = 15f))
                add(saco("sT", "FIRST(T)", 64f), saco("sX", "FIRST(X)", 180f), saco("sE", "FIRST(E)", 296f))
            }
            cena("Comece pelo fácil. T começa com id ou com abre parêntese.") {
                hot("g2")
                add(Box("t1", "id", 42f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("t2", "(", 86f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
            }
            pergunta(
                "E o X? Ele tem duas regras. Toque nos dois itens do FIRST de X.",
                setOf("o1", "o3"), setOf("o2", "o4"),
                "Uma regra começa com mais. A outra pode sumir, e isso é o épsilon.",
                "Isso. Mais e épsilon.",
            ) {
                hot("g1")
                add(Box("o1", "+", 90f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW), Box("o2", "T", 150f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW))
                add(Box("o3", "ε", 210f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW), Box("o4", "id", 270f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW))
            }
            cena("Agora E. Ele começa com T, então herda o FIRST de T.") {
                drop("o1", "o2", "o3", "o4")
                add(Box("x1", "+", 158f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("x2", "ε", 202f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                add(Box("e1", "id", 274f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("e2", "(", 318f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                hot("g0", "e1", "e2")
            }
            cena("Se o primeiro símbolo pudesse sumir, eu também olharia o seguinte.", who = PROF) { hot() }
            cena("FIRST diz qual regra escolher ao ver o próximo token.", mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── FOLLOW ───────────────────────────────
        episodio(Skill.FOLLOW, "O que vem depois?") {
            val gram = listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )")
            fun saco(id: String, nome: String, x: Float) = Box(id, nome, x, 300f, 104f, 90f, Look.BIN, Tint.NONE, size = 15f)
            cena("Se o X pode sumir, como o parser sabe quando apagar? Olhando o que vem depois dele.", mood = EpMood.THINK) {
                add(titulo("FOLLOW"))
                add(codigo("g", gram, 62f, x = 90f, passo = 28f, size = 15f))
                add(saco("sE", "FOLLOW(E)", 64f), saco("sX", "FOLLOW(X)", 180f), saco("sT", "FOLLOW(T)", 296f))
            }
            cena("O símbolo inicial ganha o fim da entrada, o cifrão.") {
                add(Box("e1", "$", 42f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                hot("e1")
            }
            cena("Em abre parêntese, E, fecha parêntese, o que vem depois de E é o fecha parêntese.") {
                add(Box("e2", ")", 86f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                hot("g2", "e2")
            }
            cena("X é o último de E. O que segue E, segue X também.") {
                add(Box("x1", "$", 158f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("x2", ")", 202f, 280f, 38f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                hot("g0", "x1", "x2")
            }
            pergunta(
                "Depois de T vem X. E o X pode começar com qual token? Toque nele.",
                setOf("o1"), setOf("o2", "o3"),
                "Olhe o FIRST de X, sem o épsilon.",
                "Isso. O FIRST de X, sem épsilon, entra no FOLLOW de T.",
            ) {
                hot("g0")
                add(Box("o1", "+", 110f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW), Box("o2", "id", 180f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW), Box("o3", "(", 250f, 190f, 46f, 40f, Look.TOKEN, Tint.YELLOW))
            }
            cena("Como X pode sumir, o que segue E também segue T. Resultado: mais, cifrão e fecha parêntese.") {
                drop("o1", "o2", "o3")
                add(Box("t1", "+", 262f, 280f, 30f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("t2", "$", 296f, 280f, 30f, 28f, Look.TOKEN, Tint.TEAL, size = 13f), Box("t3", ")", 330f, 280f, 30f, 28f, Look.TOKEN, Tint.TEAL, size = 13f))
                hot("t1", "t2", "t3")
            }
            cena("FIRST olha para a frente da regra. FOLLOW olha para o que vem depois dela.", mood = EpMood.HAPPY) { hot() }
        },
    )
}
