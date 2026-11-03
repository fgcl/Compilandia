package com.compilandia.content

import com.compilandia.learning.Skill

// Parte 5 do conteúdo: Forja do Código, back-end — RUNTIME (LessonsPart5a.kt) e CODEGEN_MIPS (LessonsPart5b.kt).

/**
 * Assembly MIPS em strings cruas: escreva '§' no lugar de '$' (senão "$t0" vira template do Kotlin).
 * Ex.: p5Asm("lw §t1, a") == "lw \$t1, a".
 */
internal fun p5Asm(s: String): String = s.trimIndent().replace('§', '$')

private val p5Cards: List<SkillCard> = listOf(
    SkillCard(
        skill = Skill.RUNTIME,
        oneLiner = "Em tempo de execução, cada chamada de função ganha um registro de ativação (quadro) na pilha — parâmetros, valor e endereço de retorno, vínculos, locais e temporários — empilhado na chamada e desempilhado no retorno; o que precisa sobreviver à chamada vai para o heap.",
        hook = "Pilha de pratos: prato novo em cima, prato pronto sai de cima. Controle Chama, Acesso Aninha.",
        feynmanPrompt = "Explique para um amigo de 12 anos como a cozinha do restaurante empilha pratos com bilhetes quando uma receita precisa de outra receita, e por que, rodando fat(3), cada prato lembra o seu próprio n e para onde voltar.",
        keyTerms = listOf("pilha", "ativa", "retorno", "chamad", "local", "recurs"),
        commonMistakes = listOf(
            "Achar que cada função tem UM lugar fixo para as suas variáveis (isso é alocação estática, que não suporta recursão)",
            "Esquecer o quadro de main ao contar a altura máxima da pilha",
            "Confundir vínculo de controle (quem chamou) com vínculo de acesso (onde a função foi declarada no texto)",
            "Confundir endereço de retorno (uma instrução) com vínculo de controle (um quadro)",
            "Não salvar \$ra na pilha antes de um jal dentro de uma função que chama outra",
            "Devolver ponteiro para variável local, que morre junto com o quadro",
        ),
        flashcards = listOf(
            "O que é um registro de ativação?" to "O bloco de memória de UMA chamada: parâmetros, valor e endereço de retorno, vínculos, locais e temporários",
            "Por que pilha e não alocação estática?" to "Recursão: cada chamada precisa da sua cópia das locais, criadas e destruídas em ordem LIFO",
            "O vínculo de controle aponta para..." to "O quadro do chamador (cadeia dinâmica)",
            "O vínculo de acesso aponta para..." to "O quadro do escopo que envolve a função no texto (cadeia estática)",
            "Altura máxima da pilha em fat(3), contando main?" to "4 quadros: main, fat(3), fat(2), fat(1)",
            "O que jal faz?" to "Guarda PC + 4 em \$ra e desvia para a função",
            "Quem aloca as locais: chamador ou chamado?" to "O chamado (só ele sabe quantas são)",
            "Onde vivem dados que sobrevivem ao retorno?" to "No heap (new/malloc)",
            "Para que lado a pilha cresce no MIPS?" to "Para endereços menores: addi \$sp, \$sp, -8 abre espaço",
        ),
    ),
    SkillCard(
        skill = Skill.CODEGEN_MIPS,
        oneLiner = "O gerador traduz cada instrução TAC para MIPS — no estilo simples, busca os operandos na .data com lw/li, opera em registradores e guarda com sw — e transforma if/while em comparações (slt/sgt), desvios (beq/bne/j) e rótulos novos.",
        hook = "Busca, busca, mistura, guarda. Falso? beq pula. Fim do then? j por cima do else.",
        feynmanPrompt = "Explique para um amigo de 12 anos como um cozinheiro com uma bancada minúscula e uma despensa enorme prepara x = a + b * 2, e como ele pula passos da receita com post-its do tipo 'se for falso, vá para L0'.",
        keyTerms = listOf("registrador", "lw", "sw", "data", "text", "desvi", "syscall"),
        commonMistakes = listOf(
            "Usar uma variável da memória direto no add (no MIPS só lw/sw tocam a memória)",
            "Esquecer o j L1 no fim do then, fazendo a execução 'escorregar' para o else",
            "Reaproveitar o mesmo rótulo em dois ifs (erro de montagem)",
            "Esquecer de declarar os temporários na .data ou de encerrar com li \$v0, 10 + syscall",
            "Confundir t1 (variável na memória) com \$t1 (registrador)",
        ),
        flashcards = listOf(
            "TAC t = a + b no estilo do trabalho vira..." to "lw \$t1, a / lw \$t2, b / add \$t3, \$t1, \$t2 / sw \$t3, t",
            "Como carregar uma constante?" to "li \$t2, 5",
            "Para que serve \$zero?" to "Vale sempre 0 (comparar com falso, copiar)",
            "Esqueleto do if/else?" to "cond; beq \$t3, \$zero, Lelse; then; j Lfim; Lelse: else; Lfim:",
            "Esqueleto do while?" to "Lini: cond; beq \$t3, \$zero, Lfim; corpo; j Lini; Lfim:",
            "Imprimir um inteiro x?" to "li \$v0, 1 / lw \$a0, x / syscall",
            "Imprimir uma string msg?" to "li \$v0, 4 / la \$a0, msg / syscall",
            "Encerrar o programa?" to "li \$v0, 10 / syscall",
            "Ideia da alocação de registradores?" to "Manter valores em registradores para cortar lw/sw, que são caros",
        ),
    ),
)

// Agregadores no FIM do arquivo (propriedades de topo são inicializadas em ordem textual).
val lessonsPart5: List<Lesson> = p5RuntimeLessons + p5MipsLessons
val cardsPart5: List<SkillCard> = p5Cards
