package com.compilandia.tutor

import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EPS
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.LRItem
import com.compilandia.engine.ParseNode
import com.compilandia.engine.Production
import com.compilandia.engine.render
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/**
 * Perguntas do "Fazer com ajuda" para recursão, fatoração, itens LR(0) e ambiguidade (professor dos blocos 1 a 4).
 * Cada resposta certa sai do motor; as erradas são os erros de prova de cada receita.
 */
object PerguntasGuiadas {
    /** Uma recursão à esquerda por gramática, com um pedaço que repete e um que termina. */
    val RECURSAO = listOf(
        "E -> E + T | T\nT -> id",
        "L -> L , id | id",
        "S -> S a | b",
        "T -> T * F | F\nF -> n",
        "C -> C ; A | A\nA -> id = n",
        "P -> P x | y z",
    )

    /** Um grupo de opções com o mesmo começo; algumas sobram com resto vazio. */
    val FATORACAO = listOf(
        "S -> if E then S | if E then S else S | a\nE -> b",
        "A -> a b c | a b d | e",
        "L -> id , L | id",
        "C -> id = n | id ( )",
        "D -> int id ; | int id = n ;",
        "V -> id [ n ] | id",
    )

    val LR0 = listOf("E -> E + n | n", "S -> ( S ) | id", "L -> L , id | id", "E -> E * n | n", "A -> id = E\nE -> E + n | n", "S -> a S | b")

    /** Frases de três números: duas árvores cada. Nas de operador repetido que comuta, as duas dão o mesmo valor. */
    val CONTAS = listOf("9 - 5 - 2", "2 + 3 * 4", "8 - 2 * 3", "2 * 3 + 4", "1 + 2 + 3", "7 - 4 + 2", "2 * 3 * 4")

    /** A gramática ambígua das contas. */
    val GRAMATICA_CONTAS: Grammar = Grammar.parse("E -> E + E | E - E | E * E | " + (0..9).joinToString(" | "))

    private fun regra(nt: String, ps: List<List<String>>) = "$nt → " + ps.joinToString(" | ") { if (it.isEmpty()) EPS else it.joinToString(" ") }
    private fun regra(g: Grammar, nt: String) = regra(nt, g.productionsOf(nt).map { it.rhs })
    private fun novoNome(antes: Grammar, depois: Grammar, nt: String) = depois.nonTerminals.first { it !in antes.nonTerminals && it.startsWith(nt) }

    /** Recursão: o pedaço que repete, a regra do não-terminal e a regra nova. Os passos são os de [Sims.recursao]. */
    fun recursao(g: Grammar, sim: Sim): Sim {
        val nt = GrammarTransform.leftRecursiveNTs(g).first()
        val alfa = g.productionsOf(nt).first { it.rhs.firstOrNull() == nt }.rhs.drop(1)
        val beta = g.productionsOf(nt).firstOrNull { it.rhs.firstOrNull() != nt }?.rhs ?: emptyList()
        val final = GrammarTransform.eliminateImmediateLeftRecursion(g)
        val novo = novoNome(g, final, nt)
        val a = alfa.joinToString(" ")
        val b = beta.joinToString(" ").ifEmpty { EPS }
        val perguntas = mapOf(
            1 to pergunta(tx("pg_rec_repete"), a, listOf(b, "$nt $a"), 1, tx("sims_pedaco_laranja_que_repete_1", a)),
            3 to pergunta(tx("pg_regra_de", nt), regra(final, nt), listOf(regra(nt, listOf(listOf(novo) + beta)), regra(nt, listOf(beta))), 3, tx("pg_rec_regra_de_porque", b, novo)),
            4 to pergunta(
                tx("pg_regra_nova", novo), regra(final, novo),
                listOf(regra(novo, listOf(alfa + novo)), regra(novo, listOf(listOf(novo) + alfa, emptyList()))), 4, tx("pg_rec_regra_nova_porque", a),
            ),
        )
        val comPerguntas = sim.copy(passos = sim.passos.mapIndexed { i, p -> p.copy(ask = perguntas[i] ?: p.ask) })
        return if (g.nonTerminals.size > 1) comGramatica(comPerguntas, g, 70f, ate = sim.passos.lastIndex) else comPerguntas
    }

