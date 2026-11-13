# Região 5: Templo Top-Down — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO FIRST: Conjunto FIRST

#### ESTILO STORY (História / Analogia): "A fila do bandejão: quem pode abrir a fila?"
- NARRADOR: Templo Top-Down, primeira sala. Na parede, um mural: a fila do bandejão do RU ao meio-dia, todo mundo com fome.
- BYTE: Profª, por que tem fila de bandejão num templo de compiladores? Eu tô com fome e não entendi nada.
- PROF: Porque FIRST é exatamente isso: **quem pode abrir a fila**. Cada não-terminal é uma turma que vai entrar na fila. FIRST(turma) = quem pode ser o primeiro a aparecer.
- TEXTO: Só um tiquinho mais formal: FIRST(A) é o conjunto de **terminais que podem aparecer no começo** de alguma cadeia derivada de A. Terminal é gente de verdade; não-terminal é uma turma que ainda vai mandar alguém.
- CÓDIGO (A fila de S: primeiro a turma A, depois a turma B, depois o c.):
```
S → A B c
A → a | ε
B → b | ε
```
- PROF: Olha a regra S → A B c. Quem abre a fila de S? A turma A vai na frente. Se A manda o 'a', a fila começa com a.
- BYTE: Então FIRST(S) = {a}. Fácil!
- PROF: Calma, Byte! Repara em A → ε: a turma A pode **faltar**. Dizemos que A é **anulável**. Se ela falta, quem abre a fila?
- PREVEJA: A e B podem faltar (os dois têm produção ε). Quem pode abrir a fila de S → A B c?
    opções: Só a | a e b | ✔a, b e c | a, b, c e ε
    explicação: Se A vem: a. Se A falta e B vem: b. Se os dois faltam: o c abre a fila. Mas c é terminal e NUNCA falta, então ε não entra. FIRST(S) = {a, b, c}.
- CAIXA [Gancho de memória]: Turma anulável = turma que pode faltar. Se ela falta, **passa a vez para o vizinho de trás**. Só se TODO mundo da regra puder faltar é que o ε entra no FIRST.
- WIDGET FIRST_FOLLOW gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: Veja o motor descobrindo FIRST(S) = {a, b, c}. Repare no passo em que ele 'pula' o A anulável.
- CAIXA [Não existem perguntas idiotas]: P: Pra que eu quero saber quem abre a fila? R: O analisador LL(1) olha UM token e precisa escolher a produção. Se estou em S e chega um b, o FIRST me diz: 'pode ir de S → A B c, o b é bem-vindo aqui'.
- REVELAR: Sem olhar: quanto valem FIRST(A) e FIRST(B) nessa gramática?
    → FIRST(A) = {a, ε} e FIRST(B) = {b, ε}. O ε aparece porque cada uma tem uma produção vazia.
- BYTE: Resumindo: FIRST é quem pode abrir a fila; turma que pode faltar passa a vez. Até que não sou tão burro assim.
- PROF: Nunca foi! E se você errou o 'preveja', ótimo: o cérebro guarda melhor aquilo que o surpreendeu.

#### ESTILO VISUAL (Passo a passo visual): "FIRST ao vivo: o ponto fixo trabalhando"
- PROF: Hoje você só vai olhar. O motor do jogo vai calcular FIRST na frente dos seus olhos, regra por regra.
- CÓDIGO (A gramática de expressões sem recursão à esquerda (a versão LL(1)).):
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- BYTE: Por onde ele começa?
- TEXTO: Por todas as regras, em **voltas**. A cada volta, cada produção A → X1 X2 … tenta **acrescentar** coisas a FIRST(A). Quando uma volta inteira passa sem nada novo, paramos: é o **ponto fixo**.
- PREVEJA: Por que FIRST(E) precisa de mais voltas do que FIRST(F) para ficar pronto?
    opções: Porque E é o símbolo inicial | ✔Porque depende de FIRST(T), que depende de FIRST(F) | Porque E tem ε | Porque E tem mais produções
    explicação: Os terminais ( e id nascem em F e 'sobem' a corrente: F → T → E. Enquanto FIRST(T) estiver vazio, E não tem o que copiar.
- WIDGET FIRST_FOLLOW gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» legenda: Avance passo a passo olhando só a parte FIRST: veja ( e id subindo de F para T e de T para E.
- PASSOS (O que olhar no widget):
    1) Primeira volta: E' ganha {+, ε} e T' ganha {*, ε}. Terminal no começo da regra entra direto; produção ε dá ε.
    2) Ainda na primeira volta: F ganha {(, id}.
    3) Depois: T → F T' copia FIRST(F) para FIRST(T).
    4) Depois: E → T E' copia FIRST(T) para FIRST(E).
    5) Uma volta inteira sem novidade: ponto fixo, fim.
- BYTE: Por que T não ganhou o ε de T'?
- PROF: Porque em T → F T' o F vem primeiro e **não** é anulável. F sempre manda alguém, então o T' nem é consultado para o começo.
- CAIXA [Cuidado!]: Copie FIRST(Xi) **sem o ε**. O ε só entra em FIRST(A) se TODOS os símbolos do lado direito puderem sumir.
- REVELAR: FIRST(E), FIRST(T) e FIRST(F) são iguais. Qual é o conjunto?
    → { (, id }. Toda expressão começa com abre-parênteses ou com um identificador.
- CAIXA [Poder do cérebro]: E se a regra fosse F → ( E ) | id | num? Quais conjuntos FIRST mudariam?
- REVELAR: Responda o Poder do Cérebro.
    → FIRST(F), FIRST(T) e FIRST(E) ganham num: a mudança sobe a corrente inteira. FIRST(E') e FIRST(T') não mudam.
- PROF: Viu? Não tem mágica: é só repetir 'copia o que vem no começo' até parar de mudar.

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: FIRST das declarações"
- PROF: Vamos resolver juntos, devagar, um exercício do tipo que cai em prova.
- CÓDIGO (Declarações estilo C: int a, b, c;  (vírgula e ponto e vírgula são terminais)):
```
D → T L ;
T → int | float
L → id R
R → , id R | ε
```
- PASSOS (FIRST passo a passo):
    1) 1) Terminais são o FIRST de si mesmos: FIRST(int) = {int}, FIRST(id) = {id}, FIRST(,) = {,}.
    2) 2) T → int | float: as duas produções começam com terminal. FIRST(T) = {int, float}.
    3) 3) L → id R: começa com o terminal id. FIRST(L) = {id}.
    4) 4) R → , id R | ε: a primeira começa com ',' e a segunda é ε. FIRST(R) = {',', ε}.
    5) 5) D → T L ;: começa com T, que NÃO é anulável. Copio FIRST(T) e paro. FIRST(D) = {int, float}.
    6) 6) Mais uma volta conferindo tudo: nada muda. Ponto fixo!
- CAIXA [Cuidado!]: No passo 5, NÃO continue para o L só porque deu vontade. Só se avança para o próximo símbolo quando o atual é anulável.
- WIDGET FIRST_FOLLOW gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» legenda: Confira com o motor: os mesmos conjuntos.
- BYTE: Agora me deixa tentar um sozinho?
- PROF: Exemplo 'desbotado': eu começo, você termina. Gramática de expressões LL(1).
- CÓDIGO:
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- TEXTO: De presente: FIRST(E') = {+, ε} e FIRST(T') = {*, ε}.
- REVELAR: Sua vez: FIRST(F) = ?
    → {(, id}. As duas produções começam com terminal.
- REVELAR: E FIRST(T), sabendo que T → F T'?
    → {(, id}. F não é anulável, então FIRST(T) = FIRST(F) e o T' nem entra na conta.
- REVELAR: Por último, FIRST(E) para E → T E'?
    → {(, id}. Mesmo raciocínio: T não é anulável. Repare que ε NÃO entra em FIRST(E).
- PROF: Acertou duas de três? Você já está no nível de prova. Errou? Releia o passo 5: é o erro nº 1 da turma, e você acabou de vacinar o cérebro contra ele.

#### ESTILO SOCRATIC (Perguntas guiadas): "Descubra a regra do FIRST sozinho"
- PROF: Hoje eu não vou te dar a regra. Só vou fazer perguntas. Quem descobre é você.
- PREVEJA: A → b C. Qual é FIRST(A)?
    opções: ✔{b} | {b, C} | FIRST(C) | {C}
    explicação: Tudo que A gera começa com o terminal b. O que vem depois não interessa para o FIRST.
- PREVEJA: Agora A → B c, com B → x. FIRST(A)?
    opções: {c} | ✔{x} | {x, c} | {B}
    explicação: A começa com B, e B começa com x. FIRST(A) herda FIRST(B) = {x}. Não-terminal nunca entra no conjunto: só terminais (e talvez ε).
- BYTE: Então quando começa com variável, eu copio o FIRST dela. Saquei.
- PREVEJA: E se B → x | ε? Em A → B c, FIRST(A) = ?
    opções: {x} | {x, ε} | ✔{x, c} | {x, c, ε}
    explicação: Se B sumir, a cadeia começa com c. Então entram x e c. O ε não entra, porque o c nunca some.
- PREVEJA: E se for A → B C, com B → x | ε e C → y | ε?
    opções: {x, y} | ✔{x, y, ε} | {x} | {ε}
    explicação: x (B aparece), y (B some e C aparece) e ε (os dois somem: A pode gerar a cadeia vazia).
- REVELAR: Qual 'regra' você acabou de descobrir? Tente dizer com suas palavras antes de tocar.
    → Para A → X1 X2 … Xn: pegue FIRST(X1) sem ε. Se X1 é anulável, pegue também FIRST(X2) sem ε, e assim por diante. Se TODOS forem anuláveis, acrescente ε.
- PROF: É exatamente o algoritmo do Livro do Dragão. Você o reinventou.
- PREVEJA: Última: A → A b | c (recursiva à esquerda). FIRST(A)?
    opções: Loop infinito, não dá pra calcular | ✔{c} | {b, c} | {A, c}
    explicação: A primeira regra só diz 'FIRST(A) contém FIRST(A)': nada de novo. A segunda dá c. O ponto fixo para em {c}. O cálculo não trava; quem trava com recursão à esquerda é o analisador descendente!
- CAIXA [Poder do cérebro]: Por que o cálculo precisa de várias 'voltas' (ponto fixo) em vez de uma passada só? Pense em A → B e B → A | x, visitadas nessa ordem.
- REVELAR: Responda o Poder do Cérebro.
    → Na 1ª passada, ao olhar A → B, FIRST(B) ainda está vazio. Só depois B ganha x, e precisamos voltar para A copiar. Repete-se até nada mudar.
- WIDGET FIRST_FOLLOW gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: Confirme suas descobertas: o motor aplica exatamente as regras que você deduziu.

#### ESTILO FORMAL (Definição de prova): "FIRST: definição e algoritmo (versão prova)"
- PROF: Agora de terno e gravata. Esta é a versão que você escreve na prova.
- TEXTO: **Definição.** Para uma cadeia α de símbolos gramaticais, FIRST(α) = { a terminal | α ⇒* a β }. Se α ⇒* ε, então ε ∈ FIRST(α) também.
- PASSOS (Regras para cada símbolo X):
    1) 1. Se X é terminal, FIRST(X) = {X}.
    2) 2. Se X → ε é uma produção, acrescente ε a FIRST(X).
    3) 3. Se X → Y1 Y2 … Yk: acrescente FIRST(Y1) − {ε}; se ε ∈ FIRST(Y1), acrescente FIRST(Y2) − {ε}; e assim por diante. Se ε ∈ FIRST(Yi) para todo i, acrescente ε.
    4) 4. Repita até nenhum conjunto mudar (ponto fixo).
