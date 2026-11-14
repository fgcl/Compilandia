# Região 3: Vale das Gramáticas — Regras que geram linguagens
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO CFG_DERIVATION: Gramáticas e derivações
Pré-requisitos no jogo: Fases de um compilador
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Regras de troca» → Fazer com ajuda → Programar o Byte → Montar na Fábrica → Prática: você pilota «Derive até chegar» → Pegadinha «Duas derivações, uma árvore» → Desafio
Jogos que treinam: Derivação Turbo (escolher produções e crescer a árvore), Duas Árvores (achar duas árvores para a mesma frase)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: Fábrica de frases

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug comeu uma troca
Na tela: S -> a S b | c
BYTE: "Antes da aula: qual frase sai dessa regra?"
Opções: ✔a c b | a b c | c c
Depois: "Vale a c b. S vira a S b, e o S de dentro vira c."

#### AULA ANIMADA BÁSICA: "Regras de troca" (29 cenas, ~231s) [1º exemplo começa na cena 5]

1. TELA: # Frase certa ou errada? ⏎ n + n ⏎ n + +
   BYTE: "O parser recebe os tokens do scanner. Ele precisa saber quais sequências de tokens são frases válidas."

2. TELA: # Frase certa ou errada? ⏎ n + n ⏎ n + +
   BYTE: "Uma destas frases está mal escrita. Toque nela."
   PERGUNTA (toque): certo = n + + ; errado = n + n
   se errar: "Depois de um mais, precisa vir um número."
   se acertar: "Falta um número depois do mais."

3. TELA: # Frase certa ou errada? ⏎ n + n ⏎ n + + ⏎ ...
   BYTE: "Listar todas as frases certas não dá: elas são infinitas. E expressão regular não basta, porque não sabe casar parênteses."

4. TELA: !!GRAMÁTICA!!
   BYTE: "Uma gramática livre de contexto é uma lista curta de regras de troca que gera todas as frases válidas."

5. TELA: # Gramática ⏎ E -> E + E ⏎ E -> n
   BYTE: "Cada regra, ou produção, diz que o símbolo da esquerda pode ser trocado pelo lado direito. Aqui, E pode virar E mais E, ou pode virar n."

6. TELA: # Gramática ⏎ E -> E + E ⏎ E -> n ⏎ + ⏎ E ⏎ n
   BYTE: "Nesta gramática, qual símbolo as regras ainda trocam por outra coisa? Toque."
   PERGUNTA (toque): certo = E ; errado = + | n
   se errar: "É o símbolo que aparece do lado esquerdo das regras."
   se acertar: "O E. Ele está do lado esquerdo, então uma regra pode trocá-lo."

7. TELA: # Gramática ⏎ E -> E + E ⏎ E -> n ⏎ *E*
   BYTE: "E é um não-terminal: ainda vai ser trocado. O mais e o n são terminais: são os tokens, e ficam na frase."

8. TELA: # Gramática ⏎ *E -> E + E* ⏎ E -> n ⏎ E ⏎ + ⏎ E
   BYTE: "Começo pelo símbolo inicial, E, e aplico a primeira regra. O que aparece no meio do caminho, como E mais E, se chama forma sentencial."

9. TELA: # Gramática ⏎ E -> E + E ⏎ E -> n ⏎ E ⏎ + ⏎ E
   BYTE: "Numa derivação mais à esquerda, qual E eu troco agora? Toque nele."
   PERGUNTA (toque): certo = E ; errado = E
   se errar: "O nome já conta: mais à esquerda."
   se acertar: "Esse mesmo. Sempre o não-terminal mais à esquerda."

10. TELA: # Gramática ⏎ E -> E + E ⏎ *E -> n* ⏎ n ⏎ + ⏎ n
   BYTE: "Ele vira n. Depois troco o outro, e sobra a frase n mais n."

11. TELA: # Gramática ⏎ E -> E + E ⏎ E -> n ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ n
   BYTE: "Só sobraram terminais. Uma forma só com terminais é uma sentença, e ela pertence à linguagem. O desenho dessas trocas é a árvore de derivação."

12. TELA: # A regra vazia ⏎ *S -> a S b | ε* ⏎ a ⏎ S ⏎ b
   BYTE: "Uma regra pode ter o lado direito vazio. Escrevemos épsilon. Aqui, S vai para épsilon apaga o S. Já troquei S por a S b."

