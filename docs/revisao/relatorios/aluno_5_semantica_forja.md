# Relatório do aluno 5: Cidade Semântica (região 8) e Forja do Código (região 9)

## Como foi estudar isto

Eu continuaria usando. As oito aulas animadas são curtas (17 a 23 cenas, cerca de 2 a 2,5 minutos de fala), cabem numa sessão de 10 minutos e quase todas têm um momento em que eu faço a conta antes de ouvir a resposta. Escopo, TAC e a pilha de chamadas eu entendi de primeira.

O que mais me travou foi a distância entre a trilha e a ficha. A aula animada ensina a ideia central (dobra de constantes, "lw, lw, conta, sw", colorir o grafo) e a ficha cobra o conteúdo de prova (blocos básicos e líderes, quádruplas e triplas, `in = use ∪ (out − def)`, Chaitin, `$ra`/`jal`, vínculo de controle), que só existe nas aulas de texto. Quem faz só a trilha abre a ficha e acha que perdeu uma aula.

O segundo problema é o MIPS. A aula animada explica `lw` e `sw` com calma, mas o assembly de verdade aparece antes dela, no assunto de Ambientes de execução, e a própria aula mostra `$t1` e `t1` na mesma tela sem avisar que são coisas diferentes.

## Onde eu travei

**Atributos e tipos**

- **Média. A entrada na semântica é uma frase só.** Aula animada, cena 4: "A árvore já existe. Agora a análise semântica calcula os atributos dela." Não diz o que o parser deixou passar. A região 1 tinha o exemplo `int x = "oi";`, mas isso foi sete regiões atrás. Eu abriria a aula com: "O parser aceitou `ok + 1`. A gramática só olha a forma; ela não sabe que `ok` é bool. Quem pega isso é a próxima fase."
- **Média. A árvore mudou de cara sem aviso.** Cena 4: "+ ⏎ a * ⏎ x 2". Até a região 7 toda árvore tinha E, T e F nos nós. Aqui o operador está no nó, e a aula que explica essa árvore (AST) vem dois assuntos depois. Para piorar, a prática "O tipo que desce" volta para a árvore com D, T e L. Sugestão: pôr AST antes de Atributos, ou dizer na cena 4: "Esta é a árvore enxuta, só com operações e valores. Você aprende a montá-la daqui a duas aulas."
- **Média. Degrau alto na prática "O tipo que desce", cenas 7 e 8.** PROF: "Definição que só tem atributo sintetizado é S-atribuída. O parser ascendente calcula tudo nas reduções." Na cena seguinte já me perguntam se é S ou L-atribuída. "Definição" de quê? A sigla SDD só aparece na ficha ("Uma SDD pendura atributos") e nos textos. Eu diria antes: "O conjunto gramática + regras de atributo se chama definição dirigida pela sintaxe, SDD."
- **Baixa.** O atributo herdado chama `L.her` na prática e `L.in` em todos os textos.
- **Baixa.** Cena 10, receita: "Bool numa conta é erro." Em C, `ok + 1` compila. Só o texto VISUAL avisa: "Nesta linguagem (como em Java)". Falta essa frase na aula.

**Escopo e tabela de símbolos**

- Funcionou: a pilha de tabelas no canto da tela, que cresce e encolhe junto com as chaves, e as perguntas das cenas 14 a 16, que só dá para responder aplicando a regra.
- **Baixa. A busca "desce" na trilha e "sobe" no texto.** Cena 7: "começa na tabela do topo e desce". Texto WORKED: "y não está no bloco 1, sobe para o 0". Eu usaria sempre "de dentro para fora".
- **Baixa.** A ficha cobra "Escopo dinâmico decide pelo quê?" e "tempo de vida", que só estão nos textos SOCRATIC e FORMAL.

**AST com classes**

- **Média. O título promete classes e a trilha mostra uma linha.** Cena 7: "new BinOp('+', a, b)". As classes `Num`, `Var` e `BinOp`, o `while` do parser e o `$$ = new BinOp(...)` do Bison estão só nos textos WORKED e FORMAL, mas 4 dos 9 flashcards são sobre isso ("Linha-chave da associatividade à esquerda no descendente?"). Para o trabalho prático, o texto WORKED é a aula de verdade; a trilha deveria mandar para ele.
- **Média. A aula não mostra `a - b - c`.** A regra da cena 16 ("Na raiz fica a última operação a ser calculada") não me diz qual dos dois menos é o último. Só o texto VISUAL mostra.
- **Baixa. Quatro nomes para a mesma coisa:** "regra de passagem" (aula), "não-terminais em corrente" (STORY), "não-terminais de cadeia" (FORMAL), "correntes" (ficha).
- **Baixa.** Cena 5: "Toque nos dois parênteses: eles não mudam a conta." A fala já entrega a resposta.

