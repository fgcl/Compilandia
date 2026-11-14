# Revisão das aulas: o que foi achado e o que já foi feito

Revisão feita em 12 e 13 de novembro de 2026 por revisores "professor" e "aluno" (IA), um par por bloco de regiões. Este arquivo junta os 10 relatórios num só lugar, sem repetir o mesmo achado. A lista para marcar está em [TAREFAS.md](TAREFAS.md). A pesquisa que embasa as decisões está em [PESQUISA.md](PESQUISA.md).

Os relatórios são lista de suspeitas, não veredito: modelo fingindo ser aluno entende mais do que um aluno real (veja [PESQUISA.md](PESQUISA.md), seção 7). Por isso cada achado diz se foi **conferido** (alguém olhou a transcrição ou o código) ou se veio só do relatório.

## Situação dos relatórios

| Bloco | Professor | Aluno |
|---|---|---|
| 1. Porto e Floresta Léxica (regiões 1 e 2) | lido | lido |
| 2. Vale e Ponte (regiões 3 e 4) | lido | lido |
| 3. Templo Top-Down (região 5) | lido | lido |
| 4. Bottom-Up e Bison (regiões 6 e 7) | lido | lido |
| 5. Semântica e Forja (regiões 8 e 9) | **falta** | lido |
| 6. Seu compilador (série em C e Guia do TP) | **falta** | lido |

Todos os 10 relatórios gravados estão completos (cada um termina em "As 5 coisas/melhorias"). Faltam só os professores dos blocos 5 e 6.

## Onde está cada coisa

- `docs/revisao/relatorios/`: os 10 relatórios, com o trecho citado e o conserto proposto para cada achado.
- `docs/revisao/roteiro_professor.md` e `roteiro_aluno.md`: as instruções de cada revisor. Os roteiros citam o caminho antigo das transcrições (`/tmp/.../scratchpad/dump/`); hoje elas estão em `docs/revisao/transcricoes/`.
- `docs/revisao/transcricoes/`: tudo o que o aluno lê, em texto, por região. `*_A_trilha.md` traz abertura, aula animada, práticas, pegadinhas, clipes, Bugs e ficha. `*_B_texto.md` traz as aulas "Ler com calma". **Foram geradas antes das correções de 13/11**: os trechos consertados abaixo ainda aparecem lá na versão antiga.
- `docs/revisao/medidas.py`: conta cenas, perguntas e posição da resposta certa nas transcrições.

## Feito em 13/11 (segunda sessão)

1. **Recuperação de erros sem contradição.** As quatro entradas que sincronizavam no `;` com um token do FIRST de `Val` antes dele foram trocadas: aula (`x = * ; y = 2 ;`), pegadinha "Parar ou seguir" (`id = = ; id =`), clipe (`= * = ;`) e abertura (`x = = ;  y = 3;`). Agora o `;` é mesmo onde o parser se segura pelas regras 1 e 3 da receita.
2. **Handle no glossário** diz o que a aula ensina: "o lado direito no topo da pilha que é certo reduzir agora", com o aviso de que nem todo lado direito no topo é handle.
3. **Itens LR(0), aula de tabelas, cena 7**: a pergunta diz que o item `E' → E •` aceita, não reduz. Antes ela pedia "caixas com o ponto no fim" e marcava I1 como errado.
4. **Token tem um sentido só**: o token é o que o scanner entrega, a classe com o lexema junto (como no Dragão, par nome e atributo). Aula, ficha, Bug, clipe e glossário dizem o mesmo. Em `x = 10` há três tokens.
5. **"Parser" apresentado** na cena 1 de Tokens: "A fase seguinte é o analisador sintático, que todo mundo chama de parser."
6. **Correções menores conferidas**: ambiguidade não depende de as contas darem resultados diferentes (cena 10); folhas podem ser ε; "duas derivações pela esquerda **da mesma frase**"; glossário de recursão à esquerda ("o lado direito começa com o próprio símbolo da esquerda"); fatoração com uma regra só ("maior prefixo comum a todas do grupo") e a nota sobre a ordem do livro; "a pilha cresce para cima" só no desenho, e desce na memória do MIPS.
7. **Alternativas embaralhadas na tela** (`learning/OrdemDasOpcoes.kt`): "Preveja", abertura com o Bug e pergunta do Caça-Conflitos. Números ficam em ordem crescente, e "nenhuma", "as duas" ou "depende" ficam no fim. Com o embaralho, a certa cai em cada uma das 4 posições perto de 1/4 das vezes; antes caía na última 0 vezes em 139.
8. **O colega acerta em 13 das 29 aulas.** Na parte Erros, "Um colega disse isto" agora tem afirmação certa em LL(1), preditivo, LALR, Bison, fases, AFN→AFD, Flex, fatoração, escopo, três endereços, registro de ativação, alocação e AST. O teste `OrdemDasOpcoesTest` falha se a proporção sair de 35% a 65%.

