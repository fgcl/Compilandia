package com.compilandia.tutor

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.Look
import com.compilandia.content.Shot
import com.compilandia.content.Tint
import com.compilandia.content.Wire
import com.compilandia.content.alvo
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.fita
import com.compilandia.content.fitaTokens
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EOF
import com.compilandia.engine.EPS
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.GrammarGenerator
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Parser
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LLAction
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.Mips
import com.compilandia.engine.ParseNode
import com.compilandia.engine.Production
import com.compilandia.engine.RegexBank
import com.compilandia.engine.SentenceGenerator
import com.compilandia.engine.SetStep
import com.compilandia.engine.TacGen
import com.compilandia.engine.render
import com.compilandia.learning.Skill
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import com.compilandia.textos.tx

/** Pergunta do modo Prever: a animação para antes do passo. */
data class SimAsk(val pergunta: String, val opcoes: List<String>, val certa: Int, val porque: String)

data class SimStep(val shot: Shot, val say: String, val ask: SimAsk? = null, val erro: Boolean = false)

/** Uma simulação feita na hora com a entrada da pessoa. Todo passo sai do motor. */
data class Sim(val titulo: String, val passos: List<SimStep>, val skill: Skill? = null)

private fun tintDe(g: Grammar, s: String) = if (g.isNonTerminal(s)) Tint.ORANGE else if (s == EOF) Tint.NONE else Tint.TEAL

/** Monta [certa] e dois erros plausíveis em posições que variam com o passo. */
private fun opcoes(certa: String, erradas: List<String>, k: Int): Pair<List<String>, Int> {
    val outras = erradas.filter { it != certa }.distinct().take(2)
    val tudo = (listOf(certa) + outras).toMutableList()
    val rotaciona = k % tudo.size
    val lista = tudo.drop(rotaciona) + tudo.take(rotaciona)
    return lista to lista.indexOf(certa)
}

object Sims {
    // ------------------------------------------------------------ FIRST e FOLLOW

    fun conjuntos(g: Grammar, kind: SetStep.Kind): Sim {
        val ff = FirstFollow(g)
        val nome = if (kind == SetStep.Kind.FIRST) "FIRST" else "FOLLOW"
        val nts = g.nonTerminals.take(6)
        // A gramática inteira fica embaixo; os conjuntos crescem em cima.
        val alt = min(56f, 196f / nts.size)
        val y0 = 74f
        fun y(nt: String) = y0 + nts.indexOf(nt).coerceAtLeast(0) * alt
        val tam = min(46f, alt - 8f)
        val nos = nts.map { n -> Box("n$n", n, 40f, y(n), tam, tam, Look.STATE, Tint.ORANGE, size = min(16f, tam * 0.4f)) }
        val atuais = linkedMapOf<String, MutableList<String>>()
        fun chips(realce: Set<String>, alvoNt: String?): List<Box> = atuais.flatMap { (n, lista) ->
            val cada = min(36f, 270f / max(lista.size, 1))
            lista.mapIndexed { i, s ->
                Box("s$n-$s", s, 92f + i * cada, y(n), cada - 3f, min(30f, alt - 12f), Look.TOKEN, if (s == EPS) Tint.PINK else Tint.TEAL, hot = n == alvoNt && s in realce, size = min(13f, cada * 0.4f))
            }
        }
        val yG = y0 + nts.size * alt + 4f
        fun gramatica(lhsAceso: String?): List<Box> = nts.mapIndexed { i, nt ->
            val lados = g.productions.filter { it.lhs == nt }.joinToString(" | ") { pr -> pr.rhs.joinToString(" ").ifEmpty { EPS } }
            Box("gl$i", "$nt → $lados", 30f, yG + i * 24f, 310f, 22f, Look.CODE, if (nt == lhsAceso) Tint.YELLOW else Tint.NONE, hot = nt == lhsAceso, size = 14f)
        }
        val passos = mutableListOf<SimStep>()
        passos += SimStep(Shot(listOf(titulo(nome)) + nos + gramatica(null)), tx("sims_todos_comecam_vazios_vou_olhar"))
        val visitados = ff.steps.filter { it.kind == kind }.take(24)
        visitados.forEachIndexed { k, st ->
            val fonte = fonte(g, st, nts)
            val ask = perguntaConjunto(g, ff, st, nome, k)
            atuais.getOrPut(st.nonTerminal) { mutableListOf() } += st.added
            val fio = if (fonte != null && fonte != st.nonTerminal) listOf(Wire("n$fonte", "n${st.nonTerminal}", hot = true, bend = 30f, flow = st.added.toList())) else emptyList()
            passos += SimStep(
                Shot(listOf(titulo(nome)) + nos + chips(st.added, st.nonTerminal) + gramatica(st.production.lhs), fio),
                frase(st, nome), ask,
            )
        }
        passos += SimStep(Shot(listOf(titulo(nome)) + nos + chips(emptySet(), null) + gramatica(null)), tx("sims_rodei_tudo_nada_mudou_acabou"))
        return Sim(tx("sims_1_sua_gramatica", nome), passos, if (kind == SetStep.Kind.FIRST) Skill.FIRST else Skill.FOLLOW)
    }

