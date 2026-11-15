package com.compilandia

import com.compilandia.content.Album
import com.compilandia.content.FichaDoTexto
import com.compilandia.content.Library
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Aberturas
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertTrue

/** Ficha e Bugs cobram o que a aula animada ensina, ou o cartão leva a marca "Vem da aula Ler com calma". */
class FichaDoTextoTest {
    private fun animada(s: Skill): String =
        (Episodes.all.filter { it.skill == s && it.temporada != Temporada.EM_C } + Episodes.clipes.filter { it.skill == s })
            .joinToString("\n") { it.passos.toString() } + Aberturas.of(s).toString()

    private fun texto(s: Skill) = Library.lessons.filter { it.skill == s }.joinToString("\n") { it.beats.toString() }

    @Test fun cadaMarcaEstaNoCartaoENoTextoENaoNaAnimada() {
        for ((s, marcas) in FichaDoTexto.marcas) {
            val cartoes = Library.card(s)!!.flashcards
            val anim = animada(s)
            val txt = texto(s)
            for (m in marcas) {
                assertTrue(cartoes.any { (p, r) -> "$p\n$r".contains(m.noCartao, ignoreCase = true) }, "$s: nenhum cartão com '${m.noCartao}'")
                assertTrue(!anim.contains(m.noCartao, ignoreCase = true), "$s: '${m.noCartao}' já está na aula animada; tire a marca")
                assertTrue(!anim.contains(m.noTexto, ignoreCase = true), "$s: '${m.noTexto}' já está na aula animada; tire a marca")
                assertTrue(txt.contains(m.noTexto, ignoreCase = true), "$s: '${m.noTexto}' não está na aula de texto")
            }
        }
    }

    @Test fun bugsSoCobramOQueAAulaAnimadaEnsina() {
        for (e in Album.especies) {
            val marcas = FichaDoTexto.marcas[e.skill].orEmpty()
            for (t in e.testes) for (m in marcas) {
                assertTrue(!t.texto.contains(m.noCartao, ignoreCase = true), "${e.id}: o Bug cobra '${m.noCartao}', que só a aula de texto ensina: ${t.texto}")
            }
        }
    }

    @Test fun aMarcaApareceNosCartoesCertos() {
        val ficha = Library.card(Skill.OPTIMIZATION)!!.flashcards
        val marcados = ficha.filter { (p, r) -> FichaDoTexto.doTexto(Skill.OPTIMIZATION, p, r) }
        // Desde a 27ª rodada, blocos básicos e líderes estão na aula animada; redução de força continua só no texto.
        assertTrue(marcados.none { "líder" in it.first }, "o líder agora está na aula animada")
        assertTrue(marcados.isNotEmpty(), "ainda há cartão de Otimização que vem do texto")
        assertTrue(marcados.none { it.first.startsWith("Dobra") }, "dobra de constantes está na aula animada")
        val total = Library.cards.sumOf { c -> c.flashcards.count { (p, r) -> FichaDoTexto.doTexto(c.skill, p, r) } }
        println("Cartões marcados: $total de ${Library.cards.sumOf { it.flashcards.size }}")
    }
}
