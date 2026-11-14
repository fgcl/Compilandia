package com.compilandia.tutor

import com.compilandia.content.Box
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.content.Wire
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.Minimizer
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.setText
import com.compilandia.learning.Skill
import com.compilandia.textos.tx
import kotlin.math.min
import kotlin.random.Random

/**
 * Questões de prova da Floresta Léxica: a tabela Dtran da construção de subconjuntos
 * e a escadinha de pares da minimização. Cada casa sai do motor ([SmallNFA], [Minimizer]).
 */
object ProvaAutomatos {
    private fun nome(i: Int) = ('A' + i).toString()

    /** O AFN desenhado numa linha, com as setas rotuladas (ε para as vazias). */
    private fun desenhoAfn(nfa: SmallNFA): Pair<List<Box>, List<Wire>> {
        val n = nfa.stateCount
        val passo = if (n > 1) 300f / (n - 1) else 0f
        val caixas = (0 until n).map { i ->
            Box("n$i", "$i", 30f + i * passo, 96f, 34f, 34f, if (i in nfa.accepting) Look.ACCEPT else Look.STATE, if (i == nfa.start) Tint.YELLOW else Tint.NONE, size = 13f)
        }
        val fios = nfa.edges.groupBy({ it.first to it.third }, { it.second?.toString() ?: "ε" }).map { (par, rot) ->
            Wire("n${par.first}", "n${par.second}", rot.sorted().joinToString(","), bend = if (par.first != par.second && par.second < par.first) -30f else 0f)
        }
        return caixas to fios
    }

    /** Subconjuntos: o estado inicial e cada casa Dtran[T, c] = ε-fecho(move(T, c)). */
    fun subconjuntos(nfa: SmallNFA): Sim {
        val r = nfa.subsetConstruction()
        val (afn, fios) = desenhoAfn(nfa)
        val sigma = nfa.alphabet
        val cw = min(70f, 200f / sigma.size)
        val y0 = 196f
        val ch = min(28f, 190f / (r.states.size + 1))
        fun x(c: Int) = 140f + cw / 2 + c * cw
        val cab = listOf(titulo(tx("prova_dtran_titulo"), y = 26f, size = 20f)) + afn +
            Box("hs", tx("prova_dtran_conjunto"), 82f, y0, 116f, ch - 3f, Look.CELL, Tint.DARK, size = 11f) +
            sigma.mapIndexed { c, s -> Box("hc$c", "$s", x(c), y0, cw - 3f, ch - 3f, Look.CELL, Tint.DARK, size = 12f) }
        val linhas = mutableListOf<Box>()
        fun linha(i: Int) {
            val fin = i in r.accepting
            linhas += Box("l$i", "${nome(i)} = ${setText(r.states[i])}", 82f, y0 + (i + 1) * ch, 116f, ch - 3f, Look.CELL, if (fin) Tint.GREEN else Tint.ORANGE, size = 11f)
        }
        val inicio = r.states[0]
        val passos = mutableListOf(SimStep(Shot(cab, fios), tx("prova_dtran_abre")))
        linha(0)
        val certoIni = setText(inicio)
        val aMais = (0 until nfa.stateCount).firstOrNull { it !in inicio }?.let { setText(inicio + it) }
        val (o0, i0) = opcoes(certoIni, listOfNotNull(setText(setOf(nfa.start)), aMais, setText(inicio - nfa.start)), 0)
        passos += SimStep(Shot(cab + linhas, fios), tx("prova_dtran_inicio", certoIni), SimAsk(tx("prova_dtran_inicio_q", nfa.start), o0, i0, tx("prova_dtran_inicio", certoIni)))
        val casas = mutableListOf<Box>()
        r.steps.forEachIndexed { k, st ->
            val i = r.states.indexOf(st.from)
            val c = sigma.indexOf(st.symbol)
            val novo = st.to.isNotEmpty() && linhas.none { it.id == "l${r.states.indexOf(st.to)}" }
            if (novo) linha(r.states.indexOf(st.to))
            val alvo = if (st.to.isEmpty()) "∅" else nome(r.states.indexOf(st.to))
            casas += Box("d$i-$c", alvo, x(c), y0 + (i + 1) * ch, cw - 3f, ch - 3f, Look.TOKEN, if (st.to.isEmpty()) Tint.DARK else Tint.TEAL, hot = true, size = 12f)
            val certa = setText(st.to)
            val outras = listOf(setText(st.moved)) + r.states.map { setText(it) }.shuffled(Random(k)) + "∅"
            val (ops, idx) = opcoes(certa, outras, k + 1)
            val diz = if (st.to.isEmpty()) tx("prova_dtran_morto", nome(i), st.symbol) else tx("prova_dtran_porque", setText(st.from), st.symbol, setText(st.moved), certa)
            passos += SimStep(Shot(cab + linhas + casas.map { if (it === casas.last()) it else it.copy(hot = false) }, fios), diz,
                SimAsk(tx("prova_dtran_q", nome(i), st.symbol), ops, idx, diz))
        }
        passos += finais(r.accepting.map { nome(it) }, r.states.size, Shot(cab + linhas + casas.map { it.copy(hot = false) }, fios), tx("prova_dtran_finais_porque"))
        return Sim(tx("trilha_etapa_prova"), passos, Skill.NFA_TO_DFA)
    }

