# Região 4: Ponte da Transformação — Consertar gramáticas
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO LEFT_RECURSION: Recursão à esquerda
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O laço sem fim» → Montar na Fábrica → Prática: você pilota «Rode a versão consertada» → Pegadinha «A recursão escondida» → Desafio
Jogos que treinam: Cirurgia de Gramática (montar a gramática consertada com peças), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug prendeu o parser
Na tela: E -> E + T | T
BYTE: "Antes da aula: o que o parser descendente faz aqui?"
Opções: lê o + | ✔gira sem ler | aceita
Depois: "Gira sem ler nada. Ele troca E por E para sempre."

#### AULA ANIMADA BÁSICA: "O laço sem fim" (20 cenas, ~150s) [1º exemplo começa na cena 3]

1. TELA: # E -> E + n | n ⏎ void E(void) { ⏎     E(); ⏎     casa('+'); ⏎     casa(NUM); ⏎ }
   BYTE: "Num parser descendente, cada não-terminal vira uma função. A função segue o lado direito da regra, da esquerda para a direita."

2. TELA: # E -> E + n | n ⏎ void E(void) { ⏎     E(); ⏎     casa('+'); ⏎     casa(NUM); ⏎ }
   BYTE: "Antes de ler qualquer token, o que a função E faz? Toque na linha."
   PERGUNTA (toque): certo =     E(); ; errado =     casa('+'); |     casa(NUM);
   se errar: "Olhe a primeira linha dentro da função."
   se acertar: "Isso. Ela chama a si mesma, sem ler nada."

3. TELA: # E -> E + n | n ⏎ E
   BYTE: "Olhe esta regra. O lado direito de E começa com o próprio E."

4. TELA: # E -> E + n | n ⏎ E ⏎ E ⏎ E ⏎ ...
   BYTE: "Um parser de cima para baixo procura E. Para isso, procura E. Para isso, procura E."

5. TELA: !!RECURSÃO À ESQUERDA!!
   BYTE: "Regra em que o lado direito começa com o próprio lado esquerdo tem recursão à esquerda. Nenhum parser descendente aceita isso."

6. TELA: # Qual trava? ⏎ A -> A b ⏎ A -> b A ⏎ A -> c
   BYTE: "Qual destas regras tem recursão à esquerda? Toque nela."
   PERGUNTA (toque): certo = A -> A b ; errado = A -> b A | A -> c
   se errar: "Procure a regra cujo lado direito começa com o próprio lado esquerdo."
   se acertar: "Isso. A começa com A, então o parser não sai do lugar."

7. TELA: # Antes ⏎ E -> E + n | n ⏎ Depois ⏎ E  -> n E' ⏎ E' -> + n E' | ε
   BYTE: "O conserto: a alternativa sem recursão vem primeiro. O que se repetia vai para um não-terminal novo, com recursão à direita."

8. TELA: # Antes ⏎ E -> E + n | n ⏎ Depois ⏎ E  -> n E' ⏎ *E' -> + n E' | ε*
   BYTE: "A linguagem é a mesma: um n, depois zero ou mais vezes mais n. Só mudou o lado da recursão."

9. TELA: # Receita ⏎ 1. Separe: A → A α tem laço, A → β não tem. ⏎ 2. Troque cada A → β por A → β A'. ⏎ 3. Crie A' → α A' | ε. ⏎ A -> A α | β
   BYTE: "O conserto tem três passos. Ele vale para qualquer regra desse tipo."

10. TELA: # Receita ⏎ *1. Separe: A → A α tem laço, A → β não tem.* ⏎ 2. Troque cada A → β por A → β A'. ⏎ 3. Crie A' → α A' | ε. ⏎ E -> E + n | n ⏎ + n n
   BYTE: "Aqui, qual alternativa faz o papel do beta, a que não tem laço?"
   PERGUNTA (toque): certo = n ; errado = + n
   se errar: "O beta não começa com E."
   se acertar: "Isso. O n é o beta, e o mais n é o alfa."

11. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id
   BYTE: "Uma lista de ids separados por vírgula. Agora quem conserta é você."

12. TELA: # Lista de ids ⏎ *L -> L , id | id* ⏎ L , id id
   BYTE: "Passo um: qual alternativa começa com o próprio L? Toque nela."
   PERGUNTA (toque): certo = L , id ; errado = id
   se errar: "Procure a alternativa cujo primeiro símbolo é o L."
   se acertar: "Isso. Ela tem o laço. A vírgula e o id formam o alfa."

13. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id ⏎ L -> id L' L -> L' id L -> , id L'
   BYTE: "Passo dois: como fica a regra nova de L? Toque nela."
   PERGUNTA (toque): certo = L -> id L' ; errado = L -> L' id | L -> , id L'
   se errar: "A parte sem laço vem primeiro. O L linha vai no fim."
   se acertar: "Isso. Primeiro o id, depois a regra nova."

14. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id ⏎ L  -> id L' ⏎ , id L' ε L , id id L'
   BYTE: "Passo três: o que entra em L linha? Toque nas duas alternativas."
   PERGUNTA (toque): certo = , id L' | ε ; errado = L , id | id L'
   se errar: "O alfa seguido de L linha, e o épsilon para parar."
   se acertar: "Isso. Vírgula e id repetem, e o épsilon encerra."

15. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id ⏎ *L  -> id L'* ⏎ *L' -> , id L' | ε*
   BYTE: "Pronto. Nenhuma regra começa com o próprio lado esquerdo."

16. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id ⏎ L  -> id L' ⏎ L' -> , id L' | ε ⏎ id , id ⏎ Sim Não
   BYTE: "A gramática nova ainda gera id vírgula id? Toque na resposta."
   PERGUNTA (toque): certo = Sim ; errado = Não
   se errar: "Primeiro, L vai para id e L linha. Depois, vírgula, id e épsilon."
   se acertar: "Isso. A linguagem continua a mesma."

17. TELA: # Lista de ids ⏎ L -> L , id | id ⏎ L , id id ⏎ *L  -> id L'* ⏎ L' -> , id L' | ε ⏎ id , id
   BYTE: "Agora a função de L lê um id antes de chamar outra função. A cada chamada a entrada anda, e o parser termina."

18. TELA: # Erros comuns ⏎ Para tirar a recursão de A → A α | β, basta escrever A → α A | β. ⏎ certo errado
   BYTE: "Um colega consertou a recursão à esquerda assim. Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Gere uma frase com as duas versões. O beta fica no começo ou no fim?"
   se acertar: "Isso. A original gera beta seguido de vários alfas. A do colega gera os alfas antes do beta. A linguagem mudou."

19. TELA: # Erros comuns ⏎ Certo: A → β A' e A' → α A' | ε. O β continua no começo. ⏎ *Errado: Esquecer o ε na regra nova A'.* ⏎ *Certo: Sem A' → ε a repetição nunca termina, e nenhuma frase é gerada.*
   BYTE: "Outro erro: esquecer o épsilon em A linha. É ele que encerra a repetição. Sem ele, a derivação nunca chega só em terminais."

20. TELA: # Resumo ⏎ Recursão à esquerda: A → A α. O parser descendente entra em laço. ⏎ Conserto: A → β A' e A' → α A' | ε. ⏎ A linguagem não muda; só a recursão passa para a direita.
   BYTE: "Resumindo: recursão à esquerda trava o parser descendente. O conserto põe o beta na frente e empurra a repetição para um não-terminal novo, que termina com épsilon."

#### PRATICA: "Rode a versão consertada" (5 cenas, ~28s)

1. TELA: # n + n ⏎ E -> n X ⏎ X -> + n X | ε
   BYTE: "Esta gramática já está consertada. Sem recursão à esquerda, a pilha anda."

2. TELA: # n + n ⏎ *E -> n X* ⏎ X -> + n X | ε
   BYTE: "Topo não terminal: troca pela regra. Topo igual à entrada: casa."

3. TELA: # n + n ⏎ E -> n X ⏎ *X -> + n X | ε*
   PROF: "Repare no X à direita. A recursão foi para o fim, onde não trava."

4. TELA: *n* + n $ ⏎ E ⏎ $ ⏎ pilha
   BYTE: "Opere a pilha até aceitar."
   BRINQUEDO interativo: meta = "Leve a pilha até o fim" ; teclas = [Trocar pela regra, Casar]
   se errar: "Olhe o topo. Ele é terminal ou não terminal?"
   ao conseguir: "Aceitou. Com a recursão à direita, o parser sempre lê um token antes de voltar."

5. TELA: # n + n ⏎ E -> n X ⏎ *X -> + n X | ε*
   BYTE: "A linguagem é a mesma de antes. Só a forma da regra mudou."

#### PEGADINHA: "A recursão escondida" (5 cenas, ~28s)

1. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d
   BYTE: "Nenhuma regra aqui começa com ela mesma. Parece que não tem recursão."

2. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ A a
   BYTE: "Siga a primeira opção. S vira A a."

3. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S c a
   BYTE: "Agora troque o A por S c. Quem reaparece no começo? Toque nele."
   PERGUNTA (toque): certo = S ; errado = c | a
   se errar: "Olhe o primeiro símbolo da frase."
   se acertar: "Isso. S chegou em S sem ler nada. É recursão indireta."

4. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ *S -> S c a | d a | b*
   BYTE: "O conserto começa colocando as opções de A dentro de S. Assim a recursão aparece."

5. TELA: # Sem recursão? ⏎ S -> A a | b ⏎ A -> S c | d ⏎ S -> S c a | d a | b
   PROF: "Agora é recursão direta, e a receita de sempre resolve."

#### CLIPE 30s: "Por que a recursão trava" (2 cenas, ~13s)

1. TELA: # E -> E + T ⏎ *E* + T
   BYTE: "Na regra E vai para E mais T, o parser troca E por E. Ele não lê nada."

2. TELA: # E -> E + T ⏎ *E* + T + T
   BYTE: "Ele repete isso para sempre. Por isso o LL(1) não aceita."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Laço Eterno — mentira: "Recursão à esquerda só deixa o parser lento." / verdade: "Ela trava o parser descendente: ele chama a mesma regra sem ler nada."
    · [V] Parser LR aceita recursão à esquerda. → Isso mesmo.
    · [V] E -> E + T | T vira E -> T X e X -> + T X | ε. → Isso mesmo.
    · [F] Recursão à direita trava o LL(1). → À direita, ele lê um token antes de voltar.

#### FICHA DO ASSUNTO
Em uma frase: A → Aα | β faz o analisador descendente entrar em laço infinito; troque por A → βA' e A' → αA' | ε, que gera a mesma linguagem βα*.
Gancho: Beta começa, alfa repete, épsilon encerra.
Termos-chave: recursão à esquerda, descendente, laço infinito, novo não-terminal, mesma linguagem
Erros comuns: Esquecer o | ε em A'. // Trocar α e β na receita. // Esquecer de colocar A' no fim de cada β. // Ignorar a recursão indireta (S → A a, A → S b). // Achar que analisadores LR também precisam eliminar recursão à esquerda.
Flashcards:
    · P: Quando G é recursiva à esquerda? → R: Quando existe A com A ⇒⁺ Aα
    · P: A → Aα | β vira o quê? → R: A → βA'; A' → αA' | ε
    · P: Linguagem de A → Aα | β? → R: βα*
    · P: Quem sofre com recursão à esquerda? → R: Analisadores descendentes (LL, descida recursiva)
    · P: E → E + T | T sem recursão à esquerda? → R: E → T E'; E' → + T E' | ε
    · P: Pré-condições do algoritmo geral? → R: Sem ciclos e sem produções-ε
    · P: Exemplo de recursão indireta? → R: S → A a | b; A → S c | d
    · P: LR precisa eliminar recursão à esquerda? → R: Não, LR lida bem com ela

#### GLOSSÁRIO (balão ao tocar no termo)
- recursão à esquerda: Regra em que o símbolo começa com ele mesmo, como E vai para E mais T.


## ASSUNTO LEFT_FACTORING: Fatoração à esquerda
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Pôr em evidência» → Pegadinha «Quando fatorar não basta» → Desafio
Jogos que treinam: Cirurgia de Gramática (montar a gramática consertada com peças), Sudoku LL(1) (encaixar produções na tabela)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug grudou dois começos
Na tela: A -> a b | a c
BYTE: "Antes da aula: olhando só o a, dá para escolher a opção?"
Opções: sim | ✔não | às vezes
Depois: "Não dá. As duas começam igual."

#### AULA ANIMADA BÁSICA: "Pôr em evidência" (20 cenas, ~137s) [1º exemplo começa na cena 3]

1. TELA: *C -> id = n | id ( )* ⏎ C -> id = n ⏎ C id ⏎ C -> id ( )
   BYTE: "Um comando pode ser atribuição ou chamada. Os dois começam com id."

2. TELA: *C -> id = n | id ( )* ⏎ C -> id = n ⏎ C id ⏎ C -> id ( ) ⏎ não dá para saber
   BYTE: "Vendo só o id, qual regra o parser escolhe? Toque."
   PERGUNTA (toque): certo = não dá para saber ; errado = C -> id = n | C -> id ( )
   se errar: "Olhe o primeiro token de cada regra."
   se acertar: "Isso. Um token só não decide entre as duas."

3. TELA: # Qual das duas? ⏎ a b ⏎ a c
   BYTE: "Estas duas alternativas começam igual. Lendo o a, um parser preditivo não sabe qual das duas escolher."

