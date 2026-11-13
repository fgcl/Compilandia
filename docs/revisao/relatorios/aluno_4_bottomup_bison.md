# Relatório do aluno 4: Fortaleza Bottom-Up (região 6) e Oficina Yacc/Bison (região 7)

## Como foi estudar isto

Eu continuaria usando, mas a região 6 é onde eu mais correria risco de largar. Empilha-reduz e Bison são boas aulas: começam com um problema que eu já conhecia (a recursão à esquerda que trava o preditivo; as 112 casas para conferir à mão) e me fazem tocar logo. O meio da escalada é o que trava. A aula de itens monta o estado zero e um goto e declara "Os estados estão prontos". A aula de SLR mostra uma tabela que aparece pronta, sem os estados de onde saiu. A aula de LALR mostra itens LR(1) com o lookahead já colado, sem dizer de onde ele vem. Em cada degrau eu entendo a ideia, mas a receita não me deixa fazer a questão de prova sozinho. Quem salva são as aulas de texto "Exemplo resolvido" e a prática "Rode a tabela SLR", que estão fora da aula animada. Os nomes também pesam: fecho/fechamento/closure, goto/desvio, ok/acc/aceita, e "núcleo", "goto", "cifrão" e "lookahead" com dois sentidos cada.

## Onde eu travei

**Empilha-reduz**
- **Alta. O glossário ensina o erro que a aula proíbe.** Glossário: handle é "O pedaço do topo da pilha que casa com o lado direito de uma regra." O Bug do álbum marca como falso: "Handle é qualquer pedaço igual a um lado direito." Trocaria por: "O pedaço do topo que é certo reduzir agora. Casar com um lado direito não basta."
- **Média. A receita é circular** (cena 11): "1. Sem handle no topo: empilha um token. 2. Handle no topo: reduz". Ela não diz como eu sei se é handle. As duas gramáticas da aula (E → E + n | n e S → ( S ) | id) nunca têm o caso duvidoso. A cena 21 só o cita na dica: "Pense em id mais id vezes id. Com E mais T no topo e vezes na entrada", uma gramática que a aula não mostrou. Eu poria a pegadinha "Reduzir cedo demais" dentro da aula, antes dos erros comuns, e diria na receita: "Por enquanto você decide olhando a gramática; a tabela das próximas aulas decide por você."
- **Média. Por que um segundo método?** As cenas 1 a 3 convencem ("O preditivo decide a regra cedo demais", "ler primeiro e escolher a regra depois"). Mas na região 4 eu aprendi a consertar a recursão com E'. Falta uma frase: "Você pode reescrever a gramática, como na Ponte, mas ela fica feia. Aqui a gramática fica como você escreveu." Isso só aparece no texto (História: "tem recursão à esquerda, e aqui ninguém se importa!").
- **Baixa.** Cena 10: "É esse o R do nome LR." É a primeira vez que "LR" aparece, o L não é explicado, e a derivação não é mostrada. Bastava pôr na tela `E ⇒ E + n ⇒ n + n` ao lado das reduções.
- **Baixa.** A cena 12 se responde lendo: a tela mostra "E | $" e a opção diz "Só o símbolo inicial e $".
- **Baixa.** A ficha cobra "As 4 ações" e dois tipos de conflito; a aula diz "três ações" e não fala de conflito (só a aula de SLR, cena 8).

**Itens LR(0), closure e goto**
- **Alta. A aula para antes de terminar a conta.** Cena 12 dá o algoritmo inteiro em uma fala: "Repita para cada símbolo depois de um ponto, até não surgir estado novo." O "Sua vez" (cenas 13 a 17) faz o fecho do estado zero, o goto por id e o goto por abre parêntese, e encerra: "Os estados estão prontos." Não estão: faltam o goto por S, o estado com S → ( S • ) e o final. A aula nunca mostra que ler "(" de novo cai num estado que já existe. Esse caso só aparece na ficha ("Criar um estado novo para um conjunto de itens que já existe") e no texto. Eu continuaria a cena 17 por mais três toques e incluiria a pergunta "esse conjunto já existe?".
- **Alta. O S' chega só de ouvido e parece outra coisa.** Cena 10: "A receita começa com uma regra extra, S linha vai para S". Não está na tela da receita e não tem motivo. Na região 4, "E'" era o não-terminal novo que tira a recursão (E → n E'). Eu acharia que é o mesmo truque. Poria na tela: "0. Crie S' → S. É só a linha de chegada: quando S' → S • aparecer, a frase foi aceita."
- **Média. Fecho em cascata só aparece na aula de tabelas** ("A caixa do estado", cena 4: "Um desses itens tem o ponto antes do T. Então entram as regras do T."). Na animada básica, o fecho sempre para no primeiro nível. Essa aula de tabelas é a melhor do assunto e devia vir antes do "Sua vez".
- **Média.** No brinquedo da cena 9 das tabelas, "monte o fecho de outra gramática", a tela (pela transcrição) só mostra "I0{E' → • E}". A gramática nova não aparece, e eu tenho de adivinhá-la pelas teclas.
- **Baixa.** "Neste estado chega um n" (cena 6) usa "estado" três cenas antes de "Cada conjunto de itens é um estado" (cena 9).

