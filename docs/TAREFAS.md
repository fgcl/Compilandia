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
- [ ] Menos perguntas de duas opções (92 de 192)
- [x] Primeira interação na cena 2 em FIRST, FOLLOW, tabela LL(1), descida, itens LR(0) e SLR
- [x] Primeira interação antes da cena 4 em ERROR_RECOVERY, ATTRIBUTES_TYPES, SCOPE_SYMBOLS, CODEGEN_MIPS, LALR, AST_BUILD e OPTIMIZATION (hoje na 5 ou 6)
- [ ] Nenhum trecho com mais de 4 cenas só ouvindo
- [ ] Um nome por conceito (lista em REVISAO-AULAS.md, padrão 8) e o glossário com os sinônimos
- [ ] Tirar das regiões 3 e 4 os termos das regiões 5 e 6 (LL(1), pilha, `$`, FOLLOW, LR), ou avisar "você vai ver no Templo"
- [ ] Ficha e Bugs cobram só o que a aula animada ensina, ou marcam "vem da aula de texto"
- [ ] Dar falas à professora e ao Bug nas aulas animadas
- [x] Série em C 5: os fios `yyparse()` e `NUM 42` saem do alto das caixas, e o rótulo fica no vão

## 3. O aluno produz a resposta

- [ ] Etapa "Questão de prova" em LL1_TABLE (exemplo resolvido → para completar → sozinho, com conflito no terceiro)
- [ ] "Questão de prova" em SLR_TABLE: `E → E + n | n`, depois `S → ( S ) | id`, depois `S → L = R | R`
- [ ] "Questão de prova" em NFA_TO_DFA e DFA_MINIMIZATION (tabela Dtran e escadinha de pares)
- [ ] "Fazer com ajuda" para LR0_ITEMS, AMBIGUITY, LEFT_RECURSION e LEFT_FACTORING
- [ ] Exercício integrado "torne LL(1)": tirar a recursão, depois fatorar
- [ ] Um "diga por quê" depois de cada "Sua vez" (autoexplicação)
- [ ] Revisão espaçada exige acerto antes de dar o assunto por revisado

## 4. O segundo exemplo traz o caso difícil

- [x] AFN→AFD: `(ab|a)c`, com fecho que muda o conjunto e um move vazio
- [x] Minimização: o AFD do livro (3 rodadas)
- [x] Minimização: `a*b*` com estado morto
- [x] FOLLOW: receita sem α e β, com "se some, olhe o próximo" e "fim da regra"
- [x] FOLLOW: caso difícil `S → B C d` (C some, o d entra), conferido pelo motor
- [x] FIRST: caso `S → B d`, `B → C | b`, `C → c | ε` (anulável sem ε escrito; a primeira rodada deixa S só com o d)
- [x] Tabela LL(1): produção anulável não vazia (`S → A B`) e conflito FIRST × FIRST
- [x] Recursão à esquerda: `E → E + T | E − T | T`
- [ ] Recursão à esquerda: E/T/F numa prática
- [x] Fatoração: o caso do resto vazio (`S → if S | if S else S | x`)
- [x] Gramáticas não LL(1) no sorteio do "Fazer com ajuda"

## 5. Por região

**Porto e Floresta**
- [ ] Regex: uma cena por operador, `|` e parênteses antes de `(a|b)*abb`, classes `[a-z]`
- [ ] Thompson com um exemplo desenhado, ou tirar da ficha
- [ ] Minimização: cena 8 × cena 18; justificar o segundo X
- [ ] Ponte "AFD aceita a cadeia inteira" × "o scanner fica com o último final"

**Vale e Ponte**
- [x] Associatividade: a árvore de `9 - 5 - 2` nas duas gramáticas, e a pergunta "o menos passou a ser da direita?"
- [ ] Montar E/T/F na aula de ambiguidade
- [ ] Derivação mais à direita praticada em 3 cenas
- [x] ε como produção, "forma sentencial" e "sentença" ditas na aula
- [ ] Abrir a Ponte apresentando o parser descendente
- [ ] Recursão indireta até o fim
- [x] Pré-requisitos: LEFT_RECURSION ← AMBIGUITY; LL1_TABLE ← LEFT_RECURSION, LEFT_FACTORING

**Templo Top-Down**
- [x] Pegadinha do FOLLOW com `S → A B c`
- [x] As três condições LL(1) na ficha; FIRST e FOLLOW visíveis na aula da tabela
- [x] Descida recursiva: pré-requisito, `casa()` e o `main` com o teste do `$`

**Fortaleza e Oficina**
- [x] Itens LR(0): autômato completo de `S → ( S ) | id`, `S' → S` como passo 0
- [ ] SLR: a tabela nasce das caixas de itens
- [ ] LALR: origem do lookahead; fusão desenhada com estados completos; kernel × core
- [x] Pegadinha do handle: "beco sem saída" no lugar de "ordem da conta"
- [ ] Bison: precedência da regra × do token, `%right` com `2^3^2`, `%prec`; dizer que "reduz com qualquer token" é a redução padrão

**Cidade e Forja**
- [ ] Ambientes de execução depois de Geração de código MIPS
- [ ] `$ra`, `$sp`, `jal`, `4($sp)` um por cena antes do `fat.asm`
- [ ] Cena de transição na abertura da região 8
- [ ] Segunda aula animada para Otimização, MIPS e Alocação, ou ficha marcando o que é do texto

**Seu compilador**
- [ ] Por que há duas linguagens, com tabela de equivalência de nomes
- [ ] "Na sua linguagem" e "Teste desta etapa" no fim de cada capítulo
- [ ] Algo rodando no capítulo 1; cronograma de 3 semanas
- [ ] Capítulo 4: FIRST/FOLLOW da Byteiro inteira

## 6. Revisão e documentação

- [x] Juntar as três pesquisas em [PESQUISA.md](PESQUISA.md)
- [x] Ler os 5 relatórios que faltavam e juntar os achados em REVISAO-AULAS.md
- [x] Prompt de trabalho para o Opus 5.5 em [PROMPT-TRABALHO.md](PROMPT-TRABALHO.md)
- [ ] Revisor professor do bloco 5 (Semântica e Forja)
- [ ] Revisor professor do bloco 6 (Seu compilador)
- [ ] Gerar as transcrições de novo depois das correções
- [ ] Teste com aluno de verdade: olhar o que o botão "Não entendi" guardou
