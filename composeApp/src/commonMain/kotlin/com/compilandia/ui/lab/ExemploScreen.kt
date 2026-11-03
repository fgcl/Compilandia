package com.compilandia.ui.lab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.compilandia.learning.Skill
import com.compilandia.platform.nowMs
import com.compilandia.tutor.Sims
import com.compilandia.ui.AppState
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import kotlin.random.Random

/** A mesma ideia da aula, com um exemplo novo que o motor acabou de gerar. */
@Composable
fun ExemploScreen(app: AppState, skill: Skill) {
    var n by remember { mutableIntStateOf(0) }
    val sim = remember(n) { Sims.exemplo(skill, Random(nowMs() + n)) }
    Column(Modifier.fillMaxSize()) {
        Header(sim?.titulo ?: skill.title, null, Palette.region(skill.region)) {
            Box(Modifier.clip(RoundedCornerShape(8.dp)).clickable { n++ }.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text("Outro", color = Palette.on(Palette.region(skill.region)), style = MaterialTheme.typography.labelMedium)
            }
        }
        if (sim != null) SimPlayer(sim, Modifier.weight(1f))
    }
    @Suppress("UNUSED_EXPRESSION") app
}
