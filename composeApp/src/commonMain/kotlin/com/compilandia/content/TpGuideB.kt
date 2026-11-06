package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Guia do Trabalho Prático — capítulos 4 a 6 (gramática LL(1), parser descendente, tabela de símbolos).

/** Expressões da Byteiro, versão "ingênua" com recursão à esquerda (recortada para caber no widget). */
private const val TP_G_EXPR_RECURSIVA = """Add -> Add + Mul | Add - Mul | Mul
Mul -> Mul * Un | Un
Un -> - Un | ( Add ) | id | num"""

/** Se a Byteiro tivesse chamada de procedimento: prefixos comuns por todo lado. */
private const val TP_G_PREFIXOS = """Cmd -> id <- E ; | id ( E ) ; | se ( E ) B | se ( E ) B senao B
B -> { }
E -> id | num"""

/** Comandos da Byteiro em BNF (expressões resumidas em E). É LL(1). */
internal const val TP_G_COMANDOS = """Cmd -> id <- E ; | se ( E ) Bloco Senao | enquanto ( E ) Bloco | escreva ( E ) ;
Senao -> senao Bloco | ε
Bloco -> { Cmds }
Cmds -> Cmd Cmds | ε
E -> id | num"""

/** Cascata de precedência em BNF LL(1) (dois níveis). */
private const val TP_G_CASCATA = """E -> T E'
E' -> + T E' | ε
T -> F T'
T' -> * F T' | ε
F -> ( E ) | id | num"""

/** Lista de comandos para a demonstração do modo pânico. */
private const val TP_G_PANICO = """Cmds -> Cmd Cmds | ε
Cmd -> id <- E ; | escreva ( E ) ;
E -> id | num"""

// ───────────────────────────── Capítulo 4 ─────────────────────────────

internal val tpCap4 = GuideChapter(
    4, tx("tpguideb_gramatica_ll_1_byteiro"),
    tx("tpguideb_tirar_recursao_esquerda_fatorar_provar"),
    Skill.LL1_TABLE,
    listOf(
        Say(PROF, tx("tpguideb_gramatica_informal_capitulo_2_otima")),
        Steps(
            tx("tpguideb_tres_checagens_antes_programar"),
            listOf(
                tx("tpguideb_1_sem_recursao_esquerda_nenhum"),
                tx("tpguideb_2_fatorada_duas_alternativas_mesmo"),
                tx("tpguideb_3_first_follow_sem_cruzamento"),
            ),
        ),
        Code(
            """
            Expr  -> Expr ou ExprE | ExprE
            ExprE -> ExprE e Rel | Rel
            Rel   -> Add oprel Add | Add
            Add   -> Add + Mul | Add - Mul | Mul
            Mul   -> Mul * Un | Mul / Un | Mul % Un | Un
            Un    -> nao Un | - Un | Prim
            Prim  -> ( Expr ) | ID | NUM | CHAR | verdadeiro | falso
            """.trimIndent(),
            tx("tpguideb_primeira_tentativa_precedencia_certinha_recu"),
        ),
        Predict(
            tx("tpguideb_que_acontece_se_voce_traduzir"),
            listOf(
                tx("tpguideb_funciona_normalmente"),
                tx("tpguideb_stackoverflowerror_add_chama_add_sem"),
                tx("comum_erro_compilacao_java"),
                tx("tpguideb_funciona_mas_com_associatividade_errada"),
            ),
            1,
            tx("tpguideb_primeira_coisa_que_add_faz"),
        ),
        Visual(VK.LEFT_RECURSION, grammar = TP_G_EXPR_RECURSIVA, caption = tx("tpguideb_antes_depois_vira")),
        Text(tx("tpguideb_na_ebnf_fica_ainda_mais")),
        Code(
            """
            expr     -> exprE { ou exprE }
            exprE    -> exprRel { e exprRel }
            exprRel  -> exprAdd [ oprel exprAdd ]        oprel = "=" | "<>" | "<" | ">" | "<=" | ">="
            exprAdd  -> exprMul { ( + | - ) exprMul }
            exprMul  -> exprUn { ( * | / | % ) exprUn }
            exprUn   -> nao exprUn | - exprUn | primario
            primario -> ( expr ) | ID | NUM | CHAR | verdadeiro | falso
            """.trimIndent(),
            tx("tpguideb_expressoes_byteiro_em_ebnf_ll"),
        ),
        Callout(MNEMONIC, tx("tpguideb_cascata_quanto_mais_fundo_metodo")),
        Text(tx("tpguideb_rel_add_oprel_add_add")),
        Visual(VK.LEFT_FACTORING, grammar = TP_G_PREFIXOS, caption = tx("tpguideb_se_byteiro_tivesse_chamada_procedimento")),
        Code(TP_G_COMANDOS, tx("tpguideb_comandos_byteiro_em_bnf_expressoes")),
        Visual(VK.FIRST_FOLLOW, grammar = TP_G_COMANDOS, caption = tx("tpguideb_calcule_first_follow_passo_passo")),
        Visual(VK.LL1_TABLE, grammar = TP_G_COMANDOS, caption = tx("tpguideb_nenhuma_celula_com_duas_producoes")),
        Predict(
            tx("tpguideb_em_senao_senao_bloco_quando"),
            listOf(
                tx("tpguideb_nunca_so_enfeite"),
                tx("tpguideb_quando_token_atual_esta_em"),
                tx("tpguideb_quando_token_atual_senao"),
                tx("tpguideb_quando_arquivo_acaba"),
            ),
            1,
            tx("tpguideb_tabela_poe_nas_colunas_follow"),
        ),
        Callout(WATCH_OUT, tx("tpguideb_se_sua_linguagem_permitir_se")),
        Reveal(tx("tpguideb_calcule_first_comando_byteiro_completa"), tx("tpguideb_id_se_enquanto_escreva_leia")),
        Reveal(tx("tpguideb_follow_comando_dentro_bloco_quando"), tx("tpguideb_quando_token_nao_esta_em")),
        Callout(NO_DUMB_QUESTIONS, tx("tpguideb_p_preciso_entregar_tabela_ll")),
        Callout(EXAM_TIP, tx("tpguideb_trio_que_cai_em_prova")),
        Say(PILHA, tx("tpguideb_gramatica_ll_1_pronta_agora")),
    ),
)

