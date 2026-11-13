# Relatório do aluno 2: Vale das Gramáticas e Ponte da Transformação

## Como foi estudar isto

Eu continuaria usando. As quatro aulas animadas cabem em uma sessão de 10 minutos cada (20 a 22 cenas) e todas têm uma "Sua vez" em que eu faço o conserto passo a passo. A passagem do léxico para as gramáticas existe, mas dura duas falas. O que mais me travou foi a Ponte: ela conserta gramáticas para um tipo de parser ("descendente", "preditivo", "LL(1)") que o jogo só apresenta na região 5. Entendi a mecânica dos consertos, mas não quem é o cliente deles. Também saí com uma contradição na cabeça: o Vale manda pôr a recursão à esquerda para o menos funcionar, e a Ponte manda tirá-la. E a aula de ambiguidade abre com `2 + 3 * 4` e termina sem mostrar a gramática que resolve esse exemplo.

## Onde eu travei

### Gramáticas e derivações

- **Média. Cena 3, a ponte com o léxico.** "E expressão regular não basta, porque não sabe casar parênteses." A tela mostra `n + n` e `n + +`, sem parêntese nenhum. A região 2 já tinha plantado isso (Bug da Regex que Conta), então não é mudança brusca de assunto, mas é afirmação sem exemplo. Eu poria na tela `( ( n ) )` e `( ( n )` e diria: "Regex não sabe contar quantos parênteses abriu. Para conferir que cada um fecha, precisamos de uma ferramenta nova."
- **Média. Cena 8, pergunta antes da explicação.** "Numa derivação mais à esquerda, qual E eu troco agora?" O termo nunca tinha aparecido; a dica é "O nome já conta". Acerto pelo nome, sem saber por que existe essa regra. A aula animada nunca diz por que a ordem importa (só o texto Perguntas guiadas diz). Frase que eu poria antes: "Tem dois E. Para todo mundo derivar igual, combinamos: troco sempre o da esquerda primeiro."
- **Média. Cena 9, passo engolido.** "Ele vira n. Depois troco o outro, e sobra a frase n mais n." A tela pula de `E + E` para `n + n`; a forma `n + E` não aparece. É justamente o passo que a prova cobra, linha por linha.
- **Média. A receita (cena 11) não diz como escolher a regra.** "2. Troque o não-terminal mais à esquerda." Trocar por qual lado direito? Na "Sua vez" a dica é "Olhe a primeira palavra da meta", mas a gramática usada (`Decl`) não tem recursão. Falta um passo: "Compare com a frase alvo e escolha a regra que faz aparecer o próximo pedaço dela."
- **Média. Pegadinha "Duas derivações, uma árvore", cena 4.** "São duas derivações diferentes. Isso é ambiguidade?" Na trilha, essa pegadinha vem antes do assunto que ensina ambiguidade. É chute. Eu a moveria para a trilha de Ambiguidade.
- **Média. Prática "Derive até chegar", cena 5.** "Quando você escolhe certo sem pensar, a tabela LL(1) já está na sua cabeça." Não sei o que é LL(1). A mesma prática usa `X -> + T X | ε` sem dizer o que ε faz numa regra (só "épsilon para acabar").
- **Baixa. Cenas 12 e 20.** "Qual passo escolhe o E da esquerda?" Basta achar a palavra "esquerda" na receita. Não testa nada.
- **Baixa. Bug do álbum e ficha** usam "forma sentencial", "sentença", "⇒lm", "⇒rm", "S ⇒* α", que a aula animada não ensina (ela diz "frase" e "forma").

### Árvores e ambiguidade

