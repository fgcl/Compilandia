# Relatório do aluno 3: Região 5, Templo Top-Down

## Como foi estudar isto

Eu continuaria usando, mas não só a trilha animada. Cada aula cabe em uns 3 minutos e o "por quê" de quase todas me convenceu. O problema é que a trilha me deixa bom em tocar na peça certa e fraco em resolver sozinho. Na prova de bolso acertei o FIRST, errei o FOLLOW sem perceber e levei o erro para a tabela. O que mais me travou foi a receita do FOLLOW, escrita com `A → α B β`, e as aulas animadas só mostrarem um símbolo depois do não-terminal. O que salva está nos textos (estilo "Exemplo resolvido"), que eu só abriria apertando "Não entendi". A aula de Erros se contradiz entre o primeiro e o segundo exemplo.

## Onde eu travei

**FIRST**
- (média) Cena 3 define "o conjunto dos terminais que podem aparecer no começo" e três cenas depois o ε entra no saco. ε é terminal? Eu mudaria para: "os tokens que podem aparecer no começo, mais o ε quando o símbolo pode sumir inteiro".
- (média) Cena 9, a receita não tem linha para a regra que é só ε. Isso só aparece na dica da cena 6 ("quando a regra é épsilon, o próprio épsilon entra"). Eu poria como passo 0.
- (média) Cenas 18 e 21: "Mais uma rodada não muda nada". Nos dois exemplos a segunda rodada nunca muda nada, então não vi para que serve repetir.

**FOLLOW**
- (alta) Cena 10, passo 2: "A → α B β: FIRST(β) sem ε entra em FOLLOW(B)". Ninguém diz o que são α e β. Na Região 4, α e β eram outra coisa ("põe o beta na frente"). As letras A e B da receita batem com não-terminais de verdade (a própria pegadinha usa `S -> A b`, `A -> a B`). As frases faladas são muito melhores que a tela: "o FIRST do que vem depois" (cena 12) e "se o resto da regra é anulável, ou não existe, B herda o FOLLOW do lado esquerdo" (cena 13). Eu escreveria essas na tela.
- (alta) Nenhum exemplo animado tem dois símbolos depois do não-terminal. Foi aí que errei a prova de bolso (abaixo). O caso só está no texto História ("Se B falta, depois de A vem direto o c").
- (média) Cenas 7 a 9: três ideias novas em três cenas (vizinho, herança no fim, herança porque o vizinho some), e logo depois a fórmula.
- (média) Cena 18: "O FOLLOW de T tem mais, fecha parêntese e cifrão". Numa gramática nova, com nomes novos (R, Y), esse conjunto chega pronto. Eu nunca calculo um FOLLOW inteiro sozinho.
- (média) Bug do álbum: "[F] Se A fecha a regra de B, FOLLOW de A entra em FOLLOW de B". A receita na tela diz "FOLLOW(A) entra também" com as letras no papel contrário. Quem decorou a receita marca verdade e erra. Trocaria as letras do Bug para X e Y.

**Tabela LL(1)**
- (média) Cena 7 pede as casas do FOLLOW de X, mas a tela só tem gramática e tabela. Tenho que lembrar o conjunto da aula anterior, e a dica entrega: "O FOLLOW de X é fecha parêntese e cifrão". Eu deixaria FIRST e FOLLOW visíveis ao lado da tabela.
- (média) Cena 5: "O FIRST de T X". FIRST de uma sequência nunca foi ensinado na aula animada do FIRST.
- (média) A regra anulável é sempre o ε escrito. Uma regra como `B → C`, com C anulável, nunca aparece.

**Analisador preditivo (pilha)**
- (média) Cena 8, "O resto segue igual", pula do segundo passo para o "aceita" da cena 9. O formato de prova (Pilha | Entrada | Ação, topo de que lado) só existe no texto Exemplo resolvido.

**Descida recursiva**
- (média) Cena 12: "Se o não-terminal não é anulável, o último else não pode ficar vazio. Ele avisa o erro". Mas o `T()` da tela tem no else a segunda regra, sem erro nenhum. Não vejo o corpo de `casa`, de onde vem `tok`, nem quem chama `E()` e confere o `$`. Isso está só nos textos, e é o parser do meu TP.
- (média) O pré-requisito é só FIRST, mas a cena 1 abre com a tabela LL(1) e a cena 3 fala da "pilha do analisador". A ficha diz que o ε se escolhe pelo FOLLOW; a aula diz "épsilon no else".
- (baixa) Os textos usam `fun` e `when (look)`, que não são C nem Java.

