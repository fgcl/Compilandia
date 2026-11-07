package com.compilandia.engine

import com.compilandia.content.Etapa
import com.compilandia.content.Robo
import com.compilandia.content.Trilha
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** J7: toda fase tem resposta, e a entrega segue a busca da tabela de símbolos. */
class CarteiroTest {
    @Test fun todaFaseTemRespostaQuePassa() {
        for (f in Carteiro.fases) {
            val v = Carteiro.confere(f, f.solucao)
            assertTrue(v.passou, "${f.id}: a resposta não passa: ${v.frase}")
            assertFalse(Carteiro.confere(f, emptyMap()).passou)
            assertEquals(f.usos.map { it.id }.toSet(), f.esperado.keys, f.id)
            // Tudo na sala global não basta em nenhuma fase depois da primeira.
            if (f.id != "escopo_global") {
                val tudoFora = f.cartas.associate { it.id to "arq" }
                assertFalse(Carteiro.confere(f, tudoFora).passou, "${f.id}: passa com tudo na global")
            }
        }
    }

    @Test fun byteSobeAteAcharADeclaracao() {
        val f = Carteiro.fases.first { it.id == "escopo_sombra" }
        val e = Carteiro.entrega(f, mapOf("xi" to "main", "xf" to "if"), f.usos.first { it.id == "u2" })
        assertEquals(listOf("if"), e.quadros.map { it.sala })
        assertEquals("xf", e.achou)
        val e2 = Carteiro.entrega(f, mapOf("xi" to "arq", "xf" to "if"), f.usos.first { it.id == "u1" })
        assertEquals(listOf("main", "arq"), e2.quadros.map { it.sala })
        assertEquals("Na sala main() não há x. O Byte sobe para global.", e2.quadros.first().nota)
    }

    @Test fun irmaoNaoVeOOutro() {
        val f = Carteiro.fases.first { it.id == "escopo_irmaos" }
        // i em main: o else acha o i e devia dar erro.
        val v = Carteiro.confere(f, mapOf("i" to "main", "j" to "main"))
        assertEquals("i em else", v.testes[v.falha!!].entrada)
        assertEquals("O uso de i em else achou int i = 0, na sala main(). Ali devia dar erro.", v.frase)
        assertEquals("O caminho de else passa por int i = 0. Ponha essa carta numa sala fora do caminho.", Carteiro.ondeOlhar(f, mapOf("i" to "main", "j" to "main")))
        val e = Carteiro.entrega(f, f.solucao, f.usos.first { it.id == "u4" })
        assertNull(e.achou)
        assertEquals(listOf("else", "main", "arq"), e.quadros.map { it.sala })
    }

    @Test fun duasDeclaracoesNaMesmaSalaNaoRodam() {
        val f = Carteiro.fases.first { it.id == "escopo_sombra" }
        val v = Carteiro.confere(f, mapOf("xi" to "main", "xf" to "main"))
        assertEquals(-1, v.falha)
        assertEquals("Há dois x na sala main(). O compilador acusa redeclaração antes de rodar.", v.frase)
    }

    @Test fun programaSaiEmC() {
        val f = Carteiro.fases.first { it.id == "escopo_global" }
        val p = Carteiro.programa(f, f.solucao).joinToString("\n") { "  ".repeat(it.fundura) + it.texto }
        assertEquals(
            "int total = 0;\nvoid soma() {\n  total = total + 1;\n}\nint main() {\n  printf(\"%d\", total);\n}",
            p,
        )
        assertEquals("soma()", Carteiro.nomeSala(f, "soma"))
    }

    @Test fun carteiroEntraNaTrilha() {
        assertEquals(Robo.CARTEIRO, Robo.de(Skill.SCOPE_SYMBOLS))
        assertTrue(Trilha.passos(Skill.SCOPE_SYMBOLS).any { it.etapa == Etapa.PROGRAMAR })
    }
}
