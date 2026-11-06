package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

// Guia do Trabalho Prático — capítulos 7 e 8 (verificação de tipos, geração de MIPS).

// ───────────────────────────── Capítulo 7 ─────────────────────────────

internal val tpCap7 = GuideChapter(
    7, tx("tpguidec_verificacao_tipos"),
    tx("tpguidec_cada_metodo_expressao_devolve_tipo"),
    Skill.ATTRIBUTES_TYPES,
    listOf(
        Say(BYTE, tx("tpguidec_olha_isso_logico_ok_ok")),
        Say(PROF, tx("tpguidec_deveria_forma_esta_certa_sentido")),
        Visual(VK.ATTR_TREE, input = tx("tpguidec_1_b_ok"), caption = tx("tpguidec_tipo_subindo_pela_arvore_no")),
        Code(
            tx("tpguidec_operador_operandos_resultado_numerico_numeri"),
            tx("tpguidec_regras_tipo_das_expressoes_byteiro"),
        ),
        Code(
            tx("tpguidec_destino_valor_resultado_inteiro_inteiro"),
            tx("tpguidec_regras_atribuicao_conversao_implicita_so"),
        ),
        Predict(
            tx("tpguidec_com_inteiro_n_caractere_c"),
            listOf("n <- c + 1;", "n <- c;", "c <- c + 1;", "c <- 'Z';"),
            2,
            tx("tpguidec_c_1_numerico_numerico_inteiro"),
        ),
        Code(
            tx("tpguidec_private_static_boolean_numerico_tipodado"),
            tx("tpguidec_ajudantes_tipo_no_sintatico"),
        ),
        Code(
            tx("tpguidec_private_tipodado_expradd_tipodado_t"),
            tx("tpguidec_cascata_agora_devolve_tipodado_mesmo"),
        ),
        Code(
            tx("tpguidec_private_tipodado_exprun_if_eh"),
            tx("tpguidec_folhas_arvore_literais_tem_tipo"),
        ),
        Code(
            tx("tpguidec_private_void_atribuicao_token_id"),
            tx("tpguidec_comandos_atribuicao_confere_compatibilidade"),
        ),
        Callout(WATCH_OUT, tx("tpguidec_bug_campeao_em_exprrel_esquecer")),
        Steps(
            tx("solta_tpguidec_1"),
            listOf(
                "`primario` de c → CARACTERE; de 1 → INTEIRO.",
                tx("tpguidec_expradd_numerico_numerico_inteiro"),
                tx("tpguidec_primario_n_inteiro_exprrel_inteiro"),
                "`exprUn`: nao LOGICO → LOGICO.",
                tx("tpguidec_expre_logico_logico_logico"),
                tx("tpguidec_atribuicao_podeatribuir_logico_logico"),
            ),
        ),
        Predict(
            "`inteiro n; … se (n) { … }` na Byteiro:",
            listOf(
                tx("tpguidec_compila_n_diferente_0_verdadeiro"),
                tx("tpguidec_erro_semantico_condicao_precisa_ser"),
                tx("tpguidec_erro_sintatico_esperava_comparacao"),
                tx("tpguidec_compila_com_aviso"),
            ),
            1,
            tx("tpguidec_byteiro_estilo_java_condicao_tem"),
        ),
        Callout(REAL_WORLD, tx("tpguidec_cada_linguagem_escolhe_c_converte")),
        Callout(NO_DUMB_QUESTIONS, tx("tpguidec_p_conversao_caractere_inteiro_gera")),
        Code(
            tx("tpguidec_programa_t_logico_ok_ok"),
            tx("tpguidec_cinco_testes_semanticos_com_saida"),
        ),
        Callout(EXAM_TIP, tx("tpguidec_atributo_sintetizado_sobe_tipo_expressao")),
        Reveal(tx("tpguidec_verificacao_tipos_precisa_ser_fase"), tx("tpguidec_nao_necessariamente_no_esquema_passada")),
    ),
)

// ───────────────────────────── Capítulo 8 ─────────────────────────────

