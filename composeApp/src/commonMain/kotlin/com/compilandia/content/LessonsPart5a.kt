package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.RuntimeSim
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 5a do conteúdo: RUNTIME — ambientes de execução e registros de ativação (Livro do Dragão, cap. 7).
// Registradores MIPS em strings cruas são escritos com '§' e convertidos por p5Asm (ver LessonsPart5.kt).

/** Mesmo texto que o widget CALL_STACK mostra (vem do motor, então nunca diverge). */
private val P5_FAT_CODE: String = RuntimeSim.fatorial(3).code
private val P5_CHAIN1_CODE: String = RuntimeSim.chain(1).code
private val P5_CHAIN2_CODE: String = RuntimeSim.chain(2).code

private val P5_FAT_ASM: String = p5Asm(
    tx("lessonspart5a_text_main_li_a0_3"),
)

/** As peças do fat.asm, uma por vez. O MipsVM confere o que cada uma faz em AulasConsertadasTest. */
internal const val P5_PECA_SP = "addi §sp, §sp, -8      # abre 8 bytes na pilha"
internal const val P5_PECA_DESLOCAMENTO = "sw   §ra, 4(§sp)       # grava no endereço §sp + 4\nsw   §a0, 0(§sp)       # grava no endereço §sp + 0"
internal const val P5_PECA_JAL = "jal  fat               # pula para fat e guarda a volta em §ra\nmove §a0, §v0          # a volta é aqui"
internal const val P5_PECA_RA = "jr   §ra               # pula para o endereço em §ra"

