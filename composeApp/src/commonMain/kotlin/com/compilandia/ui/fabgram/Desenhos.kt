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
 */
internal class Pincel(
    /** Um "pixel" da arte: a célula tem 32. */
    val u: Float,
    val relogio: Float,
    val reduz: Boolean,
    val ativo: Boolean,
    val medidor: TextMeasurer,
    /** O bloco tem um defeito que o Laboratório apontou (separador emperrado). */
    val alerta: Boolean = false,
) {
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

    fun de(t: BlocoTipo): Desenho =
        porTipo[t.id] ?: Desenho { p, _, x, y, w, h -> chassi(p.u, x, y, w, h, corDaCategoria(t)) }
}

/** A cor da faixa: distribuição âmbar, produção turquesa, núcleo cobre. */
internal fun corDaCategoria(t: BlocoTipo): Color = when (t.categoria) {
    Categoria.DISTRIBUICAO -> AC
    Categoria.PRODUCAO -> TURQUESA
    Categoria.NUCLEO -> COBRE
}


internal fun DrawScope.ret(x: Float, y: Float, w: Float, h: Float, cor: Color) = drawRect(cor, Offset(x, y), Size(w, h))

/**
 * O chassi comum: sombra, contorno, moldura com luz em cima e à esquerda, fundo rebaixado,
 * a faixa da categoria e um parafuso em cada canto. Devolve nada; o miolo é desenhado por cima.
 */
internal fun DrawScope.chassi(u: Float, x: Float, y: Float, w: Float, h: Float, faixa: Color) {
    drawRoundRect(SOMBRA_BLOCO, Offset(x + 3 * u, y + 4 * u), Size(w, h), CornerRadius(4 * u))
    drawRoundRect(OL, Offset(x, y), Size(w, h), CornerRadius(4 * u))
    drawRoundRect(METAL, Offset(x + u, y + u), Size(w - 2 * u, h - 2 * u), CornerRadius(3 * u))
    ret(x + 2 * u, y + u, w - 4 * u, u, METAL_L)
    ret(x + u, y + 2 * u, u, h - 4 * u, METAL_L)
    ret(x + 2 * u, y + h - 2 * u, w - 4 * u, u, METAL_D)
    ret(x + w - 2 * u, y + 2 * u, u, h - 4 * u, METAL_D)
    drawRoundRect(FUNDO_BLOCO, Offset(x + 4 * u, y + 4 * u), Size(w - 8 * u, h - 8 * u), CornerRadius(2 * u))
    ret(x + 4 * u, y + h - 4 * u, w - 8 * u, u, faixa)
    val m = PARAFUSO * u
    for ((px, py) in listOf(x + m to y + m, x + w - m to y + m, x + m to y + h - m, x + w - m to y + h - m)) {
        drawCircle(METAL_D, 1.2f * u, Offset(px, py))
        drawCircle(METAL_L, 0.6f * u, Offset(px - 0.3f * u, py - 0.3f * u))
    }
}

private fun px(v: Float, density: Float) = TextUnit(v / density, TextUnitType.Sp)

/** Uma letra ou símbolo centrado em ([cx], [cy]). */
internal fun DrawScope.rotulo(p: Pincel, texto: String, cx: Float, cy: Float, tam: Float, cor: Color) {
    val est = TextStyle(
        color = cor, fontSize = px(tam, density), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
    )
    val lay = p.medidor.measure(texto, est)
    drawText(lay, topLeft = Offset(cx - lay.size.width / 2f, cy - lay.size.height / 2f))
}

private fun DrawScope.fonte(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, COBRE)
    val cx = x + w / 2; val cy = y + h / 2 - u
    rotate(p.giro * 8f, Offset(cx, cy)) {
        for (k in 0 until 8) {
            val a = k * PI.toFloat() / 4
            drawCircle(COBRE.copy(alpha = 0.7f), 1.6f * u, Offset(cx + cos(a) * 15 * u, cy + sin(a) * 15 * u))
        }
    }
    drawCircle(OL, 12 * u, Offset(cx, cy)); drawCircle(METAL_D, 11 * u, Offset(cx, cy))
    caixa(cx, cy, 16 * u, u)
    rotulo(p, b.nt ?: "S", cx, cy, 10 * u, TINTA_CAIXA)
}