13. TELA: # A regra vazia ⏎ S -> a S b | ε ⏎ a ⏎ S ⏎ b ⏎ a S b ⏎ a b ⏎ b a
   BYTE: "Qual destas é uma sentença, só com terminais, que sai de S? Toque."
   PERGUNTA (toque): certo = a b ; errado = a S b | b a
   se errar: "Troque o S do meio por épsilon. O que sobra?"
   se acertar: "a b. O a S b ainda tem não-terminal: é uma forma sentencial. E b a não sai dessa gramática."

14. TELA: # Receita ⏎ 1. Comece pelo símbolo inicial. ⏎ 2. Troque o não-terminal mais à esquerda. ⏎ 3. Repita até sobrar só terminal.
   BYTE: "Derivar uma frase tem três passos. Eles ficam aqui na tela."

15. TELA: # Receita ⏎ 1. Comece pelo símbolo inicial. ⏎ 2. Troque o não-terminal mais à esquerda. ⏎ 3. Repita até sobrar só terminal. ⏎ E + E
   BYTE: "Na forma E mais E, qual passo escolhe o E da esquerda? Toque."
   PERGUNTA (toque): certo = 2. Troque o não-terminal mais à esquerda. ; errado = 1. Comece pelo símbolo inicial. | 3. Repita até sobrar só terminal.
   se errar: "Um dos passos diz qual não-terminal trocar."
   se acertar: "Certo. Passo dois: sempre o mais à esquerda."

16. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Decl
   BYTE: "Agora quem deriva é você. A meta é a declaração lá em cima."

17. TELA: # float * id ; ⏎ *Decl -> Tipo Var ;* ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Tipo ⏎ Var ⏎ ;
   BYTE: "Decl virou Tipo, Var e ponto e vírgula. Qual você troca agora?"
   PERGUNTA (toque): certo = Tipo ; errado = Var | ;
   se errar: "Passo dois: o não-terminal mais à esquerda."
   se acertar: "Tipo vem primeiro."

18. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ *Tipo -> int | float* ⏎ Var -> id | * id ⏎ *Tipo* ⏎ Var ⏎ ; ⏎ Tipo -> int ⏎ Tipo -> float
   BYTE: "Qual regra de Tipo leva até a meta? Toque nela."
   PERGUNTA (toque): certo = Tipo -> float ; errado = Tipo -> int
   se errar: "Olhe a primeira palavra da meta."
   se acertar: "Tipo vira float."

19. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ *Var -> id | * id* ⏎ Var -> id ⏎ Var -> * id ⏎ float ⏎ *Var* ⏎ ;
   BYTE: "Falta Var. Na meta, um asterisco vem antes do id. Toque na regra."
   PERGUNTA (toque): certo = Var -> * id ; errado = Var -> id
   se errar: "Só uma regra de Var começa com asterisco."
   se acertar: "Exato. Sobrou só terminal, e é a meta."

20. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Decl ⏎ Tipo ⏎ float ⏎ Var ⏎ * ⏎ id ⏎ ;
   BYTE: "Três trocas, uma por vez. Esta é a árvore da declaração."

21. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ int id id ; ⏎ Sim ⏎ Não
   BYTE: "E a frase int id id ponto e vírgula? Ela está na linguagem?"
   PERGUNTA (toque): certo = Não ; errado = Sim
   se errar: "Procure uma regra que ponha dois id seguidos."
   se acertar: "Nenhuma regra põe dois id seguidos."

22. TELA: # float * id ; ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ int id id ;
   PROF: "Sem derivação que chegue nela, a frase fica fora da linguagem. É assim que o parser acusa erro de sintaxe."

23. TELA: # Mais à direita ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Tipo ⏎ Var ⏎ ;
   BYTE: "Há outra ordem: a derivação mais à direita troca sempre o não-terminal mais à direita. É ela que o parser de baixo para cima refaz, de trás para a frente."

24. TELA: # Mais à direita ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Tipo ⏎ Var ⏎ ;
   BYTE: "Em Tipo, Var e ponto e vírgula, qual a mais à direita troca primeiro? Toque."
   PERGUNTA (toque): certo = Var ; errado = Tipo | ;
   se errar: "Terminal não se troca. Dos não-terminais, pegue o da direita."
   se acertar: "Var. O ponto e vírgula é terminal, então o Var é o não-terminal mais à direita."

