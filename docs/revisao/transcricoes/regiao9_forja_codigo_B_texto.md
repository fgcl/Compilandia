# Região 9: Forja do Código — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO TAC: Código de três endereços

#### ESTILO STORY (História / Analogia): "A receita para o ajudante que só faz uma coisa por vez"
- NARRADOR: Forja do Código. A árvore anotada chega numa esteira; do outro lado, sai uma lista de instruções.
- BYTE: Por que não transformar a árvore direto em Assembly?
- PROF: Porque existem dezenas de processadores. É mais esperto traduzir para uma linguagem intermediária simples, e só no fim para cada máquina.
- TEXTO: Imagine uma receita para um ajudante de cozinha que só sabe fazer **uma operação por vez**, com **no máximo dois ingredientes**, e sempre coloca o resultado **numa tigela com etiqueta**.
- CÓDIGO ('Junte b com c (multiplicando) na tigela t1; depois junte a com t1 na tigela t2.' — a + b * c):
```
t1 = b * c
t2 = a + t1
```
- PASSOS (O mapa da analogia):
    · Uma operação por vez, dois ingredientes → **instrução de três endereços**: x = y op z
    · Tigelas com etiqueta t1, t2... → **temporários** gerados pelo compilador
    · Ordem da receita → **pós-ordem** da árvore: filhos antes do pai
    · Ficha com 4 campos (operação, ingr. 1, ingr. 2, tigela) → **quádrupla**
    · Ficha sem tigela: 'use o resultado do passo 0' → **tripla**
- PREVEJA: Quantas instruções (sem contar a atribuição final) para a + b * c - a?
    opções: 2 | ✔3 | 4
    explicação: Três operadores → três instruções: t1 = b * c; t2 = a + t1; t3 = t2 - a.
- WIDGET TAC entrada=«a + b * c» legenda: A máquina de receitas: a multiplicação vem primeiro porque está mais embaixo na árvore.
- PILHA: Tigelas... são como eu. Guardam... resultados no meio do caminho.
- REVELAR: Por que 'três endereços'?
    → Porque cada instrução menciona no máximo três nomes: dois operandos e um destino (x = y op z).
- CAIXA [No mundo real]: O LLVM IR é uma linguagem intermediária no espírito do TAC (em forma SSA): Clang, Rust e Swift geram LLVM IR, e o LLVM o traduz para x86, ARM, RISC-V...

#### ESTILO VISUAL (Passo a passo visual): "Da árvore à lista, em pós-ordem"
- PROF: Vamos ver a árvore e o código lado a lado.
- WIDGET ATTR_TREE entrada=«a + b * c» legenda: A árvore: '*' está embaixo, '+' na raiz.
- WIDGET TAC entrada=«a + b * c» legenda: Pós-ordem: b * c gera t1; depois a + t1 gera t2.
- PASSOS (A regra do gerador):
    · Folha (variável ou número): o 'endereço' é o próprio nome
    · Nó operador: gera o código do filho esquerdo, depois o do direito
    · Cria um temporário novo tN e emite: tN = esq op dir
    · O endereço do nó passa a ser tN
- WIDGET TAC entrada=«(a + b) * (a + b)» legenda: Repare: a + b é calculado DUAS vezes (t1 e t2). O gerador ingênuo não percebe, a otimização vai.
- BYTE: E com comparação e lógica? Funciona igual?
- WIDGET TAC entrada=«a < b && ok» legenda: Aqui, como valores: t1 = a < b; t2 = t1 && ok. (Compiladores reais costumam usar desvios — curto-circuito.)
- PREVEJA: Para a + x * 2 (a int, x float), um compilador real gera...
    opções: t1 = x * 2; t2 = a + t1 | ✔t1 = x * 2; t2 = inttofloat(a); t3 = t2 + t1 | t1 = a + x; t2 = t1 * 2
    explicação: A verificação de tipos manda converter a. O widget do jogo esconde a conversão, mas no Livro do Dragão ela aparece como instrução própria (e o 2 vira 2.0 na própria constante).
- WIDGET TAC entrada=«a + x * 2» legenda: Versão simplificada do jogo (sem a instrução de conversão).
- CÓDIGO (As mesmas instruções de a + b * c como quádruplas):
```
     op    arg1   arg2   resultado
(0)  *     b      c      t1
(1)  +     a      t1     t2
```
- CAIXA [Poder do cérebro]: Número de temporários no gerador ingênuo = número de operadores. Dá para economizar reaproveitando tigelas que não serão mais usadas, isso é trabalho da alocação de registradores.

#### ESTILO WORKED (Exemplo resolvido): "Expressões, quádruplas, triplas e um if"
- PROF: Exercício 1: gere TAC para x = (a + b) * (a - c).
- PASSOS (Pós-ordem, com calma):
    · Subárvore esquerda a + b → t1 = a + b
    · Subárvore direita a - c → t2 = a - c
    · Raiz * → t3 = t1 * t2
    · Atribuição → x = t3
- CÓDIGO (Duas representações das mesmas instruções):
```
quádruplas                      triplas
     op  arg1  arg2  res             op  arg1  arg2
(0)  +   a     b     t1        (0)   +   a     b
(1)  -   a     c     t2        (1)   -   a     c
(2)  *   t1    t2    t3        (2)   *   (0)   (1)
(3)  =   t3          x         (3)   =   x     (2)
```
- PROF: Exercício 2: comandos com desvio. if (a < b) x = 1; else x = 2;
- CÓDIGO (Rótulos e desvios: o if vira 'pule se for falso'):
```
    t1 = a < b
    ifFalse t1 goto L1
    x = 1
    goto L2
L1: x = 2
L2: ...
```
- BYTE: Agora eu faço!
- REVELAR: TAC de y = a * b + a * b.
    → t1 = a * b; t2 = a * b; t3 = t1 + t2; y = t3. (Sim, a * b duas vezes, o gerador ingênuo não otimiza.)
- REVELAR: As triplas de y = a * b + a * b.
    → (0) * a b; (1) * a b; (2) + (0) (1); (3) = y (2).
- REVELAR: TAC de while (a < b) a = a + 1;
    → L1: t1 = a < b;  ifFalse t1 goto L2;  t2 = a + 1;  a = t2;  goto L1;  L2: ...
- WIDGET TAC entrada=«a * b + a * b» legenda: Confira a primeira resposta.
- CAIXA [Dica de prova]: Em triplas, o resultado é referenciado pela POSIÇÃO: (2) * (0) (1). Na prova, numere as linhas desde o começo, senão as referências viram bagunça.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que uma linguagem no meio do caminho?"
- PROF: Pergunta 1: se eu tenho 5 linguagens-fonte e 4 processadores, quantos compiladores completos eu escrevo sem código intermediário?
- REVELAR: E com uma representação intermediária comum?
    → Sem: 5 × 4 = 20 compiladores. Com: 5 front-ends + 4 back-ends = 9 peças. A IR é o 'adaptador universal'.
- REVELAR: Por que limitar a no máximo um operador por instrução?
    → Porque se parece com instruções de máquina (um op por vez), torna a ordem de avaliação explícita e facilita otimizar e alocar registradores.
- REVELAR: Em que ordem as instruções de uma expressão aparecem?
    → Na pós-ordem da árvore: cada operador só pode ser calculado depois dos seus operandos.
- BYTE: E de onde saem os nomes t1, t2...?
- REVELAR: Quem cria os temporários?
    → O próprio compilador (new Temp()), um por nó operador. Eles não existem no programa-fonte.
- PREVEJA: Por que quádruplas são mais fáceis de reordenar do que triplas?
    opções: Porque ocupam menos memória | ✔Porque nas triplas os resultados são referenciados pela posição; mover uma instrução quebra as referências | Porque triplas não aceitam desvios
    explicação: Na quádrupla o resultado tem nome (t1); na tripla ele é '(0)'. Mudou a posição, mudou o nome. Por isso existem as triplas INDIRETAS (uma lista de ponteiros que se reordena).
- REVELAR: Como representar um if em TAC, se não há chaves nem blocos?
    → Com rótulos e desvios: ifFalse cond goto L1, goto L2, L1: ... A estrutura vira um grafo de saltos.
- WIDGET TAC entrada=«(a + b) * c» legenda: Teste: preveja as instruções antes de olhar.
- CAIXA [Poder do cérebro]: O TAC é baixo nível o bastante para ficar perto da máquina e alto o bastante para não depender dela. Esse equilíbrio é o que torna a otimização portátil.

#### ESTILO FORMAL (Definição de prova): "Instruções de três endereços e representações"
- TEXTO: **Código de três endereços:** sequência de instruções com, no máximo, um operador no lado direito. Um 'endereço' pode ser um nome do programa, uma constante ou um temporário gerado pelo compilador.
- CÓDIGO (Formas de instrução (Livro do Dragão, seção 6.2)):
```
x = y op z          atribuição binária
x = op y            atribuição unária (menos, not, conversão)
x = y               cópia
goto L              desvio incondicional
if x goto L         desvio condicional
ifFalse x goto L
if x relop y goto L
param x;  call p, n;  y = call p, n;  return y
x = y[i];  x[i] = y         acesso indexado
x = &y;  x = *y;  *x = y    endereços e ponteiros
```
- CÓDIGO (SDD de tradução de expressões para TAC (|| = concatenação)):
```
S → id = E ;   S.code := E.code || gen(top.get(id.lexeme) '=' E.addr)
E → E1 + E2    E.addr := new Temp()
               E.code := E1.code || E2.code || gen(E.addr '=' E1.addr '+' E2.addr)
E → - E1       E.addr := new Temp()
               E.code := E1.code || gen(E.addr '=' 'minus' E1.addr)
E → ( E1 )     E.addr := E1.addr;  E.code := E1.code
E → id         E.addr := top.get(id.lexeme);  E.code := ''
```
- PASSOS (Representações):
    · **Quádruplas:** (op, arg1, arg2, resultado), resultado nomeado; fácil de reordenar
    · **Triplas:** (op, arg1, arg2), o resultado é a posição da instrução; economiza espaço
    · **Triplas indiretas:** lista de ponteiros para triplas; reordena-se a lista
    · **SSA:** cada variável é atribuída uma única vez (x1, x2...), com funções φ nas junções
- PREVEJA: Qual NÃO é uma instrução de três endereços válida?
    opções: t1 = a + b | ✔t2 = a + b * c | if t1 goto L3
    explicação: t2 = a + b * c tem dois operadores. Precisa virar t1 = b * c; t2 = a + t1.
- WIDGET TAC entrada=«a + b - c» legenda: Associatividade à esquerda: (a + b) primeiro, depois - c.
- CAIXA [Dica de prova]: Questão comum: 'traduza para quádruplas E triplas'. Faça primeiro o TAC em texto, numere as linhas, e só então preencha as duas tabelas.
- CAIXA [Cuidado!]: Não confunda o 'três' do nome com 'três instruções'. É no máximo três ENDEREÇOS por instrução.

#### ESTILO MNEMONIC (Truque de memória): "Dois entram, um sai"
- NARRADOR: Palácio da Memória: Forja do Código, oitava parada. Uma bigorna com três encaixes: dois para entrar, um para sair.
- CAIXA [Gancho de memória]: **'Dois entram, um sai.'** x = y op z: y e z entram, x sai. Três endereços.
- CAIXA [Gancho de memória]: **QUAdrupla = QUAtro campos** (op, arg1, arg2, resultado). **TRIpla = TRÊS campos**: ela não tem casa própria, mora no endereço (a posição).
- CAIXA [Gancho de memória]: **t de tigela.** t1, t2, t3 são tigelas temporárias na bancada do compilador.
- CAIXA [Gancho de memória]: **Pós-ordem = 'filhos primeiro, pai depois'.** Igual a jantar de família: as crianças se servem antes.
- BYTE: E a ordem das tigelas é sempre crescente, né? t1 antes de t2?
- PROF: No gerador do jogo, sim: cada operador visitado em pós-ordem ganha o próximo número.
- PREVEJA: Na tripla (2) + (0) (1), o que significa '(0)'?
    opções: O número zero | ✔O resultado da instrução da posição 0 | O temporário t0
    explicação: Triplas apontam pela posição. '(0)' = 'o que a instrução 0 calculou'.
