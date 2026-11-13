# Revisão das aulas: o que foi feito, o que foi achado e como continuar

Feita em 12 e 13 de novembro de 2026. A revisão parou na metade por limite de uso. Este arquivo serve para outra sessão (por exemplo, com o Opus) continuar sem refazer nada.

## Situação

| Bloco | Professor | Aluno |
|---|---|---|
| 1. Porto e Floresta Léxica (regiões 1 e 2) | pronto, lido | pronto, lido |
| 2. Vale e Ponte (regiões 3 e 4) | gravado, não lido | gravado, não lido |
| 3. Templo Top-Down (região 5) | pronto, lido | pronto, lido |
| 4. Bottom-Up e Bison (regiões 6 e 7) | pronto, lido | gravado, não lido |
| 5. Semântica e Forja (regiões 8 e 9) | **falta** | gravado, não lido |
| 6. Seu compilador (série em C e Guia do TP) | **falta** | gravado, não lido |

"Gravado, não lido": o revisor gravou o arquivo antes de o limite cair, mas eu não li nem conferi. Pode estar incompleto; veja se tem a seção final "As 5 coisas que eu mudaria primeiro". Os achados deste documento vêm só dos 5 relatórios lidos. Faltam de fato só os dois professores dos blocos 5 e 6.

Nada do jogo foi alterado. Só existem arquivos novos em `docs/`.

## Onde está cada coisa

- `docs/revisao/relatorios/`: os 10 relatórios gravados, com trecho citado e conserto proposto para cada achado.
- `docs/revisao/roteiro_professor.md` e `roteiro_aluno.md`: as instruções que cada revisor recebeu. Reaproveite sem mudar.
- `docs/revisao/transcricoes/`: tudo o que o aluno lê, em texto, por região (21 arquivos, 875 KB). `*_A_trilha.md` tem abertura, aula animada, práticas, pegadinhas, clipes, Bugs e ficha. `*_B_texto.md` tem as aulas "Ler com calma".
- `docs/revisao/medidas.py`: script que conta cenas, perguntas e posição da resposta certa nas transcrições.

As transcrições foram geradas por um teste temporário (já apagado) que percorria `Episodes`, `Library`, `Aberturas`, `Album` e `Glossario` e escrevia em Markdown, rodado com `cd verify && ../gradlew test`. Se as aulas mudarem, é preciso gerar de novo.

## Como continuar gastando pouco

1. Rode **um revisor por vez**, e não doze em paralelo. Cada um gasta cerca de 200 mil tokens.
2. Prompt para cada um (troque papel, arquivos e nome do relatório):

   > Leia `docs/revisao/roteiro_professor.md` e siga à risca. Seu bloco são os arquivos `docs/revisao/transcricoes/regiao3_*` e `regiao4_*`. Leia em pedaços grandes, sem reler. Grave o relatório em `docs/revisao/relatorios/professor_2_vale_ponte.md`.

   Os roteiros citam o caminho antigo das transcrições (`/tmp/.../scratchpad/dump/`). Diga no prompt que agora elas estão em `docs/revisao/transcricoes/`.
3. O que falta rodar: professor do bloco 5 e professor do bloco 6.
4. Antes disso, o passo mais barato: peça para ler os 5 relatórios "gravados, não lidos" e acrescentar os achados deles a este documento.

## Achados conferidos (eu verifiquei cada um na transcrição ou no código)

### Erros que ensinam errado

1. **Recuperação de erros se contradiz.** Na aula, cena 6, com `x = * 3 ;` a resposta certa é `;` e o `3` é errado. Pela receita da própria aula (cena 10, regra 2), o parser pararia no `3`, que está no FIRST de `Val`. A pegadinha "Parar ou seguir" usa `id = = num ; id` e marca `;` como certo; a cena 15 usa a mesma entrada e marca `num`. A abertura e o clipe repetem o problema. Conserto: trocar as quatro entradas por uma sem token do FIRST antes do `;`, como `x = * ; y = 2 ;`.
2. **Glossário define handle com o erro que a aula combate**: "O pedaço do topo da pilha que casa com o lado direito de uma regra". O Bug do álbum marca essa ideia como falsa.
3. **Itens LR(0), aula de tabelas, cena 7**: pede as caixas "com item com o ponto no fim" e marca I1 como errado, mas I1 tem `E' → E •`.
4. **Tokens**: "token" é usado como o pedaço cortado (cena 5), como a classe (cenas 10 e 20) e, no glossário, como "o 10 é um token do tipo número". O aluno sai sem saber quantos tokens tem `x = y`.
5. **Tokens, cena 1**: "O parser trabalha com palavras". A palavra "parser" não foi apresentada antes.

### Padrões que valem para o jogo inteiro

