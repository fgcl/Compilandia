package com.compilandia.engine

import com.compilandia.textos.tx

/*
 * J4, J5 e J6: os jogos de montar da análise sintática. Seguem o formato de [Veredito] do J1:
 * um resultado por teste e, no primeiro que falha, a frase exata e o que deu errado.
 * Quem diz se a frase é da linguagem é o motor (enumeração de derivações), não o jogo.
 */

/** As frases de uma gramática até um tamanho, cada uma com o número de derivações mais à esquerda (até 2). */
object Frases {
    /** Limite de formas visitadas: gramática estranha da pessoa não trava o celular. */
    const val FORMAS_MAX = 60_000

    /**
     * Todas as frases com até [maxLen] terminais que [prods] gera a partir de [inicial].
     * O valor é 1 ou 2: duas derivações mais à esquerda diferentes são duas árvores.
     * [nts] diz quem é não-terminal, mesmo sem regra ligada (aí ele não vira frase nenhuma).
     * [folga] deixa a forma passar do tamanho, por causa das regras com ε: ( ( ( S ) S ) S ) S tem 10 símbolos e vira 6.
     */
    fun geradas(prods: List<Production>, inicial: String, nts: Set<String>, maxLen: Int, folga: Int = maxLen + 2): Map<List<String>, Int> {
        val porNt = prods.groupBy { it.lhs }
        val conta = HashMap<List<String>, Int>()
        val feito = HashMap<List<String>, Int>()
        val fila = ArrayDeque<List<String>>()
        conta[listOf(inicial)] = 1
        fila += listOf(inicial)
        while (fila.isNotEmpty() && conta.size < FORMAS_MAX) {
            val f = fila.removeFirst()
            val c = conta.getValue(f)
            val antes = feito[f] ?: 0
            if (c == antes) continue
            feito[f] = c
            val i = f.indexOfFirst { it in nts }
            if (i < 0) continue
            for (p in porNt[f[i]].orEmpty()) {
                val nf = f.subList(0, i) + p.rhs + f.subList(i + 1, f.size)
                if (nf.count { it !in nts } > maxLen || nf.size > maxLen + folga) continue
                val velho = conta[nf] ?: 0
                val novo = minOf(2, velho + c - antes)
                if (novo != velho) {
                    conta[nf] = novo
                    fila += nf
                }
            }
        }
        return conta.filterKeys { w -> w.none { it in nts } }
    }

    /** Todas as palavras sobre [alfabeto] com até [maxLen] símbolos, das curtas para as longas. */
    fun todas(alfabeto: List<String>, maxLen: Int): List<List<String>> {
        val out = mutableListOf<List<String>>(emptyList())
        var nivel = listOf<List<String>>(emptyList())
        repeat(maxLen) {
            nivel = nivel.flatMap { w -> alfabeto.map { w + it } }
            out += nivel
        }
        return out
    }

    /** A ordem de "menor contraexemplo": tamanho, depois o texto. */
    val ORDEM: Comparator<List<String>> = compareBy<List<String>> { it.size }.thenBy { it.joinToString(" ") }

    fun texto(w: List<String>) = if (w.isEmpty()) EPS else w.joinToString(" ")
}

// ============================================================ J4 Byte com a pilha

/**
 * J4: a pessoa preenche a tabela LL(1). O Byte, com a pilha nas costas, analisa as frases
 * seguindo a tabela dela. A tabela certa vem do [LL1Table]; quem diz se a frase é da linguagem
 * é a enumeração de [Frases].
 */
object RoboPilha {
    const val PASSOS_MAX = 60
    /** As frases certas até esse tamanho entram na busca do contraexemplo. */
    const val BUSCA = 7

    class Fase(val id: String, val nome: String, val pedido: String, gramatica: String, val testes: List<String>, val dicas: List<String>) {
        val g: Grammar = Grammar.parse(gramatica)
        val certa = LL1Table(g)
        val colunas: List<String> = g.terminals + EOF
        /** A tabela da prova: célula → id da produção. */
        val solucao: Map<Pair<String, String>, Int> = certa.filledCells().mapValues { it.value.first().id }
        val validas: Set<List<String>> by lazy { Frases.geradas(g.productions, g.start, g.nonTerminals.toSet(), BUSCA).keys }
        fun deveAceitar(w: List<String>) = if (w.size <= BUSCA) w in validas else Earley(g).accepts(w)
    }