- WIDGET TAC entrada=«a * b + c * a» legenda: Dois entram, um sai, três vezes.
- REVELAR: Recite as três representações e o que as diferencia.
    → Quádruplas (resultado com nome), triplas (resultado = posição) e triplas indiretas (lista de ponteiros para reordenar).
- CAIXA [Conversa ao pé da lareira]: Quádrupla: 'Eu tenho nome e endereço próprio, pode me mudar de lugar.' Tripla: 'Eu sou mais econômica... só não me tire do lugar, senão ninguém me acha.'


## ASSUNTO OPTIMIZATION: Otimização (dobra de constantes)

#### ESTILO STORY (História / Analogia): "A lista de compras inteligente"
- NARRADOR: Forja do Código, sala de polimento. O TAC chega meio bruto; aqui ele sai brilhando.
- BYTE: Otimizar é deixar o código ÓTIMO?
- PROF: O nome engana: é **melhorar**, sem mudar o resultado do programa. 'Ótimo' de verdade é impossível de garantir. Vamos pensar numa lista de compras.
- TEXTO: Sua lista diz: 'comprar 2 × 6 ovos'. Você faz a conta **em casa**, não no caixa: 12 ovos. Isso é **dobra de constantes**: calcular em tempo de compilação.
- TEXTO: A lista diz 'comprar leite' duas vezes, e nada mudou entre uma e outra. Você compra uma vez só e usa as duas. Isso é eliminar **subexpressão comum**.
- TEXTO: A lista tem 'comprar fermento', mas nenhuma receita usa. Corta! Isso é eliminar **código morto**.
- PASSOS (O mapa da analogia):
    · Fazer a conta em casa → **dobra de constantes** (2 * 6 vira 12)
    · Anotar 'ovos = 12' e usar 12 dali em diante → **propagação de constantes**
    · Comprar leite uma vez só → **eliminação de subexpressões comuns**
    · Riscar o fermento que ninguém usa → **eliminação de código morto**
    · Trecho do mercado sem desvios, entra e sai por um lugar só → **bloco básico**
- PREVEJA: Qual linha pode ser calculada inteiramente pelo compilador?
    opções: t1 = a * 2 | ✔t1 = 2 * 3 | t1 = x * y
    explicação: Só 2 * 3 depende apenas de constantes. Vira t1 = 6 antes do programa rodar.
- WIDGET TAC entrada=«2 * 3 + a» legenda: O gerador produz t1 = 2 * 3; com dobra de constantes, sobra só uma instrução: 6 + a.
- CAIXA [Cuidado!]: Regra de ouro: otimização NUNCA pode mudar o que o programa faz. Na dúvida (efeitos colaterais, divisão por zero...), o compilador não otimiza.
- REVELAR: Na analogia, por que 'comprar leite' duas vezes só pode virar uma se 'nada mudou entre uma e outra'?
    → Se alguém tivesse bebido o leite no meio (uma variável redefinida), a segunda compra seria necessária. Subexpressão comum só vale sem redefinição dos operandos.

#### ESTILO VISUAL (Passo a passo visual): "Antes e depois do polimento"
- PROF: Vamos ver o TAC antes e depois de cada otimização.
- WIDGET TAC entrada=«x * 2 + 3 * 4» legenda: Antes: t2 = 3 * 4. Com dobra: o 3 * 4 vira 12 e some uma instrução.
- WIDGET TAC entrada=«a * 4 + 2 * 3» legenda: Mesmo truque: 2 * 3 vira 6. O a * 4 fica: a só é conhecido em execução.
- WIDGET TAC entrada=«(a + b) * (a + b)» legenda: Subexpressão comum: t1 = a + b e t2 = a + b.
- CÓDIGO (Eliminação de subexpressão comum):
```
antes:            depois:
t1 = a + b        t1 = a + b
t2 = a + b        t3 = t1 * t1
t3 = t1 * t2
```
- BYTE: E o código morto, como a gente vê?
- CÓDIGO (Eliminação de código morto (t1 não está vivo)):
```
antes:            depois:
t1 = a + b        t2 = a * c
t2 = a * c        x = t2
x = t2
// t1 nunca é usado
```
- CÓDIGO (Blocos básicos: B1 = (1); B2 = (2); B3 = (3)-(6); B4 = (7)):
```
(1) i = 0                ← líder (primeira instrução)
(2) L1: if i >= n goto L2  ← líder (alvo de desvio)
(3) t1 = i * 4
(4) s = s + t1
(5) i = i + 1
(6) goto L1
(7) L2: return s         ← líder (alvo, e vem depois de desvio)
```
- PREVEJA: A instrução (3) é líder?
    opções: ✔Sim: vem logo depois de um desvio condicional | Não: não é alvo de nenhum goto | Só se i for constante
    explicação: (2) é um desvio (if ... goto). A instrução seguinte a qualquer desvio é líder. Por isso (2) sozinha forma um bloco.
- CAIXA [Poder do cérebro]: Otimização local trabalha dentro de UM bloco básico, onde a execução é linear, sem surpresas. A global olha o grafo de fluxo inteiro.

#### ESTILO WORKED (Exemplo resolvido): "Polindo um trecho, uma otimização de cada vez"
- PROF: Vamos otimizar este bloco básico. Suponha que, depois dele, só x e y são usados.
- CÓDIGO (Bloco original):
```
(1) t1 = 4 * 2
(2) t2 = a + t1
(3) t3 = a + t1
(4) x = t2 * t3
(5) y = 0
(6) y = x
```
- PASSOS (Uma passada de cada vez):
    · **Dobra:** (1) t1 = 4 * 2 → t1 = 8
    · **Propagação de constantes:** t1 vale 8 → (2) t2 = a + 8 e (3) t3 = a + 8
    · **Subexpressão comum:** (3) calcula o mesmo que (2), sem redefinir a no meio → t3 = t2 (cópia)
    · **Propagação de cópias:** (4) x = t2 * t2
    · **Código morto:** t1 e t3 não são mais usados → remove (1) e (3)
    · **Código morto:** (5) y = 0 é sobrescrito em (6) sem uso → remove (5)
- CÓDIGO (Resultado: de 6 instruções para 3):
```
t2 = a + 8
x = t2 * t2
y = x
```
- BYTE: Ordem importa? Posso fazer código morto primeiro?
- PROF: Pode, mas uma otimização costuma abrir espaço para outra. Compiladores repetem as passadas até nada mudar.
- REVELAR: Otimize: t1 = 3 + 2; t2 = t1 * b; t3 = t1 * b; z = t2 + t3.
    → t1 = 5 (dobra) → t2 = 5 * b; t3 = 5 * b (propagação) → t3 = t2 (subexpr. comum) → z = t2 + t2. Final: t2 = 5 * b; z = t2 + t2.
- REVELAR: Em t1 = a + b; a = 7; t2 = a + b; dá para trocar t2 por t1?
    → NÃO! a foi redefinido entre as duas. a + b não é mais a mesma expressão.
- WIDGET TAC entrada=«a * 4 + 2 * 3» legenda: Pratique: o que a dobra de constantes faz aqui?
- CAIXA [Dica de prova]: Escreva o nome de cada otimização ao aplicar ('dobra', 'propagação', 'subexpressão comum', 'código morto'). Muitas questões pontuam o nome tanto quanto o resultado.

#### ESTILO SOCRATIC (Perguntas guiadas): "Quando é seguro otimizar?"
- PROF: A otimização tem um juramento: não mudar o comportamento. Vamos testar esse juramento com perguntas.
- REVELAR: x = 10 / 0 pode ser 'dobrado' pelo compilador?
    → Não como valor: a divisão por zero precisa acontecer (ou ser reportada). O dobrador do jogo, inclusive, se recusa a dobrar isso.
- REVELAR: E t = 0.1 + 0.2 com float?
    → Pode, desde que o compilador use exatamente a mesma aritmética da máquina-alvo. Senão o resultado mudaria.
- REVELAR: Quando duas ocorrências de a + b são a 'mesma' subexpressão?
    → Quando nem a nem b foram redefinidos entre elas (e não há efeito colateral, como chamada de função que altera a ou b).
- BYTE: E código morto: como o compilador sabe que ninguém usa?
- REVELAR: O que significa uma variável estar 'viva' num ponto?
    → Que o valor dela pode ser usado depois, num caminho de execução, antes de ser redefinido. Atribuição a variável não viva é código morto.
- PREVEJA: if (false) { print(1); }, o que a otimização pode fazer?
    opções: Nada | ✔Remover o bloco inteiro: é código inalcançável | Trocar por print(0)
    explicação: Com a condição constante, o desvio sempre pula o bloco. Inalcançável = pode sair.
- REVELAR: Por que dividir o código em blocos básicos antes de otimizar?
    → Dentro de um bloco não há desvios para dentro nem para fora no meio: as instruções executam todas, em ordem. Fica fácil raciocinar sobre valores.
- REVELAR: x * 2 → x + x ou x << 1: que otimização é essa?
    → Redução de força: trocar uma operação cara por uma equivalente mais barata.
- WIDGET TAC entrada=«2 * 3 + a» legenda: Preveja o código depois da dobra antes de olhar.
- CAIXA [Poder do cérebro]: Pense numa expressão com chamada de função: f() + f(). É subexpressão comum? Só se f não tiver efeitos colaterais, e o compilador quase nunca consegue provar isso sozinho.

#### ESTILO FORMAL (Definição de prova): "Blocos básicos e transformações que preservam a semântica"
- TEXTO: **Bloco básico:** sequência maximal de instruções consecutivas em que o fluxo só entra pela primeira e só sai pela última, sem desvios no meio. **Grafo de fluxo:** blocos como nós; arestas para os sucessores possíveis.
- CÓDIGO (Particionamento em blocos básicos (Livro do Dragão, Alg. 8.5)):
```
LÍDERES:
  1. a primeira instrução do código é líder
  2. toda instrução que é ALVO de um desvio (condicional ou não) é líder
  3. toda instrução que vem IMEDIATAMENTE APÓS um desvio é líder
BLOCOS:
  cada líder inicia um bloco, que vai até a instrução
  anterior ao próximo líder (ou até o fim)
```
- PASSOS (Transformações clássicas):
    · **Dobra de constantes:** avaliar em compilação operações com operandos constantes (2 * 3 → 6)
    · **Propagação de constantes:** após x = 6, substituir usos de x por 6 enquanto x não for redefinido
    · **Subexpressões comuns:** E já calculada e operandos não redefinidos desde então → reutilizar o valor
    · **Propagação de cópias:** após x = y, usar y no lugar de x
    · **Código morto:** remover instruções cujo resultado não está vivo (nunca usado depois)
    · **Redução de força / movimentação de código invariante de laço:** trocar ops caras; tirar do laço o que não muda nele
- TEXTO: **Local** = dentro de um bloco básico (ex.: via DAG do bloco). **Global** = sobre o grafo de fluxo, com análise de fluxo de dados (variáveis vivas, definições que alcançam, expressões disponíveis).
- PREVEJA: Qual transformação exige saber quais variáveis estão VIVAS?
    opções: Dobra de constantes | ✔Eliminação de código morto | Redução de força
    explicação: Só é seguro apagar x = ... se o valor de x não for usado depois. 'Vivo' é exatamente isso.
- WIDGET TAC entrada=«x * 2 + 3 * 4» legenda: Dobra de constantes local: 3 * 4 → 12.
- CAIXA [Dica de prova]: Questão clássica: 'identifique os líderes e os blocos básicos'. Marque líderes com as 3 regras, numerando as instruções; depois desenhe o grafo de fluxo com as arestas de desvio E de queda (fall-through).
- CAIXA [Cuidado!]: A instrução APÓS um desvio é líder, e não a do desvio. O goto/if fecha o bloco; ele é a ÚLTIMA instrução do bloco.

