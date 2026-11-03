package com.compilandia.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.compilandia.resources.Res
import com.compilandia.resources.char_robot_red_body
import com.compilandia.resources.bg_castles
import com.compilandia.resources.bg_color_desert
import com.compilandia.resources.bg_color_fall
import com.compilandia.resources.bg_color_forest
import com.compilandia.resources.bg_color_grass
import com.compilandia.resources.bg_desert
import com.compilandia.resources.bg_forest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

// ============================================================ Personagens com arte (Kenney, CC0)

/** Troca o emoji do personagem por arte de verdade quando existe. */
fun characterArt(emoji: String): DrawableResource? = when (emoji) {
    "🗿" -> Res.drawable.char_robot_red_body
    else -> null
}

// ============================================================ Arrastar e soltar

/** Estado compartilhado entre itens arrastáveis e zonas de soltura. */
class DragDropState {
    val zones = mutableStateMapOf<String, Rect>()
    var dragging by mutableStateOf<Any?>(null)
    var pointer by mutableStateOf(Offset.Zero)
    fun zoneAt(p: Offset): String? = zones.entries.firstOrNull { it.value.contains(p) }?.key
    val hovered: String? get() = if (dragging != null) zoneAt(pointer) else null
}

@Composable
fun rememberDragDrop() = remember { DragDropState() }

/** Zona que recebe itens. [content] recebe true quando há um item pairando em cima. */
@Composable
fun DropZone(state: DragDropState, id: String, modifier: Modifier = Modifier, content: @Composable BoxScope.(hovered: Boolean) -> Unit) {
    Box(
        modifier.onGloballyPositioned { state.zones[id] = it.boundsInRoot() },
        contentAlignment = Alignment.Center,
    ) { content(state.hovered == id) }
}

/**
 * Item que segue o dedo. Ao soltar sobre uma zona chama [onDrop] com o id da zona;
 * se soltar fora, volta para o lugar com uma mola. Toque simples chama [onTap] (acessibilidade).
 */
@Composable
fun <T> Draggable(
    state: DragDropState,
    payload: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTap: (() -> Unit)? = null,
    onDrop: (zone: String) -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ox = remember { Animatable(0f) }
    val oy = remember { Animatable(0f) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var start by remember { mutableStateOf(Offset.Zero) }
    val dragging = state.dragging === payload
    Box(
        modifier
            .zIndex(if (dragging) 10f else 0f)
            .offset { IntOffset(ox.value.roundToInt(), oy.value.roundToInt()) }
            .scale(if (dragging) 1.08f else 1f)
            .then(if (dragging) Modifier.shadow(10.dp, RoundedCornerShape(12.dp)) else Modifier)
            .onGloballyPositioned { if (!dragging) origin = it.positionInRoot() - Offset(ox.value, oy.value) }
            .pointerInput(payload, enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { p -> start = origin + p; state.dragging = payload; state.pointer = start; Juice.pick() },
                    onDrag = { change, d ->
                        change.consume()
                        scope.launch { ox.snapTo(ox.value + d.x); oy.snapTo(oy.value + d.y) }
                        state.pointer = start + Offset(ox.value + d.x, oy.value + d.y)
                    },
                    onDragEnd = {
                        val zone = state.zoneAt(state.pointer)
                        state.dragging = null
                        scope.launch { ox.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                        scope.launch { oy.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                        if (zone != null) { Juice.drop(); onDrop(zone) }
                    },
                    onDragCancel = {
                        state.dragging = null
                        scope.launch { ox.animateTo(0f) }; scope.launch { oy.animateTo(0f) }
                    },
                )
            }
            .then(if (onTap != null && enabled) Modifier.pointerInput(payload) {
                detectTapGestures { onTap() }
            } else Modifier),
    ) { content() }
}

/** Ficha arrastável com visual de "peça física". */
@Composable
fun Tile(text: String, color: androidx.compose.ui.graphics.Color = Palette.white, mono: Boolean = true, big: Boolean = false) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp)).background(color).border(2.dp, Palette.ink.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = if (big) 14.dp else 10.dp, vertical = if (big) 12.dp else 8.dp),
    ) {
        Text(text, fontFamily = if (mono) LocalFonts.current.mono else LocalFonts.current.body, fontWeight = FontWeight.Bold,
            fontSize = if (big) 17.sp else 15.sp, color = if (color == Palette.white) Palette.ink else Palette.inkOnLight)
    }
}