- CÓDIGO (Ponto fixo: os conjuntos só crescem e o número de terminais é finito, então o laço termina. (Com k = 0, A → ε, o ε entra.)):
```
para cada não-terminal A: FIRST(A) := ∅
repita
  para cada produção A → Y1 Y2 … Yk:
    todosAnulaveis := verdadeiro
    para i := 1 até k:
      FIRST(A) := FIRST(A) ∪ (FIRST(Yi) − {ε})
      se ε ∉ FIRST(Yi): todosAnulaveis := falso; pare
    se todosAnulaveis: FIRST(A) := FIRST(A) ∪ {ε}
até nenhum FIRST mudar
```
- TEXTO: **FIRST de uma cadeia** α = X1 X2 … Xn: mesmo raciocínio. Some FIRST(X1) − {ε} e avance enquanto o símbolo atual for anulável; ε ∈ FIRST(α) só se todos os Xi forem anuláveis (ou se α = ε).
- BYTE: Por que 'só crescem' garante que termina?
- PROF: Porque há um número finito de terminais. Cada volta que muda algo acrescenta pelo menos um elemento; uma hora acaba o que acrescentar.
- PREVEJA: Em S → A B c, A → a | ε, B → b | ε, quanto vale FIRST(A B)?
    opções: {a} | {a, b} | ✔{a, b, ε} | {a, b, c}
    explicação: A é anulável → olho B; B também é anulável → ε entra. FIRST(A B) = {a, b, ε}. O c não faz parte da cadeia A B!
- WIDGET FIRST_FOLLOW gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: Confira cada regra aplicada no passo a passo.
- CAIXA [Dica de prova]: Na prova: (1) liste os anuláveis primeiro; (2) comece pelos não-terminais cujas produções começam com terminal; (3) escreva os conjuntos com chaves e diga explicitamente se ε pertence. ε em FIRST: pode. $ em FIRST: nunca.
- CAIXA [Cuidado!]: FIRST contém apenas terminais e, possivelmente, ε. Nunca um não-terminal.
- REVELAR: Defina FIRST em uma frase, sem olhar.
    → FIRST(α) é o conjunto de terminais que podem iniciar alguma cadeia derivada de α, mais ε se α pode derivar a cadeia vazia.

#### ESTILO MNEMONIC (Truque de memória): "Ganchos para nunca esquecer o FIRST"
- NARRADOR: Palácio da Memória: você entra no Templo Top-Down. A primeira sala tem uma porta giratória com uma placa: **FIRST, quem entra primeiro**.
- CAIXA [Gancho de memória]: **FIRST = Fila: quem Inicia.** Imagine a porta giratória: passa um de cada vez, e o FIRST é quem pode passar primeiro.
- CAIXA [Gancho de memória]: Rima: **'Terminal na frente? Entra e pronto, minha gente. Variável na frente? Empresta o FIRST dela. Pode sumir? Olha o vizinho da janela!'**
- BYTE: E o ε? Quando ele entra?
- CAIXA [Gancho de memória]: **'Todos faltaram? Fila vazia: ε.'** Um único terminal (ou um não-terminal não anulável) no caminho já barra o ε.
- TEXTO: Contraste que salva nota: FIRST(A) olha o **começo do lado direito das regras DE A**. FOLLOW(A) (próxima sala) olha **onde A aparece** no lado direito das regras dos outros.
- PASSOS (Confusões clássicas lado a lado):
    1) FIRST pode ter ε; FOLLOW nunca tem ε.
    2) FOLLOW do símbolo inicial sempre tem $; FIRST nunca tem $.
    3) FIRST(A) olha as produções DE A; FOLLOW(A) olha as produções ONDE A aparece.
    4) FIRST de um terminal é ele mesmo: FIRST(id) = {id}.
- PREVEJA: Qual destes conjuntos é IMPOSSÍVEL ser um FIRST?
    opções: {a, ε} | {id, (} | ✔{+, $} | {ε}
    explicação: $ é o marcador de fim de entrada: mora no FOLLOW, nunca no FIRST. Já {ε} é possível, por exemplo com A → ε sozinho.
- WIDGET FIRST_FOLLOW gramática=«S → ( S ) S | ε» legenda: Teste o gancho em S → ( S ) S | ε: quem abre a fila? '(': ou ninguém (ε).
- REVELAR: Diga a rima de cabeça e aplique em S → ( S ) S | ε.
    → 'Terminal na frente? Entra e pronto' → '(' entra. A produção ε: 'todos faltaram' → ε entra. FIRST(S) = {(, ε}.
- PROF: Esqueceu? Volte a esta sala do palácio: porta giratória, fila, quem inicia. O resto sai daí.


## ASSUNTO FOLLOW: Conjunto FOLLOW

#### ESTILO STORY (História / Analogia): "Quem vem logo depois de você na fila?"
- NARRADOR: Segunda sala do Templo Top-Down: um ponto de ônibus. A fila dá a volta no quarteirão.
- PROF: Na sala do FIRST, perguntávamos quem **abre** a fila de uma turma. Aqui a pergunta muda: quem pode estar **logo atrás** da turma A, em alguma fila possível?
- TEXTO: FOLLOW(A) = conjunto de **terminais que podem aparecer imediatamente à direita de A** em alguma forma sentencial. Se A puder ser o último da fila inteira, entra o **$**: o motorista fechando a porta (fim da entrada).
- CÓDIGO (De novo a nossa gramática da fila.):
```
S → A B c
A → a | ε
B → b | ε
```
- BYTE: Quem vem depois de B? Fácil: em S → A B c, depois do B vem c.
- PROF: Isso! FOLLOW(B) = {c}. E depois de A?
- PREVEJA: Em S → A B c, com B → b | ε, quem pode vir logo depois de A?
    opções: Só b | Só c | ✔b e c | b, c e ε
    explicação: Se B aparece, depois de A vem b. Se B falta, depois de A vem direto o c. FOLLOW(A) = {b, c}. ε nunca entra no FOLLOW: alguém sempre vem depois, nem que seja o $.
- BYTE: E o S? Ninguém aparece depois dele em regra nenhuma!
- PROF: S é o símbolo inicial: a frase inteira. Depois da frase inteira vem o fim da entrada. Então FOLLOW(S) = {$}. Sempre, sem exceção, o $ entra no FOLLOW do inicial.
- CAIXA [Gancho de memória]: Regra da herança: **se B é o último da turma A, quem vem depois de A também vem depois de B.** O último da fila herda o 'depois' do grupo inteiro.
- WIDGET FIRST_FOLLOW gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: Veja o motor: FOLLOW(S) = {$}, FOLLOW(A) = {b, c}, FOLLOW(B) = {c}.
- CAIXA [Não existem perguntas idiotas]: P: Pra que saber quem vem depois? R: Para decidir quando usar A → ε! Se o próximo token é algo que pode vir DEPOIS de A, faz sentido A sumir. É assim que a tabela LL(1) usa o FOLLOW.
- REVELAR: Em S → ( S ) S | ε, o que pode vir depois de S?
    → ')' (pelo S de dentro dos parênteses) e $ (S é o inicial, e o último S herda FOLLOW(S)). FOLLOW(S) = {), $}.
- PROF: FIRST: quem abre. FOLLOW: quem vem atrás. Duas perguntas sobre a mesma fila.

#### ESTILO VISUAL (Passo a passo visual): "FOLLOW ao vivo nas expressões"
- PROF: Mesma gramática de expressões, agora com os olhos grudados no FOLLOW.
- CÓDIGO:
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- TEXTO: O motor começa colocando $ em FOLLOW(E), porque E é o inicial. Depois percorre as produções procurando **onde cada não-terminal aparece no lado direito** e o que vem à direita dele.
- PREVEJA: Em F → ( E ), o que entra em FOLLOW(E)?
    opções: ( | ✔) | $ | id
    explicação: Logo depois do E vem o ')'. Então ')' ∈ FOLLOW(E). Junto com o $, FOLLOW(E) = {), $}.
- WIDGET FIRST_FOLLOW gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» legenda: Vá até a parte FOLLOW e leia a justificativa de cada passo.
- PASSOS (O que observar):
    1) E → T E': depois de T vem E', então FIRST(E') − {ε} = {+} entra em FOLLOW(T).
    2) Como E' é anulável, FOLLOW(E) também entra em FOLLOW(T).
    3) E' está no fim de E → T E': FOLLOW(E') herda FOLLOW(E) = {), $}.
    4) T → F T': FOLLOW(F) ganha FIRST(T') − {ε} = {*} e, como T' pode sumir, herda FOLLOW(T).
    5) Repete as voltas até nada mudar.
- BYTE: Tá, mas quanto deu no fim?
- REVELAR: Tente antes de tocar: FOLLOW(T) = ?
    → {+, ), $}: o + vem de FIRST(E'), e ) e $ vêm herdados de FOLLOW(E).
- REVELAR: E FOLLOW(F) = ?
    → {*, +, ), $}: o * vem de FIRST(T'); o resto é herdado de FOLLOW(T).
- CAIXA [Poder do cérebro]: Repare: FOLLOW(E') = FOLLOW(E) e FOLLOW(T') = FOLLOW(T). Por quê? Dica: onde E' e T' aparecem nas regras?
- REVELAR: Responda o Poder do Cérebro.
    → E' só aparece no FIM das produções de E e de E' (E → T E', E' → + T E'), então só recebe herança de FOLLOW(E) e de si mesmo. O mesmo vale para T' com T.
- WIDGET FIRST_FOLLOW gramática=«S → ( S ) S | ε» legenda: Bônus: S → ( S ) S | ε. Veja o ')' e o $ chegando em FOLLOW(S).

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: FOLLOW passo a passo"
- PROF: Calcular FOLLOW sem FIRST é como fazer bolo sem farinha. Então começamos com os FIRST prontos.
- CÓDIGO:
```
D → T L ;
T → int | float
L → id R
R → , id R | ε
```
- TEXTO: Do exercício de FIRST: FIRST(D) = FIRST(T) = {int, float}, FIRST(L) = {id}, FIRST(R) = {',', ε}. R é o único anulável.
- PASSOS (FOLLOW passo a passo):
    1) 1) D é o inicial: FOLLOW(D) = {$}.
    2) 2) Em D → T L ;, depois de T vem L. FIRST(L) = {id} entra em FOLLOW(T). FOLLOW(T) = {id}.
    3) 3) Na mesma regra, depois de L vem ';'. FOLLOW(L) = {;}.
    4) 4) Em L → id R, R está no fim: FOLLOW(R) herda FOLLOW(L) = {;}.
    5) 5) Em R → , id R, R está no fim de novo: herda FOLLOW(R) de si mesmo. Nada novo.
    6) 6) Mais uma volta: nada muda. Ponto fixo.
- BYTE: Pera: no passo 2, por que o ';' não entra em FOLLOW(T) também?
- PROF: Porque L **não é anulável**: ele sempre gera pelo menos um id. Então o ';' nunca encosta no T.
- WIDGET FIRST_FOLLOW gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» legenda: Confira: FOLLOW(D) = {$}, FOLLOW(T) = {id}, FOLLOW(L) = FOLLOW(R) = {;}.
- PROF: Agora o desbotado. Gramática S → A B c, A → a | ε, B → b | ε, com FIRST(A) = {a, ε} e FIRST(B) = {b, ε}.
- TEXTO: Eu faço o primeiro: S é o inicial, então FOLLOW(S) = {$}. S não aparece em nenhum lado direito, então acabou para ele.
- REVELAR: Sua vez: FOLLOW(B)?
    → {c}. Em S → A B c, depois de B vem o terminal c.
- REVELAR: E FOLLOW(A)? Cuidado com o B anulável.
    → {b, c}. Depois de A vem B: entra FIRST(B) − {ε} = {b}. Como B pode sumir, olho o próximo: c.
- CAIXA [Cuidado!]: Colocou ε em FOLLOW(A)? Normal, acontece com todo mundo! Mas ε NUNCA está em FOLLOW. O ε de FIRST(B) é só um sinal de 'olhe mais à direita'.

