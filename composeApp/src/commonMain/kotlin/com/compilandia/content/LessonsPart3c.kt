package com.compilandia.content

import com.compilandia.content.Beat.Callout
import com.compilandia.content.Beat.Code
import com.compilandia.content.Beat.Predict
import com.compilandia.content.Beat.Reveal
import com.compilandia.content.Beat.Say
import com.compilandia.content.Beat.Steps
import com.compilandia.content.Beat.Text
import com.compilandia.content.Beat.Visual
import com.compilandia.content.CalloutKind.BRAIN_POWER
import com.compilandia.content.CalloutKind.EXAM_TIP
import com.compilandia.content.CalloutKind.FIRESIDE
import com.compilandia.content.CalloutKind.MNEMONIC
import com.compilandia.content.CalloutKind.NO_DUMB_QUESTIONS
import com.compilandia.content.CalloutKind.REAL_WORLD
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.LEXI
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

/** Programa de exemplo com três blocos aninhados (usado em várias aulas de escopo). */
internal const val P3_SCOPE_PROGRAM = """{                        // bloco 0
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
}"""

// =====================================================================
// SCOPE_SYMBOLS — Escopo e tabela de símbolos
// =====================================================================

internal val scopeLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.STORY, "O porteiro do condomínio",
        listOf(
            Say(NARRADOR, "Cidade Semântica, bairro dos Escopos. Prédios dentro de condomínios, apartamentos dentro de prédios."),
            Say(BYTE, "Tem dois 'x' no meu programa. Como o compilador sabe de qual eu estou falando?"),
            Say(PROF, "Do mesmo jeito que o porteiro acha o 'João' certo. Imagine: alguém chega no seu apartamento e pergunta pelo João."),
            Text("O porteiro procura primeiro **no seu apartamento**. Não achou? Procura **no prédio**. Depois **no condomínio inteiro**. O primeiro João encontrado, de dentro para fora, é o João certo."),
            Text("Se tem um João no seu apê e outro no condomínio, o do apê **esconde** o de fora enquanto você está lá dentro. Isso é **sombreamento** (shadowing)."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Apartamento, prédio, condomínio → **blocos aninhados** { { { } } }",
                    "Lista de moradores de cada lugar → **uma tabela de símbolos por escopo**",
                    "Procurar de dentro para fora → **regra do aninhamento mais próximo** (escopo estático)",
                    "Mudou-se do apê → **sair do bloco**: a lista dele é descartada",
                    "Nenhum João em lugar nenhum → erro **'variável não declarada'**",
                ),
            ),
            Say(PILHA, "As listas... ficam empilhadas. Entrou num bloco... empilha. Saiu... desempilha. Eu adoro... esse trabalho."),
            Code(P3_SCOPE_PROGRAM, "Um condomínio de código"),
            Predict(
                "Na linha print(x + y), dentro do bloco 2, quais declarações são usadas?",
                listOf("x (1) e y (2)", "x (3) e y (4)", "x (3) e y (2)"),
                1,
                "De dentro para fora: y está no próprio bloco 2 (4); x não está no bloco 2, mas está no bloco 1 (3). O x (1) fica escondido.",
            ),
            Visual(VisualKind.SCOPE, caption = "Ligue cada uso à sua declaração, como o porteiro faria."),
            Callout(NO_DUMB_QUESTIONS, "'E o léxico não resolve isso?' Não: a Lexi só sabe que 'x' é um ID. Quem decide QUAL x é a análise semântica, com a tabela de símbolos."),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.VISUAL, "A pilha de tabelas, quadro a quadro",
        listOf(
            Say(PROF, "Vamos assistir a pilha de tabelas enquanto o compilador lê o programa de cima para baixo."),
            Code(P3_SCOPE_PROGRAM, "O programa"),
            Steps(
                "A pilha em cada momento (topo à direita)",
                listOf(
                    "Abre bloco 0 → [ {} ]",
                    "int x; int y; → [ {x:int(1), y:int(2)} ]",
                    "Abre bloco 1 → [ {x(1), y(2)} | {} ]",
                    "float x; → [ {x(1), y(2)} | {x:float(3)} ] — o x(3) sombreia o x(1)",
                    "x = y + 1 → x acha (3) no topo; y não está no topo, desce e acha (2)",
                    "Abre bloco 2, int y; → [ ... | {x(3)} | {y:int(4)} ]",
                    "print(x + y) → x: (3), y: (4)",
                    "Fecha bloco 2 → a tabela {y(4)} é descartada",
                    "print(y) → y: (2)   •   Fecha bloco 1   •   print(x) → x: (1)",
                ),
            ),
            Visual(VisualKind.SCOPE, caption = "Agora com outro programa: para cada uso, qual declaração vence?"),
            Say(LEXI, "Para mim, todos esses x são iguais. Olha só:"),
            Visual(VisualKind.TOKENIZE, input = "int x = 1; { float x = 2.5; print(x); }", caption = "O léxico vê ID, ID, ID... sem saber a qual declaração cada um pertence."),
            Predict(
                "Depois de fechar o bloco 1, alguém ainda consegue achar o x (3)?",
                listOf("Sim, ele fica na tabela global", "Não, a tabela do bloco 1 foi desempilhada", "Só se for float"),
                1,
                "Saiu do bloco, a tabela daquele escopo sai do topo. O próximo print(x) só enxerga o x (1).",
            ),
            Visual(VisualKind.ATTR_TREE, input = "a + z", caption = "E se o nome não está em tabela nenhuma? A busca falha: 'z' não foi declarada — e o erro sobe pela árvore."),
            Callout(BRAIN_POWER, "Repare: a busca percorre a pilha do TOPO para a BASE. Isso é toda a regra de escopo estático, em uma frase."),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.WORKED, "Ligando cada uso à sua declaração",
        listOf(
            Say(PROF, "Exercício típico: para cada uso de variável, diga qual declaração ele referencia."),
            Code(P3_SCOPE_PROGRAM, "Programa (declarações numeradas de 1 a 4)"),
            Steps(
                "Resolvendo os usos, de dentro para fora",
                listOf(
                    "x = 1 (bloco 0): x está no bloco 0 → **(1)**",
                    "x = y + 1 (bloco 1): x está no bloco 1 → **(3)**; y não está no bloco 1, sobe para o 0 → **(2)**",
                    "print(x + y) (bloco 2): x não está no 2, está no 1 → **(3)**; y está no 2 → **(4)**",
                ),
            ),
            Say(BYTE, "Peraí, deixa os últimos comigo."),
            Reveal("print(y), depois que o bloco 2 fechou.", "(2). A tabela do bloco 2, onde estava o y (4), já foi desempilhada; no bloco 1 não há y; no bloco 0 há o (2)."),
            Reveal("print(x), na última linha.", "(1). Estamos de volta ao bloco 0; o x float (3) morreu com o bloco 1."),
            Reveal("Se dentro do bloco 2 houvesse 'int y;' duas vezes, o que aconteceria?", "Erro de redeclaração: o mesmo nome declarado duas vezes no MESMO escopo. Sombrear é declarar num escopo DIFERENTE."),
            Reveal("Qual o tipo da expressão x + y em print(x + y)?", "x (3) é float e y (4) é int → float + int = float. A tabela de símbolos alimenta a verificação de tipos!"),
            Visual(VisualKind.SCOPE, caption = "Pratique com um programa sorteado."),
            Callout(EXAM_TIP, "Na prova, desenhe a pilha de tabelas ao lado do código e atualize a cada { e }. É mais rápido e evita confundir quem já 'morreu'."),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.SOCRATIC, "Uma tabela só não bastaria?",
        listOf(
            Say(PROF, "Vamos desenhar a tabela de símbolos juntos, com perguntas."),
            Reveal("Que informação guardar para cada nome?", "Pelo menos: nome, tipo, categoria (variável, função, parâmetro...) e, mais tarde, endereço/deslocamento na memória. Às vezes a linha da declaração, para mensagens de erro."),
            Reveal("Por que um único mapa global nome → informação não funciona?", "Porque o mesmo nome pode ter declarações diferentes em blocos diferentes (x int e x float). E ao sair de um bloco, os nomes dele precisam sumir."),
            Reveal("Então, o que fazer ao entrar num bloco? E ao sair?", "Entrar: criar uma tabela nova, ligada à anterior. Sair: voltar para a anterior, descartando a nova. Uma pilha de tabelas!"),
            Say(BYTE, "E pra procurar um nome, eu começo por onde?"),
            Reveal("Por onde começa a busca, e quando ela para?", "Começa na tabela do topo (bloco mais interno) e vai descendo; para no primeiro achado. Se chegar ao fundo sem achar: 'não declarada'."),
            Predict(
                "Declarar 'int x' quando já existe 'x' num bloco EXTERNO é...",
                listOf("Erro de redeclaração", "Permitido: o novo x sombreia o externo", "Ignorado"),
                1,
                "A verificação de redeclaração olha só a tabela do TOPO. Em blocos diferentes, o novo x sombreia o antigo (C e C++ permitem; Java, por escolha de projeto, proíbe uma local sombrear outra local — mas a tabela funciona igual).",
            ),
            Reveal("Escopo estático (léxico) vs dinâmico: qual a diferença?", "Estático: o x certo é decidido pelo TEXTO do programa (aninhamento). Dinâmico: pela ordem de CHAMADAS em tempo de execução (a declaração mais recente ativa). C, Java e Kotlin usam estático."),
            Reveal("Em qual fase o compilador usa e preenche a tabela?", "O léxico pode inserir os nomes; a análise sintática/semântica insere declarações e resolve usos; a geração de código usa os endereços. Ela acompanha todas as fases."),
            Visual(VisualKind.SCOPE, caption = "Teste suas respostas."),
            Callout(BRAIN_POWER, "Se o compilador inserisse 'x' do bloco interno na MESMA tabela do externo, o que aconteceria com o x externo ao sair do bloco? (Ele teria sido sobrescrito — perdido para sempre.)"),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.FORMAL, "Escopo estático e ambientes encadeados",
        listOf(
            Text("**Escopo** de uma declaração: a região do programa em que um uso daquele nome se refere a ela. **Regra do aninhamento mais próximo:** um uso de x refere-se à declaração de x no bloco mais interno que contém o uso."),
            Code(
                """classe Env:
  tabela:   Mapa<nome, Simbolo>
  anterior: Env?                      // escopo envolvente

  put(nome, s): tabela[nome] := s
  get(nome):
    e := este
    enquanto e ≠ nulo:
      se nome ∈ e.tabela: retorne e.tabela[nome]
      e := e.anterior
    retorne nulo""",
                "Tabela de símbolos encadeada (Livro do Dragão, seção 2.7)",
            ),
            Code(
                """bloco  → '{'       { salvo := topo; topo := new Env(topo) }
           decls stmts
           '}'       { topo := salvo }
decl   → tipo id ;   { se id ∈ topo.tabela: erro "redeclaração"
                       senão topo.put(id, Simbolo(tipo)) }
fator  → id          { s := topo.get(id)
                       se s = nulo: erro "não declarada"
                       senão fator.tipo := s.tipo }""",
                "Esquema de tradução usando a pilha de ambientes",
            ),
            Steps(
                "Operações e custos (com tabela hash por escopo)",
                listOf(
                    "entrar no escopo: O(1)",
                    "inserir: O(1) esperado — só no escopo do topo",
                    "buscar: O(profundidade do aninhamento) no pior caso",
                    "sair do escopo: O(1) — descarta a tabela do topo",
                ),
            ),
            Predict(
                "Escopo dinâmico: f usa x sem declarar; g declara x=2 e chama f; o global tem x=1. Com escopo DINÂMICO, f vê x =",
                listOf("1", "2", "erro"),
                1,
                "Dinâmico segue a cadeia de CHAMADAS: a ativação mais recente com x é a de g. No escopo estático, f veria o global (1).",
            ),
            Visual(VisualKind.SCOPE, caption = "Escopo estático: a resposta vem só do texto."),
            Callout(EXAM_TIP, "Ao responder 'qual declaração', justifique com a regra: 'pelo aninhamento mais próximo, o uso na linha N refere-se à declaração do bloco B'. E diferencie redeclaração (mesmo escopo, erro) de sombreamento (escopo interno, permitido)."),
            Callout(WATCH_OUT, "Escopo é sobre ONDE um nome é visível (estático, no texto). Tempo de vida é sobre QUANDO a variável existe na memória (dinâmico, na execução). Não são a mesma coisa."),
        ),
    ),
    Lesson(
        Skill.SCOPE_SYMBOLS, Style.MNEMONIC, "A cebola dos escopos",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Cidade Semântica, bairro dos Escopos. Cada casa é uma cebola: camadas dentro de camadas."),
            Callout(MNEMONIC, "**Escopo é cebola: procure de dentro para fora.** A primeira camada que tiver o nome vence."),
            Callout(MNEMONIC, "**'Abre chave, empilha; fecha chave, desempilha.'** A pilha de tabelas segue as chaves do programa."),
            Callout(MNEMONIC, "**E-I-B-S** — as quatro operações: **E**ntrar no escopo, **I**nserir, **B**uscar, **S**air. ('É I Bem Simples.')"),
            Callout(MNEMONIC, "Sombreamento: **'quem está mais perto faz sombra em quem está longe'**. O x de fora não morreu; só está na sombra."),
            Say(BYTE, "E se eu declarar o mesmo nome duas vezes na mesma camada?"),
            Say(PROF, "Aí não é sombra, é briga: **redeclaração**, erro. Sombra só existe entre camadas diferentes."),
            Predict(
                "Busca na pilha de tabelas: de onde para onde?",
                listOf("Da base para o topo", "Do topo para a base", "Só no topo"),
                1,
                "Topo = bloco mais interno. Descer a pilha = ir para fora na cebola. (Só no topo é a regra para checar REDECLARAÇÃO, não para buscar usos.)",
            ),
            Visual(VisualKind.SCOPE, caption = "Descasque a cebola: para cada uso, ache a camada certa."),
            Reveal("Recite a regra do escopo estático numa frase.", "Um uso de x refere-se à declaração de x no bloco mais interno (mais próximo) que contém o uso."),
            Callout(FIRESIDE, "Escopo estático: 'Me lê o código e você sabe quem eu sou.' Escopo dinâmico: 'Comigo depende de quem me chamou... é mais emocionante.' Programador cansado às 3 da manhã: 'Eu fico com o estático.'"),
        ),
    ),
)

