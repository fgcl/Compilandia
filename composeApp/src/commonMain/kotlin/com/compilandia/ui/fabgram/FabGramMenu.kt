package com.compilandia.ui.fabgram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.fabrica.BlocoTipo
import com.compilandia.fabrica.Blocos
import com.compilandia.fabrica.Categoria
import com.compilandia.fabrica.Loja
import com.compilandia.textos.tx
import com.compilandia.tutor.FabGram

/**
 * O menu de construção, no molde do PlacementFragment do Mindustry: uma aba por [Categoria] e,
 * dentro, os blocos que dá para pôr no chão. O bloco trancado mostra o preço; tocar nele abre a loja.
 * A primeira linha tem o que não é bloco: mover, apagar, desfazer, refazer e arrumar.
 */
@Composable
internal fun MenuConstrucao(e: EstadoFabrica, comprados: List<String>, modifier: Modifier, onArrumar: () -> Unit) {
    Column(
        modifier.padding(6.dp).clip(RoundedCornerShape(12.dp)).background(VIDRO_MENU).padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        LinhaDeFerramentas(e, onArrumar)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (c in Categoria.entries.filter { Blocos.doMenu(it).isNotEmpty() }) Aba(c, e.aba == c) { e.aba = c }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (t in Blocos.doMenu(e.aba)) {
                val tem = Loja.tem(t, comprados)
                BotaoBloco(t, e.escolhido(t), tem) { e.escolhe(t, tem) }
            }
        }
    }
}

@Composable
private fun LinhaDeFerramentas(e: EstadoFabrica, onArrumar: () -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val ferramentas = listOf(
            Ferramenta.MOVER to tx("fabgram_mover"), Ferramenta.ESTEIRA to tx("fabgram_esteira"), Ferramenta.APAGAR to tx("fabgram_apagar"),
        )
        for ((f, nome) in ferramentas) {
            Botao(nome, e.ferramenta == f, "fabgram-${f.name.lowercase()}") { e.ferramenta = f; e.construindo = null }
        }
        Icone(Forma.VOLTA, e.desfazer.isNotEmpty(), tx("fabgram_desfazer"), "fabgram-desfazer") { e.volta() }
        Icone(Forma.AVANCA, e.refazer.isNotEmpty(), tx("fabgram_refazer"), "fabgram-refazer") { e.avanca() }
        Icone(Forma.RECOMECA, ativo = true, descricao = tx("fabgram_arrumar"), tag = "fabgram-arrumar") { onArrumar() }
    }
}

@Composable
private fun Botao(texto: String, on: Boolean, tag: String, onClick: () -> Unit) {
    Text(
        texto, color = if (on) OL else PLACA, style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (on) AC2 else BOTAO_APAGADO)
            .clickable { onClick() }.padding(horizontal = 9.dp, vertical = 8.dp).testTag(tag),
    )
}

@Composable
private fun Aba(c: Categoria, on: Boolean, onClick: () -> Unit) {
    val cor = corDaCategoria(Blocos.doMenu(c).first())
    Text(
        nomeCategoria(c), color = if (on) OL else cor, style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) cor else BOTAO_APAGADO)
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("fabgram-aba-${c.name.lowercase()}"),
    )
}

/** Um bloco do menu: o desenho dele em miniatura, o nome e, se trancado, o cadeado com o preço. */
@Composable
private fun BotaoBloco(t: BlocoTipo, on: Boolean, tem: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(62.dp).clip(RoundedCornerShape(8.dp))
            .background(if (on) AC2.copy(alpha = 0.25f) else BOTAO_APAGADO)
            .then(if (on) Modifier.border(1.5.dp, AC2, RoundedCornerShape(8.dp)) else Modifier)
            .clickable { onClick() }.padding(4.dp).testTag("fabgram-bloco-${t.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Miniatura(t, Modifier.size(40.dp).alpha(if (tem) 1f else 0.4f))
            if (!tem) Preco(t.preco, Modifier.align(Alignment.BottomEnd))
        }
        Text(nomeBloco(t), color = PLACA, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

/** O preço do bloco trancado: a engrenagem desenhada (a fonte do site não tem o glifo) e o número. */
@Composable
private fun Preco(preco: Int, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(4.dp)).background(OL).padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.compilandia.ui.GearIcon(AC2, Modifier.size(11.dp))
        Text("$preco", color = AC2, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** O desenho do tipo, encolhido para caber em [modifier]. Serve para o menu e para a loja. */
@Composable
internal fun Miniatura(t: BlocoTipo, modifier: Modifier) {
    val medidor = rememberTextMeasurer()
    Canvas(modifier) {
        val lado = maxOf(t.w, t.h)
        val cel = size.minDimension / lado
        val u = cel / CELULA_DA_ARTE
        val b = FabGram.Bloco(-1, t, 0, 0, nt = if (t == Blocos.FONTE || t == Blocos.SEPARADOR) "S" else null)
        val w = t.w * cel; val h = t.h * cel
        val ox = (size.width - w) / 2; val oy = (size.height - h) / 2
        with(Desenhos.de(t)) { pinta(Pincel(u, 0f, true, false, medidor), b, ox, oy, w, h) }
    }
}

private const val CELULA_DA_ARTE = 32f


@Composable
internal fun nomeCategoria(c: Categoria): String = when (c) {
    Categoria.DISTRIBUICAO -> tx("fabgram_cat_distribuicao")
    Categoria.PRODUCAO -> tx("fabgram_cat_producao")
    Categoria.NUCLEO -> tx("fabgram_cat_nucleo")
}

@Composable
internal fun nomeBloco(t: BlocoTipo): String = when (t) {
    Blocos.FONTE -> tx("fabgram_bloco_fonte"); Blocos.MONTADORA -> tx("fabgram_bloco_montadora")
    Blocos.CESTO -> tx("fabgram_bloco_cesto"); Blocos.ESTEIRA -> tx("fabgram_bloco_esteira")
    Blocos.SEPARADOR -> tx("fabgram_bloco_separador"); Blocos.ROTEADOR -> tx("fabgram_bloco_roteador")
    Blocos.JUNCAO -> tx("fabgram_bloco_juncao"); Blocos.COFRE -> tx("fabgram_bloco_cofre")
    Blocos.PRENSA -> tx("fabgram_bloco_prensa"); Blocos.BRACO -> tx("fabgram_bloco_braco")
    Blocos.ARMARIO -> tx("fabgram_bloco_armario")
    else -> t.id
}

@Composable
internal fun descricaoBloco(t: BlocoTipo): String = when (t) {
    Blocos.FONTE -> tx("fabgram_bloco_fonte_desc"); Blocos.MONTADORA -> tx("fabgram_bloco_montadora_desc")
    Blocos.CESTO -> tx("fabgram_bloco_cesto_desc"); Blocos.ESTEIRA -> tx("fabgram_bloco_esteira_desc")
    Blocos.SEPARADOR -> tx("fabgram_bloco_separador_desc"); Blocos.ROTEADOR -> tx("fabgram_bloco_roteador_desc")
    Blocos.JUNCAO -> tx("fabgram_bloco_juncao_desc"); Blocos.COFRE -> tx("fabgram_bloco_cofre_desc")
    Blocos.PRENSA -> tx("fabgram_bloco_prensa_desc"); Blocos.BRACO -> tx("fabgram_bloco_braco_desc")
    Blocos.ARMARIO -> tx("fabgram_bloco_armario_desc")
    else -> ""
}
