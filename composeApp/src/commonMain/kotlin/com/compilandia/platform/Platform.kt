package com.compilandia.platform

/** Persistência simples (um arquivo JSON com o progresso). */
expect object Storage {
    fun load(): String?
    fun save(text: String)
}

enum class Sfx(val file: String) {
    CORRECT("confirmation_002.wav"),
    WRONG("error_006.wav"),
    CLICK("click_002.wav"),
    LEVEL_UP("jingles_nes_9.ogg"),
    WHOOSH("maximize_006.wav"),
    DROP("drop_002.wav"),
    PICK("select_003.wav"),
    COMBO("power_up_3.ogg"),
    VICTORY("jingles_nes_0.ogg"),
    BOSS_HIT("zap_two_tone.ogg"),
    FAIL("low_down.ogg"),
    STAR("glass_002.wav"),
    BLIP("blip.wav"),
    IMPACT("impact.wav"),
    GATE("gate.wav"),
    GLITCH("glitch.wav"),
    TICK("tick.wav"),
}

/** Efeitos sonoros sintetizados na hora (nenhum arquivo de áudio necessário). */
expect object Sound {
    var enabled: Boolean
    /** Registra os bytes de um efeito (carregados de composeResources/files). */
    fun load(sfx: Sfx, bytes: ByteArray)
    /** [rate] muda o tom: 1 é o normal, 2 é uma oitava acima. */
    fun play(sfx: Sfx, rate: Float = 1f)
}

/** Toca as falas gravadas (arquivos OGG Vorbis). */
expect object VoicePlayer {
    /** Começa a tocar. Devolve false se este aparelho não conseguiu abrir o áudio. */
    fun play(id: String, bytes: ByteArray, onEnd: () -> Unit): Boolean
    fun stop()
    fun positionMs(): Long
}

/** Abre a folha de compartilhar do aparelho. No computador não faz nada: o texto já foi copiado. */
expect object Compartilha {
    fun texto(t: String)
}

expect fun nowMs(): Long

/** O idioma do aparelho e o jeito de reabrir o jogo depois de trocar de idioma (S2). */
expect object Idioma {
    /** Código de duas letras do idioma do aparelho, como "pt" ou "en". */
    fun doAparelho(): String
    /** Fecha e abre o jogo de novo. Devolve false onde não dá: a pessoa reabre à mão. */
    fun reabrir(): Boolean
}
