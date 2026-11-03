package com.compilandia

import com.compilandia.content.Falas
import com.compilandia.content.episodes.Episodes
import com.compilandia.ui.voice.VoiceBank
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun todas() = (Episodes.falas() + Falas.todas()).distinctBy { it.id }

/** Grava a lista de falas que scripts/voice/render.py e scripts/text_lint.py leem. */
class VoiceExportTest {
    @Test fun exporta() {
        File("build").mkdirs()
        File("build/voice-lines.tsv").writeText(todas().joinToString("") { "${it.id}\t${it.who.name}\t${it.text}\n" })
    }
}

/** Fala sem áudio vira legenda no jogo; este teste avisa antes de chegar lá. */
class VoiceCoverageTest {
    private val dir = File("src/commonMain/composeResources/files/voice")

    @Test fun todaFalaTemAudioEBoca() {
        val index = VoiceBank.parse(File(dir, "index.txt").readText())
        val faltam = todas().filter { it.id !in index || !File(dir, "${it.id}.ogg").exists() }
        assertTrue(faltam.isEmpty(), "sem áudio (${faltam.size}): rode scripts/voice/render.py. Ex.: ${faltam.take(3).map { it.text }}")
    }

    @Test fun bocaAcompanhaADuracao() {
        for ((id, c) in VoiceBank.parse(File(dir, "index.txt").readText())) {
            assertTrue(c.times.isNotEmpty(), "$id sem boca")
            assertTrue(c.times.zip(c.times.drop(1)).all { (a, b) -> a <= b }, "$id fora de ordem")
            assertTrue(c.times.last() <= c.durMs, "$id passa do fim do áudio")
            assertTrue(c.shapes.all { it in 0..6 }, "$id com boca inválida")
        }
    }

    @Test fun desktopDecodificaOgg() {
        val ogg = dir.listFiles { f -> f.extension == "ogg" }?.firstOrNull() ?: return
        val src = javax.sound.sampled.AudioSystem.getAudioInputStream(ogg.inputStream().buffered())
        val f = src.format
        val pcm = javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, f.sampleRate, 16, f.channels, f.channels * 2, f.sampleRate, false)
        val data = javax.sound.sampled.AudioSystem.getAudioInputStream(pcm, src).readAllBytes()
        assertEquals(24000f, f.sampleRate)
        assertTrue(data.size > 1000, "decodificou ${data.size} bytes")
    }
}
