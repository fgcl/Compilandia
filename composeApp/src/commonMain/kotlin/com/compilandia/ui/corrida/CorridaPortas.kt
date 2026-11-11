package com.compilandia.ui.corrida

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.learning.Mission
import com.compilandia.textos.tx
import com.compilandia.ui.BoltIcon
import com.compilandia.ui.GearIcon
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.pegboard
import com.compilandia.ui.placaDeMetal

/** Uma porta da corrida: perigosa é mais difícil e sem dica; a da oficina pula a sala e dá uma peça. */
internal data class Porta(val titulo: String, val premio: String, val cor: Color, val perigosa: Boolean, val oficina: Boolean)

/**
 * As três portas, como portas de metal da Oficina: rebites, uma plaquinha com o nome e o prêmio desenhado.
 * A perigosa é a mais alta e tem a faixa zebrada. O assunto da sala aparece uma vez, no alto.
 */
@Composable
internal fun ColumnScope.Portas(m: Mission, onEscolhe: (Porta) -> Unit) {
    val portas = listOf(
        Porta(tx("corridascreen_tranquila"), "+10", Palette.green, perigosa = false, oficina = false),
        Porta(tx("corridascreen_perigosa"), "+20", Palette.red, perigosa = true, oficina = false),
        Porta(tx("comum_oficina"), tx("corridascreen_peca"), Palette.teal, perigosa = false, oficina = true),
    )
    Column(Modifier.weight(1f).fillMaxWidth().pegboard(Palette.drawerHi.copy(alpha = 0.14f)).padding(horizontal = 12.dp).padding(top = 16.dp)) {
        Text(m.skill.title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, color = Palette.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
            portas.forEachIndexed { i, p -> PortaDeMetal(p, Modifier.weight(1f).fillMaxHeight(if (i == 1) 0.92f else 0.8f).testTag("porta-$i")) { Juice.click(); onEscolhe(p) } }
        }
    }
}

@Composable
private fun PortaDeMetal(p: Porta, modifier: Modifier, onClick: () -> Unit) {
    val corpo = Palette.drawer
    Box(
        modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .placaDeMetal(corpo, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi).clickable { onClick() },
    ) {
        // Dois painéis em relevo, como numa porta de verdade.
        val linha = Palette.drawerLo
        val luz = Palette.drawerHi
        Canvas(Modifier.matchParentSize()) {
            val m = 12.dp.toPx(); val r = CornerRadius(5.dp.toPx()); val t = 2.dp.toPx()
            val topo = size.height * 0.24f; val meio = size.height * 0.56f; val fim = size.height * 0.8f
            for ((y0, y1) in listOf(topo to meio - m / 2, meio + m / 2 to fim)) {
                drawRoundRect(linha, Offset(m, y0), Size(size.width - 2 * m, y1 - y0), r, style = Stroke(t))
                drawRoundRect(luz, Offset(m + t, y0 + t), Size(size.width - 2 * m - 2 * t, y1 - y0 - 2 * t), r, style = Stroke(t / 2))
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (p.perigosa) HazardStripe() else Spacer(Modifier.height(10.dp))
            Spacer(Modifier.height(12.dp))
            // A plaquinha com o nome, na cor da porta.
            Box(Modifier.padding(horizontal = 8.dp).clip(RoundedCornerShape(6.dp)).background(p.cor).padding(horizontal = 8.dp, vertical = 5.dp)) {
                Text(p.titulo, color = Palette.on(p.cor), style = MaterialTheme.typography.titleSmall, maxLines = 1)
            }
            Spacer(Modifier.weight(1f))
            // O prêmio: engrenagem para as salas, raio para a peça da oficina.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (p.oficina) BoltIcon(Palette.hazard, Modifier.size(26.dp)) else GearIcon(Palette.hazard, Modifier.size(26.dp))
                Text(p.premio, color = Palette.hazard, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(18.dp))
        }
        // A maçaneta.
        Box(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(10.dp, 26.dp).clip(RoundedCornerShape(5.dp)).background(Palette.hazard))
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(6.dp).background(Palette.drawerLo))
        Spacer(Modifier.width(0.dp))
    }
}
