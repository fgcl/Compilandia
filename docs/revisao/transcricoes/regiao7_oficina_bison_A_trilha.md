# Região 7: Oficina Yacc/Bison — Geradores de analisadores
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO YACC_BISON: Yacc/Bison: especificação e precedência
Pré-requisitos no jogo: Empilha-reduz (shift-reduce), Árvores e ambiguidade
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A fábrica de parsers» → Prática: você pilota «Por que o vezes espera» → Pegadinha «Quem fica embaixo manda» → Desafio
Jogos que treinam: Caça-Conflitos (achar e explicar conflitos), Oficina Bison (programar o Bison até os testes passarem)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug trocou os left
Na tela: %left '*' / %left '+'
BYTE: "Antes da aula: quem liga mais forte aqui?"
Opções: ✔+ | * | empate
Depois: "O mais. Quanto mais embaixo, mais forte."

#### AULA ANIMADA BÁSICA: "A fábrica de parsers" (23 cenas, ~177s) [1º exemplo começa na cena 4]

1. TELA: # LALR(1) ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr | expr '-' expr ⏎     | expr '*' expr | expr '/' expr ⏎     | '(' expr ')' | NUM ; ⏎ 14 estados × 8 tokens
   BYTE: "Uma calculadora com parênteses já vira uma tabela LALR de 14 estados."

2. TELA: # LALR(1) ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr | expr '-' expr ⏎     | expr '*' expr | expr '/' expr ⏎     | '(' expr ')' | NUM ; ⏎ *14 estados × 8 tokens* ⏎ 22 112 1000
   BYTE: "Cada estado tem 8 colunas de token. Quantas casas você confere à mão? Toque."
   PERGUNTA (toque): certo = 112 ; errado = 22 | 1000
   se errar: "Multiplique os estados pelas colunas."
   se acertar: "Isso. E cada regra nova muda muitas delas."

3. TELA: !!BISON!!
   BYTE: "O Bison monta essa tabela sozinho, a partir da gramática. Ele é a versão livre do Yacc e gera um parser LALR(1) em C."

4. TELA: # Arquivo .y ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ;
   BYTE: "Você escreve a gramática num arquivo ponto y. O Bison lê esse arquivo e escreve o código do parser."

5. TELA: # Arquivo .y ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ *expr: expr '+' expr { $$ = $1 + $3; }* ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ;
   BYTE: "Cada regra pode ter uma ação entre chaves, que roda na hora da redução. Cifrão um é o valor do primeiro símbolo. Cifrão três, do terceiro."

6. TELA: # Arquivo .y ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎ *    | expr '-' expr { $$ = $1 - $3; }* ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ;
   BYTE: "Cifrão cifrão é o valor do lado esquerdo. Ele fica na pilha e sobe pela árvore, de redução em redução."

7. TELA: # Arquivo .y ⏎ %token NUM ⏎ *%left '+' '-'* ⏎ *%left '*' '/'* ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ;
   BYTE: "Essa gramática é ambígua, e a tabela teria conflitos. Quem desempata são as linhas de precedência, lá em cima."

8. TELA: # Arquivo .y ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ;
   BYTE: "Quanto mais abaixo no arquivo, maior a precedência. Quem ganha? Toque na linha."
   PERGUNTA (toque): certo = %left '*' '/' ; errado = %left '+' '-'
   se errar: "Procure a linha mais perto das regras."
   se acertar: "Isso. O vezes está mais abaixo, então ele vem antes do mais."

9. TELA: # Arquivo .y ⏎ %token NUM ⏎ *%left '+' '-'* ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ; ⏎ 1 + 2 * 3 = 7 ⏎ 5 - 2 - 1 = 2
   BYTE: "O left diz que o operador associa à esquerda: contas iguais em fila juntam da esquerda para a direita."

10. TELA: # Arquivo .y ⏎ %token NUM ⏎ %left '+' '-' ⏎ %left '*' '/' ⏎ %% ⏎ expr: expr '+' expr { $$ = $1 + $3; } ⏎     | expr '-' expr { $$ = $1 - $3; } ⏎     | expr '*' expr { $$ = $1 * $3; } ⏎     | NUM ; ⏎ 1 + 2 * 3 = 7 ⏎ 5 - 2 - 1 = 2
   BYTE: "Se o Bison não avisa nenhum conflito, toda casa da tabela ficou com uma ação só."

