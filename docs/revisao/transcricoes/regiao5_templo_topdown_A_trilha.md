# Região 5: Templo Top-Down — Prever de cima para baixo: LL(1)
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO FIRST: Conjunto FIRST
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Com o que isso começa?» → Fazer com ajuda → Prática: você pilota «Símbolos correndo nos fios» → Pegadinha «FIRST de uma regra inteira» → Desafio
Jogos que treinam: Alquimia FIRST/FOLLOW (jogar ingredientes nos caldeirões), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug comeu o FIRST
Na tela: S -> a b | c
BYTE: "Antes da aula: o que pode abrir uma frase de S?"
Opções: ✔a e c | a e b | só a
Depois: "O a e o c. Cada opção abre com um deles."

#### AULA ANIMADA BÁSICA: "Com o que isso começa?" (21 cenas, ~167s) [1º exemplo começa na cena 4]

1. TELA: E -> T X ⏎ X -> + T X | ε ⏎ *T -> id | ( E )* ⏎ T -> id ⏎ T id ⏎ T -> ( E )
   BYTE: "O parser tem T no topo da pilha e lê id. T tem duas regras, e ele só pode olhar um token."

2. TELA: E -> T X ⏎ X -> + T X | ε ⏎ *T -> id | ( E )* ⏎ *T -> id* ⏎ T id ⏎ *T -> ( E )*
   BYTE: "Para escolher sem voltar atrás, ele precisa saber com que token cada regra pode começar."

3. TELA: !!FIRST!!
   BYTE: "FIRST de um símbolo é o conjunto dos terminais que podem aparecer no começo do que ele gera."

4. TELA: # FIRST ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ FIRST(T) FIRST(X) FIRST(E)
   BYTE: "Vamos calcular nesta gramática. Cada não-terminal ganha um saco, e o FIRST dele enche aos poucos."

5. TELA: # FIRST ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *T -> id | ( E )* ⏎ id ( ⏎ FIRST(T) FIRST(X) FIRST(E)
   BYTE: "Comece por T. As duas regras dele começam com terminal: id e abre parêntese. Os dois entram direto."

6. TELA: # FIRST ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ + T ε id ⏎ id ( ⏎ FIRST(T) FIRST(X) FIRST(E)
   BYTE: "Agora X. Uma regra começa com mais, a outra é só épsilon. Toque nos dois itens do FIRST de X."
   PERGUNTA (toque): certo = + | ε ; errado = T | id
   se errar: "O mais entra direto. E quando a regra é épsilon, o próprio épsilon entra."
   se acertar: "Isso. Mais e épsilon. Épsilon no FIRST quer dizer que X pode sumir. Um símbolo assim é anulável."

7. TELA: # FIRST ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ id ( + ε *id* *(* ⏎ FIRST(T) FIRST(X) FIRST(E)
   BYTE: "Agora E, que vai para T X. A regra começa com um não-terminal, então o FIRST de T entra no de E."

8. TELA: # FIRST ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ id ( + ε id ( ⏎ FIRST(T) FIRST(X) FIRST(E)
   PROF: "T não é anulável, então paramos nele. Se fosse, o FIRST de X entraria também."

9. TELA: # Receita do FIRST ⏎ 1. Começa com terminal: ele entra. ⏎ 2. Começa com não-terminal: FIRST dele, sem ε. ⏎ 3. Se ele é anulável, olhe o próximo símbolo. ⏎ 4. Todos anuláveis: ε entra. Repita até parar.
   BYTE: "A receita do FIRST tem quatro passos. Você aplica em cada regra da gramática."

10. TELA: # Receita do FIRST ⏎ *1. Começa com terminal: ele entra.* ⏎ 2. Começa com não-terminal: FIRST dele, sem ε. ⏎ 3. Se ele é anulável, olhe o próximo símbolo. ⏎ 4. Todos anuláveis: ε entra. Repita até parar.
   BYTE: "Passo um: a regra começa com terminal. Ele entra no FIRST, e essa regra está resolvida."

11. TELA: # Receita do FIRST ⏎ 1. Começa com terminal: ele entra. ⏎ 2. Começa com não-terminal: FIRST dele, sem ε. ⏎ 3. Se ele é anulável, olhe o próximo símbolo. ⏎ 4. Todos anuláveis: ε entra. Repita até parar. ⏎ E -> T X
   BYTE: "Em E vai para T X, qual passo da receita você usa? Toque nele."
   PERGUNTA (toque): certo = 2. Começa com não-terminal: FIRST dele, sem ε. ; errado = 1. Começa com terminal: ele entra. | 3. Se ele é anulável, olhe o próximo símbolo.
   se errar: "E começa com T, e T é um não-terminal com regras próprias."
   se acertar: "Isso. Passo dois: o FIRST de T entra no de E, menos o épsilon."

12. TELA: # Receita do FIRST ⏎ 1. Começa com terminal: ele entra. ⏎ 2. Começa com não-terminal: FIRST dele, sem ε. ⏎ *3. Se ele é anulável, olhe o próximo símbolo.* ⏎ 4. Todos anuláveis: ε entra. Repita até parar. ⏎ E -> T X
   BYTE: "Passo três: se esse não-terminal é anulável, ele pode sumir. Então o símbolo seguinte também pode começar a regra."

13. TELA: # Receita do FIRST ⏎ 1. Começa com terminal: ele entra. ⏎ 2. Começa com não-terminal: FIRST dele, sem ε. ⏎ 3. Se ele é anulável, olhe o próximo símbolo. ⏎ *4. Todos anuláveis: ε entra. Repita até parar.* ⏎ E -> T X
   BYTE: "Passo quatro: se todos os símbolos da regra são anuláveis, entra o épsilon. Depois repita tudo, até uma rodada não mudar nada."

14. TELA: # FIRST ⏎ S -> A b | c ⏎ A -> a A | ε ⏎ FIRST(A) FIRST(S)
   BYTE: "Outra gramática. Agora quem calcula é você. Comece por A, porque S depende dele."

15. TELA: # FIRST ⏎ S -> A b | c ⏎ *A -> a A | ε* ⏎ a b ε c ⏎ FIRST(A) FIRST(S)
   BYTE: "A tem duas regras: a seguido de A, e épsilon. Toque no que entra no FIRST de A."
   PERGUNTA (toque): certo = a | ε ; errado = b | c
   se errar: "Uma regra começa com o terminal a. A outra é épsilon."
   se acertar: "Isso. O a entra pelo passo um. O épsilon entra porque A é anulável."

16. TELA: # FIRST ⏎ *S -> A b | c* ⏎ A -> a A | ε ⏎ b ε A ⏎ a ε ⏎ FIRST(A) FIRST(S)
   BYTE: "S vai para A b. O a de A já entra no FIRST de S. Mas A é anulável. Que outro token pode começar S?"
   PERGUNTA (toque): certo = b ; errado = ε | A
   se errar: "Se A sumir, quem fica no começo é o b."
   se acertar: "Isso. Pelo passo três, o b entra. O épsilon de A fica de fora, porque o b nunca some."

17. TELA: # FIRST ⏎ *S -> A b | c* ⏎ A -> a A | ε ⏎ c ε ⏎ a ε a b ⏎ FIRST(A) FIRST(S)
   BYTE: "Falta a outra regra de S. Toque no que entra por ela."
   PERGUNTA (toque): certo = c ; errado = ε
   se errar: "A segunda regra de S começa com um terminal."
   se acertar: "Isso. A letra c entra pelo passo um."

18. TELA: # FIRST ⏎ S -> A b | c ⏎ A -> a A | ε ⏎ a ε *a* *b* *c* ⏎ FIRST(A) FIRST(S)
   BYTE: "FIRST de S: a, b e c. Mais uma rodada não muda nada, então o cálculo acabou."

19. TELA: # Erros comuns ⏎ FIRST(S) = { a, ε, c }, porque A começa com a ou some. ⏎ certo errado
   BYTE: "Um colega calculou assim o FIRST de S. Está certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "A é anulável, mas depois dele vem o b, que nunca some."
   se acertar: "Isso. O épsilon só entra se a regra inteira pode sumir. Aqui o b sempre fica, então quem entra é o b."

20. TELA: # Erros comuns ⏎ Certo: FIRST(S) = { a, b, c }. O ε de A não passa para S. ⏎ *Errado: Parar no primeiro não-terminal, mesmo quando ele é anulável.* ⏎ *Certo: Se ele é anulável, junte também o FIRST do símbolo seguinte.*
   BYTE: "Outro erro comum é parar cedo. Não-terminal anulável não fecha a conta: olhe o símbolo que vem depois dele."

21. TELA: # Resumo ⏎ FIRST(α): os terminais que podem começar o que α gera. ⏎ O ε só entra quando tudo em α é anulável. ⏎ Repita os passos até uma rodada não mudar nada.
   BYTE: "Resumindo: FIRST é o conjunto de começos possíveis. Épsilon só entra quando tudo pode sumir. E o cálculo repete até parar de mudar."

#### PRATICA: "Símbolos correndo nos fios" (6 cenas, ~33s)

1. TELA: F ⏎ T ⏎ E
   BYTE: "Três regras em cadeia. E começa com T, e T começa com F."

