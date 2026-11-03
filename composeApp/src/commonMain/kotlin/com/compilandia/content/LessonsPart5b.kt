package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.content.Speaker.*
import com.compilandia.content.VisualKind as VK
import com.compilandia.learning.Skill
import com.compilandia.learning.Style

// Parte 5b do conteúdo: CODEGEN_MIPS — geração de código MIPS (Livro do Dragão, cap. 8; estilo do trabalho prático no MARS).
// Registradores MIPS em strings cruas são escritos com '§' e convertidos por p5Asm (ver LessonsPart5.kt).

/** x = a + b * 2 no estilo do gerador do jogo (Mips.fromTac), embrulhado num programa completo para o MARS. */
private val P5_ASM_EXPR: String = p5Asm(
    """
    # x = a + b * 2;  print(x);
    # TAC:  t1 = b * 2 ;  t2 = a + t1 ;  x = t2
            .data
    a:      .word 4
    b:      .word 2
    t1:     .word 0            # temporários também moram na memória
    t2:     .word 0
    x:      .word 0
            .text
    main:
            lw   §t1, b        # t1 = b * 2
            li   §t2, 2
            mul  §t3, §t1, §t2
            sw   §t3, t1
            lw   §t1, a        # t2 = a + t1
            lw   §t2, t1
            add  §t3, §t1, §t2
            sw   §t3, t2
            lw   §t0, t2       # x = t2
            sw   §t0, x
            li   §v0, 1        # print(x): syscall 1 = inteiro
            lw   §a0, x
            syscall
            li   §v0, 10       # syscall 10 = encerrar
            syscall
    """,
)

private val P5_ASM_IF_SHORT: String = p5Asm(
    """
    # if (a > b) x = a; else x = b;
            lw   §t1, a
            lw   §t2, b
            sgt  §t3, §t1, §t2     # §t3 = 1 se a > b, senão 0
            beq  §t3, §zero, L0    # condição falsa? pula para o else
            sw   §t1, x            # then:  x = a
            j    L1                # pula por cima do else
    L0:     sw   §t2, x            # else:  x = b
    L1:                            # as duas pistas se encontram aqui
    """,
)

private val P5_ASM_IF_FULL: String = p5Asm(
    """
    # if (a > b) x = a - b; else x = b - a;   print("x = ", x);
            .data
    a:      .word 4
    b:      .word 2
    x:      .word 0
    msg:    .asciiz "x = "
            .text
    main:
            lw   §t1, a
            lw   §t2, b
            sgt  §t3, §t1, §t2     # cond = (a > b)
            beq  §t3, §zero, L0    # falso → else
            sub  §t3, §t1, §t2     # then: x = a - b
            sw   §t3, x
            j    L1
    L0:     sub  §t3, §t2, §t1     # else: x = b - a
            sw   §t3, x
    L1:     li   §v0, 4            # imprime o texto
            la   §a0, msg
            syscall
            li   §v0, 1            # imprime o inteiro x
            lw   §a0, x
            syscall
            li   §v0, 10           # encerra
            syscall
    """,
)

private val P5_ASM_WHILE_FADED: String = p5Asm(
    """
    # s = 0; i = 1; while (i <= 3) { s = s + i; i = i + 1; }  print(s);
            li   §t0, 0
            sw   §t0, s
            li   §t0, 1
            sw   §t0, i
    L0:     lw   §t1, i            # teste do laço
            li   §t2, 3
            sgt  §t3, §t1, §t2     # §t3 = 1 quando i > 3 (hora de sair)
            ____ §t3, §zero, L1    # (A)
            lw   §t1, s            # s = s + i
            lw   §t2, i
            add  §t3, §t1, §t2
            sw   §t3, ____         # (B)
            lw   §t1, i            # i = i + 1
            addi §t3, §t1, 1
            sw   §t3, i
            ____ ____              # (C)
    L1:     ...                    # print(s)
    """,
)

