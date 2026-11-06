package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 4c do conteúdo: ERROR_RECOVERY (Templo Top-Down — Sala do Pânico).
// Posições de detecção e eventos de recuperação conferidos com PanicMode.trace / firstErrorPosition.

/** Lista de comandos de atribuição separados por ';' (LL(1)). FIRST(E) = {id, num}, FOLLOW(E) = {;}. */
internal const val P4_CMDS = "P -> S ; P | ε\nS -> id = E\nE -> id | num"

/** Expressões LL(1) do livro. */
internal const val P4_EXPR_LL = "E -> T E'\nE' -> + T E' | ε\nT -> F T'\nT' -> * F T' | ε\nF -> ( E ) | id"

private const val P4_BISON_ERRO =
    "%{\n#include <stdio.h>\nextern int yylineno;      /* %option yylineno no scanner.l */\nint yylex(void);\nvoid yyerror(const char *s);\n%}\n%define parse.error verbose\n%token ID NUM\n%%\nprograma : programa comando\n         | comando\n         ;\ncomando  : ID '=' expr ';'\n         | error ';'          { yyerrok; }\n         ;\nexpr     : ID\n         | NUM\n         ;\n%%\nvoid yyerror(const char *s) {\n    fprintf(stderr, \"Erro sintatico na linha %d: %s\\n\", yylineno, s);\n}"

// ───────────────────────────── ERROR_RECOVERY ─────────────────────────────