#### ESTILO MNEMONIC (Truque de memória): "DOPA SUMO e o PAD dos blocos"
- NARRADOR: Palácio da Memória: última parada, sala de polimento da Forja. Na parede, um lutador de sumô dopado de café: DOPA SUMO.
- CAIXA [Gancho de memória]: **DOPA SUMO**: **DO**bra, **P**ropag**A**, **SU**bexpressão comum, **MO**rto. As quatro otimizações que mais caem.
- CAIXA [Gancho de memória]: Líderes: **PAD**: **P**rimeira instrução, **A**lvo de desvio, **D**epois de desvio.
- CAIXA [Gancho de memória]: **'Faz a conta em casa, não no caixa.'** Dobra de constantes = calcular em compilação o que só depende de literais.
- CAIXA [Gancho de memória]: Código morto: **'se ninguém lê a carta, não precisa escrever'**. Valor não vivo = instrução descartável.
- BYTE: E o juramento?
- PROF: **'Mais rápido, nunca diferente.'** O programa otimizado tem que dar exatamente o mesmo resultado.
- PREVEJA: t1 = 2 * 5; x = t1 + a — aplique DO e PA.
    opções: ✔x = 10 + a | x = 2 * 5 + a | x = t1 + 10
    explicação: DObra: t1 = 10. PropagA: x = 10 + a. E aí o MOrto leva o t1 = 10, se t1 não for usado de novo.
- WIDGET TAC entrada=«a * 4 + 2 * 3» legenda: DOPA SUMO em ação: o 2 * 3 vira 6.
- REVELAR: Recite DOPA SUMO e PAD.
    → Dobra, Propagação, Subexpressão comum, Morto. Líderes: Primeira, Alvo de desvio, Depois de desvio.
- CAIXA [Conversa ao pé da lareira]: Dobra de constantes: 'Eu faço as contas antes do show começar.' Código morto: 'E eu tiro do palco quem não tem fala.' Subexpressão comum: 'E eu não deixo ninguém repetir a mesma piada.'


## ASSUNTO CODEGEN_MIPS: Geração de código MIPS

#### ESTILO STORY (História / Analogia): "A bancada pequena e a despensa enorme"
- NARRADOR: Última estação da Forja do Código. O TAC chega em fichinhas; do outro lado da esteira sai um arquivo .asm que o MARS vai rodar.
- BYTE: MIPS... Eu fico com medo só de ver tanto cifrão.
- PROF: Fica tranquilo: é uma cozinha. O processador é um cozinheiro com uma **bancada pequenininha** e uma **despensa enorme** no fim do corredor.
- TEXTO: Na bancada cabem só 32 potinhos com etiqueta (`$t0`, `$t1`, ...). Ele **só cozinha o que está na bancada**: para usar a farinha, vai à despensa buscar (**lw**) e, quando termina, leva o resultado de volta (**sw**).
- PASSOS (O mapa da analogia):
    · Potinhos da bancada → **registradores** (`$t0`–`$t9`, `$s0`–`$s7`, ...): rápidos e poucos
    · Prateleiras da despensa → **memória**, declarada na seção **.data** (`x: .word 0`)
    · Buscar na despensa → **lw** (*load word*); guardar de volta → **sw** (*store word*)
    · 'Use 2 ovos' (não precisa buscar nada) → **li** (*load immediate*: uma constante)
    · Misturar dois potinhos num terceiro → **add / sub / mul / div**
    · O caderno de passos → seção **.text**, começando no rótulo **main:**
    · Post-it 'se faltou ovo, pule para L0' → **desvios** (beq, bne, j) e **rótulos**
- CÓDIGO (A receita traduzida, uma ficha TAC por bloco):
```
# x = a + b * 2     (TAC: t1 = b * 2 ; t2 = a + t1 ; x = t2)
lw   $t1, b         # busca b na despensa
li   $t2, 2         # pega a constante 2
mul  $t3, $t1, $t2  # mistura
sw   $t3, t1        # guarda na prateleira t1
lw   $t1, a
lw   $t2, t1
add  $t3, $t1, $t2
sw   $t3, t2
lw   $t0, t2
sw   $t0, x
```
- PREVEJA: Cada instrução TAC do tipo t = a op b virou quantas instruções MIPS no código acima?
    opções: 1 | 2 | ✔4 | 10
    explicação: Busca (lw ou li), busca (lw ou li), mistura (op), guarda (sw): quatro. A cópia final x = t2 vira só lw + sw.
- WIDGET MIPS entrada=«a + b * 2» legenda: Rode o 'MARS de bolso' (a=4, b=2) e acompanhe a bancada e a despensa mudando.
- CAIXA [Cuidado!]: `t1` (sem cifrão) é uma **prateleira** da despensa: uma variável na .data que o gerador criou para o temporário. `$t1` (com cifrão) é um **potinho** da bancada. Nomes parecidos, lugares diferentes! No seu trabalho, chamar os temporários de `_t1` evita confusão.
- BYTE: Mas ir à despensa toda hora não é lento?
- PROF: Muito! Um cozinheiro esperto deixa na bancada o que vai usar de novo. Isso tem nome: **alocação de registradores**. Primeiro a gente faz funcionar; depois deixa rápido.
- REVELAR: Na analogia, o que é uma 'syscall'?
    → É tocar a campainha do salão: o cozinheiro pede ao gerente (o sistema / o MARS) algo que ele não faz sozinho, como mostrar o prato ao cliente (imprimir) ou fechar a cozinha (encerrar). O pedido vai em `$v0`: 1 = imprimir inteiro, 4 = imprimir texto, 10 = encerrar.
- CAIXA [No mundo real]: MIPS é a arquitetura didática do livro de Patterson & Hennessy, e o PlayStation 1 e o Nintendo 64 rodavam processadores MIPS. No trabalho prático você usa o MARS: escreve o .asm, monta (F3) e roda (F5).

#### ESTILO VISUAL (Passo a passo visual): "Da árvore ao MARS, instrução por instrução"
- PROF: Vamos ver a mesma expressão em três línguas: árvore, TAC e MIPS.
- WIDGET ATTR_TREE entrada=«(a + b) * c» legenda: A árvore: + embaixo, * na raiz.
- WIDGET TAC entrada=«(a + b) * c» legenda: Pós-ordem: t1 = a + b; t2 = t1 * c.
- WIDGET MIPS entrada=«(a + b) * c» legenda: Cada linha TAC vira um bloquinho. Avance e veja $t1, $t2, $t3 e a memória.
- PASSOS (Lendo o que aconteceu (a=4, b=2, c=3)):
    · lw $t1, a → $t1 = 4;  lw $t2, b → $t2 = 2
    · add $t3, $t1, $t2 → $t3 = 6;  sw $t3, t1 → memória t1 = 6
    · lw $t1, t1 → 6;  lw $t2, c → 3;  mul → $t3 = 18;  sw → memória t2 = 18
    · Cópia final: lw $t0, t2;  sw $t0, r → o resultado r = 18
- PREVEJA: Para a * b + c * d (a=4, b=2, c=3, d=5), qual valor fica no resultado r?
    opções: ✔23 | 70 | 34
    explicação: t1 = 4 * 2 = 8; t2 = 3 * 5 = 15; t3 = 8 + 15 = 23. A precedência já veio resolvida da árvore: os dois * antes do +.
- WIDGET MIPS entrada=«a * b + c * d» legenda: Confira: três blocos de operação e uma cópia final.
- TEXTO: Agora, desvios. Um `if` vira **comparação + desvio + rótulos**. Veja o máximo entre a e b:
- CÓDIGO (if/else em MIPS: a bifurcação e a ponte):
```
# if (a > b) x = a; else x = b;
        lw   $t1, a
        lw   $t2, b
        sgt  $t3, $t1, $t2     # $t3 = 1 se a > b, senão 0
        beq  $t3, $zero, L0    # condição falsa? pula para o else
        sw   $t1, x            # then:  x = a
        j    L1                # pula por cima do else
L0:     sw   $t2, x            # else:  x = b
L1:                            # as duas pistas se encontram aqui
```
- PASSOS (O caminho da execução com a=4, b=2):
    · sgt: 4 > 2 → $t3 = 1
    · beq $t3, $zero, L0: 1 ≠ 0, então NÃO pula
    · sw $t1, x → x = 4 (o then)
    · j L1 → salta por cima do else
    · Com a=1, b=2: $t3 = 0, o beq pula para L0 e x = b
- PREVEJA: Se você esquecer o `j L1`, o que acontece com a=4, b=2?
    opções: Nada muda | ✔Executa o then e DEPOIS cai no else: x termina valendo b | Erro de montagem no MARS
    explicação: O processador não conhece chaves: ele segue linha a linha. Sem o j, ele 'escorrega' do then para dentro de L0 e sobrescreve x com 2. O MARS monta numa boa, o erro é de lógica.
- WIDGET MIPS entrada=«(a + b) / 2» legenda: Bônus: média inteira. O div de três registradores é pseudoinstrução no MARS (por baixo: div + mflo).
- CAIXA [Poder do cérebro]: Repare que o gerador ingênuo faz `sw $t3, t1` e logo depois `lw $t1, t1`: guarda e busca a mesma coisa. Dá para cortar isso? (Spoiler: é a primeira coisa que a alocação de registradores elimina.)

#### ESTILO WORKED (Exemplo resolvido): "Programas .asm completos: expressão, if/else e while"
- PROF: Exercício 1, com cara de trabalho prático: gere o programa MIPS completo para `x = a + b * 2; print(x);`, com a = 4 e b = 2.
- PASSOS (A receita da tradução):
    · 1. TAC: t1 = b * 2;  t2 = a + t1;  x = t2
    · 2. .data: uma `.word` para cada variável E cada temporário
    · 3. .text: cada instrução TAC vira lw/li, lw/li, operação, sw
    · 4. print: `li $v0, 1`, `lw $a0, x`, `syscall`
    · 5. fim: `li $v0, 10`, `syscall`
- CÓDIGO (expr.asm: rode no MARS: o console mostra 8):
```
# x = a + b * 2;  print(x);
# TAC:  t1 = b * 2 ;  t2 = a + t1 ;  x = t2
        .data
a:      .word 4
b:      .word 2
t1:     .word 0            # temporários também moram na memória
t2:     .word 0
x:      .word 0
        .text
main:
        lw   $t1, b        # t1 = b * 2
        li   $t2, 2
        mul  $t3, $t1, $t2
        sw   $t3, t1
        lw   $t1, a        # t2 = a + t1
        lw   $t2, t1
        add  $t3, $t1, $t2
        sw   $t3, t2
        lw   $t0, t2       # x = t2
        sw   $t0, x
        li   $v0, 1        # print(x): syscall 1 = inteiro
        lw   $a0, x
        syscall
        li   $v0, 10       # syscall 10 = encerrar
        syscall
```
- WIDGET MIPS entrada=«a + b * 2» legenda: Confira no simulador: no fim, o resultado (aqui chamado r) vale 8.
- PROF: Exercício 2: if/else completo, com texto na saída. `if (a > b) x = a - b; else x = b - a;` e depois imprime.
- CÓDIGO (dist.asm: saída no MARS: x = 2):
```
# if (a > b) x = a - b; else x = b - a;   print("x = ", x);
        .data
a:      .word 4
b:      .word 2
x:      .word 0
msg:    .asciiz "x = "
        .text
main:
        lw   $t1, a
        lw   $t2, b
        sgt  $t3, $t1, $t2     # cond = (a > b)
        beq  $t3, $zero, L0    # falso → else
        sub  $t3, $t1, $t2     # then: x = a - b
        sw   $t3, x
        j    L1
L0:     sub  $t3, $t2, $t1     # else: x = b - a
        sw   $t3, x
L1:     li   $v0, 4            # imprime o texto
        la   $a0, msg
        syscall
        li   $v0, 1            # imprime o inteiro x
        lw   $a0, x
        syscall
        li   $v0, 10           # encerra
        syscall
```
- CAIXA [Cuidado!]: Cada if do programa precisa de rótulos NOVOS (L0, L1, depois L2, L3...). No gerador, use um contador: `novoRotulo()` devolve L0, L1, L2... Rótulo repetido = erro de montagem no MARS.
- BYTE: Deixa o while comigo! Eu consigo.
- CÓDIGO (Complete as lacunas (A), (B) e (C)):
```
# s = 0; i = 1; while (i <= 3) { s = s + i; i = i + 1; }  print(s);
        li   $t0, 0
        sw   $t0, s
        li   $t0, 1
        sw   $t0, i
L0:     lw   $t1, i            # teste do laço
        li   $t2, 3
        sgt  $t3, $t1, $t2     # $t3 = 1 quando i > 3 (hora de sair)
        ____ $t3, $zero, L1    # (A)
        lw   $t1, s            # s = s + i
        lw   $t2, i
        add  $t3, $t1, $t2
        sw   $t3, ____         # (B)
        lw   $t1, i            # i = i + 1
        addi $t3, $t1, 1
        sw   $t3, i
        ____ ____              # (C)
L1:     ...                    # print(s)
```
- REVELAR: (A): qual desvio?
    → `bne $t3, $zero, L1`: se a condição de SAÍDA (i > 3) é verdadeira (≠ 0), pula para fora do laço. (Equivalente: calcular i <= 3 com `sle` e usar `beq $t3, $zero, L1`.)
