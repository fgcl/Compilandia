# Relatório do professor — Regiões 1 (Porto da Partida) e 2 (Floresta Léxica)

## Veredito

O trecho ensina bem e quase sem erro de conteúdo: refiz todas as contas e nenhuma resposta marcada como certa está errada. Os textos WORKED e FORMAL de AFN→AFD, minimização e Flex têm nível de prova. O maior problema é de desenho: **os dois assuntos mais procedimentais e mais cobrados (AFN→AFD e minimização) têm a trilha mais curta** (aula animada → pegadinha → desafio, sem "Fazer com ajuda" nem prática), com exemplos fáceis demais. Neles o ε-fecho depois do move nunca acrescenta estado, o estado morto não aparece e a propagação não passa da 2ª rodada. O aluno reconhece a resposta, mas não produz a tabela Dtran nem a tabela de pares. O segundo problema: ficha e Bugs cobram conteúdo que só existe nos textos opcionais (Thompson, yytext, front-end, tabela de símbolos). Há algumas contradições pequenas entre versões.

## Erros técnicos

Nenhum de gravidade alta. Os de gravidade média e baixa:

| Assunto | Onde | Trecho | Problema | Conserto | Grav. |
|---|---|---|---|---|---|
| TOKENS | Glossário | "Em x = 10, o 10 é um token do tipo número." | Chama o lexema de token, o erro que a própria aula combate (cena 20). | "token: a classe de um pedaço do código (ID, NUM, IF). Em x = 10, o texto 10 é o lexema; o token dele é NUM." | média |
| PHASES | Pegadinha, cena 4 | "Semântica. Só ela consulta a tabela de símbolos." | Falso: a ficha e o STORY dizem que todas as fases leem e escrevem nela. | "Semântica. É ela que confere, na tabela de símbolos, se o nome foi declarado." | média |
| PHASES | Animada, cenas 10, 19 e 22 | "três de análise, três de síntese" | No Dragão (1.2) a análise constrói a representação intermediária; o FORMAL do próprio jogo diz isso. Contradição interna. | Na cena 10: "As três primeiras entendem o programa. Daqui em diante o compilador constrói. O código intermediário é a ponte: o front-end termina nele." Alinhar o resumo. | média |
| MINIMIZAÇÃO | Animada, cenas 1–10 | AFD A,B,C,D sem seta `a` em B e C e sem setas em D | A tabela é feita em AFD incompleto, sem estado morto. A ficha chama isso de erro nº 1 ("O morto também vota"). O resultado sai certo por sorte do exemplo. | Completar o desenho, ou incluir uma cena: "Falta seta? Ela vai para o estado morto ∅, que também entra na tabela." | média |
| MINIMIZAÇÃO | Animada, cena 18 | "A e B vão para estados diferentes com b. Então A e B são diferentes." (gabarito: errado) | No exemplo 1, A e B são de fato diferentes, e justamente por causa do b. Quem lembra do exemplo marca "certo" com razão. | Usar um caso de destinos diferentes mas equivalentes, como o AFD "termina em b" do texto WORKED: "Com b, q0 vai para q1 e q2 vai para q3. Então q0 e q2 são diferentes." Errado, porque q1 ≡ q3. | média |
| REGEX_DFA | Animada, cena 5; STORY | "sai no máximo uma seta por símbolo" | O FORMAL e o resumo dizem "exatamente uma". A simplificação é aceitável, mas precisa de aviso, porque a minimização depende dela. | "Sai uma seta por símbolo. Quando o desenho não mostra a seta, ela leva ao estado morto, e a cadeia é rejeitada." | média |
| AFN→AFD | Animada, cenas 16–18 | "O AFD tem três estados" | {0,1,3} lendo b dá conjunto vazio e a aula não diz nada. A ficha cobra "move vazio → estado morto ∅". | Cena nova: "E lendo b? Ninguém tem seta com b. O conjunto fica vazio: é o estado morto, que o desenho omite." | baixa |
| MINIMIZAÇÃO | Pegadinha, cenas 1 e 5 | "parecem gêmeos quando vão para os mesmos lugares"; "Se um vai para um par já separado" | No desenho (q0→q1→q2⇄q1, q2 final) nenhum par vai para os mesmos lugares, e nada se junta no fim. A frase da cena 5 está imprecisa. | Usar dois estados com setas idênticas, um final e outro não. Cena 5: "Se, com a mesma letra, os dois caem num par já separado, eles também se separam." | baixa |
| Vários | — | — | MINIMIZAÇÃO cena 8: a tela marca (A,C) sem a fala justificar. REGEX mnemônico "Subconjuntos Simplificam": a construção pode ir a 2ⁿ estados. TOKENS cena 2: "treze caracteres" com 10 símbolos na tela. PHASES FORMAL: 7 caixas numa aula de "seis fases". | Justificar (A,C); "Subconjuntos tiram a dúvida"; mostrar os espaços (␣); avisar que a 7ª caixa é opcional. | baixa |
| LEX_FLEX | Ficha, flashcard; STORY | "lex scanner.l && gcc lex.yy.c" | Só liga se o arquivo tiver yywrap e main; a mesma ficha lista esse erro. | "flex scanner.l && gcc lex.yy.c -lfl (ou defina yywrap e main no .l)". | baixa |

