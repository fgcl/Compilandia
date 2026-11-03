package com.compilandia.content

import com.compilandia.engine.DFA
import com.compilandia.engine.EOF
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Parser
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LLAction
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.Production
import com.compilandia.engine.SententialForm
import com.compilandia.engine.render

/**
 * Um brinquedo do palco. A pessoa aperta uma tecla, o motor calcula, e a imagem nova sai de [shot].
 * Nada aqui é decorado: se o algoritmo mudar, o brinquedo muda junto.
 */
interface Toy {
    /** O que a pessoa precisa conseguir, em uma linha. */
    val meta: String
    val teclas: List<String>
    val shot: Shot
    val pronto: Boolean
    /** Comentário curto da última jogada. */
    val aviso: String?
    /** Devolve false quando a jogada não vale. */
    fun aperta(tecla: String): Boolean
    fun reinicia()
}

/** Fita de tokens: o que já foi lido some, a cabeça fica acesa. */
internal fun fitaTokens(tokens: List<String>, pos: Int, y: Float, prefixo: String = "e"): List<Box> {
    val cada = minOf(58f, 340f / tokens.size.coerceAtLeast(1))
    val x0 = 180f - (tokens.size - 1) * cada / 2
    return tokens.mapIndexed { i, t ->
        Box("$prefixo$i", t, x0 + i * cada, y, cada - 4f, 36f, Look.TOKEN, if (i < pos) Tint.DARK else if (i == pos) Tint.YELLOW else Tint.NONE, hot = i == pos, size = minOf(15f, cada * 0.42f))
    }
}

/** Pilota um autômato: cada letra vira uma transição calculada pelo motor. */
class ToyTrem(
    private val d: DFA,
    private val posicoes: List<Pair<Float, Float>>,
    override val meta: String,
    private val tamanho: Int,
) : Toy {
    private var lidas = ""
    override val teclas: List<String> = d.alphabet.map { it.toString() }
    override var aviso: String? = null
        private set

    private val estado get() = d.run(lidas).last()
    override val pronto: Boolean get() = lidas.length == tamanho && estado in d.accepting

    override val shot: Shot
        get() {
            val est = estados(posicoes, d.accepting).map { if (it.id == "q$estado") it.copy(tint = Tint.YELLOW, hot = true) else it }
            val pares = d.delta.entries.groupBy({ it.key.first to it.value }, { it.key.second })
            val fios = pares.map { (par, letras) ->
                Wire("q${par.first}", "q${par.second}", letras.sorted().joinToString(","), bend = if (par.first != par.second && (par.second to par.first) in pares) 24f else 0f)
            }
            val fita = List(tamanho) { i -> Box("c$i", lidas.getOrNull(i)?.toString() ?: "", 180f + (i - (tamanho - 1) / 2f) * 48f, 300f, 44f, 48f, Look.CELL, if (i < lidas.length) Tint.YELLOW else Tint.NONE, size = 20f) }
            val (ax, ay) = posicoes.getOrElse(estado) { 180f to 120f }
            val ator = if (estado >= 0) Actor(ax, ay - 16f, 0.62f) else null
            return Shot(est + fita, fios, ator)
        }

    override fun aperta(tecla: String): Boolean {
        val c = tecla.first()
        if (lidas.length >= tamanho || d.step(estado, c) < 0) {
            aviso = if (lidas.length >= tamanho) "A fita acabou. Recomece." else "Daqui não sai seta com essa letra."
            if (lidas.length >= tamanho) reinicia()
            return false
        }
        lidas += c
        aviso = if (pronto) "Chegou." else if (lidas.length == tamanho) "Acabou a fita fora do círculo duplo." else null
        return !(lidas.length == tamanho && !pronto)
    }

    override fun reinicia() { lidas = ""; aviso = null }
}

