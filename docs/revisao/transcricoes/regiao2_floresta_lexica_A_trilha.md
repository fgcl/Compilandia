# Região 2: Floresta Léxica — Cortar texto em tokens
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO TOKENS: Tokens, lexemas e padrões
Pré-requisitos no jogo: Fases de um compilador
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Cortar a linha» → Fazer com ajuda → Prática: você pilota «Corte você mesmo» → Pegadinha «Por que ifa não é if» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Fatiador de Tokens (cortar e classificar), Porteiro Regex (deixar entrar só quem casa)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug colou os tokens
Na tela: x>=10
BYTE: "Antes da aula: quantos tokens tem esta linha?"
Opções: 2 | ✔3 | 5
Depois: "Três: x, maior ou igual, e dez."

#### AULA ANIMADA BÁSICA: "Cortar a linha" (21 cenas, ~156s) [1º exemplo começa na cena 4]

1. TELA: # Letras demais ⏎ i f ( x 1 > = 1 0 )
   BYTE: "O parser trabalha com palavras, como nomes e operadores. Mas o arquivo chega caractere por caractere."

2. TELA: # Letras demais ⏎ i f ( x 1 > = 1 0 ) ⏎ 5 6 13
   BYTE: "Quantas palavras o parser quer ver nesta linha? Toque no número."
   PERGUNTA (toque): certo = 6 ; errado = 5 | 13
   se errar: "Nome, número, sinal e parêntese contam. Espaço não conta."
   se acertar: "Isso. Seis palavras no lugar de treze caracteres."

3. TELA: # Letras demais ⏎ i f ( x 1 > = 1 0 ) ⏎ if ( x1 >= 10 )
   BYTE: "Por isso existe a análise léxica. O analisador léxico, ou scanner, junta os caracteres em tokens antes do parser."

4. TELA: # Scanner ⏎ c o n t a = 1 0 + x 1 ;
   BYTE: "Para o computador, esta linha é só uma fila de caracteres."

5. TELA: # Scanner ⏎ c o n t a = 1 0 + x 1 ; ⏎ entram letras ⏎ conta = 10 + x1 ; ⏎ saem tokens
   BYTE: "O scanner anda por ela da esquerda para a direita e separa os pedaços. Cada pedaço vira um token."

6. TELA: # Scanner ⏎ c o n t a = 1 0 + x 1 ; ⏎ conta 10 = + ⏎ x1 ID NUM ; OP  | (Byte em cena)
   BYTE: "Cada token tem uma classe: identificador, número ou operador. É a classe que o parser enxerga."

7. TELA: # Scanner ⏎ c o n t a = 1 0 + x 1 ; ⏎ conta 10 = + ⏎ x1 ID NUM ; OP
   BYTE: "Espaços, quebras de linha e comentários são descartados aqui. Nenhuma fase depois os vê."

8. TELA: # Onde corta? ⏎ x > = 1 0
   BYTE: "Sua vez. Toque nos dois cortes desta linha."
   PERGUNTA (toque): certo = g1 | g3 ; errado = g2 | g4
   se errar: "O maior ou igual fica inteiro, e o número também."
   se acertar: "Isso. Três tokens: x, maior ou igual, e dez."

9. TELA: # Maior casamento ⏎ > = ⏎ >= ⏎ conta2
   BYTE: "Essa é a regra do maior casamento: o scanner sempre pega o pedaço mais longo possível. Por isso conta2 é um nome só."

10. TELA: # Lexema e token ⏎ x1 ID ⏎ lexema token
   BYTE: "O texto exato do pedaço é o lexema. A classe dele é o token. Vários lexemas diferentes têm o mesmo token."

11. TELA: # Receita ⏎ 1. Pule espaços e comentários. ⏎ 2. Pegue o pedaço mais longo que forma token. ⏎ 3. Dê a classe: palavra-chave, ID, NUM, OP...
   BYTE: "A receita do scanner tem três passos. Ela fica aqui na tela."

12. TELA: # Receita ⏎ 1. Pule espaços e comentários. ⏎ 2. Pegue o pedaço mais longo que forma token. ⏎ 3. Dê a classe: palavra-chave, ID, NUM, OP... ⏎ conta2
   BYTE: "Por que conta2 virou um token só, e não dois? Toque no passo."
   PERGUNTA (toque): certo = 2. Pegue o pedaço mais longo que forma token. ; errado = 1. Pule espaços e comentários. | 3. Dê a classe: palavra-chave, ID, NUM, OP...
   se errar: "O scanner segue lendo enquanto o pedaço ainda forma um nome."
   se acertar: "Isso. Passo dois, o maior casamento."

13. TELA: # Receita ⏎ 1. Pule espaços e comentários. ⏎ 2. Pegue o pedaço mais longo que forma token. ⏎ *3. Dê a classe: palavra-chave, ID, NUM, OP...* ⏎ if ifx
   BYTE: "Passo três: if é palavra reservada e tem classe própria. Já ifx é um identificador comum, pelo maior casamento."

14. TELA: # Outra linha ⏎ w h i l e ( i < = n ) i + + ;
   BYTE: "Entram letras, saem tokens. Agora quem corta é você."

15. TELA: # Outra linha ⏎ w h i l e ( i < = n ) i + + ; ⏎ ID palavra-chave
   BYTE: "A linha começa com while. Qual é a classe dele? Toque."
   PERGUNTA (toque): certo = palavra-chave ; errado = ID
   se errar: "While está na lista de palavras reservadas da linguagem."
   se acertar: "Isso. Palavra-chave, como if e for."

16. TELA: # Outra linha ⏎ w h i l e ( i < = n ) i + + ; ⏎ i + + ;
   BYTE: "Agora o fim da linha. Toque nos dois cortes deste pedaço."
   PERGUNTA (toque): certo = h1 | h3 ; errado = h2
   se errar: "O mais mais fica inteiro, como o maior ou igual."
   se acertar: "Isso. Três tokens: o nome, o mais mais e o ponto e vírgula."

17. TELA: # Outra linha ⏎ w h i l e ( i < = n ) i + + ; ⏎ 7 9 17
   BYTE: "Quantos tokens saem da linha inteira? Toque no número."
   PERGUNTA (toque): certo = 9 ; errado = 7 | 17
   se errar: "Conte cada nome, cada sinal e cada parêntese. Pule os espaços."
   se acertar: "Isso. Essa linha gera nove tokens."

18. TELA: # Outra linha ⏎ w h i l e ( i < = n ) i + + ; ⏎ while ( i <= n ) i ++ ;
   BYTE: "Nove tokens, cada um com sua classe. É isso que o parser recebe."

19. TELA: # Erros comuns ⏎ Em x>=10, o scanner gera os tokens x, >, = e 10. ⏎ certo errado
   BYTE: "Como o scanner corta este trecho? Um colega cortou assim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O scanner pega sempre o pedaço mais longo que forma um token."
   se acertar: "Isso. Pelo maior casamento, maior ou igual é um token só. São três tokens, não quatro."

20. TELA: # Erros comuns ⏎ Certo: x>=10 dá três tokens: x, >= e 10. É o maior casamento. ⏎ *Errado: Token e lexema são a mesma coisa.* ⏎ *Certo: Lexema é o texto (conta2). Token é a classe (ID).*
   BYTE: "Outro erro: confundir token com lexema. Lexema é o texto que estava no arquivo. Token é a classe que o parser recebe."

21. TELA: # Resumo ⏎ O scanner lê caracteres e entrega tokens ao parser. ⏎ Maior casamento: sempre o pedaço mais longo que forma token. ⏎ Lexema é o texto; token é a classe. Espaço e comentário somem.
   BYTE: "Resumindo: a análise léxica transforma caracteres em tokens. Ela usa o maior casamento, separa lexema de classe e joga fora espaços e comentários."

#### PRATICA: "Corte você mesmo" (5 cenas, ~26s)

1. TELA: # x>=10; ⏎ x >= 10 ;
   BYTE: "Agora você é o scanner. A linha é x maior ou igual a dez."

2. TELA: # x>=10; ⏎ x *>=* 10 ;
   BYTE: "Mais uma letra estica o pedaço. Cortar fecha o token."

3. TELA: # x>=10; ⏎ x >= 10 ;
   PROF: "Lembra da regra? Estique enquanto couber."

4. TELA: *x* > = 1 0 ;
   BYTE: "Corte a linha inteira. Estique, e corte na hora certa."
   BRINQUEDO interativo: meta = "Corte a linha inteira" ; teclas = [Mais uma letra, Cortar aqui]
   se errar: "O motor recusou. Pense no maior pedaço possível."
   ao conseguir: "Linha cortada. Foram quatro tokens."

