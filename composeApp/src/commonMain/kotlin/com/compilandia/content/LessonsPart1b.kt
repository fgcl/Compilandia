package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.GrammarBank
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 1b do conteúdo: CFG_DERIVATION, AMBIGUITY (Vale das Gramáticas).

private val p1bExpr: String get() = GrammarBank.exprLeftRec.text
private val p1bAmbig: String get() = GrammarBank.exprAmbiguous.text
private val p1bAnbn: String get() = GrammarBank.anbn.text
private val p1bParens: String get() = GrammarBank.parens.text
private const val P1B_PORTUGUES = "Frase -> SN SV\nSN -> Art Sub\nSV -> Ver SN\nArt -> o | a\nSub -> gato | menina | peixe\nVer -> come | ama"
private const val P1B_ELSE = "S -> i S | i S e S | x"

// ───────────────────────────── CFG_DERIVATION ─────────────────────────────

private val p1bCfg: List<Lesson> = listOf(
    Lesson(
        Skill.CFG_DERIVATION, Style.STORY, tx("lessonspart1b_analise_sintatica_voce_ja_fez"),
        listOf(
            Say(PROF, tx("lessonspart1b_lembra_aula_portugues_sujeito_predicado")),
            Say(BYTE, tx("lessonspart1b_serio_eu_odiava_aquilo")),
            Say(PROF, tx("lessonspart1b_entao_vamos_odiar_juntos_mas")),
            Code(P1B_PORTUGUES, tx("lessonspart1b_minigramatica_portugues")),
            Text(tx("lessonspart1b_cada_linha_producao_frase_sintagma")),
            Visual(VK.DERIVATION, grammar = P1B_PORTUGUES, input = tx("lessonspart1b_gato_come_peixe"), caption = tx("lessonspart1b_veja_frase_nascer_trocando_sempre")),
            Say(PROF, tx("lessonspart1b_cada_passo_troca_nao_terminal")),
            Predict(
                tx("lessonspart1b_essa_gramatica_gera_menina_ama"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                0,
                tx("lessonspart1b_frase_sn_sv_art_sub"),
            ),
            Predict(
                tx("lessonspart1b_gato_come_peixe_2"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                1,
                tx("lessonspart1b_todas_palavras_existem_lexico_ok"),
            ),
            Text(tx("lessonspart1b_troque_palavras_por_tokens_igualzinho")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1b_p_por_que_livre_contexto")),
            Callout(FIRESIDE, tx("lessonspart1b_expressao_regular_eu_descrevo_palavras")),
            Reveal(tx("lessonspart1b_nessa_gramatica_quem_sao_terminais"), tx("lessonspart1b_nao_terminais_frase_sn_sv")),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.VISUAL, tx("lessonspart1b_derivacao_em_camera_lenta"),
        listOf(
            Say(PROF, tx("lessonspart1b_vamos_ver_gramatica_expressoes_livro")),
            Code(p1bExpr, tx("lessonspart1b_expressao_t_termo_f_fator")),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "id + id * id", caption = tx("lessonspart1b_avance_passo_passo_veja_arvore")),
            Text(tx("lessonspart1b_em_cada_linha_nao_terminal")),
            Predict(
                tx("lessonspart1b_na_derivacao_mais_esquerda_depois"),
                listOf("id + T * F", "id + F", "id + id"),
                0,
                tx("lessonspart1b_ainda_precisamos_gerar_id_id"),
            ),
            Say(BYTE, tx("lessonspart1b_arvore_mesma_coisa_que_derivacao")),
            Say(PROF, tx("lessonspart1b_arvore_resumo_derivacao_mostra_qual")),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = tx("lessonspart1b_leia_folhas_esquerda_para_direita")),
            Visual(VK.DERIVATION, grammar = p1bAnbn, input = tx("lessonspart1b_b_b_b"), caption = tx("solta_lessonspart1b_1")),
            Reveal(tx("lessonspart1b_quantos_passos_teve_derivacao_b"), tx("lessonspart1b_4_passos_tres_vezes_b")),
            Visual(VK.DERIVATION, grammar = p1bParens, input = "( ( ) ) ( )", caption = tx("lessonspart1b_parenteses_balanceados_repare_como_faz")),
            Reveal(tx("lessonspart1b_diferenca_entre_forma_sentencial_sentenca"), tx("lessonspart1b_forma_sentencial_qualquer_coisa_derivavel")),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.WORKED, tx("lessonspart1b_derivando_esquerda_direita"),
        listOf(
            Say(PROF, tx("lessonspart1b_exercicio_derivacao_mais_esquerda_mais")),
            Code(p1bExpr),
            Steps(
                tx("lessonspart1b_mais_esquerda_lm_troque_sempre"),
                listOf(tx("lessonspart1b_t"), "⇒ T + T", "⇒ F + T", "⇒ id + T", "⇒ id + T * F", "⇒ id + F * F", "⇒ id + id * F", tx("lessonspart1b_id_id_id_8_passos")),
            ),
            Text(tx("lessonspart1b_agora_mais_direita_rm_troque")),
            Code(tx("lessonspart1b_t_t_f_t_id")),
            Reveal(tx("lessonspart1b_proximos_dois_passos"), tx("lessonspart1b_f_id_id_id")),
            Reveal(tx("lessonspart1b_tres_passos_finais"), "⇒ T + id * id ⇒ F + id * id ⇒ id + id * id"),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1b_p_duas_derivacoes_dao_arvores")),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = tx("lessonspart1b_arvore_comum_duas_derivacoes")),
            Text(tx("lessonspart1b_mais_esmaecido_gramatica_b_cadeia")),
            Reveal(tx("lessonspart1b_derivacao_completa"), tx("lessonspart1b_b_b_b_b_b")),
            Visual(VK.DERIVATION, grammar = p1bAnbn, input = tx("comum_b_b"), caption = tx("comum_confira")),
            Callout(EXAM_TIP, tx("lessonspart1b_em_prova_escreva_passo_por")),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.SOCRATIC, tx("lessonspart1b_que_gramatica_gera"),
        listOf(
            Say(PROF, tx("lessonspart1b_pergunta_1_tente_sem_medo")),
            Code("S -> a S b | ε"),
            Predict(
                tx("lessonspart1b_essa_gramatica_gera_b"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                1,
                tx("lessonspart1b_toda_vez_que_vira_b"),
            ),
            Predict(
                tx("lessonspart1b_qual_menor_cadeia_que_ela"),
                listOf("ab", "a", tx("lessonspart1b_cadeia_vazia"), "b"),
                2,
                tx("solta_lessonspart1b_2"),
            ),
            Reveal(tx("lessonspart1b_descreva_linguagem_inteira_em_frase"), tx("lessonspart1b_anbn_n_0_n_seguidos")),
            Predict(
                tx("lessonspart1b_expressao_regular_consegue_descrever_anbn"),
                listOf(tx("lessonspart1b_sim_b"), tx("comum_nao_2")),
                1,
                tx("lessonspart1b_b_aceita_aab_com_contagens"),
            ),
            Say(BYTE, tx("lessonspart1b_entao_gramatica_tipo_er_turbinada")),
            Say(PROF, tx("lessonspart1b_pode_pensar_assim_toda_linguagem")),
            Code(p1bExpr),
            Predict(
                tx("lessonspart1b_quantos_nao_terminais_essa_gramatica"),
                listOf("3", "5", "8"),
                0,
                tx("lessonspart1b_t_f_que_aparecem_lado"),
            ),
            Predict(
                tx("lessonspart1b_quantos_terminais"),
                listOf("3", "5", "8"),
                1,
                tx("lessonspart1b_id"),
            ),
            Reveal(tx("lessonspart1b_por_que_numa_derivacao_precisamos"), tx("lessonspart1b_porque_forma_sentencial_pode_ter")),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "( id ) * id", caption = tx("lessonspart1b_derive_voce_mesmo_antes_avancar")),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.FORMAL, tx("lessonspart1b_glc_derivacoes_definicao_formal"),
        listOf(
            Text(tx("lessonspart1b_gramatica_livre_contexto_g_n")),
            Text(tx("lessonspart1b_derivacao_em_passo_se_p")),
            Text(tx("lessonspart1b_se_entao_forma_sentencial_g")),
            Text(tx("lessonspart1b_derivacao_mais_esquerda_lm_sempre")),
            Text(tx("lessonspart1b_arvore_derivacao_raiz_rotulada_nos")),
            Code(
                tx("lessonspart1b_convencoes_livro_dragao_terminais_b"),
            ),
            Predict(
                tx("lessonspart1b_se_id_t_entao_id"),
                listOf(tx("lessonspart1b_sentenca"), tx("lessonspart1b_forma_sentencial_nao_sentenca"), tx("lessonspart1b_producao")),
                1,
                tx("lessonspart1b_ainda_tem_nao_terminal_t"),
            ),
            Predict(
                tx("lessonspart1b_cada_arvore_derivacao_corresponde_quantas"),
                listOf(tx("comum_exatamente"), tx("lessonspart1b_varias"), tx("comum_nenhuma")),
                0,
                tx("lessonspart1b_arvore_fixa_producoes_regra_sempre"),
            ),
            Callout(EXAM_TIP, tx("lessonspart1b_questao_tradicional_dada_g_mostre")),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "id * ( id + id )", caption = tx("lessonspart1b_pratique_derivacao_lm_id_id")),
            Reveal(tx("lessonspart1b_escreva_g_n_t_p"), tx("lessonspart1b_n_t_b_p_b")),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.MNEMONIC, tx("lessonspart1b_quatro_pecas_gramatica"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1b_g_n_t_p_n")),
            Callout(MNEMONIC, tx("lessonspart1b_terminal_termina_folha_nao_se")),
            Callout(MNEMONIC, tx("lessonspart1b_derivacao_mais_esquerda_ler_livro")),
            Text(tx("lessonspart1b_palacio_memoria_vale_das_gramaticas")),
            Callout(MNEMONIC, tx("lessonspart1b_forma_sentencial_predio_em_obra")),
            Predict(
                tx("lessonspart1b_numa_arvore_derivacao_terminais_ficam"),
                listOf(tx("lessonspart1b_na_raiz"), tx("lessonspart1b_nos_nos_internos"), tx("lessonspart1b_nas_folhas")),
                2,
                tx("lessonspart1b_terminal_termina_sempre_folha_nos"),
            ),
            Callout(WATCH_OUT, tx("lessonspart1b_confusoes_classicas_1_achar_que")),
            Visual(VK.PARSE_TREE, grammar = p1bAnbn, input = tx("comum_b_b"), caption = tx("lessonspart1b_folhas_esquerda_para_direita_sentenca")),
            Reveal(tx("lessonspart1b_recite_que_significam_n_t"), tx("lessonspart1b_nao_terminais_terminais_producoes_simbolo")),
        ),
    ),
)

// ───────────────────────────── AMBIGUITY ─────────────────────────────

private val p1bAmbiguity: List<Lesson> = listOf(
    Lesson(
        Skill.AMBIGUITY, Style.STORY, tx("lessonspart1b_policial_binoculo"),
        listOf(
            Say(NARRADOR, tx("lessonspart1b_manchete_jornal_policial_ve_ladrao")),
            Say(BYTE, tx("lessonspart1b_perai_quem_estava_com_binoculo")),
            Say(PROF, tx("lessonspart1b_exatamente_frase_tem_duas_arvores")),
            Text(tx("lessonspart1b_em_portugues_gente_desempata_pelo")),
            Predict(
                tx("lessonspart1b_quanto_vale_2_3_4"),
                listOf("14", "20", tx("lessonspart1b_depende_arvore")),
                2,
                tx("lessonspart1b_na_matematica_escola_14_mas"),
            ),
            Code(p1bAmbig, tx("lessonspart1b_curta_bonita_mas_ambigua")),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id", caption = tx("lessonspart1b_duas_arvores_para_mesma_frase")),
            Say(PROF, tx("lessonspart1b_na_arvore_em_que_fica")),
            Callout(REAL_WORLD, tx("lessonspart1b_calculadora_simples_camelo_faz_2")),
            Say(BYTE, tx("lessonspart1b_como_resolve")),
            Say(PROF, tx("lessonspart1b_dois_jeitos_reescrever_gramatica_com")),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = tx("lessonspart1b_gramatica_t_f_unica_arvore")),
            Reveal(tx("lessonspart1b_defina_ambiguidade_com_suas_palavras"), tx("lessonspart1b_gramatica_ambigua_se_existe_pelo")),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.VISUAL, tx("lessonspart1b_cacando_arvores_gemeas"),
        listOf(
            Say(PROF, tx("lessonspart1b_widget_procura_todas_arvores_para")),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id"),
            Text(tx("lessonspart1b_olhe_raiz_cada_arvore_numa")),
            Predict(
                tx("lessonspart1b_id_id_id_nessa_mesma"),
                listOf(tx("lessonspart1b_arvore"), tx("comum_duas_arvores")),
                1,
                tx("lessonspart1b_id_id_id_ou_id"),
            ),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id + id", caption = tx("lessonspart1b_associatividade_quem_se_junta_primeiro")),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id + id", caption = tx("lessonspart1b_na_gramatica_t_f_recursao")),
            Say(BYTE, tx("lessonspart1b_famoso_else_pendurado")),
            Code(P1B_ELSE, tx("lessonspart1b_i_if_cond_then_else")),
            Visual(VK.PARSE_TREE, grammar = P1B_ELSE, input = tx("lessonspart1b_i_i_x_x"), caption = tx("lessonspart1b_else_pertence_ao_primeiro_if")),
            Callout(REAL_WORLD, tx("lessonspart1b_c_java_companhia_decidem_else")),
            Reveal(tx("lessonspart1b_por_que_ambiguidade_problema_para"), tx("lessonspart1b_porque_cada_arvore_pode_ter")),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.WORKED, tx("lessonspart1b_provando_que_gramatica_ambigua"),
        listOf(
            Say(PROF, tx("lessonspart1b_para_provar_ambiguidade_basta_exibir")),
            Code(p1bAmbig),
            Steps(tx("lessonspart1b_derivacao_mais_esquerda_no_1"), listOf(tx("lessonspart1b_e_e_e"), tx("lessonspart1b_id_2"), tx("lessonspart1b_id_3"), tx("lessonspart1b_id_id"), "⇒ id + id * id")),
            Steps(tx("lessonspart1b_derivacao_mais_esquerda_no_2"), listOf(tx("lessonspart1b_e_e_e_2"), tx("lessonspart1b_e_e_e_3"), tx("lessonspart1b_id_3"), tx("lessonspart1b_id_id"), "⇒ id + id * id")),
            Text(tx("lessonspart1b_duas_derivacoes_mais_esquerda_distintas")),
            Callout(WATCH_OUT, tx("lessonspart1b_derivacao_lm_outra_rm_diferentes")),
            Text(tx("lessonspart1b_sua_vez_esmaecido_mostre_que")),
            Reveal(tx("lessonspart1b_primeira_derivacao_lm_na_raiz"), tx("lessonspart1b_id_id_id_id_id")),
            Reveal(tx("lessonspart1b_segunda_derivacao_lm_na_raiz"), tx("lessonspart1b_id_id_id_id_id_2")),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id * id + id", caption = tx("lessonspart1b_confira_duas_arvores")),
            Reveal(tx("lessonspart1b_qual_das_duas_respeita_precedencia"), tx("lessonspart1b_com_na_raiz_id_id")),
            Callout(EXAM_TIP, tx("lessonspart1b_roteiro_resposta_1_escolha_sentenca")),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.SOCRATIC, tx("lessonspart1b_culpa_gramatica_ou_linguagem"),
        listOf(
            Predict(
                tx("lessonspart1b_se_unica_sentenca_tem_duas"),
                listOf(tx("lessonspart1b_sim_basta"), tx("lessonspart1b_nao_precisa_ser_maioria"), tx("lessonspart1b_so_se_todas_tiverem")),
                0,
                tx("lessonspart1b_basta_contraexemplo_unica_sentenca_com"),
            ),
            Predict(
                tx("lessonspart1b_id_gramatica_t_f_geram"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                0,
                tx("lessonspart1b_ambas_geram_todas_expressoes_com"),
            ),
            Say(PROF, tx("lessonspart1b_entao")),
            Reveal(tx("lessonspart1b_ambiguidade_propriedade_gramatica_ou_linguag"), tx("lessonspart1b_gramatica_em_geral_mesma_linguagem")),
            Predict(
                tx("lessonspart1b_na_gramatica_t_f_por"),
                listOf(tx("lessonspart1b_porque_so_aparece_em_t"), tx("lessonspart1b_por_sorte"), tx("lessonspart1b_porque_escrito_primeiro")),
                0,
                tx("lessonspart1b_para_chegar_voce_precisa_passar"),
            ),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id * id + id"),
            Predict(
                tx("lessonspart1b_por_que_id_id_id"),
                listOf(tx("lessonspart1b_porque_recursao_esta_esquerda_so"), tx("lessonspart1b_porque_analisador_le_esquerda")),
                0,
                tx("lessonspart1b_operando_direita_t_que_nao"),
            ),
            Say(BYTE, tx("lessonspart1b_existe_algum_programa_que_diz")),
            Predict(
                tx("lessonspart1b_existe_algoritmo_que_decide_para"),
                listOf(tx("comum_sim"), tx("lessonspart1b_nao_problema_indecidivel")),
                1,
                tx("lessonspart1b_indecidivel_por_isso_usamos_condicoes"),
            ),
            Reveal(tx("lessonspart1b_duas_formas_lidar_com_gramatica"), tx("lessonspart1b_1_reescrever_gramatica_niveis_precedencia")),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.FORMAL, tx("lessonspart1b_ambiguidade_definicao_desambiguacao"),
        listOf(
            Text(tx("lessonspart1b_definicao_gramatica_g_ambigua_se")),
            Text(tx("lessonspart1b_linguagem_inerentemente_ambigua_se_toda")),
            Text(tx("lessonspart1b_fato_nao_existe_algoritmo_que")),
            Code(
                tx("lessonspart1b_desambiguacao_expressoes_nivel_por_precedenc"),
            ),
            Code(
                tx("lessonspart1b_stmt_matched_stmt_open_stmt"),
                tx("lessonspart1b_else_pendurado_sem_ambiguidade_livro"),
            ),
            Predict(
                tx("lessonspart1b_para_provar_que_g_ambigua"),
                listOf(tx("lessonspart1b_derivacao_lm_outra_rm_diferentes_2"), tx("lessonspart1b_duas_derivacoes_lm_diferentes_para"), tx("lessonspart1b_que_g_tem_recursao_esquerda")),
                1,
                tx("lessonspart1b_duas_lm_distintas_duas_arvores"),
            ),
            Predict(
                tx("lessonspart1b_toda_gramatica_ll_1_ou"),
                listOf(tx("comum_ambigua"), tx("lessonspart1b_nao_ambigua"), tx("lessonspart1b_depende_entrada")),
                1,
                tx("lessonspart1b_analisador_deterministico_constroi_arvore_po"),
            ),
            Callout(EXAM_TIP, tx("lessonspart1b_classico_mostre_que_gramatica_if")),
            Visual(VK.PARSE_TREE, grammar = P1B_ELSE, input = tx("lessonspart1b_i_i_x_x"), caption = tx("lessonspart1b_contraexemplo_classico_em_miniatura")),
            Reveal(tx("lessonspart1b_por_que_recursao_esquerda_associatividade"), tx("lessonspart1b_em_t_operando_que_pode")),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.MNEMONIC, tx("lessonspart1b_duas_arvores_problema"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1b_ambi_dois_ambidestro_usa_duas")),
            Callout(MNEMONIC, tx("lessonspart1b_prova_ambiguidade_duas_mesma_mao")),
            Callout(MNEMONIC, tx("lessonspart1b_precedencia_profundidade_quem_tem_mais")),
            Callout(MNEMONIC, tx("lessonspart1b_recursao_aponta_associatividade_t_recursao")),
            Text(tx("lessonspart1b_palacio_memoria_no_vale_das")),
            Callout(MNEMONIC, tx("lessonspart1b_else_pendurado_casa_com_vizinho")),
            Predict(
                tx("lessonspart1b_em_2_3_2_potencia"),
                listOf("(2 ^ 3) ^ 2 = 64", "2 ^ (3 ^ 2) = 512"),
                1,
                tx("lessonspart1b_associativa_direita_mais_direita_se"),
            ),
            Callout(WATCH_OUT, tx("lessonspart1b_confusoes_classicas_1_achar_que_2")),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id", caption = tx("lessonspart1b_gemeas_mesma_placa_estruturas_diferentes")),
            Reveal(tx("lessonspart1b_recite_ganchos_ambi_mesma_mao"), tx("lessonspart1b_ambigua_duas_arvores_prova_com")),
        ),
    ),
)

internal val p1bLessons: List<Lesson> = p1bCfg + p1bAmbiguity