/** Opera o empilha-reduz: a tabela do motor diz o que era certo. */
class ToyEmpilhaReduz(g: Grammar, private val entrada: List<String>, override val meta: String) : Toy {
    private val passos = LRParser.parse(LRTable(g, LRKind.SLR1), entrada).steps
    private var k = 0
    override val teclas = listOf("Empilhar", "Reduzir")
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = passos.getOrNull(k)?.action == LRAction.Accept

    override val shot: Shot
        get() {
            val p = passos[k.coerceAtMost(passos.lastIndex)]
            val tudo = entrada + EOF
            val pos = tudo.size - p.input.size
            val fita = fitaTokens(tudo, pos, 40f)
            val pilha = p.symbols.mapIndexed { i, s -> Box("p$i-$s", s, 180f, 340f - i * 40f, 84f, 36f, Look.TOKEN, if (s.first().isUpperCase()) Tint.ORANGE else Tint.TEAL) }
            return Shot(listOf(rotulo("rp", "pilha", 180f, 374f)) + fita + pilha)
        }

    override fun aperta(tecla: String): Boolean {
        val a = passos.getOrNull(k)?.action
        val certo = when (a) {
            is LRAction.Shift -> tecla == "Empilhar"
            is LRAction.Reduce -> tecla == "Reduzir"
            else -> false
        }
        aviso = if (certo) null else if (a is LRAction.Shift) "Ainda não forma regra. Empilhe." else "O topo já forma uma regra. Reduza."
        if (certo) k++
        return certo
    }

    override fun reinicia() { k = 0; aviso = null }
}

/** Opera a pilha do LL(1): expandir o topo ou casar com a entrada. */
class ToyPilhaLL(g: Grammar, private val entrada: List<String>, override val meta: String) : Toy {
    private val passos = LL1Parser.trace(LL1Table(g), entrada)
    private var k = 0
    override val teclas = listOf("Trocar pela regra", "Casar")
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = passos.getOrNull(k)?.action == LLAction.Accept

    override val shot: Shot
        get() {
            val p = passos[k.coerceAtMost(passos.lastIndex)]
            val tudo = entrada + EOF
            val fita = fitaTokens(tudo, tudo.size - p.input.size, 40f)
            val pilha = p.stack.takeLast(7).mapIndexed { i, s -> Box("p$i-$s", s, 180f, 340f - i * 40f, 84f, 36f, Look.TOKEN, if (s == EOF) Tint.NONE else if (s.first().isUpperCase()) Tint.ORANGE else Tint.TEAL) }
            return Shot(listOf(rotulo("rp", "pilha", 180f, 374f)) + fita + pilha)
        }

    override fun aperta(tecla: String): Boolean {
        val a = passos.getOrNull(k)?.action
        val certo = (a is LLAction.Expand && tecla == "Trocar pela regra") || (a is LLAction.Match && tecla == "Casar")
        aviso = if (certo) null else if (a is LLAction.Expand) "O topo é não terminal. Olhe a tabela." else "Topo e entrada são iguais. Case."
        if (certo) k++
        return certo
    }

    override fun reinicia() { k = 0; aviso = null }
}

/** Corta a linha na mão: esticar o pedaço ou fechar o token. O motor diz onde o token termina. */
class ToyCorte(private val linha: String, override val meta: String) : Toy {
    private val tokens = MiniLexer.tokenize(linha)
    private var t = 0
    private var fim = 0
    override val teclas = listOf("Mais uma letra", "Cortar aqui")
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = t >= tokens.size

    init { reinicia() }

    override val shot: Shot
        get() {
            val atual = tokens.getOrNull(t)
            val fita = fita(linha, 110f, minOf(24f, 340f / linha.length)).mapIndexed { i, b ->
                val dentro = atual != null && i >= atual.start && i < fim
                val feito = atual == null || i < atual.start
                b.copy(tint = if (dentro) Tint.YELLOW else if (feito) Tint.DARK else Tint.NONE, hot = dentro)
            }
            val feitos = tokens.take(t).mapIndexed { i, tk ->
                Box("tk$i", tk.lexeme, 70f + (i % 4) * 74f, 210f + (i / 4) * 44f, 68f, 36f, Look.TOKEN, Tint.TEAL, size = 13f)
            }
            return Shot(fita + feitos)
        }