## Feito em 13/11 (terceira rodada)

1. **E7 a E13 conferidos e consertados.** A tabela SLR sai inteira do motor (5 estados, GOTO e regras numeradas). O `x = 2;` do escopo foi para dentro do `main`. A vivacidade da alocação anda de baixo para cima, como a própria aula manda. O `ifFalse` do texto WORKED segue a forma da prática. A aula de MIPS não mostra mais `$t1` ao lado de `t1`. O `calc.y` da série em C mostra os `%left` que o motor usa. A cola do Guia ganhou `move`, e a caixa de pseudo-instruções ficou completa.
2. **Associatividade na Ponte.** Três cenas depois do primeiro exemplo de recursão à esquerda: a árvore de `9 - 5 - 2` na gramática velha (pende para a esquerda, dá 2) e na nova (pende para a direita; lida ao pé da letra daria 6), e a pergunta "o menos passou a juntar pela direita?". Resposta: não, o laço do parser mantém a conta. As árvores são conferidas contra as gramáticas em `AulasConsertadasTest`.
3. **Receita do FOLLOW sem α e β.** Cada passo diz para que serve: "Fim da entrada", "Depois de B: FIRST sem ε. Se some, o próximo", "Fim da regra: herde o FOLLOW da esquerda", "Repita".
4. **Fita da aula**: nome de parte que não cabe ganha reticências e um respiro, em vez de encostar no seguinte ("ReceitSua vez").

## Feito em 13/11 (quarta rodada)

1. **Fim dos erros da seção 1.** O WORKED da fatoração e a dica de prova usam a regra da ficha ("maior prefixo comum a todas do grupo") e dizem que começar pelo prefixo de só duas, como faz o livro, chega à mesma gramática. A pegadinha das fases não diz mais que "só a semântica consulta a tabela de símbolos" (o scanner também consulta). A definição de AFD ficou uma só: uma seta por símbolo em cada estado; a que não aparece vai para o estado morto, que o desenho esconde.
2. **Fala de acerto variada.** 240 das 259 falas de acerto começavam com "Isso". A tela já comemora o acerto (som, Byte pulando), então a maioria agora começa direto pela explicação, e uma em cada três abre com "Certo", "Exato", "Boa", "Acertou" ou "É isso", em rodízio. Nas aulas de texto, o "**Isso!**" fixo virou um de quatro elogios. O `FalaDeAcertoTest` falha se duas falas seguidas abrirem igual ou se "Isso" passar de 20%.
3. **FOLLOW com o caso difícil.** Três cenas novas com `S → B C d`, `C → c | ε`: o aluno toca em tudo que entra no FOLLOW de B (c e d). É o caso em que o revisor aluno errou. O motor confere a resposta em `AulasConsertadasTest`.

## Feito em 13/11 (quinta rodada)

