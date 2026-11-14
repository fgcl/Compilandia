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
import com.compilandia.textos.tx

/** A gramática da pegadinha do FOLLOW: FOLLOW(A) = { d, c }, porque B pode sumir. */
val GRAMATICA_PEG_FOLLOW = listOf("S -> A B c", "A -> a", "B -> d | ε")

/** Pegadinhas de prova: do FIRST ao Bison. */
val pegadinhasMeio by lazy {
    listOf(
        episodio(Skill.FIRST, tx("eppegadinhas2_first_regra_inteira"), PEGADINHA) {
            cena(tx("eppegadinhas2_comeca_com_pode_sumir_b")) {
                add(titulo(tx("eppegadinhas2_first")))
                add(codigo("g", listOf("S -> A B c", "A -> a | ε", "B -> b | ε"), 70f, x = 116f, passo = 28f, size = 16f))
            }
            cena(tx("eppegadinhas2_se_nao_sumir_abre_frase")) {
                add(fileira("f", listOf("a" to Tint.TEAL, "b" to Tint.TEAL, "c" to Tint.TEAL, "ε" to Tint.PINK), 240f, w = 64f, h = 54f, size = 20f))
                hot("g1")
            }
            pergunta(
                tx("eppegadinhas2_toque_em_todos_simbolos_que"),
                setOf("f0", "f1", "f2"), setOf("f3"),
                tx("eppegadinhas2_b_podem_sumir_mas_c"),
                tx("eppegadinhas2_isso_b_c_epsilon_fica"),
            ) { hot() }
            cena(tx("eppegadinhas2_erro_comum_parar_no_primeiro"), who = PROF) { drop("f3") }
            cena(tx("eppegadinhas2_so_entra_epsilon_se_tudo"), mood = EpMood.HAPPY) {}
        },

        // B pode sumir, e isso muda o FOLLOW de A: entra o d de B e também o c que vem depois (professor do bloco 3).
        episodio(Skill.FOLLOW, tx("eppegadinhas2_epsilon_que_muda_follow"), PEGADINHA) {
            cena(tx("eppegadinhas2_b_pode_sumir_isso_muda")) {
                add(titulo(tx("peg_follow_titulo")))
                add(codigo("g", GRAMATICA_PEG_FOLLOW, 70f, x = 120f, passo = 28f, size = 16f))
            }
            cena(tx("peg_follow_onde_a")) {
                hot("g0")
                add(Box("p", tx("peg_follow_depois_de_a"), 180f, 180f, 300f, 40f, Look.CELL, Tint.YELLOW, size = 15f))
            }
            pergunta(tx("peg_follow_q"), setOf("t0", "t1"), setOf("t2", "t3"), tx("peg_follow_q_dica"), tx("peg_follow_q_ok")) {
                hot("g2")
                add(fileira("t", listOf("d" to Tint.TEAL, "c" to Tint.TEAL, "ε" to Tint.PINK, "$" to Tint.TEAL), 260f, w = 64f, h = 54f, size = 20f))
            }
            cena(tx("peg_follow_erro"), who = PROF) { drop("t2", "t3"); hot("t1") }
            palavra("FOLLOW", tx("eppegadinhas2_first_olha_para_dentro_follow"), who = PROF, mood = EpMood.HAPPY)
        },

        episodio(Skill.LL1_TABLE, tx("eppegadinhas2_duas_regras_casa_so"), PEGADINHA) {
            val base = listOf(listOf("", "a", "b", "$"), listOf("S", "", "", ""), listOf("A", "", "", ""))
            cena(tx("eppegadinhas2_esta_gramatica_parece_inocente_vamos")) {
                add(titulo("S -> A b   A -> b | ε", size = 20f))
                add(tabela("m", base, 130f, cw = 80f, ch = 56f, size = 15f))
            }
            cena(tx("eppegadinhas2_pode_virar_b_essa_regra")) { text("ml2c2", "b"); hot("ml2c2") }
            cena(tx("eppegadinhas2_tambem_pode_sumir_vai_nas")) { hot("ml2c2") }
            pergunta(
                tx("eppegadinhas2_toque_na_casa_que_recebeu"),
                setOf("ml2c2"), setOf("ml1c2", "ml2c1", "ml2c3"),
                tx("eppegadinhas2_first_primeira_opcao_follow_tem"),
                tx("eppegadinhas2_isso_com_b_na_entrada"),
            ) { hot() }
            cena(tx("eppegadinhas2_duas_regras_na_casa_gramatica"), who = PROF) {
                text("ml2c2", tx("eppegadinhas2_b")); tint("ml2c2", Tint.RED); hot("ml2c2")
            }
            cena(tx("eppegadinhas2_na_prova_confira_sempre_first"), mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.RECURSIVE_DESCENT, tx("eppegadinhas2_quem_avanca_token"), PEGADINHA) {
            cena(tx("eppegadinhas2_na_descida_recursiva_cada_regra")) {
                add(titulo("void F()"))
                add(codigo("c", listOf("if (tk == ID) casa(ID);", "else if (tk == ABRE) {", "  casa(ABRE);", "  E();", "  casa(FECHA);", "} else erro();"), 74f, x = 34f, passo = 30f, size = 14f))
            }
            cena(tx("eppegadinhas2_funcao_so_olha_token_atual")) { hot("c0", "c1") }
            pergunta(
                tx("eppegadinhas2_em_qual_linha_entrada_anda"),
                setOf("c0"), setOf("c1", "c5"),
                tx("eppegadinhas2_procure_funcao_que_confere_token"),
                tx("eppegadinhas2_isso_quem_avanca_casa"),
            ) { hot() }
            cena(tx("eppegadinhas2_chamar_nao_avanca_nada_por")) { hot("c3") }
            cena(tx("eppegadinhas2_se_ninguem_chamar_casa_programa"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.ERROR_RECOVERY, tx("eppegadinhas2_parar_ou_seguir"), PEGADINHA) {
            cena(tx("eppegadinhas2_compilador_que_para_no_primeiro")) {
                add(titulo(tx("eppegadinhas2_dez_erros_dez_compilacoes")))
                add(fileira("t", listOf("id" to Tint.TEAL, "=" to Tint.ORANGE, "=" to Tint.RED, ";" to Tint.BLUE, "id" to Tint.TEAL, "=" to Tint.ORANGE), 150f, w = 50f))
            }
            // Nada do FIRST de Val antes do ";". Com um num ali, o parser recomeçaria no num, e não no ";".
            cena(tx("eppegadinhas2_no_modo_panico_ele_joga")) { hot("t2") }
            pergunta(
                tx("eppegadinhas2_qual_token_bom_ponto_recomeco"),
                setOf("t3"), setOf("t0", "t4"),
                tx("eppegadinhas2_procure_token_que_costuma_fechar"),
                tx("eppegadinhas2_isso_ponto_virgula_marca_fim"),
            ) { hot() }
            cena(tx("eppegadinhas2_recuperar_nao_conserta_programa_so"), who = PROF) {
                dropAll("t")
                add(fileira("t", listOf(";" to Tint.BLUE, "id" to Tint.TEAL), 150f, w = 60f))
            }
            cena(tx("eppegadinhas2_depois_erro_compilador_nao_gera"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.SHIFT_REDUCE, tx("eppegadinhas2_reduzir_cedo_demais"), PEGADINHA) {
            cena(tx("eppegadinhas2_na_pilha_temos_mais_t")) {
                add(titulo(tx("eppegadinhas2_t_vem"), size = 24f))
                add(pilha("p", listOf("E", "+", "T"), 240f, 300f, w = 92f, h = 40f))
                add(codigo("g", listOf("E -> E + T | T", "T -> T * F | F", "F -> id"), 80f, x = 40f, passo = 26f, size = 13f))
                add(Box("in", "*", 90f, 240f, 56f, 50f, Look.TOKEN, Tint.YELLOW, size = 20f))
            }
            pergunta(
                tx("eppegadinhas2_maquina_reduz_mais_t_ou"),
                setOf("sobe"), setOf("red"),
                tx("peg_handle_dica"),
                tx("peg_handle_ok"),
            ) {
                add(alvo("sobe", 240f, 174f, 92f, 40f, "*"))
                add(Box("red", tx("palco_reduzir"), 90f, 320f, 110f, 44f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            // Reduzir cedo não troca a ordem da conta: deixa E com * na entrada, e nenhuma regra continua (professor do bloco 4).
            cena(tx("peg_handle_beco"), mood = EpMood.SURPRISED) {
                drop("sobe", "red"); dropAll("p")
                add(pilha("p", listOf("E"), 240f, 300f, w = 92f, h = 40f))
                add(Box("x", tx("peg_handle_nenhuma"), 180f, 370f, 230f, 40f, Look.TOKEN, Tint.RED, size = 14f))
                hot("in", "g0")
            }
            cena(tx("eppegadinhas2_quem_decide_que_vem_na")) {
                drop("in", "x")
                add(pilha("p", listOf("E", "+", "T", "*"), 240f, 300f, w = 92f, h = 40f))
            }
            palavra("HANDLE", tx("eppegadinhas2_handle_pedaco_certo_reduzir_agora"), who = PROF, mood = EpMood.HAPPY)
            cena(tx("peg_handle_fim"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.LR0_ITEMS, tx("eppegadinhas2_fecho_que_puxa_regras"), PEGADINHA) {
            cena(tx("eppegadinhas2_ponto_marca_ate_onde_regra")) {
                add(titulo(tx("comum_fecho")))
                add(Box("i0", "E -> . E + n", 180f, 110f, 200f, 40f, Look.CELL, Tint.YELLOW, size = 16f))
            }
            cena(tx("eppegadinhas2_ponto_esta_antes_entao_posso")) { hot("i0") }
            pergunta(
                tx("eppegadinhas2_quem_entra_no_fecho_toque"),
                setOf("a", "b"), setOf("c"),
                tx("eppegadinhas2_entram_regras_todas_com_ponto"),
                tx("eppegadinhas2_isso_fecho_puxa_regras_com"),
            ) {
                hot()
                add(Box("a", "E -> . E + n", 180f, 180f, 200f, 40f, Look.CELL, size = 16f))
                add(Box("b", "E -> . n", 180f, 230f, 200f, 40f, Look.CELL, size = 16f))
                add(Box("c", "E -> n .", 180f, 280f, 200f, 40f, Look.CELL, size = 16f))
            }
            cena(tx("eppegadinhas2_item_com_ponto_no_fim"), who = PROF) { drop("a"); tint("c", Tint.ORANGE); hot("c") }
            palavra("CLOSURE", tx("eppegadinhas2_ponto_antes_nao_terminal_puxa"), mood = EpMood.HAPPY)
        },

        episodio(Skill.SLR_TABLE, tx("eppegadinhas2_conflito_nao_ambiguidade"), PEGADINHA) {
            cena(tx("eppegadinhas2_esta_gramatica_ponteiros_nao_ambigua")) {
                add(titulo(tx("eppegadinhas2_conflito_sem_ambiguidade"), size = 22f))
                add(codigo("g", listOf("S -> L = R | R", "L -> * R | id", "R -> L"), 70f, x = 110f, passo = 28f, size = 16f))
            }
            cena(tx("eppegadinhas2_depois_ler_l_esse_estado")) {
                add(Box("a", "S -> L . = R", 180f, 190f, 210f, 40f, Look.CELL, Tint.TEAL, size = 15f))
                add(Box("b", "R -> L .", 180f, 240f, 210f, 40f, Look.CELL, Tint.ORANGE, size = 15f))
            }
            pergunta(
                tx("eppegadinhas2_igual_esta_no_follow_r"),
                setOf("sr"), setOf("rr"),
                tx("eppegadinhas2_ideia_empilha_igual_outra_reduz"),
                tx("eppegadinhas2_isso_empilhar_ou_reduzir_conflito"),
            ) {
                add(Box("sr", "shift reduce", 106f, 310f, 148f, 50f, Look.TOKEN, Tint.RED, size = 15f))
                add(Box("rr", "reduce reduce", 262f, 310f, 148f, 50f, Look.TOKEN, Tint.NONE, size = 15f))
            }
            cena(tx("eppegadinhas2_slr_olha_so_follow_que"), who = PROF) { drop("sr", "rr") }
            palavra(tx("comum_conflito_maiusc"), tx("eppegadinhas2_conflito_diz_que_tecnica_nao"), mood = EpMood.HAPPY)
        },

        episodio(Skill.LALR, tx("eppegadinhas2_juntar_estados_tem_preco"), PEGADINHA) {
            cena(tx("eppegadinhas2_lr_1_tem_muitos_estados")) {
                add(titulo(tx("eppegadinhas2_juntar_estados")))
                add(Box("a", tx("palco_item_com", "A -> c .", "d"), 100f, 150f, 150f, 46f, Look.CELL, Tint.TEAL, size = 13f))
                add(Box("b", tx("palco_item_com", "B -> c .", "e"), 100f, 206f, 150f, 46f, Look.CELL, Tint.TEAL, size = 13f))
                add(Box("c", tx("palco_item_com", "A -> c .", "e"), 260f, 150f, 150f, 46f, Look.CELL, Tint.ORANGE, size = 13f))
                add(Box("d", tx("palco_item_com", "B -> c .", "d"), 260f, 206f, 150f, 46f, Look.CELL, Tint.ORANGE, size = 13f))
            }
            cena(tx("eppegadinhas2_separados_cada_estado_sabe_que")) { hot("a", "b") }
            cena(tx("eppegadinhas2_miolo_igual_entao_lalr_junta")) {
                move("c", 180f, 150f); move("a", 180f, 150f)
                move("d", 180f, 206f); move("b", 180f, 206f)
            }
            pergunta(
                tx("eppegadinhas2_depois_juntar_com_d_na"),
                setOf("x", "y"), setOf("z"),
                tx("eppegadinhas2_junte_listas_duas_regras_ficaram"),
                tx("eppegadinhas2_isso_duas_reducoes_com_mesmo"),
            ) {
                drop("a", "b", "c", "d")
                add(Box("x", tx("palco_item_com", "A -> c .", "d, e"), 180f, 150f, 230f, 46f, Look.CELL, Tint.RED, size = 14f))
                add(Box("y", tx("palco_item_com", "B -> c .", "d, e"), 180f, 206f, 230f, 46f, Look.CELL, Tint.RED, size = 14f))
                add(Box("z", "S -> a . A d", 180f, 262f, 230f, 46f, Look.CELL, size = 14f))
            }
            cena(tx("eppegadinhas2_essa_unica_briga_que_lalr"), who = PROF, mood = EpMood.HAPPY) { drop("z") }
        },

        episodio(Skill.YACC_BISON, tx("eppegadinhas2_quem_fica_embaixo_manda"), PEGADINHA) {
            cena(tx("eppegadinhas2_esta_especificacao_tem_dois_left")) {
                add(titulo("2 + 3 * 4"))
                add(codigo("c", listOf("%left '*'", "%left '+'"), 90f, x = 120f, passo = 32f, size = 18f))
            }
            cena(tx("eppegadinhas2_quanto_mais_embaixo_no_arquivo")) { hot("c1") }
            pergunta(
                tx("eppegadinhas2_com_essa_ordem_quanto_dois"),
                setOf("v20"), setOf("v14"),
                tx("eppegadinhas2_quem_liga_mais_forte_roda"),
                tx("eppegadinhas2_isso_vinte_mais_rodou_primeiro"),
            ) {
                hot()
                add(Box("v14", "14", 110f, 230f, 100f, 60f, Look.TOKEN, Tint.TEAL, size = 22f))
                add(Box("v20", "20", 250f, 230f, 100f, 60f, Look.TOKEN, Tint.ORANGE, size = 22f))
            }
            cena(tx("eppegadinhas2_troque_linhas_mais_em_cima")) {
                drop("v20", "v14")
                text("c0", "%left '+'"); text("c1", "%left '*'")
                add(Box("v", "14", 180f, 230f, 100f, 60f, Look.TOKEN, Tint.GREEN, size = 22f))
                hot("c1")
            }
            palavra(tx("eppegadinhas2_precedencia"), tx("eppegadinhas2_pegadinha_achar_que_primeiro_manda"), who = PROF, mood = EpMood.HAPPY)
        },

        // %prec no menos unário (professor do bloco 4): a regra herda a força do último token, e o %prec troca.
        episodio(Skill.YACC_BISON, tx("peg_un_titulo"), PEGADINHA) {
            cena(tx("peg_un_01")) {
                add(titulo("- 2 * 3"))
                add(codigo("s", BISON_UNARIO, 64f, x = 20f, passo = 26f, size = 12f))
                hot("s6")
            }
            pergunta(tx("peg_un_02"), setOf("u1"), setOf("u2", "u3"), tx("peg_un_02_dica"), tx("peg_un_02_ok")) {
                add(Box("u2", tx("peg_un_o2"), 180f, 290f, 280f, 38f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("u1", tx("peg_un_o1"), 180f, 336f, 280f, 38f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("u3", tx("peg_un_o3"), 180f, 382f, 280f, 38f, Look.TOKEN, Tint.YELLOW, size = 14f))
            }
            cena(tx("peg_un_03")) {
                drop("u1", "u2", "u3"); dropAll("s")
                add(codigo("s", BISON_UNARIO_PREC, 64f, x = 20f, passo = 26f, size = 12f))
                hot("s3", "s7")
            }
            pergunta(tx("peg_un_04"), setOf("m"), setOf("v"), tx("peg_un_04_dica"), tx("peg_un_04_ok")) {
                add(Box("m", "- 2", 120f, 330f, 100f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f))
                add(Box("v", "2 * 3", 240f, 330f, 100f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f))
            }
            cena(tx("peg_un_05"), mood = EpMood.THINK) { drop("v"); tint("m", Tint.GREEN); hot("s7") }
            cena(tx("peg_un_06"), who = PROF, mood = EpMood.HAPPY) { hot("s3", "s7") }
        },
    )
}

/** Menos unário sem %prec (a regra fica com a força do menos) e com %prec UMINUS. Conferidas em AulasConsertadasTest. */
val BISON_UNARIO = listOf(
    "%token NUM", "%left '-'", "%left '*'", "%%",
    "expr: expr '-' expr { \$\$ = \$1 - \$3; }", "    | expr '*' expr { \$\$ = \$1 * \$3; }",
    "    | '-' expr { \$\$ = -\$2; }", "    | NUM ;",
)
val BISON_UNARIO_PREC = listOf(
    "%token NUM", "%left '-'", "%left '*'", "%left UMINUS", "%%",
    "expr: expr '-' expr { \$\$ = \$1 - \$3; }", "    | expr '*' expr { \$\$ = \$1 * \$3; }",
    "    | '-' expr %prec UMINUS { \$\$ = -\$2; }", "    | NUM ;",
)

@Suppress("unused") private val keepActor = Actor(0f, 0f)