11. TELA: # Receita ⏎ 1. Antes do %%: %token e a precedência. ⏎ 2. Depois do %%: as regras e suas ações. ⏎ 3. Rode bison calc.y e leia os avisos.
   BYTE: "A receita do arquivo .y tem três passos."

12. TELA: # Receita ⏎ 1. Antes do %%: %token e a precedência. ⏎ 2. Depois do %%: as regras e suas ações. ⏎ 3. Rode bison calc.y e leia os avisos. ⏎ %left '-'
   BYTE: "Onde vai a linha do left com o menos? Toque na parte certa."
   PERGUNTA (toque): certo = 1. Antes do %%: %token e a precedência. ; errado = 2. Depois do %%: as regras e suas ações. | 3. Rode bison calc.y e leia os avisos.
   se errar: "Precedência é declaração. Ela não é regra."
   se acertar: "Isso. Precedência fica nas declarações, lá em cima."

13. TELA: # Receita ⏎ 1. Antes do %%: %token e a precedência. ⏎ 2. Depois do %%: as regras e suas ações. ⏎ *3. Rode bison calc.y e leia os avisos.*
   BYTE: "Conflito avisado não trava o Bison. No shift-reduce ele empilha; no reduce-reduce, usa a regra que vem primeiro. E pode errar a sua conta."

14. TELA: # calc.y ⏎ %token NUM ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ;
   BYTE: "Uma calculadora só de menos, sem nenhuma linha de precedência."

15. TELA: # calc.y ⏎ %token NUM ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ; ⏎ 0 1 2
   BYTE: "Rodo o Bison neste arquivo. Quantos conflitos ele avisa? Toque."
   PERGUNTA (toque): certo = 1 ; errado = 0 | 2
   se errar: "Em cinco menos dois menos um, quem junta primeiro?"
   se acertar: "Isso. Um conflito de empilha-reduz, no menos."

16. TELA: # calc.y ⏎ %token NUM ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ; ⏎ $ bison calc.y ⏎ *calc.y: warning: 1 shift/reduce conflict*
   BYTE: "O aviso confirma. Sem precedência, o Bison prefere empilhar."

17. TELA: # calc.y ⏎ %token NUM ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ; ⏎ $ bison calc.y ⏎ *calc.y: warning: 1 shift/reduce conflict* ⏎ 5 - 2 - 1 ⏎ 2 4
   BYTE: "Empilhar primeiro junta pela direita. Quanto dá cinco menos dois menos um? Toque."
   PERGUNTA (toque): certo = 4 ; errado = 2
   se errar: "Faça dois menos um primeiro. Depois, cinco menos o resultado."
   se acertar: "Isso. Dá quatro, mas a conta certa seria dois."

18. TELA: # calc.y ⏎ %token NUM ⏎ *%left '-'* ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ;
   BYTE: "Uma linha resolve: left no menos. Agora o Bison reduz primeiro."

19. TELA: # calc.y ⏎ %token NUM ⏎ *%left '-'* ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ; ⏎ 5 - 2 - 1 ⏎ 2 4
   BYTE: "Com essa linha, quanto dá cinco menos dois menos um? Toque."
   PERGUNTA (toque): certo = 2 ; errado = 4
   se errar: "Agora junta pela esquerda: cinco menos dois, depois menos um."
   se acertar: "Isso. E o Bison não avisa mais nenhum conflito."

20. TELA: # calc.y ⏎ %token NUM ⏎ %left '-' ⏎ %% ⏎ expr: expr '-' expr { $$ = $1 - $3; } ⏎     | NUM ; ⏎ $ bison calc.y ⏎ 5 - 2 - 1 ⏎ *2*
   BYTE: "Saída limpa e conta certa. A calculadora está pronta."

21. TELA: # Erros comuns ⏎ No Bison, o operador declarado primeiro tem a maior precedência. ⏎ certo errado
   BYTE: "A ordem das linhas de precedência importa. Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Na calculadora, o vezes foi declarado depois do mais."
   se acertar: "Isso. É o contrário: quem é declarado por último tem a maior precedência."

22. TELA: # Erros comuns ⏎ Certo: A última linha de %left tem a maior precedência. ⏎ *Errado: Ignorar o aviso de conflito porque o parser foi gerado.* ⏎ *Certo: Conflito sem tratar é o Bison escolhendo por você. Use %left ou reescreva a regra.*
   BYTE: "Outro erro: ignorar o aviso de conflito. O parser sai, mas a escolha padrão do Bison pode montar a árvore errada."

