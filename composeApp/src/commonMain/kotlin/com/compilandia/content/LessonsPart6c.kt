package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 6c do conteúdo: REG_ALLOC (Forja do Código — Coloração de Registradores).
// Números conferidos com RegAlloc (liveness, interference, chromatic, color, mips, memoryAccesses) e Mips.fromTac:
//  "a * b + c * d": grafo t1—t2 (t3 isolado), χ = 2; lw+sw: 11 (ingênuo) → 5 (k = 2); k = 1 derrama t1 → 7.
//  "( a + b ) * ( c - d )": mesmo formato, χ = 2, 11 → 5.
//  "a + b + c + d": nenhuma aresta, χ = 1, 11 → 5.
//  "a * b + ( c * d + a * c )": triângulo t1, t2, t3 (+ t1—t4), χ = 3; 17 (ingênuo) → 7 (k = 3); k = 2 derrama t1 → 9.
// Todos os programas completos (com main:) rodam no MipsVM (a=4, b=2, c=3, d=5).

/** a * b + c * d no estilo ingênuo (Mips.fromTac): todo temporário mora na memória. */
private val P6_RA_NAIVE: String = p6Asm(
    """
    # r = a * b + c * d    (TAC: t1 = a * b ; t2 = c * d ; t3 = t1 + t2 ; r = t3)
    # versão INGÊNUA: 11 acessos à memória (7 lw + 4 sw) no cálculo
            .data
    a:      .word 4
    b:      .word 2
    c:      .word 3
    d:      .word 5
    t1:     .word 0
    t2:     .word 0
    t3:     .word 0
    r:      .word 0
            .text
    main:
            lw   §t1, a
            lw   §t2, b
            mul  §t3, §t1, §t2
            sw   §t3, t1          # guarda t1 na memória...
            lw   §t1, c
            lw   §t2, d
            mul  §t3, §t1, §t2
            sw   §t3, t2
            lw   §t1, t1          # ...e busca de volta logo depois
            lw   §t2, t2
            add  §t3, §t1, §t2
            sw   §t3, t3
            lw   §t0, t3
            sw   §t0, r
            li   §v0, 1           # imprime r
            lw   §a0, r
            syscall
            li   §v0, 10
            syscall
    """,
)

/** O mesmo cálculo com os temporários em registradores (RegAlloc.mips com k = 2). */
private val P6_RA_ALLOC: String = p6Asm(
    """
    # r = a * b + c * d    com alocação de registradores (k = 2)
    # t1 → §s1   t2 → §s0   t3 → §s0   (t2 morre quando t3 nasce: dividem a cor)
    # 5 acessos à memória (4 lw + 1 sw) no cálculo
            .data
    a:      .word 4
    b:      .word 2
    c:      .word 3
    d:      .word 5
    r:      .word 0
            .text
    main:
            lw   §t8, a           # §t8 e §t9: rascunho para operandos da memória
            lw   §t9, b
            mul  §s1, §t8, §t9    # t1 = a * b
            lw   §t8, c
            lw   §t9, d
            mul  §s0, §t8, §t9    # t2 = c * d
            add  §s0, §s1, §s0    # t3 = t1 + t2
            sw   §s0, r           # r = t3
            li   §v0, 1           # imprime r
            lw   §a0, r
            syscall
            li   §v0, 10
            syscall
    """,
)

/** a * b + ( c * d + a * c ) com só 2 registradores: t1 é derramado (spill). */
private val P6_RA_SPILL: String = p6Asm(
    """
    # r = a * b + ( c * d + a * c )   com k = 2: t1 vai para a memória (spill)
    # t2 → §s1   t3, t4, t5 → §s0   t1 → memória
    # 9 acessos (7 lw + 2 sw); com k = 3 seriam 7; no ingênuo, 17
            .data
    a:      .word 4
    b:      .word 2
    c:      .word 3
    d:      .word 5
    t1:     .word 0           # o derramado precisa de lugar na memória
    r:      .word 0
            .text
    main:
            lw   §t8, a
            lw   §t9, b
            mul  §t8, §t8, §t9    # t1 = a * b ...
            sw   §t8, t1          # ... e derrama: guarda na memória
            lw   §t8, c
            lw   §t9, d
            mul  §s1, §t8, §t9    # t2 = c * d
            lw   §t8, a
            lw   §t9, c
            mul  §s0, §t8, §t9    # t3 = a * c
            add  §s0, §s1, §s0    # t4 = t2 + t3
            lw   §t8, t1          # recarrega o derramado na hora do uso
            add  §s0, §t8, §s0    # t5 = t1 + t4
            sw   §s0, r
            li   §v0, 1           # imprime r (35)
            lw   §a0, r
            syscall
            li   §v0, 10
            syscall
    """,
)