    private fun fonte(g: Grammar, st: SetStep, nts: List<String>): String? {
        if (st.kind == SetStep.Kind.FIRST) return st.production.rhs.firstOrNull { g.isNonTerminal(it) && it in nts }
        return if (tx("sims_follow_1_entra", st.production.lhs) in st.reason) st.production.lhs else null
    }

    private fun frase(st: SetStep, nome: String): String {
        val alvo = st.nonTerminal
        val itens = st.added.joinToString(" e ")
        return when {
            nome == "FIRST" && EPS in st.added && st.added.size == 1 -> tx("sims_tudo_direita_1_pode_sumir", alvo)
            nome == "FIRST" && "FIRST(" in st.reason -> tx("sims_1_comeca_com_outro_nao", st.production, alvo, itens)
            nome == "FIRST" -> tx("sims_em_1_regra_abre_com", st.production, itens)
            tx("comum_simbolo_inicial") in st.reason -> tx("sims_1_inicio_depois_dele_vem", alvo)
            tx("sims_follow_1_entra", st.production.lhs) in st.reason -> tx("sims_1_fica_no_fim_2", alvo, st.production.lhs, st.production.lhs, itens)
            else -> tx("sims_em_1_depois_2_pode", st.production, alvo, itens)
        }
    }

    private fun perguntaConjunto(g: Grammar, ff: FirstFollow, st: SetStep, nome: String, k: Int): SimAsk {
        val certa = st.added.first()
        val (ops, idx) = opcoes(certa, (g.terminals + listOf(EOF, EPS)).shuffled(Random(k)), k)
        return SimAsk(tx("sims_que_entra_em_1_2", nome, st.nonTerminal), ops, idx, frase(st, nome))
    }

    // ------------------------------------------------------------ Tabela LL(1)

    fun tabelaLL(g: Grammar): Sim {
        val t = LL1Table(g)
        val cols = t.columns
        val rows = g.nonTerminals
        val cw = min(66f, 340f / (cols.size + 1))
        val ch = min(44f, 300f / (rows.size + 1))
        val x0 = 180f - cols.size * cw / 2
        val y0 = 76f
        val tam = min(13f, cw * 0.3f)
        val fixas = mutableListOf(titulo(tx("comum_tabela_ll_1_2")))
        cols.forEachIndexed { c, s -> fixas += Box("h$c", s, x0 + (c + 1) * cw, y0, cw - 3f, ch - 3f, Look.CELL, Tint.DARK, size = tam) }
        rows.forEachIndexed { l, s -> fixas += Box("r$l", s, x0, y0 + (l + 1) * ch, cw - 3f, ch - 3f, Look.CELL, Tint.ORANGE, size = tam) }
        for (l in rows.indices) for (c in cols.indices) fixas += Box("v$l-$c", "", x0 + (c + 1) * cw, y0 + (l + 1) * ch, cw - 3f, ch - 3f, Look.CELL, Tint.NONE)
        val passos = mutableListOf(SimStep(Shot(fixas.toList()), tx("sims_linha_para_cada_nao_terminal")))
        val cheias = mutableListOf<Box>()
        var k = 0
        for (l in rows.indices) for (c in cols.indices) {
            val rs = t.cell(rows[l], cols[c])
            if (rs.isEmpty()) continue
            val conflito = rs.size > 1
            cheias += Box(
                "c$l-$c", rs.joinToString("/") { it.rhsText() }, x0 + (c + 1) * cw, y0 + (l + 1) * ch, cw - 3f, ch - 3f, Look.TOKEN,
                if (conflito) Tint.RED else Tint.TEAL, hot = true, size = min(tam, (cw - 6f) / (rs.joinToString("/") { it.rhsText() }.length * 0.62f).coerceAtLeast(1f)).coerceAtLeast(6.5f),
            )
            val r = t.reasons[rows[l] to cols[c]].orEmpty().firstOrNull().orEmpty()
            val diz = if (conflito) tx("sims_conflito_1_regras_na_mesma", rs.size) else
                if ("ε" in r) tx("sims_1_pode_sumir_2_vem", rs[0].lhs, cols[c], rs[0]) else
                tx("sims_1_abre_2_regra_3", cols[c], rs[0].rhsText(), rs[0])
            // Na casa com conflito, a resposta certa são as regras todas, como a casa mostra.
            val certa = rs.joinToString("/") { it.rhsText() }
            val (ops, idx) = opcoes(certa, g.productionsOf(rows[l]).map { it.rhsText() } + listOf(tx("sims_deixar_vazia")), k)
            passos += SimStep(
                Shot(fixas + cheias.map { if (it === cheias.last()) it else it.copy(hot = false) }), diz,
                if (k % 2 == 0 || conflito) SimAsk(tx("sims_que_vai_na_casa_1", rows[l], cols[c]), ops, idx, diz) else null,
                erro = conflito,
            )
            k++
            if (k >= 30) break
        }
        return Sim(tx("sims_montando_tabela"), passos, Skill.LL1_TABLE)
    }

    // ------------------------------------------------------------ LL(1) rodando