internal val p5MipsLessons: List<Lesson> = listOf(
    // ───────────────────────────── STORY ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.STORY, "A bancada pequena e a despensa enorme",
        listOf(
            Say(NARRADOR, "Última estação da Forja do Código. O TAC chega em fichinhas; do outro lado da esteira sai um arquivo .asm que o MARS vai rodar."),
            Say(BYTE, "MIPS... Eu fico com medo só de ver tanto cifrão."),
            Say(PROF, "Fica tranquilo: é uma cozinha. O processador é um cozinheiro com uma **bancada pequenininha** e uma **despensa enorme** no fim do corredor."),
            Text("Na bancada cabem só 32 potinhos com etiqueta (`\$t0`, `\$t1`, ...). Ele **só cozinha o que está na bancada**: para usar a farinha, vai à despensa buscar (**lw**) e, quando termina, leva o resultado de volta (**sw**)."),
            Steps(
                "O mapa da analogia",
                listOf(
                    "Potinhos da bancada → **registradores** (`\$t0`–`\$t9`, `\$s0`–`\$s7`, ...): rápidos e poucos",
                    "Prateleiras da despensa → **memória**, declarada na seção **.data** (`x: .word 0`)",
                    "Buscar na despensa → **lw** (*load word*); guardar de volta → **sw** (*store word*)",
                    "'Use 2 ovos' (não precisa buscar nada) → **li** (*load immediate*: uma constante)",
                    "Misturar dois potinhos num terceiro → **add / sub / mul / div**",
                    "O caderno de passos → seção **.text**, começando no rótulo **main:**",
                    "Post-it 'se faltou ovo, pule para L0' → **desvios** (beq, bne, j) e **rótulos**",
                ),
            ),
            Code(
                p5Asm(
                    """
                    # x = a + b * 2     (TAC: t1 = b * 2 ; t2 = a + t1 ; x = t2)
                    lw   §t1, b         # busca b na despensa
                    li   §t2, 2         # pega a constante 2
                    mul  §t3, §t1, §t2  # mistura
                    sw   §t3, t1        # guarda na prateleira t1
                    lw   §t1, a
                    lw   §t2, t1
                    add  §t3, §t1, §t2
                    sw   §t3, t2
                    lw   §t0, t2
                    sw   §t0, x
                    """,
                ),
                "A receita traduzida, uma ficha TAC por bloco",
            ),
            Predict(
                "Cada instrução TAC do tipo t = a op b virou quantas instruções MIPS no código acima?",
                listOf("1", "2", "4", "10"),
                2,
                "Busca (lw ou li), busca (lw ou li), mistura (op), guarda (sw): quatro. A cópia final x = t2 vira só lw + sw.",
            ),
            Visual(VK.MIPS, input = "a + b * 2", caption = "Rode o 'MARS de bolso' (a=4, b=2) e acompanhe a bancada e a despensa mudando."),
            Callout(WATCH_OUT, "`t1` (sem cifrão) é uma **prateleira** da despensa: uma variável na .data que o gerador criou para o temporário. `\$t1` (com cifrão) é um **potinho** da bancada. Nomes parecidos, lugares diferentes! No seu trabalho, chamar os temporários de `_t1` evita confusão."),
            Say(BYTE, "Mas ir à despensa toda hora não é lento?"),
            Say(PROF, "Muito! Um cozinheiro esperto deixa na bancada o que vai usar de novo. Isso tem nome: **alocação de registradores**. Primeiro a gente faz funcionar; depois deixa rápido."),
            Reveal("Na analogia, o que é uma 'syscall'?", "É tocar a campainha do salão: o cozinheiro pede ao gerente (o sistema / o MARS) algo que ele não faz sozinho, como mostrar o prato ao cliente (imprimir) ou fechar a cozinha (encerrar). O pedido vai em `\$v0`: 1 = imprimir inteiro, 4 = imprimir texto, 10 = encerrar."),
            Callout(REAL_WORLD, "MIPS é a arquitetura didática do livro de Patterson & Hennessy, e o PlayStation 1 e o Nintendo 64 rodavam processadores MIPS. No trabalho prático você usa o MARS: escreve o .asm, monta (F3) e roda (F5)."),
        ),
    ),

    // ───────────────────────────── VISUAL ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.VISUAL, "Da árvore ao MARS, instrução por instrução",
        listOf(
            Say(PROF, "Vamos ver a mesma expressão em três línguas: árvore, TAC e MIPS."),
            Visual(VK.ATTR_TREE, input = "(a + b) * c", caption = "A árvore: + embaixo, * na raiz."),
            Visual(VK.TAC, input = "(a + b) * c", caption = "Pós-ordem: t1 = a + b; t2 = t1 * c."),
            Visual(VK.MIPS, input = "(a + b) * c", caption = "Cada linha TAC vira um bloquinho. Avance e veja \$t1, \$t2, \$t3 e a memória."),
            Steps(
                "Lendo o que aconteceu (a=4, b=2, c=3)",
                listOf(
                    "lw \$t1, a → \$t1 = 4;  lw \$t2, b → \$t2 = 2",
                    "add \$t3, \$t1, \$t2 → \$t3 = 6;  sw \$t3, t1 → memória t1 = 6",
                    "lw \$t1, t1 → 6;  lw \$t2, c → 3;  mul → \$t3 = 18;  sw → memória t2 = 18",
                    "Cópia final: lw \$t0, t2;  sw \$t0, r → o resultado r = 18",
                ),
            ),
            Predict(
                "Para a * b + c * d (a=4, b=2, c=3, d=5), qual valor fica no resultado r?",
                listOf("23", "70", "34"),
                0,
                "t1 = 4 * 2 = 8; t2 = 3 * 5 = 15; t3 = 8 + 15 = 23. A precedência já veio resolvida da árvore: os dois * antes do +.",
            ),
            Visual(VK.MIPS, input = "a * b + c * d", caption = "Confira: três blocos de operação e uma cópia final."),
            Text("Agora, desvios. Um `if` vira **comparação + desvio + rótulos**. Veja o máximo entre a e b:"),
            Code(P5_ASM_IF_SHORT, "if/else em MIPS: a bifurcação e a ponte"),
            Steps(
                "O caminho da execução com a=4, b=2",
                listOf(
                    "sgt: 4 > 2 → \$t3 = 1",
                    "beq \$t3, \$zero, L0: 1 ≠ 0, então NÃO pula",
                    "sw \$t1, x → x = 4 (o then)",
                    "j L1 → salta por cima do else",
                    "Com a=1, b=2: \$t3 = 0, o beq pula para L0 e x = b",
                ),
            ),
            Predict(
                "Se você esquecer o `j L1`, o que acontece com a=4, b=2?",
                listOf("Nada muda", "Executa o then e DEPOIS cai no else: x termina valendo b", "Erro de montagem no MARS"),
                1,
                "O processador não conhece chaves: ele segue linha a linha. Sem o j, ele 'escorrega' do then para dentro de L0 e sobrescreve x com 2. O MARS monta numa boa — o erro é de lógica.",
            ),
            Visual(VK.MIPS, input = "(a + b) / 2", caption = "Bônus: média inteira. O div de três registradores é pseudoinstrução no MARS (por baixo: div + mflo)."),
            Callout(BRAIN_POWER, "Repare que o gerador ingênuo faz `sw \$t3, t1` e logo depois `lw \$t1, t1`: guarda e busca a mesma coisa. Dá para cortar isso? (Spoiler: é a primeira coisa que a alocação de registradores elimina.)"),
        ),
    ),

    // ───────────────────────────── WORKED ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.WORKED, "Programas .asm completos: expressão, if/else e while",
        listOf(
            Say(PROF, "Exercício 1, com cara de trabalho prático: gere o programa MIPS completo para `x = a + b * 2; print(x);`, com a = 4 e b = 2."),
            Steps(
                "A receita da tradução",
                listOf(
                    "1. TAC: t1 = b * 2;  t2 = a + t1;  x = t2",
                    "2. .data: uma `.word` para cada variável E cada temporário",
                    "3. .text: cada instrução TAC vira lw/li, lw/li, operação, sw",
                    "4. print: `li \$v0, 1`, `lw \$a0, x`, `syscall`",
                    "5. fim: `li \$v0, 10`, `syscall`",
                ),
            ),
            Code(P5_ASM_EXPR, "expr.asm — rode no MARS: o console mostra 8"),
            Visual(VK.MIPS, input = "a + b * 2", caption = "Confira no simulador: no fim, o resultado (aqui chamado r) vale 8."),
            Say(PROF, "Exercício 2: if/else completo, com texto na saída. `if (a > b) x = a - b; else x = b - a;` e depois imprime."),
            Code(P5_ASM_IF_FULL, "dist.asm — saída no MARS: x = 2"),
            Callout(WATCH_OUT, "Cada if do programa precisa de rótulos NOVOS (L0, L1, depois L2, L3...). No gerador, use um contador: `novoRotulo()` devolve L0, L1, L2... Rótulo repetido = erro de montagem no MARS."),
            Say(BYTE, "Deixa o while comigo! Eu consigo."),
            Code(P5_ASM_WHILE_FADED, "Complete as lacunas (A), (B) e (C)"),
            Reveal("(A): qual desvio?", "`bne \$t3, \$zero, L1`: se a condição de SAÍDA (i > 3) é verdadeira (≠ 0), pula para fora do laço. (Equivalente: calcular i <= 3 com `sle` e usar `beq \$t3, \$zero, L1`.)"),
            Reveal("(B) e (C)?", "(B) `s`: o resultado da soma volta para s. (C) `j L0`: o laço volta para o TESTE, não para o corpo. Esse salto para trás é o que faz o while repetir."),
            Reveal("Quanto vale s na hora do print? Quantas vezes o teste em L0 roda?", "s = 1 + 2 + 3 = 6. O teste roda 4 vezes: com i = 1, 2, 3 (entra) e i = 4 (sai)."),
            Reveal("Agora sem ajuda: o MIPS (estilo do gerador) de x = (a + b) * c.", "lw \$t1, a / lw \$t2, b / add \$t3, \$t1, \$t2 / sw \$t3, t1 / lw \$t1, t1 / lw \$t2, c / mul \$t3, \$t1, \$t2 / sw \$t3, t2 / lw \$t0, t2 / sw \$t0, x. Com a=4, b=2, c=3: x = 18."),
            Visual(VK.MIPS, input = "(a + b) * c", caption = "Confira o último exercício."),
            Callout(EXAM_TIP, "Para testar no MARS: Assemble (F3), depois Go (F5), e olhe o painel Run I/O. Deu diferente? Use Step (F7) e acompanhe a aba Registers e o Data Segment, comparando com o que você calculou à mão."),
        ),
    ),

    // ───────────────────────────── SOCRATIC ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.SOCRATIC, "Por que tanto lw e sw?",
        listOf(
            Say(PROF, "Vou só fazer perguntas. Você vai descobrir sozinho como se gera código MIPS."),
            Reveal("No MIPS, `add` só aceita registradores. Então como somar duas variáveis que estão na memória?", "Primeiro trazê-las para registradores (lw), somar, e devolver o resultado à memória (sw). O MIPS é uma arquitetura **load/store**: só lw/sw tocam a memória; todo o resto trabalha em registradores."),
            Reveal("E se um operando é uma constante, como em b * 2?", "Não há o que buscar na memória: usa-se `li \$t2, 2` (carrega imediato). Para somar uma constante existe até atalho: `addi \$t3, \$t1, 2`."),
            Reveal("Onde as variáveis e os temporários 'moram' no estilo do trabalho prático?", "Na seção .data, uma `.word` para cada um: `x: .word 0`. É simples e sempre funciona — o preço é acessar a memória o tempo todo."),
            Say(BYTE, "Mas e o if? MIPS não tem chaves..."),
            Reveal("Como fazer 'se a > b, faça X; senão, faça Y' tendo só 'pule para tal linha'?", "Calcule a condição num registrador (sgt/slt deixam 1 ou 0). Se ela for 0, pule para o rótulo do else (`beq \$t3, \$zero, L0`). No fim do then, um `j L1` pula o else."),
            Reveal("E o while? Que desvio faz ele repetir?", "Um `j` para TRÁS, de volta ao rótulo do teste. O teste fica no começo: se falhar, um desvio para FRENTE sai do laço."),
            Predict(
                "Para que serve o registrador `\$zero`?",
                listOf(
                    "Guarda o último resultado que deu zero",
                    "Vale sempre 0 — ótimo para comparar com 'falso' (beq \$t3, \$zero, L0) e para copiar",
                    "Conta quantas instruções já rodaram",
                ),
                1,
                "Escrever nele não adianta: ele é fixo em 0. `beq \$t3, \$zero, L0` se lê 'se \$t3 é falso, pule'. E `move \$t0, \$t1` é, por baixo, uma soma com `\$zero`.",
            ),
            Reveal("Como o programa imprime algo, se o processador nem sabe o que é tela?", "Pede ao sistema: põe o código do serviço em `\$v0` (1 = inteiro, 4 = string, 10 = sair), o argumento em `\$a0` (o número, ou o endereço da string, obtido com `la`) e executa `syscall`."),
            Say(BYTE, "E dá pra gastar menos lw e sw?"),
            Reveal("Em x = a + b * 2, o gerador ingênuo faz 7 acessos à memória. Quantos são realmente necessários?", "Três: buscar a, buscar b e guardar x. Os temporários podem viver em registradores: `lw \$t0, a / lw \$t1, b / li \$t2, 2 / mul \$t1, \$t1, \$t2 / add \$t0, \$t0, \$t1 / sw \$t0, x`. Decidir quem fica em qual registrador é a **alocação de registradores**."),
            Visual(VK.MIPS, input = "a + b * 2", caption = "Conte os lw e sw do gerador ingênuo e compare com a versão de 3 acessos."),
            Callout(BRAIN_POWER, "Por que não pôr TUDO em registradores? Porque são só 32 (e vários reservados). Quando faltam, alguém volta para a memória — o famoso **spill**. Escolher bem quem fica é um problema difícil (vira coloração de grafos!)."),
        ),
    ),

    // ───────────────────────────── FORMAL ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.FORMAL, "Esquemas de tradução TAC → MIPS",
        listOf(
            Text("**Gerador de código:** fase final, que mapeia a representação intermediária (TAC) em instruções da máquina-alvo. Problemas centrais: **seleção de instruções**, **alocação de registradores** e **ordem de avaliação** (Livro do Dragão, cap. 8)."),
            Code(
                p5Asm(
                    """
                    §zero         constante 0
                    §v0–§v1       valores de retorno / código da syscall
                    §a0–§a3       argumentos
                    §t0–§t9       temporários (não preservados na chamada)
                    §s0–§s7       salvos (preservados na chamada)
                    §sp §fp §ra   topo da pilha, base do quadro, endereço de retorno
                    """,
                ),
                "Registradores MIPS (32 bits) usados no trabalho",
            ),
            Code(
                p5Asm(
                    """
                    lw   rd, rot       rd := Mem[rot]
                    sw   rs, rot       Mem[rot] := rs
                    li   rd, k         rd := k                    (pseudo)
                    la   rd, rot       rd := endereço de rot      (pseudo)
                    add  rd, rs, rt    rd := rs + rt     (sub, mul, div análogos)
                    addi rd, rs, k     rd := rs + k
                    slt  rd, rs, rt    rd := (rs < rt) ? 1 : 0    (sgt, seq: pseudo)
                    beq  rs, rt, L     se rs = rt, desvia para L  (bne: se ≠)
                    j    L             desvia para L
                    syscall            serviço em §v0: 1 int, 4 string, 10 sair
                    """,
                ),
                "O subconjunto do MIPS que um gerador simples precisa",
            ),
            Code(
                p5Asm(
                    """
                    gera(x = y op z):
                        carrega("§t1", y);  carrega("§t2", z)
                        emite(opMips(op) + " §t3, §t1, §t2")
                        emite("sw §t3, " + x)

                    gera(x = y):
                        carrega("§t0", y);  emite("sw §t0, " + x)

                    carrega(r, v):
                        se v é constante: emite("li " + r + ", " + v)
                        senão:            emite("lw " + r + ", " + v)
                    """,
                ),
                "Esquema TAC → MIPS no estilo 'tudo na memória' (o do jogo e do trabalho)",
            ),
            Code(
                p5Asm(
                    """
                    if (E) S1 else S2              while (E) S
                        <E em §t3>                 Linicio:
                        beq §t3, §zero, Lelse          <E em §t3>
                        <código de S1>                 beq §t3, §zero, Lfim
                        j Lfim                         <código de S>
                    Lelse:                             j Linicio
                        <código de S2>             Lfim:
                    Lfim:
                    """,
                ),
                "Esquemas para comandos (rótulos novos a cada uso)",
            ),
            Predict(
                "Pelo esquema 'tudo na memória', quantas instruções MIPS gera x = a + b * c?",
                listOf("6", "8", "10"),
                2,
                "Dois operadores × 4 instruções (lw, lw, op, sw) = 8, mais 2 da cópia x = t2 (lw, sw) = 10.",
            ),
            Visual(VK.MIPS, input = "a + b * c", caption = "Conte as instruções: 10."),
            Text("**Custo:** num processador real, acessar a memória é bem mais caro que usar um registrador. O critério clássico de qualidade é **minimizar lw/sw**: manter valores vivos em registradores, reaproveitar resultados e eliminar pares `sw t1` / `lw t1` consecutivos."),
            Callout(EXAM_TIP, "Em 'gere o código MIPS', escreva SEMPRE as duas seções (.data com variáveis e temporários; .text começando em main:), comente cada bloco com a instrução TAC correspondente e termine com `li \$v0, 10` + `syscall`. Sem isso o MARS até para ('dropped off bottom'), mas o professor desconta."),
            Callout(WATCH_OUT, "Em `lw` e `sw` o REGISTRADOR vem primeiro nos dois, mas a direção muda: `lw \$t1, a` leva a → \$t1; `sw \$t1, a` leva \$t1 → a. E o `div` de três operandos é pseudo: o real é `div \$t1, \$t2` seguido de `mflo \$t3` (quociente; o resto fica em hi, lido com `mfhi`)."),
        ),
    ),

    // ───────────────────────────── MNEMONIC ─────────────────────────────
    Lesson(
        Skill.CODEGEN_MIPS, Style.MNEMONIC, "Busca, busca, mistura, guarda",
        listOf(
            Say(NARRADOR, "Palácio da Memória: Forja do Código, última sala — a Montadora MIPS. Na parede, um relógio de cozinha marca quatro tempos."),
            Callout(MNEMONIC, "**'Busca, busca, mistura, guarda.'** Toda instrução TAC x = y op z vira: lw (busca y), lw (busca z), op (mistura), sw (guarda em x). Ritmo de samba: 1, 2, 3, 4."),
            Callout(MNEMONIC, "**Load = Leva para a bancada; Store = Salva na despensa.** Nos dois, o registrador vem PRIMEIRO: `lw \$t1, a` e `sw \$t1, a` — só muda a direção da seta."),
            Callout(MNEMONIC, "**Família de registradores pela inicial:** `\$t` = **t**emporário (pode sujar), `\$s` = **s**alvo (guarde antes de usar), `\$a` = **a**rgumento, `\$v` = **v**alor de retorno, `\$ra` = **r**eturn **a**ddress, `\$sp` = **s**tack **p**ointer."),
            Callout(MNEMONIC, "**beq = Branch if EQual (pula se igual); bne = Branch if Not Equal.** O if testa AO CONTRÁRIO: 'se a condição é falsa (igual a \$zero), pula o then'."),
            Callout(MNEMONIC, "**syscall 1, 4, 10: 'um número, uma fila de letras e dez-liga'.** 1 imprime inteiro; 4 imprime string ('fila' tem 4 letras; use `la \$a0, msg`); 10 encerra (dez... desliga!)."),
            Say(BYTE, "E o if com rótulos? Eu sempre me perco."),
            Say(PROF, "Pense numa estrada com **bifurcação**. O `beq` é a placa 'falso? vire para L0'. O then é a pista reta; o `j L1` no fim dele é a **ponte** que passa por cima do else. L1 é onde as duas pistas se encontram."),
            Predict(
                "No esqueleto do while, para onde vai o `j` do fim do corpo?",
                listOf("Para o rótulo do fim do laço", "Para o rótulo do TESTE, no começo", "Para main"),
                1,
                "while = 'volta e testa de novo'. O único salto para trás é esse j; o desvio para FRENTE (beq/bne) é a porta de saída.",
            ),
            Visual(VK.MIPS, input = "d * (a - c)", caption = "Bata o ritmo: busca, busca, mistura, guarda — duas vezes — e a cópia final. (d=5, a=4, c=3 → 5)"),
            Reveal("Recite: as duas seções de um .asm e o que vai em cada uma.", ".data: as variáveis (`x: .word 0`) e as strings (`msg: .asciiz \"...\"`). .text: as instruções, começando em `main:` e terminando com `li \$v0, 10` + `syscall`."),
            Callout(FIRESIDE, "Memória: 'Eu guardo tudo, sou enorme!' Registrador: 'E eu sou muito mais rápido.' Memória: 'Mas só cabem 32 de vocês...' Alocador de registradores: 'Calma, gente, é para isso que eu existo.'"),
        ),
    ),
)