    val fases: List<Fase> by lazy {
        listOf(
            Fase(
                "pilha_parenteses", tx("robopilha_fase_parenteses"), tx("robopilha_pedido_parenteses"), "A -> ( A ) | x",
                listOf("x", "( x )", "( ( x ) )", "( x", "x )"),
                listOf(tx("robopilha_dica_parenteses_1"), tx("robopilha_dica_parenteses_2")),
            ),
            Fase(
                "pilha_lista", tx("robopilha_fase_lista"), tx("robopilha_pedido_lista"), "L -> id R\nR -> , id R | ε",
                listOf("id", "id , id", "id , id , id", "id ,", ", id"),
                listOf(tx("robopilha_dica_lista_1"), tx("robopilha_dica_lista_2")),
            ),
            Fase(
                "pilha_vazio", tx("robopilha_fase_vazio"), tx("robopilha_pedido_vazio"), "S -> A b\nA -> a A | ε",
                listOf("b", "a b", "a a b", "a", "b b"),
                listOf(tx("robopilha_dica_vazio_1"), tx("robopilha_dica_vazio_2")),
            ),
            Fase(
                "pilha_expressao", tx("robopilha_fase_expressao"), tx("robopilha_pedido_expressao"), "E -> T E'\nE' -> + T E' | ε\nT -> id | ( E )",
                listOf("id", "id + id", "( id )", "( id + id ) + id", "id +", "+ id", "( id"),
                listOf(tx("robopilha_dica_expressao_1"), tx("robopilha_dica_expressao_2")),
            ),
        )
    }

    enum class Acao { INICIO, EXPANDE, CASA, ACEITA, VAZIA, NAO_CASA, SOBROU, LONGA }

    /** Um quadro do palco. [pilha] vai do fundo ($) ao topo. [celula] é a da tabela usada ou que faltou. */
    data class Quadro(val pilha: List<String>, val lidos: Int, val celula: Pair<String, String>?, val prod: Production?, val acao: Acao, val nota: String)

    data class Corrida(val frase: List<String>, val quadros: List<Quadro>, val aceitou: Boolean) {
        val fim: Quadro get() = quadros.last()
    }

    /** O Byte analisa [frase] seguindo a tabela [t] (célula → id da produção). */
    fun corre(f: Fase, t: Map<Pair<String, String>, Int>, frase: List<String>): Corrida {
        val g = f.g
        val pilha = mutableListOf(EOF, g.start)
        val inp = frase + EOF
        var pos = 0
        val q = mutableListOf(Quadro(pilha.toList(), 0, null, null, Acao.INICIO, tx("robopilha_nota_inicio", g.start)))
        while (q.size < PASSOS_MAX) {
            val topo = pilha.last()
            val a = inp[pos]
            when {
                topo == EOF && a == EOF -> {
                    q += Quadro(pilha.toList(), pos, null, null, Acao.ACEITA, tx("robopilha_nota_aceita"))
                    return Corrida(frase, q, true)
                }
                topo == EOF -> {
                    q += Quadro(pilha.toList(), pos, null, null, Acao.SOBROU, tx("robopilha_nota_sobrou", a))
                    return Corrida(frase, q, false)
                }
                !g.isNonTerminal(topo) -> {
                    if (topo != a) {
                        q += Quadro(pilha.toList(), pos, null, null, Acao.NAO_CASA, tx("robopilha_nota_nao_casa", topo, a))
                        return Corrida(frase, q, false)
                    }
                    pilha.removeAt(pilha.lastIndex)
                    pos++
                    q += Quadro(pilha.toList(), pos, null, null, Acao.CASA, tx("robopilha_nota_casa", a))
                }
                else -> {
                    val pid = t[topo to a]
                    if (pid == null) {
                        q += Quadro(pilha.toList(), pos, topo to a, null, Acao.VAZIA, tx("robopilha_nota_vazia", topo, a))
                        return Corrida(frase, q, false)
                    }
                    val p = g.productions[pid]
                    pilha.removeAt(pilha.lastIndex)
                    for (s in p.rhs.asReversed()) pilha += s
                    q += Quadro(pilha.toList(), pos, topo to a, p, Acao.EXPANDE, tx("robopilha_nota_expande", topo, a, p.rhsText()))
                }
            }
        }
        q += Quadro(pilha.toList(), pos, null, null, Acao.LONGA, tx("robopilha_nota_longa"))
        return Corrida(frase, q, false)
    }

