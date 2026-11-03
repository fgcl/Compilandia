package com.compilandia.learning

/**
 * Regiões do mapa = "Palácio da Memória" (método de loci).
 * A ORDEM do caminho é a ORDEM das fases do compilador: andar pelo mapa
 * é recitar o pipeline.
 */
enum class Region(val title: String, val emoji: String, val tagline: String) {
    PORTO("Porto da Partida", "⚓", "Onde o código-fonte embarca"),
    FLORESTA_LEXICA("Floresta Léxica", "🌲", "Cortar texto em tokens"),
    VALE_GRAMATICAS("Vale das Gramáticas", "🌄", "Regras que geram linguagens"),
    PONTE_TRANSFORMACAO("Ponte da Transformação", "🌉", "Consertar gramáticas"),
    TEMPLO_TOPDOWN("Templo Top-Down", "🛕", "Prever de cima para baixo: LL(1)"),
    FORTALEZA_BOTTOMUP("Fortaleza Bottom-Up", "🏰", "Montar de baixo para cima: LR"),
    OFICINA_BISON("Oficina Yacc/Bison", "🦬", "Geradores de analisadores"),
    CIDADE_SEMANTICA("Cidade Semântica", "🏙️", "Significado, tipos e escopos"),
    FORJA_CODIGO("Forja do Código", "⚒️", "Código intermediário e otimização"),
}

enum class Skill(val title: String, val region: Region, val prereqs: List<Skill> = emptyList()) {
    PHASES("Fases de um compilador", Region.PORTO),
    TOKENS("Tokens, lexemas e padrões", Region.FLORESTA_LEXICA, listOf(PHASES)),
    REGEX_DFA("Expressões regulares e autômatos", Region.FLORESTA_LEXICA, listOf(TOKENS)),
    NFA_TO_DFA("AFN → AFD (construção de subconjuntos)", Region.FLORESTA_LEXICA, listOf(REGEX_DFA)),
    DFA_MINIMIZATION("Minimização de AFD (tabela de pares)", Region.FLORESTA_LEXICA, listOf(REGEX_DFA)),
    LEX_FLEX("Lex/Flex: o arquivo .l", Region.FLORESTA_LEXICA, listOf(TOKENS, REGEX_DFA)),
    CFG_DERIVATION("Gramáticas e derivações", Region.VALE_GRAMATICAS, listOf(PHASES)),
    AMBIGUITY("Árvores e ambiguidade", Region.VALE_GRAMATICAS, listOf(CFG_DERIVATION)),
    LEFT_RECURSION("Recursão à esquerda", Region.PONTE_TRANSFORMACAO, listOf(CFG_DERIVATION)),
    LEFT_FACTORING("Fatoração à esquerda", Region.PONTE_TRANSFORMACAO, listOf(CFG_DERIVATION)),
    FIRST("Conjunto FIRST", Region.TEMPLO_TOPDOWN, listOf(CFG_DERIVATION)),
    FOLLOW("Conjunto FOLLOW", Region.TEMPLO_TOPDOWN, listOf(FIRST)),
    LL1_TABLE("Tabela LL(1)", Region.TEMPLO_TOPDOWN, listOf(FIRST, FOLLOW)),
    LL1_PARSING("Analisador preditivo (pilha)", Region.TEMPLO_TOPDOWN, listOf(LL1_TABLE)),
    RECURSIVE_DESCENT("Descida recursiva", Region.TEMPLO_TOPDOWN, listOf(FIRST)),
    ERROR_RECOVERY("Erros sintáticos e recuperação", Region.TEMPLO_TOPDOWN, listOf(LL1_PARSING)),
    SHIFT_REDUCE("Empilha-reduz (shift-reduce)", Region.FORTALEZA_BOTTOMUP, listOf(CFG_DERIVATION)),
    LR0_ITEMS("Itens LR(0), closure e goto", Region.FORTALEZA_BOTTOMUP, listOf(SHIFT_REDUCE)),
    SLR_TABLE("Tabela SLR(1) e conflitos", Region.FORTALEZA_BOTTOMUP, listOf(LR0_ITEMS, FOLLOW)),
    LALR("LR(1) e LALR(1)", Region.FORTALEZA_BOTTOMUP, listOf(SLR_TABLE)),
    YACC_BISON("Yacc/Bison: especificação e precedência", Region.OFICINA_BISON, listOf(SHIFT_REDUCE, AMBIGUITY)),
    ATTRIBUTES_TYPES("Atributos e verificação de tipos", Region.CIDADE_SEMANTICA, listOf(CFG_DERIVATION)),
    SCOPE_SYMBOLS("Escopo e tabela de símbolos", Region.CIDADE_SEMANTICA, listOf(PHASES)),
    AST_BUILD("Árvore sintática abstrata (AST) com classes", Region.CIDADE_SEMANTICA, listOf(CFG_DERIVATION)),
    TAC("Código de três endereços", Region.FORJA_CODIGO, listOf(ATTRIBUTES_TYPES)),
    OPTIMIZATION("Otimização (dobra de constantes)", Region.FORJA_CODIGO, listOf(TAC)),
    RUNTIME("Ambientes de execução (registros de ativação)", Region.FORJA_CODIGO, listOf(SCOPE_SYMBOLS)),
    CODEGEN_MIPS("Geração de código MIPS", Region.FORJA_CODIGO, listOf(TAC)),
    REG_ALLOC("Alocação de registradores", Region.FORJA_CODIGO, listOf(CODEGEN_MIPS)),
}