**Tabela SLR(1) e conflitos**
- **Alta. A tabela cai do céu.** Cena 4: "n + $ / 0 s2 / 1 s3 ok / 2 r2 r2". Não há os estados 0, 1 e 2 em forma de caixas de itens, nem a gramática numerada. O "s3" aponta para um estado que não está na tabela. Eu acabei de aprender a montar caixas e a aula não liga as duas coisas. Trocaria as cenas 4 a 6 por: as três caixas à esquerda, e cada casa nascendo de um item (ponto antes de n vira s2; ponto no fim vira r2 nas colunas do FOLLOW).
- **Alta. O exemplo confunde s com r.** Cena 5: "s com um número manda empilhar e ir para esse estado. r com um número manda reduzir por essa regra." Na tela os dois números são 2 (s2 e r2), e a lista "1: E -> E + n 2: E -> n" só aparece na prática. Eu sairia achando que o 2 é a mesma coisa nos dois. Poria a lista numerada na tela desde a cena 4.
- **Média. "Goto" muda de sentido.** Cena 6: "Empilhar sai direto dos gotos do autômato." Cena 18: "Coluna de não-terminal é a parte GOTO". Diria: "Seta com terminal vira s. Seta com não-terminal vai para a parte GOTO da tabela."
- **Média. A pilha troca de conteúdo sem aviso.** Em empilha-reduz ela tinha símbolos ("E + n"). Na prática, cena 2: "O parser guarda uma pilha de estados". Falta: "Cada estado na pilha vale por um símbolo lido. Por isso a redução tira um estado para cada símbolo do lado direito."
- **Média. O "por quê" é abstrato.** Cena 2: "a redução leva a um beco sem saída", sem exemplo. O texto visual tem o exemplo certo (estado com E → T • e T → T • * F, vem "*").
- **Baixa.** A pegadinha afirma "O igual está no FOLLOW de R" sem mostrar a conta (o texto visual mostra).

**LR(1) e LALR(1)**
- **Alta. Ninguém diz de onde vem o lookahead.** Cena 4: "Aqui estão quatro itens LR(1)." A receita (cena 9) manda "1. Monte os estados LR(1), com lookahead", e isso a aula não ensina. A ficha cobra "FIRST(β a)" e o erro "Calcular o lookahead dos novos itens com FOLLOW em vez de FIRST(β a)". Só os textos "Exemplo resolvido" e "Definição de prova" ensinam. Eu poria ao menos uma cena: "O item novo herda o que vem depois do não-terminal no item pai; se não vem nada, herda o lookahead do pai."
- **Média. A aula não usa o exemplo que ela mesma preparou.** A pegadinha anterior termina em "O LALR olha mais fino e resolve." A aula de LALR abre com "E -> n •" e "r ?", sem conflito na tela. Devia abrir com o estado { S → L • = R ; R → L • } e mostrar que R → L • só carrega $.
- **Média. Item e estado se misturam.** Cena 12: "Dois estados LR(1) de outra gramática", e a tela mostra três itens soltos. Não sei quais são os dois estados.
- **Média. Os nomes.** LALR nunca é soletrado na animada (só no texto: "Look-Ahead LR"). O 0 e o 1 dos parênteses nunca são explicados. Uma linha resolve: "O número é quantos tokens da entrada ele espia para reduzir: LR(0) nenhum, os outros um." O melhor mapa está escondido no sexto estilo de texto: "força: LR(0) < SLR(1) < LALR(1) < LR(1) / tamanho: LR(0) = SLR(1) = LALR(1) < LR(1)" e a imagem dos óculos. Eu levaria as duas linhas para o resumo da aula animada.
- **Baixa.** A cena 10 entrega a resposta na dica ("É o passo que junta estados").