- REVELAR: (B) e (C)?
    → (B) `s`: o resultado da soma volta para s. (C) `j L0`: o laço volta para o TESTE, não para o corpo. Esse salto para trás é o que faz o while repetir.
- REVELAR: Quanto vale s na hora do print? Quantas vezes o teste em L0 roda?
    → s = 1 + 2 + 3 = 6. O teste roda 4 vezes: com i = 1, 2, 3 (entra) e i = 4 (sai).
- REVELAR: Agora sem ajuda: o MIPS (estilo do gerador) de x = (a + b) * c.
    → lw $t1, a / lw $t2, b / add $t3, $t1, $t2 / sw $t3, t1 / lw $t1, t1 / lw $t2, c / mul $t3, $t1, $t2 / sw $t3, t2 / lw $t0, t2 / sw $t0, x. Com a=4, b=2, c=3: x = 18.
- WIDGET MIPS entrada=«(a + b) * c» legenda: Confira o último exercício.
- CAIXA [Dica de prova]: Para testar no MARS: Assemble (F3), depois Go (F5), e olhe o painel Run I/O. Deu diferente? Use Step (F7) e acompanhe a aba Registers e o Data Segment, comparando com o que você calculou à mão.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que tanto lw e sw?"
- PROF: Vou só fazer perguntas. Você vai descobrir sozinho como se gera código MIPS.
- REVELAR: No MIPS, `add` só aceita registradores. Então como somar duas variáveis que estão na memória?
    → Primeiro trazê-las para registradores (lw), somar, e devolver o resultado à memória (sw). O MIPS é uma arquitetura **load/store**: só lw/sw tocam a memória; todo o resto trabalha em registradores.
- REVELAR: E se um operando é uma constante, como em b * 2?
    → Não há o que buscar na memória: usa-se `li $t2, 2` (carrega imediato). Para somar uma constante existe até atalho: `addi $t3, $t1, 2`.
- REVELAR: Onde as variáveis e os temporários 'moram' no estilo do trabalho prático?
    → Na seção .data, uma `.word` para cada um: `x: .word 0`. É simples e sempre funciona, o preço é acessar a memória o tempo todo.
- BYTE: Mas e o if? MIPS não tem chaves...
- REVELAR: Como fazer 'se a > b, faça X; senão, faça Y' tendo só 'pule para tal linha'?
    → Calcule a condição num registrador (sgt/slt deixam 1 ou 0). Se ela for 0, pule para o rótulo do else (`beq $t3, $zero, L0`). No fim do then, um `j L1` pula o else.
- REVELAR: E o while? Que desvio faz ele repetir?
    → Um `j` para TRÁS, de volta ao rótulo do teste. O teste fica no começo: se falhar, um desvio para FRENTE sai do laço.
- PREVEJA: Para que serve o registrador `$zero`?
    opções: Guarda o último resultado que deu zero | ✔Vale sempre 0 — ótimo para comparar com 'falso' (beq $t3, $zero, L0) e para copiar | Conta quantas instruções já rodaram
    explicação: Escrever nele não adianta: ele é fixo em 0. `beq $t3, $zero, L0` se lê 'se $t3 é falso, pule'. E `move $t0, $t1` é, por baixo, uma soma com `$zero`.
- REVELAR: Como o programa imprime algo, se o processador nem sabe o que é tela?
    → Pede ao sistema: põe o código do serviço em `$v0` (1 = inteiro, 4 = string, 10 = sair), o argumento em `$a0` (o número, ou o endereço da string, obtido com `la`) e executa `syscall`.
- BYTE: E dá pra gastar menos lw e sw?
- REVELAR: Em x = a + b * 2, o gerador ingênuo faz 7 acessos à memória. Quantos são realmente necessários?
    → Três: buscar a, buscar b e guardar x. Os temporários podem viver em registradores: `lw $t0, a / lw $t1, b / li $t2, 2 / mul $t1, $t1, $t2 / add $t0, $t0, $t1 / sw $t0, x`. Decidir quem fica em qual registrador é a **alocação de registradores**.
- WIDGET MIPS entrada=«a + b * 2» legenda: Conte os lw e sw do gerador ingênuo e compare com a versão de 3 acessos.
- CAIXA [Poder do cérebro]: Por que não pôr TUDO em registradores? Porque são só 32 (e vários reservados). Quando faltam, alguém volta para a memória, o famoso **spill**. Escolher bem quem fica é um problema difícil (vira coloração de grafos!).

#### ESTILO FORMAL (Definição de prova): "Esquemas de tradução TAC → MIPS"
- TEXTO: **Gerador de código:** fase final, que mapeia a representação intermediária (TAC) em instruções da máquina-alvo. Problemas centrais: **seleção de instruções**, **alocação de registradores** e **ordem de avaliação** (Livro do Dragão, cap. 8).
- CÓDIGO (Registradores MIPS (32 bits) usados no trabalho):
```
$zero         constante 0
$v0–$v1       valores de retorno / código da syscall
$a0–$a3       argumentos
$t0–$t9       temporários (não preservados na chamada)
$s0–$s7       salvos (preservados na chamada)
$sp $fp $ra   topo da pilha, base do quadro, endereço de retorno
```
- CÓDIGO (O subconjunto do MIPS que um gerador simples precisa):
```
lw   rd, rot       rd := Mem[rot]
sw   rs, rot       Mem[rot] := rs
li   rd, k         rd := k                    (pseudo)
la   rd, rot       rd := endereço de rot      (pseudo)
add  rd, rs, rt    rd := rs + rt     (sub, mul, div análogos)
addi rd, rs, k     rd := rs + k
slt  rd, rs, rt    rd := (rs < rt) ? 1 : 0    (sgt, seq: pseudo)
beq  rs, rt, L     se rs = rt, desvia para L  (bne: se ≠)
j    L             desvia para L
syscall            serviço em $v0: 1 int, 4 string, 10 sair
```
- CÓDIGO (Esquema TAC → MIPS no estilo 'tudo na memória' (o do jogo e do trabalho)):
```
gera(x = y op z):
    carrega("$t1", y);  carrega("$t2", z)
    emite(opMips(op) + " $t3, $t1, $t2")
    emite("sw $t3, " + x)

gera(x = y):
    carrega("$t0", y);  emite("sw $t0, " + x)

carrega(r, v):
    se v é constante: emite("li " + r + ", " + v)
    senão:            emite("lw " + r + ", " + v)
```
- CÓDIGO (Esquemas para comandos (rótulos novos a cada uso)):
```
if (E) S1 else S2              while (E) S
    <E em $t3>                 Linicio:
    beq $t3, $zero, Lelse          <E em $t3>
    <código de S1>                 beq $t3, $zero, Lfim
    j Lfim                         <código de S>
Lelse:                             j Linicio
    <código de S2>             Lfim:
Lfim:
```
- PREVEJA: Pelo esquema 'tudo na memória', quantas instruções MIPS gera x = a + b * c?
    opções: 6 | 8 | ✔10
    explicação: Dois operadores × 4 instruções (lw, lw, op, sw) = 8, mais 2 da cópia x = t2 (lw, sw) = 10.
- WIDGET MIPS entrada=«a + b * c» legenda: Conte as instruções: 10.
- TEXTO: **Custo:** num processador real, acessar a memória é bem mais caro que usar um registrador. O critério clássico de qualidade é **minimizar lw/sw**: manter valores vivos em registradores, reaproveitar resultados e eliminar pares `sw t1` / `lw t1` consecutivos.
- CAIXA [Dica de prova]: Em 'gere o código MIPS', escreva SEMPRE as duas seções (.data com variáveis e temporários; .text começando em main:), comente cada bloco com a instrução TAC correspondente e termine com `li $v0, 10` + `syscall`. Sem isso o MARS até para ('dropped off bottom'), mas o professor desconta.
- CAIXA [Cuidado!]: Em `lw` e `sw` o REGISTRADOR vem primeiro nos dois, mas a direção muda: `lw $t1, a` leva a → $t1; `sw $t1, a` leva $t1 → a. E o `div` de três operandos é pseudo: o real é `div $t1, $t2` seguido de `mflo $t3` (quociente; o resto fica em hi, lido com `mfhi`).

#### ESTILO MNEMONIC (Truque de memória): "Busca, busca, mistura, guarda"
- NARRADOR: Palácio da Memória: Forja do Código, última sala, a Montadora MIPS. Na parede, um relógio de cozinha marca quatro tempos.
- CAIXA [Gancho de memória]: **'Busca, busca, mistura, guarda.'** Toda instrução TAC x = y op z vira: lw (busca y), lw (busca z), op (mistura), sw (guarda em x). Ritmo de samba: 1, 2, 3, 4.
- CAIXA [Gancho de memória]: **Load = Leva para a bancada; Store = Salva na despensa.** Nos dois, o registrador vem PRIMEIRO: `lw $t1, a` e `sw $t1, a` — só muda a direção da seta.
- CAIXA [Gancho de memória]: **Família de registradores pela inicial:** `$t` = **t**emporário (pode sujar), `$s` = **s**alvo (guarde antes de usar), `$a` = **a**rgumento, `$v` = **v**alor de retorno, `$ra` = **r**eturn **a**ddress, `$sp` = **s**tack **p**ointer.
- CAIXA [Gancho de memória]: **beq = Branch if EQual (pula se igual); bne = Branch if Not Equal.** O if testa AO CONTRÁRIO: 'se a condição é falsa (igual a $zero), pula o then'.
- CAIXA [Gancho de memória]: **syscall 1, 4, 10: 'um número, uma fila de letras e dez-liga'.** 1 imprime inteiro; 4 imprime string ('fila' tem 4 letras; use `la $a0, msg`); 10 encerra (dez... desliga!).
- BYTE: E o if com rótulos? Eu sempre me perco.
- PROF: Pense numa estrada com **bifurcação**. O `beq` é a placa 'falso? vire para L0'. O then é a pista reta; o `j L1` no fim dele é a **ponte** que passa por cima do else. L1 é onde as duas pistas se encontram.
- PREVEJA: No esqueleto do while, para onde vai o `j` do fim do corpo?
    opções: Para o rótulo do fim do laço | ✔Para o rótulo do TESTE, no começo | Para main
    explicação: while = 'volta e testa de novo'. O único salto para trás é esse j; o desvio para FRENTE (beq/bne) é a porta de saída.
- WIDGET MIPS entrada=«d * (a - c)» legenda: Bata o ritmo: busca, busca, mistura, guarda, duas vezes, e a cópia final. (d=5, a=4, c=3 → 5)
- REVELAR: Recite: as duas seções de um .asm e o que vai em cada uma.
    → .data: as variáveis (`x: .word 0`) e as strings (`msg: .asciiz "..."`). .text: as instruções, começando em `main:` e terminando com `li $v0, 10` + `syscall`.
- CAIXA [Conversa ao pé da lareira]: Memória: 'Eu guardo tudo, sou enorme!' Registrador: 'E eu sou muito mais rápido.' Memória: 'Mas só cabem 32 de vocês...' Alocador de registradores: 'Calma, gente, é para isso que eu existo.'


## ASSUNTO RUNTIME: Ambientes de execução (registros de ativação)

#### ESTILO STORY (História / Analogia): "A pilha de pratos da cozinha"
- NARRADOR: Forja do Código, fim de expediente. O código já foi gerado... mas, quando ele RODA, onde moram as variáveis de cada função?
- BYTE: Na memória, ué. Cada variável tem o seu lugarzinho fixo, não tem?
- PROF: Quase! Pense na cozinha de um restaurante em dia de movimento. Cada pedido que começa vira um prato na pilha da bancada, com um bilhete grudado.
- TEXTO: O bilhete diz: **quais ingredientes** vieram no pedido, **para qual mesa** devolver o prato pronto e tem um espaço para os **rascunhos** do cozinheiro. E ele só mexe no prato do **topo**.
- TEXTO: Se, no meio de um prato, ele precisa de um molho que é outra receita, põe um **prato novo em cima**, faz o molho, tira esse prato e volta para o de baixo exatamente de onde parou.
- PASSOS (O mapa da analogia):
    · Cada pedido em preparo → uma **ativação** (uma chamada de função em andamento)
    · Prato com bilhete → **registro de ativação** (ou **quadro**, *frame*)
    · Ingredientes do pedido → **parâmetros**
    · Para qual mesa devolver → **endereço de retorno** (no MIPS, o registrador `$ra`)
    · Rascunhos do cozinheiro → **variáveis locais** e **temporários**
    · Pilha de pratos na bancada → **pilha de execução**: só o topo trabalha
    · Molho que precisa de outro molho da MESMA receita → **recursão**: vários pratos iguais empilhados
