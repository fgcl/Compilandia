package com.compilandia.ui.bancada

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.engine.EOF
import com.compilandia.engine.Guindaste
import com.compilandia.engine.Veredito
import com.compilandia.textos.tx
import com.compilandia.ui.ChipRow
import com.compilandia.ui.GrammarText
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood

/** O estado de uma fase do Guindaste (J5): o programa de regras, na ordem, e a corrida no palco. */
class GuindasteEstado(val fase: Guindaste.Fase) : EstadoBancada(fase.pedido) {
    val programa = mutableStateListOf<Guindaste.Regra>()
    var frases by mutableStateOf(fase.testes.map { com.compilandia.engine.tokensOf(it) })
    var corrida by mutableStateOf<Guindaste.Corrida?>(null)

    init { linha.carrega(1, tocar = false) }

    fun poe(carta: Int) {
        if (programa.size >= Guindaste.REGRAS_MAX) return
        programa += Guindaste.Regra(carta)
        mudou()
    }

    fun tira(i: Int) { programa.removeAt(i); mudou() }

    fun sobe(i: Int) {
        if (i <= 0) return
        val t = programa[i - 1]
        programa[i - 1] = programa[i]
        programa[i] = t
        mudou()
    }

    /** Liga ou desliga um símbolo da esteira na espera da regra [i]. Todos ligados é o mesmo que não esperar. */
    fun alternaOlha(i: Int, s: String) {
        val r = programa[i]
        val atual = r.olha ?: fase.colunas.toSet()
        val novo = if (s in atual) atual - s else atual + s
        programa[i] = r.copy(olha = if (novo.size == fase.colunas.size) null else novo)
        mudou()
    }

    private fun mudou() { corrida = null; mexeu() }

    fun limpa() { programa.clear(); mudou(); frase = fase.pedido }

    fun rodar(tocar: Boolean = true): Veredito {
        val p = programa.toList()
        frases = Guindaste.frases(fase, p).map { it.first }
        val v = Guindaste.confere(fase, p)
        aplica(v)
        if (v.falha != -1) mostra(v.falha ?: 0, tocar)
        return v
    }

    fun mostra(i: Int, tocar: Boolean = true) {
        noPalco = i
        val c = Guindaste.corre(fase, programa.toList(), frases[i])
        corrida = c
        linha.carrega(c.quadros.size, tocar && !ThemeState.reduceMotion)
        if (ThemeState.reduceMotion) linha.atual = c.quadros.size - 1
    }

    fun pedeDica() = dica(fase.dicas) { Guindaste.ondeOlhar(fase, programa.toList()) ?: fase.dicas.last() }
}

@Composable
fun GuindasteJogo(e: GuindasteEstado, ultima: Boolean, onPassou: () -> Unit, onSeguir: () -> Unit) {
    TocaLinha(e.linha)
    val c = e.corrida
    val q = c?.quadros?.getOrElse(e.linha.atual) { c.quadros.last() }
    Arranjo(
        palco = {
            PalcoGuindaste(e, c, q)
            ControlesPalco(e.linha, q?.nota ?: tx("guindaste_toque_cartas"))
            FalaDoByte(e.frase, e.humor)
            if (e.veredito?.passou == true) BotaoSeguir(ultima, onSeguir)
        },
        painel = {
            Text(tx("robopilha_gramatica"), style = MaterialTheme.typography.titleSmall)
            GrammarText(e.fase.g)
            Text(tx("guindaste_cartas"), style = MaterialTheme.typography.titleSmall)
            ChipRow {
                e.fase.cartas.forEachIndexed { i, p -> CartaRegra(p.toString()) { e.poe(i) } }
            }
            Text(tx("guindaste_programa"), style = MaterialTheme.typography.titleSmall)
            ProgramaGuindaste(e, q?.regra)
            BotoesBancada(
                onRodar = {
                    val v = e.rodar()
                    if (v.passou) comemora(onPassou) else if (v.falha != -1) lamenta()
                },
                onDica = { e.pedeDica() },
                onLimpar = { e.limpa() },
            )
            ListaTestes(e.veredito, e.noPalco) { e.mostra(it) }
        },
    )
}

