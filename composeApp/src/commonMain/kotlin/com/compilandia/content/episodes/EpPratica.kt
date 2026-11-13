package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PRATICA
import com.compilandia.content.Tint
import com.compilandia.content.ToyCorte
import com.compilandia.content.ToyDerivacao
import com.compilandia.content.ToyEmpilhaReduz
import com.compilandia.content.ToyMips
import com.compilandia.content.ToyPilhaLL
import com.compilandia.content.ToyTipos
import com.compilandia.content.ToyTrem
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.Grammar
import com.compilandia.engine.tokensOf
import com.compilandia.learning.Skill
import com.compilandia.tutor.Sims
import com.compilandia.textos.tx

/** A gramática de contas com recursão à esquerda, e ela depois das duas rodadas da receita. Conferidas em AulasConsertadasTest. */
val GRAMATICA_ETF_REC = listOf("E -> E + T | T", "T -> T * F | F", "F -> id")
val GRAMATICA_ETF_DEPOIS = listOf("E  -> T E'", "E' -> + T E' | ε", "T  -> F T'", "T' -> * F T' | ε", "F  -> id")

/** Temporada Na prática: a pessoa pilota, e quem calcula é o motor. */
val praticas by lazy {
    listOf(
        episodio(Skill.PHASES, tx("eppratica_camera_entra_na_maquina"), PRATICA) {
            val nomes = listOf(tx("comum_lexica"), tx("comum_sintatica"), tx("comum_semantica"), tx("comum_intermediario"), tx("comum_otimizacao_2"), tx("comum_geracao"))
            val fases = nomes.mapIndexed { i, n -> Box("f$i", n, 180f, 76f + i * 56f, 210f, 46f, Look.TOKEN, Tint.NONE, size = 17f) }
            cena(tx("eppratica_esta_linha_montagem_inteira_vamos")) {
                add(fases)
                tint("f0", Tint.TEAL)
            }
            cena(tx("eppratica_dentro_fase_lexica_linha_chega")) {
                text("f0", "")
                text("f1", "")
                val letras = "x>=1"
                letras.forEachIndexed { i, c -> add(Box("c$i", c.toString(), 156f + i * 16f, 76f, 14f, 22f, Look.CELL, Tint.DARK, size = 9f)) }
                camera(180f, 76f, 4f)
            }
            pergunta(
                tx("eppratica_dois_destes_caracteres_formam_token"),
                setOf("c1", "c2"), setOf("c0", "c3"),
                tx("eppratica_procure_operador_comparacao"),
                tx("eppratica_isso_maior_ou_igual_token"),
            ) { hot() }
            cena(tx("eppratica_tokens_saem_daqui_seguem_para")) {
                dropAll("c")
                text("f0", tx("comum_lexica"))
                text("f1", tx("comum_sintatica"))
                camera()
                hot("f1"); tint("f1", Tint.ORANGE)
            }
            cena(tx("eppratica_dentro_sintatica_mesmos_tokens_viram")) {
                text("f0", "")
                text("f1", "")
                text("f2", "")
                add(Box("r", ">=", 180f, 124f, 26f, 15f, Look.NODE, Tint.ORANGE, size = 8f))
                add(Box("a", "x", 160f, 140f, 18f, 13f, Look.NODE, Tint.TEAL, size = 7f))
                add(Box("b", "1", 200f, 140f, 18f, 13f, Look.NODE, Tint.TEAL, size = 7f))
                wire("r", "a", arrow = false); wire("r", "b", arrow = false)
                camera(180f, 132f, 4f)
            }
            cena(tx("eppratica_cada_maquina_recebe_trabalho_anterior"), who = PROF, mood = EpMood.HAPPY) {
                drop("r", "a", "b")
                text("f0", tx("comum_lexica"))
                text("f1", tx("comum_sintatica"))
                text("f2", tx("comum_semantica"))
                camera()
                hot()
            }
        },

        episodio(Skill.FIRST, tx("eppratica_simbolos_correndo_nos_fios"), PRATICA) {
            val no = { id: String, t: String, y: Float -> Box(id, t, 60f, y, 58f, 58f, Look.STATE, Tint.ORANGE, size = 20f) }
            val conj = { id: String, y: Float -> Box(id, "", 220f, y, 210f, 50f, Look.CELL, Tint.DARK, size = 15f) }
            cena(tx("eppratica_tres_regras_em_cadeia_comeca")) {
                add(no("F", "F", 90f), no("T", "T", 220f), no("E", "E", 350f))
                add(conj("cF", 90f), conj("cT", 220f), conj("cE", 350f))
                wire("F", "T", bend = 0f); wire("T", "E", bend = 0f)
            }
            cena(tx("eppratica_f_abre_com_id_ou")) { text("cF", "id   ("); hot("F", "cF") }
            cena(tx("eppratica_t_comeca_com_f_entao")) { flow("F", "T", "id", "("); hot("T") }
            cena(tx("eppratica_chegou_agora_mesmos_simbolos_seguem")) { text("cT", "id   ("); flow("T", "E", "id", "("); hot("E") }
            pergunta(
                tx("eppratica_quem_chega_no_first_toque"),
                setOf("a", "b"), setOf("c"),
                tx("eppratica_so_correm_pelo_fio_simbolos"),
                tx("eppratica_isso_first_escorre_quem_esta"),
            ) {
                clearWires()
                wire("F", "T"); wire("T", "E")
                add(Box("a", "id", 150f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("b", "(", 220f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
                add(Box("c", "+", 290f, 290f, 60f, 40f, Look.TOKEN, Tint.TEAL, size = 15f))
            }
            cena(tx("eppratica_quando_nada_mais_corre_acabou"), who = PROF, mood = EpMood.HAPPY) {
                drop("a", "b", "c")
                text("cE", "id   (")
                hot("cE")
            }
        },

        episodio(Skill.TOKENS, tx("eppratica_corte_voce_mesmo"), PRATICA) {
            cena(tx("eppratica_agora_voce_scanner_linha_x")) {
                add(titulo("x>=10;"))
                add(fileira("t", listOf("x" to Tint.TEAL, ">=" to Tint.ORANGE, "10" to Tint.YELLOW, ";" to Tint.BLUE), 180f, w = 62f))
            }
            cena(tx("eppratica_mais_letra_estica_pedaco_cortar")) { hot("t1") }
            cena(tx("eppratica_lembra_regra_estique_enquanto_couber"), who = PROF) { hot() }
            brinquedo(
                tx("eppratica_corte_linha_inteira_estique_corte"),
                tx("eppratica_motor_recusou_pense_no_maior"),
                tx("eppratica_linha_cortada_foram_quatro_tokens"),
            ) { ToyCorte("x>=10;", tx("eppratica_corte_linha_inteira")) }
            cena(tx("eppratica_scanner_verdade_faz_isso_milhoes"), mood = EpMood.HAPPY) {
                add(titulo("x>=10;"))
                hot("t0", "t1", "t2", "t3")
            }
        },

        episodio(Skill.REGEX_DFA, tx("eppratica_pilote_trem"), PRATICA) {
            cena(tx("eppratica_esta_regex_pede_quantos_mais")) {
                add(titulo(tx("eppratica_b")))
                add(fileira("p", listOf("a" to Tint.TEAL, "a*" to Tint.ORANGE, "b" to Tint.TEAL), 180f, w = 74f, h = 54f, size = 20f))
            }
            cena(tx("eppratica_automato_tem_estado_para_cada")) { hot("p1") }
            cena(tx("eppratica_laco_no_meio_estrela_para"), who = PROF) { hot("p1") }
            brinquedo(
                tx("eppratica_agora_pilote_chegue_no_circulo"),
                tx("eppratica_essa_letra_nao_deu_certo"),
                tx("eppratica_chegou_no_circulo_duplo_frase"),
            ) { val d = DFA.fromRegex("aa*b"); ToyTrem(d, Sims.posicoes(d.stateCount), tx("eppratica_chegue_no_circulo_duplo_com"), 3) }
            cena(tx("eppratica_pilotar_rodar_automato_cabeca_isso"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CFG_DERIVATION, tx("eppratica_derive_ate_chegar"), PRATICA) {
            cena(tx("eppratica_frase_alvo_id_mais_id")) {
                add(titulo("id + id"))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id"), 110f, x = 120f, passo = 32f, size = 17f))
            }
            cena(tx("eppratica_troque_sempre_nao_terminal_mais")) { hot("g0") }
            cena(tx("eppratica_x_decide_se_frase_continua"), who = PROF) { hot("g1") }
            brinquedo(
                tx("eppratica_sua_vez_escolha_regras_ate"),
                tx("eppratica_essa_escolha_saiu_frase_comece"),
                tx("eppratica_chegou_isso_derivacao_mais_esquerda"),
            ) { ToyDerivacao(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id"), tokensOf("id + id"), tx("eppratica_chegue_em_id_id")) }
            cena(tx("eppratica_quando_voce_escolhe_certo_sem"), mood = EpMood.HAPPY) {}
        },

        // E/T/F numa prática (professor do bloco 2): a receita roda duas vezes, e a pessoa deriva na gramática nova.
        // Sem pilha nem $, que só chegam no Templo.
        episodio(Skill.LEFT_RECURSION, tx("rec_p_titulo"), PRATICA) {
            cena(tx("rec_p_01")) {
                add(titulo("E/T/F"))
                add(codigo("g", GRAMATICA_ETF_REC, 80f, x = 110f, passo = 32f, size = 17f))
                hot("g0", "g1")
            }
            pergunta(tx("rec_p_02"), setOf("e1"), setOf("e2", "e3"), tx("rec_p_02_dica"), tx("rec_p_02_ok")) {
                hot("g0")
                add(Box("e2", "E -> + T E'", 180f, 220f, 200f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("e1", "E -> T E'", 180f, 272f, 200f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("e3", "E -> E' T", 180f, 324f, 200f, 42f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            pergunta(tx("rec_p_03"), setOf("t1"), setOf("t2", "t3"), tx("rec_p_03_dica"), tx("rec_p_03_ok")) {
                drop("e1", "e2", "e3"); hot("g1")
                add(codigo("n", GRAMATICA_ETF_DEPOIS.take(2), 190f, x = 90f, passo = 28f, size = 15f))
                // Os dois erros de prova: esquecer o ε e copiar o alfa da outra regra.
                add(Box("t2", "T' -> * F T'", 180f, 270f, 220f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("t3", "T' -> + T T' | ε", 180f, 318f, 220f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(Box("t1", "T' -> * F T' | ε", 180f, 366f, 220f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
            }
            cena(tx("rec_p_04")) {
                clear()
                add(titulo(tx("comum_depois")))
                add(codigo("n", GRAMATICA_ETF_DEPOIS, 80f, x = 100f, passo = 32f, size = 17f))
                hot("n1", "n3")
            }
            brinquedo(
                tx("rec_p_05"),
                tx("rec_p_05_dica"),
                tx("rec_p_05_ok"),
            ) { ToyDerivacao(Grammar.parse(GRAMATICA_ETF_DEPOIS.joinToString("\n")), tokensOf("id + id * id"), tx("rec_p_meta")) }
            cena(tx("rec_p_06"), who = PROF, mood = EpMood.HAPPY) {}
        },

        episodio(Skill.LL1_PARSING, tx("eppratica_opere_pilha"), PRATICA) {
            cena(tx("eppratica_pilha_entrada_duas_acoes_parece")) {
                add(titulo("id + id"))
                add(codigo("g", listOf("E -> T X", "X -> + T X | ε", "T -> id"), 110f, x = 120f, passo = 32f, size = 17f))
            }
            cena(tx("eppratica_topo_nao_terminal_consulta_tabela")) { hot("g0") }
            cena(tx("eppratica_topo_terminal_precisa_bater_com")) { hot("g2") }
            brinquedo(
                tx("eppratica_opere_ate_pilha_esvaziar"),
                tx("eppratica_olhe_topo_ele_terminal_ou"),
                tx("eppratica_pilha_vazia_entrada_no_fim"),
            ) { ToyPilhaLL(Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id"), tokensOf("id + id"), tx("eppratica_leve_pilha_ate_fim")) }
            cena(tx("eppratica_sem_recursao_sem_volta_atras"), who = PROF, mood = EpMood.HAPPY) {}
        },

        episodio(Skill.SHIFT_REDUCE, tx("eppratica_opere_esteira"), PRATICA) {
            cena(tx("eppratica_agora_sentido_contrario_frase_ate")) {
                add(titulo("n + n"))
                add(codigo("g", listOf("E -> E + n | n"), 130f, x = 90f, passo = 32f, size = 18f))
            }
            cena(tx("eppratica_empilhar_puxa_proximo_token_reduzir")) { hot("g0") }
            cena(tx("eppratica_quando_sobrar_so_entrada_acabar"), who = PROF) {}
            brinquedo(
                tx("eppratica_sua_vez_na_esteira_empilhe"),
                tx("eppratica_olhe_topo_ele_ja_forma"),
                tx("eppratica_sobrou_entrada_acabou_aceito"),
            ) { ToyEmpilhaReduz(Grammar.parse("E -> E + n | n"), tokensOf("n + n"), tx("eppratica_reduza_ate_sobrar")) }
            cena(tx("eppratica_foi_derivacao_mais_direita_tras"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.YACC_BISON, tx("eppratica_por_que_vezes_espera"), PRATICA) {
            cena(tx("eppratica_sem_declarar_nada_niveis_gramatica")) {
                add(titulo("id + id * id"))
                add(codigo("g", listOf("E -> E + T | T", "T -> T * F | F", "F -> id"), 100f, x = 110f, passo = 32f, size = 17f))
            }
            cena(tx("eppratica_regra_mais_funda_roda_antes")) { hot("g1") }
            cena(tx("eppratica_com_bison_left_faz_mesmo"), who = PROF) {}
            brinquedo(
                tx("eppratica_rode_esteira_repare_quando_vezes"),
                tx("eppratica_antes_reduzir_olhe_que_vem"),
                tx("eppratica_aceito_vezes_reduziu_antes_mais"),
            ) { ToyEmpilhaReduz(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> id"), tokensOf("id + id * id"), tx("eppratica_reduza_ate_sobrar")) }
            cena(tx("eppratica_esperar_empilhar_foi_por_isso"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.ATTRIBUTES_TYPES, tx("eppratica_suba_tipos"), PRATICA) {
            cena(tx("eppratica_aqui_inteiro_x_real_vamos")) {
                add(titulo(tx("comum_x_2")))
                add(rotulo("r", "a: int     x: float", 180f, 130f, Tint.YELLOW, 18f))
            }
            cena(tx("eppratica_comece_pelas_folhas_depois_cada")) {}
            cena(tx("eppratica_se_dos_filhos_real_resultado"), who = PROF) {}
            brinquedo(
                tx("eppratica_diga_tipo_cada_no_aceso"),
                tx("eppratica_tipo_errado_olhe_filhos_desse"),
                tx("eppratica_arvore_anotada_tipo_subiu_ate"),
            ) { ToyTipos(tx("comum_x_2"), ExprGenerator.env, tx("eppratica_diga_tipo_no_aceso")) }
            cena(tx("eppratica_isso_que_voce_fez_atributo"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CODEGEN_MIPS, tx("eppratica_monte_mips"), PRATICA) {
            cena(tx("eppratica_mips_so_faz_conta_em")) {
                add(titulo(tx("comum_b_2")))
                add(fileira("p", listOf("lw" to Tint.TEAL, "add" to Tint.ORANGE, "sw" to Tint.YELLOW), 180f, w = 84f, h = 54f, size = 18f))
            }
            cena(tx("eppratica_lw_traz_memoria_add_faz")) { hot("p0", "p1", "p2") }
            cena(tx("eppratica_esqueceu_lw_conta_usa_lixo"), who = PROF) { hot() }
            brinquedo(
                tx("eppratica_monte_instrucoes_na_ordem"),
                tx("eppratica_ordem_errada_primeiro_carrega_depois"),
                tx("eppratica_programa_pronto_foi_assim_que"),
            ) { ToyMips(tx("comum_b_2"), tx("eppratica_escolha_proxima_instrucao")) }
            cena(tx("eppratica_cada_linha_tres_enderecos_vira"), mood = EpMood.HAPPY) {}
        },
    )
}
