package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.AstTools
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 6b do conteúdo: AST_BUILD (Cidade Semântica — Fábrica de AST).
// Contagens de nós conferidas com AstTools.concreteTree / countNodes (gramática E/T/F do motor):
//  "a + b * c" 13 × 5;  "( a + b ) * c" 18 × 5;  "a - b - c" 14 × 5;  "( ( a ) )" 14 × 1;
//  "2 * x + y" 13 × 5;  "a - ( b - c )" 19 × 5;  "( a + b ) * ( c - x )" 28 × 7.

/** Assembly / Bison em strings cruas: '§' vira '$'. */
internal fun p6Asm(s: String): String = s.trimIndent().replace('§', '$')

private val P6_AST_CMDS: String = """
abstract class Cmd { }

class Atrib extends Cmd {          // x = expr;
    String var; Expr valor;
}
class Se extends Cmd {             // if (cond) entao else senao
    Expr cond; Cmd entao, senao;   // senao == null quando não há else
}
class Enquanto extends Cmd {       // while (cond) corpo
    Expr cond; Cmd corpo;
}
class Bloco extends Cmd {          // { c1 c2 ... }
    List<Cmd> cmds = new ArrayList<>();
}
""".trimIndent()

private val P6_AST_BISON: String = p6Asm(
    """
    exp   : exp '+' termo     { §§ = new BinOp('+', §1, §3); }
          | exp '-' termo     { §§ = new BinOp('-', §1, §3); }
          | termo             { §§ = §1; }          /* corrente: só repassa */
          ;
    termo : termo '*' fator   { §§ = new BinOp('*', §1, §3); }
          | fator             { §§ = §1; }
          ;
    fator : '(' exp ')'       { §§ = §2; }          /* o parêntese SOME aqui */
          | ID                { §§ = new Var(§1); }
          | NUM               { §§ = new Num(§1); }
          ;
    """,
)

private val P6_AST_VISIT: String = """
abstract class Expr {
    abstract int avaliar(Map<String, Integer> mem);
    abstract String gerar(Gerador g);          // devolve onde ficou o valor
}
class Num extends Expr {
    int valor;
    int avaliar(Map<String, Integer> mem) { return valor; }
    String gerar(Gerador g) { return String.valueOf(valor); }
}
class Var extends Expr {
    String nome;
    int avaliar(Map<String, Integer> mem) { return mem.get(nome); }
    String gerar(Gerador g) { return nome; }
}
class BinOp extends Expr {
    char op; Expr esq, dir;
    int avaliar(Map<String, Integer> mem) {
        int x = esq.avaliar(mem), y = dir.avaliar(mem);   // filhos primeiro (pós-ordem)
        switch (op) { case '+': return x + y; case '-': return x - y;
                      case '*': return x * y; default:  return x / y; }
    }
    String gerar(Gerador g) {
        String x = esq.gerar(g), y = dir.gerar(g);
        String t = g.novoTemp();
        g.emite(t + " = " + x + " " + op + " " + y);       // TAC!
        return t;
    }
}
""".trimIndent()

