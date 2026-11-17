package com.compilandia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.compilandia.learning.RoomItem
import com.compilandia.ui.room.MovelIcone
import com.compilandia.ui.room.MovelNaSala
import com.compilandia.ui.room.SalaDesenhada
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.moveisArte
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A sala desenhada por código: todo item da loja tem desenho, e as fotos saem em build/screenshots/sala_*.png. */
class SalaDesenhadaTest : RenderBase() {
    @Test fun todoItemDaLojaTemDesenho() {
        assertEquals(RoomItem.entries.map { it.id }.toSet(), moveisArte.keys)
        // O centro de cada móvel cai dentro do quadrado da sala.
        for ((id, m) in moveisArte) {
            val c = Iso.sala(1f).p(m.centro)
            assertTrue(c.x in 0f..1f && c.y in 0f..1f, "$id fora da sala: $c")
        }
    }

    @Test fun fotos() {
        shot("sala_vazia", LADO, LADO) { Box(Modifier.fillMaxSize()) { SalaDesenhada("sala") } }
        shot("sala_cheia_sozinha", LADO, LADO) {
            Box(Modifier.fillMaxSize()) {
                SalaDesenhada("sala")
                for (id in moveisArte.keys) MovelNaSala(id, popIn = false) {}
            }
        }
        shot("sala_icones", ICONE * 6, ICONE * 2) {
            Column {
                for (linha in moveisArte.keys.chunked(6)) {
                    Row { for (id in linha) MovelIcone(id, Modifier.size(ICONE.dp)) }
                }
            }
        }
    }

    private companion object {
        const val LADO = 550
        const val ICONE = 72
    }
}
