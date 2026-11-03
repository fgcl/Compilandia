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
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.FIRST_FOLLOW
import com.compilandia.content.VisualKind.LEFT_FACTORING
import com.compilandia.content.VisualKind.LEFT_RECURSION
import com.compilandia.content.VisualKind.LR0_AUTOMATON
import com.compilandia.content.VisualKind.LR_TRACE
import com.compilandia.content.VisualKind.PARSE_TREE
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// ───────────────────────────── DESCIDA RECURSIVA ─────────────────────────────

internal val p2RecDescentLessons: List<Lesson> = listOf(
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.STORY, "A repartição dos funcionários-função",
        listOf(
            Say(NARRADOR, "Corredor final do Templo Top-Down: uma repartição pública cheia de guichês. Cada guichê tem o nome de um não-terminal."),
            Say(PROF, "Na descida recursiva, **cada não-terminal vira uma função** — um funcionário. O funcionário S atende comandos; o L, listas; o E, expressões."),
            Code(P2_CMD, "A gramática dos comandos da repartição."),
            Say(BYTE, "E como o funcionário S sabe o que fazer?"),
            Say(PROF, "Ele olha a **senha do próximo cliente**: o token de lookahead. Senha 'if'? Segue o roteiro de S → if E then S. Senha 'print'? Roteiro de S → print E."),
            Text("Seguir o roteiro é simples: para cada **terminal** do lado direito, confere a senha e chama o próximo cliente (**match**). Para cada **não-terminal**, manda o cliente ao guichê daquele funcionário (**chama a função**)."),
            Predict(
                "S recebeu a senha 'begin'. Qual é a sequência de ações do funcionário S?",
                listOf("match(begin); L(); match(end)", "L(); match(begin); match(end)", "match(begin); S(); match(end)", "Chama E()"), 0,
                "É o roteiro de S → begin L end: confere 'begin', manda para o L, confere 'end'."
            ),
            Visual(PARSE_TREE, P2_CMD, "begin print id end", "A árvore sintática é o organograma das chamadas: cada nó interno é uma função que foi chamada."),
            Say(BYTE, "E o L, que pode ser ε? Como ele sabe que deve ficar quieto?"),
            Say(PROF, "Se a senha for 'end' (que está em FOLLOW(L)), o L retorna sem fazer nada: a lista acabou. Se for if, print ou begin (FIRST(S)), ele chama S() e depois L() de novo."),
            Callout(WATCH_OUT, "Funcionário que, como primeiro ato, chama a si mesmo sem atender ninguém (A → A α) entra em laço infinito. Recursão à esquerda mata a descida recursiva."),
            Reveal("Quem faz o papel da 'pilha' na descida recursiva, se não há pilha explícita?", "A pilha de chamadas do próprio programa! Cada função chamada e ainda não terminada é um não-terminal pendente."),
            Callout(REAL_WORLD, "GCC e Clang, compiladores de C/C++ usados no mundo inteiro, usam analisadores de descida recursiva escritos à mão: fáceis de ler e ótimos para mensagens de erro."),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.VISUAL, "Vendo as funções se chamarem",
        listOf(
            Say(PROF, "Vamos ver a descida recursiva acontecendo: cada passo da derivação é uma chamada de função."),
            Code(P2_EXPR_LL),
            Code(
                """
                fun E() { T(); Elinha() }
                fun Elinha() {
                  if (look == "+") { match("+"); T(); Elinha() }
                  // senão: ε (look deve estar em FOLLOW(E') = { ), ${'$'} })
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
                """.trimIndent(),
                "Uma função por não-terminal (E' virou Elinha, T' virou Tlinha)."
            ),
            Visual(DERIVATION, P2_EXPR_LL, "id * id", "Cada expansão desta derivação mais à esquerda corresponde a uma chamada de função do código acima."),
            Steps(
                "Chamadas para id * id",
                listOf(
                    "E() chama T().",
                    "T() chama F(); F() vê 'id' e faz match(id).",
                    "T() chama Tlinha(); vê '*': match(*), F() casa o segundo id, Tlinha() de novo.",
                    "Tlinha() vê \$: não faz nada (ε) e retorna.",
                    "De volta em E(), que chama Elinha(); vê \$: ε, retorna.",
                    "E() retorna com look = \$: sucesso!",
                )
            ),
            Predict(
                "Ao analisar 'id * id', quantas vezes F() é chamada?",
                listOf("1", "2", "3", "0"), 1,
                "Uma por id: T() chama F() para o primeiro e Tlinha() chama F() para o segundo."
            ),
            Visual(PARSE_TREE, P2_EXPR_LL, "id * id", "A árvore é o retrato das chamadas: os nós E, T, F, T' e E' são as funções."),
            Say(BYTE, "E por que não usar a gramática E → E + T direto?"),
            Visual(LEFT_RECURSION, P2_EXPR, caption = "Porque E() chamaria E() sem consumir nada: laço infinito. A transformação cria justamente as funções Elinha e Tlinha."),
            Reveal("Na árvore de 'id * id', quantos nós T' aparecem, e por quê?", "Dois: o primeiro T' (filho de T) expande para * F T', e o T' de dentro vira ε. Cada chamada de Tlinha() é um nó T'."),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.WORKED, "Exemplo resolvido: funções das declarações",
        listOf(
            Say(PROF, "Exercício de prova: escreva um analisador de descida recursiva para as declarações."),
            Code(P2_DECL),
            Text("Receita: **uma função por não-terminal**; uma variável global `look` com o token atual; uma função `match(t)` que confere e avança."),
            Code(
                """
                fun match(t: String) {
                  if (look == t) look = proximoToken()
                  else erro("esperava " + t + ", veio " + look)
                }
                """.trimIndent(),
                "O 'conferir a senha'."
            ),
            Code(
                """
                fun D() { T(); L(); match(";") }      // produção única

                fun T() {
                  when (look) {
                    "int"   -> match("int")
                    "float" -> match("float")
                    else    -> erro("esperava um tipo")
                  }
                }

                fun L() { match("id"); R() }          // L → id R
                """.trimIndent(),
                "D, T e L resolvidos. D e L têm uma produção só: nem precisam olhar o lookahead."
            ),
            Say(BYTE, "E o R, que tem ε?"),
            Say(PROF, "Esse fica para você. Pense: qual token faz R escolher R → , id R? E qual faz escolher R → ε?"),
            Reveal("Qual token escolhe R → , id R?", "',' — é FIRST(, id R)."),
            Reveal("E qual escolhe R → ε?", "';' — é FOLLOW(R). Qualquer outro token é erro."),
            Reveal("Escreva R() completo.", "fun R() { when (look) { \",\" -> { match(\",\"); match(\"id\"); R() };  \";\" -> { /* ε: não consome nada */ };  else -> erro(\"esperava , ou ;\") } }"),
            Visual(PARSE_TREE, P2_DECL, "int id , id ;", "Cada nó interno = uma chamada. Repare no R chamando R: recursão à DIREITA, inofensiva."),
            Callout(EXAM_TIP, "Nas produções com ε, deixe o teste explícito: 'se look ∈ FOLLOW(A), não faz nada; senão, erro'. Detectar o erro cedo (no else) costuma valer ponto."),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.SOCRATIC, "Inventando a descida recursiva",
        listOf(
            Say(PROF, "Vamos inventar a descida recursiva do zero. Objetivo: uma função que reconheça 'tudo que S gera'."),
            Predict(
                "S → a B c. Qual é o corpo mais natural de S()?",
                listOf("match(a); B(); match(c)", "B(); match(a); match(c)", "match(a); match(B); match(c)", "S(); B()"), 0,
                "Siga o lado direito da esquerda para a direita: terminal vira match; não-terminal vira chamada da função dele."
            ),
            Predict(
                "S → a B | c D. Como S() escolhe?",
                listOf("Tenta a primeira e, se falhar, volta atrás", "Olha o token: 'a' → primeira, 'c' → segunda", "Sempre a primeira", "Sorteia"), 1,
                "Com FIRST disjuntos, um token basta. É a descida recursiva PREDITIVA, sem backtracking."
            ),
            Predict(
                "E → E + T | T. O que acontece se E() começar chamando E()?",
                listOf("Funciona normal", "Laço infinito / estouro de pilha", "Erro de sintaxe", "Lê o +"), 1,
                "E() chama E() sem consumir token nenhum: chama de novo, e de novo… estouro de pilha. Recursão à esquerda é proibida aqui."
            ),
            Predict(
                "A → b A | ε. Com que tokens A() deve escolher o ε?",
                listOf("Com b", "Com os tokens de FOLLOW(A)", "Com qualquer token", "Nunca"), 1,
                "Se o token não abre b A mas pode vir depois de A, A some. É a mesma lógica da tabela LL(1)."
            ),
            Say(BYTE, "Então descida recursiva é a tabela LL(1) escrita em ifs?"),
            Say(PROF, "Exatamente! Cada 'when (look)' é uma linha da tabela. E a pilha? É a pilha de chamadas."),
            Predict(
                "A → x y | x z. A descida recursiva preditiva funciona direto?",
                listOf("Sim", "Não: precisa fatorar à esquerda (A → x A', A' → y | z)", "Sim, se usar FOLLOW", "Não: precisa eliminar ε"), 1,
                "O mesmo token x abre as duas alternativas: sem fatorar, só com backtracking."
            ),
            Visual(LEFT_FACTORING, P2_FACT, caption = "Veja a fatoração deixando a gramática pronta para virar funções."),
            Reveal("Resuma com suas palavras como construir uma descida recursiva.", "Uma função por não-terminal; o lookahead escolhe a produção (FIRST, ou FOLLOW para a alternativa ε); no corpo, match para terminais e chamada para não-terminais; a gramática precisa ser LL(1) (sem recursão à esquerda e fatorada)."),
            Visual(PARSE_TREE, P2_CMD, "if id then print id", "Treino: imagine S() → match(if), E(), match(then), S()… e confira com a árvore."),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.FORMAL, "Descida recursiva: definição de prova",
        listOf(
            Say(PROF, "Definição de prova, no estilo do Livro do Dragão."),
            Text("Um **analisador de descida recursiva** é um conjunto de procedimentos, um para cada não-terminal. A execução começa pelo procedimento do símbolo inicial e tem sucesso se ele termina tendo consumido toda a entrada."),
            Code(
                """
                void A() {
                  escolha uma produção A → X1 X2 … Xk     // pelo lookahead
                  para i := 1 até k:
                    se Xi é não-terminal: chame Xi()
                    senão se Xi = símbolo de entrada atual: avance a entrada
                    senão: erro()
                }
                """.trimIndent(),
                "Procedimento típico para um não-terminal A."
            ),
            Text("Na forma geral, a escolha da produção pode exigir **retrocesso** (backtracking). Na forma **preditiva**, o lookahead decide: usa-se A → α se look ∈ FIRST(α), ou se α ⇒* ε e look ∈ FOLLOW(A). Isso exige uma gramática **LL(1)**."),
            Steps(
                "Requisitos da descida recursiva preditiva",
                listOf(
                    "Sem recursão à esquerda (direta ou indireta): senão, laço infinito.",
                    "Fatorada à esquerda: senão, um token não decide.",
                    "Condições LL(1) satisfeitas: alternativas com conjuntos de escolha disjuntos.",
                    "Se nenhuma alternativa serve para o lookahead: erro (a menos que haja alternativa ε e look ∈ FOLLOW(A)).",
                )
            ),
            Predict(
                "Qual a relação entre a descida recursiva preditiva e o analisador preditivo com tabela?",
                listOf("Reconhecem classes diferentes", "Mesma classe (LL(1)); a pilha explícita vira a pilha de chamadas", "A descida recursiva é bottom-up", "O de tabela não precisa de FIRST"), 1,
                "São duas implementações da mesma ideia: a tabela vira os ifs e a pilha vira a recursão."
            ),
            Code(P2_CMD, "Exercício típico: escreva S(), L() e E()."),
            Reveal("Escreva L() para L → S L | ε.", "L() { se look ∈ {if, print, begin}: S(); L()   senão se look = end: retorna (ε)   senão: erro() }"),
            Visual(FIRST_FOLLOW, P2_CMD, caption = "Os conjuntos que decidem cada if: FIRST(S) = {if, print, begin} e FOLLOW(L) = {end}."),
            Callout(EXAM_TIP, "Na prova: (1) elimine recursão à esquerda e fatore; (2) calcule FIRST/FOLLOW; (3) uma função por não-terminal com um 'switch' no lookahead; (4) não esqueça o match e a checagem final do \$ (principal: S(); se look ≠ \$, erro)."),
            Reveal("Por que a recursão à DIREITA (L → S L) não causa problema?", "Porque antes de chamar L() de novo, S() consome pelo menos um token. A entrada diminui a cada chamada, então a recursão termina."),
        )
    ),
    Lesson(
        Skill.RECURSIVE_DESCENT, Style.MNEMONIC, "Uma variável, uma função",
        listOf(
            Say(NARRADOR, "Último corredor do Templo Top-Down: uma fileira de portas, cada uma com o nome de um não-terminal. Atrás de cada porta, um funcionário que pode abrir outras portas."),
            Callout(MNEMONIC, "**'Uma variável, uma função. Um terminal, um match.'**"),
            Callout(MNEMONIC, "**Recursão à esquerda = cachorro correndo atrás do rabo.** E() chama E() que chama E()… e nunca sai do lugar, porque não come nenhum token."),
            Callout(MNEMONIC, "**'Olha a senha antes de atender.'** O lookahead é a senha do cliente: o FIRST escolhe o roteiro; o FOLLOW autoriza o 'não faço nada' (ε)."),
            Say(LEXI, "Eu entrego os tokens já cortadinhos. A função só precisa espiar o próximo: nada de reler texto!"),
            Say(BYTE, "E qual a diferença pra pilha do golem?"),
            Callout(FIRESIDE, "Golem Pilha e Descida Recursiva ao pé da lareira. Pilha: 'Eu guardo os não-terminais pendentes numa pilha de pedra.' Descida: 'E eu na pilha de chamadas. Somos gêmeos, só muda a roupa.' Pilha: 'Mas você dá mensagens de erro mais bonitas.' Descida: 'Por isso o Clang me contratou.'"),
            Steps(
                "Confusões clássicas",
                listOf(
                    "Descida recursiva NÃO é bottom-up: é top-down, começa pela raiz (S()).",
                    "Recursão à DIREITA é ok; à ESQUERDA, não.",
                    "Esquecer de checar \$ no final: S() pode retornar com tokens sobrando.",
                    "Escolher ε 'por padrão' sem checar FOLLOW: funciona, mas o erro é detectado mais tarde.",
                )
            ),
            Predict(
                "Qual gramática NÃO serve para descida recursiva preditiva sem transformar?",
                listOf("E → T E'", "L → S L | ε", "A → A b | c", "R → , id R | ε"), 2,
                "A → A b | c é recursiva à esquerda: A() chamaria A() sem consumir nada."
            ),
            Visual(PARSE_TREE, P2_DECL, "float id ;", "Cada nó interno = uma porta aberta (uma função chamada). Conte: D, T, L, R."),
            Reveal("Recite os dois ganchos principais.", "'Uma variável, uma função; um terminal, um match.' e 'Recursão à esquerda = cachorro correndo atrás do rabo.'"),
        )
    ),
)

// ───────────────────────────── EMPILHA-REDUZ ─────────────────────────────

internal val p2ShiftReduceLessons: List<Lesson> = listOf(
    Lesson(
        Skill.SHIFT_REDUCE, Style.STORY, "Montando Lego de baixo para cima",
        listOf(
            Say(NARRADOR, "Bem-vindo à Fortaleza Bottom-Up! Aqui ninguém prevê nada: a gente constrói de baixo para cima."),
            Say(PROF, "Imagine montar um Lego seguindo o manual de trás pra frente. Você pega as pecinhas uma a uma (**empilhar**, shift). Quando as peças do topo formam um 'kit' do manual, você as encaixa num bloco maior (**reduzir**, reduce)."),
            Code(P2_EXPR, "O 'manual'. Repare: tem recursão à esquerda, e aqui ninguém se importa!"),
            Say(BYTE, "Então eu empilho 'id' e já reduzo, porque F → id?"),
            Say(PROF, "Isso! O 'id' no topo vira F. E esse F vira T (T → F). Cada redução é um passo de derivação feito ao contrário."),
            Predict(
                "Pilha: \$ T. Próximo token: *. Reduzir T para E (E → T) ou empilhar o *?",
                listOf("Reduzir para E", "Empilhar o *", "Aceitar", "Tanto faz"), 1,
                "Se virasse E, teríamos 'E *', e nenhuma forma sentencial tem E seguido de *. O kit certo é T * F: empilhamos e esperamos. Saber QUANDO reduzir é a arte toda."
            ),
            Text("O kit certo, no lugar certo, tem nome: **handle** (alça). Reduzir o handle é dar um passo para trás numa **derivação mais à direita**."),
            Visual(LR_TRACE, P2_EXPR, "id * id", "Assista à montagem: empilha, reduz, reduz, empilha, empilha, reduz, reduz, reduz, aceita."),
            Callout(NO_DUMB_QUESTIONS, "P: Quem me diz qual kit é o handle? R: Uma tabela construída com itens LR (as próximas salas da Fortaleza). Por enquanto, confie no manual e na intuição."),
            Say(PILHA, "Aqui… eu… guardo… o PASSADO. O que você já… viu."),
            Reveal("Quais são as 4 ações de um analisador empilha-reduz?", "Empilhar (shift), reduzir (reduce), aceitar (accept) e erro (error)."),
            Callout(REAL_WORLD, "Yacc e Bison geram analisadores empilha-reduz (LALR). O PostgreSQL, por exemplo, usa Bison para analisar SQL."),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.VISUAL, "Empilha-reduz em câmera lenta",
        listOf(
            Say(PROF, "Vamos ver um analisador empilha-reduz completo, com estados e tudo."),
            Code(P2_EXPR),
            Visual(LR_TRACE, P2_EXPR, "id + id * id", "Avance passo a passo. Repare quando ele reduz e quando ele espera."),
            Steps(
                "Momentos-chave",
                listOf(
                    "Empilha id e reduz F → id, T → F, E → T: o primeiro operando já virou E.",
                    "Empilha + e o segundo id; reduz F → id e T → F.",
                    "Agora o topo é E + T e vem '*': ele EMPILHA em vez de reduzir E → E + T. A multiplicação precisa ser montada antes.",
                    "Monta T * F, reduz T → T * F e só então E → E + T.",
                    "Pilha com E e entrada \$: aceita.",
                )
            ),
            Predict(
                "Com 'E + T' no topo e '*' na entrada, por que NÃO reduzir E → E + T?",
                listOf("Porque T * F ainda precisa ser montado (o * tem precedência)", "Porque a pilha está cheia", "Porque E + T não é produção", "Porque é proibido reduzir com * na entrada"), 0,
                "Se reduzisse, ficaria E * …, que não existe na gramática. O analisador espera montar T * F primeiro: a precedência sai naturalmente da gramática."
            ),
            Visual(PARSE_TREE, P2_EXPR, "id + id * id", "A árvore montada de baixo para cima: as folhas surgem primeiro, a raiz E por último."),
            Say(BYTE, "Então o trace, lido de trás pra frente, é uma derivação?"),
            Say(PROF, "Isso! As reduções, lidas ao contrário, formam a **derivação mais à direita**. Bottom-up = derivação mais à direita de trás pra frente."),
            Reveal("Na entrada 'id * id', qual é a ÚLTIMA redução antes de aceitar?", "E → T. A raiz da árvore é sempre a última peça encaixada."),
            Callout(BRAIN_POWER, "O analisador nunca precisa procurar o handle no MEIO da pilha: ele sempre aparece no topo. Por que isso facilita tanto a vida?"),
            Reveal("Responda o Poder do Cérebro.", "Porque basta olhar o topo da pilha (e o próximo token) para decidir. É isso que permite guiar as decisões com uma pilha simples e um autômato finito."),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.WORKED, "Exemplo resolvido: id * id",
        listOf(
            Say(PROF, "Exercício resolvido: análise empilha-reduz de id * id na gramática de expressões. Formato: Pilha | Entrada | Ação."),
            Code(P2_EXPR),
            Steps(
                "Resolução",
                listOf(
                    "\$ | id * id \$ | empilhar",
                    "\$ id | * id \$ | reduzir F → id",
                    "\$ F | * id \$ | reduzir T → F",
                    "\$ T | * id \$ | empilhar (não reduza E → T: * não pode seguir E)",
                    "\$ T * | id \$ | empilhar",
                    "\$ T * id | \$ | reduzir F → id",
                    "\$ T * F | \$ | reduzir T → T * F",
                    "\$ T | \$ | reduzir E → T",
                    "\$ E | \$ | aceitar",
                )
            ),
            Visual(LR_TRACE, P2_EXPR, "id * id", "Confira com o motor (ele mostra também os números dos estados)."),
            Say(BYTE, "No passo 4 eu teria reduzido pra E. Tô ferrado?"),
            Say(PROF, "De jeito nenhum: é O erro mais comum, e é exatamente por isso que existem as tabelas LR. A pista aqui: '*' não está em FOLLOW(E) = {+, ), \$}."),
            Say(PROF, "Agora o desbotado. Gramática S → S a | b, entrada b a a."),
            Code(P2_LIST),
            Text("Eu começo: \$ | b a a \$ | empilhar. Depois \$ b | a a \$ | reduzir S → b. Depois \$ S | a a \$ | empilhar."),
            Reveal("Pilha \$ S a, entrada a \$. Ação?", "Reduzir S → S a. A pilha vira \$ S."),
            Reveal("Pilha \$ S, entrada a \$. E agora?", "Empilhar a. Depois, com \$ S a e entrada \$, reduzir S → S a."),
            Reveal("E o final?", "Pilha \$ S e entrada \$: aceitar."),
            Visual(LR_TRACE, P2_LIST, "b a a", "Confira: a recursão à esquerda, proibida no Templo, aqui é bem-vinda."),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.SOCRATIC, "Descobrindo o que é um handle",
        listOf(
            Say(PROF, "Perguntas para você descobrir sozinho o que é um handle."),
            Code(P2_HANDLE, "Exemplo clássico do Livro do Dragão, com a frase a b b c d e."),
            Predict(
                "Pilha: \$ a b. Entrada: b c d e \$. O 'b' no topo casa com A → b. Reduzir?",
                listOf("Sim", "Não"), 0,
                "Sim: chegamos a a A b c d e, que leva a S. Aqui esse b é o handle."
            ),
            Predict(
                "Agora a pilha é \$ a A b e a entrada c d e \$. O 'b' no topo casa com A → b de novo. Reduzir?",
                listOf("Sim, b é sempre handle", "Não: o certo é empilhar c e reduzir A b c para A"), 1,
                "Reduzir daria a A A c d e, que não leva a S. O handle aqui é A b c. Nem toda ocorrência de um lado direito é um handle!"
            ),
            Say(BYTE, "Então handle não é só 'achei um lado direito'…"),
            Reveal("Tente definir handle com suas palavras.", "É uma ocorrência de um lado direito que, reduzida, nos leva um passo para trás numa derivação mais à DIREITA até o símbolo inicial: a redução 'certa'."),
            Predict(
                "Em que posição da pilha o handle aparece?",
                listOf("Sempre no topo", "Sempre no fundo", "Em qualquer lugar", "No meio"), 0,
                "Resultado importante: numa análise empilha-reduz o handle sempre aparece no topo da pilha, nunca escondido no meio."
            ),
            Predict(
                "Se em algum momento o analisador não sabe se deve empilhar ou reduzir, temos um…",
                listOf("Conflito empilha-reduz (shift/reduce)", "Conflito reduz-reduz", "Erro léxico", "Aceite"), 0,
                "Conflito shift/reduce. Se ele não souber QUAL redução fazer, é reduce/reduce."
            ),
            Visual(LR_TRACE, P2_HANDLE, "a b b c d e", "Veja o analisador acertando o handle em cada passo."),
            Reveal("Escreva a sequência de reduções da análise de a b b c d e.", "A → b, A → A b c, B → d, S → a A B e. Lida ao contrário, é a derivação mais à direita: S ⇒ a A B e ⇒ a A d e ⇒ a A b c d e ⇒ a b b c d e."),
            Say(PROF, "Você reconstruiu sozinho a ideia central do bottom-up. Achar handles é o que o resto da Fortaleza ensina a automatizar."),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.FORMAL, "Empilha-reduz: definições de prova",
        listOf(
            Say(PROF, "Definições de prova, sem enrolação."),
            Text("**Análise empilha-reduz**: análise ascendente (bottom-up) com uma pilha de símbolos gramaticais e um buffer de entrada. Configuração inicial: pilha \$, entrada w\$. Sucesso: pilha \$ S, entrada \$."),
            Steps(
                "As quatro ações",
                listOf(
                    "**Empilhar (shift)**: move o próximo símbolo da entrada para o topo da pilha.",
                    "**Reduzir (reduce)**: o topo contém um lado direito β (o handle); desempilha β e empilha A, para A → β.",
                    "**Aceitar (accept)**: anuncia o término com sucesso.",
                    "**Erro (error)**: detecta um erro de sintaxe e chama a rotina de recuperação.",
                )
            ),
            Text("**Handle.** Se S ⇒*rm α A w ⇒rm α β w, então a produção A → β na posição logo após α é um handle de α β w. (w só tem terminais; 'rm' = mais à direita.)"),
            Text("**Prefixo viável**: prefixo de uma forma sentencial direita que não ultrapassa o fim do handle. O conteúdo da pilha é sempre um prefixo viável."),
            Predict(
                "A sequência de reduções de um analisador empilha-reduz, lida de trás para frente, corresponde a:",
                listOf("Uma derivação mais à esquerda", "Uma derivação mais à direita", "Uma árvore ambígua", "A tabela LL(1)"), 1,
                "Bottom-up = derivação mais à direita (rightmost) ao contrário. É o R de LR."
            ),
            Steps(
                "Conflitos",
                listOf(
                    "**shift/reduce**: não dá para decidir entre empilhar e reduzir (ex.: o else pendurado).",
                    "**reduce/reduce**: não dá para decidir qual redução fazer.",
                    "Gramáticas com conflito para um método não pertencem à classe dele (ex.: não são SLR(1)). Gramáticas ambíguas sempre geram conflito.",
                )
            ),
            Visual(LR_TRACE, P2_EXPR, "( id )", "Siga as definições na análise de ( id )."),
            Callout(EXAM_TIP, "A resposta-padrão é a tabela Pilha | Entrada | Ação, com a produção usada em cada 'reduzir'. E saiba de cor: o handle está sempre no topo; LR = Left-to-right, Rightmost derivation (ao contrário)."),
            Reveal("Defina handle em uma frase.", "Uma subcadeia que casa com o lado direito de uma produção e cuja redução representa um passo de volta numa derivação mais à direita."),
            Callout(FIRESIDE, "LL vs LR ao pé da lareira. LL: 'Eu escolho a produção olhando só o primeiro token dela.' LR: 'Eu vejo o lado direito INTEIRO e mais um token antes de decidir. Sou paciente, e por isso aceito mais gramáticas, inclusive as recursivas à esquerda.' LL: 'Mas eu sou fácil de escrever à mão!' LR: 'Por isso inventaram o Bison pra mim.'"),
        )
    ),
    Lesson(
        Skill.SHIFT_REDUCE, Style.MNEMONIC, "Empilha até ver a alça",
        listOf(
            Say(NARRADOR, "Portão da Fortaleza Bottom-Up: uma esteira de fábrica leva caixinhas (tokens) até um montador."),
            Callout(MNEMONIC, "**'Empilha até ver a alça; achou a alça no topo, reduz.'** Handle = alça: o pedaço por onde você pega e dobra tudo para o lado esquerdo da regra."),
            Callout(MNEMONIC, "**LR = 'Lê da esquerda, Reconstrói a mais à direita… de trás pra frente.'** Bottom-up = rightmost ao contrário."),
            Callout(MNEMONIC, "**Reduzir = 'tira o lado direito, põe o lado esquerdo'.** Como trocar um punhado de moedinhas por uma nota."),
            Steps(
                "Top-down × bottom-up",
                listOf(
                    "Top-down (Templo): começa pela raiz S e prevê. Bottom-up (Fortaleza): começa pelas folhas e monta.",
                    "LL: derivação mais à esquerda, na ordem. LR: derivação mais à direita, invertida.",
                    "Pilha LL: o que falta (futuro). Pilha LR: o que já foi visto (passado).",
                    "Recursão à esquerda: proibida no LL, tranquila no LR.",
                )
            ),
            Say(BYTE, "E 'shift' é empilhar ou é deslocar?"),
            Say(PROF, "Os dois! O token se desloca da entrada para a pilha. No português do Livro do Dragão: **empilhar** (shift) e **reduzir** (reduce)."),
            Predict(
                "Qual ação consome um token da entrada?",
                listOf("Reduzir", "Empilhar", "Aceitar", "Todas"), 1,
                "Só o shift consome entrada. Reduzir só mexe na pilha (troca β por A)."
            ),
            Visual(LR_TRACE, P2_ANBN, "a a b b", "Treine o gancho: onde está a alça? Repare na redução S → ε, que empilha S sem desempilhar nada."),
            Reveal("Recite os três ganchos.", "'Empilha até ver a alça; achou no topo, reduz.' 'LR = rightmost de trás pra frente.' 'Reduzir = tira o direito, põe o esquerdo.'"),
        )
    ),
)

// ───────────────────────────── ITENS LR(0) ─────────────────────────────

internal val p2Lr0Lessons: List<Lesson> = listOf(
    Lesson(
        Skill.LR0_ITEMS, Style.STORY, "O marcador de página",
        listOf(
            Say(NARRADOR, "Torre central da Fortaleza Bottom-Up. Cada andar é um 'estado' cheio de marcadores de página."),
            Say(PROF, "Um **item LR(0)** é uma produção com um ponto • no meio: um marcador de onde você está na leitura. C → c • C quer dizer: 'já vi o c, agora espero um C'."),
            Code(P2_CC, "Nosso livro de receitas. Gera cadeias como d d, c d d, c c d c d…"),
            Say(BYTE, "Tipo a barra de progresso do download?"),
            Say(PROF, "Isso! Ponto no começo (C → • c C): 0%. Ponto no fim (C → c C •): 100%, hora de **reduzir**."),
            Predict(
                "Quantos itens LR(0) a produção S → C C gera?",
                listOf("1", "2", "3", "4"), 2,
                "S → • C C, S → C • C e S → C C •. Uma produção com n símbolos dá n + 1 itens."
            ),
            Say(PROF, "Agora a mágica do **fechamento (closure)**. Se eu espero um C (ponto antes de C), então posso estar no começo de qualquer receita de C. Eu 'convido' C → • c C e C → • d."),
            Callout(MNEMONIC, "Fechamento = **convite em cascata**: o não-terminal logo depois do ponto convida as próprias produções, com o ponto no começo. Os convidados também convidam os seus."),
            Say(PROF, "E o **desvio (goto)**: andar o ponto por cima de um símbolo. goto(I, c) = pegue os itens com • antes de c, passe o ponto para depois do c e feche de novo."),
            Visual(LR0_AUTOMATON, P2_CC, caption = "O prédio inteiro: 7 andares (estados I0 a I6) e as escadas (transições) entre eles."),
            Reveal("No estado I0, quais itens existem? (Dica: comece por S' → • S e vá convidando.)", "S' → • S, S → • C C, C → • c C, C → • d. O S convidou S → • C C; o C convidou as duas produções de C."),
            Callout(NO_DUMB_QUESTIONS, "P: Que S' é esse? R: É a **gramática aumentada**: criamos S' → S para ter um único item de aceitação, S' → S •."),
            Say(BYTE, "Marcador, convite, passo. Itens, closure, goto. Gostei!"),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.VISUAL, "Explorando o autômato LR(0)",
        listOf(
            Say(PROF, "Vamos explorar o autômato LR(0) do jeito que se explora um mapa."),
            Code(P2_CC),
            Visual(LR0_AUTOMATON, P2_CC, caption = "Toque em cada estado para ver seus itens. As setas são os desvios (goto)."),
            Steps(
                "Roteiro de exploração",
                listOf(
                    "I0: o estado inicial, fechamento de { S' → • S }.",
                    "I0 --S--> I1 = { S' → S • }: o estado de aceitação.",
                    "I0 --C--> I2 = { S → C • C, C → • c C, C → • d }: o ponto passou pelo primeiro C, e o segundo C convidou suas produções.",
                    "I0 --c--> I3 = { C → c • C, C → • c C, C → • d }. Repare em I3 --c--> I3: um laço!",
                    "I0 --d--> I4 = { C → d • }: item completo → redução.",
                    "I5 = { S → C C • } e I6 = { C → c C • }: mais dois estados de redução.",
                )
            ),
            Predict(
                "De I2, lendo d, vamos para qual estado?",
                listOf("Um estado novo", "I4", "I3", "I6"), 1,
                "goto(I2, d) = { C → d • } = I4. Se o conjunto de itens já existe, reaproveitamos o estado: por isso o autômato é finito."
            ),
            Say(BYTE, "Por que de I3, lendo c, volta pro I3?"),
            Say(PROF, "Porque goto(I3, c) dá C → c • C de novo, e o fechamento repete os mesmos convidados. Mesmo conjunto = mesmo estado. É o autômato 'contando' os c's sem precisar de memória extra."),
            Visual(LR0_AUTOMATON, P2_EXPR, caption = "Agora uma gramática maior: 12 estados (I0 a I11). Procure o estado que tem E → T • e T → T • * F juntos."),
            Reveal("Que problema esse estado (E → T • junto com T → T • * F) causaria se reduzíssemos sempre que um item está completo?", "Conflito empilha-reduz no '*': o item completo manda reduzir E → T e o outro manda empilhar *. O LR(0) puro não resolve; o SLR(1) usa FOLLOW(E) = {+, ), \$} para decidir (próxima sala)."),
            Callout(BRAIN_POWER, "O autômato LR(0) reconhece exatamente os **prefixos viáveis** da gramática. Olhe os caminhos a partir de I0: cada caminho soletra algo que pode estar na pilha."),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.WORKED, "Exemplo resolvido: a coleção canônica de S → C C",
        listOf(
            Say(PROF, "Exercício de prova: construa a coleção canônica LR(0) de S → C C, C → c C | d."),
            Code(P2_CC),
            Steps(
                "Resolução",
                listOf(
                    "Passo 0, aumente a gramática: 0) S' → S, 1) S → C C, 2) C → c C, 3) C → d.",
                    "I0 = closure({ S' → • S }) = { S' → • S, S → • C C, C → • c C, C → • d }.",
                    "goto(I0, S) = { S' → S • } = I1.",
                    "goto(I0, C) = closure({ S → C • C }) = { S → C • C, C → • c C, C → • d } = I2.",
                    "goto(I0, c) = closure({ C → c • C }) = { C → c • C, C → • c C, C → • d } = I3.",
                    "goto(I0, d) = { C → d • } = I4.",
                    "goto(I2, C) = { S → C C • } = I5. goto(I2, c) = I3 e goto(I2, d) = I4: já existem!",
                )
            ),
            Visual(LR0_AUTOMATON, P2_CC, caption = "Confira os estados I0 a I5 com o motor."),
            Say(BYTE, "Falta o I3, né?"),
            Say(PROF, "Falta! Essa parte é sua. Lembre: goto = passar o ponto e depois fechar."),
            Reveal("goto(I3, C) = ?", "{ C → c C • }: um estado novo, I6."),
            Reveal("goto(I3, c) = ?", "closure({ C → c • C }) = { C → c • C, C → • c C, C → • d }: o próprio I3."),
            Reveal("goto(I3, d) = ?", "{ C → d • } = I4."),
            Reveal("Quantos estados ao todo, e quais são de redução?", "7 estados (I0 a I6). Itens completos: I1 (aceita), I4 (C → d), I5 (S → C C), I6 (C → c C). Nenhum estado mistura item completo com outro item: a gramática é LR(0)!"),
            Callout(EXAM_TIP, "Numere as produções da gramática aumentada, desenhe cada estado como uma caixinha com o núcleo (kernel) separado dos itens do fechamento e, antes de criar um estado novo, verifique se aquele conjunto já existe."),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.SOCRATIC, "Por que itens, closure e goto?",
        listOf(
            Say(PROF, "Vamos redescobrir itens, closure e goto só com perguntas."),
            Predict(
                "Num analisador empilha-reduz, o que precisamos saber a cada momento?",
                listOf("Só o próximo token", "Em que ponto das produções candidatas estamos", "A árvore inteira", "O FIRST de tudo"), 1,
                "Para saber se o handle já está completo no topo, precisamos saber 'onde estamos' em cada produção candidata. Isso é um item."
            ),
            Predict(
                "O item A → X Y • Z significa:",
                listOf("Já reduzi A", "Já vi X Y; espero algo derivado de Z", "Espero X Y Z", "Erro"), 1,
                "O ponto separa o passado (o que está na pilha) do futuro (o que vem na entrada)."
            ),
            Predict(
                "Se espero um Z (item A → X Y • Z) e Z → w | v, o que mais posso estar esperando?",
                listOf("Nada", "Z → • w e Z → • v", "Z → w • e Z → v •", "A → • X Y Z"), 1,
                "Esperar um Z é esperar o começo de alguma produção de Z. Isso é o fechamento."
            ),
            Predict(
                "Se o item é A → X • b Y, com b terminal, quantos itens o fechamento acrescenta por causa dele?",
                listOf("Todos os de b", "Nenhum: terminal não tem produções", "Um", "Dois"), 1,
                "O fechamento só convida quando há um NÃO-terminal logo depois do ponto."
            ),
            Predict(
                "Estou no conjunto de itens I e empilho o símbolo X. O novo conjunto é…",
                listOf("I de novo", "Os itens com • antes de X, com o ponto avançado, e depois fechados", "Todos os itens que contêm X", "Vazio"), 1,
                "Isso é goto(I, X): avança o ponto sobre X e aplica o fechamento."
            ),
            Reveal("Por que o número de estados é finito?", "Há um número finito de itens (produções × posições do ponto), logo um número finito de conjuntos de itens. Conjuntos repetidos viram o mesmo estado."),
            Predict(
                "Para que serve a produção S' → S da gramática aumentada?",
                listOf("Enfeite", "Ter um item de aceitação único: S' → S •", "Evitar recursão à esquerda", "Calcular FOLLOW"), 1,
                "Quando o analisador chegaria a reduzir para S' (item S' → S • com \$ na entrada), ele aceita."
            ),
            Visual(LR0_AUTOMATON, P2_LIST, caption = "Teste: S → S a | b. Antes de olhar, quantos itens terá I0?"),
            Reveal("Quais são os itens de I0?", "3: S' → • S, S → • S a, S → • b. O S convidou suas duas produções; o S de S → • S a já foi convidado, então nada novo."),
            Say(PROF, "Recursão à esquerda no closure? Sem drama: o conjunto não cresce para sempre, porque item repetido não entra de novo."),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.FORMAL, "Itens, CLOSURE, GOTO e coleção canônica (versão prova)",
        listOf(
            Say(PROF, "Definições formais, como no Livro do Dragão."),
            Text("**Item LR(0)**: produção de G com um ponto em alguma posição do lado direito. A → X Y Z gera A → • X Y Z, A → X • Y Z, A → X Y • Z e A → X Y Z •. A produção A → ε gera só A → •."),
            Text("**Gramática aumentada** G': G com um novo símbolo inicial S' e a produção S' → S. O analisador aceita quando reduziria por S' → S."),
            Code(
                """
                CLOSURE(I):
                  J := I
                  repita
                    para cada item A → α • B β em J:
                      para cada produção B → γ de G':
                        se B → • γ não está em J:
                          adicione B → • γ a J
                  até nenhum item ser adicionado
                  retorne J
                """.trimIndent(),
                "Fechamento de um conjunto de itens."
            ),
            Code(
                "GOTO(I, X) = CLOSURE({ A → α X • β  |  A → α • X β ∈ I })",
                "Desvio: avança o ponto sobre X e fecha."
            ),
            Code(
                """
                ITENS(G'):
                  C := { CLOSURE({ S' → • S }) }
                  repita
                    para cada conjunto I em C:
                      para cada símbolo gramatical X:
                        se GOTO(I, X) ≠ ∅ e GOTO(I, X) ∉ C:
                          adicione GOTO(I, X) a C
                  até nenhum conjunto ser adicionado
                """.trimIndent(),
                "Coleção canônica de conjuntos de itens LR(0)."
            ),
            Text("**Itens de núcleo (kernel)**: S' → • S e todos os itens com o ponto fora da extremidade esquerda. Os demais são **não-núcleo** e podem ser recalculados pelo fechamento."),
            Predict(
                "Qual destes é item de núcleo?",
                listOf("C → • c C", "S → • C C", "C → c • C", "C → • d"), 2,
                "C → c • C tem o ponto depois do início. Os outros têm o ponto na extremidade esquerda (e não são S' → • S): vieram do fechamento."
            ),
            Visual(LR0_AUTOMATON, P2_CC, caption = "Identifique, em cada estado, quais itens são de núcleo."),
            Callout(EXAM_TIP, "Pergunta típica: 'calcule CLOSURE({…})' ou 'GOTO(I, X)'. Não esqueça: GOTO = avançar o ponto **e depois** fechar. E A → ε gera só o item A → •, que já nasce completo."),
            Reveal("Qual a relação entre o autômato LR(0) e os prefixos viáveis?", "O autômato reconhece exatamente os prefixos viáveis: γ é viável se e somente se existe o caminho GOTO(I0, γ) a partir do estado inicial."),
        )
    ),
    Lesson(
        Skill.LR0_ITEMS, Style.MNEMONIC, "Você está aqui •",
        listOf(
            Say(NARRADOR, "Torre da Fortaleza Bottom-Up: num dos andares há um mapa de shopping com um ponto vermelho, **VOCÊ ESTÁ AQUI**."),
            Callout(MNEMONIC, "**O ponto • é o 'você está aqui'.** À esquerda dele: o que já está na pilha (passado). À direita: o que falta ler (futuro)."),
            Callout(MNEMONIC, "**CLOSURE = 'convida a família'.** Ponto antes de um não-terminal B? Chama todas as produções de B, com o ponto no começo. A família de B convida as próprias famílias."),
            Callout(MNEMONIC, "**GOTO = 'gol': chuta o ponto um passo pra frente e depois comemora com a família (closure).**"),
            Steps(
                "Confusões clássicas",
                listOf(
                    "O closure só adiciona itens com o ponto NO COMEÇO (B → • γ).",
                    "O goto avança o ponto só nos itens com • antes de X; os outros ficam de fora.",
                    "Esquecer de fechar depois do goto.",
                    "Criar estado repetido: conjunto igual = mesmo estado.",
                    "Item completo (ponto no fim) = hora de reduzir; não sai goto dele.",
                )
            ),
            Predict(
                "Byte calculou goto(I0, c) = { C → c • C } e parou. O que faltou?",
                listOf("Nada", "Fechar: C → • c C e C → • d", "Adicionar S → • C C", "Remover o ponto"), 1,
                "Goto = avançar + fechar. Como o ponto ficou antes de C, a família de C precisa ser convidada."
            ),
            Callout(FIRESIDE, "Item e Estado ao pé da lareira. Item: 'Eu sou só um marcador numa receita.' Estado: 'E eu sou o grupo de marcadores que valem ao mesmo tempo: todas as receitas que podem estar em andamento agora.' Item: 'Então você é a minha turma?' Estado: 'Sou. E o fechamento é como eu chamo a turma toda.'"),
            Visual(LR0_AUTOMATON, P2_ANBN, caption = "Treino rápido em S → a S b | ε: ache o estado em que o 'você está aqui' aparece antes do S e conte os convidados."),
            Reveal("Recite os três ganchos.", "Ponto = 'você está aqui'. Closure = 'convida a família'. Goto = 'chuta o ponto um passo e comemora com a família'."),
        )
    ),
)
