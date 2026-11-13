# Região 8: Cidade Semântica — aulas de texto "Ler com calma" (6 estilos por assunto)


## ASSUNTO ATTRIBUTES_TYPES: Atributos e verificação de tipos

#### ESTILO STORY (História / Analogia): "Notas que sobem, sobrenomes que descem"
- NARRADOR: Cidade Semântica. A árvore sintática já está pronta... mas ela ainda não sabe o que significa.
- BYTE: Como assim? A árvore de 'a + x * 2' tá certinha!
- PROF: Ela está certa na FORMA. Agora precisamos pendurar informação em cada nó: tipo, valor, código... Esses penduricalhos são os **atributos**.
- TEXTO: Pense na sua média do semestre: a nota final é calculada a partir das notas das provas, que vêm dos exercícios. A informação **sobe**. Isso é um atributo **sintetizado**.
- TEXTO: Agora pense no sobrenome: você o recebe dos pais e repassa aos irmãos mais novos. A informação **desce** (ou vem do lado). Isso é um atributo **herdado**.
- PASSOS (O mapa da analogia):
    1) Nota final calculada das parciais → **sintetizado** (do filho para o pai)
    2) Sobrenome vindo dos pais → **herdado** (do pai ou irmão para o filho)
    3) Regulamento de como calcular a média → **regra semântica** (E.tipo = ...)
    4) Gramática + atributos + regras → **definição dirigida pela sintaxe (SDD)**
- TEXTO: O fiscal da balança da feira é o **verificador de tipos**: não deixa somar banana com bool. Mas int com float pode, é só converter o int, como trocar reais por dólares antes de somar. Isso é **coerção**.
- PREVEJA: a é int e x é float. Qual o tipo de a + x * 2?
    opções: int | ✔float, com coerção de int para float | erro de tipo
    explicação: x * 2 é float (2 é convertido). a + float também vira float: o compilador converte a com inttofloat.
- WIDGET ATTR_TREE entrada=«a + x * 2» legenda: Os tipos sobem das folhas até a raiz: int + float → float.
- REVELAR: Em 'int a, b, c;', o tipo int é sintetizado ou herdado para cada variável?
    → Herdado: ele desce do nó do tipo para a lista de nomes, como um sobrenome passado aos irmãos.
- CAIXA [Não existem perguntas idiotas]: 'Atributo é só uma variável no nó?' Basicamente, sim! O que importa é DE ONDE vem o valor dela: dos filhos (sintetizado) ou do pai/irmãos (herdado).

#### ESTILO VISUAL (Passo a passo visual): "Assistindo os tipos subirem pela árvore"
- PROF: Vamos ver o verificador de tipos trabalhando nó por nó, em pós-ordem: filhos primeiro, pai depois.
- WIDGET ATTR_TREE entrada=«(a + b) * c» legenda: Tudo int: as folhas consultam a tabela de símbolos, os operadores combinam. Valor 18 com a=4, b=2, c=3.
- WIDGET ATTR_TREE entrada=«a + x * 2» legenda: Aqui aparece a coerção: no '+', o int encontra um float e é alargado.
- BYTE: Então o tipo do pai é só 'o maior' dos tipos dos filhos?
- PROF: Nas aritméticas, sim: int < float, e o resultado é o maior. Para comparações e lógica, as regras são outras.
- WIDGET ATTR_TREE entrada=«a < b && ok» legenda: Comparação sempre produz bool; && exige dois bool.
- PREVEJA: E ok + 1, com ok sendo bool?
    opções: int (true vale 1) | ✔erro de tipo | bool
    explicação: Nesta linguagem (como em Java), não dá para fazer aritmética com bool. E o erro se propaga para cima.
- WIDGET ATTR_TREE entrada=«ok + 1» legenda: O nó '+' marca erro: bool não entra em aritmética.
- WIDGET PARSE_TREE gramática=«D -> T L ; ⏎ T -> int | float ⏎ L -> id R ⏎ R -> , id R | ε» entrada=«int id , id ;» legenda: Atributo herdado: T.tipo = int é passado para L.in e desce para cada id da lista.
- PASSOS (O que você viu):
    1) Folhas: o tipo vem do literal ou da tabela de símbolos
    2) Nós internos: tipo calculado dos filhos → sintetizado
    3) Ordem de avaliação: pós-ordem (como o $$ do Bison, numa redução)
    4) Na declaração, o tipo desce pela lista → herdado
- CAIXA [Poder do cérebro]: Um analisador LR calcula atributos sintetizados de graça: em cada redução, os valores dos filhos estão na pilha. É exatamente o $$ = $1 + $3 do Bison.

#### ESTILO WORKED (Exemplo resolvido): "Árvore anotada com valor e com tipo"
- PROF: Primeiro, valores. A SDD clássica da calculadora:
- CÓDIGO (SDD S-atribuída (só atributos sintetizados)):
```
E → E1 + T    E.val = E1.val + T.val
E → T         E.val = T.val
T → T1 * F    T.val = T1.val * F.val
T → F         T.val = F.val
F → ( E )     F.val = E.val
F → num       F.val = num.lexval
```
- PASSOS (Anotando 3 * 5 + 4 de baixo para cima):
    1) Folhas: num.lexval = 3, 5, 4 → F.val = 3, 5, 4
    2) T → F: T.val = 3
    3) T → T1 * F: T.val = 3 * 5 = 15
    4) E → T: E.val = 15
    5) E → E1 + T (com T.val = 4): E.val = 15 + 4 = **19**
- PROF: Agora, tipos, com coerção. Regra: se algum operando é float, o resultado é float e o int é convertido.
- PASSOS (Tipando a + x * 2 (a: int, x: float)):
    1) a → int (tabela de símbolos); x → float; 2 → int (literal)
    2) x * 2: float * int → **float**, converte 2 para 2.0
    3) a + (x * 2): int + float → **float**, gera inttofloat(a)
- WIDGET ATTR_TREE entrada=«a + x * 2» legenda: Confira: raiz float, com a coerção marcada.
- BYTE: Deixa comigo agora.
- REVELAR: Tipo de (a + b) * c, com a, b, c int?
    → int em todos os nós internos; nenhuma coerção.