1. **O aluno toca já na cena 2.** FIRST, FOLLOW, tabela LL(1), descida recursiva, itens LR(0) e SLR abriam com 5 ou 6 cenas só de ouvir. Agora cada uma pergunta logo depois da situação de abertura algo que o aluno já consegue responder: qual regra serve com aquele token, o que falta chegar à pilha, que casa da tabela consultar, que tokens vêm depois de E. Na descida, todas as casas da tabela são tocáveis, para o destaque não entregar a resposta.
2. **FIRST com o caso difícil.** Quatro cenas com `S → B d`, `B → C | b`, `C → c | ε`: B some sem épsilon escrito, o d entra no FIRST de S, e, na ordem das regras, a primeira rodada deixa S só com o d. É o que justifica o passo "repita". O motor confere cada conjunto e a ordem das rodadas.
3. **Fases como no livro.** A análise vai até o código intermediário; a síntese parte dele (otimização e geração). Antes, a aula dizia "três de análise, três de síntese".
4. **Série em C 5.** Os fios `yyparse()` e `NUM 42` saem do alto das caixas; o rótulo e as fichas ficam no vão, e não atrás de `calc.l`.

## Feito em 13/11 (sexta rodada)

1. **Pergunta na cena 2 em mais sete aulas.** Recuperação de erros, atributos, escopo, otimização, LALR, AST e MIPS abriam com três ou quatro passos só de ouvir. Agora cada uma pergunta logo depois da abertura: qual erro o compilador simples nunca avisa, qual instrução soma dois float, de qual declaração é o `i` dentro de `g`, quantas vezes o laço calcula `2 * 3`, em quais tokens o SLR reduz `E -> n •`, quais pedaços de `a + ( b )` mudam a conta, e onde a conta `total + 1` pode acontecer. Nenhuma tem só duas opções.
2. **Tabela LL(1) com o caso difícil.** Seis cenas no fim da Sua vez. `S → A B`, `A → a | ε`, `B → b | ε`: a regra não tem ε escrito, mas some inteira, e o aluno toca nas três colunas (a e b pelo FIRST, $ pelo FOLLOW de S). Depois `S → a b | a c`: as duas regras caem em (S, a), o conflito FIRST × FIRST, e a fatoração da Ponte conserta. A tabela da tela sai do motor (`tabelaLl1DaAula`), e `AulasConsertadasTest` confere as casas, o conflito e a fatoração.
3. **Recursão à esquerda com dois alfas.** `E → E + T | E − T | T`: o aluno toca nas duas alternativas com laço e escolhe o E' certo entre os dois erros de prova (esquecer o menos, esquecer o ε). `A9GramaticaFactsTest` confere com o motor que a conversão bate e que as duas opções erradas perdem frases.

## Feito em 13/11 (sétima rodada)

1. **Pegadinha do FOLLOW que testa o que anuncia.** Era `S → A b`, `A → a B`, `B → d | ε`, em que o FOLLOW de B é {b} com ou sem `B → ε`. Agora é `S → A B c`, `A → a`, `B → d | ε`, e a pergunta é o FOLLOW de A: o d de B e o c, porque B pode sumir. ε e $ ficam como opções erradas. O motor confere em `AulasConsertadasTest`.
2. **Fatoração com resto vazio.** Quatro cenas no fim da Sua vez com `S → if S | if S else S | x`: o maior começo comum é `if S`, a primeira regra não deixa sobra, e a sobra vazia vira ε em S'. A professora avisa que a gramática fatorada ainda não é LL(1): com else na entrada, S' tem duas regras (o else solto). `A9GramaticaFactsTest` confere a fatoração, o conflito e que, sem o ε, o if sem else some.
3. **Gramáticas sem LL(1) no "Fazer com ajuda".** Três gramáticas com conflito (`S → A b`, `A → b | ε`; `S → A a`, `A → a A | ε`; `S → id = E | id`) entram no sorteio de FIRST e FOLLOW. Na tabela LL(1), o quarto problema, que a pessoa faz sozinha, é sempre uma delas. A casa com conflito agora sempre vira pergunta, e a resposta certa são as duas regras, como a casa mostra (antes era só a primeira).

