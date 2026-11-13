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
import com.compilandia.content.fileira
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ParseNode
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Aulas básicas de gramática. As quatro viraram aulas longas (A9), no molde de EpLongas.kt:
 * por que existe, o primeiro exemplo (as cenas da aula curta), a receita à vista e um segundo exemplo
 * em que a pessoa calcula. O que o segundo exemplo afirma é conferido contra o motor em A9GramaticaFactsTest.
 */

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

/** Segundo exemplo da derivação: uma declaração em C. */
val GRAMATICA_CFG_2 = listOf("Decl -> Tipo Var ;", "Tipo -> int | float", "Var -> id | * id")
/** A frase que a pessoa deriva no segundo exemplo, e a árvore dela. */
val FRASE_CFG_2 = listOf("float", "*", "id", ";")
val ARVORE_CFG_2 = no("Decl", no("Tipo", no("float")), no("Var", no("*"), no("id")), no(";"))
/** A frase que fica de fora da linguagem. */
val FRASE_CFG_FORA = listOf("int", "id", "id", ";")

/** Segundo exemplo da ambiguidade: só o menos. */
val GRAMATICA_AMB_2 = listOf("E -> E - E | n")
/** A mesma gramática, consertada: à direita do menos, só um n. */
val GRAMATICA_AMB_CONSERTO = listOf("E -> E - n | n")
/** As duas árvores de n - n - n: o menos da esquerda primeiro, e o da direita primeiro. */
val ARVORE_AMB_ESQ = no("E", no("E", no("E", no("n")), no("-"), no("E", no("n"))), no("-"), no("E", no("n")))
val ARVORE_AMB_DIR = no("E", no("E", no("n")), no("-"), no("E", no("E", no("n")), no("-"), no("E", no("n"))))
/** A única árvore de n - n - n na gramática consertada. */
val ARVORE_AMB_CONSERTO = no("E", no("E", no("E", no("n")), no("-"), no("n")), no("-"), no("n"))

/** Segundo exemplo da recursão à esquerda: uma lista de ids, e ela consertada. */
val GRAMATICA_REC_2 = listOf("L -> L , id | id")
val GRAMATICA_REC_2_DEPOIS = listOf("L  -> id L'", "L' -> , id L' | ε")
/** Caso difícil da recursão: duas alternativas com laço, cada uma vira uma alternativa de E'. */
val GRAMATICA_REC_3 = listOf("E -> E + T | E - T | T")
val GRAMATICA_REC_3_DEPOIS = listOf("E  -> T E'", "E' -> + T E' | - T E' | ε")

/** Segundo exemplo da fatoração: atribuição e duas chamadas, todas começando com id. */
val GRAMATICA_FAT_2 = listOf("C -> id = n | id ( ) | id ( n )")
/** Depois da primeira rodada, e depois da segunda. */
val GRAMATICA_FAT_2_RODADA1 = listOf("C  -> id C'", "C' -> = n | ( ) | ( n )")
val GRAMATICA_FAT_2_DEPOIS = listOf("C   -> id C'", "C'  -> = n | ( C''", "C'' -> ) | n )")

private fun bloco(id: String, t: String, x: Float, y: Float, w: Float = 80f, size: Float = 15f) =
    Box(id, t, x, y, w, 42f, Look.TOKEN, Tint.YELLOW, size = size)

