# Relatório do professor — Bloco 2: Vale das Gramáticas (região 3) e Ponte da Transformação (região 4)

## Veredito

O bloco está tecnicamente limpo: refiz todas as derivações e todas as transformações e não achei nenhuma que ensine errado. A ambiguidade é apresentada como propriedade da gramática, com duas árvores para a mesma frase, e o erro "uma ⇒lm e uma ⇒rm provam ambiguidade" é atacado três vezes. O maior problema é de desenho: **a trilha principal (aula animada + pegadinhas) é bem mais rasa que os textos "Ler com calma"**. Quem só percorre a trilha nunca vê a gramática E/T/F ser construída, nunca faz uma derivação mais à direita, nunca vê ε numa fatoração e nunca é avisado de que tirar a recursão à esquerda mexe na árvore. Este último ponto é o mais grave: a região 3 ensina "recursão à esquerda = associa à esquerda" e a região 4 diz "só a recursão passa para a direita", sem dizer que a linguagem é a mesma mas a árvore não. O aluno sai com base suficiente para FIRST/FOLLOW e tabela LL(1); sai com base fraca para LR (derivação mais à direita) e para o TP (associatividade no parser descendente).

## Erros técnicos

Não achei erro de gravidade alta. Conferi refazendo:

- Derivações: `float * id ;` (3 passos); `id + id` em E→TX; ⇒lm e ⇒rm de `id + id * id` em E/T/F (8 passos cada, igual aos 8 nós internos); `aaabbb` (4 passos); as quatro ⇒lm do texto WORKED de ambiguidade (`id + id * id` e `id * id + id`); contas 9−5−2 (6 e 2) e 2^3^2 (512).
- Recursão à esquerda: E→E+n|n; L→L,id|id; E→E+T|E−T|T; S→Sa|b; E/T/F completa; A→Ac|Aad|bd|ε (Dragão, ex. 4.20); recursão indireta S→Aa|b, A→Sc|d **pelos dois caminhos** (pegadinha: S→Sca|da|b; texto: A→Aac|bc|d). Os dois geram (da|b)(ca)*. Algoritmo 4.19 e a propriedade j > i conferem.
- Fatoração: A→ab|ac; C→id=n|id()|id(n) (duas rodadas, resultado LL(1)); A→abc|abd|ae|f; else pendente (conflito em [S', e] confere: FOLLOW(S') = {e, $}); L→S;L|S (resultado LL(1)); A→aB|aBc.
- Gramática matched/open do else: confere com o Dragão, §4.3.2.

Imprecisões:

