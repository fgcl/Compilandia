# Região 8: Cidade Semântica — Significado, tipos e escopos
(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)


## ASSUNTO ATTRIBUTES_TYPES: Atributos e verificação de tipos
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Tipos sobem pela árvore» → Fazer com ajuda → Prática: você pilota «Suba os tipos» → Prática: você pilota «O tipo que desce» → Pegadinha «Sintetizado sobe, herdado desce» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Árvore Anotada (calcular atributos de baixo para cima), Fábrica de AST (montar a AST com new BinOp(...))
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug apagou um tipo
Na tela: 2 + 1.5
BYTE: "Antes da aula: qual é o tipo do resultado?"
Opções: int | ✔float | erro
Depois: "Float. O compilador converte o inteiro antes da conta."

#### AULA ANIMADA BÁSICA: "Tipos sobem pela árvore" (20 cenas, ~158s) [1º exemplo começa na cena 4]

1. TELA: # Tipos sobem pela árvore ⏎ a + x ⏎ add add.s ⏎ int + int float + float
   BYTE: "Na máquina, somar inteiros e somar reais são instruções diferentes."

2. TELA: # Tipos sobem pela árvore ⏎ *a + x* ⏎ *add* *add.s* ⏎ int + int float + float
   BYTE: "Para escolher a instrução certa, o compilador precisa saber o tipo de cada pedaço da expressão."

3. TELA: !!ATRIBUTO!!
   BYTE: "Um atributo é uma informação associada a um nó da árvore, como o tipo ou o valor. As regras que o calculam vêm junto com a gramática."

4. TELA: # a + x * 2 ⏎ + ⏎ a * ⏎ x 2 ⏎ a é int, x é float
   BYTE: "A árvore já existe. Agora a análise semântica calcula os atributos dela. Aqui, o atributo é o tipo."

5. TELA: # a + x * 2 ⏎ + ⏎ a * ⏎ int ⏎ x 2 ⏎ float int ⏎ a é int, x é float
   BYTE: "Começo pelas folhas. O tipo de a e de x vem da declaração. O dois, sem ponto, é inteiro."

6. TELA: # a + x * 2 ⏎ + ⏎ a *** ⏎ int ⏎ x 2 ⏎ float int ⏎ a é int, x é float ⏎ int float
   BYTE: "Real vezes inteiro dá o quê? Toque no tipo."
   PERGUNTA (toque): certo = float ; errado = int
   se errar: "O tipo maior ganha, para não perder a parte quebrada."
   se acertar: "Isso. Inteiro com real dá real."

7. TELA: # a + x * 2 ⏎ + ⏎ a * ⏎ int float ⏎ x 2 ⏎ float int ⏎ a é int, x é float
   BYTE: "O dois é convertido para real antes da conta. Essa conversão automática se chama coerção. O tipo real sobe para o nó do vezes."

8. TELA: # a + x * 2 ⏎ *+* ⏎ float ⏎ a * ⏎ int float ⏎ x 2 ⏎ float int ⏎ a é int, x é float
   BYTE: "Na raiz, inteiro mais real também dá real. Atributo que o nó calcula a partir dos filhos é um atributo sintetizado."

9. TELA: # a + x * 2 ⏎ + ⏎ float ⏎ a * ⏎ int float ⏎ x 2 ⏎ float int ⏎ a é int, x é float
   PROF: "Se os tipos não combinam e não há coerção possível, o compilador acusa erro semântico."

10. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ 3. Conta: um filho float dá float. Senão, int. ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "A verificação de tipos sobe das folhas até a raiz, um nó por vez."

11. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ 3. Conta: um filho float dá float. Senão, int. ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "No primeiro exemplo, o x era float. Que regra disse isso? Toque."
   PERGUNTA (toque): certo = 2. Variável: o tipo vem da tabela de símbolos. ; errado = 1. Número com ponto é float. Sem ponto, int. | 3. Conta: um filho float dá float. Senão, int. | 4. Comparação dá bool. Bool numa conta é erro.
   se errar: "O x é uma variável. Onde mora o tipo dela?"
   se acertar: "Isso. A declaração do x ficou na tabela."

12. TELA: # Receita ⏎ 1. Número com ponto é float. Sem ponto, int. ⏎ 2. Variável: o tipo vem da tabela de símbolos. ⏎ *3. Conta: um filho float dá float. Senão, int.* ⏎ 4. Comparação dá bool. Bool numa conta é erro.
   BYTE: "No passo três, o int que encontra um float sofre coerção: vira float antes da conta."

13. TELA: # c * 2 + y ⏎ + ⏎ * y ⏎ float ⏎ c 2 ⏎ int int
   BYTE: "Outra conta. As folhas já têm tipo, pelas regras um e dois."

