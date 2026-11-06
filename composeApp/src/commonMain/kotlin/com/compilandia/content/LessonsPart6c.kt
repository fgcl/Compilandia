package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 6c do conteúdo: REG_ALLOC (Forja do Código — Coloração de Registradores).
// Números conferidos com RegAlloc (liveness, interference, chromatic, color, mips, memoryAccesses) e Mips.fromTac:
//  "a * b + c * d": grafo t1—t2 (t3 isolado), χ = 2; lw+sw: 11 (ingênuo) → 5 (k = 2); k = 1 derrama t1 → 7.
//  "( a + b ) * ( c - d )": mesmo formato, χ = 2, 11 → 5.
//  "a + b + c + d": nenhuma aresta, χ = 1, 11 → 5.
//  "a * b + ( c * d + a * c )": triângulo t1, t2, t3 (+ t1—t4), χ = 3; 17 (ingênuo) → 7 (k = 3); k = 2 derrama t1 → 9.
// Todos os programas completos (com main:) rodam no MipsVM (a=4, b=2, c=3, d=5).

/** a * b + c * d no estilo ingênuo (Mips.fromTac): todo temporário mora na memória. */
private val P6_RA_NAIVE: String = p6Asm(
    tx("lessonspart6c_r_b_c_d_tac"),
)

/** O mesmo cálculo com os temporários em registradores (RegAlloc.mips com k = 2). */
private val P6_RA_ALLOC: String = p6Asm(
    tx("lessonspart6c_r_b_c_d_com"),
)

/** a * b + ( c * d + a * c ) com só 2 registradores: t1 é derramado (spill). */
private val P6_RA_SPILL: String = p6Asm(
    tx("lessonspart6c_r_b_c_d_c"),
)

/** Convenção MIPS: o chamado salva e restaura os §s que usar. */
private val P6_RA_CALL: String = p6Asm(
    tx("lessonspart6c_convencao_s0_s7_sao_salvos"),
)

private val P6_RA_LIVE_TABLE: String = tx("lessonspart6c_r_b_c_d_c_2")

