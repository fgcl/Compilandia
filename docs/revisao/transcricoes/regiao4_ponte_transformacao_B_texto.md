# Região 4: Ponte da Transformação — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO LEFT_RECURSION: Recursão à esquerda

#### ESTILO STORY (História / Analogia): "O cachorro que corre atrás do rabo"
- NARRADOR: Repartição pública. Placa na parede: 'Para tirar o documento A, apresente o documento A.'
- BYTE: Mas… isso é impossível! Eu nunca saio do lugar!
- PROF: Pois é exatamente isso que acontece com um analisador **descendente** (top-down) diante de `E -> E + T`.
- TEXTO: Na descida recursiva, cada não-terminal vira uma função. A função E() tenta a produção E → E + T: 'para reconhecer E, primeiro reconheça E'. Ela chama E()… que chama E()… que chama E()…
- CÓDIGO (Laço infinito → estouro de pilha):
```
void E() {
    E();          // chama a si mesma sem consumir NADA
    match('+');
    T();
}
```
- PILHA: E()… E()… E()… E()… Eu… só… vejo… o… topo… e… ele… não… para… de… crescer… ESTOUREI.
- CAIXA [Cuidado!]: O problema não é ter recursão, é a recursão ser a PRIMEIRA coisa, antes de consumir qualquer token. Cachorro correndo atrás do rabo: gira, gira e não sai do lugar.
- BYTE: E como sai do ciclo?
- PROF: Olha o que `E -> E + T | T` realmente gera: um T e depois quantos '+ T' quiser. T + T + T… Então começamos pelo T (a parte que NÃO é recursiva) e deixamos a repetição para o final.
- CÓDIGO (Mesma linguagem, recursão agora à DIREITA):
```
E  -> T E'
E' -> + T E' | ε
```
- TEXTO: É como consertar a placa da repartição: 'Apresente o RG. Depois, se quiser, mais documentos.' Primeiro algo concreto; depois a repetição.
- WIDGET LEFT_RECURSION gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Antes e depois: E e T ganharam os ajudantes E' e T'.
- PREVEJA: A gramática nova gera a mesma linguagem?
    opções: ✔Sim, as mesmas cadeias | Não, gera menos cadeias
    explicação: As duas geram T (+ T)*. Muda só o formato da árvore.
- CAIXA [No mundo real]: Analisadores LR (Yacc/Bison) adoram recursão à esquerda, ela até economiza pilha. O problema é só dos descendentes (LL, descida recursiva).
- REVELAR: Por que recursão à esquerda trava o analisador descendente?
    → Porque a função de A chama A de novo antes de consumir qualquer token: a entrada não avança e a recursão nunca termina.

#### ESTILO VISUAL (Passo a passo visual): "A cirurgia ao vivo"
- PROF: Vamos operar. O widget mostra a gramática antes e depois.
- WIDGET LEFT_RECURSION gramática=«S -> S a | b» legenda: S -> S a | b vira S -> b S' e S' -> a S' | ε.
- TEXTO: Leia assim: α = `a` (o que vem DEPOIS do S recursivo) e β = `b` (a alternativa que NÃO começa com S). A linguagem é b a a a… = βα*.
- WIDGET DERIVATION gramática=«S -> S a | b» entrada=«b a a» legenda: Gramática original: a árvore cresce para a ESQUERDA.
- WIDGET DERIVATION gramática=«S -> b S' ⏎ S' -> a S' | ε» entrada=«b a a» legenda: Gramática nova: a árvore cresce para a DIREITA.
- PREVEJA: Qual das duas um analisador descendente consegue usar?
    opções: A original | ✔A nova
    explicação: Na nova, S começa consumindo b, e S' decide pelo próximo token: 'a' → continua; fim → ε.
