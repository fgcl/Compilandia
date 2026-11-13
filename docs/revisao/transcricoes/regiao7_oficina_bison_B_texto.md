# Região 7: Oficina Yacc/Bison — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO YACC_BISON: Yacc/Bison: especificação e precedência

#### ESTILO STORY (História / Analogia): "A fábrica que fabrica analisadores"
- NARRADOR: Oficina Yacc/Bison. Um bisão de avental martela tabelas LALR numa bigorna.
- BYTE: Eu passei a semana montando tabela SLR na mão. Tem jeito mais fácil?
- PROF: Tem: você escreve a **receita** (a gramática) e uma fábrica constrói a **máquina** (o analisador em C). Essa fábrica é o Bison, um compilador de compiladores.
- TEXTO: Pense numa padaria industrial. Você entrega uma ficha de pedido em **três partes**, separadas por %%: **ingredientes** (declarações), **modo de preparo** (regras) e **observações** (código C extra).
- PASSOS (O mapa da analogia):
    1) Ficha do pedido → arquivo **calc.y**
    2) Lista de ingredientes (%token NUM, %left '+') → **declarações**
    3) Modo de preparo (expr : expr '+' expr) → **regras** da gramática
    4) 'Quando terminar esta etapa, anote o total' → **ação semântica** { $$ = $1 + $3; }
    5) Fornecedor que entrega um ingrediente por vez → **yylex** (a Lexi, ou o Flex)
    6) O robô confeiteiro que segue a tabela → **yyparse**
    7) Alarme de fumaça → **yyerror**
- LEXI: Eu sou o fornecedor! Corto '12+3' em NUM, '+', NUM e entrego um por vez quando o yyparse pede.
- CÓDIGO (A ficha de pedido mais famosa: a calculadora (versão que o jogo entende)):
```
%token NUM
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

```
- PREVEJA: O robô vai montar 2+3*4. Sem nenhuma linha %left, o que você acha que acontece?
    opções: Ele recusa a receita | ✔Ele avisa conflitos e, por padrão, prefere empilhar (shift) | Ele calcula sempre da esquerda para a direita
    explicação: A gramática é ambígua, então há conflitos empilhar/reduzir. O Bison avisa e escolhe shift em cada um, e às vezes o resultado sai errado.
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %right UMINUS ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '-' expr { $$ = $1 - $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '/' expr { $$ = $1 / $3; } ⏎      | '-' expr %prec UMINUS { $$ = -$2; } ⏎      | '(' expr ')' { $$ = $2; } ⏎      | NUM ⏎      ; ⏎ » entrada=«2+3*4» legenda: Com %left '+' '-' e %left '*' '/' na ficha, 2+3*4 = 14. Veja as reduções e os $$.
- CAIXA [Não existem perguntas idiotas]: 'Yacc' quer dizer *Yet Another Compiler-Compiler* (Bell Labs, anos 1970). 'Bison' é a versão GNU, trocadilho: yak (iaque) → bisão.
- CAIXA [No mundo real]: O PostgreSQL (gram.y), o Bash (parse.y) e o PHP usam Bison até hoje. O GCC também usava, mas trocou por descida recursiva escrita à mão (C++ no GCC 3.4, C no 4.1) para ter mensagens de erro melhores.

#### ESTILO VISUAL (Passo a passo visual): "Ligando e desligando a precedência"
- PROF: Experimento de laboratório: a mesma gramática ambígua, com e sem declarações de precedência.
- WIDGET SLR_TABLE gramática=«E -> E + E | E * E | ( E ) | id» legenda: Por baixo do capô: a gramática ambígua tem conflitos empilhar/reduzir em + e *.
- WIDGET BISON gramática=«%token NUM ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | NUM ⏎      ; ⏎ » entrada=«2*3+4» legenda: SEM precedência: 4 conflitos, o Bison escolhe shift em todos... e 2*3+4 dá 14 (calculou 2*(3+4))!
- BYTE: Catorze?! Minha calculadora de bolso discorda.
- PILHA: Eu tinha 2*3... vi o '+'... o padrão mandou empilhar. Eu... obedeci.
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %right UMINUS ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '-' expr { $$ = $1 - $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '/' expr { $$ = $1 / $3; } ⏎      | '-' expr %prec UMINUS { $$ = -$2; } ⏎      | '(' expr ')' { $$ = $2; } ⏎      | NUM ⏎      ; ⏎ » entrada=«2*3+4» legenda: COM %left: na célula (estado de 'expr * expr', token '+'), a regra de '*' é mais forte que '+' → reduce. Resultado 10.
- PASSOS (Como o Bison decide cada conflito empilhar/reduzir):
    1) Precedência da **regra** = a do último terminal dela (ou a do %prec)
    2) Precedência do **token** de lookahead = a linha onde ele foi declarado
    3) Token mais forte → **shift**; regra mais forte → **reduce**
    4) Empate: %left → reduce; %right → shift; %nonassoc → erro de sintaxe
