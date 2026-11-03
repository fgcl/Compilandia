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
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.VisualKind.DERIVATION
import com.compilandia.content.VisualKind.FIRST_FOLLOW
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// ───────────────────────────── FIRST ─────────────────────────────

internal val p2FirstLessons: List<Lesson> = listOf(
    Lesson(
        Skill.FIRST, Style.STORY, "A fila do bandejão: quem pode abrir a fila?",
        listOf(
            Say(NARRADOR, "Templo Top-Down, primeira sala. Na parede, um mural: a fila do bandejão do RU ao meio-dia, todo mundo com fome."),
            Say(BYTE, "Profª, por que tem fila de bandejão num templo de compiladores? Eu tô com fome e não entendi nada."),
            Say(PROF, "Porque FIRST é exatamente isso: **quem pode abrir a fila**. Cada não-terminal é uma turma que vai entrar na fila. FIRST(turma) = quem pode ser o primeiro a aparecer."),
            Text("Só um tiquinho mais formal: FIRST(A) é o conjunto de **terminais que podem aparecer no começo** de alguma cadeia derivada de A. Terminal é gente de verdade; não-terminal é uma turma que ainda vai mandar alguém."),
            Code(P2_ABC, "A fila de S: primeiro a turma A, depois a turma B, depois o c."),
            Say(PROF, "Olha a regra S → A B c. Quem abre a fila de S? A turma A vai na frente. Se A manda o 'a', a fila começa com a."),
            Say(BYTE, "Então FIRST(S) = {a}. Fácil!"),
            Say(PROF, "Calma, Byte! Repara em A → ε: a turma A pode **faltar**. Dizemos que A é **anulável**. Se ela falta, quem abre a fila?"),
            Predict(
                "A e B podem faltar (os dois têm produção ε). Quem pode abrir a fila de S → A B c?",
                listOf("Só a", "a e b", "a, b e c", "a, b, c e ε"), 2,
                "Se A vem: a. Se A falta e B vem: b. Se os dois faltam: o c abre a fila. Mas c é terminal e NUNCA falta, então ε não entra. FIRST(S) = {a, b, c}."
            ),
            Callout(MNEMONIC, "Turma anulável = turma que pode faltar. Se ela falta, **passa a vez para o vizinho de trás**. Só se TODO mundo da regra puder faltar é que o ε entra no FIRST."),
            Visual(FIRST_FOLLOW, P2_ABC, caption = "Veja o motor descobrindo FIRST(S) = {a, b, c}. Repare no passo em que ele 'pula' o A anulável."),
            Callout(NO_DUMB_QUESTIONS, "P: Pra que eu quero saber quem abre a fila? R: O analisador LL(1) olha UM token e precisa escolher a produção. Se estou em S e chega um b, o FIRST me diz: 'pode ir de S → A B c, o b é bem-vindo aqui'."),
            Reveal("Sem olhar: quanto valem FIRST(A) e FIRST(B) nessa gramática?", "FIRST(A) = {a, ε} e FIRST(B) = {b, ε}. O ε aparece porque cada uma tem uma produção vazia."),
            Say(BYTE, "Resumindo: FIRST é quem pode abrir a fila; turma que pode faltar passa a vez. Até que não sou tão burro assim."),
            Say(PROF, "Nunca foi! E se você errou o 'preveja', ótimo: o cérebro guarda melhor aquilo que o surpreendeu."),
        )
    ),
    Lesson(
        Skill.FIRST, Style.VISUAL, "FIRST ao vivo: o ponto fixo trabalhando",
        listOf(
            Say(PROF, "Hoje você só vai olhar. O motor do jogo vai calcular FIRST na frente dos seus olhos, regra por regra."),
            Code(P2_EXPR_LL, "A gramática de expressões sem recursão à esquerda (a versão LL(1))."),
            Say(BYTE, "Por onde ele começa?"),
            Text("Por todas as regras, em **voltas**. A cada volta, cada produção A → X1 X2 … tenta **acrescentar** coisas a FIRST(A). Quando uma volta inteira passa sem nada novo, paramos: é o **ponto fixo**."),
            Predict(
                "Por que FIRST(E) precisa de mais voltas do que FIRST(F) para ficar pronto?",
                listOf("Porque E é o símbolo inicial", "Porque depende de FIRST(T), que depende de FIRST(F)", "Porque E tem ε", "Porque E tem mais produções"), 1,
                "Os terminais ( e id nascem em F e 'sobem' a corrente: F → T → E. Enquanto FIRST(T) estiver vazio, E não tem o que copiar."
            ),
            Visual(FIRST_FOLLOW, P2_EXPR_LL, caption = "Avance passo a passo olhando só a parte FIRST: veja ( e id subindo de F para T e de T para E."),
            Steps(
                "O que olhar no widget",
                listOf(
                    "Primeira volta: E' ganha {+, ε} e T' ganha {*, ε}. Terminal no começo da regra entra direto; produção ε dá ε.",
                    "Ainda na primeira volta: F ganha {(, id}.",
                    "Depois: T → F T' copia FIRST(F) para FIRST(T).",
                    "Depois: E → T E' copia FIRST(T) para FIRST(E).",
                    "Uma volta inteira sem novidade: ponto fixo, fim.",
                )
            ),
            Say(BYTE, "Por que T não ganhou o ε de T'?"),
            Say(PROF, "Porque em T → F T' o F vem primeiro e **não** é anulável. F sempre manda alguém, então o T' nem é consultado para o começo."),
            Callout(WATCH_OUT, "Copie FIRST(Xi) **sem o ε**. O ε só entra em FIRST(A) se TODOS os símbolos do lado direito puderem sumir."),
            Reveal("FIRST(E), FIRST(T) e FIRST(F) são iguais. Qual é o conjunto?", "{ (, id }. Toda expressão começa com abre-parênteses ou com um identificador."),
            Callout(BRAIN_POWER, "E se a regra fosse F → ( E ) | id | num? Quais conjuntos FIRST mudariam?"),
            Reveal("Responda o Poder do Cérebro.", "FIRST(F), FIRST(T) e FIRST(E) ganham num: a mudança sobe a corrente inteira. FIRST(E') e FIRST(T') não mudam."),
            Say(PROF, "Viu? Não tem mágica: é só repetir 'copia o que vem no começo' até parar de mudar."),
        )
    ),
    Lesson(
        Skill.FIRST, Style.WORKED, "Exemplo resolvido: FIRST das declarações",
        listOf(
            Say(PROF, "Vamos resolver juntos, devagar, um exercício do tipo que cai em prova."),
            Code(P2_DECL, "Declarações estilo C: int a, b, c;  (vírgula e ponto e vírgula são terminais)"),
            Steps(
                "FIRST passo a passo",
                listOf(
                    "1) Terminais são o FIRST de si mesmos: FIRST(int) = {int}, FIRST(id) = {id}, FIRST(,) = {,}.",
                    "2) T → int | float: as duas produções começam com terminal. FIRST(T) = {int, float}.",
                    "3) L → id R: começa com o terminal id. FIRST(L) = {id}.",
                    "4) R → , id R | ε: a primeira começa com ',' e a segunda é ε. FIRST(R) = {',', ε}.",
                    "5) D → T L ;: começa com T, que NÃO é anulável. Copio FIRST(T) e paro. FIRST(D) = {int, float}.",
                    "6) Mais uma volta conferindo tudo: nada muda. Ponto fixo!",
                )
            ),
            Callout(WATCH_OUT, "No passo 5, NÃO continue para o L só porque deu vontade. Só se avança para o próximo símbolo quando o atual é anulável."),
            Visual(FIRST_FOLLOW, P2_DECL, caption = "Confira com o motor: os mesmos conjuntos."),
            Say(BYTE, "Agora me deixa tentar um sozinho?"),
            Say(PROF, "Exemplo 'desbotado': eu começo, você termina. Gramática de expressões LL(1)."),
            Code(P2_EXPR_LL),
            Text("De presente: FIRST(E') = {+, ε} e FIRST(T') = {*, ε}."),
            Reveal("Sua vez: FIRST(F) = ?", "{(, id}. As duas produções começam com terminal."),
            Reveal("E FIRST(T), sabendo que T → F T'?", "{(, id}. F não é anulável, então FIRST(T) = FIRST(F) e o T' nem entra na conta."),
            Reveal("Por último, FIRST(E) para E → T E'?", "{(, id}. Mesmo raciocínio: T não é anulável. Repare que ε NÃO entra em FIRST(E)."),
            Say(PROF, "Acertou duas de três? Você já está no nível de prova. Errou? Releia o passo 5: é o erro nº 1 da turma, e você acabou de vacinar o cérebro contra ele."),
        )
    ),
    Lesson(
        Skill.FIRST, Style.SOCRATIC, "Descubra a regra do FIRST sozinho",
        listOf(
            Say(PROF, "Hoje eu não vou te dar a regra. Só vou fazer perguntas. Quem descobre é você."),
            Predict(
                "A → b C. Qual é FIRST(A)?",
                listOf("{b}", "{b, C}", "FIRST(C)", "{C}"), 0,
                "Tudo que A gera começa com o terminal b. O que vem depois não interessa para o FIRST."
            ),
            Predict(
                "Agora A → B c, com B → x. FIRST(A)?",
                listOf("{c}", "{x}", "{x, c}", "{B}"), 1,
                "A começa com B, e B começa com x. FIRST(A) herda FIRST(B) = {x}. Não-terminal nunca entra no conjunto: só terminais (e talvez ε)."
            ),
            Say(BYTE, "Então quando começa com variável, eu copio o FIRST dela. Saquei."),
            Predict(
                "E se B → x | ε? Em A → B c, FIRST(A) = ?",
                listOf("{x}", "{x, ε}", "{x, c}", "{x, c, ε}"), 2,
                "Se B sumir, a cadeia começa com c. Então entram x e c. O ε não entra, porque o c nunca some."
            ),
            Predict(
                "E se for A → B C, com B → x | ε e C → y | ε?",
                listOf("{x, y}", "{x, y, ε}", "{x}", "{ε}"), 1,
                "x (B aparece), y (B some e C aparece) e ε (os dois somem: A pode gerar a cadeia vazia)."
            ),
            Reveal(
                "Qual 'regra' você acabou de descobrir? Tente dizer com suas palavras antes de tocar.",
                "Para A → X1 X2 … Xn: pegue FIRST(X1) sem ε. Se X1 é anulável, pegue também FIRST(X2) sem ε, e assim por diante. Se TODOS forem anuláveis, acrescente ε."
            ),
            Say(PROF, "É exatamente o algoritmo do Livro do Dragão. Você o reinventou."),
            Predict(
                "Última: A → A b | c (recursiva à esquerda). FIRST(A)?",
                listOf("Loop infinito, não dá pra calcular", "{c}", "{b, c}", "{A, c}"), 1,
                "A primeira regra só diz 'FIRST(A) contém FIRST(A)': nada de novo. A segunda dá c. O ponto fixo para em {c}. O cálculo não trava; quem trava com recursão à esquerda é o analisador descendente!"
            ),
            Callout(BRAIN_POWER, "Por que o cálculo precisa de várias 'voltas' (ponto fixo) em vez de uma passada só? Pense em A → B e B → A | x, visitadas nessa ordem."),
            Reveal("Responda o Poder do Cérebro.", "Na 1ª passada, ao olhar A → B, FIRST(B) ainda está vazio. Só depois B ganha x, e precisamos voltar para A copiar. Repete-se até nada mudar."),
            Visual(FIRST_FOLLOW, P2_ABC, caption = "Confirme suas descobertas: o motor aplica exatamente as regras que você deduziu."),
        )
    ),
    Lesson(
        Skill.FIRST, Style.FORMAL, "FIRST: definição e algoritmo (versão prova)",
        listOf(
            Say(PROF, "Agora de terno e gravata. Esta é a versão que você escreve na prova."),
            Text("**Definição.** Para uma cadeia α de símbolos gramaticais, FIRST(α) = { a terminal | α ⇒* a β }. Se α ⇒* ε, então ε ∈ FIRST(α) também."),
            Steps(
                "Regras para cada símbolo X",
                listOf(
                    "1. Se X é terminal, FIRST(X) = {X}.",
                    "2. Se X → ε é uma produção, acrescente ε a FIRST(X).",
                    "3. Se X → Y1 Y2 … Yk: acrescente FIRST(Y1) − {ε}; se ε ∈ FIRST(Y1), acrescente FIRST(Y2) − {ε}; e assim por diante. Se ε ∈ FIRST(Yi) para todo i, acrescente ε.",
                    "4. Repita até nenhum conjunto mudar (ponto fixo).",
                )
            ),
            Code(
                """
                para cada não-terminal A: FIRST(A) := ∅
                repita
                  para cada produção A → Y1 Y2 … Yk:
                    todosAnulaveis := verdadeiro
                    para i := 1 até k:
                      FIRST(A) := FIRST(A) ∪ (FIRST(Yi) − {ε})
                      se ε ∉ FIRST(Yi): todosAnulaveis := falso; pare
                    se todosAnulaveis: FIRST(A) := FIRST(A) ∪ {ε}
                até nenhum FIRST mudar
                """.trimIndent(),
                "Ponto fixo: os conjuntos só crescem e o número de terminais é finito, então o laço termina. (Com k = 0, A → ε, o ε entra.)"
            ),
            Text("**FIRST de uma cadeia** α = X1 X2 … Xn: mesmo raciocínio. Some FIRST(X1) − {ε} e avance enquanto o símbolo atual for anulável; ε ∈ FIRST(α) só se todos os Xi forem anuláveis (ou se α = ε)."),
            Say(BYTE, "Por que 'só crescem' garante que termina?"),
            Say(PROF, "Porque há um número finito de terminais. Cada volta que muda algo acrescenta pelo menos um elemento; uma hora acaba o que acrescentar."),
            Predict(
                "Em S → A B c, A → a | ε, B → b | ε, quanto vale FIRST(A B)?",
                listOf("{a}", "{a, b}", "{a, b, ε}", "{a, b, c}"), 2,
                "A é anulável → olho B; B também é anulável → ε entra. FIRST(A B) = {a, b, ε}. O c não faz parte da cadeia A B!"
            ),
            Visual(FIRST_FOLLOW, P2_ABC, caption = "Confira cada regra aplicada no passo a passo."),
            Callout(EXAM_TIP, "Na prova: (1) liste os anuláveis primeiro; (2) comece pelos não-terminais cujas produções começam com terminal; (3) escreva os conjuntos com chaves e diga explicitamente se ε pertence. ε em FIRST: pode. \$ em FIRST: nunca."),
            Callout(WATCH_OUT, "FIRST contém apenas terminais e, possivelmente, ε. Nunca um não-terminal."),
            Reveal("Defina FIRST em uma frase, sem olhar.", "FIRST(α) é o conjunto de terminais que podem iniciar alguma cadeia derivada de α, mais ε se α pode derivar a cadeia vazia."),
        )
    ),
    Lesson(
        Skill.FIRST, Style.MNEMONIC, "Ganchos para nunca esquecer o FIRST",
        listOf(
            Say(NARRADOR, "Palácio da Memória: você entra no Templo Top-Down. A primeira sala tem uma porta giratória com uma placa: **FIRST — quem entra primeiro**."),
            Callout(MNEMONIC, "**FIRST = Fila: quem Inicia.** Imagine a porta giratória: passa um de cada vez, e o FIRST é quem pode passar primeiro."),
            Callout(MNEMONIC, "Rima: **'Terminal na frente? Entra e pronto, minha gente. Variável na frente? Empresta o FIRST dela. Pode sumir? Olha o vizinho da janela!'**"),
            Say(BYTE, "E o ε? Quando ele entra?"),
            Callout(MNEMONIC, "**'Todos faltaram? Fila vazia: ε.'** Um único terminal (ou um não-terminal não anulável) no caminho já barra o ε."),
            Text("Contraste que salva nota: FIRST(A) olha o **começo do lado direito das regras DE A**. FOLLOW(A) (próxima sala) olha **onde A aparece** no lado direito das regras dos outros."),
            Steps(
                "Confusões clássicas lado a lado",
                listOf(
                    "FIRST pode ter ε; FOLLOW nunca tem ε.",
                    "FOLLOW do símbolo inicial sempre tem \$; FIRST nunca tem \$.",
                    "FIRST(A) olha as produções DE A; FOLLOW(A) olha as produções ONDE A aparece.",
                    "FIRST de um terminal é ele mesmo: FIRST(id) = {id}.",
                )
            ),
            Predict(
                "Qual destes conjuntos é IMPOSSÍVEL ser um FIRST?",
                listOf("{a, ε}", "{id, (}", "{+, \$}", "{ε}"), 2,
                "\$ é o marcador de fim de entrada: mora no FOLLOW, nunca no FIRST. Já {ε} é possível, por exemplo com A → ε sozinho."
            ),
            Visual(FIRST_FOLLOW, P2_PARENS, caption = "Teste o gancho em S → ( S ) S | ε: quem abre a fila? '(' — ou ninguém (ε)."),
            Reveal("Diga a rima de cabeça e aplique em S → ( S ) S | ε.", "'Terminal na frente? Entra e pronto' → '(' entra. A produção ε: 'todos faltaram' → ε entra. FIRST(S) = {(, ε}."),
            Say(PROF, "Esqueceu? Volte a esta sala do palácio: porta giratória, fila, quem inicia. O resto sai daí."),
        )
    ),
)