/** Convenção MIPS: o chamado salva e restaura os §s que usar. */
private val P6_RA_CALL: String = p6Asm(
    """
    # Convenção: §s0..§s7 são SALVOS PELO CHAMADO; §t0..§t9 não (o chamador que se cuide)
            .data
    x:      .word 0
            .text
    main:
            li   §s0, 7           # main guarda um valor importante em §s0
            li   §a0, 5
            jal  dobro            # chama dobro(5)
            add  §s0, §s0, §v0    # 7 + 10: §s0 ainda vale 7!
            sw   §s0, x
            li   §v0, 1
            move §a0, §s0         # imprime 17
            syscall
            li   §v0, 10
            syscall
    dobro:
            addi §sp, §sp, -4
            sw   §s0, 0(§sp)      # vai usar §s0? então SALVA antes...
            add  §s0, §a0, §a0
            move §v0, §s0
            lw   §s0, 0(§sp)      # ... e RESTAURA antes de voltar
            addi §sp, §sp, 4
            jr   §ra
    """,
)

private val P6_RA_LIVE_TABLE: String = """
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
""".trimIndent()

internal val p6RegLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.STORY, "Poucas bancadas na cozinha",
        listOf(
            Say(NARRADOR, "Cozinha de restaurante em dia de casamento. A despensa no subsolo guarda tudo, mas cada viagem de escada leva uma eternidade. Lá em cima há só três bancadas."),
            Say(BYTE, "É a cozinha da aula de MIPS! Bancada é registrador, despensa é memória."),
            Say(PROF, "Isso. E o chef novo faz uma burrice: termina o molho, LEVA para a despensa, e cinco minutos depois desce de novo para BUSCAR o mesmo molho. É o código ingênuo: `sw` seguido de `lw` do mesmo temporário."),
            Say(PROF, "O chef experiente pensa diferente: 'O molho vai ser usado logo, fica numa bancada. Quando o molho for usado pela última vez, a bancada fica LIVRE para outra preparação.'"),
            Steps(
                "Cozinha → alocação de registradores",
                listOf(
                    "🍲 Cada preparação intermediária (molho, massa) é um **temporário** (t1, t2, ...).",
                    "🪵 Cada bancada é um **registrador**. Só há k bancadas.",
                    "⏳ Do momento em que a preparação fica pronta até seu ÚLTIMO uso, ela está **viva** e ocupa uma bancada: esse é o seu **intervalo de vida**.",
                    "🚫 Duas preparações vivas AO MESMO TEMPO não podem dividir bancada: elas **interferem**.",
                    "🎨 Dar uma bancada a cada preparação, sem que vizinhas briguem, é **colorir um grafo**: cada cor é um registrador.",
                    "🪣 Se não houver bancadas suficientes, alguma preparação desce para a despensa e é buscada de novo quando precisar: é o **spill** (derramamento).",
                ),
            ),
            Callout(REAL_WORLD, "É o mesmo problema de pintar o mapa do Brasil: estados que fazem fronteira não podem ter a mesma cor. Na alocação, a 'fronteira' é estar vivo ao mesmo tempo. Quatro cores bastam para qualquer mapa plano, mas o grafo de um programa não é um mapa: às vezes precisa de mais."),
            Predict(
                "Em r = a * b + c * d: t1 = a * b fica pronto; depois t2 = c * d; depois t3 = t1 + t2. Quais temporários precisam de bancadas diferentes?",
                listOf("t1 e t2", "t1 e t3", "t2 e t3", "Todos os três"),
                0,
                "Quando t2 fica pronto, t1 ainda espera para ser usado: os dois estão vivos juntos. Já t3 nasce exatamente quando t1 e t2 morrem (são usados pela última vez), então t3 pode reaproveitar a bancada de qualquer um deles.",
            ),
            Visual(VK.REGALLOC, input = "a * b + c * d", caption = "Veja os vivos, o grafo (só uma aresta: t1—t2) e a coloração com 2 registradores."),
            Reveal("Quantas viagens à despensa (lw + sw) o código ingênuo faz nesse cálculo, e quantas depois da alocação?", "Ingênuo: 11 (7 lw + 4 sw). Com 2 registradores: 5 (4 lw das variáveis a, b, c, d + 1 sw do resultado r). Menos da metade!"),
            Callout(NO_DUMB_QUESTIONS, "P: Por que não usar logo um registrador diferente para cada temporário? R: Porque um programa de verdade tem milhares de temporários e o MIPS tem 32 registradores (e vários reservados). Reaproveitar bancadas é obrigatório."),
            Reveal("Na analogia, quem é: bancada, preparação, preparações que brigam, descer para a despensa?", "Registrador, temporário, temporários que interferem (vivos ao mesmo tempo, aresta no grafo), spill para a memória."),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.VISUAL, "Antes e depois, no MARS de bolso",
        listOf(
            Say(PROF, "Vamos ver os quatro passos da alocação em widgets: vivacidade, grafo, coloração e o MIPS final."),
            Visual(VK.REGALLOC, input = "a * b + c * d", caption = "Siga a coluna dos vivos de BAIXO para CIMA: é assim que a análise anda."),
            Text("Primeiro, o código ingênuo, do jeito que o gerador da aula de MIPS faz. Rode e conte os `lw` e `sw`."),
            Visual(VK.MIPS_RUN, grammar = P6_RA_NAIVE, caption = "Ingênuo: 11 acessos à memória no cálculo. Imprime 23."),
            Visual(VK.MIPS_RUN, grammar = P6_RA_ALLOC, caption = "Alocado: 5 acessos. Mesmo resultado, 23."),
            Predict(
                "No código alocado, por que t2 e t3 podem ficar os dois em \$s0?",
                listOf("Sorte", "Porque t2 é usado pela última vez exatamente na instrução que define t3: eles nunca estão vivos ao mesmo tempo", "Porque são do mesmo tipo", "Porque \$s0 é especial"),
                1,
                "t2 morre em 't3 = t1 + t2' e t3 nasce ali mesmo. Sem sobreposição, sem aresta no grafo, mesma cor.",
            ),
            Visual(VK.REGALLOC, input = "a + b + c + d", caption = "Cadeia: cada temporário morre quando o próximo nasce. Grafo sem arestas: 1 registrador basta!"),
            Visual(VK.REGALLOC, input = "a * b + ( c * d + a * c )", caption = "Agora há 3 temporários vivos ao mesmo tempo. Procure o triângulo no grafo."),
            Predict(
                "Com o triângulo t1—t2—t3, qual o menor número de registradores sem spill?",
                listOf("1", "2", "3", "5"),
                2,
                "Num triângulo, cada vértice é vizinho dos outros dois: três cores diferentes são obrigatórias. χ = 3.",
            ),
            Visual(VK.MIPS_RUN, grammar = P6_RA_SPILL, caption = "E se só houver 2 registradores? t1 é derramado: sw depois de calculado, lw na hora do uso. 9 acessos (com 3 registradores seriam 7; o ingênuo faz 17). Imprime 35."),
            Callout(BRAIN_POWER, "Repare que o derramado escolhido foi t1, o que vive MAIS tempo e tem mais vizinhos. Por que essa é uma boa escolha para mandar para a despensa?"),
            Reveal("Resposta", "Tirar t1 do grafo remove 3 arestas de uma vez e quebra o triângulo; o resto passa a caber em 2 cores. E, como t1 é usado só uma vez depois de definido, o custo é um único sw + um único lw."),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.WORKED, "Vivacidade, grafo e cores, à mão",
        listOf(
            Say(PROF, "Exercício completo: r = a * b + ( c * d + a * c ). Primeiro o TAC, depois a vivacidade de trás para frente, depois o grafo e a coloração."),
            Code(P6_RA_LIVE_TABLE, "Vivacidade de baixo para cima e o grafo de interferência"),
            Steps(
                "Como a tabela foi preenchida",
                listOf(
                    "Comece no FIM: depois da última instrução, nada está vivo (out = { }). As variáveis a..d e r ficam na memória, então só contamos temporários.",
                    "i=5 (r = t5): usa t5 → in = {t5}.",
                    "i=4 (t5 = t1 + t4): out = {t5}; tira o def t5 e põe os usos t1, t4 → in = {t1, t4}.",
                    "i=3 (t4 = t2 + t3): out = {t1, t4}; tira t4, põe t2, t3 → in = {t1, t2, t3}.",
                    "i=2 (t3 = a * c): out = {t1, t2, t3}; tira t3 → in = {t1, t2}. Aqui há 3 vivos juntos: o pico.",
                    "Arestas: em cada instrução, o temporário DEFINIDO liga-se a todos os outros do out. Isso dá t2—t1, t3—t1, t3—t2, t4—t1.",
                ),
            ),
            Steps(
                "Chaitin com k = 3 (simplificar, empilhar, colorir)",
                listOf(
                    "Graus: t1 = 3, t2 = 2, t3 = 2, t4 = 1, t5 = 0. Só tira quem tem grau < 3.",
                    "Tira t2 (grau 2) → empilha. Agora t1 tem grau 2: tira t1. Depois t3, t4, t5 (graus 0).",
                    "Pilha (topo primeiro): t5, t4, t3, t1, t2.",
                    "Desempilha e dá a menor cor livre: t5 → 0, t4 → 0, t3 → 0, t1 → 1 (vizinhos t3, t4 têm 0), t2 → 2 (vizinhos t1 = 1, t3 = 0).",
                    "Resultado: \$s0 = {t3, t4, t5}, \$s1 = t1, \$s2 = t2. Nenhum spill.",
                ),
            ),
            Visual(VK.REGALLOC, input = "a * b + ( c * d + a * c )", caption = "Confira os vivos, o grafo, χ = 3 e o MIPS. Acessos à memória: 17 no ingênuo, 7 alocado."),
            Callout(WATCH_OUT, "Erro clássico: fazer a vivacidade de CIMA para BAIXO. Não dá: para saber se t1 está vivo agora, você precisa saber se ele será usado DEPOIS. A informação vem do futuro, então a análise anda para trás."),
            Text("Agora é com você (exemplo esmaecido): **r = ( a + b ) * ( c - d )**. TAC: t1 = a + b; t2 = c - d; t3 = t1 * t2; r = t3."),
            Reveal("Calcule out e in de cada instrução, de baixo para cima.", "i=3 r = t3: out { }, in {t3}. i=2 t3 = t1 * t2: out {t3}, in {t1, t2}. i=1 t2 = c - d: out {t1, t2}, in {t1}. i=0 t1 = a + b: out {t1}, in { }."),
            Reveal("Quais as arestas e quantos registradores são necessários?", "Só t2—t1 (em i=1, t2 é definido com t1 vivo). t3 fica sozinho. χ = 2: por exemplo, t1 → \$s1, t2 e t3 → \$s0."),
            Reveal("Quantos lw + sw antes e depois?", "Ingênuo: 11. Alocado com 2 registradores: 5 (lw de a, b, c, d e sw de r)."),
            Visual(VK.REGALLOC, input = "( a + b ) * ( c - d )", caption = "Confira suas respostas."),
            Callout(EXAM_TIP, "Na prova, monte SEMPRE a tabela com colunas use | def | out | in, preenchendo de baixo para cima. Depois desenhe o grafo e só então colora. A tabela bem feita já vale boa parte da questão."),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.SOCRATIC, "Quando dois valores podem dividir um registrador?",
        listOf(
            Say(PROF, "Hoje você vai reinventar a alocação por coloração. Primeira pergunta, a mais básica de todas."),
            Predict(
                "Por que o compilador se esforça para manter valores em registradores?",
                listOf("Porque a memória pode apagar dados", "Porque acessar a memória (lw/sw) é muito mais lento que usar um registrador", "Porque o MIPS proíbe lw", "Para economizar espaço na .data"),
                1,
                "Operação entre registradores leva um ciclo; uma ida à memória pode custar dezenas ou centenas de ciclos se o dado não estiver na cache. Cortar lw/sw é cortar tempo.",
            ),
            Say(BYTE, "Mas registrador é pouco. Então dois valores vão ter que dividir um?"),
            Predict(
                "Quando dois temporários x e y podem usar o MESMO registrador?",
                listOf("Quando têm nomes parecidos", "Quando nunca estão vivos ao mesmo tempo", "Quando são usados na mesma instrução", "Nunca"),
                1,
                "Se y só passa a existir depois do último uso de x, x não precisa mais do registrador. É a ideia toda.",
            ),
            Predict(
                "O que quer dizer 'x está vivo no ponto P'?",
                listOf("x já foi definido antes de P", "Existe um caminho de P até um USO de x sem passar por uma nova definição de x", "x está num registrador", "x vai ser impresso"),
                1,
                "Vivo = 'o valor atual ainda vai ser lido'. Se x vai ser redefinido antes de ser lido, o valor atual está morto: pode descartar.",
            ),
            Reveal("Dessa definição sai a fórmula in = use ∪ (out − def). Explique cada pedaço.", "Antes da instrução, está vivo o que ela USA (use) e o que estava vivo depois dela (out), menos o que ela própria redefine (def), porque esse valor antigo nunca seria lido."),
            Predict(
                "Se a fórmula calcula 'in' a partir de 'out', em que direção percorremos o código?",
                listOf("De cima para baixo", "De baixo para cima", "Tanto faz"),
                1,
                "Para calcular o in da instrução i, preciso do out, que é o in da i + 1. Então começo no fim, com out = { }.",
            ),
            Say(PROF, "Agora monte um grafo: um nó por temporário e uma aresta entre quem está vivo junto. Dar registradores é dar cores sem que vizinhos repitam."),
            Visual(VK.REGALLOC, input = "( a + b ) * ( c - d )", caption = "Duas cores bastam. Repare qual temporário ficou sem vizinhos."),
            Reveal("E se o grafo precisar de mais cores do que há registradores?", "Algum temporário é derramado (spill): mora na memória, com sw depois de calculado e lw antes de cada uso. Escolhe-se, de preferência, um com muitos vizinhos e poucos usos."),
            Reveal("Por que \$t e \$s existem separados no MIPS?", "Por causa das chamadas de função. Quem chama não sabe quais registradores a função vai usar. A convenção divide a responsabilidade: \$t0–\$t9 podem ser destruídos pela função (se o chamador precisa deles, ele salva); \$s0–\$s7 devem ser devolvidos intactos (se a função usa, ela salva e restaura)."),
            Callout(FIRESIDE, "t1: 'Eu cheguei primeiro nesse registrador!' t3: 'Mas você já morreu, ninguém vai te ler de novo.' t1: '...' Alocador: 'Próximo! \$s0 está livre.'"),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.FORMAL, "Alocação por coloração: definição de prova",
        listOf(
            Text("Para cada instrução i: **use[i]** = variáveis lidas por i; **def[i]** = variáveis escritas por i. Uma variável x está **viva** num ponto P se existe um caminho de P até um uso de x que não passa por definição de x."),
            Code(
                """
                Vivacidade (análise de fluxo de dados PARA TRÁS):
                    out[i] = ∪ in[s]   para cada sucessor s de i     (no fim: out = ∅)
                    in[i]  = use[i] ∪ (out[i] − def[i])
                Num bloco básico (sem desvios), o único sucessor de i é i + 1:
                    percorra de i = n−1 até 0, com out[n−1] = ∅.
                Com laços: repita até nenhum in/out mudar (ponto fixo).
                """.trimIndent(),
                "Equações de vivacidade",
            ),
            Text("**Intervalo de vida** de x: os pontos do programa em que x está viva. **Grafo de interferência** G = (V, E): V = temporários; {x, y} ∈ E se x é definido numa instrução em que y ∈ out (y ≠ x). (Refinamento clássico: numa cópia x = y, não se liga x a y, para permitir que dividam o registrador.)"),
            Text("**Alocação com k registradores** = **k-coloração** de G. Decidir se G é k-colorível é NP-completo para k ≥ 3, por isso usamos heurísticas."),
            Code(
                """
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
                """.trimIndent(),
                "Simplificar, empilhar, colorir",
            ),
            Text("**Por que funciona**: se grau(n) < k, então, por pior que seja a coloração dos vizinhos, sobra uma cor para n. Remover n não atrapalha; colorimos na volta. Spill só acontece quando todo nó restante tem grau ≥ k."),
            Predict(
                "Um grafo tem um nó n de grau 5 e k = 4. O que é VERDADE?",
                listOf("n certamente será derramado", "n não pode ser simplificado agora, mas pode acabar colorido se dois vizinhos receberem a mesma cor", "O grafo não é 4-colorível"),
                1,
                "Grau ≥ k só torna n um CANDIDATO. Na seleção, se os vizinhos repetirem cores, sobra cor para n. Por isso o spill de Chaitin é 'otimista' na versão de Briggs.",
            ),
            Visual(VK.MIPS_RUN, grammar = P6_RA_CALL, caption = "Convenção do MIPS: \$s0 é salvo pelo CHAMADO. dobro usa \$s0, mas salva e restaura; main encontra o 7 intacto e imprime 17."),
            Text("**Convenção MIPS**: \$t0–\$t9 são **salvos pelo chamador** (caller-saved): uma função pode sobrescrevê-los à vontade. \$s0–\$s7 são **salvos pelo chamado** (callee-saved): quem os usa salva na pilha e restaura antes do `jr \$ra`. Regra prática: valor que atravessa uma chamada → \$s; valor de vida curta → \$t."),
            Callout(EXAM_TIP, "Roteiro de prova: (1) TAC; (2) tabela use/def/out/in DE BAIXO PARA CIMA; (3) arestas: def × out; (4) Chaitin com a pilha escrita; (5) mapa temporário → registrador; (6) se pedir, o MIPS e a contagem de lw/sw antes × depois."),
            Reveal("Escreva a equação de vivacidade e a regra de interferência.", "in[i] = use[i] ∪ (out[i] − def[i]); out[i] = ∪ in dos sucessores. x e y interferem se x é definido numa instrução em que y está vivo na saída (y ∈ out[i])."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.REG_ALLOC, Style.MNEMONIC, "Pinte o mapa da cozinha",
        listOf(
            Callout(MNEMONIC, "**VIGOR**: **VI**vacidade → **G**rafo → c**O**lorir → **R**egistradores. (E se faltar cor? Spill: 'derrama no chão'.)"),
            Text("📍 **Palácio da memória**: na Forja do Código, ao lado da Montadora MIPS, fica o **Ateliê de Coloração** 🎨. Na parede, um mapa com bolinhas (os temporários). O pintor tem só k latas de tinta (registradores). Bolinhas ligadas por fio não podem ter a mesma cor. A que sobrar sem tinta vai para o porão (a memória)."),
            Callout(MNEMONIC, "**Vivacidade se faz como desfazer a mala: de trás para frente.** O último item que entrou é o primeiro que sai. Comece no fim do código com out = { }."),
            Callout(MNEMONIC, "**in = use ∪ (out − def)** se lê: 'está vivo antes quem eu USO agora, mais quem estava vivo depois e eu NÃO matei'. Def mata, use ressuscita."),
            Callout(MNEMONIC, "**Interferência = 'nasceu enquanto o outro estava vivo'.** Quem é definido ganha aresta com todo mundo do out daquela instrução."),
            Predict(
                "Na fase de simplificação com k = 3, qual nó pode ser removido com segurança?",
                listOf("Qualquer um", "Um com grau 2 (menor que k)", "Só o de maior grau", "Um com grau 3"),
                1,
                "'Menos vizinhos que latas, sempre sobra lata.' Grau < k garante cor na volta.",
            ),
            Callout(MNEMONIC, "**\$t = tchau** (a função pode estragar: se precisar, o CHAMADOR salva). **\$s = sagrado** (a função devolve como achou: o CHAMADO salva)."),
            Callout(WATCH_OUT, "Contrastes: **vivo** ≠ 'já definido' (vivo = ainda vai ser LIDO). **χ (número cromático)** = menor k sem spill; não é o número de temporários. **Spill** não é erro: é custo (mais lw/sw)."),
            Visual(VK.REGALLOC, input = "a * b + ( c * d + a * c )", caption = "Recite VIGOR enquanto olha: vivos, grafo (triângulo!), 3 cores, MIPS com \$s0, \$s1, \$s2."),
            Reveal("Os números para guardar: quantos lw + sw em a * b + c * d, antes e depois?", "11 no ingênuo, 5 com alocação (2 registradores). Em a * b + ( c * d + a * c ): 17 → 7 com 3 registradores, ou 9 com 2 (t1 derramado)."),
            Reveal("Sem olhar: os 4 passos, a fórmula e a convenção.", "VIGOR: vivacidade (de trás para frente), grafo de interferência, colorir com k cores (Chaitin: simplifica grau < k, empilha, colore), registradores. in = use ∪ (out − def). \$t: salvos pelo chamador; \$s: salvos pelo chamado."),
        ),
    ),
)
