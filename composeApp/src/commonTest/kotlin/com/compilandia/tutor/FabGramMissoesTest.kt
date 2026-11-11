package com.compilandia.tutor

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** O tutorial: cada missão começa estragada, e o que o chão mostra basta para consertar. */
class FabGramMissoesTest {
    private fun frase(m: Missao) = m.frase.split(" ")

    @Test fun cadaMissaoComecaQuebradaMenosALigar() {
        for (m in Missao.entries) {
            val p = FabGram.prepara(m)
            val c = FabGram.corrida(p.planta, frase(m))
            if (m == Missao.LIGAR) assertTrue(c.saiu, "$m já devia funcionar")
            else { assertNotNull(c.quebra, "$m devia travar antes de consertar"); assertTrue(!c.saiu) }
        }
    }

    @Test fun oFantasmaResolveCadaMissao() {
        for (m in Missao.entries) {
            val p = FabGram.prepara(m)
            val c = FabGram.corrida(FabGram.resolve(p), frase(m))
            assertTrue(c.saiu, "$m: com o fantasma preenchido a frase devia sair (${c.quebra})")
        }
    }

    @Test fun cadaMissaoPedeUmaCoisaSo() {
        for (m in Missao.entries) {
            val p = FabGram.prepara(m)
            assertTrue(p.guiaEsteiras.size <= 4, "$m pede ${p.guiaEsteiras.size} esteiras: é tutorial, peça pouco")
            assertTrue(p.guiaEsteiras.isEmpty() || p.guiaBloco == null, "$m mistura duas tarefas")
        }
    }

    @Test fun separadorCarimbaONumeroDaRegra() {
        val p = FabGram.prepara(Missao.ESCOLHA)
        val c = FabGram.corrida(FabGram.resolve(p), listOf("b"))
        assertTrue(c.trechos.any { it.caixa && it.marca == 2 }, "a caixa devia ir carimbada com o 2")
    }
}

class FabGramDesafiosTest {
    @Test fun desafioComecaSemEsteiraEAPlantaAutomaticaGanhaTresEstrelas() {
        for (m in Missao.entries.filter { it.desafio }) {
            val p = FabGram.prepara(m)
            assertTrue(p.planta.esteiras.isEmpty(), "$m devia começar sem esteiras")
            assertTrue(!FabGram.cumpre(p, p.planta))
            val pronta = FabGram.resolve(p)
            assertTrue(FabGram.cumpre(p, pronta), "$m: a planta automática devia cumprir")
            kotlin.test.assertEquals(3, p.estrelas(pronta.esteiras.size))
            kotlin.test.assertEquals(1, p.estrelas(pronta.esteiras.size * 2))
        }
    }

    @Test fun oTutorialVemAntesDosDesafios() {
        kotlin.test.assertEquals(6, TUTORIAL)
        assertTrue(Missao.entries.take(TUTORIAL).none { it.desafio })
    }
}

class FabGramDicaTest {
    @Test fun dicaMostraPoucoDeCadaVezEChegaNaPlantaQueFunciona() {
        for (m in Missao.entries.filter { it.desafio }) {
            val p = FabGram.prepara(m)
            var pl = p.planta
            var vezes = 0
            while (!FabGram.cumpre(p, pl) && vezes < 100) {
                val d = FabGram.dica(p, pl)
                assertTrue(d.isNotEmpty(), "$m: sem dica e ainda sem cumprir")
                assertTrue(d.size <= 4, "$m: a dica mostrou ${d.size} esteiras de uma vez")
                pl = FabGram.Planta(pl.g, pl.blocos, pl.esteiras + d)
                vezes++
            }
            assertTrue(FabGram.cumpre(p, pl), "$m: seguindo só as dicas a fábrica devia funcionar")
        }
    }

    @Test fun cadaDicaCustaUmaEstrela() {
        val p = FabGram.prepara(Missao.D_ANBN)
        kotlin.test.assertEquals(3, p.estrelas(p.orcamento!!))
    }
}

class FabricaNaTrilhaTest {
    private fun l() = com.compilandia.learning.Learner(com.compilandia.learning.LearnerState(), kotlin.random.Random(1))

    @Test fun derivacaoAmbiguidadeERecursaoTemEtapaNaFabrica() {
        for (s in listOf(com.compilandia.learning.Skill.CFG_DERIVATION, com.compilandia.learning.Skill.AMBIGUITY, com.compilandia.learning.Skill.LEFT_RECURSION))
            assertTrue(com.compilandia.content.Trilha.passos(s).any { it.etapa == com.compilandia.content.Etapa.FABRICA }, "$s sem etapa da Fábrica")
        assertTrue(com.compilandia.content.Trilha.passos(com.compilandia.learning.Skill.FIRST).none { it.etapa == com.compilandia.content.Etapa.FABRICA })
    }

    @Test fun missaoRendeEngrenagensUmaVezEOTutorialNaoPula() {
        val l = l()
        val g0 = l.state.gears
        kotlin.test.assertEquals(5, l.cumpreMissaoFabrica("LIGAR", 0, 0))
        kotlin.test.assertEquals(0, l.cumpreMissaoFabrica("LIGAR", 0, 0))
        kotlin.test.assertEquals(1, l.state.fabGramMissoes)
        // um desafio aberto pela trilha não destrava o tutorial
        kotlin.test.assertEquals(10, l.cumpreMissaoFabrica("D_IFELSE", Missao.D_IFELSE.ordinal, 2))
        kotlin.test.assertEquals(1, l.state.fabGramMissoes)
        // melhorar de 2 para 3 estrelas paga só a nova
        kotlin.test.assertEquals(5, l.cumpreMissaoFabrica("D_IFELSE", Missao.D_IFELSE.ordinal, 3))
        kotlin.test.assertEquals(g0 + 20, l.state.gears)
        val amb = com.compilandia.content.Trilha.passos(com.compilandia.learning.Skill.AMBIGUITY).first { it.etapa == com.compilandia.content.Etapa.FABRICA }
        kotlin.test.assertEquals(com.compilandia.content.EstadoEtapa.FEITA, com.compilandia.content.Trilha.estado(l, com.compilandia.learning.Skill.AMBIGUITY, amb))
    }
}