    fun parseLL(g: Grammar, tokens: List<String>): Sim {
        val tab = LL1Table(g)
        val trace = LL1Parser.trace(tab, tokens, 90)
        val tudo = tokens + EOF
        var seq = 0
        var ids = List(2) { seq++ }
        val passos = mutableListOf<SimStep>()
        trace.forEachIndexed { k, st ->
            val pos = tudo.size - st.input.size
            val pares = st.stack.zip(ids).takeLast(8)
            val pilha = pares.mapIndexed { i, (s, id) ->
                Box("p$id", s, 104f, 372f - i * 39f, 96f, 35f, Look.TOKEN, tintDe(g, s), hot = i == pares.lastIndex, size = 15f)
            }
            val a = st.input.first()
            val topo = st.stack.last()
            val acao = st.action
            val (diz, painel) = when (acao) {
                is LLAction.Expand -> tx("sims_topo_1_vem_2_tabela", topo, a, acao.production.rhsText()) to "M[$topo, $a]\n$topo → ${acao.production.rhsText()}"
                is LLAction.Match -> tx("sims_topo_entrada_sao_1_casou", a) to tx("sims_casar_1", a)
                LLAction.Accept -> tx("sims_pilha_entrada_acabaram_juntas_frase") to tx("sims_aceita")
                is LLAction.Error -> tx("sims_travou_1_frase_nao_pertence", acao.message) to tx("sims_erro")
            }
            val cor = when (acao) { is LLAction.Error -> Tint.RED; LLAction.Accept -> Tint.GREEN; else -> Tint.YELLOW }
            val caixa = Box("m", painel, 262f, 158f, 168f, 62f, Look.CELL, cor, size = 13f)
            val ask = when (acao) {
                is LLAction.Expand -> {
                    val (ops, idx) = opcoes(tx("sims_trocar_por_1", acao.production.rhsText()), listOf(tx("sims_casar_1_2", a)) + g.productionsOf(topo).map { tx("sims_trocar_por_1", it.rhsText()) } + tx("sims_dar_erro"), k)
                    SimAsk(tx("sims_topo_1_entrada_2_que", topo, a), ops, idx, diz)
                }
                is LLAction.Match -> {
                    val (ops, idx) = opcoes(tx("sims_casar_1_2", a), listOf(tx("sims_dar_erro"), tx("sims_trocar_1", topo)), k)
                    SimAsk(tx("sims_topo_1_entrada_2_que", topo, a), ops, idx, diz)
                }
                else -> null
            }
            passos += SimStep(
                Shot(fitaTokens(tudo, pos, 46f) + rotulo("rp", tx("sims_pilha"), 104f, 396f) + pilha + caixa, actor = Actor(290f, 372f, 0.82f)),
                diz, if (k % 2 == 0) ask else null, erro = acao is LLAction.Error,
            )
            when (acao) {
                is LLAction.Expand -> ids = ids.dropLast(1) + acao.production.rhs.map { seq++ }
                is LLAction.Match -> ids = ids.dropLast(1)
                else -> {}
            }
        }
        return Sim(tx("sims_rodando_ll_1"), passos, Skill.LL1_PARSING)
    }

    // ------------------------------------------------------------ Empilha-reduz

    fun parseLR(g: Grammar, tokens: List<String>, kind: LRKind = LRKind.SLR1): Sim {
        val tab = LRTable(g, kind)
        val r = LRParser.parse(tab, tokens, 120)
        val tudo = tokens + EOF
        var seq = 0
        // Cada peça da pilha guarda o id: o token sobe da fita para a pilha sem trocar de identidade.
        var ids = emptyList<String>()
        val passos = mutableListOf<SimStep>()
        r.steps.forEachIndexed { k, st ->
            val pos = tudo.size - st.input.size
            val n = st.symbols.size
            val cada = min(54f, 340f / max(n, 1))
            val x0 = 180f - (n - 1) * cada / 2
            val acao = st.action
            val alvoN = (acao as? LRAction.Reduce)?.let { tab.grammar.productions[it.prodId].rhs.size } ?: 0
            val pilha = st.symbols.mapIndexed { i, s ->
                Box(ids[i], s, x0 + i * cada, 240f, cada - 4f, 44f, Look.TOKEN, tintDe(g, s), hot = i >= n - alvoN, size = min(15f, cada * 0.4f))
            }
            val a = st.input.first()
            val (diz, painel, cor) = when (acao) {
                is LRAction.Shift -> Triple(tx("sims_vem_1_topo_ainda_nao", a), tx("sims_empilhar_1", a), Tint.YELLOW)
                is LRAction.Reduce -> {
                    val p = tab.grammar.productions[acao.prodId]
                    Triple(tx("sims_topo_forma_1_reduz_para", p.rhsText(), p.lhs), tx("sims_reduzir_1", p), Tint.ORANGE)
                }
                LRAction.Accept -> Triple(tx("sims_sobrou_so_1_entrada_acabou", g.start), tx("sims_aceita"), Tint.GREEN)
                null -> Triple(tx("sims_sem_acao_na_tabela_com", a), tx("sims_erro"), Tint.RED)
                else -> Triple(st.note, "", Tint.NONE)
            }
            val fita = fitaTokens(tudo, pos, 60f).filterIndexed { i, _ -> i >= pos }
            val caixa = Box("m", painel, 180f, 150f, 230f, 56f, Look.CELL, cor, size = 14f)
            val ask = when (acao) {
                is LRAction.Shift -> { val (o, i) = opcoes(tx("comum_empilhar_2"), listOf(tx("comum_reduzir")), k); SimAsk(tx("sims_vem_1_que_maquina_faz", a), o, i, diz) }
                is LRAction.Reduce -> { val (o, i) = opcoes(tx("comum_reduzir"), listOf(tx("comum_empilhar_2")), k); SimAsk(tx("sims_vem_1_que_maquina_faz", a), o, i, diz) }
                else -> null
            }
            passos += SimStep(
                Shot(fita + caixa + rotulo("rp", tx("sims_pilha"), 180f, 276f) + pilha, actor = Actor(310f, 372f, 0.72f)),
                diz, if (k % 2 == 1) ask else null, erro = acao == null,
            )
            when (acao) {
                is LRAction.Shift -> ids = ids + "e$pos"
                is LRAction.Reduce -> {
                    val p = tab.grammar.productions[acao.prodId]
                    ids = ids.dropLast(p.rhs.size) + "n${seq++}"
                }
                else -> {}
            }
        }
        return Sim(tx("sims_rodando_empilha_reduz"), passos, Skill.SHIFT_REDUCE)
    }

