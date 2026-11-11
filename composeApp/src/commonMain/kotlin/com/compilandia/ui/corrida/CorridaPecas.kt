package com.compilandia.ui.corrida

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.ui.Palette
import com.compilandia.ui.pegboard
import com.compilandia.ui.placaDeMetal

/**
 * A Oficina da corrida (mural, rodada 10): duas peças em placas de metal, cada uma com o desenho
 * e o nome, sem frase. Um toque leva a peça e abre a próxima sala.
 */
@Composable
internal fun ColumnScope.Oficina(ofertas: List<Peca>, onEscolhe: (Peca) -> Unit) {
    Column(
        Modifier.weight(1f).fillMaxWidth().pegboard(Palette.drawerHi.copy(alpha = 0.14f)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(tx("corridascreen_oficina_escolha_peca"), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (o in ofertas) Column(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).placaDeMetal(Palette.drawer, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi)
                    .clickable { onEscolhe(o) }.padding(vertical = 22.dp).testTag("peca-${o.name}"),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PecaIcone(o, 72.dp)
                Text(o.nome, color = Palette.on(Palette.drawer), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** O desenho de cada peça: escudo, lâmpada, chave inglesa e ímã. Desenhados, porque emoji pode faltar na fonte. */
@Composable
internal fun PecaIcone(p: Peca, tamanho: Dp) {
    val tinta = Palette.inkOnLight
    Canvas(Modifier.size(tamanho)) {
        when (p) {
            Peca.ESCUDO -> escudo(Palette.teal, tinta)
            Peca.DICA -> lampada(Palette.hazard, tinta)
            Peca.REPARO -> chave(Color(0xFFB8C2C6), tinta)
            Peca.IMA -> ima(Palette.red, Color(0xFFDDE3E5), tinta)
        }
    }
}

private fun DrawScope.contorno() = Stroke(size.width * 0.05f, cap = StrokeCap.Round)

private fun DrawScope.escudo(cor: Color, tinta: Color) {
    val w = size.width; val h = size.height
    val p = Path().apply {
        moveTo(w * 0.5f, h * 0.08f); lineTo(w * 0.86f, h * 0.2f); lineTo(w * 0.82f, h * 0.55f)
        quadraticTo(w * 0.74f, h * 0.8f, w * 0.5f, h * 0.93f); quadraticTo(w * 0.26f, h * 0.8f, w * 0.18f, h * 0.55f)
        lineTo(w * 0.14f, h * 0.2f); close()
    }
    drawPath(p, cor); drawPath(p, tinta, style = contorno())
    drawLine(tinta.copy(alpha = 0.5f), Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), w * 0.04f)
}

private fun DrawScope.lampada(cor: Color, tinta: Color) {
    val w = size.width; val h = size.height
    drawCircle(cor, w * 0.28f, Offset(w * 0.5f, h * 0.4f)); drawCircle(tinta, w * 0.28f, Offset(w * 0.5f, h * 0.4f), style = contorno())
    val base = Size(w * 0.26f, h * 0.2f); val o = Offset(w * 0.37f, h * 0.66f)
    drawRoundRect(Color(0xFFB8C2C6), o, base, CornerRadius(w * 0.04f)); drawRoundRect(tinta, o, base, CornerRadius(w * 0.04f), style = contorno())
    for (k in 1..2) drawLine(tinta, Offset(o.x, o.y + base.height * k / 3f), Offset(o.x + base.width, o.y + base.height * k / 3f), w * 0.03f)
}

private fun DrawScope.chave(cor: Color, tinta: Color) {
    val w = size.width; val h = size.height
    rotate(-45f) {
        val cabo = Size(w * 0.16f, h * 0.5f); val o = Offset(w * 0.42f, h * 0.42f)
        drawRoundRect(cor, o, cabo, CornerRadius(w * 0.06f)); drawRoundRect(tinta, o, cabo, CornerRadius(w * 0.06f), style = contorno())
        val c = Offset(w * 0.5f, h * 0.3f)
        drawCircle(cor, w * 0.2f, c); drawCircle(tinta, w * 0.2f, c, style = contorno())
        // A boca da chave: um corte no alto da cabeça.
        drawRect(Palette.drawer, Offset(w * 0.43f, h * 0.08f), Size(w * 0.14f, h * 0.2f))
    }
}

private fun DrawScope.ima(cor: Color, ponta: Color, tinta: Color) {
    val w = size.width; val h = size.height; val g = w * 0.18f
    val arco = Stroke(g, cap = StrokeCap.Butt)
    drawArc(cor, 0f, 180f, false, Offset(w * 0.22f, h * 0.3f), Size(w * 0.56f, h * 0.5f), style = arco)
    for (x in listOf(w * 0.22f, w * 0.78f)) {
        drawLine(cor, Offset(x, h * 0.55f), Offset(x, h * 0.3f), g)
        drawLine(ponta, Offset(x, h * 0.3f), Offset(x, h * 0.14f), g)
    }
    drawArc(tinta, 0f, 180f, false, Offset(w * 0.22f - g / 2, h * 0.3f - g / 2), Size(w * 0.56f + g, h * 0.5f + g), style = contorno())
}
