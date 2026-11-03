package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 4c do conteúdo: ERROR_RECOVERY (Templo Top-Down — Sala do Pânico).
// Posições de detecção e eventos de recuperação conferidos com PanicMode.trace / firstErrorPosition.

/** Lista de comandos de atribuição separados por ';' (LL(1)). FIRST(E) = {id, num}, FOLLOW(E) = {;}. */
internal const val P4_CMDS = "P -> S ; P | ε\nS -> id = E\nE -> id | num"

/** Expressões LL(1) do livro. */
internal const val P4_EXPR_LL = "E -> T E'\nE' -> + T E' | ε\nT -> F T'\nT' -> * F T' | ε\nF -> ( E ) | id"

private const val P4_BISON_ERRO =
    "%{\n#include <stdio.h>\nextern int yylineno;      /* %option yylineno no scanner.l */\nint yylex(void);\nvoid yyerror(const char *s);\n%}\n%define parse.error verbose\n%token ID NUM\n%%\nprograma : programa comando\n         | comando\n         ;\ncomando  : ID '=' expr ';'\n         | error ';'          { yyerrok; }\n         ;\nexpr     : ID\n         | NUM\n         ;\n%%\nvoid yyerror(const char *s) {\n    fprintf(stderr, \"Erro sintatico na linha %d: %s\\n\", yylineno, s);\n}"

// ───────────────────────────── ERROR_RECOVERY ─────────────────────────────