- REVELAR: Tipo de x * 2 + 3 * 4?
    → x * 2 é float; 3 * 4 é int; float + int → float (o 12 é convertido).
- REVELAR: Tipo de a < b && ok?
    → a < b → bool; bool && bool (ok) → bool.
- WIDGET ATTR_TREE entrada=«x * 2 + 3 * 4» legenda: Confira a segunda resposta.
- CAIXA [Dica de prova]: Em 'desenhe a árvore anotada', escreva o atributo ao lado de CADA nó (até os T → F chatos). O professor quer ver a ordem de avaliação, não só o resultado.

#### ESTILO SOCRATIC (Perguntas guiadas): "De onde vem a informação de cada nó?"
- PROF: Pergunta-guia de hoje: para calcular este atributo, eu preciso olhar PARA BAIXO ou PARA CIMA?
- REVELAR: O valor de E em E → E1 + T depende de quê?
    → De E1.val e T.val: dos filhos. É sintetizado.
- REVELAR: Se TODOS os atributos são sintetizados, em que ordem dá para avaliar?
    → Pós-ordem (de baixo para cima). Uma SDD assim é **S-atribuída** e combina perfeitamente com um analisador LR: avalia na hora da redução.
- REVELAR: E em D → T L, com 'int a, b, c', como cada id descobre seu tipo?
    → Pelo atributo herdado L.in = T.tipo, que desce pela lista. O id está abaixo e à direita de quem sabe o tipo.
- BYTE: Então herdado pode vir de qualquer lugar?
- REVELAR: Se um atributo herdado pudesse depender de um irmão à DIREITA, um analisador que lê da esquerda para a direita conseguiria calcular na hora?
    → Não: o irmão da direita ainda não foi analisado. Daí a restrição L-atribuída.
- PREVEJA: Numa SDD **L-atribuída**, o atributo herdado de Xi em A → X1 X2 ... Xn pode depender de...
    opções: Qualquer atributo de qualquer filho | ✔Atributos herdados de A e atributos de X1 ... Xi-1 (irmãos à esquerda) | Apenas atributos sintetizados de A
    explicação: L de Left: só o pai (herdados dele) e os irmãos à esquerda. Assim uma passada em profundidade, da esquerda para a direita, resolve tudo, perfeito para descida recursiva.
- REVELAR: Toda SDD S-atribuída é L-atribuída?
    → Sim! Ela não tem herdados, então a restrição é satisfeita trivialmente.
- REVELAR: Por que o verificador de tipos precisa da tabela de símbolos?
    → Porque nas folhas só há nomes. O tipo de 'a' foi decidido lá na declaração; a tabela é a memória que liga uso e declaração.
- WIDGET ATTR_TREE entrada=«a + b * c» legenda: Pós-ordem em ação: b * c primeiro, depois a soma.
- CAIXA [Poder do cérebro]: Se o grafo de dependências entre atributos tiver um ciclo, não existe ordem de avaliação. S-atribuída e L-atribuída são duas classes que garantem: sem ciclos.

#### ESTILO FORMAL (Definição de prova): "SDD, atributos e sistema de tipos"
- TEXTO: **Definição dirigida pela sintaxe (SDD):** gramática livre de contexto + atributos associados aos símbolos + regras semânticas associadas às produções.
- PASSOS (Tipos de atributo (para um não-terminal A num nó N)):
    1) **Sintetizado:** definido por regra da produção em que A é a CABEÇA, usando atributos dos filhos de N e do próprio N
    2) **Herdado:** definido por regra da produção em que A está no CORPO (no pai de N), usando atributos do pai, dos irmãos e do próprio N
    3) Terminais só têm atributos sintetizados, fornecidos pelo léxico (ex.: num.lexval)
- PASSOS (Classes de SDD):
    1) **S-atribuída:** só atributos sintetizados → avaliação em pós-ordem; implementável durante a análise LR
    2) **L-atribuída:** em A → X1...Xn, cada herdado de Xi depende só de herdados de A e de atributos de X1...Xi-1 (e de Xi, sem ciclo) → uma passada em profundidade, esquerda para direita
    3) **Esquema de tradução (SDT):** gramática com fragmentos de programa {ações} embutidos no corpo; em SDD S-atribuída, as ações vão no fim (SDT pós-fixo)
- CÓDIGO (SDD L-atribuída clássica: declarações (in é herdado)):
```
D → T L ;        L.in = T.tipo
T → int          T.tipo = int
T → float        T.tipo = float
L → L1 , id      L1.in = L.in ;  addType(id.entrada, L.in)
L → id           addType(id.entrada, L.in)
```
- CÓDIGO (Verificação de tipos com coerção (alargamento)):
```
E → E1 op E2   (op aritmético):
  se E1.tipo = bool ou E2.tipo = bool: erro de tipo
  E.tipo := max(E1.tipo, E2.tipo)      // int < float
  E.addr1 := widen(E1.addr, E1.tipo, E.tipo)   // insere inttofloat se preciso
  E.addr2 := widen(E2.addr, E2.tipo, E.tipo)
E → E1 relop E2:  numéricos → E.tipo := bool
E → E1 && E2:     ambos bool → E.tipo := bool
```
- PREVEJA: Uma coerção int → float inserida pelo compilador é chamada de...
    opções: Conversão explícita (cast) | ✔Coerção implícita (alargamento) | Estreitamento
    explicação: Coerção = conversão implícita feita pelo compilador. int → float é alargamento (não perde a ordem de grandeza). float → int seria estreitamento e costuma exigir cast explícito.
- WIDGET ATTR_TREE entrada=«a + x * 2» legenda: Instância da regra acima: max(int, float) = float, com widen em a.
- CAIXA [Dica de prova]: Ao classificar uma SDD, verifique CADA regra que define um herdado: ela usa algum irmão à direita? Se sim, não é L-atribuída. Sem herdados → S-atribuída (e também L).
- CAIXA [Cuidado!]: Verificação de tipos é estática (em tempo de compilação). Divisão por zero com valores de variáveis só aparece em tempo de execução, não é erro de tipo.

