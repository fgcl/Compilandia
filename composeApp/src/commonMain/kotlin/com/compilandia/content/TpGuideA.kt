package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Guia do Trabalho Prático — capítulos 1 a 3 (planejamento, a linguagem Byteiro, o léxico).
// Todo o código Java/MIPS do guia é original, escrito para a linguagem-exemplo Byteiro.

/** O programa-exemplo que atravessa o guia inteiro (contagem.byt). */
internal const val TP_PROGRAMA_CONTAGEM = """programa contagem {
    inteiro n, soma;
    caractere c;
    n <- 5;
    soma <- 0;
    enquanto (n > 0) {
        soma <- soma + n * 2;   // acumula o dobro
        n <- n - 1;
    }
    c <- 'A';
    se (soma >= 30 e nao (n <> 0)) {
        escreva("soma = ", soma);
    } senao {
        escreva("pouco: ", c);
    }
}"""

// ───────────────────────────── Capítulo 1 ─────────────────────────────

internal val tpCap1 = GuideChapter(
    1, tx("tpguidea_visao_geral_planejamento"),
    tx("tpguidea_tp_compilador_inteiro_mas_sao"),
    Skill.PHASES,
    listOf(
        Say(NARRADOR, tx("tpguidea_semana_3_semestre_enunciado_cai")),
        Say(BYTE, tx("tpguidea_compilador_inteiro_eu_mal_faco")),
        Say(PROF, tx("tpguidea_respira_ninguem_escreve_compilador_vez")),
        Visual(VK.PIPELINE, caption = tx("tpguidea_cada_estacao_esteira_vira_classe")),
        Steps(
            tx("tpguidea_pecas_tp_arquivo_java_cada"),
            listOf(
                tx("tpguidea_lexico_lexico_java_texto_tokens"),
                tx("tpguidea_sintatico_sintatico_java_tokens_programa"),
                tx("tpguidea_semantico_tabelasimbolos_java_checagens_dent"),
                tx("tpguidea_gerador_tambem_dentro_sintatico_cada"),
                tx("tpguidea_otimizacao_opcional_bonus_dobrar_constantes"),
                tx("tpguidea_testes_pasta_programas_que_devem"),
            ),
        ),
        Text(tx("tpguidea_repare_num_detalhe_semantico_gerador")),
        Code(
            tx("tpguidea_byteiro_src_main_java_le"),
            tx("tpguidea_estrutura_pastas_sugerida"),
        ),
        Code(
            """
            public class ErroCompilacao extends RuntimeException {
                public final int linha;

                public ErroCompilacao(int linha, String msg) {
                    super("linha " + linha + ": " + msg);
                    this.linha = linha;
                }
            }
            """.trimIndent(),
            tx("tpguidea_errocompilacao_java_todo_erro_compilador"),
        ),
        Code(
            tx("tpguidea_import_java_nio_file_files"),
            tx("tpguidea_main_java_maestro_java_17"),
        ),
        Predict(
            tx("tpguidea_qual_ordem_implementacao_menos_dor"),
            listOf(
                tx("tpguidea_escrever_tudo_vez_testar_na"),
                tx("tpguidea_lexico_sintatico_semantico_geracao_testando"),
                tx("tpguidea_comecar_pela_geracao_mips_que"),
                tx("tpguidea_comecar_pela_tabela_simbolos_que"),
            ),
            1,
            tx("tpguidea_cada_fase_consome_saida_anterior"),
        ),
        Steps(
            tx("tpguidea_cronograma_8_semanas_ajuste_data"),
            listOf(
                tx("tpguidea_semana_1_definir_linguagem_escrever"),
                tx("tpguidea_semana_2_lexico_testelexico_que"),
                tx("tpguidea_semanas_3_4_gramatica_ll"),
                tx("tpguidea_semana_5_tabela_simbolos_verificacao"),
                tx("tpguidea_semanas_6_7_geracao_mips"),
                tx("tpguidea_semana_8_otimizacao_bateria_testes"),
            ),
        ),
        Code(
            tx("tpguidea_git_init_byteiro_cd_byteiro"),
            tx("tpguidea_git_desde_dia_1_branch"),
        ),
        Callout(REAL_WORLD, tx("tpguidea_commit_pequeno_salva_vida_quando")),
        Say(LEXI, tx("tpguidea_dupla_comeca_lexico_enquanto_outro")),
        Say(PROF, tx("tpguidea_mas_atencao_todo_mundo_precisa")),
        Callout(WATCH_OUT, tx("tpguidea_leia_enunciado_com_marca_texto")),
        Callout(NO_DUMB_QUESTIONS, tx("tpguidea_p_posso_usar_antlr_javacc")),
        Reveal(tx("tpguidea_em_frase_que_seu_main"), tx("tpguidea_le_arquivo_fonte_entrega_ao")),
        Say(PILHA, tx("tpguidea_devagar_sempre_fase_por_vez")),
    ),
)

