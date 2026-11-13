package com.compilandia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.compilandia.content.CUIDADO_ID
import com.compilandia.content.Look
import com.compilandia.content.Passo
import com.compilandia.content.TITULO_ID
import com.compilandia.textos.tx
import com.compilandia.ui.Palette

/**
 * As partes de uma aula longa (mural, rodada 14): por que existe, o primeiro exemplo,
 * a receita, o segundo exemplo, em que a pessoa faz, e o fecho com os erros comuns e o resumo. Saem do próprio roteiro:
 * a palavra grande (ou o primeiro título novo) fecha o porquê, a cena com as linhas r1, r2... abre a receita,
 * e o primeiro título diferente depois dela abre a sua vez. Aula que não segue esse
 * caminho (pegadinha, clipe) devolve nulo e fica com a fita de sempre.
 */
fun partesDaAula(passos: List<Passo>, marcado: Int? = null): List<IntRange>? {
    val receitaRe = Regex("^r\\d+$")
    fun titulo(k: Int) = passos[k].shot.boxes.firstOrNull { it.id == TITULO_ID }?.text
    val receita = passos.indexOfFirst { p -> p.shot.boxes.count { it.look == Look.CELL && receitaRe.matches(it.id) } >= 2 }
    if (receita < 2) return null
    // O exemplo começa depois da palavra grande; aula sem ela começa no primeiro título novo.
    val palavra = passos.indexOfFirst { p -> p.shot.boxes.any { it.look == Look.BANG } }
    val exemplo = if (marcado != null && marcado in 1 until receita) marcado
        else if (palavra in 0 until receita - 1) palavra + 1
        else (1 until receita).firstOrNull { k -> titulo(k) != null && titulo(k) != titulo(k - 1) } ?: return null
    val tituloDaReceita = titulo(receita)
    val suaVez = (receita + 1 until passos.size).firstOrNull { k -> titulo(k) != null && titulo(k) != tituloDaReceita } ?: return null
    // O fecho (erros comuns e resumo) é a quinta parte, quando a aula tem: começa no primeiro cartão de cuidado.
    val fecho = (suaVez + 1 until passos.size).firstOrNull { k -> passos[k].shot.boxes.any { it.id == CUIDADO_ID } }
    val antes = listOf(0 until exemplo, exemplo until receita, receita until suaVez)
    return if (fecho == null) antes + listOf(suaVez until passos.size) else antes + listOf(suaVez until fecho, fecho until passos.size)
}

private val nomes get() = listOf(tx("aula_parte_porque"), tx("aula_parte_exemplo"), tx("aula_parte_receita"), tx("aula_parte_sua_vez"), tx("aula_parte_erros"))

/** Os nomes das partes em cima da fita, cada um com a largura da sua parte. Um toque pula para o começo dela. */
@Composable
fun RotulosDasPartes(partes: List<IntRange>, index: Int, maisLonge: Int, onSeek: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 8.dp).padding(top = 3.dp).height(18.dp).testTag("partes-da-aula")) {
        partes.forEachIndexed { k, parte ->
            val aqui = index in parte
            val aberta = parte.first <= maisLonge
            Box(
                // O respiro no fim impede que um nome cortado encoste no próximo ("ReceitSua vez").
                Modifier.weight(parte.count().toFloat()).padding(end = 4.dp).then(if (aberta && !aqui) Modifier.clickable { onSeek(parte.first) } else Modifier),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    nomes[k], maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false,
                    color = when { aqui -> Palette.hazard; aberta -> Palette.ink; else -> Palette.inkSoft.copy(alpha = 0.6f) },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