4. TELA: # Qual das duas? ⏎ a b ⏎ a c
   BYTE: "Toque na parte que as duas regras têm em comum."
   PERGUNTA (toque): certo = a ; errado = b | c
   se errar: "Compare o começo das duas linhas."
   se acertar: "Isso. O a é o prefixo comum das duas."

5. TELA: # Qual das duas? ⏎ b ⏎ a ⏎ c
   PROF: "Faço como na matemática: ponho o prefixo comum em evidência. Isso é a fatoração à esquerda."

6. TELA: # Fatorada ⏎ A  -> a b | a c ⏎ A  -> a A' ⏎ *A' -> b | c*
   BYTE: "A escolha fica para depois do a, num não-terminal novo. Aí basta olhar o próximo token."

7. TELA: # Fatorada ⏎ A  -> a b | a c ⏎ A  -> a A' ⏎ *A' -> b | c*
   BYTE: "Com um token só de previsão, o parser sempre sabe o que fazer. É o que uma gramática LL(1) exige."

8. TELA: # Receita ⏎ 1. Ache o maior prefixo comum α. ⏎ 2. Troque as regras por A → α A'. ⏎ 3. Em A', ponha o resto de cada regra. ⏎ 4. Repita enquanto duas regras começarem igual.
   BYTE: "A fatoração tem quatro passos. Eles ficam aqui na tela."

9. TELA: # Receita ⏎ 1. Ache o maior prefixo comum α. ⏎ 2. Troque as regras por A → α A'. ⏎ 3. Em A', ponha o resto de cada regra. ⏎ 4. Repita enquanto duas regras começarem igual.
   BYTE: "Qual passo guarda o que sobrou de cada regra? Toque nele."
   PERGUNTA (toque): certo = 3. Em A', ponha o resto de cada regra. ; errado = 1. Ache o maior prefixo comum α. | 2. Troque as regras por A → α A'. | 4. Repita enquanto duas regras começarem igual.
   se errar: "O resto vem depois do começo comum."
   se acertar: "Isso. O resto vira as alternativas da regra nova."

10. TELA: # Comandos ⏎ *C -> id = n | id ( ) | id ( n )*
   BYTE: "Três comandos começam com id: uma atribuição e duas chamadas."

11. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ id id = id (
   BYTE: "Passo um: qual é o maior começo comum das três? Toque."
   PERGUNTA (toque): certo = id ; errado = id = | id (
   se errar: "Compare o segundo símbolo das três. Ele já muda."
   se acertar: "Isso. As três regras começam com id."

12. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ *C  -> id C'* ⏎ C' -> = n | ( ) | ( n )
   BYTE: "Ponho o id em evidência. O resto vai para a regra nova."

13. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C  -> id C' ⏎ *C' -> = n | ( ) | ( n )* ⏎ = n ( ) ( n )
   BYTE: "Na regra nova, duas alternativas ainda começam igual. Toque nas duas."
   PERGUNTA (toque): certo = ( ) | ( n ) ; errado = = n
   se errar: "Procure duas com o mesmo primeiro símbolo."
   se acertar: "Isso. As duas começam com abre parêntese."

14. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C  -> id C' ⏎ *C' -> = n | ( ) | ( n )* ⏎ C' -> = n | ( C'' ⏎ C' -> = n | ( ) C'' ⏎ C' -> ( C''
   BYTE: "Passo quatro: fatore de novo. Como fica a regra nova? Toque."
   PERGUNTA (toque): certo = C' -> = n | ( C'' ; errado = C' -> = n | ( ) C'' | C' -> ( C''
   se errar: "O abre parêntese vai para a frente. O resto desce para outra regra."
   se acertar: "Isso. O que vem depois do parêntese ganha uma regra só dele."

15. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ *C'  -> = n | ( C''* ⏎ *C'' -> ) | n )*
   BYTE: "Pronto. Nenhuma regra tem duas alternativas com o mesmo começo."

16. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ *C'  -> = n | ( C''* ⏎ C'' -> ) | n ) ⏎ 1 2
   BYTE: "O parser está na segunda regra e lê abre parêntese. Quantas alternativas servem?"
   PERGUNTA (toque): certo = 1 ; errado = 2
   se errar: "Conte as alternativas que começam com abre parêntese."
   se acertar: "Isso. Uma só. A gramática fatorada é LL(1)."

17. TELA: # Comandos ⏎ C -> id = n | id ( ) | id ( n ) ⏎ C   -> id C' ⏎ C'  -> = n | ( C'' ⏎ C'' -> ) | n )
   BYTE: "Fatorar não muda a linguagem. Só adia a escolha até chegar o token que decide."

18. TELA: # Erros comuns ⏎ A → a b | a c: fatorar muda as frases que a gramática gera. ⏎ certo errado
   BYTE: "Fatorar à esquerda muda a linguagem? Um colega disse que sim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Derive a b e a c com a gramática fatorada."
   se acertar: "Isso. As frases são as mesmas. A fatoração só muda o momento da escolha, não o que é gerado."

19. TELA: # Erros comuns ⏎ Certo: A → a A' e A' → b | c geram as mesmas frases: a b e a c. ⏎ *Errado: Pôr em evidência só uma parte do prefixo comum.* ⏎ *Certo: Use o maior prefixo comum, e repita se sobrar começo igual.*
   BYTE: "Outro erro: fatorar pela metade. Se as alternativas novas ainda começam igual, o conflito continua. Repita até não sobrar prefixo comum."

20. TELA: # Resumo ⏎ Prefixo comum: duas alternativas do mesmo não-terminal começam igual. ⏎ Fatorar: A → α A', e A' recebe o resto de cada alternativa. ⏎ Não muda a linguagem. É um dos preparos para a gramática ser LL(1).
   BYTE: "Resumindo: prefixo comum impede a escolha com um token. A fatoração põe o prefixo em evidência e adia a decisão, sem mudar a linguagem."

#### PEGADINHA: "Quando fatorar não basta" (5 cenas, ~31s)

1. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c
   BYTE: "Aqui as opções de A começam diferente: uma com a, outra com B."

2. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ *B -> a c*
   BYTE: "Mas B também começa com a. O começo igual está escondido uma regra abaixo."

3. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ uma duas
   BYTE: "Com a na entrada, quantas opções de A valem? Toque no número."
   PERGUNTA (toque): certo = duas ; errado = uma
   se errar: "A primeira opção começa com a. E o B começa com o quê?"
   se acertar: "Isso. Duas opções: é conflito, mesmo sem prefixo à vista."

4. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ *A -> a b | a c* ⏎ A -> a X ⏎ X -> b | c
   BYTE: "Primeiro coloque B dentro de A. Depois fatore."

5. TELA: # Sem prefixo? ⏎ A -> a b | B ⏎ B -> a c ⏎ A -> a b | a c ⏎ *A -> a X* ⏎ *X -> b | c*
   PROF: "Fatorar só enxerga o que está escrito. Às vezes é preciso abrir a regra antes."

#### CLIPE 30s: "Fatorar é adiar a escolha" (2 cenas, ~11s)

1. TELA: # A -> a b | a c ⏎ a ?
   BYTE: "Quando duas opções começam igual, o parser não sabe qual pegar."

2. TELA: # A -> a X ⏎ a X
   BYTE: "Fatorar lê o começo igual uma vez e decide depois."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Começo Igual — mentira: "Fatorar muda a linguagem." / verdade: "A linguagem é a mesma. Só adia a escolha."
    · [V] A -> a b | a c vira A -> a X e X -> b | c. → Isso mesmo.
    · [V] Fatorar acrescenta um nível na árvore. → Isso mesmo.
    · [F] Fatorar resolve qualquer conflito LL(1). → Conflito com FOLLOW, por exemplo, continua lá.

#### FICHA DO ASSUNTO
Em uma frase: Quando duas alternativas começam com o mesmo prefixo α, ponha α em evidência: A → αA' e A' → β₁ | β₂, adiando a decisão do analisador preditivo.
Gancho: ab + ac = a(b + c): coloque o prefixo comum em evidência.
Termos-chave: prefixo comum, fatoração, novo não-terminal, adiar, preditivo
Erros comuns: Achar que a fatoração elimina ambiguidade. // Parar na primeira rodada sem fatorar o novo não-terminal. // Usar um prefixo que não é comum a todas as alternativas do grupo. // Esquecer o ε quando uma alternativa é exatamente o prefixo.
Flashcards:
    · P: A → αβ₁ | αβ₂ fatorada? → R: A → αA'; A' → β₁ | β₂
    · P: Para que serve a fatoração à esquerda? → R: Adiar a escolha da produção até ver entrada suficiente
    · P: Qual prefixo usar? → R: O maior prefixo comum a duas ou mais alternativas
    · P: Fatore A → x y z | x y → R: A → x y A'; A' → z | ε
    · P: Else pendurado fatorado? → R: S → i E t S S' | a; S' → e S | ε
    · P: Fatorar resolve ambiguidade? → R: Não
    · P: Fatorada e sem recursão à esquerda ⇒ LL(1)? → R: Não necessariamente

#### GLOSSÁRIO (balão ao tocar no termo)
- fatoração: Pôr em evidência o começo comum de duas regras para adiar a escolha.