#### ESTILO SOCRATIC (Perguntas guiadas): "Perguntas que levam ao FOLLOW"
- PROF: Gramática pequenininha: S → a S b | ε. Ela gera a b, a a b b, a a a b b b… e a cadeia vazia.
- WIDGET DERIVATION gramática=«S → a S b | ε» entrada=«a a b b» legenda: Olhe as formas sentenciais: a S b, a a S b b… O que aparece logo depois de S?
- PREVEJA: Olhando a derivação, que terminal aparece logo à direita de S nas formas sentenciais?
    opções: a | ✔b | ε | nenhum
    explicação: Em a S b e a a S b b, o S está sempre colado num b. Então b ∈ FOLLOW(S).
- PREVEJA: E na forma sentencial inicial, que é só 'S'? O que vem depois?
    opções: Nada, então FOLLOW fica vazio | ✔O fim da entrada, $ | ε | a
    explicação: Depois da frase completa vem o fim da entrada. Por isso a regra: $ ∈ FOLLOW(símbolo inicial).
- BYTE: Então FOLLOW(S) = {b, $}. Mas por que olhar as regras em vez de derivar tudo?
- PROF: Porque as derivações são infinitas! As regras resumem tudo: onde S aparece num lado direito, olho o que está à direita dele.
- PREVEJA: Nova gramática: A → x B y. O que você conclui sobre FOLLOW(B)?
    opções: ✔y ∈ FOLLOW(B) | x ∈ FOLLOW(B) | FOLLOW(B) ⊆ FOLLOW(A) | Nada
    explicação: B é seguido de y. O FIRST do que vem à direita (aqui, y) entra em FOLLOW(B).
- PREVEJA: E se for A → x B, com B no fim?
    opções: FOLLOW(B) fica vazio | ✔FOLLOW(A) ⊆ FOLLOW(B) | FOLLOW(B) ⊆ FOLLOW(A) | x ∈ FOLLOW(B)
    explicação: Tudo que pode vir depois de A pode vir depois de B, porque B termina A. A herança vai do PAI (lado esquerdo) para o filho do fim.
- PREVEJA: E se for A → x B C, com C anulável?
    opções: Só FIRST(C) − {ε} | Só FOLLOW(A) | ✔FIRST(C) − {ε} e também FOLLOW(A) | FIRST(C) com ε
    explicação: C pode aparecer (entra FIRST(C) sem ε) ou sumir (aí B fica no fim e herda FOLLOW(A)).
- REVELAR: Junte tudo: escreva as três regras do FOLLOW com suas palavras.
    → 1) $ no FOLLOW do inicial. 2) Em A → α B β, FIRST(β) − {ε} entra em FOLLOW(B). 3) Se β é vazio ou anulável, FOLLOW(A) entra em FOLLOW(B).
- WIDGET FIRST_FOLLOW gramática=«S → a S b | ε» legenda: Confira: FOLLOW(S) = {b, $}.
- PROF: Você acabou de reconstruir o algoritmo do Livro do Dragão. Se tropeçou no caminho, ótimo: foi o tropeço que fixou a regra.

#### ESTILO FORMAL (Definição de prova): "FOLLOW: definição e algoritmo (versão prova)"
- PROF: Versão prova, com todas as letras.
- TEXTO: **Definição.** Para um não-terminal A, FOLLOW(A) é o conjunto de terminais a que podem aparecer imediatamente à direita de A em alguma forma sentencial, isto é, S ⇒* α A a β. Se A pode ser o símbolo mais à direita de uma forma sentencial, $ ∈ FOLLOW(A).
- PASSOS (Regras (aplique até nada mudar)):
    1) 1. Coloque $ em FOLLOW(S), onde S é o símbolo inicial.
    2) 2. Para cada produção A → α B β: tudo em FIRST(β), exceto ε, entra em FOLLOW(B).
    3) 3. Para cada produção A → α B, ou A → α B β com ε ∈ FIRST(β): tudo em FOLLOW(A) entra em FOLLOW(B).
- CÓDIGO (Repare: FIRST(ε) = {ε}. Por isso, quando B está no fim (β vazio), a regra 3 dispara.):
```
para cada não-terminal A: FOLLOW(A) := ∅
FOLLOW(S) := { $ }
repita
  para cada produção A → X1 X2 … Xn:
    para i := 1 até n:
      se Xi é um não-terminal B:
        β := Xi+1 … Xn
        FOLLOW(B) := FOLLOW(B) ∪ (FIRST(β) − {ε})
        se ε ∈ FIRST(β):        // β vazio ou anulável
          FOLLOW(B) := FOLLOW(B) ∪ FOLLOW(A)
até nenhum FOLLOW mudar
```
- PREVEJA: Qual afirmação é SEMPRE verdadeira?
    opções: ε pode estar em FOLLOW | ✔$ ∈ FOLLOW(símbolo inicial) | FOLLOW(A) ⊆ FIRST(A) | FOLLOW de um terminal é ele mesmo
    explicação: É a regra 1. FOLLOW nunca contém ε, e não existe relação geral entre FOLLOW(A) e FIRST(A).
- WIDGET FIRST_FOLLOW gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» legenda: Cada passo do widget cita a regra usada. Tente prever o próximo antes de avançar.
- CAIXA [Dica de prova]: Monte uma tabelinha Não-terminal | FIRST | FOLLOW. Faça TODOS os FIRST antes. No FOLLOW, para cada não-terminal, varra as regras procurando onde ele aparece do lado DIREITO e anote a regra usada (2 ou 3). Professor adora ver a justificativa.
- CAIXA [Cuidado!]: Direção da herança: a regra 3 é FOLLOW(A) ⊆ FOLLOW(B), do lado esquerdo para o símbolo do fim. Nunca o contrário.
- REVELAR: Para S → A a A b | B b B a, A → ε, B → ε: quanto valem FOLLOW(A) e FOLLOW(B)?
    → FOLLOW(A) = {a, b} e FOLLOW(B) = {a, b}. Depois do primeiro A vem a, depois do segundo vem b; o mesmo para B. (Exemplo clássico: a gramática é LL(1) mesmo com A e B anuláveis.)
- BYTE: Então: $ no inicial, olho à direita, e se acabou ou pode sumir, herdo do pai.
- PROF: Essa é a prova inteira numa frase só.

#### ESTILO MNEMONIC (Truque de memória): "Dólar, Vizinho, Herança"
- NARRADOR: Segunda sala do Templo Top-Down: um ponto de ônibus com a placa **FOLLOW, quem vem atrás**. O ônibus se chama $ e sempre para no fim da linha.
- CAIXA [Gancho de memória]: **As 3 regras do FOLLOW = Dólar, Vizinho, Herança.** Dólar: $ no inicial. Vizinho: quem está à direita empresta o FIRST (sem ε). Herança: o filho do fim herda o FOLLOW do pai.
- CAIXA [Gancho de memória]: Rima: **'Ficou no fim, ou o resto pode sumir? Herda do pai o que vai vir!'**
- BYTE: Eu sempre confundo a direção da herança…
- PROF: Pensa em herança de verdade: desce do pai para o filho. FOLLOW(pai, o lado esquerdo) desce para FOLLOW(filho do fim). Filho não deixa herança pro pai.
- PASSOS (FIRST × FOLLOW: o duelo):
    1) FIRST: quem ABRE a fila. FOLLOW: quem vem ATRÁS.
    2) FIRST pode ter ε. FOLLOW: jamais ε.
    3) FOLLOW pode ter $. FIRST: jamais $.
    4) FIRST(A): olho as regras DE A. FOLLOW(A): olho as regras ONDE A aparece à direita.
    5) FOLLOW depende de FIRST; FIRST não depende de FOLLOW.
- PREVEJA: Byte calculou FOLLOW(X) = {a, ε, $}. Onde está o erro?
    opções: Falta o b | O $ não pode | ✔O ε não pode | Está certo
    explicação: ε nunca entra em FOLLOW. Se apareceu, ele provavelmente copiou FIRST(β) sem tirar o ε, quando devia ter aplicado a Herança.
- WIDGET FIRST_FOLLOW gramática=«S → a S b | ε» legenda: Teste o gancho em S → a S b | ε: Dólar dá $; Vizinho dá b. FOLLOW(S) = {b, $}.
- CAIXA [Conversa ao pé da lareira]: FIRST e FOLLOW ao pé da lareira. FIRST: 'Eu sou o começo de tudo!' FOLLOW: 'E eu sou o que vem depois. Mas não vivo sem você: sempre que olho o vizinho, peço o FIRST dele emprestado.' FIRST: 'E quando o vizinho some?' FOLLOW: 'Aí peço ao meu pai. Família é pra isso.'
- REVELAR: Diga as 3 regras pelo gancho 'Dólar, Vizinho, Herança'.
    → Dólar: $ ∈ FOLLOW(inicial). Vizinho: em A → α B β, FIRST(β) − {ε} ⊆ FOLLOW(B). Herança: se β é vazio ou anulável, FOLLOW(A) ⊆ FOLLOW(B).


## ASSUNTO LL1_TABLE: Tabela LL(1)

#### ESTILO STORY (História / Analogia): "O caderninho do porteiro"
- NARRADOR: Terceira sala do Templo Top-Down: um porteiro de balada com um caderninho enorme na mão.
- PROF: O porteiro sabe em que 'parte da festa' você está (o não-terminal A) e vê quem está chegando (o próximo token a). O caderninho diz qual porta abrir: **M[A, a] = qual produção usar**.
- BYTE: E como ele preencheu esse caderninho?
- PROF: Com duas regrinhas. **Regra 1:** se a pode abrir a fila de α (a ∈ FIRST(α)), anote A → α em M[A, a].
- PROF: **Regra 2:** se α pode sumir (ε ∈ FIRST(α)), então para cada b que pode vir depois de A (b ∈ FOLLOW(A)), anote A → α em M[A, b]. Inclusive na coluna $.
- CÓDIGO (A gramática da fila de novo. FOLLOW(A) = {b, c}, FOLLOW(B) = {c}.):
```
S → A B c
A → a | ε
B → b | ε
```
- PREVEJA: Estou em A e chega um 'c'. O que diz o caderninho?
    opções: A → a | ✔A → ε | Erro | As duas
    explicação: 'c' não abre A → a. Mas A pode sumir e c ∈ FOLLOW(A) = {b, c}. Então M[A, c] = A → ε: a turma A falta e o c é atendido mais adiante.
- WIDGET LL1_TABLE gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: O caderninho completo. Repare em A → ε nas colunas b e c.
- BYTE: E se o porteiro tiver DUAS portas anotadas na mesma linha e coluna?
- PROF: Aí ele trava: é um **conflito**, e a gramática **não é LL(1)**. Olha só:
- WIDGET LL1_TABLE gramática=«S → a b | a c» legenda: S → a b | a c: as duas produções começam com a, e caem juntas em M[S, a]. O porteiro não sabe qual porta abrir.
- REVELAR: E uma célula VAZIA no caderninho, significa o quê?
    → Erro de sintaxe: nenhuma produção de A pode começar com aquele token, nem ele pode vir depois de um A que sumiu.
- CAIXA [No mundo real]: O GCC e o Clang, compiladores de C/C++ usados no mundo todo, têm analisadores descendentes escritos à mão: o mesmo 'caderninho', só que embutido em ifs.
- PROF: Resumo do porteiro: FIRST abre a porta; se a produção pode sumir, o FOLLOW abre também. Duas portas na mesma célula? Briga na entrada.

#### ESTILO VISUAL (Passo a passo visual): "Vendo a tabela nascer (e quebrar)"
- PROF: Vamos ver a tabela da gramática de expressões nascer, e depois o que acontece com uma gramática 'proibida'.
- CÓDIGO:
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- WIDGET LL1_TABLE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» legenda: Toque nas células para ver por que cada produção foi parar ali.
- PASSOS (O que olhar):
    1) Linhas = não-terminais; colunas = terminais + $.
    2) E → T E' aparece em ( e id: é FIRST(T E').
    3) E' → ε aparece em ) e $: é FOLLOW(E').
    4) T' → ε aparece em +, ) e $: é FOLLOW(T').
    5) Nenhuma célula tem duas produções: a gramática é LL(1).
