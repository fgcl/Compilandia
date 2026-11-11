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
