package com.compilandia.engine

import com.compilandia.textos.tx

/**
 * J8: a reação no fim dos jogos de classificar. Depois que a pessoa classifica, o resultado roda:
 * o autômato filtra palavras novas, a gramática operada vai para o analisador, o programa passa pelas fases.
 * Quem roda é o motor. Cada passo vira um quadro do palco da reação.
 */
data class PassoReacao(
    /** O que o palco mostra, em fonte de código. */
    val linha: String,
    /** A legenda do passo. */
    val nota: String,
    /** Verde, vermelho ou neutro. */
    val ok: Boolean? = null,
)

object Reacoes {
    // ------------------------------------------------------------ Porteiro Regex

    private fun caminho(dfa: DFA, w: String): String {
        val estados = dfa.run(w)
        return (if (w.isEmpty()) "ε" else w) + ":  " + estados.joinToString(" → ") { if (it < 0) "✗" else "q$it" }
    }

    /** Um quadro do autômato lendo [w]. [palpite] é o que a pessoa disse (null quando a palavra é nova). */
    fun porteiroPasso(dfa: DFA, w: String, palpite: Boolean?): PassoReacao {
        val estados = dfa.run(w)
        val entra = estados.last() in dfa.accepting
        val p = if (w.isEmpty()) "ε" else w
        val nota = when {
            estados.last() < 0 -> tx("reacao_porteiro_sem_trilho", p)
            entra -> tx("reacao_porteiro_entra", p, "q${estados.last()}")
            else -> tx("reacao_porteiro_barra", p, "q${estados.last()}")
        }
        val antes = when (palpite) {
            null, entra -> ""
            true -> tx("reacao_porteiro_voce_entra", p) + " "
            false -> tx("reacao_porteiro_voce_barra", p) + " "
        }
        val fim = if (entra) tx("reacao_porteiro_dentro") else tx("reacao_porteiro_fora")
        return PassoReacao(caminho(dfa, w) + "   " + fim, antes + nota, if (palpite == null) null else palpite == entra)
    }

    /**
     * O porteiro vira autômato: primeiro as palavras em que a pessoa errou, depois palavras novas.
     * [respostas] é palavra → o que a pessoa disse (entra?).
     */
    fun porteiro(regex: String, respostas: List<Pair<String, Boolean?>>, novas: List<String>): List<PassoReacao> {
        val dfa = DFA.fromRegex(regex)
        val erradas = respostas.filter { (w, p) -> p != dfa.accepts(w) }
        return buildList {
            add(PassoReacao(regex, tx("reacao_porteiro_abre", regex)))
            for ((w, p) in erradas.take(3)) add(porteiroPasso(dfa, w, p))
            for (w in novas.filter { n -> respostas.none { it.first == n } }.take(4)) add(porteiroPasso(dfa, w, null))
        }
    }

    // ------------------------------------------------------------ Esteira das Fases

    /** A mesma linha passa pelas seis fases. Cada saída vem do motor. */
    fun esteira(nomes: List<String>, src: String = "a + 2 * 3", destino: String = "x", a: Long = 4): List<PassoReacao> {
        val e = ExprParser.parse(src)
        val tokens = MiniLexer.tokenize("$destino = $src;")
        val tipo = TypeChecker.evaluate(e, mapOf("a" to (Type.INT to a.toDouble())))[e.id]?.type ?: Type.ERROR
        val tac = TacGen.generate(e, destino)
        val dobrado = TacGen.generate(TacGen.constantFold(e), destino)
        val mips = Mips.fromTac(dobrado)
        val fim = Mips.run(mips, mapOf("a" to a)).lastOrNull()?.mem?.get(destino)
        return listOf(
            PassoReacao("$destino = $src;", tx("reacao_esteira_entra")),
            PassoReacao(tokens.joinToString(" ") { if (it.cls == TokenClass.ID || it.cls == TokenClass.NUM) "<${it.cls.name.lowercase()},${it.lexeme}>" else "<${it.lexeme}>" }, tx("reacao_esteira_lexica", nomes[0], tokens.size)),
            PassoReacao("$destino = " + e.render(), tx("reacao_esteira_sintatica", nomes[1])),
            PassoReacao("$destino = " + e.render() + "  : " + tipo.label, tx("reacao_esteira_semantica", nomes[2], tipo.label)),
            PassoReacao(tac.joinToString("\n"), tx("reacao_esteira_intermediario", nomes[3], tac.size)),
            PassoReacao(dobrado.joinToString("\n"), tx("reacao_esteira_otimizacao", nomes[4], tac.size, dobrado.size)),
            PassoReacao(mips.joinToString("\n"), tx("reacao_esteira_geracao", nomes[5], mips.size)),
            PassoReacao("a = $a  →  $destino = ${fim ?: "?"}", tx("reacao_esteira_roda", a, destino, fim ?: "?"), ok = true),
        )
    }

