# Região 2: Floresta Léxica — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO TOKENS: Tokens, lexemas e padrões

#### ESTILO STORY (História / Analogia): "O caixa do supermercado"
- NARRADOR: Supermercado em dia de promoção. A Lexi está no caixa, passando os produtos da esteira um por um.
- LEXI: Bip! 'Guaraná 2L'. Bip! 'Pão francês'. Bip! 'Guaraná lata'. Pra mim, cada produto vai para uma CATEGORIA no cupom fiscal.
- PROF: Repare em três coisas diferentes: o produto concreto na mão dela, a categoria impressa no cupom e a regra que diz qual produto vai em qual categoria.
- PASSOS (Supermercado → compilador):
    1) O produto concreto ('Guaraná 2L') → **lexema**: o pedaço de texto real do programa, como `contador` ou `42`.
    2) A categoria no cupom (BEBIDA) → **token**: o nome da classe, como `id` ou `num`.
    3) A regra ('todo líquido engarrafado é BEBIDA') → **padrão**: a descrição de quais lexemas pertencem ao token, como 'letra seguida de letras ou dígitos'.
- BYTE: Então 'Guaraná 2L' e 'Guaraná lata' são lexemas diferentes do MESMO token?
- PROF: Exatamente! Como `x` e `total`: lexemas diferentes, mesmo token `id`.
- TEXTO: Mas no cupom a categoria sozinha não basta: precisa saber QUAL bebida foi. Por isso o token carrega um **atributo**: ⟨id, ponteiro para `total` na tabela de símbolos⟩.
- PREVEJA: Em `if (x >= 10)`, qual é o LEXEMA do token de comparação?
    opções: relop (operador relacional) | ✔>= | x >= 10 | if
    explicação: `>=` é o texto real (lexema). 'relop' é o nome do token, a categoria.
- LEXI: E tem mais: eu sempre pego o MAIOR pedaço possível. Vendo `>=`, não corto em `>` e `=`. É como ler 'guarda-chuva' como uma palavra só, e não 'guarda' + 'chuva'.
- CAIXA [Não existem perguntas idiotas]: P: E o `if`? Ele também casa com 'letras seguidas de letras', o padrão de identificador! R: Empate. Palavras-chave são **reservadas**: a regra delas tem prioridade. Já `iff` é mais comprido que `if`, então pelo maior casamento vira identificador.
- WIDGET TOKENIZE entrada=«if (iff >= 10) whilex = whilex + 1;» legenda: Repare: `iff` e `whilex` são identificadores, não palavras-chave!
- CAIXA [No mundo real]: Por isso em C você não pode chamar uma variável de `while`, mas pode chamar de `whilex`.
- REVELAR: Na analogia, quem é lexema, quem é token e quem é padrão?
    → Lexema = o produto concreto (o texto). Token = a categoria no cupom (a classe). Padrão = a regra que decide a categoria.

#### ESTILO VISUAL (Passo a passo visual): "Fatiando código em cores"
- LEXI: Olha a minha katana em ação. Cada cor é um token diferente.
- WIDGET TOKENIZE entrada=«while (i <= n1) i++;» legenda: Toque em cada token para ver o lexema e a classe.
- TEXTO: Repare em três coisas: (1) os espaços desaparecem; (2) `<=` é UM token; (3) `i++` vira dois tokens, `i` e `++`.
- PREVEJA: Quantos tokens tem `x+=y;`?
    opções: 3 | ✔4 | 5 | 6
    explicação: x, +=, y e ;: 4 tokens. Mesmo sem espaços, o maior casamento pega `+=` inteiro.
- WIDGET TOKENIZE entrada=«x+=y;» legenda: Sem espaços, o léxico ainda sabe onde cortar.
- PROF: Mas como a Lexi SABE que `n1` é identificador? Ela segue um padrão, e padrão se desenha como autômato.
- WIDGET DFA entrada=«l(l|d)*» legenda: Padrão de identificador (l = letra, d = dígito). Teste 'ld', 'll' e 'dl'.
- BYTE: 'dl' foi rejeitada! Faz sentido: identificador não começa com dígito.
- WIDGET DFA entrada=«d+(pd+)?» legenda: Padrão de número (d = dígito, p = ponto). Teste 'dpd', 'd' e 'dp'.
- CAIXA [Cuidado!]: Em 'dp' (como `3.`) o autômato termina num estado NÃO final. Então o maior casamento válido é só `3`; o ponto sobra para o próximo token.
- CAIXA [Poder do cérebro]: Como a Lexi corta `x = a+++b;`? Pense no maior casamento antes de tocar.
- REVELAR: Resposta
    → x, =, a, ++, +, b, ; — ela morde `++` primeiro (o maior), e sobra um `+`.
- WIDGET TOKENIZE entrada=«x = a+++b;» legenda: Confira a mordida gulosa.
- REVELAR: Por que o token id carrega um ponteiro para a tabela de símbolos?
    → O sintático só precisa saber que é um `id`, mas as fases seguintes precisam saber QUAL: nome, tipo, escopo. O atributo aponta para essa entrada.

#### ESTILO WORKED (Exemplo resolvido): "Montando a tabela de tokens"
- PROF: Vamos montar a tabela lexema / token / atributo de uma linha, com calma.
- CÓDIGO (Entrada):
```
if (x >= 10) total += x;
```
- PASSOS (Resolvido):
    1) `if` → palavra-chave **if**. Também casa com id, mas palavra reservada vence. Sem atributo.
    2) `(` → delimitador **(**.
    3) `x` → **id**, atributo: ponteiro para x na tabela de símbolos.
    4) `>=` → **relop**, atributo GE. Maior casamento: não é `>` + `=`.
    5) `10` → **num**, atributo: o valor 10.
    6) `)` → delimitador **)**.
    7) `total` → **id**, ponteiro para total.
    8) `+=` → operador de atribuição composta **+=**.
    9) `x` → **id**, o MESMO ponteiro do primeiro x.
    10) `;` → delimitador **;**. Total: 10 tokens.
- WIDGET TOKENIZE entrada=«if (x >= 10) total += x;» legenda: Confira com a Lexi.
- CAIXA [Cuidado!]: Os dois `x` geram dois TOKENS, mas a tabela de símbolos tem UMA só entrada para x. Token se repete; entrada na tabela, não.
- TEXTO: Sua vez (exemplo esmaecido): `while (i <= n1) i++;`
- REVELAR: Quantos tokens?
    → 9: while, (, i, <=, n1, ), i, ++, ;
- REVELAR: Qual é o token de `while`? E de `n1`?
    → `while` é palavra-chave (reservada). `n1` é id: começa com letra e depois vem um dígito.
- REVELAR: `<=` e `++` são um token cada ou dois?
    → Um cada: maior casamento. `<=` é relop (LE); `++` é o operador de incremento.
- WIDGET TOKENIZE entrada=«while (i <= n1) i++;» legenda: Confira suas respostas.
- CAIXA [Dica de prova]: Questão comum: 'quantos tokens há no trecho?'. Armadilhas: operadores de dois caracteres (`<=`, `==`, `++`), número com ponto (`3.14` é UM token), string (`"a b c"` é UM token) e comentários (zero tokens).

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que existe um analisador léxico?"
- PROF: Vou te fazer perguntas. Chute sem medo: errar aqui é parte do método.
- PREVEJA: O analisador léxico descarta espaços e comentários. Por quê?
    opções: ✔Porque eles não mudam o significado do programa (na maioria das linguagens) | Porque ocupam memória demais | Porque o sintático tem preguiça | Porque é proibido usar espaços em C
    explicação: Espaço só separa lexemas; depois disso não serve pra mais nada. (Python é a exceção famosa: a indentação vira tokens INDENT/DEDENT!)
- PREVEJA: Em `count2=count2+1`, o que é `count2`?
    opções: Dois tokens: id `count` e num `2` | ✔Um único token id | Erro léxico | Uma palavra-chave
    explicação: O padrão de id é letra seguida de letras OU dígitos, e o maior casamento engole o 2 junto.
- PREVEJA: `>=` deve virar quantos tokens?
    opções: ✔1 | 2
    explicação: Um só. Regra do **maior casamento** (maximal munch): pegue o maior prefixo da entrada que casa com algum padrão.
- BYTE: Mas e o `if`? Ele casa com o padrão de identificador E com o de palavra-chave. Empate!
- REVELAR: Como desempatar `if`? E por que `iff` não é palavra-chave?
    → Empate de MESMO tamanho: vence a regra listada primeiro, palavras-chave vêm antes de id. `iff` tem 3 letras: o maior casamento prefere o id de 3 letras ao `if` de 2.
- PREVEJA: Em `fi (a == f(x))`, o léxico percebe que `fi` deveria ser `if`?
    opções: Sim, é erro léxico | ✔Não: `fi` é um identificador válido
    explicação: O léxico não enxerga o contexto: `fi` casa com o padrão de id. Exemplo literal do Livro do Dragão!
- PROF: Última: por que não deixar o sintático fazer tudo, lendo caractere por caractere?
- REVELAR: Três motivos para separar análise léxica de sintática.
    → 1) **Simplicidade**: a gramática não precisa falar de espaços e comentários. 2) **Eficiência**: técnicas especializadas (buffers, autômatos) leem caracteres muito rápido. 3) **Portabilidade**: as peculiaridades da entrada ficam isoladas no léxico.
- CAIXA [Conversa ao pé da lareira]: Léxico: 'Eu trabalho com caracteres; você, com tokens.' Sintático: 'E eu te chamo quando preciso: getNextToken()!', na prática o léxico é uma sub-rotina do sintático.
- WIDGET TOKENIZE entrada=«fi (a == f(x)) x = 1;» legenda: Veja: `fi` passa como identificador sem reclamação.

#### ESTILO FORMAL (Definição de prova): "Token, padrão e lexema: definições"
- TEXTO: **Token**: par ⟨nome-do-token, valor-de-atributo⟩. O nome é um símbolo abstrato que representa um tipo de unidade léxica (palavra-chave, identificador…); é o que o analisador sintático processa.
- TEXTO: **Padrão**: descrição da forma que os lexemas de um token podem assumir. Para uma palavra-chave, é a própria sequência de caracteres; para identificadores, é uma estrutura casada por muitas cadeias.
- TEXTO: **Lexema**: sequência de caracteres do programa fonte que casa com o padrão de um token e é identificada pelo analisador léxico como uma instância desse token.
- CÓDIGO (Exemplos de tokens (Livro do Dragão, Fig. 3.2)):
```
TOKEN       PADRÃO (informal)                    LEXEMAS
if          caracteres i, f                      if
else        caracteres e, l, s, e                else
comparison  < ou > ou <= ou >= ou == ou !=       <=, !=
id          letra seguida de letras e dígitos    pi, score, D2
number      qualquer constante numérica          3.14159, 0, 6.02e23
literal     qualquer coisa entre aspas           "core dumped"
```
- TEXTO: Exemplo: `E = M * C ** 2` gera ⟨id, ptr E⟩ ⟨assign_op⟩ ⟨id, ptr M⟩ ⟨mult_op⟩ ⟨id, ptr C⟩ ⟨exp_op⟩ ⟨number, 2⟩.
- CÓDIGO (Esqueleto de um analisador léxico):
```
função getNextToken():
    pular espaços e comentários (contando linhas)
    lexemeBegin ← forward
    para cada padrão P, em ordem de prioridade:
        achar o MAIOR prefixo da entrada que casa com P
    escolher o maior prefixo; empate → padrão listado primeiro
    se nenhum casou: erro léxico (ex.: modo pânico)
    se for id: instalar/consultar na tabela de símbolos
    retornar ⟨nome, atributo⟩
```
- CAIXA [Não existem perguntas idiotas]: P: E quando nenhum padrão casa? R: Recuperação em **modo pânico**: apagar caracteres até achar um token válido. Outras ações possíveis: inserir, remover, substituir um caractere ou trocar dois adjacentes.
- PREVEJA: Em `x = 3.14;`, quantos lexemas existem?
    opções: ✔4 | 5 | 6
    explicação: x, =, 3.14 e ;: o número com ponto é UM lexema só.
- CAIXA [Dica de prova]: Não troque as definições! Token = CLASSE (nome + atributo). Lexema = INSTÂNCIA (o texto). Padrão = REGRA (em geral uma expressão regular). Frase de prova perfeita: 'o lexema `3.14` casa com o padrão de number e é reportado como o token ⟨number, 3.14⟩'.
- REVELAR: Por que o token id precisa de atributo e o token `if` não?
    → Porque há infinitos lexemas possíveis para id, o sintático não saberia QUAL nome era. Para `if` existe um único lexema, então o nome do token já diz tudo.