5. TELA: # x>=10; ⏎ *x* *>=* *10* *;*
   BYTE: "Scanner de verdade faz isso milhões de vezes por segundo."

#### PEGADINHA: "Por que ifa não é if" (6 cenas, ~39s)

1. TELA: # ifa = 1; ⏎ *i* f a = 1 ;
   BYTE: "Olhe esta linha: ifa igual a um. O scanner começa pelo i."

2. TELA: # ifa = 1; ⏎ *i* *f* a = 1 ;
   BYTE: "Depois do i vem o f. O pedaço agora é if, que é palavra reservada."

3. TELA: # ifa = 1; ⏎ i f a = 1 ;
   BYTE: "Ele fecha o token agora? Toque na última letra desse token."
   PERGUNTA (toque): certo = a ; errado = f | =
   se errar: "O scanner estica enquanto der. O a ainda cabe num nome."
   se acertar: "Isso. O a ainda cabe, então o token é ifa."

4. TELA: # ifa = 1; ⏎ *i* *f* *a* = 1 ; ⏎ ifa = 1 ;
   BYTE: "Maior casamento: fica o pedaço mais comprido. Por isso ifa é um nome comum."

5. TELA: # ifa = 1; ⏎ i f a = 1 ; ⏎ ifa = 1 ;
   PROF: "A palavra reservada só ganha no empate. If sozinho tem o mesmo tamanho nas duas regras."

6. TELA: !!MAIOR CASAMENTO!!
   BYTE: "Ordem de decisão: primeiro o tamanho, depois a reservada."

#### CLIPE 30s: "Token e lexema" (2 cenas, ~10s)

1. TELA: # Lexema ⏎ conta
   BYTE: "Lexema é o texto que está no código, como conta."

2. TELA: # Token ⏎ conta ⏎ identificador
   BYTE: "Token é a classe dele, como identificador."

#### CLIPE 30s: "Maior casamento" (2 cenas, ~9s)

1. TELA: # Maior casamento ⏎ x *>* = 1
   BYTE: "O scanner fica com o pedaço mais comprido que puder."

2. TELA: # Maior casamento ⏎ x *>* *=* 1
   BYTE: "Maior ou igual é um token só, e não dois."

#### CLIPE 30s: "Palavra-chave ou nome" (2 cenas, ~10s)

1. TELA: # Empate ⏎ if ⏎ nome reservada
   BYTE: "If casa com a regra de nome e com a palavra reservada."

2. TELA: # Empate ⏎ if ⏎ nome *reservada*
   BYTE: "Quando o tamanho empata, a reservada ganha."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Lexema Gêmeo — mentira: "Token e lexema são a mesma coisa." / verdade: "Lexema é o texto. Token é a classe: id, num, if."
    · [V] Em conta = 10, conta é lexema e id é token. → Isso mesmo.
    · [V] Dois lexemas diferentes podem ter o mesmo token. → Isso mesmo.
    · [F] Cada lexema tem um token só dele. → x e y têm o mesmo token: id.
- Bug do Corte Curto — mentira: ">= são dois tokens: > e =." / verdade: "O scanner pega o maior pedaço que casa."
    · [V] ifa é um identificador. → Isso mesmo.
    · [F] Em x>=10, o scanner fecha > assim que o vê. → Ele tenta esticar e fecha >=.
    · [F] ifa vira o token if seguido de a. → Maior casamento: fica ifa inteiro.

#### FICHA DO ASSUNTO
Em uma frase: Lexema é o texto real, token é a classe (nome + atributo) e padrão é a regra que diz quais lexemas pertencem ao token.
Gancho: Token é a gaveta, lexema é a meia, padrão é a etiqueta.
Termos-chave: token, lexema, padrão, atributo, maior casamento, palavra-chave
Erros comuns: Contar espaços e comentários como tokens. // Separar `>=` em dois tokens (o maior casamento pega `>=` inteiro). // Achar que `x` e `y` são tokens diferentes (mesmo token id, lexemas diferentes). // Achar que `iff` é a palavra-chave if seguida de um f.
Flashcards:
    · P: O que é um lexema? → R: A sequência de caracteres do fonte que casa com o padrão de um token
    · P: O que é um token? → R: Um par ⟨nome, atributo⟩ que representa uma classe de unidades léxicas
    · P: O que é um padrão? → R: A descrição (em geral uma ER) dos lexemas de um token
    · P: Quantos tokens em `x>=10;`? → R: 4: x, >=, 10, ;
    · P: Regra do maior casamento? → R: Escolher o maior prefixo que casa com algum padrão
    · P: `if` casa com id e palavra-chave: quem vence? → R: A palavra-chave (reservada, de maior prioridade)
    · P: Atributo do token id? → R: Ponteiro para a entrada na tabela de símbolos
    · P: Em `fi (a == b)`, o léxico acusa erro? → R: Não: fi é um identificador válido

#### GLOSSÁRIO (balão ao tocar no termo)
- token: Um pedaço com nome. Em x = 10, o 10 é um token do tipo número.
- lexema: O texto exato de um token. No token número, o lexema pode ser 10.


## ASSUNTO REGEX_DFA: Expressões regulares e autômatos
Pré-requisitos no jogo: Tokens, lexemas e padrões
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O trem do autômato» → Fazer com ajuda → Programar o Byte → Prática: você pilota «Pilote o trem» → Pegadinha «Estrela, mais e interrogação» → Desafio
Jogos que treinam: Trem do Autômato (pilotar o trem estado a estado), Porteiro Regex (deixar entrar só quem casa), Fábrica de Autômatos (montar o autômato e passar nos robôs de teste)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: Trilho que você constrói

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug soltou uma regex
Na tela: a*b
BYTE: "Antes da aula: qual cadeia casa com ela?"
Opções: ba | ✔aab | abb
Depois: "Vale a a b. Quantos a quiser, e um b no fim."

#### AULA ANIMADA BÁSICA: "O trem do autômato" (22 cenas, ~159s) [1º exemplo começa na cena 3]

1. TELA: # Nomes sem fim ⏎ x1 conta ⏎ 1x total2
   BYTE: "Existem infinitos nomes de variável. Não dá para listar todos. É preciso descrever o formato deles."

2. TELA: # Nomes sem fim ⏎ x1 conta ⏎ 1x total2
   BYTE: "Um destes não pode ser nome de variável. Toque nele."
   PERGUNTA (toque): certo = 1x ; errado = x1 | conta | total2
   se errar: "Em C, o nome começa com letra, nunca com dígito."
   se acertar: "Isso. Nome não começa com dígito. Falta um jeito curto e exato de escrever essa regra."

3. TELA: # a b*
   BYTE: "Uma expressão regular descreve um formato de cadeia. Esta diz: um a, seguido de quantos b quiser, até nenhum."

4. TELA: # a b* ⏎ q0 q1  | setas: q0 --a--> q1, q1 --b--> q1
   BYTE: "O autômato finito é a máquina que confere esse formato. Cada círculo é um estado, e cada seta é uma transição."

5. TELA: # a b* ⏎ q0 q1 ⏎ *a* b b  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "O trem começa no estado inicial e lê um símbolo por vez. É um AFD: de cada estado sai no máximo uma seta por símbolo."

6. TELA: # a b* ⏎ q0 q1 ⏎ a *b* b  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "Leu a. O trem segue a seta do a até o outro estado."

7. TELA: # a b* ⏎ q0 q1 ⏎ a b *b*  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "O trem está no segundo estado e lê b. Para onde ele vai? Toque."
   PERGUNTA (toque): certo = q1 ; errado = q0
   se errar: "Procure a seta com b que sai do estado onde ele está."
   se acertar: "Isso. A seta com b é um laço no mesmo estado."

8. TELA: # a b* ⏎ q0 q1 ⏎ a b *b*  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "Leu b. Dá a volta no laço e fica no mesmo estado."

9. TELA: # a b* ⏎ q0 *q1* ⏎ a b b  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "A cadeia acabou num estado final, o de círculo duplo. A cadeia foi aceita."

10. TELA: # a b* ⏎ Aceita Rejeita ⏎ q0 *q1* ⏎ b a  | setas: q0 --a--> q1, q1 --b--> q1  | (Byte em cena)
   BYTE: "E a cadeia b a? O trem sai do primeiro estado lendo b. Toque na resposta."
   PERGUNTA (toque): certo = Rejeita ; errado = Aceita
   se errar: "Olhe as setas que saem do primeiro estado. Alguma tem b?"
   se acertar: "Certo. Sem transição para b, o autômato rejeita a cadeia."

