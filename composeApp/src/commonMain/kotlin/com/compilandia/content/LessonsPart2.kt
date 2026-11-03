package com.compilandia.content

import com.compilandia.learning.Skill

// Parte 2 do conteúdo: Templo Top-Down (FIRST, FOLLOW, LL(1), descida recursiva)
// e a entrada da Fortaleza Bottom-Up (empilha-reduz, itens LR(0)).
// As aulas estão em LessonsPart2a.kt, LessonsPart2b.kt e LessonsPart2c.kt.

/** Gramáticas usadas nas aulas da parte 2 (formato de Grammar.parse; "→" também é aceito). */
internal const val P2_EXPR_LL = "E → T E'\nE' → + T E' | ε\nT → F T'\nT' → * F T' | ε\nF → ( E ) | id"
internal const val P2_EXPR = "E → E + T | T\nT → T * F | F\nF → ( E ) | id"
internal const val P2_DECL = "D → T L ;\nT → int | float\nL → id R\nR → , id R | ε"
internal const val P2_ABC = "S → A B c\nA → a | ε\nB → b | ε"
internal const val P2_PARENS = "S → ( S ) S | ε"
internal const val P2_ANBN = "S → a S b | ε"
internal const val P2_FACT = "S → a b | a c"
internal const val P2_LIST = "S → S a | b"
internal const val P2_CC = "S → C C\nC → c C | d"
internal const val P2_CMD = "S → if E then S | print E | begin L end\nL → S L | ε\nE → id"
internal const val P2_HANDLE = "S → a A B e\nA → A b c | b\nB → d"

val lessonsPart2: List<Lesson> by lazy {
    p2FirstLessons + p2FollowLessons + p2Ll1TableLessons + p2Ll1ParsingLessons +
        p2RecDescentLessons + p2ShiftReduceLessons + p2Lr0Lessons
}

