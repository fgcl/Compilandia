package com.compilandia.tutor

import com.compilandia.content.Box
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.content.Wire
import com.compilandia.content.codigo
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprParser
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import com.compilandia.textos.tx
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Questão de prova da Forja (professor do bloco 5): o que a prova pede, do jeito que ela pede.
 * TAC: a expressão vira TAC, quádruplas e triplas. Alocação: a tabela de vivacidade de baixo para cima
 * (use, def, out, in), as arestas do grafo e o número de cores. Tudo sai do [TacGen] e do [RegAlloc].
 */
object ProvaForja {
    /** Do simples ao difícil: a terceira repete a * b, e as triplas mostram as duas contas iguais. */
    val TAC = listOf("a + b * c", "( a + b ) * ( a - c )", "a * b + a * b")

    /** A terceira precisa de três registradores: t1 e t2 ficam vivos enquanto t3 nasce. */
    val ALOCACAO = listOf("a + b * c", "a * b + c * d", "( a + b ) * ( ( c - d ) * ( e + f ) )")

    const val DESTINO = "x"

    private fun sem(s: String) = if (s.startsWith("(") && s.endsWith(")")) s.drop(1).dropLast(1) else s

    // ------------------------------------------------------------ TAC, quádruplas, triplas

    /** As quádruplas: op, arg1, arg2, res. A atribuição final é "=" com o temporário em arg1. */
    fun quadruplas(code: List<TacGen.Instr>): List<List<String>> =
        code.map { if (it.op == null) listOf("=", it.a, "", it.target) else listOf(it.op, it.a, it.b!!, it.target) }

    /** As triplas: o resultado não tem nome; quem usa aponta a posição (k) da linha que o calculou. */
    fun triplas(code: List<TacGen.Instr>): List<List<String>> {
        fun ref(x: String) = code.indexOfFirst { it.target == x && RegAlloc.isTemp(x) }.let { if (it >= 0) "($it)" else x }
        return code.map { if (it.op == null) listOf("=", it.target, ref(it.a)) else listOf(it.op, ref(it.a), ref(it.b!!)) }
    }

    private fun linhas(cab: List<String>, rs: List<List<String>>) = listOf(listOf("") + cab) + rs.mapIndexed { i, r -> listOf("($i)") + r }

    fun tac(src: String): Sim {
        val e = ExprParser.parse(src)
        val code = TacGen.generate(e, DESTINO)
        val conta = "$DESTINO = $src"
        val topo = listOf(titulo(tx("trilha_etapa_prova")), rotulo("src", conta, 180f, 66f, Tint.YELLOW, 16f))
        val passos = mutableListOf(SimStep(Shot(topo), tx("prova_tac_abre", conta)))
        passos += instrucoes(e, code, topo)
        val lista = codigo("l", code.mapIndexed { i, c -> "($i) $c" }, 96f, x = 70f, passo = 22f, size = 13f)
        passos += quadruplasPassos(code, topo + lista)
        passos += triplasPassos(code, topo + lista)
        val (ops, idx) = opcoes(tx("prova_tac_quad"), listOf(tx("prova_tac_trip"), tx("prova_tac_as_duas")), code.size)
        passos += SimStep(Shot(topo + lista), tx("prova_tac_reordena_porque"), SimAsk(tx("prova_tac_reordena"), ops, idx, tx("prova_tac_reordena_porque")))
        return Sim(tx("trilha_etapa_prova"), passos, Skill.TAC)
    }

    /** Uma linha por vez, na pós-ordem. As erradas: a conta inteira numa linha só, e outra conta fora da vez. */
    private fun instrucoes(e: Expr, code: List<TacGen.Instr>, topo: List<Box>): List<SimStep> {
        val internos = e.postorder().filter { !it.isLeaf }
        return code.mapIndexed { k, ins ->
            val certa = ins.toString()
            val erradas = if (ins.op == null) listOf("${ins.a} = ${ins.target}", "${ins.target} = ${sem(e.render())}")
            else listOf("${ins.target} = ${sem(e.render())}") + internos.filter { it !== internos[k] }.map { "${ins.target} = ${sem(it.render())}" }
            val (ops, idx) = opcoes(certa, erradas, k)
            val feitas = codigo("l", code.take(k + 1).mapIndexed { i, c -> "($i) $c" }, 96f, x = 70f, passo = 22f, size = 13f)
            val diz = if (ins.op == null) tx("prova_tac_copia", ins.a, ins.target) else tx("prova_tac_linha", sem(internos[k].render()), ins.target)
            SimStep(Shot(topo + feitas), diz, SimAsk(tx("prova_tac_proxima", k), ops, idx, diz))
        }
    }