25. TELA: # Mais à direita ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ float Var ; ⏎ Tipo * id ; ⏎ float * id ;
   BYTE: "Qual destas formas só aparece na derivação mais à direita? Toque."
   PERGUNTA (toque): certo = Tipo * id ; ; errado = float Var ; | float * id ;
   se errar: "Na mais à direita, a derivação troca o Tipo por último."
   se acertar: "Tipo, asterisco, id e ponto e vírgula. A da esquerda troca o Tipo antes e passa por float Var."

26. TELA: # Mais à direita ⏎ Decl -> Tipo Var ; ⏎ Tipo -> int | float ⏎ Var -> id | * id ⏎ Decl ⏎ Tipo Var ; ⏎ *float Var ;* ⏎ float * id ; ⏎ Decl ⏎ Tipo Var ; ⏎ *Tipo * id ;* ⏎ float * id ;
   BYTE: "As duas chegam à mesma sentença, com a mesma árvore. Só muda a ordem das trocas."

27. TELA: # Erros comuns ⏎ Na derivação mais à esquerda, posso trocar qualquer não-terminal em cada passo. ⏎ certo ⏎ errado
   BYTE: "Um colega descreveu assim a derivação mais à esquerda. Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O nome diz qual não-terminal é trocado."
   se acertar: "Em cada passo, troca-se sempre o não-terminal mais à esquerda. Na mais à direita, sempre o da direita."

28. TELA: # Erros comuns ⏎ Certo: Mais à esquerda: troque sempre o primeiro não-terminal da forma. ⏎ *Errado: Trocar um terminal por uma regra.* ⏎ *Certo: Só não-terminal fica do lado esquerdo. Terminal é token e não muda.*
   BYTE: "Outro erro: tentar trocar um terminal. Terminal é token, não tem regra. Só não-terminal fica do lado esquerdo de uma produção."

29. TELA: # Resumo ⏎ Gramática: terminais, não-terminais, produções e um símbolo inicial. ⏎ Derivar: começar no inicial e trocar não-terminais até sobrar só terminal. ⏎ A frase está na linguagem se existe uma derivação para ela.
   BYTE: "Resumindo: a gramática livre de contexto gera as frases por trocas. Derivar é aplicar produções a partir do símbolo inicial, e a árvore registra essas trocas."

#### PEGADINHA: "Duas derivações, uma árvore" (5 cenas, ~28s)

1. TELA: # a b ⏎ S -> A B ⏎ A -> a ⏎ B -> b
   BYTE: "Olhe esta gramática pequena. A frase é a b."

2. TELA: # a b ⏎ S -> A B ⏎ A -> a ⏎ B -> b ⏎ S  A B  a B  a b
   BYTE: "Pela esquerda: S vira A B, depois a B, depois a b."

3. TELA: # a b ⏎ S -> A B ⏎ A -> a ⏎ B -> b ⏎ S  A B  a B  a b ⏎ S  A B  A b  a b
   BYTE: "Pela direita: S vira A B, depois A b, depois a b."

4. TELA: # a b ⏎ S -> A B ⏎ A -> a ⏎ B -> b ⏎ S  A B  a B  a b ⏎ S  A B  A b  a b ⏎ é ambígua ⏎ não é
   BYTE: "São duas derivações diferentes. Isso é ambiguidade?"
   PERGUNTA (toque): certo = não é ; errado = é ambígua
   se errar: "Ambiguidade pede duas árvores, ou duas derivações do mesmo lado."
   se acertar: "As duas montam a mesma árvore. Só muda a ordem."

5. TELA: # a b ⏎ S -> A B ⏎ A -> a ⏎ B -> b ⏎ S  A B  a B  a b ⏎ S  A B  A b  a b
   PROF: "Para provar ambiguidade, compare duas derivações pela esquerda, ou duas pela direita."

#### PRATICA: "Derive até chegar" (5 cenas, ~32s)

1. TELA: # id + id ⏎ E -> T X ⏎ X -> + T X | ε ⏎ T -> id
   BYTE: "A frase alvo é id mais id. As regras estão aqui."

2. TELA: # id + id ⏎ *E -> T X* ⏎ X -> + T X | ε ⏎ T -> id
   BYTE: "Troque sempre o não terminal mais à esquerda."

3. TELA: # id + id ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id
   PROF: "O X decide se a frase continua. Mais uma soma, ou épsilon para acabar."