internal val p4ErrorLessons: List<Lesson> = listOf(
    Lesson(
        Skill.ERROR_RECOVERY, Style.STORY, tx("lessonspart4c_corretor_redacao_enem"),
        listOf(
            Say(NARRADOR, tx("lessonspart4c_pilha_300_redacoes_cafe_frio")),
            Say(BYTE, tx("lessonspart4c_ai_ele_rasga_redacao_vai")),
            Say(PROF, tx("lessonspart4c_claro_que_nao_ele_marca")),
            Steps(
                tx("lessonspart4c_tipos_erro_na_redacao_no"),
                listOf(
                    tx("lessonspart4c_caza_em_vez_casa_erro"),
                    tx("lessonspart4c_frase_sem_verbo_parentese_sem"),
                    tx("lessonspart4c_sol_congelou_feijoada_erro_semantico"),
                    tx("lessonspart4c_texto_perfeito_mas_fugiu_tema"),
                ),
            ),
            Steps(
                tx("lessonspart4c_quatro_estrategias_corretor"),
                listOf(
                    tx("lessonspart4c_modo_panico_pula_tudo_ate"),
                    tx("lessonspart4c_nivel_frase_faz_remendo_local"),
                    tx("lessonspart4c_producoes_erro_gabarito_ja_preve"),
                    tx("lessonspart4c_correcao_global_reescrever_redacao_com"),
                ),
            ),
            Predict(
                tx("lessonspart4c_qual_dessas_estrategias_compiladores_verdade"),
                listOf(tx("lessonspart4c_correcao_global"), tx("lessonspart4c_modo_panico_vezes_com_remendos"), tx("lessonspart4c_nenhuma_param_no_primeiro_erro"), tx("lessonspart4c_producoes_erro_para_todo_erro")),
                1,
                tx("lessonspart4c_panico_simples_seguro_correcao_global"),
            ),
            Say(PILHA, tx("lessonspart4c_no_meu_templo_ponto_final")),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num id = num ;", caption = "Faltou um ';' depois do primeiro comando. Veja em que token o erro aparece e como o parser segue em frente."),
            Reveal("O que o parser fez com o ';' que faltou?", tx("lessonspart4c_com_terminal_no_topo_pilha")),
            Callout(FIRESIDE, tx("lessonspart4c_corretor_eu_nao_conserto_redacao")),
            Reveal(tx("lessonspart4c_na_analogia_quem_ponto_final"), tx("lessonspart4c_token_sincronizacao_modo_panico_recuperacao")),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.VISUAL, tx("lessonspart4c_sala_panico_ao_vivo"),
        listOf(
            Say(PILHA, tx("lessonspart4c_bem_vindo_sala_panico_token")),
            Code(P4_CMDS, tx("lessonspart4c_gramatica_lista_atribuicoes_first_id")),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = = num ; id = num ;", caption = tx("lessonspart4c_mais_com_no_topo_na")),
            Text(tx("lessonspart4c_regra_panico_com_nao_terminal")),
            Predict(
                tx("lessonspart4c_no_exemplo_acima_parser_descarta"),
                listOf(tx("lessonspart4c_desempilha"), tx("lessonspart4c_continua_com_porque_num_first"), tx("lessonspart4c_desiste"), tx("lessonspart4c_volta_ao_inicio_programa")),
                1,
                tx("lessonspart4c_num_pode_comecar_entao_para"),
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = ; id = num ;", caption = tx("lessonspart4c_agora_expressao_sumiu_que_acontece")),
            Reveal(tx("lessonspart4c_por_que_aqui_foi_desempilhado"), tx("lessonspart4c_nao_comeca_mas_esta_em")),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id num ; id = num ;", caption = tx("lessonspart4c_faltou_desta_vez_quem_esta")),
            Text(tx("lessonspart4c_terminal_no_topo_diferente_entrada")),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num num ; id = id ;", caption = tx("lessonspart4c_num_mais_conte_quantas_recuperacoes")),
            Callout(WATCH_OUT, tx("lessonspart4c_erro_digitacao_gerou_duas_recuperacoes")),
            Say(BYTE, tx("lessonspart4c_funciona_com_expressoes_tambem")),
            Visual(VK.PANIC, grammar = P4_EXPR_LL, input = "id + * id", caption = tx("lessonspart4c_na_gramatica_expressoes_aparece_onde")),
            Reveal(tx("lessonspart4c_qual_token_descartado_em_id"), tx("lessonspart4c_com_t_no_topo_m")),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.WORKED, tx("lessonspart4c_rastreando_panico_na_mao"),
        listOf(
            Say(PROF, tx("lessonspart4c_vamos_rastrear_com_calma_modo")),
            Code(P4_CMDS, tx("lessonspart4c_first_p_id_first_id")),
            Steps(
                tx("lessonspart4c_resolvido_topo_pilha_direita"),
                listOf(
                    "Pilha `\$ P`, entrada `id = ; id = num ; \$`: M[P, id] = P → S ; P. Pilha vira `\$ P ; S`.",
                    "M[S, id] = S → id = E. Pilha `\$ P ; E = id`.",
                    "Casa `id`, casa `=`. Pilha `\$ P ; E`, entrada `; id = num ; \$`.",
                    tx("lessonspart4c_m_esta_vazia_erro_detectado"),
                    tx("lessonspart4c_panico_first_mas_follow_nada"),
                    tx("lessonspart4c_pilha_p_casa_resto_id"),
                    tx("lessonspart4c_no_fim_p_com_parser"),
                ),
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = ; id = num ;", caption = tx("lessonspart4c_confira_cada_passo_com_motor")),
            Callout(WATCH_OUT, tx("lessonspart4c_depois_erro_parser_nao_gera")),
            Text("Sua vez (exemplo esmaecido): `id = = num ; id = num ;`."),
            Reveal(tx("lessonspart4c_em_que_token_erro_detectado"), tx("lessonspart4c_no_3o_token_segundo_no")),
            Reveal(tx("lessonspart4c_que_panico_faz"), tx("lessonspart4c_nao_esta_em_first_nem")),
            Reveal("E em `id num ; id = num ;`, o que muda?", tx("lessonspart4c_erro_aparece_com_terminal_no")),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id num ; id = num ;", caption = tx("lessonspart4c_confira_ultima_resposta")),
            Say(PROF, tx("lessonspart4c_no_bison_voce_nao_escreve")),
            Code(P4_BISON_ERRO, tx("lessonspart4c_parser_y_error_descarta_tudo")),
            Callout(EXAM_TIP, tx("lessonspart4c_na_prova_rastreie_em_tabela")),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.SOCRATIC, tx("lessonspart4c_por_que_nao_parar_no"),
        listOf(
            Say(PROF, tx("lessonspart4c_perguntas_so_perguntas_voce_vai")),
            Predict(
                tx("lessonspart4c_seu_programa_tem_5_erros"),
                listOf("1", tx("lessonspart4c_pelo_menos_5"), tx("comum_nenhuma"), tx("lessonspart4c_depende_processador")),
                1,
                tx("lessonspart4c_erro_por_compilacao_pelo_menos"),
            ),
            Predict(
                "Em `id = num num ; id = id ;`, em qual token o parser LL(1) PERCEBE o erro?",
                listOf(tx("lessonspart4c_no_1o_id"), tx("lessonspart4c_no_3o_primeiro_num"), tx("lessonspart4c_no_4o_segundo_num"), tx("lessonspart4c_so_no_fim")),
                2,
                tx("lessonspart4c_id_num_ainda_pode_virar"),
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num num ; id = id ;", caption = tx("lessonspart4c_vermelho_marca_ponto_deteccao")),
            Say(BYTE, tx("lessonspart4c_ele_grita_exatamente_no_primeiro")),
            Say(PROF, tx("lessonspart4c_tem_propriedade_prefixo_viavel_ll")),
            Predict(
                tx("lessonspart4c_com_no_topo_token_inesperado"),
                listOf(tx("lessonspart4c_porque_menor_conjunto"), tx("lessonspart4c_porque_se_proximo_token_algo"), tx("lessonspart4c_porque_first_sempre_vazio"), tx("comum_por_tradicao")),
                1,
                tx("lessonspart4c_desempilhar_continuar_com_que_vem"),
            ),
            Predict(
                tx("lessonspart4c_por_que_tambem_parar_num"),
                listOf(tx("lessonspart4c_porque_ai_para_recomecar_proprio"), tx("lessonspart4c_nao_deveria_parar"), tx("lessonspart4c_para_gerar_mais_erros"), tx("lessonspart4c_porque_first_follow")),
                0,
                tx("lessonspart4c_em_id_num_depois_descartar"),
            ),
            Reveal(tx("lessonspart4c_por_que_sao_otimos_tokens"), tx("lessonspart4c_porque_marcam_fins_claros_construcoes")),
            Reveal(tx("lessonspart4c_qual_risco_sincronizar_no_lugar"), tx("lessonspart4c_erros_em_cascata_parser_volta")),
            Visual(VK.PANIC, grammar = P4_EXPR_LL, input = "( id + ) * id", caption = tx("lessonspart4c_desafio_antes_avancar_preveja_se")),
            Reveal(tx("lessonspart4c_resposta_desafio"), tx("lessonspart4c_sincronizador_follow_t_nada_descartado")),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.FORMAL, tx("lessonspart4c_tratamento_erros_sintaticos_definicao_prova"),
        listOf(
            Text(tx("lessonspart4c_objetivos_tratador_erros_dragao_4")),
            Text(tx("lessonspart4c_propriedade_prefixo_viavel_metodos_ll")),
            Steps(
                tx("lessonspart4c_estrategias_recuperacao"),
                listOf(
                    tx("lessonspart4c_modo_panico_descartar_simbolos_entrada"),
                    tx("lessonspart4c_nivel_frase_correcao_local_restante"),
                    tx("lessonspart4c_producoes_erro_aumentar_gramatica_com"),
                    tx("lessonspart4c_correcao_global_achar_cadeia_correta"),
                ),
            ),
            Code(
                tx("lessonspart4c_com_nao_terminal_no_topo"),
                tx("lessonspart4c_modo_panico_no_analisador_ll"),
            ),
            Visual(VK.LL1_TABLE, grammar = P4_CMDS, caption = tx("lessonspart4c_toda_celula_vazia_desta_tabela")),
            Predict(
                tx("lessonspart4c_na_tabela_livro_celula_m"),
                listOf(tx("lessonspart4c_descartar"), tx("lessonspart4c_desempilhar_pois_follow"), tx("lessonspart4c_aceitar_entrada"), tx("lessonspart4c_expandir")),
                1,
                tx("lessonspart4c_celula_em_branco_descarta_token"),
            ),
            Text(tx("lessonspart4c_no_yacc_bison_token_reservado")),
            Predict(
                tx("lessonspart4c_qual_nao_objetivo_tratador_erros"),
                listOf(tx("lessonspart4c_relatar_erros_com_clareza"), tx("lessonspart4c_detectar_erros_subsequentes"), tx("lessonspart4c_corrigir_programa_automaticamente_para_que"), tx("lessonspart4c_custo_minimo_em_programas_corretos")),
                2,
                tx("lessonspart4c_recuperar_corrigir_recuperacao_serve_para"),
            ),
            Callout(EXAM_TIP, tx("lessonspart4c_cai_muito_cite_explique_estrategias")),
            Reveal(tx("lessonspart4c_defina_propriedade_prefixo_viavel_em"), tx("lessonspart4c_analisador_detecta_erro_assim_que")),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.MNEMONIC, tx("lessonspart4c_first_fica_follow_foge"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart4c_first_fica_follow_foge_achou")),
            Callout(MNEMONIC, tx("lessonspart4c_terminal_teimoso_finge_que_estava")),
            Text(tx("lessonspart4c_palacio_memoria_no_templo_top")),
            Callout(MNEMONIC, tx("lessonspart4c_tipos_erro_em_zoom_crescente")),
            Callout(MNEMONIC, tx("lessonspart4c_estrategias_mais_simples_mais_ambiciosa")),
            Predict(
                tx("lessonspart4c_no_topo_token_x_na"),
                listOf(tx("lessonspart4c_desempilhar"), tx("lessonspart4c_descartar_x_olhar_proximo_token"), tx("comum_aceitar"), tx("lessonspart4c_empilhar_x")),
                1,
                tx("lessonspart4c_nem_fica_nem_foge_token"),
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num ; = = ; id = id ;", caption = tx("lessonspart4c_recite_enquanto_avanca_lixo_vai")),
            Callout(MNEMONIC, tx("lessonspart4c_prefixo_viavel_grito_imediato_ll")),
            Callout(MNEMONIC, tx("lessonspart4c_bison_error_coringa_que_casa")),
            Callout(WATCH_OUT, tx("lessonspart4c_confusoes_classicas_1_achar_que")),
            Reveal(tx("lessonspart4c_recite_que_fazer_com_no"), tx("lessonspart4c_descarta_tokens_ate_achar_first")),
            Callout(REAL_WORLD, tx("lessonspart4c_mensagem_boa_verdade_tem_linha")),
        ),
    ),
)
