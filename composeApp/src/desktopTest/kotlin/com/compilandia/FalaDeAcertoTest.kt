package com.compilandia

import com.compilandia.content.episodes.Episodes
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A fala de acerto não pode virar ruído. Se toda resposta certa começa com "Isso", o aluno
 * para de ouvir o começo e, com ele, a explicação que vem depois (revisão das aulas, padrão 1).
 */
class FalaDeAcertoTest {
    /** A abertura é o trecho antes do primeiro ponto, se for curto ("Certo.", "É isso."); frase comprida não é abertura. */
    private fun abertura(t: String): String? {
        val corte = t.trim().trimStart('*').split('.', '!', ':').first().trim().lowercase()
        return corte.takeIf { it.split(' ').size <= 2 }
    }

    private fun acertos() = Episodes.all.map { e ->
        e.skill.name to e.passos.mapNotNull { p -> p.ask?.got?.text ?: p.brinquedo?.acerto?.text }
    }

    @Test fun duasSeguidasNaoComecamIgual() {
        val ruins = acertos().flatMap { (skill, falas) ->
            falas.zipWithNext().filter { (a, b) -> abertura(a) != null && abertura(a) == abertura(b) }.map { (a, b) -> "$skill: \"$a\" / \"$b\"" }
        }
        assertTrue(ruins.isEmpty(), "falas de acerto seguidas com o mesmo começo:\n" + ruins.joinToString("\n"))
    }

    @Test fun issoNoComecoEhMinoria() {
        val todas = acertos().flatMap { it.second }
        val isso = todas.filter { abertura(it) == "isso" }
        assertTrue(isso.size * 5 <= todas.size, "${isso.size} de ${todas.size} acertos começam com Isso (máximo: 20%)")
    }
}
