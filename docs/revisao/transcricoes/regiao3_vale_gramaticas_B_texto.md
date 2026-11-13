# Região 3: Vale das Gramáticas — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO CFG_DERIVATION: Gramáticas e derivações

#### ESTILO STORY (História / Analogia): "Análise sintática você já fez na escola"
- PROF: Lembra da aula de português? Sujeito, predicado, objeto direto… Aquilo se chamava **análise sintática**. Literalmente o mesmo nome!
- BYTE: Sério?! Eu odiava aquilo.
- PROF: Então vamos odiar juntos, mas entendendo. Uma frase é feita de partes, e cada parte tem uma regra de montagem.
- CÓDIGO (Uma minigramática do português):
```
Frase -> SN SV
SN -> Art Sub
SV -> Ver SN
Art -> o | a
Sub -> gato | menina | peixe
Ver -> come | ama
```
- TEXTO: Cada linha é uma **produção**: 'uma Frase é um sintagma nominal (SN) seguido de um sintagma verbal (SV)'. Quem aparece à esquerda da seta é **não-terminal** (uma categoria). As palavras de verdade, o, gato, come, são **terminais**.
- WIDGET DERIVATION gramática=«Frase -> SN SV ⏎ SN -> Art Sub ⏎ SV -> Ver SN ⏎ Art -> o | a ⏎ Sub -> gato | menina | peixe ⏎ Ver -> come | ama» entrada=«o gato come o peixe» legenda: Veja a frase nascer, trocando sempre o não-terminal mais à esquerda.
- PROF: Cada passo troca UM não-terminal pelo lado direito de uma de suas produções. Isso é uma **derivação**. Quando sempre trocamos o mais à esquerda, é uma **derivação mais à esquerda**.
- PREVEJA: Essa gramática gera 'a menina ama o gato'?
    opções: ✔Sim | Não
    explicação: Frase ⇒ SN SV ⇒ Art Sub SV ⇒ a Sub SV ⇒ a menina SV ⇒ a menina Ver SN ⇒ … ⇒ a menina ama o gato. (Ela não liga para concordância de gênero: 'a gato' também sai. Gramática de brinquedo!)
- PREVEJA: E 'gato o come peixe o'?
    opções: Sim | ✔Não
    explicação: Todas as palavras existem (léxico ok!), mas a ordem não segue as regras: erro sintático.
- TEXTO: Troque palavras por tokens e é igualzinho: `E -> E + T` diz 'uma expressão pode ser uma expressão, um +, e um termo'. O compilador faz análise sintática como você fazia na escola, só que sem preguiça.
- CAIXA [Não existem perguntas idiotas]: P: Por que 'livre de contexto'? R: Porque o lado esquerdo é UM não-terminal sozinho: você pode trocar SN por 'Art Sub' em qualquer lugar, não importa o que esteja em volta (o contexto).
- CAIXA [Conversa ao pé da lareira]: Expressão regular: 'Eu descrevo palavras.' Gramática: 'Eu descrevo frases, e sei aninhar coisas, como parênteses dentro de parênteses, coisa que você não consegue!'
- REVELAR: Nessa gramática, quem são os terminais e os não-terminais?
    → Não-terminais: Frase, SN, SV, Art, Sub, Ver. Terminais: o, a, gato, menina, peixe, come, ama. Símbolo inicial: Frase (o da primeira linha).

#### ESTILO VISUAL (Passo a passo visual): "Derivação em câmera lenta"
- PROF: Vamos ver a gramática de expressões do Livro do Dragão gerando `id + id * id`.
- CÓDIGO (E = expressão, T = termo, F = fator):
```
E -> E + T | T
T -> T * F | F
F -> ( E ) | id
```
- WIDGET DERIVATION gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id * id» legenda: Avance passo a passo e veja a árvore crescer a cada troca.
- TEXTO: Em cada linha, o não-terminal trocado é o MAIS À ESQUERDA. As formas intermediárias, como `id + T * F`, se chamam **formas sentenciais**. A última, só com terminais, é uma **sentença**.
- PREVEJA: Na derivação mais à esquerda, depois de `id + T`, qual é o próximo passo?
    opções: ✔id + T * F | id + F | id + id
    explicação: Ainda precisamos gerar `id * id` depois do +. Se T virasse F direto, o `*` nunca apareceria! Então T → T * F.
