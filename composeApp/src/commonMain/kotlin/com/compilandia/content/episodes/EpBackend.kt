package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Mips
import com.compilandia.engine.ParseNode
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

val semantica by lazy {
    listOf(
        // ─────────────────────────────── ATRIBUTOS ───────────────────────────────
        episodio(Skill.ATTRIBUTES_TYPES, "Tipos sobem pela árvore") {
            val raiz = no("+", no("a"), no("*", no("x"), no("2")))
            val (b, w) = arvore(raiz, 96f, passoY = 80f, largura = 300f)
            fun tipo(id: String, t: String, alvo: Box) = Box(id, t, alvo.x, alvo.y + 26f, 56f, 20f, Look.LABEL, Tint.YELLOW, size = 14f)
            val node = b.associateBy { it.text }
            cena("A árvore já existe. Agora a semântica pendura informação nela.") {
                add(titulo("a + x * 2"))
                add(b); for (x in w) wire(x)
                add(rotulo("info", "a é int, x é float", 180f, 330f, size = 17f))
            }
            cena("Começo pelas folhas. A é inteiro, x é real, dois é inteiro.") {
                add(tipo("ta", "int", node.getValue("a")), tipo("tx", "float", node.getValue("x")), tipo("t2", "int", node.getValue("2")))
            }
            pergunta(
                "Real vezes inteiro dá o quê? Toque no tipo.",
                setOf("of"), setOf("oi"),
                "O tipo maior ganha, para não perder a parte quebrada.",
                "Isso. Real com inteiro dá real.",
            ) {
                add(Box("oi", "int", 120f, 380f, 100f, 42f, Look.TOKEN, Tint.TEAL, size = 17f), Box("of", "float", 240f, 380f, 100f, 42f, Look.TOKEN, Tint.ORANGE, size = 17f))
                hot("t1")
            }
            cena("O dois é convertido para real e o resultado sobe.") {
                drop("oi", "of")
                add(tipo("tm", "float", node.getValue("*")))
                hot()
            }
            cena("Na raiz, inteiro mais real também dá real. Valor que sobe de filho para pai é atributo sintetizado.") {
                add(tipo("tr", "float", node.getValue("+")))
                hot("t")
            }
            cena("Se os tipos não combinam, o compilador acusa erro semântico.", who = PROF) { hot() }
        },

        // ─────────────────────────────── ESCOPO ───────────────────────────────
        episodio(Skill.SCOPE_SYMBOLS, "Quem é esse x?") {
            val src = listOf("int x;", "void f() {", "  float x;", "  x = 1.5;", "}", "x = 2;")
            cena("O mesmo nome pode existir em dois lugares. Quem resolve é a tabela de símbolos.") {
                add(titulo("Escopo"))
                add(codigo("c", src, 62f, passo = 27f, size = 15f))
                add(pilha("p", listOf("global: x int"), 180f, 370f, w = 220f, h = 38f, tint = Tint.TEAL))
            }
            cena("Entrou num bloco, empilha uma tabela nova.") {
                hot("c1")
                add(Box("p1", "f: x float", 180f, 328f, 220f, 38f, Look.TOKEN, Tint.ORANGE))
            }
            pergunta(
                "Na linha x igual a um e meio, qual x vale? Toque na tabela certa.",
                setOf("p1"), setOf("p0"),
                "A busca começa na tabela do topo.",
                "Isso. O x de dentro esconde o de fora.",
            ) { hot("c3") }
            cena("A busca começa no topo e desce até achar o nome. Não achou, é erro.") { hot("p1", "p0") }
            cena("Quando o bloco acaba, a tabela sai da pilha.") {
                drop("p1")
                hot("c4")
            }
            cena("Por isso x igual a dois fala do x global.", mood = EpMood.HAPPY) { hot("c5", "p0") }
        },

        // ─────────────────────────────── AST ───────────────────────────────
        episodio(Skill.AST_BUILD, "A árvore enxuta") {
            val deriv = no("E", no("E", no("id")), no("+"), no("T", no("("), no("E", no("id")), no(")")))
            val ast = no("BinOp +", no("a"), no("b"))
            cena("A árvore de derivação guarda tudo, até parêntese e regra de passagem.") {
                add(titulo("a + ( b )"))
                val (b, w) = arvore(deriv, 90f, passoY = 62f, largura = 320f)
                add(b); for (x in w) wire(x)
            }
            pergunta(
                "Tem nó sobrando aqui. Toque nos dois parênteses: eles não mudam a conta.",
                setOf("t20", "t22"), setOf("t", "t00"),
                "A árvore já mostra a ordem. O parêntese fez o trabalho dele.",
                "Isso. Pontuação some da árvore abstrata.",
            ) {}
            cena("A árvore abstrata guarda só o que importa: a operação e os dois lados.") {
                dropAll("t")
                val (b, w) = arvore(ast, 110f, passoY = 90f, largura = 220f, prefix = "a")
                add(b); for (x in w) wire(x)
            }
            cena("No código Java, cada nó é um objeto.") {
                add(Box("j", "new BinOp('+', a, b)", 180f, 310f, 300f, 46f, Look.TOKEN, Tint.YELLOW, size = 15f))
                hot("j")
            }
            cena("Todas as próximas fases andam nessa árvore menor.", mood = EpMood.HAPPY) { hot() }
        },
    )
}