2. TELA: *F* *id   (* ⏎ T ⏎ E
   BYTE: "F abre com id, ou com abre parêntese. Eles entram primeiro."

3. TELA: F id   ( ⏎ *T* ⏎ E
   BYTE: "T começa com F. Então tudo que abre F corre pelo fio até T."

4. TELA: F id   ( ⏎ T id   ( ⏎ *E*
   BYTE: "Chegou. Agora os mesmos símbolos seguem de T para E."

5. TELA: F id   ( ⏎ T id   ( ⏎ id ( + ⏎ *E*
   BYTE: "Quem chega no FIRST de E? Toque nos dois."
   PERGUNTA (toque): certo = id | ( ; errado = +
   se errar: "Só correm pelo fio os símbolos que já estão em T."
   se acertar: "Isso. FIRST escorre de quem está no começo da regra."

6. TELA: F id   ( ⏎ T id   ( ⏎ E *id   (*
   PROF: "Quando nada mais corre, acabou. É assim que o algoritmo sabe parar."

#### PEGADINHA: "FIRST de uma regra inteira" (5 cenas, ~26s)

1. TELA: # FIRST de S ⏎ S -> A B c ⏎ A -> a | ε ⏎ B -> b | ε
   BYTE: "S começa com A. A pode sumir, e B também."

2. TELA: # FIRST de S ⏎ S -> A B c ⏎ *A -> a | ε* ⏎ B -> b | ε ⏎ a b c ε
   BYTE: "Se A não sumir, o a abre a frase. Primeiro candidato."

3. TELA: # FIRST de S ⏎ S -> A B c ⏎ A -> a | ε ⏎ B -> b | ε ⏎ a b c ε
   BYTE: "Toque em todos os símbolos que entram no FIRST de S."
   PERGUNTA (toque): certo = a | b | c ; errado = ε
   se errar: "A e B podem sumir, mas o c fica. Então a frase nunca é vazia."
   se acertar: "Isso: a, b e c. O épsilon fica de fora, porque o c nunca some."

4. TELA: # FIRST de S ⏎ S -> A B c ⏎ A -> a | ε ⏎ B -> b | ε ⏎ a b c
   PROF: "O erro comum é parar no primeiro símbolo. Quem pode sumir manda olhar o próximo."

5. TELA: # FIRST de S ⏎ S -> A B c ⏎ A -> a | ε ⏎ B -> b | ε ⏎ a b c
   BYTE: "Só entra épsilon se tudo à direita puder sumir."

#### CLIPE 30s: "O que é FIRST" (2 cenas, ~12s)

1. TELA: # FIRST ⏎ *id* T +
   BYTE: "FIRST é o que pode aparecer primeiro ao expandir um símbolo."

2. TELA: # FIRST ⏎ *id* T +
   BYTE: "Com ele, o parser decide qual regra seguir olhando um token."

#### CLIPE 30s: "Para que serve o épsilon" (2 cenas, ~10s)

1. TELA: # X -> + T X | ε ⏎ ε
   BYTE: "Épsilon é a cadeia vazia: uma regra que não escreve nada."

2. TELA: # X -> + T X | ε ⏎ + T + T + T  fim
   BYTE: "É assim que uma lista consegue terminar."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Primeiro Só — mentira: "FIRST é só o primeiro símbolo da regra." / verdade: "Se ele é não terminal, vale o FIRST dele. Se ele some, olha o próximo."
    · [V] Se B some em A -> B C, FIRST de C entra em FIRST de A. → Isso mesmo.
    · [V] ε entra em FIRST de A se A pode virar vazio. → Isso mesmo.
    · [F] FIRST de um terminal é vazio. → FIRST de terminal é ele mesmo.

#### FICHA DO ASSUNTO
Em uma frase: FIRST(α) é o conjunto de terminais que podem começar alguma cadeia derivada de α, mais ε, se α pode derivar a cadeia vazia.
Gancho: FIRST = Fila: quem Inicia. Terminal na frente entra e pronto; variável na frente empresta o FIRST dela; se ela pode faltar, passa a vez pro vizinho.
Termos-chave: terminal, começ, ε, anulável, não-terminal, ponto fixo
Erros comuns: Continuar para o próximo símbolo mesmo quando o atual NÃO é anulável. // Copiar o ε de FIRST(Y1) para FIRST(A) quando só Y1 (e não todo o lado direito) pode sumir. // Colocar $ dentro de um FIRST ($ só existe no FOLLOW). // Colocar um não-terminal dentro do conjunto (FIRST só tem terminais e talvez ε). // Parar depois de uma única passada, sem repetir até o ponto fixo.
Flashcards:
    · P: FIRST(a), se a é terminal? → R: {a}
    · P: Quando ε ∈ FIRST(A)? → R: Quando A ⇒* ε (A é anulável).
    · P: A → B c e B → b | ε. FIRST(A)? → R: {b, c}
    · P: A → B C, B → x | ε, C → y | ε. FIRST(A)? → R: {x, y, ε}
    · P: FIRST(E) na gramática E → T E' …? → R: {(, id}
    · P: $ pode estar em FIRST? → R: Nunca.
    · P: Por que o algoritmo repete as voltas? → R: Conjuntos dependem uns dos outros; repete até nada mudar (ponto fixo).
    · P: FIRST(A → A b | c)? → R: {c}: o cálculo não trava com recursão à esquerda.

#### GLOSSÁRIO (balão ao tocar no termo)
- épsilon: A palavra vazia. Uma regra com épsilon deixa o símbolo sumir.
- FIRST: Os tokens que podem começar um símbolo.


## ASSUNTO FOLLOW: Conjunto FOLLOW
Pré-requisitos no jogo: Conjunto FIRST
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O que vem depois?» → Fazer com ajuda → Pegadinha «O épsilon que muda o FOLLOW» → Desafio
Jogos que treinam: Alquimia FIRST/FOLLOW (jogar ingredientes nos caldeirões), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug comeu o FOLLOW
Na tela: E -> T + E | T
BYTE: "Antes da aula: o que pode vir logo depois de T?"
Opções: ✔+ | id | ε
Depois: "O mais. Olhe o que está à direita de T na regra."

#### AULA ANIMADA BÁSICA: "O que vem depois?" (22 cenas, ~182s) [1º exemplo começa na cena 4]

1. TELA: E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ X -> + T X ⏎ X ) ⏎ X -> ε
   BYTE: "O parser tem X no topo e lê fecha parêntese. X é anulável: pode sumir, ou pode virar mais T X."

2. TELA: E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ X -> + T X ⏎ X *)* ⏎ *X -> ε*
   BYTE: "Sumir só está certo se fecha parêntese puder aparecer logo depois de X. O FIRST não responde isso."

3. TELA: !!FOLLOW!!
   BYTE: "FOLLOW de um não-terminal é o conjunto dos terminais que podem aparecer logo depois dele em alguma derivação."

4. TELA: # FOLLOW ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "Mesma gramática, três sacos de novo. Agora cada saco guarda o que pode vir depois do não-terminal."

5. TELA: # FOLLOW ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ *$* ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "E é o símbolo inicial. Depois da frase inteira vem o fim da entrada, o cifrão. Ele entra no FOLLOW de E."

6. TELA: # FOLLOW ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *T -> id | ( E )* ⏎ $ *)* ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "Agora procuro E do lado direito das regras. Em abre parêntese, E, fecha parêntese, quem vem depois de E é o fecha parêntese."

7. TELA: # FOLLOW ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ + id ( ⏎ $ ) ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "Em E vai para T X, depois de T vem X. Com que token X pode começar? Toque nele."
   PERGUNTA (toque): certo = + ; errado = id | (
   se errar: "Olhe o FIRST de X, sem o épsilon."
   se acertar: "Isso. O FIRST de X, sem o épsilon, entra no FOLLOW de T. Épsilon nunca entra em FOLLOW."

8. TELA: # FOLLOW ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ $ ) *$* *)* + ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "X é o último símbolo da regra de E. Então o que vem depois de E também vem depois de X."

9. TELA: # FOLLOW ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ $ ) $ ) *+* *$* *)* ⏎ FOLLOW(E) FOLLOW(X) FOLLOW(T)
   BYTE: "E como X é anulável, T pode ficar no fim da regra. O FOLLOW de E entra no de T: mais, cifrão e fecha parêntese."

10. TELA: # Receita do FOLLOW ⏎ 1. O símbolo inicial ganha o $. ⏎ 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ⏎ 3. β anulável ou vazio: FOLLOW(A) entra também. ⏎ 4. Repita até uma rodada não mudar nada.
   BYTE: "A receita do FOLLOW tem quatro passos. Para cada não-terminal B, procure onde ele aparece do lado direito."

11. TELA: # Receita do FOLLOW ⏎ *1. O símbolo inicial ganha o $.* ⏎ 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ⏎ 3. β anulável ou vazio: FOLLOW(A) entra também. ⏎ 4. Repita até uma rodada não mudar nada.
   BYTE: "Passo um: o símbolo inicial ganha o cifrão, que marca o fim da entrada."

12. TELA: # Receita do FOLLOW ⏎ 1. O símbolo inicial ganha o $. ⏎ 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ⏎ 3. β anulável ou vazio: FOLLOW(A) entra também. ⏎ 4. Repita até uma rodada não mudar nada. ⏎ E -> T X
   BYTE: "O mais, que está no FIRST de X, entrou no FOLLOW de T. Qual passo fez isso? Toque."
   PERGUNTA (toque): certo = 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ; errado = 1. O símbolo inicial ganha o $. | 3. β anulável ou vazio: FOLLOW(A) entra também.
   se errar: "X vem logo depois de T. É o FIRST do que vem depois."
   se acertar: "Isso. Passo dois: o FIRST do que vem depois, sem o épsilon."

13. TELA: # Receita do FOLLOW ⏎ 1. O símbolo inicial ganha o $. ⏎ 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ⏎ *3. β anulável ou vazio: FOLLOW(A) entra também.* ⏎ 4. Repita até uma rodada não mudar nada. ⏎ E -> T X
   BYTE: "Passo três: se o resto da regra é anulável, ou não existe, B herda o FOLLOW do lado esquerdo da regra."

14. TELA: # Receita do FOLLOW ⏎ 1. O símbolo inicial ganha o $. ⏎ 2. A → α B β: FIRST(β) sem ε entra em FOLLOW(B). ⏎ 3. β anulável ou vazio: FOLLOW(A) entra também. ⏎ *4. Repita até uma rodada não mudar nada.* ⏎ E -> T X
   BYTE: "Passo quatro: um FOLLOW depende de outro. Repita os passos até uma rodada inteira não acrescentar nada."

15. TELA: # FOLLOW ⏎ E -> T R ⏎ R -> + T R | ε ⏎ T -> F Y ⏎ Y -> * F Y | ε ⏎ F -> ( E ) | id ⏎ FOLLOW(T) FOLLOW(F)
   BYTE: "Agora uma gramática maior, com soma e multiplicação. A meta é o FOLLOW de F."

16. TELA: # FOLLOW ⏎ E -> T R ⏎ R -> + T R | ε ⏎ *T -> F Y* ⏎ *Y -> * F Y | ε* ⏎ F -> ( E ) | id ⏎ * + id ⏎ FOLLOW(T) FOLLOW(F)
   BYTE: "Em T vai para F Y, depois de F vem Y. Com que token Y pode começar? Toque."
   PERGUNTA (toque): certo = * ; errado = + | id
   se errar: "Olhe as regras de Y. Uma delas começa com um terminal."
   se acertar: "Isso. O vezes entra pelo passo dois."

17. TELA: # FOLLOW ⏎ E -> T R ⏎ R -> + T R | ε ⏎ *T -> F Y* ⏎ Y -> * F Y | ε ⏎ F -> ( E ) | id ⏎ T E Y ⏎ FOLLOW(T) * FOLLOW(F)
   BYTE: "Y é anulável. Então F pode ficar no fim da regra de T. De quem F herda o FOLLOW? Toque."
   PERGUNTA (toque): certo = T ; errado = E | Y
   se errar: "F está no lado direito da regra de T."
   se acertar: "Isso. Pelo passo três, tudo que segue T também segue F."

18. TELA: # FOLLOW ⏎ E -> T R ⏎ R -> + T R | ε ⏎ T -> F Y ⏎ Y -> * F Y | ε ⏎ F -> ( E ) | id ⏎ *+* FOLLOW(T) *)* *$* * FOLLOW(F)
   BYTE: "O FOLLOW de T tem mais, fecha parêntese e cifrão. Os três entram no FOLLOW de F."

19. TELA: # FOLLOW ⏎ E -> T R ⏎ R -> + T R | ε ⏎ T -> F Y ⏎ Y -> * F Y | ε ⏎ F -> ( E ) | id ⏎ + FOLLOW(T) ) $ *** *+* FOLLOW(F) *)* *$*
   BYTE: "FOLLOW de F: vezes, mais, fecha parêntese e cifrão. Outra rodada não muda nada."

20. TELA: # Erros comuns ⏎ FOLLOW(F) = { *, ε }, porque depois de F vem Y. ⏎ certo errado
   BYTE: "Um colega copiou o FIRST de Y inteiro para o FOLLOW de F. Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "FOLLOW só tem terminais e o cifrão."
   se acertar: "Isso. Épsilon nunca entra em FOLLOW. Quando Y some, o que entra é o FOLLOW de T."

21. TELA: # Erros comuns ⏎ Certo: FOLLOW(F) = { *, +, ), $ }. Nunca há ε em FOLLOW. ⏎ *Errado: Esquecer o $ no FOLLOW do símbolo inicial.* ⏎ *Certo: O símbolo inicial sempre tem $ no FOLLOW.*
   BYTE: "Outro erro é esquecer o cifrão. Sem ele, a regra que some no fim da entrada fica sem coluna na tabela."

22. TELA: # Resumo ⏎ FOLLOW(B): os terminais que podem vir logo depois de B. ⏎ Vem do FIRST do que segue B. Se isso some, vem do FOLLOW do lado esquerdo. ⏎ Nunca tem ε. O símbolo inicial sempre tem $.
   BYTE: "Resumindo: FOLLOW é o que pode vir depois. Ele sai do FIRST do vizinho e do FOLLOW de quem está à esquerda da seta. Épsilon nunca entra."

#### PEGADINHA: "O épsilon que muda o FOLLOW" (5 cenas, ~25s)

1. TELA: # FOLLOW de B ⏎ S -> A b ⏎ A -> a B ⏎ B -> d | ε
   BYTE: "B pode sumir. Isso muda quem vem depois de A."

2. TELA: # FOLLOW de B ⏎ S -> A b ⏎ *A -> a B* ⏎ B -> d | ε ⏎ FOLLOW de A entra no FOLLOW de B
   BYTE: "B está no fim da regra de A. Depois de B vem o que vem depois de A."

3. TELA: # FOLLOW de B ⏎ *S -> A b* ⏎ A -> a B ⏎ B -> d | ε ⏎ FOLLOW de A entra no FOLLOW de B
   BYTE: "E depois de A vem b, olhando a primeira regra."

4. TELA: # FOLLOW de B ⏎ S -> A b ⏎ A -> a B ⏎ B -> d | ε ⏎ FOLLOW de A entra no FOLLOW de B ⏎ b d ε
   BYTE: "Então, o que entra no FOLLOW de B? Toque."
   PERGUNTA (toque): certo = b ; errado = d | ε
   se errar: "Não olhe para dentro de B. Olhe o que vem depois dele."
   se acertar: "Isso. O b. E nada de épsilon: FOLLOW nunca tem épsilon."

5. TELA: !!FOLLOW!!
   PROF: "FIRST olha para dentro. FOLLOW olha para fora."

#### CLIPE 30s: "O que é FOLLOW" (2 cenas, ~10s)

1. TELA: # FOLLOW ⏎ T *+* E
   BYTE: "FOLLOW é o que pode vir logo depois de um símbolo."

2. TELA: # FOLLOW ⏎ T *+* E
   BYTE: "Ele serve para decidir quando um símbolo pode sumir."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do FOLLOW Vazio — mentira: "ε pode entrar no FOLLOW." / verdade: "FOLLOW nunca tem ε. Ele guarda o que vem depois, e o fim é $."
    · [V] $ entra no FOLLOW do símbolo inicial. → Isso mesmo.
    · [V] FOLLOW de A recebe o FIRST do que vem depois de A, sem ε. → Isso mesmo.
    · [F] Se A fecha a regra de B, FOLLOW de A entra em FOLLOW de B. → É ao contrário: FOLLOW de B entra em FOLLOW de A.

#### FICHA DO ASSUNTO
Em uma frase: FOLLOW(A) é o conjunto de terminais que podem aparecer imediatamente à direita de A em alguma forma sentencial; $ entra se A pode terminar a frase.
Gancho: Dólar, Vizinho, Herança: $ no inicial; o vizinho da direita empresta o FIRST (sem ε); quem fica no fim herda o FOLLOW do pai.
Termos-chave: depois, $, first, herda, inicial, anulável
Erros comuns: Colocar ε em FOLLOW (FOLLOW nunca tem ε). // Esquecer o $ no FOLLOW do símbolo inicial. // Inverter a herança: é FOLLOW(A) ⊆ FOLLOW(B) para A → α B, não o contrário. // Esquecer a herança quando o resto β é anulável (não só quando B está no fim). // Calcular FOLLOW antes de ter todos os FIRST prontos.
Flashcards:
    · P: Regra 1 do FOLLOW? → R: $ ∈ FOLLOW(símbolo inicial).
    · P: Em A → α B β, o que entra em FOLLOW(B)? → R: FIRST(β) − {ε}
    · P: Quando FOLLOW(A) entra em FOLLOW(B)? → R: Se A → α B, ou A → α B β com β ⇒* ε.
    · P: FOLLOW pode conter ε? → R: Nunca.
    · P: S → a S b | ε. FOLLOW(S)? → R: {b, $}
    · P: S → ( S ) S | ε. FOLLOW(S)? → R: {), $}
    · P: Na gramática de expressões LL(1), FOLLOW(F)? → R: {*, +, ), $}
    · P: Para que serve o FOLLOW na tabela LL(1)? → R: Decidir onde colocar as produções que derivam ε.

#### GLOSSÁRIO (balão ao tocar no termo)
- FOLLOW: Os tokens que podem vir logo depois de um símbolo.
- cifrão: Marca o fim da entrada. O símbolo inicial sempre tem cifrão no FOLLOW.


## ASSUNTO LL1_TABLE: Tabela LL(1)
Pré-requisitos no jogo: Conjunto FIRST, Conjunto FOLLOW
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A tabela de decisões» → Fazer com ajuda → Programar o Byte → Pegadinha «Duas regras, uma casa só» → Desafio
Jogos que treinam: Sudoku LL(1) (encaixar produções na tabela), Torre da Pilha (operar a pilha do analisador preditivo), Programador Descendente (montar o código do parser)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: Byte com a pilha

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug riscou a tabela
Na tela: E -> id | ( E )
BYTE: "Antes da aula: com id na entrada, qual opção de E vale?"
Opções: ✔id | ( E ) | nenhuma
Depois: "A opção id. Cada token aponta para uma regra."

#### AULA ANIMADA BÁSICA: "A tabela de decisões" (19 cenas, ~156s)

1. TELA: E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ X -> + T X ⏎ X + ⏎ X -> ε
   BYTE: "O parser tem X no topo e lê mais. Qual regra de X ele usa? FIRST e FOLLOW já sabem a resposta."

2. TELA: E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ *X -> + T X* ⏎ X + ⏎ *X -> ε*
   BYTE: "Mas calcular os conjuntos a cada token seria lento. O compilador calcula uma vez e anota as respostas."

3. TELA: !!LL(1)!!
   BYTE: "Essa folha de respostas é a tabela LL(1): uma casa para cada par de não-terminal e próximo token."

4. TELA: # Tabela preditiva ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ id + ( ) $ ⏎ E ⏎ X ⏎ T
   BYTE: "Cada linha é um não-terminal. Cada coluna é um terminal ou o cifrão. A casa guarda a regra a usar."

5. TELA: # Tabela preditiva ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ id + ( ) $ ⏎ E *T X* *T X* ⏎ X ⏎ T
   BYTE: "E vai para T X. O FIRST de T X tem id e abre parêntese. A regra entra nessas duas colunas, na linha de E."

6. TELA: # Tabela preditiva ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ id + ( ) $ ⏎ E T X T X ⏎ X *+ T X* ⏎ T
   BYTE: "X vai para mais T X. O FIRST desse lado direito é só o mais. A regra entra na coluna do mais."

7. TELA: # Tabela preditiva ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ id + ( ) $ ⏎ E T X T X ⏎ X + T X ⏎ T
   BYTE: "X também vai para épsilon. Regra que some vai nas colunas do FOLLOW de X. Toque nas duas casas."
   PERGUNTA (toque): certo = ml2c4 | ml2c5 ; errado = ml2c1 | ml2c3
   se errar: "O FOLLOW de X é fecha parêntese e cifrão."
   se acertar: "Isso. Com fecha parêntese ou cifrão na entrada, o parser apaga X."

8. TELA: # Tabela preditiva ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ id + ( ) $ ⏎ E T X T X ⏎ X + T X ε ε ⏎ T *id* *( E )*
   BYTE: "Faltam as regras de T. A regra id vai na coluna do id. A dos parênteses vai na coluna do abre parêntese."

9. TELA: # Receita ⏎ 1. A → α vai nas colunas do FIRST(α). ⏎ 2. Se α é anulável, vai também nas do FOLLOW(A). ⏎ 3. Duas regras na casa: não é LL(1). Vazia: erro.
   BYTE: "A receita da tabela tem três passos. Você aplica os dois primeiros em cada regra da gramática."

10. TELA: # Receita ⏎ 1. A → α vai nas colunas do FIRST(α). ⏎ 2. Se α é anulável, vai também nas do FOLLOW(A). ⏎ 3. Duas regras na casa: não é LL(1). Vazia: erro. ⏎ X -> ε  vai em  M[X, )]
   BYTE: "X vai para épsilon entrou na coluna do parêntese que fecha. Toque na regra que fez isso."
   PERGUNTA (toque): certo = 2. Se α é anulável, vai também nas do FOLLOW(A). ; errado = 1. A → α vai nas colunas do FIRST(α). | 3. Duas regras na casa: não é LL(1). Vazia: erro.
   se errar: "Épsilon some. Aí quem manda é o que vem depois de X."
   se acertar: "Isso. Passo dois: regra anulável vai nas colunas do FOLLOW."

11. TELA: # Receita ⏎ 1. A → α vai nas colunas do FIRST(α). ⏎ 2. Se α é anulável, vai também nas do FOLLOW(A). ⏎ *3. Duas regras na casa: não é LL(1). Vazia: erro.* ⏎ X -> ε  vai em  M[X, )]
   PROF: "Passo três: se uma casa recebe duas regras, o parser não sabe escolher. Isso é um conflito, e a gramática não é LL(1)."

12. TELA: # LL(1) ⏎ L -> id R ⏎ R -> , id R | ε ⏎ id , $ ⏎ L ⏎ R
   BYTE: "Outra gramática: uma lista de ids separados por vírgula. Agora quem preenche a tabela é você."

13. TELA: # LL(1) ⏎ *L -> id R* ⏎ R -> , id R | ε ⏎ id , $ ⏎ L ⏎ R
   BYTE: "L vai para id R. Em que casa essa regra entra? Toque."
   PERGUNTA (toque): certo = ml1c1 ; errado = ml1c2 | ml1c3
   se errar: "O lado direito começa com id. Use o passo um."
   se acertar: "Isso. Linha L, coluna do id."

14. TELA: # LL(1) ⏎ L -> id R ⏎ *R -> , id R | ε* ⏎ id , $ ⏎ L id R ⏎ R
   BYTE: "R vai para vírgula, id, R. Toque na casa dela."
   PERGUNTA (toque): certo = ml2c2 ; errado = ml2c1 | ml2c3
   se errar: "Ela começa com a vírgula."
   se acertar: "Isso. O FIRST dela é a vírgula."

15. TELA: # LL(1) ⏎ L -> id R ⏎ *R -> , id R | ε* ⏎ id , $ ⏎ L id R ⏎ R , id R
   BYTE: "R também vai para épsilon. O FOLLOW de R é só o cifrão. Toque na casa."
   PERGUNTA (toque): certo = ml2c3 ; errado = ml2c1
   se errar: "Passo dois: regra anulável vai nas colunas do FOLLOW."
   se acertar: "Isso. R some quando a entrada acaba."

16. TELA: # LL(1) ⏎ L -> id R ⏎ R -> , id R | ε ⏎ id , $ ⏎ L *id R* ⏎ R *, id R* *ε*
   BYTE: "Nenhuma casa ficou com duas regras. Esta gramática é LL(1), e as casas vazias são erros de sintaxe."

17. TELA: # Erros comuns ⏎ A regra R → ε vai na coluna ε da tabela. ⏎ certo errado
   BYTE: "Um colega criou uma coluna só para o épsilon. Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "As colunas são os tokens que o parser pode ler. Ninguém lê épsilon."
   se acertar: "Isso. Épsilon não é token e não tem coluna. A regra que some vai nas colunas do FOLLOW."

18. TELA: # Erros comuns ⏎ Certo: R → ε vai nas colunas do FOLLOW(R). Não existe coluna ε. ⏎ *Errado: Olhar só o FIRST para dizer que a gramática é LL(1).* ⏎ *Certo: Regra anulável ocupa o FOLLOW e pode bater com o FIRST de outra.*
   BYTE: "Outro erro é conferir só o FIRST. Uma regra anulável ocupa as colunas do FOLLOW, e ali ela pode bater com outra regra."

19. TELA: # Resumo ⏎ Linha: não-terminal. Coluna: terminal ou $. ⏎ A → α entra no FIRST(α). Se α é anulável, também no FOLLOW(A). ⏎ Duas regras numa casa: conflito. Casa vazia: erro de sintaxe.
   BYTE: "Resumindo: a tabela guarda, para cada não-terminal e cada token, a regra a usar. O FIRST preenche quase tudo. O FOLLOW cuida das regras que somem."

#### PEGADINHA: "Duas regras, uma casa só" (6 cenas, ~35s)

1. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A
   BYTE: "Esta gramática parece inocente. Vamos montar a tabela."

2. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A *b*
   BYTE: "A pode virar b. Essa regra vai na coluna do b."

3. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A *b*
   BYTE: "A também pode sumir. Vai nas colunas do FOLLOW de A, e depois de A vem b."

4. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A b
   BYTE: "Toque na casa que recebeu duas regras."
   PERGUNTA (toque): certo = b ; errado = ml1c2 | ml2c1 | ml2c3
   se errar: "O FIRST da primeira opção e o FOLLOW de A têm o mesmo símbolo."
   se acertar: "Isso. Com b na entrada, o parser não sabe qual escolher."

5. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A *b / ε*
   PROF: "Duas regras na casa: a gramática não é LL(1). E ela nem tem recursão à esquerda."

6. TELA: # S -> A b   A -> b | ε ⏎ a b $ ⏎ S ⏎ A b / ε
   BYTE: "Na prova, confira sempre: FIRST da opção não pode bater com o FOLLOW de quem some."

#### CLIPE 30s: "O que quer dizer LL(1)" (2 cenas, ~10s)

1. TELA: # L  L  (1) ⏎ esquerda esquerda um token
   BYTE: "Lê da esquerda, deriva pela esquerda, e olha um token."

2. TELA: # L  L  (1) ⏎ duas regras
   BYTE: "Se uma casa da tabela tem duas regras, não é LL(1)."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Casa Dupla — mentira: "Duas regras na mesma casa é normal." / verdade: "Casa com duas regras é conflito. A gramática não é LL(1)."
    · [V] A regra entra na coluna de cada símbolo do FIRST do seu lado direito. → Isso mesmo.
    · [V] Se o lado direito pode sumir, a regra entra nas colunas do FOLLOW. → Isso mesmo.
    · [F] Toda gramática sem recursão à esquerda é LL(1). → S -> A b, A -> b | ε não tem recursão e não é LL(1).

#### FICHA DO ASSUNTO
Em uma frase: A tabela preditiva M[A, a] diz qual produção de A usar quando o próximo token é a; a gramática é LL(1) se nenhuma célula tem duas produções.
Gancho: Linha é Variável, Coluna é Token. Primeiro o FIRST; se o ε aparecer, chama o FOLLOW. Sudoku LL(1): uma célula, uma produção.
Termos-chave: first, follow, ε, célula, conflito, lookahead
Erros comuns: Pôr A → ε em colunas de FIRST em vez das colunas de FOLLOW(A). // Esquecer a coluna $. // Achar que produções ε impedem a gramática de ser LL(1). // Achar que eliminar recursão à esquerda e fatorar sempre garante LL(1). // Preencher célula por célula 'no olho' em vez de produção por produção.
Flashcards:
    · P: Regra 1 da tabela? → R: Para cada a ∈ FIRST(α), A → α em M[A, a].
    · P: Regra 2 da tabela? → R: Se ε ∈ FIRST(α), A → α em M[A, b] para cada b ∈ FOLLOW(A).
    · P: Célula vazia significa? → R: Erro de sintaxe.
    · P: Célula com duas produções significa? → R: Conflito: a gramática não é LL(1).
    · P: O que significa LL(1)? → R: Left-to-right, Leftmost derivation, 1 token de lookahead.
    · P: Gramática com recursão à esquerda pode ser LL(1)? → R: Não.
    · P: Gramática ambígua pode ser LL(1)? → R: Não.
    · P: M[E', )] na gramática de expressões LL(1)? → R: E' → ε

#### GLOSSÁRIO (balão ao tocar no termo)
- LL(1): Parser que lê da esquerda e decide olhando só um token à frente.
- lookahead: O próximo token da entrada, que o parser espia para decidir.


## ASSUNTO LL1_PARSING: Analisador preditivo (pilha)
Pré-requisitos no jogo: Tabela LL(1)
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A pilha decide» → Fazer com ajuda → Prática: você pilota «Opere a pilha» → Pegadinha «Terminal não vai para a tabela» → Desafio
Jogos que treinam: Derivação Turbo (escolher produções e crescer a árvore), Torre da Pilha (operar a pilha do analisador preditivo)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug derrubou a pilha
Na tela: topo: id   entrada: id
BYTE: "Antes da aula: topo e entrada iguais. O que fazer?"
Opções: ✔casar | expandir | erro
Depois: "Casar. Os dois saem, e a máquina anda."

#### AULA ANIMADA BÁSICA: "A pilha decide" (22 cenas, ~172s) [1º exemplo começa na cena 4]

1. TELA: # ( id ) ⏎ ( id [??]
   BYTE: "O parser leu abre parêntese e depois id. A frase ainda não acabou, e ele precisa lembrar o que falta."

2. TELA: # ( id ) ⏎ ( id [??] ⏎ ) ( id
   BYTE: "O que ele ainda precisa ler, cedo ou tarde? Toque."
   PERGUNTA (toque): certo = ) ; errado = ( | id
   se errar: "Todo parêntese aberto precisa fechar."
   se acertar: "Isso. Ele precisa guardar essa pendência até o parêntese chegar."

3. TELA: !!PREDITIVO!!
   BYTE: "Ele guarda numa pilha o que ainda espera ler. Esse é o analisador preditivo: a pilha e a tabela LL(1) decidem tudo."

4. TELA: # id + id ⏎ *id* + id $ ⏎ topo: E, token: id ⏎ E -> T X ⏎ *E* X -> + T X | ε ⏎ $ T -> id | ( E ) ⏎ pilha
   BYTE: "O analisador tem a pilha e a entrada. A pilha começa com cifrão e o símbolo inicial. Ele só olha o topo e o próximo token."

5. TELA: # id + id ⏎ id + id $ ⏎ E -> T X ⏎ T ⏎ *E -> T X* ⏎ X X -> + T X | ε ⏎ $ T -> id | ( E ) ⏎ pilha
   BYTE: "O topo é um não-terminal. Olho a casa da tabela: linha E, coluna id. Troco E pelo lado direito, empilhado de trás para a frente."

6. TELA: # id + id ⏎ *id* + id $ ⏎ T -> id ⏎ *id* ⏎ E -> T X ⏎ X X -> + T X | ε ⏎ $ *T -> id | ( E )* ⏎ pilha
   BYTE: "Assim T fica no topo, e X espera embaixo. Com id na entrada, a tabela manda trocar T por id."

7. TELA: # id + id ⏎ *id* + id $ ⏎ topo: id, token: id ⏎ *id* ⏎ E -> T X ⏎ X X -> + T X | ε ⏎ $ *T -> id | ( E )* ⏎ pilha ⏎ Casar Expandir
   BYTE: "Topo é id e o token é id. O que eu faço? Toque na ação."
   PERGUNTA (toque): certo = Casar ; errado = Expandir
   se errar: "Terminal no topo igual ao token da entrada: é par."
   se acertar: "Isso. Casou: o id sai da pilha e a entrada avança um token."

8. TELA: # id + id ⏎ + id $ ⏎ casou id ⏎ E -> T X ⏎ X X -> + T X | ε ⏎ $ T -> id | ( E ) ⏎ pilha
   BYTE: "O resto segue igual. Não-terminal no topo, troca pela tabela. Terminal no topo, casa com a entrada."

9. TELA: # id + id ⏎ *$* ⏎ aceita ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *$* T -> id | ( E ) ⏎ pilha
   BYTE: "No fim sobra cifrão na pilha e cifrão na entrada. A frase foi aceita."

10. TELA: # Receita ⏎ 1. Topo terminal igual ao token: casa e avança. ⏎ 2. Topo não-terminal: troca pela regra da tabela. ⏎ 3. $ com $: aceita. Casa vazia: erro.
   BYTE: "O analisador repete três casos até a entrada acabar. O topo da pilha diz qual caso vale."

11. TELA: # Receita ⏎ 1. Topo terminal igual ao token: casa e avança. ⏎ 2. Topo não-terminal: troca pela regra da tabela. ⏎ 3. $ com $: aceita. Casa vazia: erro. ⏎ topo: X, token: )
   BYTE: "No topo está X, e o token é fecha parêntese. Qual caso vale? Toque."
   PERGUNTA (toque): certo = 2. Topo não-terminal: troca pela regra da tabela. ; errado = 1. Topo terminal igual ao token: casa e avança. | 3. $ com $: aceita. Casa vazia: erro.
   se errar: "O X do topo não é token. Ele é um não-terminal."
   se acertar: "Isso. Caso dois. A tabela manda trocar X por épsilon, ou seja, só tirar X da pilha."

12. TELA: # Receita ⏎ 1. Topo terminal igual ao token: casa e avança. ⏎ 2. Topo não-terminal: troca pela regra da tabela. ⏎ *3. $ com $: aceita. Casa vazia: erro.*
   BYTE: "Se a casa da tabela está vazia, ou o terminal do topo é diferente do token, a frase tem erro de sintaxe."

13. TELA: # id , id ⏎ *id* , id $ ⏎ topo: L, token: id ⏎ L -> id R ⏎ *L* R -> , id R | ε ⏎ $ ⏎ pilha
   BYTE: "Agora uma lista: id, vírgula, id. A tabela é a da aula passada, e você decide cada passo."

14. TELA: # id , id ⏎ id , id $ ⏎ topo: L, token: id ⏎ *L -> id R* ⏎ L R -> , id R | ε ⏎ $ ⏎ pilha ⏎ id R
   BYTE: "A tabela troca L por id R. Qual dos dois fica no topo? Toque."
   PERGUNTA (toque): certo = id ; errado = R
   se errar: "O lado direito entra na pilha de trás para a frente."
   se acertar: "Isso. O id fica em cima, pronto para casar."

15. TELA: # id , id ⏎ , id $ ⏎ casou id ⏎ L -> id R ⏎ *R* R -> , id R | ε ⏎ $ ⏎ pilha
   BYTE: "O id do topo casa com o id da entrada. Os dois saem, e sobra R no topo."

16. TELA: # id , id ⏎ *,* id $ ⏎ topo: R, token: , ⏎ L -> id R ⏎ *R* R -> , id R | ε ⏎ $ ⏎ pilha R -> , id R ⏎ R -> ε
   BYTE: "Topo R, token vírgula. Qual regra a tabela manda usar? Toque."
   PERGUNTA (toque): certo = R -> , id R ; errado = R -> ε
   se errar: "Qual das duas regras começa com a vírgula?"
   se acertar: "Isso. É a regra que começa com vírgula."

17. TELA: # id , id ⏎ *$* ⏎ topo: R, token: $ ⏎ L -> id R ⏎ *R* R -> , id R | ε ⏎ $ ⏎ pilha
   BYTE: "Casa a vírgula, casa o id. De novo, R fica sozinho no topo."

18. TELA: # id , id ⏎ *$* ⏎ topo: R, token: $ ⏎ L -> id R ⏎ *R* R -> , id R | ε ⏎ $ ⏎ pilha R -> , id R ⏎ R -> ε
   BYTE: "Agora a entrada acabou. Qual regra de R vale? Toque."
   PERGUNTA (toque): certo = R -> ε ; errado = R -> , id R
   se errar: "O cifrão está no FOLLOW de R."
   se acertar: "Isso. R vai para épsilon: R sai da pilha e nada entra."

19. TELA: # id , id ⏎ *$* ⏎ aceita ⏎ L -> id R ⏎ R -> , id R | ε ⏎ *$* ⏎ pilha
   BYTE: "Cifrão com cifrão: aceita. As regras usadas, na ordem, formam a derivação mais à esquerda da lista."

20. TELA: # Erros comuns ⏎ Para trocar E por T X, empilho T e depois X. ⏎ certo errado
   BYTE: "Em que ordem o lado direito entra na pilha? Um colega fez assim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Quem precisa ser lido primeiro tem que ficar no topo."
   se acertar: "Isso. Empilha de trás para a frente: primeiro X, depois T. Assim T fica no topo e é tratado antes."

21. TELA: # Erros comuns ⏎ Certo: Empilho X e depois T. O primeiro símbolo da regra fica no topo. ⏎ *Errado: Ao usar X → ε, avançar a entrada.* ⏎ *Certo: Só X sai da pilha. O token continua na entrada.*
   BYTE: "Outro erro: avançar a entrada ao aplicar uma regra. Só o casamento de terminal consome token. Trocar por épsilon só tira o topo."

22. TELA: # Resumo ⏎ A pilha começa com $ e o símbolo inicial. ⏎ Não-terminal no topo: troca pela tabela. Terminal no topo: casa com o token. ⏎ $ com $ aceita. Casa vazia ou terminal diferente é erro.
   BYTE: "Resumindo: o preditivo olha o topo e um token. A tabela escolhe a regra, o casamento consome a entrada, e cifrão com cifrão aceita."

#### PRATICA: "Opere a pilha" (5 cenas, ~27s)

1. TELA: # id + id ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id
   BYTE: "Pilha, entrada e duas ações. Parece pouco, e é tudo que o parser faz."

2. TELA: # id + id ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id
   BYTE: "Topo não terminal: consulta a tabela e troca pela regra."

3. TELA: # id + id ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *T -> id*
   BYTE: "Topo terminal: precisa bater com a entrada, e os dois saem."

4. TELA: *id* + id $ ⏎ E ⏎ $ ⏎ pilha
   BYTE: "Opere até a pilha esvaziar."
   BRINQUEDO interativo: meta = "Leve a pilha até o fim" ; teclas = [Trocar pela regra, Casar]
   se errar: "Olhe o topo. Ele é terminal ou não terminal?"
   ao conseguir: "Pilha vazia e entrada no fim. A frase vale."

5. TELA: # id + id ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *T -> id*
   PROF: "Sem recursão, sem volta atrás. Por isso o LL(1) é tão rápido."

#### PEGADINHA: "Terminal não vai para a tabela" (6 cenas, ~36s)

1. TELA: # Terminal não vai para a tabela ⏎ +  id  $ ⏎ + ⏎ X ⏎ $
   BYTE: "O topo da pilha pode ter dois tipos de símbolo."

2. TELA: # Terminal não vai para a tabela ⏎ +  id  $ ⏎ + ⏎ *X* ⏎ $
   BYTE: "Com não-terminal no topo, o parser consulta a tabela e troca pela regra."

3. TELA: # Terminal não vai para a tabela ⏎ *+  id  $* ⏎ *+* ⏎ X ⏎ $
   BYTE: "Com terminal no topo, a tabela não entra. Ele só confere com a entrada."

4. TELA: # Terminal não vai para a tabela ⏎ casar e avançar ⏎ consultar a tabela ⏎ erro ⏎ +  id  $ ⏎ + ⏎ X ⏎ $
   BYTE: "O topo é mais e a entrada é mais. O que o parser faz? Toque."
   PERGUNTA (toque): certo = casar e avançar ; errado = consultar a tabela | erro
   se errar: "Mais é terminal. Terminal no topo nunca vai para a tabela."
   se acertar: "Isso. Casa, tira do topo e avança na entrada."

5. TELA: # Terminal não vai para a tabela ⏎ *id  $* ⏎ *)* ⏎ X ⏎ $
   BYTE: "Se o terminal do topo for diferente da entrada, é erro na hora."

6. TELA: # Terminal não vai para a tabela ⏎ +  id  $ ⏎ + ⏎ X ⏎ $
   BYTE: "Tabela só para não-terminal. Terminal só confere."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Pilha Invertida — mentira: "O lado direito entra na pilha na ordem escrita." / verdade: "Entra de trás para frente, para o primeiro símbolo ficar no topo."
    · [V] Terminal no topo precisa casar com a entrada. → Isso mesmo.
    · [V] Casa vazia na tabela significa erro. → Isso mesmo.
    · [F] Entrada acabou com a pilha cheia: frase aceita. → Só aceita com pilha e entrada no cifrão.

#### FICHA DO ASSUNTO
Em uma frase: O analisador preditivo não recursivo usa uma pilha (começando com $ S) e a tabela M: terminal no topo casa com a entrada; não-terminal no topo é trocado pelo lado direito, invertido.
Gancho: Terminal no topo? Casa e anda. Variável no topo? Consulta e troca. $ com $? Festa! (E arrume a mochila: o que usa primeiro vai por último.)
Termos-chave: pilha, topo, tabela, casa, invertid, $
Erros comuns: Empilhar o lado direito na ordem normal (o primeiro símbolo tem que ficar no topo). // Avançar a entrada numa expansão (só casar consome token). // Empilhar ε (produção vazia só desempilha). // Esquecer de começar a pilha com $ S. // Misturar o lado do topo da pilha no meio da simulação.
Flashcards:
    · P: Configuração inicial? → R: Pilha $ S; entrada w$.
    · P: Topo terminal igual ao token? → R: Desempilha e avança (casar).
    · P: Topo não-terminal A, token a? → R: Troca A pelo lado direito de M[A, a], invertido.
    · P: Quando aceita? → R: Topo $ e entrada $.
    · P: M[A, a] vazia? → R: Erro de sintaxe.
    · P: Que derivação a saída forma? → R: A derivação mais à esquerda.
    · P: Expandir A → ε faz o quê na pilha? → R: Só desempilha A.
    · P: O analisador preditivo faz backtracking? → R: Nunca.


## ASSUNTO RECURSIVE_DESCENT: Descida recursiva
Pré-requisitos no jogo: Conjunto FIRST
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Um método por regra» → Fazer com ajuda → Pegadinha «Quem avança o token» → Desafio
Jogos que treinam: Derivação Turbo (escolher produções e crescer a árvore), Torre da Pilha (operar a pilha do analisador preditivo), Programador Descendente (montar o código do parser)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug apagou uma função
Na tela: E -> T X
BYTE: "Antes da aula: quantas funções a função E precisa chamar?"
Opções: 1 | ✔2 | 3
Depois: "Duas: T e X."

#### AULA ANIMADA BÁSICA: "Um método por regra" (21 cenas, ~168s) [1º exemplo começa na cena 4]

1. TELA: # Tabela preditiva ⏎ id + ( ) $ ⏎ E T X T X ⏎ X + T X ε ε ⏎ T id ( E )
   BYTE: "A tabela LL(1) funciona, mas é uma grade de casas. Ninguém gosta de preencher nem de depurar isso à mão."

2. TELA: # Tabela preditiva ⏎ id + ( ) $ ⏎ E T X T X ⏎ X + T X ε ε ⏎ T id ( E ) ⏎ M[X, id]
   BYTE: "E quando dá erro, você quer saber em que regra o parser estava, para dar uma mensagem boa."

3. TELA: !!DESCIDA RECURSIVA!!
   BYTE: "Na descida recursiva, cada não-terminal vira uma função. A pilha do analisador vira a pilha de chamadas do programa."

4. TELA: # Descida recursiva ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ void E() { T(); X(); } ⏎ void X() { ⏎   if (tok == '+') { ⏎     casa('+'); T(); X(); ⏎   } ⏎ } ⏎ void T() { ⏎   if (tok == ID) casa(ID); ⏎   else { casa('('); E(); casa(')'); } ⏎ }
   BYTE: "É o mesmo analisador preditivo, escrito como código. Para cada não-terminal, uma função com o nome dele."

5. TELA: # Descida recursiva ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ *void E() { T(); X(); }* ⏎ void X() { ⏎   if (tok == '+') { ⏎     casa('+'); T(); X(); ⏎   } ⏎ } ⏎ void T() { ⏎   if (tok == ID) casa(ID); ⏎   else { casa('('); E(); casa(')'); } ⏎ }
   BYTE: "A regra de E pede T e depois X. A função E chama a função T e depois a função X, nessa ordem."

6. TELA: # Descida recursiva ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ void E() { T(); X(); } ⏎ void X() { ⏎   if (tok == '+') { ⏎ *    casa('+'); T(); X();* ⏎   } ⏎ } ⏎ void T() { ⏎   if (tok == ID) casa(ID); ⏎   else { casa('('); E(); casa(')'); } ⏎ }
   BYTE: "Terminal vira uma chamada de casa. Ela confere se o token é o esperado e avança. Se não for, é erro de sintaxe."

7. TELA: # Descida recursiva ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id | ( E ) ⏎ void E() { T(); X(); } ⏎ void X() { ⏎ *  if (tok == '+') {* ⏎     casa('+'); T(); X(); ⏎   } ⏎ } ⏎ void T() { ⏎   if (tok == ID) casa(ID); ⏎   else { casa('('); E(); casa(')'); } ⏎ } ⏎ X -> + T X X -> ε
   BYTE: "Na função X, se o token não é mais, ela não faz nada e volta. Que regra é essa? Toque nela."
   PERGUNTA (toque): certo = X -> ε ; errado = X -> + T X
   se errar: "Não fazer nada é o mesmo que sumir da frase."
   se acertar: "Isso. Não fazer nada é a regra épsilon: X sumiu sem consumir token."

8. TELA: # Descida recursiva ⏎ E -> T X ⏎ X -> + T X | ε ⏎ *T -> id | ( E )* ⏎ void E() { T(); X(); } ⏎ void X() { ⏎   if (tok == '+') { ⏎     casa('+'); T(); X(); ⏎   } ⏎ } ⏎ void T() { ⏎ *  if (tok == ID) casa(ID);* ⏎ *  else { casa('('); E(); casa(')'); }* ⏎ }
   BYTE: "Cada if testa o próximo token contra o FIRST de uma regra. É a mesma decisão da tabela LL(1), agora dentro do código."

9. TELA: # Descida recursiva ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id | ( E ) ⏎ void E() { T(); X(); } ⏎ void X() { ⏎   if (tok == '+') { ⏎     casa('+'); T(); X(); ⏎   } ⏎ } ⏎ void T() { ⏎   if (tok == ID) casa(ID); ⏎   else { casa('('); E(); casa(')'); } ⏎ }
   PROF: "É esse o parser que o trabalho prático pede. Ele só funciona se a gramática for LL(1): sem recursão à esquerda e fatorada."

10. TELA: # Receita ⏎ 1. Cada não-terminal vira uma função. ⏎ 2. Não-terminal na regra: chama a função dele. ⏎ 3. Terminal na regra: casa confere e avança. ⏎ 4. Várias regras: if no FIRST, épsilon no else.
   BYTE: "A receita traduz cada regra para código, símbolo por símbolo."

11. TELA: # Receita ⏎ 1. Cada não-terminal vira uma função. ⏎ 2. Não-terminal na regra: chama a função dele. ⏎ 3. Terminal na regra: casa confere e avança. ⏎ 4. Várias regras: if no FIRST, épsilon no else. ⏎ T -> ( E )
   BYTE: "Na regra dos parênteses, o símbolo do meio vira o quê? Toque na regra."
   PERGUNTA (toque): certo = 2. Não-terminal na regra: chama a função dele. ; errado = 1. Cada não-terminal vira uma função. | 3. Terminal na regra: casa confere e avança. | 4. Várias regras: if no FIRST, épsilon no else.
   se errar: "O símbolo do meio é um não-terminal, não um token."
   se acertar: "Isso. O E vira a chamada da função dele."

12. TELA: # Receita ⏎ 1. Cada não-terminal vira uma função. ⏎ 2. Não-terminal na regra: chama a função dele. ⏎ 3. Terminal na regra: casa confere e avança. ⏎ *4. Várias regras: if no FIRST, épsilon no else.*
   BYTE: "Se o não-terminal não é anulável, o último else não pode ficar vazio. Ele avisa o erro de sintaxe."

13. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ void L() { casa(ID); ___ } ⏎ void R() { ⏎   if (tok == ___) { ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ }
   BYTE: "A lista de ids de novo. Agora quem escreve o código é você."

14. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ *void L() { casa(ID); ___ }* ⏎ void R() { ⏎   if (tok == ___) { ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ } ⏎ R(); casa(R);
   BYTE: "L pede id e depois R. O que completa a função L? Toque."
   PERGUNTA (toque): certo = R(); ; errado = casa(R);
   se errar: "Não-terminal na regra vira chamada de função."
   se acertar: "Isso. Depois de casar o id, L chama R."

15. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ void L() { casa(ID); R(); } ⏎ void R() { ⏎ *  if (tok == ___) {* ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ } ⏎ ',' ID
   BYTE: "Que token faz R escolher a regra da vírgula? Toque."
   PERGUNTA (toque): certo = ',' ; errado = ID
   se errar: "Olhe com que token essa regra começa."
   se acertar: "Isso. A vírgula é o FIRST dessa regra."

16. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ *void L() { casa(ID); R(); }* ⏎ void R() { ⏎ *  if (tok == ',') {* ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ } ⏎ id , id $
   BYTE: "Agora rode o código na entrada id, vírgula, id."

17. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ void L() { casa(ID); R(); } ⏎ void R() { ⏎   if (tok == ',') { ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ } ⏎ id , id $ ⏎ 1 2 3
   BYTE: "Nessa entrada, quantas vezes o parser chama a função R? Toque."
   PERGUNTA (toque): certo = 2 ; errado = 1 | 3
   se errar: "Uma chamada vem de L. Outra vem de dentro do if."
   se acertar: "Isso. Duas vezes. Na segunda, a entrada já acabou."

18. TELA: # Descida recursiva ⏎ L -> id R ⏎ R -> , id R | ε ⏎ void L() { casa(ID); R(); } ⏎ void R() { ⏎ *  if (tok == ',') {* ⏎     casa(','); casa(ID); R(); ⏎   } ⏎ } ⏎ id , id *$*
   BYTE: "Na segunda chamada o token é cifrão, e o if falha. R usa a regra épsilon: não casa nada e volta."

19. TELA: # Erros comuns ⏎ E → E + T vira: void E() { E(); casa('+'); T(); } ⏎ certo errado
   BYTE: "Um colega traduziu uma regra com recursão à esquerda direto para código. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Veja a primeira coisa que a função E faz, antes de ler qualquer token."
   se acertar: "Isso. E chama E sem consumir nada, e a recursão nunca termina. Antes de descer, elimine a recursão à esquerda."

20. TELA: # Erros comuns ⏎ Certo: Recursão à esquerda trava a descida. Elimine antes: E → T X. ⏎ *Errado: Tratar a regra ε com uma chamada casa(ε).* ⏎ *Certo: Regra ε é o else vazio: a função volta sem consumir token.*
   BYTE: "Outro erro é tentar casar o épsilon. Épsilon não é token. A regra épsilon é só voltar sem fazer nada."

21. TELA: # Resumo ⏎ Cada não-terminal é uma função. Não-terminal na regra é chamada; terminal é casa. ⏎ O if olha o próximo token e escolhe a regra pelo FIRST. ⏎ Só vale para gramática LL(1): sem recursão à esquerda e fatorada.
   BYTE: "Resumindo: a descida recursiva é o preditivo em forma de código. Uma função por não-terminal, um if por regra, e a pilha de chamadas faz o papel da pilha."

#### PEGADINHA: "Quem avança o token" (5 cenas, ~32s)

1. TELA: # void F() ⏎ if (tk == ID) casa(ID); ⏎ else if (tk == ABRE) { ⏎   casa(ABRE); ⏎   E(); ⏎   casa(FECHA); ⏎ } else erro();
   BYTE: "Na descida recursiva, cada regra vira uma função. Olhe a do F."

2. TELA: # void F() ⏎ *if (tk == ID) casa(ID);* ⏎ *else if (tk == ABRE) {* ⏎   casa(ABRE); ⏎   E(); ⏎   casa(FECHA); ⏎ } else erro();
   BYTE: "A função só olha o token atual para decidir. Olhar não gasta nada."

3. TELA: # void F() ⏎ if (tk == ID) casa(ID); ⏎ else if (tk == ABRE) { ⏎   casa(ABRE); ⏎   E(); ⏎   casa(FECHA); ⏎ } else erro();
   BYTE: "Em qual linha a entrada anda de verdade? Toque na primeira."
   PERGUNTA (toque): certo = if (tk == ID) casa(ID); ; errado = else if (tk == ABRE) { | } else erro();
   se errar: "Procure a função que confere o token e passa para o próximo."
   se acertar: "Isso. Quem avança é o casa."

4. TELA: # void F() ⏎ if (tk == ID) casa(ID); ⏎ else if (tk == ABRE) { ⏎   casa(ABRE); ⏎ *  E();* ⏎   casa(FECHA); ⏎ } else erro();
   BYTE: "Chamar E não avança nada por conta própria. Quem avança lá dentro é outro casa."

5. TELA: # void F() ⏎ if (tk == ID) casa(ID); ⏎ else if (tk == ABRE) { ⏎   casa(ABRE); ⏎   E(); ⏎   casa(FECHA); ⏎ } else erro();
   PROF: "Se ninguém chamar casa, o programa fica parado no mesmo token, para sempre."

#### CLIPE 30s: "Descida recursiva" (2 cenas, ~10s)

1. TELA: # E -> T X ⏎ void E() { ⏎   T(); ⏎   X(); ⏎ }
   BYTE: "Cada não terminal vira uma função."

2. TELA: # E -> T X ⏎ void E() { ⏎ *  T();* ⏎ *  X();* ⏎ }
   BYTE: "A função olha o token, escolhe a regra e chama as outras."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Token Avançado — mentira: "Toda função lê um token antes de decidir." / verdade: "A função olha o token atual e só avança quando casa."
    · [V] Cada não terminal vira uma função. → Isso mesmo.
    · [V] casaToken confere e avança a entrada. → Isso mesmo.
    · [F] Descida recursiva roda E -> E + T como está. → Ela entraria em recursão infinita.

#### FICHA DO ASSUNTO
Em uma frase: Descida recursiva: uma função por não-terminal; o lookahead escolhe a produção, terminais viram match e não-terminais viram chamadas.
Gancho: Uma variável, uma função; um terminal, um match. Recursão à esquerda = cachorro correndo atrás do rabo.
Termos-chave: função, não-terminal, lookahead, match, recursão à esquerda, first
Erros comuns: Usar gramática com recursão à esquerda (laço infinito). // Não fatorar à esquerda (um token não decide). // Esquecer de verificar $ no fim: S() pode retornar com tokens sobrando. // Avançar o token dentro de uma chamada de não-terminal sem dar match. // Achar que descida recursiva é bottom-up.
Flashcards:
    · P: Quantas funções? → R: Uma por não-terminal.
    · P: Terminal no lado direito vira? → R: match(terminal).
    · P: Não-terminal no lado direito vira? → R: Chamada da função dele.
    · P: Como escolher a produção? → R: Pelo lookahead: FIRST, ou FOLLOW para a alternativa ε.
    · P: Onde fica a pilha? → R: Na pilha de chamadas do programa.
    · P: Por que recursão à esquerda quebra? → R: A() chama A() sem consumir token: laço infinito.
    · P: Recursão à direita é problema? → R: Não: cada chamada consome token antes.
    · P: Que classe de gramáticas a forma preditiva aceita? → R: LL(1).


## ASSUNTO ERROR_RECOVERY: Erros sintáticos e recuperação
Pré-requisitos no jogo: Analisador preditivo (pilha)
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Cair e levantar» → Pegadinha «Parar ou seguir» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Sala do Pânico (achar o erro e sincronizar)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug plantou um erro
Na tela: x = = 2;  y = 3;
BYTE: "Antes da aula: onde o parser recomeça?"
Opções: no segundo = | ✔depois do ; | no fim
Depois: "Depois do ponto e vírgula. Ali começa outro comando."

#### AULA ANIMADA BÁSICA: "Cair e levantar" (21 cenas, ~173s) [1º exemplo começa na cena 4]

1. TELA: # Dois erros ⏎ *x = * 3;* ⏎ y = 2; ⏎ *z = ;*
   BYTE: "Este código tem dois erros de sintaxe. Um compilador simples para no primeiro e não diz mais nada."

2. TELA: # Dois erros ⏎ x = * 3; ⏎ y = 2; ⏎ *z = ;*
   BYTE: "Você corrige, compila de novo, e só então descobre o segundo. Com dez erros, são dez compilações."

3. TELA: !!RECUPERAÇÃO!!
   BYTE: "Avisar o erro, se ajeitar e continuar a análise se chama recuperação de erros."

4. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ x = * 3 ; y = 2 ;
   BYTE: "A ideia: o parser avisa onde travou, joga fora um pedaço da entrada e volta a analisar num ponto seguro."

5. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ *Val -> id | num* ⏎ x = *** 3 ; y = 2 ; ⏎ erro aqui
   BYTE: "Aqui o parser trava: depois do igual a gramática pede um valor, e chegou um vezes."

6. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ x = * 3 ; y = 2 ; ⏎ erro aqui
   BYTE: "No modo pânico, ele descarta tokens até achar um token de sincronização. Toque onde ele se segura."
   PERGUNTA (toque): certo = ; ; errado = 3 | y | 2
   se errar: "Procure o token que encerra o comando."
   se acertar: "Isso. O ponto e vírgula fecha o comando. Ele é o token de sincronização."

7. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ *Cmd -> id = Val ;* ⏎ Val -> id | num ⏎ x = ; *y* = 2 ;
   BYTE: "Tudo até ali vai fora, sem analisar. A análise recomeça no comando seguinte, e o resto do arquivo é conferido."

8. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ x = ; y = 2 ;
   PROF: "Bons tokens de sincronização saem do FOLLOW do não-terminal que estava no topo: é o que pode vir depois dele."

9. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ x = ; y = 2 ;
   BYTE: "Assim uma compilação só aponta vários erros. O programa não é gerado, mas você corrige tudo de uma vez."

10. TELA: # Receita ⏎ 1. Casa vazia, não-terminal no topo: descarta. ⏎ 2. Achou um token do FIRST do topo: continua. ⏎ 3. Achou um do FOLLOW do topo: tira o topo. ⏎ 4. Terminal no topo errado: tira o terminal.
   BYTE: "No analisador preditivo, o modo pânico segue quatro regras. Elas dizem o que fazer quando a casa da tabela está vazia."

11. TELA: # Receita ⏎ 1. Casa vazia, não-terminal no topo: descarta. ⏎ 2. Achou um token do FIRST do topo: continua. ⏎ 3. Achou um do FOLLOW do topo: tira o topo. ⏎ 4. Terminal no topo errado: tira o terminal.
   BYTE: "Ele parou num token que pode vir depois do topo. Qual regra vale? Toque."
   PERGUNTA (toque): certo = 3. Achou um do FOLLOW do topo: tira o topo. ; errado = 1. Casa vazia, não-terminal no topo: descarta. | 2. Achou um token do FIRST do topo: continua. | 4. Terminal no topo errado: tira o terminal.
   se errar: "O que pode vir depois está no FOLLOW."
   se acertar: "Isso. O topo sai da pilha e a análise segue."

12. TELA: # Receita ⏎ 1. Casa vazia, não-terminal no topo: descarta. ⏎ 2. Achou um token do FIRST do topo: continua. ⏎ 3. Achou um do FOLLOW do topo: tira o topo. ⏎ *4. Terminal no topo errado: tira o terminal.*
   BYTE: "Regra quatro: terminal no topo diferente do token. O parser tira o terminal da pilha, como se ele tivesse vindo, e avisa."

13. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ id = = num ; id = ; $
   BYTE: "Outra gramática: comandos de atribuição. A entrada tem dois erros."

14. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ id = = num ; id = ; $
   BYTE: "O parser lê da esquerda para a direita. Onde ele trava primeiro? Toque."
   PERGUNTA (toque): certo = = ; errado = id | = | num | ; | id | = | ; | $
   se errar: "Depois do igual vem um valor. O que veio?"
   se acertar: "Isso. Val está no topo, e chegou outro igual."

15. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ *Val -> id | num* ⏎ id = = num ; id = ; $ ⏎ erro aqui
   BYTE: "Ele descarta tokens até achar um seguro para Val. Onde ele para? Toque."
   PERGUNTA (toque): certo = num ; errado = ; | id | = | ; | $
   se errar: "Procure um token que pode começar um Val."
   se acertar: "Isso. O num está no FIRST de Val."

16. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ id = *num* ; id = ; $
   BYTE: "O igual sobrando vai fora. Val fica na pilha e casa com o num."

17. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ id = num ; id = *;* $ ⏎ tira Val descarta ;
   BYTE: "Segundo erro: topo Val, e veio ponto e vírgula. O que acontece? Toque."
   PERGUNTA (toque): certo = tira Val ; errado = descarta ;
   se errar: "Ponto e vírgula pode vir depois de Val. Ele está no FOLLOW."
   se acertar: "Isso. Val sai da pilha, e o ponto e vírgula casa."

18. TELA: # Modo pânico ⏎ Prog -> Cmd Prog | ε ⏎ Cmd -> id = Val ; ⏎ Val -> id | num ⏎ id = num ; id = ; *$*
   BYTE: "Dois erros avisados numa leitura só, e o parser chegou ao fim."

19. TELA: # Erros comuns ⏎ No modo pânico, o parser conserta o programa e gera o código. ⏎ certo errado
   BYTE: "O que a recuperação entrega no fim? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Ele jogou tokens fora. O que sobrou não é mais o seu programa."
   se acertar: "Isso. A recuperação serve para achar mais erros. Com erro de sintaxe, o compilador não gera código."

20. TELA: # Erros comuns ⏎ Certo: A recuperação só serve para avisar mais erros na mesma compilação. ⏎ *Errado: Sincronizar em qualquer token que aparecer.* ⏎ *Certo: Sincronize no FIRST ou no FOLLOW do topo, como o ; que fecha o comando.*
   BYTE: "Outro erro é parar de descartar cedo demais. Num token qualquer, o parser trava de novo e solta erros em cascata, que nem existem."

21. TELA: # Resumo ⏎ Erro: casa vazia na tabela, ou terminal do topo diferente do token. ⏎ Modo pânico: descarta até um token de sincronização (FIRST ou FOLLOW do topo). ⏎ Meta: avisar vários erros de uma vez, sem erros em cascata.
   BYTE: "Resumindo: no erro, o parser avisa, descarta até um token seguro e segue. O FOLLOW dá os tokens seguros. O objetivo é mostrar todos os erros numa compilação."

#### PEGADINHA: "Parar ou seguir" (5 cenas, ~34s)

1. TELA: # Dez erros, dez compilações ⏎ id = = num ; id
   BYTE: "Um compilador que para no primeiro erro faz você recompilar dez vezes."

2. TELA: # Dez erros, dez compilações ⏎ id = *=* *num* ; id
   BYTE: "No modo pânico, ele joga tokens fora até achar um bom ponto de recomeço."

3. TELA: # Dez erros, dez compilações ⏎ id = = num ; id
   BYTE: "Qual token é um bom ponto de recomeço nesta linha? Toque nele."
   PERGUNTA (toque): certo = ; ; errado = id | num
   se errar: "Procure o token que costuma fechar um comando."
   se acertar: "Isso. O ponto e vírgula marca o fim do comando."

4. TELA: # Dez erros, dez compilações ⏎ ; id
   PROF: "Recuperar não conserta o programa. Só deixa o compilador achar os próximos erros."

5. TELA: # Dez erros, dez compilações ⏎ ; id
   BYTE: "Depois do erro, o compilador não gera o programa. Ele só lista tudo de uma vez."

#### CLIPE 30s: "Modo pânico" (2 cenas, ~12s)

1. TELA: # Modo pânico ⏎ = = 2 ;
   BYTE: "Achou erro? O parser joga tokens fora até achar um ponto seguro."

2. TELA: # Modo pânico ⏎ = = 2 *;*
   BYTE: "O ponto e vírgula é o mais comum. Daí ele continua procurando erros."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Para Tudo — mentira: "No primeiro erro, o compilador tem que parar." / verdade: "Ele se recupera e segue, para mostrar vários erros."
    · [V] O modo pânico descarta tokens até um sincronizador. → Isso mesmo.
    · [V] Ponto e vírgula costuma ser bom sincronizador. → Isso mesmo.
    · [F] Recuperar do erro conserta o programa. → Só permite achar os próximos erros.

#### FICHA DO ASSUNTO
Em uma frase: O analisador detecta o erro no primeiro token que torna a entrada impossível (prefixo viável), relata linha e 'esperado × encontrado' e se recupera para achar mais erros; no LL(1), o modo pânico descarta tokens até um de FIRST(A) (continua) ou de FOLLOW(A) ∪ {$} (desempilha A).
Gancho: FIRST Fica, FOLLOW Foge. Pânico Pula, Frase Faz remendo, Produção Prevê, Global é Gênio.
Termos-chave: pânico, sincroniza, follow, descart, prefixo, erro
Erros comuns: Achar que recuperar um erro é corrigir o programa // Usar FIRST(A) como conjunto para desempilhar A (é FOLLOW(A)) // Esquecer o $ entre os tokens de sincronização // Achar que o erro é detectado depois do token problemático (é exatamente nele) // Confiar em todas as mensagens seguintes sem considerar o erro em cascata
Flashcards:
    · P: Quatro tipos de erro? → R: Léxico, sintático, semântico e lógico
    · P: Quatro estratégias de recuperação? → R: Modo pânico, nível de frase, produções de erro e correção global
    · P: O que é a propriedade do prefixo viável? → R: LL e LR acusam o erro no primeiro token que não pode continuar um prefixo válido
    · P: Modo pânico: token em FIRST(A)? → R: Para de descartar e continua com A
    · P: Modo pânico: token em FOLLOW(A) ∪ {$}? → R: Desempilha A (sincronizou)
    · P: Terminal no topo diferente da entrada? → R: Desempilha o terminal (finge que estava lá)
    · P: Tokens de sincronização típicos em C? → R: ';' e '}'
    · P: Token especial do Bison para recuperação? → R: error (ex.: comando : error ';')
    · P: Para que serve yyerrok? → R: Sair do modo de recuperação e voltar a relatar erros na hora