- **Alta. Cena 9 e resumo (cena 22): o conserto da precedência nunca é mostrado.** "O conserto é reescrever a gramática, com um nível por precedência, ou declarar a precedência no Bison." A aula mostra só o conserto do menos (`E -> E - n | n`). A gramática E/T/F aparece apenas nos textos, num flashcard e num Bug ("Com níveis E, T e F ela deixa de ser"). Saí sem saber consertar o exemplo de abertura. Eu trocaria a segunda "Sua vez" (ou acrescentaria três cenas): mostrar `E -> E + T | T` e `T -> T * n | n`, montar a árvore de `n + n * n` e perguntar "dá para pôr o mais embaixo?".
- **Alta. Cena 17, três palavras novas numa fala.** "A recursão fica só à esquerda, e o menos associa à esquerda." "Recursão" numa regra e "associa" nunca foram definidos (recursão à esquerda só é explicada na região 4). Frase nova: "Na regra antiga, os dois lados do menos podiam crescer. Na nova, só o lado esquerdo cresce; o direito é um n e pronto. Então a conta só pode se agrupar pela esquerda: (9 - 5) - 2."
- **Média. Cena 5.** "Nesta árvore o vezes fica mais fundo. Ele é calculado primeiro." Aceito como regra, mas falta o motivo: "para fazer a conta de cima preciso antes do resultado da conta de baixo".
- **Média. A receita (cena 10) não ajuda a achar a frase.** "1. Escolha uma frase da linguagem." Qual? Falta a dica que o texto Passo a passo visual dá: usar uma frase com dois operadores e tentar cada um na raiz.
- **Média. Pegadinha "O else solto", cena 5.** "O Bison resolve empilhando o else." Não sei o que é empilhar aqui (é assunto da região 6). As duas árvores também não aparecem; a tela só mostra `if 1`, `if 2`, `else`.
- **Baixa.** A abertura com o Bug e a cena 2 fazem a mesma pergunta (resposta 20).
- **Baixa. Bugs e ficha:** "separando comandos fechados e abertos" (no texto formal o nome é `matched_stmt`/`open_stmt`), "indecidível" e "LL(1) ou LR(1)" não são explicados em lugar nenhum até aqui.

### Recursão à esquerda

- **Alta. Contradição com o Vale.** Em Ambiguidade (cena 17) o conserto era deixar "a recursão só à esquerda", para o menos associar "como na matemática". Aqui (cena 8): "Só mudou o lado da recursão." Então o menos passou a associar à direita? Estraguei o que consertei? A aula animada não toca nisso. O único lugar que toca é o texto formal: "a associatividade fica 'escondida' em A' e é recuperada com atributos herdados", que para mim é grego. Precisa de uma cena: "A árvore mudou de forma. As frases são as mesmas, e a conta certa a gente recupera depois, na Cidade Semântica."
- **Alta. Cena 1, o cliente do conserto não foi apresentado.** "Num parser descendente, cada não-terminal vira uma função." Nenhuma aula anterior diz que existem tipos de parser. `casa('+')` não é explicado. E a função mostrada só tem a alternativa `E + n`; como programador de C eu pergunto "cadê o if para o `| n`?". Mesmo assim, este é o melhor "por quê" do bloco: vi `E();` na primeira linha e pensei em estouro de pilha. Faltam duas frases: "No Templo você vai escrever o parser assim, uma função por não-terminal. A gramática está certa; quem engasga é esse jeito de programar."
- **Média. Cena 7, degrau alto.** Aparecem juntos o `E'` (só lido como "L linha" na cena 13), o ε dentro de uma regra e a recursão à direita. Nenhuma frase é derivada com a gramática nova antes da receita. Eu mostraria `n + n` saindo: `E ⇒ n E' ⇒ n + n E' ⇒ n + n`, com a fala "o E linha virou nada: é isso que o épsilon quer dizer".
- **Média. Cena 9, α e β.** "1. Separe: A → A α tem laço, A → β não tem." Ninguém diz o que são as letras gregas. O texto Passo a passo visual diz bem: "α = o que vem DEPOIS do S recursivo e β = a alternativa que NÃO começa com S". Essa frase devia estar na aula.
- **Média. Prática "Rode a versão consertada".** "Topo não terminal: troca pela regra. Topo igual à entrada: casa." Pilha de quê? O que é o `$` na tela? É o analisador de pilha da região 5.
- **Baixa.** O clipe diz "Por isso o LL(1) não aceita", e um dos jogos da região é o "Sudoku LL(1)".

### Fatoração à esquerda

