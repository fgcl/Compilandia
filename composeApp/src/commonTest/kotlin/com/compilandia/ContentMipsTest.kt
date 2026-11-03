package com.compilandia

import com.compilandia.content.Beat
import com.compilandia.content.Library
import com.compilandia.engine.MipsVM
import kotlin.test.Test
import kotlin.test.assertTrue

/** Todo programa MIPS completo (com "main:") que aparece nas aulas e no guia precisa rodar no MARS de bolso. */
class ContentMipsTest {
    @Test fun everyMipsProgramRuns() {
        val all = Library.lessons.map { "${it.skill}/${it.style}" to it.beats } + Library.guide.map { "guia ${it.number}" to it.beats }
        val failures = mutableListOf<String>()
        var count = 0
        for ((where, beats) in all) for (b in beats) {
            if (b !is Beat.Code || !b.code.contains("main:")) continue
            if (listOf("class ", "void ", "public ", "private ", "String ", "return ").any { it in b.code }) continue   // é Java, não assembly
            count++
            try { MipsVM(b.code).run() } catch (e: Exception) { failures += "$where: ${e.message}" }
        }
        println("Programas MIPS verificados: $count")
        failures.forEach { println("FALHA $it") }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }
}
