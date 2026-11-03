package com.compilandia.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.compilandia.learning.Region
import com.compilandia.resources.Res
import com.compilandia.resources.atkinson_bold
import com.compilandia.resources.atkinson_regular
import com.compilandia.resources.barlow_condensed_bold
import com.compilandia.resources.barlow_condensed_semibold
import com.compilandia.resources.space_mono_bold
import com.compilandia.resources.space_mono_regular
import org.jetbrains.compose.resources.Font

/** Temas da oficina do Byte (escolha em Ajustes). */
enum class ThemeMode(val title: String, val description: String) {
    OFICINA("Oficina", "Metal escuro da bancada do Byte, padrão"),
    CLARO("Galpão claro", "Chapa clara, bom sob luz forte"),
    SEPIA("Sépia", "Tons quentes, menos azul, bom para ler muito"),
    NOITE_AMBAR("Noite âmbar", "Escuro e quase sem luz azul, para estudar antes de dormir"),
    CONTRASTE("Alto contraste", "Preto, branco e amarelo, máxima legibilidade"),
}

/**
 * Todas as cores de um tema. As de destaque (orange, teal...) servem tanto de fundo quanto de texto sobre
 * paper/surface: ThemeContrastTest garante o contraste. Texto sobre qualquer cor: use [Palette.on].
 */
data class ThemeColors(
    val paper: Color, val paperDark: Color, val ink: Color, val inkSoft: Color, val surface: Color, val dark: Boolean,
    /** Botão principal. */
    val action: Color,
    val orange: Color, val teal: Color, val purple: Color, val yellow: Color, val red: Color, val green: Color, val blue: Color, val pink: Color,
    val nonTerminal: Color, val terminal: Color,
    /** Gaveta: corpo, luz na borda de cima, sombra na borda de baixo. */
    val drawer: Color, val drawerHi: Color, val drawerLo: Color,
    /** Etiqueta das gavetas. */
    val tag: Color, val tagInk: Color,
    /** Faixa de perigo e gaveta aberta. */
    val hazard: Color, val hazardDark: Color,
    /** Texto claro sobre cor forte (o tema âmbar evita o branco, que tem azul). */
    val onStrong: Color,
    /** Uma cor por região, na ordem de [Region]. */
    val regions: List<Color>,
)

object ThemeState {
    var mode by mutableStateOf(ThemeMode.OFICINA)
    var fontScale by mutableStateOf(1f)
    var reduceMotion by mutableStateOf(false)
    val colors: ThemeColors get() = colorsFor(mode)
    fun colorsFor(m: ThemeMode): ThemeColors = when (m) {
        ThemeMode.OFICINA -> OFICINA
        ThemeMode.CLARO -> CLARO
        ThemeMode.SEPIA -> SEPIA
        ThemeMode.NOITE_AMBAR -> NOITE_AMBAR
        ThemeMode.CONTRASTE -> CONTRASTE
    }