val backend by lazy {
    listOf(
        // ─────────────────────────────── TAC ───────────────────────────────
        episodio(Skill.TAC, "Uma conta por linha") {
            val code = TacGen.generate(ExprParser.parse("a + b * c"))
            val (b, w) = arvore(no("+", no("a"), no("*", no("b"), no("c"))), 86f, passoY = 62f, largura = 240f)
            cena("Máquina não entende conta comprida. Código de três endereços faz uma conta por linha.") {
                add(titulo("a + b * c"))
                add(b); for (x in w) wire(x)
            }
            pergunta(
                "Qual conta tem que sair primeiro? Toque no operador.",
                setOf("t1"), setOf("t"),
                "A árvore manda: quem está mais fundo vem antes.",
                "Isso. O vezes está mais fundo.",
            ) {}
            cena("Guardo o resultado num temporário, o t1.") {
                add(codigo("c", listOf(code[0].toString()), 300f, x = 110f, size = 18f))
                hot("c0", "t1")
            }
            cena("Na linha seguinte, a árvore já pode usar o t1 no lugar do vezes.") {
                add(codigo("d", listOf(code[1].toString()), 332f, x = 110f, size = 18f))
                hot("d0", "t")
            }
            cena("Cada linha tem no máximo três endereços: dois operandos e um resultado.", who = PROF) { hot() }
            cena("A ordem saiu da árvore. O parser já tinha feito esse trabalho.", mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── OTIMIZAÇÃO ───────────────────────────────
        episodio(Skill.OPTIMIZATION, "Conta feita antes") {
            val parts = listOf("x", "=", "2", "*", "3", "+", "y")
            fun item(i: Int, t: String, x: Float) = Box("o$i", t, x, 140f, 40f, 48f, Look.TOKEN, Tint.TEAL, size = 18f)
            cena("Se dá para fazer a conta agora, o compilador faz. O programa nem precisa calcular.") {
                add(titulo("Dobra de constantes"))
                parts.forEachIndexed { i, t -> add(item(i, t, 48f + i * 44f)) }
            }
            pergunta(
                "Toque na parte que o compilador já consegue resolver.",
                setOf("o3"), setOf("o5", "o0"),
                "Procure uma conta só com números.",
                "Isso. Dois vezes três não depende de nada.",
            ) {}
            cena("Dois vezes três vira seis, ainda na compilação.", mood = EpMood.HAPPY) {
                drop("o3", "o4")
                text("o2", "6"); tint("o2", Tint.GREEN); move("o2", 180f, 140f)
                move("o5", 224f, 140f); move("o6", 268f, 140f)
                hot("o2")
            }
            cena("O y fica. O valor dele só existe quando o programa roda.") { hot("o6") }
            cena("Otimizar nunca pode mudar o resultado. Só o caminho até ele.", who = PROF) { hot() }
        },

        // ─────────────────────────────── REGISTROS DE ATIVAÇÃO ───────────────────────────────
        episodio(Skill.RUNTIME, "A pilha de chamadas") {
            fun reg(id: String, t: String, i: Int, c: Tint) = Box(id, t, 180f, 330f - i * 62f, 250f, 54f, Look.TOKEN, c, size = 15f)
            cena("Toda chamada de função ganha um registro na pilha.") {
                add(titulo("fat(2)"))
                add(reg("r0", "fat  n = 2  volta: main", 0, Tint.TEAL))
            }
            cena("Ali moram os parâmetros, as variáveis locais e o endereço de volta.") { hot("r0") }
            cena("O fat de dois chama o fat de um. Outro registro sobe, com outro n.") {
                add(reg("r1", "fat  n = 1  volta: fat", 1, Tint.ORANGE))
                hot("r1")
            }
            cena("Mais uma chamada, agora com n igual a zero.") {
                add(reg("r2", "fat  n = 0  volta: fat", 2, Tint.YELLOW))
                hot("r2")
            }
            pergunta(
                "Quem retorna primeiro? Toque no registro.",
                setOf("r2"), setOf("r0", "r1"),
                "Só o topo da pilha está rodando.",
                "Isso. O último a entrar é o primeiro a sair.",
            ) { hot() }
            cena("Retornou, o registro sai e o controle volta para quem chamou.") {
                drop("r2")
                hot("r1")
            }
            cena("Por isso a recursão funciona: cada chamada tem o seu próprio n.", mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── MIPS ───────────────────────────────
        episodio(Skill.CODEGEN_MIPS, "Carrega, opera, guarda") {
            val tac = TacGen.generate(ExprParser.parse("b * c"))
            val mips = Mips.fromTac(tac)
            cena("O MIPS só faz conta com registradores. Variável mora na memória.") {
                add(titulo(tac[0].toString()))
                add(Box("mem", "memória", 90f, 110f, 130f, 46f, Look.TOKEN, Tint.BLUE, size = 15f), Box("reg", "registradores", 270f, 110f, 150f, 46f, Look.TOKEN, Tint.ORANGE, size = 15f))
                wire("mem", "reg", "lw")
            }
            cena("Por isso toda conta tem três tempos: carrega, opera e guarda.") {
                add(codigo("c", mips, 180f, x = 86f, passo = 34f, size = 17f))
                hot("c0", "c1")
            }
            pergunta(
                "Qual linha faz a multiplicação? Toque nela.",
                setOf("c2"), setOf("c0", "c1", "c3"),
                "Procure a instrução mul.",
                "Isso. Antes dela, os dois lw trazem os valores.",
            ) { hot() }
            cena("O sw leva o resultado de volta para a memória.") { hot("c3") }
            cena("Cada linha do código de três endereços vira esse trio.", mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── REGISTRADORES ───────────────────────────────
        episodio(Skill.REG_ALLOC, "Colorir o grafo") {
            fun v(id: String, x: Float, y: Float) = Box(id, id, x, y, 56f, 56f, Look.STATE, Tint.NONE, size = 18f)
            cena("Registrador é pouco e variável é muita. Vamos dividir.") {
                add(titulo("Alocação"))
                add(v("a", 80f, 140f), v("b", 180f, 240f), v("c", 280f, 140f))
            }
            cena("Duas variáveis vivas ao mesmo tempo não podem usar o mesmo registrador.") {
                wire("a", "b", arrow = false)
                wire("b", "c", arrow = false)
            }
            cena("Liguei quem convive. Esse é o grafo de interferência.", who = PROF) {}
            pergunta(
                "Quais duas podem dividir um registrador? Toque nelas.",
                setOf("a", "c"), setOf("b"),
                "Procure as duas que não têm fio entre elas.",
                "Isso. Sem fio entre elas, nunca convivem.",
            ) {}
            cena("Pinto o grafo: vizinhos nunca têm a mesma cor. Cada cor é um registrador.", mood = EpMood.HAPPY) {
                tint("a", Tint.TEAL); tint("c", Tint.TEAL); tint("b", Tint.ORANGE)
                add(rotulo("ra", "\$t0", 80f, 186f, Tint.TEAL, 17f), rotulo("rb", "\$t1", 180f, 286f, Tint.ORANGE, 17f), rotulo("rc", "\$t0", 280f, 186f, Tint.TEAL, 17f))
            }
            cena("Faltou cor? Uma variável vai para a memória. Isso se chama spill.") {}
        },
    )
}