- WIDGET LEFT_RECURSION gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Agora com duas recursões: E e T.
- TEXTO: Repare: F não mudou, ela não tinha recursão à esquerda. A cirurgia só mexe em quem precisa.
- WIDGET LL1_TABLE gramática=«E -> E + T | T ⏎ T -> T * F | F ⏎ F -> ( E ) | id» legenda: Tabela LL(1) da gramática ORIGINAL: veja as células com conflito.
- WIDGET LL1_TABLE gramática=«E -> T E' ⏎ E' -> + T E' | ε ⏎ T -> F T' ⏎ T' -> * F T' | ε ⏎ F -> ( E ) | id» legenda: Tabela da gramática transformada: uma produção por célula. LL(1)!
- REVELAR: Por que a tabela original tinha conflito?
    → E → E + T e E → T começam, ambas, com algo que deriva id ou '('. Olhando só o próximo token, não dá para escolher. Recursão à esquerda SEMPRE causa esse conflito.
- CAIXA [Não existem perguntas idiotas]: P: O ε em E' é obrigatório? R: Sim! É ele que encerra a repetição. Sem o ε, E' nunca terminaria de gerar.

#### ESTILO WORKED (Exemplo resolvido): "Eliminando passo a passo"
- PROF: Receita: A → Aα₁ | … | Aαₘ | β₁ | … | βₙ vira A → β₁A' | … | βₙA' e A' → α₁A' | … | αₘA' | ε.
- CÓDIGO (Exercício resolvido):
```
E -> E + T | E - T | T
```
- PASSOS (Resolvido):
    1) 1. Separe as alternativas que começam com E (recursivas) das outras.
    2) 2. Recursivas: E + T e E - T → α₁ = `+ T`, α₂ = `- T`.
    3) 3. Não recursiva: T → β₁ = `T`.
    4) 4. Novo E: cada β seguido de E' → E → T E'.
    5) 5. Novo E': cada α seguido de E', mais ε → E' → + T E' | - T E' | ε.
- WIDGET LEFT_RECURSION gramática=«E -> E + T | E - T | T ⏎ T -> id» legenda: Confira com o motor.
- TEXTO: Agora esmaecido (exemplo do Livro do Dragão): `A -> A c | A a d | b d | ε`.
- REVELAR: Quem são os α's?
    → α₁ = `c` e α₂ = `a d`: o que vem DEPOIS do A nas alternativas recursivas.
- REVELAR: Quem são os β's? (Atenção ao ε!)
    → β₁ = `b d` e β₂ = `ε`. A alternativa vazia também é um β: ela não começa com A.
- REVELAR: Escreva a gramática final.
    → A → b d A' | A'
A' → c A' | a d A' | ε
- WIDGET LEFT_RECURSION gramática=«A -> A c | A a d | b d | ε» legenda: Confira: o β = ε virou A → A'.
- CAIXA [Cuidado!]: Erro nº 1: esquecer o `| ε` em A'. Erro nº 2: esquecer de colocar A' no fim de CADA β.
- CAIXA [Dica de prova]: Se a questão pedir 'elimine a recursão à esquerda e mostre que a gramática resultante é LL(1)', faça a transformação E depois a tabela. Os pontos costumam vir das duas partes.

#### ESTILO SOCRATIC (Perguntas guiadas): "Descubra a receita sozinho"
- CÓDIGO:
```
S -> S a | b
```
- PREVEJA: Quais cadeias essa gramática gera?
    opções: ✔b, ba, baa, baaa… | a, ab, aab… | ab, aabb, aaabbb…
    explicação: S ⇒ S a ⇒ S a a ⇒ b a a: o b sempre fica na ponta esquerda, e os a's se acumulam à direita.
- REVELAR: Escreva essa linguagem como expressão regular.
    → `ba*`: um b e depois quantos a's quiser. Em geral, A → Aα | β gera βα*.
