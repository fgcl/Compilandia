# Relatório do aluno — Regiões 1 (Porto) e 2 (Floresta Léxica)

## Como foi estudar isto

Eu continuaria, mas pelas aulas de texto, não pelas animadas. A primeira aula (Fases) é clara e não assusta, e isso pesa para quem chega com medo. Só que ela acaba sem gancho: o resumo fecha em "Cada fase pega um tipo de erro" e nada me chama para a Floresta. O que me deu vontade de seguir foi a prática "A câmera entra na máquina" ("Dois destes caracteres formam um token só").
O que mais me travou: (1) em Tokens saí sem saber se token é o pedaço ou a classe; (2) em Minimização a cena 8 ensina com a mesma frase que a cena 18 chama de erro; (3) as fichas e os Bugs cobram coisas que a aula animada não ensina (Thompson, estado morto, yytext, aⁿbⁿ); (4) AFN→AFD nunca me faz aplicar o fecho depois do move, que a própria aula diz ser o erro clássico.
As aulas animadas têm tom seco e igual; as de texto têm graça e cobrem a prova.

## Onde eu travei

**Fases**
- Média. Pegadinha, cenas 2 e 4: "O scanner para antes de qualquer outra fase" e "Só ela consulta a tabela de símbolos". Nem "scanner" nem "tabela de símbolos" apareceram na aula. Trocaria por "A análise léxica para…" e "Só ela confere se o nome foi declarado".
- Média. Aula, cena 7: a tela só mostra a palavra "árvore". Nunca vejo uma árvore de `x = a + 2`. Sei árvore de ED, mas não sei quem é raiz e quem é filho aqui. Desenhar `=` em cima, `x` e `+` embaixo.
- Média. A metade "síntese" tem duas cenas (10 e 11) e uma pergunta (18). "A otimização melhora esse código sem mudar o resultado": melhora o quê? Falta um antes/depois. Já o trio léxico/sintático/semântico volta nas cenas 9, 13, 14, 17, 20, 21, 22, na pegadinha inteira e nos seis Bugs.
- Baixa. Cena 18: "o laço vira desvios", sem dizer o que é desvio. Dica da cena 20: "A gramática aceita o comando", palavra nunca vista.

**Tokens**
- Alta. Cena 5: "Cada pedaço vira um token". Cena 10: "A classe dele é o token". Cena 20, na mesma tela: "x>=10 dá três tokens: x, >= e 10" e "Token é a classe (ID)". Glossário: "token: Um pedaço com nome". Ficha: "Achar que `x` e `y` são tokens diferentes" é erro. Então `x = y` tem 3 tokens ou 2? Eu erraria na prova. Frase que eu poria na cena 10: "Na contagem, cada pedaço cortado é um token. Cada token tem um nome de classe (ID, NUM) e um texto (o lexema). x e y são dois tokens da mesma classe."
- Média. Cena 1, primeira frase da aula: "O parser trabalha com palavras". "Parser" não existe na Região 1 (lá é "análise sintática"). Diria: "A análise sintática (o parser) trabalha…".
- Média. Receita, passo 3: "Dê a classe: palavra-chave, ID, NUM, OP...". Qual é a classe de `(` e de `;`? Na captura da cena 6 o `;` cai na caixa OP sem comentário.
- Baixa. "palavra-chave" (cena 11), "palavra reservada" (13), "reservada" (pegadinha): escolher um nome.

**Expressões regulares e autômatos**
- Alta. A aula promete e não entrega. Cena 2: "Nome não começa com dígito. Falta um jeito curto e exato de escrever essa regra." A regra dos nomes nunca é escrita; os exemplos são `a b*` e `a b* c`. Fechar com `letra (letra|dígito)*` e perguntar se `1x` passa.
- Alta. Só aprendo a rodar um autômato pronto. Ninguém mostra como sair da expressão e chegar no desenho, e a trilha tem "Programar o Byte", "Trilho que você constrói" e "Fábrica de Autômatos (montar o autômato)".
- Alta. Degrau: a barra aparece numa meia frase (cena 14: "A barra quer dizer ou"), sem exemplo e sem parênteses. A etapa seguinte abre com "# (a|b)*abb… aceita as palavras que terminam em abb". Tenho que acreditar.
- Média. Vocabulário: "cadeia" (abertura, cena 3) nunca é explicada; depois vira "palavra" (Tabelas) e "frase" (prática: "A frase foi aceita"). Cena 5: "É um AFD", sigla nunca aberta e sem contraste (o AFN só vem na aula seguinte). Cena 14: "ε" na tela, o Byte não fala dele. Tabelas, cena 5: "δ" sem explicação, e o asterisco passa a marcar estado final logo depois de eu aprender que `*` é repetição.
- Média. Texto WORKED deste assunto é "Construção de subconjuntos na mão", com "Dtran[T, x] = ε-fecho(move(T, x))". É a matéria da aula seguinte.