#### ESTILO MNEMONIC (Truque de memória): "Gaveta, meia e etiqueta"
- CAIXA [Gancho de memória]: **Token é a gaveta**, **lexema é a meia** que você guarda, **padrão é a etiqueta** que diz quais meias vão naquela gaveta.
- LEXI: Outro truque: **LEXema = LEtra EXata**. É o texto literal, do jeitinho que está no código.
- CAIXA [Gancho de memória]: Do abstrato ao concreto: **T**ipo (token) → **P**rescrição (padrão) → **L**iteral (lexema).
- TEXTO: **Palácio da memória**: Floresta Léxica, primeira parada depois do Porto. A Lexi corta os troncos de texto com a katana, cada tora cortada é um lexema, e joga cada tora na carroça certa (o token).
- CAIXA [Gancho de memória]: **Maior casamento = guloso**: a Lexi sempre morde o maior pedaço que dá. `>=` inteiro, `count2` inteiro, `iff` inteiro.
- CAIXA [Gancho de memória]: **Reservada é VIP**: em empate de tamanho, palavra-chave fura a fila na frente do identificador.
- PREVEJA: Lendo `y = 42;`, qual é o LEXEMA do token ⟨num, 42⟩?
    opções: num | ✔42 | y | ⟨num, 42⟩
    explicação: O lexema é o texto `42`. 'num' é o nome do token; o par inteiro é o token.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que `x` e `y` são tokens diferentes, mesmo token id, lexemas diferentes; (2) achar que o léxico pega erro de digitação em palavra-chave (`whlie` é um id válido!); (3) contar espaços como tokens.
- REVELAR: Complete: 'O lexema ___ casa com o padrão ___ e vira o token ___.'
    → Exemplo: 'O lexema `total` casa com o padrão `letra(letra|dígito)*` e vira o token ⟨id, ponteiro para total⟩.'
- WIDGET TOKENIZE entrada=«float media = total / n1;» legenda: Gaveta, meia, etiqueta: identifique cada uma.


## ASSUNTO REGEX_DFA: Expressões regulares e autômatos

#### ESTILO STORY (História / Analogia): "O trem que lê palavras"
- NARRADOR: Na Floresta Léxica passa um trenzinho. Ele só anda pelos trilhos pintados com a letra que está lendo.
- PROF: Cada estação é um **estado**. Cada trilho tem uma letra: é uma **transição**. Algumas estações têm bandeira de chegada: são os **estados finais** (de aceitação).
- BYTE: E como o trem decide se a palavra é aceita?
- PROF: Ele parte da estação inicial e lê a palavra letra por letra, seguindo os trilhos. Acabou a palavra numa estação com bandeira? Aceita! Ficou sem trilho, ou parou longe da bandeira? Rejeita.
- WIDGET DFA entrada=«(a|b)*abb» legenda: O trem do Livro do Dragão: aceita palavras de a's e b's que terminam em abb. Teste 'aabb' e 'abab'.
- TEXTO: Esse trem é um **AFD** (autômato finito determinístico): em cada estação, para cada letra, há NO MÁXIMO um trilho. O maquinista nunca fica em dúvida.
- BYTE: E se tivesse dois trilhos com a mesma letra?
- PROF: Aí é um **AFN** (não determinístico): um trem mágico que se divide e segue TODOS os caminhos ao mesmo tempo, como num multiverso. E ainda tem trilhos ε, que ele pega sem ler letra nenhuma.
- CAIXA [Conversa ao pé da lareira]: AFN: 'Eu sou fácil de construir a partir da expressão regular!' AFD: 'Mas eu sou rápido: um passo por letra, sem dúvida nenhuma.' Os dois reconhecem exatamente as mesmas linguagens: as regulares.
- PASSOS (A fábrica de trens):
    1) **Expressão regular**: o desenho do trajeto, como `(a|b)*abb`.
    2) **Construção de Thompson**: transforma a ER num AFN, peça por peça (união, concatenação, fecho).
    3) **Construção de subconjuntos**: cada estação do AFD é um CESTO de estações do AFN onde o trem mágico poderia estar.
    4) **Minimização**: junta estações que se comportam igual. O AFD de `(a|b)*abb` fica com 4 estados.
- PREVEJA: Um AFN com 3 estados vira um AFD com, NO MÁXIMO, quantos estados?
    opções: 3 | 6 | ✔8 | 9
    explicação: Cada estado do AFD é um SUBCONJUNTO dos estados do AFN: 2³ = 8 subconjuntos possíveis. Na prática, quase sempre são bem menos.
- CAIXA [No mundo real]: Lex/Flex fazem exatamente isso: você escreve as ERs dos tokens e eles geram o AFD que vira o coração do analisador léxico.
- REVELAR: Na analogia, o que é estação, trilho, bandeira e trem mágico?
    → Estado, transição, estado final (de aceitação) e AFN (que pode estar em vários estados ao mesmo tempo).

#### ESTILO VISUAL (Passo a passo visual): "Brincando com autômatos"
- PROF: Cada widget é um AFD MÍNIMO gerado pelo motor a partir da expressão regular. Teste cadeias e veja o caminho acender.
- WIDGET DFA entrada=«ab*» legenda: ab*: um a e depois quantos b quiser. Teste 'a', 'abbb' e 'ba'.
- TEXTO: Repare: o `*` (fecho de Kleene) virou um **laço**: um trilho que sai e volta para o mesmo estado.
- WIDGET DFA entrada=«(a|b)*abb» legenda: Termina em abb.
- PREVEJA: Em `(a|b)*abb`, depois de ler 'ab', em que situação o autômato está?
    opções: Já aceitou | ✔Já viu 'ab': falta um b | Rejeitou para sempre | Voltou ao início
    explicação: Cada estado do AFD mínimo 'lembra' quanto do sufixo abb já viu: nada, 'a', 'ab' ou 'abb'. Por isso são 4 estados.
- WIDGET DFA entrada=«(aa)*» legenda: Quantidade par de a's: dois estados que se alternam, como um interruptor.
- BYTE: Dá para fazer um que aceite a's e b's na mesma quantidade, tipo aaabbb?
- PROF: Não dá! Autômato finito tem memória finita: não consegue contar até um número arbitrário. aⁿbⁿ precisa de gramática livre de contexto, você vai ver no Vale das Gramáticas.
- WIDGET DFA entrada=«0|1(0|1)*» legenda: Binários sem zero à esquerda: teste '0', '101' e '011'.
- REVELAR: Por que '011' é rejeitada?
    → Depois de ler '0', o autômato está num estado final SEM saídas: o '0' sozinho é aceito, mas não pode ser seguido de nada. Ler '1' cai no estado morto.
- CAIXA [Cuidado!]: O motor desenha o AFD mínimo SEM o estado morto. Transição que não aparece = vai para o estado morto (rejeição).
- WIDGET DFA entrada=«x(y|z)+» legenda: Agora teste você: quais cadeias são aceitas?

#### ESTILO WORKED (Exemplo resolvido): "Construção de subconjuntos na mão"
- PROF: Vamos converter um AFN em AFD com calma. Este AFN aceita as cadeias de a's e b's que terminam em ab.
- CÓDIGO (AFN: em 0, lendo a, o trem pode ficar em 0 OU ir para 1 (não determinismo!)):
```
estados: 0 (inicial), 1, 2 (final)
0 --a--> 0     0 --b--> 0
0 --a--> 1
1 --b--> 2
```
- TEXTO: Regra do jogo: cada estado do AFD é um **conjunto** de estados do AFN, e Dtran[T, x] = ε-fecho(move(T, x)). Este AFN não tem transições ε, então o ε-fecho não acrescenta nada.
- PASSOS (Resolvido):
    1) Início: A = ε-fecho({0}) = {0}.
    2) A com a: move({0}, a) = {0, 1} → novo estado **B**.
    3) A com b: move({0}, b) = {0} → é o próprio **A**.
    4) B com a: move({0, 1}, a) = {0, 1} → **B**.
    5) B com b: move({0, 1}, b) = {0, 2} → novo estado **C**.
    6) C com a: {0, 1} = **B**. C com b: {0} = **A**.
    7) Finais: os conjuntos que contêm o estado 2 → só **C**. Pronto: AFD com 3 estados.
- CÓDIGO (Tabela de transição do AFD (→ inicial, * final)):
```
      a    b
→A    B    A
 B    B    C
*C    B    A
```
- WIDGET DFA entrada=«(a|b)*ab» legenda: O motor chega ao mesmo AFD de 3 estados. Compare!
- TEXTO: Agora esmaecido: AFN para cadeias que terminam em **bb**: 0 --a,b--> 0, 0 --b--> 1, 1 --b--> 2 (final).
- REVELAR: A = {0}. Calcule Dtran[A, a] e Dtran[A, b].
    → Dtran[A, a] = {0} = A. Dtran[A, b] = {0, 1} = novo estado B.
- REVELAR: Agora B = {0, 1}: com a? com b?
    → B com a: {0} = A. B com b: {0, 1, 2} = novo estado C.
- REVELAR: C = {0, 1, 2}: com a? com b? Quem é final?
    → C com a: {0} = A. C com b: {0, 1, 2} = C. Final: C (contém o 2). AFD com 3 estados.
- WIDGET DFA entrada=«(a|b)*bb» legenda: Confira: o motor também dá 3 estados.
- CAIXA [Dica de prova]: Na prova, organize em tabela: 'estado do AFD', 'conjunto do AFN' e uma coluna por símbolo. E SEMPRE aplique o ε-fecho depois do move quando o AFN tiver transições ε, o de Thompson tem muitas!

#### ESTILO SOCRATIC (Perguntas guiadas): "Descobrindo as expressões regulares"
- PROF: Vou te mostrar autômatos e perguntar. As regras, você descobre.
- PREVEJA: Quais cadeias `a(a|b)*b` aceita?
    opções: ✔As que começam com a e terminam com b | Só 'ab' | Qualquer cadeia de a's e b's | As que começam com b
    explicação: Um a obrigatório, qualquer miolo de a's e b's, e um b obrigatório no fim.
- WIDGET DFA entrada=«a(a|b)*b» legenda: Teste: 'ab', 'aab', 'abba', 'ba'.
- PREVEJA: `x(y|z)+` aceita a cadeia 'x'?
    opções: Sim | ✔Não
    explicação: `+` significa 'uma ou mais vezes': r+ = r r*. Sem pelo menos um y ou z, rejeita.
- PREVEJA: Qual ER descreve os binários ímpares (terminam em 1)?
    opções: ✔(0|1)*1 | 1(0|1)* | (01)* | 0*1*
    explicação: Qualquer coisa, desde que o último dígito seja 1. `1(0|1)*` descreve os que COMEÇAM com 1.
- WIDGET DFA entrada=«(0|1)*1»
- REVELAR: Escreva `r?` usando só |, concatenação e *.
    → `r?` = `r | ε` (zero ou uma vez). E `r+` = `r r*`. Ou seja: `?` e `+` são só açúcar sintático!
- PREVEJA: Dá para escrever uma ER para parênteses balanceados, como (()) e ()()?
    opções: Sim, com bastante * | ✔Não: autômato finito não consegue contar profundidade arbitrária
    explicação: Para saber quantos ')' faltam, seria preciso memória ilimitada. Isso é trabalho de gramática livre de contexto.
- PROF: Agora a pergunta de ouro: por que o analisador léxico usa ER e autômato, e não gramática?
- REVELAR: Por que ER/AFD no léxico?
    → Tokens têm estrutura simples (regular), e o AFD reconhece em tempo linear: um passo por caractere, sem voltar atrás. Estruturas aninhadas (parênteses, blocos) ficam para o sintático, com gramáticas.
- REVELAR: Se AFN e AFD têm o mesmo poder, por que usar AFN?
    → Porque o AFN é muito mais fácil de construir a partir de uma ER (Thompson, peça por peça). Depois convertemos para AFD, que é rápido de executar.

