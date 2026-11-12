package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Aulas básicas longas (A2). Quatro blocos em cada uma:
 * por que o algoritmo existe, o primeiro exemplo, a receita à vista e um segundo exemplo em que a pessoa calcula.
 * Os conjuntos mostrados no segundo exemplo são conferidos contra o motor em EpisodeFactsTest.
 */

/** Gramática do segundo exemplo do FIRST. */
val GRAMATICA_FIRST_2 = listOf("S -> A b | c", "A -> a A | ε")

/** Gramática do segundo exemplo do FOLLOW. */
val GRAMATICA_FOLLOW_2 = listOf("E -> T R", "R -> + T R | ε", "T -> F Y", "Y -> * F Y | ε", "F -> ( E ) | id")

/** Assuntos que já ganharam a aula longa. Os outros seguem com a aula curta até serem refeitos. */
val ASSUNTOS_COM_AULA_LONGA = setOf(
    Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT, Skill.ERROR_RECOVERY, Skill.SHIFT_REDUCE, Skill.YACC_BISON,
    Skill.ATTRIBUTES_TYPES, Skill.SCOPE_SYMBOLS, Skill.TAC, Skill.OPTIMIZATION, Skill.RUNTIME, Skill.REG_ALLOC,
    Skill.CFG_DERIVATION, Skill.AMBIGUITY, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING,
    Skill.TOKENS, Skill.REGEX_DFA, Skill.NFA_TO_DFA, Skill.LEX_FLEX,
    Skill.PHASES, Skill.FIRST, Skill.FOLLOW, Skill.LL1_TABLE, Skill.LR0_ITEMS, Skill.SLR_TABLE, Skill.LALR,
    Skill.DFA_MINIMIZATION, Skill.AST_BUILD, Skill.CODEGEN_MIPS,
)

internal fun saco(id: String, nome: String, x: Float, y: Float = 300f) = Box(id, nome, x, y, 104f, 90f, Look.BIN, Tint.NONE, size = 15f)
/** Uma ficha que entra num saco: grande o bastante para ler no celular, e já dentro do saco (mural, rodada 13). */
internal fun ficha(id: String, t: String, x: Float, y: Float = 280f, w: Float = 30f) = Box(id, t, x, y + 14f, w, 32f, Look.TOKEN, Tint.TEAL, size = 17f)
internal fun opcao(id: String, t: String, x: Float, y: Float = 200f) = Box(id, t, x, y, 50f, 40f, Look.TOKEN, Tint.YELLOW)
/** Uma regra da receita. Texto longo ganha fonte menor para caber na caixa. */
internal fun regra(id: String, t: String, y: Float) = Box(id, t, 180f, y, 340f, 40f, Look.CELL, Tint.NONE, size = if (t.length > 44) 11f else 12f)

/**
 * O parser parado diante de uma escolha: pilha à esquerda, token à direita, as duas regras no meio.
 * A gramática inteira fica no alto, com a linha das duas regras acesa (mural, rodada 13):
 * sem ela, quem chega não sabe de onde vem o símbolo nem o token.
 */
internal fun EpisodeBuilder.escolha(topo: String, token: String, a: String, b: String, gram: List<String>, linha: Int) {
    clear()
    add(codigo("g", gram, 56f, x = 90f, passo = 26f, size = 15f))
    add(Box("topo", topo, 62f, 250f, 56f, 44f, Look.TOKEN, Tint.ORANGE, size = 18f))
    add(Box("tok", token, 298f, 250f, 56f, 44f, Look.TOKEN, Tint.TEAL, size = 18f))
    add(Box("ra", a, 180f, 192f, 150f, 40f, Look.CELL, size = 14f), Box("rb", b, 180f, 308f, 150f, 40f, Look.CELL, size = 14f))
    wire("topo", "ra", arrow = false); wire("topo", "rb", arrow = false)
    hot("g$linha")
}

