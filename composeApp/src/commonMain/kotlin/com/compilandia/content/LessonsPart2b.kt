package com.compilandia.content

import com.compilandia.content.Beat.Callout
import com.compilandia.content.Beat.Code
import com.compilandia.content.Beat.Predict
import com.compilandia.content.Beat.Reveal
import com.compilandia.content.Beat.Say
import com.compilandia.content.Beat.Steps
import com.compilandia.content.Beat.Text
import com.compilandia.content.Beat.Visual
import com.compilandia.content.CalloutKind.BRAIN_POWER
import com.compilandia.content.CalloutKind.EXAM_TIP
import com.compilandia.content.CalloutKind.FIRESIDE
import com.compilandia.content.CalloutKind.MNEMONIC
import com.compilandia.content.CalloutKind.NO_DUMB_QUESTIONS
import com.compilandia.content.CalloutKind.REAL_WORLD
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.LEFT_RECURSION
import com.compilandia.content.VisualKind.LL1_TABLE
import com.compilandia.content.VisualKind.LL1_TRACE
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// ───────────────────────────── TABELA LL(1) ─────────────────────────────

internal val p2Ll1TableLessons: List<Lesson> = listOf(
    Lesson(
        Skill.LL1_TABLE, Style.STORY, tx("lessonspart2b_caderninho_porteiro"),
        listOf(
            Say(NARRADOR, tx("lessonspart2b_terceira_sala_templo_top_down")),
            Say(PROF, tx("lessonspart2b_porteiro_sabe_em_que_parte")),
            Say(BYTE, tx("lessonspart2b_como_ele_preencheu_esse_caderninho")),
            Say(PROF, tx("lessonspart2b_com_duas_regrinhas_regra_1")),
            Say(PROF, tx("lessonspart2b_regra_2_se_pode_sumir")),
            Code(P2_ABC, tx("lessonspart2b_gramatica_fila_novo_follow_b")),
            Predict(
                tx("lessonspart2b_estou_em_chega_c_que"),
                listOf("A → a", "A → ε", tx("comum_erro"), tx("lessonspart2b_duas")), 1,
                tx("lessonspart2b_c_nao_abre_mas_pode")
            ),
            Visual(LL1_TABLE, P2_ABC, caption = tx("lessonspart2b_caderninho_completo_repare_em_nas")),
            Say(BYTE, tx("lessonspart2b_se_porteiro_tiver_duas_portas")),
            Say(PROF, tx("lessonspart2b_ai_ele_trava_conflito_gramatica")),
            Visual(LL1_TABLE, P2_FACT, caption = tx("solta_lessonspart2b_1")),
            Reveal(tx("lessonspart2b_celula_vazia_no_caderninho_significa"), tx("lessonspart2b_erro_sintaxe_nenhuma_producao_pode")),
            Callout(REAL_WORLD, tx("lessonspart2b_gcc_clang_compiladores_c_c")),
            Say(PROF, tx("lessonspart2b_resumo_porteiro_first_abre_porta")),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.VISUAL, tx("lessonspart2b_vendo_tabela_nascer_quebrar"),
        listOf(
            Say(PROF, tx("lessonspart2b_vamos_ver_tabela_gramatica_expressoes")),
            Code(P2_EXPR_LL),
            Visual(LL1_TABLE, P2_EXPR_LL, caption = tx("lessonspart2b_toque_nas_celulas_para_ver")),
            Steps(
                tx("lessonspart2b_que_olhar"),
                listOf(
                    tx("lessonspart2b_linhas_nao_terminais_colunas_terminais"),
                    tx("solta_lessonspart2b_2"),
                    tx("solta_lessonspart2b_3"),
                    tx("solta_lessonspart2b_4"),
                    tx("lessonspart2b_nenhuma_celula_tem_duas_producoes"),
                )
            ),
            Predict(
                tx("lessonspart2b_em_qual_celula_fica_f"),
                listOf("M[F, (]", "M[F, )]", tx("lessonspart2b_m"), "M[F, id]"), 0,
                tx("lessonspart2b_first_entao_m_f_f")
            ),
            Say(BYTE, tx("lessonspart2b_aquela_gramatica_bonita_t_t")),
            Visual(LL1_TABLE, P2_EXPR, caption = tx("lessonspart2b_versao_recursiva_esquerda_veja_celulas")),
            Text(tx("lessonspart2b_m_id_ganhou_duas_producoes")),
            Callout(WATCH_OUT, tx("lessonspart2b_gramatica_com_recursao_esquerda_ou")),
            Reveal(tx("lessonspart2b_quantas_celulas_preenchidas_tem_tabela"), tx("lessonspart2b_13_2_3_t_2")),
            Visual(LEFT_RECURSION, P2_EXPR, caption = tx("lessonspart2b_lembrete_ponte_eliminar_recursao_esquerda")),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.WORKED, tx("lessonspart2b_exemplo_resolvido_tabela_das_declaracoes"),
        listOf(
            Say(PROF, tx("lessonspart2b_exercicio_prova_construa_tabela_ll")),
            Code(P2_DECL),
            Text(tx("lessonspart2b_ingredientes_ja_calculados_first_t")),
            Steps(
                tx("lessonspart2b_producao_por_producao"),
                listOf(
                    tx("solta_lessonspart2b_5"),
                    "T → int : M[T, int].",
                    "T → float : M[T, float].",
                    "L → id R : M[L, id].",
                    "R → , id R : FIRST = {,}. M[R, ,].",
                    "R → ε : FIRST(ε) = {ε}, então use FOLLOW(R) = {;}. M[R, ;].",
                    tx("lessonspart2b_nenhuma_celula_com_duas_producoes"),
                )
            ),
            Visual(LL1_TABLE, P2_DECL, caption = tx("lessonspart2b_confira_7_celulas_preenchidas_todo")),
            Say(BYTE, tx("lessonspart2b_por_que_r_nao_vai")),
            Say(PROF, tx("lessonspart2b_porque_follow_r_depois_r")),
            Say(PROF, tx("lessonspart2b_agora_desbotado_b_c_b")),
            Text(tx("lessonspart2b_eu_faco_b_c_tem")),
            Reveal(tx("lessonspart2b_sua_vez_onde_vai"), "Em M[A, b] e M[A, c], porque FOLLOW(A) = {b, c}."),
            Reveal(tx("lessonspart2b_b"), "Em M[B, c], porque FOLLOW(B) = {c}."),
            Reveal(tx("lessonspart2b_gramatica_ll_1"), tx("lessonspart2b_sim_nenhuma_celula_ficou_com")),
            Visual(LL1_TABLE, P2_ABC, caption = tx("comum_confira_suas_respostas")),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.SOCRATIC, tx("lessonspart2b_como_escolher_sem_voltar_atras"),
        listOf(
            Say(PROF, tx("lessonspart2b_pergunta_raiz_analisador_esta_em")),
            Predict(
                tx("solta_lessonspart2b_6"),
                listOf("A → x y", "A → z w", tx("lessonspart2b_precisa_testar_duas"), tx("comum_nenhuma")), 1,
                tx("lessonspart2b_z_first_z_w_z")
            ),
            Predict(
                "A → x y | x w. Chega x. E agora?",
                listOf("A → x y", "A → x w", tx("lessonspart2b_nao_para_decidir_com_1"), tx("comum_erro")), 2,
                tx("lessonspart2b_duas_comecam_com_x_conflito")
            ),
            Predict(
                "A → x y | ε, e FOLLOW(A) = {z}. Chega z. Qual?",
                listOf("A → x y", "A → ε", tx("comum_erro"), tx("lessonspart2b_duas")), 1,
                tx("lessonspart2b_z_nao_abre_x_y")
            ),
            Predict(
                tx("solta_lessonspart2b_7"),
                listOf("A → x y", "A → ε", tx("lessonspart2b_conflito_duas"), tx("comum_erro")), 2,
                tx("lessonspart2b_x_abre_x_y_first")
            ),
            Reveal(
                tx("lessonspart2b_com_base_nessas_4_perguntas"),
                tx("lessonspart2b_para_cada_par_1_first")
            ),
            Say(BYTE, tx("lessonspart2b_entao_tabela_so_jeito_enxergar")),
            Say(PROF, tx("lessonspart2b_exatamente_montou_tabela_nenhuma_celula")),
            Visual(LL1_TABLE, P2_LIST, caption = tx("lessonspart2b_teste_b_antes_olhar_preveja")),
            Reveal(tx("lessonspart2b_por_que_m_b_tem"), tx("lessonspart2b_first_first_b_first_b")),
            Callout(BRAIN_POWER, tx("lessonspart2b_gramatica_ambigua_pode_ser_ll")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2b_nao_analisador_ll_1_constroi")),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.FORMAL, tx("lessonspart2b_construcao_tabela_preditiva_versao_prova"),
        listOf(
            Say(PROF, tx("lessonspart2b_algoritmo_construcao_tabela_analise_preditiv")),
            Code(
                tx("lessonspart2b_entrada_gramatica_g_com_first"),
                tx("lessonspart2b_percorra_producao_por_producao_nao")
            ),
            Text(tx("lessonspart2b_definicao_g_ll_1_se")),
            Steps(
                tx("lessonspart2b_condicoes_equivalentes_para_cada_par"),
                listOf(
                    tx("lessonspart2b_1_first_first"),
                    tx("lessonspart2b_2_no_maximo_entre_deriva"),
                    tx("lessonspart2b_3_se_entao_first_follow"),
                )
            ),
            Predict(
                tx("lessonspart2b_qual_destas_nao_condicao_para"),
                listOf(
                    tx("lessonspart2b_first_das_alternativas_disjuntos"),
                    tx("lessonspart2b_no_maximo_alternativa_anulavel"),
                    tx("lessonspart2b_gramatica_nao_pode_ter_producoes"),
                    tx("lessonspart2b_first_follow_quando_outra_alternativa"),
                ), 2,
                tx("lessonspart2b_producoes_sao_permitidas_em_ll")
            ),
            Visual(LL1_TABLE, P2_EXPR_LL, caption = tx("lessonspart2b_cada_celula_traz_motivo_first")),
            Callout(EXAM_TIP, tx("lessonspart2b_na_prova_1_calcule_first")),
            Callout(WATCH_OUT, tx("lessonspart2b_recursao_esquerda_ambiguidade_ou_prefixos")),
            Reveal(tx("lessonspart2b_formule_definicao_ll_1_em"), tx("lessonspart2b_g_ll_1_se_sua")),
            Say(BYTE, tx("lessonspart2b_ll_1_left_to_right")),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.MNEMONIC, tx("lessonspart2b_sudoku_ll_1_celula_producao"),
        listOf(
            Say(NARRADOR, tx("lessonspart2b_terceira_sala_templo_tabuleiro_sudoku")),
            Callout(MNEMONIC, tx("lessonspart2b_linha_variavel_coluna_token_m")),
            Callout(MNEMONIC, tx("lessonspart2b_rima_ouro_primeiro_first_se")),
            Callout(MNEMONIC, tx("lessonspart2b_sudoku_ll_1_celula_producao_2")),
            Say(BYTE, tx("lessonspart2b_nome_ll_1_sempre_esqueco")),
            Callout(MNEMONIC, tx("lessonspart2b_l_l_1_le_esquerda")),
            Steps(
                tx("comum_confusoes_classicas"),
                listOf(
                    tx("lessonspart2b_por_nas_colunas_first_errado"),
                    tx("lessonspart2b_esquecer_coluna_ela_existe_recebe"),
                    tx("lessonspart2b_achar_que_celula_vazia_tanto"),
                    tx("lessonspart2b_achar_que_producao_impede_ll"),
                )
            ),
            Predict(
                tx("solta_lessonspart2b_8"),
                listOf(tx("lessonspart2b_na_coluna"), tx("lessonspart2b_nas_colunas"), tx("lessonspart2b_so_na_coluna"), tx("lessonspart2b_em_todas_colunas")), 1,
                tx("lessonspart2b_nao_existe_coluna_producao_vazia")
            ),
            Visual(LL1_TABLE, P2_PARENS, caption = tx("lessonspart2b_teste_rima_em_first_manda")),
            Reveal(tx("lessonspart2b_recite_rima_aplique_em_t"), tx("lessonspart2b_primeiro_first_m_t_se")),
        )
    ),
)

// ───────────────────────────── ANALISADOR PREDITIVO ─────────────────────────────

internal val p2Ll1ParsingLessons: List<Lesson> = listOf(
    Lesson(
        Skill.LL1_PARSING, Style.STORY, tx("lessonspart2b_pilha_golem_das_tarefas"),
        listOf(
            Say(NARRADOR, tx("lessonspart2b_no_altar_templo_top_down")),
            Say(PILHA, tx("lessonspart2b_eu_guardo_que_ainda_falta")),
            Say(PROF, tx("lessonspart2b_pilha_analisador_preditivo_lista_tarefas")),
            Say(BYTE, tx("lessonspart2b_como_golem_trabalha")),
            Steps(
                tx("lessonspart2b_regras_golem"),
                listOf(
                    tx("lessonspart2b_topo_terminal_igual_ao_token"),
                    tx("lessonspart2b_topo_nao_terminal_token_consulta"),
                    tx("lessonspart2b_topo_entrada_aceita"),
                    tx("lessonspart2b_qualquer_outra_situacao_erro"),
                )
            ),
            Say(PILHA, tx("lessonspart2b_tras_pra_frente_porque_primeiro")),
            Predict(
                tx("lessonspart2b_pilha_topo_direita_entrada_comecando"),
                listOf(tx("lessonspart2b_t"), tx("lessonspart2b_t_2"), tx("lessonspart2b_t_3"), "\$ T"), 1,
                tx("lessonspart2b_tira_empilha_t_ao_contrario")
            ),
            Visual(LL1_TRACE, P2_EXPR_LL, "id + id", tx("lessonspart2b_acompanhe_golem_pilha_entrada_acao")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart2b_p_por_que_pilha_nao")),
            Reveal(tx("lessonspart2b_quando_golem_usa_que_ele"), tx("lessonspart2b_nada_ele_so_tira_topo")),
            Say(PILHA, tx("lessonspart2b_pilha_vazia_entrada_vazia_festa")),
            Say(PROF, tx("lessonspart2b_repare_sequencia_expansoes_exatamente_deriva")),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.VISUAL, tx("lessonspart2b_analisador_preditivo_passo_passo"),
        listOf(
            Say(PROF, tx("lessonspart2b_entrada_classica_id_id_id")),
            Code(P2_EXPR_LL),
            Visual(LL1_TRACE, P2_EXPR_LL, "id + id * id", tx("lessonspart2b_avance_passo_por_vez_antes")),
            Steps(
                tx("lessonspart2b_pontos_atencao_no_trace"),
                listOf(
                    tx("lessonspart2b_passos_1_3_t_t"),
                    tx("lessonspart2b_passo_4_id_casa_com"),
                    tx("lessonspart2b_passo_5_t_no_topo"),
                    tx("lessonspart2b_passo_6_t_foi_previsto"),
                    tx("lessonspart2b_no_fim_t_com_na"),
                )
            ),
            Predict(
                tx("lessonspart2b_topo_t_entrada_qual_acao"),
                listOf("T' → ε", "T' → * F T'", tx("lessonspart2b_casar"), tx("comum_erro")), 1,
                tx("lessonspart2b_first_f_t_entao_m")
            ),
            Say(BYTE, tx("lessonspart2b_por_que_no_passo_5")),
            Say(PROF, tx("lessonspart2b_por_causa_token_com_t")),
            Reveal(tx("lessonspart2b_quantas_vezes_id_foi_casado"), tx("lessonspart2b_3_para_cada_id_entrada")),
            Visual(DERIVATION, P2_EXPR_LL, "id + id * id", tx("lessonspart2b_compare_derivacao_mais_esquerda_usa")),
            Callout(BRAIN_POWER, tx("lessonspart2b_se_entrada_fosse_id_id")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2b_depois_casar_topo_t_token")),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.WORKED, "Exemplo resolvido: int id , id ;",
        listOf(
            Say(PROF, tx("lessonspart2b_vamos_simular_mao_no_formato")),
            Code(P2_DECL, tx("lessonspart2b_tabela_m_d_int_d")),
            Steps(
                tx("lessonspart2b_simulacao_int_id_id"),
                listOf(
                    "\$ D | int id , id ; \$ | expandir D → T L ;",
                    "\$ ; L T | int id , id ; \$ | expandir T → int",
                    "\$ ; L int | int id , id ; \$ | casar int",
                    "\$ ; L | id , id ; \$ | expandir L → id R",
                    "\$ ; R id | id , id ; \$ | casar id",
                    "\$ ; R | , id ; \$ | expandir R → , id R",
                    "\$ ; R id , | , id ; \$ | casar ,",
                )
            ),
            Say(BYTE, tx("lessonspart2b_olha_so_ficou_la_embaixo")),
            Say(PILHA, tx("lessonspart2b_paciencia_meu_forte")),
            Text(tx("lessonspart2b_agora_passos_finais_sao_seus")),
            Reveal(tx("lessonspart2b_proxima_acao"), tx("solta_lessonspart2b_9")),
            Reveal(tx("lessonspart2b_topo_r_entrada_acao"), tx("lessonspart2b_expandir_r_m_r_pilha")),
            Reveal(tx("lessonspart2b_depois"), "Casar ';'. Pilha \$ e entrada \$ → aceitar!"),
            Visual(LL1_TRACE, P2_DECL, "int id , id ;", tx("lessonspart2b_confira_com_motor_11_linhas")),
            Callout(EXAM_TIP, tx("lessonspart2b_escreva_pilha_sempre_com_topo")),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.SOCRATIC, tx("lessonspart2b_reinvente_analisador_preditivo"),
        listOf(
            Say(PROF, tx("lessonspart2b_voce_vai_redescobrir_analisador_preditivo")),
            Predict(
                tx("lessonspart2b_por_que_colocar_no_fundo"),
                listOf(tx("comum_enfeite"), tx("lessonspart2b_para_saber_quando_pilha_acabou"), tx("lessonspart2b_porque_nao_terminal"), tx("lessonspart2b_para_guardar_resultado")), 1,
                tx("lessonspart2b_sentinela_quando_topo_entrada_tambem")
            ),
            Predict(
                tx("lessonspart2b_topo_terminal_entrada_id_que"),
                listOf(tx("lessonspart2b_casar_mesmo_assim"), tx("lessonspart2b_erro_esperava"), tx("lessonspart2b_consultar_tabela"), tx("lessonspart2b_empilhar_id")), 1,
                tx("lessonspart2b_terminal_no_topo_promessa_proximo")
            ),
            Predict(
                tx("lessonspart2b_topo_m_x_y_z"),
                listOf(tx("comum_por_tradicao"), tx("lessonspart2b_para_x_ficar_no_topo"), tx("lessonspart2b_para_z_casar_primeiro"), tx("comum_tanto_faz")), 1,
                tx("lessonspart2b_derivamos_sempre_simbolo_mais_esquerda")
            ),
            Predict(
                tx("lessonspart2b_topo_m_esta_vazia_que"),
                listOf(tx("comum_aceitar"), tx("lessonspart2b_empilhar"), tx("comum_erro_sintaxe"), tx("lessonspart2b_tentar_outra_producao_backtracking")), 2,
                tx("lessonspart2b_analisador_preditivo_nunca_volta_atras")
            ),
            Reveal(
                tx("lessonspart2b_monte_algoritmo_com_suas_respostas"),
                tx("lessonspart2b_1_x_aceita_2_x")
            ),
            Say(BYTE, tx("lessonspart2b_qual_saida_analisador")),
            Predict(
                tx("lessonspart2b_se_eu_imprimir_cada_producao"),
                listOf(tx("comum_derivacao_mais_direita"), tx("comum_derivacao_mais_esquerda"), tx("comum_tabela_ll_1"), tx("lessonspart2b_conjuntos_follow")), 1,
                tx("lessonspart2b_expansao_acontece_sempre_no_nao")
            ),
            Visual(LL1_TRACE, P2_PARENS, "( ) ( )", tx("lessonspart2b_teste_suas_regras_em_com")),
            Reveal(tx("lessonspart2b_nesse_trace_quantas_vezes_foi"), tx("lessonspart2b_3_vezes_dentro_primeiro_par")),
            Say(PROF, tx("lessonspart2b_voce_reinventou_algoritmo_livro_dragao")),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.FORMAL, tx("lessonspart2b_analisador_preditivo_nao_recursivo_versao"),
        listOf(
            Say(PROF, tx("lessonspart2b_analisador_preditivo_nao_recursivo_dirigido")),
            Text(tx("lessonspart2b_componentes_buffer_entrada_terminado_em")),
            Code(
                tx("lessonspart2b_empilhe_empilhe_primeiro_simbolo_w"),
                tx("lessonspart2b_programa_controle_para_nada_empilhado")
            ),
            Text(tx("lessonspart2b_invariante_parte_entrada_ja_casada")),
            Predict(
                tx("lessonspart2b_configuracao_pilha_t_topo_direita"),
                listOf(tx("comum_erro"), "T' → ε, E' → ε, aceita", tx("lessonspart2b_casa_com_t"), "E' → + T E'"), 1,
                tx("lessonspart2b_m_t_t_m_pois")
            ),
            Visual(LL1_TRACE, P2_EXPR_LL, "( id )", tx("lessonspart2b_siga_pseudocodigo_linha_linha_nesta")),
            Callout(EXAM_TIP, tx("lessonspart2b_formato_esperado_colunas_pilha_entrada")),
            Callout(WATCH_OUT, tx("lessonspart2b_nao_confunda_pilhas_ll_guarda")),
            Reveal(tx("lessonspart2b_quais_sao_situacoes_programa_controle"), tx("lessonspart2b_x_aceita_x_casa_avanca")),
            Callout(REAL_WORLD, tx("lessonspart2b_antlr_gerador_analisadores_muito_usado")),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.MNEMONIC, tx("lessonspart2b_tres_frases_na_testa_golem"),
        listOf(
            Say(NARRADOR, tx("lessonspart2b_no_altar_templo_top_down_2")),
            Callout(MNEMONIC, tx("lessonspart2b_terminal_no_topo_casa_anda")),
            Callout(MNEMONIC, tx("lessonspart2b_empilhar_ao_contrario_arrumar_mochila")),
            Say(BYTE, tx("lessonspart2b_qual_diferenca_entre_pilha_daqui")),
            Callout(FIRESIDE, tx("lessonspart2b_ll_lr_ao_pe_lareira")),
            Steps(
                tx("comum_confusoes_classicas"),
                listOf(
                    tx("lessonspart2b_empilhar_lado_direito_na_ordem"),
                    tx("lessonspart2b_avancar_entrada_numa_expansao_nao"),
                    tx("lessonspart2b_empilhar_nao_existe_producao_so"),
                    tx("lessonspart2b_esquecer_comecar_com_na_pilha"),
                )
            ),
            Predict(
                tx("lessonspart2b_qual_acao_faz_entrada_avancar"),
                listOf(tx("comum_expandir"), tx("comum_casar_2"), tx("lessonspart2b_ambas"), tx("comum_nenhuma")), 1,
                tx("lessonspart2b_so_casar_consome_token_expandir")
            ),
            Visual(LL1_TRACE, P2_ANBN, tx("comum_b_b"), tx("lessonspart2b_teste_frases_golem_em_b")),
            Reveal(tx("lessonspart2b_recite_tres_frases_golem"), tx("lessonspart2b_terminal_no_topo_casa_anda_2")),
        )
    ),
)
