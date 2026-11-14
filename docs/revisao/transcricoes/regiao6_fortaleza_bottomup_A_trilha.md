# Região 6: Fortaleza Bottom-Up — Montar de baixo para cima: LR
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO SHIFT_REDUCE: Empilha-reduz (shift-reduce)
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Empilha e reduz» → Fazer com ajuda → Programar o Byte → Prática: você pilota «Opere a esteira» → Pegadinha «Reduzir cedo demais» → Desafio
Jogos que treinam: Esteira Shift-Reduce (empilhar ou reduzir contra o relógio), Oficina Bison (programar o Bison até os testes passarem)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: Guindaste empilha-reduz

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug travou a esteira
Na tela: pilha: E + T   entrada: fim
BYTE: "Antes da aula: a entrada acabou. O que a máquina faz?"
Opções: ✔reduz | empilha | para
Depois: "Reduz. Não há mais nada para empilhar."

#### AULA ANIMADA BÁSICA: "Empilha e reduz" (24 cenas, ~179s) [1º exemplo começa na cena 4]

1. TELA: # E -> E + n ⏎ *E* ⏎ E + n ⏎ E + n + n ⏎ E + n + n + n ⏎ ...
   BYTE: "A gramática E vai para E mais n tem recursão à esquerda. O preditivo troca E por E mais n, e o E volta ao topo."

2. TELA: # E -> E + n ⏎ *E* ⏎ E + n ⏎ E + n + n ⏎ E + n + n + n ⏎ ... ⏎ 1 ⏎ 2 ⏎ sem fim
   BYTE: "Quantas trocas ele faz até ler o primeiro n? Toque."
   PERGUNTA (toque): certo = sem fim ; errado = 1 | 2
   se errar: "Depois de cada troca, o E continua na frente."
   se acertar: "Ele troca para sempre e nunca lê nada. O preditivo decide a regra cedo demais."

3. TELA: !!EMPILHA E REDUZ!!
   BYTE: "A saída é ler primeiro e escolher a regra depois, quando o lado direito inteiro já está na pilha. É o empilha-reduz."

4. TELA: # E -> E + n | n ⏎ n ⏎ + ⏎ n ⏎ $ ⏎ pilha
   BYTE: "Esse analisador é ascendente: monta a árvore das folhas para a raiz. A entrada chega pela esteira, e a pilha guarda o que já foi lido."

5. TELA: # E -> E + n | n ⏎ pilha ⏎ + ⏎ n ⏎ $ ⏎ n
   BYTE: "Empilhar, ou shift, é passar o próximo token da entrada para o topo da pilha."

6. TELA: # E -> E + n | n ⏎ pilha ⏎ + ⏎ n ⏎ $ ⏎ *n* ⏎ empilha ⏎ reduz ⏎ aceita
   BYTE: "No topo está n, e existe a regra E vai para n. O que o parser faz agora? Toque."
   PERGUNTA (toque): certo = reduz ; errado = empilha | aceita
   se errar: "O topo já é o lado direito inteiro de uma regra."
   se acertar: "Reduz. O n no topo é um handle: sai o n e entra o E."

7. TELA: # E -> E + n | n ⏎ pilha ⏎ + ⏎ n ⏎ $ ⏎ *E*
   BYTE: "No topo está n, o lado direito da regra E vai para n. Esse trecho pronto para reduzir é o handle. Reduzo: n sai, E entra."

8. TELA: # E -> E + n | n ⏎ pilha ⏎ $ ⏎ E ⏎ + ⏎ n
   BYTE: "Agora não há handle no topo. Empilho o mais e depois o n."

9. TELA: # E -> E + n | n ⏎ pilha ⏎ $ ⏎ *E* ⏎ *+* ⏎ *n* ⏎ Empilhar ⏎ Reduzir
   BYTE: "A pilha tem E mais n. Empilho ou reduzo? Toque na resposta."
   PERGUNTA (toque): certo = Reduzir ; errado = Empilhar
   se errar: "Olhe o topo. E mais n é o lado direito de uma regra?"
   se acertar: "Exato. E mais n é o handle. Reduzir, ou reduce, troca os três por E."

10. TELA: # E -> E + n | n ⏎ pilha ⏎ $ ⏎ *E*
   BYTE: "Sobrou só o símbolo inicial na pilha, e a entrada acabou. A frase foi aceita."

11. TELA: # E -> E + n | n ⏎ pilha ⏎ $ ⏎ *E*
   PROF: "Lidas de trás para a frente, as reduções formam uma derivação mais à direita. É esse o R do nome LR."

12. TELA: # Receita ⏎ 1. Sem handle no topo: empilha um token. ⏎ 2. Handle no topo: reduz pela regra dele. ⏎ 3. Só o símbolo inicial e $: aceita.
   BYTE: "A cada passo, o parser escolhe entre três ações, olhando o topo da pilha."

13. TELA: # Receita ⏎ 1. Sem handle no topo: empilha um token. ⏎ 2. Handle no topo: reduz pela regra dele. ⏎ 3. Só o símbolo inicial e $: aceita. ⏎ E   |   $
   BYTE: "Pilha só com E, entrada no cifrão. Qual ação vale? Toque."
   PERGUNTA (toque): certo = 3. Só o símbolo inicial e $: aceita. ; errado = 1. Sem handle no topo: empilha um token. | 2. Handle no topo: reduz pela regra dele.
   se errar: "Olhe o que sobrou na pilha e na entrada."
   se acertar: "Aceita, e a frase está certa."

14. TELA: # Receita ⏎ 1. Sem handle no topo: empilha um token. ⏎ *2. Handle no topo: reduz pela regra dele.* ⏎ 3. Só o símbolo inicial e $: aceita.
   BYTE: "Reduzir tira da pilha o lado direito inteiro e põe no lugar o não-terminal do lado esquerdo."

15. TELA: # S -> ( S ) | id ⏎ ( ⏎ id ⏎ ) ⏎ $ ⏎ pilha
   BYTE: "Agora parênteses em volta de id. Você decide cada passo."

16. TELA: # S -> ( S ) | id ⏎ pilha ⏎ ) ⏎ $ ⏎ ( ⏎ *id*
   BYTE: "Nada para reduzir ainda. Empilho o abre parêntese e depois o id."

17. TELA: # S -> ( S ) | id ⏎ pilha ⏎ ) ⏎ $ ⏎ ( ⏎ *id* ⏎ Empilhar ⏎ Reduzir
   BYTE: "Na pilha, abre parêntese e id. Empilho ou reduzo? Toque."
   PERGUNTA (toque): certo = Reduzir ; errado = Empilhar
   se errar: "O id sozinho é o lado direito de uma regra?"
   se acertar: "O id é um handle e vira S."

18. TELA: # S -> ( S ) | id ⏎ pilha ⏎ ) ⏎ $ ⏎ ( ⏎ *S*
   BYTE: "Reduzo: o id vira S. No topo ficam abre parêntese e S."

19. TELA: # S -> ( S ) | id ⏎ pilha ⏎ ) ⏎ $ ⏎ *(* ⏎ *S* ⏎ Empilhar ⏎ Reduzir
   BYTE: "E agora, com abre parêntese e S? Empilho ou reduzo? Toque."
   PERGUNTA (toque): certo = Empilhar ; errado = Reduzir
   se errar: "Alguma regra termina em S sem o fecha parêntese?"
   se acertar: "Boa. Falta o fecha parêntese, então empilha."

20. TELA: # S -> ( S ) | id ⏎ pilha ⏎ ( ⏎ S ⏎ $ ⏎ )
   BYTE: "Toque nas peças que a redução troca por S."
   PERGUNTA (toque): certo = ( | S | ) ; errado = $
   se errar: "O lado direito inteiro sai junto."
   se acertar: "As três saem, e entra um S."

21. TELA: # S -> ( S ) | id ⏎ pilha ⏎ *$* ⏎ *S*
   BYTE: "Sobrou só o símbolo inicial, com o cifrão na entrada: aceita. Saber onde está o handle é o trabalho da tabela LR."