## Feito em 13/11 (oitava rodada)

1. **AFN→AFD com o caso difícil.** Quatro cenas no fim da Sua vez com o AFN de `(ab|a)c`. De {0,1,4}, lendo a, o move dá {2,5} e o ε-fecho acrescenta o 6; lendo b, o move é vazio, e a aula diz que essa seta vai para o estado morto. O motor calcula os conjuntos da tela, e `AulasConsertadasTest` confere cada um.
2. **Minimização com o AFD do livro.** Oito cenas com o AFD de `(a|b)*abb`. Cada casa da tabela mostra a rodada em que foi marcada: E com todos na 1, D com A, B e C na 2, A com B e B com C na 3. O aluno marca as rodadas 2 e 3 e vê que uma marca puxa outra; A e C ficam em branco e se juntam. As rodadas saem do `Minimizer`, e o teste confere as três. O `a*b*` com estado morto ficou para depois.
3. **Pegadinha do handle.** Dizia que reduzir cedo "faz a conta na ordem errada". Em `E → E + T`, `T → T * F`, reduzir `E + T` com `*` na entrada deixa E na pilha e nenhuma regra continua E com vezes: o parser rejeita uma frase certa. Uma cena nova mostra a pilha com E e o aviso "nenhuma regra com E *". O teste confere que `*` não está no FOLLOW de E.

## Feito em 13/11 (nona rodada)

1. **Minimização com estado morto.** Quatro cenas depois do AFD do livro, com `a*b*`: o estado morto M aparece no desenho porque a tabela precisa dele. Na rodada 1, M cai com P e com Q; na 2, P com Q cai por causa de P com M. Nada se junta, e a aula diz por quê: sem o morto, P e Q pareceriam iguais, e o AFD aceitaria `b a`. O `Minimizer` confere as rodadas.
2. **Pré-requisitos.** Recursão à esquerda pede ambiguidade (usa `E → E + T | T`); tabela LL(1) pede recursão e fatoração; descida recursiva pede o analisador preditivo, porque abre com a tabela.
3. **Descida recursiva com `casa()` e `main`.** Três cenas mostram o corpo de `casa()` e o `main` que lê o primeiro token, chama E e testa o `$`. A pergunta: em `id id`, E volta depois do primeiro id; o `main` avisa o erro. O teste simula o código da aula e confere que E volta com id na entrada. A fala "LL(1): sem recursão à esquerda e fatorada" virou "isso ajuda, mas não basta: a tabela não pode ter conflito".

## Feito em 13/11 (décima rodada)

1. **As três condições LL(1) na ficha, e FIRST e FOLLOW à vista.** A ficha da tabela ganhou três flashcards: FIRST(α) e FIRST(β) sem terminal comum; no máximo uma alternativa deriva ε; se β deriva ε, FIRST(α) e FOLLOW(A) sem terminal comum. No primeiro exemplo da aula, FIRST(T), FIRST(X) e FOLLOW(X) ficam na tela enquanto a tabela enche, calculados pelo motor.
2. **O autômato LR(0) inteiro.** A aula terminava dizendo "os estados estão prontos" com três estados na tela. Agora quatro cenas desenham os seis estados de `S → ( S ) | id` a partir do `LRAutomaton`, com os itens do estado do `(` embaixo. O aluno toca no destino do goto por `(` nesse estado (ele mesmo: conjunto igual é o mesmo estado, e por isso a construção termina) e no estado que aceita, o de `S' → S •`.
3. **ε como produção, forma sentencial e sentença.** Duas cenas com `S → a S b | ε`: a regra vazia apaga o S, e o aluno escolhe a sentença `a b` entre `a S b` (forma sentencial) e `b a`. O primeiro exemplo passa a dizer "forma sentencial" para `E + E` e "sentença" para a forma só com terminais.

