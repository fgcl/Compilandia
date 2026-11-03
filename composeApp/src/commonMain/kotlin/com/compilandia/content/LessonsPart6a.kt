package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 6a do conteúdo: DFA_MINIMIZATION (Floresta Léxica — Tabela dos Pares).
// Todas as marcações, rodadas e classes abaixo foram conferidas com Minimizer.run() sobre Minimizer.bank:
//  0 = AFD do livro (fig. 3.36) para (a|b)*abb: q0 ≡ q2; 4 marcas na rodada 1, 3 na 2, 2 na 3; mínimo com 4 estados.
//  1 = número par de a's: q0 ≡ q2 e q1 ≡ q3 (só rodada 1); mínimo com 2.
//  2 = termina em b: q0 ≡ q2 e q1 ≡ q3 (só rodada 1); mínimo com 2.
//  3 = ab*: completa com ∅; q1 ≡ q2; {q0,∅} marcado na rodada 2; mínimo com 2.
//  5 = a*b*: completa com ∅; q0 ≡ q1; {q0,q2} e {q1,q2} marcados na rodada 2 por causa do ∅; mínimo com 2.

/** Tabela em escadinha do AFD do livro, com a rodada em que cada par foi marcado. */
private val P6_DRAGON_TABLE: String = """
AFD do livro para (a|b)*abb   (q0..q4 = A..E do Dragão)

         a    b
  → q0   q1   q2
    q1   q1   q3
    q2   q1   q2
    q3   q1   q4
  * q4   q1   q2

Tabela de pares (o número é a RODADA em que o par foi marcado;
· = nunca marcado = EQUIVALENTES)

        +----+
   q1   | 3  |
        +----+----+
   q2   | ·  | 3  |
        +----+----+----+
   q3   | 2  | 2  | 2  |
        +----+----+----+----+
   q4   | 1  | 1  | 1  | 1  |
        +----+----+----+----+
          q0   q1   q2   q3
""".trimIndent()

private val P6_DRAGON_ROUNDS: String = """
Rodada 1 (final × não-final): q4 é o único final
  {q0,q4} {q1,q4} {q2,q4} {q3,q4}  → marcados

Rodada 2 (olhe só o que foi marcado ATÉ a rodada 1)
  par       com a        com b        marca?
  {q0,q1}   {q1,q1} =    {q2,q3} ·    não (ainda)
  {q0,q2}   {q1,q1} =    {q2,q2} =    não
  {q0,q3}   {q1,q1} =    {q2,q4} ✔    SIM
  {q1,q2}   {q1,q1} =    {q3,q2} ·    não (ainda)
  {q1,q3}   {q1,q1} =    {q3,q4} ✔    SIM
  {q2,q3}   {q1,q1} =    {q2,q4} ✔    SIM

Rodada 3
  {q0,q1}   com b → {q2,q3} ✔ (marcado na rodada 2)  SIM
  {q1,q2}   com b → {q3,q2} ✔                        SIM
  {q0,q2}   a → {q1,q1}, b → {q2,q2}                 não

Rodada 4: nada mudou → PARE.
Sobrou {q0,q2} sem marca → q0 ≡ q2 (A ≡ C no livro).
""".trimIndent()

private val P6_DRAGON_MIN: String = """
AFD mínimo (4 estados)

               a        b
  → {q0,q2}    {q1}     {q0,q2}
    {q1}       {q1}     {q3}
    {q3}       {q1}     {q4}
  * {q4}       {q1}     {q0,q2}
""".trimIndent()