22. TELA: # Erros comuns ⏎ Todo lado direito de regra no topo da pilha é um handle. ⏎ certo ⏎ errado
   BYTE: "Sempre que o topo casa com um lado direito, eu reduzo? Certo ou errado? Toque."
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Pense em id mais id vezes id. Com E mais T no topo e vezes na entrada, reduzir estraga a conta."
   se acertar: "Só é handle se a redução leva de volta ao símbolo inicial. Às vezes o certo é empilhar mais, e quem decide é a tabela."

23. TELA: # Erros comuns ⏎ Certo: Handle é o lado direito cuja redução faz parte da derivação mais à direita. ⏎ *Errado: Procurar um handle no meio da pilha.* ⏎ *Certo: O handle aparece sempre no topo. O parser nunca mexe no meio.*
   BYTE: "Outro erro: procurar handle no meio da pilha. No empilha-reduz, o handle sempre se forma no topo."

24. TELA: # Resumo ⏎ Empilhar (shift): o token vai para o topo. Reduzir (reduce): o handle vira o lado esquerdo. ⏎ Aceita com só o símbolo inicial na pilha e $ na entrada. ⏎ As reduções, de trás para a frente, dão a derivação mais à direita.
   BYTE: "Resumindo: o empilha-reduz lê primeiro e decide depois. Ele empilha até formar um handle no topo, reduz, e repete até sobrar o símbolo inicial."

#### PEGADINHA: "Reduzir cedo demais" (6 cenas, ~44s)

1. TELA: # E + T   vem * ⏎ E ⏎ + ⏎ T ⏎ E -> E + T | T ⏎ T -> T * F | F ⏎ F -> id ⏎ *
   BYTE: "Na pilha temos E mais T, e vem um vezes. O topo já forma uma regra."

2. TELA: # E + T   vem * ⏎ E ⏎ + ⏎ T ⏎ E -> E + T | T ⏎ T -> T * F | F ⏎ F -> id ⏎ * ⏎ * ⏎ reduzir
   BYTE: "A máquina reduz E mais T, ou empilha o vezes? Toque no desenho."
   PERGUNTA (toque): certo = * ; errado = reduzir
   se errar: "Se reduzir agora, fica E na pilha, e vem um vezes. Alguma regra continua E com vezes?"
   se acertar: "Empilha. Esse T ainda vai virar T vezes F."

3. TELA: # E + T   vem * ⏎ *E -> E + T | T* ⏎ T -> T * F | F ⏎ F -> id ⏎ *** ⏎ E ⏎ nenhuma regra com E *
   BYTE: "Veja o que aconteceria. Reduzindo, sobra E na pilha com o vezes na entrada. Nenhuma regra tem E seguido de vezes."

4. TELA: # E + T   vem * ⏎ *E -> E + T | T* ⏎ T -> T * F | F ⏎ F -> id ⏎ E ⏎ + ⏎ T ⏎ *
   BYTE: "Quem decide é o que vem na entrada. Ver um lado direito no topo não basta."

5. TELA: !!HANDLE!!
   PROF: "Handle é o pedaço certo de reduzir agora. Nem todo lado direito é handle."

6. TELA: !!HANDLE!!
   BYTE: "Reduzir antes da hora leva a um beco sem saída. O parser rejeita uma frase que está certa."

#### PRATICA: "Opere a esteira" (5 cenas, ~28s)

1. TELA: # n + n ⏎ E -> E + n | n
   BYTE: "Agora o sentido é o contrário: da frase até o símbolo inicial."

2. TELA: # n + n ⏎ *E -> E + n | n*
   BYTE: "Empilhar puxa o próximo token. Reduzir troca o topo pelo lado esquerdo da regra."

3. TELA: # n + n ⏎ *E -> E + n | n*
   PROF: "Quando sobrar só E, e a entrada acabar, a frase vale."

4. TELA: pilha ⏎ *n* ⏎ + ⏎ n ⏎ $
   BYTE: "Sua vez na esteira. Empilhe e reduza até sobrar E."
   BRINQUEDO: dica "Olhe o topo. Ele já forma uma regra inteira?" / acerto "Sobrou E e a entrada acabou. Aceito."

5. TELA: # n + n ⏎ *E -> E + n | n*
   BYTE: "Foi uma derivação mais à direita, de trás para frente."

#### CLIPE 30s: "O que é handle" (2 cenas, ~9s)

1. TELA: # Handle ⏎ $ ⏎ *E* ⏎ *+* ⏎ *T*
   BYTE: "Handle é o pedaço do topo da pilha que é a hora certa de reduzir."

2. TELA: # Handle ⏎ $ ⏎ *E*
   BYTE: "Aqui, E mais T vira E."

#### CLIPE 30s: "Empilhar ou reduzir" (2 cenas, ~10s)

1. TELA: # Empilhar ⏎ E ⏎ + ⏎ id
   BYTE: "Empilhar puxa o próximo token para a pilha."

2. TELA: # Reduzir ⏎ E ⏎ + ⏎ T
   BYTE: "Reduzir troca o topo pelo lado esquerdo de uma regra."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Reduz Cedo — mentira: "Viu lado direito na pilha? Reduza na hora." / verdade: "Depende de quem vem. Com E + T na pilha e * na entrada, empilha."
    · [V] Reduzir troca o topo pelo lado esquerdo da regra. → Isso mesmo.
    · [V] Shift-reduce faz a derivação mais à direita ao contrário. → Isso mesmo.
    · [F] Handle é qualquer pedaço igual a um lado direito. → Handle é o pedaço certo de reduzir agora.

#### FICHA DO ASSUNTO
Em uma frase: Análise empilha-reduz (bottom-up): empilha tokens até o handle aparecer no topo e então reduz para o lado esquerdo; as reduções formam a derivação mais à direita ao contrário.
Gancho: Empilha até ver a alça; achou a alça no topo, reduz. LR = rightmost de trás pra frente.
Termos-chave: empilha, reduz, handle, topo, mais à direita, conflito
Erros comuns: Reduzir qualquer ocorrência de um lado direito (nem toda ocorrência é handle). // Achar que reduzir consome token da entrada. // Procurar o handle no meio da pilha (ele está sempre no topo). // Confundir: reduções do LR formam a derivação mais à DIREITA invertida, não a mais à esquerda.
Flashcards:
    · P: As 4 ações? → R: Empilhar, reduzir, aceitar, erro.
    · P: O que é handle? → R: Lado direito cuja redução é um passo atrás numa derivação mais à direita.
    · P: Onde o handle aparece? → R: Sempre no topo da pilha.
    · P: Configuração de aceitação? → R: Pilha $ S, entrada $.
    · P: Reduzir por A → β faz o quê? → R: Desempilha β e empilha A.
    · P: Conflito shift/reduce? → R: Não dá para decidir entre empilhar e reduzir.
    · P: Conflito reduce/reduce? → R: Não dá para decidir qual produção reduzir.
    · P: O que significa o R de LR? → R: Rightmost: derivação mais à direita (ao contrário).

#### GLOSSÁRIO (balão ao tocar no termo)
- handle: O lado direito no topo que é certo reduzir agora. Nem todo lado direito no topo é handle.
- shift: Empilhar: o parser pega o próximo token e põe na pilha.
- reduce: Reduzir: o parser troca o topo da pilha pelo lado esquerdo de uma regra.


## ASSUNTO LR0_ITEMS: Itens LR(0), closure e goto
Pré-requisitos no jogo: Empilha-reduz (shift-reduce)
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «O ponto que anda» → Pegadinha «O fecho que puxa regras» → Desafio
Jogos que treinam: Esteira Shift-Reduce (empilhar ou reduzir contra o relógio), Construtor de Estados (fechar (closure) e transitar (goto))
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug moveu o ponto
Na tela: E -> E . + T
BYTE: "Antes da aula: o que a máquina espera ler agora?"
Opções: ✔+ | E | T
Depois: "O mais. O ponto fica logo antes dele."

#### AULA ANIMADA BÁSICA: "O ponto que anda" (25 cenas, ~205s)

1. TELA: # LR(0) ⏎ E ⏎ + ⏎ E -> E + n
   BYTE: "O parser empilhou E e mais. Para saber se empilha ou reduz, ele precisa lembrar em que ponto de cada regra está."