internal val p4ErrorLessons: List<Lesson> = listOf(
    Lesson(
        Skill.ERROR_RECOVERY, Style.STORY, "O corretor de redação do ENEM",
        listOf(
            Say(NARRADOR, "Uma pilha de 300 redações e um café frio. Na linha 3 da primeira, o corretor acha um erro."),
            Say(BYTE, "Aí ele rasga a redação e vai embora?"),
            Say(PROF, "Claro que não! Ele marca o erro, acha um ponto seguro para continuar — o próximo ponto final — e segue lendo. No fim, o aluno recebe TODOS os erros de uma vez. Um compilador bom faz igual."),
            Steps(
                "Os tipos de erro, na redação e no código",
                listOf(
                    "✏️ 'Caza' em vez de 'casa' → **erro léxico**: um caractere ou palavra que não existe (em C, um `@` solto).",
                    "🧱 Frase sem verbo, parêntese sem fechar → **erro sintático**: a estrutura está quebrada (`x = (a + ;`).",
                    "🤯 'O sol congelou a feijoada' → **erro semântico**: estrutura ok, sentido não (somar string com int, variável não declarada).",
                    "🎯 Texto perfeito, mas fugiu do tema → **erro lógico**: compila e roda, mas faz a coisa errada (`if (x = 0)` no lugar de `==`).",
                ),
            ),
            Steps(
                "As quatro estratégias do corretor",
                listOf(
                    "🏃 **Modo pânico**: pula tudo até o próximo ponto final e recomeça dali. Simples e nunca entra em laço; mas pode pular um trecho grande.",
                    "🩹 **Nível de frase**: faz um remendo local — 'aqui faltou uma vírgula, finjo que ela está lá'. Cuidado: remendos mal escolhidos podem gerar laços infinitos.",
                    "📋 **Produções de erro**: o gabarito já prevê os erros mais comuns ('mais melhor') e tem uma mensagem pronta para cada um.",
                    "🧠 **Correção global**: reescrever a redação com o MÍNIMO de mudanças até ela ficar certa. Lindo na teoria, caro demais na prática.",
                ),
            ),
            Predict(
                "Qual dessas estratégias os compiladores de verdade mais usam?",
                listOf("Correção global", "Modo pânico (às vezes com remendos de frase)", "Nenhuma: param no primeiro erro", "Produções de erro para todo erro possível"),
                1,
                "Pânico é simples e seguro. A correção global é cara demais; produções de erro só para casos frequentes.",
            ),
            Say(PILHA, "No meu Templo, o 'ponto final' se chama **token de sincronização**. Em C, os favoritos são `;` e `}`."),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num id = num ;", caption = "Faltou um ';' depois do primeiro comando. Veja em que token o erro aparece e como o parser segue em frente."),
            Reveal("O que o parser fez com o ';' que faltou?", "Com o terminal ';' no topo da pilha e 'id' na entrada, ele finge que o ';' estava lá: desempilha o ';' e continua. É um remendo em nível de frase — exatamente o 'finjo que a vírgula está aqui' do corretor."),
            Callout(FIRESIDE, "Corretor: 'Eu não conserto a redação, só aponto.' Compilador: 'Eu também! Recuperar não é corrigir o programa: é só voltar a um estado em que eu consiga achar o PRÓXIMO erro.'"),
            Reveal("Na analogia, quem é: ponto final, remendo, gabarito de erros comuns e reescrita mínima?", "Token de sincronização (modo pânico), recuperação em nível de frase, produções de erro e correção global."),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.VISUAL, "Sala do Pânico ao vivo",
        listOf(
            Say(PILHA, "Bem-vindo à Sala do Pânico. O token vermelho é onde eu PERCEBO o erro. Depois, avance passo a passo e veja como eu me recupero."),
            Code(P4_CMDS, "Gramática: uma lista de atribuições. FIRST(E) = {id, num}, FOLLOW(E) = {;}"),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = = num ; id = num ;", caption = "Um '=' a mais. Com E no topo e '=' na entrada, a célula M[E, =] está vazia."),
            Text("Regra do pânico: com o não-terminal A no topo e a célula vazia, **descarte tokens** até achar um que (1) está em **FIRST(A)** — aí A continua no topo — ou (2) está em **FOLLOW(A) ∪ {\$}** — aí A é desempilhado."),
            Predict(
                "No exemplo acima, o parser descarta o '=' e encontra 'num'. O que ele faz?",
                listOf("Desempilha E", "Continua com E, porque num ∈ FIRST(E)", "Desiste", "Volta ao início do programa"),
                1,
                "'num' pode começar um E, então dá para seguir com E → num. Só um token foi jogado fora.",
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = ; id = num ;", caption = "Agora a expressão SUMIU. O que acontece quando o próximo token já é um ';'?"),
            Reveal("Por que aqui E foi desempilhado sem descartar nada?", "O ';' não começa um E, mas está em FOLLOW(E): é o que viria logo DEPOIS de um E. O parser 'desiste' de E e sincroniza no ';'."),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id num ; id = num ;", caption = "Faltou o '='. Desta vez quem está no topo é um TERMINAL."),
            Text("Terminal no topo diferente da entrada: o parser finge que o terminal estava lá e o desempilha. É uma recuperação simples, de nível de frase (uma 'inserção')."),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num num ; id = id ;", caption = "Um 'num' a mais. Conte quantas recuperações aparecem."),
            Callout(WATCH_OUT, "UM erro de digitação gerou DUAS recuperações: primeiro o parser finge um ';' e depois descarta 'num ;'. É o **erro em cascata** — por isso, depois do primeiro erro, desconfie das mensagens seguintes."),
            Say(BYTE, "Funciona com expressões também?"),
            Visual(VK.PANIC, grammar = P4_EXPR_LL, input = "id + * id", caption = "Na gramática de expressões: o '*' aparece onde deveria começar um termo T."),
            Reveal("Qual token é descartado em 'id + * id' e por quê?", "O '*'. Com T no topo, M[T, *] está vazia; '*' não está em FIRST(T) = {(, id} nem em FOLLOW(T) = {+, ), \$}. O próximo, 'id', está em FIRST(T): o parser segue com T."),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.WORKED, "Rastreando o pânico na mão",
        listOf(
            Say(PROF, "Vamos rastrear com calma o modo pânico em `id = ; id = num ;` — o programador esqueceu o valor da primeira atribuição."),
            Code(P4_CMDS, "FIRST(P) = {id, ε}   FIRST(S) = {id}   FIRST(E) = {id, num}\nFOLLOW(P) = {\$}   FOLLOW(S) = {;}   FOLLOW(E) = {;}"),
            Steps(
                "Resolvido (topo da pilha à direita)",
                listOf(
                    "Pilha `\$ P`, entrada `id = ; id = num ; \$`: M[P, id] = P → S ; P. Pilha vira `\$ P ; S`.",
                    "M[S, id] = S → id = E. Pilha `\$ P ; E = id`.",
                    "Casa `id`, casa `=`. Pilha `\$ P ; E`, entrada `; id = num ; \$`.",
                    "🚨 M[E, ;] está vazia → **erro detectado no 3º token**. Mensagem: 'esperava id ou num, encontrou ;'.",
                    "Pânico: ';' ∉ FIRST(E), mas ';' ∈ FOLLOW(E). Nada a descartar: **desempilha E**.",
                    "Pilha `\$ P ;`: casa `;`. O resto, `id = num ;`, é analisado normalmente.",
                    "No fim, P → ε com `\$` e o parser chega ao fim: 1 erro relatado, análise completa.",
                ),
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = ; id = num ;", caption = "Confira cada passo com o motor."),
            Callout(WATCH_OUT, "Depois de um erro, o parser NÃO gera código: a recuperação serve só para continuar procurando erros. O programa só compila de verdade quando a lista de erros fica vazia."),
            Text("Sua vez (exemplo esmaecido): `id = = num ; id = num ;`."),
            Reveal("Em que token o erro é detectado e quem está no topo da pilha?", "No 3º token, o segundo '='. No topo está E (já casamos id e =)."),
            Reveal("O que o pânico faz?", "'=' não está em FIRST(E) nem em FOLLOW(E): descarta '='. O próximo, 'num', está em FIRST(E): para de descartar e segue com E → num."),
            Reveal("E em `id num ; id = num ;`, o que muda?", "O erro aparece com o TERMINAL '=' no topo e 'num' na entrada. O parser finge que o '=' estava lá (desempilha o '=') e continua com E → num."),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id num ; id = num ;", caption = "Confira a última resposta."),
            Say(PROF, "No Bison, você não escreve o pânico na mão: usa o token especial `error` numa produção."),
            Code(P4_BISON_ERRO, "parser.y — 'error ;' descarta tudo até o próximo ';' e segue para o próximo comando"),
            Callout(EXAM_TIP, "Na prova, rastreie em tabela (pilha | entrada | ação) e escreva explicitamente: 'M[E, ;] vazia → erro; ; ∈ FOLLOW(E) → desempilha E'. Mostrar o conjunto de sincronização vale ponto."),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.SOCRATIC, "Por que não parar no primeiro erro?",
        listOf(
            Say(PROF, "Perguntas, só perguntas. Você vai deduzir como um compilador deve reagir a erros."),
            Predict(
                "Seu programa tem 5 erros de sintaxe. O compilador para no primeiro. Quantas vezes você compila até achar todos?",
                listOf("1", "Pelo menos 5", "Nenhuma", "Depende do processador"),
                1,
                "Um erro por compilação = pelo menos 5 rodadas. Por isso o compilador tenta se recuperar e relatar o máximo de erros de uma vez.",
            ),
            Predict(
                "Em `id = num num ; id = id ;`, em qual token o parser LL(1) PERCEBE o erro?",
                listOf("No 1º (id)", "No 3º (o primeiro num)", "No 4º (o segundo num)", "Só no fim"),
                2,
                "'id = num' ainda pode virar um programa válido (basta vir um ';'). O 2º 'num' é o primeiro token que torna isso impossível.",
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num num ; id = id ;", caption = "O vermelho marca o ponto de detecção."),
            Say(BYTE, "Ele grita exatamente no primeiro token impossível. Isso tem nome?"),
            Say(PROF, "Tem: **propriedade do prefixo viável**. LL e LR detectam o erro assim que o que foi lido não pode mais ser o começo de nenhum programa válido."),
            Predict(
                "Com E no topo e um token inesperado, por que usar FOLLOW(E) como sincronizador?",
                listOf("Porque é o menor conjunto", "Porque se o próximo token é algo que vem DEPOIS de um E, dá para fingir que o E acabou e seguir", "Porque FIRST é sempre vazio", "Por tradição"),
                1,
                "Desempilhar E e continuar com o que vem depois dele é a aposta mais segura: a pilha volta a combinar com a entrada.",
            ),
            Predict(
                "E por que também parar num token de FIRST(E)?",
                listOf("Porque aí dá para recomeçar o próprio E, sem jogar fora o resto", "Não deveria parar", "Para gerar mais erros", "Porque FIRST = FOLLOW"),
                0,
                "Em `id = = num`, depois de descartar o '=' extra, 'num' começa um E: não há por que abandonar o E.",
            ),
            Reveal("Por que ';' e '}' são ótimos tokens de sincronização em C?", "Porque marcam FINS claros de construções (comando e bloco). Pular até eles costuma descartar só o comando quebrado, e o resto do programa é analisado normalmente."),
            Reveal("Qual o risco de sincronizar no lugar errado?", "Erros em cascata: o parser volta 'desalinhado' e acusa erros que não existem. Por isso compiladores esperam alguns tokens corretos antes de relatar o próximo erro (o Bison espera 3)."),
            Visual(VK.PANIC, grammar = P4_EXPR_LL, input = "( id + ) * id", caption = "Desafio: antes de avançar, preveja se o ')' será descartado ou usado como sincronizador de T."),
            Reveal("Resposta do desafio", "Sincronizador: ')' ∈ FOLLOW(T) = {+, ), \$}. Nada é descartado, T é desempilhado e o ')' fecha o parêntese normalmente."),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.FORMAL, "Tratamento de erros sintáticos: definição de prova",
        listOf(
            Text("**Objetivos do tratador de erros** (Dragão, 4.1.3): (1) relatar a presença de erros de forma clara e precisa; (2) recuperar-se de cada erro rápido o bastante para detectar erros subsequentes; (3) acrescentar custo mínimo ao processamento de programas corretos."),
            Text("**Propriedade do prefixo viável**: os métodos LL e LR detectam um erro assim que o prefixo da entrada já lido não pode ser completado para formar uma cadeia da linguagem. O erro é acusado no PRIMEIRO token que torna isso impossível."),
            Steps(
                "Estratégias de recuperação",
                listOf(
                    "**Modo pânico**: descartar símbolos da entrada, um a um, até encontrar um **token de sincronização** de um conjunto escolhido (ex.: `;`, `}`, FOLLOW(A)). Simples e livre de laços infinitos.",
                    "**Nível de frase**: correção local do restante da entrada — inserir, remover ou substituir um token (ex.: inserir `;` que falta). Risco de laço se sempre inserir antes do token atual.",
                    "**Produções de erro**: aumentar a gramática com produções que geram as construções erradas mais comuns, emitindo diagnósticos específicos.",
                    "**Correção global**: achar a cadeia correta y mais próxima da entrada x (mínimo de inserções, remoções e trocas). Interesse teórico: custo alto demais.",
                ),
            ),
            Code(
                """
                com A (não-terminal) no topo e a = próximo token:
                    se M[A, a] tem produção: expanda normalmente
                    senão:  // erro
                        relate o erro (linha, esperado × encontrado)
                        enquanto a ∉ FIRST(A) e a ∉ FOLLOW(A) ∪ {${'$'}}:
                            descarte a; a ← próximo token
                        se a ∈ FIRST(A): continue com A no topo
                        senão: desempilhe A   // sincronizou em FOLLOW(A)
                com terminal t no topo e t ≠ a:
                    relate "esperava t"; desempilhe t  // finge que t estava lá
                """.trimIndent(),
                "Modo pânico no analisador LL(1) (Dragão, 4.4.5)",
            ),
            Visual(VK.LL1_TABLE, grammar = P4_CMDS, caption = "Toda célula vazia desta tabela é um ponto de erro. As colunas de FOLLOW(A) são as 'synch' de cada linha."),
            Predict(
                "Na tabela do livro, uma célula M[A, a] marcada 'synch' significa:",
                listOf("Descartar a", "Desempilhar A, pois a ∈ FOLLOW(A)", "Aceitar a entrada", "Expandir A → ε"),
                1,
                "Célula em branco → descarta o token. 'synch' → desempilha A. Terminal no topo diferente do token → desempilha o terminal.",
            ),
            Text("**No Yacc/Bison**: o token reservado `error` pode aparecer em produções, como `comando : error ';'`. Ao detectar um erro, o parser chama **yyerror**, desempilha estados até um que tenha ação para `error`, empilha `error` e descarta tokens até um que possa segui-lo. Para não gerar cascata, só relata o próximo erro depois de 3 tokens empilhados com sucesso; **yyerrok** encerra esse modo imediatamente e **yyclearin** descarta o token atual."),
            Predict(
                "Qual NÃO é objetivo do tratador de erros?",
                listOf("Relatar erros com clareza", "Detectar erros subsequentes", "Corrigir o programa automaticamente para que ele rode", "Custo mínimo em programas corretos"),
                2,
                "Recuperar ≠ corrigir. A recuperação serve para continuar a análise e achar mais erros; o código não é gerado.",
            ),
            Callout(EXAM_TIP, "Cai muito: 'cite e explique as estratégias de recuperação de erros'. Escreva as quatro, com uma vantagem e uma desvantagem de cada, e diga que o modo pânico usa FOLLOW (e FIRST) como sincronizadores. Cite a propriedade do prefixo viável."),
            Reveal("Defina propriedade do prefixo viável em uma frase.", "O analisador detecta o erro assim que o prefixo lido da entrada deixa de ser prefixo de alguma sentença da linguagem — nunca depois disso."),
        ),
    ),
    Lesson(
        Skill.ERROR_RECOVERY, Style.MNEMONIC, "FIRST Fica, FOLLOW Foge",
        listOf(
            Callout(MNEMONIC, "**FIRST Fica, FOLLOW Foge.** Achou um token de FIRST(A)? A FICA no topo e recomeça. Achou um de FOLLOW(A)? A FOGE (é desempilhado). Nenhum dos dois? Joga o token fora."),
            Callout(MNEMONIC, "**Terminal teimoso? Finge que estava lá.** Terminal no topo que não bate com a entrada é desempilhado — como pular uma vírgula esquecida."),
            Text("📍 **Palácio da memória**: no Templo Top-Down, atrás do altar da tabela LL(1), há uma porta com luz vermelha: a **Sala do Pânico** 🚨. O Pilha corre por ela jogando tokens pela janela até ver uma placa de saída — `;` ou `}` — pintada com as cores do FOLLOW."),
            Callout(MNEMONIC, "Tipos de erro em zoom crescente: **Letra → Frase → Sentido → Resultado**. Léxico (palavra que não existe), sintático (frase mal montada), semântico (frase sem sentido), lógico (tudo compila, mas o resultado sai errado)."),
            Callout(MNEMONIC, "Estratégias, da mais simples à mais ambiciosa: **P**ânico **P**ula, **F**rase **F**az remendo, **P**rodução **P**revê, **G**lobal é **G**ênio — mas ninguém paga o salário dele."),
            Predict(
                "E no topo, token 'x' na entrada, x ∉ FIRST(E) e x ∉ FOLLOW(E). O que fazer?",
                listOf("Desempilhar E", "Descartar x e olhar o próximo token", "Aceitar", "Empilhar x"),
                1,
                "Nem fica, nem foge: o token vai pela janela. Repita até achar alguém de FIRST (fica) ou de FOLLOW (foge).",
            ),
            Visual(VK.PANIC, grammar = P4_CMDS, input = "id = num ; = = ; id = id ;", caption = "Recite enquanto avança: o lixo '= = ;' vai pela janela até aparecer 'id', que está em FIRST(P). P fica!"),
            Callout(MNEMONIC, "**Prefixo viável = grito imediato**: LL e LR gritam no PRIMEIRO token impossível, nunca depois. Se a mensagem aponta a linha 10, o erro real está na linha 10 ou ANTES."),
            Callout(MNEMONIC, "Bison: **`error` é o coringa** que casa com o lixo; **yyerrok** é o 'tá ok, pode reclamar de novo'; **yyerror** é o megafone que imprime a mensagem."),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que recuperar = corrigir o programa; (2) usar FIRST como conjunto de 'desempilhar' (é FOLLOW!); (3) esquecer o \$ entre os sincronizadores; (4) confiar cegamente na 2ª, 3ª… mensagem — pode ser cascata."),
            Reveal("Recite: o que fazer com A no topo e célula vazia?", "Descarta tokens até achar um de FIRST(A) — A fica — ou de FOLLOW(A) ∪ {\$} — A foge (desempilha). FIRST Fica, FOLLOW Foge."),
            Callout(REAL_WORLD, "Mensagem boa de verdade tem linha e 'esperado × encontrado': 'Erro sintatico na linha 7: esperava ';' mas encontrou 'id''. É o que o professor quer ver no seu TP."),
        ),
    ),
)
