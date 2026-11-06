package com.compilandia.learning

import com.compilandia.textos.tx

/**
 * Regiões do mapa = "Palácio da Memória" (método de loci).
 * A ORDEM do caminho é a ORDEM das fases do compilador: andar pelo mapa
 * é recitar o pipeline.
 */
enum class Region(val title: String, val emoji: String, val tagline: String) {
    PORTO(tx("skills_porto_partida"), "⚓", tx("skills_onde_codigo_fonte_embarca")),
    FLORESTA_LEXICA(tx("skills_floresta_lexica"), "🌲", tx("skills_cortar_texto_em_tokens")),
    VALE_GRAMATICAS(tx("skills_vale_das_gramaticas"), "🌄", tx("skills_regras_que_geram_linguagens")),
    PONTE_TRANSFORMACAO(tx("skills_ponte_transformacao"), "🌉", tx("skills_consertar_gramaticas")),
    TEMPLO_TOPDOWN(tx("skills_templo_top_down"), "🛕", tx("skills_prever_cima_para_baixo_ll")),
    FORTALEZA_BOTTOMUP(tx("skills_fortaleza_bottom_up"), "🏰", tx("skills_montar_baixo_para_cima_lr")),
    OFICINA_BISON(tx("skills_oficina_yacc_bison"), "🦬", tx("skills_geradores_analisadores")),
    CIDADE_SEMANTICA(tx("skills_cidade_semantica"), "🏙️", tx("skills_significado_tipos_escopos")),
    FORJA_CODIGO(tx("skills_forja_codigo"), "⚒️", tx("skills_codigo_intermediario_otimizacao")),
}

enum class Skill(val title: String, val region: Region, val prereqs: List<Skill> = emptyList()) {
    PHASES(tx("skills_fases_compilador"), Region.PORTO),
    TOKENS(tx("skills_tokens_lexemas_padroes"), Region.FLORESTA_LEXICA, listOf(PHASES)),
    REGEX_DFA(tx("skills_expressoes_regulares_automatos"), Region.FLORESTA_LEXICA, listOf(TOKENS)),
    NFA_TO_DFA("AFN → AFD (construção de subconjuntos)", Region.FLORESTA_LEXICA, listOf(REGEX_DFA)),
    DFA_MINIMIZATION(tx("skills_minimizacao_afd_tabela_pares"), Region.FLORESTA_LEXICA, listOf(REGEX_DFA)),
    LEX_FLEX(tx("skills_lex_flex_arquivo_l"), Region.FLORESTA_LEXICA, listOf(TOKENS, REGEX_DFA)),
    CFG_DERIVATION(tx("skills_gramaticas_derivacoes"), Region.VALE_GRAMATICAS, listOf(PHASES)),
    AMBIGUITY(tx("skills_arvores_ambiguidade"), Region.VALE_GRAMATICAS, listOf(CFG_DERIVATION)),
    LEFT_RECURSION(tx("skills_recursao_esquerda"), Region.PONTE_TRANSFORMACAO, listOf(CFG_DERIVATION)),
    LEFT_FACTORING(tx("skills_fatoracao_esquerda"), Region.PONTE_TRANSFORMACAO, listOf(CFG_DERIVATION)),
    FIRST(tx("skills_conjunto_first"), Region.TEMPLO_TOPDOWN, listOf(CFG_DERIVATION)),
    FOLLOW(tx("skills_conjunto_follow"), Region.TEMPLO_TOPDOWN, listOf(FIRST)),
    LL1_TABLE(tx("comum_tabela_ll_1_2"), Region.TEMPLO_TOPDOWN, listOf(FIRST, FOLLOW)),
    LL1_PARSING(tx("skills_analisador_preditivo_pilha"), Region.TEMPLO_TOPDOWN, listOf(LL1_TABLE)),
    RECURSIVE_DESCENT(tx("comum_descida_recursiva"), Region.TEMPLO_TOPDOWN, listOf(FIRST)),
    ERROR_RECOVERY(tx("skills_erros_sintaticos_recuperacao"), Region.TEMPLO_TOPDOWN, listOf(LL1_PARSING)),
    SHIFT_REDUCE(tx("skills_empilha_reduz_shift_reduce"), Region.FORTALEZA_BOTTOMUP, listOf(CFG_DERIVATION)),
    LR0_ITEMS(tx("skills_itens_lr_0_closure_goto"), Region.FORTALEZA_BOTTOMUP, listOf(SHIFT_REDUCE)),
    SLR_TABLE(tx("skills_tabela_slr_1_conflitos"), Region.FORTALEZA_BOTTOMUP, listOf(LR0_ITEMS, FOLLOW)),
    LALR(tx("skills_lr_1_lalr_1"), Region.FORTALEZA_BOTTOMUP, listOf(SLR_TABLE)),
    YACC_BISON(tx("skills_yacc_bison_especificacao_precedencia"), Region.OFICINA_BISON, listOf(SHIFT_REDUCE, AMBIGUITY)),
    ATTRIBUTES_TYPES(tx("skills_atributos_verificacao_tipos"), Region.CIDADE_SEMANTICA, listOf(CFG_DERIVATION)),
    SCOPE_SYMBOLS(tx("skills_escopo_tabela_simbolos"), Region.CIDADE_SEMANTICA, listOf(PHASES)),
    AST_BUILD(tx("skills_arvore_sintatica_abstrata_ast_com"), Region.CIDADE_SEMANTICA, listOf(CFG_DERIVATION)),
    TAC(tx("skills_codigo_tres_enderecos"), Region.FORJA_CODIGO, listOf(ATTRIBUTES_TYPES)),
    OPTIMIZATION(tx("skills_otimizacao_dobra_constantes"), Region.FORJA_CODIGO, listOf(TAC)),
    RUNTIME(tx("skills_ambientes_execucao_registros_ativacao"), Region.FORJA_CODIGO, listOf(SCOPE_SYMBOLS)),
    CODEGEN_MIPS(tx("skills_geracao_codigo_mips"), Region.FORJA_CODIGO, listOf(TAC)),
    REG_ALLOC(tx("skills_alocacao_registradores"), Region.FORJA_CODIGO, listOf(CODEGEN_MIPS)),
}