**Erros e recuperação**
- (alta) Cena 6: com `x = * 3 ; y = 2 ;`, o certo é `;` e o `3` é errado ("Tudo até ali vai fora"). Cena 15: com `id = = num ;`, o certo é `num` e o `;` é errado. É a mesma situação (Val no topo, lixo, um número, ponto e vírgula) com respostas opostas, e ninguém avisa que são duas versões do modo pânico.
- (média) Cena 10, passo 1: "Casa vazia, não-terminal no topo: descarta." Descarta o quê? Sugiro: "jogue fora tokens da entrada, um por um, até cair no passo 2 ou no 3".
- (média) A ficha cobra prefixo viável, quatro tipos de erro, quatro estratégias, `error` e `yyerrok` do Bison. Nada disso está na aula animada.

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| topo da pilha (do parser) | FIRST c.1, FOLLOW c.1 | Pilha c.3–4, três assuntos depois |
| anulável | FIRST c.6, só na fala de acerto | ali mesmo; fora do glossário |
| α, β | FIRST c.21, FOLLOW c.10, Tabela c.9 | nunca nesta região |
| FIRST de uma sequência ("FIRST de T X") | Tabela c.5 | só no texto Formal do FIRST |
| M[X, )] | Tabela c.10, Descida c.2 | só na ficha e nos textos |
| ponto fixo | ficha do FIRST | dentro de um flashcard e no texto Visual |
| $ | ficha do FIRST ("$ pode estar em FIRST?") | FOLLOW c.5, assunto seguinte |
| variável (= não-terminal) | fichas de FIRST, Tabela, Pilha, Descida | só num parêntese do texto Formal da Região 3 |
| expandir | Pilha, botão da c.7 | nunca; o narrador diz "trocar pela regra" |
| preditivo | Pilha c.3 | nunca dizem que é porque prevê a regra |
| casa / casaToken / match; tok / tk / look | Descida: aula, Bug, pegadinha, ficha, textos | três nomes para cada coisa |
| X, depois R e Y, depois E' e T' | aulas animadas contra fichas e textos | a mesma gramática com três nomes |
| top-down, bottom-up | título da região; ficha da Descida | top-down nunca; bottom-up é da Região 6 |
| prefixo viável, nível de frase, correção global, yyerrok | ficha de Erros | só nos textos; Bison é da Região 7 |

## Prova de bolso

Gramática que inventei: `S → A B d`, `A → a A | ε`, `B → b B | C`, `C → c | ε`. Usei só as receitas das aulas animadas.

- **FIRST.** C = {c, ε}, lembrando da cena 6, porque a receita não tem o passo. Em `B → C`: o passo 2 dá c, o passo 3 manda "olhar o próximo símbolo" e não há próximo. Um símbolo só conta como "todos anuláveis"? B é anulável sem ter `| ε` escrito? Chutei que sim: B = {b, c, ε}. S = {a, b, c, d}, graças à pegadinha `S -> A B c`. Acertei, com dois chutes.
- **FOLLOW(A)**, em `S → A B d`. Foi onde a receita me largou. O β é "B d", dois símbolos. Pus FIRST(B) sem ε = {b, c}. Depois pensei como na cena 9 ("como X é anulável, T pode ficar no fim da regra"): B é anulável, então vale o passo 3 e entra o FOLLOW de S. Respondi {b, c, $}. O certo é {b, c, d}: B some, mas o d fica. Nada na aula animada diz "se o vizinho some, olhe o seguinte; só herda se chegar ao fim da regra".
- **Tabela.** Com o FOLLOW errado, `A → ε` foi para b, c, $ e M[A, d] ficou vazia. Minha tabela rejeita a frase `d`, que é válida, e nenhum conflito me avisou. Em `B → C` eu preencheria só a coluna c e esqueceria a coluna d.

| Assunto | Exercício | Resultado | O que faltou |
|---|---|---|---|
| FIRST | os 4 conjuntos da gramática acima | consigo | passo da regra ε; caso `B → C`; uma rodada que mude algo |
| FOLLOW | os 4 conjuntos | não consigo | dois ou mais símbolos depois do não-terminal; α e β em português |
| Tabela LL(1) | tabela da mesma gramática | em parte | regra anulável que não é ε; nunca fiz gramática → FIRST → FOLLOW → tabela de uma vez |
| Pilha | simular `( id )` | em parte | formato de prova; exemplo sem pulo |
| Descida recursiva | funções da gramática acima | em parte | `casa`, `tok`, main com `$`; quando o else é erro |
| Erros | pânico em `id = * num ;` | em parte | qual das duas versões vale |

## Diversão e ritmo

