package com.compilandia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** As três faixas de largura do Material 3. Celular deitado conta como média. */
enum class Faixa { COMPACTA, MEDIA, EXPANDIDA }

/** O tamanho da janela e a faixa dela. As telas leem daqui, e só daqui decidem o arranjo. */
@Immutable
data class Janela(val largura: Dp, val altura: Dp) {
    val faixa: Faixa = faixaDe(largura, altura)

    /** Mais larga que alta, com folga: celular deitado, computador, tablet deitado. */
    val deitada: Boolean get() = largura > altura * 1.15f

    /** Palco e painel lado a lado: sobra largura e falta altura para empilhar. */
    val ladoALado: Boolean get() = faixa == Faixa.EXPANDIDA || (faixa == Faixa.MEDIA && deitada)
}

/**
 * A única função que decide a faixa.
 * Menos de 600 dp é compacta; de 600 a 840, média; 840 ou mais, expandida.
 * Celular deitado (mais largo que alto e baixo) fica na média, mesmo com mais de 840 dp.
 */
fun faixaDe(largura: Dp, altura: Dp): Faixa = when {
    largura >= 840.dp && altura >= 480.dp -> Faixa.EXPANDIDA
    largura >= 600.dp -> Faixa.MEDIA
    largura > altura -> Faixa.MEDIA
    else -> Faixa.COMPACTA
}

val LocalJanela = staticCompositionLocalOf { Janela(360.dp, 760.dp) }

/** Largura máxima do texto corrido. Linha mais longa que isso cansa o olho. */
val TEXTO_MAX = 640.dp

/** Largura máxima do jogo inteiro. Num monitor maior que isso, o jogo fica no meio, com a parede de furos dos lados. */
val APP_MAX = 1440.dp

/** Largura máxima de uma tela de lista quando a janela é larga. */
val LISTA_MAX = 760.dp

/** Texto corrido: ocupa a largura até [TEXTO_MAX] e fica no meio. */
fun Modifier.larguraDeLeitura(max: Dp = TEXTO_MAX): Modifier = fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = max)

/**
 * Tela de lista (mapa, ajustes, jogos, laboratório) numa janela larga: a coluna fica no meio,
 * com a parede de furos dos dois lados. Na janela estreita não muda nada.
 */
@Composable
fun ColunaCentral(max: Dp = LISTA_MAX, content: @Composable BoxScope.() -> Unit) {
    val j = LocalJanela.current
    if (j.largura <= max + 32.dp) {
        Box(Modifier.fillMaxSize(), content = content)
        return
    }
    Box(Modifier.fillMaxSize().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.fillMaxHeight().widthIn(max = max).fillMaxWidth().background(Palette.paper), content = content)
    }
}

/**
 * A janela que as telas enxergam. Até [APP_MAX] é a janela toda; num monitor maior, o jogo fica numa faixa
 * de [APP_MAX] no meio, para nenhuma tela espalhar texto e botões de uma borda à outra.
 */
@Composable
fun JanelaDoApp(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val largura = if (maxWidth > APP_MAX) APP_MAX else maxWidth
        val dentro: @Composable () -> Unit = { CompositionLocalProvider(LocalJanela provides Janela(largura, maxHeight), content = content) }
        if (maxWidth <= APP_MAX) { dentro(); return@BoxWithConstraints }
        Box(Modifier.fillMaxSize().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.fillMaxHeight().width(APP_MAX)) { dentro() }
        }
    }
}
