package com.compilandia.content

import com.compilandia.learning.Skill

// Parte 6 do conteúdo: DFA_MINIMIZATION (LessonsPart6a.kt), AST_BUILD (LessonsPart6b.kt) e REG_ALLOC (LessonsPart6c.kt).

private val p6Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.DFA_MINIMIZATION,
        oneLiner = "Minimizar um AFD é fundir os estados equivalentes (que nenhuma cadeia consegue distinguir): marca-se todo par final × não-final, propaga-se a marca pelas transições até estabilizar, e os pares que sobram sem marca viram um estado só — o AFD mínimo, único a menos de nomes.",
        hook = "MPJ: Marca, Propaga, Junta. O morto também vota.",
        feynmanPrompt = "Explique para um amigo de 12 anos como o dono de uma lanchonete descobre quais atendentes fazem exatamente o mesmo trabalho, e por que, no AFD de (a|b)*abb, os estados q0 e q2 viram um só.",
        keyTerms = listOf("equivalen", "distingu", "par", "marca", "final", "morto", "tabela"),
        commonMistakes = listOf(
            "Esquecer de completar o AFD com o estado morto antes de montar a tabela",
            "Marcar um par com base num par de destino que ainda NÃO está marcado",
            "Parar depois de uma rodada, sem repetir até nada mudar",
            "Esquecer de remover estados inalcançáveis",
            "Fazer a tabela e não desenhar o AFD mínimo (com inicial e finais) no fim",
        ),
        flashcards = listOf(
            "Quando dois estados são equivalentes?" to "Quando nenhuma cadeia w leva um a final e o outro a não-final",
            "O que a rodada 1 marca?" to "Todo par (final, não-final) — a testemunha é a cadeia vazia",
            "Regra das rodadas seguintes?" to "Marque {p,q} se, para alguma letra c, {δ(p,c), δ(q,c)} já está marcado",
            "Quando parar?" to "Quando uma rodada inteira não marca nenhum par novo",
            "O que fazer se falta transição?" to "Completar com o estado morto ∅ (não final, laço em tudo)",
            "Quantas casas tem a tabela com n estados?" to "n(n−1)/2 (só a escadinha de baixo)",
            "No AFD do livro para (a|b)*abb, quem se funde?" to "q0 e q2 (A e C): 5 estados viram 4",
            "Método alternativo à tabela?" to "Refinar partições (Moore/Hopcroft): começa com {F, S−F} e divide grupos",
            "O AFD mínimo é único?" to "Sim, a menos de renomear os estados (Myhill–Nerode)",
        ),
    ),
    SkillCard(
        skill = Skill.AST_BUILD,
        oneLiner = "A árvore sintática abstrata guarda só operadores, operandos e construções (BinOp, Var, Num, Atrib, Se, Enquanto, Bloco), jogando fora correntes de não-terminais, parênteses, pontuação e ε; o parser a constrói (new BinOp no descendente ou \$\$ = new BinOp(...) no Bison) e as fases seguintes andam nela.",
        hook = "AST = Árvore Sem Tralha: corta o CPE (Correntes, Parênteses, Epsilons). Quem está mais fundo trabalha primeiro.",
        feynmanPrompt = "Explique para um amigo de 12 anos a diferença entre ler o livro inteiro e ler um bom resumo, e por que a árvore de 'a + b * c' cai de 13 nós para 5 sem perder a ordem das contas.",
        keyTerms = listOf("abstrat", "arvore", "classe", "binop", "parentes", "precedenc", "parser"),
        commonMistakes = listOf(
            "Criar nós para parênteses ou para não-terminais de corrente (E → T → F)",
            "Montar a AST pendurada para a direita (recursão em vez de laço) e inverter a associatividade de - e /",
            "Achar que sem parênteses a precedência se perde (ela mora na forma da árvore)",
            "Confundir AST (estrutura em árvore) com TAC (lista de instruções gerada a partir dela)",
            "Esquecer \$\$ = \$2 na regra do parêntese ou \$\$ = \$1 nas correntes do Bison",
        ),
        flashcards = listOf(
            "O que a AST joga fora?" to "Correntes de não-terminais, parênteses/pontuação e ε",
            "Nós da derivação × AST em a + b * c?" to "13 × 5",
            "Onde mora a precedência na AST?" to "Na forma: quem está mais fundo é calculado primeiro",
            "Linha-chave da associatividade à esquerda no descendente?" to "e = new BinOp(op, e, termo()); dentro do while",
            "Ação Bison para E → E + T?" to "\$\$ = new BinOp('+', \$1, \$3);",
            "Ação Bison para F → ( E )?" to "\$\$ = \$2; (o parêntese some)",
            "Classes de comando típicas?" to "Atrib, Se, Enquanto, Bloco (estendem Cmd)",
            "Vantagem da AST sobre gerar código direto no parser?" to "Várias passadas (tipos, depois código), fases separadas e testáveis, otimização na árvore",
            "Nós da AST com n operadores binários?" to "2n + 1",
        ),
    ),
    SkillCard(
        skill = Skill.REG_ALLOC,
        oneLiner = "Alocar registradores é manter os temporários em registradores em vez da memória: calcula-se a vivacidade de trás para frente (in = use ∪ (out − def)), liga-se no grafo de interferência quem está vivo ao mesmo tempo e colore-se o grafo com k cores (k registradores); se não der, algum temporário é derramado (spill) para a memória.",
        hook = "VIGOR: VIvacidade, Grafo, cOlorir, Registradores. Def mata, use ressuscita. \$t = tchau, \$s = sagrado.",
        feynmanPrompt = "Explique para um amigo de 12 anos como uma cozinha com poucas bancadas decide qual preparação fica em qual bancada, e por que em r = a * b + c * d bastam 2 bancadas e as idas à despensa caem de 11 para 5.",
        keyTerms = listOf("registrador", "viv", "interferen", "color", "spill", "memoria"),
        commonMistakes = listOf(
            "Fazer a análise de vivacidade de cima para baixo",
            "Achar que 'vivo' é 'já foi definido' (vivo é 'ainda vai ser lido')",
            "Ligar no grafo temporários que nunca estão vivos ao mesmo tempo",
            "Confundir o número cromático com o número de temporários",
            "Inverter a convenção: \$s é salvo pelo CHAMADO, \$t pelo CHAMADOR",
        ),
        flashcards = listOf(
            "Por que alocar registradores?" to "lw/sw são lentos; registradores são rápidos e poucos",
            "Equação da vivacidade?" to "in = use ∪ (out − def), de trás para frente",
            "Quando x e y interferem?" to "Quando um é definido enquanto o outro está vivo (ambos vivos juntos)",
            "k registradores equivalem a..." to "colorir o grafo de interferência com k cores",
            "Passos de Chaitin?" to "Simplificar nós de grau < k (empilhando), depois desempilhar e colorir",
            "O que é spill?" to "Derramar um temporário para a memória: sw após definir, lw antes de cada uso",
            "lw + sw em a * b + c * d: ingênuo × alocado?" to "11 × 5",
            "\$s0–\$s7 são salvos por quem?" to "Pelo chamado (callee-saved)",
            "\$t0–\$t9 são salvos por quem?" to "Pelo chamador (caller-saved)",
        ),
    ),
)

// Agregadores no FIM do arquivo (propriedades de topo são inicializadas em ordem textual).
val lessonsPart6: List<Lesson> by lazy { p6MinLessons + p6AstLessons + p6RegLessons }
val cardsPart6: List<SkillCard> by lazy { p6Cards }
