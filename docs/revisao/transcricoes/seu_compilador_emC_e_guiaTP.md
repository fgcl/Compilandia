# Série "Seu compilador" em C (aulas animadas EM_C, na ordem)

#### EM_C / TOKENS: "Em C 1: o laço do getchar" (10 cenas, ~64s)

1. TELA: # Em C aqui, Java no TP ⏎ série: C, com árvore ⏎ Guia do TP: Java, sem árvore ⏎ uma função por regra
   PROF: "Esta série escreve o compilador em C e monta a árvore. O Guia do TP faz o trabalho em Java, sem árvore. A ideia é a mesma: uma função por regra. O guia traz a tabela dos nomes."

2. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7
   BYTE: "Vamos escrever o analisador léxico em C, na mão."

3. TELA: # lexer.c ⏎ *int c = getchar();* ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '4'; v = ?}
   BYTE: "O getchar lê um caractere por vez. Chegou o 4."

4. TELA: # lexer.c ⏎ int c = getchar(); ⏎ *if (isdigit(c)) {* ⏎ *  int v = 0;* ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '4'; v = 0}
   BYTE: "É dígito. Entra no if, e o valor começa em zero."

5. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎ *    v = v * 10 + (c - '0');* ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '4'; v = 4}
   BYTE: "Cada volta do while junta um dígito. Agora v vale quatro."

6. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎ *    v = v * 10 + (c - '0');* ⏎ *    c = getchar();* ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '2'; v = 42}
   BYTE: "Lê o 2. Quatro vezes dez, mais dois, dá quarenta e dois."

7. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '+'; v = 42}
   BYTE: "Agora chegou o mais. Qual linha roda logo depois? Toque nela."
   PERGUNTA (toque): certo =   ungetc(c, stdin); ; errado =     v = v * 10 + (c - '0'); |     c = getchar(); | if (isdigit(c)) {
   se errar: "O mais não é dígito. O while para."
   se acertar: "O laço para e o ungetc devolve o mais."

8. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎ *  ungetc(c, stdin);* ⏎   return (Token){NUM, v}; ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '+'; v = 42}
   BYTE: "O mais foi lido a mais. O ungetc devolve ele para a entrada."

9. TELA: # lexer.c ⏎ int c = getchar(); ⏎ if (isdigit(c)) { ⏎   int v = 0; ⏎   while (isdigit(c)) { ⏎     v = v * 10 + (c - '0'); ⏎     c = getchar(); ⏎   } ⏎   ungetc(c, stdin); ⏎ *  return (Token){NUM, v};* ⏎ } ⏎ 4 ⏎ 2 ⏎ + ⏎ 7 ⏎ variáveis{c = '+'; v = 42} ⏎ Token{tipo: NUM; valor: 42}
   BYTE: "E sai o token. Ele é um struct com o tipo e o valor."

10. TELA: *x* ⏎ 1 ⏎ = ⏎ 4 ⏎ 2 ⏎ + ⏎ y ⏎ ;
   BYTE: "Agora corte uma linha inteira, como o seu lexer em C faria."
   BRINQUEDO: dica "Maior casamento: estique o pedaço enquanto ele ainda for o mesmo token." / acerto "Os tokens que você cortou são os que o seu lexer devolveria."

#### EM_C / RECURSIVE_DESCENT: "Em C 2: uma função por regra" (9 cenas, ~51s)

1. TELA: # parser.c ⏎ void expr(void) { ⏎   termo(); ⏎   while (tok == '+') { ⏎     casa('+'); ⏎     termo(); ⏎   } ⏎ }
   BYTE: "Na descida recursiva em C, cada regra da gramática vira uma função."

2. TELA: # parser.c ⏎ void expr(void) { ⏎ *  termo();* ⏎ *  while (tok == '+') {* ⏎     casa('+'); ⏎     termo(); ⏎   } ⏎ }
   BYTE: "expr chama termo. O while repete enquanto vier um mais."

3. TELA: # parser.c ⏎ void expr(void) { ⏎   termo(); ⏎   while (tok == '+') { ⏎     casa('+'); ⏎     termo(); ⏎   } ⏎ } ⏎ void termo(void) { ⏎ *  fator(); while (tok == '*') ...* ⏎ } ⏎ *void fator(void) { casa(NUM); }*
   BYTE: "termo é igual, com o vezes. E fator só casa um número."

4. TELA: # parser.c ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM ⏎ main() ⏎ expr() ⏎ 2 ⏎ + ⏎ 3 ⏎ * ⏎ 4
   BYTE: "Vamos rodar com dois mais três vezes quatro. O main chama expr."

5. TELA: # parser.c ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM ⏎ main() ⏎ expr() ⏎ termo() ⏎ fator() ⏎ 2 ⏎ + ⏎ 3 ⏎ * ⏎ 4
   BYTE: "expr chama termo, e termo chama fator. A pilha cresce."

6. TELA: # parser.c ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM ⏎ main() ⏎ expr() ⏎ 2 ⏎ + ⏎ 3 ⏎ * ⏎ 4
   BYTE: "fator casa o 2 e volta. Depois termo volta também."

7. TELA: # parser.c ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM ⏎ main() ⏎ expr() ⏎ 2 ⏎ + ⏎ 3 ⏎ * ⏎ 4
   BYTE: "Agora vem o mais. Qual função está no topo da pilha na hora de casar? Toque."
   PERGUNTA (toque): certo = expr() ; errado = main()
   se errar: "Quem tem o while do mais?"
   se acertar: "O while do mais mora dentro de expr."

8. TELA: # parser.c ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM ⏎ main() ⏎ expr() ⏎ termo() ⏎ 2 ⏎ + ⏎ 3 ⏎ * ⏎ 4
   PROF: "No vezes, quem casa é termo. Cada função cuida do seu operador."

9. TELA: *1* ⏎ * ⏎ 2 ⏎ + ⏎ 3 ⏎ *main()* ⏎ pilha de chamadas ⏎ expr:  termo ('+' termo)* ⏎ termo: fator ('*' fator)* ⏎ fator: NUM
   BYTE: "Agora você é o processador. Chame, case e volte, na ordem certa."
   BRINQUEDO: dica "Chame quando o topo precisa de outra regra. Case quando o token bate. Volte quando a função acabou." / acerto "Você rodou a descida recursiva inteira, igual ao programa em C."

#### EM_C / AST_BUILD: "Em C 3: o struct Node" (8 cenas, ~43s)

1. TELA: # struct Node ⏎ typedef struct Node { ⏎   char op; ⏎   int val; ⏎   struct Node *esq, *dir; ⏎ } Node;
   BYTE: "Na AST em C, cada nó da árvore é um struct."

2. TELA: # struct Node ⏎ typedef struct Node { ⏎ *  char op;* ⏎ *  int val;* ⏎   struct Node *esq, *dir; ⏎ } Node;
   BYTE: "op guarda o operador. val guarda o número."

3. TELA: # struct Node ⏎ typedef struct Node { ⏎   char op; ⏎   int val; ⏎ *  struct Node *esq, *dir;* ⏎ } Node;
   BYTE: "esq e dir são ponteiros. Eles apontam para os filhos."

4. TELA: # 2 + 3 * 4 ⏎ Node{val: 2} ⏎ Node{val: 3} ⏎ Node{val: 4}
   BYTE: "Para dois mais três vezes quatro, o parser cria os nós de baixo para cima."

5. TELA: # 2 + 3 * 4 ⏎ Node{val: 2} ⏎ Node{val: 3} ⏎ Node{val: 4} ⏎ *Node{op: '*'; esq ●; dir ●}*
   BYTE: "Primeiro as folhas, um nó para cada número. Depois vem o vezes."

6. TELA: # 2 + 3 * 4 ⏎ Node{val: 2} ⏎ Node{val: 3} ⏎ Node{val: 4} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ *Node{op: '+'; esq ●; dir ●}*
   BYTE: "Por último o mais, que vira a raiz da árvore."

7. TELA: # 2 + 3 * 4 ⏎ Node{val: 2} ⏎ Node{val: 3} ⏎ Node{val: 4} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{op: '+'; esq ●; dir ●}
   BYTE: "Para qual nó aponta o ponteiro dir da raiz? Toque nele."
   PERGUNTA (toque): certo = Node{op: '*'; esq ●; dir ●} ; errado = Node{val: 2} | Node{val: 3} | Node{val: 4}
   se errar: "Siga o fio que sai da linha dir."
   se acertar: "O dir da raiz aponta para o vezes."

8. TELA: # 2 + 3 * 4 ⏎ Node{val: 2} ⏎ Node{val: 3} ⏎ Node{val: 4} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{op: '+'; esq ●; dir ●}
   PROF: "A árvore é isso: structs ligados por ponteiros."

#### EM_C / CODEGEN_MIPS: "Em C 4: da árvore ao MIPS" (7 cenas, ~42s)

1. TELA: # a + b * c ⏎ Node{op: '+'; esq ●; dir ●} ⏎ Node{nome: a} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{nome: b} ⏎ Node{nome: c}
   BYTE: "O gerador anda na árvore e escreve MIPS, uma linha por vez."

2. TELA: # a + b * c ⏎ Node{op: '+'; esq ●; dir ●} ⏎ Node{nome: a} ⏎ *Node{op: '*'; esq ●; dir ●}* ⏎ Node{nome: b} ⏎ Node{nome: c}
   BYTE: "Ele visita os filhos antes do pai. O primeiro nó pronto é o vezes."

3. TELA: # a + b * c ⏎ Node{op: '+'; esq ●; dir ●} ⏎ Node{nome: a} ⏎ *Node{op: '*'; esq ●; dir ●}* ⏎ Node{nome: b} ⏎ Node{nome: c} ⏎ *lw $t1, b* ⏎ *lw $t2, c* ⏎ *mul $t3, $t1, $t2* ⏎ *sw $t3, t1*
   BYTE: "O vezes vira quatro linhas: carrega b, carrega c, multiplica e guarda."

4. TELA: # a + b * c ⏎ Node{op: '+'; esq ●; dir ●} ⏎ Node{nome: a} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{nome: b} ⏎ Node{nome: c} ⏎ lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1
   BYTE: "Qual nó o gerador visita agora? Toque nele."
   PERGUNTA (toque): certo = Node{op: '+'; esq ●; dir ●} ; errado = Node{nome: a} | Node{nome: b} | Node{nome: c}
   se errar: "Os dois filhos da raiz já estão prontos."
   se acertar: "Agora é a vez da raiz."

5. TELA: # a + b * c ⏎ *Node{op: '+'; esq ●; dir ●}* ⏎ Node{nome: a} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{nome: b} ⏎ Node{nome: c} ⏎ lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1 ⏎ *lw $t1, a* ⏎ *lw $t2, t1* ⏎ *add $t3, $t1, $t2* ⏎ *sw $t3, t2*
   BYTE: "O mais usa o resultado do vezes, que já está guardado na memória."

