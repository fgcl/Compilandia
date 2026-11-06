package com.compilandia

import com.compilandia.content.Glossario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A8: o glossário acha o termo certo, inteiro, e não confunde pedaço de palavra. */
class GlossarioTest {
    private fun ids(t: String) = Glossario.achar(t).map { it.third.id }

    @Test fun achaSiglasETermosCompostos() {
        assertEquals(listOf("follow", "cifrao"), ids("O FOLLOW de E ganha o cifrão."))
        assertEquals(listOf("tabelasimbolos"), ids("A tabela de símbolos guarda cada nome."))
        assertEquals(listOf("registroativacao"), ids("Cada chamada ganha um registro de ativação."))
    }

    @Test fun naoConfundePedacoDePalavra() {
        assertEquals(listOf("naoterminal"), ids("E é um não-terminal."))
        assertTrue("first" !in ids("O first, em minúscula, não é a sigla."))
        assertTrue(ids("Reduzimos a conta.").isEmpty(), "reduce não casa com reduzimos")
    }

    @Test fun marcaCadaTermoUmaVezSo() {
        assertEquals(1, ids("Token aqui, token ali, mais um token.").size)
    }

    @Test fun todoTermoTemDefinicaoCurta() {
        for (t in Glossario.termos) {
            assertTrue(t.definicao.split(" ").size <= 18, "${t.id}: definição comprida")
            assertTrue(t.formas.isNotEmpty(), "${t.id}: sem formas")
        }
    }
}