/** Minijogos. Cada habilidade tem pelo menos dois jogos com mecânicas diferentes. */
enum class GameId(val title: String, val emoji: String, val mechanic: String, val skills: List<Skill>) {
    PIPELINE("Esteira das Fases", "🏭", "ordenar e encaixar", listOf(Skill.PHASES)),
    PHASE_DETECTIVE("Detetive de Erros", "🕵️", "classificar quem pega cada erro", listOf(Skill.PHASES, Skill.TOKENS, Skill.ATTRIBUTES_TYPES, Skill.SCOPE_SYMBOLS, Skill.ERROR_RECOVERY)),
    TOKEN_SLICER("Fatiador de Tokens", "🔪", "cortar e classificar", listOf(Skill.TOKENS, Skill.LEX_FLEX)),
    DFA_TRAIN("Trem do Autômato", "🚂", "pilotar o trem estado a estado", listOf(Skill.REGEX_DFA, Skill.NFA_TO_DFA, Skill.DFA_MINIMIZATION)),
    REGEX_SORTER("Porteiro Regex", "🚪", "deixar entrar só quem casa", listOf(Skill.REGEX_DFA, Skill.TOKENS)),
    DERIVATION_RACE("Derivação Turbo", "🏎️", "escolher produções e crescer a árvore", listOf(Skill.CFG_DERIVATION, Skill.AMBIGUITY, Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT)),
    TWO_TREES("Duas Árvores", "🌳", "achar duas árvores para a mesma frase", listOf(Skill.AMBIGUITY, Skill.CFG_DERIVATION)),
    GRAMMAR_SURGERY("Cirurgia de Gramática", "🩺", "montar a gramática consertada com peças", listOf(Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)),
    FIRST_FOLLOW_ALCHEMY("Alquimia FIRST/FOLLOW", "⚗️", "jogar ingredientes nos caldeirões", listOf(Skill.FIRST, Skill.FOLLOW)),
    LL1_SUDOKU("Sudoku LL(1)", "🧩", "encaixar produções na tabela", listOf(Skill.LL1_TABLE, Skill.FIRST, Skill.FOLLOW, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)),
    STACK_TOWER("Torre da Pilha", "🗼", "operar a pilha do analisador preditivo", listOf(Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT, Skill.LL1_TABLE)),
    SHIFT_REDUCE_RUSH("Esteira Shift-Reduce", "📦", "empilhar ou reduzir contra o relógio", listOf(Skill.SHIFT_REDUCE, Skill.SLR_TABLE, Skill.LR0_ITEMS)),
    ITEM_BUILDER("Construtor de Estados", "🏗️", "fechar (closure) e transitar (goto)", listOf(Skill.LR0_ITEMS, Skill.LALR)),
    CONFLICT_HUNTER("Caça-Conflitos", "🎯", "achar e explicar conflitos", listOf(Skill.SLR_TABLE, Skill.LALR, Skill.YACC_BISON)),
    BISON_WORKSHOP("Oficina Bison", "🦬", "programar o Bison até os testes passarem", listOf(Skill.YACC_BISON, Skill.SHIFT_REDUCE)),
    ATTRIBUTE_TREE("Árvore Anotada", "🏷️", "calcular atributos de baixo para cima", listOf(Skill.ATTRIBUTES_TYPES, Skill.TAC, Skill.OPTIMIZATION)),
    SCOPE_DETECTIVE("Esconde-Esconde do Escopo", "🔦", "ligar cada uso à sua declaração", listOf(Skill.SCOPE_SYMBOLS, Skill.RUNTIME)),
    TAC_ASSEMBLY("Linha de Montagem TAC", "🔧", "montar código de três endereços", listOf(Skill.TAC, Skill.OPTIMIZATION, Skill.CODEGEN_MIPS, Skill.AST_BUILD)),
    SUBSET_FACTORY("Fábrica de Subconjuntos", "🧺", "juntar estados do AFN em estados do AFD", listOf(Skill.NFA_TO_DFA)),
    FLEX_WORKSHOP("Oficina Flex", "🦎", "ordenar e escolher regras do .l", listOf(Skill.LEX_FLEX)),
    PANIC_ROOM("Sala do Pânico", "🚨", "achar o erro e sincronizar", listOf(Skill.ERROR_RECOVERY)),
    CALL_STACK("Pilha de Chamadas", "📚", "empilhar e desempilhar registros de ativação", listOf(Skill.RUNTIME)),
    MIPS_FORGE("Montadora MIPS", "🔩", "traduzir TAC para assembly e rodar", listOf(Skill.CODEGEN_MIPS, Skill.REG_ALLOC)),
    DESCENT_CODER("Programador Descendente", "🧑‍💻", "montar o código do parser", listOf(Skill.RECURSIVE_DESCENT, Skill.LL1_TABLE)),
    MIN_TABLE("Tabela dos Pares", "🧮", "marcar pares distinguíveis e juntar estados", listOf(Skill.DFA_MINIMIZATION)),
    AST_FACTORY("Fábrica de AST", "🪵", "montar a AST com new BinOp(...)", listOf(Skill.AST_BUILD, Skill.ATTRIBUTES_TYPES)),
    REG_COLORING("Coloração de Registradores", "🎨", "vivacidade + colorir o grafo de interferência", listOf(Skill.REG_ALLOC)),
    BRANCH_MAZE("Labirinto de Desvios", "🔀", "completar os desvios do if/while e rodar no MARS", listOf(Skill.CODEGEN_MIPS)),
    AUTOMATON_FACTORY("Fábrica de Autômatos", "🏭", "montar o autômato e passar nos robôs de teste", listOf(Skill.REGEX_DFA, Skill.DFA_MINIMIZATION)),
    /** Revisão relâmpago de flashcards (não entra no sorteio de minijogos). */
    FLASHCARDS("Revisão Relâmpago", "⚡", "lembrar sem olhar", emptyList()),
    ;

