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
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.FIRST_FOLLOW
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// ───────────────────────────── FIRST ─────────────────────────────

internal val p2FirstLessons: List<Lesson> = listOf(
    Lesson(
        Skill.FIRST, Style.STORY, tx("lessonspart2a_fila_bandejao_quem_pode_abrir"),
        listOf(
            Say(NARRADOR, tx("lessonspart2a_templo_top_down_primeira_sala")),
            Say(BYTE, tx("lessonspart2a_profa_por_que_tem_fila")),
            Say(PROF, tx("lessonspart2a_porque_first_exatamente_isso_quem")),
            Text(tx("lessonspart2a_so_tiquinho_mais_formal_first")),
            Code(P2_ABC, tx("lessonspart2a_fila_primeiro_turma_depois_turma")),
            Say(PROF, tx("lessonspart2a_olha_regra_b_c_quem")),
            Say(BYTE, tx("lessonspart2a_entao_first_facil")),
            Say(PROF, tx("lessonspart2a_calma_byte_repara_em_turma")),
            Predict(
                tx("lessonspart2a_b_podem_faltar_dois_tem"),
                listOf(tx("lessonspart2a_so"), tx("comum_b_3"), tx("lessonspart2a_b_c"), tx("lessonspart2a_b_c_2")), 2,
                tx("lessonspart2a_se_vem_se_falta_b")
            ),
            Callout(MNEMONIC, tx("lessonspart2a_turma_anulavel_turma_que_pode")),
            Visual(FIRST_FOLLOW, P2_ABC, caption = tx("lessonspart2a_veja_motor_descobrindo_first_b")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart2a_p_pra_que_eu_quero")),
            Reveal(tx("lessonspart2a_sem_olhar_quanto_valem_first"), tx("lessonspart2a_first_first_b_b_aparece")),
            Say(BYTE, tx("lessonspart2a_resumindo_first_quem_pode_abrir")),
            Say(PROF, tx("lessonspart2a_nunca_foi_se_voce_errou")),
        )
    ),
    Lesson(
        Skill.FIRST, Style.VISUAL, tx("lessonspart2a_first_ao_vivo_ponto_fixo"),
        listOf(
            Say(PROF, tx("lessonspart2a_hoje_voce_so_vai_olhar")),
            Code(P2_EXPR_LL, tx("lessonspart2a_gramatica_expressoes_sem_recursao_esquerda")),
            Say(BYTE, tx("lessonspart2a_por_onde_ele_comeca")),
            Text(tx("lessonspart2a_por_todas_regras_em_voltas")),
            Predict(
                tx("lessonspart2a_por_que_first_precisa_mais"),
                listOf(tx("lessonspart2a_porque_simbolo_inicial"), tx("lessonspart2a_porque_depende_first_t_que"), tx("lessonspart2a_porque_tem"), tx("lessonspart2a_porque_tem_mais_producoes")), 1,
                tx("lessonspart2a_terminais_id_nascem_em_f")
            ),
            Visual(FIRST_FOLLOW, P2_EXPR_LL, caption = tx("lessonspart2a_avance_passo_passo_olhando_so")),
            Steps(
                tx("lessonspart2a_que_olhar_no_widget"),
                listOf(
                    tx("lessonspart2a_primeira_volta_ganha_t_ganha"),
                    "Ainda na primeira volta: F ganha {(, id}.",
                    tx("lessonspart2a_depois_t_f_t_copia"),
                    tx("lessonspart2a_depois_t_copia_first_t"),
                    tx("lessonspart2a_volta_inteira_sem_novidade_ponto"),
                )
            ),
            Say(BYTE, tx("lessonspart2a_por_que_t_nao_ganhou")),
            Say(PROF, tx("lessonspart2a_porque_em_t_f_t")),
            Callout(WATCH_OUT, tx("lessonspart2a_copie_first_xi_sem_so")),
            Reveal(tx("lessonspart2a_first_first_t_first_f"), tx("lessonspart2a_id_toda_expressao_comeca_com")),
            Callout(BRAIN_POWER, tx("lessonspart2a_se_regra_fosse_f_id")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2a_first_f_first_t_first")),
            Say(PROF, tx("lessonspart2a_viu_nao_tem_magica_so")),
        )
    ),
    Lesson(
        Skill.FIRST, Style.WORKED, tx("lessonspart2a_exemplo_resolvido_first_das_declaracoes"),
        listOf(
            Say(PROF, tx("lessonspart2a_vamos_resolver_juntos_devagar_exercicio")),
            Code(P2_DECL, tx("lessonspart2a_declaracoes_estilo_c_int_b")),
            Steps(
                tx("lessonspart2a_first_passo_passo"),
                listOf(
                    tx("lessonspart2a_1_terminais_sao_first_si"),
                    tx("lessonspart2a_2_t_int_float_duas"),
                    tx("lessonspart2a_3_l_id_r_comeca"),
                    tx("lessonspart2a_4_r_id_r_primeira"),
                    tx("lessonspart2a_5_d_t_l_comeca"),
                    tx("lessonspart2a_6_mais_volta_conferindo_tudo"),
                )
            ),
            Callout(WATCH_OUT, tx("lessonspart2a_no_passo_5_nao_continue")),
            Visual(FIRST_FOLLOW, P2_DECL, caption = tx("lessonspart2a_confira_com_motor_mesmos_conjuntos")),
            Say(BYTE, tx("lessonspart2a_agora_me_deixa_tentar_sozinho")),
            Say(PROF, tx("lessonspart2a_exemplo_desbotado_eu_comeco_voce")),
            Code(P2_EXPR_LL),
            Text(tx("lessonspart2a_presente_first_first_t")),
            Reveal(tx("lessonspart2a_sua_vez_first_f"), tx("lessonspart2a_id_duas_producoes_comecam_com")),
            Reveal(tx("lessonspart2a_first_t_sabendo_que_t"), tx("lessonspart2a_id_f_nao_anulavel_entao")),
            Reveal(tx("lessonspart2a_por_ultimo_first_para_t"), tx("lessonspart2a_id_mesmo_raciocinio_t_nao")),
            Say(PROF, tx("lessonspart2a_acertou_duas_tres_voce_ja")),
        )
    ),
    Lesson(
        Skill.FIRST, Style.SOCRATIC, tx("lessonspart2a_descubra_regra_first_sozinho"),
        listOf(
            Say(PROF, tx("lessonspart2a_hoje_eu_nao_vou_te")),
            Predict(
                "A → b C. Qual é FIRST(A)?",
                listOf("{b}", "{b, C}", "FIRST(C)", "{C}"), 0,
                tx("lessonspart2a_tudo_que_gera_comeca_com")
            ),
            Predict(
                tx("lessonspart2a_agora_b_c_com_b"),
                listOf("{c}", "{x}", "{x, c}", "{B}"), 1,
                tx("lessonspart2a_comeca_com_b_b_comeca")
            ),
            Say(BYTE, tx("lessonspart2a_entao_quando_comeca_com_variavel")),
            Predict(
                tx("lessonspart2a_se_b_x_em_b"),
                listOf("{x}", tx("lessonspart2a_x"), "{x, c}", tx("lessonspart2a_x_c")), 2,
                tx("lessonspart2a_se_b_sumir_cadeia_comeca")
            ),
            Predict(
                tx("lessonspart2a_se_for_b_c_com"),
                listOf("{x, y}", tx("comum_x_y"), "{x}", "{ε}"), 1,
                tx("lessonspart2a_x_b_aparece_y_b")
            ),
            Reveal(
                tx("lessonspart2a_qual_regra_voce_acabou_descobrir"),
                tx("lessonspart2a_para_x1_x2_xn_pegue")
            ),
            Say(PROF, tx("lessonspart2a_exatamente_algoritmo_livro_dragao_voce")),
            Predict(
                tx("lessonspart2a_ultima_b_c_recursiva_esquerda"),
                listOf(tx("lessonspart2a_loop_infinito_nao_pra_calcular"), "{c}", "{b, c}", "{A, c}"), 1,
                tx("lessonspart2a_primeira_regra_so_diz_first")
            ),
            Callout(BRAIN_POWER, tx("lessonspart2a_por_que_calculo_precisa_varias")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2a_na_1a_passada_ao_olhar")),
            Visual(FIRST_FOLLOW, P2_ABC, caption = tx("lessonspart2a_confirme_suas_descobertas_motor_aplica")),
        )
    ),
    Lesson(
        Skill.FIRST, Style.FORMAL, tx("lessonspart2a_first_definicao_algoritmo_versao_prova"),
        listOf(
            Say(PROF, tx("lessonspart2a_agora_terno_gravata_esta_versao")),
            Text(tx("lessonspart2a_definicao_para_cadeia_simbolos_gramaticais")),
            Steps(
                tx("lessonspart2a_regras_para_cada_simbolo_x"),
                listOf(
                    tx("lessonspart2a_1_se_x_terminal_first"),
                    tx("lessonspart2a_2_se_x_producao_acrescente"),
                    tx("lessonspart2a_3_se_x_y1_y2"),
                    tx("lessonspart2a_4_repita_ate_nenhum_conjunto"),
                )
            ),
            Code(
                tx("lessonspart2a_para_cada_nao_terminal_first"),
                tx("lessonspart2a_ponto_fixo_conjuntos_so_crescem")
            ),
            Text(tx("lessonspart2a_first_cadeia_x1_x2_xn")),
            Say(BYTE, tx("lessonspart2a_por_que_so_crescem_garante")),
            Say(PROF, tx("lessonspart2a_porque_ha_numero_finito_terminais")),
            Predict(
                tx("lessonspart2a_em_b_c_b_b"),
                listOf("{a}", "{a, b}", tx("lessonspart2a_b_2"), "{a, b, c}"), 2,
                tx("lessonspart2a_anulavel_olho_b_b_tambem")
            ),
            Visual(FIRST_FOLLOW, P2_ABC, caption = tx("lessonspart2a_confira_cada_regra_aplicada_no")),
            Callout(EXAM_TIP, tx("lessonspart2a_na_prova_1_liste_anulaveis")),
            Callout(WATCH_OUT, tx("lessonspart2a_first_contem_apenas_terminais_possivelmente")),
            Reveal(tx("lessonspart2a_defina_first_em_frase_sem"), tx("lessonspart2a_first_conjunto_terminais_que_podem")),
        )
    ),
    Lesson(
        Skill.FIRST, Style.MNEMONIC, tx("lessonspart2a_ganchos_para_nunca_esquecer_first"),
        listOf(
            Say(NARRADOR, tx("lessonspart2a_palacio_memoria_voce_entra_no")),
            Callout(MNEMONIC, tx("lessonspart2a_first_fila_quem_inicia_imagine")),
            Callout(MNEMONIC, tx("lessonspart2a_rima_terminal_na_frente_entra")),
            Say(BYTE, tx("lessonspart2a_quando_ele_entra")),
            Callout(MNEMONIC, tx("lessonspart2a_todos_faltaram_fila_vazia_unico")),
            Text(tx("lessonspart2a_contraste_que_salva_nota_first")),
            Steps(
                tx("lessonspart2a_confusoes_classicas_lado_lado"),
                listOf(
                    tx("lessonspart2a_first_pode_ter_follow_nunca"),
                    tx("lessonspart2a_follow_simbolo_inicial_sempre_tem"),
                    tx("lessonspart2a_first_olha_producoes_follow_olha"),
                    tx("lessonspart2a_first_terminal_ele_mesmo_first"),
                )
            ),
            Predict(
                tx("lessonspart2a_qual_destes_conjuntos_impossivel_ser"),
                listOf(tx("lessonspart2a_a"), "{id, (}", "{+, \$}", "{ε}"), 2,
                tx("lessonspart2a_marcador_fim_entrada_mora_no")
            ),
            Visual(FIRST_FOLLOW, P2_PARENS, caption = tx("lessonspart2a_teste_gancho_em_quem_abre")),
            Reveal(tx("lessonspart2a_diga_rima_cabeca_aplique_em"), tx("lessonspart2a_terminal_na_frente_entra_pronto")),
            Say(PROF, tx("lessonspart2a_esqueceu_volte_esta_sala_palacio")),
        )
    ),
)

// ───────────────────────────── FOLLOW ─────────────────────────────

internal val p2FollowLessons: List<Lesson> = listOf(
    Lesson(
        Skill.FOLLOW, Style.STORY, tx("lessonspart2a_quem_vem_logo_depois_voce"),
        listOf(
            Say(NARRADOR, tx("lessonspart2a_segunda_sala_templo_top_down")),
            Say(PROF, tx("lessonspart2a_na_sala_first_perguntavamos_quem")),
            Text(tx("lessonspart2a_follow_conjunto_terminais_que_podem")),
            Code(P2_ABC, tx("lessonspart2a_novo_nossa_gramatica_fila")),
            Say(BYTE, tx("lessonspart2a_quem_vem_depois_b_facil")),
            Say(PROF, "Isso! FOLLOW(B) = {c}. E depois de A?"),
            Predict(
                tx("lessonspart2a_em_b_c_com_b"),
                listOf(tx("lessonspart2a_so_b"), tx("lessonspart2a_so_c"), tx("lessonspart2a_b_c_3"), tx("lessonspart2a_b_c_4")), 2,
                tx("lessonspart2a_se_b_aparece_depois_vem")
            ),
            Say(BYTE, tx("lessonspart2a_ninguem_aparece_depois_dele_em")),
            Say(PROF, tx("lessonspart2a_simbolo_inicial_frase_inteira_depois")),
            Callout(MNEMONIC, tx("lessonspart2a_regra_heranca_se_b_ultimo")),
            Visual(FIRST_FOLLOW, P2_ABC, caption = "Veja o motor: FOLLOW(S) = {\$}, FOLLOW(A) = {b, c}, FOLLOW(B) = {c}."),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart2a_p_pra_que_saber_quem")),
            Reveal(tx("lessonspart2a_em_que_pode_vir_depois"), tx("lessonspart2a_pelo_dentro_dos_parenteses_inicial")),
            Say(PROF, tx("lessonspart2a_first_quem_abre_follow_quem")),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.VISUAL, tx("lessonspart2a_follow_ao_vivo_nas_expressoes"),
        listOf(
            Say(PROF, tx("lessonspart2a_mesma_gramatica_expressoes_agora_com")),
            Code(P2_EXPR_LL),
            Text(tx("lessonspart2a_motor_comeca_colocando_em_follow")),
            Predict(
                tx("lessonspart2a_em_f_que_entra_em"),
                listOf("(", ")", "\$", "id"), 1,
                tx("lessonspart2a_logo_depois_vem_entao_follow")
            ),
            Visual(FIRST_FOLLOW, P2_EXPR_LL, caption = tx("lessonspart2a_va_ate_parte_follow_leia")),
            Steps(
                tx("lessonspart2a_que_observar"),
                listOf(
                    "E → T E': depois de T vem E', então FIRST(E') − {ε} = {+} entra em FOLLOW(T).",
                    tx("lessonspart2a_como_anulavel_follow_tambem_entra"),
                    tx("lessonspart2a_esta_no_fim_t_follow"),
                    "T → F T': FOLLOW(F) ganha FIRST(T') − {ε} = {*} e, como T' pode sumir, herda FOLLOW(T).",
                    tx("lessonspart2a_repete_voltas_ate_nada_mudar"),
                )
            ),
            Say(BYTE, tx("lessonspart2a_ta_mas_quanto_deu_no")),
            Reveal(tx("lessonspart2a_tente_antes_tocar_follow_t"), tx("lessonspart2a_vem_first_vem_herdados_follow")),
            Reveal(tx("lessonspart2a_follow_f"), tx("lessonspart2a_vem_first_t_resto_herdado")),
            Callout(BRAIN_POWER, tx("lessonspart2a_repare_follow_follow_follow_t")),
            Reveal(tx("comum_responda_poder_cerebro"), tx("lessonspart2a_so_aparece_no_fim_das")),
            Visual(FIRST_FOLLOW, P2_PARENS, caption = tx("lessonspart2a_bonus_veja_chegando_em_follow")),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.WORKED, tx("lessonspart2a_exemplo_resolvido_follow_passo_passo"),
        listOf(
            Say(PROF, tx("lessonspart2a_calcular_follow_sem_first_como")),
            Code(P2_DECL),
            Text(tx("lessonspart2a_exercicio_first_first_d_first")),
            Steps(
                tx("lessonspart2a_follow_passo_passo"),
                listOf(
                    tx("lessonspart2a_1_d_inicial_follow_d"),
                    "2) Em D → T L ;, depois de T vem L. FIRST(L) = {id} entra em FOLLOW(T). FOLLOW(T) = {id}.",
                    "3) Na mesma regra, depois de L vem ';'. FOLLOW(L) = {;}.",
                    tx("lessonspart2a_4_em_l_id_r"),
                    tx("lessonspart2a_5_em_r_id_r"),
                    tx("lessonspart2a_6_mais_volta_nada_muda"),
                )
            ),
            Say(BYTE, tx("lessonspart2a_pera_no_passo_2_por")),
            Say(PROF, tx("lessonspart2a_porque_l_nao_anulavel_ele")),
            Visual(FIRST_FOLLOW, P2_DECL, caption = "Confira: FOLLOW(D) = {\$}, FOLLOW(T) = {id}, FOLLOW(L) = FOLLOW(R) = {;}."),
            Say(PROF, tx("lessonspart2a_agora_desbotado_gramatica_b_c")),
            Text(tx("lessonspart2a_eu_faco_primeiro_inicial_entao")),
            Reveal(tx("lessonspart2a_sua_vez_follow_b"), "{c}. Em S → A B c, depois de B vem o terminal c."),
            Reveal(tx("lessonspart2a_follow_cuidado_com_b_anulavel"), tx("lessonspart2a_b_c_depois_vem_b")),
            Callout(WATCH_OUT, tx("lessonspart2a_colocou_em_follow_normal_acontece")),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.SOCRATIC, tx("lessonspart2a_perguntas_que_levam_ao_follow"),
        listOf(
            Say(PROF, tx("lessonspart2a_gramatica_pequenininha_b_ela_gera")),
            Visual(DERIVATION, P2_ANBN, tx("comum_b_b"), tx("lessonspart2a_olhe_formas_sentenciais_b_b")),
            Predict(
                tx("lessonspart2a_olhando_derivacao_que_terminal_aparece"),
                listOf("a", "b", "ε", "nenhum"), 1,
                tx("lessonspart2a_em_b_b_b_esta")
            ),
            Predict(
                tx("lessonspart2a_na_forma_sentencial_inicial_que"),
                listOf(tx("lessonspart2a_nada_entao_follow_fica_vazio"), tx("lessonspart2a_fim_entrada"), "ε", "a"), 1,
                tx("lessonspart2a_depois_frase_completa_vem_fim")
            ),
            Say(BYTE, tx("lessonspart2a_entao_follow_b_mas_por")),
            Say(PROF, tx("lessonspart2a_porque_derivacoes_sao_infinitas_regras")),
            Predict(
                tx("lessonspart2a_nova_gramatica_x_b_y"),
                listOf("y ∈ FOLLOW(B)", "x ∈ FOLLOW(B)", tx("lessonspart2a_follow_b_follow"), tx("comum_nada")), 0,
                tx("lessonspart2a_b_seguido_y_first_que")
            ),
            Predict(
                tx("lessonspart2a_se_for_x_b_com"),
                listOf(tx("lessonspart2a_follow_b_fica_vazio"), tx("lessonspart2a_follow_follow_b"), tx("lessonspart2a_follow_b_follow"), "x ∈ FOLLOW(B)"), 1,
                tx("lessonspart2a_tudo_que_pode_vir_depois")
            ),
            Predict(
                tx("lessonspart2a_se_for_x_b_c"),
                listOf(tx("lessonspart2a_so_first_c"), tx("lessonspart2a_so_follow"), tx("lessonspart2a_first_c_tambem_follow"), tx("lessonspart2a_first_c_com")), 2,
                tx("lessonspart2a_c_pode_aparecer_entra_first")
            ),
            Reveal(tx("lessonspart2a_junte_tudo_escreva_tres_regras"), tx("lessonspart2a_1_no_follow_inicial_2")),
            Visual(FIRST_FOLLOW, P2_ANBN, caption = "Confira: FOLLOW(S) = {b, \$}."),
            Say(PROF, tx("lessonspart2a_voce_acabou_reconstruir_algoritmo_livro")),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.FORMAL, tx("lessonspart2a_follow_definicao_algoritmo_versao_prova"),
        listOf(
            Say(PROF, tx("lessonspart2a_versao_prova_com_todas_letras")),
            Text(tx("lessonspart2a_definicao_para_nao_terminal_follow")),
            Steps(
                tx("lessonspart2a_regras_aplique_ate_nada_mudar"),
                listOf(
                    tx("lessonspart2a_1_coloque_em_follow_onde"),
                    tx("lessonspart2a_2_para_cada_producao_b"),
                    tx("lessonspart2a_3_para_cada_producao_b"),
                )
            ),
            Code(
                tx("lessonspart2a_para_cada_nao_terminal_follow"),
                tx("lessonspart2a_repare_first_por_isso_quando")
            ),
            Predict(
                tx("lessonspart2a_qual_afirmacao_sempre_verdadeira"),
                listOf(tx("lessonspart2a_pode_estar_em_follow"), tx("lessonspart2a_follow_simbolo_inicial"), tx("lessonspart2a_follow_first"), tx("lessonspart2a_follow_terminal_ele_mesmo")), 1,
                tx("lessonspart2a_regra_1_follow_nunca_contem")
            ),
            Visual(FIRST_FOLLOW, P2_DECL, caption = tx("lessonspart2a_cada_passo_widget_cita_regra")),
            Callout(EXAM_TIP, tx("lessonspart2a_monte_tabelinha_nao_terminal_first")),
            Callout(WATCH_OUT, tx("lessonspart2a_direcao_heranca_regra_3_follow")),
            Reveal(tx("lessonspart2a_para_b_b_b_b"), tx("lessonspart2a_follow_b_follow_b_b")),
            Say(BYTE, tx("lessonspart2a_entao_no_inicial_olho_direita")),
            Say(PROF, tx("lessonspart2a_essa_prova_inteira_numa_frase")),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.MNEMONIC, tx("lessonspart2a_dolar_vizinho_heranca"),
        listOf(
            Say(NARRADOR, tx("lessonspart2a_segunda_sala_templo_top_down_2")),
            Callout(MNEMONIC, tx("lessonspart2a_3_regras_follow_dolar_vizinho")),
            Callout(MNEMONIC, tx("lessonspart2a_rima_ficou_no_fim_ou")),
            Say(BYTE, tx("lessonspart2a_eu_sempre_confundo_direcao_heranca")),
            Say(PROF, tx("lessonspart2a_pensa_em_heranca_verdade_desce")),
            Steps(
                tx("lessonspart2a_first_follow_duelo"),
                listOf(
                    tx("lessonspart2a_first_quem_abre_fila_follow"),
                    tx("lessonspart2a_first_pode_ter_follow_jamais"),
                    tx("lessonspart2a_follow_pode_ter_first_jamais"),
                    tx("lessonspart2a_first_olho_regras_follow_olho"),
                    tx("lessonspart2a_follow_depende_first_first_nao"),
                )
            ),
            Predict(
                tx("lessonspart2a_byte_calculou_follow_x_onde"),
                listOf(tx("lessonspart2a_falta_b"), tx("lessonspart2a_nao_pode"), tx("lessonspart2a_nao_pode_2"), tx("lessonspart2a_esta_certo")), 2,
                tx("lessonspart2a_nunca_entra_em_follow_se")
            ),
            Visual(FIRST_FOLLOW, P2_ANBN, caption = tx("lessonspart2a_teste_gancho_em_b_dolar")),
            Callout(FIRESIDE, tx("lessonspart2a_first_follow_ao_pe_lareira")),
            Reveal(tx("lessonspart2a_diga_3_regras_pelo_gancho"), tx("lessonspart2a_dolar_follow_inicial_vizinho_em")),
        )
    ),
)
