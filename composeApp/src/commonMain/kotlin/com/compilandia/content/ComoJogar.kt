package com.compilandia.content

import com.compilandia.content.Speaker.BYTE
import com.compilandia.learning.GameId

/** Instrução falada de cada jogo. Curta de propósito: quem joga aprende mexendo. */
val comoJogar: Map<GameId, Fala> = mapOf(
    GameId.PIPELINE to "Monte a esteira. Toque nas fases na ordem certa.",
    GameId.PHASE_DETECTIVE to "Cada cartão traz um erro. Diga qual fase pega esse erro, e rápido.",
    GameId.TOKEN_SLICER to "Toque entre as letras para cortar a linha. Depois diga a classe de cada token.",
    GameId.DFA_TRAIN to "Você pilota o trem. A cada letra, toque no próximo estado.",
    GameId.AUTOMATON_FACTORY to "Monte o autômato com o dedo. Os robôs testam, e a frase que furar aparece aqui.",
    GameId.REGEX_SORTER to "Você é o porteiro. Só entra quem casa com a expressão.",
    GameId.DERIVATION_RACE to "Chegue na frase alvo. Troque sempre o não-terminal mais à esquerda.",
    GameId.TWO_TREES to "Prove que a gramática é ambígua. Monte duas derivações para a mesma frase.",
    GameId.GRAMMAR_SURGERY to "Conserte a gramática. Toque numa peça e depois no espaço vazio.",
    GameId.FIRST_FOLLOW_ALCHEMY to "Escolha um caldeirão e jogue dentro os símbolos certos.",
    GameId.LL1_SUDOKU to "Preencha a tabela. Toque numa célula e escolha a regra dela.",
    GameId.STACK_TOWER to "Olhe o topo da pilha e o próximo token. Depois escolha a ação.",
    GameId.SHIFT_REDUCE_RUSH to "Empilhe os tokens. Quando um lado direito aparecer no topo, reduza.",
    GameId.ITEM_BUILDER to "Monte o estado. Marque os itens que o fechamento acrescenta.",
    GameId.CONFLICT_HUNTER to "Ache as células com duas ações. Depois decida como o Bison resolve.",
    GameId.BISON_WORKSHOP to "Edite a especificação até todos os testes passarem.",
    GameId.ATTRIBUTE_TREE to "Calcule de baixo para cima. Toque num nó com os filhos prontos.",
    GameId.SCOPE_DETECTIVE to "Para cada uso da variável, toque na declaração que ele enxerga.",
    GameId.TAC_ASSEMBLY to "Monte o código na ordem. Cada temporário precisa existir antes do uso.",
    GameId.SUBSET_FACTORY to "Transforme o AFN em AFD. Para cada conjunto, marque para onde ele vai.",
    GameId.FLEX_WORKSHOP to "Arrume as regras até o scanner gerar os tokens esperados.",
    GameId.PANIC_ROOM to "Ache onde o parser percebe o erro e onde ele consegue voltar.",
    GameId.CALL_STACK to "Rode o programa. Empilhe na chamada e desempilhe no retorno.",
    GameId.MIPS_FORGE to "Traduza cada instrução para MIPS: carrega, opera e guarda.",
    GameId.DESCENT_CODER to "Monte o método do parser, uma linha de cada vez.",
    GameId.MIN_TABLE to "Marque os pares de estados diferentes. Os que sobrarem viram um só.",
    GameId.AST_FACTORY to "Monte a árvore de fora para dentro. Escolha o nó de cada espaço.",
    GameId.REG_COLORING to "Veja quem está vivo e pinte o grafo com poucos registradores.",
    GameId.BRANCH_MAZE to "Complete os desvios do if e do while, e rode o programa.",
).mapValues { Fala(BYTE, it.value) }