- PILHA: Prato novo... vai em cima. Prato pronto... sai de cima. Eu nunca... tiro do meio.
- CÓDIGO (O fatorial recursivo: uma receita que pede a si mesma):
```
1 int fat(int n) {
2   if (n <= 1) return 1;
3   return n * fat(n - 1);
4 }
5 int main() {
6   return fat(3);
7 }
```
- PREVEJA: Rodando fat(3), quantos pratos (registros de ativação) ficam empilhados no momento MAIS alto, contando o de main?
    opções: 2 | 3 | ✔4 | 6
    explicação: main → fat(3) → fat(2) → fat(1): quatro quadros ao mesmo tempo. Só quando fat(1) devolve 1 a pilha começa a baixar. Se você chutou 3, esqueceu do prato do main, erro supercomum!
- WIDGET CALL_STACK entrada=«fat3» legenda: Empilhe e desempilhe: cada fat(n) tem o SEU próprio n no bilhete.
- CAIXA [Não existem perguntas idiotas]: 'Se todos os quadros são da mesma função fat, por que o n não se mistura?' Porque cada chamada ganha um prato NOVO, com o seu próprio n. O n=3 fica guardadinho embaixo enquanto o n=1 trabalha no topo.
- TEXTO: E o resto da cozinha? O **cardápio** pregado na parede é o **código**; as **placas fixas** (nome da casa, horário) são a **área estática** (variáveis globais); o **depósito**, de onde se pega e devolve caixa a qualquer hora, é o **heap** (`new`, `malloc`).
- REVELAR: Por que o depósito (heap) existe, se a pilha já funciona tão bem?
    → Porque o prato da pilha é jogado fora quando a função termina. Se você precisa de algo que viva DEPOIS do retorno, um objeto criado com `new`, uma lista que cresce, ele vai para o heap.
- CAIXA [No mundo real]: Recursão sem caso base = pratos empilhados até o teto. Em Java isso é o famoso `StackOverflowError`; em C, geralmente um *segmentation fault*. É o prato batendo no lustre.

#### ESTILO VISUAL (Passo a passo visual): "A pilha de chamadas, quadro a quadro"
- PROF: Hoje a gente assiste à memória durante a execução. Primeiro, um programa sem recursão: main chama f, que chama g, que chama h.
- CÓDIGO (f chama g, g chama h... e f ainda chama h de novo):
```
1 int h(int a) {
2   return a + 1;
3 }
4 int g(int b) {
5   return h(b * 2);
6 }
7 int f(int c) {
8   return g(c) + h(c);
9 }
10 int main() {
11  return f(1);
12 }
```
- WIDGET CALL_STACK entrada=«chain1» legenda: Avance passo a passo: cada chamada EMPILHA um quadro; cada return DESEMPILHA.
- PASSOS (O que você acabou de ver):
    · main chama f(1): pilha [main, f]
    · f chama g(1): [main, f, g]
    · g chama h(2): [main, f, g, h], o ponto mais alto, 4 quadros
    · h devolve 3 e sai; g devolve 3 e sai: [main, f]
    · f chama h(1) DE NOVO: [main, f, h], o mesmo h, um quadro novinho
    · h devolve 2; f devolve 3 + 2 = 5; main devolve 5
- BYTE: O h apareceu duas vezes, em alturas diferentes da pilha!
- PROF: Exato. O quadro depende de QUEM chamou e QUANDO, não só do nome da função. Por isso cada quadro guarda o seu próprio endereço de retorno.
- PREVEJA: Agora fat(4). Qual a altura máxima da pilha, contando main?
    opções: 4 | ✔5 | 8
    explicação: main + fat(4) + fat(3) + fat(2) + fat(1) = 5. Regra prática: profundidade da recursão + 1 (o main).
- WIDGET CALL_STACK entrada=«fat4» legenda: Confira: a pilha sobe até fat(1) e depois desce devolvendo 1, 2, 6, 24.
- TEXTO: Agora uma recursão que **ramifica**: fib(3) chama fib(2) e fib(1). O desenho de TODAS as chamadas forma a **árvore de ativação**.
- CÓDIGO (Árvore de ativação de fib(3): 6 ativações no total (com main)):
```
          main
           |
         fib(3)
        /      \
    fib(2)    fib(1)
    /    \
fib(1)  fib(0)
```
- WIDGET CALL_STACK entrada=«fib3» legenda: Repare: em cada instante, a pilha é o CAMINHO da raiz até o nó que está executando.
- PREVEJA: Em fib(3), no instante em que fib(0) executa, quem está na pilha?
    opções: main, fib(3), fib(2), fib(1), fib(0) | ✔main, fib(3), fib(2), fib(0) | Só fib(0)
    explicação: O fib(1) da esquerda já terminou e saiu. A pilha guarda só os ancestrais vivos: main → fib(3) → fib(2) → fib(0). Altura máxima de fib3: 4.
- CAIXA [Poder do cérebro]: A árvore de ativação conta a história inteira; a pilha mostra só o 'agora'. Percorrer a árvore em profundidade (desce = chama, sobe = retorna) é exatamente o que a pilha faz.

#### ESTILO WORKED (Exemplo resolvido): "fat(3) na mão: e em MIPS"
- PROF: Exercício clássico: rode fat(3) à mão, mostrando a pilha de registros de ativação e os valores devolvidos.
- CÓDIGO (O programa):
```
1 int fat(int n) {
2   if (n <= 1) return 1;
3   return n * fat(n - 1);
4 }
5 int main() {
6   return fat(3);
7 }
```
- PASSOS (Resolvido, chamada por chamada):
    · 1. main chama fat(3): quadro {n=3, volta para: main, linha 6}
    · 2. 3 > 1, então fat(3) precisa de fat(2): quadro {n=2, volta para: fat, linha 3}
    · 3. fat(2) chama fat(1): quadro {n=1, volta para: fat, linha 3}. Pilha: main, fat(3), fat(2), fat(1) — 4 quadros
    · 4. fat(1) cai no caso base, devolve 1 e o seu quadro é desempilhado
    · 5. fat(2) retoma na linha 3: 2 * 1 = 2; devolve 2 e sai
    · 6. fat(3) retoma: 3 * 2 = 6; devolve 6 e sai
    · 7. main recebe 6
- TEXTO: Agora o mesmo fatorial em MIPS, do jeito que o MARS roda. O quadro de cada chamada tem 2 palavras (8 bytes): o `$ra` e o `n`.
- TEXTO: Antes do programa inteiro, as quatro peças novas, uma de cada vez.
- CÓDIGO ($sp: o topo da pilha):
```
addi $sp, $sp, -8  # abre 8 bytes
```
- TEXTO: `$sp` (stack pointer, ponteiro da pilha) guarda o endereço do topo da pilha. No MIPS, a pilha cresce para endereços menores: subtrair 8 do `$sp` abre 8 bytes, duas palavras de 4 bytes. Somar 8 fecha esse espaço.
- CÓDIGO (4($sp): o deslocamento):
```
sw $ra, 4($sp)  # em $sp + 4
sw $a0, 0($sp)  # em $sp + 0
```
- TEXTO: `4($sp)` quer dizer: o endereço que está em `$sp`, mais 4. O número antes do parêntese é o deslocamento, em bytes. `0($sp)` é a primeira palavra do quadro, e `4($sp)` é a segunda. `sw` grava um registrador nesse endereço, e `lw` lê de volta.
- PREVEJA: Se `$sp` vale 1000, em que endereço `sw $ra, 4($sp)` grava?
    opções: 4 | 996 | 1000 | ✔1004
    explicação: 1004: o endereço do `$sp` mais o deslocamento 4. O `$sp` não muda; quem muda o `$sp` é o `addi`.
- CÓDIGO (jal: chamar a função):
```
jal fat         # $ra = linha de baixo
move $a0, $v0   # a volta é aqui
```
- TEXTO: `jal` (jump and link, pular e ligar) faz duas coisas: pula para o rótulo `fat` e guarda em `$ra` o endereço da instrução logo abaixo do `jal`. É por ali que o programa continua quando `fat` terminar.
- CÓDIGO ($ra: o endereço de volta):
```
jr $ra  # pula para o endereço em $ra
```
- TEXTO: `$ra` (return address, endereço de retorno) é o registrador da volta. `jr $ra` pula para esse endereço: é o `return` do C. Cada `jal` escreve por cima do `$ra`. Por isso, uma função que chama outra guarda o seu `$ra` na pilha antes, com `sw $ra, 4($sp)`.
- PREVEJA: Dentro de `fat(3)`, o `jal fat` da recursão acabou de rodar. O que está no `$ra` agora?
    opções: A volta para o `main` | ✔A volta para a linha logo depois desse `jal`, dentro de `fat` | Zero, porque a função ainda não terminou
    explicação: A linha depois desse `jal`, dentro de `fat`. A volta para o `main` foi apagada, e só não se perdeu porque estava guardada em `4($sp)`. No fim, `lw $ra, 4($sp)` a traz de volta antes do `jr $ra`.
- CÓDIGO (fat.asm: prólogo abre o quadro, epílogo fecha):
```
        .text
main:   li   $a0, 3            # argumento: n = 3
        jal  fat               # chama fat; $ra = endereço da linha de baixo
        move $a0, $v0          # o resultado chega em $v0
        li   $v0, 1            # syscall 1: imprimir inteiro
        syscall                # imprime 6
        li   $v0, 10           # syscall 10: encerrar
        syscall

fat:    addi $sp, $sp, -8      # PRÓLOGO: abre o quadro (2 palavras)
        sw   $ra, 4($sp)       # guarda o endereço de retorno
        sw   $a0, 0($sp)       # guarda n
        slti $t0, $a0, 2       # $t0 = (n < 2)
        beq  $t0, $zero, REC   # n >= 2? vai para a recursão
        li   $v0, 1            # caso base: devolve 1
        addi $sp, $sp, 8       # fecha o quadro
        jr   $ra
REC:    addi $a0, $a0, -1      # argumento n - 1
        jal  fat               # $v0 = fat(n - 1)
        lw   $a0, 0($sp)       # recupera o SEU n
        lw   $ra, 4($sp)       # recupera o SEU endereço de retorno
        addi $sp, $sp, 8       # EPÍLOGO: fecha o quadro
        mul  $v0, $a0, $v0     # n * fat(n - 1)
        jr   $ra               # volta para quem chamou
```
- PASSOS (Quem faz o quê na chamada):
    · **Chamador:** põe o argumento em `$a0` e executa `jal fat` (que guarda o endereço de volta em `$ra`)
    · **Chamado, na entrada (prólogo):** `addi $sp, $sp, -8` abre o quadro; salva `$ra` e `$a0` nele
    · **Chamado, na saída (epílogo):** põe o resultado em `$v0`, restaura `$ra`, `addi $sp, $sp, 8` fecha o quadro e `jr $ra` volta
    · **Chamador, depois:** usa o valor que chegou em `$v0`
- BYTE: Deixa eu tentar um sozinho? Acho que eu consigo.
- CÓDIGO (Sua vez: main chama f(2)):
```
1 int h(int a) {
2   return a + 1;
3 }
4 int g(int b) {
5   return h(b * 2);
6 }
7 int f(int c) {
8   return g(c) + h(c);
9 }
10 int main() {
11  return f(2);
12 }
```
- REVELAR: Qual a pilha no ponto mais alto?
    → [main, f(c=2), g(b=2), h(a=4)], 4 quadros. h recebeu 4 porque g passou b * 2.
- REVELAR: Que valor cada chamada devolve?
    → h(4) = 5; g(2) = 5; depois f chama h(2) = 3; f devolve 5 + 3 = 8; main devolve 8.