## Feito em 13/11 (décima primeira rodada)

1. **Derivação mais à direita.** Quatro passos no fim da Sua vez da derivação, com a mesma declaração `float * id ;`. O aluno toca no não-terminal que a mais à direita troca primeiro (Var, porque `;` é terminal) e na forma que só aparece nela (`Tipo * id ;`). A última cena põe as duas derivações lado a lado: mesma sentença, mesma árvore, outra ordem. Nessas cenas a faixa da receita não acende passo, porque o passo 2 diz "mais à esquerda".
2. **A tabela SLR nasce das caixas de itens.** Quatro passos com os seis estados de `S → ( S ) | id`, os mesmos da aula de itens LR(0), e as regras numeradas na tela. O aluno põe o empilhar do estado do `(` com id e os r1 do estado do `)` nas colunas do FOLLOW; a tabela inteira sai do `LRTable`, sem conflito.
3. **E/T na aula de ambiguidade.** O primeiro exemplo, `n + n * n`, ganha o conserto em dois níveis (`E → E + T | T`, `T → T * n | n`), a árvore única e a pergunta "por que o vezes fica sempre mais fundo?". O motor confere que só há uma derivação e que a árvore da tela é ela.

## Feito em 14/11 (décima quarta rodada)

1. **LALR: de onde vem o lookahead.** Três cenas no fim da Sua vez voltam aos ponteiros da pegadinha do SLR (`S → L = R | R`). O SLR tem conflito no igual depois do L. O aluno descobre que `L → • id` ganha o `=` do item `S → • L = R, $` que o criou, e vê que `R → L •` fica só com o `$`: o conflito some. O motor confere os itens LR(1) e que o LALR não tem conflito. A fusão desenhada com estados completos continua pendente.
2. **O scanner volta ao último final.** Duas cenas no Flex, depois do maior casamento: o AFD da aula de autômatos aceita só se a cadeia inteira acabar num final, e o scanner anda enquanto dá. Com `3.x`, a regra do NUM passa pelo ponto, trava no x e volta ao 3. O MiniFlex confere os tokens.
3. **Thompson desenhado.** Quatro cenas no fim de AFN→AFD: o AFN de `a|b` sai do motor (6 estados, 4 setas ε), e o aluno calcula que a estrela em volta dá 8 estados.

## Feito em 14/11 (décima terceira rodada)

1. **Minimização: o segundo X e a contradição com o Cuidado.** A cena dizia "se os destinos são diferentes, as origens também são", e o Cuidado do fim chamava essa frase de erro. Agora a cena diz "os destinos formam um par marcado". O aluno acha o segundo X (A com C, que com b vão para C e D), e a fala explica por que B com C não cai. O Cuidado usa A e C do AFD do livro: estados diferentes no desenho, mas equivalentes.
2. **Bison: `%prec` no menos unário.** Pegadinha nova: a regra `'-' expr` fica com a precedência do último token (o menos, a mais fraca). Em `- 2 * 3`, o vezes roda antes. Com `%left UMINUS` e `%prec UMINUS`, o sinal roda antes. O MiniBison confere a ordem das reduções nos dois arquivos.
3. **A Ponte abre com o parser descendente.** A primeira cena da recursão à esquerda diz que no Templo o aluno vai programar um parser descendente, que lê um token por vez e nunca volta atrás. A fatoração abre dizendo que esse parser escolhe a regra pelo próximo token.

## Feito em 13/11 (décima segunda rodada)

