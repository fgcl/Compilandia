package com.compilandia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.compilandia.content.Glossario
import com.compilandia.content.Termo
import com.compilandia.platform.nowMs
import com.compilandia.textos.tx

/** O balão do glossário (A8): qualquer texto com termo sublinhado abre aqui. */
object GlossarioUi {
    var aberto by mutableStateOf<Termo?>(null)
    /** Os testes desligam para conferir texto sem link. */
    var ligado = true
}

/** Acrescenta os links do glossário a um texto já montado. Só marca termo fora de trecho de código. */
fun AnnotatedString.Builder.marcarGlossario(texto: String, codigo: List<IntRange> = emptyList()) {
    if (!GlossarioUi.ligado) return
    val estilo = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline))
    for ((ini, fim, termo) in Glossario.achar(texto)) {
        if (codigo.any { ini < it.last + 1 && fim > it.first }) continue
        addLink(LinkAnnotation.Clickable("gl:${termo.id}", estilo) { GlossarioUi.aberto = termo }, ini, fim)
    }
}

/** O balão: o termo, a explicação curta e o caminho para a aula do assunto. */
@Composable
fun GlossarioBalao(app: AppState) {
    val t = GlossarioUi.aberto ?: return
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))
            .clickable(remember { MutableInteractionSource() }, null) { GlossarioUi.aberto = null },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(24.dp).widthIn(max = 420.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Palette.paper)
                .border(2.dp, Palette.hazard, RoundedCornerShape(14.dp)).clickable(remember { MutableInteractionSource() }, null) {}.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(t.nome, style = MaterialTheme.typography.headlineSmall, color = Palette.ink)
            Text(t.definicao, style = MaterialTheme.typography.bodyLarge, color = Palette.ink)
            Text(t.skill.title, style = MaterialTheme.typography.labelMedium, color = Palette.inkSoft)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BigButton(tx("gl_ver_aula"), Modifier.weight(1f), color = Palette.drawer, small = true) {
                    GlossarioUi.aberto = null
                    app.go(Screen.Trilha(t.skill))
                }
                BigButton(tx("gl_fechar"), Modifier.weight(1f), small = true) { GlossarioUi.aberto = null }
            }
        }
    }
    // Consultar um termo é sinal de que ele escapou da memória (vai para a revisão por ideia, R3).
    androidx.compose.runtime.LaunchedEffect(t.id) { app.learner.consultouTermo(t.skill, t.id, nowMs()); app.commit() }
}
