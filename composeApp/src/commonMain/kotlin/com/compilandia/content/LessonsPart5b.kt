package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 5b do conteúdo: CODEGEN_MIPS — geração de código MIPS (Livro do Dragão, cap. 8; estilo do trabalho prático no MARS).
// Registradores MIPS em strings cruas são escritos com '§' e convertidos por p5Asm (ver LessonsPart5.kt).

/** x = a + b * 2 no estilo do gerador do jogo (Mips.fromTac), embrulhado num programa completo para o MARS. */
private val P5_ASM_EXPR: String = p5Asm(
    tx("lessonspart5b_x_b_2_print_x"),
)

private val P5_ASM_IF_SHORT: String = p5Asm(
    tx("lessonspart5b_if_b_x_else_x"),
)

private val P5_ASM_IF_FULL: String = p5Asm(
    """
    # if (a > b) x = a - b; else x = b - a;   print("x = ", x);
            .data
    a:      .word 4
    b:      .word 2
    x:      .word 0
    msg:    .asciiz "x = "
            .text
    main:
            lw   §t1, a
            lw   §t2, b
            sgt  §t3, §t1, §t2     # cond = (a > b)
            beq  §t3, §zero, L0    # falso → else
            sub  §t3, §t1, §t2     # then: x = a - b
            sw   §t3, x
            j    L1
    L0:     sub  §t3, §t2, §t1     # else: x = b - a
            sw   §t3, x
    L1:     li   §v0, 4            # imprime o texto
            la   §a0, msg
            syscall
            li   §v0, 1            # imprime o inteiro x
            lw   §a0, x
            syscall
            li   §v0, 10           # encerra
            syscall
    """,
)

private val P5_ASM_WHILE_FADED: String = p5Asm(
    tx("lessonspart5b_0_i_1_while_i"),
)