// ───────────────────────────── FOLLOW ─────────────────────────────

internal val p2FollowLessons: List<Lesson> = listOf(
    Lesson(
        Skill.FOLLOW, Style.STORY, "Quem vem logo depois de você na fila?",
        listOf(
            Say(NARRADOR, "Segunda sala do Templo Top-Down: um ponto de ônibus. A fila dá a volta no quarteirão."),
            Say(PROF, "Na sala do FIRST, perguntávamos quem **abre** a fila de uma turma. Aqui a pergunta muda: quem pode estar **logo atrás** da turma A, em alguma fila possível?"),
            Text("FOLLOW(A) = conjunto de **terminais que podem aparecer imediatamente à direita de A** em alguma forma sentencial. Se A puder ser o último da fila inteira, entra o **\$**: o motorista fechando a porta (fim da entrada)."),
            Code(P2_ABC, "De novo a nossa gramática da fila."),
            Say(BYTE, "Quem vem depois de B? Fácil: em S → A B c, depois do B vem c."),
            Say(PROF, "Isso! FOLLOW(B) = {c}. E depois de A?"),
            Predict(
                "Em S → A B c, com B → b | ε, quem pode vir logo depois de A?",
                listOf("Só b", "Só c", "b e c", "b, c e ε"), 2,
                "Se B aparece, depois de A vem b. Se B falta, depois de A vem direto o c. FOLLOW(A) = {b, c}. ε nunca entra no FOLLOW: alguém sempre vem depois, nem que seja o \$."
            ),
            Say(BYTE, "E o S? Ninguém aparece depois dele em regra nenhuma!"),
            Say(PROF, "S é o símbolo inicial: a frase inteira. Depois da frase inteira vem o fim da entrada. Então FOLLOW(S) = {\$}. Sempre, sem exceção, o \$ entra no FOLLOW do inicial."),
            Callout(MNEMONIC, "Regra da herança: **se B é o último da turma A, quem vem depois de A também vem depois de B.** O último da fila herda o 'depois' do grupo inteiro."),
            Visual(FIRST_FOLLOW, P2_ABC, caption = "Veja o motor: FOLLOW(S) = {\$}, FOLLOW(A) = {b, c}, FOLLOW(B) = {c}."),
            Callout(NO_DUMB_QUESTIONS, "P: Pra que saber quem vem depois? R: Para decidir quando usar A → ε! Se o próximo token é algo que pode vir DEPOIS de A, faz sentido A sumir. É assim que a tabela LL(1) usa o FOLLOW."),
            Reveal("Em S → ( S ) S | ε, o que pode vir depois de S?", "')' (pelo S de dentro dos parênteses) e \$ (S é o inicial, e o último S herda FOLLOW(S)). FOLLOW(S) = {), \$}."),
            Say(PROF, "FIRST: quem abre. FOLLOW: quem vem atrás. Duas perguntas sobre a mesma fila."),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.VISUAL, "FOLLOW ao vivo nas expressões",
        listOf(
            Say(PROF, "Mesma gramática de expressões, agora com os olhos grudados no FOLLOW."),
            Code(P2_EXPR_LL),
            Text("O motor começa colocando \$ em FOLLOW(E), porque E é o inicial. Depois percorre as produções procurando **onde cada não-terminal aparece no lado direito** e o que vem à direita dele."),
            Predict(
                "Em F → ( E ), o que entra em FOLLOW(E)?",
                listOf("(", ")", "\$", "id"), 1,
                "Logo depois do E vem o ')'. Então ')' ∈ FOLLOW(E). Junto com o \$, FOLLOW(E) = {), \$}."
            ),
            Visual(FIRST_FOLLOW, P2_EXPR_LL, caption = "Vá até a parte FOLLOW e leia a justificativa de cada passo."),
            Steps(
                "O que observar",
                listOf(
                    "E → T E': depois de T vem E', então FIRST(E') − {ε} = {+} entra em FOLLOW(T).",
                    "Como E' é anulável, FOLLOW(E) também entra em FOLLOW(T).",
                    "E' está no fim de E → T E': FOLLOW(E') herda FOLLOW(E) = {), \$}.",
                    "T → F T': FOLLOW(F) ganha FIRST(T') − {ε} = {*} e, como T' pode sumir, herda FOLLOW(T).",
                    "Repete as voltas até nada mudar.",
                )
            ),
            Say(BYTE, "Tá, mas quanto deu no fim?"),
            Reveal("Tente antes de tocar: FOLLOW(T) = ?", "{+, ), \$}: o + vem de FIRST(E'), e ) e \$ vêm herdados de FOLLOW(E)."),
            Reveal("E FOLLOW(F) = ?", "{*, +, ), \$}: o * vem de FIRST(T'); o resto é herdado de FOLLOW(T)."),
            Callout(BRAIN_POWER, "Repare: FOLLOW(E') = FOLLOW(E) e FOLLOW(T') = FOLLOW(T). Por quê? Dica: onde E' e T' aparecem nas regras?"),
            Reveal("Responda o Poder do Cérebro.", "E' só aparece no FIM das produções de E e de E' (E → T E', E' → + T E'), então só recebe herança de FOLLOW(E) e de si mesmo. O mesmo vale para T' com T."),
            Visual(FIRST_FOLLOW, P2_PARENS, caption = "Bônus: S → ( S ) S | ε. Veja o ')' e o \$ chegando em FOLLOW(S)."),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.WORKED, "Exemplo resolvido: FOLLOW passo a passo",
        listOf(
            Say(PROF, "Calcular FOLLOW sem FIRST é como fazer bolo sem farinha. Então começamos com os FIRST prontos."),
            Code(P2_DECL),
            Text("Do exercício de FIRST: FIRST(D) = FIRST(T) = {int, float}, FIRST(L) = {id}, FIRST(R) = {',', ε}. R é o único anulável."),
            Steps(
                "FOLLOW passo a passo",
                listOf(
                    "1) D é o inicial: FOLLOW(D) = {\$}.",
                    "2) Em D → T L ;, depois de T vem L. FIRST(L) = {id} entra em FOLLOW(T). FOLLOW(T) = {id}.",
                    "3) Na mesma regra, depois de L vem ';'. FOLLOW(L) = {;}.",
                    "4) Em L → id R, R está no fim: FOLLOW(R) herda FOLLOW(L) = {;}.",
                    "5) Em R → , id R, R está no fim de novo: herda FOLLOW(R) de si mesmo. Nada novo.",
                    "6) Mais uma volta: nada muda. Ponto fixo.",
                )
            ),
            Say(BYTE, "Pera: no passo 2, por que o ';' não entra em FOLLOW(T) também?"),
            Say(PROF, "Porque L **não é anulável**: ele sempre gera pelo menos um id. Então o ';' nunca encosta no T."),
            Visual(FIRST_FOLLOW, P2_DECL, caption = "Confira: FOLLOW(D) = {\$}, FOLLOW(T) = {id}, FOLLOW(L) = FOLLOW(R) = {;}."),
            Say(PROF, "Agora o desbotado. Gramática S → A B c, A → a | ε, B → b | ε, com FIRST(A) = {a, ε} e FIRST(B) = {b, ε}."),
            Text("Eu faço o primeiro: S é o inicial, então FOLLOW(S) = {\$}. S não aparece em nenhum lado direito, então acabou para ele."),
            Reveal("Sua vez: FOLLOW(B)?", "{c}. Em S → A B c, depois de B vem o terminal c."),
            Reveal("E FOLLOW(A)? Cuidado com o B anulável.", "{b, c}. Depois de A vem B: entra FIRST(B) − {ε} = {b}. Como B pode sumir, olho o próximo: c."),
            Callout(WATCH_OUT, "Colocou ε em FOLLOW(A)? Normal, acontece com todo mundo! Mas ε NUNCA está em FOLLOW. O ε de FIRST(B) é só um sinal de 'olhe mais à direita'."),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.SOCRATIC, "Perguntas que levam ao FOLLOW",
        listOf(
            Say(PROF, "Gramática pequenininha: S → a S b | ε. Ela gera a b, a a b b, a a a b b b… e a cadeia vazia."),
            Visual(DERIVATION, P2_ANBN, "a a b b", "Olhe as formas sentenciais: a S b, a a S b b… O que aparece logo depois de S?"),
            Predict(
                "Olhando a derivação, que terminal aparece logo à direita de S nas formas sentenciais?",
                listOf("a", "b", "ε", "nenhum"), 1,
                "Em a S b e a a S b b, o S está sempre colado num b. Então b ∈ FOLLOW(S)."
            ),
            Predict(
                "E na forma sentencial inicial, que é só 'S'? O que vem depois?",
                listOf("Nada, então FOLLOW fica vazio", "O fim da entrada, \$", "ε", "a"), 1,
                "Depois da frase completa vem o fim da entrada. Por isso a regra: \$ ∈ FOLLOW(símbolo inicial)."
            ),
            Say(BYTE, "Então FOLLOW(S) = {b, \$}. Mas por que olhar as regras em vez de derivar tudo?"),
            Say(PROF, "Porque as derivações são infinitas! As regras resumem tudo: onde S aparece num lado direito, olho o que está à direita dele."),
            Predict(
                "Nova gramática: A → x B y. O que você conclui sobre FOLLOW(B)?",
                listOf("y ∈ FOLLOW(B)", "x ∈ FOLLOW(B)", "FOLLOW(B) ⊆ FOLLOW(A)", "Nada"), 0,
                "B é seguido de y. O FIRST do que vem à direita (aqui, y) entra em FOLLOW(B)."
            ),
            Predict(
                "E se for A → x B, com B no fim?",
                listOf("FOLLOW(B) fica vazio", "FOLLOW(A) ⊆ FOLLOW(B)", "FOLLOW(B) ⊆ FOLLOW(A)", "x ∈ FOLLOW(B)"), 1,
                "Tudo que pode vir depois de A pode vir depois de B, porque B termina A. A herança vai do PAI (lado esquerdo) para o filho do fim."
            ),
            Predict(
                "E se for A → x B C, com C anulável?",
                listOf("Só FIRST(C) − {ε}", "Só FOLLOW(A)", "FIRST(C) − {ε} e também FOLLOW(A)", "FIRST(C) com ε"), 2,
                "C pode aparecer (entra FIRST(C) sem ε) ou sumir (aí B fica no fim e herda FOLLOW(A))."
            ),
            Reveal("Junte tudo: escreva as três regras do FOLLOW com suas palavras.", "1) \$ no FOLLOW do inicial. 2) Em A → α B β, FIRST(β) − {ε} entra em FOLLOW(B). 3) Se β é vazio ou anulável, FOLLOW(A) entra em FOLLOW(B)."),
            Visual(FIRST_FOLLOW, P2_ANBN, caption = "Confira: FOLLOW(S) = {b, \$}."),
            Say(PROF, "Você acabou de reconstruir o algoritmo do Livro do Dragão. Se tropeçou no caminho, ótimo: foi o tropeço que fixou a regra."),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.FORMAL, "FOLLOW: definição e algoritmo (versão prova)",
        listOf(
            Say(PROF, "Versão prova, com todas as letras."),
            Text("**Definição.** Para um não-terminal A, FOLLOW(A) é o conjunto de terminais a que podem aparecer imediatamente à direita de A em alguma forma sentencial, isto é, S ⇒* α A a β. Se A pode ser o símbolo mais à direita de uma forma sentencial, \$ ∈ FOLLOW(A)."),
            Steps(
                "Regras (aplique até nada mudar)",
                listOf(
                    "1. Coloque \$ em FOLLOW(S), onde S é o símbolo inicial.",
                    "2. Para cada produção A → α B β: tudo em FIRST(β), exceto ε, entra em FOLLOW(B).",
                    "3. Para cada produção A → α B, ou A → α B β com ε ∈ FIRST(β): tudo em FOLLOW(A) entra em FOLLOW(B).",
                )
            ),
            Code(
                """
                para cada não-terminal A: FOLLOW(A) := ∅
                FOLLOW(S) := { ${'$'} }
                repita
                  para cada produção A → X1 X2 … Xn:
                    para i := 1 até n:
                      se Xi é um não-terminal B:
                        β := Xi+1 … Xn
                        FOLLOW(B) := FOLLOW(B) ∪ (FIRST(β) − {ε})
                        se ε ∈ FIRST(β):        // β vazio ou anulável
                          FOLLOW(B) := FOLLOW(B) ∪ FOLLOW(A)
                até nenhum FOLLOW mudar
                """.trimIndent(),
                "Repare: FIRST(ε) = {ε}. Por isso, quando B está no fim (β vazio), a regra 3 dispara."
            ),
            Predict(
                "Qual afirmação é SEMPRE verdadeira?",
                listOf("ε pode estar em FOLLOW", "\$ ∈ FOLLOW(símbolo inicial)", "FOLLOW(A) ⊆ FIRST(A)", "FOLLOW de um terminal é ele mesmo"), 1,
                "É a regra 1. FOLLOW nunca contém ε, e não existe relação geral entre FOLLOW(A) e FIRST(A)."
            ),
            Visual(FIRST_FOLLOW, P2_DECL, caption = "Cada passo do widget cita a regra usada. Tente prever o próximo antes de avançar."),
            Callout(EXAM_TIP, "Monte uma tabelinha Não-terminal | FIRST | FOLLOW. Faça TODOS os FIRST antes. No FOLLOW, para cada não-terminal, varra as regras procurando onde ele aparece do lado DIREITO e anote a regra usada (2 ou 3). Professor adora ver a justificativa."),
            Callout(WATCH_OUT, "Direção da herança: a regra 3 é FOLLOW(A) ⊆ FOLLOW(B), do lado esquerdo para o símbolo do fim. Nunca o contrário."),
            Reveal("Para S → A a A b | B b B a, A → ε, B → ε: quanto valem FOLLOW(A) e FOLLOW(B)?", "FOLLOW(A) = {a, b} e FOLLOW(B) = {a, b}. Depois do primeiro A vem a, depois do segundo vem b; o mesmo para B. (Exemplo clássico: a gramática é LL(1) mesmo com A e B anuláveis.)"),
            Say(BYTE, "Então: \$ no inicial, olho à direita, e se acabou ou pode sumir, herdo do pai."),
            Say(PROF, "Essa é a prova inteira numa frase só."),
        )
    ),
    Lesson(
        Skill.FOLLOW, Style.MNEMONIC, "Dólar, Vizinho, Herança",
        listOf(
            Say(NARRADOR, "Segunda sala do Templo Top-Down: um ponto de ônibus com a placa **FOLLOW — quem vem atrás**. O ônibus se chama \$ e sempre para no fim da linha."),
            Callout(MNEMONIC, "**As 3 regras do FOLLOW = Dólar, Vizinho, Herança.** Dólar: \$ no inicial. Vizinho: quem está à direita empresta o FIRST (sem ε). Herança: o filho do fim herda o FOLLOW do pai."),
            Callout(MNEMONIC, "Rima: **'Ficou no fim, ou o resto pode sumir? Herda do pai o que vai vir!'**"),
            Say(BYTE, "Eu sempre confundo a direção da herança…"),
            Say(PROF, "Pensa em herança de verdade: desce do pai para o filho. FOLLOW(pai, o lado esquerdo) desce para FOLLOW(filho do fim). Filho não deixa herança pro pai."),
            Steps(
                "FIRST × FOLLOW: o duelo",
                listOf(
                    "FIRST: quem ABRE a fila. FOLLOW: quem vem ATRÁS.",
                    "FIRST pode ter ε. FOLLOW: jamais ε.",
                    "FOLLOW pode ter \$. FIRST: jamais \$.",
                    "FIRST(A): olho as regras DE A. FOLLOW(A): olho as regras ONDE A aparece à direita.",
                    "FOLLOW depende de FIRST; FIRST não depende de FOLLOW.",
                )
            ),
            Predict(
                "Byte calculou FOLLOW(X) = {a, ε, \$}. Onde está o erro?",
                listOf("Falta o b", "O \$ não pode", "O ε não pode", "Está certo"), 2,
                "ε nunca entra em FOLLOW. Se apareceu, ele provavelmente copiou FIRST(β) sem tirar o ε — quando devia ter aplicado a Herança."
            ),
            Visual(FIRST_FOLLOW, P2_ANBN, caption = "Teste o gancho em S → a S b | ε: Dólar dá \$; Vizinho dá b. FOLLOW(S) = {b, \$}."),
            Callout(FIRESIDE, "FIRST e FOLLOW ao pé da lareira. FIRST: 'Eu sou o começo de tudo!' FOLLOW: 'E eu sou o que vem depois. Mas não vivo sem você: sempre que olho o vizinho, peço o FIRST dele emprestado.' FIRST: 'E quando o vizinho some?' FOLLOW: 'Aí peço ao meu pai. Família é pra isso.'"),
            Reveal("Diga as 3 regras pelo gancho 'Dólar, Vizinho, Herança'.", "Dólar: \$ ∈ FOLLOW(inicial). Vizinho: em A → α B β, FIRST(β) − {ε} ⊆ FOLLOW(B). Herança: se β é vazio ou anulável, FOLLOW(A) ⊆ FOLLOW(B)."),
        )
    ),
)
