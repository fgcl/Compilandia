package com.compilandia.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EngineV2Test {
    @Test fun subsetConstructionDragonBook() {
        val n = SmallNFA.bank.first { it.id == "abb" }
        val r = n.subsetConstruction()
        assertEquals(4, r.states.size)
        val d = r.toDFA(n.alphabet)
        for (s in listOf("abb", "aabb", "babb", "ab", "", "abba", "bbb")) assertEquals(n.accepts(s), d.accepts(s), s)
        for (nfa in SmallNFA.bank) {
            val dd = nfa.subsetConstruction().toDFA(nfa.alphabet)
            for (s in listOf("", "a", "b", "ab", "abb", "aab", "abc", "ac", "c", "bba", "aa")) {
                if (s.all { it in nfa.alphabet }) assertEquals(nfa.accepts(s), dd.accepts(s), "${nfa.id}: $s")
            }
        }
        val eps = SmallNFA.bank.first { it.id == "abc" }
        assertEquals(setOf(0, 1, 3), eps.epsClosure(setOf(0)))
    }

    @Test fun miniFlex() {
        for (ch in FlexChallenges.all) assertFalse(FlexChallenges.check(ch, ch.rules).first, ch.id)
        val f1 = FlexChallenges.all[0]
        val fixed = f1.rules.toMutableList().also { val ifr = it.removeAt(2); it.add(1, ifr) }
        assertTrue(FlexChallenges.check(f1, fixed).first, FlexChallenges.check(f1, fixed).second.toString())
        val f2 = FlexChallenges.all[1]
        assertTrue(FlexChallenges.check(f2, f2.rules + f2.extra[0]).first)
        val f3 = FlexChallenges.all[2]
        assertTrue(FlexChallenges.check(f3, f3.rules + f3.extra[0]).first, FlexChallenges.check(f3, f3.rules + f3.extra[0]).second.toString())
        val f4 = FlexChallenges.all[3]
        assertTrue(FlexChallenges.check(f4, f4.rules + f4.extra[0]).first, FlexChallenges.check(f4, f4.rules + f4.extra[0]).second.toString())
        val f5 = FlexChallenges.all[4]
        assertTrue(FlexChallenges.check(f5, listOf(f5.rules[0], f5.extra[0], f5.rules[2])).first)
        val spec = MiniFlex.parseSpec("%%\n\"if\"   { return IF; }\n[a-z]+ { return ID; }\n[ \\t]+ { }\n%%")
        assertEquals(listOf("IF", "ID"), spec.scan("if xyz").tokens.map { it.token })
    }

    @Test fun panicMode() {
        val g = Grammar.parse("P -> S ; P | ε\nS -> id = E\nE -> id | num")
        val t = LL1Table(g)
        assertTrue(t.isLL1)
        val bad = tokensOf("id = = num ; id = num ;")
        assertEquals(2, PanicMode.firstErrorPosition(t, bad))
        val tr = PanicMode.trace(t, bad)
        assertTrue(tr.reachedEnd)
        assertTrue(tr.events.isNotEmpty())
        assertNull(PanicMode.firstErrorPosition(t, tokensOf("id = num ;")))
    }

    @Test fun runtimeFrames() {
        val p = RuntimeSim.fatorial(3)
        assertEquals(6, p.result)
        val maxDepth = p.events.maxOf { it.stack.size }
        assertEquals(4, maxDepth) // main + fat(3) + fat(2) + fat(1)
        assertEquals(RuntimeSim.fib(3).result, 2)
        assertEquals(RuntimeSim.chain(1).result, 5) // g(1)=h(2)=3, h(1)=2
    }

    @Test fun mipsFromTac() {
        val e = ExprParser.parse("a + b * c - 1")
        val tac = TacGen.generate(e, "r")
        val asm = Mips.fromTac(tac)
        val st = Mips.run(asm, Mips.intEnv).last()
        assertEquals(9L, st.mem["r"])
    }

    @Test fun recursiveDescentCode() {
        val t = LL1Table(GrammarBank.exprLL1.grammar)
        val code = RecursiveDescentGen.function(t, "E'")
        assertTrue(code.any { it.text.contains("casaToken(\"+\")") })
        assertTrue(code.first().text.startsWith("void E_()"))
        val calls = RecursiveDescentGen.callTrace(t, tokensOf("id + id"))
        assertTrue(calls.last().second.contains("aceito"))
        assertNotNull(calls.firstOrNull { it.first.size >= 3 })
    }

    @Test fun bisonAcceptsRealSpecs() {
        val spec = """
            %{
            #include <stdio.h>
            int yylex(void);
            %}
            %union { int ival; }
            %token <ival> NUM
            %type <ival> expr
            %left '+'
            %%
            expr : expr '+' expr { $$ = $1 + $3; }
                 | NUM
                 ;
            %%
            int main(void) { return yyparse(); }
        """.trimIndent()
        val b = MiniBison.compile(spec)
        assertEquals(5L, b.run("2+3").value)
    }
}