**Yacc/Bison**
- **Média. "Cifrão" virou outra coisa.** Até aqui cifrão era o fim da entrada. Cena 5: "Cifrão um é o valor do primeiro símbolo." A aula não diz que valor é esse nem de onde vem o de NUM. Sugestão: "Aqui o cifrão não é o fim da entrada. $1 é o número que o primeiro símbolo carrega; o do NUM vem do léxico."
- **Média.** Na aula "Lendo o bison -v", cena 5: "Com qualquer token, ele reduz." Acabei de aprender que só se reduz no FOLLOW ou no lookahead. Faltou meia frase ("o Bison economiza: se for erro, ele descobre no passo seguinte"). "$end" nunca é explicado.
- **Baixa.** %right só aparece no resumo (cena 23). %prec e %nonassoc só na ficha e nos textos.
- **Baixa.** "Quanto mais embaixo, mais forte" aparece na abertura, nas cenas 8, 21, 22 e 23, na pegadinha, no clipe e no Bug. Depois da quarta vez cansa.

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| ascendente / bottom-up / "de baixo para cima" | Empilha-reduz, cena 4; nome da região; ficha | três nomes; nunca se diz que são a mesma coisa |
| LR (o L) | Empilha-reduz, cena 10 | só no texto formal |
| (0) e (1) em LR(0), SLR(1), LR(1) | títulos das aulas | nunca na animada |
| S' ("regra extra") | Itens, cena 10 (só na fala) | flashcard e texto História |
| fecho / fechamento / closure | Itens, cena 5; tabelas, cena 5; pegadinha | explicado, mas com três nomes |
| goto / desvio / "transitar" | Itens, cena 7; ficha; nome do jogo | explicado; três nomes e dois sentidos (SLR, cenas 6 e 18) |
| "•" e "." para o ponto | aula usa •; abertura, pegadinha e clipe usam . | nunca se diz que é o mesmo |
| núcleo (kernel) | Itens, flashcard | só no texto formal; em LALR (cena 5) significa outra coisa |
| ACTION | SLR, prática, cena 1 | de passagem; a ficha usa "ACTION[i, a]" e "Ii" sem explicar |
| ok / acc / accept | SLR, cena 4; textos; ficha | três nomes |
| regra numerada (o 2 de r2) | SLR, cena 4 | prática, cena 1 |
| lookahead (dentro do item) | LALR, cena 3 | na região 5 era "o próximo token da entrada" |
| "símbolos de previsão" | LALR, Bugs do álbum | nunca (terceiro nome do lookahead) |
| LALR (sigla) | SLR, pegadinha, cena 4 | nunca soletrada na animada |
| FIRST(β a) | LALR, ficha | só nos textos |
| Bison | Itens, tabelas, cena 8; SLR, clipe | região 7 |
| Yacc | Bison, cena 3 | texto História |
| $default, $end | Bison, tabelas, cenas 4 e 6 | $default por alto; $end nunca |
| prefixo viável | textos da região 6 | nunca de verdade |

## Prova de bolso

| Assunto | Exercício | Resultado | O que faltou |
|---|---|---|---|
| Empilha-reduz | Tabela pilha/entrada/ação de id * id em E → E + T \| T; T → T * F \| F; F → id | Em parte | Com "$ T" e "*" na entrada, a receita não diz se reduzo E → T. Só o texto resolvido avisa. |
| Itens LR(0) | Estados de S → A b; A → a A; A → c | Em parte | Fiz o I0 (o fecho em cascata só graças à aula de tabelas) e os gotos por S, A, a e c. No estado de "a", lendo "a" de novo, a aula não diz que eu volto ao mesmo estado; eu criaria um repetido e não saberia quando parar. Também não sei numerar nem o que fazer com S' → S •. Com o texto resolvido, chego aos 7 estados. |
| Tabela SLR | Preencher a tabela dessa gramática, dados os estados | Em parte | As três regras bastam para as casas. Faltou a numeração das regras e montar uma tabela inteira uma vez. Rodar uma entrada eu consigo, pela prática. |
| LR(1)/LALR | (a) Fundir dois estados e apontar o conflito; (b) montar o I0 LR(1) | (a) consigo; (b) não consigo | A origem do lookahead não está na aula animada. |
| Bison | .y de soma e multiplicação; valor de 2+3*4 com os %left trocados | Consigo | Menos unário e compilar com Flex só pelo texto resolvido. |