6. TELA: # a + b * c ⏎ Node{op: '+'; esq ●; dir ●} ⏎ Node{nome: a} ⏎ Node{op: '*'; esq ●; dir ●} ⏎ Node{nome: b} ⏎ Node{nome: c} ⏎ lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1 ⏎ lw $t1, a ⏎ lw $t2, t1 ⏎ add $t3, $t1, $t2 ⏎ sw $t3, t2 ⏎ void gera(Node *n) { ⏎ *  if (n->op) { gera(n->esq); gera(n->dir); }* ⏎   emite(n); ⏎ }
   PROF: "Em C, é uma função recursiva: gera os filhos e depois o pai."

7. TELA: a + b * c ⏎ próxima instrução
   BYTE: "Agora monte o MIPS na ordem certa, instrução por instrução."
   BRINQUEDO: dica "Antes de calcular, os dois valores precisam estar nos registradores." / acerto "É exatamente o que o seu gerador em C escreveria."

#### EM_C / YACC_BISON: "Em C 5: flex, bison e o main" (7 cenas, ~36s)

1. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }}
   BYTE: "Um compilador em C de verdade costuma ter três arquivos."

2. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }}
   BYTE: "O main chama o yyparse, o parser que o Bison escreveu."

3. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }}
   BYTE: "Sempre que precisa de um token, o parser chama o yylex."

4. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }}
   BYTE: "O yylex devolve o tipo. O valor vai junto, em yylval."

5. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }}
   BYTE: "Quem chama o yylex? Toque no arquivo."
   PERGUNTA (toque): certo = calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ; errado = calc.l{[0-9]+  NUM; yylval = atoi(yytext)} | main.c{int main(void) {;   return yyparse();; }}
   se errar: "Quem pede tokens é o parser."
   se acertar: "O parser pede um token por vez."

6. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }} ⏎ *terminal{bison -d calc.y; flex calc.l; gcc *.c -o calc}*
   BYTE: "O Bison e o Flex escrevem arquivos C. O gcc junta tudo num programa."

7. TELA: # flex + bison + C ⏎ calc.l{[0-9]+  NUM; yylval = atoi(yytext)} ⏎ calc.y{%left '+'; %left '*'; e: e '+' e { $$ = $1 + $3; };  | e '*' e { $$ = $1 * $3; };  | NUM ;} ⏎ main.c{int main(void) {;   return yyparse();; }} ⏎ terminal{bison -d calc.y; flex calc.l; gcc *.c -o calc} ⏎ *./calc{2+3*4; = 14}*
   BYTE: "Roda o calc, digita a conta, e sai o resultado."


# Guia do Trabalho Prático (capítulos de texto)

## Capítulo 1: Visão geral e planejamento [PHASES]
O TP é um compilador inteiro, mas são cinco programinhas que conversam. Planeje antes de codar.

- NARRADOR: Semana 3 do semestre. O enunciado cai no Canvas: 'Construir um compilador completo para uma linguagem imperativa simples, em Java, gerando assembly MIPS executável no MARS'. Você lê duas vezes. Depois lê de novo.
- BYTE: Um compilador INTEIRO? Eu mal faço um for em Java sem olhar a internet…
- PROF: Respira. Ninguém escreve um compilador de uma vez. Você vai escrever **cinco pedaços pequenos** que conversam entre si, e cada um você já treinou aqui no Compilândia.
- WIDGET PIPELINE legenda: Cada estação da esteira vira uma classe Java no seu TP.
- PASSOS (As peças do TP (e o arquivo Java de cada uma)):
    · **Léxico** (`Lexico.java`): texto → tokens. Um AFD escrito com `switch` de estados.
    · **Sintático** (`Sintatico.java`): tokens → 'o programa está bem formado?'. Descida recursiva: um método por não-terminal.
    · **Semântico** (`TabelaSimbolos.java` + checagens dentro do `Sintatico`): declarou? o tipo bate?
    · **Gerador** (também dentro do `Sintatico`): cada método emite o seu pedaço de assembly MIPS.
    · **Otimização** (opcional/bônus): dobrar constantes, cortar código morto.
    · **Testes**: uma pasta de programas que DEVEM compilar e outra de programas que DEVEM dar erro.
- TEXTO: Repare num detalhe: semântico e gerador moram **dentro** do sintático. É o esquema de **tradução dirigida pela sintaxe em uma passada**: o parser pede um token ao léxico quando precisa, confere a semântica na hora e já cospe o MIPS. Sem árvore gigante na memória, sem segunda passada. É o formato que cabe num TP de semestre.
- CAIXA [Não existem perguntas idiotas]: **Por que duas linguagens?** Nas regiões, a série **Em C** escreve um compilador em C e monta a árvore (`struct Node`) antes de gerar o código. Este guia faz o TP em **Java** e não monta árvore: cada função do sintático confere e emite o MIPS na mesma passada. A ideia é uma só: **uma função por regra da gramática**. Para o TP, siga este guia. Use a série para ver a árvore por dentro. A tabela abaixo traduz os nomes.
- CÓDIGO (Os mesmos papéis, com nomes diferentes):
```
C (série Em C)  Java (Guia do TP)
expr()          exprAdd()
termo()         exprMul()
fator()         primario()
tok == '+'      eh(MAIS)
casa('+')       casaToken(MAIS)
struct Node     sem árvore
gera()          emite() na hora

MAIS = TipoToken.MAIS
```
- CÓDIGO (Estrutura de pastas sugerida):
```
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
```
- CÓDIGO (ErroCompilacao.java, TODO erro do compilador passa por aqui, sempre com a linha):
```
public class ErroCompilacao extends RuntimeException {
    public final int linha;

    public ErroCompilacao(int linha, String msg) {
        super("linha " + linha + ": " + msg);
        this.linha = linha;
    }
}
```
- CÓDIGO (Main.java: o maestro (Java 17)):
```
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
```
- TEXTO: **Rodando no primeiro dia.** O `Main` acima chama `Lexico` e `Sintatico`, que só nascem nos capítulos 3 e 5. Não espere: crie hoje as duas classes provisórias abaixo. Com elas, o projeto compila, lê o `.byt` e grava um `.asm` que o MARS roda. Daqui em diante, cada capítulo troca um pedaço e o resto continua rodando.
- CÓDIGO (Lexico.java e Sintatico.java provisórios: o projeto roda desde o dia 1):
```
// Provisório: o capítulo 3 troca pelo AFD.
public class Lexico {
    final String fonte;

    public Lexico(String fonte) { this.fonte = fonte; }
}

// Provisório: o capítulo 5 troca pela descida recursiva.
public class Sintatico {
    private final Lexico lexico;

    public Sintatico(Lexico lexico) { this.lexico = lexico; }

    public String compila() {
        long linhas = lexico.fonte.lines().count();
        return "# " + linhas + " linhas lidas\n"
             + ".text\n"
             + "main:\n"
             + "    li $v0, 10      # encerra o programa\n"
             + "    syscall\n";
    }
}
```
- CÓDIGO (Teste desta etapa: o comando e a saída esperada (abra o saida.asm no MARS: ele termina sem erro)):
```
# testes/ok/ola.byt (3 linhas):
programa ola {
    escreva("ola");
}

# no terminal:
javac -d out src/*.java
java -cp out Main testes/ok/ola.byt
compilado com sucesso -> saida.asm

# o saida.asm:
# 3 linhas lidas
.text
main:
    li $v0, 10      # encerra o programa
    syscall
```
- PREVEJA: Qual ordem de implementação dá MENOS dor de cabeça?
    opções: Escrever tudo de uma vez e testar na última semana | ✔Léxico → sintático → semântico → geração, testando cada fase antes de seguir | Começar pela geração de MIPS, que é o que vale mais ponto | Começar pela tabela de símbolos, que todo mundo usa
    explicação: Cada fase CONSOME a saída da anterior. Se o léxico tem bug, o parser parece quebrado sem estar. Construa de baixo para cima e teste cada camada antes de empilhar a próxima.
- PASSOS (Cronograma de 8 semanas (ajuste à data do seu enunciado)):
    · Semana 1: Definir a linguagem e escrever 10 programas-exemplo (5 certos, 5 com erro).
    · Semana 2: Léxico + um `TesteLexico` que imprime os tokens.
    · Semanas 3–4: Gramática LL(1) no papel (FIRST/FOLLOW!) e o parser descendente.
    · Semana 5: Tabela de símbolos e verificação de tipos.
    · Semanas 6–7: Geração de MIPS, testando cada comando no MARS.
    · Semana 8: Otimização, bateria de testes, relatório. E uma folga: algo SEMPRE atrasa.
- PASSOS (Só tem 3 semanas? Cronograma curto):
    · Semana 1: linguagem e léxico (capítulos 2 e 3). O esqueleto roda no dia 1, e o `TesteLexico` imprime os tokens no fim da semana.
    · Semana 2: gramática LL(1), parser, tabela de símbolos e tipos (capítulos 4 a 7). A tabela FIRST/FOLLOW do capítulo 4 já serve de prova para o relatório.
    · Semana 3: MIPS no MARS (capítulo 8), bateria de testes e o checklist (capítulos 9 e 10). A otimização fica de fora: é bônus.
- CÓDIGO (Git desde o dia 1: um branch por fase, commits pequenos):
```
git init byteiro && cd byteiro
git checkout -b lexico
# ... escreve, testa ...
git add src/Lexico.java testes/
git commit -m "lexico: reconhece <- e comentarios de bloco"
git checkout main
git merge lexico
```
- CAIXA [No mundo real]: Commit pequeno salva vida: quando a geração de código quebrar o parser às 23h, `git diff` mostra exatamente o que mudou. E o histórico de commits prova que o trabalho é da equipe.
- LEXI: Dupla? Um começa o léxico enquanto o outro escreve a gramática no papel. Trio? O terceiro escreve a bateria de testes desde o primeiro dia.
- PROF: Mas atenção: **todo mundo** precisa entender **tudo**. Na arguição, o professor aponta para qualquer método e pergunta 'por que isso está aqui?' para qualquer integrante.
- CAIXA [Cuidado!]: Leia o enunciado com marca-texto. Formato da mensagem de erro, nome do arquivo de saída, se para no primeiro erro, se palavras-chave ignoram maiúsculas… Alguns professores corrigem com scripts que comparam a saída caractere por caractere, um espaço a mais vira 'teste falhou'.
- CAIXA [Não existem perguntas idiotas]: P: Posso usar ANTLR, JavaCC ou outro gerador? R: Na maioria das turmas, NÃO, o objetivo é escrever léxico e parser à mão. Em alguns campi o léxico é feito com lex/flex (a Oficina Flex do app treina isso). Na dúvida, pergunte ao professor ANTES de começar.
- REVELAR: Em uma frase: o que o seu `Main` faz?
    → Lê o arquivo-fonte, entrega ao `Sintatico` (que puxa tokens do `Lexico`, consulta a `TabelaSimbolos` e emite MIPS) e grava o `.asm`: ou imprime a linha e a mensagem do primeiro erro.