internal val p6AstLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.STORY, "O resumo e o livro inteiro",
        listOf(
            Say(NARRADOR, "Véspera de prova de Literatura. Byte tem duas opções: reler o livro inteiro, com prefácio, índice e notas de rodapé, ou ler o resumo de duas páginas da colega."),
            Say(BYTE, "O resumo, óbvio! Mas... não perde nada importante?"),
            Say(PROF, "Um BOM resumo não perde nada que importa para a prova: personagens, quem fez o quê, em que ordem. Ele joga fora só o que servia para o livro ser LIDO: capítulos de transição, numeração, notas. A **árvore sintática abstrata (AST)** é o resumo; a **árvore de derivação** é o livro inteiro."),
            Steps(
                "Livro × resumo → árvore de derivação × AST",
                listOf(
                    "📖 Livro inteiro → **árvore de derivação (concreta)**: um nó para CADA passo da gramática.",
                    "🔁 Capítulos de transição ('E vira T, que vira F...') → **não-terminais em corrente**: E → T → F → a. Somem na AST.",
                    "📑 Numeração e índice → **parênteses e pontuação**: só serviam para o parser saber a ordem. A forma da AST já guarda essa ordem.",
                    "🗒️ Páginas em branco → produções **ε**: somem.",
                    "⭐ Personagens e ações → **operadores e operandos**: viram nós `BinOp`, `Var`, `Num`.",
                ),
            ),
            Predict(
                "Para 'a + b * c', a árvore de derivação da gramática E/T/F tem 13 nós. Quantos tem a AST?",
                listOf("13", "9", "5", "3"),
                2,
                "Só sobram os 3 operandos e os 2 operadores: BinOp('+') com Var(a) e BinOp('*') com Var(b) e Var(c). 13 → 5: um resumo de verdade!",
            ),
            Visual(VK.AST, input = "a + b * c", caption = "Lado a lado: o livro (derivação) e o resumo (AST). Repare que o * continua mais FUNDO que o +: a precedência sobreviveu."),
            Say(BYTE, "Então a AST é só uma árvore mais magrinha. Por que ela é tão importante?"),
            Say(PROF, "Porque, depois da análise sintática, TODO o resto do compilador estuda pelo resumo: o verificador de tipos, a tabela de símbolos, o gerador de código. Ninguém quer reler o livro inteiro três vezes."),
            Callout(REAL_WORLD, "No trabalho prático da PUC (o compilador em Java), muitos grupos traduzem direto dentro do parser: lê um token, já emite assembly. Funciona, mas é como fazer a prova lendo o livro uma vez só, sem poder voltar. Com a AST você lê uma vez, monta o resumo e depois pode passar por ele quantas vezes quiser: uma vez para checar tipos, outra para gerar código."),
            Callout(NO_DUMB_QUESTIONS, "P: Se eu jogo fora os parênteses, como o compilador sabe que (a + b) * c é diferente de a + b * c? R: Pela FORMA da árvore. Em (a + b) * c, o + fica embaixo do *; em a + b * c, é o * que fica embaixo do +. Quem está mais fundo é calculado primeiro."),
            Reveal("Na analogia, quem é: livro inteiro, resumo, capítulo de transição, numeração das páginas?", "Árvore de derivação, AST, não-terminal em corrente (E → T → F), parênteses/pontuação."),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.VISUAL, "Dieta de árvores, ao vivo",
        listOf(
            Say(PROF, "Hoje cada widget mostra a mesma expressão duas vezes: a árvore de derivação (gorda) e a AST (magra), com a contagem de nós e o código Java que monta a AST."),
            Visual(VK.AST, input = "( ( a ) )", caption = "O caso extremo: 14 nós na árvore de derivação, 1 nó na AST. Tudo era parêntese e corrente."),
            Predict(
                "O que restou na AST de '( ( a ) )'?",
                listOf("BinOp com dois filhos", "Um único Var(a)", "Dois nós de parênteses", "Nada: a AST fica vazia"),
                1,
                "Os parênteses só agrupavam; não há operação nenhuma. A AST é apenas new Var(\"a\").",
            ),
            Visual(VK.AST, input = "a - b - c", caption = "Associatividade: 14 nós contra 5. Veja qual '-' ficou embaixo."),
            Predict(
                "Na AST de 'a - b - c', qual subtração fica mais funda (é feita primeiro)?",
                listOf("A da esquerda: (a - b) - c", "A da direita: a - (b - c)", "Tanto faz: dá o mesmo valor"),
                0,
                "A gramática E → E - T é recursiva à ESQUERDA, então a árvore cresce para a esquerda: new BinOp('-', new BinOp('-', a, b), c). E não dá o mesmo valor: com a=10, b=3, c=2, (10-3)-2 = 5 mas 10-(3-2) = 9.",
            ),
            Visual(VK.AST, input = "a - ( b - c )", caption = "Agora com parênteses à direita: 19 nós contra 5. A forma da AST mudou, mesmo sem nenhum nó de parêntese."),
            Visual(VK.AST, input = "( a + b ) * ( c - x )", caption = "28 nós na derivação, 7 na AST: 4 folhas e 3 operadores."),
            Steps(
                "Regras que você acabou de ver acontecendo",
                listOf(
                    "Folha id/num → **Var** ou **Num** (um nó só, sem a corrente F → id).",
                    "Produção com operador (E → E + T) → **BinOp** com o operador e os dois filhos.",
                    "Produção de corrente (E → T, T → F) → nenhum nó novo: só repassa o filho.",
                    "F → ( E ) → nenhum nó novo: devolve a AST de E. O parêntese evapora.",
                    "Em expressões binárias: nós da AST = 2 × (nº de operadores) + 1.",
                ),
            ),
            Visual(VK.MINILANG, input = "m = a;\nif (b > m) { m = b; }\nprint(m);", caption = "Comandos também viram nós (Atrib, Se...). O gerador anda nessa árvore e emite os desvios. Com a=4 e b=7, imprime 7."),
            Callout(BRAIN_POWER, "Uma expressão com 10 operadores binários: quantos nós tem a AST? E por que a árvore de derivação sempre tem MAIS nós que isso?"),
            Reveal("Resposta", "21 nós (10 BinOp + 11 folhas). A árvore de derivação tem, no mínimo, um nó de não-terminal para cada nó da AST, mais os nós de corrente, de parênteses e dos próprios tokens de operador como folhas."),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.WORKED, "Construindo a AST na descida recursiva",
        listOf(
            Say(PROF, "Lembra do parser descendente recursivo, com um método por não-terminal? A mudança é pequena: em vez de void, cada método **devolve uma Expr**."),
            Code(AstTools.javaClasses, "As classes da AST (Java, como no trabalho prático)"),
            Code(AstTools.javaParserBuildingAst, "O parser que CONSTRÓI a AST. O while em expr() e termo() garante associatividade à esquerda."),
            Steps(
                "Rastreando '2 * x + y'",
                listOf(
                    "expr() chama termo(); termo() chama fator().",
                    "fator() vê '2' (número) → devolve **new Num(2)**. Em termo(), e = Num(2).",
                    "termo() vê '*': casa, chama fator(), que devolve **Var(x)**. e = **new BinOp('*', Num(2), Var(x))**.",
                    "termo() vê '+', que não é * nem /: sai do while e devolve o BinOp('*').",
                    "De volta em expr(): e = BinOp('*'). Vê '+': casa, chama termo(), que devolve **Var(y)**.",
                    "e = **new BinOp('+', BinOp('*', Num(2), Var(x)), Var(y))**. Fim da entrada: devolve e.",
                ),
            ),
            Visual(VK.AST, input = "2 * x + y", caption = "Confira: 13 nós na derivação, 5 na AST, e o código Java gerado."),
            Callout(WATCH_OUT, "A linha mais importante é `e = new BinOp(op, e, termo());`: o resultado ANTERIOR vira o filho ESQUERDO do novo nó. É assim que 'a - b - c' vira (a - b) - c. Se você escrever recursão (`e = new BinOp(op, e, expr())`), a árvore fica pendurada para a direita e a subtração dá errado."),
            Text("Agora é com você (exemplo esmaecido): **a - ( b - c )**."),
            Reveal("O que fator() devolve ao encontrar '('?", "Casa '(', chama expr() — que monta new BinOp('-', Var(b), Var(c)) — casa ')' e devolve ESSE BinOp. Nenhum nó de parêntese é criado."),
            Reveal("Escreva o código Java completo da AST.", "new BinOp('-', new Var(\"a\"), new BinOp('-', new Var(\"b\"), new Var(\"c\")))"),
            Reveal("Quantos nós tem cada árvore?", "Árvore de derivação: 19 nós. AST: 5 nós (2 BinOp + 3 Var)."),
            Visual(VK.AST, input = "a - ( b - c )", caption = "Confira suas respostas."),
            Callout(EXAM_TIP, "Questão comum: 'dada a gramática com ações semânticas, desenhe a AST de tal expressão'. Faça em dois passos: primeiro a árvore de derivação (rascunho), depois risque correntes e parênteses e troque cada produção com operador por um BinOp."),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.SOCRATIC, "O que o compilador realmente precisa saber?",
        listOf(
            Say(PROF, "Hoje eu pergunto, você descobre. Comece olhando a árvore de derivação de '( a + b ) * c'."),
            Visual(VK.AST, input = "( a + b ) * c", caption = "18 nós na derivação. Olhe primeiro só o lado esquerdo."),
            Predict(
                "Para GERAR CÓDIGO dessa expressão, de que informação o compilador precisa?",
                listOf("Quais operações, em que ordem, sobre quais operandos", "Quais não-terminais foram usados", "Onde estavam os parênteses no texto", "Quantos espaços havia entre os tokens"),
                0,
                "Gerar t1 = a + b; t2 = t1 * c só exige operações, operandos e ordem. Todo o resto era andaime para o PARSER decidir essa ordem.",
            ),
            Predict(
                "Se a AST não tem parênteses, onde está guardada a informação 'some antes de multiplicar'?",
                listOf("Num campo prioridade em cada nó", "Na forma da árvore: o + é FILHO do *", "Numa tabela à parte", "Ela se perde"),
                1,
                "Quem está mais fundo é avaliado primeiro (pós-ordem). A estrutura É a precedência.",
            ),
            Say(BYTE, "E o nó T no meio da corrente E → T → F? Ele não faz nada?"),
            Reveal("Faz alguma coisa?", "Na derivação, ele registra que a gramática passou pelo nível de precedência do *. Mas, como não houve nenhum *, não há operação a representar. Na AST, ele simplesmente some: o método termo() devolve o que fator() devolveu."),
            Predict(
                "No TP, seu grupo gera assembly direto no parser. Aí surge: 'a variável x é usada antes de ser declarada mais abaixo no arquivo'. O que fica difícil?",
                listOf("Nada", "Você já emitiu código para x antes de conhecer a declaração; seria preciso voltar atrás", "O analisador léxico quebra"),
                1,
                "Tradução direta é UMA passada só. Com uma AST, você faz uma passada para coletar declarações e checar tipos e OUTRA para gerar código, quantas forem necessárias.",
            ),
            Reveal("Cite duas outras vantagens de construir a AST antes de gerar código.", "1) Separação de responsabilidades: o parser só reconhece e monta; a semântica e o gerador vivem em métodos das classes (avaliar, verificarTipo, gerar), mais fáceis de testar. 2) Otimizações como dobra de constantes viram 'trocar um BinOp(Num, Num) por um Num' na árvore. Bônus: dá para imprimir a AST e depurar."),
            Predict(
                "E comandos, como if e while? Eles viram o quê na AST?",
                listOf("Somem, como os parênteses", "Nós próprios, como Se(cond, entao, senao) e Enquanto(cond, corpo)", "Folhas Var"),
                1,
                "Cada construção com significado vira uma classe. Palavras-chave, chaves e ponto e vírgula somem; condição e corpo ficam como filhos.",
            ),
            Visual(VK.MINILANG, input = "i = 0;\ns = 0;\nwhile (i < 4) { s = s + i; i = i + 1; }\nprint(s);", caption = "Um Enquanto com Bloco de duas Atrib dentro. O gerador percorre a árvore e produz o laço com desvios. Imprime 6."),
            Callout(FIRESIDE, "Árvore de derivação: 'Eu tenho TUDO, cada detalhe!' AST: 'E por isso ninguém aguenta andar em você três vezes.' Árvore de derivação: 'Hmpf. Pelo menos o parser me ama.' AST: 'O parser te usa e me entrega para o resto do compilador.'"),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.FORMAL, "AST: definição de prova",
        listOf(
            Text("**Árvore de derivação (concreta)**: raiz = símbolo inicial; cada nó interno é um não-terminal A cujos filhos são o lado direito de uma produção A → α usada; folhas, lidas da esquerda para a direita, formam a sentença."),
            Text("**Árvore sintática abstrata (AST)**: cada nó interno é um **operador ou construção** da linguagem e seus filhos são os **operandos/componentes** com significado. Omite não-terminais de cadeia (A → B), parênteses, pontuação e ε. A AST não é única da gramática: é uma escolha de projeto do compilador."),
            Code(P6_AST_CMDS, "Comandos como classes (junto com Expr, Num, Var, BinOp)"),
            Text("**Construção dirigida pela sintaxe**: cada produção recebe uma regra semântica que sintetiza o atributo `nodo` (a AST do lado esquerdo) a partir dos `nodo` dos filhos. É uma definição **S-atribuída**: os atributos sobem da folha para a raiz."),
            Code(P6_AST_BISON, "No Bison (ascendente): \$\$ é o valor do lado esquerdo; \$1, \$2, \$3 os dos símbolos do lado direito"),
            Predict(
                "Na regra `fator : '(' exp ')' { \$\$ = \$2; }`, por que \$2?",
                listOf("Porque o parêntese é o 2º símbolo", "Porque exp é o 2º símbolo do lado direito, e a AST do fator é simplesmente a AST de exp", "Para criar um nó de parênteses"),
                1,
                "'(' é \$1, exp é \$2, ')' é \$3. Repassar \$2 é exatamente 'jogar fora o parêntese'.",
            ),
            Text("**Descendente recursivo**: cada método de não-terminal devolve a AST do que reconheceu. Para A → A op B (recursiva à esquerda, eliminada como A → B (op B)*), use um **laço** que acumula: `e = new BinOp(op, e, B())`, preservando a associatividade à esquerda."),
            Code(P6_AST_VISIT, "Percorrendo a AST: cada fase é um método (ou um Visitor). Repare na pós-ordem: filhos antes do pai."),
            Callout(REAL_WORLD, "Com muitas fases, em vez de pôr avaliar/gerar/verificarTipo em cada classe, usa-se o padrão **Visitor**: uma classe por fase (VerificadorDeTipos, GeradorMips) com um método visitar por tipo de nó. javac, Roslyn (C#) e o Clang são organizados em torno da AST assim."),
            Callout(EXAM_TIP, "Três perguntas que caem: (1) desenhar a AST de uma expressão (lembre: sem parênteses, associativa à esquerda); (2) escrever as ações \$\$ = new ... de uma gramática Bison; (3) 'diferencie árvore de derivação de AST' — responda com a definição + um exemplo com contagem de nós."),
            Reveal("Defina AST e cite três coisas que ela descarta em relação à árvore de derivação.", "Árvore em que nós internos são operadores/construções e filhos são operandos com significado. Descarta: não-terminais de cadeia (E → T → F), parênteses/pontuação e produções ε (e, com elas, as palavras-chave que só marcavam a construção)."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.AST_BUILD, Style.MNEMONIC, "A árvore de dieta",
        listOf(
            Callout(MNEMONIC, "**AST = Árvore Sem Tralha.** Tira três tralhas: **C**orrentes (E → T → F), **P**arênteses (e pontuação) e **E**psilons. 'Corta o CPE.'"),
            Text("📍 **Palácio da memória**: na Cidade Semântica, ao lado do cartório da tabela de símbolos, fica a **Fábrica de AST** 🪵, uma marcenaria. Chega um tronco enorme (a árvore de derivação); o marceneiro serra os galhos que só passam adiante, joga a serragem dos parênteses fora e entrega uma mesa de 5 peças, pronta para uso."),
            Callout(MNEMONIC, "**Parênteses são andaimes**: indispensáveis enquanto o prédio está sendo construído (parsing), retirados quando ele fica pronto. A forma do prédio (a árvore) já guarda o que eles seguravam."),
            Callout(MNEMONIC, "**Corrente de WhatsApp**: E → T → F → a é como mensagem encaminhada cinco vezes. O conteúdo é o 'a'; os encaminhamentos não acrescentam nada. Na AST: Var(a), e só."),
            Callout(MNEMONIC, "**Quem está mais fundo trabalha primeiro.** Na AST de a + b * c, o * está no porão: é feito antes. Profundidade = precedência."),
            Predict(
                "Qual linha de código é o 'segredo' da associatividade à esquerda no parser descendente?",
                listOf("return new BinOp(op, termo(), expr());", "e = new BinOp(op, e, termo()); dentro de um while", "if (tok.equals(\"(\")) return expr();"),
                1,
                "'O velho vai para a esquerda': o resultado acumulado vira filho esquerdo do novo nó, a cada volta do laço.",
            ),
            Callout(MNEMONIC, "**Bison em três palavras: 'filho vira pai'.** \$\$ = new BinOp('+', \$1, \$3) — os filhos \$1 e \$3 se tornam filhos do nó novo \$\$. Corrente? \$\$ = \$1. Parêntese? \$\$ = \$2."),
            Callout(WATCH_OUT, "Contrastes que confundem: **árvore de derivação** tem não-terminais e todos os tokens; a **AST** tem só operadores e operandos. **AST** é estrutura; **TAC** é uma lista de instruções gerada a partir dela. **Parse tree** = árvore de derivação = árvore sintática concreta (três nomes, uma coisa)."),
            Visual(VK.AST, input = "( a + b ) * c", caption = "Corta o CPE: conte as correntes, os parênteses e veja 18 virar 5."),
            Reveal("Sem olhar: o que é jogado fora, o que fica e onde mora a precedência?", "Fora: correntes, parênteses/pontuação, ε. Fica: operadores (BinOp) e operandos (Var, Num), e construções (Atrib, Se, Enquanto, Bloco). Precedência e associatividade moram na FORMA da árvore."),
            Reveal("Por que vale a pena montar a AST no TP em vez de gerar código direto no parser?", "Porque aí o compilador pode percorrer o programa várias vezes (tipos, depois código), com cada fase separada e testável, e otimizar mexendo na árvore. Tradução direta é uma passada só, sem volta."),
        ),
    ),
)