internal val p6RegLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.STORY, tx("lessonspart6c_poucas_bancadas_na_cozinha"),
        listOf(
            Say(NARRADOR, tx("lessonspart6c_cozinha_restaurante_em_dia_casamento")),
            Say(BYTE, tx("lessonspart6c_cozinha_aula_mips_bancada_registrador")),
            Say(PROF, tx("lessonspart6c_isso_chef_novo_faz_burrice")),
            Say(PROF, tx("lessonspart6c_chef_experiente_pensa_diferente_molho")),
            Steps(
                "Cozinha → alocação de registradores",
                listOf(
                    tx("lessonspart6c_cada_preparacao_intermediaria_molho_massa"),
                    tx("lessonspart6c_cada_bancada_registrador_so_ha"),
                    tx("lessonspart6c_momento_em_que_preparacao_fica"),
                    tx("lessonspart6c_duas_preparacoes_vivas_ao_mesmo"),
                    tx("lessonspart6c_dar_bancada_cada_preparacao_sem"),
                    tx("lessonspart6c_se_nao_houver_bancadas_suficientes"),
                ),
            ),
            Callout(REAL_WORLD, tx("lessonspart6c_mesmo_problema_pintar_mapa_brasil")),
            Predict(
                tx("lessonspart6c_em_r_b_c_d"),
                listOf(tx("lessonspart6c_t1_t2"), tx("lessonspart6c_t1_t3"), tx("lessonspart6c_t2_t3"), tx("lessonspart6c_todos_tres")),
                0,
                tx("lessonspart6c_quando_t2_fica_pronto_t1"),
            ),
            Visual(VK.REGALLOC, input = tx("comum_b_c_d"), caption = tx("lessonspart6c_veja_vivos_grafo_so_aresta")),
            Reveal(tx("lessonspart6c_quantas_viagens_despensa_lw_sw"), tx("lessonspart6c_ingenuo_11_7_lw_4")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart6c_p_por_que_nao_usar")),
            Reveal(tx("lessonspart6c_na_analogia_quem_bancada_preparacao"), tx("lessonspart6c_registrador_temporario_temporarios_que_inter")),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.VISUAL, tx("lessonspart6c_antes_depois_no_mars_bolso"),
        listOf(
            Say(PROF, tx("lessonspart6c_vamos_ver_quatro_passos_alocacao")),
            Visual(VK.REGALLOC, input = tx("comum_b_c_d"), caption = tx("lessonspart6c_siga_coluna_dos_vivos_baixo")),
            Text(tx("lessonspart6c_primeiro_codigo_ingenuo_jeito_que")),
            Visual(VK.MIPS_RUN, grammar = P6_RA_NAIVE, caption = tx("lessonspart6c_ingenuo_11_acessos_memoria_no")),
            Visual(VK.MIPS_RUN, grammar = P6_RA_ALLOC, caption = tx("lessonspart6c_alocado_5_acessos_mesmo_resultado")),
            Predict(
                "No código alocado, por que t2 e t3 podem ficar os dois em \$s0?",
                listOf(tx("lessonspart6c_sorte"), tx("lessonspart6c_porque_t2_usado_pela_ultima"), tx("lessonspart6c_porque_sao_mesmo_tipo"), "Porque \$s0 é especial"),
                1,
                tx("lessonspart6c_t2_morre_em_t3_t1"),
            ),
            Visual(VK.REGALLOC, input = tx("comum_b_c_d_2"), caption = tx("lessonspart6c_cadeia_cada_temporario_morre_quando")),
            Visual(VK.REGALLOC, input = tx("lessonspart6c_b_c_d_c"), caption = tx("lessonspart6c_agora_ha_3_temporarios_vivos")),
            Predict(
                tx("lessonspart6c_com_triangulo_t1_t2_t3"),
                listOf("1", "2", "3", "5"),
                2,
                tx("lessonspart6c_num_triangulo_cada_vertice_vizinho"),
            ),
            Visual(VK.MIPS_RUN, grammar = P6_RA_SPILL, caption = tx("lessonspart6c_se_so_houver_2_registradores")),
            Callout(BRAIN_POWER, tx("lessonspart6c_repare_que_derramado_escolhido_foi")),
            Reveal(tx("comum_resposta"), tx("lessonspart6c_tirar_t1_grafo_remove_3")),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.WORKED, tx("lessonspart6c_vivacidade_grafo_cores_mao"),
        listOf(
            Say(PROF, tx("lessonspart6c_exercicio_completo_r_b_c")),
            Code(P6_RA_LIVE_TABLE, tx("lessonspart6c_vivacidade_baixo_para_cima_grafo")),
            Steps(
                tx("lessonspart6c_como_tabela_foi_preenchida"),
                listOf(
                    tx("lessonspart6c_comece_no_fim_depois_ultima"),
                    "i=5 (r = t5): usa t5 → in = {t5}.",
                    tx("lessonspart6c_i_4_t5_t1_t4"),
                    tx("lessonspart6c_i_3_t4_t2_t3"),
                    tx("lessonspart6c_i_2_t3_c_out"),
                    tx("lessonspart6c_arestas_em_cada_instrucao_temporario"),
                ),
            ),
            Steps(
                tx("lessonspart6c_chaitin_com_k_3_simplificar"),
                listOf(
                    tx("lessonspart6c_graus_t1_3_t2_2"),
                    tx("lessonspart6c_tira_t2_grau_2_empilha"),
                    tx("lessonspart6c_pilha_topo_primeiro_t5_t4"),
                    tx("lessonspart6c_desempilha_menor_cor_livre_t5"),
                    "Resultado: \$s0 = {t3, t4, t5}, \$s1 = t1, \$s2 = t2. Nenhum spill.",
                ),
            ),
            Visual(VK.REGALLOC, input = tx("lessonspart6c_b_c_d_c"), caption = tx("lessonspart6c_confira_vivos_grafo_3_mips")),
            Callout(WATCH_OUT, tx("lessonspart6c_erro_classico_fazer_vivacidade_cima")),
            Text(tx("lessonspart6c_agora_com_voce_exemplo_esmaecido")),
            Reveal(tx("lessonspart6c_calcule_out_in_cada_instrucao"), "i=3 r = t3: out { }, in {t3}. i=2 t3 = t1 * t2: out {t3}, in {t1, t2}. i=1 t2 = c - d: out {t1, t2}, in {t1}. i=0 t1 = a + b: out {t1}, in { }."),
            Reveal(tx("lessonspart6c_quais_arestas_quantos_registradores_sao"), "Só t2—t1 (em i=1, t2 é definido com t1 vivo). t3 fica sozinho. χ = 2: por exemplo, t1 → \$s1, t2 e t3 → \$s0."),
            Reveal(tx("lessonspart6c_quantos_lw_sw_antes_depois"), tx("lessonspart6c_ingenuo_11_alocado_com_2")),
            Visual(VK.REGALLOC, input = tx("comum_b_c_d_2_2"), caption = tx("comum_confira_suas_respostas")),
            Callout(EXAM_TIP, tx("lessonspart6c_na_prova_monte_sempre_tabela")),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.SOCRATIC, tx("lessonspart6c_quando_dois_valores_podem_dividir"),
        listOf(
            Say(PROF, tx("lessonspart6c_hoje_voce_vai_reinventar_alocacao")),
            Predict(
                tx("lessonspart6c_por_que_compilador_se_esforca"),
                listOf(tx("lessonspart6c_porque_memoria_pode_apagar_dados"), tx("lessonspart6c_porque_acessar_memoria_lw_sw"), tx("lessonspart6c_porque_mips_proibe_lw"), tx("lessonspart6c_para_economizar_espaco_na_data")),
                1,
                tx("lessonspart6c_operacao_entre_registradores_leva_ciclo"),
            ),
            Say(BYTE, tx("lessonspart6c_mas_registrador_pouco_entao_dois")),
            Predict(
                tx("lessonspart6c_quando_dois_temporarios_x_y"),
                listOf(tx("lessonspart6c_quando_tem_nomes_parecidos"), tx("lessonspart6c_quando_nunca_estao_vivos_ao"), tx("lessonspart6c_quando_sao_usados_na_mesma"), tx("comum_nunca")),
                1,
                tx("lessonspart6c_se_y_so_passa_existir"),
            ),
            Predict(
                tx("lessonspart6c_que_quer_dizer_x_esta"),
                listOf(tx("lessonspart6c_x_ja_foi_definido_antes"), tx("lessonspart6c_existe_caminho_p_ate_uso"), tx("lessonspart6c_x_esta_num_registrador"), tx("lessonspart6c_x_vai_ser_impresso")),
                1,
                tx("lessonspart6c_vivo_valor_atual_ainda_vai"),
            ),
            Reveal(tx("lessonspart6c_dessa_definicao_sai_formula_in"), tx("lessonspart6c_antes_instrucao_esta_vivo_que")),
            Predict(
                tx("lessonspart6c_se_formula_calcula_in_partir"),
                listOf(tx("lessonspart6c_cima_para_baixo"), tx("lessonspart6c_baixo_para_cima"), tx("comum_tanto_faz")),
                1,
                tx("lessonspart6c_para_calcular_in_instrucao_i"),
            ),
            Say(PROF, tx("lessonspart6c_agora_monte_grafo_no_por")),
            Visual(VK.REGALLOC, input = tx("comum_b_c_d_2_2"), caption = tx("lessonspart6c_duas_cores_bastam_repare_qual")),
            Reveal(tx("lessonspart6c_se_grafo_precisar_mais_cores"), tx("lessonspart6c_algum_temporario_derramado_spill_mora")),
            Reveal(tx("lessonspart6c_por_que_t_existem_separados"), "Por causa das chamadas de função. Quem chama não sabe quais registradores a função vai usar. A convenção divide a responsabilidade: \$t0–\$t9 podem ser destruídos pela função (se o chamador precisa deles, ele salva); \$s0–\$s7 devem ser devolvidos intactos (se a função usa, ela salva e restaura)."),
            Callout(FIRESIDE, "t1: 'Eu cheguei primeiro nesse registrador!' t3: 'Mas você já morreu, ninguém vai te ler de novo.' t1: '...' Alocador: 'Próximo! \$s0 está livre.'"),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.FORMAL, tx("lessonspart6c_alocacao_por_coloracao_definicao_prova"),
        listOf(
            Text(tx("lessonspart6c_para_cada_instrucao_i_use")),
            Code(
                tx("lessonspart6c_vivacidade_analise_fluxo_dados_para"),
                tx("lessonspart6c_equacoes_vivacidade"),
            ),
            Text(tx("lessonspart6c_intervalo_vida_x_pontos_programa")),
            Text(tx("lessonspart6c_alocacao_com_k_registradores_k")),
            Code(
                tx("lessonspart6c_chaitin_g_k_pilha_vazia"),
                tx("lessonspart6c_simplificar_empilhar_colorir"),
            ),
            Text(tx("lessonspart6c_por_que_funciona_se_grau")),
            Predict(
                tx("lessonspart6c_grafo_tem_no_n_grau"),
                listOf(tx("lessonspart6c_n_certamente_sera_derramado"), tx("lessonspart6c_n_nao_pode_ser_simplificado"), tx("lessonspart6c_grafo_nao_4_colorivel")),
                1,
                tx("lessonspart6c_grau_k_so_torna_n"),
            ),
            Visual(VK.MIPS_RUN, grammar = P6_RA_CALL, caption = "Convenção do MIPS: \$s0 é salvo pelo CHAMADO. dobro usa \$s0, mas salva e restaura; main encontra o 7 intacto e imprime 17."),
            Text("**Convenção MIPS**: \$t0–\$t9 são **salvos pelo chamador** (caller-saved): uma função pode sobrescrevê-los à vontade. \$s0–\$s7 são **salvos pelo chamado** (callee-saved): quem os usa salva na pilha e restaura antes do `jr \$ra`. Regra prática: valor que atravessa uma chamada → \$s; valor de vida curta → \$t."),
            Callout(EXAM_TIP, tx("lessonspart6c_roteiro_prova_1_tac_2")),
            Reveal(tx("lessonspart6c_escreva_equacao_vivacidade_regra_interferenc"), tx("lessonspart6c_in_i_use_i_out")),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.MNEMONIC, tx("lessonspart6c_pinte_mapa_cozinha"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart6c_vigor_vi_vacidade_g_rafo")),
            Text(tx("lessonspart6c_palacio_memoria_na_forja_codigo")),
            Callout(MNEMONIC, tx("lessonspart6c_vivacidade_se_faz_como_desfazer")),
            Callout(MNEMONIC, tx("lessonspart6c_in_use_out_def_se")),
            Callout(MNEMONIC, tx("lessonspart6c_interferencia_nasceu_enquanto_outro_estava")),
            Predict(
                tx("lessonspart6c_na_fase_simplificacao_com_k"),
                listOf(tx("lessonspart6c_qualquer"), tx("lessonspart6c_com_grau_2_menor_que"), tx("lessonspart6c_so_maior_grau"), tx("lessonspart6c_com_grau_3")),
                1,
                tx("lessonspart6c_menos_vizinhos_que_latas_sempre"),
            ),
            Callout(MNEMONIC, tx("lessonspart6c_t_tchau_funcao_pode_estragar")),
            Callout(WATCH_OUT, tx("lessonspart6c_contrastes_vivo_ja_definido_vivo")),
            Visual(VK.REGALLOC, input = tx("lessonspart6c_b_c_d_c"), caption = "Recite VIGOR enquanto olha: vivos, grafo (triângulo!), 3 cores, MIPS com \$s0, \$s1, \$s2."),
            Reveal(tx("lessonspart6c_numeros_para_guardar_quantos_lw"), tx("lessonspart6c_11_no_ingenuo_5_com")),
            Reveal(tx("lessonspart6c_sem_olhar_4_passos_formula"), tx("lessonspart6c_vigor_vivacidade_tras_para_frente")),
        ),
    ),
)
