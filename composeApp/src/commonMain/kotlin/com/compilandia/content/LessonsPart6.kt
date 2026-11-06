package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Parte 6 do conteúdo: DFA_MINIMIZATION (LessonsPart6a.kt), AST_BUILD (LessonsPart6b.kt) e REG_ALLOC (LessonsPart6c.kt).

private val p6Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.DFA_MINIMIZATION,
        oneLiner = tx("lessonspart6_minimizar_afd_fundir_estados_equivalentes"),
        hook = tx("lessonspart6_mpj_marca_propaga_junta_morto"),
        feynmanPrompt = tx("lessonspart6_explique_para_amigo_12_anos"),
        keyTerms = listOf("equivalen", "distingu", "par", "marca", "final", "morto", "tabela"),
        commonMistakes = listOf(
            tx("lessonspart6_esquecer_completar_afd_com_estado"),
            tx("lessonspart6_marcar_par_com_base_num"),
            tx("lessonspart6_parar_depois_rodada_sem_repetir"),
            tx("lessonspart6_esquecer_remover_estados_inalcancaveis"),
            tx("lessonspart6_fazer_tabela_nao_desenhar_afd"),
        ),
        flashcards = listOf(
            tx("lessonspart6_quando_dois_estados_sao_equivalentes") to tx("lessonspart6_quando_nenhuma_cadeia_w_leva"),
            tx("lessonspart6_que_rodada_1_marca") to tx("lessonspart6_todo_par_final_nao_final"),
            tx("lessonspart6_regra_das_rodadas_seguintes") to tx("lessonspart6_marque_p_q_se_para"),
            tx("lessonspart6_quando_parar") to tx("lessonspart6_quando_rodada_inteira_nao_marca"),
            tx("lessonspart6_que_fazer_se_falta_transicao") to tx("lessonspart6_completar_com_estado_morto_nao"),
            tx("lessonspart6_quantas_casas_tem_tabela_com") to tx("lessonspart6_n_n_1_2_so"),
            tx("lessonspart6_no_afd_livro_para_b") to tx("lessonspart6_q0_q2_c_5_estados"),
            tx("lessonspart6_metodo_alternativo_tabela") to tx("lessonspart6_refinar_particoes_moore_hopcroft_comeca"),
            tx("lessonspart6_afd_minimo_unico") to tx("lessonspart6_sim_menos_renomear_estados_myhill"),
        ),
    ),
    SkillCard(
        skill = Skill.AST_BUILD,
        oneLiner = tx("lessonspart6_arvore_sintatica_abstrata_guarda_so"),
        hook = tx("lessonspart6_ast_arvore_sem_tralha_corta"),
        feynmanPrompt = tx("lessonspart6_explique_para_amigo_12_anos_2"),
        keyTerms = listOf("abstrat", "arvore", "classe", "binop", "parentes", "precedenc", "parser"),
        commonMistakes = listOf(
            tx("lessonspart6_criar_nos_para_parenteses_ou"),
            tx("lessonspart6_montar_ast_pendurada_para_direita"),
            tx("lessonspart6_achar_que_sem_parenteses_precedencia"),
            tx("lessonspart6_confundir_ast_estrutura_em_arvore"),
            tx("lessonspart6_esquecer_2_na_regra_parentese"),
        ),
        flashcards = listOf(
            tx("lessonspart6_que_ast_joga_fora") to tx("lessonspart6_correntes_nao_terminais_parenteses_pontuacao"),
            tx("lessonspart6_nos_derivacao_ast_em_b") to "13 × 5",
            tx("lessonspart6_onde_mora_precedencia_na_ast") to tx("lessonspart6_na_forma_quem_esta_mais"),
            tx("lessonspart6_linha_chave_associatividade_esquerda_no") to "e = new BinOp(op, e, termo()); dentro do while",
            tx("lessonspart6_acao_bison_para_t") to "\$\$ = new BinOp('+', \$1, \$3);",
            tx("lessonspart6_acao_bison_para_f") to tx("lessonspart6_2_parentese_some"),
            tx("lessonspart6_classes_comando_tipicas") to tx("lessonspart6_atrib_se_enquanto_bloco_estendem"),
            tx("lessonspart6_vantagem_ast_sobre_gerar_codigo") to tx("lessonspart6_varias_passadas_tipos_depois_codigo"),
            tx("lessonspart6_nos_ast_com_n_operadores") to "2n + 1",
        ),
    ),
    SkillCard(
        skill = Skill.REG_ALLOC,
        oneLiner = tx("lessonspart6_alocar_registradores_manter_temporarios_em"),
        hook = tx("lessonspart6_vigor_vivacidade_grafo_colorir_registradores"),
        feynmanPrompt = tx("lessonspart6_explique_para_amigo_12_anos_3"),
        keyTerms = listOf("registrador", "viv", "interferen", "color", "spill", "memoria"),
        commonMistakes = listOf(
            tx("lessonspart6_fazer_analise_vivacidade_cima_para"),
            tx("lessonspart6_achar_que_vivo_ja_foi"),
            tx("lessonspart6_ligar_no_grafo_temporarios_que"),
            tx("lessonspart6_confundir_numero_cromatico_com_numero"),
            tx("lessonspart6_inverter_convencao_salvo_pelo_chamado"),
        ),
        flashcards = listOf(
            tx("lessonspart6_por_que_alocar_registradores") to tx("lessonspart6_lw_sw_sao_lentos_registradores"),
            tx("lessonspart6_equacao_vivacidade") to tx("lessonspart6_in_use_out_def_tras"),
            tx("lessonspart6_quando_x_y_interferem") to tx("lessonspart6_quando_definido_enquanto_outro_esta"),
            tx("lessonspart6_k_registradores_equivalem") to tx("lessonspart6_colorir_grafo_interferencia_com_k"),
            tx("lessonspart6_passos_chaitin") to tx("lessonspart6_simplificar_nos_grau_k_empilhando"),
            tx("lessonspart6_que_spill") to tx("lessonspart6_derramar_temporario_para_memoria_sw"),
            tx("lessonspart6_lw_sw_em_b_c") to "11 × 5",
            "\$s0–\$s7 são salvos por quem?" to tx("lessonspart6_pelo_chamado_callee_saved"),
            "\$t0–\$t9 são salvos por quem?" to tx("lessonspart6_pelo_chamador_caller_saved"),
        ),
    ),
)

// Agregadores no FIM do arquivo (propriedades de topo são inicializadas em ordem textual).
val lessonsPart6: List<Lesson> by lazy { p6MinLessons + p6AstLessons + p6RegLessons }
val cardsPart6: List<SkillCard> by lazy { p6Cards }