#### ESTILO MNEMONIC (Truque de memória): "S sobe, H desce"
- NARRADOR: Palácio da Memória: Cidade Semântica, prédio dos Tipos. Um elevador só sobe (sintetizado); a escada só desce (herdado).
- CAIXA [Gancho de memória]: **S↑ H↓**: **S**intetizado **S**obe (dos filhos para o pai); **H**erdado desce (do pai/irmãos para o filho).
- CAIXA [Gancho de memória]: **S-atribuída = Só Sintetizado.** **L-atribuída = Left**: herda só do pai e dos irmãos da esquerda.
- CAIXA [Gancho de memória]: Coerção é **copo pequeno no copo grande**: int cabe em float (alargamento). O contrário derrama (estreitamento).
- CÓDIGO (Tabela de bolso dos tipos (⊕ = + - * /)):
```
int   ⊕ int    → int
int   ⊕ float  → float   (inttofloat no int)
float ⊕ float  → float
num  relop num → bool
bool  &&  bool → bool
bool  ⊕  num   → ERRO   (e o erro sobe)
```
- BYTE: Tipo é que nem fofoca: começa na folha e todo mundo acima fica sabendo!
- PREVEJA: Bison: $$ = $1 + $3; calcula um atributo...
    opções: Herdado | ✔Sintetizado | Global
    explicação: $$ é o pai, calculado dos filhos $1 e $3: sobe. O Bison é uma máquina de SDD S-atribuída.
- WIDGET ATTR_TREE entrada=«ok + 1» legenda: O erro também sobe: um bool numa soma contamina a raiz.
- REVELAR: Diga em uma frase: sintetizado vs herdado.
    → Sintetizado: calculado a partir dos filhos (sobe). Herdado: calculado a partir do pai e dos irmãos (desce/vem do lado).
- CAIXA [Conversa ao pé da lareira]: Sintetizado: 'Eu sou prático: espero meus filhos terminarem e resumo tudo.' Herdado: 'E eu levo o contexto que você não vê lá de baixo, tipo o int de int a, b, c.'


## ASSUNTO SCOPE_SYMBOLS: Escopo e tabela de símbolos

#### ESTILO STORY (História / Analogia): "O porteiro do condomínio"
- NARRADOR: Cidade Semântica, bairro dos Escopos. Prédios dentro de condomínios, apartamentos dentro de prédios.
- BYTE: Tem dois 'x' no meu programa. Como o compilador sabe de qual eu estou falando?
- PROF: Do mesmo jeito que o porteiro acha o 'João' certo. Imagine: alguém chega no seu apartamento e pergunta pelo João.
- TEXTO: O porteiro procura primeiro **no seu apartamento**. Não achou? Procura **no prédio**. Depois **no condomínio inteiro**. O primeiro João encontrado, de dentro para fora, é o João certo.
- TEXTO: Se tem um João no seu apê e outro no condomínio, o do apê **esconde** o de fora enquanto você está lá dentro. Isso é **sombreamento** (shadowing).
- PASSOS (O mapa da analogia):
    1) Apartamento, prédio, condomínio → **blocos aninhados** { { { } } }
    2) Lista de moradores de cada lugar → **uma tabela de símbolos por escopo**
    3) Procurar de dentro para fora → **regra do aninhamento mais próximo** (escopo estático)
    4) Mudou-se do apê → **sair do bloco**: a lista dele é descartada
    5) Nenhum João em lugar nenhum → erro **'variável não declarada'**
- PILHA: As listas... ficam empilhadas. Entrou num bloco... empilha. Saiu... desempilha. Eu adoro... esse trabalho.
- CÓDIGO (Um condomínio de código):
```
{                        // bloco 0
  int x;  int y;         // (1) x    (2) y
  x = 1;
  {                      // bloco 1
    float x;             // (3) x
    x = y + 1;
    {                    // bloco 2
      int y;             // (4) y
      print(x + y);
    }
    print(y);
  }
  print(x);
}
```
- PREVEJA: Na linha print(x + y), dentro do bloco 2, quais declarações são usadas?
    opções: x (1) e y (2) | ✔x (3) e y (4) | x (3) e y (2)
    explicação: De dentro para fora: y está no próprio bloco 2 (4); x não está no bloco 2, mas está no bloco 1 (3). O x (1) fica escondido.
- WIDGET SCOPE legenda: Ligue cada uso à sua declaração, como o porteiro faria.
- CAIXA [Não existem perguntas idiotas]: 'E o léxico não resolve isso?' Não: a Lexi só sabe que 'x' é um ID. Quem decide QUAL x é a análise semântica, com a tabela de símbolos.

#### ESTILO VISUAL (Passo a passo visual): "A pilha de tabelas, quadro a quadro"
- PROF: Vamos assistir a pilha de tabelas enquanto o compilador lê o programa de cima para baixo.
- CÓDIGO (O programa):
```
{                        // bloco 0
  int x;  int y;         // (1) x    (2) y
  x = 1;
  {                      // bloco 1
    float x;             // (3) x
    x = y + 1;
    {                    // bloco 2
      int y;             // (4) y
      print(x + y);
    }
    print(y);
  }
  print(x);
}
```
- PASSOS (A pilha em cada momento (topo à direita)):
    1) Abre bloco 0 → [ {} ]
    2) int x; int y; → [ {x:int(1), y:int(2)} ]
    3) Abre bloco 1 → [ {x(1), y(2)} | {} ]
    4) float x; → [ {x(1), y(2)} | {x:float(3)} ] — o x(3) sombreia o x(1)
    5) x = y + 1 → x acha (3) no topo; y não está no topo, desce e acha (2)
    6) Abre bloco 2, int y; → [ ... | {x(3)} | {y:int(4)} ]
    7) print(x + y) → x: (3), y: (4)
    8) Fecha bloco 2 → a tabela {y(4)} é descartada
    9) print(y) → y: (2)   •   Fecha bloco 1   •   print(x) → x: (1)
- WIDGET SCOPE legenda: Agora com outro programa: para cada uso, qual declaração vence?
- LEXI: Para mim, todos esses x são iguais. Olha só:
- WIDGET TOKENIZE entrada=«int x = 1; { float x = 2.5; print(x); }» legenda: O léxico vê ID, ID, ID... sem saber a qual declaração cada um pertence.
- PREVEJA: Depois de fechar o bloco 1, alguém ainda consegue achar o x (3)?
    opções: Sim, ele fica na tabela global | ✔Não, a tabela do bloco 1 foi desempilhada | Só se for float
    explicação: Saiu do bloco, a tabela daquele escopo sai do topo. O próximo print(x) só enxerga o x (1).
