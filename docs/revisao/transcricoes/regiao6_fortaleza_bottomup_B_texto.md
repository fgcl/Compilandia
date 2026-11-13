# Região 6: Fortaleza Bottom-Up — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO SHIFT_REDUCE: Empilha-reduz (shift-reduce)

#### ESTILO STORY (História / Analogia): "Montando Lego de baixo para cima"
- NARRADOR: Bem-vindo à Fortaleza Bottom-Up! Aqui ninguém prevê nada: a gente constrói de baixo para cima.
- PROF: Imagine montar um Lego seguindo o manual de trás pra frente. Você pega as pecinhas uma a uma (**empilhar**, shift). Quando as peças do topo formam um 'kit' do manual, você as encaixa num bloco maior (**reduzir**, reduce).
- CÓDIGO (O 'manual'. Repare: tem recursão à esquerda, e aqui ninguém se importa!):
```
E → E + T | T
T → T * F | F
F → ( E ) | id
```
- BYTE: Então eu empilho 'id' e já reduzo, porque F → id?
- PROF: Isso! O 'id' no topo vira F. E esse F vira T (T → F). Cada redução é um passo de derivação feito ao contrário.
- PREVEJA: Pilha: $ T. Próximo token: *. Reduzir T para E (E → T) ou empilhar o *?
    opções: Reduzir para E | ✔Empilhar o * | Aceitar | Tanto faz
    explicação: Se virasse E, teríamos 'E *', e nenhuma forma sentencial tem E seguido de *. O kit certo é T * F: empilhamos e esperamos. Saber QUANDO reduzir é a arte toda.
- TEXTO: O kit certo, no lugar certo, tem nome: **handle** (alça). Reduzir o handle é dar um passo para trás numa **derivação mais à direita**.
- WIDGET LR_TRACE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» entrada=«id * id» legenda: Assista à montagem: empilha, reduz, reduz, empilha, empilha, reduz, reduz, reduz, aceita.
- CAIXA [Não existem perguntas idiotas]: P: Quem me diz qual kit é o handle? R: Uma tabela construída com itens LR (as próximas salas da Fortaleza). Por enquanto, confie no manual e na intuição.
- PILHA: Aqui… eu… guardo… o PASSADO. O que você já… viu.
- REVELAR: Quais são as 4 ações de um analisador empilha-reduz?
    → Empilhar (shift), reduzir (reduce), aceitar (accept) e erro (error).
- CAIXA [No mundo real]: Yacc e Bison geram analisadores empilha-reduz (LALR). O PostgreSQL, por exemplo, usa Bison para analisar SQL.

#### ESTILO VISUAL (Passo a passo visual): "Empilha-reduz em câmera lenta"
- PROF: Vamos ver um analisador empilha-reduz completo, com estados e tudo.
- CÓDIGO:
```
E → E + T | T
T → T * F | F
F → ( E ) | id
```
- WIDGET LR_TRACE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» entrada=«id + id * id» legenda: Avance passo a passo. Repare quando ele reduz e quando ele espera.
- PASSOS (Momentos-chave):
    1) Empilha id e reduz F → id, T → F, E → T: o primeiro operando já virou E.
    2) Empilha + e o segundo id; reduz F → id e T → F.
    3) Agora o topo é E + T e vem '*': ele EMPILHA em vez de reduzir E → E + T. A multiplicação precisa ser montada antes.
    4) Monta T * F, reduz T → T * F e só então E → E + T.
    5) Pilha com E e entrada $: aceita.
- PREVEJA: Com 'E + T' no topo e '*' na entrada, por que NÃO reduzir E → E + T?
    opções: ✔Porque T * F ainda precisa ser montado (o * tem precedência) | Porque a pilha está cheia | Porque E + T não é produção | Porque é proibido reduzir com * na entrada
    explicação: Se reduzisse, ficaria E * …, que não existe na gramática. O analisador espera montar T * F primeiro: a precedência sai naturalmente da gramática.
- WIDGET PARSE_TREE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» entrada=«id + id * id» legenda: A árvore montada de baixo para cima: as folhas surgem primeiro, a raiz E por último.
- BYTE: Então o trace, lido de trás pra frente, é uma derivação?
- PROF: Isso! As reduções, lidas ao contrário, formam a **derivação mais à direita**. Bottom-up = derivação mais à direita de trás pra frente.
- REVELAR: Na entrada 'id * id', qual é a ÚLTIMA redução antes de aceitar?
    → E → T. A raiz da árvore é sempre a última peça encaixada.
- CAIXA [Poder do cérebro]: O analisador nunca precisa procurar o handle no MEIO da pilha: ele sempre aparece no topo. Por que isso facilita tanto a vida?
- REVELAR: Responda o Poder do Cérebro.
    → Porque basta olhar o topo da pilha (e o próximo token) para decidir. É isso que permite guiar as decisões com uma pilha simples e um autômato finito.

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: id * id"
- PROF: Exercício resolvido: análise empilha-reduz de id * id na gramática de expressões. Formato: Pilha | Entrada | Ação.
- CÓDIGO:
```
E → E + T | T
T → T * F | F
F → ( E ) | id
```
- PASSOS (Resolução):
    1) $ | id * id $ | empilhar
    2) $ id | * id $ | reduzir F → id
    3) $ F | * id $ | reduzir T → F
    4) $ T | * id $ | empilhar (não reduza E → T: * não pode seguir E)
    5) $ T * | id $ | empilhar
    6) $ T * id | $ | reduzir F → id
    7) $ T * F | $ | reduzir T → T * F
    8) $ T | $ | reduzir E → T
    9) $ E | $ | aceitar
- WIDGET LR_TRACE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» entrada=«id * id» legenda: Confira com o motor (ele mostra também os números dos estados).
- BYTE: No passo 4 eu teria reduzido pra E. Tô ferrado?
- PROF: De jeito nenhum: é O erro mais comum, e é exatamente por isso que existem as tabelas LR. A pista aqui: '*' não está em FOLLOW(E) = {+, ), $}.
- PROF: Agora o desbotado. Gramática S → S a | b, entrada b a a.
- CÓDIGO:
```
S → S a | b
```
- TEXTO: Eu começo: $ | b a a $ | empilhar. Depois $ b | a a $ | reduzir S → b. Depois $ S | a a $ | empilhar.
- REVELAR: Pilha $ S a, entrada a $. Ação?
    → Reduzir S → S a. A pilha vira $ S.
- REVELAR: Pilha $ S, entrada a $. E agora?
    → Empilhar a. Depois, com $ S a e entrada $, reduzir S → S a.
- REVELAR: E o final?
    → Pilha $ S e entrada $: aceitar.
- WIDGET LR_TRACE gramática=«S → S a | b» entrada=«b a a» legenda: Confira: a recursão à esquerda, proibida no Templo, aqui é bem-vinda.

#### ESTILO SOCRATIC (Perguntas guiadas): "Descobrindo o que é um handle"
- PROF: Perguntas para você descobrir sozinho o que é um handle.
- CÓDIGO (Exemplo clássico do Livro do Dragão, com a frase a b b c d e.):
```
S → a A B e
A → A b c | b
B → d
```
- PREVEJA: Pilha: $ a b. Entrada: b c d e $. O 'b' no topo casa com A → b. Reduzir?
    opções: ✔Sim | Não
    explicação: Sim: chegamos a a A b c d e, que leva a S. Aqui esse b é o handle.
- PREVEJA: Agora a pilha é $ a A b e a entrada c d e $. O 'b' no topo casa com A → b de novo. Reduzir?
    opções: Sim, b é sempre handle | ✔Não: o certo é empilhar c e reduzir A b c para A
    explicação: Reduzir daria a A A c d e, que não leva a S. O handle aqui é A b c. Nem toda ocorrência de um lado direito é um handle!