23. TELA: # Resumo ⏎ Arquivo .y: declarações, %%, e as regras com suas ações. ⏎ $$ é o valor do lado esquerdo; $1, $2 e $3 são os do lado direito. ⏎ %left e %right resolvem conflitos. A última linha tem mais precedência.
   BYTE: "Resumindo: o Bison gera a tabela LALR a partir do arquivo ponto y. As ações calculam valores na redução, e as linhas de precedência resolvem os conflitos."

#### TABELAS: "Lendo o bison -v" (9 cenas, ~56s)

1. TELA: # calc.output ⏎ %token NUM ⏎ %left '+' ⏎ %left '*' ⏎ %% ⏎ e: e '+' e { $$ = $1 + $3; } ⏎  | e '*' e { $$ = $1 * $3; } ⏎  | NUM ;
   BYTE: "Com a opção -v, o Bison escreve um relatório: o arquivo output."

2. TELA: # calc.output ⏎ Estado 0{e' → • e; NUM empilha → 2; e vai → 1}
   BYTE: "Cada estado do relatório vira uma caixa. Este é o estado zero."

3. TELA: # calc.output ⏎ Estado 0{e' → • e; »NUM empilha → 2«; e vai → 1}
   BYTE: "Em cima ficam os itens. Embaixo, o que fazer com cada token."

4. TELA: # calc.output ⏎ Estado 0{e' → • e; »NUM empilha → 2«; e vai → 1} ⏎ Estado 2{e → NUM •; $default reduz e → NUM}
   BYTE: "Chegou NUM. A linha manda empilhar e ir para outro estado."

5. TELA: # calc.output ⏎ Estado 0{e' → • e; NUM empilha → 2; e vai → 1} ⏎ *Estado 2{e → NUM •; »$default reduz e → NUM«}*
   BYTE: "Nesse estado o item já terminou. Com qualquer token, ele reduz."

6. TELA: # calc.output ⏎ Estado 0{e' → • e; NUM empilha → 2; »e vai → 1«} ⏎ Estado 1{e' → e •; e → e • '+' e; e → e • '*' e; '+' empilha → 3; '*' empilha → 4; $end aceita} ⏎ *Estado 2{e → NUM •; $default reduz e → NUM}*
   BYTE: "Depois de reduzir, a pilha volta ao estado zero com um e. Toque no destino."
   PERGUNTA (toque): certo = Estado 1 ; errado = Estado 0 | Estado 2
   se errar: "Procure a linha do e dentro do estado zero."
   se acertar: "Isso. A linha do e no estado zero aponta para essa caixa."

7. TELA: # calc.output ⏎ Estado 5{e → e '+' e •; e → e • '+' e; e → e • '*' e; »'*' empilha → 4«; $default reduz e → e '+' e} ⏎ conflito resolvido: vezes empilha
   BYTE: "Agora o estado do conflito. O mais já leu os dois lados e chegou um vezes."

8. TELA: # calc.output ⏎ *Estado 5{e → e '+' e •; e → e • '+' e; e → e • '*' e; »'*' empilha → 4«; $default reduz e → e '+' e}* ⏎ conflito resolvido: vezes empilha
   PROF: "O vezes está mais abaixo no arquivo. Por isso a linha dele empilha."

9. TELA: # calc.output ⏎ *Estado 5{e → e '+' e •; e → e • '+' e; e → e • '*' e; »'*' empilha → 4«; $default reduz e → e '+' e}* ⏎ 0 conflitos · 7 estados
   BYTE: "Sem conflito sobrando, o relatório fica limpo. Agora é rodar."

#### PRATICA: "Por que o vezes espera" (5 cenas, ~30s)

1. TELA: # id + id * id ⏎ E -> E + T | T ⏎ T -> T * F | F ⏎ F -> id
   BYTE: "Sem declarar nada, os níveis da gramática já dão a precedência."

2. TELA: # id + id * id ⏎ E -> E + T | T ⏎ *T -> T * F | F* ⏎ F -> id
   BYTE: "Regra mais funda roda antes. T fica abaixo de E, então o vezes vem primeiro."

3. TELA: # id + id * id ⏎ E -> E + T | T ⏎ *T -> T * F | F* ⏎ F -> id
   PROF: "Com Bison, o left faz o mesmo sem esses níveis."