#### ESTILO FORMAL (Definição de prova): "ER, AFN e AFD: definições e algoritmos"
- TEXTO: **Expressão regular** sobre o alfabeto Σ: ε e cada a ∈ Σ são ERs; se r e s são ERs, então r|s, rs, r* e (r) também são. L(r|s) = L(r) ∪ L(s); L(rs) = L(r)L(s); L(r*) = (L(r))*.
- TEXTO: Precedência: `*` (maior) > concatenação > `|` (menor), todas associativas à esquerda. Assim, `a|b*c` = `a|((b*)c)`.
- TEXTO: **AFN**: (S, Σ, move, s₀, F), conjunto finito de estados S, alfabeto Σ, função de transição que leva (estado, símbolo ou ε) a um CONJUNTO de estados, estado inicial s₀ e estados finais F. **AFD**: caso especial sem transições ε e com exatamente um destino para cada par (estado, símbolo).
- CÓDIGO (Algoritmo 3.23 (Livro do Dragão)):
```
Construção de Thompson (ER → AFN):
  ε     :  i --ε--> f
  a     :  i --a--> f
  s|t   :  novo i --ε--> N(s) e N(t); finais de ambos --ε--> novo f
  st    :  final de N(s) se funde com o inicial de N(t)
  s*    :  i --ε--> N(s), i --ε--> f;
           final de N(s) --ε--> início de N(s) e --ε--> f
Propriedade: N(r) tem no máximo 2 × (nº de símbolos e operadores de r) estados.
```
- CÓDIGO (Algoritmo 3.20 (Livro do Dragão)):
```
Construção de subconjuntos (AFN → AFD):
  Dstates ← { ε-fecho(s₀) }, não marcado
  enquanto existe T não marcado em Dstates:
      marque T
      para cada símbolo a:
          U ← ε-fecho(move(T, a))
          se U ∉ Dstates: adicione U (não marcado)
          Dtran[T, a] ← U
  finais: todo T que contém um estado final do AFN
```
- TEXTO: **ε-fecho(T)**: estados alcançáveis a partir de algum estado de T usando só transições ε (inclui o próprio T). **move(T, a)**: estados alcançáveis a partir de T com UMA transição rotulada a.
- PREVEJA: Qual é a precedência correta dos operadores?
    opções: | > concatenação > * | ✔* > concatenação > | | concatenação > * > |
    explicação: Como na aritmética: * é a 'potência', concatenação a 'multiplicação' e | a 'soma'.
- PREVEJA: Pela precedência, `ab*` significa…
    opções: (ab)* | ✔a(b*)
    explicação: O * se aplica só ao símbolo imediatamente à esquerda: um a seguido de zero ou mais b's.
- WIDGET DFA entrada=«ab*» legenda: Confirme: 'abab' é rejeitada.
- CAIXA [Dica de prova]: Cai muito: (1) desenhar o AFN de Thompson de `(a|b)*abb` (11 estados, de 0 a 10); (2) aplicar subconjuntos e chegar a A, B, C, D, E, com E final; (3) minimizar juntando A e C → 4 estados. Saiba que ε-fecho({0}) = {0, 1, 2, 4, 7}.
- CAIXA [Cuidado!]: Um AFN de n estados pode virar um AFD de até 2ⁿ estados (explosão exponencial), mas isso é raro na prática. Em troca, simular o AFD gasta só um passo por caractere.
- REVELAR: Defina ε-fecho e move em uma linha cada.
    → ε-fecho(T): estados alcançáveis a partir de T só por transições ε (incluindo T). move(T, a): estados alcançáveis a partir de T por exatamente uma transição rotulada a.

#### ESTILO MNEMONIC (Truque de memória): "Thompson Transforma, Subconjuntos Simplificam"
- CAIXA [Gancho de memória]: **T**hompson **T**ransforma (ER → AFN). **S**ubconjuntos **S**implificam o caos (AFN → AFD). E a **M**inimização **M**ata os estados repetidos.
- CAIXA [Gancho de memória]: **N** de AFN = **N**ão sei para onde vou (vários caminhos). **D** de AFD = **D**ecidido (um caminho só).
- CAIXA [Gancho de memória]: Precedência igual à da matemática: `*` é potência, concatenação é multiplicação, `|` é soma. Como 2 + 3·4², `a|bc*` = `a|(b(c*))`.
- CAIXA [Gancho de memória]: `*` = zero ou mais (a estrela é generosa: aceita até nada). `+` = um ou mais (MAIS = pelo menos um). `?` = talvez (zero ou um).
- PREVEJA: Qual ER aceita a cadeia vazia?
    opções: a+ | ab* | ✔(ab)* | x(y|z)+
    explicação: Só a estrela no 'lado de fora' permite zero repetições de tudo. As outras exigem pelo menos um símbolo.
- WIDGET DFA entrada=«(ab)*» legenda: O estado inicial já é final: a cadeia vazia é aceita!
- TEXTO: **Palácio da memória**: ainda na Floresta Léxica, o **Trem do Autômato**. A Lexi desenha o trajeto (ER), os duendes de Thompson espalham trilhos ε por todo lado (AFN), e o maquinista junta estações em cestos (subconjuntos) para nunca ficar em dúvida (AFD).
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que AFN é mais poderoso que AFD, reconhecem as mesmas linguagens; (2) esquecer o ε-fecho depois do move; (3) achar que ER sabe contar (aⁿbⁿ não é regular!).
- CAIXA [Gancho de memória]: **Regular não conta e não aninha.** Precisou contar ou casar parênteses? É trabalho de gramática.
- REVELAR: Recite a esteira com o nome de cada algoritmo.
    → ER →(Thompson)→ AFN →(subconjuntos)→ AFD →(minimização)→ AFD mínimo.


## ASSUNTO NFA_TO_DFA: AFN → AFD (construção de subconjuntos)

#### ESTILO STORY (História / Analogia): "A turma no grupo do WhatsApp"
- NARRADOR: Rodoviária lotada, véspera de feriado. Você e sua turma precisam chegar à plataforma certa, mas a placa diz: 'Ônibus A: plataforma 1 OU plataforma 3'. Cadê a certeza?
- BYTE: Eu ia travar. Escolho uma e torço?
- PROF: Um **AFN** faz diferente: ele não escolhe. A turma se DIVIDE e cada um vai para uma possibilidade. E tem os corredores livres, sem catraca: quem está num ponto pode atravessar de graça. Esses são os trilhos **ε**.
- PROF: Agora o truque: em vez de acompanhar cada amigo, você só olha o **grupo do WhatsApp** e anota: 'neste momento a turma está em {1, 3, 4}'. Esse conjunto inteiro vira UM estado de um **AFD**.
- PASSOS (Rodoviária → construção de subconjuntos):
    1) Cada plataforma é um **estado do AFN**.
    2) Corredor livre (sem ler letra nenhuma) é uma **transição ε**. Juntar todo mundo que dá para alcançar a pé é o **ε-fecho**.
    3) Usar uma passagem com a letra 'a' é o **move(T, a)**: para onde cada amigo pode ir lendo 'a'.
    4) A lista do grupo ('estamos em {0, 1}') é um **estado do AFD**: um CONJUNTO de estados do AFN.
    5) Se ninguém da turma tem para onde ir, o grupo fica vazio: é o **estado morto ∅**.
    6) Se pelo menos UM amigo chegou ao destino, a turma toda comemora: o conjunto é **final** se contém algum final do AFN.
- BYTE: Então o AFD é só o diário do grupo: 'estávamos aqui, lemos tal letra, fomos parar ali'?
- PROF: Exatamente. E como o diário é anotado ANTES da viagem, na hora de ler a entrada não existe mais dúvida: um passo por letra.
- PREVEJA: No AFN de '(a|b)*ab' a turma começa em {0}. De 0, lendo 'a', dá para ficar em 0 ou ir para 1. Lendo 'a', onde a turma fica?
    opções: {0} | {1} | ✔{0, 1} | ∅
    explicação: A turma se divide: uns ficam em 0, outros vão para 1. O estado do AFD é o conjunto {0, 1}. Sem dúvida, sem sorteio: carregamos as duas possibilidades juntas.
- WIDGET NFA_SUBSET entrada=«ab» legenda: Avance passo a passo: em amarelo, onde a turma está. Repare que o AFD final tem só 3 estados.
- CAIXA [Não existem perguntas idiotas]: P: Se o AFN já reconhece a linguagem, para que converter? R: Simular o AFN obriga a carregar um conjunto a cada letra lida. O AFD faz essa conta UMA vez, na construção; depois, cada caractere custa só uma consulta à tabela. Analisador léxico lê milhões de caracteres: essa economia importa.
- CAIXA [Poder do cérebro]: Com 3 plataformas, quantas listas diferentes o grupo do WhatsApp poderia ter, no máximo? Pense em cada plataforma como 'tem gente' ou 'não tem'.
- REVELAR: Resposta
    → 2³ = 8 listas possíveis (incluindo a vazia). Por isso um AFN de n estados vira um AFD de até 2ⁿ estados. Mas quase sempre a turma só visita poucas listas, no exemplo acima, 3.
- REVELAR: Na analogia, quem é: plataforma, corredor livre, lista do grupo, grupo vazio?
    → Estado do AFN, transição ε, estado do AFD (conjunto de estados do AFN) e estado morto ∅.

#### ESTILO VISUAL (Passo a passo visual): "Assistindo a fábrica de subconjuntos"
- PROF: Hoje você vai VER a construção acontecer. Em cada passo, o amarelo mostra o conjunto atual, e a tabela do AFD vai se preenchendo sozinha.
- WIDGET NFA_SUBSET entrada=«abb» legenda: O clássico (a|b)*abb, numa versão de AFN com 4 estados. Avance devagar.
- TEXTO: Repare: começamos em A = {0}. Lendo 'a' vamos para {0, 1} = B; de B, lendo 'b', vamos para {0, 2} = C; de C, lendo 'b', chegamos a {0, 3} = D, o único que contém o final 3.
- PREVEJA: Por que TODOS os estados do AFD contêm o 0?
    opções: Porque 0 é o inicial e nunca sai do conjunto | ✔Porque 0 tem laço com a e com b: quem está em 0 sempre pode continuar em 0 | Coincidência | Porque o 0 é final
    explicação: O laço a,b em 0 significa 'ainda não comecei o sufixo abb'. Essa possibilidade nunca morre, então o 0 aparece em todo conjunto.
- LEXI: Agora com trilhos ε. Aqui o primeiro estado já NÃO é só {0}.
- WIDGET NFA_SUBSET entrada=«abc» legenda: ab* | c: do estado 0 saem dois trilhos ε. Veja o ε-fecho logo no início.
- PREVEJA: No AFN de 'ab* | c', 0 tem trilhos ε para 1 e para 3. Quanto vale ε-fecho({0})?
    opções: {0} | {1, 3} | ✔{0, 1, 3} | {0, 1, 2, 3, 4}
    explicação: O ε-fecho inclui o próprio 0 e todo mundo alcançável só por ε: 1 e 3. O 2 e o 4 exigem ler uma letra, então ficam de fora.
- CAIXA [Cuidado!]: Quando o move dá vazio (por exemplo, de A = {0, 1, 3} lendo 'b'), o widget mostra ∅: é o **estado morto**. O desenho do AFD omite o morto, mas ele existe, qualquer transição que não aparece leva para lá.
- WIDGET NFA_SUBSET entrada=«ou» legenda: (ab | a)c: dois caminhos que se reencontram por ε no estado 4.
- TEXTO: Repare no passo de A com 'a': move = {1, 3}, mas o ε-fecho puxa o 4 também, porque 3 --ε--> 4. Resultado: B = {1, 3, 4}. É por isso que 'ac' é aceita: B já 'sabe' que pode ler o c.
- REVELAR: Quantos estados tem o AFD de (ab | a)c, e qual é o final?
    → 4 estados: A = {0}, B = {1, 3, 4}, C = {2, 4} e D = {5}. Só D é final, porque só ele contém o 5.
- BYTE: E quando o número de estados explode mesmo?
- WIDGET DFA entrada=«(a|b)*a(a|b)(a|b)» legenda: 'O antepenúltimo símbolo é a'. O AFN tem 4 estados; o AFD mínimo tem 8 = 2³. Ele precisa lembrar as 3 últimas letras!
- CAIXA [No mundo real]: Esse tipo de linguagem é o pior caso. Os padrões de tokens de verdade (identificadores, números, palavras-chave) geram AFDs pequenos, por isso Lex/Flex podem converter tudo para AFD sem medo.

