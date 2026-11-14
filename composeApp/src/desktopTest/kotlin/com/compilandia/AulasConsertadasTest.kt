package com.compilandia

import com.compilandia.content.episodes.ARVORE_MENOS_NOVA
import com.compilandia.content.episodes.ARVORE_MENOS_VELHA
import com.compilandia.content.episodes.GRAMATICA_EXPR
import com.compilandia.content.episodes.GRAMATICA_FIRST_3
import com.compilandia.content.episodes.GRAMATICA_FOLLOW_3
import com.compilandia.content.episodes.GRAMATICA_LL1_3
import com.compilandia.content.episodes.GRAMATICA_LL1_4
import com.compilandia.content.episodes.GRAMATICA_PEG_FOLLOW
import com.compilandia.content.episodes.AFN_3
import com.compilandia.content.episodes.AFD_LIVRO
import com.compilandia.content.episodes.AFD_ASBS
import com.compilandia.learning.Skill
import com.compilandia.engine.Minimizer
import com.compilandia.content.episodes.tabelaLl1DaAula
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Table
import com.compilandia.content.episodes.tabelaSlrDaAula
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.ParseNode
import com.compilandia.engine.render
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Confere contra o motor o conteúdo novo das aulas consertadas na revisão. */
class AulasConsertadasTest {
    /** Toda subárvore usa uma regra da gramática (número vira n), e as folhas formam a frase. */
    private fun confere(arvore: ParseNode, gramatica: String) {
        val g = Grammar.parse(gramatica)
        fun simbolo(s: String) = if (s.all { it.isDigit() }) "n" else s
        fun anda(no: ParseNode) {
            if (no.isLeaf) return
            val rhs = no.children.map { simbolo(it.symbol) }.filter { it != "ε" }
            assertTrue(g.productionsOf(no.symbol).any { it.rhs == rhs }, "${no.symbol} -> $rhs não é regra de: $gramatica")
            no.children.forEach(::anda)
        }
        anda(arvore)
        assertEquals(listOf("9", "-", "5", "-", "2"), arvore.leaves())
    }

    @Test fun arvoreVelhaPendeParaAEsquerda() {
        confere(ARVORE_MENOS_VELHA, "E -> E - n | n")
        assertEquals("E", ARVORE_MENOS_VELHA.children.first().symbol, "o filho da esquerda é o E que cresce")
    }

    @Test fun arvoreNovaPendeParaADireita() {
        confere(ARVORE_MENOS_NOVA, "E -> n E'\nE' -> - n E' | ε")
        assertEquals("E'", ARVORE_MENOS_NOVA.children.last().symbol, "o filho da direita é o E' que cresce")
    }

    /** A aula pergunta em qual coluna o estado 2 não reduz: n vazia, + e $ com r2. */
    @Test fun tabelaSlrInteira() {
        val t = tabelaSlrDaAula()
        assertEquals(listOf("", "n", "+", "$", "E"), t[0])
        assertEquals(6, t.size, "cabeçalho e os 5 estados de E -> E + n | n")
        assertEquals(listOf("2", "", "r2", "r2", ""), t[3])
        assertEquals("1", t[1][4], "GOTO de E no estado 0")
        assertTrue(t.drop(1).any { "ok" in it }, "a linha que aceita")
    }

