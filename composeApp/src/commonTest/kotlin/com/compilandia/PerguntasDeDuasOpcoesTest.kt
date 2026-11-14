package com.compilandia

import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Padrão 3 da revisão: pergunta de duas opções dá meio acerto no chute.
 * Eram 106 nas aulas básicas (29 delas no "certo ou errado" do fecho); em 14/11 ficaram 90. Este teste barra a volta.
 */
class PerguntasDeDuasOpcoesTest {
    @Test fun naoVoltaASubir() {
        val binarias = Episodes.all.filter { it.temporada == Temporada.BASICO }
            .flatMap { e -> e.passos.mapNotNull { it.ask } }
            .count { it.wrong.size == 1 }
        assertTrue(binarias <= 90, "$binarias perguntas de duas opções nas aulas básicas (o teto é 90)")
    }
}