- PILHA: Devagar… e sempre… uma fase… por vez.
- PASSOS (Na sua linguagem):
    · Qual é o nome da sua linguagem e a extensão dos arquivos-fonte?
    · Em que linguagem o compilador deve ser escrito, e o que ele deve gerar (MIPS para o MARS, ou outra coisa)?
    · Quantas semanas faltam para a entrega? Escolha o cronograma de 8 ou o de 3 semanas.

## Capítulo 2: Definindo a linguagem Byteiro [TOKENS]
Antes do Java: tokens, precedência e uma gramática informal de uma linguagem pequena e completa.

- PROF: Antes de uma linha de Java, você precisa de uma linguagem. No TP a turma inventa (ou recebe) a sua. Neste guia vamos inventar a **Byteiro**: pequenininha de propósito.
- BYTE: Posso pôr classes, lambdas, vetores, funções recursivas…?
- PROF: NÃO. Cada recurso a mais é um capítulo a mais de dor. **Pequena e completa ganha de grande e quebrada.** Funções e vetores ficam como 'trabalhos futuros' no relatório.
- CÓDIGO (contagem.byt: um programa Byteiro completo (vamos compilá-lo até o MIPS no capítulo 8)):
```
programa contagem {
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
}
```
- PASSOS (Ficha técnica da Byteiro):
    · **Tipos**: `inteiro` (32 bits), `logico` (`verdadeiro`/`falso`), `caractere` (`'A'`).
    · **Comandos**: declaração (`inteiro a, b;`), atribuição com `<-`, `se`/`senao`, `enquanto`, `escreva(...)`, `leia(x)`.
    · **Blocos** `{ }` — cada bloco abre um escopo novo. `se`, `senao` e `enquanto` EXIGEM bloco.
    · **Expressões**: aritmética `+ - * / %`, relacionais `= <> < > <= >=`, lógicas `e ou nao`.
    · **Comentários**: `// até o fim da linha` e `/* de bloco */`.
    · **Strings** só aparecem dentro do `escreva`: não existe tipo string.
- CÓDIGO (Tabela de tokens: vai direto para o relatório):
```
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
```
- CÓDIGO (Tabela de precedência da Byteiro):
```
Precedência (da MAIS FRACA para a MAIS FORTE)
1. ou                          associa à esquerda
2. e                           associa à esquerda
3. =  <>  <  >  <=  >=         NÃO associativo: a < b < c é erro
4. +  -                        associa à esquerda
5. *  /  %                     associa à esquerda
6. nao   - (unário)            prefixo
7. ( )  literais  variáveis
```
- PREVEJA: Na Byteiro, como é agrupado `a + b * 2 > 10 e ok`?
    opções: ((a + b) * 2) > (10 e ok) | ✔((a + (b * 2)) > 10) e ok | a + ((b * 2) > (10 e ok)) | Erro: misturar aritmética e lógica é proibido
    explicação: Quem está mais EMBAIXO na tabela gruda mais forte: primeiro `*`, depois `+`, depois `>`, e por último `e`. É exatamente a ordem que o parser vai seguir no capítulo 5.
- CAIXA [Cuidado!]: Por que `<-` para atribuir? Assim `=` fica livre para COMPARAR e ninguém escreve `se (x = 1)` achando que atribui. Mas tem pegadinha: `a<-1` agora é `a <- 1` (atribuição), e não `a < -1`. É a regra do **maior lexema**: o léxico sempre pega o token mais comprido possível. Documente isso no relatório!
- WIDGET TOKENIZE entrada=«if (x<-1) y = 0; // C de verdade» legenda: O Lexi do app conhece C: em C, `x<-1` é `x < -1`. Na Byteiro o MESMO texto vira `x <- 1`. Quem define os tokens é a especificação da linguagem.
- CÓDIGO (Gramática informal (EBNF): { } = repete 0+ vezes, [ ] = opcional. As expressões ficam para o capítulo 4.):
```
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
```
- CAIXA [No mundo real]: Exigir chaves no `se` fecha a porta do 'senão pendurado' (dangling else): nunca há dúvida de a qual `se` um `senao` pertence. Go e Rust fazem a mesma escolha.
- REVELAR: Quem pega cada erro? (1) `x <- 3 # 2;`  (2) `x <- 3 + ;`  (3) `x <- verdadeiro + 1;`
    → (1) **Léxico**: `#` não pertence a nenhum token. (2) **Sintático**: depois do `+` deveria vir uma expressão. (3) **Semântico**: a forma está certa, mas somar um `logico` não faz sentido.
- PREVEJA: E `inteiro 2x;`? Que tipo de erro a Byteiro deve dar?
    opções: Léxico: número malformado '2x' | Sintático: esperava identificador, encontrou '2' | ✔Depende de como VOCÊ especificou o léxico, e precisa documentar | Nenhum: 2x é um identificador válido
    explicação: Um léxico pode cortar `2x` em NUM(2) + ID(x) e deixar o sintático reclamar, ou recusar na hora. Os dois são defensáveis! Na Byteiro decidimos que é erro **léxico** ('número malformado'). O que tira ponto é não ter decidido.
- CAIXA [Dica de prova]: Escreva a especificação no relatório ANTES do código: tabela de tokens, precedência, gramática, decisões (maiúsculas importam? pode redeclarar em bloco interno?). Ela vira o seu mapa, e muitos professores dão nota para essa parte.
- PASSOS (Lição de casa antes do capítulo 3):
    · Escreva 5 programas Byteiro CORRETOS, cada um exercitando um recurso (declaração, se/senao, enquanto, escreva, expressões).
    · Escreva 5 programas ERRADOS, com UM erro cada: léxico, sintático, não declarado, tipo errado, redeclaração.
    · Ao lado de cada um, anote a saída esperada. Parabéns: você acabou de começar a bateria de testes.
- LEXI: Tabela de tokens pronta? Então me passa a katana. Próximo capítulo eu fatio tudo.
- PASSOS (Na sua linguagem):
    · Quais tipos e comandos o seu enunciado exige? Monte a ficha técnica, como a da Byteiro.
    · Como se escrevem a atribuição e a comparação de igualdade? Se forem parecidas (`=` e `==`), o léxico vai precisar do maior casamento.
    · Qual é a precedência dos seus operadores, do mais fraco ao mais forte?
- TEXTO: **Teste desta etapa:** os 10 programas-exemplo (5 certos e 5 com um erro cada) estão na pasta `testes/`, e cada um tem a saída esperada anotada ao lado. Eles são a bateria do capítulo 9 começando.

## Capítulo 3: Do regex ao AFD: o analisador léxico [REGEX_DFA]
Cada token é uma regex, cada regex é um AFD, todos viram UM autômato, escrito em Java com switch.

- LEXI: Meu segredo: cada categoria de token é uma **expressão regular**; cada regex vira um **AFD**; e todos os AFDs são colados num autômato grande, com um estado inicial só.
- CÓDIGO (As regex da Byteiro):
```
ID      =  l (l | d | _)*          l = letra, d = dígito
NUM     =  d d*
CHAR    =  ' c '                   c = qualquer caractere menos ' e \n
STRING  =  " s* "                  s = qualquer caractere menos " e \n
MENOR   =  <        ATRIB = <-     MENOR_IGUAL = <=     DIFERENTE = <>
MAIOR   =  >        MAIOR_IGUAL = >=
DIV     =  /        (// e /* */ são comentários: descartados)
```
- WIDGET DFA entrada=«l(l|d)*» legenda: ID (l = letra, d = dígito; o _ ficou de fora para caber no widget). Teste 'l', 'ldl' e 'dl'.
- WIDGET DFA entrada=«dd*» legenda: NUM: pelo menos um dígito. Teste 'd', 'ddd' e a cadeia vazia.
- WIDGET DFA entrada=«m(h|i|g)?» legenda: A família do '<' (m = '<', h = '-', i = '=', g = '>'): aceita m, mh, mi, mg. Repare que o estado depois de 'm' já é final, é o MENOR sozinho.
- BYTE: Tá, são vários AFDs. Mas como eu junto tudo num só?
- PROF: Crie um estado 0 com uma seta para o começo de cada AFD. Como os primeiros caracteres são diferentes, letra, dígito, '<', '/'…, nunca há duas setas com o mesmo rótulo saindo do 0. Continua determinístico!
- CÓDIGO (O autômato grande da Byteiro (desenhe a versão bonita no relatório!)):
```
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
```
- CÓDIGO (Pedaço da tabela de transições (* = devolve o token sem consumir o caractere)):
```
estado │ letra  │ dígito │   <    │   -    │   =    │   >    │ outro
───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
  0    │   1    │   2    │   3    │ MENOS  │ IGUAL  │   4    │ (ver acima)
  1    │   1    │   1    │  ID*   │  ID*   │  ID*   │  ID*   │  ID*
  2    │  ERRO  │   2    │  NUM*  │  NUM*  │  NUM*  │  NUM*  │  NUM*
  3    │ MENOR* │ MENOR* │ MENOR* │ ATRIB  │ M_IGU  │ DIFER  │ MENOR*
  4    │ MAIOR* │ MAIOR* │ MAIOR* │ MAIOR* │ MA_IGU │ MAIOR* │ MAIOR*
```
- CAIXA [Não existem perguntas idiotas]: P: Cada palavra reservada precisa de um AFD próprio? R: Daria um autômato enorme. O truque clássico: reconheça tudo como ID (estado 1) e, no fim, consulte uma tabela (HashMap) de reservadas. Se estiver lá, é palavra-chave; senão, é ID.
- CÓDIGO (TipoToken.java e Token.java):
```
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
```
- CÓDIGO (Lexico.java (1/3): campos e ajudantes. '\0' faz o papel de 'fim do arquivo'.):
```
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
```
- CÓDIGO (Lexico.java (2/3): o AFD inteiro: um case por estado, igualzinho ao desenho):
```
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
```
- CÓDIGO (Lexico.java (3/3)):
```
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
```
- PREVEJA: Entrada `x<y`. O AFD está no estado 3 (já leu '<') e vê o 'y'. O que o código faz?
    opções: Consome o 'y' e devolve MENOR | ✔Devolve MENOR SEM consumir o 'y', ele será o começo do próximo token | Erro léxico: '<y' não é token | Volta ao estado 0 e descarta o '<'
    explicação: O 'y' é o 'outro*' do desenho: ele só serviu para avisar que o token acabou. Se você fizer `pos++` aí, o 'y' some e o parser recebe `x <` seguido do que vier depois. É o bug nº 1 de léxico feito à mão.
- CAIXA [Cuidado!]: Contagem de linhas: incremente `linha` em TODO lugar que consome '\n', inclusive dentro do comentário de bloco (estado 7). E não conte '\r': arquivo salvo no Windows tem '\r\n' no fim de cada linha; contar os dois dobra o número da linha nas mensagens.
- CAIXA [Cuidado!]: `Character.isLetter` aceita 'ç', 'é', 'π'… Se a especificação diz que identificador é só ASCII, troque por `(c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')`. Detalhe pequeno, mas teste de correção adora.
- PASSOS (Os erros léxicos da Byteiro (sempre com a linha!)):
    · Caractere inválido: `x <- 3 # 2;` → linha N: erro léxico: caractere inválido '#'
    · Número malformado: `12abc` → número malformado '12a'
    · Caractere vazio ou sem fechar: `''` ou `'A`
    · String que atravessa a linha: `"olá` + Enter
    · Comentário `/*` sem `*/` até o fim do arquivo
