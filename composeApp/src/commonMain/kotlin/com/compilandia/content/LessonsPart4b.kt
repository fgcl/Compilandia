package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

// Parte 4b do conteúdo: LEX_FLEX (Floresta Léxica — Oficina Flex).
// Os fluxos de tokens citados foram conferidos com MiniFlex.parseSpec(...).scan(...).

// Especificações .l usadas nos widgets FLEX (seção de regras entre as linhas %%).

private val p4FlexBasico = """
%%
[ \t\n]+                { }
"if"                    { return IF; }
"while"                 { return WHILE; }
[a-z][a-z0-9]*          { return ID; }
[0-9]+                  { return NUM; }
"=="                    { return EQ; }
"="                     { return ATRIB; }
"("                     { return ABRE; }
")"                     { return FECHA; }
";"                     { return PV; }
%%
""".trim()

private val p4FlexOrdemErrada = """
%%
[ \t\n]+                { }
[a-z][a-z0-9]*          { return ID; }
"if"                    { return IF; }
[0-9]+                  { return NUM; }
%%
""".trim()

private val p4FlexOps = """
%%
" "+          { }
[a-z]+        { return ID; }
">"           { return GT; }
">="          { return GE; }
"="           { return ATRIB; }
"=="          { return EQ; }
%%
""".trim()

private val p4FlexMenorPrimeiro = """
%%
[ ]+              { }
[a-z]+            { return ID; }
"<"               { return LT; }
"<="              { return LE; }
%%
""".trim()

private val p4FlexPontoPrimeiro = """
%%
.                 { }
[ ]+              { }
[a-z]+            { return ID; }
"+"               { return MAIS; }
%%
""".trim()

/** Versão "de bolso" do scanner do TP (sem definições nomeadas, para o widget conseguir rodar). */
private val p4FlexTp = """
%{
#include "y.tab.h"
int linha = 1;
%}
%%
[ \t]+                  { }
\n                      { linha++; }
"//".*                  { }
"int"                   { return INT; }
"float"                 { return FLOAT; }
"if"                    { return IF; }
"else"                  { return ELSE; }
[a-zA-Z_][a-zA-Z0-9_]*  { yylval.nome = strdup(yytext); return ID; }
[0-9]+"."[0-9]+         { yylval.real = atof(yytext); return REAL; }
[0-9]+                  { yylval.inteiro = atoi(yytext); return NUM; }
"<="                    { return LE; }
"<"                     { return LT; }
"="                     { return ATRIB; }
"+"                     { return MAIS; }
";"                     { return PV; }
"("                     { return ABRE; }
")"                     { return FECHA; }
.                       { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); }
%%
int yywrap(void) { return 1; }
""".trim()

/** Arquivo .l completo, de verdade, que compila sozinho com `lex scanner.l && gcc lex.yy.c`. */
private val p4FlexCompleto = """
%{
/* scanner.l : analisador lexico do TP */
#include <stdio.h>
enum { INT = 1, FLOAT, IF, ELSE, ID, NUM, REAL,
       LE, LT, ATRIB, MAIS, PV, ABRE, FECHA };
int linha = 1;
%}
DIGITO   [0-9]
LETRA    [a-zA-Z_]
%%
[ \t]+                       { /* ignora espacos */ }
\n                           { linha++; }
"//".*                       { /* ignora comentario */ }
"int"                        { return INT; }
"float"                      { return FLOAT; }
"if"                         { return IF; }
"else"                       { return ELSE; }
{LETRA}({LETRA}|{DIGITO})*   { return ID; }
{DIGITO}+"."{DIGITO}+        { return REAL; }
{DIGITO}+                    { return NUM; }
"<="                         { return LE; }
"<"                          { return LT; }
"="                          { return ATRIB; }
"+"                          { return MAIS; }
";"                          { return PV; }
"("                          { return ABRE; }
")"                          { return FECHA; }
.                            { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); }
%%
int yywrap(void) { return 1; }

int main(void) {
    int t;
    while ((t = yylex()) != 0)
        printf("linha %d: token %d, lexema '%s' (%d chars)\n",
               linha, t, yytext, yyleng);
    return 0;
}
""".trim()

// ───────────────────────────── LEX_FLEX ─────────────────────────────