- WIDGET ATTR_TREE entrada=«a + z» legenda: E se o nome não está em tabela nenhuma? A busca falha: 'z' não foi declarada, e o erro sobe pela árvore.
- CAIXA [Poder do cérebro]: Repare: a busca percorre a pilha do TOPO para a BASE. Isso é toda a regra de escopo estático, em uma frase.

#### ESTILO WORKED (Exemplo resolvido): "Ligando cada uso à sua declaração"
- PROF: Exercício típico: para cada uso de variável, diga qual declaração ele referencia.
- CÓDIGO (Programa (declarações numeradas de 1 a 4)):
```
{                        // bloco 0
  int x;  int y;         // (1) x    (2) y
  x = 1;
  {                      // bloco 1
    float x;             // (3) x
    x = y + 1;
    {                    // bloco 2
      int y;             // (4) y
      print(x + y);
    }
    print(y);
  }
  print(x);
}
```
- PASSOS (Resolvendo os usos, de dentro para fora):
    1) x = 1 (bloco 0): x está no bloco 0 → **(1)**
    2) x = y + 1 (bloco 1): x está no bloco 1 → **(3)**; y não está no bloco 1, sobe para o 0 → **(2)**
    3) print(x + y) (bloco 2): x não está no 2, está no 1 → **(3)**; y está no 2 → **(4)**
- BYTE: Peraí, deixa os últimos comigo.
- REVELAR: print(y), depois que o bloco 2 fechou.
    → (2). A tabela do bloco 2, onde estava o y (4), já foi desempilhada; no bloco 1 não há y; no bloco 0 há o (2).
- REVELAR: print(x), na última linha.
    → (1). Estamos de volta ao bloco 0; o x float (3) morreu com o bloco 1.
- REVELAR: Se dentro do bloco 2 houvesse 'int y;' duas vezes, o que aconteceria?
    → Erro de redeclaração: o mesmo nome declarado duas vezes no MESMO escopo. Sombrear é declarar num escopo DIFERENTE.
- REVELAR: Qual o tipo da expressão x + y em print(x + y)?
    → x (3) é float e y (4) é int → float + int = float. A tabela de símbolos alimenta a verificação de tipos!
- WIDGET SCOPE legenda: Pratique com um programa sorteado.
- CAIXA [Dica de prova]: Na prova, desenhe a pilha de tabelas ao lado do código e atualize a cada { e }. É mais rápido e evita confundir quem já 'morreu'.

#### ESTILO SOCRATIC (Perguntas guiadas): "Uma tabela só não bastaria?"
- PROF: Vamos desenhar a tabela de símbolos juntos, com perguntas.
- REVELAR: Que informação guardar para cada nome?
    → Pelo menos: nome, tipo, categoria (variável, função, parâmetro...) e, mais tarde, endereço/deslocamento na memória. Às vezes a linha da declaração, para mensagens de erro.
- REVELAR: Por que um único mapa global nome → informação não funciona?
    → Porque o mesmo nome pode ter declarações diferentes em blocos diferentes (x int e x float). E ao sair de um bloco, os nomes dele precisam sumir.
- REVELAR: Então, o que fazer ao entrar num bloco? E ao sair?
    → Entrar: criar uma tabela nova, ligada à anterior. Sair: voltar para a anterior, descartando a nova. Uma pilha de tabelas!
- BYTE: E pra procurar um nome, eu começo por onde?
- REVELAR: Por onde começa a busca, e quando ela para?
    → Começa na tabela do topo (bloco mais interno) e vai descendo; para no primeiro achado. Se chegar ao fundo sem achar: 'não declarada'.
- PREVEJA: Declarar 'int x' quando já existe 'x' num bloco EXTERNO é...
    opções: Erro de redeclaração | ✔Permitido: o novo x sombreia o externo | Ignorado
    explicação: A verificação de redeclaração olha só a tabela do TOPO. Em blocos diferentes, o novo x sombreia o antigo (C e C++ permitem; Java, por escolha de projeto, proíbe uma local sombrear outra local, mas a tabela funciona igual).
- REVELAR: Escopo estático (léxico) vs dinâmico: qual a diferença?
    → Estático: o x certo é decidido pelo TEXTO do programa (aninhamento). Dinâmico: pela ordem de CHAMADAS em tempo de execução (a declaração mais recente ativa). C, Java e Kotlin usam estático.
- REVELAR: Em qual fase o compilador usa e preenche a tabela?
    → O léxico pode inserir os nomes; a análise sintática/semântica insere declarações e resolve usos; a geração de código usa os endereços. Ela acompanha todas as fases.
- WIDGET SCOPE legenda: Teste suas respostas.
- CAIXA [Poder do cérebro]: Se o compilador inserisse 'x' do bloco interno na MESMA tabela do externo, o que aconteceria com o x externo ao sair do bloco? (Ele teria sido sobrescrito, perdido para sempre.)

#### ESTILO FORMAL (Definição de prova): "Escopo estático e ambientes encadeados"
- TEXTO: **Escopo** de uma declaração: a região do programa em que um uso daquele nome se refere a ela. **Regra do aninhamento mais próximo:** um uso de x refere-se à declaração de x no bloco mais interno que contém o uso.
- CÓDIGO (Tabela de símbolos encadeada (Livro do Dragão, seção 2.7)):
```
classe Env:
  tabela:   Mapa<nome, Simbolo>
  anterior: Env?                      // escopo envolvente

  put(nome, s): tabela[nome] := s
  get(nome):
    e := este
    enquanto e ≠ nulo:
      se nome ∈ e.tabela: retorne e.tabela[nome]
      e := e.anterior
    retorne nulo
```
- CÓDIGO (Esquema de tradução usando a pilha de ambientes):
```
bloco  → '{'       { salvo := topo; topo := new Env(topo) }
           decls stmts
           '}'       { topo := salvo }
decl   → tipo id ;   { se id ∈ topo.tabela: erro "redeclaração"
                       senão topo.put(id, Simbolo(tipo)) }
fator  → id          { s := topo.get(id)
                       se s = nulo: erro "não declarada"
                       senão fator.tipo := s.tipo }
```
- PASSOS (Operações e custos (com tabela hash por escopo)):
    1) entrar no escopo: O(1)
    2) inserir: O(1) esperado, só no escopo do topo
    3) buscar: O(profundidade do aninhamento) no pior caso
    4) sair do escopo: O(1), descarta a tabela do topo
