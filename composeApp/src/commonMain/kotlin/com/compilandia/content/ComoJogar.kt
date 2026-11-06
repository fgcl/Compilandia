package com.compilandia.content

import com.compilandia.content.Speaker.BYTE
import com.compilandia.learning.GameId
import com.compilandia.textos.tx

/** Instrução falada de cada jogo. Curta de propósito: quem joga aprende mexendo. */
val comoJogar: Map<GameId, Fala> = mapOf(
    GameId.PIPELINE to tx("comojogar_monte_esteira_toque_nas_fases"),
    GameId.PHASE_DETECTIVE to tx("comojogar_cada_cartao_traz_erro_diga"),
    GameId.TOKEN_SLICER to tx("comojogar_toque_entre_letras_para_cortar"),
    GameId.DFA_TRAIN to tx("comojogar_voce_pilota_trem_cada_letra"),
    GameId.AUTOMATON_FACTORY to tx("comojogar_monte_automato_com_dedo_robos"),
    GameId.REGEX_SORTER to tx("comojogar_voce_porteiro_so_entra_quem"),
    GameId.DERIVATION_RACE to tx("comojogar_chegue_na_frase_alvo_troque"),
    GameId.TWO_TREES to tx("comojogar_prove_que_gramatica_ambigua_monte"),
    GameId.GRAMMAR_SURGERY to tx("comojogar_conserte_gramatica_toque_numa_peca"),
    GameId.FIRST_FOLLOW_ALCHEMY to tx("comojogar_escolha_caldeirao_jogue_dentro_simbolos"),
    GameId.LL1_SUDOKU to tx("comojogar_preencha_tabela_toque_numa_celula"),
    GameId.STACK_TOWER to tx("comojogar_olhe_topo_pilha_proximo_token"),
    GameId.SHIFT_REDUCE_RUSH to tx("comojogar_empilhe_tokens_quando_lado_direito"),
    GameId.ITEM_BUILDER to tx("comojogar_monte_estado_marque_itens_que"),
    GameId.CONFLICT_HUNTER to tx("comojogar_ache_celulas_com_duas_acoes"),
    GameId.BISON_WORKSHOP to tx("comojogar_edite_especificacao_ate_todos_testes"),
    GameId.ATTRIBUTE_TREE to tx("comojogar_calcule_baixo_para_cima_toque"),
    GameId.SCOPE_DETECTIVE to tx("comojogar_para_cada_uso_variavel_toque"),
    GameId.TAC_ASSEMBLY to tx("comojogar_monte_codigo_na_ordem_cada"),
    GameId.SUBSET_FACTORY to tx("comojogar_transforme_afn_em_afd_para"),
    GameId.FLEX_WORKSHOP to tx("comojogar_arrume_regras_ate_scanner_gerar"),
    GameId.PANIC_ROOM to tx("comojogar_ache_onde_parser_percebe_erro"),
    GameId.CALL_STACK to tx("comojogar_rode_programa_empilhe_na_chamada"),
    GameId.MIPS_FORGE to tx("comojogar_traduza_cada_instrucao_para_mips"),
    GameId.DESCENT_CODER to tx("comojogar_monte_metodo_parser_linha_cada"),
    GameId.MIN_TABLE to tx("comojogar_marque_pares_estados_diferentes_que"),
    GameId.AST_FACTORY to tx("comojogar_monte_arvore_fora_para_dentro"),
    GameId.REG_COLORING to tx("comojogar_veja_quem_esta_vivo_pinte"),
    GameId.BRANCH_MAZE to tx("comojogar_complete_desvios_if_while_rode"),
).mapValues { Fala(BYTE, it.value) }