@Composable
private fun CartaRegra(texto: String, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(Palette.codeBg).border(2.dp, Palette.hazard, RoundedCornerShape(8.dp))
            .clickable { com.compilandia.platform.Sound.play(com.compilandia.platform.Sfx.CLICK); onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) { Text(texto, color = Palette.onAccent, fontFamily = LocalFonts.current.mono, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
}

/** O programa: as regras na ordem em que o guindaste tenta. A que acabou de trocar fica acesa. */
@Composable
private fun ProgramaGuindaste(e: GuindasteEstado, acesa: Int?) {
    val mono = LocalFonts.current.mono
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (e.programa.isEmpty()) Text(tx("guindaste_toque_cartas"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(6.dp))
        e.programa.forEachIndexed { i, r ->
            val viva = i == acesa
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (viva) Palette.hazard.copy(alpha = 0.28f) else Color.Transparent).padding(horizontal = 4.dp, vertical = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", fontFamily = mono, fontSize = 12.sp, color = Palette.onAccent.copy(alpha = 0.5f), modifier = Modifier.width(22.dp))
                    Text(
                        e.fase.cartas[r.carta].toString(), fontFamily = mono, fontSize = 14.sp, modifier = Modifier.weight(1f),
                        color = if (viva) Palette.yellow else Palette.onAccent, fontWeight = if (viva) FontWeight.Bold else FontWeight.Normal,
                    )
                    if (i > 0) BotaoMini("↑", tx("robomips_subir")) { e.sobe(i) }
                    BotaoMini("×", tx("robomips_tirar")) { e.tira(i) }
                }
                if (e.fase.comOlhar) {
                    Row(Modifier.padding(start = 22.dp, bottom = 4.dp).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tx("guindaste_antes_de"), color = Palette.onAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        for (s in e.fase.colunas) {
                            val liga = r.olha == null || s in r.olha
                            Box(
                                Modifier.size(36.dp, 30.dp).clip(RoundedCornerShape(6.dp)).background(if (liga) Palette.teal else Color.White.copy(alpha = 0.08f))
                                    .clickable { e.alternaOlha(i, s) }.semantics { contentDescription = tx("guindaste_espera_simbolo", s) },
                                contentAlignment = Alignment.Center,
                            ) { Text(s, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (liga) Palette.on(Palette.teal) else Palette.onAccent.copy(alpha = 0.4f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoMini(t: String, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).clickable { onClick() }.semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) { Text(t, color = Palette.onAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

private val COR_CAIXA = Color(0xFFC98B3A)
private val COR_CAIXA_NT = Color(0xFFF7E3C4)
private val COR_ESTEIRA = Color(0xFF4A4F55)

/**
 * O palco: a pilha de caixas à esquerda, a esteira com o resto da frase à direita e o guindaste em cima.
 * Ao trocar, o guindaste ergue as caixas do topo (o handle) e larga uma caixa com o lado esquerdo.
 */
@Composable
private fun PalcoGuindaste(e: GuindasteEstado, c: Guindaste.Corrida?, q: Guindaste.Quadro?) {
    val mono = LocalFonts.current.mono
    val g = e.fase.g
    BoxWithConstraints(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).background(Palette.codeBg)) {
        val w = maxWidth
        val rotulo = Palette.onAccent.copy(alpha = 0.7f)
        Text(tx("robopilha_pilha"), color = rotulo, style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(10.dp, 226.dp))
        Text(tx("guindaste_esteira"), color = rotulo, style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(w * 0.62f, 226.dp))
        // O trilho do guindaste.
        Box(Modifier.offset(8.dp, 14.dp).width(w - 16.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Palette.hazard))
        val pilha = q?.pilha ?: emptyList()
        val mao = q?.mao ?: 0
        val troca = q?.acao == Guindaste.Acao.TROCA
        // A pilha cresce para a direita; as caixas do handle aparecem erguidas antes de virar o lado esquerdo.
        Row(Modifier.offset(10.dp, 170.dp).widthIn(max = w * 0.58f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
            pilha.forEachIndexed { k, s ->
                val topo = k == pilha.lastIndex
                Caixote(s, g.isNonTerminal(s), destaque = topo && troca, mono)
            }
        }
        // A esteira: o que ainda não saiu, com o próximo aceso.
        val resto = c?.let { (it.frase + EOF).drop(q?.lidos ?: 0) } ?: emptyList()
        Box(Modifier.offset(w * 0.62f, 214.dp).width(w * 0.36f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(COR_ESTEIRA))
        Row(Modifier.offset(w * 0.62f, 170.dp).widthIn(max = w * 0.36f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            resto.forEachIndexed { k, s ->
                Box(
                    Modifier.heightIn(min = 40.dp).widthIn(min = 30.dp).clip(RoundedCornerShape(5.dp)).background(if (k == 0) Palette.yellow else COR_CAIXA.copy(alpha = 0.6f)).padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(s, fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)) }
            }
        }
        // O guindaste anda até o topo da pilha. Na troca, mostra o que ergueu: "id → T".
        val alvoX = (10.dp + 44.dp * (pilha.size - 1).coerceAtLeast(0)).coerceAtMost(w * 0.5f)
        val x by animateDpAsState(if (q?.acao == Guindaste.Acao.EMPILHA) w * 0.62f else alvoX, tween(if (ThemeState.reduceMotion) 0 else (e.linha.passoMs() * 0.6).toInt()), label = "guindasteX")
        Box(Modifier.offset(x + 18.dp, 20.dp).width(3.dp).height(if (troca) 150.dp else 46.dp).background(Palette.onAccent.copy(alpha = 0.6f)))
        val humor = when (q?.acao) { Guindaste.Acao.ACEITA -> Mood.HAPPY; Guindaste.Acao.TRAVA, Guindaste.Acao.LONGA -> Mood.SAD; else -> Mood.NEUTRAL }
        Box(Modifier.offset(x, 22.dp).width(40.dp)) { ByteFigure(Modifier.width(40.dp), mood = humor, animate = !ThemeState.reduceMotion) }
        val usada = if (troca && q?.regra != null) e.programa.getOrNull(q.regra)?.let { e.fase.cartas[it.carta] } else null
        if (usada != null) {
            val p = usada
            Box(Modifier.offset(x + 30.dp, 110.dp).clip(RoundedCornerShape(8.dp)).background(Palette.yellow).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("${p.rhsText()}  →  ${p.lhs}", fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(Palette.yellow))
            }
        } else if (mao == 0 && q?.acao == Guindaste.Acao.TRAVA) {
            Box(Modifier.offset(x + 30.dp, 76.dp).clip(RoundedCornerShape(8.dp)).background(Palette.red).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(tx("guindaste_travou"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(Palette.red))
            }
        }
    }
}

@Composable
private fun Caixote(s: String, nt: Boolean, destaque: Boolean, mono: androidx.compose.ui.text.font.FontFamily) {
    val cor = if (nt) COR_CAIXA_NT else COR_CAIXA
    Box(
        Modifier.heightIn(min = 40.dp).widthIn(min = 40.dp).clip(RoundedCornerShape(5.dp)).background(cor)
            .border(if (destaque) 3.dp else 0.dp, if (destaque) Palette.yellow else cor, RoundedCornerShape(5.dp)).padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) { Text(s, fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)) }
}