- PREVEJA: Escopo dinâmico: f usa x sem declarar; g declara x=2 e chama f; o global tem x=1. Com escopo DINÂMICO, f vê x =
    opções: 1 | ✔2 | erro
    explicação: Dinâmico segue a cadeia de CHAMADAS: a ativação mais recente com x é a de g. No escopo estático, f veria o global (1).
- WIDGET SCOPE legenda: Escopo estático: a resposta vem só do texto.
- CAIXA [Dica de prova]: Ao responder 'qual declaração', justifique com a regra: 'pelo aninhamento mais próximo, o uso na linha N refere-se à declaração do bloco B'. E diferencie redeclaração (mesmo escopo, erro) de sombreamento (escopo interno, permitido).
- CAIXA [Cuidado!]: Escopo é sobre ONDE um nome é visível (estático, no texto). Tempo de vida é sobre QUANDO a variável existe na memória (dinâmico, na execução). Não são a mesma coisa.

#### ESTILO MNEMONIC (Truque de memória): "A cebola dos escopos"
- NARRADOR: Palácio da Memória: Cidade Semântica, bairro dos Escopos. Cada casa é uma cebola: camadas dentro de camadas.
- CAIXA [Gancho de memória]: **Escopo é cebola: procure de dentro para fora.** A primeira camada que tiver o nome vence.
- CAIXA [Gancho de memória]: **'Abre chave, empilha; fecha chave, desempilha.'** A pilha de tabelas segue as chaves do programa.
- CAIXA [Gancho de memória]: **E-I-B-S**: as quatro operações: **E**ntrar no escopo, **I**nserir, **B**uscar, **S**air. ('É I Bem Simples.')
- CAIXA [Gancho de memória]: Sombreamento: **'quem está mais perto faz sombra em quem está longe'**. O x de fora não morreu; só está na sombra.
- BYTE: E se eu declarar o mesmo nome duas vezes na mesma camada?
- PROF: Aí não é sombra, é briga: **redeclaração**, erro. Sombra só existe entre camadas diferentes.
- PREVEJA: Busca na pilha de tabelas: de onde para onde?
    opções: Da base para o topo | ✔Do topo para a base | Só no topo
    explicação: Topo = bloco mais interno. Descer a pilha = ir para fora na cebola. (Só no topo é a regra para checar REDECLARAÇÃO, não para buscar usos.)
- WIDGET SCOPE legenda: Descasque a cebola: para cada uso, ache a camada certa.
- REVELAR: Recite a regra do escopo estático numa frase.
    → Um uso de x refere-se à declaração de x no bloco mais interno (mais próximo) que contém o uso.
- CAIXA [Conversa ao pé da lareira]: Escopo estático: 'Me lê o código e você sabe quem eu sou.' Escopo dinâmico: 'Comigo depende de quem me chamou... é mais emocionante.' Programador cansado às 3 da manhã: 'Eu fico com o estático.'


## ASSUNTO AST_BUILD: Árvore sintática abstrata (AST) com classes

#### ESTILO STORY (História / Analogia): "O resumo e o livro inteiro"
- NARRADOR: Véspera de prova de Literatura. Byte tem duas opções: reler o livro inteiro, com prefácio, índice e notas de rodapé, ou ler o resumo de duas páginas da colega.
- BYTE: O resumo, óbvio! Mas... não perde nada importante?
- PROF: Um BOM resumo não perde nada que importa para a prova: personagens, quem fez o quê, em que ordem. Ele joga fora só o que servia para o livro ser LIDO: capítulos de transição, numeração, notas. A **árvore sintática abstrata (AST)** é o resumo; a **árvore de derivação** é o livro inteiro.
- PASSOS (Livro × resumo → árvore de derivação × AST):
    1) Livro inteiro → **árvore de derivação (concreta)**: um nó para CADA passo da gramática.
    2) Capítulos de transição ('E vira T, que vira F...') → **não-terminais em corrente**: E → T → F → a. Somem na AST.
    3) Numeração e índice → **parênteses e pontuação**: só serviam para o parser saber a ordem. A forma da AST já guarda essa ordem.
    4) Páginas em branco → produções **ε**: somem.
    5) Personagens e ações → **operadores e operandos**: viram nós `BinOp`, `Var`, `Num`.
- PREVEJA: Para 'a + b * c', a árvore de derivação da gramática E/T/F tem 13 nós. Quantos tem a AST?
    opções: 13 | 9 | ✔5 | 3
    explicação: Só sobram os 3 operandos e os 2 operadores: BinOp('+') com Var(a) e BinOp('*') com Var(b) e Var(c). 13 → 5: um resumo de verdade!
- WIDGET AST entrada=«a + b * c» legenda: Lado a lado: o livro (derivação) e o resumo (AST). Repare que o * continua mais FUNDO que o +: a precedência sobreviveu.
- BYTE: Então a AST é só uma árvore mais magrinha. Por que ela é tão importante?
- PROF: Porque, depois da análise sintática, TODO o resto do compilador estuda pelo resumo: o verificador de tipos, a tabela de símbolos, o gerador de código. Ninguém quer reler o livro inteiro três vezes.
- CAIXA [No mundo real]: No trabalho prático (o compilador em Java), muitos grupos traduzem direto dentro do parser: lê um token, já emite assembly. Funciona, mas é como fazer a prova lendo o livro uma vez só, sem poder voltar. Com a AST você lê uma vez, monta o resumo e depois pode passar por ele quantas vezes quiser: uma vez para checar tipos, outra para gerar código.
- CAIXA [Não existem perguntas idiotas]: P: Se eu jogo fora os parênteses, como o compilador sabe que (a + b) * c é diferente de a + b * c? R: Pela FORMA da árvore. Em (a + b) * c, o + fica embaixo do *; em a + b * c, é o * que fica embaixo do +. Quem está mais fundo é calculado primeiro.
- REVELAR: Na analogia, quem é: livro inteiro, resumo, capítulo de transição, numeração das páginas?
    → Árvore de derivação, AST, não-terminal em corrente (E → T → F), parênteses/pontuação.