- BYTE: E a árvore? É a mesma coisa que a derivação?
- PROF: A árvore é o 'resumo' da derivação: mostra QUAL produção foi usada em cada nó, mas esquece a ORDEM das trocas.
- WIDGET PARSE_TREE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id * id» legenda: Leia as folhas da esquerda para a direita: dá a sentença.
- WIDGET DERIVATION gramática=«S -> a S b | ε» entrada=«a a a b b b» legenda: S -> a S b | ε: cada passo põe um a na frente e um b atrás. Nenhuma ER faz isso!
- REVELAR: Quantos passos teve a derivação de `a a a b b b`? Por quê?
    → 4 passos: três vezes S → a S b e uma vez S → ε. Cada a ganha o seu b casado.
- WIDGET DERIVATION gramática=«S -> ( S ) S | ε» entrada=«( ( ) ) ( )» legenda: Parênteses balanceados. Repare como o ε faz S sumir.
- REVELAR: Diferença entre forma sentencial e sentença?
    → Forma sentencial: qualquer coisa derivável do símbolo inicial (pode ter não-terminais). Sentença: forma sentencial só com terminais.

#### ESTILO WORKED (Exemplo resolvido): "Derivando à esquerda e à direita"
- PROF: Exercício: derivação mais à esquerda E mais à direita de `id + id * id`.
- CÓDIGO:
```
E -> E + T | T
T -> T * F | F
F -> ( E ) | id
```
- PASSOS (Mais à esquerda (⇒lm): troque sempre o NT mais à esquerda):
    1) E ⇒ E + T
    2) ⇒ T + T
    3) ⇒ F + T
    4) ⇒ id + T
    5) ⇒ id + T * F
    6) ⇒ id + F * F
    7) ⇒ id + id * F
    8) ⇒ id + id * id ✔ (8 passos)
- TEXTO: Agora mais à direita (⇒rm): troque sempre o NT mais à DIREITA. Eu começo:
- CÓDIGO:
```
E ⇒ E + T
  ⇒ E + T * F
  ⇒ E + T * id
```
- REVELAR: Próximos dois passos?
    → ⇒ E + F * id ⇒ E + id * id
- REVELAR: E os três passos finais?
    → ⇒ T + id * id ⇒ F + id * id ⇒ id + id * id
- CAIXA [Não existem perguntas idiotas]: P: As duas derivações dão árvores diferentes? R: Não! Mesmas produções, mesma árvore, só muda a ordem das trocas. Cada árvore tem exatamente UMA derivação mais à esquerda e UMA mais à direita.
- WIDGET PARSE_TREE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id * id» legenda: A árvore comum às duas derivações.
- TEXTO: Mais um, esmaecido: gramática `S -> a S b | ε`, cadeia `a a b b`.
- REVELAR: Derivação completa?
    → S ⇒ a S b ⇒ a a S b b ⇒ a a b b (S → ε no último passo). Com um só NT em cada forma sentencial, mais à esquerda = mais à direita!
- WIDGET DERIVATION gramática=«S -> a S b | ε» entrada=«a a b b» legenda: Confira.
- CAIXA [Dica de prova]: Em prova, escreva um passo por linha, marque o NT trocado e indique ⇒lm ou ⇒rm. Conferência rápida: nº de passos = nº de nós internos da árvore.

#### ESTILO SOCRATIC (Perguntas guiadas): "O que uma gramática gera?"
- PROF: Pergunta 1: tente sem medo.
- CÓDIGO:
```
S -> a S b | ε
```
- PREVEJA: Essa gramática gera `a a b`?
    opções: Sim | ✔Não
    explicação: Toda vez que S vira a S b, entram um a E um b. A quantidade de a's e b's é sempre igual.
- PREVEJA: Qual é a menor cadeia que ela gera?
    opções: ab | a | ✔a cadeia vazia ε | b
    explicação: S → ε direto: zero passos de 'a S b'.
