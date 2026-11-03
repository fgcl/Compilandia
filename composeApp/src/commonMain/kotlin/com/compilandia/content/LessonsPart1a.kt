package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 1a do conteúdo: PHASES, TOKENS, REGEX_DFA (Porto da Partida e Floresta Léxica).

// ───────────────────────────── PHASES ─────────────────────────────

private val p1aPhases: List<Lesson> = listOf(
    Lesson(
        Skill.PHASES, Style.STORY, "A Lanchonete do Compilador",
        listOf(
            Say(NARRADOR, "Sexta à noite, a Lanchonete do Zé está lotada. Você escreve num papelzinho: 'X-tudo sem cebola + suco de laranja'. Esse papel é o seu **código-fonte**."),
            Say(BYTE, "Peraí… o que uma lanchonete tem a ver com compilador?"),
            Say(PROF, "Tudo! O pedido passa por uma linha de montagem: cada pessoa faz UMA tarefa e passa adiante. Um compilador é exatamente isso: uma sequência de **fases**."),
            Steps(
                "A linha de montagem da lanchonete",
                listOf(
                    "🧾 O atendente lê o papel letra por letra e separa os itens: [X-tudo] [sem cebola] [+] [suco de laranja] → **análise léxica** (caracteres viram tokens).",
                    "📋 O chefe confere se o pedido está bem montado: 'X-tudo com com batata sem' não é pedido → **análise sintática** (tokens viram árvore).",
                    "🤔 O gerente confere o SENTIDO: 'suco de feijoada' tem forma de pedido, mas não existe → **análise semântica** (tipos, declarações).",
                    "📄 O pedido vira uma comanda padronizada que qualquer cozinheiro entende → **geração de código intermediário**.",
                    "⚡ A cozinha percebe que dá para esquentar os dois pães na mesma chapa → **otimização**.",
                    "🍔 O lanche sai pronto → **geração de código** (código de máquina).",
                ),
            ),
            Say(BYTE, "E se alguém da linha errar?"),
            Say(PROF, "Cada fase pega um tipo de erro. Letra ilegível é com o atendente (léxico). Pedido sem pé nem cabeça é com o chefe (sintático). Pedido sem sentido é com o gerente (semântico)."),
            Predict(
                "O cliente escreveu 'X-tudo §§ batata'. Quem reclama PRIMEIRO?",
                listOf("O atendente (léxico)", "O chefe (sintático)", "O gerente (semântico)", "O cozinheiro (geração de código)"),
                0,
                "'§§' nem é um símbolo do cardápio: o atendente não consegue transformar isso em item. Em C, um caractere solto como '@' é erro **léxico** — ninguém depois dele chega a ver o problema.",
            ),
            Callout(BRAIN_POWER, "E o caderninho onde o atendente anota o nome de cada cliente e o que ele pediu? Todo mundo da lanchonete consulta esse caderninho…"),
            Reveal("Quem é o 'caderninho' que todas as fases consultam?", "A **tabela de símbolos**: guarda os nomes do programa (variáveis, funções) com tipo, escopo e outras informações. Todas as fases leem e escrevem nela."),
            Callout(FIRESIDE, "Atendente, chefe e gerente ENTENDEM o pedido: são a **análise** (front-end). A cozinha CONSTRÓI a comida: é a **síntese** (back-end). A comanda padronizada é a ponte — troque a cozinha (outra máquina) e o salão continua igual."),
            Callout(NO_DUMB_QUESTIONS, "P: As fases rodam uma depois da outra, cada uma inteira? R: Na prática várias fases são agrupadas numa mesma 'passada' — o sintático pede ao léxico 'me dá o próximo token' sob demanda. Mas pensar em fases separadas é o jeito certo de ENTENDER."),
            Reveal("Sem olhar: as seis fases, em ordem.", "Léxica → Sintática → Semântica → Código intermediário → Otimização → Geração de código. E a tabela de símbolos acompanhando todo mundo."),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.VISUAL, "Veja um programa atravessar a esteira",
        listOf(
            Say(PROF, "Hoje você vai VER as fases trabalhando. Toque em cada estação da esteira."),
            Visual(VK.PIPELINE, caption = "Toque em cada fase: o que entra e o que sai?"),
            Text("Repare: a entrada é uma **sequência de caracteres** e a saída é **código de máquina**. No meio, cada fase muda a *representação* do programa."),
            Say(LEXI, "Minha vez! Eu pego caracteres e devolvo tokens. Olha a katana:"),
            Visual(VK.TOKENIZE, input = "position = initial + rate * 60;", caption = "Cada cor é uma classe de token. Os espaços somem!"),
            Predict(
                "Quantos tokens tem `position = initial + rate * 60;`?",
                listOf("7", "8", "31 (um por caractere)", "5"),
                1,
                "8: position, =, initial, +, rate, *, 60 e ;. Espaços não viram token — o léxico os descarta.",
            ),
            Say(BYTE, "E depois dos tokens, o que acontece?"),
            Say(PROF, "O sintático monta a árvore, e o semântico pendura informação nela — como o TIPO de cada pedaço."),
            Visual(VK.ATTR_TREE, input = "a + x * 2", caption = "a é int, x é float. Veja onde o semântico insere conversões de tipo."),
            Callout(WATCH_OUT, "Descobrir que int misturado com float precisa de conversão (inttofloat) é trabalho da **análise semântica**, não da sintática. A árvore já existia; o semântico só a anotou."),
            Visual(VK.TAC, input = "a + b * c", caption = "Código de três endereços: no máximo um operador por instrução."),
            Text("Repare na ordem: `b * c` é calculado ANTES da soma. Quem decidiu isso foi a árvore sintática — a precedência já estava gravada nela."),
            Reveal("Por que se chama 'código de TRÊS endereços'?", "Cada instrução tem no máximo três operandos: dois de entrada e um resultado, como `t1 = b * c`."),
            Callout(REAL_WORLD, "O Clang é um front-end (C/C++ → LLVM IR) e o LLVM é o back-end que otimiza e gera código para x86, ARM… O app no seu celular passou por uma esteira exatamente assim."),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.WORKED, "position = initial + rate * 60, fase por fase",
        listOf(
            Say(PROF, "Vamos resolver com calma o exemplo mais famoso do Livro do Dragão. Entrada: `position = initial + rate * 60`, com as variáveis declaradas como float."),
            Steps(
                "Resolvido, fase a fase",
                listOf(
                    "1. **Léxica**: ⟨id,1⟩ ⟨=⟩ ⟨id,2⟩ ⟨+⟩ ⟨id,3⟩ ⟨*⟩ ⟨60⟩. O número em ⟨id,1⟩ aponta para a linha de `position` na tabela de símbolos.",
                    "2. **Sintática**: árvore com `=` na raiz; à direita, `+`; dentro do `+`, o `*` — mais embaixo, porque tem precedência maior.",
                    "3. **Semântica**: `rate` é float e `60` é inteiro → insere o nó `inttofloat(60)`.",
                    "4. **Código intermediário**: t1 = inttofloat(60); t2 = id3 * t1; t3 = id2 + t2; id1 = t3.",
                    "5. **Otimização**: converte 60 em 60.0 já na compilação e elimina temporários: t1 = id3 * 60.0; id1 = id2 + t1.",
                    "6. **Geração de código**: LDF R2, id3 / MULF R2, R2, #60.0 / LDF R1, id2 / ADDF R1, R1, R2 / STF id1, R1.",
                ),
            ),
            Code("t1 = inttofloat(60)\nt2 = id3 * t1\nt3 = id2 + t2\nid1 = t3", "Código intermediário antes de otimizar"),
            Say(BYTE, "Como a otimização conseguiu sumir com o inttofloat?"),
            Say(PROF, "Porque 60 é constante! O compilador converte UMA vez, na hora de compilar, em vez de converter toda vez que o programa roda."),
            Callout(WATCH_OUT, "Na tabela de símbolos, position = 1, initial = 2, rate = 3. Por isso `id1`, `id2`, `id3`: é só a ordem em que os nomes apareceram."),
            Text("Agora é sua vez, num exemplo **esmaecido**: eu começo, você termina. Entrada: `total = preco * 2`, com total e preco do tipo int."),
            Text("**Léxica**: ⟨id,1⟩ ⟨=⟩ ⟨id,2⟩ ⟨*⟩ ⟨2⟩. **Sintática**: `=` na raiz, `*` à direita com os filhos preco e 2."),
            Reveal("**Semântica**: precisa de alguma conversão?", "Não! preco é int e 2 é int: int * int = int. Nada de inttofloat. (Se preco fosse float, aí sim: inttofloat(2).)"),
            Reveal("**Código intermediário** (três endereços)?", "t1 = id2 * 2\nid1 = t1"),
            Reveal("**Otimização**: dá para melhorar?", "Sim: eliminar o temporário → id1 = id2 * 2. Um otimizador esperto ainda poderia trocar `* 2` por um deslocamento à esquerda (redução de força)."),
            Visual(VK.TAC, input = "a * 2", caption = "Confira com o motor (a no lugar de preco)."),
            Callout(EXAM_TIP, "Questão clássica: 'mostre a saída de cada fase para a atribuição X'. Treine com position = initial + rate * 60 até fazer de olhos fechados."),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.SOCRATIC, "Por que tantas fases?",
        listOf(
            Say(PROF, "Hoje eu não vou explicar. Vou só perguntar — e você descobre. Chutar errado faz parte."),
            Predict(
                "Quem detecta o erro em `int x = 3 @ 4;`?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Ninguém: compila normal"),
                0,
                "'@' não pertence a nenhum padrão de token da linguagem. O léxico trava antes de qualquer outra fase ver o código.",
            ),
            Predict(
                "E em `x = (3 + 4;`?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Ninguém: compila normal"),
                1,
                "Todos os tokens são válidos: x, =, (, 3, +, 4, ;. O problema é a ESTRUTURA — parêntese aberto sem fechar. Estrutura é com a sintática.",
            ),
            Predict(
                "E em `int x; x = \"oi\";`?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Ninguém: compila normal"),
                2,
                "Tokens ok, estrutura ok (é uma atribuição perfeita!). Mas guardar uma string num int não faz SENTIDO. Tipos = semântica.",
            ),
            Predict(
                "E `y = 5;` sem nunca ter declarado y (em C)?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Ninguém: compila normal"),
                2,
                "A estrutura é uma atribuição perfeita. Saber se `y` foi declarado exige consultar a tabela de símbolos → semântica.",
            ),
            Say(BYTE, "Hmm… cada fase pega um tipo de erro. Então é só divisão de tarefas?"),
            Say(PROF, "Isso! Agora outra pergunta. Imagine 5 linguagens e 4 processadores diferentes."),
            Reveal(
                "Se cada compilador fosse 'direto' de linguagem para máquina, quantos você escreveria? E com um código intermediário no meio?",
                "Direto: 5 × 4 = 20 compiladores. Com código intermediário: 5 front-ends + 4 back-ends = 9 peças. É por isso que existe a divisão front-end / back-end!",
            ),
            Callout(BRAIN_POWER, "E se você digitar `whlie (x)` em vez de `while (x)`? O léxico reclama?"),
            Reveal("Pense: `whlie` combina com o padrão de identificador?", "Combina! Letras seguidas de letras é um identificador válido. O léxico aceita feliz; o erro só aparece depois — `whlie (x)` até parece uma chamada de função."),
            Say(PROF, "Percebeu? Você acabou de deduzir sozinho: o léxico vê **palavras**, o sintático vê **estrutura**, o semântico vê **significado**."),
            Reveal("Resuma cada fase de análise em uma pergunta.", "Léxica: os caracteres formam palavras válidas? Sintática: as palavras formam frases bem estruturadas? Semântica: as frases fazem sentido (tipos, declarações)?"),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.FORMAL, "Compilador e fases: definições de prova",
        listOf(
            Text("**Definição.** Um *compilador* é um programa que lê um programa escrito numa linguagem (a linguagem **fonte**) e o traduz para um programa equivalente em outra linguagem (a linguagem **objeto**), relatando os erros detectados no programa fonte."),
            Text("Um *interpretador*, em vez de produzir um programa objeto, executa diretamente as operações especificadas no programa fonte sobre as entradas fornecidas pelo usuário."),
            Text("O compilador tem duas partes. **Análise** (front-end): divide o fonte em partes, impõe uma estrutura gramatical, detecta erros e constrói a representação intermediária e a tabela de símbolos. **Síntese** (back-end): constrói o programa objeto a partir da representação intermediária e da tabela de símbolos."),
            Code(
                """
                fluxo de caracteres
                  → Analisador léxico ............ fluxo de tokens
                  → Analisador sintático ......... árvore sintática
                  → Analisador semântico ......... árvore sintática (anotada)
                  → Gerador de cód. intermediário  representação intermediária
                  → Otimizador indep. de máquina . representação intermediária
                  → Gerador de código ............ código da máquina-alvo
                  → Otimizador dep. de máquina ... código da máquina-alvo
                Tabela de símbolos: compartilhada por todas as fases
                """.trimIndent(),
                "Fases de um compilador (Aho, Lam, Sethi, Ullman — cap. 1)",
            ),
            Steps(
                "O que cada fase faz (formato de prova)",
                listOf(
                    "**Análise léxica** (scanning): lê os caracteres e os agrupa em lexemas; para cada lexema produz um token ⟨nome-do-token, valor-de-atributo⟩.",
                    "**Análise sintática** (parsing): usa os tokens para construir a árvore de sintaxe, que representa a estrutura gramatical.",
                    "**Análise semântica**: usa a árvore e a tabela de símbolos para verificar a consistência com a definição da linguagem; tarefa central: verificação de tipos (incluindo coerções).",
                    "**Geração de código intermediário**: produz uma representação de baixo nível, fácil de gerar e de traduzir para a máquina-alvo (ex.: código de três endereços).",
                    "**Otimização de código**: melhora o código intermediário (mais rápido, menor, menos energia) preservando o significado.",
                    "**Geração de código**: mapeia a representação intermediária para a linguagem-alvo, escolhendo registradores e instruções.",
                ),
            ),
            Callout(NO_DUMB_QUESTIONS, "P: Fase é o mesmo que passada (pass)? R: Não. Fase é uma divisão LÓGICA do trabalho; passada é uma leitura completa da entrada. Várias fases podem ser agrupadas numa única passada."),
            Predict(
                "Qual fase recebe como entrada um fluxo de tokens?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Geração de código"),
                1,
                "O léxico PRODUZ tokens; o sintático os CONSOME para montar a árvore.",
            ),
            Predict(
                "Verificação de tipos é tarefa de qual fase?",
                listOf("Análise léxica", "Análise sintática", "Análise semântica", "Otimização"),
                2,
                "Tipos são significado: a análise semântica consulta a árvore e a tabela de símbolos para checá-los e inserir coerções.",
            ),
            Callout(EXAM_TIP, "Em questão discursiva, escreva para cada fase o trio **entrada → o que faz → saída**. E cite a tabela de símbolos e o tratamento de erros, que atravessam todas as fases."),
            Reveal("Diferença entre compilador e interpretador, em uma linha.", "O compilador traduz o programa inteiro para um programa-alvo (que depois executa); o interpretador executa o fonte diretamente. O código compilado costuma ser mais rápido; o interpretador costuma dar diagnósticos de erro melhores."),
        ),
    ),
    Lesson(
        Skill.PHASES, Style.MNEMONIC, "Lá Sempre Sai Isso: Ótimo Gol!",
        listOf(
            Say(LEXI, "Decorar seis fases em ordem? Fácil, se você der um empurrãozinho no cérebro."),
            Callout(MNEMONIC, "**L**á **S**empre **S**ai **I**sso: **Ó**timo **G**ol! → **L**éxica, **S**intática, **S**emântica, código **I**ntermediário, **O**timização, **G**eração de código."),
            Say(PROF, "Imagine o narrador do rádio gritando. Toda vez que ouvir 'GOL', lembre da última fase: a geração de código — o programa finalmente 'sai'."),
            Text("📍 **Palácio da memória**: o mapa da Compilândia É o pipeline. Você embarca no ⚓ Porto (código-fonte), corta tokens na 🌲 Floresta Léxica, atravessa o 🌄 Vale das Gramáticas e as fortalezas da análise sintática, chega à 🏙️ Cidade Semântica e termina na ⚒️ Forja do Código."),
            Reveal("Feche os olhos e ande pelo mapa: quais regiões, em ordem?", "Porto → Floresta Léxica → Vale das Gramáticas → Ponte da Transformação → Templo Top-Down → Fortaleza Bottom-Up → Oficina Bison → Cidade Semântica → Forja do Código. Do Vale até a Oficina é tudo análise sintática!"),
            Callout(MNEMONIC, "Zoom crescente: **letra → palavra → frase → sentido**. O léxico junta letras em palavras; o sintático junta palavras em frases; o semântico pergunta se a frase faz sentido."),
            Text("Frase famosa de Chomsky: *'Ideias verdes incolores dormem furiosamente.'* Palavras válidas ✅ (léxico ok), frase bem montada ✅ (sintaxe ok), sentido zero ❌ (a semântica reclamaria!)."),
            Predict(
                "'O gato bebeu o.' — em que nível está o problema?",
                listOf("Palavra (léxico)", "Estrutura (sintaxe)", "Sentido (semântica)"),
                1,
                "Todas as palavras existem, mas a frase termina no meio: falta o complemento. Problema de estrutura = sintático.",
            ),
            Callout(WATCH_OUT, "Confusão clássica: 'variável não declarada' NÃO é erro sintático. `y = 5;` tem estrutura perfeita; só a tabela de símbolos sabe que `y` não existe → erro semântico."),
            Callout(MNEMONIC, "**Front-end entende, back-end constrói.** Front = de frente para o programador (a linguagem fonte); back = lá atrás, perto da máquina."),
            Visual(VK.PIPELINE, caption = "Recite o mnemônico tocando em cada estação."),
            Reveal("Recite o mnemônico e diga o que SAI de cada fase.", "Léxica → tokens; Sintática → árvore; Semântica → árvore anotada; Intermediário → código de três endereços; Otimização → código intermediário melhorado; Geração → código de máquina."),
        ),
    ),
)