- CAIXA [No mundo real]: Muitos enunciados pedem palavras-chave que ignoram maiúsculas (`SE`, `Se`, `se`). É UMA linha: `RESERVADAS.getOrDefault(s.toLowerCase(), TipoToken.ID)`. Decida também se identificadores ignoram maiúsculas (`Soma` = `soma`?), e documente.
- CÓDIGO (TesteLexico.java: rode ANTES de escrever uma linha do parser):
```
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
```
- CÓDIGO (Saída esperada: o comentário sumiu, `<-` e `<>` são UM token cada, e a linha muda no lugar certo):
```
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
```
- REVELAR: Por que `proximo()` devolve um token por vez em vez de uma lista com todos?
    → Porque o parser de uma passada pede tokens sob demanda, e um erro léxico aparece exatamente quando o parser chega lá. Gerar a lista inteira antes também funciona (e permite espiar vários tokens à frente); só garanta que o erro léxico continue saindo com a linha certa.
- LEXI: Fatiado! Agora alguém precisa conferir se os pedaços estão na ORDEM certa. Chama a Profª Gramática.
- PASSOS (Na sua linguagem):
    · Quais tokens seus começam com o mesmo caractere? Cada grupo vira um estado, como o do `<`.
    · Como são os seus comentários e strings? Onde cada um termina?
    · Que mensagem o seu léxico dá para um caractere que não pertence à linguagem?
- TEXTO: **Teste desta etapa:** troque a linha do `TesteLexico` por uma da SUA linguagem, com um comentário, um operador de dois caracteres e uma quebra de linha. Confira token a token, como na saída esperada acima: o comentário some, o operador de dois caracteres sai como um token só, e o número da linha muda no lugar certo.
- CÓDIGO (Teste desta etapa: o comando e o que deve sair):
```
javac -d out src/*.java
java -cp out TesteLexico
```

## Capítulo 4: A gramática LL(1) da Byteiro [LL1_TABLE]
Tirar recursão à esquerda, fatorar e provar com FIRST/FOLLOW que um token basta para decidir.

- PROF: A gramática informal do capítulo 2 é ótima para humanos. Para virar descida recursiva, ela precisa ser **LL(1)**: olhando UM token, o parser sabe qual regra usar, sem voltar atrás, sem adivinhar.
- PASSOS (As três checagens antes de programar):
    · 1. **Sem recursão à esquerda**: nenhum `A -> A …`. (Em Java isso é recursão infinita.)
    · 2. **Fatorada**: duas alternativas do mesmo não-terminal não podem começar igual.
    · 3. **FIRST/FOLLOW sem cruzamento**: os FIRST das alternativas são disjuntos; e se uma alternativa pode sumir (ε), os FIRST das outras não cruzam com o FOLLOW.
- CÓDIGO (Primeira tentativa: precedência certinha… e recursão à esquerda em quatro lugares):
```
Expr  -> Expr ou ExprE | ExprE
ExprE -> ExprE e Rel | Rel
Rel   -> Add oprel Add | Add
Add   -> Add + Mul | Add - Mul | Mul
Mul   -> Mul * Un | Mul / Un | Mul % Un | Un
Un    -> nao Un | - Un | Prim
Prim  -> ( Expr ) | ID | NUM | CHAR | verdadeiro | falso
```
- PREVEJA: O que acontece se você traduzir `Add -> Add + Mul` direto para Java (`void add() { add(); casaToken(MAIS); mul(); }`)?
    opções: Funciona normalmente | ✔StackOverflowError: add() chama add() sem consumir nenhum token | Erro de compilação do Java | Funciona, mas com associatividade errada
    explicação: A primeira coisa que `add()` faz é chamar `add()`: com o MESMO token na frente. Nada muda, então ela se chama para sempre até a pilha estourar.
- WIDGET LEFT_RECURSION gramática=«Add -> Add + Mul | Add - Mul | Mul ⏎ Mul -> Mul * Un | Un ⏎ Un -> - Un | ( Add ) | id | num» legenda: Antes e depois: A -> A α | β vira A -> β A' e A' -> α A' | ε.
- TEXTO: Na EBNF fica ainda mais limpo: `A -> A α | β` é o mesmo que `A -> β { α }`: um β seguido de zero ou mais α. E `{ }` vira um **while** em Java. Bônus: o while monta a árvore para a esquerda, então `10 - 3 - 2` continua dando 5.
- CÓDIGO (Expressões da Byteiro em EBNF LL(1), sete níveis, sete métodos Java):
```
expr     -> exprE { ou exprE }
exprE    -> exprRel { e exprRel }
exprRel  -> exprAdd [ oprel exprAdd ]        oprel = "=" | "<>" | "<" | ">" | "<=" | ">="
exprAdd  -> exprMul { ( + | - ) exprMul }
exprMul  -> exprUn { ( * | / | % ) exprUn }
exprUn   -> nao exprUn | - exprUn | primario
primario -> ( expr ) | ID | NUM | CHAR | verdadeiro | falso
```
- CAIXA [Gancho de memória]: Cascata: quanto mais FUNDO o método, mais FORTE o operador. `ou` mora no topo (agrupa por último); `*` mora lá embaixo (agrupa primeiro).
- TEXTO: E o `Rel -> Add oprel Add | Add`? As duas alternativas começam com `Add`: prefixo comum! Fatorando: `Rel -> Add Rel'` e `Rel' -> oprel Add | ε`. Na EBNF isso é o opcional `[ oprel exprAdd ]`: que em Java vira um **if**.
- WIDGET LEFT_FACTORING gramática=«Cmd -> id <- E ; | id ( E ) ; | se ( E ) B | se ( E ) B senao B ⏎ B -> { } ⏎ E -> id | num» legenda: Se a Byteiro tivesse chamada de procedimento `p(x);`, atribuição e chamada começariam com `id`. E `se` com e sem `senao` também. Veja a fatoração.
- CÓDIGO (Os comandos da Byteiro em BNF (expressões resumidas em E), prontos para o teste LL(1)):
```
Cmd -> id <- E ; | se ( E ) Bloco Senao | enquanto ( E ) Bloco | escreva ( E ) ;
Senao -> senao Bloco | ε
Bloco -> { Cmds }
Cmds -> Cmd Cmds | ε
E -> id | num
```
- WIDGET FIRST_FOLLOW gramática=«Cmd -> id <- E ; | se ( E ) Bloco Senao | enquanto ( E ) Bloco | escreva ( E ) ; ⏎ Senao -> senao Bloco | ε ⏎ Bloco -> { Cmds } ⏎ Cmds -> Cmd Cmds | ε ⏎ E -> id | num» legenda: Calcule FIRST e FOLLOW passo a passo. Repare: `senao` NÃO aparece em FOLLOW(Senao).
- WIDGET LL1_TABLE gramática=«Cmd -> id <- E ; | se ( E ) Bloco Senao | enquanto ( E ) Bloco | escreva ( E ) ; ⏎ Senao -> senao Bloco | ε ⏎ Bloco -> { Cmds } ⏎ Cmds -> Cmd Cmds | ε ⏎ E -> id | num» legenda: Nenhuma célula com duas produções: é LL(1)! O bloco obrigatório no `se` resolveu o senão pendurado.
- PREVEJA: Em `Senao -> senao Bloco | ε`, quando o parser escolhe a alternativa ε?
    opções: Nunca: ε é só enfeite | ✔Quando o token atual está em FOLLOW(Senao), na prática, 'quando não é senao' | Quando o token atual é senao | Quando o arquivo acaba
    explicação: A tabela põe ε nas colunas de FOLLOW(Senao). No código da descida recursiva isso vira `if (eh(SENAO)) { … }` sem else: se não é `senao`, não consome nada, e se o token for lixo, o próximo `casaToken` reclama.
- CAIXA [Cuidado!]: Se a sua linguagem permitir `se (c) comando senao comando` SEM chaves, a célula [Senao, senao] ganha duas produções: é o dangling else, e a gramática não é LL(1). Solução prática no parser: sempre casar o `senao` com o `se` mais próximo (o `if` guloso), e documentar essa decisão.
- REVELAR: Calcule FIRST(comando) da Byteiro completa.
    → { ID, se, enquanto, escreva, leia, `{`, inteiro, logico, caractere }, declarações começam pelo tipo, e um bloco solto começa por `{`. Esse conjunto vira o método `comecaComando()` no capítulo 5.
- REVELAR: No bloco, `{ comando }` repete. Quando a repetição para?
    → Quando o token não está em FIRST(comando). Na gramática certa, ele só pode ser `}`: qualquer outra coisa faz o `casaToken(FECHA_CHAVE)` reclamar com uma mensagem precisa.
- TEXTO: O relatório pede FIRST e FOLLOW como prova de que a gramática é LL(1). As contas da aula usam BNF, e a Byteiro está em EBNF. A ponte é uma regra só: cada `{ α }` vira um não-terminal novo `X -> α X | ε`, e cada `[ α ]` vira `Y -> α | ε`. Abaixo está a Byteiro inteira assim, do `programa` ao `primario`.
- CÓDIGO (A Byteiro inteira em BNF. Cmds, Ids, Args, Ou, E2, AddR e MulR vêm de { }; Senao e RelR vêm de [ ]):
```
Programa -> programa id Bloco
Bloco -> { Cmds }
Cmds -> Cmd Cmds | ε
Cmd -> Decl | id <- Expr ; | Bloco
    | se ( Expr ) Bloco Senao
    | enquanto ( Expr ) Bloco
    | escreva ( Arg Args ) ;
    | leia ( id ) ;
Decl -> Tipo id Ids ;
Ids -> , id Ids | ε
Tipo -> inteiro | logico | caractere
Senao -> senao Bloco | ε
Args -> , Arg Args | ε
Arg -> string | Expr
Expr -> ExprE Ou
Ou -> ou ExprE Ou | ε
ExprE -> Rel E2
E2 -> e Rel E2 | ε
Rel -> Add RelR
RelR -> oprel Add | ε
Add -> Mul AddR
AddR -> + Mul AddR | - Mul AddR | ε
Mul -> Un MulR
MulR -> * Un MulR | / Un MulR | % Un MulR | ε
Un -> nao Un | - Un | Prim
Prim -> ( Expr ) | id | num | char
    | verdadeiro | falso
```
- PASSOS (FIRST e FOLLOW de cada não-terminal (calculados pelo motor do jogo)):
    · **Programa**: FIRST = { `programa` }; FOLLOW = { `$` }
    · **Bloco**: FIRST = { `{` }; FOLLOW = { `$` `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` `}` `senao` }
    · **Cmds**: FIRST = { `ε` `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` }; FOLLOW = { `}` }
    · **Cmd**: FIRST = { `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` }; FOLLOW = { `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` `}` }
    · **Decl**: FIRST = { `inteiro` `logico` `caractere` }; FOLLOW = { `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` `}` }
    · **Ids**: FIRST = { `,` `ε` }; FOLLOW = { `;` }
    · **Tipo**: FIRST = { `inteiro` `logico` `caractere` }; FOLLOW = { `id` }
    · **Senao**: FIRST = { `senao` `ε` }; FOLLOW = { `id` `{` `se` `enquanto` `escreva` `leia` `inteiro` `logico` `caractere` `}` }
    · **Args**: FIRST = { `,` `ε` }; FOLLOW = { `)` }
    · **Arg**: FIRST = { `string` `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `,` `)` }
    · **Expr**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `;` `)` `,` }
    · **Ou**: FIRST = { `ou` `ε` }; FOLLOW = { `;` `)` `,` }
    · **ExprE**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `ou` `;` `)` `,` }
    · **E2**: FIRST = { `e` `ε` }; FOLLOW = { `ou` `;` `)` `,` }
    · **Rel**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `e` `ou` `;` `)` `,` }
    · **RelR**: FIRST = { `oprel` `ε` }; FOLLOW = { `e` `ou` `;` `)` `,` }
    · **Add**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `oprel` `e` `ou` `;` `)` `,` }
    · **AddR**: FIRST = { `+` `-` `ε` }; FOLLOW = { `oprel` `e` `ou` `;` `)` `,` }
    · **Mul**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `+` `-` `oprel` `e` `ou` `;` `)` `,` }
    · **MulR**: FIRST = { `*` `/` `%` `ε` }; FOLLOW = { `+` `-` `oprel` `e` `ou` `;` `)` `,` }
    · **Un**: FIRST = { `nao` `-` `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `*` `/` `%` `+` `-` `oprel` `e` `ou` `;` `)` `,` }
    · **Prim**: FIRST = { `(` `id` `num` `char` `verdadeiro` `falso` }; FOLLOW = { `*` `/` `%` `+` `-` `oprel` `e` `ou` `;` `)` `,` }