#### ESTILO WORKED (Exemplo resolvido): "Subconjuntos com ε, do começo ao fim"
- PROF: Vamos resolver com calma um AFN COM transições ε. É assim que aparece na prova, porque o AFN de Thompson tem ε por todo lado.
- CÓDIGO (Dois caminhos (ab ou a) que se juntam no 4 antes do c):
```
AFN de (ab | a)c: inicial 0, final 5
0 --a--> 1     1 --b--> 2     2 --ε--> 4
0 --a--> 3     3 --ε--> 4
4 --c--> 5
```
- TEXTO: A receita tem um passo só, repetido: **Dtran[T, x] = ε-fecho(move(T, x))**. Primeiro anda com a letra (move), depois anda de graça pelos ε (fecho).
- PASSOS (Resolvido):
    1) Início: A = ε-fecho({0}) = {0}. Do 0 não sai nenhum ε.
    2) A com a: move = {1, 3}. Fecho: 3 --ε--> 4, então **B = {1, 3, 4}**.
    3) A com b e A com c: move = ∅ → estado morto.
    4) B com b: move = {2}. Fecho: 2 --ε--> 4, então **C = {2, 4}**.
    5) B com c: move = {5} → **D = {5}**. B com a: ∅.
    6) C com c: move = {5} = D. C com a e com b: ∅.
    7) D com qualquer letra: ∅. Nenhum estado novo: terminou!
    8) Finais: quem contém o 5 → só **D**. AFD com 4 estados (sem contar o morto).
- CÓDIGO (Tabela Dtran (→ inicial, * final, ∅ = estado morto)):
```
        a    b    c
→A {0}       B    ∅    ∅
 B {1,3,4}   ∅    C    D
 C {2,4}     ∅    ∅    D
*D {5}       ∅    ∅    ∅
```
- WIDGET NFA_SUBSET entrada=«ou» legenda: Confira cada linha da tabela com o motor.
- CAIXA [Cuidado!]: O erro número 1 é esquecer o fecho depois do move. Sem ele, B seria {1, 3} e o AFD rejeitaria 'ac', mesmo 'ac' estando na linguagem!
- TEXTO: Agora é com você (exemplo esmaecido). AFN de **ab* | c**: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4. Finais: 2 e 4.
- REVELAR: Qual é o estado inicial A do AFD?
    → A = ε-fecho({0}) = {0, 1, 3}. Os dois ε saem do 0.
- REVELAR: Calcule Dtran[A, a], Dtran[A, b] e Dtran[A, c].
    → A com a: move = {2}, fecho = {2} = B. A com b: ∅ (morto). A com c: move = {4} = C.
- REVELAR: Continue com B = {2} e C = {4}. Quem é final?
    → B com b: {2} = B (o laço do b*). Todo o resto dá ∅. Finais: B (contém 2) e C (contém 4). AFD com 3 estados.
- WIDGET NFA_SUBSET entrada=«abc» legenda: Confira suas respostas.
- CAIXA [Dica de prova]: Monte SEMPRE a tabela com as colunas 'estado do AFD | conjunto do AFN | uma coluna por símbolo'. Escreva o move e o fecho separados no rascunho: o professor dá ponto parcial pelo raciocínio.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que conjuntos?"
- PROF: Hoje eu só pergunto. Você vai reinventar a construção de subconjuntos sozinho.
- PREVEJA: Um AFN está no estado 0 e tem DOIS trilhos 'a': para 0 e para 1. Lendo 'a', onde ele está?
    opções: Em 0 | Em 1 | ✔Nos dois ao mesmo tempo | Trava: AFN não pode ter isso
    explicação: O AFN aceita se ALGUM caminho aceita. Para não perder nenhum, acompanhamos todos ao mesmo tempo: o AFN está em {0, 1}.
- BYTE: Então, se eu sempre anotar o CONJUNTO onde ele pode estar, nunca preciso adivinhar?
- PROF: Isso! E se você dá um nome a cada conjunto (A, B, C…), acabou de inventar um autômato determinístico.
- PREVEJA: Depois de ler uma letra, por que aplicar o ε-fecho de novo?
    opções: Não precisa, só no início | ✔Porque, ao chegar num estado, o AFN pode seguir de graça pelos trilhos ε | Para tirar estados repetidos | Para achar os finais
    explicação: Trilho ε não consome entrada. Se você chega em 3 e existe 3 --ε--> 4, então também está em 4. Esquecer isso perde caminhos.
- PREVEJA: E quando move(T, a) dá o conjunto vazio?
    opções: A cadeia é aceita | Voltamos ao estado inicial | ✔Vamos para o estado morto ∅, de onde não se sai | É um erro na construção
    explicação: Nenhum caminho sobreviveu. ∅ é um estado do AFD como outro qualquer, só que não final e com laço para tudo.
- REVELAR: Um conjunto como {0, 2} deve ser final quando o final do AFN é o 2? E {0, 1}?
    → {0, 2} é final: basta UM caminho ter chegado ao fim. {0, 1} não é: nenhum caminho está num estado final.
- PROF: Agora o tamanho. Olhe este AFN de 3 estados: 'o penúltimo símbolo é a'.
- WIDGET NFA_SUBSET entrada=«pen» legenda: Conte quantos estados o AFD ganha.
- PREVEJA: Com 3 estados no AFN, o máximo seriam 2³ = 8 conjuntos. Por que só apareceram 4?
    opções: O motor errou | ✔Todo conjunto alcançável contém o 0 (por causa do laço), então sobram só 2² = 4 combinações | Porque o AFN não tem ε | Porque o alfabeto tem 2 letras
    explicação: O 0 está sempre presente; só os estados 1 e 2 variam (estão ou não). São 2 × 2 = 4 conjuntos possíveis, e todos aparecem.
- REVELAR: Se fosse 'o n-ésimo símbolo a partir do fim é a', quantos estados o AFD teria?
    → 2ⁿ. O AFD precisa lembrar as n últimas letras, e cada uma pode ser a ou b. É o exemplo clássico da explosão exponencial.
- WIDGET DFA entrada=«(a|b)*a(a|b)(a|b)» legenda: n = 3: o AFD mínimo tem 2³ = 8 estados. Nem minimizando dá para fugir.
- REVELAR: Então por que os analisadores léxicos usam AFD mesmo assim?
    → Porque a explosão é rara com padrões de tokens reais, a conversão acontece UMA vez (ao gerar o léxico) e depois cada caractere custa um único passo. Pagar na fábrica para economizar na estrada.

#### ESTILO FORMAL (Definição de prova): "Construção de subconjuntos: definição de prova"
- TEXTO: **AFN**: N = (S, Σ, δ, s₀, F), com δ: S × (Σ ∪ {ε}) → P(S). **AFD**: caso particular sem transições ε e com exatamente um destino para cada par (estado, símbolo).
- TEXTO: **ε-fecho(s)**: estados alcançáveis a partir de s usando apenas transições ε (inclui s). **ε-fecho(T)** = ∪ ε-fecho(s), s ∈ T. **move(T, a)**: estados alcançáveis a partir de algum s ∈ T por UMA transição rotulada a.
- CÓDIGO (Cálculo do ε-fecho (Livro do Dragão, Fig. 3.33)):
```
ε-fecho(T):
    empilhe todos os estados de T
    fecho ← T
    enquanto a pilha não estiver vazia:
        desempilhe t
        para cada u com t --ε--> u:
            se u ∉ fecho: fecho ← fecho ∪ {u}; empilhe u
    devolva fecho
```
- CÓDIGO (Construção de subconjuntos (Algoritmo 3.20)):
```
Dstates ← { ε-fecho({s₀}) }, não marcado
enquanto houver T não marcado em Dstates:
    marque T
    para cada símbolo a ∈ Σ:
        U ← ε-fecho(move(T, a))
        se U ∉ Dstates: adicione U (não marcado)
        Dtran[T, a] ← U
estado inicial do AFD: ε-fecho({s₀})
estados finais do AFD: todo T com T ∩ F ≠ ∅
```
- TEXTO: **Correção**: o AFD D simula todos os caminhos do AFN N em paralelo; após ler w, D está em T exatamente quando T é o conjunto de estados em que N pode estar após ler w. Logo L(D) = L(N).
- CÓDIGO (Exemplo do livro: AFN de Thompson de (a|b)*abb (estados 0..10, final 10)):
```
A = {0,1,2,4,7}
B = {1,2,3,4,6,7,8}
C = {1,2,4,5,6,7}
D = {1,2,4,5,6,7,9}
E = {1,2,4,5,6,7,10}

Dtran   a   b
→A      B   C
 B      B   D
 C      B   C
 D      B   E
*E      B   C
```
- PREVEJA: No exemplo do livro, por que E é o único estado final?
    opções: Porque foi o último criado | ✔Porque é o único que contém o estado 10, final do AFN | Porque tem mais estados | Porque Dtran[E, b] = C
    explicação: Regra: T é final se T ∩ F ≠ ∅. F = {10} e só E contém o 10.
- CAIXA [Não existem perguntas idiotas]: P: O AFD do livro tem 5 estados, mas o AFD mínimo de (a|b)*abb tem 4. Está errado? R: Não! A construção de subconjuntos não garante o mínimo. A e C se comportam igual (ambos vão para B com a e para C com b) e a minimização os funde.
- WIDGET DFA entrada=«(a|b)*abb» legenda: O motor mostra o AFD já minimizado: 4 estados.
- TEXTO: **Custo**: um AFN de n estados pode gerar um AFD de até 2ⁿ estados. Comparação clássica para reconhecer x com a ER r: simular o AFN gasta espaço O(|r|) e tempo O(|r| × |x|); o AFD gasta tempo O(|x|), mas pode exigir espaço O(2^|r|) no pior caso.
- PREVEJA: Qual afirmação é VERDADEIRA?
    opções: AFN reconhece mais linguagens que AFD | ✔Todo AFN tem um AFD equivalente, que pode ter exponencialmente mais estados | A construção de subconjuntos sempre produz o AFD mínimo | O estado inicial do AFD é sempre {s₀}
    explicação: Mesmo poder (linguagens regulares), custo de tamanho possivelmente exponencial. O inicial é ε-fecho({s₀}), que pode ser maior que {s₀}.
- CAIXA [Dica de prova]: Questão clássica: 'converta o AFN de Thompson de (a|b)*abb em AFD'. Decore o caminho: ε-fecho({0}) = {0,1,2,4,7}, cinco estados A–E, E final, e A ≡ C na minimização. Escreva Dtran[T, a] = ε-fecho(move(T, a)) no topo da folha.
- REVELAR: Escreva a fórmula central do algoritmo e as regras de inicial e finais.
    → Dtran[T, a] = ε-fecho(move(T, a)). Inicial: ε-fecho({s₀}). Finais: todo T que contém pelo menos um estado final do AFN.

#### ESTILO MNEMONIC (Truque de memória): "Anda e Fecha a Porta"
- CAIXA [Gancho de memória]: **Anda e fecha a porta!** Primeiro ANDA com a letra (move), depois FECHA (ε-fecho). Como a sua mãe gritando da cozinha: entrou, fecha a porta!
- CAIXA [Gancho de memória]: **Cesto de frutas**: cada estado do AFD é um cesto com estados do AFN dentro. O cesto é **final se tem UMA fruta premiada** (um final do AFN). O **cesto vazio ∅** é o estado morto.
- TEXTO: **Palácio da memória**: na Floresta Léxica, logo depois do Trem do Autômato, fica a **Fábrica de Subconjuntos** . Os duendes de Thompson entregam trilhos bagunçados (AFN); os operários põem estados em cestos, carimbam A, B, C… e entregam ao trem um mapa sem dúvidas (AFD).
- CAIXA [Gancho de memória]: **2ⁿ = interruptores**: cada estado do AFN é uma lâmpada que está acesa (no cesto) ou apagada. n lâmpadas → 2ⁿ combinações. É o TETO, não o normal.
- PREVEJA: Qual a ordem correta dentro de cada passo da construção?
    opções: ε-fecho, depois move | ✔move, depois ε-fecho | só move | só ε-fecho
    explicação: Anda (move com a letra) e fecha a porta (ε-fecho). O ε-fecho 'sozinho' só aparece uma vez: no estado inicial, ε-fecho({s₀}).
- CAIXA [Gancho de memória]: **Inicial também fecha a porta**: o estado inicial do AFD NÃO é {s₀}, é ε-fecho({s₀}). Se esqueceu, o cesto começa faltando fruta.
- WIDGET NFA_SUBSET entrada=«eps» legenda: a*b*: o inicial já é {0, 1} por causa do ε. Repare que A já é final, a cadeia vazia é aceita!
- CAIXA [Cuidado!]: Contrastes que caem na prova: **move** usa UMA letra; **ε-fecho** não usa letra nenhuma. **AFN** pode estar em vários estados; **AFD** em exatamente um. **∅** é o estado morto, não o inicial. Subconjuntos NÃO minimiza: isso é outra etapa.
- CAIXA [Gancho de memória]: A esteira completa: **T**hompson **T**ransforma (ER → AFN), **S**ubconjuntos **S**implificam (AFN → AFD), **M**inimização **M**ata os repetidos (AFD → AFD mínimo). Lex/Flex rodam a esteira inteira por você.
- REVELAR: Sem olhar: as 3 regras da construção de subconjuntos.
    → 1) Inicial = ε-fecho({s₀}). 2) Dtran[T, a] = ε-fecho(move(T, a)), anda e fecha a porta. 3) T é final se contém algum final do AFN.