/** A gramática das aulas de FIRST, FOLLOW e tabela LL(1). */
internal val GRAMATICA_EXPR = listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )")

val aulasLongas by lazy {
    listOf(
        // ─────────────────────────────── FASES ───────────────────────────────
        episodio(Skill.PHASES, tx("eplexico_linha_montagem")) {
            val nomes = listOf(tx("comum_lexica"), tx("comum_sintatica"), tx("comum_semantica"), tx("comum_intermediario"), tx("comum_otimizacao_2"), tx("comum_geracao"))
            val coluna = nomes.mapIndexed { i, n -> Box("f$i", n, 90f, 86f + i * 50f, 150f, 42f, Look.TOKEN, Tint.NONE, size = 15f) }
            val grade = nomes.mapIndexed { i, n -> Box("f$i", n, if (i % 2 == 0) 95f else 265f, 160f + (i / 2) * 64f, 150f, 46f, Look.TOKEN, Tint.NONE, size = 15f) }

            // 1. Por que existe
            cena(tx("fases_l_01")) {
                add(titulo(tx("fases_l_titulo_porque")))
                add(Box("cod", "conta = preco + 2", 84f, 170f, 150f, 52f, Look.CELL, size = 13f))
                add(Box("cpu", tx("fases_l_maquina"), 280f, 170f, 136f, 52f, Look.CELL, Tint.DARK, size = 15f))
                add(rotulo("lc", tx("fases_l_seu_codigo"), 84f, 216f), rotulo("lp", tx("fases_l_processador"), 280f, 216f))
            }
            pergunta(tx("fases_l_02"), setOf("cpu"), setOf("cod"), tx("fases_l_02_dica"), tx("fases_l_02_ok")) { hot() }
            cena(tx("fases_l_03"), mood = EpMood.THINK) {
                add(rotulo("q", "?", 184f, 170f, Tint.RED, size = 30f))
            }
            // 2. Primeiro exemplo
            cena(tx("fases_l_04")) {
                clear()
                add(titulo(tx("eplexico_seis_fases")))
                add(coluna)
            }
            cena(tx("eplexico_entra_seu_codigo_para_maquina")) {
                add(Box("p", tx("eplexico_x_2"), 270f, 86f, 150f, 42f, Look.CELL, size = 14f))
            }
            cena(tx("eplexico_fase_lexica_corta_letras_em")) {
                hot("f0"); tint("f0", Tint.TEAL)
                text("p", tx("eplexico_x_2_2"))
                wire("f0", "p")
            }
            cena(tx("eplexico_sintatica_monta_arvore_com_esses")) {
                hot("f1"); tint("f1", Tint.TEAL)
                move("p", 270f, 136f)
                clearWires(); wire("f1", "p")
                text("p", tx("comum_arvore"))
            }
            cena(tx("eplexico_semantica_confere_sentido_como_tipos")) {
                hot("f2"); tint("f2", Tint.TEAL)
                move("p", 270f, 186f)
                clearWires(); wire("f2", "p")
                text("p", tx("eplexico_arvore_com_tipos"))
            }
            pergunta(
                tx("eplexico_em_int_x_igual_oi"), setOf("f2"), setOf("f0", "f1", "f3", "f4", "f5"),
                tx("eplexico_escrita_esta_certa_frase_tem"), tx("eplexico_isso_tipo_errado_assunto_semantica"),
            ) {
                drop("p"); clearWires()
                add(Box("q", "int x = \"oi\";", 270f, 120f, 150f, 42f, Look.CELL, size = 14f))
                hot()
            }
            cena(tx("fases_l_05")) {
                drop("q")
                tint("f3", Tint.TEAL); hot("f3")
                add(Box("p", "t1 = a + 2", 270f, 236f, 150f, 42f, Look.CELL, size = 14f))
                wire("f3", "p")
            }
            cena(tx("fases_l_06")) {
                tint("f4", Tint.TEAL); tint("f5", Tint.TEAL); hot("f4", "f5")
                add(Box("m", tx("fases_l_maquina"), 270f, 336f, 150f, 42f, Look.CELL, Tint.DARK, size = 14f))
                clearWires(); wire("f5", "m")
            }
            // 3. A receita
            cena(tx("fases_l_07")) {
                clear()
                add(titulo(tx("fases_l_titulo_receita")))
                listOf(
                    tx("fases_l_r_lexica"), tx("fases_l_r_sintatica"), tx("fases_l_r_semantica"),
                    tx("fases_l_r_intermediario"), tx("fases_l_r_otimizacao"), tx("fases_l_r_geracao"),
                ).forEachIndexed { i, t -> add(Box("r$i", t, 180f, 86f + i * 50f, 320f, 40f, Look.CELL, if (i < 3) Tint.TEAL else Tint.NONE, size = 14f)) }
            }
            cena(tx("fases_l_08")) { hot("r0", "r1") }
            cena(tx("fases_l_09")) { hot("r2") }
            // 4. Segundo exemplo, com perguntas
            cena(tx("fases_l_10")) {
                clear()
                add(titulo(tx("fases_l_titulo_outro")))
                add(Box("w", "while (i < 10) i = i + 1;", 180f, 88f, 330f, 44f, Look.CELL, size = 14f))
                add(grade)
            }
            pergunta(tx("fases_l_11"), setOf("f0"), setOf("f1", "f2", "f3", "f4", "f5"), tx("fases_l_11_dica"), tx("fases_l_11_ok")) { hot() }
            pergunta(tx("fases_l_12"), setOf("f1"), setOf("f0", "f2", "f3", "f4", "f5"), tx("fases_l_12_dica"), tx("fases_l_12_ok")) {
                tint("f0", Tint.TEAL)
                text("w", "while (i < 10 i = i + 1;"); tint("w", Tint.RED)
            }
            pergunta(tx("fases_l_13"), setOf("f3"), setOf("f0", "f1", "f2", "f4", "f5"), tx("fases_l_13_dica"), tx("fases_l_13_ok")) {
                tint("f1", Tint.TEAL); tint("f2", Tint.TEAL)
                text("w", "while (i < 10) i = i + 1;"); tint("w", Tint.NONE)
            }
            cena(tx("fases_l_14"), mood = EpMood.HAPPY) {
                for (i in 0..5) tint("f$i", Tint.TEAL)
                hot()
            }
        },

        // ─────────────────────────────── FIRST ───────────────────────────────
        episodio(Skill.FIRST, tx("epgramatica_com_que_isso_comeca")) {
            val gram = listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )")
            // 1. Por que existe
            cena(tx("first_l_01"), mood = EpMood.THINK) { escolha("T", "id", "T -> id", "T -> ( E )", GRAMATICA_EXPR, 2) }
            cena(tx("first_l_02")) { hot("ra", "rb", "g2") }
            palavra("FIRST", tx("first_l_03"))
            // 2. Primeiro exemplo
            cena(tx("first_l_04")) {
                clear()
                add(titulo("FIRST"))
                add(codigo("g", gram, 62f, x = 90f, passo = 28f, size = 15f))
                add(saco("sT", "FIRST(T)", 64f), saco("sX", "FIRST(X)", 180f), saco("sE", "FIRST(E)", 296f))
            }
            cena(tx("epgramatica_comece_pelo_facil_t_comeca")) {
                hot("g2")
                add(ficha("t1", "id", 46f), ficha("t2", "(", 82f))
            }
            pergunta(
                tx("epgramatica_x_ele_tem_duas_regras"), setOf("o1", "o3"), setOf("o2", "o4"),
                tx("epgramatica_regra_comeca_com_mais_outra"), tx("epgramatica_isso_mais_epsilon"),
            ) {
                hot("g1")
                add(opcao("o1", "+", 90f, 190f), opcao("o2", "T", 150f, 190f), opcao("o3", "ε", 210f, 190f), opcao("o4", "id", 270f, 190f))
            }
            cena(tx("epgramatica_agora_ele_comeca_com_t")) {
                drop("o1", "o2", "o3", "o4")
                add(ficha("x1", "+", 162f), ficha("x2", "ε", 198f))
                add(ficha("e1", "id", 278f), ficha("e2", "(", 314f))
                hot("g0", "e1", "e2")
            }
            cena(tx("epgramatica_se_primeiro_simbolo_pudesse_sumir"), who = PROF) { hot() }
            // 3. A receita
            cena(tx("first_l_05")) {
                clear()
                add(titulo(tx("first_l_titulo_receita")))
                add(regra("r1", tx("first_l_r1"), 110f), regra("r2", tx("first_l_r2"), 166f), regra("r3", tx("first_l_r3"), 222f))
            }
            cena(tx("first_l_06")) { hot("r1") }
            pergunta(tx("first_l_07"), setOf("r2"), setOf("r1", "r3"), tx("first_l_07_dica"), tx("first_l_07_ok")) {
                hot()
                add(codigo("q", listOf("E -> T X"), 300f, x = 130f, size = 16f))
            }
            cena(tx("first_l_08")) { drop("q"); hot("r3") }
            // 4. Segundo exemplo, com perguntas
            cena(tx("first_l_09")) {
                clear()
                add(titulo("FIRST"))
                add(codigo("g", GRAMATICA_FIRST_2, 70f, x = 100f, passo = 30f, size = 16f))
                add(saco("sA", "FIRST(A)", 100f, 316f), saco("sS", "FIRST(S)", 260f, 316f))
            }
            pergunta(tx("first_l_10"), setOf("o1", "o3"), setOf("o2", "o4"), tx("first_l_10_dica"), tx("first_l_10_ok")) {
                hot("g1")
                add(opcao("o1", "a", 90f), opcao("o2", "b", 150f), opcao("o3", "ε", 210f), opcao("o4", "c", 270f))
            }
            pergunta(tx("first_l_11"), setOf("p1"), setOf("p2", "p3"), tx("first_l_11_dica"), tx("first_l_11_ok")) {
                drop("o1", "o2", "o3", "o4")
                add(ficha("a1", "a", 82f, 296f), ficha("a2", "ε", 118f, 296f))
                hot("g0")
                add(opcao("p1", "b", 120f), opcao("p2", "ε", 180f), opcao("p3", "A", 240f))
            }
            pergunta(tx("first_l_12"), setOf("q1"), setOf("q2"), tx("first_l_12_dica"), tx("first_l_12_ok")) {
                drop("p1", "p2", "p3")
                add(ficha("s1", "a", 228f, 296f), ficha("s2", "b", 260f, 296f))
                add(opcao("q1", "c", 150f), opcao("q2", "ε", 210f))
            }
            cena(tx("first_l_13"), mood = EpMood.HAPPY) {
                drop("q1", "q2")
                add(ficha("s3", "c", 292f, 296f))
                hot("s1", "s2", "s3")
            }
        },

        // ─────────────────────────────── FOLLOW ───────────────────────────────
        episodio(Skill.FOLLOW, tx("epgramatica_que_vem_depois")) {
            val gram = listOf("E -> T X", "X -> + T X | ε", "T -> id | ( E )")
            // 1. Por que existe
            cena(tx("follow_l_01"), mood = EpMood.THINK) { escolha("X", ")", "X -> + T X", "X -> ε", GRAMATICA_EXPR, 1) }
            cena(tx("follow_l_02")) { hot("rb", "tok", "g1") }
            palavra("FOLLOW", tx("follow_l_03"))
            // 2. Primeiro exemplo
            cena(tx("follow_l_04")) {
                clear()
                add(titulo("FOLLOW"))
                add(codigo("g", gram, 62f, x = 90f, passo = 28f, size = 15f))
                add(saco("sE", "FOLLOW(E)", 64f), saco("sX", "FOLLOW(X)", 180f), saco("sT", "FOLLOW(T)", 296f))
            }
            cena(tx("epgramatica_simbolo_inicial_ganha_fim_entrada")) {
                add(ficha("e1", "$", 46f))
                hot("e1")
            }
            cena(tx("epgramatica_em_abre_parentese_fecha_parentese")) {
                add(ficha("e2", ")", 82f))
                hot("g2", "e2")
            }
            pergunta(
                tx("epgramatica_depois_t_vem_x_x"), setOf("o1"), setOf("o2", "o3"),
                tx("epgramatica_olhe_first_x_sem_epsilon"), tx("epgramatica_isso_first_x_sem_epsilon"),
            ) {
                hot("g0")
                add(opcao("o1", "+", 110f, 190f), opcao("o2", "id", 180f, 190f), opcao("o3", "(", 250f, 190f))
            }
            cena(tx("epgramatica_x_ultimo_que_segue_segue")) {
                drop("o1", "o2", "o3")
                add(ficha("t1", "+", 262f))
                add(ficha("x1", "$", 162f), ficha("x2", ")", 198f))
                hot("g0", "x1", "x2")
            }
            cena(tx("epgramatica_como_x_pode_sumir_que")) {
                add(ficha("t2", "$", 296f), ficha("t3", ")", 330f))
                hot("t1", "t2", "t3")
            }
            // 3. A receita
            cena(tx("follow_l_05")) {
                clear()
                add(titulo(tx("follow_l_titulo_receita")))
                add(regra("r1", tx("follow_l_r1"), 110f), regra("r2", tx("follow_l_r2"), 166f), regra("r3", tx("follow_l_r3"), 222f))
            }
            cena(tx("follow_l_06")) { hot("r1") }
            pergunta(tx("follow_l_07"), setOf("r2"), setOf("r1", "r3"), tx("follow_l_07_dica"), tx("follow_l_07_ok")) {
                hot()
                add(codigo("q", listOf("E -> T X"), 300f, x = 130f, size = 16f))
            }
            cena(tx("follow_l_08")) { drop("q"); hot("r3") }
            // 4. Segundo exemplo, com perguntas
            cena(tx("follow_l_09")) {
                clear()
                add(titulo("FOLLOW"))
                add(codigo("g", GRAMATICA_FOLLOW_2, 58f, x = 96f, passo = 27f, size = 15f))
                add(saco("sT", "FOLLOW(T)", 100f, 330f), saco("sF", "FOLLOW(F)", 260f, 330f))
            }
            pergunta(tx("follow_l_10"), setOf("o1"), setOf("o2", "o3"), tx("follow_l_10_dica"), tx("follow_l_10_ok")) {
                hot("g2", "g3")
                add(opcao("o1", "*", 120f, 222f), opcao("o2", "+", 180f, 222f), opcao("o3", "id", 240f, 222f))
            }
            pergunta(tx("follow_l_11"), setOf("n1"), setOf("n2", "n3"), tx("follow_l_11_dica"), tx("follow_l_11_ok")) {
                drop("o1", "o2", "o3")
                add(ficha("f1", "*", 224f, 310f, 22f))
                hot("g2")
                add(opcao("n1", "T", 120f, 222f), opcao("n2", "E", 180f, 222f), opcao("n3", "Y", 240f, 222f))
            }
            cena(tx("follow_l_12")) {
                drop("n1", "n2", "n3")
                add(ficha("u1", "+", 70f, 310f, 26f), ficha("u2", ")", 100f, 310f, 26f), ficha("u3", "$", 130f, 310f, 26f))
                hot("u1", "u2", "u3")
            }
            cena(tx("follow_l_13"), mood = EpMood.HAPPY) {
                add(ficha("f2", "+", 248f, 310f, 22f), ficha("f3", ")", 272f, 310f, 22f), ficha("f4", "$", 296f, 310f, 22f))
                hot("f1", "f2", "f3", "f4")
            }
        },
    )
}