## Diversão e ritmo

- **Funcionou:** a pergunta "sem fim" (Empilha-reduz, cena 2) mostra o problema antes de dar o nome. No Bison, a sequência das cenas 15 a 19 (um conflito, a conta dá 4, uma linha conserta, dá 2) foi onde eu mais senti que aprendi; foi o clique do bloco. "112 casas" me deu vontade de conhecer a ferramenta. A prática "Rode a tabela SLR" foi o segundo clique: "Três símbolos no lado direito, três estados fora."
- **Textos:** eu escolheria sempre o "Exemplo resolvido" (é o que deixa pronto para a prova) e o de perguntas guiadas de LALR ("Você acabou de inventar o LALR(1)"). A açaiteria e o ônibus "com destino na testa" são boas imagens. Pularia o formal na primeira leitura. As "Conversas ao pé da lareira" entre LL e LR têm graça que ensina.
- **Cansou:** as aulas têm de 18 a 23 cenas, cerca de 3 minutos cada, e cabem numa sessão. A maior sequência só ouvindo são as cenas 3 a 7 de Empilha-reduz e 6 a 9 de LALR. A prática "Opere a esteira" repete o mesmo n + n da aula. Na aula de LALR os toques são fáceis demais (comparar dois textos na tela), então eu acerto tudo sem aprender a montar nada.
- **Degrau alto:** Itens, cena 5 (fecho) para cena 7 (goto, fechamento e estado novo em duas falas). SLR, cena 3 para cena 4 (da ideia do FOLLOW para uma tabela pronta).
- **Personagens:** na animada, Byte e professora são vozes neutras. A personalidade está nos textos ("Tô ferrado?", a Pilha que fala devagar). O tom é adequado para universitário. As telas que abri são legíveis no celular.

## Nota por assunto

| Assunto | Clareza | Diversão | Pronto para a prova | Uma frase |
|---|---|---|---|---|
| Empilha-reduz | 4 | 4 | 3 | Entendi a máquina; não sei decidir o caso difícil sem a tabela. |
| Itens LR(0) | 3 | 3 | 2 | Sei o que é o ponto e o fecho; não sei terminar o autômato. |
| Tabela SLR | 3 | 4 | 3 | Rodar a tabela ficou claro; montá-la, não. |
| LR(1)/LALR | 3 | 2 | 2 | Sei o discurso (junta núcleos, cria R/R), não sei fazer a conta. |
| Yacc/Bison | 4 | 5 | 4 | A melhor do bloco: problema, erro visível, conserto. |

## Desconfio que está errado

- Empilha-reduz, glossário: a definição de handle é a frase que o Bug do álbum marca como falsa.
- SLR, cena 4: tabela com o título "Tabela SLR(1)" mostra só 3 dos 5 estados e nenhuma coluna E; "s3" aponta para um estado ausente.
- Bison, tabelas, cena 9: "0 conflitos · 7 estados"; o Bison de verdade talvez conte um estado a mais por causa do $end.
- Bison, tabelas, cena 5: "Com qualquer token, ele reduz" parece contradizer o LALR; deve ser a redução padrão do Bison, mas a aula não diz.
- Empilha-reduz, cena 2: o preditivo "troca para sempre"; na região 5 a tabela LL(1) acusaria conflito, não laço.

## As 5 coisas que eu mudaria primeiro

1. **Itens LR(0):** terminar o autômato de S → ( S ) | id no "Sua vez", com um toque de "esse estado já existe", e pôr o S' → S como passo 0 na tela, com o motivo.
2. **SLR:** fazer a tabela nascer das caixas de itens, com as regras numeradas na tela e um exemplo em que o número do s seja diferente do número do r.
3. **LALR:** abrir com o conflito de S → L = R da pegadinha anterior e incluir uma cena sobre a origem do lookahead; levar as duas linhas "força / tamanho" para o resumo.
4. **Nomes:** escolher um por conceito (fecho, goto, aceita) e avisar das trocas de sentido ("núcleo", "goto", "cifrão", "lookahead"); consertar o glossário de handle.
5. **Empilha-reduz:** pôr o caso "E + T com * na entrada" dentro da aula e uma frase dizendo por que vale a pena um método novo se eu já sei tirar a recursão à esquerda.