- REVELAR: Descreva a linguagem inteira em uma frase.
    → { aⁿbⁿ | n ≥ 0 }: n a's seguidos de n b's.
- PREVEJA: Uma expressão regular consegue descrever aⁿbⁿ?
    opções: Sim: a*b* | ✔Não
    explicação: a*b* aceita aab, com contagens diferentes. Nenhuma ER consegue: seria preciso memória ilimitada. Gramáticas livres de contexto são mais poderosas.
- BYTE: Então gramática é tipo uma ER turbinada?
- PROF: Pode pensar assim: toda linguagem regular é livre de contexto, mas não o contrário. Pergunta 2:
- CÓDIGO:
```
E -> E + T | T
T -> T * F | F
F -> ( E ) | id
```
- PREVEJA: Quantos não-terminais essa gramática tem?
    opções: ✔3 | 5 | 8
    explicação: E, T e F: os que aparecem do lado esquerdo.
- PREVEJA: E quantos terminais?
    opções: 3 | ✔5 | 8
    explicação: +, *, (, ) e id.
- REVELAR: Por que numa derivação precisamos escolher QUAL não-terminal trocar?
    → Porque uma forma sentencial pode ter vários NTs. Fixar uma ordem (sempre o mais à esquerda, ou sempre o mais à direita) deixa a derivação única para cada árvore. Os analisadores LL constroem a mais à esquerda; os LR, a mais à direita ao contrário.
- WIDGET DERIVATION gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«( id ) * id» legenda: Derive você mesmo antes de avançar.

#### ESTILO FORMAL (Definição de prova): "GLC e derivações: definição formal"
- TEXTO: **Gramática livre de contexto** G = (N, T, P, S): N, conjunto de não-terminais (variáveis sintáticas); T, conjunto de terminais (os tokens); P, conjunto de produções A → α, com A ∈ N e α ∈ (N ∪ T)*; S ∈ N, o símbolo inicial.
- TEXTO: **Derivação em um passo**: se A → γ ∈ P, então αAβ ⇒ αγβ. ⇒* significa 'zero ou mais passos'; ⇒⁺, 'um ou mais passos'.
- TEXTO: Se S ⇒* α, então α é uma **forma sentencial** de G; se α só tem terminais, é uma **sentença**. A **linguagem gerada** é L(G) = { w ∈ T* | S ⇒⁺ w }.
- TEXTO: **Derivação mais à esquerda** (⇒lm): sempre substitui o não-terminal mais à esquerda. **Mais à direita** (⇒rm, ou canônica): sempre o mais à direita.
- TEXTO: **Árvore de derivação**: raiz rotulada S; nós internos rotulados por NTs; se um nó A tem filhos X₁…Xₙ, então A → X₁…Xₙ ∈ P; folhas são terminais ou ε. As folhas lidas da esquerda para a direita formam o **produto** (fronteira) da árvore.
- CÓDIGO:
```
Convenções (Livro do Dragão):
  terminais:            a, b, c, +, *, (, ), id, if ...
  não-terminais:        A, B, C, S, E, T, F ...
  símbolos quaisquer:   X, Y, Z
  cadeias de terminais: u, v, w, x, y, z
  formas sentenciais:   α, β, γ
  A → α₁ | α₂ | ... | αₖ   (alternativas de A)
```
- PREVEJA: Se S ⇒* `id + T`, então `id + T` é…
    opções: uma sentença | ✔uma forma sentencial (não sentença) | uma produção
    explicação: Ainda tem o não-terminal T: é forma sentencial, mas não sentença.
- PREVEJA: Cada árvore de derivação corresponde a quantas derivações mais à esquerda?
    opções: ✔Exatamente uma | Várias | Nenhuma
    explicação: A árvore fixa as produções; a regra 'sempre o mais à esquerda' fixa a ordem. Resultado: uma única derivação ⇒lm.