2. TELA: # LR(0) ⏎ E ⏎ + ⏎ *E -> E + n* ⏎ n ⏎ E ⏎ +
   BYTE: "A pilha já tem E e mais. O que falta chegar para a regra ficar completa? Toque."
   PERGUNTA (toque): certo = n ; errado = E | +
   se errar: "Compare a pilha com o lado direito da regra, da esquerda para a direita."
   se acertar: "Falta o n. O parser está no meio da regra, logo depois do mais."

3. TELA: # LR(0) ⏎ E ⏎ + ⏎ *E -> E + n*
   BYTE: "Sem isso, ele não sabe se ainda espera um n ou se o handle já está completo."

4. TELA: !!ITEM!!
   BYTE: "Um item LR(0) é uma regra com um ponto no lado direito. O ponto marca até onde a regra já foi lida."

5. TELA: # Item LR(0) ⏎ *E -> E • + n*
   BYTE: "O que está antes do ponto já está na pilha. O que está depois é o que o parser ainda espera ler."

6. TELA: # Item LR(0) ⏎ E -> • E + n ⏎ *E -> • n*
   BYTE: "Se o ponto está antes de um não-terminal, como E, o parser pode estar começando qualquer regra de E. Juntar essas regras é o fechamento, o closure."

7. TELA: # Item LR(0) ⏎ E -> • E + n ⏎ E -> • n
   BYTE: "Neste estado chega um n. Qual item sobrevive? Toque nele."
   PERGUNTA (toque): certo = E -> • n ; errado = E -> • E + n
   se errar: "O ponto só anda sobre o símbolo que vem logo depois dele."
   se acertar: "O ponto passa por cima do n. O outro item esperava E, não n, e fica para trás."

8. TELA: # Item LR(0) ⏎ E -> n • ⏎ estado novo
   BYTE: "Andar o ponto por cima de um símbolo é o goto. O resultado, depois do fechamento, é um novo estado."

9. TELA: # Item LR(0) ⏎ *E -> n •* ⏎ estado novo
   BYTE: "Ponto no fim é um item completo: o lado direito inteiro está na pilha, e a regra pode ser reduzida."

10. TELA: # Item LR(0) ⏎ E -> n • ⏎ estado novo
   PROF: "Cada conjunto de itens é um estado. Os estados e os gotos formam o autômato LR(0)."

11. TELA: # Receita ⏎ 1. Closure: ponto antes de A puxa as regras de A. ⏎ 2. Goto: ande o ponto por X e feche de novo. ⏎ 3. Ponto no fim: item completo, pode reduzir.
   BYTE: "A receita começa com uma regra extra, S linha vai para S, e usa duas operações."

12. TELA: # Receita ⏎ 1. Closure: ponto antes de A puxa as regras de A. ⏎ 2. Goto: ande o ponto por X e feche de novo. ⏎ 3. Ponto no fim: item completo, pode reduzir.
   BYTE: "Ponto antes de E. Qual operação junta as regras de E? Toque."
   PERGUNTA (toque): certo = 1. Closure: ponto antes de A puxa as regras de A. ; errado = 2. Goto: ande o ponto por X e feche de novo. | 3. Ponto no fim: item completo, pode reduzir.
   se errar: "É a que olha o símbolo logo depois do ponto."
   se acertar: "Acertou. Closure, o fechamento."

13. TELA: # Receita ⏎ 1. Closure: ponto antes de A puxa as regras de A. ⏎ *2. Goto: ande o ponto por X e feche de novo.* ⏎ 3. Ponto no fim: item completo, pode reduzir.
   BYTE: "Depois do goto, sempre faça o closure de novo. Repita para cada símbolo depois de um ponto, até não surgir estado novo."

14. TELA: # Estado zero ⏎ S' -> • S
   BYTE: "Outra gramática: parênteses em volta de id. O estado zero começa com S linha vai para ponto S."

15. TELA: # Estado zero ⏎ S' -> • S ⏎ S -> • ( S ) ⏎ S -> • id ⏎ S -> ( • S ) ⏎ S -> id •
   BYTE: "No estado zero, o ponto está antes de S. Quais itens o closure junta? Toque nos dois."
   PERGUNTA (toque): certo = S -> • ( S ) | S -> • id ; errado = S -> ( • S ) | S -> id •
   se errar: "O ponto fica no começo das regras de S."
   se acertar: "Os dois itens com o ponto no começo."

16. TELA: # Estado zero ⏎ S' -> • S ⏎ S -> • ( S ) ⏎ S -> • id ⏎ S -> id • ⏎ S -> • id ⏎ S -> ( S • )
   BYTE: "Do estado zero, lendo id, para onde o ponto vai? Toque no item."
   PERGUNTA (toque): certo = S -> id • ; errado = S -> • id | S -> ( S • )
   se errar: "O ponto pula o id."
   se acertar: "Ponto no fim: esse estado reduz."

17. TELA: # Estado zero ⏎ S' -> • S ⏎ S -> • ( S ) ⏎ S -> • id ⏎ S -> ( • S ) ⏎ sim ⏎ não
   BYTE: "Lendo abre parêntese, o ponto fica antes de S. Precisa de closure?"
   PERGUNTA (toque): certo = sim ; errado = não
   se errar: "Ponto antes de um não-terminal sempre puxa as regras dele."
   se acertar: "É isso. Entram de novo as regras de S."

18. TELA: # Estado zero ⏎ S' -> • S ⏎ S -> • ( S ) ⏎ S -> • id ⏎ *S -> ( • S )* ⏎ *S -> • ( S )* ⏎ *S -> • id*
   BYTE: "Os estados estão prontos. Onde há item completo, a tabela vai reduzir. Onde o ponto está antes de um terminal, ela vai empilhar."

19. TELA: # O autômato inteiro ⏎ I0 ⏎ I1 ⏎ *I2* ⏎ I3 ⏎ I4 ⏎ I5 ⏎ S -> ( • S ) ⏎ S -> • ( S ) ⏎ S -> • id
   BYTE: "Repetindo o goto e o closure até não surgir estado novo, saem seis estados. Embaixo estão os itens do estado do abre parêntese."

20. TELA: # O autômato inteiro ⏎ I0 ⏎ I1 ⏎ I2 ⏎ I3 ⏎ I4 ⏎ I5 ⏎ S -> ( • S ) ⏎ S -> • ( S ) ⏎ S -> • id
   BYTE: "Nesse estado, lendo abre parêntese de novo, para onde o goto leva? Toque no estado."
   PERGUNTA (toque): certo = I2 ; errado = I0 | I1 | I3 | I4 | I5
   se errar: "Ande o ponto pelo abre parêntese e feche. Compare o resultado com os estados que já existem."
   se acertar: "Para ele mesmo. O goto dá o mesmo conjunto de itens, e conjunto igual é o mesmo estado. É por isso que a construção termina."

21. TELA: # O autômato inteiro ⏎ I0 ⏎ I1 ⏎ I2 ⏎ I3 ⏎ I4 ⏎ I5 ⏎ S' -> S •
   BYTE: "Qual estado aceita a entrada? Toque."
   PERGUNTA (toque): certo = I1 ; errado = I0 | I2 | I3 | I4 | I5
   se errar: "Procure o item da regra extra com o ponto no fim."
   se acertar: "O estado de S linha vai para S ponto. Com o cifrão na entrada, ele aceita."

22. TELA: # O autômato inteiro ⏎ I0 ⏎ *I1* ⏎ I2 ⏎ *I3* ⏎ I4 ⏎ *I5* ⏎ S' -> S •
   BYTE: "Seis estados. Dois reduzem: o do id e o do parêntese fechado. Um aceita, e nos outros o ponto está antes de um símbolo."

23. TELA: # Erros comuns ⏎ No closure, entram as regras de todo símbolo depois do ponto, terminal ou não. ⏎ certo ⏎ errado
   BYTE: "Quando o closure puxa regras novas? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Terminal não tem regras. Só não-terminal tem."
   se acertar: "O closure só age quando o ponto está logo antes de um não-terminal. E só o símbolo logo depois do ponto conta."