#### ESTILO VISUAL (Passo a passo visual): "Dieta de árvores, ao vivo"
- PROF: Hoje cada widget mostra a mesma expressão duas vezes: a árvore de derivação (gorda) e a AST (magra), com a contagem de nós e o código Java que monta a AST.
- WIDGET AST entrada=«( ( a ) )» legenda: O caso extremo: 14 nós na árvore de derivação, 1 nó na AST. Tudo era parêntese e corrente.
- PREVEJA: O que restou na AST de '( ( a ) )'?
    opções: BinOp com dois filhos | ✔Um único Var(a) | Dois nós de parênteses | Nada: a AST fica vazia
    explicação: Os parênteses só agrupavam; não há operação nenhuma. A AST é apenas new Var("a").
- WIDGET AST entrada=«a - b - c» legenda: Associatividade: 14 nós contra 5. Veja qual '-' ficou embaixo.
- PREVEJA: Na AST de 'a - b - c', qual subtração fica mais funda (é feita primeiro)?
    opções: ✔A da esquerda: (a - b) - c | A da direita: a - (b - c) | Tanto faz: dá o mesmo valor
    explicação: A gramática E → E - T é recursiva à ESQUERDA, então a árvore cresce para a esquerda: new BinOp('-', new BinOp('-', a, b), c). E não dá o mesmo valor: com a=10, b=3, c=2, (10-3)-2 = 5 mas 10-(3-2) = 9.
- WIDGET AST entrada=«a - ( b - c )» legenda: Agora com parênteses à direita: 19 nós contra 5. A forma da AST mudou, mesmo sem nenhum nó de parêntese.
- WIDGET AST entrada=«( a + b ) * ( c - x )» legenda: 28 nós na derivação, 7 na AST: 4 folhas e 3 operadores.
- PASSOS (Regras que você acabou de ver acontecendo):
    1) Folha id/num → **Var** ou **Num** (um nó só, sem a corrente F → id).
    2) Produção com operador (E → E + T) → **BinOp** com o operador e os dois filhos.
    3) Produção de corrente (E → T, T → F) → nenhum nó novo: só repassa o filho.
    4) F → ( E ) → nenhum nó novo: devolve a AST de E. O parêntese evapora.
    5) Em expressões binárias: nós da AST = 2 × (nº de operadores) + 1.
- WIDGET MINILANG entrada=«m = a;
if (b > m) { m = b; }
print(m);» legenda: Comandos também viram nós (Atrib, Se...). O gerador anda nessa árvore e emite os desvios. Com a=4 e b=7, imprime 7.
- CAIXA [Poder do cérebro]: Uma expressão com 10 operadores binários: quantos nós tem a AST? E por que a árvore de derivação sempre tem MAIS nós que isso?
- REVELAR: Resposta
    → 21 nós (10 BinOp + 11 folhas). A árvore de derivação tem, no mínimo, um nó de não-terminal para cada nó da AST, mais os nós de corrente, de parênteses e dos próprios tokens de operador como folhas.

#### ESTILO WORKED (Exemplo resolvido): "Construindo a AST na descida recursiva"
- PROF: Lembra do parser descendente recursivo, com um método por não-terminal? A mudança é pequena: em vez de void, cada método **devolve uma Expr**.
- CÓDIGO (As classes da AST (Java, como no trabalho prático)):
```
abstract class Expr { }
class Num extends Expr { int valor;  Num(int v) { valor = v; } }
class Var extends Expr { String nome; Var(String n) { nome = n; } }
class BinOp extends Expr {
    char op; Expr esq, dir;
    BinOp(char op, Expr esq, Expr dir) { this.op = op; this.esq = esq; this.dir = dir; }
}
```
- CÓDIGO (O parser que CONSTRÓI a AST. O while em expr() e termo() garante associatividade à esquerda.):
```
Expr expr() {                 // E → T (('+'|'-') T)*
    Expr e = termo();
    while (tok.equals("+") || tok.equals("-")) {
        char op = tok.charAt(0); casaToken(tok);
        e = new BinOp(op, e, termo());   // associa à esquerda
    }
    return e;
}
Expr termo() {                // T → F (('*'|'/') F)*
    Expr e = fator();
    while (tok.equals("*") || tok.equals("/")) {
        char op = tok.charAt(0); casaToken(tok);
        e = new BinOp(op, e, fator());
    }
    return e;
}
Expr fator() {                // F → ( E ) | id | num
    if (tok.equals("(")) { casaToken("("); Expr e = expr(); casaToken(")"); return e; }
    if (ehNumero(tok)) { int v = Integer.parseInt(tok); casaToken(tok); return new Num(v); }
    String nome = tok; casaToken(tok); return new Var(nome);
}
```
- PASSOS (Rastreando '2 * x + y'):
    1) expr() chama termo(); termo() chama fator().
    2) fator() vê '2' (número) → devolve **new Num(2)**. Em termo(), e = Num(2).
    3) termo() vê '*': casa, chama fator(), que devolve **Var(x)**. e = **new BinOp('*', Num(2), Var(x))**.
    4) termo() vê '+', que não é * nem /: sai do while e devolve o BinOp('*').
    5) De volta em expr(): e = BinOp('*'). Vê '+': casa, chama termo(), que devolve **Var(y)**.
    6) e = **new BinOp('+', BinOp('*', Num(2), Var(x)), Var(y))**. Fim da entrada: devolve e.
- WIDGET AST entrada=«2 * x + y» legenda: Confira: 13 nós na derivação, 5 na AST, e o código Java gerado.
- CAIXA [Cuidado!]: A linha mais importante é `e = new BinOp(op, e, termo());`: o resultado ANTERIOR vira o filho ESQUERDO do novo nó. É assim que 'a - b - c' vira (a - b) - c. Se você escrever recursão (`e = new BinOp(op, e, expr())`), a árvore fica pendurada para a direita e a subtração dá errado.
- TEXTO: Agora é com você (exemplo esmaecido): **a - ( b - c )**.
- REVELAR: O que fator() devolve ao encontrar '('?
    → Casa '(', chama expr(), que monta new BinOp('-', Var(b), Var(c)), casa ')' e devolve ESSE BinOp. Nenhum nó de parêntese é criado.
