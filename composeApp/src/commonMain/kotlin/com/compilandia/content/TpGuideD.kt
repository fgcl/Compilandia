package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill

// Guia do Trabalho Prático — capítulos 9 e 10 (otimizações e testes, checklist de entrega).

// ───────────────────────────── Capítulo 9 ─────────────────────────────

internal val tpCap9 = GuideChapter(
    9, "Otimizações simples e bateria de testes",
    "Dobrar constantes, cortar código morto, um peephole de leve — e testes que provam que nada quebrou.",
    Skill.OPTIMIZATION,
    listOf(
        Say(BYTE, "`x <- 2 * 60 + 1;` vira li, li, mul, li, add, sw. O processador vai calcular 121 TODA vez que passar por ali?"),
        Say(PROF, "Vai — a não ser que o compilador calcule antes. Isso é **dobramento de constantes** (constant folding): se os dois lados de uma operação são conhecidos na compilação, faça a conta no Java e emita só o resultado."),
        Visual(VK.TAC, input = "a + 2 * 3", caption = "Repare na instrução de `2 * 3`: os dois operandos são constantes. O compilador pode fazer essa conta sozinho."),
        Text("O truque para encaixar isso no gerador de uma passada: o `Res` ganha um terceiro campo, `constante`. Uma folha numérica **reserva** o registrador, mas **adia** o `li`. Se a próxima operação também recebe uma constante, a conta é feita em Java e nenhum código sai. Só quando alguém realmente precisa do valor num registrador é que o `li` é emitido."),
        Code(
            $$"""
                private record Res(TipoDado tipo, String reg, Integer constante) {
                    Res(TipoDado tipo, String reg) { this(tipo, reg, null); }   // valor só em execução
                }

                private Res constante(TipoDado tipo, int valor) {     // reserva o registrador, NÃO emite li
                    return new Res(tipo, novoTemp(), valor);
                }

                private String noRegistrador(Res r) {                 // materializa quando precisar
                    if (r.constante() != null) emite("li " + r.reg() + ", " + r.constante());
                    return r.reg();
                }

                private Res binaria(Token op, String instr, Res esq, Res dir, TipoDado tipoRes) {
                    if (esq.constante() != null && dir.constante() != null) {
                        Integer v = calcula(op.tipo, esq.constante(), dir.constante());
                        if (v != null) {                               // DOBROU: zero instruções
                            liberaTemp();
                            return new Res(tipoRes, esq.reg(), v);
                        }
                    }
                    String a = noRegistrador(esq), b = noRegistrador(dir);
                    emite(instr + " " + a + ", " + a + ", " + b);
                    liberaTemp();
                    return new Res(tipoRes, a);
                }

                private static Integer calcula(TipoToken op, int a, int b) {
                    return switch (op) {
                        case MAIS        -> a + b;
                        case MENOS       -> a - b;
                        case VEZES       -> a * b;
                        case DIV         -> b == 0 ? null : a / b;     // por zero: deixa para a execução
                        case MOD         -> b == 0 ? null : a % b;
                        case IGUAL       -> a == b ? 1 : 0;
                        case DIFERENTE   -> a != b ? 1 : 0;
                        case MENOR       -> a < b ? 1 : 0;
                        case MAIOR       -> a > b ? 1 : 0;
                        case MENOR_IGUAL -> a <= b ? 1 : 0;
                        case MAIOR_IGUAL -> a >= b ? 1 : 0;
                        case E           -> a & b;
                        case OU          -> a | b;
                        default          -> null;
                    };
                }
            """.trimIndent(),
            "Dobramento de constantes: o Res agora pode carregar um valor conhecido",
        ),
        Text("No resto do gerador são três trocas mecânicas: (1) em `primario`, literais viram `return constante(tipo, valor);` em vez de `novoTemp()` + `li`; (2) cada `binaria(instr, esq, dir); esq = new Res(...)` vira `esq = binaria(op, instr, esq, dir, tipo);`; (3) todo lugar que usava `x.reg()` para emitir (`sw`, `move`, `beq`, `xori`, `sub`) passa a usar `noRegistrador(x)`. Nos unários: se o operando é constante, devolva `-c` ou `1 - c` sem emitir nada."),
        Code(
            $$"""
            # x <- 2 * 60 + 1;   SEM dobramento        # COM dobramento
                li  $t0, 2                                 li  $t0, 121
                li  $t1, 60                                sw  $t0, x_0
                mul $t0, $t0, $t1
                li  $t1, 1
                add $t0, $t0, $t1
                sw  $t0, x_0
            """.trimIndent(),
            "Seis instruções viram duas",
        ),
        Predict(
            "Com esse dobramento, `y <- a + 2 + 3;` vira `a + 5`?",
            listOf(
                "Sim, 2 + 3 é dobrado",
                "Não: a árvore é (a + 2) + 3 e nenhuma operação tem DOIS lados constantes",
                "Não, porque `a` é variável e estraga a expressão inteira",
                "Sim, mas só se `a` for zero",
            ),
            1,
            "O `+` associa à esquerda: primeiro `a + 2` (um lado variável), depois `(…) + 3`. Já `2 + 3 + a` vira `5 + a`. Compiladores reais reordenam (reassociação) para pegar o primeiro caso — um bom 'trabalho futuro' para o relatório.",
        ),
        Callout(WATCH_OUT, "Nunca dobre `x / 0` com uma conta em Java: ela lança ArithmeticException e o SEU COMPILADOR cai com um stack trace. Devolva `null` (não dobra) e deixe o erro para a execução — ou emita um aviso com a linha. E repare: Java e MIPS truncam a divisão de inteiros do mesmo jeito (`-7 / 2 = -3`), então dobrar não muda o resultado."),
        Say(LEXI, "E aquele `se (1 > 2) { … }`? O bloco nunca roda. Corta fora!"),
        Code(
            $$"""
                private boolean emitindo = true;
                // emite(...) e rotulo(...) ganham um 'if (emitindo)' na frente

                private void blocoMorto() {                    // analisa e verifica, mas não gera código
                    boolean antes = emitindo;
                    emitindo = false;
                    bloco();
                    emitindo = antes;
                }

                private Res condicao() {                       // agora devolve o Res (pode ser constante)
                    casaToken(TipoToken.ABRE_PAR);
                    int linha = tok.linha;
                    Res c = expr();
                    exige(c.tipo() == TipoDado.LOGICO, linha, "a condição precisa ser logico");
                    casaToken(TipoToken.FECHA_PAR);
                    liberaTemp();
                    return c;
                }

                private void se() {
                    casaToken(TipoToken.SE);
                    Res c = condicao();
                    if (c.constante() != null) {               // resultado conhecido JÁ na compilação
                        boolean entra = c.constante() != 0;
                        if (entra) bloco(); else blocoMorto();
                        if (eh(TipoToken.SENAO)) {
                            avanca();
                            if (entra) blocoMorto(); else bloco();
                        }
                        return;                                // nenhum beq, nenhum rótulo
                    }
                    String senao = novoRotulo("senao"), fim = novoRotulo("fimse");
                    emite("beq " + noRegistrador(c) + ", $zero, " + senao);
                    // ... o resto igual ao capítulo 8
                }
            """.trimIndent(),
            "Eliminação de código morto: o bloco continua sendo ANALISADO (erros ainda aparecem), só não gera MIPS",
        ),
        Callout(NO_DUMB_QUESTIONS, "P: Por que não simplesmente pular os tokens do bloco morto? R: Porque o bloco ainda precisa estar CERTO: um erro de sintaxe ou de tipo dentro de `se (falso) { … }` continua sendo erro. Por isso ele passa pelo parser normalmente, só com a 'caneta' desligada. (A string de um `escreva` morto ainda vai para o .data — inofensivo.)"),
        Code(
            $$"""
            # antes                          # depois (peephole)
                lw   $t0, soma_1                 lw   $a0, soma_1
                move $a0, $t0
                li   $v0, 1                      li   $v0, 1
                syscall                          syscall

                sw   $t0, n_0                    sw   $t0, n_0
                lw   $t0, n_0                    # lw removido: $t0 já tem esse valor
            """.trimIndent(),
            "Otimização peephole: uma janela de 2 instruções deslizando sobre o .asm pronto",
        ),
        Callout(WATCH_OUT, "Peephole nunca atravessa um RÓTULO. Em `sw \$t0, n_0` / `enquanto0:` / `lw \$t0, n_0`, alguém pode pular para `enquanto0` vindo de outro lugar, com outro valor em \$t0. Remover aquele `lw` quebra o laço."),
        Say(PILHA, "Otimizou? Então… prove… que nada… quebrou."),
        Steps(
            "Bateria de testes que pega bug de verdade",
            listOf(
                "**Precedência e associatividade**: `escreva(2 + 3 * 4);` → 14; `escreva((2 + 3) * 4);` → 20; `escreva(10 - 3 - 2);` → 5 (e não 9!).",
                "**Negativos e resto**: `escreva(-7 / 2, \" \", -7 % 2);` → -3 -1.",
                "**Laços**: `enquanto` que roda 0 vezes, 1 vez e N vezes.",
                "**Aninhamento**: `se` dentro de `se` dentro de `enquanto` (rótulos únicos!).",
                "**Escopos**: sombreamento (x interno e externo com valores diferentes, imprima os dois).",
                "**Erros**: um arquivo por mensagem de erro — léxico, sintático, cada regra semântica.",
                "**Bordas**: arquivo vazio, só comentário, comentário sem fechar, string com espaço, `'A' + 1` impresso como número.",
            ),
        ),
        Visual(VK.MIPS, input = "(a + b) * (c - d)", caption = "Treine a conferência à mão: com a=4, b=2, c=3, d=5, o resultado tem que ser -12. Calcule ANTES de rodar."),
        Code(
            $$"""
            #!/bin/bash
            # roda_testes.sh — compila cada teste e compara com a saída esperada
            javac -d bin src/*.java || exit 1
            passou=0; falhou=0
            for f in testes/ok/*.byt; do
              java -cp bin Main "$f" /tmp/t.asm > /dev/null
              java -jar Mars4_5.jar nc /tmp/t.asm > /tmp/t.out
              if diff -q /tmp/t.out "${f%.byt}.esperado" > /dev/null; then passou=$((passou+1))
              else echo "FALHOU: $f"; falhou=$((falhou+1)); fi
            done
            for f in testes/erro/*.byt; do
              java -cp bin Main "$f" /tmp/t.asm > /tmp/t.out
              if diff -q /tmp/t.out "${f%.byt}.esperado" > /dev/null; then passou=$((passou+1))
              else echo "FALHOU: $f"; falhou=$((falhou+1)); fi
            done
            echo "$passou passaram, $falhou falharam"
            """.trimIndent(),
            "Testes de regressão automáticos (Linux/macOS/Git Bash). Gere cada .esperado UMA vez, confira à mão, e congele.",
        ),
        Callout(REAL_WORLD, "Isso é teste de **regressão**: depois de cada otimização ou correção, rode a bateria inteira. É exatamente assim que GCC e LLVM trabalham — milhares de programas de teste rodando a cada mudança."),
        Callout(EXAM_TIP, "Prova adora: 'aplique dobramento de constantes e eliminação de código morto neste código de três endereços'. Primeiro dobre, depois veja quais desvios ficaram com condição constante, depois corte o que nunca é alcançado."),
        Reveal("Por que gerar o `.esperado` rodando o próprio compilador é perigoso?", "Porque se o compilador está errado, você congela o erro como 'resposta certa'. Confira cada `.esperado` à mão (ou calcule antes de rodar) na primeira vez — depois ele protege contra regressões."),
    ),
)