4. TELA: *id* + id * id $ ⏎ pilha
   BYTE: "Rode a esteira. Repare quando o vezes faz a máquina esperar."
   BRINQUEDO interativo: meta = "Reduza até sobrar E" ; teclas = [Empilhar, Reduzir]
   se errar: "Antes de reduzir, olhe o que vem na entrada."
   ao conseguir: "Aceito. O vezes reduziu antes do mais."

5. TELA: # id + id * id ⏎ E -> E + T | T ⏎ *T -> T * F | F* ⏎ F -> id
   BYTE: "Esperar é empilhar. Foi por isso que a conta saiu na ordem certa."

#### PEGADINHA: "Quem fica embaixo manda" (5 cenas, ~31s)

1. TELA: # 2 + 3 * 4 ⏎ %left '*' ⏎ %left '+'
   BYTE: "Esta especificação tem dois left. A ordem deles decide a conta."

2. TELA: # 2 + 3 * 4 ⏎ %left '*' ⏎ *%left '+'*
   BYTE: "Quanto mais embaixo no arquivo, mais forte. Aqui o mais ficou embaixo."

3. TELA: # 2 + 3 * 4 ⏎ %left '*' ⏎ %left '+' ⏎ 14 20
   BYTE: "Com essa ordem, quanto dá dois mais três vezes quatro? Toque."
   PERGUNTA (toque): certo = 20 ; errado = 14
   se errar: "Quem liga mais forte roda primeiro. Quem ficou embaixo?"
   se acertar: "Isso: vinte. O mais rodou primeiro, e a conta saiu errada."

4. TELA: # 2 + 3 * 4 ⏎ %left '+' ⏎ *%left '*'* ⏎ 14
   BYTE: "Troque as linhas: mais em cima, vezes embaixo. Agora dá catorze."

5. TELA: !!PRECEDÊNCIA!!
   PROF: "A pegadinha é achar que o primeiro manda. Quem manda é o último."

#### CLIPE 30s: "Precedência no Bison" (2 cenas, ~11s)

1. TELA: # %left ⏎ %left '+' ⏎ %left '*'
   BYTE: "O left diz quem liga mais forte e para qual lado agrupa."

2. TELA: # %left ⏎ %left '+' ⏎ *%left '*'*
   BYTE: "Quanto mais embaixo no arquivo, mais forte."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Precedência Trocada — mentira: "O primeiro %left é o mais forte." / verdade: "Quanto mais embaixo no arquivo, mais forte."
    · [V] %left faz 8 - 3 - 2 agrupar pela esquerda. → Isso mesmo.
    · [V] Sem declaração, em shift-reduce o Bison empilha. → Isso mesmo.
    · [F] Em reduce-reduce, o Bison escolhe a última regra. → Ele escolhe a primeira do arquivo.

#### FICHA DO ASSUNTO
Em uma frase: O Bison lê declarações %% regras %% código, gera um analisador LALR(1) em C (yyparse) que pede tokens a yylex, e resolve conflitos com %left/%right/%nonassoc, %prec ou, por padrão, shift.
Gancho: D-R-C: Declara %% Regra %% Código. Na dúvida, empilha; o último da fila de %left é o VIP.
Termos-chave: %token, %left, precedência, associatividade, yylex, yyparse, shift
Erros comuns: Achar que a primeira linha de %left tem a maior precedência (é a menor) // Usar %right para subtração ou divisão // Esquecer o ';' no fim da regra ou o %token de um token com nome // Achar que o Bison falha em conflito: ele avisa e escolhe shift (ou a primeira regra no R/R) // Esquecer o %prec no menos unário
Flashcards:
    · P: Quais as três seções de um .y? → R: Declarações %% regras %% código C
    · P: O que é $$? → R: O valor semântico do lado esquerdo da regra
    · P: E $2 em expr : '(' expr ')'? → R: O valor do expr do meio
    · P: Linha de %left mais embaixo liga... → R: Mais forte (maior precedência)
    · P: Empate de precedência com %left? → R: Reduce (associa à esquerda)
    · P: Empate com %right? → R: Shift (associa à direita)
    · P: Empate com %nonassoc? → R: Erro de sintaxe
    · P: Conflito E/R sem precedência: o Bison faz... → R: Shift, com aviso
    · P: Conflito R/R: o Bison faz... → R: Reduz pela regra que aparece primeiro
    · P: Quem fornece os tokens ao yyparse? → R: yylex (escrito à mão ou gerado pelo Flex)

#### GLOSSÁRIO (balão ao tocar no termo)
- precedência: Quem é feito primeiro numa conta, como o vezes antes do mais.