**Código de três endereços**

- Funcionou: a aula inteira, e a prática do if/while (a cena 5, "o que impede a execução de cair no else?", foi onde o `goto` fez sentido).
- **Média. Quádruplas e triplas não existem na trilha.** Metade da ficha é sobre elas ("Campos da tripla?", "Por que triplas indiretas?"). O texto WORKED resolve bem, com a tabela lado a lado.
- **Baixa.** Cena 14, dica: "Comece pelo filho da esquerda." A receita só diz "Gere os filhos antes do pai". Faltou "esquerda antes da direita" no passo 1.
- **Baixa.** Prática, cena 9: "É assim que o seu laço chega ao MIPS." É a primeira vez que a palavra MIPS aparece no jogo; o glossário dela só vem em Geração de código.

**Otimização**

- **Alta para a prova. A trilha ensina uma otimização e a ficha cobra seis.** Cena 16: "Outras otimizações clássicas: propagar cópias e eliminar código morto." É só isso. A ficha pede propagação, subexpressão comum, código morto, bloco básico, três regras de líder e redução de força. O Bug do álbum cobra "Código que nunca roda pode sair", que a trilha não ensinou. Eu faria uma segunda aula animada, "Líderes e blocos", com o exemplo de 7 linhas do texto VISUAL.
- **Baixa.** São 19 cenas para uma ideia que entendi na cena 6. Entre as cenas 9 e 16 eu só repeti "conta com dois números embaixo".

**Ambientes de execução**

- **Alta. O assembly de verdade chega antes da aula de MIPS.** O texto WORKED traz `fat.asm` inteiro: `jal`, `jr`, `slti`, `move`, `addi $sp, $sp, -8`, `sw $ra, 4($sp)`. Na ordem do mapa, eu ainda não vi nem `lw`. Os comentários ajudam, mas ninguém explica o que significa o `4(` em `4($sp)`, e "PC" aparece em "Guarda PC + 4 em $ra" sem nunca ter sido definido no jogo. Sugestão: pôr este assunto depois de Geração de código MIPS, ou passar o `fat.asm` para uma etapa "pilha em MIPS" no fim da região.
- **Média. Cinco nomes para a caixa:** "bloco" (cena 3), "registro de ativação" (aula), "caixa" (tabelas), "quadro"/"frame" (textos e ficha), "prato" (STORY). A ficha usa "quadro" ("Esquecer o quadro de main"), que a trilha nunca usou.
- **Média. Para que lado a pilha cresce?** Tabelas, cena 5: "A pilha cresce para cima." Flashcard: "Para endereços menores". Texto FORMAL: "cresce para BAIXO". Falta uma frase: "No desenho o topo fica em cima; na memória do MIPS o topo anda para endereços menores."
- **Baixa.** Tabelas, cena 1: "main{volta: SO}". SO nunca é explicado.

**Geração de código MIPS**

- **Alta. `$t1` e `t1` na mesma tela.** Cena 5: "lw $t1, b ⏎ lw $t2, c ⏎ mul $t3, $t1, $t2 ⏎ sw $t3, t1". O registrador `$t1` guarda b e a variável `t1` recebe o resultado. A aula não comenta; o aviso só está no texto STORY ("`t1` (sem cifrão) é uma prateleira") e na ficha. Eu trocaria os registradores do exemplo para `$t8` e `$t9`, ou chamaria o temporário de `_t1`, como o próprio texto sugere.
- **Média. A aula não explica o cifrão nem o nome dos registradores.** Diz o que é registrador (cena 1: "poucos e rápidos"), `lw` (cena 6: "de load word") e `sw` (cena 7), e isso está bom. Mas `$t0`, quantos são e por que "t" só aparecem no texto MNEMONIC. Uma cena resolveria: "Registrador começa com cifrão. Os `$t` são de rascunho: `$t0` a `$t9`."
- **Média. Três jeitos diferentes de escrever o mesmo if.** Pegadinha: "bge $t0, $t1, fim" e "li $t2, 1" (o `li` aparece sem uma palavra de explicação). Textos e ficha: `sgt` + `beq $t3, $zero, L0`. TAC: `ifFalse`. Eu escolheria um só.
- **Média. Tudo o que o trabalho pede está só nos textos:** `.data`, `.text`, `li`, `la`, `$zero`, `syscall`. Os textos explicam bem na primeira vez (mapa da analogia no STORY, tabela de instruções no FORMAL). Só `.asciiz` e `mflo` passam sem explicação.
- **Baixa.** Cena 6: "Qual linha faz a multiplicação?", com dica "Procure a instrução mul". Acerto sem entender nada.

**Alocação de registradores**