**Conferi refazendo a conta, e está certo:**
- **Tokens:** todas as contagens e fatiamentos (`while (i<=n) i++;` 9, `x = a+++b;`, `if (x >= 10) total += x;` 10, `x===y`, entre outros) e as seis saídas do exemplo do Dragão.
- **AFN→AFD:** "termina em ab", "termina em bb", ab\*|c ({0,1,3}, {2}, {4}), (ab|a)c (A={0}, B={1,3,4}, C={2,4}, D={5}), o Thompson do livro (A–E, ε-fecho({0}) = {0,1,2,4,7}, Dtran), "penúltimo é a" (4 estados) e "antepenúltimo é a" (8).
- **Minimização:** A,B,C,D (B≡C); P,Q,R (P≡Q); AFD do livro rodada a rodada (9 de 10 pares, q0≡q2) e por partições; "termina em b" (2 estados); a\*b\* com ∅; ab\*.
- **Flex:** maior casamento e empate em todos os exemplos (`ifa`, `iffy`, `==` abaixo de `=`, `3.14`, regra `.` no topo), o scanner.l do TP token por token e a Fig. 3.8.
- **Cena 11 do Flex:** conferi no código que a entrada é `while x==10`, com espaço. Na transcrição o espaço não aparece; se a célula vazia não for visível na tela, o aluno lê `whilex`, que é ID nas duas ordens.

## Problemas de didática e estrutura

**PHASES**
- (média) A ficha cobra "front-end x back-end", "m + n", `whlie`, "fase x passada" e tabela de símbolos. Nada disso está na aula animada nem na pegadinha, só nos textos opcionais. "Tabela de símbolos" e "scanner" aparecem pela primeira vez dentro de uma fala de acerto da pegadinha, sem explicação.
- (média) Não há "Fazer com ajuda". A questão de prova típica ("mostre a saída de cada fase para a atribuição X") só existe no texto WORKED, que é bom. Vale levar o exemplo `total = preco * 2` para a trilha.

**TOKENS** — o melhor da trilha: tem exemplo antes do termo, cortes feitos pelo aluno e a pegadinha `ifa`.
- (média) O assunto se chama "Tokens, lexemas e padrões", mas **padrão** e **atributo** não aparecem na aula animada, só na ficha e nos textos. Falta uma cena: "A regra que diz quais textos entram numa classe é o padrão. ID: letra seguida de letras ou dígitos."

**REGEX_DFA**
- (alta) A cena 14 apresenta três operadores numa fala só (estrela, barra, concatenação). A barra nunca aparece num exemplo nem numa pergunta da aula animada, e o ε aparece na tela sem nome. Precedência e parênteses só existem na ficha e no FORMAL, mas o Bug já cobra "(ab)\* aceita aba". Conserto: uma cena por operador e trocar o segundo exemplo (hoje `a b* c`, que só acrescenta um estado) por `a(b|c)*`, com uma pergunta "ab\* é a(b\*) ou (ab)\*?".
- (média) "AFD" é usado na cena 5 sem dizer o nome por extenso. O glossário também não diz.
- (média) A cena 20 ensina que "passar por um final no meio não aceita nada". O scanner faz o oposto útil: lembra o último final e recua. Essa ponte só existe em textos opcionais (TOKENS VISUAL, LEX SOCRATIC). Cena sugerida no Flex: "O scanner roda o autômato até travar e volta ao último estado final por onde passou. Esse é o maior casamento."
- (média) A ficha ("Thompson as transforma em AFN, e a construção de subconjuntos...") e os flashcards (ε-fecho, 2ⁿ) são do assunto seguinte. O estilo WORKED também: é a construção de subconjuntos com o mesmo AFN da aula animada de AFN→AFD. Falta ali um resolvido de "escreva a ER para…" ou de Thompson.
- (baixa) O Bug "Regex que Conta" pede para julgar "gramática livre de contexto", termo da Região 3, e a aula animada não diz que regex não conta.

**AFN→AFD**
- (alta) O erro nº 1 da lista de referência (ε-fecho esquecido depois do move) é dito, mas nunca praticado. Nos dois exemplos da aula animada e na pegadinha o fecho só importa no estado inicial. O exemplo certo já existe no texto WORKED: (ab|a)c, em que move dá {1,3} e o fecho puxa o 4. Deve ser o "Sua vez" da aula animada.
- (alta) Não há "Fazer com ajuda", nem tabela Dtran, nem nomes A, B, C na trilha. Na prova o aluno escreve a tabela "estado | conjunto | a | b".

