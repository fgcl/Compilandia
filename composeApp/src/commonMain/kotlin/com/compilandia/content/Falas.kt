package com.compilandia.content

import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.PROF

/** Falas soltas: as que não pertencem a um episódio. Tudo aqui passa pelo text_lint e vira áudio. */
object Falas {
    val toqueNoByte = listOf(
        Fala(BYTE, "Ei, isso faz cócegas no meu sensor."),
        Fala(BYTE, "Bora consertar esse compilador?"),
        Fala(BYTE, "Hoje eu acordei com a bateria cheia."),
        Fala(BYTE, "Tem um Bug roendo as gavetas de novo."),
    )
    val acerto = listOf(
        Fala(BYTE, "Boa! Pegou o jeito."),
        Fala(BYTE, "Isso! Três seguidas."),
        Fala(BYTE, "Mandou bem demais."),
        Fala(BYTE, "O Bug nem viu de onde veio."),
        Fala(BYTE, "Assim o compilador volta a funcionar."),
    )
    val erro = listOf(
        Fala(BYTE, "Quase! Olha de novo com calma."),
        Fala(BYTE, "Esse pegou. Errar aqui ajuda a lembrar."),
        Fala(BYTE, "Sem problema, tenta outra vez."),
    )
    val vitoria = listOf(
        Fala(BYTE, "Bug eliminado! Pode abrir a caixa."),
        Fala(BYTE, "Acabou com ele. Vem ver o que caiu."),
    )
    val derrota = listOf(
        Fala(BYTE, "Esse Bug fugiu, mas a gente aprendeu onde ele mora."),
    )
    val confianca = Fala(BYTE, "Antes de começar: você sabe isso?")
    val boas = listOf(
        Fala(BYTE, "Oi! Eu sou o Byte. Eu queria virar um compilador de verdade."),
        Fala(BYTE, "Mas o Grande Compilador quebrou, e os Bugs estão roendo tudo."),
        Fala(PROF, "Quem entende compiladores consegue consertar. Você começa do zero, com a gente."),
        Fala(PROF, "Como posso te chamar?"),
        Fala(BYTE, "Bora! A oficina está quase vazia, e cada acerto rende engrenagens."),
    )

    /** Toda fala solta, para gerar os áudios. */
    fun todas(): List<Fala> = toqueNoByte + acerto + erro + vitoria + derrota + confianca + boas + comoJogar.values
}
