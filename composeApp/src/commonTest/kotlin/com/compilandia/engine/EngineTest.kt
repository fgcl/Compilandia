package com.compilandia.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineTest {
    private val exprLL = GrammarBank.exprLL1.grammar
    private val expr = GrammarBank.exprLeftRec.grammar

    @Test fun parsesGrammar() {
        assertEquals(listOf("E", "E'", "T", "T'", "F"), exprLL.nonTerminals)
        assertEquals(setOf("+", "*", "(", ")", "id"), exprLL.terminals.toSet())
        assertTrue(exprLL.productions.any { it.lhs == "E'" && it.isEpsilon })
    }

    @Test fun firstFollowDragonBook() {
        val ff = FirstFollow(exprLL)
        assertEquals(setOf("(", "id"), ff.first["E"])
        assertEquals(setOf("+", EPS), ff.first["E'"])
        assertEquals(setOf("*", EPS), ff.first["T'"])
        assertEquals(setOf(")", EOF), ff.follow["E"])
        assertEquals(setOf(")", EOF), ff.follow["E'"])
        assertEquals(setOf("+", ")", EOF), ff.follow["T"])
        assertEquals(setOf("+", "*", ")", EOF), ff.follow["F"])
        assertTrue(ff.steps.isNotEmpty())
    }

    @Test fun ll1TableAndParse() {
        val t = LL1Table(exprLL)
        assertTrue(t.isLL1)
        assertEquals("E' → + T E'", t.cell("E'", "+").single().toString())
        assertEquals("T' → ε", t.cell("T'", ")").single().toString())
        assertTrue(LL1Parser.accepts(t, tokensOf("id + id * id")))
        assertFalse(LL1Parser.accepts(t, tokensOf("id + * id")))
        assertFalse(LL1Table(expr).isLL1)
    }

    @Test fun leftRecursionRemovalGivesLL1() {
        val g = GrammarTransform.eliminateImmediateLeftRecursion(expr)
        assertTrue(GrammarTransform.leftRecursiveNTs(g).isEmpty())
        val t = LL1Table(g)
        assertTrue(t.isLL1, g.toText())
        assertTrue(LL1Parser.accepts(t, tokensOf("( id + id ) * id")))
    }

    @Test fun leftFactoring() {
        val g = Grammar.parse("S -> if E then S | if E then S else S | a\nE -> b")
        assertTrue(GrammarTransform.needsLeftFactoring(g))
        val f = GrammarTransform.leftFactor(g)
        assertFalse(GrammarTransform.needsLeftFactoring(f), f.toText())
    }

    @Test fun lr0StatesForExprGrammar() {
        val a = LRAutomaton.lr0(expr)
        assertEquals(12, a.states.size) // I0..I11 do Livro do Dragão
    }

    @Test fun slrParsesExpr() {
        val t = LRTable(expr, LRKind.SLR1)
        assertFalse(t.hasConflicts)
        val r = LRParser.parse(t, tokensOf("id * ( id + id )"))
        assertTrue(r.accepted)
        assertEquals(listOf("id", "*", "(", "id", "+", "id", ")"), r.tree!!.leaves())
        assertFalse(LRParser.parse(t, tokensOf("id + + id")).accepted)
    }

    @Test fun lalrBeatsSlrOnPointerGrammar() {
        val g = GrammarBank.lalrNotSlr.grammar
        assertTrue(LRTable(g, LRKind.SLR1).hasConflicts)
        assertFalse(LRTable(g, LRKind.LALR1).hasConflicts)
        assertFalse(LRTable(g, LRKind.LR1).hasConflicts)
        assertTrue(LRParser.parse(LRTable(g, LRKind.LALR1), tokensOf("* id = id")).accepted)
    }

    @Test fun ambiguityDetected() {
        val g = GrammarBank.exprAmbiguous.grammar
        val ds = DerivationHelper.leftmostDerivations(g, tokensOf("id + id * id"), limit = 3)
        assertEquals(2, ds.size)
        val t1 = DerivationHelper.treeOf(g, ds[0]); val t2 = DerivationHelper.treeOf(g, ds[1])
        assertTrue(t1 != t2)
        assertEquals(1, DerivationHelper.leftmostDerivations(exprLL, tokensOf("id + id * id")).size)
        assertTrue(LRTable(g, LRKind.SLR1).hasConflicts)
    }

    @Test fun earleyViability() {
        val e = Earley(expr)
        assertTrue(e.accepts(tokensOf("id + id")))
        assertFalse(e.accepts(tokensOf("id +")))
        assertTrue(e.derives(listOf("T", "+", "T"), tokensOf("id * id + id")))
        val eps = Grammar.parse("S -> A B\nA -> a | ε\nB -> b | ε")
        assertTrue(Earley(eps).accepts(emptyList()))
        assertTrue(Earley(eps).accepts(listOf("b")))
    }

    @Test fun sentencesAreInLanguage() {
        val rnd = Random(7)
        val t = LRTable(expr, LRKind.SLR1)
        repeat(30) {
            val s = SentenceGenerator.generate(expr, rnd)
            assertNotNull(s)
            assertTrue(LRParser.parse(t, s).accepted, s.joinToString(" "))
        }
    }

    @Test fun randomGrammarsAreSane() {
        val rnd = Random(1)
        repeat(20) {
            val g = GrammarGenerator.random(rnd)
            val ff = FirstFollow(g)
            assertTrue(g.nonTerminals.all { ff.first.getValue(it).isNotEmpty() }, g.toText())
            val ll = GrammarGenerator.random(rnd, requireLL1 = true)
            assertTrue(LL1Table(ll).isLL1)
        }
    }

    @Test fun regexToDfa() {
        val d = DFA.fromRegex("(a|b)*abb")
        assertEquals(4, d.stateCount) // AFD mínimo clássico
        assertTrue(d.accepts("abb")); assertTrue(d.accepts("aababb"))
        assertFalse(d.accepts("ab")); assertFalse(d.accepts("abba"))
        val bin = DFA.fromRegex("0|1(0|1)*")
        assertTrue(bin.accepts("0")); assertTrue(bin.accepts("101")); assertFalse(bin.accepts("01"))
        for (e in RegexBank.entries) {
            val dfa = DFA.fromRegex(e.regex)
            val samples = dfa.samples(Random(3), 6)
            assertTrue(samples.isNotEmpty(), e.regex)
        }
    }

    @Test fun lexerMaximalMunch() {
        val t = MiniLexer.tokenize("if(iff>=10){count2++;}")
        assertEquals(listOf("if", "(", "iff", ">=", "10", ")", "{", "count2", "++", ";", "}"), t.map { it.lexeme })
        assertEquals(TokenClass.KEYWORD, t[0].cls)
        assertEquals(TokenClass.ID, t[2].cls)
        val rnd = Random(5)
        repeat(50) { MiniLexer.tokenize(MiniLexer.randomLine(rnd, 3)) }
    }

    @Test fun typeCheckingAndTac() {
        val e = ExprParser.parse("a + x * 2")
        val attrs = TypeChecker.evaluate(e, ExprGenerator.env)
        assertEquals(Type.FLOAT, attrs.getValue(e.id).type)
        assertEquals(7.0, attrs.getValue(e.id).value)
        val bad = ExprParser.parse("ok + 1")
        assertEquals(Type.ERROR, TypeChecker.evaluate(bad, ExprGenerator.env).getValue(bad.id).type)
        val tac = TacGen.generate(ExprParser.parse("a + b * c"), "r")
        assertEquals(listOf("t1 = b * c", "t2 = a + t1", "r = t2"), tac.map { it.toString() })
        val folded = TacGen.constantFold(ExprParser.parse("a + 2 * 3"))
        assertEquals("(a + 6)", folded.render())
        val rnd = Random(9)
        repeat(30) { ExprParser.parse(ExprGenerator.random(rnd, 3, allowBool = true)) }
    }

    @Test fun scopes() {
        val rnd = Random(2)
        repeat(20) {
            val p = ScopeGen.random(rnd)
            assertTrue(p.uses.isNotEmpty())
            for ((_, l) in p.uses) l.resolvesTo?.let { assertNotNull(p.lines[it].declId) }
        }
    }

    @Test fun bisonPrecedence() {
        val noPrec = MiniBison.compile(BisonChallenges.all[0].starterSpec)
        assertTrue(noPrec.conflicts.isNotEmpty())
        val fixed = """
            %token NUM
            %left '+' '-'
            %left '*' '/'
            %right '^'
            %right UMINUS
            %%
            expr : expr '+' expr { $$ = $1 + $3; }
                 | expr '-' expr { $$ = $1 - $3; }
                 | expr '*' expr { $$ = $1 * $3; }
                 | expr '/' expr { $$ = $1 / $3; }
                 | expr '^' expr { $$ = $1 ^ $3; }
                 | '-' expr %prec UMINUS { $$ = -$2; }
                 | '(' expr ')'  { $$ = $2; }
                 | NUM
                 ;
        """.trimIndent()
        val b = MiniBison.compile(fixed)
        assertTrue(b.conflicts.isEmpty(), b.conflicts.toString())
        assertEquals(14L, b.run("2+3*4").value)
        assertEquals(3L, b.run("8-3-2").value)
        assertEquals(512L, b.run("2^3^2").value)
        assertEquals(-3L, b.run("-5+2").value)
        assertEquals(5L, b.run("4--1").value)
        assertFalse(b.run("2+").accepted)
    }

    @Test fun bisonChallengesSolvable() {
        val solutions = mapOf(
            "b1" to "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n",
            "b2" to "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n",
            "b3" to "%token NUM\n%left '+' '-'\n%left '*' '/'\n%right '^'\n%%\nexpr : expr '^' expr { $$ = $1 ^ $3; } | ",
            "b4" to "%token NUM\n%left '+' '-'\n%left '*' '/'\n%right UMINUS\n%%\nexpr : '-' expr %prec UMINUS { $$ = -$2; } | ",
        )
        val rules = """expr '+' expr { $$ = $1 + $3; }
     | expr '-' expr { $$ = $1 - $3; }
     | expr '*' expr { $$ = $1 * $3; }
     | expr '/' expr { $$ = $1 / $3; }
     | '(' expr ')'  { $$ = $2; }
     | NUM
     ;"""
        for (ch in BisonChallenges.all.filter { it.id in solutions }) {
            val head = solutions.getValue(ch.id)
            val spec = if (head.endsWith("| ")) head + rules else head + "expr : " + rules
            val v = BisonChallenges.check(ch, spec)
            assertTrue(v.passed, "${ch.id}: ${v.lines}")
            assertFalse(BisonChallenges.check(ch, ch.starterSpec).passed, "${ch.id} starter should fail")
        }
        val layered = """
            %token NUM
            %%
            expr : expr '+' term { $$ = $1 + $3; } | term ;
            term : term '*' fator { $$ = $1 * $3; } | fator ;
            fator : NUM ;
        """.trimIndent()
        assertTrue(BisonChallenges.check(BisonChallenges.all.first { it.id == "b5" }, layered).passed)
        val b6 = BisonChallenges.all.first { it.id == "b6" }
        val sol6 = b6.starterSpec.replace("%left '<'", "%nonassoc '<'")
        assertTrue(BisonChallenges.check(b6, sol6).passed, BisonChallenges.check(b6, sol6).lines.toString())
        assertFalse(BisonChallenges.check(b6, b6.starterSpec).passed)
    }
}
