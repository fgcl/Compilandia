@file:OptIn(ExperimentalLayoutApi::class)

package com.compilandia.ui

import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.compilandia.textos.tx

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.EOF
import com.compilandia.engine.EPS
import com.compilandia.engine.Grammar
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound

/** Botão de oficina: chapa com a borda de baixo mais escura, como uma tecla de máquina. */
@Composable
fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Palette.action,
    textColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    small: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(if (small) 8.dp else 10.dp)
    val bg = if (enabled) color else Palette.paperDark
    val fg = when {
        !enabled -> Palette.inkSoft
        textColor != Color.Unspecified -> textColor
        else -> Palette.on(bg)
    }
    val edge = if (color == Palette.white) Palette.ink.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f).compositeOver(color)
    Box(
        modifier
            .clip(shape)
            .background(if (enabled) edge else Palette.paperDark)
            .padding(bottom = if (enabled) 4.dp else 0.dp)
            .clip(shape)
            .background(bg)
            .then(if (color == Palette.white || !enabled) Modifier.border(1.5.dp, Palette.ink.copy(alpha = 0.3f), shape) else Modifier)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = if (small) 12.dp else 18.dp, vertical = if (small) 7.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            leading?.invoke()
            Text(
                text,
                color = fg,
                style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    selected: Boolean = false,
    color: Color = Palette.purple,
    mono: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    val fonts = LocalFonts.current
    Box(
        modifier
            .clip(shape)
            .background(if (selected) color else Palette.white)
            .border(2.dp, if (selected) Palette.ink else color.copy(alpha = if (enabled) 0.7f else 0.25f), shape)
            .then(if (onClick != null && enabled) Modifier.clickable { Sound.play(Sfx.CLICK); onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            color = if (selected) Palette.on(color) else if (enabled) Palette.ink else Palette.inkSoft.copy(alpha = 0.5f),
            fontFamily = if (mono) fonts.mono else fonts.body,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = Palette.white,
    border: Color = Palette.ink,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .clip(shape)
            .background(color)
            .border(BorderStroke(1.5.dp, border.copy(alpha = 0.35f)), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.headlineSmall, modifier = modifier)
}

@Composable
fun Header(title: String, subtitle: String? = null, color: Color = Palette.drawer, actions: @Composable RowScope.() -> Unit = {}) {
    val app = AppState.current
    val fg = Palette.on(color)
    Column {
    Row(
        Modifier.fillMaxWidth().background(color).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app.stack.size > 1) {
            BotaoVoltar(fg)
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = fg, maxLines = 2)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = fg)
        }
        actions()
    }
    HazardStripe()
    }
}

@Composable
fun ProgressBar(value: Float, color: Color = Palette.teal, modifier: Modifier = Modifier, height: Dp = 10.dp) {
    val v by animateFloatAsState(value.coerceIn(0f, 1f))
    val shape = RoundedCornerShape(height)
    Box(modifier.height(height).clip(shape).background(Palette.paperDark).border(1.dp, Palette.ink.copy(alpha = 0.3f), shape)) {
        Box(Modifier.fillMaxWidth(v).height(height).clip(shape).background(color))
    }
}

/** Gramática com não-terminais e terminais nas cores de destaque do tema. */
@Composable
fun GrammarText(g: Grammar, modifier: Modifier = Modifier, highlightProd: Int? = null) {
    val fonts = LocalFonts.current
    Column(modifier.horizontalScroll(rememberScrollState())) {
        for (nt in g.nonTerminals) {
            val prods = g.productionsOf(nt)
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Palette.nonTerminal, fontWeight = FontWeight.Bold)) { append(nt) }
                    append(" → ")
                    prods.forEachIndexed { i, p ->
                        if (i > 0) append("  |  ")
                        val hl = p.id == highlightProd
                        if (hl) pushStyle(SpanStyle(background = Palette.yellow.copy(alpha = 0.6f)))
                        if (p.rhs.isEmpty()) append(EPS)
                        p.rhs.forEachIndexed { j, s ->
                            if (j > 0) append(" ")
                            symbolSpan(g, s)
                        }
                        if (hl) pop()
                    }
                },
                fontFamily = fonts.mono,
                fontSize = 15.sp,
                softWrap = false,
            )
        }
    }
}

fun AnnotatedString.Builder.symbolSpan(g: Grammar, s: String) {
    val color = when {
        g.isNonTerminal(s) -> Palette.nonTerminal
        s == EOF -> Palette.orange
        else -> Palette.terminal
    }
    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(s) }
}

@Composable
fun SymbolsText(g: Grammar, symbols: List<String>, modifier: Modifier = Modifier, size: Int = 16) {
    Text(
        buildAnnotatedString {
            if (symbols.isEmpty()) append(EPS)
            symbols.forEachIndexed { i, s -> if (i > 0) append(" "); symbolSpan(g, s) }
        },
        fontFamily = LocalFonts.current.mono,
        fontSize = size.sp,
        modifier = modifier,
    )
}