/** Minijogos. Cada habilidade tem pelo menos dois jogos com mecânicas diferentes. */
enum class GameId(val title: String, val emoji: String, val mechanic: String, val skills: List<Skill>) {
    PIPELINE(tx("skills_esteira_das_fases"), "🏭", tx("skills_ordenar_encaixar"), listOf(Skill.PHASES)),
    PHASE_DETECTIVE(tx("skills_detetive_erros"), "🕵️", tx("skills_classificar_quem_pega_cada_erro"), listOf(Skill.PHASES, Skill.TOKENS, Skill.ATTRIBUTES_TYPES, Skill.SCOPE_SYMBOLS, Skill.ERROR_RECOVERY)),
    TOKEN_SLICER(tx("skills_fatiador_tokens"), "🔪", tx("skills_cortar_classificar"), listOf(Skill.TOKENS, Skill.LEX_FLEX)),
    DFA_TRAIN(tx("skills_trem_automato"), "🚂", tx("skills_pilotar_trem_estado_estado"), listOf(Skill.REGEX_DFA, Skill.NFA_TO_DFA, Skill.DFA_MINIMIZATION)),
    REGEX_SORTER(tx("skills_porteiro_regex"), "🚪", tx("skills_deixar_entrar_so_quem_casa"), listOf(Skill.REGEX_DFA, Skill.TOKENS)),
    DERIVATION_RACE(tx("skills_derivacao_turbo"), "🏎️", tx("skills_escolher_producoes_crescer_arvore"), listOf(Skill.CFG_DERIVATION, Skill.AMBIGUITY, Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT)),
    TWO_TREES(tx("skills_duas_arvores"), "🌳", tx("skills_achar_duas_arvores_para_mesma"), listOf(Skill.AMBIGUITY, Skill.CFG_DERIVATION)),
    GRAMMAR_SURGERY(tx("skills_cirurgia_gramatica"), "🩺", tx("skills_montar_gramatica_consertada_com_pecas"), listOf(Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)),
    FIRST_FOLLOW_ALCHEMY(tx("skills_alquimia_first_follow"), "⚗️", tx("skills_jogar_ingredientes_nos_caldeiroes"), listOf(Skill.FIRST, Skill.FOLLOW)),
    LL1_SUDOKU(tx("skills_sudoku_ll_1"), "🧩", tx("skills_encaixar_producoes_na_tabela"), listOf(Skill.LL1_TABLE, Skill.FIRST, Skill.FOLLOW, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)),
    STACK_TOWER(tx("skills_torre_pilha"), "🗼", tx("skills_operar_pilha_analisador_preditivo"), listOf(Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT, Skill.LL1_TABLE)),
    SHIFT_REDUCE_RUSH(tx("skills_esteira_shift_reduce"), "📦", tx("skills_empilhar_ou_reduzir_contra_relogio"), listOf(Skill.SHIFT_REDUCE, Skill.SLR_TABLE, Skill.LR0_ITEMS)),
    ITEM_BUILDER(tx("skills_construtor_estados"), "🏗️", tx("skills_fechar_closure_transitar_goto"), listOf(Skill.LR0_ITEMS, Skill.LALR)),
    CONFLICT_HUNTER(tx("skills_caca_conflitos"), "🎯", tx("skills_achar_explicar_conflitos"), listOf(Skill.SLR_TABLE, Skill.LALR, Skill.YACC_BISON)),
    BISON_WORKSHOP(tx("skills_oficina_bison"), "🦬", tx("skills_programar_bison_ate_testes_passarem"), listOf(Skill.YACC_BISON, Skill.SHIFT_REDUCE)),
    ATTRIBUTE_TREE(tx("skills_arvore_anotada"), "🏷️", tx("skills_calcular_atributos_baixo_para_cima"), listOf(Skill.ATTRIBUTES_TYPES, Skill.TAC, Skill.OPTIMIZATION)),
    SCOPE_DETECTIVE(tx("skills_esconde_esconde_escopo"), "🔦", tx("skills_ligar_cada_uso_sua_declaracao"), listOf(Skill.SCOPE_SYMBOLS, Skill.RUNTIME)),
    TAC_ASSEMBLY(tx("skills_linha_montagem_tac"), "🔧", tx("skills_montar_codigo_tres_enderecos"), listOf(Skill.TAC, Skill.OPTIMIZATION, Skill.CODEGEN_MIPS, Skill.AST_BUILD)),
    SUBSET_FACTORY(tx("skills_fabrica_subconjuntos"), "🧺", tx("skills_juntar_estados_afn_em_estados"), listOf(Skill.NFA_TO_DFA)),
    FLEX_WORKSHOP(tx("skills_oficina_flex"), "🦎", tx("skills_ordenar_escolher_regras_l"), listOf(Skill.LEX_FLEX)),
    PANIC_ROOM(tx("skills_sala_panico"), "🚨", tx("skills_achar_erro_sincronizar"), listOf(Skill.ERROR_RECOVERY)),
    CALL_STACK(tx("skills_pilha_chamadas"), "📚", tx("skills_empilhar_desempilhar_registros_ativacao"), listOf(Skill.RUNTIME)),
    MIPS_FORGE(tx("skills_montadora_mips"), "🔩", tx("skills_traduzir_tac_para_assembly_rodar"), listOf(Skill.CODEGEN_MIPS, Skill.REG_ALLOC)),
    DESCENT_CODER(tx("skills_programador_descendente"), "🧑‍💻", tx("skills_montar_codigo_parser"), listOf(Skill.RECURSIVE_DESCENT, Skill.LL1_TABLE)),
    MIN_TABLE(tx("skills_tabela_dos_pares"), "🧮", tx("skills_marcar_pares_distinguiveis_juntar_estados"), listOf(Skill.DFA_MINIMIZATION)),
    AST_FACTORY(tx("skills_fabrica_ast"), "🪵", tx("skills_montar_ast_com_new_binop"), listOf(Skill.AST_BUILD, Skill.ATTRIBUTES_TYPES)),
    REG_COLORING(tx("skills_coloracao_registradores"), "🎨", tx("skills_vivacidade_colorir_grafo_interferencia"), listOf(Skill.REG_ALLOC)),
    BRANCH_MAZE(tx("skills_labirinto_desvios"), "🔀", tx("skills_completar_desvios_if_while_rodar"), listOf(Skill.CODEGEN_MIPS)),
    AUTOMATON_FACTORY(tx("skills_fabrica_automatos"), "🏭", tx("skills_montar_automato_passar_nos_robos"), listOf(Skill.REGEX_DFA, Skill.DFA_MINIMIZATION)),
    /** Revisão relâmpago de flashcards (não entra no sorteio de minijogos). */
    FLASHCARDS(tx("skills_revisao_relampago"), "⚡", tx("skills_lembrar_sem_olhar"), emptyList()),
    ;