internal val p5MipsLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.STORY, tx("lessonspart5b_bancada_pequena_despensa_enorme"),
        listOf(
            Say(NARRADOR, tx("lessonspart5b_ultima_estacao_forja_codigo_tac")),
            Say(BYTE, tx("lessonspart5b_mips_eu_fico_com_medo")),
            Say(PROF, tx("lessonspart5b_fica_tranquilo_cozinha_processador_cozinheir")),
            Text(tx("solta_lessonspart5b_1")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("solta_lessonspart5b_2"),
                    tx("lessonspart5b_prateleiras_despensa_memoria_declarada_na"),
                    tx("solta_lessonspart5b_3"),
                    tx("lessonspart5b_use_2_ovos_nao_precisa"),
                    tx("lessonspart5b_misturar_dois_potinhos_num_terceiro"),
                    tx("lessonspart5b_caderno_passos_secao_text_comecando"),
                    tx("lessonspart5b_post_it_se_faltou_ovo"),
                ),
            ),
            Code(
                p5Asm(
                    """
                    # x = a + b * 2     (TAC: t1 = b * 2 ; t2 = a + t1 ; x = t2)
                    lw   §t1, b         # busca b na despensa
                    li   §t2, 2         # pega a constante 2
                    mul  §t3, §t1, §t2  # mistura
                    sw   §t3, t1        # guarda na prateleira t1
                    lw   §t1, a
                    lw   §t2, t1
                    add  §t3, §t1, §t2
                    sw   §t3, t2
                    lw   §t0, t2
                    sw   §t0, x
                    """,
                ),
                tx("lessonspart5b_receita_traduzida_ficha_tac_por"),
            ),
            Predict(
                tx("lessonspart5b_cada_instrucao_tac_tipo_t"),
                listOf("1", "2", "4", "10"),
                2,
                tx("lessonspart5b_busca_lw_ou_li_busca"),
            ),
            Visual(VK.MIPS, input = tx("lessonspart5b_b_2"), caption = tx("lessonspart5b_rode_mars_bolso_4_b")),
            Callout(WATCH_OUT, tx("solta_lessonspart5b_4")),
            Say(BYTE, tx("lessonspart5b_mas_ir_despensa_toda_hora")),
            Say(PROF, tx("lessonspart5b_muito_cozinheiro_esperto_deixa_na")),
            Reveal(tx("lessonspart5b_na_analogia_que_syscall"), tx("solta_lessonspart5b_5")),
            Callout(REAL_WORLD, tx("lessonspart5b_mips_arquitetura_didatica_livro_patterson")),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.VISUAL, tx("lessonspart5b_arvore_ao_mars_instrucao_por"),
        listOf(
            Say(PROF, tx("lessonspart5b_vamos_ver_mesma_expressao_em")),
            Visual(VK.ATTR_TREE, input = tx("comum_b_c_2"), caption = tx("lessonspart5b_arvore_embaixo_na_raiz")),
            Visual(VK.TAC, input = tx("comum_b_c_2"), caption = tx("lessonspart5b_pos_ordem_t1_b_t2")),
            Visual(VK.MIPS, input = tx("comum_b_c_2"), caption = tx("solta_lessonspart5b_6")),
            Steps(
                tx("lessonspart5b_lendo_que_aconteceu_4_b"),
                listOf(
                    "lw \$t1, a → \$t1 = 4;  lw \$t2, b → \$t2 = 2",
                    "add \$t3, \$t1, \$t2 → \$t3 = 6;  sw \$t3, t1 → memória t1 = 6",
                    "lw \$t1, t1 → 6;  lw \$t2, c → 3;  mul → \$t3 = 18;  sw → memória t2 = 18",
                    "Cópia final: lw \$t0, t2;  sw \$t0, r → o resultado r = 18",
                ),
            ),
            Predict(
                tx("lessonspart5b_para_b_c_d_4"),
                listOf("23", "70", "34"),
                0,
                tx("lessonspart5b_t1_4_2_8_t2"),
            ),
            Visual(VK.MIPS, input = tx("comum_b_c_d"), caption = tx("lessonspart5b_confira_tres_blocos_operacao_copia")),
            Text(tx("lessonspart5b_agora_desvios_if_vira_comparacao")),
            Code(P5_ASM_IF_SHORT, tx("lessonspart5b_if_else_em_mips_bifurcacao")),
            Steps(
                tx("lessonspart5b_caminho_execucao_com_4_b"),
                listOf(
                    "sgt: 4 > 2 → \$t3 = 1",
                    "beq \$t3, \$zero, L0: 1 ≠ 0, então NÃO pula",
                    "sw \$t1, x → x = 4 (o then)",
                    tx("lessonspart5b_j_l1_salta_por_cima"),
                    tx("solta_lessonspart5b_7"),
                ),
            ),
            Predict(
                tx("lessonspart5b_se_voce_esquecer_j_l1"),
                listOf(tx("comum_nada_muda"), tx("lessonspart5b_executa_then_depois_cai_no"), tx("lessonspart5b_erro_montagem_no_mars")),
                1,
                tx("lessonspart5b_processador_nao_conhece_chaves_ele"),
            ),
            Visual(VK.MIPS, input = tx("lessonspart5b_b_2_2"), caption = tx("lessonspart5b_bonus_media_inteira_div_tres")),
            Callout(BRAIN_POWER, tx("solta_lessonspart5b_8")),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.WORKED, tx("lessonspart5b_programas_asm_completos_expressao_if"),
        listOf(
            Say(PROF, tx("lessonspart5b_exercicio_1_com_cara_trabalho")),
            Steps(
                tx("lessonspart5b_receita_traducao"),
                listOf(
                    "1. TAC: t1 = b * 2;  t2 = a + t1;  x = t2",
                    tx("lessonspart5b_2_data_word_para_cada"),
                    tx("lessonspart5b_3_text_cada_instrucao_tac"),
                    "4. print: `li \$v0, 1`, `lw \$a0, x`, `syscall`",
                    "5. fim: `li \$v0, 10`, `syscall`",
                ),
            ),
            Code(P5_ASM_EXPR, tx("lessonspart5b_expr_asm_rode_no_mars")),
            Visual(VK.MIPS, input = tx("lessonspart5b_b_2"), caption = tx("lessonspart5b_confira_no_simulador_no_fim")),
            Say(PROF, tx("lessonspart5b_exercicio_2_if_else_completo")),
            Code(P5_ASM_IF_FULL, tx("lessonspart5b_dist_asm_saida_no_mars")),
            Callout(WATCH_OUT, tx("lessonspart5b_cada_if_programa_precisa_rotulos")),
            Say(BYTE, "Deixa o while comigo! Eu consigo."),
            Code(P5_ASM_WHILE_FADED, tx("lessonspart5b_complete_lacunas_b_c")),
            Reveal(tx("lessonspart5b_qual_desvio"), tx("solta_lessonspart5b_9")),
            Reveal(tx("lessonspart5b_b_c"), tx("lessonspart5b_b_resultado_soma_volta_para")),
            Reveal(tx("lessonspart5b_quanto_vale_na_hora_print"), tx("lessonspart5b_1_2_3_6_teste")),
            Reveal(tx("lessonspart5b_agora_sem_ajuda_mips_estilo"), "lw \$t1, a / lw \$t2, b / add \$t3, \$t1, \$t2 / sw \$t3, t1 / lw \$t1, t1 / lw \$t2, c / mul \$t3, \$t1, \$t2 / sw \$t3, t2 / lw \$t0, t2 / sw \$t0, x. Com a=4, b=2, c=3: x = 18."),
            Visual(VK.MIPS, input = tx("comum_b_c_2"), caption = tx("lessonspart5b_confira_ultimo_exercicio")),
            Callout(EXAM_TIP, tx("lessonspart5b_para_testar_no_mars_assemble")),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.SOCRATIC, tx("lessonspart5b_por_que_tanto_lw_sw"),
        listOf(
            Say(PROF, tx("lessonspart5b_vou_so_fazer_perguntas_voce")),
            Reveal(tx("lessonspart5b_no_mips_add_so_aceita"), tx("lessonspart5b_primeiro_traze_las_para_registradores")),
            Reveal(tx("lessonspart5b_se_operando_constante_como_em"), tx("solta_lessonspart5b_10")),
            Reveal(tx("lessonspart5b_onde_variaveis_temporarios_moram_no"), tx("lessonspart5b_na_secao_data_word_para")),
            Say(BYTE, tx("lessonspart5b_mas_if_mips_nao_tem")),
            Reveal(tx("lessonspart5b_como_fazer_se_b_faca"), tx("solta_lessonspart5b_11")),
            Reveal(tx("lessonspart5b_while_que_desvio_faz_ele"), tx("lessonspart5b_j_para_tras_volta_ao")),
            Predict(
                tx("solta_lessonspart5b_12"),
                listOf(
                    tx("lessonspart5b_guarda_ultimo_resultado_que_deu"),
                    tx("solta_lessonspart5b_13"),
                    tx("lessonspart5b_conta_quantas_instrucoes_ja_rodaram"),
                ),
                1,
                tx("solta_lessonspart5b_14"),
            ),
            Reveal(tx("lessonspart5b_como_programa_imprime_algo_se"), tx("solta_lessonspart5b_15")),
            Say(BYTE, tx("lessonspart5b_pra_gastar_menos_lw_sw")),
            Reveal(tx("lessonspart5b_em_x_b_2_gerador"), tx("solta_lessonspart5b_16")),
            Visual(VK.MIPS, input = tx("lessonspart5b_b_2"), caption = tx("lessonspart5b_conte_lw_sw_gerador_ingenuo")),
            Callout(BRAIN_POWER, tx("lessonspart5b_por_que_nao_por_tudo")),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.FORMAL, tx("lessonspart5b_esquemas_traducao_tac_mips"),
        listOf(
            Text(tx("lessonspart5b_gerador_codigo_fase_final_que")),
            Code(
                p5Asm(
                    tx("lessonspart5b_zero_constante_0_v0_v1"),
                ),
                tx("lessonspart5b_registradores_mips_32_bits_usados"),
            ),
            Code(
                p5Asm(
                    tx("lessonspart5b_lw_rd_rot_rd_mem"),
                ),
                tx("lessonspart5b_subconjunto_mips_que_gerador_simples"),
            ),
            Code(
                p5Asm(
                    tx("lessonspart5b_gera_x_y_op_z"),
                ),
                tx("lessonspart5b_esquema_tac_mips_no_estilo"),
            ),
            Code(
                p5Asm(
                    tx("lessonspart5b_if_s1_else_s2_while"),
                ),
                tx("lessonspart5b_esquemas_para_comandos_rotulos_novos"),
            ),
            Predict(
                tx("lessonspart5b_pelo_esquema_tudo_na_memoria"),
                listOf("6", "8", "10"),
                2,
                tx("lessonspart5b_dois_operadores_4_instrucoes_lw"),
            ),
            Visual(VK.MIPS, input = tx("comum_b_c"), caption = tx("lessonspart5b_conte_instrucoes_10")),
            Text(tx("lessonspart5b_custo_num_processador_real_acessar")),
            Callout(EXAM_TIP, tx("solta_lessonspart5b_17")),
            Callout(WATCH_OUT, tx("solta_lessonspart5b_18")),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.MNEMONIC, tx("lessonspart5b_busca_busca_mistura_guarda"),
        listOf(
            Say(NARRADOR, tx("lessonspart5b_palacio_memoria_forja_codigo_ultima")),
            Callout(MNEMONIC, tx("lessonspart5b_busca_busca_mistura_guarda_toda")),
            Callout(MNEMONIC, tx("solta_lessonspart5b_19")),
            Callout(MNEMONIC, tx("solta_lessonspart5b_20")),
            Callout(MNEMONIC, tx("solta_lessonspart5b_21")),
            Callout(MNEMONIC, tx("solta_lessonspart5b_22")),
            Say(BYTE, tx("lessonspart5b_if_com_rotulos_eu_sempre")),
            Say(PROF, tx("lessonspart5b_pense_numa_estrada_com_bifurcacao")),
            Predict(
                tx("lessonspart5b_no_esqueleto_while_para_onde"),
                listOf(tx("lessonspart5b_para_rotulo_fim_laco"), tx("lessonspart5b_para_rotulo_teste_no_comeco"), tx("lessonspart5b_para_main")),
                1,
                tx("lessonspart5b_while_volta_testa_novo_unico"),
            ),
            Visual(VK.MIPS, input = tx("lessonspart5b_d_c"), caption = tx("lessonspart5b_bata_ritmo_busca_busca_mistura")),
            Reveal(tx("lessonspart5b_recite_duas_secoes_asm_que"), tx("solta_lessonspart5b_23")),
            Callout(FIRESIDE, tx("lessonspart5b_memoria_eu_guardo_tudo_sou")),
        ),
    ),
)
