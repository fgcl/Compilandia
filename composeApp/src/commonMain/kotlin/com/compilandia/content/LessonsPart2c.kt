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
import com.compilandia.content.Speaker.LEXI
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.FIRST_FOLLOW
import com.compilandia.content.VisualKind.LEFT_FACTORING
import com.compilandia.content.VisualKind.LEFT_RECURSION
import com.compilandia.content.VisualKind.LR0_AUTOMATON
import com.compilandia.content.VisualKind.LR_TRACE
import com.compilandia.content.VisualKind.PARSE_TREE
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// ───────────────────────────── DESCIDA RECURSIVA ─────────────────────────────

internal val p2RecDescentLessons: List<Lesson> = listOf(
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.STORY, tx("lessonspart2c_reparticao_dos_funcionarios_funcao"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_corredor_final_templo_top_down")),
            Say(PROF, tx("lessonspart2c_na_descida_recursiva_cada_nao")),
            Code(P2_CMD, tx("lessonspart2c_gramatica_dos_comandos_reparticao")),
            Say(BYTE, tx("lessonspart2c_como_funcionario_sabe_que_fazer")),
            Say(PROF, tx("lessonspart2c_ele_olha_senha_proximo_cliente")),
            Text(tx("lessonspart2c_seguir_roteiro_simples_para_cada")),
            Predict(
                tx("lessonspart2c_recebeu_senha_begin_qual_sequencia"),
                listOf("match(begin); L(); match(end)", "L(); match(begin); match(end)", "match(begin); S(); match(end)", tx("lessonspart2c_chama")), 0,
                tx("lessonspart2c_roteiro_begin_l_end_confere")
            ),
            Visual(PARSE_TREE, P2_CMD, tx("lessonspart2c_begin_print_id_end"), tx("lessonspart2c_arvore_sintatica_organograma_das_chamadas")),
            Say(BYTE, tx("lessonspart2c_l_que_pode_ser_como")),
            Say(PROF, tx("lessonspart2c_se_senha_for_end_que")),
            Callout(WATCH_OUT, tx("lessonspart2c_funcionario_que_como_primeiro_ato")),
            Reveal(tx("lessonspart2c_quem_faz_papel_pilha_na"), tx("lessonspart2c_pilha_chamadas_proprio_programa_cada")),
            Callout(REAL_WORLD, tx("lessonspart2c_gcc_clang_compiladores_c_c")),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.VISUAL, tx("lessonspart2c_vendo_funcoes_se_chamarem"),
        listOf(
            Say(PROF, tx("lessonspart2c_vamos_ver_descida_recursiva_acontecendo")),
            Code(P2_EXPR_LL),
            Code(
                tx("lessonspart2c_fun_t_elinha_fun_elinha"),
                tx("lessonspart2c_funcao_por_nao_terminal_virou")
            ),
            Visual(DERIVATION, P2_EXPR_LL, "id * id", tx("lessonspart2c_cada_expansao_desta_derivacao_mais")),
            Steps(
                tx("lessonspart2c_chamadas_para_id_id"),
                listOf(
                    tx("lessonspart2c_chama_t"),
                    tx("lessonspart2c_t_chama_f_f_ve"),
                    tx("lessonspart2c_t_chama_tlinha_ve_match"),
                    tx("lessonspart2c_tlinha_ve_nao_faz_nada"),
                    tx("lessonspart2c_volta_em_que_chama_elinha"),
                    tx("lessonspart2c_retorna_com_look_sucesso"),
                )
            ),
            Predict(
                tx("lessonspart2c_ao_analisar_id_id_quantas"),
                listOf("1", "2", "3", "0"), 1,
                tx("lessonspart2c_por_id_t_chama_f")
            ),
            Visual(PARSE_TREE, P2_EXPR_LL, "id * id", tx("lessonspart2c_arvore_retrato_das_chamadas_nos")),
            Say(BYTE, tx("lessonspart2c_por_que_nao_usar_gramatica")),
            Visual(LEFT_RECURSION, P2_EXPR, caption = tx("lessonspart2c_porque_chamaria_sem_consumir_nada")),
            Reveal(tx("lessonspart2c_na_arvore_id_id_quantos"), tx("lessonspart2c_dois_primeiro_t_filho_t")),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.WORKED, tx("lessonspart2c_exemplo_resolvido_funcoes_das_declaracoes"),
        listOf(
            Say(PROF, tx("lessonspart2c_exercicio_prova_escreva_analisador_descida")),
            Code(P2_DECL),
            Text(tx("lessonspart2c_receita_funcao_por_nao_terminal")),
            Code(
                """
                fun match(t: String) {
                  if (look == t) look = proximoToken()
                  else erro("esperava " + t + ", veio " + look)
                }
                """.trimIndent(),
                tx("lessonspart2c_conferir_senha")
            ),
            Code(
                tx("lessonspart2c_fun_d_t_l_match"),
                tx("lessonspart2c_d_t_l_resolvidos_d")
            ),
            Say(BYTE, tx("lessonspart2c_r_que_tem")),
            Say(PROF, tx("lessonspart2c_esse_fica_para_voce_pense")),
            Reveal(tx("lessonspart2c_qual_token_escolhe_r_id"), tx("lessonspart2c_first_id_r")),
            Reveal(tx("lessonspart2c_qual_escolhe_r"), tx("lessonspart2c_follow_r_qualquer_outro_token")),
            Reveal(tx("lessonspart2c_escreva_r_completo"), tx("lessonspart2c_fun_r_when_look_match")),
            Visual(PARSE_TREE, P2_DECL, "int id , id ;", tx("lessonspart2c_cada_no_interno_chamada_repare")),
            Callout(EXAM_TIP, tx("lessonspart2c_nas_producoes_com_deixe_teste")),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.SOCRATIC, tx("lessonspart2c_inventando_descida_recursiva"),
        listOf(
            Say(PROF, tx("lessonspart2c_vamos_inventar_descida_recursiva_zero")),
            Predict(
                "S → a B c. Qual é o corpo mais natural de S()?",
                listOf("match(a); B(); match(c)", "B(); match(a); match(c)", "match(a); match(B); match(c)", "S(); B()"), 0,
                tx("lessonspart2c_siga_lado_direito_esquerda_para")
            ),
            Predict(
                "S → a B | c D. Como S() escolhe?",
                listOf(tx("lessonspart2c_tenta_primeira_se_falhar_volta"), tx("lessonspart2c_olha_token_primeira_c_segunda"), tx("lessonspart2c_sempre_primeira"), tx("lessonspart2c_sorteia")), 1,
                tx("lessonspart2c_com_first_disjuntos_token_basta")
            ),
            Predict(
                "E → E + T | T. O que acontece se E() começar chamando E()?",
                listOf(tx("lessonspart2c_funciona_normal"), tx("lessonspart2c_laco_infinito_estouro_pilha"), tx("comum_erro_sintaxe"), tx("lessonspart2c_le")), 1,
                tx("lessonspart2c_chama_sem_consumir_token_nenhum")
            ),
            Predict(
                "A → b A | ε. Com que tokens A() deve escolher o ε?",
                listOf(tx("lessonspart2c_com_b"), tx("lessonspart2c_com_tokens_follow"), tx("lessonspart2c_com_qualquer_token"), tx("comum_nunca")), 1,
                tx("lessonspart2c_se_token_nao_abre_b")
            ),
            Say(BYTE, tx("lessonspart2c_entao_descida_recursiva_tabela_ll")),
            Say(PROF, tx("lessonspart2c_exatamente_cada_when_look_linha")),
            Predict(
                "A → x y | x z. A descida recursiva preditiva funciona direto?",
                listOf(tx("comum_sim"), tx("lessonspart2c_nao_precisa_fatorar_esquerda_x"), tx("lessonspart2c_sim_se_usar_follow"), tx("lessonspart2c_nao_precisa_eliminar")), 1,
                tx("lessonspart2c_mesmo_token_x_abre_duas")
            ),
            Visual(LEFT_FACTORING, P2_FACT, caption = tx("lessonspart2c_veja_fatoracao_deixando_gramatica_pronta")),
            Reveal(tx("lessonspart2c_resuma_com_suas_palavras_como"), tx("lessonspart2c_funcao_por_nao_terminal_lookahead")),
            Visual(PARSE_TREE, P2_CMD, tx("lessonspart2c_if_id_then_print_id"), tx("lessonspart2c_treino_imagine_match_if_match")),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.FORMAL, tx("lessonspart2c_descida_recursiva_definicao_prova"),
        listOf(
            Say(PROF, tx("lessonspart2c_definicao_prova_no_estilo_livro")),
            Text(tx("lessonspart2c_analisador_descida_recursiva_conjunto_proced")),
            Code(
                tx("lessonspart2c_void_escolha_producao_x1_x2"),
                tx("lessonspart2c_procedimento_tipico_para_nao_terminal")
            ),
            Text(tx("lessonspart2c_na_forma_geral_escolha_producao")),
            Steps(
                tx("lessonspart2c_requisitos_descida_recursiva_preditiva"),
                listOf(
                    tx("lessonspart2c_sem_recursao_esquerda_direta_ou"),
                    tx("lessonspart2c_fatorada_esquerda_senao_token_nao"),
                    tx("lessonspart2c_condicoes_ll_1_satisfeitas_alternativas"),
                    tx("lessonspart2c_se_nenhuma_alternativa_serve_para"),
                )
            ),
            Predict(
                tx("lessonspart2c_qual_relacao_entre_descida_recursiva"),
                listOf(tx("lessonspart2c_reconhecem_classes_diferentes"), tx("lessonspart2c_mesma_classe_ll_1_pilha"), tx("lessonspart2c_descida_recursiva_bottom_up"), tx("lessonspart2c_tabela_nao_precisa_first")), 1,
                tx("lessonspart2c_sao_duas_implementacoes_mesma_ideia")
            ),
            Code(P2_CMD, tx("lessonspart2c_exercicio_tipico_escreva_l")),
            Reveal(tx("lessonspart2c_escreva_l_para_l_l"), tx("lessonspart2c_l_se_look_if_print")),
            Visual(FIRST_FOLLOW, P2_CMD, caption = "Os conjuntos que decidem cada if: FIRST(S) = {if, print, begin} e FOLLOW(L) = {end}."),
            Callout(EXAM_TIP, tx("lessonspart2c_na_prova_1_elimine_recursao")),
            Reveal(tx("lessonspart2c_por_que_recursao_direita_l"), tx("lessonspart2c_porque_antes_chamar_l_novo")),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.MNEMONIC, tx("lessonspart2c_variavel_funcao"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_ultimo_corredor_templo_top_down")),
            Callout(MNEMONIC, tx("lessonspart2c_variavel_funcao_terminal_match")),
            Callout(MNEMONIC, tx("lessonspart2c_recursao_esquerda_cachorro_correndo_atras")),
            Callout(MNEMONIC, tx("lessonspart2c_olha_senha_antes_atender_lookahead")),
            Say(LEXI, tx("lessonspart2c_eu_entrego_tokens_ja_cortadinhos")),
            Say(BYTE, tx("lessonspart2c_qual_diferenca_pra_pilha_golem")),
            Callout(FIRESIDE, tx("lessonspart2c_golem_pilha_descida_recursiva_ao")),
            Steps(
                tx("comum_confusoes_classicas"),
                listOf(
                    tx("lessonspart2c_descida_recursiva_nao_bottom_up"),
                    tx("lessonspart2c_recursao_direita_ok_esquerda_nao"),
                    tx("lessonspart2c_esquecer_checar_no_final_pode"),
                    tx("lessonspart2c_escolher_por_padrao_sem_checar"),
                )
            ),
            Predict(
                tx("lessonspart2c_qual_gramatica_nao_serve_para"),
                listOf("E → T E'", "L → S L | ε", "A → A b | c", "R → , id R | ε"), 2,
                "A → A b | c é recursiva à esquerda: A() chamaria A() sem consumir nada."
            ),
            Visual(PARSE_TREE, P2_DECL, "float id ;", tx("lessonspart2c_cada_no_interno_porta_aberta")),
            Reveal(tx("lessonspart2c_recite_dois_ganchos_principais"), tx("lessonspart2c_variavel_funcao_terminal_match_recursao")),
        )
    ),
)

// ───────────────────────────── EMPILHA-REDUZ ─────────────────────────────

internal val p2ShiftReduceLessons: List<Lesson> = listOf(
    Lesson(
        Skill.SHIFT_REDUCE, Style.STORY, tx("lessonspart2c_montando_lego_baixo_para_cima"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_bem_vindo_fortaleza_bottom_up")),
            Say(PROF, tx("lessonspart2c_imagine_montar_lego_seguindo_manual")),
            Code(P2_EXPR, tx("lessonspart2c_manual_repare_tem_recursao_esquerda")),
            Say(BYTE, tx("lessonspart2c_entao_eu_empilho_id_ja")),
            Say(PROF, tx("lessonspart2c_isso_id_no_topo_vira")),
            Predict(
                tx("lessonspart2c_pilha_t_proximo_token_reduzir"),
                listOf(tx("lessonspart2c_reduzir_para"), tx("lessonspart2c_empilhar"), tx("comum_aceitar"), tx("comum_tanto_faz")), 1,
                tx("lessonspart2c_se_virasse_teriamos_nenhuma_forma")
            ),
            Text(tx("lessonspart2c_kit_certo_no_lugar_certo")),
            Visual(LR_TRACE, P2_EXPR, "id * id", tx("lessonspart2c_assista_montagem_empilha_reduz_reduz")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart2c_p_quem_me_diz_qual")),
            Say(PILHA, tx("lessonspart2c_aqui_eu_guardo_passado_que")),
            Reveal(tx("lessonspart2c_quais_sao_4_acoes_analisador"), tx("lessonspart2c_empilhar_shift_reduzir_reduce_aceitar")),
            Callout(REAL_WORLD, tx("lessonspart2c_yacc_bison_geram_analisadores_empilha")),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.VISUAL, tx("lessonspart2c_empilha_reduz_em_camera_lenta"),
        listOf(
            Say(PROF, tx("lessonspart2c_vamos_ver_analisador_empilha_reduz")),
            Code(P2_EXPR),
            Visual(LR_TRACE, P2_EXPR, "id + id * id", tx("lessonspart2c_avance_passo_passo_repare_quando")),
            Steps(
                "Momentos-chave",
                listOf(
                    tx("lessonspart2c_empilha_id_reduz_f_id"),
                    "Empilha + e o segundo id; reduz F → id e T → F.",
                    tx("lessonspart2c_agora_topo_t_vem_ele"),
                    tx("lessonspart2c_monta_t_f_reduz_t"),
                    tx("lessonspart2c_pilha_com_entrada_aceita"),
                )
            ),
            Predict(
                tx("lessonspart2c_com_t_no_topo_na"),
                listOf(tx("lessonspart2c_porque_t_f_ainda_precisa"), tx("lessonspart2c_porque_pilha_esta_cheia"), tx("lessonspart2c_porque_t_nao_producao"), tx("lessonspart2c_porque_proibido_reduzir_com_na")), 0,
                tx("lessonspart2c_se_reduzisse_ficaria_que_nao")
            ),
            Visual(PARSE_TREE, P2_EXPR, "id + id * id", tx("lessonspart2c_arvore_montada_baixo_para_cima")),
            Say(BYTE, tx("lessonspart2c_entao_trace_lido_tras_pra")),
            Say(PROF, tx("lessonspart2c_isso_reducoes_lidas_ao_contrario")),
            Reveal(tx("lessonspart2c_na_entrada_id_id_qual"), "E → T. A raiz da árvore é sempre a última peça encaixada."),
            Callout(BRAIN_POWER, tx("lessonspart2c_analisador_nunca_precisa_procurar_handle")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2c_porque_basta_olhar_topo_pilha")),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.WORKED, tx("lessonspart2c_exemplo_resolvido_id_id"),
        listOf(
            Say(PROF, tx("lessonspart2c_exercicio_resolvido_analise_empilha_reduz")),
            Code(P2_EXPR),
            Steps(
                tx("lessonspart2c_resolucao"),
                listOf(
                    tx("lessonspart2c_id_id_empilhar"),
                    tx("lessonspart2c_id_id_reduzir_f_id"),
                    tx("lessonspart2c_f_id_reduzir_t_f"),
                    tx("lessonspart2c_t_id_empilhar_nao_reduza"),
                    tx("lessonspart2c_t_id_empilhar"),
                    tx("lessonspart2c_t_id_reduzir_f_id"),
                    tx("lessonspart2c_t_f_reduzir_t_t"),
                    tx("lessonspart2c_t_reduzir_t"),
                    tx("lessonspart2c_aceitar"),
                )
            ),
            Visual(LR_TRACE, P2_EXPR, "id * id", tx("lessonspart2c_confira_com_motor_ele_mostra")),
            Say(BYTE, tx("lessonspart2c_no_passo_4_eu_teria")),
            Say(PROF, tx("lessonspart2c_jeito_nenhum_erro_mais_comum")),
            Say(PROF, tx("lessonspart2c_agora_desbotado_gramatica_b_entrada")),
            Code(P2_LIST),
            Text(tx("lessonspart2c_eu_comeco_b_empilhar_depois")),
            Reveal(tx("lessonspart2c_pilha_entrada_acao"), tx("lessonspart2c_reduzir_pilha_vira")),
            Reveal(tx("lessonspart2c_pilha_entrada_agora"), tx("lessonspart2c_empilhar_depois_com_entrada_reduzir")),
            Reveal(tx("lessonspart2c_final"), tx("lessonspart2c_pilha_entrada_aceitar")),
            Visual(LR_TRACE, P2_LIST, tx("comum_b"), tx("lessonspart2c_confira_recursao_esquerda_proibida_no")),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.SOCRATIC, tx("lessonspart2c_descobrindo_que_handle"),
        listOf(
            Say(PROF, tx("lessonspart2c_perguntas_para_voce_descobrir_sozinho")),
            Code(P2_HANDLE, tx("lessonspart2c_exemplo_classico_livro_dragao_com")),
            Predict(
                tx("lessonspart2c_pilha_b_entrada_b_c"),
                listOf(tx("comum_sim"), tx("comum_nao_2")), 0,
                tx("lessonspart2c_sim_chegamos_b_c_d")
            ),
            Predict(
                tx("lessonspart2c_agora_pilha_b_entrada_c"),
                listOf(tx("lessonspart2c_sim_b_sempre_handle"), tx("lessonspart2c_nao_certo_empilhar_c_reduzir")), 1,
                tx("lessonspart2c_reduzir_daria_c_d_que")
            ),
            Say(BYTE, tx("lessonspart2c_entao_handle_nao_so_achei")),
            Reveal(tx("lessonspart2c_tente_definir_handle_com_suas"), tx("lessonspart2c_ocorrencia_lado_direito_que_reduzida")),
            Predict(
                tx("lessonspart2c_em_que_posicao_pilha_handle"),
                listOf(tx("lessonspart2c_sempre_no_topo"), tx("lessonspart2c_sempre_no_fundo"), tx("lessonspart2c_em_qualquer_lugar"), tx("lessonspart2c_no_meio")), 0,
                tx("lessonspart2c_resultado_importante_numa_analise_empilha")
            ),
            Predict(
                tx("lessonspart2c_se_em_algum_momento_analisador"),
                listOf(tx("lessonspart2c_conflito_empilha_reduz_shift_reduce"), tx("lessonspart2c_conflito_reduz_reduz"), tx("comum_erro_lexico"), tx("lessonspart2c_aceite")), 0,
                tx("lessonspart2c_conflito_shift_reduce_se_ele")
            ),
            Visual(LR_TRACE, P2_HANDLE, tx("lessonspart2c_b_b_c_d"), tx("lessonspart2c_veja_analisador_acertando_handle_em")),
            Reveal(tx("lessonspart2c_escreva_sequencia_reducoes_analise_b"), "A → b, A → A b c, B → d, S → a A B e. Lida ao contrário, é a derivação mais à direita: S ⇒ a A B e ⇒ a A d e ⇒ a A b c d e ⇒ a b b c d e."),
            Say(PROF, tx("lessonspart2c_voce_reconstruiu_sozinho_ideia_central")),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.FORMAL, tx("lessonspart2c_empilha_reduz_definicoes_prova"),
        listOf(
            Say(PROF, tx("lessonspart2c_definicoes_prova_sem_enrolacao")),
            Text(tx("lessonspart2c_analise_empilha_reduz_analise_ascendente")),
            Steps(
                tx("lessonspart2c_quatro_acoes"),
                listOf(
                    tx("lessonspart2c_empilhar_shift_move_proximo_simbolo"),
                    tx("lessonspart2c_reduzir_reduce_topo_contem_lado"),
                    tx("lessonspart2c_aceitar_accept_anuncia_termino_com"),
                    tx("lessonspart2c_erro_error_detecta_erro_sintaxe"),
                )
            ),
            Text(tx("lessonspart2c_handle_se_rm_w_rm")),
            Text(tx("lessonspart2c_prefixo_viavel_prefixo_forma_sentencial")),
            Predict(
                tx("lessonspart2c_sequencia_reducoes_analisador_empilha_reduz"),
                listOf(tx("comum_derivacao_mais_esquerda"), tx("comum_derivacao_mais_direita"), tx("lessonspart2c_arvore_ambigua"), tx("comum_tabela_ll_1")), 1,
                tx("lessonspart2c_bottom_up_derivacao_mais_direita")
            ),
            Steps(
                tx("comum_conflitos"),
                listOf(
                    tx("lessonspart2c_shift_reduce_nao_para_decidir"),
                    tx("lessonspart2c_reduce_reduce_nao_para_decidir"),
                    tx("lessonspart2c_gramaticas_com_conflito_para_metodo"),
                )
            ),
            Visual(LR_TRACE, P2_EXPR, "( id )", tx("lessonspart2c_siga_definicoes_na_analise_id")),
            Callout(EXAM_TIP, tx("lessonspart2c_resposta_padrao_tabela_pilha_entrada")),
            Reveal(tx("lessonspart2c_defina_handle_em_frase"), tx("lessonspart2c_subcadeia_que_casa_com_lado")),
            Callout(FIRESIDE, tx("lessonspart2c_ll_vs_lr_ao_pe")),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.MNEMONIC, tx("lessonspart2c_empilha_ate_ver_alca"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_portao_fortaleza_bottom_up_esteira")),
            Callout(MNEMONIC, tx("lessonspart2c_empilha_ate_ver_alca_achou")),
            Callout(MNEMONIC, tx("lessonspart2c_lr_le_esquerda_reconstroi_mais")),
            Callout(MNEMONIC, tx("lessonspart2c_reduzir_tira_lado_direito_poe")),
            Steps(
                tx("lessonspart2c_top_down_bottom_up"),
                listOf(
                    tx("lessonspart2c_top_down_templo_comeca_pela"),
                    tx("lessonspart2c_ll_derivacao_mais_esquerda_na"),
                    tx("lessonspart2c_pilha_ll_que_falta_futuro"),
                    tx("lessonspart2c_recursao_esquerda_proibida_no_ll"),
                )
            ),
            Say(BYTE, tx("lessonspart2c_shift_empilhar_ou_deslocar")),
            Say(PROF, tx("lessonspart2c_dois_token_se_desloca_entrada")),
            Predict(
                tx("lessonspart2c_qual_acao_consome_token_entrada"),
                listOf(tx("comum_reduzir"), tx("comum_empilhar_2"), tx("comum_aceitar"), tx("lessonspart2c_todas")), 1,
                tx("lessonspart2c_so_shift_consome_entrada_reduzir")
            ),
            Visual(LR_TRACE, P2_ANBN, tx("comum_b_b"), tx("lessonspart2c_treine_gancho_onde_esta_alca")),
            Reveal(tx("lessonspart2c_recite_tres_ganchos"), tx("lessonspart2c_empilha_ate_ver_alca_achou_2")),
        )
    ),
)

// ───────────────────────────── ITENS LR(0) ─────────────────────────────

internal val p2Lr0Lessons: List<Lesson> = listOf(
    Lesson(
        Skill.LR0_ITEMS, Style.STORY, tx("lessonspart2c_marcador_pagina"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_torre_central_fortaleza_bottom_up")),
            Say(PROF, tx("lessonspart2c_item_lr_0_producao_com")),
            Code(P2_CC, tx("lessonspart2c_nosso_livro_receitas_gera_cadeias")),
            Say(BYTE, tx("lessonspart2c_tipo_barra_progresso_download")),
            Say(PROF, tx("lessonspart2c_isso_ponto_no_comeco_c")),
            Predict(
                tx("lessonspart2c_quantos_itens_lr_0_producao"),
                listOf("1", "2", "3", "4"), 2,
                "S → • C C, S → C • C e S → C C •. Uma produção com n símbolos dá n + 1 itens."
            ),
            Say(PROF, tx("lessonspart2c_agora_magica_fechamento_closure_se")),
            Callout(MNEMONIC, tx("lessonspart2c_fechamento_convite_em_cascata_nao")),
            Say(PROF, tx("lessonspart2c_desvio_goto_andar_ponto_por")),
            Visual(LR0_AUTOMATON, P2_CC, caption = tx("lessonspart2c_predio_inteiro_7_andares_estados")),
            Reveal(tx("lessonspart2c_no_estado_i0_quais_itens"), "S' → • S, S → • C C, C → • c C, C → • d. O S convidou S → • C C; o C convidou as duas produções de C."),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart2c_p_que_esse_r_gramatica")),
            Say(BYTE, tx("lessonspart2c_marcador_convite_passo_itens_closure")),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.VISUAL, tx("lessonspart2c_explorando_automato_lr_0"),
        listOf(
            Say(PROF, tx("lessonspart2c_vamos_explorar_automato_lr_0")),
            Code(P2_CC),
            Visual(LR0_AUTOMATON, P2_CC, caption = tx("lessonspart2c_toque_em_cada_estado_para")),
            Steps(
                tx("lessonspart2c_roteiro_exploracao"),
                listOf(
                    "I0: o estado inicial, fechamento de { S' → • S }.",
                    tx("lessonspart2c_i0_i1_estado_aceitacao"),
                    tx("lessonspart2c_i0_c_i2_c_c"),
                    tx("lessonspart2c_i0_c_i3_c_c"),
                    tx("lessonspart2c_i0_d_i4_c_d"),
                    tx("lessonspart2c_i5_c_c_i6_c"),
                )
            ),
            Predict(
                tx("lessonspart2c_i2_lendo_d_vamos_para"),
                listOf(tx("lessonspart2c_estado_novo"), "I4", "I3", "I6"), 1,
                tx("lessonspart2c_goto_i2_d_c_d")
            ),
            Say(BYTE, tx("lessonspart2c_por_que_i3_lendo_c")),
            Say(PROF, tx("lessonspart2c_porque_goto_i3_c_c")),
            Visual(LR0_AUTOMATON, P2_EXPR, caption = tx("lessonspart2c_agora_gramatica_maior_12_estados")),
            Reveal(tx("lessonspart2c_que_problema_esse_estado_t"), tx("lessonspart2c_conflito_empilha_reduz_no_item")),
            Callout(BRAIN_POWER, tx("lessonspart2c_automato_lr_0_reconhece_exatamente")),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.WORKED, tx("lessonspart2c_exemplo_resolvido_colecao_canonica_c"),
        listOf(
            Say(PROF, tx("lessonspart2c_exercicio_prova_construa_colecao_canonica")),
            Code(P2_CC),
            Steps(
                tx("lessonspart2c_resolucao"),
                listOf(
                    tx("lessonspart2c_passo_0_aumente_gramatica_0"),
                    "I0 = closure({ S' → • S }) = { S' → • S, S → • C C, C → • c C, C → • d }.",
                    "goto(I0, S) = { S' → S • } = I1.",
                    "goto(I0, C) = closure({ S → C • C }) = { S → C • C, C → • c C, C → • d } = I2.",
                    "goto(I0, c) = closure({ C → c • C }) = { C → c • C, C → • c C, C → • d } = I3.",
                    "goto(I0, d) = { C → d • } = I4.",
                    tx("lessonspart2c_goto_i2_c_c_c"),
                )
            ),
            Visual(LR0_AUTOMATON, P2_CC, caption = tx("lessonspart2c_confira_estados_i0_i5_com")),
            Say(BYTE, tx("lessonspart2c_falta_i3_ne")),
            Say(PROF, tx("lessonspart2c_falta_essa_parte_sua_lembre")),
            Reveal(tx("lessonspart2c_goto_i3_c"), "{ C → c C • }: um estado novo, I6."),
            Reveal(tx("lessonspart2c_goto_i3_c_2"), tx("lessonspart2c_closure_c_c_c_c")),
            Reveal(tx("lessonspart2c_goto_i3_d"), "{ C → d • } = I4."),
            Reveal(tx("lessonspart2c_quantos_estados_ao_todo_quais"), tx("lessonspart2c_7_estados_i0_i6_itens")),
            Callout(EXAM_TIP, tx("lessonspart2c_numere_producoes_gramatica_aumentada_desenhe")),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.SOCRATIC, tx("lessonspart2c_por_que_itens_closure_goto"),
        listOf(
            Say(PROF, tx("lessonspart2c_vamos_redescobrir_itens_closure_goto")),
            Predict(
                tx("lessonspart2c_num_analisador_empilha_reduz_que"),
                listOf(tx("lessonspart2c_so_proximo_token"), tx("lessonspart2c_em_que_ponto_das_producoes"), tx("lessonspart2c_arvore_inteira"), tx("lessonspart2c_first_tudo")), 1,
                tx("lessonspart2c_para_saber_se_handle_ja")
            ),
            Predict(
                tx("lessonspart2c_item_x_y_z_significa"),
                listOf(tx("lessonspart2c_ja_reduzi"), tx("lessonspart2c_ja_vi_x_y_espero"), tx("lessonspart2c_espero_x_y_z"), tx("comum_erro")), 1,
                tx("lessonspart2c_ponto_separa_passado_que_esta")
            ),
            Predict(
                tx("lessonspart2c_se_espero_z_item_x"),
                listOf(tx("comum_nada"), "Z → • w e Z → • v", "Z → w • e Z → v •", "A → • X Y Z"), 1,
                tx("lessonspart2c_esperar_z_esperar_comeco_alguma")
            ),
            Predict(
                tx("lessonspart2c_se_item_x_b_y"),
                listOf(tx("lessonspart2c_todos_b"), tx("lessonspart2c_nenhum_terminal_nao_tem_producoes"), "Um", tx("lessonspart2c_dois")), 1,
                tx("lessonspart2c_fechamento_so_convida_quando_ha")
            ),
            Predict(
                tx("lessonspart2c_estou_no_conjunto_itens_i"),
                listOf(tx("lessonspart2c_i_novo"), tx("lessonspart2c_itens_com_antes_x_com"), tx("lessonspart2c_todos_itens_que_contem_x"), tx("lessonspart2c_vazio")), 1,
                tx("lessonspart2c_isso_goto_i_x_avanca")
            ),
            Reveal(tx("lessonspart2c_por_que_numero_estados_finito"), tx("lessonspart2c_ha_numero_finito_itens_producoes")),
            Predict(
                tx("lessonspart2c_para_que_serve_producao_gramatica"),
                listOf(tx("comum_enfeite"), tx("lessonspart2c_ter_item_aceitacao_unico"), tx("lessonspart2c_evitar_recursao_esquerda"), tx("lessonspart2c_calcular_follow")), 1,
                tx("lessonspart2c_quando_analisador_chegaria_reduzir_para")
            ),
            Visual(LR0_AUTOMATON, P2_LIST, caption = tx("lessonspart2c_teste_b_antes_olhar_quantos")),
            Reveal(tx("lessonspart2c_quais_sao_itens_i0"), tx("lessonspart2c_3_b_convidou_suas_duas")),
            Say(PROF, tx("lessonspart2c_recursao_esquerda_no_closure_sem")),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.FORMAL, tx("lessonspart2c_itens_closure_goto_colecao_canonica"),
        listOf(
            Say(PROF, tx("lessonspart2c_definicoes_formais_como_no_livro")),
            Text(tx("lessonspart2c_item_lr_0_producao_g")),
            Text(tx("lessonspart2c_gramatica_aumentada_g_g_com")),
            Code(
                tx("lessonspart2c_closure_i_j_i_repita"),
                tx("lessonspart2c_fechamento_conjunto_itens")
            ),
            Code(
                tx("lessonspart2c_goto_i_x_closure_x"),
                tx("lessonspart2c_desvio_avanca_ponto_sobre_x")
            ),
            Code(
                tx("lessonspart2c_itens_g_c_closure_repita"),
                tx("lessonspart2c_colecao_canonica_conjuntos_itens_lr")
            ),
            Text(tx("lessonspart2c_itens_nucleo_kernel_todos_itens")),
            Predict(
                tx("lessonspart2c_qual_destes_item_nucleo"),
                listOf("C → • c C", "S → • C C", "C → c • C", "C → • d"), 2,
                "C → c • C tem o ponto depois do início. Os outros têm o ponto na extremidade esquerda (e não são S' → • S): vieram do fechamento."
            ),
            Visual(LR0_AUTOMATON, P2_CC, caption = tx("lessonspart2c_identifique_em_cada_estado_quais")),
            Callout(EXAM_TIP, tx("lessonspart2c_pergunta_tipica_calcule_closure_ou")),
            Reveal(tx("lessonspart2c_qual_relacao_entre_automato_lr"), tx("lessonspart2c_automato_reconhece_exatamente_prefixos_viave")),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.MNEMONIC, tx("lessonspart2c_voce_esta_aqui"),
        listOf(
            Say(NARRADOR, tx("lessonspart2c_torre_fortaleza_bottom_up_num")),
            Callout(MNEMONIC, tx("lessonspart2c_ponto_voce_esta_aqui_esquerda")),
            Callout(MNEMONIC, tx("lessonspart2c_closure_convida_familia_ponto_antes")),
            Callout(MNEMONIC, tx("lessonspart2c_goto_gol_chuta_ponto_passo")),
            Steps(
                tx("comum_confusoes_classicas"),
                listOf(
                    tx("lessonspart2c_closure_so_adiciona_itens_com"),
                    tx("lessonspart2c_goto_avanca_ponto_so_nos"),
                    tx("lessonspart2c_esquecer_fechar_depois_goto"),
                    tx("lessonspart2c_criar_estado_repetido_conjunto_igual"),
                    tx("lessonspart2c_item_completo_ponto_no_fim"),
                )
            ),
            Predict(
                "Byte calculou goto(I0, c) = { C → c • C } e parou. O que faltou?",
                listOf(tx("comum_nada"), tx("lessonspart2c_fechar_c_c_c_c"), tx("lessonspart2c_adicionar_c_c"), tx("lessonspart2c_remover_ponto")), 1,
                tx("lessonspart2c_goto_avancar_fechar_como_ponto")
            ),
            Callout(FIRESIDE, tx("lessonspart2c_item_estado_ao_pe_lareira")),
            Visual(LR0_AUTOMATON, P2_ANBN, caption = tx("lessonspart2c_treino_rapido_em_b_ache")),
            Reveal(tx("lessonspart2c_recite_tres_ganchos"), tx("lessonspart2c_ponto_voce_esta_aqui_closure")),
        )
    ),
)
