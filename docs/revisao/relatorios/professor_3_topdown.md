# Relatório do professor — Região 5 (Templo Top-Down)

## Veredito

O bloco é tecnicamente sólido onde mais importa: refiz todos os FIRST, FOLLOW, tabelas e traços e **não achei nenhum conjunto, célula ou passo de pilha errado**. As receitas de FIRST, FOLLOW e da tabela estão completas e iguais às do Dragão; ε fora do FOLLOW, $ no inicial, produção anulável nas colunas do FOLLOW e "não existe coluna ε" são atacados de frente.
O maior problema é de **coerência em Erros sintáticos**: a abertura, o primeiro exemplo, a pegadinha e o clipe mandam sincronizar no `;`, e a receita da mesma aula (e todos os textos) manda parar antes, no token do FIRST. Na mesma entrada, uma tela marca `num` como certo e outra como errado.
O segundo problema é de **cobertura dos casos difíceis na trilha animada**: anulável no meio (FOLLOW), produção anulável que não é literalmente ε (tabela), conflito FIRST/FIRST e ponto fixo em que a ordem importa só aparecem nos textos opcionais.
Resposta à pergunta da tarefa: com a trilha **mais** os textos "exemplo resolvido" e "definição de prova", o aluno faz a questão clássica em gramáticas bem-comportadas (ε no fim da regra). Só com a trilha animada, não: ele toca em fichas, nunca escreve um conjunto inteiro nem dá o veredito "não é LL(1)" sozinho.

## Erros técnicos

| Assunto | Onde | Trecho | Problema | Conserto | Grav. |
|---|---|---|---|---|---|
| ERROR_RECOVERY | Abertura; aula cenas 6–7; pegadinha "Parar ou seguir" cena 3; clipe | `x = * 3 ;` → certo = `;`, errado = `3`. Pegadinha `id = = num ; id` → certo = `;`, errado = `num` | Contradiz a receita da própria aula (cena 10, passo 2) e a cena 15, onde, na mesma entrada `id = = num ;`, certo = `num` e errado = `;`. Com Val no topo: descarta `*`, e `3` ∈ FIRST(Val) → para ali. "Tudo até ali vai fora" (cena 7) é falso | Trocar as entradas para que não haja token de FIRST antes do `;`: `x = * ; y = 2 ;` (aula), `id = = ; id` (pegadinha), `x = = ; y = 3;` (abertura), `= = ;` (clipe). Cena 7: "O vezes vai fora. O ponto e vírgula pode vir depois de Val: Val sai da pilha e o ponto e vírgula casa." | alta |
| FOLLOW | Pegadinha "O épsilon que muda o FOLLOW", cena 1 | "B pode sumir. Isso muda quem vem depois de A." | Falso: em `S → A b, A → a B, B → d \| ε`, FOLLOW(A) = FOLLOW(B) = {b} com ou sem `B → ε`. B herda porque está no fim, não porque some. A pegadinha não testa o caso que anuncia | Trocar a gramática: `S → A B c`, `A → a`, `B → d \| ε`; pergunta "o que entra no FOLLOW de A?"; certo = d e c; errado = ε. Fala: "B pode sumir. Então, depois de A, pode vir o d de B ou direto o c." | média |
| RECURSIVE_DESCENT | Aula cenas 9 e 21; texto SOCRATIC (último revelar) | "se a gramática for LL(1): sem recursão à esquerda e fatorada" | Os dois-pontos leem como definição. Contradiz LL1_TABLE (Bug "Toda gramática sem recursão à esquerda é LL(1)" = falso; ficha; pegadinha) | "Ele só escolhe com um token se a gramática for LL(1). Tirar a recursão à esquerda e fatorar é o primeiro passo; depois confira a tabela." | média |
| LL1_TABLE | Pegadinha "Duas regras, uma casa só" | colunas `a b $` | A gramática `S → A b, A → b \| ε` não tem terminal `a`. Coluna de terminal inexistente | Colunas `b $` | baixa |
| ERROR_RECOVERY | Aula cena 13 | "Outra gramática: comandos de atribuição." | É a mesma gramática das cenas 4–9 | "A mesma gramática, outra entrada, agora com dois erros." | baixa |
| FOLLOW | Abertura | `E -> T + E \| T` … "O mais. Olhe o que está à direita de T na regra." | $ também segue T (E → T, E inicial). A frase reforça o erro de olhar só a direita | "O mais. E há outro, que a aula mostra: quando T fecha a frase, vem o fim da entrada." | baixa |
| FOLLOW | Bug do álbum, 3ª frase | "Se A fecha a regra de B, FOLLOW de A entra em FOLLOW de B" | Certo como falso, mas com as letras invertidas em relação à receita (`A → α B β`) e à ficha | "Em A → α B, o FOLLOW de B entra no FOLLOW de A. → É ao contrário: o de A entra no de B." | baixa |
| LL1_PARSING | Brinquedo "Opere a pilha"; prática cena 5 | "Pilha vazia e entrada no fim"; "Sem recursão, sem volta atrás." | A aula diz que sobra `$` na pilha; "recursão" confunde com recursão à esquerda | "Só o cifrão na pilha e na entrada"; "Sem volta atrás: cada token é lido uma vez só." | baixa |

