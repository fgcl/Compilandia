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
            Text("Na bancada cabem só 32 potinhos com etiqueta (`\$t0`, `\$t1`, ...). Ele **só cozinha o que está na bancada**: para usar a farinha, vai à despensa buscar (**lw**) e, quando termina, leva o resultado de volta (**sw**)."),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    "Potinhos da bancada → **registradores** (`\$t0`–`\$t9`, `\$s0`–`\$s7`, ...): rápidos e poucos",
                    tx("lessonspart5b_prateleiras_despensa_memoria_declarada_na"),
                    "Buscar na despensa → **lw** (*load word*); guardar de volta → **sw** (*store word*)",
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
            Callout(WATCH_OUT, "`t1` (sem cifrão) é uma **prateleira** da despensa: uma variável na .data que o gerador criou para o temporário. `\$t1` (com cifrão) é um **potinho** da bancada. Nomes parecidos, lugares diferentes! No seu trabalho, chamar os temporários de `_t1` evita confusão."),
            Say(BYTE, tx("lessonspart5b_mas_ir_despensa_toda_hora")),
            Say(PROF, tx("lessonspart5b_muito_cozinheiro_esperto_deixa_na")),
            Reveal(tx("lessonspart5b_na_analogia_que_syscall"), "É tocar a campainha do salão: o cozinheiro pede ao gerente (o sistema / o MARS) algo que ele não faz sozinho, como mostrar o prato ao cliente (imprimir) ou fechar a cozinha (encerrar). O pedido vai em `\$v0`: 1 = imprimir inteiro, 4 = imprimir texto, 10 = encerrar."),
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
            Visual(VK.MIPS, input = tx("comum_b_c_2"), caption = "Cada linha TAC vira um bloquinho. Avance e veja \$t1, \$t2, \$t3 e a memória."),
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
                    "Com a=1, b=2: \$t3 = 0, o beq pula para L0 e x = b",
                ),
            ),
            Predict(
                tx("lessonspart5b_se_voce_esquecer_j_l1"),
                listOf(tx("comum_nada_muda"), tx("lessonspart5b_executa_then_depois_cai_no"), tx("lessonspart5b_erro_montagem_no_mars")),
                1,
                tx("lessonspart5b_processador_nao_conhece_chaves_ele"),
            ),
            Visual(VK.MIPS, input = tx("lessonspart5b_b_2_2"), caption = tx("lessonspart5b_bonus_media_inteira_div_tres")),
            Callout(BRAIN_POWER, "Repare que o gerador ingênuo faz `sw \$t3, t1` e logo depois `lw \$t1, t1`: guarda e busca a mesma coisa. Dá para cortar isso? (Spoiler: é a primeira coisa que a alocação de registradores elimina.)"),
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
            Reveal(tx("lessonspart5b_qual_desvio"), "`bne \$t3, \$zero, L1`: se a condição de SAÍDA (i > 3) é verdadeira (≠ 0), pula para fora do laço. (Equivalente: calcular i <= 3 com `sle` e usar `beq \$t3, \$zero, L1`.)"),
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
            Reveal(tx("lessonspart5b_se_operando_constante_como_em"), "Não há o que buscar na memória: usa-se `li \$t2, 2` (carrega imediato). Para somar uma constante existe até atalho: `addi \$t3, \$t1, 2`."),
            Reveal(tx("lessonspart5b_onde_variaveis_temporarios_moram_no"), tx("lessonspart5b_na_secao_data_word_para")),
            Say(BYTE, tx("lessonspart5b_mas_if_mips_nao_tem")),
            Reveal(tx("lessonspart5b_como_fazer_se_b_faca"), "Calcule a condição num registrador (sgt/slt deixam 1 ou 0). Se ela for 0, pule para o rótulo do else (`beq \$t3, \$zero, L0`). No fim do then, um `j L1` pula o else."),
            Reveal(tx("lessonspart5b_while_que_desvio_faz_ele"), tx("lessonspart5b_j_para_tras_volta_ao")),
            Predict(
                "Para que serve o registrador `\$zero`?",
                listOf(
                    tx("lessonspart5b_guarda_ultimo_resultado_que_deu"),
                    "Vale sempre 0 — ótimo para comparar com 'falso' (beq \$t3, \$zero, L0) e para copiar",
                    tx("lessonspart5b_conta_quantas_instrucoes_ja_rodaram"),
                ),
                1,
                "Escrever nele não adianta: ele é fixo em 0. `beq \$t3, \$zero, L0` se lê 'se \$t3 é falso, pule'. E `move \$t0, \$t1` é, por baixo, uma soma com `\$zero`.",
            ),
            Reveal(tx("lessonspart5b_como_programa_imprime_algo_se"), "Pede ao sistema: põe o código do serviço em `\$v0` (1 = inteiro, 4 = string, 10 = sair), o argumento em `\$a0` (o número, ou o endereço da string, obtido com `la`) e executa `syscall`."),
            Say(BYTE, tx("lessonspart5b_pra_gastar_menos_lw_sw")),
            Reveal(tx("lessonspart5b_em_x_b_2_gerador"), "Três: buscar a, buscar b e guardar x. Os temporários podem viver em registradores: `lw \$t0, a / lw \$t1, b / li \$t2, 2 / mul \$t1, \$t1, \$t2 / add \$t0, \$t0, \$t1 / sw \$t0, x`. Decidir quem fica em qual registrador é a **alocação de registradores**."),
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
            Callout(EXAM_TIP, "Em 'gere o código MIPS', escreva SEMPRE as duas seções (.data com variáveis e temporários; .text começando em main:), comente cada bloco com a instrução TAC correspondente e termine com `li \$v0, 10` + `syscall`. Sem isso o MARS até para ('dropped off bottom'), mas o professor desconta."),
            Callout(WATCH_OUT, "Em `lw` e `sw` o REGISTRADOR vem primeiro nos dois, mas a direção muda: `lw \$t1, a` leva a → \$t1; `sw \$t1, a` leva \$t1 → a. E o `div` de três operandos é pseudo: o real é `div \$t1, \$t2` seguido de `mflo \$t3` (quociente; o resto fica em hi, lido com `mfhi`)."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.MNEMONIC, tx("lessonspart5b_busca_busca_mistura_guarda"),
        listOf(
            Say(NARRADOR, tx("lessonspart5b_palacio_memoria_forja_codigo_ultima")),
            Callout(MNEMONIC, tx("lessonspart5b_busca_busca_mistura_guarda_toda")),
            Callout(MNEMONIC, "**Load = Leva para a bancada; Store = Salva na despensa.** Nos dois, o registrador vem PRIMEIRO: `lw \$t1, a` e `sw \$t1, a` — só muda a direção da seta."),
            Callout(MNEMONIC, "**Família de registradores pela inicial:** `\$t` = **t**emporário (pode sujar), `\$s` = **s**alvo (guarde antes de usar), `\$a` = **a**rgumento, `\$v` = **v**alor de retorno, `\$ra` = **r**eturn **a**ddress, `\$sp` = **s**tack **p**ointer."),
            Callout(MNEMONIC, "**beq = Branch if EQual (pula se igual); bne = Branch if Not Equal.** O if testa AO CONTRÁRIO: 'se a condição é falsa (igual a \$zero), pula o then'."),
            Callout(MNEMONIC, "**syscall 1, 4, 10: 'um número, uma fila de letras e dez-liga'.** 1 imprime inteiro; 4 imprime string ('fila' tem 4 letras; use `la \$a0, msg`); 10 encerra (dez... desliga!)."),
            Say(BYTE, tx("lessonspart5b_if_com_rotulos_eu_sempre")),
            Say(PROF, tx("lessonspart5b_pense_numa_estrada_com_bifurcacao")),
            Predict(
                tx("lessonspart5b_no_esqueleto_while_para_onde"),
                listOf(tx("lessonspart5b_para_rotulo_fim_laco"), tx("lessonspart5b_para_rotulo_teste_no_comeco"), tx("lessonspart5b_para_main")),
                1,
                tx("lessonspart5b_while_volta_testa_novo_unico"),
            ),
            Visual(VK.MIPS, input = tx("lessonspart5b_d_c"), caption = tx("lessonspart5b_bata_ritmo_busca_busca_mistura")),
            Reveal(tx("lessonspart5b_recite_duas_secoes_asm_que"), ".data: as variáveis (`x: .word 0`) e as strings (`msg: .asciiz \"...\"`). .text: as instruções, começando em `main:` e terminando com `li \$v0, 10` + `syscall`."),
            Callout(FIRESIDE, tx("lessonspart5b_memoria_eu_guardo_tudo_sou")),
        ),
    ),
)