    // ------------------------------------------------------------ Detetive de Erros

    /** O compilador roda o caso fase por fase e para na que pega o erro. [fases] são os nomes dos quatro setores. */
    fun detetive(codigo: String, certa: Int, palpite: Int, fases: List<String>): List<PassoReacao> = buildList {
        add(PassoReacao(codigo, tx("reacao_detetive_compila")))
        for (k in 0..certa) {
            if (k < certa) add(PassoReacao(fases[k], tx("reacao_detetive_passou", fases[k]), true))
            else add(PassoReacao(fases[k], if (k == 3) tx("reacao_detetive_execucao", fases[k]) else tx("reacao_detetive_pegou", fases[k]), false))
        }
        if (palpite in 0..3 && palpite != certa) add(PassoReacao(codigo, tx("reacao_detetive_palpite", fases[palpite], fases[certa])))
    }

    // ------------------------------------------------------------ Fatiador de Tokens

    /**
     * Os tokens seguem para a próxima fase com a classe que a pessoa deu.
     * Identificador entra na tabela de símbolos; palavra-chave avisa o parser; número vira constante.
     */
    fun fatiador(tokens: List<Token>, classes: Map<Int, TokenClass>): List<PassoReacao> = buildList {
        val nomes = mutableListOf<String>()
        for ((i, t) in tokens.withIndex()) {
            val c = classes[i] ?: continue
            val certo = c == t.cls
            val destino = when (c) {
                TokenClass.ID -> tx("reacao_fatiador_id", t.lexeme)
                TokenClass.KEYWORD -> tx("reacao_fatiador_chave", t.lexeme)
                TokenClass.NUM -> tx("reacao_fatiador_num", t.lexeme)
                else -> tx("reacao_fatiador_simbolo", t.lexeme)
            }
            if (c == TokenClass.ID) nomes += t.lexeme
            val nota = if (certo) destino else destino + " " + tx("reacao_fatiador_errado", t.cls.label)
            add(PassoReacao(t.lexeme + "  →  " + c.label, nota, certo))
        }
        val fluxo = tokens.indices.mapNotNull { i ->
            val c = classes[i] ?: return@mapNotNull null
            when (c) { TokenClass.ID -> "id"; TokenClass.NUM -> "num"; else -> tokens[i].lexeme }
        }
        add(PassoReacao(fluxo.joinToString(" "), tx("reacao_fatiador_parser")))
        val n = nomes.distinct().size
        val nota = when (n) { 0 -> tx("reacao_fatiador_tabela_vazia"); 1 -> tx("reacao_fatiador_tabela_um"); else -> tx("reacao_fatiador_tabela", n) }
        add(PassoReacao(nomes.distinct().joinToString(", ").ifEmpty { "∅" }, nota))
    }

    // ------------------------------------------------------------ Duas Árvores

    /** O valor de uma árvore de expressão com os ids trocados por [valores], da esquerda para a direita. */
    fun avalia(t: ParseNode, valores: List<Long>): Pair<Long, String>? {
        var k = 0
        fun go(n: ParseNode): Pair<Long, String>? {
            if (n.isLeaf) {
                if (n.symbol != "id") return null
                val v = valores.getOrNull(k++) ?: return null
                return v to "$v"
            }
            val f = n.children
            return when {
                f.size == 1 -> go(f[0])
                f.size == 3 && f[0].symbol == "(" -> go(f[1])
                f.size == 3 && (f[1].symbol == "+" || f[1].symbol == "*") -> {
                    val a = go(f[0]) ?: return null
                    val b = go(f[2]) ?: return null
                    val v = if (f[1].symbol == "+") a.first + b.first else a.first * b.first
                    v to "(${a.second} ${f[1].symbol} ${b.second})"
                }
                else -> null
            }
        }
        return go(t)
    }

