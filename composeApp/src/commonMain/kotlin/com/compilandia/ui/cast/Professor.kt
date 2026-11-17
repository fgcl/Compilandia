package com.compilandia.ui.cast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.compilandia.ui.cast.art.SOMBRA_DO_PROFESSOR
import com.compilandia.ui.cast.art.cabeca
import com.compilandia.ui.cast.art.pernas
import com.compilandia.ui.cast.art.tronco
import kotlin.math.sin

/*
 * O professor careca da sala de aula (pedido do dono, no lugar do Byte na tela inicial). Desenho com volume:
 * cada parte tem luz vinda da esquerda, como a sala em diorama. Respira, pisca e pula quando tocado.
 * O desenho mora num quadro de 200 x 400.
 */
@Composable
fun ProfessorFigure(modifier: Modifier = Modifier, hop: Int = 0, animate: Boolean = true) {
    val clock = rememberRigClock(animate) { false }
    val pulo = remember { Animatable(0f) }
    LaunchedEffect(hop) {
        if (hop > 0) {
            pulo.snapTo(1f)
            pulo.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
    }
    Canvas(modifier.aspectRatio(0.5f)) {
        val u = size.width / 200f
        val t = clock.time.floatValue
        val respira = sin(t * 1.8f) * 1.5f
        scale(u, u, Offset.Zero) {
            drawOval(SOMBRA_DO_PROFESSOR, Offset(46f, 372f), Size(108f, 22f))
            translate(0f, -pulo.value * 26f) {
                pernas()
                translate(0f, respira * 0.4f) { tronco() }
                translate(0f, respira) { cabeca(clock.blink.floatValue) }
            }
        }
    }
}