    /** Quais estados são finais: os conjuntos com um final do AFN. */
    private fun finais(certos: List<String>, n: Int, shot: Shot, porque: String): SimStep {
        val certa = certos.joinToString(", ")
        val todos = (0 until n).map { nome(it) }
        val erradas = listOf(todos.last(), todos.filter { it !in certos }.joinToString(", "), todos.joinToString(", "))
        val (ops, idx) = opcoes(certa, erradas.filter { it.isNotEmpty() }, n)
        return SimStep(shot, porque, SimAsk(tx("prova_dtran_finais_q"), ops, idx, porque))
    }

    // ------------------------------------------------------------ Minimização

    /** A letra que derruba o par na rodada [r]: o par de destino já estava marcado antes. */
    private fun letra(res: Minimizer.Result, m: Minimizer.Mark, antes: Set<Pair<Int, Int>>): Char =
        res.dfa.alphabet.first { c -> par(res.dfa.delta.getValue(m.p to c), res.dfa.delta.getValue(m.q to c)) in antes }

    private fun par(a: Int, b: Int) = if (a < b) a to b else b to a

    fun minimizacao(d: DFA): Sim {
        val res = Minimizer.run(d)
        val a = res.dfa
        val n = a.stateCount
        fun nm(i: Int) = res.stateName(i)
        fun parTxt(p: Pair<Int, Int>) = "{${nm(p.first)}, ${nm(p.second)}}"
        val cw = min(46f, 260f / n)
        val transicoes = listOf(Box("th", "δ   " + a.alphabet.joinToString("   "), 180f, 52f, 200f, 22f, Look.CODE, size = 12f)) +
            (0 until n).map { i ->
                val fim = if (i in a.accepting) "*" else " "
                Box("t$i", "$fim${nm(i)}  " + a.alphabet.joinToString("  ") { nm(a.delta.getValue(i to it)) }, 180f, 74f + i * 19f, 200f, 18f, Look.CODE, size = 12f)
            }
        val y0 = 82f + n * 19f + 20f
        fun cx(j: Int) = 80f + j * cw
        fun cy(i: Int) = y0 + (i - 1) * (cw * 0.7f)
        val esc = mutableListOf(titulo(tx("prova_min_titulo"), y = 22f, size = 20f))
        for (i in 1 until n) {
            esc += Box("e$i", nm(i), cx(-1) + cw * 0.3f, cy(i), cw * 0.6f, cw * 0.62f, Look.LABEL, size = 12f)
            for (j in 0 until i) esc += Box("p$i-$j", "", cx(j), cy(i), cw - 3f, cw * 0.62f, Look.CELL, Tint.NONE)
        }
        for (j in 0 until n - 1) esc += Box("f$j", nm(j), cx(j), cy(n) - 4f, cw - 3f, 18f, Look.LABEL, size = 12f)
        val base = transicoes + esc
        val marcas = mutableListOf<Box>()
        fun marca(m: Minimizer.Mark) {
            val (p, q) = par(m.p, m.q)
            marcas += Box("p$q-$p", "${m.round}", cx(p), cy(q), cw - 3f, cw * 0.62f, Look.TOKEN, Tint.RED, hot = true, size = 12f)
        }
        val passos = mutableListOf(SimStep(Shot(base), tx("prova_min_abre")))
        val r1 = res.marks.filter { it.round == 1 }
        r1.forEach { marca(it) }
        val marcados = res.marks.map { par(it.p, it.q) }.toSet()
        val livres = (1 until n).flatMap { i -> (0 until i).map { j -> j to i } }.filter { it !in marcados }
        val tardios = res.marks.filter { it.round > 1 }.map { par(it.p, it.q) }
        val (o1, i1) = opcoes(parTxt(par(r1[0].p, r1[0].q)), (tardios + livres).map { parTxt(it) }, 1)
        passos += SimStep(Shot(base + marcas), tx("prova_min_r1"), SimAsk(tx("prova_min_r1_q"), o1, i1, tx("prova_min_r1")))
        val antes = r1.map { par(it.p, it.q) }.toMutableSet()
        var rodada = 2
        for ((k, m) in res.marks.filter { it.round > 1 }.withIndex()) {
            if (m.round != rodada) { antes += res.marks.filter { it.round == rodada }.map { par(it.p, it.q) }; rodada = m.round }
            val c = letra(res, m, antes)
            val destino = par(a.delta.getValue(m.p to c), a.delta.getValue(m.q to c))
            val outra = a.alphabet.firstOrNull { it != c }?.let { o -> par(a.delta.getValue(m.p to o), a.delta.getValue(m.q to o)) }
            marca(m)
            val diz = tx("prova_min_porque", parTxt(par(m.p, m.q)), c, parTxt(destino), m.round)
            val (ops, idx) = opcoes(parTxt(destino), listOfNotNull(outra?.takeIf { it != destino && it.first != it.second }?.let { parTxt(it) }, parTxt(par(m.p, m.q))) + livres.map { parTxt(it) }, k)
            passos += SimStep(Shot(base + marcas.map { if (it === marcas.last()) it else it.copy(hot = false) }), diz,
                SimAsk(tx("prova_min_q", parTxt(par(m.p, m.q)), c), ops, idx, diz))
        }
        passos += juntam(res, livres.map { parTxt(it) }, marcados.map { parTxt(it) }, Shot(base + marcas.map { it.copy(hot = false) }))
        return Sim(tx("trilha_etapa_prova"), passos, Skill.DFA_MINIMIZATION)
    }

    /** O fim da prova: os pares em branco se juntam. Sem par em branco, nada se junta. */
    private fun juntam(res: Minimizer.Result, livres: List<String>, marcados: List<String>, shot: Shot): SimStep {
        val nenhum = tx("prova_min_nenhum")
        val certa = livres.joinToString(", ").ifEmpty { nenhum }
        val (ops, idx) = opcoes(certa, marcados.shuffled(Random(res.marks.size)) + nenhum, res.marks.size)
        val porque = if (livres.isEmpty()) tx("prova_min_fim_nada") else tx("prova_min_fim", certa, res.minimized.stateCount)
        return SimStep(shot, porque, SimAsk(tx("prova_min_fim_q"), ops, idx, porque))
    }
}
