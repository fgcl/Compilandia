# Região 8: Cidade Semântica — Significado, tipos e escopos
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO AST_BUILD: Árvore sintática abstrata (AST) com classes
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A árvore enxuta» → Fazer com ajuda → Pegadinha «O parêntese não vira nó» → Desafio
Jogos que treinam: Linha de Montagem TAC (montar código de três endereços), Fábrica de AST (montar a AST com new BinOp(...))
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug podou a árvore
Na tela: ( a + b )
BYTE: "Antes da aula: os parênteses entram na AST?"
Opções: sim | ✔não | só um
Depois: "Não. A forma da árvore já mostra o agrupamento."

#### AULA ANIMADA BÁSICA: "A árvore enxuta" (22 cenas, ~155s)

1. TELA: # AST ⏎ a + ( b )
   BYTE: "O parser segue a árvore de derivação. Ela tem um nó para cada símbolo das regras usadas, e fica enorme."

2. TELA: # AST ⏎ *a + ( b )* ⏎ a ⏎ + ⏎ ( ⏎ b ⏎ )
   BYTE: "Destes cinco pedaços, quais mudam o resultado da conta? Toque neles."
   PERGUNTA (toque): certo = a | + | b ; errado = ( | )
   se errar: "Tire os parênteses. A conta muda?"
   se acertar: "O a, o mais e o b. Os parênteses em volta do b não mudam nada."

3. TELA: # AST ⏎ a + ( b ) ⏎ *a* ⏎ *+* ⏎ *b*
   BYTE: "As fases seguintes só precisam saber qual é a operação e quais são os operandos."

4. TELA: !!AST!!
   BYTE: "A árvore que guarda só isso é a árvore sintática abstrata, a AST."

5. TELA: # a + ( b ) ⏎ E ⏎ E ⏎ id ⏎ + ⏎ T ⏎ ( ⏎ E ⏎ id ⏎ )
   BYTE: "A árvore de derivação guarda tudo: parênteses, e até as regras que só passam de um não-terminal para outro."

6. TELA: # a + ( b ) ⏎ E ⏎ E ⏎ id ⏎ + ⏎ T ⏎ ( ⏎ E ⏎ id ⏎ )
   BYTE: "Tem nó sobrando aqui. Toque nos dois parênteses: eles não mudam a conta."
   PERGUNTA (toque): certo = ( | ) ; errado = E | id
   se errar: "A árvore já mostra a ordem. O parêntese fez o trabalho dele."
   se acertar: "Pontuação some da árvore abstrata."

7. TELA: # a + ( b ) ⏎ BinOp + ⏎ a ⏎ b
   BYTE: "A AST guarda só o que tem significado: o operador no nó, e os operandos como filhos."

8. TELA: # a + ( b ) ⏎ BinOp + ⏎ a ⏎ b ⏎ *new BinOp('+', a, b)*
   BYTE: "No código, cada nó da AST é um objeto, criado pelo parser na hora em que reconhece a regra."

9. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ 3. Parêntese e regra de passagem somem.
   BYTE: "A receita para montar a AST tem três regras."

10. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ 3. Parêntese e regra de passagem somem.
   BYTE: "Na árvore, cada nome virou folha. Qual regra é essa? Toque."
   PERGUNTA (toque): certo = 2. Valor ou nome vira folha. ; errado = 1. Operador vira nó; operandos viram filhos. | 3. Parêntese e regra de passagem somem.
   se errar: "Nome não tem filhos."
   se acertar: "Exato. Regra dois: valor e nome viram folha."

11. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ *3. Parêntese e regra de passagem somem.*
   BYTE: "Regra três: o parêntese só servia para agrupar. Na AST, a própria forma da árvore já mostra o agrupamento."

12. TELA: # AST ⏎ total * ( preco - 2 )
   BYTE: "Agora monte a AST de total vezes, abre parêntese, preço menos dois."

13. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ * ⏎ - ⏎ (
   BYTE: "Qual operador fica na raiz? Toque nele."
   PERGUNTA (toque): certo = * ; errado = - | (
   se errar: "O parêntese faz o menos acontecer antes. Quem fica por último é a raiz."
   se acertar: "O vezes é a última conta, então vai na raiz."

14. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ * ⏎ total ⏎ - ⏎ preco ⏎ 2 ⏎ total
   BYTE: "O menos fica embaixo do vezes. Quais são os filhos do menos? Toque."
   PERGUNTA (toque): certo = preco | 2 ; errado = total
   se errar: "São os dois lados do menos."
   se acertar: "Preço e dois são folhas do menos."

15. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ *** ⏎ total ⏎ *-* ⏎ preco ⏎ 2
   BYTE: "Sem parênteses na árvore, e a ordem continua certa: o filho sai antes do pai."

16. TELA: # AST ⏎ a ⏎ - ⏎ b ⏎ - ⏎ c
   BYTE: "Agora a menos b menos c. Na linha, os dois menos parecem iguais."

17. TELA: # AST ⏎ a ⏎ - ⏎ b ⏎ - ⏎ c ⏎ um nó só, com três filhos
   BYTE: "Qual dos dois menos fica embaixo na AST? Toque nele."
   PERGUNTA (toque): certo = - ; errado = - | um nó só, com três filhos
   se errar: "Na conta da escola, qual menos você faz primeiro?"
   se acertar: "O da esquerda. A menos b sai primeiro, e o c sai do resultado."

18. TELA: # AST ⏎ a ⏎ *-* ⏎ b ⏎ - ⏎ c ⏎ - ⏎ - ⏎ a ⏎ b ⏎ c ⏎ 10 - 3 - 2 = 5
   BYTE: "Com dez, três e dois, esta árvore dá cinco. Pendurada para a direita, daria nove."

19. TELA: # Diga por quê ⏎ total * (preco - 2) ⏎ O menos roda antes, e o vezes usa o resultado. ⏎ O menos tem mais precedência que o vezes. ⏎ Menos sempre fica embaixo de vezes, em toda conta.
   BYTE: "Na conta do preço, o menos ficou embaixo do vezes. Por quê?"
   PERGUNTA (toque): certo = O menos roda antes, e o vezes usa o resultado. ; errado = O menos tem mais precedência que o vezes. | Menos sempre fica embaixo de vezes, em toda conta.
   se errar: "Qual conta precisa sair primeiro para o vezes funcionar?"
   se acertar: "O parêntese manda fazer o menos antes. O vezes usa o resultado dele."

20. TELA: # Erros comuns ⏎ A AST de a * (b - 2) não tem nó para os parênteses. ⏎ Escolha o veredito com o motivo certo. ⏎ Certo: a forma da árvore já agrupa. ⏎ Certo: parêntese nunca muda a conta. ⏎ Errado: sem o nó, o menos roda depois.
   BYTE: "Os parênteses aparecem na AST? Um colega desenhou sem eles. Certo ou errado?"
   PERGUNTA (toque): certo = Certo: a forma da árvore já agrupa. ; errado = Certo: parêntese nunca muda a conta. | Errado: sem o nó, o menos roda depois.
   se errar: "O parêntese muda a forma da árvore. Depois disso, ele ainda serve?"
   se acertar: "Boa. O parêntese só agrupa. Na AST, o agrupamento já está na forma: o menos fica embaixo do vezes."

21. TELA: # Erros comuns ⏎ Certo: A AST de a * (b - 2) não tem nó para os parênteses. ⏎ *Errado: Pôr na raiz o primeiro operador que aparece na expressão.* ⏎ *Certo: Na raiz fica a última operação a ser calculada.*
   BYTE: "Um erro comum: pôr na raiz o primeiro operador da linha. A raiz é a última conta. A primeira conta fica no fundo da árvore."

22. TELA: # Resumo ⏎ Árvore de derivação: um nó por regra. AST: só operações e operandos. ⏎ Operador vira nó interno; nome e valor viram folha. ⏎ Parêntese e regra de passagem somem. O filho é calculado antes do pai.
   BYTE: "Resumindo: a AST é a árvore de derivação sem o que só serve à gramática. Ficam os operadores, os operandos e a ordem das contas."

#### PEGADINHA: "O parêntese não vira nó" (6 cenas, ~32s)

1. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c
   BYTE: "Esta conta tem parênteses. Eles mudam a ordem da conta."

2. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *(* ⏎ *)* ⏎ E
   BYTE: "Na árvore de derivação, cada parêntese aparece como folha."

3. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *** ⏎ *+* ⏎ c ⏎ a ⏎ b
   BYTE: "Na AST eles somem. A forma da árvore já guarda a ordem."

4. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ * ⏎ + ⏎ c ⏎ a ⏎ b ⏎ * ⏎ + ⏎ ( ⏎ )
   BYTE: "Quais destes viram nós na árvore? Toque em todos."
   PERGUNTA (toque): certo = * | + ; errado = ( | )
   se errar: "A AST guarda operações e valores. Sinal de agrupar não vira nó."
   se acertar: "Vezes e mais viram nós. Os parênteses somem."

5. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *** ⏎ *+* ⏎ c ⏎ a ⏎ b
   BYTE: "O mais fica embaixo do vezes. É assim que a AST lembra do parêntese."

6. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ * ⏎ + ⏎ c ⏎ a ⏎ b
   PROF: "Na AST fica o que o programa faz, não como foi escrito."

#### CLIPE 30s: "AST ou árvore de derivação" (2 cenas, ~10s)

1. TELA: # (a + b) ⏎ ( E )   E + T   T   F
   BYTE: "A árvore de derivação guarda cada regra usada."

2. TELA: # (a + b) ⏎ a + b
   BYTE: "A AST guarda só o que importa para o significado."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Árvore Gorda — mentira: "AST é a árvore de derivação inteira." / verdade: "AST guarda só o que importa. Parênteses e regras de passagem somem."
    · [V] ( a + b ) não tem nó de parêntese na AST. → Isso mesmo.
    · [V] A forma da AST já mostra a precedência. → Isso mesmo.
    · [F] A AST tem mais nós que a árvore de derivação. → Ela é mais enxuta.

#### FICHA DO ASSUNTO
Em uma frase: A árvore sintática abstrata guarda só operadores, operandos e construções (BinOp, Var, Num, Atrib, Se, Enquanto, Bloco), jogando fora correntes de não-terminais, parênteses, pontuação e ε; o parser a constrói (new BinOp no descendente ou $$ = new BinOp(...) no Bison) e as fases seguintes andam nela.
Gancho: AST = Árvore Sem Tralha: corta o CPE (Correntes, Parênteses, Epsilons). Filho trabalha antes do pai.
Termos-chave: abstrat, arvore, classe, binop, parentes, precedenc, parser
Erros comuns: Criar nós para parênteses ou para não-terminais de corrente (E → T → F) // Montar a AST pendurada para a direita (recursão em vez de laço) e inverter a associatividade de - e / // Achar que sem parênteses a precedência se perde (ela mora na forma da árvore) // Confundir AST (estrutura em árvore) com TAC (lista de instruções gerada a partir dela) // Esquecer $$ = $2 na regra do parêntese ou $$ = $1 nas correntes do Bison
Flashcards:
    · P: O que a AST joga fora? → R: Correntes de não-terminais, parênteses/pontuação e ε
    · P: Nós da derivação × AST em a + b * c? → R: 13 × 5
    · P: Onde mora a precedência na AST? → R: Na forma: o filho é calculado antes do pai
    · P: Linha-chave da associatividade à esquerda no descendente? → R: e = new BinOp(op, e, termo()); dentro do while
    · P: Ação Bison para E → E + T? → R: $$ = new BinOp('+', $1, $3);
    · P: Ação Bison para F → ( E )? → R: $$ = $2; (o parêntese some)
    · P: Classes de comando típicas? → R: Atrib, Se, Enquanto, Bloco (estendem Cmd)
    · P: Vantagem da AST sobre gerar código direto no parser? → R: Várias passadas (tipos, depois código), fases separadas e testáveis, otimização na árvore
    · P: Nós da AST com n operadores binários? → R: 2n + 1

#### GLOSSÁRIO (balão ao tocar no termo)
- AST: Árvore enxuta: guarda as operações e os valores, sem parênteses nem regras.


## ASSUNTO SCOPE_SYMBOLS: Escopo e tabela de símbolos
Pré-requisitos no jogo: Fases de um compilador
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Quem é esse x?» → Programar o Byte → Prática: você pilota «Funções na tabela» → Pegadinha «A sombra da variável» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Esconde-Esconde do Escopo (ligar cada uso à sua declaração)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: Carteiro do escopo

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug escondeu um x
Na tela: int x; { int x; x = 1; }
BYTE: "Antes da aula: esse x igual a um mexe em qual x?"
Opções: ✔o de dentro | o de fora | nos dois
Depois: "No de dentro. A busca começa pelo bloco mais próximo."

#### AULA ANIMADA BÁSICA: "Quem é esse x?" (23 cenas, ~167s) [1º exemplo começa na cena 5]

1. TELA: # Escopo ⏎ void f() { ⏎   int i; ⏎ } ⏎ void g() { ⏎   float i; ⏎ }
   BYTE: "Duas funções declaram uma variável i. Cada uma quer a sua."

2. TELA: # Escopo ⏎ void f() { ⏎   int i; ⏎ } ⏎ *void g() {* ⏎   float i; ⏎ }
   BYTE: "Um uso de i dentro de g fala de qual declaração? Toque."
   PERGUNTA (toque): certo =   float i; ; errado =   int i; | *void g() {*
   se errar: "Procure a declaração que está entre as chaves de g."
   se acertar: "O float i, declarado dentro de g. O int i de f não vale ali."

3. TELA: # Escopo ⏎ void f() { ⏎ *  int i;* ⏎ } ⏎ void g() { ⏎ *  float i;* ⏎ }
   BYTE: "Em cada uso do i, o compilador precisa achar a declaração certa."

4. TELA: !!ESCOPO!!
   BYTE: "O escopo de uma declaração é o trecho do programa onde ela vale. Em C, ela vale da declaração até o fecha-chave do bloco."

5. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ int main() { x = 2; } ⏎ global: x int
   BYTE: "O mesmo nome pode existir em dois escopos. Quem guarda cada declaração, com nome e tipo, é a tabela de símbolos."

6. TELA: # Escopo ⏎ int x; ⏎ *void f() {* ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ int main() { x = 2; } ⏎ global: x int ⏎ f: x float
   BYTE: "Para escopos aninhados, o compilador usa uma pilha de tabelas. Entrou num bloco, empilha uma tabela nova."

7. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎ *  x = 1.5;* ⏎ } ⏎ int main() { x = 2; } ⏎ global: x int ⏎ f: x float
   BYTE: "Na linha x igual a um e meio, qual x vale? Toque na tabela certa."
   PERGUNTA (toque): certo = f: x float ; errado = global: x int
   se errar: "A busca começa na tabela do topo."
   se acertar: "O x de dentro esconde o de fora."

8. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ int main() { x = 2; } ⏎ *global: x int* ⏎ *f: x float*
   BYTE: "A busca começa na tabela do topo e desce, bloco por bloco, até achar o nome. Se não achar em nenhuma, é erro semântico."

9. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ *}* ⏎ int main() { x = 2; } ⏎ global: x int
   BYTE: "Quando o bloco acaba, a tabela dele sai da pilha. Os nomes dele deixam de valer."

10. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ *int main() { x = 2; }* ⏎ *global: x int* ⏎ *main: (sem x)*
   BYTE: "No main, x igual a dois. A tabela do main não tem x, então a busca desce e acha o x global."

11. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote no topo. Repetido ali: erro. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ 4. Fechou bloco: desempilhe a tabela dele.
   BYTE: "Cada bloco tem a sua tabela. A receita cuida da pilha delas."

12. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote no topo. Repetido ali: erro. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ 4. Fechou bloco: desempilhe a tabela dele.
   BYTE: "O x de dentro escondeu o de fora. Que regra fez isso? Toque."
   PERGUNTA (toque): certo = 3. Uso: busque do topo. Não achou, erro. ; errado = 1. Abriu bloco: empilhe uma tabela vazia. | 2. Declarou: anote no topo. Repetido ali: erro. | 4. Fechou bloco: desempilhe a tabela dele.
   se errar: "Pense em onde a busca começa."
   se acertar: "Boa. A busca para no primeiro x que acha."

13. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote no topo. Repetido ali: erro. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ *4. Fechou bloco: desempilhe a tabela dele.*
   BYTE: "Regra quatro: o bloco fechou, e os nomes declarados nele somem juntos."

14. TELA: # Receita ⏎ *2. Declarou: anote no topo. Repetido ali: erro.* ⏎ { ⏎   int y; ⏎ *  float y;* ⏎ } ⏎ topo: y int ⏎ Erro ⏎ esconde o int ⏎ y vira float
   BYTE: "No mesmo bloco, o código declara y de novo. O que acontece? Toque."
   PERGUNTA (toque): certo = Erro ; errado = esconde o int | y vira float
   se errar: "A tabela do topo já tem um y. Leia a regra dois."
   se acertar: "Erro de redeclaração. No mesmo bloco, cada nome só pode ter uma declaração."

15. TELA: # Escopo ⏎ *{* ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎   } ⏎   print(x); ⏎ } ⏎ fora: z int, x float
   BYTE: "Agora a busca é sua. O bloco de fora já tem a tabela dele."

16. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎ *    x = x + 1;* ⏎     z = z + 1; ⏎   } ⏎   print(x); ⏎ } ⏎ fora: z int, x float ⏎ dentro: x float
   BYTE: "Entrou o bloco de dentro. Na linha acesa, qual tabela responde?"
   PERGUNTA (toque): certo = dentro: x float ; errado = fora: z int, x float
   se errar: "A busca começa no topo. O nome está lá?"
   se acertar: "A tabela de dentro tem esse nome."

17. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎ *    z = z + 1;* ⏎   } ⏎   print(x); ⏎ } ⏎ fora: z int, x float ⏎ dentro: x float
   BYTE: "E nesta linha acesa? Toque na tabela onde a busca para."
   PERGUNTA (toque): certo = fora: z int, x float ; errado = dentro: x float
   se errar: "O topo não tem esse nome. A busca desce."
   se acertar: "Só a tabela de fora conhece esse nome."

18. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎ *  }* ⏎   print(x); ⏎ } ⏎ fora: z int, x float ⏎ dentro: x float
   BYTE: "Chegou a chave que fecha o bloco. Qual tabela sai da pilha?"
   PERGUNTA (toque): certo = dentro: x float ; errado = fora: z int, x float
   se errar: "Regra quatro: sai a tabela do bloco que fechou."
   se acertar: "Acertou. Sai a tabela de dentro."

19. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎   } ⏎ *  print(x);* ⏎ } ⏎ *fora: z int, x float*
   BYTE: "O print usa a única tabela que sobrou, a de fora."

20. TELA: # Diga por quê ⏎ dentro: x float fora: z int, x float ⏎ O x de fora foi apagado quando o bloco abriu. ⏎ A busca sempre termina na tabela global. ⏎ A busca começa no bloco de dentro.
   BYTE: "No x igual a x mais um, a busca parou na tabela de dentro. Por quê?"
   PERGUNTA (toque): certo = A busca começa no bloco de dentro. ; errado = O x de fora foi apagado quando o bloco abriu. | A busca sempre termina na tabela global.
   se errar: "Em que tabela a busca começa?"
   se acertar: "A busca começa no topo da pilha, o bloco de dentro. O x está ali, então ela para."

21. TELA: # Erros comuns ⏎ Com um x fora, posso declarar outro x num bloco interno: o de dentro esconde o de fora. ⏎ Escolha o veredito com o motivo certo. ⏎ Certo: é outro escopo, e a busca acha o de dentro. ⏎ Certo: o x de fora é apagado. ⏎ Errado: um nome só pode ser declarado uma vez.
   BYTE: "Já existe um x fora. Posso declarar outro x dentro do bloco? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = Certo: é outro escopo, e a busca acha o de dentro. ; errado = Certo: o x de fora é apagado. | Errado: um nome só pode ser declarado uma vez.
   se errar: "A declaração nova entra na tabela do topo, que ainda não tem x."
   se acertar: "Em outro escopo pode. O x de dentro esconde o de fora. Erro é declarar duas vezes no mesmo bloco."

22. TELA: # Erros comuns ⏎ Certo: Com um x fora, posso declarar outro x num bloco interno: o de dentro esconde o de fora. ⏎ *Errado: Buscar o nome só na tabela do topo.* ⏎ *Certo: A busca desce do topo até a tabela global. Erro só se nenhuma tiver.*
   BYTE: "Um erro comum: procurar só no bloco atual. A busca desce por todas as tabelas da pilha, do bloco mais interno até o global."

23. TELA: # Resumo ⏎ Tabela de símbolos: nome, tipo e onde cada coisa foi declarada. ⏎ Uma tabela por bloco, numa pilha. Abriu bloco, empilha; fechou, desempilha. ⏎ Uso: busca do topo para baixo. O mais interno esconde o de fora.
   BYTE: "Resumindo: o escopo diz onde cada declaração vale. A pilha de tabelas de símbolos resolve cada nome, sempre do bloco mais interno para o mais externo."

#### PEGADINHA: "A sombra da variável" (5 cenas, ~27s)

1. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ print(x);
   BYTE: "Dois x, um fora e um dentro. O de dentro faz sombra no de fora."

2. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎ *  print(x);* ⏎ } ⏎ print(x);
   BYTE: "O print de dentro enxerga qual x? Toque na declaração."
   PERGUNTA (toque): certo =   int x = 2; ; errado = int x = 1;
   se errar: "A busca começa no bloco mais interno."
   se acertar: "Ele acha o x de dentro e para ali."

3. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ *print(x);*
   BYTE: "O bloco fechou. O print de fora enxerga qual x?"
   PERGUNTA (toque): certo = int x = 1; ; errado =   int x = 2;
   se errar: "Quando o bloco fecha, os nomes dele somem."
   se acertar: "É isso. Sobrou só o de fora, que nunca mudou."

4. TELA: # Dois x ⏎ *int x = 1;* ⏎ { ⏎ *  int x = 2;* ⏎   print(x); ⏎ } ⏎ print(x);
   PROF: "A pegadinha é achar que x igual a dois mudou o x de fora. É outra variável."

5. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ print(x); ⏎ 2 ⏎ 1
   BYTE: "Saída do programa: dois, depois um."

#### PRATICA: "Funções na tabela" (7 cenas, ~63s)

1. TELA: # Funções na tabela ⏎ int soma(int a, int b) { ⏎   int s = a + b; ⏎   return s; ⏎ } ⏎ void main() { ⏎   int x = soma(1, 2); ⏎   int y = soma(x); ⏎   int z = soma(x, y); ⏎ }
   BYTE: "Até aqui, a tabela de símbolos guardava variáveis. Agora o programa tem funções. Cada função abre uma tabela própria, filha da global."

2. TELA: # Funções na tabela ⏎ *int soma(int a, int b) {* ⏎   int s = a + b; ⏎   return s; ⏎ } ⏎ void main() { ⏎   int x = soma(1, 2); ⏎   int y = soma(x); ⏎   int z = soma(x, y); ⏎ } ⏎ global ⏎ main ⏎ soma
   BYTE: "Os parâmetros a e b entram em qual tabela? Toque."
   PERGUNTA (toque): certo = soma ; errado = global | main
   se errar: "Quem pode usar o a? Só o código de dentro da soma."
   se acertar: "Na tabela da soma, junto com o s. Fora da soma, ninguém enxerga o a nem o b."

3. TELA: # Funções na tabela ⏎ global{soma: int (int, int); main: void ()} ⏎ soma{a: int, parâmetro; b: int, parâmetro; s: int} ⏎ main{x: int; y: int; z: int}
   PROF: "A função entra na tabela global. A entrada guarda o tipo que ela devolve e os tipos dos parâmetros. Os parâmetros entram na tabela da própria função."

4. TELA: # Funções na tabela ⏎ *global{soma: int (int, int); main: void ()}* ⏎ soma{a: int, parâmetro; b: int, parâmetro; s: int} ⏎ *main{x: int; y: int; z: int}* ⏎ O a da tabela da soma ⏎ Um a na tabela global ⏎ Nada: o compilador dá erro
   BYTE: "Dentro do main, alguém escreve x igual a a. O que a busca pelo a encontra? Toque."
   PERGUNTA (toque): certo = Nada: o compilador dá erro ; errado = O a da tabela da soma | Um a na tabela global
   se errar: "A busca sai do main e sobe para o pai. A tabela da soma está nesse caminho?"
   se acertar: "Do main, a busca sobe para a global, e lá não há nenhum a. A tabela da soma é irmã da tabela do main, e não mãe. Então o compilador dá erro."

5. TELA: # Funções na tabela ⏎ *global{soma: int (int, int); main: void ()}* ⏎ int x = soma(1, 2); ⏎ int y = soma(x); ⏎ int z = soma(x, y);
   BYTE: "Numa chamada, o compilador procura a função na tabela global. Depois compara a entrada dela com o que a chamada manda. O que a chamada manda são os argumentos."

6. TELA: # Funções na tabela ⏎ global{soma: int (int, int); main: void ()} ⏎ int x = soma(1, 2); ⏎ int y = soma(x); ⏎ int z = soma(x, y);
   BYTE: "Qual chamada o compilador recusa? Toque."
   PERGUNTA (toque): certo = int y = soma(x); ; errado = int x = soma(1, 2); | int z = soma(x, y);
   se errar: "Conte os argumentos e compare com a entrada da soma."
   se acertar: "A soma pede dois parâmetros, e essa chamada manda um argumento só. A quantidade não bate."

7. TELA: # Funções na tabela ⏎ *global{soma: int (int, int); main: void ()}* ⏎ int x = soma(1, 2); ⏎ *int y = soma(x);* ⏎ int z = soma(x, y);
   PROF: "Por isso a função guarda os tipos dos parâmetros na tabela global. Sem essa entrada, o compilador não teria com o que comparar a chamada."

#### CLIPE 30s: "Tabela de símbolos" (2 cenas, ~9s)

1. TELA: # Tabela de símbolos ⏎ x ⏎ int ⏎ global ⏎ y ⏎ float ⏎ local
   BYTE: "A tabela guarda cada nome, com o tipo e o escopo."

2. TELA: # Tabela de símbolos ⏎ *x* ⏎ int ⏎ global ⏎ y ⏎ float ⏎ local
   BYTE: "Todo uso de variável passa por ela."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Escopo Vazado — mentira: "Variável de dentro do bloco vale fora dele." / verdade: "Cada bloco abre um escopo. Ao fechar, os nomes dele somem."
    · [V] A busca começa pelo escopo mais interno. → Isso mesmo.
    · [V] Declarar x dentro esconde o x de fora. → Isso mesmo.
    · [F] Usar variável não declarada é erro léxico. → É erro semântico.

#### FICHA DO ASSUNTO
Em uma frase: A tabela de símbolos guarda nome, tipo e categoria; com escopo estático, ela é uma pilha de tabelas: entra no bloco empilha, sai desempilha, e a busca vai do topo para a base.
Gancho: Escopo é cebola: procure de dentro para fora. Abre chave empilha, fecha chave desempilha.
Termos-chave: escopo, pilha, tabela de símbolos, aninhamento, sombreamento, declaração
Erros comuns: Buscar da base para o topo (o certo é do topo, mais interno, para a base) // Confundir sombreamento (escopo interno) com redeclaração (mesmo escopo) // Achar que o léxico decide a qual declaração um nome se refere // Continuar vendo variáveis de um bloco que já foi fechado // Confundir escopo (onde é visível) com tempo de vida (quando existe)
Flashcards:
    · P: Regra do escopo estático? → R: Vale a declaração do bloco mais interno que contém o uso
    · P: Ao entrar num bloco, a tabela... → R: Empilha uma tabela nova
    · P: Ao sair do bloco... → R: Desempilha (descarta) a tabela do topo
    · P: A busca de um nome começa onde? → R: Na tabela do topo (bloco mais interno)
    · P: Redeclaração é checada em qual tabela? → R: Só na do topo
    · P: O que é sombreamento? → R: Declaração interna escondendo uma externa de mesmo nome
    · P: Nome não achado em nenhuma tabela? → R: Erro: variável não declarada
    · P: Escopo dinâmico decide pelo quê? → R: Pela cadeia de chamadas em execução