// ───────────────────────────── Capítulo 10 ─────────────────────────────

internal val tpCap10 = GuideChapter(
    10, "Checklist de entrega e erros que tiram ponto",
    "O que conferir na véspera, o que vai no relatório e as perguntas da arguição.",
    Skill.ERROR_RECOVERY,
    listOf(
        Say(NARRADOR, "Véspera da entrega. 23h47. A geladeira faz um barulho estranho. O cursor pisca."),
        Say(BYTE, "Tá tudo funcionando! …Eu acho."),
        Say(PROF, "'Eu acho' não é teste. Vamos passar o checklist, item por item."),
        Steps(
            "Checklist do código",
            listOf(
                "Compila do zero, fora da IDE: `javac -d bin src/*.java` sem erros nem avisos estranhos.",
                "Roda exatamente como o enunciado manda (ex.: `java Main programa.byt`).",
                "Nenhum caminho absoluto (`C:\\Users\\…`) e nenhum `System.out.println` de depuração esquecido.",
                "Mensagens de erro no formato pedido, com a linha certa, e o programa PARA no primeiro erro (se for o pedido).",
                "Toda a bateria de testes passa — rode o script uma última vez.",
                "O `.asm` gerado monta e roda no MARS sem nenhuma edição manual.",
                "README com: como compilar, como rodar, versão do Java.",
            ),
        ),
        Steps(
            "Checklist do relatório",
            listOf(
                "Especificação da linguagem: tokens (com regex), precedência, gramática.",
                "O AFD do léxico desenhado + a lista de erros léxicos.",
                "Gramática LL(1) em EBNF + FIRST/FOLLOW como prova de que não há conflitos.",
                "Decisões de projeto: maiúsculas, sombreamento, conversões implícitas, `<-` vs `<` `-`.",
                "Esquemas de tradução: o molde MIPS de cada comando (se, enquanto, escreva…).",
                "Otimizações implementadas, com exemplo antes/depois.",
                "Testes: tabela com programa, saída esperada e saída obtida. E as limitações conhecidas (ex.: 10 registradores).",
            ),
        ),
        Callout(WATCH_OUT, "**Linha errada nas mensagens.** O erro mais comum de todos. Causas: não contar '\\n' dentro de comentário de bloco, contar '\\r' também, ou usar a linha do token ANTERIOR quando o enunciado quer a do token encontrado (ou vice-versa). Tenha um teste de erro DEPOIS de um comentário de várias linhas."),
        Callout(WATCH_OUT, "**Registradores vazando.** Esquecer um `liberaTemp()` não quebra programas pequenos… até o 11º comando, quando aparece 'faltaram registradores' do nada. Teste com um programa de 30 atribuições seguidas."),
        Code(
            """
                private void comando() {
                    int antes = proxTemp;
                    switch (tok.tipo) {
                        // ... os mesmos casos de antes ...
                    }
                    if (proxTemp != antes)               // rede de segurança: pega o bug na hora
                        throw new IllegalStateException("BUG: registrador vazou no comando antes da linha " + tok.linha);
                }
            """.trimIndent(),
            "Uma linha de 'assert' que economiza uma noite de depuração",
        ),
        Predict(
            "Alguém escreveu no léxico: `if (lexema == \"senao\") …`. O que acontece?",
            listOf(
                "Funciona sempre",
                "Às vezes não reconhece a palavra: `==` em String compara REFERÊNCIAS, não conteúdo",
                "Erro de compilação do Java",
                "Funciona só no Windows",
            ),
            1,
            "Em Java, `==` entre Strings pergunta 'é o mesmo objeto?'. O lexema montado num StringBuilder é um objeto NOVO, então a comparação falha. Use `equals` — ou, melhor ainda, o HashMap de reservadas e enums como no capítulo 3.",
        ),
        Callout(WATCH_OUT, "**Rótulos e nomes colidindo.** Dois `se` aninhados gerando o mesmo rótulo, ou uma variável chamada `main`, `j` ou `b` virando rótulo cru. O contador nos rótulos e o sufixo `_N` nas variáveis resolvem — tenha um teste com `inteiro j, b, main;`."),
        Callout(WATCH_OUT, "**Aceitar lixo.** `programa t { } xyz`, `escreva(1);;` (dois `;`), `a < b < c`. Cada um desses deveria ser um teste da pasta `erro/` com a mensagem esperada."),
        Callout(WATCH_OUT, "**Variável em laço não reinicia.** Na Byteiro, `inteiro t;` dentro de um `enquanto` NÃO gera código: o `.word` é o mesmo a cada volta e guarda o valor da volta anterior. Ou documente isso ('variáveis valem 0 só no início do programa'), ou gere um `sw \$zero, rotulo` em cada declaração."),
        Callout(REAL_WORLD, "Integridade acadêmica: copiar o compilador de um colega, de um semestre anterior ou pedir para uma IA escrever sem entender aparece na arguição em dois minutos — e costuma zerar o trabalho. Use este guia como MAPA: os exemplos são da Byteiro, a sua linguagem é outra, e o código precisa ser seu."),
        Callout(EXAM_TIP, "Treine a arguição em voz alta, um integrante perguntando para o outro. As perguntas abaixo são as clássicas."),
        Reveal("Arguição: 'Por que a sua gramática é LL(1)?'", "Porque não tem recursão à esquerda, está fatorada e, em cada não-terminal com alternativas, os FIRST são disjuntos (e não cruzam com o FOLLOW quando há ε). Mostre a tabela FIRST/FOLLOW do relatório e aponte o `if`/`switch` correspondente no código."),
        Reveal("Arguição: 'Mostre onde o tipo de `a + 'b'` é calculado.'", "Em `exprAdd`: `primario` devolve INTEIRO para `a` e CARACTERE para `'b'`; os dois são numéricos, então o resultado é INTEIRO. Nenhuma instrução extra: o caractere já é um número na memória."),
        Reveal("Arguição: 'Como o seu léxico sabe que `<-` é um token só?'", "Estado 3 do AFD: depois de ler '<', ele olha o próximo caractere. Se for '-', consome e devolve ATRIB (maior lexema). Se for outra coisa, devolve MENOR sem consumir."),
        Reveal("Arguição: 'O que acontece com a pilha de escopos quando o bloco do enquanto fecha?'", "`fechaEscopo()` desempilha o HashMap do bloco: os nomes declarados lá deixam de ser visíveis. Mas o `.word` continua no `.data` — escopo é uma regra de COMPILAÇÃO, não de memória."),
        Steps(
            "Plano para as últimas 48 horas",
            listOf(
                "48h: congele funcionalidades. Nada de recurso novo — só correção de bug.",
                "36h: rode a bateria inteira, conserte o que falhar, rode de novo.",
                "24h: relatório — figuras do AFD, gramática, moldes MIPS, tabela de testes.",
                "12h: clone o repositório numa pasta limpa e siga o README como se fosse o professor.",
                "2h: entregue. Sim, antes do prazo. O Canvas também cai.",
            ),
        ),
        Say(LEXI, "Tokens cortados."),
        Say(PILHA, "Gramática… empilhada."),
        Say(PROF, "Tipos conferidos, MIPS rodando, testes passando. Você não 'fez um trabalho de compiladores' — você **construiu um compilador**. Pouca gente pode dizer isso."),
        Reveal("Última recuperação: recite as fases do seu compilador e a classe Java de cada uma.", "Léxico → `Lexico` (AFD com switch). Sintático → `Sintatico` (descida recursiva, casaToken). Semântico → `TabelaSimbolos` + checagens no `Sintatico`. Geração → métodos do `Sintatico` emitindo MIPS. Otimização → dobramento de constantes e código morto no gerador. E o `Main` costurando tudo."),
    ),
)