- WIDGET BISON gramática=«%token NUM ⏎ %right '-' ⏎ %% ⏎ expr : expr '-' expr { $$ = $1 - $3; } ⏎      | NUM ⏎      ; ⏎ » entrada=«8-3-2» legenda: %right '-' por engano: 8-3-2 vira 8-(3-2) = 7. Subtração tem que ser %left.
- PREVEJA: E com %right '^' para potência, quanto dá 2^3^2?
    opções: 64, ou seja (2^3)^2 | ✔512, ou seja 2^(3^2) | Erro de sintaxe
    explicação: %right: no empate, shift. A pilha guarda 2^3 e empilha o segundo '^': 3^2 = 9 primeiro, depois 2^9 = 512. Como na matemática.
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' ⏎ %left '*' ⏎ %right '^' ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '^' expr { $$ = $1 ^ $3; } ⏎      | NUM ⏎      ; ⏎ » entrada=«2^3^2» legenda: Confira: 512. A linha %right '^' vem por último, então liga mais forte que * e +.
- WIDGET BISON gramática=«%token NUM ⏎ %nonassoc '<' ⏎ %left '+' ⏎ %% ⏎ expr : expr '<' expr { $$ = $1 < $3; } ⏎      | expr '+' expr { $$ = $1 + $3; } ⏎      | NUM ⏎      ; ⏎ » entrada=«1+1<3» legenda: %nonassoc '<': 1+1<3 funciona (= 1), mas tente mentalmente 1<2<3, vira erro de sintaxe, de propósito.
- CAIXA [Poder do cérebro]: A precedência não muda a gramática: ela só apaga uma das duas ações das células em conflito. A tabela LALR continua lá embaixo.

#### ESTILO WORKED (Exemplo resolvido): "Calculadora completa: Bison + Flex + gcc"
- PROF: Vamos montar uma calculadora de verdade, do jeito que você faria no laboratório.
- CÓDIGO (calc.y: declarações %% regras %% código C (com um yylex escrito à mão)):
```
%{
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

```
- PASSOS (Lendo o arquivo por partes):
    1) %{ ... %}: código C copiado literalmente para o topo do analisador gerado
    2) %token NUM: um token com nome; o Bison numera a partir de 258 (caracteres como '+' usam o próprio código ASCII)
    3) %left / %right: precedência, linhas mais embaixo ligam mais forte
    4) %%: começam as regras. $$ é o valor do lado esquerdo; $1, $2, $3 os do lado direito
    5) %%: começa o código C do usuário: yylex, yyerror e main
    6) yylex devolve o código do token e coloca o valor em yylval; devolver 0 = fim da entrada
