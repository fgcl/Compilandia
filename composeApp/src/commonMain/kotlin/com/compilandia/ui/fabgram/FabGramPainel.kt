package com.compilandia.ui.fabgram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.tokensOf
import com.compilandia.textos.tx
import com.compilandia.tutor.Corrida
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.TipoBloco
import com.compilandia.tutor.Falha
import com.compilandia.tutor.Laudo
import com.compilandia.tutor.Motivo
import com.compilandia.tutor.Quebra
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette

private val TINTA_CODIGO = Color(0xFFE8E4D8)
private val VIDRO = Color(0xE6202328)

/** O painel da gramática: o texto, os exemplos, a frase e os sinais. */
@Composable
internal fun Painel(e: EstadoFabrica, laudo: Laudo?, feitas: Int, onFechar: () -> Unit) {
    val fonts = LocalFonts.current
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tx("fabgram_gramatica"), style = MaterialTheme.typography.titleMedium, color = Palette.ink, modifier = Modifier.weight(1f))
            Text(tx("fabgram_fechar"), color = Palette.inkSoft, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onFechar() }.padding(8.dp))
        }
        ListaMissoes(e, feitas)
        BasicTextField(
            e.texto, { e.livre(); e.texto = it; e.corrida = null; e.sel = null },
            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(10.dp).testTag("fabgram-texto"),
            textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 15.sp, color = TINTA_CODIGO), cursorBrush = SolidColor(Palette.yellow),
        )
        Text(tx("fabgram_ajuda_texto"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
        Text(tx("fabgram_frase"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
        BasicTextField(
            e.frase, { if (e.missao != null) e.livre(); e.frase = it.take(40); e.corrida = null },
            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(horizontal = 10.dp, vertical = 10.dp).testTag("fabgram-frase"),
            singleLine = true, textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 16.sp, color = TINTA_CODIGO), cursorBrush = SolidColor(Palette.yellow),
        )
        Text(tx("fabgram_exemplos"), style = MaterialTheme.typography.labelLarge, color = Palette.ink)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((g, f) in FabGram.exemplos) Text(
                g.lines().first(), fontFamily = fonts.mono, fontSize = 13.sp, color = Palette.ink,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Palette.paperDark)
                    .clickable { e.livre(); e.texto = g; e.frase = f; e.corrida = null; e.sel = null }.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        val m = e.planta?.blocos?.firstOrNull { it.id == e.sel }?.prod
        Text(if (m != null) tx("fabgram_maquina_1", m.lhs + " → " + m.rhs.joinToString(" ").ifEmpty { "ε" }) else tx("fabgram_toque"), color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
        Sinais(laudo)
    }
}

/** Cada defeito do Laboratório vira um sinal físico nas máquinas. */
@Composable
private fun ColumnScope.Sinais(l: Laudo?) {
    if (l?.g == null) return
    Text(tx("fabgram_sinais"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
    if (l.problemas.isEmpty()) Text(tx("fabgram_sem_sinais"), color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
    for (p in l.problemas) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 4.dp).size(10.dp).clip(RoundedCornerShape(2.dp)).background(corSinal(p.falha)))
        Column {
            Text(nomeSinal(p.falha), fontWeight = FontWeight.Bold, color = Palette.ink, style = MaterialTheme.typography.bodyMedium)
            Text(p.aponta, color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** A frase pedida no alto: cada casa se enche quando a peça chega à montadora. */
@Composable
internal fun Casas(c: Corrida?, pedida: List<String>, t: Float) {
    val fonts = LocalFonts.current
    val chegou = c?.chegadas?.filter { it.t <= t }.orEmpty()
    val n = maxOf(pedida.size, chegou.maxOfOrNull { it.casa + 1 } ?: 0)
    if (n == 0) return
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(VIDRO).horizontalScroll(rememberScrollState()).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        for (i in 0 until n) {
            val veio = chegou.firstOrNull { it.casa == i }?.simbolo
            val pede = pedida.getOrNull(i)
            val falta = c != null && t >= c.duracao && veio == null
            val bg = when {
                veio != null && veio == pede -> Palette.green
                veio != null -> Palette.red
                falta -> Palette.red.copy(alpha = 0.35f)
                else -> Color(0xFF33373E)
            }
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 11.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text(veio ?: pede ?: "·", fontFamily = fonts.mono, fontSize = 17.sp, color = if (veio != null) Palette.on(bg) else PLACA)
            }
        }
    }
}

/** O que aconteceu, ou o que o dedo faz agora. */
@Composable
internal fun Aviso(e: EstadoFabrica, l: Laudo?) {
    val c = e.corrida
    val q = c?.quebra
    val msg = when {
        l?.erro != null -> tx("fabgram_erro_1", l.erro)
        tokensOf(e.frase).isEmpty() -> tx("fabgram_vazia")
        c != null && !e.terminou -> null
        q != null -> textoQuebra(q)
        e.cumpriu -> null
        c != null && c.aceita && c.ambigua -> tx("fabgram_ambigua")
        c != null && c.aceita -> tx("fabgram_fez")
        c?.feita != null -> tx("fabgram_nao_faz_2", c.pedida.joinToString(" "), c.feita.joinToString(" ").ifEmpty { "ε" })
        c != null -> tx("fabgram_nenhuma_1", c.pedida.joinToString(" "))
        e.missao != null -> null
        else -> when (e.ferramenta) {
            Ferramenta.MOVER -> tx("fabgram_dica_mover"); Ferramenta.ESTEIRA -> tx("fabgram_dica_esteira"); Ferramenta.APAGAR -> tx("fabgram_dica_apagar")
        }
    } ?: return
    val ruim = q != null || (c != null && e.terminou && !c.aceita)
    Text(
        msg, color = PLACA, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(VIDRO).then(if (ruim) Modifier.border(1.dp, VERMELHO, RoundedCornerShape(8.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp).testTag("fabgram-msg"),
    )
}

@Composable
private fun textoQuebra(q: Quebra): String {
    val quem = if (q.caixa) tx("fabgram_caixa_1", q.simbolo) else tx("fabgram_peca_1", q.simbolo)
    val d = q.destino
    val para = when (d.tipo) {
        TipoBloco.SEPARADOR -> tx("fabgram_destino_separador_1", d.nt.orEmpty())
        TipoBloco.MAQUINA -> tx("fabgram_destino_maquina_1", d.prod!!.lhs + " → " + d.prod.rhs.joinToString(" ").ifEmpty { "ε" })
        else -> tx("fabgram_destino_montadora")
    }
    return when (q.motivo) {
        Motivo.SEM_SAIDA -> tx("fabgram_quebra_sem_saida_2", quem, para)
        Motivo.ACABA -> tx("fabgram_quebra_acaba_2", quem, para)
        Motivo.BATE -> tx("fabgram_quebra_bate_2", quem, para)
        Motivo.VOLTA -> tx("fabgram_quebra_volta_2", quem, para)
    }
}

/** A barra de baixo: o que o dedo faz, desfazer, refazer e arrumar. O Ligar fica no alto, ao lado da frase. */
@Composable
internal fun Ferramentas(e: EstadoFabrica, modifier: Modifier, onArrumar: () -> Unit) {
    Row(
        modifier.padding(6.dp).clip(RoundedCornerShape(12.dp)).background(VIDRO).horizontalScroll(rememberScrollState()).padding(5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        for ((f, nome) in listOf(Ferramenta.MOVER to tx("fabgram_mover"), Ferramenta.ESTEIRA to tx("fabgram_esteira"), Ferramenta.APAGAR to tx("fabgram_apagar"))) {
            val on = e.ferramenta == f
            Text(
                nome, color = if (on) OL else PLACA, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (on) AC2 else Color.White.copy(alpha = 0.08f))
                    .clickable { e.ferramenta = f }.padding(horizontal = 9.dp, vertical = 9.dp).testTag("fabgram-${f.name.lowercase()}"),
            )
        }
        Icone(Forma.VOLTA, ativo = e.desfazer.isNotEmpty(), descricao = tx("fabgram_desfazer"), tag = "fabgram-desfazer") { e.volta() }
        Icone(Forma.AVANCA, ativo = e.refazer.isNotEmpty(), descricao = tx("fabgram_refazer"), tag = "fabgram-refazer") { e.avanca() }
        Icone(Forma.RECOMECA, ativo = true, descricao = tx("fabgram_arrumar"), tag = "fabgram-arrumar") { onArrumar() }
    }
}

private enum class Forma { VOLTA, AVANCA, RECOMECA }

/** Ícone desenhado: no Wasm, um glifo que falta na fonte vira um quadrado. */
@Composable
private fun Icone(forma: Forma, ativo: Boolean, descricao: String, tag: String, onClick: () -> Unit) {
    val cor = if (ativo) PLACA else PLACA.copy(alpha = 0.3f)
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.08f))
            .then(if (ativo) Modifier.clickable { onClick() } else Modifier).semantics { contentDescription = descricao }.testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(18.dp)) {
            val w = size.width
            val esp = w * 0.13f
            val flip = if (forma == Forma.AVANCA) -1f else 1f
            val arco = if (forma == Forma.RECOMECA) 300f else 250f
            withTransform({ scale(flip, 1f, center) }) {
                drawArc(cor, 200f, arco, false, topLeft = Offset(w * 0.18f, w * 0.2f), size = Size(w * 0.64f, w * 0.64f), style = Stroke(esp, cap = StrokeCap.Round))
                val seta = Path().apply { moveTo(w * 0.02f, w * 0.36f); lineTo(w * 0.32f, w * 0.30f); lineTo(w * 0.16f, w * 0.60f); close() }
                drawPath(seta, cor)
            }
        }
    }
}

@Composable
internal fun nomeSinal(f: Falha): String = when (f) {
    Falha.IMPRODUTIVO -> tx("fabgram_sinal_fumaca")
    Falha.INALCANCAVEL -> tx("fabgram_sinal_poeira")
    Falha.RECURSAO, Falha.RECURSAO_INDIRETA -> tx("fabgram_sinal_pisca")
    Falha.PREFIXO -> tx("fabgram_sinal_lampada")
    Falha.CONFLITO_LL, Falha.CONFLITO_SLR -> tx("fabgram_sinal_emperra")
    Falha.AMBIGUA -> tx("fabgram_sinal_fichas")
}