- PREVEJA: Em qual célula fica F → ( E )?
    opções: ✔M[F, (] | M[F, )] | M[E, (] | M[F, id]
    explicação: FIRST(( E )) = {(}. Então M[F, (] = F → ( E ).
- BYTE: E aquela gramática bonita, E → E + T | T, com recursão à esquerda?
- WIDGET LL1_TABLE gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» legenda: A versão recursiva à esquerda: veja as células em conflito.
- TEXTO: M[E, id] ganhou **duas** produções, E → E + T e E → T, porque FIRST(E + T) = FIRST(T) = {(, id}. Recursão à esquerda sempre provoca esse tipo de conflito.
- CAIXA [Cuidado!]: Gramática com recursão à esquerda ou ambígua NUNCA é LL(1). Primeiro conserte (na Ponte da Transformação), depois monte a tabela.
- REVELAR: Quantas células preenchidas tem a tabela da versão LL(1)? Conte no widget antes de tocar.
    → 13: E (2), E' (3), T (2), T' (4) e F (2).
- WIDGET LEFT_RECURSION gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» legenda: Lembrete da Ponte: eliminar a recursão à esquerda leva da gramática com conflitos à versão LL(1).

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: tabela das declarações"
- PROF: Exercício de prova: construa a tabela LL(1) da gramática de declarações.
- CÓDIGO:
```
D → T L ;
T → int | float
L → id R
R → , id R | ε
```
- TEXTO: Ingredientes já calculados: FIRST(T) = {int, float}, FIRST(L) = {id}, FIRST(R) = {',', ε}; FOLLOW(D) = {$}, FOLLOW(T) = {id}, FOLLOW(L) = FOLLOW(R) = {;}.
- PASSOS (Produção por produção):
    1) D → T L ; : FIRST(T L ;) = {int, float}. Anote em M[D, int] e M[D, float].
    2) T → int : M[T, int].
    3) T → float : M[T, float].
    4) L → id R : M[L, id].
    5) R → , id R : FIRST = {,}. M[R, ,].
    6) R → ε : FIRST(ε) = {ε}, então use FOLLOW(R) = {;}. M[R, ;].
    7) Nenhuma célula com duas produções → LL(1).
- WIDGET LL1_TABLE gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» legenda: Confira as 7 células preenchidas. Todo o resto é erro.
- BYTE: Por que R → ε não vai na coluna $?
- PROF: Porque $ ∉ FOLLOW(R): depois de R sempre vem ';'. Se chegar $ com R no topo, é erro mesmo, faltou o ponto e vírgula!
- PROF: Agora o desbotado: S → A B c, A → a | ε, B → b | ε, com FOLLOW(A) = {b, c} e FOLLOW(B) = {c}.
- TEXTO: Eu faço: S → A B c tem FIRST = {a, b, c}, então vai em M[S, a], M[S, b] e M[S, c]. A → a vai em M[A, a]. B → b vai em M[B, b].
- REVELAR: Sua vez: onde vai A → ε?
    → Em M[A, b] e M[A, c], porque FOLLOW(A) = {b, c}.
- REVELAR: E B → ε?
    → Em M[B, c], porque FOLLOW(B) = {c}.
- REVELAR: A gramática é LL(1)?
    → Sim: nenhuma célula ficou com duas produções.
- WIDGET LL1_TABLE gramática=«S → A B c ⏎ A → a | ε ⏎ B → b | ε» legenda: Confira suas respostas.

#### ESTILO SOCRATIC (Perguntas guiadas): "Como escolher sem voltar atrás?"
- PROF: Pergunta-raiz: o analisador está em A e vê o token a. Como ele escolhe a produção sem nunca voltar atrás?
- PREVEJA: A → x y | z w. Chega o token z. Qual produção?
    opções: A → x y | ✔A → z w | Precisa testar as duas | Nenhuma
    explicação: z ∈ FIRST(z w) e z ∉ FIRST(x y). Escolha única. É a regra do FIRST.
- PREVEJA: A → x y | x w. Chega x. E agora?
    opções: A → x y | A → x w | ✔Não dá para decidir com 1 token | Erro
    explicação: As duas começam com x: conflito em M[A, x]. Com um só token de espiada não dá. (O remédio é a fatoração à esquerda.)
- PREVEJA: A → x y | ε, e FOLLOW(A) = {z}. Chega z. Qual?
    opções: A → x y | ✔A → ε | Erro | As duas
    explicação: z não abre x y, mas pode vir DEPOIS de A. Então A some: A → ε. É a regra do FOLLOW.
- PREVEJA: A → x y | ε, mas agora FOLLOW(A) = {x, z}. Chega x. Qual?
    opções: A → x y | A → ε | ✔Conflito: as duas | Erro
    explicação: x abre x y (FIRST) E x pode vir depois de A (FOLLOW). Duas produções na mesma célula: não é LL(1).
- REVELAR: Com base nessas 4 perguntas, quais são as condições para uma gramática ser LL(1)? Tente formular antes de tocar.
    → Para cada par A → α | β: (1) FIRST(α) e FIRST(β) disjuntos; (2) no máximo um deles deriva ε; (3) se β ⇒* ε, FIRST(α) e FOLLOW(A) disjuntos.
- BYTE: Então a tabela é só um jeito de enxergar essas condições de uma vez?
- PROF: Exatamente. Montou a tabela e nenhuma célula tem duas produções? É LL(1). Simples assim.
- WIDGET LL1_TABLE gramática=«S → S a | b» legenda: Teste: S → S a | b. Antes de olhar, preveja se haverá conflito.
- REVELAR: Por que M[S, b] tem conflito?
    → FIRST(S a) = FIRST(S) = {b} e FIRST(b) = {b}: as duas produções disputam a coluna b. Recursão à esquerda gera conflito sempre.
- CAIXA [Poder do cérebro]: Uma gramática ambígua pode ser LL(1)? Pense: se fosse, a tabela daria uma única produção a cada passo…
- REVELAR: Responda o Poder do Cérebro.
    → Não. O analisador LL(1) constrói uma única árvore de forma determinística; uma gramática ambígua tem duas árvores para alguma frase, então sua tabela terá conflito.

#### ESTILO FORMAL (Definição de prova): "Construção da tabela preditiva (versão prova)"
- PROF: Algoritmo de construção da tabela de análise preditiva, como no Livro do Dragão.
- CÓDIGO (Percorra PRODUÇÃO por produção, não célula por célula.):
```
entrada: gramática G, com FIRST e FOLLOW calculados
saída:   tabela M[A, a]

para cada produção A → α de G:
  para cada terminal a ∈ FIRST(α):
    adicione A → α a M[A, a]
  se ε ∈ FIRST(α):
    para cada b ∈ FOLLOW(A):      // b pode ser $
      adicione A → α a M[A, b]
toda entrada vazia de M = erro
```
- TEXTO: **Definição.** G é **LL(1)** se e somente se M não tem nenhuma entrada com mais de uma produção. LL(1): lê a entrada da esquerda para a direita (**L**), produz uma derivação mais à esquerda (**L**), usando **1** símbolo de lookahead.
- PASSOS (Condições equivalentes, para cada par A → α | β):
    1) 1. FIRST(α) ∩ FIRST(β) = ∅.
    2) 2. No máximo um entre α e β deriva ε.
    3) 3. Se β ⇒* ε, então FIRST(α) ∩ FOLLOW(A) = ∅ (e vice-versa).
- PREVEJA: Qual destas NÃO é condição para LL(1)?
    opções: FIRST das alternativas disjuntos | No máximo uma alternativa anulável | ✔A gramática não pode ter produções ε | FIRST(α) ∩ FOLLOW(A) = ∅ quando a outra alternativa é anulável
    explicação: Produções ε são permitidas em LL(1)! A gramática de expressões LL(1) tem E' → ε e T' → ε.
- WIDGET LL1_TABLE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» legenda: Cada célula traz o motivo: 'a ∈ FIRST(α)' ou 'α pode virar ε e b ∈ FOLLOW(A)'.
- CAIXA [Dica de prova]: Na prova: (1) calcule FIRST e FOLLOW numa tabelinha; (2) percorra produção por produção; (3) não esqueça a coluna $; (4) se perguntarem 'é LL(1)?', aponte a célula com conflito e as duas produções que brigam nela.
- CAIXA [Cuidado!]: Recursão à esquerda, ambiguidade ou prefixos comuns não fatorados impedem LL(1). Mas a volta não vale: tirar tudo isso não garante LL(1) (ex.: S → A a, A → a | ε dá conflito em M[A, a]).
- REVELAR: Formule a definição de LL(1) em uma linha.
    → G é LL(1) se sua tabela preditiva não tem nenhuma entrada multiplamente definida.
- BYTE: LL(1): Left-to-right, Leftmost, 1 token. Guardei!

#### ESTILO MNEMONIC (Truque de memória): "Sudoku LL(1): uma célula, uma produção"
- NARRADOR: Terceira sala do Templo: um tabuleiro de Sudoku gigante pintado no chão. Linhas com letras maiúsculas, colunas com tokens.
- CAIXA [Gancho de memória]: **'Linha é Variável, Coluna é Token.'** M[A, a]: o maiúsculo vem primeiro, como num endereço 'rua, número'.
- CAIXA [Gancho de memória]: Rima de ouro: **'Primeiro o FIRST; se o ε aparecer, chama o FOLLOW pra resolver.'**
- CAIXA [Gancho de memória]: **Sudoku LL(1): uma célula, uma produção.** Dois números no mesmo quadradinho invalidam o Sudoku. Duas produções na mesma célula invalidam o LL(1).
- BYTE: E o nome LL(1)? Sempre esqueço o que é cada L.
- CAIXA [Gancho de memória]: **L-L-1 = 'Lê da esquerda, Leva a derivação mais à esquerda, 1 espiadinha.'** Left-to-right, Leftmost derivation, 1 token de lookahead.
- PASSOS (Confusões clássicas):
    1) Pôr A → ε nas colunas do FIRST: errado. ε vai nas colunas de FOLLOW(A).
    2) Esquecer a coluna $: ela existe e recebe produções ε quando $ ∈ FOLLOW(A).
    3) Achar que célula vazia é 'tanto faz': é ERRO de sintaxe.
    4) Achar que produção ε impede LL(1): não impede.
- PREVEJA: A → ε e FOLLOW(A) = {), $}. Em quais colunas vai A → ε?
    opções: Na coluna ε | ✔Nas colunas ) e $ | Só na coluna $ | Em todas as colunas
    explicação: Não existe coluna ε! A produção vazia vai nas colunas dos terminais de FOLLOW(A): ) e $.
- WIDGET LL1_TABLE gramática=«S → ( S ) S | ε» legenda: Teste a rima em S → ( S ) S | ε: o FIRST manda S → ( S ) S para '('; o ε chama o FOLLOW = {), $}.
- REVELAR: Recite a rima e aplique em E' → + T E' | ε, com FOLLOW(E') = {), $}.
    → 'Primeiro o FIRST': M[E', +] = E' → + T E'. 'Se o ε aparecer, chama o FOLLOW': M[E', )] = M[E', $] = E' → ε.


## ASSUNTO LL1_PARSING: Analisador preditivo (pilha)

#### ESTILO STORY (História / Analogia): "Pilha, o golem das tarefas"
- NARRADOR: No altar do Templo Top-Down mora Pilha, um golem de pedra. Ele só enxerga o topo da própria cabeça.
- PILHA: Eu… guardo… o que… ainda… falta… ver.
- PROF: A pilha do analisador preditivo é uma **lista de tarefas**: o que ainda esperamos encontrar na entrada. Começa com S (a frase inteira) em cima do $ (o fim).
- BYTE: E como o golem trabalha?
- PASSOS (As regras do golem):
    1) Topo é terminal e igual ao token da entrada? **Casa**: tira do topo e avança na entrada. Tarefa cumprida!
    2) Topo é não-terminal A e o token é a? Consulta o caderninho M[A, a] e **troca A pelo lado direito**, empilhado de trás para frente.
    3) Topo $ e entrada $? **Aceita**.
    4) Qualquer outra situação? **Erro**.