- **Alta. A aula manda fazer de baixo para cima e faz de cima para baixo.** Cena 12: "A análise de vivacidade anda de trás para a frente." Nas cenas 14 e 15 eu respondo a linha 2 e depois a linha 3, descendo, e acerto. Na cena 19: "Errado: Calcular a vivacidade de cima para baixo." Saí sem saber por que não pode, se acabei de fazer. O motivo só está no texto WORKED: "A informação vem do futuro, então a análise anda para trás." Eu faria as cenas 14 e 15 começarem pela última linha.
- **Média.** Cenas 4 a 9: o grafo de a, b e c aparece sem código nenhum. Não vejo de onde saíram as arestas; só leio o desenho.
- **Média.** Cena 8: surgem `$s0` e `$s1`. A aula de MIPS só usou `$t`. A diferença está nos textos e na ficha ("$t = tchau, $s = sagrado").
- **Média.** A fórmula, Chaitin e `χ` estão só nos textos. O WORKED é o melhor texto do bloco (tabela use/def/out/in preenchida linha a linha), mas usa "χ = 3" sem dizer o que é χ.

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| SDD | Atributos, ficha | só textos STORY e FORMAL |
| S-atribuída / "definição" | Atributos, prática 2, cena 7 | na mesma cena, sem dizer o que é "definição" |
| pós-ordem | Atributos, ficha | só texto VISUAL |
| `inttofloat` | Atributos, ficha | só textos |
| bool | Atributos, cena 10 | nunca (some na cena 16 como tipo de `ok`) |
| escopo dinâmico, tempo de vida | Escopo, ficha | só textos SOCRATIC e FORMAL |
| corrente / cadeia | AST, ficha e textos | aula chama de "regra de passagem" |
| quádrupla, tripla | TAC, ficha | só textos |
| MIPS | TAC, prática, cena 9 | Geração de código (assunto posterior) |
| bloco básico, líder, redução de força | Otimização, ficha | só textos |
| quadro, heap, vínculo de controle/acesso | Ambientes, ficha | só textos |
| `$ra`, `jal`, `$sp`, `$a0`, `$v0` | Ambientes, ficha e texto WORKED | comentários no código; tabela só no FORMAL |
| `4($sp)` | Ambientes, texto WORKED | nunca |
| PC | Ambientes, ficha | nunca |
| SO | Ambientes, tabelas, cena 1 | nunca |
| `$` e `t` de `$t0` | MIPS, cena 1 | só texto MNEMONIC |
| `li`, `bge` | MIPS, pegadinha | `li` só nos textos; `bge` só em palavras |
| `$zero`, `syscall`, `.data`, `la` | MIPS, ficha | só textos (bem explicados) |
| `$s0` | Alocação, cena 8 | só textos e ficha |
| χ, Chaitin, `use`/`def`/`out`/`in` | Alocação, ficha e textos | `χ` só no MNEMONIC; o resto no WORKED |
| Briggs | Alocação, texto FORMAL | nunca |

## Prova de bolso

| Assunto | Exercício que tentei | Resultado | O que faltou |
|---|---|---|---|
| Atributos | Anotar tipos em `x * 2 + 3 * c`; classificar uma SDD escrita em regras | em parte | Anotar, consigo. Classificar, não: a trilha nunca mostra uma regra escrita como `E.val = E1.val + T.val` |
| Escopo | Ligar usos a declarações com 3 blocos | consigo | Só redeclaração no mesmo bloco, que aparece de passagem |
| AST | Desenhar a AST de `a - b - c * (d + 1)` | em parte | Qual menos fica mais fundo |
| TAC | TAC de `x = (a+b)*(c-d)` e de um while; depois quádruplas e triplas | em parte | Quádruplas e triplas só com o texto |
| Otimização | Dobrar `(4*2+a)*3`; achar líderes num trecho com goto | em parte | Líderes e blocos: nada na trilha |
| Ambientes | Pilha de `fat(4)` no pico; campos do registro e quem preenche | em parte | A pilha, consigo. Campos e chamador/chamado só no texto FORMAL |
| MIPS | `.asm` completo de `x = a + b * 2; print(x)` | não (só trilha); consigo (com o texto WORKED) | `.data`, `li`, `syscall`, cópia final |
| Alocação | Tabela de vivacidade, grafo e cores de `(a+b)*(c-d)` | em parte | O grafo e as cores saem; a tabela use/def/out/in só com o texto |

## Diversão e ritmo

**O que funcionou.** A pegadinha do `x / x vira 1` ("Pense em x valendo zero") foi o melhor momento do bloco: errei, e a dica me fez pensar em vez de entregar. Em Ambientes, a cena 2 pergunta antes de explicar ("Se o n morasse num lugar só, qual valor seria perdido?") e funciona. O exercício de completar as lacunas (A), (B) e (C) do while em MIPS é o tipo de coisa que eu faria de novo. Nos textos o Byte tem voz ("Eu fico com medo só de ver tanto cifrão"), e as "Conversas ao pé da lareira" têm graça ligada ao assunto (t1: "Eu cheguei primeiro nesse registrador!").