11. TELA: # a b* ⏎ q0 *q1* ⏎ b a  | setas: q0 --a--> q1, q1 --b--> q1
   BYTE: "É assim que o scanner reconhece cada tipo de token: uma expressão regular descreve, e um autômato confere."

12. TELA: # Receita ⏎ 1. Comece no estado inicial. ⏎ 2. A cada símbolo, siga a transição dele. ⏎ 3. Faltou transição: rejeite a cadeia. ⏎ 4. Acabou num estado final: aceita.
   BYTE: "Para ler uma cadeia, o trem segue sempre quatro passos."

13. TELA: # Receita ⏎ 1. Comece no estado inicial. ⏎ 2. A cada símbolo, siga a transição dele. ⏎ 3. Faltou transição: rejeite a cadeia. ⏎ 4. Acabou num estado final: aceita. ⏎ a b a
   BYTE: "Em a b a, o trem trava na última letra. Qual passo explica? Toque."
   PERGUNTA (toque): certo = 3. Faltou transição: rejeite a cadeia. ; errado = 1. Comece no estado inicial. | 2. A cada símbolo, siga a transição dele. | 4. Acabou num estado final: aceita.
   se errar: "Depois de a b, ele está no segundo estado. Sai seta com a dali?"
   se acertar: "Isso. Sem seta para a letra, o trem rejeita a cadeia."

14. TELA: # Receita ⏎ 1. Comece no estado inicial. ⏎ 2. A cada símbolo, siga a transição dele. ⏎ 3. Faltou transição: rejeite a cadeia. ⏎ 4. Acabou num estado final: aceita. ⏎ b*  →  ε, b, bb, bbb...
   BYTE: "Na expressão, a estrela repete zero ou mais vezes. A barra quer dizer ou. E um símbolo ao lado do outro é sequência."

15. TELA: # a b* c ⏎ q0 q1 q2  | setas: q0 --a--> q1, q1 --b--> q1, q1 --c--> q2
   BYTE: "Outra expressão: a, depois b quantas vezes quiser, e c no fim."

16. TELA: # a b* c ⏎ q0 q1 q2  | setas: q0 --a--> q1, q1 --b--> q1, q1 --c--> q2
   BYTE: "Qual destes estados deve ser final? Toque nele."
   PERGUNTA (toque): certo = q2 ; errado = q0 | q1
   se errar: "A cadeia só pode acabar depois da letra c."
   se acertar: "Isso. Só a seta com a letra c chega ao final."

17. TELA: # a b* c ⏎ q0 q1 q2 ⏎ a c ⏎ Aceita Rejeita  | setas: q0 --a--> q1, q1 --b--> q1, q1 --c--> q2
   BYTE: "A cadeia a c é aceita ou rejeitada? Toque na resposta."
   PERGUNTA (toque): certo = Aceita ; errado = Rejeita
   se errar: "A estrela deixa a letra b aparecer zero vezes."
   se acertar: "Isso. A letra b pode faltar, e a cadeia passa."

18. TELA: # a b* c ⏎ q0 q1 q2 ⏎ a b c b ⏎ Aceita Rejeita  | setas: q0 --a--> q1, q1 --b--> q1, q1 --c--> q2
   BYTE: "Agora a cadeia a b c b. Ela é aceita ou rejeitada? Toque."
   PERGUNTA (toque): certo = Rejeita ; errado = Aceita
   se errar: "Depois do c, o trem está no final. Sai seta com b dali?"
   se acertar: "Isso. Sobrou um b sem seta, então o trem rejeita a cadeia."

19. TELA: # a b* c ⏎ q0 q1 *q2* ⏎ a b c b  | setas: q0 --a--> q1, q1 --b--> q1, q1 --c--> q2
   BYTE: "O scanner tem uma máquina dessas para cada tipo de token."

20. TELA: # Erros comuns ⏎ A cadeia passou por um estado final no meio do caminho, então foi aceita. ⏎ certo errado
   BYTE: "Quando o autômato aceita? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O que importa é onde o trem está quando a cadeia termina."
   se acertar: "Isso. Só vale o estado em que a leitura termina. Passar por um final no meio não aceita nada."

21. TELA: # Erros comuns ⏎ Certo: Aceita só se a cadeia inteira foi lida e parou num estado final. ⏎ *Errado: A expressão a* exige pelo menos um a.* ⏎ *Certo: A estrela aceita zero vezes. Para uma ou mais, use a a* (ou a+).*
   BYTE: "Outro erro: achar que a estrela exige ao menos uma repetição. Estrela é zero ou mais. A cadeia vazia também casa."

22. TELA: # Resumo ⏎ A expressão regular descreve o formato; o autômato confere. ⏎ AFD: um estado inicial, uma transição por símbolo, estados finais. ⏎ Aceita se leu tudo e parou num final. Sem transição, rejeita.
   BYTE: "Resumindo: a expressão regular descreve o formato de um token, e o autômato finito confere cadeia por cadeia. Aceitar é terminar a leitura num estado final."

#### TABELAS: "O autômato vira tabela" (10 cenas, ~56s)

1. TELA: # (a|b)*abb ⏎ q0 q1 ⏎ q3 q2  | setas: q0 --a--> q1, q0 --b--> q0, q1 --a--> q1, q1 --b--> q2, q2 --a--> q1, q2 --b--> q3, q3 --a--> q1, q3 --b--> q0
   BYTE: "Este autômato aceita as palavras que terminam em abb."

2. TELA: # (a|b)*abb ⏎ *q0 início{a → q1; b → q0}* q1{a → q1; b → q2} ⏎ q3 final{a → q1; b → q0} q2{a → q1; b → q3}
   BYTE: "Agora cada estado vira caixa: o nome em cima, uma linha por letra."

3. TELA: # (a|b)*abb ⏎ q0 início{a → q1; b → q0} *q1{a → q1; »b → q2«}* ⏎ q3 final{a → q1; b → q0} q2{a → q1; b → q3}
   BYTE: "O fio sai da linha certa. A linha b do q1 leva ao q2."

4. TELA: # (a|b)*abb ⏎ q0 início{a → q1; b → q0} q1{a → q1; b → q2} ⏎ q3 final{a → q1; b → q0} *q2{a → q1; »b → q3«}*
   BYTE: "Estou no q2 e chegou um b. Toque no estado de destino."
   PERGUNTA (toque): certo = q3 final ; errado = q0 início | q1 | q2
   se errar: "Procure a linha do b dentro da caixa do q2."
   se acertar: "Isso. O b do q2 leva ao q3, que é o estado final."

5. TELA: # Tabela de transição ⏎ δ a b ⏎ *→q0* q1 q0 ⏎ q1 q1 q2 ⏎ q2 q1 q3 ⏎ **q3* q1 q0
   BYTE: "Junte as caixas e cada uma vira uma linha. A seta marca o início, e o asterisco, o final."

6. TELA: # Tabela de transição ⏎ δ a b ⏎ →q0 *q1* q0 ⏎ q1 q1 q2 ⏎ q2 q1 q3 ⏎ *q3 q1 q0 ⏎ a b b
   BYTE: "Vamos ler abb. Começa na linha do q0 e lê o a."

7. TELA: # Tabela de transição ⏎ δ a b ⏎ →q0 q1 q0 ⏎ q1 q1 *q2* ⏎ q2 q1 q3 ⏎ *q3 q1 q0 ⏎ a b b
   BYTE: "A célula diz q1. Pula para a linha do q1 e lê o b."

8. TELA: # Tabela de transição ⏎ δ a b ⏎ →q0 q1 q0 ⏎ q1 q1 q2 ⏎ q2 q1 *q3* ⏎ *q3 q1 q0 ⏎ a b b
   BYTE: "Deu q2. Lê o último b, e a célula manda para o q3."

9. TELA: # Tabela de transição ⏎ δ a b ⏎ →q0 q1 q0 ⏎ q1 q1 q2 ⏎ q2 q1 q3 ⏎ **q3* q1 q0 ⏎ a b b
   PROF: "O q3 tem asterisco. A palavra foi aceita só olhando a tabela."

10. TELA: δ a b ⏎ *→q0* q1 q0 ⏎ q1 q1 q2 ⏎ q2 q1 q3 ⏎ *q3 q1 q0
   BYTE: "Agora você. Leia quatro letras e termine na linha com asterisco."
   BRINQUEDO interativo: meta = "Quatro letras, terminando na linha com asterisco." ; teclas = [a, b]
   se errar: "Olhe a célula da linha acesa. Para terminar no q3, as três últimas precisam ser abb."
   ao conseguir: "Chegou ao final usando só a tabela."

#### PRATICA: "Pilote o trem" (5 cenas, ~29s)