**AFN → AFD**
- Alta. Cena 20: "Outro erro: esquecer o épsilon fecho depois de ler o símbolo." Em nenhum dos dois exemplos, nem na pegadinha, o fecho depois do move acrescenta estado. Trocaria o segundo exemplo pelo `(ab | a)c` do texto WORKED.
- Alta. O que escrevo quando nenhum estado do conjunto tem seta? Só a dica da cena 7 encosta nisso ("O dois não anda"). No segundo exemplo ninguém pergunta `{0,1,3}` lendo b. A ficha cobra: "move(T, a) vazio leva a quê? → Ao estado morto ∅".
- Média. Por que existe AFN? A aula começa com "Este é um AFN". Se o AFD é o que funciona, quem desenharia um AFN? A resposta ("é muito mais fácil de construir a partir de uma ER") só está num texto do assunto anterior.
- Média. Degrau nas cenas 10–11: a receita põe "ε-fecho" e "(move)" na tela depois de um exemplo sem ε, e "move" nunca é dito em voz alta.
- Média. Cena 8: quatro setas entram de uma vez e, na captura, as de {0,1} e {0,2} se sobrepõem. Falta a tabela conjunto × símbolo, que é o que a prova pede.

**Minimização**
- Alta. Cena 8: "Se os destinos são diferentes, as origens também são. Marco A com B." Cena 18: "A e B vão para estados diferentes com b. Então A e B são diferentes" → errado. É quase a mesma frase. Cena 8 nova: "O par de destinos (C, D) já tem X. Por isso A e B ganham X."
- Alta. A ficha diz que o erro número um é "Esquecer de completar o AFD com o estado morto" e o gancho é "O morto também vota". A aula não fala em estado morto e o primeiro exemplo tem B, C e D sem seta de a.
- Média. Cena 8: a tela marca dois X (A,B e A,C) e o Byte só justifica um.
- Média. Nenhum exemplo tem par que só cai numa rodada posterior, embora a cena 13 avise disso. E o AFD mínimo nunca é desenhado ("B e C viram um estado só" e acabou): para onde vão as setas do estado fundido?
- Média. Sem "por quê": a aula abre em "# AFD", não em "Para que serve?". O motivo ("Minimizar é a faxina depois da mudança", "tabela menor") só está no texto STORY.
- Baixa. Cena 4: "reconhece a mesma linguagem". "Linguagem" nunca foi definida; diria "aceita exatamente as mesmas cadeias".

**Lex/Flex**
- Média. Cena 3: `[a-z]+`, `[0-9]+`, `[ \t\n]` e `"if"` entram sem explicação. A aula de regex só ensinou letra, `*`, `|`, `+`, `?`. Uma cena: "[a-z] é qualquer letra de a até z. Texto entre aspas casa exatamente aquele texto."
- Média. Bug do álbum: "yytext guarda a classe do token". yytext não aparece na aula nem na pegadinha.

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| scanner | Fases, pegadinha 2 | Tokens, cena 3 (depois) |
| tabela de símbolos | Fases, pegadinha 4 e ficha | só no texto STORY |
| gramática | Fases, cena 20 (dica) | nunca |
| parser | Tokens, cena 1 | nunca |
| padrão, atributo, prefixo | Tokens, ficha | só nos textos |
| cadeia | Regex, abertura | nunca |
| AFD (sigla) | Regex, cena 5 | texto STORY |
| ε (cadeia vazia) | Regex, cena 14 | nunca |
| parênteses, precedência | Regex, Tabelas 1 e ficha | texto FORMAL |
| Thompson | Regex, ficha | nunca com exemplo |
| linguagem regular, aⁿbⁿ, livre de contexto | Regex, Bugs | só nos textos |
| move | AFN→AFD, cena 10 | só nos textos |
| estado morto, ∅ | AFN→AFD e Minimização, fichas | só nos textos |
| `[a-z]`, aspas | Flex, cena 3 | texto FORMAL |
| yytext, yylex, yylval, `%%`, Bison | Flex, Bugs e ficha | só nos textos |

## Prova de bolso

| Assunto | Exercício | Resultado | O que faltou |
|---|---|---|---|
| Fases | Classificar três erros; dar a saída de cada fase para `a = b + 3` | Em parte | Erros, sim. Saídas, não: nunca vi a árvore nem os tokens escritos |
| Tokens | Cortar e classificar `total = x1 * 10; // soma` | Em parte | Corto certo. Classe de `;`; como contar; `3.14` e string |
| Regex | Escrever a ER de identificador e desenhar o AFD | Não | Só sei rodar autômato pronto; `\|` e parênteses sem treino |
| AFN→AFD | Converter AFN com ε no meio do caminho | Em parte | Fecho depois do move nunca praticado; conjunto vazio; tabela |
| Minimização | Minimizar o AFD de 5 estados de `(a\|b)*abb` | Em parte | Várias rodadas; desenhar o resultado; transição faltando |
| Flex | Dizer os tokens de uma entrada num `.l` dado; escrever o `.l` do TP | Consigo / Não | Rastrear, sim. Escrever, não: seções, yytext, compilar |