- REVELAR: Por que analisadores léxicos preferem AFD?
    → Um passo por caractere, sem voltar atrás e sem carregar conjuntos. A conversão (talvez cara) é feita uma vez só, quando o léxico é gerado.


## ASSUNTO DFA_MINIMIZATION: Minimização de AFD (tabela de pares)

#### ESTILO STORY (História / Analogia): "Os atendentes que fazem a mesma coisa"
- NARRADOR: Lanchonete do bairro, hora do almoço. O dono olha a folha de pagamento e franze a testa: 'Cinco atendentes... será que preciso de todos?'
- BYTE: Ué, demite quem trabalha menos?
- PROF: Não é bem isso. Ele faz um teste: dá a mesma sequência de pedidos para dois atendentes. Se, para QUALQUER sequência, os dois acabam do mesmo jeito (entregando o lanche ou não), eles são **intercambiáveis**. Dá para juntar os dois num cargo só.
- PASSOS (Lanchonete → minimização de AFD):
    1) Cada atendente é um **estado** do AFD.
    2) Cada pedido do cliente ('a', 'b') é um **símbolo** da entrada: o atendente repassa você para alguém.
    3) 'Entregar o lanche' no fim da fila de pedidos é terminar num **estado final**.
    4) Dois atendentes que SEMPRE terminam igual são **equivalentes** (indistinguíveis).
    5) Se existe UMA sequência de pedidos em que um entrega e o outro não, eles são **distinguíveis**.
    6) Juntar todos os equivalentes dá o **AFD mínimo**: mesmo serviço, folha de pagamento menor.
- LEXI: E na Floresta Léxica isso importa muito: meu analisador léxico é um AFD gigante. Menos estados = tabela menor = léxico mais leve.
- PREVEJA: Atendente X entrega o lanche quando a fila de pedidos acaba nele; atendente Y nunca entrega. Eles podem ser equivalentes?
    opções: Sim, se repassarem os pedidos para as mesmas pessoas | ✔Não: com a fila VAZIA um entrega e o outro não | Depende do tamanho do alfabeto
    explicação: A cadeia vazia já os separa. Por isso o primeiro passo do método é sempre marcar todo par (final, não-final): eles são distinguíveis de cara.
- PROF: Depois vem o 'efeito dominó': se X e Y, ao receber 'b', repassam você para dois atendentes que JÁ sabemos que são diferentes, então X e Y também são diferentes.
- WIDGET MINIMIZE entrada=«0» legenda: O AFD do livro para (a|b)*abb. Avance e veja os pares sendo marcados rodada a rodada. Quem sobra sem marca se funde.
- REVELAR: No widget, quais dois estados sobraram sem marca e se fundiram?
    → q0 e q2 (no livro, A e C). Os dois mandam 'a' para q1 e 'b' para q2: fazem exatamente o mesmo trabalho. O AFD cai de 5 para 4 estados.
- CAIXA [Não existem perguntas idiotas]: P: Se a construção de subconjuntos já dá um AFD, por que ele vem com estados sobrando? R: Ela cria um estado para cada CONJUNTO de estados do AFN que aparece, sem perguntar se dois conjuntos diferentes se comportam igual. Minimizar é a faxina depois da mudança.
- CAIXA [Poder do cérebro]: E o atendente que só fica de braços cruzados e nunca entrega nada, não importa o pedido? Ele também precisa entrar na conta. (Spoiler: é o **estado morto**, e esquecê-lo é o erro mais comum da prova.)
- REVELAR: Na analogia, quem é: atendente, pedido, entregar o lanche, cargo único depois da fusão?
    → Estado, símbolo da entrada, terminar num estado final, e um estado do AFD mínimo (uma classe de equivalência).

#### ESTILO VISUAL (Passo a passo visual): "Vendo a tabela se preencher"
- PROF: Hoje você só assiste e prevê. A tabela de pares é uma escadinha: uma casa para cada par de estados {p, q}. Marcar a casa = 'esses dois são diferentes'.
- WIDGET MINIMIZE entrada=«1» legenda: Número par de a's, com estados sobrando. Repare: tudo se resolve já na rodada 1.
- PREVEJA: Nesse AFD, q0 e q2 são finais; q1 e q3 não. Depois da rodada 1, quais pares ficam SEM marca?
    opções: Nenhum | ✔{q0,q2} e {q1,q3} | Só {q0,q2} | {q0,q1} e {q2,q3}
    explicação: A rodada 1 marca só final × não-final. Sobram final com final ({q0,q2}) e não-final com não-final ({q1,q3}). Como nenhuma rodada seguinte marca mais nada, eles se fundem: o AFD mínimo tem 2 estados (par / ímpar).
- WIDGET MINIMIZE entrada=«0» legenda: O clássico do livro. Agora há 3 rodadas de marcação: veja o dominó caindo.
- PASSOS (O que você acabou de ver no exemplo do livro):
    1) Rodada 1: 4 pares marcados, todos com q4, o único final.
    2) Rodada 2: {q0,q3}, {q1,q3} e {q2,q3}. Com 'b', cada um vai para um par que contém q4 e já estava marcado.
    3) Rodada 3: {q0,q1} e {q1,q2}. Com 'b', vão para pares marcados na rodada 2.
    4) Rodada 4: nada novo. Fim. 9 dos 10 pares marcados; só {q0,q2} sobrou.
- LEXI: Agora um AFD que veio da construção de subconjuntos SEM estado morto. Olha o que o widget faz primeiro.
- WIDGET MINIMIZE entrada=«5» legenda: a*b*: falta a transição de q2 com 'a'. O widget completa com o estado morto ∅ antes de montar a tabela.
- PREVEJA: Em a*b*, q0, q1 e q2 são TODOS finais. Sem o estado morto, a rodada 1 não marcaria nada. O que o ∅ muda?
    opções: Nada, ∅ é ignorado | ✔Com 'a', q2 vai para ∅ e q1 fica em q1; como {q1,∅} é final × não-final, {q1,q2} acaba marcado | ∅ se funde com q2
    explicação: O ∅ é o único não-final, então a rodada 1 marca {q0,∅}, {q1,∅} e {q2,∅}. Na rodada 2, 'a' leva q2 para ∅ e q0/q1 para q1: marca {q0,q2} e {q1,q2}. Resultado: q0 ≡ q1, e q2 fica separado.
- REVELAR: E se você esquecesse o ∅ nesse exemplo?
    → Nada seria marcado e você fundiria q0, q1 e q2 num estado só, final, com laço em a e b. Esse AFD aceita (a|b)*, então aceitaria 'ba', que NÃO está em a*b*. Esquecer o estado morto muda a linguagem!
- WIDGET MINIMIZE entrada=«3» legenda: ab*: aqui o ∅ se distingue de q0 só na rodada 2. Depois, o ∅ sozinho some do desenho do AFD mínimo.
- CAIXA [Cuidado!]: O ∅ entra na TABELA, mas, se ficar sozinho na classe dele, não precisa ser desenhado no AFD mínimo: transição que não aparece vai para o morto, por convenção.

#### ESTILO WORKED (Exemplo resolvido): "A tabela da prova, casa por casa"
- PROF: Esta é A questão de prova sobre minimização. Vamos fazer com o AFD do livro, sem pular nada. Pegue papel.
- CÓDIGO (Transições e tabela final em escadinha. Desenhe só a metade de baixo: {p,q} e {q,p} são o mesmo par.):
```
AFD do livro para (a|b)*abb   (q0..q4 = A..E do Dragão)

         a    b
  → q0   q1   q2
    q1   q1   q3
    q2   q1   q2
    q3   q1   q4
  * q4   q1   q2

Tabela de pares (o número é a RODADA em que o par foi marcado;
· = nunca marcado = EQUIVALENTES)

        +----+
   q1   | 3  |
        +----+----+
   q2   | ·  | 3  |
        +----+----+----+
   q3   | 2  | 2  | 2  |
        +----+----+----+----+
   q4   | 1  | 1  | 1  | 1  |
        +----+----+----+----+
          q0   q1   q2   q3
```
- TEXTO: Antes de tudo, dois cuidados: o AFD está **completo** (toda casa da tabela de transições preenchida, então não precisa de ∅) e todos os estados são **alcançáveis** a partir de q0.
- CÓDIGO (Rodada a rodada. ✔ = par de destino já marcado; = = mesmo estado; · = par ainda não marcado):
```
Rodada 1 (final × não-final): q4 é o único final
  {q0,q4} {q1,q4} {q2,q4} {q3,q4}  → marcados

Rodada 2 (olhe só o que foi marcado ATÉ a rodada 1)
  par       com a        com b        marca?
  {q0,q1}   {q1,q1} =    {q2,q3} ·    não (ainda)
  {q0,q2}   {q1,q1} =    {q2,q2} =    não
  {q0,q3}   {q1,q1} =    {q2,q4} ✔    SIM
  {q1,q2}   {q1,q1} =    {q3,q2} ·    não (ainda)
  {q1,q3}   {q1,q1} =    {q3,q4} ✔    SIM
  {q2,q3}   {q1,q1} =    {q2,q4} ✔    SIM

Rodada 3
  {q0,q1}   com b → {q2,q3} ✔ (marcado na rodada 2)  SIM
  {q1,q2}   com b → {q3,q2} ✔                        SIM
  {q0,q2}   a → {q1,q1}, b → {q2,q2}                 não

Rodada 4: nada mudou → PARE.
Sobrou {q0,q2} sem marca → q0 ≡ q2 (A ≡ C no livro).
```
- PASSOS (Os detalhes que valem ponto):
    1) Rodada 1: marque TODA casa da linha/coluna de q4 (final × não-final).
    2) Rodada 2: para cada casa vazia {p,q}, olhe {δ(p,a), δ(q,a)} e {δ(p,b), δ(q,b)}. Se algum destino é casa marcada, marque {p,q}.
    3) Se os destinos são o MESMO estado (como {q1,q1}), esse símbolo não diz nada.
    4) Na rodada 2, {q0,q1} vai com 'b' para {q2,q3}, que ainda estava vazio. Não marque ainda: anote e volte depois.
    5) Rodada 3: {q2,q3} agora está marcado, então {q0,q1} e {q1,q2} caem.
    6) Rodada 4: nenhuma casa nova. Pare. A casa vazia {q0,q2} diz: q0 ≡ q2.
- CÓDIGO (Funda q0 e q2. Cada classe vira um estado; a classe com o inicial é inicial; a com final é final.):
```
AFD mínimo (4 estados)

               a        b
  → {q0,q2}    {q1}     {q0,q2}
    {q1}       {q1}     {q3}
    {q3}       {q1}     {q4}
  * {q4}       {q1}     {q0,q2}
```
- WIDGET MINIMIZE entrada=«0» legenda: Confira cada casa e cada rodada com o motor.
- TEXTO: Agora é com você (exemplo esmaecido). AFD 'termina em b': q0 inicial; finais q1 e q3. Transições: q0 –a→ q2, q0 –b→ q1, q1 –a→ q2, q1 –b→ q3, q2 –a→ q0, q2 –b→ q3, q3 –a→ q0, q3 –b→ q1.
- REVELAR: Rodada 1: quais casas você marca?
    → As de final × não-final: {q0,q1}, {q0,q3}, {q1,q2} e {q2,q3}.
- REVELAR: Rodada 2: teste {q0,q2} e {q1,q3}.
    → {q0,q2}: com a → {q2,q0} (o próprio par, vazio); com b → {q1,q3} (vazio). {q1,q3}: com a → {q2,q0}; com b → {q3,q1}. Nenhum destino marcado: nada muda e o algoritmo para.
- REVELAR: Resultado final?
    → q0 ≡ q2 e q1 ≡ q3. AFD mínimo com 2 estados: 'a última letra foi b' (final) e 'não foi' (inicial).