24. TELA: # Erros comuns ⏎ Certo: Closure: só para o não-terminal que vem logo depois do ponto. ⏎ *Errado: Andar o ponto no goto e esquecer o closure.* ⏎ *Certo: Todo goto termina com closure. Sem ele, faltam itens no estado.*
   BYTE: "Outro erro: andar o ponto e parar. Se o ponto novo ficou antes de um não-terminal, as regras dele entram no estado."

25. TELA: # Resumo ⏎ Item: regra com um ponto. Antes dele já foi lido; depois dele ainda se espera. ⏎ Closure puxa regras; goto anda o ponto e fecha. Cada conjunto é um estado. ⏎ Ponto no fim é item completo: sinal de redução.
   BYTE: "Resumindo: o item marca o progresso numa regra. Closure e goto montam os estados, e o item completo avisa a redução."

#### PEGADINHA: "O fecho que puxa regras" (5 cenas, ~29s)

1. TELA: # Fecho ⏎ E -> . E + n
   BYTE: "O ponto marca até onde a regra foi lida. Aqui, nada ainda."

2. TELA: # Fecho ⏎ *E -> . E + n*
   BYTE: "O ponto está antes de E. Então posso estar prestes a ler um E inteiro."

3. TELA: # Fecho ⏎ E -> . E + n ⏎ E -> . E + n ⏎ E -> . n ⏎ E -> n .
   BYTE: "Quem entra no fecho? Toque nos itens certos."
   PERGUNTA (toque): certo = E -> . E + n | E -> . n ; errado = E -> n .
   se errar: "Entram as regras de E, todas com o ponto no começo."
   se acertar: "O fecho puxa as regras, com o ponto no começo."

4. TELA: # Fecho ⏎ E -> . E + n ⏎ E -> . n ⏎ *E -> n .*
   PROF: "Item com o ponto no fim é outra coisa: é o momento de reduzir."

5. TELA: !!CLOSURE!!
   BYTE: "Ponto antes de não terminal, puxa. Ponto no fim, reduz."

#### DIAGRAMAS DE TABELA: "A caixa do estado" (9 cenas, ~60s)

1. TELA: # Estado I0 ⏎ E → E + T | T ⏎ T → id ⏎ I0{E' → • E}
   BYTE: "No parser LR, cada estado é uma caixa de itens. Vamos montar a primeira."

2. TELA: # Estado I0 ⏎ E → E + T | T ⏎ T → id ⏎ *I0{E' → • E}*
   BYTE: "Começa com um item só. O ponto antes do E quer dizer: falta ler um E."

3. TELA: # Estado I0 ⏎ *E → E + T | T* ⏎ T → id ⏎ I0{E' → • E; E → • E + T; E → • T}
   BYTE: "Se falta um E, entram as regras do E, com o ponto no começo."

4. TELA: # Estado I0 ⏎ E → E + T | T ⏎ *T → id* ⏎ I0{E' → • E; E → • E + T; E → • T; T → • id}
   BYTE: "Um desses itens tem o ponto antes do T. Então entram as regras do T."

5. TELA: # Estado I0 ⏎ E → E + T | T ⏎ T → id ⏎ *I0{E' → • E; E → • E + T; E → • T; T → • id}*
   BYTE: "Não entra mais nada. Essa caixa completa se chama fecho, ou closure."

6. TELA: # Estado I0 ⏎ I0{E' → • E; E → • E + T; E → • T; T → • id} ⏎ *I1{E' → E •; E → E • + T}* ⏎ *I2{E → T •}* ⏎ *I3{T → id •}*
   BYTE: "Agora o goto. Pulando cada símbolo, nasce outra caixa. O fio sai da linha dele."

7. TELA: # Estado I0 ⏎ I0{E' → • E; E → • E + T; E → • T; T → • id} ⏎ I1{E' → E •; E → E • + T} ⏎ I2{E → T •} ⏎ I3{T → id •}
   BYTE: "Item com o ponto no fim manda reduzir. Só o E linha vai para E, ponto, não reduz: ele aceita a entrada. Toque nas caixas que reduzem."
   PERGUNTA (toque): certo = I2{E → T •} | I3{T → id •} ; errado = I0{E' → • E; E → • E + T; E → • T; T → • id} | I1{E' → E •; E → E • + T}
   se errar: "Procure o ponto na última posição. O item do E linha aceita, não reduz."
   se acertar: "Com o ponto no fim, a regra inteira já está na pilha."

8. TELA: # Estado I0 ⏎ I0{E' → • E; E → • E + T; E → • T; T → • id} ⏎ I1{E' → E •; E → E • + T} ⏎ *I2{E → T •}* ⏎ *I3{T → id •}*
   PROF: "O bison desenha os estados assim, com a opção graph."