    private fun obtido(c: Corrida): String = when (c.fim.acao) {
        Acao.ACEITA -> tx("robopilha_aceita")
        Acao.VAZIA -> c.fim.celula!!.let { tx("robopilha_resultado_vazia", it.first, it.second) }
        Acao.NAO_CASA -> tx("robopilha_resultado_nao_casa", c.fim.pilha.last())
        Acao.SOBROU -> tx("robopilha_resultado_sobrou")
        else -> tx("robopilha_resultado_longa")
    }

    /** As frases do teste e, se escapou alguma frase certa, a menor delas no fim da lista. */
    fun frases(f: Fase, t: Map<Pair<String, String>, Int>): List<Pair<List<String>, Boolean>> {
        val lista = f.testes.map { tokensOf(it) }
        val achado = f.validas.sortedWith(Frases.ORDEM).firstOrNull { it !in lista && !corre(f, t, it).aceitou }
        return lista.map { it to false } + listOfNotNull(achado?.let { it to true })
    }

    /** A primeira célula da pessoa que não bate com a tabela da prova, na ordem da tabela. */
    fun celulaDiferente(f: Fase, t: Map<Pair<String, String>, Int>): Pair<String, String>? {
        for (nt in f.g.nonTerminals) for (c in f.colunas) if (t[nt to c] != f.solucao[nt to c]) return nt to c
        return null
    }

    fun confere(f: Fase, t: Map<Pair<String, String>, Int>): Veredito {
        if (t.isEmpty()) return Veredito(emptyList(), -1, tx("robopilha_tabela_vazia"))
        val lista = frases(f, t)
        val corridas = lista.map { corre(f, t, it.first) }
        val res = lista.zip(corridas).map { (w, c) ->
            val quer = f.deveAceitar(w.first)
            ResultadoTeste(Frases.texto(w.first), c.aceitou == quer, obtido(c), if (quer) tx("robopilha_aceita") else tx("robopilha_recusa"), w.second)
        }
        val i = res.indexOfFirst { !it.passou }
        if (i >= 0) {
            val c = corridas[i]
            val w = res[i].entrada
            val frase = when {
                c.aceitou -> tx("robopilha_falha_aceitou", w)
                c.fim.acao == Acao.VAZIA -> c.fim.celula!!.let { tx("robopilha_falha_vazia", w, it.first, it.second) }
                c.fim.acao == Acao.NAO_CASA -> tx("robopilha_falha_nao_casa", w, c.fim.pilha.last(), (lista[i].first + EOF)[c.fim.lidos])
                c.fim.acao == Acao.SOBROU -> tx("robopilha_falha_sobrou", w, (lista[i].first + EOF)[c.fim.lidos])
                else -> tx("robopilha_falha_longa", w)
            }
            return Veredito(res, i, frase)
        }
        // Toda frase passou. A tabela da prova ainda pode ter célula diferente: uma regra sobrando.
        val d = celulaDiferente(f, t) ?: return Veredito(res, null, tx("robopilha_passou", res.size))
        val tem = t[d]?.let { f.g.productions[it].toString() }
        val certa = f.solucao[d]?.let { f.g.productions[it].toString() }
        val linha = ResultadoTeste("M[${d.first}, ${d.second}]", false, tem ?: tx("robopilha_celula_vazia"), certa ?: tx("robopilha_celula_vazia"), achado = true)
        val frase = if (certa == null) tx("robopilha_falha_sobra", d.first, d.second, tem!!) else tx("robopilha_falha_celula", d.first, d.second)
        return Veredito(res + linha, res.size, frase)
    }