// ───────────────────────────── Capítulo 2 ─────────────────────────────

internal val tpCap2 = GuideChapter(
    2, tx("tpguidea_definindo_linguagem_byteiro"),
    tx("tpguidea_antes_java_tokens_precedencia_gramatica"),
    Skill.TOKENS,
    listOf(
        Say(PROF, tx("tpguidea_antes_linha_java_voce_precisa")),
        Say(BYTE, tx("tpguidea_posso_por_classes_lambdas_vetores")),
        Say(PROF, tx("tpguidea_nao_cada_recurso_mais_capitulo")),
        Code(TP_PROGRAMA_CONTAGEM, tx("tpguidea_contagem_byt_programa_byteiro_completo")),
        Steps(
            tx("tpguidea_ficha_tecnica_byteiro"),
            listOf(
                tx("tpguidea_tipos_inteiro_32_bits_logico"),
                tx("tpguidea_comandos_declaracao_inteiro_b_atribuicao"),
                "**Blocos** `{ }` — cada bloco abre um escopo novo. `se`, `senao` e `enquanto` EXIGEM bloco.",
                tx("tpguidea_expressoes_aritmetica_relacionais_logicas_ou"),
                tx("tpguidea_comentarios_ate_fim_linha_bloco"),
                tx("tpguidea_strings_so_aparecem_dentro_escreva"),
            ),
        ),
        Code(
            tx("tpguidea_categoria_padrao_informal_exemplos_reservada"),
            tx("tpguidea_tabela_tokens_vai_direto_para"),
        ),
        Code(
            tx("tpguidea_precedencia_mais_fraca_para_mais"),
            tx("tpguidea_tabela_precedencia_byteiro"),
        ),
        Predict(
            tx("tpguidea_na_byteiro_como_agrupado_b"),
            listOf(
                tx("tpguidea_b_2_10_ok"),
                tx("tpguidea_b_2_10_ok_2"),
                tx("tpguidea_b_2_10_ok_3"),
                tx("tpguidea_erro_misturar_aritmetica_logica_proibido"),
            ),
            1,
            tx("tpguidea_quem_esta_mais_embaixo_na"),
        ),
        Callout(WATCH_OUT, tx("tpguidea_por_que_para_atribuir_assim")),
        Visual(VK.TOKENIZE, input = "if (x<-1) y = 0; // C de verdade", caption = tx("tpguidea_lexi_app_conhece_c_em")),
        Code(
            """
            programa   = "programa" ID bloco
            bloco      = "{" { comando } "}"
            comando    = declaracao | atribuicao | se | enquanto | escreva | leia | bloco
            declaracao = tipo ID { "," ID } ";"
            tipo       = "inteiro" | "logico" | "caractere"
            atribuicao = ID "<-" expr ";"
            se         = "se" "(" expr ")" bloco [ "senao" bloco ]
            enquanto   = "enquanto" "(" expr ")" bloco
            escreva    = "escreva" "(" arg { "," arg } ")" ";"
            arg        = STRING | expr
            leia       = "leia" "(" ID ")" ";"
            """.trimIndent(),
            tx("tpguidea_gramatica_informal_ebnf_repete_0"),
        ),
        Callout(REAL_WORLD, tx("tpguidea_exigir_chaves_no_se_fecha")),
        Reveal("Quem pega cada erro? (1) `x <- 3 # 2;`  (2) `x <- 3 + ;`  (3) `x <- verdadeiro + 1;`", tx("tpguidea_1_lexico_nao_pertence_nenhum")),
        Predict(
            "E `inteiro 2x;`? Que tipo de erro a Byteiro deve dar?",
            listOf(
                tx("tpguidea_lexico_numero_malformado_2x"),
                tx("tpguidea_sintatico_esperava_identificador_encontrou_2"),
                tx("tpguidea_depende_como_voce_especificou_lexico"),
                tx("tpguidea_nenhum_2x_identificador_valido"),
            ),
            2,
            tx("tpguidea_lexico_pode_cortar_2x_em"),
        ),
        Callout(EXAM_TIP, tx("tpguidea_escreva_especificacao_no_relatorio_antes")),
        Steps(
            tx("tpguidea_licao_casa_antes_capitulo_3"),
            listOf(
                tx("tpguidea_escreva_5_programas_byteiro_corretos"),
                tx("tpguidea_escreva_5_programas_errados_com"),
                tx("tpguidea_ao_lado_cada_anote_saida"),
            ),
        ),
        Say(LEXI, tx("tpguidea_tabela_tokens_pronta_entao_me")),
    ),
)

