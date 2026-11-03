package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 4a do conteúdo: NFA_TO_DFA (Floresta Léxica — Fábrica de Subconjuntos).
// Todos os conjuntos, estados e contagens abaixo foram conferidos com SmallNFA.subsetConstruction()
// e DFA.fromRegex() do motor.

// ───────────────────────────── NFA_TO_DFA ─────────────────────────────

internal val p4NfaLessons: List<Lesson> = listOf(
    Lesson(
        Skill.NFA_TO_DFA, Style.STORY, "A turma no grupo do WhatsApp",
        listOf(
            Say(NARRADOR, "Rodoviária lotada, véspera de feriado. Você e sua turma precisam chegar à plataforma certa, mas a placa diz: 'Ônibus A: plataforma 1 OU plataforma 3'. Cadê a certeza?"),
            Say(BYTE, "Eu ia travar. Escolho uma e torço?"),
            Say(PROF, "Um **AFN** faz diferente: ele não escolhe. A turma se DIVIDE e cada um vai para uma possibilidade. E tem os corredores livres, sem catraca: quem está num ponto pode atravessar de graça. Esses são os trilhos **ε**."),
            Say(PROF, "Agora o truque: em vez de acompanhar cada amigo, você só olha o **grupo do WhatsApp** e anota: 'neste momento a turma está em {1, 3, 4}'. Esse conjunto inteiro vira UM estado de um **AFD**."),
            Steps(
                "Rodoviária → construção de subconjuntos",
                listOf(
                    "🚏 Cada plataforma é um **estado do AFN**.",
                    "🚶 Corredor livre (sem ler letra nenhuma) é uma **transição ε**. Juntar todo mundo que dá para alcançar a pé é o **ε-fecho**.",
                    "🎫 Usar uma passagem com a letra 'a' é o **move(T, a)**: para onde cada amigo pode ir lendo 'a'.",
                    "📱 A lista do grupo ('estamos em {0, 1}') é um **estado do AFD**: um CONJUNTO de estados do AFN.",
                    "😵 Se ninguém da turma tem para onde ir, o grupo fica vazio: é o **estado morto ∅**.",
                    "🏁 Se pelo menos UM amigo chegou ao destino, a turma toda comemora: o conjunto é **final** se contém algum final do AFN.",
                ),
            ),
            Say(BYTE, "Então o AFD é só o diário do grupo: 'estávamos aqui, lemos tal letra, fomos parar ali'?"),
            Say(PROF, "Exatamente. E como o diário é anotado ANTES da viagem, na hora de ler a entrada não existe mais dúvida: um passo por letra."),
            Predict(
                "No AFN de '(a|b)*ab' a turma começa em {0}. De 0, lendo 'a', dá para ficar em 0 ou ir para 1. Lendo 'a', onde a turma fica?",
                listOf("{0}", "{1}", "{0, 1}", "∅"),
                2,
                "A turma se divide: uns ficam em 0, outros vão para 1. O estado do AFD é o conjunto {0, 1}. Sem dúvida, sem sorteio: carregamos as duas possibilidades juntas.",
            ),
            Visual(VK.NFA_SUBSET, input = "ab", caption = "Avance passo a passo: em amarelo, onde a turma está. Repare que o AFD final tem só 3 estados."),
            Callout(NO_DUMB_QUESTIONS, "P: Se o AFN já reconhece a linguagem, para que converter? R: Simular o AFN obriga a carregar um conjunto a cada letra lida. O AFD faz essa conta UMA vez, na construção; depois, cada caractere custa só uma consulta à tabela. Analisador léxico lê milhões de caracteres: essa economia importa."),
            Callout(BRAIN_POWER, "Com 3 plataformas, quantas listas diferentes o grupo do WhatsApp poderia ter, no máximo? Pense em cada plataforma como 'tem gente' ou 'não tem'."),
            Reveal("Resposta", "2³ = 8 listas possíveis (incluindo a vazia). Por isso um AFN de n estados vira um AFD de até 2ⁿ estados. Mas quase sempre a turma só visita poucas listas — no exemplo acima, 3."),
            Reveal("Na analogia, quem é: plataforma, corredor livre, lista do grupo, grupo vazio?", "Estado do AFN, transição ε, estado do AFD (conjunto de estados do AFN) e estado morto ∅."),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.VISUAL, "Assistindo a fábrica de subconjuntos",
        listOf(
            Say(PROF, "Hoje você vai VER a construção acontecer. Em cada passo, o amarelo mostra o conjunto atual, e a tabela do AFD vai se preenchendo sozinha."),
            Visual(VK.NFA_SUBSET, input = "abb", caption = "O clássico (a|b)*abb, numa versão de AFN com 4 estados. Avance devagar."),
            Text("Repare: começamos em A = {0}. Lendo 'a' vamos para {0, 1} = B; de B, lendo 'b', vamos para {0, 2} = C; de C, lendo 'b', chegamos a {0, 3} = D, o único que contém o final 3."),
            Predict(
                "Por que TODOS os estados do AFD contêm o 0?",
                listOf("Porque 0 é o inicial e nunca sai do conjunto", "Porque 0 tem laço com a e com b: quem está em 0 sempre pode continuar em 0", "Coincidência", "Porque o 0 é final"),
                1,
                "O laço a,b em 0 significa 'ainda não comecei o sufixo abb'. Essa possibilidade nunca morre, então o 0 aparece em todo conjunto.",
            ),
            Say(LEXI, "Agora com trilhos ε. Aqui o primeiro estado já NÃO é só {0}."),
            Visual(VK.NFA_SUBSET, input = "abc", caption = "ab* | c: do estado 0 saem dois trilhos ε. Veja o ε-fecho logo no início."),
            Predict(
                "No AFN de 'ab* | c', 0 tem trilhos ε para 1 e para 3. Quanto vale ε-fecho({0})?",
                listOf("{0}", "{1, 3}", "{0, 1, 3}", "{0, 1, 2, 3, 4}"),
                2,
                "O ε-fecho inclui o próprio 0 e todo mundo alcançável só por ε: 1 e 3. O 2 e o 4 exigem ler uma letra, então ficam de fora.",
            ),
            Callout(WATCH_OUT, "Quando o move dá vazio (por exemplo, de A = {0, 1, 3} lendo 'b'), o widget mostra ∅: é o **estado morto**. O desenho do AFD omite o morto, mas ele existe — qualquer transição que não aparece leva para lá."),
            Visual(VK.NFA_SUBSET, input = "ou", caption = "(ab | a)c: dois caminhos que se reencontram por ε no estado 4."),
            Text("Repare no passo de A com 'a': move = {1, 3}, mas o ε-fecho puxa o 4 também, porque 3 --ε--> 4. Resultado: B = {1, 3, 4}. É por isso que 'ac' é aceita: B já 'sabe' que pode ler o c."),
            Reveal("Quantos estados tem o AFD de (ab | a)c, e qual é o final?", "4 estados: A = {0}, B = {1, 3, 4}, C = {2, 4} e D = {5}. Só D é final, porque só ele contém o 5."),
            Say(BYTE, "E quando o número de estados explode mesmo?"),
            Visual(VK.DFA, input = "(a|b)*a(a|b)(a|b)", caption = "'O antepenúltimo símbolo é a'. O AFN tem 4 estados; o AFD mínimo tem 8 = 2³. Ele precisa lembrar as 3 últimas letras!"),
            Callout(REAL_WORLD, "Esse tipo de linguagem é o pior caso. Os padrões de tokens de verdade (identificadores, números, palavras-chave) geram AFDs pequenos — por isso Lex/Flex podem converter tudo para AFD sem medo."),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.WORKED, "Subconjuntos com ε, do começo ao fim",
        listOf(
            Say(PROF, "Vamos resolver com calma um AFN COM transições ε. É assim que aparece na prova, porque o AFN de Thompson tem ε por todo lado."),
            Code("AFN de (ab | a)c — inicial 0, final 5\n0 --a--> 1     1 --b--> 2     2 --ε--> 4\n0 --a--> 3     3 --ε--> 4\n4 --c--> 5", "Dois caminhos (ab ou a) que se juntam no 4 antes do c"),
            Text("A receita tem um passo só, repetido: **Dtran[T, x] = ε-fecho(move(T, x))**. Primeiro anda com a letra (move), depois anda de graça pelos ε (fecho)."),
            Steps(
                "Resolvido",
                listOf(
                    "Início: A = ε-fecho({0}) = {0}. Do 0 não sai nenhum ε.",
                    "A com a: move = {1, 3}. Fecho: 3 --ε--> 4, então **B = {1, 3, 4}**.",
                    "A com b e A com c: move = ∅ → estado morto.",
                    "B com b: move = {2}. Fecho: 2 --ε--> 4, então **C = {2, 4}**.",
                    "B com c: move = {5} → **D = {5}**. B com a: ∅.",
                    "C com c: move = {5} = D. C com a e com b: ∅.",
                    "D com qualquer letra: ∅. Nenhum estado novo: terminou!",
                    "Finais: quem contém o 5 → só **D**. AFD com 4 estados (sem contar o morto).",
                ),
            ),
            Code("        a    b    c\n→A {0}       B    ∅    ∅\n B {1,3,4}   ∅    C    D\n C {2,4}     ∅    ∅    D\n*D {5}       ∅    ∅    ∅", "Tabela Dtran (→ inicial, * final, ∅ = estado morto)"),
            Visual(VK.NFA_SUBSET, input = "ou", caption = "Confira cada linha da tabela com o motor."),
            Callout(WATCH_OUT, "O erro número 1 é esquecer o fecho depois do move. Sem ele, B seria {1, 3} e o AFD rejeitaria 'ac' — mesmo 'ac' estando na linguagem!"),
            Text("Agora é com você (exemplo esmaecido). AFN de **ab* | c**: 0 --ε--> 1, 0 --ε--> 3, 1 --a--> 2, 2 --b--> 2, 3 --c--> 4. Finais: 2 e 4."),
            Reveal("Qual é o estado inicial A do AFD?", "A = ε-fecho({0}) = {0, 1, 3}. Os dois ε saem do 0."),
            Reveal("Calcule Dtran[A, a], Dtran[A, b] e Dtran[A, c].", "A com a: move = {2}, fecho = {2} = B. A com b: ∅ (morto). A com c: move = {4} = C."),
            Reveal("Continue com B = {2} e C = {4}. Quem é final?", "B com b: {2} = B (o laço do b*). Todo o resto dá ∅. Finais: B (contém 2) e C (contém 4). AFD com 3 estados."),
            Visual(VK.NFA_SUBSET, input = "abc", caption = "Confira suas respostas."),
            Callout(EXAM_TIP, "Monte SEMPRE a tabela com as colunas 'estado do AFD | conjunto do AFN | uma coluna por símbolo'. Escreva o move e o fecho separados no rascunho: o professor dá ponto parcial pelo raciocínio."),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.SOCRATIC, "Por que conjuntos?",
        listOf(
            Say(PROF, "Hoje eu só pergunto. Você vai reinventar a construção de subconjuntos sozinho."),
            Predict(
                "Um AFN está no estado 0 e tem DOIS trilhos 'a': para 0 e para 1. Lendo 'a', onde ele está?",
                listOf("Em 0", "Em 1", "Nos dois ao mesmo tempo", "Trava: AFN não pode ter isso"),
                2,
                "O AFN aceita se ALGUM caminho aceita. Para não perder nenhum, acompanhamos todos ao mesmo tempo: o AFN está em {0, 1}.",
            ),
            Say(BYTE, "Então, se eu sempre anotar o CONJUNTO onde ele pode estar, nunca preciso adivinhar?"),
            Say(PROF, "Isso! E se você dá um nome a cada conjunto (A, B, C…), acabou de inventar um autômato determinístico."),
            Predict(
                "Depois de ler uma letra, por que aplicar o ε-fecho de novo?",
                listOf("Não precisa, só no início", "Porque, ao chegar num estado, o AFN pode seguir de graça pelos trilhos ε", "Para tirar estados repetidos", "Para achar os finais"),
                1,
                "Trilho ε não consome entrada. Se você chega em 3 e existe 3 --ε--> 4, então também está em 4. Esquecer isso perde caminhos.",
            ),
            Predict(
                "E quando move(T, a) dá o conjunto vazio?",
                listOf("A cadeia é aceita", "Voltamos ao estado inicial", "Vamos para o estado morto ∅, de onde não se sai", "É um erro na construção"),
                2,
                "Nenhum caminho sobreviveu. ∅ é um estado do AFD como outro qualquer, só que não final e com laço para tudo.",
            ),
            Reveal("Um conjunto como {0, 2} deve ser final quando o final do AFN é o 2? E {0, 1}?", "{0, 2} é final: basta UM caminho ter chegado ao fim. {0, 1} não é: nenhum caminho está num estado final."),
            Say(PROF, "Agora o tamanho. Olhe este AFN de 3 estados: 'o penúltimo símbolo é a'."),
            Visual(VK.NFA_SUBSET, input = "pen", caption = "Conte quantos estados o AFD ganha."),
            Predict(
                "Com 3 estados no AFN, o máximo seriam 2³ = 8 conjuntos. Por que só apareceram 4?",
                listOf("O motor errou", "Todo conjunto alcançável contém o 0 (por causa do laço), então sobram só 2² = 4 combinações", "Porque o AFN não tem ε", "Porque o alfabeto tem 2 letras"),
                1,
                "O 0 está sempre presente; só os estados 1 e 2 variam (estão ou não). São 2 × 2 = 4 conjuntos possíveis — e todos aparecem.",
            ),
            Reveal("Se fosse 'o n-ésimo símbolo a partir do fim é a', quantos estados o AFD teria?", "2ⁿ. O AFD precisa lembrar as n últimas letras, e cada uma pode ser a ou b. É o exemplo clássico da explosão exponencial."),
            Visual(VK.DFA, input = "(a|b)*a(a|b)(a|b)", caption = "n = 3: o AFD mínimo tem 2³ = 8 estados. Nem minimizando dá para fugir."),
            Reveal("Então por que os analisadores léxicos usam AFD mesmo assim?", "Porque a explosão é rara com padrões de tokens reais, a conversão acontece UMA vez (ao gerar o léxico) e depois cada caractere custa um único passo. Pagar na fábrica para economizar na estrada."),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.FORMAL, "Construção de subconjuntos: definição de prova",
        listOf(
            Text("**AFN**: N = (S, Σ, δ, s₀, F), com δ: S × (Σ ∪ {ε}) → 𝒫(S). **AFD**: caso particular sem transições ε e com exatamente um destino para cada par (estado, símbolo)."),
            Text("**ε-fecho(s)**: estados alcançáveis a partir de s usando apenas transições ε (inclui s). **ε-fecho(T)** = ∪ ε-fecho(s), s ∈ T. **move(T, a)**: estados alcançáveis a partir de algum s ∈ T por UMA transição rotulada a."),
            Code(
                """
                ε-fecho(T):
                    empilhe todos os estados de T
                    fecho ← T
                    enquanto a pilha não estiver vazia:
                        desempilhe t
                        para cada u com t --ε--> u:
                            se u ∉ fecho: fecho ← fecho ∪ {u}; empilhe u
                    devolva fecho
                """.trimIndent(),
                "Cálculo do ε-fecho (Livro do Dragão, Fig. 3.33)",
            ),
            Code(
                """
                Dstates ← { ε-fecho({s₀}) }, não marcado
                enquanto houver T não marcado em Dstates:
                    marque T
                    para cada símbolo a ∈ Σ:
                        U ← ε-fecho(move(T, a))
                        se U ∉ Dstates: adicione U (não marcado)
                        Dtran[T, a] ← U
                estado inicial do AFD: ε-fecho({s₀})
                estados finais do AFD: todo T com T ∩ F ≠ ∅
                """.trimIndent(),
                "Construção de subconjuntos (Algoritmo 3.20)",
            ),
            Text("**Correção**: o AFD D simula todos os caminhos do AFN N em paralelo; após ler w, D está em T exatamente quando T é o conjunto de estados em que N pode estar após ler w. Logo L(D) = L(N)."),
            Code(
                "A = {0,1,2,4,7}\nB = {1,2,3,4,6,7,8}\nC = {1,2,4,5,6,7}\nD = {1,2,4,5,6,7,9}\nE = {1,2,4,5,6,7,10}\n\nDtran   a   b\n→A      B   C\n B      B   D\n C      B   C\n D      B   E\n*E      B   C",
                "Exemplo do livro: AFN de Thompson de (a|b)*abb (estados 0..10, final 10)",
            ),
            Predict(
                "No exemplo do livro, por que E é o único estado final?",
                listOf("Porque foi o último criado", "Porque é o único que contém o estado 10, final do AFN", "Porque tem mais estados", "Porque Dtran[E, b] = C"),
                1,
                "Regra: T é final se T ∩ F ≠ ∅. F = {10} e só E contém o 10.",
            ),
            Callout(NO_DUMB_QUESTIONS, "P: O AFD do livro tem 5 estados, mas o AFD mínimo de (a|b)*abb tem 4. Está errado? R: Não! A construção de subconjuntos não garante o mínimo. A e C se comportam igual (ambos vão para B com a e para C com b) e a minimização os funde."),
            Visual(VK.DFA, input = "(a|b)*abb", caption = "O motor mostra o AFD já minimizado: 4 estados."),
            Text("**Custo**: um AFN de n estados pode gerar um AFD de até 2ⁿ estados. Comparação clássica para reconhecer x com a ER r: simular o AFN gasta espaço O(|r|) e tempo O(|r| × |x|); o AFD gasta tempo O(|x|), mas pode exigir espaço O(2^|r|) no pior caso."),
            Predict(
                "Qual afirmação é VERDADEIRA?",
                listOf("AFN reconhece mais linguagens que AFD", "Todo AFN tem um AFD equivalente, que pode ter exponencialmente mais estados", "A construção de subconjuntos sempre produz o AFD mínimo", "O estado inicial do AFD é sempre {s₀}"),
                1,
                "Mesmo poder (linguagens regulares), custo de tamanho possivelmente exponencial. O inicial é ε-fecho({s₀}), que pode ser maior que {s₀}.",
            ),
            Callout(EXAM_TIP, "Questão clássica: 'converta o AFN de Thompson de (a|b)*abb em AFD'. Decore o caminho: ε-fecho({0}) = {0,1,2,4,7}, cinco estados A–E, E final, e A ≡ C na minimização. Escreva Dtran[T, a] = ε-fecho(move(T, a)) no topo da folha."),
            Reveal("Escreva a fórmula central do algoritmo e as regras de inicial e finais.", "Dtran[T, a] = ε-fecho(move(T, a)). Inicial: ε-fecho({s₀}). Finais: todo T que contém pelo menos um estado final do AFN."),
        ),
    ),
    Lesson(
        Skill.NFA_TO_DFA, Style.MNEMONIC, "Anda e Fecha a Porta",
        listOf(
            Callout(MNEMONIC, "**Anda e fecha a porta!** Primeiro ANDA com a letra (move), depois FECHA (ε-fecho). Como a sua mãe gritando da cozinha: entrou, fecha a porta!"),
            Callout(MNEMONIC, "**Cesto de frutas**: cada estado do AFD é um cesto com estados do AFN dentro. O cesto é **final se tem UMA fruta premiada** (um final do AFN). O **cesto vazio ∅** é o estado morto."),
            Text("📍 **Palácio da memória**: na Floresta Léxica, logo depois do Trem do Autômato, fica a **Fábrica de Subconjuntos** 🧺. Os duendes de Thompson entregam trilhos bagunçados (AFN); os operários põem estados em cestos, carimbam A, B, C… e entregam ao trem um mapa sem dúvidas (AFD)."),
            Callout(MNEMONIC, "**2ⁿ = interruptores**: cada estado do AFN é uma lâmpada que está acesa (no cesto) ou apagada. n lâmpadas → 2ⁿ combinações. É o TETO, não o normal."),
            Predict(
                "Qual a ordem correta dentro de cada passo da construção?",
                listOf("ε-fecho, depois move", "move, depois ε-fecho", "só move", "só ε-fecho"),
                1,
                "Anda (move com a letra) e fecha a porta (ε-fecho). O ε-fecho 'sozinho' só aparece uma vez: no estado inicial, ε-fecho({s₀}).",
            ),
            Callout(MNEMONIC, "**Inicial também fecha a porta**: o estado inicial do AFD NÃO é {s₀}, é ε-fecho({s₀}). Se esqueceu, o cesto começa faltando fruta."),
            Visual(VK.NFA_SUBSET, input = "eps", caption = "a*b*: o inicial já é {0, 1} por causa do ε. Repare que A já é final — a cadeia vazia é aceita!"),
            Callout(WATCH_OUT, "Contrastes que caem na prova: **move** usa UMA letra; **ε-fecho** não usa letra nenhuma. **AFN** pode estar em vários estados; **AFD** em exatamente um. **∅** é o estado morto, não o inicial. Subconjuntos NÃO minimiza: isso é outra etapa."),
            Callout(MNEMONIC, "A esteira completa: **T**hompson **T**ransforma (ER → AFN), **S**ubconjuntos **S**implificam (AFN → AFD), **M**inimização **M**ata os repetidos (AFD → AFD mínimo). Lex/Flex rodam a esteira inteira por você."),
            Reveal("Sem olhar: as 3 regras da construção de subconjuntos.", "1) Inicial = ε-fecho({s₀}). 2) Dtran[T, a] = ε-fecho(move(T, a)) — anda e fecha a porta. 3) T é final se contém algum final do AFN."),
            Reveal("Por que analisadores léxicos preferem AFD?", "Um passo por caractere, sem voltar atrás e sem carregar conjuntos. A conversão (talvez cara) é feita uma vez só, quando o léxico é gerado."),
        ),
    ),
)
