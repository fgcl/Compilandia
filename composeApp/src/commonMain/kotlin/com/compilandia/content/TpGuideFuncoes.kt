package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Capítulo 11, opcional: vetores e funções na Byteiro (professor do bloco 6). Para quando o enunciado pede.
 * Vetor: .space, base + 4·i com sll. Função: jal, $ra, o quadro na pilha e os $t ocupados guardados antes do jal.
 * Os dois .asm completos são o que o código do capítulo gera; PedidosProfessores3Test roda cada um no MipsVM.
 */
internal const val TP_VETOR_BYT = """programa vetor {
    inteiro v[5], i, soma;
    i <- 0;
    enquanto (i < 5) { v[i] <- i * i; i <- i + 1; }
    i <- 0;
    soma <- 0;
    enquanto (i < 5) { soma <- soma + v[i]; i <- i + 1; }
    escreva("soma = ", soma);
}"""

internal const val TP_VETOR_ASM = $$"""# gerado pelo compilador Byteiro
.data
v_0: .space 20
i_1: .word 0
soma_2: .word 0
str0: .asciiz "soma = "
.text
main:
    li $t0, 0
    sw $t0, i_1
enquanto0:
    lw $t0, i_1
    li $t1, 5
    slt $t0, $t0, $t1
    beq $t0, $zero, fimenq1
    lw $t0, i_1
    sll $t0, $t0, 2
    la $t1, v_0
    add $t0, $t1, $t0
    lw $t1, i_1
    lw $t2, i_1
    mul $t1, $t1, $t2
    sw $t1, 0($t0)
    lw $t0, i_1
    li $t1, 1
    add $t0, $t0, $t1
    sw $t0, i_1
    j enquanto0
fimenq1:
    li $t0, 0
    sw $t0, i_1
    li $t0, 0
    sw $t0, soma_2
enquanto2:
    lw $t0, i_1
    li $t1, 5
    slt $t0, $t0, $t1
    beq $t0, $zero, fimenq3
    lw $t0, soma_2
    lw $t1, i_1
    sll $t1, $t1, 2
    la $t2, v_0
    add $t1, $t2, $t1
    lw $t1, 0($t1)
    add $t0, $t0, $t1
    sw $t0, soma_2
    lw $t0, i_1
    li $t1, 1
    add $t0, $t0, $t1
    sw $t0, i_1
    j enquanto2
fimenq3:
    la $a0, str0
    li $v0, 4
    syscall
    lw $t0, soma_2
    move $a0, $t0
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 10
    syscall"""

internal const val TP_FAT_BYT = """programa fatorial
funcao fat(inteiro n) {
    se (n <= 1) { retorne 1; }
    retorne n * fat(n - 1);
}
{
    escreva("fat(5) = ", fat(5));
}"""

internal const val TP_FAT_ASM = $$"""# gerado pelo compilador Byteiro
.data
str0: .asciiz "fat(5) = "
.text
main:
    la $a0, str0
    li $v0, 4
    syscall
    li $t0, 5
    move $a0, $t0
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal f_fat
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    move $t0, $v0
    move $a0, $t0
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 10
    syscall
f_fat:
    addi $sp, $sp, -8
    sw $ra, 4($sp)
    sw $a0, 0($sp)
    lw $t0, 0($sp)
    li $t1, 1
    sle $t0, $t0, $t1
    beq $t0, $zero, senao0
    li $t0, 1
    move $v0, $t0
    j fim_fat
senao0:
    lw $t0, 0($sp)
    lw $t1, 0($sp)
    li $t2, 1
    sub $t1, $t1, $t2
    move $a0, $t1
    addi $sp, $sp, -8
    sw $t0, 0($sp)
    sw $t1, 4($sp)
    jal f_fat
    lw $t0, 0($sp)
    lw $t1, 4($sp)
    addi $sp, $sp, 8
    move $t1, $v0
    mul $t0, $t0, $t1
    move $v0, $t0
    j fim_fat
fim_fat:
    lw $ra, 4($sp)
    addi $sp, $sp, 8
    jr $ra"""

private const val VETOR_JAVA = $$"""    // Simbolo ganha: public int tamanho = 0;   (0 = variável comum)
    // Lexico: '[' e ']' viram ABRE_COL e FECHA_COL no simples()

    private void declaraUm(TipoDado t) {
        Token id = casaToken(TipoToken.ID);
        Simbolo s = tabela.declara(id.lexema, t, id.linha);
        if (eh(TipoToken.ABRE_COL)) {                     // inteiro v[5];
            avanca();
            s.tamanho = Integer.parseInt(casaToken(TipoToken.NUM).lexema);
            casaToken(TipoToken.FECHA_COL);
            dados.append(s.rotulo).append(": .space ").append(4 * s.tamanho).append('\n');
        } else {
            dados.append(s.rotulo).append(": .word 0\n");
        }
    }

    private String endereco(Simbolo s) {                  // [ expr ]: devolve o registrador com base + 4·i
        casaToken(TipoToken.ABRE_COL);
        int linha = tok.linha;
        Res i = expr();
        exige(i.tipo() == TipoDado.INTEIRO, linha, "o índice de '" + s.nome + "' precisa ser inteiro");
        casaToken(TipoToken.FECHA_COL);
        String base = novoTemp();
        emite("sll " + i.reg() + ", " + i.reg() + ", 2");   // i * 4: cada inteiro ocupa 4 bytes
        emite("la " + base + ", " + s.rotulo);              // o endereço do começo do vetor
        emite("add " + i.reg() + ", " + base + ", " + i.reg());
        liberaTemp();                                       // a base já foi somada
        return i.reg();
    }

    // primario(), no caso do ID:            v[i] lê a memória no endereço
    //     if (s.tamanho > 0) { String r = endereco(s); emite("lw " + r + ", 0(" + r + ")"); return new Res(s.tipo, r); }
    // atribuicao(), logo depois do usa(id): o endereço primeiro, o valor depois
    //     String end = s.tamanho > 0 ? endereco(s) : null;
    //     ... casaToken(ATRIB); Res v = expr(); ...
    //     emite(end != null ? "sw " + v.reg() + ", 0(" + end + ")" : "sw " + v.reg() + ", " + s.rotulo);
    //     liberaTemp(); if (end != null) liberaTemp();"""

