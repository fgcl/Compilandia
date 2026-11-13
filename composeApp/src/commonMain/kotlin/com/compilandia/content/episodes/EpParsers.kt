package com.compilandia.content.episodes

import com.compilandia.content.fecho
import com.compilandia.content.Fecho
import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
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
import com.compilandia.textos.tx

/** A tabela LL(1) da gramática das aulas, preenchida (conferida em EpisodeFactsTest.tabelaLl1). */
private val TABELA_LL_1 = listOf(
    listOf("", "id", "+", "(", ")", "$"),
    listOf("E", "T X", "", "T X", "", ""),
    listOf("X", "", "+ T X", "", "ε", "ε"),
    listOf("T", "id", "", "( E )", "", ""),
)

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

/*
 * A9: as aulas básicas destes cinco assuntos viraram longas, no roteiro do A2
 * (por que existe, primeiro exemplo, a receita à vista e um segundo exemplo em que a pessoa decide).
 * O que o segundo exemplo afirma é conferido contra o motor em A9ParsersFactsTest.
 */

/** Gramática e entrada do segundo exemplo do analisador preditivo e da descida recursiva. */
val GRAMATICA_PREDITIVO_2 = listOf("L -> id R", "R -> , id R | ε")
val ENTRADA_PREDITIVO_2 = listOf("id", ",", "id")

/** Gramática e entrada com dois erros do segundo exemplo da recuperação. */
val GRAMATICA_ERRO_2 = listOf("Prog -> Cmd Prog | ε", "Cmd -> id = Val ;", "Val -> id | num")
val ENTRADA_ERRO_2 = listOf("id", "=", "=", "num", ";", "id", "=", ";")

/** Gramática e entrada do segundo exemplo do empilha-reduz. */
val GRAMATICA_SR_2 = listOf("S -> ( S ) | id")
val ENTRADA_SR_2 = listOf("(", "id", ")")

/** O arquivo .y do primeiro exemplo do Bison. */
val BISON_SPEC_1 = listOf(
    "%token NUM",
    "%left '+' '-'",
    "%left '*' '/'",
    "%%",
    "expr: expr '+' expr { \$\$ = \$1 + \$3; }",
    "    | expr '-' expr { \$\$ = \$1 - \$3; }",
    "    | expr '*' expr { \$\$ = \$1 * \$3; }",
    "    | NUM ;",
)

/** A calculadora com parênteses do "por que existe" do Bison: o motor conta os estados da tabela. */
val BISON_CALC = listOf(
    "%token NUM",
    "%left '+' '-'",
    "%left '*' '/'",
    "%%",
    "expr: expr '+' expr | expr '-' expr",
    "    | expr '*' expr | expr '/' expr",
    "    | '(' expr ')' | NUM ;",
)

/** Segundo exemplo do Bison: só o menos, sem precedência, e depois com o %left. */
val BISON_MENOS = listOf("%token NUM", "%%", "expr: expr '-' expr { \$\$ = \$1 - \$3; }", "    | NUM ;")
val BISON_MENOS_LEFT = listOf("%token NUM", "%left '-'", "%%", "expr: expr '-' expr { \$\$ = \$1 - \$3; }", "    | NUM ;")

/** O aviso do bison 3 para a calculadora sem precedência (sem o [-Wconflicts-sr] do fim, que não cabe). */
const val BISON_AVISO = "calc.y: warning: 1 shift/reduce conflict"

/** Opção de toque do segundo exemplo. */
private fun botao(id: String, t: String, x: Float, y: Float, w: Float = 70f) = Box(id, t, x, y, w, 40f, Look.TOKEN, Tint.YELLOW, size = 15f)