4. TELA: chegue nesta frase ⏎ id ⏎ + ⏎ id ⏎ *E*
   BYTE: "Sua vez. Escolha as regras até chegar na frase."
   BRINQUEDO: dica "Essa escolha saiu da frase. Comece de novo." / acerto "Chegou. Isso é uma derivação mais à esquerda."

5. TELA: # id + id ⏎ E -> T X ⏎ *X -> + T X | ε* ⏎ T -> id
   BYTE: "Quando você escolhe certo sem pensar, já faz o trabalho do parser do Templo. Ele olha o próximo token e escolhe a regra."

#### CLIPE 30s: "Gramática em 30 segundos" (2 cenas, ~12s)

1. TELA: # S -> a S b | c ⏎ S
   BYTE: "Uma gramática é uma lista de trocas."

2. TELA: # S -> a S b | c ⏎ a ⏎ c ⏎ b
   BYTE: "Comece no símbolo inicial e troque até sobrar só terminal. O que sobrar é uma frase."

#### CLIPE 30s: "Terminal e não terminal" (2 cenas, ~10s)

1. TELA: # Terminal ⏎ id
   BYTE: "Terminal é peça final, como id. Ele fica na frase."

2. TELA: # Não terminal ⏎ id ⏎ E
   BYTE: "Não terminal ainda vai virar outra coisa, como E."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Terminal Trocado — mentira: "Terminal é o que ainda vai virar outra coisa." / verdade: "Não terminal é o que se troca. Terminal fica até o fim."
    · [V] As folhas da árvore de uma frase são terminais, ou ε quando uma regra some. → Isso mesmo.
    · [V] Uma frase só tem terminais. → Isso mesmo.
    · [F] Forma sentencial nunca tem não terminal. → Ela pode ter os dois tipos de símbolo.

#### FICHA DO ASSUNTO
Em uma frase: Uma gramática livre de contexto (N, T, P, S) gera sentenças trocando não-terminais pelos lados direitos das produções; a árvore de derivação resume essas trocas.
Gancho: G = (N, T, P, S): Nunca Tire Prova Sem estudar.
Termos-chave: terminal, não-terminal, produção, símbolo inicial, derivação, árvore
Erros comuns: Numa derivação mais à esquerda, trocar um NT que não é o mais à esquerda. // Achar que as derivações ⇒lm e ⇒rm geram árvores diferentes. // Confundir forma sentencial (pode ter NTs) com sentença (só terminais). // Achar que ε não pode ser folha da árvore.
Flashcards:
    · P: Os 4 componentes de uma GLC? → R: Não-terminais, terminais, produções, símbolo inicial
    · P: O que é uma forma sentencial? → R: Qualquer α tal que S ⇒* α
    · P: O que é uma sentença? → R: Uma forma sentencial só com terminais
    · P: Derivação mais à esquerda? → R: Sempre substitui o NT mais à esquerda
    · P: Quantas derivações ⇒lm tem uma árvore? → R: Exatamente uma
    · P: Folhas da árvore lidas da esquerda para a direita? → R: O produto da árvore: a sentença
    · P: Linguagem de S → a S b | ε? → R: { aⁿbⁿ | n ≥ 0 }
    · P: Por que 'livre de contexto'? → R: O lado esquerdo é um único NT, trocado sem olhar o que está em volta

#### GLOSSÁRIO (balão ao tocar no termo)
- gramática: Um conjunto de regras de troca que diz quais frases a linguagem aceita.
- terminal: Símbolo que aparece no programa de verdade, como id ou mais.
- não-terminal: Símbolo que ainda vai ser trocado por uma regra, como E ou T.
- derivação: A sequência de trocas que leva do símbolo inicial até a frase.


## ASSUNTO AMBIGUITY: Árvores e ambiguidade
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Duas árvores, uma frase» → Montar na Fábrica → Pegadinha «O else solto» → Desafio
Jogos que treinam: Derivação Turbo (escolher produções e crescer a árvore), Duas Árvores (achar duas árvores para a mesma frase)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug fez duas árvores
Na tela: 2 + 3 * 4
BYTE: "Antes da aula: se o mais rodar primeiro, dá quanto?"
Opções: 14 | ✔20 | 24
Depois: "Vinte. Duas árvores, dois resultados."

#### AULA ANIMADA BÁSICA: "Duas árvores, uma frase" (26 cenas, ~202s) [1º exemplo começa na cena 4]

1. TELA: # Qual conta vem primeiro? ⏎ 2 + 3 * 4
   BYTE: "Dois mais três vezes quatro dá catorze, porque o vezes vem antes."

