package com.compilandia.content.episodes

import com.compilandia.content.Fala
import com.compilandia.content.Speaker
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Antes da aula, o Bug ataca: 10 segundos para tentar sozinho. Errar aqui prepara a explicação. */
data class Abertura(val skill: Skill, val manchete: String, val desafio: String, val fala: Fala, val opcoes: List<String>, val certa: Int, val depois: Fala)

private fun ab(s: Skill, manchete: String, desafio: String, fala: String, opcoes: List<String>, certa: Int, depois: String) =
    Abertura(s, manchete, desafio, Fala(Speaker.BYTE, fala), opcoes, certa, Fala(Speaker.BYTE, depois))

object Aberturas {
    val todas = listOf(
        ab(Skill.PHASES, tx("epnovos_bug_embaralhou_fases"), tx("epnovos_lexica_semantica"), tx("epnovos_antes_aula_qual_fase_vem"), listOf(tx("comum_sintatica_2"), tx("comum_otimizacao"), tx("epnovos_geracao")), 0, tx("epnovos_sintatica_tokens_viram_arvore_antes")),
        ab(Skill.TOKENS, tx("epnovos_bug_colou_tokens"), "x>=10", tx("epnovos_antes_aula_quantos_tokens_tem"), listOf("2", "3", "5"), 1, tx("epnovos_tres_x_maior_ou_igual")),
        ab(Skill.REGEX_DFA, tx("epnovos_bug_soltou_regex"), "a*b", tx("epnovos_antes_aula_qual_cadeia_casa"), listOf("ba", "aab", "abb"), 1, tx("epnovos_vale_b_quantos_quiser_b")),
        ab(Skill.NFA_TO_DFA, tx("epnovos_bug_duplicou_setas"), tx("epnovos_q0_q1_q0_q2"), tx("epnovos_antes_aula_lendo_para_onde"), listOf("q1", "q2", tx("epnovos_dois")), 2, tx("epnovos_para_dois_afd_guarda_esse")),
        ab(Skill.DFA_MINIMIZATION, tx("epnovos_bug_clonou_estado"), "q1 = q2", tx("epnovos_antes_aula_dois_estados_fazem"), listOf("apagar", "juntar", "deixar"), 1, tx("epnovos_juntar_estado_so_faz_mesmo")),
        ab(Skill.LEX_FLEX, tx("epnovos_bug_trocou_regras"), tx("epnovos_z_antes_if"), tx("epnovos_antes_aula_palavra_if_vira"), listOf("ID", "IF", "erro"), 0, tx("epnovos_vira_id_no_empate_ganha")),
        ab(Skill.CFG_DERIVATION, tx("epnovos_bug_comeu_troca"), "S -> a S b | c", tx("epnovos_antes_aula_qual_frase_sai"), listOf(tx("epnovos_c_b"), tx("epnovos_b_c"), "c c"), 0, tx("epnovos_vale_c_b_vira_b")),
        ab(Skill.AMBIGUITY, tx("epnovos_bug_fez_duas_arvores"), "2 + 3 * 4", tx("epnovos_antes_aula_se_mais_rodar"), listOf("14", "20", "24"), 1, tx("epnovos_vinte_duas_arvores_dois_resultados")),
        ab(Skill.LEFT_RECURSION, tx("epnovos_bug_prendeu_parser"), "E -> E + T | T", tx("epnovos_antes_aula_que_parser_descendente"), listOf(tx("epnovos_le"), tx("epnovos_gira_sem_ler"), "aceita"), 1, tx("epnovos_gira_sem_ler_nada_ele")),
        ab(Skill.LEFT_FACTORING, tx("epnovos_bug_grudou_dois_comecos"), "A -> a b | a c", tx("epnovos_antes_aula_olhando_so_para"), listOf("sim", tx("comum_nao"), tx("epnovos_vezes")), 1, tx("epnovos_nao_duas_comecam_igual")),
        ab(Skill.FIRST, tx("epnovos_bug_comeu_first"), "S -> a b | c", tx("epnovos_antes_aula_que_pode_abrir"), listOf(tx("epnovos_c"), tx("comum_b_3"), tx("epnovos_so")), 0, tx("epnovos_c_cada_opcao_abre_com")),
        ab(Skill.FOLLOW, tx("epnovos_bug_comeu_follow"), "E -> T + E | T", tx("epnovos_antes_aula_que_pode_vir"), listOf("+", "id", "ε"), 0, tx("epnovos_mais_olhe_que_esta_direita")),
        ab(Skill.LL1_TABLE, tx("epnovos_bug_riscou_tabela"), "E -> id | ( E )", tx("epnovos_antes_aula_com_id_na"), listOf("id", tx("comum_e"), "nenhuma"), 0, tx("epnovos_opcao_id_cada_token_aponta")),
        ab(Skill.LL1_PARSING, tx("epnovos_bug_derrubou_pilha"), tx("epnovos_topo_id_entrada_id"), tx("epnovos_antes_aula_topo_entrada_iguais"), listOf("casar", "expandir", "erro"), 0, tx("epnovos_casar_dois_saem_maquina_anda")),
        ab(Skill.RECURSIVE_DESCENT, tx("epnovos_bug_apagou_funcao"), "E -> T X", tx("epnovos_antes_aula_quantas_funcoes_funcao"), listOf("1", "2", "3"), 1, tx("epnovos_duas_t_x")),
        ab(Skill.ERROR_RECOVERY, tx("epnovos_bug_plantou_erro"), "x = = 2;  y = 3;", tx("epnovos_antes_aula_onde_parser_recomeca"), listOf(tx("epnovos_no_segundo"), "depois do ;", tx("epnovos_no_fim")), 1, tx("epnovos_depois_ponto_virgula_ali_comeca")),
        ab(Skill.SHIFT_REDUCE, tx("epnovos_bug_travou_esteira"), tx("epnovos_pilha_t_entrada_fim"), tx("epnovos_antes_aula_entrada_acabou_que"), listOf("reduz", "empilha", "para"), 0, tx("epnovos_reduz_nao_ha_mais_nada")),
        ab(Skill.LR0_ITEMS, tx("epnovos_bug_moveu_ponto"), "E -> E . + T", tx("epnovos_antes_aula_que_maquina_espera"), listOf("+", "E", "T"), 0, tx("epnovos_mais_ponto_fica_logo_antes")),
        ab(Skill.SLR_TABLE, tx("epnovos_bug_cruzou_duas_acoes"), tx("epnovos_empilhar_reduzir"), tx("epnovos_antes_aula_casa_com_duas"), listOf("conflito", tx("epnovos_erro_lexico"), "normal"), 0, tx("epnovos_conflito_tabela_deu_duas_ordens")),
        ab(Skill.LALR, tx("epnovos_bug_juntou_estados"), "LR(1)   LALR   SLR", tx("epnovos_antes_aula_qual_deles_costuma"), listOf("LR(1)", "LALR", "SLR"), 0, tx("epnovos_lr_1_lalr_nasce_juntando")),
        ab(Skill.YACC_BISON, tx("epnovos_bug_trocou_left"), "%left '*'\n%left '+'", tx("epnovos_antes_aula_quem_liga_mais"), listOf("+", "*", "empate"), 0, tx("epnovos_mais_quanto_mais_embaixo_mais")),
        ab(Skill.ATTRIBUTES_TYPES, tx("epnovos_bug_apagou_tipo"), "2 + 1.5", tx("epnovos_antes_aula_qual_tipo_resultado"), listOf("int", "float", "erro"), 1, tx("epnovos_float_compilador_converte_inteiro_antes")),
        ab(Skill.SCOPE_SYMBOLS, tx("epnovos_bug_escondeu_x"), "int x; { int x; x = 1; }", tx("epnovos_antes_aula_esse_x_igual"), listOf(tx("epnovos_dentro"), tx("epnovos_fora"), tx("epnovos_nos_dois")), 0, tx("epnovos_no_dentro_busca_comeca_pelo")),
        ab(Skill.AST_BUILD, tx("epnovos_bug_podou_arvore"), tx("epnovos_b"), tx("epnovos_antes_aula_parenteses_entram_na"), listOf("sim", tx("comum_nao"), tx("epnovos_so_2")), 1, tx("epnovos_nao_forma_arvore_ja_mostra")),
        ab(Skill.TAC, tx("epnovos_bug_quebrou_conta"), tx("comum_b_c"), tx("epnovos_antes_aula_quantas_linhas_tres"), listOf("1", "2", "3"), 1, tx("epnovos_duas_conta_por_linha")),
        ab(Skill.OPTIMIZATION, tx("epnovos_bug_deixou_conta_toa"), "x = 2 * 3;", tx("epnovos_antes_aula_compilador_pode_fazer"), listOf("sim", tx("comum_nao"), tx("epnovos_so_em_c")), 0, tx("epnovos_pode_ele_ja_escreve_seis")),
        ab(Skill.RUNTIME, tx("epnovos_bug_encheu_pilha"), tx("epnovos_f_3_chama_f_2"), tx("epnovos_antes_aula_quem_sai_primeiro"), listOf("f(3)", "f(2)", "nenhum"), 1, tx("epnovos_f_dois_ultimo_entrar_sai")),
        ab(Skill.CODEGEN_MIPS, tx("epnovos_bug_roubou_registrador"), "add \$t0, \$t1, x", tx("epnovos_antes_aula_essa_instrucao_vale"), listOf("sim", tx("comum_nao"), tx("epnovos_so_com_sw")), 1, tx("epnovos_nao_vale_add_so_usa")),
        ab(Skill.REG_ALLOC, tx("epnovos_bug_sumiu_com_registrador"), tx("epnovos_t1_t2_vivos_juntos"), tx("epnovos_antes_aula_eles_podem_dividir"), listOf("sim", tx("comum_nao"), tx("epnovos_vezes")), 1, tx("epnovos_nao_podem_apagaria_outro")),
    )

    fun of(s: Skill): Abertura? = todas.firstOrNull { it.skill == s }

    fun falas(): List<Fala> = todas.flatMap { listOf(it.fala, it.depois) }
}