    /**
     * A terceira dica: aponta a célula por onde a primeira frase que falha passa errado.
     * Não diz a regra; diz onde olhar.
     */
    fun ondeOlhar(f: Fase, t: Map<Pair<String, String>, Int>): String? {
        val ruim = frases(f, t).map { corre(f, t, it.first) }.firstOrNull { it.aceitou != f.deveAceitar(it.frase) }
        if (ruim != null) {
            val errada = ruim.quadros.firstOrNull { it.acao == Acao.EXPANDE && t[it.celula!!] != f.solucao[it.celula] }?.celula
            val cel = errada ?: ruim.fim.celula
            if (cel != null) return tx("robopilha_olhe_celula", cel.first, cel.second, Frases.texto(ruim.frase))
        }
        val d = celulaDiferente(f, t) ?: return null
        return tx("robopilha_olhe_sobra", d.first, d.second)
    }
}

// ============================================================ J5 Guindaste empilha-reduz

/**
 * J5: o guindaste pega os símbolos da esteira e empilha. A pessoa programa as regras de troca
 * e a ordem delas. A cada passo o guindaste tenta as regras de cima para baixo: a primeira que
 * casa com o topo da pilha troca o topo pelo lado esquerdo. Se nenhuma casa, ele empilha.
 * Na última fase, cada regra pode esperar um símbolo da esteira (o começo da ideia do SLR).
 */
object Guindaste {
    const val PASSOS_MAX = 80
    const val REGRAS_MAX = 6
    /** Palavras de qualquer tipo até esse tamanho entram na busca do contraexemplo. */
    const val BUSCA = 5

    /** Uma linha do programa: a carta e, se a fase deixa, os símbolos da esteira que liberam a troca (null = qualquer). */
    data class Regra(val carta: Int, val olha: Set<String>? = null)

    class Fase(
        val id: String,
        val nome: String,
        val pedido: String,
        gramatica: String,
        extras: List<String>,
        val testes: List<String>,
        val dicas: List<String>,
        val comOlhar: Boolean,
        /** Um programa certo. Os testes garantem que ele passa. */
        val solucao: List<Regra>,
    ) {
        val g: Grammar = Grammar.parse(gramatica)
        /** As cartas da mesa: as regras da gramática e as que não são dela. */
        val cartas: List<Production> = g.productions + extras.map { Grammar.parse(it).productions.first() }
        val colunas: List<String> = g.terminals + EOF
        val validas: Set<List<String>> by lazy { Frases.geradas(g.productions, g.start, g.nonTerminals.toSet(), 7).keys }
        fun deveAceitar(w: List<String>) = if (w.size <= 7) w in validas else Earley(g).accepts(w)
    }

    val fases: List<Fase> by lazy {
        val soma = setOf("+", EOF)
        listOf(
            Fase(
                "guindaste_parenteses", tx("guindaste_fase_parenteses"), tx("guindaste_pedido_parenteses"), "S -> ( S ) | x", listOf("S -> ( S"),
                listOf("x", "( x )", "( ( x ) )", "( x", "x )"),
                listOf(tx("guindaste_dica_parenteses_1"), tx("guindaste_dica_parenteses_2")), false,
                listOf(Regra(0), Regra(1)),
            ),
            Fase(
                "guindaste_lista", tx("guindaste_fase_lista"), tx("guindaste_pedido_lista"), "L -> L , id | id", listOf("L -> id ,"),
                listOf("id", "id , id", "id , id , id", "id ,", ", id"),
                listOf(tx("guindaste_dica_lista_1"), tx("guindaste_dica_lista_2")), false,
                listOf(Regra(0), Regra(1)),
            ),
            Fase(
                "guindaste_soma", tx("guindaste_fase_soma"), tx("guindaste_pedido_soma"), "E -> E + T | T\nT -> id", listOf("E -> id"),
                listOf("id", "id + id", "id + id + id", "+ id", "id +"),
                listOf(tx("guindaste_dica_soma_1"), tx("guindaste_dica_soma_2")), false,
                listOf(Regra(2), Regra(0), Regra(1)),
            ),
            Fase(
                "guindaste_vezes", tx("guindaste_fase_vezes"), tx("guindaste_pedido_vezes"), "E -> E + T | T\nT -> T * id | id", emptyList(),
                listOf("id", "id * id", "id + id", "id + id * id", "id * id + id", "id + * id", "* id"),
                listOf(tx("guindaste_dica_vezes_1"), tx("guindaste_dica_vezes_2")), true,
                listOf(Regra(2), Regra(3), Regra(0, soma), Regra(1, soma)),
            ),
        )
    }