    // ------------------------------------------------------------ Árvores

    private fun parcial(g: Grammar, deriv: List<Production>, k: Int): ParseNode {
        var i = 0
        fun build(sym: String): ParseNode {
            if (!g.isNonTerminal(sym) || i >= k) return ParseNode(sym)
            val p = deriv[i++]
            val filhos = if (p.rhs.isEmpty()) listOf(ParseNode(EPS)) else p.rhs.map { build(it) }
            return ParseNode(sym, filhos)
        }
        return build(g.start)
    }

    private fun desenhaArvore(n: ParseNode, y0: Float = 60f, alturaUtil: Float = 310f, g: Grammar? = null): Pair<List<Box>, List<Wire>> {
        val folhas = n.leaves().size.coerceAtLeast(1) + 1
        val passoY = min(56f, alturaUtil / max(n.depth() - 1, 1))
        val (bs, ws) = arvore(n, y0, passoY)
        val larg = 330f / folhas
        val ajust = bs.map { b ->
            val w = min(b.w, max(26f, larg - 3f))
            val pendente = g != null && g.isNonTerminal(b.text) && !b.id.let { id -> ws.any { it.from == id } }
            b.copy(w = w, size = min(14f, w / (b.text.length * 0.66f + 0.6f)), h = 28f, tint = if (pendente) Tint.YELLOW else b.tint, hot = pendente)
        }
        return ajust to ws
    }

    fun derivacao(g: Grammar, tokens: List<String>): Sim? {
        val d = DerivationHelper.leftmostDerivations(g, tokens, limit = 1).firstOrNull() ?: return null
        val passos = mutableListOf<SimStep>()
        for (k in 0..d.size) {
            val t = parcial(g, d, k)
            val (bs, ws) = desenhaArvore(t, g = g)
            val diz = if (k == 0) tx("sims_comeco_por_1_troco_sempre", g.start)
            else tx("sims_troco_1_por_2", d[k - 1].lhs, d[k - 1].rhsText())
            val ask = if (k < d.size && k % 2 == 0) {
                val (ops, idx) = opcoes(d[k].rhsText(), g.productionsOf(d[k].lhs).map { it.rhsText() }, k)
                SimAsk(tx("sims_para_chegar_na_frase_1", d[k].lhs), ops, idx, tx("sims_so_1_ainda_chega_em", d[k].rhsText(), tokens.joinToString(" ")))
            } else null
            passos += SimStep(Shot(bs, ws), diz, ask)
        }
        passos += SimStep(passos.last().shot, tx("sims_folhas_esquerda_para_direita_formam", tokens.joinToString(" ")))
        return Sim(tx("sims_arvore_frase"), passos, Skill.CFG_DERIVATION)
    }

    fun ambigua(g: Grammar, tokens: List<String>): Sim? {
        val ds = DerivationHelper.leftmostDerivations(g, tokens, limit = 2)
        if (ds.size < 2) return null
        val a = desenhaArvore(DerivationHelper.treeOf(g, ds[0]))
        val b = desenhaArvore(DerivationHelper.treeOf(g, ds[1]))
        val rot = rotulo("fr", tokens.joinToString(" "), 180f, 30f, Tint.YELLOW, 18f)
        return Sim(
            tx("comum_duas_arvores"), listOf(
                SimStep(Shot(listOf(rot) + a.first, a.second), tx("sims_olha_arvore_para_esta_frase")),
                SimStep(Shot(listOf(rot) + b.first, b.second), tx("sims_agora_outra_arvore_com_mesma"), erro = true),
                SimStep(Shot(listOf(rot) + a.first, a.second), tx("sims_primeira_agrupa_jeito_volte_compare")),
                SimStep(Shot(listOf(rot) + b.first, b.second), tx("sims_mais_fundo_roda_primeiro_por")),
            ),
            Skill.AMBIGUITY,
        )
    }

    // ------------------------------------------------------------ Transformações

