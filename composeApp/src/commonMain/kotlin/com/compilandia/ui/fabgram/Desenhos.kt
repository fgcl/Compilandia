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
private const val PARAFUSO = 2.5f

internal fun interface Desenho {
    fun DrawScope.pinta(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float)
}

internal object Desenhos {
    val porTipo: Map<String, Desenho> = mapOf(
        Blocos.FONTE.id to Desenho { p, b, x, y, w, h -> fonte(p, b, x, y, w, h) },
        Blocos.MONTADORA.id to Desenho { p, _, x, y, w, h -> montadora(p, x, y, w, h) },
        Blocos.CESTO.id to Desenho { p, _, x, y, w, h -> cesto(p, x, y, w, h) },
        Blocos.ESTEIRA.id to Desenho { p, _, x, y, w, h -> iconeEsteira(p, x, y, w, h) },
        Blocos.SEPARADOR.id to Desenho { p, b, x, y, w, h -> separador(p, b, x, y, w, h) },
        Blocos.ROTEADOR.id to Desenho { p, b, x, y, w, h -> roteador(p, b, x, y, w, h) },
        Blocos.JUNCAO.id to Desenho { p, _, x, y, w, h -> juncao(p, x, y, w, h) },
        Blocos.COFRE.id to Desenho { p, _, x, y, w, h -> cofre(p, x, y, w, h) },
        Blocos.PRENSA.id to Desenho { p, _, x, y, w, h -> prensa(p, x, y, w, h) },
        Blocos.BRACO.id to Desenho { p, _, x, y, w, h -> braco(p, x, y, w, h) },
        Blocos.ARMARIO.id to Desenho { p, b, x, y, w, h -> armario(p, b, x, y, w, h) },
    )

    /** O desenho do tipo: a variante escolhida (Variantes.kt), ou o desenho original. */
    fun de(t: BlocoTipo): Desenho = Variantes.escolhida(t.id)?.desenho
        ?: porTipo[t.id] ?: Desenho { p, _, x, y, w, h -> chassi(p, x, y, w, h, p.faixa(t)) }
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

private fun DrawScope.fonte(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.nucleo)
    val cx = x + w / 2; val cy = y + h / 2 - u
    rotate(p.giro * 8f, Offset(cx, cy)) {
        for (i in 0 until 8) {
            val a = i * PI.toFloat() / 4
            disco(p, cx + cos(a) * 15 * u, cy + sin(a) * 15 * u, 1.6f * u, k.nucleo)
        }
    }
    disco(p, cx, cy, 12 * u, k.linha); disco(p, cx, cy, 11 * u, k.escuro)
    caixaP(p, cx, cy, 16 * u)
    rotulo(p, b.nt ?: "S", cx, cy, 10 * u, if (k.contorno) k.linha else TINTA_CAIXA)
}

private fun DrawScope.montadora(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.ok)
    val n = 3
    val passo = (w - 14 * u) / n
    for (i in 0 until n) forma(p, x + 7 * u + i * passo, y + h / 2 - 9 * u, passo - 3 * u, 16 * u, k.escuro)
    // a seta que mostra para onde a frase sai
    for (j in 0 until 4) detalhe(p, x + w / 2 - (4 - j) * u, y + 8 * u + j * u, 2 * (4 - j) * u, u, k.ok)
}

private fun DrawScope.cesto(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.alerta)
    val cx = x + w / 2; val cy = y + h / 2 - u
    disco(p, cx, cy, 17 * u, if (k.contorno) k.alerta else BOCA_CESTO)
    disco(p, cx, cy, 14 * u, k.alerta.copy(alpha = 0.25f + 0.15f * ((sin(p.relogio * 2f) + 1f) / 2f)))
    for (i in -2..2) detalhe(p, cx + i * 6 * u - u, cy - 14 * u, 2 * u, 28 * u, if (k.contorno) k.alerta else k.escuro)
}

private fun DrawScope.iconeEsteira(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    forma(p, x + 2 * u, y + 6 * u, w - 4 * u, h - 12 * u, if (k.contorno) k.esteiraBorda else k.linha)
    if (!k.contorno) forma(p, x + 2 * u, y + 7 * u, w - 4 * u, h - 14 * u, k.esteira)
    for (i in 0 until 3) chevron(p, x + (8 + i * 8) * u, y + h / 2, k.seta)
}

/** Uma seta em V apontando para a direita, centrada em ([cx], [cy]). */
internal fun DrawScope.chevron(p: Pincel, cx: Float, cy: Float, cor: Color) {
    val u = p.u
    if (p.pk.pixel) {
        // no pixel a seta é um triângulo cheio: em V, ela vira pontinhos soltos
        for (j in 0 until 3) detalhe(p, cx - 2 * u + j * 2 * u, cy - (3 - j) * 2 * u, 2 * u, 2 * (3 - j) * 2 * u, cor)
        return
    }
    linha(p, Offset(cx - 2 * u, cy - 4 * u), Offset(cx + 2 * u, cy), cor, 1.6f * u)
    linha(p, Offset(cx + 2 * u, cy), Offset(cx - 2 * u, cy + 4 * u), cor, 1.6f * u)
}