| Assunto | Onde | Trecho | O que está errado | Conserto | Grav. |
|---|---|---|---|---|---|
| LEFT_FACTORING | animada c.11 × ficha × texto WORKED × texto FORMAL | Ficha: "O maior prefixo comum a duas ou mais alternativas". Animada: "maior começo comum das três" → certo = `id`, errado = `id (` | Pela regra da ficha e do Algoritmo 4.21, o maior prefixo comum a duas ou mais é `id (`. A aula marca como erro o que a ficha manda fazer. O WORKED faz o mesmo: enuncia "duas ou mais" e resolve com "comum às TRÊS: só a" (pela regra enunciada seria `a b`). O resultado final é equivalente, mas a regra não é a mesma. | Uma só formulação em todo lugar: "Junte as alternativas que começam com o mesmo símbolo. Ponha em evidência o maior prefixo comum a **todas** do grupo. Repita no não-terminal novo." No FORMAL, acrescentar: "O livro escolhe o maior prefixo comum a duas ou mais; dá outra ordem de rodadas e a mesma gramática no fim." | média |
| LEFT_RECURSION | animada c.8, c.20; prática c.5; texto STORY | "A linguagem é a mesma… Só mudou o lado da recursão." / "Muda só o formato da árvore." | Verdadeiro, mas incompleto de um jeito que custa ponto: a árvore de `n − n − n` passa a pender para a direita. O aluno que acabou de aprender "a recursão aponta a associatividade" conclui que o menos virou associativo à direita. | Ver melhoria 1. | média |
| AMBIGUITY | animada c.16 e c.22; Bug das Duas Contas | "As duas árvores dão contas diferentes. Pela receita, a gramática é ambígua." / "duas árvores são duas contas" | Liga ambiguidade a resultado diferente. Com `+` as duas árvores dão o mesmo valor e a gramática é ambígua do mesmo jeito (o texto VISUAL diz isso; a trilha não). | c.16: "Duas árvores para a mesma frase: a gramática é ambígua. Aqui ainda por cima as contas dão resultados diferentes." | média |
| CFG_DERIVATION | Bug do Terminal Trocado × ficha | "[V] As folhas da árvore de uma frase são terminais." | A ficha lista como erro "achar que ε não pode ser folha" e o FORMAL diz "folhas são terminais ou ε". | "[V] As folhas da árvore de uma frase são terminais (ou ε, quando uma regra some)." | baixa |
| AMBIGUITY | pegadinha do else; Bug do Else Solto | "Duas árvores, a gramática é ambígua." / "tem duas árvores nessa gramática" | Nenhuma gramática aparece na tela. Fala-se de propriedade da gramática sem gramática. | Mostrar `S -> if S | if S else S | x` na cena 1, como no texto VISUAL. | média |
| AMBIGUITY | Bug das Duas Contas | "[V] Duas derivações pela esquerda diferentes provam ambiguidade." | Falta "da mesma frase". | Acrescentar "da mesma frase". | baixa |
| LEFT_FACTORING | texto FORMAL | "São condições necessárias, não suficientes." | Fatorada não é estritamente necessária: A→Ba|Bb com B→ε tem prefixo comum e é LL(1). | "Na prática são os dois preparos obrigatórios, mas não garantem LL(1)." | baixa |
| LEFT_RECURSION | texto FORMAL | "Pré-condição: sem ciclos e sem produções-ε… Na prova, cite isso!" seguido do exemplo com A→ε | O exemplo viola a pré-condição recém-enunciada, sem aviso. | Acrescentar: "Este exemplo tem A → ε e mesmo assim dá certo; o livro avisa que aqui o ε é inofensivo." | baixa |
| LEFT_FACTORING | texto STORY | "Se duas alternativas começam igual, ele trava." | "Travar" foi usado na recursão à esquerda para laço infinito. Aqui o parser não entra em laço: fica sem saber escolher. | "…ele não tem como escolher." | baixa |
| LEFT_RECURSION | glossário | "Regra em que o símbolo começa com ele mesmo" | Quem começa é o lado direito. | "Regra cujo lado direito começa com o próprio símbolo da esquerda, como E -> E + T." | baixa |

## Problemas de didática e estrutura

**CFG_DERIVATION**
- (alta) **Derivação mais à direita não é praticada na trilha.** Aparece só numa fala de acerto (c.20) e na pegadinha. É a base do LR ("mais à direita ao contrário"). Acrescentar 3 cenas depois da c.10: mesma frase, trocando sempre o da direita, e a pergunta "a árvore mudou?".
- (alta) **A pegadinha "Duas derivações, uma árvore" pergunta "Isso é ambiguidade?" antes de a palavra ser ensinada** (não aparece nas regiões 1 e 2; a definição vem no assunto seguinte). Trocar por "As duas derivações desenham árvores diferentes?" ou mover a pegadinha para AMBIGUITY, onde ela já é o erro comum da c.20.
- (média) **ε como produção não é ensinado na aula animada.** Estreia na prática: "ou épsilon para acabar". O aluno conhece ε de autômato (região 2), não de gramática. Pôr uma cena com `S -> a S b | ε`: "ε é o nada: esta regra apaga o S".
- (média) **"Forma sentencial" e "sentença" são cobradas no Bug e na ficha sem terem sido ditas na aula animada** (que usa "frase" e "forma"). Dizer os dois nomes na c.9 e na c.10.
- (média) **A receita (c.11) diz qual não-terminal trocar, mas não qual regra escolher**, que é a dificuldade real. Passo 2 novo: "Troque o não-terminal mais à esquerda pela regra que combina com o próximo pedaço da meta." A pergunta da c.12 ("qual passo escolhe o E da esquerda?") só pede releitura; cortar. A c.8 se acerta pela fala ("mais à esquerda").
- (baixa) Prática c.5: "a tabela LL(1) já está na sua cabeça" é termo não ensinado. Cortar.
- Textos: bons. STORY (gramática do português), WORKED (lm e rm lado a lado, dica dos nós internos) e SOCRATIC têm valor próprio.