Com os textos WORKED de AFN→AFD, Minimização e Flex eu passaria para "consigo" nos três.

## Diversão e ritmo

Funcionou:
- Tokens, prática: o brinquedo com as teclas "Mais uma letra" e "Cortar aqui". Eu viro o scanner.
- Tokens, pegadinha "ifa", e depois a abertura do Flex ("Vira ID. No empate, ganha a regra que vem primeiro"): descobri que "a reservada ganha" era só a ordem do arquivo. Foi o melhor clique do bloco.
- AFN→AFD, cena 2: a resposta certa é "as duas". Surpresa boa.
- Flex, cenas 11–15: "com um erro escondido. Quem roda agora é você". Depurar arquivo é o que vou fazer no TP.
- Textos: "suco de feijoada", o grupo do WhatsApp, e q0 e q2 ao pé da lareira ("vocês acabam de virar um estado só").

Cansou:
- 50 das 53 respostas certas começam com "Isso.".
- Em "Erros comuns", o "colega" está errado nas seis aulas. Na terceira eu já tocava em "errado" sem ler.
- Minimização, cenas 3 a 8: seis cenas só ouvindo, com a definição de equivalência, a tabela e a propagação. Fases tem dois trechos assim (3–8 e 10–15).
- Personagens: nas trilhas o Byte tem 193 falas e a professora 11. O Bug é só manchete. A Lexi e a katana surgem nos textos sem apresentação. O Byte só tem personalidade nos textos ("Peraí… o que uma lanchonete tem a ver com compilador?"). Nada soa infantil; as animadas é que são secas.
- Tela (seis capturas): legível. O "não entendi" é pequeno e apagado, e é o botão de que eu mais preciso.

Textos: eu abriria STORY em Fases e AFN→AFD, e WORKED em Tokens, Minimização e Flex. Pularia todos os FORMAL (Σ, δ*, "T ∩ F ≠ ∅"). O WORKED de Fases usa `⟨id,1⟩`, `inttofloat` e `LDF R2, id3` sem explicar.

## Nota por assunto

| Assunto | Clareza | Diversão | Pronto para a prova | Em uma frase |
|---|---|---|---|---|
| Fases | 4 | 3 | 3 | Clara, repete demais os três erros e esquece a síntese |
| Tokens | 3 | 4 | 3 | A mais interativa; token é pedaço ou classe? |
| Regex e autômatos | 3 | 4 | 2 | Sei pilotar o trem, não sei construir o trilho |
| AFN→AFD | 3 | 3 | 2 | Entendi os conjuntos; fecho e vazio ficaram no ar |
| Minimização | 2 | 2 | 2 | A cena 8 contradiz a 18; sem o texto eu não faço |
| Flex | 4 | 4 | 3 | Boa para rastrear; para o TP, só o texto |

## Desconfio que está errado

- Fases, pegadinha 4: "Só ela consulta a tabela de símbolos" × ficha: "com a tabela de símbolos acompanhando todas as fases".
- Fases: a aula diz "seis fases"; o texto FORMAL lista sete linhas, com dois otimizadores.
- Tokens, cenas 10 e 20: "Token é a classe" × glossário "Um pedaço com nome" × ficha "Um par ⟨nome, atributo⟩".
- Tokens, cena 6: `;` na caixa OP; o texto WORKED chama de "delimitador".
- Minimização, aula, exemplo 1: AFD incompleto usado sem estado morto, o que a ficha chama de erro.
- Minimização, texto SOCRATIC: "Ter as mesmas setas é suficiente". E se um for final e o outro não?
- AFN→AFD, cena 18: "O AFD tem três estados". O texto diz "sem contar o morto".

## As 5 coisas que eu mudaria primeiro

1. Tokens: decidir numa frase se token é o pedaço ou a classe, e usar igual na aula, no glossário e na ficha.
2. Minimização: reescrever a cena 8, justificar o segundo X, mostrar um par que só cai na rodada seguinte, completar com o estado morto e desenhar o AFD mínimo.
3. AFN→AFD: trocar o segundo exemplo por um em que o fecho depois do move muda o conjunto e em que aparece um conjunto vazio; uma cena dizendo de onde vem o AFN.
4. Regex: escrever a regra dos nomes no fim, treinar `|` e parênteses antes de `(a|b)*abb`, e alinhar ficha e Bugs com a aula (ou ensinar Thompson com um exemplo).
5. Vocabulário e tom: apresentar "parser", "cadeia" e "AFD" na primeira vez; terminar a aula 1 com o gancho da cena 19 ("O mapa do jogo segue esse mesmo caminho") apontando para a Floresta; variar o "Isso." e deixar o colega acertar às vezes.