private fun DrawScope.separador(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    val cor = if (p.alerta) k.alerta else k.distribuicao
    chassi(p, x, y, w, h, cor)
    val cel = 32 * u
    val linhas = (h / cel).toInt().coerceAtLeast(1)
    if (linhas > 1) for (i in 0 until linhas) {
        chevron(p, x + w - 8 * u, y + i * cel + cel / 2, CORES_NUMERO[i % CORES_NUMERO.size])
    }
    val cy = y + h / 2
    val cx = x + w / 2 - if (linhas > 1) 3 * u else 0f
    disco(p, cx, cy, 9 * u, k.linha)
    disco(p, cx, cy, 8 * u, cor)
    rotulo(p, b.nt ?: "?", cx, cy, 10 * u, p.tintaSobre())
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
    forma(p, cx - 5 * u, y + 5 * u, 10 * u, h - 11 * u, if (k.contorno) k.esteiraBorda else k.esteira)
    forma(p, x + 5 * u, cy - 5 * u, w - 10 * u, 10 * u, if (k.contorno) k.esteiraBorda else k.esteira)
    for (d in 0 until 4) rotate(d * 90f, Offset(cx, cy)) { chevron(p, cx + 10 * u, cy, k.seta) }
}

private fun DrawScope.cofre(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.producao)
    val cx = x + w / 2; val cy = y + h / 2 - u
    disco(p, cx, cy, 10 * u, k.linha); disco(p, cx, cy, 9 * u, k.claro); disco(p, cx, cy, 7 * u, k.corpo)
    rotate(p.giro * 20f, Offset(cx, cy)) {
        for (i in 0 until 3) {
            val a = i * 2 * PI.toFloat() / 3
            linha(p, Offset(cx, cy), Offset(cx + cos(a) * 6 * u, cy + sin(a) * 6 * u), k.producao, 1.6f * u)
        }
    }
    disco(p, cx, cy, 1.6f * u, k.distribuicao)
}

private fun DrawScope.prensa(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.producao)
    val cx = x + w / 2; val cy = y + h / 2 - 2 * u
    val curso = p.vaivem * 7 * u
    // dois pistões que fecham no meio
    for (lado in listOf(-1f, 1f)) {
        val px0 = if (lado < 0) x + 6 * u + curso else x + w - 22 * u - curso
        forma(p, px0, cy - 9 * u, 16 * u, 18 * u, k.linha)
        forma(p, px0 + u, cy - 8 * u, 14 * u, 16 * u, k.claro)
        for (i in 0 until 3) detalhe(p, px0 + (3 + i * 4) * u, cy - 6 * u, u, 12 * u, k.escuro)
    }
    disco(p, cx, cy, 7 * u, k.linha); disco(p, cx, cy, 6 * u, k.producao)
    rotate(p.giro * 30f, Offset(cx, cy)) {
        linha(p, Offset(cx, cy - 4 * u), Offset(cx, cy + 4 * u), k.linha, u)
        linha(p, Offset(cx - 4 * u, cy), Offset(cx + 4 * u, cy), k.linha, u)
    }
}

private fun DrawScope.braco(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.producao)
    val bx = x + w / 2; val by = y + h - 16 * u
    disco(p, bx, by, 7 * u, k.linha); disco(p, bx, by, 6 * u, k.claro)
    val ang = -90f + if (p.ativo && !p.reduz) sin(p.relogio * 6f) * 35f else 0f
    rotate(ang, Offset(bx, by)) {
        linha(p, Offset(bx, by), Offset(bx + 27 * u, by), k.linha, 6 * u)
        linha(p, Offset(bx, by), Offset(bx + 27 * u, by), k.producao, 4 * u)
        linha(p, Offset(bx + 27 * u, by - 5 * u), Offset(bx + 27 * u, by + 5 * u), k.claro, 2 * u)
    }
    disco(p, bx, by, 2 * u, k.distribuicao)
}

private fun DrawScope.armario(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u; val k = p.pk
    chassi(p, x, y, w, h, k.producao)
    forma(p, x + 7 * u, y + 7 * u, w - 14 * u, h - 24 * u, k.linha)
    forma(p, x + 8 * u, y + 8 * u, w - 16 * u, h - 26 * u, k.tela)
    val linhas = 4
    for (i in 0 until linhas) {
        val acesa = p.ativo && !p.reduz && ((p.relogio * 6).toInt() + i) % linhas == 0
        val largura = (w - 22 * u) * (0.4f + 0.15f * ((i + b.id) % linhas))
        detalhe(p, x + 11 * u, y + (12 + i * 7) * u, largura, 2 * u, k.telaLuz.copy(alpha = if (acesa) 1f else 0.55f))
    }
    val luzes = listOf(k.distribuicao, k.alerta, k.ok)
    for (i in 0 until 6) disco(p, x + (10 + i * 8) * u, y + h - 12 * u, 1.5f * u, luzes[i % luzes.size])
}
