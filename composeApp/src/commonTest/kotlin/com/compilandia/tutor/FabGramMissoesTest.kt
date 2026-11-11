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
