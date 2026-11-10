package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.tutor.FabGram.Modelo
import com.compilandia.tutor.FabGram.TipoBloco
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FabGramTest {
    private fun g(t: String) = Grammar.parse(Diagnostico.normaliza(t))

    @Test fun modeloSegueOPapelDaRegra() {
        val gr = g("S -> a S b | T | ε | c\nT -> if E then S else S\nE -> id x")
        val m = gr.productions.map { FabGram.modelo(it, gr) }
        assertEquals(listOf(Modelo.PRENSA, Modelo.BRACO, Modelo.COFRE, Modelo.COFRE, Modelo.ARMARIO, Modelo.PRENSA), m)
    }

    @Test fun blocosSoTemTamanhoFixo() {
        for ((t, _) in FabGram.exemplos) {
            val pl = FabGram.planta(g(t))
            for (b in pl.blocos) assertTrue((b.w to b.h) in setOf(1 to 1, 1 to 2, 2 to 2), "bloco ${b.tipo} ${b.w}x${b.h}")
        }
    }

    @Test fun nadaSeSobrepoeNaPlanta() {
        for ((t, _) in FabGram.exemplos + ("A -> B C | ε\nB -> b | B b\nC -> c C d e | ε" to "")) {
            val pl = FabGram.planta(g(t))
            val ocupadas = HashMap<FabGram.Cel, String>()
            for (b in pl.blocos) for (x in b.x until b.x + b.w) for (y in b.y until b.y + b.h) {
                val c = FabGram.Cel(x, y)
                assertTrue(c !in ocupadas, "$t: ${b.tipo} em cima de ${ocupadas[c]} em $c")
                ocupadas[c] = b.tipo.name
            }
            for (e in pl.esteiras) for (c in e.celulas) {
                assertTrue(c.x in 0 until pl.largura && c.y in 0 until pl.altura, "$t: esteira fora da grade em $c")
                assertTrue(c !in ocupadas, "$t: esteira ${e.papel} em cima de ${ocupadas[c]} em $c")
            }
        }
    }

    @Test fun fazAFraseCertaEEnchendoTodasAsCasas() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val c = FabGram.corrida(pl, listOf("a", "a", "b", "b"))
        assertTrue(c.aceita)
        assertFalse(c.ambigua)
        assertEquals(listOf("a", "a", "b", "b"), c.arvore!!.leaves())
        assertEquals(4, c.chegadas.size)
        assertEquals(setOf(0, 1, 2, 3), c.chegadas.map { it.casa }.toSet())
        // cofre (S -> ε) trabalha uma vez, prensa duas
        val cofre = pl.blocos.first { it.modelo == Modelo.COFRE }
        assertEquals(1, c.trabalhos.count { it.bloco == cofre.id })
        assertTrue(c.duracao > 0f)
    }

    @Test fun fraseErradaMostraAMaisPerto() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val c = FabGram.corrida(pl, listOf("a", "b", "b"))
        assertFalse(c.aceita)
        val perto = assertNotNull(c.feita)
        assertEquals(1, FabGram.edicao(perto, listOf("a", "b", "b")))
    }

    @Test fun achaAmbiguidade() {
        val pl = FabGram.planta(g("E -> E + E | id"))
        val c = FabGram.corrida(pl, listOf("id", "+", "id", "+", "id"))
        assertTrue(c.aceita)
        assertTrue(c.ambigua)
    }

    @Test fun caixaPassaPorDentroDaMaquina() {
        // nada teleporta: cada trecho começa onde o anterior do mesmo item terminou ou numa máquina
        val pl = FabGram.planta(g("E -> E + T | T\nT -> id"))
        val c = FabGram.corrida(pl, listOf("id", "+", "id"))
        val maquinas = pl.blocos.filter { it.tipo == TipoBloco.MAQUINA }
        for (tr in c.trechos) {
            val (x, y) = tr.pontos.first()
            val naFonte = x == pl.fonte.cx && y == pl.fonte.cy
            val naMaquina = maquinas.any { it.cx == x && it.cy == y }
            val noCaminho = c.trechos.any { o -> o !== tr && o.pontos.last() == tr.pontos.first() }
            assertTrue(naFonte || naMaquina || noCaminho, "trecho de ${tr.simbolo} começa do nada em ($x, $y)")
        }
    }
}
