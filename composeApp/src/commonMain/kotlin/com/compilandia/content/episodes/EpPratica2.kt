package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PRATICA
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Três aulas de prática que faltavam nas aulas animadas: rodar uma entrada com a tabela SLR (pilha de estados,
 * ACTION e GOTO), o if e o while em três endereços (rótulos e desvios) e o atributo herdado.
 * A tabela e os passos do parser são conferidos contra o motor em PraticaNovaFactsTest.
 */

/** A tabela SLR(1) completa de E -> E + n | n: ACTION nas colunas n, + e $, GOTO na coluna E. */
val TABELA_SLR_PRATICA = listOf(
    listOf("", "n", "+", "$", "E"),
    listOf("0", "s2", "", "", "1"),
    listOf("1", "", "s3", "ok", ""),
    listOf("2", "", "r2", "r2", ""),
    listOf("3", "s4", "", "", ""),
    listOf("4", "", "r1", "r1", ""),
)

/** A pilha de estados antes de cada passo, na entrada n + n. */
val PILHAS_SLR_PRATICA = listOf("0", "0 2", "0 1", "0 1 3", "0 1 3 4", "0 1")

/** A gramática do "torne LL(1)": recursão à esquerda em L e prefixo comum em C. Conferida em AulasConsertadasTest. */
val GRAMATICA_TORNE_LL1 = listOf("L -> L ; C | C", "C -> id = E | id ( )", "E -> n")

/** As linhas de uma gramática do motor, com -> no lugar da seta. */
internal fun linhasDaGramatica(g: com.compilandia.engine.Grammar) = g.toText().lines().map { it.replace("→", "->") }

/** O if com else em três endereços. */
val TAC_IF = listOf("    t1 = a < b", "    ifFalse t1 goto L1", "    x = 1", "    goto L2", "L1: x = 2", "L2:")

/** O while em três endereços. */
val TAC_WHILE = listOf("L1: t1 = i < n", "    ifFalse t1 goto L2", "    i = i + 1", "    goto L1", "L2:")

private fun tecla(id: String, t: String, x: Float, y: Float, w: Float = 150f) = Box(id, t, x, y, w, 40f, Look.TOKEN, Tint.YELLOW, size = 14f)