- CAIXA [Dica de prova]: Como escrever a prova: para cada não-terminal com mais de uma regra, os FIRST das regras não se cruzam. Para cada um que pode sumir (tem ε), o FIRST dele não cruza o FOLLOW. Exemplo: `Senao` tem FIRST { senao, ε } e o FOLLOW não tem `senao`, então o parser sabe quando o senão acabou. Com essa gramática, nenhuma casa da tabela LL(1) recebe duas regras.
- CAIXA [Não existem perguntas idiotas]: P: Preciso entregar a tabela LL(1) inteira? R: Depende do enunciado. Para descida recursiva, o que importa é a PROVA de que não há conflito: em cada ponto de decisão do código, os conjuntos de tokens testados são disjuntos. FIRST/FOLLOW no relatório é exatamente essa prova.
- CAIXA [Dica de prova]: O trio que cai em prova e no TP: recursão à esquerda → **while**; opcional `[ ]` → **if**; repetição `{ }` → **while**. Decore e você traduz qualquer EBNF para Java em minutos.
- PILHA: Gramática… LL(1)… pronta. Agora… me deem métodos… para empilhar.
- PASSOS (Na sua linguagem):
    · Quais regras da sua gramática têm recursão à esquerda?
    · Quais alternativas começam com o mesmo token?
    · Onde você usou `{ }` e `[ ]`? Em cada um, o FIRST de dentro cruza com o que pode vir depois?
- TEXTO: **Teste desta etapa:** a tabela FIRST e FOLLOW da sua gramática inteira, como a da Byteiro acima, com as duas condições conferidas: os FIRST das alternativas não se cruzam, e cada parte que pode sumir não cruza com o FOLLOW. Nenhuma casa da tabela LL(1) fica com duas regras. Essa tabela vai para o relatório.

## Capítulo 5: O parser descendente recursivo [RECURSIVE_DESCENT]
Um método por não-terminal, casaToken para terminais, if/while no token atual, e erros com linha.

- PILHA: Eu sou… a pilha de chamadas do Java. Cada método que você chama… eu empilho. Quando ele retorna… desempilho. A árvore sintática… é o caminho que eu faço.
- PASSOS (A receita (decore!)):
    · Um método Java para cada não-terminal: `bloco()`, `comando()`, `expr()`…
    · Terminal no lado direito → `casaToken(TIPO)`: confere e avança.
    · Não-terminal no lado direito → chama o método dele.
    · Várias alternativas → `if`/`switch` olhando o token atual (os FIRST que você calculou).
    · `{ … }` → `while`; `[ … ]` → `if`.
- WIDGET DESCENT_CODE gramática=«E -> T E' ⏎ E' -> + T E' | ε ⏎ T -> F T' ⏎ T' -> * F T' | ε ⏎ F -> ( E ) | id | num» entrada=«id + num * id» legenda: O código que o motor gera para a cascata de dois níveis, e a pilha de chamadas para `id + num * id`.
- CÓDIGO (Sintatico.java (1/4), a infraestrutura. Só `avanca()` mexe em `tok`!):
```
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
```
- CÓDIGO (Sintatico.java (2/4), programa, bloco e o 'despachante' comando()):
```
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
```
- CÓDIGO (Sintatico.java (3/4), os comandos: leia cada método ao lado da sua regra EBNF):
```
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
```
- CÓDIGO (Sintatico.java (4/4), a cascata de precedência: cada nível chama o de baixo):
```
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
```
- REVELAR: Por que `exprRel` usa `if` e `exprAdd` usa `while`?
    → Porque `a + b + c` é permitido (repetição → while), mas `a < b < c` não é (relacionais são NÃO associativos → no máximo um → if). Com o if, o `<` extra sobra e o parser reclama dele.
- PASSOS (A pilha de chamadas para `x <- a + 2 * b;`):
    · `atribuicao()` casa ID(x) e `<-`, depois chama `expr()`.
    · `expr` → `exprE` → `exprRel` → `exprAdd` → `exprMul` → `exprUn` → `primario()` casa `a`. Volta tudo até `exprAdd`.
    · `exprAdd` vê `+`: avança e chama `exprMul()`.
    · `exprMul` → `exprUn` → `primario` casa `2`. De volta em `exprMul`, vê `*`: avança, `exprUn` → `primario` casa `b`.
    · `exprMul` não vê mais `*`: retorna. `exprAdd` não vê mais `+`: retorna. Sobe até `atribuicao()`.
    · `atribuicao()` casa `;`. Repare: `2 * b` terminou ANTES do `+`: a precedência saiu de graça da ordem das chamadas.
- CÓDIGO (erro_falta_pv.byt — esqueceram o ';' da linha 2):
```
programa t {
    inteiro x
    x <- 1;
}
```
- PREVEJA: Que mensagem o nosso parser imprime para esse programa?
    opções: linha 2: erro sintático: esperava ';', encontrou 'x' | ✔linha 3: erro sintático: esperava ';', encontrou 'x' | linha 4: erro sintático: esperava '}', encontrou fim de arquivo | Nenhuma: o programa compila
    explicação: O parser só PERCEBE que falta o ';' quando vê o token seguinte, o `x` da linha 3. A mensagem usa a linha do token encontrado. Se o enunciado pedir 'a linha do erro', isso é o esperado; se quiser a linha anterior, guarde a linha do último token consumido.
- CAIXA [Cuidado!]: Esqueceu o `casaToken(TipoToken.EOF)` no fim de `programa()`? Então `programa t { } lixo lixo lixo` compila 'com sucesso'. Esse é um dos primeiros testes que o corretor faz.
- CAIXA [Cuidado!]: Chamar `lexico.proximo()` direto em vários lugares 'perde' tokens: você avança duas vezes sem perceber. Regra de ouro: SÓ o `avanca()` (e o `casaToken`, que usa ele) mexe em `tok`.
- WIDGET DESCENT_CODE gramática=«Cmd -> id <- E ; | se ( E ) Bloco Senao | enquanto ( E ) Bloco | escreva ( E ) ; ⏎ Senao -> senao Bloco | ε ⏎ Bloco -> { Cmds } ⏎ Cmds -> Cmd Cmds | ε ⏎ E -> id | num» entrada=«se ( id ) { }» legenda: Agora com os comandos da Byteiro: veja o if/else de `Cmd` testando os FIRST de cada alternativa.
- CAIXA [Não existem perguntas idiotas]: P: E se o enunciado pedir para continuar depois do primeiro erro? R: Modo pânico: ao errar, registre a mensagem e descarte tokens até um ponto seguro (`;` ou `}`), depois continue. Para a maioria dos TPs, parar no primeiro erro já é o pedido, confira.
- WIDGET PANIC gramática=«Cmds -> Cmd Cmds | ε ⏎ Cmd -> id <- E ; | escreva ( E ) ; ⏎ E -> id | num» entrada=«id <- ; escreva ( id ) ;» legenda: Atribuição sem expressão: o parser reclama, pula até um token de sincronização e segue para o escreva.
- CAIXA [No mundo real]: Os parsers do GCC (C/C++), do Clang, do javac e do Go são descida recursiva escrita à mão, exatamente a técnica do seu TP. Ela dá controle total sobre as mensagens de erro, e é por isso que os compiladores profissionais voltaram a ela.
- CAIXA [Dica de prova]: Questão clássica: 'escreva o procedimento da descida recursiva para o não-terminal X'. Siga a receita: um if por alternativa (com os FIRST), casaToken nos terminais, chamada nos não-terminais, e um else com erro.
- PASSOS (Na sua linguagem):
    · Qual método corresponde a cada não-terminal da sua gramática?
    · Onde entram os `while` (de cada `{ }`) e os `if` (de cada `[ ]`)?
    · Que mensagem o seu parser dá quando falta o terminador de um comando?
- TEXTO: **Teste desta etapa:** todos os programas de `testes/ok/` passam sem erro, e o que falta um `;` para na linha do token seguinte:
- CÓDIGO (Teste desta etapa: o comando e o que deve sair):
```
# testes/erro/falta_ponto_virgula.byt
programa t {
    inteiro x
    x <- 1;
}

java -cp out Main testes/erro/falta_ponto_virgula.byt
linha 3: erro sintático: esperava ';', encontrou 'x'
```

## Capítulo 6: Tabela de símbolos e escopos [SCOPE_SYMBOLS]
Uma pilha de HashMaps: abre bloco = push, declara = put no topo, usa = busca do topo para baixo.

- BYTE: Meu parser aceita `x <- 1;` mesmo sem nenhum `inteiro x;`. Ele não lembra de NADA!
- PROF: Ele precisa de memória: a **tabela de símbolos**. E como a Byteiro tem blocos dentro de blocos, essa memória precisa de **escopos**.
- CÓDIGO (escopos.byt: leia de cima para baixo e acompanhe cada x):
```
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
```
- WIDGET SCOPE legenda: Toque em cada uso e veja a qual declaração ele se liga.
- PREVEJA: O que o compilador diz na linha 11 (`y <- 3;`)?
    opções: Nada: y foi declarado no programa | ✔linha 11: erro semântico: 'y' não foi declarado | linha 7: erro semântico: y declarado depois de x | Erro sintático
    explicação: Quando o bloco interno fechou, o escopo dele (com o y) foi DESEMPILHADO. A busca na linha 11 só encontra o escopo de fora, onde não há y.
