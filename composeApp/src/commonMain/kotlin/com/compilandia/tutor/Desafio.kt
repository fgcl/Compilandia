package com.compilandia.tutor

import com.compilandia.engine.Earley
import com.compilandia.engine.Grammar
import com.compilandia.engine.SentenceGenerator
import kotlin.random.Random
import com.compilandia.textos.tx

enum class Cor { VERDE, AMARELO, CINZA }

/**
 * Desafio do dia: um Wordle em que a gramática é o dicionário.
 * Todo palpite precisa ser uma frase válida, e o motor confere. Palpitar já é derivar.
 * O sorteio vem do número do dia: é o mesmo em qualquer celular, sem servidor.
 */
object Desafio {
    const val TENTATIVAS = 6
    /** 2 de novembro de 2026 é o nº 1. */
    private const val DIA_ZERO = 20758L

    private val gramaticas = listOf(
        "E -> T + E | T\nT -> ( E ) | id",
        "E -> E + T | E * T | T\nT -> ( E ) | num | id",
        "S -> if C then S else S | if C then S | a\nC -> b",
        "D -> T id ; D | T id ;\nT -> int | float",
        "L -> L , I | I\nI -> id = num | id",
        "S -> a S b | a b | c S | c",
        "P -> S ; P | S\nS -> id = E\nE -> id | num",
        "E -> - E | ( E ) | E + E | id",
    )

    data class Dia(val numero: Long, val texto: String, val g: Grammar, val segredo: List<String>) {
        val tamanho: Int get() = segredo.size
        val teclas: List<String> get() = g.terminals
    }

    fun numero(dia: Long): Long = dia - DIA_ZERO

    fun doDia(dia: Long): Dia {
        val rnd = Random(dia * 7919 + 13)
        // Sorteia gramáticas até sair uma frase boa: nem curta demais, nem repetindo um token só.
        repeat(40) {
            val texto = gramaticas[rnd.nextInt(gramaticas.size)]
            val g = Grammar.parse(texto)
            val f = SentenceGenerator.generate(g, rnd, maxDepth = 6, maxTokens = 7, tries = 30)
            if (f != null && f.size in 5..7 && f.distinct().size >= 3) return Dia(numero(dia), texto, g, f)
        }
        val g = Grammar.parse(gramaticas[0])
        return Dia(numero(dia), gramaticas[0], g, listOf("(", "id", "+", "id", ")"))
    }

    fun valido(d: Dia, palpite: List<String>): Boolean = palpite.size == d.tamanho && Earley(d.g).accepts(palpite)

    /** Regra do Wordle: verde primeiro; amarelo só enquanto sobrar daquele token no segredo. */
    fun cores(palpite: List<String>, segredo: List<String>): List<Cor> {
        val sobra = HashMap<String, Int>()
        for (i in segredo.indices) if (palpite[i] != segredo[i]) sobra[segredo[i]] = (sobra[segredo[i]] ?: 0) + 1
        return palpite.indices.map { i ->
            when {
                palpite[i] == segredo[i] -> Cor.VERDE
                (sobra[palpite[i]] ?: 0) > 0 -> { sobra[palpite[i]] = sobra.getValue(palpite[i]) - 1; Cor.AMARELO }
                else -> Cor.CINZA
            }
        }
    }

    /** O texto que vai para o grupo: mostra o caminho sem entregar a frase. */
    fun resultado(d: Dia, palpites: List<List<String>>, venceu: Boolean): String = buildString {
        appendLine(tx("desafio_compilandia_no_1_2_3", d.numero, if (venceu) palpites.size else "X", TENTATIVAS))
        for (p in palpites) appendLine(cores(p, d.segredo).joinToString("") { when (it) { Cor.VERDE -> "🟩"; Cor.AMARELO -> "🟨"; Cor.CINZA -> "⬛" } })
    }
}
