package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.engine.RuntimeSim
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 5a do conteúdo: RUNTIME — ambientes de execução e registros de ativação (Livro do Dragão, cap. 7).
// Registradores MIPS em strings cruas são escritos com '§' e convertidos por p5Asm (ver LessonsPart5.kt).

/** Mesmo texto que o widget CALL_STACK mostra (vem do motor, então nunca diverge). */
private val P5_FAT_CODE: String = RuntimeSim.fatorial(3).code
private val P5_CHAIN1_CODE: String = RuntimeSim.chain(1).code
private val P5_CHAIN2_CODE: String = RuntimeSim.chain(2).code

private val P5_FAT_ASM: String = p5Asm(
    """
            .text
    main:   li   §a0, 3            # argumento: n = 3
            jal  fat               # chama fat; §ra = endereço da linha de baixo
            move §a0, §v0          # o resultado chega em §v0
            li   §v0, 1            # syscall 1: imprimir inteiro
            syscall                # imprime 6
            li   §v0, 10           # syscall 10: encerrar
            syscall

    fat:    addi §sp, §sp, -8      # PRÓLOGO: abre o quadro (2 palavras)
            sw   §ra, 4(§sp)       # guarda o endereço de retorno
            sw   §a0, 0(§sp)       # guarda n
            slti §t0, §a0, 2       # §t0 = (n < 2)
            beq  §t0, §zero, REC   # n >= 2? vai para a recursão
            li   §v0, 1            # caso base: devolve 1
            addi §sp, §sp, 8       # fecha o quadro
            jr   §ra
    REC:    addi §a0, §a0, -1      # argumento n - 1
            jal  fat               # §v0 = fat(n - 1)
            lw   §a0, 0(§sp)       # recupera o SEU n
            lw   §ra, 4(§sp)       # recupera o SEU endereço de retorno
            addi §sp, §sp, 8       # EPÍLOGO: fecha o quadro
            mul  §v0, §a0, §v0     # n * fat(n - 1)
            jr   §ra               # volta para quem chamou
    """,
)

