package com.compilandia.ui.room

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ProfessorFigure
import com.compilandia.ui.cast.art.Humor
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.NaAula
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

/*
 * O professor vive na frente do quadro: para, anda até outro ponto, de vez em quando fica contente (e diz isso),
 * e comemora com alegria quando é tocado. A rotina recomeça depois de cada comemoração.
 */
private class Professor(inicio: Float, val alegria: String, val contente: String) {
    val u = Animatable(inicio)
    var humor by mutableStateOf(Humor.PARADO)
    var paraEsquerda by mutableStateOf(false)
    var fala by mutableStateOf<String?>(null)

    suspend fun comemora() {
        humor = Humor.ALEGRIA
        fala = alegria
        delay(COMEMORA_MS)
        fala = null
    }

    suspend fun rotina() {
        while (true) {
            humor = Humor.PARADO
            delay(Random.nextLong(PARADO_MIN_MS, PARADO_MAX_MS))
            if (Random.nextFloat() < CHANCE_CONTENTE) fica() else anda(Random.nextFloat() * (FIM - INICIO) + INICIO)
        }
    }

    private suspend fun fica() {
        humor = Humor.CONTENTE
        fala = contente
        delay(CONTENTE_MS)
        fala = null
    }

    private suspend fun anda(alvo: Float) {
        paraEsquerda = alvo < u.value
        humor = Humor.ANDANDO
        u.animateTo(alvo, tween((abs(alvo - u.value) / VELOCIDADE * 1000).toInt(), easing = LinearEasing))
    }
}

@Composable
internal fun ProfessorNaSala(side: Dp) {
    val alegria = tx("sala_prof_alegria")
    val contente = tx("sala_prof_contente")
    val prof = remember { Professor(NaAula.PROFESSOR.u, alegria, contente) }
    var toques by remember { mutableIntStateOf(0) }
    LaunchedEffect(toques) {
        if (toques > 0) prof.comemora()
        if (!ThemeState.reduceMotion) prof.rotina()
    }
    val pe = Iso.aula(1f).p(prof.u.value, NaAula.PROFESSOR.v)
    val h = side * ALTURA
    val w = h * LARGURA_POR_ALTURA
    ProfessorFigure(
        Modifier.offset(side * pe.x - w / 2, side * pe.y - h * PE).size(w, h)
            .graphicsLayer { scaleX = if (prof.paraEsquerda && prof.humor == Humor.ANDANDO) -1f else 1f }
            .clickable(remember { MutableInteractionSource() }, null) { toques++; Juice.click() },
        humor = prof.humor,
    )
    prof.fala?.let { Balao(it, Modifier.offset(side * pe.x, side * pe.y - h * TOPO_DO_BALAO)) }
}

/** O balão da fala, centrado em cima da cabeça. */
@Composable
private fun Balao(texto: String, modifier: Modifier) {
    Box(modifier.size(0.dp).wrapContentSize(unbounded = true), contentAlignment = Alignment.BottomCenter) {
        Text(
            texto, color = Palette.tagInk, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.background(Palette.tag, RoundedCornerShape(10.dp))
                .border(1.5.dp, Palette.tagInk, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/** Altura do professor em fração do lado da sala; o pé fica a 300 de 320 no desenho. */
private const val ALTURA = 0.13f
private const val LARGURA_POR_ALTURA = 0.625f
private const val PE = 0.9375f
private const val TOPO_DO_BALAO = 1.0f

/** Por onde ele anda, ao longo do quadro (u), e com que pressa (unidades por segundo). */
private const val INICIO = 1.9f
private const val FIM = 6.95f
private const val VELOCIDADE = 0.9f

private const val PARADO_MIN_MS = 2200L
private const val PARADO_MAX_MS = 5000L
private const val CONTENTE_MS = 3400L
private const val COMEMORA_MS = 2500L
private const val CHANCE_CONTENTE = 0.35f
