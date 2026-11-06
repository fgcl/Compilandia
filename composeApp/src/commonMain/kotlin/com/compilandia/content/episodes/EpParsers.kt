package com.compilandia.content.episodes

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

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

val topDown by lazy {
    listOf(
        // ─────────────────────────────── TABELA LL(1) ───────────────────────────────

        // ─────────────────────────────── PREDITIVO ───────────────────────────────
        episodio(Skill.LL1_PARSING, tx("epparsers_pilha_decide")) {
            val n = 4
            cena(tx("epparsers_parser_preditivo_tem_pilha_entrada")) {
                add(titulo("id + id"))
                add(rotulo("rs", "pilha", 74f, 326f), rotulo("ra", tx("epparsers_topo_token_id"), 240f, 200f, Tint.YELLOW, 17f))
                parser(listOf("$", "E"), listOf("id", "+", "id", "$"), n)
                hot("p1", "i0")
            }
            cena(tx("epparsers_topo_nao_terminal_olho_tabela")) {
                parser(listOf("$", "X", "T"), listOf("id", "+", "id", "$"), n)
                text("ra", "E -> T X")
            }
            cena(tx("epparsers_agora_t_esta_no_topo")) {
                parser(listOf("$", "X", "id"), listOf("id", "+", "id", "$"), n)
                text("ra", "T -> id")
                hot("p2", "i0")
            }
            pergunta(
                tx("epparsers_topo_id_token_id_que"),
                setOf("casar"), setOf("expandir"),
                tx("epparsers_terminal_no_topo_igual_ao"),
                tx("epparsers_isso_casou_dois_saem"),
            ) {
                text("ra", tx("epparsers_topo_id_token_id"))
                add(Box("casar", tx("comum_casar_2"), 190f, 260f, 110f, 46f, Look.TOKEN, Tint.TEAL, size = 17f), Box("expandir", tx("comum_expandir"), 310f, 260f, 110f, 46f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena(tx("epparsers_jogo_segue_igual_expande_nao")) {
                drop("casar", "expandir")
                parser(listOf("$", "X"), listOf("+", "id", "$"), n)
                text("ra", tx("epparsers_casou_id"))
                hot()
            }
            cena(tx("epparsers_sobrou_cifrao_na_pilha_cifrao"), mood = EpMood.HAPPY) {
                parser(listOf("$"), listOf("$"), n)
                text("ra", "aceita")
                hot("p0", "i3")
            }
        },

        // ─────────────────────────────── DESCIDA RECURSIVA ───────────────────────────────
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
            cena(tx("epparsers_para_escrever_parser_mao_cada")) {
                add(titulo(tx("comum_descida_recursiva")))
                add(codigo("c", src, 64f, passo = 27f, size = 12f))
            }
            cena(tx("epparsers_regra_pede_t_depois_x")) { hot("c0") }
            cena(tx("epparsers_terminal_vira_casa_ele_confere")) { hot("c3") }
            pergunta(
                tx("epparsers_se_token_nao_for_mais"),
                setOf("r1"), setOf("r0"),
                tx("epparsers_nao_fazer_nada_mesmo_que"),
                tx("epparsers_isso_nao_fazer_nada_regra"),
            ) {
                hot("c2")
                add(Box("r0", "X -> + T X", 110f, 350f, 150f, 44f, Look.TOKEN, Tint.TEAL, size = 15f), Box("r1", "X -> ε", 270f, 350f, 130f, 44f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            cena(tx("epparsers_ifs_usam_first_para_escolher")) {
                drop("r0", "r1")
                hot("c7", "c8")
            }
            cena(tx("epparsers_esse_parser_que_trabalho_pratico"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── RECUPERAÇÃO DE ERROS ───────────────────────────────
        episodio(Skill.ERROR_RECOVERY, tx("epparsers_cair_levantar")) {
            val itens = listOf("x", "=", "*", "3", ";", "y", "=", "2", ";")
            cena(tx("epparsers_erro_nao_pode_derrubar_compilador")) {
                add(titulo(tx("comum_modo_panico")))
                add(fileira("k", itens.map { it to Tint.TEAL }, 130f, w = 34f, h = 42f, gap = 4f))
            }
            cena(tx("epparsers_aqui_parser_trava_depois_igual"), mood = EpMood.SURPRISED) {
                tint("k2", Tint.RED)
                hot("k2")
                add(rotulo("e", tx("epparsers_erro_aqui"), 110f, 180f, Tint.RED, 17f))
            }
            pergunta(
                tx("epparsers_no_modo_panico_ele_descarta"),
                setOf("k4"), setOf("k3", "k5", "k7"),
                tx("epparsers_procure_token_que_encerra_comando"),
                tx("epparsers_isso_ponto_virgula_sincronizador"),
            ) { hot() }
            cena(tx("epparsers_tudo_ate_ali_vai_fora")) {
                drop("k2", "k3", "e")
                tint("k4", Tint.GREEN)
                hot("k5")
            }
            cena(tx("epparsers_bons_sincronizadores_saem_follow_cada"), who = PROF) { hot() }
            cena(tx("epparsers_assim_compilacao_so_aponta_varios"), mood = EpMood.HAPPY) {}
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
            cena(tx("epparsers_agora_gente_monta_arvore_baixo")) {
                add(titulo("E -> E + n | n"))
                esteira(listOf(0 to "n", 1 to "+", 2 to "n", 3 to "$"))
                add(rotulo("rp", "pilha", 300f, 372f))
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
        },

        // ─────────────────────────────── ITENS LR(0) ───────────────────────────────

        // ─────────────────────────────── SLR ───────────────────────────────

        // ─────────────────────────────── LALR ───────────────────────────────

        // ─────────────────────────────── BISON ───────────────────────────────
        episodio(Skill.YACC_BISON, tx("epparsers_fabrica_parsers")) {
            val spec = listOf(
                "%left '+' '-'",
                "%left '*' '/'",
                "%%",
                "expr: expr '+' expr { \$\$ = \$1 + \$3; }",
                "    | expr '*' expr { \$\$ = \$1 * \$3; }",
                "    | NUM ;",
            )
            cena(tx("epparsers_bison_le_gramatica_escreve_parser")) {
                add(titulo(tx("epparsers_arquivo_y")))
                add(codigo("s", spec, 70f, passo = 30f, size = 13f))
            }
            cena(tx("epparsers_cada_regra_tem_acao_cifrao")) { hot("s3") }
            cena(tx("epparsers_cifrao_cifrao_resultado_regra_ele")) { hot("s4") }
            cena(tx("epparsers_essa_gramatica_ambigua_quem_resolve"), mood = EpMood.THINK) { hot("s0", "s1") }
            pergunta(
                tx("epparsers_quanto_mais_abaixo_no_arquivo"),
                setOf("s1"), setOf("s0"),
                tx("epparsers_procure_linha_mais_perto_das"),
                tx("epparsers_isso_vezes_esta_mais_abaixo"),
            ) { hot() }
            cena(tx("epparsers_left_diz_que_contas_iguais")) {
                add(Box("e1", "1 + 2 * 3 = 7", 180f, 300f, 200f, 42f, Look.TOKEN, Tint.GREEN, size = 16f))
                add(Box("e2", "5 - 2 - 1 = 2", 180f, 350f, 200f, 42f, Look.TOKEN, Tint.TEAL, size = 16f))
                hot("s0")
            }
            cena(tx("epparsers_sem_nenhuma_linha_conflito_na"), mood = EpMood.HAPPY) { hot() }
        },
    )
}
