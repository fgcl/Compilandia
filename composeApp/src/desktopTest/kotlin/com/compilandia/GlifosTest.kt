package com.compilandia

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * No navegador não há fonte do sistema nem de emoji: o caractere que nenhuma fonte do jogo tem vira quadrado.
 * Todo caractere dos textos e das strings da interface precisa estar em todas as fontes do jogo
 * ou na fonte de símbolos (simbolos.ttf), que o site carrega como reserva.
 * Emoji não passa: no lugar dele, um ícone desenhado (ui/Icones.kt).
 */
class GlifosTest {
    private val pastaFontes = File("src/commonMain/composeResources/font")
    private fun fonte(f: File) = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, f)
    private val simbolos = fonte(File(pastaFontes, "simbolos.ttf"))
    private val doJogo = pastaFontes.listFiles()!!.filter { it.extension == "ttf" && it.name != "simbolos.ttf" }.map(::fonte)

    /** Emoji que só serve de chave para escolher o desenho do personagem: nunca é desenhado como texto. */
    private val chavesDePersonagem = setOf(0x1F916, 0x1F989, 0x1F5FF)

    private fun desenhavel(cp: Int) = cp < 0x80 || doJogo.all { it.canDisplay(cp) } || simbolos.canDisplay(cp)

    private fun faltando(arquivos: List<File>, linhaConta: (String) -> Boolean = { true }): List<String> =
        arquivos.flatMap { a ->
            a.readLines().withIndex().filter { linhaConta(it.value) }.flatMap { (n, l) ->
                l.codePoints().toArray().filter { !desenhavel(it) && it !in chavesDePersonagem }.distinct()
                    .map { "${a.path}:${n + 1}: U+%04X '%s'".format(it, String(Character.toChars(it))) }
            }
        }

    @Test fun textosSoUsamCaracteresQueAsFontesDesenham() {
        val arquivos = File("src/commonMain/textos").walkTopDown().filter { it.extension == "xml" }.toList()
        val falta = faltando(arquivos)
        assertTrue(falta.isEmpty(), "Caractere sem fonte (vira quadrado no site):\n" + falta.take(40).joinToString("\n"))
    }

    @Test fun interfaceSoUsaCaracteresQueAsFontesDesenham() {
        val arquivos = File("src/commonMain/kotlin/com/compilandia/ui").walkTopDown().filter { it.extension == "kt" }.toList()
        val ehComentario = { l: String -> l.trimStart().let { it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") } }
        val falta = faltando(arquivos) { !ehComentario(it) }
        assertTrue(falta.isEmpty(), "Caractere sem fonte (vira quadrado no site). Use um ícone de ui/Icones.kt:\n" + falta.take(40).joinToString("\n"))
    }
}
