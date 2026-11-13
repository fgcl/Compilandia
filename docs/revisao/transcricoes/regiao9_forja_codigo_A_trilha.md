# Região 9: Forja do Código — Código intermediário e otimização
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO TAC: Código de três endereços
Pré-requisitos no jogo: Atributos e verificação de tipos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Uma conta por linha» → Fazer com ajuda → Prática: você pilota «if e while viram desvios» → Pegadinha «A ordem dos temporários» → Desafio
Jogos que treinam: Árvore Anotada (calcular atributos de baixo para cima), Linha de Montagem TAC (montar código de três endereços)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug quebrou a conta
Na tela: a + b * c
BYTE: "Antes da aula: quantas linhas de três endereços dá isso?"
Opções: 1 | ✔2 | 3
Depois: "Duas. Uma conta por linha."

#### AULA ANIMADA BÁSICA: "Uma conta por linha" (21 cenas, ~146s) [1º exemplo começa na cena 4]

1. TELA: # TAC ⏎ a + b * c processador ⏎ uma conta por vez
   BYTE: "O processador faz uma conta por vez, sempre com dois valores."

2. TELA: # TAC ⏎ *a + b * c* processador ⏎ uma conta por vez ⏎ 1 2 3
   BYTE: "Quantas contas há nessa linha? Toque no número."
   PERGUNTA (toque): certo = 2 ; errado = 1 | 3
   se errar: "Conte os operadores. Cada um é uma conta."
   se acertar: "Isso. Duas contas, então duas linhas."

3. TELA: !!TAC!!
   BYTE: "O código de três endereços é um código intermediário: cada linha faz uma conta só, com no máximo três nomes."

4. TELA: # a + b * c ⏎ + ⏎ a * ⏎ b c
   BYTE: "Primeiro a árvore sintática. Ela já diz qual conta vem antes."

5. TELA: # a + b * c ⏎ + ⏎ a * ⏎ b c
   BYTE: "Qual conta tem que sair primeiro? Toque no operador."
   PERGUNTA (toque): certo = * ; errado = +
   se errar: "A árvore manda: quem está mais fundo vem antes."
   se acertar: "Isso. O vezes está mais fundo."

6. TELA: # a + b * c ⏎ + ⏎ a *** ⏎ b c ⏎ *t1 = b * c*
   BYTE: "O resultado do vezes não tem nome no programa. O compilador cria um temporário para ele, o t1."

7. TELA: # a + b * c ⏎ *+* ⏎ a * ⏎ b c ⏎ t1 = b * c ⏎ *t2 = a + t1*
   BYTE: "Na linha seguinte, a árvore já pode usar o t1 no lugar do vezes."

8. TELA: # a + b * c ⏎ + ⏎ a * ⏎ b c ⏎ t1 = b * c ⏎ t2 = a + t1
   PROF: "Cada linha tem no máximo três endereços: dois operandos e um resultado. É daí que vem o nome."

9. TELA: # a + b * c ⏎ + ⏎ a * ⏎ b c ⏎ t1 = b * c ⏎ t2 = a + t1
   BYTE: "A ordem das linhas saiu da árvore. E esse código ainda não depende de nenhum processador."

10. TELA: # Receita ⏎ 1. Gere os filhos antes do pai. ⏎ 2. Cada operador ganha um temporário novo. ⏎ 3. O pai usa o temporário do filho. ⏎ 4. Atribuição: copie o último temporário.
   BYTE: "Para gerar as linhas, ande pela árvore de baixo para cima."

11. TELA: # Receita ⏎ 1. Gere os filhos antes do pai. ⏎ 2. Cada operador ganha um temporário novo. ⏎ 3. O pai usa o temporário do filho. ⏎ 4. Atribuição: copie o último temporário.
   BYTE: "Por que o vezes saiu antes do mais? Toque na regra."
   PERGUNTA (toque): certo = 1. Gere os filhos antes do pai. ; errado = 2. Cada operador ganha um temporário novo. | 3. O pai usa o temporário do filho. | 4. Atribuição: copie o último temporário.
   se errar: "Na árvore, o vezes é filho do mais."
   se acertar: "Isso. Filho antes do pai."

12. TELA: # Receita ⏎ 1. Gere os filhos antes do pai. ⏎ 2. Cada operador ganha um temporário novo. ⏎ *3. O pai usa o temporário do filho.* ⏎ 4. Atribuição: copie o último temporário.
   BYTE: "Regra três: na segunda linha, o t1 ocupa o lugar do vezes."

13. TELA: # x = ( a + b ) * ( c - d ) ⏎ * ⏎ + - ⏎ a b c d
   BYTE: "Agora quem gera é você. No fim, o resultado vai para o x."

14. TELA: # x = ( a + b ) * ( c - d ) ⏎ * ⏎ + - ⏎ a b c d
   BYTE: "Qual operador vira o t1? Toque nele."
   PERGUNTA (toque): certo = + ; errado = - | *
   se errar: "O vezes espera os dois filhos. Comece pelo filho da esquerda."
   se acertar: "Isso. A soma vira o t1."

15. TELA: # x = ( a + b ) * ( c - d ) ⏎ * ⏎ t1 - ⏎ c d ⏎ t1 = a + b
   BYTE: "A soma virou t1. Qual operador vira o t2? Toque."
   PERGUNTA (toque): certo = - ; errado = *
   se errar: "O vezes ainda espera o filho da direita."
   se acertar: "Isso. A subtração vira o t2."

16. TELA: # x = ( a + b ) * ( c - d ) ⏎ *** ⏎ t1 t2 ⏎ t1 = a + b ⏎ t2 = c - d ⏎ t3 = a * d ⏎ t3 = t1 * t2 ⏎ t3 = t1 * c - d
   BYTE: "Agora a raiz. Toque na linha que o vezes gera."
   PERGUNTA (toque): certo = t3 = t1 * t2 ; errado = t3 = a * d | t3 = t1 * c - d
   se errar: "Regra três: use os temporários dos filhos."
   se acertar: "Isso. Três endereços: t3, t1 e t2."

17. TELA: # x = ( a + b ) * ( c - d ) ⏎ t3 ⏎ t1 = a + b ⏎ t2 = c - d ⏎ t3 = t1 * t2 ⏎ t3 t2 t1
   BYTE: "Falta guardar no x. Qual temporário vai para ele? Toque."
   PERGUNTA (toque): certo = t3 ; errado = t2 | t1
   se errar: "Regra quatro: o último temporário, o da raiz."
   se acertar: "Isso. A última linha só copia o valor."

18. TELA: # x = ( a + b ) * ( c - d ) ⏎ t3 ⏎ *t1 = a + b* ⏎ *t2 = c - d* ⏎ *t3 = t1 * t2* ⏎ *x = t3*
   BYTE: "Quatro linhas, e nenhuma faz mais de uma conta. Na etapa de prática, o if e o while também viram linhas, com rótulos e desvios."

19. TELA: # Erros comuns ⏎ x = a + b * c cabe numa linha só de três endereços. ⏎ certo errado
   BYTE: "Esta atribuição cabe numa linha de três endereços? Um colega disse que sim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Conte os nomes: x, a, b e c. Quantos cabem numa linha?"
   se acertar: "Isso. São duas contas e quatro nomes. Cada linha leva um operador e no máximo três endereços."

20. TELA: # Erros comuns ⏎ Certo: São duas linhas: t1 = b * c, depois x = a + t1. ⏎ *Errado: Reusar o mesmo temporário para duas contas diferentes.* ⏎ *Certo: Cada operador ganha um temporário novo: t1, t2, t3...*
   BYTE: "Outro erro: reaproveitar o t1 na segunda conta. Se o valor antigo ainda for usado, ele se perde. Cada resultado ganha um temporário novo."

21. TELA: # Resumo ⏎ Três endereços: resultado = operando operador operando. ⏎ Gere os filhos antes do pai; cada conta guarda num temporário novo. ⏎ É código intermediário: não depende do processador.
   BYTE: "Resumindo: o código de três endereços quebra a expressão em contas simples. A árvore dá a ordem, e cada resultado parcial ganha um temporário."