    /** Uma regra em pecinhas. As peças têm id próprio, para voarem até a regra nova. */
    private fun linha(y: Float, itens: List<Pair<String, String>>, tint: (String) -> Tint): List<Box> {
        val larguras = itens.map { (_, t) -> max(28f, t.length * 10f + 12f) }
        val total = larguras.sum() + (itens.size - 1) * 4f
        val k = min(1f, 340f / total)
        var x = 180f - total * k / 2
        return itens.mapIndexed { i, (id, t) ->
            val w = larguras[i] * k
            val b = Box(id, t, x + w / 2, y, w - 1f, 34f, Look.TOKEN, tint(id), size = min(14f, 14f * k + 1f))
            x += w + 4f * k
            b
        }
    }

    /** O parser preso: a regra se abre, e o primeiro símbolo é sempre ela de novo. */
    fun laco(g: Grammar, nt: String): Sim? {
        val p = g.productionsOf(nt).firstOrNull { it.rhs.firstOrNull() == nt } ?: return null
        val resto = p.rhs.drop(1)
        var forma = listOf(nt)
        val passos = mutableListOf<SimStep>()
        repeat(5) { k ->
            val itens = forma.take(10)
            val cada = min(56f, 330f / itens.size)
            val x0 = 180f - (itens.size - 1) * cada / 2
            val pecas = itens.mapIndexed { i, s ->
                Box("f$i", s, x0 + i * cada, 180f, cada - 4f, 46f, Look.TOKEN, if (i == 0) Tint.RED else tintDe(g, s), hot = i == 0, size = min(16f, cada * 0.45f))
            }
            passos += SimStep(Shot(pecas), if (k == 0) tx("sims_parser_olha_1_precisa_decidir", nt) else tx("sims_1_virou_2_3_primeiro", nt, nt, resto.joinToString(" "), nt), erro = k == 4)
            forma = listOf(nt) + resto + forma.drop(1)
        }
        return Sim(tx("sims_preso_em_1", nt), passos, Skill.LEFT_RECURSION)
    }

    fun recursao(g: Grammar): Sim? {
        val nt = GrammarTransform.leftRecursiveNTs(g).firstOrNull() ?: return null
        val ps = g.productionsOf(nt)
        val recs = ps.filter { it.rhs.firstOrNull() == nt }
        val bases = ps.filter { it.rhs.firstOrNull() != nt }
        val alfa = recs.first().rhs.drop(1)
        val beta = bases.firstOrNull()?.rhs ?: emptyList()
        val novo = "$nt'"
        val orig = buildList {
            add("a0" to nt); add("seta" to "->"); add("a1" to nt)
            alfa.forEachIndexed { i, s -> add("al$i" to s) }
            add("bar" to "|")
            if (beta.isEmpty()) add("be0" to EPS) else beta.forEachIndexed { i, s -> add("be$i" to s) }
        }
        fun tint(id: String) = when {
            id.startsWith("al") -> Tint.ORANGE
            id.startsWith("be") -> Tint.TEAL
            id == "a1" -> Tint.RED
            id == "bar" || id.startsWith("seta") -> Tint.NONE
            else -> Tint.YELLOW
        }
        val cab = listOf(titulo(tx("sims_tirando_recursao")))
        val r1 = linha(150f, orig) { tint(it) }
        val d1 = linha(150f, buildList {
            add("a0" to nt); add("seta" to "->")
            if (beta.isEmpty()) add("be0" to EPS) else beta.forEachIndexed { i, s -> add("be$i" to s) }
            add("n1" to novo)
        }) { if (it == "n1") Tint.PINK else tint(it) }
        val d2 = linha(220f, buildList {
            add("n0" to novo); add("seta2" to "->")
            alfa.forEachIndexed { i, s -> add("al$i" to s) }
            add("n2" to novo); add("bar" to "|"); add("eps" to EPS)
        }) { if (it.startsWith("n")) Tint.PINK else if (it == "eps") Tint.GREEN else tint(it) }
        val final = GrammarTransform.eliminateImmediateLeftRecursion(g)
        val lista = codigo("g", final.toText().lines().take(6), 290f, size = 13f)
        return Sim(
            tx("sims_consertando_recursao"), listOf(
                SimStep(Shot(cab + r1), tx("sims_esta_regra_comeca_com_ela")),
                SimStep(Shot(cab + r1.map { it.copy(hot = it.id.startsWith("al")) }), tx("sims_pedaco_laranja_que_repete_1", alfa.joinToString(" "))),
                SimStep(Shot(cab + r1.map { it.copy(hot = it.id.startsWith("be")) }), tx("sims_pedaco_verde_por_onde_regra", if (beta.isEmpty()) EPS else beta.joinToString(" "))),
                SimStep(Shot(cab + d1), tx("sims_primeiro_vem_que_termina_depois")),
                SimStep(Shot(cab + d1 + d2), tx("sims_regra_nova_repete_laranja_chama")),
                SimStep(Shot(cab + d1 + d2 + lista), tx("sims_linguagem_mesma_parser_agora_sabe")),
            ),
            Skill.LEFT_RECURSION,
        )
    }