1. **E/T/F na prática de recursão.** A prática "Rode a versão consertada" usava pilha e `$`, que só chegam no Templo. Agora ela parte de `E → E + T | T`, `T → T * F | F`, `F → id`: o aluno escolhe a regra nova de E e a de T' (com os dois erros de prova como alternativas: sem ε e com o alfa da outra regra) e deriva `id + id * id` na gramática nova. O motor confere que a receita dá a gramática da tela e que a linguagem é a mesma.
2. **Recursão indireta até o fim.** A pegadinha parava em "a receita de sempre resolve". Agora o aluno escolhe os dois betas de `S → S c a | d a | b` e a regra do S', vê o A sair da gramática e a outra ordem (abrir S dentro de A, a ordem do livro), com a mesma linguagem. O teste compara as três gramáticas em todas as frases de até 7 tokens.
3. **`%right` com `2^3^2` no Bison.** Três cenas no fim da Sua vez com `pot.y`: o aluno calcula 512 e vê que com `%left` daria 64; left manda reduzir, right manda empilhar. A ação usa `pow($1, $3)`, como em C (o `^` do C é ou exclusivo), e o MiniBison passou a entender `pow`. No relatório do `bison -v`, "com qualquer token, ele reduz" agora diz que é a redução padrão.

## Erros que ensinam errado

Conferidos, com o estado de cada um:

| # | Assunto | Achado | Estado |
|---|---|---|---|
| E1 | Recuperação de erros | Quatro entradas sincronizavam no `;` contra a regra 2 da própria receita | feito |
| E2 | Glossário | Handle definido com o erro que o Bug combate | feito |
| E3 | Itens LR(0), tabelas, cena 7 | I1 tem `E' → E •` e era marcado errado | feito |
| E4 | Tokens | "Token" como pedaço, como classe e como "tipo" | feito |
| E5 | Tokens, cena 1 | "parser" usado antes de ser apresentado | feito |
| E6 | Fatoração, cena 11 × ficha × WORKED | Regra do "maior prefixo" diferente em cada lugar | feito |
| E7 | SLR, cena 4 | Tabela "SLR(1)" mostra 3 de 5 estados; `s3` aponta para estado ausente (aluno 4) | feito |
| E8 | Escopo, cena 4 | `x = 2;` fora de função, que não compila em C (aluno 5) | feito |
| E9 | Alocação, cenas 14 e 15 | Vivacidade percorrida de cima para baixo, contra o que a cena 19 chama de erro (aluno 5) | feito |
| E10 | TAC, texto WORKED | `ifFalse a < b goto L1` não bate com a prática nem com o FORMAL (aluno 5) | feito |
| E11 | MIPS, cena 5 | `$t1` e `t1` na mesma tela sem aviso (aluno 5) | feito |
| E12 | Série em C 5 | `e: e '+' e` sem `%left`, e `2+3*4 = 14` mostrando só a regra do `+` (aluno 6) | feito |
| E13 | Guia do TP, cap. 8 | A cola diz "só isto" basta, mas o gerador usa `move`; pseudo-instruções fora da lista (aluno 6) | feito |

## Padrões que valem para o jogo inteiro

1. **A resposta certa quase nunca era a última opção.** Feito (item 7 acima).
2. **"Erros comuns" tinha sempre a resposta "errado"**: 29 de 29. Feito (item 8 acima).
3. **92 das 192 perguntas das aulas animadas têm duas opções.** Pendente.
4. **234 das 239 falas de acerto começam com "Isso".** Pendente. O lugar mais barato é `LessonScreen.kt`, que escreve "**Isso!**" no código, e as chaves `*_ok` dos textos.
5. **Personagens sem vida**: nas trilhas o Byte tem 870 falas, a professora 63 e o Bug nenhuma. As aulas animadas soam secas; as de texto, engraçadas. Pendente.
6. **Trechos longos só ouvindo**: até 6 cenas seguidas sem interação. Em FIRST, FOLLOW, tabela LL(1), descida recursiva, itens LR(0) e tabela SLR, a primeira interação vem na cena 6 ou 7. Pendente.
7. **Termos usados antes de serem ensinados** (os quatro alunos acharam): LL(1), pilha e `$`, "empilhar", FOLLOW, LR e "ambiguidade" aparecem nas regiões 3 e 4 antes da 5 e da 6; Thompson, estado morto, `yytext`, front-end e back-end, prefixo viável, `error`, `yyerrok`, Chaitin e blocos básicos aparecem na ficha ou nos Bugs sem estar na aula animada. Pendente.
8. **Nomes demais para a mesma coisa**: `casa`, `casaToken` e `match`; `X`, `E'` e `A'`; closure, fecho e fechamento; goto e desvio; ok, acc e aceita; cadeia, palavra, frase e sentença; regra, produção, alternativa e opção; "else solto" e "else pendurado"; "núcleo" para kernel e para core; cinco nomes para o parser descendente. Pendente.