- CAIXA [Dica de prova]: Questão tradicional: 'dada G, mostre uma derivação mais à esquerda (ou à direita) de w e a árvore'. Erro que custa pontos: numa ⇒lm, trocar um NT que NÃO é o mais à esquerda.
- WIDGET DERIVATION gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id * ( id + id )» legenda: Pratique: derivação ⇒lm de id * ( id + id ).
- REVELAR: Escreva G = (N, T, P, S) para `S -> a S b | ε`.
    → N = {S}; T = {a, b}; P = {S → a S b, S → ε}; símbolo inicial S.

#### ESTILO MNEMONIC (Truque de memória): "As quatro peças da gramática"
- CAIXA [Gancho de memória]: **G = (N, T, P, S)** → '**N**unca **T**ire **P**rova **S**em estudar': Não-terminais, Terminais, Produções, Símbolo inicial.
- CAIXA [Gancho de memória]: **Terminal termina**: é folha, não se expande mais. O não-terminal ainda 'não terminou', vai ser trocado.
- CAIXA [Gancho de memória]: Derivação mais à esquerda = **ler um livro**: você resolve as coisas na ordem em que lê, da esquerda para a direita.
- TEXTO: **Palácio da memória**: o **Vale das Gramáticas**. As produções são sementes: um não-terminal plantado (S) brota galhos (o lado direito) até virar só folhas (terminais). A árvore de derivação é, literalmente, uma árvore que cresceu no vale.
- CAIXA [Gancho de memória]: Forma **sentencial** = prédio em obra (ainda tem andaime, os NTs). **Sentença** = prédio pronto, só terminais.
- PREVEJA: Numa árvore de derivação, os terminais ficam…
    opções: na raiz | nos nós internos | ✔nas folhas
    explicação: Terminal termina: é sempre folha. Nós internos são não-terminais.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que a árvore muda entre ⇒lm e ⇒rm (não muda!); (2) confundir derivação (de S até w) com redução (o caminho inverso, usado no LR); (3) esquecer que ε é uma folha legítima.
- WIDGET PARSE_TREE gramática=«S -> a S b | ε» entrada=«a a b b» legenda: Folhas da esquerda para a direita = a sentença.
- REVELAR: Recite: o que significam N, T, P, S, ⇒, ⇒* e ⇒lm?
    → Não-terminais, Terminais, Produções, Símbolo inicial; ⇒ é um passo; ⇒* é zero ou mais passos; ⇒lm é um passo trocando o NT mais à esquerda.


## ASSUNTO AMBIGUITY: Árvores e ambiguidade

#### ESTILO STORY (História / Analogia): "O policial e o binóculo"
- NARRADOR: Manchete do jornal: 'Policial vê ladrão com binóculo.'
- BYTE: Peraí: quem estava com o binóculo? O policial ou o ladrão?
- PROF: Exatamente! A frase tem DUAS árvores: [vê [ladrão com binóculo]] ou [[vê ladrão] com binóculo]. Mesmas palavras, estrutura diferente, sentido diferente. Isso é **ambiguidade**.
- TEXTO: Em português, a gente desempata pelo contexto. Um compilador não tem esse 'bom senso': se a gramática permite duas árvores, ele não sabe qual escolher, e cada árvore pode significar um programa diferente!
- PREVEJA: Quanto vale 2 + 3 * 4?
    opções: 14 | 20 | ✔Depende da árvore
    explicação: Na matemática da escola, 14. Mas se a gramática permitir a árvore (2 + 3) * 4, dá 20. Quem decide o significado é a árvore!
- CÓDIGO (Curta e bonita… mas ambígua):
```
E -> E + E | E * E | ( E ) | id
```
- WIDGET PARSE_TREE gramática=«E -> E + E | E * E | ( E ) | id» entrada=«id + id * id» legenda: Duas árvores para a mesma frase: gramática ambígua.
- PROF: Na árvore em que o * fica mais embaixo, ele é calculado primeiro: 14. Na outra, o + fica embaixo: 20.
- CAIXA [No mundo real]: Calculadora simples de camelô faz 2 + 3 × 4 = 20 (vai calculando conforme você digita). A científica dá 14. São 'gramáticas' diferentes!
- BYTE: E como resolve?
- PROF: Dois jeitos: reescrever a gramática com níveis de precedência (E, T, F) ou dizer ao gerador de analisadores quem tem prioridade (`%left` no Bison).
- WIDGET PARSE_TREE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id * id» legenda: Gramática E/T/F: uma única árvore, com o * embaixo.
- REVELAR: Defina ambiguidade com suas palavras.
    → Uma gramática é ambígua se existe pelo menos UMA sentença com duas (ou mais) árvores de derivação diferentes.