    fun fatorar(g: Grammar): Sim? {
        val (nt, grupo) = g.nonTerminals.firstNotNullOfOrNull { n ->
            g.productionsOf(n).filter { it.rhs.isNotEmpty() }.groupBy { it.rhs.first() }.values.firstOrNull { it.size > 1 }?.let { n to it }
        } ?: return null
        var comum = 0
        while (grupo.all { it.rhs.size > comum && it.rhs[comum] == grupo[0].rhs[comum] }) comum++
        val pre = grupo[0].rhs.take(comum)
        val novo = "$nt'"
        val cab = listOf(titulo(tx("sims_fatorando")))
        val ids = mutableListOf<Pair<String, String>>()
        ids += "a0" to nt; ids += "seta" to "->"
        grupo.forEachIndexed { i, p ->
            if (i > 0) ids += "bar$i" to "|"
            p.rhs.forEachIndexed { j, s -> ids += (if (j < comum) "pre$i-$j" else "res$i-$j") to s }
        }
        fun tint(id: String) = when {
            id.startsWith("pre") -> Tint.ORANGE
            id.startsWith("res") -> Tint.TEAL
            id.startsWith("bar") || id.startsWith("seta") -> Tint.NONE
            else -> Tint.YELLOW
        }
        val r1 = linha(150f, ids) { tint(it) }
        val d1 = linha(150f, buildList {
            add("a0" to nt); add("seta" to "->")
            pre.forEachIndexed { j, s -> add("pre0-$j" to s) }
            add("n1" to novo)
        }) { if (it == "n1") Tint.PINK else tint(it) }
        val d2 = linha(220f, buildList {
            add("n0" to novo); add("seta2" to "->")
            grupo.forEachIndexed { i, p ->
                if (i > 0) add("bar$i" to "|")
                val resto = p.rhs.drop(comum)
                if (resto.isEmpty()) add("res$i-e" to EPS) else resto.forEachIndexed { j, s -> add("res$i-${j + comum}" to s) }
            }
        }) { if (it.startsWith("n")) Tint.PINK else tint(it) }
        val prefTxt = pre.joinToString(" ")
        return Sim(
            tx("sims_fatorando"), listOf(
                SimStep(Shot(cab + r1), tx("sims_duas_opcoes_comecam_igual_1", prefTxt)),
                SimStep(Shot(cab + r1.map { it.copy(hot = it.id.startsWith("pre")) }), tx("sims_olhando_simbolo_so_parser_nao")),
                SimStep(Shot(cab + d1), tx("sims_entao_le_comeco_igual_vez")),
                SimStep(Shot(cab + d1 + d2), tx("sims_regra_nova_guarda_so_que")),
            ),
            Skill.LEFT_FACTORING,
        )
    }

    // ------------------------------------------------------------ Autômato

    fun posicoes(n: Int): List<Pair<Float, Float>> {
        if (n == 1) return listOf(180f to 150f)
        if (n == 2) return listOf(90f to 150f, 270f to 150f)
        if (n == 3) return listOf(60f to 160f, 180f to 160f, 300f to 160f)
        return List(n) { i ->
            val a = PI * (i.toDouble() / n * 2 - 0.5 - 1.0 / n)
            (180f + 130f * cos(a).toFloat()) to (160f + 112f * sin(a).toFloat())
        }
    }

    fun afd(regex: String, entrada: String): Sim {
        val d = DFA.fromRegex(regex)
        val n = d.stateCount.coerceAtMost(9)
        val pos = posicoes(n)
        val est = pos.mapIndexed { i, (x, y) ->
            Box("q$i", "q$i", x, y, 44f, 44f, if (i in d.accepting) Look.ACCEPT else Look.STATE, size = 14f)
        }
        val pares = d.delta.entries.filter { it.key.first < n && it.value < n }.groupBy({ it.key.first to it.value }, { it.key.second })
        val fios = pares.map { (p, l) ->
            Wire("q${p.first}", "q${p.second}", l.sorted().joinToString(","), bend = if (p.first != p.second && (p.second to p.first) in pares) 26f else 0f)
        }
        val caminho = d.run(entrada)
        fun quadro(k: Int): Shot {
            val s = caminho[k]
            val cel = min(28f, 320f / max(entrada.length, 1))
            val fit = fita(entrada.ifEmpty { " " }, 330f, cel).mapIndexed { i, b -> b.copy(tint = if (i < k) Tint.DARK else if (i == k) Tint.YELLOW else Tint.NONE, hot = i == k) }
            val (ax, ay) = pos.getOrElse(s) { 180f to 160f }
            return Shot(
                est.map { if (it.id == "q$s") it.copy(tint = Tint.YELLOW) else it } + fit,
                fios.map { if (k > 0 && it.from == "q${caminho[k - 1]}" && it.to == "q$s") it.copy(hot = true) else it },
                if (s in 0 until n) Actor(ax, ay - 14f, 0.58f) else null,
            )
        }
        val passos = mutableListOf(SimStep(quadro(0), tx("sims_trem_comeca_em_q0_le")))
        for (k in 1..entrada.length) {
            val c = entrada[k - 1]
            val s = caminho[k]
            if (s < 0) {
                passos += SimStep(quadro(k), tx("sims_nao_ha_seta_com_1", c), erro = true)
                return Sim(tx("sims_automato_1", regex), passos, Skill.REGEX_DFA)
            }
            val (ops, idx) = opcoes("q$s", List(n) { "q$it" }.shuffled(Random(k)), k)
            passos += SimStep(quadro(k), tx("sims_leu_1_vai_q_2", c, caminho[k - 1], s), SimAsk(tx("sims_leu_1_para_onde_vai", c), ops, idx, tx("sims_seta_com_1_leva_para", c, s)).takeIf { k % 2 == 1 })
        }
        val fim = caminho.last()
        passos += SimStep(passos.last().shot, if (fim in d.accepting) tx("sims_acabou_fita_em_circulo_duplo") else tx("sims_acabou_fita_fora_circulo_duplo"), erro = fim !in d.accepting)
        return Sim(tx("sims_automato_1", regex), passos, Skill.REGEX_DFA)
    }

