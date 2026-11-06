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
    private val chamada = Regex("""\btx\("([a-z0-9_]+)"""")

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
}