2. TELA: # Qual conta vem primeiro? ⏎ 2 + 3 * 4 ⏎ 14 ⏎ 20 ⏎ 24
   BYTE: "Se alguém somar antes, o resultado muda. Toque no número que sai."
   PERGUNTA (toque): certo = 20 ; errado = 14 | 24
   se errar: "Some dois com três. Depois multiplique por quatro."
   se acertar: "Vinte, e não catorze."

3. TELA: # Qual conta vem primeiro? ⏎ 2 + 3 * 4 ⏎ 20
   BYTE: "No compilador, quem decide a ordem das contas é a forma da árvore que o parser monta."

4. TELA: # n + n * n ⏎ E -> E + E | E * E | n
   BYTE: "Esta gramática aceita mais e vezes. Vou montar a árvore da frase n mais n vezes n."

5. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ E ⏎ n ⏎ * ⏎ E ⏎ n
   BYTE: "Na árvore, a conta mais funda sai primeiro. Toque no operador que o compilador calcula primeiro."
   PERGUNTA (toque): certo = * ; errado = n | + | n | n
   se errar: "Procure o operador mais longe da raiz."
   se acertar: "O vezes. Ele está mais fundo, então sai primeiro."

6. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ E ⏎ n ⏎ *** ⏎ E ⏎ n
   BYTE: "Nesta árvore o vezes fica mais fundo. Ele é calculado primeiro, como na matemática."

7. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ E ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ n ⏎ * ⏎ E ⏎ n
   BYTE: "Mas dá para montar outra árvore, com o mais embaixo."

8. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ *E* ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ n ⏎ * ⏎ E ⏎ n
   PROF: "Uma mesma frase com duas árvores de derivação diferentes: a gramática é ambígua."

9. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ E ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ n ⏎ * ⏎ E ⏎ n ⏎ o + ⏎ o *
   BYTE: "Nesta árvore, o que roda primeiro? Toque na resposta."
   PERGUNTA (toque): certo = o + ; errado = o *
   se errar: "Quem está mais fundo na árvore acontece antes."
   se acertar: "Certo. Aqui o mais roda primeiro, e o resultado sai errado."

10. TELA: # n + n * n ⏎ E -> E + E | E * E | n ⏎ E ⏎ E ⏎ E ⏎ n ⏎ + ⏎ E ⏎ n ⏎ * ⏎ E ⏎ n
   BYTE: "Um compilador não pode ter duas interpretações. O conserto é reescrever a gramática, com um nível por precedência, ou declarar a precedência no Bison."

11. TELA: # Receita ⏎ 1. Escolha uma frase da linguagem. ⏎ 2. Tente montar duas árvores diferentes para ela. ⏎ 3. Achou duas? A gramática é ambígua.
   BYTE: "Para mostrar que uma gramática é ambígua, a receita tem três passos."

12. TELA: # Receita ⏎ 1. Escolha uma frase da linguagem. ⏎ 2. Tente montar duas árvores diferentes para ela. ⏎ *3. Achou duas? A gramática é ambígua.* ⏎ Sim ⏎ Não
   BYTE: "Uma frase deu uma árvore só. Isso prova que a gramática não é ambígua?"
   PERGUNTA (toque): certo = Não ; errado = Sim
   se errar: "Outra frase ainda pode ter duas árvores."
   se acertar: "Boa. Uma frase basta para provar a ambiguidade, mas não o contrário."

13. TELA: # n - n - n ⏎ E -> E - E | n
   BYTE: "Outra gramática, agora só com menos. A frase é n menos n menos n."

14. TELA: # n - n - n ⏎ E -> E - E | n ⏎ E ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ n ⏎ - ⏎ E ⏎ n
   BYTE: "Nesta árvore, qual menos acontece primeiro? Toque nele."
   PERGUNTA (toque): certo = - ; errado = -
   se errar: "Quem está mais fundo na árvore acontece antes."
   se acertar: "Primeiro o menos da esquerda, como na escola."

15. TELA: # n - n - n ⏎ E -> E - E | n ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ n
   BYTE: "Achei outra árvore para a mesma frase. Aqui, qual menos vem primeiro?"
   PERGUNTA (toque): certo = - ; errado = -
   se errar: "Quem está mais fundo na árvore acontece antes."
   se acertar: "Aqui o menos da direita vem primeiro."

