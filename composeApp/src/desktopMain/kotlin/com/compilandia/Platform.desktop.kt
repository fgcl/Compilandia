package com.compilandia.platform

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

actual object Storage {
    private val file = File(System.getProperty("user.home"), ".compilandia/save.json")
    actual fun load(): String? = try { file.takeIf { it.exists() }?.readText() } catch (e: Exception) { null }
    actual fun save(text: String) {
        try { file.parentFile?.mkdirs(); file.writeText(text) } catch (e: Exception) { }
    }
}

/** Os testes rodam com -Dcompilandia.mudo=true (build.gradle.kts): nada toca no alto-falante de quem compila. */
private val mudo: Boolean = System.getProperty("compilandia.mudo") == "true"

actual object Sound {
    actual var enabled: Boolean = true
    private val wavs = HashMap<Sfx, ByteArray>()

    actual fun load(sfx: Sfx, bytes: ByteArray) {
        if (sfx.file.endsWith(".wav")) wavs[sfx] = bytes   // o Java só toca WAV; OGG cai no sintetizador
    }

    actual fun play(sfx: Sfx, rate: Float) {
        if (!enabled || mudo) return
        val wav = wavs[sfx]
        thread(isDaemon = true) {
            try {
                if (wav != null) {
                    val clip = AudioSystem.getClip()
                    clip.open(AudioSystem.getAudioInputStream(java.io.ByteArrayInputStream(wav)))
                    clip.start()
                    Thread.sleep(clip.microsecondLength / 1000 + 50)
                    clip.close()
                    return@thread
                }
                val notes = when (sfx) {
                    Sfx.CORRECT, Sfx.STAR -> listOf(660.0 to 70, 880.0 to 110)
                    Sfx.WRONG, Sfx.FAIL -> listOf(220.0 to 160)
                    Sfx.LEVEL_UP, Sfx.VICTORY, Sfx.COMBO -> listOf(523.0 to 90, 659.0 to 90, 784.0 to 160)
                    else -> listOf(1200.0 to 25)
                }
                val rate = 22050f
                val fmt = AudioFormat(rate, 8, 1, true, false)
                val line = AudioSystem.getSourceDataLine(fmt)
                line.open(fmt); line.start()
                for ((f, ms) in notes) {
                    val n = (rate * ms / 1000).toInt()
                    val buf = ByteArray(n) { i -> (sin(2 * PI * f * i / rate) * 40 * (1 - i.toDouble() / n)).toInt().toByte() }
                    line.write(buf, 0, buf.size)
                }
                line.drain(); line.close()
            } catch (e: Exception) { }
        }
    }
}

actual object VoicePlayer {
    private var clip: Clip? = null

    actual fun play(id: String, bytes: ByteArray, onEnd: () -> Unit): Boolean {
        stop()
        // Mudo, a fala não toca e o Narrator segue na hora, como quando falta o áudio.
        if (mudo) return false
        return try {
            // O Java não abre OGG sozinho: quem entende o arquivo é o vorbisspi (só no desktop).
            val source = AudioSystem.getAudioInputStream(java.io.ByteArrayInputStream(bytes))
            val f = source.format
            val pcm = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.sampleRate, 16, f.channels, f.channels * 2, f.sampleRate, false)
            val data = AudioSystem.getAudioInputStream(pcm, source).readAllBytes()
            val c = AudioSystem.getClip()
            c.open(pcm, data, 0, data.size)
            c.addLineListener { e ->
                if (e.type == LineEvent.Type.STOP && clip === c) { clip = null; c.close(); onEnd() }
            }
            clip = c
            c.start()
            true
        } catch (e: Exception) { false }
    }

    actual fun stop() {
        val c = clip ?: return
        clip = null
        c.stop()
        c.close()
    }

    actual fun positionMs(): Long = clip?.microsecondPosition?.div(1000) ?: 0L
}

actual object Compartilha {
    actual fun texto(t: String) { }
}

actual fun nowMs(): Long = System.currentTimeMillis()

actual object Idioma {
    actual fun doAparelho(): String = java.util.Locale.getDefault().language
    actual fun reabrir(): Boolean = false
}

/** R4: sem lembrete fora do celular. A retomada aparece quando a pessoa abre o jogo. */
actual object Lembrete {
    actual val disponivel: Boolean = false
    actual fun agenda(ligado: Boolean, hora: Int) {}
    actual fun pedePermissao(onResultado: (Boolean) -> Unit) = onResultado(false)
}
