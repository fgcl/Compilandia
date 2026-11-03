package com.compilandia

import androidx.compose.ui.graphics.Color
import com.compilandia.learning.Region
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeMode
import com.compilandia.ui.ThemeState
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/** Todo tema precisa ser legível: texto normal pede 4,5:1 (WCAG AA). */
class ThemeContrastTest {
    private val aa = 4.5

    @AfterTest fun restore() { ThemeState.mode = ThemeMode.OFICINA }

    private fun forEachTheme(check: (ThemeMode, MutableList<String>) -> Unit) {
        val problems = mutableListOf<String>()
        for (t in ThemeMode.entries) {
            ThemeState.mode = t
            check(t, problems)
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    private fun MutableList<String>.need(t: ThemeMode, what: String, a: Color, b: Color, min: Double = aa) {
        val c = Palette.contrast(a, b)
        if (c < min) add("$t: $what = ${(c * 100).toInt() / 100.0}")
    }

    @Test fun textoSobreFundos() = forEachTheme { t, p ->
        for ((name, bg) in listOf("paper" to Palette.paper, "paperDark" to Palette.paperDark, "surface" to Palette.white)) {
            p.need(t, "ink/$name", Palette.ink, bg)
            p.need(t, "inkSoft/$name", Palette.inkSoft, bg)
            for ((n, c) in listOf("orange" to Palette.orange, "teal" to Palette.teal, "purple" to Palette.purple, "red" to Palette.red,
                "green" to Palette.green, "blue" to Palette.blue, "pink" to Palette.pink, "nonTerminal" to Palette.nonTerminal, "terminal" to Palette.terminal)) {
                p.need(t, "$n/$name", c, bg)
            }
        }
    }

    @Test fun textoSobreCoresFortes() = forEachTheme { t, p ->
        val strong = listOf(Palette.action, Palette.orange, Palette.teal, Palette.purple, Palette.yellow, Palette.red, Palette.green,
            Palette.blue, Palette.pink, Palette.drawer) + Region.entries.map { Palette.region(it) }
        strong.forEachIndexed { i, c -> p.need(t, "on($i)", Palette.on(c), c) }
    }

    @Test fun gavetas() = forEachTheme { t, p ->
        p.need(t, "etiqueta", Palette.tagInk, Palette.tag)
        p.need(t, "faixa", Palette.hazardDark, Palette.hazard)
        p.need(t, "gaveta aberta (número)", Palette.hazard, Palette.hazardDark)
    }

    @Test fun altoContrasteDeVerdade() {
        ThemeState.mode = ThemeMode.CONTRASTE
        val c = ThemeState.colors
        assertTrue(Palette.contrast(c.ink, c.paper) >= 15.0)
        assertTrue(Palette.contrast(Palette.on(c.action), c.action) >= 7.0)
    }

    @Test fun noiteAmbarSemAzul() {
        val c = ThemeState.colorsFor(ThemeMode.NOITE_AMBAR)
        val all = listOf(c.paper, c.paperDark, c.ink, c.inkSoft, c.surface, c.action, c.orange, c.teal, c.purple, c.yellow, c.red,
            c.green, c.blue, c.pink, c.nonTerminal, c.terminal, c.drawer, c.drawerHi, c.drawerLo, c.tag, c.tagInk, c.hazard, c.hazardDark, c.onStrong) + c.regions
        for (x in all) assertTrue(x.blue <= x.red, "cor com azul acima do vermelho: $x")
    }
}