- **Média. Cenas 1 a 3 e 7, a regra do jogo não foi dita.** "Vendo só o id, qual regra o parser escolhe?" Respondo "não dá para saber", mas ninguém explicou que o parser só pode olhar um token e não pode voltar atrás. Depois vêm "parser preditivo" e "É o que uma gramática LL(1) exige", sem definição. Frase para a cena 1: "O parser do Templo lê um token por vez e precisa escolher a regra na hora, sem voltar atrás."
- **Média. Caso que a aula não mostra: resto vazio.** A ficha cobra "Fatore A → x y z | x y" e o erro "Esquecer o ε quando uma alternativa é exatamente o prefixo". A receita diz "3. Em A', ponha o resto de cada regra" e não diz o que pôr quando não sobra nada.
- **Baixa. Cena 14.** Os três botões (`( C''`, `( ) C''`, `( C''` sem o `= n`) são quase iguais no celular; na captura precisei comparar símbolo por símbolo.
- **Baixa. Bug do álbum:** "Conflito com FOLLOW, por exemplo, continua lá." FOLLOW é da região 5.
- O que funcionou: "ponho o prefixo comum em evidência" (cena 5) e a segunda rodada de fatoração na "Sua vez" (cenas 13 a 15).

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| parser (= analisador sintático) | Gramáticas, cena 1 (e desde a região 2) | nunca ligada a "análise sintática" da região 1 |
| livre de contexto | Gramáticas, cena 4 | só no texto História e num flashcard |
| símbolo inicial | Gramáticas, cena 7 | só no texto História ("o da primeira linha") |
| derivação mais à esquerda | Gramáticas, cena 8 (pergunta) | cena 8, na resposta |
| forma / forma sentencial | Gramáticas, cena 12; Bug do álbum | só nos textos Visual e Formal |
| ε dentro de regra | Gramáticas, prática cena 1 | Recursão, cena 19, em parte |
| LL(1) | Gramáticas, prática cena 5; Recursão, clipe; Fatoração, cena 7 | região 5 |
| ambiguidade | Gramáticas, pegadinha cena 4 | assunto seguinte |
| precedência "em níveis", E/T/F | Ambiguidade, cena 9 | só nos textos |
| recursão (em regra), associa | Ambiguidade, cena 17 | região 4 / nunca |
| Bison, "empilhando" | Ambiguidade, cena 9 e pegadinha cena 5 | regiões 6 e 7 |
| indecidível | Ambiguidade, ficha | nunca |
| parser descendente | Recursão, abertura e cena 1 | região 5 |
| `casa(...)` | Recursão, cena 1 | nunca |
| α, β | Recursão, cena 9 | só no texto Visual |
| E' ("E linha") | Recursão, cena 7 | cena 7, como "não-terminal novo" |
| LR | Recursão, Bug e ficha | região 6 |
| pilha, `$` | Recursão, prática cena 4 | região 5 |
| preditivo, "token de previsão" | Fatoração, cenas 3 e 7 | região 5 |
| FIRST, FOLLOW | Fatoração, Bug e texto Visual | região 5 |

Nomes que mudam: "frase" (aula), "sentença" (ficha) e "cadeia" (textos); "regra", "produção", "alternativa" e "opção"; e cinco nomes na Ponte para o mesmo cliente: "parser descendente", "de cima para baixo", "preditivo", "LL(1)" e "descida recursiva".

## Prova de bolso

| Assunto | Exercício | Resultado | O que faltou |
|---|---|---|---|
| Gramáticas | Derivação mais à esquerda e árvore de `id + id * id` em E/T/F | consigo em parte | Como escolher entre `E + T` e `T`; como desenhar a árvore (a aula mostra pronta); derivação à direita só no texto Exemplo resolvido |
| Ambiguidade | Provar que `S -> S S \| a` é ambígua e reescrever `E -> E + E \| E * E \| n` | provar: em parte; reescrever: não consigo | Dica para achar a frase; a gramática em níveis não está na aula animada |
| Recursão | Tirar a recursão de `E -> E + T \| E - T \| T` | consigo em parte | Dois α na mesma regra só aparecem no texto Exemplo resolvido; lendo ele, consigo |
| Fatoração | Fatorar `S -> i E t S \| i E t S e S \| a` | consigo em parte | O que escrever quando o resto é vazio (ε) |