val praticasNovas by lazy {
    listOf(
        // ─────────────────────────────── PARSER LR COM A TABELA ───────────────────────────────
        episodio(Skill.SLR_TABLE, tx("lrp_titulo"), PRATICA) {
            fun estado(k: Int, entrada: String) {
                text("pi", tx("lrp_pilha", PILHAS_SLR_PRATICA[k]))
                text("en", tx("lrp_entrada", entrada))
            }
            cena(tx("lrp_01")) {
                add(titulo("n + n"))
                add(tabela("m", TABELA_SLR_PRATICA, 66f, cw = 58f, ch = 30f, size = 13f))
                add(rotulo("rg", "1: E -> E + n    2: E -> n", 180f, 252f, Tint.YELLOW, 14f))
                hot("ml0c1", "ml0c2", "ml0c3")
            }
            cena(tx("lrp_02")) {
                add(Box("pi", "", 180f, 290f, 300f, 30f, Look.CELL, size = 14f), Box("en", "", 180f, 326f, 300f, 30f, Look.CELL, size = 14f))
                estado(0, "n + n $")
                hot("pi")
            }
            pergunta(tx("lrp_03"), setOf("o1"), setOf("o2"), tx("lrp_03_dica"), tx("lrp_03_ok")) {
                hot("ml1c1")
                add(tecla("o1", tx("lrp_op_empilha"), 96f, 372f, 160f), tecla("o2", tx("lrp_op_reduz"), 266f, 372f, 160f))
            }
            cena(tx("lrp_04")) {
                drop("o1", "o2")
                estado(1, "+ n $")
                hot("ml3c2")
            }
            pergunta(tx("lrp_05"), setOf("ml1c4"), setOf("ml1c1", "ml3c4", "ml2c4"), tx("lrp_05_dica"), tx("lrp_05_ok")) {
                text("pi", tx("lrp_pilha", "0"))
                hot("pi")
            }
            cena(tx("lrp_06")) {
                estado(4, "$")
                hot("ml2c2", "ml4c1")
            }
            pergunta(tx("lrp_07"), setOf("q3"), setOf("q1", "q2"), tx("lrp_07_dica"), tx("lrp_07_ok")) {
                hot("ml5c3")
                add(tecla("q1", "1", 110f, 372f, 56f), tecla("q2", "2", 180f, 372f, 56f), tecla("q3", "3", 250f, 372f, 56f))
            }
            cena(tx("lrp_08"), mood = EpMood.HAPPY) {
                drop("q1", "q2", "q3")
                estado(5, "$")
                hot("ml2c3")
            }
            cena(tx("lrp_09"), who = PROF) { hot() }
        },

        // ─────────────────────────────── IF E WHILE EM TRÊS ENDEREÇOS ───────────────────────────────
        episodio(Skill.TAC, tx("desv_titulo"), PRATICA) {
            cena(tx("desv_01"), mood = EpMood.THINK) {
                add(titulo("if / while"))
                add(codigo("s", listOf("if (a < b) x = 1;", "else x = 2;"), 70f, x = 70f, size = 15f))
            }
            cena(tx("desv_02")) {
                add(rotulo("p1", tx("desv_p_rotulo"), 180f, 160f, Tint.YELLOW, 15f), rotulo("p2", tx("desv_p_desvio"), 180f, 190f, Tint.YELLOW, 15f))
            }
            cena(tx("desv_03")) {
                drop("p1", "p2")
                add(codigo("t", TAC_IF.take(2), 150f, x = 60f, size = 14f))
                hot("t0", "t1")
            }
            pergunta(tx("desv_04"), setOf("o1"), setOf("o2"), tx("desv_04_dica"), tx("desv_04_ok")) {
                hot("t1")
                add(tecla("o1", "x = 1", 110f, 340f, 110f), tecla("o2", "x = 2", 250f, 340f, 110f))
            }
            pergunta(tx("desv_05"), setOf("q1"), setOf("q2", "q3"), tx("desv_05_dica"), tx("desv_05_ok")) {
                drop("o1", "o2")
                add(codigo("u", TAC_IF.subList(2, 3), 200f, x = 60f, size = 14f))
                hot("u0")
                add(tecla("q1", "goto L2", 70f, 340f, 100f), tecla("q2", "L1:", 180f, 340f, 100f), tecla("q3", tx("desv_op_nada"), 290f, 340f, 100f))
            }
            cena(tx("desv_06")) {
                drop("q1", "q2", "q3", "u0")
                dropAll("t")
                add(codigo("t", TAC_IF, 150f, x = 60f, size = 14f))
                hot("t4", "t5")
            }
            cena(tx("desv_07")) {
                clear()
                add(titulo("while"))
                add(codigo("s", listOf("while (i < n) i = i + 1;"), 70f, x = 50f, size = 15f))
                add(codigo("w", TAC_WHILE, 130f, x = 60f, size = 14f))
                hot("w0")
            }
            pergunta(tx("desv_08"), setOf("w0"), setOf("w1", "w4"), tx("desv_08_dica"), tx("desv_08_ok")) { hot("w3") }
            cena(tx("desv_09"), mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── ATRIBUTO HERDADO ───────────────────────────────
        episodio(Skill.ATTRIBUTES_TYPES, tx("her_titulo"), PRATICA) {
            fun no(id: String, t: String, x: Float, y: Float, folha: Boolean = false) =
                Box(id, t, x, y, maxOf(34f, t.length * 11f + 12f), 30f, Look.NODE, if (folha) Tint.TEAL else Tint.ORANGE, size = 14f)
            cena(tx("her_01"), mood = EpMood.THINK) {
                add(titulo("int a, b"))
                add(no("D", "D", 180f, 80f), no("T", "T", 90f, 140f), no("L", "L", 250f, 140f))
                add(no("ti", "int", 90f, 200f, true), no("L1", "L", 200f, 200f), no("vg", ",", 255f, 200f, true), no("b", "id b", 315f, 200f, true))
                add(no("a", "id a", 200f, 260f, true))
                for ((p, f) in listOf("D" to "T", "D" to "L", "T" to "ti", "L" to "L1", "L" to "vg", "L" to "b", "L1" to "a")) wire(p, f, arrow = false)
            }
            cena(tx("her_02")) {
                add(rotulo("at", "T.tipo = int", 62f, 108f, Tint.YELLOW, 14f))
                hot("T", "ti")
            }
            pergunta(tx("her_03"), setOf("o2"), setOf("o1"), tx("her_03_dica"), tx("her_03_ok")) {
                hot("a", "b")
                add(tecla("o1", tx("her_op_sobe"), 110f, 340f, 110f), tecla("o2", tx("her_op_desce"), 250f, 340f, 110f))
            }
            cena(tx("her_04")) {
                drop("o1", "o2")
                add(rotulo("al", "L.her = int", 316f, 112f, Tint.YELLOW, 14f), rotulo("al1", "L.her = int", 112f, 246f, Tint.YELLOW, 14f))
                hot("L", "L1")
            }
            pergunta(tx("her_05"), setOf("p1"), setOf("p2"), tx("her_05_dica"), tx("her_05_ok")) {
                hot("a", "b")
                add(tecla("p1", tx("her_op_anota"), 96f, 340f, 160f), tecla("p2", tx("her_op_soma"), 266f, 340f, 160f))
            }
            cena(tx("her_06")) {
                drop("p1", "p2")
                add(Box("ts", "a: int    b: int", 180f, 330f, 240f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
                hot("ts")
            }
            cena(tx("her_07"), who = PROF) { hot() }
            pergunta(tx("her_08"), setOf("q2"), setOf("q1"), tx("her_08_dica"), tx("her_08_ok")) {
                drop("ts")
                add(tecla("q1", tx("her_op_s"), 96f, 340f, 160f), tecla("q2", tx("her_op_l"), 266f, 340f, 160f))
            }
            cena(tx("her_09"), mood = EpMood.HAPPY) { drop("q1", "q2") }
        },
        // ─────────────────────────────── TORNE LL(1): RECURSÃO, DEPOIS FATORAÇÃO ───────────────────────────────
        // Exercício integrado da Ponte (revisor professor do bloco 2): o motor faz cada transformação.
        episodio(Skill.LEFT_FACTORING, tx("tll_titulo"), PRATICA) {
            val g0 = com.compilandia.engine.Grammar.parse(GRAMATICA_TORNE_LL1.joinToString("\n"))
            val g1 = com.compilandia.engine.GrammarTransform.eliminateImmediateLeftRecursion(g0)
            val g2 = com.compilandia.engine.GrammarTransform.leftFactor(g1)
            val l1 = linhasDaGramatica(g1)
            val l2 = linhasDaGramatica(g2)
            val linhaC = "g${l1.indexOfFirst { it.startsWith("C ") }}"
            val casasComConflito = com.compilandia.engine.LL1Table(g2).conflicts.size
            cena(tx("tll_01"), mood = EpMood.THINK) {
                add(titulo(tx("tll_titulo"), size = 22f))
                add(codigo("g", GRAMATICA_TORNE_LL1, 80f, x = 70f, passo = 34f, size = 17f))
            }
            pergunta(tx("tll_02"), setOf("g0"), setOf("g1", "g2"), tx("tll_02_dica"), tx("tll_02_ok")) {}
            cena(tx("tll_03")) {
                dropAll("g")
                add(codigo("g", l1, 80f, x = 50f, passo = 34f, size = 16f))
                add(rotulo("passo", tx("tll_passo1"), 180f, 260f, Tint.TEAL, 15f))
            }
            pergunta(tx("tll_04"), setOf(linhaC), l1.indices.map { "g$it" }.toSet() - linhaC, tx("tll_04_dica"), tx("tll_04_ok")) { drop("passo") }
            cena(tx("tll_05")) {
                dropAll("g")
                add(codigo("g", l2, 80f, x = 50f, passo = 34f, size = 16f))
                add(rotulo("passo", tx("tll_passo2"), 180f, 300f, Tint.TEAL, 15f))
            }
            val certa = "k$casasComConflito"
            pergunta(tx("tll_06"), setOf(certa), setOf("k0", "k1", "k2") - certa, tx("tll_06_dica"), tx("tll_06_ok")) {
                drop("passo")
                add(
                    Box("k0", tx("tll_nenhuma"), 90f, 360f, 100f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f),
                    Box("k1", tx("tll_uma"), 180f, 360f, 70f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f),
                    Box("k2", tx("tll_duas"), 270f, 360f, 90f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f),
                )
            }
            cena(tx("tll_07"), mood = EpMood.HAPPY) { drop("k0", "k1", "k2") }
        },
    )
}
