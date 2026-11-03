package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill

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
    1, "Visão geral e planejamento",
    "O TP é um compilador inteiro — mas são cinco programinhas que conversam. Planeje antes de codar.",
    Skill.PHASES,
    listOf(
        Say(NARRADOR, "Semana 3 do semestre. O enunciado cai no Canvas: 'Construir um compilador completo para uma linguagem imperativa simples, em Java, gerando assembly MIPS executável no MARS'. Você lê duas vezes. Depois lê de novo."),
        Say(BYTE, "Um compilador INTEIRO? Eu mal faço um for em Java sem olhar a internet…"),
        Say(PROF, "Respira. Ninguém escreve um compilador de uma vez. Você vai escrever **cinco pedaços pequenos** que conversam entre si — e cada um você já treinou aqui no Compilândia."),
        Visual(VK.PIPELINE, caption = "Cada estação da esteira vira uma classe Java no seu TP."),
        Steps(
            "As peças do TP (e o arquivo Java de cada uma)",
            listOf(
                "🔪 **Léxico** (`Lexico.java`): texto → tokens. Um AFD escrito com `switch` de estados.",
                "🌳 **Sintático** (`Sintatico.java`): tokens → 'o programa está bem formado?'. Descida recursiva: um método por não-terminal.",
                "🏙️ **Semântico** (`TabelaSimbolos.java` + checagens dentro do `Sintatico`): declarou? o tipo bate?",
                "⚒️ **Gerador** (também dentro do `Sintatico`): cada método emite o seu pedaço de assembly MIPS.",
                "✨ **Otimização** (opcional/bônus): dobrar constantes, cortar código morto.",
                "🧪 **Testes**: uma pasta de programas que DEVEM compilar e outra de programas que DEVEM dar erro.",
            ),
        ),
        Text("Repare num detalhe: semântico e gerador moram **dentro** do sintático. É o esquema de **tradução dirigida pela sintaxe em uma passada**: o parser pede um token ao léxico quando precisa, confere a semântica na hora e já cospe o MIPS. Sem árvore gigante na memória, sem segunda passada. É o formato que cabe num TP de semestre."),
        Code(
            """
            byteiro/
            ├── src/
            │   ├── Main.java              // lê o .byt, chama o compilador, grava o .asm
            │   ├── ErroCompilacao.java    // exceção que carrega o número da linha
            │   ├── TipoToken.java         // enum com as categorias de token
            │   ├── Token.java
            │   ├── Lexico.java            // o AFD (capítulo 3)
            │   ├── Sintatico.java         // descida recursiva + semântica + MIPS
            │   ├── TipoDado.java
            │   ├── Simbolo.java
            │   └── TabelaSimbolos.java    // pilha de escopos (capítulo 6)
            ├── testes/
            │   ├── ok/                    // programas que DEVEM compilar
            │   └── erro/                  // programas que DEVEM falhar (um erro por arquivo!)
            └── README.md                  // como compilar e rodar
            """.trimIndent(),
            "Estrutura de pastas sugerida",
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
            "ErroCompilacao.java — TODO erro do compilador passa por aqui, sempre com a linha",
        ),
        Code(
            """
            import java.nio.file.Files;
            import java.nio.file.Path;

            public class Main {
                public static void main(String[] args) throws Exception {
                    if (args.length < 1) {
                        System.err.println("uso: java Main programa.byt [saida.asm]");
                        return;
                    }
                    String fonte = Files.readString(Path.of(args[0]));
                    String saida = args.length > 1 ? args[1] : "saida.asm";
                    try {
                        Sintatico s = new Sintatico(new Lexico(fonte));
                        String asm = s.compila();           // análise + geração numa passada só
                        Files.writeString(Path.of(saida), asm);
                        System.out.println("compilado com sucesso -> " + saida);
                    } catch (ErroCompilacao e) {
                        System.out.println(e.getMessage()); // ex.: linha 7: erro sintático: esperava ';'...
                        System.exit(1);
                    }
                }
            }
            """.trimIndent(),
            "Main.java — o maestro (Java 17)",
        ),
        Predict(
            "Qual ordem de implementação dá MENOS dor de cabeça?",
            listOf(
                "Escrever tudo de uma vez e testar na última semana",
                "Léxico → sintático → semântico → geração, testando cada fase antes de seguir",
                "Começar pela geração de MIPS, que é o que vale mais ponto",
                "Começar pela tabela de símbolos, que todo mundo usa",
            ),
            1,
            "Cada fase CONSOME a saída da anterior. Se o léxico tem bug, o parser parece quebrado sem estar. Construa de baixo para cima e teste cada camada antes de empilhar a próxima.",
        ),
        Steps(
            "Cronograma de 8 semanas (ajuste à data do seu enunciado)",
            listOf(
                "Semana 1 — Definir a linguagem e escrever 10 programas-exemplo (5 certos, 5 com erro).",
                "Semana 2 — Léxico + um `TesteLexico` que imprime os tokens.",
                "Semanas 3–4 — Gramática LL(1) no papel (FIRST/FOLLOW!) e o parser descendente.",
                "Semana 5 — Tabela de símbolos e verificação de tipos.",
                "Semanas 6–7 — Geração de MIPS, testando cada comando no MARS.",
                "Semana 8 — Otimização, bateria de testes, relatório. E uma folga: algo SEMPRE atrasa.",
            ),
        ),
        Code(
            """
            git init byteiro && cd byteiro
            git checkout -b lexico
            # ... escreve, testa ...
            git add src/Lexico.java testes/
            git commit -m "lexico: reconhece <- e comentarios de bloco"
            git checkout main
            git merge lexico
            """.trimIndent(),
            "Git desde o dia 1: um branch por fase, commits pequenos",
        ),
        Callout(REAL_WORLD, "Commit pequeno salva vida: quando a geração de código quebrar o parser às 23h, `git diff` mostra exatamente o que mudou. E o histórico de commits prova que o trabalho é da equipe."),
        Say(LEXI, "Dupla? Um começa o léxico enquanto o outro escreve a gramática no papel. Trio? O terceiro escreve a bateria de testes desde o primeiro dia."),
        Say(PROF, "Mas atenção: **todo mundo** precisa entender **tudo**. Na arguição, o professor aponta para qualquer método e pergunta 'por que isso está aqui?' para qualquer integrante."),
        Callout(WATCH_OUT, "Leia o enunciado com marca-texto. Formato da mensagem de erro, nome do arquivo de saída, se para no primeiro erro, se palavras-chave ignoram maiúsculas… Alguns professores corrigem com scripts que comparam a saída caractere por caractere — um espaço a mais vira 'teste falhou'."),
        Callout(NO_DUMB_QUESTIONS, "P: Posso usar ANTLR, JavaCC ou outro gerador? R: Na maioria das turmas, NÃO — o objetivo é escrever léxico e parser à mão. Em alguns campi o léxico é feito com lex/flex (a Oficina Flex do app treina isso). Na dúvida, pergunte ao professor ANTES de começar."),
        Reveal("Em uma frase: o que o seu `Main` faz?", "Lê o arquivo-fonte, entrega ao `Sintatico` (que puxa tokens do `Lexico`, consulta a `TabelaSimbolos` e emite MIPS) e grava o `.asm` — ou imprime a linha e a mensagem do primeiro erro."),
        Say(PILHA, "Devagar… e sempre… uma fase… por vez."),
    ),
)