#### PRATICA: "if e while viram desvios" (9 cenas, ~74s)

1. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2;
   BYTE: "Até aqui, o código de três endereços só fez contas. Mas programa tem if e while, e a máquina não tem nenhum dos dois."

2. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2; ⏎ rótulo:  L1: ⏎ desvio:  goto L1
   BYTE: "A máquina só sabe seguir para a próxima linha ou desviar para uma linha marcada com um rótulo."

3. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2; ⏎ *    t1 = a < b* ⏎ *    ifFalse t1 goto L1*
   BYTE: "Primeiro a condição vira uma conta, guardada em t1. Depois vem o desvio condicional: se t1 for falso, pule para L1."

4. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2; ⏎     t1 = a < b ⏎ *    ifFalse t1 goto L1* ⏎ x = 1 x = 2
   BYTE: "Se a condição é verdadeira, a execução segue para a próxima linha. Que comando vem ali? Toque."
   PERGUNTA (toque): certo = x = 1 ; errado = x = 2
   se errar: "É o comando que roda quando a é menor que b."
   se acertar: "Isso. O bloco do if vem logo depois do desvio condicional."

5. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2; ⏎     t1 = a < b ⏎     ifFalse t1 goto L1 ⏎ *    x = 1* ⏎ goto L2 L1: nada
   BYTE: "Depois de x igual a um, o que impede a execução de cair no else? Toque."
   PERGUNTA (toque): certo = goto L2 ; errado = L1: | nada
   se errar: "Sem nada ali, a próxima linha é a do else."
   se acertar: "Isso. Um desvio incondicional pula o else e vai para o fim, no rótulo L2."

6. TELA: # if / while ⏎ if (a < b) x = 1; ⏎ else x = 2; ⏎     t1 = a < b ⏎     ifFalse t1 goto L1 ⏎     x = 1 ⏎     goto L2 ⏎ *L1: x = 2* ⏎ *L2:*
   BYTE: "O rótulo L1 marca o começo do else. O L2 marca o fim do if inteiro."

7. TELA: # while ⏎ while (i < n) i = i + 1; ⏎ *L1: t1 = i < n* ⏎     ifFalse t1 goto L2 ⏎     i = i + 1 ⏎     goto L1 ⏎ L2:
   BYTE: "O while usa as mesmas peças. O rótulo fica antes da condição, e no fim do corpo um goto volta para ele."

8. TELA: # while ⏎ while (i < n) i = i + 1; ⏎ L1: t1 = i < n ⏎     ifFalse t1 goto L2 ⏎     i = i + 1 ⏎ *    goto L1* ⏎ L2:
   BYTE: "No while, para onde vai o goto do fim do corpo? Toque na linha."
   PERGUNTA (toque): certo = L1: t1 = i < n ; errado =     ifFalse t1 goto L2 | L2:
   se errar: "O laço precisa testar a condição de novo."
   se acertar: "Isso. Ele volta para a condição, que é testada a cada volta."

9. TELA: # while ⏎ while (i < n) i = i + 1; ⏎ L1: t1 = i < n ⏎     ifFalse t1 goto L2 ⏎     i = i + 1 ⏎     goto L1 ⏎ L2:
   BYTE: "O if e o while viram três peças: rótulo, desvio condicional e desvio incondicional. É assim que o seu laço chega ao MIPS."

#### PEGADINHA: "A ordem dos temporários" (5 cenas, ~23s)

1. TELA: # a + b * c ⏎ a + b * c
   BYTE: "Três endereços: no máximo uma conta por linha."

2. TELA: # a + b * c ⏎ a + b * c
   BYTE: "Qual conta vira a primeira linha? Toque no operador."
   PERGUNTA (toque): certo = * ; errado = +
   se errar: "Quem está mais fundo na árvore roda antes."
   se acertar: "Isso. O vezes vai primeiro e guarda em t1."

3. TELA: # a + b * c ⏎ a + b * c ⏎ *t1 = b * c*
   BYTE: "Primeira linha: t1 recebe b vezes c."

4. TELA: # a + b * c ⏎ a + b * c ⏎ t1 = b * c ⏎ *t2 = a + t1*
   BYTE: "Segunda linha: t2 recebe a mais t1."

5. TELA: # a + b * c ⏎ a + b * c ⏎ t1 = b * c ⏎ t2 = a + t1
   PROF: "Se inverter as linhas, o t1 é lido antes de existir."

#### CLIPE 30s: "Três endereços" (2 cenas, ~9s)

1. TELA: # a + b * c ⏎ t1 = b * c
   BYTE: "Cada linha tem no máximo uma conta."

2. TELA: # a + b * c ⏎ t1 = b * c ⏎ *t2 = a + t1*
   BYTE: "Conta maior quebra em temporários: t1, t2, t3."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Conta Longa — mentira: "Uma linha de TAC tem várias contas." / verdade: "No máximo uma conta por linha. O resto vai para temporários."
    · [V] a + b * c vira duas linhas. → Isso mesmo.
    · [V] t1 = b * c vem antes de t2 = a + t1. → Isso mesmo.
    · [F] Temporário é variável que a pessoa declarou. → Quem cria é o compilador.

#### FICHA DO ASSUNTO
Em uma frase: Código de três endereços é uma linguagem intermediária com no máximo um operador por instrução (x = y op z), gerada em pós-ordem com temporários, e representada como quádruplas ou triplas.
Gancho: Dois entram, um sai. QUAdrupla = QUAtro campos; a TRIpla mora no endereço (posição).
Termos-chave: temporário, três endereços, quádrupla, tripla, pós-ordem, rótulo
Erros comuns: Colocar dois operadores numa instrução (t = a + b * c) // Gerar o pai antes dos filhos (a ordem é pós-ordem) // Em triplas, dar nome ao resultado em vez de usar a posição // Esquecer o goto para pular o else num if-else
Flashcards:
    · P: Forma geral da instrução? → R: x = y op z
    · P: Quantos operadores por instrução? → R: No máximo um
    · P: TAC de a + b * c? → R: t1 = b * c; t2 = a + t1
    · P: Campos da quádrupla? → R: op, arg1, arg2, resultado
    · P: Campos da tripla? → R: op, arg1, arg2 (resultado = posição)
    · P: Por que triplas indiretas? → R: Para reordenar sem quebrar referências
    · P: Ordem de geração numa expressão? → R: Pós-ordem da árvore
    · P: Como vira um if em TAC? → R: Desvios condicionais, goto e rótulos

#### GLOSSÁRIO (balão ao tocar no termo)
- três endereços: Código em que cada linha faz uma conta só, como t1 = a + b.
- temporário: Variável que o compilador inventa para guardar um resultado, como t1.


## ASSUNTO OPTIMIZATION: Otimização (dobra de constantes)
Pré-requisitos no jogo: Código de três endereços
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Conta feita antes» → Fazer com ajuda → Pegadinha «Otimizar sem mudar o resultado» → Desafio
Jogos que treinam: Árvore Anotada (calcular atributos de baixo para cima), Linha de Montagem TAC (montar código de três endereços)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug deixou conta à toa
Na tela: x = 2 * 3;
BYTE: "Antes da aula: o compilador pode fazer essa conta sozinho?"
Opções: ✔sim | não | só em C
Depois: "Pode. Ele já escreve seis no código."

#### AULA ANIMADA BÁSICA: "Conta feita antes" (19 cenas, ~137s) [1º exemplo começa na cena 4]

1. TELA: # Otimização ⏎ for (i = 0; i < 1000; i++) ⏎ *  x = 2 * 3 + y;*
   BYTE: "Esta linha roda mil vezes dentro de um laço."

2. TELA: # Otimização ⏎ for (i = 0; i < 1000; i++) ⏎ *  x = 2 * 3 + y;* ⏎ *× 1000*
   BYTE: "Cada conta feita antes poupa mil contas quando o programa roda."

3. TELA: !!OTIMIZAR!!
   BYTE: "Otimizar é trocar o código por outro mais rápido ou menor, que dá exatamente o mesmo resultado."

