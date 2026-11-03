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
import com.compilandia.content.CalloutKind.REAL_WORLD
import com.compilandia.content.CalloutKind.WATCH_OUT
import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.LEXI
import com.compilandia.content.Speaker.NARRADOR
import com.compilandia.content.Speaker.PILHA
import com.compilandia.content.Speaker.PROF
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// =====================================================================
// YACC_BISON — Yacc/Bison: especificação e precedência
// =====================================================================

internal val bisonLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.YACC_BISON, Style.STORY, "A fábrica que fabrica analisadores",
        listOf(
            Say(NARRADOR, "Oficina Yacc/Bison. Um bisão de avental martela tabelas LALR numa bigorna."),
            Say(BYTE, "Eu passei a semana montando tabela SLR na mão. Tem jeito mais fácil?"),
            Say(PROF, "Tem: você escreve a **receita** (a gramática) e uma fábrica constrói a **máquina** (o analisador em C). Essa fábrica é o Bison — um compilador de compiladores."),
            Text("Pense numa padaria industrial. Você entrega uma ficha de pedido em **três partes**, separadas por %%: **ingredientes** (declarações), **modo de preparo** (regras) e **observações** (código C extra)."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Ficha do pedido → arquivo **calc.y**",
                    "Lista de ingredientes (%token NUM, %left '+') → **declarações**",
                    "Modo de preparo (expr : expr '+' expr) → **regras** da gramática",
                    "'Quando terminar esta etapa, anote o total' → **ação semântica** { \$\$ = \$1 + \$3; }",
                    "Fornecedor que entrega um ingrediente por vez → **yylex** (a Lexi, ou o Flex)",
                    "O robô confeiteiro que segue a tabela → **yyparse**",
                    "Alarme de fumaça → **yyerror**",
                ),
            ),
            Say(LEXI, "Eu sou o fornecedor! Corto '12+3' em NUM, '+', NUM e entrego um por vez quando o yyparse pede."),
            Code(P3_BISON_CALC, "A ficha de pedido mais famosa: a calculadora (versão que o jogo entende)"),
            Predict(
                "O robô vai montar 2+3*4. Sem nenhuma linha %left, o que você acha que acontece?",
                listOf("Ele recusa a receita", "Ele avisa conflitos e, por padrão, prefere empilhar (shift)", "Ele calcula sempre da esquerda para a direita"),
                1,
                "A gramática é ambígua, então há conflitos empilhar/reduzir. O Bison avisa e escolhe shift em cada um — e às vezes o resultado sai errado.",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "2+3*4", caption = "Com %left '+' '-' e %left '*' '/' na ficha, 2+3*4 = 14. Veja as reduções e os \$\$."),
            Callout(NO_DUMB_QUESTIONS, "'Yacc' quer dizer *Yet Another Compiler-Compiler* (Bell Labs, anos 1970). 'Bison' é a versão GNU — trocadilho: yak (iaque) → bisão."),
            Callout(REAL_WORLD, "O PostgreSQL (gram.y), o Bash (parse.y) e o PHP usam Bison até hoje. O GCC também usava, mas trocou por descida recursiva escrita à mão (C++ no GCC 3.4, C no 4.1) para ter mensagens de erro melhores."),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.VISUAL, "Ligando e desligando a precedência",
        listOf(
            Say(PROF, "Experimento de laboratório: a mesma gramática ambígua, com e sem declarações de precedência."),
            Visual(VisualKind.SLR_TABLE, grammar = P3_AMBIG, caption = "Por baixo do capô: a gramática ambígua tem conflitos empilhar/reduzir em + e *."),
            Visual(VisualKind.BISON, grammar = P3_BISON_NOPREC, input = "2*3+4", caption = "SEM precedência: 4 conflitos, o Bison escolhe shift em todos... e 2*3+4 dá 14 (calculou 2*(3+4))!"),
            Say(BYTE, "Catorze?! Minha calculadora de bolso discorda."),
            Say(PILHA, "Eu tinha 2*3... vi o '+'... o padrão mandou empilhar. Eu... obedeci."),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "2*3+4", caption = "COM %left: na célula (estado de 'expr * expr', token '+'), a regra de '*' é mais forte que '+' → reduce. Resultado 10."),
            Steps(
                "Como o Bison decide cada conflito empilhar/reduzir",
                listOf(
                    "Precedência da **regra** = a do último terminal dela (ou a do %prec)",
                    "Precedência do **token** de lookahead = a linha onde ele foi declarado",
                    "Token mais forte → **shift**; regra mais forte → **reduce**",
                    "Empate: %left → reduce; %right → shift; %nonassoc → erro de sintaxe",
                ),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_RIGHT_MINUS, input = "8-3-2", caption = "%right '-' por engano: 8-3-2 vira 8-(3-2) = 7. Subtração tem que ser %left."),
            Predict(
                "E com %right '^' para potência, quanto dá 2^3^2?",
                listOf("64, ou seja (2^3)^2", "512, ou seja 2^(3^2)", "Erro de sintaxe"),
                1,
                "%right: no empate, shift. A pilha guarda 2^3 e empilha o segundo '^': 3^2 = 9 primeiro, depois 2^9 = 512. Como na matemática.",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_POW, input = "2^3^2", caption = "Confira: 512. A linha %right '^' vem por último, então liga mais forte que * e +."),
            Visual(VisualKind.BISON, grammar = P3_BISON_NONASSOC, input = "1+1<3", caption = "%nonassoc '<': 1+1<3 funciona (= 1), mas tente mentalmente 1<2<3 — vira erro de sintaxe, de propósito."),
            Callout(BRAIN_POWER, "A precedência não muda a gramática: ela só apaga uma das duas ações das células em conflito. A tabela LALR continua lá embaixo."),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.WORKED, "Calculadora completa: Bison + Flex + gcc",
        listOf(
            Say(PROF, "Vamos montar uma calculadora de verdade, do jeito que você faria no laboratório."),
            Code(P3_REAL_CALC_Y, "calc.y — declarações %% regras %% código C (com um yylex escrito à mão)"),
            Steps(
                "Lendo o arquivo por partes",
                listOf(
                    "%{ ... %}: código C copiado literalmente para o topo do analisador gerado",
                    "%token NUM: um token com nome; o Bison numera a partir de 258 (caracteres como '+' usam o próprio código ASCII)",
                    "%left / %right: precedência — linhas mais embaixo ligam mais forte",
                    "%%: começam as regras. \$\$ é o valor do lado esquerdo; \$1, \$2, \$3 os do lado direito",
                    "%%: começa o código C do usuário: yylex, yyerror e main",
                    "yylex devolve o código do token e coloca o valor em yylval; devolver 0 = fim da entrada",
                ),
            ),
            Code(P3_REAL_CALC_L, "calc.l — o mesmo léxico gerado pelo Flex (aí você apaga o yylex do calc.y)"),
            Code("bison -d calc.y          # gera calc.tab.c e calc.tab.h\nflex calc.l              # gera lex.yy.c\ngcc calc.tab.c lex.yy.c -o calc\necho \"2+3*4\" | ./calc     # imprime: = 14", "Compilando e rodando"),
            Say(LEXI, "O calc.tab.h é o contrato entre mim e o bisão: ali está escrito que NUM vale 258."),
            Steps(
                "Rastreando 2+3*4 (só as decisões importantes)",
                listOf(
                    "Pilha 'expr + expr', próximo token '*': regra de '+' (nível 1) < token '*' (nível 2) → **shift**",
                    "Pilha 'expr + expr * expr', fim da linha: reduz expr * expr → \$\$ = 3 * 4 = 12",
                    "Pilha 'expr + expr': reduz → \$\$ = 2 + 12 = 14",
                ),
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "-5+2", caption = "No jogo, a mesma calculadora (sem o prólogo C). Veja o menos unário com %prec UMINUS."),
            Say(BYTE, "Minha vez!"),
            Reveal("Com %left '-', quanto vale 8-3-2? E se fosse %right '-'?", "%left: empate → reduce → (8-3)-2 = 3. %right: empate → shift → 8-(3-2) = 7."),
            Reveal("Na regra '-' expr %prec UMINUS, por que o %prec?", "Sem ele, a regra herdaria a precedência do '-' binário (último terminal da regra). Com %prec UMINUS, ela ganha a precedência da linha %right UMINUS, a mais forte."),
            Reveal("No mini-Bison do jogo, o que NÃO entra na especificação?", "O prólogo %{ %}, a terceira seção com código C, %union/%type. O jogo já traz um léxico pronto (números viram NUM, palavras viram ID) e entende ações do tipo \$\$ = expressão;."),
            Callout(EXAM_TIP, "Se a prova pede 'escreva a especificação Yacc', não esqueça: os dois %%, o %token de cada token com nome, o ';' no fim de cada regra e as ações entre chaves."),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.SOCRATIC, "Por que deixar a gramática ambígua?",
        listOf(
            Say(PROF, "Pergunta provocadora: se ambiguidade é ruim, por que quase toda calculadora em Bison usa expr : expr '+' expr?"),
            Reveal("Qual a alternativa sem ambiguidade?", "Camadas: expr → expr + term | term; term → term * fator | fator. Funciona, mas cresce uma camada por nível de precedência."),
            Visual(VisualKind.BISON, grammar = P3_BISON_LAYERS, input = "2+3*4", caption = "Versão em camadas: zero conflitos, zero %left, e ainda dá 14."),
            Reveal("E qual a vantagem da versão ambígua + %left?", "Gramática curta, legível e com menos reduções unitárias (term → fator...). A precedência fica numa tabelinha de declarações."),
            Reveal("Como o Bison sabe a precedência de uma REGRA?", "Pelo último terminal do lado direito (expr '*' expr → a de '*'), ou pelo %prec, se você escrever."),
            Reveal("E a do lookahead?", "Pela linha de %left/%right/%nonassoc em que o token aparece. Linhas mais embaixo = mais forte."),
            Predict(
                "Um conflito empilhar/reduzir em que o token NÃO tem precedência declarada. O que o Bison faz?",
                listOf("Para a compilação com erro", "Escolhe shift e emite um aviso de conflito", "Escolhe reduce"),
                1,
                "Padrão do Yacc/Bison: shift, com aviso ('conflicts: N shift/reduce'). Para reduzir/reduzir, escolhe a regra que aparece primeiro no arquivo.",
            ),
            Reveal("Esse padrão 'shift' é bom para o if-then-else pendurado?", "É! Shift no 'else' faz o else casar com o if mais próximo — o que C e Java fazem. Por isso muita gente aceita esse conflito (e o declara com %expect 1)."),
            Visual(VisualKind.PARSE_TREE, grammar = P3_IFELSE, input = "if c then if c then x else x", caption = "Duas árvores possíveis: de quem é o else? O shift escolhe o if de dentro."),
            Reveal("Por que conflitos reduzir/reduzir são mais perigosos?", "Porque 'pegar a primeira regra' quase nunca é o que você queria, e uma das regras pode ficar inalcançável. Geralmente indicam erro no desenho da gramática."),
            Callout(BRAIN_POWER, "Você pode ligar os relatórios: bison -v calc.y gera calc.output com todos os estados e conflitos. É a tabela que você aprendeu a montar à mão!"),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.FORMAL, "Estrutura da especificação e resolução de conflitos",
        listOf(
            Code("declarações\n%%\nregras de tradução\n%%\nfunções auxiliares em C", "Formato geral de um arquivo .y"),
            Steps(
                "Declarações",
                listOf(
                    "%{ código C %} — copiado para o início do analisador",
                    "%token NOME — declara terminais com nome",
                    "%left / %right / %nonassoc t1 t2 — mesma precedência na linha; linhas posteriores têm precedência maior",
                    "%start s — símbolo inicial (padrão: lado esquerdo da primeira regra)",
                    "%union / %type <campo> — tipos dos valores semânticos (padrão: int, via YYSTYPE)",
                ),
            ),
            Text("**Regra:** cabeça : corpo1 { ação1 } | corpo2 { ação2 } ; — na ação, \$\$ é o atributo sintetizado da cabeça e \$i o do i-ésimo símbolo do corpo. Ação omitida equivale a { \$\$ = \$1; }."),
            Code(
                """conflito em ACTION[s, a] entre shift e reduce por A → β:
  pa := precedência do token a
  pr := precedência da regra (%prec, ou último terminal de β)
  se pa ou pr indefinida:   shift   (+ aviso de conflito)
  senão se pa > pr:         shift
  senão se pa < pr:         reduce
  senão (empate):
      %left     → reduce
      %right    → shift
      %nonassoc → erro
conflito reduce/reduce:     reduce pela regra que aparece primeiro (+ aviso)""",
                "Regras de desambiguação do Yacc/Bison",
            ),
            Steps(
                "Interface com o resto do programa",
                listOf(
                    "int yyparse(void) — o analisador LALR(1); retorna 0 em sucesso e 1 em erro de sintaxe",
                    "int yylex(void) — chamado pelo yyparse; retorna o código do token (0 = fim) e põe o valor em yylval",
                    "void yyerror(const char *s) — chamado em erro de sintaxe",
                    "bison -d gera o cabeçalho .tab.h com os códigos dos tokens, que o Flex inclui",
                ),
            ),
            Predict(
                "Declarações: %left '+' '-'  e, abaixo, %left '*' '/'. Em 'expr - expr' com lookahead '-', o Bison...",
                listOf("Empilha (shift)", "Reduz (reduce)", "Dá erro"),
                1,
                "Mesma precedência (mesma linha) e %left → reduce: (a - b) - c.",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "(2+3)*4", caption = "A especificação no subconjunto que o mini-Bison do jogo aceita: %token, %left/%right/%nonassoc, %prec e ações \$\$ = ...;"),
            Callout(EXAM_TIP, "Questão clássica: 'quantos conflitos e como o Yacc os resolve?'. Responda com estado, token, as duas ações e a regra de desambiguação usada (precedência, associatividade ou padrão shift)."),
            Callout(WATCH_OUT, "A ordem das linhas importa ao contrário do que parece: a PRIMEIRA linha de %left é a de MENOR precedência."),
        ),
    ),
    Lesson(
        Skill.YACC_BISON, Style.MNEMONIC, "Ganchos da família yy",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Oficina Yacc/Bison, sétima parada do caminho. Na parede, três gavetas separadas por %%."),
            Callout(MNEMONIC, "**D-R-C: 'Declara %% Regra %% Código'.** Três gavetas, dois separadores."),
            Callout(MNEMONIC, "Precedência: **'o último da fila é o VIP'**. A linha de %left mais embaixo é a que liga mais forte."),
            Callout(MNEMONIC, "**'Na dúvida, empilha; no empate de reduções, quem chegou primeiro.'** — o padrão do Bison para E/R e R/R."),
            Callout(MNEMONIC, "%left = **'esquerda reduz'**; %right = **'direita empilha'**; %nonassoc = **'não se mistura'** (a < b < c vira erro)."),
            Code(
                """yylex    → LÊ o próximo token          (a Lexi)
yyparse  → ANALISA (LALR)              (o bisão)
yyerror  → RECLAMA                     (o alarme)
yylval   → VALOR do token atual        (o 12 de NUM)
yytext   → TEXTO casado pelo Flex      ("12")""",
                "A família yy: o sobrenome de todo mundo",
            ),
            Callout(MNEMONIC, "**\$\$ é o pai, \$1 \$2 \$3 são os filhos** da esquerda para a direita. Os valores SOBEM: o pai é calculado a partir dos filhos."),
            Predict(
                "Na regra expr : '(' expr ')' { \$\$ = ???; }, o que vai no lugar de ???",
                listOf("\$1", "\$2", "\$3"),
                1,
                "'(' é \$1, expr é \$2, ')' é \$3. O valor útil é o do meio.",
            ),
            Visual(VisualKind.BISON, grammar = P3_BISON_CALC, input = "8-3-2", caption = "Bisão com %left '-': 8-3-2 = 3."),
            Reveal("Recite: como o Bison resolve um empate de precedência?", "%left → reduce, %right → shift, %nonassoc → erro."),
            Callout(FIRESIDE, "Bison: 'Você escreve 20 linhas e eu te dou um analisador LALR inteiro.' Descida recursiva: 'E eu sou só código normal — fácil de depurar e com mensagens de erro bonitas. Pergunta pro GCC quem ele escolheu.'"),
        ),
    ),
)