## Diversão e ritmo

- Funcionou: calcular `9 - 5 - 2` na árvore e dar 6 (Ambiguidade, cena 15) foi o momento em que "duas árvores, duas contas" clicou. A frase `int id id ;` ficar fora da linguagem e a professora dizer "É assim que o parser acusa erro de sintaxe" (Gramáticas, cenas 18 e 19) ligou o assunto ao compilador. A função em C que chama `E();` antes de ler qualquer coisa fala a minha língua.
- Os textos têm a graça que falta nas animações: "Para tirar o documento A, apresente o documento A", o robô do banco que trava em "boleto…", a calculadora de camelô, a PILHA gritando "ESTOUREI". Eu abriria História primeiro e Exemplo resolvido antes da prova. Pularia Formal e, na Ponte, o Passo a passo visual, que pergunta "Por que [S', e] tem conflito?" com resposta em FIRST e FOLLOW.
- Cansou: nas animações o Byte é um narrador sem personalidade e a professora entra para uma frase de definição. O Bug é só uma manchete. O tom não é infantil; é seco.
- Maior trecho só ouvindo: 5 cenas (Gramáticas, 3 a 7; Ambiguidade, 3 a 7). Aceitável.
- Ambiguidade e Fatoração quase não têm prática na trilha (aula, pegadinha e desafio).
- As telas que abri são legíveis; as árvores com nós coloridos ajudam.

## Nota por assunto

| Assunto | Clareza | Diversão | Pronto para a prova | Uma frase |
|---|---|---|---|---|
| Gramáticas e derivações | 4 | 3 | 3 | Sei trocar, não sei escolher a regra nem desenhar a árvore sozinho. |
| Árvores e ambiguidade | 3 | 4 | 2 | Entendi o problema; o conserto da precedência ficou de fora. |
| Recursão à esquerda | 3 | 3 | 3 | Decoro a receita, mas acho que ela desfaz o conserto do Vale. |
| Fatoração à esquerda | 4 | 3 | 3 | Mecânica clara; falta o caso do ε e dizer quem é o parser. |

## Desconfio que está errado

- Recursão, cena 1: a função `E` mostrada só implementa `E + n` e ignora `| n`. Um parser de verdade seria assim?
- Recursão, cena 8: "Só mudou o lado da recursão", sem avisar que a forma da árvore (e a associatividade do Vale) muda junto.
- Ambiguidade, Bug do Else Solto: "tem duas árvores nessa gramática", mas nenhuma gramática é mostrada.
- Fatoração, Bug: "Fatorar acrescenta um nível na árvore" é dado como verdade sem a aula ter mostrado árvore nenhuma.
- Fatoração, pegadinha "Quando fatorar não basta": no fim, fatorar basta, depois de substituir. O título confunde.

## As 5 coisas que eu mudaria primeiro

1. Abrir a Ponte com uma cena que apresenta o cliente: "No Templo você vai programar um parser que lê um token por vez, de cima para baixo, sem voltar atrás. Ele engasga com dois formatos de regra. Aqui a gente reescreve esses formatos sem mudar as frases." E usar um nome só para ele.
2. Em Recursão, responder à contradição com o Vale: a árvore muda, a linguagem não, e a conta certa é recuperada depois.
3. Em Ambiguidade, mostrar a gramática em níveis consertando `n + n * n`, e explicar "associa" e "recursão" com palavras comuns.
4. Tirar das práticas, pegadinhas e Bugs o que só é ensinado depois (LL(1), pilha e `$`, "empilhando", FOLLOW, a pergunta de ambiguidade dentro de Gramáticas) ou mover para o assunto certo.
5. Completar as receitas: em Gramáticas, como escolher a regra olhando a frase alvo e mostrar todas as formas intermediárias; em Fatoração, o caso do resto vazio; em Recursão, dizer o que são α e β e derivar uma frase com a gramática nova.