4. TELA: # Dobra de constantes ⏎ x = 2 * 3 + y
   BYTE: "A otimização mais simples: se a conta só tem constantes, o compilador faz agora. O programa nem precisa calcular. É a dobra de constantes."

5. TELA: # Dobra de constantes ⏎ x = 2 * 3 + y
   BYTE: "Toque na parte que o compilador já consegue resolver."
   PERGUNTA (toque): certo = * ; errado = + | x
   se errar: "Procure uma conta só com números."
   se acertar: "Isso. Dois vezes três não depende de nada."

6. TELA: # Dobra de constantes ⏎ x = *6* + y
   BYTE: "Dois vezes três vira seis, ainda na compilação."

7. TELA: # Dobra de constantes ⏎ x = 6 + *y*
   BYTE: "O y fica. O valor dele só existe em tempo de execução, quando o programa roda."

8. TELA: # Dobra de constantes ⏎ x = 6 + y
   PROF: "Regra de ouro: otimizar nunca pode mudar o resultado do programa. Só o caminho até ele."

9. TELA: # Receita ⏎ 1. Só números: troque a conta pelo resultado. ⏎ 2. Suba: o resultado pode fechar outra conta. ⏎ 3. Com variável, a conta fica para a execução.
   BYTE: "A dobra anda pela árvore, das folhas até a raiz."

10. TELA: # Receita ⏎ 1. Só números: troque a conta pelo resultado. ⏎ 2. Suba: o resultado pode fechar outra conta. ⏎ 3. Com variável, a conta fica para a execução.
   BYTE: "No primeiro exemplo, a soma com y ficou. Que regra manda? Toque."
   PERGUNTA (toque): certo = 3. Com variável, a conta fica para a execução. ; errado = 1. Só números: troque a conta pelo resultado. | 2. Suba: o resultado pode fechar outra conta.
   se errar: "O y só tem valor quando o programa roda."
   se acertar: "Isso. A variável segura a conta para depois."

11. TELA: # Receita ⏎ 1. Só números: troque a conta pelo resultado. ⏎ *2. Suba: o resultado pode fechar outra conta.* ⏎ 3. Com variável, a conta fica para a execução.
   BYTE: "O passo dois faz uma dobra puxar a outra: o resultado vira uma constante nova."

12. TELA: # ( 10 - 2 * 3 ) * x ⏎ * ⏎ - x ⏎ 10 * ⏎ 2 3 ⏎ t1 = 2 * 3 ⏎ t2 = 10 - t1 ⏎ t3 = t2 * x
   BYTE: "Sem otimizar, esta conta vira três linhas. Vamos dobrar."

13. TELA: # ( 10 - 2 * 3 ) * x ⏎ * ⏎ - x ⏎ 10 * ⏎ 2 3 ⏎ t1 = 2 * 3 ⏎ t2 = 10 - t1 ⏎ t3 = t2 * x
   BYTE: "Qual conta o compilador dobra primeiro? Toque no operador."
   PERGUNTA (toque): certo = * ; errado = * | -
   se errar: "Procure um operador com dois números embaixo."
   se acertar: "Isso. Dois vezes três vira seis."

14. TELA: # ( 10 - 2 * 3 ) * x ⏎ * ⏎ - x ⏎ 10 6 ⏎ t1 = 2 * 3 ⏎ t2 = 10 - t1 ⏎ t3 = t2 * x
   BYTE: "O seis subiu. Qual conta ficou só com números? Toque."
   PERGUNTA (toque): certo = - ; errado = *
   se errar: "Regra dois: olhe o pai do seis."
   se acertar: "Isso. Dez menos seis vira quatro."

15. TELA: # ( 10 - 2 * 3 ) * x ⏎ *** ⏎ 4 x ⏎ t1 = 2 * 3 ⏎ t1 = 4 * x ⏎ t2 = 10 - t1
   BYTE: "Sobrou quatro vezes x. Toque na única linha que o programa roda."
   PERGUNTA (toque): certo = t1 = 4 * x ; errado = t1 = 2 * 3 | t2 = 10 - t1
   se errar: "O x só existe quando o programa roda. Essa conta fica."
   se acertar: "Isso. Uma linha no lugar de três."

16. TELA: # ( 10 - 2 * 3 ) * x ⏎ * ⏎ 4 x ⏎ *t1 = 4 * x*
   BYTE: "O resultado é o mesmo para qualquer x. Só o caminho encurtou. Outras otimizações clássicas: propagar cópias e eliminar código morto."

17. TELA: # Erros comuns ⏎ Com x lido do teclado, o compilador dobra x * 2 para um número. ⏎ certo errado
   BYTE: "Dá para dobrar esta conta? Um colega disse que sim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O valor de x já existe na hora de compilar?"
   se acertar: "Isso. O valor de x só aparece quando o programa roda. Dobra de constantes só vale para o que é conhecido na compilação."

18. TELA: # Erros comuns ⏎ Certo: Só se dobra conta com constantes conhecidas na compilação. ⏎ *Errado: Aceitar uma otimização que muda o resultado em algum caso.* ⏎ *Certo: Otimização válida dá o mesmo resultado para toda entrada.*
   BYTE: "Outro erro: achar que vale ganhar velocidade mudando o resultado em algum caso raro. Não vale. A otimização tem que preservar o resultado sempre."

19. TELA: # Resumo ⏎ Otimizar: código mais rápido ou menor, com o mesmo resultado. ⏎ Dobra de constantes: conta só com constantes é feita na compilação. ⏎ Com variável, a conta fica para o tempo de execução.
   BYTE: "Resumindo: a otimização melhora o código sem mudar o que ele faz. A dobra de constantes resolve na compilação tudo o que não depende de variável."

#### PEGADINHA: "Otimizar sem mudar o resultado" (5 cenas, ~26s)

1. TELA: # Pode trocar? ⏎ 2 * 3   vira   6
   BYTE: "Otimizar é trocar código por outro mais rápido, com o mesmo resultado."

2. TELA: # Pode trocar? ⏎ 2 * 3   vira   6 ⏎ x * 1   vira   x
   BYTE: "Multiplicar por um não muda nada. Dá para tirar."

3. TELA: # Pode trocar? ⏎ 2 * 3   vira   6 ⏎ x * 1   vira   x ⏎ x + 0   vira   x ⏎ x / x   vira   1
   BYTE: "Uma destas trocas não vale. Toque nela."
   PERGUNTA (toque): certo = x / x   vira   1 ; errado = x + 0   vira   x
   se errar: "Pense em x valendo zero."
   se acertar: "Isso. Com x zero, o programa dava erro, e a troca sumiria com ele."

4. TELA: # Pode trocar? ⏎ 2 * 3   vira   6 ⏎ x * 1   vira   x ⏎ x + 0   vira   x ⏎ *x / x   vira   1*
   PROF: "Otimização não pode mudar o que o programa faz, nem nos casos raros."

5. TELA: # Pode trocar? ⏎ 2 * 3   vira   6 ⏎ x * 1   vira   x ⏎ x + 0   vira   x ⏎ x / x   vira   1
   BYTE: "Na dúvida, o compilador deixa como está."

#### CLIPE 30s: "Dobra de constantes" (2 cenas, ~9s)

1. TELA: # x = 2 * 3 ⏎ 2 * 3
   BYTE: "Dois vezes três não precisa esperar o programa rodar."

2. TELA: # x = 2 * 3 ⏎ 6
   BYTE: "O compilador faz a conta e escreve seis."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Otimização Mentirosa — mentira: "Otimizar pode mudar o resultado." / verdade: "Otimização precisa manter o que o programa faz."
    · [V] 2 * 3 pode virar 6 ainda na compilação. → Isso mesmo.
    · [V] Código que nunca roda pode sair. → Isso mesmo.
    · [F] x / x sempre pode virar 1. → Com x zero, o programa dava erro.

