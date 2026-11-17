package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.compilandia.fabrica.BlocoTipo
import com.compilandia.fabrica.Blocos
import com.compilandia.fabrica.Categoria
import com.compilandia.tutor.FabGram.Bloco
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A arte de cada tipo de bloco, vista de cima como no Mindustry: chassi de metal com bisel,
 * uma faixa com a cor da categoria e, no meio, o mecanismo que se mexe quando o bloco trabalha.
 * O mesmo desenho serve para o palco e para o ícone do menu de construção.
 *
 * Bloco novo: escreva a função aqui e registre em [Desenhos.porTipo]. O ConteudoFabricaTest cobra.
 * Desenhe só com as formas do pacote (Tracos.kt) e as cores do [Pincel.pk]: assim o bloco sai certo
 * em todo pacote de textura.
 */
internal class Pincel(
    /** Um "pixel" da arte: a célula tem 32. */
    val u: Float,
    val relogio: Float,
    val reduz: Boolean,
    val ativo: Boolean,
    val medidor: TextMeasurer?,
    /** O bloco tem um defeito que o Laboratório apontou (separador emperrado). */
    val alerta: Boolean = false,
    /** O pacote de textura com que se desenha. */
    val pk: Pacote = PacoteAtual.pacote,
    /** Quanto do trabalho já foi feito, de 0 a 1; nulo quando a máquina está parada. A animação de dentro segue isto. */
    val progresso: Float? = null,
    /** Separador: o número da saída por onde a caixa está saindo agora. */
    val saida: Int? = null,
    /** Montadora: a frase até agora, casa por casa (nulo é casa ainda vazia). */
    val frase: List<String?> = emptyList(),
) {
    /** A cor da faixa do tipo, no pacote. */
    fun faixa(t: BlocoTipo): Color = when (t.categoria) {
        Categoria.DISTRIBUICAO -> pk.distribuicao
        Categoria.PRODUCAO -> pk.producao
        Categoria.NUCLEO -> pk.nucleo
    }

    /** O ângulo das peças que giram: parado quando o bloco não trabalha ou o movimento está reduzido. */
    val giro: Float get() = when {
        reduz -> 0f
        ativo -> relogio * GIRO_ATIVO
        else -> relogio * GIRO_PARADO
    }
    /** De 0 a 1 e de volta, para pistões. */
    val vaivem: Float get() = if (ativo && !reduz) ((sin(relogio * VAIVEM) + 1f) / 2f) else 0f
}

private const val GIRO_ATIVO = 9f
private const val GIRO_PARADO = 0.6f
private const val VAIVEM = 10f

internal fun interface Desenho {
    fun DrawScope.pinta(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float)
}

internal object Desenhos {
    val porTipo: Map<String, Desenho> = mapOf(
        Blocos.FONTE.id to Desenho(DrawScope::fonte),
        Blocos.MONTADORA.id to Desenho(DrawScope::montadora),
        Blocos.CESTO.id to Desenho { p, _, x, y, w, h -> cesto(p, x, y, w, h) },
        Blocos.ESTEIRA.id to Desenho { p, _, x, y, w, h -> iconeEsteira(p, x, y, w, h) },
        Blocos.SEPARADOR.id to Desenho(DrawScope::separador),
        Blocos.ROTEADOR.id to Desenho { p, b, x, y, w, h -> roteador(p, b, x, y, w, h) },
        Blocos.JUNCAO.id to Desenho { p, _, x, y, w, h -> juncao(p, x, y, w, h) },
        Blocos.COFRE.id to Desenho(DrawScope::cofre),
        Blocos.PRENSA.id to Desenho(DrawScope::prensa),
        Blocos.BRACO.id to Desenho(DrawScope::braco),
        Blocos.ARMARIO.id to Desenho(DrawScope::armario),
    )