- WIDGET MINIMIZE entrada=«2» legenda: Confira sua tabela.
- CAIXA [Dica de prova]: Escreva a rodada dentro de cada casa (1, 2, 3...) e, ao lado, o motivo ('b: {q2,q4}'). Se você errar uma casa, o professor vê o raciocínio e dá ponto parcial. E termine SEMPRE desenhando o AFD mínimo: muita gente faz a tabela e esquece a resposta.

#### ESTILO SOCRATIC (Perguntas guiadas): "Como provar que dois estados são diferentes?"
- PROF: Hoje você vai inventar o método da tabela respondendo perguntas. Comece por aqui: o que significa dois estados serem 'iguais'?
- PREVEJA: Qual a melhor definição de 'p e q são equivalentes'?
    opções: Têm as mesmas setas saindo | ✔Para TODA cadeia w, lendo w a partir de p termina em final se e só se lendo w a partir de q também termina | São ambos finais | Estão perto um do outro no desenho
    explicação: Equivalência é sobre COMPORTAMENTO futuro: nenhuma cadeia consegue separar os dois. Ter as mesmas setas é suficiente, mas não necessário.
- BYTE: Mas 'para toda cadeia' são infinitas cadeias! Como eu testo isso?
- PROF: Boa! Vire a pergunta do avesso: em vez de provar que são iguais, tente provar que são **diferentes**. Para isso basta UMA cadeia, uma testemunha.
- PREVEJA: Qual é a testemunha mais curta possível, e quem ela separa?
    opções: 'a', separa quem tem laço | ✔A cadeia vazia ε: separa todo final de todo não-final | 'ab', separa os iniciais
    explicação: Lendo nada, você já está no fim. Se p é final e q não, ε é a testemunha. Daí a rodada 1.
- PREVEJA: Suponha que {r, s} já está marcado com testemunha w. Se δ(p,'b') = r e δ(q,'b') = s, qual é a testemunha para {p, q}?
    opções: w | ✔'b' seguido de w | w seguido de 'b' | Não existe
    explicação: De p, ler 'b' leva a r; depois w separa r de s. Então 'b'w separa p de q. É esse o 'efeito dominó': cada rodada cria testemunhas uma letra mais longas.
- REVELAR: Por que o algoritmo precisa repetir as rodadas até nada mudar?
    → Porque marcar um par novo pode tornar possível marcar outros que dependiam dele. No AFD do livro, {q0,q1} só cai na rodada 3, depois que {q2,q3} foi marcado na rodada 2.
- REVELAR: Por que o algoritmo sempre termina?
    → Há um número finito de pares, e um par marcado nunca é desmarcado. Cada rodada útil marca pelo menos um; quando uma rodada não marca nada, as seguintes também não marcariam.
- PREVEJA: Um par sobreviveu a todas as rodadas sem marca. O que podemos concluir?
    opções: Talvez sejam diferentes, por uma cadeia longa que não testamos | ✔São equivalentes: se existisse testemunha, alguma rodada a teria encontrado | Precisamos testar à mão
    explicação: O algoritmo é completo: uma testemunha de tamanho n seria descoberta na rodada n + 1. Sem marca no fim = nenhuma testemunha existe.
- BYTE: E se uma transição está faltando? Eu comparo com o quê?
- REVELAR: Resposta
    → Complete o AFD com o estado morto ∅ (não final, laço em tudo) e compare normalmente. Faltar transição significa 'vai para o morto'; o ∅ é que permite marcar pares como {q1,q2} em a*b*.
- WIDGET MINIMIZE entrada=«5» legenda: Veja a testemunha 'a' separando q2 de q1 graças ao ∅.
- CAIXA [Conversa ao pé da lareira]: Estado q0: 'Eu e o q2 fazemos exatamente o mesmo trabalho há anos.' Estado q2: 'Pois é, ninguém nunca achou uma cadeia que nos separasse.' Minimizador: 'Então parabéns, vocês acabam de virar um estado só.'

#### ESTILO FORMAL (Definição de prova): "Minimização: definição de prova"
- TEXTO: Seja D = (S, Σ, δ, s₀, F) um AFD **completo** e com todos os estados alcançáveis. Estenda δ para cadeias: δ*(p, ε) = p e δ*(p, xw) = δ*(δ(p, x), w).
- TEXTO: **Equivalência**: p ≡ q se, para toda w ∈ Σ*, δ*(p, w) ∈ F ⇔ δ*(q, w) ∈ F. Caso contrário, p e q são **distinguíveis**, e uma w que os separa é uma **testemunha**. ≡ é uma relação de equivalência; suas classes são os estados do AFD mínimo.
- CÓDIGO (Algoritmo de preenchimento da tabela (pares distinguíveis)):
```
MinimizaPorTabela(D):
    remova os estados inalcançáveis a partir de s₀
    se faltar transição: adicione ∅ (não final, δ(∅, c) = ∅) e complete
    para cada par {p, q}:
        se (p ∈ F) ≠ (q ∈ F): marque {p, q}
    repita
        para cada par {p, q} não marcado:
            se existe c ∈ Σ com {δ(p,c), δ(q,c)} marcado:
                marque {p, q}
    até nenhuma marcação nova
    pares não marcados ⇒ p ≡ q; junte cada classe num estado
```
- TEXTO: **Construção do mínimo**: estados = classes [p]; δ'([p], c) = [δ(p, c)] (bem definido, porque estados equivalentes vão para estados equivalentes); inicial = [s₀]; finais = classes contidas em F. Se [∅] tiver só o ∅, pode ser omitido do desenho.
- CÓDIGO (Alternativa: refinar partições (o caminho do Livro do Dragão, Alg. 3.39)):
```
MinimizaPorPartições(D):        // Moore; Hopcroft é a versão otimizada
    Π ← { F, S − F }
    repita
        para cada grupo G de Π:
            divida G: p e q ficam juntos se, para todo c,
            δ(p,c) e δ(q,c) caem no MESMO grupo de Π
    até Π não mudar
```
- PASSOS (Partições no AFD do livro ((a|b)*abb)):
    1) Π₀ = { {q0,q1,q2,q3}, {q4} }
    2) Com 'b', q3 vai para {q4} e os outros ficam no grupo grande: Π₁ = { {q0,q1,q2}, {q3}, {q4} }
    3) Com 'b', q1 vai para {q3} e q0, q2 vão para o grupo grande: Π₂ = { {q0,q2}, {q1}, {q3}, {q4} }
    4) q0 e q2 têm destinos idênticos: Π não muda mais. Mesmo resultado da tabela: q0 ≡ q2.
- TEXTO: **Teorema (Myhill–Nerode)**: para cada linguagem regular existe um AFD com o número mínimo de estados, e ele é **único a menos de renomear os estados**. Por isso dois alunos que minimizam certo chegam ao mesmo autômato, só com nomes diferentes.
- PREVEJA: Qual afirmação é FALSA?
    opções: O AFD mínimo é único a menos de nomes | A tabela e as partições dão o mesmo AFD mínimo | ✔Um par que ficou sem marca numa rodada nunca mais pode ser marcado | Estados inalcançáveis devem ser removidos antes
    explicação: Falso: no AFD do livro, {q0,q1} fica em branco na rodada 2 e é marcado na rodada 3. Só no FIM (quando uma rodada inteira não marca nada) o branco vira veredito de equivalência.
- TEXTO: **Custo**: a tabela tem n(n − 1)/2 casas e cada rodada as varre todas: polinomial, ótimo para provas com até ~8 estados. O algoritmo de Hopcroft faz o mesmo em O(n · |Σ| · log n) e é o que geradores de léxico usam.
- CAIXA [Dica de prova]: Roteiro de prova: (1) inalcançáveis fora; (2) completar com ∅; (3) tabela em escadinha com a RODADA em cada casa; (4) repetir até estabilizar; (5) listar as classes; (6) desenhar o AFD mínimo com inicial e finais. Esquecer o passo 6 é perder metade da questão.
- REVELAR: Defina estados distinguíveis e diga o que a rodada 1 marca.
    → p e q são distinguíveis se existe w com δ*(p, w) ∈ F e δ*(q, w) ∉ F (ou vice-versa). A rodada 1 marca todo par final × não-final, cuja testemunha é ε.

#### ESTILO MNEMONIC (Truque de memória): "Marca, Propaga, Junta"
- CAIXA [Gancho de memória]: **MPJ: Marca, Propaga, Junta.** MARCA final × não-final. PROPAGA as marcas pelas setas, rodada após rodada. JUNTA quem sobrou sem marca. (Pense em 'MPJ = Ministério Público Julgando quem é igual'.)
- TEXTO: **Palácio da memória**: na Floresta Léxica, logo depois da Fábrica de Subconjuntos, fica a **Tabela dos Pares** : uma escada de pedra. Cada degrau tem casinhas, uma para cada par de estados. O guarda pinta de vermelho as casinhas dos pares diferentes; as que ficam brancas viram uma casa só.
- CAIXA [Gancho de memória]: **Escadinha, não quadrado.** {p,q} = {q,p} e {p,p} não interessa: desenhe só o triângulo de baixo. n estados → n(n−1)/2 casas. Com 5 estados: 10 casas.
- CAIXA [Gancho de memória]: **Final e não-final são água e óleo**: não se misturam nunca. Por isso a rodada 1 é automática.
- CAIXA [Gancho de memória]: **Dominó**: 'se os FILHOS são diferentes, os PAIS também são'. Se p e q, com a mesma letra, caem em casinha vermelha, pinte {p,q} de vermelho.
- PREVEJA: Na rodada 2, o par {q0,q1} vai com 'b' para {q2,q3}, que está em branco AGORA. O que fazer?
    opções: Marcar, porque provavelmente vai ser marcado | ✔Deixar em branco e voltar na próxima rodada | Fundir q0 e q1
    explicação: Dominó só cai empurrado por peça que JÁ caiu. Na rodada 3, {q2,q3} está vermelho e aí {q0,q1} cai também.
- CAIXA [Gancho de memória]: **O morto também vota.** O estado ∅ não fala, não aceita nada, mas DISTINGUE: sem ele, a*b* viraria (a|b)*. Antes de desenhar a escadinha, pergunte: 'falta alguma seta?'
- CAIXA [Cuidado!]: Os três tropeços clássicos: (1) **esquecer o ∅**; (2) **marcar com base em casinha ainda branca**; (3) **parar depois de uma rodada só**, sem conferir se o dominó continua.
- CAIXA [Gancho de memória]: A esteira da Floresta: **T**hompson **T**ransforma (ER → AFN), **S**ubconjuntos **S**implificam (AFN → AFD), **M**inimização **M**ata os repetidos (AFD → AFD mínimo). E o mínimo é **único**: 'cada linguagem tem uma única certidão de nascimento mínima'.
- WIDGET MINIMIZE entrada=«0» legenda: Recite MPJ enquanto o widget roda: marca (rodada 1), propaga (rodadas 2 e 3), junta (q0 com q2).
- REVELAR: Sem olhar: os 3 passos e os 3 tropeços.
    → Passos: Marca final × não-final; Propaga pelas setas até estabilizar; Junta os pares em branco. Tropeços: esquecer o estado morto, marcar com base em par ainda não marcado, e não repetir as rodadas.
- REVELAR: Contraste: o que a construção de subconjuntos faz e o que a minimização faz?
    → Subconjuntos tira o NÃO-DETERMINISMO (AFN → AFD), podendo criar estados sobrando. Minimização tira a REDUNDÂNCIA (AFD → menor AFD equivalente). Uma não substitui a outra.


## ASSUNTO LEX_FLEX: Lex/Flex: o arquivo .l

#### ESTILO STORY (História / Analogia): "A triagem dos Correios"
- NARRADOR: Centro de triagem dos Correios, madrugada. Uma esteira infinita de caracteres passa na frente de um robô. A Lexi segura uma folha plastificada cheia de regras.
- LEXI: Eu não separo nada na mão. Eu escrevo a FOLHA DE REGRAS, entrego para a fábrica, e a fábrica monta o robô que faz a triagem sozinho.
- PASSOS (Correios → Lex/Flex):
    1) A folha de regras ('pacote assim → carimbo tal') é o **arquivo .l**: cada linha é um **padrão** (expressão regular) e uma **ação** em C.
    2) A fábrica que lê a folha e monta o robô é o programa **lex** (ou **flex**): ele gera o arquivo **lex.yy.c**.
    3) O robô é a função **yylex()**: cada vez que alguém chama, ela pega o próximo pacote e devolve o carimbo.
    4) O pacote que o robô pegou é o **lexema**: fica em **yytext**, e o tamanho dele em **yyleng**.
    5) O carimbo é o **return TOKEN;** da ação. Ação vazia **{ }** é a lixeira: espaço e comentário somem.
