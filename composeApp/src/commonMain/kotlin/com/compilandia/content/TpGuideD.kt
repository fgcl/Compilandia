package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Guia do Trabalho Prático — capítulos 9 e 10 (otimizações e testes, checklist de entrega).

// ───────────────────────────── Capítulo 9 ─────────────────────────────

internal val tpCap9 = GuideChapter(
    9, tx("tpguided_otimizacoes_simples_bateria_testes"),
    tx("tpguided_dobrar_constantes_cortar_codigo_morto"),
    Skill.OPTIMIZATION,
    listOf(
        Say(BYTE, tx("solta_tpguided_1")),
        Say(PROF, tx("tpguided_vai_nao_ser_que_compilador")),
        Visual(VK.TAC, input = tx("comum_2_3"), caption = tx("tpguided_repare_na_instrucao_2_3")),
        Text(tx("tpguided_truque_para_encaixar_isso_no")),
        Code(
            tx("tpguided_private_record_res_tipodado_tipo"),
            tx("tpguided_dobramento_constantes_res_agora_pode"),
        ),
        Text(tx("tpguided_no_resto_gerador_sao_tres")),
        Code(
            $$"""
            # x <- 2 * 60 + 1;   SEM dobramento        # COM dobramento
                li  $t0, 2                                 li  $t0, 121
                li  $t1, 60                                sw  $t0, x_0
                mul $t0, $t0, $t1
                li  $t1, 1
                add $t0, $t0, $t1
                sw  $t0, x_0
            """.trimIndent(),
            tx("tpguided_seis_instrucoes_viram_duas"),
        ),
        Predict(
            tx("solta_tpguided_2"),
            listOf(
                tx("tpguided_sim_2_3_dobrado"),
                tx("tpguided_nao_arvore_2_3_nenhuma"),
                tx("tpguided_nao_porque_variavel_estraga_expressao"),
                tx("tpguided_sim_mas_so_se_for"),
            ),
            1,
            tx("tpguided_associa_esquerda_primeiro_2_lado"),
        ),
        Callout(WATCH_OUT, tx("tpguided_nunca_dobre_x_0_com")),
        Say(LEXI, tx("solta_tpguided_3")),
        Code(
            $$"""
                private boolean emitindo = true;
                // emite(...) e rotulo(...) ganham um 'if (emitindo)' na frente

                private void blocoMorto() {                    // analisa e verifica, mas não gera código
                    boolean antes = emitindo;
                    emitindo = false;
                    bloco();
                    emitindo = antes;
                }

                private Res condicao() {                       // agora devolve o Res (pode ser constante)
                    casaToken(TipoToken.ABRE_PAR);
                    int linha = tok.linha;
                    Res c = expr();
                    exige(c.tipo() == TipoDado.LOGICO, linha, "a condição precisa ser logico");
                    casaToken(TipoToken.FECHA_PAR);
                    liberaTemp();
                    return c;
                }

                private void se() {
                    casaToken(TipoToken.SE);
                    Res c = condicao();
                    if (c.constante() != null) {               // resultado conhecido JÁ na compilação
                        boolean entra = c.constante() != 0;
                        if (entra) bloco(); else blocoMorto();
                        if (eh(TipoToken.SENAO)) {
                            avanca();
                            if (entra) blocoMorto(); else bloco();
                        }
                        return;                                // nenhum beq, nenhum rótulo
                    }
                    String senao = novoRotulo("senao"), fim = novoRotulo("fimse");
                    emite("beq " + noRegistrador(c) + ", $zero, " + senao);
                    // ... o resto igual ao capítulo 8
                }
            """.trimIndent(),
            tx("tpguided_eliminacao_codigo_morto_bloco_continua"),
        ),
        Callout(NO_DUMB_QUESTIONS, tx("tpguided_p_por_que_nao_simplesmente")),
        Code(
            $$"""
            # antes                          # depois (peephole)
                lw   $t0, soma_1                 lw   $a0, soma_1
                move $a0, $t0
                li   $v0, 1                      li   $v0, 1
                syscall                          syscall

                sw   $t0, n_0                    sw   $t0, n_0
                lw   $t0, n_0                    # lw removido: $t0 já tem esse valor
            """.trimIndent(),
            tx("tpguided_otimizacao_peephole_janela_2_instrucoes"),
        ),
        Callout(WATCH_OUT, tx("solta_tpguided_4")),
        Say(PILHA, tx("tpguided_otimizou_entao_prove_que_nada")),
        Steps(
            tx("tpguided_bateria_testes_que_pega_bug"),
            listOf(
                tx("tpguided_precedencia_associatividade_escreva_2_3"),
                "**Negativos e resto**: `escreva(-7 / 2, \" \", -7 % 2);` → -3 -1.",
                tx("tpguided_lacos_enquanto_que_roda_0"),
                tx("tpguided_aninhamento_se_dentro_se_dentro"),
                tx("tpguided_escopos_sombreamento_x_interno_externo"),
                tx("tpguided_erros_arquivo_por_mensagem_erro"),
                tx("tpguided_bordas_arquivo_vazio_so_comentario"),
            ),
        ),
        Visual(VK.MIPS, input = tx("tpguided_b_c_d"), caption = tx("tpguided_treine_conferencia_mao_com_4")),
        Code(
            tx("tpguided_bin_bash_roda_testes_sh"),
            tx("tpguided_testes_regressao_automaticos_linux_macos"),
        ),
        Callout(REAL_WORLD, tx("tpguided_isso_teste_regressao_depois_cada")),
        Callout(EXAM_TIP, tx("tpguided_prova_adora_aplique_dobramento_constantes")),
        Reveal(tx("tpguided_por_que_gerar_esperado_rodando"), tx("tpguided_porque_se_compilador_esta_errado")),
    ),
)

