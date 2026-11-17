package com.compilandia.content.episodes

import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.titulo
import com.compilandia.engine.BlocosBasicos
import com.compilandia.engine.MiniLang
import com.compilandia.textos.tx

/*
 * Blocos básicos, líderes e código morto na aula animada de Otimização (professor do bloco 5).
 * O TAC do laço sai do MiniLang, e os líderes e a linha morta saem do BlocosBasicos.
 */
internal const val OTIM_LACO = "i = 0; s = 0; while (i < n) { s = s + i; i = i + 1; }"

/** Um bloco sem desvio: t2 nasce e ninguém lê. Só x está vivo no fim. */
internal val OTIM_MORTO = listOf("t1 = a + b", "t2 = a * b", "x = t1")

internal fun EpisodeBuilder.otimBlocos() {
    val laco = MiniLang.tac(OTIM_LACO)
    val lideres = BlocosBasicos.lideres(laco)
    val linhas = laco.indices.map { "c$it" }.toSet()
    cena(tx("otim_b_01")) {
        clear()
        add(titulo(tx("otim_b_titulo"), size = 22f))
        add(codigo("c", laco, 66f, x = 80f, passo = 27f, size = 14f))
    }
    cena(tx("otim_b_02"), who = PROF) { hot("c0") }
    val certas = lideres.map { "c$it" }.toSet()
    pergunta(tx("otim_b_03"), certas, linhas - certas, tx("otim_b_03_dica"), tx("otim_b_03_ok")) { hot() }
    cena(tx("otim_b_04"), mood = EpMood.HAPPY) {
        val tintas = listOf(Tint.TEAL, Tint.ORANGE, Tint.PINK, Tint.BLUE)
        BlocosBasicos.blocos(laco).forEachIndexed { k, r -> for (i in r) tint("c$i", tintas[k % 4]) }
        hot(*certas.toTypedArray())
    }
    val morta = BlocosBasicos.mortas(OTIM_MORTO, setOf("x")).single()
    cena(tx("otim_b_05")) {
        clear()
        add(titulo(tx("otim_b_morto"), size = 22f))
        add(codigo("m", OTIM_MORTO, 140f, x = 110f, passo = 40f, size = 18f))
    }
    pergunta(tx("otim_b_06"), setOf("m$morta"), OTIM_MORTO.indices.map { "m$it" }.toSet() - "m$morta", tx("otim_b_06_dica"), tx("otim_b_06_ok")) { hot() }
    cena(tx("otim_b_07"), who = PROF, mood = EpMood.HAPPY) {
        drop("m$morta")
        tint("m0", Tint.GREEN); tint("m2", Tint.GREEN)
    }
}