    /** Fatoração: o maior começo comum, a regra do não-terminal e a regra nova. Os passos são os de [Sims.fatorar]. */
    fun fatoracao(g: Grammar, nt: String, grupo: List<Production>, comum: Int, sim: Sim): Sim {
        val final = GrammarTransform.leftFactor(g)
        val novo = novoNome(g, final, nt)
        val pre = grupo[0].rhs.take(comum)
        val p = pre.joinToString(" ")
        val outras = g.productionsOf(nt).filter { it !in grupo }.map { it.rhs }
        val restos = grupo.map { it.rhs.drop(comum) }
        val maisLonga = grupo.maxBy { it.rhs.size }.rhs
        val erradasComeco = listOfNotNull(
            pre.take(1).takeIf { comum > 1 }?.joinToString(" "),
            maisLonga.take(comum + 1).takeIf { it.size > comum }?.joinToString(" "),
            nt,
        )
        // O ε a mais ou a menos: quem tem sobra vazia esquece o ε; quem não tem inventa um.
        val erradaEps = if (restos.any { it.isEmpty() }) restos.filter { it.isNotEmpty() } else restos + listOf(emptyList())
        val perguntas = mapOf(
            1 to pergunta(tx("pg_fat_comeco"), p, erradasComeco, 1, tx("pg_fat_comeco_porque", p)),
            2 to pergunta(
                tx("pg_regra_de", nt), regra(nt, listOf(pre + novo) + outras),
                listOf(regra(nt, listOf(listOf(novo) + pre) + outras), regra(nt, listOf(pre) + outras)), 2, tx("pg_fat_regra_de_porque", p, novo),
            ),
            3 to pergunta(
                tx("pg_regra_nova", novo), regra(final, novo),
                listOf(regra(novo, grupo.map { it.rhs }), regra(novo, erradaEps)), 3, tx("pg_fat_regra_nova_porque"),
            ),
        )
        val comPerguntas = sim.copy(passos = sim.passos.mapIndexed { i, s -> s.copy(ask = perguntas[i] ?: s.ask) })
        return if (g.nonTerminals.size > 1) comGramatica(comPerguntas, g, 70f) else comPerguntas
    }

    /** A gramática inteira, pequena, no palco dos passos antes de [ate]: o Fazer com ajuda sempre mostra o contexto. */
    internal fun comGramatica(sim: Sim, g: Grammar, y: Float, ate: Int = sim.passos.size): Sim {
        val caixas = codigo("gx", g.toText().lines(), y, x = 24f, passo = 17f, size = 12f)
        return sim.copy(passos = sim.passos.mapIndexed { i, p -> if (i < ate) p.copy(shot = p.shot.copy(boxes = p.shot.boxes + caixas)) else p })
    }

    private fun pergunta(q: String, certa: String, erradas: List<String>, k: Int, porque: String): SimAsk {
        val (ops, idx) = opcoes(certa, erradas, k)
        return SimAsk(q, ops, idx, porque)
    }

    // ------------------------------------------------------------ Itens LR(0)

    /**
     * A pergunta do estado [k]: se o fecho acrescentou itens, um estado sim e outro não pergunta qual;
     * senão, pergunta qual item o goto leva para lá (o ponto pula o símbolo lido). Null quando não há dois erros plausíveis.
     */
    fun lr0(a: LRAutomaton, k: Int): Pair<String, SimAsk?> {
        val g = a.grammar
        val st = a.states[k]
        val (origem, lido) = a.transitions.entries.first { it.value == k }.key
        val chegada = tx("pg_lr0_chegada", origem, lido, k)
        val doFecho = st.items - st.kernel
        fun tam(i: LRItem) = g.productions[i.prodId].rhs.size
        fun fora(c: List<LRItem>) = c.filter { it !in st.items && it.dot in 0..tam(it) }.map { it.render(g) }.distinct()
        val ask = if (doFecho.isNotEmpty() && k % 2 == 0) {
            val certo = doFecho.first()
            val y = g.productions[certo.prodId].lhs
            val outros = g.productions.indices.map { LRItem(it, 0) }
            pergunta(tx("pg_lr0_fecho", k, st.kernel.first().render(g)), certo.render(g), fora(listOf(LRItem(certo.prodId, tam(certo))) + outros), k, tx("pg_lr0_fecho_porque", y))
        } else {
            val base = st.kernel.first()
            val antes = LRItem(base.prodId, base.dot - 1)
            val candidatos = listOf(antes, LRItem(base.prodId, base.dot + 1), LRItem(base.prodId, tam(base))) + g.productions.indices.map { LRItem(it, 1) }
            pergunta(tx("pg_lr0_goto", origem, antes.render(g), lido, k), base.render(g), fora(candidatos), k, tx("pg_lr0_goto_porque", lido))
        }
        return chegada to ask.takeIf { it.opcoes.size == 3 }
    }

