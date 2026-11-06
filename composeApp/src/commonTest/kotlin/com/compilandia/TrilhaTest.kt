package com.compilandia

import com.compilandia.content.Etapa
import com.compilandia.content.Guiado
import com.compilandia.content.EstadoEtapa
import com.compilandia.content.Library
import com.compilandia.content.SeuCompilador
import com.compilandia.content.Temporada
import com.compilandia.content.Trilha
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A1 e A5: nenhuma aula fica escondida, e pular nunca tranca nada. */
class TrilhaTest {
    private val t0 = 20_000L * 86_400_000L

    @Test fun nenhumaAulaForaDoCaminho() {
        val nasTrilhas = Skill.entries.flatMap { s -> Trilha.passos(s).mapNotNull { it.episodio?.id } + listOfNotNull(Trilha.outroJeito(s)?.id) }.toSet()
        val noCompilador = SeuCompilador.paradas().mapNotNull { it.episodio?.id }.toSet()
        val fora = Episodes.all.filter { it.id !in nasTrilhas && it.id !in noCompilador }
        assertTrue(fora.isEmpty(), "aulas sem lugar: ${fora.map { it.id }}")
    }

    @Test fun todoAssuntoComecaVendoETerminaNoDesafio() {
        for (s in Skill.entries) {
            val p = Trilha.passos(s)
            assertTrue(p.any { it.etapa == Etapa.VER }, "$s sem aula")
            assertEquals(Etapa.DESAFIO, p.last().etapa, "$s não termina no desafio")
        }
    }

    @Test fun seuCompiladorTemTudoEmOrdem() {
        val p = SeuCompilador.paradas()
        val aulas = p.mapNotNull { it.episodio }
        assertEquals(Episodes.all.filter { it.temporada == Temporada.EM_C }, aulas, "a série em C perdeu a ordem")
        assertEquals(Library.guide.map { it.number }.sorted(), p.mapNotNull { it.capitulo?.number }, "capítulos fora de ordem")
    }

    @Test fun continuarSegueAOrdemEPularAvanca() {
        val l = Learner(LearnerState(), Random(1))
        val s = Skill.FOLLOW
        val passos = Trilha.passos(s)
        assertEquals(passos.first(), Trilha.proximo(l, s))
        l.markEpisode(s)
        val depois = Trilha.proximo(l, s)!!
        assertTrue(depois.etapa != Etapa.BUG && depois.etapa != Etapa.VER, "aula vista conta o Bug e a aula: $depois")
        l.marcaEtapa(s, depois.chave, feita = false, t0)
        assertEquals(EstadoEtapa.PULADA, Trilha.estado(l, s, depois))
        assertTrue(Trilha.proximo(l, s) != depois, "pular não avançou")
    }

    @Test fun desafioJogadoFechaATrilha() {
        val l = Learner(LearnerState(), Random(1))
        val s = Skill.PHASES
        l.markEpisode(s)
        for (p in Trilha.passos(s)) if (p.episodio != null) l.markEpisodeId(p.episodio.id)
        assertFalse(Trilha.completa(l, s))
        l.recordAttempt(s, GameId.PIPELINE, 1, 1.0, null, 1000, 0, t0)
        assertNull(Trilha.proximo(l, s))
    }

    @Test fun pularOMesmoTipoTresVezesViraOpcional() {
        val l = Learner(LearnerState(), Random(1))
        val comPegadinha = Skill.entries.filter { s -> Trilha.passos(s).any { it.etapa == Etapa.PEGADINHA } }.take(4)
        for (s in comPegadinha.take(3)) {
            val peg = Trilha.passos(s).first { it.etapa == Etapa.PEGADINHA }
            l.marcaEtapa(s, peg.chave, feita = false, t0)
        }
        val quarto = comPegadinha[3]
        val peg = Trilha.passos(quarto).first { it.etapa == Etapa.PEGADINHA }
        assertTrue(Trilha.opcional(l, peg))
        l.markEpisode(quarto)
        assertTrue(Trilha.proximo(l, quarto)?.etapa != Etapa.PEGADINHA, "etapa opcional ainda no Continuar")
        assertEquals(EstadoEtapa.PENDENTE, Trilha.estado(l, quarto, peg), "opcional continua na trilha")
    }