1. TELA: # a a* b ⏎ a a* b
   BYTE: "Esta regex pede um a, quantos a mais quiser, e um b no fim."

2. TELA: # a a* b ⏎ a *a** b
   BYTE: "O autômato tem um estado para cada situação. Cada letra é uma seta."

3. TELA: # a a* b ⏎ a *a** b
   PROF: "O laço no meio é a estrela: dá para ficar ali lendo a."

4. TELA: *q0* q1 q2  | setas: q0 --a--> q1, q1 --a--> q1, q1 --b--> q2  | (Byte em cena)
   BYTE: "Agora pilote. Chegue no círculo duplo com exatamente três letras."
   BRINQUEDO interativo: meta = "Chegue no círculo duplo com 3 letras" ; teclas = [a, b]
   se errar: "Essa letra não deu certo. Olhe as setas do estado aceso."
   ao conseguir: "Chegou no círculo duplo. A frase foi aceita."

5. TELA: # a a* b ⏎ a *a** b
   BYTE: "Pilotar é rodar o autômato de cabeça. É isso que a prova pede."

#### PEGADINHA: "Estrela, mais e interrogação" (5 cenas, ~31s)

1. TELA: # a*   a+   a? ⏎ vazia a aa ⏎ a* ⏎ a+ ⏎ a?
   BYTE: "Três símbolos parecidos, que a prova adora trocar."

2. TELA: # a*   a+   a? ⏎ vazia a aa ⏎ *a** sim sim sim ⏎ a+ ⏎ a?
   BYTE: "A estrela aceita zero ou mais. Vale vazio, um a e dois a."

3. TELA: # a*   a+   a? ⏎ vazia a aa ⏎ a* sim sim sim ⏎ *a+* ⏎ a?
   BYTE: "O mais exige pelo menos um. Toque na casa em que a mais recusa a cadeia."
   PERGUNTA (toque): certo = ml2c1 ; errado = ml2c2 | ml2c3
   se errar: "Pelo menos um a. Qual cadeia não tem nenhum?"
   se acertar: "Isso. A cadeia vazia fica de fora."

4. TELA: # a*   a+   a? ⏎ vazia a aa ⏎ a* sim sim sim ⏎ a+ não sim sim ⏎ *a?* sim sim não
   BYTE: "A interrogação é zero ou um. Vale vazio, vale um a, e dois a passa do limite."

5. TELA: # a*   a+   a? ⏎ vazia a aa ⏎ a* sim sim sim ⏎ a+ não sim sim ⏎ a? sim sim não
   PROF: "Estrela é zero ou mais. Mais é um ou mais. Interrogação é zero ou um."

#### CLIPE 30s: "O que a estrela faz" (2 cenas, ~12s)

1. TELA: # a* ⏎ a
   BYTE: "A estrela repete o que vem antes, zero ou mais vezes."

2. TELA: # a* ⏎ a a a
   BYTE: "Então a seguido de estrela aceita vazio, um a, dois a, e assim por diante."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Estrela Cheia — mentira: "a* exige pelo menos um a." / verdade: "A estrela aceita zero ou mais. A cadeia vazia entra."
    · [V] a+ exige pelo menos um a. → Isso mesmo.
    · [V] A cadeia vazia casa com a*. → Isso mesmo.
    · [F] (ab)* aceita aba. → Só repetições inteiras de ab.
- Bug da Regex que Conta — mentira: "Regex confere parênteses balanceados." / verdade: "Autômato finito não conta sem limite. Para isso existe gramática."
    · [V] a^n b^n não é linguagem regular. → Isso mesmo.
    · [V] Gramática livre de contexto confere parênteses. → Isso mesmo.
    · [F] Um AFD tem memória infinita. → Ele só lembra o estado atual.

#### FICHA DO ASSUNTO
Em uma frase: Expressões regulares descrevem os padrões dos tokens; Thompson as transforma em AFN, e a construção de subconjuntos gera um AFD que reconhece cada cadeia com um passo por caractere.
Gancho: Thompson Transforma (ER → AFN), Subconjuntos Simplificam (AFN → AFD).
Termos-chave: expressão regular, afn, afd, estado, transição, subconjuntos, thompson
Erros comuns: Achar que AFN reconhece mais linguagens do que AFD. // Esquecer de aplicar o ε-fecho depois do move. // Errar a precedência: * > concatenação > |. // Achar que uma ER consegue descrever aⁿbⁿ ou parênteses balanceados.
Flashcards:
    · P: Precedência dos operadores de ER? → R: * > concatenação > |
    · P: r+ escrito com *? → R: r r*
    · P: r? escrito com |? → R: r | ε
    · P: Algoritmo ER → AFN? → R: Construção de Thompson
    · P: Algoritmo AFN → AFD? → R: Construção de subconjuntos
    · P: O que é ε-fecho(T)? → R: Os estados alcançáveis a partir de T só com transições ε
    · P: Máximo de estados do AFD vindo de um AFN de n estados? → R: 2ⁿ
    · P: Estados do AFD mínimo de (a|b)*abb? → R: 4
    · P: Uma ER reconhece aⁿbⁿ? → R: Não: a linguagem não é regular

#### GLOSSÁRIO (balão ao tocar no termo)
- autômato: Uma máquina de estados que lê letra por letra e diz se aceita a palavra.
- AFD: Autômato em que cada letra leva a um estado só. Nunca há dúvida.


## ASSUNTO NFA_TO_DFA: AFN → AFD (construção de subconjuntos)
Pré-requisitos no jogo: Expressões regulares e autômatos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «De muitos caminhos para um» → Pegadinha «O fecho esquecido» → Desafio
Jogos que treinam: Trem do Autômato (pilotar o trem estado a estado), Fábrica de Subconjuntos (juntar estados do AFN em estados do AFD)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug duplicou as setas
Na tela: q0 a q1 / q0 a q2
BYTE: "Antes da aula: lendo a, para onde o autômato vai?"
Opções: q1 | q2 | ✔os dois
Depois: "Para os dois. O AFD guarda esse conjunto como um estado só."

#### AULA ANIMADA BÁSICA: "De muitos caminhos para um" (21 cenas, ~168s) [1º exemplo começa na cena 4]

1. TELA: # AFN: termina em ab ⏎ 0 1 2  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2
   BYTE: "Este é um AFN, um autômato não determinístico. Nele, o mesmo símbolo pode levar a dois estados. Lendo a, o estado zero tem duas saídas."

2. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ fica no 0 vai para 1 as duas  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2
   BYTE: "O trem está no zero e lê a. Qual seta ele segue? Toque."
   PERGUNTA (toque): certo = as duas ; errado = fica no 0 | vai para 1
   se errar: "Nada no desenho escolhe entre as duas setas."
   se acertar: "Isso. O AFN segue as duas ao mesmo tempo. Ele aceita se algum caminho termina num estado final."

3. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0}  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2
   BYTE: "Um programa não pode adivinhar o caminho certo. A saída é acompanhar o conjunto de todos os estados em que o AFN pode estar."

4. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} *{0,1}*  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}
   BYTE: "Começo no conjunto que só tem o zero. Lendo a, o AFN pode estar em zero ou em um. Nasce o conjunto zero e um."

5. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} *{0,1}* ⏎ {0} {0,2} {2}  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}
   BYTE: "Do conjunto zero e um, lendo b, para onde vamos? Toque no conjunto certo."
   PERGUNTA (toque): certo = {0,2} ; errado = {0} | {2}
   se errar: "Veja cada estado do conjunto. Com b, o zero fica no zero e o um vai para o dois."
   se acertar: "Isso. Juntando os dois caminhos, dá zero e dois."

6. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} {0,1} *{0,2}*  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}, {0,1} --b--> {0,2}
   BYTE: "Cada conjunto novo vira um estado do AFD. Se o conjunto contém algum estado final do AFN, ele é final no AFD."

7. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} {0,1} *{0,2}*  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}, {0,1} --b--> {0,2}
   BYTE: "Do conjunto zero e dois, lendo a, para onde vamos? Toque no conjunto."
   PERGUNTA (toque): certo = {0,1} ; errado = {0} | {0,2}
   se errar: "Lendo a letra a, o estado zero vai para zero e um. O dois não anda."
   se acertar: "Isso. Volta para o conjunto zero e um, que já existe."

8. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} {0,1} {0,2}  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}, {0,1} --b--> {0,2}, {0} --b--> {0}, {0,1} --a--> {0,1}, {0,2} --a--> {0,1}, {0,2} --b--> {0}
   BYTE: "As transições que faltam caem em conjuntos que já existem. Quando não surge conjunto novo, a construção acabou."

