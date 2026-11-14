package com.compilandia.learning

import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Bayesian Knowledge Tracing (Corbett & Anderson, 1995).
 * Domínio = estado oculto binário; cada tentativa atualiza P(sabe).
 */
@Serializable
data class BKT(
    val pKnow: Double = 0.1,
    val pLearn: Double = 0.15,
    val pSlip: Double = 0.1,
    val pGuess: Double = 0.2,
) {
    /** score ∈ [0,1]; acertos parciais viram uma mistura das duas atualizações. */
    fun update(score: Double): BKT {
        val s = score.coerceIn(0.0, 1.0)
        val postCorrect = pKnow * (1 - pSlip) / (pKnow * (1 - pSlip) + (1 - pKnow) * pGuess)
        val postWrong = pKnow * pSlip / (pKnow * pSlip + (1 - pKnow) * (1 - pGuess))
        val posterior = s * postCorrect + (1 - s) * postWrong
        val next = posterior + (1 - posterior) * pLearn
        return copy(pKnow = next.coerceIn(0.001, 0.999))
    }

    fun pCorrect(): Double = pKnow * (1 - pSlip) + (1 - pKnow) * pGuess
}

/**
 * Memória com curva de esquecimento no estilo FSRS:
 *   R(t) = (1 + t / (9·S))^-1      (t e S em dias)
 * S = estabilidade (dias até R cair para 90%), D = dificuldade (1..10).
 */
@Serializable
data class Memory(
    val stability: Double = 0.5,
    val difficulty: Double = 5.0,
    val lastReviewMs: Long = 0,
    val reviews: Int = 0,
    val lapses: Int = 0,
    /** Errou uma revisão: o assunto fica pendente até a próxima acertar (Rawson e Dunlosky, 2013). */
    val reaprender: Boolean = false,
) {
    fun retrievability(nowMs: Long): Double {
        if (reviews == 0) return 0.0
        val days = max(0.0, (nowMs - lastReviewMs) / DAY_MS)
        return (1.0 + days / (9.0 * stability)).pow(-1.0)
    }

    /** Intervalo ideal até a próxima revisão para manter R ≥ [target]. */
    fun intervalDays(target: Double = 0.9): Double = 9.0 * stability * (1.0 / target - 1.0)

    fun review(nowMs: Long, score: Double): Memory {
        val r = if (reviews == 0) 0.0 else retrievability(nowMs)
        val good = score >= 0.6
        val d = (difficulty + (if (good) -0.6 * (score - 0.6) / 0.4 else 1.2 * (0.6 - score) / 0.6)).coerceIn(1.0, 10.0)
        val s = if (reviews == 0) {
            if (good) 1.0 + 2.0 * score else 0.3
        } else if (good) {
            // revisar quando já está esquecendo (R baixo) fortalece MAIS: efeito de espaçamento
            val gain = exp(0.9) * (11 - d) * stability.pow(-0.2) * (exp((1 - r) * 1.4) - 1) * (0.5 + score / 2)
            stability * (1 + max(0.1, gain))
        } else {
            max(0.2, stability * 0.35)
        }
        // Errar na primeira vez é aprender; errar quando a revisão já era devida deixa a revisão em aberto.
        val emAberto = !good && (reaprender || isDue(nowMs))
        return Memory(min(s, 365.0), d, nowMs, reviews + 1, lapses + if (good) 0 else 1, emAberto)
    }

    /** Pendente quando a memória caiu abaixo de 90% ou quando a última revisão foi um erro. */
    fun isDue(nowMs: Long): Boolean = reviews > 0 && (reaprender || retrievability(nowMs) < 0.9)

    companion object { const val DAY_MS = 86_400_000.0 }
}

/**
 * Pequena rede neural (MLP 10→12→1) treinada ONLINE, a cada tentativa,
 * para prever a chance de você acertar o próximo desafio. É uma versão de
 * bolso da ideia de "Deep Knowledge Tracing" (Piech et al., 2015):
 * em vez de regras fixas, ela aprende o SEU padrão.
 *
 * Usada para escolher a dificuldade que deixa você na "zona de desafio"
 * (~70–80% de chance de acerto — dificuldade desejável).
 */
