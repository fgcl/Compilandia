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
        Skill.SLR_TABLE, Style.STORY, "A açaiteria que segue o quadro",
        listOf(
            Say(NARRADOR, "Fortaleza Bottom-Up, ala oeste. Na parede, um quadro gigante cheio de células: s5, r2, acc, vazio, vazio, s7..."),
            Say(BYTE, "Parece uma planilha de Excel que deu errado. Pra que serve isso?"),
            Say(PROF, "É o cérebro de um analisador LR: a tabela ACTION/GOTO. Vou te explicar com uma açaiteria."),
            Text("A atendente monta copos de açaí. Ela **coloca ingredientes no copo** um a um (empilha). Quando o copo tem uma combinação completa — açaí + granola + banana —, ela **troca tudo por um nome**: 'Combo Clássico' (reduz)."),
            Text("Ela não pensa: olha **onde está no pedido** (o estado) e **o próximo pedido do cliente** (o token), e obedece o quadro da parede."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Momento do pedido (o que já está no copo) → **estado** do autômato LR(0)",
                    "Próximo pedido do cliente → **token de lookahead**",
                    "'Põe no copo e vai para o momento 7' → **shift 7** (s7)",
                    "'Troca açaí+granola+banana por Clássico' → **reduce** por Clássico → açaí granola banana",
                    "'Com um Clássico na mão, vá para o momento 4' → **GOTO**[estado, Clássico] = 4",
                    "'O que pode vir DEPOIS de um Clássico' → **FOLLOW(Clássico)**",
                ),
            ),
            Say(PILHA, "Eu sou... a pilha. Só vejo... meu topo. O quadro diz... o resto. Gosto de quadros."),
            Predict(
                "O copo tem açaí + granola + banana (um Clássico completo). O próximo pedido é 'leite condensado'. Na regra SLR, quando ela fecha o Clássico?",
                listOf(
                    "Sempre: copo completo = fecha na hora",
                    "Só se 'leite condensado' puder vir DEPOIS de um Clássico (está no FOLLOW)",
                    "Nunca: ela só empilha",
                ),
                1,
                "O SLR só coloca 'reduz' nas colunas do FOLLOW. Se leite condensado nunca vem depois de um Clássico, fechar agora seria um erro garantido.",
            ),
            Callout(WATCH_OUT, "Se a mesma célula do quadro mandar 'põe no copo' E 'fecha o combo', a atendente trava. Isso é um **conflito empilhar/reduzir**. Duas ordens de 'fecha' diferentes = **reduzir/reduzir**."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = "O quadro de verdade para E → E + T | T ... — nenhuma célula com duas ordens: a gramática é SLR(1)."),
            Reveal("Na analogia, o que faz o papel do FOLLOW?", "A lista do que pode vir logo depois de um combo. A atendente só fecha o combo (reduz) se o próximo pedido está nessa lista."),
            Callout(NO_DUMB_QUESTIONS, "Por que 'Simple'? Porque o SLR usa o autômato LR(0) mais simples possível e decide reduções com o FOLLOW, que você já sabe calcular. Simples, mas um pouco míope — e isso vai dar assunto."),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.VISUAL, "Do autômato à tabela, célula por célula",
        listOf(
            Say(PROF, "Hoje a gente constrói a tabela olhando o autômato LR(0). Primeiro, os estados."),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_EXPR, caption = "Coleção canônica LR(0) da gramática de expressões (aumentada com E' → E)."),
            Steps(
                "Quatro regras para encher a tabela",
                listOf(
                    "Seta com **terminal** a de Ii para Ij → ACTION[i, a] = **sj**",
                    "Seta com **não-terminal** A de Ii para Ij → GOTO[i, A] = **j**",
                    "Item completo **A → α •** em Ii → **rN** em ACTION[i, a] para cada a em **FOLLOW(A)**",
                    "Item **E' → E •** em Ii → ACTION[i, \$] = **acc**",
                ),
            ),
            Visual(VisualKind.FIRST_FOLLOW, grammar = P3_EXPR, caption = "Vamos precisar do FOLLOW: FOLLOW(E) = { +, ), \$ } e FOLLOW(T) = FOLLOW(F) = { +, *, ), \$ }."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = "A tabela pronta. Procure a linha do estado 2."),
            Say(BYTE, "Peraí! O estado 2 tem E → T • (quer reduzir) e T → T • * F (quer empilhar). Isso não dá briga?"),
            Predict(
                "No estado 2, com o próximo token sendo '*', o que a tabela SLR manda?",
                listOf("Reduzir por E → T", "Empilhar '*' (s7)", "Conflito: as duas coisas"),
                1,
                "'*' não está em FOLLOW(E) = { +, ), \$ }, então o reduce nem é escrito nessa coluna. Sobra só o shift. Briga evitada pelo FOLLOW!",
            ),
            Visual(VisualKind.LR_TRACE, grammar = P3_EXPR, input = "id + id * id", caption = "Siga a pilha: no estado 9, com '*', ele empilha; só depois reduz T → T * F."),
            Say(PILHA, "Viu? Eu guardei E + T... e esperei o '*'. Por isso... a multiplicação liga mais forte."),
            Say(PROF, "Agora uma gramática onde o FOLLOW não salva ninguém."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = "S → L = R | R; L → * R | id; R → L. Ache a célula vermelha: estado 2, coluna '='."),
            Reveal(
                "Por que '=' entrou em FOLLOW(R)?",
                "L → * R joga FOLLOW(L) dentro de FOLLOW(R), e S → L = R coloca '=' em FOLLOW(L). Resultado: o SLR escreve r5 (R → L) em [2, =], junto com s6 de S → L • = R.",
            ),
            Callout(BRAIN_POWER, "Essa gramática NÃO é ambígua. O conflito é culpa do SLR ser míope: nenhuma forma sentencial começa com 'R ='. Guarde essa: é a porta de entrada do LALR."),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.WORKED, "Preenchendo linhas da tabela com calma",
        listOf(
            Say(PROF, "Exercício clássico de prova: preencher linhas da tabela SLR da gramática de expressões. Vou fazer duas; você faz as outras."),
            Code("(1) E → E + T     (2) E → T\n(3) T → T * F     (4) T → F\n(5) F → ( E )     (6) F → id", "Produções numeradas (o número vira o rN)"),
            Code("I1: E' → E •      E → E • + T          (+ → I6)\nI2: E → T •       T → T • * F          (* → I7)\nI9: E → E + T •   T → T • * F          (* → I7)", "Três estados do autômato LR(0)"),
            Text("FOLLOW(E) = { +, ), \$ }    FOLLOW(T) = { +, *, ), \$ }"),
            Steps(
                "Linha do estado 1",
                listOf(
                    "E' → E • está completo e é o item aumentado → ACTION[1, \$] = **acc**",
                    "E → E • + T tem terminal depois do ponto, e há seta + → I6 → ACTION[1, +] = **s6**",
                    "Todo o resto da linha fica vazio = **erro**",
                ),
            ),
            Steps(
                "Linha do estado 2",
                listOf(
                    "E → T • completo → **r2** nas colunas de FOLLOW(E): +, ) e \$",
                    "T → T • * F com seta * → I7 → ACTION[2, *] = **s7**",
                    "Alguma célula com duas ações? Não: '*' ∉ FOLLOW(E). Linha limpa!",
                ),
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = "Confira as linhas 1 e 2 na tabela gerada pelo motor."),
            Say(BYTE, "Deixa eu tentar! Eu consigo, eu consigo..."),
            Reveal(
                "Sua vez: estado 9 = { E → E + T • ; T → T • * F } com seta * → I7. Preencha a linha.",
                "r1 em +, ) e \$ (FOLLOW(E)); s7 em *. Sem conflito, pelo mesmo motivo do estado 2.",
            ),
            Reveal(
                "Agora o estado 10 = { T → T * F • }.",
                "r3 em +, *, ) e \$ — todas as colunas de FOLLOW(T).",
            ),
            Say(PROF, "Último, sem rodinhas: a gramática dos ponteiros."),
            Reveal(
                "S → L = R | R; L → * R | id; R → L. Estado 2 = { S → L • = R ; R → L • }, seta = → I6, FOLLOW(R) = { =, \$ }. Preencha a linha.",
                "ACTION[2, =] = { s6, r5 } → CONFLITO empilhar/reduzir! ACTION[2, \$] = r5. Conclusão: a gramática não é SLR(1).",
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = "A célula [2, =] com duas ações confirma sua resposta."),
            Callout(EXAM_TIP, "Calcule e escreva FOLLOW de todos os não-terminais ANTES de abrir a tabela. Metade dos erros de prova é reduce na coluna errada."),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.SOCRATIC, "Por que o FOLLOW decide as reduções?",
        listOf(
            Say(PROF, "Hoje eu só pergunto. Responda na sua cabeça antes de tocar — errar aqui é treino, não nota."),
            Reveal("Um estado tem o item A → α •. O que esse ponto no fim está dizendo?", "Que o topo da pilha contém α inteiro: temos um candidato a handle. Dá para reduzir por A → α."),
            Reveal("O LR(0) puro reduz em QUALQUER próximo token. E se o mesmo estado tiver também B → β • a γ?", "No token a, ele quer reduzir E empilhar: conflito. O LR(0) é cego para o futuro."),
            Reveal("Se reduzimos para A, que tokens podem aparecer logo depois numa derivação válida?", "Só os de FOLLOW(A). Se o próximo token não está lá, reduzir para A é erro garantido."),
            Say(BYTE, "Então o SLR é o LR(0) com um filtro de FOLLOW nas reduções!"),
            Say(PROF, "Exatamente. Mesmo autômato, mesmos estados — só reduções mais educadas."),
            Predict(
                "E se o token a estiver em FOLLOW(A) e o estado também tiver B → β • a γ?",
                listOf("O SLR escolhe shift sozinho", "Conflito empilhar/reduzir: a gramática não é SLR(1)", "O SLR reduz pela regra mais curta"),
                1,
                "A tabela SLR não escolhe nada: se duas ações caem na mesma célula, a gramática simplesmente não é SLR(1). (Quem escolhe 'shift' por padrão é o Bison — outra história.)",
            ),
            Reveal("E dois itens completos A → α • e B → β • no mesmo estado?", "Se FOLLOW(A) ∩ FOLLOW(B) ≠ ∅, conflito reduzir/reduzir naquelas colunas."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_AMBIG, caption = "E → E + E | E * E | ( E ) | id: conflitos nos estados 7 e 8, colunas + e *."),
            Reveal("Toda gramática ambígua gera conflito na tabela SLR?", "Sim: nenhuma gramática ambígua é LR(k). Mas a volta é falsa — conflito no SLR não prova ambiguidade (lembra de S → L = R | R?)."),
            Reveal("Por que o FOLLOW é grosseiro demais?", "Ele junta tudo que pode seguir A em QUALQUER lugar da gramática, sem saber de qual caminho você veio. Recuperar esse contexto é o trabalho do LR(1)."),
            Callout(BRAIN_POWER, "Pense: como você carregaria, dentro de cada item, a informação 'o que pode vir depois DESTE A, neste caminho'? Segure essa ideia até a aula de LALR."),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.FORMAL, "Construção da tabela SLR(1): algoritmo",
        listOf(
            Text("Seja G' a gramática aumentada (S' → S) e C = { I0, ..., In } a coleção canônica de conjuntos de itens LR(0) de G'. O estado i corresponde a Ii; o inicial é closure({ [S' → • S] })."),
            Code(
                """para cada estado Ii:
  para cada item [A → α • a β] em Ii, com a terminal:
      se GOTO(Ii, a) = Ij:  ACTION[i, a] := shift j
  para cada item [A → α •] em Ii, com A ≠ S':
      para todo a em FOLLOW(A):  ACTION[i, a] := reduce A → α
  se [S' → S •] ∈ Ii:  ACTION[i, $] := accept
  para cada não-terminal A:
      se GOTO(Ii, A) = Ij:  GOTO[i, A] := j
células não preenchidas := erro""",
                "Algoritmo SLR(1) (Livro do Dragão, Alg. 4.46)",
            ),
            Text("**Definição.** G é SLR(1) se e somente se nenhuma célula de ACTION recebe mais de uma ação."),
            Steps(
                "Os dois tipos de conflito",
                listOf(
                    "**Empilhar/reduzir:** Ii contém [A → α • a β] e [B → γ •] com a ∈ FOLLOW(B)",
                    "**Reduzir/reduzir:** Ii contém [A → α •] e [B → β •] com FOLLOW(A) ∩ FOLLOW(B) ≠ ∅",
                ),
            ),
            Code(
                """empilha o estado 0;  a := primeiro token
repita:
  s := estado no topo
  caso ACTION[s, a]:
    shift t:       empilha t;  a := próximo token
    reduce A → β:  desempilha |β| estados;  t := topo
                   empilha GOTO[t, A];  emite A → β
    accept:        pare com sucesso
    erro:          chame a recuperação de erro""",
                "Algoritmo do analisador LR (o mesmo para SLR, LALR e LR(1) — só a tabela muda)",
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_EXPR, caption = "Tabela SLR(1) da gramática de expressões: sem conflitos."),
            Predict(
                "Qual afirmação é VERDADEIRA (sobre classes de gramáticas)?",
                listOf("Toda gramática SLR(1) é LL(1)", "Toda gramática SLR(1) é LALR(1)", "Toda gramática LALR(1) é SLR(1)"),
                1,
                "Hierarquia: LR(0) ⊂ SLR(1) ⊂ LALR(1) ⊂ LR(1). LL(1) e SLR(1) são incomparáveis (E → E + T é SLR mas não LL).",
            ),
            Reveal("Dimensões da tabela?", "Uma linha por estado LR(0). ACTION: uma coluna por terminal + \$. GOTO: uma coluna por não-terminal (sem S')."),
            Callout(WATCH_OUT, "Transição com terminal vira 'shift' em ACTION; transição com não-terminal vira número em GOTO. Não existe 'shift' na GOTO nem número puro na ACTION."),
            Callout(EXAM_TIP, "Para justificar um conflito, cite: estado, terminal, os dois itens e o FOLLOW. Ex.: 'I2, coluna =: s6 por S → L • = R e r5 por R → L •, pois = ∈ FOLLOW(R)'."),
        ),
    ),
    Lesson(
        Skill.SLR_TABLE, Style.MNEMONIC, "O porteiro FOLLOW da torre SLR",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Fortaleza Bottom-Up, torre do SLR. Na porta, um porteiro de prancheta chamado FOLLOW."),
            Callout(MNEMONIC, "SLR = **'Só Libera Redução'** se o próximo token estiver na prancheta do FOLLOW."),
            Code(
                """• antes de TERMINAL         →  sN   (empilha e vai para N)
• no FIM do item            →  rN   (só nas colunas do FOLLOW)
S' → S •                    →  acc  (só na coluna $)
seta com NÃO-terminal       →  GOTO (número puro)""",
                "Cola de bolso",
            ),
            Callout(MNEMONIC, "Ponto na frente do terminal: **'tem estrada, segue'** (shift). Ponto no fim: **'chegou, fecha'** (reduce)."),
            Callout(MNEMONIC, "**E/R** = um item quer andar e outro quer fechar (uma porta, dois comandos). **R/R** = dois itens querem fechar ao mesmo tempo (dois nomes para o mesmo pacote)."),
            Say(LEXI, "Eu corto tokens; o porteiro só deixa reduzir quem ele conhece. Equipe!"),
            Predict(
                "No estado { S → L • = R ; R → L • } com '=' ∈ FOLLOW(R), o porteiro...",
                listOf("Deixa só empilhar", "Libera a redução E a seta mostra empilhar: conflito E/R", "Não deixa nada"),
                1,
                "O porteiro olha só a prancheta global (FOLLOW). Ele não sabe que, ali, um R nunca é seguido de '='. Míope!",
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_LIST, caption = "S → S a | b: tabela pequena e sem conflitos. Recursão à esquerda não assusta o LR!"),
            Callout(FIRESIDE, "LR(0): 'Eu reduzo sempre que vejo um item completo, sem olhar ninguém!' SLR: 'E por isso briga com todo mundo. Eu pergunto ao FOLLOW antes.' LALR (lá do fundo): 'Vocês dois ainda vão me pedir ajuda...'"),
            Reveal("Recite as quatro regras da tabela SLR.", "Terminal após o ponto → shift. Não-terminal → GOTO. Item completo → reduce nas colunas do FOLLOW. S' → S • → accept em \$."),
            Callout(MNEMONIC, "Contraste: no LL(1), o FOLLOW só entra nas produções que derivam ε. No SLR, o FOLLOW decide TODA redução."),
        ),
    ),
)

