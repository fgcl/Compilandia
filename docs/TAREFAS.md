# Tarefas

Marque com `[x]` ao terminar. A origem de cada item está em [REVISAO-AULAS.md](REVISAO-AULAS.md) (código E1, E2... ou o bloco) e nos relatórios em `docs/revisao/relatorios/`. A ordem dentro de cada seção é a de prioridade.

## 1. Erros que ensinam errado

- [x] E1. Recuperação de erros: as quatro entradas que sincronizavam no `;` contra a receita
- [x] E2. Glossário: handle
- [x] E3. Itens LR(0), aula de tabelas, cena 7: `E' → E •` aceita, não reduz
- [x] E4. Token com um sentido só (aula, ficha, Bug, clipe, glossário)
- [x] E5. Tokens, cena 1: apresentar "parser"
- [x] Ambiguidade, cena 10: não depende de as contas darem resultados diferentes
- [x] Bugs: folhas podem ser ε; "duas derivações da mesma frase"
- [x] Glossário: recursão à esquerda
- [x] Fatoração: uma regra só para o "maior prefixo" na ficha e no FORMAL
- [x] Fatoração: o WORKED e a dica de prova usam a mesma regra ("comum a todas do grupo") e citam a ordem do livro
- [x] Ambientes de execução: "a pilha cresce para cima" só no desenho
- [x] E7. SLR, cena 4: tabela inteira, tirada do motor, com GOTO e as regras numeradas
- [x] E8. Escopo, cena 4: `x = 2;` agora dentro do `main`, com a tabela vazia do `main` na pilha
- [x] E9. Alocação, cenas 14 e 15: vivacidade de baixo para cima
- [x] E10. TAC, texto WORKED: `t1 = a < b` e `ifFalse t1 goto L1`, como na prática
- [x] E11. MIPS, cena 5: o resultado vai para a variável `a` (e `total`), sem `t1` ao lado de `$t1`
- [x] E12. Série em C 5: o `calc.y` da tela é o que o motor roda, com os `%left`
- [x] E13. Guia do TP, cap. 8: `move` na cola; `li`, `la`, `move` e rótulo na lista de pseudo-instruções
- [x] AST, texto STORY: "compilador em Java" está certo (o Guia do TP é em Java); a confusão é a falta de explicar as duas linguagens, item da seção 5
- [x] "Só a semântica consulta a tabela de símbolos": agora diz que a semântica procura o y na tabela
- [x] AFD: uma seta por símbolo em cada estado; a que não aparece vai para o estado morto
- [x] Fases: a análise vai até o código intermediário, e a síntese parte dele (como no livro)

## 2. Padrões do jogo inteiro

- [x] Embaralhar as alternativas na tela (`OrdemDasOpcoes`)
- [x] O colega da parte Erros acerta em parte das aulas (13 de 29)
- [x] Variar a fala de acerto: 240 de 259 começavam com "Isso"; agora nenhuma, e o `FalaDeAcertoTest` barra a volta
- [x] Menos perguntas de duas opções: eram 106, agora 50 (`PerguntasDeDuasOpcoesTest` de teto). Sim/não, aceita/rejeita, juntar/separar e as 29 do "certo ou errado" do fecho ganharam a terceira opção; as que sobram pedem tocar num de dois itens do palco
- [x] Primeira interação na cena 2 em FIRST, FOLLOW, tabela LL(1), descida, itens LR(0) e SLR
- [x] Primeira interação antes da cena 4 em ERROR_RECOVERY, ATTRIBUTES_TYPES, SCOPE_SYMBOLS, CODEGEN_MIPS, LALR, AST_BUILD e OPTIMIZATION (hoje na 5 ou 6)
- [x] Nenhum trecho com mais de 4 cenas só ouvindo (`TrechosSoOuvindoTest`)
- [x] Um nome por conceito (lista em REVISAO-AULAS.md, padrão 8) e o glossário com os sinônimos
- [x] Um nome por conceito, o que sobrou: X da aula de FIRST/FOLLOW × E' da recursão (pergunta na aula de FIRST)
- [x] Um nome por conceito: cadeia (o que o autômato lê), frase (o que a gramática gera), palavra só no sentido de todo dia
- [x] Tirar das regiões 3 e 4 os termos das regiões 5 e 6 (LL(1), pilha, `$`, FOLLOW, LR), ou avisar "você vai ver no Templo"
- [x] Ficha e Bugs cobram só o que a aula animada ensina, ou marcam "vem da aula de texto" (`FichaDoTexto`)
- [ ] Dar falas à professora e ao Bug nas aulas animadas
- [x] Série em C 5: os fios `yyparse()` e `NUM 42` saem do alto das caixas, e o rótulo fica no vão