9. TELA: # AFN: termina em ab ⏎ 0 1 2 ⏎ {0} {0,1} {0,2}  | setas: 0 --a b--> 0, 0 --a--> 1, 1 --b--> 2, {0} --a--> {0,1}, {0,1} --b--> {0,2}, {0} --b--> {0}, {0,1} --a--> {0,1}, {0,2} --a--> {0,1}, {0,2} --b--> {0}
   BYTE: "Pronto. O AFD aceita as mesmas cadeias do AFN, sem chute: uma transição só para cada símbolo."

10. TELA: # Receita ⏎ 1. Comece pelo ε-fecho do estado inicial. ⏎ 2. Para cada símbolo, junte os destinos (move). ⏎ 3. Aplique o ε-fecho. Conjunto novo vira estado. ⏎ 4. Tem final dentro? O conjunto é final.
   BYTE: "Esse método é a construção de subconjuntos. Ela tem quatro passos."

11. TELA: # Receita ⏎ *1. Comece pelo ε-fecho do estado inicial.* ⏎ 2. Para cada símbolo, junte os destinos (move). ⏎ *3. Aplique o ε-fecho. Conjunto novo vira estado.* ⏎ 4. Tem final dentro? O conjunto é final.
   PROF: "Quando o AFN tem transição épsilon, ele passa por ela sem ler nada. O épsilon fecho de um conjunto junta tudo que se alcança assim."

12. TELA: # Receita ⏎ 1. Comece pelo ε-fecho do estado inicial. ⏎ 2. Para cada símbolo, junte os destinos (move). ⏎ 3. Aplique o ε-fecho. Conjunto novo vira estado. ⏎ 4. Tem final dentro? O conjunto é final.
   BYTE: "O conjunto zero e dois virou final. Qual passo decide isso? Toque."
   PERGUNTA (toque): certo = 4. Tem final dentro? O conjunto é final. ; errado = 1. Comece pelo ε-fecho do estado inicial. | 2. Para cada símbolo, junte os destinos (move). | 3. Aplique o ε-fecho. Conjunto novo vira estado.
   se errar: "No AFN, o estado dois é final."
   se acertar: "Isso. Basta um final dentro do conjunto."

13. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4
   BYTE: "Outro AFN, agora com setas épsilon. Quem monta os conjuntos é você."

14. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4
   BYTE: "Primeiro passo: o fecho épsilon do zero. Toque nos estados que entram."
   PERGUNTA (toque): certo = 0 | 1 | 3 ; errado = 2 | 4
   se errar: "Comece no zero e siga só as setas épsilon."
   se acertar: "Isso. Zero, um e três, sem ler nenhuma letra."

15. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4 ⏎ {0,1,3} ⏎ {2,4} {2} {1,2}  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4
   BYTE: "Desse conjunto, lendo a, aonde chegamos? Toque no conjunto."
   PERGUNTA (toque): certo = {2} ; errado = {2,4} | {1,2}
   se errar: "Só o estado um tem seta com a letra a. Ela leva ao dois."
   se acertar: "Isso. Nasce o conjunto que só tem o dois."

16. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4 ⏎ {2} ⏎ {0,1,3} ⏎ {3,4} {2,4} {4}  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4, {0,1,3} --a--> {2}
   BYTE: "E se o conjunto zero, um e três ler c? Toque no destino."
   PERGUNTA (toque): certo = {4} ; errado = {3,4} | {2,4}
   se errar: "Procure a única seta com a letra c no desenho."
   se acertar: "Isso. Nasce o conjunto que só tem o quatro."

17. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4 ⏎ {2} ⏎ {0,1,3} ⏎ {4}  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4, {0,1,3} --a--> {2}, {0,1,3} --c--> {4}
   BYTE: "Quais estados do AFD são finais? Toque em todos."
   PERGUNTA (toque): certo = {2} | {4} ; errado = {0,1,3}
   se errar: "No AFN, o dois e o quatro são finais."
   se acertar: "Isso. Cada um tem um final dentro."

18. TELA: # AFN: ab* | c ⏎ 1 2 ⏎ 0 ⏎ 3 4 ⏎ *{2}* ⏎ {0,1,3} ⏎ *{4}*  | setas: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4, {0,1,3} --a--> {2}, {0,1,3} --c--> {4}, {2} --b--> {2}
   BYTE: "Pronto. O AFD tem três estados e lê sem adivinhar. No pior caso, um AFN de n estados vira um AFD de dois elevado a n."

19. TELA: # Erros comuns ⏎ O conjunto {0, 2} só é final se todos os estados dele forem finais. ⏎ certo errado
   BYTE: "Quando um conjunto é final no AFD? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O AFN aceita se algum caminho chega a um final."
   se acertar: "Isso. Basta um estado final dentro do conjunto. O AFN aceita quando pelo menos um caminho dá certo."

20. TELA: # Erros comuns ⏎ Certo: Basta um estado final do AFN dentro do conjunto. ⏎ *Errado: Calcular o move e esquecer o ε-fecho do resultado.* ⏎ *Certo: Depois de cada move, aplique o ε-fecho. Senão faltam estados.*
   BYTE: "Outro erro: esquecer o épsilon fecho depois de ler o símbolo. Os estados alcançados por épsilon também fazem parte do conjunto."

21. TELA: # Resumo ⏎ Cada estado do AFD é um conjunto de estados do AFN. ⏎ Começa no ε-fecho do inicial. Para cada símbolo: move e ε-fecho. ⏎ Conjunto com algum final é final. Acaba quando não surge conjunto novo.
   BYTE: "Resumindo: a construção de subconjuntos anda com todos os estados possíveis de uma vez. Cada conjunto vira um estado do AFD, e basta um final dentro para aceitar."

#### PEGADINHA: "O fecho esquecido" (5 cenas, ~35s)

1. TELA: # Começo do AFD ⏎ q0 q1 q2  | setas: q0 --ε--> q1, q1 --a--> q2
   BYTE: "Este autômato anda de q0 para q1 sem ler nada. A seta com épsilon é de graça."

2. TELA: # Começo do AFD ⏎ q0 q1 q2  | setas: q0 --ε--> q1, q1 --a--> q2
   BYTE: "Qual é o estado inicial do AFD? Toque em todos os estados do AFN que entram nele."
   PERGUNTA (toque): certo = q0 | q1 ; errado = q2
   se errar: "Comece em q0 e ande pelas setas de épsilon. Sem ler letra nenhuma."
   se acertar: "Isso. Inicial é q0 mais tudo que se alcança sem ler."

3. TELA: # Começo do AFD ⏎ q0 q1 q2 ⏎ { q0, q1 } ⏎ estado inicial do AFD  | setas: q0 --ε--> q1, q1 --a--> q2
   BYTE: "O erro comum é escrever só q0. Sem o fecho, a seta com a some, e o AFD aceita menos."

4. TELA: # Começo do AFD ⏎ q0 q1 q2 ⏎ { q0, q1 } ⏎ estado inicial do AFD ⏎ *move  depois  fecho*  | setas: q0 --ε--> q1, q1 --a--> q2
   PROF: "A receita tem dois passos: anda com a letra, depois fecha com épsilon."

5. TELA: !!FECHO!!
   BYTE: "Nunca pule o fecho. Ele vale no começo e depois de cada letra."

#### CLIPE 30s: "AFN e AFD" (2 cenas, ~9s)

1. TELA: # AFD ⏎ q1 ⏎ q0 ⏎ q2  | setas: q0 --a--> q1
   BYTE: "No AFD, cada letra leva a um só lugar."

2. TELA: # AFN ⏎ q1 ⏎ q0 ⏎ q2  | setas: q0 --a--> q1, q0 --a--> q2
   BYTE: "No AFN, a mesma letra pode levar a vários lugares."

#### CLIPE 30s: "Épsilon-fecho" (2 cenas, ~11s)

1. TELA: # Fecho ⏎ *q0* q1 q2  | setas: q0 --ε--> q1, q1 --ε--> q2
   BYTE: "O fecho junta um estado com tudo que se alcança sem ler nada."

2. TELA: # Fecho ⏎ *q0* *q1* *q2*  | setas: q0 --ε--> q1, q1 --ε--> q2
   BYTE: "É o passeio de graça pelas setas de épsilon."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Fecho Esquecido — mentira: "O início do AFD é só o início do AFN." / verdade: "É o fecho dele: tudo que se alcança sem ler nada."
    · [V] Cada estado do AFD é um conjunto de estados do AFN. → Isso mesmo.
    · [V] Um conjunto é final se tiver um estado final do AFN. → Isso mesmo.
    · [F] Depois de ler uma letra, não preciso fechar de novo. → Sempre feche depois do move.