- BYTE: Então handle não é só 'achei um lado direito'…
- REVELAR: Tente definir handle com suas palavras.
    → É uma ocorrência de um lado direito que, reduzida, nos leva um passo para trás numa derivação mais à DIREITA até o símbolo inicial: a redução 'certa'.
- PREVEJA: Em que posição da pilha o handle aparece?
    opções: ✔Sempre no topo | Sempre no fundo | Em qualquer lugar | No meio
    explicação: Resultado importante: numa análise empilha-reduz o handle sempre aparece no topo da pilha, nunca escondido no meio.
- PREVEJA: Se em algum momento o analisador não sabe se deve empilhar ou reduzir, temos um…
    opções: ✔Conflito empilha-reduz (shift/reduce) | Conflito reduz-reduz | Erro léxico | Aceite
    explicação: Conflito shift/reduce. Se ele não souber QUAL redução fazer, é reduce/reduce.
- WIDGET LR_TRACE gramática=«S → a A B e ⏎ A → A b c | b ⏎ B → d» entrada=«a b b c d e» legenda: Veja o analisador acertando o handle em cada passo.
- REVELAR: Escreva a sequência de reduções da análise de a b b c d e.
    → A → b, A → A b c, B → d, S → a A B e. Lida ao contrário, é a derivação mais à direita: S ⇒ a A B e ⇒ a A d e ⇒ a A b c d e ⇒ a b b c d e.
- PROF: Você reconstruiu sozinho a ideia central do bottom-up. Achar handles é o que o resto da Fortaleza ensina a automatizar.

#### ESTILO FORMAL (Definição de prova): "Empilha-reduz: definições de prova"
- PROF: Definições de prova, sem enrolação.
- TEXTO: **Análise empilha-reduz**: análise ascendente (bottom-up) com uma pilha de símbolos gramaticais e um buffer de entrada. Configuração inicial: pilha $, entrada w$. Sucesso: pilha $ S, entrada $.
- PASSOS (As quatro ações):
    1) **Empilhar (shift)**: move o próximo símbolo da entrada para o topo da pilha.
    2) **Reduzir (reduce)**: o topo contém um lado direito β (o handle); desempilha β e empilha A, para A → β.
    3) **Aceitar (accept)**: anuncia o término com sucesso.
    4) **Erro (error)**: detecta um erro de sintaxe e chama a rotina de recuperação.
- TEXTO: **Handle.** Se S ⇒*rm α A w ⇒rm α β w, então a produção A → β na posição logo após α é um handle de α β w. (w só tem terminais; 'rm' = mais à direita.)
- TEXTO: **Prefixo viável**: prefixo de uma forma sentencial direita que não ultrapassa o fim do handle. O conteúdo da pilha é sempre um prefixo viável.
- PREVEJA: A sequência de reduções de um analisador empilha-reduz, lida de trás para frente, corresponde a:
    opções: Uma derivação mais à esquerda | ✔Uma derivação mais à direita | Uma árvore ambígua | A tabela LL(1)
    explicação: Bottom-up = derivação mais à direita (rightmost) ao contrário. É o R de LR.
- PASSOS (Conflitos):
    1) **shift/reduce**: não dá para decidir entre empilhar e reduzir (ex.: o else pendurado).
    2) **reduce/reduce**: não dá para decidir qual redução fazer.
    3) Gramáticas com conflito para um método não pertencem à classe dele (ex.: não são SLR(1)). Gramáticas ambíguas sempre geram conflito.
- WIDGET LR_TRACE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» entrada=«( id )» legenda: Siga as definições na análise de ( id ).
- CAIXA [Dica de prova]: A resposta-padrão é a tabela Pilha | Entrada | Ação, com a produção usada em cada 'reduzir'. E saiba de cor: o handle está sempre no topo; LR = Left-to-right, Rightmost derivation (ao contrário).
- REVELAR: Defina handle em uma frase.
    → Uma subcadeia que casa com o lado direito de uma produção e cuja redução representa um passo de volta numa derivação mais à direita.
- CAIXA [Conversa ao pé da lareira]: LL vs LR ao pé da lareira. LL: 'Eu escolho a produção olhando só o primeiro token dela.' LR: 'Eu vejo o lado direito INTEIRO e mais um token antes de decidir. Sou paciente, e por isso aceito mais gramáticas, inclusive as recursivas à esquerda.' LL: 'Mas eu sou fácil de escrever à mão!' LR: 'Por isso inventaram o Bison pra mim.'

#### ESTILO MNEMONIC (Truque de memória): "Empilha até ver a alça"
- NARRADOR: Portão da Fortaleza Bottom-Up: uma esteira de fábrica leva caixinhas (tokens) até um montador.
- CAIXA [Gancho de memória]: **'Empilha até ver a alça; achou a alça no topo, reduz.'** Handle = alça: o pedaço por onde você pega e dobra tudo para o lado esquerdo da regra.
- CAIXA [Gancho de memória]: **LR = 'Lê da esquerda, Reconstrói a mais à direita… de trás pra frente.'** Bottom-up = rightmost ao contrário.
- CAIXA [Gancho de memória]: **Reduzir = 'tira o lado direito, põe o lado esquerdo'.** Como trocar um punhado de moedinhas por uma nota.
- PASSOS (Top-down × bottom-up):
    1) Top-down (Templo): começa pela raiz S e prevê. Bottom-up (Fortaleza): começa pelas folhas e monta.
    2) LL: derivação mais à esquerda, na ordem. LR: derivação mais à direita, invertida.
    3) Pilha LL: o que falta (futuro). Pilha LR: o que já foi visto (passado).
    4) Recursão à esquerda: proibida no LL, tranquila no LR.
- BYTE: E 'shift' é empilhar ou é deslocar?
- PROF: Os dois! O token se desloca da entrada para a pilha. No português do Livro do Dragão: **empilhar** (shift) e **reduzir** (reduce).
- PREVEJA: Qual ação consome um token da entrada?
    opções: Reduzir | ✔Empilhar | Aceitar | Todas
    explicação: Só o shift consome entrada. Reduzir só mexe na pilha (troca β por A).
- WIDGET LR_TRACE gramática=«S → a S b | ε» entrada=«a a b b» legenda: Treine o gancho: onde está a alça? Repare na redução S → ε, que empilha S sem desempilhar nada.
- REVELAR: Recite os três ganchos.
    → 'Empilha até ver a alça; achou no topo, reduz.' 'LR = rightmost de trás pra frente.' 'Reduzir = tira o direito, põe o esquerdo.'


## ASSUNTO LR0_ITEMS: Itens LR(0), closure e goto

#### ESTILO STORY (História / Analogia): "O marcador de página"
- NARRADOR: Torre central da Fortaleza Bottom-Up. Cada andar é um 'estado' cheio de marcadores de página.
- PROF: Um **item LR(0)** é uma produção com um ponto • no meio: um marcador de onde você está na leitura. C → c • C quer dizer: 'já vi o c, agora espero um C'.
- CÓDIGO (Nosso livro de receitas. Gera cadeias como d d, c d d, c c d c d…):
```
S → C C
C → c C | d
```
- BYTE: Tipo a barra de progresso do download?
- PROF: Isso! Ponto no começo (C → • c C): 0%. Ponto no fim (C → c C •): 100%, hora de **reduzir**.
- PREVEJA: Quantos itens LR(0) a produção S → C C gera?
    opções: 1 | 2 | ✔3 | 4
    explicação: S → • C C, S → C • C e S → C C •. Uma produção com n símbolos dá n + 1 itens.
