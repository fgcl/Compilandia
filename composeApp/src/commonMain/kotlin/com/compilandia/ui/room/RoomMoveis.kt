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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.NaAula
import com.compilandia.ui.room.art.P3

/**
 * Um móvel que se toca: a área cobre a [caixa] do móvel no mundo da sala de aula (ui/room/art/SalaDeAulaMoveis.kt),
 * projetada no quadrado. A plaquinha fica presa no ponto [placa] do mundo.
 */
private data class Movel(val caixa: Pair<P3, P3>, val placa: P3, val tag: String) {
    private val iso get() = Iso.aula(1f)
    val area: Rect get() {
        val (o, t) = caixa
        val us = listOf(0f, t.u)
        val vs = listOf(0f, t.v)
        val cantos = us.flatMap { du -> vs.flatMap { dv -> listOf(iso.p(o + P3(du, dv)), iso.p(o + P3(du, dv, t.z))) } }
        return Rect(cantos.minOf { it.x }, cantos.minOf { it.y }, cantos.maxOf { it.x }, cantos.maxOf { it.y })
    }
    val ponto: Offset get() = iso.p(placa)
}

// O quadro é o mapa de aulas, o computador da mesa é o Jogar, a tela de projeção o Laboratório e o mural a Loja.
private val moveis = listOf(
    Movel(NaAula.COMPUTADOR, P3(10.5f, 1.4f, 2.1f), "movel-jogar"),
    Movel(NaAula.TELA, P3(9.0f, 0f, 4.45f), "movel-lab"),
    Movel(NaAula.QUADRO, P3(2.5f, 0f, 3.5f), "movel-mapa"),
    Movel(NaAula.MURAL, P3(0f, 3.1f, 3.15f), "movel-loja"),
)

/** Os móveis que abrem os lugares da sala. Ficam por cima da arte e por baixo do professor. */
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
            Modifier.offset(side * m.area.left, side * m.area.top).size(side * m.area.width,
                side * m.area.height).testTag(m.tag)
                .clickable(fontes[i], null, role = Role.Button) { Juice.click(); abre(m)() },
        )
    }
    moveis.forEachIndexed { i, m -> Placa(side, m, fontes[i], nome(m),
        marca = m.tag == "movel-jogar" && desafioPendente, onClick = abre(m)) }
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
private fun Placa(side: Dp, m: Movel, fonte: MutableInteractionSource, texto: String, marca: Boolean,
    onClick: () -> Unit) {
    val apertado by fonte.collectIsPressedAsState()
    val passando by fonte.collectIsHoveredAsState()
    val escala by animateFloatAsState(if (apertado) 1.12f else if (passando) 1.06f else 1f)
    val acesa = apertado || passando
    Box(Modifier.offset(side * m.ponto.x, side * m.ponto.y).size(0.dp).wrapContentSize(unbounded = true),
        contentAlignment = Alignment.Center) {
        Box(
            Modifier.scale(escala).clip(RoundedCornerShape(6.dp)).background(if (acesa) Palette.hazard else Color(0xE6161A1D))
                .border(1.5.dp, Palette.hazard, RoundedCornerShape(6.dp)).clickable(fonte,
                    null) { Juice.click(); onClick() }
                .padding(horizontal = 9.dp, vertical = 3.dp),
        ) {
            Text(texto, color = if (acesa) Palette.hazardDark else Color(0xFFF2EEE3),
                fontFamily = LocalFonts.current.display, style = MaterialTheme.typography.labelLarge)
            if (marca) Box(Modifier.align(Alignment.TopEnd).offset(8.dp,
                (-6).dp).size(9.dp).clip(CircleShape).background(Palette.hazard).border(1.5.dp, Color(0xFF161A1D),
                    CircleShape))
        }
    }
}
