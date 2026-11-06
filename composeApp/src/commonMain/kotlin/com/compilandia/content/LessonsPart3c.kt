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

/** Programa de exemplo com três blocos aninhados (usado em várias aulas de escopo). */
internal const val P3_SCOPE_PROGRAM = """{                        // bloco 0
  int x;  int y;         // (1) x    (2) y
  x = 1;
  {                      // bloco 1
    float x;             // (3) x
    x = y + 1;
    {                    // bloco 2
      int y;             // (4) y
      print(x + y);
    }
    print(y);
  }
  print(x);
}"""

// =====================================================================
// SCOPE_SYMBOLS — Escopo e tabela de símbolos
// =====================================================================

internal val scopeLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.STORY, tx("lessonspart3c_porteiro_condominio"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_cidade_semantica_bairro_dos_escopos")),
            Say(BYTE, tx("lessonspart3c_tem_dois_x_no_meu")),
            Say(PROF, tx("lessonspart3c_mesmo_jeito_que_porteiro_acha")),
            Text(tx("lessonspart3c_porteiro_procura_primeiro_no_seu")),
            Text(tx("lessonspart3c_se_tem_joao_no_seu")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3c_apartamento_predio_condominio_blocos_aninhad"),
                    tx("lessonspart3c_lista_moradores_cada_lugar_tabela"),
                    tx("lessonspart3c_procurar_dentro_para_fora_regra"),
                    tx("lessonspart3c_mudou_se_ape_sair_bloco"),
                    tx("lessonspart3c_nenhum_joao_em_lugar_nenhum"),
                ),
            ),
            Say(PILHA, tx("lessonspart3c_listas_ficam_empilhadas_entrou_num")),
            Code(P3_SCOPE_PROGRAM, tx("lessonspart3c_condominio_codigo")),
            Predict(
                tx("lessonspart3c_na_linha_print_x_y"),
                listOf(tx("lessonspart3c_x_1_y_2"), tx("lessonspart3c_x_3_y_4"), tx("lessonspart3c_x_3_y_2")),
                1,
                tx("lessonspart3c_dentro_para_fora_y_esta"),
            ),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_ligue_cada_uso_sua_declaracao")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart3c_lexico_nao_resolve_isso_nao")),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.VISUAL, tx("lessonspart3c_pilha_tabelas_quadro_quadro"),
        listOf(
            Say(PROF, tx("lessonspart3c_vamos_assistir_pilha_tabelas_enquanto")),
            Code(P3_SCOPE_PROGRAM, tx("comum_programa")),
            Steps(
                tx("lessonspart3c_pilha_em_cada_momento_topo"),
                listOf(
                    "Abre bloco 0 → [ {} ]",
                    "int x; int y; → [ {x:int(1), y:int(2)} ]",
                    "Abre bloco 1 → [ {x(1), y(2)} | {} ]",
                    "float x; → [ {x(1), y(2)} | {x:float(3)} ] — o x(3) sombreia o x(1)",
                    tx("lessonspart3c_x_y_1_x_acha"),
                    "Abre bloco 2, int y; → [ ... | {x(3)} | {y:int(4)} ]",
                    tx("lessonspart3c_print_x_y_x_3"),
                    tx("lessonspart3c_fecha_bloco_2_tabela_y"),
                    tx("lessonspart3c_print_y_y_2_fecha"),
                ),
            ),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_agora_com_outro_programa_para")),
            Say(LEXI, tx("lessonspart3c_para_mim_todos_esses_x")),
            Visual(VisualKind.TOKENIZE, input = "int x = 1; { float x = 2.5; print(x); }", caption = tx("lessonspart3c_lexico_ve_id_id_id")),
            Predict(
                tx("lessonspart3c_depois_fechar_bloco_1_alguem"),
                listOf(tx("lessonspart3c_sim_ele_fica_na_tabela"), tx("lessonspart3c_nao_tabela_bloco_1_foi"), tx("lessonspart3c_so_se_for_float")),
                1,
                tx("lessonspart3c_saiu_bloco_tabela_daquele_escopo"),
            ),
            Visual(VisualKind.ATTR_TREE, input = tx("lessonspart3c_z"), caption = tx("lessonspart3c_se_nome_nao_esta_em")),
            Callout(BRAIN_POWER, tx("lessonspart3c_repare_busca_percorre_pilha_topo")),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.WORKED, tx("lessonspart3c_ligando_cada_uso_sua_declaracao"),
        listOf(
            Say(PROF, tx("lessonspart3c_exercicio_tipico_para_cada_uso")),
            Code(P3_SCOPE_PROGRAM, tx("lessonspart3c_programa_declaracoes_numeradas_1_4")),
            Steps(
                tx("lessonspart3c_resolvendo_usos_dentro_para_fora"),
                listOf(
                    tx("lessonspart3c_x_1_bloco_0_x"),
                    tx("lessonspart3c_x_y_1_bloco_1"),
                    tx("lessonspart3c_print_x_y_bloco_2"),
                ),
            ),
            Say(BYTE, tx("lessonspart3c_perai_deixa_ultimos_comigo")),
            Reveal(tx("lessonspart3c_print_y_depois_que_bloco"), tx("lessonspart3c_2_tabela_bloco_2_onde")),
            Reveal(tx("lessonspart3c_print_x_na_ultima_linha"), tx("solta_lessonspart3c_1")),
            Reveal(tx("solta_lessonspart3c_2"), tx("lessonspart3c_erro_redeclaracao_mesmo_nome_declarado")),
            Reveal(tx("lessonspart3c_qual_tipo_expressao_x_y"), tx("lessonspart3c_x_3_float_y_4")),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_pratique_com_programa_sorteado")),
            Callout(EXAM_TIP, tx("lessonspart3c_na_prova_desenhe_pilha_tabelas")),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.SOCRATIC, tx("lessonspart3c_tabela_so_nao_bastaria"),
        listOf(
            Say(PROF, tx("lessonspart3c_vamos_desenhar_tabela_simbolos_juntos")),
            Reveal(tx("lessonspart3c_que_informacao_guardar_para_cada"), tx("lessonspart3c_pelo_menos_nome_tipo_categoria")),
            Reveal(tx("lessonspart3c_por_que_unico_mapa_global"), tx("lessonspart3c_porque_mesmo_nome_pode_ter")),
            Reveal(tx("lessonspart3c_entao_que_fazer_ao_entrar"), tx("lessonspart3c_entrar_criar_tabela_nova_ligada")),
            Say(BYTE, tx("lessonspart3c_pra_procurar_nome_eu_comeco")),
            Reveal(tx("lessonspart3c_por_onde_comeca_busca_quando"), tx("lessonspart3c_comeca_na_tabela_topo_bloco")),
            Predict(
                tx("lessonspart3c_declarar_int_x_quando_ja"),
                listOf(tx("lessonspart3c_erro_redeclaracao"), tx("lessonspart3c_permitido_novo_x_sombreia_externo"), tx("lessonspart3c_ignorado")),
                1,
                tx("lessonspart3c_verificacao_redeclaracao_olha_so_tabela"),
            ),
            Reveal(tx("lessonspart3c_escopo_estatico_lexico_vs_dinamico"), tx("lessonspart3c_estatico_x_certo_decidido_pelo")),
            Reveal(tx("lessonspart3c_em_qual_fase_compilador_usa"), tx("lessonspart3c_lexico_pode_inserir_nomes_analise")),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_teste_suas_respostas")),
            Callout(BRAIN_POWER, tx("lessonspart3c_se_compilador_inserisse_x_bloco")),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.FORMAL, tx("lessonspart3c_escopo_estatico_ambientes_encadeados"),
        listOf(
            Text(tx("lessonspart3c_escopo_declaracao_regiao_programa_em")),
            Code(
                tx("lessonspart3c_classe_env_tabela_mapa_nome"),
                tx("lessonspart3c_tabela_simbolos_encadeada_livro_dragao"),
            ),
            Code(
                tx("lessonspart3c_bloco_salvo_topo_topo_new"),
                tx("lessonspart3c_esquema_traducao_usando_pilha_ambientes"),
            ),
            Steps(
                tx("lessonspart3c_operacoes_custos_com_tabela_hash"),
                listOf(
                    tx("lessonspart3c_entrar_no_escopo_1"),
                    tx("lessonspart3c_inserir_1_esperado_so_no"),
                    tx("lessonspart3c_buscar_profundidade_aninhamento_no_pior"),
                    tx("lessonspart3c_sair_escopo_1_descarta_tabela"),
                ),
            ),
            Predict(
                tx("lessonspart3c_escopo_dinamico_f_usa_x"),
                listOf("1", "2", "erro"),
                1,
                tx("lessonspart3c_dinamico_segue_cadeia_chamadas_ativacao"),
            ),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_escopo_estatico_resposta_vem_so")),
            Callout(EXAM_TIP, tx("lessonspart3c_ao_responder_qual_declaracao_justifique")),
            Callout(WATCH_OUT, tx("lessonspart3c_escopo_sobre_onde_nome_visivel")),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.MNEMONIC, tx("lessonspart3c_cebola_dos_escopos"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_palacio_memoria_cidade_semantica_bairro")),
            Callout(MNEMONIC, tx("lessonspart3c_escopo_cebola_procure_dentro_para")),
            Callout(MNEMONIC, tx("solta_lessonspart3c_3")),
            Callout(MNEMONIC, tx("lessonspart3c_i_b_quatro_operacoes_ntrar")),
            Callout(MNEMONIC, tx("lessonspart3c_sombreamento_quem_esta_mais_perto")),
            Say(BYTE, tx("lessonspart3c_se_eu_declarar_mesmo_nome")),
            Say(PROF, tx("lessonspart3c_ai_nao_sombra_briga_redeclaracao")),
            Predict(
                tx("lessonspart3c_busca_na_pilha_tabelas_onde"),
                listOf(tx("lessonspart3c_base_para_topo"), tx("lessonspart3c_topo_para_base"), tx("lessonspart3c_so_no_topo")),
                1,
                tx("lessonspart3c_topo_bloco_mais_interno_descer"),
            ),
            Visual(VisualKind.SCOPE, caption = tx("lessonspart3c_descasque_cebola_para_cada_uso")),
            Reveal(tx("lessonspart3c_recite_regra_escopo_estatico_numa"), tx("lessonspart3c_uso_x_refere_se_declaracao")),
            Callout(FIRESIDE, tx("lessonspart3c_escopo_estatico_me_le_codigo")),
        ),
    ),
)

// =====================================================================
// TAC — Código de três endereços
// =====================================================================

internal val tacLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.TAC, Style.STORY, tx("lessonspart3c_receita_para_ajudante_que_so"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_forja_codigo_arvore_anotada_chega")),
            Say(BYTE, tx("lessonspart3c_por_que_nao_transformar_arvore")),
            Say(PROF, tx("lessonspart3c_porque_existem_dezenas_processadores_mais")),
            Text(tx("lessonspart3c_imagine_receita_para_ajudante_cozinha")),
            Code(tx("lessonspart3c_t1_b_c_t2_t1"), tx("solta_lessonspart3c_4")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3c_operacao_por_vez_dois_ingredientes"),
                    tx("lessonspart3c_tigelas_com_etiqueta_t1_t2"),
                    tx("lessonspart3c_ordem_receita_pos_ordem_arvore"),
                    tx("lessonspart3c_ficha_com_4_campos_operacao"),
                    tx("lessonspart3c_ficha_sem_tigela_use_resultado"),
                ),
            ),
            Predict(
                tx("lessonspart3c_quantas_instrucoes_sem_contar_atribuicao"),
                listOf("2", "3", "4"),
                1,
                tx("lessonspart3c_tres_operadores_tres_instrucoes_t1"),
            ),
            Visual(VisualKind.TAC, input = tx("comum_b_c"), caption = tx("lessonspart3c_maquina_receitas_multiplicacao_vem_primeiro")),
            Say(PILHA, tx("lessonspart3c_tigelas_sao_como_eu_guardam")),
            Reveal(tx("lessonspart3c_por_que_tres_enderecos"), tx("lessonspart3c_porque_cada_instrucao_menciona_no")),
            Callout(REAL_WORLD, tx("lessonspart3c_llvm_ir_linguagem_intermediaria_no")),
        ),
    ),
    Lesson(
        Skill.TAC, Style.VISUAL, tx("lessonspart3c_arvore_lista_em_pos_ordem"),
        listOf(
            Say(PROF, tx("lessonspart3c_vamos_ver_arvore_codigo_lado")),
            Visual(VisualKind.ATTR_TREE, input = tx("comum_b_c"), caption = tx("lessonspart3c_arvore_esta_embaixo_na_raiz")),
            Visual(VisualKind.TAC, input = tx("comum_b_c"), caption = tx("lessonspart3c_pos_ordem_b_c_gera")),
            Steps(
                tx("lessonspart3c_regra_gerador"),
                listOf(
                    tx("lessonspart3c_folha_variavel_ou_numero_endereco"),
                    tx("lessonspart3c_no_operador_gera_codigo_filho"),
                    tx("lessonspart3c_cria_temporario_novo_tn_emite"),
                    tx("lessonspart3c_endereco_no_passa_ser_tn"),
                ),
            ),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_b_b"), caption = tx("lessonspart3c_repare_b_calculado_duas_vezes")),
            Say(BYTE, tx("lessonspart3c_com_comparacao_logica_funciona_igual")),
            Visual(VisualKind.TAC, input = tx("comum_b_ok"), caption = "Aqui, como valores: t1 = a < b; t2 = t1 && ok. (Compiladores reais costumam usar desvios — curto-circuito.)"),
            Predict(
                tx("lessonspart3c_para_x_2_int_x"),
                listOf(
                    "t1 = x * 2; t2 = a + t1",
                    "t1 = x * 2; t2 = inttofloat(a); t3 = t2 + t1",
                    "t1 = a + x; t2 = t1 * 2",
                ),
                1,
                tx("lessonspart3c_verificacao_tipos_manda_converter_widget"),
            ),
            Visual(VisualKind.TAC, input = tx("comum_x_2"), caption = tx("lessonspart3c_versao_simplificada_jogo_sem_instrucao")),
            Code(tx("lessonspart3c_op_arg1_arg2_resultado_0"), tx("lessonspart3c_mesmas_instrucoes_b_c_como")),
            Callout(BRAIN_POWER, tx("lessonspart3c_numero_temporarios_no_gerador_ingenuo")),
        ),
    ),
    Lesson(
        Skill.TAC, Style.WORKED, tx("lessonspart3c_expressoes_quadruplas_triplas_if"),
        listOf(
            Say(PROF, tx("lessonspart3c_exercicio_1_gere_tac_para")),
            Steps(
                tx("lessonspart3c_pos_ordem_com_calma"),
                listOf(
                    tx("lessonspart3c_subarvore_esquerda_b_t1_b"),
                    tx("lessonspart3c_subarvore_direita_c_t2_c"),
                    tx("lessonspart3c_raiz_t3_t1_t2"),
                    tx("lessonspart3c_atribuicao_x_t3"),
                ),
            ),
            Code(
                tx("lessonspart3c_quadruplas_triplas_op_arg1_arg2"),
                tx("lessonspart3c_duas_representacoes_das_mesmas_instrucoes"),
            ),
            Say(PROF, tx("lessonspart3c_exercicio_2_comandos_com_desvio")),
            Code(
                tx("lessonspart3c_iffalse_b_goto_l1_x"),
                tx("lessonspart3c_rotulos_desvios_if_vira_pule"),
            ),
            Say(BYTE, tx("lessonspart3c_agora_eu_faco")),
            Reveal(tx("lessonspart3c_tac_y_b_b"), tx("lessonspart3c_t1_b_t2_b_t3")),
            Reveal(tx("lessonspart3c_triplas_y_b_b"), "(0) * a b; (1) * a b; (2) + (0) (1); (3) = y (2)."),
            Reveal("TAC de while (a < b) a = a + 1;", "L1: ifFalse a < b goto L2;  t1 = a + 1;  a = t1;  goto L1;  L2: ..."),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_b_b_2"), caption = tx("lessonspart3c_confira_primeira_resposta")),
            Callout(EXAM_TIP, tx("lessonspart3c_em_triplas_resultado_referenciado_pela")),
        ),
    ),
    Lesson(
        Skill.TAC, Style.SOCRATIC, tx("lessonspart3c_por_que_linguagem_no_meio"),
        listOf(
            Say(PROF, tx("lessonspart3c_pergunta_1_se_eu_tenho")),
            Reveal(tx("lessonspart3c_com_representacao_intermediaria_comum"), tx("lessonspart3c_sem_5_4_20_compiladores")),
            Reveal(tx("lessonspart3c_por_que_limitar_no_maximo"), tx("lessonspart3c_porque_se_parece_com_instrucoes")),
            Reveal(tx("lessonspart3c_em_que_ordem_instrucoes_expressao"), tx("lessonspart3c_na_pos_ordem_arvore_cada")),
            Say(BYTE, tx("lessonspart3c_onde_saem_nomes_t1_t2")),
            Reveal(tx("lessonspart3c_quem_cria_temporarios"), tx("lessonspart3c_proprio_compilador_new_temp_por")),
            Predict(
                tx("lessonspart3c_por_que_quadruplas_sao_mais"),
                listOf(
                    tx("lessonspart3c_porque_ocupam_menos_memoria"),
                    tx("lessonspart3c_porque_nas_triplas_resultados_sao"),
                    tx("lessonspart3c_porque_triplas_nao_aceitam_desvios"),
                ),
                1,
                tx("lessonspart3c_na_quadrupla_resultado_tem_nome"),
            ),
            Reveal(tx("lessonspart3c_como_representar_if_em_tac"), tx("lessonspart3c_com_rotulos_desvios_iffalse_cond")),
            Visual(VisualKind.TAC, input = tx("comum_b_c_2"), caption = tx("lessonspart3c_teste_preveja_instrucoes_antes_olhar")),
            Callout(BRAIN_POWER, tx("lessonspart3c_tac_baixo_nivel_bastante_para")),
        ),
    ),
    Lesson(
        Skill.TAC, Style.FORMAL, tx("lessonspart3c_instrucoes_tres_enderecos_representacoes"),
        listOf(
            Text(tx("lessonspart3c_codigo_tres_enderecos_sequencia_instrucoes")),
            Code(
                tx("lessonspart3c_x_y_op_z_atribuicao"),
                tx("lessonspart3c_formas_instrucao_livro_dragao_secao"),
            ),
            Code(
                """S → id = E ;   S.code := E.code || gen(top.get(id.lexeme) '=' E.addr)
E → E1 + E2    E.addr := new Temp()
               E.code := E1.code || E2.code || gen(E.addr '=' E1.addr '+' E2.addr)
E → - E1       E.addr := new Temp()
               E.code := E1.code || gen(E.addr '=' 'minus' E1.addr)
E → ( E1 )     E.addr := E1.addr;  E.code := E1.code
E → id         E.addr := top.get(id.lexeme);  E.code := ''""",
                tx("lessonspart3c_sdd_traducao_expressoes_para_tac"),
            ),
            Steps(
                tx("lessonspart3c_representacoes"),
                listOf(
                    tx("lessonspart3c_quadruplas_op_arg1_arg2_resultado"),
                    tx("lessonspart3c_triplas_op_arg1_arg2_resultado"),
                    tx("solta_lessonspart3c_5"),
                    tx("lessonspart3c_ssa_cada_variavel_atribuida_unica"),
                ),
            ),
            Predict(
                tx("lessonspart3c_qual_nao_instrucao_tres_enderecos"),
                listOf(tx("comum_t1_b"), tx("lessonspart3c_t2_b_c"), "if t1 goto L3"),
                1,
                tx("solta_lessonspart3c_6"),
            ),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_b_c"), caption = tx("lessonspart3c_associatividade_esquerda_b_primeiro_depois")),
            Callout(EXAM_TIP, tx("lessonspart3c_questao_comum_traduza_para_quadruplas")),
            Callout(WATCH_OUT, tx("lessonspart3c_nao_confunda_tres_nome_com")),
        ),
    ),
    Lesson(
        Skill.TAC, Style.MNEMONIC, tx("lessonspart3c_dois_entram_sai"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_palacio_memoria_forja_codigo_oitava")),
            Callout(MNEMONIC, tx("lessonspart3c_dois_entram_sai_x_y")),
            Callout(MNEMONIC, tx("lessonspart3c_quadrupla_quatro_campos_op_arg1")),
            Callout(MNEMONIC, tx("lessonspart3c_t_tigela_t1_t2_t3")),
            Callout(MNEMONIC, tx("lessonspart3c_pos_ordem_filhos_primeiro_pai")),
            Say(BYTE, tx("lessonspart3c_ordem_das_tigelas_sempre_crescente")),
            Say(PROF, tx("lessonspart3c_no_gerador_jogo_sim_cada")),
            Predict(
                tx("lessonspart3c_na_tripla_2_0_1"),
                listOf(tx("lessonspart3c_numero_zero"), tx("lessonspart3c_resultado_instrucao_posicao_0"), tx("lessonspart3c_temporario_t0")),
                1,
                tx("lessonspart3c_triplas_apontam_pela_posicao_0"),
            ),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_b_c_2"), caption = tx("lessonspart3c_dois_entram_sai_tres_vezes")),
            Reveal(tx("lessonspart3c_recite_tres_representacoes_que_diferencia"), tx("lessonspart3c_quadruplas_resultado_com_nome_triplas")),
            Callout(FIRESIDE, tx("lessonspart3c_quadrupla_eu_tenho_nome_endereco")),
        ),
    ),
)

