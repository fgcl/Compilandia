package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Temporada.CLIPE
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

private fun EpisodeBuilder.grande(t: String, y: Float = 200f, tint: Tint = Tint.YELLOW) = add(Box("big", t, 180f, y, 300f, 74f, Look.TOKEN, tint, size = 24f))

/** Aulas de 30 segundos para o feed. As duas primeiras falas também respondem às perguntas no Laboratório. */
val clipesTodos by lazy {
    listOf(
        episodio(Skill.PHASES, tx("comum_que_compilador_faz"), CLIPE) {
            cena(tx("epclipes_compilador_traduz_seu_codigo_para")) {
                add(titulo(tx("epclipes_compilador")))
                add(Box("a", "x = 1 + 2;", 180f, 130f, 200f, 50f, Look.CELL, size = 17f))
                add(Box("b", "01101000", 180f, 290f, 200f, 50f, Look.CELL, Tint.TEAL, size = 17f))
                wire("a", "b")
            }
            cena(tx("epclipes_ele_confere_tudo_antes_se")) { hot("a") }
        },
        episodio(Skill.PHASES, tx("comum_compilador_ou_interpretador"), CLIPE) {
            cena(tx("epclipes_compilador_traduz_tudo_antes_entrega")) {
                add(titulo(tx("epclipes_tudo_vez")))
                add(fileira("c", listOf(tx("epclipes_linha_1") to Tint.TEAL, tx("epclipes_linha_2") to Tint.TEAL, tx("epclipes_linha_3") to Tint.TEAL), 200f, w = 96f))
            }
            cena(tx("epclipes_interpretador_le_executa_na_hora")) {
                add(titulo(tx("epclipes_cada_vez")))
                tint("c1", Tint.NONE); tint("c2", Tint.NONE); hot("c0")
            }
        },
        episodio(Skill.TOKENS, tx("comum_token_lexema"), CLIPE) {
            cena(tx("epclipes_lexema_texto_que_esta_no")) {
                add(titulo(tx("epclipes_lexema")))
                add(Box("a", "conta", 180f, 200f, 150f, 60f, Look.CELL, Tint.YELLOW, size = 22f))
            }
            cena(tx("epclipes_token_classe_dele_como_identificador")) {
                add(titulo(tx("comum_token")))
                add(Box("b", "identificador", 180f, 280f, 200f, 50f, Look.TOKEN, Tint.TEAL, size = 18f))
                wire("a", "b")
            }
        },
        episodio(Skill.TOKENS, tx("comum_maior_casamento"), CLIPE) {
            cena(tx("epclipes_scanner_fica_com_pedaco_mais")) {
                add(titulo(tx("comum_maior_casamento")))
                add(fileira("c", listOf("x" to Tint.NONE, ">" to Tint.YELLOW, "=" to Tint.YELLOW, "1" to Tint.NONE), 200f, w = 62f, h = 62f, size = 24f))
                hot("c1")
            }
            cena(tx("epclipes_maior_ou_igual_token_so")) { hot("c1", "c2") }
        },
        episodio(Skill.TOKENS, tx("comum_palavra_chave_ou_nome"), CLIPE) {
            cena(tx("epclipes_if_casa_com_regra_nome")) {
                add(titulo(tx("epclipes_empate")))
                add(Box("a", "if", 180f, 140f, 100f, 56f, Look.CELL, Tint.YELLOW, size = 24f))
                add(fileira("r", listOf("nome" to Tint.TEAL, "reservada" to Tint.PINK), 270f, w = 130f, h = 50f))
            }
            cena(tx("epclipes_quando_tamanho_empata_reservada_ganha")) { hot("r1"); tint("r0", Tint.DARK) }
        },
        episodio(Skill.REGEX_DFA, tx("comum_que_estrela_faz"), CLIPE) {
            cena(tx("epclipes_estrela_repete_que_vem_antes")) {
                add(titulo("a*"))
                add(fileira("c", listOf("a" to Tint.YELLOW), 200f, w = 62f, h = 62f, size = 24f))
            }
            cena(tx("epclipes_entao_seguido_estrela_aceita_vazio")) {
                add(fileira("c", listOf("a" to Tint.YELLOW, "a" to Tint.YELLOW, "a" to Tint.YELLOW), 200f, w = 62f, h = 62f, size = 24f))
            }
        },
        episodio(Skill.NFA_TO_DFA, tx("comum_afn_afd"), CLIPE) {
            val pos = listOf(70f to 200f, 280f to 120f, 280f to 280f)
            cena(tx("epclipes_no_afd_cada_letra_leva")) {
                add(titulo("AFD"))
                add(estados(pos))
                wire("q0", "q1", "a")
            }
            cena(tx("epclipes_no_afn_mesma_letra_pode")) {
                add(titulo("AFN"))
                wire("q0", "q2", "a", hot = true)
            }
        },
        episodio(Skill.NFA_TO_DFA, tx("comum_epsilon_fecho"), CLIPE) {
            val pos = listOf(60f to 200f, 180f to 200f, 300f to 200f)
            cena(tx("epclipes_fecho_junta_estado_com_tudo")) {
                add(titulo(tx("comum_fecho")))
                add(estados(pos))
                wire("q0", "q1", "ε"); wire("q1", "q2", "ε")
                hot("q0")
            }
            cena(tx("epclipes_passeio_graca_pelas_setas_epsilon")) { hot("q0", "q1", "q2") }
        },
        episodio(Skill.CFG_DERIVATION, tx("comum_gramatica_em_30_segundos"), CLIPE) {
            cena(tx("epclipes_gramatica_lista_trocas")) {
                add(titulo("S -> a S b | c"))
                add(fileira("f", listOf("S" to Tint.ORANGE), 200f, w = 56f, h = 56f, size = 22f))
            }
            cena(tx("epclipes_comece_no_simbolo_inicial_troque")) {
                add(fileira("f", listOf("a" to Tint.TEAL, "c" to Tint.TEAL, "b" to Tint.TEAL), 200f, w = 56f, h = 56f, size = 22f))
            }
        },
        episodio(Skill.CFG_DERIVATION, tx("comum_terminal_nao_terminal"), CLIPE) {
            cena(tx("epclipes_terminal_peca_final_como_id")) {
                add(titulo(tx("epclipes_terminal")))
                add(Box("t", "id", 110f, 200f, 110f, 60f, Look.TOKEN, Tint.TEAL, size = 22f))
            }
            cena(tx("epclipes_nao_terminal_ainda_vai_virar")) {
                add(titulo(tx("epclipes_nao_terminal")))
                add(Box("n", "E", 250f, 200f, 110f, 60f, Look.TOKEN, Tint.ORANGE, size = 22f))
            }
        },
        episodio(Skill.AMBIGUITY, tx("comum_que_ambiguidade"), CLIPE) {
            cena(tx("epclipes_gramatica_ambigua_quando_mesma_frase")) {
                add(titulo("2 + 3 * 4"))
                grande("(2 + 3) * 4 = 20", 170f, Tint.ORANGE)
            }
            cena(tx("epclipes_duas_arvores_dao_dois_sentidos")) {
                add(Box("b", "2 + (3 * 4) = 14", 180f, 260f, 300f, 74f, Look.TOKEN, Tint.TEAL, size = 24f))
            }
        },
        episodio(Skill.LEFT_RECURSION, tx("comum_por_que_recursao_trava"), CLIPE) {
            cena(tx("epclipes_na_regra_vai_para_mais")) {
                add(titulo("E -> E + T"))
                add(fileira("f", listOf("E" to Tint.RED, "+" to Tint.TEAL, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 22f))
                hot("f0")
            }
            cena(tx("epclipes_ele_repete_isso_para_sempre")) {
                add(fileira("f", listOf("E" to Tint.RED, "+" to Tint.TEAL, "T" to Tint.ORANGE, "+" to Tint.TEAL, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 22f))
                hot("f0")
            }
        },
        episodio(Skill.LEFT_FACTORING, tx("comum_fatorar_adiar_escolha"), CLIPE) {
            cena(tx("epclipes_quando_duas_opcoes_comecam_igual")) {
                add(titulo("A -> a b | a c"))
                add(fileira("f", listOf("a" to Tint.YELLOW, "?" to Tint.RED), 200f, w = 74f, h = 60f, size = 22f))
            }
            cena(tx("epclipes_fatorar_le_comeco_igual_vez")) {
                add(titulo("A -> a X"))
                add(fileira("f", listOf("a" to Tint.YELLOW, "X" to Tint.ORANGE), 200f, w = 74f, h = 60f, size = 22f))
            }
        },
        episodio(Skill.FIRST, tx("comum_que_first"), CLIPE) {
            cena(tx("epclipes_first_que_pode_aparecer_primeiro")) {
                add(titulo("FIRST"))
                add(fileira("f", listOf("id" to Tint.YELLOW, "T" to Tint.ORANGE, "+" to Tint.TEAL), 200f, w = 70f, h = 56f, size = 20f))
                hot("f0")
            }
            cena(tx("epclipes_com_ele_parser_decide_qual")) {}
        },
        episodio(Skill.FOLLOW, tx("comum_que_follow"), CLIPE) {
            cena(tx("epclipes_follow_que_pode_vir_logo")) {
                add(titulo("FOLLOW"))
                add(fileira("f", listOf("T" to Tint.ORANGE, "+" to Tint.YELLOW, "E" to Tint.ORANGE), 200f, w = 70f, h = 56f, size = 20f))
                hot("f1")
            }
            cena(tx("epclipes_ele_serve_para_decidir_quando")) {}
        },
        episodio(Skill.FIRST, tx("comum_para_que_serve_epsilon"), CLIPE) {
            cena(tx("epclipes_epsilon_cadeia_vazia_regra_que")) {
                add(titulo("X -> + T X | ε"))
                grande("ε")
            }
            cena(tx("epclipes_assim_que_lista_consegue_terminar")) { text("big", "+ T + T + T  fim") }
        },
        episodio(Skill.LL1_TABLE, tx("comum_que_quer_dizer_ll_1"), CLIPE) {
            cena(tx("epclipes_le_esquerda_deriva_pela_esquerda")) {
                add(titulo("L  L  (1)"))
                add(fileira("f", listOf("esquerda" to Tint.TEAL, "esquerda" to Tint.ORANGE, tx("epclipes_token_2") to Tint.YELLOW), 200f, w = 104f, h = 56f, size = 15f))
            }
            cena(tx("epclipes_se_casa_tabela_tem_duas")) {
                dropAll("f")
                grande(tx("epclipes_duas_regras"), tint = Tint.RED)
            }
        },
        episodio(Skill.RECURSIVE_DESCENT, tx("comum_descida_recursiva"), CLIPE) {
            cena(tx("comum_cada_nao_terminal_vira_funcao")) {
                add(titulo("E -> T X"))
                add(codigo("c", listOf("void E() {", "  T();", "  X();", "}"), 140f, x = 110f, passo = 34f, size = 18f))
            }
            cena(tx("epclipes_funcao_olha_token_escolhe_regra")) { hot("c1", "c2") }
        },
        episodio(Skill.ERROR_RECOVERY, tx("comum_modo_panico"), CLIPE) {
            cena(tx("epclipes_achou_erro_parser_joga_tokens")) {
                add(titulo(tx("comum_modo_panico")))
                add(fileira("f", listOf("=" to Tint.RED, "=" to Tint.RED, "2" to Tint.DARK, ";" to Tint.YELLOW), 200f, w = 62f, h = 56f, size = 20f))
            }
            cena(tx("epclipes_ponto_virgula_mais_comum_dai")) { hot("f3") }
        },
        episodio(Skill.SHIFT_REDUCE, tx("comum_que_handle"), CLIPE) {
            cena(tx("epclipes_handle_pedaco_topo_pilha_que")) {
                add(titulo(tx("epclipes_handle")))
                add(fileira("f", listOf("$" to Tint.NONE, "E" to Tint.ORANGE, "+" to Tint.YELLOW, "T" to Tint.ORANGE), 200f, w = 62f, h = 56f, size = 20f))
                hot("f1", "f2", "f3")
            }
            cena(tx("epclipes_aqui_mais_t_vira")) {
                drop("f2", "f3")
                hot("f1")
            }
        },
        episodio(Skill.SHIFT_REDUCE, tx("comum_empilhar_ou_reduzir"), CLIPE) {
            cena(tx("epclipes_empilhar_puxa_proximo_token_para")) {
                add(titulo(tx("comum_empilhar_2")))
                add(pilha("p", listOf("E", "+"), 180f, 300f, w = 110f, h = 46f))
                add(Box("n", "id", 300f, 160f, 76f, 46f, Look.TOKEN, Tint.YELLOW, size = 17f))
            }
            cena(tx("epclipes_reduzir_troca_topo_pelo_lado")) {
                add(titulo(tx("comum_reduzir")))
                move("n", 180f, 202f); text("n", "T")
            }
        },
        episodio(Skill.LR0_ITEMS, tx("comum_ponto_item"), CLIPE) {
            cena(tx("epclipes_item_regra_com_ponto")) {
                add(titulo(tx("epclipes_item")))
                grande("E -> E . + T")
            }
            cena(tx("epclipes_ponto_mostra_ate_onde_regra")) { text("big", "E -> E + . T") }
        },
        episodio(Skill.SLR_TABLE, tx("comum_conflito_shift_reduce"), CLIPE) {
            cena(tx("epclipes_no_mesmo_estado_com_mesmo")) {
                add(titulo(tx("epclipes_duas_ordens")))
                add(fileira("f", listOf("empilhar" to Tint.TEAL, "reduzir" to Tint.ORANGE), 200f, w = 130f, h = 60f, size = 17f))
            }
            cena(tx("epclipes_bison_escolhe_empilhar_avisa")) { hot("f0"); tint("f1", Tint.DARK) }
        },
        episodio(Skill.LALR, tx("comum_que_lalr_faz"), CLIPE) {
            cena(tx("epclipes_lr_1_sabe_muito_mas")) {
                add(titulo("LR(1)"))
                add(fileira("f", listOf("1" to Tint.TEAL, "2" to Tint.TEAL, "3" to Tint.TEAL, "4" to Tint.TEAL), 200f, w = 60f, h = 56f))
            }
            cena(tx("epclipes_lalr_junta_estados_parecidos_guarda")) {
                add(titulo("LALR"))
                move("f1", 150f, 200f); move("f0", 150f, 200f); move("f3", 212f, 200f); move("f2", 212f, 200f)
            }
        },
        episodio(Skill.YACC_BISON, tx("comum_precedencia_no_bison"), CLIPE) {
            cena(tx("epclipes_left_diz_quem_liga_mais")) {
                add(titulo("%left"))
                add(codigo("c", listOf("%left '+'", "%left '*'"), 170f, x = 120f, passo = 44f, size = 20f))
            }
            cena(tx("comum_quanto_mais_embaixo_no_arquivo")) { hot("c1") }
        },
        episodio(Skill.ATTRIBUTES_TYPES, tx("comum_atributo_sintetizado_herdado"), CLIPE) {
            cena(tx("epclipes_sintetizado_sobe_dos_filhos_para")) {
                add(titulo(tx("epclipes_sobe")))
                add(Box("p", "pai", 180f, 120f, 110f, 50f, Look.NODE, Tint.YELLOW, size = 17f))
                add(Box("f", "filho", 180f, 280f, 110f, 50f, Look.NODE, Tint.TEAL, size = 17f))
                wire("f", "p", hot = true)
            }
            cena(tx("epclipes_herdado_desce_pai_ou_vem")) {
                add(titulo(tx("epclipes_desce")))
                clearWires(); wire("p", "f", hot = true)
            }
        },
        episodio(Skill.ATTRIBUTES_TYPES, tx("comum_coercao_tipo"), CLIPE) {
            cena(tx("epclipes_inteiro_mais_real_compilador_converte")) {
                add(titulo("2 + 1.5"))
                add(fileira("f", listOf("int" to Tint.TEAL, "+" to Tint.NONE, "float" to Tint.ORANGE), 200f, w = 84f, h = 56f, size = 17f))
            }
            cena(tx("epclipes_isso_coercao_resultado_real")) { text("f0", "float"); tint("f0", Tint.ORANGE) }
        },
        episodio(Skill.SCOPE_SYMBOLS, tx("comum_tabela_simbolos"), CLIPE) {
            cena(tx("epclipes_tabela_guarda_cada_nome_com")) {
                add(titulo(tx("comum_tabela_simbolos")))
                add(fileira("a", listOf("x" to Tint.YELLOW, "int" to Tint.TEAL, "global" to Tint.ORANGE), 170f, w = 96f, h = 50f))
                add(fileira("b", listOf("y" to Tint.YELLOW, "float" to Tint.TEAL, "local" to Tint.ORANGE), 230f, w = 96f, h = 50f))
            }
            cena(tx("epclipes_todo_uso_variavel_passa_por")) { hot("a0") }
        },
        episodio(Skill.AST_BUILD, tx("comum_ast_ou_arvore_derivacao"), CLIPE) {
            cena(tx("epclipes_arvore_derivacao_guarda_cada_regra")) {
                add(titulo(tx("epclipes_b")))
                grande(tx("epclipes_t_t_f"), tint = Tint.ORANGE)
            }
            cena(tx("epclipes_ast_guarda_so_que_importa")) { text("big", tx("comum_b_2")); tint("big", Tint.TEAL) }
        },
        episodio(Skill.TAC, tx("comum_tres_enderecos"), CLIPE) {
            cena(tx("epclipes_cada_linha_tem_no_maximo")) {
                add(titulo(tx("comum_b_c")))
                add(codigo("c", listOf("t1 = b * c"), 180f, x = 96f, passo = 44f, size = 20f))
            }
            cena(tx("epclipes_conta_maior_quebra_em_temporarios")) {
                add(codigo("c", listOf("t1 = b * c", tx("comum_t2_t1")), 180f, x = 96f, passo = 44f, size = 20f))
                hot("c1")
            }
        },
        episodio(Skill.OPTIMIZATION, tx("comum_dobra_constantes"), CLIPE) {
            cena(tx("epclipes_dois_vezes_tres_nao_precisa")) {
                add(titulo("x = 2 * 3"))
                grande("2 * 3")
            }
            cena(tx("epclipes_compilador_faz_conta_escreve_seis")) { text("big", "6"); tint("big", Tint.GREEN) }
        },
        episodio(Skill.RUNTIME, tx("comum_registro_ativacao"), CLIPE) {
            cena(tx("epclipes_cada_chamada_funcao_ganha_bloco")) {
                add(titulo(tx("epclipes_pilha_chamadas")))
                add(pilha("p", listOf("main", "f"), 180f, 300f, w = 170f, h = 50f))
            }
            cena(tx("epclipes_nele_ficam_parametros_variaveis_locais")) { hot("p1") }
        },
        episodio(Skill.CODEGEN_MIPS, tx("comum_registrador_memoria"), CLIPE) {
            cena(tx("epclipes_registrador_rapido_mas_sao_poucos")) {
                add(titulo("t0  t1  t2"))
                add(fileira("r", listOf("t0" to Tint.YELLOW, "t1" to Tint.YELLOW, "t2" to Tint.YELLOW), 160f, w = 84f, h = 50f))
            }
            cena(tx("epclipes_em_mips_lw_traz_memoria")) {
                add(codigo("c", listOf("lw  \$t0, x", "sw  \$t0, y"), 240f, x = 90f, passo = 40f, size = 18f))
            }
        },
        episodio(Skill.REG_ALLOC, tx("comum_que_spill"), CLIPE) {
            cena(tx("epclipes_faltou_registrador_valor_vai_para")) {
                add(titulo(tx("epclipes_spill")))
                add(fileira("r", listOf("t1" to Tint.TEAL, "t2" to Tint.TEAL, "t3" to Tint.RED), 170f, w = 84f, h = 56f))
            }
            cena(tx("epclipes_isso_spill_funciona_mas_fica")) { move("r2", 180f, 300f); text("r2", tx("comum_memoria")); hot("r2") }
        },
    )
}