// =====================================================================
// TAC — Código de três endereços
// =====================================================================

internal val tacLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.TAC, Style.STORY, "A receita para o ajudante que só faz uma coisa por vez",
        listOf(
            Say(NARRADOR, "Forja do Código. A árvore anotada chega numa esteira; do outro lado, sai uma lista de instruções."),
            Say(BYTE, "Por que não transformar a árvore direto em Assembly?"),
            Say(PROF, "Porque existem dezenas de processadores. É mais esperto traduzir para uma linguagem intermediária simples, e só no fim para cada máquina."),
            Text("Imagine uma receita para um ajudante de cozinha que só sabe fazer **uma operação por vez**, com **no máximo dois ingredientes**, e sempre coloca o resultado **numa tigela com etiqueta**."),
            Code("t1 = b * c\nt2 = a + t1", "'Junte b com c (multiplicando) na tigela t1; depois junte a com t1 na tigela t2.' — a + b * c"),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Uma operação por vez, dois ingredientes → **instrução de três endereços**: x = y op z",
                    "Tigelas com etiqueta t1, t2... → **temporários** gerados pelo compilador",
                    "Ordem da receita → **pós-ordem** da árvore: filhos antes do pai",
                    "Ficha com 4 campos (operação, ingr. 1, ingr. 2, tigela) → **quádrupla**",
                    "Ficha sem tigela: 'use o resultado do passo 0' → **tripla**",
                ),
            ),
            Predict(
                "Quantas instruções (sem contar a atribuição final) para a + b * c - a?",
                listOf("2", "3", "4"),
                1,
                "Três operadores → três instruções: t1 = b * c; t2 = a + t1; t3 = t2 - a.",
            ),
            Visual(VisualKind.TAC, input = "a + b * c", caption = "A máquina de receitas: a multiplicação vem primeiro porque está mais embaixo na árvore."),
            Say(PILHA, "Tigelas... são como eu. Guardam... resultados no meio do caminho."),
            Reveal("Por que 'três endereços'?", "Porque cada instrução menciona no máximo três nomes: dois operandos e um destino (x = y op z)."),
            Callout(REAL_WORLD, "O LLVM IR é uma linguagem intermediária no espírito do TAC (em forma SSA): Clang, Rust e Swift geram LLVM IR, e o LLVM o traduz para x86, ARM, RISC-V..."),
        ),
    ),
    Lesson(
        Skill.TAC, Style.VISUAL, "Da árvore à lista, em pós-ordem",
        listOf(
            Say(PROF, "Vamos ver a árvore e o código lado a lado."),
            Visual(VisualKind.ATTR_TREE, input = "a + b * c", caption = "A árvore: '*' está embaixo, '+' na raiz."),
            Visual(VisualKind.TAC, input = "a + b * c", caption = "Pós-ordem: b * c gera t1; depois a + t1 gera t2."),
            Steps(
                "A regra do gerador",
                listOf(
                    "Folha (variável ou número): o 'endereço' é o próprio nome",
                    "Nó operador: gera o código do filho esquerdo, depois o do direito",
                    "Cria um temporário novo tN e emite: tN = esq op dir",
                    "O endereço do nó passa a ser tN",
                ),
            ),
            Visual(VisualKind.TAC, input = "(a + b) * (a + b)", caption = "Repare: a + b é calculado DUAS vezes (t1 e t2). O gerador ingênuo não percebe — a otimização vai."),
            Say(BYTE, "E com comparação e lógica? Funciona igual?"),
            Visual(VisualKind.TAC, input = "a < b && ok", caption = "Aqui, como valores: t1 = a < b; t2 = t1 && ok. (Compiladores reais costumam usar desvios — curto-circuito.)"),
            Predict(
                "Para a + x * 2 (a int, x float), um compilador real gera...",
                listOf(
                    "t1 = x * 2; t2 = a + t1",
                    "t1 = x * 2; t2 = inttofloat(a); t3 = t2 + t1",
                    "t1 = a + x; t2 = t1 * 2",
                ),
                1,
                "A verificação de tipos manda converter a. O widget do jogo esconde a conversão, mas no Livro do Dragão ela aparece como instrução própria (e o 2 vira 2.0 na própria constante).",
            ),
            Visual(VisualKind.TAC, input = "a + x * 2", caption = "Versão simplificada do jogo (sem a instrução de conversão)."),
            Code("     op    arg1   arg2   resultado\n(0)  *     b      c      t1\n(1)  +     a      t1     t2", "As mesmas instruções de a + b * c como quádruplas"),
            Callout(BRAIN_POWER, "Número de temporários no gerador ingênuo = número de operadores. Dá para economizar reaproveitando tigelas que não serão mais usadas — isso é trabalho da alocação de registradores."),
        ),
    ),
    Lesson(
        Skill.TAC, Style.WORKED, "Expressões, quádruplas, triplas e um if",
        listOf(
            Say(PROF, "Exercício 1: gere TAC para x = (a + b) * (a - c)."),
            Steps(
                "Pós-ordem, com calma",
                listOf(
                    "Subárvore esquerda a + b → t1 = a + b",
                    "Subárvore direita a - c → t2 = a - c",
                    "Raiz * → t3 = t1 * t2",
                    "Atribuição → x = t3",
                ),
            ),
            Code(
                """quádruplas                      triplas
     op  arg1  arg2  res             op  arg1  arg2
(0)  +   a     b     t1        (0)   +   a     b
(1)  -   a     c     t2        (1)   -   a     c
(2)  *   t1    t2    t3        (2)   *   (0)   (1)
(3)  =   t3          x         (3)   =   x     (2)""",
                "Duas representações das mesmas instruções",
            ),
            Say(PROF, "Exercício 2: comandos com desvio. if (a < b) x = 1; else x = 2;"),
            Code(
                """    ifFalse a < b goto L1
    x = 1
    goto L2
L1: x = 2
L2: ...""",
                "Rótulos e desvios: o if vira 'pule se for falso'",
            ),
            Say(BYTE, "Agora eu faço!"),
            Reveal("TAC de y = a * b + a * b.", "t1 = a * b; t2 = a * b; t3 = t1 + t2; y = t3. (Sim, a * b duas vezes — o gerador ingênuo não otimiza.)"),
            Reveal("As triplas de y = a * b + a * b.", "(0) * a b; (1) * a b; (2) + (0) (1); (3) = y (2)."),
            Reveal("TAC de while (a < b) a = a + 1;", "L1: ifFalse a < b goto L2;  t1 = a + 1;  a = t1;  goto L1;  L2: ..."),
            Visual(VisualKind.TAC, input = "a * b + a * b", caption = "Confira a primeira resposta."),
            Callout(EXAM_TIP, "Em triplas, o resultado é referenciado pela POSIÇÃO: (2) * (0) (1). Na prova, numere as linhas desde o começo — senão as referências viram bagunça."),
        ),
    ),
    Lesson(
        Skill.TAC, Style.SOCRATIC, "Por que uma linguagem no meio do caminho?",
        listOf(
            Say(PROF, "Pergunta 1: se eu tenho 5 linguagens-fonte e 4 processadores, quantos compiladores completos eu escrevo sem código intermediário?"),
            Reveal("E com uma representação intermediária comum?", "Sem: 5 × 4 = 20 compiladores. Com: 5 front-ends + 4 back-ends = 9 peças. A IR é o 'adaptador universal'."),
            Reveal("Por que limitar a no máximo um operador por instrução?", "Porque se parece com instruções de máquina (um op por vez), torna a ordem de avaliação explícita e facilita otimizar e alocar registradores."),
            Reveal("Em que ordem as instruções de uma expressão aparecem?", "Na pós-ordem da árvore: cada operador só pode ser calculado depois dos seus operandos."),
            Say(BYTE, "E de onde saem os nomes t1, t2...?"),
            Reveal("Quem cria os temporários?", "O próprio compilador (new Temp()), um por nó operador. Eles não existem no programa-fonte."),
            Predict(
                "Por que quádruplas são mais fáceis de reordenar do que triplas?",
                listOf(
                    "Porque ocupam menos memória",
                    "Porque nas triplas os resultados são referenciados pela posição; mover uma instrução quebra as referências",
                    "Porque triplas não aceitam desvios",
                ),
                1,
                "Na quádrupla o resultado tem nome (t1); na tripla ele é '(0)'. Mudou a posição, mudou o nome. Por isso existem as triplas INDIRETAS (uma lista de ponteiros que se reordena).",
            ),
            Reveal("Como representar um if em TAC, se não há chaves nem blocos?", "Com rótulos e desvios: ifFalse cond goto L1, goto L2, L1: ... A estrutura vira um grafo de saltos."),
            Visual(VisualKind.TAC, input = "(a + b) * c", caption = "Teste: preveja as instruções antes de olhar."),
            Callout(BRAIN_POWER, "O TAC é baixo nível o bastante para ficar perto da máquina e alto o bastante para não depender dela. Esse equilíbrio é o que torna a otimização portátil."),
        ),
    ),
    Lesson(
        Skill.TAC, Style.FORMAL, "Instruções de três endereços e representações",
        listOf(
            Text("**Código de três endereços:** sequência de instruções com, no máximo, um operador no lado direito. Um 'endereço' pode ser um nome do programa, uma constante ou um temporário gerado pelo compilador."),
            Code(
                """x = y op z          atribuição binária
x = op y            atribuição unária (menos, not, conversão)
x = y               cópia
goto L              desvio incondicional
if x goto L         desvio condicional
ifFalse x goto L
if x relop y goto L
param x;  call p, n;  y = call p, n;  return y
x = y[i];  x[i] = y         acesso indexado
x = &y;  x = *y;  *x = y    endereços e ponteiros""",
                "Formas de instrução (Livro do Dragão, seção 6.2)",
            ),
            Code(
                """S → id = E ;   S.code := E.code || gen(top.get(id.lexeme) '=' E.addr)
E → E1 + E2    E.addr := new Temp()
               E.code := E1.code || E2.code || gen(E.addr '=' E1.addr '+' E2.addr)
E → - E1       E.addr := new Temp()
               E.code := E1.code || gen(E.addr '=' 'minus' E1.addr)
E → ( E1 )     E.addr := E1.addr;  E.code := E1.code
E → id         E.addr := top.get(id.lexeme);  E.code := ''""",
                "SDD de tradução de expressões para TAC (|| = concatenação)",
            ),
            Steps(
                "Representações",
                listOf(
                    "**Quádruplas:** (op, arg1, arg2, resultado) — resultado nomeado; fácil de reordenar",
                    "**Triplas:** (op, arg1, arg2) — o resultado é a posição da instrução; economiza espaço",
                    "**Triplas indiretas:** lista de ponteiros para triplas; reordena-se a lista",
                    "**SSA:** cada variável é atribuída uma única vez (x1, x2...), com funções φ nas junções",
                ),
            ),
            Predict(
                "Qual NÃO é uma instrução de três endereços válida?",
                listOf("t1 = a + b", "t2 = a + b * c", "if t1 goto L3"),
                1,
                "t2 = a + b * c tem dois operadores. Precisa virar t1 = b * c; t2 = a + t1.",
            ),
            Visual(VisualKind.TAC, input = "a + b - c", caption = "Associatividade à esquerda: (a + b) primeiro, depois - c."),
            Callout(EXAM_TIP, "Questão comum: 'traduza para quádruplas E triplas'. Faça primeiro o TAC em texto, numere as linhas, e só então preencha as duas tabelas."),
            Callout(WATCH_OUT, "Não confunda o 'três' do nome com 'três instruções'. É no máximo três ENDEREÇOS por instrução."),
        ),
    ),
    Lesson(
        Skill.TAC, Style.MNEMONIC, "Dois entram, um sai",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Forja do Código, oitava parada. Uma bigorna com três encaixes: dois para entrar, um para sair."),
            Callout(MNEMONIC, "**'Dois entram, um sai.'** x = y op z: y e z entram, x sai. Três endereços."),
            Callout(MNEMONIC, "**QUAdrupla = QUAtro campos** (op, arg1, arg2, resultado). **TRIpla = TRÊS campos**: ela não tem casa própria, mora no endereço (a posição)."),
            Callout(MNEMONIC, "**t de tigela.** t1, t2, t3 são tigelas temporárias na bancada do compilador."),
            Callout(MNEMONIC, "**Pós-ordem = 'filhos primeiro, pai depois'.** Igual a jantar de família: as crianças se servem antes."),
            Say(BYTE, "E a ordem das tigelas é sempre crescente, né? t1 antes de t2?"),
            Say(PROF, "No gerador do jogo, sim: cada operador visitado em pós-ordem ganha o próximo número."),
            Predict(
                "Na tripla (2) + (0) (1), o que significa '(0)'?",
                listOf("O número zero", "O resultado da instrução da posição 0", "O temporário t0"),
                1,
                "Triplas apontam pela posição. '(0)' = 'o que a instrução 0 calculou'.",
            ),
            Visual(VisualKind.TAC, input = "a * b + c * a", caption = "Dois entram, um sai — três vezes."),
            Reveal("Recite as três representações e o que as diferencia.", "Quádruplas (resultado com nome), triplas (resultado = posição) e triplas indiretas (lista de ponteiros para reordenar)."),
            Callout(FIRESIDE, "Quádrupla: 'Eu tenho nome e endereço próprio, pode me mudar de lugar.' Tripla: 'Eu sou mais econômica... só não me tire do lugar, senão ninguém me acha.'"),
        ),
    ),
)

