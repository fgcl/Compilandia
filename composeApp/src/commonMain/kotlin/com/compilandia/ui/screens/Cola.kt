package com.compilandia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.compilandia.content.Camera
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
import com.compilandia.ui.motion.STAGE_W
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
        val pecas = ultimo.boxes.filter { it.id != TITULO_ID && it.text.trim() !in regras }
        // Sobrou só a gramática, que a cola já mostra em letra grande: sem caixa vazia.
        if (pecas.isEmpty()) return null
        return ultimo.copy(boxes = pecas, actor = null)
    }

    /** O retângulo que as peças ocupam no palco: esquerda, topo, direita, base. */
    fun moldura(shot: Shot): FloatArray {
        val m = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
        for (b in shot.boxes) {
            val cx = if (b.look == Look.CODE) b.x + b.w / 2 else b.x
            m[0] = minOf(m[0], cx - b.w / 2); m[1] = minOf(m[1], b.y - b.h / 2)
            m[2] = maxOf(m[2], cx + b.w / 2); m[3] = maxOf(m[3], b.y + b.h / 2)
        }
        // Os fios também ocupam lugar: o laço sobe acima do estado, e o fio curvo sai da reta.
        val porId = shot.boxes.associateBy { it.id }
        fun ponto(x: Float, y: Float, r: Float) { m[0] = minOf(m[0], x - r); m[1] = minOf(m[1], y - r); m[2] = maxOf(m[2], x + r); m[3] = maxOf(m[3], y + r) }
        for (w in shot.wires) {
            val a = porId[w.from] ?: continue
            val b = porId[w.to] ?: continue
            if (w.from == w.to) { ponto(a.x, a.y - a.h / 2 - 30f, 12f); continue }
            val dx = b.x - a.x; val dy = b.y - a.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            ponto((a.x + b.x) / 2 - dy / len * w.bend / 2, (a.y + b.y) / 2 + dx / len * w.bend / 2, 10f)
        }
        return m
    }

    /**
     * A câmera olha só para onde há desenho (mural, rodada 16): sem a metade vazia do palco,
     * e o exemplo cresce até encher a caixa de altura [stageH].
     */
    fun enquadra(shot: Shot, stageH: Float): Shot {
        if (shot.boxes.isEmpty()) return shot
        val m = moldura(shot)
        val folga = 14f
        val w = m[2] - m[0] + 2 * folga
        val h = m[3] - m[1] + 2 * folga
        val zoom = minOf(STAGE_W / w, stageH / h, 2.4f)
        return shot.copy(camera = Camera((m[0] + m[2]) / 2, (m[1] + m[3]) / 2, zoom))
    }

    /** Altura da caixa do exemplo, em proporção à largura: a do desenho, nem baixa nem alta demais. */
    fun proporcao(shot: Shot): Float {
        if (shot.boxes.isEmpty()) return 0.55f
        val m = moldura(shot)
        return ((m[3] - m[1] + 28f) / (m[2] - m[0] + 28f)).coerceIn(0.38f, 0.7f)
    }
}

/**
 * O cartão da cola por cima da tela. [onAula] abre a aula animada; nulo esconde o botão.
 * [onPraticar] leva a um jogo do assunto (mural, rodada 15): só a trilha passa, dentro do jogo fica nulo.
 */
@Composable
fun ColaDialog(s: Skill, onFechar: () -> Unit, onAula: (() -> Unit)?, onPraticar: (() -> Unit)? = null) {
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
                    val prop = remember(exemplo) { Cola.proporcao(exemplo) }
                    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f / prop).clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).testTag("cola-exemplo")) {
                        val stageH = STAGE_W * maxHeight.value / maxWidth.value
                        val enquadrado = remember(exemplo, stageH) { Cola.enquadra(exemplo, stageH) }
                        MotionStage(enquadrado, Modifier.fillMaxSize(), stageH = stageH, center = false)
                    }
                }
                if (onPraticar != null) BigButton(tx("cola_praticar"), Modifier.fillMaxWidth().testTag("cola-praticar"), color = Palette.action) { onPraticar() }
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
