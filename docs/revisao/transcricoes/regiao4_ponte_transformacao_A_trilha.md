# Região 4: Ponte da Transformação — Consertar gramáticas
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO LEFT_RECURSION: Recursão à esquerda
Pré-requisitos no jogo: Árvores e ambiguidade
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O laço sem fim» → Montar na Fábrica → Prática: você pilota «Conserte E, T e F» → Pegadinha «A recursão escondida» → Desafio
Jogos que treinam: Cirurgia de Gramática (montar a gramática consertada com peças), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug prendeu o parser
Na tela: E -> E + T | T
BYTE: "Antes da aula: o que o parser descendente faz aqui?"
Opções: lê o + | ✔gira sem ler | aceita
Depois: "Gira sem ler nada. Ele troca E por E para sempre."

#### AULA ANIMADA BÁSICA: "O laço sem fim" (28 cenas, ~224s) [1º exemplo começa na cena 4]

1. TELA: # Rumo ao Templo ⏎ *n* ⏎ + ⏎ n ⏎ um token por vez, sem voltar
   BYTE: "No Templo, você vai programar um parser descendente. Ele lê um token por vez e nunca volta atrás."

2. TELA: # E -> E + n | n ⏎ void E(void) { ⏎     E(); ⏎     casa('+'); ⏎     casa(NUM); ⏎ }
   BYTE: "Num parser descendente, cada não-terminal vira uma função. A função segue o lado direito da regra, da esquerda para a direita."

3. TELA: # E -> E + n | n ⏎ void E(void) { ⏎     E(); ⏎     casa('+'); ⏎     casa(NUM); ⏎ }
   BYTE: "Antes de ler qualquer token, o que a função E faz? Toque na linha."
   PERGUNTA (toque): certo =     E(); ; errado =     casa('+'); |     casa(NUM);
   se errar: "Olhe a primeira linha dentro da função."
   se acertar: "Ela chama a si mesma, sem ler nada."

4. TELA: # E -> E + n | n ⏎ E
   BYTE: "Olhe esta regra. O lado direito de E começa com o próprio E."

5. TELA: # E -> E + n | n ⏎ E ⏎ E ⏎ E ⏎ ...
   BYTE: "Um parser de cima para baixo procura E. Para isso, procura E. Para isso, procura E."

6. TELA: !!RECURSÃO À ESQUERDA!!
   BYTE: "Regra em que o lado direito começa com o próprio lado esquerdo tem recursão à esquerda. Nenhum parser descendente aceita isso."

7. TELA: # Qual trava? ⏎ A -> A b ⏎ A -> b A ⏎ A -> c
   BYTE: "Qual destas regras tem recursão à esquerda? Toque nela."
   PERGUNTA (toque): certo = A -> A b ; errado = A -> b A | A -> c
   se errar: "Procure a regra cujo lado direito começa com o próprio lado esquerdo."
   se acertar: "É isso. A começa com A, então o parser não sai do lugar."

8. TELA: # Antes ⏎ E -> E + n | n ⏎ Depois ⏎ E  -> n E' ⏎ E' -> + n E' | ε
   BYTE: "O conserto: a alternativa sem recursão vem primeiro. O que se repetia vai para um não-terminal novo, com recursão à direita."

9. TELA: # Antes ⏎ E -> E + n | n ⏎ Depois ⏎ E  -> n E' ⏎ *E' -> + n E' | ε*
   BYTE: "A linguagem é a mesma: um n, depois zero ou mais vezes mais n. Só mudou o lado da recursão, e com ele a forma da árvore."

10. TELA: # E -> E - n | n ⏎ E ⏎ *E* ⏎ E ⏎ 9 ⏎ - ⏎ 5 ⏎ - ⏎ 2 ⏎ (9 - 5) - 2 = 2
   BYTE: "Agora troque o mais pelo menos. Na gramática velha, a árvore de nove menos cinco menos dois pende para a esquerda. O nove menos cinco fica embaixo e sai primeiro: dá dois."

11. TELA: # E -> n E'   E' -> - n E' | ε ⏎ E ⏎ 9 ⏎ *E'* ⏎ - ⏎ 5 ⏎ E' ⏎ - ⏎ 2 ⏎ E' ⏎ ε ⏎ 9 - (5 - 2) = 6 ?
   BYTE: "Na gramática nova, a mesma frase dá uma árvore que pende para a direita. Lida ao pé da letra, ela faria cinco menos dois primeiro, e daria seis."