    @Test fun fazerComAjudaTiraAAjudaAosPoucos() {
        val elegiveis = Skill.entries.filter { Guiado.disponivel(it) }
        println("Fazer com ajuda em: $elegiveis")
        assertTrue(Skill.FIRST in elegiveis && Skill.FOLLOW in elegiveis && Skill.LL1_TABLE in elegiveis, "faltam assuntos de algoritmo: $elegiveis")
        val sim = Guiado.problema(Skill.FIRST, 0, 42)!!
        val perguntas = sim.passos.indices.filter { sim.passos[it].ask != null }
        assertTrue(Guiado.exigir(sim, 0).isEmpty(), "no primeiro o Byte faz tudo")
        assertEquals(setOf(perguntas.last()), Guiado.exigir(sim, 1))
        assertEquals(perguntas.drop(1).toSet(), Guiado.exigir(sim, 2))
        assertEquals(perguntas.toSet(), Guiado.exigir(sim, 3))
        val outros = (0..3).map { Guiado.problema(Skill.FIRST, it, 42)!!.titulo + it }.toSet()
        assertEquals(4, outros.size)
    }

    @Test fun bancoDoFazerComAjudaSoTemGramaticaLL1() {
        for (txt in Guiado.GRAMATICAS_LL) {
            val g = com.compilandia.engine.Grammar.parse(txt)
            assertTrue(com.compilandia.engine.LL1Table(g).isLL1, "não é LL(1): $txt")
        }
    }

    @Test fun osQuatroProblemasNuncaSeRepetem() {
        for (s in Skill.entries.filter { Guiado.disponivel(it) }) for (semente in listOf(1L, 42L, 777L)) {
            val ps = Guiado.problemas(s, semente)
            assertEquals(4, ps.size, "$s com semente $semente")
            assertEquals(4, ps.map { p -> p.passos.joinToString("|") { it.say } }.toSet().size, "$s repetiu problema com semente $semente")
        }
    }

    @Test fun fazerVemDepoisDeVer() {
        val p = Trilha.passos(Skill.FIRST).map { it.etapa }
        assertEquals(p.indexOf(Etapa.VER) + 1, p.indexOf(Etapa.FAZER))
    }

    @Test fun trilhaCompletaEsquecidaOfereceRevisaoComClipeQueMuda() {
        val l = Learner(LearnerState(), Random(1))
        val s = Skill.FIRST
        l.markEpisode(s)
        for (p in Trilha.passos(s)) l.marcaEtapa(s, p.chave, feita = true, t0)
        l.recordAttempt(s, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, t0)
        assertNull(Trilha.proximo(l, s), "sem relógio, nada de revisão")
        assertNull(Trilha.proximo(l, s, t0), "memória fresca não pede revisão")
        val depois = t0 + 60L * 86_400_000L
        val r = Trilha.proximo(l, s, depois)
        assertEquals(Etapa.REVISAO, r?.etapa)
        assertEquals(Temporada.CLIPE, r?.episodio?.temporada)
        l.recordAttempt(s, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, depois)
        val r2 = Trilha.revisao(l, s)
        assertTrue(r2?.episodio?.id != r?.episodio?.id, "a revisão seguinte troca de clipe")
    }

    @Test fun saveAntigoAbreSemEtapas() {
        val l = Learner(LearnerState(episodesSeen = listOf("TOKENS", "REGEX_DFA")), Random(1))
        assertTrue(SeuCompilador.aberta(l))
        assertEquals(EstadoEtapa.FEITA, Trilha.estado(l, Skill.TOKENS, Trilha.passos(Skill.TOKENS).first { it.etapa == Etapa.VER }))
    }
}