    // ------------------------------------------------------------ Ambiguidade com contas

    private fun valor(n: ParseNode): Int = when {
        n.children.isEmpty() -> n.symbol.toInt()
        n.children.size == 1 -> valor(n.children[0])
        else -> {
            val x = valor(n.children[0])
            val y = valor(n.children[2])
            when (n.children[1].symbol) { "+" -> x + y; "-" -> x - y; else -> x * y }
        }
    }

    /** A conta que roda primeiro: o nó de operador cujos dois filhos já são números. */
    private fun primeira(n: ParseNode): ParseNode =
        if (n.children.size == 3 && n.children.all { it.children.size <= 1 }) n
        else n.children.filter { it.children.size == 3 }.map { primeira(it) }.first()

    /** Duas árvores para uma conta de três números: o que roda primeiro e quanto dá cada uma. */
    fun contas(frase: String): Sim? {
        val tokens = frase.split(" ")
        val ds = DerivationHelper.leftmostDerivations(GRAMATICA_CONTAS, tokens, limit = 2)
        if (ds.size < 2) return null
        val arvores = ds.map { DerivationHelper.treeOf(GRAMATICA_CONTAS, it) }
        val (va, vb) = arvores.map { valor(it) }
        val sub = arvores.map { primeira(it).leaves().joinToString(" ") }
        val pares = listOf(tokens.take(3), tokens.drop(2)).map { it.joinToString(" ") }
        val n = tokens.filterIndexed { i, _ -> i % 2 == 0 }.map { it.toInt() }
        val chutes = listOf(va, vb, n.sum(), n[0] * n[1] * n[2], n[0] + n[1] * n[2], n[0] * n[1] + n[2], va + 1, va - 1).map { it.toString() }
        val rot = rotulo("fr", frase, 180f, 30f, Tint.YELLOW, 20f)
        val regras = codigo("gx", listOf("E → E + E | E - E | E * E | 0 | 1 | ... | 9"), 58f, x = 30f, size = 12f)
        fun arvore(i: Int, vararg extra: com.compilandia.content.Box): Shot {
            val (bs, ws) = Sims.desenhaArvore(arvores[i], y0 = 92f, alturaUtil = 240f)
            return Shot(listOf(rot) + regras + bs + extra, ws)
        }
        fun nota(id: String, t: String) = rotulo(id, t, 180f, 372f, Tint.TEAL, 16f)
        val passos = listOf(
            SimStep(Shot(listOf(titulo(tx("comum_duas_arvores")), rot) + codigo("g", listOf("E → E + E | E - E | E * E", "E → 0 | 1 | 2 | ... | 9"), 130f, x = 60f, passo = 30f, size = 15f)), tx("pg_amb_intro")),
            SimStep(arvore(0), tx("sims_olha_arvore_para_esta_frase")),
            SimStep(arvore(0, nota("pr", tx("pg_amb_primeiro", sub[0]))), tx("pg_amb_fundo", sub[0]), pergunta(tx("pg_amb_q_primeiro"), sub[0], pares + frase, 2, tx("pg_amb_fundo", sub[0]))),
            SimStep(arvore(0, nota("vl", "= $va")), tx("pg_amb_valor", va), pergunta(tx("pg_amb_q_valor"), "$va", chutes, 3, tx("pg_amb_valor", va))),
            SimStep(arvore(1, nota("pr", tx("pg_amb_primeiro", sub[1]))), tx("sims_agora_outra_arvore_com_mesma"), erro = true),
            SimStep(arvore(1, nota("vl", "= $vb")), tx("pg_amb_valor", vb), pergunta(tx("pg_amb_q_outra"), "$vb", chutes, 5, tx("pg_amb_valor", vb))),
            SimStep(arvore(1, nota("vl", "= $vb")), if (va != vb) tx("pg_amb_fim_diferente", va, vb) else tx("pg_amb_fim_igual", va)),
        )
        return Sim(tx("comum_duas_arvores"), passos, Skill.AMBIGUITY)
    }
}