12. TELA: # E -> n E'   E' -> - n E' | ε ⏎ E ⏎ 9 ⏎ *E'* ⏎ - ⏎ 5 ⏎ E' ⏎ - ⏎ 2 ⏎ E' ⏎ ε ⏎ Não ⏎ Sim
   BYTE: "Então o menos passou a juntar pela direita? Toque na resposta."
   PERGUNTA (toque): certo = Não ; errado = Sim
   se errar: "A gramática nova gera as mesmas frases. Quem decide a conta é o código que monta o resultado."
   se acertar: "A linguagem é a mesma, mas a árvore mudou. O parser junta os pedaços num laço, da esquerda para a direita. A conta volta a dar dois. Você vai ver esse laço no assunto da AST."

13. TELA: # Receita ⏎ 1. Separe: A → A α tem laço, A → β não tem. ⏎ 2. Troque cada A → β por A → β A'. ⏎ 3. Crie A' → α A' | ε. ⏎ A -> A α | β
   BYTE: "O conserto tem três passos. Ele vale para qualquer regra desse tipo."

14. TELA: # Receita ⏎ *1. Separe: A → A α tem laço, A → β não tem.* ⏎ 2. Troque cada A → β por A → β A'. ⏎ 3. Crie A' → α A' | ε. ⏎ E -> E + n | n ⏎ + n ⏎ n
   BYTE: "Aqui, qual alternativa faz o papel do beta, a que não tem laço?"
   PERGUNTA (toque): certo = n ; errado = + n
   se errar: "O beta não começa com E."
   se acertar: "O n é o beta, e o mais n é o alfa."

15. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id
   BYTE: "Uma lista de ids separados por vírgula. Agora quem conserta é você."

16. TELA: # Lista de ids ⏎ *L -> L , id | id* ⏎ L , id ⏎ id
   BYTE: "Passo um: qual alternativa começa com o próprio L? Toque nela."
   PERGUNTA (toque): certo = L , id ; errado = id
   se errar: "Procure a alternativa cujo primeiro símbolo é o L."
   se acertar: "Certo. Ela tem o laço. A vírgula e o id formam o alfa."

17. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id ⏎ L -> id L' ⏎ L -> L' id ⏎ L -> , id L'
   BYTE: "Passo dois: como fica a regra nova de L? Toque nela."
   PERGUNTA (toque): certo = L -> id L' ; errado = L -> L' id | L -> , id L'
   se errar: "A parte sem laço vem primeiro. O L linha vai no fim."
   se acertar: "Primeiro o id, depois a regra nova."

18. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id ⏎ L  -> id L' ⏎ , id L' ⏎ ε ⏎ L , id ⏎ id L'
   BYTE: "Passo três: o que entra em L linha? Toque nas duas alternativas."
   PERGUNTA (toque): certo = , id L' | ε ; errado = L , id | id L'
   se errar: "O alfa seguido de L linha, e o épsilon para parar."
   se acertar: "Vírgula e id repetem, e o épsilon encerra."

19. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id ⏎ *L  -> id L'* ⏎ *L' -> , id L' | ε*
   BYTE: "Pronto. Nenhuma regra começa com o próprio lado esquerdo."

20. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id ⏎ L  -> id L' ⏎ L' -> , id L' | ε ⏎ id , id ⏎ Sim ⏎ Não
   BYTE: "A gramática nova ainda gera id vírgula id? Toque na resposta."
   PERGUNTA (toque): certo = Sim ; errado = Não
   se errar: "Primeiro, L vai para id e L linha. Depois, vírgula, id e épsilon."
   se acertar: "Exato. A linguagem continua a mesma."

21. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id ⏎ id ⏎ *L  -> id L'* ⏎ L' -> , id L' | ε ⏎ id , id
   BYTE: "Agora a função de L lê um id antes de chamar outra função. A cada chamada a entrada anda, e o parser termina."

22. TELA: # Dois laços ⏎ *E -> E + T | E - T | T* ⏎ E + T ⏎ E - T ⏎ T
   BYTE: "Na prova, a regra costuma ter mais de uma alternativa com laço. Aqui E vai para E mais T, para E menos T, ou para T."

23. TELA: # Dois laços ⏎ *E -> E + T | E - T | T* ⏎ E + T ⏎ E - T ⏎ T
   BYTE: "Passo um: quais alternativas começam com o próprio E? Toque em todas."
   PERGUNTA (toque): certo = E + T | E - T ; errado = T
   se errar: "Olhe o primeiro símbolo de cada uma. Mais de uma pode começar com E."
   se acertar: "As duas primeiras. Então há dois alfas: mais T, e menos T. O T sozinho é o beta."