9. TELA: fecho do I0 ⏎ I0{E' → • E}
   BYTE: "Agora monte o fecho de outra gramática. Toque nos itens que entram."
   BRINQUEDO: dica "Só entra item com o ponto no começo, de quem aparece logo depois de um ponto." / acerto "Fecho completo. É exatamente o I0 que o motor calcula."

#### CLIPE 30s: "O ponto do item" (2 cenas, ~9s)

1. TELA: # Item ⏎ E -> E . + T
   BYTE: "Um item é uma regra com um ponto."

2. TELA: # Item ⏎ E -> E + . T
   BYTE: "O ponto mostra até onde a regra já foi lida."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Fecho Curto — mentira: "Closure só copia o item." / verdade: "Se o ponto está antes de um não terminal, as regras dele entram."
    · [V] O ponto marca até onde a regra foi lida. → Isso mesmo.
    · [V] Item com o ponto no fim pede redução. → Isso mesmo.
    · [F] Todo estado LR(0) tem um item só. → O fecho costuma puxar vários.

#### FICHA DO ASSUNTO
Em uma frase: Item LR(0) é uma produção com um ponto marcando o progresso; o fechamento acrescenta B → •γ quando o ponto está antes de B, e o desvio avança o ponto sobre um símbolo e fecha.
Gancho: Ponto = 'você está aqui'. Closure = convida a família. Goto = chuta o ponto um passo e comemora com a família.
Termos-chave: ponto, item, closure, goto, estado, aumentada
Erros comuns: Esquecer de aplicar o fechamento depois do goto. // No fechamento, adicionar itens com o ponto fora do começo. // Fechar sobre terminais (só não-terminais após o ponto convidam produções). // Criar um estado novo para um conjunto de itens que já existe. // Esquecer a gramática aumentada S' → S.
Flashcards:
    · P: Quantos itens gera A → X Y Z? → R: 4
    · P: Itens de A → ε? → R: Só A → •
    · P: CLOSURE de um item A → α • B β adiciona? → R: B → • γ para toda produção de B (e repete).
    · P: GOTO(I, X)? → R: Avança o ponto sobre X nos itens de I e fecha.
    · P: Para que S' → S? → R: Ter um único item de aceitação, S' → S •.
    · P: Item completo (ponto no fim) indica? → R: Redução.
    · P: Quantos estados LR(0) tem S → C C, C → c C | d? → R: 7 (I0 a I6).
    · P: Item de base (kernel)? → R: S' → • S ou item com o ponto fora do início.

#### GLOSSÁRIO (balão ao tocar no termo)
- item: Uma regra com um ponto que marca quanto dela já foi lido.
- closure: Fecho: junta ao estado as regras do símbolo que está logo depois do ponto.
- goto: Para onde o autômato LR vai depois de ler um símbolo.


## ASSUNTO SLR_TABLE: Tabela SLR(1) e conflitos
Pré-requisitos no jogo: Itens LR(0), closure e goto, Conjunto FOLLOW
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Quando reduzir?» → Fazer com ajuda → Prática: você pilota «Rode a tabela SLR» → Pegadinha «Conflito não é ambiguidade» → Desafio
Jogos que treinam: Esteira Shift-Reduce (empilhar ou reduzir contra o relógio), Caça-Conflitos (achar e explicar conflitos)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug cruzou duas ações
Na tela: empilhar / reduzir
BYTE: "Antes da aula: uma casa com as duas ações é o quê?"
Opções: ✔conflito | erro léxico | normal
Depois: "Conflito. A tabela deu duas ordens de uma vez."

#### AULA ANIMADA BÁSICA: "Quando reduzir?" (25 cenas, ~201s)

1. TELA: # SLR(1) ⏎ E -> n • ⏎ ?
   BYTE: "Este estado tem o item completo E vai para n, ponto. O LR(0) reduziria sempre. Mas reduzir agora está certo?"

2. TELA: # SLR(1) ⏎ E -> n • ⏎ ? ⏎ *E -> E + n | n* ⏎ + ⏎ $ ⏎ n
   BYTE: "Nesta gramática, que tokens podem vir logo depois de E? Toque nos dois."
   PERGUNTA (toque): certo = + | $ ; errado = n
   se errar: "Na regra E vai para E mais n, olhe o que vem depois do primeiro E. Lembre também que E é o símbolo inicial."
   se acertar: "Mais e cifrão. O mais vem da regra, e o cifrão porque E é o símbolo inicial."

3. TELA: # SLR(1) ⏎ E -> n • ⏎ *?* ⏎ E -> E + n | n
   BYTE: "Se o próximo token não pode vir depois de E, a redução leva a um beco sem saída."

4. TELA: !!SLR!!
   BYTE: "O SLR, de Simple LR, só reduz por A quando o próximo token está no FOLLOW de A."

5. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n
   BYTE: "O autômato vira uma tabela. Cada linha é um estado, e cada coluna é o próximo símbolo."

6. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ *s2* ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ *r2* ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n
   BYTE: "Na tabela, s com um número manda empilhar e ir para esse estado. r com um número manda reduzir por essa regra."

7. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n
   BYTE: "Toque em todas as casas que mandam empilhar."
   PERGUNTA (toque): certo = s2 | s3 | s4 ; errado = ok | r2 | r2 | r1 | r1
   se errar: "Empilhar é a letra s com o número do estado."
   se acertar: "Essas três. Cada s vem de uma seta do autômato com um terminal."

8. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ *FOLLOW(E) = { +, $ }*
   BYTE: "Empilhar sai direto dos gotos do autômato. Reduzir é a parte delicada: o SLR só reduz se o próximo token está no FOLLOW."

9. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ FOLLOW(E) = { +, $ }
   BYTE: "O estado 2 terminou a regra de E. Em qual coluna não posso reduzir? Toque nela."
   PERGUNTA (toque): certo =  ; errado = r2 | r2
   se errar: "Procure o símbolo que não está no FOLLOW de E."
   se acertar: "O n não está no FOLLOW de E. A casa fica vazia, e casa vazia é erro de sintaxe."

10. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ shift ou reduce?
   BYTE: "Se a mesma casa pede empilhar e reduzir, há um conflito shift-reduce. Com duas reduções, um conflito reduce-reduce."

11. TELA: # Tabela SLR(1) ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ shift ou reduce?
   PROF: "Gramática que enche a tabela sem nenhum conflito é SLR(1)."

12. TELA: # Receita ⏎ 1. Goto por terminal t: s na coluna t. ⏎ 2. A → α •: r nas colunas do FOLLOW(A). ⏎ 3. S' → S •: ok (aceita) na coluna do $.
   BYTE: "A receita da tabela SLR tem três regras."

13. TELA: # Receita ⏎ 1. Goto por terminal t: s na coluna t. ⏎ 2. A → α •: r nas colunas do FOLLOW(A). ⏎ 3. S' → S •: ok (aceita) na coluna do $.
   BYTE: "O estado dois tem E vai para n, ponto. Qual regra preenche a linha dele? Toque."
   PERGUNTA (toque): certo = 2. A → α •: r nas colunas do FOLLOW(A). ; errado = 1. Goto por terminal t: s na coluna t. | 3. S' → S •: ok (aceita) na coluna do $.
   se errar: "O ponto está no fim da regra."
   se acertar: "Certo. Regra dois: reduz nas colunas do FOLLOW."

14. TELA: # Receita ⏎ 1. Goto por terminal t: s na coluna t. ⏎ 2. A → α •: r nas colunas do FOLLOW(A). ⏎ *3. S' → S •: ok (aceita) na coluna do $.*
   BYTE: "Regra três: o item S linha vai para S, ponto, aceita a entrada na coluna do cifrão. E goto por não-terminal vira só o número do estado."

15. TELA: # SLR(1) ⏎ S -> id • ⏎ FOLLOW(S) = { ), $ } ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ k
   BYTE: "Agora parênteses em volta de id. O estado tem S vai para id, ponto."

16. TELA: # SLR(1) ⏎ S -> id • ⏎ *FOLLOW(S) = { ), $ }* ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ k
   BYTE: "O FOLLOW de S tem fecha parêntese e cifrão. Toque nas casas do reduz."
   PERGUNTA (toque): certo =  |  ; errado =  | 
   se errar: "Regra dois: só nas colunas do FOLLOW."
   se acertar: "Nas outras colunas, a casa fica vazia."

17. TELA: # SLR(1) ⏎ S -> id • ⏎ FOLLOW(S) = { ), $ } ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ k ⏎ r ⏎ r ⏎ reduz ⏎ empilha ⏎ erro
   BYTE: "Depois de id, o próximo token é abre parêntese. Toque no que a tabela faz."
   PERGUNTA (toque): certo = erro ; errado = reduz | empilha
   se errar: "A casa do abre parêntese ficou vazia."
   se acertar: "Casa vazia é erro de sintaxe."

18. TELA: # SLR(1) ⏎ S -> id • ⏎ FOLLOW(S) = { ), $ } ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ k ⏎ *r* ⏎ *r*
   BYTE: "Com o FOLLOW, a tabela reduz só onde a regra pode terminar. Na etapa de prática, você roda uma entrada com essa tabela."

19. TELA: # A tabela inteira ⏎ 1: S -> ( S )    2: S -> id ⏎ depois de:  ( I2   id I3   ) I5 ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ S ⏎ 0 ⏎ 1 ⏎ 2 ⏎ 3 ⏎ 4 ⏎ 5
   BYTE: "Agora a tabela inteira de parênteses em volta de id. Ela sai dos seis estados da aula de itens, e cada estado vira uma linha."

20. TELA: # A tabela inteira ⏎ 1: S -> ( S )    2: S -> id ⏎ depois de:  ( I2   id I3   ) I5 ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ S ⏎ 0 ⏎ 1 ⏎ *2* ⏎ 3 ⏎ 4 ⏎ 5
   BYTE: "O estado do abre parêntese, lendo id, vai para o estado do id. Em que casa entra esse empilhar? Toque."
   PERGUNTA (toque): certo =  ; errado =  |  |  | 
   se errar: "Linha do estado do abre parêntese, coluna do id."
   se acertar: "Nessa casa entra s com o número do estado do id. Regra um: goto por terminal vira empilhar."

21. TELA: # A tabela inteira ⏎ 1: S -> ( S )    2: S -> id ⏎ depois de:  ( I2   id I3   ) I5 ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ S ⏎ 0 ⏎ 1 ⏎ 2 ⏎ s3 ⏎ 3 ⏎ 4 ⏎ *5*
   BYTE: "O estado do fecha parêntese tem o item completo da regra um. Toque nas casas do reduzir."
   PERGUNTA (toque): certo =  |  ; errado =  |  | 
   se errar: "Regra dois: as colunas do FOLLOW de S, que tem fecha parêntese e cifrão."
   se acertar: "Fecha parêntese e cifrão, com r1. Nas outras colunas, a casa fica vazia."

22. TELA: # A tabela inteira ⏎ 1: S -> ( S )    2: S -> id ⏎ depois de:  ( I2   id I3   ) I5 ⏎ id ⏎ ( ⏎ ) ⏎ $ ⏎ S ⏎ 0 ⏎ s3 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ ok ⏎ 2 ⏎ *s3* ⏎ s2 ⏎ 4 ⏎ 3 ⏎ r2 ⏎ r2 ⏎ 4 ⏎ s5 ⏎ 5 ⏎ *r1* ⏎ *r1*
   BYTE: "Pronto: a tabela inteira saiu das caixas de itens. Nenhuma casa tem duas ações, então a gramática é SLR."

23. TELA: # Erros comuns ⏎ Estado com A → α •: escrevo r em todas as colunas. ⏎ certo ⏎ errado
   BYTE: "Onde entra a redução na tabela SLR? Um colega preencheu assim. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Isso é o que o LR(0) faz. O SLR olha um conjunto antes."
   se acertar: "Exato. No SLR, a redução entra só nas colunas do FOLLOW de A. Encher a linha inteira cria conflitos que não existem."

24. TELA: # Erros comuns ⏎ Certo: No SLR, r entra só nas colunas do FOLLOW(A). ⏎ *Errado: Escrever s (shift) na coluna de um não-terminal.* ⏎ *Certo: Coluna de não-terminal é a parte GOTO: só o número do estado.*
   BYTE: "Outro erro: pôr shift em coluna de não-terminal. Ninguém lê não-terminal da entrada. Ali vai só o estado de destino, o goto."

25. TELA: # Resumo ⏎ Terminal depois do ponto: s (empilha) e vai para o estado do goto. ⏎ Item completo A → α •: r nas colunas do FOLLOW(A). ⏎ Duas ações na mesma casa: conflito. Sem conflito, a gramática é SLR(1).
   BYTE: "Resumindo: a tabela SLR sai do autômato LR(0) e do FOLLOW. Os gotos viram shift, os itens completos viram reduce no FOLLOW, e casa vazia é erro."

#### PEGADINHA: "Conflito não é ambiguidade" (5 cenas, ~36s)

1. TELA: # Conflito sem ambiguidade ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L
   BYTE: "Esta gramática de ponteiros não é ambígua. Mesmo assim, o SLR tropeça."

2. TELA: # Conflito sem ambiguidade ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ S -> L . = R ⏎ R -> L .
   BYTE: "Depois de ler L, esse estado tem duas opções. Seguir com o igual, ou reduzir para R."

3. TELA: # Conflito sem ambiguidade ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ S -> L . = R ⏎ R -> L . ⏎ shift reduce ⏎ reduce reduce
   BYTE: "O igual está no FOLLOW de R. Com igual na entrada, que conflito aparece? Toque."
   PERGUNTA (toque): certo = shift reduce ; errado = reduce reduce
   se errar: "Uma ideia empilha o igual. A outra reduz."
   se acertar: "Empilhar ou reduzir: conflito shift reduce."

4. TELA: # Conflito sem ambiguidade ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ S -> L . = R ⏎ R -> L .
   PROF: "O SLR olha só o FOLLOW, que é um conjunto grosso. O LALR olha mais fino e resolve."

5. TELA: !!CONFLITO!!
   BYTE: "Conflito diz que a técnica não deu conta. Não prova que a gramática é ambígua."

#### PRATICA: "Rode a tabela SLR" (9 cenas, ~80s)

1. TELA: # n + n ⏎ *n* ⏎ *+* ⏎ *$* ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n
   BYTE: "Esta é a tabela SLR completa da gramática E vai para E mais n, ou n. As colunas n, mais e cifrão são a parte ACTION. A coluna E é a parte GOTO."

2. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ *pilha de estados: 0* ⏎ entrada: n + n $
   BYTE: "O parser guarda uma pilha de estados, que começa com o estado zero. A ação sai da casa do estado do topo com o próximo token."

3. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ *s2* ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 ⏎ entrada: n + n $ ⏎ empilha, vai ao 2 ⏎ reduz pela regra 2
   BYTE: "Topo zero, token n. A casa diz s2. O que o parser faz? Toque."
   PERGUNTA (toque): certo = empilha, vai ao 2 ; errado = reduz pela regra 2
   se errar: "A letra s vem de shift, que é empilhar."
   se acertar: "O n sai da entrada, e o estado dois entra na pilha."

4. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ *r2* ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 2 ⏎ entrada: + n $
   BYTE: "Agora o topo é dois e o token é mais. A casa diz r2: reduzir pela regra dois, E vai para n. O lado direito tem um símbolo, então sai um estado da pilha."

5. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ *pilha de estados: 0* ⏎ entrada: + n $
   BYTE: "Ficou o zero no topo, e a redução produziu um E. Qual casa diz o próximo estado? Toque nela."
   PERGUNTA (toque): certo = 1 ; errado = s2 |  | 
   se errar: "Linha do estado que ficou no topo, coluna do não-terminal reduzido."
   se acertar: "Exato. GOTO de zero com E é um, e o estado um entra na pilha. Toda redução termina com um goto."

6. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ *s3* ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ *s4* ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 1 3 4 ⏎ entrada: $
   BYTE: "Topo um, token mais: s3. Topo três, token n: s4. A pilha agora é zero, um, três, quatro, e sobrou o cifrão na entrada."

7. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ *r1* ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 1 3 4 ⏎ entrada: $ ⏎ 1 ⏎ 2 ⏎ 3
   BYTE: "Topo quatro, token cifrão: r1, a regra E vai para E mais n. Quantos estados saem da pilha? Toque."
   PERGUNTA (toque): certo = 3 ; errado = 1 | 2
   se errar: "Conte os símbolos do lado direito da regra um."
   se acertar: "Três símbolos no lado direito, três estados fora. Sobra o zero, e GOTO de zero com E devolve o um."

8. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ *ok* ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 1 ⏎ entrada: $
   BYTE: "Topo um, token cifrão: a casa diz ok. A entrada foi aceita."

9. TELA: # n + n ⏎ n ⏎ + ⏎ $ ⏎ E ⏎ 0 ⏎ s2 ⏎ 1 ⏎ 1 ⏎ s3 ⏎ ok ⏎ 2 ⏎ r2 ⏎ r2 ⏎ 3 ⏎ s4 ⏎ 4 ⏎ r1 ⏎ r1 ⏎ 1: E -> E + n    2: E -> n ⏎ pilha de estados: 0 1 ⏎ entrada: $
   PROF: "Shift empilha um estado. Reduce tira um estado por símbolo da regra e consulta o GOTO."

#### CLIPE 30s: "Conflito shift-reduce" (2 cenas, ~11s)

1. TELA: # Duas ordens ⏎ empilhar ⏎ reduzir
   BYTE: "No mesmo estado, com o mesmo token, a tabela manda empilhar e reduzir."

2. TELA: # Duas ordens ⏎ *empilhar* ⏎ reduzir
   BYTE: "O Bison escolhe empilhar, e avisa."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do FOLLOW Cego — mentira: "O SLR reduz em qualquer token." / verdade: "Só nos tokens do FOLLOW do lado esquerdo."
    · [V] LR(0) reduz sem olhar a entrada. → Isso mesmo.
    · [V] Conflito shift-reduce é dúvida entre empilhar e reduzir. → Isso mesmo.
    · [F] Se o SLR tem conflito, a gramática é ambígua. → S -> L = R mostra o contrário.

#### FICHA DO ASSUNTO
Em uma frase: A tabela SLR(1) usa os estados LR(0): terminal após o ponto vira shift, não-terminal vira GOTO e item completo A → α • vira reduce só nas colunas de FOLLOW(A).
Gancho: SLR = 'Só Libera Redução' se o próximo token está na prancheta do porteiro FOLLOW.
Termos-chave: follow, shift, reduce, goto, conflito, estado, lr(0)
Erros comuns: Colocar o reduce em todas as colunas (isso é LR(0), não SLR) // Usar FIRST em vez de FOLLOW para decidir as reduções // Escrever shift na tabela GOTO ou número puro na ACTION // Achar que conflito na tabela SLR prova que a gramática é ambígua // Esquecer o accept em [S' → S •] na coluna $
Flashcards:
    · P: O que vai em ACTION[i, a] se Ii tem [A → α • a β]? → R: shift j, onde j = GOTO(Ii, a)
    · P: Onde vai o reduce de [A → α •] no SLR? → R: Nas colunas de FOLLOW(A)
    · P: Quando a gramática é SLR(1)? → R: Quando nenhuma célula de ACTION tem duas ações
    · P: Conflito empilhar/reduzir no SLR ocorre quando... → R: [A → α • a β] e [B → γ •] no mesmo estado com a ∈ FOLLOW(B)
    · P: Conflito reduzir/reduzir no SLR ocorre quando... → R: Dois itens completos com FOLLOWs que se intersectam
    · P: Onde fica o accept? → R: ACTION[i, $] do estado com S' → S •
    · P: S → L = R | R; L → * R | id; R → L é SLR(1)? → R: Não: conflito no estado 2 com '=' (s6 e r5)
    · P: Toda gramática ambígua dá conflito SLR? → R: Sim; mas nem todo conflito indica ambiguidade

#### GLOSSÁRIO (balão ao tocar no termo)
- conflito: Uma casa da tabela com duas ações. O parser não sabe o que fazer.


## ASSUNTO LALR: LR(1) e LALR(1)
Pré-requisitos no jogo: Tabela SLR(1) e conflitos
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Mais precisão, mesma tabela» → Pegadinha «Juntar estados tem preço» → Desafio
Jogos que treinam: Construtor de Estados (fechar (closure) e transitar (goto)), Caça-Conflitos (achar e explicar conflitos)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug juntou estados
Na tela: LR(1)   LALR   SLR
BYTE: "Antes da aula: qual deles costuma ter mais estados?"
Opções: ✔LR(1) | LALR | SLR
Depois: "O LR(1). O LALR nasce juntando estados dele."

#### AULA ANIMADA BÁSICA: "Mais precisão, mesma tabela" (27 cenas, ~221s)

1. TELA: # LALR ⏎ E -> n • ⏎ FOLLOW(E) = { +, ), $ } ⏎ r ?
   BYTE: "Há gramáticas sem ambiguidade em que a tabela SLR tem conflito. O FOLLOW manda reduzir onde não devia."

2. TELA: # LALR ⏎ *E -> n •* ⏎ FOLLOW(E) = { +, ), $ } ⏎ r ? ⏎ + ⏎ ) ⏎ $ ⏎ n
   BYTE: "O SLR põe a redução de E vai para n em quais tokens? Toque em todos."
   PERGUNTA (toque): certo = + | ) | $ ; errado = n
   se errar: "O SLR olha o FOLLOW de E, que está na tela."
   se acertar: "Mais, fecha parêntese e fim de entrada. O SLR reduz em todo token do FOLLOW de E, e o n não está lá."