internal val p4FlexLessons: List<Lesson> = listOf(
    Lesson(
        Skill.LEX_FLEX, Style.STORY, tx("lessonspart4b_triagem_dos_correios"),
        listOf(
            Say(NARRADOR, tx("lessonspart4b_centro_triagem_dos_correios_madrugada")),
            Say(LEXI, tx("lessonspart4b_eu_nao_separo_nada_na")),
            Steps(
                "Correios → Lex/Flex",
                listOf(
                    tx("lessonspart4b_folha_regras_pacote_assim_carimbo"),
                    tx("lessonspart4b_fabrica_que_le_folha_monta"),
                    tx("lessonspart4b_robo_funcao_yylex_cada_vez"),
                    tx("lessonspart4b_pacote_que_robo_pegou_lexema"),
                    tx("lessonspart4b_carimbo_return_token_acao_acao"),
                ),
            ),
            Say(PROF, tx("lessonspart4b_robo_obedece_duas_regras_ouro")),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "if (x1 == 10) y = 2;", caption = tx("lessonspart4b_veja_qual_regra_carimbou_cada")),
            Predict(
                tx("lessonspart4b_com_essa_folha_if_acima"),
                listOf(tx("lessonspart4b_if_seguido_id_fy"), tx("lessonspart4b_unico_id_iffy"), tx("comum_erro_lexico"), "IF, IF, ID"),
                1,
                tx("lessonspart4b_guloso_regra_id_aceita_4"),
            ),
            Say(BYTE, tx("lessonspart4b_se_alguem_colar_regra_id")),
            Visual(VK.FLEX, grammar = p4FlexOrdemErrada, input = "if x 10", caption = tx("lessonspart4b_folha_com_ordem_trocada_olhe")),
            Reveal(tx("lessonspart4b_por_que_if_virou_id"), tx("lessonspart4b_empate_duas_regras_aceitam_if")),
            Callout(REAL_WORLD, tx("lessonspart4b_no_trabalho_pratico_lexico")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart4b_p_lex_flex_sao_mesma")),
            Reveal(tx("lessonspart4b_na_analogia_quem_folha_fabrica"), tx("lessonspart4b_arquivo_l_programa_lex_flex")),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.VISUAL, tx("lessonspart4b_flex_ao_vivo"),
        listOf(
            Say(LEXI, tx("lessonspart4b_cada_widget_abaixo_scanner_l")),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "while (n) n = n;", caption = tx("lessonspart4b_espacos_casam_com_regra_1")),
            Text(tx("lessonspart4b_repare_em_duas_regras_comecam")),
            Visual(VK.FLEX, grammar = p4FlexOps, input = tx("lessonspart4b_b_c_d_f"), caption = tx("lessonspart4b_desta_vez_esta_antes_na")),
            Predict(
                tx("lessonspart4b_com_essa_mesma_folha_como"),
                listOf("ID EQ EQ ID", "ID EQ ATRIB ID", "ID ATRIB EQ ID", "ID ATRIB ATRIB ATRIB ID"),
                1,
                tx("lessonspart4b_guloso_esquerda_para_direita_primeiro"),
            ),
            Say(PROF, tx("lessonspart4b_agora_scanner_no_estilo_trabalho")),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "float media = 3.14; // comentario", caption = tx("lessonspart4b_comentario_inteiro_casa_com_some")),
            Reveal(tx("lessonspart4b_por_que_3_14_virou"), tx("lessonspart4b_regra_real_casa_3_14")),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "int x@ = 2;", caption = tx("lessonspart4b_cai_na_regra_coringa_que")),
            Callout(WATCH_OUT, tx("lessonspart4b_regra_tem_que_ser_ultima")),
            Visual(VK.FLEX, grammar = p4FlexPontoPrimeiro, input = "x + ab", caption = tx("lessonspart4b_prova_com_no_topo_x")),
            Reveal(tx("lessonspart4b_quantos_tokens_saem_x_ab"), tx("lessonspart4b_so_1_id_ab_x")),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.WORKED, tx("lessonspart4b_escrevendo_scanner_l_tp"),
        listOf(
            Say(PROF, tx("lessonspart4b_vamos_montar_com_calma_arquivo")),
            Code(p4FlexCompleto, tx("lessonspart4b_scanner_l_compile_com_lex")),
            Steps(
                tx("lessonspart4b_lendo_arquivo_parte_por_parte"),
                listOf(
                    tx("lessonspart4b_codigo_c_copiado_literalmente_para"),
                    tx("lessonspart4b_digito_0_9_letra_za"),
                    tx("lessonspart4b_primeiro_comecam_regras_cada_linha"),
                    tx("lessonspart4b_espacos_comentarios_acao_sem_return"),
                    tx("lessonspart4b_palavras_chave_int_float_if"),
                    tx("lessonspart4b_real_antes_num_por_clareza"),
                    tx("lessonspart4b_regra_por_ultimo_qualquer_caractere"),
                    tx("lessonspart4b_segundo_codigo_c_usuario_yywrap"),
                ),
            ),
            Text(tx("lessonspart4b_agora_vamos_rodar_cabeca_linha")),
            Steps(
                tx("lessonspart4b_resolvido_tokens_linha"),
                listOf(
                    tx("lessonspart4b_float_empate_entre_float_id"),
                    tx("lessonspart4b_regra_espacos_acao_vazia_nada"),
                    tx("lessonspart4b_media_so_id_casa_id"),
                    tx("lessonspart4b_atrib_3_14_real_casa"),
                    tx("lessonspart4b_pv_comentario_engole_ate_fim"),
                ),
            ),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "float media = 3.14; // comentario", caption = tx("lessonspart4b_confira_float_id_atrib_real")),
            Text(tx("lessonspart4b_sua_vez_exemplo_esmaecido_com")),
            Reveal("Tokens de `intx = 3.5;`?", tx("lessonspart4b_id_atrib_real_pv_intx")),
            Reveal("Tokens de `if (x <= 10) x = x + 1;`?", tx("lessonspart4b_if_abre_id_le_num")),
            Reveal(tx("solta_lessonspart4b_1"), tx("lessonspart4b_tokens_int_id_atrib_num")),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "if (x <= 10) x = x + 1;", caption = tx("lessonspart4b_confira_segunda_resposta_teste_outras")),
            Callout(EXAM_TIP, tx("lessonspart4b_sem_yywrap_ou_option_noyywrap")),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.SOCRATIC, tx("lessonspart4b_por_que_l_desse_jeito"),
        listOf(
            Say(PROF, tx("lessonspart4b_hoje_voce_vai_descobrir_regras")),
            Predict(
                tx("lessonspart4b_na_folha_abaixo_vem_antes"),
                listOf("ID LT ATRIB ID", "ID LE ID", tx("lessonspart4b_erro_falta_regra"), "ID LT ID"),
                1,
                tx("lessonspart4b_robo_guloso_casa_2_caracteres"),
            ),
            Visual(VK.FLEX, grammar = p4FlexMenorPrimeiro, input = tx("lessonspart4b_b_c_d"), caption = tx("lessonspart4b_confira_le_em_b_lt")),
            Say(BYTE, tx("lessonspart4b_entao_ordem_das_regras_nunca")),
            Predict(
                tx("lessonspart4b_quando_ordem_das_regras_importa"),
                listOf(tx("comum_nunca"), tx("lessonspart4b_sempre"), tx("lessonspart4b_so_quando_duas_regras_casam"), tx("lessonspart4b_so_para_numeros")),
                2,
                tx("lessonspart4b_empate_tamanho_unico_caso_exemplo"),
            ),
            Predict(
                tx("lessonspart4b_regra_sem_return_como_t"),
                listOf(tx("lessonspart4b_devolve_token_vazio"), tx("lessonspart4b_para_scanner"), tx("lessonspart4b_descarta_lexema_yylex_continua_procurando"), tx("lessonspart4b_erro_compilacao")),
                2,
                tx("lessonspart4b_yylex_laco_so_sai_quando"),
            ),
            Predict(
                tx("lessonspart4b_sintatico_precisa_saber_valor_numero"),
                listOf(tx("lessonspart4b_pelo_return"), tx("lessonspart4b_por_yylval_preenchido_na_acao"), tx("lessonspart4b_por_yyleng"), tx("lessonspart4b_sintatico_le_arquivo_novo")),
                1,
                tx("lessonspart4b_return_leva_so_classe_token"),
            ),
            Predict(
                tx("lessonspart4b_se_regra_qualquer_caractere_ficar"),
                listOf(tx("comum_nada_muda"), tx("lessonspart4b_x_somem_so_ab_vira"), tx("lessonspart4b_tudo_some"), tx("lessonspart4b_erro_compilacao_2")),
                1,
                tx("lessonspart4b_x_tem_tamanho_1_empatam"),
            ),
            Visual(VK.FLEX, grammar = p4FlexPontoPrimeiro, input = "x + ab", caption = tx("lessonspart4b_confira_estrago")),
            Reveal(tx("lessonspart4b_como_flex_consegue_achar_maior"), tx("lessonspart4b_ele_junta_afns_todas_regras")),
            Reveal(tx("lessonspart4b_por_que_resultado_tao_rapido"), tx("lessonspart4b_porque_afd_passo_por_caractere")),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.FORMAL, tx("lessonspart4b_especificacao_lex_definicao_prova"),
        listOf(
            Text(tx("lessonspart4b_programa_lex_tem_tres_secoes")),
            Code(tx("lessonspart4b_declaracoes_regras_traducao_funcoes_auxiliar"), tx("lessonspart4b_estrutura_arquivo_l")),
            Code(
                tx("lessonspart4b_expressao_casa_com_exemplo_c"),
                tx("lessonspart4b_expressoes_regulares_lex_livro_dragao"),
            ),
            Text(tx("lessonspart4b_resolucao_conflitos_dragao_3_5")),
            Steps(
                tx("lessonspart4b_interface_analisador_gerado"),
                listOf(
                    tx("lessonspart4b_yylex_devolve_codigo_proximo_token"),
                    tx("lessonspart4b_yytext_ponteiro_para_inicio_lexema"),
                    tx("lessonspart4b_yylval_variavel_global_com_valor"),
                    tx("lessonspart4b_yyin_arquivo_entrada_padrao_stdin"),
                ),
            ),
            Text(tx("lessonspart4b_implementacao_lex_constroi_afn_para")),
            Predict(
                tx("solta_lessonspart4b_2"),
                listOf(tx("lessonspart4b_token_if_depois_lexema"), tx("lessonspart4b_unico_lexema_ifa_regra_z"), tx("lessonspart4b_erro_conflito")),
                1,
                tx("lessonspart4b_regra_1_resolucao_prefixo_mais"),
            ),
            Predict(
                tx("lessonspart4b_qual_papel_operador_em_letra"),
                listOf(tx("lessonspart4b_divisao"), tx("lessonspart4b_lookahead_so_casa_se_que"), tx("lessonspart4b_comentario"), tx("lessonspart4b_alternativa_como")),
                1,
                tx("lessonspart4b_operador_contexto_direita_lookahead_usado"),
            ),
            Callout(EXAM_TIP, tx("lessonspart4b_em_prova_discursiva_cite_3")),
            Reveal(tx("lessonspart4b_escreva_regra_identificador_que_devolve"), tx("lessonspart4b_za_z_za_z0_9")),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.MNEMONIC, tx("lessonspart4b_guloso_mas_educado"),
        listOf(
            Callout(MNEMONIC, tx("lessonspart4b_d_r_c_d_efinicoes")),
            Callout(MNEMONIC, tx("lessonspart4b_flex_guloso_mas_educado_guloso")),
            Callout(MNEMONIC, tx("lessonspart4b_palavra_chave_vip_entra_antes")),
            Text(tx("lessonspart4b_palacio_memoria_na_floresta_lexica")),
            Callout(MNEMONIC, tx("lessonspart4b_tudo_lex_comeca_com_yy")),
            Callout(MNEMONIC, tx("lessonspart4b_vazio_lixeira_sem_return_lexema")),
            Predict(
                tx("lessonspart4b_yytext_ou_yylval_onde_esta"),
                listOf("yytext", "yylval", "yyleng", "yylex"),
                0,
                tx("lessonspart4b_yytext_texto_cru_yylval_so"),
            ),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "iffy = while2;", caption = tx("lessonspart4b_guloso_iffy_while2_sao_ids")),
            Callout(WATCH_OUT, tx("lessonspart4b_confusoes_classicas_1_achar_que")),
            Callout(REAL_WORLD, tx("solta_lessonspart4b_3")),
            Reveal(tx("lessonspart4b_recite_duas_regras_ouro_mnemonico"), tx("lessonspart4b_1_maior_casamento_guloso_2")),
        ),
    ),
)