24. TELA: # Dois laços ⏎ E -> E + T | E - T | T ⏎ E + T ⏎ E - T ⏎ T ⏎ E  -> T E' ⏎ E' -> + T E' | ε ⏎ E' -> + T E' | - T E' | ε ⏎ E' -> + T E' | - T E'
   BYTE: "E vai para T E linha. E o E linha, como fica? Toque na regra certa."
   PERGUNTA (toque): certo = E' -> + T E' | - T E' | ε ; errado = E' -> + T E' | ε | E' -> + T E' | - T E'
   se errar: "Cada alfa vira uma alternativa de E linha. E o épsilon fecha a regra."
   se acertar: "Mais T E linha, menos T E linha, ou épsilon. Sem o menos, a gramática perde as subtrações. Sem o épsilon, ela nunca termina."

25. TELA: # Dois laços ⏎ E -> E + T | E - T | T ⏎ E + T ⏎ E - T ⏎ T ⏎ E  -> T E' ⏎ *E' -> + T E' | - T E' | ε*
   BYTE: "Cada alfa ganhou sua alternativa, e o épsilon aparece uma vez só. Com três alfas, seriam três alternativas mais o épsilon."

26. TELA: # Erros comuns ⏎ Para tirar a recursão de A → A α | β, basta escrever A → α A | β. ⏎ certo ⏎ errado
   BYTE: "Um colega consertou a recursão à esquerda assim. Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Gere uma frase com as duas versões. O beta fica no começo ou no fim?"
   se acertar: "A original gera beta seguido de vários alfas. A do colega gera os alfas antes do beta. A linguagem mudou."

27. TELA: # Erros comuns ⏎ Certo: A → β A' e A' → α A' | ε. O β continua no começo. ⏎ *Errado: Esquecer o ε na regra nova A'.* ⏎ *Certo: Sem A' → ε a repetição nunca termina, e nenhuma frase é gerada.*
   BYTE: "Outro erro: esquecer o épsilon em A linha. É ele que encerra a repetição. Sem ele, a derivação nunca chega só em terminais."

28. TELA: # Resumo ⏎ Recursão à esquerda: A → A α. O parser descendente entra em laço. ⏎ Conserto: A → β A' e A' → α A' | ε. ⏎ A linguagem não muda; a árvore muda. O laço do parser mantém a conta certa.
   BYTE: "Resumindo: recursão à esquerda trava o parser descendente. O conserto põe o beta na frente e empurra a repetição para um não-terminal novo, que termina com épsilon."

#### PEGADINHA: "A recursão escondida" (10 cenas, ~68s)

1. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d
   BYTE: "Nenhuma regra aqui começa com ela mesma. Parece que não tem recursão."

2. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ A ⏎ a
   BYTE: "Siga a primeira opção. S vira A a."

3. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S ⏎ c ⏎ a
   BYTE: "Agora troque o A por S c. Quem reaparece no começo? Toque nele."
   PERGUNTA (toque): certo = S ; errado = c | a
   se errar: "Olhe o primeiro símbolo da frase."
   se acertar: "S chegou em S sem ler nada. É recursão indireta."

4. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ *S -> S c a | d a | b*
   BYTE: "O conserto começa colocando as opções de A dentro de S. Assim a recursão aparece."

5. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S -> S c a | d a | b
   BYTE: "Agora é recursão direta, e a receita de sempre resolve."

6. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S -> S c a | d a | b ⏎ S c a ⏎ d a ⏎ b
   BYTE: "Passo um da receita: separe. Quais alternativas são os betas, as que não começam com S? Toque em todas."
   PERGUNTA (toque): certo = d a | b ; errado = S c a
   se errar: "O beta não começa com o próprio S. Pode haver mais de um."
   se acertar: "Os betas são d a e b. Cada um ganha um S linha no fim."

7. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S  -> d a S' | b S' ⏎ S' -> d a S' | b S' | ε ⏎ S' -> c a S' | ε ⏎ S' -> c a S'
   BYTE: "S vai para d a S linha, ou b S linha. E o S linha, como fica? Toque."
   PERGUNTA (toque): certo = S' -> c a S' | ε ; errado = S' -> d a S' | b S' | ε | S' -> c a S'
   se errar: "O alfa é c a, o que vem depois do S na alternativa com laço. E o épsilon fecha a regra."
   se acertar: "O S linha repete c a, e o épsilon encerra. Os betas ficam no S."

