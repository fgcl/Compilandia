package com.compilandia

import com.compilandia.textos.Txt
import com.compilandia.textos.tx
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Os textos moram em src/commonMain/textos. Estes testes pegam chave errada antes de ela virar "chave_crua" na tela. */
class TextosTest {
    private val fontes = File("src/commonMain/kotlin")
    private val chamada = Regex("""\b(?:tx\(|Txt\.em\([^,()]+,\s*)"([a-z0-9_]+)"""")

    private fun usadas(): Map<String, List<String>> {
        val m = mutableMapOf<String, MutableList<String>>()
        fontes.walkTopDown().filter { it.extension == "kt" && "/textos/" !in it.invariantSeparatorsPath }.forEach { f ->
            for (r in chamada.findAll(f.readText())) m.getOrPut(r.groupValues[1]) { mutableListOf() } += f.name
        }
        return m
    }

    @Test fun todaChaveUsadaExiste() {
        val faltam = usadas().filterKeys { !Txt.existe(it) }
        assertTrue(faltam.isEmpty(), "chaves sem texto: ${faltam.entries.take(5)}")
    }

    @Test fun nenhumaChaveSobrando() {
        val usadas = usadas().keys
        val sobram = Txt.chaves.filter { it !in usadas }
        assertTrue(sobram.isEmpty(), "chaves que ninguém usa (${sobram.size}): ${sobram.take(8)}")
    }

    @Test fun traducaoTemOsMesmosMarcadores() {
        val marcador = Regex("%\\d+\\\$s")
        for (k in Txt.traduzidas("en")) {
            val pt = marcador.findAll(Txt.pt(k)).map { it.value }.toSet()
            Txt.idioma = "en"
            val en = marcador.findAll(Txt[k]).map { it.value }.toSet()
            Txt.idioma = "pt"
            assertEquals(pt, en, "marcadores de $k")
        }
    }

    @Test fun formataComoOTemplateDoKotlin() {
        val n = 3
        assertEquals("Meta do dia cumprida: +$n engrenagens", tx("appstate_meta_dia_cumprida_1_engrenagens", n))
        assertEquals("100%", Txt.formata("%1\$s%", arrayOf(100)))
        assertEquals("% de %", Txt.formata("% de %", arrayOf(1)))
        assertEquals("b a", Txt.formata("%2\$s %1\$s", arrayOf("a", "b")))
    }

    /** S2: toda chave traduzível tem inglês. Código e gramática ficam com translatable="false". */
    @Test fun todaChaveTemIngles() {
        val en = Txt.traduzidas("en")
        val faltam = Txt.chaves.filter { Txt.traduzivel(it) && it !in en }
        assertTrue(faltam.isEmpty(), "chaves sem inglês (${faltam.size}): ${faltam.take(8)}")
    }

    /**
     * O id da fala e da aula nasce do português (Txt.paraPt). Se duas frases diferentes viram
     * a mesma frase em inglês, a volta erra e o "não entendi" cai na fala errada.
     */
    @Test fun frasesDiferentesNaoViramAMesma() {
        val porTexto = mutableMapOf<String, MutableSet<String>>()
        for (k in Txt.traduzidas("en")) {
            val en = Txt.em("en", k)
            if (en.split(' ').size < 4) continue
            porTexto.getOrPut(en) { mutableSetOf() } += Txt.pt(k)
        }
        val ruins = porTexto.filterValues { it.size > 1 }
        assertTrue(ruins.isEmpty(), "frases que viraram a mesma em inglês (${ruins.size}): ${ruins.entries.take(5)}")
    }

    @Test fun idiomaInicial() {
        assertEquals("en", Txt.idiomaInicial(null, "en"))
        assertEquals("pt", Txt.idiomaInicial(null, "pt"))
        assertEquals("en", Txt.idiomaInicial(null, "de"))
        // Save de antes da escolha continua em português, qualquer que seja o aparelho.
        assertEquals("pt", Txt.idiomaInicial("{\"name\":\"Felipe\"}", "en"))
        assertEquals("pt", Txt.idiomaInicial("{\"name\":\"Felipe\",\"idioma\":\"\"}", "en"))
        assertEquals("en", Txt.idiomaInicial("{\"idioma\": \"en\"}", "pt"))
    }
}
