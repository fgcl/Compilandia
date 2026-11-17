package com.compilandia.ui.cast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import com.compilandia.ui.cast.art.Humor
import com.compilandia.ui.cast.art.poseDoProfessor
import com.compilandia.ui.cast.art.professorRecortado

/*
 * O professor careca da sala de aula, em papel recortado (o estilo escolhido pelo dono). Respira e pisca parado,
 * anda, comemora com alegria e fica contente: o [humor] escolhe a animação. O desenho mora num quadro de 200 x 320.
 */
@Composable
fun ProfessorFigure(modifier: Modifier = Modifier, humor: Humor = Humor.PARADO, animate: Boolean = true) {
    val clock = rememberRigClock(animate) { false }
    Canvas(modifier.aspectRatio(LARGURA / ALTURA)) {
        val u = size.width / LARGURA
        val t = clock.time.floatValue
        val pose = poseDoProfessor(humor, t, piscando = clock.blink.floatValue < 0.5f)
        scale(u, u, Offset.Zero) { professorRecortado(pose, t) }
    }
}

private const val LARGURA = 200f
private const val ALTURA = 320f
