package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.AstTools
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 6b do conteúdo: AST_BUILD (Cidade Semântica — Fábrica de AST).
// Contagens de nós conferidas com AstTools.concreteTree / countNodes (gramática E/T/F do motor):
//  "a + b * c" 13 × 5;  "( a + b ) * c" 18 × 5;  "a - b - c" 14 × 5;  "( ( a ) )" 14 × 1;
//  "2 * x + y" 13 × 5;  "a - ( b - c )" 19 × 5;  "( a + b ) * ( c - x )" 28 × 7.

/** Assembly / Bison em strings cruas: '§' vira '$'. */
internal fun p6Asm(s: String): String = s.trimIndent().replace('§', '$')

private val P6_AST_CMDS: String = tx("lessonspart6b_abstract_class_cmd_class_atrib")

private val P6_AST_BISON: String = p6Asm(
    tx("lessonspart6b_exp_exp_termo_new_binop"),
)

private val P6_AST_VISIT: String = tx("lessonspart6b_abstract_class_expr_abstract_int")

internal val p6AstLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.STORY, tx("lessonspart6b_resumo_livro_inteiro"),
        listOf(
            Say(NARRADOR, tx("lessonspart6b_vespera_prova_literatura_byte_tem")),
            Say(BYTE, tx("lessonspart6b_resumo_obvio_mas_nao_perde")),
            Say(PROF, tx("lessonspart6b_bom_resumo_nao_perde_nada")),
            Steps(
                tx("lessonspart6b_livro_resumo_arvore_derivacao_ast"),
                listOf(
                    tx("lessonspart6b_livro_inteiro_arvore_derivacao_concreta"),
                    tx("lessonspart6b_capitulos_transicao_vira_t_que"),
                    tx("lessonspart6b_numeracao_indice_parenteses_pontuacao_so"),
                    tx("lessonspart6b_paginas_em_branco_producoes_somem"),
                    tx("lessonspart6b_personagens_acoes_operadores_operandos_viram"),
                ),
            ),
            Predict(
                tx("lessonspart6b_para_b_c_arvore_derivacao"),
                listOf("13", "9", "5", "3"),
                2,
                tx("lessonspart6b_so_sobram_3_operandos_2"),
            ),
            Visual(VK.AST, input = tx("comum_b_c"), caption = tx("lessonspart6b_lado_lado_livro_derivacao_resumo")),
            Say(BYTE, tx("lessonspart6b_entao_ast_so_arvore_mais")),
            Say(PROF, tx("lessonspart6b_porque_depois_analise_sintatica_todo")),
            Callout(REAL_WORLD, tx("lessonspart6b_no_trabalho_pratico_java")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart6b_p_se_eu_jogo_fora")),
            Reveal(tx("lessonspart6b_na_analogia_quem_livro_inteiro"), tx("lessonspart6b_arvore_derivacao_ast_nao_terminal")),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.VISUAL, tx("lessonspart6b_dieta_arvores_ao_vivo"),
        listOf(
            Say(PROF, tx("lessonspart6b_hoje_cada_widget_mostra_mesma")),
            Visual(VK.AST, input = tx("lessonspart6b_a"), caption = tx("lessonspart6b_caso_extremo_14_nos_na")),
            Predict(
                tx("lessonspart6b_que_restou_na_ast"),
                listOf(tx("lessonspart6b_binop_com_dois_filhos"), tx("lessonspart6b_unico_var"), tx("lessonspart6b_dois_nos_parenteses"), tx("lessonspart6b_nada_ast_fica_vazia")),
                1,
                tx("lessonspart6b_parenteses_so_agrupavam_nao_ha"),
            ),
            Visual(VK.AST, input = tx("lessonspart6b_b_c"), caption = tx("lessonspart6b_associatividade_14_nos_contra_5")),
            Predict(
                tx("lessonspart6b_na_ast_b_c_qual"),
                listOf(tx("lessonspart6b_esquerda_b_c"), tx("lessonspart6b_direita_b_c"), tx("lessonspart6b_tanto_faz_mesmo_valor")),
                0,
                tx("lessonspart6b_gramatica_t_recursiva_esquerda_entao"),
            ),
            Visual(VK.AST, input = tx("lessonspart6b_b_c_2"), caption = tx("lessonspart6b_agora_com_parenteses_direita_19")),
            Visual(VK.AST, input = tx("lessonspart6b_b_c_x"), caption = tx("lessonspart6b_28_nos_na_derivacao_7")),
            Steps(
                tx("lessonspart6b_regras_que_voce_acabou_ver"),
                listOf(
                    tx("lessonspart6b_folha_id_num_var_ou"),
                    tx("lessonspart6b_producao_com_operador_t_binop"),
                    tx("lessonspart6b_producao_corrente_t_t_f"),
                    tx("solta_lessonspart6b_1"),
                    tx("lessonspart6b_em_expressoes_binarias_nos_ast"),
                ),
            ),
            Visual(VK.MINILANG, input = "m = a;\nif (b > m) { m = b; }\nprint(m);", caption = tx("lessonspart6b_comandos_tambem_viram_nos_atrib")),
            Callout(BRAIN_POWER, tx("lessonspart6b_expressao_com_10_operadores_binarios")),
            Reveal(tx("comum_resposta"), tx("lessonspart6b_21_nos_10_binop_11")),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.WORKED, tx("lessonspart6b_construindo_ast_na_descida_recursiva"),
        listOf(
            Say(PROF, tx("lessonspart6b_lembra_parser_descendente_recursivo_com")),
            Code(AstTools.javaClasses, tx("lessonspart6b_classes_ast_java_como_no")),
            Code(AstTools.javaParserBuildingAst, tx("lessonspart6b_parser_que_constroi_ast_while")),
            Steps(
                tx("lessonspart6b_rastreando_2_x_y"),
                listOf(
                    "expr() chama termo(); termo() chama fator().",
                    tx("lessonspart6b_fator_ve_2_numero_devolve"),
                    tx("lessonspart6b_termo_ve_casa_chama_fator"),
                    tx("lessonspart6b_termo_ve_que_nao_nem"),
                    tx("lessonspart6b_volta_em_expr_binop_ve"),
                    tx("lessonspart6b_new_binop_binop_num_2"),
                ),
            ),
            Visual(VK.AST, input = "2 * x + y", caption = tx("lessonspart6b_confira_13_nos_na_derivacao")),
            Callout(WATCH_OUT, tx("lessonspart6b_linha_mais_importante_new_binop")),
            Text(tx("lessonspart6b_agora_com_voce_exemplo_esmaecido")),
            Reveal(tx("lessonspart6b_que_fator_devolve_ao_encontrar"), tx("lessonspart6b_casa_chama_expr_que_monta")),
            Reveal(tx("lessonspart6b_escreva_codigo_java_completo_ast"), tx("lessonspart6b_new_binop_new_var_new")),
            Reveal(tx("lessonspart6b_quantos_nos_tem_cada_arvore"), tx("lessonspart6b_arvore_derivacao_19_nos_ast")),
            Visual(VK.AST, input = tx("lessonspart6b_b_c_2"), caption = tx("comum_confira_suas_respostas")),
            Callout(EXAM_TIP, tx("lessonspart6b_questao_comum_dada_gramatica_com")),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.SOCRATIC, tx("lessonspart6b_que_compilador_realmente_precisa_saber"),
        listOf(
            Say(PROF, tx("lessonspart6b_hoje_eu_pergunto_voce_descobre")),
            Visual(VK.AST, input = tx("comum_b_c_3"), caption = tx("lessonspart6b_18_nos_na_derivacao_olhe")),
            Predict(
                tx("lessonspart6b_para_gerar_codigo_dessa_expressao"),
                listOf(tx("lessonspart6b_quais_operacoes_em_que_ordem"), tx("lessonspart6b_quais_nao_terminais_foram_usados"), tx("lessonspart6b_onde_estavam_parenteses_no_texto"), tx("lessonspart6b_quantos_espacos_havia_entre_tokens")),
                0,
                tx("lessonspart6b_gerar_t1_b_t2_t1"),
            ),
            Predict(
                tx("lessonspart6b_se_ast_nao_tem_parenteses"),
                listOf(tx("lessonspart6b_num_campo_prioridade_em_cada"), tx("lessonspart6b_na_forma_arvore_filho"), tx("lessonspart6b_numa_tabela_parte"), tx("lessonspart6b_ela_se_perde")),
                1,
                tx("lessonspart6b_quem_esta_mais_fundo_avaliado"),
            ),
            Say(BYTE, tx("lessonspart6b_no_t_no_meio_corrente")),
            Reveal(tx("lessonspart6b_faz_alguma_coisa"), tx("lessonspart6b_na_derivacao_ele_registra_que")),
            Predict(
                tx("lessonspart6b_no_tp_seu_grupo_gera"),
                listOf(tx("comum_nada"), tx("lessonspart6b_voce_ja_emitiu_codigo_para"), tx("lessonspart6b_analisador_lexico_quebra")),
                1,
                tx("lessonspart6b_traducao_direta_passada_so_com"),
            ),
            Reveal(tx("lessonspart6b_cite_duas_outras_vantagens_construir"), tx("lessonspart6b_1_separacao_responsabilidades_parser_so")),
            Predict(
                tx("lessonspart6b_comandos_como_if_while_eles"),
                listOf(tx("lessonspart6b_somem_como_parenteses"), tx("lessonspart6b_nos_proprios_como_se_cond"), tx("lessonspart6b_folhas_var")),
                1,
                tx("lessonspart6b_cada_construcao_com_significado_vira"),
            ),
            Visual(VK.MINILANG, input = "i = 0;\ns = 0;\nwhile (i < 4) { s = s + i; i = i + 1; }\nprint(s);", caption = tx("lessonspart6b_enquanto_com_bloco_duas_atrib")),
            Callout(FIRESIDE, tx("lessonspart6b_arvore_derivacao_eu_tenho_tudo")),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.FORMAL, tx("lessonspart6b_ast_definicao_prova"),
        listOf(
            Text(tx("lessonspart6b_arvore_derivacao_concreta_raiz_simbolo")),
            Text(tx("lessonspart6b_arvore_sintatica_abstrata_ast_cada")),
            Code(P6_AST_CMDS, tx("lessonspart6b_comandos_como_classes_junto_com")),
            Text(tx("lessonspart6b_construcao_dirigida_pela_sintaxe_cada")),
            Code(P6_AST_BISON, tx("lessonspart6b_no_bison_ascendente_valor_lado")),
            Predict(
                tx("solta_lessonspart6b_2"),
                listOf(tx("lessonspart6b_porque_parentese_2o_simbolo"), tx("lessonspart6b_porque_exp_2o_simbolo_lado"), tx("lessonspart6b_para_criar_no_parenteses")),
                1,
                tx("lessonspart6b_1_exp_2_3_repassar"),
            ),
            Text(tx("lessonspart6b_descendente_recursivo_cada_metodo_nao")),
            Code(P6_AST_VISIT, tx("lessonspart6b_percorrendo_ast_cada_fase_metodo")),
            Callout(REAL_WORLD, tx("lessonspart6b_com_muitas_fases_em_vez")),
            Callout(EXAM_TIP, tx("lessonspart6b_tres_perguntas_que_caem_1")),
            Reveal(tx("lessonspart6b_defina_ast_cite_tres_coisas"), tx("lessonspart6b_arvore_em_que_nos_internos")),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.MNEMONIC, tx("lessonspart6b_arvore_dieta"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart6b_ast_arvore_sem_tralha_tira")),
            Text(tx("lessonspart6b_palacio_memoria_na_cidade_semantica")),
            Callout(MNEMONIC, tx("lessonspart6b_parenteses_sao_andaimes_indispensaveis_enqua")),
            Callout(MNEMONIC, tx("lessonspart6b_corrente_whatsapp_t_f_como")),
            Callout(MNEMONIC, tx("lessonspart6b_quem_esta_mais_fundo_trabalha")),
            Predict(
                tx("lessonspart6b_qual_linha_codigo_segredo_associatividade"),
                listOf("return new BinOp(op, termo(), expr());", tx("solta_lessonspart6b_3"), "if (tok.equals(\"(\")) return expr();"),
                1,
                tx("lessonspart6b_velho_vai_para_esquerda_resultado"),
            ),
            Callout(MNEMONIC, tx("lessonspart6b_bison_em_tres_palavras_filho")),
            Callout(WATCH_OUT, tx("lessonspart6b_contrastes_que_confundem_arvore_derivacao")),
            Visual(VK.AST, input = tx("comum_b_c_3"), caption = tx("lessonspart6b_corta_cpe_conte_correntes_parenteses")),
            Reveal(tx("lessonspart6b_sem_olhar_que_jogado_fora"), tx("lessonspart6b_fora_correntes_parenteses_pontuacao_fica")),
            Reveal(tx("lessonspart6b_por_que_vale_pena_montar"), tx("lessonspart6b_porque_ai_compilador_pode_percorrer")),
        ),
    ),
)