#### FICHA DO ASSUNTO
Em uma frase: Otimizar é melhorar o código sem mudar seu comportamento: dobra e propagação de constantes, eliminação de subexpressões comuns e de código morto, normalmente dentro de blocos básicos.
Gancho: DOPA SUMO: DObra, PropagA, SUbexpressão comum, MOrto. Líderes = PAD: Primeira, Alvo, Depois de desvio.
Termos-chave: dobra de constantes, propagação, subexpressão comum, código morto, bloco básico, líder
Erros comuns: Reaproveitar a + b depois de a ter sido redefinido // Dobrar operações que dependem de variáveis // Achar que o desvio é líder (líder é a instrução APÓS o desvio) // Remover instrução cujo resultado ainda está vivo // Esquecer que otimização não pode mudar o resultado do programa
Flashcards:
    · P: Dobra de constantes? → R: Calcular em compilação: 2 * 3 → 6
    · P: Propagação de constantes? → R: Após x = 6, usar 6 no lugar de x
    · P: Subexpressão comum exige... → R: Operandos não redefinidos entre as ocorrências
    · P: Código morto? → R: Instrução cujo resultado nunca é usado
    · P: Bloco básico? → R: Sequência sem desvios no meio: entra no início, sai no fim
    · P: Três regras de líder? → R: Primeira instrução, alvo de desvio, instrução após desvio
    · P: Otimização local trabalha onde? → R: Dentro de um bloco básico
    · P: x * 2 → x + x é... → R: Redução de força


## ASSUNTO RUNTIME: Ambientes de execução (registros de ativação)
Pré-requisitos no jogo: Escopo e tabela de símbolos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A pilha de chamadas» → Pegadinha «Cada chamada, um registro» → Desafio
Jogos que treinam: Esconde-Esconde do Escopo (ligar cada uso à sua declaração), Pilha de Chamadas (empilhar e desempilhar registros de ativação)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug encheu a pilha
Na tela: f(3) chama f(2)
BYTE: "Antes da aula: quem sai primeiro da pilha?"
Opções: f(3) | ✔f(2) | nenhum
Depois: "F de dois. O último a entrar sai primeiro."

#### AULA ANIMADA BÁSICA: "A pilha de chamadas" (23 cenas, ~153s) [1º exemplo começa na cena 4]

1. TELA: # fat(2) ⏎ int fat(int n) { ⏎   if (n <= 1) return 1; ⏎ *  return n * fat(n - 1);* ⏎ } ⏎ n = 2
   BYTE: "A função fat chama a si mesma, com um n menor."

2. TELA: # fat(2) ⏎ int fat(int n) { ⏎   if (n <= 1) return 1; ⏎   return n * fat(n - 1); ⏎ } ⏎ n = 2 n = 1
   BYTE: "Se o n morasse num lugar só, qual valor seria perdido? Toque."
   PERGUNTA (toque): certo = n = 2 ; errado = n = 1
   se errar: "A chamada nova escreve por cima da antiga."
   se acertar: "Isso. O fat de dois perderia o seu n."

3. TELA: !!REGISTRO!!
   BYTE: "Cada chamada de função ganha um bloco próprio na pilha de execução: o registro de ativação."

4. TELA: # fat(2) ⏎ fat  n = 2  volta: main
   BYTE: "Quando o main chama fat de dois, um registro de ativação é empilhado para essa chamada."

5. TELA: # fat(2) ⏎ *fat  n = 2  volta: main*
   BYTE: "No registro ficam os parâmetros, as variáveis locais e o endereço de retorno: o ponto para onde voltar quando a função acabar."

6. TELA: # fat(2) ⏎ *fat  n = 1  volta: fat* ⏎ fat  n = 2  volta: main
   BYTE: "O fat de dois chama o fat de um. Outro registro sobe, com outro n."

7. TELA: # fat(2) ⏎ *fat  n = 1  volta: fat* ⏎ fat  n = 2  volta: main
   BYTE: "O fat de um não chama ninguém. Ele devolve um na hora."

8. TELA: # fat(2) ⏎ fat  n = 1  volta: fat ⏎ fat  n = 2  volta: main
   BYTE: "Quem retorna primeiro? Toque no registro."
   PERGUNTA (toque): certo = fat  n = 1  volta: fat ; errado = fat  n = 2  volta: main
   se errar: "Só o topo da pilha está rodando."
   se acertar: "Isso. O último a entrar é o primeiro a sair."

9. TELA: # fat(2) ⏎ *fat  n = 2  volta: main*
   BYTE: "Retornou: o registro sai da pilha, e a execução continua no endereço de retorno de quem chamou."

10. TELA: # fat(2) ⏎ fat  n = 2  volta: main
   BYTE: "Por isso a recursão funciona: cada chamada tem o seu próprio n."

11. TELA: # Receita ⏎ 1. Chamou: empilhe o registro (parâmetros, retorno). ⏎ 2. Só o registro do topo está rodando. ⏎ 3. Retornou: desempilhe e vá para o retorno.
   BYTE: "Chamar e retornar seguem sempre a mesma receita."

12. TELA: # Receita ⏎ 1. Chamou: empilhe o registro (parâmetros, retorno). ⏎ 2. Só o registro do topo está rodando. ⏎ 3. Retornou: desempilhe e vá para o retorno.
   BYTE: "O fat de um voltou para o fat de dois. Qual regra? Toque."
   PERGUNTA (toque): certo = 3. Retornou: desempilhe e vá para o retorno. ; errado = 1. Chamou: empilhe o registro (parâmetros, retorno). | 2. Só o registro do topo está rodando.
   se errar: "O registro saiu, e o controle voltou."
   se acertar: "Isso. Regra três: desempilha e volta."

13. TELA: # Receita ⏎ *1. Chamou: empilhe o registro (parâmetros, retorno).* ⏎ 2. Só o registro do topo está rodando. ⏎ 3. Retornou: desempilhe e vá para o retorno.
   BYTE: "Regra um: o endereço de retorno entra no registro, junto com o n. É por ele que a função sabe para onde voltar."

14. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎   return g(c) + h(c); ⏎ } ⏎ *f  c = 1* ⏎ main
   BYTE: "Outro programa. O main chamou f com c igual a um."

15. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎ *  return g(c) + h(c);* ⏎ } ⏎ g ⏎ f  c = 1 h ⏎ main
   BYTE: "Quem o f chama primeiro? Toque na função."
   PERGUNTA (toque): certo = g ; errado = h
   se errar: "Leia a linha do f da esquerda para a direita."
   se acertar: "Isso. O g ganha um registro em cima do f."

16. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎ *  return h(b * 2);* ⏎ } ⏎ int f(int c) { ⏎   return g(c) + h(c); ⏎ } ⏎ a = 1 ⏎ g  b = 1 ⏎ a = 2 ⏎ f  c = 1 ⏎ a = 3 ⏎ main
   BYTE: "O g chama h com b vezes dois. Quanto vale o a? Toque."
   PERGUNTA (toque): certo = a = 2 ; errado = a = 1 | a = 3
   se errar: "O b vale um. Um vezes dois dá quanto?"
   se acertar: "Isso. O registro do h guarda a igual a dois."

17. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎   return g(c) + h(c); ⏎ } ⏎ h  a = 2 ⏎ 3 ⏎ g  b = 1 ⏎ 4 ⏎ f  c = 1 ⏎ 5 ⏎ main
   BYTE: "Quantos registros a pilha tem agora, contando o main? Toque."
   PERGUNTA (toque): certo = 4 ; errado = 3 | 5
   se errar: "Conte as caixas, do main até o topo."
   se acertar: "Isso. Um registro por chamada que ainda não voltou."

18. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎   return g(c) + h(c); ⏎ } ⏎ f  c = 1 ⏎ main
   BYTE: "O h e o g já retornaram. Qual registro roda agora? Toque."
   PERGUNTA (toque): certo = f  c = 1 ; errado = main
   se errar: "Regra dois: só o topo roda."
   se acertar: "Isso. O f volta a rodar de onde parou."

19. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎ *  return g(c) + h(c);* ⏎ } ⏎ *h  a = 1* ⏎ f  c = 1 ⏎ main
   BYTE: "O f chama o h de novo. Registro novo, com a igual a um."

20. TELA: # f(1) ⏎ int h(int a) { ⏎   return a + 1; ⏎ } ⏎ int g(int b) { ⏎   return h(b * 2); ⏎ } ⏎ int f(int c) { ⏎   return g(c) + h(c); ⏎ } ⏎ *main* *f(1) = 5*
   BYTE: "No fim, o f devolve cinco para o main."

