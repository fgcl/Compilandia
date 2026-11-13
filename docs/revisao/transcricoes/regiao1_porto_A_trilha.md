# Região 1: Porto da Partida — Onde o código-fonte embarca
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO PHASES: Fases de um compilador
Pré-requisitos no jogo: nenhum
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A linha de montagem» → Prática: você pilota «A câmera entra na máquina» → Pegadinha «Quem pega este erro?» → Desafio
Jogos que treinam: Esteira das Fases (ordenar e encaixar), Detetive de Erros (classificar quem pega cada erro)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug embaralhou as fases
Na tela: léxica  ?  semântica
BYTE: "Antes da aula: qual fase vem no meio?"
Opções: ✔sintática | otimização | geração
Depois: "A sintática. Os tokens viram árvore antes de checar o sentido."

#### AULA ANIMADA BÁSICA: "A linha de montagem" (22 cenas, ~177s) [1º exemplo começa na cena 4]

1. TELA: # Para que serve? ⏎ conta = preco + 2 lw  add  sw ⏎ seu código processador
   BYTE: "Você escreve conta igual a preço mais dois. O processador não entende isso: ele só executa instruções simples."

2. TELA: # Para que serve? ⏎ conta = preco + 2 lw  add  sw ⏎ seu código processador
   BYTE: "Qual dos dois o processador entende? Toque nele."
   PERGUNTA (toque): certo = lw  add  sw ; errado = conta = preco + 2
   se errar: "Ele só lê instruções curtas, como carregar, somar e guardar."
   se acertar: "Isso. Código de máquina. O compilador é o programa que traduz o seu código para aquilo."

3. TELA: # Para que serve? ⏎ conta = preco + 2 ? lw  add  sw ⏎ seu código processador
   BYTE: "Traduzir tudo de uma vez seria difícil de fazer e de conferir. Então o trabalho é dividido."

4. TELA: # Seis fases ⏎ Léxica ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "O compilador trabalha em seis fases, uma depois da outra. O que sai de uma fase é a entrada da seguinte."

5. TELA: # Seis fases ⏎ Léxica x = a + 2 ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Entra o seu código-fonte. Para a máquina, ele ainda é só uma fila de caracteres."

6. TELA: # Seis fases ⏎ *Léxica* x  =  a  +  2 ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "A análise léxica junta os caracteres em tokens: nomes, números, operadores. Espaços e comentários ficam para trás."

7. TELA: # Seis fases ⏎ Léxica ⏎ *Sintática* árvore ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "A análise sintática confere se os tokens formam frases válidas da linguagem e monta a árvore sintática."

8. TELA: # Seis fases ⏎ Léxica ⏎ Sintática ⏎ *Semântica* árvore com tipos ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "A análise semântica confere o sentido: se a variável foi declarada e se os tipos combinam."

9. TELA: # Seis fases ⏎ Léxica ⏎ int x = "oi"; ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Em int x igual a oi, entre aspas, o texto não cabe num inteiro. Qual fase reclama? Toque nela."
   PERGUNTA (toque): certo = Semântica ; errado = Léxica | Sintática | Intermediário | Otimização | Geração
   se errar: "A escrita está certa e a frase tem forma de comando. O problema é o sentido."
   se acertar: "Isso. A frase está bem formada, mas o tipo não combina. É erro semântico."

10. TELA: # Seis fases ⏎ Léxica ⏎ Sintática ⏎ Semântica ⏎ *Intermediário* t1 = a + 2 ⏎ Otimização ⏎ Geração
   BYTE: "As três primeiras fases são a análise. Agora começa a síntese. O código intermediário escreve uma conta por linha, sem depender da máquina."

11. TELA: # Seis fases ⏎ Léxica ⏎ Sintática ⏎ Semântica ⏎ Intermediário t1 = a + 2 ⏎ *Otimização* ⏎ *Geração* lw  add  sw
   BYTE: "A otimização melhora esse código sem mudar o resultado. A geração escreve o código de máquina final."

12. TELA: # O que sai de cada fase ⏎ Léxica → tokens ⏎ Sintática → árvore ⏎ Semântica → árvore com tipos ⏎ Intermediário → uma conta por linha ⏎ Otimização → menos contas ⏎ Geração → código de máquina
   BYTE: "A receita é o que sai de cada fase. Guarde a ordem, porque ela cai em prova."

13. TELA: # O que sai de cada fase ⏎ *Léxica → tokens* ⏎ *Sintática → árvore* ⏎ Semântica → árvore com tipos ⏎ Intermediário → uma conta por linha ⏎ Otimização → menos contas ⏎ Geração → código de máquina
   BYTE: "Cada fase pega um tipo de erro. Caractere que não forma token é erro léxico. Token fora de ordem é erro sintático."

