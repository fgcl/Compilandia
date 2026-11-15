package com.compilandia.content

// Guia do Trabalho Prático: construir, passo a passo, o compilador (Java → MIPS/MARS)
// da linguagem-exemplo Byteiro. Os capítulos moram em TpGuideA.kt … TpGuideD.kt; o 11, opcional, em TpGuideFuncoes.kt.

val tpGuide: List<GuideChapter> by lazy {
    listOf(tpCap1, tpCap2, tpCap3, tpCap4, tpCap5, tpCap6, tpCap7, tpCap8, tpCap9, tpCap10, tpCap11)
        // Cada capítulo fecha com "Na sua linguagem" e "Teste desta etapa" (TpGuideFechamento.kt).
        .map { it.copy(beats = it.beats + fechamentoDoCapitulo(it.number)) }
}