**Conferido refazendo a conta, tudo correto:** FIRST e FOLLOW de todas as gramáticas da aula, dos textos e dos flashcards (entre elas expressões com E'/T', declarações `D→TL;`, `S→ABc`, `S→(S)S|ε`, `S→AaAb|BbBa`); as tabelas (`E/X/T` e declarações com 7 células, expressões com 13) e os quatro conflitos mostrados; as três condições LL(1) dos textos SOCRATIC e FORMAL; os traços `id + id`, `id , id`, `id + id * id` (17 passos), `int id , id ;` (11 linhas) e `( ) ( )`; os pseudocódigos; os traços de pânico dos textos, incluindo a cascata em `id = num num ;`.

## Problemas de didática e estrutura

**FIRST** (bom: o "Sua vez" traz o caso novo, anulável na frente, e a pegadinha traz a cadeia de anuláveis)
- [média] O ponto fixo é só falado. Os dois exemplos são resolvidos na ordem das dependências ("Comece por A, porque S depende dele") e a "rodada a mais" nunca muda nada. O aluno conclui que basta uma passada na ordem certa. Acrescentar uma cena em que a regra é visitada cedo demais: "Comecei por E → T X, mas o saco de T ainda está vazio. Nada entra. Na segunda rodada, entra."
- [média] FIRST de uma **sequência** (`FIRST(α)`) aparece pela primeira vez no resumo (cena 21) e é o que a tabela usa; só o texto FORMAL ensina. Incluir uma cena: "FIRST de um pedaço de regra, como X b: mesma receita, da esquerda para a direita."
- [baixa] Cena 1 fala em "topo da pilha", que só é ensinado três assuntos depois. Trocar por "O parser precisa trocar T por uma das regras e o próximo token é id."

**FOLLOW**
- [alta] A trilha animada nunca exercita o **anulável no meio** (`A → α B C γ`, C anulável: entra FIRST(C) sem ε **e** o que vem depois). O segundo exemplo repete o caso do primeiro (resto anulável → herda), um nível mais fundo. O caso está só nos textos (STORY, WORKED, SOCRATIC). Com a pegadinha trocada (ver tabela) isso se resolve.
- [média] Cena 9 olha uma ocorrência de T; T também aparece em `X → + T X`. A receita diz "procure onde ele aparece", e o erro de prova é parar na primeira ocorrência. Acrescentar: "T aparece também em X → + T X. Confira: dá o mesmo conjunto."
- [média] Cena 18 entrega o FOLLOW(T) pronto; o aluno toca duas vezes na aula inteira do segundo exemplo. Pedir FOLLOW(T) antes.

**LL1_TABLE**
- [média] Em todos os exemplos (aula e seis textos) a produção anulável é literalmente `→ ε`. O passo 2 diz "se α é anulável", mas nunca se vê uma regra como `S → A B` (A e B anuláveis), que entra nas colunas do FIRST **e** nas do FOLLOW. É pegadinha clássica de prova.
- [média] O único conflito da trilha é FIRST×FOLLOW. FIRST×FIRST (prefixo comum, recursão à esquerda) só está nos textos, e a aula não liga de volta à Ponte ("antes da tabela, tire a recursão e fatore").
- [média] As três condições LL(1) só existem nos textos. Pôr na ficha.
- [alta, alinhamento] A aula entrega FIRST e FOLLOW prontos ("O FOLLOW de R é só o cifrão"). Em consulta pontual ao código (`content/Trilha.kt`, `GRAMATICAS_LL`), as oito gramáticas do "Fazer com ajuda" são todas LL(1) e nenhuma tem regra que começa com não-terminal anulável. Logo, na prática gerada o veredito é sempre "é LL(1)" e o caso difícil do FIRST não aparece.

**LL1_PARSING** (bom: os erros de ordem de empilhar e de avançar na expansão vêm com a correção)
- [média] O formato de prova `Pilha | Entrada | Ação` só aparece no texto WORKED; a aula mostra a pilha em pé. Fechar o primeiro exemplo com a tabela completa de `id + id`, que hoje é pulada na cena 8 ("O resto segue igual"), inclusive o `X → ε` com `$`.