    /** O caso difícil do FOLLOW: C some, então o d também entra no FOLLOW de B (a aula pede c e d, e não $ nem a). */
    @Test fun followComVizinhoQueSome() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_FOLLOW_3.joinToString("\n")))
        assertEquals(setOf("c", "d"), f.follow["B"])
        assertTrue("ε" in f.firstOfSymbol("C"), "C precisa ser anulável, senão o d não entra")
    }

    /** FIRST, caso difícil: B some sem ε escrito, e na ordem das regras a primeira rodada deixa S só com o d. */
    @Test fun firstComAnulavelSemEpsilonEscrito() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_FIRST_3.joinToString("\n")))
        assertEquals(setOf("b", "c", "ε"), f.first["B"])
        assertEquals(setOf("b", "c", "d"), f.first["S"])
        val deS = f.steps.filter { it.kind == com.compilandia.engine.SetStep.Kind.FIRST && it.nonTerminal == "S" }
        assertEquals(setOf("d"), deS.first().added, "a primeira coisa que entra em S é o d")
        assertTrue(f.firstRounds >= 3, "o b e o c chegam em rodadas seguintes")
    }

    /** As perguntas novas do começo das aulas: o que vem depois de E no SLR, e a casa M[X, id] vazia na descida. */
    @Test fun perguntasDoComeco() {
        assertEquals(setOf("+", "$"), FirstFollow(Grammar.parse("E -> E + n | n")).follow["E"])
        val g = FirstFollow(Grammar.parse(GRAMATICA_EXPR.joinToString("\n")))
        assertTrue("id" !in g.first.getValue("X") && "id" !in g.follow.getValue("X"), "M[X, id] tem de ser vazia")
        assertTrue("+" in g.first.getValue("X") && ")" in g.follow.getValue("X"))
    }

    /** Tabela LL(1), caso difícil: S -> A B não tem ε escrito, mas vai em a, b e também no $ (FOLLOW de S). */
    @Test fun tabelaComRegraAnulavelSemEpsilon() {
        val t = tabelaLl1DaAula(GRAMATICA_LL1_3)
        assertEquals(listOf("", "a", "b", "$"), t[0])
        assertEquals(listOf("S", "A B", "A B", "A B"), t[1])
        val m = LL1Table(Grammar.parse(GRAMATICA_LL1_3.joinToString("\n")))
        assertTrue(m.isLL1, "nenhuma casa com duas regras")
        assertEquals(setOf("$"), m.ff.follow["S"])
    }

    /** Conflito FIRST × FIRST: as duas regras de S na casa (S, a); a fatoração da aula conserta. */
    @Test fun tabelaComConflitoFirstFirst() {
        val m = LL1Table(Grammar.parse(GRAMATICA_LL1_4.joinToString("\n")))
        assertEquals(listOf("S" to "a"), m.conflicts)
        assertEquals(2, m.cell("S", "a").size)
        assertTrue(tabelaLl1DaAula(GRAMATICA_LL1_4)[1].drop(2).all { it.isEmpty() }, "as outras casas ficam vazias")
        val fatorada = Grammar.parse("S -> a S'\nS' -> b | c")
        assertTrue(LL1Table(fatorada).isLL1)
        assertEquals(fatorada.toText(), GrammarTransform.leftFactor(Grammar.parse(GRAMATICA_LL1_4.joinToString("\n"))).toText())
    }

    /** Pegadinha do FOLLOW: B pode sumir, então o FOLLOW de A tem o d de B e o c que vem depois (não tem $ nem ε). */
    @Test fun pegadinhaDoFollowComVizinhoQueSome() {
        val f = FirstFollow(Grammar.parse(GRAMATICA_PEG_FOLLOW.joinToString("\n")))
        assertEquals(setOf("d", "c"), f.follow["A"])
    }

    /** AFN de (ab|a)c: o ε-fecho depois do move acrescenta o 6, e o move por b no começo é vazio. */
    @Test fun subconjuntosComFechoQueMudaEMoveVazio() {
        val ini = AFN_3.epsClosure(setOf(0))
        assertEquals(setOf(0, 1, 4), ini)
        assertEquals(setOf(2, 5), AFN_3.move(ini, 'a'))
        assertEquals(setOf(2, 5, 6), AFN_3.epsClosure(AFN_3.move(ini, 'a')))
        assertTrue(AFN_3.move(ini, 'b').isEmpty())
        for (w in listOf("abc", "ac")) assertTrue(AFN_3.accepts(w), w)
        for (w in listOf("a", "bc", "abbc", "c")) assertTrue(!AFN_3.accepts(w), w)
    }

    /** Minimização do AFD do livro: três rodadas de marcas, e só A com C fica em branco. */
    @Test fun minimizacaoDoAfdDoLivro() {
        val r = Minimizer.run(AFD_LIVRO)
        assertEquals(null, r.deadState, "o AFD do livro já é completo")
        val porRodada = r.marks.groupBy({ it.round }, { it.p to it.q })
        assertEquals(setOf(0 to 4, 1 to 4, 2 to 4, 3 to 4), porRodada[1]!!.toSet())
        assertEquals(setOf(0 to 3, 1 to 3, 2 to 3), porRodada[2]!!.toSet())
        assertEquals(setOf(0 to 1, 1 to 2), porRodada[3]!!.toSet())
        assertEquals(3, r.rounds)
        assertTrue(setOf(0, 2) in r.classes)
        assertEquals(4, r.minimized.stateCount)
    }

    /** Pegadinha do handle: reduzir E + T com * na entrada é beco sem saída, porque * não está no FOLLOW de E. */
    @Test fun handleReduzirCedoEBecoSemSaida() {
        val g = Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> id")
        assertTrue("*" !in FirstFollow(g).follow.getValue("E"))
        assertTrue(g.productions.none { p -> p.rhs.zipWithNext().any { it == "E" to "*" } }, "nenhuma regra tem E seguido de *")
    }

    /** a*b*: o Minimizer põe o estado morto na tabela; P e Q caem na rodada 2 por causa dele, e nada se junta. */
    @Test fun minimizacaoComEstadoMorto() {
        val r = Minimizer.run(AFD_ASBS)
        assertEquals(2, r.deadState)
        assertEquals(setOf(0 to 2, 1 to 2), r.marks.filter { it.round == 1 }.map { it.p to it.q }.toSet())
        assertEquals(listOf(0 to 1), r.marks.filter { it.round == 2 }.map { it.p to it.q })
        assertTrue(r.classes.all { it.size == 1 }, "nada se junta")
        // Sem o morto, juntar P e Q daria um estado com a e b em laço: aceitaria b a.
        val junto = com.compilandia.engine.DFA(1, 0, setOf(0), listOf('a', 'b'), mapOf((0 to 'a') to 0, (0 to 'b') to 0))
        assertTrue(junto.accepts("ba") && !AFD_ASBS.accepts("ba"))
    }

    /**
     * Descida recursiva: o código da aula (épsilon no else de X) volta de E com id ainda na entrada,
     * e só o teste do $ no main pega o erro. O analisador por tabela para antes, na casa vazia M[X, id].
     */
    @Test fun descidaPrecisaDoTesteDoFim() {
        val ent = listOf("id", "id", "$"); var i = 0
        fun casa(t: String) { check(ent[i] == t) { "erro em casa($t)" }; i++ }
        lateinit var e: () -> Unit
        fun t() { if (ent[i] == "id") casa("id") else { casa("("); e(); casa(")") } }
        fun x() { if (ent[i] == "+") { casa("+"); t(); x() } }
        e = { t(); x() }
        e()
        assertEquals("id", ent[i], "E voltou e o token ainda é id: o main precisa do teste do $")
        val tab = LL1Table(Grammar.parse(GRAMATICA_EXPR.joinToString("\n")))
        assertTrue(tab.cell("X", "id").isEmpty())
        assertTrue(com.compilandia.engine.LL1Parser.trace(tab, listOf("id", "id")).last().action is com.compilandia.engine.LLAction.Error)
    }

    /** Pré-requisitos da revisão: a Ponte antes da tabela LL(1), a ambiguidade antes da recursão, o preditivo antes da descida. */
    @Test fun preRequisitosDaRevisao() {
        assertTrue(Skill.AMBIGUITY in Skill.LEFT_RECURSION.prereqs)
        assertTrue(Skill.LL1_TABLE.prereqs.containsAll(listOf(Skill.LEFT_RECURSION, Skill.LEFT_FACTORING)))
        assertTrue(Skill.LL1_PARSING in Skill.RECURSIVE_DESCENT.prereqs)
    }

    /** Itens LR(0) de S -> ( S ) | id: seis estados; lendo ( no estado do ( volta a ele mesmo; um estado aceita. */
    @Test fun automatoLr0Inteiro() {
        val aut = com.compilandia.engine.LRAutomaton.lr0(Grammar.parse(com.compilandia.content.episodes.GRAMATICA_LR_2.joinToString("\n")))
        assertEquals(6, aut.states.size)
        val par = aut.transitions.getValue(0 to "(")
        assertEquals(par, aut.transitions.getValue(par to "("), "o goto por ( no estado do ( volta a ele")
        assertEquals(aut.transitions.getValue(0 to "id"), aut.transitions.getValue(par to "id"), "id leva ao mesmo estado dos dois lados")
        val aceita = aut.transitions.getValue(0 to "S")
        assertTrue(aut.states[aceita].items.any { it.prodId == 0 && it.dot == 1 }, "S' -> S • está no estado que aceita")
    }

    /** ε como produção: S -> a S b | ε gera a b, e não b a. */
    @Test fun regraVaziaGeraSentenca() {
        val g = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_EPS.joinToString("\n"))
        assertTrue(com.compilandia.engine.Earley(g).accepts(listOf("a", "b")))
        assertTrue(!com.compilandia.engine.Earley(g).accepts(listOf("b", "a")))
    }

    /** As três condições LL(1) entraram na ficha da tabela. */
    @Test fun fichaDaTabelaTemAsTresCondicoes() {
        val ficha = com.compilandia.content.Library.cards.first { it.skill == Skill.LL1_TABLE }
        val perguntas = ficha.flashcards.map { it.first }
        for (k in listOf("ll1_cond1_q", "ll1_cond2_q", "ll1_cond3_q")) assertTrue(com.compilandia.textos.tx(k) in perguntas, k)
    }

    /** Derivação mais à direita de float * id ;: as formas da tela batem com a troca do não-terminal mais à direita. */
    @Test fun derivacaoMaisADireita() {
        val g = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_CFG_2.joinToString("\n"))
        fun anda(forma: List<String>, direita: Boolean, alvo: List<String>): List<String> {
            val i = if (direita) forma.indexOfLast { g.isNonTerminal(it) } else forma.indexOfFirst { g.isNonTerminal(it) }
            val nt = forma[i]
            // A regra cujos terminais estão todos na frase; entre Var -> id e Var -> * id, a mais longa.
            val p = g.productionsOf(nt).filter { p -> alvo.containsAll(p.rhs.filter { !g.isNonTerminal(it) }) }.maxBy { it.rhs.size }
            return forma.take(i) + p.rhs + forma.drop(i + 1)
        }
        for ((direita, esperado) in listOf(false to com.compilandia.content.episodes.DERIV_ESQ_CFG_2, true to com.compilandia.content.episodes.DERIV_DIR_CFG_2)) {
            var forma = listOf("Decl"); val formas = mutableListOf(forma.joinToString(" "))
            while (forma.any { g.isNonTerminal(it) }) { forma = anda(forma, direita, com.compilandia.content.episodes.FRASE_CFG_2); formas += forma.joinToString(" ") }
            assertEquals(esperado, formas)
        }
    }

    /** Tabela SLR inteira de S -> ( S ) | id: s no estado do ( com id, r1 no estado do ) em ) e $, sem conflito. */
    @Test fun tabelaSlrInteiraDosParenteses() {
        val gram = com.compilandia.content.episodes.GRAMATICA_LR_2
        val t = com.compilandia.engine.LRTable(Grammar.parse(gram.joinToString("\n")), com.compilandia.engine.LRKind.SLR1)
        assertTrue(t.conflicts.isEmpty())
        val aut = t.automaton
        val par = aut.transitions.getValue(0 to "("); val id = aut.transitions.getValue(0 to "id")
        val fecha = aut.transitions.getValue(aut.transitions.getValue(par to "S") to ")")
        val m = com.compilandia.content.episodes.tabelaSlr(gram, listOf("id", "(", ")", "$"), listOf("S"))
        assertEquals("s$id", m[par + 1][1])
        assertEquals(listOf("", "", "r1", "r1", ""), m[fecha + 1].drop(1))
    }

    /** E -> E + T | T, T -> T * n | n: n + n * n tem uma árvore só, com o vezes dentro do T. */
    @Test fun etfDaUmaArvoreSo() {
        val g = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_ETF.joinToString("\n"))
        val ds = com.compilandia.engine.DerivationHelper.leftmostDerivations(g, listOf("n", "+", "n", "*", "n"), limit = 10)
        assertEquals(1, ds.size)
        assertEquals(com.compilandia.content.episodes.ARVORE_ETF, com.compilandia.engine.DerivationHelper.treeOf(g, ds[0]))
    }

    private fun regras(g: Grammar) = g.productions.map { it.lhs to it.rhs }.toSet()

    /** Todas as frases de até [max] terminais: derivação mais à esquerda, cortando a forma que passou do tamanho. */
    private fun frases(g: Grammar, max: Int): Set<List<String>> {
        val out = mutableSetOf<List<String>>(); val vistos = mutableSetOf<List<String>>()
        val fila = ArrayDeque(listOf(listOf(g.start)))
        while (fila.isNotEmpty()) {
            val f = fila.removeFirst()
            if (!vistos.add(f) || f.count { !g.isNonTerminal(it) } > max) continue
            val i = f.indexOfFirst { g.isNonTerminal(it) }
            if (i < 0) { out += f; continue }
            for (p in g.productionsOf(f[i])) fila += f.take(i) + p.rhs + f.drop(i + 1)
        }
        return out
    }

    /** E/T/F da prática: a receita do motor dá a gramática da tela, e id + id * id sai dela. */
    @Test fun etfDaPratica() {
        val antes = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_ETF_REC.joinToString("\n"))
        val depois = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_ETF_DEPOIS.joinToString("\n"))
        assertEquals(regras(depois), regras(GrammarTransform.eliminateImmediateLeftRecursion(antes)))
        assertTrue(GrammarTransform.leftRecursiveNTs(depois).isEmpty())
        val frase = listOf("id", "+", "id", "*", "id")
        assertEquals(1, com.compilandia.engine.DerivationHelper.leftmostDerivations(depois, frase, limit = 10).size)
        assertEquals(frases(antes, 5), frases(depois, 5))
    }

    /** Recursão indireta até o fim: a receita no S aberto, o A que sobra, a ordem do livro e a mesma linguagem nas três. */
    @Test fun recursaoIndiretaAteOFim() {
        val antes = Grammar.parse(com.compilandia.content.episodes.INDIRETA_ANTES.joinToString("\n"))
        val depois = Grammar.parse(com.compilandia.content.episodes.INDIRETA_DEPOIS.joinToString("\n"))
        val outra = Grammar.parse(com.compilandia.content.episodes.INDIRETA_OUTRA.joinToString("\n"))
        assertEquals(regras(depois), regras(GrammarTransform.eliminateImmediateLeftRecursion(Grammar.parse("S -> S c a | d a | b"))))
        // A ordem do livro (S, A): troca S dentro de A, e o A fica com recursão direta.
        val aAberto = Grammar.parse("A -> A a c | b c | d")
        assertEquals(regras(Grammar.parse(com.compilandia.content.episodes.INDIRETA_OUTRA.drop(1).joinToString("\n"))), regras(GrammarTransform.eliminateImmediateLeftRecursion(aAberto)))
        assertTrue("A" !in depois.nonTerminals)
        for (g in listOf(depois, outra)) assertTrue(GrammarTransform.leftRecursiveNTs(g).isEmpty())
        val l = frases(antes, 7)
        assertEquals(l, frases(depois, 7)); assertEquals(l, frases(outra, 7))
    }

    /** Bison: com %right, 2^3^2 dá 512 e sem conflito; com %left, 64. */
    @Test fun potenciaComRight() {
        val spec = com.compilandia.content.episodes.BISON_POT.joinToString("\n")
        val right = com.compilandia.engine.MiniBison.compile(spec)
        assertTrue(right.conflicts.isEmpty())
        assertEquals(512L, right.run("2 ^ 3 ^ 2").value)
        assertEquals(64L, com.compilandia.engine.MiniBison.compile(spec.replace("%right", "%left")).run("2 ^ 3 ^ 2").value)
    }

    /** Minimização, primeiro exemplo: A com B e A com C caem porque vão para C e D; B com C vão os dois para D e ficam. No livro, A com C fica. */
    @Test fun segundoXDaMinimizacao() {
        val afd = com.compilandia.engine.DFA(4, 0, setOf(3), listOf('a', 'b'), mapOf((0 to 'a') to 1, (0 to 'b') to 2, (1 to 'b') to 3, (2 to 'b') to 3))
        val marcados = Minimizer.run(afd).marks.map { it.p to it.q }.filter { (p, q) -> p < 4 && q < 4 }.toSet()
        assertEquals(setOf(0 to 1, 0 to 2, 0 to 3, 1 to 3, 2 to 3), marcados)
        assertEquals(3, afd.delta.getValue(1 to 'b')); assertEquals(3, afd.delta.getValue(2 to 'b'))
        assertTrue((0 to 2) !in Minimizer.run(AFD_LIVRO).marks.map { it.p to it.q })
    }

    /** Menos unário: sem %prec, o vezes roda antes do sinal; com %prec UMINUS, o sinal roda antes. Os dois dão -6 e não têm conflito. */
    @Test fun menosUnarioComPrec() {
        for ((spec, sinalPrimeiro) in listOf(com.compilandia.content.episodes.BISON_UNARIO to false, com.compilandia.content.episodes.BISON_UNARIO_PREC to true)) {
            val b = com.compilandia.engine.MiniBison.compile(spec.joinToString("\n"))
            assertTrue(b.conflicts.isEmpty())
            val r = b.run("- 2 * 3")
            assertEquals(-6L, r.value)
            // reductions[0] é expr -> NUM do 2; a segunda é o sinal (com %prec) ou o NUM do 3 (sem).
            assertEquals(sinalPrimeiro, "'-' expr" in r.reductions[1].substringBefore("⇒"), r.reductions.toString())
        }
    }

    /** Ponteiros: o SLR tem conflito; no LR(1), L -> • id ganha = de S -> • L = R, $, e o estado depois do L tem os dois itens da tela. LALR sem conflito. */
    @Test fun lookaheadDosPonteiros() {
        val g = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_PONTEIROS.joinToString("\n"))
        assertTrue(com.compilandia.engine.LRTable(g, com.compilandia.engine.LRKind.SLR1).conflicts.isNotEmpty())
        assertTrue(com.compilandia.engine.LRTable(g, com.compilandia.engine.LRKind.LALR1).conflicts.isEmpty())
        val aut = com.compilandia.engine.LRAutomaton.lr1(g)
        val ga = aut.grammar
        fun txt(i: com.compilandia.engine.LRItem) = i.render(ga).replace("→", "->")
        val s0 = aut.states[0].items.map(::txt)
        assertTrue("L -> • id, =" in s0, s0.toString())
        val depoisDoL = aut.states[aut.transitions.getValue(0 to "L")].items.map(::txt).toSet()
        assertEquals(com.compilandia.content.episodes.LALR_ESTADO_L.map { it.replace(" , ", ", ") }.toSet(), depoisDoL)
    }

    /** Último final: com 3.x, a regra do NUM passa pelo ponto, trava no x e volta ao 3; com 3.5, leva tudo. */
    @Test fun scannerVoltaAoUltimoFinal() {
        val f = com.compilandia.engine.MiniFlex.parseSpec(com.compilandia.content.episodes.FLEX_ULTIMO.joinToString("\n"))
        assertEquals(listOf("NUM" to "3", "PONTO" to ".", "ID" to "x"), f.scan(com.compilandia.content.episodes.FLEX_ULTIMO_ENTRADA).tokens.map { it.token to it.text })
        assertEquals(listOf("NUM" to "3.5"), f.scan("3.5").tokens.map { it.token to it.text })
    }

    /** Thompson: a|b dá 6 estados e 4 setas ε, como a fala diz; com a estrela, 8. */
    @Test fun thompsonDaUniao() {
        val u = com.compilandia.engine.NFA.thompson(com.compilandia.engine.Regex0.parse(com.compilandia.content.episodes.THOMPSON_UNIAO))
        assertEquals(6, u.stateCount)
        assertEquals(setOf(0 to 1, 0 to 3, 2 to 5, 4 to 5), u.edges.filter { it.second == null }.map { it.first to it.third }.toSet())
        assertEquals(4, u.edges.count { it.second == null })
        assertEquals(5, u.accept)
        assertEquals(8, com.compilandia.engine.NFA.thompson(com.compilandia.engine.Regex0.parse("(${com.compilandia.content.episodes.THOMPSON_UNIAO})*")).stateCount)
    }

    /** Fusão do LALR: em S -> C C, I3 e I6 têm o mesmo kernel sem lookahead e o mesmo núcleo; I2 divide duas linhas do fecho com I3, mas não o kernel. 10 estados viram 7. */
    @Test fun fusaoComEstadosInteiros() {
        val lr1 = com.compilandia.engine.LRAutomaton.lr1(Grammar.parse(com.compilandia.content.episodes.GRAMATICA_FUSAO.joinToString("\n")))
        assertEquals(10, lr1.states.size)
        assertEquals(7, lr1.mergeLALR().states.size)
        assertEquals(7, com.compilandia.engine.LRAutomaton.lr0(Grammar.parse(com.compilandia.content.episodes.GRAMATICA_FUSAO.joinToString("\n"))).states.size)
        fun nucleo(sid: Int) = lr1.states[sid].items.map { it.core() }.toSet()
        fun kernel(sid: Int) = lr1.states[sid].kernel.map { it.core() }.toSet()
        assertEquals(nucleo(3), nucleo(6)); assertEquals(kernel(3), kernel(6))
        assertTrue(nucleo(3) != nucleo(2) && nucleo(3) != nucleo(4))
        assertEquals(2, (nucleo(3) intersect nucleo(2)).size)
        assertTrue((kernel(3) intersect kernel(2)).isEmpty())
        // O c do primeiro C leva a I3 e o do segundo, a I6, como a fala diz.
        assertEquals(3, lr1.transitions[0 to "c"]); assertEquals(6, lr1.transitions[2 to "c"])
        val i3 = com.compilandia.content.episodes.itensAgrupados(lr1, lr1.states[3].items)
        assertEquals(listOf("C -> c • C , c d", "C -> • c C , c d", "C -> • d , c d"), i3)
        val juntos = com.compilandia.content.episodes.itensAgrupados(lr1, lr1.states[3].items + lr1.states[6].items)
        assertTrue(juntos.all { it.endsWith(", c d $") }, juntos.toString())
        // Os outros pares da fala: I4 com I7 e I8 com I9.
        assertEquals(nucleo(4), nucleo(7)); assertEquals(nucleo(8), nucleo(9))
    }

    /** Operadores da aula de regex: cada expressão casa o que a cena mostra, e só a classe do meio separa o 1x. */
    @Test fun operadoresDaRegex() {
        fun casa(r: String, s: String) = com.compilandia.engine.DFA.fromRegex(r).accepts(s)
        assertTrue(casa(com.compilandia.content.episodes.REGEX_SEQ, "ab") && !casa(com.compilandia.content.episodes.REGEX_SEQ, "a") && !casa(com.compilandia.content.episodes.REGEX_SEQ, "ba"))
        assertTrue(listOf("", "b", "bb", "bbb").all { casa(com.compilandia.content.episodes.REGEX_ESTRELA, it) })
        assertEquals(setOf("a", "bc"), com.compilandia.content.episodes.casamRegex(com.compilandia.content.episodes.REGEX_BARRA, com.compilandia.content.episodes.REGEX_BARRA_OPCOES).map { com.compilandia.content.episodes.REGEX_BARRA_OPCOES[it] }.toSet())
        assertTrue(casa(com.compilandia.content.episodes.REGEX_PARENTESES, "ac") && casa(com.compilandia.content.episodes.REGEX_PARENTESES, "bc") && !casa(com.compilandia.content.episodes.REGEX_PARENTESES, "a"))
        assertEquals(setOf("abb", "babb"), com.compilandia.content.episodes.casamRegex(com.compilandia.content.episodes.REGEX_LIVRO, com.compilandia.content.episodes.REGEX_LIVRO_OPCOES).map { com.compilandia.content.episodes.REGEX_LIVRO_OPCOES[it] }.toSet())
        assertEquals(1, com.compilandia.content.episodes.classeDosNomes())
        fun classe(i: Int) = Regex(com.compilandia.engine.MiniFlex.toKotlinRegex(com.compilandia.content.episodes.REGEX_CLASSES[i]))
        assertTrue(classe(0).matches("1x"))
        assertTrue(!classe(2).matches("conta"))
    }

    /** As peças do fat.asm no MipsVM: addi abre 8 bytes, 4($sp) é $sp + 4, jal guarda a linha de baixo em $ra e jr $ra volta para ela. */
    @Test fun pecasDoFatAsm() {
        fun asm(s: String) = com.compilandia.content.p5Asm(s)
        val vm = com.compilandia.engine.MipsVM(".text\nmain:\nli \$sp, 1008\n" + asm(com.compilandia.content.P5_PECA_SP) +
            "\nli \$ra, 77\nli \$a0, 3\n" + asm(com.compilandia.content.P5_PECA_DESLOCAMENTO) + "\nli \$v0, 10\nsyscall").run()
        assertEquals(1000L, vm.regs["\$sp"])
        assertEquals(77L, vm.mem[1004L]); assertEquals(3L, vm.mem[1000L])
        val chamada = com.compilandia.engine.MipsVM(".text\nmain:\n" + asm(com.compilandia.content.P5_PECA_JAL) +
            "\nli \$v0, 10\nsyscall\nfat:\nmove \$t1, \$ra\nli \$v0, 5\n" + asm(com.compilandia.content.P5_PECA_RA)).run()
        assertEquals(1L, chamada.regs["\$t1"]) // a instrução logo abaixo do jal
        assertEquals(5L, chamada.regs["\$a0"]) // o move depois do jal rodou: o jr $ra voltou para ele
    }
}