    private val OFICINA = ThemeColors(
        paper = Color(0xFF2B3336), paperDark = Color(0xFF222A2C), ink = Color(0xFFF2EEE3), inkSoft = Color(0xFFC3CAC6), surface = Color(0xFF364146), dark = true,
        action = Color(0xFFC8432A),
        orange = Color(0xFFFFA477), teal = Color(0xFF5FD0BE), purple = Color(0xFFE8B08C), yellow = Color(0xFFF5B700),
        red = Color(0xFFFF9A85), green = Color(0xFF7ED99A), blue = Color(0xFF8CC4EE), pink = Color(0xFFF4A6C4),
        nonTerminal = Color(0xFFFFB59E), terminal = Color(0xFF7EDCCB),
        drawer = Color(0xFF5E7076), drawerHi = Color(0xFF7F939A), drawerLo = Color(0xFF3F4E53),
        tag = Color(0xFFF2EEE3), tagInk = Color(0xFF1F2628),
        hazard = Color(0xFFF5B700), hazardDark = Color(0xFF1F2628), onStrong = Color(0xFFFFFFFF),
        regions = listOf(0xFF1F6F8F, 0xFF2E7D4F, 0xFFD9920F, 0xFF5F7330, 0xFFB23A2A, 0xFF4E6169, 0xFF9A5A1E, 0xFFA83A63, 0xFF1E6E62).map { Color(it) },
    )
    private val CLARO = ThemeColors(
        paper = Color(0xFFE9E6DC), paperDark = Color(0xFFD9D5C8), ink = Color(0xFF1F2628), inkSoft = Color(0xFF4C585C), surface = Color(0xFFF7F5EE), dark = false,
        action = Color(0xFFC8432A),
        orange = Color(0xFF9A3618), teal = Color(0xFF09605A), purple = Color(0xFF7A3E6E), yellow = Color(0xFFF5B700),
        red = Color(0xFFA8231C), green = Color(0xFF176230), blue = Color(0xFF1F5F99), pink = Color(0xFFA82A63),
        nonTerminal = Color(0xFF9A3618), terminal = Color(0xFF09605A),
        drawer = Color(0xFF5F7076), drawerHi = Color(0xFF8FA1A7), drawerLo = Color(0xFF4D5C61),
        tag = Color(0xFFF7F5EE), tagInk = Color(0xFF1F2628),
        hazard = Color(0xFFF5B700), hazardDark = Color(0xFF1F2628), onStrong = Color(0xFFFFFFFF),
        regions = listOf(0xFF1F6F8F, 0xFF2E7D4F, 0xFFD9920F, 0xFF5F7330, 0xFFB23A2A, 0xFF4E6169, 0xFF9A5A1E, 0xFFA83A63, 0xFF1E6E62).map { Color(it) },
    )
    private val SEPIA = ThemeColors(
        paper = Color(0xFFF1E7D0), paperDark = Color(0xFFE3D5B5), ink = Color(0xFF332818), inkSoft = Color(0xFF5E4E38), surface = Color(0xFFFAF3E3), dark = false,
        action = Color(0xFFA93E1C),
        orange = Color(0xFF8E4112), teal = Color(0xFF2F6653), purple = Color(0xFF7A4A33), yellow = Color(0xFFD99A0A),
        red = Color(0xFFA8321E), green = Color(0xFF405E1B), blue = Color(0xFF365A75), pink = Color(0xFF9A3F52),
        nonTerminal = Color(0xFF8E4112), terminal = Color(0xFF2F6653),
        drawer = Color(0xFF7A6750), drawerHi = Color(0xFFA89274), drawerLo = Color(0xFF64533B),
        tag = Color(0xFFFAF3E3), tagInk = Color(0xFF332818),
        hazard = Color(0xFFD99A0A), hazardDark = Color(0xFF332818), onStrong = Color(0xFFFFFFFF),
        regions = listOf(0xFF365A75, 0xFF405E1B, 0xFFC58A12, 0xFF6E6A2A, 0xFFA8321E, 0xFF6B5B4B, 0xFF8A5220, 0xFF9A3F52, 0xFF2F6653).map { Color(it) },
    )
    private val NOITE_AMBAR = ThemeColors(
        paper = Color(0xFF14110C), paperDark = Color(0xFF221B12), ink = Color(0xFFFFD9A0), inkSoft = Color(0xFFD9B88A), surface = Color(0xFF241D13), dark = true,
        action = Color(0xFFE08A3C),
        orange = Color(0xFFE8964A), teal = Color(0xFFC9A85A), purple = Color(0xFFD8986A), yellow = Color(0xFFE5B85C),
        red = Color(0xFFF0805A), green = Color(0xFFB7BC56), blue = Color(0xFFC79A6A), pink = Color(0xFFDE9A78),
        nonTerminal = Color(0xFFF0A868), terminal = Color(0xFFD4C070),
        drawer = Color(0xFF3A2E1E), drawerHi = Color(0xFF54432C), drawerLo = Color(0xFF1E170E),
        tag = Color(0xFFFFD9A0), tagInk = Color(0xFF14110C),
        hazard = Color(0xFFE5B85C), hazardDark = Color(0xFF14110C), onStrong = Color(0xFFFFE9C8),
        regions = listOf(0xFF7A4A1A, 0xFF6A5A1A, 0xFFC8932E, 0xFF5E5022, 0xFF8E3A1C, 0xFF54432C, 0xFF7E4A1A, 0xFF84462C, 0xFF62501E).map { Color(it) },
    )
    private val CONTRASTE = ThemeColors(
        paper = Color(0xFF000000), paperDark = Color(0xFF1A1A1A), ink = Color(0xFFFFFFFF), inkSoft = Color(0xFFE6E6E6), surface = Color(0xFF0D0D0D), dark = true,
        action = Color(0xFFFFD400),
        orange = Color(0xFFFFB000), teal = Color(0xFF00E5C8), purple = Color(0xFFFFB3E6), yellow = Color(0xFFFFD400),
        red = Color(0xFFFF8080), green = Color(0xFF5CFF8F), blue = Color(0xFF6CC5FF), pink = Color(0xFFFF9FD0),
        nonTerminal = Color(0xFFFFD400), terminal = Color(0xFF00E5C8),
        drawer = Color(0xFF262626), drawerHi = Color(0xFF8A8A8A), drawerLo = Color(0xFF000000),
        tag = Color(0xFFFFFFFF), tagInk = Color(0xFF000000),
        hazard = Color(0xFFFFD400), hazardDark = Color(0xFF000000), onStrong = Color(0xFFFFFFFF),
        regions = listOf(0xFF6CC5FF, 0xFF5CFF8F, 0xFFFFD400, 0xFFFFB000, 0xFFFF9090, 0xFFE6E6E6, 0xFFFFC27A, 0xFFFF9FD0, 0xFF00E5C8).map { Color(it) },
    )
}

