package com.compilandia.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.PassoReacao
import com.compilandia.textos.tx
import com.compilandia.ui.BigButton
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.bancada.ControlesPalco
import com.compilandia.ui.bancada.LinhaDoTempo
import com.compilandia.ui.bancada.TocaLinha

/**
 * J8: depois de classificar, o resultado roda. Os passos vêm do motor ([com.compilandia.engine.Reacoes]);
 * o palco toca um por vez, com os mesmos controles da Bancada. O Continuar fica sempre à mão: errar não bloqueia.
 */
@Composable
fun Reacao(passos: List<PassoReacao>, onContinuar: () -> Unit) {
    if (passos.isEmpty()) {
        BigButton(tx("reacao_continuar"), Modifier.fillMaxWidth()) { onContinuar() }
        return
    }
    val linha = remember(passos) { LinhaDoTempo() }
    LaunchedEffect(passos) {
        linha.carrega(passos.size, tocar = !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = passos.size - 1
    }
    TocaLinha(linha)
    val mono = LocalFonts.current.mono
    val atual = passos.getOrElse(linha.atual) { passos.last() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.codeBg).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(tx("reacao_titulo"), color = Palette.hazard, style = MaterialTheme.typography.titleSmall)
        // Os passos já tocados ficam visíveis; o atual tem borda acesa.
        for ((i, p) in passos.withIndex()) {
            if (i > linha.atual) break
            val cor = when (p.ok) { true -> Palette.green; false -> Palette.red; null -> Palette.onAccent.copy(alpha = 0.25f) }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(if (i == linha.atual) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                    .border(if (i == linha.atual) 2.dp else 1.dp, cor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                val marca = when (p.ok) { true -> "✓ "; false -> "✗ "; null -> "" }
                Text(marca + p.linha, fontFamily = mono, fontSize = 13.sp, fontWeight = if (i == linha.atual) FontWeight.Bold else FontWeight.Normal, color = Palette.onAccent)
            }
        }
    }
    ControlesPalco(linha, atual.nota)
    BigButton(tx("reacao_continuar"), Modifier.fillMaxWidth()) { onContinuar() }
}