- PASSOS (A pilha de HashMaps em quatro operações):
    · `{` → **abreEscopo**: empilha um HashMap vazio.
    · `inteiro x;` → **declara**: põe x no HashMap do TOPO (se já estiver lá: erro de redeclaração).
    · uso de `x` → **busca**: procura do topo para baixo; o primeiro que achar ganha (é isso que faz o x de dentro esconder o de fora).
    · `}` → **fechaEscopo**: desempilha — tudo que foi declarado no bloco morre junto.
- CÓDIGO (TipoDado.java e Simbolo.java):
```
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
```
- CÓDIGO (TabelaSimbolos.java, a classe inteira cabe numa tela):
```
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
```
- PILHA: Busca? Eu começo… pelo topo. Não achou? Desço um andar. Não achou? Desço mais. Chegou no chão… e nada? Não declarado.
- CAIXA [Cuidado!]: Armadilha do Java: se você usar `java.util.Stack` em vez de `ArrayDeque`, o for-each percorre a pilha de BAIXO para CIMA! A busca acha o x de FORA primeiro e o sombreamento quebra sem nenhuma mensagem de erro. Com `ArrayDeque` + `push`, a iteração começa no topo.
- CAIXA [Não existem perguntas idiotas]: P: Por que o rótulo `x_1` e não só `x`? R: Dois motivos. (1) Os dois x do exemplo precisam de DOIS endereços diferentes no `.data`: se ambos fossem `x`, o MARS reclamaria de rótulo duplicado. (2) Variáveis chamadas `b`, `j` ou `add` coincidem com instruções MIPS e podem confundir o montador. O sufixo resolve os dois.
- CÓDIGO (No Sintatico: o parser agora abre/fecha escopos e registra/consulta nomes):
```
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
```
- TEXTO: Onde chamar `usa(...)`? Em todo lugar que um ID aparece sem ser declaração: em `atribuicao()` (o destino), em `leia()` e em `primario()`. No capítulo 7 o `Simbolo` devolvido vai dizer o TIPO; no 8, o RÓTULO de memória.
- PASSOS (A tabela durante escopos.byt):
    · Linha 1, `{` do programa: pilha = [ {} ]
    · Linha 2: pilha = [ {x→x_0 inteiro} ]
    · Linha 4, `{`: pilha = [ {}, {x→x_0} ] (topo à esquerda)
    · Linhas 5 e 7: topo = {x→x_1 caractere, y→y_2 inteiro}
    · Linha 6: busca(x) acha x_1 no topo, o caractere.
    · Linha 9, `}`: desempilha. Pilha = [ {x→x_0} ]
    · Linha 10: busca(x) acha x_0. Linha 11: busca(y) → null → erro semântico!
- CAIXA [Não existem perguntas idiotas]: P: Pode declarar um x no bloco interno se já existe um x fora? R: Depende da linguagem! C permite (sombreamento). Java PROÍBE redeclarar uma variável local num bloco interno. A Byteiro permite. Qualquer escolha vale, desde que esteja escrita no relatório e o compilador faça o que está escrito.
- CAIXA [Dica de prova]: Questão clássica: dado um programa com blocos aninhados, diga a qual declaração cada uso se refere (e quais usos são erro). O jogo Esconde-Esconde do Escopo treina exatamente isso.
- CAIXA [No mundo real]: Compiladores reais guardam bem mais no Simbolo: tamanho, deslocamento no registro de ativação (offset relativo ao frame pointer), se é parâmetro, se foi usado (é daí que vem o aviso 'unused variable').
- REVELAR: E se a Byteiro ganhasse funções com variáveis locais?
    → Cada função abriria um escopo para parâmetros e locais, e as locais morariam na PILHA de execução (offset do frame), não no `.data`, para que cada chamada recursiva tenha a sua cópia. Veja a região de registros de ativação na Forja do Código.
- PASSOS (Na sua linguagem):
    · Que construções abrem escopo na sua linguagem: blocos, funções, laços?
    · Pode declarar o mesmo nome num bloco de dentro (sombrear)?
    · Pode usar uma variável antes de declarar? E declarar duas vezes no mesmo bloco?
- TEXTO: **Teste desta etapa:** variável sem declaração e declaração repetida param com a linha certa:
- CÓDIGO (Teste desta etapa: o comando e o que deve sair):
```
programa t { x <- 1; }
  -> linha 1: erro semântico: 'x' não foi declarado
programa t { inteiro a; caractere a; }
  -> linha 1: erro semântico: 'a' já foi declarado neste escopo (linha 1)
```

## Capítulo 7: Verificação de tipos [ATTRIBUTES_TYPES]
Cada método de expressão devolve o TIPO do que reconheceu; atribuição e condição conferem as regras.

- BYTE: Olha isso: `logico ok; ok <- 3 * 'A';` passa no meu parser! Não deveria dar erro?
- PROF: Deveria. A forma está certa; o SENTIDO não. A ideia: cada método de expressão para de ser `void` e passa a **devolver o tipo** do pedaço que reconheceu. O tipo sobe da folha para a raiz, é um **atributo sintetizado**.
- WIDGET ATTR_TREE entrada=«a + 1 > b && ok» legenda: O tipo subindo pela árvore (no widget a sintaxe é de C: `&&` é o nosso `e`; a e b são inteiros, ok é lógico).
- CÓDIGO (Regras de tipo das expressões da Byteiro (vão para o relatório)):
```
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
```
- CÓDIGO (Regras da atribuição: conversão implícita só no sentido que não perde informação):
```
destino       valor        resultado
---------     ---------    -----------------------------------------
inteiro   <-  inteiro      ok
inteiro   <-  caractere    ok (conversão implícita: 'A' vira 65)
caractere <-  caractere    ok
caractere <-  inteiro      ERRO (300 não cabe num caractere)
logico    <-  logico       ok
qualquer outra combinação  ERRO
```
- PREVEJA: Com `inteiro n; caractere c;`, qual destas linhas dá erro semântico?
    opções: n <- c + 1; | n <- c; | ✔c <- c + 1; | c <- 'Z';
    explicação: `c + 1` é numérico + numérico → **inteiro**. Guardar inteiro em caractere é proibido (poderia não caber). Já `n <- c + 1` e `n <- c` estão ok: caractere sobe para inteiro sem perder nada.
- CÓDIGO (Ajudantes de tipo no Sintatico):
```
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
```
- CÓDIGO (A cascata agora devolve TipoDado, o mesmo esqueleto do capítulo 5):
```
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
```
- CÓDIGO (Folhas da árvore: literais têm tipo fixo; identificadores perguntam à tabela de símbolos):
```
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
```
- CÓDIGO (Comandos: atribuição confere a compatibilidade; se/enquanto exigem condição logico):
```
private void atribuicao() {
    Token id = casaToken(TipoToken.ID);
    Simbolo s = usa(id);
    Token seta = casaToken(TipoToken.ATRIB);
    TipoDado t = expr();
    exige(podeAtribuir(s.tipo, t), seta.linha,
          "não dá para guardar " + t + " em '" + s.nome + "' (" + s.tipo + ")");
    casaToken(TipoToken.PONTO_VIRGULA);
}

private void condicao() {                     // ( expr ), usado por se e enquanto
    casaToken(TipoToken.ABRE_PAR);
    int linha = tok.linha;
    exige(expr() == TipoDado.LOGICO, linha, "a condição precisa ser logico");
    casaToken(TipoToken.FECHA_PAR);
}
```
- CAIXA [Cuidado!]: Bug campeão: em `exprRel`, esquecer o `t = TipoDado.LOGICO`. Aí `a < b` 'tem tipo' inteiro, `se (a < b)` dá erro de condição e `x <- a < b` passa para um inteiro. Comparação SEMPRE produz logico, não importa o tipo dos lados.
- PASSOS (Os tipos subindo em `ok <- c + 1 > n e nao ok;` (c caractere, n inteiro, ok logico)):
    · `primario` de c → CARACTERE; de 1 → INTEIRO.
    · `exprAdd`: numérico + numérico → INTEIRO.
    · `primario` de n → INTEIRO. `exprRel`: INTEIRO > INTEIRO → LOGICO.
    · `exprUn`: nao LOGICO → LOGICO.
    · `exprE`: LOGICO e LOGICO → LOGICO.
    · `atribuicao`: podeAtribuir(LOGICO, LOGICO) → ✓.
- PREVEJA: `inteiro n; … se (n) { … }` na Byteiro:
    opções: Compila: n diferente de 0 é verdadeiro, como em C | ✔Erro semântico: a condição precisa ser logico | Erro sintático: esperava comparação | Compila com aviso
    explicação: A Byteiro é 'estilo Java': condição tem que ser logico. Sintaticamente `(n)` é uma expressão perfeita; quem barra é a checagem de tipo do `condicao()`. Escreva `se (n <> 0)`.
- CAIXA [No mundo real]: Cada linguagem escolhe. C converte inteiro para booleano sem pedir licença; JavaScript faz `'5' * 2` dar 10; Java exige boolean no if. Nenhuma está 'certa', o que importa é a regra estar escrita e o compilador obedecê-la.
- CAIXA [Não existem perguntas idiotas]: P: A conversão caractere → inteiro gera alguma instrução? R: Não! O caractere já mora na memória como número (65). A 'conversão' existe só na tabela de tipos. Já em C, int → float gera uma instrução de verdade no MIPS (`cvt.s.w`), porque a representação muda.
- CÓDIGO (Cinco testes semânticos com a saída esperada, copie para testes/erro/):
```
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
```
- CAIXA [Dica de prova]: Atributo **sintetizado** sobe (o tipo da expressão, calculado a partir dos filhos). Atributo **herdado** desce: em `inteiro a, b, c;` o tipo é passado como parâmetro para `declaraUm(t)` de cada ID. Seu TP tem os dois: cite isso no relatório!
- REVELAR: A verificação de tipos precisa ser uma fase separada, com uma árvore?
    → Não necessariamente. No esquema de uma passada ela vive DENTRO do parser (tradução dirigida pela sintaxe). Construir uma AST e depois visitá-la também é válido, e facilita otimizações maiores, mas dá mais código. Para o TP, uma passada é o caminho mais curto.
- PASSOS (Na sua linguagem):
    · Quais tipos podem se misturar numa conta?
    · A condição do se e do enquanto precisa ser lógica, ou número vale?
    · Guardar um tipo numa variável de outro tipo converte ou é erro?
- TEXTO: **Teste desta etapa:** os cinco testes semânticos acima, cada um em `testes/erro/`, imprimem exatamente a mensagem ao lado deles.

## Capítulo 8: Gerando MIPS para o MARS [CODEGEN_MIPS]
Expressões em registradores temporários, variáveis no .data, rótulos para se/enquanto, syscalls para escrever.

