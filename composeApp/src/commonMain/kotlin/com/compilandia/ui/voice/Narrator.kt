package com.compilandia.ui.voice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.compilandia.content.Fala
import com.compilandia.content.Speaker
import com.compilandia.platform.VoicePlayer
import com.compilandia.resources.Res
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/** Índice das falas: duração e a forma da boca em cada instante (vem de scripts/voice/render.py). */
object VoiceBank {
    class Clip(val durMs: Int, val times: IntArray, val shapes: ByteArray)

    private var clips: Map<String, Clip> = emptyMap()
    val size: Int get() = clips.size

    fun parse(text: String): Map<String, Clip> = text.lineSequence().filter { it.isNotBlank() }.associate { line ->
        val parts = line.split(' ')
        val track = parts.getOrNull(2).orEmpty().split(',').filter { it.isNotEmpty() }.map { it.split(':') }
        parts[0] to Clip(
            parts[1].toInt(),
            IntArray(track.size) { track[it][0].toInt() },
            ByteArray(track.size) { track[it][1].toByte() },
        )
    }

    suspend fun load() {
        if (clips.isNotEmpty()) return
        clips = try { parse(Res.readBytes("files/voice/index.txt").decodeToString()) } catch (e: Exception) { emptyMap() }
    }

    fun clip(id: String): Clip? = clips[id]
}

/** Quem está falando agora. Os personagens leem daqui para mexer a boca. */
object Narrator {
    var enabled by mutableStateOf(true)
    /** Os testes ligam isto: rodam sem falar nos alto-falantes de quem compila. */
    var silent = false
    var speaker by mutableStateOf<Speaker?>(null)
        private set
    private var clip: VoiceBank.Clip? = null

    /** A fala tem áudio pronto para tocar? Sem isso, a tela mostra a legenda. */
    fun canSpeak(f: Fala): Boolean = enabled && !silent && VoiceBank.clip(f.id) != null

    private var serial = 0
    private var waiting: CompletableDeferred<Unit>? = null

    /**
     * Fala e espera terminar. Sem áudio volta na hora, e quem chamou mostra a legenda.
     * Uma fala nova substitui a anterior. A antiga, ao terminar, não pode calar a nova: daí o [serial].
     */
    suspend fun say(f: Fala) {
        stop()
        if (!enabled || silent) return
        val c = VoiceBank.clip(f.id) ?: return
        val mine = ++serial
        val bytes = try { Res.readBytes("files/voice/${f.id}.ogg") } catch (e: Exception) { return }
        if (mine != serial) return
        val done = CompletableDeferred<Unit>()
        if (!VoicePlayer.play(f.id, bytes) { done.complete(Unit) }) return
        waiting = done
        speaker = f.who
        clip = c
        try {
            withTimeoutOrNull(c.durMs + 1500L) { done.await() }
        } finally {
            if (mine == serial) stop()
        }
    }

    fun stop() {
        serial++
        speaker = null
        clip = null
        VoicePlayer.stop()
        waiting?.complete(Unit)
        waiting = null
    }

    /** Forma da boca neste instante da fala (ver Mouth). */
    fun viseme(): Int {
        val c = clip ?: return 0
        val t = VoicePlayer.positionMs().toInt()
        var lo = 0
        var hi = c.times.size - 1
        var best = -1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (c.times[mid] <= t) { best = mid; lo = mid + 1 } else hi = mid - 1
        }
        return if (best < 0) 0 else c.shapes[best].toInt()
    }
}
