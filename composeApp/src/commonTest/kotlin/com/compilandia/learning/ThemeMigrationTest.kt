package com.compilandia.learning

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeMigrationTest {
    private fun loadTheme(json: String) = Learner.load(json).state.theme

    @Test fun padraoAntigoVaiParaOficina() {
        assertEquals("OFICINA", loadTheme("""{"theme":"CLARO"}"""))
        assertEquals("OFICINA", loadTheme("""{"theme":"ESCURO"}"""))
    }

    @Test fun outrasEscolhasFicam() {
        assertEquals("SEPIA", loadTheme("""{"theme":"SEPIA"}"""))
        assertEquals("NOITE_AMBAR", loadTheme("""{"theme":"NOITE_AMBAR"}"""))
    }

    @Test fun escolhaNovaDeClaroSobreviveAoRecarregar() {
        val l = Learner()
        l.setTheme("CLARO")
        assertEquals("CLARO", Learner.load(l.save()).state.theme)
    }
}