internal val p5RuntimeLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.STORY, "A pilha de pratos da cozinha",
        listOf(
            Say(NARRADOR, "Forja do Código, fim de expediente. O código já foi gerado... mas, quando ele RODA, onde moram as variáveis de cada função?"),
            Say(BYTE, "Na memória, ué. Cada variável tem o seu lugarzinho fixo, não tem?"),
            Say(PROF, "Quase! Pense na cozinha de um restaurante em dia de movimento. Cada pedido que começa vira um prato na pilha da bancada, com um bilhete grudado."),
            Text("O bilhete diz: **quais ingredientes** vieram no pedido, **para qual mesa** devolver o prato pronto e tem um espaço para os **rascunhos** do cozinheiro. E ele só mexe no prato do **topo**."),
            Text("Se, no meio de um prato, ele precisa de um molho que é outra receita, põe um **prato novo em cima**, faz o molho, tira esse prato e volta para o de baixo exatamente de onde parou."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Cada pedido em preparo → uma **ativação** (uma chamada de função em andamento)",
                    "Prato com bilhete → **registro de ativação** (ou **quadro**, *frame*)",
                    "Ingredientes do pedido → **parâmetros**",
                    "Para qual mesa devolver → **endereço de retorno** (no MIPS, o registrador `\$ra`)",
                    "Rascunhos do cozinheiro → **variáveis locais** e **temporários**",
                    "Pilha de pratos na bancada → **pilha de execução**: só o topo trabalha",
                    "Molho que precisa de outro molho da MESMA receita → **recursão**: vários pratos iguais empilhados",
                ),
            ),
            Say(PILHA, "Prato novo... vai em cima. Prato pronto... sai de cima. Eu nunca... tiro do meio."),
            Code(P5_FAT_CODE, "O fatorial recursivo: uma receita que pede a si mesma"),
            Predict(
                "Rodando fat(3), quantos pratos (registros de ativação) ficam empilhados no momento MAIS alto, contando o de main?",
                listOf("2", "3", "4", "6"),
                2,
                "main → fat(3) → fat(2) → fat(1): quatro quadros ao mesmo tempo. Só quando fat(1) devolve 1 a pilha começa a baixar. Se você chutou 3, esqueceu do prato do main — erro supercomum!",
            ),
            Visual(VK.CALL_STACK, input = "fat3", caption = "Empilhe e desempilhe: cada fat(n) tem o SEU próprio n no bilhete."),
            Callout(NO_DUMB_QUESTIONS, "'Se todos os quadros são da mesma função fat, por que o n não se mistura?' Porque cada chamada ganha um prato NOVO, com o seu próprio n. O n=3 fica guardadinho embaixo enquanto o n=1 trabalha no topo."),
            Text("E o resto da cozinha? O **cardápio** pregado na parede é o **código**; as **placas fixas** (nome da casa, horário) são a **área estática** (variáveis globais); o **depósito**, de onde se pega e devolve caixa a qualquer hora, é o **heap** (`new`, `malloc`)."),
            Reveal("Por que o depósito (heap) existe, se a pilha já funciona tão bem?", "Porque o prato da pilha é jogado fora quando a função termina. Se você precisa de algo que viva DEPOIS do retorno — um objeto criado com `new`, uma lista que cresce — ele vai para o heap."),
            Callout(REAL_WORLD, "Recursão sem caso base = pratos empilhados até o teto. Em Java isso é o famoso `StackOverflowError`; em C, geralmente um *segmentation fault*. É o prato batendo no lustre."),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.VISUAL, "A pilha de chamadas, quadro a quadro",
        listOf(
            Say(PROF, "Hoje a gente assiste à memória durante a execução. Primeiro, um programa sem recursão: main chama f, que chama g, que chama h."),
            Code(P5_CHAIN1_CODE, "f chama g, g chama h... e f ainda chama h de novo"),
            Visual(VK.CALL_STACK, input = "chain1", caption = "Avance passo a passo: cada chamada EMPILHA um quadro; cada return DESEMPILHA."),
            Steps(
                "O que você acabou de ver",
                listOf(
                    "main chama f(1): pilha [main, f]",
                    "f chama g(1): [main, f, g]",
                    "g chama h(2): [main, f, g, h] — o ponto mais alto, 4 quadros",
                    "h devolve 3 e sai; g devolve 3 e sai: [main, f]",
                    "f chama h(1) DE NOVO: [main, f, h] — o mesmo h, um quadro novinho",
                    "h devolve 2; f devolve 3 + 2 = 5; main devolve 5",
                ),
            ),
            Say(BYTE, "O h apareceu duas vezes, em alturas diferentes da pilha!"),
            Say(PROF, "Exato. O quadro depende de QUEM chamou e QUANDO, não só do nome da função. Por isso cada quadro guarda o seu próprio endereço de retorno."),
            Predict(
                "Agora fat(4). Qual a altura máxima da pilha, contando main?",
                listOf("4", "5", "8"),
                1,
                "main + fat(4) + fat(3) + fat(2) + fat(1) = 5. Regra prática: profundidade da recursão + 1 (o main).",
            ),
            Visual(VK.CALL_STACK, input = "fat4", caption = "Confira: a pilha sobe até fat(1) e depois desce devolvendo 1, 2, 6, 24."),
            Text("Agora uma recursão que **ramifica**: fib(3) chama fib(2) e fib(1). O desenho de TODAS as chamadas forma a **árvore de ativação**."),
            Code(
                """
                        main
                         |
                       fib(3)
                      /      \
                  fib(2)    fib(1)
                  /    \
              fib(1)  fib(0)
                """.trimIndent(),
                "Árvore de ativação de fib(3): 6 ativações no total (com main)",
            ),
            Visual(VK.CALL_STACK, input = "fib3", caption = "Repare: em cada instante, a pilha é o CAMINHO da raiz até o nó que está executando."),
            Predict(
                "Em fib(3), no instante em que fib(0) executa, quem está na pilha?",
                listOf("main, fib(3), fib(2), fib(1), fib(0)", "main, fib(3), fib(2), fib(0)", "Só fib(0)"),
                1,
                "O fib(1) da esquerda já terminou e saiu. A pilha guarda só os ancestrais vivos: main → fib(3) → fib(2) → fib(0). Altura máxima de fib3: 4.",
            ),
            Callout(BRAIN_POWER, "A árvore de ativação conta a história inteira; a pilha mostra só o 'agora'. Percorrer a árvore em profundidade (desce = chama, sobe = retorna) é exatamente o que a pilha faz."),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.WORKED, "fat(3) na mão — e em MIPS",
        listOf(
            Say(PROF, "Exercício clássico: rode fat(3) à mão, mostrando a pilha de registros de ativação e os valores devolvidos."),
            Code(P5_FAT_CODE, "O programa"),
            Steps(
                "Resolvido, chamada por chamada",
                listOf(
                    "1. main chama fat(3): quadro {n=3, volta para: main, linha 6}",
                    "2. 3 > 1, então fat(3) precisa de fat(2): quadro {n=2, volta para: fat, linha 3}",
                    "3. fat(2) chama fat(1): quadro {n=1, volta para: fat, linha 3}. Pilha: main, fat(3), fat(2), fat(1) — 4 quadros",
                    "4. fat(1) cai no caso base, devolve 1 e o seu quadro é desempilhado",
                    "5. fat(2) retoma na linha 3: 2 * 1 = 2; devolve 2 e sai",
                    "6. fat(3) retoma: 3 * 2 = 6; devolve 6 e sai",
                    "7. main recebe 6",
                ),
            ),
            Text("Agora o mesmo fatorial em MIPS, do jeito que o MARS roda. O quadro de cada chamada tem 2 palavras (8 bytes): o `\$ra` e o `n`."),
            Code(P5_FAT_ASM, "fat.asm — prólogo abre o quadro, epílogo fecha"),
            Steps(
                "Quem faz o quê na chamada",
                listOf(
                    "**Chamador:** põe o argumento em `\$a0` e executa `jal fat` (que guarda o endereço de volta em `\$ra`)",
                    "**Chamado, na entrada (prólogo):** `addi \$sp, \$sp, -8` abre o quadro; salva `\$ra` e `\$a0` nele",
                    "**Chamado, na saída (epílogo):** põe o resultado em `\$v0`, restaura `\$ra`, `addi \$sp, \$sp, 8` fecha o quadro e `jr \$ra` volta",
                    "**Chamador, depois:** usa o valor que chegou em `\$v0`",
                ),
            ),
            Say(BYTE, "Deixa eu tentar um sozinho? Acho que eu consigo."),
            Code(P5_CHAIN2_CODE, "Sua vez: main chama f(2)"),
            Reveal("Qual a pilha no ponto mais alto?", "[main, f(c=2), g(b=2), h(a=4)] — 4 quadros. h recebeu 4 porque g passou b * 2."),
            Reveal("Que valor cada chamada devolve?", "h(4) = 5; g(2) = 5; depois f chama h(2) = 3; f devolve 5 + 3 = 8; main devolve 8."),
            Reveal("Para onde volta o SEGUNDO h, o h(2)?", "Para f, na linha 8 (a soma). Esse endereço de retorno está no quadro dele — não é o do primeiro h, que voltava para g, linha 5."),
            Visual(VK.CALL_STACK, input = "chain2", caption = "Confira as suas três respostas."),
            Callout(WATCH_OUT, "Esqueceu de salvar `\$ra` antes do `jal` interno? A chamada recursiva sobrescreve `\$ra`, e o `jr \$ra` final volta para o lugar errado — o programa entra em laço. Função que chama outra SEMPRE salva `\$ra` na pilha."),
            Callout(EXAM_TIP, "Na prova, desenhe a pilha como uma coluna de caixas, uma por chamada, com os parâmetros e o 'volta para'. Risque (não apague) as caixas que saem: o professor quer ver a ordem."),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.SOCRATIC, "Por que uma PILHA?",
        listOf(
            Say(PROF, "Vamos reinventar o ambiente de execução só com perguntas. Responda de cabeça antes de tocar — errar aqui é parte do método."),
            Reveal("Ideia mais simples: cada variável de cada função ganha um endereço fixo (alocação estática). O que dá errado com fat(3)?", "Só existe UM n para a função fat. Quando fat(2) escreve n=2, destrói o n=3 que fat(3) ainda vai usar na volta. Alocação estática não suporta recursão (o FORTRAN antigo nem permitia recursão por isso)."),
            Reveal("Então cada CHAMADA precisa da sua própria cópia de n. Em que ordem essas cópias nascem e morrem?", "A última chamada a começar é a primeira a terminar (LIFO): fat(1) nasce por último e morre primeiro. Esse é exatamente o comportamento de uma pilha."),
            Say(BYTE, "Tá, mas além do n, o que mais o quadro precisa guardar?"),
            Reveal("Quando fat(1) termina, como a máquina sabe para onde voltar?", "Ela guardou o **endereço de retorno** na hora da chamada. No MIPS, `jal` coloca esse endereço em `\$ra`; como a próxima chamada sobrescreveria `\$ra`, ele é salvo no quadro."),
            Reveal("E como devolver o resultado e voltar a usar o quadro de quem chamou?", "O resultado vai num lugar combinado (no MIPS, `\$v0`). Para reencontrar o quadro anterior, guarda-se o **vínculo de controle** (link dinâmico), um ponteiro para o quadro do chamador — ou, no MIPS simples, basta desfazer o `\$sp`."),
            Predict(
                "Quem deve ABRIR o espaço das variáveis LOCAIS de uma função?",
                listOf(
                    "O chamador, porque ele sabe quantos parâmetros passa",
                    "O chamado, porque só ele sabe quantas locais e temporários tem",
                    "O sistema operacional",
                ),
                1,
                "O tamanho das locais é 'segredo' do chamado (o chamador pode até ter sido compilado antes). Regra geral: o chamador cuida do que conhece (parâmetros, endereço de retorno); o chamado cuida do resto.",
            ),
            Reveal("Uma função cria uma lista e a devolve para quem chamou. A lista pode morar no quadro dela?", "Não! O quadro é desempilhado no retorno, e quem chamou ficaria com um ponteiro para lixo (ponteiro pendente). Coisas que precisam sobreviver à chamada vão para o **heap**."),
            Reveal("Recursão infinita: o que acontece com a pilha?", "Cada chamada empilha um quadro e nenhuma sai. A pilha cresce até invadir uma área proibida: **estouro de pilha** (stack overflow)."),
            Say(BYTE, "E passar parâmetro por referência? Muda alguma coisa no quadro?"),
            Reveal("Em swap(x, y) por VALOR, o que vai para o quadro? E por REFERÊNCIA?", "Por valor: CÓPIAS de x e y — trocar as cópias não muda nada lá fora. Por referência: os ENDEREÇOS de x e y — o chamado escreve direto nas variáveis do chamador, e a troca acontece de verdade."),
            Visual(VK.CALL_STACK, input = "fib3", caption = "Teste: antes de cada passo, preveja se a pilha sobe ou desce."),
            Callout(BRAIN_POWER, "Repare no que você acabou de deduzir: pilha (por causa do LIFO), endereço de retorno, vínculo de controle, heap e passagem de parâmetros. É isso que o capítulo 7 do Livro do Dragão formaliza. Você não é 'burro' — você acabou de reinventar um ambiente de execução."),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.FORMAL, "Organização da memória e registros de ativação",
        listOf(
            Text("**Ambiente de execução** (*run-time environment*): o conjunto de estruturas que o código gerado usa para rodar — organização da memória, registros de ativação, sequências de chamada e passagem de parâmetros."),
            Code(
                p5Asm(
                    """
                    endereços altos
                    +----------------------+
                    |        PILHA         |  topo em §sp; cresce para BAIXO
                    |          v           |
                    |                      |
                    |          ^           |
                    |         HEAP         |  cresce para CIMA (new, malloc)
                    +----------------------+
                    |  ESTÁTICO  (.data)   |  globais e constantes; tamanho fixo
                    +----------------------+
                    |  CÓDIGO    (.text)   |  as instruções do programa
                    +----------------------+
                    endereços baixos
                    """,
                ),
                "Subdivisão típica da memória em tempo de execução (Livro do Dragão, seção 7.1)",
            ),
            Code(
                """
                +--------------------------------+
                | parâmetros reais               |  preenchidos pelo CHAMADOR
                | valor de retorno               |
                | vínculo de controle (dinâmico) |  -> quadro do chamador
                | vínculo de acesso (estático)   |  -> quadro do escopo envolvente
                | estado salvo da máquina        |  endereço de retorno, registradores
                | dados locais                   |  preenchidos pelo CHAMADO
                | temporários                    |
                +--------------------------------+
                """.trimIndent(),
                "Registro de ativação (quadro): campos típicos (seção 7.2.2)",
            ),
            Steps(
                "Sequência de chamada e de retorno",
                listOf(
                    "**Chamador:** avalia os parâmetros reais e os coloca onde o chamado espera (no quadro, ou em `\$a0`–`\$a3`)",
                    "**Chamador:** registra o endereço de retorno e o vínculo de controle; desvia para o chamado (`jal`)",
                    "**Chamado (prólogo):** aloca o quadro (`\$sp` -= tamanho), salva registradores (incluindo `\$ra`) e inicializa as locais",
                    "**Chamado (retorno):** põe o valor de retorno no lugar combinado (`\$v0`), restaura registradores e `\$sp`, desvia para o endereço de retorno (`jr \$ra`)",
                    "**Chamador:** recupera o valor devolvido e continua",
                ),
            ),
            Text("**Árvore de ativação:** cada nó é uma ativação; os filhos de um nó são as ativações que ele chamou, da esquerda para a direita, na ordem das chamadas. **Propriedade:** a pilha de controle, num dado instante, contém exatamente o caminho da raiz até o nó em execução."),
            Steps(
                "Três estratégias de alocação",
                listOf(
                    "**Estática:** endereço fixo em tempo de compilação; não permite recursão nem tamanho dinâmico (globais; FORTRAN 77)",
                    "**Em pilha:** um quadro por ativação, LIFO; permite recursão; o quadro morre no retorno (locais em C e Java)",
                    "**Em heap:** blocos com tempo de vida arbitrário; liberação manual (`free`) ou por coleta de lixo",
                ),
            ),
            Code(
                p5Asm(
                    """
                    §a0–§a3   argumentos           §v0–§v1  valores de retorno
                    §ra       endereço de retorno  §sp      topo da pilha
                    §fp       base do quadro atual (opcional)
                    §t0–§t9   temporários: o CHAMADOR salva se precisar deles depois
                    §s0–§s7   salvos: o CHAMADO salva antes de usar

                    jal f     # §ra := PC + 4;  PC := f
                    jr  §ra   # PC := §ra
                    """,
                ),
                "Convenção de chamada do MIPS",
            ),
            Predict(
                "Qual vínculo aponta para o quadro de QUEM CHAMOU a função?",
                listOf("Vínculo de acesso (estático)", "Vínculo de controle (dinâmico)", "Endereço de retorno"),
                1,
                "Controle/dinâmico = cadeia de CHAMADAS. Acesso/estático = cadeia de ANINHAMENTO no texto (útil em linguagens com funções aninhadas, como Pascal). O endereço de retorno aponta para uma INSTRUÇÃO, não para um quadro.",
            ),
            Visual(VK.CALL_STACK, input = "fat3", caption = "A definição em ação: 4 ativações simultâneas no pico (main + 3 de fat)."),
            Callout(EXAM_TIP, "Questão clássica: 'desenhe o registro de ativação e diga quem preenche cada campo'. Parâmetros, valor de retorno (espaço) e endereço de retorno: o chamador. Locais, temporários e registradores salvos: o chamado. Liste de cima para baixo nessa ordem."),
            Callout(WATCH_OUT, "Endereço de retorno ≠ vínculo de controle. O primeiro diz para qual INSTRUÇÃO voltar; o segundo, qual QUADRO volta a ser o atual."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.RUNTIME, Style.MNEMONIC, "O prédio da memória e o café da manhã do quadro",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Forja do Código, penúltima sala. Um prédio de quatro andares; do telhado, uma pilha de caixas desce sozinha."),
            Callout(MNEMONIC, "**O prédio, de baixo para cima: C-E-H-P (leia 'CEP', o endereço de tudo).** **C**ódigo no térreo, **E**stático no 1º andar, **H**eap subindo do 2º, **P**ilha descendo do telhado."),
            Callout(MNEMONIC, "**'Heap sobe, pilha desce; se os dois se encontram, o prédio cai.'** É o estouro de memória."),
            Callout(MNEMONIC, "**Controle Chama, Acesso Aninha.** Vínculo de **c**ontrole → quem me **c**hamou (cadeia dinâmica). Vínculo de **a**cesso → onde estou **a**ninhado no texto (cadeia estática)."),
            Callout(MNEMONIC, "**O café da manhã do quadro: Pão, Vitamina, Café, Açúcar, Esfirra, Leite, Torrada** → **P**arâmetros, **V**alor de retorno, vínculo de **C**ontrole, vínculo de **A**cesso, **E**stado salvo (endereço de retorno), **L**ocais, **T**emporários."),
            Callout(MNEMONIC, "**jal = Jump And Link**: pula e deixa um bilhete de volta em `\$ra` (**r**eturn **a**ddress). **jr \$ra = Jump Register**: volta seguindo o bilhete."),
            Say(BYTE, "E pra lembrar quem faz o quê na chamada? Eu sempre troco."),
            Say(PROF, "Recepção de hotel! O **hóspede** (chamador) chega com a bagagem e diz quando volta para casa: parâmetros e endereço de retorno. O **hotel** (chamado) arruma o quarto e o frigobar: locais e registradores salvos. Na saída, o hotel entrega a conta (valor de retorno) e libera o quarto."),
            Predict(
                "Em main → f → g → h (h chamado por g), pelo gancho 'Controle Chama', o vínculo de controle de h aponta para o quadro de...",
                listOf("main", "f", "g", "h"),
                2,
                "Controle = quem CHAMOU. Quem chamou h foi g. (E quando f chama h direto, o vínculo de controle desse OUTRO quadro de h aponta para f.)",
            ),
            Visual(VK.CALL_STACK, input = "chain1", caption = "Suba e desça o prédio: cada andar da pilha é um quadro."),
            Reveal("Recite os 7 campos do quadro pelo café da manhã.", "Pão, Vitamina, Café, Açúcar, Esfirra, Leite, Torrada → parâmetros, valor de retorno, vínculo de controle, vínculo de acesso, estado salvo (endereço de retorno), locais, temporários."),
            Callout(FIRESIDE, "Pilha: 'Eu sou rápida e organizada: entrou, saiu, tudo limpinho.' Heap: 'E eu sou flexível: guardo o que você quiser pelo tempo que quiser.' Pilha: 'E esquece de devolver...' Heap: 'Por isso inventaram o coletor de lixo!'"),
        ),
    ),
)
