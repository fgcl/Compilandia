package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.EM_C
import com.compilandia.content.Tint
import com.compilandia.content.ToyChamadas
import com.compilandia.content.ToyCorte
import com.compilandia.content.ToyMips
import com.compilandia.content.Wire
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fita
import com.compilandia.content.pilha
import com.compilandia.content.rastroDescida
import com.compilandia.content.registro
import com.compilandia.content.rotulo
import com.compilandia.content.structsDaExpr
import com.compilandia.content.titulo
import com.compilandia.engine.ExprParser
import com.compilandia.engine.MiniBison
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.Mips
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill

/**
 * Série "Compilador em C": o mesmo compilador escrito em C, um pedaço por aula.
 * O código C aparece curto, com a linha acesa, e o efeito dela no palco ao lado. Quem calcula é o motor.
 */
val serieEmC by lazy {
    listOf(
        // ─────────────────────────────── 1. O laço do getchar ───────────────────────────────
        episodio(Skill.TOKENS, "Em C 1: o laço do getchar", EM_C) {
            val linha = "42+7"
            val primeiro = MiniLexer.tokenize(linha).first().lexeme
            val c = listOf(
                "int c = getchar();",
                "if (isdigit(c)) {",
                "  int v = 0;",
                "  while (isdigit(c)) {",
                "    v = v * 10 + (c - '0');",
                "    c = getchar();",
                "  }",
                "  ungetc(c, stdin);",
                "  return (Token){NUM, v};",
                "}",
            )
            fun vars(cc: String, v: String) = registro("vars", "variáveis", listOf("c = $cc", "v = $v"), 95f, 352f, 130f, Tint.NONE, 13f)
            cena("Vamos escrever o analisador léxico em C, na mão.") {
                add(titulo("lexer.c"))
                add(codigo("s", c, 58f, passo = 21f, size = 12f))
                add(fita(linha, 290f, 30f))
            }
            cena("O getchar lê um caractere por vez. Chegou o 4.") {
                hot("s0"); tint("c0", Tint.YELLOW)
                add(vars("'4'", "?"))
            }
            cena("É dígito. Entra no if, e o valor começa em zero.") {
                hot("s1", "s2"); add(vars("'4'", "0"))
            }
            cena("Cada volta do while junta um dígito. Agora v vale quatro.") {
                hot("s4"); add(vars("'4'", "4"))
            }
            cena("Lê o 2. Quatro vezes dez, mais dois, dá quarenta e dois.") {
                hot("s4", "s5"); tint("c1", Tint.YELLOW); add(vars("'2'", "42"))
            }
            pergunta(
                "Agora chegou o mais. Qual linha roda logo depois? Toque nela.",
                setOf("s7"), setOf("s4", "s5", "s1"),
                "O mais não é dígito. O while para.",
                "Isso. O laço para e o ungetc devolve o mais.",
            ) { hot(); tint("c2", Tint.ORANGE); add(vars("'+'", "42")) }
            cena("O mais foi lido a mais. O ungetc devolve ele para a entrada.") { hot("s7"); tint("c2", Tint.NONE) }
            cena("E sai o token. Ele é um struct com o tipo e o valor.", mood = EpMood.HAPPY) {
                hot("s8")
                add(registro("tok", "Token", listOf("tipo: NUM", "valor: $primeiro"), 265f, 352f, 140f, Tint.TEAL, 13f))
            }
            brinquedo(
                "Agora corte uma linha inteira, como o seu lexer em C faria.",
                "Maior casamento: estique o pedaço enquanto ele ainda for o mesmo token.",
                "Os tokens que você cortou são os que o seu lexer devolveria.",
            ) { ToyCorte("x1 = 42 + y;", "Corte a linha em tokens.") }
        },

        // ─────────────────────────────── 2. Uma função por regra ───────────────────────────────
        episodio(Skill.RECURSIVE_DESCENT, "Em C 2: uma função por regra", EM_C) {
            val toks = listOf("2", "+", "3", "*", "4")
            val rastro = rastroDescida(toks)
            val codigoExpr = listOf(
                "void expr(void) {",
                "  termo();",
                "  while (tok == '+') {",
                "    casa('+');",
                "    termo();",
                "  }",
                "}",
            )
            fun pilhaEm(k: Int): List<Box> {
                val p = rastro[k].pilha
                return pilha("f", p.map { "$it()" }, 100f, 370f, 140f, 34f, Tint.ORANGE).mapIndexed { i, b -> if (i == p.lastIndex) b.copy(tint = Tint.YELLOW) else b }
            }
            fun fitaEm(k: Int): List<Box> = toks.mapIndexed { i, t ->
                Box("e$i", t, 180f + (i - 2) * 52f, 70f, 46f, 36f, Look.TOKEN, if (i < rastro[k].lidos) Tint.DARK else Tint.NONE, size = 16f)
            }
            fun mostra(k: Int) { dropAll("f"); add(pilhaEm(k)); dropAll("e"); add(fitaEm(k)) }
            val casaMais = rastro.indexOfFirst { it.acao == "casar" && it.alvo == "+" }
            cena("Na descida recursiva em C, cada regra da gramática vira uma função.") {
                add(titulo("parser.c"))
                add(codigo("s", codigoExpr, 70f, x = 40f, passo = 26f, size = 14f))
            }
            cena("expr chama termo. O while repete enquanto vier um mais.") { hot("s1", "s2") }
            cena("termo é igual, com o vezes. E fator só casa um número.") {
                add(codigo("t", listOf("void termo(void) {", "  fator(); while (tok == '*') ...", "}", "void fator(void) { casa(NUM); }"), 270f, x = 14f, passo = 24f, size = 13f))
                hot("t1", "t3")
            }
            cena("Vamos rodar com dois mais três vezes quatro. O main chama expr.") {
                dropAll("s"); dropAll("t")
                add(rotulo("rp", "pilha de chamadas", 100f, 400f))
                add(codigo("g", listOf("expr:  termo ('+' termo)*", "termo: fator ('*' fator)*", "fator: NUM"), 180f, x = 180f, passo = 30f, size = 11f))
                mostra(0)
            }
            cena("expr chama termo, e termo chama fator. A pilha cresce.") { mostra(2) }
            cena("fator casa o 2 e volta. Depois termo volta também.") {
                mostra(rastro.indexOfFirst { it.acao == "voltar" && it.alvo == "termo" })
            }
            pergunta(
                "Agora vem o mais. Qual função está no topo da pilha na hora de casar? Toque.",
                setOf("f${rastro[casaMais].pilha.lastIndex}"), setOf("f0"),
                "Quem tem o while do mais?",
                "Isso. O while do mais mora dentro de expr.",
            ) { mostra(casaMais) }
            cena("No vezes, quem casa é termo. Cada função cuida do seu operador.", who = PROF) {
                mostra(rastro.indexOfFirst { it.acao == "casar" && it.alvo == "*" })
            }
            brinquedo(
                "Agora você é o processador. Chame, case e volte, na ordem certa.",
                "Chame quando o topo precisa de outra regra. Case quando o token bate. Volte quando a função acabou.",
                "Você rodou a descida recursiva inteira, igual ao programa em C.",
            ) { ToyChamadas(listOf("1", "*", "2", "+", "3"), "Rode 1 * 2 + 3 até a pilha esvaziar.") }
        },

        // ─────────────────────────────── 3. O struct Node ───────────────────────────────
        episodio(Skill.AST_BUILD, "Em C 3: o struct Node", EM_C) {
            val e = ExprParser.parse("2 + 3 * 4")
            val (nos, ponteiros) = structsDaExpr(e, 110f, 118f, 310f, size = 13f, folhaW = 84f, opW = 94f)
            val folhas = nos.filter { it.rows.size == 1 }.map { it.id }
            val vezes = nos.first { it.rows.firstOrNull() == "op: '*'" }.id
            val raiz = "t"
            cena("Na AST em C, cada nó da árvore é um struct.") {
                add(titulo("struct Node"))
                add(codigo("s", listOf("typedef struct Node {", "  char op;", "  int val;", "  struct Node *esq, *dir;", "} Node;"), 120f, x = 60f, passo = 30f, size = 15f))
            }
            cena("op guarda o operador. val guarda o número.") { hot("s1", "s2") }
            cena("esq e dir são ponteiros. Eles apontam para os filhos.") { hot("s3") }
            cena("Para dois mais três vezes quatro, o parser cria os nós de baixo para cima.") {
                dropAll("s")
                add(titulo("2 + 3 * 4"))
                add(nos.filter { it.id in folhas })
            }
            cena("Primeiro as folhas, um nó para cada número. Depois vem o vezes.") {
                add(nos.filter { it.id == vezes })
                wires(ponteiros.filter { it.from == vezes })
                hot(vezes)
            }
            cena("Por último o mais, que vira a raiz da árvore.") {
                add(nos.filter { it.id == raiz })
                wires(ponteiros.filter { it.from == raiz })
                hot(raiz)
            }
            pergunta(
                "Para qual nó aponta o ponteiro dir da raiz? Toque nele.",
                setOf(vezes), folhas.toSet(),
                "Siga o fio que sai da linha dir.",
                "Isso. O dir da raiz aponta para o vezes.",
            ) { hot(); lit(raiz, 2) }
            cena("A árvore é isso: structs ligados por ponteiros.", who = PROF, mood = EpMood.HAPPY) { lit(raiz, -1); hot() }
        },

        // ─────────────────────────────── 4. Da árvore ao MIPS ───────────────────────────────
        episodio(Skill.CODEGEN_MIPS, "Em C 4: da árvore ao MIPS", EM_C) {
            val src = "a + b * c"
            val e = ExprParser.parse(src)
            val mips = Mips.fromTac(TacGen.generate(e))
            val (nos, ponteiros) = structsDaExpr(e, 90f, 110f, 190f, dx = -86f, size = 11f, folhaW = 60f, opW = 76f)
            val op = nos.filter { it.rows.size == 3 }
            val vezes = op.first { it.rows[0] == "op: '*'" }.id
            val raiz = "t"
            // Cada operador do TAC vira quatro linhas: carrega, carrega, calcula e guarda.
            val porOp = mips.size / op.size
            fun linhas(n: Int) = codigo("m", mips.take(n), 74f, x = 196f, passo = 24f, size = 12f)
            cena("O gerador anda na árvore e escreve MIPS, uma linha por vez.") {
                add(titulo(src))
                add(nos); wires(ponteiros)
            }
            cena("Ele visita os filhos antes do pai. O primeiro nó pronto é o vezes.") { hot(vezes) }
            cena("O vezes vira quatro linhas: carrega b, carrega c, multiplica e guarda.") {
                add(linhas(porOp)); hot(vezes, *(0 until porOp).map { "m$it" }.toTypedArray())
            }
            pergunta(
                "Qual nó o gerador visita agora? Toque nele.",
                setOf(raiz), nos.map { it.id }.toSet() - raiz - vezes,
                "Os dois filhos da raiz já estão prontos.",
                "Isso. Agora é a vez da raiz.",
            ) { hot() }
            cena("O mais usa o resultado do vezes, que já está guardado na memória.") {
                add(linhas(mips.size)); hot(raiz, *(porOp until mips.size).map { "m$it" }.toTypedArray())
            }
            cena("Em C, é uma função recursiva: gera os filhos e depois o pai.", who = PROF) {
                add(codigo("c", listOf("void gera(Node *n) {", "  if (n->op) { gera(n->esq); gera(n->dir); }", "  emite(n);", "}"), 300f, x = 14f, passo = 22f, size = 12f))
                hot("c1")
            }
            brinquedo(
                "Agora monte o MIPS na ordem certa, instrução por instrução.",
                "Antes de calcular, os dois valores precisam estar nos registradores.",
                "É exatamente o que o seu gerador em C escreveria.",
            ) { ToyMips(src, "Monte o MIPS de $src.") }
        },

        // ─────────────────────────────── 5. Flex, Bison e o main ───────────────────────────────
        episodio(Skill.YACC_BISON, "Em C 5: flex, bison e o main", EM_C) {
            val conta = "2+3*4"
            val valor = MiniBison.compile(SPEC_CALC.joinToString("\n")).run(conta).value
            val y = registro("y", "calc.y", listOf("e: e '+' e", "{ \$\$ = \$1 + \$3; }"), 180f, 100f, 180f, Tint.ORANGE, 13f)
            val l = registro("l", "calc.l", listOf("[0-9]+  NUM", "yylval = atoi(yytext)"), 92f, 232f, 172f, Tint.TEAL, 13f)
            val m = registro("main", "main.c", listOf("int main(void) {", "  return yyparse();", "}"), 272f, 232f, 162f, Tint.YELLOW, 13f)
            cena("Um compilador em C de verdade costuma ter três arquivos.") {
                add(titulo("flex + bison + C"))
                add(l, y, m)
            }
            cena("O main chama o yyparse, o parser que o Bison escreveu.") {
                wire(Wire("main", "y", "yyparse()", hot = true, fromRow = 1))
                lit("main", 1)
            }
            cena("Sempre que precisa de um token, o parser chama o yylex.") {
                lit("main", -1)
                wire(Wire("y", "l", "yylex()", hot = true, bend = 16f))
            }
            cena("O yylex devolve o tipo. O valor vai junto, em yylval.") {
                clearWires()
                wire(Wire("main", "y", "yyparse()", fromRow = 1))
                wire(Wire("l", "y", flow = listOf("NUM", "42"), fromRow = 1))
                lit("l", 1)
            }
            pergunta(
                "Quem chama o yylex? Toque no arquivo.",
                setOf("y"), setOf("l", "main"),
                "Quem pede tokens é o parser.",
                "Isso. O parser pede um token por vez.",
            ) { lit("l", -1) }
            cena("O Bison e o Flex escrevem arquivos C. O gcc junta tudo num programa.") {
                add(registro("gcc", "terminal", listOf("bison -d calc.y", "flex calc.l", "gcc *.c -o calc"), 92f, 350f, 172f, Tint.NONE, 13f))
                hot("gcc")
            }
            cena("Roda o calc, digita a conta, e sai o resultado.", mood = EpMood.HAPPY) {
                add(registro("run", "./calc", listOf(conta, "= $valor"), 272f, 350f, 150f, Tint.GREEN, 14f, lit = 1))
                wire(Wire("gcc", "run"))
                hot("run")
            }
        },
    )
}