1. **A resposta certa quase nunca é a última opção.** Nas 137 perguntas "Preveja" com 4 opções, a última é a certa 0 vezes e a segunda 84 vezes. Nas 29 aberturas, a terceira opção é a certa 1 vez. As opções não são embaralhadas (`ui/screens/LessonScreen.kt:169` e `ui/screens/EpisodeScreen.kt:543`). Conserto barato: embaralhar na hora de mostrar.
2. **"Erros comuns" tem sempre a resposta "errado"**: 29 de 29 aulas.
3. **92 das 192 perguntas das aulas animadas têm duas opções.**
4. **234 das 239 falas de acerto começam com "Isso".**
5. **Personagens**: nas trilhas o Byte tem 870 falas, a professora 63 e o Bug nenhuma. Os revisores acharam as aulas animadas secas e as de texto engraçadas.
6. **Trecho só ouvindo**: até 6 cenas seguidas sem interação. Em FIRST, FOLLOW, tabela LL(1), descida recursiva, itens LR(0) e tabela SLR, a primeira interação só vem na cena 6 ou 7.

## Achados dos revisores (não conferi um a um; os relatórios trazem o trecho)

Os três professores refizeram todas as contas e **não acharam nenhum conjunto, tabela, autômato ou traço de pilha errado**. Os problemas são de desenho:

1. **O aluno reconhece, mas não produz.** A trilha animada nunca pede a questão de prova inteira: gramática → FIRST → FOLLOW → tabela → veredito; ou itens LR(0) → tabela SLR → conflito; ou a tabela de subconjuntos; ou a tabela de pares da minimização. Os exemplos resolvidos existem, mas só nas aulas de texto, que são opcionais. Este é o achado mais importante.
2. **Os exemplos da aula animada evitam o caso difícil.** O ε-fecho depois do move nunca acrescenta estado; não aparece estado morto; no FOLLOW nunca há dois símbolos depois do não-terminal; a regra anulável é sempre o ε escrito; o ponto fixo nunca muda nada na segunda rodada; todas as gramáticas do "Fazer com ajuda" são LL(1). Na "prova de bolso", o revisor aluno errou o FOLLOW de `S → A B d` usando só a receita da aula.
3. **Ficha e Bugs cobram o que a aula animada não ensinou**: Thompson, estado morto, `yytext`, front-end e back-end, tabela de símbolos, prefixo viável, `error` e `yyerrok`.
4. **A receita do FOLLOW usa α e β sem explicar.** As frases faladas são melhores que a fórmula da tela.
5. **Nomes demais para a mesma coisa**: `casa`, `casaToken` e `match`; `X`, `E'` e `A'`; closure, fecho e fechamento; cadeia, palavra e frase; "núcleo" para kernel e para core.
6. **Sem prática de produção** em AFN→AFD, minimização e itens LR(0): não têm "Fazer com ajuda".
7. **Outras contradições**: "só a semântica consulta a tabela de símbolos" contra a ficha; minimização, cena 8 contra cena 18; "LL(1): sem recursão à esquerda e fatorada" lido como definição; LALR mostrado como fusão de itens soltos, e não de estados; pegadinha do handle explicada por "ordem da conta" numa gramática em que o problema é beco sem saída.

O que funciona, segundo os revisores: a aula de Tokens, a pegadinha do `ifa`, a abertura do FOLLOW, o experimento `5 - 2 - 1` no Bison, os textos "exemplo resolvido" e a correção técnica em geral.

## As 6 mudanças que eu faria primeiro

1. Corrigir os 5 erros da seção "Erros que ensinam errado".
2. Embaralhar as opções e fazer o "colega" acertar em parte das aulas.
3. Criar a etapa "Questão de prova" em cada assunto procedimental, com apoio que diminui: um exemplo resolvido, um para completar, um sozinho. Os exemplos já estão escritos nas aulas de texto.
4. Trocar o segundo exemplo ("Sua vez") de cada aula por um que tenha o caso difícil. Os relatórios dizem qual usar em cada assunto.
5. Alinhar ficha e Bugs com a aula animada, e escolher um nome por conceito.
6. Dar falas à professora e ao Bug nas aulas animadas e variar o "Isso".

## Pesquisa

Resumos obtidos por busca na web, sem leitura do artigo inteiro. Confira antes de citar. O que já estava em `PESQUISA-DIVERSAO.md` e `PESQUISA-LAB-IA.md` não está repetido.

### Como desenhar a aula