- REVELAR: Para onde volta o SEGUNDO h, o h(2)?
    → Para f, na linha 8 (a soma). Esse endereço de retorno está no quadro dele, não é o do primeiro h, que voltava para g, linha 5.
- WIDGET CALL_STACK entrada=«chain2» legenda: Confira as suas três respostas.
- CAIXA [Cuidado!]: Esqueceu de salvar `$ra` antes do `jal` interno? A chamada recursiva sobrescreve `$ra`, e o `jr $ra` final volta para o lugar errado — o programa entra em laço. Função que chama outra SEMPRE salva `$ra` na pilha.
- CAIXA [Dica de prova]: Na prova, desenhe a pilha como uma coluna de caixas, uma por chamada, com os parâmetros e o 'volta para'. Risque (não apague) as caixas que saem: o professor quer ver a ordem.

#### ESTILO SOCRATIC (Perguntas guiadas): "Por que uma PILHA?"
- PROF: Vamos reinventar o ambiente de execução só com perguntas. Responda de cabeça antes de tocar, errar aqui é parte do método.
- REVELAR: Ideia mais simples: cada variável de cada função ganha um endereço fixo (alocação estática). O que dá errado com fat(3)?
    → Só existe UM n para a função fat. Quando fat(2) escreve n=2, destrói o n=3 que fat(3) ainda vai usar na volta. Alocação estática não suporta recursão (o FORTRAN antigo nem permitia recursão por isso).
- REVELAR: Então cada CHAMADA precisa da sua própria cópia de n. Em que ordem essas cópias nascem e morrem?
    → A última chamada a começar é a primeira a terminar (LIFO): fat(1) nasce por último e morre primeiro. Esse é exatamente o comportamento de uma pilha.
- BYTE: Tá, mas além do n, o que mais o quadro precisa guardar?
- REVELAR: Quando fat(1) termina, como a máquina sabe para onde voltar?
    → Ela guardou o **endereço de retorno** na hora da chamada. No MIPS, `jal` coloca esse endereço em `$ra`; como a próxima chamada sobrescreveria `$ra`, ele é salvo no quadro.
- REVELAR: E como devolver o resultado e voltar a usar o quadro de quem chamou?
    → O resultado vai num lugar combinado (no MIPS, `$v0`). Para reencontrar o quadro anterior, guarda-se o **vínculo de controle** (link dinâmico), um ponteiro para o quadro do chamador — ou, no MIPS simples, basta desfazer o `$sp`.
- PREVEJA: Quem deve ABRIR o espaço das variáveis LOCAIS de uma função?
    opções: O chamador, porque ele sabe quantos parâmetros passa | ✔O chamado, porque só ele sabe quantas locais e temporários tem | O sistema operacional
    explicação: O tamanho das locais é 'segredo' do chamado (o chamador pode até ter sido compilado antes). Regra geral: o chamador cuida do que conhece (parâmetros, endereço de retorno); o chamado cuida do resto.
- REVELAR: Uma função cria uma lista e a devolve para quem chamou. A lista pode morar no quadro dela?
    → Não! O quadro é desempilhado no retorno, e quem chamou ficaria com um ponteiro para lixo (ponteiro pendente). Coisas que precisam sobreviver à chamada vão para o **heap**.
- REVELAR: Recursão infinita: o que acontece com a pilha?
    → Cada chamada empilha um quadro e nenhuma sai. A pilha cresce até invadir uma área proibida: **estouro de pilha** (stack overflow).
- BYTE: E passar parâmetro por referência? Muda alguma coisa no quadro?
- REVELAR: Em swap(x, y) por VALOR, o que vai para o quadro? E por REFERÊNCIA?
    → Por valor: CÓPIAS de x e y, trocar as cópias não muda nada lá fora. Por referência: os ENDEREÇOS de x e y, o chamado escreve direto nas variáveis do chamador, e a troca acontece de verdade.
- WIDGET CALL_STACK entrada=«fib3» legenda: Teste: antes de cada passo, preveja se a pilha sobe ou desce.
- CAIXA [Poder do cérebro]: Repare no que você acabou de deduzir: pilha (por causa do LIFO), endereço de retorno, vínculo de controle, heap e passagem de parâmetros. É isso que o capítulo 7 do Livro do Dragão formaliza. Você não é 'burro': você acabou de reinventar um ambiente de execução.

#### ESTILO FORMAL (Definição de prova): "Organização da memória e registros de ativação"
- TEXTO: **Ambiente de execução** (*run-time environment*): o conjunto de estruturas que o código gerado usa para rodar, organização da memória, registros de ativação, sequências de chamada e passagem de parâmetros.
- CÓDIGO (Subdivisão típica da memória em tempo de execução (Livro do Dragão, seção 7.1)):
```
endereços altos
+----------------------+
|        PILHA         |  topo em $sp; cresce para BAIXO
|          v           |
|                      |
|          ^           |
|         HEAP         |  cresce para CIMA (new, malloc)
+----------------------+
|  ESTÁTICO  (.data)   |  globais e constantes; tamanho fixo
+----------------------+
|  CÓDIGO    (.text)   |  as instruções do programa
+----------------------+
endereços baixos
```
- CÓDIGO (Registro de ativação (quadro): campos típicos (seção 7.2.2)):
```
+--------------------------------+
| parâmetros reais               |  preenchidos pelo CHAMADOR
| valor de retorno               |
| vínculo de controle (dinâmico) |  -> quadro do chamador
| vínculo de acesso (estático)   |  -> quadro do escopo envolvente
| estado salvo da máquina        |  endereço de retorno, registradores
| dados locais                   |  preenchidos pelo CHAMADO
| temporários                    |
+--------------------------------+
```
- PASSOS (Sequência de chamada e de retorno):
    · **Chamador:** avalia os parâmetros reais e os coloca onde o chamado espera (no quadro, ou em `$a0`–`$a3`)
    · **Chamador:** registra o endereço de retorno e o vínculo de controle; desvia para o chamado (`jal`)
    · **Chamado (prólogo):** aloca o quadro (`$sp` -= tamanho), salva registradores (incluindo `$ra`) e inicializa as locais
    · **Chamado (retorno):** põe o valor de retorno no lugar combinado (`$v0`), restaura registradores e `$sp`, desvia para o endereço de retorno (`jr $ra`)
    · **Chamador:** recupera o valor devolvido e continua
- TEXTO: **Árvore de ativação:** cada nó é uma ativação; os filhos de um nó são as ativações que ele chamou, da esquerda para a direita, na ordem das chamadas. **Propriedade:** a pilha de controle, num dado instante, contém exatamente o caminho da raiz até o nó em execução.
- PASSOS (Três estratégias de alocação):
    · **Estática:** endereço fixo em tempo de compilação; não permite recursão nem tamanho dinâmico (globais; FORTRAN 77)
    · **Em pilha:** um quadro por ativação, LIFO; permite recursão; o quadro morre no retorno (locais em C e Java)
    · **Em heap:** blocos com tempo de vida arbitrário; liberação manual (`free`) ou por coleta de lixo
- CÓDIGO (Convenção de chamada do MIPS):
```
$a0–$a3   argumentos           $v0–$v1  valores de retorno
$ra       endereço de retorno  $sp      topo da pilha
$fp       base do quadro atual (opcional)
$t0–$t9   temporários: o CHAMADOR salva se precisar deles depois
$s0–$s7   salvos: o CHAMADO salva antes de usar

PC        contador de programa: o endereço da instrução que roda agora

jal f     # $ra := PC + 4 (a instrução de baixo);  PC := f
jr  $ra   # PC := $ra
```
- PREVEJA: Qual vínculo aponta para o quadro de QUEM CHAMOU a função?
    opções: Vínculo de acesso (estático) | ✔Vínculo de controle (dinâmico) | Endereço de retorno
    explicação: Controle/dinâmico = cadeia de CHAMADAS. Acesso/estático = cadeia de ANINHAMENTO no texto (útil em linguagens com funções aninhadas, como Pascal). O endereço de retorno aponta para uma INSTRUÇÃO, não para um quadro.
- WIDGET CALL_STACK entrada=«fat3» legenda: A definição em ação: 4 ativações simultâneas no pico (main + 3 de fat).
- CAIXA [Dica de prova]: Questão clássica: 'desenhe o registro de ativação e diga quem preenche cada campo'. Parâmetros, valor de retorno (espaço) e endereço de retorno: o chamador. Locais, temporários e registradores salvos: o chamado. Liste de cima para baixo nessa ordem.
- CAIXA [Cuidado!]: Endereço de retorno ≠ vínculo de controle. O primeiro diz para qual INSTRUÇÃO voltar; o segundo, qual QUADRO volta a ser o atual.

#### ESTILO MNEMONIC (Truque de memória): "O prédio da memória e o café da manhã do quadro"
- NARRADOR: Palácio da Memória: Forja do Código, penúltima sala. Um prédio de quatro andares; do telhado, uma pilha de caixas desce sozinha.
- CAIXA [Gancho de memória]: **O prédio, de baixo para cima: C-E-H-P (leia 'CEP', o endereço de tudo).** **C**ódigo no térreo, **E**stático no 1º andar, **H**eap subindo do 2º, **P**ilha descendo do telhado.
- CAIXA [Gancho de memória]: **'Heap sobe, pilha desce; se os dois se encontram, o prédio cai.'** É o estouro de memória.
- CAIXA [Gancho de memória]: **Controle Chama, Acesso Aninha.** Vínculo de **c**ontrole → quem me **c**hamou (cadeia dinâmica). Vínculo de **a**cesso → onde estou **a**ninhado no texto (cadeia estática).
- CAIXA [Gancho de memória]: **O café da manhã do quadro: Pão, Vitamina, Café, Açúcar, Esfirra, Leite, Torrada** → **P**arâmetros, **V**alor de retorno, vínculo de **C**ontrole, vínculo de **A**cesso, **E**stado salvo (endereço de retorno), **L**ocais, **T**emporários.
- CAIXA [Gancho de memória]: **jal = Jump And Link**: pula e deixa um bilhete de volta em `$ra` (**r**eturn **a**ddress). **jr $ra = Jump Register**: volta seguindo o bilhete.
- BYTE: E pra lembrar quem faz o quê na chamada? Eu sempre troco.
- PROF: Recepção de hotel! O **hóspede** (chamador) chega com a bagagem e diz quando volta para casa: parâmetros e endereço de retorno. O **hotel** (chamado) arruma o quarto e o frigobar: locais e registradores salvos. Na saída, o hotel entrega a conta (valor de retorno) e libera o quarto.
- PREVEJA: Em main → f → g → h (h chamado por g), pelo gancho 'Controle Chama', o vínculo de controle de h aponta para o quadro de...
    opções: main | f | ✔g | h
    explicação: Controle = quem CHAMOU. Quem chamou h foi g. (E quando f chama h direto, o vínculo de controle desse OUTRO quadro de h aponta para f.)
- WIDGET CALL_STACK entrada=«chain1» legenda: Suba e desça o prédio: cada andar da pilha é um quadro.
- REVELAR: Recite os 7 campos do quadro pelo café da manhã.
    → Pão, Vitamina, Café, Açúcar, Esfirra, Leite, Torrada → parâmetros, valor de retorno, vínculo de controle, vínculo de acesso, estado salvo (endereço de retorno), locais, temporários.
- CAIXA [Conversa ao pé da lareira]: Pilha: 'Eu sou rápida e organizada: entrou, saiu, tudo limpinho.' Heap: 'E eu sou flexível: guardo o que você quiser pelo tempo que quiser.' Pilha: 'E esquece de devolver...' Heap: 'Por isso inventaram o coletor de lixo!'


## ASSUNTO REG_ALLOC: Alocação de registradores

