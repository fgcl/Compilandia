package com.compilandia.learning

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LearningTest {
    private val day = 86_400_000L

    @Test fun bktRisesWithSuccess() {
        var b = BKT()
        repeat(6) { b = b.update(1.0) }
        assertTrue(b.pKnow > 0.9, "${b.pKnow}")
        var w = BKT(pKnow = 0.8)
        repeat(4) { w = w.update(0.0) }
        assertTrue(w.pKnow < 0.5)
    }

    @Test fun spacingGrowsStability() {
        var m = Memory()
        var t = 0L
        m = m.review(t, 1.0)
        val s1 = m.stability
        t += (m.intervalDays() * day).toLong()
        m = m.review(t, 1.0)
        assertTrue(m.stability > s1 * 1.5, "${m.stability} vs $s1")
        val forgot = m.review(t + day, 0.0)
        assertTrue(forgot.stability < m.stability)
        assertTrue(m.retrievability(t + (30 * day)) < 0.9)
    }

    /** Revisão só conta com acerto: errar uma revisão devida deixa o assunto pendente até acertar; errar na primeira vez, não. */
    @Test fun revisaoExigeAcerto() {
        var t = 0L
        val primeiroErro = Memory().review(t, 0.0)
        assertTrue(!primeiroErro.isDue(t + 1000))
        var m = Memory().review(t, 1.0)
        t += (40 * day).toLong()
        assertTrue(m.isDue(t))
        m = m.review(t, 0.2)
        assertTrue(m.isDue(t + 1000), "errou a revisão: continua pendente")
        m = m.review(t + 2000, 0.1)
        assertTrue(m.isDue(t + 3000), "errou de novo: continua pendente")
        m = m.review(t + 4000, 1.0)
        assertTrue(!m.isDue(t + 5000), "acertou: revisado")
    }

    @Test fun tinyNetLearnsPattern() {
        var n = TinyNet.init(1)
        val rnd = Random(3)
        repeat(3000) {
            val x = DoubleArray(TinyNet.FEATURES) { rnd.nextDouble() }
            x[TinyNet.FEATURES - 1] = 1.0
            val y = if (x[0] > 0.5) 1.0 else 0.0
            n = n.train(x, y)
        }
        val hi = DoubleArray(TinyNet.FEATURES) { 0.5 }.also { it[0] = 0.9; it[9] = 1.0 }
        val lo = DoubleArray(TinyNet.FEATURES) { 0.5 }.also { it[0] = 0.1; it[9] = 1.0 }
        assertTrue(n.predict(hi) > 0.8 && n.predict(lo) < 0.2, "${n.predict(hi)} ${n.predict(lo)}")
    }

    @Test fun banditPrefersRewardedStyle() {
        val l = Learner(rnd = Random(4))
        repeat(15) {
            l.recordLessonViewed(Skill.FIRST, Style.VISUAL, true)
            l.recordLessonViewed(Skill.FIRST, Style.FORMAL, false)
        }
        val picks = (1..50).map { l.chooseStyle(Skill.FIRST) }
        assertTrue(picks.count { it == Style.VISUAL } > picks.count { it == Style.FORMAL })
        val next = l.notUnderstood(Skill.FIRST, Style.VISUAL, emptySet(), 0)
        assertNotEquals(Style.VISUAL, next)
        val g = l.bored(Skill.TOKENS, GameId.TOKEN_SLICER, 0)
        assertNotEquals(GameId.TOKEN_SLICER, g)
    }

    @Test fun missionsAndPersistence() {
        val l = Learner(LearnerState(gearsSeeded = true), Random(5))
        val now = 100 * day
        val m = l.nextMission(now)
        assertTrue(m.skill.prereqs.isEmpty() || l.prereqsMet(m.skill))
        repeat(5) { l.recordAttempt(Skill.PHASES, GameId.PIPELINE, 2, 1.0, 0.9, 30_000, 0, now) }
        assertTrue(l.mastery(Skill.PHASES) > 0.6)
        val mix = l.dailyMix(now, 4)
        assertEquals(4, mix.map { it.skill }.distinct().size)
        val restored = Learner.load(l.save())
        assertEquals(l.state, restored.state)
        assertTrue(Coach.insights(restored, now).isNotEmpty())
        assertEquals(5, l.calibration().samples)
        // depois de muito tempo, PHASES aparece como revisão
        assertTrue(l.dueSkills(now + 60 * day).any { it.first == Skill.PHASES })
    }

    @Test fun everySkillHasTwoGames() {
        for (s in Skill.entries) assertTrue(GameId.forSkill(s).isNotEmpty(), s.name)
        for (s in Skill.entries) assertTrue(GameId.forSkill(s).size >= 2, "${s.name} precisa de 2 jogos")
    }

    @Test fun examPlanAndReset() {
        val l = Learner(rnd = Random(9))
        val now = 200 * day
        l.setExamDay(now / day + 5)
        assertEquals(5L, l.daysToExam(now))
        repeat(6) { l.recordAttempt(Skill.FIRST, GameId.FIRST_FOLLOW_ALCHEMY, 2, 1.0, 0.9, 1000, 0, now) }
        assertTrue(Skill.FIRST !in l.examPlan(now))
        l.boredLesson(Skill.FIRST, Style.FORMAL, now)
        assertTrue(l.state.feedback.any { it.kind == "aula_chata" })
        l.defeatBoss(Region.PORTO)
        assertTrue(Achievements.all(l).first { it.title == "Primeiro passo" }.unlocked)
        l.reset()
        assertEquals(0, l.state.history.size)
        assertTrue(l.state.onboardingDone)
        assertEquals("aeiouc", normalizeForMatch("ÁÉÍÓÚç"))
    }
}