    /** O desenho do tipo; um tipo sem desenho registrado sai como o chassi com a faixa da categoria. */
    fun de(t: BlocoTipo): Desenho = porTipo[t.id] ?: Desenho { p, _, x, y, w, h -> chassi(p, x, y, w, h, p.faixa(t)) }
}

/** A cor da faixa no pacote em uso: distribuição, produção ou núcleo. Para o menu e os cartões. */
internal fun corDaCategoria(t: BlocoTipo): Color = Pincel(1f, 0f, true, false, MEDIDOR_NENHUM).faixa(t)

private val MEDIDOR_NENHUM: TextMeasurer? = null

internal fun DrawScope.ret(x: Float, y: Float, w: Float, h: Float, cor: Color) = drawRect(cor, Offset(x, y), Size(w, h))

private fun px(v: Float, density: Float) = TextUnit(v / density, TextUnitType.Sp)

/** Uma letra ou símbolo centrado em ([cx], [cy]). */
internal fun DrawScope.rotulo(p: Pincel, texto: String, cx: Float, cy: Float, tam: Float, cor: Color) {
    val medidor = p.medidor ?: return
    val est = TextStyle(
        color = cor, fontSize = px(tam, density), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
    )
    val lay = medidor.measure(texto, est)
    drawText(lay, topLeft = Offset(cx - lay.size.width / 2f, cy - lay.size.height / 2f))
}

private fun DrawScope.cesto(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.alerta)
    val cx = x + w / 2; val cy = y + h / 2 - u
    disco(p, cx, cy, 17 * u, BOCA_CESTO)
    disco(p, cx, cy, 14 * u, k.alerta.copy(alpha = 0.25f + 0.15f * ((sin(p.relogio * 2f) + 1f) / 2f)))
    for (i in -2..2) detalhe(p, cx + i * 6 * u - u, cy - 14 * u, 2 * u, 28 * u, k.escuro)
}

private fun DrawScope.iconeEsteira(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    forma(p, x + 2 * u, y + 6 * u, w - 4 * u, h - 12 * u, k.linha)
    forma(p, x + 2 * u, y + 7 * u, w - 4 * u, h - 14 * u, k.esteira)
    for (i in 0 until 3) chevron(p, x + (8 + i * 8) * u, y + h / 2, k.seta)
}

/** Uma seta em V apontando para a direita, centrada em ([cx], [cy]). */
internal fun DrawScope.chevron(p: Pincel, cx: Float, cy: Float, cor: Color) {
    val u = p.u
    linha(p, Offset(cx - 2 * u, cy - 4 * u), Offset(cx + 2 * u, cy), cor, 1.6f * u)
    linha(p, Offset(cx + 2 * u, cy), Offset(cx - 2 * u, cy + 4 * u), cor, 1.6f * u)
}

private fun DrawScope.roteador(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.distribuicao)
    val cel = 32 * u
    for (i in 0 until (w / cel).toInt().coerceAtLeast(1)) {
        val cx = x + i * cel + cel / 2; val cy = y + h / 2 - u
        rotate(45f + p.giro * 4f, Offset(cx, cy)) {
            forma(p, cx - 6 * u, cy - 6 * u, 12 * u, 12 * u, k.linha)
            forma(p, cx - 5 * u, cy - 5 * u, 10 * u, 10 * u, k.distribuicao)
        }
    }
    if (b.w > 1) for (i in 1 until b.w) detalhe(p, x + i * cel - u / 2, y + 6 * u, u, h - 12 * u, k.escuro)
}

private fun DrawScope.juncao(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.distribuicao)
    val cx = x + w / 2; val cy = y + h / 2 - u
    forma(p, cx - 5 * u, y + 5 * u, 10 * u, h - 11 * u, k.esteira)
    forma(p, x + 5 * u, cy - 5 * u, w - 10 * u, 10 * u, k.esteira)
    for (d in 0 until 4) rotate(d * 90f, Offset(cx, cy)) { chevron(p, cx + 10 * u, cy, k.seta) }
}