#### FICHA DO ASSUNTO
Em uma frase: A construção de subconjuntos transforma um AFN em AFD: cada estado do AFD é um CONJUNTO de estados do AFN, começando em ε-fecho({s₀}), com Dtran[T, a] = ε-fecho(move(T, a)); é final todo conjunto que contém um final do AFN.
Gancho: Anda e fecha a porta: move com a letra, depois ε-fecho. Cesto com uma fruta premiada é final; cesto vazio é o estado morto.
Termos-chave: conjunto, fecho, move, final, morto, inicial
Erros comuns: Esquecer o ε-fecho depois do move // Usar {s₀} como estado inicial em vez de ε-fecho({s₀}) // Marcar como final só o conjunto em que TODOS os estados são finais (basta um) // Achar que a construção de subconjuntos já entrega o AFD mínimo // Achar que AFN reconhece mais linguagens que AFD
Flashcards:
    · P: O que é um estado do AFD na construção de subconjuntos? → R: Um conjunto de estados do AFN
    · P: Fórmula de Dtran[T, a]? → R: ε-fecho(move(T, a))
    · P: Estado inicial do AFD? → R: ε-fecho({s₀})
    · P: Quando um estado do AFD é final? → R: Quando contém pelo menos um estado final do AFN
    · P: move(T, a) vazio leva a quê? → R: Ao estado morto ∅
    · P: Máximo de estados do AFD para um AFN de n estados? → R: 2ⁿ
    · P: ε-fecho({0}) no AFN de Thompson de (a|b)*abb? → R: {0, 1, 2, 4, 7}
    · P: Linguagem clássica da explosão exponencial? → R: O n-ésimo símbolo a partir do fim é a (2ⁿ estados)
    · P: Por que o léxico usa AFD e não AFN? → R: Um passo por caractere, sem carregar conjuntos

#### GLOSSÁRIO (balão ao tocar no termo)
- AFN: Autômato que pode ter várias saídas para a mesma letra, ou andar sem ler nada.


## ASSUNTO DFA_MINIMIZATION: Minimização de AFD (tabela de pares)
Pré-requisitos no jogo: Expressões regulares e autômatos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Juntar os gêmeos» → Pegadinha «Juntar gêmeos de mentira» → Desafio
Jogos que treinam: Trem do Autômato (pilotar o trem estado a estado), Tabela dos Pares (marcar pares distinguíveis e juntar estados), Fábrica de Autômatos (montar o autômato e passar nos robôs de teste)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug clonou um estado
Na tela: q1 = q2
BYTE: "Antes da aula: dois estados fazem tudo igual. O que fazer?"
Opções: apagar | ✔juntar | deixar
Depois: "Juntar. Um estado só faz o mesmo trabalho."

#### AULA ANIMADA BÁSICA: "Juntar os gêmeos" (20 cenas, ~149s)

1. TELA: # AFD ⏎ B ⏎ A ⏎ D ⏎ C  | setas: A --a--> B, A --b--> C, B --b--> D, C --b--> D
   BYTE: "Este AFD tem quatro estados. Talvez dois deles façam exatamente o mesmo papel."

2. TELA: # AFD ⏎ B ⏎ A ⏎ D ⏎ C  | setas: A --a--> B, A --b--> C, B --b--> D, C --b--> D
   BYTE: "Qual destes estados é final? Toque nele."
   PERGUNTA (toque): certo = D ; errado = A | B | C
   se errar: "Final tem círculo duplo."
   se acertar: "Isso. D é o único final."

3. TELA: # AFD ⏎ *B* ⏎ A ⏎ D ⏎ *C*  | setas: A --a--> B, A --b--> C, B --b--> D, C --b--> D
   BYTE: "Dois estados são equivalentes quando, a partir deles, as mesmas cadeias levam a aceitar e as mesmas levam a rejeitar."

4. TELA: !!MÍNIMO!!
   BYTE: "Juntar os estados equivalentes dá o AFD mínimo: o menor que reconhece a mesma linguagem."

5. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B ⏎ C ⏎ D
   BYTE: "A ferramenta é a tabela de pares. Cada casa compara dois estados. Um X na casa quer dizer que os dois são diferentes."

6. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B ⏎ C ⏎ D *X* *X* *X*
   BYTE: "Primeira rodada: um estado final nunca é equivalente a um não final. Marco todo par que tem o D."

7. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B ⏎ C ⏎ D X X X ⏎ (C,D) já tem X
   BYTE: "Agora olho para onde cada par vai com cada símbolo. Com b, A vai para C, e B vai para D."

8. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B *X* ⏎ C *X* ⏎ D X X X ⏎ (C,D) já tem X
   BYTE: "O par C e D já está marcado. Se os destinos são diferentes, as origens também são. Marco A com B."

9. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B X ⏎ C X ⏎ D X X X
   BYTE: "Sobrou uma casa em branco. Toque nela: esses dois são gêmeos."
   PERGUNTA (toque): certo = tl2c2 ; errado = X | X | X | X | X
   se errar: "Procure a casa sem X na tabela."
   se acertar: "Isso. B e C vão sempre para o mesmo lugar."

10. TELA: # Tabela de pares ⏎ D é o único final ⏎ A B C ⏎ B X ⏎ C X ⏎ D X X X ⏎ *B + C*
   BYTE: "Casa em branco no fim quer dizer estados equivalentes. B e C viram um estado só."

11. TELA: # Receita ⏎ 1. Marque todo par final com não final. ⏎ 2. Marque o par que vai a um par marcado. ⏎ 3. Repita até nada mudar. Branco: junte.
   BYTE: "A receita tem três passos e se repete até parar."

12. TELA: # Receita ⏎ 1. Marque todo par final com não final. ⏎ 2. Marque o par que vai a um par marcado. ⏎ 3. Repita até nada mudar. Branco: junte.
   BYTE: "Marquei A com B porque eles vão para C e D. Toque na regra que usei."
   PERGUNTA (toque): certo = 2. Marque o par que vai a um par marcado. ; errado = 1. Marque todo par final com não final. | 3. Repita até nada mudar. Branco: junte.
   se errar: "Não foi por ser final. Foi pelo destino."
   se acertar: "Isso. Regra dois, pelo destino."

13. TELA: # Receita ⏎ 1. Marque todo par final com não final. ⏎ 2. Marque o par que vai a um par marcado. ⏎ *3. Repita até nada mudar. Branco: junte.*
   BYTE: "Pare só quando uma rodada inteira não marcar nada. Uma marca nova pode provocar outra na rodada seguinte."

14. TELA: # AFD ⏎ P Q R ⏎ P, Q P, R Q, R  | setas: P --0--> Q, Q --0--> Q, P --1--> R, Q --1--> R, R --0,1--> R
   BYTE: "Outro autômato: P, Q e R, com R final."

15. TELA: # AFD ⏎ P Q *R* ⏎ P, Q P, R Q, R  | setas: P --0--> Q, Q --0--> Q, P --1--> R, Q --1--> R, R --0,1--> R
   BYTE: "Toque nos pares que a primeira regra marca."
   PERGUNTA (toque): certo = P, R | Q, R ; errado = P, Q
   se errar: "Procure os pares com o R, que é final."
   se acertar: "Isso. Só o R é final. Os outros dois, não."

16. TELA: # AFD ⏎ *P* *Q* R ⏎ P, Q P, R Q, R ⏎ juntar separar  | setas: P --0--> Q, Q --0--> Q, P --1--> R, Q --1--> R, R --0,1--> R
   BYTE: "P e Q, com zero, vão os dois para Q. Com um, para R. Juntar ou separar?"
   PERGUNTA (toque): certo = juntar ; errado = separar
   se errar: "Os destinos são iguais. Nenhum par marcado aparece."
   se acertar: "Isso. Os dois viram um estado só."

17. TELA: # AFD ⏎ P Q R ⏎ *P, Q* P, R Q, R  | setas: P --0--> Q, Q --0--> Q, P --1--> R, Q --1--> R, R --0,1--> R
   BYTE: "Ficaram dois estados. O autômato reconhece a mesma linguagem de antes."

18. TELA: # Erros comuns ⏎ A e B vão para estados diferentes com b. Então A e B são diferentes. ⏎ certo errado
   BYTE: "Basta os destinos serem estados diferentes para marcar o par? Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "E se os dois destinos forem equivalentes entre si?"
   se acertar: "Isso. Só marco o par se os destinos formam um par já marcado. Destinos diferentes, mas equivalentes, não separam ninguém."

19. TELA: # Erros comuns ⏎ Certo: Marque o par só quando os destinos formam um par já marcado. ⏎ *Errado: Parar depois da primeira rodada de destinos.* ⏎ *Certo: Repita até uma rodada inteira não marcar nenhum par.*
   BYTE: "Outro erro: parar cedo. Uma marca nova pode revelar outra diferença. Só acabe quando uma rodada passar em branco."