#### GLOSSÁRIO (balão ao tocar no termo)
- escopo: O trecho do programa onde um nome vale.
- tabela de símbolos: A lista que o compilador guarda com cada nome, seu tipo e seu escopo.


## ASSUNTO ATTRIBUTES_TYPES: Atributos e verificação de tipos
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Tipos sobem pela árvore» → Fazer com ajuda → Prática: você pilota «Suba os tipos» → Prática: você pilota «O tipo que desce» → Prática: você pilota «Tipos nos comandos» → Prática: você pilota «O herdado na descida» → Pegadinha «Sintetizado sobe, herdado desce» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Árvore Anotada (calcular atributos de baixo para cima), Fábrica de AST (montar a AST com new BinOp(...))
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug apagou um tipo
Na tela: 2 + 1.5
BYTE: "Antes da aula: qual é o tipo do resultado?"
Opções: int | ✔float | erro
Depois: "Float. O compilador converte o inteiro antes da conta."

#### AULA ANIMADA BÁSICA: "Tipos sobem pela árvore" (30 cenas, ~220s) [1º exemplo começa na cena 7]

1. TELA: # Forma certa, sentido errado ⏎ a + ok ⏎ a: int ⏎ ok: bool ⏎ nome + nome: o parser aceita
   BYTE: "O parser confere a forma. A mais ok tem a forma certa: nome, mais, nome. Mas ok é um bool, verdadeiro ou falso."

2. TELA: # Forma certa, sentido errado ⏎ *a + ok* ⏎ a: int ⏎ ok: bool ⏎ nome + nome: o parser aceita ⏎ scanner ⏎ parser ⏎ semântica
   BYTE: "Somar número com verdadeiro ou falso não faz sentido. Quem acusa esse erro? Toque."
   PERGUNTA (toque): certo = semântica ; errado = scanner | parser
   se errar: "O scanner e o parser só olham a forma. A forma aqui está certa."
   se acertar: "A análise semântica. Ela confere o sentido, e é o assunto desta região. O primeiro passo é saber o tipo de cada pedaço."

3. TELA: # Tipos sobem pela árvore ⏎ a + x ⏎ add ⏎ add.s ⏎ int + int ⏎ float + float
   BYTE: "Na máquina, somar inteiros e somar reais são instruções diferentes."

4. TELA: # Tipos sobem pela árvore ⏎ *a + x* ⏎ add ⏎ add.s ⏎ int + int ⏎ float + float ⏎ sub.s ⏎ float - float
   BYTE: "Se a e x forem float, qual instrução faz essa soma? Toque."
   PERGUNTA (toque): certo = add.s ; errado = add | sub.s
   se errar: "Leia o que está embaixo de cada instrução. Procure soma de float."
   se acertar: "O add ponto s. A instrução muda com o tipo, então o compilador precisa conhecer o tipo antes."

5. TELA: # Tipos sobem pela árvore ⏎ *a + x* ⏎ *add* ⏎ *add.s* ⏎ int + int ⏎ float + float ⏎ *sub.s* ⏎ float - float
   BYTE: "Para escolher a instrução certa, o compilador precisa saber o tipo de cada pedaço da expressão."

6. TELA: !!ATRIBUTO!!
   BYTE: "Um atributo é uma informação associada a um nó da árvore, como o tipo ou o valor. As regras que o calculam vêm junto com a gramática."

7. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ * ⏎ x ⏎ 2 ⏎ a é int, x é float
   BYTE: "A árvore já existe. Agora a análise semântica calcula os atributos dela. Aqui, o atributo é o tipo."

8. TELA: # a + x * 2 ⏎ *+* ⏎ a ⏎ *** ⏎ x ⏎ 2 ⏎ a é int, x é float
   BYTE: "Repare na árvore: o operador fica no nó, e não há E, T nem F. É a árvore enxuta que você montou na aula de AST."

9. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ * ⏎ x ⏎ *2* ⏎ a é int, x é float ⏎ float ⏎ int ⏎ bool
   BYTE: "O a é int, e o x é float. E o dois, sem ponto? Toque no tipo dele."
   PERGUNTA (toque): certo = int ; errado = float | bool
   se errar: "Número sem ponto não tem parte decimal."
   se acertar: "Inteiro. Um número escrito sem ponto é do tipo int."

10. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ * ⏎ x ⏎ *2* ⏎ a é int, x é float ⏎ int ⏎ float ⏎ int
   BYTE: "Começo pelas folhas. O tipo de a e de x vem da declaração. O dois, sem ponto, é inteiro."

11. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ *** ⏎ x ⏎ 2 ⏎ a é int, x é float ⏎ int ⏎ float ⏎ int ⏎ int ⏎ float ⏎ Erro
   BYTE: "Real vezes inteiro dá o quê? Toque no tipo."
   PERGUNTA (toque): certo = float ; errado = int | Erro
   se errar: "Inteiro com real: qual dos dois guarda a parte quebrada?"
   se acertar: "Inteiro com real dá real."

12. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ * ⏎ x ⏎ 2 ⏎ a é int, x é float ⏎ int ⏎ float ⏎ int ⏎ float
   BYTE: "O dois é convertido para real antes da conta. Essa conversão automática se chama coerção. O tipo real sobe para o nó do vezes."

13. TELA: # a + x * 2 ⏎ *+* ⏎ a ⏎ * ⏎ x ⏎ 2 ⏎ a é int, x é float ⏎ int ⏎ float ⏎ int ⏎ float ⏎ float
   BYTE: "Na raiz, inteiro mais real também dá real. Atributo que o nó calcula a partir dos filhos é um atributo sintetizado."

14. TELA: # a + x * 2 ⏎ + ⏎ a ⏎ * ⏎ x ⏎ 2 ⏎ a é int, x é float ⏎ int ⏎ float ⏎ int ⏎ float ⏎ float
   PROF: "Se os tipos não combinam e não há coerção possível, o compilador acusa erro semântico."

15. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ 3. Conta: um filho float dá float. Senão, int. ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "A verificação de tipos sobe das folhas até a raiz, um nó por vez."

16. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ 3. Conta: um filho float dá float. Senão, int. ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "No primeiro exemplo, o x era float. Que regra disse isso? Toque."
   PERGUNTA (toque): certo = 2. Variável: o tipo vem da tabela de símbolos. ; errado = 1. Número com ponto é float. Sem ponto, int. | 3. Conta: um filho float dá float. Senão, int. | 4. Comparação dá bool. Bool numa conta é erro.
   se errar: "O x é uma variável. Onde mora o tipo dela?"
   se acertar: "Certo. A declaração do x ficou na tabela."

17. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ *3. Conta: um filho float dá float. Senão, int.* ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "No passo três, o int que encontra um float sofre coerção: vira float antes da conta."

18. TELA: # c * 2 + y ⏎ + ⏎ * ⏎ c ⏎ 2 ⏎ y ⏎ int ⏎ int ⏎ float
   BYTE: "Outra conta. As folhas já têm tipo, pelas regras um e dois."

19. TELA: # c * 2 + y ⏎ + ⏎ *** ⏎ c ⏎ 2 ⏎ y ⏎ int ⏎ int ⏎ float ⏎ int ⏎ float ⏎ bool
   BYTE: "Qual é o tipo de c vezes dois? Toque."
   PERGUNTA (toque): certo = int ; errado = float | bool
   se errar: "Os dois filhos são int. Nenhum é float."
   se acertar: "Inteiro com inteiro fica inteiro."

