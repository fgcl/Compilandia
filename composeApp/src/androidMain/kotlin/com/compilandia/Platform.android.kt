package com.compilandia.platform

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import java.io.File

actual object Storage {
    var dir: File? = null
    private val file get() = dir?.let { File(it, "compilandia_save.json") }
    actual fun load(): String? = try { file?.takeIf { it.exists() }?.readText() } catch (e: Exception) { null }
    actual fun save(text: String) {
        try { file?.writeText(text) } catch (e: Exception) { }
    }
}

actual object Sound {
    actual var enabled: Boolean = true
    private val pool: SoundPool by lazy {
        SoundPool.Builder().setMaxStreams(6)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .build()
    }
    private val ids = HashMap<Sfx, Int>()
    private val tone: ToneGenerator? by lazy { try { ToneGenerator(AudioManager.STREAM_MUSIC, 60) } catch (e: Exception) { null } }

    actual fun load(sfx: Sfx, bytes: ByteArray) {
        try {
            val dir = Storage.dir ?: return
            val f = File(dir, "sfx_" + sfx.file)
            if (!f.exists() || f.length() != bytes.size.toLong()) f.writeBytes(bytes)
            ids[sfx] = pool.load(f.absolutePath, 1)
        } catch (e: Exception) { }
    }

    actual fun play(sfx: Sfx, rate: Float) {
        if (!enabled) return
        val id = ids[sfx]
        if (id != null) {
            try { pool.play(id, 0.9f, 0.9f, 1, 0, rate.coerceIn(0.5f, 2f)); return } catch (e: Exception) { }
        }
        val t = tone ?: return
        try {
            when (sfx) {
                Sfx.CORRECT, Sfx.STAR -> t.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                Sfx.WRONG, Sfx.FAIL -> t.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                Sfx.LEVEL_UP, Sfx.VICTORY, Sfx.COMBO -> t.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 300)
                Sfx.BLIP, Sfx.TICK -> {}
                else -> t.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            }
        } catch (e: Exception) { }
    }
}

actual object VoicePlayer {
    private var player: MediaPlayer? = null

    actual fun play(id: String, bytes: ByteArray, onEnd: () -> Unit): Boolean {
        stop()
        return try {
            val dir = File(Storage.dir ?: return false, "voice").apply { mkdirs() }
            val f = File(dir, "$id.ogg")
            if (!f.exists() || f.length() != bytes.size.toLong()) f.writeBytes(bytes)
            val p = MediaPlayer()
            p.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            p.setDataSource(f.absolutePath)
            p.setOnCompletionListener { it.release(); if (player === it) player = null; onEnd() }
            p.setOnErrorListener { mp, _, _ -> mp.release(); if (player === mp) player = null; onEnd(); true }
            p.prepare()
            p.start()
            player = p
            true
        } catch (e: Exception) { false }
    }

    actual fun stop() {
        val p = player ?: return
        player = null
        try { p.stop() } catch (e: Exception) { }
        p.release()
    }

    actual fun positionMs(): Long = try { player?.currentPosition?.toLong() ?: 0L } catch (e: IllegalStateException) { 0L }
}

actual object Compartilha {
    var ctx: android.content.Context? = null
    actual fun texto(t: String) {
        val c = ctx ?: return
        try {
            val envia = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT, t)
            c.startActivity(android.content.Intent.createChooser(envia, "Compartilhar").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { }
    }
}

actual fun nowMs(): Long = System.currentTimeMillis()
