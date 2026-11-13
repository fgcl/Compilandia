package com.compilandia.content.episodes

import com.compilandia.content.fecho
import com.compilandia.content.Fecho
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRTable
import com.compilandia.engine.isComplete
import com.compilandia.engine.ParseNode
import com.compilandia.engine.Mips
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Aulas básicas longas, segunda leva (A2). Mesmo roteiro da primeira: por que existe, primeiro exemplo
 * (as cenas boas da aula curta), a receita à vista e um segundo exemplo em que a pessoa calcula.
 * Os fatos do segundo exemplo são conferidos contra o motor em EpisodeFactsTest.
 */

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

/** Gramática do segundo exemplo da tabela LL(1). */
val GRAMATICA_LL1_2 = listOf("L -> id R", "R -> , id R | ε")

/** Caso difícil da tabela: S -> A B não tem ε escrito, mas some inteira, então vai também no FOLLOW de S. */
val GRAMATICA_LL1_3 = listOf("S -> A B", "A -> a | ε", "B -> b | ε")
/** Conflito FIRST × FIRST: as duas regras de S começam com a. A fatoração da Ponte conserta. */
val GRAMATICA_LL1_4 = listOf("S -> a b | a c")

/** A tabela LL(1) tirada do motor: cabeçalho com os terminais e o $, depois uma linha por não-terminal. */
internal fun tabelaLl1DaAula(gram: List<String>): List<List<String>> {
    val t = LL1Table(Grammar.parse(gram.joinToString("\n")))
    return listOf(listOf("") + t.columns) + t.grammar.nonTerminals.map { nt ->
        listOf(nt) + t.columns.map { c -> t.cell(nt, c).joinToString(" | ") { it.rhsText() } }
    }
}

/**
 * AFD do livro para (a|b)*abb, estados A a E (0 a 4), só E final. A minimização leva três rodadas:
 * E com todos; depois D com A, B e C; depois A com B e B com C. A e C nunca caem.
 */
val AFD_LIVRO = com.compilandia.engine.DFA(
    5, 0, setOf(4), listOf('a', 'b'),
    mapOf(
        (0 to 'a') to 1, (0 to 'b') to 2, (1 to 'a') to 1, (1 to 'b') to 3, (2 to 'a') to 1, (2 to 'b') to 2,
        (3 to 'a') to 1, (3 to 'b') to 4, (4 to 'a') to 1, (4 to 'b') to 2,
    ),
)

/** Gramática do segundo exemplo de LR(0) e SLR. */
val GRAMATICA_LR_2 = listOf("S -> ( S ) | id")