20. TELA: # c * 2 + y ⏎ *+* ⏎ * ⏎ c ⏎ 2 ⏎ y ⏎ int ⏎ int ⏎ float ⏎ int ⏎ float ⏎ bool ⏎ int
   BYTE: "E na raiz, inteiro mais real? Toque no tipo."
   PERGUNTA (toque): certo = float ; errado = int | bool
   se errar: "Regra três: basta um filho float."
   se acertar: "O inteiro vira real antes da soma."

21. TELA: # c * 2 + y ⏎ + ⏎ * ⏎ c ⏎ 2 ⏎ *ok* ⏎ int ⏎ int ⏎ bool ⏎ int ⏎ int ⏎ float ⏎ erro
   BYTE: "Agora o y deu lugar ao ok, que é bool. Que tipo sobe?"
   PERGUNTA (toque): certo = erro ; errado = int | float
   se errar: "Regra quatro: bool não entra em conta."
   se acertar: "Exato. A raiz fica com erro de tipo."

22. TELA: # c * 2 + y ⏎ *+* ⏎ * ⏎ c ⏎ 2 ⏎ ok ⏎ int ⏎ int ⏎ bool ⏎ int ⏎ erro
   BYTE: "O erro sobe até a raiz, e o compilador avisa."

23. TELA: # A regra escrita ⏎ *E → E1 + T    E.val = E1.val + T.val* ⏎ E → T         E.val = T.val ⏎ T → num       T.val = num.val ⏎ E ⏎ E ⏎ T ⏎ 2 ⏎ + ⏎ T ⏎ 3 ⏎ = 2 ⏎ = 3
   BYTE: "Na prova, cada regra da gramática vem com a sua conta ao lado."

24. TELA: # A regra escrita ⏎ *E → E1 + T    E.val = E1.val + T.val* ⏎ E → T         E.val = T.val ⏎ T → num       T.val = num.val ⏎ *E* ⏎ E ⏎ T ⏎ 2 ⏎ + ⏎ T ⏎ 3 ⏎ = 2 ⏎ = 3 ⏎ = 2 ⏎ 5 ⏎ 6 ⏎ 23
   BYTE: "O E de baixo vale dois e o T vale três. Quanto vale a raiz? Toque."
   PERGUNTA (toque): certo = 5 ; errado = 6 | 23
   se errar: "Use a primeira linha: o val do E de baixo mais o val do T."
   se acertar: "Isso. Dois mais três dá cinco. O valor subiu dos filhos para o pai."

25. TELA: # A regra escrita ⏎ *E → E1 + T    E.val = E1.val + T.val* ⏎ *E → T         E.val = T.val* ⏎ *T → num       T.val = num.val* ⏎ E ⏎ E ⏎ T ⏎ 2 ⏎ + ⏎ T ⏎ 3 ⏎ = 2 ⏎ = 3 ⏎ = 2 ⏎ = 5 ⏎ S-atribuída ⏎ só L-atribuída ⏎ nenhuma
   BYTE: "Toda conta aqui usa só os filhos. Como a prova chama essa definição? Toque."
   PERGUNTA (toque): certo = S-atribuída ; errado = só L-atribuída | nenhuma
   se errar: "Só há atributo sintetizado. O S do nome vem dele."
   se acertar: "Isso, S-atribuída. E toda S-atribuída também é L-atribuída."

26. TELA: # A regra escrita ⏎ *D → T L       L.tipo = T.tipo* ⏎ T → int       T.tipo = int ⏎ L → id        addtipo(id, L.tipo) ⏎ D ⏎ T ⏎ int ⏎ *L* ⏎ a ⏎ int ⏎ int ⏎ S-atribuída ⏎ só L-atribuída ⏎ nenhuma
   BYTE: "Aqui o L herda o tipo do irmão T. Que nome a definição ganha? Toque."
   PERGUNTA (toque): certo = só L-atribuída ; errado = S-atribuída | nenhuma
   se errar: "Herdado do irmão da esquerda ainda deixa avaliar da esquerda para a direita."
   se acertar: "Isso. Tem herdado, então não é S. Vem da esquerda, então é L-atribuída."

27. TELA: # Diga por quê ⏎ int + float → float ⏎ O compilador ignora o tipo da soma. ⏎ O inteiro vira real antes da soma. ⏎ Real ganha de qualquer outro tipo.
   BYTE: "Inteiro mais real deu real, sem erro. Por quê?"
   PERGUNTA (toque): certo = O inteiro vira real antes da soma. ; errado = Real ganha de qualquer outro tipo. | O compilador ignora o tipo da soma.
   se errar: "O inteiro vira real sem estragar a conta?"
   se acertar: "O real guarda a ordem de grandeza do inteiro. O compilador converte, e a soma vira real."

28. TELA: # Erros comuns ⏎ Atributo sintetizado é o que o nó recebe do pai. ⏎ Escolha o veredito com o motivo certo. ⏎ Errado: sintetizado sobe dos filhos. ⏎ Errado: sintetizado vem dos irmãos. ⏎ Certo: o pai passa o tipo para os filhos.
   BYTE: "De onde vem o atributo sintetizado? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = Errado: sintetizado sobe dos filhos. ; errado = Errado: sintetizado vem dos irmãos. | Certo: o pai passa o tipo para os filhos.
   se errar: "Nesta aula, os tipos subiram ou desceram pela árvore?"
   se acertar: "Sintetizado sobe: o nó calcula a partir dos filhos. O que vem do pai ou dos irmãos é o atributo herdado."

29. TELA: # Erros comuns ⏎ Certo: Sintetizado: calculado a partir dos filhos. Herdado: vem do pai ou dos irmãos. ⏎ *Errado: int + float é erro de tipo.* ⏎ *Certo: Há coerção: o int vira float. Erro é misturar bool com número.*
   BYTE: "Outro erro: tratar toda mistura de tipos como erro. Inteiro com real tem coerção. Erro é quando nenhuma conversão faz sentido."

30. TELA: # Resumo ⏎ Atributo: informação num nó da árvore, calculada por regras semânticas. ⏎ O tipo é sintetizado: sobe das folhas para a raiz. ⏎ int com float sofre coerção para float. Sem conversão possível: erro semântico.
   BYTE: "Resumindo: a análise semântica anota a árvore com atributos. O tipo é um atributo sintetizado, que sobe dos filhos. No caminho, o compilador aplica coerção ou acusa erro."

#### PEGADINHA: "Sintetizado sobe, herdado desce" (5 cenas, ~34s)

1. TELA: # int a, b ⏎ D ⏎ T: int ⏎ L ⏎ a ⏎ b
   BYTE: "Declaração: int a, b. O tipo está num galho, e os nomes estão no outro."

2. TELA: # int a, b ⏎ D ⏎ T: int ⏎ L ⏎ a ⏎ b
   PROF: "Em a mais b, o tipo sobe dos filhos para o pai. Isso é sintetizado."

