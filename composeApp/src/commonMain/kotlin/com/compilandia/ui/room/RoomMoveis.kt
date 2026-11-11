package com.compilandia.ui.room

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen

/**
 * Um móvel que se toca: a área cobre o desenho do móvel em room_base, como fração do lado da sala.
 * A plaquinha fica presa no próprio móvel, em ([px], [py]).
 */
private data class Movel(val x: Float, val y: Float, val w: Float, val h: Float, val px: Float, val py: Float, val tag: String)

// Medido na arte da sala (scripts/art): quadro de ferramentas, mesa com monitor, gaveteiro e caixas.
private val moveis = listOf(
    Movel(0.10f, 0.20f, 0.20f, 0.27f, 0.20f, 0.23f, "movel-jogar"),
    Movel(0.16f, 0.33f, 0.28f, 0.31f, 0.34f, 0.35f, "movel-lab"),
    Movel(0.48f, 0.18f, 0.23f, 0.38f, 0.60f, 0.22f, "movel-mapa"),
    Movel(0.35f, 0.70f, 0.21f, 0.18f, 0.46f, 0.74f, "movel-loja"),
)

/** Os móveis que abrem os lugares da oficina. Ficam por cima da arte e por baixo do Byte. */
@Composable
internal fun MoveisDaSala(app: AppState, side: Dp, desafioPendente: Boolean, onJogar: () -> Unit) {
    val fontes = remember { moveis.map { MutableInteractionSource() } }
    fun abre(m: Movel): () -> Unit = when (m.tag) {
        "movel-jogar" -> onJogar
        "movel-lab" -> { { app.go(Screen.Lab) } }
        "movel-mapa" -> { { app.go(Screen.Map) } }
        else -> { { app.go(Screen.Shop) } }
    }
    // primeiro as áreas, depois as plaquinhas: nenhuma área cobre a plaquinha de outro móvel
    moveis.forEachIndexed { i, m ->
        Box(
            Modifier.offset(side * m.x, side * m.y).size(side * m.w, side * m.h).testTag(m.tag)
                .clickable(fontes[i], null, role = Role.Button) { Juice.click(); abre(m)() },
        )
    }
    moveis.forEachIndexed { i, m -> Placa(side, m, fontes[i], nome(m), marca = m.tag == "movel-jogar" && desafioPendente, onClick = abre(m)) }
}

@Composable
private fun nome(m: Movel): String = when (m.tag) {
    "movel-jogar" -> tx("sala_jogar")
    "movel-lab" -> tx("comum_laboratorio")
    "movel-mapa" -> tx("roomscreen_mapa")
    else -> tx("roomscreen_loja")
}

/** A plaquinha: uma palavra só, presa no móvel. Acende quando o dedo ou o mouse está no móvel. */
@Composable
private fun Placa(side: Dp, m: Movel, fonte: MutableInteractionSource, texto: String, marca: Boolean, onClick: () -> Unit) {
    val apertado by fonte.collectIsPressedAsState()
    val passando by fonte.collectIsHoveredAsState()
    val escala by animateFloatAsState(if (apertado) 1.12f else if (passando) 1.06f else 1f)
    val acesa = apertado || passando
    Box(Modifier.offset(side * m.px, side * m.py).size(0.dp).wrapContentSize(unbounded = true), contentAlignment = Alignment.Center) {
        Box(
            Modifier.scale(escala).clip(RoundedCornerShape(6.dp)).background(if (acesa) Palette.hazard else Color(0xE6161A1D))
                .border(1.5.dp, Palette.hazard, RoundedCornerShape(6.dp)).clickable(fonte, null) { Juice.click(); onClick() }
                .padding(horizontal = 9.dp, vertical = 3.dp),
        ) {
            Text(texto, color = if (acesa) Palette.hazardDark else Color(0xFFF2EEE3), fontFamily = LocalFonts.current.display, style = MaterialTheme.typography.labelLarge)
            if (marca) Box(Modifier.align(Alignment.TopEnd).offset(8.dp, (-6).dp).size(9.dp).clip(CircleShape).background(Palette.hazard).border(1.5.dp, Color(0xFF161A1D), CircleShape))
        }
    }
}