- PREVEJA: Um analisador descendente lendo `b a a` precisa escolher a primeira produção de S. Olhando só o 'b', ele sabe quantas vezes usar S → S a?
    opções: Sim | ✔Não: teria que adivinhar o futuro (quantos a's virão)
    explicação: A quantidade de S → S a depende de quantos a's vêm DEPOIS. Com um token de lookahead, impossível saber.
- PROF: Então vamos gerar βα* de outro jeito: primeiro o β, depois um 'gerador de α's'.
- PREVEJA: Qual gramática gera b a* sem recursão à esquerda?
    opções: ✔S -> b S'  e  S' -> a S' | ε | S -> S' b  e  S' -> a S' | ε | S -> a S | b
    explicação: A segunda e a terceira geram a*b (o b no FIM). Só a primeira põe o b na frente e repete os a's depois.
- REVELAR: Generalize: o que fazer com A → Aα | β?
    → A → β A' e A' → α A' | ε.
- PREVEJA: `S -> A a | b` e `A -> S c | d` têm recursão à esquerda?
    opções: Não: nenhuma produção começa com o próprio lado esquerdo | ✔Sim, indireta: S ⇒ A a ⇒ S c a
    explicação: Recursão à esquerda INDIRETA: S ⇒⁺ S… A receita imediata não basta; primeiro é preciso substituir para torná-la imediata.
- REVELAR: Como eliminar essa recursão indireta?
    → Ordene S, A. Em A → S c, substitua S pelas suas alternativas: A → A a c | b c | d. Agora é imediata: A → b c A' | d A'; A' → a c A' | ε.
- WIDGET LEFT_RECURSION gramática=«S -> A a | b ⏎ A -> A a c | b c | d» legenda: O último passo (recursão já imediata), feito pelo motor.

#### ESTILO FORMAL (Definição de prova): "Algoritmo de eliminação"
- TEXTO: **Definição.** Uma gramática é **recursiva à esquerda** se tem um não-terminal A tal que A ⇒⁺ Aα para alguma cadeia α. A recursão é **imediata** quando existe uma produção A → Aα.
- TEXTO: Métodos de análise descendente não conseguem tratar gramáticas recursivas à esquerda; é necessária uma transformação que elimine a recursão.
- CÓDIGO:
```
Recursão imediata:
  A → Aα₁ | Aα₂ | ... | Aαₘ | β₁ | β₂ | ... | βₙ
  (nenhum βᵢ começa com A)
substituir por:
  A  → β₁A' | β₂A' | ... | βₙA'
  A' → α₁A' | α₂A' | ... | αₘA' | ε
```
- CÓDIGO (Algoritmo 4.19 (Livro do Dragão)):
```
Entrada: G sem ciclos (A ⇒⁺ A) e sem produções-ε
ordene os não-terminais A₁, A₂, ..., Aₙ
para i ← 1 até n:
    para j ← 1 até i − 1:
        substitua cada Aᵢ → Aⱼγ por Aᵢ → δ₁γ | δ₂γ | ... | δₖγ,
        onde Aⱼ → δ₁ | δ₂ | ... | δₖ são as produções atuais de Aⱼ
    elimine a recursão imediata das produções de Aᵢ
```
- CAIXA [Cuidado!]: Pré-condição: sem ciclos e sem produções-ε. (O algoritmo pode até funcionar com ε, mas sem garantia.) Na prova, cite isso!
- PREVEJA: Ao final do algoritmo, toda produção Aᵢ → Aⱼγ satisfaz…
    opções: j < i | ✔j > i | j = i
    explicação: Depois da iteração i, qualquer Aᵢ → Aⱼγ tem j > i. Os índices só sobem, nenhum ciclo de recursão à esquerda sobrevive.
- TEXTO: Exemplo do livro: S → A a | b, A → A c | S d | ε. Para A, substitua S em A → S d: A → A c | A a d | b d | ε. Eliminando a imediata: A → b d A' | A', A' → c A' | a d A' | ε.
- WIDGET LEFT_RECURSION gramática=«S -> A a | b ⏎ A -> A c | A a d | b d | ε» legenda: O resultado do exemplo (depois da substituição).
- CAIXA [Dica de prova]: Duas armadilhas: (1) a nova gramática gera a MESMA linguagem, mas as árvores mudam (a associatividade fica 'escondida' em A' e é recuperada com atributos herdados); (2) recursão à esquerda NÃO é ambiguidade.
- REVELAR: Por que exigir que nenhum βᵢ comece com A?
    → Porque se βᵢ começasse com A, A → βᵢA' continuaria recursiva à esquerda, a transformação não resolveria nada.

#### ESTILO MNEMONIC (Truque de memória): "Beta começa, alfa repete, épsilon encerra"
- CAIXA [Gancho de memória]: **Beta começa, alfa repete, épsilon encerra.** A → β A'; A' → α A' | ε.
- CAIXA [Gancho de memória]: **Rabo à esquerda morde**: em A → **A**α, o A morde o próprio rabo logo de cara. Recursão à DIREITA (A → αA) é inofensiva: primeiro come α (consome tokens), depois chama.
- TEXTO: β vem 'antes': é o começo da cadeia, a parte não recursiva. α é o refrão que se repete. Linguagem: β α α α… = βα*.
- CAIXA [Gancho de memória]: **O A-linha é um while disfarçado**: enquanto aparecer α, repete; quando não, ε e sai do laço.
- TEXTO: **Palácio da memória**: na cabeceira da **Ponte da Transformação**, um cachorro corre atrás do rabo (A → Aα). O guarda da ponte só deixa passar quem mostra o β primeiro. No meio da ponte, um carrossel (A') gira α, α, α… até alguém gritar ε.
- PREVEJA: Elimine a recursão de `L -> L , id | id`.
    opções: ✔L -> id L'  e  L' -> , id L' | ε | L -> , id L'  e  L' -> id L' | ε | L -> id , L | id
    explicação: β = `id` começa; α = `, id` repete; ε encerra. (A terceira até gera a mesma linguagem, mas precisaria de fatoração para ser LL(1).)
- WIDGET LEFT_RECURSION gramática=«L -> L , id | id» legenda: Lista de identificadores separados por vírgula.
- CAIXA [Cuidado!]: Confusões clássicas: (1) trocar α e β; (2) esquecer o ε; (3) achar que só existe recursão imediata (há a INDIRETA: S → A a, A → S b); (4) achar que LR também precisa eliminar, não precisa!
- CAIXA [Gancho de memória]: **LL** = lê da **L**eft, derivação mais à **L**eft, e **L**eft recursion é o inimigo nº 1 do LL.
- REVELAR: Recite a receita aplicada a E → E + T | T.
    → Beta começa: E → T E'. Alfa repete, épsilon encerra: E' → + T E' | ε.


## ASSUNTO LEFT_FACTORING: Fatoração à esquerda

#### ESTILO STORY (História / Analogia): "O menu do telefone do banco"
- NARRADOR: Você liga para o banco. A voz robótica: 'Para segunda via do boleto, diga BOLETO SEGUNDA VIA. Para boleto vencido, diga BOLETO VENCIDO…'
- BYTE: Eu digo 'boleto…' e o robô fica perdido!
- PROF: Pois é. Ouvindo só 'boleto', o sistema ainda não sabe qual opção você quer. Ele precisa ADIAR a decisão até ouvir mais.
- TEXTO: Um analisador preditivo tem o mesmo problema: olhando só o próximo token, precisa escolher UMA produção. Se duas alternativas começam igual, ele trava.
- CÓDIGO (As duas começam com 'if E then S', qual escolher ao ver 'if'?):
```
S -> if E then S
   | if E then S else S
```
- PROF: Solução da URA esperta: 'Diga BOLETO'. Depois, um submenu: 'segunda via ou vencido?'. Junta o pedaço comum e decide depois.
- CÓDIGO (Prefixo comum na frente; as diferenças vão para S'):
```
S  -> if E then S S'
S' -> else S | ε
```
- CAIXA [Gancho de memória]: Lembra da escola? ab + ac = a(b + c). **Colocar em evidência!** Fatoração à esquerda é exatamente isso, com o prefixo comum.
- WIDGET LEFT_FACTORING gramática=«S -> i E t S | i E t S e S | a ⏎ E -> b» legenda: i = if, t = then, e = else. O prefixo comum 'i E t S' foi para a frente.
- PREVEJA: Depois da fatoração, ao ver 'if', o analisador sabe qual produção de S usar?
    opções: ✔Sim: só uma alternativa de S começa com if | Não
    explicação: Agora há uma única alternativa com if. A dúvida 'tem else ou não?' fica para S', quando o token else (ou outro) já estiver à vista.
- CAIXA [Não existem perguntas idiotas]: P: A fatoração muda a linguagem? R: Não. Só reorganiza as produções, assim como a(b + c) = ab + ac.
- CAIXA [Cuidado!]: Fatorar NÃO tira ambiguidade. O else pendurado continua ambíguo: ao ver else, S' ainda poderia escolher 'else S' ou ε. Casar com o if mais próximo continua sendo uma regra extra.
- REVELAR: Na analogia, o que é o 'submenu'?
    → O novo não-terminal (S', A'…): guarda os 'restos' depois do prefixo comum, e a decisão fica para depois.

#### ESTILO VISUAL (Passo a passo visual): "Fatorando ao vivo"
- PROF: Olhe o antes e o depois. Primeiro, o clássico do else.
- WIDGET LEFT_FACTORING gramática=«S -> i E t S | i E t S e S | a ⏎ E -> b»
- TEXTO: O motor procurou o MAIOR prefixo comum entre as alternativas que começam com o mesmo símbolo: `i E t S`. O que sobrou (ε e `e S`) foi para S'.
- WIDGET DERIVATION gramática=«S -> i E t S S' | a ⏎ S' -> e S | ε ⏎ E -> b» entrada=«i b t a e a» legenda: Derivando com a gramática fatorada.
- WIDGET LL1_TABLE gramática=«S -> i E t S | i E t S e S | a ⏎ E -> b» legenda: Antes: duas produções disputando a célula [S, i].
- WIDGET LL1_TABLE gramática=«S -> i E t S S' | a ⏎ S' -> e S | ε ⏎ E -> b» legenda: Depois: [S, i] resolvida… mas olhe [S', e]! A ambiguidade do else continua.
- PREVEJA: Por que [S', e] tem conflito?
    opções: ✔Porque e ∈ FIRST(e S) e também e ∈ FOLLOW(S') | Porque a fatoração foi feita errado
    explicação: S' → e S entra pela coluna e (FIRST), e S' → ε entra pelas colunas de FOLLOW(S'), que inclui e. É a ambiguidade, não a fatoração.
- WIDGET LEFT_FACTORING gramática=«L -> S ; L | S ⏎ S -> id = E | print E ⏎ E -> id | num» legenda: Lista de comandos: L -> S ; L | S também precisa de fatoração.
- TEXTO: Resultado: L → S L' e L' → ; L | ε. Leia: 'um comando e, talvez, um ponto e vírgula com mais comandos'.
- REVELAR: Quando o analisador escolhe L' → ε?
    → Quando o próximo token NÃO é ';', quando ele está em FOLLOW(L'), como o fim da entrada.
- WIDGET LL1_TABLE gramática=«L -> S L' ⏎ L' -> ; L | ε ⏎ S -> id = E | print E ⏎ E -> id | num» legenda: Sem conflitos: agora é LL(1).

#### ESTILO WORKED (Exemplo resolvido): "Fatorando passo a passo"
- PROF: Receita: para cada A, ache o MAIOR prefixo α comum a duas ou mais alternativas. Se α ≠ ε: A → αβ₁ | … | αβₙ | γ vira A → αA' | γ e A' → β₁ | … | βₙ.
- CÓDIGO (Exercício resolvido):
```
A -> a b c | a b d | a e | f
```
- PASSOS (Resolvido):
    1) 1. Alternativas que começam com `a`: a b c, a b d, a e. (f fica de fora: é o γ.)
    2) 2. Maior prefixo comum às TRÊS: só `a` (a e não tem b).
    3) 3. Primeira rodada: A → a A' | f; A' → b c | b d | e.
    4) 4. Repita! Em A', `b c` e `b d` compartilham `b`.
    5) 5. Segunda rodada: A' → b A'' | e; A'' → c | d.
    6) 6. Fim: nenhum par de alternativas do mesmo NT começa igual.
- WIDGET LEFT_FACTORING gramática=«A -> a b c | a b d | a e | f» legenda: O motor repete as rodadas até estabilizar.
- TEXTO: Sua vez, esmaecido: `C -> id = E | id ( ) | id ( E )`, com `E -> num`.
- REVELAR: Primeira rodada: qual o maior prefixo comum às três?
    → Só `id` (o segundo símbolo é `=` numa e `(` nas outras). C → id C'; C' → = E | ( ) | ( E ).
- REVELAR: Segunda rodada, em C'?
    → `( )` e `( E )` compartilham `(`. C' → = E | ( C''; C'' → ) | E ).
- WIDGET LEFT_FACTORING gramática=«C -> id = E | id ( ) | id ( E ) ⏎ E -> num» legenda: Confira.
- CAIXA [Cuidado!]: Não pare na primeira rodada! Depois de fatorar, olhe de novo o novo não-terminal.
- CAIXA [Dica de prova]: Use o MAIOR prefixo comum a pelo menos duas alternativas. Prefixo menor não é erro, só dá mais rodadas; prefixo que não é comum a todas do grupo, aí sim é erro.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que adiar a decisão?"
- CÓDIGO:
```
A -> a b | a c
```
- PREVEJA: Um analisador preditivo com 1 token de lookahead vê 'a'. Qual produção ele escolhe?
    opções: A -> a b | A -> a c | ✔Ainda não dá para saber
    explicação: As duas começam com a. Só o SEGUNDO token decide, e ele ainda não está visível.
- PREVEJA: E se ele pudesse olhar 2 tokens à frente?
    opções: Ainda não daria | ✔Daria: o 2º token (b ou c) decide
    explicação: Isso seria LL(2). Mas queremos LL(1), mais simples e rápido. Em vez de olhar mais longe, mudamos a gramática.
- REVELAR: Como reescrever A para decidir só depois de consumir o 'a'?
    → A → a A'; A' → b | c. Ao ver 'a' há uma única opção; b ou c é decidido em A', com o token certo à vista.
- PREVEJA: Em `A -> a B | a B c`, qual o prefixo comum?
    opções: a | ✔a B | a B c
    explicação: As duas começam com `a B`; a segunda continua com c, a primeira acaba.
- REVELAR: Fatore.
    → A → a B A'; A' → c | ε. (O ε aparece porque uma alternativa era exatamente o prefixo.)
- PREVEJA: Esta gramática tem prefixo comum? `S -> A x | B y`, `A -> c`, `B -> c`
    opções: Não: A e B são diferentes | ✔Sim, escondido: depois de expandir, as duas começam com c
    explicação: A fatoração 'de olho' só vê símbolos iguais escritos. Aqui FIRST(A x) ∩ FIRST(B y) = {c}: conflito LL(1) mesmo sem prefixo literal. Seria preciso substituir A e B primeiro.
- WIDGET LL1_TABLE gramática=«S -> A x | B y ⏎ A -> c ⏎ B -> c» legenda: Conflito em [S, c] mesmo sem prefixo literal comum.
- REVELAR: Qual a relação entre fatoração à esquerda e LL(1)?
    → A fatoração remove um tipo de conflito (alternativas com o mesmo início), adiando a escolha até haver token suficiente. Junto com eliminar recursão à esquerda, é a transformação típica para tentar deixar uma gramática LL(1).

#### ESTILO FORMAL (Definição de prova): "Algoritmo de fatoração à esquerda"
- TEXTO: **Fatoração à esquerda** é uma transformação de gramática útil para produzir gramáticas adequadas à análise preditiva (descendente). Quando a escolha entre duas alternativas de A não está clara, reescrevemos as produções de A para adiar a decisão até ter visto entrada suficiente.
- CÓDIGO (Algoritmo 4.21 (Livro do Dragão)):
```
Entrada: gramática G.  Saída: gramática equivalente fatorada.
para cada não-terminal A:
    encontre o MAIOR prefixo α comum a duas ou mais alternativas
    se α ≠ ε:
        substitua  A → αβ₁ | αβ₂ | ... | αβₙ | γ
        por        A  → αA' | γ
                   A' → β₁ | β₂ | ... | βₙ
        (γ = alternativas que não começam com α; A' é novo)
repita até que nenhum par de alternativas de um NT tenha prefixo comum
```
- TEXTO: Exemplo do livro: S → i E t S | i E t S e S | a, E → b. Resultado: S → i E t S S' | a, S' → e S | ε, E → b.
- WIDGET LEFT_FACTORING gramática=«S -> i E t S | i E t S e S | a ⏎ E -> b»
- PREVEJA: Fatorar `A -> α β1 | α β2` preserva a linguagem?
    opções: ✔Sim, sempre | Não, perde cadeias
    explicação: αA' com A' → β₁ | β₂ gera exatamente αβ₁ e αβ₂, é a propriedade distributiva.
- PREVEJA: Uma gramática fatorada e sem recursão à esquerda é necessariamente LL(1)?
    opções: Sim | ✔Não
    explicação: São condições necessárias, não suficientes. O else pendurado fatorado ainda tem conflito em [S', e]; e uma gramática ambígua nunca é LL(1).
- CAIXA [Dica de prova]: Ordem segura numa questão 'torne LL(1)': (1) elimine a recursão à esquerda; (2) fatore à esquerda; (3) calcule FIRST e FOLLOW; (4) monte a tabela e verifique se alguma célula tem mais de uma produção.
- CAIXA [Cuidado!]: Não confunda: fatoração trata PREFIXOS COMUNS (A → αβ₁ | αβ₂); recursão à esquerda trata A → Aα. Problemas diferentes, receitas diferentes, e ambos atrapalham o LL(1).
- REVELAR: Escreva a forma geral da fatoração.
    → A → αβ₁ | … | αβₙ | γ  ⟹  A → αA' | γ  e  A' → β₁ | … | βₙ.

#### ESTILO MNEMONIC (Truque de memória): "Coloque em evidência"
- CAIXA [Gancho de memória]: **ab + ac = a(b + c)**: fatoração à esquerda é o 'colocar em evidência' do ensino fundamental, com o prefixo comum no papel do a.
- CAIXA [Gancho de memória]: **Primeiro o que é igual, depois o que difere.** A → (parte igual) A'; A' → (as diferenças).
- CAIXA [Gancho de memória]: **Recursão = rabo; fatoração = gêmeos.** Na recursão à esquerda, o NT aparece no início do próprio lado direito. Na fatoração, duas alternativas nascem com a mesma cara.
- TEXTO: **Palácio da memória**: do outro lado da **Ponte da Transformação**, longe do cachorro, há uma bifurcação com duas placas idênticas 'if E then S…'. O engenheiro junta as duas estradas num trecho único e só coloca a bifurcação depois (S'), onde as placas ficam diferentes.
- PREVEJA: Qual gramática PRECISA de fatoração (e não de eliminar recursão à esquerda)?
    opções: E -> E + T | T | ✔S -> id ( ) | id = num | S -> a S b | ε
    explicação: As duas alternativas de S começam com id: gêmeos! A primeira opção é rabo (recursão); a terceira já está boa.
- WIDGET LEFT_FACTORING gramática=«S -> id ( ) | id = num» legenda: Gêmeos separados: id em evidência.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que fatorar resolve ambiguidade (não resolve!); (2) parar na primeira rodada; (3) usar um prefixo que não é comum a todas as alternativas do grupo; (4) esquecer o ε quando uma alternativa é igual ao prefixo (A → αβ | α vira A' → β | ε).
- CAIXA [Gancho de memória]: As duas cirurgias da Ponte antes do Templo LL(1): **'Rabo e Gêmeos'**: corte o rabo (recursão à esquerda) e separe os gêmeos (fatoração).
- REVELAR: Fatore `A -> x y z | x y` em voz alta.
    → Prefixo comum `x y`: A → x y A'; A' → z | ε.