- PILHA: De… trás… pra… frente… porque… o primeiro… tem que… ficar… em cima.
- PREVEJA: Pilha $ E (topo à direita), entrada começando com id, e M[E, id] = E → T E'. Como fica a pilha?
    opções: $ T E' | ✔$ E' T | $ E T E' | $ T
    explicação: Tira o E e empilha T E' ao contrário: primeiro E', depois T. O T fica no topo, pronto para ser atendido primeiro.
- WIDGET LL1_TRACE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«id + id» legenda: Acompanhe o golem: pilha, entrada e ação a cada passo.
- CAIXA [Não existem perguntas idiotas]: P: Por que pilha e não fila? R: Porque a árvore é aninhada: ao expandir T, tudo que é de T precisa ser resolvido antes de voltar ao E'. Quem entrou por último sai primeiro: pilha!
- REVELAR: Quando o golem usa E' → ε, o que ele empilha?
    → Nada. Ele só tira o E' do topo: a tarefa 'ver um E'' foi cumprida com zero tokens.
- PILHA: Pilha… vazia… entrada… vazia… festa.
- PROF: E repare: a sequência de expansões é exatamente a **derivação mais à esquerda**. O golem é um derivador com bússola.

#### ESTILO VISUAL (Passo a passo visual): "Analisador preditivo, passo a passo"
- PROF: Entrada clássica: id + id * id. São 17 passos até o 'aceitar'. Vamos assistir.
- CÓDIGO:
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- WIDGET LL1_TRACE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«id + id * id» legenda: Avance um passo por vez. Antes de cada clique, tente adivinhar a ação.
- PASSOS (Pontos de atenção no trace):
    1) Passos 1–3: E → T E', T → F T', F → id. Três expansões até aparecer um terminal no topo.
    2) Passo 4: id casa com id.
    3) Passo 5: T' no topo, entrada '+'. M[T', +] = T' → ε: o T' some.
    4) Passo 6: E' → + T E'. O '+' foi previsto pela tabela, e no passo 7 ele casa.
    5) No fim: T' → ε e E' → ε com $ na entrada; pilha $ e entrada $: aceita.
- PREVEJA: Topo = T', entrada = *. Qual ação?
    opções: T' → ε | ✔T' → * F T' | Casar * | Erro
    explicação: * ∈ FIRST(* F T'), então M[T', *] = T' → * F T'. Só no passo seguinte o * vai casar.
- BYTE: Por que no passo 5 o T' sumiu e nesse outro ele expandiu?
- PROF: Por causa do token! Com '+' o T' não tem o que fazer (+ ∈ FOLLOW(T')), então some. Com '*' ele tem trabalho: expande.
- REVELAR: Quantas vezes 'id' foi casado nesse trace?
    → 3: uma para cada id da entrada. Cada terminal da entrada é casado exatamente uma vez.
- WIDGET DERIVATION gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«id + id * id» legenda: Compare: a derivação mais à esquerda usa as mesmas produções, na mesma ordem das expansões do trace.
- CAIXA [Poder do cérebro]: E se a entrada fosse 'id + * id'? Em que momento o golem travaria?
- REVELAR: Responda o Poder do Cérebro.
    → Depois de casar o '+', o topo é T e o token é '*'. M[T, *] está vazia → erro de sintaxe, detectado já no primeiro token impossível.

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: int id , id ;"
- PROF: Vamos simular à mão, no formato que a prova pede: Pilha | Entrada | Ação. Topo da pilha à direita.
- CÓDIGO (Tabela: M[D,int] = D → T L ;   M[T,int] = T → int   M[L,id] = L → id R   M[R,,] = R → , id R   M[R,;] = R → ε):
```
D → T L ;
T → int | float
L → id R
R → , id R | ε
```
- PASSOS (Simulação de int id , id ;):
    1) $ D | int id , id ; $ | expandir D → T L ;
    2) $ ; L T | int id , id ; $ | expandir T → int
    3) $ ; L int | int id , id ; $ | casar int
    4) $ ; L | id , id ; $ | expandir L → id R
    5) $ ; R id | id , id ; $ | casar id
    6) $ ; R | , id ; $ | expandir R → , id R
    7) $ ; R id , | , id ; $ | casar ,
- BYTE: Olha só: o ';' ficou lá embaixo esperando desde o primeiro passo!
- PILHA: Paciência… é… meu… forte.
- TEXTO: Agora os passos finais são seus. Pense em cada um antes de revelar. Pilha atual: $ ; R id. Entrada: id ; $.
- REVELAR: Próxima ação?
    → Casar id. A pilha fica $ ; R e a entrada ; $.
- REVELAR: Topo R, entrada ';'. Ação?
    → Expandir R → ε (é M[R, ;]). A pilha fica $ ;.
- REVELAR: E depois?
    → Casar ';'. Pilha $ e entrada $ → aceitar!
- WIDGET LL1_TRACE gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» entrada=«int id , id ;» legenda: Confira com o motor: 11 linhas, terminando em 'aceitar'.
- CAIXA [Dica de prova]: Escreva a pilha SEMPRE com o topo do mesmo lado (de preferência à direita) e o $ no fundo. Metade dos erros de simulação em prova é pilha 'virada' no meio do caminho.

#### ESTILO SOCRATIC (Perguntas guiadas): "Reinvente o analisador preditivo"
- PROF: Você vai redescobrir o analisador preditivo não recursivo. Ponto de partida: uma pilha contendo $ S.
- PREVEJA: Por que colocar $ no fundo da pilha?
    opções: Enfeite | ✔Para saber quando a pilha 'acabou' e comparar com o fim da entrada | Porque $ é um não-terminal | Para guardar o resultado
    explicação: $ é a sentinela: quando o topo é $ e a entrada também acabou ($), aceitamos.
- PREVEJA: O topo é o terminal ')' e a entrada é 'id'. O que fazer?
    opções: Casar mesmo assim | ✔Erro: esperava ')' | Consultar a tabela | Empilhar id
    explicação: Terminal no topo é uma promessa: 'o próximo token TEM que ser este'. Veio outro? Erro de sintaxe.
- PREVEJA: O topo é A e M[A, a] = A → X Y Z. Por que empilhar Z, depois Y, depois X?
    opções: Por tradição | ✔Para X ficar no topo e ser tratado primeiro | Para Z casar primeiro | Tanto faz
    explicação: Derivamos sempre o símbolo mais à esquerda: X é o próximo a ser expandido ou casado, então precisa estar no topo.
- PREVEJA: O topo é A e M[A, a] está vazia. O que isso significa?
    opções: Aceitar | Empilhar a | ✔Erro de sintaxe | Tentar outra produção (backtracking)
    explicação: O analisador preditivo NUNCA volta atrás. Célula vazia = nenhuma produção de A serve = erro.
- REVELAR: Monte o algoritmo com as suas respostas, em 4 casos (X = topo, a = token).
    → (1) X = a = $ → aceita. (2) X = a terminal → desempilha e avança. (3) X terminal ≠ a → erro. (4) X não-terminal → se M[X, a] = X → Y1…Yk, troca X por Yk…Y1 (Y1 no topo); se vazia, erro.
- BYTE: E qual é a 'saída' do analisador?
- PREVEJA: Se eu imprimir cada produção usada nas expansões, o que obtenho?
    opções: Uma derivação mais à direita | ✔Uma derivação mais à esquerda | A tabela LL(1) | Os conjuntos FOLLOW
    explicação: A expansão acontece sempre no não-terminal pendente mais à esquerda: é a derivação mais à esquerda, o segundo L de LL(1).
- WIDGET LL1_TRACE gramática=«S → ( S ) S | ε» entrada=«( ) ( )» legenda: Teste suas regras em S → ( S ) S | ε com a entrada ( ) ( ).
- REVELAR: Nesse trace, quantas vezes S → ε foi usada?
    → 3 vezes: dentro do primeiro par, dentro do segundo par e no final, com $ na entrada.
- PROF: Você reinventou o algoritmo do Livro do Dragão. Isso não é coisa de 'burro', hein?

#### ESTILO FORMAL (Definição de prova): "Analisador preditivo não recursivo (versão prova)"
- PROF: Analisador preditivo não recursivo, dirigido por tabela: a definição de prova.
- TEXTO: Componentes: um **buffer de entrada** terminado em $; uma **pilha** de símbolos gramaticais com $ no fundo; a **tabela** M[A, a]; e um **programa de controle**. Configuração inicial: pilha = $ S (S no topo), entrada = w$.
- CÓDIGO (O programa de controle. Para A → ε, nada é empilhado.):
```
empilhe $; empilhe S
a := primeiro símbolo de w$
X := topo da pilha
enquanto X ≠ $:
  se X = a:                desempilhe; a := próximo símbolo
  senão se X é terminal:   erro()
  senão se M[X, a] vazia:  erro()
  senão se M[X, a] = X → Y1 Y2 … Yk:
    imprima X → Y1 Y2 … Yk
    desempilhe
    empilhe Yk, Yk-1, …, Y1      // Y1 fica no topo
  X := topo da pilha
se a = $: aceite   senão: erro()
```
- TEXTO: **Invariante:** a parte da entrada já casada, seguida da pilha lida do topo para o fundo, é sempre uma forma sentencial mais à esquerda. Por isso a saída é uma derivação mais à esquerda.
- PREVEJA: Configuração: pilha $ E' T' (topo à direita), entrada $. O que acontece a seguir?
    opções: Erro | ✔T' → ε, E' → ε, aceita | Casa $ com T' | E' → + T E'
    explicação: M[T', $] = T' → ε e M[E', $] = E' → ε, pois $ está em FOLLOW de ambos. Sobra pilha $ e entrada $: aceita.
- WIDGET LL1_TRACE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«( id )» legenda: Siga o pseudocódigo linha a linha nesta entrada curta.
- CAIXA [Dica de prova]: Formato esperado: colunas **Pilha | Entrada | Ação** (ou Saída), uma linha por passo. Ações escritas como 'E → T E'' ou 'casar id'. Termine com 'aceita' ou marque a linha do erro.
- CAIXA [Cuidado!]: Não confunda as pilhas: a do LL guarda o que ainda FALTA ver (previsão). A do LR, lá na Fortaleza Bottom-Up, guarda o que JÁ foi visto.
- REVELAR: Quais são as situações do programa de controle?
    → X = a = $: aceita. X = a: casa e avança. X terminal ≠ a, ou M[X, a] vazia: erro. X não-terminal com M[X, a] = produção: substitui X pelo lado direito, invertido.
- CAIXA [No mundo real]: O ANTLR, gerador de analisadores muito usado, produz analisadores descendentes da família LL (no ANTLR 4, o ALL(*)), primos do que você está estudando.

#### ESTILO MNEMONIC (Truque de memória): "As três frases na testa do golem"
- NARRADOR: No altar do Templo Top-Down, o golem Pilha tem três frases gravadas na testa.
- CAIXA [Gancho de memória]: **'Terminal no topo? Casa e anda. Variável no topo? Consulta e troca. $ com $? Festa!'**
- CAIXA [Gancho de memória]: **Empilhar ao contrário = arrumar a mochila.** O que você vai usar primeiro entra por último e fica em cima. Para A → X Y Z: guarda Z, depois Y, depois X.
- BYTE: E qual a diferença entre a pilha daqui e a da Fortaleza Bottom-Up?
- CAIXA [Conversa ao pé da lareira]: LL e LR ao pé da lareira. LL: 'Minha pilha é o FUTURO: tudo que eu prevejo que vai aparecer.' LR: 'A minha é o PASSADO: tudo que já li e ainda não consegui montar.' LL: 'Eu decido cedo, só com o primeiro token.' LR: 'Eu espero ver o lado direito inteiro antes de decidir. Por isso aceito mais gramáticas.'
- PASSOS (Confusões clássicas):
    1) Empilhar o lado direito na ordem normal (X no fundo): errado, X tem que ficar no topo.
    2) Avançar a entrada numa expansão: não! Só avança quando CASA um terminal.
    3) Empilhar ε: não existe. Produção ε só desempilha.
    4) Esquecer de começar com $ S na pilha.