**AMBIGUITY**
- (alta) **A gramática E/T/F nunca é montada na trilha.** A c.9 e o resumo dizem "um nível por precedência" e não mostram. O conserto demonstrado (E→E−n|n) trata só de associatividade. E/T/F aparece num flashcard e nos textos, e é a gramática de todo o resto do jogo (a abertura da região 4 já usa `E -> E + T | T` com T sem explicação). Ver melhoria 2.
- (alta) **Trilha sem produção.** Não tem "Fazer com ajuda" nem prática; o aluno toca em "1 ou 2", nunca desenha as duas árvores nem escreve a gramática consertada. A questão de prova é "mostre que é ambígua e dê uma equivalente não ambígua".
- (média) Pegadinha do else, c.5: "O Bison resolve empilhando o else" usa "empilhar" antes da região 6. Trocar por "As ferramentas ficam com o if de dentro; você vai ver como na Oficina do Bison."
- (média) Nomes: "else solto" (pegadinha, Bug) × "else pendurado" (ficha, textos). Escolher um; sugiro "else pendente", o nome das traduções do Dragão.
- (baixa) Distrator "16" na c.15 não corresponde a erro real; trocar por "−2".
- O que funciona: c.11, c.20 e c.21 atacam exatamente os erros de prova. Textos WORKED e FORMAL estão no nível de prova. A "calculadora de camelô" (STORY) é detalhe sedutor e impreciso (não é outra gramática, é execução imediata); cortar.

**LEFT_RECURSION**
- (alta) **Associatividade.** Só o FORMAL toca no assunto: "a associatividade fica 'escondida' em A' e é recuperada com atributos herdados" — termo da região 8. A solução existe adiante (AST_BUILD e o guia do TP: laço `e = new BinOp(op, e, termo())`), mas a Ponte não aponta para ela.
- (alta) **O segundo exemplo só troca as letras** (E→E+n|n e L→L,id|id têm o mesmo formato). Faltam na trilha: duas alternativas recursivas (E→E+T|E−T|T), dois níveis (E/T/F, o exercício clássico) e β = ε. Estão todos no texto WORKED. Trocar o "Sua vez" por E→E+T|E−T|T e levar E/T/F para uma prática.
- (média) **Recursão indireta: tratada, mas pela metade e de dois jeitos.** A pegadinha substitui A dentro de S e para em "a receita de sempre resolve"; o SOCRATIC substitui S dentro de A (a ordem do livro). Terminar a pegadinha (S→daS'|bS'; S'→caS'|ε) e dizer em um dos dois: "tanto faz qual você abre; o algoritmo do livro fixa uma ordem para funcionar sempre".
- (média) **Ninguém confere a equivalência na situação difícil.** A c.16 deriva `id , id` na gramática nova; falta derivar a mesma frase nas duas e comparar as árvores (o texto VISUAL faz com `b a a`).
- (média) A prática "Rode a versão consertada" usa pilha, `$` e "trocar/casar", ensinados só na região 5. Ou explicar `$` em uma fala, ou trocar por derivação.
- (baixa) Notação: `E'` na aula, `X` nas práticas e Bugs; `casa()` na aula, `match()` no texto. Unificar. "L linha" é falado sem aviso; dizer uma vez "E', lê-se E linha".
- (baixa) Bug "[V] Parser LR aceita recursão à esquerda": LR não foi ensinado na trilha.
- Textos: todos úteis; SOCRATIC ("teria que adivinhar quantos a's virão") é a melhor explicação do bloco. O VISUAL usa tabela LL(1) antes da região 5; avisar "você vai aprender a montar esta tabela no Templo".

**LEFT_FACTORING**
- (alta) **O caso com ε não está na trilha.** A→αβ|α (if com e sem else; E→T+E|T) é o que mais cai e onde mais se erra. Está na ficha e nos textos, não na aula. Acrescentar como terceiro exemplo curto ou na pegadinha.
- (alta) **Trilha mais magra do bloco**: abertura, aula, pegadinha, desafio. Nenhuma etapa em que o aluno escreve a gramática fatorada.
- (média) c.9 pergunta "qual passo guarda o que sobrou?": releitura da receita. Trocar por `A -> a b | a c | d`: "o que acontece com o d?" (fica como está), caso que a receita não cobre.
- (baixa) Bug "Conflito com FOLLOW continua lá" e texto VISUAL usam FIRST/FOLLOW antes da região 5.
- O que funciona: o segundo exemplo traz caso novo de verdade (segunda rodada), e a pegadinha do prefixo escondido é boa.