#### ESTILO VISUAL (Passo a passo visual): "Caçando árvores gêmeas"
- PROF: O widget procura todas as árvores para a entrada. Se achar duas, mostra as duas.
- WIDGET PARSE_TREE gramática=«E -> E + E | E * E | ( E ) | id» entrada=«id + id * id»
- TEXTO: Olhe a RAIZ de cada árvore: numa, a primeira produção é E → E + E; na outra, E → E * E. Raízes diferentes = estruturas diferentes.
- PREVEJA: E `id + id + id`, nessa mesma gramática?
    opções: Uma árvore | ✔Duas árvores
    explicação: (id + id) + id ou id + (id + id). Com + o valor coincide, mas com - não: (5 - 3) - 1 ≠ 5 - (3 - 1). É um problema de **associatividade**.
- WIDGET PARSE_TREE gramática=«E -> E + E | E * E | ( E ) | id» entrada=«id + id + id» legenda: Associatividade: quem se junta primeiro?
- WIDGET PARSE_TREE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id + id + id» legenda: Na gramática E/T/F a recursão à esquerda força (id + id) + id.
- BYTE: E o famoso 'else pendurado'?
- CÓDIGO (i = 'if (cond) then', e = 'else', x = um comando qualquer):
```
S -> i S | i S e S | x
```
- WIDGET PARSE_TREE gramática=«S -> i S | i S e S | x» entrada=«i i x e x» legenda: O else pertence ao primeiro if ou ao segundo?
- CAIXA [No mundo real]: C, Java e companhia decidem: o else casa com o if **mais próximo** que ainda não tem else. O Yacc/Bison implementa isso preferindo shift no conflito.
- REVELAR: Por que ambiguidade é um problema para um compilador?
    → Porque cada árvore pode ter um significado diferente (14 ou 20; o else de um if ou do outro). O compilador precisa de UMA tradução determinística.

#### ESTILO WORKED (Exemplo resolvido): "Provando que uma gramática é ambígua"
- PROF: Para PROVAR ambiguidade, basta exibir UMA sentença com duas derivações mais à esquerda diferentes. Vamos lá.
- CÓDIGO:
```
E -> E + E | E * E | ( E ) | id
```
- PASSOS (Derivação mais à esquerda nº 1 (+ na raiz)):
    1) E ⇒ E + E
    2) ⇒ id + E
    3) ⇒ id + E * E
    4) ⇒ id + id * E
    5) ⇒ id + id * id
- PASSOS (Derivação mais à esquerda nº 2 (* na raiz)):
    1) E ⇒ E * E
    2) ⇒ E + E * E
    3) ⇒ id + E * E
    4) ⇒ id + id * E
    5) ⇒ id + id * id
- TEXTO: Duas derivações mais à esquerda distintas para a mesma sentença ⇒ duas árvores ⇒ **gramática ambígua**. ∎
- CAIXA [Cuidado!]: Uma derivação ⇒lm e outra ⇒rm diferentes NÃO provam nada: a mesma árvore tem as duas! Compare sempre lm com lm (ou rm com rm), ou compare árvores.
- TEXTO: Sua vez, esmaecido: mostre que `id * id + id` também tem duas árvores.
- REVELAR: Primeira derivação ⇒lm (+ na raiz)?
    → E ⇒ E + E ⇒ E * E + E ⇒ id * E + E ⇒ id * id + E ⇒ id * id + id
- REVELAR: Segunda derivação ⇒lm (* na raiz)?
    → E ⇒ E * E ⇒ id * E ⇒ id * E + E ⇒ id * id + E ⇒ id * id + id