- REVELAR: Escreva o código Java completo da AST.
    → new BinOp('-', new Var("a"), new BinOp('-', new Var("b"), new Var("c")))
- REVELAR: Quantos nós tem cada árvore?
    → Árvore de derivação: 19 nós. AST: 5 nós (2 BinOp + 3 Var).
- WIDGET AST entrada=«a - ( b - c )» legenda: Confira suas respostas.
- CAIXA [Dica de prova]: Questão comum: 'dada a gramática com ações semânticas, desenhe a AST de tal expressão'. Faça em dois passos: primeiro a árvore de derivação (rascunho), depois risque correntes e parênteses e troque cada produção com operador por um BinOp.

#### ESTILO SOCRATIC (Perguntas guiadas): "O que o compilador realmente precisa saber?"
- PROF: Hoje eu pergunto, você descobre. Comece olhando a árvore de derivação de '( a + b ) * c'.
- WIDGET AST entrada=«( a + b ) * c» legenda: 18 nós na derivação. Olhe primeiro só o lado esquerdo.
- PREVEJA: Para GERAR CÓDIGO dessa expressão, de que informação o compilador precisa?
    opções: ✔Quais operações, em que ordem, sobre quais operandos | Quais não-terminais foram usados | Onde estavam os parênteses no texto | Quantos espaços havia entre os tokens
    explicação: Gerar t1 = a + b; t2 = t1 * c só exige operações, operandos e ordem. Todo o resto era andaime para o PARSER decidir essa ordem.
- PREVEJA: Se a AST não tem parênteses, onde está guardada a informação 'some antes de multiplicar'?
    opções: Num campo prioridade em cada nó | ✔Na forma da árvore: o + é FILHO do * | Numa tabela à parte | Ela se perde
    explicação: Quem está mais fundo é avaliado primeiro (pós-ordem). A estrutura É a precedência.
- BYTE: E o nó T no meio da corrente E → T → F? Ele não faz nada?
- REVELAR: Faz alguma coisa?
    → Na derivação, ele registra que a gramática passou pelo nível de precedência do *. Mas, como não houve nenhum *, não há operação a representar. Na AST, ele simplesmente some: o método termo() devolve o que fator() devolveu.
- PREVEJA: No TP, seu grupo gera assembly direto no parser. Aí surge: 'a variável x é usada antes de ser declarada mais abaixo no arquivo'. O que fica difícil?
    opções: Nada | ✔Você já emitiu código para x antes de conhecer a declaração; seria preciso voltar atrás | O analisador léxico quebra
    explicação: Tradução direta é UMA passada só. Com uma AST, você faz uma passada para coletar declarações e checar tipos e OUTRA para gerar código, quantas forem necessárias.
- REVELAR: Cite duas outras vantagens de construir a AST antes de gerar código.
    → 1) Separação de responsabilidades: o parser só reconhece e monta; a semântica e o gerador vivem em métodos das classes (avaliar, verificarTipo, gerar), mais fáceis de testar. 2) Otimizações como dobra de constantes viram 'trocar um BinOp(Num, Num) por um Num' na árvore. Bônus: dá para imprimir a AST e depurar.
- PREVEJA: E comandos, como if e while? Eles viram o quê na AST?
    opções: Somem, como os parênteses | ✔Nós próprios, como Se(cond, entao, senao) e Enquanto(cond, corpo) | Folhas Var
    explicação: Cada construção com significado vira uma classe. Palavras-chave, chaves e ponto e vírgula somem; condição e corpo ficam como filhos.
- WIDGET MINILANG entrada=«i = 0;
s = 0;
while (i < 4) { s = s + i; i = i + 1; }
print(s);» legenda: Um Enquanto com Bloco de duas Atrib dentro. O gerador percorre a árvore e produz o laço com desvios. Imprime 6.
- CAIXA [Conversa ao pé da lareira]: Árvore de derivação: 'Eu tenho TUDO, cada detalhe!' AST: 'E por isso ninguém aguenta andar em você três vezes.' Árvore de derivação: 'Hmpf. Pelo menos o parser me ama.' AST: 'O parser te usa e me entrega para o resto do compilador.'

#### ESTILO FORMAL (Definição de prova): "AST: definição de prova"
- TEXTO: **Árvore de derivação (concreta)**: raiz = símbolo inicial; cada nó interno é um não-terminal A cujos filhos são o lado direito de uma produção A → α usada; folhas, lidas da esquerda para a direita, formam a sentença.
- TEXTO: **Árvore sintática abstrata (AST)**: cada nó interno é um **operador ou construção** da linguagem e seus filhos são os **operandos/componentes** com significado. Omite não-terminais de cadeia (A → B), parênteses, pontuação e ε. A AST não é única da gramática: é uma escolha de projeto do compilador.
- CÓDIGO (Comandos como classes (junto com Expr, Num, Var, BinOp)):
```
abstract class Cmd { }

class Atrib extends Cmd {          // x = expr;
    String var; Expr valor;
}
class Se extends Cmd {             // if (cond) entao else senao
    Expr cond; Cmd entao, senao;   // senao == null quando não há else
}
class Enquanto extends Cmd {       // while (cond) corpo
    Expr cond; Cmd corpo;
}
class Bloco extends Cmd {          // { c1 c2 ... }
    List<Cmd> cmds = new ArrayList<>();
}
```
- TEXTO: **Construção dirigida pela sintaxe**: cada produção recebe uma regra semântica que sintetiza o atributo `nodo` (a AST do lado esquerdo) a partir dos `nodo` dos filhos. É uma definição **S-atribuída**: os atributos sobem da folha para a raiz.
- CÓDIGO (No Bison (ascendente): $$ é o valor do lado esquerdo; $1, $2, $3 os dos símbolos do lado direito):
```
exp   : exp '+' termo     { $$ = new BinOp('+', $1, $3); }
      | exp '-' termo     { $$ = new BinOp('-', $1, $3); }
      | termo             { $$ = $1; }          /* corrente: só repassa */
      ;
termo : termo '*' fator   { $$ = new BinOp('*', $1, $3); }
      | fator             { $$ = $1; }
      ;
fator : '(' exp ')'       { $$ = $2; }          /* o parêntese SOME aqui */
      | ID                { $$ = new Var($1); }
      | NUM               { $$ = new Num($1); }
      ;
```
- PREVEJA: Na regra `fator : '(' exp ')' { $$ = $2; }`, por que $2?
    opções: Porque o parêntese é o 2º símbolo | ✔Porque exp é o 2º símbolo do lado direito, e a AST do fator é simplesmente a AST de exp | Para criar um nó de parênteses
    explicação: '(' é $1, exp é $2, ')' é $3. Repassar $2 é exatamente 'jogar fora o parêntese'.