    private fun comPergunta(t: List<Box>, id: String): List<Box> = t.map { if (it.id == id) it.copy(text = "?", hot = true, tint = Tint.YELLOW) else it }

    private fun quadruplasPassos(code: List<TacGen.Instr>, base: List<Box>): List<SimStep> {
        val rs = quadruplas(code)
        val t = tabela("q", linhas(listOf("op", "arg1", "arg2", "res"), rs), 236f, cw = 58f, ch = 28f, size = 13f)
        val ult = rs.lastIndex
        val (o1, i1) = opcoes(rs[0][3], listOf(rs[0][1], "(0)"), 1)
        val (o2, i2) = opcoes(rs[ult][1], listOf(rs[ult][3], "($ult)"), 2)
        val d1 = tx("prova_quad_res", rs[0][3])
        val d2 = tx("prova_quad_copia", rs[ult][1])
        // O player mostra o palco do passo anterior enquanto a pergunta está aberta: o "?" vem num passo antes dela.
        return listOf(
            SimStep(Shot(base + comPergunta(t, "ql1c4")), tx("prova_quad_abre")),
            SimStep(Shot(base + t), d1, SimAsk(tx("prova_quad_q_res"), o1, i1, d1)),
            SimStep(Shot(base + comPergunta(t, "ql${ult + 1}c2")), tx("prova_quad_copia_abre")),
            SimStep(Shot(base + t), d2, SimAsk(tx("prova_quad_q_copia", ult), o2, i2, d2)),
        )
    }

    private fun triplasPassos(code: List<TacGen.Instr>, base: List<Box>): List<SimStep> {
        val rs = triplas(code)
        val t = tabela("p", linhas(listOf("op", "arg1", "arg2"), rs), 236f, cw = 64f, ch = 28f, size = 13f)
        // A primeira tripla que aponta para outra linha, e a da atribuição.
        val k = rs.indices.first { i -> rs[i].any { it.startsWith("(") } && rs[i][0] != "=" }
        val c = if (rs[k][1].startsWith("(")) 1 else 2
        val nome = code[k].let { if (c == 1) it.a else it.b!! }
        val ult = rs.lastIndex
        val (o1, i1) = opcoes(rs[k][c], listOf(nome, "($k)"), 3)
        val (o2, i2) = opcoes(rs[ult][2], listOf(code[ult].a, "($ult)"), 4)
        val d1 = tx("prova_trip_ref", nome, rs[k][c])
        val d2 = tx("prova_trip_copia", DESTINO, rs[ult][2])
        return listOf(
            SimStep(Shot(base + comPergunta(t, "pl${k + 1}c${c + 1}")), tx("prova_trip_abre")),
            SimStep(Shot(base + t), d1, SimAsk(tx("prova_trip_q_ref", k, nome), o1, i1, d1)),
            SimStep(Shot(base + comPergunta(t, "pl${ult + 1}c3")), tx("prova_trip_copia_abre")),
            SimStep(Shot(base + t), d2, SimAsk(tx("prova_trip_q_copia", ult), o2, i2, d2)),
        )
    }

    // ------------------------------------------------------------ Vivacidade, arestas, cores

    fun conj(s: Set<String>) = if (s.isEmpty()) "{ }" else s.sortedBy { it.drop(1).toInt() }.joinToString(",", "{", "}")

    data class Linha(val use: Set<String>, val def: Set<String>, val out: Set<String>, val inn: Set<String>)

    fun tabelaViva(code: List<TacGen.Instr>): List<Linha> {
        val lv = RegAlloc.liveness(code)
        return code.mapIndexed { i, c ->
            Linha(listOfNotNull(c.a, c.b).filter { RegAlloc.isTemp(it) }.toSet(), setOfNotNull(c.target.takeIf { RegAlloc.isTemp(it) }), lv.liveOut[i], lv.liveIn[i])
        }
    }

