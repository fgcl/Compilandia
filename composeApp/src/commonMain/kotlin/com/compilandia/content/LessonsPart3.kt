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

// =====================================================================
// Parte 3 do conteúdo: Fortaleza Bottom-Up (SLR, LALR), Oficina Bison,
// Cidade Semântica (atributos, tipos, escopo) e Forja do Código (TAC, otimização).
// Os arquivos LessonsPart3b.kt, LessonsPart3c.kt e LessonsPart3Cards.kt
// completam esta parte; aqui ficam as gramáticas compartilhadas e o agregador.
// =====================================================================

/** Gramática clássica de expressões (SLR(1) sem conflitos). */
internal const val P3_EXPR = "E -> E + T | T\nT -> T * F | F\nF -> ( E ) | id"

/** Atribuição com ponteiros: LALR(1) mas NÃO SLR(1) (conflito no estado 2 com '='). */
internal const val P3_PTR = "S -> L = R | R\nL -> * R | id\nR -> L"

/** Expressões ambíguas: conflitos empilhar/reduzir em + e *. */
internal const val P3_AMBIG = "E -> E + E | E * E | ( E ) | id"

/** LR(1) mas NÃO LALR(1): a fusão de núcleos cria conflito reduzir/reduzir. */
internal const val P3_RR = "S -> a A d | b B d | a B e | b A e\nA -> c\nB -> c"

/** Lista recursiva à esquerda: SLR(1) tranquilo. */
internal const val P3_LIST = "S -> S a | b"

/** Else pendurado. */
internal const val P3_IFELSE = "S -> if c then S | if c then S else S | x"

/** Declarações estilo C (atributo herdado do tipo). */
internal const val P3_DECL = "D -> T L ;\nT -> int | float\nL -> id R\nR -> , id R | ε"

// ---------- Especificações para o mini-Bison do jogo ----------

internal const val P3_BISON_CALC = """%token NUM
%left '+' '-'
%left '*' '/'
%right UMINUS
%%
expr : expr '+' expr { $$ = $1 + $3; }
     | expr '-' expr { $$ = $1 - $3; }
     | expr '*' expr { $$ = $1 * $3; }
     | expr '/' expr { $$ = $1 / $3; }
     | '-' expr %prec UMINUS { $$ = -$2; }
     | '(' expr ')' { $$ = $2; }
     | NUM
     ;
"""

internal const val P3_BISON_NOPREC = """%token NUM
%%
expr : expr '+' expr { $$ = $1 + $3; }
     | expr '*' expr { $$ = $1 * $3; }
     | NUM
     ;
"""

internal const val P3_BISON_RIGHT_MINUS = """%token NUM
%right '-'
%%
expr : expr '-' expr { $$ = $1 - $3; }
     | NUM
     ;
"""

internal const val P3_BISON_POW = """%token NUM
%left '+'
%left '*'
%right '^'
%%
expr : expr '+' expr { $$ = $1 + $3; }
     | expr '*' expr { $$ = $1 * $3; }
     | expr '^' expr { $$ = $1 ^ $3; }
     | NUM
     ;
"""

internal const val P3_BISON_NONASSOC = """%token NUM
%nonassoc '<'
%left '+'
%%
expr : expr '<' expr { $$ = $1 < $3; }
     | expr '+' expr { $$ = $1 + $3; }
     | NUM
     ;
"""

internal const val P3_BISON_LAYERS = """%token NUM
%%
expr  : expr '+' term   { $$ = $1 + $3; }
      | term
      ;
term  : term '*' fator  { $$ = $1 * $3; }
      | fator
      ;
fator : NUM
      | '(' expr ')'    { $$ = $2; }
      ;
"""

internal const val P3_BISON_PTR = """%token ID
%%
s : l '=' r
  | r
  ;
l : '*' r
  | ID
  ;
r : l
  ;
"""

internal const val P3_BISON_RR = """%token a b c d e
%%
s : a x d
  | b y d
  | a y e
  | b x e
  ;
x : c ;
y : c ;
"""

