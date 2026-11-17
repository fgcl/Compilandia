package com.compilandia.ui.room

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.compilandia.ui.Juice
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.moveisArte
import com.compilandia.ui.room.art.salaVazia

/** A oficina sem os móveis da loja, desenhada por código (ui/room/art). */
@Composable
internal fun SalaDesenhada(descricao: String, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().semantics { contentDescription = descricao }) { salaVazia(Iso.sala(size.width)) }
}

/** Um móvel comprado, no lugar dele na sala. Ao chegar ([popIn]), ele cresce a partir do próprio centro. */
@Composable
internal fun MovelNaSala(id: String, popIn: Boolean, onDone: () -> Unit) {
    val arte = moveisArte[id] ?: return
    val s = remember { Animatable(if (popIn) 0f else 1f) }
    LaunchedEffect(popIn) {
        if (popIn) {
            Juice.levelUp()
            s.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            onDone()
        }
    }
    // O centro do móvel em fração da sala: é dali que ele cresce.
    val c = Iso.sala(1f).p(arte.centro)
    val m = Modifier.fillMaxSize().graphicsLayer {
        scaleX = s.value
        scaleY = s.value
        transformOrigin = TransformOrigin(c.x, c.y)
    }
    Canvas(m) { arte.desenha(this, Iso.sala(size.width)) }
}

/** Um móvel sozinho, centrado e do tamanho do quadrado: o ícone da loja e da oferta. */
@Composable
internal fun MovelIcone(id: String, modifier: Modifier = Modifier) {
    val arte = moveisArte[id] ?: return
    Canvas(modifier) {
        val lado = size.minDimension * OCUPA / (LARGURA_POR_UNIDADE * arte.tamanho)
        arte.desenha(this, Iso.centrada(arte.centro, Offset(size.width / 2, size.height / 2), lado))
    }
}

/**
 * O tamanho de cada móvel é a largura dele em unidades; com folga, o ícone enche o quadrado.
 * Uma unidade do mundo mede perto de 2·DX do lado da sala na horizontal.
 */
private const val OCUPA = 1.3f
private const val LARGURA_POR_UNIDADE = 2 * Iso.DX