    companion object {
        fun forSkill(s: Skill): List<GameId> = entries.filter { s in it.skills }
    }
}

/** Estilos de explicação. O botão "Não entendi" troca entre eles. */
enum class Style(val title: String, val emoji: String, val description: String) {
    STORY("História / Analogia", "📖", "Uma metáfora do dia a dia"),
    VISUAL("Passo a passo visual", "🎬", "Ver o algoritmo acontecendo"),
    WORKED("Exemplo resolvido", "✍️", "Um exercício resolvido com calma"),
    SOCRATIC("Perguntas guiadas", "🤔", "Você descobre respondendo perguntas"),
    FORMAL("Definição de prova", "🎓", "Do jeito que cai na prova"),
    MNEMONIC("Truque de memória", "🧠", "Ganchos para nunca esquecer"),
}

/** O que dá para comprar para a oficina, na ordem em que o jogo oferece. O id é o da camada em RoomLayers. */
enum class RoomItem(val id: String, val title: String, val price: Int) {
    ABAJUR("abajur", "Abajur da bancada", 30),
    PLANTA("planta", "Planta", 40),
    CADEIRA("cadeira", "Cadeira", 60),
    LIXEIRA("lixeira", "Lixeira de bugs", 40),
    QUADRO("quadro", "Quadro do autômato", 80),
    LUMINARIA("luminaria", "Luminária", 90),
    URSINHO("ursinho", "Ursinho do Byte", 70),
    CAIXA_SOM("caixasom", "Caixinha de som", 90),
    ESTANTE("estante", "Estante", 120),
    RADIO("radio", "Rádio", 100),
    CAFE("cafe", "Cantinho do café", 130),
    SOFA("sofa", "Sofá", 200),
}
