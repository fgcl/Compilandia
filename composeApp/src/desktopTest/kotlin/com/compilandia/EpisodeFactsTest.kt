package com.compilandia

import com.compilandia.content.Look
import com.compilandia.content.episodes.Episodes
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprParser
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.Mips
import com.compilandia.engine.Minimizer
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Voz bonita falando bobagem é pior que texto. Aqui o que os episódios afirmam é conferido contra o motor. */
class EpisodeFactsTest {
    private val g = Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id | ( E )")

    @Test fun haUmEpisodioPorAssunto() {
        val faltam = Skill.entries.filter { Episodes.of(it) == null }
        assertTrue(faltam.isEmpty(), "sem episódio: $faltam")
        assertEquals(Skill.entries.size, Episodes.basicos.distinctBy { it.skill }.size)
    }

    @Test fun cadaEpisodioPedeUmToqueETemRitmoCurto() {
        for (e in Episodes.all) {
            assertTrue(e.passos.any { it.ask != null || it.brinquedo != null }, "${e.title} não pede nenhuma ação")
            // A2: a aula básica longa tem de 14 a 22 cenas e ao menos 2 perguntas; as outras seguem curtas até serem refeitas.
            val longa = e.temporada == com.compilandia.content.Temporada.BASICO && e.skill in com.compilandia.content.episodes.ASSUNTOS_COM_AULA_LONGA
            if (longa) {
                assertTrue(e.passos.size in 14..22, "${e.title} (longa) tem ${e.passos.size} passos")
                assertTrue(e.passos.count { it.ask != null } >= 2, "${e.title} (longa) tem menos de 2 perguntas")
            } else {
                assertTrue(e.passos.size in 5..10, "${e.title} tem ${e.passos.size} passos")
            }
            var seguidas = 0
            for (p in e.passos) {
                seguidas = if (p.ask != null || p.brinquedo != null) 0 else seguidas + 1
                assertTrue(seguidas <= 6, "${e.skill}: muitas cenas sem ação")
            }
        }
    }

    @Test fun perguntasApontamParaPecasQueExistem() {
        for (e in Episodes.all) for (p in e.passos) {
            val a = p.ask ?: continue
            val ids = p.shot.boxes.map { it.id }.toSet()
            assertTrue(a.right.isNotEmpty(), "${e.skill}: pergunta sem resposta")
            assertTrue(ids.containsAll(a.right), "${e.skill}: faltam ${a.right - ids}")
            assertTrue(ids.containsAll(a.wrong), "${e.skill}: faltam ${a.wrong - ids}")
            assertTrue(a.right.intersect(a.wrong).isEmpty(), "${e.skill}: resposta certa marcada como errada")
        }
    }

    @Test fun pecasCabemNoPalco() {
        for (e in Episodes.all) for (p in e.passos) for (b in p.shot.boxes) {
            if (b.look == Look.CODE) continue
            assertTrue(b.x - b.w / 2 >= -6f && b.x + b.w / 2 <= 366f, "${e.title}: '${b.text}' sai pela lateral (x=${b.x}, w=${b.w})")
            assertTrue(b.y + b.h / 2 <= 412f, "${e.title}: '${b.text}' sai por baixo (y=${b.y})")
        }
    }

    @Test fun nosDeArvoreNaoSeSobrepoem() {
        for (e in Episodes.all) for (p in e.passos) {
            val nos = p.shot.boxes.filter { it.look == Look.NODE }
            for (i in nos.indices) for (j in i + 1 until nos.size) {
                val a = nos[i]
                val b = nos[j]
                val juntos = kotlin.math.abs(a.x - b.x) < (a.w + b.w) / 2 - 2f && kotlin.math.abs(a.y - b.y) < (a.h + b.h) / 2 - 2f
                assertFalse(juntos, "${e.skill}: '${a.text}' em cima de '${b.text}'")
            }
        }
    }

    @Test fun tokensDoExemplo() {
        val t = MiniLexer.tokenize("conta = 10 + x1;").map { it.lexeme }
        assertEquals(listOf("conta", "=", "10", "+", "x1", ";"), t)
        assertEquals(listOf("x", ">=", "10"), MiniLexer.tokenize("x>=10").map { it.lexeme })
    }

    @Test fun automatoDoTrem() {
        val d = DFA.fromRegex("ab*")
        assertTrue(d.accepts("abb"))
        assertFalse(d.accepts("ba"))
    }

    @Test fun afnTerminaEmAb() {
        val n = SmallNFA("t", "", 3, 0, setOf(2), listOf(Triple(0, 'a', 0), Triple(0, 'b', 0), Triple(0, 'a', 1), Triple(1, 'b', 2)))
        assertEquals(setOf(0, 1), n.epsClosure(n.move(setOf(0), 'a')))
        assertEquals(setOf(0, 2), n.epsClosure(n.move(setOf(0, 1), 'b')))
    }

    @Test fun gemeosDaMinimizacao() {
        // A=0, B=1, C=2, D=3, como na tabela do episódio
        val d = DFA(4, 0, setOf(3), listOf('a', 'b'), mapOf(
            (0 to 'a') to 1, (0 to 'b') to 2, (1 to 'a') to 1, (1 to 'b') to 3,
            (2 to 'a') to 1, (2 to 'b') to 3, (3 to 'a') to 1, (3 to 'b') to 2,
        ))
        val r = Minimizer.run(d)
        assertEquals(3, r.minimized.stateCount)
        assertTrue(setOf(1, 2) in r.classes, "B e C são gêmeos: ${r.classes}")
    }