3. TELA: # int a, b ⏎ D ⏎ T: int ⏎ L ⏎ a ⏎ b
   BYTE: "Aqui é diferente. Para saber o tipo de a, o valor vem de qual nó? Toque."
   PERGUNTA (toque): certo = T: int ; errado = L | b
   se errar: "O int está no galho do lado. Ele precisa atravessar."
   se acertar: "O int passa pelo pai e desce até a. Isso é herdado."

4. TELA: # int a, b ⏎ D ⏎ *T: int* ⏎ *L* ⏎ *a* ⏎ *b*
   BYTE: "Herdado vem do pai ou de um irmão. Por isso desce e anda de lado."

5. TELA: !!HERDADO!!
   PROF: "S-atribuída usa só sintetizado. L-atribuída aceita herdado, da esquerda para a direita."

#### PRATICA: "Suba os tipos" (5 cenas, ~27s)

1. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Aqui a é inteiro e x é real. Vamos anotar a árvore."

2. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Comece pelas folhas. Depois, cada pai olha os filhos."

3. TELA: # a + x * 2 ⏎ a: int     x: float
   PROF: "Se um dos filhos é real, o resultado sobe como real."

4. TELA: + ⏎ *a* ⏎ * ⏎ x ⏎ 2
   BYTE: "Diga o tipo de cada nó aceso, de baixo para cima."
   BRINQUEDO: dica "Tipo errado. Olhe os filhos desse nó." / acerto "Árvore anotada. O tipo subiu até a raiz."

5. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Isso que você fez é atributo sintetizado, na ordem de baixo para cima."

#### PRATICA: "O tipo que desce" (9 cenas, ~74s)

1. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ id b ⏎ id a
   BYTE: "Na declaração int a, b, o tipo está escrito uma vez só, no começo. Mas o a e o b precisam dele."

2. TELA: # int a, b ⏎ D ⏎ *T* ⏎ L ⏎ *int* ⏎ L ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int
   BYTE: "O tipo nasce no nó T, pelo token int. Esse atributo é sintetizado: sobe do filho para T."

3. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ *id b* ⏎ *id a* ⏎ T.tipo = int ⏎ sobe ⏎ desce
   BYTE: "O a e o b estão embaixo de L, que é irmão de T. Para chegar neles, o tipo sobe ou desce pela árvore? Toque."
   PERGUNTA (toque): certo = desce ; errado = sobe
   se errar: "Os ids são folhas, lá embaixo. O tipo está em cima."
   se acertar: "Ele passa de T para o irmão L e desce. Atributo que vem do pai ou de um irmão é um atributo herdado."

4. TELA: # int a, b ⏎ D ⏎ T ⏎ *L* ⏎ int ⏎ *L* ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int
   BYTE: "A regra semântica diz: o herdado de L recebe o tipo de T. Em cada L de baixo, o herdado é copiado do L de cima."

5. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ *id b* ⏎ *id a* ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int ⏎ anota na tabela ⏎ soma os valores
   BYTE: "Quando o herdado chega numa folha id, o que o compilador faz com ele? Toque."
   PERGUNTA (toque): certo = anota na tabela ; errado = soma os valores
   se errar: "É uma declaração: o compilador precisa lembrar o tipo de cada nome."
   se acertar: "Acertou. Cada id entra na tabela de símbolos com o tipo que desceu."

6. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int ⏎ *a: int    b: int*
   BYTE: "Resultado: a e b entram na tabela como int, mesmo com o tipo escrito uma vez só."

7. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int ⏎ a: int    b: int
   PROF: "Definição que só tem atributo sintetizado é S-atribuída. O parser ascendente calcula tudo nas reduções."

8. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int ⏎ S-atribuída ⏎ L-atribuída
   BYTE: "Esta definição tem um herdado, que vem do irmão da esquerda. De que tipo ela é? Toque."
   PERGUNTA (toque): certo = L-atribuída ; errado = S-atribuída
   se errar: "Se tem atributo herdado, não pode ser a do S."
   se acertar: "L-atribuída: o herdado só depende do pai e dos irmãos à esquerda. Dá para calcular numa passada, da esquerda para a direita."

9. TELA: # int a, b ⏎ D ⏎ T ⏎ L ⏎ int ⏎ L ⏎ , ⏎ id b ⏎ id a ⏎ T.tipo = int ⏎ L.her = int ⏎ L.her = int
   BYTE: "Sintetizado sobe, herdado desce. Toda definição S-atribuída também é L-atribuída, mas não o contrário."

#### PRATICA: "Tipos nos comandos" (8 cenas, ~64s)

1. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool
   BYTE: "A expressão já sabe subir o tipo. Agora o comando confere se esse tipo serve para ele."

2. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ n = x * 2; ⏎ x = n + 1; ⏎ ok = n < x;
   PROF: "Na atribuição, o tipo da direita precisa caber na variável. Um int cabe num float, com coerção. Um float não cabe num int, porque a parte quebrada se perderia."

3. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ n = x * 2; ⏎ x = n + 1; ⏎ ok = n < x;
   BYTE: "Qual atribuição o verificador recusa? Toque."
   PERGUNTA (toque): certo = n = x * 2; ; errado = x = n + 1; | ok = n < x;
   se errar: "Procure um float indo para um int."
   se acertar: "O x vezes 2 sobe como float, e o n é int. Como em Java, é erro. Na segunda, o int vira float e passa."

4. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ if (n < 10) ... ⏎ while (ok) ... ⏎ if (n) ...
   BYTE: "No if e no while, a condição precisa ser bool. Na nossa linguagem é como em Java: número não serve de condição."

5. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ if (n < 10) ... ⏎ while (ok) ... ⏎ if (n) ...
   BYTE: "Qual condição o verificador recusa? Toque."
   PERGUNTA (toque): certo = if (n) ... ; errado = if (n < 10) ... | while (ok) ...
   se errar: "Qual delas sobe como int?"
   se acertar: "Sozinho, o n sobe como int. Em C passaria, mas aqui a condição precisa ser bool. Para testar o número, compare: n maior que zero."

6. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ float media(int a, int b) ⏎ return a + b; ⏎ media(x, 2); ⏎ media(1, 2);
   PROF: "Na chamada, cada argumento precisa caber no parâmetro da mesma posição. No return, a expressão precisa caber no tipo que a função promete."

7. TELA: # Tipos nos comandos ⏎ n: int    x: float    ok: bool ⏎ float media(int a, int b) ⏎ return a + b; ⏎ media(x, 2); ⏎ media(1, 2);
   BYTE: "Qual linha o verificador recusa? Toque."
   PERGUNTA (toque): certo = media(x, 2); ; errado = return a + b; | media(1, 2);
   se errar: "Confira cada argumento com o parâmetro do mesmo lugar."
   se acertar: "O primeiro parâmetro é int, e o x é float. Já o return de a mais b passa: o int vira float, como na atribuição."

8. TELA: # Tipos nos comandos ⏎ *n: int    x: float    ok: bool* ⏎ float media(int a, int b) ⏎ return a + b; ⏎ media(x, 2); ⏎ media(1, 2);
   BYTE: "Atribuição, condição, chamada e return. Em todos, o comando compara o tipo que subiu com o tipo que ele espera."

#### PRATICA: "O herdado na descida" (7 cenas, ~72s)

