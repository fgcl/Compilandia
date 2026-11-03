package com.compilandia.content

import com.compilandia.learning.Skill

// Parte 4 do conteúdo: NFA_TO_DFA e LEX_FLEX (Floresta Léxica) e ERROR_RECOVERY (Templo Top-Down).
// As aulas estão em LessonsPart4a.kt (AFN → AFD), LessonsPart4b.kt (Lex/Flex) e LessonsPart4c.kt (recuperação de erros).

private val p4Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.NFA_TO_DFA,
        oneLiner = "A construção de subconjuntos transforma um AFN em AFD: cada estado do AFD é um CONJUNTO de estados do AFN, começando em ε-fecho({s₀}), com Dtran[T, a] = ε-fecho(move(T, a)); é final todo conjunto que contém um final do AFN.",
        hook = "Anda e fecha a porta: move com a letra, depois ε-fecho. Cesto com uma fruta premiada é final; cesto vazio é o estado morto.",
        feynmanPrompt = "Explique para um amigo de 12 anos como dá para acompanhar uma turma que se divide pela rodoviária só olhando a lista do grupo do WhatsApp, e por que essa lista vira um autômato que nunca fica em dúvida.",
        keyTerms = listOf("conjunto", "fecho", "move", "final", "morto", "inicial"),
        commonMistakes = listOf(
            "Esquecer o ε-fecho depois do move",
            "Usar {s₀} como estado inicial em vez de ε-fecho({s₀})",
            "Marcar como final só o conjunto em que TODOS os estados são finais (basta um)",
            "Achar que a construção de subconjuntos já entrega o AFD mínimo",
            "Achar que AFN reconhece mais linguagens que AFD",
        ),
        flashcards = listOf(
            "O que é um estado do AFD na construção de subconjuntos?" to "Um conjunto de estados do AFN",
            "Fórmula de Dtran[T, a]?" to "ε-fecho(move(T, a))",
            "Estado inicial do AFD?" to "ε-fecho({s₀})",
            "Quando um estado do AFD é final?" to "Quando contém pelo menos um estado final do AFN",
            "move(T, a) vazio leva a quê?" to "Ao estado morto ∅",
            "Máximo de estados do AFD para um AFN de n estados?" to "2ⁿ",
            "ε-fecho({0}) no AFN de Thompson de (a|b)*abb?" to "{0, 1, 2, 4, 7}",
            "Linguagem clássica da explosão exponencial?" to "O n-ésimo símbolo a partir do fim é a (2ⁿ estados)",
            "Por que o léxico usa AFD e não AFN?" to "Um passo por caractere, sem carregar conjuntos",
        ),
    ),
    SkillCard(
        skill = Skill.LEX_FLEX,
        oneLiner = "O Lex/Flex lê um arquivo .l (definições %% regras 'padrão { ação }' %% código C) e gera lex.yy.c com a função yylex(), que devolve um token por chamada usando o maior casamento e, no empate, a regra listada primeiro.",
        hook = "D-R-C, guloso mas educado: come o maior pedaço; no empate, respeita a fila. Palavra-chave é VIP: vem antes do ID.",
        feynmanPrompt = "Explique para um amigo de 12 anos como uma folha de regras dos Correios vira um robô de triagem, e por que a ordem das linhas da folha às vezes importa e às vezes não.",
        keyTerms = listOf("regra", "padr", "maior", "primeir", "yytext", "yylex", "return"),
        commonMistakes = listOf(
            "Pôr a regra de identificador antes das palavras-chave",
            "Pôr a regra-coringa '.' antes das outras regras",
            "Achar que a ordem das regras vence o tamanho do lexema",
            "Confundir yytext (o texto do lexema) com yylval (o valor passado ao Bison)",
            "Esquecer yywrap (ou %option noyywrap / -ll) e o gcc não linkar",
        ),
        flashcards = listOf(
            "Quais são as 3 seções de um arquivo .l?" to "Definições %% regras %% código C",
            "O que o comando lex gera?" to "O arquivo lex.yy.c com a função yylex()",
            "Regra 1 de desempate do Lex?" to "O maior casamento (lexema mais longo) vence",
            "Regra 2 de desempate do Lex?" to "Mesmo tamanho: vence a regra listada primeiro",
            "'iffy' com \"if\" antes de [a-z]+ vira o quê?" to "Um único ID (maior casamento)",
            "O que guardam yytext e yyleng?" to "O texto do lexema e o seu tamanho",
            "Ação sem return, como { }, faz o quê?" to "Descarta o lexema e yylex() continua",
            "Como o valor de um número chega ao Bison?" to "Por yylval (ex.: yylval = atoi(yytext);)",
            "Como compilar o scanner do TP?" to "lex scanner.l && gcc lex.yy.c",
        ),
    ),
    SkillCard(
        skill = Skill.ERROR_RECOVERY,
        oneLiner = "O analisador detecta o erro no primeiro token que torna a entrada impossível (prefixo viável), relata linha e 'esperado × encontrado' e se recupera para achar mais erros; no LL(1), o modo pânico descarta tokens até um de FIRST(A) (continua) ou de FOLLOW(A) ∪ {\$} (desempilha A).",
        hook = "FIRST Fica, FOLLOW Foge. Pânico Pula, Frase Faz remendo, Produção Prevê, Global é Gênio.",
        feynmanPrompt = "Explique para um amigo de 12 anos como um corretor de redação que acha um erro continua lendo a partir do próximo ponto final, e por que isso é melhor do que parar na primeira falha.",
        keyTerms = listOf("pânico", "sincroniza", "follow", "descart", "prefixo", "erro"),
        commonMistakes = listOf(
            "Achar que recuperar um erro é corrigir o programa",
            "Usar FIRST(A) como conjunto para desempilhar A (é FOLLOW(A))",
            "Esquecer o \$ entre os tokens de sincronização",
            "Achar que o erro é detectado depois do token problemático (é exatamente nele)",
            "Confiar em todas as mensagens seguintes sem considerar o erro em cascata",
        ),
        flashcards = listOf(
            "Quatro tipos de erro?" to "Léxico, sintático, semântico e lógico",
            "Quatro estratégias de recuperação?" to "Modo pânico, nível de frase, produções de erro e correção global",
            "O que é a propriedade do prefixo viável?" to "LL e LR acusam o erro no primeiro token que não pode continuar um prefixo válido",
            "Modo pânico: token em FIRST(A)?" to "Para de descartar e continua com A",
            "Modo pânico: token em FOLLOW(A) ∪ {\$}?" to "Desempilha A (sincronizou)",
            "Terminal no topo diferente da entrada?" to "Desempilha o terminal (finge que estava lá)",
            "Tokens de sincronização típicos em C?" to "';' e '}'",
            "Token especial do Bison para recuperação?" to "error (ex.: comando : error ';')",
            "Para que serve yyerrok?" to "Sair do modo de recuperação e voltar a relatar erros na hora",
        ),
    ),
)

// Agregados no FIM do arquivo (propriedades de topo inicializam na ordem do arquivo).
val lessonsPart4: List<Lesson> by lazy { p4NfaLessons + p4FlexLessons + p4ErrorLessons }
val cardsPart4: List<SkillCard> by lazy { p4Cards }
