package com.compilandia.ui.games

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.compilandia.resources.Res
import com.compilandia.resources.prop_gear
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import org.jetbrains.compose.resources.painterResource
import com.compilandia.textos.tx

const val MACHINE_LAMPS = 6
private val LAMP_ON = Color(0xFFFFE28A)
private val STEEL = Color(0xFF5E7076)
private val STEEL_HI = Color(0xFF7F939A)
private val STEEL_LO = Color(0xFF3F4E53)
private val DARK = Color(0xFF2B3336)

/** O Grande Compilador: cada acerto no chefão acende uma lâmpada, e a última religa a máquina. */
@Composable
fun MachinePanel(lit: Int, running: Boolean, region: String, modifier: Modifier = Modifier) {
    val power by animateFloatAsState(lit.toFloat() / MACHINE_LAMPS, tween(600))
    val inf = rememberInfiniteTransition()
    val spin by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(if (running) 900 else 5200, easing = LinearEasing), RepeatMode.Restart))
    val blink by inf.animateFloat(0.25f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))
    val angle = if (ThemeState.reduceMotion) 0f else spin * (0.25f + 0.75f * power)
    Column(modifier.fillMaxWidth().padding(10.dp).clip(RoundedCornerShape(10.dp)).background(STEEL).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (i in 0 until MACHINE_LAMPS) {
                val on = i < lit
                val next = i == lit && !running
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(
                        when {
                            on -> LAMP_ON
                            next -> LAMP_ON.copy(alpha = 0.18f + 0.5f * blink)
                            else -> DARK
                        },
                    ),
                )
            }
        }
        Canvas(Modifier.fillMaxWidth().height(14.dp)) {
            drawRoundRect(DARK, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
            drawRoundRect(Palette.hazard, Offset.Zero, Size(size.width * power, size.height), androidx.compose.ui.geometry.CornerRadius(size.height / 2))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(Res.drawable.prop_gear), null, Modifier.size(70.dp).rotate(angle))
            Image(painterResource(Res.drawable.prop_gear), null, Modifier.offset(x = (-8).dp, y = 10.dp).size(52.dp).rotate(-angle * 1.4f))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(tx("machine_grande_compilador"), color = Color(0xFFF2EEE3), style = MaterialTheme.typography.titleMedium)
                Text(if (running) tx("machine_religado") else region, color = if (running) LAMP_ON else Color(0xFFD9D4C7), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    @Suppress("UNUSED_VARIABLE") val unused = listOf(STEEL_HI, STEEL_LO)
}