@Composable
fun Mono(text: String, modifier: Modifier = Modifier, color: Color = Palette.ink, size: Int = 14, bold: Boolean = false) {
    Text(text, fontFamily = LocalFonts.current.mono, fontSize = size.sp, color = color, modifier = modifier,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
}

@Composable
fun CodeBlock(code: String, caption: String = "") {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.codeBg)
                .horizontalScroll(rememberScrollState()).padding(12.dp)
        ) {
            Text(code, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = Color(0xFFE8E6F0), softWrap = false)
        }
        if (caption.isNotBlank()) Text(caption, style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Texto com **negrito** e `código` simples. */
@Composable
fun RichText(text: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge, color: Color = Palette.ink) {
    val mono = LocalFonts.current.mono
    Text(
        buildAnnotatedString {
            val codigo = mutableListOf<IntRange>()
            var i = 0
            while (i < text.length) {
                when {
                    text.startsWith("**", i) -> {
                        val end = text.indexOf("**", i + 2)
                        if (end < 0) { append(text.substring(i)); break }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                        i = end + 2
                    }
                    text[i] == '`' -> {
                        val end = text.indexOf('`', i + 1)
                        if (end < 0) { append(text.substring(i)); break }
                        val de = length
                        withStyle(SpanStyle(fontFamily = mono, background = Palette.paperDark, color = Palette.purple)) { append(text.substring(i + 1, end)) }
                        codigo += de until length
                        i = end + 1
                    }
                    else -> { append(text[i]); i++ }
                }
            }
            // A8: termos do glossário viram links que abrem o balão.
            marcarGlossario(toString(), codigo)
        },
        style = style,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

/** Anel de domínio (0..1). */
@Composable
fun MasteryRing(value: Double, color: Color, size: Dp = 46.dp, label: String? = null) {
    val v by animateFloatAsState(value.toFloat().coerceIn(0f, 1f))
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(size)) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = this.size.minDimension * 0.13f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val inset = stroke.width / 2
            drawArc(Palette.paperDark, 0f, 360f, false, topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - 2 * inset, this.size.height - 2 * inset), style = stroke)
            drawArc(color, -90f, 360f * v, false, topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - 2 * inset, this.size.height - 2 * inset), style = stroke)
        }
        Text(label ?: "${(v * 100).toInt()}%", fontSize = (size.value / 4).sp, fontWeight = FontWeight.Bold, color = Palette.ink)
    }
}

/** Faixa de feedback depois de uma jogada. */
@Composable
fun FeedbackBanner(ok: Boolean, text: String) {
    val c = if (ok) Palette.green else Palette.red
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.copy(alpha = 0.15f)).border(2.dp, c, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (ok) "✅" else "🤔", fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        RichText(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SpeechBubble(emoji: String, name: String, text: String, color: Color = Palette.white, rightSide: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = if (rightSide) Arrangement.End else Arrangement.Start) {
        if (!rightSide) Avatar(emoji)
        Column(Modifier.weight(1f, fill = false).padding(horizontal = 8.dp)) {
            Text(name, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            Box(
                Modifier.clip(RoundedCornerShape(16.dp)).background(color).border(2.dp, Palette.ink, RoundedCornerShape(16.dp)).padding(12.dp)
            ) { RichText(text) }
        }
        if (rightSide) Avatar(emoji)
    }
}

@Composable
fun Avatar(emoji: String, size: Dp = 44.dp) {
    val art = characterArt(emoji)
    val cast = emoji == "🤖" || emoji == "🦉"
    Box(
        Modifier.size(size).clip(RoundedCornerShape(50)).background(if (art != null || cast) Palette.paperDark else Palette.yellow).border(2.dp, Palette.ink, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            emoji == "🤖" -> com.compilandia.ui.cast.ByteFigure(Modifier.height(size * 0.86f), animate = false)
            emoji == "🦉" -> com.compilandia.ui.cast.ProfFigure(Modifier.size(size * 0.86f), animate = false)
            art != null -> androidx.compose.foundation.Image(org.jetbrains.compose.resources.painterResource(art), emoji, Modifier.size(size * 0.82f))
            else -> Text(emoji, fontSize = (size.value * 0.5f).sp)
        }
    }
}

@Composable
fun Pill(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(color).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = Palette.on(color), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun VSpace(h: Int) = Spacer(Modifier.height(h.dp))

/**
 * O botão de voltar do cabeçalho. Telas com cabeçalho próprio (mapa, corrida) usam o mesmo,
 * para que toda tela tenha uma saída no mesmo lugar, no canto de cima à esquerda.
 */
@Composable
fun BotaoVoltar(fg: Color, aoVoltar: (() -> Unit)? = null) {
    val app = AppState.current
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(fg.copy(alpha = 0.18f))
            .clickable { Sound.play(Sfx.CLICK); if (aoVoltar != null) aoVoltar() else app.back() }
            .semantics { contentDescription = tx("comum_voltar") }.testTag("voltar"),
        contentAlignment = Alignment.Center,
    ) { Text("←", color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
}