## O achado mais importante: o aluno reconhece, mas não produz

Os três professores refizeram as contas e **não acharam nenhum conjunto, tabela, autômato ou traço de pilha errado**. Os problemas são de desenho:

1. **A trilha nunca pede a questão de prova inteira**: gramática → FIRST → FOLLOW → tabela → veredito; itens LR(0) → tabela SLR → conflito; tabela de subconjuntos; tabela de pares da minimização; mostrar que é ambígua e dar uma gramática equivalente; tornar uma gramática LL(1). Os exemplos resolvidos existem, mas só nas aulas de texto, que são opcionais.
2. **O segundo exemplo ("Sua vez") evita o caso difícil.** O ε-fecho depois do move nunca acrescenta estado; não aparece estado morto; no FOLLOW nunca há dois símbolos depois do não-terminal; a regra anulável é sempre o ε escrito; o ponto fixo nunca muda nada na segunda rodada; todas as gramáticas do "Fazer com ajuda" são LL(1); na recursão à esquerda o segundo exemplo só troca as letras; a fatoração nunca mostra o resto vazio (ε). Na "prova de bolso", o revisor aluno errou o FOLLOW de `S → A B d` usando só a receita da aula.
3. **Sem "Fazer com ajuda" nem prática** em AFN→AFD, minimização, itens LR(0), ambiguidade, recursão e fatoração.

## Achados por região (vindos dos relatórios)

Só o essencial; o trecho exato e a frase nova proposta estão no relatório indicado.

**Porto e Floresta** (`professor_1`, `aluno_1`)
- Minimização: cena 8 contradiz a cena 18; justificar o segundo X; mostrar um par que só cai na rodada seguinte; estado morto; desenhar o AFD mínimo.
- AFN→AFD: segundo exemplo com fecho que muda o conjunto e com conjunto vazio, como `(ab|a)c`; dizer de onde vem o AFN.
- Regex: uma cena por operador, `|` e parênteses antes de `(a|b)*abb`, classes `[a-z]`; Thompson com exemplo, ou tirar da ficha.
- Contradições: "só a semântica consulta a tabela de símbolos" × ficha; AFD "no máximo uma" × "exatamente uma" seta; "três de análise, três de síntese".
- Ponte entre "AFD aceita a cadeia inteira" e "o scanner fica com o último final".

**Vale e Ponte** (`professor_2`, `aluno_2`)
- **Associatividade**: o Vale manda pôr recursão à esquerda para o menos associar à esquerda, e a Ponte manda tirar. Falta dizer que a linguagem é a mesma, a árvore muda, e o código do parser (laço) recupera a conta. Os dois revisores acharam.
- A gramática E/T/F (um nível por precedência) nunca é montada na aula animada, e é a gramática do resto do jogo.
- Derivação mais à direita não é praticada (é a base do LR); ε como produção não é ensinado; "forma sentencial" e "sentença" são cobradas sem ter sido ditas; a receita não diz como escolher a regra.
- A Ponte conserta gramáticas para um parser que só é apresentado na região 5: abrir com "No Templo você vai programar um parser que lê um token por vez, de cima para baixo, sem voltar atrás."
- Recursão indireta tratada pela metade e de dois jeitos; α e β sem explicação; prática "Rode a versão consertada" usa pilha e `$` antes da hora.
- Pré-requisitos: LEFT_RECURSION ← AMBIGUITY; LL1_TABLE ← LEFT_RECURSION, LEFT_FACTORING.