private const val FUNCAO_JAVA = $$"""    private final StringBuilder funcoes = new StringBuilder();   // vai depois do main, no compila()
    private StringBuilder saida = codigo;                         // emite() e rotulo() escrevem em 'saida'
    private final Set<String> nomesDeFuncao = new HashSet<>();
    private String fimDaFuncao = null;                            // null: estamos no main

    private void funcao() {                       // funcao id ( inteiro id ) bloco
        casaToken(TipoToken.FUNCAO);
        Token nome = casaToken(TipoToken.ID);
        nomesDeFuncao.add(nome.lexema);           // antes do corpo: a função pode chamar a si mesma
        casaToken(TipoToken.ABRE_PAR);
        casaToken(TipoToken.INTEIRO);
        Token p = casaToken(TipoToken.ID);
        casaToken(TipoToken.FECHA_PAR);
        saida = funcoes;
        fimDaFuncao = "fim_" + nome.lexema;
        rotulo("f_" + nome.lexema);
        emite("addi $sp, $sp, -8");               // o quadro: o parâmetro e o $ra
        emite("sw $ra, 4($sp)");
        emite("sw $a0, 0($sp)");
        tabela.abreEscopo();
        tabela.declaraComRotulo(p.lexema, TipoDado.INTEIRO, p.linha, "0($sp)");   // o n mora na pilha
        bloco();
        tabela.fechaEscopo();
        rotulo(fimDaFuncao);
        emite("lw $ra, 4($sp)");
        emite("addi $sp, $sp, 8");
        emite("jr $ra");
        saida = codigo;
        fimDaFuncao = null;
    }

    private void retorne() {                      // retorne expr ;
        Token r = casaToken(TipoToken.RETORNE);
        exige(fimDaFuncao != null, r.linha, "'retorne' fora de função");
        Res v = expr();
        emite("move $v0, " + v.reg());            // o resultado volta em $v0
        liberaTemp();
        emite("j " + fimDaFuncao);
        casaToken(TipoToken.PONTO_VIRGULA);
    }

    private Res chamada(Token nome) {             // id ( expr ), chamada pelo primario() quando vem '('
        exige(nomesDeFuncao.contains(nome.lexema), nome.linha, "'" + nome.lexema + "' não é função");
        casaToken(TipoToken.ABRE_PAR);
        Res arg = expr();
        casaToken(TipoToken.FECHA_PAR);
        emite("move $a0, " + arg.reg());
        int ocupados = proxTemp;                  // a função vai usar os $t: guarde os que estão ocupados
        emite("addi $sp, $sp, " + (-4 * ocupados));
        for (int k = 0; k < ocupados; k++) emite("sw $t" + k + ", " + (4 * k) + "($sp)");
        emite("jal f_" + nome.lexema);            // pula e guarda em $ra a linha de baixo
        for (int k = 0; k < ocupados; k++) emite("lw $t" + k + ", " + (4 * k) + "($sp)");
        emite("addi $sp, $sp, " + (4 * ocupados));
        emite("move " + arg.reg() + ", $v0");     // o resultado ocupa o registrador do argumento
        return new Res(TipoDado.INTEIRO, arg.reg());
    }"""

internal val tpCap11 = GuideChapter(
    11, tx("tpfun_titulo"), tx("tpfun_resumo"), Skill.RUNTIME,
    listOf(
        Say(PROF, tx("tpfun_abre")),
        Text(tx("tpfun_vetor_texto")),
        Code(tx("tpfun_vetor_gramatica"), tx("tpfun_gramatica_legenda")),
        Predict(tx("tpfun_vetor_preveja"), listOf("1003", "1012", "1016", "1004"), 1, tx("tpfun_vetor_preveja_exp")),
        Code(VETOR_JAVA, tx("tpfun_vetor_java_legenda")),
        Callout(WATCH_OUT, tx("tpfun_vetor_cuidado")),
        Code(TP_VETOR_BYT, tx("tpfun_vetor_byt_legenda")),
        Code(TP_VETOR_ASM, tx("tpfun_vetor_asm_legenda")),
        Text(tx("tpfun_funcao_texto")),
        Code(tx("tpfun_funcao_gramatica"), tx("tpfun_gramatica_legenda")),
        Steps(tx("tpfun_convencao_titulo"), listOf(tx("tpfun_convencao_1"), tx("tpfun_convencao_2"), tx("tpfun_convencao_3"), tx("tpfun_convencao_4"))),
        Code(FUNCAO_JAVA, tx("tpfun_funcao_java_legenda")),
        Predict(tx("tpfun_ra_preveja"), listOf(tx("tpfun_ra_op_1"), tx("tpfun_ra_op_2"), tx("tpfun_ra_op_3")), 1, tx("tpfun_ra_preveja_exp")),
        Code(TP_FAT_BYT, tx("tpfun_fat_byt_legenda")),
        Code(TP_FAT_ASM, tx("tpfun_fat_asm_legenda")),
        Callout(WATCH_OUT, tx("tpfun_funcao_cuidado")),
        Reveal(tx("tpfun_reveal_q"), tx("tpfun_reveal_r")),
    ),
)