    override fun aperta(tecla: String): Boolean {
        val atual = tokens.getOrNull(t) ?: return false
        if (tecla == "Mais uma letra") {
            if (fim >= atual.end) { aviso = "Passou do token. A próxima letra já é outra coisa."; return false }
            fim++
            aviso = null
            return true
        }
        if (fim < atual.end) { aviso = "Ainda cabe mais. Maior casamento: estique."; return false }
        t++
        val prox = tokens.getOrNull(t)
        fim = if (prox != null) prox.start + 1 else linha.length
        aviso = null
        return true
    }

    override fun reinicia() { t = 0; fim = (tokens.firstOrNull()?.start ?: 0) + 1; aviso = null }
}

/** Deriva à mão: a pessoa escolhe a regra e a forma sentencial cresce. */
class ToyDerivacao(private val g: Grammar, private val alvo: List<String>, override val meta: String) : Toy {
    private var forma = SententialForm(listOf(g.start))
    private val nt get() = forma.symbols.getOrNull(forma.leftmostNT(g))
    private val regras: List<Production> get() = nt?.let { g.productionsOf(it) }.orEmpty()
    override val teclas: List<String> get() = regras.map { it.toString() }
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = forma.symbols == alvo

    override val shot: Shot
        get() {
            val forma0 = forma.symbols.ifEmpty { listOf("ε") }
            val cada = minOf(54f, 340f / forma0.size)
            val x0 = 180f - (forma0.size - 1) * cada / 2
            val alvoBox = alvo.mapIndexed { i, s -> Box("a$i", s, 180f + (i - (alvo.size - 1) / 2f) * 44f, 60f, 40f, 34f, Look.CELL, Tint.DARK, size = 13f) }
            val pecas = forma0.mapIndexed { i, s ->
                Box("f$i-$s", s, x0 + i * cada, 220f, cada - 4f, 42f, Look.TOKEN, if (g.isNonTerminal(s)) Tint.ORANGE else Tint.TEAL, hot = i == forma.leftmostNT(g), size = minOf(16f, cada * 0.42f))
            }
            return Shot(listOf(rotulo("ra", "chegue nesta frase", 180f, 26f)) + alvoBox + pecas)
        }

    override fun aperta(tecla: String): Boolean {
        val p = regras.firstOrNull { it.toString() == tecla } ?: return false
        val nova = forma.apply(forma.leftmostNT(g), p)
        if (nova.symbols.count { !g.isNonTerminal(it) } > alvo.size) { aviso = "A frase passou do tamanho."; reinicia(); return false }
        forma = nova
        if (forma.leftmostNT(g) < 0 && !pronto) { aviso = "Virou outra frase. Comece de novo."; reinicia(); return false }
        aviso = null
        return true
    }

    override fun reinicia() { forma = SententialForm(listOf(g.start)) }
}

/** Tipos de baixo para cima: a pessoa diz o tipo de cada nó, e o verificador do motor confere. */
class ToyTipos(src: String, private val env: Map<String, Pair<com.compilandia.engine.Type, Double>>, override val meta: String) : Toy {
    private val raiz = com.compilandia.engine.ExprParser.parse(src)
    private val attrs = com.compilandia.engine.TypeChecker.evaluate(raiz, env)
    private val ordem = raiz.postorder()
    private var k = 0
    override val teclas = listOf("int", "float")
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = k >= ordem.size

    private fun no(e: com.compilandia.engine.Expr): com.compilandia.engine.ParseNode =
        com.compilandia.engine.ParseNode(rotuloDe(e), e.children.map { no(it) })

    private fun rotuloDe(e: com.compilandia.engine.Expr): String {
        val feito = ordem.indexOf(e) < k
        return if (feito) "${e.text}: ${attrs.getValue(e.id).type.label}" else e.text
    }