14. TELA: # O que sai de cada fase ⏎ Léxica → tokens ⏎ Sintática → árvore ⏎ *Semântica → árvore com tipos* ⏎ Intermediário → uma conta por linha ⏎ Otimização → menos contas ⏎ Geração → código de máquina
   BYTE: "Frase bem formada sem sentido, como tipo trocado ou variável não declarada, é erro semântico."

15. TELA: # Outro código ⏎ while (i < 10) i = i + 1; ⏎ Léxica Sintática ⏎ Semântica Intermediário ⏎ Otimização Geração
   BYTE: "Agora este while passa pelas mesmas fases."

16. TELA: # Outro código ⏎ while (i < 10) i = i + 1; ⏎ Léxica Sintática ⏎ Semântica Intermediário ⏎ Otimização Geração
   BYTE: "Quem corta o while em tokens? Toque na fase."
   PERGUNTA (toque): certo = Léxica ; errado = Sintática | Semântica | Intermediário | Otimização | Geração
   se errar: "É a primeira fase. Ela só olha as letras."
   se acertar: "Isso. A léxica corta e dá nome a cada pedaço."

17. TELA: # Outro código ⏎ while (i < 10 i = i + 1; ⏎ Léxica Sintática ⏎ Semântica Intermediário ⏎ Otimização Geração
   BYTE: "Se faltar o fecha parêntese, qual fase reclama?"
   PERGUNTA (toque): certo = Sintática ; errado = Léxica | Semântica | Intermediário | Otimização | Geração
   se errar: "As letras estão certas. O que falta é a forma do comando."
   se acertar: "Isso. Parêntese sem par quebra a árvore. É a sintática."

18. TELA: # Outro código ⏎ while (i < 10) i = i + 1; ⏎ Léxica Sintática ⏎ Semântica Intermediário ⏎ Otimização Geração
   BYTE: "Em qual fase o laço vira desvios, com uma conta por linha?"
   PERGUNTA (toque): certo = Intermediário ; errado = Léxica | Sintática | Semântica | Otimização | Geração
   se errar: "Vem depois da semântica e antes da otimização."
   se acertar: "Isso. O código intermediário escreve o laço com desvios."

19. TELA: # Outro código ⏎ while (i < 10) i = i + 1; ⏎ Léxica Sintática ⏎ Semântica Intermediário ⏎ Otimização Geração
   BYTE: "Seis fases em ordem: três de análise, três de síntese. O mapa do jogo segue esse mesmo caminho."

20. TELA: # Erros comuns ⏎ Variável usada sem declarar é erro sintático. ⏎ certo errado
   BYTE: "Em que fase cai este erro? Um colega respondeu assim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "A gramática aceita o comando. O problema é o que o nome significa."
   se acertar: "Isso. A sintática só olha a forma. Saber se o nome foi declarado é trabalho da análise semântica."

21. TELA: # Erros comuns ⏎ Certo: Variável não declarada é erro semântico: a forma está certa. ⏎ *Errado: O compilador executa o programa.* ⏎ *Certo: O compilador só traduz. Quem executa é o processador, ou um interpretador.*
   BYTE: "Outro erro comum: achar que o compilador roda o programa. Ele só traduz. O interpretador é que executa direto, sem gerar o arquivo."

22. TELA: # Resumo ⏎ Análise: léxica → tokens; sintática → árvore; semântica → árvore com tipos. ⏎ Síntese: código intermediário, otimização e geração de código. ⏎ Erro léxico: caractere. Sintático: forma. Semântico: sentido.
   BYTE: "Resumindo: o compilador traduz em seis fases. Três analisam o código-fonte, três constroem o código de máquina. Cada fase pega um tipo de erro."

#### PRATICA: "A câmera entra na máquina" (6 cenas, ~33s)

1. TELA: Léxica ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Esta é a linha de montagem inteira. Vamos entrar na primeira máquina."

2. TELA: x > = 1 ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Dentro da fase léxica, a linha chega letra por letra."

3. TELA: x > = 1 ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Dois destes caracteres formam um token só. Toque nos dois."
   PERGUNTA (toque): certo = > | = ; errado = x | 1
   se errar: "Procure o operador de comparação."
   se acertar: "Isso. Maior ou igual é um token só."

4. TELA: Léxica ⏎ *Sintática* ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Os tokens saem daqui e seguem para a próxima máquina."

5. TELA: >= ⏎ x 1 ⏎ Intermediário ⏎ Otimização ⏎ Geração
   BYTE: "Dentro da sintática, os mesmos tokens viram uma árvore."

6. TELA: Léxica ⏎ Sintática ⏎ Semântica ⏎ Intermediário ⏎ Otimização ⏎ Geração
   PROF: "Cada máquina recebe o trabalho da anterior e entrega para a próxima."

#### PEGADINHA: "Quem pega este erro?" (5 cenas, ~36s)

