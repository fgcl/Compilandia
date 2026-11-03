package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill

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
    4, "A gramática LL(1) da Byteiro",
    "Tirar recursão à esquerda, fatorar e provar com FIRST/FOLLOW que um token basta para decidir.",
    Skill.LL1_TABLE,
    listOf(
        Say(PROF, "A gramática informal do capítulo 2 é ótima para humanos. Para virar descida recursiva, ela precisa ser **LL(1)**: olhando UM token, o parser sabe qual regra usar — sem voltar atrás, sem adivinhar."),
        Steps(
            "As três checagens antes de programar",
            listOf(
                "1. **Sem recursão à esquerda**: nenhum `A -> A …`. (Em Java isso é recursão infinita.)",
                "2. **Fatorada**: duas alternativas do mesmo não-terminal não podem começar igual.",
                "3. **FIRST/FOLLOW sem cruzamento**: os FIRST das alternativas são disjuntos; e se uma alternativa pode sumir (ε), os FIRST das outras não cruzam com o FOLLOW.",
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
            "Primeira tentativa: precedência certinha… e recursão à esquerda em quatro lugares",
        ),
        Predict(
            "O que acontece se você traduzir `Add -> Add + Mul` direto para Java (`void add() { add(); casaToken(MAIS); mul(); }`)?",
            listOf(
                "Funciona normalmente",
                "StackOverflowError: add() chama add() sem consumir nenhum token",
                "Erro de compilação do Java",
                "Funciona, mas com associatividade errada",
            ),
            1,
            "A primeira coisa que `add()` faz é chamar `add()` — com o MESMO token na frente. Nada muda, então ela se chama para sempre até a pilha estourar.",
        ),
        Visual(VK.LEFT_RECURSION, grammar = TP_G_EXPR_RECURSIVA, caption = "Antes e depois: A -> A α | β vira A -> β A' e A' -> α A' | ε."),
        Text("Na EBNF fica ainda mais limpo: `A -> A α | β` é o mesmo que `A -> β { α }` — um β seguido de zero ou mais α. E `{ }` vira um **while** em Java. Bônus: o while monta a árvore para a esquerda, então `10 - 3 - 2` continua dando 5."),
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
            "Expressões da Byteiro em EBNF LL(1) — sete níveis, sete métodos Java",
        ),
        Callout(MNEMONIC, "Cascata: quanto mais FUNDO o método, mais FORTE o operador. `ou` mora no topo (agrupa por último); `*` mora lá embaixo (agrupa primeiro)."),
        Text("E o `Rel -> Add oprel Add | Add`? As duas alternativas começam com `Add`: prefixo comum! Fatorando: `Rel -> Add Rel'` e `Rel' -> oprel Add | ε`. Na EBNF isso é o opcional `[ oprel exprAdd ]` — que em Java vira um **if**."),
        Visual(VK.LEFT_FACTORING, grammar = TP_G_PREFIXOS, caption = "Se a Byteiro tivesse chamada de procedimento `p(x);`, atribuição e chamada começariam com `id`. E `se` com e sem `senao` também. Veja a fatoração."),
        Code(TP_G_COMANDOS, "Os comandos da Byteiro em BNF (expressões resumidas em E) — prontos para o teste LL(1)"),
        Visual(VK.FIRST_FOLLOW, grammar = TP_G_COMANDOS, caption = "Calcule FIRST e FOLLOW passo a passo. Repare: `senao` NÃO aparece em FOLLOW(Senao)."),
        Visual(VK.LL1_TABLE, grammar = TP_G_COMANDOS, caption = "Nenhuma célula com duas produções: é LL(1)! O bloco obrigatório no `se` resolveu o senão pendurado."),
        Predict(
            "Em `Senao -> senao Bloco | ε`, quando o parser escolhe a alternativa ε?",
            listOf(
                "Nunca: ε é só enfeite",
                "Quando o token atual está em FOLLOW(Senao) — na prática, 'quando não é senao'",
                "Quando o token atual é senao",
                "Quando o arquivo acaba",
            ),
            1,
            "A tabela põe ε nas colunas de FOLLOW(Senao). No código da descida recursiva isso vira `if (eh(SENAO)) { … }` sem else: se não é `senao`, não consome nada — e se o token for lixo, o próximo `casaToken` reclama.",
        ),
        Callout(WATCH_OUT, "Se a sua linguagem permitir `se (c) comando senao comando` SEM chaves, a célula [Senao, senao] ganha duas produções: é o dangling else, e a gramática não é LL(1). Solução prática no parser: sempre casar o `senao` com o `se` mais próximo (o `if` guloso) — e documentar essa decisão."),
        Reveal("Calcule FIRST(comando) da Byteiro completa.", "{ ID, se, enquanto, escreva, leia, `{`, inteiro, logico, caractere } — declarações começam pelo tipo, e um bloco solto começa por `{`. Esse conjunto vira o método `comecaComando()` no capítulo 5."),
        Reveal("FOLLOW de `{ comando }` dentro do bloco: quando a repetição para?", "Quando o token não está em FIRST(comando). Na gramática certa, ele só pode ser `}` — qualquer outra coisa faz o `casaToken(FECHA_CHAVE)` reclamar com uma mensagem precisa."),
        Callout(NO_DUMB_QUESTIONS, "P: Preciso entregar a tabela LL(1) inteira? R: Depende do enunciado. Para descida recursiva, o que importa é a PROVA de que não há conflito: em cada ponto de decisão do código, os conjuntos de tokens testados são disjuntos. FIRST/FOLLOW no relatório é exatamente essa prova."),
        Callout(EXAM_TIP, "O trio que cai em prova e no TP: recursão à esquerda → **while**; opcional `[ ]` → **if**; repetição `{ }` → **while**. Decore e você traduz qualquer EBNF para Java em minutos."),
        Say(PILHA, "Gramática… LL(1)… pronta. Agora… me deem métodos… para empilhar."),
    ),
)