3. TELA: # LALR ⏎ E -> n • ⏎ *FOLLOW(E) = { +, ), $ }* ⏎ r ?
   BYTE: "O problema: o FOLLOW de A é um só, para a gramática toda. Ele não sabe em que estado o parser está."

4. TELA: !!LR(1)!!
   BYTE: "O item LR(1) guarda, junto com a regra e o ponto, o token que pode vir depois naquele contexto: o lookahead."

5. TELA: # LR(1) ⏎ E -> n • , + ⏎ E -> n • , $ ⏎ T -> id • , + ⏎ T -> id • , $
   BYTE: "Aqui estão quatro itens LR(1). Depois da vírgula vem o lookahead. A redução só vale se o próximo token for ele."

6. TELA: # LR(1) ⏎ *E -> n • , +* ⏎ E -> n • , $ ⏎ T -> id • , + ⏎ T -> id • , $
   BYTE: "O núcleo é o item sem o lookahead: a regra e o ponto. Toque no item com o mesmo núcleo do destacado."
   PERGUNTA (toque): certo = E -> n • , $ ; errado = T -> id • , + | T -> id • , $
   se errar: "Compare a regra e o ponto. Ignore o que vem depois da vírgula."
   se acertar: "A regra e o ponto são iguais, só muda o token."

