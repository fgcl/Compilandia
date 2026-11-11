package com.compilandia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Peças da oficina do Byte: faixa de perigo, pegboard, gavetas e ícones. */

@Composable
fun HazardStripe(modifier: Modifier = Modifier) {
    val light = Palette.hazard
    val dark = Palette.hazardDark
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        drawRect(dark)
        val w = 14.dp.toPx()
        val h = size.height
        var x = -h
        while (x < size.width) {
            drawPath(Path().apply {
                moveTo(x, h); lineTo(x + h, 0f); lineTo(x + h + w, 0f); lineTo(x + w, h); close()
            }, light)
            x += w * 2
        }
    }
}

/** Fundo furado de pegboard. */
fun Modifier.pegboard(hole: Color): Modifier = drawBehind {
    val step = 15.dp.toPx()
    val r = 2.dp.toPx()
    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            drawCircle(hole, r, Offset(x, y))
            x += step
        }
        y += step
    }
}

/** Manômetro do nível: o ponteiro é o progresso até o próximo nível. */
@Composable
fun XpGauge(xp: Int, progress: Float, modifier: Modifier = Modifier) {
    val track = Palette.drawer
    val fill = Palette.hazard
    Box(modifier.size(82.dp, 56.dp), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.size(82.dp, 56.dp)) {
            val stroke = 9.dp.toPx()
            val d = size.width - stroke
            val topLeft = Offset(stroke / 2, 8.dp.toPx())
            drawArc(track, 180f, 180f, false, topLeft, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(fill, 180f, 180f * progress.coerceIn(0f, 1f), false, topLeft, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text("$xp", color = Palette.ink, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun BoltIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val u = size.width / 24f
        drawPath(Path().apply {
            moveTo(13 * u, 2 * u); lineTo(4 * u, 14 * u); lineTo(10 * u, 14 * u); lineTo(9 * u, 22 * u)
            lineTo(19 * u, 9 * u); lineTo(13 * u, 9 * u); close()
        }, color)
    }
}

@Composable
fun GearIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(28.dp)) {
        val u = size.width / 24f
        val c = Offset(12 * u, 12 * u)
        val w = 2.4f * u
        drawCircle(color, 5 * u, c, style = Stroke(w))
        for (k in 0 until 8) {
            val a = k * 45.0 * kotlin.math.PI / 180.0
            val dx = kotlin.math.cos(a).toFloat()
            val dy = kotlin.math.sin(a).toFloat()
            drawLine(color, c + Offset(dx, dy) * (7.5f * u), c + Offset(dx, dy) * (10.5f * u), w, StrokeCap.Round)
        }
    }
}

/** Placa de metal da Oficina: luz em cima, sombra embaixo e um rebite em cada canto. */
private fun Modifier.placaDeMetal(corpo: Color, luz: Color, sombra: Color, rebite: Color): Modifier = this.background(corpo).drawBehind {
    drawRect(luz, size = Size(size.width, 2.dp.toPx()))
    drawRect(sombra, Offset(0f, size.height - 3.dp.toPx()), Size(size.width, 3.dp.toPx()))
    val r = 2.5f.dp.toPx(); val m = 7.dp.toPx()
    for (x in listOf(m, size.width - m)) for (y in listOf(m, size.height - m)) {
        drawCircle(sombra, r + 0.8f.dp.toPx(), Offset(x, y + 0.8f.dp.toPx()))
        drawCircle(rebite, r, Offset(x, y))
    }
}

/** O número da gaveta numa plaquinha da cor da região. */
@Composable
private fun NumeroDaGaveta(numero: Int, cor: Color) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(cor).drawBehind {
        drawRect(Color.Black.copy(alpha = 0.22f), Offset(0f, size.height - 3.dp.toPx()), Size(size.width, 3.dp.toPx()))
    }, contentAlignment = Alignment.Center) {
        Text("$numero", color = Palette.on(cor), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BarraDaGaveta(progresso: Float, trilho: Color, cor: Color) {
    Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(7.dp).clip(RoundedCornerShape(4.dp)).background(trilho)) {
        Box(Modifier.fillMaxWidth(progresso.coerceIn(0.02f, 1f)).height(7.dp).background(cor))
    }
}

/**
 * Gaveta fechada (Oficina caprichada, mural rodada 5): placa de metal com rebites,
 * o número na cor da região, o nome direto na placa e a barra de progresso embaixo.
 */
@Composable
fun Drawer(
    label: String, modifier: Modifier = Modifier, note: String? = null, dimmed: Boolean = false,
    numero: Int? = null, cor: Color = Palette.drawerHi, progresso: Float? = null,
    fim: (@Composable () -> Unit)? = null, onClick: () -> Unit,
) {
    val body = Palette.drawer
    Row(
        modifier.fillMaxWidth().alpha(if (dimmed) 0.72f else 1f).heightIn(min = 64.dp).clip(RoundedCornerShape(9.dp))
            .placaDeMetal(body, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi)
            .clickable { onClick() }.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (numero != null) NumeroDaGaveta(numero, cor)
        Column(Modifier.weight(1f)) {
            Text(label, color = Palette.on(body), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (progresso != null) BarraDaGaveta(progresso, Palette.drawerLo, cor)
        }
        if (fim != null) fim()
        else if (note != null) Text(note, color = Palette.on(body), fontFamily = LocalFonts.current.mono, fontSize = 12.sp, maxLines = 1)
    }
}

/** Gaveta aberta: a região em que você está, a placa amarela da oficina. Tocar entra na região. */
@Composable
fun OpenDrawer(
    title: String, hint: String, numero: Int? = null, cor: Color = Palette.drawer, progresso: Float? = null,
    nota: String? = null, fim: (@Composable () -> Unit)? = null, onClick: () -> Unit,
) {
    val onHazard = Palette.hazardDark
    Row(
        Modifier.fillMaxWidth().heightIn(min = 76.dp).clip(RoundedCornerShape(10.dp))
            .placaDeMetal(Palette.hazard, Color.White.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.25f))
            .clickable { onClick() }.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (numero != null) NumeroDaGaveta(numero, cor)
        Column(Modifier.weight(1f)) {
            Text(title, color = onHazard, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (nota != null) Text(nota, color = onHazard.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (progresso != null) BarraDaGaveta(progresso, onHazard.copy(alpha = 0.25f), onHazard)
        }
        if (fim != null) fim()
        else Text(hint, color = onHazard, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** Etiqueta curta da barra de ferramentas do mapa. */
@Composable
fun RowScope.ToolTag(text: String, highlighted: Boolean = false, onClick: () -> Unit) {
    val bg = if (highlighted) Palette.hazard else Palette.drawer
    Box(
        Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(7.dp)).background(bg).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (highlighted) Palette.hazardDark else Palette.on(bg), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
