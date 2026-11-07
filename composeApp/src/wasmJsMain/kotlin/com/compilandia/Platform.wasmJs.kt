@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.compilandia.platform

import kotlin.io.encoding.Base64

/*
 * O jogo no navegador. Nada sai da máquina: o save fica no localStorage,
 * e som e voz tocam pelo <audio> do próprio navegador a partir dos arquivos do jogo.
 */

private fun lsGet(k: String): String? = js("window.localStorage ? window.localStorage.getItem(k) : null")
private fun lsSet(k: String, v: String) { js("try { window.localStorage.setItem(k, v); } catch (e) {}") }

actual object Storage {
    private const val CHAVE = "compilandia-save"
    actual fun load(): String? = try { lsGet(CHAVE) } catch (e: Throwable) { null }
    actual fun save(text: String) { try { lsSet(CHAVE, text) } catch (e: Throwable) { } }
}

private fun dataUrl(mime: String, bytes: ByteArray) = "data:$mime;base64," + Base64.encode(bytes)
private fun mimeDe(nome: String) = if (nome.endsWith(".wav")) "audio/wav" else "audio/ogg"

/** Um <audio> por toque: o navegador mistura sozinho. preservesPitch falso deixa o rate mudar o tom. */
private fun tocaEfeito(url: String, rate: Double) {
    js("try { var a = new Audio(url); a.preservesPitch = false; a.playbackRate = rate; a.volume = 0.7; var p = a.play(); if (p) p.catch(function(){}); } catch (e) {}")
}

actual object Sound {
    actual var enabled: Boolean = true
    private val urls = HashMap<Sfx, String>()

    actual fun load(sfx: Sfx, bytes: ByteArray) { urls[sfx] = dataUrl(mimeDe(sfx.file), bytes) }

    actual fun play(sfx: Sfx, rate: Float) {
        if (!enabled) return
        val u = urls[sfx] ?: return
        tocaEfeito(u, rate.toDouble().coerceIn(0.5, 4.0))
    }
}

// A voz fica num <audio> guardado em window, para o Kotlin perguntar a posição (a boca segue a fala).
private fun vozToca(url: String, aoFim: () -> Unit): Boolean = js(
    """{
      try {
        if (window.__voz) { window.__voz.onended = null; window.__voz.pause(); }
        var a = new Audio(url);
        window.__voz = a;
        a.onended = function () { if (window.__voz === a) { window.__voz = null; aoFim(); } };
        a.onerror = function () { if (window.__voz === a) { window.__voz = null; aoFim(); } };
        var p = a.play();
        if (p) p.catch(function () { if (window.__voz === a) { window.__voz = null; aoFim(); } });
        return true;
      } catch (e) { return false; }
    }"""
)
private fun vozPara() { js("if (window.__voz) { window.__voz.onended = null; window.__voz.pause(); window.__voz = null; }") }
private fun vozPosicao(): Double = js("window.__voz ? window.__voz.currentTime * 1000 : 0")
/** O Safari antigo não toca OGG Vorbis: aí a legenda aparece sozinha. */
private fun tocaOgg(): Boolean = js("(function(){ try { return new Audio().canPlayType('audio/ogg; codecs=vorbis') !== ''; } catch (e) { return false; } })()")

actual object VoicePlayer {
    actual fun play(id: String, bytes: ByteArray, onEnd: () -> Unit): Boolean {
        if (!tocaOgg()) return false
        return vozToca(dataUrl("audio/ogg", bytes), onEnd)
    }
    actual fun stop() = vozPara()
    actual fun positionMs(): Long = vozPosicao().toLong()
}

private fun compartilha(t: String) {
    js("try { if (navigator.share) { navigator.share({ text: t }).catch(function(){}); } else if (navigator.clipboard) { navigator.clipboard.writeText(t).catch(function(){}); } } catch (e) {}")
}

actual object Compartilha {
    actual fun texto(t: String) = compartilha(t)
}

private fun agora(): Double = js("Date.now()")
actual fun nowMs(): Long = agora().toLong()

private fun idiomaDoNavegador(): String = js("(navigator.language || 'pt').slice(0, 2)")
private fun recarregar() { js("window.location.reload()") }

actual object Idioma {
    actual fun doAparelho(): String = idiomaDoNavegador()
    actual fun reabrir(): Boolean { recarregar(); return true }
}

/** R4: sem lembrete fora do celular. A retomada aparece quando a pessoa abre o jogo. */
actual object Lembrete {
    actual val disponivel: Boolean = false
    actual fun agenda(ligado: Boolean, hora: Int) {}
    actual fun pedePermissao(onResultado: (Boolean) -> Unit) = onResultado(false)
}
