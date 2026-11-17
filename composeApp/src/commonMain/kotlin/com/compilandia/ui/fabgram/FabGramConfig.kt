package com.compilandia.ui.fabgram

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.fabrica.BlocoTipo
import com.compilandia.fabrica.Loja
import com.compilandia.fabrica.Papel
import com.compilandia.textos.tx
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.ui.BigButton
import com.compilandia.ui.Palette

/**
 * Configurar um bloco, como o sorter do Mindustry escolhe o item: a máquina escolhe a regra,
 * o separador e a fonte escolhem a letra. A máquina só aceita a regra que cabe nela
 * (o cofre leva uma peça, a prensa três, o armário seis; o braço só troca a letra).
 */
@Composable
internal fun ConfigBloco(pl: Planta, b: Bloco, modifier: Modifier, onPronto: (Planta) -> Unit, onFechar: () -> Unit) {
    var letra by remember(b.id) { mutableStateOf(b.nt ?: pl.g.start) }
    val rhs = remember(b.id) { mutableStateListOf<String>().apply { addAll(b.prod?.rhs.orEmpty()) } }
    val letras = letrasDa(pl, letra)
    val ehLetra: (String) -> Boolean = { it in letras }
    val cabe = !b.def.maquina || b.def.cabe(rhs, ehLetra)
    Column(
        modifier.widthIn(max = 520.dp).padding(8.dp).clip(RoundedCornerShape(12.dp)).background(VIDRO_MENU)
            .border(1.5.dp, corDaCategoria(b.def), RoundedCornerShape(12.dp)).padding(12.dp).testTag("fabgram-config"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TituloDoBloco(b.def)
        Text(tituloConfig(b), color = PLACA, style = MaterialTheme.typography.labelLarge)
        Fichas(letras + novaLetra(letras), letra, "fabgram-cfg-letra") { letra = it }
        if (b.def.maquina) LadoDireito(pl, b.def, letra, rhs, letras, cabe)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BigButton(tx("fabgram_cfg_pronto"), small = true, enabled = cabe, color = Palette.green) {
                onPronto(pl.configura(b.id, letra, if (b.def.maquina) rhs.toList() else null).esticaSeparadores())
            }
            BigButton(tx("fabgram_fechar"), small = true, color = Palette.paperDark) { onFechar() }
        }
    }
}

@Composable
private fun TituloDoBloco(t: BlocoTipo) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Miniatura(t, Modifier.size(36.dp))
        Column(Modifier.weight(1f)) {
            Text(nomeBloco(t), color = AC2, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(descricaoBloco(t), color = PLACA, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun tituloConfig(b: Bloco): String = when (b.tipo) {
    Papel.FONTE -> tx("fabgram_cfg_fonte")
    Papel.SEPARADOR -> tx("fabgram_cfg_separador")
    else -> tx("fabgram_cfg_regra")
}

/** As letras que a fábrica já conhece: as das máquinas, dos separadores e da fonte. */
private fun letrasDa(pl: Planta, atual: String): List<String> =
    (listOf(pl.g.start) + pl.g.nonTerminals + pl.blocos.mapNotNull { it.nt } + atual).distinct()

private fun novaLetra(letras: List<String>): List<String> =
    listOfNotNull(('A'..'Z').map { it.toString() }.firstOrNull { it !in letras })

/** O lado direito da regra: as fichas escolhidas (toque tira) e a paleta para acrescentar. */
@Composable
private fun LadoDireito(
    pl: Planta, t: BlocoTipo, letra: String, rhs: MutableList<String>, letras: List<String>, cabe: Boolean,
) {
    Text(
        letra + " → " + rhs.joinToString(" ").ifEmpty { "ε" },
        color = if (cabe) PLACA else VERMELHO, fontFamily = FontFamily.Monospace, fontSize = 18.sp,
        modifier = Modifier.testTag("fabgram-cfg-regra"),
    )
    Text(
        if (t.soLetra) tx("fabgram_cfg_so_letra") else tx("fabgram_cfg_cabe_2", rhs.size, t.capacidade),
        color = if (cabe) PLACA.copy(alpha = 0.7f) else VERMELHO, style = MaterialTheme.typography.bodySmall,
    )
    if (rhs.isNotEmpty()) Fichas(rhs.toList(), null, "fabgram-cfg-tira") { s -> rhs.removeAt(rhs.lastIndexOf(s)) }
    val paleta = if (t.soLetra) letras else (letras + pl.g.terminals + SIMBOLOS_COMUNS).distinct()
    Fichas(paleta, null, "fabgram-cfg-poe") { rhs += it }
    NovoSimbolo { rhs += it }
}

/** Símbolos que aparecem nos exercícios, à mão mesmo antes de existir máquina que use. */
private val SIMBOLOS_COMUNS = listOf("a", "b", "c", "id", "+", "*", "(", ")", ",", ";")

@Composable
private fun NovoSimbolo(onNovo: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicTextField(
            texto, { texto = it.filter { c -> !c.isWhitespace() }.take(MAX_SIMBOLO) },
            Modifier.width(110.dp).clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(8.dp)
                .testTag("fabgram-cfg-novo"),
            singleLine = true,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, color = PLACA),
            cursorBrush = SolidColor(AC2),
        )
        Ficha(tx("fabgram_cfg_novo"), false, "fabgram-cfg-acrescenta") {
            if (texto.isNotEmpty()) onNovo(texto)
            texto = ""
        }
    }
}

private const val MAX_SIMBOLO = 8

@Composable
private fun Fichas(itens: List<String>, marcado: String?, tag: String, onToque: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        for (s in itens) Ficha(s, s == marcado, "$tag-$s") { onToque(s) }
    }
}

