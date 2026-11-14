package com.compilandia

import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import kotlin.test.Test
import kotlin.test.assertTrue

/** Nas aulas animadas, nenhum trecho passa de 4 cenas só ouvindo: um toque quebra cada sequência (revisores, padrão 2). */
class TrechosSoOuvindoTest {
    @Test fun nenhumTrechoComMaisDeQuatroCenasSoOuvindo() {
        val longos = mutableListOf<String>()
        for (e in Episodes.all.filter { it.temporada == Temporada.BASICO }) {
            var inicio = -1
            var n = 0
            fun fecha() { if (n > 4) longos += "${e.skill}: cenas $inicio a ${inicio + n - 1} ($n)"; n = 0 }
            e.passos.forEachIndexed { i, p ->
                if (p.ask != null || p.brinquedo != null) fecha() else { if (n == 0) inicio = i; n++ }
            }
            fecha()
        }
        assertTrue(longos.isEmpty(), longos.joinToString("\n"))
    }
}
