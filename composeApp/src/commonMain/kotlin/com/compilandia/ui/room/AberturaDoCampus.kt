package com.compilandia.ui.room

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.room.art.Campus
import com.compilandia.ui.room.art.campus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Ao abrir o app, o campus aparece visto de cima; depois de um instante a câmera desce no prédio das salas e a
 * sala de aula aparece por baixo (decisão do dono no painel: sempre ao abrir, entra sozinho, tocar pula).
 */
object AberturaDoCampus {
    /** Desligada nos testes de captura, para as fotos mostrarem a sala. */
    var ligada = true
    const val PARADO_MS = 1600L
    const val ENTRANDO_MS = 1100
    const val ZOOM = 6f
}

/** O campus enche a largura (as pontas da laje podem sair um pouco). */
private const val LARGURA = 1.02f
private const val PULO_MS = 700

@Composable
fun AberturaDoCampus(onFim: () -> Unit) {
    if (ThemeState.reduceMotion) { LaunchedEffect(Unit) { onFim() }; return }
    val zoom = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var indo by remember { mutableStateOf(false) }
    fun entrar() {
        if (indo) return onFim()
        indo = true
        scope.launch {
            zoom.animateTo(1f, tween(AberturaDoCampus.ENTRANDO_MS, easing = FastOutSlowInEasing))
            onFim()
        }
    }
    LaunchedEffect(Unit) { delay(AberturaDoCampus.PARADO_MS); entrar() }
    val toque = remember { MutableInteractionSource() }
    BoxWithConstraints(Modifier.fundo(zoom.value).clickable(toque, null) { entrar() }, contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth * LARGURA, maxHeight)
        val alvo = Campus.iso(1f).p(Campus.ALVO)
        val e = zoom.value
        Box(Modifier.requiredSize(lado).camera(alvo, e)) {
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = tx("campus_descricao") }) {
                campus(Campus.iso(size.width))
            }
            if (!indo) Marcador(Modifier.offset(lado * alvo.x, lado * alvo.y))
        }
        Legendas()
    }
}

@Composable
private fun BoxScope.Legendas() {
    Text(
        tx("campus_titulo"), color = Palette.ink, fontFamily = LocalFonts.current.display,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
    )
    Text(
        tx("campus_toque"), color = Palette.inkSoft, style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
    )
}

/** O fundo some na segunda metade da descida, mostrando a sala que já está por baixo. */
@Composable
private fun Modifier.fundo(e: Float) = fillMaxSize()
    .graphicsLayer { alpha = 1f - ((e - 0.5f) / 0.5f).coerceIn(0f, 1f) }
    .background(Palette.paperDark)

/** A câmera desce no ponto [alvo] (fração do quadrado), trazendo ele para o meio enquanto aproxima. */
private fun Modifier.camera(alvo: Offset, e: Float) = graphicsLayer {
    val k = 1f + (AberturaDoCampus.ZOOM - 1f) * e * e
    scaleX = k
    scaleY = k
    transformOrigin = TransformOrigin(alvo.x, alvo.y)
    translationX = (0.5f - alvo.x) * size.width * e
    translationY = (0.5f - alvo.y) * size.height * e
}

/** A plaquinha que pula em cima do prédio das salas. */
@Composable
private fun Marcador(modifier: Modifier) {
    val pulo by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(PULO_MS), RepeatMode.Reverse))
    Box(modifier.size(0.dp).wrapContentSize(unbounded = true), contentAlignment = Alignment.BottomCenter) {
        Box(
            Modifier.offset(y = (-18 - 8 * pulo).dp).clip(RoundedCornerShape(6.dp)).background(Palette.hazard)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                tx("campus_sua_sala"), color = Palette.hazardDark, fontFamily = LocalFonts.current.display,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