8. TELA: # Depois ⏎ S  -> d a S' | b S' ⏎ S' -> c a S' | ε ⏎ A -> S c | d
   BYTE: "Pronto, sem recursão. E o A? Nenhuma regra usa o A agora, então ele sai da gramática."

9. TELA: # Depois ⏎ S  -> d a S' | b S' ⏎ S' -> c a S' | ε ⏎ abrindo S dentro de A: ⏎ S  -> A a | b ⏎ A  -> b c A' | d A' ⏎ A' -> a c A' | ε
   BYTE: "Dava para fazer o contrário e abrir o S dentro do A. A gramática final é outra, mas a linguagem é a mesma."

10. TELA: # Depois ⏎ S  -> d a S' | b S' ⏎ S' -> c a S' | ε ⏎ *abrindo S dentro de A:* ⏎ S  -> A a | b ⏎ A  -> b c A' | d A' ⏎ A' -> a c A' | ε
   PROF: "Essa é a ordem do livro. Ele fixa uma sequência dos não-terminais para o método funcionar sempre."

#### PRATICA: "Conserte E, T e F" (6 cenas, ~51s)

1. TELA: # E/T/F ⏎ *E -> E + T | T* ⏎ *T -> T * F | F* ⏎ F -> id
   BYTE: "Esta é a gramática de contas que volta no resto do jogo. E e T têm recursão à esquerda, então a receita roda uma vez em cada um."

2. TELA: # E/T/F ⏎ *E -> E + T | T* ⏎ T -> T * F | F ⏎ F -> id ⏎ E -> + T E' ⏎ E -> T E' ⏎ E -> E' T
   BYTE: "Comece pelo E. O alfa é mais T, e o beta é o T sozinho. Como fica a regra nova de E? Toque."
   PERGUNTA (toque): certo = E -> T E' ; errado = E -> + T E' | E -> E' T
   se errar: "A parte sem laço vem primeiro. O E linha vai no fim."
   se acertar: "T E linha. E o E linha fica com mais T E linha, ou épsilon."

3. TELA: # E/T/F ⏎ E -> E + T | T ⏎ *T -> T * F | F* ⏎ F -> id ⏎ E  -> T E' ⏎ E' -> + T E' | ε ⏎ T' -> * F T' ⏎ T' -> + T T' | ε ⏎ T' -> * F T' | ε
   BYTE: "Agora a segunda rodada, no T. Como fica a regra do T linha? Toque."
   PERGUNTA (toque): certo = T' -> * F T' | ε ; errado = T' -> * F T' | T' -> + T T' | ε
   se errar: "O alfa de T é vezes F. E o épsilon fecha a regra."
   se acertar: "Vezes F T linha, ou épsilon. Sem o épsilon, a derivação nunca terminaria."

4. TELA: # Depois ⏎ E  -> T E' ⏎ *E' -> + T E' | ε* ⏎ T  -> F T' ⏎ *T' -> * F T' | ε* ⏎ F  -> id
   BYTE: "A gramática inteira. O F não tem laço e fica igual. Nenhuma regra começa com o próprio lado esquerdo."

5. TELA: chegue nesta frase ⏎ id ⏎ + ⏎ id ⏎ * ⏎ id ⏎ *E*
   BYTE: "Sua vez. Derive id mais id vezes id na gramática nova. A cada passo, troque o não-terminal mais à esquerda."
   BRINQUEDO: dica "Olhe a frase lá em cima. Se o próximo símbolo dela não cabe, o linha vira épsilon." / acerto "Chegou. A gramática nova gera a mesma frase, e a derivação termina."

6. TELA: # Depois ⏎ E  -> T E' ⏎ *E' -> + T E' | ε* ⏎ T  -> F T' ⏎ *T' -> * F T' | ε* ⏎ F  -> id
   PROF: "Duas regras com laço, duas rodadas da receita. Na prova, essa gramática cai desse jeito."

#### CLIPE 30s: "Por que a recursão trava" (2 cenas, ~15s)

1. TELA: # E -> E + T ⏎ *E* ⏎ + ⏎ T
   BYTE: "Na regra E vai para E mais T, o parser troca E por E. Ele não lê nada."