- PREVEJA: Qual ação faz a entrada avançar?
    opções: Expandir | ✔Casar | Ambas | Nenhuma
    explicação: Só casar consome token. Expandir só reorganiza a pilha.
- WIDGET LL1_TRACE gramática=«S → a S b | ε» entrada=«a a b b» legenda: Teste as frases do golem em S → a S b | ε.
- REVELAR: Recite as três frases do golem.
    → Terminal no topo? Casa e anda. Variável no topo? Consulta (a tabela) e troca (pelo lado direito, invertido). $ com $? Festa (aceita)!


## ASSUNTO RECURSIVE_DESCENT: Descida recursiva

#### ESTILO STORY (História / Analogia): "A repartição dos funcionários-função"
- NARRADOR: Corredor final do Templo Top-Down: uma repartição pública cheia de guichês. Cada guichê tem o nome de um não-terminal.
- PROF: Na descida recursiva, **cada não-terminal vira uma função**: um funcionário. O funcionário S atende comandos; o L, listas; o E, expressões.
- CÓDIGO (A gramática dos comandos da repartição.):
```
S → if E then S | print E | begin L end
L → S L | ε
E → id
```
- BYTE: E como o funcionário S sabe o que fazer?
- PROF: Ele olha a **senha do próximo cliente**: o token de lookahead. Senha 'if'? Segue o roteiro de S → if E then S. Senha 'print'? Roteiro de S → print E.
- TEXTO: Seguir o roteiro é simples: para cada **terminal** do lado direito, confere a senha e chama o próximo cliente (**match**). Para cada **não-terminal**, manda o cliente ao guichê daquele funcionário (**chama a função**).
- PREVEJA: S recebeu a senha 'begin'. Qual é a sequência de ações do funcionário S?
    opções: ✔match(begin); L(); match(end) | L(); match(begin); match(end) | match(begin); S(); match(end) | Chama E()
    explicação: É o roteiro de S → begin L end: confere 'begin', manda para o L, confere 'end'.
- WIDGET PARSE_TREE gramática=«S → if E then S | print E | begin L end ⏎ L → S L | ε ⏎ E → id» entrada=«begin print id end» legenda: A árvore sintática é o organograma das chamadas: cada nó interno é uma função que foi chamada.
- BYTE: E o L, que pode ser ε? Como ele sabe que deve ficar quieto?
- PROF: Se a senha for 'end' (que está em FOLLOW(L)), o L retorna sem fazer nada: a lista acabou. Se for if, print ou begin (FIRST(S)), ele chama S() e depois L() de novo.
- CAIXA [Cuidado!]: Funcionário que, como primeiro ato, chama a si mesmo sem atender ninguém (A → A α) entra em laço infinito. Recursão à esquerda mata a descida recursiva.
- REVELAR: Quem faz o papel da 'pilha' na descida recursiva, se não há pilha explícita?
    → A pilha de chamadas do próprio programa! Cada função chamada e ainda não terminada é um não-terminal pendente.
- CAIXA [No mundo real]: GCC e Clang, compiladores de C/C++ usados no mundo inteiro, usam analisadores de descida recursiva escritos à mão: fáceis de ler e ótimos para mensagens de erro.

#### ESTILO VISUAL (Passo a passo visual): "Vendo as funções se chamarem"
- PROF: Vamos ver a descida recursiva acontecendo: cada passo da derivação é uma chamada de função.
- CÓDIGO:
```
E → T E'
E' → + T E' | ε
T → F T'
T' → * F T' | ε
F → ( E ) | id
```
- CÓDIGO (Uma função por não-terminal (E' virou Elinha, T' virou Tlinha).):
```
fun E() { T(); Elinha() }
fun Elinha() {
  if (look == "+") { match("+"); T(); Elinha() }
  // senão: ε (look deve estar em FOLLOW(E') = { ), $ })
}
fun T() { F(); Tlinha() }
fun Tlinha() {
  if (look == "*") { match("*"); F(); Tlinha() }
}
fun F() {
  when (look) {
    "("  -> { match("("); E(); match(")") }
    "id" -> match("id")
    else -> erro("esperava ( ou id")
  }
}
```
- WIDGET DERIVATION gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«id * id» legenda: Cada expansão desta derivação mais à esquerda corresponde a uma chamada de função do código acima.
- PASSOS (Chamadas para id * id):
    1) E() chama T().
    2) T() chama F(); F() vê 'id' e faz match(id).
    3) T() chama Tlinha(); vê '*': match(*), F() casa o segundo id, Tlinha() de novo.
    4) Tlinha() vê $: não faz nada (ε) e retorna.
    5) De volta em E(), que chama Elinha(); vê $: ε, retorna.
    6) E() retorna com look = $: sucesso!
- PREVEJA: Ao analisar 'id * id', quantas vezes F() é chamada?
    opções: 1 | ✔2 | 3 | 0
    explicação: Uma por id: T() chama F() para o primeiro e Tlinha() chama F() para o segundo.
- WIDGET PARSE_TREE gramática=«E → T E' ⏎ E' → + T E' | ε ⏎ T → F T' ⏎ T' → * F T' | ε ⏎ F → ( E ) | id» entrada=«id * id» legenda: A árvore é o retrato das chamadas: os nós E, T, F, T' e E' são as funções.
- BYTE: E por que não usar a gramática E → E + T direto?
- WIDGET LEFT_RECURSION gramática=«E → E + T | T ⏎ T → T * F | F ⏎ F → ( E ) | id» legenda: Porque E() chamaria E() sem consumir nada: laço infinito. A transformação cria justamente as funções Elinha e Tlinha.
- REVELAR: Na árvore de 'id * id', quantos nós T' aparecem, e por quê?
    → Dois: o primeiro T' (filho de T) expande para * F T', e o T' de dentro vira ε. Cada chamada de Tlinha() é um nó T'.

#### ESTILO WORKED (Exemplo resolvido): "Exemplo resolvido: funções das declarações"
- PROF: Exercício de prova: escreva um analisador de descida recursiva para as declarações.
- CÓDIGO:
```
D → T L ;
T → int | float
L → id R
R → , id R | ε
```
- TEXTO: Receita: **uma função por não-terminal**; uma variável global `look` com o token atual; uma função `match(t)` que confere e avança.
- CÓDIGO (O 'conferir a senha'.):
```
fun match(t: String) {
  if (look == t) look = proximoToken()
  else erro("esperava " + t + ", veio " + look)
}
```
- CÓDIGO (D, T e L resolvidos. D e L têm uma produção só: nem precisam olhar o lookahead.):
```
fun D() { T(); L(); match(";") }      // produção única

fun T() {
  when (look) {
    "int"   -> match("int")
    "float" -> match("float")
    else    -> erro("esperava um tipo")
  }
}

fun L() { match("id"); R() }          // L → id R
```
- BYTE: E o R, que tem ε?
- PROF: Esse fica para você. Pense: qual token faz R escolher R → , id R? E qual faz escolher R → ε?
- REVELAR: Qual token escolhe R → , id R?
    → ',': é FIRST(, id R).
- REVELAR: E qual escolhe R → ε?
    → ';': é FOLLOW(R). Qualquer outro token é erro.
- REVELAR: Escreva R() completo.
    → fun R() { when (look) { "," -> { match(","); match("id"); R() };  ";" -> { /* ε: não consome nada */ };  else -> erro("esperava , ou ;") } }
- WIDGET PARSE_TREE gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» entrada=«int id , id ;» legenda: Cada nó interno = uma chamada. Repare no R chamando R: recursão à DIREITA, inofensiva.
- CAIXA [Dica de prova]: Nas produções com ε, deixe o teste explícito: 'se look ∈ FOLLOW(A), não faz nada; senão, erro'. Detectar o erro cedo (no else) costuma valer ponto.

#### ESTILO SOCRATIC (Perguntas guiadas): "Inventando a descida recursiva"
- PROF: Vamos inventar a descida recursiva do zero. Objetivo: uma função que reconheça 'tudo que S gera'.
- PREVEJA: S → a B c. Qual é o corpo mais natural de S()?
    opções: ✔match(a); B(); match(c) | B(); match(a); match(c) | match(a); match(B); match(c) | S(); B()
    explicação: Siga o lado direito da esquerda para a direita: terminal vira match; não-terminal vira chamada da função dele.
- PREVEJA: S → a B | c D. Como S() escolhe?
    opções: Tenta a primeira e, se falhar, volta atrás | ✔Olha o token: 'a' → primeira, 'c' → segunda | Sempre a primeira | Sorteia
    explicação: Com FIRST disjuntos, um token basta. É a descida recursiva PREDITIVA, sem backtracking.
- PREVEJA: E → E + T | T. O que acontece se E() começar chamando E()?
    opções: Funciona normal | ✔Laço infinito / estouro de pilha | Erro de sintaxe | Lê o +
    explicação: E() chama E() sem consumir token nenhum: chama de novo, e de novo… estouro de pilha. Recursão à esquerda é proibida aqui.
- PREVEJA: A → b A | ε. Com que tokens A() deve escolher o ε?
    opções: Com b | ✔Com os tokens de FOLLOW(A) | Com qualquer token | Nunca
    explicação: Se o token não abre b A mas pode vir depois de A, A some. É a mesma lógica da tabela LL(1).
- BYTE: Então descida recursiva é a tabela LL(1) escrita em ifs?
- PROF: Exatamente! Cada 'when (look)' é uma linha da tabela. E a pilha? É a pilha de chamadas.
- PREVEJA: A → x y | x z. A descida recursiva preditiva funciona direto?
    opções: Sim | ✔Não: precisa fatorar à esquerda (A → x A', A' → y | z) | Sim, se usar FOLLOW | Não: precisa eliminar ε
    explicação: O mesmo token x abre as duas alternativas: sem fatorar, só com backtracking.
- WIDGET LEFT_FACTORING gramática=«S → a b | a c» legenda: Veja a fatoração deixando a gramática pronta para virar funções.
- REVELAR: Resuma com suas palavras como construir uma descida recursiva.
    → Uma função por não-terminal; o lookahead escolhe a produção (FIRST, ou FOLLOW para a alternativa ε); no corpo, match para terminais e chamada para não-terminais; a gramática precisa ser LL(1) (sem recursão à esquerda e fatorada).
- WIDGET PARSE_TREE gramática=«S → if E then S | print E | begin L end ⏎ L → S L | ε ⏎ E → id» entrada=«if id then print id» legenda: Treino: imagine S() → match(if), E(), match(then), S()… e confira com a árvore.

#### ESTILO FORMAL (Definição de prova): "Descida recursiva: definição de prova"
- PROF: Definição de prova, no estilo do Livro do Dragão.
- TEXTO: Um **analisador de descida recursiva** é um conjunto de procedimentos, um para cada não-terminal. A execução começa pelo procedimento do símbolo inicial e tem sucesso se ele termina tendo consumido toda a entrada.
- CÓDIGO (Procedimento típico para um não-terminal A.):
```
void A() {
  escolha uma produção A → X1 X2 … Xk     // pelo lookahead
  para i := 1 até k:
    se Xi é não-terminal: chame Xi()
    senão se Xi = símbolo de entrada atual: avance a entrada
    senão: erro()
}
```
- TEXTO: Na forma geral, a escolha da produção pode exigir **retrocesso** (backtracking). Na forma **preditiva**, o lookahead decide: usa-se A → α se look ∈ FIRST(α), ou se α ⇒* ε e look ∈ FOLLOW(A). Isso exige uma gramática **LL(1)**.
- PASSOS (Requisitos da descida recursiva preditiva):
    1) Sem recursão à esquerda (direta ou indireta): senão, laço infinito.
    2) Fatorada à esquerda: senão, um token não decide.
    3) Condições LL(1) satisfeitas: alternativas com conjuntos de escolha disjuntos.
    4) Se nenhuma alternativa serve para o lookahead: erro (a menos que haja alternativa ε e look ∈ FOLLOW(A)).