- TEXTO: **Descendente recursivo**: cada método de não-terminal devolve a AST do que reconheceu. Para A → A op B (recursiva à esquerda, eliminada como A → B (op B)*), use um **laço** que acumula: `e = new BinOp(op, e, B())`, preservando a associatividade à esquerda.
- CÓDIGO (Percorrendo a AST: cada fase é um método (ou um Visitor). Repare na pós-ordem: filhos antes do pai.):
```
abstract class Expr {
    abstract int avaliar(Map<String, Integer> mem);
    abstract String gerar(Gerador g);          // devolve onde ficou o valor
}
class Num extends Expr {
    int valor;
    int avaliar(Map<String, Integer> mem) { return valor; }
    String gerar(Gerador g) { return String.valueOf(valor); }
}
class Var extends Expr {
    String nome;
    int avaliar(Map<String, Integer> mem) { return mem.get(nome); }
    String gerar(Gerador g) { return nome; }
}
class BinOp extends Expr {
    char op; Expr esq, dir;
    int avaliar(Map<String, Integer> mem) {
        int x = esq.avaliar(mem), y = dir.avaliar(mem);   // filhos primeiro (pós-ordem)
        switch (op) { case '+': return x + y; case '-': return x - y;
                      case '*': return x * y; default:  return x / y; }
    }
    String gerar(Gerador g) {
        String x = esq.gerar(g), y = dir.gerar(g);
        String t = g.novoTemp();
        g.emite(t + " = " + x + " " + op + " " + y);       // TAC!
        return t;
    }
}
```
- CAIXA [No mundo real]: Com muitas fases, em vez de pôr avaliar/gerar/verificarTipo em cada classe, usa-se o padrão **Visitor**: uma classe por fase (VerificadorDeTipos, GeradorMips) com um método visitar por tipo de nó. javac, Roslyn (C#) e o Clang são organizados em torno da AST assim.
- CAIXA [Dica de prova]: Três perguntas que caem: (1) desenhar a AST de uma expressão (lembre: sem parênteses, associativa à esquerda); (2) escrever as ações $$ = new ... de uma gramática Bison; (3) 'diferencie árvore de derivação de AST', responda com a definição + um exemplo com contagem de nós.
- REVELAR: Defina AST e cite três coisas que ela descarta em relação à árvore de derivação.
    → Árvore em que nós internos são operadores/construções e filhos são operandos com significado. Descarta: não-terminais de cadeia (E → T → F), parênteses/pontuação e produções ε (e, com elas, as palavras-chave que só marcavam a construção).

#### ESTILO MNEMONIC (Truque de memória): "A árvore de dieta"
- CAIXA [Gancho de memória]: **AST = Árvore Sem Tralha.** Tira três tralhas: **C**orrentes (E → T → F), **P**arênteses (e pontuação) e **E**psilons. 'Corta o CPE.'
- TEXTO: **Palácio da memória**: na Cidade Semântica, ao lado do cartório da tabela de símbolos, fica a **Fábrica de AST** , uma marcenaria. Chega um tronco enorme (a árvore de derivação); o marceneiro serra os galhos que só passam adiante, joga a serragem dos parênteses fora e entrega uma mesa de 5 peças, pronta para uso.
- CAIXA [Gancho de memória]: **Parênteses são andaimes**: indispensáveis enquanto o prédio está sendo construído (parsing), retirados quando ele fica pronto. A forma do prédio (a árvore) já guarda o que eles seguravam.
- CAIXA [Gancho de memória]: **Corrente de WhatsApp**: E → T → F → a é como mensagem encaminhada cinco vezes. O conteúdo é o 'a'; os encaminhamentos não acrescentam nada. Na AST: Var(a), e só.
- CAIXA [Gancho de memória]: **Quem está mais fundo trabalha primeiro.** Na AST de a + b * c, o * está no porão: é feito antes. Profundidade = precedência.
- PREVEJA: Qual linha de código é o 'segredo' da associatividade à esquerda no parser descendente?
    opções: return new BinOp(op, termo(), expr()); | ✔e = new BinOp(op, e, termo()); dentro de um while | if (tok.equals("(")) return expr();
    explicação: 'O velho vai para a esquerda': o resultado acumulado vira filho esquerdo do novo nó, a cada volta do laço.
- CAIXA [Gancho de memória]: **Bison em três palavras: 'filho vira pai'.** $$ = new BinOp('+', $1, $3), os filhos $1 e $3 se tornam filhos do nó novo $$. Corrente? $$ = $1. Parêntese? $$ = $2.
- CAIXA [Cuidado!]: Contrastes que confundem: **árvore de derivação** tem não-terminais e todos os tokens; a **AST** tem só operadores e operandos. **AST** é estrutura; **TAC** é uma lista de instruções gerada a partir dela. **Parse tree** = árvore de derivação = árvore sintática concreta (três nomes, uma coisa).
- WIDGET AST entrada=«( a + b ) * c» legenda: Corta o CPE: conte as correntes, os parênteses e veja 18 virar 5.
- REVELAR: Sem olhar: o que é jogado fora, o que fica e onde mora a precedência?
    → Fora: correntes, parênteses/pontuação, ε. Fica: operadores (BinOp) e operandos (Var, Num), e construções (Atrib, Se, Enquanto, Bloco). Precedência e associatividade moram na FORMA da árvore.
- REVELAR: Por que vale a pena montar a AST no TP em vez de gerar código direto no parser?
    → Porque aí o compilador pode percorrer o programa várias vezes (tipos, depois código), com cada fase separada e testável, e otimizar mexendo na árvore. Tradução direta é uma passada só, sem volta.