@Serializable
data class TinyNet(
    val nIn: Int = FEATURES,
    val nHidden: Int = 12,
    val w1: List<Double> = emptyList(),
    val b1: List<Double> = emptyList(),
    val w2: List<Double> = emptyList(),
    val b2: Double = 0.0,
    val trainedSamples: Int = 0,
    val runningLoss: Double = 0.25,
) {
    companion object {
        const val FEATURES = 10
        fun init(seed: Int = 42): TinyNet {
            val rnd = Random(seed)
            val nIn = FEATURES; val nH = 12
            val s1 = sqrt(1.0 / nIn); val s2 = sqrt(1.0 / nH)
            return TinyNet(
                nIn, nH,
                List(nIn * nH) { (rnd.nextDouble() * 2 - 1) * s1 },
                List(nH) { 0.0 },
                List(nH) { (rnd.nextDouble() * 2 - 1) * s2 },
                0.0,
            )
        }

        fun sigmoid(x: Double) = 1.0 / (1.0 + exp(-x))
        fun tanh(x: Double): Double { val e = exp(2 * x); return (e - 1) / (e + 1) }
    }

    private fun ready() = if (w1.isEmpty()) init() else this

    fun predict(x: DoubleArray): Double {
        val n = ready()
        var o = n.b2
        for (h in 0 until n.nHidden) {
            var a = n.b1[h]
            for (i in 0 until n.nIn) a += n.w1[h * n.nIn + i] * x[i]
            o += n.w2[h] * tanh(a.coerceIn(-20.0, 20.0))
        }
        return sigmoid(o)
    }

    /** Um passo de SGD com perda logística (entropia cruzada) e decaimento L2. */
    fun train(x: DoubleArray, y: Double, lr: Double = 0.08, l2: Double = 1e-4): TinyNet {
        val n = ready()
        val hidden = DoubleArray(n.nHidden)
        var o = n.b2
        for (h in 0 until n.nHidden) {
            var a = n.b1[h]
            for (i in 0 until n.nIn) a += n.w1[h * n.nIn + i] * x[i]
            hidden[h] = tanh(a.coerceIn(-20.0, 20.0))
            o += n.w2[h] * hidden[h]
        }
        val p = sigmoid(o)
        val dOut = p - y
        val w1 = n.w1.toMutableList(); val b1 = n.b1.toMutableList(); val w2 = n.w2.toMutableList()
        for (h in 0 until n.nHidden) {
            val dH = dOut * n.w2[h] * (1 - hidden[h] * hidden[h])
            w2[h] = n.w2[h] - lr * (dOut * hidden[h] + l2 * n.w2[h])
            b1[h] = n.b1[h] - lr * dH
            for (i in 0 until n.nIn) {
                val k = h * n.nIn + i
                w1[k] = n.w1[k] - lr * (dH * x[i] + l2 * n.w1[k])
            }
        }
        val loss = -(y * ln(p.coerceIn(1e-6, 1 - 1e-6)) + (1 - y) * ln((1 - p).coerceIn(1e-6, 1 - 1e-6)))
        return n.copy(
            w1 = w1, b1 = b1, w2 = w2, b2 = n.b2 - lr * dOut,
            trainedSamples = n.trainedSamples + 1,
            runningLoss = 0.95 * n.runningLoss + 0.05 * loss,
        )
    }
}

/** Beta(α, β) por habilidade/estilo/jogo — Thompson Sampling (bandit multi-braço). */
@Serializable
data class BetaArm(val alpha: Double = 1.0, val beta: Double = 1.0) {
    fun mean() = alpha / (alpha + beta)
    fun reward(r: Double) = copy(alpha = alpha + r, beta = beta + (1 - r))
    fun sample(rnd: Random): Double {
        val x = gamma(alpha, rnd); val y = gamma(beta, rnd)
        return x / (x + y)
    }

    private fun gamma(shape: Double, rnd: Random): Double {
        if (shape < 1) return gamma(shape + 1, rnd) * rnd.nextDouble().coerceAtLeast(1e-12).pow(1 / shape)
        val d = shape - 1.0 / 3; val c = 1 / sqrt(9 * d)
        while (true) {
            var x: Double; var v: Double
            do { x = normal(rnd); v = 1 + c * x } while (v <= 0)
            v = v * v * v
            val u = rnd.nextDouble()
            if (u < 1 - 0.0331 * x * x * x * x) return d * v
            if (ln(u) < 0.5 * x * x + d * (1 - v + ln(v))) return d * v
        }
    }

    private fun normal(rnd: Random): Double {
        val u1 = rnd.nextDouble().coerceAtLeast(1e-12); val u2 = rnd.nextDouble()
        return sqrt(-2 * ln(u1)) * kotlin.math.cos(2 * kotlin.math.PI * u2)
    }
}