21. TELA: # Erros comuns ⏎ Numa recursão, todas as chamadas dividem a mesma variável local n. ⏎ certo errado
   BYTE: "Onde mora o n de cada chamada do fat? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Quantos registros de ativação havia na pilha quando o fat de um rodava?"
   se acertar: "Isso. Cada chamada tem o seu registro, com o seu n. Se fosse um só, a recursão destruiria os valores."

22. TELA: # Erros comuns ⏎ Certo: Cada chamada tem um registro de ativação próprio, com o seu n. ⏎ *Errado: As funções retornam na ordem em que foram chamadas.* ⏎ *Certo: É uma pilha: a última chamada é a primeira a retornar.*
   BYTE: "Outro erro: achar que quem foi chamado primeiro retorna primeiro. É o contrário. A pilha devolve na ordem inversa das chamadas."

23. TELA: # Resumo ⏎ Registro de ativação: parâmetros, variáveis locais e endereço de retorno. ⏎ Chamada empilha um registro; retorno desempilha. ⏎ Só o registro do topo está executando.
   BYTE: "Resumindo: cada chamada ganha um registro de ativação na pilha, com seus parâmetros, suas locais e o endereço de retorno. A última chamada é a primeira a voltar."

#### TABELAS: "Uma caixa por chamada" (8 cenas, ~51s)

1. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ main{volta: SO}
   BYTE: "Cada chamada de função ganha uma caixa na pilha. É o registro de ativação."

2. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ 5 int main() { ⏎ *6   return fat(3);* ⏎ 7 } ⏎ fat{n = 3; volta: main, linha 6} ⏎ main{volta: SO}
   BYTE: "O main chama fat de três. Nasce uma caixa com n igual a três."

3. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ 5 int main() { ⏎ *6   return fat(3);* ⏎ 7 } ⏎ fat{n = 3; »volta: main, linha 6«} ⏎ main{volta: SO}
   BYTE: "Dentro dela fica também o endereço de volta: para onde o resultado vai."

4. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ *3   return n * fat(n - 1);* ⏎ 4 } ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ fat{n = 2; volta: fat, linha 3} ⏎ fat{n = 3; volta: main, linha 6} ⏎ main{volta: SO}
   BYTE: "fat de três chama fat de dois. Outra caixa entra por cima."

5. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ *3   return n * fat(n - 1);* ⏎ 4 } ⏎ fat{n = 1; volta: fat, linha 3} ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ fat{n = 2; volta: fat, linha 3} ⏎ fat{n = 3; volta: main, linha 6} ⏎ main{volta: SO}
   BYTE: "E fat de dois chama fat de um. A pilha cresce para cima."

6. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ fat{n = 1; volta: fat, linha 3} ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ fat{n = 2; volta: fat, linha 3} ⏎ fat{n = 3; volta: main, linha 6} ⏎ main{volta: SO}
   BYTE: "Quando fat de um terminar, qual caixa sai primeiro? Toque nela."
   PERGUNTA (toque): certo = fat ; errado = main | fat | fat
   se errar: "A pilha só tira pelo topo."
   se acertar: "Isso. Sai sempre a de cima, a última que entrou."

7. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ fat{n = 2; volta: fat, linha 3} ⏎ fat{n = 3; volta: main, linha 6} ⏎ devolveu 1 main{volta: SO}
   BYTE: "fat de um devolve um. A caixa dele sai, e o valor desce."

8. TELA: # fat(3) ⏎ 1 int fat(int n) { ⏎ 2   if (n <= 1) return 1; ⏎ 3   return n * fat(n - 1); ⏎ 4 } ⏎ 5 int main() { ⏎ 6   return fat(3); ⏎ 7 } ⏎ fat(3) = 6 main{volta: SO}
   PROF: "Cada caixa que sai leva o valor para a de baixo. No fim, sobra o main com 6."

#### PEGADINHA: "Cada chamada, um registro" (5 cenas, ~29s)

1. TELA: # fat(3) ⏎ fat(3)
   BYTE: "Função recursiva não reaproveita o mesmo espaço. Cada chamada empilha um registro."

2. TELA: # fat(3) ⏎ fat(2) ⏎ fat(3)
   BYTE: "fat de três chama fat de dois. Sobe outro registro, com o n dele."

3. TELA: # fat(3) ⏎ fat(1) ⏎ fat(2) ⏎ fat(3)
   BYTE: "fat de dois chama fat de um. Três registros vivos ao mesmo tempo."

4. TELA: # fat(3) ⏎ fat(1) ⏎ fat(2) ⏎ fat(3)
   BYTE: "Quem retorna primeiro? Toque no registro."
   PERGUNTA (toque): certo = fat(1) ; errado = fat(3) | fat(2)
   se errar: "A pilha só tira do topo."
   se acertar: "Isso. O último a entrar é o primeiro a sair."

5. TELA: # fat(3) ⏎ fat(2) ⏎ fat(3)
   PROF: "Ao retornar, o registro some, com as variáveis locais dele."

#### CLIPE 30s: "Registro de ativação" (2 cenas, ~11s)

1. TELA: # Pilha de chamadas ⏎ f ⏎ main
   BYTE: "Cada chamada de função ganha um bloco na pilha."

2. TELA: # Pilha de chamadas ⏎ *f* ⏎ main
   BYTE: "Nele ficam os parâmetros, as variáveis locais e o endereço de volta."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Recursão Única — mentira: "Função recursiva tem um registro só." / verdade: "Cada chamada empilha um registro novo."
    · [V] O registro guarda parâmetros, locais e endereço de volta. → Isso mesmo.
    · [V] Ao retornar, o registro sai da pilha. → Isso mesmo.
    · [F] Variável local sobrevive depois do retorno. → Ela sai junto com o registro.

#### FICHA DO ASSUNTO
Em uma frase: Em tempo de execução, cada chamada de função ganha um registro de ativação (quadro) na pilha, parâmetros, valor e endereço de retorno, vínculos, locais e temporários, empilhado na chamada e desempilhado no retorno; o que precisa sobreviver à chamada vai para o heap.
Gancho: Pilha de pratos: prato novo em cima, prato pronto sai de cima. Controle Chama, Acesso Aninha.
Termos-chave: pilha, ativa, retorno, chamad, local, recurs
Erros comuns: Achar que cada função tem UM lugar fixo para as suas variáveis (isso é alocação estática, que não suporta recursão) // Esquecer o quadro de main ao contar a altura máxima da pilha // Confundir vínculo de controle (quem chamou) com vínculo de acesso (onde a função foi declarada no texto) // Confundir endereço de retorno (uma instrução) com vínculo de controle (um quadro) // Não salvar $ra na pilha antes de um jal dentro de uma função que chama outra // Devolver ponteiro para variável local, que morre junto com o quadro
Flashcards:
    · P: O que é um registro de ativação? → R: O bloco de memória de UMA chamada: parâmetros, valor e endereço de retorno, vínculos, locais e temporários
    · P: Por que pilha e não alocação estática? → R: Recursão: cada chamada precisa da sua cópia das locais, criadas e destruídas em ordem LIFO
    · P: O vínculo de controle aponta para... → R: O quadro do chamador (cadeia dinâmica)
    · P: O vínculo de acesso aponta para... → R: O quadro do escopo que envolve a função no texto (cadeia estática)
    · P: Altura máxima da pilha em fat(3), contando main? → R: 4 quadros: main, fat(3), fat(2), fat(1)
    · P: O que jal faz? → R: Guarda PC + 4 em $ra e desvia para a função
    · P: Quem aloca as locais: chamador ou chamado? → R: O chamado (só ele sabe quantas são)
    · P: Onde vivem dados que sobrevivem ao retorno? → R: No heap (new/malloc)
    · P: Para que lado a pilha cresce no MIPS? → R: Para endereços menores: addi $sp, $sp, -8 abre espaço

#### GLOSSÁRIO (balão ao tocar no termo)
- registro de ativação: A caixa que cada chamada de função ganha na pilha, com parâmetros e variáveis.


