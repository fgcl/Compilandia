package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.tutor.FabGram.Modelo
import com.compilandia.tutor.FabGram.TipoBloco
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
            val gr = g(t)
            val pl = FabGram.planta(gr)
            for (b in pl.blocos) when (b.tipo) {
                // o separador tem uma saída por regra; o roteador, uma por letra mais a da montadora
                TipoBloco.SEPARADOR -> assertEquals(1 to gr.productionsOf(b.nt!!).size, b.w to b.h)
                TipoBloco.ROTEADOR -> assertEquals(FabGram.letras(gr).size + 1 to 1, b.w to b.h)
                else -> assertTrue((b.w to b.h) in setOf(1 to 1, 1 to 2, 2 to 2), "bloco ${b.tipo} ${b.w}x${b.h}")
            }
        }
    }

    @Test fun soEntraPelaFrente() {
        // a caixa que passa ao lado da máquina não entra: a esteira precisa apontar para ela
        val pl = FabGram.planta(g("S -> a b"))
        val prensa = pl.blocos.first { it.modelo == Modelo.PRENSA }
        val entrada = pl.esteiras.entries.first { (c, d) -> prensa.contem(c + d) }.key
        val deLado = pl.comEsteira(entrada, FabGram.Dir.DIREITA)!!.comEsteira(entrada + FabGram.Dir.DIREITA, FabGram.Dir.DIREITA)!!
        val c = FabGram.corrida(deLado, listOf("a", "b"))
        assertNotNull(c.quebra)
    }

    private val todas = FabGram.exemplos.map { it.first } + "A -> B C | ε\nB -> b | B b\nC -> c C d e | ε"

    @Test fun nadaSeSobrepoeNaPlanta() {
        for (t in todas) {
            val pl = FabGram.planta(g(t))
            val ocupadas = HashMap<FabGram.Cel, String>()
            for (b in pl.blocos) for (c in b.celulas()) {
                assertTrue(c !in ocupadas, "$t: ${b.tipo} em cima de ${ocupadas[c]} em $c")
                ocupadas[c] = b.tipo.name
            }
            for (c in pl.esteiras.keys) {
                assertTrue(c !in ocupadas, "$t: esteira em cima de ${ocupadas[c]} em $c")
            }
        }
    }

    @Test fun todoBlocoQueFazAlgoTemSaida() {
        for (t in todas) {
            val pl = FabGram.planta(g(t))
            for (b in pl.blocos) {
                val precisa = b.tipo == TipoBloco.FONTE || b.tipo == TipoBloco.SEPARADOR || (b.tipo == TipoBloco.MAQUINA && b.prod!!.rhs.isNotEmpty())
                if (precisa) assertNotNull(pl.saidaDe(b), "$t: ${b.tipo} ${b.prod} sem saída")
            }
        }
    }

    @Test fun plantaAutomaticaFazOsExemplos() {
        for ((t, f) in FabGram.exemplos) {
            val c = FabGram.corrida(FabGram.planta(g(t)), f.split(" "))
            assertNull(c.quebra, "$t: travou em ${c.quebra}")
            assertTrue(c.saiu, t)
            assertEquals(f.split(" "), c.chegadas.sortedBy { it.casa }.map { it.simbolo }, t)
        }
    }

    @Test fun fazAFraseCertaEEnchendoTodasAsCasas() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val c = FabGram.corrida(pl, listOf("a", "a", "b", "b"))
        assertTrue(c.saiu)
        assertFalse(c.ambigua)
        assertEquals(listOf("a", "a", "b", "b"), c.arvore!!.leaves())
        // cada peça cai na casa dela, mesmo chegando fora de ordem
        assertEquals(listOf("a", "a", "b", "b"), c.chegadas.sortedBy { it.casa }.map { it.simbolo })
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

    @Test fun nadaTeleporta() {
        // todo trecho começa no centro de um bloco: o item sai de onde estava
        val pl = FabGram.planta(g("E -> E + T | T\nT -> id"))
        val c = FabGram.corrida(pl, listOf("id", "+", "id"))
        for (tr in c.trechos) {
            val (x, y) = tr.pontos.first()
            assertTrue(pl.blocos.any { it.cx == x && it.cy == y }, "trecho de ${tr.simbolo} começa do nada em ($x, $y)")
            // e anda de célula vizinha em célula vizinha
            for (i in 2 until tr.pontos.size - 1) {
                val a = tr.pontos[i - 1]; val b = tr.pontos[i]
                assertEquals(1f, kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second), "salto em ${tr.simbolo}")
            }
        }
    }

    @Test fun esteiraCortadaTravaAFabrica() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val prensa = pl.blocos.first { it.modelo == Modelo.PRENSA }
        // corta a volta da caixa S nova, já perto do separador
        val volta = (pl.rota(prensa, pl.separadorDe("S")) as Rota.Chega).celulas.filter { it in pl.esteiras }
        val cortada = pl.semEsteira(volta[volta.size - 3])
        val c = FabGram.corrida(cortada, listOf("a", "a", "b", "b"))
        val q = assertNotNull(c.quebra)
        assertEquals(Motivo.ACABA, q.motivo)
        assertEquals(TipoBloco.SEPARADOR, q.destino.tipo)
        assertFalse(c.saiu)
    }

    @Test fun maquinaSemSaidaAvisa() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val prensa = pl.blocos.first { it.modelo == Modelo.PRENSA }
        val c = FabGram.corrida(pl.semEsteira(pl.saidaDe(prensa)!!), listOf("a", "b"))
        assertEquals(Motivo.SEM_SAIDA, c.quebra?.motivo)
    }

    @Test fun moverNaoDeixaEmpilhar() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val prensa = pl.blocos.first { it.modelo == Modelo.PRENSA }
        val cofre = pl.blocos.first { it.modelo == Modelo.COFRE }
        assertNull(pl.move(prensa.id, cofre.x, cofre.y))
        assertNull(pl.move(prensa.id, 0, 0, FabGram.Area(1, 1, 50, 50)))
        val longe = assertNotNull(pl.move(prensa.id, 20, 20))
        assertEquals(20, longe.blocos.first { it.id == prensa.id }.x)
        // longe da esteira, a prensa não recebe caixa: a fábrica para no caminho
        assertNotNull(FabGram.corrida(longe, listOf("a", "b")).quebra)
    }

    @Test fun girarDaUmQuartoDeVolta() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        val c = pl.esteiras.keys.first()
        val d = pl.esteiras.getValue(c)
        assertEquals(d.gira(), pl.gira(c).esteiras[c])
        assertEquals(d, pl.gira(c).gira(c).gira(c).gira(c).esteiras[c])
    }

    @Test fun salvaECarregaAMesmaPlanta() {
        val gr = g("E -> E + T | T\nT -> id")
        val pl = FabGram.planta(gr).let { it.move(it.montadora.id, 30, 30)!! }.semEsteira(FabGram.Cel(1, 4))
        val volta = assertNotNull(FabGram.carrega(gr, FabGram.salva(pl)))
        assertEquals(pl.blocos, volta.blocos)
        assertEquals(pl.esteiras, volta.esteiras)
        // a planta de outra gramática não serve
        assertNull(FabGram.carrega(g("S -> a S b | ε\nA -> a"), FabGram.salva(pl)))
    }
}