- PREVEJA: Qual a relação entre a descida recursiva preditiva e o analisador preditivo com tabela?
    opções: Reconhecem classes diferentes | ✔Mesma classe (LL(1)); a pilha explícita vira a pilha de chamadas | A descida recursiva é bottom-up | O de tabela não precisa de FIRST
    explicação: São duas implementações da mesma ideia: a tabela vira os ifs e a pilha vira a recursão.
- CÓDIGO (Exercício típico: escreva S(), L() e E().):
```
S → if E then S | print E | begin L end
L → S L | ε
E → id
```
- REVELAR: Escreva L() para L → S L | ε.
    → L() { se look ∈ {if, print, begin}: S(); L()   senão se look = end: retorna (ε)   senão: erro() }
- WIDGET FIRST_FOLLOW gramática=«S → if E then S | print E | begin L end ⏎ L → S L | ε ⏎ E → id» legenda: Os conjuntos que decidem cada if: FIRST(S) = {if, print, begin} e FOLLOW(L) = {end}.
- CAIXA [Dica de prova]: Na prova: (1) elimine recursão à esquerda e fatore; (2) calcule FIRST/FOLLOW; (3) uma função por não-terminal com um 'switch' no lookahead; (4) não esqueça o match e a checagem final do $ (principal: S(); se look ≠ $, erro).
- REVELAR: Por que a recursão à DIREITA (L → S L) não causa problema?
    → Porque antes de chamar L() de novo, S() consome pelo menos um token. A entrada diminui a cada chamada, então a recursão termina.

#### ESTILO MNEMONIC (Truque de memória): "Uma variável, uma função"
- NARRADOR: Último corredor do Templo Top-Down: uma fileira de portas, cada uma com o nome de um não-terminal. Atrás de cada porta, um funcionário que pode abrir outras portas.
- CAIXA [Gancho de memória]: **'Uma variável, uma função. Um terminal, um match.'**
- CAIXA [Gancho de memória]: **Recursão à esquerda = cachorro correndo atrás do rabo.** E() chama E() que chama E()… e nunca sai do lugar, porque não come nenhum token.
- CAIXA [Gancho de memória]: **'Olha a senha antes de atender.'** O lookahead é a senha do cliente: o FIRST escolhe o roteiro; o FOLLOW autoriza o 'não faço nada' (ε).
- LEXI: Eu entrego os tokens já cortadinhos. A função só precisa espiar o próximo: nada de reler texto!
- BYTE: E qual a diferença pra pilha do golem?
- CAIXA [Conversa ao pé da lareira]: Golem Pilha e Descida Recursiva ao pé da lareira. Pilha: 'Eu guardo os não-terminais pendentes numa pilha de pedra.' Descida: 'E eu na pilha de chamadas. Somos gêmeos, só muda a roupa.' Pilha: 'Mas você dá mensagens de erro mais bonitas.' Descida: 'Por isso o Clang me contratou.'
- PASSOS (Confusões clássicas):
    1) Descida recursiva NÃO é bottom-up: é top-down, começa pela raiz (S()).
    2) Recursão à DIREITA é ok; à ESQUERDA, não.
    3) Esquecer de checar $ no final: S() pode retornar com tokens sobrando.
    4) Escolher ε 'por padrão' sem checar FOLLOW: funciona, mas o erro é detectado mais tarde.
- PREVEJA: Qual gramática NÃO serve para descida recursiva preditiva sem transformar?
    opções: E → T E' | L → S L | ε | ✔A → A b | c | R → , id R | ε
    explicação: A → A b | c é recursiva à esquerda: A() chamaria A() sem consumir nada.
- WIDGET PARSE_TREE gramática=«D → T L ; ⏎ T → int | float ⏎ L → id R ⏎ R → , id R | ε» entrada=«float id ;» legenda: Cada nó interno = uma porta aberta (uma função chamada). Conte: D, T, L, R.
- REVELAR: Recite os dois ganchos principais.
    → 'Uma variável, uma função; um terminal, um match.' e 'Recursão à esquerda = cachorro correndo atrás do rabo.'


## ASSUNTO ERROR_RECOVERY: Erros sintáticos e recuperação

#### ESTILO STORY (História / Analogia): "O corretor de redação do ENEM"
- NARRADOR: Uma pilha de 300 redações e um café frio. Na linha 3 da primeira, o corretor acha um erro.
- BYTE: Aí ele rasga a redação e vai embora?
- PROF: Claro que não! Ele marca o erro, acha um ponto seguro para continuar, o próximo ponto final, e segue lendo. No fim, o aluno recebe TODOS os erros de uma vez. Um compilador bom faz igual.
- PASSOS (Os tipos de erro, na redação e no código):
    1) 'Caza' em vez de 'casa' → **erro léxico**: um caractere ou palavra que não existe (em C, um `@` solto).
    2) Frase sem verbo, parêntese sem fechar → **erro sintático**: a estrutura está quebrada (`x = (a + ;`).
    3) 'O sol congelou a feijoada' → **erro semântico**: estrutura ok, sentido não (somar string com int, variável não declarada).
    4) Texto perfeito, mas fugiu do tema → **erro lógico**: compila e roda, mas faz a coisa errada (`if (x = 0)` no lugar de `==`).
- PASSOS (As quatro estratégias do corretor):
    1) **Modo pânico**: pula tudo até o próximo ponto final e recomeça dali. Simples e nunca entra em laço; mas pode pular um trecho grande.
    2) **Nível de frase**: faz um remendo local, 'aqui faltou uma vírgula, finjo que ela está lá'. Cuidado: remendos mal escolhidos podem gerar laços infinitos.
    3) **Produções de erro**: o gabarito já prevê os erros mais comuns ('mais melhor') e tem uma mensagem pronta para cada um.
    4) **Correção global**: reescrever a redação com o MÍNIMO de mudanças até ela ficar certa. Lindo na teoria, caro demais na prática.
- PREVEJA: Qual dessas estratégias os compiladores de verdade mais usam?
    opções: Correção global | ✔Modo pânico (às vezes com remendos de frase) | Nenhuma: param no primeiro erro | Produções de erro para todo erro possível
    explicação: Pânico é simples e seguro. A correção global é cara demais; produções de erro só para casos frequentes.
- PILHA: No meu Templo, o 'ponto final' se chama **token de sincronização**. Em C, os favoritos são `;` e `}`.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = num id = num ;» legenda: Faltou um ';' depois do primeiro comando. Veja em que token o erro aparece e como o parser segue em frente.
- REVELAR: O que o parser fez com o ';' que faltou?
    → Com o terminal ';' no topo da pilha e 'id' na entrada, ele finge que o ';' estava lá: desempilha o ';' e continua. É um remendo em nível de frase, exatamente o 'finjo que a vírgula está aqui' do corretor.
- CAIXA [Conversa ao pé da lareira]: Corretor: 'Eu não conserto a redação, só aponto.' Compilador: 'Eu também! Recuperar não é corrigir o programa: é só voltar a um estado em que eu consiga achar o PRÓXIMO erro.'
- REVELAR: Na analogia, quem é: ponto final, remendo, gabarito de erros comuns e reescrita mínima?
    → Token de sincronização (modo pânico), recuperação em nível de frase, produções de erro e correção global.

#### ESTILO VISUAL (Passo a passo visual): "Sala do Pânico ao vivo"
- PILHA: Bem-vindo à Sala do Pânico. O token vermelho é onde eu PERCEBO o erro. Depois, avance passo a passo e veja como eu me recupero.
- CÓDIGO (Gramática: uma lista de atribuições. FIRST(E) = {id, num}, FOLLOW(E) = {;}):
```
P -> S ; P | ε
S -> id = E
E -> id | num
```
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = = num ; id = num ;» legenda: Um '=' a mais. Com E no topo e '=' na entrada, a célula M[E, =] está vazia.
- TEXTO: Regra do pânico: com o não-terminal A no topo e a célula vazia, **descarte tokens** até achar um que (1) está em **FIRST(A)**: aí A continua no topo, ou (2) está em **FOLLOW(A) ∪ {$}**: aí A é desempilhado.
- PREVEJA: No exemplo acima, o parser descarta o '=' e encontra 'num'. O que ele faz?
    opções: Desempilha E | ✔Continua com E, porque num ∈ FIRST(E) | Desiste | Volta ao início do programa
    explicação: 'num' pode começar um E, então dá para seguir com E → num. Só um token foi jogado fora.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = ; id = num ;» legenda: Agora a expressão SUMIU. O que acontece quando o próximo token já é um ';'?
- REVELAR: Por que aqui E foi desempilhado sem descartar nada?
    → O ';' não começa um E, mas está em FOLLOW(E): é o que viria logo DEPOIS de um E. O parser 'desiste' de E e sincroniza no ';'.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id num ; id = num ;» legenda: Faltou o '='. Desta vez quem está no topo é um TERMINAL.
- TEXTO: Terminal no topo diferente da entrada: o parser finge que o terminal estava lá e o desempilha. É uma recuperação simples, de nível de frase (uma 'inserção').
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = num num ; id = id ;» legenda: Um 'num' a mais. Conte quantas recuperações aparecem.
- CAIXA [Cuidado!]: UM erro de digitação gerou DUAS recuperações: primeiro o parser finge um ';' e depois descarta 'num ;'. É o **erro em cascata**: por isso, depois do primeiro erro, desconfie das mensagens seguintes.
- BYTE: Funciona com expressões também?
- WIDGET PANIC gramática=«E -> T E' ⏎ E' -> + T E' | ε ⏎ T -> F T' ⏎ T' -> * F T' | ε ⏎ F -> ( E ) | id» entrada=«id + * id» legenda: Na gramática de expressões: o '*' aparece onde deveria começar um termo T.
- REVELAR: Qual token é descartado em 'id + * id' e por quê?
    → O '*'. Com T no topo, M[T, *] está vazia; '*' não está em FIRST(T) = {(, id} nem em FOLLOW(T) = {+, ), $}. O próximo, 'id', está em FIRST(T): o parser segue com T.

#### ESTILO WORKED (Exemplo resolvido): "Rastreando o pânico na mão"
- PROF: Vamos rastrear com calma o modo pânico em `id = ; id = num ;`: o programador esqueceu o valor da primeira atribuição.
- CÓDIGO (FIRST(P) = {id, ε}   FIRST(S) = {id}   FIRST(E) = {id, num}
FOLLOW(P) = {$}   FOLLOW(S) = {;}   FOLLOW(E) = {;}):
```
P -> S ; P | ε
S -> id = E
E -> id | num
```
- PASSOS (Resolvido (topo da pilha à direita)):
    1) Pilha `$ P`, entrada `id = ; id = num ; $`: M[P, id] = P → S ; P. Pilha vira `$ P ; S`.
    2) M[S, id] = S → id = E. Pilha `$ P ; E = id`.
    3) Casa `id`, casa `=`. Pilha `$ P ; E`, entrada `; id = num ; $`.
    4) M[E, ;] está vazia → **erro detectado no 3º token**. Mensagem: 'esperava id ou num, encontrou ;'.
    5) Pânico: ';' ∉ FIRST(E), mas ';' ∈ FOLLOW(E). Nada a descartar: **desempilha E**.
    6) Pilha `$ P ;`: casa `;`. O resto, `id = num ;`, é analisado normalmente.
    7) No fim, P → ε com `$` e o parser chega ao fim: 1 erro relatado, análise completa.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = ; id = num ;» legenda: Confira cada passo com o motor.
