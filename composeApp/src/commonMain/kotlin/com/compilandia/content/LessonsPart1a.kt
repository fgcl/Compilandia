package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 1a do conteúdo: PHASES, TOKENS, REGEX_DFA (Porto da Partida e Floresta Léxica).

// ───────────────────────────── PHASES ─────────────────────────────

private val p1aPhases: List<Lesson> = listOf(
    Lesson(
        Skill.PHASES, Style.STORY, tx("lessonspart1a_lanchonete_compilador"),
        listOf(
            Say(NARRADOR, tx("lessonspart1a_sexta_noite_lanchonete_ze_esta")),
            Say(BYTE, tx("lessonspart1a_perai_que_lanchonete_tem_ver")),
            Say(PROF, tx("lessonspart1a_tudo_pedido_passa_por_linha")),
            Steps(
                tx("lessonspart1a_linha_montagem_lanchonete"),
                listOf(
                    tx("lessonspart1a_atendente_le_papel_letra_por"),
                    tx("lessonspart1a_chefe_confere_se_pedido_esta"),
                    tx("lessonspart1a_gerente_confere_sentido_suco_feijoada"),
                    tx("lessonspart1a_pedido_vira_comanda_padronizada_que"),
                    tx("lessonspart1a_cozinha_percebe_que_para_esquentar"),
                    tx("lessonspart1a_lanche_sai_pronto_geracao_codigo"),
                ),
            ),
            Say(BYTE, tx("lessonspart1a_se_alguem_linha_errar")),
            Say(PROF, tx("lessonspart1a_cada_fase_pega_tipo_erro")),
            Predict(
                tx("lessonspart1a_cliente_escreveu_x_tudo_batata"),
                listOf(tx("lessonspart1a_atendente_lexico"), tx("lessonspart1a_chefe_sintatico"), tx("lessonspart1a_gerente_semantico"), tx("lessonspart1a_cozinheiro_geracao_codigo")),
                0,
                tx("lessonspart1a_nem_simbolo_cardapio_atendente_nao"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart1a_caderninho_onde_atendente_anota_nome")),
            Reveal(tx("lessonspart1a_quem_caderninho_que_todas_fases"), tx("lessonspart1a_tabela_simbolos_guarda_nomes_programa")),
            Callout(FIRESIDE, tx("lessonspart1a_atendente_chefe_gerente_entendem_pedido")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1a_p_fases_rodam_depois_outra")),
            Reveal(tx("lessonspart1a_sem_olhar_seis_fases_em"), tx("lessonspart1a_lexica_sintatica_semantica_codigo_intermedia")),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.VISUAL, tx("lessonspart1a_veja_programa_atravessar_esteira"),
        listOf(
            Say(PROF, tx("lessonspart1a_hoje_voce_vai_ver_fases")),
            Visual(VK.PIPELINE, caption = tx("lessonspart1a_toque_em_cada_fase_que")),
            Text(tx("lessonspart1a_repare_entrada_sequencia_caracteres_saida")),
            Say(LEXI, tx("lessonspart1a_minha_vez_eu_pego_caracteres")),
            Visual(VK.TOKENIZE, input = "position = initial + rate * 60;", caption = tx("lessonspart1a_cada_cor_classe_token_espacos")),
            Predict(
                "Quantos tokens tem `position = initial + rate * 60;`?",
                listOf("7", "8", tx("lessonspart1a_31_por_caractere"), "5"),
                1,
                tx("lessonspart1a_8_position_initial_rate_60"),
            ),
            Say(BYTE, tx("lessonspart1a_depois_dos_tokens_que_acontece")),
            Say(PROF, tx("lessonspart1a_sintatico_monta_arvore_semantico_pendura")),
            Visual(VK.ATTR_TREE, input = tx("comum_x_2"), caption = tx("lessonspart1a_int_x_float_veja_onde")),
            Callout(WATCH_OUT, tx("lessonspart1a_descobrir_que_int_misturado_com")),
            Visual(VK.TAC, input = tx("comum_b_c"), caption = tx("lessonspart1a_codigo_tres_enderecos_no_maximo")),
            Text(tx("lessonspart1a_repare_na_ordem_b_c")),
            Reveal(tx("lessonspart1a_por_que_se_chama_codigo"), tx("lessonspart1a_cada_instrucao_tem_no_maximo")),
            Callout(REAL_WORLD, tx("lessonspart1a_clang_front_end_c_c")),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.WORKED, tx("lessonspart1a_position_initial_rate_60_fase"),
        listOf(
            Say(PROF, tx("lessonspart1a_vamos_resolver_com_calma_exemplo")),
            Steps(
                tx("lessonspart1a_resolvido_fase_fase"),
                listOf(
                    tx("lessonspart1a_1_lexica_id_1_id"),
                    tx("lessonspart1a_2_sintatica_arvore_com_na"),
                    tx("lessonspart1a_3_semantica_rate_float_60"),
                    tx("lessonspart1a_4_codigo_intermediario_t1_inttofloat"),
                    tx("lessonspart1a_5_otimizacao_converte_60_em"),
                    tx("lessonspart1a_6_geracao_codigo_ldf_r2"),
                ),
            ),
            Code("t1 = inttofloat(60)\nt2 = id3 * t1\nt3 = id2 + t2\nid1 = t3", tx("lessonspart1a_codigo_intermediario_antes_otimizar")),
            Say(BYTE, tx("lessonspart1a_como_otimizacao_conseguiu_sumir_com")),
            Say(PROF, tx("lessonspart1a_porque_60_constante_compilador_converte")),
            Callout(WATCH_OUT, tx("lessonspart1a_na_tabela_simbolos_position_1")),
            Text(tx("lessonspart1a_agora_sua_vez_num_exemplo")),
            Text(tx("lessonspart1a_lexica_id_1_id_2")),
            Reveal(tx("lessonspart1a_semantica_precisa_alguma_conversao"), tx("lessonspart1a_nao_preco_int_2_int")),
            Reveal(tx("lessonspart1a_codigo_intermediario_tres_enderecos"), "t1 = id2 * 2\nid1 = t1"),
            Reveal(tx("lessonspart1a_otimizacao_para_melhorar"), tx("lessonspart1a_sim_eliminar_temporario_id1_id2")),
            Visual(VK.TAC, input = tx("lessonspart1a_2"), caption = tx("lessonspart1a_confira_com_motor_no_lugar")),
            Callout(EXAM_TIP, tx("lessonspart1a_questao_classica_mostre_saida_cada")),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.SOCRATIC, tx("lessonspart1a_por_que_tantas_fases"),
        listOf(
            Say(PROF, tx("lessonspart1a_hoje_eu_nao_vou_explicar")),
            Predict(
                tx("solta_lessonspart1a_1"),
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("lessonspart1a_ninguem_compila_normal")),
                0,
                tx("lessonspart1a_nao_pertence_nenhum_padrao_token"),
            ),
            Predict(
                "E em `x = (3 + 4;`?",
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("lessonspart1a_ninguem_compila_normal")),
                1,
                tx("lessonspart1a_todos_tokens_sao_validos_x"),
            ),
            Predict(
                "E em `int x; x = \"oi\";`?",
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("lessonspart1a_ninguem_compila_normal")),
                2,
                tx("lessonspart1a_tokens_ok_estrutura_ok_atribuicao"),
            ),
            Predict(
                tx("solta_lessonspart1a_2"),
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("lessonspart1a_ninguem_compila_normal")),
                2,
                tx("lessonspart1a_estrutura_atribuicao_perfeita_saber_se"),
            ),
            Say(BYTE, tx("lessonspart1a_hmm_cada_fase_pega_tipo")),
            Say(PROF, tx("lessonspart1a_isso_agora_outra_pergunta_imagine")),
            Reveal(
                tx("lessonspart1a_se_cada_compilador_fosse_direto"),
                tx("lessonspart1a_direto_5_4_20_compiladores"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart1a_se_voce_digitar_whlie_x")),
            Reveal(tx("lessonspart1a_pense_whlie_combina_com_padrao"), tx("lessonspart1a_combina_letras_seguidas_letras_identificador")),
            Say(PROF, tx("lessonspart1a_percebeu_voce_acabou_deduzir_sozinho")),
            Reveal(tx("lessonspart1a_resuma_cada_fase_analise_em"), tx("lessonspart1a_lexica_caracteres_formam_palavras_validas")),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.FORMAL, tx("lessonspart1a_compilador_fases_definicoes_prova"),
        listOf(
            Text(tx("lessonspart1a_definicao_compilador_programa_que_le")),
            Text(tx("lessonspart1a_interpretador_em_vez_produzir_programa")),
            Text(tx("lessonspart1a_compilador_tem_duas_partes_analise")),
            Code(
                tx("lessonspart1a_fluxo_caracteres_analisador_lexico_fluxo"),
                tx("lessonspart1a_fases_compilador_aho_lam_sethi"),
            ),
            Steps(
                tx("lessonspart1a_que_cada_fase_faz_formato"),
                listOf(
                    tx("lessonspart1a_analise_lexica_scanning_le_caracteres"),
                    tx("lessonspart1a_analise_sintatica_parsing_usa_tokens"),
                    tx("lessonspart1a_analise_semantica_usa_arvore_tabela"),
                    tx("lessonspart1a_geracao_codigo_intermediario_produz_represen"),
                    tx("lessonspart1a_otimizacao_codigo_melhora_codigo_intermediar"),
                    tx("lessonspart1a_geracao_codigo_mapeia_representacao_intermed"),
                ),
            ),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1a_p_fase_mesmo_que_passada")),
            Predict(
                tx("lessonspart1a_qual_fase_recebe_como_entrada"),
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("comum_geracao_codigo")),
                1,
                tx("lessonspart1a_lexico_produz_tokens_sintatico_consome"),
            ),
            Predict(
                tx("lessonspart1a_verificacao_tipos_tarefa_qual_fase"),
                listOf(tx("lessonspart1a_analise_lexica"), tx("lessonspart1a_analise_sintatica"), tx("comum_analise_semantica"), tx("comum_otimizacao_2")),
                2,
                tx("lessonspart1a_tipos_sao_significado_analise_semantica"),
            ),
            Callout(EXAM_TIP, tx("lessonspart1a_em_questao_discursiva_escreva_para")),
            Reveal(tx("lessonspart1a_diferenca_entre_compilador_interpretador_em"), tx("lessonspart1a_compilador_traduz_programa_inteiro_para")),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.MNEMONIC, tx("lessonspart1a_la_sempre_sai_isso_otimo"),
        listOf(
            Say(LEXI, tx("lessonspart1a_decorar_seis_fases_em_ordem")),
            Callout(MNEMONIC, tx("lessonspart1a_l_empre_ai_i_sso")),
            Say(PROF, tx("lessonspart1a_imagine_narrador_radio_gritando_toda")),
            Text(tx("lessonspart1a_palacio_memoria_mapa_compilandia_pipeline")),
            Reveal(tx("lessonspart1a_feche_olhos_ande_pelo_mapa"), tx("solta_lessonspart1a_3")),
            Callout(MNEMONIC, tx("lessonspart1a_zoom_crescente_letra_palavra_frase")),
            Text(tx("lessonspart1a_frase_famosa_chomsky_ideias_verdes")),
            Predict(
                tx("lessonspart1a_gato_bebeu_em_que_nivel"),
                listOf(tx("lessonspart1a_palavra_lexico"), tx("lessonspart1a_estrutura_sintaxe"), tx("lessonspart1a_sentido_semantica")),
                1,
                tx("lessonspart1a_todas_palavras_existem_mas_frase"),
            ),
            Callout(WATCH_OUT, tx("lessonspart1a_confusao_classica_variavel_nao_declarada")),
            Callout(MNEMONIC, tx("lessonspart1a_front_end_entende_back_end")),
            Visual(VK.PIPELINE, caption = tx("lessonspart1a_recite_mnemonico_tocando_em_cada")),
            Reveal(tx("lessonspart1a_recite_mnemonico_diga_que_sai"), tx("lessonspart1a_lexica_tokens_sintatica_arvore_semantica")),
        ),
    ),
)

// ───────────────────────────── TOKENS ─────────────────────────────

private val p1aTokens: List<Lesson> = listOf(
    Lesson(
        Skill.TOKENS, Style.STORY, tx("lessonspart1a_caixa_supermercado"),
        listOf(
            Say(NARRADOR, tx("lessonspart1a_supermercado_em_dia_promocao_lexi")),
            Say(LEXI, tx("lessonspart1a_bip_guarana_2l_bip_pao")),
            Say(PROF, tx("lessonspart1a_repare_em_tres_coisas_diferentes")),
            Steps(
                "Supermercado → compilador",
                listOf(
                    tx("lessonspart1a_produto_concreto_guarana_2l_lexema"),
                    tx("lessonspart1a_categoria_no_cupom_bebida_token"),
                    tx("lessonspart1a_regra_todo_liquido_engarrafado_bebida"),
                ),
            ),
            Say(BYTE, tx("lessonspart1a_entao_guarana_2l_guarana_lata")),
            Say(PROF, tx("lessonspart1a_exatamente_como_x_total_lexemas")),
            Text(tx("lessonspart1a_mas_no_cupom_categoria_sozinha")),
            Predict(
                tx("lessonspart1a_em_if_x_10_qual"),
                listOf(tx("lessonspart1a_relop_operador_relacional"), ">=", "x >= 10", "if"),
                1,
                tx("lessonspart1a_texto_real_lexema_relop_nome"),
            ),
            Say(LEXI, tx("lessonspart1a_tem_mais_eu_sempre_pego")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1a_p_if_ele_tambem_casa")),
            Visual(VK.TOKENIZE, input = "if (iff >= 10) whilex = whilex + 1;", caption = tx("lessonspart1a_repare_iff_whilex_sao_identificadores")),
            Callout(REAL_WORLD, tx("lessonspart1a_por_isso_em_c_voce")),
            Reveal(tx("lessonspart1a_na_analogia_quem_lexema_quem"), tx("lessonspart1a_lexema_produto_concreto_texto_token")),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.VISUAL, tx("lessonspart1a_fatiando_codigo_em_cores"),
        listOf(
            Say(LEXI, tx("lessonspart1a_olha_minha_katana_em_acao")),
            Visual(VK.TOKENIZE, input = "while (i <= n1) i++;", caption = tx("lessonspart1a_toque_em_cada_token_para")),
            Text(tx("lessonspart1a_repare_em_tres_coisas_1")),
            Predict(
                "Quantos tokens tem `x+=y;`?",
                listOf("3", "4", "5", "6"),
                1,
                tx("lessonspart1a_x_y_4_tokens_mesmo"),
            ),
            Visual(VK.TOKENIZE, input = "x+=y;", caption = tx("lessonspart1a_sem_espacos_lexico_ainda_sabe")),
            Say(PROF, tx("lessonspart1a_mas_como_lexi_sabe_que")),
            Visual(VK.DFA, input = "l(l|d)*", caption = tx("lessonspart1a_padrao_identificador_l_letra_d")),
            Say(BYTE, tx("lessonspart1a_dl_foi_rejeitada_faz_sentido")),
            Visual(VK.DFA, input = "d+(pd+)?", caption = tx("lessonspart1a_padrao_numero_d_digito_p")),
            Callout(WATCH_OUT, tx("lessonspart1a_em_dp_como_3_automato")),
            Callout(BRAIN_POWER, tx("solta_lessonspart1a_4")),
            Reveal(tx("comum_resposta"), tx("solta_lessonspart1a_5")),
            Visual(VK.TOKENIZE, input = "x = a+++b;", caption = tx("lessonspart1a_confira_mordida_gulosa")),
            Reveal(tx("lessonspart1a_por_que_token_id_carrega"), tx("lessonspart1a_sintatico_so_precisa_saber_que")),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.WORKED, tx("lessonspart1a_montando_tabela_tokens"),
        listOf(
            Say(PROF, tx("lessonspart1a_vamos_montar_tabela_lexema_token")),
            Code("if (x >= 10) total += x;", tx("comum_entrada_2")),
            Steps(
                tx("comum_resolvido"),
                listOf(
                    tx("lessonspart1a_if_palavra_chave_if_tambem"),
                    tx("lessonspart1a_delimitador"),
                    tx("lessonspart1a_x_id_atributo_ponteiro_para"),
                    tx("lessonspart1a_relop_atributo_ge_maior_casamento"),
                    tx("lessonspart1a_10_num_atributo_valor_10"),
                    tx("lessonspart1a_delimitador_2"),
                    tx("lessonspart1a_total_id_ponteiro_para_total"),
                    tx("lessonspart1a_operador_atribuicao_composta"),
                    tx("lessonspart1a_x_id_mesmo_ponteiro_primeiro"),
                    "`;` → delimitador **;**. Total: 10 tokens.",
                ),
            ),
            Visual(VK.TOKENIZE, input = "if (x >= 10) total += x;", caption = tx("lessonspart1a_confira_com_lexi")),
            Callout(WATCH_OUT, tx("lessonspart1a_dois_x_geram_dois_tokens")),
            Text("Sua vez (exemplo esmaecido): `while (i <= n1) i++;`"),
            Reveal(tx("lessonspart1a_quantos_tokens"), "9: while, (, i, <=, n1, ), i, ++, ;"),
            Reveal(tx("lessonspart1a_qual_token_while_n1"), tx("lessonspart1a_while_palavra_chave_reservada_n1")),
            Reveal(tx("lessonspart1a_sao_token_cada_ou_dois"), tx("lessonspart1a_cada_maior_casamento_relop_le")),
            Visual(VK.TOKENIZE, input = "while (i <= n1) i++;", caption = tx("comum_confira_suas_respostas")),
            Callout(EXAM_TIP, tx("lessonspart1a_questao_comum_quantos_tokens_ha")),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.SOCRATIC, tx("lessonspart1a_por_que_existe_analisador_lexico"),
        listOf(
            Say(PROF, tx("lessonspart1a_vou_te_fazer_perguntas_chute")),
            Predict(
                tx("lessonspart1a_analisador_lexico_descarta_espacos_comentari"),
                listOf(
                    tx("lessonspart1a_porque_eles_nao_mudam_significado"),
                    tx("lessonspart1a_porque_ocupam_memoria_demais"),
                    tx("lessonspart1a_porque_sintatico_tem_preguica"),
                    tx("lessonspart1a_porque_proibido_usar_espacos_em"),
                ),
                0,
                tx("lessonspart1a_espaco_so_separa_lexemas_depois"),
            ),
            Predict(
                tx("lessonspart1a_em_count2_count2_1_que"),
                listOf(tx("lessonspart1a_dois_tokens_id_count_num"), tx("lessonspart1a_unico_token_id"), tx("comum_erro_lexico"), tx("lessonspart1a_palavra_chave")),
                1,
                tx("lessonspart1a_padrao_id_letra_seguida_letras"),
            ),
            Predict(
                tx("lessonspart1a_deve_virar_quantos_tokens"),
                listOf("1", "2"),
                0,
                tx("lessonspart1a_so_regra_maior_casamento_maximal"),
            ),
            Say(BYTE, tx("lessonspart1a_mas_if_ele_casa_com")),
            Reveal(tx("lessonspart1a_como_desempatar_if_por_que"), tx("lessonspart1a_empate_mesmo_tamanho_vence_regra")),
            Predict(
                tx("lessonspart1a_em_fi_f_x_lexico"),
                listOf(tx("lessonspart1a_sim_erro_lexico"), tx("lessonspart1a_nao_fi_identificador_valido")),
                1,
                tx("lessonspart1a_lexico_nao_enxerga_contexto_fi"),
            ),
            Say(PROF, tx("lessonspart1a_ultima_por_que_nao_deixar")),
            Reveal(
                tx("lessonspart1a_tres_motivos_para_separar_analise"),
                tx("lessonspart1a_1_simplicidade_gramatica_nao_precisa"),
            ),
            Callout(FIRESIDE, tx("lessonspart1a_lexico_eu_trabalho_com_caracteres")),
            Visual(VK.TOKENIZE, input = "fi (a == f(x)) x = 1;", caption = tx("lessonspart1a_veja_fi_passa_como_identificador")),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.FORMAL, tx("lessonspart1a_token_padrao_lexema_definicoes"),
        listOf(
            Text(tx("lessonspart1a_token_par_nome_token_valor")),
            Text(tx("lessonspart1a_padrao_descricao_forma_que_lexemas")),
            Text(tx("lessonspart1a_lexema_sequencia_caracteres_programa_fonte")),
            Code(
                tx("lessonspart1a_token_padrao_informal_lexemas_if"),
                tx("lessonspart1a_exemplos_tokens_livro_dragao_fig"),
            ),
            Text(tx("lessonspart1a_exemplo_m_c_2_gera")),
            Code(
                tx("lessonspart1a_funcao_getnexttoken_pular_espacos_comentario"),
                tx("lessonspart1a_esqueleto_analisador_lexico"),
            ),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1a_p_quando_nenhum_padrao_casa")),
            Predict(
                "Em `x = 3.14;`, quantos lexemas existem?",
                listOf("4", "5", "6"),
                0,
                tx("lessonspart1a_x_3_14_numero_com"),
            ),
            Callout(EXAM_TIP, tx("lessonspart1a_nao_troque_definicoes_token_classe")),
            Reveal(tx("lessonspart1a_por_que_token_id_precisa"), tx("lessonspart1a_porque_ha_infinitos_lexemas_possiveis")),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.MNEMONIC, tx("lessonspart1a_gaveta_meia_etiqueta"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1a_token_gaveta_lexema_meia_que")),
            Say(LEXI, tx("lessonspart1a_outro_truque_lexema_letra_exata")),
            Callout(MNEMONIC, tx("lessonspart1a_abstrato_ao_concreto_t_ipo")),
            Text(tx("lessonspart1a_palacio_memoria_floresta_lexica_primeira")),
            Callout(MNEMONIC, tx("lessonspart1a_maior_casamento_guloso_lexi_sempre")),
            Callout(MNEMONIC, tx("lessonspart1a_reservada_vip_em_empate_tamanho")),
            Predict(
                tx("lessonspart1a_lendo_y_42_qual_lexema"),
                listOf("num", "42", "y", tx("lessonspart1a_num_42")),
                1,
                tx("lessonspart1a_lexema_texto_42_num_nome"),
            ),
            Callout(WATCH_OUT, tx("lessonspart1a_confusoes_classicas_1_achar_que")),
            Reveal(tx("lessonspart1a_complete_lexema_casa_com_padrao"), tx("lessonspart1a_exemplo_lexema_total_casa_com")),
            Visual(VK.TOKENIZE, input = "float media = total / n1;", caption = tx("lessonspart1a_gaveta_meia_etiqueta_identifique_cada")),
        ),
    ),
)

// ───────────────────────────── REGEX_DFA ─────────────────────────────

private val p1aRegex: List<Lesson> = listOf(
    Lesson(
        Skill.REGEX_DFA, Style.STORY, tx("lessonspart1a_trem_que_le_palavras"),
        listOf(
            Say(NARRADOR, tx("lessonspart1a_na_floresta_lexica_passa_trenzinho")),
            Say(PROF, tx("lessonspart1a_cada_estacao_estado_cada_trilho")),
            Say(BYTE, tx("lessonspart1a_como_trem_decide_se_palavra")),
            Say(PROF, tx("lessonspart1a_ele_parte_estacao_inicial_le")),
            Visual(VK.DFA, input = "(a|b)*abb", caption = tx("lessonspart1a_trem_livro_dragao_aceita_palavras")),
            Text(tx("lessonspart1a_esse_trem_afd_automato_finito")),
            Say(BYTE, tx("lessonspart1a_se_tivesse_dois_trilhos_com")),
            Say(PROF, tx("lessonspart1a_ai_afn_nao_deterministico_trem")),
            Callout(FIRESIDE, tx("lessonspart1a_afn_eu_sou_facil_construir")),
            Steps(
                tx("lessonspart1a_fabrica_trens"),
                listOf(
                    tx("lessonspart1a_expressao_regular_desenho_trajeto_como"),
                    tx("lessonspart1a_construcao_thompson_transforma_er_num"),
                    tx("lessonspart1a_construcao_subconjuntos_cada_estacao_afd"),
                    tx("lessonspart1a_minimizacao_junta_estacoes_que_se"),
                ),
            ),
            Predict(
                tx("lessonspart1a_afn_com_3_estados_vira"),
                listOf("3", "6", "8", "9"),
                2,
                tx("lessonspart1a_cada_estado_afd_subconjunto_dos"),
            ),
            Callout(REAL_WORLD, tx("lessonspart1a_lex_flex_fazem_exatamente_isso")),
            Reveal(tx("lessonspart1a_na_analogia_que_estacao_trilho"), tx("lessonspart1a_estado_transicao_estado_final_aceitacao")),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.VISUAL, tx("lessonspart1a_brincando_com_automatos"),
        listOf(
            Say(PROF, tx("lessonspart1a_cada_widget_afd_minimo_gerado")),
            Visual(VK.DFA, input = "ab*", caption = tx("lessonspart1a_ab_depois_quantos_b_quiser")),
            Text(tx("lessonspart1a_repare_fecho_kleene_virou_laco")),
            Visual(VK.DFA, input = "(a|b)*abb", caption = tx("lessonspart1a_termina_em_abb")),
            Predict(
                tx("lessonspart1a_em_b_abb_depois_ler"),
                listOf(tx("lessonspart1a_ja_aceitou"), tx("lessonspart1a_ja_viu_ab_falta_b"), tx("lessonspart1a_rejeitou_para_sempre"), tx("lessonspart1a_voltou_ao_inicio")),
                1,
                tx("lessonspart1a_cada_estado_afd_minimo_lembra"),
            ),
            Visual(VK.DFA, input = "(aa)*", caption = tx("lessonspart1a_quantidade_par_dois_estados_que")),
            Say(BYTE, tx("lessonspart1a_para_fazer_que_aceite_b")),
            Say(PROF, tx("lessonspart1a_nao_automato_finito_tem_memoria")),
            Visual(VK.DFA, input = "0|1(0|1)*", caption = tx("lessonspart1a_binarios_sem_zero_esquerda_teste")),
            Reveal(tx("lessonspart1a_por_que_011_rejeitada"), tx("lessonspart1a_depois_ler_0_automato_esta")),
            Callout(WATCH_OUT, tx("lessonspart1a_motor_desenha_afd_minimo_sem")),
            Visual(VK.DFA, input = "x(y|z)+", caption = tx("lessonspart1a_agora_teste_voce_quais_cadeias")),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.WORKED, tx("lessonspart1a_construcao_subconjuntos_na_mao"),
        listOf(
            Say(PROF, tx("lessonspart1a_vamos_converter_afn_em_afd")),
            Code(tx("lessonspart1a_estados_0_inicial_1_2"), tx("lessonspart1a_afn_em_0_lendo_trem")),
            Text(tx("lessonspart1a_regra_jogo_cada_estado_afd")),
            Steps(
                tx("comum_resolvido"),
                listOf(
                    tx("lessonspart1a_inicio_fecho_0_0"),
                    tx("solta_lessonspart1a_6"),
                    tx("lessonspart1a_com_b_move_0_b"),
                    "B com a: move({0, 1}, a) = {0, 1} → **B**.",
                    "B com b: move({0, 1}, b) = {0, 2} → novo estado **C**.",
                    "C com a: {0, 1} = **B**. C com b: {0} = **A**.",
                    tx("lessonspart1a_finais_conjuntos_que_contem_estado"),
                ),
            ),
            Code(tx("lessonspart1a_b_b_b_b_c"), tx("lessonspart1a_tabela_transicao_afd_inicial_final")),
            Visual(VK.DFA, input = "(a|b)*ab", caption = tx("lessonspart1a_motor_chega_ao_mesmo_afd")),
            Text(tx("lessonspart1a_agora_esmaecido_afn_para_cadeias")),
            Reveal("A = {0}. Calcule Dtran[A, a] e Dtran[A, b].", tx("solta_lessonspart1a_7")),
            Reveal("Agora B = {0, 1}: com a? com b?", tx("solta_lessonspart1a_8")),
            Reveal(tx("lessonspart1a_c_0_1_2_com"), tx("lessonspart1a_c_com_0_c_com")),
            Visual(VK.DFA, input = "(a|b)*bb", caption = tx("lessonspart1a_confira_motor_tambem_3_estados")),
            Callout(EXAM_TIP, tx("lessonspart1a_na_prova_organize_em_tabela")),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.SOCRATIC, tx("lessonspart1a_descobrindo_expressoes_regulares"),
        listOf(
            Say(PROF, tx("lessonspart1a_vou_te_mostrar_automatos_perguntar")),
            Predict(
                tx("lessonspart1a_quais_cadeias_b_b_aceita"),
                listOf(tx("lessonspart1a_que_comecam_com_terminam_com"), tx("lessonspart1a_so_ab"), tx("lessonspart1a_qualquer_cadeia_b"), tx("lessonspart1a_que_comecam_com_b")),
                0,
                tx("lessonspart1a_obrigatorio_qualquer_miolo_b_b"),
            ),
            Visual(VK.DFA, input = "a(a|b)*b", caption = tx("lessonspart1a_teste_ab_aab_abba_ba")),
            Predict(
                tx("lessonspart1a_x_y_z_aceita_cadeia"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                1,
                tx("lessonspart1a_significa_ou_mais_vezes_r"),
            ),
            Predict(
                tx("lessonspart1a_qual_er_descreve_binarios_impares"),
                listOf("(0|1)*1", "1(0|1)*", "(01)*", "0*1*"),
                0,
                tx("lessonspart1a_qualquer_coisa_desde_que_ultimo"),
            ),
            Visual(VK.DFA, input = "(0|1)*1"),
            Reveal(tx("lessonspart1a_escreva_r_usando_so_concatenacao"), tx("lessonspart1a_r_r_zero_ou_vez")),
            Predict(
                tx("lessonspart1a_para_escrever_er_para_parenteses"),
                listOf(tx("lessonspart1a_sim_com_bastante"), tx("lessonspart1a_nao_automato_finito_nao_consegue")),
                1,
                tx("lessonspart1a_para_saber_quantos_faltam_seria"),
            ),
            Say(PROF, tx("lessonspart1a_agora_pergunta_ouro_por_que")),
            Reveal(tx("lessonspart1a_por_que_er_afd_no"), tx("lessonspart1a_tokens_tem_estrutura_simples_regular")),
            Reveal(tx("lessonspart1a_se_afn_afd_tem_mesmo"), tx("lessonspart1a_porque_afn_muito_mais_facil")),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.FORMAL, tx("lessonspart1a_er_afn_afd_definicoes_algoritmos"),
        listOf(
            Text(tx("lessonspart1a_expressao_regular_sobre_alfabeto_cada")),
            Text(tx("lessonspart1a_precedencia_maior_concatenacao_menor_todas")),
            Text(tx("lessonspart1a_afn_move_s0_f_conjunto")),
            Code(
                tx("lessonspart1a_construcao_thompson_er_afn_i"),
                tx("lessonspart1a_algoritmo_3_23_livro_dragao"),
            ),
            Code(
                tx("lessonspart1a_construcao_subconjuntos_afn_afd_dstates"),
                tx("lessonspart1a_algoritmo_3_20_livro_dragao"),
            ),
            Text(tx("lessonspart1a_fecho_t_estados_alcancaveis_partir")),
            Predict(
                tx("lessonspart1a_qual_precedencia_correta_dos_operadores"),
                listOf(tx("lessonspart1a_concatenacao"), tx("comum_concatenacao"), tx("lessonspart1a_concatenacao_2")),
                1,
                tx("lessonspart1a_como_na_aritmetica_potencia_concatenacao"),
            ),
            Predict(
                tx("lessonspart1a_pela_precedencia_ab_significa"),
                listOf("(ab)*", "a(b*)"),
                1,
                tx("lessonspart1a_se_aplica_so_ao_simbolo"),
            ),
            Visual(VK.DFA, input = "ab*", caption = tx("lessonspart1a_confirme_abab_rejeitada")),
            Callout(EXAM_TIP, tx("lessonspart1a_cai_muito_1_desenhar_afn")),
            Callout(WATCH_OUT, tx("lessonspart1a_afn_n_estados_pode_virar")),
            Reveal(tx("lessonspart1a_defina_fecho_move_em_linha"), tx("lessonspart1a_fecho_t_estados_alcancaveis_partir_2")),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.MNEMONIC, tx("lessonspart1a_thompson_transforma_subconjuntos_simplificam"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1a_t_hompson_t_ransforma_er")),
            Callout(MNEMONIC, tx("lessonspart1a_n_afn_n_ao_sei")),
            Callout(MNEMONIC, tx("lessonspart1a_precedencia_igual_matematica_potencia_concat")),
            Callout(MNEMONIC, tx("lessonspart1a_zero_ou_mais_estrela_generosa")),
            Predict(
                tx("lessonspart1a_qual_er_aceita_cadeia_vazia"),
                listOf("a+", "ab*", "(ab)*", "x(y|z)+"),
                2,
                tx("lessonspart1a_so_estrela_no_lado_fora"),
            ),
            Visual(VK.DFA, input = "(ab)*", caption = tx("lessonspart1a_estado_inicial_ja_final_cadeia")),
            Text(tx("lessonspart1a_palacio_memoria_ainda_na_floresta")),
            Callout(WATCH_OUT, tx("lessonspart1a_confusoes_classicas_1_achar_que_2")),
            Callout(MNEMONIC, tx("lessonspart1a_regular_nao_conta_nao_aninha")),
            Reveal(tx("lessonspart1a_recite_esteira_com_nome_cada"), "ER →(Thompson)→ AFN →(subconjuntos)→ AFD →(minimização)→ AFD mínimo."),
        ),
    ),
)

internal val p1aLessons: List<Lesson> = p1aPhases + p1aTokens + p1aRegex