14. TELA: # c * 2 + y ⏎ + ⏎ *** y ⏎ float ⏎ c 2 ⏎ int int ⏎ int float bool
   BYTE: "Qual é o tipo de c vezes dois? Toque."
   PERGUNTA (toque): certo = int ; errado = float | bool
   se errar: "Os dois filhos são int. Nenhum é float."
   se acertar: "Isso. Inteiro com inteiro fica inteiro."

15. TELA: # c * 2 + y ⏎ *+* ⏎ * y ⏎ int float ⏎ c 2 ⏎ int int ⏎ int float bool
   BYTE: "E na raiz, inteiro mais real? Toque no tipo."
   PERGUNTA (toque): certo = float ; errado = int | bool
   se errar: "Regra três: basta um filho float."
   se acertar: "Isso. O inteiro vira real antes da soma."

16. TELA: # c * 2 + y ⏎ + ⏎ * *ok* ⏎ int bool ⏎ c 2 ⏎ int int ⏎ int float erro
   BYTE: "Agora o y deu lugar ao ok, que é bool. Que tipo sobe?"
   PERGUNTA (toque): certo = erro ; errado = int | float
   se errar: "Regra quatro: bool não entra em conta."
   se acertar: "Isso. A raiz fica com erro de tipo."

17. TELA: # c * 2 + y ⏎ *+* ⏎ erro ⏎ * ok ⏎ int bool ⏎ c 2 ⏎ int int
   BYTE: "O erro sobe até a raiz, e o compilador avisa. Na etapa de prática, você vê o outro tipo de atributo: o herdado, que desce."

18. TELA: # Erros comuns ⏎ Atributo sintetizado é o que o nó recebe do pai. ⏎ certo errado
   BYTE: "De onde vem o atributo sintetizado? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "Nesta aula, os tipos subiram ou desceram pela árvore?"
   se acertar: "Isso. Sintetizado sobe: o nó calcula a partir dos filhos. O que vem do pai ou dos irmãos é o atributo herdado."

19. TELA: # Erros comuns ⏎ Certo: Sintetizado: calculado a partir dos filhos. Herdado: vem do pai ou dos irmãos. ⏎ *Errado: int + float é erro de tipo.* ⏎ *Certo: Há coerção: o int vira float. Erro é misturar bool com número.*
   BYTE: "Outro erro: tratar toda mistura de tipos como erro. Inteiro com real tem coerção. Erro é quando nenhuma conversão faz sentido."

20. TELA: # Resumo ⏎ Atributo: informação num nó da árvore, calculada por regras semânticas. ⏎ O tipo é sintetizado: sobe das folhas para a raiz. ⏎ int com float sofre coerção para float. Sem conversão possível: erro semântico.
   BYTE: "Resumindo: a análise semântica anota a árvore com atributos. O tipo é um atributo sintetizado, que sobe dos filhos. No caminho, o compilador aplica coerção ou acusa erro."

#### PRATICA: "Suba os tipos" (5 cenas, ~27s)

1. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Aqui a é inteiro e x é real. Vamos anotar a árvore."

2. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Comece pelas folhas. Depois, cada pai olha os filhos."

3. TELA: # a + x * 2 ⏎ a: int     x: float
   PROF: "Se um dos filhos é real, o resultado sobe como real."

4. TELA: + ⏎ *a* * ⏎ x 2
   BYTE: "Diga o tipo de cada nó aceso, de baixo para cima."
   BRINQUEDO interativo: meta = "Diga o tipo do nó aceso" ; teclas = [int, float]
   se errar: "Tipo errado. Olhe os filhos desse nó."
   ao conseguir: "Árvore anotada. O tipo subiu até a raiz."

5. TELA: # a + x * 2 ⏎ a: int     x: float
   BYTE: "Isso que você fez é atributo sintetizado, na ordem de baixo para cima."

#### PRATICA: "O tipo que desce" (9 cenas, ~74s)

1. TELA: # int a, b ⏎ D ⏎ T L ⏎ int L , id b ⏎ id a
   BYTE: "Na declaração int a, b, o tipo está escrito uma vez só, no começo. Mas o a e o b precisam dele."

2. TELA: # int a, b ⏎ D ⏎ T.tipo = int ⏎ *T* L ⏎ *int* L , id b ⏎ id a
   BYTE: "O tipo nasce no nó T, pelo token int. Esse atributo é sintetizado: sobe do filho para T."

3. TELA: # int a, b ⏎ D ⏎ T.tipo = int ⏎ T L ⏎ int L , *id b* ⏎ *id a* ⏎ sobe desce
   BYTE: "O a e o b estão embaixo de L, que é irmão de T. Para chegar neles, o tipo sobe ou desce pela árvore? Toque."
   PERGUNTA (toque): certo = desce ; errado = sobe
   se errar: "Os ids são folhas, lá embaixo. O tipo está em cima."
   se acertar: "Isso. Ele passa de T para o irmão L e desce. Atributo que vem do pai ou de um irmão é um atributo herdado."

4. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T *L* ⏎ int *L* , id b ⏎ L.her = int ⏎ id a
   BYTE: "A regra semântica diz: o herdado de L recebe o tipo de T. Em cada L de baixo, o herdado é copiado do L de cima."

5. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T L ⏎ int L , *id b* ⏎ L.her = int ⏎ *id a* ⏎ anota na tabela soma os valores
   BYTE: "Quando o herdado chega numa folha id, o que o compilador faz com ele? Toque."
   PERGUNTA (toque): certo = anota na tabela ; errado = soma os valores
   se errar: "É uma declaração: o compilador precisa lembrar o tipo de cada nome."
   se acertar: "Isso. Cada id entra na tabela de símbolos com o tipo que desceu."

6. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T L ⏎ int L , id b ⏎ L.her = int ⏎ id a ⏎ *a: int    b: int*
   BYTE: "Resultado: a e b entram na tabela como int, mesmo com o tipo escrito uma vez só."

7. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T L ⏎ int L , id b ⏎ L.her = int ⏎ id a ⏎ a: int    b: int
   PROF: "Definição que só tem atributo sintetizado é S-atribuída. O parser ascendente calcula tudo nas reduções."

8. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T L ⏎ int L , id b ⏎ L.her = int ⏎ id a ⏎ S-atribuída L-atribuída
   BYTE: "Esta definição tem um herdado, que vem do irmão da esquerda. De que tipo ela é? Toque."
   PERGUNTA (toque): certo = L-atribuída ; errado = S-atribuída
   se errar: "Se tem atributo herdado, não pode ser a do S."
   se acertar: "Isso. L-atribuída: o herdado só depende do pai e dos irmãos à esquerda. Dá para calcular numa passada, da esquerda para a direita."

9. TELA: # int a, b ⏎ D ⏎ T.tipo = int L.her = int ⏎ T L ⏎ int L , id b ⏎ L.her = int ⏎ id a
   BYTE: "Sintetizado sobe, herdado desce. Toda definição S-atribuída também é L-atribuída, mas não o contrário."

#### PEGADINHA: "Sintetizado sobe, herdado desce" (5 cenas, ~35s)

1. TELA: # int a, b ⏎ D ⏎ T: int L ⏎ a b
   BYTE: "Declaração: int a, b. O tipo está num galho, e os nomes estão no outro."

2. TELA: # int a, b ⏎ D ⏎ T: int L ⏎ a b
   PROF: "Em a mais b, o tipo sobe dos filhos para o pai. Isso é sintetizado."

3. TELA: # int a, b ⏎ D ⏎ T: int L ⏎ a b
   BYTE: "Aqui é diferente. Para saber o tipo de a, o valor vem de qual nó? Toque."
   PERGUNTA (toque): certo = T: int ; errado = L | b
   se errar: "O int está no galho do lado. Ele precisa atravessar."
   se acertar: "Isso. O int passa pelo pai e desce até a. Isso é herdado."

4. TELA: # int a, b ⏎ D ⏎ *T: int* *L* ⏎ *a* *b*
   BYTE: "Herdado vem do pai ou de um irmão à esquerda. Por isso desce e anda de lado."

5. TELA: !!HERDADO!!
   PROF: "S-atribuída usa só sintetizado. L-atribuída aceita herdado, da esquerda para a direita."

#### CLIPE 30s: "Atributo sintetizado e herdado" (2 cenas, ~8s)

1. TELA: # Sobe ⏎ pai ⏎ filho
   BYTE: "Sintetizado sobe dos filhos para o pai."

2. TELA: # Desce ⏎ pai ⏎ filho
   BYTE: "Herdado desce do pai ou vem do irmão."

#### CLIPE 30s: "Coerção de tipo" (2 cenas, ~10s)

1. TELA: # 2 + 1.5 ⏎ int + float
   BYTE: "Inteiro mais real: o compilador converte o inteiro sozinho."

2. TELA: # 2 + 1.5 ⏎ float + float
   BYTE: "Isso é coerção. O resultado é real."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Herdado — mentira: "Todo atributo sobe na árvore." / verdade: "Sintetizado sobe. Herdado desce ou vem do lado."
    · [V] S-atribuída só usa sintetizados. → Isso mesmo.
    · [V] O tipo de uma expressão costuma ser sintetizado. → Isso mesmo.
    · [F] L-atribuída não pode ter herdados. → Pode, vindos do pai ou da esquerda.