- PROF: Agora a mágica do **fechamento (closure)**. Se eu espero um C (ponto antes de C), então posso estar no começo de qualquer receita de C. Eu 'convido' C → • c C e C → • d.
- CAIXA [Gancho de memória]: Fechamento = **convite em cascata**: o não-terminal logo depois do ponto convida as próprias produções, com o ponto no começo. Os convidados também convidam os seus.
- PROF: E o **desvio (goto)**: andar o ponto por cima de um símbolo. goto(I, c) = pegue os itens com • antes de c, passe o ponto para depois do c e feche de novo.
- WIDGET LR0_AUTOMATON gramática=«S → C C ⏎ C → c C | d» legenda: O prédio inteiro: 7 andares (estados I0 a I6) e as escadas (transições) entre eles.
- REVELAR: No estado I0, quais itens existem? (Dica: comece por S' → • S e vá convidando.)
    → S' → • S, S → • C C, C → • c C, C → • d. O S convidou S → • C C; o C convidou as duas produções de C.
- CAIXA [Não existem perguntas idiotas]: P: Que S' é esse? R: É a **gramática aumentada**: criamos S' → S para ter um único item de aceitação, S' → S •.
- BYTE: Marcador, convite, passo. Itens, closure, goto. Gostei!

#### ESTILO VISUAL (Passo a passo visual): "Explorando o autômato LR(0)"
- PROF: Vamos explorar o autômato LR(0) do jeito que se explora um mapa.
- CÓDIGO:
```
S → C C
C → c C | d
```
- WIDGET LR0_AUTOMATON gramática=«S → C C ⏎ C → c C | d» legenda: Toque em cada estado para ver seus itens. As setas são os desvios (goto).
- PASSOS (Roteiro de exploração):
    1) I0: o estado inicial, fechamento de { S' → • S }.
    2) I0 --S--> I1 = { S' → S • }: o estado de aceitação.
    3) I0 --C--> I2 = { S → C • C, C → • c C, C → • d }: o ponto passou pelo primeiro C, e o segundo C convidou suas produções.
    4) I0 --c--> I3 = { C → c • C, C → • c C, C → • d }. Repare em I3 --c--> I3: um laço!
    5) I0 --d--> I4 = { C → d • }: item completo → redução.
    6) I5 = { S → C C • } e I6 = { C → c C • }: mais dois estados de redução.
- PREVEJA: De I2, lendo d, vamos para qual estado?
    opções: Um estado novo | ✔I4 | I3 | I6
    explicação: goto(I2, d) = { C → d • } = I4. Se o conjunto de itens já existe, reaproveitamos o estado: por isso o autômato é finito.
- BYTE: Por que de I3, lendo c, volta pro I3?
- PROF: Porque goto(I3, c) dá C → c • C de novo, e o fechamento repete os mesmos convidados. Mesmo conjunto = mesmo estado. É o autômato 'contando' os c's sem precisar de memória extra.
- WIDGET LR0_AUTOMATON gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» legenda: Agora uma gramática maior: 12 estados (I0 a I11). Procure o estado que tem E → T • e T → T • * F juntos.
- REVELAR: Que problema esse estado (E → T • junto com T → T • * F) causaria se reduzíssemos sempre que um item está completo?
    → Conflito empilha-reduz no '*': o item completo manda reduzir E → T e o outro manda empilhar *. O LR(0) puro não resolve; o SLR(1) usa FOLLOW(E) = {+, ), $} para decidir (próxima sala).
- CAIXA [Poder do cérebro]: O autômato LR(0) reconhece exatamente os **prefixos viáveis** da gramática. Olhe os caminhos a partir de I0: cada caminho soletra algo que pode estar na pilha.

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: a coleção canônica de S → C C"
- PROF: Exercício de prova: construa a coleção canônica LR(0) de S → C C, C → c C | d.
- CÓDIGO:
```
S → C C
C → c C | d
```
- PASSOS (Resolução):
    1) Passo 0, aumente a gramática: 0) S' → S, 1) S → C C, 2) C → c C, 3) C → d.
    2) I0 = closure({ S' → • S }) = { S' → • S, S → • C C, C → • c C, C → • d }.
    3) goto(I0, S) = { S' → S • } = I1.
    4) goto(I0, C) = closure({ S → C • C }) = { S → C • C, C → • c C, C → • d } = I2.
    5) goto(I0, c) = closure({ C → c • C }) = { C → c • C, C → • c C, C → • d } = I3.
    6) goto(I0, d) = { C → d • } = I4.
    7) goto(I2, C) = { S → C C • } = I5. goto(I2, c) = I3 e goto(I2, d) = I4: já existem!
- WIDGET LR0_AUTOMATON gramática=«S → C C ⏎ C → c C | d» legenda: Confira os estados I0 a I5 com o motor.
- BYTE: Falta o I3, né?
- PROF: Falta! Essa parte é sua. Lembre: goto = passar o ponto e depois fechar.
- REVELAR: goto(I3, C) = ?
    → { C → c C • }: um estado novo, I6.
- REVELAR: goto(I3, c) = ?
    → closure({ C → c • C }) = { C → c • C, C → • c C, C → • d }: o próprio I3.
- REVELAR: goto(I3, d) = ?
    → { C → d • } = I4.
- REVELAR: Quantos estados ao todo, e quais são de redução?
    → 7 estados (I0 a I6). Itens completos: I1 (aceita), I4 (C → d), I5 (S → C C), I6 (C → c C). Nenhum estado mistura item completo com outro item: a gramática é LR(0)!
- CAIXA [Dica de prova]: Numere as produções da gramática aumentada, desenhe cada estado como uma caixinha com o núcleo (kernel) separado dos itens do fechamento e, antes de criar um estado novo, verifique se aquele conjunto já existe.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que itens, closure e goto?"
- PROF: Vamos redescobrir itens, closure e goto só com perguntas.
- PREVEJA: Num analisador empilha-reduz, o que precisamos saber a cada momento?
    opções: Só o próximo token | ✔Em que ponto das produções candidatas estamos | A árvore inteira | O FIRST de tudo
    explicação: Para saber se o handle já está completo no topo, precisamos saber 'onde estamos' em cada produção candidata. Isso é um item.
- PREVEJA: O item A → X Y • Z significa:
    opções: Já reduzi A | ✔Já vi X Y; espero algo derivado de Z | Espero X Y Z | Erro
    explicação: O ponto separa o passado (o que está na pilha) do futuro (o que vem na entrada).
- PREVEJA: Se espero um Z (item A → X Y • Z) e Z → w | v, o que mais posso estar esperando?
    opções: Nada | ✔Z → • w e Z → • v | Z → w • e Z → v • | A → • X Y Z
    explicação: Esperar um Z é esperar o começo de alguma produção de Z. Isso é o fechamento.
- PREVEJA: Se o item é A → X • b Y, com b terminal, quantos itens o fechamento acrescenta por causa dele?
    opções: Todos os de b | ✔Nenhum: terminal não tem produções | Um | Dois
    explicação: O fechamento só convida quando há um NÃO-terminal logo depois do ponto.
- PREVEJA: Estou no conjunto de itens I e empilho o símbolo X. O novo conjunto é…
    opções: I de novo | ✔Os itens com • antes de X, com o ponto avançado, e depois fechados | Todos os itens que contêm X | Vazio
    explicação: Isso é goto(I, X): avança o ponto sobre X e aplica o fechamento.
- REVELAR: Por que o número de estados é finito?
    → Há um número finito de itens (produções × posições do ponto), logo um número finito de conjuntos de itens. Conjuntos repetidos viram o mesmo estado.
- PREVEJA: Para que serve a produção S' → S da gramática aumentada?
    opções: Enfeite | ✔Ter um item de aceitação único: S' → S • | Evitar recursão à esquerda | Calcular FOLLOW
    explicação: Quando o analisador chegaria a reduzir para S' (item S' → S • com $ na entrada), ele aceita.
- WIDGET LR0_AUTOMATON gramática=«S → S a | b» legenda: Teste: S → S a | b. Antes de olhar, quantos itens terá I0?
- REVELAR: Quais são os itens de I0?
    → 3: S' → • S, S → • S a, S → • b. O S convidou suas duas produções; o S de S → • S a já foi convidado, então nada novo.