    // ------------------------------------------------------------ Tokens

    fun tokens(linhaCod: String): Sim {
        val src = linhaCod.replace("\n", " ").take(26)
        val toks = try { MiniLexer.tokenize(src) } catch (e: Exception) { emptyList() }
        val cel = min(26f, 340f / max(src.length, 1))
        fun tint(c: com.compilandia.engine.TokenClass) = when (c) {
            com.compilandia.engine.TokenClass.ID -> Tint.TEAL
            com.compilandia.engine.TokenClass.NUM -> Tint.YELLOW
            com.compilandia.engine.TokenClass.OP -> Tint.ORANGE
            com.compilandia.engine.TokenClass.KEYWORD -> Tint.PINK
            com.compilandia.engine.TokenClass.DELIM -> Tint.BLUE
            com.compilandia.engine.TokenClass.STRING -> Tint.GREEN
        }
        val base = fita(src, 90f, cel)
        val feitos = mutableListOf<Box>()
        val passos = mutableListOf(SimStep(Shot(base + rotulo("r", "tokens", 180f, 150f)), tx("sims_scanner_anda_esquerda_para_direita")))
        toks.forEachIndexed { k, t ->
            val fit = base.mapIndexed { i, b -> b.copy(tint = if (i in t.start until t.end) Tint.YELLOW else if (i < t.start) Tint.DARK else Tint.NONE, hot = i in t.start until t.end) }
            feitos += Box("tk$k", t.lexeme, 56f + (k % 4) * 83f, 188f + (k / 4) * 48f, 76f, 40f, Look.TOKEN, tint(t.cls), size = min(14f, 72f / (t.lexeme.length * 0.66f + 0.5f)))
            val rotuloCls = Box("cls", t.cls.label, 180f, 136f, 200f, 28f, Look.LABEL, tint(t.cls), size = 18f)
            val ask = if (k % 2 == 0) {
                val (ops, idx) = opcoes(t.cls.label, com.compilandia.engine.TokenClass.entries.map { it.label }.shuffled(Random(k)), k)
                SimAsk(tx("sims_que_classe_1", t.lexeme), ops, idx, tx("sims_1_2", t.lexeme, t.cls.label))
            } else null
            passos += SimStep(Shot(fit + feitos + rotuloCls), tx("sims_1_2", t.lexeme, t.cls.label), ask)
        }
        return Sim(tx("sims_cortando_em_tokens"), passos, Skill.TOKENS)
    }

    // ------------------------------------------------------------ Expressão até o MIPS

    private fun noDe(e: Expr): ParseNode = ParseNode(e.text, e.children.map { noDe(it) })

    fun expressao(src: String): Sim {
        val e = ExprParser.parse(src)
        val tac = TacGen.generate(e)
        val (bs, ws) = desenhaArvore(noDe(e), 56f, 170f)
        val passos = mutableListOf(SimStep(Shot(bs, ws), tx("sims_parser_ja_montou_arvore_que")))
        var atual = bs
        val linhas = mutableListOf<String>()
        fun caminho(e0: Expr, alvo: Expr, id: String): String? {
            if (e0 === alvo) return id
            e0.children.forEachIndexed { i, c -> caminho(c, alvo, "$id$i")?.let { return it } }
            return null
        }
        val internos = e.postorder().filter { !it.isLeaf }
        internos.forEachIndexed { k, n ->
            val id = caminho(e, n, "t")
            val ins = tac.getOrNull(k) ?: return@forEachIndexed
            linhas += ins.toString()
            atual = atual.map { if (it.id == id) it.copy(text = ins.target, tint = Tint.YELLOW, hot = true) else it.copy(hot = false) }
            // Pergunta do Prever: qual conta vem agora? As erradas são as outras contas da árvore.
            val (ops, certa) = opcoes(n.render(), internos.filter { it !== n }.map { it.render() }.reversed() + e.render(), k)
            val ask = SimAsk(tx("sims_ask_tac"), ops, certa, tx("sims_ask_tac_porque", n.render()))
            passos += SimStep(Shot(atual + codigo("l", linhas.toList(), 256f, size = 13f), ws), tx("sims_calculo_1_guardo_em_2", n.render(), ins.target), ask)
        }
        val asm = try { Mips.fromTac(tac) } catch (x: Exception) { emptyList() }
        if (asm.isNotEmpty()) {
            passos += SimStep(Shot(codigo("m", asm.take(10), 60f, size = 13f)), tx("sims_cada_linha_vira_instrucoes_mips"))
        }
        return Sim(tx("sims_expressao_ao_mips"), passos, Skill.TAC)
    }

    // ------------------------------------------------------------ Autômato LR(0)