- Bug do Inteiro Esticado — mentira: "int + float dá erro." / verdade: "O compilador converte o int para float e segue."
    · [V] int + float resulta em float. → Isso mesmo.
    · [V] Coerção é uma conversão automática. → Isso mesmo.
    · [F] float cabe em int sem perder nada. → A parte depois da vírgula se perde.

#### FICHA DO ASSUNTO
Em uma frase: Uma SDD pendura atributos nos símbolos da gramática: sintetizados sobem dos filhos, herdados descem do pai/irmãos; a verificação de tipos é uma SDD que calcula tipos e insere coerções.
Gancho: S↑ H↓: Sintetizado Sobe, Herdado desce. S-atribuída = Só Sintetizado; L-atribuída = herda da Left.
Termos-chave: sintetizado, herdado, s-atribuída, l-atribuída, coerção, tipo
Erros comuns: Chamar de herdado um atributo calculado a partir dos filhos // Achar que L-atribuída permite depender de irmãos à direita // Esquecer que toda S-atribuída também é L-atribuída // Tratar int + float como erro em vez de coerção para float // Confundir erro de tipo (compilação) com divisão por zero (execução)
Flashcards:
    · P: Atributo sintetizado depende de... → R: Atributos dos filhos (e do próprio nó)
    · P: Atributo herdado depende de... → R: Atributos do pai e dos irmãos
    · P: O que é SDD S-atribuída? → R: Só tem atributos sintetizados
    · P: O que é SDD L-atribuída? → R: Herdados dependem só do pai e de irmãos à esquerda
    · P: Ordem de avaliação de uma S-atribuída? → R: Pós-ordem (de baixo para cima)
    · P: Tipo de int + float? → R: float, com inttofloat no int
    · P: Tipo de a < b? → R: bool
    · P: Em int a, b, c, o tipo chega às variáveis como... → R: Atributo herdado
    · P: $$ = $1 + $3 no Bison é que tipo de atributo? → R: Sintetizado

#### GLOSSÁRIO (balão ao tocar no termo)
- atributo: Uma informação pendurada num nó da árvore, como o tipo ou o valor.


## ASSUNTO SCOPE_SYMBOLS: Escopo e tabela de símbolos
Pré-requisitos no jogo: Fases de um compilador
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «Quem é esse x?» → Programar o Byte → Pegadinha «A sombra da variável» → Desafio
Jogos que treinam: Detetive de Erros (classificar quem pega cada erro), Esconde-Esconde do Escopo (ligar cada uso à sua declaração)
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): false ; robô da Bancada: Carteiro do escopo

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug escondeu um x
Na tela: int x; { int x; x = 1; }
BYTE: "Antes da aula: esse x igual a um mexe em qual x?"
Opções: ✔o de dentro | o de fora | nos dois
Depois: "No de dentro. A busca começa pelo bloco mais próximo."

#### AULA ANIMADA BÁSICA: "Quem é esse x?" (20 cenas, ~146s) [1º exemplo começa na cena 4]

1. TELA: # Escopo ⏎ void f() { ⏎   int i; ⏎ } ⏎ void g() { ⏎   float i; ⏎ }
   BYTE: "Duas funções declaram uma variável i. Cada uma quer a sua."

2. TELA: # Escopo ⏎ void f() { ⏎ *  int i;* ⏎ } ⏎ void g() { ⏎ *  float i;* ⏎ }
   BYTE: "Em cada uso do i, o compilador precisa achar a declaração certa."

3. TELA: !!ESCOPO!!
   BYTE: "O escopo de uma declaração é o trecho do programa onde ela vale. Em C, é o bloco onde ela foi feita."

4. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ x = 2; ⏎ global: x int
   BYTE: "O mesmo nome pode existir em dois escopos. Quem guarda cada declaração, com nome e tipo, é a tabela de símbolos."

5. TELA: # Escopo ⏎ int x; ⏎ *void f() {* ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ x = 2; ⏎ f: x float ⏎ global: x int
   BYTE: "Para escopos aninhados, o compilador usa uma pilha de tabelas. Entrou num bloco, empilha uma tabela nova."

6. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎ *  x = 1.5;* ⏎ } ⏎ x = 2; ⏎ f: x float ⏎ global: x int
   BYTE: "Na linha x igual a um e meio, qual x vale? Toque na tabela certa."
   PERGUNTA (toque): certo = f: x float ; errado = global: x int
   se errar: "A busca começa na tabela do topo."
   se acertar: "Isso. O x de dentro esconde o de fora."

7. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ x = 2; ⏎ *f: x float* ⏎ *global: x int*
   BYTE: "A busca começa na tabela do topo e desce, bloco por bloco, até achar o nome. Se não achar em nenhuma, é erro semântico."

8. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ *}* ⏎ x = 2; ⏎ global: x int
   BYTE: "Quando o bloco acaba, a tabela dele sai da pilha, e as variáveis locais deixam de existir."

9. TELA: # Escopo ⏎ int x; ⏎ void f() { ⏎   float x; ⏎   x = 1.5; ⏎ } ⏎ *x = 2;* ⏎ *global: x int*
   BYTE: "Por isso x igual a dois fala do x global."

10. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote o nome na tabela do topo. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ 4. Fechou bloco: desempilhe a tabela dele.
   BYTE: "Cada bloco tem a sua tabela. A receita cuida da pilha delas."

11. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote o nome na tabela do topo. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ 4. Fechou bloco: desempilhe a tabela dele.
   BYTE: "O x de dentro escondeu o de fora. Que regra fez isso? Toque."
   PERGUNTA (toque): certo = 3. Uso: busque do topo. Não achou, erro. ; errado = 1. Abriu bloco: empilhe uma tabela vazia. | 2. Declarou: anote o nome na tabela do topo. | 4. Fechou bloco: desempilhe a tabela dele.
   se errar: "Pense em onde a busca começa."
   se acertar: "Isso. A busca para no primeiro x que acha."

12. TELA: # Receita ⏎ 1. Abriu bloco: empilhe uma tabela vazia. ⏎ 2. Declarou: anote o nome na tabela do topo. ⏎ 3. Uso: busque do topo. Não achou, erro. ⏎ *4. Fechou bloco: desempilhe a tabela dele.*
   BYTE: "Regra quatro: o bloco fechou, e os nomes declarados nele somem juntos."

13. TELA: # Escopo ⏎ *{* ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎   } ⏎   print(x); ⏎ } ⏎ fora: z int, x float
   BYTE: "Agora a busca é sua. O bloco de fora já tem a tabela dele."

14. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎ *    x = x + 1;* ⏎     z = z + 1; ⏎   } ⏎   print(x); ⏎ } ⏎ dentro: x float ⏎ fora: z int, x float
   BYTE: "Entrou o bloco de dentro. Na linha acesa, qual tabela responde?"
   PERGUNTA (toque): certo = dentro: x float ; errado = fora: z int, x float
   se errar: "A busca começa no topo. O nome está lá?"
   se acertar: "Isso. A tabela de dentro tem esse nome."

15. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎ *    z = z + 1;* ⏎   } ⏎   print(x); ⏎ } ⏎ dentro: x float ⏎ fora: z int, x float
   BYTE: "E nesta linha acesa? Toque na tabela onde a busca para."
   PERGUNTA (toque): certo = fora: z int, x float ; errado = dentro: x float
   se errar: "O topo não tem esse nome. A busca desce."
   se acertar: "Isso. Só a tabela de fora conhece esse nome."

16. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎ *  }* ⏎   print(x); ⏎ } ⏎ dentro: x float ⏎ fora: z int, x float
   BYTE: "Chegou a chave que fecha o bloco. Qual tabela sai da pilha?"
   PERGUNTA (toque): certo = dentro: x float ; errado = fora: z int, x float
   se errar: "Regra quatro: sai a tabela do bloco que fechou."
   se acertar: "Isso. Sai a tabela de dentro."

17. TELA: # Escopo ⏎ { ⏎   int z; ⏎   float x; ⏎   z = z + 1; ⏎   { ⏎     float x; ⏎     x = x + 1; ⏎     z = z + 1; ⏎   } ⏎ *  print(x);* ⏎ } ⏎ *fora: z int, x float*
   BYTE: "O print usa a única tabela que sobrou, a de fora."

18. TELA: # Erros comuns ⏎ Declarar x de novo num bloco interno é erro: o nome já existe. ⏎ certo errado
   BYTE: "Já existe um x fora. Posso declarar outro x dentro do bloco? Um colega disse isto. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "A declaração nova entra na tabela do topo, que ainda não tem x."
   se acertar: "Isso. Em outro escopo pode. O x de dentro esconde o de fora. Erro é declarar duas vezes no mesmo bloco."

19. TELA: # Erros comuns ⏎ Certo: Pode: o x interno esconde o externo. Erro é repetir no mesmo bloco. ⏎ *Errado: Buscar o nome só na tabela do topo.* ⏎ *Certo: A busca desce do topo até a tabela global. Erro só se nenhuma tiver.*
   BYTE: "Outro erro: procurar só no bloco atual. A busca desce por todas as tabelas da pilha, do bloco mais interno até o global."

20. TELA: # Resumo ⏎ Tabela de símbolos: nome, tipo e onde cada coisa foi declarada. ⏎ Uma tabela por bloco, numa pilha. Abriu bloco, empilha; fechou, desempilha. ⏎ Uso: busca do topo para baixo. O mais interno esconde o de fora.
   BYTE: "Resumindo: o escopo diz onde cada declaração vale. A pilha de tabelas de símbolos resolve cada nome, sempre do bloco mais interno para o mais externo."