    enum class Acao { INICIO, EMPILHA, TROCA, ACEITA, TRAVA, LONGA }

    /** Um quadro: a pilha (fundo primeiro), quantos símbolos já saíram da esteira e a regra usada. [mao] é quantos símbolos o guindaste ergueu. */
    data class Quadro(val pilha: List<String>, val lidos: Int, val acao: Acao, val regra: Int?, val mao: Int, val nota: String)

    data class Corrida(val frase: List<String>, val quadros: List<Quadro>, val aceitou: Boolean) {
        val fim: Quadro get() = quadros.last()
        /** A última troca antes de travar: quase sempre a que veio cedo demais. */
        val ultimaTroca: Quadro? get() = quadros.lastOrNull { it.acao == Acao.TROCA }
    }

    fun corre(f: Fase, programa: List<Regra>, frase: List<String>): Corrida {
        val pilha = mutableListOf<String>()
        val inp = frase + EOF
        var pos = 0
        val q = mutableListOf(Quadro(emptyList(), 0, Acao.INICIO, null, 0, tx("guindaste_nota_inicio")))
        while (q.size < PASSOS_MAX) {
            val prox = inp[pos]
            if (pilha == listOf(f.g.start) && prox == EOF) {
                q += Quadro(pilha.toList(), pos, Acao.ACEITA, null, 0, tx("guindaste_nota_aceita", f.g.start))
                return Corrida(frase, q, true)
            }
            val r = programa.indexOfFirst { rr ->
                val p = f.cartas[rr.carta]
                p.rhs.isNotEmpty() && pilha.size >= p.rhs.size && pilha.subList(pilha.size - p.rhs.size, pilha.size) == p.rhs && (rr.olha == null || prox in rr.olha)
            }
            if (r >= 0) {
                val p = f.cartas[programa[r].carta]
                repeat(p.rhs.size) { pilha.removeAt(pilha.lastIndex) }
                pilha += p.lhs
                q += Quadro(pilha.toList(), pos, Acao.TROCA, r, p.rhs.size, tx("guindaste_nota_troca", p.rhsText(), p.lhs))
                continue
            }
            if (prox == EOF) {
                q += Quadro(pilha.toList(), pos, Acao.TRAVA, null, 0, tx("guindaste_nota_trava", Frases.texto(pilha)))
                return Corrida(frase, q, false)
            }
            pilha += prox
            pos++
            q += Quadro(pilha.toList(), pos, Acao.EMPILHA, null, 0, tx("guindaste_nota_empilha", prox))
        }
        q += Quadro(pilha.toList(), pos, Acao.LONGA, null, 0, tx("guindaste_nota_longa"))
        return Corrida(frase, q, false)
    }

    private fun obtido(c: Corrida) = when (c.fim.acao) {
        Acao.ACEITA -> tx("robopilha_aceita")
        Acao.TRAVA -> tx("guindaste_resultado_trava", Frases.texto(c.fim.pilha))
        else -> tx("robopilha_resultado_longa")
    }

    /** O menor erro fora da lista: palavra curta de qualquer tipo, ou frase certa mais longa que o guindaste recusa. */
    private fun achado(f: Fase, programa: List<Regra>, lista: List<List<String>>): List<String>? {
        val curtas = Frases.todas(f.g.terminals, BUSCA).firstOrNull { it !in lista && corre(f, programa, it).aceitou != f.deveAceitar(it) }
        return curtas ?: f.validas.sortedWith(Frases.ORDEM).firstOrNull { it.size > BUSCA && it !in lista && !corre(f, programa, it).aceitou }
    }