    fun lr0(g: Grammar): Sim {
        val a = LRAutomaton.lr0(g)
        val colunas = 4
        val passos = mutableListOf<SimStep>()
        val vistos = mutableListOf<Box>()
        val fios = mutableListOf<Wire>()
        val estados = a.states.take(16)
        fun xy(i: Int) = (56f + (i % colunas) * 83f) to (222f + (i / colunas) * 52f)
        estados.forEachIndexed { k, st ->
            val (x, y) = xy(k)
            vistos += Box("I$k", "$k", x, y, 40f, 40f, Look.STATE, size = 14f)
            a.transitions.entries.filter { it.value == k && it.key.first < k && estados.size > it.key.first }.take(2).forEach {
                fios += Wire("I${it.key.first}", "I$k", it.key.second)
            }
            val itens = st.items.toList().take(7).map { it.render(a.grammar) }
            val painel = itens.mapIndexed { i, s -> Box("it$i", s, 24f, 44f + i * 19f, 320f, 18f, Look.CODE, size = 12f) }
            val completos = st.items.count { it.dot >= a.grammar.productions[it.prodId].rhs.size }
            val diz = when {
                k == 0 -> tx("sims_estado_0_comeca_antes_tudo")
                completos > 0 && st.items.size > completos -> tx("sims_estado_1_ponto_chegou_ao", k)
                completos > 0 -> tx("sims_estado_1_ponto_chegou_ao_2", k)
                else -> tx("sims_estado_1_andei_ponto_sobre", k)
            }
            // Pergunta do Prever: o estado tem item completo? É o que decide se ali dá para reduzir.
            val ask = if (k == 0) null else {
                val sim = completos > 0
                SimAsk(
                    tx("sims_ask_lr0", k), listOf(tx("sims_ask_lr0_sim"), tx("sims_ask_lr0_nao")), if (sim) 0 else 1,
                    if (sim) tx("sims_ask_lr0_porque_sim") else tx("sims_ask_lr0_porque_nao"),
                )
            }
            passos += SimStep(Shot(painel + vistos.map { if (it.id == "I$k") it.copy(tint = Tint.YELLOW, hot = true) else it }, fios.toList()), diz, ask)
        }
        return Sim(tx("sims_construindo_estados_lr_0"), passos, Skill.LR0_ITEMS)
    }

    // ------------------------------------------------------------ Outro exemplo

    /** Gera um exemplo novo do assunto. Null quando o assunto ainda não tem simulação. */
    fun exemplo(s: Skill, rnd: Random): Sim? = try {
        fun ll(): Grammar {
            repeat(12) { val g = GrammarGenerator.random(rnd, requireLL1 = true); if (SentenceGenerator.generate(g, rnd, maxTokens = 7) != null) return g }
            return GrammarBank.exprLL1.grammar
        }
        when (s) {
            Skill.TOKENS, Skill.LEX_FLEX -> tokens(MiniLexer.randomLine(rnd, 2))
            Skill.REGEX_DFA -> {
                val e = RegexBank.entries.random(rnd)
                val d = DFA.fromRegex(e.regex)
                afd(e.regex, d.samples(rnd, 4).firstOrNull { it.second }?.first ?: "")
            }
            Skill.FIRST -> conjuntos(ll(), SetStep.Kind.FIRST)
            Skill.FOLLOW -> conjuntos(ll(), SetStep.Kind.FOLLOW)
            Skill.LL1_TABLE -> tabelaLL(ll())
            Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT -> ll().let { g -> parseLL(g, SentenceGenerator.generate(g, rnd, maxTokens = 7) ?: emptyList()) }
            Skill.CFG_DERIVATION -> ll().let { g -> derivacao(g, SentenceGenerator.generate(g, rnd, maxTokens = 7) ?: emptyList()) }
            Skill.AMBIGUITY -> GrammarBank.exprAmbiguous.grammar.let { g ->
                listOf(listOf("id", "+", "id", "*", "id"), listOf("id", "*", "id", "+", "id"), listOf("id", "+", "id", "+", "id")).random(rnd).let { ambigua(g, it) }
            }
            Skill.LEFT_RECURSION -> recursao(GrammarBank.exprLeftRec.grammar)
            Skill.LEFT_FACTORING -> fatorar(Grammar.parse(listOf("S -> if E then S | if E then S else S | a\nE -> b", "A -> a b c | a b d | e", "L -> id , L | id").random(rnd)))
            Skill.SHIFT_REDUCE, Skill.SLR_TABLE -> GrammarBank.exprLeftRec.grammar.let { g -> parseLR(g, SentenceGenerator.generate(g, rnd, maxTokens = 7) ?: listOf("id")) }
            Skill.LR0_ITEMS -> lr0(Grammar.parse(listOf("E -> E + n | n", "S -> ( S ) | id", "L -> L , id | id", "E -> E * n | n", "A -> id = E\nE -> E + n | n").random(rnd)))
            Skill.TAC, Skill.ATTRIBUTES_TYPES, Skill.AST_BUILD, Skill.CODEGEN_MIPS, Skill.OPTIMIZATION -> expressao(ExprGenerator.random(rnd, 2))
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

@Suppress("unused") private val keepAlvo = ::alvo
