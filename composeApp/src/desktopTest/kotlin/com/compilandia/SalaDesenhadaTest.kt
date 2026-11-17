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
import com.compilandia.ui.room.PecasDaSala
import com.compilandia.ui.room.SalaDesenhada
import com.compilandia.ui.room.art.Iso
import com.compilandia.ui.room.art.Campus
import com.compilandia.ui.room.art.ambienteDaSala
import com.compilandia.ui.room.art.besouro
import com.compilandia.ui.room.art.campus
import com.compilandia.ui.cast.art.Humor
import com.compilandia.ui.cast.art.poseDoProfessor
import com.compilandia.ui.cast.art.professorRecortado
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import com.compilandia.ui.room.art.moveisArte
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A sala e o campus desenhados por código: todo item da loja tem desenho; as fotos saem em build/screenshots. */
class SalaDesenhadaTest : RenderBase() {
    @Test fun todoItemDaLojaTemDesenho() {
        assertEquals(RoomItem.entries.map { it.id }.toSet(), moveisArte.keys)
        // O centro de cada móvel cai dentro do quadrado da sala.
        for ((id, m) in moveisArte) {
            val c = Iso.aula(1f).p(m.centro)
            assertTrue(c.x in 0f..1f && c.y in 0f..1f, "$id fora da sala: $c")
        }
    }

    @Test fun fotos() {
        shot("sala_vazia", LADO, LADO) {
            Box(Modifier.fillMaxSize()) {
                SalaDesenhada("sala")
                PecasDaSala({ false }, null) {}
            }
        }
        shot("sala_cheia_sozinha", LADO, LADO) {
            Box(Modifier.fillMaxSize()) {
                SalaDesenhada("sala")
                PecasDaSala({ true }, null) {}
            }
        }
        shot("campus", LADO, LADO) {
            Box(Modifier.fillMaxSize()) { androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) { campus(Campus.iso(size.width)) } }
        }
        shot("sala_ambiente", LADO, LADO) {
            Box(Modifier.fillMaxSize()) {
                SalaDesenhada("sala")
                PecasDaSala({ true }, null) {}
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val iso = Iso.aula(size.width)
                    ambienteDaSala(iso, 1.2f) { true }
                    besouro(iso.p(5f, 7.7f), size.width * 0.011f, 15f, 1f)
                }
            }
        }
        shot("professor_poses", 4 * 200, 320) {
            Row {
                for ((h, t) in listOf(Humor.PARADO to 0.3f, Humor.ANDANDO to 0.15f, Humor.ALEGRIA to 0.5f, Humor.CONTENTE to 1f)) {
                    androidx.compose.foundation.Canvas(Modifier.size(200.dp, 320.dp)) {
                        scale(size.width / 200f, size.width / 200f, Offset.Zero) { professorRecortado(poseDoProfessor(h, t, false), t) }
                    }
                }
            }
        }
        shot("campus_abertura") { com.compilandia.ui.room.AberturaDoCampus {} }
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
