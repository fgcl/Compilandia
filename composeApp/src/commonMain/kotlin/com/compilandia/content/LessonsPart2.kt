package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Parte 2 do conteúdo: Templo Top-Down (FIRST, FOLLOW, LL(1), descida recursiva)
// e a entrada da Fortaleza Bottom-Up (empilha-reduz, itens LR(0)).
// As aulas estão em LessonsPart2a.kt, LessonsPart2b.kt e LessonsPart2c.kt.

/** Gramáticas usadas nas aulas da parte 2 (formato de Grammar.parse; "→" também é aceito). */
internal const val P2_EXPR_LL = "E → T E'\nE' → + T E' | ε\nT → F T'\nT' → * F T' | ε\nF → ( E ) | id"
internal const val P2_EXPR = "E → E + T | T\nT → T * F | F\nF → ( E ) | id"
internal const val P2_DECL = "D → T L ;\nT → int | float\nL → id R\nR → , id R | ε"
internal const val P2_ABC = "S → A B c\nA → a | ε\nB → b | ε"
internal const val P2_PARENS = "S → ( S ) S | ε"
internal const val P2_ANBN = "S → a S b | ε"
internal const val P2_FACT = "S → a b | a c"
internal const val P2_LIST = "S → S a | b"
internal const val P2_CC = "S → C C\nC → c C | d"
internal const val P2_CMD = "S → if E then S | print E | begin L end\nL → S L | ε\nE → id"
internal const val P2_HANDLE = "S → a A B e\nA → A b c | b\nB → d"

val lessonsPart2: List<Lesson> by lazy {
    p2FirstLessons + p2FollowLessons + p2Ll1TableLessons + p2Ll1ParsingLessons +
        p2RecDescentLessons + p2ShiftReduceLessons + p2Lr0Lessons
}