#### ESTILO STORY (História / Analogia): "Poucas bancadas na cozinha"
- NARRADOR: Cozinha de restaurante em dia de casamento. A despensa no subsolo guarda tudo, mas cada viagem de escada leva uma eternidade. Lá em cima há só três bancadas.
- BYTE: É a cozinha da aula de MIPS! Bancada é registrador, despensa é memória.
- PROF: Isso. E o chef novo faz uma burrice: termina o molho, LEVA para a despensa, e cinco minutos depois desce de novo para BUSCAR o mesmo molho. É o código ingênuo: `sw` seguido de `lw` do mesmo temporário.
- PROF: O chef experiente pensa diferente: 'O molho vai ser usado logo, fica numa bancada. Quando o molho for usado pela última vez, a bancada fica LIVRE para outra preparação.'
- PASSOS (Cozinha → alocação de registradores):
    · Cada preparação intermediária (molho, massa) é um **temporário** (t1, t2, ...).
    · Cada bancada é um **registrador**. Só há k bancadas.
    · Do momento em que a preparação fica pronta até seu ÚLTIMO uso, ela está **viva** e ocupa uma bancada: esse é o seu **intervalo de vida**.
    · Duas preparações vivas AO MESMO TEMPO não podem dividir bancada: elas **interferem**.
    · Dar uma bancada a cada preparação, sem que vizinhas briguem, é **colorir um grafo**: cada cor é um registrador.
    · Se não houver bancadas suficientes, alguma preparação desce para a despensa e é buscada de novo quando precisar: é o **spill** (derramamento).
- CAIXA [No mundo real]: É o mesmo problema de pintar o mapa do Brasil: estados que fazem fronteira não podem ter a mesma cor. Na alocação, a 'fronteira' é estar vivo ao mesmo tempo. Quatro cores bastam para qualquer mapa plano, mas o grafo de um programa não é um mapa: às vezes precisa de mais.
- PREVEJA: Em r = a * b + c * d: t1 = a * b fica pronto; depois t2 = c * d; depois t3 = t1 + t2. Quais temporários precisam de bancadas diferentes?
    opções: ✔t1 e t2 | t1 e t3 | t2 e t3 | Todos os três
    explicação: Quando t2 fica pronto, t1 ainda espera para ser usado: os dois estão vivos juntos. Já t3 nasce exatamente quando t1 e t2 morrem (são usados pela última vez), então t3 pode reaproveitar a bancada de qualquer um deles.
- WIDGET REGALLOC entrada=«a * b + c * d» legenda: Veja os vivos, o grafo (só uma aresta: t1—t2) e a coloração com 2 registradores.
- REVELAR: Quantas viagens à despensa (lw + sw) o código ingênuo faz nesse cálculo, e quantas depois da alocação?
    → Ingênuo: 11 (7 lw + 4 sw). Com 2 registradores: 5 (4 lw das variáveis a, b, c, d + 1 sw do resultado r). Menos da metade!
- CAIXA [Não existem perguntas idiotas]: P: Por que não usar logo um registrador diferente para cada temporário? R: Porque um programa de verdade tem milhares de temporários e o MIPS tem 32 registradores (e vários reservados). Reaproveitar bancadas é obrigatório.
- REVELAR: Na analogia, quem é: bancada, preparação, preparações que brigam, descer para a despensa?
    → Registrador, temporário, temporários que interferem (vivos ao mesmo tempo, aresta no grafo), spill para a memória.

#### ESTILO VISUAL (Passo a passo visual): "Antes e depois, no MARS de bolso"
- PROF: Vamos ver os quatro passos da alocação em widgets: vivacidade, grafo, coloração e o MIPS final.
- WIDGET REGALLOC entrada=«a * b + c * d» legenda: Siga a coluna dos vivos de BAIXO para CIMA: é assim que a análise anda.
- TEXTO: Primeiro, o código ingênuo, do jeito que o gerador da aula de MIPS faz. Rode e conte os `lw` e `sw`.
- WIDGET MIPS_RUN gramática=«# r = a * b + c * d    (TAC: t1 = a * b ; t2 = c * d ; t3 = t1 + t2 ; r = t3) ⏎ # versão INGÊNUA: 11 acessos à memória (7 lw + 4 sw) no cálculo ⏎         .data ⏎ a:      .word 4 ⏎ b:      .word 2 ⏎ c:      .word 3 ⏎ d:      .word 5 ⏎ t1:     .word 0 ⏎ t2:     .word 0 ⏎ t3:     .word 0 ⏎ r:      .word 0 ⏎         .text ⏎ main: ⏎         lw   $t1, a ⏎         lw   $t2, b ⏎         mul  $t3, $t1, $t2 ⏎         sw   $t3, t1          # guarda t1 na memória... ⏎         lw   $t1, c ⏎         lw   $t2, d ⏎         mul  $t3, $t1, $t2 ⏎         sw   $t3, t2 ⏎         lw   $t1, t1          # ...e busca de volta logo depois ⏎         lw   $t2, t2 ⏎         add  $t3, $t1, $t2 ⏎         sw   $t3, t3 ⏎         lw   $t0, t3 ⏎         sw   $t0, r ⏎         li   $v0, 1           # imprime r ⏎         lw   $a0, r ⏎         syscall ⏎         li   $v0, 10 ⏎         syscall» legenda: Ingênuo: 11 acessos à memória no cálculo. Imprime 23.
- WIDGET MIPS_RUN gramática=«# r = a * b + c * d    com alocação de registradores (k = 2) ⏎ # t1 → $s1   t2 → $s0   t3 → $s0   (t2 morre quando t3 nasce: dividem a cor) ⏎ # 5 acessos à memória (4 lw + 1 sw) no cálculo ⏎         .data ⏎ a:      .word 4 ⏎ b:      .word 2 ⏎ c:      .word 3 ⏎ d:      .word 5 ⏎ r:      .word 0 ⏎         .text ⏎ main: ⏎         lw   $t8, a           # $t8 e $t9: rascunho para operandos da memória ⏎         lw   $t9, b ⏎         mul  $s1, $t8, $t9    # t1 = a * b ⏎         lw   $t8, c ⏎         lw   $t9, d ⏎         mul  $s0, $t8, $t9    # t2 = c * d ⏎         add  $s0, $s1, $s0    # t3 = t1 + t2 ⏎         sw   $s0, r           # r = t3 ⏎         li   $v0, 1           # imprime r ⏎         lw   $a0, r ⏎         syscall ⏎         li   $v0, 10 ⏎         syscall» legenda: Alocado: 5 acessos. Mesmo resultado, 23.
- PREVEJA: No código alocado, por que t2 e t3 podem ficar os dois em $s0?
    opções: Sorte | ✔Porque t2 é usado pela última vez exatamente na instrução que define t3: eles nunca estão vivos ao mesmo tempo | Porque são do mesmo tipo | Porque $s0 é especial
    explicação: t2 morre em 't3 = t1 + t2' e t3 nasce ali mesmo. Sem sobreposição, sem aresta no grafo, mesma cor.
- WIDGET REGALLOC entrada=«a + b + c + d» legenda: Cadeia: cada temporário morre quando o próximo nasce. Grafo sem arestas: 1 registrador basta!
- WIDGET REGALLOC entrada=«a * b + ( c * d + a * c )» legenda: Agora há 3 temporários vivos ao mesmo tempo. Procure o triângulo no grafo.
- PREVEJA: Com o triângulo t1—t2—t3, qual o menor número de registradores sem spill?
    opções: 1 | 2 | ✔3 | 5
    explicação: Num triângulo, cada vértice é vizinho dos outros dois: três cores diferentes são obrigatórias. χ = 3.
- WIDGET MIPS_RUN gramática=«# r = a * b + ( c * d + a * c )   com k = 2: t1 vai para a memória (spill) ⏎ # t2 → $s1   t3, t4, t5 → $s0   t1 → memória ⏎ # 9 acessos (7 lw + 2 sw); com k = 3 seriam 7; no ingênuo, 17 ⏎         .data ⏎ a:      .word 4 ⏎ b:      .word 2 ⏎ c:      .word 3 ⏎ d:      .word 5 ⏎ t1:     .word 0           # o derramado precisa de lugar na memória ⏎ r:      .word 0 ⏎         .text ⏎ main: ⏎         lw   $t8, a ⏎         lw   $t9, b ⏎         mul  $t8, $t8, $t9    # t1 = a * b ... ⏎         sw   $t8, t1          # ... e derrama: guarda na memória ⏎         lw   $t8, c ⏎         lw   $t9, d ⏎         mul  $s1, $t8, $t9    # t2 = c * d ⏎         lw   $t8, a ⏎         lw   $t9, c ⏎         mul  $s0, $t8, $t9    # t3 = a * c ⏎         add  $s0, $s1, $s0    # t4 = t2 + t3 ⏎         lw   $t8, t1          # recarrega o derramado na hora do uso ⏎         add  $s0, $t8, $s0    # t5 = t1 + t4 ⏎         sw   $s0, r ⏎         li   $v0, 1           # imprime r (35) ⏎         lw   $a0, r ⏎         syscall ⏎         li   $v0, 10 ⏎         syscall» legenda: E se só houver 2 registradores? t1 é derramado: sw depois de calculado, lw na hora do uso. 9 acessos (com 3 registradores seriam 7; o ingênuo faz 17). Imprime 35.
- CAIXA [Poder do cérebro]: Repare que o derramado escolhido foi t1, o que vive MAIS tempo e tem mais vizinhos. Por que essa é uma boa escolha para mandar para a despensa?
- REVELAR: Resposta
    → Tirar t1 do grafo remove 3 arestas de uma vez e quebra o triângulo; o resto passa a caber em 2 cores. E, como t1 é usado só uma vez depois de definido, o custo é um único sw + um único lw.

#### ESTILO WORKED (Exemplo resolvido): "Vivacidade, grafo e cores, à mão"
- PROF: Exercício completo: r = a * b + ( c * d + a * c ). Primeiro o TAC, depois a vivacidade de trás para frente, depois o grafo e a coloração.
- CÓDIGO (Vivacidade de baixo para cima e o grafo de interferência):
```
r = a * b + ( c * d + a * c )        (só os temporários disputam registradores)

 i  instrução          use       def   out (vivos depois)   in = use ∪ (out − def)
 5  r  = t5            {t5}      –     { }                  {t5}
 4  t5 = t1 + t4       {t1,t4}   t5    {t5}                 {t1,t4}
 3  t4 = t2 + t3       {t2,t3}   t4    {t1,t4}              {t1,t2,t3}
 2  t3 = a * c         { }       t3    {t1,t2,t3}           {t1,t2}
 1  t2 = c * d         { }       t2    {t1,t2}              {t1}
 0  t1 = a * b         { }       t1    {t1}                 { }

Arestas (quem é definido × quem está vivo DEPOIS):
 i=1: t2—t1    i=2: t3—t1, t3—t2    i=3: t4—t1

        t2
       /  \
     t1 —— t3        t5 (sozinho)
      |
     t4

Triângulo t1-t2-t3 ⇒ precisa de 3 cores (χ = 3)
```
- PASSOS (Como a tabela foi preenchida):
    · Comece no FIM: depois da última instrução, nada está vivo (out = { }). As variáveis a..d e r ficam na memória, então só contamos temporários.
    · i=5 (r = t5): usa t5 → in = {t5}.
    · i=4 (t5 = t1 + t4): out = {t5}; tira o def t5 e põe os usos t1, t4 → in = {t1, t4}.
    · i=3 (t4 = t2 + t3): out = {t1, t4}; tira t4, põe t2, t3 → in = {t1, t2, t3}.
    · i=2 (t3 = a * c): out = {t1, t2, t3}; tira t3 → in = {t1, t2}. Aqui há 3 vivos juntos: o pico.
    · Arestas: em cada instrução, o temporário DEFINIDO liga-se a todos os outros do out. Isso dá t2—t1, t3—t1, t3—t2, t4—t1.
- PASSOS (Chaitin com k = 3 (simplificar, empilhar, colorir)):
    · Graus: t1 = 3, t2 = 2, t3 = 2, t4 = 1, t5 = 0. Só tira quem tem grau < 3.
    · Tira t2 (grau 2) → empilha. Agora t1 tem grau 2: tira t1. Depois t3, t4, t5 (graus 0).
    · Pilha (topo primeiro): t5, t4, t3, t1, t2.
    · Desempilha e dá a menor cor livre: t5 → 0, t4 → 0, t3 → 0, t1 → 1 (vizinhos t3, t4 têm 0), t2 → 2 (vizinhos t1 = 1, t3 = 0).
    · Resultado: $s0 = {t3, t4, t5}, $s1 = t1, $s2 = t2. Nenhum spill.