1. TELA: # O herdado na descida ⏎ void D() { ⏎   Tipo t = T(); ⏎   L(t); ⏎   casa(';'); ⏎ } ⏎ void L(Tipo h) { ⏎   addtipo(id(), h); ⏎   R(h); ⏎ } ⏎ void R(Tipo h) { ⏎   if (tok.tipo == ',') { ⏎     casa(','); addtipo(id(), h); ⏎     R(h); ⏎   } ⏎ }
   BYTE: "Na aula de atributos, o tipo descia pela árvore como atributo herdado. Na descida recursiva, cada não-terminal vira uma função. Veja como o herdado chega nela."

2. TELA: # O herdado na descida ⏎ void D() { ⏎ *  Tipo t = T();* ⏎   L(t); ⏎   casa(';'); ⏎ } ⏎ *void L(Tipo h) {* ⏎   addtipo(id(), h); ⏎   R(h); ⏎ } ⏎ void R(Tipo h) { ⏎   if (tok.tipo == ',') { ⏎     casa(','); addtipo(id(), h); ⏎     R(h); ⏎   } ⏎ }
   PROF: "O sintetizado volta no return: a função T devolve o tipo. O herdado vai como parâmetro: a função L recebe o h. O id casa o identificador e devolve o nome dele."

3. TELA: # O herdado na descida ⏎ void D() { ⏎   Tipo t = T(); ⏎   L(t); ⏎   casa(';'); ⏎ } ⏎ void L(Tipo h) { ⏎   addtipo(id(), h); ⏎   R(h); ⏎ } ⏎ void R(Tipo h) { ⏎   if (tok.tipo == ',') { ⏎     casa(','); addtipo(id(), h); ⏎     R(h); ⏎   } ⏎ }
   BYTE: "Na entrada float a, b, qual linha põe o b na tabela? Toque."
   PERGUNTA (toque): certo =     casa(','); addtipo(id(), h); ; errado =   addtipo(id(), h); |   L(t); |   R(h);
   se errar: "O a vem logo depois do tipo, e o b vem depois da vírgula. Quem casa a vírgula?"
   se acertar: "O L põe o a. O b vem depois da vírgula, e quem casa a vírgula é o R. O h chegou ao R como parâmetro, pela chamada que o L fez."

4. TELA: # O herdado na descida ⏎ T() → float ⏎ L(float) ⏎   addtipo(a, float) ⏎ R(float) ⏎   addtipo(b, float) ⏎ R(float)
   BYTE: "Este é o rastro das chamadas. O mesmo float passa de função em função, como parâmetro, até chegar a cada id."

5. TELA: # O herdado na descida ⏎ D → T L ; ⏎ L → id R ⏎ R → , id R  |  ε
   PROF: "Um esquema de tradução é a gramática com ações entre chaves dentro das regras. A ação roda quando o parser passa por aquele ponto da regra."

6. TELA: # O herdado na descida ⏎ D → T L ; ⏎ L → id R ⏎ R → , id R  |  ε ⏎ D → { L.h = T.tipo } T L ; ⏎ D → T L { L.h = T.tipo } ; ⏎ D → T { L.h = T.tipo } L ;
   BYTE: "A ação passa o tipo de T para o herdado de L. Em que ponto da regra do D ela pode ficar? Toque."
   PERGUNTA (toque): certo = D → T { L.h = T.tipo } L ; ; errado = D → { L.h = T.tipo } T L ; | D → T L { L.h = T.tipo } ;
   se errar: "A ação lê o tipo do T. E o L precisa do herdado pronto quando começa."
   se acertar: "Antes do T, o tipo ainda não existe. Depois do L, o a e o b já entraram na tabela sem tipo. Só entre o T e o L dá certo."

7. TELA: # O herdado na descida ⏎ void D() { ⏎ *  Tipo t = T();* ⏎ *  L(t);* ⏎   casa(';'); ⏎ } ⏎ *D → T { L.h = T.tipo } L ;*
   PROF: "Na função D, a ação do meio é a chamada do L com o t. Ela roda depois do T e antes do L. O herdado vira parâmetro, e o sintetizado vira retorno."

#### CLIPE 30s: "Atributo sintetizado e herdado" (2 cenas, ~8s)

1. TELA: # Sobe ⏎ pai ⏎ filho
   BYTE: "Sintetizado sobe dos filhos para o pai."

2. TELA: # Desce ⏎ pai ⏎ filho
   BYTE: "Herdado desce do pai ou vem do irmão."

#### CLIPE 30s: "Coerção de tipo" (2 cenas, ~10s)

1. TELA: # 2 + 1.5 ⏎ int ⏎ + ⏎ float
   BYTE: "Inteiro mais real: o compilador converte o inteiro sozinho."

2. TELA: # 2 + 1.5 ⏎ float ⏎ + ⏎ float
   BYTE: "Isso é coerção. O resultado é real."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Herdado — mentira: "Todo atributo sobe na árvore." / verdade: "Sintetizado sobe. Herdado desce ou vem do lado."
    · [V] S-atribuída só usa sintetizados. → Isso mesmo.
    · [V] O tipo de uma expressão costuma ser sintetizado. → Isso mesmo.
    · [F] L-atribuída não pode ter herdados. → Pode, vindos do pai ou da esquerda.
- Bug do Inteiro Esticado — mentira: "int + float dá erro." / verdade: "O compilador converte o int para float e segue."
    · [V] int + float resulta em float. → Isso mesmo.
    · [V] Coerção é uma conversão automática. → Isso mesmo.
    · [F] float cabe em int sem perder nada. → A parte depois da vírgula se perde.

#### FICHA DO ASSUNTO
Em uma frase: Uma SDD pendura atributos nos símbolos da gramática: sintetizados sobem dos filhos, herdados descem do pai/irmãos; a verificação de tipos é uma SDD que calcula tipos e insere coerções.
Gancho: S↑ H↓: Sintetizado Sobe, Herdado desce. S-atribuída = Só Sintetizado; L-atribuída = herda da Left.
Termos-chave: sintetizado, herdado, s-atribuída, l-atribuída, coerção, tipo
Erros comuns: Chamar de herdado um atributo calculado a partir dos filhos // Achar que L-atribuída permite depender de irmãos à direita // Esquecer que toda S-atribuída também é L-atribuída // Tratar int + float como erro em vez de coerção para float // Confundir erro de tipo (compilação) com divisão por zero (execução)
Flashcards:
    · P: Atributo sintetizado depende de... → R: Atributos dos filhos (e do próprio nó)
    · P: Atributo herdado depende de... → R: Atributos do pai e dos irmãos
    · P: O que é SDD S-atribuída? → R: Só tem atributos sintetizados
    · P: O que é SDD L-atribuída? → R: Herdados dependem só do pai e de irmãos à esquerda
    · P: Ordem de avaliação de uma S-atribuída? → R: Pós-ordem (de baixo para cima)
    · P: Tipo de int + float? → R: float, com inttofloat no int
    · P: Tipo de a < b? → R: bool
    · P: Em int a, b, c, o tipo chega às variáveis como... → R: Atributo herdado
    · P: $$ = $1 + $3 no Bison é que tipo de atributo? → R: Sintetizado

#### GLOSSÁRIO (balão ao tocar no termo)
- atributo: Uma informação pendurada num nó da árvore, como o tipo ou o valor.