// ───────────────────────────── Capítulo 2 ─────────────────────────────

internal val tpCap2 = GuideChapter(
    2, "Definindo a linguagem Byteiro",
    "Antes do Java: tokens, precedência e uma gramática informal de uma linguagem pequena e completa.",
    Skill.TOKENS,
    listOf(
        Say(PROF, "Antes de uma linha de Java, você precisa de uma linguagem. No TP a turma inventa (ou recebe) a sua. Neste guia vamos inventar a **Byteiro** — pequenininha de propósito."),
        Say(BYTE, "Posso pôr classes, lambdas, vetores, funções recursivas…?"),
        Say(PROF, "NÃO. Cada recurso a mais é um capítulo a mais de dor. **Pequena e completa ganha de grande e quebrada.** Funções e vetores ficam como 'trabalhos futuros' no relatório."),
        Code(TP_PROGRAMA_CONTAGEM, "contagem.byt — um programa Byteiro completo (vamos compilá-lo até o MIPS no capítulo 8)"),
        Steps(
            "Ficha técnica da Byteiro",
            listOf(
                "**Tipos**: `inteiro` (32 bits), `logico` (`verdadeiro`/`falso`), `caractere` (`'A'`).",
                "**Comandos**: declaração (`inteiro a, b;`), atribuição com `<-`, `se`/`senao`, `enquanto`, `escreva(...)`, `leia(x)`.",
                "**Blocos** `{ }` — cada bloco abre um escopo novo. `se`, `senao` e `enquanto` EXIGEM bloco.",
                "**Expressões**: aritmética `+ - * / %`, relacionais `= <> < > <= >=`, lógicas `e ou nao`.",
                "**Comentários**: `// até o fim da linha` e `/* de bloco */`.",
                "**Strings** só aparecem dentro do `escreva` — não existe tipo string.",
            ),
        ),
        Code(
            """
            Categoria      Padrão (informal)                    Exemplos
            -------------  -----------------------------------  ----------------
            reservadas     programa inteiro logico caractere    se, enquanto
                           se senao enquanto escreva leia
                           e ou nao verdadeiro falso
            ID             letra (letra | dígito | _)*          soma, n1, valor_max
            NUM            dígito+                              0, 42, 32767
            CHAR           ' um-caractere '                     'A', '7'
            STRING         " qualquer coisa sem aspas e sem \n " "soma = "
            ATRIB          <-
            relacionais    =  <>  <  >  <=  >=
            aritméticos    +  -  *  /  %
            pontuação      (  )  {  }  ,  ;
            """.trimIndent(),
            "Tabela de tokens — vai direto para o relatório",
        ),
        Code(
            """
            Precedência (da MAIS FRACA para a MAIS FORTE)
            1. ou                          associa à esquerda
            2. e                           associa à esquerda
            3. =  <>  <  >  <=  >=         NÃO associativo: a < b < c é erro
            4. +  -                        associa à esquerda
            5. *  /  %                     associa à esquerda
            6. nao   - (unário)            prefixo
            7. ( )  literais  variáveis
            """.trimIndent(),
            "Tabela de precedência da Byteiro",
        ),
        Predict(
            "Na Byteiro, como é agrupado `a + b * 2 > 10 e ok`?",
            listOf(
                "((a + b) * 2) > (10 e ok)",
                "((a + (b * 2)) > 10) e ok",
                "a + ((b * 2) > (10 e ok))",
                "Erro: misturar aritmética e lógica é proibido",
            ),
            1,
            "Quem está mais EMBAIXO na tabela gruda mais forte: primeiro `*`, depois `+`, depois `>`, e por último `e`. É exatamente a ordem que o parser vai seguir no capítulo 5.",
        ),
        Callout(WATCH_OUT, "Por que `<-` para atribuir? Assim `=` fica livre para COMPARAR e ninguém escreve `se (x = 1)` achando que atribui. Mas tem pegadinha: `a<-1` agora é `a <- 1` (atribuição), e não `a < -1`. É a regra do **maior lexema**: o léxico sempre pega o token mais comprido possível. Documente isso no relatório!"),
        Visual(VK.TOKENIZE, input = "if (x<-1) y = 0; // C de verdade", caption = "O Lexi do app conhece C: em C, `x<-1` é `x < -1`. Na Byteiro o MESMO texto vira `x <- 1`. Quem define os tokens é a especificação da linguagem."),
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
            "Gramática informal (EBNF): { } = repete 0+ vezes, [ ] = opcional. As expressões ficam para o capítulo 4.",
        ),
        Callout(REAL_WORLD, "Exigir chaves no `se` fecha a porta do 'senão pendurado' (dangling else): nunca há dúvida de a qual `se` um `senao` pertence. Go e Rust fazem a mesma escolha."),
        Reveal("Quem pega cada erro? (1) `x <- 3 # 2;`  (2) `x <- 3 + ;`  (3) `x <- verdadeiro + 1;`", "(1) **Léxico**: `#` não pertence a nenhum token. (2) **Sintático**: depois do `+` deveria vir uma expressão. (3) **Semântico**: a forma está certa, mas somar um `logico` não faz sentido."),
        Predict(
            "E `inteiro 2x;`? Que tipo de erro a Byteiro deve dar?",
            listOf(
                "Léxico: número malformado '2x'",
                "Sintático: esperava identificador, encontrou '2'",
                "Depende de como VOCÊ especificou o léxico — e precisa documentar",
                "Nenhum: 2x é um identificador válido",
            ),
            2,
            "Um léxico pode cortar `2x` em NUM(2) + ID(x) e deixar o sintático reclamar, ou recusar na hora. Os dois são defensáveis! Na Byteiro decidimos que é erro **léxico** ('número malformado'). O que tira ponto é não ter decidido.",
        ),
        Callout(EXAM_TIP, "Escreva a especificação no relatório ANTES do código: tabela de tokens, precedência, gramática, decisões (maiúsculas importam? pode redeclarar em bloco interno?). Ela vira o seu mapa — e muitos professores dão nota para essa parte."),
        Steps(
            "Lição de casa antes do capítulo 3",
            listOf(
                "Escreva 5 programas Byteiro CORRETOS, cada um exercitando um recurso (declaração, se/senao, enquanto, escreva, expressões).",
                "Escreva 5 programas ERRADOS, com UM erro cada: léxico, sintático, não declarado, tipo errado, redeclaração.",
                "Ao lado de cada um, anote a saída esperada. Parabéns: você acabou de começar a bateria de testes.",
            ),
        ),
        Say(LEXI, "Tabela de tokens pronta? Então me passa a katana. Próximo capítulo eu fatio tudo."),
    ),
)