val cardsPart2: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.FIRST,
        oneLiner = "FIRST(α) é o conjunto de terminais que podem começar alguma cadeia derivada de α — mais ε, se α pode derivar a cadeia vazia.",
        hook = "FIRST = Fila: quem Inicia. Terminal na frente entra e pronto; variável na frente empresta o FIRST dela; se ela pode faltar, passa a vez pro vizinho.",
        feynmanPrompt = "Explique para um amigo que nunca viu gramática, como se fosse uma fila de bandejão, o que é FIRST de um não-terminal e quando o ε entra no conjunto.",
        keyTerms = listOf("terminal", "começ", "ε", "anulável", "não-terminal", "ponto fixo"),
        commonMistakes = listOf(
            "Continuar para o próximo símbolo mesmo quando o atual NÃO é anulável.",
            "Copiar o ε de FIRST(Y1) para FIRST(A) quando só Y1 (e não todo o lado direito) pode sumir.",
            "Colocar \$ dentro de um FIRST (\$ só existe no FOLLOW).",
            "Colocar um não-terminal dentro do conjunto (FIRST só tem terminais e talvez ε).",
            "Parar depois de uma única passada, sem repetir até o ponto fixo.",
        ),
        flashcards = listOf(
            "FIRST(a), se a é terminal?" to "{a}",
            "Quando ε ∈ FIRST(A)?" to "Quando A ⇒* ε (A é anulável).",
            "A → B c e B → b | ε. FIRST(A)?" to "{b, c}",
            "A → B C, B → x | ε, C → y | ε. FIRST(A)?" to "{x, y, ε}",
            "FIRST(E) na gramática E → T E' …?" to "{(, id}",
            "\$ pode estar em FIRST?" to "Nunca.",
            "Por que o algoritmo repete as voltas?" to "Conjuntos dependem uns dos outros; repete até nada mudar (ponto fixo).",
            "FIRST(A → A b | c)?" to "{c} — o cálculo não trava com recursão à esquerda.",
        ),
    ),
    SkillCard(
        skill = Skill.FOLLOW,
        oneLiner = "FOLLOW(A) é o conjunto de terminais que podem aparecer imediatamente à direita de A em alguma forma sentencial; \$ entra se A pode terminar a frase.",
        hook = "Dólar, Vizinho, Herança: \$ no inicial; o vizinho da direita empresta o FIRST (sem ε); quem fica no fim herda o FOLLOW do pai.",
        feynmanPrompt = "Explique para sua avó, como se fosse a fila do ônibus, quem pode vir logo depois de alguém — e por que o último de um grupo herda quem vem depois do grupo.",
        keyTerms = listOf("depois", "\$", "first", "herda", "inicial", "anulável"),
        commonMistakes = listOf(
            "Colocar ε em FOLLOW (FOLLOW nunca tem ε).",
            "Esquecer o \$ no FOLLOW do símbolo inicial.",
            "Inverter a herança: é FOLLOW(A) ⊆ FOLLOW(B) para A → α B, não o contrário.",
            "Esquecer a herança quando o resto β é anulável (não só quando B está no fim).",
            "Calcular FOLLOW antes de ter todos os FIRST prontos.",
        ),
        flashcards = listOf(
            "Regra 1 do FOLLOW?" to "\$ ∈ FOLLOW(símbolo inicial).",
            "Em A → α B β, o que entra em FOLLOW(B)?" to "FIRST(β) − {ε}",
            "Quando FOLLOW(A) entra em FOLLOW(B)?" to "Se A → α B, ou A → α B β com β ⇒* ε.",
            "FOLLOW pode conter ε?" to "Nunca.",
            "S → a S b | ε. FOLLOW(S)?" to "{b, \$}",
            "S → ( S ) S | ε. FOLLOW(S)?" to "{), \$}",
            "Na gramática de expressões LL(1), FOLLOW(F)?" to "{*, +, ), \$}",
            "Para que serve o FOLLOW na tabela LL(1)?" to "Decidir onde colocar as produções que derivam ε.",
        ),
    ),
    SkillCard(
        skill = Skill.LL1_TABLE,
        oneLiner = "A tabela preditiva M[A, a] diz qual produção de A usar quando o próximo token é a; a gramática é LL(1) se nenhuma célula tem duas produções.",
        hook = "Linha é Variável, Coluna é Token. Primeiro o FIRST; se o ε aparecer, chama o FOLLOW. Sudoku LL(1): uma célula, uma produção.",
        feynmanPrompt = "Explique para um colega de outro curso, como se fosse o caderninho de um porteiro, como cada produção vai parar em cada célula da tabela LL(1) e o que é um conflito.",
        keyTerms = listOf("first", "follow", "ε", "célula", "conflito", "lookahead"),
        commonMistakes = listOf(
            "Pôr A → ε em colunas de FIRST em vez das colunas de FOLLOW(A).",
            "Esquecer a coluna \$.",
            "Achar que produções ε impedem a gramática de ser LL(1).",
            "Achar que eliminar recursão à esquerda e fatorar sempre garante LL(1).",
            "Preencher célula por célula 'no olho' em vez de produção por produção.",
        ),
        flashcards = listOf(
            "Regra 1 da tabela?" to "Para cada a ∈ FIRST(α), A → α em M[A, a].",
            "Regra 2 da tabela?" to "Se ε ∈ FIRST(α), A → α em M[A, b] para cada b ∈ FOLLOW(A).",
            "Célula vazia significa?" to "Erro de sintaxe.",
            "Célula com duas produções significa?" to "Conflito: a gramática não é LL(1).",
            "O que significa LL(1)?" to "Left-to-right, Leftmost derivation, 1 token de lookahead.",
            "Gramática com recursão à esquerda pode ser LL(1)?" to "Não.",
            "Gramática ambígua pode ser LL(1)?" to "Não.",
            "M[E', )] na gramática de expressões LL(1)?" to "E' → ε",
        ),
    ),
    SkillCard(
        skill = Skill.LL1_PARSING,
        oneLiner = "O analisador preditivo não recursivo usa uma pilha (começando com \$ S) e a tabela M: terminal no topo casa com a entrada; não-terminal no topo é trocado pelo lado direito, invertido.",
        hook = "Terminal no topo? Casa e anda. Variável no topo? Consulta e troca. \$ com \$? Festa! (E arrume a mochila: o que usa primeiro vai por último.)",
        feynmanPrompt = "Explique para um amigo, como se a pilha fosse uma lista de tarefas, como o analisador LL(1) decide o que fazer a cada passo e quando ele aceita.",
        keyTerms = listOf("pilha", "topo", "tabela", "casa", "invertid", "\$"),
        commonMistakes = listOf(
            "Empilhar o lado direito na ordem normal (o primeiro símbolo tem que ficar no topo).",
            "Avançar a entrada numa expansão (só casar consome token).",
            "Empilhar ε (produção vazia só desempilha).",
            "Esquecer de começar a pilha com \$ S.",
            "Misturar o lado do topo da pilha no meio da simulação.",
        ),
        flashcards = listOf(
            "Configuração inicial?" to "Pilha \$ S; entrada w\$.",
            "Topo terminal igual ao token?" to "Desempilha e avança (casar).",
            "Topo não-terminal A, token a?" to "Troca A pelo lado direito de M[A, a], invertido.",
            "Quando aceita?" to "Topo \$ e entrada \$.",
            "M[A, a] vazia?" to "Erro de sintaxe.",
            "Que derivação a saída forma?" to "A derivação mais à esquerda.",
            "Expandir A → ε faz o quê na pilha?" to "Só desempilha A.",
            "O analisador preditivo faz backtracking?" to "Nunca.",
        ),
    ),
    SkillCard(
        skill = Skill.RECURSIVE_DESCENT,
        oneLiner = "Descida recursiva: uma função por não-terminal; o lookahead escolhe a produção, terminais viram match e não-terminais viram chamadas.",
        hook = "Uma variável, uma função; um terminal, um match. Recursão à esquerda = cachorro correndo atrás do rabo.",
        feynmanPrompt = "Explique para alguém que programa mas não estudou compiladores, como se fosse uma repartição com um funcionário por guichê, como escrever um analisador de descida recursiva.",
        keyTerms = listOf("função", "não-terminal", "lookahead", "match", "recursão à esquerda", "first"),
        commonMistakes = listOf(
            "Usar gramática com recursão à esquerda (laço infinito).",
            "Não fatorar à esquerda (um token não decide).",
            "Esquecer de verificar \$ no fim: S() pode retornar com tokens sobrando.",
            "Avançar o token dentro de uma chamada de não-terminal sem dar match.",
            "Achar que descida recursiva é bottom-up.",
        ),
        flashcards = listOf(
            "Quantas funções?" to "Uma por não-terminal.",
            "Terminal no lado direito vira?" to "match(terminal).",
            "Não-terminal no lado direito vira?" to "Chamada da função dele.",
            "Como escolher a produção?" to "Pelo lookahead: FIRST, ou FOLLOW para a alternativa ε.",
            "Onde fica a pilha?" to "Na pilha de chamadas do programa.",
            "Por que recursão à esquerda quebra?" to "A() chama A() sem consumir token: laço infinito.",
            "Recursão à direita é problema?" to "Não: cada chamada consome token antes.",
            "Que classe de gramáticas a forma preditiva aceita?" to "LL(1).",
        ),
    ),
    SkillCard(
        skill = Skill.SHIFT_REDUCE,
        oneLiner = "Análise empilha-reduz (bottom-up): empilha tokens até o handle aparecer no topo e então reduz para o lado esquerdo; as reduções formam a derivação mais à direita ao contrário.",
        hook = "Empilha até ver a alça; achou a alça no topo, reduz. LR = rightmost de trás pra frente.",
        feynmanPrompt = "Explique para um amigo, como se fosse montar Lego de baixo para cima, o que são as ações empilhar e reduzir e o que é um handle.",
        keyTerms = listOf("empilha", "reduz", "handle", "topo", "mais à direita", "conflito"),
        commonMistakes = listOf(
            "Reduzir qualquer ocorrência de um lado direito (nem toda ocorrência é handle).",
            "Achar que reduzir consome token da entrada.",
            "Procurar o handle no meio da pilha (ele está sempre no topo).",
            "Confundir: reduções do LR formam a derivação mais à DIREITA invertida, não a mais à esquerda.",
        ),
        flashcards = listOf(
            "As 4 ações?" to "Empilhar, reduzir, aceitar, erro.",
            "O que é handle?" to "Lado direito cuja redução é um passo atrás numa derivação mais à direita.",
            "Onde o handle aparece?" to "Sempre no topo da pilha.",
            "Configuração de aceitação?" to "Pilha \$ S, entrada \$.",
            "Reduzir por A → β faz o quê?" to "Desempilha β e empilha A.",
            "Conflito shift/reduce?" to "Não dá para decidir entre empilhar e reduzir.",
            "Conflito reduce/reduce?" to "Não dá para decidir qual produção reduzir.",
            "O que significa o R de LR?" to "Rightmost: derivação mais à direita (ao contrário).",
        ),
    ),
    SkillCard(
        skill = Skill.LR0_ITEMS,
        oneLiner = "Item LR(0) é uma produção com um ponto marcando o progresso; o fechamento acrescenta B → •γ quando o ponto está antes de B, e o desvio avança o ponto sobre um símbolo e fecha.",
        hook = "Ponto = 'você está aqui'. Closure = convida a família. Goto = chuta o ponto um passo e comemora com a família.",
        feynmanPrompt = "Explique para um colega, como se o ponto fosse a barra de progresso de um download, o que é um item LR(0) e como closure e goto constroem os estados.",
        keyTerms = listOf("ponto", "item", "closure", "goto", "estado", "aumentada"),
        commonMistakes = listOf(
            "Esquecer de aplicar o fechamento depois do goto.",
            "No fechamento, adicionar itens com o ponto fora do começo.",
            "Fechar sobre terminais (só não-terminais após o ponto convidam produções).",
            "Criar um estado novo para um conjunto de itens que já existe.",
            "Esquecer a gramática aumentada S' → S.",
        ),
        flashcards = listOf(
            "Quantos itens gera A → X Y Z?" to "4",
            "Itens de A → ε?" to "Só A → •",
            "CLOSURE de um item A → α • B β adiciona?" to "B → • γ para toda produção de B (e repete).",
            "GOTO(I, X)?" to "Avança o ponto sobre X nos itens de I e fecha.",
            "Para que S' → S?" to "Ter um único item de aceitação, S' → S •.",
            "Item completo (ponto no fim) indica?" to "Redução.",
            "Quantos estados LR(0) tem S → C C, C → c C | d?" to "7 (I0 a I6).",
            "Item de núcleo (kernel)?" to "S' → • S ou item com o ponto fora do início.",
        ),
    ),
)