| Ideia | Evidência | Uso aqui |
|---|---|---|
| Rótulos de subobjetivo: a receita diz para que serve cada passo | Margulieux e Morrison, [IJ STEM Education 2020](https://doaj.org/article/9a2c85d66fd54ec9ab019e9a510bbf17): melhor desempenho inicial, menos desistência, efeito pequeno | Reescrever as receitas (FOLLOW, itens LR(0)) com o propósito de cada passo |
| Exemplo resolvido → para completar → sozinho | [Efeito do exemplo resolvido](https://en.wikipedia.org/wiki/Worked-example_effect) e [reversão por experiência](https://en.wikipedia.org/wiki/Expertise_reversal_effect); um estudo de 2019 achou o exemplo completo melhor que o parcial | Base da "Questão de prova" |
| Explicar com as próprias palavras | [Bisra et al. 2018](https://doi.org/10.1007/s10648-018-9434-x): g = 0,55 em 64 estudos; múltipla escolha rende menos | Um "diga por quê" depois de cada "Sua vez" |
| Exemplo errado | Resultado misto; só ajuda [com a correção junto](https://par.nsf.gov/servlets/purl/10155531) | A parte "Erros" já faz isso |
| Segmentar, sinalizar e apresentar os nomes antes | Sinalização [d = 0,38](https://rex.libraries.wsu.edu/esploro/outputs/journalArticle/A-meta-analysis-of-signaling-principle-in/99900601052901842); segmentação [pequeno a médio em 56 estudos](https://www.doi.org/10.1007/S10648-018-9456-4) | Termo novo explicado na primeira vez |
| Do concreto ao abstrato | [Revisão de Fyfe 2014](https://pc.cogs.indiana.edu/?p=885); em ciências a evidência é fraca | Manter exemplo antes do termo, sem tratar como regra |
| Passos pequenos com prática depois de cada um | Rosenshine, [10 princípios](https://my.chartered.college/?p=5331) | Reduzir os trechos de 6 cenas só ouvindo |
| Reaprender em dias diferentes até acertar | [Rawson e Dunlosky 2013](https://springerlink.bibliotecabuap.elogim.com/doi/10.1007/s10648-013-9240-4): nota subiu de C para B; sem agenda imposta o ganho some | O jogo já espaça; exigir acerto antes de dar o assunto por revisado |
| Gamificação | [Resultados mistos](https://cacm.acm.org/news/the-games-people-play); ranking desanima quem está embaixo | Sem ranking |

### Ensino de compiladores

- Não achei estudo que meça quais conceitos de LL e LR os alunos erram. A lista de erros do roteiro do professor veio de notas de aula: [Washington CSE 401](https://courses.cs.washington.edu/courses/cse401/19au/sections/04-LL-worksheet.pdf), [Illinois](https://courses.engr.illinois.edu/cs421/sp2010/lectures/lecture8addendum.pdf), [Lund](https://fileadmin.cs.lth.se/cs/Education/EDAN65/2023/exercises/E05.pdf) e [Cambridge](https://www.cl.cam.ac.uk/teaching/1920/CompConstr/CC_2020_ParsingII.pdf).
- Prova típica em disciplinas brasileiras: reescrever a gramática para LL(1), calcular FIRST e FOLLOW e montar a tabela ([UFRJ](https://fabiomascarenhas.github.io/comp/09LL1.pdf), [PUC-Rio](https://hisham.hm/puc-rio/comp/aula5.pdf)). Não achei levantamento de POSCOMP ou ENADE.
- Construir o compilador aos poucos, com algo rodando a cada passo: [Ghuloum](https://notes.billmill.org/programming/compilers/An_Incremental_Approach_to_Compiler_Construction.html), [Siek](https://penguinrandomhouse.com/books/744514/essentials-of-compilation-by-jeremy-g-siek) e [Crafting Interpreters](https://eli.thegreenplace.net/2022/book-review-crafting-interpreters-by-robert-nystrom/). São argumentos de autor, sem estudo controlado. Vale conferir se o Guia do TP roda algo cedo (bloco 6, ainda não revisado).
- O Bison mostra uma frase que prova o conflito com [`-Wcounterexamples`](https://www.gnu.org/software/bison/manual/html_node/Counterexamples.html), baseado em [Isradisaikul e Myers, 2015](https://www.cs.cornell.edu/andru/papers/cupex). Boa ideia para o Caça-Conflitos.
- Em compiladores, um estudo com alunos achou o ANTLR melhor que Lex/Yacc em quase todas as medidas ([PLOS ONE 2022](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8893623/)).
- Não achei jogo de compiladores publicado nos anais da SBC.

### Usar IA como revisor "aluno" e para estudar

- Modelo fingindo ser aluno sai coerente demais e superestima o que um aluno entende ([Borchers et al., 2026](https://arxiv.org/abs/2602.01015v1)); abandona o erro simulado assim que é corrigido ([Do e Sonkar, 2026](https://arxiv.org/html/2605.12748v1)). O que ajuda é dizer o que o aluno sabe e não sabe ([Yuan et al., 2026](https://arxiv.org/abs/2601.05473v1)). Por isso o roteiro do aluno manda relatar o que o texto entrega, citar o trecho e fazer a "prova de bolso". Mesmo assim, não substitui aluno de verdade: o botão "Não entendi" do jogo é o sensor melhor.
- Nota de rubrica dada por modelo e opinião de professor podem divergir ([Wang et al., 2025](https://arxiv.org/pdf/2508.16659v1)). Use os relatórios como lista de suspeitas a conferir.
- Para você estudar com IA: peça dicas e não a resposta, peça para ser testado, e explique o conceito com suas palavras para a IA corrigir ([Mollick, "Assigning AI"](https://arxiv.org/abs/2306.10052)). Não achei estudo independente sobre os "modos de estudo" do ChatGPT, do Claude e do Gemini.
- Para escrever prompt de tarefa: diga o objetivo, o que ler, o que não fazer, o formato da saída e quando a tarefa termina ([Anthropic, sistema multiagente](https://www.anthropic.com/engineering/multi-agent-research-system)). Vários agentes gastam de 3 a 15 vezes mais que um só, segundo fontes secundárias.
