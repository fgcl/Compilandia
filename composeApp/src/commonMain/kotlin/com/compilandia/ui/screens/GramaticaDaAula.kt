package com.compilandia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Episode
import com.compilandia.content.Look
import com.compilandia.textos.tx
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.placaDeMetal

/**
 * A gramática em uso na aula: a última que apareceu no palco até a cena [index].
 * Vale linha de código (CODE) ou título com seta, como o "E -> E + n | n" do empilha-reduz.
 */
fun gramaticaDaAula(ep: Episode, index: Int): List<String>? {
    for (k in index.coerceAtMost(ep.passos.lastIndex) downTo 0) {
        val linhas = ep.passos[k].shot.boxes
            .filter { (it.look == Look.CODE || it.look == Look.TITLE) && "->" in it.text && it.text.count { c -> c == '(' } < 3 && "void" !in it.text }
            .sortedBy { it.y }.map { it.text.trim() }.distinct()
        if (linhas.isNotEmpty()) return linhas
    }
    return null
}

/** O botão da barra de cima, do tamanho do CC. Aceso enquanto o painel está aberto. */
@Composable
fun BotaoGramatica(aberto: Boolean, onClick: () -> Unit) {
    val fundo = if (aberto) Palette.hazard else Palette.white
    val tinta = if (aberto) Palette.hazardDark else Palette.ink
    Row(
        Modifier.clip(RoundedCornerShape(6.dp)).background(fundo).clickable { onClick() }.padding(horizontal = 7.dp, vertical = 5.dp).testTag("botao-gramatica"),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        IconeRegras(tinta)
        Text(tx("aula_gramatica"), color = tinta, style = MaterialTheme.typography.labelMedium)
    }
}

/** Três linhas com uma seta: o desenho de "regras". Desenhado, porque glifo pode faltar no navegador. */
@Composable
private fun IconeRegras(cor: Color) {
    Canvas(Modifier.size(14.dp)) {
        val t = 2.dp.toPx()
        for (k in 0..2) {
            val y = size.height * (0.2f + 0.3f * k)
            drawLine(cor, Offset(0f, y), Offset(size.width * 0.25f, y), t, StrokeCap.Round)
            drawLine(cor, Offset(size.width * 0.45f, y), Offset(size.width, y), t, StrokeCap.Round)
        }
    }
}

/**
 * O painel desce do alto do palco: uma placa de metal da Oficina com a gramática.
 * Não-terminal em laranja, terminal em verde-água, a seta apagada. Um toque em qualquer lugar fecha.
 */
@Composable
fun BoxScope.PainelGramatica(linhas: List<String>?, aberto: Boolean, onFechar: () -> Unit) {
    AnimatedVisibility(
        aberto && linhas != null, Modifier.align(Alignment.TopCenter),
        enter = if (ThemeState.reduceMotion) fadeIn() else slideInVertically { -it } + fadeIn(),
        exit = if (ThemeState.reduceMotion) fadeOut() else slideOutVertically { -it } + fadeOut(),
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).clickable { onFechar() }, contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.padding(12.dp).widthIn(max = 420.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .placaDeMetal(Palette.drawer, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi).testTag("painel-gramatica"),
            ) {
                HazardStripe()
                Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tx("aula_gramatica"), color = Palette.on(Palette.drawer), style = MaterialTheme.typography.titleMedium)
                    for (l in linhas.orEmpty()) LinhaDeRegra(l)
                }
            }
        }
    }
}

@Composable
internal fun LinhaDeRegra(linha: String) {
    val mono = LocalFonts.current.mono
    val nt = Color(0xFFFFB07A)
    val t = Palette.teal
    val apagado = Palette.on(Palette.drawer).copy(alpha = 0.55f)
    val texto = buildAnnotatedString {
        for ((i, parte) in linha.split(" ").filter { it.isNotEmpty() }.withIndex()) {
            if (i > 0) append(" ")
            val estilo = when {
                parte == "->" || parte == "|" -> SpanStyle(color = apagado)
                parte.first().isUpperCase() && parte.none { it.isDigit() } && parte != "ID" -> SpanStyle(color = nt, fontWeight = FontWeight.Bold)
                else -> SpanStyle(color = t, fontWeight = FontWeight.Bold)
            }
            withStyle(estilo) { append(parte) }
        }
    }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(Palette.codeBg).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(texto, fontFamily = mono, fontSize = 17.sp)
    }
}