    /** As duas árvores da mesma frase calculam: com valores nos ids, cada uma dá um resultado. Vazio quando a frase não é conta. */
    fun duasArvores(t1: ParseNode, t2: ParseNode): List<PassoReacao> {
        val n = t1.leaves().count { it == "id" }
        val valores = listOf(2L, 3L, 4L, 5L, 6L).take(n)
        val a = avalia(t1, valores) ?: return emptyList()
        val b = avalia(t2, valores) ?: return emptyList()
        val frase = t1.leaves().joinToString(" ")
        val trocada = t1.leaves().let { fs -> var k = 0; fs.joinToString(" ") { if (it == "id") "${valores[k++]}" else it } }
        return listOf(
            PassoReacao("$frase\n$trocada", tx("reacao_arvores_valores", valores.joinToString(", "))),
            PassoReacao(a.second.removeSurrounding("(", ")") + " = ${a.first}", tx("reacao_arvores_primeira", a.first)),
            PassoReacao(b.second.removeSurrounding("(", ")") + " = ${b.first}", tx("reacao_arvores_segunda", b.first)),
            if (a.first != b.first) PassoReacao("${a.first} ≠ ${b.first}", tx("reacao_arvores_diferentes", trocada), false)
            else PassoReacao("${a.first} = ${b.first}", tx("reacao_arvores_iguais", trocada)),
        )
    }

    // ------------------------------------------------------------ Cirurgia de Gramática

    /**
     * A gramática operada vai para o Byte com a pilha. Ele analisa frases da gramática original.
     * Se a tabela tiver célula com duas regras, ele para antes de começar.
     */
    fun cirurgia(original: Grammar, operada: Grammar, frases: List<List<String>>): List<PassoReacao> = buildList {
        val t = LL1Table(operada)
        add(PassoReacao(operada.productions.joinToString("\n") { it.toString() }, tx("reacao_cirurgia_abre")))
        val conflito = t.filledCells().entries.firstOrNull { it.value.size > 1 }
        if (conflito != null) {
            val (nt, a) = conflito.key
            add(PassoReacao("M[$nt, $a] = " + conflito.value.joinToString("  |  ") { it.toString() }, tx("reacao_cirurgia_conflito", nt, a), false))
            return@buildList
        }
        val earley = Earley(operada)
        for (w in frases.take(3)) {
            val passos = LL1Parser.trace(t, w)
            val fim = passos.lastOrNull()?.action
            val frase = w.joinToString(" ").ifEmpty { "ε" }
            when {
                fim == LLAction.Accept -> add(PassoReacao(frase, tx("reacao_cirurgia_aceitou", frase, passos.size), true))
                fim is LLAction.Error && !earley.accepts(w) -> add(PassoReacao(frase, tx("reacao_cirurgia_perdeu", frase), false))
                fim is LLAction.Error -> add(PassoReacao(frase, tx("reacao_cirurgia_parou", frase, fim.message), false))
                else -> add(PassoReacao(frase, tx("reacao_cirurgia_longa", frase), false))
            }
        }
    }

    /** A gramática que a pessoa montou: as regras que não mudaram mais as que ela preencheu. */
    fun montaGramatica(inicial: String, fixas: List<Production>, montadas: List<Pair<String, List<String>>>): Grammar? = try {
        val linhas = (fixas.map { it.lhs to it.rhs } + montadas).groupBy({ it.first }, { it.second })
        val ordem = listOf(inicial) + linhas.keys.filter { it != inicial }
        Grammar.parse(ordem.filter { it in linhas }.joinToString("\n") { nt -> "$nt -> " + linhas.getValue(nt).joinToString(" | ") { r -> if (r.isEmpty() || r == listOf(EPS)) EPS else r.joinToString(" ") } })
    } catch (e: Exception) {
        null
    }

    // ------------------------------------------------------------ Caça-Conflitos

    /**
     * O analisador SLR roda uma frase curta até a primeira célula com duas ações.
     * Mostra a pilha nos últimos passos e as duas ações da célula.
     */
    fun conflito(t: LRTable, g: Grammar, maxLen: Int = 5): List<PassoReacao> {
        val conflitos = t.rawConflictCells.toSet()
        if (conflitos.isEmpty()) {
            val w = Frases.geradas(g.productions, g.start, g.nonTerminals.toSet(), maxLen).keys.sortedWith(Frases.ORDEM).lastOrNull() ?: return emptyList()
            val r = LRParser.parse(t, w)
            return listOf(PassoReacao(w.joinToString(" "), tx("reacao_conflito_nenhum", w.joinToString(" "), r.steps.size), r.accepted))
        }
        val frases = Frases.geradas(g.productions, g.start, g.nonTerminals.toSet(), maxLen).keys.sortedWith(Frases.ORDEM)
        for (w in frases) {
            val r = LRParser.parse(t, w)
            val k = r.steps.indexOfFirst { (it.states.last() to it.input.first()) in conflitos }
            if (k < 0) continue
            val passo = r.steps[k]
            val cel = passo.states.last() to passo.input.first()
            val acoes = t.rawActions[cel].orEmpty()
            return buildList {
                add(PassoReacao(w.joinToString(" "), tx("reacao_conflito_frase", w.joinToString(" "))))
                for (s in r.steps.subList((k - 3).coerceAtLeast(0), k)) add(PassoReacao(pilha(s), s.note))
                add(PassoReacao(pilha(passo), tx("reacao_conflito_celula", cel.first, cel.second, acoes.joinToString(tx("reacao_conflito_ou")) { it.short() }), false))
            }
        }
        return emptyList()
    }

