package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

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
        Skill.LEX_FLEX, Style.STORY, "A triagem dos Correios",
        listOf(
            Say(NARRADOR, "Centro de triagem dos Correios, madrugada. Uma esteira infinita de caracteres passa na frente de um robô. A Lexi segura uma folha plastificada cheia de regras."),
            Say(LEXI, "Eu não separo nada na mão. Eu escrevo a FOLHA DE REGRAS, entrego para a fábrica, e a fábrica monta o robô que faz a triagem sozinho."),
            Steps(
                "Correios → Lex/Flex",
                listOf(
                    "📄 A folha de regras ('pacote assim → carimbo tal') é o **arquivo .l**: cada linha é um **padrão** (expressão regular) e uma **ação** em C.",
                    "🏭 A fábrica que lê a folha e monta o robô é o programa **lex** (ou **flex**): ele gera o arquivo **lex.yy.c**.",
                    "🤖 O robô é a função **yylex()**: cada vez que alguém chama, ela pega o próximo pacote e devolve o carimbo.",
                    "📦 O pacote que o robô pegou é o **lexema**: fica em **yytext**, e o tamanho dele em **yyleng**.",
                    "🏷️ O carimbo é o **return TOKEN;** da ação. Ação vazia **{ }** é a lixeira: espaço e comentário somem.",
                ),
            ),
            Say(PROF, "O robô obedece a DUAS regras de ouro. Primeira: ele é **guloso** — sempre pega o MAIOR pacote que alguma regra aceita. Segunda: se duas regras aceitam pacotes do mesmo tamanho, vence a que está **mais acima na folha**."),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "if (x1 == 10) y = 2;", caption = "Veja qual regra carimbou cada lexema. Edite a entrada e teste!"),
            Predict(
                "Com essa folha (\"if\" acima da regra de identificador), o que vira 'iffy'?",
                listOf("IF seguido de ID 'fy'", "Um único ID 'iffy'", "Erro léxico", "IF, IF, ID"),
                1,
                "Guloso! A regra de ID aceita 4 caracteres; a do \"if\" só 2. O maior pacote vence, e a ordem nem chega a ser consultada.",
            ),
            Say(BYTE, "E se alguém colar a regra de ID ACIMA da regra do \"if\"?"),
            Visual(VK.FLEX, grammar = p4FlexOrdemErrada, input = "if x 10", caption = "Folha com a ordem trocada: olhe o que aconteceu com o 'if'."),
            Reveal("Por que 'if' virou ID?", "Empate: as duas regras aceitam 'if' com 2 caracteres. No empate vence a que vem PRIMEIRO — e agora é a de ID. Palavras-chave SEMPRE vêm antes da regra de identificador."),
            Callout(REAL_WORLD, "No trabalho prático da PUC Minas, o analisador léxico é feito exatamente assim: você escreve o scanner.l, roda `lex scanner.l && gcc lex.yy.c` e testa com `./a.out < programa.txt`. O lex escreve centenas de linhas de C por você."),
            Callout(NO_DUMB_QUESTIONS, "P: Lex e Flex são a mesma coisa? R: Flex ('fast lex') é a versão livre e mais rápida do Lex original. A sintaxe do .l é praticamente igual; em muitos Linux, o comando `lex` já chama o flex."),
            Reveal("Na analogia, quem é: a folha, a fábrica, o robô, o pacote e o carimbo?", "Arquivo .l, o programa lex/flex, a função yylex() gerada em lex.yy.c, o lexema (yytext) e o token devolvido pelo return."),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.VISUAL, "Flex ao vivo",
        listOf(
            Say(LEXI, "Cada widget abaixo é um scanner .l de verdade rodando. Embaixo de cada token aparece o número da regra que venceu. Mude a entrada à vontade!"),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "while (n) n = n;", caption = "Espaços casam com a regra 1, que não tem return: somem."),
            Text("Repare em `==` e `=`: as duas regras começam com '='. Em `x == 1`, a regra de `==` casa 2 caracteres e vence. Em `y = 2`, só a de `=` casa."),
            Visual(VK.FLEX, grammar = p4FlexOps, input = "a>=b c==d e=f", caption = "Desta vez \">\" está ANTES de \">=\" na folha. Mesmo assim, >= vira GE. Por quê?"),
            Predict(
                "Com essa mesma folha, como fica 'x===y'?",
                listOf("ID EQ EQ ID", "ID EQ ATRIB ID", "ID ATRIB EQ ID", "ID ATRIB ATRIB ATRIB ID"),
                1,
                "Guloso, da esquerda para a direita: primeiro o maior pedaço que casa a partir do '=' é '==' (EQ). Sobra um '=' (ATRIB). Teste digitando no widget acima!",
            ),
            Say(PROF, "Agora um scanner no estilo do trabalho prático, com comentário, número real e uma regra-coringa no fim para erro léxico."),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "float media = 3.14; // comentario", caption = "O comentário inteiro casa com \"//\".* e some. E 3.14 vira REAL, não NUM + erro + NUM."),
            Reveal("Por que '3.14' virou REAL e não NUM?", "A regra de REAL casa '3.14' (4 caracteres) e a de NUM só '3' (1 caractere). Maior casamento vence, mesmo com NUM vindo depois na folha."),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "int x@ = 2;", caption = "O '@' cai na regra-coringa '.', que imprime o erro e NÃO devolve token. O scanner continua."),
            Callout(WATCH_OUT, "A regra '.' tem que ser a ÚLTIMA. Ela casa qualquer caractere com tamanho 1; se viesse antes, ganharia todos os empates de tamanho 1 — e engoliria `+`, `;` e até identificadores de uma letra!"),
            Visual(VK.FLEX, grammar = p4FlexPontoPrimeiro, input = "x + ab", caption = "Prova: com '.' no topo, 'x' e '+' somem. Só 'ab' sobrevive, porque tem 2 letras."),
            Reveal("Quantos tokens saem de 'x + ab' nessa folha errada, e por quê?", "Só 1: ID 'ab'. 'x' e '+' têm tamanho 1 e empatam com '.', que vem primeiro e joga fora. 'ab' tem tamanho 2 e o '.' não alcança."),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.WORKED, "Escrevendo o scanner.l do TP",
        listOf(
            Say(PROF, "Vamos montar com calma um arquivo .l completo, do jeito que você entrega no trabalho prático. Ele compila sozinho, sem Bison."),
            Code(p4FlexCompleto, "scanner.l — compile com: lex scanner.l && gcc lex.yy.c && ./a.out < teste.txt"),
            Steps(
                "Lendo o arquivo, parte por parte",
                listOf(
                    "**%{ … %}**: código C copiado literalmente para o topo de lex.yy.c — includes, a enumeração dos tokens e o contador de linha.",
                    "**DIGITO [0-9]** e **LETRA [a-zA-Z_]**: definições nomeadas. Nas regras, usamos {DIGITO} e {LETRA} como apelidos.",
                    "Primeiro **%%**: começam as regras. Cada linha: padrão, espaço, ação entre chaves.",
                    "Espaços e comentários: ação sem return → são descartados. O **\\n** só soma 1 em `linha`.",
                    "Palavras-chave (\"int\", \"float\", \"if\", \"else\") ANTES da regra de identificador: ganham o empate.",
                    "REAL antes de NUM por clareza (o maior casamento já resolveria). \"<=\" antes de \"<\", também por clareza.",
                    "A regra **.** por último: qualquer caractere que sobrou é erro léxico, com o número da linha.",
                    "Segundo **%%**: código C do usuário. **yywrap** devolvendo 1 avisa que não há outro arquivo; o **main** chama yylex() até ela devolver 0 (fim da entrada).",
                ),
            ),
            Text("Agora vamos 'rodar de cabeça' a linha `float media = 3.14; // comentario`."),
            Steps(
                "Resolvido: tokens da linha",
                listOf(
                    "'float' → empate entre \"float\" e ID (5 caracteres): vence \"float\", que vem antes → **FLOAT**.",
                    "' ' → regra de espaços, ação vazia: nada.",
                    "'media' → só ID casa → **ID**, yytext = \"media\", yyleng = 5.",
                    "'=' → **ATRIB**. '3.14' → REAL casa 4 caracteres, NUM só 1 → **REAL**.",
                    "';' → **PV**. '// comentario' → \"//\".* engole até o fim da linha → nada.",
                ),
            ),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "float media = 3.14; // comentario", caption = "Confira: FLOAT ID ATRIB REAL PV."),
            Text("Sua vez (exemplo esmaecido), com o mesmo scanner."),
            Reveal("Tokens de `intx = 3.5;`?", "ID ATRIB REAL PV. 'intx' tem 4 letras: a regra de ID casa mais que a de \"int\" (3). Guloso!"),
            Reveal("Tokens de `if (x <= 10) x = x + 1;`?", "IF ABRE ID LE NUM FECHA ID ATRIB ID MAIS NUM PV — 12 tokens. '<=' é um token só."),
            Reveal("O que acontece com `int x@ = 2;`?", "Tokens INT ID ATRIB NUM PV, e o scanner imprime 'Erro lexico na linha 1: '@''. A regra '.' não tem return, então o '@' não vira token e a análise continua."),
            Visual(VK.FLEX, grammar = p4FlexTp, input = "if (x <= 10) x = x + 1;", caption = "Confira a segunda resposta (e teste as outras digitando)."),
            Callout(EXAM_TIP, "Sem `yywrap` (ou `%option noyywrap`), o gcc reclama de 'undefined reference to yywrap'. Alternativa: compilar com `-ll` (lex) ou `-lfl` (flex), que trazem uma yywrap pronta."),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.SOCRATIC, "Por que o .l é desse jeito?",
        listOf(
            Say(PROF, "Hoje você vai descobrir as regras do Flex respondendo perguntas. Chute à vontade."),
            Predict(
                "Na folha abaixo, \"<\" vem ANTES de \"<=\". O que vira 'a<=b'?",
                listOf("ID LT ATRIB ID", "ID LE ID", "Erro: falta a regra do '='", "ID LT ID"),
                1,
                "O robô é guloso: \"<=\" casa 2 caracteres, \"<\" só 1. A ordem só desempata lexemas do MESMO tamanho.",
            ),
            Visual(VK.FLEX, grammar = p4FlexMenorPrimeiro, input = "a<=b c<d", caption = "Confira: LE em 'a<=b', LT em 'c<d'."),
            Say(BYTE, "Então a ordem das regras nunca importa?"),
            Predict(
                "Quando a ordem das regras IMPORTA?",
                listOf("Nunca", "Sempre", "Só quando duas regras casam lexemas do mesmo tamanho", "Só para números"),
                2,
                "Empate de tamanho é o único caso. O exemplo clássico: \"if\" e a regra de ID, ambas casando 'if'.",
            ),
            Predict(
                "Uma regra sem `return`, como `[ \\t]+ { }`, faz o quê?",
                listOf("Devolve token vazio", "Para o scanner", "Descarta o lexema e yylex() continua procurando o próximo", "Dá erro de compilação"),
                2,
                "yylex() é um laço: só sai quando uma ação executa return. Sem return, ela joga o lexema fora e segue lendo.",
            ),
            Predict(
                "O sintático precisa saber o VALOR de um número (42, não só 'NUM'). Por onde ele chega?",
                listOf("Pelo return", "Por yylval, preenchido na ação: yylval = atoi(yytext);", "Por yyleng", "O sintático lê o arquivo de novo"),
                1,
                "O return leva só a CLASSE (o token). O atributo vai numa variável global combinada com o Bison: yylval.",
            ),
            Predict(
                "Se a regra '.' (qualquer caractere) ficar no TOPO da folha, o que acontece com 'x + ab'?",
                listOf("Nada muda", "x e + somem; só 'ab' vira ID", "Tudo some", "Erro de compilação"),
                1,
                "x e + têm tamanho 1: empatam com '.', que vem primeiro e descarta. 'ab' tem tamanho 2, então a regra de ID vence pelo tamanho.",
            ),
            Visual(VK.FLEX, grammar = p4FlexPontoPrimeiro, input = "x + ab", caption = "Confira o estrago."),
            Reveal("Como o Flex consegue achar o MAIOR casamento entre dezenas de regras de uma vez?", "Ele junta os AFNs de todas as regras num só (um novo estado inicial com ε para cada um), converte para AFD e roda o AFD até travar no estado morto, lembrando o ÚLTIMO estado final por onde passou. Esse final diz o tamanho do lexema e a regra (a de menor número, em caso de empate)."),
            Reveal("E por que o resultado é tão rápido?", "Porque é um AFD: um passo por caractere, uma consulta na tabela. Toda a dificuldade (subconjuntos, minimização) foi paga quando o lex gerou o lex.yy.c."),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.FORMAL, "A especificação Lex: definição de prova",
        listOf(
            Text("Um programa Lex tem três seções separadas por `%%`: **declarações** (código C entre `%{` e `%}` e definições regulares nome → ER), **regras de tradução** na forma `Padrão { Ação }` e **funções auxiliares** em C."),
            Code("declarações\n%%\nregras de tradução\n%%\nfunções auxiliares", "Estrutura de um arquivo .l"),
            Code(
                """
                EXPRESSÃO  CASA COM                         EXEMPLO
                c          o caractere c (não operador)     a
                \c         o caractere c literalmente       \*
                "s"        a cadeia s literalmente          "**"
                .          qualquer caractere menos \n      a.*b
                ^          início de linha                  ^abc
                ${'$'}          fim de linha                     abc${'$'}
                [s]        qualquer caractere de s          [abc]
                [^s]       qualquer caractere fora de s     [^abc]
                r*         zero ou mais r                   a*
                r+         um ou mais r                     a+
                r?         zero ou um r                     a?
                r{m,n}     de m a n ocorrências de r        a{1,5}
                r1r2       r1 seguido de r2                 ab
                r1|r2      r1 ou r2                         a|b
                r1/r2      r1, se seguido de r2             abc/123
                """.trimIndent(),
                "Expressões regulares do Lex (Livro do Dragão, Fig. 3.8)",
            ),
            Text("**Resolução de conflitos** (Dragão, 3.5.3): (1) prefira sempre o **prefixo mais longo** da entrada que casa com algum padrão; (2) se o prefixo mais longo casa com dois ou mais padrões, prefira o **listado primeiro**."),
            Steps(
                "Interface do analisador gerado",
                listOf(
                    "**yylex()**: devolve o código do próximo token; devolve 0 no fim da entrada.",
                    "**yytext**: ponteiro para o início do lexema reconhecido; **yyleng**: seu comprimento.",
                    "**yylval**: variável global com o valor de atributo do token, compartilhada com o analisador sintático (Yacc/Bison).",
                    "**yyin**: arquivo de entrada (padrão: stdin). **yywrap()**: chamada no fim do arquivo; devolver 1 significa 'acabou'.",
                ),
            ),
            Text("**Implementação**: o Lex constrói um AFN para cada padrão pᵢ, cria um novo estado inicial com transições ε para cada um, e converte o AFN combinado em AFD. Ao ler a entrada, o AFD segue até não haver transição; então recua até o último estado de aceitação visto. Se esse estado contém finais de vários padrões, vale o de menor índice."),
            Predict(
                "Com as regras `\"if\" {…}` e `[a-z]+ {…}` nessa ordem, a entrada 'ifa' produz:",
                listOf("o token de \"if\" e depois o lexema 'a'", "um único lexema 'ifa', da regra [a-z]+", "erro: conflito"),
                1,
                "Regra 1 de resolução: prefixo mais longo. [a-z]+ casa 3 caracteres; \"if\" casa 2.",
            ),
            Predict(
                "Qual é o papel do operador `/` em `DO/({letra}|{digito})*=…`?",
                listOf("Divisão", "Lookahead: só casa DO se o que vem depois casa com o resto, mas o resto não entra no lexema", "Comentário", "Alternativa, como |"),
                1,
                "É o operador de contexto à direita (lookahead) — usado no exemplo do Fortran, onde DO5I=1.25 é atribuição e DO5I=1,25 é laço.",
            ),
            Callout(EXAM_TIP, "Em prova discursiva: cite as 3 seções, as 2 regras de resolução de conflito (mais longo; empate → primeiro) e explique que o Lex gera um AFD a partir das ERs (Thompson + subconjuntos). Bônus: palavras-chave antes de identificadores."),
            Reveal("Escreva a regra de um identificador que devolve ID e guarda o nome para o sintático.", "[a-zA-Z_][a-zA-Z0-9_]*   { yylval.nome = strdup(yytext); return ID; } — strdup copia, porque yytext será sobrescrito no próximo token."),
        ),
    ),
    Lesson(
        Skill.LEX_FLEX, Style.MNEMONIC, "Guloso, mas educado",
        listOf(
            Callout(MNEMONIC, "**D-R-C**: **D**efinições %% **R**egras %% **C**ódigo. Igualzinho ao Bison: Declara, Regra, Código. Os dois %% são as duas paredes da sala das regras."),
            Callout(MNEMONIC, "O Flex é **guloso, mas educado**: GULOSO porque sempre come o maior pedaço; EDUCADO porque, no empate, respeita a fila — quem chegou primeiro no arquivo é atendido."),
            Callout(MNEMONIC, "**Palavra-chave é VIP**: entra ANTES do ID na folha. Se o ID passar na frente, 'if' vira variável e o seu TP quebra."),
            Text("📍 **Palácio da memória**: na Floresta Léxica, na beira do rio, fica a **Oficina Flex** 🦎. Na porta está pregada a folha D-R-C. Lá dentro, a Lexi alimenta a máquina com expressões regulares e sai pelo outro lado um robô chamado lex.yy.c."),
            Callout(MNEMONIC, "Tudo do lex começa com **yy**: **yytext** = o TEXTO do lexema; **yyleng** = o LENGth (tamanho); **yylval** = o VALor que vai para o Bison; **yylex** = o próprio LEXer; **yywrap** = 'embrulha' (acabou o arquivo?)."),
            Callout(MNEMONIC, "**{ } vazio = lixeira.** Sem return, o lexema vai para o lixo e o robô continua. É assim que espaços e comentários somem."),
            Predict(
                "yytext ou yylval: onde está o texto \"42\" logo depois de casar `[0-9]+`?",
                listOf("yytext", "yylval", "yyleng", "yylex"),
                0,
                "yytext é o TEXTO cru. yylval só tem o valor se a SUA ação fizer yylval = atoi(yytext).",
            ),
            Visual(VK.FLEX, grammar = p4FlexBasico, input = "iffy = while2;", caption = "Guloso: 'iffy' e 'while2' são IDs. Educado: em 'if' sozinho, a regra de cima ganharia."),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que a ordem resolve TUDO — o tamanho vem antes; (2) pôr a regra '.' no começo; (3) esquecer yywrap/noyywrap; (4) devolver o token do espaço em vez de ignorar; (5) confundir lex.l (o que você escreve) com lex.yy.c (o que o lex gera)."),
            Callout(REAL_WORLD, "Receita do TP para nunca esquecer: `lex scanner.l` → `gcc lex.yy.c` → `./a.out < teste.txt`. Com Bison: `yacc -d parser.y` gera y.tab.h com os tokens, que o scanner.l inclui no %{ %}."),
            Reveal("Recite as duas regras de ouro e o mnemônico das seções.", "1) Maior casamento (guloso). 2) Empate → regra listada primeiro (educado). Seções: D-R-C, Definições %% Regras %% Código."),
        ),
    ),
)