20. TELA: # Resumo ⏎ Estados equivalentes aceitam e rejeitam as mesmas continuações. ⏎ Marque final com não final. Depois, os pares que vão a pares marcados. ⏎ Pares sem marca no fim são equivalentes: junte.
   BYTE: "Resumindo: a minimização separa o que é diferente e junta o que sobra. Começa por final contra não final, propaga pelas transições e para quando nada muda."

#### PEGADINHA: "Juntar gêmeos de mentira" (6 cenas, ~33s)

1. TELA: # Juntar gêmeos de mentira ⏎ q0 q1 q2  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   BYTE: "Dois estados parecem gêmeos quando vão para os mesmos lugares."

2. TELA: # Juntar gêmeos de mentira ⏎ q0 *q1* *q2*  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   BYTE: "A pegadinha: um estado final e um não final nunca são gêmeos."

3. TELA: # Juntar gêmeos de mentira ⏎ q0 q1 q2 ⏎ q0, q1 q1, q2  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   BYTE: "Qual par dá para separar logo de cara? Toque nele."
   PERGUNTA (toque): certo = q1, q2 ; errado = q0, q1
   se errar: "Um deles aceita a palavra e o outro não."
   se acertar: "Isso. Final contra não final já é diferente."

4. TELA: # Juntar gêmeos de mentira ⏎ *q0* *q1* q2 ⏎ q0, q1 q1, q2  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   BYTE: "Depois, olhe para onde cada um vai com a mesma letra."

5. TELA: # Juntar gêmeos de mentira ⏎ q0 *q1* *q2* ⏎ q0, q1 q1, q2  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   BYTE: "Se um vai para um par já separado, os dois também se separam."

6. TELA: # Juntar gêmeos de mentira ⏎ q0 q1 q2 ⏎ q0, q1 q1, q2  | setas: q0 --a--> q1, q1 --a--> q2, q2 --a--> q1
   PROF: "Só junte o que nenhuma palavra consegue separar."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug dos Gêmeos — mentira: "Estados com o mesmo número de setas são iguais." / verdade: "Só são gêmeos se nenhuma cadeia separa os dois."
    · [V] Um estado final e um não final nunca são gêmeos. → Isso mesmo.
    · [V] A tabela de pares começa separando finais de não finais. → Isso mesmo.
    · [F] O autômato mínimo aceita outra linguagem. → A linguagem é a mesma, com menos estados.

#### FICHA DO ASSUNTO
Em uma frase: Minimizar um AFD é fundir os estados equivalentes (que nenhuma cadeia consegue distinguir): marca-se todo par final × não-final, propaga-se a marca pelas transições até estabilizar, e os pares que sobram sem marca viram um estado só, o AFD mínimo, único a menos de nomes.
Gancho: MPJ: Marca, Propaga, Junta. O morto também vota.
Termos-chave: equivalen, distingu, par, marca, final, morto, tabela
Erros comuns: Esquecer de completar o AFD com o estado morto antes de montar a tabela // Marcar um par com base num par de destino que ainda NÃO está marcado // Parar depois de uma rodada, sem repetir até nada mudar // Esquecer de remover estados inalcançáveis // Fazer a tabela e não desenhar o AFD mínimo (com inicial e finais) no fim
Flashcards:
    · P: Quando dois estados são equivalentes? → R: Quando nenhuma cadeia w leva um a final e o outro a não-final
    · P: O que a rodada 1 marca? → R: Todo par (final, não-final), a testemunha é a cadeia vazia
    · P: Regra das rodadas seguintes? → R: Marque {p,q} se, para alguma letra c, {δ(p,c), δ(q,c)} já está marcado
    · P: Quando parar? → R: Quando uma rodada inteira não marca nenhum par novo
    · P: O que fazer se falta transição? → R: Completar com o estado morto ∅ (não final, laço em tudo)
    · P: Quantas casas tem a tabela com n estados? → R: n(n−1)/2 (só a escadinha de baixo)
    · P: No AFD do livro para (a|b)*abb, quem se funde? → R: q0 e q2 (A e C): 5 estados viram 4
    · P: Método alternativo à tabela? → R: Refinar partições (Moore/Hopcroft): começa com {F, S−F} e divide grupos
    · P: O AFD mínimo é único? → R: Sim, a menos de renomear os estados (Myhill–Nerode)


## ASSUNTO LEX_FLEX: Lex/Flex: o arquivo .l
Pré-requisitos no jogo: Tokens, lexemas e padrões, Expressões regulares e autômatos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O Flex escreve o scanner» → Fazer com ajuda → Pegadinha «A ordem das regras no Flex» → Desafio
Jogos que treinam: Fatiador de Tokens (cortar e classificar), Oficina Flex (ordenar e escolher regras do .l)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug trocou as regras
Na tela: [a-z]+  antes de  "if"
BYTE: "Antes da aula: a palavra if vira qual token?"
Opções: ✔ID | IF | erro
Depois: "Vira ID. No empate, ganha a regra que vem primeiro."

#### AULA ANIMADA BÁSICA: "O Flex escreve o scanner" (18 cenas, ~136s) [1º exemplo começa na cena 3]

1. TELA: # Scanner à mão ⏎ if (isalpha(c)) { ⏎   while (isalnum(c)) ... ⏎ } else if (isdigit(c)) { ⏎   while (isdigit(c)) ... ⏎ } else if (c == '=') ...
   BYTE: "Escrito à mão, o scanner tem um teste para cada classe de token, e um autômato por trás de cada teste."

