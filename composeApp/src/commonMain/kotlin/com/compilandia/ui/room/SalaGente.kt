package com.compilandia.ui.room

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.compilandia.content.Falas
import com.compilandia.ui.AppState
import com.compilandia.ui.Juice
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.NaAula
import com.compilandia.ui.voice.Narrator
import kotlinx.coroutines.launch

/*
 * Quem está na sala: o professor careca, de pé entre o quadro e a tela, e o Byte pequeno na mesa, ao lado do
 * computador (o dono decidiu: o Byte segue narrando as aulas, mas na sala quem fica de pé é o professor).
 */

/** Largura do Byte, em fração do lado da sala. */
private const val LARGURA_BYTE = 0.05f

/** Onde fica o pé no desenho de 400 x 480 do Byte. */
private const val PE_BYTE = 0.93f
private const val BYTE_ALTURA_POR_LARGURA = 1.2f

@Composable
internal fun GenteDaSala(app: AppState, side: Dp) {
    val scope = rememberCoroutineScope()
    var puloByte by remember { mutableIntStateOf(0) }
    val iso = Iso.aula(1f)
    ProfessorNaSala(side)
    val mesa = iso.p(NaAula.BYTE)
    val w = side * LARGURA_BYTE
    ByteFigure(
        Modifier.offset(side * mesa.x - w / 2, side * mesa.y - w * BYTE_ALTURA_POR_LARGURA * PE_BYTE).width(w)
            .clickable(remember { MutableInteractionSource() }, null) {
                puloByte++
                Juice.click()
                scope.launch { Narrator.say(Falas.toqueNoByte.random()) }
            },
        mood = if (app.ls.streakDays >= 2) Mood.HAPPY else Mood.NEUTRAL, hop = puloByte,
    )
}