**Templo Top-Down** (`professor_3`, `aluno_3`)
- Receita do FOLLOW com α e β: reescrever em palavras ("Ache B do lado direito e olhe o que vem logo depois...") e incluir `S → A B d`.
- Pegadinha do FOLLOW por `S → A B c`, mostrando todas as ocorrências do não-terminal.
- Tabela: exemplo com produção anulável não vazia (`S → A B`) e conflito FIRST×FIRST; as três condições LL(1) na ficha; deixar FIRST e FOLLOW visíveis.
- Descida recursiva: corrigir pré-requisito; mostrar `casa()` e o `main` com o teste do `$`; reescrever "LL(1): sem recursão à esquerda e fatorada", que soa como definição.

**Fortaleza e Oficina** (`professor_4`, `aluno_4`)
- Itens LR(0): terminar o autômato de `S → ( S ) | id` (6 estados, com laço), `S' → S` como passo 0 na tela, "conjunto repetido não é estado novo".
- SLR: a tabela nasce das caixas de itens, com as regras numeradas e um exemplo em que o número do `s` difere do `r`.
- LALR: abrir com o conflito de `S → L = R`; de onde vem o lookahead; fusão desenhada com estados completos, não itens soltos; separar kernel (itens de base) de core (núcleo).
- Pegadinha do handle explicada por "ordem da conta" numa gramática em que o problema é beco sem saída.
- Bison: precedência da regra × do token, `%right` com `2^3^2`, `%prec` no menos unário; "com qualquer token, ele reduz" precisa dizer que é a redução padrão.

**Cidade e Forja** (`aluno_5`; professor falta)
- A ficha cobra o que só está no texto: blocos básicos e líderes, quádruplas e triplas, `in = use ∪ (out − def)`, Chaitin, `$ra`/`jal`, vínculo de controle. Marcar na ficha o que vem da aula de texto, ou dar segunda aula animada a Otimização, MIPS e Alocação.
- Pôr Ambientes de execução depois de Geração de código MIPS, e apresentar `$ra`, `$sp`, `jal` e `4($sp)` um por cena antes do `fat.asm`.
- Abrir a região 8 com uma cena de transição ("o que o parser aceita e não devia") e avisar que a árvore agora é a enxuta.
- AST, texto STORY, diz "compilador em Java"; o guia se chama "seu compilador em C".

**Seu compilador** (`aluno_6`; professor falta)
- Dizer por que há duas linguagens (Byteiro no guia, C na série) e dar a tabela de equivalência de nomes.
- Fechar cada capítulo com "Na sua linguagem" (três perguntas) e "Teste desta etapa" com o comando e a saída esperada.
- Algo rodando já no capítulo 1; cronograma de 3 semanas ao lado do de 8.
- Capítulo 4: tabela FIRST/FOLLOW da Byteiro inteira, com a regra para `{ }` e `[ ]`.

O que funciona, segundo os revisores: a aula de Tokens, a pegadinha do `ifa`, a abertura do FOLLOW, o experimento `5 - 2 - 1` no Bison, `9 - 5 - 2` na árvore, a função em C que chama `E();` antes de ler, os textos "Exemplo resolvido", o Guia do TP com arquivos inteiros e o checklist de véspera, e a correção técnica em geral.

## Como continuar gastando pouco

1. Rode **um revisor por vez**. Cada um gasta perto de 200 mil tokens.
2. Use o prompt de [PROMPT-TRABALHO.md](PROMPT-TRABALHO.md), seção "Revisor", trocando papel, arquivos e nome do relatório.
3. Faltam os professores dos blocos 5 e 6.
4. Depois de mudar aulas, gere as transcrições de novo (o teste que as gerava foi apagado; o formato está descrito em `docs/revisao/transcricoes/00_indice.md`).