    fun vivacidade(src: String, prova: Boolean = true): Sim {
        val code = TacGen.generate(ExprParser.parse(src), DESTINO)
        val vt = tabelaViva(code)
        val topo = listOf(titulo(if (prova) tx("trilha_etapa_prova") else tx("prova_viva_titulo"))) +
            codigo("l", code.mapIndexed { i, c -> "($i) $c" }, 58f, x = 70f, passo = 21f, size = 13f)
        val y0 = 70f + code.size * 21f
        fun linhaDe(i: Int, l: Linha?) = listOf("($i)") + (l?.let { listOf(conj(it.use), conj(it.def), conj(it.out), conj(it.inn)) } ?: List(4) { "" })
        // De baixo para cima: as linhas de [ate] para baixo estão cheias. Enquanto pergunta o in da linha i, o player mostra o passo anterior, sem ela.
        fun tab(ate: Int) = tabela("v", listOf(listOf("", "use", "def", "out", "in")) + vt.indices.map { i -> linhaDe(i, vt[i].takeIf { i >= ate }) },
            y0, cw = 70f, ch = 26f, size = 11f)
        val passos = mutableListOf(SimStep(Shot(topo + tab(vt.size)), tx("prova_viva_abre")))
        for (i in vt.indices.reversed()) {
            val l = vt[i]
            val erradas = listOf(conj(l.out + l.use), conj(l.use), conj(l.out), conj(l.inn + l.def))
            val (ops, idx) = opcoes(conj(l.inn), erradas, i)
            val diz = tx("prova_viva_in", i, conj(l.use), conj(l.out), conj(l.def), conj(l.inn))
            passos += SimStep(Shot(topo + tab(i)), diz, SimAsk(tx("prova_viva_q", i), ops, idx, diz))
        }
        passos += grafo(code, topo)
        return Sim(if (prova) tx("trilha_etapa_prova") else tx("prova_viva_titulo"), passos, Skill.REG_ALLOC)
    }

    private fun nos(g: Map<String, Set<String>>, cores: Map<String, Int>?): Pair<List<Box>, List<Wire>> {
        val ts = g.keys.sortedBy { it.drop(1).toInt() }
        val tintas = listOf(Tint.TEAL, Tint.ORANGE, Tint.PINK, Tint.BLUE)
        val bs = ts.mapIndexed { i, t ->
            val ang = -PI / 2 + 2 * PI * i / ts.size
            val nome = cores?.get(t)?.let { "$t: \$t$it" } ?: t
            Box("n$t", nome, 180f + 110f * cos(ang).toFloat(), 250f + 90f * sin(ang).toFloat(), if (cores != null) 84f else 52f, 40f, Look.STATE, cores?.get(t)?.let { tintas[it % 4] } ?: Tint.NONE, size = 13f)
        }
        val ws = ts.flatMap { a -> g.getValue(a).filter { it.drop(1).toInt() > a.drop(1).toInt() }.map { b -> Wire("n$a", "n$b", arrow = false) } }
        return bs to ws
    }

    /** O grafo de interferência e as cores: com quem o temporário que mais briga interfere, e quantos registradores bastam. */
    private fun grafo(code: List<TacGen.Instr>, topo: List<Box>): List<SimStep> {
        val g = RegAlloc.interference(code)
        val k = RegAlloc.chromatic(g)
        val alvo = g.keys.maxBy { g.getValue(it).size * 100 - it.drop(1).toInt() }
        val (bs, ws) = nos(g, null)
        val certa = conj(g.getValue(alvo))
        val erradas = listOf(conj(g.keys - alvo), conj(g.getValue(alvo) + alvo), conj(emptySet())).filter { it != certa }
        val (o1, i1) = opcoes(certa, erradas, 5)
        val d1 = tx("prova_viva_aresta", alvo, certa)
        val (o2, i2) = opcoes("$k", listOf("${g.size}", "${k + 1}", "${maxOf(1, k - 1)}"), 6)
        val d2 = tx("prova_viva_cores", k)
        val (bc, wc) = nos(g, RegAlloc.color(g, k).colors)
        return listOf(
            SimStep(Shot(topo.take(1) + bs, ws), tx("prova_viva_grafo")),
            SimStep(Shot(topo.take(1) + bs, ws), d1, SimAsk(tx("prova_viva_q_aresta", alvo), o1, i1, d1)),
            SimStep(Shot(topo.take(1) + bs, ws), d2, SimAsk(tx("prova_viva_q_cores"), o2, i2, d2)),
            SimStep(Shot(topo.take(1) + bc, wc), tx("prova_viva_fim", k)),
        )
    }
}
