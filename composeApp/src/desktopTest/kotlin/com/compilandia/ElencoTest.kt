package com.compilandia

import com.compilandia.content.Speaker
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Os revisores alunos acharam as aulas animadas secas: o Byte falava quase tudo, a professora uma ou duas frases,
 * e o Bug nenhuma. Agora o Bug lança o desafio do fecho e a professora fecha cada aula. Este teste barra a volta.
 */
class ElencoTest {
    private val basicos = Episodes.all.filter { it.temporada == Temporada.BASICO }

    /** Em cada aula básica, o Bug lança um desafio só, e quem dá a dica e comemora é o Byte. */
    @Test fun oBugDesafiaNoFecho() {
        for (e in basicos) {
            val doBug = e.passos.filter { it.fala.who == Speaker.BUG }
            assertEquals(1, doBug.size, "${e.title}: falas do Bug")
            val ask = doBug.single().ask
            assertTrue(ask != null, "${e.title}: a fala do Bug é uma pergunta")
            assertEquals(Speaker.BYTE, ask.hint.who)
            assertEquals(Speaker.BYTE, ask.got.who)
            assertTrue("colega" !in doBug.single().fala.text, "${e.title}: o Bug fala por si")
        }
    }

    /** A professora fecha toda aula: o segundo erro comum e o resumo são dela. */
    @Test fun aProfessoraFechaAsAulas() {
        for (e in basicos) {
            val prof = e.passos.count { it.fala.who == Speaker.PROF }
            assertTrue(prof >= 2, "${e.title}: a professora fala $prof vezes")
            assertEquals(Speaker.PROF, e.passos.last().fala.who, "${e.title}: o resumo é da professora")
        }
        assertTrue(basicos.sumOf { e -> e.passos.count { it.fala.who == Speaker.PROF } } >= 80)
    }

    /** Toda personagem que fala tem voz no render.py. */
    @Test fun todaPersonagemTemVoz() {
        val vozes = File("../scripts/voice/render.py").readLines().first { it.startsWith("VOZES = ") }
        for (s in Speaker.entries) assertTrue("'${s.name}'" in vozes, "${s.name} sem voz no render.py")
    }
}
