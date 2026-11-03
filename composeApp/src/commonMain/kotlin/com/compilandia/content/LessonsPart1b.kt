package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.GrammarBank
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 1b do conteúdo: CFG_DERIVATION, AMBIGUITY (Vale das Gramáticas).

private val p1bExpr: String get() = GrammarBank.exprLeftRec.text
private val p1bAmbig: String get() = GrammarBank.exprAmbiguous.text
private val p1bAnbn: String get() = GrammarBank.anbn.text
private val p1bParens: String get() = GrammarBank.parens.text
private const val P1B_PORTUGUES = "Frase -> SN SV\nSN -> Art Sub\nSV -> Ver SN\nArt -> o | a\nSub -> gato | menina | peixe\nVer -> come | ama"
private const val P1B_ELSE = "S -> i S | i S e S | x"

// ───────────────────────────── CFG_DERIVATION ─────────────────────────────

private val p1bCfg: List<Lesson> = listOf(
    Lesson(
        Skill.CFG_DERIVATION, Style.STORY, "Análise sintática você já fez na escola",
        listOf(
            Say(PROF, "Lembra da aula de português? Sujeito, predicado, objeto direto… Aquilo se chamava **análise sintática**. Literalmente o mesmo nome!"),
            Say(BYTE, "Sério?! Eu odiava aquilo."),
            Say(PROF, "Então vamos odiar juntos — mas entendendo. Uma frase é feita de partes, e cada parte tem uma regra de montagem."),
            Code(P1B_PORTUGUES, "Uma minigramática do português"),
            Text("Cada linha é uma **produção**: 'uma Frase é um sintagma nominal (SN) seguido de um sintagma verbal (SV)'. Quem aparece à esquerda da seta é **não-terminal** (uma categoria). As palavras de verdade — o, gato, come — são **terminais**."),
            Visual(VK.DERIVATION, grammar = P1B_PORTUGUES, input = "o gato come o peixe", caption = "Veja a frase nascer, trocando sempre o não-terminal mais à esquerda."),
            Say(PROF, "Cada passo troca UM não-terminal pelo lado direito de uma de suas produções. Isso é uma **derivação**. Quando sempre trocamos o mais à esquerda, é uma **derivação mais à esquerda**."),
            Predict(
                "Essa gramática gera 'a menina ama o gato'?",
                listOf("Sim", "Não"),
                0,
                "Frase ⇒ SN SV ⇒ Art Sub SV ⇒ a Sub SV ⇒ a menina SV ⇒ a menina Ver SN ⇒ … ⇒ a menina ama o gato. (Ela não liga para concordância de gênero: 'a gato' também sai. Gramática de brinquedo!)",
            ),
            Predict(
                "E 'gato o come peixe o'?",
                listOf("Sim", "Não"),
                1,
                "Todas as palavras existem (léxico ok!), mas a ordem não segue as regras: erro sintático.",
            ),
            Text("Troque palavras por tokens e é igualzinho: `E -> E + T` diz 'uma expressão pode ser uma expressão, um +, e um termo'. O compilador faz análise sintática como você fazia na escola — só que sem preguiça."),
            Callout(NO_DUMB_QUESTIONS, "P: Por que 'livre de contexto'? R: Porque o lado esquerdo é UM não-terminal sozinho: você pode trocar SN por 'Art Sub' em qualquer lugar, não importa o que esteja em volta (o contexto)."),
            Callout(FIRESIDE, "Expressão regular: 'Eu descrevo palavras.' Gramática: 'Eu descrevo frases — e sei aninhar coisas, como parênteses dentro de parênteses, coisa que você não consegue!'"),
            Reveal("Nessa gramática, quem são os terminais e os não-terminais?", "Não-terminais: Frase, SN, SV, Art, Sub, Ver. Terminais: o, a, gato, menina, peixe, come, ama. Símbolo inicial: Frase (o da primeira linha)."),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.VISUAL, "Derivação em câmera lenta",
        listOf(
            Say(PROF, "Vamos ver a gramática de expressões do Livro do Dragão gerando `id + id * id`."),
            Code(p1bExpr, "E = expressão, T = termo, F = fator"),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "id + id * id", caption = "Avance passo a passo e veja a árvore crescer a cada troca."),
            Text("Em cada linha, o não-terminal trocado é o MAIS À ESQUERDA. As formas intermediárias, como `id + T * F`, se chamam **formas sentenciais**. A última, só com terminais, é uma **sentença**."),
            Predict(
                "Na derivação mais à esquerda, depois de `id + T`, qual é o próximo passo?",
                listOf("id + T * F", "id + F", "id + id"),
                0,
                "Ainda precisamos gerar `id * id` depois do +. Se T virasse F direto, o `*` nunca apareceria! Então T → T * F.",
            ),
            Say(BYTE, "E a árvore? É a mesma coisa que a derivação?"),
            Say(PROF, "A árvore é o 'resumo' da derivação: mostra QUAL produção foi usada em cada nó, mas esquece a ORDEM das trocas."),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = "Leia as folhas da esquerda para a direita: dá a sentença."),
            Visual(VK.DERIVATION, grammar = p1bAnbn, input = "a a a b b b", caption = "S -> a S b | ε: cada passo põe um a na frente e um b atrás. Nenhuma ER faz isso!"),
            Reveal("Quantos passos teve a derivação de `a a a b b b`? Por quê?", "4 passos: três vezes S → a S b e uma vez S → ε. Cada a ganha o seu b casado."),
            Visual(VK.DERIVATION, grammar = p1bParens, input = "( ( ) ) ( )", caption = "Parênteses balanceados. Repare como o ε faz S sumir."),
            Reveal("Diferença entre forma sentencial e sentença?", "Forma sentencial: qualquer coisa derivável do símbolo inicial (pode ter não-terminais). Sentença: forma sentencial só com terminais."),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.WORKED, "Derivando à esquerda e à direita",
        listOf(
            Say(PROF, "Exercício: derivação mais à esquerda E mais à direita de `id + id * id`."),
            Code(p1bExpr),
            Steps(
                "Mais à esquerda (⇒lm): troque sempre o NT mais à esquerda",
                listOf("E ⇒ E + T", "⇒ T + T", "⇒ F + T", "⇒ id + T", "⇒ id + T * F", "⇒ id + F * F", "⇒ id + id * F", "⇒ id + id * id ✔ (8 passos)"),
            ),
            Text("Agora mais à direita (⇒rm): troque sempre o NT mais à DIREITA. Eu começo:"),
            Code("E ⇒ E + T\n  ⇒ E + T * F\n  ⇒ E + T * id"),
            Reveal("Próximos dois passos?", "⇒ E + F * id ⇒ E + id * id"),
            Reveal("E os três passos finais?", "⇒ T + id * id ⇒ F + id * id ⇒ id + id * id"),
            Callout(NO_DUMB_QUESTIONS, "P: As duas derivações dão árvores diferentes? R: Não! Mesmas produções, mesma árvore — só muda a ordem das trocas. Cada árvore tem exatamente UMA derivação mais à esquerda e UMA mais à direita."),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = "A árvore comum às duas derivações."),
            Text("Mais um, esmaecido: gramática `S -> a S b | ε`, cadeia `a a b b`."),
            Reveal("Derivação completa?", "S ⇒ a S b ⇒ a a S b b ⇒ a a b b (S → ε no último passo). Com um só NT em cada forma sentencial, mais à esquerda = mais à direita!"),
            Visual(VK.DERIVATION, grammar = p1bAnbn, input = "a a b b", caption = "Confira."),
            Callout(EXAM_TIP, "Em prova, escreva um passo por linha, marque o NT trocado e indique ⇒lm ou ⇒rm. Conferência rápida: nº de passos = nº de nós internos da árvore."),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.SOCRATIC, "O que uma gramática gera?",
        listOf(
            Say(PROF, "Pergunta 1 — tente sem medo."),
            Code("S -> a S b | ε"),
            Predict(
                "Essa gramática gera `a a b`?",
                listOf("Sim", "Não"),
                1,
                "Toda vez que S vira a S b, entram um a E um b. A quantidade de a's e b's é sempre igual.",
            ),
            Predict(
                "Qual é a menor cadeia que ela gera?",
                listOf("ab", "a", "a cadeia vazia ε", "b"),
                2,
                "S → ε direto: zero passos de 'a S b'.",
            ),
            Reveal("Descreva a linguagem inteira em uma frase.", "{ aⁿbⁿ | n ≥ 0 }: n a's seguidos de n b's."),
            Predict(
                "Uma expressão regular consegue descrever aⁿbⁿ?",
                listOf("Sim: a*b*", "Não"),
                1,
                "a*b* aceita aab, com contagens diferentes. Nenhuma ER consegue: seria preciso memória ilimitada. Gramáticas livres de contexto são mais poderosas.",
            ),
            Say(BYTE, "Então gramática é tipo uma ER turbinada?"),
            Say(PROF, "Pode pensar assim: toda linguagem regular é livre de contexto, mas não o contrário. Pergunta 2:"),
            Code(p1bExpr),
            Predict(
                "Quantos não-terminais essa gramática tem?",
                listOf("3", "5", "8"),
                0,
                "E, T e F — os que aparecem do lado esquerdo.",
            ),
            Predict(
                "E quantos terminais?",
                listOf("3", "5", "8"),
                1,
                "+, *, (, ) e id.",
            ),
            Reveal("Por que numa derivação precisamos escolher QUAL não-terminal trocar?", "Porque uma forma sentencial pode ter vários NTs. Fixar uma ordem (sempre o mais à esquerda, ou sempre o mais à direita) deixa a derivação única para cada árvore. Os analisadores LL constroem a mais à esquerda; os LR, a mais à direita ao contrário."),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "( id ) * id", caption = "Derive você mesmo antes de avançar."),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.FORMAL, "GLC e derivações: definição formal",
        listOf(
            Text("**Gramática livre de contexto** G = (N, T, P, S): N, conjunto de não-terminais (variáveis sintáticas); T, conjunto de terminais (os tokens); P, conjunto de produções A → α, com A ∈ N e α ∈ (N ∪ T)*; S ∈ N, o símbolo inicial."),
            Text("**Derivação em um passo**: se A → γ ∈ P, então αAβ ⇒ αγβ. ⇒* significa 'zero ou mais passos'; ⇒⁺, 'um ou mais passos'."),
            Text("Se S ⇒* α, então α é uma **forma sentencial** de G; se α só tem terminais, é uma **sentença**. A **linguagem gerada** é L(G) = { w ∈ T* | S ⇒⁺ w }."),
            Text("**Derivação mais à esquerda** (⇒lm): sempre substitui o não-terminal mais à esquerda. **Mais à direita** (⇒rm, ou canônica): sempre o mais à direita."),
            Text("**Árvore de derivação**: raiz rotulada S; nós internos rotulados por NTs; se um nó A tem filhos X₁…Xₙ, então A → X₁…Xₙ ∈ P; folhas são terminais ou ε. As folhas lidas da esquerda para a direita formam o **produto** (fronteira) da árvore."),
            Code(
                """
                Convenções (Livro do Dragão):
                  terminais:            a, b, c, +, *, (, ), id, if ...
                  não-terminais:        A, B, C, S, E, T, F ...
                  símbolos quaisquer:   X, Y, Z
                  cadeias de terminais: u, v, w, x, y, z
                  formas sentenciais:   α, β, γ
                  A → α₁ | α₂ | ... | αₖ   (alternativas de A)
                """.trimIndent(),
            ),
            Predict(
                "Se S ⇒* `id + T`, então `id + T` é…",
                listOf("uma sentença", "uma forma sentencial (não sentença)", "uma produção"),
                1,
                "Ainda tem o não-terminal T: é forma sentencial, mas não sentença.",
            ),
            Predict(
                "Cada árvore de derivação corresponde a quantas derivações mais à esquerda?",
                listOf("Exatamente uma", "Várias", "Nenhuma"),
                0,
                "A árvore fixa as produções; a regra 'sempre o mais à esquerda' fixa a ordem. Resultado: uma única derivação ⇒lm.",
            ),
            Callout(EXAM_TIP, "Questão tradicional: 'dada G, mostre uma derivação mais à esquerda (ou à direita) de w e a árvore'. Erro que custa pontos: numa ⇒lm, trocar um NT que NÃO é o mais à esquerda."),
            Visual(VK.DERIVATION, grammar = p1bExpr, input = "id * ( id + id )", caption = "Pratique: derivação ⇒lm de id * ( id + id )."),
            Reveal("Escreva G = (N, T, P, S) para `S -> a S b | ε`.", "N = {S}; T = {a, b}; P = {S → a S b, S → ε}; símbolo inicial S."),
        ),
    ),
    Lesson(
        Skill.CFG_DERIVATION, Style.MNEMONIC, "As quatro peças da gramática",
        listOf(
            Callout(MNEMONIC, "**G = (N, T, P, S)** → '**N**unca **T**ire **P**rova **S**em estudar': Não-terminais, Terminais, Produções, Símbolo inicial."),
            Callout(MNEMONIC, "**Terminal termina**: é folha, não se expande mais. O não-terminal ainda 'não terminou' — vai ser trocado."),
            Callout(MNEMONIC, "Derivação mais à esquerda = **ler um livro**: você resolve as coisas na ordem em que lê, da esquerda para a direita."),
            Text("📍 **Palácio da memória**: o **Vale das Gramáticas**. As produções são sementes: um não-terminal plantado (S) brota galhos (o lado direito) até virar só folhas (terminais). A árvore de derivação é, literalmente, uma árvore que cresceu no vale."),
            Callout(MNEMONIC, "Forma **sentencial** = prédio em obra (ainda tem andaime, os NTs). **Sentença** = prédio pronto, só terminais."),
            Predict(
                "Numa árvore de derivação, os terminais ficam…",
                listOf("na raiz", "nos nós internos", "nas folhas"),
                2,
                "Terminal termina: é sempre folha. Nós internos são não-terminais.",
            ),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que a árvore muda entre ⇒lm e ⇒rm (não muda!); (2) confundir derivação (de S até w) com redução (o caminho inverso, usado no LR); (3) esquecer que ε é uma folha legítima."),
            Visual(VK.PARSE_TREE, grammar = p1bAnbn, input = "a a b b", caption = "Folhas da esquerda para a direita = a sentença."),
            Reveal("Recite: o que significam N, T, P, S, ⇒, ⇒* e ⇒lm?", "Não-terminais, Terminais, Produções, Símbolo inicial; ⇒ é um passo; ⇒* é zero ou mais passos; ⇒lm é um passo trocando o NT mais à esquerda."),
        ),
    ),
)