// ───────────────────────────── Capítulo 3 ─────────────────────────────

internal val tpCap3 = GuideChapter(
    3, tx("tpguidea_regex_ao_afd_analisador_lexico"),
    tx("tpguidea_cada_token_regex_cada_regex"),
    Skill.REGEX_DFA,
    listOf(
        Say(LEXI, tx("tpguidea_meu_segredo_cada_categoria_token")),
        Code(
            tx("tpguidea_id_l_l_d_l"),
            tx("tpguidea_regex_byteiro"),
        ),
        Visual(VK.DFA, input = "l(l|d)*", caption = tx("tpguidea_id_l_letra_d_digito")),
        Visual(VK.DFA, input = "dd*", caption = tx("tpguidea_num_pelo_menos_digito_teste")),
        Visual(VK.DFA, input = "m(h|i|g)?", caption = tx("tpguidea_familia_m_h_i_g")),
        Say(BYTE, tx("tpguidea_ta_sao_varios_afds_mas")),
        Say(PROF, tx("tpguidea_crie_estado_0_com_seta")),
        Code(
            tx("tpguidea_letra_digito_0_letra_1"),
            tx("tpguidea_automato_grande_byteiro_desenhe_versao"),
        ),
        Code(
            tx("tpguidea_estado_letra_digito_outro_0"),
            tx("tpguidea_pedaco_tabela_transicoes_devolve_token"),
        ),
        Callout(NO_DUMB_QUESTIONS, tx("tpguidea_p_cada_palavra_reservada_precisa")),
        Code(
            tx("tpguidea_public_enum_tipotoken_palavras_reservadas"),
            tx("tpguidea_tipotoken_java_token_java"),
        ),
        Code(
            tx("tpguidea_import_java_util_hashmap_import"),
            tx("tpguidea_lexico_java_1_3_campos"),
        ),
        Code(
            tx("tpguidea_public_token_proximo_int_estado"),
            tx("tpguidea_lexico_java_2_3_afd"),
        ),
        Code(
            tx("tpguidea_private_token_simples_char_c"),
            tx("tpguidea_lexico_java_3_3"),
        ),
        Predict(
            tx("tpguidea_entrada_x_y_afd_esta"),
            listOf(
                tx("tpguidea_consome_y_devolve_menor"),
                tx("tpguidea_devolve_menor_sem_consumir_y"),
                tx("tpguidea_erro_lexico_y_nao_token"),
                tx("tpguidea_volta_ao_estado_0_descarta"),
            ),
            1,
            tx("tpguidea_y_outro_desenho_ele_so"),
        ),
        Callout(WATCH_OUT, tx("tpguidea_contagem_linhas_incremente_linha_em")),
        Callout(WATCH_OUT, tx("tpguidea_character_isletter_aceita_c_se")),
        Steps(
            tx("tpguidea_erros_lexicos_byteiro_sempre_com"),
            listOf(
                tx("tpguidea_caractere_invalido_x_3_2"),
                tx("tpguidea_numero_malformado_12abc_numero_malformado"),
                tx("tpguidea_caractere_vazio_ou_sem_fechar"),
                tx("tpguidea_string_que_atravessa_linha_ola"),
                tx("tpguidea_comentario_sem_ate_fim_arquivo"),
            ),
        ),
        Callout(REAL_WORLD, tx("tpguidea_muitos_enunciados_pedem_palavras_chave")),
        Code(
            """
            public class TesteLexico {
                public static void main(String[] args) {
                    Lexico lx = new Lexico("inteiro n; /* contador */\nn <- n+1; se (n<>10) {}");
                    Token t;
                    do {
                        t = lx.proximo();
                        System.out.println(t);
                    } while (t.tipo != TipoToken.EOF);
                }
            }
            """.trimIndent(),
            tx("tpguidea_testelexico_java_rode_antes_escrever"),
        ),
        Code(
            """
            1   INTEIRO        inteiro
            1   ID             n
            1   PONTO_VIRGULA  ;
            2   ID             n
            2   ATRIB          <-
            2   ID             n
            2   MAIS           +
            2   NUM            1
            2   PONTO_VIRGULA  ;
            2   SE             se
            2   ABRE_PAR       (
            2   ID             n
            2   DIFERENTE      <>
            2   NUM            10
            2   FECHA_PAR      )
            2   ABRE_CHAVE     {
            2   FECHA_CHAVE    }
            2   EOF            fim de arquivo
            """.trimIndent(),
            tx("tpguidea_saida_esperada_comentario_sumiu_sao"),
        ),
        Reveal(tx("tpguidea_por_que_proximo_devolve_token"), tx("tpguidea_porque_parser_passada_pede_tokens")),
        Say(LEXI, tx("tpguidea_fatiado_agora_alguem_precisa_conferir")),
    ),
)
