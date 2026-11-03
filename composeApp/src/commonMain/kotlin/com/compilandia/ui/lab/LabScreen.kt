package com.compilandia.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.Beat
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.tutor.Sim
import com.compilandia.ui.AppState
import com.compilandia.ui.Header
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.pegboard
import com.compilandia.ui.visuals.VisualBeat
import kotlinx.coroutines.delay

const val LAB_INPUT_TAG = "lab-entrada"
const val LAB_SEND_TAG = "lab-enviar"

/** O Laboratório: uma conversa com o Byte. Quando ele roda algo, o palco ocupa a tela. */
@Composable
fun LabScreen(app: AppState) {
    var aberto by remember { mutableStateOf<Sim?>(null) }
    val scope = rememberCoroutineScope()
    val conversa = remember { LabConversa(app, scope) { aberto = it } }
    val sim = aberto
    if (sim != null) {
        Column(Modifier.fillMaxSize()) {
            Header(sim.titulo, null, Palette.purple) {
                Box(Modifier.clip(RoundedCornerShape(8.dp)).clickable { aberto = null }.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text("Conversa", color = Palette.on(Palette.purple), style = MaterialTheme.typography.labelMedium)
                }
            }
            SimPlayer(sim, Modifier.weight(1f))
        }
        return
    }
    Column(Modifier.fillMaxSize()) {
        Header("Laboratório", null, Palette.purple)
        val lista = rememberLazyListState()
        LaunchedEffect(conversa.msgs.size) { if (conversa.msgs.isNotEmpty()) lista.animateScrollToItem(conversa.msgs.lastIndex) }
        val itens = conversa.msgs.toList()
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)),
            state = lista,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Cópia fixa: a conversa muda quando o diagnóstico volta, e a lista não pode encolher no meio da medição.
            itemsIndexed(itens) { _, m ->
                Mensagem(m, m === conversa.ultimaFala, abrir = { aberto = it }, app)
            }
            // No começo a conversa é curta: o Byte ocupa a bancada enquanto espera o exercício.
            if (conversa.msgs.size <= 2) item {
                Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    ByteFigure(Modifier.height(240.dp), Mood.HAPPY, lookY = 0.6f)
                }
            }
        }
        Composer(conversa)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Mensagem(m: Msg, ultima: Boolean, abrir: (Sim) -> Unit, app: AppState) {
    when (m) {
        is Msg.Eu -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(Modifier.widthIn(max = 310.dp).clip(RoundedCornerShape(14.dp, 14.dp, 3.dp, 14.dp)).background(Palette.codeBg).padding(horizontal = 13.dp, vertical = 10.dp)) {
                Text(
                    m.texto, color = Palette.onAccent,
                    style = if (m.mono) TextStyle(fontFamily = LocalFonts.current.mono, fontSize = 15.sp, lineHeight = 22.sp) else MaterialTheme.typography.bodyLarge,
                )
            }
        }
        is Msg.Fala -> Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ByteFigure(Modifier.width(38.dp).padding(top = 2.dp), m.humor, animate = ultima)
            Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 3.dp)).background(Palette.white).padding(horizontal = 13.dp, vertical = 10.dp)) {
                if (ultima) FalaDigitada(m.texto) else Text(m.texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is Msg.Palco -> MiniPalco(m.sim.passos.map { it.shot }) { abrir(m.sim) }
        is Msg.Clipe -> MiniPalco(m.ep.passos.map { it.shot }) { app.go(com.compilandia.ui.Screen.Episode(m.ep.skill, id = m.ep.id)) }
        is Msg.Acoes -> FlowRow(Modifier.padding(start = 44.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            for (a in m.acoes) AcaoChip(a)
        }
        is Msg.Ferramenta -> Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.paper).padding(10.dp)) {
            VisualBeat(Beat.Visual(m.kind, m.grammar, m.input))
        }
        Msg.Pensando -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ByteFigure(Modifier.width(38.dp), Mood.THINK)
            Text("lendo...", style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
        }
    }
}

@Composable
private fun AcaoChip(a: Acao) {
    val bg = when { a.travada -> Palette.paper; a.destaque -> Palette.hazard; else -> Palette.white }
    val fg = when { a.travada -> Palette.inkSoft; a.destaque -> Palette.hazardDark; else -> Palette.ink }
    Row(
        Modifier.clip(RoundedCornerShape(9.dp)).background(bg).border(2.dp, if (a.destaque) Palette.hazard else Palette.drawerHi, RoundedCornerShape(9.dp))
            .clickable { a.faz() }.padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (a.travada) Cadeado(fg)
        Text(a.rotulo, color = fg, style = MaterialTheme.typography.labelMedium, maxLines = 2)
    }
}