// ───────────────────────────── AMBIGUITY ─────────────────────────────

private val p1bAmbiguity: List<Lesson> = listOf(
    Lesson(
        Skill.AMBIGUITY, Style.STORY, "O policial e o binóculo",
        listOf(
            Say(NARRADOR, "Manchete do jornal: 'Policial vê ladrão com binóculo.'"),
            Say(BYTE, "Peraí: quem estava com o binóculo? O policial ou o ladrão?"),
            Say(PROF, "Exatamente! A frase tem DUAS árvores: [vê [ladrão com binóculo]] ou [[vê ladrão] com binóculo]. Mesmas palavras, estrutura diferente, sentido diferente. Isso é **ambiguidade**."),
            Text("Em português, a gente desempata pelo contexto. Um compilador não tem esse 'bom senso': se a gramática permite duas árvores, ele não sabe qual escolher — e cada árvore pode significar um programa diferente!"),
            Predict(
                "Quanto vale 2 + 3 * 4?",
                listOf("14", "20", "Depende da árvore"),
                2,
                "Na matemática da escola, 14. Mas se a gramática permitir a árvore (2 + 3) * 4, dá 20. Quem decide o significado é a árvore!",
            ),
            Code(p1bAmbig, "Curta e bonita… mas ambígua"),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id", caption = "Duas árvores para a mesma frase: gramática ambígua."),
            Say(PROF, "Na árvore em que o * fica mais embaixo, ele é calculado primeiro: 14. Na outra, o + fica embaixo: 20."),
            Callout(REAL_WORLD, "Calculadora simples de camelô faz 2 + 3 × 4 = 20 (vai calculando conforme você digita). A científica dá 14. São 'gramáticas' diferentes!"),
            Say(BYTE, "E como resolve?"),
            Say(PROF, "Dois jeitos: reescrever a gramática com níveis de precedência (E, T, F) ou dizer ao gerador de analisadores quem tem prioridade (`%left` no Bison)."),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id * id", caption = "Gramática E/T/F: uma única árvore, com o * embaixo."),
            Reveal("Defina ambiguidade com suas palavras.", "Uma gramática é ambígua se existe pelo menos UMA sentença com duas (ou mais) árvores de derivação diferentes."),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.VISUAL, "Caçando árvores gêmeas",
        listOf(
            Say(PROF, "O widget procura todas as árvores para a entrada. Se achar duas, mostra as duas."),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id"),
            Text("Olhe a RAIZ de cada árvore: numa, a primeira produção é E → E + E; na outra, E → E * E. Raízes diferentes = estruturas diferentes."),
            Predict(
                "E `id + id + id`, nessa mesma gramática?",
                listOf("Uma árvore", "Duas árvores"),
                1,
                "(id + id) + id ou id + (id + id). Com + o valor coincide, mas com - não: (5 - 3) - 1 ≠ 5 - (3 - 1). É um problema de **associatividade**.",
            ),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id + id", caption = "Associatividade: quem se junta primeiro?"),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id + id + id", caption = "Na gramática E/T/F a recursão à esquerda força (id + id) + id."),
            Say(BYTE, "E o famoso 'else pendurado'?"),
            Code(P1B_ELSE, "i = 'if (cond) then', e = 'else', x = um comando qualquer"),
            Visual(VK.PARSE_TREE, grammar = P1B_ELSE, input = "i i x e x", caption = "O else pertence ao primeiro if ou ao segundo?"),
            Callout(REAL_WORLD, "C, Java e companhia decidem: o else casa com o if **mais próximo** que ainda não tem else. O Yacc/Bison implementa isso preferindo shift no conflito."),
            Reveal("Por que ambiguidade é um problema para um compilador?", "Porque cada árvore pode ter um significado diferente (14 ou 20; o else de um if ou do outro). O compilador precisa de UMA tradução determinística."),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.WORKED, "Provando que uma gramática é ambígua",
        listOf(
            Say(PROF, "Para PROVAR ambiguidade, basta exibir UMA sentença com duas derivações mais à esquerda diferentes. Vamos lá."),
            Code(p1bAmbig),
            Steps("Derivação mais à esquerda nº 1 (+ na raiz)", listOf("E ⇒ E + E", "⇒ id + E", "⇒ id + E * E", "⇒ id + id * E", "⇒ id + id * id")),
            Steps("Derivação mais à esquerda nº 2 (* na raiz)", listOf("E ⇒ E * E", "⇒ E + E * E", "⇒ id + E * E", "⇒ id + id * E", "⇒ id + id * id")),
            Text("Duas derivações mais à esquerda distintas para a mesma sentença ⇒ duas árvores ⇒ **gramática ambígua**. ∎"),
            Callout(WATCH_OUT, "Uma derivação ⇒lm e outra ⇒rm diferentes NÃO provam nada: a mesma árvore tem as duas! Compare sempre lm com lm (ou rm com rm), ou compare árvores."),
            Text("Sua vez, esmaecido: mostre que `id * id + id` também tem duas árvores."),
            Reveal("Primeira derivação ⇒lm (+ na raiz)?", "E ⇒ E + E ⇒ E * E + E ⇒ id * E + E ⇒ id * id + E ⇒ id * id + id"),
            Reveal("Segunda derivação ⇒lm (* na raiz)?", "E ⇒ E * E ⇒ id * E ⇒ id * E + E ⇒ id * id + E ⇒ id * id + id"),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id * id + id", caption = "Confira: duas árvores."),
            Reveal("Qual das duas respeita a precedência usual?", "A com + na raiz: (id * id) + id — o * fica embaixo e é calculado primeiro."),
            Callout(EXAM_TIP, "Roteiro de resposta: (1) escolha a sentença; (2) mostre duas derivações MAIS À ESQUERDA (ou duas árvores); (3) conclua. Para desambiguar, apresente E → E + T | T, T → T * F | F, F → ( E ) | id e explique: um nível por precedência; recursão à esquerda = associatividade à esquerda."),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.SOCRATIC, "A culpa é da gramática ou da linguagem?",
        listOf(
            Predict(
                "Se UMA única sentença tem duas árvores, a gramática é ambígua?",
                listOf("Sim, basta uma", "Não, precisa ser a maioria", "Só se todas tiverem"),
                0,
                "Basta um contraexemplo. Uma única sentença com duas árvores já torna a gramática ambígua.",
            ),
            Predict(
                "`E -> E + E | E * E | ( E ) | id` e a gramática E/T/F geram a mesma linguagem?",
                listOf("Sim", "Não"),
                0,
                "Ambas geram todas as expressões com +, *, parênteses e id. Uma é ambígua; a outra, não.",
            ),
            Say(PROF, "Então…"),
            Reveal("Ambiguidade é propriedade da gramática ou da linguagem?", "Da GRAMÁTICA, em geral: a mesma linguagem pode ter gramáticas ambíguas e não ambíguas. (Existem linguagens 'inerentemente ambíguas', em que TODA gramática é ambígua — raras, e não aparecem em linguagens de programação.)"),
            Predict(
                "Na gramática E/T/F, por que o * sempre fica mais embaixo na árvore?",
                listOf("Porque * só aparece em T, um nível abaixo de E", "Por sorte", "Porque * é escrito primeiro"),
                0,
                "Para chegar a um *, você precisa passar por T; e T só aparece abaixo de E. Cada nível da gramática é um nível de precedência.",
            ),
            Visual(VK.PARSE_TREE, grammar = p1bExpr, input = "id * id + id"),
            Predict(
                "E por que `id + id + id` agrupa à esquerda em E → E + T?",
                listOf("Porque a recursão está à esquerda: só o E da esquerda pode conter outro +", "Porque o analisador lê da esquerda"),
                0,
                "O operando da direita é um T, que não contém + (sem parênteses). Então o + 'extra' só cabe na subárvore da esquerda.",
            ),
            Say(BYTE, "Existe algum programa que diz se QUALQUER gramática é ambígua?"),
            Predict(
                "Existe um algoritmo que decide, para qualquer GLC, se ela é ambígua?",
                listOf("Sim", "Não: é um problema indecidível"),
                1,
                "Indecidível! Por isso usamos condições suficientes: se a gramática é LL(1) ou LR(1), com certeza não é ambígua.",
            ),
            Reveal("Duas formas de lidar com uma gramática ambígua.", "1) Reescrever a gramática (níveis de precedência; else casando com o if mais próximo). 2) Manter a ambígua e dar regras de desambiguação ao gerador (no Bison: %left, %right, %nonassoc e preferência por shift)."),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.FORMAL, "Ambiguidade: definição e desambiguação",
        listOf(
            Text("**Definição.** Uma gramática G é **ambígua** se existe alguma sentença w ∈ L(G) com mais de uma árvore de derivação. Equivalentemente: w tem mais de uma derivação mais à esquerda, ou mais de uma derivação mais à direita."),
            Text("Uma linguagem é **inerentemente ambígua** se toda GLC que a gera é ambígua. Exemplo: { aⁱbʲcᵏ | i = j ou j = k }."),
            Text("**Fato.** Não existe algoritmo que decida se uma GLC arbitrária é ambígua (o problema é indecidível)."),
            Code(
                """
                Desambiguação de expressões (um nível por precedência):
                  E → E + T | T        (+ : menor precedência, assoc. à esquerda)
                  T → T * F | F        (* : maior precedência, assoc. à esquerda)
                  F → ( E ) | id       (átomos)
                Regras:
                  operador de MENOR precedência fica mais perto da raiz;
                  recursão à ESQUERDA → associatividade à esquerda;
                  recursão à DIREITA  → associatividade à direita (ex.: ^).
                """.trimIndent(),
            ),
            Code(
                "stmt → matched_stmt | open_stmt\nmatched_stmt → if ( expr ) matched_stmt else matched_stmt | other\nopen_stmt → if ( expr ) stmt\n          | if ( expr ) matched_stmt else open_stmt",
                "Else pendurado sem ambiguidade (Livro do Dragão, §4.3.2)",
            ),
            Predict(
                "Para provar que G é ambígua, é suficiente mostrar…",
                listOf("uma derivação ⇒lm e outra ⇒rm diferentes para w", "duas derivações ⇒lm diferentes para a mesma w", "que G tem recursão à esquerda"),
                1,
                "Duas ⇒lm distintas = duas árvores distintas. Uma ⇒lm e uma ⇒rm podem ser da mesma árvore; e recursão à esquerda não tem nada a ver com ambiguidade.",
            ),
            Predict(
                "Toda gramática LL(1) ou LR(1) é…",
                listOf("ambígua", "não ambígua", "depende da entrada"),
                1,
                "Um analisador determinístico constrói UMA árvore por entrada; se a tabela não tem conflitos, a gramática não é ambígua.",
            ),
            Callout(EXAM_TIP, "Clássico: 'Mostre que a gramática do if-then-else é ambígua usando `if E1 then if E2 then S1 else S2`' e 'dê uma gramática equivalente não ambígua'. Decore a ideia matched/open: entre um then e um else só pode vir comando 'casado'."),
            Visual(VK.PARSE_TREE, grammar = P1B_ELSE, input = "i i x e x", caption = "O contraexemplo clássico, em miniatura."),
            Reveal("Por que recursão à esquerda dá associatividade à esquerda?", "Em E → E + T, o operando que pode conter outros + é o da ESQUERDA. Em a + b + c, o a + b vira a subárvore da esquerda: (a + b) + c."),
        ),
    ),
    Lesson(
        Skill.AMBIGUITY, Style.MNEMONIC, "Duas árvores, um problema",
        listOf(
            Callout(MNEMONIC, "**Ambi = dois.** Ambidestro usa as duas mãos; gramática ambígua tem DUAS árvores para a mesma frase."),
            Callout(MNEMONIC, "Prova de ambiguidade = **'duas da mesma mão'**: duas mais à esquerda (ou duas mais à direita). Uma de cada mão não vale!"),
            Callout(MNEMONIC, "**Precedência = profundidade.** Quem tem MAIS precedência fica MAIS FUNDO na árvore (é calculado antes). O + fica perto da raiz; o * mais embaixo."),
            Callout(MNEMONIC, "**A recursão aponta a associatividade.** E → **E** + T: recursão à esquerda → associa à esquerda. P → B ^ **P**: recursão à direita → associa à direita."),
            Text("📍 **Palácio da memória**: no Vale das Gramáticas há duas árvores gêmeas plantadas lado a lado, com a mesma placa: `id + id * id`. É o minijogo **Duas Árvores**."),
            Callout(MNEMONIC, "**Else pendurado casa com o vizinho**: o else fica com o if mais próximo que ainda está sem par."),
            Predict(
                "Em `2 ^ 3 ^ 2` (potência, associativa à direita), qual o agrupamento correto?",
                listOf("(2 ^ 3) ^ 2 = 64", "2 ^ (3 ^ 2) = 512"),
                1,
                "Associativa à direita: o de mais à direita se junta primeiro. 3² = 9, e 2⁹ = 512.",
            ),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que ambiguidade é da linguagem — quase sempre é da gramática; (2) achar que recursão à esquerda causa ambiguidade — E → E + T é recursiva à esquerda e NÃO é ambígua; (3) achar que dá para testar ambiguidade automaticamente sempre (é indecidível)."),
            Visual(VK.PARSE_TREE, grammar = p1bAmbig, input = "id + id * id", caption = "As gêmeas: mesma placa, estruturas diferentes."),
            Reveal("Recite os ganchos: ambi, mesma mão, profundidade, vizinho.", "Ambígua = duas árvores; prova com duas derivações da mesma mão (lm com lm); maior precedência = mais fundo; o else casa com o if vizinho."),
        ),
    ),
)

internal val p1bLessons: List<Lesson> = p1bCfg + p1bAmbiguity
