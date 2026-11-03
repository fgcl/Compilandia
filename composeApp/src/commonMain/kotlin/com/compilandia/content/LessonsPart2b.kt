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
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.LEFT_RECURSION
import com.compilandia.content.VisualKind.LL1_TABLE
import com.compilandia.content.VisualKind.LL1_TRACE
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// ───────────────────────────── TABELA LL(1) ─────────────────────────────

internal val p2Ll1TableLessons: List<Lesson> = listOf(
    Lesson(
        Skill.LL1_TABLE, Style.STORY, "O caderninho do porteiro",
        listOf(
            Say(NARRADOR, "Terceira sala do Templo Top-Down: um porteiro de balada com um caderninho enorme na mão."),
            Say(PROF, "O porteiro sabe em que 'parte da festa' você está (o não-terminal A) e vê quem está chegando (o próximo token a). O caderninho diz qual porta abrir: **M[A, a] = qual produção usar**."),
            Say(BYTE, "E como ele preencheu esse caderninho?"),
            Say(PROF, "Com duas regrinhas. **Regra 1:** se a pode abrir a fila de α (a ∈ FIRST(α)), anote A → α em M[A, a]."),
            Say(PROF, "**Regra 2:** se α pode sumir (ε ∈ FIRST(α)), então para cada b que pode vir depois de A (b ∈ FOLLOW(A)), anote A → α em M[A, b]. Inclusive na coluna \$."),
            Code(P2_ABC, "A gramática da fila de novo. FOLLOW(A) = {b, c}, FOLLOW(B) = {c}."),
            Predict(
                "Estou em A e chega um 'c'. O que diz o caderninho?",
                listOf("A → a", "A → ε", "Erro", "As duas"), 1,
                "'c' não abre A → a. Mas A pode sumir e c ∈ FOLLOW(A) = {b, c}. Então M[A, c] = A → ε: a turma A falta e o c é atendido mais adiante."
            ),
            Visual(LL1_TABLE, P2_ABC, caption = "O caderninho completo. Repare em A → ε nas colunas b e c."),
            Say(BYTE, "E se o porteiro tiver DUAS portas anotadas na mesma linha e coluna?"),
            Say(PROF, "Aí ele trava: é um **conflito**, e a gramática **não é LL(1)**. Olha só:"),
            Visual(LL1_TABLE, P2_FACT, caption = "S → a b | a c: as duas produções começam com a, e caem juntas em M[S, a]. O porteiro não sabe qual porta abrir."),
            Reveal("E uma célula VAZIA no caderninho, significa o quê?", "Erro de sintaxe: nenhuma produção de A pode começar com aquele token, nem ele pode vir depois de um A que sumiu."),
            Callout(REAL_WORLD, "O GCC e o Clang, compiladores de C/C++ usados no mundo todo, têm analisadores descendentes escritos à mão: o mesmo 'caderninho', só que embutido em ifs."),
            Say(PROF, "Resumo do porteiro: FIRST abre a porta; se a produção pode sumir, o FOLLOW abre também. Duas portas na mesma célula? Briga na entrada."),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.VISUAL, "Vendo a tabela nascer (e quebrar)",
        listOf(
            Say(PROF, "Vamos ver a tabela da gramática de expressões nascer — e depois o que acontece com uma gramática 'proibida'."),
            Code(P2_EXPR_LL),
            Visual(LL1_TABLE, P2_EXPR_LL, caption = "Toque nas células para ver por que cada produção foi parar ali."),
            Steps(
                "O que olhar",
                listOf(
                    "Linhas = não-terminais; colunas = terminais + \$.",
                    "E → T E' aparece em ( e id: é FIRST(T E').",
                    "E' → ε aparece em ) e \$: é FOLLOW(E').",
                    "T' → ε aparece em +, ) e \$: é FOLLOW(T').",
                    "Nenhuma célula tem duas produções: a gramática é LL(1).",
                )
            ),
            Predict(
                "Em qual célula fica F → ( E )?",
                listOf("M[F, (]", "M[F, )]", "M[E, (]", "M[F, id]"), 0,
                "FIRST(( E )) = {(}. Então M[F, (] = F → ( E )."
            ),
            Say(BYTE, "E aquela gramática bonita, E → E + T | T, com recursão à esquerda?"),
            Visual(LL1_TABLE, P2_EXPR, caption = "A versão recursiva à esquerda: veja as células em conflito."),
            Text("M[E, id] ganhou **duas** produções, E → E + T e E → T, porque FIRST(E + T) = FIRST(T) = {(, id}. Recursão à esquerda sempre provoca esse tipo de conflito."),
            Callout(WATCH_OUT, "Gramática com recursão à esquerda ou ambígua NUNCA é LL(1). Primeiro conserte (na Ponte da Transformação), depois monte a tabela."),
            Reveal("Quantas células preenchidas tem a tabela da versão LL(1)? Conte no widget antes de tocar.", "13: E (2), E' (3), T (2), T' (4) e F (2)."),
            Visual(LEFT_RECURSION, P2_EXPR, caption = "Lembrete da Ponte: eliminar a recursão à esquerda leva da gramática com conflitos à versão LL(1)."),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.WORKED, "Exemplo resolvido: tabela das declarações",
        listOf(
            Say(PROF, "Exercício de prova: construa a tabela LL(1) da gramática de declarações."),
            Code(P2_DECL),
            Text("Ingredientes já calculados: FIRST(T) = {int, float}, FIRST(L) = {id}, FIRST(R) = {',', ε}; FOLLOW(D) = {\$}, FOLLOW(T) = {id}, FOLLOW(L) = FOLLOW(R) = {;}."),
            Steps(
                "Produção por produção",
                listOf(
                    "D → T L ; : FIRST(T L ;) = {int, float}. Anote em M[D, int] e M[D, float].",
                    "T → int : M[T, int].",
                    "T → float : M[T, float].",
                    "L → id R : M[L, id].",
                    "R → , id R : FIRST = {,}. M[R, ,].",
                    "R → ε : FIRST(ε) = {ε}, então use FOLLOW(R) = {;}. M[R, ;].",
                    "Nenhuma célula com duas produções → LL(1).",
                )
            ),
            Visual(LL1_TABLE, P2_DECL, caption = "Confira as 7 células preenchidas. Todo o resto é erro."),
            Say(BYTE, "Por que R → ε não vai na coluna \$?"),
            Say(PROF, "Porque \$ ∉ FOLLOW(R): depois de R sempre vem ';'. Se chegar \$ com R no topo, é erro mesmo — faltou o ponto e vírgula!"),
            Say(PROF, "Agora o desbotado: S → A B c, A → a | ε, B → b | ε, com FOLLOW(A) = {b, c} e FOLLOW(B) = {c}."),
            Text("Eu faço: S → A B c tem FIRST = {a, b, c}, então vai em M[S, a], M[S, b] e M[S, c]. A → a vai em M[A, a]. B → b vai em M[B, b]."),
            Reveal("Sua vez: onde vai A → ε?", "Em M[A, b] e M[A, c], porque FOLLOW(A) = {b, c}."),
            Reveal("E B → ε?", "Em M[B, c], porque FOLLOW(B) = {c}."),
            Reveal("A gramática é LL(1)?", "Sim: nenhuma célula ficou com duas produções."),
            Visual(LL1_TABLE, P2_ABC, caption = "Confira suas respostas."),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.SOCRATIC, "Como escolher sem voltar atrás?",
        listOf(
            Say(PROF, "Pergunta-raiz: o analisador está em A e vê o token a. Como ele escolhe a produção sem nunca voltar atrás?"),
            Predict(
                "A → x y | z w. Chega o token z. Qual produção?",
                listOf("A → x y", "A → z w", "Precisa testar as duas", "Nenhuma"), 1,
                "z ∈ FIRST(z w) e z ∉ FIRST(x y). Escolha única. É a regra do FIRST."
            ),
            Predict(
                "A → x y | x w. Chega x. E agora?",
                listOf("A → x y", "A → x w", "Não dá para decidir com 1 token", "Erro"), 2,
                "As duas começam com x: conflito em M[A, x]. Com um só token de espiada não dá. (O remédio é a fatoração à esquerda.)"
            ),
            Predict(
                "A → x y | ε, e FOLLOW(A) = {z}. Chega z. Qual?",
                listOf("A → x y", "A → ε", "Erro", "As duas"), 1,
                "z não abre x y, mas pode vir DEPOIS de A. Então A some: A → ε. É a regra do FOLLOW."
            ),
            Predict(
                "A → x y | ε, mas agora FOLLOW(A) = {x, z}. Chega x. Qual?",
                listOf("A → x y", "A → ε", "Conflito: as duas", "Erro"), 2,
                "x abre x y (FIRST) E x pode vir depois de A (FOLLOW). Duas produções na mesma célula: não é LL(1)."
            ),
            Reveal(
                "Com base nessas 4 perguntas, quais são as condições para uma gramática ser LL(1)? Tente formular antes de tocar.",
                "Para cada par A → α | β: (1) FIRST(α) e FIRST(β) disjuntos; (2) no máximo um deles deriva ε; (3) se β ⇒* ε, FIRST(α) e FOLLOW(A) disjuntos."
            ),
            Say(BYTE, "Então a tabela é só um jeito de enxergar essas condições de uma vez?"),
            Say(PROF, "Exatamente. Montou a tabela e nenhuma célula tem duas produções? É LL(1). Simples assim."),
            Visual(LL1_TABLE, P2_LIST, caption = "Teste: S → S a | b. Antes de olhar, preveja se haverá conflito."),
            Reveal("Por que M[S, b] tem conflito?", "FIRST(S a) = FIRST(S) = {b} e FIRST(b) = {b}: as duas produções disputam a coluna b. Recursão à esquerda gera conflito sempre."),
            Callout(BRAIN_POWER, "Uma gramática ambígua pode ser LL(1)? Pense: se fosse, a tabela daria uma única produção a cada passo…"),
            Reveal("Responda o Poder do Cérebro.", "Não. O analisador LL(1) constrói uma única árvore de forma determinística; uma gramática ambígua tem duas árvores para alguma frase, então sua tabela terá conflito."),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.FORMAL, "Construção da tabela preditiva (versão prova)",
        listOf(
            Say(PROF, "Algoritmo de construção da tabela de análise preditiva, como no Livro do Dragão."),
            Code(
                """
                entrada: gramática G, com FIRST e FOLLOW calculados
                saída:   tabela M[A, a]

                para cada produção A → α de G:
                  para cada terminal a ∈ FIRST(α):
                    adicione A → α a M[A, a]
                  se ε ∈ FIRST(α):
                    para cada b ∈ FOLLOW(A):      // b pode ser ${'$'}
                      adicione A → α a M[A, b]
                toda entrada vazia de M = erro
                """.trimIndent(),
                "Percorra PRODUÇÃO por produção, não célula por célula."
            ),
            Text("**Definição.** G é **LL(1)** se e somente se M não tem nenhuma entrada com mais de uma produção. LL(1): lê a entrada da esquerda para a direita (**L**), produz uma derivação mais à esquerda (**L**), usando **1** símbolo de lookahead."),
            Steps(
                "Condições equivalentes, para cada par A → α | β",
                listOf(
                    "1. FIRST(α) ∩ FIRST(β) = ∅.",
                    "2. No máximo um entre α e β deriva ε.",
                    "3. Se β ⇒* ε, então FIRST(α) ∩ FOLLOW(A) = ∅ (e vice-versa).",
                )
            ),
            Predict(
                "Qual destas NÃO é condição para LL(1)?",
                listOf(
                    "FIRST das alternativas disjuntos",
                    "No máximo uma alternativa anulável",
                    "A gramática não pode ter produções ε",
                    "FIRST(α) ∩ FOLLOW(A) = ∅ quando a outra alternativa é anulável",
                ), 2,
                "Produções ε são permitidas em LL(1)! A gramática de expressões LL(1) tem E' → ε e T' → ε."
            ),
            Visual(LL1_TABLE, P2_EXPR_LL, caption = "Cada célula traz o motivo: 'a ∈ FIRST(α)' ou 'α pode virar ε e b ∈ FOLLOW(A)'."),
            Callout(EXAM_TIP, "Na prova: (1) calcule FIRST e FOLLOW numa tabelinha; (2) percorra produção por produção; (3) não esqueça a coluna \$; (4) se perguntarem 'é LL(1)?', aponte a célula com conflito e as duas produções que brigam nela."),
            Callout(WATCH_OUT, "Recursão à esquerda, ambiguidade ou prefixos comuns não fatorados impedem LL(1). Mas a volta não vale: tirar tudo isso não garante LL(1) (ex.: S → A a, A → a | ε dá conflito em M[A, a])."),
            Reveal("Formule a definição de LL(1) em uma linha.", "G é LL(1) se sua tabela preditiva não tem nenhuma entrada multiplamente definida."),
            Say(BYTE, "LL(1): Left-to-right, Leftmost, 1 token. Guardei!"),
        )
    ),
    Lesson(
        Skill.LL1_TABLE, Style.MNEMONIC, "Sudoku LL(1): uma célula, uma produção",
        listOf(
            Say(NARRADOR, "Terceira sala do Templo: um tabuleiro de Sudoku gigante pintado no chão. Linhas com letras maiúsculas, colunas com tokens."),
            Callout(MNEMONIC, "**'Linha é Variável, Coluna é Token.'** M[A, a]: o maiúsculo vem primeiro, como num endereço 'rua, número'."),
            Callout(MNEMONIC, "Rima de ouro: **'Primeiro o FIRST; se o ε aparecer, chama o FOLLOW pra resolver.'**"),
            Callout(MNEMONIC, "**Sudoku LL(1): uma célula, uma produção.** Dois números no mesmo quadradinho invalidam o Sudoku. Duas produções na mesma célula invalidam o LL(1)."),
            Say(BYTE, "E o nome LL(1)? Sempre esqueço o que é cada L."),
            Callout(MNEMONIC, "**L-L-1 = 'Lê da esquerda, Leva a derivação mais à esquerda, 1 espiadinha.'** Left-to-right, Leftmost derivation, 1 token de lookahead."),
            Steps(
                "Confusões clássicas",
                listOf(
                    "Pôr A → ε nas colunas do FIRST: errado. ε vai nas colunas de FOLLOW(A).",
                    "Esquecer a coluna \$: ela existe e recebe produções ε quando \$ ∈ FOLLOW(A).",
                    "Achar que célula vazia é 'tanto faz': é ERRO de sintaxe.",
                    "Achar que produção ε impede LL(1): não impede.",
                )
            ),
            Predict(
                "A → ε e FOLLOW(A) = {), \$}. Em quais colunas vai A → ε?",
                listOf("Na coluna ε", "Nas colunas ) e \$", "Só na coluna \$", "Em todas as colunas"), 1,
                "Não existe coluna ε! A produção vazia vai nas colunas dos terminais de FOLLOW(A): ) e \$."
            ),
            Visual(LL1_TABLE, P2_PARENS, caption = "Teste a rima em S → ( S ) S | ε: o FIRST manda S → ( S ) S para '('; o ε chama o FOLLOW = {), \$}."),
            Reveal("Recite a rima e aplique em E' → + T E' | ε, com FOLLOW(E') = {), \$}.", "'Primeiro o FIRST': M[E', +] = E' → + T E'. 'Se o ε aparecer, chama o FOLLOW': M[E', )] = M[E', \$] = E' → ε."),
        )
    ),
)

// ───────────────────────────── ANALISADOR PREDITIVO ─────────────────────────────

internal val p2Ll1ParsingLessons: List<Lesson> = listOf(
    Lesson(
        Skill.LL1_PARSING, Style.STORY, "Pilha, o golem das tarefas",
        listOf(
            Say(NARRADOR, "No altar do Templo Top-Down mora Pilha, um golem de pedra. Ele só enxerga o topo da própria cabeça."),
            Say(PILHA, "Eu… guardo… o que… ainda… falta… ver."),
            Say(PROF, "A pilha do analisador preditivo é uma **lista de tarefas**: o que ainda esperamos encontrar na entrada. Começa com S (a frase inteira) em cima do \$ (o fim)."),
            Say(BYTE, "E como o golem trabalha?"),
            Steps(
                "As regras do golem",
                listOf(
                    "Topo é terminal e igual ao token da entrada? **Casa**: tira do topo e avança na entrada. Tarefa cumprida!",
                    "Topo é não-terminal A e o token é a? Consulta o caderninho M[A, a] e **troca A pelo lado direito**, empilhado de trás para frente.",
                    "Topo \$ e entrada \$? **Aceita**.",
                    "Qualquer outra situação? **Erro**.",
                )
            ),
            Say(PILHA, "De… trás… pra… frente… porque… o primeiro… tem que… ficar… em cima."),
            Predict(
                "Pilha \$ E (topo à direita), entrada começando com id, e M[E, id] = E → T E'. Como fica a pilha?",
                listOf("\$ T E'", "\$ E' T", "\$ E T E'", "\$ T"), 1,
                "Tira o E e empilha T E' ao contrário: primeiro E', depois T. O T fica no topo, pronto para ser atendido primeiro."
            ),
            Visual(LL1_TRACE, P2_EXPR_LL, "id + id", "Acompanhe o golem: pilha, entrada e ação a cada passo."),
            Callout(NO_DUMB_QUESTIONS, "P: Por que pilha e não fila? R: Porque a árvore é aninhada: ao expandir T, tudo que é de T precisa ser resolvido antes de voltar ao E'. Quem entrou por último sai primeiro: pilha!"),
            Reveal("Quando o golem usa E' → ε, o que ele empilha?", "Nada. Ele só tira o E' do topo: a tarefa 'ver um E'' foi cumprida com zero tokens."),
            Say(PILHA, "Pilha… vazia… entrada… vazia… festa."),
            Say(PROF, "E repare: a sequência de expansões é exatamente a **derivação mais à esquerda**. O golem é um derivador com bússola."),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.VISUAL, "Analisador preditivo, passo a passo",
        listOf(
            Say(PROF, "Entrada clássica: id + id * id. São 17 passos até o 'aceitar'. Vamos assistir."),
            Code(P2_EXPR_LL),
            Visual(LL1_TRACE, P2_EXPR_LL, "id + id * id", "Avance um passo por vez. Antes de cada clique, tente adivinhar a ação."),
            Steps(
                "Pontos de atenção no trace",
                listOf(
                    "Passos 1–3: E → T E', T → F T', F → id. Três expansões até aparecer um terminal no topo.",
                    "Passo 4: id casa com id.",
                    "Passo 5: T' no topo, entrada '+'. M[T', +] = T' → ε: o T' some.",
                    "Passo 6: E' → + T E'. O '+' foi previsto pela tabela, e no passo 7 ele casa.",
                    "No fim: T' → ε e E' → ε com \$ na entrada; pilha \$ e entrada \$: aceita.",
                )
            ),
            Predict(
                "Topo = T', entrada = *. Qual ação?",
                listOf("T' → ε", "T' → * F T'", "Casar *", "Erro"), 1,
                "* ∈ FIRST(* F T'), então M[T', *] = T' → * F T'. Só no passo seguinte o * vai casar."
            ),
            Say(BYTE, "Por que no passo 5 o T' sumiu e nesse outro ele expandiu?"),
            Say(PROF, "Por causa do token! Com '+' o T' não tem o que fazer (+ ∈ FOLLOW(T')), então some. Com '*' ele tem trabalho: expande."),
            Reveal("Quantas vezes 'id' foi casado nesse trace?", "3: uma para cada id da entrada. Cada terminal da entrada é casado exatamente uma vez."),
            Visual(DERIVATION, P2_EXPR_LL, "id + id * id", "Compare: a derivação mais à esquerda usa as mesmas produções, na mesma ordem das expansões do trace."),
            Callout(BRAIN_POWER, "E se a entrada fosse 'id + * id'? Em que momento o golem travaria?"),
            Reveal("Responda o Poder do Cérebro.", "Depois de casar o '+', o topo é T e o token é '*'. M[T, *] está vazia → erro de sintaxe, detectado já no primeiro token impossível."),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.WORKED, "Exemplo resolvido: int id , id ;",
        listOf(
            Say(PROF, "Vamos simular à mão, no formato que a prova pede: Pilha | Entrada | Ação. Topo da pilha à direita."),
            Code(P2_DECL, "Tabela: M[D,int] = D → T L ;   M[T,int] = T → int   M[L,id] = L → id R   M[R,,] = R → , id R   M[R,;] = R → ε"),
            Steps(
                "Simulação de int id , id ;",
                listOf(
                    "\$ D | int id , id ; \$ | expandir D → T L ;",
                    "\$ ; L T | int id , id ; \$ | expandir T → int",
                    "\$ ; L int | int id , id ; \$ | casar int",
                    "\$ ; L | id , id ; \$ | expandir L → id R",
                    "\$ ; R id | id , id ; \$ | casar id",
                    "\$ ; R | , id ; \$ | expandir R → , id R",
                    "\$ ; R id , | , id ; \$ | casar ,",
                )
            ),
            Say(BYTE, "Olha só: o ';' ficou lá embaixo esperando desde o primeiro passo!"),
            Say(PILHA, "Paciência… é… meu… forte."),
            Text("Agora os passos finais são seus. Pense em cada um antes de revelar. Pilha atual: \$ ; R id. Entrada: id ; \$."),
            Reveal("Próxima ação?", "Casar id. A pilha fica \$ ; R e a entrada ; \$."),
            Reveal("Topo R, entrada ';'. Ação?", "Expandir R → ε (é M[R, ;]). A pilha fica \$ ;."),
            Reveal("E depois?", "Casar ';'. Pilha \$ e entrada \$ → aceitar!"),
            Visual(LL1_TRACE, P2_DECL, "int id , id ;", "Confira com o motor: 11 linhas, terminando em 'aceitar'."),
            Callout(EXAM_TIP, "Escreva a pilha SEMPRE com o topo do mesmo lado (de preferência à direita) e o \$ no fundo. Metade dos erros de simulação em prova é pilha 'virada' no meio do caminho."),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.SOCRATIC, "Reinvente o analisador preditivo",
        listOf(
            Say(PROF, "Você vai redescobrir o analisador preditivo não recursivo. Ponto de partida: uma pilha contendo \$ S."),
            Predict(
                "Por que colocar \$ no fundo da pilha?",
                listOf("Enfeite", "Para saber quando a pilha 'acabou' e comparar com o fim da entrada", "Porque \$ é um não-terminal", "Para guardar o resultado"), 1,
                "\$ é a sentinela: quando o topo é \$ e a entrada também acabou (\$), aceitamos."
            ),
            Predict(
                "O topo é o terminal ')' e a entrada é 'id'. O que fazer?",
                listOf("Casar mesmo assim", "Erro: esperava ')'", "Consultar a tabela", "Empilhar id"), 1,
                "Terminal no topo é uma promessa: 'o próximo token TEM que ser este'. Veio outro? Erro de sintaxe."
            ),
            Predict(
                "O topo é A e M[A, a] = A → X Y Z. Por que empilhar Z, depois Y, depois X?",
                listOf("Por tradição", "Para X ficar no topo e ser tratado primeiro", "Para Z casar primeiro", "Tanto faz"), 1,
                "Derivamos sempre o símbolo mais à esquerda: X é o próximo a ser expandido ou casado, então precisa estar no topo."
            ),
            Predict(
                "O topo é A e M[A, a] está vazia. O que isso significa?",
                listOf("Aceitar", "Empilhar a", "Erro de sintaxe", "Tentar outra produção (backtracking)"), 2,
                "O analisador preditivo NUNCA volta atrás. Célula vazia = nenhuma produção de A serve = erro."
            ),
            Reveal(
                "Monte o algoritmo com as suas respostas, em 4 casos (X = topo, a = token).",
                "(1) X = a = \$ → aceita. (2) X = a terminal → desempilha e avança. (3) X terminal ≠ a → erro. (4) X não-terminal → se M[X, a] = X → Y1…Yk, troca X por Yk…Y1 (Y1 no topo); se vazia, erro."
            ),
            Say(BYTE, "E qual é a 'saída' do analisador?"),
            Predict(
                "Se eu imprimir cada produção usada nas expansões, o que obtenho?",
                listOf("Uma derivação mais à direita", "Uma derivação mais à esquerda", "A tabela LL(1)", "Os conjuntos FOLLOW"), 1,
                "A expansão acontece sempre no não-terminal pendente mais à esquerda: é a derivação mais à esquerda, o segundo L de LL(1)."
            ),
            Visual(LL1_TRACE, P2_PARENS, "( ) ( )", "Teste suas regras em S → ( S ) S | ε com a entrada ( ) ( )."),
            Reveal("Nesse trace, quantas vezes S → ε foi usada?", "3 vezes: dentro do primeiro par, dentro do segundo par e no final, com \$ na entrada."),
            Say(PROF, "Você reinventou o algoritmo do Livro do Dragão. Isso não é coisa de 'burro', hein?"),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.FORMAL, "Analisador preditivo não recursivo (versão prova)",
        listOf(
            Say(PROF, "Analisador preditivo não recursivo, dirigido por tabela: a definição de prova."),
            Text("Componentes: um **buffer de entrada** terminado em \$; uma **pilha** de símbolos gramaticais com \$ no fundo; a **tabela** M[A, a]; e um **programa de controle**. Configuração inicial: pilha = \$ S (S no topo), entrada = w\$."),
            Code(
                """
                empilhe ${'$'}; empilhe S
                a := primeiro símbolo de w${'$'}
                X := topo da pilha
                enquanto X ≠ ${'$'}:
                  se X = a:                desempilhe; a := próximo símbolo
                  senão se X é terminal:   erro()
                  senão se M[X, a] vazia:  erro()
                  senão se M[X, a] = X → Y1 Y2 … Yk:
                    imprima X → Y1 Y2 … Yk
                    desempilhe
                    empilhe Yk, Yk-1, …, Y1      // Y1 fica no topo
                  X := topo da pilha
                se a = ${'$'}: aceite   senão: erro()
                """.trimIndent(),
                "O programa de controle. Para A → ε, nada é empilhado."
            ),
            Text("**Invariante:** a parte da entrada já casada, seguida da pilha lida do topo para o fundo, é sempre uma forma sentencial mais à esquerda. Por isso a saída é uma derivação mais à esquerda."),
            Predict(
                "Configuração: pilha \$ E' T' (topo à direita), entrada \$. O que acontece a seguir?",
                listOf("Erro", "T' → ε, E' → ε, aceita", "Casa \$ com T'", "E' → + T E'"), 1,
                "M[T', \$] = T' → ε e M[E', \$] = E' → ε, pois \$ está em FOLLOW de ambos. Sobra pilha \$ e entrada \$: aceita."
            ),
            Visual(LL1_TRACE, P2_EXPR_LL, "( id )", "Siga o pseudocódigo linha a linha nesta entrada curta."),
            Callout(EXAM_TIP, "Formato esperado: colunas **Pilha | Entrada | Ação** (ou Saída), uma linha por passo. Ações escritas como 'E → T E'' ou 'casar id'. Termine com 'aceita' ou marque a linha do erro."),
            Callout(WATCH_OUT, "Não confunda as pilhas: a do LL guarda o que ainda FALTA ver (previsão). A do LR, lá na Fortaleza Bottom-Up, guarda o que JÁ foi visto."),
            Reveal("Quais são as situações do programa de controle?", "X = a = \$: aceita. X = a: casa e avança. X terminal ≠ a, ou M[X, a] vazia: erro. X não-terminal com M[X, a] = produção: substitui X pelo lado direito, invertido."),
            Callout(REAL_WORLD, "O ANTLR, gerador de analisadores muito usado, produz analisadores descendentes da família LL (no ANTLR 4, o ALL(*)), primos do que você está estudando."),
        )
    ),
    Lesson(
        Skill.LL1_PARSING, Style.MNEMONIC, "As três frases na testa do golem",
        listOf(
            Say(NARRADOR, "No altar do Templo Top-Down, o golem Pilha tem três frases gravadas na testa."),
            Callout(MNEMONIC, "**'Terminal no topo? Casa e anda. Variável no topo? Consulta e troca. \$ com \$? Festa!'**"),
            Callout(MNEMONIC, "**Empilhar ao contrário = arrumar a mochila.** O que você vai usar primeiro entra por último e fica em cima. Para A → X Y Z: guarda Z, depois Y, depois X."),
            Say(BYTE, "E qual a diferença entre a pilha daqui e a da Fortaleza Bottom-Up?"),
            Callout(FIRESIDE, "LL e LR ao pé da lareira. LL: 'Minha pilha é o FUTURO: tudo que eu prevejo que vai aparecer.' LR: 'A minha é o PASSADO: tudo que já li e ainda não consegui montar.' LL: 'Eu decido cedo, só com o primeiro token.' LR: 'Eu espero ver o lado direito inteiro antes de decidir. Por isso aceito mais gramáticas.'"),
            Steps(
                "Confusões clássicas",
                listOf(
                    "Empilhar o lado direito na ordem normal (X no fundo): errado, X tem que ficar no topo.",
                    "Avançar a entrada numa expansão: não! Só avança quando CASA um terminal.",
                    "Empilhar ε: não existe. Produção ε só desempilha.",
                    "Esquecer de começar com \$ S na pilha.",
                )
            ),
            Predict(
                "Qual ação faz a entrada avançar?",
                listOf("Expandir", "Casar", "Ambas", "Nenhuma"), 1,
                "Só casar consome token. Expandir só reorganiza a pilha."
            ),
            Visual(LL1_TRACE, P2_ANBN, "a a b b", "Teste as frases do golem em S → a S b | ε."),
            Reveal("Recite as três frases do golem.", "Terminal no topo? Casa e anda. Variável no topo? Consulta (a tabela) e troca (pelo lado direito, invertido). \$ com \$? Festa (aceita)!"),
        )
    ),
)