    fun frases(f: Fase, programa: List<Regra>): List<Pair<List<String>, Boolean>> {
        val lista = f.testes.map { tokensOf(it) }
        return lista.map { it to false } + listOfNotNull(achado(f, programa, lista)?.let { it to true })
    }

    fun confere(f: Fase, programa: List<Regra>): Veredito {
        if (programa.isEmpty()) return Veredito(emptyList(), -1, tx("guindaste_programa_vazio"))
        val lista = frases(f, programa)
        val corridas = lista.map { corre(f, programa, it.first) }
        val res = lista.zip(corridas).map { (w, c) ->
            val quer = f.deveAceitar(w.first)
            ResultadoTeste(Frases.texto(w.first), c.aceitou == quer, obtido(c), if (quer) tx("robopilha_aceita") else tx("robopilha_recusa"), w.second)
        }
        val i = res.indexOfFirst { !it.passou }
        if (i < 0) return Veredito(res, null, tx("guindaste_passou", res.size))
        val c = corridas[i]
        val w = res[i].entrada
        val frase = when {
            c.aceitou -> tx("guindaste_falha_aceitou", w)
            c.fim.acao == Acao.LONGA -> tx("guindaste_falha_longa", w)
            else -> c.ultimaTroca?.let { u ->
                val p = f.cartas[programa[u.regra!!].carta]
                tx("guindaste_falha_trava", w, Frases.texto(c.fim.pilha), p.toString())
            } ?: tx("guindaste_falha_trava_sem_troca", w, Frases.texto(c.fim.pilha))
        }
        return Veredito(res, i, frase)
    }

    /** A terceira dica: a primeira linha do programa que difere de um programa certo. */
    fun ondeOlhar(f: Fase, programa: List<Regra>): String? {
        val s = f.solucao
        val i = s.indices.firstOrNull { it >= programa.size || programa[it] != s[it] }
        if (i == null) return if (programa.size > s.size) tx("guindaste_olhe_sobra", s.size) else null
        val certa = f.cartas[s[i].carta]
        if (i < programa.size && programa[i].carta == s[i].carta) {
            val olha = s[i].olha?.joinToString(" ") ?: tx("guindaste_olha_qualquer")
            return tx("guindaste_olhe_espera", certa.toString(), olha)
        }
        return tx("guindaste_olhe_linha", i + 1, certa.toString())
    }
}

// ============================================================ J6 Fábrica de frases

/**
 * J6: a pessoa liga as máquinas, que são as regras da gramática. A fábrica produz as frases.
 * O pedido diz quais precisam sair e quais não podem. Se uma frase sai de dois jeitos,
 * a gramática é ambígua, e a fábrica mostra as duas árvores.
 */
object Fabrica {
    class Fase(
        val id: String,
        val nome: String,
        val pedido: String,
        val pecas: List<String>,
        val testes: List<String>,
        /** A fábrica confere todas as frases até esse tamanho. */
        val maxLen: Int,
        val semAmbiguidade: Boolean,
        val dicas: List<String>,
        val solucao: Set<Int>,
        /** A linguagem pedida, como regra de verdade sobre a frase. */
        val alvo: (List<String>) -> Boolean,
    ) {
        val prods: List<Production> = pecas.map { Grammar.parse(it).productions.first() }
        val inicial: String = prods.first().lhs
        val nts: Set<String> = prods.map { it.lhs }.toSet()
        val terminais: List<String> = prods.flatMap { it.rhs }.filter { it !in nts }.distinct()
    }

