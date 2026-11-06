package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 6a do conteúdo: DFA_MINIMIZATION (Floresta Léxica — Tabela dos Pares).
// Todas as marcações, rodadas e classes abaixo foram conferidas com Minimizer.run() sobre Minimizer.bank:
//  0 = AFD do livro (fig. 3.36) para (a|b)*abb: q0 ≡ q2; 4 marcas na rodada 1, 3 na 2, 2 na 3; mínimo com 4 estados.
//  1 = número par de a's: q0 ≡ q2 e q1 ≡ q3 (só rodada 1); mínimo com 2.
//  2 = termina em b: q0 ≡ q2 e q1 ≡ q3 (só rodada 1); mínimo com 2.
//  3 = ab*: completa com ∅; q1 ≡ q2; {q0,∅} marcado na rodada 2; mínimo com 2.
//  5 = a*b*: completa com ∅; q0 ≡ q1; {q0,q2} e {q1,q2} marcados na rodada 2 por causa do ∅; mínimo com 2.

/** Tabela em escadinha do AFD do livro, com a rodada em que cada par foi marcado. */
private val P6_DRAGON_TABLE: String = tx("lessonspart6a_afd_livro_para_b_abb")

private val P6_DRAGON_ROUNDS: String = tx("lessonspart6a_rodada_1_final_nao_final")

private val P6_DRAGON_MIN: String = tx("lessonspart6a_afd_minimo_4_estados_b")

