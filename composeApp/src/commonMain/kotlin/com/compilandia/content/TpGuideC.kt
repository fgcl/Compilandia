package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill

// Guia do Trabalho Prático — capítulos 7 e 8 (verificação de tipos, geração de MIPS).

// ───────────────────────────── Capítulo 7 ─────────────────────────────

internal val tpCap7 = GuideChapter(
    7, "Verificação de tipos",
    "Cada método de expressão devolve o TIPO do que reconheceu; atribuição e condição conferem as regras.",
    Skill.ATTRIBUTES_TYPES,
    listOf(
        Say(BYTE, "Olha isso: `logico ok; ok <- 3 * 'A';` passa no meu parser! Não deveria dar erro?"),
        Say(PROF, "Deveria. A forma está certa; o SENTIDO não. A ideia: cada método de expressão para de ser `void` e passa a **devolver o tipo** do pedaço que reconheceu. O tipo sobe da folha para a raiz — é um **atributo sintetizado**."),
        Visual(VK.ATTR_TREE, input = "a + 1 > b && ok", caption = "O tipo subindo pela árvore (no widget a sintaxe é de C: `&&` é o nosso `e`; a e b são inteiros, ok é lógico)."),
        Code(
            """
            Operador           Operandos                  Resultado
            -----------------  -------------------------  ---------
            +  -  *  /  %      numérico, numérico         inteiro
            -  (unário)        numérico                   inteiro
            <  >  <=  >=       numérico, numérico         logico
            =  <>              numérico, numérico         logico
                               logico, logico             logico
            e  ou              logico, logico             logico
            nao                logico                     logico

            numérico = inteiro ou caractere ('A' vale 65 nas contas)
            """.trimIndent(),
            "Regras de tipo das expressões da Byteiro (vão para o relatório)",
        ),
        Code(
            """
            destino       valor        resultado
            ---------     ---------    -----------------------------------------
            inteiro   <-  inteiro      ok
            inteiro   <-  caractere    ok (conversão implícita: 'A' vira 65)
            caractere <-  caractere    ok
            caractere <-  inteiro      ERRO (300 não cabe num caractere)
            logico    <-  logico       ok
            qualquer outra combinação  ERRO
            """.trimIndent(),
            "Regras da atribuição: conversão implícita só no sentido que não perde informação",
        ),
        Predict(
            "Com `inteiro n; caractere c;`, qual destas linhas dá erro semântico?",
            listOf("n <- c + 1;", "n <- c;", "c <- c + 1;", "c <- 'Z';"),
            2,
            "`c + 1` é numérico + numérico → **inteiro**. Guardar inteiro em caractere é proibido (poderia não caber). Já `n <- c + 1` e `n <- c` estão ok: caractere sobe para inteiro sem perder nada.",
        ),
        Code(
            """
                private static boolean numerico(TipoDado t) {
                    return t == TipoDado.INTEIRO || t == TipoDado.CARACTERE;
                }

                private void exige(boolean ok, int linha, String msg) {
                    if (!ok) throw new ErroCompilacao(linha, "erro semântico: " + msg);
                }

                private static boolean podeAtribuir(TipoDado destino, TipoDado valor) {
                    return destino == valor || (destino == TipoDado.INTEIRO && valor == TipoDado.CARACTERE);
                }

                private void checaComparacao(Token op, TipoDado a, TipoDado b) {
                    boolean igualdade = op.tipo == TipoToken.IGUAL || op.tipo == TipoToken.DIFERENTE;
                    boolean ok = (numerico(a) && numerico(b))
                              || (igualdade && a == TipoDado.LOGICO && b == TipoDado.LOGICO);
                    exige(ok, op.linha, "não dá para comparar " + a + " " + op.lexema + " " + b);
                }
            """.trimIndent(),
            "Ajudantes de tipo no Sintatico",
        ),
        Code(
            """
                private TipoDado exprAdd() {
                    TipoDado t = exprMul();
                    while (eh(TipoToken.MAIS, TipoToken.MENOS)) {
                        Token op = avanca();
                        TipoDado d = exprMul();
                        exige(numerico(t) && numerico(d), op.linha,
                              "'" + op.lexema + "' precisa de números, recebeu " + t + " e " + d);
                        t = TipoDado.INTEIRO;
                    }
                    return t;
                }
                // exprMul é igual, com VEZES, DIV e MOD

                private TipoDado exprRel() {
                    TipoDado t = exprAdd();
                    if (eh(TipoToken.IGUAL, TipoToken.DIFERENTE, TipoToken.MENOR, TipoToken.MAIOR,
                           TipoToken.MENOR_IGUAL, TipoToken.MAIOR_IGUAL)) {
                        Token op = avanca();
                        TipoDado d = exprAdd();
                        checaComparacao(op, t, d);
                        t = TipoDado.LOGICO;                 // comparação SEMPRE produz logico
                    }
                    return t;
                }

                private TipoDado exprE() {
                    TipoDado t = exprRel();
                    while (eh(TipoToken.E)) {
                        Token op = avanca();
                        TipoDado d = exprRel();
                        exige(t == TipoDado.LOGICO && d == TipoDado.LOGICO, op.linha, "'e' precisa de dois logico");
                    }
                    return t;
                }
                // expr é igual, com OU
            """.trimIndent(),
            "A cascata agora devolve TipoDado — o mesmo esqueleto do capítulo 5",
        ),
        Code(
            """
                private TipoDado exprUn() {
                    if (eh(TipoToken.NAO)) {
                        Token op = avanca();
                        exige(exprUn() == TipoDado.LOGICO, op.linha, "'nao' precisa de um logico");
                        return TipoDado.LOGICO;
                    }
                    if (eh(TipoToken.MENOS)) {
                        Token op = avanca();
                        exige(numerico(exprUn()), op.linha, "'-' unário precisa de um número");
                        return TipoDado.INTEIRO;
                    }
                    return primario();
                }

                private TipoDado primario() {
                    Token t = tok;
                    if (eh(TipoToken.NUM))  { avanca(); return TipoDado.INTEIRO; }
                    if (eh(TipoToken.CHAR)) { avanca(); return TipoDado.CARACTERE; }
                    if (eh(TipoToken.VERDADEIRO, TipoToken.FALSO)) { avanca(); return TipoDado.LOGICO; }
                    if (eh(TipoToken.ID))   { avanca(); return usa(t).tipo; }   // o tipo vem da tabela!
                    if (eh(TipoToken.ABRE_PAR)) {
                        avanca();
                        TipoDado d = expr();
                        casaToken(TipoToken.FECHA_PAR);
                        return d;
                    }
                    throw erroEsperava("uma expressão");
                }
            """.trimIndent(),
            "Folhas da árvore: literais têm tipo fixo; identificadores perguntam à tabela de símbolos",
        ),
        Code(
            """
                private void atribuicao() {
                    Token id = casaToken(TipoToken.ID);
                    Simbolo s = usa(id);
                    Token seta = casaToken(TipoToken.ATRIB);
                    TipoDado t = expr();
                    exige(podeAtribuir(s.tipo, t), seta.linha,
                          "não dá para guardar " + t + " em '" + s.nome + "' (" + s.tipo + ")");
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private void condicao() {                     // ( expr ) — usado por se e enquanto
                    casaToken(TipoToken.ABRE_PAR);
                    int linha = tok.linha;
                    exige(expr() == TipoDado.LOGICO, linha, "a condição precisa ser logico");
                    casaToken(TipoToken.FECHA_PAR);
                }
            """.trimIndent(),
            "Comandos: atribuição confere a compatibilidade; se/enquanto exigem condição logico",
        ),
        Callout(WATCH_OUT, "Bug campeão: em `exprRel`, esquecer o `t = TipoDado.LOGICO`. Aí `a < b` 'tem tipo' inteiro, `se (a < b)` dá erro de condição e `x <- a < b` passa para um inteiro. Comparação SEMPRE produz logico, não importa o tipo dos lados."),
        Steps(
            "Os tipos subindo em `ok <- c + 1 > n e nao ok;` (c caractere, n inteiro, ok logico)",
            listOf(
                "`primario` de c → CARACTERE; de 1 → INTEIRO.",
                "`exprAdd`: numérico + numérico → INTEIRO.",
                "`primario` de n → INTEIRO. `exprRel`: INTEIRO > INTEIRO → LOGICO.",
                "`exprUn`: nao LOGICO → LOGICO.",
                "`exprE`: LOGICO e LOGICO → LOGICO.",
                "`atribuicao`: podeAtribuir(LOGICO, LOGICO) → ✓.",
            ),
        ),
        Predict(
            "`inteiro n; … se (n) { … }` na Byteiro:",
            listOf(
                "Compila: n diferente de 0 é verdadeiro, como em C",
                "Erro semântico: a condição precisa ser logico",
                "Erro sintático: esperava comparação",
                "Compila com aviso",
            ),
            1,
            "A Byteiro é 'estilo Java': condição tem que ser logico. Sintaticamente `(n)` é uma expressão perfeita; quem barra é a checagem de tipo do `condicao()`. Escreva `se (n <> 0)`.",
        ),
        Callout(REAL_WORLD, "Cada linguagem escolhe. C converte inteiro para booleano sem pedir licença; JavaScript faz `'5' * 2` dar 10; Java exige boolean no if. Nenhuma está 'certa' — o que importa é a regra estar escrita e o compilador obedecê-la."),
        Callout(NO_DUMB_QUESTIONS, "P: A conversão caractere → inteiro gera alguma instrução? R: Não! O caractere já mora na memória como número (65). A 'conversão' existe só na tabela de tipos. Já em C, int → float gera uma instrução de verdade no MIPS (`cvt.s.w`), porque a representação muda."),
        Code(
            """
            programa t { logico ok; ok <- 3 * 'A'; }
              -> linha 1: erro semântico: não dá para guardar INTEIRO em 'ok' (LOGICO)
            programa t { inteiro n; se (n) { } }
              -> linha 1: erro semântico: a condição precisa ser logico
            programa t { inteiro n; n <- verdadeiro + 1; }
              -> linha 1: erro semântico: '+' precisa de números, recebeu LOGICO e INTEIRO
            programa t { x <- 1; }
              -> linha 1: erro semântico: 'x' não foi declarado
            programa t { inteiro a; caractere a; }
              -> linha 1: erro semântico: 'a' já foi declarado neste escopo (linha 1)
            """.trimIndent(),
            "Cinco testes semânticos com a saída esperada — copie para testes/erro/",
        ),
        Callout(EXAM_TIP, "Atributo **sintetizado** sobe (o tipo da expressão, calculado a partir dos filhos). Atributo **herdado** desce: em `inteiro a, b, c;` o tipo é passado como parâmetro para `declaraUm(t)` de cada ID. Seu TP tem os dois — cite isso no relatório!"),
        Reveal("A verificação de tipos precisa ser uma fase separada, com uma árvore?", "Não necessariamente. No esquema de uma passada ela vive DENTRO do parser (tradução dirigida pela sintaxe). Construir uma AST e depois visitá-la também é válido — e facilita otimizações maiores — mas dá mais código. Para o TP, uma passada é o caminho mais curto."),
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
    8, "Gerando MIPS para o MARS",
    "Expressões em registradores temporários, variáveis no .data, rótulos para se/enquanto, syscalls para escrever.",
    Skill.CODEGEN_MIPS,
    listOf(
        Say(PROF, "Hora da Forja. Cada método, além de conferir tipos, agora **emite** o seu pedaço de assembly. Primeiro, o kit mínimo de MIPS que o gerador usa."),
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
            "Cola de MIPS do TP — só isto já compila a Byteiro inteira",
        ),
        Visual(VK.MIPS, input = "a * b + c * d", caption = "O widget usa o estilo 'tudo na memória' (cada temporário vira um .word). Nosso gerador vai guardar os temporários em registradores \$t0–\$t9."),
        Text("A estratégia de expressões: os registradores `\$t0…\$t9` funcionam como uma **pilha**. Cada método de expressão pega o próximo registrador livre, deixa o resultado nele e devolve QUAL é. Uma operação binária faz `esq = esq op dir` e devolve o `dir` ao estoque — ele sempre é o mais novo."),
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
            "A infraestrutura do gerador (dentro do Sintatico, Java 17)",
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
            "As folhas e os unários: cada folha ocupa UM registrador novo",
        ),
        Code(
            $$"""
                private void binaria(String instr, Res esq, Res dir) {        // esq = esq op dir
                    emite(instr + " " + esq.reg() + ", " + esq.reg() + ", " + dir.reg());
                    liberaTemp();                                             // dir era o mais novo
                }

                private Res exprMul() {
                    Res esq = exprUn();
                    while (eh(TipoToken.VEZES, TipoToken.DIV, TipoToken.MOD)) {
                        Token op = avanca();
                        Res dir = exprUn();
                        exige(numerico(esq.tipo()) && numerico(dir.tipo()), op.linha,
                              "'" + op.lexema + "' precisa de números, recebeu " + esq.tipo() + " e " + dir.tipo());
                        binaria(op.tipo == TipoToken.VEZES ? "mul" : op.tipo == TipoToken.DIV ? "div" : "rem", esq, dir);
                        esq = new Res(TipoDado.INTEIRO, esq.reg());
                    }
                    return esq;
                }
                // exprAdd: igual, com MAIS/MENOS -> "add"/"sub"

                private Res exprRel() {
                    Res esq = exprAdd();
                    if (eh(TipoToken.IGUAL, TipoToken.DIFERENTE, TipoToken.MENOR, TipoToken.MAIOR,
                           TipoToken.MENOR_IGUAL, TipoToken.MAIOR_IGUAL)) {
                        Token op = avanca();
                        Res dir = exprAdd();
                        checaComparacao(op, esq.tipo(), dir.tipo());
                        String instr = switch (op.tipo) {
                            case IGUAL       -> "seq";
                            case DIFERENTE   -> "sne";
                            case MENOR       -> "slt";
                            case MAIOR       -> "sgt";
                            case MENOR_IGUAL -> "sle";
                            default          -> "sge";
                        };
                        binaria(instr, esq, dir);
                        esq = new Res(TipoDado.LOGICO, esq.reg());
                    }
                    return esq;
                }

                private Res exprE() {
                    Res esq = exprRel();
                    while (eh(TipoToken.E)) {
                        Token op = avanca();
                        Res dir = exprRel();
                        exige(esq.tipo() == TipoDado.LOGICO && dir.tipo() == TipoDado.LOGICO, op.linha,
                              "'e' precisa de dois logico");
                        binaria("and", esq, dir);                             // 0/1 and 0/1
                    }
                    return esq;
                }
                // expr: igual, com OU -> "or"
            """.trimIndent(),
            "Operações binárias: o resultado fica no registrador da ESQUERDA",
        ),
        Predict(
            "Quantos registradores \$t ficam ocupados AO MESMO TEMPO, no pico, ao gerar `a + b * c`?",
            listOf("1", "2", "3", "5"),
            2,
            "a → \$t0; o `+` chama exprMul: b → \$t1, c → \$t2 (pico: 3!). `mul \$t1, \$t1, \$t2` libera o \$t2; `add \$t0, \$t0, \$t1` libera o \$t1. Sobra só o \$t0 com o resultado.",
        ),
        Steps(
            "`soma <- soma + n * 2;` virando MIPS",
            listOf(
                "`primario(soma)` → `lw \$t0, soma_1`",
                "`+` → exprMul → `primario(n)` → `lw \$t1, n_0`",
                "`*` → `primario(2)` → `li \$t2, 2`",
                "binaria → `mul \$t1, \$t1, \$t2` (libera \$t2)",
                "binaria → `add \$t0, \$t0, \$t1` (libera \$t1)",
                "atribuicao → `sw \$t0, soma_1` (libera \$t0 — de volta a zero registradores ocupados)",
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
            "Atribuição, se e enquanto: o esquema de tradução com rótulos",
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
            "Os dois moldes (templates) que você vai desenhar no relatório",
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
            "Entrada e saída com syscall",
        ),
        Callout(WATCH_OUT, "`div` com três operandos, `rem`, `seq`, `sne`, `sgt`, `sle` e `sge` são **pseudo-instruções**: o MARS as expande em instruções reais. Funcionam perfeitamente no MARS — mas se o enunciado proibir pseudo-instruções, troque por `div`+`mflo`/`mfhi` e por combinações de `slt` com `xori`."),
        Code(TP_PROGRAMA_CONTAGEM, "De novo o contagem.byt do capítulo 2…"),
        Code(TP_ASM_CONTAGEM, "…e o saida.asm COMPLETO que o nosso compilador gera para ele"),
        Steps(
            "Rodando no MARS",
            listOf(
                "Baixe o MARS (um .jar, ex.: `Mars4_5.jar`) — precisa de Java instalado.",
                "Gráfico: `java -jar Mars4_5.jar` → File › Open → `saida.asm`.",
                "Run › Assemble (F3). Erro aqui = o SEU gerador emitiu algo que não é MIPS válido.",
                "Run › Go (F5). A saída aparece na aba **Run I/O**: `soma = 30`.",
                "Linha de comando (ótimo para testes automáticos): `java -jar Mars4_5.jar nc saida.asm` (nc = sem o aviso de copyright).",
                "Depurando: use Step (F7) e olhe a aba Registers — dá para ver \$t0 mudar a cada instrução.",
            ),
        ),
        Callout(WATCH_OUT, "Esqueceu o `li \$v0, 10` + `syscall` no final? O processador continua executando o que estiver depois do seu código, e o MARS termina com um aviso de 'dropped off bottom'. Encerre sempre explicitamente."),
        Predict(
            "Se `novoRotulo` devolvesse sempre `\"fimse\"`, sem o contador, o que aconteceria num programa com dois `se`?",
            listOf(
                "Nada: rótulos podem repetir",
                "O MARS recusa na montagem: rótulo definido duas vezes",
                "O segundo se pularia para o primeiro fimse",
                "O Java dá erro de compilação",
            ),
            1,
            "Rótulo é um NOME de endereço: dois iguais é ambiguidade, e o montador recusa. Por isso o contador global — e por isso todo teste do TP deve ter `se` dentro de `se` e `enquanto` dentro de `enquanto`.",
        ),
        Callout(NO_DUMB_QUESTIONS, "P: Por que `.word` (4 bytes) até para logico e caractere? R: Simplicidade: tudo usa `lw`/`sw`, sem pensar em alinhamento. Dá para usar `.byte` com `lb`/`sb` para caractere — economiza 3 bytes por variável e acrescenta um monte de casos especiais. Num TP, uniformidade ganha."),
        Callout(REAL_WORLD, "Esse estilo — toda variável na memória, carrega, opera, guarda — é parecido com o que o GCC faz com `-O0`. Os compiladores de verdade só ficam espertos (variáveis morando em registradores) quando você liga a otimização."),
        Reveal("Uma expressão aninhada à direita com 11 operandos (`a + (b + (c + …))`) esgota \$t0–\$t9. Como resolver?", "Opções: (1) **derramar** (spill) para a pilha: `addi \$sp, \$sp, -4` + `sw \$t9, 0(\$sp)` e desempilhar depois; (2) usar também \$s0–\$s7; (3) avaliar primeiro o lado que precisa de mais registradores (numeração de Sethi-Ullman). Para o TP, documentar o limite já costuma bastar."),
    ),
)