internal val p6MinLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.STORY, "Os atendentes que fazem a mesma coisa",
        listOf(
            Say(NARRADOR, "Lanchonete do bairro, hora do almoço. O dono olha a folha de pagamento e franze a testa: 'Cinco atendentes... será que preciso de todos?'"),
            Say(BYTE, "Ué, demite quem trabalha menos?"),
            Say(PROF, "Não é bem isso. Ele faz um teste: dá a mesma sequência de pedidos para dois atendentes. Se, para QUALQUER sequência, os dois acabam do mesmo jeito (entregando o lanche ou não), eles são **intercambiáveis**. Dá para juntar os dois num cargo só."),
            Steps(
                "Lanchonete → minimização de AFD",
                listOf(
                    "🧑‍🍳 Cada atendente é um **estado** do AFD.",
                    "📝 Cada pedido do cliente ('a', 'b') é um **símbolo** da entrada: o atendente repassa você para alguém.",
                    "🍔 'Entregar o lanche' no fim da fila de pedidos é terminar num **estado final**.",
                    "🤝 Dois atendentes que SEMPRE terminam igual são **equivalentes** (indistinguíveis).",
                    "🔎 Se existe UMA sequência de pedidos em que um entrega e o outro não, eles são **distinguíveis**.",
                    "📉 Juntar todos os equivalentes dá o **AFD mínimo**: mesmo serviço, folha de pagamento menor.",
                ),
            ),
            Say(LEXI, "E na Floresta Léxica isso importa muito: meu analisador léxico é um AFD gigante. Menos estados = tabela menor = léxico mais leve."),
            Predict(
                "Atendente X entrega o lanche quando a fila de pedidos acaba nele; atendente Y nunca entrega. Eles podem ser equivalentes?",
                listOf("Sim, se repassarem os pedidos para as mesmas pessoas", "Não: com a fila VAZIA um entrega e o outro não", "Depende do tamanho do alfabeto"),
                1,
                "A cadeia vazia já os separa. Por isso o primeiro passo do método é sempre marcar todo par (final, não-final): eles são distinguíveis de cara.",
            ),
            Say(PROF, "Depois vem o 'efeito dominó': se X e Y, ao receber 'b', repassam você para dois atendentes que JÁ sabemos que são diferentes, então X e Y também são diferentes."),
            Visual(VK.MINIMIZE, input = "0", caption = "O AFD do livro para (a|b)*abb. Avance e veja os pares sendo marcados rodada a rodada. Quem sobra sem marca se funde."),
            Reveal("No widget, quais dois estados sobraram sem marca e se fundiram?", "q0 e q2 (no livro, A e C). Os dois mandam 'a' para q1 e 'b' para q2: fazem exatamente o mesmo trabalho. O AFD cai de 5 para 4 estados."),
            Callout(NO_DUMB_QUESTIONS, "P: Se a construção de subconjuntos já dá um AFD, por que ele vem com estados sobrando? R: Ela cria um estado para cada CONJUNTO de estados do AFN que aparece, sem perguntar se dois conjuntos diferentes se comportam igual. Minimizar é a faxina depois da mudança."),
            Callout(BRAIN_POWER, "E o atendente que só fica de braços cruzados e nunca entrega nada, não importa o pedido? Ele também precisa entrar na conta. (Spoiler: é o **estado morto**, e esquecê-lo é o erro mais comum da prova.)"),
            Reveal("Na analogia, quem é: atendente, pedido, entregar o lanche, cargo único depois da fusão?", "Estado, símbolo da entrada, terminar num estado final, e um estado do AFD mínimo (uma classe de equivalência)."),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.VISUAL, "Vendo a tabela se preencher",
        listOf(
            Say(PROF, "Hoje você só assiste e prevê. A tabela de pares é uma escadinha: uma casa para cada par de estados {p, q}. Marcar a casa = 'esses dois são diferentes'."),
            Visual(VK.MINIMIZE, input = "1", caption = "Número par de a's, com estados sobrando. Repare: tudo se resolve já na rodada 1."),
            Predict(
                "Nesse AFD, q0 e q2 são finais; q1 e q3 não. Depois da rodada 1, quais pares ficam SEM marca?",
                listOf("Nenhum", "{q0,q2} e {q1,q3}", "Só {q0,q2}", "{q0,q1} e {q2,q3}"),
                1,
                "A rodada 1 marca só final × não-final. Sobram final com final ({q0,q2}) e não-final com não-final ({q1,q3}). Como nenhuma rodada seguinte marca mais nada, eles se fundem: o AFD mínimo tem 2 estados (par / ímpar).",
            ),
            Visual(VK.MINIMIZE, input = "0", caption = "O clássico do livro. Agora há 3 rodadas de marcação: veja o dominó caindo."),
            Steps(
                "O que você acabou de ver no exemplo do livro",
                listOf(
                    "Rodada 1: 4 pares marcados — todos com q4, o único final.",
                    "Rodada 2: {q0,q3}, {q1,q3} e {q2,q3}. Com 'b', cada um vai para um par que contém q4 e já estava marcado.",
                    "Rodada 3: {q0,q1} e {q1,q2}. Com 'b', vão para pares marcados na rodada 2.",
                    "Rodada 4: nada novo. Fim. 9 dos 10 pares marcados; só {q0,q2} sobrou.",
                ),
            ),
            Say(LEXI, "Agora um AFD que veio da construção de subconjuntos SEM estado morto. Olha o que o widget faz primeiro."),
            Visual(VK.MINIMIZE, input = "5", caption = "a*b*: falta a transição de q2 com 'a'. O widget completa com o estado morto ∅ antes de montar a tabela."),
            Predict(
                "Em a*b*, q0, q1 e q2 são TODOS finais. Sem o estado morto, a rodada 1 não marcaria nada. O que o ∅ muda?",
                listOf("Nada, ∅ é ignorado", "Com 'a', q2 vai para ∅ e q1 fica em q1; como {q1,∅} é final × não-final, {q1,q2} acaba marcado", "∅ se funde com q2"),
                1,
                "O ∅ é o único não-final, então a rodada 1 marca {q0,∅}, {q1,∅} e {q2,∅}. Na rodada 2, 'a' leva q2 para ∅ e q0/q1 para q1: marca {q0,q2} e {q1,q2}. Resultado: q0 ≡ q1, e q2 fica separado.",
            ),
            Reveal("E se você esquecesse o ∅ nesse exemplo?", "Nada seria marcado e você fundiria q0, q1 e q2 num estado só, final, com laço em a e b. Esse AFD aceita (a|b)*, então aceitaria 'ba' — que NÃO está em a*b*. Esquecer o estado morto muda a linguagem!"),
            Visual(VK.MINIMIZE, input = "3", caption = "ab*: aqui o ∅ se distingue de q0 só na rodada 2. Depois, o ∅ sozinho some do desenho do AFD mínimo."),
            Callout(WATCH_OUT, "O ∅ entra na TABELA, mas, se ficar sozinho na classe dele, não precisa ser desenhado no AFD mínimo: transição que não aparece vai para o morto, por convenção."),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.WORKED, "A tabela da prova, casa por casa",
        listOf(
            Say(PROF, "Esta é A questão de prova sobre minimização. Vamos fazer com o AFD do livro, sem pular nada. Pegue papel."),
            Code(P6_DRAGON_TABLE, "Transições e tabela final em escadinha. Desenhe só a metade de baixo: {p,q} e {q,p} são o mesmo par."),
            Text("Antes de tudo, dois cuidados: o AFD está **completo** (toda casa da tabela de transições preenchida, então não precisa de ∅) e todos os estados são **alcançáveis** a partir de q0."),
            Code(P6_DRAGON_ROUNDS, "Rodada a rodada. ✔ = par de destino já marcado; = = mesmo estado; · = par ainda não marcado"),
            Steps(
                "Os detalhes que valem ponto",
                listOf(
                    "Rodada 1: marque TODA casa da linha/coluna de q4 (final × não-final).",
                    "Rodada 2: para cada casa vazia {p,q}, olhe {δ(p,a), δ(q,a)} e {δ(p,b), δ(q,b)}. Se algum destino é casa marcada, marque {p,q}.",
                    "Se os destinos são o MESMO estado (como {q1,q1}), esse símbolo não diz nada.",
                    "Na rodada 2, {q0,q1} vai com 'b' para {q2,q3}, que ainda estava vazio. Não marque ainda — anote e volte depois.",
                    "Rodada 3: {q2,q3} agora está marcado, então {q0,q1} e {q1,q2} caem.",
                    "Rodada 4: nenhuma casa nova. Pare. A casa vazia {q0,q2} diz: q0 ≡ q2.",
                ),
            ),
            Code(P6_DRAGON_MIN, "Funda q0 e q2. Cada classe vira um estado; a classe com o inicial é inicial; a com final é final."),
            Visual(VK.MINIMIZE, input = "0", caption = "Confira cada casa e cada rodada com o motor."),
            Text("Agora é com você (exemplo esmaecido). AFD 'termina em b': q0 inicial; finais q1 e q3. Transições: q0 –a→ q2, q0 –b→ q1, q1 –a→ q2, q1 –b→ q3, q2 –a→ q0, q2 –b→ q3, q3 –a→ q0, q3 –b→ q1."),
            Reveal("Rodada 1: quais casas você marca?", "As de final × não-final: {q0,q1}, {q0,q3}, {q1,q2} e {q2,q3}."),
            Reveal("Rodada 2: teste {q0,q2} e {q1,q3}.", "{q0,q2}: com a → {q2,q0} (o próprio par, vazio); com b → {q1,q3} (vazio). {q1,q3}: com a → {q2,q0}; com b → {q3,q1}. Nenhum destino marcado: nada muda e o algoritmo para."),
            Reveal("Resultado final?", "q0 ≡ q2 e q1 ≡ q3. AFD mínimo com 2 estados: 'a última letra foi b' (final) e 'não foi' (inicial)."),
            Visual(VK.MINIMIZE, input = "2", caption = "Confira sua tabela."),
            Callout(EXAM_TIP, "Escreva a rodada dentro de cada casa (1, 2, 3...) e, ao lado, o motivo ('b: {q2,q4}'). Se você errar uma casa, o professor vê o raciocínio e dá ponto parcial. E termine SEMPRE desenhando o AFD mínimo: muita gente faz a tabela e esquece a resposta."),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.SOCRATIC, "Como provar que dois estados são diferentes?",
        listOf(
            Say(PROF, "Hoje você vai inventar o método da tabela respondendo perguntas. Comece por aqui: o que significa dois estados serem 'iguais'?"),
            Predict(
                "Qual a melhor definição de 'p e q são equivalentes'?",
                listOf("Têm as mesmas setas saindo", "Para TODA cadeia w, lendo w a partir de p termina em final se e só se lendo w a partir de q também termina", "São ambos finais", "Estão perto um do outro no desenho"),
                1,
                "Equivalência é sobre COMPORTAMENTO futuro: nenhuma cadeia consegue separar os dois. Ter as mesmas setas é suficiente, mas não necessário.",
            ),
            Say(BYTE, "Mas 'para toda cadeia' são infinitas cadeias! Como eu testo isso?"),
            Say(PROF, "Boa! Vire a pergunta do avesso: em vez de provar que são iguais, tente provar que são **diferentes**. Para isso basta UMA cadeia — uma testemunha."),
            Predict(
                "Qual é a testemunha mais curta possível, e quem ela separa?",
                listOf("'a', separa quem tem laço", "A cadeia vazia ε: separa todo final de todo não-final", "'ab', separa os iniciais"),
                1,
                "Lendo nada, você já está no fim. Se p é final e q não, ε é a testemunha. Daí a rodada 1.",
            ),
            Predict(
                "Suponha que {r, s} já está marcado com testemunha w. Se δ(p,'b') = r e δ(q,'b') = s, qual é a testemunha para {p, q}?",
                listOf("w", "'b' seguido de w", "w seguido de 'b'", "Não existe"),
                1,
                "De p, ler 'b' leva a r; depois w separa r de s. Então 'b'w separa p de q. É esse o 'efeito dominó': cada rodada cria testemunhas uma letra mais longas.",
            ),
            Reveal("Por que o algoritmo precisa repetir as rodadas até nada mudar?", "Porque marcar um par novo pode tornar possível marcar outros que dependiam dele. No AFD do livro, {q0,q1} só cai na rodada 3, depois que {q2,q3} foi marcado na rodada 2."),
            Reveal("Por que o algoritmo sempre termina?", "Há um número finito de pares, e um par marcado nunca é desmarcado. Cada rodada útil marca pelo menos um; quando uma rodada não marca nada, as seguintes também não marcariam."),
            Predict(
                "Um par sobreviveu a todas as rodadas sem marca. O que podemos concluir?",
                listOf("Talvez sejam diferentes, por uma cadeia longa que não testamos", "São equivalentes: se existisse testemunha, alguma rodada a teria encontrado", "Precisamos testar à mão"),
                1,
                "O algoritmo é completo: uma testemunha de tamanho n seria descoberta na rodada n + 1. Sem marca no fim = nenhuma testemunha existe.",
            ),
            Say(BYTE, "E se uma transição está faltando? Eu comparo com o quê?"),
            Reveal("Resposta", "Complete o AFD com o estado morto ∅ (não final, laço em tudo) e compare normalmente. Faltar transição significa 'vai para o morto'; o ∅ é que permite marcar pares como {q1,q2} em a*b*."),
            Visual(VK.MINIMIZE, input = "5", caption = "Veja a testemunha 'a' separando q2 de q1 graças ao ∅."),
            Callout(FIRESIDE, "Estado q0: 'Eu e o q2 fazemos exatamente o mesmo trabalho há anos.' Estado q2: 'Pois é, ninguém nunca achou uma cadeia que nos separasse.' Minimizador: 'Então parabéns, vocês acabam de virar um estado só.'"),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.FORMAL, "Minimização: definição de prova",
        listOf(
            Text("Seja D = (S, Σ, δ, s₀, F) um AFD **completo** e com todos os estados alcançáveis. Estenda δ para cadeias: δ*(p, ε) = p e δ*(p, xw) = δ*(δ(p, x), w)."),
            Text("**Equivalência**: p ≡ q se, para toda w ∈ Σ*, δ*(p, w) ∈ F ⇔ δ*(q, w) ∈ F. Caso contrário, p e q são **distinguíveis**, e uma w que os separa é uma **testemunha**. ≡ é uma relação de equivalência; suas classes são os estados do AFD mínimo."),
            Code(
                """
                MinimizaPorTabela(D):
                    remova os estados inalcançáveis a partir de s₀
                    se faltar transição: adicione ∅ (não final, δ(∅, c) = ∅) e complete
                    para cada par {p, q}:
                        se (p ∈ F) ≠ (q ∈ F): marque {p, q}
                    repita
                        para cada par {p, q} não marcado:
                            se existe c ∈ Σ com {δ(p,c), δ(q,c)} marcado:
                                marque {p, q}
                    até nenhuma marcação nova
                    pares não marcados ⇒ p ≡ q; junte cada classe num estado
                """.trimIndent(),
                "Algoritmo de preenchimento da tabela (pares distinguíveis)",
            ),
            Text("**Construção do mínimo**: estados = classes [p]; δ'([p], c) = [δ(p, c)] (bem definido, porque estados equivalentes vão para estados equivalentes); inicial = [s₀]; finais = classes contidas em F. Se [∅] tiver só o ∅, pode ser omitido do desenho."),
            Code(
                """
                MinimizaPorPartições(D):        // Moore; Hopcroft é a versão otimizada
                    Π ← { F, S − F }
                    repita
                        para cada grupo G de Π:
                            divida G: p e q ficam juntos se, para todo c,
                            δ(p,c) e δ(q,c) caem no MESMO grupo de Π
                    até Π não mudar
                """.trimIndent(),
                "Alternativa: refinar partições (o caminho do Livro do Dragão, Alg. 3.39)",
            ),
            Steps(
                "Partições no AFD do livro ((a|b)*abb)",
                listOf(
                    "Π₀ = { {q0,q1,q2,q3}, {q4} }",
                    "Com 'b', q3 vai para {q4} e os outros ficam no grupo grande: Π₁ = { {q0,q1,q2}, {q3}, {q4} }",
                    "Com 'b', q1 vai para {q3} e q0, q2 vão para o grupo grande: Π₂ = { {q0,q2}, {q1}, {q3}, {q4} }",
                    "q0 e q2 têm destinos idênticos: Π não muda mais. Mesmo resultado da tabela: q0 ≡ q2.",
                ),
            ),
            Text("**Teorema (Myhill–Nerode)**: para cada linguagem regular existe um AFD com o número mínimo de estados, e ele é **único a menos de renomear os estados**. Por isso dois alunos que minimizam certo chegam ao mesmo autômato, só com nomes diferentes."),
            Predict(
                "Qual afirmação é FALSA?",
                listOf("O AFD mínimo é único a menos de nomes", "A tabela e as partições dão o mesmo AFD mínimo", "Um par que ficou sem marca numa rodada nunca mais pode ser marcado", "Estados inalcançáveis devem ser removidos antes"),
                2,
                "Falso: no AFD do livro, {q0,q1} fica em branco na rodada 2 e é marcado na rodada 3. Só no FIM (quando uma rodada inteira não marca nada) o branco vira veredito de equivalência.",
            ),
            Text("**Custo**: a tabela tem n(n − 1)/2 casas e cada rodada as varre todas: polinomial, ótimo para provas com até ~8 estados. O algoritmo de Hopcroft faz o mesmo em O(n · |Σ| · log n) e é o que geradores de léxico usam."),
            Callout(EXAM_TIP, "Roteiro de prova: (1) inalcançáveis fora; (2) completar com ∅; (3) tabela em escadinha com a RODADA em cada casa; (4) repetir até estabilizar; (5) listar as classes; (6) desenhar o AFD mínimo com inicial e finais. Esquecer o passo 6 é perder metade da questão."),
            Reveal("Defina estados distinguíveis e diga o que a rodada 1 marca.", "p e q são distinguíveis se existe w com δ*(p, w) ∈ F e δ*(q, w) ∉ F (ou vice-versa). A rodada 1 marca todo par final × não-final, cuja testemunha é ε."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.DFA_MINIMIZATION, Style.MNEMONIC, "Marca, Propaga, Junta",
        listOf(
            Callout(MNEMONIC, "**MPJ — Marca, Propaga, Junta.** MARCA final × não-final. PROPAGA as marcas pelas setas, rodada após rodada. JUNTA quem sobrou sem marca. (Pense em 'MPJ = Ministério Público Julgando quem é igual'.)"),
            Text("📍 **Palácio da memória**: na Floresta Léxica, logo depois da Fábrica de Subconjuntos, fica a **Tabela dos Pares** 🧮: uma escada de pedra. Cada degrau tem casinhas, uma para cada par de estados. O guarda pinta de vermelho as casinhas dos pares diferentes; as que ficam brancas viram uma casa só."),
            Callout(MNEMONIC, "**Escadinha, não quadrado.** {p,q} = {q,p} e {p,p} não interessa: desenhe só o triângulo de baixo. n estados → n(n−1)/2 casas. Com 5 estados: 10 casas."),
            Callout(MNEMONIC, "**Final e não-final são água e óleo**: não se misturam nunca. Por isso a rodada 1 é automática."),
            Callout(MNEMONIC, "**Dominó**: 'se os FILHOS são diferentes, os PAIS também são'. Se p e q, com a mesma letra, caem em casinha vermelha, pinte {p,q} de vermelho."),
            Predict(
                "Na rodada 2, o par {q0,q1} vai com 'b' para {q2,q3}, que está em branco AGORA. O que fazer?",
                listOf("Marcar, porque provavelmente vai ser marcado", "Deixar em branco e voltar na próxima rodada", "Fundir q0 e q1"),
                1,
                "Dominó só cai empurrado por peça que JÁ caiu. Na rodada 3, {q2,q3} está vermelho e aí {q0,q1} cai também.",
            ),
            Callout(MNEMONIC, "**O morto também vota.** O estado ∅ não fala, não aceita nada, mas DISTINGUE: sem ele, a*b* viraria (a|b)*. Antes de desenhar a escadinha, pergunte: 'falta alguma seta?'"),
            Callout(WATCH_OUT, "Os três tropeços clássicos: (1) **esquecer o ∅**; (2) **marcar com base em casinha ainda branca**; (3) **parar depois de uma rodada só**, sem conferir se o dominó continua."),
            Callout(MNEMONIC, "A esteira da Floresta: **T**hompson **T**ransforma (ER → AFN), **S**ubconjuntos **S**implificam (AFN → AFD), **M**inimização **M**ata os repetidos (AFD → AFD mínimo). E o mínimo é **único**: 'cada linguagem tem uma única certidão de nascimento mínima'."),
            Visual(VK.MINIMIZE, input = "0", caption = "Recite MPJ enquanto o widget roda: marca (rodada 1), propaga (rodadas 2 e 3), junta (q0 com q2)."),
            Reveal("Sem olhar: os 3 passos e os 3 tropeços.", "Passos: Marca final × não-final; Propaga pelas setas até estabilizar; Junta os pares em branco. Tropeços: esquecer o estado morto, marcar com base em par ainda não marcado, e não repetir as rodadas."),
            Reveal("Contraste: o que a construção de subconjuntos faz e o que a minimização faz?", "Subconjuntos tira o NÃO-DETERMINISMO (AFN → AFD), podendo criar estados sobrando. Minimização tira a REDUNDÂNCIA (AFD → menor AFD equivalente). Uma não substitui a outra."),
        ),
    ),
)