// =====================================================================
// OPTIMIZATION — Otimização (dobra de constantes e amigos)
// =====================================================================

internal val optLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.OPTIMIZATION, Style.STORY, "A lista de compras inteligente",
        listOf(
            Say(NARRADOR, "Forja do Código, sala de polimento. O TAC chega meio bruto; aqui ele sai brilhando."),
            Say(BYTE, "Otimizar é deixar o código ÓTIMO?"),
            Say(PROF, "O nome engana: é **melhorar**, sem mudar o resultado do programa. 'Ótimo' de verdade é impossível de garantir. Vamos pensar numa lista de compras."),
            Text("Sua lista diz: 'comprar 2 × 6 ovos'. Você faz a conta **em casa**, não no caixa: 12 ovos. Isso é **dobra de constantes** — calcular em tempo de compilação."),
            Text("A lista diz 'comprar leite' duas vezes, e nada mudou entre uma e outra. Você compra uma vez só e usa as duas. Isso é eliminar **subexpressão comum**."),
            Text("A lista tem 'comprar fermento', mas nenhuma receita usa. Corta! Isso é eliminar **código morto**."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Fazer a conta em casa → **dobra de constantes** (2 * 6 vira 12)",
                    "Anotar 'ovos = 12' e usar 12 dali em diante → **propagação de constantes**",
                    "Comprar leite uma vez só → **eliminação de subexpressões comuns**",
                    "Riscar o fermento que ninguém usa → **eliminação de código morto**",
                    "Trecho do mercado sem desvios, entra e sai por um lugar só → **bloco básico**",
                ),
            ),
            Predict(
                "Qual linha pode ser calculada inteiramente pelo compilador?",
                listOf("t1 = a * 2", "t1 = 2 * 3", "t1 = x * y"),
                1,
                "Só 2 * 3 depende apenas de constantes. Vira t1 = 6 antes do programa rodar.",
            ),
            Visual(VisualKind.TAC, input = "2 * 3 + a", caption = "O gerador produz t1 = 2 * 3; com dobra de constantes, sobra só uma instrução: 6 + a."),
            Callout(WATCH_OUT, "Regra de ouro: otimização NUNCA pode mudar o que o programa faz. Na dúvida (efeitos colaterais, divisão por zero...), o compilador não otimiza."),
            Reveal("Na analogia, por que 'comprar leite' duas vezes só pode virar uma se 'nada mudou entre uma e outra'?", "Se alguém tivesse bebido o leite no meio (uma variável redefinida), a segunda compra seria necessária. Subexpressão comum só vale sem redefinição dos operandos."),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.VISUAL, "Antes e depois do polimento",
        listOf(
            Say(PROF, "Vamos ver o TAC antes e depois de cada otimização."),
            Visual(VisualKind.TAC, input = "x * 2 + 3 * 4", caption = "Antes: t2 = 3 * 4. Com dobra: o 3 * 4 vira 12 e some uma instrução."),
            Visual(VisualKind.TAC, input = "a * 4 + 2 * 3", caption = "Mesmo truque: 2 * 3 vira 6. O a * 4 fica — a só é conhecido em execução."),
            Visual(VisualKind.TAC, input = "(a + b) * (a + b)", caption = "Subexpressão comum: t1 = a + b e t2 = a + b."),
            Code("antes:            depois:\nt1 = a + b        t1 = a + b\nt2 = a + b        t3 = t1 * t1\nt3 = t1 * t2", "Eliminação de subexpressão comum"),
            Say(BYTE, "E o código morto, como a gente vê?"),
            Code("antes:            depois:\nt1 = a + b        t2 = a * c\nt2 = a * c        x = t2\nx = t2\n// t1 nunca é usado", "Eliminação de código morto (t1 não está vivo)"),
            Code(
                """(1) i = 0                ← líder (primeira instrução)
(2) L1: if i >= n goto L2  ← líder (alvo de desvio)
(3) t1 = i * 4
(4) s = s + t1
(5) i = i + 1
(6) goto L1
(7) L2: return s         ← líder (alvo, e vem depois de desvio)""",
                "Blocos básicos: B1 = (1); B2 = (2); B3 = (3)-(6); B4 = (7)",
            ),
            Predict(
                "A instrução (3) é líder?",
                listOf("Sim: vem logo depois de um desvio condicional", "Não: não é alvo de nenhum goto", "Só se i for constante"),
                0,
                "(2) é um desvio (if ... goto). A instrução seguinte a qualquer desvio é líder. Por isso (2) sozinha forma um bloco.",
            ),
            Callout(BRAIN_POWER, "Otimização local trabalha dentro de UM bloco básico, onde a execução é linear — sem surpresas. A global olha o grafo de fluxo inteiro."),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.WORKED, "Polindo um trecho, uma otimização de cada vez",
        listOf(
            Say(PROF, "Vamos otimizar este bloco básico. Suponha que, depois dele, só x e y são usados."),
            Code("(1) t1 = 4 * 2\n(2) t2 = a + t1\n(3) t3 = a + t1\n(4) x = t2 * t3\n(5) y = 0\n(6) y = x", "Bloco original"),
            Steps(
                "Uma passada de cada vez",
                listOf(
                    "**Dobra:** (1) t1 = 4 * 2 → t1 = 8",
                    "**Propagação de constantes:** t1 vale 8 → (2) t2 = a + 8 e (3) t3 = a + 8",
                    "**Subexpressão comum:** (3) calcula o mesmo que (2), sem redefinir a no meio → t3 = t2 (cópia)",
                    "**Propagação de cópias:** (4) x = t2 * t2",
                    "**Código morto:** t1 e t3 não são mais usados → remove (1) e (3)",
                    "**Código morto:** (5) y = 0 é sobrescrito em (6) sem uso → remove (5)",
                ),
            ),
            Code("t2 = a + 8\nx = t2 * t2\ny = x", "Resultado: de 6 instruções para 3"),
            Say(BYTE, "Ordem importa? Posso fazer código morto primeiro?"),
            Say(PROF, "Pode, mas uma otimização costuma abrir espaço para outra. Compiladores repetem as passadas até nada mudar."),
            Reveal("Otimize: t1 = 3 + 2; t2 = t1 * b; t3 = t1 * b; z = t2 + t3.", "t1 = 5 (dobra) → t2 = 5 * b; t3 = 5 * b (propagação) → t3 = t2 (subexpr. comum) → z = t2 + t2. Final: t2 = 5 * b; z = t2 + t2."),
            Reveal("Em t1 = a + b; a = 7; t2 = a + b; dá para trocar t2 por t1?", "NÃO! a foi redefinido entre as duas. a + b não é mais a mesma expressão."),
            Visual(VisualKind.TAC, input = "a * 4 + 2 * 3", caption = "Pratique: o que a dobra de constantes faz aqui?"),
            Callout(EXAM_TIP, "Escreva o nome de cada otimização ao aplicar ('dobra', 'propagação', 'subexpressão comum', 'código morto'). Muitas questões pontuam o nome tanto quanto o resultado."),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.SOCRATIC, "Quando é seguro otimizar?",
        listOf(
            Say(PROF, "A otimização tem um juramento: não mudar o comportamento. Vamos testar esse juramento com perguntas."),
            Reveal("x = 10 / 0 pode ser 'dobrado' pelo compilador?", "Não como valor: a divisão por zero precisa acontecer (ou ser reportada). O dobrador do jogo, inclusive, se recusa a dobrar isso."),
            Reveal("E t = 0.1 + 0.2 com float?", "Pode, desde que o compilador use exatamente a mesma aritmética da máquina-alvo. Senão o resultado mudaria."),
            Reveal("Quando duas ocorrências de a + b são a 'mesma' subexpressão?", "Quando nem a nem b foram redefinidos entre elas (e não há efeito colateral, como chamada de função que altera a ou b)."),
            Say(BYTE, "E código morto: como o compilador sabe que ninguém usa?"),
            Reveal("O que significa uma variável estar 'viva' num ponto?", "Que o valor dela pode ser usado depois, num caminho de execução, antes de ser redefinido. Atribuição a variável não viva é código morto."),
            Predict(
                "if (false) { print(1); } — o que a otimização pode fazer?",
                listOf("Nada", "Remover o bloco inteiro: é código inalcançável", "Trocar por print(0)"),
                1,
                "Com a condição constante, o desvio sempre pula o bloco. Inalcançável = pode sair.",
            ),
            Reveal("Por que dividir o código em blocos básicos antes de otimizar?", "Dentro de um bloco não há desvios para dentro nem para fora no meio: as instruções executam todas, em ordem. Fica fácil raciocinar sobre valores."),
            Reveal("x * 2 → x + x ou x << 1: que otimização é essa?", "Redução de força: trocar uma operação cara por uma equivalente mais barata."),
            Visual(VisualKind.TAC, input = "2 * 3 + a", caption = "Preveja o código depois da dobra antes de olhar."),
            Callout(BRAIN_POWER, "Pense numa expressão com chamada de função: f() + f(). É subexpressão comum? Só se f não tiver efeitos colaterais — e o compilador quase nunca consegue provar isso sozinho."),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.FORMAL, "Blocos básicos e transformações que preservam a semântica",
        listOf(
            Text("**Bloco básico:** sequência maximal de instruções consecutivas em que o fluxo só entra pela primeira e só sai pela última, sem desvios no meio. **Grafo de fluxo:** blocos como nós; arestas para os sucessores possíveis."),
            Code(
                """LÍDERES:
  1. a primeira instrução do código é líder
  2. toda instrução que é ALVO de um desvio (condicional ou não) é líder
  3. toda instrução que vem IMEDIATAMENTE APÓS um desvio é líder
BLOCOS:
  cada líder inicia um bloco, que vai até a instrução
  anterior ao próximo líder (ou até o fim)""",
                "Particionamento em blocos básicos (Livro do Dragão, Alg. 8.5)",
            ),
            Steps(
                "Transformações clássicas",
                listOf(
                    "**Dobra de constantes:** avaliar em compilação operações com operandos constantes (2 * 3 → 6)",
                    "**Propagação de constantes:** após x = 6, substituir usos de x por 6 enquanto x não for redefinido",
                    "**Subexpressões comuns:** E já calculada e operandos não redefinidos desde então → reutilizar o valor",
                    "**Propagação de cópias:** após x = y, usar y no lugar de x",
                    "**Código morto:** remover instruções cujo resultado não está vivo (nunca usado depois)",
                    "**Redução de força / movimentação de código invariante de laço:** trocar ops caras; tirar do laço o que não muda nele",
                ),
            ),
            Text("**Local** = dentro de um bloco básico (ex.: via DAG do bloco). **Global** = sobre o grafo de fluxo, com análise de fluxo de dados (variáveis vivas, definições que alcançam, expressões disponíveis)."),
            Predict(
                "Qual transformação exige saber quais variáveis estão VIVAS?",
                listOf("Dobra de constantes", "Eliminação de código morto", "Redução de força"),
                1,
                "Só é seguro apagar x = ... se o valor de x não for usado depois. 'Vivo' é exatamente isso.",
            ),
            Visual(VisualKind.TAC, input = "x * 2 + 3 * 4", caption = "Dobra de constantes local: 3 * 4 → 12."),
            Callout(EXAM_TIP, "Questão clássica: 'identifique os líderes e os blocos básicos'. Marque líderes com as 3 regras, numerando as instruções; depois desenhe o grafo de fluxo com as arestas de desvio E de queda (fall-through)."),
            Callout(WATCH_OUT, "A instrução APÓS um desvio é líder, e não a do desvio. O goto/if fecha o bloco; ele é a ÚLTIMA instrução do bloco."),
        ),
    ),
    Lesson(
        Skill.OPTIMIZATION, Style.MNEMONIC, "DOPA SUMO e o PAD dos blocos",
        listOf(
            Say(NARRADOR, "Palácio da Memória: última parada, sala de polimento da Forja. Na parede, um lutador de sumô dopado de café: DOPA SUMO."),
            Callout(MNEMONIC, "**DOPA SUMO**: **DO**bra, **P**ropag**A**, **SU**bexpressão comum, **MO**rto. As quatro otimizações que mais caem."),
            Callout(MNEMONIC, "Líderes: **PAD** — **P**rimeira instrução, **A**lvo de desvio, **D**epois de desvio."),
            Callout(MNEMONIC, "**'Faz a conta em casa, não no caixa.'** Dobra de constantes = calcular em compilação o que só depende de literais."),
            Callout(MNEMONIC, "Código morto: **'se ninguém lê a carta, não precisa escrever'**. Valor não vivo = instrução descartável."),
            Say(BYTE, "E o juramento?"),
            Say(PROF, "**'Mais rápido, nunca diferente.'** O programa otimizado tem que dar exatamente o mesmo resultado."),
            Predict(
                "t1 = 2 * 5; x = t1 + a — aplique DO e PA.",
                listOf("x = 10 + a", "x = 2 * 5 + a", "x = t1 + 10"),
                0,
                "DObra: t1 = 10. PropagA: x = 10 + a. E aí o MOrto leva o t1 = 10, se t1 não for usado de novo.",
            ),
            Visual(VisualKind.TAC, input = "a * 4 + 2 * 3", caption = "DOPA SUMO em ação: o 2 * 3 vira 6."),
            Reveal("Recite DOPA SUMO e PAD.", "Dobra, Propagação, Subexpressão comum, Morto. Líderes: Primeira, Alvo de desvio, Depois de desvio."),
            Callout(FIRESIDE, "Dobra de constantes: 'Eu faço as contas antes do show começar.' Código morto: 'E eu tiro do palco quem não tem fala.' Subexpressão comum: 'E eu não deixo ninguém repetir a mesma piada.'"),
        ),
    ),
)