private fun DrawScope.montadora(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, VERDE)
    val n = 3
    val passo = (w - 14 * u) / n
    for (i in 0 until n) {
        val sx = x + 7 * u + i * passo
        drawRoundRect(OL, Offset(sx, y + h / 2 - 9 * u), Size(passo - 3 * u, 16 * u), CornerRadius(2 * u))
        drawRoundRect(METAL_D, Offset(sx + u, y + h / 2 - 8 * u), Size(passo - 5 * u, 14 * u), CornerRadius(1.5f * u))
    }
    // a seta que mostra para onde a frase sai
    for (j in 0 until 4) ret(x + w / 2 - (4 - j) * u, y + 8 * u + j * u, 2 * (4 - j) * u, u, VERDE.copy(alpha = 0.8f))
}

private fun DrawScope.cesto(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, VERMELHO)
    val cx = x + w / 2; val cy = y + h / 2 - u
    drawCircle(BOCA_CESTO, 17 * u, Offset(cx, cy))
    drawCircle(VERMELHO.copy(alpha = 0.25f + 0.15f * ((sin(p.relogio * 2f) + 1f) / 2f)), 14 * u, Offset(cx, cy))
    for (k in -2..2) ret(cx + k * 6 * u - u, cy - 15 * u, 2 * u, 30 * u, METAL_D)
}

private fun DrawScope.iconeEsteira(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    ret(x + 2 * u, y + 6 * u, w - 4 * u, h - 12 * u, OL)
    ret(x + 2 * u, y + 7 * u, w - 4 * u, h - 14 * u, EST)
    ret(x + 2 * u, y + 7 * u, w - 4 * u, u, EST_L)
    ret(x + 2 * u, y + h - 8 * u, w - 4 * u, u, EST_L)
    for (k in 0 until 3) chevron(x + (8 + k * 8) * u, y + h / 2, u, AC)
}

/** Uma seta em V apontando para a direita, centrada em ([cx], [cy]). */
internal fun DrawScope.chevron(cx: Float, cy: Float, u: Float, cor: Color) {
    for (j in 0 until 4) {
        ret(cx - 2 * u + j * u, cy - (4 - j) * u, 1.5f * u, u, cor)
        ret(cx - 2 * u + j * u, cy + (3 - j) * u, 1.5f * u, u, cor)
    }
}

private fun DrawScope.separador(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    val cor = if (p.alerta) VERMELHO else AC
    chassi(u, x, y, w, h, cor)
    val cel = 32 * u
    val linhas = (h / cel).toInt().coerceAtLeast(1)
    for (i in 0 until linhas) {
        val cy = y + i * cel + cel / 2
        if (linhas > 1) chevron(x + w - 9 * u, cy, u, CORES_NUMERO[i % CORES_NUMERO.size])
    }
    val cy = y + h / 2
    val cx = x + w / 2 - if (linhas > 1) 3 * u else 0f
    drawCircle(OL, 9 * u, Offset(cx, cy))
    drawCircle(cor, 8 * u, Offset(cx, cy))
    rotulo(p, b.nt ?: "?", cx, cy, 10 * u, OL)
}

private fun DrawScope.roteador(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, AC)
    val cel = 32 * u
    for (i in 0 until (w / cel).toInt().coerceAtLeast(1)) {
        val cx = x + i * cel + cel / 2; val cy = y + h / 2 - u
        rotate(45f + p.giro * 4f, Offset(cx, cy)) {
            ret(cx - 6 * u, cy - 6 * u, 12 * u, 12 * u, OL)
            ret(cx - 5 * u, cy - 5 * u, 10 * u, 10 * u, AC)
            ret(cx - 2 * u, cy - 2 * u, 4 * u, 4 * u, AC2)
        }
    }
    if (b.w > 1) for (i in 1 until b.w) ret(x + i * cel - u / 2, y + 6 * u, u, h - 12 * u, METAL_D)
}

private fun DrawScope.juncao(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, AC)
    val cx = x + w / 2; val cy = y + h / 2 - u
    ret(cx - 5 * u, y + 5 * u, 10 * u, h - 11 * u, EST)
    ret(x + 5 * u, cy - 5 * u, w - 10 * u, 10 * u, EST)
    drawRect(OL, Offset(cx - 5 * u, cy - 5 * u), Size(10 * u, 10 * u), style = Stroke(u))
    ret(cx - u / 2, y + 6 * u, u, 6 * u, AC); ret(cx - u / 2, y + h - 14 * u, u, 6 * u, AC)
    ret(x + 6 * u, cy - u / 2, 6 * u, u, AC); ret(x + w - 12 * u, cy - u / 2, 6 * u, u, AC)
}