- WIDGET PARSE_TREE gramática=«E -> E + E | E * E | ( E ) | id» entrada=«id * id + id» legenda: Confira: duas árvores.
- REVELAR: Qual das duas respeita a precedência usual?
    → A com + na raiz: (id * id) + id, o * fica embaixo e é calculado primeiro.
- CAIXA [Dica de prova]: Roteiro de resposta: (1) escolha a sentença; (2) mostre duas derivações MAIS À ESQUERDA (ou duas árvores); (3) conclua. Para desambiguar, apresente E → E + T | T, T → T * F | F, F → ( E ) | id e explique: um nível por precedência; recursão à esquerda = associatividade à esquerda.

#### ESTILO SOCRATIC (Perguntas guiadas): "A culpa é da gramática ou da linguagem?"
- PREVEJA: Se UMA única sentença tem duas árvores, a gramática é ambígua?
    opções: ✔Sim, basta uma | Não, precisa ser a maioria | Só se todas tiverem
    explicação: Basta um contraexemplo. Uma única sentença com duas árvores já torna a gramática ambígua.
- PREVEJA: `E -> E + E | E * E | ( E ) | id` e a gramática E/T/F geram a mesma linguagem?
    opções: ✔Sim | Não
    explicação: Ambas geram todas as expressões com +, *, parênteses e id. Uma é ambígua; a outra, não.
- PROF: Então…
- REVELAR: Ambiguidade é propriedade da gramática ou da linguagem?
    → Da GRAMÁTICA, em geral: a mesma linguagem pode ter gramáticas ambíguas e não ambíguas. (Existem linguagens 'inerentemente ambíguas', em que TODA gramática é ambígua, raras, e não aparecem em linguagens de programação.)
- PREVEJA: Na gramática E/T/F, por que o * sempre fica mais embaixo na árvore?
    opções: ✔Porque * só aparece em T, um nível abaixo de E | Por sorte | Porque * é escrito primeiro
    explicação: Para chegar a um *, você precisa passar por T; e T só aparece abaixo de E. Cada nível da gramática é um nível de precedência.
- WIDGET PARSE_TREE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» entrada=«id * id + id»
- PREVEJA: E por que `id + id + id` agrupa à esquerda em E → E + T?
    opções: ✔Porque a recursão está à esquerda: só o E da esquerda pode conter outro + | Porque o analisador lê da esquerda
    explicação: O operando da direita é um T, que não contém + (sem parênteses). Então o + 'extra' só cabe na subárvore da esquerda.
- BYTE: Existe algum programa que diz se QUALQUER gramática é ambígua?
- PREVEJA: Existe um algoritmo que decide, para qualquer GLC, se ela é ambígua?
    opções: Sim | ✔Não: é um problema indecidível
    explicação: Indecidível! Por isso usamos condições suficientes: se a gramática é LL(1) ou LR(1), com certeza não é ambígua.
- REVELAR: Duas formas de lidar com uma gramática ambígua.
    → 1) Reescrever a gramática (níveis de precedência; else casando com o if mais próximo). 2) Manter a ambígua e dar regras de desambiguação ao gerador (no Bison: %left, %right, %nonassoc e preferência por shift).

#### ESTILO FORMAL (Definição de prova): "Ambiguidade: definição e desambiguação"
- TEXTO: **Definição.** Uma gramática G é **ambígua** se existe alguma sentença w ∈ L(G) com mais de uma árvore de derivação. Equivalentemente: w tem mais de uma derivação mais à esquerda, ou mais de uma derivação mais à direita.
- TEXTO: Uma linguagem é **inerentemente ambígua** se toda GLC que a gera é ambígua. Exemplo: { aⁱbʲcᵏ | i = j ou j = k }.
- TEXTO: **Fato.** Não existe algoritmo que decida se uma GLC arbitrária é ambígua (o problema é indecidível).
- CÓDIGO:
```
Desambiguação de expressões (um nível por precedência):
  E → E + T | T        (+ : menor precedência, assoc. à esquerda)
  T → T * F | F        (* : maior precedência, assoc. à esquerda)
  F → ( E ) | id       (átomos)
Regras:
  operador de MENOR precedência fica mais perto da raiz;
  recursão à ESQUERDA → associatividade à esquerda;
  recursão à DIREITA  → associatividade à direita (ex.: ^).
```
- CÓDIGO (Else pendurado sem ambiguidade (Livro do Dragão, §4.3.2)):
```
stmt → matched_stmt | open_stmt
matched_stmt → if ( expr ) matched_stmt else matched_stmt | other
open_stmt → if ( expr ) stmt
          | if ( expr ) matched_stmt else open_stmt
```
- PREVEJA: Para provar que G é ambígua, é suficiente mostrar…
    opções: uma derivação ⇒lm e outra ⇒rm diferentes para w | ✔duas derivações ⇒lm diferentes para a mesma w | que G tem recursão à esquerda
    explicação: Duas ⇒lm distintas = duas árvores distintas. Uma ⇒lm e uma ⇒rm podem ser da mesma árvore; e recursão à esquerda não tem nada a ver com ambiguidade.
