package com.compilandia.content.episodes

import com.compilandia.content.Episode
import com.compilandia.content.Fala
import com.compilandia.content.Temporada
import com.compilandia.learning.Region
import com.compilandia.learning.Skill

/** As aulas animadas. Cada assunto tem o básico; as pegadinhas, a prática e os clipes vêm por cima. */
object Episodes {
    val basicos: List<Episode> by lazy { aulasLongas + aulasLongas2 + lexicos + gramaticas + topDown + bottomUp + semantica + backend }
    val novos: List<Episode> by lazy { pegadinhasFrente + pegadinhasMeio + pegadinhasFim + pegadinhasNovas + praticas + tabelas + serieEmC }
    val clipes: List<Episode> by lazy { clipesTodos }

    /** Todas as aulas de verdade. Clipe fica de fora: ele mora no feed. */
    val all: List<Episode> by lazy { basicos + novos }

    fun of(s: Skill): Episode? = basicos.firstOrNull { it.skill == s }

    fun byId(id: String): Episode? = all.firstOrNull { it.id == id } ?: clipes.firstOrNull { it.id == id }

    /** A série em C é uma só e vai em ordem: ela aparece inteira em qualquer região. */
    fun daRegiao(r: Region, t: Temporada): List<Episode> =
        if (t == Temporada.EM_C) all.filter { it.temporada == t } else all.filter { it.skill.region == r && it.temporada == t }

    /** Toda fala das aulas, inclusive dicas e acertos, das perguntas e dos brinquedos. */
    fun falas(): List<Fala> = (all + clipes).flatMap { e ->
        e.passos.flatMap { p -> listOfNotNull(p.fala, p.ask?.hint, p.ask?.got, p.brinquedo?.dica, p.brinquedo?.acerto) }
    }.distinct() + Aberturas.falas()
}
