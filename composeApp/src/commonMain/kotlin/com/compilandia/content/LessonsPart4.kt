package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Parte 4 do conteúdo: NFA_TO_DFA e LEX_FLEX (Floresta Léxica) e ERROR_RECOVERY (Templo Top-Down).
// As aulas estão em LessonsPart4a.kt (AFN → AFD), LessonsPart4b.kt (Lex/Flex) e LessonsPart4c.kt (recuperação de erros).

private val p4Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.NFA_TO_DFA,
        oneLiner = tx("lessonspart4_construcao_subconjuntos_transforma_afn_em"),
        hook = tx("lessonspart4_anda_fecha_porta_move_com"),
        feynmanPrompt = tx("lessonspart4_explique_para_amigo_12_anos"),
        keyTerms = listOf("conjunto", "fecho", "move", "final", "morto", "inicial"),
        commonMistakes = listOf(
            tx("lessonspart4_esquecer_fecho_depois_move"),
            tx("lessonspart4_usar_s0_como_estado_inicial"),
            tx("lessonspart4_marcar_como_final_so_conjunto"),
            tx("lessonspart4_achar_que_construcao_subconjuntos_ja"),
            tx("lessonspart4_achar_que_afn_reconhece_mais"),
        ),
        flashcards = listOf(
            tx("lessonspart4_que_estado_afd_na_construcao") to tx("lessonspart4_conjunto_estados_afn"),
            tx("lessonspart4_formula_dtran_t") to tx("lessonspart4_fecho_move_t"),
            tx("lessonspart4_estado_inicial_afd") to tx("lessonspart4_fecho_s0"),
            tx("lessonspart4_quando_estado_afd_final") to tx("lessonspart4_quando_contem_pelo_menos_estado"),
            tx("lessonspart4_move_t_vazio_leva_que") to tx("lessonspart4_ao_estado_morto"),
            tx("lessonspart4_maximo_estados_afd_para_afn") to "2ⁿ",
            tx("lessonspart4_fecho_0_no_afn_thompson") to "{0, 1, 2, 4, 7}",
            tx("lessonspart4_linguagem_classica_explosao_exponencial") to tx("lessonspart4_n_esimo_simbolo_partir_fim"),
            tx("lessonspart4_por_que_lexico_usa_afd") to tx("lessonspart4_passo_por_caractere_sem_carregar"),
        ),
    ),
    SkillCard(
        skill = Skill.LEX_FLEX,
        oneLiner = tx("lessonspart4_lex_flex_le_arquivo_l"),
        hook = tx("lessonspart4_d_r_c_guloso_mas"),
        feynmanPrompt = tx("lessonspart4_explique_para_amigo_12_anos_2"),
        keyTerms = listOf("regra", "padr", "maior", "primeir", "yytext", "yylex", "return"),
        commonMistakes = listOf(
            tx("lessonspart4_por_regra_identificador_antes_das"),
            tx("lessonspart4_por_regra_coringa_antes_das"),
            tx("lessonspart4_achar_que_ordem_das_regras"),
            tx("lessonspart4_confundir_yytext_texto_lexema_com"),
            tx("lessonspart4_esquecer_yywrap_ou_option_noyywrap"),
        ),
        flashcards = listOf(
            tx("lessonspart4_quais_sao_3_secoes_arquivo") to tx("lessonspart4_definicoes_regras_codigo_c"),
            tx("lessonspart4_que_comando_lex_gera") to tx("lessonspart4_arquivo_lex_yy_c_com"),
            tx("lessonspart4_regra_1_desempate_lex") to tx("lessonspart4_maior_casamento_lexema_mais_longo"),
            tx("lessonspart4_regra_2_desempate_lex") to tx("lessonspart4_mesmo_tamanho_vence_regra_listada"),
            tx("lessonspart4_iffy_com_if_antes_z") to tx("lessonspart4_unico_id_maior_casamento"),
            tx("lessonspart4_que_guardam_yytext_yyleng") to tx("lessonspart4_texto_lexema_seu_tamanho"),
            tx("lessonspart4_acao_sem_return_como_faz") to tx("lessonspart4_descarta_lexema_yylex_continua"),
            tx("lessonspart4_como_valor_numero_chega_ao") to "Por yylval (ex.: yylval = atoi(yytext);)",
            tx("lessonspart4_como_compilar_scanner_tp") to tx("lessonspart4_lex_scanner_l_gcc_lex"),
        ),
    ),
    SkillCard(
        skill = Skill.ERROR_RECOVERY,
        oneLiner = tx("lessonspart4_analisador_detecta_erro_no_primeiro"),
        hook = tx("lessonspart4_first_fica_follow_foge_panico"),
        feynmanPrompt = tx("lessonspart4_explique_para_amigo_12_anos_3"),
        keyTerms = listOf(tx("lessonspart4_panico"), "sincroniza", "follow", "descart", "prefixo", "erro"),
        commonMistakes = listOf(
            tx("lessonspart4_achar_que_recuperar_erro_corrigir"),
            tx("lessonspart4_usar_first_como_conjunto_para"),
            tx("lessonspart4_esquecer_entre_tokens_sincronizacao"),
            tx("lessonspart4_achar_que_erro_detectado_depois"),
            tx("lessonspart4_confiar_em_todas_mensagens_seguintes"),
        ),
        flashcards = listOf(
            tx("lessonspart4_quatro_tipos_erro") to tx("lessonspart4_lexico_sintatico_semantico_logico"),
            tx("lessonspart4_quatro_estrategias_recuperacao") to tx("lessonspart4_modo_panico_nivel_frase_producoes"),
            tx("lessonspart4_que_propriedade_prefixo_viavel") to tx("lessonspart4_ll_lr_acusam_erro_no"),
            tx("lessonspart4_modo_panico_token_em_first") to tx("lessonspart4_para_descartar_continua_com"),
            tx("lessonspart4_modo_panico_token_em_follow") to tx("lessonspart4_desempilha_sincronizou"),
            tx("lessonspart4_terminal_no_topo_diferente_entrada") to tx("lessonspart4_desempilha_terminal_finge_que_estava"),
            tx("lessonspart4_tokens_sincronizacao_tipicos_em_c") to "';' e '}'",
            tx("lessonspart4_token_especial_bison_para_recuperacao") to "error (ex.: comando : error ';')",
            tx("lessonspart4_para_que_serve_yyerrok") to tx("lessonspart4_sair_modo_recuperacao_voltar_relatar"),
        ),
    ),
)

// Agregados no FIM do arquivo (propriedades de topo inicializam na ordem do arquivo).
val lessonsPart4: List<Lesson> by lazy { p4NfaLessons + p4FlexLessons + p4ErrorLessons }
val cardsPart4: List<SkillCard> by lazy { p4Cards }