## ASSUNTO CODEGEN_MIPS: Geração de código MIPS
Pré-requisitos no jogo: Código de três endereços
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Carrega, opera, guarda» → Fazer com ajuda → Programar o Byte → Prática: você pilota «Monte o MIPS» → Pegadinha «O if vira desvio» → Desafio
Jogos que treinam: Linha de Montagem TAC (montar código de três endereços), Montadora MIPS (traduzir TAC para assembly e rodar), Labirinto de Desvios (completar os desvios do if/while e rodar no MARS)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: Robô MIPS

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug roubou um registrador
Na tela: add $t0, $t1, x
BYTE: "Antes da aula: essa instrução vale em MIPS?"
Opções: sim | ✔não | só com sw
Depois: "Não vale. O add só usa registradores. O x precisa vir antes com lw."

#### AULA ANIMADA BÁSICA: "Carrega, opera, guarda" (17 cenas, ~124s)

1. TELA: # MIPS ⏎ total $t0  | setas: total --lw--> $t0
   BYTE: "O processador não conhece variáveis. Ele só conhece registradores, que são poucos e rápidos, e posições de memória."

2. TELA: # MIPS ⏎ total *$t0*  | setas: total --lw--> $t0
   BYTE: "Contas só acontecem entre registradores, dentro do processador."

3. TELA: !!MIPS!!
   BYTE: "A geração de código traduz cada linha de três endereços para instruções da máquina. Aqui a máquina é o MIPS."

4. TELA: # t1 = b * c ⏎ memória registradores  | setas: memória --lw--> registradores
   BYTE: "No MIPS, as contas só usam registradores. As variáveis moram na memória."

5. TELA: # t1 = b * c ⏎ memória registradores ⏎ *lw $t1, b* ⏎ *lw $t2, c* ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1  | setas: memória --lw--> registradores
   BYTE: "Por isso toda conta tem três tempos: carrega os operandos, opera, e guarda o resultado."

6. TELA: # t1 = b * c ⏎ memória registradores ⏎ lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1  | setas: memória --lw--> registradores
   BYTE: "Qual linha faz a multiplicação? Toque nela."
   PERGUNTA (toque): certo = mul $t3, $t1, $t2 ; errado = lw $t1, b | lw $t2, c | sw $t3, t1
   se errar: "Procure a instrução mul."
   se acertar: "Isso. Antes dela, os dois lw, de load word, trazem os valores da memória."

7. TELA: # t1 = b * c ⏎ memória registradores ⏎ lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ *sw $t3, t1*  | setas: memória --lw--> registradores
   BYTE: "O sw, de store word, leva o resultado do registrador de volta para a memória."

8. TELA: # Receita ⏎ 1. lw: carregue cada operando num registrador. ⏎ 2. Conta entre registradores: add, sub, mul. ⏎ 3. sw: guarde o resultado na memória.
   BYTE: "A receita para cada linha de três endereços tem três passos."

9. TELA: # Receita ⏎ 1. lw: carregue cada operando num registrador. ⏎ 2. Conta entre registradores: add, sub, mul. ⏎ 3. sw: guarde o resultado na memória.
   BYTE: "Qual passo traz os valores da memória? Toque."
   PERGUNTA (toque): certo = 1. lw: carregue cada operando num registrador. ; errado = 2. Conta entre registradores: add, sub, mul. | 3. sw: guarde o resultado na memória.
   se errar: "O processador só soma o que já está num registrador."
   se acertar: "Isso. O lw vem primeiro."

10. TELA: # Receita ⏎ 1. lw: carregue cada operando num registrador. ⏎ 2. Conta entre registradores: add, sub, mul. ⏎ *3. sw: guarde o resultado na memória.*
   BYTE: "Passo três: sem o sw, o resultado fica só no registrador e se perde quando ele for reusado."

11. TELA: # preco + taxa ⏎ add $t3, $t1, $t2 ⏎ lw $t1, preco ⏎ sw $t3, t1 ⏎ lw $t2, taxa
   BYTE: "Agora preço mais taxa. As linhas estão fora de ordem."

12. TELA: # preco + taxa ⏎ add $t3, $t1, $t2 ⏎ lw $t1, preco ⏎ sw $t3, t1 ⏎ lw $t2, taxa
   BYTE: "Toque nas duas linhas que vêm primeiro."
   PERGUNTA (toque): certo = lw $t1, preco | lw $t2, taxa ; errado = add $t3, $t1, $t2 | sw $t3, t1
   se errar: "Antes de somar, os valores precisam estar nos registradores."
   se acertar: "Isso. Os dois lw abrem o trio."

13. TELA: # preco + taxa ⏎ add $t3, $t1, $t2 ⏎ lw $t1, preco ⏎ sw $t3, t1 ⏎ lw $t2, taxa
   BYTE: "E qual linha fecha o trio? Toque nela."
   PERGUNTA (toque): certo = sw $t3, t1 ; errado = add $t3, $t1, $t2
   se errar: "O resultado precisa voltar para a memória."
   se acertar: "Isso. O sw guarda o resultado."

14. TELA: # preco + taxa ⏎ lw $t1, preco ⏎ lw $t2, taxa ⏎ add $t3, $t1, $t2 ⏎ sw $t3, t1
   BYTE: "Agora você traduz qualquer linha de três endereços para MIPS: dois lw, a conta e um sw."

15. TELA: # Erros comuns ⏎ Para somar a e b, basta: add a, b direto na memória. ⏎ certo errado
   BYTE: "No MIPS, dá para somar duas variáveis direto na memória? Um colega escreveu assim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O add do MIPS só aceita registradores."
   se acertar: "Isso. Primeiro o lw carrega cada variável num registrador. Só então o add faz a conta."

16. TELA: # Erros comuns ⏎ Certo: lw, lw, add, sw: a conta acontece só entre registradores. ⏎ *Errado: Terminar no add e esquecer o sw.* ⏎ *Certo: Sem sw, a variável na memória continua com o valor antigo.*
   BYTE: "Outro erro: parar na conta. O resultado está no registrador, mas a variável mora na memória. Sem o sw, ela não muda."

17. TELA: # Resumo ⏎ lw: da memória para o registrador. sw: do registrador para a memória. ⏎ add, sub e mul operam só entre registradores. ⏎ Cada linha de três endereços vira: carrega, opera, guarda.
   BYTE: "Resumindo: gerar código é traduzir cada linha de três endereços para a máquina. No MIPS, isso é carregar com lw, operar entre registradores e guardar com sw."

#### PRATICA: "Monte o MIPS" (5 cenas, ~27s)

1. TELA: # a + b ⏎ lw add sw
   BYTE: "MIPS só faz conta em registrador. Toda soma tem três tempos."

2. TELA: # a + b ⏎ *lw* *add* *sw*
   BYTE: "Lw traz da memória. Add faz a conta. Sw guarda o resultado."

3. TELA: # a + b ⏎ lw add sw
   PROF: "Esqueceu o lw, a conta usa lixo. Esqueceu o sw, o resultado some."

4. TELA: a + b ⏎ [?próxima instrução]
   BYTE: "Monte as instruções na ordem."
   BRINQUEDO interativo: meta = "Escolha a próxima instrução" ; teclas = [lw, add, sw]
   se errar: "Ordem errada. Primeiro carrega, depois calcula, depois guarda."
   ao conseguir: "Programa pronto. Foi assim que o gerador de código fez."

5. TELA: # a + b ⏎ lw add sw
   BYTE: "Cada linha de três endereços vira esse mesmo trio."

#### PEGADINHA: "O if vira desvio" (5 cenas, ~28s)

1. TELA: # if (a < b) x = 1; ⏎ lw  $t0, a ⏎ lw  $t1, b ⏎ bge $t0, $t1, fim ⏎ li  $t2, 1 ⏎ sw  $t2, x ⏎ fim:
   BYTE: "MIPS não tem if. Tem pulo. O teste sai ao contrário."

2. TELA: # if (a < b) x = 1; ⏎ *lw  $t0, a* ⏎ *lw  $t1, b* ⏎ bge $t0, $t1, fim ⏎ li  $t2, 1 ⏎ sw  $t2, x ⏎ fim:
   BYTE: "Carrega a e b nos registradores."