    val fases: List<Fase> by lazy {
        fun anbn(w: List<String>) = w.size % 2 == 0 && w.take(w.size / 2).all { it == "a" } && w.drop(w.size / 2).all { it == "b" }
        fun equilibrado(w: List<String>): Boolean {
            var d = 0
            for (s in w) { d += if (s == "(") 1 else if (s == ")") -1 else return false; if (d < 0) return false }
            return d == 0
        }
        fun conta(w: List<String>, ops: Set<String>) = w.size % 2 == 1 && w.withIndex().all { (i, s) -> if (i % 2 == 0) s == "id" else s in ops }
        listOf(
            Fase(
                "fabrica_anbn", tx("fabrica_fase_anbn"), tx("fabrica_pedido_anbn"),
                listOf("S -> a S b", "S -> ε", "S -> a b S", "S -> S b", "S -> a S"),
                listOf("", "a b", "a a b b", "a a a b b b", "a", "b a", "a b a b", "a a b"), 6, false,
                listOf(tx("fabrica_dica_anbn_1"), tx("fabrica_dica_anbn_2")), setOf(0, 1),
            ) { anbn(it) },
            Fase(
                "fabrica_parenteses", tx("fabrica_fase_parenteses"), tx("fabrica_pedido_parenteses"),
                listOf("S -> ( S ) S", "S -> ε", "S -> ( S", "S -> S )", "S -> ) S ("),
                listOf("", "( )", "( ) ( )", "( ( ) )", "(", ") (", "( ( )", "( ) )"), 6, false,
                listOf(tx("fabrica_dica_parenteses_1"), tx("fabrica_dica_parenteses_2")), setOf(0, 1),
            ) { equilibrado(it) },
            Fase(
                "fabrica_soma", tx("fabrica_fase_soma"), tx("fabrica_pedido_soma"),
                listOf("E -> E + E", "E -> id", "E -> E + id", "E -> + id", "E -> id +"),
                listOf("id", "id + id", "id + id + id", "+ id", "id +", "id id"), 5, true,
                listOf(tx("fabrica_dica_soma_1"), tx("fabrica_dica_soma_2")), setOf(1, 2),
            ) { conta(it, setOf("+")) },
            Fase(
                "fabrica_vezes", tx("fabrica_fase_vezes"), tx("fabrica_pedido_vezes"),
                listOf("E -> E + T", "E -> T", "T -> T * id", "T -> id", "E -> E * E", "E -> id"),
                listOf("id", "id + id", "id * id", "id + id * id", "id * id + id", "id + * id", "* id"), 5, true,
                listOf(tx("fabrica_dica_vezes_1"), tx("fabrica_dica_vezes_2")), setOf(0, 1, 2, 3),
            ) { conta(it, setOf("+", "*")) },
        )
    }

    /** A gramática das máquinas ligadas, na ordem das peças. Null sem máquina para o símbolo inicial. */
    fun gramatica(f: Fase, ligadas: Set<Int>): Grammar? {
        val ps = ligadas.sorted().map { f.prods[it] }
        if (ps.none { it.lhs == f.inicial }) return null
        return Grammar(f.inicial, ps)
    }

    /** O que sai da fábrica: frase → número de jeitos (1 ou 2). */
    fun saida(f: Fase, ligadas: Set<Int>): Map<List<String>, Int> =
        Frases.geradas(ligadas.sorted().map { f.prods[it] }, f.inicial, f.nts, f.maxLen)

    /** As frases da esteira, das curtas para as longas. */
    fun esteira(f: Fase, ligadas: Set<Int>, n: Int = 10): List<Pair<List<String>, Int>> =
        saida(f, ligadas).entries.sortedWith(compareBy(Frases.ORDEM) { it.key }).take(n).map { it.key to it.value }

    private enum class Erro { FALTA, SOBRA, DOIS }