// ───────────────────────────── Capítulo 5 ─────────────────────────────

internal val tpCap5 = GuideChapter(
    5, "O parser descendente recursivo",
    "Um método por não-terminal, casaToken para terminais, if/while no token atual — e erros com linha.",
    Skill.RECURSIVE_DESCENT,
    listOf(
        Say(PILHA, "Eu sou… a pilha de chamadas do Java. Cada método que você chama… eu empilho. Quando ele retorna… desempilho. A árvore sintática… é o caminho que eu faço."),
        Steps(
            "A receita (decore!)",
            listOf(
                "Um método Java para cada não-terminal: `bloco()`, `comando()`, `expr()`…",
                "Terminal no lado direito → `casaToken(TIPO)`: confere e avança.",
                "Não-terminal no lado direito → chama o método dele.",
                "Várias alternativas → `if`/`switch` olhando o token atual (os FIRST que você calculou).",
                "`{ … }` → `while`; `[ … ]` → `if`.",
            ),
        ),
        Visual(VK.DESCENT_CODE, grammar = TP_G_CASCATA, input = "id + num * id", caption = "O código que o motor gera para a cascata de dois níveis — e a pilha de chamadas para `id + num * id`."),
        Code(
            """
            public class Sintatico {
                private final Lexico lexico;
                private Token tok;                         // lookahead: o token que o parser está "olhando"

                public Sintatico(Lexico lexico) {
                    this.lexico = lexico;
                    this.tok = lexico.proximo();
                }

                private Token avanca() {                   // consome o token atual e o devolve
                    Token t = tok;
                    tok = lexico.proximo();
                    return t;
                }

                private Token casaToken(TipoToken esperado) {
                    if (tok.tipo != esperado) throw erroEsperava("'" + esperado.texto + "'");
                    return avanca();
                }

                private boolean eh(TipoToken... tipos) {   // o token atual é algum destes?
                    for (TipoToken t : tipos) if (tok.tipo == t) return true;
                    return false;
                }

                private ErroCompilacao erroEsperava(String oQue) {
                    String achou = tok.tipo == TipoToken.EOF ? "fim de arquivo" : "'" + tok.lexema + "'";
                    return new ErroCompilacao(tok.linha, "erro sintático: esperava " + oQue + ", encontrou " + achou);
                }

                public String compila() {                  // o capítulo 8 faz isto devolver o MIPS
                    programa();
                    return "";
                }
            """.trimIndent(),
            "Sintatico.java (1/4) — a infraestrutura. Só `avanca()` mexe em `tok`!",
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
            "Sintatico.java (2/4) — programa, bloco e o 'despachante' comando()",
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
            "Sintatico.java (3/4) — os comandos: leia cada método ao lado da sua regra EBNF",
        ),
        Code(
            """
                private void expr() {                     // exprE { ou exprE }
                    exprE();
                    while (eh(TipoToken.OU)) { avanca(); exprE(); }
                }

                private void exprE() {                    // exprRel { e exprRel }
                    exprRel();
                    while (eh(TipoToken.E)) { avanca(); exprRel(); }
                }

                private void exprRel() {                  // exprAdd [ oprel exprAdd ]
                    exprAdd();
                    if (eh(TipoToken.IGUAL, TipoToken.DIFERENTE, TipoToken.MENOR, TipoToken.MAIOR,
                           TipoToken.MENOR_IGUAL, TipoToken.MAIOR_IGUAL)) {
                        avanca();
                        exprAdd();
                    }
                }

                private void exprAdd() {                  // exprMul { (+|-) exprMul }
                    exprMul();
                    while (eh(TipoToken.MAIS, TipoToken.MENOS)) { avanca(); exprMul(); }
                }

                private void exprMul() {                  // exprUn { (*|/|%) exprUn }
                    exprUn();
                    while (eh(TipoToken.VEZES, TipoToken.DIV, TipoToken.MOD)) { avanca(); exprUn(); }
                }

                private void exprUn() {                   // nao exprUn | - exprUn | primario
                    if (eh(TipoToken.NAO, TipoToken.MENOS)) { avanca(); exprUn(); }
                    else primario();
                }

                private void primario() {
                    if (eh(TipoToken.ABRE_PAR)) {
                        avanca();
                        expr();
                        casaToken(TipoToken.FECHA_PAR);
                    } else if (eh(TipoToken.ID, TipoToken.NUM, TipoToken.CHAR,
                                  TipoToken.VERDADEIRO, TipoToken.FALSO)) {
                        avanca();
                    } else {
                        throw erroEsperava("uma expressão");
                    }
                }
            }
            """.trimIndent(),
            "Sintatico.java (4/4) — a cascata de precedência: cada nível chama o de baixo",
        ),
        Reveal("Por que `exprRel` usa `if` e `exprAdd` usa `while`?", "Porque `a + b + c` é permitido (repetição → while), mas `a < b < c` não é (relacionais são NÃO associativos → no máximo um → if). Com o if, o `<` extra sobra e o parser reclama dele."),
        Steps(
            "A pilha de chamadas para `x <- a + 2 * b;`",
            listOf(
                "`atribuicao()` casa ID(x) e `<-`, depois chama `expr()`.",
                "`expr` → `exprE` → `exprRel` → `exprAdd` → `exprMul` → `exprUn` → `primario()` casa `a`. Volta tudo até `exprAdd`.",
                "`exprAdd` vê `+`: avança e chama `exprMul()`.",
                "`exprMul` → `exprUn` → `primario` casa `2`. De volta em `exprMul`, vê `*`: avança, `exprUn` → `primario` casa `b`.",
                "`exprMul` não vê mais `*`: retorna. `exprAdd` não vê mais `+`: retorna. Sobe até `atribuicao()`.",
                "`atribuicao()` casa `;`. Repare: `2 * b` terminou ANTES do `+` — a precedência saiu de graça da ordem das chamadas.",
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
            "Que mensagem o nosso parser imprime para esse programa?",
            listOf(
                "linha 2: erro sintático: esperava ';', encontrou 'x'",
                "linha 3: erro sintático: esperava ';', encontrou 'x'",
                "linha 4: erro sintático: esperava '}', encontrou fim de arquivo",
                "Nenhuma: o programa compila",
            ),
            1,
            "O parser só PERCEBE que falta o ';' quando vê o token seguinte — o `x` da linha 3. A mensagem usa a linha do token encontrado. Se o enunciado pedir 'a linha do erro', isso é o esperado; se quiser a linha anterior, guarde a linha do último token consumido.",
        ),
        Callout(WATCH_OUT, "Esqueceu o `casaToken(TipoToken.EOF)` no fim de `programa()`? Então `programa t { } lixo lixo lixo` compila 'com sucesso'. Esse é um dos primeiros testes que o corretor faz."),
        Callout(WATCH_OUT, "Chamar `lexico.proximo()` direto em vários lugares 'perde' tokens: você avança duas vezes sem perceber. Regra de ouro: SÓ o `avanca()` (e o `casaToken`, que usa ele) mexe em `tok`."),
        Visual(VK.DESCENT_CODE, grammar = TP_G_COMANDOS, input = "se ( id ) { }", caption = "Agora com os comandos da Byteiro: veja o if/else de `Cmd` testando os FIRST de cada alternativa."),
        Callout(NO_DUMB_QUESTIONS, "P: E se o enunciado pedir para continuar depois do primeiro erro? R: Modo pânico: ao errar, registre a mensagem e descarte tokens até um ponto seguro (`;` ou `}`), depois continue. Para a maioria dos TPs, parar no primeiro erro já é o pedido — confira."),
        Visual(VK.PANIC, grammar = TP_G_PANICO, input = "id <- ; escreva ( id ) ;", caption = "Atribuição sem expressão: o parser reclama, pula até um token de sincronização e segue para o escreva."),
        Callout(REAL_WORLD, "Os parsers do GCC (C/C++), do Clang, do javac e do Go são descida recursiva escrita à mão — exatamente a técnica do seu TP. Ela dá controle total sobre as mensagens de erro, e é por isso que os compiladores profissionais voltaram a ela."),
        Callout(EXAM_TIP, "Questão clássica: 'escreva o procedimento da descida recursiva para o não-terminal X'. Siga a receita: um if por alternativa (com os FIRST), casaToken nos terminais, chamada nos não-terminais — e um else com erro."),
    ),
)

// ───────────────────────────── Capítulo 6 ─────────────────────────────

internal val tpCap6 = GuideChapter(
    6, "Tabela de símbolos e escopos",
    "Uma pilha de HashMaps: abre bloco = push, declara = put no topo, usa = busca do topo para baixo.",
    Skill.SCOPE_SYMBOLS,
    listOf(
        Say(BYTE, "Meu parser aceita `x <- 1;` mesmo sem nenhum `inteiro x;`. Ele não lembra de NADA!"),
        Say(PROF, "Ele precisa de memória: a **tabela de símbolos**. E como a Byteiro tem blocos dentro de blocos, essa memória precisa de **escopos**."),
        Code(
            """
            programa escopos {
                inteiro x;              // x  -> rótulo x_0
                x <- 1;
                {
                    caractere x;        // OUTRO x -> x_1 (esconde o de fora)
                    x <- 'A';
                    inteiro y;          // y  -> y_2
                    y <- 2;
                }
                x <- x + 1;             // de volta ao x_0, inteiro
                y <- 3;                 // ERRO: o escopo de y já fechou
            }
            """.trimIndent(),
            "escopos.byt — leia de cima para baixo e acompanhe cada x",
        ),
        Visual(VK.SCOPE, caption = "Toque em cada uso e veja a qual declaração ele se liga."),
        Predict(
            "O que o compilador diz na linha 11 (`y <- 3;`)?",
            listOf(
                "Nada: y foi declarado no programa",
                "linha 11: erro semântico: 'y' não foi declarado",
                "linha 7: erro semântico: y declarado depois de x",
                "Erro sintático",
            ),
            1,
            "Quando o bloco interno fechou, o escopo dele (com o y) foi DESEMPILHADO. A busca na linha 11 só encontra o escopo de fora, onde não há y.",
        ),
        Steps(
            "A pilha de HashMaps em quatro operações",
            listOf(
                "`{` → **abreEscopo**: empilha um HashMap vazio.",
                "`inteiro x;` → **declara**: põe x no HashMap do TOPO (se já estiver lá: erro de redeclaração).",
                "uso de `x` → **busca**: procura do topo para baixo; o primeiro que achar ganha (é isso que faz o x de dentro esconder o de fora).",
                "`}` → **fechaEscopo**: desempilha — tudo que foi declarado no bloco morre junto.",
            ),
        ),
        Code(
            """
            public enum TipoDado { INTEIRO, LOGICO, CARACTERE }

            public class Simbolo {
                public final String nome;      // como está no fonte: "x"
                public final TipoDado tipo;
                public final String rotulo;    // endereço no MIPS: "x_1" (único no programa todo)
                public final int linhaDecl;    // para mensagens de erro caprichadas

                public Simbolo(String nome, TipoDado tipo, String rotulo, int linhaDecl) {
                    this.nome = nome; this.tipo = tipo; this.rotulo = rotulo; this.linhaDecl = linhaDecl;
                }
            }
            """.trimIndent(),
            "TipoDado.java e Simbolo.java",
        ),
        Code(
            """
            import java.util.ArrayDeque;
            import java.util.Deque;
            import java.util.HashMap;
            import java.util.Map;

            public class TabelaSimbolos {
                private final Deque<Map<String, Simbolo>> escopos = new ArrayDeque<>();
                private int contador = 0;                  // deixa cada rótulo único: x_0, x_1...

                public void abreEscopo()  { escopos.push(new HashMap<>()); }
                public void fechaEscopo() { escopos.pop(); }

                public Simbolo declara(String nome, TipoDado tipo, int linha) {
                    Map<String, Simbolo> topo = escopos.peek();
                    Simbolo antigo = topo.get(nome);
                    if (antigo != null)
                        throw new ErroCompilacao(linha, "erro semântico: '" + nome
                            + "' já foi declarado neste escopo (linha " + antigo.linhaDecl + ")");
                    Simbolo s = new Simbolo(nome, tipo, nome + "_" + (contador++), linha);
                    topo.put(nome, s);
                    return s;
                }

                public Simbolo busca(String nome) {        // do escopo mais interno para o mais externo
                    for (Map<String, Simbolo> escopo : escopos) {   // ArrayDeque itera a partir do topo
                        Simbolo s = escopo.get(nome);
                        if (s != null) return s;
                    }
                    return null;
                }
            }
            """.trimIndent(),
            "TabelaSimbolos.java — a classe inteira cabe numa tela",
        ),
        Say(PILHA, "Busca? Eu começo… pelo topo. Não achou? Desço um andar. Não achou? Desço mais. Chegou no chão… e nada? Não declarado."),
        Callout(WATCH_OUT, "Armadilha do Java: se você usar `java.util.Stack` em vez de `ArrayDeque`, o for-each percorre a pilha de BAIXO para CIMA! A busca acha o x de FORA primeiro e o sombreamento quebra sem nenhuma mensagem de erro. Com `ArrayDeque` + `push`, a iteração começa no topo."),
        Callout(NO_DUMB_QUESTIONS, "P: Por que o rótulo `x_1` e não só `x`? R: Dois motivos. (1) Os dois x do exemplo precisam de DOIS endereços diferentes no `.data` — se ambos fossem `x`, o MARS reclamaria de rótulo duplicado. (2) Variáveis chamadas `b`, `j` ou `add` coincidem com instruções MIPS e podem confundir o montador. O sufixo resolve os dois."),
        Code(
            """
                private final TabelaSimbolos tabela = new TabelaSimbolos();

                private void bloco() {
                    casaToken(TipoToken.ABRE_CHAVE);
                    tabela.abreEscopo();
                    while (comecaComando()) comando();
                    tabela.fechaEscopo();
                    casaToken(TipoToken.FECHA_CHAVE);
                }

                private void declaracao() {               // tipo ID { , ID } ;
                    TipoDado t = tipo();
                    declaraUm(t);
                    while (eh(TipoToken.VIRGULA)) {
                        avanca();
                        declaraUm(t);                      // o MESMO tipo desce para cada ID
                    }
                    casaToken(TipoToken.PONTO_VIRGULA);
                }

                private void declaraUm(TipoDado t) {
                    Token id = casaToken(TipoToken.ID);
                    tabela.declara(id.lexema, t, id.linha);
                }

                private TipoDado tipo() {
                    if (!eh(TipoToken.INTEIRO, TipoToken.LOGICO, TipoToken.CARACTERE))
                        throw erroEsperava("um tipo");
                    return switch (avanca().tipo) {
                        case INTEIRO -> TipoDado.INTEIRO;
                        case LOGICO  -> TipoDado.LOGICO;
                        default      -> TipoDado.CARACTERE;
                    };
                }

                private Simbolo usa(Token id) {           // toda vez que um ID aparece numa expressão/atribuição
                    Simbolo s = tabela.busca(id.lexema);
                    if (s == null)
                        throw new ErroCompilacao(id.linha, "erro semântico: '" + id.lexema + "' não foi declarado");
                    return s;
                }
            """.trimIndent(),
            "No Sintatico: o parser agora abre/fecha escopos e registra/consulta nomes",
        ),
        Text("Onde chamar `usa(...)`? Em todo lugar que um ID aparece sem ser declaração: em `atribuicao()` (o destino), em `leia()` e em `primario()`. No capítulo 7 o `Simbolo` devolvido vai dizer o TIPO; no 8, o RÓTULO de memória."),
        Steps(
            "A tabela durante escopos.byt",
            listOf(
                "Linha 1, `{` do programa: pilha = [ {} ]",
                "Linha 2: pilha = [ {x→x_0 inteiro} ]",
                "Linha 4, `{`: pilha = [ {}, {x→x_0} ] (topo à esquerda)",
                "Linhas 5 e 7: topo = {x→x_1 caractere, y→y_2 inteiro}",
                "Linha 6: busca(x) acha x_1 no topo — o caractere.",
                "Linha 9, `}`: desempilha. Pilha = [ {x→x_0} ]",
                "Linha 10: busca(x) acha x_0. Linha 11: busca(y) → null → erro semântico!",
            ),
        ),
        Callout(NO_DUMB_QUESTIONS, "P: Pode declarar um x no bloco interno se já existe um x fora? R: Depende da linguagem! C permite (sombreamento). Java PROÍBE redeclarar uma variável local num bloco interno. A Byteiro permite. Qualquer escolha vale — desde que esteja escrita no relatório e o compilador faça o que está escrito."),
        Callout(EXAM_TIP, "Questão clássica: dado um programa com blocos aninhados, diga a qual declaração cada uso se refere (e quais usos são erro). O jogo Esconde-Esconde do Escopo treina exatamente isso."),
        Callout(REAL_WORLD, "Compiladores reais guardam bem mais no Simbolo: tamanho, deslocamento no registro de ativação (offset relativo ao frame pointer), se é parâmetro, se foi usado (é daí que vem o aviso 'unused variable')."),
        Reveal("E se a Byteiro ganhasse funções com variáveis locais?", "Cada função abriria um escopo para parâmetros e locais — e as locais morariam na PILHA de execução (offset do frame), não no `.data`, para que cada chamada recursiva tenha a sua cópia. Veja a região de registros de ativação na Forja do Código."),
    ),
)