object Palette {
    private val c: ThemeColors get() = ThemeState.colors
    val paper: Color get() = c.paper
    val paperDark: Color get() = c.paperDark
    val ink: Color get() = c.ink
    val inkSoft: Color get() = c.inkSoft
    /** Fundo de cartões e botões neutros (muda com o tema). */
    val white: Color get() = c.surface
    /** Texto sobre fundos sempre escuros (codeBg, avisos). Sobre cor do tema, use [on]. */
    val onAccent = Color(0xFFFFFFFF)
    /** Fundo de blocos de código e terminais: sempre escuro. */
    val codeBg = Color(0xFF171C1E)
    /** Texto escuro fixo para desenhar sobre pastéis (nós de árvore, estados). */
    val inkOnLight = Color(0xFF1F2628)
    /** Pastéis fixos dos nós de árvore (o texto sobre eles é sempre [inkOnLight]). */
    val nodeNonTerminal = Color(0xFFF7E3C4)
    val nodeLeaf = Color(0xFFD9F7F2)
    val nodeDone = Color(0xFFBFEBCB)
    val nodeActive = Color(0xFFFBE08A)
    /** Texto dos nós: mesma ideia de nonTerminal/terminal, mas sempre sobre pastel. */
    val nodeTextNonTerminal = Color(0xFF8A2F14)
    val nodeTextTerminal = Color(0xFF0B5F55)

    val action: Color get() = c.action
    val orange: Color get() = c.orange
    val teal: Color get() = c.teal
    val purple: Color get() = c.purple
    val yellow: Color get() = c.yellow
    val red: Color get() = c.red
    val green: Color get() = c.green
    val blue: Color get() = c.blue
    val pink: Color get() = c.pink

    val drawer: Color get() = c.drawer
    val drawerHi: Color get() = c.drawerHi
    val drawerLo: Color get() = c.drawerLo
    val tag: Color get() = c.tag
    val tagInk: Color get() = c.tagInk
    val hazard: Color get() = c.hazard
    val hazardDark: Color get() = c.hazardDark

    fun region(r: Region): Color = c.regions[r.ordinal]

    /** Cores usadas para destacar símbolos (não-terminal, terminal, etc.). */
    val nonTerminal: Color get() = c.nonTerminal
    val terminal: Color get() = c.terminal

    /** A cor de texto mais legível sobre [bg]. Fundo translúcido conta como misturado ao papel. */
    fun on(bg: Color): Color {
        val solid = bg.compositeOver(c.paper)
        val light = c.onStrong
        val dark = c.hazardDark
        return if (contrast(light, solid) >= contrast(dark, solid)) light else dark
    }

    fun contrast(a: Color, b: Color): Double {
        val x = a.luminance() + 0.05
        val y = b.luminance() + 0.05
        return (if (x > y) x / y else y / x).toDouble()
    }
}

data class Fonts(val display: FontFamily, val body: FontFamily, val mono: FontFamily)

val LocalFonts = staticCompositionLocalOf { Fonts(FontFamily.Default, FontFamily.Default, FontFamily.Monospace) }

@Composable
fun CompilandiaTheme(content: @Composable () -> Unit) {
    val display = FontFamily(
        Font(Res.font.barlow_condensed_semibold, FontWeight.SemiBold),
        Font(Res.font.barlow_condensed_bold, FontWeight.Bold),
    )
    val body = FontFamily(
        Font(Res.font.atkinson_regular, FontWeight.Normal),
        Font(Res.font.atkinson_bold, FontWeight.Bold),
    )
    val mono = FontFamily(
        Font(Res.font.space_mono_regular, FontWeight.Normal),
        Font(Res.font.space_mono_bold, FontWeight.Bold),
    )
    val typography = Typography(
        displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 34.sp),
        headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 30.sp),
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 25.sp),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        titleMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 21.sp),
        titleSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 19.sp),
        bodyLarge = TextStyle(fontFamily = body, fontSize = 17.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = body, fontSize = 15.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = body, fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 20.sp),
        labelMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
        labelSmall = TextStyle(fontFamily = body, fontSize = 12.sp),
    )
    val tc = ThemeState.colors
    val colors = if (tc.dark) androidx.compose.material3.darkColorScheme(
        primary = Palette.action, onPrimary = Palette.on(Palette.action), secondary = Palette.teal, onSecondary = Palette.on(Palette.teal),
        tertiary = Palette.purple, background = Palette.paper, onBackground = Palette.ink, surface = Palette.white,
        onSurface = Palette.ink, surfaceVariant = Palette.paperDark, onSurfaceVariant = Palette.inkSoft, error = Palette.red,
    ) else lightColorScheme(
        primary = Palette.action, onPrimary = Palette.on(Palette.action), secondary = Palette.teal, onSecondary = Palette.on(Palette.teal),
        tertiary = Palette.purple, background = Palette.paper, onBackground = Palette.ink, surface = Palette.white,
        onSurface = Palette.ink, surfaceVariant = Palette.paperDark, onSurfaceVariant = Palette.inkSoft, error = Palette.red,
    )
    val d = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.runtime.CompositionLocalProvider(
        LocalFonts provides Fonts(display, body, mono),
        // Sem Surface/Scaffold no app: é isto que dá a cor do tema a todo Text sem cor explícita.
        androidx.compose.material3.LocalContentColor provides Palette.ink,
        androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(d.density, d.fontScale * ThemeState.fontScale),
    ) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}
