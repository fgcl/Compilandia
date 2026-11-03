package com.compilandia.learning

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EconomyTest {
    private val dia = 86_400_000L
    private val t0 = 20_000L * dia

    @Test fun jogarRendeEngrenagens() {
        val l = Learner(LearnerState(), Random(1))
        l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 2, 1.0, null, 1000, 0, t0)
        assertEquals(3 + 9 + 2, l.state.gears)
    }

    @Test fun comprarDesconta() {
        val l = Learner(LearnerState(gears = 50), Random(1))
        assertTrue(l.buy(RoomItem.ABAJUR))
        assertEquals(20, l.state.gears)
        assertTrue(l.owns(RoomItem.ABAJUR))
        assertFalse(l.buy(RoomItem.ABAJUR), "não compra duas vezes")
        assertFalse(l.buy(RoomItem.SOFA), "sem engrenagens")
    }

    @Test fun premioOfereceOQueDaParaComprar() {
        assertNull(Learner(LearnerState(gears = 5), Random(1)).affordableItem())
        assertNotNull(Learner(LearnerState(gears = 40), Random(1)).affordableItem())
    }

    @Test fun metasPagamUmaVezPorDia() {
        val l = Learner(LearnerState(), Random(1))
        l.noteCombo(3, t0)
        assertEquals(1, l.claimQuests(t0))
        assertEquals(QUEST_GEARS, l.state.gears)
        assertEquals(0, l.claimQuests(t0), "já pagou hoje")
        assertEquals(0f, l.quests(t0 + dia).first { it.id == "combo" }.progress, "dia novo zera")
    }

    @Test fun travaSalvaASequencia() {
        val l = Learner(LearnerState(streakFreezes = 1), Random(1))
        l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 1, 1.0, null, 1000, 0, t0)
        l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 1, 1.0, null, 1000, 0, t0 + dia)
        assertEquals(2, l.state.streakDays)
        l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 1, 1.0, null, 1000, 0, t0 + 3 * dia)
        assertEquals(3, l.state.streakDays, "um dia perdido, uma trava gasta")
        assertEquals(0, l.state.streakFreezes)
        l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 1, 1.0, null, 1000, 0, t0 + 5 * dia)
        assertEquals(1, l.state.streakDays, "sem trava, recomeça")
    }

    @Test fun primeiroPassoEAulaDepoisJogo() {
        val l = Learner(LearnerState(), Random(1))
        val a = l.nextStep(t0)
        assertTrue(a.episode)
        assertTrue(l.markEpisode(a.skill))
        assertFalse(l.markEpisode(a.skill), "só paga uma vez")
        assertFalse(l.nextStep(t0).skill == a.skill && l.nextStep(t0).episode)
    }

    @Test fun saveAntigoAbreEGanhaEngrenagens() {
        val l = Learner.load("""{"name":"Ana","xp":800,"onboardingDone":true}""")
        assertEquals("Ana", l.state.name)
        assertEquals(100, l.state.gears)
        assertTrue(l.state.voiceOn)
        val de = Learner.load(l.save())
        assertEquals(100, de.state.gears, "semente só uma vez")
    }

    @Test fun todoMovelTemPreco() {
        assertTrue(RoomItem.entries.all { it.price > 0 })
        assertEquals(RoomItem.entries.size, RoomItem.entries.distinctBy { it.id }.size)
    }
}
