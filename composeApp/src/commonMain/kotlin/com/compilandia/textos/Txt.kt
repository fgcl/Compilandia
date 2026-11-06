package com.compilandia.textos

/**
 * Os textos do jogo. Moram em composeApp/src/commonMain/textos/<idioma>/ (arquivos .xml), no formato do strings.xml do Android,
 * e o Gradle gera os mapas (gradle/textos.gradle.kts). O código pede o texto pela chave: tx("chave").
 *
 * O idioma é escolhido antes de qualquer tela abrir. Trocar de idioma pede para reabrir o jogo,
 * porque as aulas são montadas uma vez só.
 */
object Txt {
    private val pt: HashMap<String, String> by lazy { HashMap<String, String>(TextosPt.total * 2).also { TextosPt.encher(it) } }
    private val en: HashMap<String, String> by lazy { HashMap<String, String>(TextosEn.total * 2 + 16).also { TextosEn.encher(it) } }

    /** "pt" ou "en". */
    var idioma: String = "pt"
        set(v) { field = if (v == "en") "en" else "pt" }

    val idiomas = listOf("pt", "en")

    private val atual: HashMap<String, String> get() = if (idioma == "en") en else pt

    /** Do texto traduzido de volta ao português. Só existe quando o idioma não é o português. */
    private val volta: HashMap<String, String> by lazy {
        HashMap<String, String>(en.size * 2).also { m -> for ((k, v) in en) if (v !in m) m[v] = pt[k] ?: v }
    }

    operator fun get(chave: String): String = atual[chave] ?: pt[chave] ?: chave

    /** O texto de uma chave num idioma dado, qualquer que seja o atual. */
    fun em(lingua: String, chave: String): String = (if (lingua == "en") en[chave] else null) ?: pt[chave] ?: chave

    private val idiomaSalvo = Regex("\"idioma\"\\s*:\\s*\"(pt|en)\"")

    /**
     * O idioma da abertura do jogo. Vale o que a pessoa escolheu nos Ajustes.
     * Save antigo, de antes da escolha, continua em português. Jogo novo segue o aparelho.
     */
    fun idiomaInicial(save: String?, aparelho: String): String {
        if (save != null) idiomaSalvo.find(save)?.let { return it.groupValues[1] }
        if (!save.isNullOrBlank()) return "pt"
        return if (aparelho.lowercase().startsWith("pt")) "pt" else "en"
    }

    /** O texto em português de uma chave, qualquer que seja o idioma. */
    fun pt(chave: String): String = pt[chave] ?: chave

    fun existe(chave: String): Boolean = chave in pt

    val chaves: Set<String> get() = pt.keys

    fun traduzidas(lingua: String): Set<String> = if (lingua == "en") en.keys else pt.keys

    fun traduzivel(chave: String): Boolean = chave !in TextosInfo.naoTraduz

    /**
     * O texto em português que corresponde a um texto já mostrado no idioma atual.
     * Ids de voz e de aula nascem do português, para o save e os áudios valerem em qualquer idioma.
     */
    fun paraPt(texto: String): String = if (idioma == "pt") texto else volta[texto] ?: texto

    /** Troca %1$s, %2$s... pelos valores. Um % que não forma marcador fica como está. */
    fun formata(modelo: String, args: Array<out Any?>): String {
        if (args.isEmpty() || '%' !in modelo) return modelo
        val sb = StringBuilder(modelo.length + 16)
        var i = 0
        while (i < modelo.length) {
            val c = modelo[i]
            if (c == '%' && i + 1 < modelo.length && modelo[i + 1].isDigit()) {
                var j = i + 1
                while (j < modelo.length && modelo[j].isDigit()) j++
                if (j + 1 < modelo.length && modelo[j] == '$' && modelo[j + 1] == 's') {
                    val n = modelo.substring(i + 1, j).toInt()
                    if (n in 1..args.size) { sb.append(args[n - 1].toString()); i = j + 2; continue }
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }
}

/** O texto da chave no idioma atual, com %1$s, %2$s... trocados por [args]. */
fun tx(chave: String, vararg args: Any?): String = if (args.isEmpty()) Txt[chave] else Txt.formata(Txt[chave], args)