#### PEGADINHA: "A sombra da variável" (5 cenas, ~27s)

1. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ print(x);
   BYTE: "Dois x, um fora e um dentro. O de dentro faz sombra no de fora."

2. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎ *  print(x);* ⏎ } ⏎ print(x);
   BYTE: "O print de dentro enxerga qual x? Toque na declaração."
   PERGUNTA (toque): certo =   int x = 2; ; errado = int x = 1;
   se errar: "A busca começa no bloco mais interno."
   se acertar: "Isso. Ele acha o x de dentro e para ali."

3. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ *print(x);*
   BYTE: "O bloco fechou. O print de fora enxerga qual x?"
   PERGUNTA (toque): certo = int x = 1; ; errado =   int x = 2;
   se errar: "Quando o bloco fecha, os nomes dele somem."
   se acertar: "Isso. Sobrou só o de fora, que nunca mudou."

4. TELA: # Dois x ⏎ *int x = 1;* ⏎ { ⏎ *  int x = 2;* ⏎   print(x); ⏎ } ⏎ print(x);
   PROF: "A pegadinha é achar que x igual a dois mudou o x de fora. É outra variável."

5. TELA: # Dois x ⏎ int x = 1; ⏎ { ⏎   int x = 2; ⏎   print(x); ⏎ } ⏎ print(x); ⏎ 2 1
   BYTE: "Saída do programa: dois, depois um."

#### CLIPE 30s: "Tabela de símbolos" (2 cenas, ~9s)

1. TELA: # Tabela de símbolos ⏎ x int global ⏎ y float local
   BYTE: "A tabela guarda cada nome, com o tipo e o escopo."

2. TELA: # Tabela de símbolos ⏎ *x* int global ⏎ y float local
   BYTE: "Todo uso de variável passa por ela."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug do Escopo Vazado — mentira: "Variável de dentro do bloco vale fora dele." / verdade: "Cada bloco abre um escopo. Ao fechar, os nomes dele somem."
    · [V] A busca começa pelo escopo mais interno. → Isso mesmo.
    · [V] Declarar x dentro esconde o x de fora. → Isso mesmo.
    · [F] Usar variável não declarada é erro léxico. → É erro semântico.

#### FICHA DO ASSUNTO
Em uma frase: A tabela de símbolos guarda nome, tipo e categoria; com escopo estático, ela é uma pilha de tabelas: entra no bloco empilha, sai desempilha, e a busca vai do topo para a base.
Gancho: Escopo é cebola: procure de dentro para fora. Abre chave empilha, fecha chave desempilha.
Termos-chave: escopo, pilha, tabela de símbolos, aninhamento, sombreamento, declaração
Erros comuns: Buscar da base para o topo (o certo é do topo, mais interno, para a base) // Confundir sombreamento (escopo interno) com redeclaração (mesmo escopo) // Achar que o léxico decide a qual declaração um nome se refere // Continuar vendo variáveis de um bloco que já foi fechado // Confundir escopo (onde é visível) com tempo de vida (quando existe)
Flashcards:
    · P: Regra do escopo estático? → R: Vale a declaração do bloco mais interno que contém o uso
    · P: Ao entrar num bloco, a tabela... → R: Empilha uma tabela nova
    · P: Ao sair do bloco... → R: Desempilha (descarta) a tabela do topo
    · P: A busca de um nome começa onde? → R: Na tabela do topo (bloco mais interno)
    · P: Redeclaração é checada em qual tabela? → R: Só na do topo
    · P: O que é sombreamento? → R: Declaração interna escondendo uma externa de mesmo nome
    · P: Nome não achado em nenhuma tabela? → R: Erro: variável não declarada
    · P: Escopo dinâmico decide pelo quê? → R: Pela cadeia de chamadas em execução

#### GLOSSÁRIO (balão ao tocar no termo)
- escopo: O trecho do programa onde um nome vale.
- tabela de símbolos: A lista que o compilador guarda com cada nome, seu tipo e seu escopo.


## ASSUNTO AST_BUILD: Árvore sintática abstrata (AST) com classes
Pré-requisitos no jogo: Gramáticas e derivações
Trilha (ordem das etapas): Abertura com o Bug → Ver a aula animada «A árvore enxuta» → Fazer com ajuda → Pegadinha «O parêntese não vira nó» → Desafio
Jogos que treinam: Linha de Montagem TAC (montar código de três endereços), Fábrica de AST (montar a AST com new BinOp(...))
Tem "Fazer com ajuda" (4 problemas gerados, ajuda decrescente): true ; robô da Bancada: não

