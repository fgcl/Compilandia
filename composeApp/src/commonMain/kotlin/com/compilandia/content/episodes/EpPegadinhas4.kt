package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.Wire
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.pilha
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** A4: os assuntos que não tinham pegadinha. Cada uma ataca o erro mais comum de prova do assunto. */
val pegadinhasNovas by lazy {
    listOf(
        // ─────────────────────────────── MINIMIZAÇÃO ───────────────────────────────
        episodio(Skill.DFA_MINIMIZATION, tx("peg4_min_titulo"), PEGADINHA) {
            cena(tx("peg4_min_01")) {
                add(titulo(tx("peg4_min_titulo")))
                add(estados(listOf(70f to 150f, 180f to 150f, 290f to 150f), finais = setOf(2)))
                wire("q0", "q1", "a"); wire("q1", "q2", "a", bend = -30f); wire("q2", "q1", "a", bend = -30f)
            }
            cena(tx("peg4_min_02")) { hot("q1", "q2") }
            pergunta(tx("peg4_min_03"), setOf("p12"), setOf("p01"), tx("peg4_min_03_dica"), tx("peg4_min_03_ok")) {
                hot()
                add(Box("p01", "q0, q1", 120f, 270f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 16f))
                add(Box("p12", "q1, q2", 240f, 270f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            cena(tx("peg4_min_04")) {
                tint("p12", Tint.RED)
                hot("q0", "q1")
            }
            cena(tx("peg4_min_05")) {
                hot("q1", "q2")
                tint("p01", Tint.RED)
            }
            cena(tx("peg4_min_06"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── ANALISADOR PREDITIVO ───────────────────────────────
        episodio(Skill.LL1_PARSING, tx("peg4_ll_titulo"), PEGADINHA) {
            cena(tx("peg4_ll_01")) {
                add(titulo(tx("peg4_ll_titulo")))
                add(pilha("p", listOf("$", "X", "+"), 80f, 320f, tint = Tint.ORANGE))
                tint("p2", Tint.TEAL)
                add(Box("in", "+  id  $", 250f, 250f, 150f, 44f, Look.CELL, size = 16f))
            }
            cena(tx("peg4_ll_02")) { hot("p1") }
            cena(tx("peg4_ll_03")) { hot("p2", "in") }
            pergunta(tx("peg4_ll_04"), setOf("o1"), setOf("o2", "o3"), tx("peg4_ll_04_dica"), tx("peg4_ll_04_ok")) {
                hot()
                add(Box("o1", tx("peg4_ll_op_casar"), 180f, 92f, 200f, 36f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("o2", tx("peg4_ll_op_tabela"), 180f, 134f, 200f, 36f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("o3", tx("peg4_ll_op_erro"), 180f, 176f, 200f, 36f, Look.TOKEN, Tint.YELLOW, size = 14f))
            }
            cena(tx("peg4_ll_05")) {
                drop("o1", "o2", "o3")
                text("p2", ")"); tint("p2", Tint.RED)
                text("in", "id  $")
                hot("p2", "in")
            }
            cena(tx("peg4_ll_06"), mood = EpMood.HAPPY) { text("p2", "+"); tint("p2", Tint.TEAL); text("in", "+  id  $"); hot() }
        },

        // ─────────────────────────────── AST ───────────────────────────────
        episodio(Skill.AST_BUILD, tx("peg4_ast_titulo"), PEGADINHA) {
            cena(tx("peg4_ast_01")) {
                add(titulo(tx("peg4_ast_titulo")))
                add(Box("src", "( a + b ) * c", 180f, 84f, 220f, 44f, Look.CELL, size = 17f))
            }
            cena(tx("peg4_ast_02")) {
                add(Box("ap", "(", 90f, 170f, 34f, 30f, Look.NODE, Tint.TEAL, size = 14f), Box("fp", ")", 270f, 170f, 34f, 30f, Look.NODE, Tint.TEAL, size = 14f))
                add(Box("ee", "E", 180f, 170f, 40f, 30f, Look.NODE, Tint.ORANGE, size = 14f))
                hot("ap", "fp")
            }
            cena(tx("peg4_ast_03")) {
                drop("ap", "fp", "ee")
                add(Box("n0", "*", 180f, 160f, 40f, 32f, Look.NODE, Tint.ORANGE, size = 16f))
                add(Box("n1", "+", 110f, 230f, 40f, 32f, Look.NODE, Tint.ORANGE, size = 16f), Box("n2", "c", 250f, 230f, 40f, 32f, Look.NODE, Tint.TEAL, size = 16f))
                add(Box("n3", "a", 70f, 300f, 40f, 32f, Look.NODE, Tint.TEAL, size = 16f), Box("n4", "b", 150f, 300f, 40f, 32f, Look.NODE, Tint.TEAL, size = 16f))
                wires(listOf(Wire("n0", "n1", arrow = false), Wire("n0", "n2", arrow = false), Wire("n1", "n3", arrow = false), Wire("n1", "n4", arrow = false)))
                hot("n0", "n1")
            }
            pergunta(tx("peg4_ast_04"), setOf("o1", "o2"), setOf("o3", "o4"), tx("peg4_ast_04_dica"), tx("peg4_ast_04_ok")) {
                hot()
                add(Box("o1", "*", 90f, 372f, 46f, 36f, Look.TOKEN, Tint.YELLOW), Box("o2", "+", 150f, 372f, 46f, 36f, Look.TOKEN, Tint.YELLOW))
                add(Box("o3", "(", 210f, 372f, 46f, 36f, Look.TOKEN, Tint.YELLOW), Box("o4", ")", 270f, 372f, 46f, 36f, Look.TOKEN, Tint.YELLOW))
            }
            cena(tx("peg4_ast_05")) {
                drop("o1", "o2", "o3", "o4")
                hot("n1", "n0")
            }
            cena(tx("peg4_ast_06"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },
    )
}