// ───────────────────────────── Capítulo 3 ─────────────────────────────

internal val tpCap3 = GuideChapter(
    3, "Do regex ao AFD: o analisador léxico",
    "Cada token é uma regex, cada regex é um AFD, todos viram UM autômato — escrito em Java com switch.",
    Skill.REGEX_DFA,
    listOf(
        Say(LEXI, "Meu segredo: cada categoria de token é uma **expressão regular**; cada regex vira um **AFD**; e todos os AFDs são colados num autômato grande, com um estado inicial só."),
        Code(
            """
            ID      =  l (l | d | _)*          l = letra, d = dígito
            NUM     =  d d*
            CHAR    =  ' c '                   c = qualquer caractere menos ' e \n
            STRING  =  " s* "                  s = qualquer caractere menos " e \n
            MENOR   =  <        ATRIB = <-     MENOR_IGUAL = <=     DIFERENTE = <>
            MAIOR   =  >        MAIOR_IGUAL = >=
            DIV     =  /        (// e /* */ são comentários: descartados)
            """.trimIndent(),
            "As regex da Byteiro",
        ),
        Visual(VK.DFA, input = "l(l|d)*", caption = "ID (l = letra, d = dígito; o _ ficou de fora para caber no widget). Teste 'l', 'ldl' e 'dl'."),
        Visual(VK.DFA, input = "dd*", caption = "NUM: pelo menos um dígito. Teste 'd', 'ddd' e a cadeia vazia."),
        Visual(VK.DFA, input = "m(h|i|g)?", caption = "A família do '<' (m = '<', h = '-', i = '=', g = '>'): aceita m, mh, mi, mg. Repare que o estado depois de 'm' já é final — é o MENOR sozinho."),
        Say(BYTE, "Tá, são vários AFDs. Mas como eu junto tudo num só?"),
        Say(PROF, "Crie um estado 0 com uma seta para o começo de cada AFD. Como os primeiros caracteres são diferentes — letra, dígito, '<', '/'… — nunca há duas setas com o mesmo rótulo saindo do 0. Continua determinístico!"),
        Code(
            """
                         letra/dígito/_ ⟲
            (0) ─letra──► (1) ── outro* → ID ou reservada
             │
             ├─dígito──► (2) ⟲ dígito    outro* → NUM     letra → ERRO "número malformado"
             │
             ├── '<' ──► (3) ── '-' → ATRIB   '=' → MENOR_IGUAL   '>' → DIFERENTE   outro* → MENOR
             ├── '>' ──► (4) ── '=' → MAIOR_IGUAL   outro* → MAIOR
             │
             ├── '/' ──► (5) ── '/' → (6) comentário de linha: volta ao (0) no '\n'
             │                ── '*' → (7) comentário de bloco: volta ao (0) no "*/"
             │                ── outro* → DIV
             │
             ├── ' ───► (8) ── c → (9) ── ' → CHAR
             ├── " ───► (10) ⟲ s    " → STRING    '\n' ou fim → ERRO
             ├── espaço, \t, \r → fica no (0)      '\n' → fica no (0) e linha++
             └── + - * % = ( ) { } , ;  → token de 1 caractere

            outro* = o caractere que ENCERROU o token NÃO é consumido
            """.trimIndent(),
            "O autômato grande da Byteiro (desenhe a versão bonita no relatório!)",
        ),
        Code(
            """
            estado │ letra  │ dígito │   <    │   -    │   =    │   >    │ outro
            ───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
              0    │   1    │   2    │   3    │ MENOS  │ IGUAL  │   4    │ (ver acima)
              1    │   1    │   1    │  ID*   │  ID*   │  ID*   │  ID*   │  ID*
              2    │  ERRO  │   2    │  NUM*  │  NUM*  │  NUM*  │  NUM*  │  NUM*
              3    │ MENOR* │ MENOR* │ MENOR* │ ATRIB  │ M_IGU  │ DIFER  │ MENOR*
              4    │ MAIOR* │ MAIOR* │ MAIOR* │ MAIOR* │ MA_IGU │ MAIOR* │ MAIOR*
            """.trimIndent(),
            "Pedaço da tabela de transições (* = devolve o token sem consumir o caractere)",
        ),
        Callout(NO_DUMB_QUESTIONS, "P: Cada palavra reservada precisa de um AFD próprio? R: Daria um autômato enorme. O truque clássico: reconheça tudo como ID (estado 1) e, no fim, consulte uma tabela (HashMap) de reservadas. Se estiver lá, é palavra-chave; senão, é ID."),
        Code(
            """
            public enum TipoToken {
                // palavras reservadas (o texto é a própria palavra)
                PROGRAMA("programa"), INTEIRO("inteiro"), LOGICO("logico"), CARACTERE("caractere"),
                SE("se"), SENAO("senao"), ENQUANTO("enquanto"), ESCREVA("escreva"), LEIA("leia"),
                E("e"), OU("ou"), NAO("nao"), VERDADEIRO("verdadeiro"), FALSO("falso"),
                // lexema variável
                ID("identificador"), NUM("número"), CHAR("caractere literal"), STRING("string"),
                // operadores e pontuação
                ATRIB("<-"), MAIS("+"), MENOS("-"), VEZES("*"), DIV("/"), MOD("%"),
                IGUAL("="), DIFERENTE("<>"), MENOR("<"), MAIOR(">"), MENOR_IGUAL("<="), MAIOR_IGUAL(">="),
                ABRE_PAR("("), FECHA_PAR(")"), ABRE_CHAVE("{"), FECHA_CHAVE("}"),
                VIRGULA(","), PONTO_VIRGULA(";"), EOF("fim de arquivo");

                public final String texto;          // usado nas mensagens de erro
                TipoToken(String texto) { this.texto = texto; }
            }

            public class Token {
                public final TipoToken tipo;
                public final String lexema;
                public final int linha;

                public Token(TipoToken tipo, String lexema, int linha) {
                    this.tipo = tipo; this.lexema = lexema; this.linha = linha;
                }

                @Override public String toString() {
                    return String.format("%-3d %-14s %s", linha, tipo, lexema);
                }
            }
            """.trimIndent(),
            "TipoToken.java e Token.java",
        ),
        Code(
            """
            import java.util.HashMap;
            import java.util.Map;

            public class Lexico {
                private static final Map<String, TipoToken> RESERVADAS = new HashMap<>();
                static {   // as 14 primeiras constantes do enum são as reservadas
                    for (TipoToken t : TipoToken.values())
                        if (t.ordinal() <= TipoToken.FALSO.ordinal()) RESERVADAS.put(t.texto, t);
                }

                private final String fonte;
                private int pos = 0;      // índice do próximo caractere a ler
                private int linha = 1;

                public Lexico(String fonte) { this.fonte = fonte; }

                private char atual() { return pos < fonte.length() ? fonte.charAt(pos) : '\0'; }
                private char espia() { return pos + 1 < fonte.length() ? fonte.charAt(pos + 1) : '\0'; }
                private Token tok(TipoToken t, String lexema) { return new Token(t, lexema, linha); }
                private ErroCompilacao erro(String msg) { return new ErroCompilacao(linha, "erro léxico: " + msg); }
            """.trimIndent(),
            "Lexico.java (1/3) — campos e ajudantes. '\\0' faz o papel de 'fim do arquivo'.",
        ),
        Code(
            """
                public Token proximo() {
                    int estado = 0;
                    StringBuilder lex = new StringBuilder();
                    while (true) {
                        char c = atual();
                        switch (estado) {
                            case 0 -> {                                  // estado inicial
                                if (c == '\0') return tok(TipoToken.EOF, "fim de arquivo");
                                pos++;                                   // do estado 0 sempre se consome
                                if (c == '\n') linha++;
                                else if (c == ' ' || c == '\t' || c == '\r') { /* ignora */ }
                                else if (Character.isLetter(c)) { lex.append(c); estado = 1; }
                                else if (Character.isDigit(c))  { lex.append(c); estado = 2; }
                                else if (c == '<')  estado = 3;
                                else if (c == '>')  estado = 4;
                                else if (c == '/')  estado = 5;
                                else if (c == '\'') estado = 8;
                                else if (c == '"')  estado = 10;
                                else return simples(c);
                            }
                            case 1 -> {                                  // ID ou reservada
                                if (Character.isLetterOrDigit(c) || c == '_') { lex.append(c); pos++; }
                                else {                                   // 'outro': NÃO consome
                                    String s = lex.toString();
                                    return tok(RESERVADAS.getOrDefault(s, TipoToken.ID), s);
                                }
                            }
                            case 2 -> {                                  // NUM
                                if (Character.isDigit(c)) { lex.append(c); pos++; }
                                else if (Character.isLetter(c)) throw erro("número malformado '" + lex + c + "'");
                                else return tok(TipoToken.NUM, lex.toString());
                            }
                            case 3 -> {                                  // já leu '<'
                                if (c == '-') { pos++; return tok(TipoToken.ATRIB, "<-"); }
                                if (c == '=') { pos++; return tok(TipoToken.MENOR_IGUAL, "<="); }
                                if (c == '>') { pos++; return tok(TipoToken.DIFERENTE, "<>"); }
                                return tok(TipoToken.MENOR, "<");        // 'outro': NÃO consome
                            }
                            case 4 -> {                                  // já leu '>'
                                if (c == '=') { pos++; return tok(TipoToken.MAIOR_IGUAL, ">="); }
                                return tok(TipoToken.MAIOR, ">");
                            }
                            case 5 -> {                                  // já leu '/'
                                if (c == '/') { pos++; estado = 6; }
                                else if (c == '*') { pos++; estado = 7; }
                                else return tok(TipoToken.DIV, "/");
                            }
                            case 6 -> {                                  // comentário de linha
                                if (c == '\n' || c == '\0') estado = 0;  // o '\n' é contado no estado 0
                                else pos++;
                            }
                            case 7 -> {                                  // comentário de bloco
                                if (c == '\0') throw erro("comentário /* sem */ até o fim do arquivo");
                                if (c == '*' && espia() == '/') { pos += 2; estado = 0; }
                                else { if (c == '\n') linha++; pos++; }
                            }
                            case 8 -> {                                  // já leu a aspa simples
                                if (c == '\'' || c == '\n' || c == '\0') throw erro("caractere vazio ou não fechado");
                                lex.append(c); pos++; estado = 9;
                            }
                            case 9 -> {                                  // espera a aspa que fecha
                                if (c != '\'') throw erro("esperava ' depois de '" + lex + "'");
                                pos++;
                                return tok(TipoToken.CHAR, lex.toString());
                            }
                            case 10 -> {                                 // dentro da string
                                if (c == '"') { pos++; return tok(TipoToken.STRING, lex.toString()); }
                                if (c == '\n' || c == '\0') throw erro("string não fechada");
                                lex.append(c); pos++;
                            }
                            default -> throw new IllegalStateException("estado " + estado);
                        }
                    }
                }
            """.trimIndent(),
            "Lexico.java (2/3) — o AFD inteiro: um case por estado, igualzinho ao desenho",
        ),
        Code(
            """
                private Token simples(char c) {                      // tokens de 1 caractere
                    return switch (c) {
                        case '+' -> tok(TipoToken.MAIS, "+");
                        case '-' -> tok(TipoToken.MENOS, "-");
                        case '*' -> tok(TipoToken.VEZES, "*");
                        case '%' -> tok(TipoToken.MOD, "%");
                        case '=' -> tok(TipoToken.IGUAL, "=");
                        case '(' -> tok(TipoToken.ABRE_PAR, "(");
                        case ')' -> tok(TipoToken.FECHA_PAR, ")");
                        case '{' -> tok(TipoToken.ABRE_CHAVE, "{");
                        case '}' -> tok(TipoToken.FECHA_CHAVE, "}");
                        case ',' -> tok(TipoToken.VIRGULA, ",");
                        case ';' -> tok(TipoToken.PONTO_VIRGULA, ";");
                        default  -> throw erro("caractere inválido '" + c + "'");
                    };
                }
            }
            """.trimIndent(),
            "Lexico.java (3/3)",
        ),
        Predict(
            "Entrada `x<y`. O AFD está no estado 3 (já leu '<') e vê o 'y'. O que o código faz?",
            listOf(
                "Consome o 'y' e devolve MENOR",
                "Devolve MENOR SEM consumir o 'y' — ele será o começo do próximo token",
                "Erro léxico: '<y' não é token",
                "Volta ao estado 0 e descarta o '<'",
            ),
            1,
            "O 'y' é o 'outro*' do desenho: ele só serviu para avisar que o token acabou. Se você fizer `pos++` aí, o 'y' some e o parser recebe `x <` seguido do que vier depois. É o bug nº 1 de léxico feito à mão.",
        ),
        Callout(WATCH_OUT, "Contagem de linhas: incremente `linha` em TODO lugar que consome '\\n' — inclusive dentro do comentário de bloco (estado 7). E não conte '\\r': arquivo salvo no Windows tem '\\r\\n' no fim de cada linha; contar os dois dobra o número da linha nas mensagens."),
        Callout(WATCH_OUT, "`Character.isLetter` aceita 'ç', 'é', 'π'… Se a especificação diz que identificador é só ASCII, troque por `(c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')`. Detalhe pequeno, mas teste de correção adora."),
        Steps(
            "Os erros léxicos da Byteiro (sempre com a linha!)",
            listOf(
                "Caractere inválido: `x <- 3 # 2;` → linha N: erro léxico: caractere inválido '#'",
                "Número malformado: `12abc` → número malformado '12a'",
                "Caractere vazio ou sem fechar: `''` ou `'A`",
                "String que atravessa a linha: `\"olá` + Enter",
                "Comentário `/*` sem `*/` até o fim do arquivo",
            ),
        ),
        Callout(REAL_WORLD, "Muitos enunciados pedem palavras-chave que ignoram maiúsculas (`SE`, `Se`, `se`). É UMA linha: `RESERVADAS.getOrDefault(s.toLowerCase(), TipoToken.ID)`. Decida também se identificadores ignoram maiúsculas (`Soma` = `soma`?) — e documente."),
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
            "TesteLexico.java — rode ANTES de escrever uma linha do parser",
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
            "Saída esperada: o comentário sumiu, `<-` e `<>` são UM token cada, e a linha muda no lugar certo",
        ),
        Reveal("Por que `proximo()` devolve um token por vez em vez de uma lista com todos?", "Porque o parser de uma passada pede tokens sob demanda — e um erro léxico aparece exatamente quando o parser chega lá. Gerar a lista inteira antes também funciona (e permite espiar vários tokens à frente); só garanta que o erro léxico continue saindo com a linha certa."),
        Say(LEXI, "Fatiado! Agora alguém precisa conferir se os pedaços estão na ORDEM certa. Chama a Profª Gramática."),
    ),
)
