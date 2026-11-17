package com.compilandia.engine

/*
 * Acesso a vetor, x = v[i] (pedido do revisor professor do bloco 5).
 * Cada int ocupa 4 bytes, então v[i] mora em base + 4 * i (Dragão, seção 6.4.3).
 * No TAC, o deslocamento vem antes: t1 = i * 4, depois t2 = v[t1].
 * No MIPS, la pega a base, sll de 2 bits faz o vezes 4, add soma, e lw lê no endereço.
 * O MIPS roda na MipsVM: o teste confere que ele lê o elemento certo.
 */
object AcessoVetor {
    const val BYTES = 4

    fun endereco(base: Long, i: Long): Long = base + BYTES * i

    fun tac(x: String, v: String, i: String): List<String> = listOf("t1 = $i * $BYTES", "t2 = $v[t1]", "$x = t2")

    fun mips(x: String, v: String, i: String): List<String> = listOf(
        "la \$t0, $v",
        "lw \$t1, $i",
        "sll \$t1, \$t1, 2",
        "add \$t0, \$t0, \$t1",
        "lw \$t2, 0(\$t0)",
        "sw \$t2, $x",
    )

    /** O programa inteiro para a MipsVM, com o vetor e o índice no .data. */
    fun programa(x: String, v: String, i: String, valores: List<Long>, indice: Long): String = buildString {
        appendLine(".data")
        appendLine("$v: .word ${valores.joinToString(", ")}")
        appendLine("$i: .word $indice")
        appendLine("$x: .word 0")
        appendLine(".text")
        appendLine("main:")
        for (l in mips(x, v, i)) appendLine("    $l")
        appendLine("    li \$v0, 10")
        appendLine("    syscall")
    }

    /** Roda o MIPS de x = v[i] e devolve o x. */
    fun roda(x: String, v: String, i: String, valores: List<Long>, indice: Long): Long =
        requireNotNull(MipsVM(programa(x, v, i, valores, indice)).run().word(x)) { "o .data não tem o $x" }
}
