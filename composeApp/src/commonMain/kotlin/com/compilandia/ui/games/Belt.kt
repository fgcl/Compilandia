package com.compilandia.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.compilandia.content.Box as StageBox
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.ui.Palette
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.motion.STAGE_W

private const val BELT_H = 262f
private const val BELT_Y = 46f
private const val CRATE = 44f
private const val TOWER_X = 306f
private const val BLOCK_H = 30f

/** Dá a cada peça uma identidade: assim o token que sai da esteira é o mesmo bloco que cai na torre. */
private class Tracker {
    var stack = emptyList<String>()
    var input = emptyList<String>()
    var stackIds = emptyList<Int>()
    var inputIds = emptyList<Int>()
    private var next = 0

    fun update(s: List<String>, i: List<String>) {
        if (s == stack && i == input && (s.isNotEmpty() || i.isNotEmpty())) return
        val consumed = input.size - i.size
        val gone = if (consumed in 1..inputIds.size) inputIds.take(consumed) else emptyList()
        inputIds = if (consumed >= 0 && input.drop(consumed) == i) inputIds.drop(consumed) else i.map { next++ }
        var k = 0
        while (k < minOf(stack.size, s.size) && stack[k] == s[k]) k++
        val ids = stackIds.take(k).toMutableList()
        for (j in k until s.size) ids += if (j == k && gone.isNotEmpty() && s.size - k == 1) gone[0] else next++
        stackIds = ids
        stack = s
        input = i
    }
}

/**
 * O jogo vira o mundo: os tokens chegam pela esteira e a pilha cresce em torre.
 * O topo fica à direita da lista [stack]; o próximo token é o primeiro de [input].
 */
@Composable
fun BeltStage(stack: List<String>, input: List<String>, modifier: Modifier = Modifier) {
    val tracker = remember { Tracker() }
    tracker.update(stack, input)
    val roll by animateFloatAsState(-input.size * 34f, tween(420))
    val visible = stack.takeLast(MAX_BLOCKS)
    val ids = tracker.stackIds.takeLast(MAX_BLOCKS)
    val boxes = mutableListOf<StageBox>()
    input.forEachIndexed { i, t ->
        boxes += StageBox("t${tracker.inputIds[i]}", t, 232f - i * (CRATE + 6f), BELT_Y, CRATE, CRATE, Look.TOKEN, if (i == 0) Tint.YELLOW else Tint.NONE, hot = i == 0)
    }
    visible.forEachIndexed { i, t ->
        boxes += StageBox(
            "t${ids[i]}", t, TOWER_X, BELT_H - 26f - i * (BLOCK_H + 3f), 86f, BLOCK_H, Look.TOKEN,
            if (i == visible.lastIndex) Tint.ORANGE else Tint.TEAL, hot = i == visible.lastIndex, size = 13f,
        )
    }
    val shot = remember(stack, input) { Shot(boxes.toList()) }
    val rail = Palette.drawer
    val railLo = Palette.drawerLo
    val floor = Palette.hazardDark
    val hazard = Palette.hazard
    Box(modifier.fillMaxWidth().height(BELT_H.dp).clip(RoundedCornerShape(10.dp)).background(Palette.paperDark)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.width / STAGE_W
            val y = (BELT_Y - CRATE / 2 - 6f) * u
            val h = (CRATE + 12f) * u
            val endX = 262f * u
            drawRect(railLo, Offset(0f, y), Size(endX, h))
            var x = (roll * u) % (34f * u)
            while (x < endX) {
                drawRect(floor, Offset(x, y), Size(14f * u, h))
                x += 34f * u
            }
            drawRect(rail, Offset(0f, y - 4f * u), Size(endX, 4f * u))
            drawRect(rail, Offset(0f, y + h), Size(endX, 4f * u))
            // base da torre
            drawRect(hazard, Offset((TOWER_X - 52f) * u, (BELT_H - 9f) * u), Size(104f * u, 5f * u))
        }
        MotionStage(shot, Modifier.fillMaxSize(), stageH = BELT_H, center = false)
    }
}

private const val MAX_BLOCKS = 6