**MINIMIZAÇÃO**
- (alta) A trilha também não tem prática de produção. A aula animada nunca desenha o AFD mínimo com as setas redirecionadas, embora a ficha diga que parar na tabela perde metade da questão.
- (alta) O segundo exemplo (P,Q,R) é mais fácil que o primeiro: não precisa de rodada 2. Nenhum exemplo da trilha tem 3ª rodada, então "repita até nada mudar" fica só no discurso. O AFD do livro (texto WORKED, 3 rodadas, q0≡q2) deveria ser o "Sua vez".
- (baixa) O Bug dos Gêmeos ("estados com o mesmo número de setas são iguais") não é erro que aluno comete. Troque por "Parei na primeira rodada: os pares em branco são equivalentes."

**LEX_FLEX** — aula animada boa: as duas regras na ordem certa, o segundo exemplo traz o caso novo (`==` abaixo de `=` vencendo) e o erro vem junto com a correção.
- (média) `[a-z]+`, `[0-9]+`, `[ \t\n]` e as aspas aparecem sem explicação: a aula de regex só ensinou a, b, `*`, `|`, `+`, `?`. Cena sugerida: "Colchetes são uma lista de caracteres: [a-z] é qualquer letra minúscula."
- (média) As três seções `%%`, yytext e yylval não estão na trilha, mas o Bug pede para julgar "yytext guarda a classe do token".
- (baixa) Nenhum lugar avisa que, sem a regra `.`, o Flex copia o caractere não casado para a saída. É surpresa clássica no TP.

**Geral**
- (média) Quase tudo é toque em alternativa, sem pedido de justificativa. Sugestão barata: depois de cada "Sua vez", um REVELAR "Diga em voz alta por quê, depois toque para conferir".

## Lacunas

1. **Thompson (ER → AFN).** Só existe como lista de regras no FORMAL. O jogo diz "cai muito" e manda "decorar" ε-fecho({0}) = {0,1,2,4,7}, mas nunca desenha o AFN de 11 estados de (a|b)\*abb.
2. **Escrever** uma ER a partir de uma descrição (identificador, número com ponto, comentário, "termina em 1"). Hoje há só uma pergunta de múltipla escolha no SOCRATIC.
3. **Do AFD ao código do scanner** (laço com switch ou tabela, último final lembrado, recuo). É o que o TP "léxico por AFD" pede. Não avaliei o guia de TP, que é outro arquivo.
4. Classes de caracteres e ERs de tokens reais (strings, comentários de bloco) antes do Flex.
5. Minimização por partições (o método do Dragão) e recuperação de erro léxico: só nos textos FORMAL. Basta um aviso na trilha.

## Nota por assunto

| Assunto | Correção | Didática | Prova | Frase |
|---|---|---|---|---|
| PHASES | 4 | 4 | 3 | Claro e bem sequenciado; a ficha cobra o que a trilha não ensina, e falta produzir a saída de cada fase. |
| TOKENS | 4 | 5 | 4 | Melhor aula do bloco; faltam "padrão" e "atributo" na trilha, e o glossário se contradiz. |
| REGEX_DFA | 4 | 3 | 3 | Simular AFD está ótimo; operadores de ER corridos numa cena, sem união, precedência nem Thompson. |
| NFA_TO_DFA | 5 | 3 | 3 | Conteúdo impecável; a trilha não faz o aluno errar e consertar o fecho, nem montar a tabela. |
| DFA_MINIMIZATION | 4 | 3 | 3 | Texto WORKED exemplar; a aula animada usa AFD incompleto, exemplos rasos e não desenha o mínimo. |
| LEX_FLEX | 5 | 4 | 4 | Regras do Flex certas e bem atacadas; notação `[a-z]` e yytext sem pré-treino. |

## As 5 melhorias que eu faria primeiro

1. **Dar a AFN→AFD e a Minimização a trilha de TOKENS** ("Fazer com ajuda" e prática), com o aluno preenchendo a tabela Dtran (A, B, C…) e a escadinha de pares com o número da rodada, e terminando no desenho do AFD mínimo.
2. **Trocar os "Sua vez" por casos que tenham a dificuldade real.** Em AFN→AFD, (ab|a)c, em que o fecho depois do move muda o conjunto, e um move vazio (∅). Na minimização, o AFD do livro (3 rodadas) e a\*b\* com estado morto.
3. **Consertar as contradições:** glossário de token; "só ela consulta a tabela de símbolos"; AFD "no máximo uma" × "exatamente uma" seta; "três de análise, três de síntese"; cena 18 da minimização; a pegadinha dos gêmeos.
4. **Refazer o trecho de operadores de ER** (uma cena por operador, união e precedência com pergunta, classes `[a-z]`) e acrescentar um exemplo resolvido de Thompson para (a|b)\*abb, desenhado, seguido de um para completar.
5. **Alinhar ficha e Bugs com a trilha obrigatória.** Ou as fichas de PHASES, REGEX_DFA e LEX_FLEX só cobram o que a aula animada ensinou, ou a aula ganha uma cena para cada termo cobrado (tabela de símbolos, front-end, padrão, yytext, "regex não conta"). Incluir a ponte "AFD aceita a cadeia inteira × scanner fica com o último final".