- CÓDIGO (calc.l: o mesmo léxico gerado pelo Flex (aí você apaga o yylex do calc.y)):
```
%{
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

```
- CÓDIGO (Compilando e rodando):
```
bison -d calc.y          # gera calc.tab.c e calc.tab.h
flex calc.l              # gera lex.yy.c
gcc calc.tab.c lex.yy.c -o calc
echo "2+3*4" | ./calc     # imprime: = 14
```
- LEXI: O calc.tab.h é o contrato entre mim e o bisão: ali está escrito que NUM vale 258.
- PASSOS (Rastreando 2+3*4 (só as decisões importantes)):
    1) Pilha 'expr + expr', próximo token '*': regra de '+' (nível 1) < token '*' (nível 2) → **shift**
    2) Pilha 'expr + expr * expr', fim da linha: reduz expr * expr → $$ = 3 * 4 = 12
    3) Pilha 'expr + expr': reduz → $$ = 2 + 12 = 14
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %right UMINUS ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '-' expr { $$ = $1 - $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '/' expr { $$ = $1 / $3; } ⏎      | '-' expr %prec UMINUS { $$ = -$2; } ⏎      | '(' expr ')' { $$ = $2; } ⏎      | NUM ⏎      ; ⏎ » entrada=«-5+2» legenda: No jogo, a mesma calculadora (sem o prólogo C). Veja o menos unário com %prec UMINUS.
- BYTE: Minha vez!
- REVELAR: Com %left '-', quanto vale 8-3-2? E se fosse %right '-'?
    → %left: empate → reduce → (8-3)-2 = 3. %right: empate → shift → 8-(3-2) = 7.
- REVELAR: Na regra '-' expr %prec UMINUS, por que o %prec?
    → Sem ele, a regra herdaria a precedência do '-' binário (último terminal da regra). Com %prec UMINUS, ela ganha a precedência da linha %right UMINUS, a mais forte.
- REVELAR: No mini-Bison do jogo, o que NÃO entra na especificação?
    → O prólogo %{ %}, a terceira seção com código C, %union/%type. O jogo já traz um léxico pronto (números viram NUM, palavras viram ID) e entende ações do tipo $$ = expressão;.
- CAIXA [Dica de prova]: Se a prova pede 'escreva a especificação Yacc', não esqueça: os dois %%, o %token de cada token com nome, o ';' no fim de cada regra e as ações entre chaves.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que deixar a gramática ambígua?"
- PROF: Pergunta provocadora: se ambiguidade é ruim, por que quase toda calculadora em Bison usa expr : expr '+' expr?
- REVELAR: Qual a alternativa sem ambiguidade?
    → Camadas: expr → expr + term | term; term → term * fator | fator. Funciona, mas cresce uma camada por nível de precedência.
- WIDGET BISON gramática=«%token NUM ⏎ %% ⏎ expr  : expr '+' term   { $$ = $1 + $3; } ⏎       | term ⏎       ; ⏎ term  : term '*' fator  { $$ = $1 * $3; } ⏎       | fator ⏎       ; ⏎ fator : NUM ⏎       | '(' expr ')'    { $$ = $2; } ⏎       ; ⏎ » entrada=«2+3*4» legenda: Versão em camadas: zero conflitos, zero %left, e ainda dá 14.
- REVELAR: E qual a vantagem da versão ambígua + %left?
    → Gramática curta, legível e com menos reduções unitárias (term → fator...). A precedência fica numa tabelinha de declarações.
- REVELAR: Como o Bison sabe a precedência de uma REGRA?
    → Pelo último terminal do lado direito (expr '*' expr → a de '*'), ou pelo %prec, se você escrever.
- REVELAR: E a do lookahead?
    → Pela linha de %left/%right/%nonassoc em que o token aparece. Linhas mais embaixo = mais forte.
- PREVEJA: Um conflito empilhar/reduzir em que o token NÃO tem precedência declarada. O que o Bison faz?
    opções: Para a compilação com erro | ✔Escolhe shift e emite um aviso de conflito | Escolhe reduce
    explicação: Padrão do Yacc/Bison: shift, com aviso ('conflicts: N shift/reduce'). Para reduzir/reduzir, escolhe a regra que aparece primeiro no arquivo.
- REVELAR: Esse padrão 'shift' é bom para o if-then-else pendurado?
    → É! Shift no 'else' faz o else casar com o if mais próximo, o que C e Java fazem. Por isso muita gente aceita esse conflito (e o declara com %expect 1).
- WIDGET PARSE_TREE gramática=«S -> if c then S | if c then S else S | x» entrada=«if c then if c then x else x» legenda: Duas árvores possíveis: de quem é o else? O shift escolhe o if de dentro.
- REVELAR: Por que conflitos reduzir/reduzir são mais perigosos?
    → Porque 'pegar a primeira regra' quase nunca é o que você queria, e uma das regras pode ficar inalcançável. Geralmente indicam erro no desenho da gramática.
- CAIXA [Poder do cérebro]: Você pode ligar os relatórios: bison -v calc.y gera calc.output com todos os estados e conflitos. É a tabela que você aprendeu a montar à mão!

#### ESTILO FORMAL (Definição de prova): "Estrutura da especificação e resolução de conflitos"
- CÓDIGO (Formato geral de um arquivo .y):
```
declarações
%%
regras de tradução
%%
funções auxiliares em C
```
- PASSOS (Declarações):
    1) %{ código C %}: copiado para o início do analisador
    2) %token NOME — declara terminais com nome
    3) %left / %right / %nonassoc t1 t2, mesma precedência na linha; linhas posteriores têm precedência maior
    4) %start s: símbolo inicial (padrão: lado esquerdo da primeira regra)
    5) %union / %type <campo>, tipos dos valores semânticos (padrão: int, via YYSTYPE)