- PROF: Hora da Forja. Cada método, além de conferir tipos, agora **emite** o seu pedaço de assembly. Primeiro, o kit mínimo de MIPS que o gerador usa.
- CÓDIGO (Cola de MIPS do TP: só isto já compila a Byteiro inteira):
```
.data                         # seção de dados (variáveis globais)
n_0:  .word 0                 # um inteiro de 32 bits, começa em 0
str0: .asciiz "soma = "       # string terminada em \0
.text                         # seção de código
main:
    li   $t0, 42              # $t0 = 42                     (load immediate)
    lw   $t0, n_0             # $t0 = memória[n_0]           (load word)
    sw   $t0, n_0             # memória[n_0] = $t0           (store word)
    move $a0, $t0             # $a0 = $t0                    (copia registrador)
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
```
- WIDGET MIPS entrada=«a * b + c * d» legenda: O widget usa o estilo 'tudo na memória' (cada temporário vira um .word). Nosso gerador vai guardar os temporários em registradores $t0–$t9.
- TEXTO: A estratégia de expressões: os registradores `$t0…$t9` funcionam como uma **pilha**. Cada método de expressão pega o próximo registrador livre, deixa o resultado nele e devolve QUAL é. Uma operação binária faz `esq = esq op dir` e devolve o `dir` ao estoque — ele sempre é o mais novo.
- CÓDIGO (A infraestrutura do gerador (dentro do Sintatico, Java 17)):
```
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
```
- CÓDIGO (As folhas e os unários: cada folha ocupa UM registrador novo):
```
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
```
- CÓDIGO (Operações binárias: o resultado fica no registrador da ESQUERDA):
```
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
```
- PREVEJA: Quantos registradores $t ficam ocupados AO MESMO TEMPO, no pico, ao gerar `a + b * c`?
    opções: 1 | 2 | ✔3 | 5
    explicação: a → $t0; o `+` chama exprMul: b → $t1, c → $t2 (pico: 3!). `mul $t1, $t1, $t2` libera o $t2; `add $t0, $t0, $t1` libera o $t1. Sobra só o $t0 com o resultado.
- PASSOS (`soma <- soma + n * 2;` virando MIPS):
    · `primario(soma)` → `lw $t0, soma_1`
    · `+` → exprMul → `primario(n)` → `lw $t1, n_0`
    · `*` → `primario(2)` → `li $t2, 2`
    · binaria → `mul $t1, $t1, $t2` (libera $t2)
    · binaria → `add $t0, $t0, $t1` (libera $t1)
    · atribuicao → `sw $t0, soma_1` (libera $t0 — de volta a zero registradores ocupados)
- CÓDIGO (Atribuição, se e enquanto: o esquema de tradução com rótulos):
```
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
```
- CÓDIGO (Os dois moldes (templates) que você vai desenhar no relatório):
```
# se (cond) { A } senao { B }        # enquanto (cond) { C }
    <cond em $t0>                    enquantoN:
    beq $t0, $zero, senaoK               <cond em $t0>
    <código de A>                        beq $t0, $zero, fimenqM
    j fimseL                             <código de C>
senaoK:                                  j enquantoN
    <código de B>                    fimenqM:
fimseL:
```
- CÓDIGO (Entrada e saída com syscall):
```
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
```
- CAIXA [Cuidado!]: `div` com três operandos, `rem`, `seq`, `sne`, `sgt`, `sle` e `sge` são **pseudo-instruções**: o MARS as expande em instruções reais. Funcionam perfeitamente no MARS, mas se o enunciado proibir pseudo-instruções, troque por `div`+`mflo`/`mfhi` e por combinações de `slt` com `xori`. Também são pseudo-instruções `li`, `la`, `move` e o `lw`/`sw` com rótulo: o MARS troca `move` por `addu` e monta `li`, `la` e o rótulo com `lui` e `ori`. Se o enunciado proibir, escreva essas trocas à mão.
- CÓDIGO (De novo o contagem.byt do capítulo 2…):
```
programa contagem {
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
}
```
- CÓDIGO (…e o saida.asm COMPLETO que o nosso compilador gera para ele):
```
# gerado pelo compilador Byteiro
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
    syscall
```
- PASSOS (Rodando no MARS):
    · Baixe o MARS (um .jar, ex.: `Mars4_5.jar`), precisa de Java instalado.
    · Gráfico: `java -jar Mars4_5.jar` → File › Open → `saida.asm`.
    · Run › Assemble (F3). Erro aqui = o SEU gerador emitiu algo que não é MIPS válido.
    · Run › Go (F5). A saída aparece na aba **Run I/O**: `soma = 30`.
    · Linha de comando (ótimo para testes automáticos): `java -jar Mars4_5.jar nc saida.asm` (nc = sem o aviso de copyright).
    · Depurando: use Step (F7) e olhe a aba Registers — dá para ver $t0 mudar a cada instrução.
- CAIXA [Cuidado!]: Esqueceu o `li $v0, 10` + `syscall` no final? O processador continua executando o que estiver depois do seu código, e o MARS termina com um aviso de 'dropped off bottom'. Encerre sempre explicitamente.
- PREVEJA: Se `novoRotulo` devolvesse sempre `"fimse"`, sem o contador, o que aconteceria num programa com dois `se`?
    opções: Nada: rótulos podem repetir | ✔O MARS recusa na montagem: rótulo definido duas vezes | O segundo se pularia para o primeiro fimse | O Java dá erro de compilação
    explicação: Rótulo é um NOME de endereço: dois iguais é ambiguidade, e o montador recusa. Por isso o contador global, e por isso todo teste do TP deve ter `se` dentro de `se` e `enquanto` dentro de `enquanto`.
- CAIXA [Não existem perguntas idiotas]: P: Por que `.word` (4 bytes) até para logico e caractere? R: Simplicidade: tudo usa `lw`/`sw`, sem pensar em alinhamento. Dá para usar `.byte` com `lb`/`sb` para caractere, economiza 3 bytes por variável e acrescenta um monte de casos especiais. Num TP, uniformidade ganha.
- CAIXA [No mundo real]: Esse estilo: toda variável na memória, carrega, opera, guarda, é parecido com o que o GCC faz com `-O0`. Os compiladores de verdade só ficam espertos (variáveis morando em registradores) quando você liga a otimização.
- REVELAR: Uma expressão aninhada à direita com 11 operandos (`a + (b + (c + …))`) esgota $t0–$t9. Como resolver?
    → Opções: (1) **derramar** (spill) para a pilha: `addi $sp, $sp, -4` + `sw $t9, 0($sp)` e desempilhar depois; (2) usar também $s0–$s7; (3) avaliar primeiro o lado que precisa de mais registradores (numeração de Sethi-Ullman). Para o TP, documentar o limite já costuma bastar.
- PASSOS (Na sua linguagem):
    · Como a sua linguagem escreve na tela, e qual syscall do MARS faz isso?
    · Onde ficam as suas variáveis: no `.data` ou na pilha?
    · Quantos rótulos cada comando de desvio seu precisa (se, enquanto, para)?
- TEXTO: **Teste desta etapa:** o `contagem.byt` do capítulo 2 compila, roda no MARS e imprime a saída que você anotou para ele:
- CÓDIGO (Teste desta etapa: o comando e o que deve sair):
```
javac -d out src/*.java
java -cp out Main testes/ok/contagem.byt
java -jar Mars4_5.jar nc saida.asm
```

## Capítulo 9: Otimizações simples e bateria de testes [OPTIMIZATION]
Dobrar constantes, cortar código morto, um peephole de leve, e testes que provam que nada quebrou.

- BYTE: `x <- 2 * 60 + 1;` vira li, li, mul, li, add, sw. O processador vai calcular 121 TODA vez que passar por ali?
- PROF: Vai: a não ser que o compilador calcule antes. Isso é **dobramento de constantes** (constant folding): se os dois lados de uma operação são conhecidos na compilação, faça a conta no Java e emita só o resultado.
- WIDGET TAC entrada=«a + 2 * 3» legenda: Repare na instrução de `2 * 3`: os dois operandos são constantes. O compilador pode fazer essa conta sozinho.
- TEXTO: O truque para encaixar isso no gerador de uma passada: o `Res` ganha um terceiro campo, `constante`. Uma folha numérica **reserva** o registrador, mas **adia** o `li`. Se a próxima operação também recebe uma constante, a conta é feita em Java e nenhum código sai. Só quando alguém realmente precisa do valor num registrador é que o `li` é emitido.
- CÓDIGO (Dobramento de constantes: o Res agora pode carregar um valor conhecido):
```
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
```
- TEXTO: No resto do gerador são três trocas mecânicas: (1) em `primario`, literais viram `return constante(tipo, valor);` em vez de `novoTemp()` + `li`; (2) cada `binaria(instr, esq, dir); esq = new Res(...)` vira `esq = binaria(op, instr, esq, dir, tipo);`; (3) todo lugar que usava `x.reg()` para emitir (`sw`, `move`, `beq`, `xori`, `sub`) passa a usar `noRegistrador(x)`. Nos unários: se o operando é constante, devolva `-c` ou `1 - c` sem emitir nada.
- CÓDIGO (Seis instruções viram duas):
```
# x <- 2 * 60 + 1;   SEM dobramento        # COM dobramento
    li  $t0, 2                                 li  $t0, 121
    li  $t1, 60                                sw  $t0, x_0
    mul $t0, $t0, $t1
    li  $t1, 1
    add $t0, $t0, $t1
    sw  $t0, x_0
```
- PREVEJA: Com esse dobramento, `y <- a + 2 + 3;` vira `a + 5`?
    opções: Sim, 2 + 3 é dobrado | ✔Não: a árvore é (a + 2) + 3 e nenhuma operação tem DOIS lados constantes | Não, porque `a` é variável e estraga a expressão inteira | Sim, mas só se `a` for zero
    explicação: O `+` associa à esquerda: primeiro `a + 2` (um lado variável), depois `(…) + 3`. Já `2 + 3 + a` vira `5 + a`. Compiladores reais reordenam (reassociação) para pegar o primeiro caso, um bom 'trabalho futuro' para o relatório.