// =====================================================================
// OPTIMIZATION — Otimização (dobra de constantes e amigos)
// =====================================================================

internal val optLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.OPTIMIZATION, Style.STORY, tx("lessonspart3c_lista_compras_inteligente"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_forja_codigo_sala_polimento_tac")),
            Say(BYTE, tx("lessonspart3c_otimizar_deixar_codigo_otimo")),
            Say(PROF, tx("lessonspart3c_nome_engana_melhorar_sem_mudar")),
            Text(tx("lessonspart3c_sua_lista_diz_comprar_2")),
            Text(tx("lessonspart3c_lista_diz_comprar_leite_duas")),
            Text(tx("lessonspart3c_lista_tem_comprar_fermento_mas")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3c_fazer_conta_em_casa_dobra"),
                    tx("lessonspart3c_anotar_ovos_12_usar_12"),
                    tx("lessonspart3c_comprar_leite_vez_so_eliminacao"),
                    tx("lessonspart3c_riscar_fermento_que_ninguem_usa"),
                    tx("lessonspart3c_trecho_mercado_sem_desvios_entra"),
                ),
            ),
            Predict(
                tx("lessonspart3c_qual_linha_pode_ser_calculada"),
                listOf(tx("lessonspart3c_t1_2"), "t1 = 2 * 3", "t1 = x * y"),
                1,
                tx("lessonspart3c_so_2_3_depende_apenas"),
            ),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_2_3"), caption = tx("lessonspart3c_gerador_produz_t1_2_3")),
            Callout(WATCH_OUT, tx("lessonspart3c_regra_ouro_otimizacao_nunca_pode")),
            Reveal(tx("lessonspart3c_na_analogia_por_que_comprar"), tx("lessonspart3c_se_alguem_tivesse_bebido_leite")),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.VISUAL, tx("lessonspart3c_antes_depois_polimento"),
        listOf(
            Say(PROF, tx("lessonspart3c_vamos_ver_tac_antes_depois")),
            Visual(VisualKind.TAC, input = "x * 2 + 3 * 4", caption = tx("lessonspart3c_antes_t2_3_4_com")),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_4_2_3"), caption = tx("lessonspart3c_mesmo_truque_2_3_vira")),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_b_b"), caption = tx("lessonspart3c_subexpressao_comum_t1_b_t2")),
            Code(tx("lessonspart3c_antes_depois_t1_b_t1"), tx("lessonspart3c_eliminacao_subexpressao_comum")),
            Say(BYTE, tx("lessonspart3c_codigo_morto_como_gente_ve")),
            Code(tx("lessonspart3c_antes_depois_t1_b_t2"), tx("lessonspart3c_eliminacao_codigo_morto_t1_nao")),
            Code(
                tx("lessonspart3c_1_i_0_lider_primeira"),
                tx("lessonspart3c_blocos_basicos_b1_1_b2"),
            ),
            Predict(
                tx("lessonspart3c_instrucao_3_lider"),
                listOf(tx("lessonspart3c_sim_vem_logo_depois_desvio"), tx("lessonspart3c_nao_nao_alvo_nenhum_goto"), tx("lessonspart3c_so_se_i_for_constante")),
                0,
                tx("lessonspart3c_2_desvio_if_goto_instrucao"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart3c_otimizacao_local_trabalha_dentro_bloco")),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.WORKED, tx("lessonspart3c_polindo_trecho_otimizacao_cada_vez"),
        listOf(
            Say(PROF, tx("lessonspart3c_vamos_otimizar_este_bloco_basico")),
            Code(tx("lessonspart3c_1_t1_4_2_2"), tx("lessonspart3c_bloco_original")),
            Steps(
                tx("lessonspart3c_passada_cada_vez"),
                listOf(
                    tx("lessonspart3c_dobra_1_t1_4_2"),
                    tx("lessonspart3c_propagacao_constantes_t1_vale_8"),
                    tx("lessonspart3c_subexpressao_comum_3_calcula_mesmo"),
                    tx("lessonspart3c_propagacao_copias_4_x_t2"),
                    tx("lessonspart3c_codigo_morto_t1_t3_nao"),
                    tx("lessonspart3c_codigo_morto_5_y_0"),
                ),
            ),
            Code(tx("lessonspart3c_t2_8_x_t2_t2"), tx("lessonspart3c_resultado_6_instrucoes_para_3")),
            Say(BYTE, tx("lessonspart3c_ordem_importa_posso_fazer_codigo")),
            Say(PROF, tx("lessonspart3c_pode_mas_otimizacao_costuma_abrir")),
            Reveal("Otimize: t1 = 3 + 2; t2 = t1 * b; t3 = t1 * b; z = t2 + t3.", tx("lessonspart3c_t1_5_dobra_t2_5")),
            Reveal(tx("lessonspart3c_em_t1_b_7_t2"), tx("lessonspart3c_nao_foi_redefinido_entre_duas")),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_4_2_3"), caption = tx("lessonspart3c_pratique_que_dobra_constantes_faz")),
            Callout(EXAM_TIP, tx("lessonspart3c_escreva_nome_cada_otimizacao_ao")),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.SOCRATIC, tx("lessonspart3c_quando_seguro_otimizar"),
        listOf(
            Say(PROF, tx("lessonspart3c_otimizacao_tem_juramento_nao_mudar")),
            Reveal(tx("lessonspart3c_x_10_0_pode_ser"), tx("lessonspart3c_nao_como_valor_divisao_por")),
            Reveal(tx("lessonspart3c_t_0_1_0_2"), tx("lessonspart3c_pode_desde_que_compilador_use")),
            Reveal(tx("lessonspart3c_quando_duas_ocorrencias_b_sao"), tx("lessonspart3c_quando_nem_nem_b_foram")),
            Say(BYTE, tx("lessonspart3c_codigo_morto_como_compilador_sabe")),
            Reveal(tx("lessonspart3c_que_significa_variavel_estar_viva"), tx("lessonspart3c_que_valor_dela_pode_ser")),
            Predict(
                tx("lessonspart3c_if_false_print_1_que"),
                listOf(tx("comum_nada"), tx("lessonspart3c_remover_bloco_inteiro_codigo_inalcancavel"), tx("lessonspart3c_trocar_por_print_0")),
                1,
                tx("lessonspart3c_com_condicao_constante_desvio_sempre"),
            ),
            Reveal(tx("lessonspart3c_por_que_dividir_codigo_em"), tx("lessonspart3c_dentro_bloco_nao_ha_desvios")),
            Reveal(tx("lessonspart3c_x_2_x_x_ou"), tx("lessonspart3c_reducao_forca_trocar_operacao_cara")),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_2_3"), caption = tx("lessonspart3c_preveja_codigo_depois_dobra_antes")),
            Callout(BRAIN_POWER, tx("lessonspart3c_pense_numa_expressao_com_chamada")),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.FORMAL, tx("lessonspart3c_blocos_basicos_transformacoes_que_preservam"),
        listOf(
            Text(tx("lessonspart3c_bloco_basico_sequencia_maximal_instrucoes")),
            Code(
                tx("lessonspart3c_lideres_1_primeira_instrucao_codigo"),
                tx("lessonspart3c_particionamento_em_blocos_basicos_livro"),
            ),
            Steps(
                tx("lessonspart3c_transformacoes_classicas"),
                listOf(
                    tx("lessonspart3c_dobra_constantes_avaliar_em_compilacao"),
                    tx("lessonspart3c_propagacao_constantes_apos_x_6"),
                    tx("lessonspart3c_subexpressoes_comuns_ja_calculada_operandos"),
                    tx("lessonspart3c_propagacao_copias_apos_x_y"),
                    tx("lessonspart3c_codigo_morto_remover_instrucoes_cujo"),
                    tx("lessonspart3c_reducao_forca_movimentacao_codigo_invariante"),
                ),
            ),
            Text(tx("lessonspart3c_local_dentro_bloco_basico_ex")),
            Predict(
                tx("lessonspart3c_qual_transformacao_exige_saber_quais"),
                listOf(tx("comum_dobra_constantes"), tx("lessonspart3c_eliminacao_codigo_morto"), tx("comum_reducao_forca")),
                1,
                tx("lessonspart3c_so_seguro_apagar_x_se"),
            ),
            Visual(VisualKind.TAC, input = "x * 2 + 3 * 4", caption = tx("lessonspart3c_dobra_constantes_local_3_4")),
            Callout(EXAM_TIP, tx("lessonspart3c_questao_classica_identifique_lideres_blocos")),
            Callout(WATCH_OUT, tx("lessonspart3c_instrucao_apos_desvio_lider_nao")),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.MNEMONIC, tx("lessonspart3c_dopa_sumo_pad_dos_blocos"),
        listOf(
            Say(NARRADOR, tx("lessonspart3c_palacio_memoria_ultima_parada_sala")),
            Callout(MNEMONIC, tx("lessonspart3c_dopa_sumo_bra_p_ropag")),
            Callout(MNEMONIC, tx("lessonspart3c_lideres_pad_p_rimeira_instrucao")),
            Callout(MNEMONIC, tx("lessonspart3c_faz_conta_em_casa_nao")),
            Callout(MNEMONIC, tx("lessonspart3c_codigo_morto_se_ninguem_le")),
            Say(BYTE, tx("lessonspart3c_juramento")),
            Say(PROF, tx("lessonspart3c_mais_rapido_nunca_diferente_programa")),
            Predict(
                "t1 = 2 * 5; x = t1 + a — aplique DO e PA.",
                listOf(tx("lessonspart3c_x_10"), tx("lessonspart3c_x_2_5"), "x = t1 + 10"),
                0,
                tx("lessonspart3c_dobra_t1_10_propaga_x"),
            ),
            Visual(VisualKind.TAC, input = tx("lessonspart3c_4_2_3"), caption = tx("lessonspart3c_dopa_sumo_em_acao_2")),
            Reveal(tx("lessonspart3c_recite_dopa_sumo_pad"), tx("lessonspart3c_dobra_propagacao_subexpressao_comum_morto")),
            Callout(FIRESIDE, tx("lessonspart3c_dobra_constantes_eu_faco_contas")),
        ),
    ),
)