#### ABERTURA COM O BUG (10 s, antes da aula)
Manchete: O Bug podou a árvore
Na tela: ( a + b )
BYTE: "Antes da aula: os parênteses entram na AST?"
Opções: sim | ✔não | só um
Depois: "Não. A forma da árvore já mostra o agrupamento."

#### AULA ANIMADA BÁSICA: "A árvore enxuta" (17 cenas, ~125s)

1. TELA: # AST ⏎ a + ( b )
   BYTE: "O parser montou a árvore de derivação. Ela tem um nó para cada regra usada, e fica enorme."

2. TELA: # AST ⏎ *a + ( b )*
   BYTE: "As fases seguintes só precisam saber qual é a operação e quais são os operandos."

3. TELA: !!AST!!
   BYTE: "A árvore que guarda só isso é a árvore sintática abstrata, a AST."

4. TELA: # a + ( b ) ⏎ E ⏎ E + T ⏎ id ( E ) ⏎ id
   BYTE: "A árvore de derivação guarda tudo: parênteses, e até as regras que só passam de um não-terminal para outro."

5. TELA: # a + ( b ) ⏎ E ⏎ E + T ⏎ id ( E ) ⏎ id
   BYTE: "Tem nó sobrando aqui. Toque nos dois parênteses: eles não mudam a conta."
   PERGUNTA (toque): certo = ( | ) ; errado = E | id
   se errar: "A árvore já mostra a ordem. O parêntese fez o trabalho dele."
   se acertar: "Isso. Pontuação some da árvore abstrata."

6. TELA: # a + ( b ) ⏎ BinOp + ⏎ a b
   BYTE: "A AST guarda só o que tem significado: o operador no nó, e os operandos como filhos."

7. TELA: # a + ( b ) ⏎ BinOp + ⏎ a b ⏎ *new BinOp('+', a, b)*
   BYTE: "No código, cada nó da AST é um objeto, criado pelo parser na hora em que reconhece a regra."

8. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ 3. Parêntese e regra de passagem somem.
   BYTE: "A receita para montar a AST tem três regras."

9. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ 3. Parêntese e regra de passagem somem.
   BYTE: "Na árvore, cada nome virou folha. Qual regra é essa? Toque."
   PERGUNTA (toque): certo = 2. Valor ou nome vira folha. ; errado = 1. Operador vira nó; operandos viram filhos. | 3. Parêntese e regra de passagem somem.
   se errar: "Nome não tem filhos."
   se acertar: "Isso. Regra dois: valor e nome viram folha."

10. TELA: # Receita ⏎ 1. Operador vira nó; operandos viram filhos. ⏎ 2. Valor ou nome vira folha. ⏎ *3. Parêntese e regra de passagem somem.*
   BYTE: "Regra três: o parêntese só servia para agrupar. Na AST, a própria forma da árvore já mostra o agrupamento."

11. TELA: # AST ⏎ total * ( preco - 2 )
   BYTE: "Agora monte a AST de total vezes, abre parêntese, preço menos dois."

12. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ * - (
   BYTE: "Qual operador fica na raiz? Toque nele."
   PERGUNTA (toque): certo = * ; errado = - | (
   se errar: "O parêntese faz o menos acontecer antes. Quem fica por último é a raiz."
   se acertar: "Isso. O vezes é a última conta, então vai na raiz."

13. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ * ⏎ total - ⏎ preco 2 total
   BYTE: "O menos fica embaixo do vezes. Quais são os filhos do menos? Toque."
   PERGUNTA (toque): certo = preco | 2 ; errado = total
   se errar: "São os dois lados do menos."
   se acertar: "Isso. Preço e dois são folhas do menos."

14. TELA: # AST ⏎ total * ( preco - 2 ) ⏎ *** ⏎ total *-* ⏎ preco 2
   BYTE: "Sem parênteses na árvore, e a ordem continua certa: quem está mais fundo é calculado primeiro."

15. TELA: # Erros comuns ⏎ A AST de a * (b - 2) tem nós para os parênteses. ⏎ certo errado
   BYTE: "Os parênteses aparecem na AST? Um colega desenhou com eles. Certo ou errado?"
   PERGUNTA (toque): certo = errado ; errado = certo
   se errar: "O parêntese muda a forma da árvore. Depois disso, ele ainda serve?"
   se acertar: "Isso. O parêntese só agrupa. Na AST, o agrupamento já está na forma: o menos fica embaixo do vezes."

16. TELA: # Erros comuns ⏎ Certo: Parêntese não vira nó. O agrupamento fica na forma da árvore. ⏎ *Errado: Pôr na raiz o primeiro operador que aparece na expressão.* ⏎ *Certo: Na raiz fica a última operação a ser calculada.*
   BYTE: "Outro erro: pôr na raiz o primeiro operador da linha. A raiz é a última conta. A primeira conta fica no fundo da árvore."

17. TELA: # Resumo ⏎ Árvore de derivação: um nó por regra. AST: só operações e operandos. ⏎ Operador vira nó interno; nome e valor viram folha. ⏎ Parêntese e regra de passagem somem. Mais fundo é calculado antes.
   BYTE: "Resumindo: a AST é a árvore de derivação sem o que só serve à gramática. Ficam os operadores, os operandos e a ordem das contas."

#### PEGADINHA: "O parêntese não vira nó" (6 cenas, ~32s)

1. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c
   BYTE: "Esta conta tem parênteses. Eles mudam a ordem da conta."

2. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *(* E *)*
   BYTE: "Na árvore de derivação, cada parêntese aparece como folha."

3. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *** ⏎ *+* c ⏎ a b
   BYTE: "Na AST eles somem. A forma da árvore já guarda a ordem."

4. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ * ⏎ + c ⏎ a b ⏎ * + ( )
   BYTE: "Quais destes viram nós na árvore? Toque em todos."
   PERGUNTA (toque): certo = * | + ; errado = ( | )
   se errar: "A AST guarda operações e valores. Sinal de agrupar não vira nó."
   se acertar: "Isso. Vezes e mais viram nós. Os parênteses somem."

5. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ *** ⏎ *+* c ⏎ a b
   BYTE: "O mais fica embaixo do vezes. É assim que a AST lembra do parêntese."

6. TELA: # O parêntese não vira nó ⏎ ( a + b ) * c ⏎ * ⏎ + c ⏎ a b
   PROF: "Na AST fica o que o programa faz, não como foi escrito."

#### CLIPE 30s: "AST ou árvore de derivação" (2 cenas, ~10s)

1. TELA: # (a + b) ⏎ ( E )   E + T   T   F
   BYTE: "A árvore de derivação guarda cada regra usada."

2. TELA: # (a + b) ⏎ a + b
   BYTE: "A AST guarda só o que importa para o significado."

#### BUGS DO ÁLBUM (julgar verdade/mentira)
- Bug da Árvore Gorda — mentira: "AST é a árvore de derivação inteira." / verdade: "AST guarda só o que importa. Parênteses e regras de passagem somem."
    · [V] ( a + b ) não tem nó de parêntese na AST. → Isso mesmo.
    · [V] A forma da AST já mostra a precedência. → Isso mesmo.
    · [F] A AST tem mais nós que a árvore de derivação. → Ela é mais enxuta.

#### FICHA DO ASSUNTO
Em uma frase: A árvore sintática abstrata guarda só operadores, operandos e construções (BinOp, Var, Num, Atrib, Se, Enquanto, Bloco), jogando fora correntes de não-terminais, parênteses, pontuação e ε; o parser a constrói (new BinOp no descendente ou $$ = new BinOp(...) no Bison) e as fases seguintes andam nela.
Gancho: AST = Árvore Sem Tralha: corta o CPE (Correntes, Parênteses, Epsilons). Quem está mais fundo trabalha primeiro.
Termos-chave: abstrat, arvore, classe, binop, parentes, precedenc, parser
Erros comuns: Criar nós para parênteses ou para não-terminais de corrente (E → T → F) // Montar a AST pendurada para a direita (recursão em vez de laço) e inverter a associatividade de - e / // Achar que sem parênteses a precedência se perde (ela mora na forma da árvore) // Confundir AST (estrutura em árvore) com TAC (lista de instruções gerada a partir dela) // Esquecer $$ = $2 na regra do parêntese ou $$ = $1 nas correntes do Bison
Flashcards:
    · P: O que a AST joga fora? → R: Correntes de não-terminais, parênteses/pontuação e ε
    · P: Nós da derivação × AST em a + b * c? → R: 13 × 5
    · P: Onde mora a precedência na AST? → R: Na forma: quem está mais fundo é calculado primeiro
    · P: Linha-chave da associatividade à esquerda no descendente? → R: e = new BinOp(op, e, termo()); dentro do while
    · P: Ação Bison para E → E + T? → R: $$ = new BinOp('+', $1, $3);
    · P: Ação Bison para F → ( E )? → R: $$ = $2; (o parêntese some)
    · P: Classes de comando típicas? → R: Atrib, Se, Enquanto, Bloco (estendem Cmd)
    · P: Vantagem da AST sobre gerar código direto no parser? → R: Várias passadas (tipos, depois código), fases separadas e testáveis, otimização na árvore
    · P: Nós da AST com n operadores binários? → R: 2n + 1

#### GLOSSÁRIO (balão ao tocar no termo)
- AST: Árvore enxuta: guarda as operações e os valores, sem parênteses nem regras.