/** Calculadora Bison de verdade (arquivo calc.y), com lexer escrito à mão. */
internal const val P3_REAL_CALC_Y = """%{
#include <stdio.h>
#include <ctype.h>
int yylex(void);
void yyerror(const char *s);
%}

%token NUM
%left '+' '-'
%left '*' '/'
%right UMINUS

%%
linha : expr '\n'              { printf("= %d\n", $1); }
      ;
expr  : expr '+' expr          { $$ = $1 + $3; }
      | expr '-' expr          { $$ = $1 - $3; }
      | expr '*' expr          { $$ = $1 * $3; }
      | expr '/' expr          { $$ = $1 / $3; }
      | '-' expr %prec UMINUS  { $$ = -$2; }
      | '(' expr ')'           { $$ = $2; }
      | NUM                    { $$ = $1; }
      ;
%%
int yylex(void) {
    int c;
    while ((c = getchar()) == ' ' || c == '\t')
        ;                            /* pula espaços */
    if (isdigit(c)) {
        ungetc(c, stdin);
        scanf("%d", &yylval);        /* valor semântico do token */
        return NUM;
    }
    if (c == EOF) return 0;          /* 0 = fim da entrada */
    return c;                        /* '+', '*', '\n'... viram o próprio caractere */
}

void yyerror(const char *s) { fprintf(stderr, "erro: %s\n", s); }

int main(void) { return yyparse(); }
"""

/** O mesmo léxico, agora gerado pelo Flex (arquivo calc.l). */
internal const val P3_REAL_CALC_L = """%{
#include <stdlib.h>
#include "calc.tab.h"   /* gerado por: bison -d calc.y */
%}
%%
[0-9]+       { yylval = atoi(yytext); return NUM; }
[-+*/()\n]   { return yytext[0]; }
[ \t]        ;            /* ignora espaços */
.            { fprintf(stderr, "caractere estranho: %s\n", yytext); }
%%
int yywrap(void) { return 1; }
"""

// =====================================================================
// SLR_TABLE — Tabela SLR(1) e conflitos
// =====================================================================