val aulasLongas2 by lazy {
    listOf(
        episodio(Skill.LL1_TABLE, tx("epparsers_tabela_decisoes")) {
            val base = listOf(
                listOf("", "id", "+", "(", ")", "$"),
                listOf("E", "", "", "", "", ""),
                listOf("X", "", "", "", "", ""),
                listOf("T", "", "", "", "", ""),
            )
            cena(tx("ll_l_01"), mood = EpMood.THINK) { escolha("X", "+", "X -> + T X", "X -> ε", GRAMATICA_EXPR, 1) }
            pergunta(tx("ll_l_01q"), setOf("ra"), setOf("rb"), tx("ll_l_01q_dica"), tx("ll_l_01q_ok")) { hot("g1") }
            cena(tx("ll_l_02")) { hot("ra", "rb", "g1") }
            palavra("LL(1)", tx("ll_l_03"))
            cena(tx("epparsers_tabela_ll_1_junta_tudo")) {
                clear()
                add(titulo(tx("epparsers_tabela_preditiva")))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )"), 60f, x = 90f, passo = 26f, size = 15f))
                add(tabela("m", base, 170f, cw = 56f, ch = 42f, size = 12f))
            }
            cena(tx("epparsers_regra_comeca_com_id_ou")) {
                hot("g0", "ml1c1", "ml1c3")
                text("ml1c1", "T X"); text("ml1c3", "T X")
            }
            cena(tx("epparsers_regra_x_que_comeca_com")) {
                hot("g1", "ml2c2")
                text("ml2c2", "+ T X")
            }
            pergunta(
                tx("epparsers_x_tambem_pode_sumir_essa"),
                setOf("ml2c4", "ml2c5"), setOf("ml2c1", "ml2c3"),
                tx("epparsers_follow_x_fecha_parentese_cifrao"),
                tx("epparsers_isso_epsilon_vai_nas_colunas"),
            ) { hot("g1") }
            cena(tx("epparsers_falta_t_id_na_coluna")) {
                text("ml2c4", "ε"); text("ml2c5", "ε")
                text("ml3c1", "id"); text("ml3c3", tx("comum_e"))
                hot("ml3c1", "ml3c3")
            }
            cena(tx("ll_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("ll_l_r1"), 110f), regra("r2", tx("ll_l_r2"), 166f), regra("r3", tx("ll_l_r3"), 222f))
            }
            pergunta(tx("ll_l_06"), setOf("r2"), setOf("r1", "r3"), tx("ll_l_06_dica"), tx("ll_l_06_ok")) {
                hot()
                add(codigo("q", listOf(tx("palco_vai_em")), 300f, x = 70f, size = 15f))
            }
            cena(tx("epparsers_se_casa_receber_duas_regras"), who = PROF) { drop("q"); hot("r3") }
            cena(tx("ll_l_08")) {
                clear()
                add(titulo("LL(1)"))
                add(codigo("g", listOf("L -> id R", "R -> , id R | ε"), 66f, x = 100f, passo = 30f, size = 16f))
                add(tabela("m", listOf(listOf("", "id", ",", "$"), listOf("L", "", "", ""), listOf("R", "", "", "")), 180f, cw = 76f, ch = 50f, size = 14f))
            }
            pergunta(tx("ll_l_09"), setOf("ml1c1"), setOf("ml1c2", "ml1c3"), tx("ll_l_09_dica"), tx("ll_l_09_ok")) {
                hot("g0")
            }
            pergunta(tx("ll_l_10"), setOf("ml2c2"), setOf("ml2c1", "ml2c3"), tx("ll_l_10_dica"), tx("ll_l_10_ok")) {
                text("ml1c1", "id R"); hot("g1")
            }
            pergunta(tx("ll_l_11"), setOf("ml2c3"), setOf("ml2c1"), tx("ll_l_11_dica"), tx("ll_l_11_ok")) {
                text("ml2c2", ", id R"); hot("g1")
            }
            cena(tx("ll_l_12"), mood = EpMood.HAPPY) {
                text("ml2c3", "ε")
                hot("ml1c1", "ml2c2", "ml2c3")
            }
            // 5. O caso difícil: regra anulável sem ε escrito, e depois duas regras na mesma casa
            val cheia3 = tabelaLl1DaAula(GRAMATICA_LL1_3)
            cena(tx("ll_l_13")) {
                clear()
                add(titulo("LL(1)"))
                add(codigo("g", GRAMATICA_LL1_3, 60f, x = 100f, passo = 26f, size = 15f))
                add(rotulo("fs", "FOLLOW(S) = { $ }", 180f, 150f, Tint.YELLOW))
                add(tabela("m", cheia3.map { l -> l.mapIndexed { j, s -> if (j == 0 || l === cheia3[0]) s else "" } }, 200f, cw = 76f, ch = 46f, size = 14f))
                hot("g0")
            }
            pergunta(tx("ll_l_14"), setOf("ml1c1", "ml1c2", "ml1c3"), (2..3).flatMap { l -> (1..3).map { c -> "ml${l}c$c" } }.toSet(), tx("ll_l_14_dica"), tx("ll_l_14_ok")) {
                hot("g0", "fs")
            }
            cena(tx("ll_l_15"), mood = EpMood.HAPPY) {
                for (l in 1..3) for (c in 1..3) text("ml${l}c$c", cheia3[l][c])
                hot("ml1c1", "ml1c2", "ml1c3")
            }
            val cheia4 = tabelaLl1DaAula(GRAMATICA_LL1_4)
            cena(tx("ll_l_16")) {
                clear()
                add(titulo("LL(1)"))
                add(codigo("g", GRAMATICA_LL1_4, 70f, x = 120f, size = 16f))
                add(tabela("m", cheia4.map { l -> l.mapIndexed { j, s -> if (j == 0 || l === cheia4[0]) s else "" } }, 170f, cw = 70f, ch = 46f, size = 14f))
                hot("g0")
            }
            pergunta(tx("ll_l_17"), setOf("ml1c1"), setOf("ml1c2", "ml1c3", "ml1c4"), tx("ll_l_17_dica"), tx("ll_l_17_ok")) { hot("g0") }
            cena(tx("ll_l_18"), mood = EpMood.SURPRISED) {
                change("ml1c1") { it.copy(text = cheia4[1][1], size = 11f, tint = Tint.RED) }
                add(codigo("fat", listOf("S  -> a S'", "S' -> b | c"), 290f, x = 120f, passo = 28f, size = 15f))
                hot("ml1c1", "fat0", "fat1")
            }
            fecho(Fecho(
                tx("ll_f_af"), true, tx("ll_f_q"), tx("ll_f_dica"), tx("ll_f_ok"), tx("ll_f_af"),
                tx("ll_f_erro2"), tx("ll_f_certo2"), tx("ll_f_fala2"),
                listOf(tx("ll_f_z1"), tx("ll_f_z2"), tx("ll_f_z3")), tx("ll_f_resumo"),
            ))
        },

        episodio(Skill.LR0_ITEMS, tx("epparsers_ponto_que_anda")) {
            fun item(id: String, t: String, y: Float, c: Tint = Tint.NONE) = Box(id, t, 180f, y, 230f, 44f, Look.TOKEN, c, size = 17f)
            cena(tx("lr_l_01"), mood = EpMood.THINK) {
                add(titulo("LR(0)"))
                add(pilha("p", listOf("E", "+"), 80f, 250f, tint = Tint.ORANGE))
                add(Box("r", "E -> E + n", 236f, 200f, 170f, 44f, Look.CELL, size = 16f))
            }
            pergunta(tx("lr_l_01q"), setOf("o1"), setOf("o2", "o3"), tx("lr_l_01q_dica"), tx("lr_l_01q_ok")) {
                hot("r")
                add(opcao("o1", "n", 172f, 300f), opcao("o2", "E", 240f, 300f), opcao("o3", "+", 308f, 300f))
            }
            cena(tx("lr_l_02")) { drop("o1", "o2", "o3"); hot("r") }
            palavra("ITEM", tx("lr_l_03"))
            cena(tx("epparsers_que_esta_antes_ponto_eu")) {
                clear()
                add(titulo(tx("epparsers_item_lr_0")))
                add(item("a", "E -> E • + n", 120f))
                text("a", "E -> E • + n")
                hot("a")
            }
            cena(tx("epparsers_se_ponto_esta_antes_posso")) {
                text("a", "E -> • E + n")
                add(item("b", "E -> • n", 180f, Tint.TEAL))
                hot("b")
            }
            pergunta(
                tx("epparsers_neste_estado_chega_n_qual"),
                setOf("b"), setOf("a"),
                tx("epparsers_ponto_so_anda_sobre_simbolo"),
                tx("epparsers_isso_ponto_passa_por_cima"),
            ) { hot() }
            cena(tx("epparsers_andar_ponto_por_simbolo_goto")) {
                drop("a")
                text("b", "E -> n •")
                move("b", 180f, 250f)
                add(item("a2", tx("epparsers_estado_novo"), 150f, Tint.YELLOW))
                wire("a2", "b")
            }
            cena(tx("epparsers_ponto_no_fim_quer_dizer"), mood = EpMood.HAPPY) { hot("b") }
            cena(tx("epparsers_cada_conjunto_itens_estado_juntos"), who = PROF) { hot() }
            cena(tx("lr_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("lr_l_r1"), 110f), regra("r2", tx("lr_l_r2"), 166f), regra("r3", tx("lr_l_r3"), 222f))
            }
            pergunta(tx("lr_l_06"), setOf("r1"), setOf("r2", "r3"), tx("lr_l_06_dica"), tx("lr_l_06_ok")) {
                hot()
            }
            cena(tx("lr_l_07")) { hot("r2") }
            cena(tx("lr_l_08")) {
                clear()
                add(titulo(tx("lr_l_titulo_estado0")))
                add(Box("i0", "S' -> • S", 180f, 90f, 230f, 40f, Look.TOKEN, Tint.TEAL, size = 16f))
            }
            pergunta(tx("lr_l_09"), setOf("o1", "o2"), setOf("o3", "o4"), tx("lr_l_09_dica"), tx("lr_l_09_ok")) {
                add(Box("o1", "S -> • ( S )", 180f, 160f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o2", "S -> • id", 180f, 204f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("o3", "S -> ( • S )", 180f, 248f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o4", "S -> id •", 180f, 292f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            pergunta(tx("lr_l_10"), setOf("p1"), setOf("p2", "p3"), tx("lr_l_10_dica"), tx("lr_l_10_ok")) {
                drop("o3", "o4"); tint("o1", Tint.TEAL); tint("o2", Tint.TEAL)
                add(Box("p1", "S -> id •", 180f, 262f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("p2", "S -> • id", 180f, 306f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("p3", "S -> ( S • )", 180f, 350f, 230f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            pergunta(tx("lr_l_11"), setOf("q1"), setOf("q2"), tx("lr_l_11_dica"), tx("lr_l_11_ok")) {
                drop("p1", "p2", "p3")
                add(Box("i1", "S -> ( • S )", 180f, 270f, 230f, 38f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("q1", tx("lr_l_sim"), 130f, 340f, 90f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("q2", tx("lr_l_nao"), 230f, 340f, 90f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("lr_l_12"), mood = EpMood.HAPPY) {
                drop("q1", "q2")
                add(Box("i2", "S -> • ( S )", 180f, 314f, 230f, 36f, Look.TOKEN, Tint.TEAL, size = 15f), Box("i3", "S -> • id", 180f, 356f, 230f, 36f, Look.TOKEN, Tint.TEAL, size = 15f))
                hot("i1", "i2", "i3")
            }
            fecho(Fecho(
                tx("lr_f_af"), false, tx("lr_f_q"), tx("lr_f_dica"), tx("lr_f_ok"), tx("lr_f_certo"),
                tx("lr_f_erro2"), tx("lr_f_certo2"), tx("lr_f_fala2"),
                listOf(tx("lr_f_z1"), tx("lr_f_z2"), tx("lr_f_z3")), tx("lr_f_resumo"),
            ))
        },

        episodio(Skill.SLR_TABLE, tx("epparsers_quando_reduzir")) {
            cena(tx("slr_l_01"), mood = EpMood.THINK) {
                add(titulo("SLR(1)"))
                add(Box("it", "E -> n •", 180f, 150f, 200f, 46f, Look.TOKEN, Tint.TEAL, size = 18f))
                add(Box("tok", "?", 180f, 240f, 60f, 46f, Look.TOKEN, Tint.YELLOW, size = 20f))
            }
            pergunta(tx("slr_l_01q"), setOf("o1", "o2"), setOf("o3"), tx("slr_l_01q_dica"), tx("slr_l_01q_ok")) {
                add(Box("gs", "E -> E + n | n", 180f, 90f, 220f, 40f, Look.CELL, size = 16f))
                add(opcao("o1", "+", 112f, 330f), opcao("o2", "$", 180f, 330f), opcao("o3", "n", 248f, 330f))
                hot("gs")
            }
            cena(tx("slr_l_02")) { drop("o1", "o2", "o3"); hot("tok") }
            palavra("SLR", tx("slr_l_03"))
            cena(tx("epparsers_automato_vira_tabela_linha_estado")) {
                clear()
                add(titulo(tx("epparsers_tabela_slr_1")))
                add(tabela("m", tabelaSlrDaAula(), 74f, cw = 62f, ch = 38f, size = 14f))
                // O r2 da tabela é "reduza pela regra 2": o número precisa estar na tela.
                add(rotulo("rg", "1: E -> E + n    2: E -> n", 180f, 290f, size = 14f))
            }
            cena(tx("epparsers_letra_manda_empilhar_letra_r")) { hot("ml1c1", "ml3c2") }
            cena(tx("epparsers_mas_reduzir_quando_slr_so"), mood = EpMood.THINK) {
                add(Box("f", "FOLLOW(E) = { +, $ }", 180f, 336f, 240f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
                hot("f")
            }
            pergunta(
                tx("epparsers_estado_2_terminou_regra_em"),
                setOf("ml3c1"), setOf("ml3c2", "ml3c3"),
                tx("epparsers_procure_simbolo_que_nao_esta"),
                tx("epparsers_isso_n_nao_esta_no"),
            ) { hot() }
            cena(tx("epparsers_se_casa_pede_empilhar_reduzir"), mood = EpMood.SURPRISED) {
                text("f", tx("epparsers_shift_ou_reduce"))
                tint("f", Tint.RED)
            }
            cena(tx("epparsers_gramatica_sem_conflito_na_tabela"), who = PROF, mood = EpMood.HAPPY) { hot() }
            cena(tx("slr_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("slr_l_r1"), 110f), regra("r2", tx("slr_l_r2"), 166f), regra("r3", tx("slr_l_r3"), 222f))
            }
            pergunta(tx("slr_l_06"), setOf("r2"), setOf("r1", "r3"), tx("slr_l_06_dica"), tx("slr_l_06_ok")) {
                hot()
            }
            cena(tx("slr_l_07")) { hot("r3") }
            cena(tx("slr_l_08")) {
                clear()
                add(titulo("SLR(1)"))
                add(Box("it", "S -> id •", 180f, 96f, 200f, 44f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("fo", "FOLLOW(S) = { ), $ }", 180f, 154f, 250f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(tabela("m", listOf(listOf("", "id", "(", ")", "$"), listOf("k", "", "", "", "")), 236f, cw = 64f, ch = 50f, size = 15f))
            }
            pergunta(tx("slr_l_09"), setOf("ml1c3", "ml1c4"), setOf("ml1c1", "ml1c2"), tx("slr_l_09_dica"), tx("slr_l_09_ok")) {
                hot("fo")
            }
            pergunta(tx("slr_l_10"), setOf("o3"), setOf("o1", "o2"), tx("slr_l_10_dica"), tx("slr_l_10_ok")) {
                text("ml1c3", "r"); text("ml1c4", "r"); hot("ml1c2")
                add(Box("o1", tx("slr_l_reduz"), 90f, 350f, 84f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o2", tx("slr_l_empilha"), 180f, 350f, 84f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o3", tx("slr_l_erro"), 270f, 350f, 84f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("slr_l_11"), mood = EpMood.HAPPY) {
                drop("o1", "o2", "o3")
                hot("ml1c3", "ml1c4")
            }
            fecho(Fecho(
                tx("slr_f_af"), false, tx("slr_f_q"), tx("slr_f_dica"), tx("slr_f_ok"), tx("slr_f_certo"),
                tx("slr_f_erro2"), tx("slr_f_certo2"), tx("slr_f_fala2"),
                listOf(tx("slr_f_z1"), tx("slr_f_z2"), tx("slr_f_z3")), tx("slr_f_resumo"),
            ))
        },

        episodio(Skill.LALR, tx("epparsers_mais_precisao_mesma_tabela")) {
            fun cx(id: String, t: String, x: Float, y: Float, c: Tint = Tint.NONE) = Box(id, t, x, y, 160f, 54f, Look.TOKEN, c, size = 14f)
            cena(tx("lalr_l_01"), mood = EpMood.THINK) {
                add(titulo("LALR"))
                add(Box("it", "E -> n •", 180f, 140f, 200f, 46f, Look.TOKEN, Tint.TEAL, size = 18f))
                add(Box("fo", "FOLLOW(E) = { +, ), $ }", 180f, 210f, 250f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("x", "r ?", 180f, 280f, 90f, 44f, Look.TOKEN, Tint.RED, size = 18f))
            }
            pergunta(tx("lalr_l_01q"), setOf("o1", "o2", "o3"), setOf("o4"), tx("lalr_l_01q_dica"), tx("lalr_l_01q_ok")) {
                add(opcao("o1", "+", 75f, 350f), opcao("o2", ")", 145f, 350f), opcao("o3", "$", 215f, 350f), opcao("o4", "n", 285f, 350f))
                hot("it")
            }
            cena(tx("lalr_l_02")) { drop("o1", "o2", "o3", "o4"); hot("fo") }
            palavra("LR(1)", tx("lalr_l_03"))
            cena(tx("lalr_l_04")) {
                clear()
                add(titulo("LR(1)"))
                add(cx("a", "E -> n • , +", 100f, 120f, Tint.TEAL), cx("b", "E -> n • , $", 266f, 120f, Tint.TEAL))
                add(cx("c", "T -> id • , +", 100f, 190f), cx("d", "T -> id • , $", 266f, 190f))
            }
            pergunta(
                tx("epparsers_miolo_item_sem_token_virgula"),
                setOf("b"), setOf("c", "d"),
                tx("epparsers_compare_regra_ponto_ignore_que"),
                tx("epparsers_isso_regra_ponto_sao_iguais"),
            ) { hot("a") }
            cena(tx("epparsers_lalr_junta_estados_miolo_igual")) {
                drop("c", "d", "b")
                text("a", "E -> n • , + $")
                move("a", 180f, 150f)
                tint("a", Tint.GREEN)
                hot("a")
            }
            cena(tx("epparsers_fica_quase_tao_forte_quanto")) {
                add(Box("r", tx("epparsers_bison_usa_lalr_1"), 180f, 240f, 240f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
                hot("r")
            }
            cena(tx("epparsers_juntar_estados_pode_criar_conflito"), who = PROF) { hot() }
            cena(tx("lalr_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("lalr_l_r1"), 110f), regra("r2", tx("lalr_l_r2"), 166f), regra("r3", tx("lalr_l_r3"), 222f))
            }
            pergunta(tx("lalr_l_06"), setOf("r2"), setOf("r1", "r3"), tx("lalr_l_06_dica"), tx("lalr_l_06_ok")) {
                hot()
            }
            cena(tx("lalr_l_07")) { hot("r3") }
            cena(tx("lalr_l_08")) {
                clear()
                add(titulo("LALR"))
                add(cx("e1", "T -> id • , $", 100f, 120f), cx("e2", "T -> id • , )", 266f, 120f), cx("e3", "T -> ( • E ) , $", 180f, 196f))
            }
            pergunta(tx("lalr_l_09"), setOf("e1", "e2"), setOf("e3"), tx("lalr_l_09_dica"), tx("lalr_l_09_ok")) {
            }
            pergunta(tx("lalr_l_10"), setOf("o1"), setOf("o2", "o3"), tx("lalr_l_10_dica"), tx("lalr_l_10_ok")) {
                tint("e1", Tint.TEAL); tint("e2", Tint.TEAL)
                add(Box("o1", "T -> id • , ) $", 180f, 270f, 240f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o2", "T -> id • , $", 180f, 314f, 240f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o3", "T -> ( • E ) , ) $", 180f, 358f, 240f, 38f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("lalr_l_11"), mood = EpMood.HAPPY) {
                drop("e1", "e2", "e3", "o2", "o3")
                move("o1", 180f, 160f); tint("o1", Tint.GREEN); hot("o1")
            }
            fecho(Fecho(
                tx("lalr_f_af"), true, tx("lalr_f_q"), tx("lalr_f_dica"), tx("lalr_f_ok"), tx("lalr_f_af"),
                tx("lalr_f_erro2"), tx("lalr_f_certo2"), tx("lalr_f_fala2"),
                listOf(tx("lalr_f_z1"), tx("lalr_f_z2"), tx("lalr_f_z3")), tx("lalr_f_resumo"),
            ))
        },

        episodio(Skill.DFA_MINIMIZATION, tx("eplexico_juntar_gemeos")) {
            val pares = tabela(
                "t",
                listOf(listOf("", "A", "B", "C"), listOf("B", ""), listOf("C", "", ""), listOf("D", "", "", "")),
                110f, cw = 62f, ch = 46f, size = 16f,
            )
            cena(tx("min_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("comum_afd")))
                add(estados(listOf(70f to 150f, 180f to 110f, 180f to 230f, 290f to 170f), finais = setOf(3), nomes = listOf("A", "B", "C", "D")))
                wire("q0", "q1", "a"); wire("q0", "q2", "b"); wire("q1", "q3", "b"); wire("q2", "q3", "b")
            }
            pergunta(tx("min_l_01q"), setOf("q3"), setOf("q0", "q1", "q2"), tx("min_l_01q_dica"), tx("min_l_01q_ok")) {
                hot()
            }
            cena(tx("min_l_02")) { hot("q1", "q2") }
            palavra(tx("palavra_minimo"), tx("min_l_03"))
            cena(tx("min_l_04")) {
                clear()
                add(titulo(tx("eplexico_tabela_pares")))
                add(rotulo("info", tx("eplexico_d_unico_final"), 180f, 62f))
                add(pares)
            }
            cena(tx("eplexico_primeira_rodada_final_nao_final")) {
                for (c in 1..3) { text("tl3c$c", "X"); tint("tl3c$c", Tint.ORANGE) }
                hot("tl3c1", "tl3c2", "tl3c3")
            }
            cena(tx("eplexico_agora_olho_para_onde_cada"), mood = EpMood.THINK) {
                add(Box("par", tx("eplexico_c_d_ja_tem_x"), 180f, 320f, 190f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f))
                hot("tl1c1")
            }
            cena(tx("eplexico_se_destinos_sao_diferentes_origem")) {
                text("tl1c1", "X"); tint("tl1c1", Tint.ORANGE)
                text("tl2c1", "X"); tint("tl2c1", Tint.ORANGE)
                hot("tl1c1", "tl2c1")
            }
            pergunta(
                tx("eplexico_sobrou_casa_em_branco_toque"),
                setOf("tl2c2"), setOf("tl1c1", "tl2c1", "tl3c1", "tl3c2", "tl3c3"),
                tx("eplexico_procure_casa_sem_x_na"),
                tx("eplexico_isso_b_c_vao_sempre"),
            ) {
                drop("par")
                hot()
            }
            cena(tx("eplexico_casa_em_branco_no_fim"), mood = EpMood.HAPPY) {
                add(Box("m", "B + C", 180f, 330f, 120f, 44f, Look.TOKEN, Tint.GREEN, size = 16f))
                hot("m")
            }
            cena(tx("min_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("min_l_r1"), 110f), regra("r2", tx("min_l_r2"), 166f), regra("r3", tx("min_l_r3"), 222f))
            }
            pergunta(tx("min_l_06"), setOf("r2"), setOf("r1", "r3"), tx("min_l_06_dica"), tx("min_l_06_ok")) {
                hot()
            }
            cena(tx("min_l_07")) { hot("r3") }
            cena(tx("min_l_08")) {
                clear()
                add(titulo(tx("comum_afd")))
                add(estados(listOf(70f to 160f, 180f to 160f, 290f to 160f), finais = setOf(2), nomes = listOf("P", "Q", "R")))
                wire("q0", "q1", "0"); wire("q1", "q1", "0"); wire("q0", "q2", "1", bend = -40f); wire("q1", "q2", "1"); wire("q2", "q2", "0,1")
                add(Box("pq", "P, Q", 90f, 300f, 86f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("pr", "P, R", 180f, 300f, 86f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("qr", "Q, R", 270f, 300f, 86f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            pergunta(tx("min_l_09"), setOf("pr", "qr"), setOf("pq"), tx("min_l_09_dica"), tx("min_l_09_ok")) {
                hot("q2")
            }
            pergunta(tx("min_l_10"), setOf("o1"), setOf("o2"), tx("min_l_10_dica"), tx("min_l_10_ok")) {
                tint("pr", Tint.RED); tint("qr", Tint.RED); hot("q0", "q1")
                add(Box("o1", tx("min_l_juntar"), 130f, 370f, 96f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("o2", tx("min_l_separar"), 236f, 370f, 96f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("min_l_11"), mood = EpMood.HAPPY) {
                drop("o1", "o2")
                tint("pq", Tint.GREEN); hot("pq")
            }
            // 5. O caso difícil: o AFD do livro, em que um par só cai duas rodadas depois
            val nomes = listOf("A", "B", "C", "D", "E")
            val rodada = com.compilandia.engine.Minimizer.run(AFD_LIVRO).marks.associate { (it.p to it.q) to it.round }
            // Casa da tabela de um par: linha do estado maior, coluna do menor (B..E nas linhas, A..D nas colunas).
            fun casa(p: Int, q: Int) = "tl${maxOf(p, q)}c${minOf(p, q) + 1}"
            fun daRodada(r: Int) = rodada.filterValues { it == r }.keys.map { (p, q) -> casa(p, q) }.toSet()
            fun marca(r: Int) { for (id in daRodada(r)) { text(id, "$r"); tint(id, Tint.ORANGE) } }
            val brancas = (1..4).flatMap { l -> (1..l).map { c -> "tl${l}c$c" } }.toSet()
            cena(tx("min_l_12")) {
                clear()
                add(titulo(tx("min_l_titulo_livro"), size = 20f))
                add(estados(listOf(40f to 160f, 130f to 112f, 130f to 214f, 230f to 112f, 320f to 160f), finais = setOf(4), nomes = nomes))
                for ((k, para) in AFD_LIVRO.delta) {
                    val (de, c) = k
                    wire("q$de", "q$para", c.toString(), bend = if (de == 4 && para == 2) 30f else if (de == 3 && para == 1) -44f else 0f)
                }
                add(tabela("t", listOf(listOf("", "A", "B", "C", "D")) + (1..4).map { l -> listOf(nomes[l]) + List(l) { "" } }, 264f, cw = 50f, ch = 32f, size = 15f))
                hot("q4")
            }
            cena(tx("min_l_13")) { marca(1); hot(*daRodada(1).toTypedArray()) }
            pergunta(tx("min_l_14"), daRodada(2), brancas - daRodada(1) - daRodada(2), tx("min_l_14_dica"), tx("min_l_14_ok")) { hot("q4") }
            cena(tx("min_l_14b")) { marca(2); hot(*daRodada(2).toTypedArray()) }
            pergunta(tx("min_l_15"), daRodada(3), brancas - daRodada(1) - daRodada(2) - daRodada(3), tx("min_l_15_dica"), tx("min_l_15_ok")) { hot() }
            cena(tx("min_l_15b"), mood = EpMood.SURPRISED) { marca(3); hot(*daRodada(3).toTypedArray()) }
            val sobra = brancas - rodada.keys.map { (p, q) -> casa(p, q) }.toSet()
            pergunta(tx("min_l_16"), sobra, brancas - sobra, tx("min_l_16_dica"), tx("min_l_16_ok")) { hot() }
            cena(tx("min_l_17"), mood = EpMood.HAPPY) {
                for (id in sobra) tint(id, Tint.GREEN)
                tint("q0", Tint.GREEN); tint("q2", Tint.GREEN)
                hot("q0", "q2", *sobra.toTypedArray())
            }
            fecho(Fecho(
                tx("min_f_af"), false, tx("min_f_q"), tx("min_f_dica"), tx("min_f_ok"), tx("min_f_certo"),
                tx("min_f_erro2"), tx("min_f_certo2"), tx("min_f_fala2"),
                listOf(tx("min_f_z1"), tx("min_f_z2"), tx("min_f_z3")), tx("min_f_resumo"),
            ))
        },

        episodio(Skill.AST_BUILD, tx("epbackend_arvore_enxuta")) {
            val deriv = no("E", no("E", no("id")), no("+"), no("T", no("("), no("E", no("id")), no(")")))
            val ast = no("BinOp +", no("a"), no("b"))
            cena(tx("ast_l_01"), mood = EpMood.THINK) {
                add(titulo("AST"))
                add(Box("src", "a + ( b )", 180f, 170f, 200f, 52f, Look.CELL, size = 20f))
            }
            pergunta(tx("ast_l_01q"), setOf("s0", "s1", "s3"), setOf("s2", "s4"), tx("ast_l_01q_dica"), tx("ast_l_01q_ok")) {
                listOf("a", "+", "(", "b", ")").forEachIndexed { i, t -> add(Box("s$i", t, 80f + i * 50f, 270f, 40f, 46f, Look.TOKEN, Tint.YELLOW, size = 18f)) }
                hot("src")
            }
            cena(tx("ast_l_02")) { drop("s2", "s4"); hot("s0", "s1", "s3") }
            palavra("AST", tx("ast_l_03"))
            cena(tx("epbackend_arvore_derivacao_guarda_tudo_ate")) {
                clear()
                add(titulo(tx("epbackend_b")))
                val (b, w) = arvore(deriv, 90f, passoY = 62f, largura = 320f)
                add(b); for (x in w) wire(x)
            }
            pergunta(
                tx("epbackend_tem_no_sobrando_aqui_toque"),
                setOf("t20", "t22"), setOf("t", "t00"),
                tx("epbackend_arvore_ja_mostra_ordem_parentese"),
                tx("epbackend_isso_pontuacao_some_arvore_abstrata"),
            ) {}
            cena(tx("epbackend_arvore_abstrata_guarda_so_que")) {
                dropAll("t")
                val (b, w) = arvore(ast, 110f, passoY = 90f, largura = 220f, prefix = "a")
                add(b); for (x in w) wire(x)
            }
            cena(tx("epbackend_no_codigo_java_cada_no")) {
                add(Box("j", tx("epbackend_new_binop_b"), 180f, 310f, 300f, 46f, Look.TOKEN, Tint.YELLOW, size = 15f))
                hot("j")
            }
            cena(tx("ast_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("ast_l_r1"), 110f), regra("r2", tx("ast_l_r2"), 166f), regra("r3", tx("ast_l_r3"), 222f))
            }
            pergunta(tx("ast_l_06"), setOf("r2"), setOf("r1", "r3"), tx("ast_l_06_dica"), tx("ast_l_06_ok")) {
                hot()
            }
            cena(tx("ast_l_07")) { hot("r3") }
            cena(tx("ast_l_08")) {
                clear()
                add(titulo("AST"))
                add(Box("src", "total * ( preco - 2 )", 180f, 80f, 290f, 44f, Look.CELL, size = 16f))
            }
            pergunta(tx("ast_l_09"), setOf("o1"), setOf("o2", "o3"), tx("ast_l_09_dica"), tx("ast_l_09_ok")) {
                add(Box("o1", "*", 120f, 160f, 50f, 40f, Look.TOKEN, Tint.YELLOW), Box("o2", "-", 180f, 160f, 50f, 40f, Look.TOKEN, Tint.YELLOW), Box("o3", "(", 240f, 160f, 50f, 40f, Look.TOKEN, Tint.YELLOW))
            }
            pergunta(tx("ast_l_10"), setOf("p1", "p2"), setOf("p3"), tx("ast_l_10_dica"), tx("ast_l_10_ok")) {
                drop("o1", "o2", "o3")
                add(Box("n0", "*", 180f, 150f, 44f, 34f, Look.NODE, Tint.ORANGE, size = 16f), Box("n1", "total", 100f, 220f, 70f, 34f, Look.NODE, Tint.TEAL, size = 15f), Box("n2", "-", 260f, 220f, 44f, 34f, Look.NODE, Tint.ORANGE, size = 16f))
                wire("n0", "n1", arrow = false); wire("n0", "n2", arrow = false)
                add(Box("p1", "preco", 120f, 340f, 80f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("p2", "2", 200f, 340f, 50f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f), Box("p3", "total", 275f, 340f, 80f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("ast_l_11"), mood = EpMood.HAPPY) {
                drop("p1", "p2", "p3")
                add(Box("n3", "preco", 220f, 300f, 70f, 34f, Look.NODE, Tint.TEAL, size = 15f), Box("n4", "2", 300f, 300f, 44f, 34f, Look.NODE, Tint.TEAL, size = 15f))
                wire("n2", "n3", arrow = false); wire("n2", "n4", arrow = false)
                hot("n0", "n2")
            }
            fecho(Fecho(
                tx("ast_f_af"), true, tx("ast_f_q"), tx("ast_f_dica"), tx("ast_f_ok"), tx("ast_f_af"),
                tx("ast_f_erro2"), tx("ast_f_certo2"), tx("ast_f_fala2"),
                listOf(tx("ast_f_z1"), tx("ast_f_z2"), tx("ast_f_z3")), tx("ast_f_resumo"),
            ))
        },

        episodio(Skill.CODEGEN_MIPS, tx("epbackend_carrega_opera_guarda")) {
            // O resultado vai para a variável a, e não para o temporário t1: senão $t1 e t1 aparecem juntos na tela.
            val tac = TacGen.generate(ExprParser.parse("b * c")).map { it.copy(target = "a") }
            val mips = Mips.fromTac(tac)
            val mips2 = Mips.fromTac(TacGen.generate(ExprParser.parse("preco + taxa")).map { it.copy(target = "total") })
            val embaralhada = listOf(2, 0, 3, 1).filter { it < mips2.size }
            cena(tx("mips_l_01"), mood = EpMood.THINK) {
                add(titulo("MIPS"))
                add(Box("var", "total", 90f, 170f, 110f, 46f, Look.TOKEN, Tint.BLUE, size = 16f), Box("reg", "\$t0", 270f, 170f, 100f, 46f, Look.TOKEN, Tint.ORANGE, size = 16f))
                wire("var", "reg", "lw")
            }
            pergunta(tx("mips_l_01q"), setOf("reg", "r1"), setOf("var", "m1"), tx("mips_l_01q_dica"), tx("mips_l_01q_ok")) {
                add(Box("m1", "preco", 90f, 260f, 110f, 46f, Look.TOKEN, Tint.BLUE, size = 16f), Box("r1", "\$t1", 270f, 260f, 100f, 46f, Look.TOKEN, Tint.ORANGE, size = 16f))
                add(rotulo("lm", tx("comum_memoria"), 90f, 310f), rotulo("lr", tx("palco_registradores"), 270f, 310f))
                hot()
            }
            cena(tx("mips_l_02")) { hot("reg", "r1") }
            palavra("MIPS", tx("mips_l_03"))
            cena(tx("epbackend_mips_so_faz_conta_com")) {
                clear()
                add(titulo(tac[0].toString()))
                add(Box("mem", tx("comum_memoria"), 90f, 110f, 130f, 46f, Look.TOKEN, Tint.BLUE, size = 15f), Box("reg", tx("palco_registradores"), 270f, 110f, 150f, 46f, Look.TOKEN, Tint.ORANGE, size = 15f))
                wire("mem", "reg", "lw")
            }
            cena(tx("epbackend_por_isso_toda_conta_tem")) {
                add(codigo("c", mips, 180f, x = 86f, passo = 34f, size = 17f))
                hot("c0", "c1")
            }
            pergunta(
                tx("epbackend_qual_linha_faz_multiplicacao_toque"),
                setOf("c2"), setOf("c0", "c1", "c3"),
                tx("epbackend_procure_instrucao_mul"),
                tx("epbackend_isso_antes_dela_dois_lw"),
            ) { hot() }
            cena(tx("epbackend_sw_leva_resultado_volta_para")) { hot("c3") }
            cena(tx("mips_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("mips_l_r1"), 110f), regra("r2", tx("mips_l_r2"), 166f), regra("r3", tx("mips_l_r3"), 222f))
            }
            pergunta(tx("mips_l_06"), setOf("r1"), setOf("r2", "r3"), tx("mips_l_06_dica"), tx("mips_l_06_ok")) {
                hot()
            }
            cena(tx("mips_l_07")) { hot("r3") }
            cena(tx("mips_l_08")) {
                clear()
                add(titulo("preco + taxa"))
                embaralhada.forEachIndexed { i, linha -> add(Box("l$linha", mips2[linha], 180f, 120f + i * 54f, 260f, 44f, Look.CELL, size = 16f)) }
            }
            val lws = mips2.indices.filter { mips2[it].startsWith("lw") }.map { "l$it" }.toSet()
            val sws = mips2.indices.filter { mips2[it].startsWith("sw") }.map { "l$it" }.toSet()
            val todas = mips2.indices.map { "l$it" }.toSet()
            pergunta(tx("mips_l_09"), lws, todas - lws, tx("mips_l_09_dica"), tx("mips_l_09_ok")) { hot() }
            pergunta(tx("mips_l_10"), sws, todas - lws - sws, tx("mips_l_10_dica"), tx("mips_l_10_ok")) {
                for (id in lws) tint(id, Tint.TEAL)
            }
            cena(tx("mips_l_11"), mood = EpMood.HAPPY) {
                mips2.indices.forEach { i -> move("l$i", 180f, 120f + i * 54f); tint("l$i", Tint.TEAL) }
                hot()
            }
            fecho(Fecho(
                tx("mips_f_af"), false, tx("mips_f_q"), tx("mips_f_dica"), tx("mips_f_ok"), tx("mips_f_certo"),
                tx("mips_f_erro2"), tx("mips_f_certo2"), tx("mips_f_fala2"),
                listOf(tx("mips_f_z1"), tx("mips_f_z2"), tx("mips_f_z3")), tx("mips_f_resumo"),
            ))
        },
    )
}

/**
 * A tabela SLR de E -> E + n | n, inteira e tirada do motor: ACTION de n, + e $, e GOTO de E.
 * As falas citam "o estado 2" como o de E -> n •, então a numeração do motor precisa bater.
 */
internal fun tabelaSlrDaAula(): List<List<String>> {
    val t = LRTable(Grammar.parse("E -> E + n | n"), LRKind.SLR1)
    val g = t.grammar
    val reduzN = t.automaton.states.single { st -> st.items.any { it.isComplete(g) && g.productions[it.prodId].rhs == listOf("n") } }.id
    check(reduzN == 2) { "a aula diz que o estado 2 reduz E -> n, mas o motor deu $reduzN" }
    val ts = listOf("n", "+", "$")
    return listOf(listOf("") + ts + "E") + t.automaton.states.indices.map { s ->
        listOf("$s") + ts.map { a -> t.action[s to a]?.let { if (it is LRAction.Accept) "ok" else it.short() } ?: "" } + (t.gotoTable[s to "E"]?.toString() ?: "")
    }
}