// =====================================================================
// ATTRIBUTES_TYPES — Atributos e verificação de tipos
// =====================================================================

internal val attrLessonsP3: List<Lesson> = listOf(
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.STORY, "Notas que sobem, sobrenomes que descem",
        listOf(
            Say(NARRADOR, "Cidade Semântica. A árvore sintática já está pronta... mas ela ainda não sabe o que significa."),
            Say(BYTE, "Como assim? A árvore de 'a + x * 2' tá certinha!"),
            Say(PROF, "Ela está certa na FORMA. Agora precisamos pendurar informação em cada nó: tipo, valor, código... Esses penduricalhos são os **atributos**."),
            Text("Pense na sua média do semestre: a nota final é calculada a partir das notas das provas, que vêm dos exercícios. A informação **sobe**. Isso é um atributo **sintetizado**."),
            Text("Agora pense no sobrenome: você o recebe dos pais e repassa aos irmãos mais novos. A informação **desce** (ou vem do lado). Isso é um atributo **herdado**."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Nota final calculada das parciais → **sintetizado** (do filho para o pai)",
                    "Sobrenome vindo dos pais → **herdado** (do pai ou irmão para o filho)",
                    "Regulamento de como calcular a média → **regra semântica** (E.tipo = ...)",
                    "Gramática + atributos + regras → **definição dirigida pela sintaxe (SDD)**",
                ),
            ),
            Text("O fiscal da balança da feira é o **verificador de tipos**: não deixa somar banana com bool. Mas int com float pode — é só converter o int, como trocar reais por dólares antes de somar. Isso é **coerção**."),
            Predict(
                "a é int e x é float. Qual o tipo de a + x * 2?",
                listOf("int", "float, com coerção de int para float", "erro de tipo"),
                1,
                "x * 2 é float (2 é convertido). a + float também vira float: o compilador converte a com inttofloat.",
            ),
            Visual(VisualKind.ATTR_TREE, input = "a + x * 2", caption = "Os tipos sobem das folhas até a raiz: int + float → float."),
            Reveal("Em 'int a, b, c;', o tipo int é sintetizado ou herdado para cada variável?", "Herdado: ele desce do nó do tipo para a lista de nomes, como um sobrenome passado aos irmãos."),
            Callout(NO_DUMB_QUESTIONS, "'Atributo é só uma variável no nó?' Basicamente, sim! O que importa é DE ONDE vem o valor dela: dos filhos (sintetizado) ou do pai/irmãos (herdado)."),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.VISUAL, "Assistindo os tipos subirem pela árvore",
        listOf(
            Say(PROF, "Vamos ver o verificador de tipos trabalhando nó por nó, em pós-ordem: filhos primeiro, pai depois."),
            Visual(VisualKind.ATTR_TREE, input = "(a + b) * c", caption = "Tudo int: as folhas consultam a tabela de símbolos, os operadores combinam. Valor 18 com a=4, b=2, c=3."),
            Visual(VisualKind.ATTR_TREE, input = "a + x * 2", caption = "Aqui aparece a coerção: no '+', o int encontra um float e é alargado."),
            Say(BYTE, "Então o tipo do pai é só 'o maior' dos tipos dos filhos?"),
            Say(PROF, "Nas aritméticas, sim: int < float, e o resultado é o maior. Para comparações e lógica, as regras são outras."),
            Visual(VisualKind.ATTR_TREE, input = "a < b && ok", caption = "Comparação sempre produz bool; && exige dois bool."),
            Predict(
                "E ok + 1, com ok sendo bool?",
                listOf("int (true vale 1)", "erro de tipo", "bool"),
                1,
                "Nesta linguagem (como em Java), não dá para fazer aritmética com bool. E o erro se propaga para cima.",
            ),
            Visual(VisualKind.ATTR_TREE, input = "ok + 1", caption = "O nó '+' marca erro: bool não entra em aritmética."),
            Visual(VisualKind.PARSE_TREE, grammar = P3_DECL, input = "int id , id ;", caption = "Atributo herdado: T.tipo = int é passado para L.in e desce para cada id da lista."),
            Steps(
                "O que você viu",
                listOf(
                    "Folhas: o tipo vem do literal ou da tabela de símbolos",
                    "Nós internos: tipo calculado dos filhos → sintetizado",
                    "Ordem de avaliação: pós-ordem (como o \$\$ do Bison, numa redução)",
                    "Na declaração, o tipo desce pela lista → herdado",
                ),
            ),
            Callout(BRAIN_POWER, "Um analisador LR calcula atributos sintetizados de graça: em cada redução, os valores dos filhos estão na pilha. É exatamente o \$\$ = \$1 + \$3 do Bison."),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.WORKED, "Árvore anotada com valor e com tipo",
        listOf(
            Say(PROF, "Primeiro, valores. A SDD clássica da calculadora:"),
            Code(
                """E → E1 + T    E.val = E1.val + T.val
E → T         E.val = T.val
T → T1 * F    T.val = T1.val * F.val
T → F         T.val = F.val
F → ( E )     F.val = E.val
F → num       F.val = num.lexval""",
                "SDD S-atribuída (só atributos sintetizados)",
            ),
            Steps(
                "Anotando 3 * 5 + 4 de baixo para cima",
                listOf(
                    "Folhas: num.lexval = 3, 5, 4 → F.val = 3, 5, 4",
                    "T → F: T.val = 3",
                    "T → T1 * F: T.val = 3 * 5 = 15",
                    "E → T: E.val = 15",
                    "E → E1 + T (com T.val = 4): E.val = 15 + 4 = **19**",
                ),
            ),
            Say(PROF, "Agora, tipos, com coerção. Regra: se algum operando é float, o resultado é float e o int é convertido."),
            Steps(
                "Tipando a + x * 2 (a: int, x: float)",
                listOf(
                    "a → int (tabela de símbolos); x → float; 2 → int (literal)",
                    "x * 2: float * int → **float**, converte 2 para 2.0",
                    "a + (x * 2): int + float → **float**, gera inttofloat(a)",
                ),
            ),
            Visual(VisualKind.ATTR_TREE, input = "a + x * 2", caption = "Confira: raiz float, com a coerção marcada."),
            Say(BYTE, "Deixa comigo agora."),
            Reveal("Tipo de (a + b) * c, com a, b, c int?", "int em todos os nós internos; nenhuma coerção."),
            Reveal("Tipo de x * 2 + 3 * 4?", "x * 2 é float; 3 * 4 é int; float + int → float (o 12 é convertido)."),
            Reveal("Tipo de a < b && ok?", "a < b → bool; bool && bool (ok) → bool."),
            Visual(VisualKind.ATTR_TREE, input = "x * 2 + 3 * 4", caption = "Confira a segunda resposta."),
            Callout(EXAM_TIP, "Em 'desenhe a árvore anotada', escreva o atributo ao lado de CADA nó (até os T → F chatos). O professor quer ver a ordem de avaliação, não só o resultado."),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.SOCRATIC, "De onde vem a informação de cada nó?",
        listOf(
            Say(PROF, "Pergunta-guia de hoje: para calcular este atributo, eu preciso olhar PARA BAIXO ou PARA CIMA?"),
            Reveal("O valor de E em E → E1 + T depende de quê?", "De E1.val e T.val — dos filhos. É sintetizado."),
            Reveal("Se TODOS os atributos são sintetizados, em que ordem dá para avaliar?", "Pós-ordem (de baixo para cima). Uma SDD assim é **S-atribuída** e combina perfeitamente com um analisador LR: avalia na hora da redução."),
            Reveal("E em D → T L, com 'int a, b, c', como cada id descobre seu tipo?", "Pelo atributo herdado L.in = T.tipo, que desce pela lista. O id está abaixo e à direita de quem sabe o tipo."),
            Say(BYTE, "Então herdado pode vir de qualquer lugar?"),
            Reveal("Se um atributo herdado pudesse depender de um irmão à DIREITA, um analisador que lê da esquerda para a direita conseguiria calcular na hora?", "Não: o irmão da direita ainda não foi analisado. Daí a restrição L-atribuída."),
            Predict(
                "Numa SDD **L-atribuída**, o atributo herdado de Xi em A → X1 X2 ... Xn pode depender de...",
                listOf(
                    "Qualquer atributo de qualquer filho",
                    "Atributos herdados de A e atributos de X1 ... Xi-1 (irmãos à esquerda)",
                    "Apenas atributos sintetizados de A",
                ),
                1,
                "L de Left: só o pai (herdados dele) e os irmãos à esquerda. Assim uma passada em profundidade, da esquerda para a direita, resolve tudo — perfeito para descida recursiva.",
            ),
            Reveal("Toda SDD S-atribuída é L-atribuída?", "Sim! Ela não tem herdados, então a restrição é satisfeita trivialmente."),
            Reveal("Por que o verificador de tipos precisa da tabela de símbolos?", "Porque nas folhas só há nomes. O tipo de 'a' foi decidido lá na declaração; a tabela é a memória que liga uso e declaração."),
            Visual(VisualKind.ATTR_TREE, input = "a + b * c", caption = "Pós-ordem em ação: b * c primeiro, depois a soma."),
            Callout(BRAIN_POWER, "Se o grafo de dependências entre atributos tiver um ciclo, não existe ordem de avaliação. S-atribuída e L-atribuída são duas classes que garantem: sem ciclos."),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.FORMAL, "SDD, atributos e sistema de tipos",
        listOf(
            Text("**Definição dirigida pela sintaxe (SDD):** gramática livre de contexto + atributos associados aos símbolos + regras semânticas associadas às produções."),
            Steps(
                "Tipos de atributo (para um não-terminal A num nó N)",
                listOf(
                    "**Sintetizado:** definido por regra da produção em que A é a CABEÇA, usando atributos dos filhos de N e do próprio N",
                    "**Herdado:** definido por regra da produção em que A está no CORPO (no pai de N), usando atributos do pai, dos irmãos e do próprio N",
                    "Terminais só têm atributos sintetizados, fornecidos pelo léxico (ex.: num.lexval)",
                ),
            ),
            Steps(
                "Classes de SDD",
                listOf(
                    "**S-atribuída:** só atributos sintetizados → avaliação em pós-ordem; implementável durante a análise LR",
                    "**L-atribuída:** em A → X1...Xn, cada herdado de Xi depende só de herdados de A e de atributos de X1...Xi-1 (e de Xi, sem ciclo) → uma passada em profundidade, esquerda para direita",
                    "**Esquema de tradução (SDT):** gramática com fragmentos de programa {ações} embutidos no corpo; em SDD S-atribuída, as ações vão no fim (SDT pós-fixo)",
                ),
            ),
            Code(
                """D → T L ;        L.in = T.tipo
T → int          T.tipo = int
T → float        T.tipo = float
L → L1 , id      L1.in = L.in ;  addType(id.entrada, L.in)
L → id           addType(id.entrada, L.in)""",
                "SDD L-atribuída clássica: declarações (in é herdado)",
            ),
            Code(
                """E → E1 op E2   (op aritmético):
  se E1.tipo = bool ou E2.tipo = bool: erro de tipo
  E.tipo := max(E1.tipo, E2.tipo)      // int < float
  E.addr1 := widen(E1.addr, E1.tipo, E.tipo)   // insere inttofloat se preciso
  E.addr2 := widen(E2.addr, E2.tipo, E.tipo)
E → E1 relop E2:  numéricos → E.tipo := bool
E → E1 && E2:     ambos bool → E.tipo := bool""",
                "Verificação de tipos com coerção (alargamento)",
            ),
            Predict(
                "Uma coerção int → float inserida pelo compilador é chamada de...",
                listOf("Conversão explícita (cast)", "Coerção implícita (alargamento)", "Estreitamento"),
                1,
                "Coerção = conversão implícita feita pelo compilador. int → float é alargamento (não perde a ordem de grandeza). float → int seria estreitamento e costuma exigir cast explícito.",
            ),
            Visual(VisualKind.ATTR_TREE, input = "a + x * 2", caption = "Instância da regra acima: max(int, float) = float, com widen em a."),
            Callout(EXAM_TIP, "Ao classificar uma SDD, verifique CADA regra que define um herdado: ela usa algum irmão à direita? Se sim, não é L-atribuída. Sem herdados → S-atribuída (e também L)."),
            Callout(WATCH_OUT, "Verificação de tipos é estática (em tempo de compilação). Divisão por zero com valores de variáveis só aparece em tempo de execução — não é erro de tipo."),
        ),
    ),
    Lesson(
        Skill.ATTRIBUTES_TYPES, Style.MNEMONIC, "S sobe, H desce",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Cidade Semântica, prédio dos Tipos. Um elevador só sobe (sintetizado); a escada só desce (herdado)."),
            Callout(MNEMONIC, "**S↑ H↓** — **S**intetizado **S**obe (dos filhos para o pai); **H**erdado desce (do pai/irmãos para o filho)."),
            Callout(MNEMONIC, "**S-atribuída = Só Sintetizado.** **L-atribuída = Left**: herda só do pai e dos irmãos da esquerda."),
            Callout(MNEMONIC, "Coerção é **copo pequeno no copo grande**: int cabe em float (alargamento). O contrário derrama (estreitamento)."),
            Code(
                """int   ⊕ int    → int
int   ⊕ float  → float   (inttofloat no int)
float ⊕ float  → float
num  relop num → bool
bool  &&  bool → bool
bool  ⊕  num   → ERRO   (e o erro sobe)""",
                "Tabela de bolso dos tipos (⊕ = + - * /)",
            ),
            Say(BYTE, "Tipo é que nem fofoca: começa na folha e todo mundo acima fica sabendo!"),
            Predict(
                "Bison: \$\$ = \$1 + \$3; calcula um atributo...",
                listOf("Herdado", "Sintetizado", "Global"),
                1,
                "\$\$ é o pai, calculado dos filhos \$1 e \$3: sobe. O Bison é uma máquina de SDD S-atribuída.",
            ),
            Visual(VisualKind.ATTR_TREE, input = "ok + 1", caption = "O erro também sobe: um bool numa soma contamina a raiz."),
            Reveal("Diga em uma frase: sintetizado vs herdado.", "Sintetizado: calculado a partir dos filhos (sobe). Herdado: calculado a partir do pai e dos irmãos (desce/vem do lado)."),
            Callout(FIRESIDE, "Sintetizado: 'Eu sou prático: espero meus filhos terminarem e resumo tudo.' Herdado: 'E eu levo o contexto que você não vê lá de baixo, tipo o int de int a, b, c.'"),
        ),
    ),
)