// ───────────────────────────── Capítulo 5 ─────────────────────────────

internal val tpCap5 = GuideChapter(
    5, tx("tpguideb_parser_descendente_recursivo"),
    tx("tpguideb_metodo_por_nao_terminal_casatoken"),
    Skill.RECURSIVE_DESCENT,
    listOf(
        Say(PILHA, tx("tpguideb_eu_sou_pilha_chamadas_java")),
        Steps(
            tx("tpguideb_receita_decore"),
            listOf(
                tx("tpguideb_metodo_java_para_cada_nao"),
                tx("tpguideb_terminal_no_lado_direito_casatoken"),
                tx("tpguideb_nao_terminal_no_lado_direito"),
                tx("tpguideb_varias_alternativas_if_switch_olhando"),
                "`{ … }` → `while`; `[ … ]` → `if`.",
            ),
        ),
        Visual(VK.DESCENT_CODE, grammar = TP_G_CASCATA, input = tx("tpguideb_id_num_id"), caption = tx("tpguideb_codigo_que_motor_gera_para")),
        Code(
            tx("tpguideb_public_class_sintatico_private_final"),
            tx("tpguideb_sintatico_java_1_4_infraestrutura"),
        ),
        Code(
            """
                private void programa() {
                    casaToken(TipoToken.PROGRAMA);
                    casaToken(TipoToken.ID);
                    bloco();
                    casaToken(TipoToken.EOF);              // nada pode sobrar depois do '}' final
                }

                private void bloco() {
                    casaToken(TipoToken.ABRE_CHAVE);
                    while (comecaComando()) comando();     // { comando }
                    casaToken(TipoToken.FECHA_CHAVE);
                }

                private boolean comecaComando() {         // FIRST(comando)
                    return eh(TipoToken.INTEIRO, TipoToken.LOGICO, TipoToken.CARACTERE, TipoToken.ID,
                              TipoToken.SE, TipoToken.ENQUANTO, TipoToken.ESCREVA, TipoToken.LEIA,
                              TipoToken.ABRE_CHAVE);
                }

                private void comando() {
                    switch (tok.tipo) {
                        case INTEIRO, LOGICO, CARACTERE -> declaracao();
                        case ID         -> atribuicao();
                        case SE         -> se();
                        case ENQUANTO   -> enquanto();
                        case ESCREVA    -> escreva();
                        case LEIA       -> leia();
                        case ABRE_CHAVE -> bloco();
                        default         -> throw erroEsperava("um comando");
                    }
                }
            """.trimIndent(),
            tx("tpguideb_sintatico_java_2_4_programa"),
        ),
        Code(
            """
                private void declaracao() {               // tipo ID { , ID } ;
                    tipo();
                    casaToken(TipoToken.ID);
                    while (eh(TipoToken.VIRGULA)) {
                        avanca();
                        casaToken(TipoToken.ID);
                    }
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private void tipo() {
                    if (eh(TipoToken.INTEIRO, TipoToken.LOGICO, TipoToken.CARACTERE)) avanca();
                    else throw erroEsperava("um tipo");
                }

                private void atribuicao() {               // ID <- expr ;
                    casaToken(TipoToken.ID);
                    casaToken(TipoToken.ATRIB);
                    expr();
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private void se() {                       // se ( expr ) bloco [ senao bloco ]
                    casaToken(TipoToken.SE);
                    casaToken(TipoToken.ABRE_PAR);
                    expr();
                    casaToken(TipoToken.FECHA_PAR);
                    bloco();
                    if (eh(TipoToken.SENAO)) {
                        avanca();
                        bloco();
                    }
                }

                private void enquanto() {                 // enquanto ( expr ) bloco
                    casaToken(TipoToken.ENQUANTO);
                    casaToken(TipoToken.ABRE_PAR);
                    expr();
                    casaToken(TipoToken.FECHA_PAR);
                    bloco();
                }

                private void escreva() {                  // escreva ( arg { , arg } ) ;
                    casaToken(TipoToken.ESCREVA);
                    casaToken(TipoToken.ABRE_PAR);
                    argumento();
                    while (eh(TipoToken.VIRGULA)) { avanca(); argumento(); }
                    casaToken(TipoToken.FECHA_PAR);
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private void argumento() {                // STRING | expr
                    if (eh(TipoToken.STRING)) avanca();
                    else expr();
                }

                private void leia() {                     // leia ( ID ) ;
                    casaToken(TipoToken.LEIA);
                    casaToken(TipoToken.ABRE_PAR);
                    casaToken(TipoToken.ID);
                    casaToken(TipoToken.FECHA_PAR);
                    casaToken(TipoToken.PONTO_VIRGULA);
                }
            """.trimIndent(),
            tx("tpguideb_sintatico_java_3_4_comandos"),
        ),
        Code(
            tx("tpguideb_private_void_expr_expre_ou"),
            tx("tpguideb_sintatico_java_4_4_cascata"),
        ),
        Reveal(tx("tpguideb_por_que_exprrel_usa_if"), tx("tpguideb_porque_b_c_permitido_repeticao")),
        Steps(
            "A pilha de chamadas para `x <- a + 2 * b;`",
            listOf(
                tx("tpguideb_atribuicao_casa_id_x_depois"),
                tx("tpguideb_expr_expre_exprrel_expradd_exprmul"),
                tx("tpguideb_expradd_ve_avanca_chama_exprmul"),
                tx("tpguideb_exprmul_exprun_primario_casa_2"),
                tx("tpguideb_exprmul_nao_ve_mais_retorna"),
                tx("tpguideb_atribuicao_casa_repare_2_b"),
            ),
        ),
        Code(
            """
            programa t {
                inteiro x
                x <- 1;
            }
            """.trimIndent(),
            "erro_falta_pv.byt — esqueceram o ';' da linha 2",
        ),
        Predict(
            tx("tpguideb_que_mensagem_nosso_parser_imprime"),
            listOf(
                tx("tpguideb_linha_2_erro_sintatico_esperava"),
                tx("tpguideb_linha_3_erro_sintatico_esperava"),
                tx("tpguideb_linha_4_erro_sintatico_esperava"),
                tx("tpguideb_nenhuma_programa_compila"),
            ),
            1,
            tx("tpguideb_parser_so_percebe_que_falta"),
        ),
        Callout(WATCH_OUT, tx("tpguideb_esqueceu_casatoken_tipotoken_eof_no")),
        Callout(WATCH_OUT, tx("tpguideb_chamar_lexico_proximo_direto_em")),
        Visual(VK.DESCENT_CODE, grammar = TP_G_COMANDOS, input = "se ( id ) { }", caption = tx("tpguideb_agora_com_comandos_byteiro_veja")),
        Callout(NO_DUMB_QUESTIONS, tx("tpguideb_p_se_enunciado_pedir_para")),
        Visual(VK.PANIC, grammar = TP_G_PANICO, input = "id <- ; escreva ( id ) ;", caption = tx("tpguideb_atribuicao_sem_expressao_parser_reclama")),
        Callout(REAL_WORLD, tx("tpguideb_parsers_gcc_c_c_clang")),
        Callout(EXAM_TIP, tx("tpguideb_questao_classica_escreva_procedimento_descid")),
    ),
)

