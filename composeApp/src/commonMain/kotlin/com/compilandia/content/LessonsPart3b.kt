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
import com.compilandia.content.CalloutKind.REAL_WORLD
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.LEXI
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// =====================================================================
// YACC_BISON — Yacc/Bison: especificação e precedência
// =====================================================================

internal val bisonLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.YACC_BISON, Style.STORY, tx("lessonspart3b_fabrica_que_fabrica_analisadores"),
        listOf(
            Say(NARRADOR, tx("lessonspart3b_oficina_yacc_bison_bisao_avental")),
            Say(BYTE, tx("lessonspart3b_eu_passei_semana_montando_tabela")),
            Say(PROF, tx("lessonspart3b_tem_voce_escreve_receita_gramatica")),
            Text(tx("lessonspart3b_pense_numa_padaria_industrial_voce")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3b_ficha_pedido_arquivo_calc_y"),
                    tx("lessonspart3b_lista_ingredientes_token_num_left"),
                    tx("lessonspart3b_modo_preparo_expr_expr_expr"),
                    tx("lessonspart3b_quando_terminar_esta_etapa_anote"),
                    tx("lessonspart3b_fornecedor_que_entrega_ingrediente_por"),
                    tx("lessonspart3b_robo_confeiteiro_que_segue_tabela"),
                    tx("lessonspart3b_alarme_fumaca_yyerror"),
                ),
            ),
            Say(LEXI, tx("lessonspart3b_eu_sou_fornecedor_corto_12")),
            Code(P3_BISON_CALC, tx("lessonspart3b_ficha_pedido_mais_famosa_calculadora")),
            Predict(
                tx("lessonspart3b_robo_vai_montar_2_3"),
                listOf(tx("lessonspart3b_ele_recusa_receita"), tx("lessonspart3b_ele_avisa_conflitos_por_padrao"), tx("lessonspart3b_ele_calcula_sempre_esquerda_para")),
                1,
                tx("lessonspart3b_gramatica_ambigua_entao_ha_conflitos"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "2+3*4", caption = tx("lessonspart3b_com_left_left_na_ficha")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart3b_yacc_quer_dizer_yet_another")),
            Callout(REAL_WORLD, tx("lessonspart3b_postgresql_gram_y_bash_parse")),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.VISUAL, tx("lessonspart3b_ligando_desligando_precedencia"),
        listOf(
            Say(PROF, tx("lessonspart3b_experimento_laboratorio_mesma_gramatica_ambi")),
            Visual(VisualKind.SLR_TABLE, grammar = P3_AMBIG, caption = tx("lessonspart3b_por_baixo_capo_gramatica_ambigua")),
            Visual(VisualKind.BISON, grammar = P3_BISON_NOPREC, input = "2*3+4", caption = tx("lessonspart3b_sem_precedencia_4_conflitos_bison")),
            Say(BYTE, tx("lessonspart3b_catorze_minha_calculadora_bolso_discorda")),
            Say(PILHA, tx("lessonspart3b_eu_tinha_2_3_vi")),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "2*3+4", caption = tx("lessonspart3b_com_left_na_celula_estado")),
            Steps(
                tx("lessonspart3b_como_bison_decide_cada_conflito"),
                listOf(
                    tx("lessonspart3b_precedencia_regra_ultimo_terminal_dela"),
                    tx("lessonspart3b_precedencia_token_lookahead_linha_onde"),
                    "Token mais forte → **shift**; regra mais forte → **reduce**",
                    "Empate: %left → reduce; %right → shift; %nonassoc → erro de sintaxe",
                ),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_RIGHT_MINUS, input = "8-3-2", caption = tx("lessonspart3b_right_por_engano_8_3")),
            Predict(
                tx("lessonspart3b_com_right_para_potencia_quanto"),
                listOf(tx("lessonspart3b_64_ou_seja_2_3"), tx("lessonspart3b_512_ou_seja_2_3"), tx("comum_erro_sintaxe")),
                1,
                tx("lessonspart3b_right_no_empate_shift_pilha"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_POW, input = "2^3^2", caption = tx("lessonspart3b_confira_512_linha_right_vem")),
            Visual(VisualKind.BISON, grammar = P3_BISON_NONASSOC, input = "1+1<3", caption = tx("lessonspart3b_nonassoc_1_1_3_funciona")),
            Callout(BRAIN_POWER, tx("lessonspart3b_precedencia_nao_muda_gramatica_ela")),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.WORKED, tx("lessonspart3b_calculadora_completa_bison_flex_gcc"),
        listOf(
            Say(PROF, tx("lessonspart3b_vamos_montar_calculadora_verdade_jeito")),
            Code(P3_REAL_CALC_Y, tx("lessonspart3b_calc_y_declaracoes_regras_codigo")),
            Steps(
                tx("lessonspart3b_lendo_arquivo_por_partes"),
                listOf(
                    tx("lessonspart3b_codigo_c_copiado_literalmente_para"),
                    tx("lessonspart3b_token_num_token_com_nome"),
                    tx("lessonspart3b_left_right_precedencia_linhas_mais"),
                    tx("lessonspart3b_comecam_regras_valor_lado_esquerdo"),
                    tx("lessonspart3b_comeca_codigo_c_usuario_yylex"),
                    tx("lessonspart3b_yylex_devolve_codigo_token_coloca"),
                ),
            ),
            Code(P3_REAL_CALC_L, tx("lessonspart3b_calc_l_mesmo_lexico_gerado")),
            Code(tx("lessonspart3b_bison_d_calc_y_gera"), tx("lessonspart3b_compilando_rodando")),
            Say(LEXI, tx("lessonspart3b_calc_tab_h_contrato_entre")),
            Steps(
                tx("lessonspart3b_rastreando_2_3_4_so"),
                listOf(
                    tx("lessonspart3b_pilha_expr_expr_proximo_token"),
                    tx("lessonspart3b_pilha_expr_expr_expr_fim"),
                    tx("lessonspart3b_pilha_expr_expr_reduz_2"),
                ),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "-5+2", caption = tx("lessonspart3b_no_jogo_mesma_calculadora_sem")),
            Say(BYTE, tx("lessonspart3b_minha_vez")),
            Reveal(tx("lessonspart3b_com_left_quanto_vale_8"), "%left: empate → reduce → (8-3)-2 = 3. %right: empate → shift → 8-(3-2) = 7."),
            Reveal(tx("lessonspart3b_na_regra_expr_prec_uminus"), tx("lessonspart3b_sem_ele_regra_herdaria_precedencia")),
            Reveal(tx("lessonspart3b_no_mini_bison_jogo_que"), tx("lessonspart3b_prologo_terceira_secao_com_codigo")),
            Callout(EXAM_TIP, tx("lessonspart3b_se_prova_pede_escreva_especificacao")),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.SOCRATIC, tx("lessonspart3b_por_que_deixar_gramatica_ambigua"),
        listOf(
            Say(PROF, tx("lessonspart3b_pergunta_provocadora_se_ambiguidade_ruim")),
            Reveal(tx("lessonspart3b_qual_alternativa_sem_ambiguidade"), tx("lessonspart3b_camadas_expr_expr_term_term")),
            Visual(VisualKind.BISON, grammar = P3_BISON_LAYERS, input = "2+3*4", caption = tx("lessonspart3b_versao_em_camadas_zero_conflitos")),
            Reveal(tx("lessonspart3b_qual_vantagem_versao_ambigua_left"), tx("lessonspart3b_gramatica_curta_legivel_com_menos")),
            Reveal(tx("lessonspart3b_como_bison_sabe_precedencia_regra"), tx("lessonspart3b_pelo_ultimo_terminal_lado_direito")),
            Reveal(tx("lessonspart3b_lookahead"), tx("lessonspart3b_pela_linha_left_right_nonassoc")),
            Predict(
                tx("lessonspart3b_conflito_empilhar_reduzir_em_que"),
                listOf(tx("lessonspart3b_para_compilacao_com_erro"), tx("lessonspart3b_escolhe_shift_emite_aviso_conflito"), tx("lessonspart3b_escolhe_reduce")),
                1,
                tx("lessonspart3b_padrao_yacc_bison_shift_com"),
            ),
            Reveal(tx("lessonspart3b_esse_padrao_shift_bom_para"), tx("lessonspart3b_shift_no_else_faz_else")),
            Visual(VisualKind.PARSE_TREE, grammar = P3_IFELSE, input = "if c then if c then x else x", caption = tx("lessonspart3b_duas_arvores_possiveis_quem_else")),
            Reveal(tx("lessonspart3b_por_que_conflitos_reduzir_reduzir"), tx("lessonspart3b_porque_pegar_primeira_regra_quase")),
            Callout(BRAIN_POWER, tx("lessonspart3b_voce_pode_ligar_relatorios_bison")),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.FORMAL, tx("lessonspart3b_estrutura_especificacao_resolucao_conflitos"),
        listOf(
            Code(tx("lessonspart3b_declaracoes_regras_traducao_funcoes_auxiliar"), tx("lessonspart3b_formato_geral_arquivo_y")),
            Steps(
                tx("comum_declaracoes"),
                listOf(
                    tx("lessonspart3b_codigo_c_copiado_para_inicio"),
                    "%token NOME — declara terminais com nome",
                    tx("lessonspart3b_left_right_nonassoc_t1_t2"),
                    tx("lessonspart3b_start_simbolo_inicial_padrao_lado"),
                    tx("lessonspart3b_union_type_campo_tipos_dos"),
                ),
            ),
            Text(tx("lessonspart3b_regra_cabeca_corpo1_acao1_corpo2")),
            Code(
                tx("lessonspart3b_conflito_em_action_entre_shift"),
                tx("lessonspart3b_regras_desambiguacao_yacc_bison"),
            ),
            Steps(
                tx("lessonspart3b_interface_com_resto_programa"),
                listOf(
                    "int yyparse(void) — o analisador LALR(1); retorna 0 em sucesso e 1 em erro de sintaxe",
                    tx("lessonspart3b_int_yylex_void_chamado_pelo"),
                    tx("lessonspart3b_void_yyerror_const_char_chamado"),
                    tx("lessonspart3b_bison_d_gera_cabecalho_tab"),
                ),
            ),
            Predict(
                tx("lessonspart3b_declaracoes_left_abaixo_left_em"),
                listOf(tx("lessonspart3b_empilha_shift"), tx("lessonspart3b_reduz_reduce"), tx("lessonspart3b_erro")),
                1,
                tx("lessonspart3b_mesma_precedencia_mesma_linha_left"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "(2+3)*4", caption = tx("lessonspart3b_especificacao_no_subconjunto_que_mini")),
            Callout(EXAM_TIP, tx("lessonspart3b_questao_classica_quantos_conflitos_como")),
            Callout(WATCH_OUT, tx("lessonspart3b_ordem_das_linhas_importa_ao")),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.MNEMONIC, tx("lessonspart3b_ganchos_familia_yy"),
        listOf(
            Say(NARRADOR, tx("lessonspart3b_palacio_memoria_oficina_yacc_bison")),
            Callout(MNEMONIC, tx("lessonspart3b_d_r_c_declara_regra")),
            Callout(MNEMONIC, tx("lessonspart3b_precedencia_ultimo_fila_vip_linha")),
            Callout(MNEMONIC, tx("lessonspart3b_na_duvida_empilha_no_empate")),
            Callout(MNEMONIC, tx("lessonspart3b_left_esquerda_reduz_right_direita")),
            Code(
                tx("lessonspart3b_yylex_le_proximo_token_lexi"),
                tx("lessonspart3b_familia_yy_sobrenome_todo_mundo"),
            ),
            Callout(MNEMONIC, tx("lessonspart3b_pai_1_2_3_sao")),
            Predict(
                "Na regra expr : '(' expr ')' { \$\$ = ???; }, o que vai no lugar de ???",
                listOf("\$1", "\$2", "\$3"),
                1,
                tx("lessonspart3b_1_expr_2_3_valor"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "8-3-2", caption = tx("lessonspart3b_bisao_com_left_8_3")),
            Reveal(tx("lessonspart3b_recite_como_bison_resolve_empate"), "%left → reduce, %right → shift, %nonassoc → erro."),
            Callout(FIRESIDE, tx("lessonspart3b_bison_voce_escreve_20_linhas")),
        ),
    ),
)

// =====================================================================
// ATTRIBUTES_TYPES — Atributos e verificação de tipos
// =====================================================================

internal val attrLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.STORY, tx("lessonspart3b_notas_que_sobem_sobrenomes_que"),
        listOf(
            Say(NARRADOR, tx("lessonspart3b_cidade_semantica_arvore_sintatica_ja")),
            Say(BYTE, tx("lessonspart3b_como_assim_arvore_x_2")),
            Say(PROF, tx("lessonspart3b_ela_esta_certa_na_forma")),
            Text(tx("lessonspart3b_pense_na_sua_media_semestre")),
            Text(tx("lessonspart3b_agora_pense_no_sobrenome_voce")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3b_nota_final_calculada_das_parciais"),
                    tx("lessonspart3b_sobrenome_vindo_dos_pais_herdado"),
                    tx("lessonspart3b_regulamento_como_calcular_media_regra"),
                    tx("lessonspart3b_gramatica_atributos_regras_definicao_dirigid"),
                ),
            ),
            Text(tx("lessonspart3b_fiscal_balanca_feira_verificador_tipos")),
            Predict(
                tx("lessonspart3b_int_x_float_qual_tipo"),
                listOf("int", tx("lessonspart3b_float_com_coercao_int_para"), tx("lessonspart3b_erro_tipo")),
                1,
                tx("lessonspart3b_x_2_float_2_convertido"),
            ),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_x_2"), caption = tx("lessonspart3b_tipos_sobem_das_folhas_ate")),
            Reveal(tx("lessonspart3b_em_int_b_c_tipo"), tx("lessonspart3b_herdado_ele_desce_no_tipo")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart3b_atributo_so_variavel_no_no")),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.VISUAL, tx("lessonspart3b_assistindo_tipos_subirem_pela_arvore"),
        listOf(
            Say(PROF, tx("lessonspart3b_vamos_ver_verificador_tipos_trabalhando")),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_b_c_2"), caption = tx("lessonspart3b_tudo_int_folhas_consultam_tabela")),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_x_2"), caption = tx("lessonspart3b_aqui_aparece_coercao_no_int")),
            Say(BYTE, tx("lessonspart3b_entao_tipo_pai_so_maior")),
            Say(PROF, tx("lessonspart3b_nas_aritmeticas_sim_int_float")),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_b_ok"), caption = tx("lessonspart3b_comparacao_sempre_produz_bool_exige")),
            Predict(
                tx("lessonspart3b_ok_1_com_ok_sendo"),
                listOf("int (true vale 1)", tx("lessonspart3b_erro_tipo"), "bool"),
                1,
                tx("lessonspart3b_nesta_linguagem_como_em_java"),
            ),
            Visual(VisualKind.ATTR_TREE, input = "ok + 1", caption = tx("lessonspart3b_no_marca_erro_bool_nao")),
            Visual(VisualKind.PARSE_TREE, grammar = P3_DECL, input = "int id , id ;", caption = tx("lessonspart3b_atributo_herdado_t_tipo_int")),
            Steps(
                tx("lessonspart3b_que_voce_viu"),
                listOf(
                    tx("lessonspart3b_folhas_tipo_vem_literal_ou"),
                    tx("lessonspart3b_nos_internos_tipo_calculado_dos"),
                    tx("lessonspart3b_ordem_avaliacao_pos_ordem_como"),
                    tx("lessonspart3b_na_declaracao_tipo_desce_pela"),
                ),
            ),
            Callout(BRAIN_POWER, tx("lessonspart3b_analisador_lr_calcula_atributos_sintetizados")),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.WORKED, tx("lessonspart3b_arvore_anotada_com_valor_com"),
        listOf(
            Say(PROF, tx("lessonspart3b_primeiro_valores_sdd_classica_calculadora")),
            Code(
                """E → E1 + T    E.val = E1.val + T.val
E → T         E.val = T.val
T → T1 * F    T.val = T1.val * F.val
T → F         T.val = F.val
F → ( E )     F.val = E.val
F → num       F.val = num.lexval""",
                tx("lessonspart3b_sdd_atribuida_so_atributos_sintetizados"),
            ),
            Steps(
                tx("lessonspart3b_anotando_3_5_4_baixo"),
                listOf(
                    tx("lessonspart3b_folhas_num_lexval_3_5"),
                    "T → F: T.val = 3",
                    "T → T1 * F: T.val = 3 * 5 = 15",
                    "E → T: E.val = 15",
                    "E → E1 + T (com T.val = 4): E.val = 15 + 4 = **19**",
                ),
            ),
            Say(PROF, tx("lessonspart3b_agora_tipos_com_coercao_regra")),
            Steps(
                "Tipando a + x * 2 (a: int, x: float)",
                listOf(
                    tx("lessonspart3b_int_tabela_simbolos_x_float"),
                    tx("lessonspart3b_x_2_float_int_float"),
                    "a + (x * 2): int + float → **float**, gera inttofloat(a)",
                ),
            ),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_x_2"), caption = tx("lessonspart3b_confira_raiz_float_com_coercao")),
            Say(BYTE, tx("lessonspart3b_deixa_comigo_agora")),
            Reveal(tx("lessonspart3b_tipo_b_c_com_b"), tx("lessonspart3b_int_em_todos_nos_internos")),
            Reveal(tx("lessonspart3b_tipo_x_2_3_4"), tx("lessonspart3b_x_2_float_3_4")),
            Reveal(tx("lessonspart3b_tipo_b_ok"), "a < b → bool; bool && bool (ok) → bool."),
            Visual(VisualKind.ATTR_TREE, input = "x * 2 + 3 * 4", caption = tx("lessonspart3b_confira_segunda_resposta")),
            Callout(EXAM_TIP, tx("lessonspart3b_em_desenhe_arvore_anotada_escreva")),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.SOCRATIC, tx("lessonspart3b_onde_vem_informacao_cada_no"),
        listOf(
            Say(PROF, tx("lessonspart3b_pergunta_guia_hoje_para_calcular")),
            Reveal(tx("lessonspart3b_valor_em_e1_t_depende"), tx("lessonspart3b_e1_val_t_val_dos")),
            Reveal(tx("lessonspart3b_se_todos_atributos_sao_sintetizados"), tx("lessonspart3b_pos_ordem_baixo_para_cima")),
            Reveal(tx("lessonspart3b_em_d_t_l_com"), tx("lessonspart3b_pelo_atributo_herdado_l_in")),
            Say(BYTE, tx("lessonspart3b_entao_herdado_pode_vir_qualquer")),
            Reveal(tx("lessonspart3b_se_atributo_herdado_pudesse_depender"), tx("lessonspart3b_nao_irmao_direita_ainda_nao")),
            Predict(
                tx("lessonspart3b_numa_sdd_l_atribuida_atributo"),
                listOf(
                    tx("lessonspart3b_qualquer_atributo_qualquer_filho"),
                    tx("lessonspart3b_atributos_herdados_atributos_x1_xi"),
                    tx("lessonspart3b_apenas_atributos_sintetizados"),
                ),
                1,
                tx("lessonspart3b_l_left_so_pai_herdados"),
            ),
            Reveal(tx("lessonspart3b_toda_sdd_atribuida_l_atribuida"), tx("lessonspart3b_sim_ela_nao_tem_herdados")),
            Reveal(tx("lessonspart3b_por_que_verificador_tipos_precisa"), tx("lessonspart3b_porque_nas_folhas_so_ha")),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_b_c"), caption = tx("lessonspart3b_pos_ordem_em_acao_b")),
            Callout(BRAIN_POWER, tx("lessonspart3b_se_grafo_dependencias_entre_atributos")),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.FORMAL, tx("lessonspart3b_sdd_atributos_sistema_tipos"),
        listOf(
            Text(tx("lessonspart3b_definicao_dirigida_pela_sintaxe_sdd")),
            Steps(
                tx("lessonspart3b_tipos_atributo_para_nao_terminal"),
                listOf(
                    tx("lessonspart3b_sintetizado_definido_por_regra_producao"),
                    tx("lessonspart3b_herdado_definido_por_regra_producao"),
                    tx("lessonspart3b_terminais_so_tem_atributos_sintetizados"),
                ),
            ),
            Steps(
                tx("lessonspart3b_classes_sdd"),
                listOf(
                    tx("lessonspart3b_atribuida_so_atributos_sintetizados_avaliaca"),
                    tx("lessonspart3b_l_atribuida_em_x1_xn"),
                    tx("lessonspart3b_esquema_traducao_sdt_gramatica_com"),
                ),
            ),
            Code(
                """D → T L ;        L.in = T.tipo
T → int          T.tipo = int
T → float        T.tipo = float
L → L1 , id      L1.in = L.in ;  addType(id.entrada, L.in)
L → id           addType(id.entrada, L.in)""",
                tx("lessonspart3b_sdd_l_atribuida_classica_declaracoes"),
            ),
            Code(
                """E → E1 op E2   (op aritmético):
  se E1.tipo = bool ou E2.tipo = bool: erro de tipo
  E.tipo := max(E1.tipo, E2.tipo)      // int < float
  E.addr1 := widen(E1.addr, E1.tipo, E.tipo)   // insere inttofloat se preciso
  E.addr2 := widen(E2.addr, E2.tipo, E.tipo)
E → E1 relop E2:  numéricos → E.tipo := bool
E → E1 && E2:     ambos bool → E.tipo := bool""",
                tx("lessonspart3b_verificacao_tipos_com_coercao_alargamento"),
            ),
            Predict(
                tx("lessonspart3b_coercao_int_float_inserida_pelo"),
                listOf(tx("lessonspart3b_conversao_explicita_cast"), tx("lessonspart3b_coercao_implicita_alargamento"), tx("lessonspart3b_estreitamento")),
                1,
                tx("lessonspart3b_coercao_conversao_implicita_feita_pelo"),
            ),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_x_2"), caption = tx("lessonspart3b_instancia_regra_acima_max_int")),
            Callout(EXAM_TIP, tx("lessonspart3b_ao_classificar_sdd_verifique_cada")),
            Callout(WATCH_OUT, tx("lessonspart3b_verificacao_tipos_estatica_em_tempo")),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.MNEMONIC, tx("lessonspart3b_sobe_h_desce"),
        listOf(
            Say(NARRADOR, tx("lessonspart3b_palacio_memoria_cidade_semantica_predio")),
            Callout(MNEMONIC, tx("lessonspart3b_h_intetizado_obe_dos_filhos")),
            Callout(MNEMONIC, tx("lessonspart3b_atribuida_so_sintetizado_l_atribuida")),
            Callout(MNEMONIC, tx("lessonspart3b_coercao_copo_pequeno_no_copo")),
            Code(
                tx("lessonspart3b_int_int_int_int_float"),
                tx("lessonspart3b_tabela_bolso_dos_tipos"),
            ),
            Say(BYTE, tx("lessonspart3b_tipo_que_nem_fofoca_comeca")),
            Predict(
                "Bison: \$\$ = \$1 + \$3; calcula um atributo...",
                listOf(tx("lessonspart3b_herdado"), tx("comum_sintetizado"), tx("lessonspart3b_global")),
                1,
                tx("lessonspart3b_pai_calculado_dos_filhos_1"),
            ),
            Visual(VisualKind.ATTR_TREE, input = "ok + 1", caption = tx("lessonspart3b_erro_tambem_sobe_bool_numa")),
            Reveal(tx("lessonspart3b_diga_em_frase_sintetizado_vs"), tx("lessonspart3b_sintetizado_calculado_partir_dos_filhos")),
            Callout(FIRESIDE, tx("lessonspart3b_sintetizado_eu_sou_pratico_espero")),
        ),
    ),
)