- PROF: O robô obedece a DUAS regras de ouro. Primeira: ele é **guloso**: sempre pega o MAIOR pacote que alguma regra aceita. Segunda: se duas regras aceitam pacotes do mesmo tamanho, vence a que está **mais acima na folha**.
- WIDGET FLEX gramática=«%% ⏎ [ \t\n]+                { } ⏎ "if"                    { return IF; } ⏎ "while"                 { return WHILE; } ⏎ [a-z][a-z0-9]*          { return ID; } ⏎ [0-9]+                  { return NUM; } ⏎ "=="                    { return EQ; } ⏎ "="                     { return ATRIB; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ ";"                     { return PV; } ⏎ %%» entrada=«if (x1 == 10) y = 2;» legenda: Veja qual regra carimbou cada lexema. Edite a entrada e teste!
- PREVEJA: Com essa folha ("if" acima da regra de identificador), o que vira 'iffy'?
    opções: IF seguido de ID 'fy' | ✔Um único ID 'iffy' | Erro léxico | IF, IF, ID
    explicação: Guloso! A regra de ID aceita 4 caracteres; a do "if" só 2. O maior pacote vence, e a ordem nem chega a ser consultada.
- BYTE: E se alguém colar a regra de ID ACIMA da regra do "if"?
- WIDGET FLEX gramática=«%% ⏎ [ \t\n]+                { } ⏎ [a-z][a-z0-9]*          { return ID; } ⏎ "if"                    { return IF; } ⏎ [0-9]+                  { return NUM; } ⏎ %%» entrada=«if x 10» legenda: Folha com a ordem trocada: olhe o que aconteceu com o 'if'.
- REVELAR: Por que 'if' virou ID?
    → Empate: as duas regras aceitam 'if' com 2 caracteres. No empate vence a que vem PRIMEIRO, e agora é a de ID. Palavras-chave SEMPRE vêm antes da regra de identificador.
- CAIXA [No mundo real]: No trabalho prático, o analisador léxico costuma ser feito assim: você escreve o scanner.l, roda `lex scanner.l && gcc lex.yy.c` e testa com `./a.out < programa.txt`. O lex escreve centenas de linhas de C por você.
- CAIXA [Não existem perguntas idiotas]: P: Lex e Flex são a mesma coisa? R: Flex ('fast lex') é a versão livre e mais rápida do Lex original. A sintaxe do .l é praticamente igual; em muitos Linux, o comando `lex` já chama o flex.
- REVELAR: Na analogia, quem é: a folha, a fábrica, o robô, o pacote e o carimbo?
    → Arquivo .l, o programa lex/flex, a função yylex() gerada em lex.yy.c, o lexema (yytext) e o token devolvido pelo return.

#### ESTILO VISUAL (Passo a passo visual): "Flex ao vivo"
- LEXI: Cada widget abaixo é um scanner .l de verdade rodando. Embaixo de cada token aparece o número da regra que venceu. Mude a entrada à vontade!
- WIDGET FLEX gramática=«%% ⏎ [ \t\n]+                { } ⏎ "if"                    { return IF; } ⏎ "while"                 { return WHILE; } ⏎ [a-z][a-z0-9]*          { return ID; } ⏎ [0-9]+                  { return NUM; } ⏎ "=="                    { return EQ; } ⏎ "="                     { return ATRIB; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ ";"                     { return PV; } ⏎ %%» entrada=«while (n) n = n;» legenda: Espaços casam com a regra 1, que não tem return: somem.
- TEXTO: Repare em `==` e `=`: as duas regras começam com '='. Em `x == 1`, a regra de `==` casa 2 caracteres e vence. Em `y = 2`, só a de `=` casa.
- WIDGET FLEX gramática=«%% ⏎ " "+          { } ⏎ [a-z]+        { return ID; } ⏎ ">"           { return GT; } ⏎ ">="          { return GE; } ⏎ "="           { return ATRIB; } ⏎ "=="          { return EQ; } ⏎ %%» entrada=«a>=b c==d e=f» legenda: Desta vez ">" está ANTES de ">=" na folha. Mesmo assim, >= vira GE. Por quê?
- PREVEJA: Com essa mesma folha, como fica 'x===y'?
    opções: ID EQ EQ ID | ✔ID EQ ATRIB ID | ID ATRIB EQ ID | ID ATRIB ATRIB ATRIB ID
    explicação: Guloso, da esquerda para a direita: primeiro o maior pedaço que casa a partir do '=' é '==' (EQ). Sobra um '=' (ATRIB). Teste digitando no widget acima!
- PROF: Agora um scanner no estilo do trabalho prático, com comentário, número real e uma regra-coringa no fim para erro léxico.
- WIDGET FLEX gramática=«%{ ⏎ #include "y.tab.h" ⏎ int linha = 1; ⏎ %} ⏎ %% ⏎ [ \t]+                  { } ⏎ \n                      { linha++; } ⏎ "//".*                  { } ⏎ "int"                   { return INT; } ⏎ "float"                 { return FLOAT; } ⏎ "if"                    { return IF; } ⏎ "else"                  { return ELSE; } ⏎ [a-zA-Z_][a-zA-Z0-9_]*  { yylval.nome = strdup(yytext); return ID; } ⏎ [0-9]+"."[0-9]+         { yylval.real = atof(yytext); return REAL; } ⏎ [0-9]+                  { yylval.inteiro = atoi(yytext); return NUM; } ⏎ "<="                    { return LE; } ⏎ "<"                     { return LT; } ⏎ "="                     { return ATRIB; } ⏎ "+"                     { return MAIS; } ⏎ ";"                     { return PV; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ .                       { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); } ⏎ %% ⏎ int yywrap(void) { return 1; }» entrada=«float media = 3.14; // comentario» legenda: O comentário inteiro casa com "//".* e some. E 3.14 vira REAL, não NUM + erro + NUM.
- REVELAR: Por que '3.14' virou REAL e não NUM?
    → A regra de REAL casa '3.14' (4 caracteres) e a de NUM só '3' (1 caractere). Maior casamento vence, mesmo com NUM vindo depois na folha.
- WIDGET FLEX gramática=«%{ ⏎ #include "y.tab.h" ⏎ int linha = 1; ⏎ %} ⏎ %% ⏎ [ \t]+                  { } ⏎ \n                      { linha++; } ⏎ "//".*                  { } ⏎ "int"                   { return INT; } ⏎ "float"                 { return FLOAT; } ⏎ "if"                    { return IF; } ⏎ "else"                  { return ELSE; } ⏎ [a-zA-Z_][a-zA-Z0-9_]*  { yylval.nome = strdup(yytext); return ID; } ⏎ [0-9]+"."[0-9]+         { yylval.real = atof(yytext); return REAL; } ⏎ [0-9]+                  { yylval.inteiro = atoi(yytext); return NUM; } ⏎ "<="                    { return LE; } ⏎ "<"                     { return LT; } ⏎ "="                     { return ATRIB; } ⏎ "+"                     { return MAIS; } ⏎ ";"                     { return PV; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ .                       { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); } ⏎ %% ⏎ int yywrap(void) { return 1; }» entrada=«int x@ = 2;» legenda: O '@' cai na regra-coringa '.', que imprime o erro e NÃO devolve token. O scanner continua.
- CAIXA [Cuidado!]: A regra '.' tem que ser a ÚLTIMA. Ela casa qualquer caractere com tamanho 1; se viesse antes, ganharia todos os empates de tamanho 1, e engoliria `+`, `;` e até identificadores de uma letra!
- WIDGET FLEX gramática=«%% ⏎ .                 { } ⏎ [ ]+              { } ⏎ [a-z]+            { return ID; } ⏎ "+"               { return MAIS; } ⏎ %%» entrada=«x + ab» legenda: Prova: com '.' no topo, 'x' e '+' somem. Só 'ab' sobrevive, porque tem 2 letras.
- REVELAR: Quantos tokens saem de 'x + ab' nessa folha errada, e por quê?
    → Só 1: ID 'ab'. 'x' e '+' têm tamanho 1 e empatam com '.', que vem primeiro e joga fora. 'ab' tem tamanho 2 e o '.' não alcança.

#### ESTILO WORKED (Exemplo resolvido): "Escrevendo o scanner.l do TP"
- PROF: Vamos montar com calma um arquivo .l completo, do jeito que você entrega no trabalho prático. Ele compila sozinho, sem Bison.
- CÓDIGO (scanner.l: compile com: lex scanner.l && gcc lex.yy.c && ./a.out < teste.txt):
```
%{
/* scanner.l : analisador lexico do TP */
#include <stdio.h>
enum { INT = 1, FLOAT, IF, ELSE, ID, NUM, REAL,
       LE, LT, ATRIB, MAIS, PV, ABRE, FECHA };
int linha = 1;
%}
DIGITO   [0-9]
LETRA    [a-zA-Z_]
%%
[ \t]+                       { /* ignora espacos */ }
\n                           { linha++; }
"//".*                       { /* ignora comentario */ }
"int"                        { return INT; }
"float"                      { return FLOAT; }
"if"                         { return IF; }
"else"                       { return ELSE; }
{LETRA}({LETRA}|{DIGITO})*   { return ID; }
{DIGITO}+"."{DIGITO}+        { return REAL; }
{DIGITO}+                    { return NUM; }
"<="                         { return LE; }
"<"                          { return LT; }
"="                          { return ATRIB; }
"+"                          { return MAIS; }
";"                          { return PV; }
"("                          { return ABRE; }
")"                          { return FECHA; }
.                            { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); }
%%
int yywrap(void) { return 1; }

int main(void) {
    int t;
    while ((t = yylex()) != 0)
        printf("linha %d: token %d, lexema '%s' (%d chars)\n",
               linha, t, yytext, yyleng);
    return 0;
}
```
- PASSOS (Lendo o arquivo, parte por parte):
    1) **%{ … %}**: código C copiado literalmente para o topo de lex.yy.c, includes, a enumeração dos tokens e o contador de linha.
    2) **DIGITO [0-9]** e **LETRA [a-zA-Z_]**: definições nomeadas. Nas regras, usamos {DIGITO} e {LETRA} como apelidos.
    3) Primeiro **%%**: começam as regras. Cada linha: padrão, espaço, ação entre chaves.
    4) Espaços e comentários: ação sem return → são descartados. O **\n** só soma 1 em `linha`.
    5) Palavras-chave ("int", "float", "if", "else") ANTES da regra de identificador: ganham o empate.
    6) REAL antes de NUM por clareza (o maior casamento já resolveria). "<=" antes de "<", também por clareza.
    7) A regra **.** por último: qualquer caractere que sobrou é erro léxico, com o número da linha.
    8) Segundo **%%**: código C do usuário. **yywrap** devolvendo 1 avisa que não há outro arquivo; o **main** chama yylex() até ela devolver 0 (fim da entrada).
- TEXTO: Agora vamos 'rodar de cabeça' a linha `float media = 3.14; // comentario`.
- PASSOS (Resolvido: tokens da linha):
    1) 'float' → empate entre "float" e ID (5 caracteres): vence "float", que vem antes → **FLOAT**.
    2) ' ' → regra de espaços, ação vazia: nada.
    3) 'media' → só ID casa → **ID**, yytext = "media", yyleng = 5.
    4) '=' → **ATRIB**. '3.14' → REAL casa 4 caracteres, NUM só 1 → **REAL**.
    5) ';' → **PV**. '// comentario' → "//".* engole até o fim da linha → nada.