val gramaticas by lazy {
    listOf(
        // ─────────────────────────────── DERIVAÇÃO ───────────────────────────────
        episodio(Skill.CFG_DERIVATION, tx("epgramatica_regras_troca")) {
            // 1. Por que existe
            cena(tx("cfg_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("cfg_l_titulo_porque")))
                add(Box("f1", "n + n", 180f, 130f, 170f, 50f, Look.CELL, size = 20f))
                add(Box("f2", "n + +", 180f, 200f, 170f, 50f, Look.CELL, size = 20f))
            }
            pergunta(tx("cfg_l_02"), setOf("f2"), setOf("f1"), tx("cfg_l_02_dica"), tx("cfg_l_02_ok")) {}
            cena(tx("cfg_l_03")) {
                tint("f1", Tint.TEAL); tint("f2", Tint.RED)
                add(rotulo("mais", "...", 180f, 260f, size = 30f))
            }
            palavra(tx("cfg_l_palavra"), tx("cfg_l_04"))
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epgramatica_gramatica_lista_trocas_pode_virar")) {
                clear()
                add(titulo(tx("epgramatica_gramatica")))
                add(codigo("g", listOf("E -> E + E", "E -> n"), 74f, x = 90f, passo = 30f, size = 17f))
            }
            cena(tx("epgramatica_letra_maiuscula_nao_terminal_ainda")) {
                add(fileira("w", listOf("E" to Tint.ORANGE), 200f, w = 44f))
                hot("w0")
            }
            cena(tx("epgramatica_troco_pela_primeira_regra")) {
                drop("w0")
                add(fileira("w", listOf("E" to Tint.ORANGE, "+" to Tint.TEAL, "E" to Tint.ORANGE), 200f, w = 44f))
                hot("g0")
            }
            pergunta(
                tx("epgramatica_numa_derivacao_mais_esquerda_qual"),
                setOf("w0"), setOf("w2"),
                tx("epgramatica_nome_ja_conta_mais_esquerda"),
                tx("epgramatica_esse_mesmo_sempre_nao_terminal"),
            ) { hot() }
            cena(tx("epgramatica_ele_vira_n_depois_troco")) {
                drop("w0", "w1", "w2")
                add(fileira("w", listOf("n" to Tint.TEAL, "+" to Tint.TEAL, "n" to Tint.TEAL), 200f, w = 44f))
                hot("g1")
            }
            cena(tx("epgramatica_so_sobrou_terminal_frase_pertence"), mood = EpMood.HAPPY) {
                dropAll("w"); hot()
                val (b, w) = arvore(no("E", no("E", no("n")), no("+"), no("E", no("n"))), 170f, passoY = 58f, largura = 240f)
                add(b); for (x in w) wire(x)
            }
            // 3. A receita
            cena(tx("cfg_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("cfg_l_r1"), 110f), regra("r2", tx("cfg_l_r2"), 166f), regra("r3", tx("cfg_l_r3"), 222f))
            }
            pergunta(tx("cfg_l_06"), setOf("r2"), setOf("r1", "r3"), tx("cfg_l_06_dica"), tx("cfg_l_06_ok")) {
                add(codigo("q", listOf("E + E"), 300f, x = 150f, size = 17f))
            }
            // 4. Segundo exemplo, com perguntas
            cena(tx("cfg_l_07")) {
                clear()
                add(titulo(FRASE_CFG_2.joinToString(" ")))
                add(codigo("g", GRAMATICA_CFG_2, 70f, x = 90f, passo = 28f, size = 15f))
                add(fileira("w", listOf("Decl" to Tint.ORANGE), 200f, w = 64f))
            }
            pergunta(tx("cfg_l_08"), setOf("w0"), setOf("w1", "w2"), tx("cfg_l_08_dica"), tx("cfg_l_08_ok")) {
                drop("w0")
                add(fileira("w", listOf("Tipo" to Tint.ORANGE, "Var" to Tint.ORANGE, ";" to Tint.TEAL), 200f, w = 64f))
                hot("g0")
            }
            pergunta(tx("cfg_l_09"), setOf("o2"), setOf("o1"), tx("cfg_l_09_dica"), tx("cfg_l_09_ok")) {
                hot("g1", "w0")
                add(bloco("o1", "Tipo -> int", 100f, 290f, w = 150f, size = 14f), bloco("o2", "Tipo -> float", 260f, 290f, w = 150f, size = 14f))
            }
            pergunta(tx("cfg_l_10"), setOf("o2"), setOf("o1"), tx("cfg_l_10_dica"), tx("cfg_l_10_ok")) {
                dropAll("w")
                add(fileira("w", listOf("float" to Tint.TEAL, "Var" to Tint.ORANGE, ";" to Tint.TEAL), 200f, w = 64f))
                hot("g2", "w1")
                text("o1", "Var -> id"); text("o2", "Var -> * id")
            }
            cena(tx("cfg_l_11"), mood = EpMood.HAPPY) {
                drop("o1", "o2"); dropAll("w"); hot()
                val (b, w) = arvore(ARVORE_CFG_2, 170f, passoY = 58f, largura = 300f)
                add(b); for (x in w) wire(x)
            }
            pergunta(tx("cfg_l_12"), setOf("nao"), setOf("sim"), tx("cfg_l_12_dica"), tx("cfg_l_12_ok")) {
                dropAll("t"); clearWires()
                add(Box("f", FRASE_CFG_FORA.joinToString(" "), 180f, 220f, 200f, 50f, Look.CELL, size = 18f))
                add(opcao("sim", tx("comum_sim"), 140f, 300f), opcao("nao", tx("comum_nao_2"), 220f, 300f))
            }
            cena(tx("cfg_l_13"), who = PROF) {
                drop("sim", "nao"); tint("f", Tint.RED)
            }
            fecho(Fecho(
                tx("cfg_f_af"), false, tx("cfg_f_q"), tx("cfg_f_dica"), tx("cfg_f_ok"), tx("cfg_f_certo"),
                tx("cfg_f_erro2"), tx("cfg_f_certo2"), tx("cfg_f_fala2"),
                listOf(tx("cfg_f_z1"), tx("cfg_f_z2"), tx("cfg_f_z3")), tx("cfg_f_resumo"),
            ))
        },

        // ─────────────────────────────── AMBIGUIDADE ───────────────────────────────
        episodio(Skill.AMBIGUITY, tx("epgramatica_duas_arvores_frase")) {
            val mais = no("E", no("E", no("n")), no("+"), no("E", no("E", no("n")), no("*"), no("E", no("n"))))
            val vezes = no("E", no("E", no("E", no("n")), no("+"), no("E", no("n"))), no("*"), no("E", no("n")))
            // 1. Por que existe
            cena(tx("amb_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("amb_l_titulo_porque")))
                add(Box("conta", "2 + 3 * 4", 180f, 150f, 200f, 56f, Look.CELL, size = 22f))
            }
            pergunta(tx("amb_l_02"), setOf("k2"), setOf("k1", "k3"), tx("amb_l_02_dica"), tx("amb_l_02_ok")) {
                add(opcao("k1", "14", 110f, 260f), opcao("k2", "20", 180f, 260f), opcao("k3", "24", 250f, 260f))
            }
            cena(tx("amb_l_03")) {
                drop("k1", "k3"); tint("k2", Tint.RED)
            }
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epgramatica_esta_gramatica_aceita_mais_vezes")) {
                clear()
                add(titulo("n + n * n"))
                add(codigo("g", listOf("E -> E + E | E * E | n"), 66f, x = 60f, size = 16f))
            }
            cena(tx("epgramatica_nesta_arvore_vezes_fica_embaixo")) {
                val (b, w) = arvore(mais, 112f, passoY = 50f, largura = 300f)
                add(b); for (x in w) wire(x)
            }
            cena(tx("epgramatica_mas_para_montar_outra_arvore"), mood = EpMood.SURPRISED) {
                dropAll("t")
                val (b, w) = arvore(vezes, 112f, passoY = 50f, largura = 300f, prefix = "u")
                add(b); for (x in w) wire(x)
            }
            cena(tx("epgramatica_mesma_frase_duas_arvores_isso"), who = PROF) { hot("u") }
            pergunta(
                tx("epgramatica_nesta_arvore_que_roda_primeiro"),
                setOf("m"), setOf("v"),
                tx("epgramatica_quem_esta_mais_fundo_na"),
                tx("epgramatica_certo_aqui_mais_roda_primeiro"),
            ) {
                hot()
                add(Box("m", tx("epgramatica_o"), 110f, 374f, 110f, 42f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("v", tx("epgramatica_o_2"), 250f, 374f, 110f, 42f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena(tx("epgramatica_compilador_nao_pode_ter_duvida"), mood = EpMood.HAPPY) { drop("m", "v") }
            // 3. A receita
            cena(tx("amb_l_04")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("amb_l_r1"), 110f), regra("r2", tx("amb_l_r2"), 166f), regra("r3", tx("amb_l_r3"), 222f))
            }
            pergunta(tx("amb_l_05"), setOf("nao"), setOf("sim"), tx("amb_l_05_dica"), tx("amb_l_05_ok")) {
                hot("r3")
                add(opcao("sim", tx("comum_sim"), 140f, 300f), opcao("nao", tx("comum_nao_2"), 220f, 300f))
            }
            // 4. Segundo exemplo, com perguntas
            cena(tx("amb_l_06")) {
                clear()
                add(titulo("n - n - n"))
                add(codigo("g", GRAMATICA_AMB_2, 66f, x = 110f, size = 16f))
            }
            pergunta(tx("amb_l_07"), setOf("t01"), setOf("t1"), tx("epgramatica_quem_esta_mais_fundo_na"), tx("amb_l_07_ok")) {
                val (b, w) = arvore(ARVORE_AMB_ESQ, 112f, passoY = 50f, largura = 300f)
                add(b); for (x in w) wire(x)
            }
            pergunta(tx("amb_l_08"), setOf("u21"), setOf("u1"), tx("epgramatica_quem_esta_mais_fundo_na"), tx("amb_l_08_ok")) {
                dropAll("t")
                val (b, w) = arvore(ARVORE_AMB_DIR, 112f, passoY = 50f, largura = 300f, prefix = "u")
                add(b); for (x in w) wire(x)
            }
            pergunta(tx("amb_l_09"), setOf("k2"), setOf("k1", "k3"), tx("amb_l_09_dica"), tx("amb_l_09_ok")) {
                hot("u21")
                add(rotulo("val", "9 - 5 - 2", 180f, 322f, Tint.YELLOW, 18f))
                add(opcao("k1", "2", 110f, 374f), opcao("k2", "6", 180f, 374f), opcao("k3", "16", 250f, 374f))
            }
            cena(tx("amb_l_10"), who = PROF) {
                drop("k1", "k3"); hot("k2")
            }
            cena(tx("amb_l_11")) {
                clear()
                add(titulo("n - n - n"))
                add(codigo("g", GRAMATICA_AMB_2, 66f, x = 110f, size = 16f))
                add(rotulo("d", tx("comum_depois"), 180f, 120f, Tint.YELLOW, 20f))
                add(codigo("h", GRAMATICA_AMB_CONSERTO, 160f, x = 110f, size = 16f))
                hot("h0")
            }
            pergunta(tx("amb_l_12"), setOf("k1"), setOf("k2"), tx("amb_l_12_dica"), tx("amb_l_12_ok")) {
                add(opcao("k1", "1", 150f, 260f), opcao("k2", "2", 210f, 260f))
            }
            cena(tx("amb_l_13"), mood = EpMood.HAPPY) {
                clear()
                add(titulo("n - n - n"))
                add(codigo("h", GRAMATICA_AMB_CONSERTO, 66f, x = 110f, size = 16f))
                val (b, w) = arvore(ARVORE_AMB_CONSERTO, 120f, passoY = 52f, largura = 300f)
                add(b); for (x in w) wire(x)
            }
            fecho(Fecho(
                tx("amb_f_af"), false, tx("amb_f_q"), tx("amb_f_dica"), tx("amb_f_ok"), tx("amb_f_certo"),
                tx("amb_f_erro2"), tx("amb_f_certo2"), tx("amb_f_fala2"),
                listOf(tx("amb_f_z1"), tx("amb_f_z2"), tx("amb_f_z3")), tx("amb_f_resumo"),
            ))
        },

        // ─────────────────────────────── RECURSÃO À ESQUERDA ───────────────────────────────
        episodio(Skill.LEFT_RECURSION, tx("epgramatica_laco_sem_fim")) {
            // 1. Por que existe
            cena(tx("rec_l_01")) {
                add(titulo("E -> E + n | n"))
                add(codigo("c", listOf("void E(void) {", "    E();", "    casa('+');", "    casa(NUM);", "}"), 100f, x = 90f, passo = 32f, size = 16f))
            }
            pergunta(tx("rec_l_02"), setOf("c1"), setOf("c2", "c3"), tx("rec_l_02_dica"), tx("rec_l_02_ok")) {}
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epgramatica_olhe_esta_regra_comeca_com")) {
                clear()
                add(titulo("E -> E + n | n"))
                add(Box("e1", "E", 180f, 130f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
            }
            cena(tx("epgramatica_parser_cima_para_baixo_procura"), mood = EpMood.SURPRISED) {
                add(Box("e2", "E", 180f, 196f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e3", "E", 180f, 262f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e4", "...", 180f, 328f, 60f, 46f, Look.TOKEN, Tint.RED, size = 20f))
                wire("e1", "e2"); wire("e2", "e3"); wire("e3", "e4")
            }
            palavra(tx("rec_l_palavra"), tx("rec_l_03"))
            pergunta(
                tx("epgramatica_qual_destas_regras_tem_recursao"),
                setOf("r0"), setOf("r1", "r2"),
                tx("epgramatica_procure_regra_cujo_lado_direito"),
                tx("epgramatica_isso_comeca_com_entao_parser"),
            ) {
                clear()
                add(titulo(tx("epgramatica_qual_trava")))
                add(Box("r0", "A -> A b", 180f, 100f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
                add(Box("r1", "A -> b A", 180f, 170f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
                add(Box("r2", "A -> c", 180f, 240f, 170f, 50f, Look.TOKEN, Tint.NONE, size = 17f))
            }
            cena(tx("epgramatica_conserto_parte_sem_recursao_vem")) {
                clear()
                add(titulo(tx("comum_antes")))
                add(codigo("a", listOf("E -> E + n | n"), 70f, x = 80f, size = 17f))
                add(rotulo("d", tx("comum_depois"), 180f, 130f, Tint.YELLOW, 22f))
                add(codigo("b", listOf("E  -> n E'", "E' -> + n E' | ε"), 160f, x = 80f, passo = 32f, size = 17f))
            }
            cena(tx("epgramatica_linguagem_mesma_n_depois_zero"), mood = EpMood.HAPPY) { hot("b1") }
            // A árvore muda junto. Com o menos, a árvore nova lida ao pé da letra daria a conta errada (os dois revisores do bloco 2).
            val (velha, fiosVelha) = arvore(ARVORE_MENOS_VELHA, 120f, passoY = 52f, largura = 300f, prefix = "v")
            val (nova, fiosNova) = arvore(ARVORE_MENOS_NOVA, 104f, passoY = 46f, largura = 320f, prefix = "w")
            cena(tx("rec_l_assoc_1"), mood = EpMood.THINK) {
                clear()
                add(titulo("E -> E - n | n", size = 20f))
                add(velha); wires(fiosVelha)
                add(rotulo("cv", "(9 - 5) - 2 = 2", 180f, 344f, Tint.GREEN, 17f))
                hot("v0")
            }
            cena(tx("rec_l_assoc_2"), mood = EpMood.SURPRISED) {
                clear()
                add(titulo("E -> n E'   E' -> - n E' | ε", size = 17f))
                add(nova); wires(fiosNova)
                add(rotulo("cn", "9 - (5 - 2) = 6 ?", 180f, 344f, Tint.RED, 17f))
                hot("w1")
            }
            pergunta(tx("rec_l_assoc_3"), setOf("sim0"), setOf("sim1"), tx("rec_l_assoc_3_dica"), tx("rec_l_assoc_3_ok")) {
                drop("cn")
                add(Box("sim0", tx("rec_l_assoc_nao"), 110f, 344f, 100f, 42f, Look.TOKEN, Tint.YELLOW, size = 16f))
                add(Box("sim1", tx("rec_l_assoc_sim"), 250f, 344f, 100f, 42f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            // 3. A receita
            cena(tx("rec_l_04")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("rec_l_r1"), 100f), regra("r2", tx("rec_l_r2"), 156f), regra("r3", tx("rec_l_r3"), 212f))
                add(codigo("q", listOf("A -> A α | β"), 290f, x = 120f, size = 17f))
            }
            pergunta(tx("rec_l_05"), setOf("o2"), setOf("o1"), tx("rec_l_05_dica"), tx("rec_l_05_ok")) {
                hot("r1")
                text("q0", "E -> E + n | n")
                add(bloco("o1", "+ n", 125f, 350f), bloco("o2", "n", 235f, 350f))
            }
            // 4. Segundo exemplo, com perguntas
            cena(tx("rec_l_06")) {
                clear()
                add(titulo(tx("rec_l_titulo_lista")))
                add(codigo("g", GRAMATICA_REC_2, 74f, x = 100f, size = 17f))
                add(Box("a1", "L , id", 115f, 150f, 104f, 44f, Look.TOKEN, Tint.NONE, size = 16f))
                add(Box("a2", "id", 245f, 150f, 104f, 44f, Look.TOKEN, Tint.NONE, size = 16f))
            }
            pergunta(tx("rec_l_07"), setOf("a1"), setOf("a2"), tx("rec_l_07_dica"), tx("rec_l_07_ok")) { hot("g0") }
            pergunta(tx("rec_l_08"), setOf("b1"), setOf("b2", "b3"), tx("rec_l_08_dica"), tx("rec_l_08_ok")) {
                tint("a1", Tint.ORANGE); tint("a2", Tint.TEAL); hot()
                add(bloco("b1", "L -> id L'", 65f, 230f, w = 110f, size = 13f))
                add(bloco("b2", "L -> L' id", 180f, 230f, w = 110f, size = 13f))
                add(bloco("b3", "L -> , id L'", 295f, 230f, w = 110f, size = 13f))
            }
            pergunta(tx("rec_l_09"), setOf("c1", "c2"), setOf("c3", "c4"), tx("rec_l_09_dica"), tx("rec_l_09_ok")) {
                drop("b1", "b2", "b3")
                add(codigo("r", GRAMATICA_REC_2_DEPOIS.take(1), 214f, x = 100f, size = 17f))
                add(bloco("c1", ", id L'", 54f, 300f, w = 76f, size = 14f), bloco("c2", "ε", 138f, 300f, w = 76f, size = 14f))
                add(bloco("c3", "L , id", 222f, 300f, w = 76f, size = 14f), bloco("c4", "id L'", 306f, 300f, w = 76f, size = 14f))
            }
            cena(tx("rec_l_10")) {
                drop("c1", "c2", "c3", "c4")
                add(codigo("r", GRAMATICA_REC_2_DEPOIS, 214f, x = 100f, passo = 32f, size = 17f))
                hot("r0", "r1")
            }
            pergunta(tx("rec_l_11"), setOf("sim"), setOf("nao"), tx("rec_l_11_dica"), tx("rec_l_11_ok")) {
                hot()
                add(Box("f", "id , id", 180f, 310f, 140f, 40f, Look.CELL, size = 17f))
                add(opcao("sim", tx("comum_sim"), 140f, 370f), opcao("nao", tx("comum_nao_2"), 220f, 370f))
            }
            cena(tx("rec_l_12"), mood = EpMood.HAPPY) {
                drop("sim", "nao"); tint("f", Tint.TEAL)
                hot("r0")
            }
            // 5. O caso difícil: dois alfas na mesma regra
            cena(tx("rec_l_13")) {
                clear()
                add(titulo(tx("rec_l_titulo_dois")))
                add(codigo("g", GRAMATICA_REC_3, 74f, x = 70f, size = 17f))
                add(bloco("a1", "E + T", 80f, 150f, w = 90f, size = 16f), bloco("a2", "E - T", 180f, 150f, w = 90f, size = 16f), bloco("a3", "T", 280f, 150f, w = 90f, size = 16f))
                hot("g0")
            }
            pergunta(tx("rec_l_14"), setOf("a1", "a2"), setOf("a3"), tx("rec_l_14_dica"), tx("rec_l_14_ok")) { hot("g0") }
            pergunta(tx("rec_l_15"), setOf("d1"), setOf("d2", "d3"), tx("rec_l_15_dica"), tx("rec_l_15_ok")) {
                tint("a1", Tint.ORANGE); tint("a2", Tint.ORANGE); tint("a3", Tint.TEAL); hot()
                add(codigo("r", GRAMATICA_REC_3_DEPOIS.take(1), 214f, x = 60f, size = 16f))
                // Os dois erros de prova: esquecer o segundo alfa e esquecer o ε.
                add(bloco("d2", "E' -> + T E' | ε", 180f, 262f, w = 280f, size = 15f))
                add(bloco("d1", GRAMATICA_REC_3_DEPOIS[1].replace("  ", " "), 180f, 316f, w = 280f, size = 15f))
                add(bloco("d3", "E' -> + T E' | - T E'", 180f, 370f, w = 280f, size = 15f))
            }
            cena(tx("rec_l_16"), mood = EpMood.HAPPY) {
                drop("d1", "d2", "d3")
                add(codigo("r", GRAMATICA_REC_3_DEPOIS, 214f, x = 60f, passo = 32f, size = 16f))
                hot("r1")
            }
            fecho(Fecho(
                tx("rec_f_af"), false, tx("rec_f_q"), tx("rec_f_dica"), tx("rec_f_ok"), tx("rec_f_certo"),
                tx("rec_f_erro2"), tx("rec_f_certo2"), tx("rec_f_fala2"),
                listOf(tx("rec_f_z1"), tx("rec_f_z2"), tx("rec_f_z3")), tx("rec_f_resumo"),
            ))
        },

        // ─────────────────────────────── FATORAÇÃO ───────────────────────────────
        episodio(Skill.LEFT_FACTORING, tx("epgramatica_por_em_evidencia")) {
            // 1. Por que existe
            cena(tx("fat_l_01"), mood = EpMood.THINK) { escolha("C", "id", "C -> id = n", "C -> id ( )", listOf("C -> id = n | id ( )"), 0) }
            pergunta(tx("fat_l_02"), setOf("nd"), setOf("ra", "rb"), tx("fat_l_02_dica"), tx("fat_l_02_ok")) {
                add(Box("nd", tx("fat_l_nao_da"), 180f, 380f, 210f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("epgramatica_estas_duas_regras_comecam_igual"), mood = EpMood.THINK) {
                clear()
                add(titulo(tx("epgramatica_qual_das_duas")))
                add(fileira("x", listOf("a" to Tint.YELLOW, "b" to Tint.NONE), 110f, w = 60f, h = 46f))
                add(fileira("y", listOf("a" to Tint.YELLOW, "c" to Tint.NONE), 170f, w = 60f, h = 46f))
            }
            pergunta(
                tx("epgramatica_toque_na_parte_que_duas"),
                setOf("x0"), setOf("x1", "y1"),
                tx("epgramatica_compare_comeco_das_duas_linhas"),
                tx("epgramatica_isso_prefixo_comum"),
            ) {}
            cena(tx("epgramatica_faco_como_na_matematica_ponho"), who = PROF) {
                drop("y0")
                move("x0", 120f, 150f)
                move("x1", 240f, 120f)
                move("y1", 240f, 180f)
            }
            cena(tx("epgramatica_escolha_fica_para_depois_ai")) {
                clear()
                add(titulo(tx("epgramatica_fatorada")))
                // O antes fica em cima, apagado, para ver o que mudou (mural, rodada 13).
                add(Box("antes", "A  -> a b | a c", 180f, 92f, 220f, 36f, Look.CELL, Tint.DARK, size = 15f))
                add(codigo("b", listOf("A  -> a A'", "A' -> b | c"), 170f, x = 90f, passo = 34f, size = 18f))
                wire("antes", "b0")
                hot("b1")
            }
            cena(tx("epgramatica_com_simbolo_so_olhada_parser"), mood = EpMood.HAPPY) {}
            // 3. A receita
            cena(tx("fat_l_03")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("fat_l_r1"), 100f), regra("r2", tx("fat_l_r2"), 152f), regra("r3", tx("fat_l_r3"), 204f), regra("r4", tx("fat_l_r4"), 256f))
            }
            pergunta(tx("fat_l_04"), setOf("r3"), setOf("r1", "r2", "r4"), tx("fat_l_04_dica"), tx("fat_l_04_ok")) {}
            // 4. Segundo exemplo, com perguntas
            cena(tx("fat_l_05")) {
                clear()
                add(titulo(tx("fat_l_titulo_comandos")))
                add(codigo("g", GRAMATICA_FAT_2, 72f, x = 40f, size = 15f))
                hot("g0")
            }
            pergunta(tx("fat_l_06"), setOf("p1"), setOf("p2", "p3"), tx("fat_l_06_dica"), tx("fat_l_06_ok")) {
                hot()
                add(bloco("p1", "id", 90f, 150f), bloco("p2", "id =", 180f, 150f), bloco("p3", "id (", 270f, 150f))
            }
            cena(tx("fat_l_07")) {
                drop("p1", "p2", "p3")
                add(codigo("h", GRAMATICA_FAT_2_RODADA1, 150f, x = 70f, passo = 32f, size = 16f))
                hot("h0")
            }
            pergunta(tx("fat_l_08"), setOf("o2", "o3"), setOf("o1"), tx("fat_l_08_dica"), tx("fat_l_08_ok")) {
                hot("h1")
                add(bloco("o1", "= n", 90f, 260f), bloco("o2", "( )", 180f, 260f), bloco("o3", "( n )", 270f, 260f))
            }
            pergunta(tx("fat_l_09"), setOf("q1"), setOf("q2", "q3"), tx("fat_l_09_dica"), tx("fat_l_09_ok")) {
                drop("o1", "o2", "o3")
                add(bloco("q1", "C' -> = n | ( C''", 180f, 250f, w = 230f, size = 14f))
                add(bloco("q2", "C' -> = n | ( ) C''", 180f, 300f, w = 230f, size = 14f))
                add(bloco("q3", "C' -> ( C''", 180f, 350f, w = 230f, size = 14f))
            }
            cena(tx("fat_l_10")) {
                drop("q1", "q2", "q3"); dropAll("h")
                add(codigo("h", GRAMATICA_FAT_2_DEPOIS, 150f, x = 70f, passo = 32f, size = 16f))
                hot("h1", "h2")
            }
            pergunta(tx("fat_l_11"), setOf("k1"), setOf("k2"), tx("fat_l_11_dica"), tx("fat_l_11_ok")) {
                hot("h1")
                add(opcao("k1", "1", 150f, 300f), opcao("k2", "2", 210f, 300f))
            }
            cena(tx("fat_l_12"), mood = EpMood.HAPPY) {
                drop("k1", "k2"); hot()
            }
            fecho(Fecho(
                tx("fat_f_af"), true, tx("fat_f_q"), tx("fat_f_dica"), tx("fat_f_ok"), tx("fat_f_af"),
                tx("fat_f_erro2"), tx("fat_f_certo2"), tx("fat_f_fala2"),
                listOf(tx("fat_f_z1"), tx("fat_f_z2"), tx("fat_f_z3")), tx("fat_f_resumo"),
            ))
        },
    )
}

/** 9 - 5 - 2 em E -> E - n | n: pende para a esquerda, e a conta sai certa, (9 - 5) - 2. */
internal val ARVORE_MENOS_VELHA = ParseNode("E", listOf(
    ParseNode("E", listOf(ParseNode("E", listOf(ParseNode("9"))), ParseNode("-"), ParseNode("5"))),
    ParseNode("-"), ParseNode("2"),
))

/** A mesma frase em E -> n E' e E' -> - n E' | ε: pende para a direita. */
internal val ARVORE_MENOS_NOVA = ParseNode("E", listOf(
    ParseNode("9"),
    ParseNode("E'", listOf(ParseNode("-"), ParseNode("5"), ParseNode("E'", listOf(ParseNode("-"), ParseNode("2"), ParseNode("E'", listOf(ParseNode("ε"))))))),
))