private fun DrawScope.cofre(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, TURQUESA)
    val cx = x + w / 2; val cy = y + h / 2 - u
    drawCircle(OL, 10 * u, Offset(cx, cy))
    drawCircle(METAL_L, 9 * u, Offset(cx, cy))
    drawCircle(METAL, 7 * u, Offset(cx, cy))
    rotate(p.giro * 20f, Offset(cx, cy)) {
        for (k in 0 until 3) {
            val a = k * 2 * PI.toFloat() / 3
            drawLine(TURQUESA, Offset(cx, cy), Offset(cx + cos(a) * 6 * u, cy + sin(a) * 6 * u), 1.6f * u)
        }
    }
    drawCircle(AC2, 1.6f * u, Offset(cx, cy))
}

private fun DrawScope.prensa(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, TURQUESA)
    val cx = x + w / 2; val cy = y + h / 2 - 2 * u
    val curso = p.vaivem * 7 * u
    // dois pistões que fecham no meio
    for (lado in listOf(-1f, 1f)) {
        val px0 = if (lado < 0) x + 6 * u + curso else x + w - 22 * u - curso
        ret(px0, cy - 9 * u, 16 * u, 18 * u, OL)
        ret(px0 + u, cy - 8 * u, 14 * u, 16 * u, METAL_L)
        for (k in 0 until 3) ret(px0 + (3 + k * 4) * u, cy - 6 * u, u, 12 * u, METAL_D)
    }
    drawCircle(OL, 7 * u, Offset(cx, cy)); drawCircle(TURQUESA, 6 * u, Offset(cx, cy))
    rotate(p.giro * 30f, Offset(cx, cy)) {
        ret(cx - u / 2, cy - 5 * u, u, 10 * u, OL.copy(alpha = 0.6f))
        ret(cx - 5 * u, cy - u / 2, 10 * u, u, OL.copy(alpha = 0.6f))
    }
}

private fun DrawScope.braco(p: Pincel, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, TURQUESA)
    val bx = x + w / 2; val by = y + h - 16 * u
    drawCircle(OL, 7 * u, Offset(bx, by)); drawCircle(METAL_L, 6 * u, Offset(bx, by))
    val ang = -90f + if (p.ativo && !p.reduz) sin(p.relogio * 6f) * 35f else 0f
    rotate(ang, Offset(bx, by)) {
        drawRoundRect(OL, Offset(bx - u, by - 3 * u), Size(30 * u, 6 * u), CornerRadius(3 * u))
        drawRoundRect(TURQUESA, Offset(bx, by - 2 * u), Size(28 * u, 4 * u), CornerRadius(2 * u))
        ret(bx + 26 * u, by - 5 * u, 2 * u, 10 * u, METAL_L)
    }
    drawCircle(AC2, 2 * u, Offset(bx, by))
}

private fun DrawScope.armario(p: Pincel, b: Bloco, x: Float, y: Float, w: Float, h: Float) {
    val u = p.u
    chassi(u, x, y, w, h, TURQUESA)
    drawRoundRect(OL, Offset(x + 7 * u, y + 7 * u), Size(w - 14 * u, h - 24 * u), CornerRadius(2 * u))
    drawRoundRect(TELA, Offset(x + 8 * u, y + 8 * u), Size(w - 16 * u, h - 26 * u), CornerRadius(1.5f * u))
    val linhas = 4
    for (k in 0 until linhas) {
        val acesa = p.ativo && !p.reduz && ((p.relogio * 6).toInt() + k) % linhas == 0
        val largura = (w - 22 * u) * (0.4f + 0.15f * ((k + b.id) % linhas))
        ret(x + 11 * u, y + (12 + k * 7) * u, largura, 2 * u, TELA_L.copy(alpha = if (acesa) 1f else 0.55f))
    }
    val luzes = listOf(AC, VERMELHO, VERDE)
    for (k in 0 until 6) drawCircle(luzes[k % luzes.size], 1.5f * u, Offset(x + (10 + k * 8) * u, y + h - 12 * u))
}