7. TELA: # LR(1) ⏎ *E -> n • , + $*
   BYTE: "O LR(1) completo tem estados demais. O LALR junta os estados de núcleo igual e une os lookaheads."

8. TELA: # LR(1) ⏎ E -> n • , + $ ⏎ *Bison usa LALR(1)*
   BYTE: "Fica quase tão forte quanto o LR(1), com o mesmo número de estados da SLR. Por isso o Bison usa LALR(1)."

9. TELA: # LR(1) ⏎ E -> n • , + $ ⏎ Bison usa LALR(1)
   PROF: "Juntar estados pode criar um conflito reduce-reduce novo, mas nunca um shift-reduce novo."

10. TELA: # Receita ⏎ 1. Monte os estados LR(1), com lookahead. ⏎ 2. Junte núcleos iguais e una os lookaheads. ⏎ Resultado: tamanho da SLR, menos conflitos.
   BYTE: "A receita do LALR tem dois passos."

11. TELA: # Receita ⏎ 1. Monte os estados LR(1), com lookahead. ⏎ 2. Junte núcleos iguais e una os lookaheads. ⏎ Resultado: tamanho da SLR, menos conflitos.
   BYTE: "Qual passo deixa a tabela pequena de novo? Toque."
   PERGUNTA (toque): certo = 2. Junte núcleos iguais e una os lookaheads. ; errado = 1. Monte os estados LR(1), com lookahead. | Resultado: tamanho da SLR, menos conflitos.
   se errar: "É o passo que junta estados."
   se acertar: "Boa. Juntar núcleos iguais encolhe a tabela."

12. TELA: # Receita ⏎ 1. Monte os estados LR(1), com lookahead. ⏎ 2. Junte núcleos iguais e una os lookaheads. ⏎ *Resultado: tamanho da SLR, menos conflitos.*
   BYTE: "Como cada redução olha só os lookaheads do próprio estado, o LALR reduz menos por engano que o SLR."

13. TELA: # LALR ⏎ T -> id • , $ ⏎ T -> id • , ) ⏎ T -> ( • E ) , $
   BYTE: "Dois estados LR(1) de outra gramática. Compare os dois."

14. TELA: # LALR ⏎ T -> id • , $ ⏎ T -> id • , ) ⏎ T -> ( • E ) , $
   BYTE: "Quais dois itens têm o mesmo núcleo? Toque neles."
   PERGUNTA (toque): certo = T -> id • , $ | T -> id • , ) ; errado = T -> ( • E ) , $
   se errar: "Ignore o token depois da vírgula."
   se acertar: "Mesma regra, mesmo ponto."

15. TELA: # LALR ⏎ T -> id • , $ ⏎ T -> id • , ) ⏎ T -> ( • E ) , $ ⏎ T -> id • , ) $ ⏎ T -> id • , $ ⏎ T -> ( • E ) , ) $
   BYTE: "Juntos, quais lookaheads o item ganha? Toque na caixa certa."
   PERGUNTA (toque): certo = T -> id • , ) $ ; errado = T -> id • , $ | T -> ( • E ) , ) $
   se errar: "Junte os tokens dos dois."
   se acertar: "Fecha parêntese e cifrão no mesmo item."

16. TELA: # LALR ⏎ *T -> id • , ) $*
   BYTE: "É assim que o LALR fica do tamanho da SLR e guarda a precisão do lookahead."

17. TELA: # Os ponteiros, de novo ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ S -> L • = R , $ ⏎ R -> L • , $ ⏎ SLR: s ? r ?
   BYTE: "Os ponteiros da pegadinha do SLR. Depois do L, o SLR tem conflito no igual. Empilha, ou reduz R vai para L?"

18. TELA: # Os ponteiros, de novo ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ *S -> • L = R , $* ⏎ L -> • id , ? ⏎ $ ⏎ = ⏎ R
   BYTE: "No LR(1), o fecho deste item cria L vai para ponto id. Que lookahead ele ganha deste item? Toque."
   PERGUNTA (toque): certo = = ; errado = $ | R
   se errar: "O lookahead é o que vem logo depois do L no item que o criou."
   se acertar: "O igual. Depois do L vem o igual, e é ele que o L pode encontrar em seguida."

19. TELA: # Os ponteiros, de novo ⏎ S -> L = R | R ⏎ L -> * R | id ⏎ R -> L ⏎ S -> L • = R , $ ⏎ *R -> L • , $* ⏎ = : s     $ : r
   BYTE: "Do mesmo jeito, R vai para L ponto fica só com o cifrão. No igual, o parser só empilha. O conflito sumiu."

20. TELA: # A fusão, com estados inteiros ⏎ S -> C C ⏎ C -> c C | d ⏎ I3{C -> c • C , c d; C -> • c C , c d; C -> • d , c d} ⏎ I6{C -> c • C , $; C -> • c C , $; C -> • d , $}
   BYTE: "Agora estados inteiros, na gramática do livro. Ler o c no primeiro C leva ao estado três. Ler o c no segundo C leva ao seis."