// ───────────────────────────── Capítulo 10 ─────────────────────────────

internal val tpCap10 = GuideChapter(
    10, tx("tpguided_checklist_entrega_erros_que_tiram"),
    tx("tpguided_que_conferir_na_vespera_que"),
    Skill.ERROR_RECOVERY,
    listOf(
        Say(NARRADOR, tx("tpguided_vespera_entrega_23h47_geladeira_faz")),
        Say(BYTE, tx("tpguided_ta_tudo_funcionando_eu_acho")),
        Say(PROF, tx("tpguided_eu_acho_nao_teste_vamos")),
        Steps(
            tx("tpguided_checklist_codigo"),
            listOf(
                tx("tpguided_compila_zero_fora_ide_javac"),
                tx("tpguided_roda_exatamente_como_enunciado_manda"),
                tx("tpguided_nenhum_caminho_absoluto_c_users"),
                tx("tpguided_mensagens_erro_no_formato_pedido"),
                tx("tpguided_toda_bateria_testes_passa_rode"),
                tx("tpguided_asm_gerado_monta_roda_no"),
                tx("tpguided_readme_com_como_compilar_como"),
            ),
        ),
        Steps(
            tx("tpguided_checklist_relatorio"),
            listOf(
                tx("tpguided_especificacao_linguagem_tokens_com_regex"),
                tx("tpguided_afd_lexico_desenhado_lista_erros"),
                tx("tpguided_gramatica_ll_1_em_ebnf"),
                tx("tpguided_decisoes_projeto_maiusculas_sombreamento_con"),
                tx("tpguided_esquemas_traducao_molde_mips_cada"),
                tx("tpguided_otimizacoes_implementadas_com_exemplo_antes"),
                tx("tpguided_testes_tabela_com_programa_saida"),
            ),
        ),
        Callout(WATCH_OUT, tx("tpguided_linha_errada_nas_mensagens_erro")),
        Callout(WATCH_OUT, tx("tpguided_registradores_vazando_esquecer_liberatemp_na")),
        Code(
            tx("tpguided_private_void_comando_int_antes"),
            tx("tpguided_linha_assert_que_economiza_noite"),
        ),
        Predict(
            tx("tpguided_alguem_escreveu_no_lexico_if"),
            listOf(
                tx("tpguided_funciona_sempre"),
                tx("tpguided_vezes_nao_reconhece_palavra_em"),
                tx("comum_erro_compilacao_java"),
                tx("tpguided_funciona_so_no_windows"),
            ),
            1,
            tx("tpguided_em_java_entre_strings_pergunta"),
        ),
        Callout(WATCH_OUT, tx("tpguided_rotulos_nomes_colidindo_dois_se")),
        Callout(WATCH_OUT, tx("solta_tpguided_5")),
        Callout(WATCH_OUT, tx("solta_tpguided_6")),
        Callout(REAL_WORLD, tx("tpguided_integridade_academica_copiar_compilador_cole")),
        Callout(EXAM_TIP, tx("tpguided_treine_arguicao_em_voz_alta")),
        Reveal(tx("tpguided_arguicao_por_que_sua_gramatica"), tx("tpguided_porque_nao_tem_recursao_esquerda")),
        Reveal(tx("tpguided_arguicao_mostre_onde_tipo_b"), tx("tpguided_em_expradd_primario_devolve_inteiro")),
        Reveal(tx("tpguided_arguicao_como_seu_lexico_sabe"), tx("tpguided_estado_3_afd_depois_ler")),
        Reveal(tx("tpguided_arguicao_que_acontece_com_pilha"), tx("tpguided_fechaescopo_desempilha_hashmap_bloco_nomes")),
        Steps(
            tx("tpguided_plano_para_ultimas_48_horas"),
            listOf(
                tx("tpguided_48h_congele_funcionalidades_nada_recurso"),
                tx("tpguided_36h_rode_bateria_inteira_conserte"),
                tx("tpguided_24h_relatorio_figuras_afd_gramatica"),
                tx("tpguided_12h_clone_repositorio_numa_pasta"),
                tx("tpguided_2h_entregue_sim_antes_prazo"),
            ),
        ),
        Say(LEXI, tx("tpguided_tokens_cortados")),
        Say(PILHA, tx("tpguided_gramatica_empilhada")),
        Say(PROF, tx("tpguided_tipos_conferidos_mips_rodando_testes")),
        Reveal(tx("tpguided_ultima_recuperacao_recite_fases_seu"), tx("tpguided_lexico_lexico_afd_com_switch")),
    ),
)