2. TELA: # Scanner à mão ⏎ if (isalpha(c)) { ⏎   while (isalnum(c)) ... ⏎ } else if (isdigit(c)) { ⏎   while (isdigit(c)) ... ⏎ } else if (c == '=') ...
   BYTE: "Qual linha começa o teste dos números? Toque nela."
   PERGUNTA (toque): certo = } else if (isdigit(c)) { ; errado = if (isalpha(c)) { | } else if (c == '=') ...
   se errar: "Procure o teste de dígito."
   se acertar: "Isso. Cada classe pede um teste, e a ordem fica por sua conta."

3. TELA: # Arquivo .l ⏎ "if"      return IF; ⏎ [a-z]+    return ID; ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */
   BYTE: "Fazer isso à mão cansa e dá erro. O Flex é um gerador: você escreve as regras, e ele gera o scanner em C."

4. TELA: # Arquivo .l ⏎ "if"      return IF; ⏎ *[a-z]+    return ID;* ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */
   BYTE: "No arquivo ponto l, cada linha tem um padrão, que é uma expressão regular, e uma ação em C. Casou o padrão, roda a ação."

5. TELA: # Arquivo .l ⏎ "if"      return IF; ⏎ [a-z]+    return ID; ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */ ⏎ if
   BYTE: "O texto if casa com a primeira e com a segunda regra, do mesmo tamanho. Quem ganha? Toque na regra."
   PERGUNTA (toque): certo = "if"      return IF; ; errado = [a-z]+    return ID;
   se errar: "Com tamanhos iguais, a ordem do arquivo decide."
   se acertar: "Isso. No empate, vence a regra de cima."

6. TELA: # Arquivo .l ⏎ "if"      return IF; ⏎ *[a-z]+    return ID;* ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */ ⏎ ifa
   BYTE: "E o texto ifa? A primeira regra casa só duas letras. A segunda casa as três. O casamento mais longo vence."

7. TELA: # Arquivo .l ⏎ "if"      return IF; ⏎ [a-z]+    return ID; ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */ ⏎ ifa ⏎ 1. maior casamento ⏎ 2. empate: a de cima
   BYTE: "Guarde duas regras: primeiro o maior casamento. Empatou, vale a de cima."

8. TELA: # Arquivo .l ⏎ *"if"      return IF;* ⏎ [a-z]+    return ID; ⏎ [0-9]+    return NUM; ⏎ [ \t\n]  ; /* ignora */ ⏎ ifa ⏎ 1. maior casamento ⏎ 2. empate: a de cima
   BYTE: "Por isso as palavras reservadas vêm antes da regra de identificador no arquivo."

9. TELA: # Receita ⏎ 1. Teste todas as regras no ponto atual. ⏎ 2. Vence o casamento mais longo. ⏎ 3. Empate no tamanho: vence a regra de cima. ⏎ 4. Rode a ação. Sem return, o texto some.
   BYTE: "Para cada pedaço da entrada, o scanner do Flex faz quatro passos."

10. TELA: # Receita ⏎ 1. Teste todas as regras no ponto atual. ⏎ 2. Vence o casamento mais longo. ⏎ 3. Empate no tamanho: vence a regra de cima. ⏎ 4. Rode a ação. Sem return, o texto some. ⏎ [ \t\n]  ; /* ignora */
   BYTE: "Os espaços não chegam ao parser. Qual passo explica isso? Toque."
   PERGUNTA (toque): certo = 4. Rode a ação. Sem return, o texto some. ; errado = 1. Teste todas as regras no ponto atual. | 2. Vence o casamento mais longo. | 3. Empate no tamanho: vence a regra de cima.
   se errar: "Olhe a ação da regra do espaço. Ela tem return?"
   se acertar: "Isso. A ação não devolve token, então o espaço some."

11. TELA: # Arquivo .l ⏎ [a-z]+    return ID; ⏎ "while"   return WHILE; ⏎ "="       return ATRIB; ⏎ "=="      return IGUAL; ⏎ [0-9]+    return NUM; ⏎ " "       ; ⏎ w h i l e x = = 1 0
   BYTE: "Outro arquivo de regras, com um erro escondido. Quem roda agora é você."

12. TELA: # Arquivo .l ⏎ [a-z]+    return ID; ⏎ "while"   return WHILE; ⏎ "="       return ATRIB; ⏎ "=="      return IGUAL; ⏎ [0-9]+    return NUM; ⏎ " "       ; ⏎ w h i l e x = = 1 0 ⏎ ID WHILE
   BYTE: "O scanner lê while. Qual token sai? Toque na resposta."
   PERGUNTA (toque): certo = ID ; errado = WHILE
   se errar: "As duas primeiras regras casam cinco letras. No empate, vale a de cima."
   se acertar: "Isso. Sai ID. A regra do while nunca ganha."

13. TELA: # Arquivo .l ⏎ [a-z]+    return ID; ⏎ "while"   return WHILE; ⏎ "="       return ATRIB; ⏎ "=="      return IGUAL; ⏎ [0-9]+    return NUM; ⏎ " "       ; ⏎ w h i l e x = = 1 0
   BYTE: "Agora o scanner chega aos dois iguais. Qual regra vence? Toque nela."
   PERGUNTA (toque): certo = "=="      return IGUAL; ; errado = [a-z]+    return ID; | "while"   return WHILE; | "="       return ATRIB; | [0-9]+    return NUM; | " "       ;
   se errar: "Uma regra casa um caractere. A outra casa dois."
   se acertar: "Isso. O mais longo vence, mesmo estando mais abaixo."

14. TELA: # Arquivo .l ⏎ [a-z]+    return ID; ⏎ "while"   return WHILE; ⏎ "="       return ATRIB; ⏎ "=="      return IGUAL; ⏎ [0-9]+    return NUM; ⏎ " "       ; ⏎ w h i l e x = = 1 0
   BYTE: "Para while virar WHILE, qual regra precisa subir para o topo? Toque."
   PERGUNTA (toque): certo = "while"   return WHILE; ; errado = [a-z]+    return ID; | "="       return ATRIB; | "=="      return IGUAL; | [0-9]+    return NUM; | " "       ;
   se errar: "A regra que perde o empate precisa ficar em cima."
   se acertar: "Isso. Palavra reservada vem antes da regra de nomes."

15. TELA: # Arquivo .l ⏎ *"while"   return WHILE;* ⏎ [a-z]+    return ID; ⏎ "="       return ATRIB; ⏎ "=="      return IGUAL; ⏎ [0-9]+    return NUM; ⏎ " "       ; ⏎ WHILE ID IGUAL NUM
   BYTE: "Com while no topo, saem quatro tokens: WHILE, ID, IGUAL e NUM."

16. TELA: # Erros comuns ⏎ No Flex, a regra que aparece primeiro no arquivo sempre vence. ⏎ certo errado
   BYTE: "Quem vence quando duas regras casam? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Lembre do ifa: qual regra ganhou, a de cima ou a mais longa?"
   se acertar: "Isso. Primeiro vale o casamento mais longo. A ordem do arquivo só desempata quando o tamanho é igual."

17. TELA: # Erros comuns ⏎ Certo: Primeiro o casamento mais longo. A ordem só desempata. ⏎ *Errado: Pôr a regra de identificador antes das palavras reservadas.* ⏎ *Certo: Palavra reservada primeiro. Senão while e if viram ID.*
   BYTE: "Outro erro: pôr a regra de identificador em cima. Aí if e while empatam no tamanho, perdem na ordem e viram nomes comuns."

18. TELA: # Resumo ⏎ Arquivo .l: padrão (expressão regular) e ação em C. ⏎ Vence o casamento mais longo; no empate, a regra de cima. ⏎ Ação sem return descarta o texto. É assim que os espaços somem.
   BYTE: "Resumindo: o Flex gera o scanner a partir de padrões e ações. O mais longo vence, a ordem desempata, e as palavras reservadas ficam em cima."

#### PEGADINHA: "A ordem das regras no Flex" (5 cenas, ~32s)

1. TELA: # Arquivo .l ⏎ [a-z]+   { return ID; } ⏎ "if"     { return IF; }
   BYTE: "Neste arquivo ponto l, o nome vem antes do if."

2. TELA: # Arquivo .l ⏎ *[a-z]+   { return ID; }* ⏎ *"if"     { return IF; }* ⏎ if
   BYTE: "Chega a palavra if. As duas regras casam duas letras. Deu empate."

3. TELA: # Arquivo .l ⏎ [a-z]+   { return ID; } ⏎ "if"     { return IF; } ⏎ if
   BYTE: "No empate, o Flex fica com a primeira regra do arquivo. Qual regra ganha aqui?"
   PERGUNTA (toque): certo = [a-z]+   { return ID; } ; errado = "if"     { return IF; }
   se errar: "Olhe qual linha vem primeiro."
   se acertar: "Isso. O if vira ID, e a regra de baixo nunca roda."

4. TELA: # Arquivo .l ⏎ [a-z]+   { return ID; } ⏎ "if"     { return IF; } ⏎ if ⏎ if vira ID
   PROF: "O Flex até avisa que a regra de baixo nunca casa. Quem lê o aviso acha o erro."

5. TELA: # Arquivo .l ⏎ "if"     { return IF; } ⏎ [a-z]+   { return ID; } ⏎ if ⏎ if vira IF
   BYTE: "O conserto é subir a palavra reservada. Reservada em cima, nome embaixo."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Palavra Comum — mentira: "if é um nome como outro qualquer." / verdade: "No empate de tamanho, ganha a regra que vem primeiro. A reservada precisa vir antes."
    · [V] No Flex, a regra que vem primeiro ganha o empate. → Isso mesmo.
    · [V] O maior casamento decide antes da ordem das regras. → Isso mesmo.
    · [F] yytext guarda a classe do token. → yytext guarda o lexema.

#### FICHA DO ASSUNTO
Em uma frase: O Lex/Flex lê um arquivo .l (definições %% regras 'padrão { ação }' %% código C) e gera lex.yy.c com a função yylex(), que devolve um token por chamada usando o maior casamento e, no empate, a regra listada primeiro.
Gancho: D-R-C, guloso mas educado: come o maior pedaço; no empate, respeita a fila. Palavra-chave é VIP: vem antes do ID.
Termos-chave: regra, padr, maior, primeir, yytext, yylex, return
Erros comuns: Pôr a regra de identificador antes das palavras-chave // Pôr a regra-coringa '.' antes das outras regras // Achar que a ordem das regras vence o tamanho do lexema // Confundir yytext (o texto do lexema) com yylval (o valor passado ao Bison) // Esquecer yywrap (ou %option noyywrap / -ll) e o gcc não linkar
Flashcards:
    · P: Quais são as 3 seções de um arquivo .l? → R: Definições %% regras %% código C
    · P: O que o comando lex gera? → R: O arquivo lex.yy.c com a função yylex()
    · P: Regra 1 de desempate do Lex? → R: O maior casamento (lexema mais longo) vence
    · P: Regra 2 de desempate do Lex? → R: Mesmo tamanho: vence a regra listada primeiro
    · P: 'iffy' com "if" antes de [a-z]+ vira o quê? → R: Um único ID (maior casamento)
    · P: O que guardam yytext e yyleng? → R: O texto do lexema e o seu tamanho
    · P: Ação sem return, como { }, faz o quê? → R: Descarta o lexema e yylex() continua
    · P: Como o valor de um número chega ao Bison? → R: Por yylval (ex.: yylval = atoi(yytext);)
    · P: Como compilar o scanner do TP? → R: lex scanner.l && gcc lex.yy.c