val cardsPart2: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.FIRST,
        oneLiner = tx("lessonspart2_first_conjunto_terminais_que_podem"),
        hook = tx("lessonspart2_first_fila_quem_inicia_terminal"),
        keyTerms = listOf("terminal", tx("lessonspart2_comec"), "ε", tx("lessonspart2_anulavel"), tx("comum_nao_terminal"), tx("lessonspart2_ponto_fixo")),
        commonMistakes = listOf(
            tx("lessonspart2_continuar_para_proximo_simbolo_mesmo"),
            tx("lessonspart2_copiar_first_y1_para_first"),
            tx("lessonspart2_colocar_dentro_first_so_existe"),
            tx("lessonspart2_colocar_nao_terminal_dentro_conjunto"),
            tx("lessonspart2_parar_depois_unica_passada_sem"),
        ),
        flashcards = listOf(
            tx("lessonspart2_first_se_terminal") to "{a}",
            tx("lessonspart2_quando_first") to tx("lessonspart2_quando_anulavel"),
            "A → B c e B → b | ε. FIRST(A)?" to "{b, c}",
            "A → B C, B → x | ε, C → y | ε. FIRST(A)?" to tx("comum_x_y"),
            tx("lessonspart2_first_na_gramatica_t") to "{(, id}",
            tx("lessonspart2_pode_estar_em_first") to tx("lessonspart2_nunca"),
            tx("lessonspart2_por_que_algoritmo_repete_voltas") to tx("lessonspart2_conjuntos_dependem_uns_dos_outros"),
            tx("lessonspart2_first_b_c") to tx("lessonspart2_c_calculo_nao_trava_com"),
        ),
    ),
    SkillCard(
        skill = Skill.FOLLOW,
        oneLiner = tx("lessonspart2_follow_conjunto_terminais_que_podem"),
        hook = tx("lessonspart2_dolar_vizinho_heranca_no_inicial"),
        keyTerms = listOf(tx("comum_kt_depois"), "\$", "first", tx("comum_kt_herda"), tx("comum_kt_inicial"), tx("lessonspart2_anulavel")),
        commonMistakes = listOf(
            tx("lessonspart2_colocar_em_follow_follow_nunca"),
            tx("lessonspart2_esquecer_no_follow_simbolo_inicial"),
            tx("lessonspart2_inverter_heranca_follow_follow_b"),
            tx("lessonspart2_esquecer_heranca_quando_resto_anulavel"),
            tx("lessonspart2_calcular_follow_antes_ter_todos"),
        ),
        flashcards = listOf(
            tx("lessonspart2_regra_1_follow") to tx("lessonspart2_follow_simbolo_inicial"),
            tx("lessonspart2_em_b_que_entra_em") to tx("lessonspart2_first"),
            tx("lessonspart2_quando_follow_entra_em_follow") to tx("lessonspart2_se_b_ou_b_com"),
            tx("lessonspart2_follow_pode_conter") to tx("lessonspart2_nunca"),
            "S → a S b | ε. FOLLOW(S)?" to "{b, \$}",
            "S → ( S ) S | ε. FOLLOW(S)?" to "{), \$}",
            tx("lessonspart2_na_gramatica_expressoes_ll_1") to "{*, +, ), \$}",
            tx("lessonspart2_para_que_serve_follow_na") to tx("lessonspart2_decidir_onde_colocar_producoes_que"),
        ),
    ),
    SkillCard(
        skill = Skill.LL1_TABLE,
        oneLiner = tx("lessonspart2_tabela_preditiva_m_diz_qual"),
        hook = tx("lessonspart2_linha_variavel_coluna_token_primeiro"),
        keyTerms = listOf("first", "follow", "ε", tx("lessonspart2_celula"), tx("comum_kt_conflito"), "lookahead"),
        commonMistakes = listOf(
            tx("lessonspart2_por_em_colunas_first_em"),
            tx("lessonspart2_esquecer_coluna"),
            tx("lessonspart2_achar_que_producoes_impedem_gramatica"),
            tx("lessonspart2_achar_que_eliminar_recursao_esquerda"),
            tx("lessonspart2_preencher_celula_por_celula_no"),
        ),
        flashcards = listOf(
            tx("lessonspart2_regra_1_tabela") to tx("lessonspart2_para_cada_first_em_m"),
            tx("lessonspart2_regra_2_tabela") to tx("lessonspart2_se_first_em_m_b"),
            // As três condições LL(1) do livro, para cada A → α | β (professor do bloco 3: só existiam nos textos).
            tx("ll1_cond1_q") to tx("ll1_cond1_r"),
            tx("ll1_cond2_q") to tx("ll1_cond2_r"),
            tx("ll1_cond3_q") to tx("ll1_cond3_r"),
            tx("lessonspart2_celula_vazia_significa") to tx("lessonspart2_erro_sintaxe"),
            tx("lessonspart2_celula_com_duas_producoes_significa") to tx("lessonspart2_conflito_gramatica_nao_ll_1"),
            tx("lessonspart2_que_significa_ll_1") to tx("lessonspart2_left_to_right_leftmost_derivation"),
            tx("lessonspart2_gramatica_com_recursao_esquerda_pode") to tx("lessonspart2_nao"),
            tx("lessonspart2_gramatica_ambigua_pode_ser_ll") to tx("lessonspart2_nao"),
            tx("lessonspart2_m_na_gramatica_expressoes_ll") to "E' → ε",
        ),
    ),
    SkillCard(
        skill = Skill.LL1_PARSING,
        oneLiner = tx("lessonspart2_analisador_preditivo_nao_recursivo_usa"),
        hook = tx("lessonspart2_terminal_no_topo_casa_anda"),
        keyTerms = listOf(tx("comum_kt_pilha"), tx("comum_kt_topo"), tx("comum_kt_tabela"), tx("comum_kt_casa"), tx("comum_kt_invertid"), "\$"),
        commonMistakes = listOf(
            tx("lessonspart2_empilhar_lado_direito_na_ordem"),
            tx("lessonspart2_avancar_entrada_numa_expansao_so"),
            tx("lessonspart2_empilhar_producao_vazia_so_desempilha"),
            tx("lessonspart2_esquecer_comecar_pilha_com"),
            tx("lessonspart2_misturar_lado_topo_pilha_no"),
        ),
        flashcards = listOf(
            tx("lessonspart2_configuracao_inicial") to "Pilha \$ S; entrada w\$.",
            tx("lessonspart2_topo_terminal_igual_ao_token") to tx("lessonspart2_desempilha_avanca_casar"),
            tx("lessonspart2_topo_nao_terminal_token") to tx("lessonspart2_troca_pelo_lado_direito_m"),
            tx("lessonspart2_quando_aceita") to tx("lessonspart2_topo_entrada"),
            tx("lessonspart2_m_vazia") to tx("lessonspart2_erro_sintaxe"),
            tx("lessonspart2_que_derivacao_saida_forma") to tx("lessonspart2_derivacao_mais_esquerda"),
            tx("lessonspart2_expandir_faz_que_na_pilha") to tx("lessonspart2_so_desempilha"),
            tx("lessonspart2_analisador_preditivo_faz_backtracking") to tx("lessonspart2_nunca"),
        ),
    ),
    SkillCard(
        skill = Skill.RECURSIVE_DESCENT,
        oneLiner = tx("lessonspart2_descida_recursiva_funcao_por_nao"),
        hook = tx("lessonspart2_variavel_funcao_terminal_match_recursao"),
        keyTerms = listOf(tx("lessonspart2_funcao"), tx("comum_nao_terminal"), "lookahead", "match", tx("comum_recursao_esquerda"), "first"),
        commonMistakes = listOf(
            tx("lessonspart2_usar_gramatica_com_recursao_esquerda"),
            tx("lessonspart2_nao_fatorar_esquerda_token_nao"),
            tx("lessonspart2_esquecer_verificar_no_fim_pode"),
            tx("lessonspart2_avancar_token_dentro_chamada_nao"),
            tx("lessonspart2_achar_que_descida_recursiva_bottom"),
        ),
        flashcards = listOf(
            tx("lessonspart2_quantas_funcoes") to tx("lessonspart2_por_nao_terminal"),
            tx("lessonspart2_terminal_no_lado_direito_vira") to "match(terminal).",
            tx("lessonspart2_nao_terminal_no_lado_direito") to tx("lessonspart2_chamada_funcao_dele"),
            tx("lessonspart2_como_escolher_producao") to tx("lessonspart2_pelo_lookahead_first_ou_follow"),
            tx("lessonspart2_onde_fica_pilha") to tx("lessonspart2_na_pilha_chamadas_programa"),
            tx("lessonspart2_por_que_recursao_esquerda_quebra") to tx("lessonspart2_chama_sem_consumir_token_laco"),
            tx("lessonspart2_recursao_direita_problema") to tx("lessonspart2_nao_cada_chamada_consome_token"),
            tx("lessonspart2_que_classe_gramaticas_forma_preditiva") to "LL(1).",
        ),
    ),
    SkillCard(
        skill = Skill.SHIFT_REDUCE,
        oneLiner = tx("lessonspart2_analise_empilha_reduz_bottom_up"),
        hook = tx("lessonspart2_empilha_ate_ver_alca_achou"),
        keyTerms = listOf(tx("comum_kt_empilha"), tx("comum_kt_reduz"), "handle", tx("comum_kt_topo"), tx("lessonspart2_mais_direita"), tx("comum_kt_conflito")),
        commonMistakes = listOf(
            tx("lessonspart2_reduzir_qualquer_ocorrencia_lado_direito"),
            tx("lessonspart2_achar_que_reduzir_consome_token"),
            tx("lessonspart2_procurar_handle_no_meio_pilha"),
            tx("lessonspart2_confundir_reducoes_lr_formam_derivacao"),
        ),
        flashcards = listOf(
            tx("lessonspart2_4_acoes") to tx("lessonspart2_empilhar_reduzir_aceitar_erro"),
            tx("lessonspart2_que_handle") to tx("lessonspart2_lado_direito_cuja_reducao_passo"),
            tx("lessonspart2_onde_handle_aparece") to tx("lessonspart2_sempre_no_topo_pilha"),
            tx("lessonspart2_configuracao_aceitacao") to tx("lessonspart2_pilha_entrada"),
            tx("lessonspart2_reduzir_por_faz_que") to tx("lessonspart2_desempilha_empilha"),
            tx("lessonspart2_conflito_shift_reduce") to tx("lessonspart2_nao_para_decidir_entre_empilhar"),
            tx("lessonspart2_conflito_reduce_reduce") to tx("lessonspart2_nao_para_decidir_qual_producao"),
            tx("lessonspart2_que_significa_r_lr") to tx("lessonspart2_rightmost_derivacao_mais_direita_ao"),
        ),
    ),
    SkillCard(
        skill = Skill.LR0_ITEMS,
        oneLiner = tx("lessonspart2_item_lr_0_producao_com"),
        hook = tx("lessonspart2_ponto_voce_esta_aqui_closure"),
        keyTerms = listOf(tx("comum_kt_ponto"), "item", "closure", "goto", tx("comum_kt_estado"), tx("comum_kt_aumentada")),
        commonMistakes = listOf(
            tx("lessonspart2_esquecer_aplicar_fechamento_depois_goto"),
            tx("lessonspart2_no_fechamento_adicionar_itens_com"),
            tx("lessonspart2_fechar_sobre_terminais_so_nao"),
            tx("lessonspart2_criar_estado_novo_para_conjunto"),
            tx("lessonspart2_esquecer_gramatica_aumentada"),
        ),
        flashcards = listOf(
            tx("lessonspart2_quantos_itens_gera_x_y") to "4",
            tx("lessonspart2_itens") to tx("lessonspart2_so"),
            tx("lessonspart2_closure_item_b_adiciona") to tx("solta_lessonspart2_1"),
            "GOTO(I, X)?" to tx("lessonspart2_avanca_ponto_sobre_x_nos"),
            tx("lessonspart2_para_que") to tx("lessonspart2_ter_unico_item_aceitacao"),
            tx("lessonspart2_item_completo_ponto_no_fim") to tx("lessonspart2_reducao"),
            tx("lessonspart2_quantos_estados_lr_0_tem") to tx("lessonspart2_7_i0_i6"),
            tx("lessonspart2_item_nucleo_kernel") to tx("solta_lessonspart2_2"),
        ),
    ),
)