internal const val TP_ASM_CONTAGEM = $$"""# gerado pelo compilador Byteiro
.data
n_0: .word 0
soma_1: .word 0
c_2: .word 0
str0: .asciiz "soma = "
str1: .asciiz "pouco: "
.text
main:
    li $t0, 5
    sw $t0, n_0
    li $t0, 0
    sw $t0, soma_1
enquanto0:
    lw $t0, n_0
    li $t1, 0
    sgt $t0, $t0, $t1
    beq $t0, $zero, fimenq1
    lw $t0, soma_1
    lw $t1, n_0
    li $t2, 2
    mul $t1, $t1, $t2
    add $t0, $t0, $t1
    sw $t0, soma_1
    lw $t0, n_0
    li $t1, 1
    sub $t0, $t0, $t1
    sw $t0, n_0
    j enquanto0
fimenq1:
    li $t0, 65
    sw $t0, c_2
    lw $t0, soma_1
    li $t1, 30
    sge $t0, $t0, $t1
    lw $t1, n_0
    li $t2, 0
    sne $t1, $t1, $t2
    xori $t1, $t1, 1
    and $t0, $t0, $t1
    beq $t0, $zero, senao2
    la $a0, str0
    li $v0, 4
    syscall
    lw $t0, soma_1
    move $a0, $t0
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    j fimse3
senao2:
    la $a0, str1
    li $v0, 4
    syscall
    lw $t0, c_2
    move $a0, $t0
    li $v0, 11
    syscall
    li $a0, 10
    li $v0, 11
    syscall
fimse3:
    li $v0, 10
    syscall"""

