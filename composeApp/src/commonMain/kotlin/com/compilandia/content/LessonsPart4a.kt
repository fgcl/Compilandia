package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 4a do conteúdo: NFA_TO_DFA (Floresta Léxica — Fábrica de Subconjuntos).
// Todos os conjuntos, estados e contagens abaixo foram conferidos com SmallNFA.subsetConstruction()
// e DFA.fromRegex() do motor.

// ───────────────────────────── NFA_TO_DFA ─────────────────────────────

internal val p4NfaLessons: List<Lesson> = listOf(
    Lesson(
        Skill.NFA_TO_DFA, Style.STORY, tx("lessonspart4a_turma_no_grupo_whatsapp"),
        listOf(
            Say(NARRADOR, tx("lessonspart4a_rodoviaria_lotada_vespera_feriado_voce")),
            Say(BYTE, tx("lessonspart4a_eu_ia_travar_escolho_torco")),
            Say(PROF, tx("lessonspart4a_afn_faz_diferente_ele_nao")),
            Say(PROF, tx("lessonspart4a_agora_truque_em_vez_acompanhar")),
            Steps(
                tx("lessonspart4a_rodoviaria_construcao_subconjuntos"),
                listOf(
                    tx("lessonspart4a_cada_plataforma_estado_afn"),
                    tx("lessonspart4a_corredor_livre_sem_ler_letra"),
                    tx("lessonspart4a_usar_passagem_com_letra_move"),
                    tx("lessonspart4a_lista_grupo_estamos_em_0"),
                    tx("lessonspart4a_se_ninguem_turma_tem_para"),
                    tx("lessonspart4a_se_pelo_menos_amigo_chegou"),
                ),
            ),
            Say(BYTE, tx("lessonspart4a_entao_afd_so_diario_grupo")),
            Say(PROF, tx("lessonspart4a_exatamente_como_diario_anotado_antes")),
            Predict(
                tx("lessonspart4a_no_afn_b_ab_turma"),
                listOf("{0}", "{1}", "{0, 1}", "∅"),
                2,
                tx("lessonspart4a_turma_se_divide_uns_ficam"),
            ),
            Visual(VK.NFA_SUBSET, input = "ab", caption = tx("lessonspart4a_avance_passo_passo_em_amarelo")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart4a_p_se_afn_ja_reconhece")),
            Callout(BRAIN_POWER, tx("lessonspart4a_com_3_plataformas_quantas_listas")),
            Reveal(tx("comum_resposta"), tx("lessonspart4a_23_8_listas_possiveis_incluindo")),
            Reveal(tx("lessonspart4a_na_analogia_quem_plataforma_corredor"), tx("lessonspart4a_estado_afn_transicao_estado_afd")),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.VISUAL, tx("lessonspart4a_assistindo_fabrica_subconjuntos"),
        listOf(
            Say(PROF, tx("lessonspart4a_hoje_voce_vai_ver_construcao")),
            Visual(VK.NFA_SUBSET, input = "abb", caption = tx("lessonspart4a_classico_b_abb_numa_versao")),
            Text(tx("lessonspart4a_repare_comecamos_em_0_lendo")),
            Predict(
                tx("lessonspart4a_por_que_todos_estados_afd"),
                listOf(tx("lessonspart4a_porque_0_inicial_nunca_sai"), tx("lessonspart4a_porque_0_tem_laco_com"), tx("lessonspart4a_coincidencia"), tx("lessonspart4a_porque_0_final")),
                1,
                tx("lessonspart4a_laco_b_em_0_significa"),
            ),
            Say(LEXI, tx("lessonspart4a_agora_com_trilhos_aqui_primeiro")),
            Visual(VK.NFA_SUBSET, input = "abc", caption = tx("lessonspart4a_ab_c_estado_0_saem")),
            Predict(
                tx("lessonspart4a_no_afn_ab_c_0"),
                listOf("{0}", "{1, 3}", "{0, 1, 3}", "{0, 1, 2, 3, 4}"),
                2,
                tx("lessonspart4a_fecho_inclui_proprio_0_todo"),
            ),
            Callout(WATCH_OUT, tx("lessonspart4a_quando_move_vazio_por_exemplo")),
            Visual(VK.NFA_SUBSET, input = "ou", caption = tx("lessonspart4a_ab_c_dois_caminhos_que")),
            Text(tx("lessonspart4a_repare_no_passo_com_move")),
            Reveal(tx("lessonspart4a_quantos_estados_tem_afd_ab"), tx("lessonspart4a_4_estados_0_b_1")),
            Say(BYTE, tx("lessonspart4a_quando_numero_estados_explode_mesmo")),
            Visual(VK.DFA, input = "(a|b)*a(a|b)(a|b)", caption = tx("lessonspart4a_antepenultimo_simbolo_afn_tem_4")),
            Callout(REAL_WORLD, tx("lessonspart4a_esse_tipo_linguagem_pior_caso")),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.WORKED, tx("lessonspart4a_subconjuntos_com_comeco_ao_fim"),
        listOf(
            Say(PROF, tx("lessonspart4a_vamos_resolver_com_calma_afn")),
            Code(tx("lessonspart4a_afn_ab_c_inicial_0"), tx("lessonspart4a_dois_caminhos_ab_ou_que")),
            Text(tx("lessonspart4a_receita_tem_passo_so_repetido")),
            Steps(
                tx("comum_resolvido"),
                listOf(
                    tx("lessonspart4a_inicio_fecho_0_0_0"),
                    tx("lessonspart4a_com_move_1_3_fecho"),
                    tx("lessonspart4a_com_b_com_c_move"),
                    tx("lessonspart4a_b_com_b_move_2"),
                    "B com c: move = {5} → **D = {5}**. B com a: ∅.",
                    "C com c: move = {5} = D. C com a e com b: ∅.",
                    tx("lessonspart4a_d_com_qualquer_letra_nenhum"),
                    tx("lessonspart4a_finais_quem_contem_5_so"),
                ),
            ),
            Code("        a    b    c\n→A {0}       B    ∅    ∅\n B {1,3,4}   ∅    C    D\n C {2,4}     ∅    ∅    D\n*D {5}       ∅    ∅    ∅", tx("lessonspart4a_tabela_dtran_inicial_final_estado")),
            Visual(VK.NFA_SUBSET, input = "ou", caption = tx("lessonspart4a_confira_cada_linha_tabela_com")),
            Callout(WATCH_OUT, tx("lessonspart4a_erro_numero_1_esquecer_fecho")),
            Text(tx("lessonspart4a_agora_com_voce_exemplo_esmaecido")),
            Reveal(tx("lessonspart4a_qual_estado_inicial_afd"), tx("lessonspart4a_fecho_0_0_1_3")),
            Reveal(tx("lessonspart4a_calcule_dtran_dtran_b_dtran"), "A com a: move = {2}, fecho = {2} = B. A com b: ∅ (morto). A com c: move = {4} = C."),
            Reveal(tx("lessonspart4a_continue_com_b_2_c"), tx("lessonspart4a_b_com_b_2_b")),
            Visual(VK.NFA_SUBSET, input = "abc", caption = tx("comum_confira_suas_respostas")),
            Callout(EXAM_TIP, tx("lessonspart4a_monte_sempre_tabela_com_colunas")),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.SOCRATIC, tx("lessonspart4a_por_que_conjuntos"),
        listOf(
            Say(PROF, tx("lessonspart4a_hoje_eu_so_pergunto_voce")),
            Predict(
                tx("lessonspart4a_afn_esta_no_estado_0"),
                listOf(tx("lessonspart4a_em_0"), tx("lessonspart4a_em_1"), tx("lessonspart4a_nos_dois_ao_mesmo_tempo"), tx("lessonspart4a_trava_afn_nao_pode_ter")),
                2,
                tx("lessonspart4a_afn_aceita_se_algum_caminho"),
            ),
            Say(BYTE, tx("lessonspart4a_entao_se_eu_sempre_anotar")),
            Say(PROF, tx("lessonspart4a_isso_se_voce_nome_cada")),
            Predict(
                tx("lessonspart4a_depois_ler_letra_por_que"),
                listOf(tx("lessonspart4a_nao_precisa_so_no_inicio"), tx("lessonspart4a_porque_ao_chegar_num_estado"), tx("lessonspart4a_para_tirar_estados_repetidos"), tx("lessonspart4a_para_achar_finais")),
                1,
                tx("lessonspart4a_trilho_nao_consome_entrada_se"),
            ),
            Predict(
                tx("lessonspart4a_quando_move_t_conjunto_vazio"),
                listOf(tx("lessonspart4a_cadeia_aceita"), tx("lessonspart4a_voltamos_ao_estado_inicial"), tx("lessonspart4a_vamos_para_estado_morto_onde"), tx("lessonspart4a_erro_na_construcao")),
                2,
                tx("lessonspart4a_nenhum_caminho_sobreviveu_estado_afd"),
            ),
            Reveal(tx("lessonspart4a_conjunto_como_0_2_deve"), tx("lessonspart4a_0_2_final_basta_caminho")),
            Say(PROF, tx("lessonspart4a_agora_tamanho_olhe_este_afn")),
            Visual(VK.NFA_SUBSET, input = "pen", caption = tx("lessonspart4a_conte_quantos_estados_afd_ganha")),
            Predict(
                tx("lessonspart4a_com_3_estados_no_afn"),
                listOf(tx("lessonspart4a_motor_errou"), tx("lessonspart4a_todo_conjunto_alcancavel_contem_0"), tx("lessonspart4a_porque_afn_nao_tem"), tx("lessonspart4a_porque_alfabeto_tem_2_letras")),
                1,
                tx("lessonspart4a_0_esta_sempre_presente_so"),
            ),
            Reveal(tx("lessonspart4a_se_fosse_n_esimo_simbolo"), tx("lessonspart4a_2n_afd_precisa_lembrar_n")),
            Visual(VK.DFA, input = "(a|b)*a(a|b)(a|b)", caption = tx("lessonspart4a_n_3_afd_minimo_tem")),
            Reveal(tx("lessonspart4a_entao_por_que_analisadores_lexicos"), tx("lessonspart4a_porque_explosao_rara_com_padroes")),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.FORMAL, tx("lessonspart4a_construcao_subconjuntos_definicao_prova"),
        listOf(
            Text(tx("lessonspart4a_afn_n_s0_f_com")),
            Text(tx("lessonspart4a_fecho_estados_alcancaveis_partir_usando")),
            Code(
                tx("lessonspart4a_fecho_t_empilhe_todos_estados"),
                tx("lessonspart4a_calculo_fecho_livro_dragao_fig"),
            ),
            Code(
                tx("lessonspart4a_dstates_fecho_s0_nao_marcado"),
                tx("lessonspart4a_construcao_subconjuntos_algoritmo_3_20"),
            ),
            Text(tx("lessonspart4a_correcao_afd_d_simula_todos")),
            Code(
                "A = {0,1,2,4,7}\nB = {1,2,3,4,6,7,8}\nC = {1,2,4,5,6,7}\nD = {1,2,4,5,6,7,9}\nE = {1,2,4,5,6,7,10}\n\nDtran   a   b\n→A      B   C\n B      B   D\n C      B   C\n D      B   E\n*E      B   C",
                tx("lessonspart4a_exemplo_livro_afn_thompson_b"),
            ),
            Predict(
                tx("lessonspart4a_no_exemplo_livro_por_que"),
                listOf(tx("lessonspart4a_porque_foi_ultimo_criado"), tx("lessonspart4a_porque_unico_que_contem_estado"), tx("lessonspart4a_porque_tem_mais_estados"), tx("lessonspart4a_porque_dtran_b_c")),
                1,
                tx("lessonspart4a_regra_t_final_se_t"),
            ),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart4a_p_afd_livro_tem_5")),
            Visual(VK.DFA, input = "(a|b)*abb", caption = tx("lessonspart4a_motor_mostra_afd_ja_minimizado")),
            Text(tx("lessonspart4a_custo_afn_n_estados_pode")),
            Predict(
                tx("lessonspart4a_qual_afirmacao_verdadeira"),
                listOf(tx("lessonspart4a_afn_reconhece_mais_linguagens_que"), tx("lessonspart4a_todo_afn_tem_afd_equivalente"), tx("lessonspart4a_construcao_subconjuntos_sempre_produz_afd"), tx("lessonspart4a_estado_inicial_afd_sempre_s0")),
                1,
                tx("lessonspart4a_mesmo_poder_linguagens_regulares_custo"),
            ),
            Callout(EXAM_TIP, tx("lessonspart4a_questao_classica_converta_afn_thompson")),
            Reveal(tx("lessonspart4a_escreva_formula_central_algoritmo_regras"), tx("lessonspart4a_dtran_t_fecho_move_t")),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.MNEMONIC, tx("lessonspart4a_anda_fecha_porta"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart4a_anda_fecha_porta_primeiro_anda")),
            Callout(MNEMONIC, tx("lessonspart4a_cesto_frutas_cada_estado_afd")),
            Text(tx("lessonspart4a_palacio_memoria_na_floresta_lexica")),
            Callout(MNEMONIC, tx("lessonspart4a_2n_interruptores_cada_estado_afn")),
            Predict(
                tx("lessonspart4a_qual_ordem_correta_dentro_cada"),
                listOf(tx("lessonspart4a_fecho_depois_move"), tx("lessonspart4a_move_depois_fecho"), tx("lessonspart4a_so_move"), tx("lessonspart4a_so_fecho")),
                1,
                tx("lessonspart4a_anda_move_com_letra_fecha"),
            ),
            Callout(MNEMONIC, tx("lessonspart4a_inicial_tambem_fecha_porta_estado")),
            Visual(VK.NFA_SUBSET, input = "eps", caption = tx("lessonspart4a_b_inicial_ja_0_1")),
            Callout(WATCH_OUT, tx("lessonspart4a_contrastes_que_caem_na_prova")),
            Callout(MNEMONIC, tx("lessonspart4a_esteira_completa_t_hompson_t")),
            Reveal(tx("lessonspart4a_sem_olhar_3_regras_construcao"), tx("lessonspart4a_1_inicial_fecho_s0_2")),
            Reveal(tx("lessonspart4a_por_que_analisadores_lexicos_preferem"), tx("lessonspart4a_passo_por_caractere_sem_voltar")),
        ),
    ),
)