- WIDGET FLEX gramática=«%{ ⏎ #include "y.tab.h" ⏎ int linha = 1; ⏎ %} ⏎ %% ⏎ [ \t]+                  { } ⏎ \n                      { linha++; } ⏎ "//".*                  { } ⏎ "int"                   { return INT; } ⏎ "float"                 { return FLOAT; } ⏎ "if"                    { return IF; } ⏎ "else"                  { return ELSE; } ⏎ [a-zA-Z_][a-zA-Z0-9_]*  { yylval.nome = strdup(yytext); return ID; } ⏎ [0-9]+"."[0-9]+         { yylval.real = atof(yytext); return REAL; } ⏎ [0-9]+                  { yylval.inteiro = atoi(yytext); return NUM; } ⏎ "<="                    { return LE; } ⏎ "<"                     { return LT; } ⏎ "="                     { return ATRIB; } ⏎ "+"                     { return MAIS; } ⏎ ";"                     { return PV; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ .                       { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); } ⏎ %% ⏎ int yywrap(void) { return 1; }» entrada=«float media = 3.14; // comentario» legenda: Confira: FLOAT ID ATRIB REAL PV.
- TEXTO: Sua vez (exemplo esmaecido), com o mesmo scanner.
- REVELAR: Tokens de `intx = 3.5;`?
    → ID ATRIB REAL PV. 'intx' tem 4 letras: a regra de ID casa mais que a de "int" (3). Guloso!
- REVELAR: Tokens de `if (x <= 10) x = x + 1;`?
    → IF ABRE ID LE NUM FECHA ID ATRIB ID MAIS NUM PV, 12 tokens. '<=' é um token só.
- REVELAR: O que acontece com `int x@ = 2;`?
    → Tokens INT ID ATRIB NUM PV, e o scanner imprime 'Erro lexico na linha 1: '@''. A regra '.' não tem return, então o '@' não vira token e a análise continua.
- WIDGET FLEX gramática=«%{ ⏎ #include "y.tab.h" ⏎ int linha = 1; ⏎ %} ⏎ %% ⏎ [ \t]+                  { } ⏎ \n                      { linha++; } ⏎ "//".*                  { } ⏎ "int"                   { return INT; } ⏎ "float"                 { return FLOAT; } ⏎ "if"                    { return IF; } ⏎ "else"                  { return ELSE; } ⏎ [a-zA-Z_][a-zA-Z0-9_]*  { yylval.nome = strdup(yytext); return ID; } ⏎ [0-9]+"."[0-9]+         { yylval.real = atof(yytext); return REAL; } ⏎ [0-9]+                  { yylval.inteiro = atoi(yytext); return NUM; } ⏎ "<="                    { return LE; } ⏎ "<"                     { return LT; } ⏎ "="                     { return ATRIB; } ⏎ "+"                     { return MAIS; } ⏎ ";"                     { return PV; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ .                       { printf("Erro lexico na linha %d: '%s'\n", linha, yytext); } ⏎ %% ⏎ int yywrap(void) { return 1; }» entrada=«if (x <= 10) x = x + 1;» legenda: Confira a segunda resposta (e teste as outras digitando).
- CAIXA [Dica de prova]: Sem `yywrap` (ou `%option noyywrap`), o gcc reclama de 'undefined reference to yywrap'. Alternativa: compilar com `-ll` (lex) ou `-lfl` (flex), que trazem uma yywrap pronta.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que o .l é desse jeito?"
- PROF: Hoje você vai descobrir as regras do Flex respondendo perguntas. Chute à vontade.
- PREVEJA: Na folha abaixo, "<" vem ANTES de "<=". O que vira 'a<=b'?
    opções: ID LT ATRIB ID | ✔ID LE ID | Erro: falta a regra do '=' | ID LT ID
    explicação: O robô é guloso: "<=" casa 2 caracteres, "<" só 1. A ordem só desempata lexemas do MESMO tamanho.
- WIDGET FLEX gramática=«%% ⏎ [ ]+              { } ⏎ [a-z]+            { return ID; } ⏎ "<"               { return LT; } ⏎ "<="              { return LE; } ⏎ %%» entrada=«a<=b c<d» legenda: Confira: LE em 'a<=b', LT em 'c<d'.
- BYTE: Então a ordem das regras nunca importa?
- PREVEJA: Quando a ordem das regras IMPORTA?
    opções: Nunca | Sempre | ✔Só quando duas regras casam lexemas do mesmo tamanho | Só para números
    explicação: Empate de tamanho é o único caso. O exemplo clássico: "if" e a regra de ID, ambas casando 'if'.
- PREVEJA: Uma regra sem `return`, como `[ \t]+ { }`, faz o quê?
    opções: Devolve token vazio | Para o scanner | ✔Descarta o lexema e yylex() continua procurando o próximo | Dá erro de compilação
    explicação: yylex() é um laço: só sai quando uma ação executa return. Sem return, ela joga o lexema fora e segue lendo.
- PREVEJA: O sintático precisa saber o VALOR de um número (42, não só 'NUM'). Por onde ele chega?
    opções: Pelo return | ✔Por yylval, preenchido na ação: yylval = atoi(yytext); | Por yyleng | O sintático lê o arquivo de novo
    explicação: O return leva só a CLASSE (o token). O atributo vai numa variável global combinada com o Bison: yylval.
- PREVEJA: Se a regra '.' (qualquer caractere) ficar no TOPO da folha, o que acontece com 'x + ab'?
    opções: Nada muda | ✔x e + somem; só 'ab' vira ID | Tudo some | Erro de compilação
    explicação: x e + têm tamanho 1: empatam com '.', que vem primeiro e descarta. 'ab' tem tamanho 2, então a regra de ID vence pelo tamanho.
- WIDGET FLEX gramática=«%% ⏎ .                 { } ⏎ [ ]+              { } ⏎ [a-z]+            { return ID; } ⏎ "+"               { return MAIS; } ⏎ %%» entrada=«x + ab» legenda: Confira o estrago.
- REVELAR: Como o Flex consegue achar o MAIOR casamento entre dezenas de regras de uma vez?
    → Ele junta os AFNs de todas as regras num só (um novo estado inicial com ε para cada um), converte para AFD e roda o AFD até travar no estado morto, lembrando o ÚLTIMO estado final por onde passou. Esse final diz o tamanho do lexema e a regra (a de menor número, em caso de empate).
- REVELAR: E por que o resultado é tão rápido?
    → Porque é um AFD: um passo por caractere, uma consulta na tabela. Toda a dificuldade (subconjuntos, minimização) foi paga quando o lex gerou o lex.yy.c.

#### ESTILO FORMAL (Definição de prova): "A especificação Lex: definição de prova"
- TEXTO: Um programa Lex tem três seções separadas por `%%`: **declarações** (código C entre `%{` e `%}` e definições regulares nome → ER), **regras de tradução** na forma `Padrão { Ação }` e **funções auxiliares** em C.
- CÓDIGO (Estrutura de um arquivo .l):
```
declarações
%%
regras de tradução
%%
funções auxiliares
```
- CÓDIGO (Expressões regulares do Lex (Livro do Dragão, Fig. 3.8)):
```
EXPRESSÃO  CASA COM                         EXEMPLO
c          o caractere c (não operador)     a
\c         o caractere c literalmente       \*
"s"        a cadeia s literalmente          "**"
.          qualquer caractere menos \n      a.*b
^          início de linha                  ^abc
$          fim de linha                     abc$
[s]        qualquer caractere de s          [abc]
[^s]       qualquer caractere fora de s     [^abc]
r*         zero ou mais r                   a*
r+         um ou mais r                     a+
r?         zero ou um r                     a?
r{m,n}     de m a n ocorrências de r        a{1,5}
r1r2       r1 seguido de r2                 ab
r1|r2      r1 ou r2                         a|b
r1/r2      r1, se seguido de r2             abc/123
```
- TEXTO: **Resolução de conflitos** (Dragão, 3.5.3): (1) prefira sempre o **prefixo mais longo** da entrada que casa com algum padrão; (2) se o prefixo mais longo casa com dois ou mais padrões, prefira o **listado primeiro**.
- PASSOS (Interface do analisador gerado):
    1) **yylex()**: devolve o código do próximo token; devolve 0 no fim da entrada.
    2) **yytext**: ponteiro para o início do lexema reconhecido; **yyleng**: seu comprimento.
    3) **yylval**: variável global com o valor de atributo do token, compartilhada com o analisador sintático (Yacc/Bison).
    4) **yyin**: arquivo de entrada (padrão: stdin). **yywrap()**: chamada no fim do arquivo; devolver 1 significa 'acabou'.
- TEXTO: **Implementação**: o Lex constrói um AFN para cada padrão pᵢ, cria um novo estado inicial com transições ε para cada um, e converte o AFN combinado em AFD. Ao ler a entrada, o AFD segue até não haver transição; então recua até o último estado de aceitação visto. Se esse estado contém finais de vários padrões, vale o de menor índice.
- PREVEJA: Com as regras `"if" {…}` e `[a-z]+ {…}` nessa ordem, a entrada 'ifa' produz:
    opções: o token de "if" e depois o lexema 'a' | ✔um único lexema 'ifa', da regra [a-z]+ | erro: conflito
    explicação: Regra 1 de resolução: prefixo mais longo. [a-z]+ casa 3 caracteres; "if" casa 2.
- PREVEJA: Qual é o papel do operador `/` em `DO/({letra}|{digito})*=…`?
    opções: Divisão | ✔Lookahead: só casa DO se o que vem depois casa com o resto, mas o resto não entra no lexema | Comentário | Alternativa, como |
    explicação: É o operador de contexto à direita (lookahead), usado no exemplo do Fortran, onde DO5I=1.25 é atribuição e DO5I=1,25 é laço.
- CAIXA [Dica de prova]: Em prova discursiva: cite as 3 seções, as 2 regras de resolução de conflito (mais longo; empate → primeiro) e explique que o Lex gera um AFD a partir das ERs (Thompson + subconjuntos). Bônus: palavras-chave antes de identificadores.
- REVELAR: Escreva a regra de um identificador que devolve ID e guarda o nome para o sintático.
    → [a-zA-Z_][a-zA-Z0-9_]*   { yylval.nome = strdup(yytext); return ID; }, strdup copia, porque yytext será sobrescrito no próximo token.

#### ESTILO MNEMONIC (Truque de memória): "Guloso, mas educado"
- CAIXA [Gancho de memória]: **D-R-C**: **D**efinições %% **R**egras %% **C**ódigo. Igualzinho ao Bison: Declara, Regra, Código. Os dois %% são as duas paredes da sala das regras.
- CAIXA [Gancho de memória]: O Flex é **guloso, mas educado**: GULOSO porque sempre come o maior pedaço; EDUCADO porque, no empate, respeita a fila, quem chegou primeiro no arquivo é atendido.
- CAIXA [Gancho de memória]: **Palavra-chave é VIP**: entra ANTES do ID na folha. Se o ID passar na frente, 'if' vira variável e o seu TP quebra.
- TEXTO: **Palácio da memória**: na Floresta Léxica, na beira do rio, fica a **Oficina Flex** . Na porta está pregada a folha D-R-C. Lá dentro, a Lexi alimenta a máquina com expressões regulares e sai pelo outro lado um robô chamado lex.yy.c.
- CAIXA [Gancho de memória]: Tudo do lex começa com **yy**: **yytext** = o TEXTO do lexema; **yyleng** = o LENGth (tamanho); **yylval** = o VALor que vai para o Bison; **yylex** = o próprio LEXer; **yywrap** = 'embrulha' (acabou o arquivo?).
- CAIXA [Gancho de memória]: **{ } vazio = lixeira.** Sem return, o lexema vai para o lixo e o robô continua. É assim que espaços e comentários somem.
- PREVEJA: yytext ou yylval: onde está o texto "42" logo depois de casar `[0-9]+`?
    opções: ✔yytext | yylval | yyleng | yylex
    explicação: yytext é o TEXTO cru. yylval só tem o valor se a SUA ação fizer yylval = atoi(yytext).
- WIDGET FLEX gramática=«%% ⏎ [ \t\n]+                { } ⏎ "if"                    { return IF; } ⏎ "while"                 { return WHILE; } ⏎ [a-z][a-z0-9]*          { return ID; } ⏎ [0-9]+                  { return NUM; } ⏎ "=="                    { return EQ; } ⏎ "="                     { return ATRIB; } ⏎ "("                     { return ABRE; } ⏎ ")"                     { return FECHA; } ⏎ ";"                     { return PV; } ⏎ %%» entrada=«iffy = while2;» legenda: Guloso: 'iffy' e 'while2' são IDs. Educado: em 'if' sozinho, a regra de cima ganharia.
- CAIXA [Cuidado!]: Confusões clássicas: (1) achar que a ordem resolve TUDO, o tamanho vem antes; (2) pôr a regra '.' no começo; (3) esquecer yywrap/noyywrap; (4) devolver o token do espaço em vez de ignorar; (5) confundir lex.l (o que você escreve) com lex.yy.c (o que o lex gera).
- CAIXA [No mundo real]: Receita do TP para nunca esquecer: `lex scanner.l` → `gcc lex.yy.c` → `./a.out < teste.txt`. Com Bison: `yacc -d parser.y` gera y.tab.h com os tokens, que o scanner.l inclui no %{ %}.
- REVELAR: Recite as duas regras de ouro e o mnemônico das seções.
    → 1) Maior casamento (guloso). 2) Empate → regra listada primeiro (educado). Seções: D-R-C, Definições %% Regras %% Código.
