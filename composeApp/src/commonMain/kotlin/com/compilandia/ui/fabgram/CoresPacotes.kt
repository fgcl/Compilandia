package com.compilandia.ui.fabgram

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Pacote de textura da fábrica, como um texture pack: as mesmas máquinas, outro jeito de desenhar.
 * O pacote decide o chão, as esteiras, o chassi e as cores; os desenhos das máquinas (Desenhos.kt)
 * pedem formas ao pacote (Tracos.kt), que as pinta cheias, em contorno, em pixel ou arredondadas.
 *
 * Pacote novo: uma entrada em [Pacotes.todos] e o nome em textos (fabgram_pacote_<id>).
 */
internal class Pacote(
    val id: String,
    /** Cheio (as formas são preenchidas) ou contorno (só a linha, como um desenho técnico). */
    val contorno: Boolean = false,
    /** Tudo encaixa numa grade de 2 "pixels" da arte e círculo vira escada. */
    val pixel: Boolean = false,
    /** Raio do canto, em pixels da arte (a célula tem 32). */
    val raio: Float = 3f,
    /** Espessura do contorno, em pixels da arte. */
    val traco: Float = 1f,
    /** Linhas que brilham (neon). */
    val brilho: Boolean = false,
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

    /** O Mindustry de verdade é pixel art: cinzas frios, laranja do time e nada redondo. */
    val PIXEL = Pacote(
        "pixel", pixel = true, raio = 0f, traco = 2f,
        chao = Color(0xFF3A3A4A), chaoAlt = Color(0xFF34343F), junta = Color(0xFF2B2B36),
        corpo = Color(0xFF6B6B7A), claro = Color(0xFF9D9DAE), escuro = Color(0xFF45454F), fundo = Color(0xFF2B2B36),
        linha = Color(0xFF18181F),
        esteira = Color(0xFF4A4A57), esteiraBorda = Color(0xFF6B6B7A), seta = Color(0xFFFFD37F),
        distribuicao = Color(0xFFFFD37F), producao = Color(0xFF84F491), nucleo = Color(0xFFFFA664),
        alerta = Color(0xFFF25555), ok = Color(0xFF84F491), tinta = Color(0xFF18181F),
        placa = Color(0xFFFFD37F), placaTinta = Color(0xFF18181F), sombra = Color(0x66000000),
        tela = Color(0xFF253B2A), telaLuz = Color(0xFF84F491),
    )

    /** A planta da fábrica como desenho de engenheiro: linhas brancas sobre papel azul. */
    val PLANTA = Pacote(
        "planta", contorno = true, raio = 1f, traco = 1.3f,
        chao = Color(0xFF1E4D86), chaoAlt = Color(0xFF1E4D86), junta = Color(0x33FFFFFF),
        corpo = Color(0x14FFFFFF), claro = Color(0xFFDDEBFF), escuro = Color(0xFF9FC1EE), fundo = Color(0x0AFFFFFF),
        linha = Color(0xFFEAF2FF),
        esteira = Color(0x00000000), esteiraBorda = Color(0xFFEAF2FF), seta = Color(0xFFFFFFFF),
        distribuicao = Color(0xFFFFE38A), producao = Color(0xFFEAF2FF), nucleo = Color(0xFFFFC2A1),
        alerta = Color(0xFFFF8A80), ok = Color(0xFFB9F6CA), tinta = Color(0xFFEAF2FF),
        placa = Color(0xFF1E4D86), placaTinta = Color(0xFFEAF2FF), sombra = Color(0x00000000),
        tela = Color(0x22FFFFFF), telaLuz = Color(0xFFFFFFFF),
    )

    /** Placa de circuito no escuro: cada linha acende como neon. */
    val NEON = Pacote(
        "neon", contorno = true, brilho = true, raio = 4f, traco = 1.4f,
        chao = Color(0xFF07080D), chaoAlt = Color(0xFF07080D), junta = Color(0x2236E4F2),
        corpo = Color(0x10FFFFFF), claro = Color(0xFF9AF7FF), escuro = Color(0xFF2B6F7A), fundo = Color(0xFF0B0D15),
        linha = Color(0xFF36E4F2),
        esteira = Color(0x00000000), esteiraBorda = Color(0xFFFF4FD8), seta = Color(0xFFFF4FD8),
        distribuicao = Color(0xFFFF4FD8), producao = Color(0xFF36E4F2), nucleo = Color(0xFFFFC542),
        alerta = Color(0xFFFF5470), ok = Color(0xFF5CFF9D), tinta = Color(0xFF07080D),
        placa = Color(0xFF07080D), placaTinta = Color(0xFF9AF7FF), sombra = Color(0x00000000),
        tela = Color(0xFF041A1D), telaLuz = Color(0xFF36E4F2),
    )

    /** Desenho animado: cores claras, contorno grosso e tudo redondo, combinando com o Byte. */
    val DESENHO = Pacote(
        "desenho", raio = 7f, traco = 2.4f,
        chao = Color(0xFFF1E6CC), chaoAlt = Color(0xFFEADDBF), junta = Color(0xFFDCCCA8),
        corpo = Color(0xFFFFFFFF), claro = Color(0xFFFFFFFF), escuro = Color(0xFFCFC6E0), fundo = Color(0xFFF7F3FF),
        linha = Color(0xFF2B2440),
        esteira = Color(0xFF8E89A3), esteiraBorda = Color(0xFF2B2440), seta = Color(0xFFFFFFFF),
        distribuicao = Color(0xFFFFC94A), producao = Color(0xFF6ED3A8), nucleo = Color(0xFFFF8C69),
        alerta = Color(0xFFFF5A5F), ok = Color(0xFF6ED3A8), tinta = Color(0xFF2B2440),
        placa = Color(0xFFFFFFFF), placaTinta = Color(0xFF2B2440), sombra = Color(0x332B2440),
        tela = Color(0xFF2B2440), telaLuz = Color(0xFF6ED3A8),
    )

    val todos: List<Pacote> = listOf(METAL, PIXEL, PLANTA, NEON, DESENHO)

    fun porId(id: String): Pacote = todos.firstOrNull { it.id == id } ?: METAL
}

/** O pacote em uso. A tela da fábrica lê do save ao abrir; o painel troca. */
internal object PacoteAtual {
    var pacote by mutableStateOf(Pacotes.METAL)
}