- PROF: Recursão à esquerda no closure? Sem drama: o conjunto não cresce para sempre, porque item repetido não entra de novo.

#### ESTILO FORMAL (Definição de prova): "Itens, CLOSURE, GOTO e coleção canônica (versão prova)"
- PROF: Definições formais, como no Livro do Dragão.
- TEXTO: **Item LR(0)**: produção de G com um ponto em alguma posição do lado direito. A → X Y Z gera A → • X Y Z, A → X • Y Z, A → X Y • Z e A → X Y Z •. A produção A → ε gera só A → •.
- TEXTO: **Gramática aumentada** G': G com um novo símbolo inicial S' e a produção S' → S. O analisador aceita quando reduziria por S' → S.
- CÓDIGO (Fechamento de um conjunto de itens.):
```
CLOSURE(I):
  J := I
  repita
    para cada item A → α • B β em J:
      para cada produção B → γ de G':
        se B → • γ não está em J:
          adicione B → • γ a J
  até nenhum item ser adicionado
  retorne J
```
- CÓDIGO (Desvio: avança o ponto sobre X e fecha.):
```
GOTO(I, X) = CLOSURE({ A → α X • β  |  A → α • X β ∈ I })
```
- CÓDIGO (Coleção canônica de conjuntos de itens LR(0).):
```
ITENS(G'):
  C := { CLOSURE({ S' → • S }) }
  repita
    para cada conjunto I em C:
      para cada símbolo gramatical X:
        se GOTO(I, X) ≠ ∅ e GOTO(I, X) ∉ C:
          adicione GOTO(I, X) a C
  até nenhum conjunto ser adicionado
```
- TEXTO: **Itens de núcleo (kernel)**: S' → • S e todos os itens com o ponto fora da extremidade esquerda. Os demais são **não-núcleo** e podem ser recalculados pelo fechamento.
- PREVEJA: Qual destes é item de núcleo?
    opções: C → • c C | S → • C C | ✔C → c • C | C → • d
    explicação: C → c • C tem o ponto depois do início. Os outros têm o ponto na extremidade esquerda (e não são S' → • S): vieram do fechamento.
- WIDGET LR0_AUTOMATON gramática=«S → C C ⏎ C → c C | d» legenda: Identifique, em cada estado, quais itens são de núcleo.
- CAIXA [Dica de prova]: Pergunta típica: 'calcule CLOSURE({…})' ou 'GOTO(I, X)'. Não esqueça: GOTO = avançar o ponto **e depois** fechar. E A → ε gera só o item A → •, que já nasce completo.
- REVELAR: Qual a relação entre o autômato LR(0) e os prefixos viáveis?
    → O autômato reconhece exatamente os prefixos viáveis: γ é viável se e somente se existe o caminho GOTO(I0, γ) a partir do estado inicial.

#### ESTILO MNEMONIC (Truque de memória): "Você está aqui •"
- NARRADOR: Torre da Fortaleza Bottom-Up: num dos andares há um mapa de shopping com um ponto vermelho, **VOCÊ ESTÁ AQUI**.
- CAIXA [Gancho de memória]: **O ponto • é o 'você está aqui'.** À esquerda dele: o que já está na pilha (passado). À direita: o que falta ler (futuro).
- CAIXA [Gancho de memória]: **CLOSURE = 'convida a família'.** Ponto antes de um não-terminal B? Chama todas as produções de B, com o ponto no começo. A família de B convida as próprias famílias.
- CAIXA [Gancho de memória]: **GOTO = 'gol': chuta o ponto um passo pra frente e depois comemora com a família (closure).**
- PASSOS (Confusões clássicas):
    1) O closure só adiciona itens com o ponto NO COMEÇO (B → • γ).
    2) O goto avança o ponto só nos itens com • antes de X; os outros ficam de fora.
    3) Esquecer de fechar depois do goto.
    4) Criar estado repetido: conjunto igual = mesmo estado.
    5) Item completo (ponto no fim) = hora de reduzir; não sai goto dele.
- PREVEJA: Byte calculou goto(I0, c) = { C → c • C } e parou. O que faltou?
    opções: Nada | ✔Fechar: C → • c C e C → • d | Adicionar S → • C C | Remover o ponto
    explicação: Goto = avançar + fechar. Como o ponto ficou antes de C, a família de C precisa ser convidada.
- CAIXA [Conversa ao pé da lareira]: Item e Estado ao pé da lareira. Item: 'Eu sou só um marcador numa receita.' Estado: 'E eu sou o grupo de marcadores que valem ao mesmo tempo: todas as receitas que podem estar em andamento agora.' Item: 'Então você é a minha turma?' Estado: 'Sou. E o fechamento é como eu chamo a turma toda.'
- WIDGET LR0_AUTOMATON gramática=«S → a S b | ε» legenda: Treino rápido em S → a S b | ε: ache o estado em que o 'você está aqui' aparece antes do S e conte os convidados.
- REVELAR: Recite os três ganchos.
    → Ponto = 'você está aqui'. Closure = 'convida a família'. Goto = 'chuta o ponto um passo e comemora com a família'.


## ASSUNTO SLR_TABLE: Tabela SLR(1) e conflitos

#### ESTILO STORY (História / Analogia): "A açaiteria que segue o quadro"
- NARRADOR: Fortaleza Bottom-Up, ala oeste. Na parede, um quadro gigante cheio de células: s5, r2, acc, vazio, vazio, s7...
- BYTE: Parece uma planilha de Excel que deu errado. Pra que serve isso?
- PROF: É o cérebro de um analisador LR: a tabela ACTION/GOTO. Vou te explicar com uma açaiteria.
- TEXTO: A atendente monta copos de açaí. Ela **coloca ingredientes no copo** um a um (empilha). Quando o copo tem uma combinação completa, açaí + granola + banana,, ela **troca tudo por um nome**: 'Combo Clássico' (reduz).
- TEXTO: Ela não pensa: olha **onde está no pedido** (o estado) e **o próximo pedido do cliente** (o token), e obedece o quadro da parede.
- PASSOS (O mapa da analogia):
    1) Momento do pedido (o que já está no copo) → **estado** do autômato LR(0)
    2) Próximo pedido do cliente → **token de lookahead**
    3) 'Põe no copo e vai para o momento 7' → **shift 7** (s7)
    4) 'Troca açaí+granola+banana por Clássico' → **reduce** por Clássico → açaí granola banana
    5) 'Com um Clássico na mão, vá para o momento 4' → **GOTO**[estado, Clássico] = 4
    6) 'O que pode vir DEPOIS de um Clássico' → **FOLLOW(Clássico)**
- PILHA: Eu sou... a pilha. Só vejo... meu topo. O quadro diz... o resto. Gosto de quadros.
- PREVEJA: O copo tem açaí + granola + banana (um Clássico completo). O próximo pedido é 'leite condensado'. Na regra SLR, quando ela fecha o Clássico?
    opções: Sempre: copo completo = fecha na hora | ✔Só se 'leite condensado' puder vir DEPOIS de um Clássico (está no FOLLOW) | Nunca: ela só empilha
    explicação: O SLR só coloca 'reduz' nas colunas do FOLLOW. Se leite condensado nunca vem depois de um Clássico, fechar agora seria um erro garantido.
- CAIXA [Cuidado!]: Se a mesma célula do quadro mandar 'põe no copo' E 'fecha o combo', a atendente trava. Isso é um **conflito empilhar/reduzir**. Duas ordens de 'fecha' diferentes = **reduzir/reduzir**.
- WIDGET SLR_TABLE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: O quadro de verdade para E → E + T | T ..., nenhuma célula com duas ordens: a gramática é SLR(1).
- REVELAR: Na analogia, o que faz o papel do FOLLOW?
    → A lista do que pode vir logo depois de um combo. A atendente só fecha o combo (reduz) se o próximo pedido está nessa lista.