    fun confere(f: Fase, ligadas: Set<Int>): Veredito {
        if (gramatica(f, ligadas) == null) return Veredito(emptyList(), -1, tx("fabrica_sem_inicial", f.inicial))
        val out = saida(f, ligadas)
        val lista = f.testes.map { tokensOf(it) }
        fun erro(w: List<String>): Erro? {
            val n = out[w] ?: 0
            return when {
                f.alvo(w) && n == 0 -> Erro.FALTA
                !f.alvo(w) && n > 0 -> Erro.SOBRA
                f.semAmbiguidade && n >= 2 -> Erro.DOIS
                else -> null
            }
        }
        // O menor erro fora da lista: frase que sai e não podia, que não sai e devia, ou que sai de dois jeitos.
        val candidatas = (out.keys + Frases.todas(f.terminais, f.maxLen).filter { f.alvo(it) }).distinct().sortedWith(Frases.ORDEM)
        val achado = candidatas.firstOrNull { it !in lista && erro(it) != null }
        val todas = lista.map { it to false } + listOfNotNull(achado?.let { it to true })
        val res = todas.map { (w, ach) ->
            val n = out[w] ?: 0
            val obtido = when { n == 0 -> tx("fabrica_nao_sai"); n >= 2 && f.semAmbiguidade -> tx("fabrica_sai_dois"); else -> tx("fabrica_sai") }
            val esperado = when { !f.alvo(w) -> tx("fabrica_nao_sai"); f.semAmbiguidade -> tx("fabrica_sai_um"); else -> tx("fabrica_sai") }
            ResultadoTeste(Frases.texto(w), erro(w) == null, obtido, esperado, ach)
        }
        val i = res.indexOfFirst { !it.passou }
        if (i < 0) return Veredito(res, null, if (f.semAmbiguidade) tx("fabrica_passou_um", res.size, f.maxLen) else tx("fabrica_passou", res.size, f.maxLen))
        val w = res[i].entrada
        val frase = when (erro(todas[i].first)!!) {
            Erro.FALTA -> tx("fabrica_falha_falta", w)
            Erro.SOBRA -> tx("fabrica_falha_sobra", w)
            Erro.DOIS -> tx("fabrica_falha_dois", w)
        }
        return Veredito(res, i, frase)
    }

    /** A terceira dica: uma máquina só, a primeira que está ligada sem precisar ou desligada fazendo falta. */
    fun ondeOlhar(f: Fase, ligadas: Set<Int>): String? {
        val sobra = f.pecas.indices.firstOrNull { it in ligadas && it !in f.solucao }
        if (sobra != null) return tx("fabrica_olhe_desligue", f.prods[sobra].toString())
        val falta = f.pecas.indices.firstOrNull { it !in ligadas && it in f.solucao } ?: return null
        return tx("fabrica_olhe_ligue", f.prods[falta].toString())
    }

    /** Um quadro: a forma da frase na esteira, a máquina que acabou de trabalhar e de qual árvore é. */
    data class Quadro(val forma: List<String>, val peca: Int?, val arvore: Int, val nota: String)

    /** Como a fábrica monta [w]: até duas derivações mais à esquerda, cada uma com a árvore. */
    data class Montagem(val frase: List<String>, val quadros: List<Quadro>, val arvores: List<ParseNode>, val grammar: Grammar?)

    fun monta(f: Fase, ligadas: Set<Int>, w: List<String>): Montagem {
        val g = gramatica(f, ligadas) ?: return Montagem(w, listOf(Quadro(listOf(f.inicial), null, 0, tx("fabrica_sem_inicial", f.inicial))), emptyList(), null)
        val ordem = ligadas.sorted()
        val ds = DerivationHelper.leftmostDerivations(g, w, limit = 2)
        if (ds.isEmpty()) return Montagem(w, listOf(Quadro(listOf(f.inicial), null, 0, tx("fabrica_nota_nao_monta", Frases.texto(w)))), emptyList(), g)
        val usadas = if (f.semAmbiguidade) ds else ds.take(1)
        val q = mutableListOf<Quadro>()
        usadas.forEachIndexed { k, d ->
            var forma = SententialForm(listOf(g.start))
            q += Quadro(forma.symbols, null, k, if (k == 0) tx("fabrica_nota_inicio", g.start) else tx("fabrica_nota_outro_jeito", Frases.texto(w)))
            for (p in d) {
                val idx = forma.leftmostNT(g)
                forma = forma.apply(idx, p)
                q += Quadro(forma.symbols, ordem[p.id], k, tx("fabrica_nota_maquina", p.toString(), p.lhs))
            }
        }
        val fim = if (usadas.size >= 2) tx("fabrica_nota_duas", Frases.texto(w)) else tx("fabrica_nota_saiu", Frases.texto(w))
        q += Quadro(w, null, usadas.size - 1, fim)
        return Montagem(w, q, usadas.map { DerivationHelper.treeOf(g, it) }, g)
    }
}
