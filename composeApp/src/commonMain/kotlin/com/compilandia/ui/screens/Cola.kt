package com.compilandia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.TITULO_ID
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Skill
import com.compilandia.textos.tx
import com.compilandia.ui.BigButton
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Palette
import com.compilandia.ui.motion.MotionStage
import com.compilandia.ui.placaDeMetal

/**
 * A cola do assunto (mural, rodada 13): a receita da aula e o exemplo resolvido, num cartão só.
 * Tudo sai da própria aula animada, para a cola nunca dizer outra coisa que a aula.
 */
object Cola {
    private val linhaDaReceita = Regex("^r\\d+$")

    /** As linhas da receita: as caixas r1, r2... da cena da receita, na ordem. */
    fun receita(s: Skill): List<String> {
        val ep = Episodes.of(s) ?: return emptyList()
        val shot = ep.passos.map { it.shot }.firstOrNull { sh -> sh.boxes.count { it.look == Look.CELL && linhaDaReceita.matches(it.id) } >= 2 } ?: return emptyList()
        return shot.boxes.filter { it.look == Look.CELL && linhaDaReceita.matches(it.id) }.sortedBy { it.id.drop(1).toInt() }.map { it.text }
    }

    /** A gramática do exemplo resolvido, para mostrar em letra de ler em cima do desenho. */
    fun gramatica(s: Skill): List<String> {
        val ep = Episodes.of(s) ?: return emptyList()
        return gramaticaDaAula(ep, ep.passos.lastIndex).orEmpty()
    }

    /** O fim da aula: o segundo exemplo já resolvido. Sem o título e sem a gramática, que a cola mostra maiores. */
    fun exemplo(s: Skill): Shot? {
        val ultimo = Episodes.of(s)?.passos?.lastOrNull()?.shot ?: return null
        val regras = gramatica(s).toSet()
        return ultimo.copy(boxes = ultimo.boxes.filter { it.id != TITULO_ID && it.text.trim() !in regras }, actor = null)
    }
}

/** O cartão da cola por cima da tela. [onAula] abre a aula animada; nulo esconde o botão. */
@Composable
fun ColaDialog(s: Skill, onFechar: () -> Unit, onAula: (() -> Unit)?) {
    val receita = remember(s) { Cola.receita(s) }
    val exemplo = remember(s) { Cola.exemplo(s) }
    Dialog(onDismissRequest = onFechar) {
        Column(
            Modifier.widthIn(max = 460.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .placaDeMetal(Palette.drawer, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi).testTag("cola"),
        ) {
            HazardStripe()
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tx("cola_titulo", s.title), color = Palette.on(Palette.drawer), style = MaterialTheme.typography.headlineSmall)
                receita.forEachIndexed { i, linha -> LinhaDaCola(i + 1, linha) }
                val regras = remember(s) { Cola.gramatica(s) }
                for (r in regras) LinhaDeRegra(r)
                if (exemplo != null) {
                    Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(10.dp)).background(Palette.codeBg)) {
                        MotionStage(exemplo, Modifier.fillMaxSize().padding(4.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onAula != null) BigButton(tx("cola_ver_aula"), Modifier.weight(1f), color = Palette.drawerLo, small = true) { onAula() }
                    BigButton(tx("cola_fechar"), Modifier.weight(1f), color = Palette.hazard, textColor = Palette.hazardDark, small = true) { onFechar() }
                }
            }
        }
    }
}

@Composable
private fun LinhaDaCola(n: Int, texto: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Palette.paper).padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(26.dp).clip(RoundedCornerShape(13.dp)).background(Palette.hazard), contentAlignment = Alignment.Center) {
            Text("$n", color = Palette.hazardDark, style = MaterialTheme.typography.labelLarge)
        }
        Text(texto.removePrefix("$n. ").removePrefix("$n."), Modifier.weight(1f), color = Palette.ink, style = MaterialTheme.typography.bodyMedium)
    }
}