// =====================================================================
// LALR — LR(1) e LALR(1)
// =====================================================================

internal val lalrLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.LALR, Style.STORY, "Ônibus com destino na testa",
        listOf(
            Say(BYTE, "O SLR travou naquela gramática dos ponteiros. Joga fora e pronto?"),
            Say(PROF, "Não! Vamos dar ao analisador uma informação extra. Pense numa empresa de ônibus."),
            Text("Cada **item** é um passageiro numa **linha** (a produção com o ponto). O SLR desce passageiros em qualquer ponto que **alguma** linha da cidade use — é o FOLLOW global."),
            Text("O **LR(1)** cola na testa de cada passageiro o **ponto exato** onde ele vai descer: o **lookahead**. Item LR(1) = [linha, destino], ex.: [R → L •, \$]."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Linha do ônibus → **núcleo** do item (A → α • β)",
                    "Ponto de descida na testa → **lookahead** (um terminal)",
                    "Descer do ônibus → **reduzir**, só quando o próximo token é o lookahead",
                    "Frota enorme, um ônibus por combinação linha+destinos → **LR(1) canônico** (muitos estados)",
                    "Fundir ônibus com as mesmas linhas, somando destinos → **LALR(1)**",
                ),
            ),
            Say(PILHA, "Mais ônibus... mais estados... mais memória. A empresa... reclamou."),
            Text("Então a empresa funde ônibus que fazem exatamente as **mesmas linhas** (mesmo núcleo) e junta as listas de destinos. A frota volta ao tamanho do LR(0)/SLR!"),
            Predict(
                "Ônibus 1: passageiro A desce em d, B desce em e. Ônibus 2 (mesmas linhas): A desce em e, B desce em d. Fundindo os dois, o que acontece no ponto d?",
                listOf("Nada, só A desce", "A e B querem descer: confusão (reduzir/reduzir)", "Os dois continuam no ônibus"),
                1,
                "Fundidos, A e B têm d e e na testa. No ponto d, dois 'reduce' disputam a célula: conflito reduzir/reduzir que o LR(1) não tinha.",
            ),
            Callout(BRAIN_POWER, "A fusão nunca cria conflito empilhar/reduzir novo: quem 'segue viagem' (shift) depende só da linha, e as linhas são as mesmas. Só a parte 'descer' (lookahead) é somada."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_PTR, caption = "O SLR trava em [2, =]. No LR(1), o estado 2 tem [R → L •, \$]: só desce em \$, então '=' fica só com o shift."),
            Reveal("Na analogia, o que é o 'núcleo' de um estado?", "O conjunto de linhas (itens sem o lookahead). Estados LR(1) com o mesmo núcleo viram um estado só no LALR."),
            Callout(REAL_WORLD, "O Yacc e o Bison geram, por padrão, tabelas LALR(1): quase o poder do LR(1) com o tamanho do SLR. O Bison moderno também oferece IELR e LR(1) canônico via %define lr.type."),
        ),
    ),
    Lesson(
        Skill.LALR, Style.VISUAL, "Vendo a fusão de núcleos acontecer",
        listOf(
            Say(PROF, "Mesma gramática dos ponteiros, agora com lookaheads. Primeiro, o esqueleto LR(0)."),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_PTR, caption = "10 estados LR(0). O LALR terá exatamente estes 10 núcleos."),
            Code(
                """I0: S' → • S, $      S → • L = R, $      S → • R, $
    L → • * R, =/$    L → • id, =/$       R → • L, $
I2: S → L • = R, $    R → L •, $          ← reduz só em $!""",
                "Itens LR(1): [núcleo, lookahead]. '=/\$' abrevia dois itens.",
            ),
            Say(BYTE, "No estado 2, R → L • só tem \$ na testa. Então em '=' é só empilhar!"),
            Say(PROF, "Isso. O lookahead do item veio do contexto: R → • L foi criado a partir de S → • R, \$ — e depois de S só vem \$."),
            Code(
                """LR(1) canônico: 14 estados
  I4 ~ I11   (L → * • R, ...)
  I5 ~ I12   (L → id •, ...)
  I7 ~ I9    (R → L •, ...)
  I8 ~ I13   (L → * R •, ...)
LALR(1): funde cada par → 10 estados""",
                "Pares com o mesmo núcleo (numeração do motor do jogo)",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_PTR, input = "*x=y", caption = "O Bison (LALR) aceita '*x = y' sem nenhum conflito — onde o SLR travava."),
            Say(PROF, "Agora o lado sombrio da fusão."),
            Code(
                """S → a A d | b B d | a B e | b A e
A → c        B → c

LR(1):  I6 = { [A → c •, d]  [B → c •, e] }   ← depois de 'a c'
        I9 = { [B → c •, d]  [A → c •, e] }   ← depois de 'b c'
LALR:   I6+I9 = { [A → c •, d/e]  [B → c •, d/e] }  ← R/R em d e em e!""",
                "Gramática LR(1) mas não LALR(1)",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_RR, input = "a c d", caption = "O Bison avisa 2 conflitos reduzir/reduzir e escolhe a regra que aparece primeiro (x : c)."),
            Predict(
                "Com essa escolha padrão do Bison, o que acontece com a frase válida 'a c e'?",
                listOf("É aceita", "Dá syntax error, mesmo sendo válida", "O Bison pergunta ao usuário"),
                1,
                "Em 'e', ele reduz c para x (primeira regra), mas 'a x e' não existe — só 'a y e'. A fusão fez o parser perder uma frase da linguagem.",
            ),
            Callout(BRAIN_POWER, "Note: o LR(1) separava 'a c' de 'b c' em estados diferentes. A fusão misturou os contextos — é exatamente aí que o LALR é mais fraco que o LR(1)."),
        ),
    ),
    Lesson(
        Skill.LALR, Style.WORKED, "Closure LR(1) e fusão, passo a passo",
        listOf(
            Say(PROF, "Vamos calcular I0 da gramática S → L = R | R; L → * R | id; R → L, com lookaheads."),
            Steps(
                "closure({ [S' → • S, \$] })",
                listOf(
                    "Começa com [S' → • S, \$]",
                    "Ponto antes de S; o que vem depois de S é ε, então o lookahead é FIRST(\$) = { \$ } → [S → • L = R, \$] e [S → • R, \$]",
                    "De [S → • L = R, \$]: depois de L vem '= R', FIRST = { = } → [L → • * R, =] e [L → • id, =]",
                    "De [S → • R, \$]: depois de R vem nada → FIRST(\$) → [R → • L, \$]",
                    "De [R → • L, \$]: → [L → • * R, \$] e [L → • id, \$]",
                    "Nada novo? Pronto. Compactando: [L → • * R, =/\$] e [L → • id, =/\$]",
                ),
            ),
            Callout(WATCH_OUT, "A regra é FIRST(β a), onde β é o que vem DEPOIS do não-terminal no item e a é o lookahead do item. Não é FOLLOW!"),
            Steps(
                "GOTO(I0, L) = I2",
                listOf(
                    "Avança o ponto sobre L: [S → L • = R, \$] e [R → L •, \$]",
                    "Closure não acrescenta nada (ponto antes de terminal ou no fim)",
                    "Tabela: '=' → shift; R → L • reduz SÓ em \$ → sem conflito!",
                ),
            ),
            Say(BYTE, "Agora eu! Agora eu!"),
            Reveal(
                "Calcule GOTO(I0, *).",
                "[L → * • R, =/\$] e, fechando: [R → • L, =/\$], [L → • * R, =/\$], [L → • id, =/\$]. Os lookaheads de L → * R são herdados por R → • L porque R está no fim.",
            ),
            Reveal(
                "No LR(1) há [L → id •, =/\$] (depois de 'id' no início) e [L → id •, \$] (depois de 'L = id'). Fundem no LALR? Dá conflito?",
                "Fundem (mesmo núcleo L → id •) virando [L → id •, =/\$]. Um único item completo → sem conflito.",
            ),
            Reveal(
                "Na gramática S → a A d | b B d | a B e | b A e; A → c; B → c, quais estados LR(1) se fundem e o que acontece?",
                "{ [A → c •, d], [B → c •, e] } e { [B → c •, d], [A → c •, e] } têm o mesmo núcleo. Fundidos: A → c • e B → c • ambos em d e em e → reduzir/reduzir.",
            ),
            Visual(VisualKind.SLR_TABLE, grammar = P3_RR, caption = "Curiosidade: o SLR também tem esse R/R (FOLLOW(A) = FOLLOW(B) = { d, e }). Só o LR(1) canônico escapa."),
            Callout(EXAM_TIP, "Questão típica: 'mostre que G é LR(1) mas não LALR(1)'. Receita: ache dois estados LR(1) com o mesmo núcleo cujos itens completos trocam de lookahead, funda-os e aponte a célula com dois reduces."),
        ),
    ),
    Lesson(
        Skill.LALR, Style.SOCRATIC, "E se o item soubesse o futuro?",
        listOf(
            Say(PROF, "Lembra da pergunta da última aula? Como carregar, dentro do item, 'o que vem depois deste A, neste caminho'?"),
            Reveal("Qual a ideia mais simples?", "Grudar um terminal no item: [A → α • β, a] significa 'quando α β estiver completo, só reduza se o próximo token for a'."),
            Reveal("De onde vem o lookahead dos itens novos na closure?", "Do item que os criou: em [A → α • B β, a], cada B → • γ ganha lookaheads FIRST(β a). É o contexto local, não o FOLLOW global."),
            Reveal("Por que isso resolve S → L = R | R?", "No estado 2, R → L • veio de S → • R, \$, então só reduz em \$. O '=' fica só com o shift."),
            Reveal("Qual o preço?", "Muitos estados: o mesmo núcleo aparece repetido com lookaheads diferentes. Para linguagens reais, o LR(1) canônico pode ter milhares de estados a mais."),
            Say(BYTE, "E se a gente juntasse os estados que só diferem no lookahead?"),
            Say(PROF, "Você acabou de inventar o LALR(1). Os nomes que você não sabia já estavam na sua cabeça."),
            Predict(
                "A fusão de estados com mesmo núcleo pode criar um conflito EMPILHAR/REDUZIR novo?",
                listOf("Sim, sempre", "Não: se surgisse depois, ele já existia em um dos estados originais", "Só se a gramática for ambígua"),
                1,
                "Shifts dependem só do núcleo. Se [A → α •, a] e [B → β • a γ] estão no fundido, algum estado original tinha [A → α •, a] e — por ter o mesmo núcleo — também [B → β • a γ, ...]. O conflito já estava lá.",
            ),
            Reveal("E reduzir/reduzir novo?", "Pode! Dois estados sem conflito, com A e B completos trocando lookaheads (d/e e e/d), viram um estado com dois reduces em d e em e."),
            Reveal("Então, por que o Bison usa LALR e não LR(1)?", "Porque o LALR tem o número de estados do SLR e quase sempre basta para linguagens de programação. O raro R/R da fusão vale a economia."),
            Callout(FIRESIDE, "SLR: 'Eu uso o FOLLOW e pronto.' LALR: 'Eu uso lookaheads de verdade, só que fundidos — mais esperto que você e do mesmo tamanho.' LR(1): 'E eu nunca fundo nada. Mais forte que os dois... e bem mais gordo.'"),
        ),
    ),
    Lesson(
        Skill.LALR, Style.FORMAL, "Itens LR(1), closure e fusão: definições",
        listOf(
            Text("**Item LR(1):** par [A → α • β, a] com A → α β ∈ P e a ∈ T ∪ { \$ }. O primeiro componente é o **núcleo** (core); a é o **lookahead**. O lookahead só tem efeito quando β = ε."),
            Code(
                """CLOSURE(I):
  repita
    para cada [A → α • B β, a] em I
      para cada produção B → γ
        para cada b em FIRST(β a)
          acrescente [B → • γ, b] a I
  até não mudar
  retorne I

GOTO(I, X) := CLOSURE({ [A → α X • β, a] | [A → α • X β, a] ∈ I })

estado inicial := CLOSURE({ [S' → • S, $] })""",
                "Construção da coleção canônica LR(1)",
            ),
            Text("**Tabela LR(1):** igual à SLR, exceto que [A → α •, a] gera reduce **apenas** em ACTION[i, a] (não em todo o FOLLOW(A))."),
            Code(
                """LALR(1):
  1. construa a coleção LR(1) C = { I0, ..., In }
  2. agrupe os conjuntos com o mesmo núcleo;
     substitua cada grupo pela UNIÃO dos seus itens
  3. monte ACTION como no LR(1); se houver conflito,
     a gramática não é LALR(1)
  4. GOTO(J, X) := estado que contém GOTO(Ik, X)
     (bem definido: mesmos núcleos → mesmos destinos)""",
                "Construção do LALR(1) por fusão",
            ),
            Steps(
                "Propriedades que caem em prova",
                listOf(
                    "Nº de estados LALR(1) = nº de estados LR(0) = nº de estados SLR(1)",
                    "Fusão nunca introduz conflito empilhar/reduzir; pode introduzir reduzir/reduzir",
                    "Gramáticas: SLR(1) ⊂ LALR(1) ⊂ LR(1); nenhuma gramática ambígua é LR(1)",
                    "Lookaheads LALR ⊆ FOLLOW: por isso todo SLR(1) é LALR(1)",
                ),
            ),
            Predict(
                "Em [A → α • B β, a], quais lookaheads ganham os itens de B?",
                listOf("FOLLOW(B)", "FIRST(β a)", "FIRST(B)"),
                1,
                "FIRST(β a): se β não deriva ε, o a nem aparece; se deriva, o lookahead do pai 'atravessa'. FOLLOW(B) seria voltar ao SLR.",
            ),
            Visual(VisualKind.LR0_AUTOMATON, grammar = P3_RR, caption = "Os núcleos LR(0) de S → a A d | b B d | a B e | b A e. O estado com A → c • e B → c • é o fundido no LALR."),
            Callout(EXAM_TIP, "Escreva itens LR(1) agrupados por núcleo, ex. [L → • id, =/\$]. É mais rápido e deixa a fusão visível. Mas lembre: formalmente são dois itens."),
            Callout(WATCH_OUT, "Não confunda 'LALR = Look-Ahead LR' com 'LR com lookahead de 2 tokens'. O (1) continua: um token de lookahead."),
        ),
    ),
    Lesson(
        Skill.LALR, Style.MNEMONIC, "O alfaiate que funde gêmeos",
        listOf(
            Say(NARRADOR, "Palácio da Memória: o andar mais alto da Fortaleza Bottom-Up. Um alfaiate costura gêmeos idênticos num só, somando as roupas dos dois."),
            Callout(MNEMONIC, "**Mesmo esqueleto, roupas somadas.** Estados com o mesmo núcleo (esqueleto) viram um; os lookaheads (roupas) são unidos."),
            Callout(MNEMONIC, "Óculos: **LR(0)** = sem óculos. **SLR** = binóculo embaçado (FOLLOW global). **LR(1)** = lupa em cada item. **LALR** = lupa, mas costurada em pares."),
            Callout(MNEMONIC, "**'Fusão só faz Reduce duplo'**: a fusão LALR nunca cria E/R novo; só R/R."),
            Code(
                """força:    LR(0) < SLR(1) < LALR(1) < LR(1)
tamanho:  LR(0) = SLR(1) = LALR(1) < LR(1)""",
                "Duas linhas para tatuar",
            ),
            Say(BYTE, "Então o LALR é o 'custo-benefício' da família?"),
            Say(PROF, "Isso! Por isso o Yacc e o Bison escolheram ele como padrão."),
            Predict(
                "O alfaiate fundiu dois gêmeos sem brigas. O resultado tem um conflito empilhar/reduzir. Isso é possível?",
                listOf("Sim, a fusão criou", "Não: se tem E/R, um dos gêmeos já tinha", "Só em gramática ambígua"),
                1,
                "Shift depende só do esqueleto. Se o fundido tem E/R, o gêmeo que trouxe aquele reduce já tinha o mesmo shift.",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_PTR, input = "x=*y", caption = "O bisão (LALR) engole a gramática dos ponteiros sem reclamar."),
            Reveal("Qual o exemplo-gancho de 'LALR mas não SLR'? E de 'LR(1) mas não LALR'?", "S → L = R | R; L → * R | id; R → L. E: S → a A d | b B d | a B e | b A e com A → c, B → c."),
            Callout(FIRESIDE, "SLR vs LALR, ao pé da lareira. SLR: 'Eu sou simples, todo mundo me entende.' LALR: 'E eu sou do seu tamanho, mas sei de onde cada item veio. Na gramática dos ponteiros, você travou e eu não.'"),
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