    override val shot: Shot
        get() {
            val (bs, ws) = arvore(no(raiz), 70f, 74f, 330f)
            val atual = ordem.getOrNull(k)
            val alvo = atual?.let { achaId(raiz, it, "t") }
            return Shot(bs.map { it.copy(w = maxOf(it.w, it.text.length * 8.5f + 14f), hot = it.id == alvo, tint = if (it.id == alvo) Tint.YELLOW else it.tint, size = 13f) }, ws)
        }

    private fun achaId(e: com.compilandia.engine.Expr, alvo: com.compilandia.engine.Expr, id: String): String? {
        if (e === alvo) return id
        e.children.forEachIndexed { i, c -> achaId(c, alvo, "$id$i")?.let { return it } }
        return null
    }

    override fun aperta(tecla: String): Boolean {
        val e = ordem.getOrNull(k) ?: return false
        val certo = attrs.getValue(e.id).type.label
        if (tecla != certo) {
            aviso = if (e.isLeaf) "Olhe a declaração de ${e.text}." else "Se um dos filhos é float, o resultado sobe como float."
            return false
        }
        aviso = null
        k++
        return true
    }

    override fun reinicia() { k = 0; aviso = null }
}

/** Monta o MIPS na ordem: carrega, calcula, guarda. As linhas saem do gerador do motor. */
class ToyMips(private val src: String, override val meta: String) : Toy {
    private val linhas = com.compilandia.engine.Mips.fromTac(com.compilandia.engine.TacGen.generate(com.compilandia.engine.ExprParser.parse(src)))
    private var k = 0
    override val teclas = linhas.map { it.substringBefore(' ') }.distinct()
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = k >= linhas.size

    override val shot: Shot
        get() = Shot(listOf(rotulo("exp", src, 180f, 40f, Tint.YELLOW, 20f)) + codigo("m", linhas.take(k), 80f) + if (k < linhas.size) listOf(alvo("g", 180f, 90f + k * 25f, 300f, 22f, "próxima instrução")) else emptyList())

    override fun aperta(tecla: String): Boolean {
        val certo = linhas.getOrNull(k)?.substringBefore(' ') ?: return false
        if (tecla != certo) {
            aviso = when (certo) { "lw" -> "Antes de calcular, o valor precisa vir da memória."; "sw" -> "A conta terminou. Guarde o resultado."; else -> "Os dois valores já estão nos registradores." }
            return false
        }
        aviso = null
        k++
        return true
    }

    override fun reinicia() { k = 0; aviso = null }
}

/** Pilota o autômato pela tabela: a linha do estado acende, e a letra escolhe a célula. */
class ToyTabelaViva(private val d: DFA, private val tamanho: Int, override val meta: String) : Toy {
    private var lidas = ""
    override val teclas: List<String> = d.alphabet.map { it.toString() }
    override var aviso: String? = null
        private set
    private val estado get() = d.run(lidas).last()
    override val pronto: Boolean get() = lidas.length == tamanho && estado in d.accepting

    override val shot: Shot
        get() {
            val ultima = lidas.lastOrNull()?.let { d.alphabet.indexOf(it) }
            val anterior = if (lidas.isEmpty()) -1 else d.run(lidas.dropLast(1)).last()
            val celula = if (ultima != null && anterior >= 0) celulaAfd(anterior, ultima) else null
            val linhaAtual = "dl${estado + 1}c0"
            val tab = tabelaDoAfd(d, 70f).map {
                when (it.id) {
                    celula -> it.copy(tint = Tint.YELLOW, hot = true)
                    linhaAtual -> it.copy(tint = Tint.TEAL, hot = true)
                    else -> it
                }
            }
            val fita = List(tamanho) { i -> Box("c$i", lidas.getOrNull(i)?.toString() ?: "", 180f + (i - (tamanho - 1) / 2f) * 46f, 330f, 42f, 46f, Look.CELL, if (i < lidas.length) Tint.YELLOW else Tint.NONE, size = 20f) }
            return Shot(tab + fita)
        }