@Composable
private fun Ficha(texto: String, on: Boolean, tag: String, onClick: () -> Unit) {
    Text(
        texto, color = if (on) OL else PLACA, fontFamily = FontFamily.Monospace, fontSize = 15.sp,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) AC2 else BOTAO_APAGADO)
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp).testTag(tag),
    )
}

/** O cartão da loja: o bloco, o que ele faz, o preço e o botão de comprar (ou o que falta para comprar). */
@Composable
internal fun CartaoLoja(
    t: BlocoTipo, comprados: List<String>, engrenagens: Int, modifier: Modifier,
    onComprar: () -> Unit, onFechar: () -> Unit,
) {
    val sit = Loja.situacao(t, comprados, engrenagens)
    Column(
        modifier.widthIn(max = 420.dp).padding(8.dp).clip(RoundedCornerShape(12.dp)).background(VIDRO_MENU)
            .border(1.5.dp, AC2, RoundedCornerShape(12.dp)).padding(14.dp).testTag("fabgram-loja"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            tx("fabgram_loja_titulo"), color = AC2, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
        )
        Miniatura(t, Modifier.size(64.dp))
        Text(nomeBloco(t), color = PLACA, fontWeight = FontWeight.Bold)
        Text(descricaoBloco(t), color = PLACA.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        Text(
            textoDaLoja(sit, t, engrenagens), color = if (sit == Loja.Situacao.COMPRA) VERDE else PLACA,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sit == Loja.Situacao.COMPRA) {
                BigButton(tx("fabgram_loja_comprar_1", t.preco), small = true, color = Palette.green) { onComprar() }
            }
            BigButton(tx("fabgram_fechar"), small = true, color = Palette.paperDark) { onFechar() }
        }
    }
}

@Composable
private fun textoDaLoja(sit: Loja.Situacao, t: BlocoTipo, engrenagens: Int): String = when (sit) {
    Loja.Situacao.TEM -> tx("fabgram_loja_tem")
    Loja.Situacao.COMPRA -> tx("fabgram_loja_saldo_1", engrenagens)
    Loja.Situacao.CARO -> tx("fabgram_loja_caro_1", t.preco - engrenagens)
    Loja.Situacao.TRANCADO -> tx("fabgram_loja_trancado_1", t.requer?.let { nomeBloco(it) }.orEmpty())
}
