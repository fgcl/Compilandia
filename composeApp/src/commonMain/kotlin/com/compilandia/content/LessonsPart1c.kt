package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.GrammarBank
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 1c do conteúdo: LEFT_RECURSION, LEFT_FACTORING (Ponte da Transformação).

private val p1cExpr: String get() = GrammarBank.exprLeftRec.text
private val p1cExprLL: String get() = GrammarBank.exprLL1.text
private const val P1C_SA = "S -> S a | b"
private const val P1C_ELSE = "S -> i E t S | i E t S e S | a\nE -> b"
private const val P1C_ELSE_FAT = "S -> i E t S S' | a\nS' -> e S | ε\nE -> b"
private const val P1C_STMTS = "L -> S ; L | S\nS -> id = E | print E\nE -> id | num"
private const val P1C_STMTS_FAT = "L -> S L'\nL' -> ; L | ε\nS -> id = E | print E\nE -> id | num"

// ───────────────────────────── LEFT_RECURSION ─────────────────────────────

private val p1cLeftRec: List<Lesson> = listOf(
    Lesson(
        Skill.LEFT_RECURSION, Style.STORY, tx("lessonspart1c_cachorro_que_corre_atras_rabo"),
        listOf(
            Say(NARRADOR, tx("lessonspart1c_reparticao_publica_placa_na_parede")),
            Say(BYTE, tx("lessonspart1c_mas_isso_impossivel_eu_nunca")),
            Say(PROF, tx("lessonspart1c_pois_exatamente_isso_que_acontece")),
            Text(tx("lessonspart1c_na_descida_recursiva_cada_nao")),
            Code("void E() {\n    E();          // chama a si mesma sem consumir NADA\n    match('+');\n    T();\n}", tx("lessonspart1c_laco_infinito_estouro_pilha")),
            Say(PILHA, tx("lessonspart1c_eu_so_vejo_topo_ele")),
            Callout(WATCH_OUT, tx("lessonspart1c_problema_nao_ter_recursao_recursao")),
            Say(BYTE, tx("lessonspart1c_como_sai_ciclo")),
            Say(PROF, tx("lessonspart1c_olha_que_t_t_realmente")),
            Code("E  -> T E'\nE' -> + T E' | ε", tx("lessonspart1c_mesma_linguagem_recursao_agora_direita")),
            Text(tx("lessonspart1c_como_consertar_placa_reparticao_apresente")),
            Visual(VK.LEFT_RECURSION, grammar = p1cExpr, caption = tx("lessonspart1c_antes_depois_t_ganharam_ajudantes")),
            Predict(
                tx("lessonspart1c_gramatica_nova_gera_mesma_linguagem"),
                listOf(tx("lessonspart1c_sim_mesmas_cadeias"), tx("lessonspart1c_nao_gera_menos_cadeias")),
                0,
                tx("lessonspart1c_duas_geram_t_t_muda"),
            ),
            Callout(REAL_WORLD, tx("lessonspart1c_analisadores_lr_yacc_bison_adoram")),
            Reveal(tx("lessonspart1c_por_que_recursao_esquerda_trava"), tx("lessonspart1c_porque_funcao_chama_novo_antes")),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.VISUAL, tx("lessonspart1c_cirurgia_ao_vivo"),
        listOf(
            Say(PROF, tx("lessonspart1c_vamos_operar_widget_mostra_gramatica")),
            Visual(VK.LEFT_RECURSION, grammar = P1C_SA, caption = "S -> S a | b vira S -> b S' e S' -> a S' | ε."),
            Text(tx("lessonspart1c_leia_assim_que_vem_depois")),
            Visual(VK.DERIVATION, grammar = P1C_SA, input = tx("comum_b"), caption = tx("lessonspart1c_gramatica_original_arvore_cresce_para")),
            Visual(VK.DERIVATION, grammar = "S -> b S'\nS' -> a S' | ε", input = tx("comum_b"), caption = tx("lessonspart1c_gramatica_nova_arvore_cresce_para")),
            Predict(
                tx("lessonspart1c_qual_das_duas_analisador_descendente"),
                listOf(tx("lessonspart1c_original"), tx("lessonspart1c_nova")),
                1,
                tx("lessonspart1c_na_nova_comeca_consumindo_b"),
            ),
            Visual(VK.LEFT_RECURSION, grammar = p1cExpr, caption = tx("lessonspart1c_agora_com_duas_recursoes_t")),
            Text(tx("lessonspart1c_repare_f_nao_mudou_ela")),
            Visual(VK.LL1_TABLE, grammar = p1cExpr, caption = tx("lessonspart1c_tabela_ll_1_gramatica_original")),
            Visual(VK.LL1_TABLE, grammar = p1cExprLL, caption = tx("lessonspart1c_tabela_gramatica_transformada_producao_por")),
            Reveal(tx("lessonspart1c_por_que_tabela_original_tinha"), tx("solta_lessonspart1c_1")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1c_p_em_obrigatorio_r_sim")),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.WORKED, tx("lessonspart1c_eliminando_passo_passo"),
        listOf(
            Say(PROF, tx("lessonspart1c_receita_1_m_1_n")),
            Code("E -> E + T | E - T | T", tx("lessonspart1c_exercicio_resolvido")),
            Steps(
                tx("comum_resolvido"),
                listOf(
                    tx("lessonspart1c_1_separe_alternativas_que_comecam"),
                    tx("lessonspart1c_2_recursivas_t_t_1"),
                    tx("lessonspart1c_3_nao_recursiva_t_1"),
                    tx("lessonspart1c_4_novo_cada_seguido_t"),
                    tx("lessonspart1c_5_novo_cada_seguido_mais"),
                ),
            ),
            Visual(VK.LEFT_RECURSION, grammar = "E -> E + T | E - T | T\nT -> id", caption = tx("lessonspart1c_confira_com_motor")),
            Text(tx("lessonspart1c_agora_esmaecido_exemplo_livro_dragao")),
            Reveal(tx("lessonspart1c_quem_sao"), tx("lessonspart1c_1_c_2_d_que")),
            Reveal(tx("lessonspart1c_quem_sao_atencao_ao"), tx("lessonspart1c_1_b_d_2_alternativa")),
            Reveal(tx("lessonspart1c_escreva_gramatica_final"), "A → b d A' | A'\nA' → c A' | a d A' | ε"),
            Visual(VK.LEFT_RECURSION, grammar = "A -> A c | A a d | b d | ε", caption = tx("lessonspart1c_confira_virou")),
            Callout(WATCH_OUT, tx("lessonspart1c_erro_no_1_esquecer_em")),
            Callout(EXAM_TIP, tx("lessonspart1c_se_questao_pedir_elimine_recursao")),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.SOCRATIC, tx("lessonspart1c_descubra_receita_sozinho"),
        listOf(
            Code(P1C_SA),
            Predict(
                tx("lessonspart1c_quais_cadeias_essa_gramatica_gera"),
                listOf(tx("lessonspart1c_b_ba_baa_baaa"), tx("lessonspart1c_ab_aab"), tx("lessonspart1c_ab_aabb_aaabbb")),
                0,
                tx("lessonspart1c_b_b_sempre_fica_na"),
            ),
            Reveal(tx("lessonspart1c_escreva_essa_linguagem_como_expressao"), tx("lessonspart1c_ba_b_depois_quantos_quiser")),
            Predict(
                tx("lessonspart1c_analisador_descendente_lendo_b_precisa"),
                listOf(tx("comum_sim"), tx("lessonspart1c_nao_teria_que_adivinhar_futuro")),
                1,
                tx("lessonspart1c_quantidade_depende_quantos_vem_depois"),
            ),
            Say(PROF, tx("lessonspart1c_entao_vamos_gerar_outro_jeito")),
            Predict(
                tx("lessonspart1c_qual_gramatica_gera_b_sem"),
                listOf("S -> b S'  e  S' -> a S' | ε", "S -> S' b  e  S' -> a S' | ε", "S -> a S | b"),
                0,
                tx("lessonspart1c_segunda_terceira_geram_b_b"),
            ),
            Reveal(tx("lessonspart1c_generalize_que_fazer_com"), "A → β A' e A' → α A' | ε."),
            Predict(
                tx("lessonspart1c_b_c_d_tem_recursao"),
                listOf(tx("lessonspart1c_nao_nenhuma_producao_comeca_com"), tx("lessonspart1c_sim_indireta_c")),
                1,
                tx("lessonspart1c_recursao_esquerda_indireta_receita_imediata"),
            ),
            Reveal(
                tx("lessonspart1c_como_eliminar_essa_recursao_indireta"),
                tx("lessonspart1c_ordene_em_c_substitua_pelas"),
            ),
            Visual(VK.LEFT_RECURSION, grammar = "S -> A a | b\nA -> A a c | b c | d", caption = tx("lessonspart1c_ultimo_passo_recursao_ja_imediata")),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.FORMAL, tx("lessonspart1c_algoritmo_eliminacao"),
        listOf(
            Text(tx("lessonspart1c_definicao_gramatica_recursiva_esquerda_se")),
            Text(tx("lessonspart1c_metodos_analise_descendente_nao_conseguem")),
            Code(
                tx("lessonspart1c_recursao_imediata_1_2_m"),
            ),
            Code(
                tx("lessonspart1c_entrada_g_sem_ciclos_sem"),
                tx("lessonspart1c_algoritmo_4_19_livro_dragao"),
            ),
            Callout(WATCH_OUT, tx("lessonspart1c_pre_condicao_sem_ciclos_sem")),
            Predict(
                tx("lessonspart1c_ao_final_algoritmo_toda_producao"),
                listOf("j < i", "j > i", "j = i"),
                1,
                tx("lessonspart1c_depois_iteracao_i_qualquer_ai"),
            ),
            Text(tx("lessonspart1c_exemplo_livro_b_c_d")),
            Visual(VK.LEFT_RECURSION, grammar = "S -> A a | b\nA -> A c | A a d | b d | ε", caption = tx("lessonspart1c_resultado_exemplo_depois_substituicao")),
            Callout(EXAM_TIP, tx("lessonspart1c_duas_armadilhas_1_nova_gramatica")),
            Reveal(tx("lessonspart1c_por_que_exigir_que_nenhum"), tx("lessonspart1c_porque_se_i_comecasse_com")),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.MNEMONIC, tx("lessonspart1c_beta_comeca_alfa_repete_epsilon"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1c_beta_comeca_alfa_repete_epsilon_2")),
            Callout(MNEMONIC, tx("lessonspart1c_rabo_esquerda_morde_em_morde")),
            Text(tx("lessonspart1c_vem_antes_comeco_cadeia_parte")),
            Callout(MNEMONIC, tx("lessonspart1c_linha_while_disfarcado_enquanto_aparecer")),
            Text(tx("lessonspart1c_palacio_memoria_na_cabeceira_ponte")),
            Predict(
                tx("lessonspart1c_elimine_recursao_l_l_id"),
                listOf("L -> id L'  e  L' -> , id L' | ε", "L -> , id L'  e  L' -> id L' | ε", "L -> id , L | id"),
                0,
                tx("lessonspart1c_id_comeca_id_repete_encerra"),
            ),
            Visual(VK.LEFT_RECURSION, grammar = "L -> L , id | id", caption = tx("lessonspart1c_lista_identificadores_separados_por_virgula")),
            Callout(WATCH_OUT, tx("lessonspart1c_confusoes_classicas_1_trocar_2")),
            Callout(MNEMONIC, tx("lessonspart1c_ll_le_l_eft_derivacao")),
            Reveal(tx("lessonspart1c_recite_receita_aplicada_t_t"), tx("lessonspart1c_beta_comeca_t_alfa_repete")),
        ),
    ),
)

// ───────────────────────────── LEFT_FACTORING ─────────────────────────────

private val p1cFactoring: List<Lesson> = listOf(
    Lesson(
        Skill.LEFT_FACTORING, Style.STORY, tx("lessonspart1c_menu_telefone_banco"),
        listOf(
            Say(NARRADOR, tx("lessonspart1c_voce_liga_para_banco_voz")),
            Say(BYTE, tx("lessonspart1c_eu_digo_boleto_robo_fica")),
            Say(PROF, tx("lessonspart1c_pois_ouvindo_so_boleto_sistema")),
            Text(tx("lessonspart1c_analisador_preditivo_tem_mesmo_problema")),
            Code("S -> if E then S\n   | if E then S else S", tx("lessonspart1c_duas_comecam_com_if_then")),
            Say(PROF, tx("lessonspart1c_solucao_ura_esperta_diga_boleto")),
            Code("S  -> if E then S S'\nS' -> else S | ε", tx("lessonspart1c_prefixo_comum_na_frente_diferencas")),
            Callout(MNEMONIC, tx("lessonspart1c_lembra_escola_ab_ac_b")),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE, caption = tx("lessonspart1c_i_if_t_then_else")),
            Predict(
                tx("lessonspart1c_depois_fatoracao_ao_ver_if"),
                listOf(tx("lessonspart1c_sim_so_alternativa_comeca_com"), tx("comum_nao_2")),
                0,
                tx("lessonspart1c_agora_ha_unica_alternativa_com"),
            ),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart1c_p_fatoracao_muda_linguagem_r")),
            Callout(WATCH_OUT, tx("lessonspart1c_fatorar_nao_tira_ambiguidade_else")),
            Reveal(tx("lessonspart1c_na_analogia_que_submenu"), tx("lessonspart1c_novo_nao_terminal_guarda_restos")),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.VISUAL, tx("lessonspart1c_fatorando_ao_vivo"),
        listOf(
            Say(PROF, tx("lessonspart1c_olhe_antes_depois_primeiro_classico")),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE),
            Text(tx("lessonspart1c_motor_procurou_maior_prefixo_comum")),
            Visual(VK.DERIVATION, grammar = P1C_ELSE_FAT, input = tx("lessonspart1c_i_b_t"), caption = tx("lessonspart1c_derivando_com_gramatica_fatorada")),
            Visual(VK.LL1_TABLE, grammar = P1C_ELSE, caption = tx("lessonspart1c_antes_duas_producoes_disputando_celula")),
            Visual(VK.LL1_TABLE, grammar = P1C_ELSE_FAT, caption = tx("lessonspart1c_depois_i_resolvida_mas_olhe")),
            Predict(
                tx("lessonspart1c_por_que_tem_conflito"),
                listOf(tx("lessonspart1c_porque_first_tambem_follow"), tx("lessonspart1c_porque_fatoracao_foi_feita_errado")),
                0,
                tx("solta_lessonspart1c_2"),
            ),
            Visual(VK.LEFT_FACTORING, grammar = P1C_STMTS, caption = tx("lessonspart1c_lista_comandos_l_l_tambem")),
            Text(tx("lessonspart1c_resultado_l_l_l_l")),
            Reveal(tx("lessonspart1c_quando_analisador_escolhe_l"), tx("lessonspart1c_quando_proximo_token_nao_quando")),
            Visual(VK.LL1_TABLE, grammar = P1C_STMTS_FAT, caption = tx("lessonspart1c_sem_conflitos_agora_ll_1")),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.WORKED, tx("lessonspart1c_fatorando_passo_passo"),
        listOf(
            Say(PROF, tx("lessonspart1c_receita_para_cada_ache_maior")),
            Code("A -> a b c | a b d | a e | f", tx("lessonspart1c_exercicio_resolvido")),
            Steps(
                tx("comum_resolvido"),
                listOf(
                    tx("lessonspart1c_1_alternativas_que_comecam_com"),
                    tx("lessonspart1c_2_maior_prefixo_comum_tres"),
                    tx("solta_lessonspart1c_3"),
                    tx("lessonspart1c_4_repita_em_b_c"),
                    tx("solta_lessonspart1c_4"),
                    tx("lessonspart1c_6_fim_nenhum_par_alternativas"),
                ),
            ),
            Visual(VK.LEFT_FACTORING, grammar = "A -> a b c | a b d | a e | f", caption = tx("lessonspart1c_motor_repete_rodadas_ate_estabilizar")),
            Text(tx("lessonspart1c_sua_vez_esmaecido_c_id")),
            Reveal(tx("lessonspart1c_primeira_rodada_qual_maior_prefixo"), tx("lessonspart1c_so_id_segundo_simbolo_numa")),
            Reveal(tx("lessonspart1c_segunda_rodada_em_c"), "`( )` e `( E )` compartilham `(`. C' → = E | ( C''; C'' → ) | E )."),
            Visual(VK.LEFT_FACTORING, grammar = "C -> id = E | id ( ) | id ( E )\nE -> num", caption = tx("comum_confira")),
            Callout(WATCH_OUT, tx("lessonspart1c_nao_pare_na_primeira_rodada")),
            Callout(EXAM_TIP, tx("lessonspart1c_use_maior_prefixo_comum_pelo")),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.SOCRATIC, tx("lessonspart1c_por_que_adiar_decisao"),
        listOf(
            Code("A -> a b | a c"),
            Predict(
                tx("lessonspart1c_analisador_preditivo_com_1_token"),
                listOf("A -> a b", "A -> a c", tx("lessonspart1c_ainda_nao_para_saber")),
                2,
                tx("lessonspart1c_duas_comecam_com_so_segundo"),
            ),
            Predict(
                tx("lessonspart1c_se_ele_pudesse_olhar_2"),
                listOf(tx("lessonspart1c_ainda_nao_daria"), tx("lessonspart1c_daria_2o_token_b_ou")),
                1,
                tx("lessonspart1c_isso_seria_ll_2_mas"),
            ),
            Reveal(tx("lessonspart1c_como_reescrever_para_decidir_so"), tx("solta_lessonspart1c_5")),
            Predict(
                tx("lessonspart1c_em_b_b_c_qual"),
                listOf("a", tx("lessonspart1c_b_2"), tx("lessonspart1c_b_c")),
                1,
                tx("lessonspart1c_duas_comecam_com_b_segunda"),
            ),
            Reveal(tx("lessonspart1c_fatore"), tx("solta_lessonspart1c_6")),
            Predict(
                tx("lessonspart1c_esta_gramatica_tem_prefixo_comum"),
                listOf(tx("lessonspart1c_nao_b_sao_diferentes"), tx("lessonspart1c_sim_escondido_depois_expandir_duas")),
                1,
                tx("lessonspart1c_fatoracao_olho_so_ve_simbolos"),
            ),
            Visual(VK.LL1_TABLE, grammar = "S -> A x | B y\nA -> c\nB -> c", caption = tx("lessonspart1c_conflito_em_c_mesmo_sem")),
            Reveal(tx("lessonspart1c_qual_relacao_entre_fatoracao_esquerda"), tx("lessonspart1c_fatoracao_remove_tipo_conflito_alternativas")),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.FORMAL, tx("lessonspart1c_algoritmo_fatoracao_esquerda"),
        listOf(
            Text(tx("lessonspart1c_fatoracao_esquerda_transformacao_gramatica_u")),
            Code(
                tx("lessonspart1c_entrada_gramatica_g_saida_gramatica"),
                tx("lessonspart1c_algoritmo_4_21_livro_dragao"),
            ),
            Text(tx("lessonspart1c_exemplo_livro_i_t_i")),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE),
            Predict(
                tx("lessonspart1c_fatorar_1_2_preserva_linguagem"),
                listOf(tx("comum_sim_sempre"), tx("lessonspart1c_nao_perde_cadeias")),
                0,
                tx("lessonspart1c_com_1_2_gera_exatamente"),
            ),
            Predict(
                tx("lessonspart1c_gramatica_fatorada_sem_recursao_esquerda"),
                listOf(tx("comum_sim"), tx("comum_nao_2")),
                1,
                tx("lessonspart1c_sao_condicoes_necessarias_nao_suficientes"),
            ),
            Callout(EXAM_TIP, tx("lessonspart1c_ordem_segura_numa_questao_torne")),
            Callout(WATCH_OUT, tx("lessonspart1c_nao_confunda_fatoracao_trata_prefixos")),
            Reveal(tx("lessonspart1c_escreva_forma_geral_fatoracao"), "A → αβ₁ | … | αβₙ | γ  ⟹  A → αA' | γ  e  A' → β₁ | … | βₙ."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.MNEMONIC, tx("lessonspart1c_coloque_em_evidencia"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart1c_ab_ac_b_c_fatoracao")),
            Callout(MNEMONIC, tx("lessonspart1c_primeiro_que_igual_depois_que")),
            Callout(MNEMONIC, tx("lessonspart1c_recursao_rabo_fatoracao_gemeos_na")),
            Text(tx("lessonspart1c_palacio_memoria_outro_lado_ponte")),
            Predict(
                tx("lessonspart1c_qual_gramatica_precisa_fatoracao_nao"),
                listOf("E -> E + T | T", "S -> id ( ) | id = num", "S -> a S b | ε"),
                1,
                tx("lessonspart1c_duas_alternativas_comecam_com_id"),
            ),
            Visual(VK.LEFT_FACTORING, grammar = "S -> id ( ) | id = num", caption = tx("lessonspart1c_gemeos_separados_id_em_evidencia")),
            Callout(WATCH_OUT, tx("lessonspart1c_confusoes_classicas_1_achar_que")),
            Callout(MNEMONIC, tx("lessonspart1c_duas_cirurgias_ponte_antes_templo")),
            Reveal(tx("lessonspart1c_fatore_x_y_z_x"), tx("lessonspart1c_prefixo_comum_x_y_x")),
        ),
    ),
)

internal val p1cLessons: List<Lesson> = p1cLeftRec + p1cFactoring
