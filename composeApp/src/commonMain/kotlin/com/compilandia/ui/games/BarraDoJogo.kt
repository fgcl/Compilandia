package com.compilandia.ui.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.compilandia.ui.BigButton
import com.compilandia.ui.Palette

/** A ação principal da rodada (Conferir, Continuar, Concluir...), que mora na barra de baixo da partida. */
class AcaoDoJogo(val texto: String, val cor: Color, val corTexto: Color, val ativo: Boolean, val faz: () -> Unit)

/** A barra de baixo da partida guarda a ação principal que o jogo pediu. */
class BarraDoJogo {
    var acao by mutableStateOf<AcaoDoJogo?>(null)
}

/** Dentro de uma partida há uma barra; fora (aulas, testes de captura), o botão fica no lugar. */
val LocalBarraDoJogo = staticCompositionLocalOf<BarraDoJogo?> { null }

/**
 * O botão principal de um jogo. Na partida ele sobe para a barra de baixo, perto do polegar,
 * junto de Dica e Não entendi (mural, rodada 9). Fora dela, é um botão comum no meio do jogo.
 */
@Composable
fun BotaoDoJogo(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Palette.action,
    textColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    small: Boolean = false,
    onClick: () -> Unit,
) {
    val barra = LocalBarraDoJogo.current
    if (barra == null) {
        BigButton(text, modifier, color = color, textColor = textColor, enabled = enabled, small = small, onClick = onClick)
        return
    }
    val atual by rememberUpdatedState(onClick)
    val faz = remember { { atual() } }
    SideEffect {
        val a = barra.acao
        if (a == null || a.faz !== faz || a.texto != text || a.cor != color || a.ativo != enabled) barra.acao = AcaoDoJogo(text, color, textColor, enabled, faz)
    }
    DisposableEffect(barra) { onDispose { if (barra.acao?.faz === faz) barra.acao = null } }
}

/** O botão da ação da rodada, desenhado em cima da barra de baixo. Some quando o jogo não pediu nenhum. */
@Composable
fun AcaoDaBarra(barra: BarraDoJogo, embaixo: androidx.compose.ui.unit.Dp = 8.dp) {
    val a = barra.acao ?: return
    Box(Modifier.fillMaxWidth().background(Palette.paperDark).padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = embaixo)) {
        BigButton(a.texto, Modifier.fillMaxWidth().testTag("acao-do-jogo"), color = a.cor, textColor = a.corTexto, enabled = a.ativo) { a.faz() }
    }
}