// ───────────────────────────── Capítulo 6 ─────────────────────────────

internal val tpCap6 = GuideChapter(
    6, tx("tpguideb_tabela_simbolos_escopos"),
    tx("tpguideb_pilha_hashmaps_abre_bloco_push"),
    Skill.SCOPE_SYMBOLS,
    listOf(
        Say(BYTE, tx("tpguideb_meu_parser_aceita_x_1")),
        Say(PROF, tx("tpguideb_ele_precisa_memoria_tabela_simbolos")),
        Code(
            tx("tpguideb_programa_escopos_inteiro_x_x"),
            tx("tpguideb_escopos_byt_leia_cima_para"),
        ),
        Visual(VK.SCOPE, caption = tx("tpguideb_toque_em_cada_uso_veja")),
        Predict(
            "O que o compilador diz na linha 11 (`y <- 3;`)?",
            listOf(
                tx("tpguideb_nada_y_foi_declarado_no"),
                tx("tpguideb_linha_11_erro_semantico_y"),
                tx("tpguideb_linha_7_erro_semantico_y"),
                tx("tpguideb_erro_sintatico"),
            ),
            1,
            tx("tpguideb_quando_bloco_interno_fechou_escopo"),
        ),
        Steps(
            tx("tpguideb_pilha_hashmaps_em_quatro_operacoes"),
            listOf(
                "`{` → **abreEscopo**: empilha um HashMap vazio.",
                tx("tpguideb_inteiro_x_declara_poe_x"),
                tx("tpguideb_uso_x_busca_procura_topo"),
                "`}` → **fechaEscopo**: desempilha — tudo que foi declarado no bloco morre junto.",
            ),
        ),
        Code(
            tx("tpguideb_public_enum_tipodado_inteiro_logico"),
            tx("tpguideb_tipodado_java_simbolo_java"),
        ),
        Code(
            tx("tpguideb_import_java_util_arraydeque_import"),
            tx("tpguideb_tabelasimbolos_java_classe_inteira_cabe"),
        ),
        Say(PILHA, tx("tpguideb_busca_eu_comeco_pelo_topo")),
        Callout(WATCH_OUT, tx("tpguideb_armadilha_java_se_voce_usar")),
        Callout(NO_DUMB_QUESTIONS, tx("tpguideb_p_por_que_rotulo_x")),
        Code(
            tx("tpguideb_private_final_tabelasimbolos_tabela_new"),
            tx("tpguideb_no_sintatico_parser_agora_abre"),
        ),
        Text(tx("tpguideb_onde_chamar_usa_em_todo")),
        Steps(
            tx("tpguideb_tabela_durante_escopos_byt"),
            listOf(
                "Linha 1, `{` do programa: pilha = [ {} ]",
                "Linha 2: pilha = [ {x→x_0 inteiro} ]",
                tx("tpguideb_linha_4_pilha_x_x"),
                "Linhas 5 e 7: topo = {x→x_1 caractere, y→y_2 inteiro}",
                tx("tpguideb_linha_6_busca_x_acha"),
                "Linha 9, `}`: desempilha. Pilha = [ {x→x_0} ]",
                tx("tpguideb_linha_10_busca_x_acha"),
            ),
        ),
        Callout(NO_DUMB_QUESTIONS, tx("tpguideb_p_pode_declarar_x_no")),
        Callout(EXAM_TIP, tx("tpguideb_questao_classica_dado_programa_com")),
        Callout(REAL_WORLD, tx("tpguideb_compiladores_reais_guardam_bem_mais")),
        Reveal(tx("tpguideb_se_byteiro_ganhasse_funcoes_com"), tx("tpguideb_cada_funcao_abriria_escopo_para")),
    ),
)