- CAIXA [Não existem perguntas idiotas]: Por que 'Simple'? Porque o SLR usa o autômato LR(0) mais simples possível e decide reduções com o FOLLOW, que você já sabe calcular. Simples, mas um pouco míope, e isso vai dar assunto.

#### ESTILO VISUAL (Passo a passo visual): "Do autômato à tabela, célula por célula"
- PROF: Hoje a gente constrói a tabela olhando o autômato LR(0). Primeiro, os estados.
- WIDGET LR0_AUTOMATON gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Coleção canônica LR(0) da gramática de expressões (aumentada com E' → E).
- PASSOS (Quatro regras para encher a tabela):
    1) Seta com **terminal** a de Ii para Ij → ACTION[i, a] = **sj**
    2) Seta com **não-terminal** A de Ii para Ij → GOTO[i, A] = **j**
    3) Item completo **A → α •** em Ii → **rN** em ACTION[i, a] para cada a em **FOLLOW(A)**
    4) Item **E' → E •** em Ii → ACTION[i, $] = **acc**
- WIDGET FIRST_FOLLOW gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Vamos precisar do FOLLOW: FOLLOW(E) = { +, ), $ } e FOLLOW(T) = FOLLOW(F) = { +, *, ), $ }.
- WIDGET SLR_TABLE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: A tabela pronta. Procure a linha do estado 2.
- BYTE: Peraí! O estado 2 tem E → T • (quer reduzir) e T → T • * F (quer empilhar). Isso não dá briga?
- PREVEJA: No estado 2, com o próximo token sendo '*', o que a tabela SLR manda?
    opções: Reduzir por E → T | ✔Empilhar '*' (s7) | Conflito: as duas coisas
    explicação: '*' não está em FOLLOW(E) = { +, ), $ }, então o reduce nem é escrito nessa coluna. Sobra só o shift. Briga evitada pelo FOLLOW!
- WIDGET LR_TRACE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id * id» legenda: Siga a pilha: no estado 9, com '*', ele empilha; só depois reduz T → T * F.
- PILHA: Viu? Eu guardei E + T... e esperei o '*'. Por isso... a multiplicação liga mais forte.
- PROF: Agora uma gramática onde o FOLLOW não salva ninguém.
- WIDGET SLR_TABLE gramática=«S -> L = R | R ⏎ L -> * R | id ⏎ R -> L» legenda: S → L = R | R; L → * R | id; R → L. Ache a célula vermelha: estado 2, coluna '='.
- REVELAR: Por que '=' entrou em FOLLOW(R)?
    → L → * R joga FOLLOW(L) dentro de FOLLOW(R), e S → L = R coloca '=' em FOLLOW(L). Resultado: o SLR escreve r5 (R → L) em [2, =], junto com s6 de S → L • = R.
- CAIXA [Poder do cérebro]: Essa gramática NÃO é ambígua. O conflito é culpa do SLR ser míope: nenhuma forma sentencial começa com 'R ='. Guarde essa: é a porta de entrada do LALR.

#### ESTILO WORKED (Exemplo resolvido): "Preenchendo linhas da tabela com calma"
- PROF: Exercício clássico de prova: preencher linhas da tabela SLR da gramática de expressões. Vou fazer duas; você faz as outras.
- CÓDIGO (Produções numeradas (o número vira o rN)):
```
(1) E → E + T     (2) E → T
(3) T → T * F     (4) T → F
(5) F → ( E )     (6) F → id
```
- CÓDIGO (Três estados do autômato LR(0)):
```
I1: E' → E •      E → E • + T          (+ → I6)
I2: E → T •       T → T • * F          (* → I7)
I9: E → E + T •   T → T • * F          (* → I7)
```
- TEXTO: FOLLOW(E) = { +, ), $ }    FOLLOW(T) = { +, *, ), $ }
- PASSOS (Linha do estado 1):
    1) E' → E • está completo e é o item aumentado → ACTION[1, $] = **acc**
    2) E → E • + T tem terminal depois do ponto, e há seta + → I6 → ACTION[1, +] = **s6**
    3) Todo o resto da linha fica vazio = **erro**
- PASSOS (Linha do estado 2):
    1) E → T • completo → **r2** nas colunas de FOLLOW(E): +, ) e $
    2) T → T • * F com seta * → I7 → ACTION[2, *] = **s7**
    3) Alguma célula com duas ações? Não: '*' ∉ FOLLOW(E). Linha limpa!
- WIDGET SLR_TABLE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Confira as linhas 1 e 2 na tabela gerada pelo motor.
- BYTE: Deixa eu tentar! Eu consigo, eu consigo...
- REVELAR: Sua vez: estado 9 = { E → E + T • ; T → T • * F } com seta * → I7. Preencha a linha.
    → r1 em +, ) e $ (FOLLOW(E)); s7 em *. Sem conflito, pelo mesmo motivo do estado 2.
- REVELAR: Agora o estado 10 = { T → T * F • }.
    → r3 em +, *, ) e $: todas as colunas de FOLLOW(T).
- PROF: Último, sem rodinhas: a gramática dos ponteiros.
- REVELAR: S → L = R | R; L → * R | id; R → L. Estado 2 = { S → L • = R ; R → L • }, seta = → I6, FOLLOW(R) = { =, $ }. Preencha a linha.
    → ACTION[2, =] = { s6, r5 } → CONFLITO empilhar/reduzir! ACTION[2, $] = r5. Conclusão: a gramática não é SLR(1).
- WIDGET SLR_TABLE gramática=«S -> L = R | R ⏎ L -> * R | id ⏎ R -> L» legenda: A célula [2, =] com duas ações confirma sua resposta.
- CAIXA [Dica de prova]: Calcule e escreva FOLLOW de todos os não-terminais ANTES de abrir a tabela. Metade dos erros de prova é reduce na coluna errada.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que o FOLLOW decide as reduções?"
- PROF: Hoje eu só pergunto. Responda na sua cabeça antes de tocar, errar aqui é treino, não nota.
- REVELAR: Um estado tem o item A → α •. O que esse ponto no fim está dizendo?
    → Que o topo da pilha contém α inteiro: temos um candidato a handle. Dá para reduzir por A → α.
- REVELAR: O LR(0) puro reduz em QUALQUER próximo token. E se o mesmo estado tiver também B → β • a γ?
    → No token a, ele quer reduzir E empilhar: conflito. O LR(0) é cego para o futuro.
- REVELAR: Se reduzimos para A, que tokens podem aparecer logo depois numa derivação válida?
    → Só os de FOLLOW(A). Se o próximo token não está lá, reduzir para A é erro garantido.
- BYTE: Então o SLR é o LR(0) com um filtro de FOLLOW nas reduções!
- PROF: Exatamente. Mesmo autômato, mesmos estados, só reduções mais educadas.
- PREVEJA: E se o token a estiver em FOLLOW(A) e o estado também tiver B → β • a γ?
    opções: O SLR escolhe shift sozinho | ✔Conflito empilhar/reduzir: a gramática não é SLR(1) | O SLR reduz pela regra mais curta
    explicação: A tabela SLR não escolhe nada: se duas ações caem na mesma célula, a gramática simplesmente não é SLR(1). (Quem escolhe 'shift' por padrão é o Bison, outra história.)
- REVELAR: E dois itens completos A → α • e B → β • no mesmo estado?
    → Se FOLLOW(A) ∩ FOLLOW(B) ≠ ∅, conflito reduzir/reduzir naquelas colunas.