// ============================================================ Carta deslizável

/**
 * Carta que você ARRASTA para a esquerda ou para a direita (estilo app de namoro).
 * Mais rápido e físico do que tocar num botão — ótimo para decisões sim/não.
 */
@Composable
fun SwipeCard(
    key: Any,
    leftLabel: String,
    rightLabel: String,
    modifier: Modifier = Modifier,
    onSwipe: (right: Boolean) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val x = remember(key) { Animatable(0f) }
    var width by remember { mutableStateOf(1f) }
    Box(
        modifier.fillMaxWidth().onGloballyPositioned { width = it.size.width.toFloat() }
            .offset { IntOffset(x.value.roundToInt(), (abs(x.value) * 0.08f).roundToInt()) }
            .rotate(x.value / 30f)
            .pointerInput(key) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { ch, d -> ch.consume(); scope.launch { x.snapTo(x.value + d) } },
                    onDragEnd = {
                        val v = x.value
                        if (abs(v) > width * 0.28f) {
                            scope.launch { x.animateTo(if (v > 0) width * 1.5f else -width * 1.5f, tween(180)); onSwipe(v > 0) }
                        } else scope.launch { x.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
        val a = (x.value / (width * 0.28f)).coerceIn(-1f, 1f)
        if (a > 0.05f) Stamp(rightLabel, Palette.green, a, Modifier.align(Alignment.TopStart))
        if (a < -0.05f) Stamp(leftLabel, Palette.red, -a, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun Stamp(text: String, color: androidx.compose.ui.graphics.Color, alpha: Float, modifier: Modifier) {
    Box(
        modifier.padding(14.dp).rotate(-12f).border(4.dp, color.copy(alpha = alpha), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(text, color = color.copy(alpha = alpha), fontWeight = FontWeight.Bold, fontSize = 22.sp) }
}

@Composable
fun ArtImage(res: DrawableResource, size: Int, modifier: Modifier = Modifier) {
    Image(painterResource(res), null, modifier.size(size.dp))
}

/** Cenário (arte Kenney) de cada região do mapa. */
fun regionArt(r: com.compilandia.learning.Region): DrawableResource = when (r) {
    com.compilandia.learning.Region.PORTO -> Res.drawable.bg_color_grass
    com.compilandia.learning.Region.FLORESTA_LEXICA -> Res.drawable.bg_color_forest
    com.compilandia.learning.Region.VALE_GRAMATICAS -> Res.drawable.bg_color_fall
    com.compilandia.learning.Region.PONTE_TRANSFORMACAO -> Res.drawable.bg_forest
    com.compilandia.learning.Region.TEMPLO_TOPDOWN -> Res.drawable.bg_color_desert
    com.compilandia.learning.Region.FORTALEZA_BOTTOMUP -> Res.drawable.bg_castles
    com.compilandia.learning.Region.OFICINA_BISON -> Res.drawable.bg_desert
    com.compilandia.learning.Region.CIDADE_SEMANTICA -> Res.drawable.bg_color_grass
    com.compilandia.learning.Region.FORJA_CODIGO -> Res.drawable.bg_castles
}

/** Banner ilustrado (cenário + título por cima). */
@Composable
fun SceneBanner(art: DrawableResource, height: Int = 120, content: @Composable BoxScope.() -> Unit = {}) {
    Box(Modifier.fillMaxWidth().height(height.dp)) {
        Image(painterResource(art), null, Modifier.fillMaxWidth().height(height.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop, colorFilter = if (ThemeState.mode == ThemeMode.NOITE_AMBAR) amberFilter else null)
        content()
    }
}

/** Tira o azul da arte dos cenários no tema Noite âmbar. */
private val amberFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(floatArrayOf(
    0.55f, 0.45f, 0.10f, 0f, 0f,
    0.40f, 0.36f, 0.08f, 0f, 0f,
    0.14f, 0.12f, 0.03f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)))
