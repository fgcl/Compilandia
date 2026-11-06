package com.compilandia.content.episodes

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

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

val gramaticas by lazy {
    listOf(
        // ─────────────────────────────── DERIVAÇÃO ───────────────────────────────
        episodio(Skill.CFG_DERIVATION, tx("epgramatica_regras_troca")) {
            cena(tx("epgramatica_gramatica_lista_trocas_pode_virar")) {
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
        },

        // ─────────────────────────────── AMBIGUIDADE ───────────────────────────────
        episodio(Skill.AMBIGUITY, tx("epgramatica_duas_arvores_frase")) {
            val mais = no("E", no("E", no("n")), no("+"), no("E", no("E", no("n")), no("*"), no("E", no("n"))))
            val vezes = no("E", no("E", no("E", no("n")), no("+"), no("E", no("n"))), no("*"), no("E", no("n")))
            cena(tx("epgramatica_esta_gramatica_aceita_mais_vezes")) {
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
        },

        // ─────────────────────────────── RECURSÃO À ESQUERDA ───────────────────────────────
        episodio(Skill.LEFT_RECURSION, tx("epgramatica_laco_sem_fim")) {
            cena(tx("epgramatica_olhe_esta_regra_comeca_com")) {
                add(titulo("E -> E + n | n"))
                add(Box("e1", "E", 180f, 130f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
            }
            cena(tx("epgramatica_parser_cima_para_baixo_procura"), mood = EpMood.SURPRISED) {
                add(Box("e2", "E", 180f, 196f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e3", "E", 180f, 262f, 60f, 46f, Look.TOKEN, Tint.ORANGE, size = 20f))
                add(Box("e4", "...", 180f, 328f, 60f, 46f, Look.TOKEN, Tint.RED, size = 20f))
                wire("e1", "e2"); wire("e2", "e3"); wire("e3", "e4")
            }
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
        },

        // ─────────────────────────────── FATORAÇÃO ───────────────────────────────
        episodio(Skill.LEFT_FACTORING, tx("epgramatica_por_em_evidencia")) {
            cena(tx("epgramatica_estas_duas_regras_comecam_igual"), mood = EpMood.THINK) {
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
                add(codigo("b", listOf("A  -> a A'", "A' -> b | c"), 90f, x = 90f, passo = 34f, size = 18f))
                hot("b1")
            }
            cena(tx("epgramatica_com_simbolo_so_olhada_parser"), mood = EpMood.HAPPY) {}
        },

    )
}