internal val tpCap8 = GuideChapter(
    8, tx("tpguidec_gerando_mips_para_mars"),
    tx("tpguidec_expressoes_em_registradores_temporarios_vari"),
    Skill.CODEGEN_MIPS,
    listOf(
        Say(PROF, tx("tpguidec_hora_forja_cada_metodo_alem")),
        Code(
            $$"""
            .data                         # seção de dados (variáveis globais)
            n_0:  .word 0                 # um inteiro de 32 bits, começa em 0
            str0: .asciiz "soma = "       # string terminada em \0
            .text                         # seção de código
            main:
                li   $t0, 42              # $t0 = 42                     (load immediate)
                lw   $t0, n_0             # $t0 = memória[n_0]           (load word)
                sw   $t0, n_0             # memória[n_0] = $t0           (store word)
                add  $t0, $t0, $t1        # também sub, mul, div, rem, and, or
                slt  $t0, $t0, $t1        # $t0 = ($t0 < $t1) ? 1 : 0    (e sgt, sle, sge, seq, sne)
                xori $t0, $t0, 1          # troca 0 <-> 1: o nosso 'nao'
                beq  $t0, $zero, rotulo   # se $t0 == 0, pula para rotulo
                j    rotulo               # pula sempre
            rotulo:                       # um rótulo dá nome a um endereço do código
                la   $a0, str0            # $a0 = ENDEREÇO da string
                li   $v0, 1               # escolhe o serviço do syscall:
                syscall                   #   1 = escreve inteiro de $a0   4 = string   11 = caractere
                                          #   5 = lê inteiro em $v0       12 = lê caractere   10 = encerra
            """.trimIndent(),
            tx("tpguidec_cola_mips_tp_so_isto"),
        ),
        Visual(VK.MIPS, input = tx("comum_b_c_d"), caption = tx("solta_tpguidec_2")),
        Text(tx("solta_tpguidec_3")),
        Code(
            $$"""
                private record Res(TipoDado tipo, String reg) {}             // tipo + onde está o valor

                private final StringBuilder dados   = new StringBuilder();   // .word das variáveis
                private final StringBuilder strings = new StringBuilder();   // .asciiz do escreva
                private final StringBuilder codigo  = new StringBuilder();   // .text
                private int proxTemp = 0, proxRotulo = 0, proxString = 0;

                private void emite(String instr)       { codigo.append("    ").append(instr).append('\n'); }
                private void rotulo(String r)          { codigo.append(r).append(":\n"); }
                private String novoRotulo(String base) { return base + (proxRotulo++); }

                private String novoTemp() {                                   // $t0..$t9, em pilha
                    if (proxTemp > 9)
                        throw new ErroCompilacao(tok.linha, "expressão complexa demais (faltaram registradores)");
                    return "$t" + (proxTemp++);
                }
                private void liberaTemp() { proxTemp--; }

                public String compila() {
                    programa();
                    emite("li $v0, 10");                                      // syscall 10 = encerra
                    emite("syscall");
                    return "# gerado pelo compilador Byteiro\n.data\n" + dados + strings
                         + ".text\nmain:\n" + codigo;
                }

                private void declaraUm(TipoDado t) {
                    Token id = casaToken(TipoToken.ID);
                    Simbolo s = tabela.declara(id.lexema, t, id.linha);
                    dados.append(s.rotulo).append(": .word 0\n");             // toda variável ganha um .word
                }
            """.trimIndent(),
            tx("tpguidec_infraestrutura_gerador_dentro_sintatico_java"),
        ),
        Code(
            $$"""
                private Res primario() {
                    Token t = tok;
                    if (eh(TipoToken.ABRE_PAR)) {
                        avanca();
                        Res e = expr();
                        casaToken(TipoToken.FECHA_PAR);
                        return e;
                    }
                    if (eh(TipoToken.ID)) {
                        avanca();
                        Simbolo s = usa(t);
                        String r = novoTemp();
                        emite("lw " + r + ", " + s.rotulo);
                        return new Res(s.tipo, r);
                    }
                    if (eh(TipoToken.NUM, TipoToken.CHAR, TipoToken.VERDADEIRO, TipoToken.FALSO)) {
                        avanca();
                        int valor;
                        TipoDado tipo;
                        if (t.tipo == TipoToken.NUM)       { valor = Integer.parseInt(t.lexema); tipo = TipoDado.INTEIRO; }
                        else if (t.tipo == TipoToken.CHAR) { valor = t.lexema.charAt(0);         tipo = TipoDado.CARACTERE; }
                        else { valor = t.tipo == TipoToken.VERDADEIRO ? 1 : 0;                  tipo = TipoDado.LOGICO; }
                        String r = novoTemp();
                        emite("li " + r + ", " + valor);
                        return new Res(tipo, r);
                    }
                    throw erroEsperava("uma expressão");
                }

                private Res exprUn() {
                    if (eh(TipoToken.NAO)) {
                        Token op = avanca();
                        Res v = exprUn();
                        exige(v.tipo() == TipoDado.LOGICO, op.linha, "'nao' precisa de um logico");
                        emite("xori " + v.reg() + ", " + v.reg() + ", 1");     // 0 vira 1, 1 vira 0
                        return v;
                    }
                    if (eh(TipoToken.MENOS)) {
                        Token op = avanca();
                        Res v = exprUn();
                        exige(numerico(v.tipo()), op.linha, "'-' unário precisa de um número");
                        emite("sub " + v.reg() + ", $zero, " + v.reg());       // 0 - v
                        return new Res(TipoDado.INTEIRO, v.reg());
                    }
                    return primario();
                }
            """.trimIndent(),
            tx("tpguidec_folhas_unarios_cada_folha_ocupa"),
        ),
        Code(
            tx("tpguidec_private_void_binaria_string_instr"),
            tx("tpguidec_operacoes_binarias_resultado_fica_no"),
        ),
        Predict(
            tx("tpguidec_quantos_registradores_t_ficam_ocupados"),
            listOf("1", "2", "3", "5"),
            2,
            tx("solta_tpguidec_4"),
        ),
        Steps(
            "`soma <- soma + n * 2;` virando MIPS",
            listOf(
                "`primario(soma)` → `lw \$t0, soma_1`",
                "`+` → exprMul → `primario(n)` → `lw \$t1, n_0`",
                "`*` → `primario(2)` → `li \$t2, 2`",
                "binaria → `mul \$t1, \$t1, \$t2` (libera \$t2)",
                "binaria → `add \$t0, \$t0, \$t1` (libera \$t1)",
                tx("solta_tpguidec_5"),
            ),
        ),
        Code(
            $$"""
                private void atribuicao() {
                    Token id = casaToken(TipoToken.ID);
                    Simbolo s = usa(id);
                    Token seta = casaToken(TipoToken.ATRIB);
                    Res v = expr();
                    exige(podeAtribuir(s.tipo, v.tipo()), seta.linha,
                          "não dá para guardar " + v.tipo() + " em '" + s.nome + "' (" + s.tipo + ")");
                    emite("sw " + v.reg() + ", " + s.rotulo);
                    liberaTemp();
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private String condicao() {                  // ( expr ) — devolve o registrador com 0/1
                    casaToken(TipoToken.ABRE_PAR);
                    int linha = tok.linha;
                    Res c = expr();
                    exige(c.tipo() == TipoDado.LOGICO, linha, "a condição precisa ser logico");
                    casaToken(TipoToken.FECHA_PAR);
                    liberaTemp();                            // o valor continua no registrador até o beq
                    return c.reg();
                }

                private void se() {
                    casaToken(TipoToken.SE);
                    String c = condicao();
                    String senao = novoRotulo("senao"), fim = novoRotulo("fimse");
                    emite("beq " + c + ", $zero, " + senao);   // falso? pula o bloco do então
                    bloco();
                    if (eh(TipoToken.SENAO)) {
                        avanca();
                        emite("j " + fim);                     // o então terminou: pula o senão
                        rotulo(senao);
                        bloco();
                        rotulo(fim);
                    } else {
                        rotulo(senao);                         // sem senão, 'senao' É o fim
                    }
                }

                private void enquanto() {
                    casaToken(TipoToken.ENQUANTO);
                    String inicio = novoRotulo("enquanto"), fim = novoRotulo("fimenq");
                    rotulo(inicio);                            // a condição é refeita a cada volta
                    String c = condicao();
                    emite("beq " + c + ", $zero, " + fim);
                    bloco();
                    emite("j " + inicio);
                    rotulo(fim);
                }
            """.trimIndent(),
            tx("tpguidec_atribuicao_se_enquanto_esquema_traducao"),
        ),
        Code(
            $$"""
            # se (cond) { A } senao { B }        # enquanto (cond) { C }
                <cond em $t0>                    enquantoN:
                beq $t0, $zero, senaoK               <cond em $t0>
                <código de A>                        beq $t0, $zero, fimenqM
                j fimseL                             <código de C>
            senaoK:                                  j enquantoN
                <código de B>                    fimenqM:
            fimseL:
            """.trimIndent(),
            tx("tpguidec_dois_moldes_templates_que_voce"),
        ),
        Code(
            $$"""
                private void escreva() {
                    casaToken(TipoToken.ESCREVA);
                    casaToken(TipoToken.ABRE_PAR);
                    argumento();
                    while (eh(TipoToken.VIRGULA)) { avanca(); argumento(); }
                    casaToken(TipoToken.FECHA_PAR);
                    casaToken(TipoToken.PONTO_VIRGULA);
                    emite("li $a0, 10");                       // '\n' no fim de cada escreva
                    emite("li $v0, 11");
                    emite("syscall");
                }

                private void argumento() {
                    if (eh(TipoToken.STRING)) {
                        Token s = avanca();
                        String r = "str" + (proxString++);
                        strings.append(r).append(": .asciiz \"").append(s.lexema).append("\"\n");
                        emite("la $a0, " + r);
                        emite("li $v0, 4");
                    } else {
                        Res e = expr();
                        emite("move $a0, " + e.reg());
                        emite("li $v0, " + (e.tipo() == TipoDado.CARACTERE ? 11 : 1));
                        liberaTemp();
                    }
                    emite("syscall");
                }

                private void leia() {
                    casaToken(TipoToken.LEIA);
                    casaToken(TipoToken.ABRE_PAR);
                    Token id = casaToken(TipoToken.ID);
                    Simbolo s = usa(id);
                    exige(s.tipo != TipoDado.LOGICO, id.linha, "não dá para ler um logico");
                    emite("li $v0, " + (s.tipo == TipoDado.CARACTERE ? 12 : 5));
                    emite("syscall");                          // o valor lido chega em $v0
                    emite("sw $v0, " + s.rotulo);
                    casaToken(TipoToken.FECHA_PAR);
                    casaToken(TipoToken.PONTO_VIRGULA);
                }
            """.trimIndent(),
            tx("tpguidec_entrada_saida_com_syscall"),
        ),
        Callout(WATCH_OUT, tx("tpguidec_div_com_tres_operandos_rem")),
        Code(TP_PROGRAMA_CONTAGEM, tx("tpguidec_novo_contagem_byt_capitulo_2")),
        Code(TP_ASM_CONTAGEM, tx("tpguidec_saida_asm_completo_que_nosso")),
        Steps(
            tx("tpguidec_rodando_no_mars"),
            listOf(
                tx("tpguidec_baixe_mars_jar_ex_mars4"),
                tx("tpguidec_grafico_java_jar_mars4_5"),
                tx("tpguidec_run_assemble_f3_erro_aqui"),
                tx("tpguidec_run_go_f5_saida_aparece"),
                tx("tpguidec_linha_comando_otimo_para_testes"),
                tx("solta_tpguidec_6"),
            ),
        ),
        Callout(WATCH_OUT, tx("solta_tpguidec_7")),
        Predict(
            tx("tpguidec_se_novorotulo_devolvesse_sempre_fimse"),
            listOf(
                tx("tpguidec_nada_rotulos_podem_repetir"),
                tx("tpguidec_mars_recusa_na_montagem_rotulo"),
                tx("tpguidec_segundo_se_pularia_para_primeiro"),
                tx("tpguidec_java_erro_compilacao"),
            ),
            1,
            tx("tpguidec_rotulo_nome_endereco_dois_iguais"),
        ),
        Callout(NO_DUMB_QUESTIONS, tx("tpguidec_p_por_que_word_4")),
        Callout(REAL_WORLD, tx("tpguidec_esse_estilo_toda_variavel_na")),
        Reveal(tx("solta_tpguidec_8"), tx("solta_tpguidec_9")),
    ),
)