internal val p6MinLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.STORY, tx("lessonspart6a_atendentes_que_fazem_mesma_coisa"),
        listOf(
            Say(NARRADOR, tx("lessonspart6a_lanchonete_bairro_hora_almoco_dono")),
            Say(BYTE, tx("lessonspart6a_ue_demite_quem_trabalha_menos")),
            Say(PROF, tx("lessonspart6a_nao_bem_isso_ele_faz")),
            Steps(
                tx("solta_lessonspart6a_1"),
                listOf(
                    tx("lessonspart6a_cada_atendente_estado_afd"),
                    tx("lessonspart6a_cada_pedido_cliente_b_simbolo"),
                    tx("lessonspart6a_entregar_lanche_no_fim_fila"),
                    tx("lessonspart6a_dois_atendentes_que_sempre_terminam"),
                    tx("lessonspart6a_se_existe_sequencia_pedidos_em"),
                    tx("lessonspart6a_juntar_todos_equivalentes_afd_minimo"),
                ),
            ),
            Say(LEXI, tx("lessonspart6a_na_floresta_lexica_isso_importa")),
            Predict(
                tx("solta_lessonspart6a_2"),
                listOf(tx("lessonspart6a_sim_se_repassarem_pedidos_para"), tx("lessonspart6a_nao_com_fila_vazia_entrega"), tx("lessonspart6a_depende_tamanho_alfabeto")),
                1,
                tx("lessonspart6a_cadeia_vazia_ja_separa_por"),
            ),
            Say(PROF, tx("lessonspart6a_depois_vem_efeito_domino_se")),
            Visual(VK.MINIMIZE, input = "0", caption = tx("lessonspart6a_afd_livro_para_b_abb_2")),
            Reveal(tx("lessonspart6a_no_widget_quais_dois_estados"), tx("lessonspart6a_q0_q2_no_livro_c")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart6a_p_se_construcao_subconjuntos_ja")),
            Callout(BRAIN_POWER, tx("lessonspart6a_atendente_que_so_fica_bracos")),
            Reveal(tx("lessonspart6a_na_analogia_quem_atendente_pedido"), tx("lessonspart6a_estado_simbolo_entrada_terminar_num")),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.VISUAL, tx("lessonspart6a_vendo_tabela_se_preencher"),
        listOf(
            Say(PROF, tx("lessonspart6a_hoje_voce_so_assiste_preve")),
            Visual(VK.MINIMIZE, input = "1", caption = tx("lessonspart6a_numero_par_com_estados_sobrando")),
            Predict(
                tx("lessonspart6a_nesse_afd_q0_q2_sao"),
                listOf(tx("lessonspart6a_nenhum"), "{q0,q2} e {q1,q3}", tx("lessonspart6a_so_q0_q2"), "{q0,q1} e {q2,q3}"),
                1,
                tx("lessonspart6a_rodada_1_marca_so_final"),
            ),
            Visual(VK.MINIMIZE, input = "0", caption = tx("lessonspart6a_classico_livro_agora_ha_3")),
            Steps(
                tx("lessonspart6a_que_voce_acabou_ver_no"),
                listOf(
                    tx("lessonspart6a_rodada_1_4_pares_marcados"),
                    tx("lessonspart6a_rodada_2_q0_q3_q1"),
                    tx("lessonspart6a_rodada_3_q0_q1_q1"),
                    tx("lessonspart6a_rodada_4_nada_novo_fim"),
                ),
            ),
            Say(LEXI, tx("lessonspart6a_agora_afd_que_veio_construcao")),
            Visual(VK.MINIMIZE, input = "5", caption = tx("lessonspart6a_b_falta_transicao_q2_com")),
            Predict(
                tx("lessonspart6a_em_b_q0_q1_q2"),
                listOf(tx("lessonspart6a_nada_ignorado"), tx("lessonspart6a_com_q2_vai_para_q1"), tx("lessonspart6a_se_funde_com_q2")),
                1,
                tx("lessonspart6a_unico_nao_final_entao_rodada"),
            ),
            Reveal(tx("lessonspart6a_se_voce_esquecesse_nesse_exemplo"), tx("lessonspart6a_nada_seria_marcado_voce_fundiria")),
            Visual(VK.MINIMIZE, input = "3", caption = tx("lessonspart6a_ab_aqui_se_distingue_q0")),
            Callout(WATCH_OUT, tx("lessonspart6a_entra_na_tabela_mas_se")),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.WORKED, tx("lessonspart6a_tabela_prova_casa_por_casa"),
        listOf(
            Say(PROF, tx("lessonspart6a_esta_questao_prova_sobre_minimizacao")),
            Code(P6_DRAGON_TABLE, tx("lessonspart6a_transicoes_tabela_final_em_escadinha")),
            Text(tx("lessonspart6a_antes_tudo_dois_cuidados_afd")),
            Code(P6_DRAGON_ROUNDS, tx("lessonspart6a_rodada_rodada_par_destino_ja")),
            Steps(
                tx("lessonspart6a_detalhes_que_valem_ponto"),
                listOf(
                    tx("lessonspart6a_rodada_1_marque_toda_casa"),
                    tx("lessonspart6a_rodada_2_para_cada_casa"),
                    tx("lessonspart6a_se_destinos_sao_mesmo_estado"),
                    tx("lessonspart6a_na_rodada_2_q0_q1"),
                    tx("lessonspart6a_rodada_3_q2_q3_agora"),
                    "Rodada 4: nenhuma casa nova. Pare. A casa vazia {q0,q2} diz: q0 ≡ q2.",
                ),
            ),
            Code(P6_DRAGON_MIN, tx("lessonspart6a_funda_q0_q2_cada_classe")),
            Visual(VK.MINIMIZE, input = "0", caption = tx("lessonspart6a_confira_cada_casa_cada_rodada")),
            Text(tx("lessonspart6a_agora_com_voce_exemplo_esmaecido")),
            Reveal(tx("lessonspart6a_rodada_1_quais_casas_voce"), tx("lessonspart6a_final_nao_final_q0_q1")),
            Reveal("Rodada 2: teste {q0,q2} e {q1,q3}.", tx("lessonspart6a_q0_q2_com_q2_q0")),
            Reveal(tx("lessonspart6a_resultado_final"), tx("lessonspart6a_q0_q2_q1_q3_afd")),
            Visual(VK.MINIMIZE, input = "2", caption = tx("lessonspart6a_confira_sua_tabela")),
            Callout(EXAM_TIP, tx("lessonspart6a_escreva_rodada_dentro_cada_casa")),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.SOCRATIC, tx("lessonspart6a_como_provar_que_dois_estados"),
        listOf(
            Say(PROF, tx("lessonspart6a_hoje_voce_vai_inventar_metodo")),
            Predict(
                tx("lessonspart6a_qual_melhor_definicao_p_q"),
                listOf(tx("lessonspart6a_tem_mesmas_setas_saindo"), tx("lessonspart6a_para_toda_cadeia_w_lendo"), tx("lessonspart6a_sao_ambos_finais"), tx("lessonspart6a_estao_perto_outro_no_desenho")),
                1,
                tx("lessonspart6a_equivalencia_sobre_comportamento_futuro_nenh"),
            ),
            Say(BYTE, tx("lessonspart6a_mas_para_toda_cadeia_sao")),
            Say(PROF, tx("lessonspart6a_boa_vire_pergunta_avesso_em")),
            Predict(
                tx("lessonspart6a_qual_testemunha_mais_curta_possivel"),
                listOf(tx("lessonspart6a_separa_quem_tem_laco"), tx("lessonspart6a_cadeia_vazia_separa_todo_final"), tx("lessonspart6a_ab_separa_iniciais")),
                1,
                tx("lessonspart6a_lendo_nada_voce_ja_esta"),
            ),
            Predict(
                tx("lessonspart6a_suponha_que_r_ja_esta"),
                listOf("w", tx("lessonspart6a_b_seguido_w"), tx("lessonspart6a_w_seguido_b"), tx("lessonspart6a_nao_existe")),
                1,
                tx("lessonspart6a_p_ler_b_leva_r"),
            ),
            Reveal(tx("lessonspart6a_por_que_algoritmo_precisa_repetir"), tx("lessonspart6a_porque_marcar_par_novo_pode")),
            Reveal(tx("lessonspart6a_por_que_algoritmo_sempre_termina"), tx("lessonspart6a_ha_numero_finito_pares_par")),
            Predict(
                tx("lessonspart6a_par_sobreviveu_todas_rodadas_sem"),
                listOf(tx("lessonspart6a_talvez_sejam_diferentes_por_cadeia"), tx("lessonspart6a_sao_equivalentes_se_existisse_testemunha"), tx("lessonspart6a_precisamos_testar_mao")),
                1,
                tx("lessonspart6a_algoritmo_completo_testemunha_tamanho_n"),
            ),
            Say(BYTE, tx("lessonspart6a_se_transicao_esta_faltando_eu")),
            Reveal(tx("comum_resposta"), tx("lessonspart6a_complete_afd_com_estado_morto")),
            Visual(VK.MINIMIZE, input = "5", caption = tx("lessonspart6a_veja_testemunha_separando_q2_q1")),
            Callout(FIRESIDE, tx("lessonspart6a_estado_q0_eu_q2_fazemos")),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.FORMAL, tx("lessonspart6a_minimizacao_definicao_prova"),
        listOf(
            Text(tx("lessonspart6a_seja_d_s0_f_afd")),
            Text(tx("lessonspart6a_equivalencia_p_q_se_para")),
            Code(
                tx("lessonspart6a_minimizaportabela_d_remova_estados_inalcanca"),
                tx("lessonspart6a_algoritmo_preenchimento_tabela_pares_disting"),
            ),
            Text(tx("lessonspart6a_construcao_minimo_estados_classes_p")),
            Code(
                tx("lessonspart6a_minimizaporparticoes_d_moore_hopcroft_versao"),
                tx("lessonspart6a_alternativa_refinar_particoes_caminho_livro"),
            ),
            Steps(
                tx("lessonspart6a_particoes_no_afd_livro_b"),
                listOf(
                    tx("lessonspart6a_0_q0_q1_q2_q3"),
                    tx("lessonspart6a_com_b_q3_vai_para"),
                    tx("lessonspart6a_com_b_q1_vai_para"),
                    tx("lessonspart6a_q0_q2_tem_destinos_identicos"),
                ),
            ),
            Text(tx("lessonspart6a_teorema_myhill_nerode_para_cada")),
            Predict(
                tx("lessonspart6a_qual_afirmacao_falsa"),
                listOf(tx("lessonspart6a_afd_minimo_unico_menos_nomes"), tx("lessonspart6a_tabela_particoes_dao_mesmo_afd"), tx("lessonspart6a_par_que_ficou_sem_marca"), tx("lessonspart6a_estados_inalcancaveis_devem_ser_removidos")),
                2,
                tx("lessonspart6a_falso_no_afd_livro_q0"),
            ),
            Text(tx("lessonspart6a_custo_tabela_tem_n_n")),
            Callout(EXAM_TIP, tx("lessonspart6a_roteiro_prova_1_inalcancaveis_fora")),
            Reveal(tx("lessonspart6a_defina_estados_distinguiveis_diga_que"), tx("lessonspart6a_p_q_sao_distinguiveis_se")),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.MNEMONIC, tx("lessonspart6a_marca_propaga_junta"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart6a_mpj_marca_propaga_junta_marca")),
            Text(tx("lessonspart6a_palacio_memoria_na_floresta_lexica")),
            Callout(MNEMONIC, tx("lessonspart6a_escadinha_nao_quadrado_p_q")),
            Callout(MNEMONIC, tx("lessonspart6a_final_nao_final_sao_agua")),
            Callout(MNEMONIC, tx("lessonspart6a_domino_se_filhos_sao_diferentes")),
            Predict(
                tx("lessonspart6a_na_rodada_2_par_q0"),
                listOf(tx("lessonspart6a_marcar_porque_provavelmente_vai_ser"), tx("lessonspart6a_deixar_em_branco_voltar_na"), tx("lessonspart6a_fundir_q0_q1")),
                1,
                tx("lessonspart6a_domino_so_cai_empurrado_por"),
            ),
            Callout(MNEMONIC, tx("lessonspart6a_morto_tambem_vota_estado_nao")),
            Callout(WATCH_OUT, tx("lessonspart6a_tres_tropecos_classicos_1_esquecer")),
            Callout(MNEMONIC, tx("lessonspart6a_esteira_floresta_t_hompson_t")),
            Visual(VK.MINIMIZE, input = "0", caption = tx("lessonspart6a_recite_mpj_enquanto_widget_roda")),
            Reveal(tx("lessonspart6a_sem_olhar_3_passos_3"), tx("lessonspart6a_passos_marca_final_nao_final")),
            Reveal(tx("lessonspart6a_contraste_que_construcao_subconjuntos_faz"), tx("lessonspart6a_subconjuntos_tira_nao_determinismo_afn")),
        ),
    ),
)