16. TELA: # n - n - n ⏎ E -> E - E | n ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ E ⏎ n ⏎ *-* ⏎ E ⏎ n ⏎ 9 - 5 - 2 ⏎ 2 ⏎ 6 ⏎ 16
   BYTE: "Com nove, cinco e dois, quanto dá esta árvore? Toque no número."
   PERGUNTA (toque): certo = 6 ; errado = 2 | 16
   se errar: "Faça cinco menos dois antes. Depois tire o resultado de nove."
   se acertar: "Acertou. Seis. A outra árvore dava dois."

17. TELA: # n - n - n ⏎ E -> E - E | n ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ E ⏎ n ⏎ - ⏎ E ⏎ n ⏎ 9 - 5 - 2 ⏎ *6*
   PROF: "Duas árvores para a mesma frase: pela receita, a gramática é ambígua. Aqui, ainda por cima, as contas dão resultados diferentes."

18. TELA: # n - n - n ⏎ E -> E - E | n ⏎ Depois ⏎ *E -> E - n | n*
   BYTE: "Conserto: à direita do menos, a regra nova só aceita um n. A recursão fica só à esquerda, e o menos associa à esquerda."

19. TELA: # n - n - n ⏎ E -> E - E | n ⏎ Depois ⏎ *E -> E - n | n* ⏎ 1 ⏎ 2
   BYTE: "Com a regra nova, quantas árvores tem n menos n menos n?"
   PERGUNTA (toque): certo = 1 ; errado = 2
   se errar: "À direita do menos só cabe um n."
   se acertar: "Agora só existe uma árvore."

20. TELA: # n - n - n ⏎ E -> E - n | n ⏎ E ⏎ E ⏎ E ⏎ n ⏎ - ⏎ n ⏎ - ⏎ n
   BYTE: "Esta é a única árvore. O menos associa à esquerda, como na matemática."

21. TELA: # n + n * n ⏎ E -> E + T | T ⏎ *T -> T * n | n* ⏎ E ⏎ E ⏎ T ⏎ n ⏎ + ⏎ T ⏎ T ⏎ n ⏎ * ⏎ n
   BYTE: "E o primeiro exemplo, n mais n vezes n? O conserto usa dois níveis. E junta somas de T, e T junta produtos. Agora a frase tem uma árvore só."

22. TELA: # n + n * n ⏎ E -> E + T | T ⏎ T -> T * n | n ⏎ O vezes aparece depois na frase ⏎ O T só gera vezes, e o mais fica no E ⏎ A regra do vezes vem por último
   BYTE: "Por que o vezes fica sempre mais fundo que o mais? Toque na razão."
   PERGUNTA (toque): certo = O T só gera vezes, e o mais fica no E ; errado = O vezes aparece depois na frase | A regra do vezes vem por último
   se errar: "Olhe o que cada não-terminal consegue gerar. Onde um mais pode aparecer?"
   se acertar: "O mais só nasce na regra de E, que fica acima dos T. Dentro de um T só cabem vezes. Quem está mais fundo roda primeiro."

23. TELA: # n + n * n ⏎ *E -> E + T | T* ⏎ *T -> T * n | n*
   BYTE: "Um nível para cada precedência: o operador que liga mais forte fica no nível de baixo. É a gramática que a Ponte vai consertar a seguir."

24. TELA: # Erros comuns ⏎ Achei duas derivações diferentes para a frase. Então a gramática é ambígua. ⏎ certo ⏎ errado
   BYTE: "Duas derivações diferentes da mesma frase provam ambiguidade? Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "A mais à esquerda e a mais à direita podem desenhar a mesma árvore."
   se acertar: "O que conta são duas árvores diferentes, ou duas derivações mais à esquerda. Mudar só a ordem das trocas não prova nada."

25. TELA: # Erros comuns ⏎ Certo: Ambígua: uma frase com duas árvores, ou duas derivações mais à esquerda. ⏎ *Errado: Testei três frases, cada uma deu uma árvore. Logo, não é ambígua.* ⏎ *Certo: Exemplos não provam que não há ambiguidade. Um contraexemplo prova que há.*
   BYTE: "Outro erro: concluir que a gramática não é ambígua porque algumas frases deram uma árvore só. Só o contraexemplo prova, e ele prova a ambiguidade."