## 3. O aluno produz a resposta

- [x] Etapa "Questão de prova" em LL1_TABLE (exemplo resolvido → para completar → sozinho, com conflito no terceiro)
- [x] "Questão de prova" em SLR_TABLE: `E → E + n | n`, depois `S → ( S ) | id`, depois `S → L = R | R`
- [x] "Questão de prova" em NFA_TO_DFA e DFA_MINIMIZATION (tabela Dtran e escadinha de pares)
- [x] "Fazer com ajuda" para LR0_ITEMS, AMBIGUITY, LEFT_RECURSION e LEFT_FACTORING
- [x] Exercício integrado "torne LL(1)": tirar a recursão, depois fatorar
- [x] Um "diga por quê" no fim de cada "Sua vez" (autoexplicação com menu de razões)
- [x] Revisão espaçada exige acerto antes de dar o assunto por revisado

## 4. O segundo exemplo traz o caso difícil

- [x] AFN→AFD: `(ab|a)c`, com fecho que muda o conjunto e um move vazio
- [x] Minimização: o AFD do livro (3 rodadas)
- [x] Minimização: `a*b*` com estado morto
- [x] FOLLOW: receita sem α e β, com "se some, olhe o próximo" e "fim da regra"
- [x] FOLLOW: caso difícil `S → B C d` (C some, o d entra), conferido pelo motor
- [x] FIRST: caso `S → B d`, `B → C | b`, `C → c | ε` (anulável sem ε escrito; a primeira rodada deixa S só com o d)
- [x] Tabela LL(1): produção anulável não vazia (`S → A B`) e conflito FIRST × FIRST
- [x] Recursão à esquerda: `E → E + T | E − T | T`
- [x] Recursão à esquerda: E/T/F numa prática
- [x] Fatoração: o caso do resto vazio (`S → if S | if S else S | x`)
- [x] Gramáticas não LL(1) no sorteio do "Fazer com ajuda"

## 5. Por região

**Porto e Floresta**
- [x] Regex: uma cena por operador, `|` e parênteses antes de `(a|b)*abb`, classes `[a-z]`
- [x] Thompson com um exemplo desenhado, ou tirar da ficha
- [x] Minimização: cena 8 × cena 18; justificar o segundo X
- [x] Ponte "AFD aceita a cadeia inteira" × "o scanner fica com o último final"

**Vale e Ponte**
- [x] Associatividade: a árvore de `9 - 5 - 2` nas duas gramáticas, e a pergunta "o menos passou a ser da direita?"
- [x] Montar E/T/F na aula de ambiguidade
- [x] Derivação mais à direita praticada em 3 cenas
- [x] ε como produção, "forma sentencial" e "sentença" ditas na aula
- [x] Abrir a Ponte apresentando o parser descendente
- [x] Recursão indireta até o fim
- [x] Pré-requisitos: LEFT_RECURSION ← AMBIGUITY; LL1_TABLE ← LEFT_RECURSION, LEFT_FACTORING

**Templo Top-Down**
- [x] Pegadinha do FOLLOW com `S → A B c`
- [x] As três condições LL(1) na ficha; FIRST e FOLLOW visíveis na aula da tabela
- [x] Descida recursiva: pré-requisito, `casa()` e o `main` com o teste do `$`

**Fortaleza e Oficina**
- [x] Itens LR(0): autômato completo de `S → ( S ) | id`, `S' → S` como passo 0
- [x] SLR: a tabela nasce das caixas de itens
- [x] LALR: origem do lookahead (ponteiros `S → L = R`, conflito do SLR some no LR(1))
- [x] LALR: fusão desenhada com estados completos; kernel × core
- [x] Pegadinha do handle: "beco sem saída" no lugar de "ordem da conta"
- [x] Bison: `%right` com `2^3^2`; "reduz com qualquer token" é a redução padrão
- [x] Bison: precedência da regra × do token, `%prec` no menos unário

**Cidade e Forja**
- [x] Ambientes de execução depois de Geração de código MIPS
- [x] `$ra`, `$sp`, `jal`, `4($sp)` um por cena antes do `fat.asm`
- [x] "PC" sem definição no FORMAL de Ambientes e no cartão do `jal`
- [x] Cena de transição na abertura da região 8
- [x] Segunda aula animada para Otimização, MIPS e Alocação, ou ficha marcando o que é do texto (a ficha marca)