- WIDGET SLR_TABLE gramática=«E -> E + E | E * E | ( E ) | id» legenda: E → E + E | E * E | ( E ) | id: conflitos nos estados 7 e 8, colunas + e *.
- REVELAR: Toda gramática ambígua gera conflito na tabela SLR?
    → Sim: nenhuma gramática ambígua é LR(k). Mas a volta é falsa, conflito no SLR não prova ambiguidade (lembra de S → L = R | R?).
- REVELAR: Por que o FOLLOW é grosseiro demais?
    → Ele junta tudo que pode seguir A em QUALQUER lugar da gramática, sem saber de qual caminho você veio. Recuperar esse contexto é o trabalho do LR(1).
- CAIXA [Poder do cérebro]: Pense: como você carregaria, dentro de cada item, a informação 'o que pode vir depois DESTE A, neste caminho'? Segure essa ideia até a aula de LALR.

#### ESTILO FORMAL (Definição de prova): "Construção da tabela SLR(1): algoritmo"
- TEXTO: Seja G' a gramática aumentada (S' → S) e C = { I0, ..., In } a coleção canônica de conjuntos de itens LR(0) de G'. O estado i corresponde a Ii; o inicial é closure({ [S' → • S] }).
- CÓDIGO (Algoritmo SLR(1) (Livro do Dragão, Alg. 4.46)):
```
para cada estado Ii:
  para cada item [A → α • a β] em Ii, com a terminal:
      se GOTO(Ii, a) = Ij:  ACTION[i, a] := shift j
  para cada item [A → α •] em Ii, com A ≠ S':
      para todo a em FOLLOW(A):  ACTION[i, a] := reduce A → α
  se [S' → S •] ∈ Ii:  ACTION[i, $] := accept
  para cada não-terminal A:
      se GOTO(Ii, A) = Ij:  GOTO[i, A] := j
células não preenchidas := erro
```
- TEXTO: **Definição.** G é SLR(1) se e somente se nenhuma célula de ACTION recebe mais de uma ação.
- PASSOS (Os dois tipos de conflito):
    1) **Empilhar/reduzir:** Ii contém [A → α • a β] e [B → γ •] com a ∈ FOLLOW(B)
    2) **Reduzir/reduzir:** Ii contém [A → α •] e [B → β •] com FOLLOW(A) ∩ FOLLOW(B) ≠ ∅
- CÓDIGO (Algoritmo do analisador LR (o mesmo para SLR, LALR e LR(1), só a tabela muda)):
```
empilha o estado 0;  a := primeiro token
repita:
  s := estado no topo
  caso ACTION[s, a]:
    shift t:       empilha t;  a := próximo token
    reduce A → β:  desempilha |β| estados;  t := topo
                   empilha GOTO[t, A];  emite A → β
    accept:        pare com sucesso
    erro:          chame a recuperação de erro
```
- WIDGET SLR_TABLE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Tabela SLR(1) da gramática de expressões: sem conflitos.
- PREVEJA: Qual afirmação é VERDADEIRA (sobre classes de gramáticas)?
    opções: Toda gramática SLR(1) é LL(1) | ✔Toda gramática SLR(1) é LALR(1) | Toda gramática LALR(1) é SLR(1)
    explicação: Hierarquia: LR(0) ⊂ SLR(1) ⊂ LALR(1) ⊂ LR(1). LL(1) e SLR(1) são incomparáveis (E → E + T é SLR mas não LL).
- REVELAR: Dimensões da tabela?
    → Uma linha por estado LR(0). ACTION: uma coluna por terminal + $. GOTO: uma coluna por não-terminal (sem S').
- CAIXA [Cuidado!]: Transição com terminal vira 'shift' em ACTION; transição com não-terminal vira número em GOTO. Não existe 'shift' na GOTO nem número puro na ACTION.
- CAIXA [Dica de prova]: Para justificar um conflito, cite: estado, terminal, os dois itens e o FOLLOW. Ex.: 'I2, coluna =: s6 por S → L • = R e r5 por R → L •, pois = ∈ FOLLOW(R)'.

#### ESTILO MNEMONIC (Truque de memória): "O porteiro FOLLOW da torre SLR"
- NARRADOR: Palácio da Memória: Fortaleza Bottom-Up, torre do SLR. Na porta, um porteiro de prancheta chamado FOLLOW.
- CAIXA [Gancho de memória]: SLR = **'Só Libera Redução'** se o próximo token estiver na prancheta do FOLLOW.
- CÓDIGO (Cola de bolso):
```
• antes de TERMINAL         →  sN   (empilha e vai para N)
• no FIM do item            →  rN   (só nas colunas do FOLLOW)
S' → S •                    →  acc  (só na coluna $)
seta com NÃO-terminal       →  GOTO (número puro)
```
- CAIXA [Gancho de memória]: Ponto na frente do terminal: **'tem estrada, segue'** (shift). Ponto no fim: **'chegou, fecha'** (reduce).
- CAIXA [Gancho de memória]: **E/R** = um item quer andar e outro quer fechar (uma porta, dois comandos). **R/R** = dois itens querem fechar ao mesmo tempo (dois nomes para o mesmo pacote).
- LEXI: Eu corto tokens; o porteiro só deixa reduzir quem ele conhece. Equipe!
- PREVEJA: No estado { S → L • = R ; R → L • } com '=' ∈ FOLLOW(R), o porteiro...
    opções: Deixa só empilhar | ✔Libera a redução E a seta mostra empilhar: conflito E/R | Não deixa nada
    explicação: O porteiro olha só a prancheta global (FOLLOW). Ele não sabe que, ali, um R nunca é seguido de '='. Míope!
- WIDGET SLR_TABLE gramática=«S -> S a | b» legenda: S → S a | b: tabela pequena e sem conflitos. Recursão à esquerda não assusta o LR!
- CAIXA [Conversa ao pé da lareira]: LR(0): 'Eu reduzo sempre que vejo um item completo, sem olhar ninguém!' SLR: 'E por isso briga com todo mundo. Eu pergunto ao FOLLOW antes.' LALR (lá do fundo): 'Vocês dois ainda vão me pedir ajuda...'
- REVELAR: Recite as quatro regras da tabela SLR.
    → Terminal após o ponto → shift. Não-terminal → GOTO. Item completo → reduce nas colunas do FOLLOW. S' → S • → accept em $.
- CAIXA [Gancho de memória]: Contraste: no LL(1), o FOLLOW só entra nas produções que derivam ε. No SLR, o FOLLOW decide TODA redução.


## ASSUNTO LALR: LR(1) e LALR(1)

#### ESTILO STORY (História / Analogia): "Ônibus com destino na testa"
- BYTE: O SLR travou naquela gramática dos ponteiros. Joga fora e pronto?
- PROF: Não! Vamos dar ao analisador uma informação extra. Pense numa empresa de ônibus.
- TEXTO: Cada **item** é um passageiro numa **linha** (a produção com o ponto). O SLR desce passageiros em qualquer ponto que **alguma** linha da cidade use, é o FOLLOW global.
- TEXTO: O **LR(1)** cola na testa de cada passageiro o **ponto exato** onde ele vai descer: o **lookahead**. Item LR(1) = [linha, destino], ex.: [R → L •, $].
- PASSOS (O mapa da analogia):
    1) Linha do ônibus → **núcleo** do item (A → α • β)
    2) Ponto de descida na testa → **lookahead** (um terminal)
    3) Descer do ônibus → **reduzir**, só quando o próximo token é o lookahead
    4) Frota enorme, um ônibus por combinação linha+destinos → **LR(1) canônico** (muitos estados)
    5) Fundir ônibus com as mesmas linhas, somando destinos → **LALR(1)**
