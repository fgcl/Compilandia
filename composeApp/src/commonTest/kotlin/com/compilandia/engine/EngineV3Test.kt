package com.compilandia.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineV3Test {
    @Test fun minimizationTableMethod() {
        val (name, d) = Minimizer.bank.first()
        val r = Minimizer.run(d)
        assertEquals(4, r.minimized.stateCount, name)            // A e C se fundem (Dragão)
        assertTrue(r.classes.any { it == setOf(0, 2) })
        for (s in listOf("", "abb", "aabb", "ab", "babb", "abba")) assertEquals(d.accepts(s), r.minimized.accepts(s), s)
        for ((n, dd) in Minimizer.bank) {
            val rr = Minimizer.run(dd)
            assertTrue(rr.minimized.stateCount < dd.stateCount || rr.deadState != null, n)
            assertEquals(DFA.fromRegex("(a|b)*abb").stateCount, Minimizer.run(Minimizer.bank[0].second).minimized.stateCount)
            for (s in listOf("", "a", "b", "aa", "ab", "ba", "bb", "aab", "abb", "bab", "aaaa")) if (s.all { it in dd.alphabet }) assertEquals(dd.accepts(s), rr.minimized.accepts(s), "$n: $s")
        }
        assertTrue(Minimizer.bank.size >= 4)
    }

    @Test fun astVsParseTree() {
        val e = ExprParser.parse("( a + b ) * 3")
        assertEquals("new BinOp('*', new BinOp('+', new Var(\"a\"), new Var(\"b\")), new Num(3))", AstTools.toJava(e))
        val t = AstTools.concreteTree("( a + b ) * 3")
        assertNotNull(t)
        assertTrue(AstTools.countNodes(t) > 2 * AstTools.countNodes(e))
        assertEquals(listOf("(", "a", "+", "b", ")", "*", "3"), t.leaves())
    }

    @Test fun registerAllocation() {
        val code = TacGen.generate(ExprParser.parse("a * b + c * d"), "r")
        val g = RegAlloc.interference(code)
        assertTrue("t2" in g.getValue("t1"))       // t1 vivo enquanto t2 é calculado
        assertEquals(2, RegAlloc.chromatic(g))
        val alloc = RegAlloc.color(g, 2)
        assertTrue(RegAlloc.isValidColoring(g, alloc.colors))
        val asm = RegAlloc.mips(code, alloc)
        val vm = MipsVM(".data\na: .word 4\nb: .word 2\nc: .word 3\nd: .word 5\nr: .word 0\n.text\nmain:\n" + asm.joinToString("\n")).run()
        assertEquals(23L, vm.word("r"))
        assertTrue(RegAlloc.memoryAccesses(asm) < RegAlloc.memoryAccesses(Mips.fromTac(code)))
        val spill = RegAlloc.color(g, 1)
        assertTrue(spill.spilled.isNotEmpty())
        val vm2 = MipsVM(".data\na: .word 4\nb: .word 2\nc: .word 3\nd: .word 5\nr: .word 0\nt1: .word 0\nt2: .word 0\nt3: .word 0\n.text\nmain:\n" + RegAlloc.mips(code, spill).joinToString("\n")).run()
        assertEquals(23L, vm2.word("r"))
    }

    @Test fun mipsVmBranchesCallsStack() {
        val fat = $$"""
            .data
            msg: .asciiz "fat(5) = "
            .text
            main:
                la $a0, msg
                li $v0, 4
                syscall
                li $a0, 5
                jal fat
                move $a0, $v0
                li $v0, 1
                syscall
                li $v0, 10
                syscall
            fat:
                addi $sp, $sp, -8
                sw $ra, 4($sp)
                sw $a0, 0($sp)
                li $t0, 1
                ble $a0, $t0, base
                addi $a0, $a0, -1
                jal fat
                lw $a0, 0($sp)
                mul $v0, $a0, $v0
                j fim
            base:
                li $v0, 1
            fim:
                lw $ra, 4($sp)
                addi $sp, $sp, 8
                jr $ra
        """.trimIndent()
        val vm = MipsVM(fat).run()
        assertEquals("fat(5) = 120", vm.output.toString())
        assertEquals(MipsVM.STACK_TOP, vm.regs["\$sp"])
    }

    @Test fun miniLangToMips() {
        val expected = listOf(1L, 7L, 6L, 24L, 0L)
        MiniLang.samples.forEachIndexed { i, src ->
            val asm = MiniLang.asmText(MiniLang.compile(src, MiniLang.sampleEnv))
            val vm = MipsVM(asm).run()
            val v = when (i) { 0 -> vm.word("x"); 1 -> vm.word("m"); 2 -> vm.word("s"); 3 -> vm.word("f"); else -> vm.word("x") }
            assertEquals(expected[i], v, asm)
        }
        assertTrue(MiniLang.compile(MiniLang.samples[0], MiniLang.sampleEnv).any { it.blankable })
        assertFalse(runCatching { MipsVM(".text\nmain:\n    foo \$t0").run() }.isSuccess)
    }
}
