package com.compilandia.ui.fabgram

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.tutor.Missao
import com.compilandia.ui.BigButton
import com.compilandia.ui.Palette

/** Título e objetivo da missão: uma linha cada. */
@Composable
internal fun tituloMissao(m: Missao) = when (m) {
    Missao.LIGAR -> tx("fabgram_m_ligar_titulo"); Missao.CONSERTAR -> tx("fabgram_m_consertar_titulo"); Missao.MOVER -> tx("fabgram_m_mover_titulo")
    Missao.ESCOLHA -> tx("fabgram_m_escolha_titulo"); Missao.VOLTA -> tx("fabgram_m_volta_titulo"); Missao.LETRAS -> tx("fabgram_m_letras_titulo")
}

@Composable
internal fun objetivoMissao(m: Missao) = when (m) {
    Missao.LIGAR -> tx("fabgram_m_ligar_objetivo"); Missao.CONSERTAR -> tx("fabgram_m_consertar_objetivo"); Missao.MOVER -> tx("fabgram_m_mover_objetivo")
    Missao.ESCOLHA -> tx("fabgram_m_escolha_objetivo"); Missao.VOLTA -> tx("fabgram_m_volta_objetivo"); Missao.LETRAS -> tx("fabgram_m_letras_objetivo")
}

/**
 * O cartão da missão no alto do palco: "2 de 6 · Conserte a esteira" e o que fazer.
 * Cumprida, vira um botão para a próxima. [onProxima] recebe nulo no fim: vai para o modo livre.
 */
@Composable
internal fun CartaoMissao(e: EstadoFabrica, onProxima: (Missao?) -> Unit) {
    val m = e.missao?.missao ?: return
    val total = Missao.entries.size
    val proxima = Missao.entries.getOrNull(m.ordinal + 1)
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).background(VIDRO_MISSAO).border(1.5.dp, if (e.cumpriu) VERDE else AC, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp).testTag("fabgram-missao"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(tx("fabgram_missao_2", m.ordinal + 1, total) + " · " + tituloMissao(m), color = AC2, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        if (e.cumpriu) {
            Text(tx("fabgram_missao_feita"), color = PLACA, style = MaterialTheme.typography.bodyMedium)
            BigButton(if (proxima != null) tx("fabgram_proxima") else tx("fabgram_livre"), small = true, color = Palette.green) { onProxima(proxima) }
        } else Text(objetivoMissao(m), color = PLACA, style = MaterialTheme.typography.bodyMedium)
    }
}

/** No painel: as missões em fila e o modo livre. Abre até a primeira que falta. */
@Composable
internal fun ListaMissoes(e: EstadoFabrica, feitas: Int) {
    Text(tx("fabgram_missoes"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (m in Missao.entries) {
            val aberta = m.ordinal <= feitas
            val atual = e.missao?.missao == m
            val bg = when { atual -> AC2; m.ordinal < feitas -> Palette.green.copy(alpha = 0.25f); else -> Palette.paperDark }
            Text(
                "${m.ordinal + 1}", color = if (aberta) Palette.ink else Palette.inkSoft.copy(alpha = 0.5f), fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).then(if (aberta) Modifier.clickable { e.abre(m) } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-missao-${m.ordinal + 1}"),
            )
        }
        Text(
            tx("fabgram_livre"), color = Palette.ink, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (e.missao == null) AC2 else Palette.paperDark)
                .clickable { e.livre() }.padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-livre"),
        )
    }
}

private val VIDRO_MISSAO = Color(0xF01B1E22)