@Composable
fun Cadeado(c: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier.size(14.dp, 16.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(c, Offset(0f, h * 0.42f), androidx.compose.ui.geometry.Size(w, h * 0.58f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
        drawArc(c, 180f, 180f, false, Offset(w * 0.18f, 0f), androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.7f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
    }
}

/** Palco pequeno dentro da conversa. Fica rodando em volta, e um toque abre em tela cheia. */
@Composable
private fun MiniPalco(shots: List<com.compilandia.content.Shot>, onOpen: () -> Unit) {
    var k by remember(shots) { mutableIntStateOf(0) }
    LaunchedEffect(shots) {
        while (shots.size > 1) {
            delay(if (k == shots.lastIndex) 2600 else 1700)
            k = (k + 1) % shots.size
        }
    }
    Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).clickable { onOpen() }) {
        MotionStage(shots[k.coerceAtMost(shots.lastIndex)], Modifier.fillMaxSize().padding(6.dp))
        Row(
            Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(RoundedCornerShape(6.dp)).background(Palette.hazard).padding(horizontal = 8.dp, vertical = 3.dp),
        ) { Text("Abrir", color = Palette.hazardDark, style = MaterialTheme.typography.labelMedium) }
    }
}

/** Teclas que o teclado do celular esconde ou nem tem. As três primeiras são as mais usadas em gramática. */
private val TECLAS = listOf("->", "|", "ε", "'", "(", ")", "*", "+", "?", "$", ";", "=", "{", "}", "[", "]", "%%", "↵")

/** Escreve no cursor e troca o que estiver selecionado. A seta ganha espaço porque os símbolos da regra são separados por espaço. */
fun insereTecla(v: TextFieldValue, tecla: String): TextFieldValue {
    val antes = v.text.substring(0, v.selection.min)
    val depois = v.text.substring(v.selection.max)
    val novo = when (tecla) {
        "->" -> (if (antes.isEmpty() || antes.last().isWhitespace()) "" else " ") + "-> "
        "↵" -> "\n"
        else -> tecla
    }
    return TextFieldValue(antes + novo + depois, TextRange(antes.length + novo.length))
}

@Composable
private fun TecladoRapido(onTecla: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.paper).horizontalScroll(rememberScrollState()).padding(start = 10.dp, end = 10.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TECLAS.forEachIndexed { i, t ->
            val forte = i < 3
            Box(
                Modifier.defaultMinSize(minWidth = 44.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).background(if (forte) Palette.hazard else Palette.white)
                    .clickable { onTecla(t) }.testTag("tecla-$t").padding(horizontal = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(t, color = if (forte) Palette.hazardDark else Palette.ink, fontFamily = LocalFonts.current.mono, fontSize = 17.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun Composer(c: LabConversa) {
    var texto by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(c.rascunho) {
        if (c.rascunho.isNotEmpty()) {
            texto = TextFieldValue(c.rascunho, TextRange(c.rascunho.length))
            c.rascunho = ""
        }
    }
    val enviar = {
        if (texto.text.isNotBlank()) {
            c.entra(texto.text)
            texto = TextFieldValue("")
        }
    }
    TecladoRapido { t ->
        texto = insereTecla(texto, t)
        Sound.play(Sfx.TICK)
    }
    Row(Modifier.fillMaxWidth().background(Palette.paper).padding(10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(horizontal = 12.dp, vertical = 12.dp)) {
            if (texto.text.isEmpty()) Text("Cole um exercício ou pergunte", color = Palette.onAccent.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
            BasicTextField(
                texto, { texto = it }, Modifier.fillMaxWidth().testTag(LAB_INPUT_TAG), maxLines = 7,
                textStyle = TextStyle(fontFamily = LocalFonts.current.mono, fontSize = 15.sp, lineHeight = 21.sp, color = Palette.onAccent),
                cursorBrush = SolidColor(Palette.hazard),
            )
        }
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Palette.hazard).clickable { enviar() }.testTag(LAB_SEND_TAG), contentAlignment = Alignment.Center) {
            val cor = Palette.hazardDark
            androidx.compose.foundation.Canvas(Modifier.size(20.dp)) {
                drawPath(Path().apply { moveTo(0f, 0f); lineTo(size.width, size.height / 2); lineTo(0f, size.height); close() }, cor)
            }
        }
    }
}
