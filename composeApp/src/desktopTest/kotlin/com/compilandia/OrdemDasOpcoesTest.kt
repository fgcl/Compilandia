package com.compilandia

import com.compilandia.content.Beat
import com.compilandia.content.Library
import com.compilandia.content.episodes.Aberturas
import com.compilandia.learning.OrdemDasOpcoes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A tela embaralha as alternativas escritas à mão. Aqui se confere que a posição da certa deixou de ser pista. */
class OrdemDasOpcoesTest {
    private data class Q(val chave: String, val opcoes: List<String>, val certa: Int)

    private val perguntas: List<Q> by lazy {
        val beats = Library.lessons.flatMap { it.beats } + Library.guide.flatMap { it.beats }
        beats.filterIsInstance<Beat.Predict>().map { Q(it.question, it.options, it.correct) } +
            Aberturas.todas.map { Q(it.desafio + it.skill.name, it.opcoes, it.certa) }
    }

    @Test
    fun eUmaPermutacao() {
        for (q in perguntas) {
            val o = OrdemDasOpcoes.de(q.opcoes, q.chave, sal = 7)
            assertEquals(q.opcoes.indices.toSet(), o.toSet(), q.chave)
            assertEquals(q.opcoes.size, o.size, q.chave)
        }
    }

    @Test
    fun mesmaChaveMesmaOrdem() {
        val ops = listOf("a", "b", "c", "d")
        assertEquals(OrdemDasOpcoes.de(ops, "x", sal = 3), OrdemDasOpcoes.de(ops, "x", sal = 3))
    }

    @Test
    fun numerosFicamEmOrdemCrescente() {
        assertEquals(listOf(1, 2, 0), OrdemDasOpcoes.de(listOf("13", "5", "6"), "q", sal = 1))
        assertEquals(listOf(0, 1, 2), OrdemDasOpcoes.de(listOf("2", "3", "5"), "q", sal = 99))
    }

    @Test
    fun opcaoQueFalaDasOutrasFicaNoFim() {
        for (sal in 0L..20L) {
            val o = OrdemDasOpcoes.de(listOf("q1", "os dois", "q2"), "ab", sal)
            assertEquals(1, o.last())
        }
    }

    /**
     * Antes, nas perguntas de 4 opções, a certa era a última 0 vezes e a segunda 84 vezes em 137.
     * Com o embaralho, cada posição precisa aparecer perto de 1/4 das vezes.
     */
    @Test
    fun posicaoDaCertaSeEspalha() {
        val quatro = perguntas.filter { it.opcoes.size == 4 }
        assertTrue(quatro.size >= 100, "perguntas de 4 opções: ${quatro.size}")
        for (sal in listOf(0L, 1L, 2L)) {
            val pos = IntArray(4)
            for (q in quatro) pos[OrdemDasOpcoes.de(q.opcoes, q.chave, sal).indexOf(q.certa)]++
            println("sal $sal: posição da certa ${pos.toList()} em ${quatro.size}")
            for (p in pos) assertTrue(p >= quatro.size / 8, "posição rara demais: ${pos.toList()}")
        }
        val antes = IntArray(4)
        for (q in quatro) antes[q.certa]++
        println("como está escrito: ${antes.toList()}")
    }

    /**
     * Na parte Erros, o colega acertava em 0 das 29 aulas, e "errado" virava resposta decorada.
     * Guias de prova pedem perto de metade verdadeiras. Aqui: entre 35% e 65%.
     */
    @Test
    fun colegaAcertaEmParteDasAulas() {
        val respostas = com.compilandia.content.episodes.Episodes.basicos.mapNotNull { ep ->
            ep.passos.firstNotNullOfOrNull { p -> p.ask?.right?.let { if ("cuS" in it) true else if ("cuN" in it) false else null } }
        }
        assertTrue(respostas.size >= 29, "aulas com a parte Erros: ${respostas.size}")
        val certas = respostas.count { it }
        println("colega certo em $certas de ${respostas.size}")
        assertTrue(certas * 100 / respostas.size in 35..65, "colega certo em $certas de ${respostas.size}")
    }

    /** Lista as opções que ficam presas no fim, para conferir de olho se o filtro não pegou opção comum. */
    @Test
    fun listaAsFixas() {
        for (q in perguntas) {
            val o = OrdemDasOpcoes.de(q.opcoes, q.chave, sal = 0)
            val semFixa = OrdemDasOpcoes.de(q.opcoes.map { "x$it" }, q.chave, sal = 0)
            if (q.opcoes.any { it.trim().toDoubleOrNull() == null } && o.last() != semFixa.last()) println("fixa no fim: ${q.opcoes[o.last()]}  |  ${q.opcoes}")
        }
    }
}