// ───────────────────────────── TOKENS ─────────────────────────────

private val p1aTokens: List<Lesson> = listOf(
    Lesson(
        Skill.TOKENS, Style.STORY, "O caixa do supermercado",
        listOf(
            Say(NARRADOR, "Supermercado em dia de promoção. A Lexi está no caixa, passando os produtos da esteira um por um."),
            Say(LEXI, "Bip! 'Guaraná 2L'. Bip! 'Pão francês'. Bip! 'Guaraná lata'. Pra mim, cada produto vai para uma CATEGORIA no cupom fiscal."),
            Say(PROF, "Repare em três coisas diferentes: o produto concreto na mão dela, a categoria impressa no cupom e a regra que diz qual produto vai em qual categoria."),
            Steps(
                "Supermercado → compilador",
                listOf(
                    "🥤 O produto concreto ('Guaraná 2L') → **lexema**: o pedaço de texto real do programa, como `contador` ou `42`.",
                    "🏷️ A categoria no cupom (BEBIDA) → **token**: o nome da classe, como `id` ou `num`.",
                    "📏 A regra ('todo líquido engarrafado é BEBIDA') → **padrão**: a descrição de quais lexemas pertencem ao token, como 'letra seguida de letras ou dígitos'.",
                ),
            ),
            Say(BYTE, "Então 'Guaraná 2L' e 'Guaraná lata' são lexemas diferentes do MESMO token?"),
            Say(PROF, "Exatamente! Como `x` e `total`: lexemas diferentes, mesmo token `id`."),
            Text("Mas no cupom a categoria sozinha não basta: precisa saber QUAL bebida foi. Por isso o token carrega um **atributo**: ⟨id, ponteiro para `total` na tabela de símbolos⟩."),
            Predict(
                "Em `if (x >= 10)`, qual é o LEXEMA do token de comparação?",
                listOf("relop (operador relacional)", ">=", "x >= 10", "if"),
                1,
                "`>=` é o texto real (lexema). 'relop' é o nome do token — a categoria.",
            ),
            Say(LEXI, "E tem mais: eu sempre pego o MAIOR pedaço possível. Vendo `>=`, não corto em `>` e `=`. É como ler 'guarda-chuva' como uma palavra só, e não 'guarda' + 'chuva'."),
            Callout(NO_DUMB_QUESTIONS, "P: E o `if`? Ele também casa com 'letras seguidas de letras', o padrão de identificador! R: Empate. Palavras-chave são **reservadas**: a regra delas tem prioridade. Já `iff` é mais comprido que `if`, então pelo maior casamento vira identificador."),
            Visual(VK.TOKENIZE, input = "if (iff >= 10) whilex = whilex + 1;", caption = "Repare: `iff` e `whilex` são identificadores, não palavras-chave!"),
            Callout(REAL_WORLD, "Por isso em C você não pode chamar uma variável de `while`, mas pode chamar de `whilex`."),
            Reveal("Na analogia, quem é lexema, quem é token e quem é padrão?", "Lexema = o produto concreto (o texto). Token = a categoria no cupom (a classe). Padrão = a regra que decide a categoria."),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.VISUAL, "Fatiando código em cores",
        listOf(
            Say(LEXI, "Olha a minha katana em ação. Cada cor é um token diferente."),
            Visual(VK.TOKENIZE, input = "while (i <= n1) i++;", caption = "Toque em cada token para ver o lexema e a classe."),
            Text("Repare em três coisas: (1) os espaços desaparecem; (2) `<=` é UM token; (3) `i++` vira dois tokens, `i` e `++`."),
            Predict(
                "Quantos tokens tem `x+=y;`?",
                listOf("3", "4", "5", "6"),
                1,
                "x, +=, y e ; — 4 tokens. Mesmo sem espaços, o maior casamento pega `+=` inteiro.",
            ),
            Visual(VK.TOKENIZE, input = "x+=y;", caption = "Sem espaços, o léxico ainda sabe onde cortar."),
            Say(PROF, "Mas como a Lexi SABE que `n1` é identificador? Ela segue um padrão — e padrão se desenha como autômato."),
            Visual(VK.DFA, input = "l(l|d)*", caption = "Padrão de identificador (l = letra, d = dígito). Teste 'ld', 'll' e 'dl'."),
            Say(BYTE, "'dl' foi rejeitada! Faz sentido: identificador não começa com dígito."),
            Visual(VK.DFA, input = "d+(pd+)?", caption = "Padrão de número (d = dígito, p = ponto). Teste 'dpd', 'd' e 'dp'."),
            Callout(WATCH_OUT, "Em 'dp' (como `3.`) o autômato termina num estado NÃO final. Então o maior casamento válido é só `3`; o ponto sobra para o próximo token."),
            Callout(BRAIN_POWER, "Como a Lexi corta `x = a+++b;`? Pense no maior casamento antes de tocar."),
            Reveal("Resposta", "x, =, a, ++, +, b, ; — ela morde `++` primeiro (o maior), e sobra um `+`."),
            Visual(VK.TOKENIZE, input = "x = a+++b;", caption = "Confira a mordida gulosa."),
            Reveal("Por que o token id carrega um ponteiro para a tabela de símbolos?", "O sintático só precisa saber que é um `id`, mas as fases seguintes precisam saber QUAL: nome, tipo, escopo. O atributo aponta para essa entrada."),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.WORKED, "Montando a tabela de tokens",
        listOf(
            Say(PROF, "Vamos montar a tabela lexema / token / atributo de uma linha, com calma."),
            Code("if (x >= 10) total += x;", "Entrada"),
            Steps(
                "Resolvido",
                listOf(
                    "`if` → palavra-chave **if**. Também casa com id, mas palavra reservada vence. Sem atributo.",
                    "`(` → delimitador **(**.",
                    "`x` → **id**, atributo: ponteiro para x na tabela de símbolos.",
                    "`>=` → **relop**, atributo GE. Maior casamento: não é `>` + `=`.",
                    "`10` → **num**, atributo: o valor 10.",
                    "`)` → delimitador **)**.",
                    "`total` → **id**, ponteiro para total.",
                    "`+=` → operador de atribuição composta **+=**.",
                    "`x` → **id**, o MESMO ponteiro do primeiro x.",
                    "`;` → delimitador **;**. Total: 10 tokens.",
                ),
            ),
            Visual(VK.TOKENIZE, input = "if (x >= 10) total += x;", caption = "Confira com a Lexi."),
            Callout(WATCH_OUT, "Os dois `x` geram dois TOKENS, mas a tabela de símbolos tem UMA só entrada para x. Token se repete; entrada na tabela, não."),
            Text("Sua vez (exemplo esmaecido): `while (i <= n1) i++;`"),
            Reveal("Quantos tokens?", "9: while, (, i, <=, n1, ), i, ++, ;"),
            Reveal("Qual é o token de `while`? E de `n1`?", "`while` é palavra-chave (reservada). `n1` é id: começa com letra e depois vem um dígito."),
            Reveal("`<=` e `++` são um token cada ou dois?", "Um cada — maior casamento. `<=` é relop (LE); `++` é o operador de incremento."),
            Visual(VK.TOKENIZE, input = "while (i <= n1) i++;", caption = "Confira suas respostas."),
            Callout(EXAM_TIP, "Questão comum: 'quantos tokens há no trecho?'. Armadilhas: operadores de dois caracteres (`<=`, `==`, `++`), número com ponto (`3.14` é UM token), string (`\"a b c\"` é UM token) e comentários (zero tokens)."),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.SOCRATIC, "Por que existe um analisador léxico?",
        listOf(
            Say(PROF, "Vou te fazer perguntas. Chute sem medo: errar aqui é parte do método."),
            Predict(
                "O analisador léxico descarta espaços e comentários. Por quê?",
                listOf(
                    "Porque eles não mudam o significado do programa (na maioria das linguagens)",
                    "Porque ocupam memória demais",
                    "Porque o sintático tem preguiça",
                    "Porque é proibido usar espaços em C",
                ),
                0,
                "Espaço só separa lexemas; depois disso não serve pra mais nada. (Python é a exceção famosa: a indentação vira tokens INDENT/DEDENT!)",
            ),
            Predict(
                "Em `count2=count2+1`, o que é `count2`?",
                listOf("Dois tokens: id `count` e num `2`", "Um único token id", "Erro léxico", "Uma palavra-chave"),
                1,
                "O padrão de id é letra seguida de letras OU dígitos, e o maior casamento engole o 2 junto.",
            ),
            Predict(
                "`>=` deve virar quantos tokens?",
                listOf("1", "2"),
                0,
                "Um só. Regra do **maior casamento** (maximal munch): pegue o maior prefixo da entrada que casa com algum padrão.",
            ),
            Say(BYTE, "Mas e o `if`? Ele casa com o padrão de identificador E com o de palavra-chave. Empate!"),
            Reveal("Como desempatar `if`? E por que `iff` não é palavra-chave?", "Empate de MESMO tamanho: vence a regra listada primeiro — palavras-chave vêm antes de id. `iff` tem 3 letras: o maior casamento prefere o id de 3 letras ao `if` de 2."),
            Predict(
                "Em `fi (a == f(x))`, o léxico percebe que `fi` deveria ser `if`?",
                listOf("Sim, é erro léxico", "Não: `fi` é um identificador válido"),
                1,
                "O léxico não enxerga o contexto: `fi` casa com o padrão de id. Exemplo literal do Livro do Dragão!",
            ),
            Say(PROF, "Última: por que não deixar o sintático fazer tudo, lendo caractere por caractere?"),
            Reveal(
                "Três motivos para separar análise léxica de sintática.",
                "1) **Simplicidade**: a gramática não precisa falar de espaços e comentários. 2) **Eficiência**: técnicas especializadas (buffers, autômatos) leem caracteres muito rápido. 3) **Portabilidade**: as peculiaridades da entrada ficam isoladas no léxico.",
            ),
            Callout(FIRESIDE, "Léxico: 'Eu trabalho com caracteres; você, com tokens.' Sintático: 'E eu te chamo quando preciso: getNextToken()!' — na prática o léxico é uma sub-rotina do sintático."),
            Visual(VK.TOKENIZE, input = "fi (a == f(x)) x = 1;", caption = "Veja: `fi` passa como identificador sem reclamação."),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.FORMAL, "Token, padrão e lexema: definições",
        listOf(
            Text("**Token**: par ⟨nome-do-token, valor-de-atributo⟩. O nome é um símbolo abstrato que representa um tipo de unidade léxica (palavra-chave, identificador…); é o que o analisador sintático processa."),
            Text("**Padrão**: descrição da forma que os lexemas de um token podem assumir. Para uma palavra-chave, é a própria sequência de caracteres; para identificadores, é uma estrutura casada por muitas cadeias."),
            Text("**Lexema**: sequência de caracteres do programa fonte que casa com o padrão de um token e é identificada pelo analisador léxico como uma instância desse token."),
            Code(
                """
                TOKEN       PADRÃO (informal)                    LEXEMAS
                if          caracteres i, f                      if
                else        caracteres e, l, s, e                else
                comparison  < ou > ou <= ou >= ou == ou !=       <=, !=
                id          letra seguida de letras e dígitos    pi, score, D2
                number      qualquer constante numérica          3.14159, 0, 6.02e23
                literal     qualquer coisa entre aspas           "core dumped"
                """.trimIndent(),
                "Exemplos de tokens (Livro do Dragão, Fig. 3.2)",
            ),
            Text("Exemplo: `E = M * C ** 2` gera ⟨id, ptr E⟩ ⟨assign_op⟩ ⟨id, ptr M⟩ ⟨mult_op⟩ ⟨id, ptr C⟩ ⟨exp_op⟩ ⟨number, 2⟩."),
            Code(
                """
                função getNextToken():
                    pular espaços e comentários (contando linhas)
                    lexemeBegin ← forward
                    para cada padrão P, em ordem de prioridade:
                        achar o MAIOR prefixo da entrada que casa com P
                    escolher o maior prefixo; empate → padrão listado primeiro
                    se nenhum casou: erro léxico (ex.: modo pânico)
                    se for id: instalar/consultar na tabela de símbolos
                    retornar ⟨nome, atributo⟩
                """.trimIndent(),
                "Esqueleto de um analisador léxico",
            ),
            Callout(NO_DUMB_QUESTIONS, "P: E quando nenhum padrão casa? R: Recuperação em **modo pânico**: apagar caracteres até achar um token válido. Outras ações possíveis: inserir, remover, substituir um caractere ou trocar dois adjacentes."),
            Predict(
                "Em `x = 3.14;`, quantos lexemas existem?",
                listOf("4", "5", "6"),
                0,
                "x, =, 3.14 e ; — o número com ponto é UM lexema só.",
            ),
            Callout(EXAM_TIP, "Não troque as definições! Token = CLASSE (nome + atributo). Lexema = INSTÂNCIA (o texto). Padrão = REGRA (em geral uma expressão regular). Frase de prova perfeita: 'o lexema `3.14` casa com o padrão de number e é reportado como o token ⟨number, 3.14⟩'."),
            Reveal("Por que o token id precisa de atributo e o token `if` não?", "Porque há infinitos lexemas possíveis para id — o sintático não saberia QUAL nome era. Para `if` existe um único lexema, então o nome do token já diz tudo."),
        ),
    ),
    Lesson(
        Skill.TOKENS, Style.MNEMONIC, "Gaveta, meia e etiqueta",
        listOf(
            Callout(MNEMONIC, "**Token é a gaveta**, **lexema é a meia** que você guarda, **padrão é a etiqueta** que diz quais meias vão naquela gaveta."),
            Say(LEXI, "Outro truque: **LEXema = LEtra EXata**. É o texto literal, do jeitinho que está no código."),
            Callout(MNEMONIC, "Do abstrato ao concreto: **T**ipo (token) → **P**rescrição (padrão) → **L**iteral (lexema)."),
            Text("📍 **Palácio da memória**: Floresta Léxica, primeira parada depois do Porto. A Lexi corta os troncos de texto com a katana — cada tora cortada é um lexema — e joga cada tora na carroça certa (o token)."),
            Callout(MNEMONIC, "**Maior casamento = guloso**: a Lexi sempre morde o maior pedaço que dá. `>=` inteiro, `count2` inteiro, `iff` inteiro."),
            Callout(MNEMONIC, "**Reservada é VIP**: em empate de tamanho, palavra-chave fura a fila na frente do identificador."),
            Predict(
                "Lendo `y = 42;`, qual é o LEXEMA do token ⟨num, 42⟩?",
                listOf("num", "42", "y", "⟨num, 42⟩"),
                1,
                "O lexema é o texto `42`. 'num' é o nome do token; o par inteiro é o token.",
            ),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que `x` e `y` são tokens diferentes — mesmo token id, lexemas diferentes; (2) achar que o léxico pega erro de digitação em palavra-chave (`whlie` é um id válido!); (3) contar espaços como tokens."),
            Reveal("Complete: 'O lexema ___ casa com o padrão ___ e vira o token ___.'", "Exemplo: 'O lexema `total` casa com o padrão `letra(letra|dígito)*` e vira o token ⟨id, ponteiro para total⟩.'"),
            Visual(VK.TOKENIZE, input = "float media = total / n1;", caption = "Gaveta, meia, etiqueta: identifique cada uma."),
        ),
    ),
)

// ───────────────────────────── REGEX_DFA ─────────────────────────────

private val p1aRegex: List<Lesson> = listOf(
    Lesson(
        Skill.REGEX_DFA, Style.STORY, "O trem que lê palavras",
        listOf(
            Say(NARRADOR, "Na Floresta Léxica passa um trenzinho. Ele só anda pelos trilhos pintados com a letra que está lendo."),
            Say(PROF, "Cada estação é um **estado**. Cada trilho tem uma letra: é uma **transição**. Algumas estações têm bandeira de chegada: são os **estados finais** (de aceitação)."),
            Say(BYTE, "E como o trem decide se a palavra é aceita?"),
            Say(PROF, "Ele parte da estação inicial e lê a palavra letra por letra, seguindo os trilhos. Acabou a palavra numa estação com bandeira? Aceita! Ficou sem trilho, ou parou longe da bandeira? Rejeita."),
            Visual(VK.DFA, input = "(a|b)*abb", caption = "O trem do Livro do Dragão: aceita palavras de a's e b's que terminam em abb. Teste 'aabb' e 'abab'."),
            Text("Esse trem é um **AFD** (autômato finito determinístico): em cada estação, para cada letra, há NO MÁXIMO um trilho. O maquinista nunca fica em dúvida."),
            Say(BYTE, "E se tivesse dois trilhos com a mesma letra?"),
            Say(PROF, "Aí é um **AFN** (não determinístico): um trem mágico que se divide e segue TODOS os caminhos ao mesmo tempo, como num multiverso. E ainda tem trilhos ε, que ele pega sem ler letra nenhuma."),
            Callout(FIRESIDE, "AFN: 'Eu sou fácil de construir a partir da expressão regular!' AFD: 'Mas eu sou rápido: um passo por letra, sem dúvida nenhuma.' Os dois reconhecem exatamente as mesmas linguagens: as regulares."),
            Steps(
                "A fábrica de trens",
                listOf(
                    "📝 **Expressão regular**: o desenho do trajeto, como `(a|b)*abb`.",
                    "🔀 **Construção de Thompson**: transforma a ER num AFN, peça por peça (união, concatenação, fecho).",
                    "🧺 **Construção de subconjuntos**: cada estação do AFD é um CESTO de estações do AFN onde o trem mágico poderia estar.",
                    "✂️ **Minimização**: junta estações que se comportam igual. O AFD de `(a|b)*abb` fica com 4 estados.",
                ),
            ),
            Predict(
                "Um AFN com 3 estados vira um AFD com, NO MÁXIMO, quantos estados?",
                listOf("3", "6", "8", "9"),
                2,
                "Cada estado do AFD é um SUBCONJUNTO dos estados do AFN: 2³ = 8 subconjuntos possíveis. Na prática, quase sempre são bem menos.",
            ),
            Callout(REAL_WORLD, "Lex/Flex fazem exatamente isso: você escreve as ERs dos tokens e eles geram o AFD que vira o coração do analisador léxico."),
            Reveal("Na analogia, o que é estação, trilho, bandeira e trem mágico?", "Estado, transição, estado final (de aceitação) e AFN (que pode estar em vários estados ao mesmo tempo)."),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.VISUAL, "Brincando com autômatos",
        listOf(
            Say(PROF, "Cada widget é um AFD MÍNIMO gerado pelo motor a partir da expressão regular. Teste cadeias e veja o caminho acender."),
            Visual(VK.DFA, input = "ab*", caption = "ab*: um a e depois quantos b quiser. Teste 'a', 'abbb' e 'ba'."),
            Text("Repare: o `*` (fecho de Kleene) virou um **laço** — um trilho que sai e volta para o mesmo estado."),
            Visual(VK.DFA, input = "(a|b)*abb", caption = "Termina em abb."),
            Predict(
                "Em `(a|b)*abb`, depois de ler 'ab', em que situação o autômato está?",
                listOf("Já aceitou", "Já viu 'ab' — falta um b", "Rejeitou para sempre", "Voltou ao início"),
                1,
                "Cada estado do AFD mínimo 'lembra' quanto do sufixo abb já viu: nada, 'a', 'ab' ou 'abb'. Por isso são 4 estados.",
            ),
            Visual(VK.DFA, input = "(aa)*", caption = "Quantidade par de a's: dois estados que se alternam, como um interruptor."),
            Say(BYTE, "Dá para fazer um que aceite a's e b's na mesma quantidade, tipo aaabbb?"),
            Say(PROF, "Não dá! Autômato finito tem memória finita: não consegue contar até um número arbitrário. aⁿbⁿ precisa de gramática livre de contexto — você vai ver no Vale das Gramáticas."),
            Visual(VK.DFA, input = "0|1(0|1)*", caption = "Binários sem zero à esquerda: teste '0', '101' e '011'."),
            Reveal("Por que '011' é rejeitada?", "Depois de ler '0', o autômato está num estado final SEM saídas: o '0' sozinho é aceito, mas não pode ser seguido de nada. Ler '1' cai no estado morto."),
            Callout(WATCH_OUT, "O motor desenha o AFD mínimo SEM o estado morto. Transição que não aparece = vai para o estado morto (rejeição)."),
            Visual(VK.DFA, input = "x(y|z)+", caption = "Agora teste você: quais cadeias são aceitas?"),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.WORKED, "Construção de subconjuntos na mão",
        listOf(
            Say(PROF, "Vamos converter um AFN em AFD com calma. Este AFN aceita as cadeias de a's e b's que terminam em ab."),
            Code("estados: 0 (inicial), 1, 2 (final)\n0 --a--> 0     0 --b--> 0\n0 --a--> 1\n1 --b--> 2", "AFN: em 0, lendo a, o trem pode ficar em 0 OU ir para 1 (não determinismo!)"),
            Text("Regra do jogo: cada estado do AFD é um **conjunto** de estados do AFN, e Dtran[T, x] = ε-fecho(move(T, x)). Este AFN não tem transições ε, então o ε-fecho não acrescenta nada."),
            Steps(
                "Resolvido",
                listOf(
                    "Início: A = ε-fecho({0}) = {0}.",
                    "A com a: move({0}, a) = {0, 1} → novo estado **B**.",
                    "A com b: move({0}, b) = {0} → é o próprio **A**.",
                    "B com a: move({0, 1}, a) = {0, 1} → **B**.",
                    "B com b: move({0, 1}, b) = {0, 2} → novo estado **C**.",
                    "C com a: {0, 1} = **B**. C com b: {0} = **A**.",
                    "Finais: os conjuntos que contêm o estado 2 → só **C**. Pronto: AFD com 3 estados.",
                ),
            ),
            Code("      a    b\n→A    B    A\n B    B    C\n*C    B    A", "Tabela de transição do AFD (→ inicial, * final)"),
            Visual(VK.DFA, input = "(a|b)*ab", caption = "O motor chega ao mesmo AFD de 3 estados. Compare!"),
            Text("Agora esmaecido: AFN para cadeias que terminam em **bb**: 0 --a,b--> 0, 0 --b--> 1, 1 --b--> 2 (final)."),
            Reveal("A = {0}. Calcule Dtran[A, a] e Dtran[A, b].", "Dtran[A, a] = {0} = A. Dtran[A, b] = {0, 1} = novo estado B."),
            Reveal("Agora B = {0, 1}: com a? com b?", "B com a: {0} = A. B com b: {0, 1, 2} = novo estado C."),
            Reveal("C = {0, 1, 2}: com a? com b? Quem é final?", "C com a: {0} = A. C com b: {0, 1, 2} = C. Final: C (contém o 2). AFD com 3 estados."),
            Visual(VK.DFA, input = "(a|b)*bb", caption = "Confira: o motor também dá 3 estados."),
            Callout(EXAM_TIP, "Na prova, organize em tabela: 'estado do AFD', 'conjunto do AFN' e uma coluna por símbolo. E SEMPRE aplique o ε-fecho depois do move quando o AFN tiver transições ε — o de Thompson tem muitas!"),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.SOCRATIC, "Descobrindo as expressões regulares",
        listOf(
            Say(PROF, "Vou te mostrar autômatos e perguntar. As regras, você descobre."),
            Predict(
                "Quais cadeias `a(a|b)*b` aceita?",
                listOf("As que começam com a e terminam com b", "Só 'ab'", "Qualquer cadeia de a's e b's", "As que começam com b"),
                0,
                "Um a obrigatório, qualquer miolo de a's e b's, e um b obrigatório no fim.",
            ),
            Visual(VK.DFA, input = "a(a|b)*b", caption = "Teste: 'ab', 'aab', 'abba', 'ba'."),
            Predict(
                "`x(y|z)+` aceita a cadeia 'x'?",
                listOf("Sim", "Não"),
                1,
                "`+` significa 'uma ou mais vezes': r+ = r r*. Sem pelo menos um y ou z, rejeita.",
            ),
            Predict(
                "Qual ER descreve os binários ímpares (terminam em 1)?",
                listOf("(0|1)*1", "1(0|1)*", "(01)*", "0*1*"),
                0,
                "Qualquer coisa, desde que o último dígito seja 1. `1(0|1)*` descreve os que COMEÇAM com 1.",
            ),
            Visual(VK.DFA, input = "(0|1)*1"),
            Reveal("Escreva `r?` usando só |, concatenação e *.", "`r?` = `r | ε` (zero ou uma vez). E `r+` = `r r*`. Ou seja: `?` e `+` são só açúcar sintático!"),
            Predict(
                "Dá para escrever uma ER para parênteses balanceados, como (()) e ()()?",
                listOf("Sim, com bastante *", "Não: autômato finito não consegue contar profundidade arbitrária"),
                1,
                "Para saber quantos ')' faltam, seria preciso memória ilimitada. Isso é trabalho de gramática livre de contexto.",
            ),
            Say(PROF, "Agora a pergunta de ouro: por que o analisador léxico usa ER e autômato, e não gramática?"),
            Reveal("Por que ER/AFD no léxico?", "Tokens têm estrutura simples (regular), e o AFD reconhece em tempo linear: um passo por caractere, sem voltar atrás. Estruturas aninhadas (parênteses, blocos) ficam para o sintático, com gramáticas."),
            Reveal("Se AFN e AFD têm o mesmo poder, por que usar AFN?", "Porque o AFN é muito mais fácil de construir a partir de uma ER (Thompson, peça por peça). Depois convertemos para AFD, que é rápido de executar."),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.FORMAL, "ER, AFN e AFD: definições e algoritmos",
        listOf(
            Text("**Expressão regular** sobre o alfabeto Σ: ε e cada a ∈ Σ são ERs; se r e s são ERs, então r|s, rs, r* e (r) também são. L(r|s) = L(r) ∪ L(s); L(rs) = L(r)L(s); L(r*) = (L(r))*."),
            Text("Precedência: `*` (maior) > concatenação > `|` (menor), todas associativas à esquerda. Assim, `a|b*c` = `a|((b*)c)`."),
            Text("**AFN**: (S, Σ, move, s₀, F) — conjunto finito de estados S, alfabeto Σ, função de transição que leva (estado, símbolo ou ε) a um CONJUNTO de estados, estado inicial s₀ e estados finais F. **AFD**: caso especial sem transições ε e com exatamente um destino para cada par (estado, símbolo)."),
            Code(
                """
                Construção de Thompson (ER → AFN):
                  ε     :  i --ε--> f
                  a     :  i --a--> f
                  s|t   :  novo i --ε--> N(s) e N(t); finais de ambos --ε--> novo f
                  st    :  final de N(s) se funde com o inicial de N(t)
                  s*    :  i --ε--> N(s), i --ε--> f;
                           final de N(s) --ε--> início de N(s) e --ε--> f
                Propriedade: N(r) tem no máximo 2 × (nº de símbolos e operadores de r) estados.
                """.trimIndent(),
                "Algoritmo 3.23 (Livro do Dragão)",
            ),
            Code(
                """
                Construção de subconjuntos (AFN → AFD):
                  Dstates ← { ε-fecho(s₀) }, não marcado
                  enquanto existe T não marcado em Dstates:
                      marque T
                      para cada símbolo a:
                          U ← ε-fecho(move(T, a))
                          se U ∉ Dstates: adicione U (não marcado)
                          Dtran[T, a] ← U
                  finais: todo T que contém um estado final do AFN
                """.trimIndent(),
                "Algoritmo 3.20 (Livro do Dragão)",
            ),
            Text("**ε-fecho(T)**: estados alcançáveis a partir de algum estado de T usando só transições ε (inclui o próprio T). **move(T, a)**: estados alcançáveis a partir de T com UMA transição rotulada a."),
            Predict(
                "Qual é a precedência correta dos operadores?",
                listOf("| > concatenação > *", "* > concatenação > |", "concatenação > * > |"),
                1,
                "Como na aritmética: * é a 'potência', concatenação a 'multiplicação' e | a 'soma'.",
            ),
            Predict(
                "Pela precedência, `ab*` significa…",
                listOf("(ab)*", "a(b*)"),
                1,
                "O * se aplica só ao símbolo imediatamente à esquerda: um a seguido de zero ou mais b's.",
            ),
            Visual(VK.DFA, input = "ab*", caption = "Confirme: 'abab' é rejeitada."),
            Callout(EXAM_TIP, "Cai muito: (1) desenhar o AFN de Thompson de `(a|b)*abb` (11 estados, de 0 a 10); (2) aplicar subconjuntos e chegar a A, B, C, D, E, com E final; (3) minimizar juntando A e C → 4 estados. Saiba que ε-fecho({0}) = {0, 1, 2, 4, 7}."),
            Callout(WATCH_OUT, "Um AFN de n estados pode virar um AFD de até 2ⁿ estados (explosão exponencial), mas isso é raro na prática. Em troca, simular o AFD gasta só um passo por caractere."),
            Reveal("Defina ε-fecho e move em uma linha cada.", "ε-fecho(T): estados alcançáveis a partir de T só por transições ε (incluindo T). move(T, a): estados alcançáveis a partir de T por exatamente uma transição rotulada a."),
        ),
    ),
    Lesson(
        Skill.REGEX_DFA, Style.MNEMONIC, "Thompson Transforma, Subconjuntos Simplificam",
        listOf(
            Callout(MNEMONIC, "**T**hompson **T**ransforma (ER → AFN). **S**ubconjuntos **S**implificam o caos (AFN → AFD). E a **M**inimização **M**ata os estados repetidos."),
            Callout(MNEMONIC, "**N** de AFN = **N**ão sei para onde vou (vários caminhos). **D** de AFD = **D**ecidido (um caminho só)."),
            Callout(MNEMONIC, "Precedência igual à da matemática: `*` é potência, concatenação é multiplicação, `|` é soma. Como 2 + 3·4², `a|bc*` = `a|(b(c*))`."),
            Callout(MNEMONIC, "`*` = zero ou mais (a estrela é generosa: aceita até nada). `+` = um ou mais (MAIS = pelo menos um). `?` = talvez (zero ou um)."),
            Predict(
                "Qual ER aceita a cadeia vazia?",
                listOf("a+", "ab*", "(ab)*", "x(y|z)+"),
                2,
                "Só a estrela no 'lado de fora' permite zero repetições de tudo. As outras exigem pelo menos um símbolo.",
            ),
            Visual(VK.DFA, input = "(ab)*", caption = "O estado inicial já é final: a cadeia vazia é aceita!"),
            Text("📍 **Palácio da memória**: ainda na Floresta Léxica, o **Trem do Autômato**. A Lexi desenha o trajeto (ER), os duendes de Thompson espalham trilhos ε por todo lado (AFN), e o maquinista junta estações em cestos (subconjuntos) para nunca ficar em dúvida (AFD)."),
            Callout(WATCH_OUT, "Confusões clássicas: (1) achar que AFN é mais poderoso que AFD — reconhecem as mesmas linguagens; (2) esquecer o ε-fecho depois do move; (3) achar que ER sabe contar (aⁿbⁿ não é regular!)."),
            Callout(MNEMONIC, "**Regular não conta e não aninha.** Precisou contar ou casar parênteses? É trabalho de gramática."),
            Reveal("Recite a esteira com o nome de cada algoritmo.", "ER →(Thompson)→ AFN →(subconjuntos)→ AFD →(minimização)→ AFD mínimo."),
        ),
    ),
)

internal val p1aLessons: List<Lesson> = p1aPhases + p1aTokens + p1aRegex