    @Test fun firstEFollow() {
        val f = FirstFollow(g)
        assertEquals(setOf("id", "("), f.first["T"])
        assertEquals(setOf("+", "ε"), f.first["X"])
        assertEquals(setOf("id", "("), f.first["E"])
        assertEquals(setOf("$", ")"), f.follow["E"])
        assertEquals(setOf("$", ")"), f.follow["X"])
        assertEquals(setOf("+", "$", ")"), f.follow["T"])
    }

    /** O segundo exemplo das aulas longas de FIRST e FOLLOW: o que a fala afirma é o que o motor calcula. */
    @Test fun segundosExemplosDasAulasLongas() {
        val f1 = FirstFollow(Grammar.parse(com.compilandia.content.episodes.GRAMATICA_FIRST_2.joinToString("\n")))
        assertEquals(setOf("a", "ε"), f1.first["A"])
        assertEquals(setOf("a", "b", "c"), f1.first["S"])
        val f2 = FirstFollow(Grammar.parse(com.compilandia.content.episodes.GRAMATICA_FOLLOW_2.joinToString("\n")))
        assertEquals(setOf("*"), f2.first["Y"]!! - "ε")
        assertEquals(setOf("+", ")", "$"), f2.follow["T"])
        assertEquals(setOf("*", "+", ")", "$"), f2.follow["F"])
    }

    /** Segunda leva das aulas longas: o que o segundo exemplo afirma, o motor confirma. */
    @Test fun segundosExemplosDaSegundaLeva() {
        val ll = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_LL1_2.joinToString("\n"))
        val t = LL1Table(ll)
        assertTrue(t.isLL1, "a lista de ids é LL(1)")
        assertEquals(setOf("$"), t.ff.follow["R"])
        assertEquals("id R", t.cell("L", "id").single().rhsText())
        assertEquals(", id R", t.cell("R", ",").single().rhsText())
        assertTrue(t.cell("R", "$").single().isEpsilon)

        val g = Grammar.parse(com.compilandia.content.episodes.GRAMATICA_LR_2.joinToString("\n"))
        val a = com.compilandia.engine.LRAutomaton.lr0(g)
        assertEquals(3, a.states[0].items.size, "estado zero: S' -> .S, S -> .( S ), S -> .id")
        val comId = a.transitions[0 to "id"]!!
        assertTrue(a.states[comId].items.any { it.dot == a.grammar.productions[it.prodId].rhs.size }, "lendo id, o ponto chega ao fim")
        val comPar = a.transitions[0 to "("]!!
        assertTrue(a.states[comPar].items.size > 1, "lendo abre parêntese, o closure puxa as regras de S")
        assertEquals(setOf(")", "$"), FirstFollow(g).follow["S"])

        val d = DFA(3, 0, setOf(2), listOf('0', '1'), mapOf(
            (0 to '0') to 1, (0 to '1') to 2, (1 to '0') to 1, (1 to '1') to 2, (2 to '0') to 2, (2 to '1') to 2,
        ))
        assertEquals(2, Minimizer.run(d).minimized.stateCount, "P e Q viram um só")

        val tac = TacGen.generate(ExprParser.parse("total * ( preco - 2 )"))
        assertTrue(tac.last().toString().contains("*"), "o vezes é a última conta: ${tac.last()}")
        val mips = Mips.fromTac(TacGen.generate(ExprParser.parse("preco + taxa")))
        assertEquals(2, mips.count { it.startsWith("lw") }, "$mips")
        assertEquals(1, mips.count { it.startsWith("sw") }, "$mips")
    }

    @Test fun tabelaLl1() {
        val t = LL1Table(g)
        assertEquals("X → ε", t.cell("X", ")").single().toString().replace("->", "→"))
        assertEquals(1, t.cell("X", "$").size)
        assertEquals(1, t.cell("E", "(").size)
        assertTrue(t.cell("X", "id").isEmpty())
    }

    @Test fun shiftReduceDoExemplo() {
        val gr = Grammar.parse("E -> E + n | n")
        val r = LRParser.parse(LRTable(gr, LRKind.SLR1), listOf("n", "+", "n"))
        assertTrue(r.accepted)
        val acoes = r.steps.map { a -> a.action.let { if (it is LRAction.Shift) "S" else if (it is LRAction.Reduce) "R" else "A" } }
        assertEquals(listOf("S", "R", "S", "S", "R", "A"), acoes)
    }

    @Test fun slrNaoReduzForaDoFollow() {
        val gr = Grammar.parse("E -> E + n | n")
        assertEquals(setOf("+", "$"), FirstFollow(gr).follow["E"])
        val t = LRTable(gr, LRKind.SLR1)
        val estado = (0 until 12).first { s -> t.action[s to "$"] is LRAction.Reduce && t.action[s to "+"] is LRAction.Reduce }
        assertEquals(null, t.action[estado to "n"])
    }

    @Test fun tacEMips() {
        val tac = TacGen.generate(ExprParser.parse("a + b * c"))
        assertEquals(listOf("t1 = b * c", "t2 = a + t1"), tac.map { it.toString() })
        val mips = Mips.fromTac(TacGen.generate(ExprParser.parse("b * c")))
        assertEquals(4, mips.size)
        assertTrue(mips[0].startsWith("lw") && mips[1].startsWith("lw") && mips[2].startsWith("mul") && mips[3].startsWith("sw"), mips.toString())
    }

    @Test fun dobraDeConstantes() {
        assertEquals("6", TacGen.constantFold(ExprParser.parse("2 * 3")).text)
        assertNotNull(Episodes.of(Skill.OPTIMIZATION))
    }
}