2. TELA: # E -> E + T ⏎ *E* ⏎ + ⏎ T ⏎ + ⏎ T
   BYTE: "Ele repete isso para sempre. Por isso o parser de cima para baixo, o do Templo, não aceita."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Laço Eterno — mentira: "Recursão à esquerda só deixa o parser lento." / verdade: "Ela trava o parser descendente: ele chama a mesma regra sem ler nada."
    · [V] O parser de baixo para cima, que você vê na Fortaleza, aceita recursão à esquerda. → Isso mesmo.
    · [V] E -> E + T | T vira E -> T X e X -> + T X | ε. → Isso mesmo.
    · [F] Recursão à direita trava o parser de cima para baixo. → À direita, ele lê um token antes de voltar.

#### FICHA DO ASSUNTO
Em uma frase: A → Aα | β faz o analisador descendente entrar em laço infinito; troque por A → βA' e A' → αA' | ε, que gera a mesma linguagem βα*.
Gancho: Beta começa, alfa repete, épsilon encerra.
Termos-chave: recursão à esquerda, descendente, laço infinito, novo não-terminal, mesma linguagem
Erros comuns: Esquecer o | ε em A'. // Trocar α e β na receita. // Esquecer de colocar A' no fim de cada β. // Ignorar a recursão indireta (S → A a, A → S b). // Achar que os analisadores de baixo para cima (LR, da Fortaleza) também precisam eliminar recursão à esquerda.
Flashcards:
    · P: Quando G é recursiva à esquerda? → R: Quando existe A com A ⇒⁺ Aα
    · P: A → Aα | β vira o quê? → R: A → βA'; A' → αA' | ε
    · P: Linguagem de A → Aα | β? → R: βα*
    · P: Quem sofre com recursão à esquerda? → R: Analisadores descendentes (LL, descida recursiva)
    · P: E → E + T | T sem recursão à esquerda? → R: E → T E'; E' → + T E' | ε
    · P: Pré-condições do algoritmo geral? → R: Sem ciclos e sem produções-ε
    · P: Exemplo de recursão indireta? → R: S → A a | b; A → S c | d
    · P: O parser de baixo para cima (LR, da Fortaleza) precisa eliminar recursão à esquerda? → R: Não, ele lida bem com ela

#### GLOSSÁRIO (balão ao tocar no termo)
- recursão à esquerda: Regra cujo lado direito começa com o próprio símbolo da esquerda, como E vai para E mais T.


## ASSUNTO LEFT_FACTORING: Fatoração à esquerda
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Pôr em evidência» → Prática: você pilota «Torne LL(1)» → Pegadinha «Quando fatorar não basta» → Desafio
Jogos que treinam: Cirurgia de Gramática (montar a gramática consertada com peças), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug grudou dois começos
Na tela: A -> a b | a c
BYTE: "Antes da aula: olhando só o a, dá para escolher a opção?"
Opções: sim | ✔não | às vezes
Depois: "Não dá. As duas começam igual."

#### AULA ANIMADA BÁSICA: "Pôr em evidência" (25 cenas, ~180s) [1º exemplo começa na cena 3]

1. TELA: *C -> id = n | id ( )* ⏎ C ⏎ id ⏎ C -> id = n ⏎ C -> id ( )
   BYTE: "O parser descendente do Templo escolhe a regra olhando só o próximo token. Um comando pode ser atribuição ou chamada, e os dois começam com id."

2. TELA: *C -> id = n | id ( )* ⏎ C ⏎ id ⏎ C -> id = n ⏎ C -> id ( ) ⏎ não dá para saber
   BYTE: "Vendo só o id, qual regra o parser escolhe? Toque."
   PERGUNTA (toque): certo = não dá para saber ; errado = C -> id = n | C -> id ( )
   se errar: "Olhe o primeiro token de cada regra."
   se acertar: "Um token só não decide entre as duas."

3. TELA: # Qual das duas? ⏎ a ⏎ b ⏎ a ⏎ c
   BYTE: "Estas duas alternativas começam igual. Lendo o a, um parser preditivo não sabe qual das duas escolher."

4. TELA: # Qual das duas? ⏎ a ⏎ b ⏎ a ⏎ c
   BYTE: "Toque na parte que as duas regras têm em comum."
   PERGUNTA (toque): certo = a ; errado = b | c
   se errar: "Compare o começo das duas linhas."
   se acertar: "Boa. O a é o prefixo comum das duas."

5. TELA: # Qual das duas? ⏎ a ⏎ b ⏎ c
   PROF: "Faço como na matemática: ponho o prefixo comum em evidência. Isso é a fatoração à esquerda."

6. TELA: # Fatorada ⏎ A  -> a b | a c ⏎ A  -> a A' ⏎ *A' -> b | c*
   BYTE: "A escolha fica para depois do a, num não-terminal novo. Aí basta olhar o próximo token."

