package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.GrammarBank
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 1c do conteúdo: LEFT_RECURSION, LEFT_FACTORING (Ponte da Transformação).

private val p1cExpr: String get() = GrammarBank.exprLeftRec.text
private val p1cExprLL: String get() = GrammarBank.exprLL1.text
private const val P1C_SA = "S -> S a | b"
private const val P1C_ELSE = "S -> i E t S | i E t S e S | a\nE -> b"
private const val P1C_ELSE_FAT = "S -> i E t S S' | a\nS' -> e S | ε\nE -> b"
private const val P1C_STMTS = "L -> S ; L | S\nS -> id = E | print E\nE -> id | num"
private const val P1C_STMTS_FAT = "L -> S L'\nL' -> ; L | ε\nS -> id = E | print E\nE -> id | num"

// ───────────────────────────── LEFT_RECURSION ─────────────────────────────

private val p1cLeftRec: List<Lesson> = listOf(
    Lesson(
        Skill.LEFT_RECURSION, Style.STORY, "O cachorro que corre atrás do rabo",
        listOf(
            Say(NARRADOR, "Repartição pública. Placa na parede: 'Para tirar o documento A, apresente o documento A.'"),
            Say(BYTE, "Mas… isso é impossível! Eu nunca saio do lugar!"),
            Say(PROF, "Pois é exatamente isso que acontece com um analisador **descendente** (top-down) diante de `E -> E + T`."),
            Text("Na descida recursiva, cada não-terminal vira uma função. A função E() tenta a produção E → E + T: 'para reconhecer E, primeiro reconheça E'. Ela chama E()… que chama E()… que chama E()…"),
            Code("void E() {\n    E();          // chama a si mesma sem consumir NADA\n    match('+');\n    T();\n}", "Laço infinito → estouro de pilha"),
            Say(PILHA, "E()… E()… E()… E()… Eu… só… vejo… o… topo… e… ele… não… para… de… crescer… ESTOUREI. 💥"),
            Callout(WATCH_OUT, "O problema não é ter recursão — é a recursão ser a PRIMEIRA coisa, antes de consumir qualquer token. Cachorro correndo atrás do rabo: gira, gira e não sai do lugar."),
            Say(BYTE, "E como sai do ciclo?"),
            Say(PROF, "Olha o que `E -> E + T | T` realmente gera: um T e depois quantos '+ T' quiser. T + T + T… Então começamos pelo T (a parte que NÃO é recursiva) e deixamos a repetição para o final."),
            Code("E  -> T E'\nE' -> + T E' | ε", "Mesma linguagem, recursão agora à DIREITA"),
            Text("É como consertar a placa da repartição: 'Apresente o RG. Depois, se quiser, mais documentos.' Primeiro algo concreto; depois a repetição."),
            Visual(VK.LEFT_RECURSION, grammar = p1cExpr, caption = "Antes e depois: E e T ganharam os ajudantes E' e T'."),
            Predict(
                "A gramática nova gera a mesma linguagem?",
                listOf("Sim, as mesmas cadeias", "Não, gera menos cadeias"),
                0,
                "As duas geram T (+ T)*. Muda só o formato da árvore.",
            ),
            Callout(REAL_WORLD, "Analisadores LR (Yacc/Bison) adoram recursão à esquerda — ela até economiza pilha. O problema é só dos descendentes (LL, descida recursiva)."),
            Reveal("Por que recursão à esquerda trava o analisador descendente?", "Porque a função de A chama A de novo antes de consumir qualquer token: a entrada não avança e a recursão nunca termina."),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.VISUAL, "A cirurgia ao vivo",
        listOf(
            Say(PROF, "Vamos operar. O widget mostra a gramática antes e depois."),
            Visual(VK.LEFT_RECURSION, grammar = P1C_SA, caption = "S -> S a | b vira S -> b S' e S' -> a S' | ε."),
            Text("Leia assim: α = `a` (o que vem DEPOIS do S recursivo) e β = `b` (a alternativa que NÃO começa com S). A linguagem é b a a a… = βα*."),
            Visual(VK.DERIVATION, grammar = P1C_SA, input = "b a a", caption = "Gramática original: a árvore cresce para a ESQUERDA."),
            Visual(VK.DERIVATION, grammar = "S -> b S'\nS' -> a S' | ε", input = "b a a", caption = "Gramática nova: a árvore cresce para a DIREITA."),
            Predict(
                "Qual das duas um analisador descendente consegue usar?",
                listOf("A original", "A nova"),
                1,
                "Na nova, S começa consumindo b, e S' decide pelo próximo token: 'a' → continua; fim → ε.",
            ),
            Visual(VK.LEFT_RECURSION, grammar = p1cExpr, caption = "Agora com duas recursões: E e T."),
            Text("Repare: F não mudou — ela não tinha recursão à esquerda. A cirurgia só mexe em quem precisa."),
            Visual(VK.LL1_TABLE, grammar = p1cExpr, caption = "Tabela LL(1) da gramática ORIGINAL: veja as células com conflito."),
            Visual(VK.LL1_TABLE, grammar = p1cExprLL, caption = "Tabela da gramática transformada: uma produção por célula. LL(1)!"),
            Reveal("Por que a tabela original tinha conflito?", "E → E + T e E → T começam, ambas, com algo que deriva id ou '('. Olhando só o próximo token, não dá para escolher. Recursão à esquerda SEMPRE causa esse conflito."),
            Callout(NO_DUMB_QUESTIONS, "P: O ε em E' é obrigatório? R: Sim! É ele que encerra a repetição. Sem o ε, E' nunca terminaria de gerar."),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.WORKED, "Eliminando passo a passo",
        listOf(
            Say(PROF, "Receita: A → Aα₁ | … | Aαₘ | β₁ | … | βₙ vira A → β₁A' | … | βₙA' e A' → α₁A' | … | αₘA' | ε."),
            Code("E -> E + T | E - T | T", "Exercício resolvido"),
            Steps(
                "Resolvido",
                listOf(
                    "1. Separe as alternativas que começam com E (recursivas) das outras.",
                    "2. Recursivas: E + T e E - T → α₁ = `+ T`, α₂ = `- T`.",
                    "3. Não recursiva: T → β₁ = `T`.",
                    "4. Novo E: cada β seguido de E' → E → T E'.",
                    "5. Novo E': cada α seguido de E', mais ε → E' → + T E' | - T E' | ε.",
                ),
            ),
            Visual(VK.LEFT_RECURSION, grammar = "E -> E + T | E - T | T\nT -> id", caption = "Confira com o motor."),
            Text("Agora esmaecido (exemplo do Livro do Dragão): `A -> A c | A a d | b d | ε`."),
            Reveal("Quem são os α's?", "α₁ = `c` e α₂ = `a d` — o que vem DEPOIS do A nas alternativas recursivas."),
            Reveal("Quem são os β's? (Atenção ao ε!)", "β₁ = `b d` e β₂ = `ε`. A alternativa vazia também é um β: ela não começa com A."),
            Reveal("Escreva a gramática final.", "A → b d A' | A'\nA' → c A' | a d A' | ε"),
            Visual(VK.LEFT_RECURSION, grammar = "A -> A c | A a d | b d | ε", caption = "Confira: o β = ε virou A → A'."),
            Callout(WATCH_OUT, "Erro nº 1: esquecer o `| ε` em A'. Erro nº 2: esquecer de colocar A' no fim de CADA β."),
            Callout(EXAM_TIP, "Se a questão pedir 'elimine a recursão à esquerda e mostre que a gramática resultante é LL(1)', faça a transformação E depois a tabela. Os pontos costumam vir das duas partes."),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.SOCRATIC, "Descubra a receita sozinho",
        listOf(
            Code(P1C_SA),
            Predict(
                "Quais cadeias essa gramática gera?",
                listOf("b, ba, baa, baaa…", "a, ab, aab…", "ab, aabb, aaabbb…"),
                0,
                "S ⇒ S a ⇒ S a a ⇒ b a a: o b sempre fica na ponta esquerda, e os a's se acumulam à direita.",
            ),
            Reveal("Escreva essa linguagem como expressão regular.", "`ba*` — um b e depois quantos a's quiser. Em geral, A → Aα | β gera βα*."),
            Predict(
                "Um analisador descendente lendo `b a a` precisa escolher a primeira produção de S. Olhando só o 'b', ele sabe quantas vezes usar S → S a?",
                listOf("Sim", "Não — teria que adivinhar o futuro (quantos a's virão)"),
                1,
                "A quantidade de S → S a depende de quantos a's vêm DEPOIS. Com um token de lookahead, impossível saber.",
            ),
            Say(PROF, "Então vamos gerar βα* de outro jeito: primeiro o β, depois um 'gerador de α's'."),
            Predict(
                "Qual gramática gera b a* sem recursão à esquerda?",
                listOf("S -> b S'  e  S' -> a S' | ε", "S -> S' b  e  S' -> a S' | ε", "S -> a S | b"),
                0,
                "A segunda e a terceira geram a*b (o b no FIM). Só a primeira põe o b na frente e repete os a's depois.",
            ),
            Reveal("Generalize: o que fazer com A → Aα | β?", "A → β A' e A' → α A' | ε."),
            Predict(
                "`S -> A a | b` e `A -> S c | d` têm recursão à esquerda?",
                listOf("Não: nenhuma produção começa com o próprio lado esquerdo", "Sim, indireta: S ⇒ A a ⇒ S c a"),
                1,
                "Recursão à esquerda INDIRETA: S ⇒⁺ S… A receita imediata não basta; primeiro é preciso substituir para torná-la imediata.",
            ),
            Reveal(
                "Como eliminar essa recursão indireta?",
                "Ordene S, A. Em A → S c, substitua S pelas suas alternativas: A → A a c | b c | d. Agora é imediata: A → b c A' | d A'; A' → a c A' | ε.",
            ),
            Visual(VK.LEFT_RECURSION, grammar = "S -> A a | b\nA -> A a c | b c | d", caption = "O último passo (recursão já imediata), feito pelo motor."),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.FORMAL, "Algoritmo de eliminação",
        listOf(
            Text("**Definição.** Uma gramática é **recursiva à esquerda** se tem um não-terminal A tal que A ⇒⁺ Aα para alguma cadeia α. A recursão é **imediata** quando existe uma produção A → Aα."),
            Text("Métodos de análise descendente não conseguem tratar gramáticas recursivas à esquerda; é necessária uma transformação que elimine a recursão."),
            Code(
                """
                Recursão imediata:
                  A → Aα₁ | Aα₂ | ... | Aαₘ | β₁ | β₂ | ... | βₙ
                  (nenhum βᵢ começa com A)
                substituir por:
                  A  → β₁A' | β₂A' | ... | βₙA'
                  A' → α₁A' | α₂A' | ... | αₘA' | ε
                """.trimIndent(),
            ),
            Code(
                """
                Entrada: G sem ciclos (A ⇒⁺ A) e sem produções-ε
                ordene os não-terminais A₁, A₂, ..., Aₙ
                para i ← 1 até n:
                    para j ← 1 até i − 1:
                        substitua cada Aᵢ → Aⱼγ por Aᵢ → δ₁γ | δ₂γ | ... | δₖγ,
                        onde Aⱼ → δ₁ | δ₂ | ... | δₖ são as produções atuais de Aⱼ
                    elimine a recursão imediata das produções de Aᵢ
                """.trimIndent(),
                "Algoritmo 4.19 (Livro do Dragão)",
            ),
            Callout(WATCH_OUT, "Pré-condição: sem ciclos e sem produções-ε. (O algoritmo pode até funcionar com ε, mas sem garantia.) Na prova, cite isso!"),
            Predict(
                "Ao final do algoritmo, toda produção Aᵢ → Aⱼγ satisfaz…",
                listOf("j < i", "j > i", "j = i"),
                1,
                "Depois da iteração i, qualquer Aᵢ → Aⱼγ tem j > i. Os índices só sobem — nenhum ciclo de recursão à esquerda sobrevive.",
            ),
            Text("Exemplo do livro: S → A a | b, A → A c | S d | ε. Para A, substitua S em A → S d: A → A c | A a d | b d | ε. Eliminando a imediata: A → b d A' | A', A' → c A' | a d A' | ε."),
            Visual(VK.LEFT_RECURSION, grammar = "S -> A a | b\nA -> A c | A a d | b d | ε", caption = "O resultado do exemplo (depois da substituição)."),
            Callout(EXAM_TIP, "Duas armadilhas: (1) a nova gramática gera a MESMA linguagem, mas as árvores mudam (a associatividade fica 'escondida' em A' e é recuperada com atributos herdados); (2) recursão à esquerda NÃO é ambiguidade."),
            Reveal("Por que exigir que nenhum βᵢ comece com A?", "Porque se βᵢ começasse com A, A → βᵢA' continuaria recursiva à esquerda — a transformação não resolveria nada."),
        ),
    ),
    Lesson(
        Skill.LEFT_RECURSION, Style.MNEMONIC, "Beta começa, alfa repete, épsilon encerra",
        listOf(
            Callout(MNEMONIC, "**Beta começa, alfa repete, épsilon encerra.** A → β A'; A' → α A' | ε."),
            Callout(MNEMONIC, "**Rabo à esquerda morde**: em A → **A**α, o A morde o próprio rabo logo de cara. Recursão à DIREITA (A → αA) é inofensiva: primeiro come α (consome tokens), depois chama."),
            Text("β vem 'antes' — é o começo da cadeia, a parte não recursiva. α é o refrão que se repete. Linguagem: β α α α… = βα*."),
            Callout(MNEMONIC, "**O A-linha é um while disfarçado**: enquanto aparecer α, repete; quando não, ε e sai do laço."),
            Text("📍 **Palácio da memória**: na cabeceira da **Ponte da Transformação**, um cachorro corre atrás do rabo (A → Aα). O guarda da ponte só deixa passar quem mostra o β primeiro. No meio da ponte, um carrossel (A') gira α, α, α… até alguém gritar ε."),
            Predict(
                "Elimine a recursão de `L -> L , id | id`.",
                listOf("L -> id L'  e  L' -> , id L' | ε", "L -> , id L'  e  L' -> id L' | ε", "L -> id , L | id"),
                0,
                "β = `id` começa; α = `, id` repete; ε encerra. (A terceira até gera a mesma linguagem, mas precisaria de fatoração para ser LL(1).)",
            ),
            Visual(VK.LEFT_RECURSION, grammar = "L -> L , id | id", caption = "Lista de identificadores separados por vírgula."),
            Callout(WATCH_OUT, "Confusões clássicas: (1) trocar α e β; (2) esquecer o ε; (3) achar que só existe recursão imediata (há a INDIRETA: S → A a, A → S b); (4) achar que LR também precisa eliminar — não precisa!"),
            Callout(MNEMONIC, "**LL** = lê da **L**eft, derivação mais à **L**eft — e **L**eft recursion é o inimigo nº 1 do LL."),
            Reveal("Recite a receita aplicada a E → E + T | T.", "Beta começa: E → T E'. Alfa repete, épsilon encerra: E' → + T E' | ε."),
        ),
    ),
)

// ───────────────────────────── LEFT_FACTORING ─────────────────────────────

private val p1cFactoring: List<Lesson> = listOf(
    Lesson(
        Skill.LEFT_FACTORING, Style.STORY, "O menu do telefone do banco",
        listOf(
            Say(NARRADOR, "Você liga para o banco. A voz robótica: 'Para segunda via do boleto, diga BOLETO SEGUNDA VIA. Para boleto vencido, diga BOLETO VENCIDO…'"),
            Say(BYTE, "Eu digo 'boleto…' e o robô fica perdido!"),
            Say(PROF, "Pois é. Ouvindo só 'boleto', o sistema ainda não sabe qual opção você quer. Ele precisa ADIAR a decisão até ouvir mais."),
            Text("Um analisador preditivo tem o mesmo problema: olhando só o próximo token, precisa escolher UMA produção. Se duas alternativas começam igual, ele trava."),
            Code("S -> if E then S\n   | if E then S else S", "As duas começam com 'if E then S' — qual escolher ao ver 'if'?"),
            Say(PROF, "Solução da URA esperta: 'Diga BOLETO'. Depois, um submenu: 'segunda via ou vencido?'. Junta o pedaço comum e decide depois."),
            Code("S  -> if E then S S'\nS' -> else S | ε", "Prefixo comum na frente; as diferenças vão para S'"),
            Callout(MNEMONIC, "Lembra da escola? ab + ac = a(b + c). **Colocar em evidência!** Fatoração à esquerda é exatamente isso, com o prefixo comum."),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE, caption = "i = if, t = then, e = else. O prefixo comum 'i E t S' foi para a frente."),
            Predict(
                "Depois da fatoração, ao ver 'if', o analisador sabe qual produção de S usar?",
                listOf("Sim: só uma alternativa de S começa com if", "Não"),
                0,
                "Agora há uma única alternativa com if. A dúvida 'tem else ou não?' fica para S', quando o token else (ou outro) já estiver à vista.",
            ),
            Callout(NO_DUMB_QUESTIONS, "P: A fatoração muda a linguagem? R: Não. Só reorganiza as produções, assim como a(b + c) = ab + ac."),
            Callout(WATCH_OUT, "Fatorar NÃO tira ambiguidade. O else pendurado continua ambíguo: ao ver else, S' ainda poderia escolher 'else S' ou ε. Casar com o if mais próximo continua sendo uma regra extra."),
            Reveal("Na analogia, o que é o 'submenu'?", "O novo não-terminal (S', A'…): guarda os 'restos' depois do prefixo comum, e a decisão fica para depois."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.VISUAL, "Fatorando ao vivo",
        listOf(
            Say(PROF, "Olhe o antes e o depois. Primeiro, o clássico do else."),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE),
            Text("O motor procurou o MAIOR prefixo comum entre as alternativas que começam com o mesmo símbolo: `i E t S`. O que sobrou (ε e `e S`) foi para S'."),
            Visual(VK.DERIVATION, grammar = P1C_ELSE_FAT, input = "i b t a e a", caption = "Derivando com a gramática fatorada."),
            Visual(VK.LL1_TABLE, grammar = P1C_ELSE, caption = "Antes: duas produções disputando a célula [S, i]."),
            Visual(VK.LL1_TABLE, grammar = P1C_ELSE_FAT, caption = "Depois: [S, i] resolvida… mas olhe [S', e]! A ambiguidade do else continua."),
            Predict(
                "Por que [S', e] tem conflito?",
                listOf("Porque e ∈ FIRST(e S) e também e ∈ FOLLOW(S')", "Porque a fatoração foi feita errado"),
                0,
                "S' → e S entra pela coluna e (FIRST), e S' → ε entra pelas colunas de FOLLOW(S'), que inclui e. É a ambiguidade, não a fatoração.",
            ),
            Visual(VK.LEFT_FACTORING, grammar = P1C_STMTS, caption = "Lista de comandos: L -> S ; L | S também precisa de fatoração."),
            Text("Resultado: L → S L' e L' → ; L | ε. Leia: 'um comando e, talvez, um ponto e vírgula com mais comandos'."),
            Reveal("Quando o analisador escolhe L' → ε?", "Quando o próximo token NÃO é ';' — quando ele está em FOLLOW(L'), como o fim da entrada."),
            Visual(VK.LL1_TABLE, grammar = P1C_STMTS_FAT, caption = "Sem conflitos: agora é LL(1)."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.WORKED, "Fatorando passo a passo",
        listOf(
            Say(PROF, "Receita: para cada A, ache o MAIOR prefixo α comum a duas ou mais alternativas. Se α ≠ ε: A → αβ₁ | … | αβₙ | γ vira A → αA' | γ e A' → β₁ | … | βₙ."),
            Code("A -> a b c | a b d | a e | f", "Exercício resolvido"),
            Steps(
                "Resolvido",
                listOf(
                    "1. Alternativas que começam com `a`: a b c, a b d, a e. (f fica de fora: é o γ.)",
                    "2. Maior prefixo comum às TRÊS: só `a` (a e não tem b).",
                    "3. Primeira rodada: A → a A' | f; A' → b c | b d | e.",
                    "4. Repita! Em A', `b c` e `b d` compartilham `b`.",
                    "5. Segunda rodada: A' → b A'' | e; A'' → c | d.",
                    "6. Fim: nenhum par de alternativas do mesmo NT começa igual.",
                ),
            ),
            Visual(VK.LEFT_FACTORING, grammar = "A -> a b c | a b d | a e | f", caption = "O motor repete as rodadas até estabilizar."),
            Text("Sua vez, esmaecido: `C -> id = E | id ( ) | id ( E )`, com `E -> num`."),
            Reveal("Primeira rodada: qual o maior prefixo comum às três?", "Só `id` (o segundo símbolo é `=` numa e `(` nas outras). C → id C'; C' → = E | ( ) | ( E )."),
            Reveal("Segunda rodada, em C'?", "`( )` e `( E )` compartilham `(`. C' → = E | ( C''; C'' → ) | E )."),
            Visual(VK.LEFT_FACTORING, grammar = "C -> id = E | id ( ) | id ( E )\nE -> num", caption = "Confira."),
            Callout(WATCH_OUT, "Não pare na primeira rodada! Depois de fatorar, olhe de novo o novo não-terminal."),
            Callout(EXAM_TIP, "Use o MAIOR prefixo comum a pelo menos duas alternativas. Prefixo menor não é erro, só dá mais rodadas; prefixo que não é comum a todas do grupo, aí sim é erro."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.SOCRATIC, "Por que adiar a decisão?",
        listOf(
            Code("A -> a b | a c"),
            Predict(
                "Um analisador preditivo com 1 token de lookahead vê 'a'. Qual produção ele escolhe?",
                listOf("A -> a b", "A -> a c", "Ainda não dá para saber"),
                2,
                "As duas começam com a. Só o SEGUNDO token decide — e ele ainda não está visível.",
            ),
            Predict(
                "E se ele pudesse olhar 2 tokens à frente?",
                listOf("Ainda não daria", "Daria: o 2º token (b ou c) decide"),
                1,
                "Isso seria LL(2). Mas queremos LL(1), mais simples e rápido. Em vez de olhar mais longe, mudamos a gramática.",
            ),
            Reveal("Como reescrever A para decidir só depois de consumir o 'a'?", "A → a A'; A' → b | c. Ao ver 'a' há uma única opção; b ou c é decidido em A', com o token certo à vista."),
            Predict(
                "Em `A -> a B | a B c`, qual o prefixo comum?",
                listOf("a", "a B", "a B c"),
                1,
                "As duas começam com `a B`; a segunda continua com c, a primeira acaba.",
            ),
            Reveal("Fatore.", "A → a B A'; A' → c | ε. (O ε aparece porque uma alternativa era exatamente o prefixo.)"),
            Predict(
                "Esta gramática tem prefixo comum? `S -> A x | B y`, `A -> c`, `B -> c`",
                listOf("Não: A e B são diferentes", "Sim, escondido: depois de expandir, as duas começam com c"),
                1,
                "A fatoração 'de olho' só vê símbolos iguais escritos. Aqui FIRST(A x) ∩ FIRST(B y) = {c}: conflito LL(1) mesmo sem prefixo literal. Seria preciso substituir A e B primeiro.",
            ),
            Visual(VK.LL1_TABLE, grammar = "S -> A x | B y\nA -> c\nB -> c", caption = "Conflito em [S, c] mesmo sem prefixo literal comum."),
            Reveal("Qual a relação entre fatoração à esquerda e LL(1)?", "A fatoração remove um tipo de conflito (alternativas com o mesmo início), adiando a escolha até haver token suficiente. Junto com eliminar recursão à esquerda, é a transformação típica para tentar deixar uma gramática LL(1)."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.FORMAL, "Algoritmo de fatoração à esquerda",
        listOf(
            Text("**Fatoração à esquerda** é uma transformação de gramática útil para produzir gramáticas adequadas à análise preditiva (descendente). Quando a escolha entre duas alternativas de A não está clara, reescrevemos as produções de A para adiar a decisão até ter visto entrada suficiente."),
            Code(
                """
                Entrada: gramática G.  Saída: gramática equivalente fatorada.
                para cada não-terminal A:
                    encontre o MAIOR prefixo α comum a duas ou mais alternativas
                    se α ≠ ε:
                        substitua  A → αβ₁ | αβ₂ | ... | αβₙ | γ
                        por        A  → αA' | γ
                                   A' → β₁ | β₂ | ... | βₙ
                        (γ = alternativas que não começam com α; A' é novo)
                repita até que nenhum par de alternativas de um NT tenha prefixo comum
                """.trimIndent(),
                "Algoritmo 4.21 (Livro do Dragão)",
            ),
            Text("Exemplo do livro: S → i E t S | i E t S e S | a, E → b. Resultado: S → i E t S S' | a, S' → e S | ε, E → b."),
            Visual(VK.LEFT_FACTORING, grammar = P1C_ELSE),
            Predict(
                "Fatorar `A -> α β1 | α β2` preserva a linguagem?",
                listOf("Sim, sempre", "Não, perde cadeias"),
                0,
                "αA' com A' → β₁ | β₂ gera exatamente αβ₁ e αβ₂ — é a propriedade distributiva.",
            ),
            Predict(
                "Uma gramática fatorada e sem recursão à esquerda é necessariamente LL(1)?",
                listOf("Sim", "Não"),
                1,
                "São condições necessárias, não suficientes. O else pendurado fatorado ainda tem conflito em [S', e]; e uma gramática ambígua nunca é LL(1).",
            ),
            Callout(EXAM_TIP, "Ordem segura numa questão 'torne LL(1)': (1) elimine a recursão à esquerda; (2) fatore à esquerda; (3) calcule FIRST e FOLLOW; (4) monte a tabela e verifique se alguma célula tem mais de uma produção."),
            Callout(WATCH_OUT, "Não confunda: fatoração trata PREFIXOS COMUNS (A → αβ₁ | αβ₂); recursão à esquerda trata A → Aα. Problemas diferentes, receitas diferentes — e ambos atrapalham o LL(1)."),
            Reveal("Escreva a forma geral da fatoração.", "A → αβ₁ | … | αβₙ | γ  ⟹  A → αA' | γ  e  A' → β₁ | … | βₙ."),
        ),
    ),
    Lesson(
        Skill.LEFT_FACTORING, Style.MNEMONIC, "Coloque em evidência",
        listOf(
            Callout(MNEMONIC, "**ab + ac = a(b + c)** — fatoração à esquerda é o 'colocar em evidência' do ensino fundamental, com o prefixo comum no papel do a."),
            Callout(MNEMONIC, "**Primeiro o que é igual, depois o que difere.** A → (parte igual) A'; A' → (as diferenças)."),
            Callout(MNEMONIC, "**Recursão = rabo; fatoração = gêmeos.** Na recursão à esquerda, o NT aparece no início do próprio lado direito. Na fatoração, duas alternativas nascem com a mesma cara."),
            Text("📍 **Palácio da memória**: do outro lado da **Ponte da Transformação**, longe do cachorro, há uma bifurcação com duas placas idênticas 'if E then S…'. O engenheiro junta as duas estradas num trecho único e só coloca a bifurcação depois (S'), onde as placas ficam diferentes."),
            Predict(
                "Qual gramática PRECISA de fatoração (e não de eliminar recursão à esquerda)?",
                listOf("E -> E + T | T", "S -> id ( ) | id = num", "S -> a S b | ε"),
                1,
                "As duas alternativas de S começam com id: gêmeos! A primeira opção é rabo (recursão); a terceira já está boa.",
            ),
            Visual(VK.LEFT_FACTORING, grammar = "S -> id ( ) | id = num", caption = "Gêmeos separados: id em evidência."),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que fatorar resolve ambiguidade (não resolve!); (2) parar na primeira rodada; (3) usar um prefixo que não é comum a todas as alternativas do grupo; (4) esquecer o ε quando uma alternativa é igual ao prefixo (A → αβ | α vira A' → β | ε)."),
            Callout(MNEMONIC, "As duas cirurgias da Ponte antes do Templo LL(1): **'Rabo e Gêmeos'** — corte o rabo (recursão à esquerda) e separe os gêmeos (fatoração)."),
            Reveal("Fatore `A -> x y z | x y` em voz alta.", "Prefixo comum `x y`: A → x y A'; A' → z | ε."),
        ),
    ),
)

internal val p1cLessons: List<Lesson> = p1cLeftRec + p1cFactoring