- TEXTO: **Regra:** cabeça : corpo1 { ação1 } | corpo2 { ação2 } ;, na ação, $$ é o atributo sintetizado da cabeça e $i o do i-ésimo símbolo do corpo. Ação omitida equivale a { $$ = $1; }.
- CÓDIGO (Regras de desambiguação do Yacc/Bison):
```
conflito em ACTION[s, a] entre shift e reduce por A → β:
  pa := precedência do token a
  pr := precedência da regra (%prec, ou último terminal de β)
  se pa ou pr indefinida:   shift   (+ aviso de conflito)
  senão se pa > pr:         shift
  senão se pa < pr:         reduce
  senão (empate):
      %left     → reduce
      %right    → shift
      %nonassoc → erro
conflito reduce/reduce:     reduce pela regra que aparece primeiro (+ aviso)
```
- PASSOS (Interface com o resto do programa):
    1) int yyparse(void) — o analisador LALR(1); retorna 0 em sucesso e 1 em erro de sintaxe
    2) int yylex(void): chamado pelo yyparse; retorna o código do token (0 = fim) e põe o valor em yylval
    3) void yyerror(const char *s), chamado em erro de sintaxe
    4) bison -d gera o cabeçalho .tab.h com os códigos dos tokens, que o Flex inclui
- PREVEJA: Declarações: %left '+' '-'  e, abaixo, %left '*' '/'. Em 'expr - expr' com lookahead '-', o Bison...
    opções: Empilha (shift) | ✔Reduz (reduce) | Dá erro
    explicação: Mesma precedência (mesma linha) e %left → reduce: (a - b) - c.
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %right UMINUS ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '-' expr { $$ = $1 - $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '/' expr { $$ = $1 / $3; } ⏎      | '-' expr %prec UMINUS { $$ = -$2; } ⏎      | '(' expr ')' { $$ = $2; } ⏎      | NUM ⏎      ; ⏎ » entrada=«(2+3)*4» legenda: A especificação no subconjunto que o mini-Bison do jogo aceita: %token, %left/%right/%nonassoc, %prec e ações $$ = ...;
- CAIXA [Dica de prova]: Questão clássica: 'quantos conflitos e como o Yacc os resolve?'. Responda com estado, token, as duas ações e a regra de desambiguação usada (precedência, associatividade ou padrão shift).
- CAIXA [Cuidado!]: A ordem das linhas importa ao contrário do que parece: a PRIMEIRA linha de %left é a de MENOR precedência.

#### ESTILO MNEMONIC (Truque de memória): "Ganchos da família yy"
- NARRADOR: Palácio da Memória: Oficina Yacc/Bison, sétima parada do caminho. Na parede, três gavetas separadas por %%.
- CAIXA [Gancho de memória]: **D-R-C: 'Declara %% Regra %% Código'.** Três gavetas, dois separadores.
- CAIXA [Gancho de memória]: Precedência: **'o último da fila é o VIP'**. A linha de %left mais embaixo é a que liga mais forte.
- CAIXA [Gancho de memória]: **'Na dúvida, empilha; no empate de reduções, quem chegou primeiro.'**: o padrão do Bison para E/R e R/R.
- CAIXA [Gancho de memória]: %left = **'esquerda reduz'**; %right = **'direita empilha'**; %nonassoc = **'não se mistura'** (a < b < c vira erro).
- CÓDIGO (A família yy: o sobrenome de todo mundo):
```
yylex    → LÊ o próximo token          (a Lexi)
yyparse  → ANALISA (LALR)              (o bisão)
yyerror  → RECLAMA                     (o alarme)
yylval   → VALOR do token atual        (o 12 de NUM)
yytext   → TEXTO casado pelo Flex      ("12")
```
- CAIXA [Gancho de memória]: **$$ é o pai, $1 $2 $3 são os filhos** da esquerda para a direita. Os valores SOBEM: o pai é calculado a partir dos filhos.
- PREVEJA: Na regra expr : '(' expr ')' { $$ = ???; }, o que vai no lugar de ???
    opções: $1 | ✔$2 | $3
    explicação: '(' é $1, expr é $2, ')' é $3. O valor útil é o do meio.
- WIDGET BISON gramática=«%token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %right UMINUS ⏎ %% ⏎ expr : expr '+' expr { $$ = $1 + $3; } ⏎      | expr '-' expr { $$ = $1 - $3; } ⏎      | expr '*' expr { $$ = $1 * $3; } ⏎      | expr '/' expr { $$ = $1 / $3; } ⏎      | '-' expr %prec UMINUS { $$ = -$2; } ⏎      | '(' expr ')' { $$ = $2; } ⏎      | NUM ⏎      ; ⏎ » entrada=«8-3-2» legenda: Bisão com %left '-': 8-3-2 = 3.
- REVELAR: Recite: como o Bison resolve um empate de precedência?
    → %left → reduce, %right → shift, %nonassoc → erro.
- CAIXA [Conversa ao pé da lareira]: Bison: 'Você escreve 20 linhas e eu te dou um analisador LALR inteiro.' Descida recursiva: 'E eu sou só código normal, fácil de depurar e com mensagens de erro bonitas. Pergunta pro GCC quem ele escolheu.'