26. TELA: # Resumo ⏎ Ambiguidade: alguma frase tem duas árvores de derivação. ⏎ Para provar, mostre uma frase e as duas árvores. ⏎ Conserto: um nível por precedência, e a recursão do lado da associatividade.
   BYTE: "Resumindo: gramática ambígua dá duas árvores para a mesma frase, e duas árvores são duas contas. Conserta-se com níveis de precedência e com a recursão do lado certo."

#### PEGADINHA: "O else solto" (6 cenas, ~39s)

1. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   BYTE: "Dois ifs e um else só. De qual if é esse else?"

2. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   BYTE: "Uma leitura liga o else ao if de dentro."

3. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   BYTE: "A outra liga o else ao if de fora. Duas árvores, a gramática é ambígua."

4. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   BYTE: "A regra das linguagens: o else fica com o if mais perto. Toque nesse if."
   PERGUNTA (toque): certo = if 2 ; errado = if 1
   se errar: "Ande para trás a partir do else. Qual if aparece primeiro?"
   se acertar: "O else fica com o if de dentro."

5. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   PROF: "Na prática, as ferramentas escolhem a árvore em que o else gruda no if de dentro. Você vê como na Oficina do Bison."

6. TELA: # if a if b x else y ⏎ if 1 ⏎ if 2 ⏎ else
   BYTE: "Se quiser o if de fora, use chaves. Elas fecham o if de dentro."

#### CLIPE 30s: "O que é ambiguidade" (2 cenas, ~12s)

1. TELA: # 2 + 3 * 4 ⏎ (2 + 3) * 4 = 20
   BYTE: "Uma gramática é ambígua quando a mesma frase tem duas árvores."

2. TELA: # 2 + 3 * 4 ⏎ (2 + 3) * 4 = 20 ⏎ 2 + (3 * 4) = 14
   BYTE: "Duas árvores dão dois sentidos, e o compilador precisa de um."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Else Solto — mentira: "O else é do if de fora." / verdade: "O else fica com o if mais perto."
    · [V] if a then if b then x else y tem duas árvores nessa gramática. → Isso mesmo.
    · [V] Na prática, o else fica com o if mais perto dele. → Isso mesmo.
    · [F] Reescrever a gramática nunca resolve o else solto. → Dá para reescrever separando comandos fechados e abertos.
- Bug das Duas Contas — mentira: "Ambiguidade é só detalhe de desenho." / verdade: "Duas árvores dão dois resultados: 2 + 3 * 4 vira 14 ou 20."
    · [V] Gramática ambígua tem uma frase com duas árvores. → Isso mesmo.
    · [V] Duas derivações pela esquerda diferentes da mesma frase provam ambiguidade. → Isso mesmo.
    · [F] Toda gramática de expressões é ambígua. → Com níveis E, T e F ela deixa de ser.

#### FICHA DO ASSUNTO
Em uma frase: Uma gramática é ambígua se alguma sentença tem duas árvores de derivação (duas derivações mais à esquerda); resolve-se com níveis de precedência ou regras de desambiguação.
Gancho: Ambi = dois: duas árvores para a mesma frase. Mais precedência = mais fundo na árvore.
Termos-chave: ambígua, duas árvores, mais à esquerda, precedência, associatividade
Erros comuns: Tentar provar ambiguidade com uma derivação ⇒lm e outra ⇒rm. // Achar que recursão à esquerda causa ambiguidade. // Achar que ambiguidade é sempre propriedade da linguagem, e não da gramática. // Achar que existe algoritmo geral para detectar ambiguidade (é indecidível).
Flashcards:
    · P: Quando uma GLC é ambígua? → R: Quando alguma sentença tem mais de uma árvore de derivação
    · P: Como provar ambiguidade? → R: Duas derivações ⇒lm (ou duas árvores) para a mesma sentença
    · P: Sentença ambígua clássica? → R: id + id * id em E → E + E | E * E | ( E ) | id
    · P: Gramática de expressões não ambígua? → R: E → E + T | T; T → T * F | F; F → ( E ) | id
    · P: Onde fica o operador de maior precedência na árvore? → R: Mais fundo (longe da raiz)
    · P: Recursão à esquerda dá qual associatividade? → R: À esquerda
    · P: Else pendurado: com quem o else casa? → R: Com o if mais próximo sem else
    · P: Decidir se uma GLC é ambígua é decidível? → R: Não, é indecidível

#### GLOSSÁRIO (balão ao tocar no termo)
- ambígua: Gramática em que a mesma frase tem duas árvores diferentes.
