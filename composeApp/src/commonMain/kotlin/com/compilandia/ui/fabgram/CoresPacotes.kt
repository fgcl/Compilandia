package com.compilandia.ui.fabgram

import androidx.compose.ui.graphics.Color

/**
 * As cores da fábrica, juntas num pacote (como um texture pack): chão, esteiras, chassi, faixas de categoria.
 * Os desenhos (Desenhos.kt, Maquinas*.kt) só pegam cor daqui, nunca uma cor solta.
 */
internal class Pacote(
    val id: String,
    /** Raio do canto, em pixels da arte (a célula tem 32). */
    val raio: Float = 3f,
    /** Espessura do contorno, em pixels da arte. */
    val traco: Float = 1f,
    val chao: Color, val chaoAlt: Color, val junta: Color,
    val corpo: Color, val claro: Color, val escuro: Color, val fundo: Color, val linha: Color,
    val esteira: Color, val esteiraBorda: Color, val seta: Color,
    val distribuicao: Color, val producao: Color, val nucleo: Color,
    val alerta: Color, val ok: Color,
    /** Tinta sobre os discos e plaquinhas. */
    val tinta: Color,
    val placa: Color, val placaTinta: Color,
    val sombra: Color,
    val tela: Color, val telaLuz: Color,
)

internal object Pacotes {
    val METAL = Pacote(
        "metal",
        chao = Color(0xFF2C3037), chaoAlt = Color(0xFF2A2E34), junta = Color(0xFF1C1F24),
        corpo = Color(0xFF4A515C), claro = Color(0xFF6E7784), escuro = Color(0xFF30353D), fundo = Color(0xFF23272E),
        linha = Color(0xFF0F1114),
        esteira = Color(0xFF30353D), esteiraBorda = Color(0xFF4C535D), seta = Color(0xFFE8A33A),
        distribuicao = Color(0xFFE8A33A), producao = Color(0xFF3FC1B0), nucleo = Color(0xFFD9824A),
        alerta = Color(0xFFE0553C), ok = Color(0xFF5EE08A), tinta = Color(0xFF0F1114),
        placa = Color(0xFFF1EAD8), placaTinta = Color(0xFF0F1114), sombra = Color(0x59000000),
        tela = Color(0xFF0E3B38), telaLuz = Color(0xFF5FF2D2),
    )

}

/**
 * O pacote em uso. Testamos cinco (metal, pixel, planta azul, neon e desenho animado); o dono escolheu o metal.
 * Para testar outro, crie um [Pacote] novo e troque aqui: todo desenho lê as cores daqui.
 */
internal object PacoteAtual {
    val pacote: Pacote = Pacotes.METAL
}