- CAIXA [Cuidado!]: Nunca dobre `x / 0` com uma conta em Java: ela lança ArithmeticException e o SEU COMPILADOR cai com um stack trace. Devolva `null` (não dobra) e deixe o erro para a execução, ou emita um aviso com a linha. E repare: Java e MIPS truncam a divisão de inteiros do mesmo jeito (`-7 / 2 = -3`), então dobrar não muda o resultado.
- LEXI: E aquele `se (1 > 2) { … }`? O bloco nunca roda. Corta fora!
- CÓDIGO (Eliminação de código morto: o bloco continua sendo ANALISADO (erros ainda aparecem), só não gera MIPS):
```
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
```
- CAIXA [Não existem perguntas idiotas]: P: Por que não simplesmente pular os tokens do bloco morto? R: Porque o bloco ainda precisa estar CERTO: um erro de sintaxe ou de tipo dentro de `se (falso) { … }` continua sendo erro. Por isso ele passa pelo parser normalmente, só com a 'caneta' desligada. (A string de um `escreva` morto ainda vai para o .data, inofensivo.)
- CÓDIGO (Otimização peephole: uma janela de 2 instruções deslizando sobre o .asm pronto):
```
# antes                          # depois (peephole)
    lw   $t0, soma_1                 lw   $a0, soma_1
    move $a0, $t0
    li   $v0, 1                      li   $v0, 1
    syscall                          syscall

    sw   $t0, n_0                    sw   $t0, n_0
    lw   $t0, n_0                    # lw removido: $t0 já tem esse valor
```
- CAIXA [Cuidado!]: Peephole nunca atravessa um RÓTULO. Em `sw $t0, n_0` / `enquanto0:` / `lw $t0, n_0`, alguém pode pular para `enquanto0` vindo de outro lugar, com outro valor em $t0. Remover aquele `lw` quebra o laço.
- PILHA: Otimizou? Então… prove… que nada… quebrou.
- PASSOS (Bateria de testes que pega bug de verdade):
    · **Precedência e associatividade**: `escreva(2 + 3 * 4);` → 14; `escreva((2 + 3) * 4);` → 20; `escreva(10 - 3 - 2);` → 5 (e não 9!).
    · **Negativos e resto**: `escreva(-7 / 2, " ", -7 % 2);` → -3 -1.
    · **Laços**: `enquanto` que roda 0 vezes, 1 vez e N vezes.
    · **Aninhamento**: `se` dentro de `se` dentro de `enquanto` (rótulos únicos!).
    · **Escopos**: sombreamento (x interno e externo com valores diferentes, imprima os dois).
    · **Erros**: um arquivo por mensagem de erro, léxico, sintático, cada regra semântica.
    · **Bordas**: arquivo vazio, só comentário, comentário sem fechar, string com espaço, `'A' + 1` impresso como número.
- WIDGET MIPS entrada=«(a + b) * (c - d)» legenda: Treine a conferência à mão: com a=4, b=2, c=3, d=5, o resultado tem que ser -12. Calcule ANTES de rodar.
- CÓDIGO (Testes de regressão automáticos (Linux/macOS/Git Bash). Gere cada .esperado UMA vez, confira à mão, e congele.):
```
#!/bin/bash
# roda_testes.sh, compila cada teste e compara com a saída esperada
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
```
- CAIXA [No mundo real]: Isso é teste de **regressão**: depois de cada otimização ou correção, rode a bateria inteira. É exatamente assim que GCC e LLVM trabalham, milhares de programas de teste rodando a cada mudança.
- CAIXA [Dica de prova]: Prova adora: 'aplique dobramento de constantes e eliminação de código morto neste código de três endereços'. Primeiro dobre, depois veja quais desvios ficaram com condição constante, depois corte o que nunca é alcançado.
- REVELAR: Por que gerar o `.esperado` rodando o próprio compilador é perigoso?
    → Porque se o compilador está errado, você congela o erro como 'resposta certa'. Confira cada `.esperado` à mão (ou calcule antes de rodar) na primeira vez, depois ele protege contra regressões.
- PASSOS (Na sua linguagem):
    · Que otimização simples cabe na sua linguagem: dobrar constantes, cortar código morto?
    · A sua bateria tem pelo menos um teste certo e um com erro para cada comando?
    · Qual é o formato exato de mensagem de erro que o enunciado pede?
- TEXTO: **Teste desta etapa:** a bateria inteira passa:
- CÓDIGO (Teste desta etapa: o comando e o que deve sair):
```
./roda_testes.sh
10 passaram, 0 falharam      # com a sua contagem de testes
```

## Capítulo 10: Checklist de entrega e erros que tiram ponto [ERROR_RECOVERY]
O que conferir na véspera, o que vai no relatório e as perguntas da arguição.

- NARRADOR: Véspera da entrega. 23h47. A geladeira faz um barulho estranho. O cursor pisca.
- BYTE: Tá tudo funcionando! …Eu acho.
- PROF: 'Eu acho' não é teste. Vamos passar o checklist, item por item.
- PASSOS (Checklist do código):
    · Compila do zero, fora da IDE: `javac -d bin src/*.java` sem erros nem avisos estranhos.
    · Roda exatamente como o enunciado manda (ex.: `java Main programa.byt`).
    · Nenhum caminho absoluto (`C:\Users\…`) e nenhum `System.out.println` de depuração esquecido.
    · Mensagens de erro no formato pedido, com a linha certa, e o programa PARA no primeiro erro (se for o pedido).
    · Toda a bateria de testes passa, rode o script uma última vez.
    · O `.asm` gerado monta e roda no MARS sem nenhuma edição manual.
    · README com: como compilar, como rodar, versão do Java.
- PASSOS (Checklist do relatório):
    · Especificação da linguagem: tokens (com regex), precedência, gramática.
    · O AFD do léxico desenhado + a lista de erros léxicos.
    · Gramática LL(1) em EBNF + FIRST/FOLLOW como prova de que não há conflitos.
    · Decisões de projeto: maiúsculas, sombreamento, conversões implícitas, `<-` vs `<` `-`.
    · Esquemas de tradução: o molde MIPS de cada comando (se, enquanto, escreva…).
    · Otimizações implementadas, com exemplo antes/depois.
    · Testes: tabela com programa, saída esperada e saída obtida. E as limitações conhecidas (ex.: 10 registradores).
- CAIXA [Cuidado!]: **Linha errada nas mensagens.** O erro mais comum de todos. Causas: não contar '\n' dentro de comentário de bloco, contar '\r' também, ou usar a linha do token ANTERIOR quando o enunciado quer a do token encontrado (ou vice-versa). Tenha um teste de erro DEPOIS de um comentário de várias linhas.
- CAIXA [Cuidado!]: **Registradores vazando.** Esquecer um `liberaTemp()` não quebra programas pequenos… até o 11º comando, quando aparece 'faltaram registradores' do nada. Teste com um programa de 30 atribuições seguidas.
- CÓDIGO (Uma linha de 'assert' que economiza uma noite de depuração):
```
private void comando() {
    int antes = proxTemp;
    switch (tok.tipo) {
        // ... os mesmos casos de antes ...
    }
    if (proxTemp != antes)               // rede de segurança: pega o bug na hora
        throw new IllegalStateException("BUG: registrador vazou no comando antes da linha " + tok.linha);
}
```
- PREVEJA: Alguém escreveu no léxico: `if (lexema == "senao") …`. O que acontece?
    opções: Funciona sempre | ✔Às vezes não reconhece a palavra: `==` em String compara REFERÊNCIAS, não conteúdo | Erro de compilação do Java | Funciona só no Windows
    explicação: Em Java, `==` entre Strings pergunta 'é o mesmo objeto?'. O lexema montado num StringBuilder é um objeto NOVO, então a comparação falha. Use `equals`: ou, melhor ainda, o HashMap de reservadas e enums como no capítulo 3.
- CAIXA [Cuidado!]: **Rótulos e nomes colidindo.** Dois `se` aninhados gerando o mesmo rótulo, ou uma variável chamada `main`, `j` ou `b` virando rótulo cru. O contador nos rótulos e o sufixo `_N` nas variáveis resolvem, tenha um teste com `inteiro j, b, main;`.
- CAIXA [Cuidado!]: **Aceitar lixo.** `programa t { } xyz`, `escreva(1);;` (dois `;`), `a < b < c`. Cada um desses deveria ser um teste da pasta `erro/` com a mensagem esperada.
- CAIXA [Cuidado!]: **Variável em laço não reinicia.** Na Byteiro, `inteiro t;` dentro de um `enquanto` NÃO gera código: o `.word` é o mesmo a cada volta e guarda o valor da volta anterior. Ou documente isso ('variáveis valem 0 só no início do programa'), ou gere um `sw $zero, rotulo` em cada declaração.
- CAIXA [No mundo real]: Integridade acadêmica: copiar o compilador de um colega, de um semestre anterior ou pedir para uma IA escrever sem entender aparece na arguição em dois minutos, e costuma zerar o trabalho. Use este guia como MAPA: os exemplos são da Byteiro, a sua linguagem é outra, e o código precisa ser seu.
- CAIXA [Dica de prova]: Treine a arguição em voz alta, um integrante perguntando para o outro. As perguntas abaixo são as clássicas.
- REVELAR: Arguição: 'Por que a sua gramática é LL(1)?'
    → Porque não tem recursão à esquerda, está fatorada e, em cada não-terminal com alternativas, os FIRST são disjuntos (e não cruzam com o FOLLOW quando há ε). Mostre a tabela FIRST/FOLLOW do relatório e aponte o `if`/`switch` correspondente no código.
- REVELAR: Arguição: 'Mostre onde o tipo de `a + 'b'` é calculado.'
    → Em `exprAdd`: `primario` devolve INTEIRO para `a` e CARACTERE para `'b'`; os dois são numéricos, então o resultado é INTEIRO. Nenhuma instrução extra: o caractere já é um número na memória.
- REVELAR: Arguição: 'Como o seu léxico sabe que `<-` é um token só?'
    → Estado 3 do AFD: depois de ler '<', ele olha o próximo caractere. Se for '-', consome e devolve ATRIB (maior lexema). Se for outra coisa, devolve MENOR sem consumir.
- REVELAR: Arguição: 'O que acontece com a pilha de escopos quando o bloco do enquanto fecha?'
    → `fechaEscopo()` desempilha o HashMap do bloco: os nomes declarados lá deixam de ser visíveis. Mas o `.word` continua no `.data`: escopo é uma regra de COMPILAÇÃO, não de memória.
- PASSOS (Plano para as últimas 48 horas):
    · 48h: congele funcionalidades. Nada de recurso novo, só correção de bug.
    · 36h: rode a bateria inteira, conserte o que falhar, rode de novo.
    · 24h: relatório: figuras do AFD, gramática, moldes MIPS, tabela de testes.
    · 12h: clone o repositório numa pasta limpa e siga o README como se fosse o professor.
    · 2h: entregue. Sim, antes do prazo. O Canvas também cai.
- LEXI: Tokens cortados.
- PILHA: Gramática… empilhada.
- PROF: Tipos conferidos, MIPS rodando, testes passando. Você não 'fez um trabalho de compiladores', você **construiu um compilador**. Pouca gente pode dizer isso.
- REVELAR: Última recuperação: recite as fases do seu compilador e a classe Java de cada uma.
    → Léxico → `Lexico` (AFD com switch). Sintático → `Sintatico` (descida recursiva, casaToken). Semântico → `TabelaSimbolos` + checagens no `Sintatico`. Geração → métodos do `Sintatico` emitindo MIPS. Otimização → dobramento de constantes e código morto no gerador. E o `Main` costurando tudo.
- PASSOS (Na sua linguagem):
    · O enunciado pede relatório? Com quais seções?
    · Como o professor vai rodar o seu compilador: que comando, em que pasta?
    · Que três perguntas da arguição você treinaria sobre a SUA gramática?
- TEXTO: **Teste desta etapa:** numa pasta nova, clone o repositório e siga só o README: compilar, rodar um teste e abrir no MARS. Se faltar qualquer passo que não está escrito, o README está incompleto.