- WIDGET REGALLOC entrada=«a * b + ( c * d + a * c )» legenda: Confira os vivos, o grafo, χ = 3 e o MIPS. Acessos à memória: 17 no ingênuo, 7 alocado.
- CAIXA [Cuidado!]: Erro clássico: fazer a vivacidade de CIMA para BAIXO. Não dá: para saber se t1 está vivo agora, você precisa saber se ele será usado DEPOIS. A informação vem do futuro, então a análise anda para trás.
- TEXTO: Agora é com você (exemplo esmaecido): **r = ( a + b ) * ( c - d )**. TAC: t1 = a + b; t2 = c - d; t3 = t1 * t2; r = t3.
- REVELAR: Calcule out e in de cada instrução, de baixo para cima.
    → i=3 r = t3: out { }, in {t3}. i=2 t3 = t1 * t2: out {t3}, in {t1, t2}. i=1 t2 = c - d: out {t1, t2}, in {t1}. i=0 t1 = a + b: out {t1}, in { }.
- REVELAR: Quais as arestas e quantos registradores são necessários?
    → Só t2—t1 (em i=1, t2 é definido com t1 vivo). t3 fica sozinho. χ = 2: por exemplo, t1 → $s1, t2 e t3 → $s0.
- REVELAR: Quantos lw + sw antes e depois?
    → Ingênuo: 11. Alocado com 2 registradores: 5 (lw de a, b, c, d e sw de r).
- WIDGET REGALLOC entrada=«( a + b ) * ( c - d )» legenda: Confira suas respostas.
- CAIXA [Dica de prova]: Na prova, monte SEMPRE a tabela com colunas use | def | out | in, preenchendo de baixo para cima. Depois desenhe o grafo e só então colora. A tabela bem feita já vale boa parte da questão.

#### ESTILO SOCRATIC (Perguntas guiadas): "Quando dois valores podem dividir um registrador?"
- PROF: Hoje você vai reinventar a alocação por coloração. Primeira pergunta, a mais básica de todas.
- PREVEJA: Por que o compilador se esforça para manter valores em registradores?
    opções: Porque a memória pode apagar dados | ✔Porque acessar a memória (lw/sw) é muito mais lento que usar um registrador | Porque o MIPS proíbe lw | Para economizar espaço na .data
    explicação: Operação entre registradores leva um ciclo; uma ida à memória pode custar dezenas ou centenas de ciclos se o dado não estiver na cache. Cortar lw/sw é cortar tempo.
- BYTE: Mas registrador é pouco. Então dois valores vão ter que dividir um?
- PREVEJA: Quando dois temporários x e y podem usar o MESMO registrador?
    opções: Quando têm nomes parecidos | ✔Quando nunca estão vivos ao mesmo tempo | Quando são usados na mesma instrução | Nunca
    explicação: Se y só passa a existir depois do último uso de x, x não precisa mais do registrador. É a ideia toda.
- PREVEJA: O que quer dizer 'x está vivo no ponto P'?
    opções: x já foi definido antes de P | ✔Existe um caminho de P até um USO de x sem passar por uma nova definição de x | x está num registrador | x vai ser impresso
    explicação: Vivo = 'o valor atual ainda vai ser lido'. Se x vai ser redefinido antes de ser lido, o valor atual está morto: pode descartar.
- REVELAR: Dessa definição sai a fórmula in = use ∪ (out − def). Explique cada pedaço.
    → Antes da instrução, está vivo o que ela USA (use) e o que estava vivo depois dela (out), menos o que ela própria redefine (def), porque esse valor antigo nunca seria lido.
- PREVEJA: Se a fórmula calcula 'in' a partir de 'out', em que direção percorremos o código?
    opções: De cima para baixo | ✔De baixo para cima | Tanto faz
    explicação: Para calcular o in da instrução i, preciso do out, que é o in da i + 1. Então começo no fim, com out = { }.
- PROF: Agora monte um grafo: um nó por temporário e uma aresta entre quem está vivo junto. Dar registradores é dar cores sem que vizinhos repitam.
- WIDGET REGALLOC entrada=«( a + b ) * ( c - d )» legenda: Duas cores bastam. Repare qual temporário ficou sem vizinhos.
- REVELAR: E se o grafo precisar de mais cores do que há registradores?
    → Algum temporário é derramado (spill): mora na memória, com sw depois de calculado e lw antes de cada uso. Escolhe-se, de preferência, um com muitos vizinhos e poucos usos.
- REVELAR: Por que $t e $s existem separados no MIPS?
    → Por causa das chamadas de função. Quem chama não sabe quais registradores a função vai usar. A convenção divide a responsabilidade: $t0–$t9 podem ser destruídos pela função (se o chamador precisa deles, ele salva); $s0–$s7 devem ser devolvidos intactos (se a função usa, ela salva e restaura).
- CAIXA [Conversa ao pé da lareira]: t1: 'Eu cheguei primeiro nesse registrador!' t3: 'Mas você já morreu, ninguém vai te ler de novo.' t1: '...' Alocador: 'Próximo! $s0 está livre.'

#### ESTILO FORMAL (Definição de prova): "Alocação por coloração: definição de prova"
- TEXTO: Para cada instrução i: **use[i]** = variáveis lidas por i; **def[i]** = variáveis escritas por i. Uma variável x está **viva** num ponto P se existe um caminho de P até um uso de x que não passa por definição de x.
- CÓDIGO (Equações de vivacidade):
```
Vivacidade (análise de fluxo de dados PARA TRÁS):
    out[i] = ∪ in[s]   para cada sucessor s de i     (no fim: out = ∅)
    in[i]  = use[i] ∪ (out[i] − def[i])
Num bloco básico (sem desvios), o único sucessor de i é i + 1:
    percorra de i = n−1 até 0, com out[n−1] = ∅.
Com laços: repita até nenhum in/out mudar (ponto fixo).
```
- TEXTO: **Intervalo de vida** de x: os pontos do programa em que x está viva. **Grafo de interferência** G = (V, E): V = temporários; {x, y} ∈ E se x é definido numa instrução em que y ∈ out (y ≠ x). (Refinamento clássico: numa cópia x = y, não se liga x a y, para permitir que dividam o registrador.)
- TEXTO: **Alocação com k registradores** = **k-coloração** de G. Decidir se G é k-colorível é NP-completo para k ≥ 3, por isso usamos heurísticas.
- CÓDIGO (Simplificar, empilhar, colorir):
```
Chaitin(G, k):
    pilha ← vazia
    enquanto G não vazio:
        se existe n com grau(n) < k:          // SIMPLIFICAR
            remova n de G; empilhe n
        senão:                                // candidato a SPILL
            escolha n (ex.: maior grau, menor custo); remova; empilhe
    enquanto pilha não vazia:                 // SELECIONAR
        n ← desempilha
        se sobra cor não usada pelos vizinhos já coloridos: dê a menor
        senão: n é DERRAMADO (sw após def, lw antes de cada uso)
```
- TEXTO: **Por que funciona**: se grau(n) < k, então, por pior que seja a coloração dos vizinhos, sobra uma cor para n. Remover n não atrapalha; colorimos na volta. Spill só acontece quando todo nó restante tem grau ≥ k.
- PREVEJA: Um grafo tem um nó n de grau 5 e k = 4. O que é VERDADE?
    opções: n certamente será derramado | ✔n não pode ser simplificado agora, mas pode acabar colorido se dois vizinhos receberem a mesma cor | O grafo não é 4-colorível
    explicação: Grau ≥ k só torna n um CANDIDATO. Na seleção, se os vizinhos repetirem cores, sobra cor para n. Por isso o spill de Chaitin é 'otimista' na versão de Briggs.
- WIDGET MIPS_RUN gramática=«# Convenção: $s0..$s7 são SALVOS PELO CHAMADO; $t0..$t9 não (o chamador que se cuide) ⏎         .data ⏎ x:      .word 0 ⏎         .text ⏎ main: ⏎         li   $s0, 7           # main guarda um valor importante em $s0 ⏎         li   $a0, 5 ⏎         jal  dobro            # chama dobro(5) ⏎         add  $s0, $s0, $v0    # 7 + 10: $s0 ainda vale 7! ⏎         sw   $s0, x ⏎         li   $v0, 1 ⏎         move $a0, $s0         # imprime 17 ⏎         syscall ⏎         li   $v0, 10 ⏎         syscall ⏎ dobro: ⏎         addi $sp, $sp, -4 ⏎         sw   $s0, 0($sp)      # vai usar $s0? então SALVA antes... ⏎         add  $s0, $a0, $a0 ⏎         move $v0, $s0 ⏎         lw   $s0, 0($sp)      # ... e RESTAURA antes de voltar ⏎         addi $sp, $sp, 4 ⏎         jr   $ra» legenda: Convenção do MIPS: $s0 é salvo pelo CHAMADO. dobro usa $s0, mas salva e restaura; main encontra o 7 intacto e imprime 17.
- TEXTO: **Convenção MIPS**: $t0–$t9 são **salvos pelo chamador** (caller-saved): uma função pode sobrescrevê-los à vontade. $s0–$s7 são **salvos pelo chamado** (callee-saved): quem os usa salva na pilha e restaura antes do `jr $ra`. Regra prática: valor que atravessa uma chamada → $s; valor de vida curta → $t.
- CAIXA [Dica de prova]: Roteiro de prova: (1) TAC; (2) tabela use/def/out/in DE BAIXO PARA CIMA; (3) arestas: def × out; (4) Chaitin com a pilha escrita; (5) mapa temporário → registrador; (6) se pedir, o MIPS e a contagem de lw/sw antes × depois.
- REVELAR: Escreva a equação de vivacidade e a regra de interferência.
    → in[i] = use[i] ∪ (out[i] − def[i]); out[i] = ∪ in dos sucessores. x e y interferem se x é definido numa instrução em que y está vivo na saída (y ∈ out[i]).

#### ESTILO MNEMONIC (Truque de memória): "Pinte o mapa da cozinha"
- CAIXA [Gancho de memória]: **VIGOR**: **VI**vacidade → **G**rafo → c**O**lorir → **R**egistradores. (E se faltar cor? Spill: 'derrama no chão'.)
- TEXTO: **Palácio da memória**: na Forja do Código, ao lado da Montadora MIPS, fica o **Ateliê de Coloração** . Na parede, um mapa com bolinhas (os temporários). O pintor tem só k latas de tinta (registradores). Bolinhas ligadas por fio não podem ter a mesma cor. A que sobrar sem tinta vai para o porão (a memória).
- CAIXA [Gancho de memória]: **Vivacidade se faz como desfazer a mala: de trás para frente.** O último item que entrou é o primeiro que sai. Comece no fim do código com out = { }.
- CAIXA [Gancho de memória]: **in = use ∪ (out − def)** se lê: 'está vivo antes quem eu USO agora, mais quem estava vivo depois e eu NÃO matei'. Def mata, use ressuscita.
- CAIXA [Gancho de memória]: **Interferência = 'nasceu enquanto o outro estava vivo'.** Quem é definido ganha aresta com todo mundo do out daquela instrução.
- PREVEJA: Na fase de simplificação com k = 3, qual nó pode ser removido com segurança?
    opções: Qualquer um | ✔Um com grau 2 (menor que k) | Só o de maior grau | Um com grau 3
    explicação: 'Menos vizinhos que latas, sempre sobra lata.' Grau < k garante cor na volta.
- CAIXA [Gancho de memória]: **$t = tchau** (a função pode estragar: se precisar, o CHAMADOR salva). **$s = sagrado** (a função devolve como achou: o CHAMADO salva).
- CAIXA [Cuidado!]: Contrastes: **vivo** ≠ 'já definido' (vivo = ainda vai ser LIDO). **χ (número cromático)** = menor k sem spill; não é o número de temporários. **Spill** não é erro: é custo (mais lw/sw).
- WIDGET REGALLOC entrada=«a * b + ( c * d + a * c )» legenda: Recite VIGOR enquanto olha: vivos, grafo (triângulo!), 3 cores, MIPS com $s0, $s1, $s2.
- REVELAR: Os números para guardar: quantos lw + sw em a * b + c * d, antes e depois?
    → 11 no ingênuo, 5 com alocação (2 registradores). Em a * b + ( c * d + a * c ): 17 → 7 com 3 registradores, ou 9 com 2 (t1 derramado).
- REVELAR: Sem olhar: os 4 passos, a fórmula e a convenção.
    → VIGOR: vivacidade (de trás para frente), grafo de interferência, colorir com k cores (Chaitin: simplifica grau < k, empilha, colore), registradores. in = use ∪ (out − def). $t: salvos pelo chamador; $s: salvos pelo chamado.