**Sequência**
- (média) Pré-requisitos frouxos: LEFT_RECURSION não exige AMBIGUITY (mas usa E→E+T|T) e FIRST exige só CFG_DERIVATION, então dá para chegar à tabela LL(1) sem passar pela Ponte. Sugiro LEFT_RECURSION ← AMBIGUITY e LL1_TABLE ← LEFT_RECURSION, LEFT_FACTORING.
- (média) Falta fecho do bloco: um exercício integrado "torne LL(1)" (tirar recursão, depois fatorar) e a ordem das duas cirurgias, que só aparece no FORMAL de fatoração.

## Lacunas

1. Efeito da eliminação da recursão sobre árvore e associatividade (na trilha).
2. Construir gramática não ambígua para um conjunto novo de operadores (por exemplo −, /, ^ e menos unário): um nível por precedência, lado da recursão por associatividade. Nenhuma versão pede isso ao aluno.
3. Derivação mais à direita praticada e ligada ao LR.
4. Fatoração com ε e o else pendente fatorado, na trilha.
5. Exercício integrado de recursão + fatoração numa gramática de comandos parecida com a do TP.
6. Recursão indireta levada até o fim, com três não-terminais.
7. Menos importante: EBNF ({ } e [ ]), que é como o aluno vai escrever a gramática do TP e que explica o "A' é um while disfarçado"; aviso de que nem toda linguagem tem gramática LL(1).

## Nota por assunto

| Assunto | Correção | Didática | Cobertura p/ prova | Uma frase |
|---|---|---|---|---|
| CFG_DERIVATION | 5 | 3 | 3 | Certo e claro, mas a trilha não pratica ⇒rm nem ε e cobra termos que não disse. |
| AMBIGUITY | 4 | 3 | 3 | Definição e erros de prova muito bem atacados; E/T/F e a produção das árvores ficam fora da trilha. |
| LEFT_RECURSION | 4 | 3 | 3 | Transformações todas corretas; cala sobre associatividade e o "Sua vez" repete o primeiro exemplo. |
| LEFT_FACTORING | 4 | 3 | 2 | Bom segundo exemplo; regra do "maior prefixo" contraditória, sem ε e quase sem prática. |

## As 5 melhorias que eu faria primeiro

1. **Associatividade na Ponte.** Duas cenas depois da c.17 de "O laço sem fim": as árvores de `n − n − n` na gramática velha e na nova, e a fala: "A linguagem é a mesma, mas a árvore agora pende para a direita. O menos continua sendo da esquerda: quem garante isso é o código do parser, que junta os pedaços num laço, da esquerda para a direita. Você vai ver na Descida Recursiva e na AST." Trocar o resumo por: "A linguagem não muda; a árvore muda."
2. **Montar E/T/F na aula animada de ambiguidade.** Depois da c.19, quatro cenas: `E -> E + T | T`, `T -> T * n | n`, a árvore única de `n + n * n` e a pergunta "por que o vezes fica sempre mais fundo?". Declarar AMBIGUITY como pré-requisito de LEFT_RECURSION.
3. **Uma só regra de fatoração** ("maior prefixo comum a todas as alternativas do grupo") em aula, ficha e textos, e **o caso com ε na trilha** (`S -> if S | if S else S | x`, que ainda reaproveita o else).
4. **Fazer o aluno produzir.** "Fazer com ajuda" e uma prática para AMBIGUITY, LEFT_RECURSION e LEFT_FACTORING (hoje só CFG_DERIVATION tem), com o aluno montando a gramática resultante e, em ambiguidade, as duas árvores. Trocar o "Sua vez" de recursão por E→E+T|E−T|T e terminar a recursão indireta.
5. **Consertar a ordem dos termos em CFG_DERIVATION:** ensinar ⇒rm com três cenas, apresentar ε como regra, dizer "forma sentencial" e "sentença" na aula e tirar a palavra "ambiguidade" da pegadinha (ou movê-la para o assunto seguinte).