**RECURSIVE_DESCENT**
- [média] Pré-requisito declarado: só FIRST. A aula abre com a tabela LL(1), diz "é o mesmo analisador preditivo" e a alternativa ε depende do FOLLOW. Declarar LL1_PARSING.
- [média] Três nomes para a mesma função (`casa`, `casaToken` no Bug, `match` na ficha e nos textos), três para a variável (`tok`, `tk`, `look`) e três linguagens (C na aula, Kotlin com `fun`/`when` nos textos, pseudocódigo). O TP é em C: unificar em C, `casa` e `tok`.
- [média] O corpo de `casa()` e a função principal com a checagem do `$` não aparecem na aula animada (a ficha lista "esquecer de verificar $" como erro comum). Uma cena: `void casa(int t) { if (tok == t) tok = proximo(); else erro(); }` e `int main() { tok = proximo(); E(); if (tok != FIM) erro(); }`.
- [baixa] A aula ensina "épsilon no else vazio"; o texto WORKED diz que testar o FOLLOW "vale ponto". Dizer na cena 12: "Deixar vazio funciona; testar o FOLLOW acusa o erro mais cedo."
- [a verificar] No código, o "Fazer com ajuda" deste assunto usa a mesma simulação de pilha do anterior (`simLL`, ramo `else`). Se for isso, o aluno não escreve nenhuma função antes do Desafio.

**ERROR_RECOVERY**
- [média] A ficha cobra o que a trilha animada não ensina: quatro tipos de erro, quatro estratégias, prefixo viável, `error` e `yyerrok`. Isso está só nos textos. Além disso, Bison é da região 7, e lá `error`/`yyerrok` não aparecem (busquei). Mover esses dois cartões para a região 7 e acrescentar à aula uma cena com as quatro estratégias.
- [baixa] Receita, passo 1: "descarta" sem objeto. Escrever "descarta o token da entrada; a pilha fica".

**Notação entre versões**: a aula animada usa `X`, `R`, `Y`; textos, ficha e flashcards usam `E'`, `T'` ("M[E', )]", "FIRST(E) na gramática E → T E' …", sem a gramática no cartão). A região 4 usa `A'`. Escolher uma e mostrar a gramática no cartão.

## Lacunas

1. **Questão integrada, produzindo a resposta**: dada a gramática, listar anuláveis, FIRST, FOLLOW, tabela e veredito apontando a célula e as duas produções. Incluir gramáticas que **não** são LL(1) e uma que precisa ser transformada antes.
2. **Passo zero "liste os anuláveis"**: só está numa dica do texto FORMAL; deveria abrir a receita do FIRST.
3. **Else pendente como conflito LL(1)** (`S' → else S | ε`, conflito em M[S', else]): exemplo do Dragão que liga ambiguidade a tabela; não aparece.
4. **Cadeia de anuláveis no FOLLOW** (`A → B C D`, C e D anuláveis).
5. **Descida recursiva com laço** (`while (tok == '+')` no lugar de `X()`), que é como o TP costuma ser escrito.

## Nota por assunto

| Assunto | Correção | Didática | Prova | Frase |
|---|---|---|---|---|
| FIRST | 5 | 4 | 4 | Receita completa e casos difíceis cobertos; falta ver o ponto fixo trabalhar e o FIRST de sequência. |
| FOLLOW | 4 | 3 | 3 | Receita certa; pegadinha com premissa falsa e anulável no meio só nos textos. |
| LL1_TABLE | 4 | 4 | 3 | Regra do FOLLOW e "não há coluna ε" muito bem; falta regra anulável não vazia, conflito FIRST×FIRST e veredito negativo na prática. |
| LL1_PARSING | 5 | 4 | 4 | Traços corretos, erros certos atacados; falta o formato de três colunas na aula. |
| RECURSIVE_DESCENT | 4 | 3 | 3 | Ideia clara; nomes e linguagens misturados, `casa()` e `$` final ausentes, pré-requisito errado. |
| ERROR_RECOVERY | 3 | 3 | 4 | Textos corretos e ricos; a trilha animada se contradiz sobre onde sincronizar. |

## As 5 melhorias que eu faria primeiro

1. **Erros sintáticos**: trocar as quatro entradas que sincronizam no `;` (abertura, cenas 5–7, pegadinha, clipe) para ficarem de acordo com a receita FIRST/FOLLOW.
2. **Prática integrada**: um exercício "gramática → anuláveis → FIRST → FOLLOW → tabela → é LL(1)?" em que o aluno preenche tudo, e pôr no sorteio gramáticas com regra que começa em anulável (`S → A b | c`, `A → a A | ε`) e gramáticas não LL(1) (`S → A b`, `A → b | ε`; `S → a b | a c`).
3. **FOLLOW**: substituir a pegadinha por `S → A B c` e mostrar todas as ocorrências do não-terminal na aula.
4. **Tabela**: acrescentar um exemplo com produção anulável não vazia (`S → A B`) e um conflito FIRST×FIRST ligado à Ponte; pôr as três condições LL(1) na ficha.
5. **Descida recursiva**: corrigir o pré-requisito, unificar `casa`/`tok` em C em todas as versões, mostrar `casa()` e o `main` com o teste do `$`, e reescrever "LL(1): sem recursão à esquerda e fatorada".
