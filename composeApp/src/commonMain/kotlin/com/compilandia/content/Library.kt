package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.learning.Style

object Library {
    val lessons: List<Lesson> by lazy { lessonsPart1 + lessonsPart2 + lessonsPart3 + lessonsPart4 + lessonsPart5 + lessonsPart6 }
    val cards: List<SkillCard> by lazy { cardsPart1 + cardsPart2 + cardsPart3 + cardsPart4 + cardsPart5 + cardsPart6 }
    val guide: List<GuideChapter> by lazy { tpGuide }

    fun lesson(skill: Skill, style: Style): Lesson? = lessons.firstOrNull { it.skill == skill && it.style == style }
    fun stylesFor(skill: Skill): List<Style> = lessons.filter { it.skill == skill }.map { it.style }.distinct()
    fun card(skill: Skill): SkillCard? = cards.firstOrNull { it.skill == skill }
}
