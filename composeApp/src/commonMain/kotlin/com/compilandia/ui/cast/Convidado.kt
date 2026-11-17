package com.compilandia.ui.cast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.content.Speaker

/** Quem fala do canto da faixa da aula: a Profª e o Bug. O Byte encolhe e olha para eles. */
val CONVIDADOS = setOf(Speaker.PROF, Speaker.BUG)

/** O convidado sobe no canto enquanto fala e desce quando a fala é de outro. */
@Composable
fun ConvidadoPop(who: Speaker, largura: Dp, modifier: Modifier) {
    val entra = slideInVertically { it } + fadeIn()
    val sai = slideOutVertically { it } + fadeOut()
    Box(modifier) {
        AnimatedVisibility(who == Speaker.PROF, enter = entra, exit = sai) {
            ProfFigure(Modifier.size(largura), lookX = -0.5f)
        }
        AnimatedVisibility(who == Speaker.BUG, enter = entra, exit = sai) { BugFigure(Modifier.width(largura)) }
    }
}

/** Com o painel ao lado do palco, o convidado fica no alto dele, à direita, na altura do Byte. */
fun Modifier.noAltoDoPainel(topo: Dp): Modifier = offset(y = topo).padding(end = 10.dp, top = 4.dp)