3. TELA: # if (a < b) x = 1; ⏎ lw  $t0, a ⏎ lw  $t1, b ⏎ *bge $t0, $t1, fim* ⏎ li  $t2, 1 ⏎ sw  $t2, x ⏎ fim:
   BYTE: "Aqui está o truque. O if pede a menor que b, e o desvio testa a maior ou igual."

4. TELA: # if (a < b) x = 1; ⏎ lw  $t0, a ⏎ lw  $t1, b ⏎ bge $t0, $t1, fim ⏎ li  $t2, 1 ⏎ sw  $t2, x ⏎ fim:
   BYTE: "Se a for maior ou igual a b, para onde o programa pula? Toque."
   PERGUNTA (toque): certo = fim: ; errado = li  $t2, 1 | sw  $t2, x
   se errar: "Com a condição falsa, o corpo do if não pode rodar."
   se acertar: "Isso. Ele pula o corpo e cai em fim."

5. TELA: # if (a < b) x = 1; ⏎ lw  $t0, a ⏎ lw  $t1, b ⏎ *bge $t0, $t1, fim* ⏎ li  $t2, 1 ⏎ sw  $t2, x ⏎ *fim:*
   PROF: "Teste invertido: o desvio descreve quando pular o corpo."

#### CLIPE 30s: "Registrador e memória" (2 cenas, ~9s)

1. TELA: # t0  t1  t2 ⏎ t0 t1 t2
   BYTE: "Registrador é rápido, mas são poucos."

2. TELA: # t0  t1  t2 ⏎ t0 t1 t2 ⏎ lw  $t0, x ⏎ sw  $t0, y
   BYTE: "Em MIPS, lw traz da memória, e sw guarda de volta."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Load Esquecido — mentira: "MIPS soma direto da memória." / verdade: "MIPS só opera em registradores. Primeiro lw, depois a conta, depois sw."
    · [V] lw copia da memória para o registrador. → Isso mesmo.
    · [V] sw guarda o registrador na memória. → Isso mesmo.
    · [F] add soma dois endereços de memória direto. → add só usa registradores.

#### FICHA DO ASSUNTO
Em uma frase: O gerador traduz cada instrução TAC para MIPS, no estilo simples, busca os operandos na .data com lw/li, opera em registradores e guarda com sw, e transforma if/while em comparações (slt/sgt), desvios (beq/bne/j) e rótulos novos.
Gancho: Busca, busca, mistura, guarda. Falso? beq pula. Fim do then? j por cima do else.
Termos-chave: registrador, lw, sw, data, text, desvi, syscall
Erros comuns: Usar uma variável da memória direto no add (no MIPS só lw/sw tocam a memória) // Esquecer o j L1 no fim do then, fazendo a execução 'escorregar' para o else // Reaproveitar o mesmo rótulo em dois ifs (erro de montagem) // Esquecer de declarar os temporários na .data ou de encerrar com li $v0, 10 + syscall // Confundir t1 (variável na memória) com $t1 (registrador)
Flashcards:
    · P: TAC t = a + b no estilo do trabalho vira... → R: lw $t1, a / lw $t2, b / add $t3, $t1, $t2 / sw $t3, t
    · P: Como carregar uma constante? → R: li $t2, 5
    · P: Para que serve $zero? → R: Vale sempre 0 (comparar com falso, copiar)
    · P: Esqueleto do if/else? → R: cond; beq $t3, $zero, Lelse; then; j Lfim; Lelse: else; Lfim:
    · P: Esqueleto do while? → R: Lini: cond; beq $t3, $zero, Lfim; corpo; j Lini; Lfim:
    · P: Imprimir um inteiro x? → R: li $v0, 1 / lw $a0, x / syscall
    · P: Imprimir uma string msg? → R: li $v0, 4 / la $a0, msg / syscall
    · P: Encerrar o programa? → R: li $v0, 10 / syscall
    · P: Ideia da alocação de registradores? → R: Manter valores em registradores para cortar lw/sw, que são caros

#### GLOSSÁRIO (balão ao tocar no termo)
- registrador: Gaveta rápida dentro do processador. As contas do MIPS acontecem nela.
- MIPS: Uma linguagem de máquina simples, usada para estudar geração de código.


## ASSUNTO REG_ALLOC: Alocação de registradores
Pré-requisitos no jogo: Geração de código MIPS
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Colorir o grafo» → Pegadinha «Quem vive junto briga» → Desafio
Jogos que treinam: Montadora MIPS (traduzir TAC para assembly e rodar), Coloração de Registradores (vivacidade + colorir o grafo de interferência)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug sumiu com um registrador
Na tela: t1 e t2 vivos juntos
BYTE: "Antes da aula: eles podem dividir um registrador?"
Opções: sim | ✔não | às vezes
Depois: "Não podem. Um apagaria o outro."

#### AULA ANIMADA BÁSICA: "Colorir o grafo" (20 cenas, ~145s) [1º exemplo começa na cena 4]

1. TELA: # Alocação ⏎ memória registradores
   BYTE: "Ler a memória é lento. Registrador é rápido, mas são poucos."

2. TELA: # Alocação ⏎ memória registradores
   BYTE: "Onde a conta roda mais rápido? Toque."
   PERGUNTA (toque): certo = registradores ; errado = memória
   se errar: "O registrador fica dentro do processador."
   se acertar: "Isso. Por isso cada valor quer um registrador."

3. TELA: !!ALOCAÇÃO!!
   BYTE: "Decidir qual valor fica em qual registrador é a alocação de registradores."

4. TELA: # Alocação ⏎ a c ⏎ b
   BYTE: "Os registradores são poucos, e os temporários são muitos. O compilador precisa dividir."

5. TELA: # Alocação ⏎ a c ⏎ b
   BYTE: "Uma variável está viva enquanto o valor dela ainda vai ser usado. Duas vivas ao mesmo tempo não podem usar o mesmo registrador."

6. TELA: # Alocação ⏎ a c ⏎ b
   PROF: "Cada variável vira um nó. Ligo com uma aresta as que estão vivas ao mesmo tempo. Esse é o grafo de interferência."

7. TELA: # Alocação ⏎ a c ⏎ b
   BYTE: "Quais duas podem dividir um registrador? Toque nelas."
   PERGUNTA (toque): certo = a | c ; errado = b
   se errar: "Procure as duas que não têm fio entre elas."
   se acertar: "Isso. Sem aresta entre elas, nunca estão vivas juntas."

8. TELA: # Alocação ⏎ a c ⏎ $s0 $s0 ⏎ b ⏎ $s1
   BYTE: "Agora é colorir o grafo: vizinhos nunca recebem a mesma cor. Cada cor é um registrador."

9. TELA: # Alocação ⏎ a c ⏎ $s0 $s0 ⏎ b ⏎ $s1
   BYTE: "Se faltar cor, uma variável vai morar na memória. Isso é o spill, o derramamento."

10. TELA: # Receita ⏎ 1. Vivacidade: quem ainda será lido abaixo? ⏎ 2. Interferência: ligue quem vive junto. ⏎ 3. Cores: vizinhos diferentes. Sem cor, spill.
   BYTE: "A receita começa no código e termina nas cores."

11. TELA: # Receita ⏎ 1. Vivacidade: quem ainda será lido abaixo? ⏎ 2. Interferência: ligue quem vive junto. ⏎ 3. Cores: vizinhos diferentes. Sem cor, spill.
   BYTE: "O fio entre as duas primeiras variáveis veio de qual passo? Toque."
   PERGUNTA (toque): certo = 2. Interferência: ligue quem vive junto. ; errado = 1. Vivacidade: quem ainda será lido abaixo? | 3. Cores: vizinhos diferentes. Sem cor, spill.
   se errar: "O fio liga quem vive ao mesmo tempo."
   se acertar: "Isso. Interferência vira fio no grafo."

12. TELA: # Receita ⏎ *1. Vivacidade: quem ainda será lido abaixo?* ⏎ 2. Interferência: ligue quem vive junto. ⏎ 3. Cores: vizinhos diferentes. Sem cor, spill.
   BYTE: "A análise de vivacidade anda de trás para a frente, da última linha até a primeira."

