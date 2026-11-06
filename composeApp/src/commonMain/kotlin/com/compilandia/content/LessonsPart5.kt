package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Parte 5 do conteúdo: Forja do Código, back-end — RUNTIME (LessonsPart5a.kt) e CODEGEN_MIPS (LessonsPart5b.kt).

/**
 * Assembly MIPS em strings cruas: escreva '§' no lugar de '$' (senão "$t0" vira template do Kotlin).
 * Ex.: p5Asm("lw §t1, a") == "lw \$t1, a".
 */
internal fun p5Asm(s: String): String = s.trimIndent().replace('§', '$')

private val p5Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.RUNTIME,
        oneLiner = tx("lessonspart5_em_tempo_execucao_cada_chamada"),
        hook = tx("lessonspart5_pilha_pratos_prato_novo_em"),
        feynmanPrompt = tx("lessonspart5_explique_para_amigo_12_anos"),
        keyTerms = listOf("pilha", "ativa", "retorno", "chamad", "local", "recurs"),
        commonMistakes = listOf(
            tx("lessonspart5_achar_que_cada_funcao_tem"),
            tx("lessonspart5_esquecer_quadro_main_ao_contar"),
            tx("lessonspart5_confundir_vinculo_controle_quem_chamou"),
            tx("lessonspart5_confundir_endereco_retorno_instrucao_com"),
            "Não salvar \$ra na pilha antes de um jal dentro de uma função que chama outra",
            tx("lessonspart5_devolver_ponteiro_para_variavel_local"),
        ),
        flashcards = listOf(
            tx("lessonspart5_que_registro_ativacao") to tx("lessonspart5_bloco_memoria_chamada_parametros_valor"),
            tx("lessonspart5_por_que_pilha_nao_alocacao") to tx("lessonspart5_recursao_cada_chamada_precisa_sua"),
            tx("lessonspart5_vinculo_controle_aponta_para") to tx("lessonspart5_quadro_chamador_cadeia_dinamica"),
            tx("lessonspart5_vinculo_acesso_aponta_para") to tx("lessonspart5_quadro_escopo_que_envolve_funcao"),
            tx("lessonspart5_altura_maxima_pilha_em_fat") to tx("lessonspart5_4_quadros_main_fat_3"),
            tx("lessonspart5_que_jal_faz") to "Guarda PC + 4 em \$ra e desvia para a função",
            tx("lessonspart5_quem_aloca_locais_chamador_ou") to tx("lessonspart5_chamado_so_ele_sabe_quantas"),
            tx("lessonspart5_onde_vivem_dados_que_sobrevivem") to tx("lessonspart5_no_heap_new_malloc"),
            tx("lessonspart5_para_que_lado_pilha_cresce") to "Para endereços menores: addi \$sp, \$sp, -8 abre espaço",
        ),
    ),
    SkillCard(
        skill = Skill.CODEGEN_MIPS,
        oneLiner = tx("lessonspart5_gerador_traduz_cada_instrucao_tac"),
        hook = tx("lessonspart5_busca_busca_mistura_guarda_falso"),
        feynmanPrompt = tx("lessonspart5_explique_para_amigo_12_anos_2"),
        keyTerms = listOf("registrador", "lw", "sw", "data", "text", "desvi", "syscall"),
        commonMistakes = listOf(
            tx("lessonspart5_usar_variavel_memoria_direto_no"),
            tx("lessonspart5_esquecer_j_l1_no_fim"),
            tx("lessonspart5_reaproveitar_mesmo_rotulo_em_dois"),
            "Esquecer de declarar os temporários na .data ou de encerrar com li \$v0, 10 + syscall",
            "Confundir t1 (variável na memória) com \$t1 (registrador)",
        ),
        flashcards = listOf(
            tx("lessonspart5_tac_t_b_no_estilo") to "lw \$t1, a / lw \$t2, b / add \$t3, \$t1, \$t2 / sw \$t3, t",
            tx("lessonspart5_como_carregar_constante") to "li \$t2, 5",
            "Para que serve \$zero?" to tx("lessonspart5_vale_sempre_0_comparar_com"),
            tx("lessonspart5_esqueleto_if_else") to "cond; beq \$t3, \$zero, Lelse; then; j Lfim; Lelse: else; Lfim:",
            tx("lessonspart5_esqueleto_while") to "Lini: cond; beq \$t3, \$zero, Lfim; corpo; j Lini; Lfim:",
            tx("lessonspart5_imprimir_inteiro_x") to "li \$v0, 1 / lw \$a0, x / syscall",
            tx("lessonspart5_imprimir_string_msg") to "li \$v0, 4 / la \$a0, msg / syscall",
            tx("lessonspart5_encerrar_programa") to "li \$v0, 10 / syscall",
            tx("lessonspart5_ideia_alocacao_registradores") to tx("lessonspart5_manter_valores_em_registradores_para"),
        ),
    ),
)

// Agregadores no FIM do arquivo (propriedades de topo são inicializadas em ordem textual).
val lessonsPart5: List<Lesson> = p5RuntimeLessons + p5MipsLessons
val cardsPart5: List<SkillCard> = p5Cards
