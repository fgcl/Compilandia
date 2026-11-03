package com.compilandia.ui.album

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.content.Album
import com.compilandia.content.Especie
import com.compilandia.learning.GameId
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.Screen
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.BugFigure
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.pegboard

private val PAPEL = Color(0xFFE9E0C8)
private val SOMBRA = Color(0xFF8D8875)
private val TINTA = Color(0xFF1F2628)
private val CASCOS = listOf(
    Color(0xFFC8432A) to Color(0xFF9A2E1B), Color(0xFF2E7D4F) to Color(0xFF1C5535), Color(0xFFD9920F) to Color(0xFFA26C08),
    Color(0xFF1F6F8F) to Color(0xFF134F68), Color(0xFFA83A63) to Color(0xFF7A2846), Color(0xFF5F7330) to Color(0xFF435322),
    Color(0xFF9A5A1E) to Color(0xFF6E3F12), Color(0xFF4E6169) to Color(0xFF364449),
)

private fun casco(e: Especie) = CASCOS[Album.especies.indexOf(e) % CASCOS.size]

/** Sugere o próximo Bug: o do assunto em que a pessoa anda errando, ou o primeiro que falta. */
private fun doDia(app: AppState): Especie? {
    val l = app.learner
    val faltam = Album.especies.filter { it.id !in app.ls.bugsPegos }
    return faltam.minByOrNull { l.mastery(it.skill) - if (l.skill(it.skill).attempts > 0) 0.3 else 0.0 } ?: Album.especies.randomOrNull()
}

@Composable
fun AlbumScreen(app: AppState) {
    val pegos = app.ls.bugsPegos
    var escolhido by remember { mutableStateOf(Album.especies.lastOrNull { it.id in pegos }) }
    Column(Modifier.fillMaxSize()) {
        Header("Álbum de Bugs", "${pegos.size} de ${Album.especies.size}", Palette.drawer)
        LazyVerticalGrid(
            GridCells.Fixed(4),
            Modifier.weight(1f).fillMaxWidth().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(4) }) {
                val e = escolhido
                if (e != null) Destaque(e) else Text("Cada erro comum de prova é um Bug. Pegue o primeiro para abrir o álbum.", style = MaterialTheme.typography.bodyLarge)
            }
            items(Album.especies) { e ->
                val tem = e.id in pegos
                Figurinha(e, tem) { if (tem) escolhido = e else app.go(Screen.CacaBug(e.id)) }
            }
        }
        Box(Modifier.fillMaxWidth().background(Palette.paper).padding(12.dp)) {
            BigButton("Caçar o Bug de hoje", Modifier.fillMaxWidth()) { doDia(app)?.let { app.go(Screen.CacaBug(it.id)) } }
        }
    }
}

@Composable
private fun Figurinha(e: Especie, tem: Boolean, onClick: () -> Unit) {
    val i = Album.especies.indexOf(e)
    val (c, lo) = casco(e)
    Box(
        Modifier.fillMaxWidth().aspectRatio(0.9f).rotate(if (tem) ((i * 37) % 7 - 3).toFloat() else 0f).clip(RoundedCornerShape(6.dp))
            .background(if (tem) PAPEL else Palette.white).clickable { onClick() }.padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (tem) BugFigure(Modifier.fillMaxWidth(), casco = c, cascoEscuro = lo)
        else BugFigure(Modifier.fillMaxWidth().alpha(0.32f), casco = Color.Black, cascoEscuro = Color.Black)
    }
}

/** Figurinha grande, com o brilho passando. */
@Composable
private fun Destaque(e: Especie) {
    val (c, lo) = casco(e)
    val brilho by rememberInfiniteTransition().animateFloat(-0.3f, 1.3f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart))
    Box(Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(10.dp)).background(PAPEL)) {
        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BugFigure(Modifier.width(92.dp), casco = c, cascoEscuro = lo)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(e.nome, color = TINTA, style = MaterialTheme.typography.titleMedium)
                Text(e.verdade, color = TINTA, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (!ThemeState.reduceMotion) Canvas(Modifier.matchParentSize()) {
            val x = size.width * brilho
            drawRect(
                Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent), x - 40.dp.toPx(), x + 40.dp.toPx()),
                Offset(x - 40.dp.toPx(), 0f), Size(80.dp.toPx(), size.height),
            )
        }
    }
    @Suppress("UNUSED_EXPRESSION") SOMBRA
}

const val CARTA_TAG = "carta"

/**
 * A caça: o Bug solta a mentira dele, e a pessoa julga as frases.
 * Direita é verdade, esquerda é conversa do Bug. Acertou todas, a figurinha cola.
 */