- PILHA: Mais ônibus... mais estados... mais memória. A empresa... reclamou.
- TEXTO: Então a empresa funde ônibus que fazem exatamente as **mesmas linhas** (mesmo núcleo) e junta as listas de destinos. A frota volta ao tamanho do LR(0)/SLR!
- PREVEJA: Ônibus 1: passageiro A desce em d, B desce em e. Ônibus 2 (mesmas linhas): A desce em e, B desce em d. Fundindo os dois, o que acontece no ponto d?
    opções: Nada, só A desce | ✔A e B querem descer: confusão (reduzir/reduzir) | Os dois continuam no ônibus
    explicação: Fundidos, A e B têm d e e na testa. No ponto d, dois 'reduce' disputam a célula: conflito reduzir/reduzir que o LR(1) não tinha.
- CAIXA [Poder do cérebro]: A fusão nunca cria conflito empilhar/reduzir novo: quem 'segue viagem' (shift) depende só da linha, e as linhas são as mesmas. Só a parte 'descer' (lookahead) é somada.
- WIDGET SLR_TABLE gramática=«S -> L = R | R ⏎ L -> * R | id ⏎ R -> L» legenda: O SLR trava em [2, =]. No LR(1), o estado 2 tem [R → L •, $]: só desce em $, então '=' fica só com o shift.
- REVELAR: Na analogia, o que é o 'núcleo' de um estado?
    → O conjunto de linhas (itens sem o lookahead). Estados LR(1) com o mesmo núcleo viram um estado só no LALR.
- CAIXA [No mundo real]: O Yacc e o Bison geram, por padrão, tabelas LALR(1): quase o poder do LR(1) com o tamanho do SLR. O Bison moderno também oferece IELR e LR(1) canônico via %define lr.type.

#### ESTILO VISUAL (Passo a passo visual): "Vendo a fusão de núcleos acontecer"
- PROF: Mesma gramática dos ponteiros, agora com lookaheads. Primeiro, o esqueleto LR(0).
- WIDGET LR0_AUTOMATON gramática=«S -> L = R | R ⏎ L -> * R | id ⏎ R -> L» legenda: 10 estados LR(0). O LALR terá exatamente estes 10 núcleos.
- CÓDIGO (Itens LR(1): [núcleo, lookahead]. '=/$' abrevia dois itens.):
```
I0: S' → • S, $      S → • L = R, $      S → • R, $
    L → • * R, =/$    L → • id, =/$       R → • L, $
I2: S → L • = R, $    R → L •, $          ← reduz só em $!
```
- BYTE: No estado 2, R → L • só tem $ na testa. Então em '=' é só empilhar!
- PROF: Isso. O lookahead do item veio do contexto: R → • L foi criado a partir de S → • R, $, e depois de S só vem $.
- CÓDIGO (Pares com o mesmo núcleo (numeração do motor do jogo)):
```
LR(1) canônico: 14 estados
  I4 ~ I11   (L → * • R, ...)
  I5 ~ I12   (L → id •, ...)
  I7 ~ I9    (R → L •, ...)
  I8 ~ I13   (L → * R •, ...)
LALR(1): funde cada par → 10 estados
```
- WIDGET BISON gramática=«%token ID ⏎ %% ⏎ s : l '=' r ⏎   | r ⏎   ; ⏎ l : '*' r ⏎   | ID ⏎   ; ⏎ r : l ⏎   ; ⏎ » entrada=«*x=y» legenda: O Bison (LALR) aceita '*x = y' sem nenhum conflito, onde o SLR travava.
- PROF: Agora o lado sombrio da fusão.
- CÓDIGO (Gramática LR(1) mas não LALR(1)):
```
S → a A d | b B d | a B e | b A e
A → c        B → c

LR(1):  I6 = { [A → c •, d]  [B → c •, e] }   ← depois de 'a c'
        I9 = { [B → c •, d]  [A → c •, e] }   ← depois de 'b c'
LALR:   I6+I9 = { [A → c •, d/e]  [B → c •, d/e] }  ← R/R em d e em e!
```
- WIDGET BISON gramática=«%token a b c d e ⏎ %% ⏎ s : a x d ⏎   | b y d ⏎   | a y e ⏎   | b x e ⏎   ; ⏎ x : c ; ⏎ y : c ; ⏎ » entrada=«a c d» legenda: O Bison avisa 2 conflitos reduzir/reduzir e escolhe a regra que aparece primeiro (x : c).
- PREVEJA: Com essa escolha padrão do Bison, o que acontece com a frase válida 'a c e'?
    opções: É aceita | ✔Dá syntax error, mesmo sendo válida | O Bison pergunta ao usuário
    explicação: Em 'e', ele reduz c para x (primeira regra), mas 'a x e' não existe, só 'a y e'. A fusão fez o parser perder uma frase da linguagem.
- CAIXA [Poder do cérebro]: Note: o LR(1) separava 'a c' de 'b c' em estados diferentes. A fusão misturou os contextos, é exatamente aí que o LALR é mais fraco que o LR(1).

#### ESTILO WORKED (Exemplo resolvido): "Closure LR(1) e fusão, passo a passo"
- PROF: Vamos calcular I0 da gramática S → L = R | R; L → * R | id; R → L, com lookaheads.
- PASSOS (closure({ [S' → • S, $] })):
    1) Começa com [S' → • S, $]
    2) Ponto antes de S; o que vem depois de S é ε, então o lookahead é FIRST($) = { $ } → [S → • L = R, $] e [S → • R, $]
    3) De [S → • L = R, $]: depois de L vem '= R', FIRST = { = } → [L → • * R, =] e [L → • id, =]
    4) De [S → • R, $]: depois de R vem nada → FIRST($) → [R → • L, $]
    5) De [R → • L, $]: → [L → • * R, $] e [L → • id, $]
    6) Nada novo? Pronto. Compactando: [L → • * R, =/$] e [L → • id, =/$]
- CAIXA [Cuidado!]: A regra é FIRST(β a), onde β é o que vem DEPOIS do não-terminal no item e a é o lookahead do item. Não é FOLLOW!
- PASSOS (GOTO(I0, L) = I2):
    1) Avança o ponto sobre L: [S → L • = R, $] e [R → L •, $]
    2) Closure não acrescenta nada (ponto antes de terminal ou no fim)
    3) Tabela: '=' → shift; R → L • reduz SÓ em $ → sem conflito!
- BYTE: Agora eu! Agora eu!
- REVELAR: Calcule GOTO(I0, *).
    → [L → * • R, =/$] e, fechando: [R → • L, =/$], [L → • * R, =/$], [L → • id, =/$]. Os lookaheads de L → * R são herdados por R → • L porque R está no fim.
- REVELAR: No LR(1) há [L → id •, =/$] (depois de 'id' no início) e [L → id •, $] (depois de 'L = id'). Fundem no LALR? Dá conflito?
    → Fundem (mesmo núcleo L → id •) virando [L → id •, =/$]. Um único item completo → sem conflito.
- REVELAR: Na gramática S → a A d | b B d | a B e | b A e; A → c; B → c, quais estados LR(1) se fundem e o que acontece?
    → { [A → c •, d], [B → c •, e] } e { [B → c •, d], [A → c •, e] } têm o mesmo núcleo. Fundidos: A → c • e B → c • ambos em d e em e → reduzir/reduzir.
- WIDGET SLR_TABLE gramática=«S -> a A d | b B d | a B e | b A e ⏎ A -> c ⏎ B -> c» legenda: Curiosidade: o SLR também tem esse R/R (FOLLOW(A) = FOLLOW(B) = { d, e }). Só o LR(1) canônico escapa.
- CAIXA [Dica de prova]: Questão típica: 'mostre que G é LR(1) mas não LALR(1)'. Receita: ache dois estados LR(1) com o mesmo núcleo cujos itens completos trocam de lookahead, funda-os e aponte a célula com dois reduces.

