package com.compilandia

import com.compilandia.content.CUIDADO_ID
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Skill
import com.compilandia.ui.screens.partesDaAula
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Seção 3 da revisão: um "diga por quê" no fim de cada Sua vez, e o julgamento do colega com o motivo.
 * Ambiguidade já pergunta "por que o vezes fica mais fundo?" dentro da Sua vez.
 */
class DigaPorQueTest {
    private val basicos = Episodes.all.filter { it.temporada == Temporada.BASICO }

    @Test fun cadaSuaVezTerminaComUmPorQue() {
        for (e in basicos.filter { it.skill != Skill.AMBIGUITY }) {
            val k = e.passos.indexOfFirst { p -> p.shot.boxes.any { it.id == com.compilandia.content.PORQUE_ID } }
            assertTrue(k > 0, "${e.skill}: sem o diga por quê")
            val a = e.passos[k].ask!!
            assertEquals(setOf("pq0"), a.right, "${e.skill}")
            assertEquals(setOf("pq1", "pq2"), a.wrong, "${e.skill}")
            // Logo depois vem o julgamento do colega, que abre a parte Erros.
            assertTrue(e.passos[k + 1].shot.boxes.any { it.id == CUIDADO_ID }, "${e.skill}: o por quê não fecha a Sua vez")
            val partes = partesDaAula(e.passos, e.inicioDoExemplo)
            if (partes != null) assertTrue(k in partes[3], "${e.skill}: o por quê caiu fora da Sua vez ($k, $partes)")
            val textos = e.passos[k].shot.boxes.filter { it.id.matches(Regex("pq\\d")) }.map { it.text }
            assertEquals(3, textos.toSet().size, "${e.skill}: razões repetidas")
        }
    }

    @Test fun oColegaSeJulgaComOMotivo() {
        for (e in basicos) {
            val p = e.passos.first { p -> p.ask != null && p.shot.boxes.any { it.id == CUIDADO_ID } }
            val a = p.ask!!
            assertEquals(2, a.wrong.size, "${e.skill}: o julgamento ainda tem só certo ou errado")
            val opcoes = (a.right + a.wrong).map { id -> p.shot.boxes.first { it.id == id }.text }
            assertEquals(3, opcoes.toSet().size, "${e.skill}: opções repetidas")
            // A errada de mesmo veredito começa igual à certa (Certo: ... ou Errado: ...), a outra não.
            val certa = p.shot.boxes.first { it.id in a.right }.text.substringBefore(":")
            assertEquals(1, a.wrong.count { id -> p.shot.boxes.first { it.id == id }.text.substringBefore(":") == certa }, "${e.skill}")
        }
    }
}
