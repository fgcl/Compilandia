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
import com.compilandia.ui.room.art.frenteDaSala
import com.compilandia.ui.room.art.salaDeAula

/** A sala de aula sem as peças da loja, desenhada por código (ui/room/art). */
@Composable
internal fun SalaDesenhada(descricao: String, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().semantics { contentDescription = descricao }) { salaDeAula(Iso.aula(size.width)) }
}

/**
 * As peças compradas e a frente da sala, na ordem de pintura: as peças que ficam atrás da mesa do professor,
 * a mesa com as cadeiras diante dela, e as outras peças. [tem] diz se a peça foi comprada; [chegando] é a que acabou
 * de chegar e entra pulando.
 */
@Composable
internal fun PecasDaSala(tem: (String) -> Boolean, chegando: String?, onChegou: () -> Unit) {
    val (atras, frente) = moveisArte.entries.partition { it.value.atras }
    for ((id, _) in atras) if (tem(id)) MovelNaSala(id, popIn = chegando == id, onChegou)
    Canvas(Modifier.fillMaxSize()) { frenteDaSala(Iso.aula(size.width)) }
    for ((id, _) in frente) if (tem(id)) MovelNaSala(id, popIn = chegando == id, onChegou)
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
    val c = Iso.aula(1f).p(arte.centro)
    val m = Modifier.fillMaxSize().graphicsLayer {
        scaleX = s.value
        scaleY = s.value
        transformOrigin = TransformOrigin(c.x, c.y)
    }
    Canvas(m) { arte.desenha(this, Iso.aula(size.width)) }
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