13. TELA: # a * b + ( c - d ) * e ⏎ t1 = a * b ⏎ t2 = c - d ⏎ t3 = t2 * e ⏎ t4 = t1 + t3 ⏎ t4 ⏎ t1 ⏎ t2 t3
   BYTE: "Agora com temporários de verdade. Quatro linhas, quatro temporários."

14. TELA: # a * b + ( c - d ) * e ⏎ t1 = a * b ⏎ *t2 = c - d* ⏎ t3 = t2 * e ⏎ t4 = t1 + t3 ⏎ t4 ⏎ t1 ⏎ t2 t3
   BYTE: "Logo depois da linha acesa, quem ainda está vivo? Toque nos temporários."
   PERGUNTA (toque): certo = t2 | t1 ; errado = t3 | t4
   se errar: "Vivo é quem ainda vai ser lido mais abaixo."
   se acertar: "Isso. Os dois convivem, então ganham um fio."

15. TELA: # a * b + ( c - d ) * e ⏎ t1 = a * b ⏎ t2 = c - d ⏎ *t3 = t2 * e* ⏎ t4 = t1 + t3 ⏎ t4 ⏎ t1 ⏎ t2 t3
   BYTE: "E depois desta linha acesa? Toque em quem está vivo."
   PERGUNTA (toque): certo = t1 | t3 ; errado = t2 | t4
   se errar: "O t2 já foi lido. Quem ainda espera?"
   se acertar: "Isso. Mais um fio, do t1 até o t3."

16. TELA: # a * b + ( c - d ) * e ⏎ t1 = a * b ⏎ t2 = c - d ⏎ t3 = t2 * e ⏎ t4 = t1 + t3 ⏎ t4 ⏎ t1 ⏎ t2 t3 ⏎ 1 2 3
   BYTE: "Quantos registradores bastam para este grafo? Toque no número."
   PERGUNTA (toque): certo = 2 ; errado = 1 | 3
   se errar: "O t2 e o t3 não têm fio entre eles."
   se acertar: "Isso. Dois bastam, sem spill."

17. TELA: # a * b + ( c - d ) * e ⏎ t1 = a * b ⏎ t2 = c - d ⏎ t3 = t2 * e ⏎ t4 = t1 + t3 ⏎ t4 ⏎ t1 ⏎ $s0 ⏎ $s1 ⏎ t2 t3 ⏎ $s0 $s0
   BYTE: "Quatro temporários em dois registradores. Ninguém foi para a memória."

18. TELA: # Erros comuns ⏎ Quatro temporários precisam de quatro registradores. ⏎ certo errado
   BYTE: "Quantos registradores este código precisa? Um colega contou os temporários. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "No exemplo, quatro temporários couberam em quantos registradores?"
   se acertar: "Isso. O que conta é quantos estão vivos ao mesmo tempo. Quem nunca convive pode dividir o registrador."

19. TELA: # Erros comuns ⏎ Certo: Bastam tantos registradores quantas cores o grafo de interferência pede. ⏎ *Errado: Calcular a vivacidade de cima para baixo.* ⏎ *Certo: A vivacidade anda de baixo para cima: vivo é quem ainda será lido.*
   BYTE: "Outro erro: calcular a vivacidade de cima para baixo. Vivo é quem ainda vai ser lido adiante, então a análise começa no fim."

20. TELA: # Resumo ⏎ Vivo: o valor ainda será lido mais abaixo. ⏎ Grafo de interferência: aresta entre quem está vivo ao mesmo tempo. ⏎ Colorir: vizinhos com cores diferentes. Faltou cor, spill para a memória.
   BYTE: "Resumindo: a alocação decide quem fica em registrador. A vivacidade mostra quem convive, o grafo liga os que convivem, e a coloração distribui os registradores."

#### PEGADINHA: "Quem vive junto briga" (5 cenas, ~31s)

1. TELA: # Vivos juntos ⏎ t1 = a + b ⏎ t2 = c + d ⏎ t3 = t1 * t2
   BYTE: "Dois valores vivos ao mesmo tempo não podem dividir um registrador."

2. TELA: # Vivos juntos ⏎ *t1 = a + b* ⏎ t2 = c + d ⏎ *t3 = t1 * t2*
   BYTE: "t1 nasce na primeira linha, e a terceira lê esse valor. Fica vivo o tempo todo."

3. TELA: # Vivos juntos ⏎ t1 = a + b ⏎ t2 = c + d ⏎ t3 = t1 * t2 ⏎ t1 t2 t3
   BYTE: "Quem está vivo junto com t1 na segunda linha? Toque no nó."
   PERGUNTA (toque): certo = t2 ; errado = t3
   se errar: "Veja quem já nasceu e ainda tem uso pela frente."
   se acertar: "Isso. t1 e t2 são vizinhos no grafo. Registradores diferentes."

4. TELA: # Vivos juntos ⏎ t1 = a + b ⏎ t2 = c + d ⏎ t3 = t1 * t2 ⏎ t1 t2 t3
   BYTE: "t3 nasce quando t1 e t2 morrem. Ele pode reaproveitar um dos registradores."

5. TELA: !!INTERFERÊNCIA!!
   PROF: "Dois registradores bastam. Colorir o grafo é só isso."

#### CLIPE 30s: "O que é spill" (2 cenas, ~10s)

1. TELA: # Spill ⏎ t1 t2 t3
   BYTE: "Faltou registrador? Um valor vai para a memória."

2. TELA: # Spill ⏎ t1 t2 ⏎ *memória*
   BYTE: "Isso é spill. Funciona, mas fica mais lento."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Registrador Infinito — mentira: "Sempre há registrador para todo mundo." / verdade: "São poucos. Quem não cabe vai para a memória: isso é spill."
    · [V] Temporários vivos ao mesmo tempo interferem. → Isso mesmo.
    · [V] Colorir o grafo é dar registradores diferentes a vizinhos. → Isso mesmo.
    · [F] Temporários que não interferem precisam de registradores diferentes. → Eles podem dividir o mesmo.

#### FICHA DO ASSUNTO
Em uma frase: Alocar registradores é manter os temporários em registradores em vez da memória: calcula-se a vivacidade de trás para frente (in = use ∪ (out − def)), liga-se no grafo de interferência quem está vivo ao mesmo tempo e colore-se o grafo com k cores (k registradores); se não der, algum temporário é derramado (spill) para a memória.
Gancho: VIGOR: VIvacidade, Grafo, cOlorir, Registradores. Def mata, use ressuscita. $t = tchau, $s = sagrado.
Termos-chave: registrador, viv, interferen, color, spill, memoria
Erros comuns: Fazer a análise de vivacidade de cima para baixo // Achar que 'vivo' é 'já foi definido' (vivo é 'ainda vai ser lido') // Ligar no grafo temporários que nunca estão vivos ao mesmo tempo // Confundir o número cromático com o número de temporários // Inverter a convenção: $s é salvo pelo CHAMADO, $t pelo CHAMADOR
Flashcards:
    · P: Por que alocar registradores? → R: lw/sw são lentos; registradores são rápidos e poucos
    · P: Equação da vivacidade? → R: in = use ∪ (out − def), de trás para frente
    · P: Quando x e y interferem? → R: Quando um é definido enquanto o outro está vivo (ambos vivos juntos)
    · P: k registradores equivalem a... → R: colorir o grafo de interferência com k cores
    · P: Passos de Chaitin? → R: Simplificar nós de grau < k (empilhando), depois desempilhar e colorir
    · P: O que é spill? → R: Derramar um temporário para a memória: sw após definir, lw antes de cada uso
    · P: lw + sw em a * b + c * d: ingênuo × alocado? → R: 11 × 5
    · P: $s0–$s7 são salvos por quem? → R: Pelo chamado (callee-saved)
    · P: $t0–$t9 são salvos por quem? → R: Pelo chamador (caller-saved)

#### GLOSSÁRIO (balão ao tocar no termo)
- spill: Guardar uma variável na memória quando faltam registradores.