@Composable
fun CacaBugScreen(app: AppState, id: String) {
    val e = remember(id) { Album.of(id) } ?: return
    val testes = remember(e) { e.testes.shuffled() }
    var k by remember { mutableIntStateOf(0) }
    var erros by remember { mutableIntStateOf(0) }
    var hurt by remember { mutableIntStateOf(0) }
    var resposta by remember(k) { mutableStateOf<Boolean?>(null) }
    var fim by remember { mutableStateOf(false) }
    var pago by remember { mutableStateOf(false) }
    val (c, lo) = casco(e)
    val t = testes.getOrNull(k)

    androidx.compose.runtime.DisposableEffect(Unit) {
        Juice.combo = 0
        Juice.arenas++
        Juice.palcos++
        onDispose {
            Juice.arenas--
            Juice.palcos--
            Juice.combo = 0
        }
    }

    fun julga(diz: Boolean) {
        if (resposta != null || t == null) return
        resposta = diz
        if (diz == t.verdade) { Juice.correct(); hurt++ } else { Juice.wrong(); erros++ }
    }
    fun proxima() {
        if (k + 1 < testes.size) k++ else {
            fim = true
            val venceu = erros == 0
            app.learner.recordAttempt(e.skill, GameId.FLASHCARDS, 2, (testes.size - erros).toDouble() / testes.size, null, 20_000, 0, nowMs())
            if (venceu) { pago = app.learner.pegaBug(e.id, nowMs()); Juice.victory() }
            app.commit()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Header(e.nome, e.skill.title, Palette.region(e.skill.region))
        Box(Modifier.fillMaxWidth().height(190.dp).background(Palette.hazardDark).pegboard(Palette.drawerHi.copy(alpha = 0.12f))) {
            ByteFigure(Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 2.dp).height(120.dp), if (fim && erros == 0) Mood.HAPPY else if (resposta != null && resposta != t?.verdade) Mood.SAD else Mood.THINK, lookX = 0.8f, hop = hurt)
            BugFigure(Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 6.dp).width(138.dp), hurt = hurt, down = if (fim && erros == 0) 1f else 0f, casco = c, cascoEscuro = lo)
            if (!fim) Box(Modifier.align(Alignment.TopCenter).padding(top = 10.dp, start = 14.dp, end = 14.dp).clip(RoundedCornerShape(10.dp)).background(Palette.tag).padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text(e.mentira, color = Palette.tagInk, style = MaterialTheme.typography.bodyMedium)
            }
        }
        HazardStripe()
        Column(
            Modifier.weight(1f).fillMaxWidth().background(Palette.paperDark).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (fim) {
                val venceu = erros == 0
                if (venceu) Colada(e)
                Text(if (venceu) "Pegou o ${e.nome}" else "Escapou por pouco", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(e.verdade, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                if (pago) Text("+15 engrenagens", style = MaterialTheme.typography.titleMedium, color = Palette.yellow)
                Spacer(Modifier.weight(1f))
                if (!venceu) BigButton("Tentar de novo", Modifier.fillMaxWidth()) { app.replace(Screen.CacaBug(e.id)) }
                BigButton("Voltar ao álbum", Modifier.fillMaxWidth(), color = Palette.drawer) { app.replace(Screen.Album) }
            } else if (t != null) {
                Text("Verdade ou conversa do Bug?", style = MaterialTheme.typography.titleSmall, color = Palette.inkSoft)
                Carta(t.texto, k, resposta == null) { julga(it) }
                val r = resposta
                if (r != null) {
                    Text(if (r == t.verdade) "Boa." else "O Bug te pegou.", style = MaterialTheme.typography.titleMedium, color = if (r == t.verdade) Palette.green else Palette.red)
                    Text(t.porque, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                    BigButton("Continuar", Modifier.fillMaxWidth()) { proxima() }
                } else {
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BigButton("É do Bug", Modifier.weight(1f), color = Palette.action) { julga(false) }
                        BigButton("Verdade", Modifier.weight(1f), color = Palette.teal, textColor = Palette.hazardDark) { julga(true) }
                    }
                }
            }
        }
    }
}

/** A carta anda com o dedo. Solta longe do centro, ela conta como resposta. */
@Composable
private fun Carta(texto: String, chave: Int, ativa: Boolean, onJulga: (Boolean) -> Unit) {
    var dx by remember(chave) { mutableFloatStateOf(0f) }
    val volta = remember(chave) { Animatable(0f) }
    LaunchedEffect(ativa) { if (!ativa) { volta.snapTo(dx); volta.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)); dx = 0f } }
    val x = if (ativa) dx else volta.value
    Box(
        Modifier.fillMaxWidth().offset(x = (x / 2.5f).dp).rotate(x / 42f).clip(RoundedCornerShape(14.dp)).background(Palette.white)
            .testTag(CARTA_TAG)
            .pointerInput(chave, ativa) {
                if (ativa) detectDragGestures(
                    onDragEnd = {
                        if (dx > 200f) onJulga(true) else if (dx < -200f) onJulga(false) else dx = 0f
                    },
                    onDrag = { c, d -> c.consume(); dx += d.x },
                )
            }.padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Colada(e: Especie) {
    val s = remember { Animatable(2.4f) }
    LaunchedEffect(Unit) { s.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
    val (c, lo) = casco(e)
    Box(Modifier.width(104.dp).scale(s.value).rotate(-3f).clip(RoundedCornerShape(7.dp)).background(PAPEL).padding(8.dp)) {
        BugFigure(Modifier.fillMaxWidth(), down = 0f, casco = c, cascoEscuro = lo)
    }
}