    companion object {
        fun forSkill(s: Skill): List<GameId> = entries.filter { s in it.skills }
    }
}

/** Estilos de explicação. O botão "Não entendi" troca entre eles. */
enum class Style(val title: String, val emoji: String, val description: String) {
    STORY(tx("skills_historia_analogia"), "📖", tx("skills_metafora_dia_dia")),
    VISUAL(tx("skills_passo_passo_visual"), "🎬", tx("skills_ver_algoritmo_acontecendo")),
    WORKED(tx("skills_exemplo_resolvido"), "✍️", tx("skills_exercicio_resolvido_com_calma")),
    SOCRATIC(tx("skills_perguntas_guiadas"), "🤔", tx("skills_voce_descobre_respondendo_perguntas")),
    FORMAL(tx("skills_definicao_prova"), "🎓", tx("skills_jeito_que_cai_na_prova")),
    MNEMONIC(tx("skills_truque_memoria"), "🧠", tx("skills_ganchos_para_nunca_esquecer")),
}

/** O que dá para comprar para a oficina, na ordem em que o jogo oferece. O id é o da camada em RoomLayers. */
enum class RoomItem(val id: String, val title: String, val price: Int) {
    ABAJUR("abajur", tx("skills_abajur_bancada"), 30),
    PLANTA("planta", tx("skills_planta"), 40),
    CADEIRA("cadeira", tx("skills_cadeira"), 60),
    LIXEIRA("lixeira", tx("skills_lixeira_bugs"), 40),
    QUADRO("quadro", tx("skills_quadro_automato"), 80),
    LUMINARIA("luminaria", tx("skills_luminaria"), 90),
    URSINHO("ursinho", tx("skills_ursinho_byte"), 70),
    CAIXA_SOM("caixasom", tx("skills_caixinha_som"), 90),
    ESTANTE("estante", tx("skills_estante"), 120),
    RADIO("radio", tx("skills_radio"), 100),
    CAFE("cafe", tx("skills_cantinho_cafe"), 130),
    SOFA("sofa", tx("skills_sofa"), 200),
}