7. TELA: # Fatorada ⏎ A  -> a b | a c ⏎ A  -> a A' ⏎ *A' -> b | c*
   BYTE: "Com um token só de previsão, o parser sempre sabe o que fazer. No Templo, gramática assim se chama LL(1)."

8. TELA: # Receita ⏎ 1. Ache o maior prefixo comum α. ⏎ 2. Troque as regras por A → α A'. ⏎ 3. Em A', ponha o resto de cada regra. ⏎ 4. Repita enquanto duas regras começarem igual.
   BYTE: "A fatoração tem quatro passos. Eles ficam aqui na tela."

9. TELA: # Receita ⏎ 1. Ache o maior prefixo comum α. ⏎ 2. Troque as regras por A → α A'. ⏎ 3. Em A', ponha o resto de cada regra. ⏎ 4. Repita enquanto duas regras começarem igual.
   BYTE: "Qual passo guarda o que sobrou de cada regra? Toque nele."
   PERGUNTA (toque): certo = 3. Em A', ponha o resto de cada regra. ; errado = 1. Ache o maior prefixo comum α. | 2. Troque as regras por A → α A'. | 4. Repita enquanto duas regras começarem igual.
   se errar: "O resto vem depois do começo comum."
   se acertar: "O resto vira as alternativas da regra nova."

10. TELA: # Comandos ⏎ *C -> id = n | id ( ) | id ( n )*
   BYTE: "Três comandos começam com id: uma atribuição e duas chamadas."

11. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ id ⏎ id = ⏎ id (
   BYTE: "Passo um: qual é o maior começo comum das três? Toque."
   PERGUNTA (toque): certo = id ; errado = id = | id (
   se errar: "Compare o segundo símbolo das três. Ele já muda."
   se acertar: "As três regras começam com id."

12. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ *C  -> id C'* ⏎ C' -> = n | ( ) | ( n )
   BYTE: "Ponho o id em evidência. O resto vai para a regra nova."

13. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C  -> id C' ⏎ *C' -> = n | ( ) | ( n )* ⏎ = n ⏎ ( ) ⏎ ( n )
   BYTE: "Na regra nova, duas alternativas ainda começam igual. Toque nas duas."
   PERGUNTA (toque): certo = ( ) | ( n ) ; errado = = n
   se errar: "Procure duas com o mesmo primeiro símbolo."
   se acertar: "Acertou. As duas começam com abre parêntese."

14. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C  -> id C' ⏎ *C' -> = n | ( ) | ( n )* ⏎ C' -> = n | ( C'' ⏎ C' -> = n | ( ) C'' ⏎ C' -> ( C''
   BYTE: "Passo quatro: fatore de novo. Como fica a regra nova? Toque."
   PERGUNTA (toque): certo = C' -> = n | ( C'' ; errado = C' -> = n | ( ) C'' | C' -> ( C''
   se errar: "O abre parêntese vai para a frente. O resto desce para outra regra."
   se acertar: "O que vem depois do parêntese ganha uma regra só dele."

15. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ *C'  -> = n | ( C''* ⏎ *C'' -> ) | n )*
   BYTE: "Pronto. Nenhuma regra tem duas alternativas com o mesmo começo."

16. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ *C'  -> = n | ( C''* ⏎ C'' -> ) | n ) ⏎ 1 ⏎ 2
   BYTE: "O parser está na segunda regra e lê abre parêntese. Quantas alternativas servem?"
   PERGUNTA (toque): certo = 1 ; errado = 2
   se errar: "Conte as alternativas que começam com abre parêntese."
   se acertar: "Uma só. A gramática fatorada é LL(1)."

17. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ C'  -> = n | ( C'' ⏎ C'' -> ) | n )
   BYTE: "Fatorar não muda a linguagem. Só adia a escolha até chegar o token que decide."

18. TELA: # If e else ⏎ *S -> if S | if S else S | x*
   BYTE: "Agora um caso que cai em prova: o if com else e sem else. Uma regra inteira é o começo da outra."

19. TELA: # If e else ⏎ S -> if S | if S else S | x ⏎ if ⏎ if S ⏎ if S else
   BYTE: "Qual é o maior começo comum das duas regras com if? Toque."
   PERGUNTA (toque): certo = if S ; errado = if | if S else
   se errar: "Compare símbolo por símbolo. Onde a primeira regra acaba?"
   se acertar: "O if S. A primeira regra acaba ali, e a segunda continua com o else."

20. TELA: # If e else ⏎ S -> if S | if S else S | x ⏎ if S ⏎ else S ⏎ ε ⏎ S ⏎ x
   BYTE: "Depois do if S, o que sobra de cada regra? Toque nas duas sobras."
   PERGUNTA (toque): certo = else S | ε ; errado = S | x
   se errar: "A segunda regra deixa o else S. A primeira não deixa nada. Como se escreve nada?"
   se acertar: "O else S, e o épsilon. Quando o resto é vazio, ele vira épsilon na regra nova."

21. TELA: # If e else ⏎ S -> if S | if S else S | x ⏎ S  -> if S S' | x ⏎ *S' -> else S | ε*
   BYTE: "Pronto. Sem o épsilon em S linha, o if sem else sumiria da linguagem."

22. TELA: # If e else ⏎ S -> if S | if S else S | x ⏎ S  -> if S S' | x ⏎ *S' -> else S | ε*
   PROF: "Cuidado: ainda não é LL(1). Com else na entrada, S linha tem duas regras. Fatorar não tira a ambiguidade do else solto."

23. TELA: # Erros comuns ⏎ Fatorar A → a b | a c não muda as frases que a gramática gera. ⏎ certo ⏎ errado
   BYTE: "Fatorar à esquerda muda a linguagem? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = certo ; errado = errado
   se errar: "Derive a b e a c com a gramática fatorada. Sai alguma frase nova?"
   se acertar: "É isso. A → a A' e A' → b | c geram a b e a c, as mesmas frases. A fatoração só muda o momento da escolha."

24. TELA: # Erros comuns ⏎ Certo: Fatorar A → a b | a c não muda as frases que a gramática gera. ⏎ *Errado: Pôr em evidência só uma parte do prefixo comum.* ⏎ *Certo: Use o maior prefixo comum, e repita se sobrar começo igual.*
   BYTE: "Um erro comum: fatorar pela metade. Se as alternativas novas ainda começam igual, o conflito continua. Repita até não sobrar prefixo comum."

25. TELA: # Resumo ⏎ Prefixo comum: duas alternativas do mesmo não-terminal começam igual. ⏎ Fatorar: A → α A', e A' recebe o resto de cada alternativa. ⏎ Não muda a linguagem. É um dos preparos para a gramática ser LL(1).
   BYTE: "Resumindo: prefixo comum impede a escolha com um token. A fatoração põe o prefixo em evidência e adia a decisão, sem mudar a linguagem."

#### PEGADINHA: "Quando fatorar não basta" (5 cenas, ~31s)

1. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c
   BYTE: "Aqui as opções de A começam diferente: uma com a, outra com B."

2. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ *B -> a c*
   BYTE: "Mas B também começa com a. O começo igual está escondido uma regra abaixo."

3. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ uma ⏎ duas
   BYTE: "Com a na entrada, quantas opções de A valem? Toque no número."
   PERGUNTA (toque): certo = duas ; errado = uma
   se errar: "A primeira opção começa com a. E o B começa com o quê?"
   se acertar: "Duas opções: é conflito, mesmo sem prefixo à vista."

4. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ *A -> a b | a c* ⏎ A -> a X ⏎ X -> b | c
   BYTE: "Primeiro coloque B dentro de A. Depois fatore."

5. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ A -> a b | a c ⏎ *A -> a X* ⏎ *X -> b | c*
   PROF: "Fatorar só enxerga o que está escrito. Às vezes é preciso abrir a regra antes."

#### PRATICA: "Torne LL(1)" (7 cenas, ~59s)

1. TELA: # Torne LL(1) ⏎ L -> L ; C | C ⏎ C -> id = E | id ( ) ⏎ E -> n
   BYTE: "Esta gramática tem os dois defeitos da Ponte. O parser do Templo engasga nela. Vamos consertar na ordem do livro."

2. TELA: # Torne LL(1) ⏎ L -> L ; C | C ⏎ C -> id = E | id ( ) ⏎ E -> n
   BYTE: "Primeiro a recursão. Qual linha começa com o próprio não-terminal? Toque."
   PERGUNTA (toque): certo = L -> L ; C | C ; errado = C -> id = E | id ( ) | E -> n
   se errar: "Procure a regra em que o lado direito começa com o mesmo nome do lado esquerdo."
   se acertar: "A do L. L vai para L ponto e vírgula C começa com o próprio L."

3. TELA: # Torne LL(1) ⏎ L -> C L' ⏎ L' -> ; C L' | ε ⏎ C -> id = E | id ( ) ⏎ E -> n ⏎ 1. sem recursão à esquerda
   BYTE: "O L agora vai para C e L linha. O L linha repete o ponto e vírgula C, ou some. As frases são as mesmas."

4. TELA: # Torne LL(1) ⏎ L -> C L' ⏎ L' -> ; C L' | ε ⏎ C -> id = E | id ( ) ⏎ E -> n
   BYTE: "Agora o prefixo comum. Qual linha tem duas regras com o mesmo começo? Toque."
   PERGUNTA (toque): certo = C -> id = E | id ( ) ; errado = L -> C L' | L' -> ; C L' | ε | E -> n
   se errar: "Olhe o primeiro símbolo de cada regra, linha por linha."
   se acertar: "A do C. As duas regras começam com id."

5. TELA: # Torne LL(1) ⏎ L -> C L' ⏎ L' -> ; C L' | ε ⏎ C -> id C' ⏎ C' -> = E | ( ) ⏎ E -> n ⏎ 2. fatorada
   BYTE: "Fatorado: C vai para id e C linha. O C linha recebe o resto de cada regra: o igual com E, ou os parênteses."

6. TELA: # Torne LL(1) ⏎ L -> C L' ⏎ L' -> ; C L' | ε ⏎ C -> id C' ⏎ C' -> = E | ( ) ⏎ E -> n ⏎ nenhuma ⏎ uma ⏎ duas
   BYTE: "Quantas casas da tabela LL(1) desta gramática ficam com duas regras? Toque."
   PERGUNTA (toque): certo = nenhuma ; errado = uma | duas
   se errar: "Sem recursão e sem começo repetido, o parser sempre escolhe pelo próximo token."
   se acertar: "Nenhuma. A gramática agora é LL(1)."

7. TELA: # Torne LL(1) ⏎ L -> C L' ⏎ L' -> ; C L' | ε ⏎ C -> id C' ⏎ C' -> = E | ( ) ⏎ E -> n
   BYTE: "Primeiro tirar a recursão, depois fatorar. Na prova, mostre as duas gramáticas e diga qual defeito cada passo resolveu."

#### CLIPE 30s: "Fatorar é adiar a escolha" (2 cenas, ~11s)

1. TELA: # A -> a b | a c ⏎ a ⏎ ?
   BYTE: "Quando duas opções começam igual, o parser não sabe qual pegar."

2. TELA: # A -> a X ⏎ a ⏎ X
   BYTE: "Fatorar lê o começo igual uma vez e decide depois."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Começo Igual — mentira: "Fatorar muda a linguagem." / verdade: "A linguagem é a mesma. Só adia a escolha."
    · [V] A -> a b | a c vira A -> a X e X -> b | c. → Isso mesmo.
    · [V] Fatorar acrescenta um nível na árvore. → Isso mesmo.
    · [F] Fatorar resolve qualquer conflito LL(1). → Fatorar só tira o começo igual. O else solto, por exemplo, continua com duas regras possíveis.

#### FICHA DO ASSUNTO
Em uma frase: Quando duas alternativas começam com o mesmo prefixo α, ponha α em evidência: A → αA' e A' → β₁ | β₂, adiando a decisão do analisador preditivo.
Gancho: ab + ac = a(b + c): coloque o prefixo comum em evidência.
Termos-chave: prefixo comum, fatoração, novo não-terminal, adiar, preditivo
Erros comuns: Achar que a fatoração elimina ambiguidade. // Parar na primeira rodada sem fatorar o novo não-terminal. // Usar um prefixo que não é comum a todas as alternativas do grupo. // Esquecer o ε quando uma alternativa é exatamente o prefixo.
Flashcards:
    · P: A → αβ₁ | αβ₂ fatorada? → R: A → αA'; A' → β₁ | β₂
    · P: Para que serve a fatoração à esquerda? → R: Adiar a escolha da produção até ver entrada suficiente
    · P: Qual prefixo usar? → R: O maior prefixo comum a todas as alternativas do grupo (as que começam com o mesmo símbolo)
    · P: Fatore A → x y z | x y → R: A → x y A'; A' → z | ε
    · P: Else pendurado fatorado? → R: S → i E t S S' | a; S' → e S | ε
    · P: Fatorar resolve ambiguidade? → R: Não
    · P: Fatorada e sem recursão à esquerda ⇒ LL(1)? → R: Não necessariamente

#### GLOSSÁRIO (balão ao tocar no termo)
- fatoração: Pôr em evidência o começo comum de duas regras para adiar a escolha.