    override fun aperta(tecla: String): Boolean {
        if (lidas.length >= tamanho) { reinicia(); aviso = "A fita acabou. Recomece."; return false }
        lidas += tecla.first()
        aviso = if (pronto) "Chegou." else if (lidas.length == tamanho) "Terminou numa linha sem asterisco." else null
        return !(lidas.length == tamanho && !pronto)
    }

    override fun reinicia() { lidas = ""; aviso = null }
}

/** Monta o fecho de um estado LR(0): cada item certo entra na caixa; o motor diz quais são. */
class ToyClosure(g: Grammar, override val meta: String) : Toy {
    private val aug = g.augmented()
    private val kernel = setOf(com.compilandia.engine.LRItem(0, 0))
    private val fecho = com.compilandia.engine.LRAutomaton.closure0(aug, kernel).toList()
    private val falsos = aug.productions.drop(1).filter { p -> p.rhs.isNotEmpty() }.map { com.compilandia.engine.LRItem(it.id, 1) }.take(2)
    private val opcoes = (fecho.drop(1) + falsos).map { it.render(aug) }.sortedBy { it.length }
    private val dentro = mutableListOf(fecho.first())
    override val teclas: List<String> get() = (opcoes - dentro.map { it.render(aug) }.toSet()).ifEmpty { listOf("Pronto") }
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = dentro.size == fecho.size

    override val shot: Shot
        get() {
            val linhas = dentro.map { it.render(aug) }
            return Shot(listOf(rotulo("rg", "fecho do I0", 180f, 60f, Tint.YELLOW, 20f), registro("I0", "I0", linhas, 180f, 100f + alturaRegistro(linhas.size) / 2, 230f, if (pronto) Tint.GREEN else Tint.NONE, 16f, lit = linhas.lastIndex)))
        }

    override fun aperta(tecla: String): Boolean {
        val item = fecho.firstOrNull { it.render(aug) == tecla }
        if (item == null || item in dentro) {
            aviso = "Só entra item com o ponto no começo, de uma regra que aparece logo depois de um ponto."
            return false
        }
        dentro += item
        aviso = null
        return true
    }

    override fun reinicia() { dentro.clear(); dentro += fecho.first(); aviso = null }
}

/** É o processador da descida recursiva em C: chamar a próxima função, casar o token ou voltar. */
class ToyChamadas(private val tokens: List<String>, override val meta: String) : Toy {
    private val passos = rastroDescida(tokens)
    private var k = 0
    override val teclas = listOf("Chamar", "Casar", "Voltar")
    override var aviso: String? = null
        private set
    override val pronto: Boolean get() = k >= passos.size

    override val shot: Shot
        get() {
            val antes = passos.getOrNull(k - 1)
            val pilhaAgora = antes?.pilha ?: listOf("main")
            val lidos = antes?.lidos ?: 0
            val fita = fitaTokens(tokens, lidos, 40f)
            val pilha = pilha("f", pilhaAgora.map { "$it()" }, 110f, 360f, 150f, 36f, Tint.ORANGE).mapIndexed { i, b -> if (i == pilhaAgora.lastIndex) b.copy(tint = Tint.YELLOW, hot = true) else b }
            return Shot(fita + pilha + rotulo("rp", "pilha de chamadas", 110f, 392f) + codigo("g", listOf("expr:  termo ('+' termo)*", "termo: fator ('*' fator)*", "fator: NUM"), 120f, x = 196f, passo = 30f, size = 10f))
        }

    override fun aperta(tecla: String): Boolean {
        val p = passos.getOrNull(k) ?: return false
        val certo = when (p.acao) { "chamar" -> "Chamar"; "casar" -> "Casar"; else -> "Voltar" }
        if (tecla != certo) {
            aviso = when (p.acao) {
                "chamar" -> "A regra do topo ainda precisa de outra função."
                "casar" -> "O token da fita é o que essa função espera. Case."
                else -> "Essa função já fez tudo. Volte."
            }
            return false
        }
        aviso = null
        k++
        return true
    }

    override fun reinicia() { k = 0; aviso = null }
}