val topDown by lazy {
    listOf(
        // ─────────────────────────────── TABELA LL(1) ───────────────────────────────

        // ─────────────────────────────── PREDITIVO ───────────────────────────────
        episodio(Skill.LL1_PARSING, tx("epparsers_pilha_decide")) {
            val n = 4
            // 1. Por que existe
            cena(tx("pred_l_01"), mood = EpMood.THINK) {
                add(titulo("( id )"))
                add(Box("w0", "(", 110f, 160f, 60f, 46f, Look.TOKEN, Tint.TEAL, size = 20f), Box("w1", "id", 180f, 160f, 60f, 46f, Look.TOKEN, Tint.TEAL, size = 20f))
                add(alvo("w2", 250f, 160f, 60f, 46f))
            }
            pergunta(tx("pred_l_02"), setOf("o1"), setOf("o2", "o3"), tx("pred_l_02_dica"), tx("pred_l_02_ok")) {
                add(botao("o1", ")", 110f, 280f), botao("o2", "(", 180f, 280f), botao("o3", "id", 250f, 280f))
            }
            palavra(tx("pred_l_palavra"), tx("pred_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epparsers_parser_preditivo_tem_pilha_entrada")) {
                clear()
                add(titulo("id + id"))
                // A gramática fica à vista no primeiro exemplo, como no segundo: "olho a tabela" precisa de regra na tela.
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )"), 250f, x = 150f, passo = 25f, size = 14f))
                add(rotulo("rs", tx("sims_pilha"), 74f, 326f), rotulo("ra", tx("epparsers_topo_token_id"), 240f, 180f, Tint.YELLOW, 17f))
                parser(listOf("$", "E"), listOf("id", "+", "id", "$"), n)
                hot("p1", "i0")
            }
            cena(tx("epparsers_topo_nao_terminal_olho_tabela")) {
                parser(listOf("$", "X", "T"), listOf("id", "+", "id", "$"), n)
                text("ra", "E -> T X")
                hot("g0")
            }
            cena(tx("epparsers_agora_t_esta_no_topo")) {
                parser(listOf("$", "X", "id"), listOf("id", "+", "id", "$"), n)
                text("ra", "T -> id")
                hot("p2", "i0", "g2")
            }
            pergunta(
                tx("epparsers_topo_id_token_id_que"),
                setOf("casar"), setOf("expandir"),
                tx("epparsers_terminal_no_topo_igual_ao"),
                tx("epparsers_isso_casou_dois_saem"),
            ) {
                text("ra", tx("epparsers_topo_id_token_id"))
                add(Box("casar", tx("comum_casar_2"), 178f, 360f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 17f), Box("expandir", tx("comum_expandir"), 296f, 360f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena(tx("epparsers_jogo_segue_igual_expande_nao")) {
                drop("casar", "expandir")
                parser(listOf("$", "X"), listOf("+", "id", "$"), n)
                text("ra", tx("epparsers_casou_id"))
                hot()
            }
            cena(tx("epparsers_sobrou_cifrao_na_pilha_cifrao"), mood = EpMood.HAPPY) {
                parser(listOf("$"), listOf("$"), n)
                text("ra", tx("sims_aceita"))
                hot("p0", "i3")
            }
            // 3. A receita
            cena(tx("pred_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("pred_l_r1"), 110f), regra("r2", tx("pred_l_r2"), 166f), regra("r3", tx("pred_l_r3"), 222f))
            }
            pergunta(tx("pred_l_06"), setOf("r2"), setOf("r1", "r3"), tx("pred_l_06_dica"), tx("pred_l_06_ok")) {
                hot()
                add(Box("q", tx("pred_l_q"), 180f, 310f, 220f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("pred_l_07")) { drop("q"); hot("r3") }
            // 4. Segundo exemplo, com perguntas
            val m = ENTRADA_PREDITIVO_2.size + 1
            val fim = ENTRADA_PREDITIVO_2 + "$"
            fun regrasDeR() = add(
                Box("o3", "R -> , id R", 250f, 330f, 160f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f),
                Box("o4", "R -> ε", 250f, 378f, 160f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f),
            )
            cena(tx("pred_l_08")) {
                clear()
                add(titulo("id , id"))
                add(codigo("g", GRAMATICA_PREDITIVO_2, 250f, x = 150f, passo = 25f, size = 14f))
                add(rotulo("rs", tx("sims_pilha"), 74f, 326f), rotulo("ra", tx("pred_l_ra_l"), 240f, 180f, Tint.YELLOW, 17f))
                parser(listOf("$", "L"), fim, m)
                hot("p1", "i0")
            }
            pergunta(tx("pred_l_09"), setOf("o1"), setOf("o2"), tx("pred_l_09_dica"), tx("pred_l_09_ok")) {
                hot("g0")
                add(botao("o1", "id", 210f, 350f), botao("o2", "R", 290f, 350f))
            }
            cena(tx("pred_l_10")) {
                drop("o1", "o2")
                parser(listOf("$", "R"), fim.drop(1), m)
                text("ra", tx("epparsers_casou_id"))
                hot("p1")
            }
            pergunta(tx("pred_l_11"), setOf("o3"), setOf("o4"), tx("pred_l_11_dica"), tx("pred_l_11_ok")) {
                text("ra", tx("pred_l_ra_r"))
                regrasDeR()
                hot("p1", "i1")
            }
            cena(tx("pred_l_12")) {
                drop("o3", "o4")
                parser(listOf("$", "R"), fim.drop(3), m)
                text("ra", tx("pred_l_ra_fim"))
                hot("p1", "i3")
            }
            pergunta(tx("pred_l_13"), setOf("o4"), setOf("o3"), tx("pred_l_13_dica"), tx("pred_l_13_ok")) { regrasDeR() }
            cena(tx("pred_l_14"), mood = EpMood.HAPPY) {
                drop("o3", "o4")
                parser(listOf("$"), listOf("$"), m)
                text("ra", tx("sims_aceita"))
                hot("p0", "i3")
            }
            fecho(Fecho(
                tx("pred_f_af"), true, tx("pred_f_q"), tx("pred_f_dica"), tx("pred_f_ok"), tx("pred_f_af"),
                tx("pred_f_erro2"), tx("pred_f_certo2"), tx("pred_f_fala2"),
                listOf(tx("pred_f_z1"), tx("pred_f_z2"), tx("pred_f_z3")), tx("pred_f_resumo"),
            ))
        },

        // ─────────────────────────────── DESCIDA RECURSIVA ───────────────────────────────
        // DESC_CASA e DESC_MAIN ficam no fim do arquivo.
        episodio(Skill.RECURSIVE_DESCENT, tx("epparsers_metodo_por_regra")) {
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
            // 1. Por que existe
            cena(tx("desc_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("epparsers_tabela_preditiva")))
                add(tabela("m", TABELA_LL_1, 120f, cw = 56f, ch = 42f, size = 12f))
            }
            pergunta(tx("desc_l_01q"), setOf("ml2c1"), (1..3).flatMap { l -> (1..5).map { c -> "ml${l}c$c" } }.toSet() - "ml2c1", tx("desc_l_01q_dica"), tx("desc_l_01q_ok")) { hot() }
            cena(tx("desc_l_02")) {
                add(Box("erro", "M[X, id]", 180f, 330f, 160f, 44f, Look.TOKEN, Tint.RED, size = 15f))
                hot("ml2c1")
            }
            palavra(tx("comum_descida_recursiva").uppercase(), tx("desc_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epparsers_para_escrever_parser_mao_cada")) {
                clear()
                add(titulo(tx("comum_descida_recursiva")))
                // As regras em cima e o código embaixo: cada linha acesa no código acende a regra que a gerou.
                add(codigo("g", GRAMATICA_EXPR, 58f, x = 100f, passo = 22f, size = 14f))
                add(codigo("c", src, 128f, passo = 23f, size = 12f))
            }
            cena(tx("epparsers_regra_pede_t_depois_x")) { hot("c0", "g0") }
            cena(tx("epparsers_terminal_vira_casa_ele_confere")) { hot("c3", "g1") }
            pergunta(
                tx("epparsers_se_token_nao_for_mais"),
                setOf("r1"), setOf("r0"),
                tx("epparsers_nao_fazer_nada_mesmo_que"),
                tx("epparsers_isso_nao_fazer_nada_regra"),
            ) {
                hot("c2", "g1")
                add(Box("r0", "X -> + T X", 110f, 386f, 150f, 44f, Look.TOKEN, Tint.TEAL, size = 15f), Box("r1", "X -> ε", 270f, 386f, 130f, 44f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            cena(tx("epparsers_ifs_usam_first_para_escolher")) {
                drop("r0", "r1")
                hot("c7", "c8", "g2")
            }
            // As duas peças que faltavam (professor do bloco 3): o corpo de casa() e o main que confere o fim.
            cena(tx("desc_l_casa")) {
                clear()
                add(titulo(tx("desc_l_titulo_pecas"), size = 22f))
                add(codigo("k", DESC_CASA, 70f, x = 40f, passo = 24f, size = 13f))
                add(codigo("m", DESC_MAIN, 190f, x = 40f, passo = 24f, size = 13f))
                hot("k1", "k2")
            }
            cena(tx("desc_l_main")) { hot("m1", "m2", "m3") }
            pergunta(tx("desc_l_mainq"), setOf("e2"), setOf("e1", "e3"), tx("desc_l_mainq_dica"), tx("desc_l_mainq_ok")) {
                add(fileira("ent", listOf("id" to Tint.TEAL, "id" to Tint.TEAL, "$" to Tint.NONE), 322f, w = 50f, h = 36f))
                add(botao("e1", tx("sims_aceita"), 80f, 376f, 96f), botao("e2", tx("desc_l_erro"), 180f, 376f, 80f), botao("e3", tx("desc_l_chama_e"), 285f, 376f, 104f))
                hot("m3")
            }
            cena(tx("epparsers_esse_parser_que_trabalho_pratico"), who = PROF, mood = EpMood.HAPPY) { hot() }
            // 3. A receita
            cena(tx("desc_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("desc_l_r1"), 110f), regra("r2", tx("desc_l_r2"), 166f), regra("r3", tx("desc_l_r3"), 222f), regra("r4", tx("desc_l_r4"), 278f))
            }
            pergunta(tx("desc_l_06"), setOf("r2"), setOf("r1", "r3", "r4"), tx("desc_l_06_dica"), tx("desc_l_06_ok")) {
                hot()
                add(codigo("q", listOf("T -> ( E )"), 340f, x = 130f, size = 16f))
            }
            cena(tx("desc_l_07")) { drop("q0"); hot("r4") }
            // 4. Segundo exemplo, com perguntas
            cena(tx("desc_l_08")) {
                clear()
                add(titulo(tx("comum_descida_recursiva")))
                add(codigo("g", GRAMATICA_PREDITIVO_2, 62f, x = 100f, passo = 26f, size = 15f))
                add(codigo("c", listOf("void L() { casa(ID); ___ }", "void R() {", "  if (tok == ___) {", "    casa(','); casa(ID); R();", "  }", "}"), 132f, x = 40f, passo = 27f, size = 14f))
            }
            pergunta(tx("desc_l_09"), setOf("o1"), setOf("o2"), tx("desc_l_09_dica"), tx("desc_l_09_ok")) {
                hot("c0")
                add(botao("o1", "R();", 120f, 330f, 90f), botao("o2", "casa(R);", 240f, 330f, 110f))
            }
            pergunta(tx("desc_l_10"), setOf("p1"), setOf("p2"), tx("desc_l_10_dica"), tx("desc_l_10_ok")) {
                drop("o1", "o2")
                text("c0", "void L() { casa(ID); R(); }")
                hot("c2")
                add(botao("p1", "','", 140f, 330f), botao("p2", "ID", 220f, 330f))
            }
            cena(tx("desc_l_11")) {
                drop("p1", "p2")
                text("c2", "  if (tok == ',') {")
                add(fileira("k", (ENTRADA_PREDITIVO_2 + "$").map { it to Tint.TEAL }, 312f, w = 50f, h = 36f))
                hot("c0", "c2")
            }
            pergunta(tx("desc_l_12"), setOf("n2"), setOf("n1", "n3"), tx("desc_l_12_dica"), tx("desc_l_12_ok")) {
                hot()
                add(botao("n1", "1", 120f, 375f, 54f), botao("n2", "2", 180f, 375f, 54f), botao("n3", "3", 240f, 375f, 54f))
            }
            cena(tx("desc_l_13"), mood = EpMood.HAPPY) {
                drop("n1", "n2", "n3")
                hot("c2", "k3")
            }
            fecho(Fecho(
                tx("desc_f_af"), false, tx("desc_f_q"), tx("desc_f_dica"), tx("desc_f_ok"), tx("desc_f_certo"),
                tx("desc_f_erro2"), tx("desc_f_certo2"), tx("desc_f_fala2"),
                listOf(tx("desc_f_z1"), tx("desc_f_z2"), tx("desc_f_z3")), tx("desc_f_resumo"),
            ))
        },

        // ─────────────────────────────── RECUPERAÇÃO DE ERROS ───────────────────────────────
        episodio(Skill.ERROR_RECOVERY, tx("epparsers_cair_levantar")) {
            // Sem token do FIRST de Val antes do ";": assim o ";" é mesmo onde ele se segura (regras 1 e 3 da receita).
            val itens = listOf("x", "=", "*", ";", "y", "=", "2", ";")
            // 1. Por que existe
            cena(tx("erro_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("erro_l_titulo")))
                add(codigo("c", listOf("x = * ;", "y = 2;", "z = ;"), 130f, x = 110f, passo = 40f, size = 18f))
                hot("c0", "c2")
            }
            pergunta(tx("erro_l_01q"), setOf("c2"), setOf("c0", "c1"), tx("erro_l_01q_dica"), tx("erro_l_01q_ok")) { hot("c0") }
            cena(tx("erro_l_02")) { hot("c2") }
            palavra(tx("erro_l_palavra"), tx("erro_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epparsers_erro_nao_pode_derrubar_compilador")) {
                clear()
                add(titulo(tx("comum_modo_panico")))
                // A mesma gramática do segundo exemplo: o erro só se entende vendo a regra que ele quebra.
                add(codigo("g", GRAMATICA_ERRO_2, 66f, x = 80f, passo = 27f, size = 14f))
                add(fileira("k", itens.map { it to Tint.TEAL }, 190f, w = 34f, h = 42f, gap = 4f))
            }
            cena(tx("epparsers_aqui_parser_trava_depois_igual"), mood = EpMood.SURPRISED) {
                tint("k2", Tint.RED)
                hot("k2", "g2")
                add(rotulo("e", tx("epparsers_erro_aqui"), 110f, 240f, Tint.RED, 17f))
            }
            pergunta(
                tx("epparsers_no_modo_panico_ele_descarta"),
                setOf("k3"), setOf("k4", "k6", "k7"),
                tx("epparsers_procure_token_que_encerra_comando"),
                tx("epparsers_isso_ponto_virgula_sincronizador"),
            ) { hot() }
            cena(tx("epparsers_tudo_ate_ali_vai_fora")) {
                drop("k2", "e")
                tint("k3", Tint.GREEN)
                hot("k4", "g1")
            }
            cena(tx("epparsers_bons_sincronizadores_saem_follow_cada"), who = PROF) { hot() }
            cena(tx("epparsers_assim_compilacao_so_aponta_varios"), mood = EpMood.HAPPY) {}
            // 3. A receita
            cena(tx("erro_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("erro_l_r1"), 110f), regra("r2", tx("erro_l_r2"), 166f), regra("r3", tx("erro_l_r3"), 222f), regra("r4", tx("erro_l_r4"), 278f))
            }
            pergunta(tx("erro_l_06"), setOf("r3"), setOf("r1", "r2", "r4"), tx("erro_l_06_dica"), tx("erro_l_06_ok")) { hot() }
            cena(tx("erro_l_07")) { hot("r4") }
            // 4. Segundo exemplo, com perguntas
            val toks = ENTRADA_ERRO_2 + "$"
            val todos = toks.indices.map { "k$it" }.toSet()
            cena(tx("erro_l_08")) {
                clear()
                add(titulo(tx("comum_modo_panico")))
                add(codigo("g", GRAMATICA_ERRO_2, 66f, x = 80f, passo = 27f, size = 14f))
                add(fileira("k", toks.map { it to Tint.TEAL }, 190f, w = 34f, h = 42f, gap = 4f))
            }
            pergunta(tx("erro_l_09"), setOf("k2"), todos - "k2", tx("erro_l_09_dica"), tx("erro_l_09_ok")) { hot() }
            pergunta(tx("erro_l_10"), setOf("k3"), setOf("k4", "k5", "k6", "k7", "k8"), tx("erro_l_10_dica"), tx("erro_l_10_ok")) {
                tint("k2", Tint.RED)
                add(rotulo("e", tx("epparsers_erro_aqui"), 110f, 236f, Tint.RED, 17f))
                hot("g2")
            }
            cena(tx("erro_l_11")) {
                drop("k2", "e")
                tint("k3", Tint.GREEN)
                hot("k3")
            }
            pergunta(tx("erro_l_12"), setOf("m1"), setOf("m2"), tx("erro_l_12_dica"), tx("erro_l_12_ok")) {
                tint("k7", Tint.RED)
                hot("k7")
                add(botao("m1", tx("erro_l_tira"), 100f, 290f, 140f), botao("m2", tx("erro_l_descarta"), 260f, 290f, 140f))
            }
            cena(tx("erro_l_13"), mood = EpMood.HAPPY) {
                drop("m1", "m2")
                tint("k7", Tint.GREEN)
                hot("k8")
            }
            fecho(Fecho(
                tx("erro_f_af"), false, tx("erro_f_q"), tx("erro_f_dica"), tx("erro_f_ok"), tx("erro_f_certo"),
                tx("erro_f_erro2"), tx("erro_f_certo2"), tx("erro_f_fala2"),
                listOf(tx("erro_f_z1"), tx("erro_f_z2"), tx("erro_f_z3")), tx("erro_f_resumo"),
            ))
        },
    )
}

val bottomUp by lazy {
    listOf(
        // ─────────────────────────────── SHIFT-REDUCE ───────────────────────────────
        episodio(Skill.SHIFT_REDUCE, tx("epparsers_empilha_reduz")) {
            fun esteira(itens: List<Pair<Int, String>>) {
                dropAll("i")
                for ((pos, t) in itens) add(Box("i$pos", t, 40f + pos * 46f, 130f, 40f, 40f, Look.TOKEN, Tint.TEAL, size = 16f))
            }
            // 1. Por que existe
            cena(tx("sr_l_01"), mood = EpMood.THINK) {
                add(titulo("E -> E + n"))
                add(codigo("x", listOf("E", "E + n", "E + n + n", "E + n + n + n", "..."), 80f, x = 120f, passo = 34f, size = 17f))
                hot("x0")
            }
            pergunta(tx("sr_l_02"), setOf("o3"), setOf("o1", "o2"), tx("sr_l_02_dica"), tx("sr_l_02_ok")) {
                add(botao("o1", "1", 90f, 300f, 60f), botao("o2", "2", 170f, 300f, 60f), botao("o3", tx("sr_l_sem_fim"), 270f, 300f, 110f))
            }
            palavra(tx("epparsers_empilha_reduz").uppercase(), tx("sr_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epparsers_agora_gente_monta_arvore_baixo")) {
                clear()
                add(titulo("E -> E + n | n"))
                esteira(listOf(0 to "n", 1 to "+", 2 to "n", 3 to "$"))
                add(rotulo("rp", tx("sims_pilha"), 300f, 372f))
                actor(Actor(190f, 250f, 0.9f))
            }
            cena(tx("epparsers_empilhar_puxar_proximo_token_para")) {
                esteira(listOf(1 to "+", 2 to "n", 3 to "$"))
                add(pilha("p", listOf("n"), 300f, 340f, h = 34f))
            }
            cena(tx("epparsers_no_topo_tem_n_lado")) {
                dropAll("p")
                add(pilha("p", listOf("E"), 300f, 340f, h = 34f, tint = Tint.ORANGE))
                hot("p0")
            }
            cena(tx("epparsers_empilho_mais_duas_vezes")) {
                esteira(listOf(3 to "$"))
                dropAll("p")
                add(Box("p0", "E", 300f, 340f, 70f, 34f, Look.TOKEN, Tint.ORANGE))
                add(pilha("q", listOf("+", "n"), 300f, 303f, h = 34f))
                hot()
            }
            pergunta(
                tx("epparsers_pilha_tem_mais_n_empilho"),
                setOf("red"), setOf("emp"),
                tx("epparsers_olhe_topo_mais_n_lado"),
                tx("epparsers_isso_handle_no_topo_entao"),
            ) {
                add(Box("emp", tx("comum_empilhar_2"), 90f, 300f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 16f), Box("red", tx("comum_reduzir"), 90f, 356f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 16f))
                hot("p0", "q0", "q1")
            }
            cena(tx("epparsers_sobrou_so_entrada_acabou_frase"), mood = EpMood.HAPPY) {
                drop("emp", "red")
                dropAll("q")
                hot("p0")
            }
            cena(tx("epparsers_lidas_tras_para_frente_reducoes"), who = PROF) { actor(null) }
            // 3. A receita
            cena(tx("sr_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("sr_l_r1"), 110f), regra("r2", tx("sr_l_r2"), 166f), regra("r3", tx("sr_l_r3"), 222f))
            }
            pergunta(tx("sr_l_06"), setOf("r3"), setOf("r1", "r2"), tx("sr_l_06_dica"), tx("sr_l_06_ok")) {
                hot()
                add(Box("q", "E   |   $", 180f, 310f, 160f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f))
            }
            cena(tx("sr_l_07")) { drop("q"); hot("r2") }
            // 4. Segundo exemplo, com perguntas
            fun botoes() = add(
                Box("emp", tx("comum_empilhar_2"), 90f, 300f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 16f),
                Box("red", tx("comum_reduzir"), 90f, 356f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 16f),
            )
            cena(tx("sr_l_08")) {
                clear()
                add(titulo(GRAMATICA_SR_2.single()))
                esteira((ENTRADA_SR_2 + "$").mapIndexed { i, t -> i to t })
                add(rotulo("rp", tx("sims_pilha"), 300f, 372f))
            }
            cena(tx("sr_l_09")) {
                esteira(listOf(2 to ")", 3 to "$"))
                add(pilha("p", listOf("(", "id"), 300f, 340f, h = 34f))
                hot("p1")
            }
            pergunta(tx("sr_l_10"), setOf("red"), setOf("emp"), tx("sr_l_10_dica"), tx("sr_l_10_ok")) {
                botoes()
                hot("p1")
            }
            cena(tx("sr_l_11")) {
                drop("emp", "red")
                dropAll("p")
                add(Box("p0", "(", 300f, 340f, 70f, 34f, Look.TOKEN, Tint.TEAL), Box("p1", "S", 300f, 303f, 70f, 34f, Look.TOKEN, Tint.ORANGE))
                hot("p1")
            }
            pergunta(tx("sr_l_12"), setOf("emp"), setOf("red"), tx("sr_l_12_dica"), tx("sr_l_12_ok")) {
                botoes()
                hot("p0", "p1")
            }
            pergunta(tx("sr_l_13"), setOf("p0", "p1", "p2"), setOf("i3"), tx("sr_l_13_dica"), tx("sr_l_13_ok")) {
                drop("emp", "red")
                esteira(listOf(3 to "$"))
                add(Box("p2", ")", 300f, 266f, 70f, 34f, Look.TOKEN, Tint.TEAL))
                hot()
            }
            cena(tx("sr_l_14"), mood = EpMood.HAPPY) {
                dropAll("p")
                add(Box("p0", "S", 300f, 340f, 70f, 34f, Look.TOKEN, Tint.ORANGE))
                hot("p0", "i3")
            }
            fecho(Fecho(
                tx("sr_f_af"), false, tx("sr_f_q"), tx("sr_f_dica"), tx("sr_f_ok"), tx("sr_f_certo"),
                tx("sr_f_erro2"), tx("sr_f_certo2"), tx("sr_f_fala2"),
                listOf(tx("sr_f_z1"), tx("sr_f_z2"), tx("sr_f_z3")), tx("sr_f_resumo"),
            ))
        },

        // ─────────────────────────────── ITENS LR(0) ───────────────────────────────

        // ─────────────────────────────── SLR ───────────────────────────────

        // ─────────────────────────────── LALR ───────────────────────────────

        // ─────────────────────────────── BISON ───────────────────────────────
        episodio(Skill.YACC_BISON, tx("epparsers_fabrica_parsers")) {
            // 1. Por que existe
            cena(tx("bison_l_01"), mood = EpMood.THINK) {
                add(titulo("LALR(1)"))
                add(codigo("g", BISON_CALC, 66f, x = 20f, passo = 26f, size = 12f))
                add(Box("n", tx("bison_l_estados"), 180f, 270f, 240f, 44f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            pergunta(tx("bison_l_02"), setOf("o2"), setOf("o1", "o3"), tx("bison_l_02_dica"), tx("bison_l_02_ok")) {
                hot("n")
                add(botao("o1", "22", 100f, 350f), botao("o2", "112", 180f, 350f), botao("o3", "1000", 260f, 350f))
            }
            palavra("BISON", tx("bison_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epparsers_bison_le_gramatica_escreve_parser")) {
                clear()
                add(titulo(tx("epparsers_arquivo_y")))
                add(codigo("s", BISON_SPEC_1, 66f, passo = 28f, size = 13f))
            }
            cena(tx("epparsers_cada_regra_tem_acao_cifrao")) { hot("s4") }
            cena(tx("epparsers_cifrao_cifrao_resultado_regra_ele")) { hot("s5") }
            cena(tx("epparsers_essa_gramatica_ambigua_quem_resolve"), mood = EpMood.THINK) { hot("s1", "s2") }
            pergunta(
                tx("epparsers_quanto_mais_abaixo_no_arquivo"),
                setOf("s2"), setOf("s1"),
                tx("epparsers_procure_linha_mais_perto_das"),
                tx("epparsers_isso_vezes_esta_mais_abaixo"),
            ) { hot() }
            cena(tx("epparsers_left_diz_que_contas_iguais")) {
                add(Box("e1", "1 + 2 * 3 = 7", 180f, 318f, 200f, 42f, Look.TOKEN, Tint.GREEN, size = 16f))
                add(Box("e2", "5 - 2 - 1 = 2", 180f, 366f, 200f, 42f, Look.TOKEN, Tint.TEAL, size = 16f))
                hot("s1")
            }
            cena(tx("epparsers_sem_nenhuma_linha_conflito_na"), mood = EpMood.HAPPY) { hot() }
            // 3. A receita
            cena(tx("bison_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("bison_l_r1"), 110f), regra("r2", tx("bison_l_r2"), 166f), regra("r3", tx("bison_l_r3"), 222f))
            }
            pergunta(tx("bison_l_06"), setOf("r1"), setOf("r2", "r3"), tx("bison_l_06_dica"), tx("bison_l_06_ok")) {
                hot()
                add(codigo("q", listOf("%left '-'"), 310f, x = 120f, size = 16f))
            }
            cena(tx("bison_l_07")) { drop("q0"); hot("r3") }
            // 4. Segundo exemplo, com perguntas
            fun conta() = add(
                Box("conta", "5 - 2 - 1", 180f, 300f, 160f, 40f, Look.TOKEN, Tint.TEAL, size = 16f),
                botao("v2", "2", 140f, 360f, 60f), botao("v4", "4", 220f, 360f, 60f),
            )
            cena(tx("bison_l_08")) {
                clear()
                add(titulo("calc.y"))
                add(codigo("s", BISON_MENOS, 70f, x = 20f, passo = 28f, size = 13f))
            }
            pergunta(tx("bison_l_09"), setOf("n1"), setOf("n0", "n2"), tx("bison_l_09_dica"), tx("bison_l_09_ok")) {
                add(botao("n0", "0", 110f, 230f, 60f), botao("n1", "1", 180f, 230f, 60f), botao("n2", "2", 250f, 230f, 60f))
            }
            cena(tx("bison_l_10")) {
                drop("n0", "n1", "n2")
                add(codigo("o", listOf("\$ bison calc.y", BISON_AVISO), 200f, x = 20f, passo = 26f, size = 12f))
                hot("o1")
            }
            pergunta(tx("bison_l_11"), setOf("v4"), setOf("v2"), tx("bison_l_11_dica"), tx("bison_l_11_ok")) { conta() }
            cena(tx("bison_l_12")) {
                drop("v2", "v4", "conta")
                dropAll("o")
                dropAll("s")
                add(codigo("s", BISON_MENOS_LEFT, 70f, x = 20f, passo = 28f, size = 13f))
                hot("s1")
            }
            pergunta(tx("bison_l_13"), setOf("v2"), setOf("v4"), tx("bison_l_13_dica"), tx("bison_l_13_ok")) { conta() }
            cena(tx("bison_l_14"), mood = EpMood.HAPPY) {
                drop("v4")
                tint("v2", Tint.GREEN)
                add(codigo("o", listOf("\$ bison calc.y"), 230f, x = 20f, passo = 26f, size = 12f))
                hot("v2")
            }
            fecho(Fecho(
                tx("bison_f_af"), true, tx("bison_f_q"), tx("bison_f_dica"), tx("bison_f_ok"), tx("bison_f_af"),
                tx("bison_f_erro2"), tx("bison_f_certo2"), tx("bison_f_fala2"),
                listOf(tx("bison_f_z1"), tx("bison_f_z2"), tx("bison_f_z3")), tx("bison_f_resumo"),
            ))
        },
    )
}

/** O corpo de casa(): confere o token e lê o próximo. */
internal val DESC_CASA = listOf("void casa(int t) {", "  if (tok == t) tok = proximo();", "  else erro();", "}")
/** O main da descida recursiva: lê o primeiro token, chama E e confere que a entrada acabou. */
internal val DESC_MAIN = listOf("int main() {", "  tok = proximo();", "  E();", "  if (tok != '$') erro();", "}")
