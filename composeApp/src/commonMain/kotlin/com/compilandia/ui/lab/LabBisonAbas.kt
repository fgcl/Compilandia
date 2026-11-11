package com.compilandia.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.content.linhasDoEstado
import com.compilandia.engine.LRAction
import com.compilandia.engine.MiniBison
import com.compilandia.engine.isComplete
import com.compilandia.engine.nextSymbol
import com.compilandia.engine.render
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.textos.tx
import com.compilandia.ui.BigButton
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette

/**
 * A pilha do parser numa fila só, como a esteira do jogo: o que já foi lido à esquerda,
 * com o estado em cima e o valor embaixo de cada símbolo, e o que falta ler à direita, apagado.
 * Embaixo, a ação do passo numa etiqueta: empilhar ou reduzir, com a conta.
 */
@Composable
internal fun PilhaPasso(quadros: List<MiniBison.Quadro>, passo: Int, onPasso: (Int) -> Unit) {
    val q = quadros.getOrNull(passo)
    if (q == null) {
        Text(tx("labbisonscreen_entrada_nao_virou_tokens_use"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
        return
    }
    val mono = LocalFonts.current.mono
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper).padding(10.dp).testTag("lab-bison-pilhas"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val simbolos = listOf("$") + q.simbolos
            val valores = listOf<Long?>(null) + q.valores
            simbolos.forEachIndexed { k, s ->
                Caixa(s, q.estados.getOrNull(k)?.toString(), valores.getOrNull(k)?.toString(), if (k == simbolos.lastIndex) Palette.hazard else Palette.drawer, apagada = false)
            }
            Box(Modifier.width(2.dp).height(40.dp).background(Palette.inkSoft.copy(alpha = 0.5f)))
            q.entrada.forEachIndexed { k, t -> Caixa(t, null, null, if (k == 0) Palette.teal else Palette.paperDark, apagada = k > 0) }
        }
        val (rotulo, cor) = when (val a = q.acao) {
            is LRAction.Shift -> tx("labbison_empilhar") to Palette.teal
            is LRAction.Reduce -> tx("labbison_reduzir") to Palette.orange
            LRAction.Accept -> tx("labbison_aceita") to Palette.green
            null -> tx("labbison_erro") to Palette.red
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(cor).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(rotulo, color = Palette.on(cor), fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            val detalhe = when (val a = q.acao) {
                is LRAction.Shift -> q.entrada.firstOrNull() ?: ""
                is LRAction.Reduce -> q.conta ?: ""
                LRAction.Accept -> "${q.valores.lastOrNull() ?: ""}"
                null -> q.entrada.firstOrNull() ?: ""
            }
            Text(detalhe, Modifier.weight(1f), fontFamily = mono, fontSize = 14.sp, color = Palette.ink, fontWeight = FontWeight.Bold, maxLines = 2)
            Text("${passo + 1}/${quadros.size}", color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BigButton(tx("comum_voltar"), Modifier.weight(1f), color = Palette.drawer, small = true, enabled = passo > 0) { onPasso(passo - 1); Sound.play(Sfx.TICK) }
            BigButton(tx("comum_proximo"), Modifier.weight(2f), enabled = passo < quadros.lastIndex) { onPasso(passo + 1); Sound.play(Sfx.DROP) }
            BigButton(tx("labbisonscreen_ate_fim"), Modifier.weight(1f), color = Palette.drawer, small = true, enabled = passo < quadros.lastIndex) { onPasso(quadros.lastIndex) }
        }
    }
}

/** Uma caixa da fila: o estado pequeno em cima, o símbolo no meio, o valor embaixo. */
@Composable
private fun Caixa(simbolo: String, estado: String?, valor: String?, cor: Color, apagada: Boolean) {
    val mono = LocalFonts.current.mono
    Column(Modifier.alpha(if (apagada) 0.6f else 1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(estado ?: "", color = Palette.inkSoft, fontFamily = mono, fontSize = 10.sp)
        Box(Modifier.widthIn(min = 34.dp).clip(RoundedCornerShape(5.dp)).background(cor).padding(horizontal = 7.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(simbolo.removeSurrounding("'"), color = Palette.on(cor), fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text(valor ?: "", color = Palette.orange, fontFamily = mono, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Os conflitos. Os que sobraram aparecem abertos, em vermelho, como um duelo entre empilhar e reduzir.
 * Os que a precedência resolveu ficam numa linha cada, com a ação que venceu. Um toque abre o duelo.
 */
@Composable
internal fun Conflitos(bison: MiniBison) {
    val t = bison.table
    val celulas = t.rawConflictCells
    if (celulas.isEmpty()) {
        Text(tx("labbisonscreen_nenhuma_celula_tabela_tem_duas"), color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
        return
    }
    val sobra = celulas.filter { (s, a) -> bison.conflicts.any { it.state == s && it.terminal == a } }
    for (c in sobra) Duelo(bison, c.first, c.second, sobrou = true)
    val resolvidas = celulas - sobra.toSet()
    if (resolvidas.isNotEmpty()) {
        Text(tx("labbisonscreen_precedencia_decidiu_1_celula_2", resolvidas.size, if (resolvidas.size > 1) "s" else ""), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper)) {
            resolvidas.forEachIndexed { k, (s, a) ->
                var aberto by remember(s, a) { mutableStateOf(false) }
                if (k > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.drawerLo))
                Row(
                    Modifier.fillMaxWidth().clickable { aberto = !aberto }.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(tx("labbisonscreen_estado_1_chega_2", s, a), Modifier.weight(1f), color = Palette.ink, style = MaterialTheme.typography.bodyMedium)
                    val venceu = t.action[s to a]
                    val (r, cor) = if (venceu is LRAction.Shift) tx("labbison_empilhar") to Palette.teal else tx("labbison_reduzir") to Palette.orange
                    Box(Modifier.clip(RoundedCornerShape(5.dp)).background(cor).padding(horizontal = 7.dp, vertical = 3.dp)) {
                        Text(r, color = Palette.on(cor), fontFamily = LocalFonts.current.mono, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (aberto) Box(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) { Duelo(bison, s, a, sobrou = false) }
            }
        }
    }
}

/** Uma célula com duas ações: o item que empilha contra o item que reduz, e quem venceu. */
@Composable
private fun Duelo(bison: MiniBison, s: Int, a: String, sobrou: Boolean) {
    val t = bison.table
    val g = t.grammar
    val st = t.automaton.states[s]
    val empilha = st.items.filter { it.nextSymbol(g) == a }.map { it.core().render(g) }.distinct()
    val reduz = st.items.filter { it.isComplete(g) && (it.lookahead == null || it.lookahead == a) }.map { it.core().render(g) }.distinct()
    val decisao = t.resolutions[s to a]
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper)
            .border(2.dp, if (sobrou) Palette.red else Palette.green, RoundedCornerShape(8.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sobrou) Text(tx("labbisonscreen_estado_1_chega_2", s, a), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (empilha.isNotEmpty()) RegistroCard(tx("labbison_empilhar"), empilha, Palette.teal, Modifier.weight(1f))
            for (r in reduz) RegistroCard(tx("labbison_reduzir"), listOf(r), Palette.orange, Modifier.weight(1f))
        }
        Text(
            if (sobrou) tx("labbisonscreen_sobrou_1", decisao ?: tx("labbisonscreen_bison_escolhe_avisa")) else tx("labbisonscreen_resolvido_1", decisao ?: ""),
            color = if (sobrou) Palette.red else Palette.green, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Os estados do relatório do bison -v. Ficam à vista só os que estão na pilha no passo atual,
 * com o topo em amarelo; o resto abre num toque.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Estados(bison: MiniBison, q: MiniBison.Quadro?) {
    val t = bison.table
    val topo = q?.estados?.lastOrNull()
    val naPilha = q?.estados.orEmpty().toSet()
    var todos by remember { mutableStateOf(false) }
    val mostra = t.automaton.states.indices.filter { todos || it in naPilha || naPilha.isEmpty() }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (s in mostra) {
            val cor = when (s) { topo -> Palette.hazard; in naPilha -> Palette.teal; else -> Palette.drawer }
            RegistroCard(tx("comum_estado_1", s), linhasDoEstado(t, s), cor, Modifier.widthIn(min = 150.dp, max = 330.dp))
        }
    }
    if (mostra.size < t.automaton.states.size || todos) {
        EtiquetaBison(if (todos) tx("labbison_so_da_pilha") else tx("labbison_ver_todos", t.automaton.states.size), false) { todos = !todos; Sound.play(Sfx.CLICK) }
    }
}

/** A caixa com compartimentos, em Compose: o nome numa faixa e uma linha por item. */
@Composable
fun RegistroCard(nome: String, linhas: List<String>, cor: Color, modifier: Modifier = Modifier) {
    val mono = LocalFonts.current.mono
    Column(modifier.clip(RoundedCornerShape(6.dp)).background(Palette.paperDark).border(1.5.dp, cor, RoundedCornerShape(6.dp))) {
        Box(Modifier.fillMaxWidth().background(cor).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(nome, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(cor))
        }
        for ((i, l) in linhas.withIndex()) {
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.drawerLo))
            Text(l, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontFamily = mono, fontSize = 12.sp, color = Palette.ink)
        }
    }
}
