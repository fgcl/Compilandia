package com.compilandia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

/** Gaveta fechada: etiqueta à esquerda, puxador à direita, luz e sombra nas bordas. */
@Composable
fun Drawer(label: String, modifier: Modifier = Modifier, note: String? = null, dimmed: Boolean = false, onClick: () -> Unit) {
    val body = Palette.drawer
    val hi = Palette.drawerHi
    val lo = Palette.drawerLo
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier.fillMaxWidth().alpha(if (dimmed) 0.78f else 1f).height(62.dp).clip(shape).background(body)
            .drawBehind {
                drawRect(hi, size = Size(size.width, 2.dp.toPx()))
                drawRect(lo, Offset(0f, size.height - 3.dp.toPx()), Size(size.width, 3.dp.toPx()))
            }
            .clickable { onClick() }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.weight(1f, fill = false).widthIn(min = 200.dp).clip(RoundedCornerShape(4.dp)).background(Palette.tag)
                .border(2.dp, Palette.tagInk, RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(label, color = Palette.tagInk, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (note != null) {
                Text(note, color = Palette.on(body), fontFamily = LocalFonts.current.mono, fontSize = 12.sp, maxLines = 1)
                Box(Modifier.width(8.dp))
            }
            Box(Modifier.size(46.dp, 12.dp).clip(RoundedCornerShape(6.dp)).background(Palette.drawerLo))
        }
    }
}

/** Gaveta aberta: a região em destaque, com as peças à mostra. */
@Composable
fun OpenDrawer(title: String, hint: String, onClick: () -> Unit, bins: @Composable RowScope.() -> Unit) {
    val onHazard = Palette.hazardDark
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.hazard)
            .clickable { onClick() }.padding(start = 11.dp, end = 11.dp, top = 9.dp, bottom = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = onHazard, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(hint, color = onHazard, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), content = bins)
    }
}

@Composable
fun RowScope.DrawerBin(value: String, label: String) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Palette.hazardDark).padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Palette.hazard, style = MaterialTheme.typography.titleLarge)
        Text(label, color = Palette.tag, fontFamily = LocalFonts.current.mono, fontSize = 12.sp, maxLines = 1)
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