21. TELA: # A fusão, com estados inteiros ⏎ S -> C C ⏎ C -> c C | d ⏎ I3{C -> c • C , c d; C -> • c C , c d; C -> • d , c d} ⏎ I6{C -> c • C , $; C -> • c C , $; C -> • d , $} ⏎ kernel: itens de base
   BYTE: "A primeira linha de cada caixa veio do goto, ao ler o c. São os itens de base, o kernel. As outras linhas, o fecho acrescentou."

22. TELA: # A fusão, com estados inteiros ⏎ S -> C C ⏎ C -> c C | d ⏎ *I3{C -> c • C , c d; C -> • c C , c d; C -> • d , c d}* ⏎ I6{C -> c • C , $; C -> • c C , $; C -> • d , $} ⏎ I2{S -> C • C , $; C -> • c C , $; C -> • d , $} ⏎ I4{C -> d • , c d}
   BYTE: "O núcleo é o estado inteiro sem os lookaheads. Qual estado tem o mesmo núcleo do três? Toque."
   PERGUNTA (toque): certo = I6{C -> c • C , $; C -> • c C , $; C -> • d , $} ; errado = I2{S -> C • C , $; C -> • c C , $; C -> • d , $} | I4{C -> d • , c d}
   se errar: "Compare os itens de base, sem o lookahead. O resto do estado sai deles pelo fecho."
   se acertar: "O seis. Os itens de base são iguais, então o fecho também é. O dois tem duas linhas iguais às do três, mas outro kernel: o núcleo é outro."

23. TELA: # A fusão, com estados inteiros ⏎ S -> C C ⏎ C -> c C | d ⏎ *I36{C -> c • C , c d $; C -> • c C , c d $; C -> • d , c d $}*
   BYTE: "O LALR junta os dois num estado só. Cada item fica com c, d e cifrão. Para achar os pares, basta comparar os kernels sem o lookahead."

24. TELA: # A fusão, com estados inteiros ⏎ S -> C C ⏎ C -> c C | d ⏎ I36{C -> c • C , c d $; C -> • c C , c d $; C -> • d , c d $} ⏎ *LR(1): 10     LALR: 7*
   BYTE: "O quatro junta com o sete, e o oito com o nove. Os dez estados do LR(1) viram sete, o mesmo número do LR(0)."

25. TELA: # Erros comuns ⏎ Toda gramática LALR(1) é LR(1), mas nem toda LR(1) é LALR(1). ⏎ certo ⏎ errado
   BYTE: "LALR e LR(1) têm a mesma força? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = certo ; errado = errado
   se errar: "Juntar estados de mesmo núcleo pode misturar lookaheads que o LR(1) mantinha separados."
   se acertar: "Acertou. A fusão pode criar um conflito reduce-reduce que o LR(1) não tinha. Por isso o LALR fica entre o SLR e o LR(1)."

26. TELA: # Erros comuns ⏎ Certo: Toda gramática LALR(1) é LR(1), mas nem toda LR(1) é LALR(1). ⏎ *Errado: Usar o FOLLOW para preencher as reduções da tabela LALR.* ⏎ *Certo: No LALR, a redução entra nas colunas dos lookaheads do item.*
   BYTE: "Um erro comum: usar o FOLLOW no LALR. FOLLOW é coisa do SLR. No LALR e no LR(1), quem decide a coluna é o lookahead do item."

27. TELA: # Resumo ⏎ Item LR(1): regra, ponto e lookahead. ⏎ LALR: junta estados LR(1) de núcleo igual e une os lookaheads. ⏎ Tamanho da SLR, força entre SLR e LR(1). É o método do Bison.
   BYTE: "Resumindo: o LR(1) guarda o lookahead em cada item. O LALR junta os estados de mesmo núcleo. Fica pequeno como o SLR e quase tão forte quanto o LR(1)."

#### PEGADINHA: "Juntar estados tem preço" (5 cenas, ~33s)

1. TELA: # Juntar estados ⏎ A -> c .   com d ⏎ B -> c .   com e ⏎ A -> c .   com e ⏎ B -> c .   com d
   BYTE: "O LR(1) tem muitos estados. O LALR junta os que têm o mesmo núcleo."

2. TELA: # Juntar estados ⏎ *A -> c .   com d* ⏎ *B -> c .   com e* ⏎ A -> c .   com e ⏎ B -> c .   com d
   BYTE: "Separados, cada estado sabe o que fazer. Com d reduz um, com e reduz outro."

3. TELA: # Juntar estados ⏎ *A -> c .   com d* ⏎ *B -> c .   com e* ⏎ A -> c .   com e ⏎ B -> c .   com d
   BYTE: "O núcleo é igual, então o LALR junta tudo num estado só."

4. TELA: # Juntar estados ⏎ A -> c .   com d, e ⏎ B -> c .   com d, e ⏎ S -> a . A d
   BYTE: "Depois de juntar, com d na entrada, quem pode reduzir? Toque nas duas regras."
   PERGUNTA (toque): certo = A -> c .   com d, e | B -> c .   com d, e ; errado = S -> a . A d
   se errar: "Junte as listas: as duas regras ficaram com d e com e."
   se acertar: "Duas reduções com o mesmo token: conflito reduce reduce."

5. TELA: # Juntar estados ⏎ A -> c .   com d, e ⏎ B -> c .   com d, e
   PROF: "Essa é a única briga que o LALR pode criar. Shift reduce novo ele não cria."

#### CLIPE 30s: "O que o LALR faz" (2 cenas, ~11s)

1. TELA: # LR(1) ⏎ 1 ⏎ 2 ⏎ 3 ⏎ 4
   BYTE: "O LR(1) sabe muito, mas tem estados demais."

2. TELA: # LALR ⏎ 1 ⏎ 2 ⏎ 3 ⏎ 4
   BYTE: "O LALR junta os estados parecidos e guarda o que pode vir depois."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Estado Repetido — mentira: "LALR tem os estados do LR(1)." / verdade: "LALR funde estados de mesmo núcleo. Fica do tamanho do SLR."
    · [V] Juntar estados pode criar conflito reduce-reduce. → Isso mesmo.
    · [V] Juntar estados não cria shift-reduce novo. → Isso mesmo.
    · [F] SLR e LALR usam os mesmos símbolos de previsão. → O SLR usa FOLLOW. O LALR é mais fino.

#### FICHA DO ASSUNTO
Em uma frase: LR(1) põe um lookahead em cada item e só reduz nele; LALR(1) funde estados LR(1) com o mesmo núcleo, ficando do tamanho do SLR com quase todo o poder do LR(1).
Gancho: Mesmo esqueleto, roupas somadas: a fusão só cria Reduce duplo, nunca Empilhar/Reduzir.
Termos-chave: lookahead, núcleo, fusão, lr(1), reduzir/reduzir, first
Erros comuns: Calcular o lookahead dos novos itens com FOLLOW em vez de FIRST(β a) // Achar que a fusão LALR pode criar conflito empilhar/reduzir novo // Achar que o LALR tem mais estados que o SLR (tem o mesmo número) // Confundir 'Look-Ahead LR' com lookahead de dois tokens
Flashcards:
    · P: O que é um item LR(1)? → R: [A → α • β, a]: item LR(0) mais um terminal de lookahead
    · P: Lookaheads dos itens de B na closure de [A → α • B β, a]? → R: FIRST(β a)
    · P: Quando [A → α •, a] reduz? → R: Só quando o próximo token é a
    · P: O que o LALR funde? → R: Estados LR(1) com o mesmo núcleo
    · P: Que conflito a fusão pode criar? → R: Somente reduzir/reduzir
    · P: Nº de estados LALR(1) comparado ao SLR(1)? → R: Igual (o número de estados LR(0))
    · P: Hierarquia de gramáticas? → R: SLR(1) ⊂ LALR(1) ⊂ LR(1)
    · P: Que método o Bison usa por padrão? → R: LALR(1)
    · P: Exemplo LR(1) mas não LALR(1)? → R: S → a A d | b B d | a B e | b A e; A → c; B → c

#### GLOSSÁRIO (balão ao tocar no termo)
- LALR: Tabela LR(1) com estados iguais juntados. Fica do tamanho da SLR.