**O que cansou.**
- Nas oito aulas, a pergunta de "Erros comuns" é "Um colega disse isto. Certo ou errado?", e a resposta certa é sempre "errado". Na terceira aula eu já tocava sem ler. Uma em cada três deveria ser "certo".
- Nas oito aulas a receita vem seguida de "Que regra disse isso? Toque." Isso testa se eu sei ler a lista, não se eu sei usá-la.
- Na aula animada o Byte é um narrador neutro e a PROF fala uma ou duas frases por aula. O Bug só existe na abertura e no nome do álbum.
- Alguns mnemônicos dão mais trabalho que o conteúdo: "Pão, Vitamina, Café, Açúcar, Esfirra, Leite, Torrada" para os sete campos do quadro; "É I Bem Simples".
- Ambientes, texto SOCRATIC: "Você não é 'burro'". Ninguém tinha dito que eu era.

**Telas.** Abri três capturas (Atributos, MIPS, Alocação). Legíveis no celular, com a árvore e o código grandes. A do MIPS confirma `$t1` e `t1` a duas linhas de distância.

**Textos.** Eu escolheria WORKED em todos os assuntos (é o que serve para a prova e o trabalho) e STORY no MIPS (a bancada e a despensa). Pularia o FORMAL de Atributos (`widen`, `E.addr`, SDT de uma vez só) e o MNEMONIC de Ambientes.

## Nota por assunto

| Assunto | Clareza | Diversão | Pronto para a prova | Uma frase |
|---|---|---|---|---|
| Atributos e tipos | 3 | 3 | 3 | Subir tipos é fácil; S e L-atribuída chegam rápido demais |
| Escopo | 5 | 4 | 4 | A melhor aula do bloco |
| AST | 4 | 3 | 3 | Entendi a árvore; as classes ficaram no texto |
| TAC | 5 | 4 | 3 | Clara, mas quádruplas e triplas ficaram de fora |
| Otimização | 4 | 3 | 2 | Uma otimização ensinada, seis cobradas |
| Ambientes | 4 | 4 | 3 | Pilha ótima; o MIPS veio cedo demais |
| MIPS | 3 | 3 | 2 (4 com os textos) | `lw`/`sw` ok; o resto do assembly está fora da trilha |
| Alocação | 3 | 4 | 2 (4 com o WORKED) | A direção da vivacidade ficou contraditória |

## Desconfio que está errado

- Escopo, aula, cena 4: `x = 2;` aparece fora de qualquer função; em C isso não compila.
- Atributos, aula, cena 10: "Bool numa conta é erro" não vale para C, e a aula não diz de qual linguagem está falando.
- Ambientes, tabelas, cena 5: "A pilha cresce para cima" contra o flashcard "Para endereços menores".
- TAC, texto WORKED: `ifFalse a < b goto L1` não bate com a prática (`t1 = a < b` e depois `ifFalse t1 goto L1`) nem com a lista do texto FORMAL (`ifFalse x goto L`).
- Otimização, aula, cena 13: a transcrição lista "certo = * ; errado = * | -". Há dois `*` na tela; conferir se o toque distingue os dois.
- AST, texto STORY: "No trabalho prático (o compilador em Java)", mas o guia do jogo se chama "seu compilador em C".
- Alocação, aula, cenas 14 e 15: a vivacidade é percorrida de cima para baixo, contra o que a cena 19 chama de erro.

## As 5 coisas que eu mudaria primeiro

1. **Levar o conteúdo de prova para a trilha, ou avisar que está no texto.** Cada ficha deveria marcar o que vem da aula de texto, e Otimização, MIPS e Alocação merecem uma segunda aula animada (líderes e blocos; `.data`, `li` e `syscall`; tabela de vivacidade).
2. **Pôr Ambientes de execução depois de Geração de código MIPS**, e apresentar `$ra`, `$sp`, `jal` e `4($sp)` um por cena, antes do `fat.asm`.
3. **Tirar a armadilha `$t1` × `t1`** da cena 5 da aula de MIPS e acrescentar uma cena sobre o cifrão e as famílias `$t` e `$s`.
4. **Refazer as cenas 14 e 15 de Alocação de baixo para cima**, com a frase "a informação vem do futuro".
5. **Abrir a região 8 com uma cena de transição** (o que o parser aceita e não devia) e avisar que a árvore agora é a enxuta; variar a resposta de "Um colega disse isto" para não ser sempre "errado".