- CAIXA [Cuidado!]: Depois de um erro, o parser NÃO gera código: a recuperação serve só para continuar procurando erros. O programa só compila de verdade quando a lista de erros fica vazia.
- TEXTO: Sua vez (exemplo esmaecido): `id = = num ; id = num ;`.
- REVELAR: Em que token o erro é detectado e quem está no topo da pilha?
    → No 3º token, o segundo '='. No topo está E (já casamos id e =).
- REVELAR: O que o pânico faz?
    → '=' não está em FIRST(E) nem em FOLLOW(E): descarta '='. O próximo, 'num', está em FIRST(E): para de descartar e segue com E → num.
- REVELAR: E em `id num ; id = num ;`, o que muda?
    → O erro aparece com o TERMINAL '=' no topo e 'num' na entrada. O parser finge que o '=' estava lá (desempilha o '=') e continua com E → num.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id num ; id = num ;» legenda: Confira a última resposta.
- PROF: No Bison, você não escreve o pânico na mão: usa o token especial `error` numa produção.
- CÓDIGO (parser.y: 'error ;' descarta tudo até o próximo ';' e segue para o próximo comando):
```
%{
#include <stdio.h>
extern int yylineno;      /* %option yylineno no scanner.l */
int yylex(void);
void yyerror(const char *s);
%}
%define parse.error verbose
%token ID NUM
%%
programa : programa comando
         | comando
         ;
comando  : ID '=' expr ';'
         | error ';'          { yyerrok; }
         ;
expr     : ID
         | NUM
         ;
%%
void yyerror(const char *s) {
    fprintf(stderr, "Erro sintatico na linha %d: %s\n", yylineno, s);
}
```
- CAIXA [Dica de prova]: Na prova, rastreie em tabela (pilha | entrada | ação) e escreva explicitamente: 'M[E, ;] vazia → erro; ; ∈ FOLLOW(E) → desempilha E'. Mostrar o conjunto de sincronização vale ponto.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que não parar no primeiro erro?"
- PROF: Perguntas, só perguntas. Você vai deduzir como um compilador deve reagir a erros.
- PREVEJA: Seu programa tem 5 erros de sintaxe. O compilador para no primeiro. Quantas vezes você compila até achar todos?
    opções: 1 | ✔Pelo menos 5 | Nenhuma | Depende do processador
    explicação: Um erro por compilação = pelo menos 5 rodadas. Por isso o compilador tenta se recuperar e relatar o máximo de erros de uma vez.
- PREVEJA: Em `id = num num ; id = id ;`, em qual token o parser LL(1) PERCEBE o erro?
    opções: No 1º (id) | No 3º (o primeiro num) | ✔No 4º (o segundo num) | Só no fim
    explicação: 'id = num' ainda pode virar um programa válido (basta vir um ';'). O 2º 'num' é o primeiro token que torna isso impossível.
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = num num ; id = id ;» legenda: O vermelho marca o ponto de detecção.
- BYTE: Ele grita exatamente no primeiro token impossível. Isso tem nome?
- PROF: Tem: **propriedade do prefixo viável**. LL e LR detectam o erro assim que o que foi lido não pode mais ser o começo de nenhum programa válido.
- PREVEJA: Com E no topo e um token inesperado, por que usar FOLLOW(E) como sincronizador?
    opções: Porque é o menor conjunto | ✔Porque se o próximo token é algo que vem DEPOIS de um E, dá para fingir que o E acabou e seguir | Porque FIRST é sempre vazio | Por tradição
    explicação: Desempilhar E e continuar com o que vem depois dele é a aposta mais segura: a pilha volta a combinar com a entrada.
- PREVEJA: E por que também parar num token de FIRST(E)?
    opções: ✔Porque aí dá para recomeçar o próprio E, sem jogar fora o resto | Não deveria parar | Para gerar mais erros | Porque FIRST = FOLLOW
    explicação: Em `id = = num`, depois de descartar o '=' extra, 'num' começa um E: não há por que abandonar o E.
- REVELAR: Por que ';' e '}' são ótimos tokens de sincronização em C?
    → Porque marcam FINS claros de construções (comando e bloco). Pular até eles costuma descartar só o comando quebrado, e o resto do programa é analisado normalmente.
- REVELAR: Qual o risco de sincronizar no lugar errado?
    → Erros em cascata: o parser volta 'desalinhado' e acusa erros que não existem. Por isso compiladores esperam alguns tokens corretos antes de relatar o próximo erro (o Bison espera 3).
- WIDGET PANIC gramática=«E -> T E' ⏎ E' -> + T E' | ε ⏎ T -> F T' ⏎ T' -> * F T' | ε ⏎ F -> ( E ) | id» entrada=«( id + ) * id» legenda: Desafio: antes de avançar, preveja se o ')' será descartado ou usado como sincronizador de T.
- REVELAR: Resposta do desafio
    → Sincronizador: ')' ∈ FOLLOW(T) = {+, ), $}. Nada é descartado, T é desempilhado e o ')' fecha o parêntese normalmente.

#### ESTILO FORMAL (Definição de prova): "Tratamento de erros sintáticos: definição de prova"
- TEXTO: **Objetivos do tratador de erros** (Dragão, 4.1.3): (1) relatar a presença de erros de forma clara e precisa; (2) recuperar-se de cada erro rápido o bastante para detectar erros subsequentes; (3) acrescentar custo mínimo ao processamento de programas corretos.
- TEXTO: **Propriedade do prefixo viável**: os métodos LL e LR detectam um erro assim que o prefixo da entrada já lido não pode ser completado para formar uma cadeia da linguagem. O erro é acusado no PRIMEIRO token que torna isso impossível.
- PASSOS (Estratégias de recuperação):
    1) **Modo pânico**: descartar símbolos da entrada, um a um, até encontrar um **token de sincronização** de um conjunto escolhido (ex.: `;`, `}`, FOLLOW(A)). Simples e livre de laços infinitos.
    2) **Nível de frase**: correção local do restante da entrada, inserir, remover ou substituir um token (ex.: inserir `;` que falta). Risco de laço se sempre inserir antes do token atual.
    3) **Produções de erro**: aumentar a gramática com produções que geram as construções erradas mais comuns, emitindo diagnósticos específicos.
    4) **Correção global**: achar a cadeia correta y mais próxima da entrada x (mínimo de inserções, remoções e trocas). Interesse teórico: custo alto demais.
- CÓDIGO (Modo pânico no analisador LL(1) (Dragão, 4.4.5)):
```
com A (não-terminal) no topo e a = próximo token:
    se M[A, a] tem produção: expanda normalmente
    senão:  // erro
        relate o erro (linha, esperado × encontrado)
        enquanto a ∉ FIRST(A) e a ∉ FOLLOW(A) ∪ {$}:
            descarte a; a ← próximo token
        se a ∈ FIRST(A): continue com A no topo
        senão: desempilhe A   // sincronizou em FOLLOW(A)
com terminal t no topo e t ≠ a:
    relate "esperava t"; desempilhe t  // finge que t estava lá
```
- WIDGET LL1_TABLE gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» legenda: Toda célula vazia desta tabela é um ponto de erro. As colunas de FOLLOW(A) são as 'synch' de cada linha.
- PREVEJA: Na tabela do livro, uma célula M[A, a] marcada 'synch' significa:
    opções: Descartar a | ✔Desempilhar A, pois a ∈ FOLLOW(A) | Aceitar a entrada | Expandir A → ε
    explicação: Célula em branco → descarta o token. 'synch' → desempilha A. Terminal no topo diferente do token → desempilha o terminal.
- TEXTO: **No Yacc/Bison**: o token reservado `error` pode aparecer em produções, como `comando : error ';'`. Ao detectar um erro, o parser chama **yyerror**, desempilha estados até um que tenha ação para `error`, empilha `error` e descarta tokens até um que possa segui-lo. Para não gerar cascata, só relata o próximo erro depois de 3 tokens empilhados com sucesso; **yyerrok** encerra esse modo imediatamente e **yyclearin** descarta o token atual.
- PREVEJA: Qual NÃO é objetivo do tratador de erros?
    opções: Relatar erros com clareza | Detectar erros subsequentes | ✔Corrigir o programa automaticamente para que ele rode | Custo mínimo em programas corretos
    explicação: Recuperar ≠ corrigir. A recuperação serve para continuar a análise e achar mais erros; o código não é gerado.
- CAIXA [Dica de prova]: Cai muito: 'cite e explique as estratégias de recuperação de erros'. Escreva as quatro, com uma vantagem e uma desvantagem de cada, e diga que o modo pânico usa FOLLOW (e FIRST) como sincronizadores. Cite a propriedade do prefixo viável.
- REVELAR: Defina propriedade do prefixo viável em uma frase.
    → O analisador detecta o erro assim que o prefixo lido da entrada deixa de ser prefixo de alguma sentença da linguagem, nunca depois disso.

#### ESTILO MNEMONIC (Truque de memória): "FIRST Fica, FOLLOW Foge"
- CAIXA [Gancho de memória]: **FIRST Fica, FOLLOW Foge.** Achou um token de FIRST(A)? A FICA no topo e recomeça. Achou um de FOLLOW(A)? A FOGE (é desempilhado). Nenhum dos dois? Joga o token fora.
- CAIXA [Gancho de memória]: **Terminal teimoso? Finge que estava lá.** Terminal no topo que não bate com a entrada é desempilhado, como pular uma vírgula esquecida.
- TEXTO: **Palácio da memória**: no Templo Top-Down, atrás do altar da tabela LL(1), há uma porta com luz vermelha: a **Sala do Pânico** . O Pilha corre por ela jogando tokens pela janela até ver uma placa de saída, `;` ou `}`: pintada com as cores do FOLLOW.
- CAIXA [Gancho de memória]: Tipos de erro em zoom crescente: **Letra → Frase → Sentido → Resultado**. Léxico (palavra que não existe), sintático (frase mal montada), semântico (frase sem sentido), lógico (tudo compila, mas o resultado sai errado).
- CAIXA [Gancho de memória]: Estratégias, da mais simples à mais ambiciosa: **P**ânico **P**ula, **F**rase **F**az remendo, **P**rodução **P**revê, **G**lobal é **G**ênio, mas ninguém paga o salário dele.
- PREVEJA: E no topo, token 'x' na entrada, x ∉ FIRST(E) e x ∉ FOLLOW(E). O que fazer?
    opções: Desempilhar E | ✔Descartar x e olhar o próximo token | Aceitar | Empilhar x
    explicação: Nem fica, nem foge: o token vai pela janela. Repita até achar alguém de FIRST (fica) ou de FOLLOW (foge).
- WIDGET PANIC gramática=«P -> S ; P | ε ⏎ S -> id = E ⏎ E -> id | num» entrada=«id = num ; = = ; id = id ;» legenda: Recite enquanto avança: o lixo '= = ;' vai pela janela até aparecer 'id', que está em FIRST(P). P fica!
- CAIXA [Gancho de memória]: **Prefixo viável = grito imediato**: LL e LR gritam no PRIMEIRO token impossível, nunca depois. Se a mensagem aponta a linha 10, o erro real está na linha 10 ou ANTES.
- CAIXA [Gancho de memória]: Bison: **`error` é o coringa** que casa com o lixo; **yyerrok** é o 'tá ok, pode reclamar de novo'; **yyerror** é o megafone que imprime a mensagem.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que recuperar = corrigir o programa; (2) usar FIRST como conjunto de 'desempilhar' (é FOLLOW!); (3) esquecer o $ entre os sincronizadores; (4) confiar cegamente na 2ª, 3ª… mensagem, pode ser cascata.
- REVELAR: Recite: o que fazer com A no topo e célula vazia?
    → Descarta tokens até achar um de FIRST(A), A fica, ou de FOLLOW(A) ∪ {$}, A foge (desempilha). FIRST Fica, FOLLOW Foge.
- CAIXA [No mundo real]: Mensagem boa de verdade tem linha e 'esperado × encontrado': 'Erro sintatico na linha 7: esperava ';' mas encontrou 'id''. É o que o professor quer ver no seu TP.
