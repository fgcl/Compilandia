package com.compilandia.ui.fabgram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

/** As formas básicas que os desenhos das máquinas usam, com o canto e as cores do [Pacote]. */

/** Retângulo de canto redondo, no raio do pacote. */
internal fun DrawScope.forma(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) =
    drawRoundRect(cor, Offset(x, y), Size(w, h), CornerRadius(p.pk.raio * p.u))

/** Retângulo reto: detalhe pequeno (grade de pistão, faixa, junta). */
internal fun DrawScope.detalhe(p: Pincel, x: Float, y: Float, w: Float, h: Float, cor: Color) =
    drawRect(cor, Offset(x, y), Size(w, h))

internal fun DrawScope.disco(p: Pincel, cx: Float, cy: Float, r: Float, cor: Color) = drawCircle(cor, r, Offset(cx, cy))

internal fun DrawScope.linha(p: Pincel, a: Offset, b: Offset, cor: Color, esp: Float) =
    drawLine(cor, a, b, esp, StrokeCap.Round)

/** A caixa (não terminal): o papelão de sempre, o mesmo das esteiras. */
internal fun DrawScope.caixaP(p: Pincel, cx: Float, cy: Float, lado: Float) = caixa(cx, cy, lado, p.u)
