package com.compilandia.content

/**
 * Uma fala gravada. O id nasce do texto: mudou a frase, muda o arquivo de áudio,
 * e o teste VoiceCoverageTest avisa que falta renderizar.
 */
data class Fala(val who: Speaker, val text: String) {
    /** Nasce do texto em português, para o áudio valer em qualquer idioma. */
    val id: String get() = falaId(who, com.compilandia.textos.Txt.paraPt(text))
    /** O que a voz lê: sem marcas de negrito e de código. */
    val plain: String get() = text.replace("**", "").replace("`", "")
}

/** FNV-1a de 64 bits. Precisa dar o mesmo resultado em qualquer plataforma. */
fun falaId(who: Speaker, text: String): String {
    var h = -3750763034362895579L
    for (c in who.name + "|" + text) {
        h = h xor c.code.toLong()
        h *= 1099511628211L
    }
    return h.toULong().toString(16).padStart(16, '0').take(12)
}