- PREVEJA: Toda gramática LL(1) ou LR(1) é…
    opções: ambígua | ✔não ambígua | depende da entrada
    explicação: Um analisador determinístico constrói UMA árvore por entrada; se a tabela não tem conflitos, a gramática não é ambígua.
- CAIXA [Dica de prova]: Clássico: 'Mostre que a gramática do if-then-else é ambígua usando `if E1 then if E2 then S1 else S2`' e 'dê uma gramática equivalente não ambígua'. Decore a ideia matched/open: entre um then e um else só pode vir comando 'casado'.
- WIDGET PARSE_TREE gramática=«S -> i S | i S e S | x» entrada=«i i x e x» legenda: O contraexemplo clássico, em miniatura.
- REVELAR: Por que recursão à esquerda dá associatividade à esquerda?
    → Em E → E + T, o operando que pode conter outros + é o da ESQUERDA. Em a + b + c, o a + b vira a subárvore da esquerda: (a + b) + c.

#### ESTILO MNEMONIC (Truque de memória): "Duas árvores, um problema"
- CAIXA [Gancho de memória]: **Ambi = dois.** Ambidestro usa as duas mãos; gramática ambígua tem DUAS árvores para a mesma frase.
- CAIXA [Gancho de memória]: Prova de ambiguidade = **'duas da mesma mão'**: duas mais à esquerda (ou duas mais à direita). Uma de cada mão não vale!
- CAIXA [Gancho de memória]: **Precedência = profundidade.** Quem tem MAIS precedência fica MAIS FUNDO na árvore (é calculado antes). O + fica perto da raiz; o * mais embaixo.
- CAIXA [Gancho de memória]: **A recursão aponta a associatividade.** E → **E** + T: recursão à esquerda → associa à esquerda. P → B ^ **P**: recursão à direita → associa à direita.
- TEXTO: **Palácio da memória**: no Vale das Gramáticas há duas árvores gêmeas plantadas lado a lado, com a mesma placa: `id + id * id`. É o minijogo **Duas Árvores**.
- CAIXA [Gancho de memória]: **Else pendurado casa com o vizinho**: o else fica com o if mais próximo que ainda está sem par.
- PREVEJA: Em `2 ^ 3 ^ 2` (potência, associativa à direita), qual o agrupamento correto?
    opções: (2 ^ 3) ^ 2 = 64 | ✔2 ^ (3 ^ 2) = 512
    explicação: Associativa à direita: o de mais à direita se junta primeiro. 3² = 9, e 2⁹ = 512.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que ambiguidade é da linguagem, quase sempre é da gramática; (2) achar que recursão à esquerda causa ambiguidade, E → E + T é recursiva à esquerda e NÃO é ambígua; (3) achar que dá para testar ambiguidade automaticamente sempre (é indecidível).
- WIDGET PARSE_TREE gramática=«E -> E + E | E * E | ( E ) | id» entrada=«id + id * id» legenda: As gêmeas: mesma placa, estruturas diferentes.
- REVELAR: Recite os ganchos: ambi, mesma mão, profundidade, vizinho.
    → Ambígua = duas árvores; prova com duas derivações da mesma mão (lm com lm); maior precedência = mais fundo; o else casa com o if vizinho.