internal val slrLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.SLR_TABLE, Style.STORY, tx("lessonspart3_acaiteria_que_segue_quadro"),
        listOf(
            Say(NARRADOR, tx("lessonspart3_fortaleza_bottom_up_ala_oeste")),
            Say(BYTE, tx("lessonspart3_parece_planilha_excel_que_deu")),
            Say(PROF, tx("lessonspart3_cerebro_analisador_lr_tabela_action")),
            Text(tx("lessonspart3_atendente_monta_copos_acai_ela")),
            Text(tx("lessonspart3_ela_nao_pensa_olha_onde")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3_momento_pedido_que_ja_esta"),
                    tx("lessonspart3_proximo_pedido_cliente_token_lookahead"),
                    tx("lessonspart3_poe_no_copo_vai_para"),
                    tx("lessonspart3_troca_acai_granola_banana_por"),
                    tx("lessonspart3_com_classico_na_mao_va"),
                    tx("lessonspart3_que_pode_vir_depois_classico"),
                ),
            ),
            Say(PILHA, tx("lessonspart3_eu_sou_pilha_so_vejo")),
            Predict(
                tx("lessonspart3_copo_tem_acai_granola_banana"),
                listOf(
                    tx("lessonspart3_sempre_copo_completo_fecha_na"),
                    tx("lessonspart3_so_se_leite_condensado_puder"),
                    tx("lessonspart3_nunca_ela_so_empilha"),
                ),
                1,
                tx("lessonspart3_slr_so_coloca_reduz_nas"),
            ),
            Callout(WATCH_OUT, tx("lessonspart3_se_mesma_celula_quadro_mandar")),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = tx("lessonspart3_quadro_verdade_para_t_t")),
            Reveal(tx("lessonspart3_na_analogia_que_faz_papel"), tx("lessonspart3_lista_que_pode_vir_logo")),
            Callout(NO_DUMB_QUESTIONS, tx("lessonspart3_por_que_simple_porque_slr")),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.VISUAL, tx("lessonspart3_automato_tabela_celula_por_celula"),
        listOf(
            Say(PROF, tx("lessonspart3_hoje_gente_constroi_tabela_olhando")),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_EXPR, caption = tx("lessonspart3_colecao_canonica_lr_0_gramatica")),
            Steps(
                tx("lessonspart3_quatro_regras_para_encher_tabela"),
                listOf(
                    tx("lessonspart3_seta_com_terminal_ii_para"),
                    tx("lessonspart3_seta_com_nao_terminal_ii"),
                    tx("lessonspart3_item_completo_em_ii_rn"),
                    tx("lessonspart3_item_em_ii_action_i"),
                ),
            ),
            Visual(VisualKind.FIRST_FOLLOW, grammar = P3_EXPR, caption = "Vamos precisar do FOLLOW: FOLLOW(E) = { +, ), \$ } e FOLLOW(T) = FOLLOW(F) = { +, *, ), \$ }."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = tx("lessonspart3_tabela_pronta_procure_linha_estado")),
            Say(BYTE, tx("lessonspart3_perai_estado_2_tem_t")),
            Predict(
                tx("lessonspart3_no_estado_2_com_proximo"),
                listOf(tx("lessonspart3_reduzir_por_t"), tx("lessonspart3_empilhar_s7"), tx("lessonspart3_conflito_duas_coisas")),
                1,
                tx("lessonspart3_nao_esta_em_follow_entao"),
            ),
            Visual(VisualKind.LR_TRACE, grammar = P3_EXPR, input = "id + id * id", caption = tx("lessonspart3_siga_pilha_no_estado_9")),
            Say(PILHA, tx("lessonspart3_viu_eu_guardei_t_esperei")),
            Say(PROF, tx("lessonspart3_agora_gramatica_onde_follow_nao")),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = "S → L = R | R; L → * R | id; R → L. Ache a célula vermelha: estado 2, coluna '='."),
            Reveal(
                tx("lessonspart3_por_que_entrou_em_follow"),
                "L → * R joga FOLLOW(L) dentro de FOLLOW(R), e S → L = R coloca '=' em FOLLOW(L). Resultado: o SLR escreve r5 (R → L) em [2, =], junto com s6 de S → L • = R.",
            ),
            Callout(BRAIN_POWER, tx("lessonspart3_essa_gramatica_nao_ambigua_conflito")),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.WORKED, tx("lessonspart3_preenchendo_linhas_tabela_com_calma"),
        listOf(
            Say(PROF, tx("lessonspart3_exercicio_classico_prova_preencher_linhas")),
            Code(tx("lessonspart3_1_t_2_t_3"), tx("lessonspart3_producoes_numeradas_numero_vira_rn")),
            Code(tx("lessonspart3_i1_t_i6_i2_t"), tx("lessonspart3_tres_estados_automato_lr_0")),
            Text("FOLLOW(E) = { +, ), \$ }    FOLLOW(T) = { +, *, ), \$ }"),
            Steps(
                tx("lessonspart3_linha_estado_1"),
                listOf(
                    "E' → E • está completo e é o item aumentado → ACTION[1, \$] = **acc**",
                    "E → E • + T tem terminal depois do ponto, e há seta + → I6 → ACTION[1, +] = **s6**",
                    tx("lessonspart3_todo_resto_linha_fica_vazio"),
                ),
            ),
            Steps(
                tx("lessonspart3_linha_estado_2"),
                listOf(
                    "E → T • completo → **r2** nas colunas de FOLLOW(E): +, ) e \$",
                    "T → T • * F com seta * → I7 → ACTION[2, *] = **s7**",
                    tx("lessonspart3_alguma_celula_com_duas_acoes"),
                ),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = tx("lessonspart3_confira_linhas_1_2_na")),
            Say(BYTE, tx("lessonspart3_deixa_eu_tentar_eu_consigo")),
            Reveal(
                "Sua vez: estado 9 = { E → E + T • ; T → T • * F } com seta * → I7. Preencha a linha.",
                "r1 em +, ) e \$ (FOLLOW(E)); s7 em *. Sem conflito, pelo mesmo motivo do estado 2.",
            ),
            Reveal(
                "Agora o estado 10 = { T → T * F • }.",
                tx("lessonspart3_r3_em_todas_colunas_follow"),
            ),
            Say(PROF, tx("lessonspart3_ultimo_sem_rodinhas_gramatica_dos")),
            Reveal(
                "S → L = R | R; L → * R | id; R → L. Estado 2 = { S → L • = R ; R → L • }, seta = → I6, FOLLOW(R) = { =, \$ }. Preencha a linha.",
                tx("lessonspart3_action_2_s6_r5_conflito"),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = tx("lessonspart3_celula_2_com_duas_acoes")),
            Callout(EXAM_TIP, tx("lessonspart3_calcule_escreva_follow_todos_nao")),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.SOCRATIC, tx("lessonspart3_por_que_follow_decide_reducoes"),
        listOf(
            Say(PROF, tx("lessonspart3_hoje_eu_so_pergunto_responda")),
            Reveal(tx("lessonspart3_estado_tem_item_que_esse"), tx("lessonspart3_que_topo_pilha_contem_inteiro")),
            Reveal(tx("lessonspart3_lr_0_puro_reduz_em"), tx("lessonspart3_no_token_ele_quer_reduzir")),
            Reveal(tx("lessonspart3_se_reduzimos_para_que_tokens"), tx("lessonspart3_so_follow_se_proximo_token")),
            Say(BYTE, tx("lessonspart3_entao_slr_lr_0_com")),
            Say(PROF, tx("lessonspart3_exatamente_mesmo_automato_mesmos_estados")),
            Predict(
                tx("lessonspart3_se_token_estiver_em_follow"),
                listOf(tx("lessonspart3_slr_escolhe_shift_sozinho"), tx("lessonspart3_conflito_empilhar_reduzir_gramatica_nao"), tx("lessonspart3_slr_reduz_pela_regra_mais")),
                1,
                tx("lessonspart3_tabela_slr_nao_escolhe_nada"),
            ),
            Reveal(tx("lessonspart3_dois_itens_completos_b_no"), tx("lessonspart3_se_follow_follow_b_conflito")),
            Visual(VisualKind.SLR_TABLE, grammar = P3_AMBIG, caption = "E → E + E | E * E | ( E ) | id: conflitos nos estados 7 e 8, colunas + e *."),
            Reveal(tx("lessonspart3_toda_gramatica_ambigua_gera_conflito"), tx("lessonspart3_sim_nenhuma_gramatica_ambigua_lr")),
            Reveal(tx("lessonspart3_por_que_follow_grosseiro_demais"), tx("lessonspart3_ele_junta_tudo_que_pode")),
            Callout(BRAIN_POWER, tx("lessonspart3_pense_como_voce_carregaria_dentro")),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.FORMAL, tx("lessonspart3_construcao_tabela_slr_1_algoritmo"),
        listOf(
            Text(tx("lessonspart3_seja_g_gramatica_aumentada_c")),
            Code(
                tx("lessonspart3_para_cada_estado_ii_para"),
                tx("lessonspart3_algoritmo_slr_1_livro_dragao"),
            ),
            Text(tx("lessonspart3_definicao_g_slr_1_se")),
            Steps(
                tx("lessonspart3_dois_tipos_conflito"),
                listOf(
                    tx("lessonspart3_empilhar_reduzir_ii_contem_b"),
                    tx("lessonspart3_reduzir_reduzir_ii_contem_b"),
                ),
            ),
            Code(
                tx("lessonspart3_empilha_estado_0_primeiro_token"),
                tx("lessonspart3_algoritmo_analisador_lr_mesmo_para"),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = tx("lessonspart3_tabela_slr_1_gramatica_expressoes")),
            Predict(
                tx("lessonspart3_qual_afirmacao_verdadeira_sobre_classes"),
                listOf(tx("lessonspart3_toda_gramatica_slr_1_ll"), tx("lessonspart3_toda_gramatica_slr_1_lalr"), tx("lessonspart3_toda_gramatica_lalr_1_slr")),
                1,
                tx("lessonspart3_hierarquia_lr_0_slr_1"),
            ),
            Reveal(tx("lessonspart3_dimensoes_tabela"), tx("lessonspart3_linha_por_estado_lr_0")),
            Callout(WATCH_OUT, tx("lessonspart3_transicao_com_terminal_vira_shift")),
            Callout(EXAM_TIP, tx("lessonspart3_para_justificar_conflito_cite_estado")),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.MNEMONIC, tx("lessonspart3_porteiro_follow_torre_slr"),
        listOf(
            Say(NARRADOR, tx("lessonspart3_palacio_memoria_fortaleza_bottom_up")),
            Callout(MNEMONIC, tx("lessonspart3_slr_so_libera_reducao_se")),
            Code(
                tx("lessonspart3_antes_terminal_sn_empilha_vai"),
                tx("lessonspart3_cola_bolso"),
            ),
            Callout(MNEMONIC, tx("lessonspart3_ponto_na_frente_terminal_tem")),
            Callout(MNEMONIC, tx("lessonspart3_r_item_quer_andar_outro")),
            Say(LEXI, tx("lessonspart3_eu_corto_tokens_porteiro_so")),
            Predict(
                "No estado { S → L • = R ; R → L • } com '=' ∈ FOLLOW(R), o porteiro...",
                listOf(tx("lessonspart3_deixa_so_empilhar"), tx("lessonspart3_libera_reducao_seta_mostra_empilhar"), tx("lessonspart3_nao_deixa_nada")),
                1,
                tx("lessonspart3_porteiro_olha_so_prancheta_global"),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_LIST, caption = "S → S a | b: tabela pequena e sem conflitos. Recursão à esquerda não assusta o LR!"),
            Callout(FIRESIDE, tx("lessonspart3_lr_0_eu_reduzo_sempre")),
            Reveal(tx("lessonspart3_recite_quatro_regras_tabela_slr"), tx("lessonspart3_terminal_apos_ponto_shift_nao")),
            Callout(MNEMONIC, tx("lessonspart3_contraste_no_ll_1_follow")),
        ),
    ),
)

// =====================================================================
// LALR — LR(1) e LALR(1)
// =====================================================================

internal val lalrLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.LALR, Style.STORY, tx("lessonspart3_onibus_com_destino_na_testa"),
        listOf(
            Say(BYTE, tx("lessonspart3_slr_travou_naquela_gramatica_dos")),
            Say(PROF, tx("lessonspart3_nao_vamos_dar_ao_analisador")),
            Text(tx("lessonspart3_cada_item_passageiro_numa_linha")),
            Text(tx("lessonspart3_lr_1_cola_na_testa")),
            Steps(
                tx("comum_mapa_analogia"),
                listOf(
                    tx("lessonspart3_linha_onibus_nucleo_item"),
                    tx("lessonspart3_ponto_descida_na_testa_lookahead"),
                    tx("lessonspart3_descer_onibus_reduzir_so_quando"),
                    tx("lessonspart3_frota_enorme_onibus_por_combinacao"),
                    tx("lessonspart3_fundir_onibus_com_mesmas_linhas"),
                ),
            ),
            Say(PILHA, tx("lessonspart3_mais_onibus_mais_estados_mais")),
            Text(tx("lessonspart3_entao_empresa_funde_onibus_que")),
            Predict(
                tx("lessonspart3_onibus_1_passageiro_desce_em"),
                listOf(tx("lessonspart3_nada_so_desce"), tx("lessonspart3_b_querem_descer_confusao_reduzir"), tx("lessonspart3_dois_continuam_no_onibus")),
                1,
                tx("lessonspart3_fundidos_b_tem_d_na"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart3_fusao_nunca_cria_conflito_empilhar")),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = tx("lessonspart3_slr_trava_em_2_no")),
            Reveal(tx("lessonspart3_na_analogia_que_nucleo_estado"), tx("lessonspart3_conjunto_linhas_itens_sem_lookahead")),
            Callout(REAL_WORLD, tx("lessonspart3_yacc_bison_geram_por_padrao")),
        ),
    ),
    Lesson(
        Skill.LALR, Style.VISUAL, tx("lessonspart3_vendo_fusao_nucleos_acontecer"),
        listOf(
            Say(PROF, tx("lessonspart3_mesma_gramatica_dos_ponteiros_agora")),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_PTR, caption = tx("lessonspart3_10_estados_lr_0_lalr")),
            Code(
                tx("lessonspart3_i0_l_r_r_l"),
                tx("lessonspart3_itens_lr_1_nucleo_lookahead"),
            ),
            Say(BYTE, tx("lessonspart3_no_estado_2_r_l")),
            Say(PROF, tx("lessonspart3_isso_lookahead_item_veio_contexto")),
            Code(
                tx("lessonspart3_lr_1_canonico_14_estados"),
                tx("lessonspart3_pares_com_mesmo_nucleo_numeracao"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_PTR, input = "*x=y", caption = tx("lessonspart3_bison_lalr_aceita_x_y")),
            Say(PROF, tx("lessonspart3_agora_lado_sombrio_fusao")),
            Code(
                """S → a A d | b B d | a B e | b A e
A → c        B → c

LR(1):  I6 = { [A → c •, d]  [B → c •, e] }   ← depois de 'a c'
        I9 = { [B → c •, d]  [A → c •, e] }   ← depois de 'b c'
LALR:   I6+I9 = { [A → c •, d/e]  [B → c •, d/e] }  ← R/R em d e em e!""",
                tx("lessonspart3_gramatica_lr_1_mas_nao"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_RR, input = tx("lessonspart3_c_d"), caption = tx("lessonspart3_bison_avisa_2_conflitos_reduzir")),
            Predict(
                tx("lessonspart3_com_essa_escolha_padrao_bison"),
                listOf(tx("lessonspart3_aceita"), tx("lessonspart3_syntax_error_mesmo_sendo_valida"), tx("lessonspart3_bison_pergunta_ao_usuario")),
                1,
                tx("lessonspart3_em_ele_reduz_c_para"),
            ),
            Callout(BRAIN_POWER, tx("lessonspart3_note_lr_1_separava_c")),
        ),
    ),
    Lesson(
        Skill.LALR, Style.WORKED, tx("lessonspart3_closure_lr_1_fusao_passo"),
        listOf(
            Say(PROF, tx("lessonspart3_vamos_calcular_i0_gramatica_l")),
            Steps(
                "closure({ [S' → • S, \$] })",
                listOf(
                    tx("lessonspart3_comeca_com"),
                    tx("lessonspart3_ponto_antes_que_vem_depois"),
                    "De [S → • L = R, \$]: depois de L vem '= R', FIRST = { = } → [L → • * R, =] e [L → • id, =]",
                    tx("lessonspart3_r_depois_r_vem_nada"),
                    tx("lessonspart3_r_l_l_r_l"),
                    tx("lessonspart3_nada_novo_pronto_compactando_l"),
                ),
            ),
            Callout(WATCH_OUT, tx("lessonspart3_regra_first_onde_que_vem")),
            Steps(
                "GOTO(I0, L) = I2",
                listOf(
                    tx("lessonspart3_avanca_ponto_sobre_l_l"),
                    tx("lessonspart3_closure_nao_acrescenta_nada_ponto"),
                    tx("lessonspart3_tabela_shift_r_l_reduz"),
                ),
            ),
            Say(BYTE, tx("lessonspart3_agora_eu_agora_eu")),
            Reveal(
                tx("lessonspart3_calcule_goto_i0"),
                tx("lessonspart3_l_r_fechando_r_l"),
            ),
            Reveal(
                tx("lessonspart3_no_lr_1_ha_l"),
                tx("lessonspart3_fundem_mesmo_nucleo_l_id"),
            ),
            Reveal(
                tx("lessonspart3_na_gramatica_d_b_b"),
                tx("lessonspart3_c_d_b_c_b"),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_RR, caption = tx("lessonspart3_curiosidade_slr_tambem_tem_esse")),
            Callout(EXAM_TIP, tx("lessonspart3_questao_tipica_mostre_que_g")),
        ),
    ),
    Lesson(
        Skill.LALR, Style.SOCRATIC, tx("lessonspart3_se_item_soubesse_futuro"),
        listOf(
            Say(PROF, tx("lessonspart3_lembra_pergunta_ultima_aula_como")),
            Reveal(tx("lessonspart3_qual_ideia_mais_simples"), tx("lessonspart3_grudar_terminal_no_item_significa")),
            Reveal(tx("lessonspart3_onde_vem_lookahead_dos_itens"), tx("lessonspart3_item_que_criou_em_b")),
            Reveal(tx("lessonspart3_por_que_isso_resolve_l"), tx("lessonspart3_no_estado_2_r_l_2")),
            Reveal(tx("lessonspart3_qual_preco"), tx("lessonspart3_muitos_estados_mesmo_nucleo_aparece")),
            Say(BYTE, tx("lessonspart3_se_gente_juntasse_estados_que")),
            Say(PROF, tx("lessonspart3_voce_acabou_inventar_lalr_1")),
            Predict(
                tx("lessonspart3_fusao_estados_com_mesmo_nucleo"),
                listOf(tx("comum_sim_sempre"), tx("lessonspart3_nao_se_surgisse_depois_ele"), tx("lessonspart3_so_se_gramatica_for_ambigua")),
                1,
                tx("lessonspart3_shifts_dependem_so_nucleo_se"),
            ),
            Reveal(tx("lessonspart3_reduzir_reduzir_novo"), tx("lessonspart3_pode_dois_estados_sem_conflito")),
            Reveal(tx("lessonspart3_entao_por_que_bison_usa"), tx("lessonspart3_porque_lalr_tem_numero_estados")),
            Callout(FIRESIDE, tx("lessonspart3_slr_eu_uso_follow_pronto")),
        ),
    ),
    Lesson(
        Skill.LALR, Style.FORMAL, tx("lessonspart3_itens_lr_1_closure_fusao"),
        listOf(
            Text(tx("lessonspart3_item_lr_1_par_com")),
            Code(
                tx("lessonspart3_closure_i_repita_para_cada"),
                tx("lessonspart3_construcao_colecao_canonica_lr_1"),
            ),
            Text(tx("lessonspart3_tabela_lr_1_igual_slr")),
            Code(
                tx("lessonspart3_lalr_1_1_construa_colecao"),
                tx("lessonspart3_construcao_lalr_1_por_fusao"),
            ),
            Steps(
                tx("lessonspart3_propriedades_que_caem_em_prova"),
                listOf(
                    tx("lessonspart3_no_estados_lalr_1_no"),
                    tx("lessonspart3_fusao_nunca_introduz_conflito_empilhar"),
                    tx("lessonspart3_gramaticas_slr_1_lalr_1"),
                    tx("lessonspart3_lookaheads_lalr_follow_por_isso"),
                ),
            ),
            Predict(
                tx("lessonspart3_em_b_quais_lookaheads_ganham"),
                listOf("FOLLOW(B)", tx("comum_first"), "FIRST(B)"),
                1,
                tx("lessonspart3_first_se_nao_deriva_nem"),
            ),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_RR, caption = tx("lessonspart3_nucleos_lr_0_d_b")),
            Callout(EXAM_TIP, tx("lessonspart3_escreva_itens_lr_1_agrupados")),
            Callout(WATCH_OUT, tx("lessonspart3_nao_confunda_lalr_look_ahead")),
        ),
    ),
    Lesson(
        Skill.LALR, Style.MNEMONIC, tx("lessonspart3_alfaiate_que_funde_gemeos"),
        listOf(
            Say(NARRADOR, tx("lessonspart3_palacio_memoria_andar_mais_alto")),
            Callout(MNEMONIC, tx("lessonspart3_mesmo_esqueleto_roupas_somadas_estados")),
            Callout(MNEMONIC, tx("lessonspart3_oculos_lr_0_sem_oculos")),
            Callout(MNEMONIC, tx("lessonspart3_fusao_so_faz_reduce_duplo")),
            Code(
                tx("lessonspart3_forca_lr_0_slr_1"),
                tx("lessonspart3_duas_linhas_para_tatuar"),
            ),
            Say(BYTE, tx("lessonspart3_entao_lalr_custo_beneficio_familia")),
            Say(PROF, tx("lessonspart3_isso_por_isso_yacc_bison")),
            Predict(
                tx("lessonspart3_alfaiate_fundiu_dois_gemeos_sem"),
                listOf(tx("lessonspart3_sim_fusao_criou"), tx("lessonspart3_nao_se_tem_r_dos"), tx("lessonspart3_so_em_gramatica_ambigua")),
                1,
                tx("lessonspart3_shift_depende_so_esqueleto_se"),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_PTR, input = "x=*y", caption = tx("lessonspart3_bisao_lalr_engole_gramatica_dos")),
            Reveal(tx("lessonspart3_qual_exemplo_gancho_lalr_mas"), "S → L = R | R; L → * R | id; R → L. E: S → a A d | b B d | a B e | b A e com A → c, B → c."),
            Callout(FIRESIDE, tx("lessonspart3_slr_vs_lalr_ao_pe")),
        ),
    ),
)

// =====================================================================
// Agregadores (ficam no FIM do arquivo: propriedades de topo são
// inicializadas em ordem textual).
// =====================================================================

val lessonsPart3: List<Lesson> =
    slrLessonsP3 + lalrLessonsP3 + bisonLessonsP3 + attrLessonsP3 + scopeLessonsP3 + tacLessonsP3 + optLessonsP3

val cardsPart3: List<SkillCard> = p3Cards