**Seu compilador**
- [x] Por que há duas linguagens, com tabela de equivalência de nomes
- [x] "Na sua linguagem" e "Teste desta etapa" no fim de cada capítulo
- [x] Algo rodando no capítulo 1; cronograma de 3 semanas
- [x] Capítulo 4: FIRST/FOLLOW da Byteiro inteira

## 6. Revisão e documentação

- [x] Juntar as três pesquisas em [PESQUISA.md](PESQUISA.md)
- [x] Ler os 5 relatórios que faltavam e juntar os achados em REVISAO-AULAS.md
- [x] Prompt de trabalho para o Opus 5.5 em [PROMPT-TRABALHO.md](PROMPT-TRABALHO.md)
- [x] Revisor professor do bloco 5 (Semântica e Forja), e os erros técnicos dele consertados
- [x] Revisor professor do bloco 6 (Seu compilador), e os erros técnicos dele consertados
- [x] Gerar as transcrições de novo depois das correções (`TranscricoesTest`)
- [ ] Teste com aluno de verdade: olhar o que o botão "Não entendi" guardou

## 7. O que os professores dos blocos 5 e 6 pediram e ainda falta

Origem: `docs/revisao/relatorios/professor_5_semantica_forja.md` e `professor_6_seu_compilador.md`. A ordem dentro de cada grupo é a de prioridade dos relatórios.

**Cidade e Forja (bloco 5)**
- [ ] Questão de prova na Forja: TAC → quádruplas → triplas, e vivacidade (use/def/out/in) → arestas → cores, com as respostas do `TacGen` e do `RegAlloc`
- [ ] Booleanos com curto-circuito: `if (a < b && c < d)` em TAC e em MIPS (`sgt` + `beq $zero`), e o `bge` da pegadinha de MIPS no mesmo esquema
- [ ] MIPS: levar o if e o while da aula até o MIPS, com um esquema de desvio só na trilha
- [x] Atributos: uma cena com a regra escrita (`E.val = E1.val + T.val`) ao lado da árvore, e a questão "é S-atribuída ou L-atribuída?"
- [x] Atributos: dica da cena 11 "Inteiro com real: qual dos dois guarda a parte quebrada?" no lugar de "O tipo maior ganha"
- [x] Região 8 na ordem AST, Escopo, Atributos
- [x] Escopo: perguntar sobre redeclaração no mesmo bloco
- [x] Ambientes: na fala de acerto do colega, "o n chega em `$a0`, mas a próxima chamada escreve por cima"
- [ ] Alocação: "Fazer com ajuda" ou prática com a tabela de vivacidade
- [ ] Otimização: blocos básicos, líderes e código morto numa aula animada (hoje só nos textos)
- [ ] Lacunas: acesso a vetor (`t = i * 4`, `la`/`sll`), verificação de tipos em comandos (atribuição, condição do if, chamada, return), tabela de símbolos com funções, vivacidade com laço, ação no meio da regra e herdado como parâmetro na descida, DAG de bloco básico

**Seu compilador (bloco 6)**
- [ ] Guia do TP: um "Estenda a Byteiro" por capítulo, com a resposta escondida (`==`, `repita … ate`, o método `repita()`, os tipos de `^`, o molde MIPS de `para`)
- [ ] Guia do TP: capítulo opcional de funções e vetores (`jal`/`$ra`, quadro na pilha, `.space`, base + 4·i)
- [x] Guia do TP: o TAC de cada molde ao lado do MIPS no cap. 8
- [ ] Guia do TP: recuperação de erro implementada (modo pânico no FOLLOW, com um teste); hoje o cap. 10 é só checklist
- [x] Guia do TP: `compila()` dos caps. 5 a 7 devolver `li $v0, 10` / `syscall`, e não `""`
- [x] Guia do TP: cap. 7 dizer que o esquema de uma passada é L-atribuído; cap. 2 com um PREVEJA sobre `nao x > 0`; cap. 6 com "linha 6" antes de "linhas 5 e 7"; o aviso "variável em laço não reinicia" no `declaraUm` do cap. 8
- [x] Guia do TP: bordas do MARS (`\` e acento no `.asciiz`, identificador acentuado virando rótulo)
- [x] Em C 2 com `( expr )`, para a descida chamar a si mesma, e a ponte com `X -> + T X | ε` do Templo
- [x] Em C 3 com o código que cria os nós (`e = novo('+', e, termo())`)
- [ ] Série Em C: segundo exemplo e erro comum em cada aula; na Em C 4, pedir o MIPS de `a * b + c`; a pergunta da Em C 2, cena 7, com três opções
- [x] Em C 5: uma fala de fecho ligando o `gera` da aula 4 às ações `{ }` do Bison