#### ESTILO SOCRATIC (Perguntas guiadas): "E se o item soubesse o futuro?"
- PROF: Lembra da pergunta da última aula? Como carregar, dentro do item, 'o que vem depois deste A, neste caminho'?
- REVELAR: Qual a ideia mais simples?
    → Grudar um terminal no item: [A → α • β, a] significa 'quando α β estiver completo, só reduza se o próximo token for a'.
- REVELAR: De onde vem o lookahead dos itens novos na closure?
    → Do item que os criou: em [A → α • B β, a], cada B → • γ ganha lookaheads FIRST(β a). É o contexto local, não o FOLLOW global.
- REVELAR: Por que isso resolve S → L = R | R?
    → No estado 2, R → L • veio de S → • R, $, então só reduz em $. O '=' fica só com o shift.
- REVELAR: Qual o preço?
    → Muitos estados: o mesmo núcleo aparece repetido com lookaheads diferentes. Para linguagens reais, o LR(1) canônico pode ter milhares de estados a mais.
- BYTE: E se a gente juntasse os estados que só diferem no lookahead?
- PROF: Você acabou de inventar o LALR(1). Os nomes que você não sabia já estavam na sua cabeça.
- PREVEJA: A fusão de estados com mesmo núcleo pode criar um conflito EMPILHAR/REDUZIR novo?
    opções: Sim, sempre | ✔Não: se surgisse depois, ele já existia em um dos estados originais | Só se a gramática for ambígua
    explicação: Shifts dependem só do núcleo. Se [A → α •, a] e [B → β • a γ] estão no fundido, algum estado original tinha [A → α •, a] e, por ter o mesmo núcleo, também [B → β • a γ, ...]. O conflito já estava lá.
- REVELAR: E reduzir/reduzir novo?
    → Pode! Dois estados sem conflito, com A e B completos trocando lookaheads (d/e e e/d), viram um estado com dois reduces em d e em e.
- REVELAR: Então, por que o Bison usa LALR e não LR(1)?
    → Porque o LALR tem o número de estados do SLR e quase sempre basta para linguagens de programação. O raro R/R da fusão vale a economia.
- CAIXA [Conversa ao pé da lareira]: SLR: 'Eu uso o FOLLOW e pronto.' LALR: 'Eu uso lookaheads de verdade, só que fundidos, mais esperto que você e do mesmo tamanho.' LR(1): 'E eu nunca fundo nada. Mais forte que os dois... e bem mais gordo.'

#### ESTILO FORMAL (Definição de prova): "Itens LR(1), closure e fusão: definições"
- TEXTO: **Item LR(1):** par [A → α • β, a] com A → α β ∈ P e a ∈ T ∪ { $ }. O primeiro componente é o **núcleo** (core); a é o **lookahead**. O lookahead só tem efeito quando β = ε.
- CÓDIGO (Construção da coleção canônica LR(1)):
```
CLOSURE(I):
  repita
    para cada [A → α • B β, a] em I
      para cada produção B → γ
        para cada b em FIRST(β a)
          acrescente [B → • γ, b] a I
  até não mudar
  retorne I

GOTO(I, X) := CLOSURE({ [A → α X • β, a] | [A → α • X β, a] ∈ I })

estado inicial := CLOSURE({ [S' → • S, $] })
```
- TEXTO: **Tabela LR(1):** igual à SLR, exceto que [A → α •, a] gera reduce **apenas** em ACTION[i, a] (não em todo o FOLLOW(A)).
- CÓDIGO (Construção do LALR(1) por fusão):
```
LALR(1):
  1. construa a coleção LR(1) C = { I0, ..., In }
  2. agrupe os conjuntos com o mesmo núcleo;
     substitua cada grupo pela UNIÃO dos seus itens
  3. monte ACTION como no LR(1); se houver conflito,
     a gramática não é LALR(1)
  4. GOTO(J, X) := estado que contém GOTO(Ik, X)
     (bem definido: mesmos núcleos → mesmos destinos)
```
- PASSOS (Propriedades que caem em prova):
    1) Nº de estados LALR(1) = nº de estados LR(0) = nº de estados SLR(1)
    2) Fusão nunca introduz conflito empilhar/reduzir; pode introduzir reduzir/reduzir
    3) Gramáticas: SLR(1) ⊂ LALR(1) ⊂ LR(1); nenhuma gramática ambígua é LR(1)
    4) Lookaheads LALR ⊆ FOLLOW: por isso todo SLR(1) é LALR(1)
- PREVEJA: Em [A → α • B β, a], quais lookaheads ganham os itens de B?
    opções: FOLLOW(B) | ✔FIRST(β a) | FIRST(B)
    explicação: FIRST(β a): se β não deriva ε, o a nem aparece; se deriva, o lookahead do pai 'atravessa'. FOLLOW(B) seria voltar ao SLR.
- WIDGET LR0_AUTOMATON gramática=«S -> a A d | b B d | a B e | b A e ⏎ A -> c ⏎ B -> c» legenda: Os núcleos LR(0) de S → a A d | b B d | a B e | b A e. O estado com A → c • e B → c • é o fundido no LALR.
- CAIXA [Dica de prova]: Escreva itens LR(1) agrupados por núcleo, ex. [L → • id, =/$]. É mais rápido e deixa a fusão visível. Mas lembre: formalmente são dois itens.
- CAIXA [Cuidado!]: Não confunda 'LALR = Look-Ahead LR' com 'LR com lookahead de 2 tokens'. O (1) continua: um token de lookahead.

#### ESTILO MNEMONIC (Truque de memória): "O alfaiate que funde gêmeos"
- NARRADOR: Palácio da Memória: o andar mais alto da Fortaleza Bottom-Up. Um alfaiate costura gêmeos idênticos num só, somando as roupas dos dois.
- CAIXA [Gancho de memória]: **Mesmo esqueleto, roupas somadas.** Estados com o mesmo núcleo (esqueleto) viram um; os lookaheads (roupas) são unidos.
- CAIXA [Gancho de memória]: Óculos: **LR(0)** = sem óculos. **SLR** = binóculo embaçado (FOLLOW global). **LR(1)** = lupa em cada item. **LALR** = lupa, mas costurada em pares.
- CAIXA [Gancho de memória]: **'Fusão só faz Reduce duplo'**: a fusão LALR nunca cria E/R novo; só R/R.
- CÓDIGO (Duas linhas para tatuar):
```
força:    LR(0) < SLR(1) < LALR(1) < LR(1)
tamanho:  LR(0) = SLR(1) = LALR(1) < LR(1)
```
- BYTE: Então o LALR é o 'custo-benefício' da família?
- PROF: Isso! Por isso o Yacc e o Bison escolheram ele como padrão.
- PREVEJA: O alfaiate fundiu dois gêmeos sem brigas. O resultado tem um conflito empilhar/reduzir. Isso é possível?
    opções: Sim, a fusão criou | ✔Não: se tem E/R, um dos gêmeos já tinha | Só em gramática ambígua
    explicação: Shift depende só do esqueleto. Se o fundido tem E/R, o gêmeo que trouxe aquele reduce já tinha o mesmo shift.
- WIDGET BISON gramática=«%token ID ⏎ %% ⏎ s : l '=' r ⏎   | r ⏎   ; ⏎ l : '*' r ⏎   | ID ⏎   ; ⏎ r : l ⏎   ; ⏎ » entrada=«x=*y» legenda: O bisão (LALR) engole a gramática dos ponteiros sem reclamar.
- REVELAR: Qual o exemplo-gancho de 'LALR mas não SLR'? E de 'LR(1) mas não LALR'?
    → S → L = R | R; L → * R | id; R → L. E: S → a A d | b B d | a B e | b A e com A → c, B → c.
- CAIXA [Conversa ao pé da lareira]: SLR vs LALR, ao pé da lareira. SLR: 'Eu sou simples, todo mundo me entende.' LALR: 'E eu sou do seu tamanho, mas sei de onde cada item veio. Na gramática dos ponteiros, você travou e eu não.'
