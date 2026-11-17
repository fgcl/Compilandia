package com.compilandia.ui.fabgram

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Desenhos alternativos de uma máquina: outra silhueta, outra ideia, o mesmo bloco.
 * O pacote de textura muda a pintura; a variante muda o desenho. As duas escolhas se combinam.
 *
 * Variante nova: escreva o desenho (Conceitos*.kt) e acrescente em [porTipo] com um id estável.
 */
internal class Variante(val id: String, val desenho: Desenho)

internal object Variantes {
    /**
     * Duas linhas de desenho para cada máquina:
     * "diagrama", como o shapez (laje recortada, ícone grande da função, setas do fluxo e as fichas da regra);
     * "industrial", como o Mindustry (octógono chanfrado, respiros que esquentam, mecanismo girando).
     */
    val porTipo: Map<String, List<Variante>> = mapOf(
        "prensa" to par(DrawScope::prensaDiagrama, DrawScope::prensaIndustrial),
        "cofre" to par(DrawScope::cofreDiagrama, DrawScope::cofreIndustrial),
        "braco" to par(DrawScope::bracoDiagrama, DrawScope::bracoIndustrial),
        "armario" to par(DrawScope::armarioDiagrama, DrawScope::armarioIndustrial),
        "separador" to par(DrawScope::separadorDiagrama, DrawScope::separadorIndustrial),
        "roteador" to par(DrawScope::roteadorDiagrama, DrawScope::roteadorIndustrial),
        "fonte" to par(DrawScope::fonteDiagrama, DrawScope::fonteIndustrial),
        "montadora" to par(DrawScope::montadoraDiagrama, DrawScope::montadoraIndustrial),
        "cesto" to par(DrawScope::cestoDiagrama, DrawScope::cestoIndustrial),
    )

    private fun par(diagrama: Desenho, industrial: Desenho) =
        listOf(Variante(DIAGRAMA, diagrama), Variante(INDUSTRIAL, industrial))

    const val DIAGRAMA = "diagrama"
    const val INDUSTRIAL = "industrial"

    /**
     * A escolha do dono na prancha (rodada 2): diagrama em quase tudo; o cesto e o roteador
     * ficaram com o desenho de antes, então não entram aqui.
     */
    val PADRAO: Map<String, String> = listOf("prensa", "cofre", "braco", "armario", "separador", "fonte", "montadora")
        .associateWith { DIAGRAMA }

    /** A variante em uso por tipo; começa com a escolha do dono. */
    val escolhas = mutableStateMapOf<String, String>().apply { putAll(PADRAO) }

    fun escolhida(tipo: String): Variante? = escolhas[tipo]?.let { id -> porTipo[tipo]?.firstOrNull { it.id == id } }
}
