package com.compilandia.ui.cast.art

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/*
 * O esqueleto do professor de papel recortado (escolhido pelo dono no painel de estilos): para cada humor e cada
 * instante, os ângulos do ombro, do cotovelo e do quadril, o pulo e o achatar. ProfessorRecortado.kt desenha.
 */

enum class Humor { PARADO, ANDANDO, ALEGRIA, CONTENTE }

/** Uma junta: giro em graus no próprio pivô, deslocamento e escala. */
data class Junta(val giro: Float = 0f, val dx: Float = 0f, val dy: Float = 0f, val sx: Float = 1f, val sy: Float = 1f)

/** O rosto que aparece: olhos abertos, piscando, de alegria (arcos para cima) ou de contente (fechados, sorrindo). */
enum class Rosto { NORMAL, PISCA, ALEGRIA, CONTENTE }

data class PoseProf(
    val tudo: Junta = Junta(),
    val tronco: Junta = Junta(),
    val cabeca: Junta = Junta(),
    val bracoE: Float = 8f,
    val bracoD: Float = -8f,
    val anteE: Float = 0f,
    val anteD: Float = 0f,
    val pernaE: Junta = Junta(),
    val pernaD: Junta = Junta(),
    val rosto: Rosto = Rosto.NORMAL,
    val sombra: Float = 1f,
    val brilhos: Boolean = false,
    val coracoes: Boolean = false,
)

fun poseDoProfessor(humor: Humor, t: Float, piscando: Boolean): PoseProf {
    val rosto = if (piscando) Rosto.PISCA else Rosto.NORMAL
    return when (humor) {
        Humor.PARADO -> parado(t, rosto)
        Humor.ANDANDO -> andando(t, rosto)
        Humor.ALEGRIA -> alegria(t)
        Humor.CONTENTE -> contente(t)
    }
}

/** Respira (o tronco estica um pouco), balança a cabeça devagar e os braços acompanham. */
private fun parado(t: Float, rosto: Rosto): PoseProf {
    val r = sin(t * 2.1f)
    return PoseProf(
        tronco = Junta(sy = 1f + r * 0.012f), cabeca = Junta(giro = sin(t * 0.9f) * 2.5f, dy = r * 0.8f),
        bracoE = 7f + r * 1.5f, bracoD = -7f - r * 1.5f, anteE = -4f, anteD = 4f, rosto = rosto,
    )
}

/** Uma perna sobe enquanto a outra apoia; o corpo sobe na passagem e achata um pouco no apoio. */
private fun andando(t: Float, rosto: Rosto): PoseProf {
    val w = t * PI.toFloat() * 2f * PASSOS_POR_S
    val s = sin(w)
    val apoio = abs(cos(w))
    fun perna(lado: Float) = max(0f, lado * s).let { Junta(dy = -it * 12f, sy = 1f - it * 0.14f) }
    return PoseProf(
        tudo = Junta(giro = s * 1.5f, dy = -abs(s) * 6f, sx = 1f + apoio * 0.02f, sy = 1f - apoio * 0.03f),
        tronco = Junta(giro = s * 1.2f), cabeca = Junta(giro = -s * 2.5f),
        bracoE = 10f - s * 16f, bracoD = -10f - s * 16f,
        anteE = -10f - max(0f, -s) * 22f, anteD = 10f + max(0f, s) * 22f,
        pernaE = perna(1f), pernaD = perna(-1f), rosto = rosto,
    )
}

/** Agacha, pula de braços para cima, achata ao cair e se ajeita. Um ciclo a cada [ALEGRIA_S]. */
private fun alegria(t: Float): PoseProf {
    val p = (t % ALEGRIA_S) / ALEGRIA_S
    val (y, escala, bra) = when {
        p < 0.18f -> Triple(0f, 1f - 0.1f * suave(p / 0.18f), 30f - 15f * suave(p / 0.18f))
        p < 0.62f -> ((p - 0.18f) / 0.44f).let {
            Triple(-4f * 24f * it * (1 - it), 1f + 0.05f * sin(it * PI.toFloat()), 140f)
        }
        p < 0.74f -> suave((p - 0.62f) / 0.12f).let { Triple(0f, 1f - 0.12f * sin(it * PI.toFloat()), 140f - 30f * it) }
        else -> Triple(0f, 1f, 110f + 20f * suave((p - 0.74f) / 0.26f))
    }
    val noAr = y < -4f
    val perna = Junta(dy = if (noAr) -6f else 0f, sy = if (noAr) 0.9f else 1f)
    return PoseProf(
        tudo = Junta(dy = y, sx = 2f - escala, sy = escala), cabeca = Junta(giro = sin(t * 9f) * 3f),
        bracoE = bra, bracoD = -bra, anteE = 18f, anteD = -18f, pernaE = perna, pernaD = perna,
        rosto = Rosto.ALEGRIA, sombra = 1f + y / 90f, brilhos = true,
    )
}

/** Braços cruzados na barriga, cabeça de lado, olhos fechados sorrindo e um balanço lento. */
private fun contente(t: Float): PoseProf {
    val b = sin(t * 1.6f)
    return PoseProf(
        tudo = Junta(giro = b * 2.5f), cabeca = Junta(giro = 8f + b * 3f),
        tronco = Junta(sy = 1f + sin(t * 2f) * 0.012f),
        bracoE = -16f, bracoD = 16f, anteE = -78f, anteD = 78f, rosto = Rosto.CONTENTE, coracoes = true,
    )
}

private fun suave(x: Float) = x * x * (3 - 2 * x)

private const val PASSOS_POR_S = 1.5f
const val ALEGRIA_S = 1.25f