1. TELA: # Quem pega? ⏎ léxico sintático semântico
   BYTE: "Três erros parecidos caem na prova. Cada um é pego por uma fase diferente."

2. TELA: # Quem pega? ⏎ *int x@ = 2;* ⏎ léxico sintático semântico
   BYTE: "Primeiro: int x arroba igual a dois. Esse caractere não existe na linguagem. Quem reclama?"
   PERGUNTA (toque): certo = léxico ; errado = sintático | semântico
   se errar: "Nenhum token começa com arroba. Quem monta os tokens?"
   se acertar: "Isso. O scanner para antes de qualquer outra fase."

3. TELA: # Quem pega? ⏎ *x = 2  y = 3;* ⏎ léxico sintático semântico
   BYTE: "Agora falta o ponto e vírgula. Os tokens estão certos. Quem reclama?"
   PERGUNTA (toque): certo = sintático ; errado = léxico | semântico
   se errar: "Toda palavra existe. O que falta é uma peça na frase."
   se acertar: "Sintática. A forma do comando está errada."

4. TELA: # Quem pega? ⏎ *x = y + 1;* ⏎ léxico sintático semântico
   BYTE: "Por último, y mais um, com y nunca declarado. A frase tem forma certa. Quem reclama?"
   PERGUNTA (toque): certo = semântico ; errado = léxico | sintático
   se errar: "O problema não é a forma. É o nome y, que não existe."
   se acertar: "Semântica. Só ela consulta a tabela de símbolos."

5. TELA: # Quem pega? ⏎ x = y + 1; ⏎ léxico sintático semântico
   PROF: "Na prova, pergunte primeiro: o erro está na letra, na forma ou no sentido?"

#### CLIPE 30s: "O que um compilador faz" (2 cenas, ~13s)

1. TELA: # Compilador ⏎ x = 1 + 2; ⏎ 01101000
   BYTE: "Um compilador traduz o seu código para outra língua, que a máquina roda."

2. TELA: # Compilador ⏎ *x = 1 + 2;* ⏎ 01101000
   BYTE: "Ele confere tudo antes. Se achar erro, ele não gera nada."

#### CLIPE 30s: "Compilador ou interpretador" (2 cenas, ~11s)

1. TELA: # Tudo de uma vez ⏎ linha 1 linha 2 linha 3
   BYTE: "O compilador traduz tudo antes e entrega um programa pronto."

2. TELA: # Uma de cada vez ⏎ *linha 1* linha 2 linha 3
   BYTE: "O interpretador lê e executa na hora, linha por linha."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Fase Única — mentira: "O compilador faz tudo de uma vez só." / verdade: "São fases em sequência. A saída de uma é a entrada da outra."
    · [V] A análise léxica entrega tokens para a sintática. → Isso mesmo.
    · [F] O erro de tipo é pego na fase léxica. → Tipo é assunto da semântica.
    · [F] A otimização vem antes da análise sintática. → Só dá para otimizar depois de entender o código.
- Bug do Erro Trocado — mentira: "Todo erro é erro de sintaxe." / verdade: "Cada fase pega um tipo de erro."
    · [V] Variável não declarada é erro semântico. → Isso mesmo.
    · [V] Faltar ponto e vírgula é erro sintático. → Isso mesmo.
    · [F] Um @ perdido no código é erro semântico. → O scanner pega: nenhum token começa com @.

#### FICHA DO ASSUNTO
Em uma frase: Um compilador é uma linha de montagem: léxica → sintática → semântica → código intermediário → otimização → geração de código, com a tabela de símbolos acompanhando todas as fases.
Gancho: Lá Sempre Sai Isso: Ótimo Gol! (Léxica, Sintática, Semântica, Intermediário, Otimização, Geração)
Termos-chave: léxica, sintática, semântica, código intermediário, otimização, tabela de símbolos
Erros comuns: Dizer que variável não declarada é erro sintático (é semântico: depende da tabela de símbolos). // Achar que o léxico detecta palavra-chave digitada errado (`whlie` é um identificador válido). // Confundir fase (divisão lógica) com passada (leitura completa da entrada). // Esquecer a tabela de símbolos e o tratamento de erros, que atravessam todas as fases.
Flashcards:
    · P: O que sai da análise léxica? → R: Um fluxo de tokens
    · P: O que sai da análise sintática? → R: Uma árvore sintática
    · P: Qual fase faz a verificação de tipos? → R: A análise semântica
    · P: Front-end x back-end? → R: Front-end = análise (entende o fonte); back-end = síntese (gera o código-alvo)
    · P: Que erro é `x = (3 + 4;`? → R: Sintático
    · P: Que erro é `int x = 3 @ 4;`? → R: Léxico
    · P: Que erro é usar `y` sem declarar? → R: Semântico
    · P: Por que usar código intermediário? → R: m linguagens + n máquinas = m + n peças, e não m × n compiladores