    private fun pilha(s: LRStep): String {
        val p = buildString {
            append(s.states.first())
            for (i in s.symbols.indices) append(" ${s.symbols[i]} ${s.states[i + 1]}")
        }
        return "$p   |   " + s.input.joinToString(" ")
    }

    // ------------------------------------------------------------ Esconde-Esconde do Escopo

    /** As linhas que abrem os blocos em volta da linha [i], do mais de dentro para o de fora. */
    fun blocos(p: ScopeGen.Program, i: Int): List<Int> {
        val out = mutableListOf<Int>()
        var nivel = p.lines[i].indent - 1
        var k = i - 1
        while (k >= 0 && nivel >= 0) {
            val l = p.lines[k]
            if (l.text == "{" && l.indent == nivel) { out += k; nivel-- }
            k--
        }
        return out
    }

    /** Onde a declaração visível de [nome] na linha [i] está, procurando bloco por bloco. */
    fun resolve(p: ScopeGen.Program, i: Int, nome: String): Pair<List<Int>, Int?> {
        val visitados = mutableListOf<Int>()
        for (b in blocos(p, i)) {
            visitados += b
            val dentro = p.lines[b].indent + 1
            var k = b + 1
            while (k < i) {
                val l = p.lines[k]
                if (l.indent == dentro && l.declId != null && l.text.removeSuffix(";").substringAfter(" ") == nome) return visitados to k
                // pula os blocos filhos que já fecharam antes do uso
                if (l.text == "{" && l.indent == dentro) {
                    var m = k + 1
                    while (m < p.lines.size && !(p.lines[m].text == "}" && p.lines[m].indent == dentro)) m++
                    k = m
                }
                k++
            }
        }
        return visitados to null
    }

    /** O compilador procura cada uso bloco por bloco. [respostas] é linha do uso → linha que a pessoa ligou (null = "não declarada"). */
    fun escopo(p: ScopeGen.Program, respostas: Map<Int, Int?>): List<PassoReacao> = buildList {
        val usos = p.uses.map { it.index }
        val erradas = usos.filter { respostas.containsKey(it) && respostas[it] != p.lines[it].resolvesTo }
        val mostrar = (erradas + usos.filter { it !in erradas }).take(3)
        for (u in mostrar) {
            val nome = p.lines[u].use ?: continue
            val (visitados, achou) = resolve(p, u, nome)
            for ((n, b) in visitados.withIndex()) {
                val ultimo = n == visitados.lastIndex
                val nota = when {
                    ultimo && achou != null -> tx("reacao_escopo_achou", nome, u + 1, achou + 1, p.lines[achou].text)
                    ultimo -> tx("reacao_escopo_nada", nome, u + 1)
                    else -> tx("reacao_escopo_sobe", nome, b + 1)
                }
                val certo = if (!ultimo) null else respostas[u]?.let { it == achou } ?: (achou == null && respostas.containsKey(u))
                add(PassoReacao(tx("reacao_escopo_linha", u + 1, p.lines[u].text.trim(), b + 1), nota, certo))
            }
        }
    }

    // ------------------------------------------------------------ Sala do Pânico

    /** O modo pânico roda até o fim da entrada: cada erro, o que foi descartado e onde ele volta. */
    fun panico(input: List<String>, trace: RecoveryTrace): List<PassoReacao> = buildList {
        add(PassoReacao((input + EOF).joinToString(" "), tx("reacao_panico_abre")))
        for (e in trace.events) {
            val marcada = (input + EOF).mapIndexed { k, s -> if (k == e.position) "[$s]" else s }.joinToString(" ")
            add(PassoReacao(marcada, e.message, false))
        }
        val fim = when {
            !trace.reachedEnd -> tx("reacao_panico_parou")
            trace.events.size == 1 -> tx("reacao_panico_fim_um")
            else -> tx("reacao_panico_fim", trace.events.size)
        }
        add(PassoReacao((input + EOF).joinToString(" "), fim, trace.reachedEnd))
    }
}
