package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Uma frase para julgar: é verdade ou é conversa do Bug? */
data class Afirma(val texto: String, val verdade: Boolean, val porque: String)

/**
 * Cada erro comum de prova é um Bug com nome. Para colar a figurinha, a pessoa julga as frases dele.
 * [mentira] é o que o Bug diz; [verdade] desmonta o erro. Nomear o erro funciona: refutação tem g = 0,41.
 */
data class Especie(val id: String, val nome: String, val skill: Skill, val mentira: String, val verdade: String, val testes: List<Afirma>)

private fun v(t: String, p: String = tx("bugs_isso_mesmo")) = Afirma(t, true, p)
private fun f(t: String, p: String) = Afirma(t, false, p)

object Album {
    val especies = listOf(
        Especie("fase-unica", tx("bugs_bug_fase_unica"), Skill.PHASES, tx("bugs_compilador_faz_tudo_vez_so"), tx("bugs_sao_fases_em_sequencia_saida"), listOf(
            v(tx("bugs_analise_lexica_entrega_tokens_para")),
            f(tx("bugs_erro_tipo_pego_na_fase"), tx("bugs_tipo_assunto_semantica")),
            f(tx("bugs_otimizacao_vem_antes_analise_sintatica"), tx("bugs_so_para_otimizar_depois_entender")),
        )),
        Especie("erro-trocado", tx("bugs_bug_erro_trocado"), Skill.PHASES, tx("bugs_todo_erro_erro_sintaxe"), tx("bugs_cada_fase_pega_tipo_erro"), listOf(
            v(tx("bugs_variavel_nao_declarada_erro_semantico")),
            v(tx("bugs_faltar_ponto_virgula_erro_sintatico")),
            f(tx("bugs_perdido_no_codigo_erro_semantico"), tx("bugs_scanner_pega_nenhum_token_comeca")),
        )),
        Especie("lexema-gemeo", tx("bugs_bug_lexema_gemeo"), Skill.TOKENS, tx("bugs_token_lexema_sao_mesma_coisa"), tx("bugs_lexema_texto_token_classe_id"), listOf(
            v(tx("bugs_em_conta_10_conta_lexema")),
            v(tx("bugs_dois_lexemas_diferentes_podem_ter")),
            f(tx("bugs_cada_lexema_tem_token_so"), tx("bugs_x_y_tem_mesmo_token")),
        )),
        Especie("corte-curto", tx("bugs_bug_corte_curto"), Skill.TOKENS, tx("bugs_sao_dois_tokens"), tx("bugs_scanner_pega_maior_pedaco_que"), listOf(
            v(tx("bugs_ifa_identificador")),
            f(tx("bugs_em_x_10_scanner_fecha"), tx("bugs_ele_tenta_esticar_fecha")),
            f(tx("bugs_ifa_vira_token_if_seguido"), tx("bugs_maior_casamento_fica_ifa_inteiro")),
        )),
        Especie("palavra-comum", tx("bugs_bug_palavra_comum"), Skill.LEX_FLEX, tx("bugs_if_nome_como_outro_qualquer"), tx("bugs_no_empate_tamanho_ganha_regra"), listOf(
            v(tx("bugs_no_flex_regra_que_vem")),
            v(tx("bugs_maior_casamento_decide_antes_ordem")),
            f(tx("bugs_yytext_guarda_classe_token"), tx("bugs_yytext_guarda_lexema")),
        )),
        Especie("estrela-cheia", tx("bugs_bug_estrela_cheia"), Skill.REGEX_DFA, tx("bugs_exige_pelo_menos"), tx("bugs_estrela_aceita_zero_ou_mais"), listOf(
            v(tx("bugs_exige_pelo_menos_2")),
            v(tx("bugs_cadeia_vazia_casa_com")),
            f(tx("bugs_ab_aceita_aba"), tx("bugs_so_repeticoes_inteiras_ab")),
        )),
        Especie("regex-conta", tx("bugs_bug_regex_que_conta"), Skill.REGEX_DFA, tx("bugs_regex_confere_parenteses_balanceados"), tx("bugs_automato_finito_nao_conta_sem"), listOf(
            v(tx("bugs_n_b_n_nao_linguagem")),
            v(tx("bugs_gramatica_livre_contexto_confere_parenteses")),
            f(tx("bugs_afd_tem_memoria_infinita"), tx("bugs_ele_so_lembra_estado_atual")),
        )),
        Especie("fecho-esquecido", tx("bugs_bug_fecho_esquecido"), Skill.NFA_TO_DFA, tx("bugs_inicio_afd_so_inicio_afn"), tx("bugs_fecho_dele_tudo_que_se"), listOf(
            v(tx("bugs_cada_estado_afd_conjunto_estados")),
            v(tx("bugs_conjunto_final_se_tiver_estado")),
            f(tx("bugs_depois_ler_letra_nao_preciso"), tx("bugs_sempre_feche_depois_move")),
        )),
        Especie("gemeos", tx("bugs_bug_dos_gemeos"), Skill.DFA_MINIMIZATION, tx("bugs_estados_com_mesmo_numero_setas"), tx("bugs_so_sao_gemeos_se_nenhuma"), listOf(
            v(tx("bugs_estado_final_nao_final_nunca")),
            v(tx("bugs_tabela_pares_comeca_separando_finais")),
            f(tx("bugs_automato_minimo_aceita_outra_linguagem"), tx("bugs_linguagem_mesma_com_menos_estados")),
        )),
        Especie("terminal-trocado", tx("bugs_bug_terminal_trocado"), Skill.CFG_DERIVATION, tx("bugs_terminal_que_ainda_vai_virar"), tx("bugs_nao_terminal_que_se_troca"), listOf(
            v(tx("bugs_folhas_arvore_frase_sao_terminais")),
            v(tx("bugs_frase_so_tem_terminais")),
            f(tx("bugs_forma_sentencial_nunca_tem_nao"), tx("bugs_ela_pode_ter_dois_tipos")),
        )),
        Especie("else-solto", tx("bugs_bug_else_solto"), Skill.AMBIGUITY, tx("bugs_else_if_fora"), tx("bugs_else_fica_com_if_mais"), listOf(
            v(tx("bugs_if_then_if_b_then")),
            v(tx("bugs_bison_resolve_esse_caso_empilhando")),
            f(tx("bugs_reescrever_gramatica_nunca_resolve_else"), tx("bugs_para_reescrever_separando_comandos_fechados")),
        )),
        Especie("duas-contas", tx("bugs_bug_das_duas_contas"), Skill.AMBIGUITY, tx("bugs_ambiguidade_so_detalhe_desenho"), tx("bugs_duas_arvores_dao_dois_resultados"), listOf(
            v(tx("bugs_gramatica_ambigua_tem_frase_com")),
            v(tx("bugs_duas_derivacoes_pela_esquerda_diferentes")),
            f(tx("bugs_toda_gramatica_expressoes_ambigua"), tx("bugs_com_niveis_t_f_ela")),
        )),
        Especie("laco-eterno", tx("bugs_bug_laco_eterno"), Skill.LEFT_RECURSION, tx("bugs_recursao_esquerda_so_deixa_parser"), tx("bugs_ela_trava_parser_descendente_ele"), listOf(
            v(tx("bugs_parser_lr_aceita_recursao_esquerda")),
            v("E -> E + T | T vira E -> T X e X -> + T X | ε."),
            f(tx("bugs_recursao_direita_trava_ll_1"), tx("bugs_direita_ele_le_token_antes")),
        )),
        Especie("comeco-igual", tx("bugs_bug_comeco_igual"), Skill.LEFT_FACTORING, tx("bugs_fatorar_muda_linguagem"), tx("bugs_linguagem_mesma_so_adia_escolha"), listOf(
            v("A -> a b | a c vira A -> a X e X -> b | c."),
            v(tx("bugs_fatorar_acrescenta_nivel_na_arvore")),
            f(tx("bugs_fatorar_resolve_qualquer_conflito_ll"), tx("bugs_conflito_com_follow_por_exemplo")),
        )),
        Especie("primeiro-so", tx("bugs_bug_primeiro_so"), Skill.FIRST, tx("bugs_first_so_primeiro_simbolo_regra"), tx("bugs_se_ele_nao_terminal_vale"), listOf(
            v(tx("bugs_se_b_some_em_b")),
            v(tx("bugs_entra_em_first_se_pode")),
            f(tx("bugs_first_terminal_vazio"), tx("bugs_first_terminal_ele_mesmo")),
        )),
        Especie("follow-vazio", tx("bugs_bug_follow_vazio"), Skill.FOLLOW, tx("bugs_pode_entrar_no_follow"), tx("bugs_follow_nunca_tem_ele_guarda"), listOf(
            v(tx("bugs_entra_no_follow_simbolo_inicial")),
            v(tx("bugs_follow_recebe_first_que_vem")),
            f(tx("bugs_se_fecha_regra_b_follow"), tx("bugs_ao_contrario_follow_b_entra")),
        )),
        Especie("casa-dupla", tx("bugs_bug_casa_dupla"), Skill.LL1_TABLE, tx("bugs_duas_regras_na_mesma_casa"), tx("bugs_casa_com_duas_regras_conflito"), listOf(
            v(tx("bugs_regra_entra_na_coluna_cada")),
            v(tx("bugs_se_lado_direito_pode_sumir")),
            f(tx("bugs_toda_gramatica_sem_recursao_esquerda"), tx("solta_bugs_1")),
        )),
        Especie("pilha-invertida", tx("bugs_bug_pilha_invertida"), Skill.LL1_PARSING, tx("bugs_lado_direito_entra_na_pilha"), tx("bugs_entra_tras_para_frente_para"), listOf(
            v(tx("bugs_terminal_no_topo_precisa_casar")),
            v(tx("bugs_casa_vazia_na_tabela_significa")),
            f(tx("bugs_entrada_acabou_com_pilha_cheia"), tx("bugs_so_aceita_com_pilha_entrada")),
        )),
        Especie("token-avancado", tx("bugs_bug_token_avancado"), Skill.RECURSIVE_DESCENT, tx("bugs_toda_funcao_le_token_antes"), tx("bugs_funcao_olha_token_atual_so"), listOf(
            v(tx("comum_cada_nao_terminal_vira_funcao")),
            v(tx("bugs_casatoken_confere_avanca_entrada")),
            f(tx("bugs_descida_recursiva_roda_t_como"), tx("bugs_ela_entraria_em_recursao_infinita")),
        )),
        Especie("para-tudo", tx("bugs_bug_para_tudo"), Skill.ERROR_RECOVERY, tx("bugs_no_primeiro_erro_compilador_tem"), tx("bugs_ele_se_recupera_segue_para"), listOf(
            v(tx("bugs_modo_panico_descarta_tokens_ate")),
            v(tx("bugs_ponto_virgula_costuma_ser_bom")),
            f(tx("bugs_recuperar_erro_conserta_programa"), tx("bugs_so_permite_achar_proximos_erros")),
        )),
        Especie("reduz-cedo", tx("bugs_bug_reduz_cedo"), Skill.SHIFT_REDUCE, tx("bugs_viu_lado_direito_na_pilha"), tx("bugs_depende_quem_vem_com_t"), listOf(
            v(tx("bugs_reduzir_troca_topo_pelo_lado")),
            v(tx("bugs_shift_reduce_faz_derivacao_mais")),
            f(tx("bugs_handle_qualquer_pedaco_igual_lado"), tx("bugs_handle_pedaco_certo_reduzir_agora")),
        )),
        Especie("fecho-curto", tx("bugs_bug_fecho_curto"), Skill.LR0_ITEMS, tx("bugs_closure_so_copia_item"), tx("bugs_se_ponto_esta_antes_nao"), listOf(
            v(tx("bugs_ponto_marca_ate_onde_regra")),
            v(tx("bugs_item_com_ponto_no_fim")),
            f(tx("bugs_todo_estado_lr_0_tem"), tx("bugs_fecho_costuma_puxar_varios")),
        )),
        Especie("follow-cego", tx("bugs_bug_follow_cego"), Skill.SLR_TABLE, tx("bugs_slr_reduz_em_qualquer_token"), tx("bugs_so_nos_tokens_follow_lado"), listOf(
            v(tx("bugs_lr_0_reduz_sem_olhar")),
            v(tx("bugs_conflito_shift_reduce_duvida_entre")),
            f(tx("bugs_se_slr_tem_conflito_gramatica"), "S -> L = R mostra o contrário."),
        )),
        Especie("estado-repetido", tx("bugs_bug_estado_repetido"), Skill.LALR, tx("bugs_lalr_tem_estados_lr_1"), tx("bugs_lalr_funde_estados_mesmo_miolo"), listOf(
            v(tx("bugs_juntar_estados_pode_criar_conflito")),
            v(tx("bugs_juntar_estados_nao_cria_shift")),
            f(tx("bugs_slr_lalr_usam_mesmos_simbolos"), tx("bugs_slr_usa_follow_lalr_mais")),
        )),
        Especie("precedencia-trocada", tx("bugs_bug_precedencia_trocada"), Skill.YACC_BISON, tx("bugs_primeiro_left_mais_forte"), tx("comum_quanto_mais_embaixo_no_arquivo"), listOf(
            v("%left faz 8 - 3 - 2 agrupar pela esquerda."),
            v(tx("bugs_sem_declaracao_em_shift_reduce")),
            f(tx("bugs_em_reduce_reduce_bison_escolhe"), tx("bugs_ele_escolhe_primeira_arquivo")),
        )),
        Especie("herdado", tx("bugs_bug_herdado"), Skill.ATTRIBUTES_TYPES, tx("bugs_todo_atributo_sobe_na_arvore"), tx("bugs_sintetizado_sobe_herdado_desce_ou"), listOf(
            v(tx("bugs_atribuida_so_usa_sintetizados")),
            v(tx("bugs_tipo_expressao_costuma_ser_sintetizado")),
            f(tx("bugs_l_atribuida_nao_pode_ter"), tx("bugs_pode_vindos_pai_ou_esquerda")),
        )),
        Especie("inteiro-esticado", tx("bugs_bug_inteiro_esticado"), Skill.ATTRIBUTES_TYPES, tx("bugs_int_float_erro"), tx("bugs_compilador_converte_int_para_float"), listOf(
            v(tx("bugs_int_float_resulta_em_float")),
            v(tx("bugs_coercao_conversao_automatica")),
            f(tx("bugs_float_cabe_em_int_sem"), tx("bugs_parte_depois_virgula_se_perde")),
        )),
        Especie("escopo-vazado", tx("bugs_bug_escopo_vazado"), Skill.SCOPE_SYMBOLS, tx("bugs_variavel_dentro_bloco_vale_fora"), tx("bugs_cada_bloco_abre_escopo_ao"), listOf(
            v(tx("bugs_busca_comeca_pelo_escopo_mais")),
            v(tx("bugs_declarar_x_dentro_esconde_x")),
            f(tx("bugs_usar_variavel_nao_declarada_erro"), tx("bugs_erro_semantico")),
        )),
        Especie("arvore-gorda", tx("bugs_bug_arvore_gorda"), Skill.AST_BUILD, tx("bugs_ast_arvore_derivacao_inteira"), tx("bugs_ast_guarda_so_que_importa"), listOf(
            v(tx("bugs_b_nao_tem_no_parentese")),
            v(tx("bugs_forma_ast_ja_mostra_precedencia")),
            f(tx("bugs_ast_tem_mais_nos_que"), tx("bugs_ela_mais_enxuta")),
        )),
        Especie("conta-longa", tx("bugs_bug_conta_longa"), Skill.TAC, tx("bugs_linha_tac_tem_varias_contas"), tx("bugs_no_maximo_conta_por_linha"), listOf(
            v(tx("bugs_b_c_vira_duas_linhas")),
            v(tx("bugs_t1_b_c_vem_antes")),
            f(tx("bugs_temporario_variavel_que_pessoa_declarou"), tx("bugs_quem_cria_compilador")),
        )),
        Especie("otimizacao-mentirosa", tx("bugs_bug_otimizacao_mentirosa"), Skill.OPTIMIZATION, tx("bugs_otimizar_pode_mudar_resultado"), tx("bugs_otimizacao_precisa_manter_que_programa"), listOf(
            v(tx("bugs_2_3_pode_virar_6")),
            v(tx("bugs_codigo_que_nunca_roda_pode")),
            f(tx("bugs_x_x_sempre_pode_virar"), tx("bugs_com_x_zero_programa_dava")),
        )),
        Especie("recursao-unica", tx("bugs_bug_recursao_unica"), Skill.RUNTIME, tx("bugs_funcao_recursiva_tem_registro_so"), tx("bugs_cada_chamada_empilha_registro_novo"), listOf(
            v(tx("bugs_registro_guarda_parametros_locais_endereco")),
            v(tx("bugs_ao_retornar_registro_sai_pilha")),
            f(tx("bugs_variavel_local_sobrevive_depois_retorno"), tx("bugs_ela_sai_junto_com_registro")),
        )),
        Especie("load-esquecido", tx("bugs_bug_load_esquecido"), Skill.CODEGEN_MIPS, tx("bugs_mips_soma_direto_memoria"), tx("bugs_mips_so_opera_em_registradores"), listOf(
            v(tx("bugs_lw_copia_memoria_para_registrador")),
            v(tx("bugs_sw_guarda_registrador_na_memoria")),
            f(tx("bugs_add_soma_dois_enderecos_memoria"), tx("bugs_add_so_usa_registradores")),
        )),
        Especie("registrador-infinito", tx("bugs_bug_registrador_infinito"), Skill.REG_ALLOC, tx("bugs_sempre_ha_registrador_para_todo"), tx("bugs_sao_poucos_quem_nao_cabe"), listOf(
            v(tx("bugs_temporarios_vivos_ao_mesmo_tempo")),
            v(tx("bugs_colorir_grafo_dar_registradores_diferentes")),
            f(tx("bugs_temporarios_que_nao_interferem_precisam"), tx("bugs_eles_podem_dividir_mesmo")),
        )),
    )

    fun of(id: String): Especie? = especies.firstOrNull { it.id == id }
}
