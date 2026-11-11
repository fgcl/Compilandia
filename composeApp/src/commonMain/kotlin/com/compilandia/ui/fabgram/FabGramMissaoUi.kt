package com.compilandia.ui.fabgram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.compilandia.textos.tx
import com.compilandia.tutor.dica
import com.compilandia.tutor.Missao
import com.compilandia.tutor.TUTORIAL
import com.compilandia.ui.BigButton
import com.compilandia.ui.Palette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Título e objetivo da missão: uma linha cada. */
@Composable
internal fun tituloMissao(m: Missao) = when (m) {
    Missao.LIGAR -> tx("fabgram_m_ligar_titulo"); Missao.CONSERTAR -> tx("fabgram_m_consertar_titulo"); Missao.MOVER -> tx("fabgram_m_mover_titulo")
    Missao.ESCOLHA -> tx("fabgram_m_escolha_titulo"); Missao.VOLTA -> tx("fabgram_m_volta_titulo"); Missao.LETRAS -> tx("fabgram_m_letras_titulo")
    Missao.D_ANBN -> tx("fabgram_d_anbn_titulo"); Missao.D_PARENTESES -> tx("fabgram_d_parenteses_titulo"); Missao.D_EXPRESSAO -> tx("fabgram_d_expressao_titulo")
    Missao.D_LISTA -> tx("fabgram_d_lista_titulo"); Missao.D_DECLARACAO -> tx("fabgram_d_declaracao_titulo"); Missao.D_IFELSE -> tx("fabgram_d_ifelse_titulo")
}

@Composable
internal fun objetivoMissao(m: Missao) = when (m) {
    Missao.LIGAR -> tx("fabgram_m_ligar_objetivo"); Missao.CONSERTAR -> tx("fabgram_m_consertar_objetivo"); Missao.MOVER -> tx("fabgram_m_mover_objetivo")
    Missao.ESCOLHA -> tx("fabgram_m_escolha_objetivo"); Missao.VOLTA -> tx("fabgram_m_volta_objetivo"); Missao.LETRAS -> tx("fabgram_m_letras_objetivo")
    else -> tx("fabgram_d_objetivo_1", m.extras.joinToString(" · "))
}

/** O número no cartão: "2 de 6" no tutorial, "Desafio 1" depois. */
@Composable
private fun rotulo(m: Missao) = if (m.desafio) tx("fabgram_desafio_1", m.ordinal - TUTORIAL + 1) else tx("fabgram_missao_2", m.ordinal + 1, TUTORIAL)

/**
 * O cartão da missão no alto do palco: "2 de 6 · Conserte a esteira" e o que fazer.
 * No desafio, mostra quantas esteiras você usou e as estrelas que isso vale.
 * Cumprida, vira um botão para a próxima. [onProxima] recebe nulo no fim: vai para o modo livre.
 */
@Composable
internal fun CartaoMissao(e: EstadoFabrica, onProxima: (Missao?) -> Unit) {
    val pronta = e.missao ?: return
    val m = pronta.missao
    val proxima = Missao.entries.getOrNull(m.ordinal + 1)
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).background(VIDRO_MISSAO).border(1.5.dp, if (e.cumpriu) VERDE else AC, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp).testTag("fabgram-missao"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(rotulo(m) + " · " + tituloMissao(m), color = AC2, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        if (pronta.orcamento != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tx("fabgram_contador_2", e.planta?.esteiras?.size ?: 0, pronta.orcamento), color = PLACA, style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("fabgram-contador"))
            Estrelas(if (e.cumpriu) e.estrelas else 0, 16.dp)
        }
        if (e.cumpriu) {
            Text(tx("fabgram_missao_feita") + if (e.ganhou > 0) "  " + tx("fabgram_ganhou_1", e.ganhou) else "", color = PLACA, style = MaterialTheme.typography.bodyMedium)
            // No else solto o ponto é este: a mesma frase saiu de dois jeitos.
            if (e.corrida?.ambigua == true) Text(tx("fabgram_ambigua"), color = Color(0xFF8FB8FF), style = MaterialTheme.typography.bodyMedium)
            BigButton(if (proxima != null) tx("fabgram_proxima") else tx("fabgram_livre"), small = true, color = Palette.green) { onProxima(proxima) }
        } else {
            Text(objetivoMissao(m), color = PLACA, style = MaterialTheme.typography.bodyMedium)
            // Dica do desafio: mostra em azul o próximo trecho que falta e custa uma estrela.
            if (m.desafio) Text(
                if (e.dicas == 0) tx("fabgram_dica_custa") else tx("fabgram_dica_mais_1", e.dicas),
                color = Color(0xFF8CC4EE), style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF8CC4EE), RoundedCornerShape(8.dp))
                    .clickable {
                        val pl = e.planta ?: return@clickable
                        val d = com.compilandia.tutor.FabGram.dica(pronta, pl)
                        if (d.isNotEmpty()) { e.dicaGuia = d; e.dicas++ }
                    }.padding(horizontal = 12.dp, vertical = 6.dp).testTag("fabgram-dica"),
            )
        }
    }
}

/** Três estrelas desenhadas; as [cheias] primeiras acesas. No Wasm, a estrela da fonte pode virar quadrado. */
@Composable
internal fun Estrelas(cheias: Int, tamanho: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 0 until 3) Canvas(Modifier.size(tamanho)) {
            val raio = size.minDimension / 2
            val estrela = Path().apply {
                for (k in 0 until 10) {
                    val a = -PI / 2 + k * PI / 5
                    val rr = if (k % 2 == 0) raio else raio * 0.45f
                    val x = center.x + (cos(a) * rr).toFloat(); val y = center.y + (sin(a) * rr).toFloat()
                    if (k == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(estrela, if (i < cheias) AC2 else Color.White.copy(alpha = 0.18f))
        }
    }
}

/** No painel: as missões do tutorial, os desafios e o modo livre. Abre até a primeira que falta. */
@Composable
internal fun ListaMissoes(e: EstadoFabrica, feitas: Int, estrelas: Map<String, Int>) {
    Text(tx("fabgram_missoes"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (m in Missao.entries) {
            val aberta = m.ordinal <= feitas
            val atual = e.missao?.missao == m
            val bg = when { atual -> AC2; m.ordinal < feitas -> Palette.green.copy(alpha = 0.25f); else -> Palette.paperDark }
            Column(
                Modifier.clip(RoundedCornerShape(6.dp)).background(bg).then(if (aberta) Modifier.clickable { e.abre(m) } else Modifier)
                    .padding(horizontal = 10.dp, vertical = 6.dp).testTag("fabgram-missao-${m.ordinal + 1}"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (m.desafio) "D${m.ordinal - TUTORIAL + 1}" else "${m.ordinal + 1}",
                    color = if (aberta) Palette.ink else Palette.inkSoft.copy(alpha = 0.5f), fontWeight = FontWeight.Bold,
                )
                if (m.desafio && estrelas[m.name] != null) Estrelas(estrelas.getValue(m.name), 9.dp)
            }
        }
        Text(
            tx("fabgram_livre"), color = Palette.ink, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (e.missao == null) AC2 else Palette.paperDark)
                .clickable { e.livre() }.padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-livre"),
        )
    }
}

private val VIDRO_MISSAO = Color(0xF01B1E22)