- Funcionou: a abertura do FOLLOW ("Sumir só está certo se fecha parêntese puder aparecer logo depois de X. O FIRST não responde isso") me fez querer a aula. O clique veio na Tabela, cena 7: "Com fecha parêntese ou cifrão na entrada, o parser apaga X". As melhores perguntas foram a da Descida, cena 17 ("quantas vezes o parser chama a função R?"), e a dica da Pilha, cena 18 ("O cifrão está no FOLLOW de R"), que faz pensar sem entregar. O FOLLOW volta no modo pânico (Erros, cena 8) e amarra a região.
- Cansou: as seis aulas têm o mesmo esqueleto. Em todas há "Um colega fez assim. Certo ou errado?" e a resposta é sempre "errado" (conferi: 26 de 26 no jogo inteiro). Em todas há "Qual passo fez isso? Toque", que testa ler a lista. Várias perguntas vêm respondidas na fala: FIRST c.6 ("Uma regra começa com mais, a outra é só épsilon. Toque nos dois"), Tabela c.15 ("O FOLLOW de R é só o cifrão. Toque na casa"), FIRST c.17 (c ou ε).
- Ritmo: a maior sequência só ouvindo tem 6 cenas (FOLLOW 1–6, Tabela 1–6, Descida 1–6). A tela da receita fica parada por 4 ou 5 cenas (FIRST 9–13).
- Personagens: na trilha, Byte e a professora são a mesma voz neutra, e o Bug só existe nas manchetes ("O Bug comeu o FIRST"). Nos textos eles têm graça: o bandejão do RU, o golem ("Paciência… é… meu… forte."), a lareira ("Por isso o Clang me contratou").
- Textos: escolheria o Exemplo resolvido, que leva a mesma gramática `D → T L ;` do FIRST até a descida recursiva, e as Perguntas guiadas da Tabela. Pularia o Formal no celular e o Truque de memória do FIRST ("Entra e pronto, minha gente" é infantil). "Dólar, Vizinho, Herança" e "FIRST Fica, FOLLOW Foge" eu usaria. As seis telas que abri são legíveis, inclusive a do código.
- Encaixe dos seis: cada abertura puxa a aula anterior, e isso funciona. Falta uma cena-mapa (conjuntos → tabela → pilha → o mesmo em código → erro) e alguém dizer por que é "top-down". A árvore da Região 3 não aparece em nenhuma aula animada.

## Nota por assunto

| Assunto | Clareza | Diversão | Pronto para a prova | Uma frase |
|---|---|---|---|---|
| FIRST | 4 | 3 | 4 | A pegadinha `A B c` ensina mais que a receita. |
| FOLLOW | 3 | 3 | 2 | O porquê é ótimo; a receita com α e β me derrubou. |
| Tabela LL(1) | 4 | 3 | 3 | Fácil porque os conjuntos chegam prontos. |
| Pilha | 4 | 4 | 3 | Operar é gostoso; falta escrever no papel. |
| Descida recursiva | 4 | 3 | 3 | Entendi a ideia; não sei montar o programa inteiro. |
| Erros | 3 | 3 | 2 | Dois exemplos, duas respostas diferentes. |

## Desconfio que está errado

- Erros, cenas 6–7: pela receita da própria aula (cena 10), com Val no topo e `* 3 ;`, o parser pararia no `3` (FIRST de Val), não no `;`.
- Tabela, pegadinha "Duas regras, uma casa só": colunas `a b $`, mas a gramática `S -> A b`, `A -> b | ε` não tem terminal a; a linha S fica vazia.
- Descida, cena 12: a fala sobre o último else não bate com o `T()` da tela. Na cena 8, "cada if testa o FIRST", mas o else não testa `(`.
- Pilha, prática: "Pilha vazia" contra "cifrão com cifrão"; "Sem recursão" dito pela professora logo antes da aula de descida recursiva.
- FIRST, cena 3 e glossário ("Os tokens que podem começar um símbolo"): não mencionam o ε.
- Ficha do FIRST: flashcard "FIRST(E) na gramática E → T E' …?" traz reticências no lugar da gramática.

## As 5 coisas que eu mudaria primeiro

1. Reescrever a receita do FOLLOW sem α e β: "Ache B do lado direito e olhe o que vem logo depois. Terminal: entra. Não-terminal: entra o FIRST dele, sem ε; se ele pode sumir, olhe o seguinte. Chegou ao fim da regra: B herda o FOLLOW de quem está à esquerda da seta." Incluir um exemplo do tipo `S → A B d`.
2. Pôr na trilha um exercício completo (gramática → FIRST → FOLLOW → tabela → simulação), como o `D → T L ;` dos textos, e uma cena-mapa da região.
3. Consertar a aula de Erros: o primeiro exemplo deve seguir a mesma regra do segundo, ou avisar que é a versão simples.
4. Padronizar nomes: X ou E'; casa ou match; tok ou look; expandir ou trocar; variável ou não-terminal; código dos textos em C.
5. Perguntas: o "colega" às vezes precisa estar certo; parar de narrar a resposta; deixar FIRST e FOLLOW visíveis na Tabela; no FIRST, mostrar uma rodada que muda algo e uma regra `B → C`.
