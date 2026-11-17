package com.compilandia.content.episodes

import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.MiniLang
import com.compilandia.textos.tx

/*
 * O if, o while e o e (&&) até o MIPS, no fim da Sua vez da aula de MIPS (professor do bloco 5).
 * Um esquema de desvio só, o mesmo do Guia do TP e do jogo dos desvios: a comparação deixa 0 ou 1
 * num registrador (slt), e o beq com $zero pula quando ela é falsa. No TAC, o par é o ifFalse.
 * Todo código da tela sai do MiniLang; PedidosProfessoresTest roda cada um no MipsVM.
 */
internal const val MIPS_IF = "if (a < b) { x = 1; }"
internal const val MIPS_WHILE = "while (i < n) { i = i + 1; }"
internal const val MIPS_E = "if (a < b && c < d) { x = 1; }"

/** a = 9 e b = 2: a < b dá 0, e nenhuma linha de c < d roda. */
internal val MIPS_E_VALORES = mapOf("a" to 9L, "b" to 2L, "c" to 1L, "d" to 5L)

private fun linha(l: List<String>, prefixo: String) = "c" + l.indexOfFirst { it.startsWith(prefixo) }

internal fun EpisodeBuilder.mipsDesvios() {
    val se = MiniLang.corpo(MIPS_IF)
    val tac = MiniLang.tac(MIPS_IF)
    cena(tx("mips_d_01")) {
        clear()
        add(titulo("if (a < b) x = 1;", size = 22f))
        add(codigo("c", se, 74f, x = 70f, passo = 32f, size = 16f))
        hot(linha(se, "slt"))
    }
    val beq = linha(se, "beq")
    pergunta(tx("mips_d_02"), setOf(beq), setOf(linha(se, "slt"), linha(se, "li"), linha(se, "sw")), tx("mips_d_02_dica"), tx("mips_d_02_ok")) { hot() }
    cena(tx("mips_d_03"), who = PROF) {
        add(rotulo("lt", "TAC", 40f, 304f, Tint.TEAL, 14f))
        add(codigo("t", tac.take(2), 304f, x = 70f, passo = 30f, size = 15f))
        hot(linha(se, "slt"), beq, "t0", "t1")
    }
    val enq = MiniLang.corpo(MIPS_WHILE)
    cena(tx("mips_d_04")) {
        clear()
        add(titulo("while (i < n) i = i + 1;", size = 20f))
        add(codigo("c", enq, 70f, x = 70f, passo = 29f, size = 14f))
        hot("c0")
    }
    val fim = "c${enq.lastIndex}"
    pergunta(tx("mips_d_05"), setOf("c0"), setOf(fim, "c1", linha(enq, "beq")), tx("mips_d_05_dica"), tx("mips_d_05_ok")) { hot(linha(enq, "j ")) }
    cena(tx("mips_d_06"), mood = EpMood.HAPPY) { hot(linha(enq, "beq"), fim) }
    val e = MiniLang.corpo(MIPS_E)
    cena(tx("mips_d_07")) {
        clear()
        add(titulo("if (a < b && c < d) x = 1;", size = 18f))
        add(codigo("c", e, 66f, x = 70f, passo = 27f, size = 14f))
        hot(*e.indices.filter { e[it].startsWith("beq") }.map { "c$it" }.toTypedArray())
    }
    pergunta(tx("mips_d_08"), setOf("n0"), setOf("n2", "n4"), tx("mips_d_08_dica"), tx("mips_d_08_ok")) {
        add(opcao("n0", "0", 120f, 376f), opcao("n2", "2", 180f, 376f), opcao("n4", "4", 240f, 376f))
        hot()
    }
    cena(tx("mips_d_09"), who = PROF, mood = EpMood.HAPPY) {
        drop("n0", "n2", "n4")
        hot(*(4..7).map { "c$it" }.toTypedArray())
    }
}