internal val p5RuntimeLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.STORY, tx("lessonspart5a_pilha_pratos_cozinha"),
        listOf(
            Say(NARRADOR, tx("lessonspart5a_forja_codigo_fim_expediente_codigo")),
            Say(BYTE, tx("lessonspart5a_na_memoria_ue_cada_variavel")),
            Say(PROF, tx("lessonspart5a_quase_pense_na_cozinha_restaurante")),
            Text(tx("lessonspart5a_bilhete_diz_quais_ingredientes_vieram")),
            Text(tx("lessonspart5a_se_no_meio_prato_ele")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart5a_cada_pedido_em_preparo_ativacao"),
                    tx("lessonspart5a_prato_com_bilhete_registro_ativacao"),
                    tx("lessonspart5a_ingredientes_pedido_parametros"),
                    tx("solta_lessonspart5a_1"),
                    tx("lessonspart5a_rascunhos_cozinheiro_variaveis_locais_tempor"),
                    tx("lessonspart5a_pilha_pratos_na_bancada_pilha"),
                    tx("lessonspart5a_molho_que_precisa_outro_molho"),
                ),
            ),
            Say(PILHA, tx("lessonspart5a_prato_novo_vai_em_cima")),
            Code(P5_FAT_CODE, tx("lessonspart5a_fatorial_recursivo_receita_que_pede")),
            Predict(
                tx("lessonspart5a_rodando_fat_3_quantos_pratos"),
                listOf("2", "3", "4", "6"),
                2,
                tx("lessonspart5a_main_fat_3_fat_2"),
            ),
            Visual(VK.CALL_STACK, input = "fat3", caption = tx("lessonspart5a_empilhe_desempilhe_cada_fat_n")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart5a_se_todos_quadros_sao_mesma")),
            Text(tx("lessonspart5a_resto_cozinha_cardapio_pregado_na")),
            Reveal(tx("lessonspart5a_por_que_deposito_heap_existe"), tx("lessonspart5a_porque_prato_pilha_jogado_fora")),
            Callout(REAL_WORLD, tx("lessonspart5a_recursao_sem_caso_base_pratos")),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.VISUAL, tx("lessonspart5a_pilha_chamadas_quadro_quadro"),
        listOf(
            Say(PROF, tx("lessonspart5a_hoje_gente_assiste_memoria_durante")),
            Code(P5_CHAIN1_CODE, tx("lessonspart5a_f_chama_g_g_chama")),
            Visual(VK.CALL_STACK, input = "chain1", caption = tx("solta_lessonspart5a_2")),
            Steps(
                tx("lessonspart5a_que_voce_acabou_ver"),
                listOf(
                    tx("lessonspart5a_main_chama_f_1_pilha"),
                    tx("lessonspart5a_f_chama_g_1_main"),
                    tx("lessonspart5a_g_chama_h_2_main"),
                    "h devolve 3 e sai; g devolve 3 e sai: [main, f]",
                    tx("lessonspart5a_f_chama_h_1_novo"),
                    "h devolve 2; f devolve 3 + 2 = 5; main devolve 5",
                ),
            ),
            Say(BYTE, tx("lessonspart5a_h_apareceu_duas_vezes_em")),
            Say(PROF, tx("lessonspart5a_exato_quadro_depende_quem_chamou")),
            Predict(
                tx("lessonspart5a_agora_fat_4_qual_altura"),
                listOf("4", "5", "8"),
                1,
                tx("lessonspart5a_main_fat_4_fat_3"),
            ),
            Visual(VK.CALL_STACK, input = "fat4", caption = tx("lessonspart5a_confira_pilha_sobe_ate_fat")),
            Text(tx("lessonspart5a_agora_recursao_que_ramifica_fib")),
            Code(
                """
                        main
                         |
                       fib(3)
                      /      \
                  fib(2)    fib(1)
                  /    \
              fib(1)  fib(0)
                """.trimIndent(),
                tx("lessonspart5a_arvore_ativacao_fib_3_6"),
            ),
            Visual(VK.CALL_STACK, input = "fib3", caption = tx("lessonspart5a_repare_em_cada_instante_pilha")),
            Predict(
                tx("lessonspart5a_em_fib_3_no_instante"),
                listOf("main, fib(3), fib(2), fib(1), fib(0)", "main, fib(3), fib(2), fib(0)", tx("lessonspart5a_so_fib_0")),
                1,
                tx("lessonspart5a_fib_1_esquerda_ja_terminou"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart5a_arvore_ativacao_conta_historia_inteira")),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.WORKED, tx("lessonspart5a_fat_3_na_mao_em"),
        listOf(
            Say(PROF, tx("lessonspart5a_exercicio_classico_rode_fat_3")),
            Code(P5_FAT_CODE, tx("comum_programa")),
            Steps(
                tx("lessonspart5a_resolvido_chamada_por_chamada"),
                listOf(
                    "1. main chama fat(3): quadro {n=3, volta para: main, linha 6}",
                    tx("lessonspart5a_2_3_1_entao_fat"),
                    "3. fat(2) chama fat(1): quadro {n=1, volta para: fat, linha 3}. Pilha: main, fat(3), fat(2), fat(1) — 4 quadros",
                    tx("lessonspart5a_4_fat_1_cai_no"),
                    tx("solta_lessonspart5a_3"),
                    "6. fat(3) retoma: 3 * 2 = 6; devolve 6 e sai",
                    tx("lessonspart5a_7_main_recebe_6"),
                ),
            ),
            Text(tx("solta_lessonspart5a_4")),
            // As quatro peças do fat.asm, uma por vez, antes do programa inteiro (revisor aluno do bloco 5).
            Text(tx("lessonspart5a_pecas_uma_por_vez")),
            Code(p5Asm(P5_PECA_SP), tx("lessonspart5a_peca_sp_titulo")),
            Text(tx("lessonspart5a_peca_sp")),
            Code(p5Asm(P5_PECA_DESLOCAMENTO), tx("lessonspart5a_peca_desloc_titulo")),
            Text(tx("lessonspart5a_peca_desloc")),
            Predict(
                tx("lessonspart5a_peca_desloc_q"),
                listOf("4", "996", "1000", "1004"),
                3,
                tx("lessonspart5a_peca_desloc_r"),
            ),
            Code(p5Asm(P5_PECA_JAL), tx("lessonspart5a_peca_jal_titulo")),
            Text(tx("lessonspart5a_peca_jal")),
            Code(p5Asm(P5_PECA_RA), tx("lessonspart5a_peca_ra_titulo")),
            Text(tx("lessonspart5a_peca_ra")),
            Predict(
                tx("lessonspart5a_peca_ra_q"),
                listOf(tx("lessonspart5a_peca_ra_o1"), tx("lessonspart5a_peca_ra_o2"), tx("lessonspart5a_peca_ra_o3")),
                1,
                tx("lessonspart5a_peca_ra_r"),
            ),
            Code(P5_FAT_ASM, tx("lessonspart5a_fat_asm_prologo_abre_quadro")),
            Steps(
                tx("lessonspart5a_quem_faz_que_na_chamada"),
                listOf(
                    tx("solta_lessonspart5a_5"),
                    "**Chamado, na entrada (prólogo):** `addi \$sp, \$sp, -8` abre o quadro; salva `\$ra` e `\$a0` nele",
                    tx("solta_lessonspart5a_6"),
                    tx("solta_lessonspart5a_7"),
                ),
            ),
            Say(BYTE, tx("lessonspart5a_deixa_eu_tentar_sozinho_acho")),
            Code(P5_CHAIN2_CODE, tx("lessonspart5a_sua_vez_main_chama_f")),
            Reveal(tx("lessonspart5a_qual_pilha_no_ponto_mais"), tx("lessonspart5a_main_f_c_2_g")),
            Reveal(tx("lessonspart5a_que_valor_cada_chamada_devolve"), "h(4) = 5; g(2) = 5; depois f chama h(2) = 3; f devolve 5 + 3 = 8; main devolve 8."),
            Reveal(tx("lessonspart5a_para_onde_volta_segundo_h"), tx("lessonspart5a_para_f_na_linha_8")),
            Visual(VK.CALL_STACK, input = "chain2", caption = tx("lessonspart5a_confira_suas_tres_respostas")),
            Callout(WATCH_OUT, tx("solta_lessonspart5a_8")),
            Callout(EXAM_TIP, tx("lessonspart5a_na_prova_desenhe_pilha_como")),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.SOCRATIC, tx("lessonspart5a_por_que_pilha"),
        listOf(
            Say(PROF, tx("lessonspart5a_vamos_reinventar_ambiente_execucao_so")),
            Reveal(tx("lessonspart5a_ideia_mais_simples_cada_variavel"), tx("lessonspart5a_so_existe_n_para_funcao")),
            Reveal(tx("lessonspart5a_entao_cada_chamada_precisa_sua"), tx("lessonspart5a_ultima_chamada_comecar_primeira_terminar")),
            Say(BYTE, tx("lessonspart5a_ta_mas_alem_n_que")),
            Reveal(tx("lessonspart5a_quando_fat_1_termina_como"), tx("solta_lessonspart5a_9")),
            Reveal(tx("lessonspart5a_como_devolver_resultado_voltar_usar"), tx("solta_lessonspart5a_10")),
            Predict(
                tx("lessonspart5a_quem_deve_abrir_espaco_das"),
                listOf(
                    tx("lessonspart5a_chamador_porque_ele_sabe_quantos"),
                    tx("lessonspart5a_chamado_porque_so_ele_sabe"),
                    tx("lessonspart5a_sistema_operacional"),
                ),
                1,
                tx("lessonspart5a_tamanho_das_locais_segredo_chamado"),
            ),
            Reveal(tx("lessonspart5a_funcao_cria_lista_devolve_para"), tx("lessonspart5a_nao_quadro_desempilhado_no_retorno")),
            Reveal(tx("lessonspart5a_recursao_infinita_que_acontece_com"), tx("lessonspart5a_cada_chamada_empilha_quadro_nenhuma")),
            Say(BYTE, tx("lessonspart5a_passar_parametro_por_referencia_muda")),
            Reveal(tx("lessonspart5a_em_swap_x_y_por"), tx("lessonspart5a_por_valor_copias_x_y")),
            Visual(VK.CALL_STACK, input = "fib3", caption = tx("lessonspart5a_teste_antes_cada_passo_preveja")),
            Callout(BRAIN_POWER, tx("lessonspart5a_repare_no_que_voce_acabou")),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.FORMAL, tx("lessonspart5a_organizacao_memoria_registros_ativacao"),
        listOf(
            Text(tx("lessonspart5a_ambiente_execucao_run_time_environment")),
            Code(
                p5Asm(
                    tx("lessonspart5a_enderecos_altos_pilha_topo_em"),
                ),
                tx("lessonspart5a_subdivisao_tipica_memoria_em_tempo"),
            ),
            Code(
                tx("lessonspart5a_parametros_reais_preenchidos_pelo_chamador"),
                tx("lessonspart5a_registro_ativacao_quadro_campos_tipicos"),
            ),
            Steps(
                tx("lessonspart5a_sequencia_chamada_retorno"),
                listOf(
                    tx("solta_lessonspart5a_11"),
                    tx("lessonspart5a_chamador_registra_endereco_retorno_vinculo"),
                    tx("solta_lessonspart5a_12"),
                    tx("solta_lessonspart5a_13"),
                    tx("lessonspart5a_chamador_recupera_valor_devolvido_continua"),
                ),
            ),
            Text(tx("lessonspart5a_arvore_ativacao_cada_no_ativacao")),
            Steps(
                tx("lessonspart5a_tres_estrategias_alocacao"),
                listOf(
                    tx("lessonspart5a_estatica_endereco_fixo_em_tempo"),
                    tx("lessonspart5a_em_pilha_quadro_por_ativacao"),
                    tx("lessonspart5a_em_heap_blocos_com_tempo"),
                ),
            ),
            Code(
                p5Asm(
                    tx("lessonspart5a_a0_a3_argumentos_v0_v1"),
                ),
                tx("lessonspart5a_convencao_chamada_mips"),
            ),
            Predict(
                tx("lessonspart5a_qual_vinculo_aponta_para_quadro"),
                listOf(tx("lessonspart5a_vinculo_acesso_estatico"), tx("lessonspart5a_vinculo_controle_dinamico"), tx("lessonspart5a_endereco_retorno")),
                1,
                tx("lessonspart5a_controle_dinamico_cadeia_chamadas_acesso"),
            ),
            Visual(VK.CALL_STACK, input = "fat3", caption = tx("lessonspart5a_definicao_em_acao_4_ativacoes")),
            Callout(EXAM_TIP, tx("lessonspart5a_questao_classica_desenhe_registro_ativacao")),
            Callout(WATCH_OUT, tx("lessonspart5a_endereco_retorno_vinculo_controle_primeiro")),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.MNEMONIC, tx("lessonspart5a_predio_memoria_cafe_manha_quadro"),
        listOf(
            Say(NARRADOR, tx("lessonspart5a_palacio_memoria_forja_codigo_penultima")),
            Callout(MNEMONIC, tx("lessonspart5a_predio_baixo_para_cima_c")),
            Callout(MNEMONIC, tx("lessonspart5a_heap_sobe_pilha_desce_se")),
            Callout(MNEMONIC, tx("lessonspart5a_controle_chama_acesso_aninha_vinculo")),
            Callout(MNEMONIC, tx("lessonspart5a_cafe_manha_quadro_pao_vitamina")),
            Callout(MNEMONIC, tx("solta_lessonspart5a_14")),
            Say(BYTE, tx("lessonspart5a_pra_lembrar_quem_faz_que")),
            Say(PROF, tx("lessonspart5a_recepcao_hotel_hospede_chamador_chega")),
            Predict(
                tx("lessonspart5a_em_main_f_g_h"),
                listOf("main", "f", "g", "h"),
                2,
                tx("lessonspart5a_controle_quem_chamou_quem_chamou"),
            ),
            Visual(VK.CALL_STACK, input = "chain1", caption = tx("lessonspart5a_suba_desca_predio_cada_andar")),
            Reveal(tx("lessonspart5a_recite_7_campos_quadro_pelo"), tx("lessonspart5a_pao_vitamina_cafe_acucar_esfirra")),
            Callout(FIRESIDE, tx("lessonspart5a_pilha_eu_sou_rapida_organizada")),
        ),
    ),
)
