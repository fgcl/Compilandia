package com.compilandia

import com.compilandia.content.Ideias
import com.compilandia.content.Lembretes
import com.compilandia.content.Retomadas
import com.compilandia.learning.GameId
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** R1 a R4: retomada, revisão por ideia e lembrete. */
class RevisaoTest {
    private val t0 = 1_700_000_000_000L
    private val dia = 86_400_000L

    private fun estudou(): Learner {
        val l = Learner(LearnerState(), Random(1))
        l.markEpisode(Skill.FIRST)
        l.markEpisode(Skill.FOLLOW)
        l.recordAttempt(Skill.FIRST, GameId.FIRST_FOLLOW_ALCHEMY, 1, 1.0, null, 1000, 0, t0)
        l.recordAttempt(Skill.FOLLOW, GameId.FIRST_FOLLOW_ALCHEMY, 1, 0.5, null, 1000, 0, t0)
        l.marcaVisita(t0)
        return l
    }

    @Test fun retomadaSoDepoisDeDoisDias() {
        val l = estudou()
        assertNull(Retomadas.monta(l, t0 + dia), "um dia não chega")
        val r = assertNotNull(Retomadas.monta(l, t0 + 3 * dia))
        assertEquals(3, r.itens.size)
        assertEquals(listOf(Skill.FOLLOW, Skill.FIRST), r.ultimos)
        assertEquals("Da última vez você viu FOLLOW e FIRST.", Retomadas.abertura(r).replace(Skill.FOLLOW.title, "FOLLOW").replace(Skill.FIRST.title, "FIRST"))
        assertTrue(r.itens.all { it.skill in setOf(Skill.FIRST, Skill.FOLLOW) }, "só assuntos estudados")
        assertNull(Retomadas.monta(Learner(LearnerState(), Random(1)), t0), "primeira visita não tem retomada")
    }

    @Test fun ideiaComecaComAMemoriaDoAssuntoEMudaSozinha() {
        val l = estudou()
        val i = Ideias.de(Skill.FIRST).first()
        assertEquals(l.skill(Skill.FIRST).memory, l.memoriaIdeia(i.id, Skill.FIRST), "save antigo: ideia herda o assunto")
        val antes = l.skill(Skill.FIRST).memory
        l.revisaIdeia(i.id, Skill.FIRST, 0.0, t0 + 5 * dia)
        assertEquals(antes, l.skill(Skill.FIRST).memory, "o assunto inteiro não muda")
        // Errou uma ideia e acertou a outra no mesmo dia: a errada esquece mais rápido e volta antes.
        val outra = Ideias.de(Skill.FIRST)[1]
        l.revisaIdeia(outra.id, Skill.FIRST, 1.0, t0 + 5 * dia)
        val depois = t0 + 9 * dia
        assertTrue(Ideias.retencao(l, i, depois) < Ideias.retencao(l, outra, depois))
    }

    @Test fun todoAssuntoTemIdeia() {
        for (s in Skill.entries) assertTrue(Ideias.de(s).size >= 3, "${s.name} sem ideias")
        assertEquals(Ideias.todas.size, Ideias.todas.map { it.id }.toSet().size)
    }

    @Test fun lembreteSoComRevisaoPendenteETrocaDeTexto() {
        val l = estudou()
        assertNull(Lembretes.texto(l, t0), "memória fresca: celular quieto")
        val a = assertNotNull(Lembretes.texto(l, t0 + 30 * dia))
        val b = assertNotNull(Lembretes.texto(l, t0 + 31 * dia))
        assertTrue(a != b, "o texto muda de um dia para o outro")
        assertTrue(Skill.FOLLOW.title in a, "cita o assunto mais esquecido: $a")
    }

    @Test fun saveAntigoAbreComLembreteDesligado() {
        val l = Learner.load("""{"name":"Ana","xp":10}""")
        assertEquals(false, l.state.lembreteLigado)
        assertEquals(0L, l.state.ultimaVisitaMs)
        assertTrue(l.state.ideias.isEmpty())
    }
}
