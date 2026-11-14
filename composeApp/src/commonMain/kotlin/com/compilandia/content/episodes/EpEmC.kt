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
import com.compilandia.textos.tx

/**
 * Série "Compilador em C": o mesmo compilador escrito em C, um pedaço por aula.
 * O código C aparece curto, com a linha acesa, e o efeito dela no palco ao lado. Quem calcula é o motor.
 */
val serieEmC by lazy {
    listOf(
        // ─────────────────────────────── 1. O laço do getchar ───────────────────────────────
        episodio(Skill.TOKENS, tx("epemc_em_c_1_laco_getchar"), EM_C) {
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
            fun vars(cc: String, v: String) = registro("vars", tx("epemc_variaveis"), listOf("c = $cc", "v = $v"), 95f, 352f, 130f, Tint.NONE, 13f)
            // Por que há duas linguagens (revisor aluno do bloco 6): aqui C com árvore, no Guia do TP Java sem árvore.
            cena(tx("epemc_duas_linguagens"), who = PROF) {
                add(titulo(tx("epemc_titulo_duas")))
                add(Box("c", tx("epemc_serie_c"), 180f, 130f, 320f, 50f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("j", tx("epemc_guia_java"), 180f, 196f, 320f, 50f, Look.TOKEN, Tint.ORANGE, size = 15f))
                add(rotulo("mesma", tx("epemc_uma_funcao_por_regra"), 180f, 258f, Tint.YELLOW, 16f))
            }
            cena(tx("epemc_vamos_escrever_analisador_lexico_em")) {
                clear()
                add(titulo("lexer.c"))
                add(codigo("s", c, 58f, passo = 21f, size = 12f))
                add(fita(linha, 290f, 30f))
            }
            cena(tx("epemc_getchar_le_caractere_por_vez")) {
                hot("s0"); tint("c0", Tint.YELLOW)
                add(vars("'4'", "?"))
            }
            cena(tx("epemc_digito_entra_no_if_valor")) {
                hot("s1", "s2"); add(vars("'4'", "0"))
            }
            cena(tx("epemc_cada_volta_while_junta_digito")) {
                hot("s4"); add(vars("'4'", "4"))
            }
            cena(tx("epemc_le_2_quatro_vezes_dez")) {
                hot("s4", "s5"); tint("c1", Tint.YELLOW); add(vars("'2'", "42"))
            }
            pergunta(
                tx("epemc_agora_chegou_mais_qual_linha"),
                setOf("s7"), setOf("s4", "s5", "s1"),
                tx("epemc_mais_nao_digito_while_para"),
                tx("epemc_isso_laco_para_ungetc_devolve"),
            ) { hot(); tint("c2", Tint.ORANGE); add(vars("'+'", "42")) }
            cena(tx("epemc_mais_foi_lido_mais_ungetc")) { hot("s7"); tint("c2", Tint.NONE) }
            cena(tx("epemc_sai_token_ele_struct_com"), mood = EpMood.HAPPY) {
                hot("s8")
                add(registro("tok", tx("comum_token"), listOf(tx("epemc_tipo_num"), tx("epemc_valor_1", primeiro)), 265f, 352f, 140f, Tint.TEAL, 13f))
            }
            brinquedo(
                tx("epemc_agora_corte_linha_inteira_como"),
                tx("epemc_maior_casamento_estique_pedaco_enquanto"),
                tx("epemc_tokens_que_voce_cortou_sao"),
            ) { ToyCorte("x1 = 42 + y;", tx("epemc_corte_linha_em_tokens")) }
        },

        // ─────────────────────────────── 2. Uma função por regra ───────────────────────────────
        episodio(Skill.RECURSIVE_DESCENT, tx("epemc_em_c_2_funcao_por"), EM_C) {
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
            cena(tx("epemc_na_descida_recursiva_em_c")) {
                add(titulo("parser.c"))
                add(codigo("s", codigoExpr, 70f, x = 40f, passo = 26f, size = 14f))
            }
            cena(tx("epemc_expr_chama_termo_while_repete")) { hot("s1", "s2") }
            cena(tx("epemc_termo_igual_com_vezes_fator")) {
                add(codigo("t", listOf("void termo(void) {", "  fator(); while (tok == '*') ...", "}", "void fator(void) { casa(NUM); }"), 270f, x = 14f, passo = 24f, size = 13f))
                hot("t1", "t3")
            }
            cena(tx("epemc_vamos_rodar_com_dois_mais")) {
                dropAll("s"); dropAll("t")
                add(rotulo("rp", tx("comum_pilha_chamadas"), 100f, 400f))
                add(codigo("g", listOf(tx("comum_expr_termo_termo"), tx("comum_termo_fator_fator"), tx("comum_fator_num")), 180f, x = 180f, passo = 30f, size = 11f))
                mostra(0)
            }
            cena(tx("epemc_expr_chama_termo_termo_chama")) { mostra(2) }
            cena(tx("epemc_fator_casa_2_volta_depois")) {
                mostra(rastro.indexOfFirst { it.acao == "voltar" && it.alvo == "termo" })
            }
            pergunta(
                tx("epemc_agora_vem_mais_qual_funcao"),
                setOf("f${rastro[casaMais].pilha.lastIndex}"), setOf("f0"),
                tx("epemc_quem_tem_while_mais"),
                tx("epemc_isso_while_mais_mora_dentro"),
            ) { mostra(casaMais) }
            cena(tx("epemc_no_vezes_quem_casa_termo"), who = PROF) {
                mostra(rastro.indexOfFirst { it.acao == "casar" && it.alvo == "*" })
            }
            brinquedo(
                tx("epemc_agora_voce_processador_chame_case"),
                tx("epemc_chame_quando_topo_precisa_outra"),
                tx("epemc_voce_rodou_descida_recursiva_inteira"),
            ) { ToyChamadas(listOf("1", "*", "2", "+", "3"), tx("epemc_rode_1_2_3_ate")) }
        },

        // ─────────────────────────────── 3. O struct Node ───────────────────────────────
        episodio(Skill.AST_BUILD, tx("epemc_em_c_3_struct_node"), EM_C) {
            val e = ExprParser.parse("2 + 3 * 4")
            val (nos, ponteiros) = structsDaExpr(e, 110f, 118f, 310f, size = 13f, folhaW = 84f, opW = 94f)
            val folhas = nos.filter { it.rows.size == 1 }.map { it.id }
            val vezes = nos.first { it.rows.firstOrNull() == "op: '*'" }.id
            val raiz = "t"
            cena(tx("epemc_na_ast_em_c_cada")) {
                add(titulo("struct Node"))
                add(codigo("s", listOf("typedef struct Node {", "  char op;", "  int val;", "  struct Node *esq, *dir;", "} Node;"), 120f, x = 60f, passo = 30f, size = 15f))
            }
            cena(tx("epemc_op_guarda_operador_val_guarda")) { hot("s1", "s2") }
            cena(tx("epemc_esq_dir_sao_ponteiros_eles")) { hot("s3") }
            cena(tx("epemc_para_dois_mais_tres_vezes")) {
                dropAll("s")
                add(titulo("2 + 3 * 4"))
                add(nos.filter { it.id in folhas })
            }
            cena(tx("epemc_primeiro_folhas_no_para_cada")) {
                add(nos.filter { it.id == vezes })
                wires(ponteiros.filter { it.from == vezes })
                hot(vezes)
            }
            cena(tx("epemc_por_ultimo_mais_que_vira")) {
                add(nos.filter { it.id == raiz })
                wires(ponteiros.filter { it.from == raiz })
                hot(raiz)
            }
            pergunta(
                tx("epemc_para_qual_no_aponta_ponteiro"),
                setOf(vezes), folhas.toSet(),
                tx("epemc_siga_fio_que_sai_linha"),
                tx("epemc_isso_dir_raiz_aponta_para"),
            ) { hot(); lit(raiz, 2) }
            cena(tx("epemc_arvore_isso_structs_ligados_por"), who = PROF, mood = EpMood.HAPPY) { lit(raiz, -1); hot() }
        },

        // ─────────────────────────────── 4. Da árvore ao MIPS ───────────────────────────────
        episodio(Skill.CODEGEN_MIPS, tx("epemc_em_c_4_arvore_ao"), EM_C) {
            val src = tx("comum_b_c")
            val e = ExprParser.parse(src)
            val mips = Mips.fromTac(TacGen.generate(e))
            val (nos, ponteiros) = structsDaExpr(e, 90f, 110f, 190f, dx = -86f, size = 11f, folhaW = 60f, opW = 76f)
            val op = nos.filter { it.rows.size == 3 }
            val vezes = op.first { it.rows[0] == "op: '*'" }.id
            val raiz = "t"
            // Cada operador do TAC vira quatro linhas: carrega, carrega, calcula e guarda.
            val porOp = mips.size / op.size
            fun linhas(n: Int) = codigo("m", mips.take(n), 74f, x = 196f, passo = 24f, size = 12f)
            cena(tx("epemc_gerador_anda_na_arvore_escreve")) {
                add(titulo(src))
                add(nos); wires(ponteiros)
            }
            cena(tx("epemc_ele_visita_filhos_antes_pai")) { hot(vezes) }
            cena(tx("epemc_vezes_vira_quatro_linhas_carrega")) {
                add(linhas(porOp)); hot(vezes, *(0 until porOp).map { "m$it" }.toTypedArray())
            }
            pergunta(
                tx("epemc_qual_no_gerador_visita_agora"),
                setOf(raiz), nos.map { it.id }.toSet() - raiz - vezes,
                tx("epemc_dois_filhos_raiz_ja_estao"),
                tx("epemc_isso_agora_vez_raiz"),
            ) { hot() }
            cena(tx("epemc_mais_usa_resultado_vezes_que")) {
                add(linhas(mips.size)); hot(raiz, *(porOp until mips.size).map { "m$it" }.toTypedArray())
            }
            cena(tx("epemc_em_c_funcao_recursiva_gera"), who = PROF) {
                add(codigo("c", listOf("void gera(Node *n) {", "  if (n->op) { gera(n->esq); gera(n->dir); }", "  emite(n);", "}"), 300f, x = 14f, passo = 22f, size = 12f))
                hot("c1")
            }
            brinquedo(
                tx("epemc_agora_monte_mips_na_ordem"),
                tx("epemc_antes_calcular_dois_valores_precisam"),
                tx("epemc_exatamente_que_seu_gerador_em"),
            ) { ToyMips(src, tx("epemc_monte_mips_1", src)) }
        },

        // ─────────────────────────────── 5. Flex, Bison e o main ───────────────────────────────
        episodio(Skill.YACC_BISON, tx("epemc_em_c_5_flex_bison"), EM_C) {
            val conta = "2+3*4"
            val valor = MiniBison.compile(SPEC_CALC.joinToString("\n")).run(conta).value
            // O calc.y da tela é o mesmo que o MiniBison roda: sem o %left, e: e '+' e seria a gramática ambígua da Oficina.
            val y = registro("y", "calc.y", SPEC_CALC.filter { it != "%token NUM" && it != "%%" }, 180f, 120f, 300f, Tint.ORANGE, 13f)
            val l = registro("l", "calc.l", listOf(tx("epemc_0_9_num"), tx("epemc_yylval_atoi_yytext")), 92f, 276f, 172f, Tint.TEAL, 13f)
            val m = registro("main", "main.c", listOf("int main(void) {", "  return yyparse();", "}"), 272f, 276f, 162f, Tint.YELLOW, 13f)
            cena(tx("epemc_compilador_em_c_verdade_costuma")) {
                add(titulo(tx("epemc_flex_bison_c")))
                add(l, y, m)
            }
            cena(tx("epemc_main_chama_yyparse_parser_que")) {
                wire(Wire("main", "y", "yyparse()", hot = true))
                lit("main", 1)
            }
            cena(tx("epemc_sempre_que_precisa_token_parser")) {
                lit("main", -1)
                wire(Wire("y", "l", "yylex()", hot = true, bend = 16f))
            }
            cena(tx("epemc_yylex_devolve_tipo_valor_vai")) {
                clearWires()
                wire(Wire("main", "y", "yyparse()"))
                wire(Wire("l", "y", flow = listOf("NUM", "42")))
                lit("l", 1)
            }
            pergunta(
                tx("epemc_quem_chama_yylex_toque_no"),
                setOf("y"), setOf("l", "main"),
                tx("epemc_quem_pede_tokens_parser"),
                tx("epemc_isso_parser_pede_token_por"),
            ) { lit("l", -1) }
            cena(tx("epemc_bison_flex_escrevem_arquivos_c")) {
                add(registro("gcc", "terminal", listOf(tx("epemc_bison_d_calc_y"), tx("epemc_flex_calc_l"), tx("epemc_gcc_c_calc")), 92f, 368f, 172f, Tint.NONE, 13f))
                hot("gcc")
            }
            cena(tx("epemc_roda_calc_digita_conta_sai"), mood = EpMood.HAPPY) {
                add(registro("run", "./calc", listOf(conta, "= $valor"), 272f, 368f, 150f, Tint.GREEN, 14f, lit = 1))
                wire(Wire("gcc", "run"))
                hot("run")
            }
        },
    )
}
