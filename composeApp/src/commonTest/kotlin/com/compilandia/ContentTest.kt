package com.compilandia

import com.compilandia.content.Beat
import com.compilandia.content.Library
import com.compilandia.content.VisualKind
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Grammar
import com.compilandia.engine.MiniBison
import com.compilandia.engine.tokensOf
import com.compilandia.engine.Earley
import com.compilandia.engine.LL1Table
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.Mips
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import kotlin.test.Test
import kotlin.test.assertTrue

/** Garante que todo conteúdo escrito é consistente com o motor (gramáticas válidas, entradas aceitas...). */
class ContentTest {
    @Test fun everyVisualIsValid() {
        val all = Library.lessons.map { it.skill.name + "/" + it.style.name to it.beats } + Library.guide.map { "guia ${it.number}" to it.beats }
        for ((where, beats) in all) for (b in beats) {
            if (b !is Beat.Visual) continue
            val ctx = "$where: ${b.kind} '${b.grammar}' '${b.input}'"
            when (b.kind) {
                VisualKind.DFA -> DFA.fromRegex(b.input)
                VisualKind.ATTR_TREE, VisualKind.TAC -> ExprParser.parse(b.input)
                VisualKind.BISON -> { val bz = MiniBison.compile(b.grammar); if (b.input.isNotBlank()) assertTrue(bz.run(b.input).accepted, ctx) }
                VisualKind.PIPELINE, VisualKind.SCOPE, VisualKind.TOKENIZE -> {}
                VisualKind.NFA_SUBSET -> assertTrue(SmallNFA.bank.any { it.id == b.input }, ctx)
                VisualKind.FLEX -> { val f = MiniFlex.parseSpec(b.grammar); assertTrue(f.rules.isNotEmpty(), ctx); if (b.input.isNotBlank()) assertTrue(f.scan(b.input).error == null, ctx) }
                VisualKind.CALL_STACK -> assertTrue(b.input in setOf("fat3", "fat4", "fib3", "chain1", "chain2"), ctx)
                VisualKind.MIPS -> { val e = ExprParser.parse(b.input); Mips.run(Mips.fromTac(TacGen.generate(e, "r")), Mips.intEnv) }
                VisualKind.MINIMIZE -> assertTrue(b.input.toIntOrNull() in com.compilandia.engine.Minimizer.bank.indices, ctx)
                VisualKind.AST -> assertTrue(com.compilandia.engine.AstTools.concreteTree(b.input) != null, ctx)
                VisualKind.REGALLOC -> { val e = ExprParser.parse(b.input); TacGen.generate(e, "r") }
                VisualKind.MIPS_RUN -> com.compilandia.engine.MipsVM(b.grammar).run()
                VisualKind.MINILANG -> com.compilandia.engine.MipsVM(com.compilandia.engine.MiniLang.asmText(com.compilandia.engine.MiniLang.compile(b.input, com.compilandia.engine.MiniLang.sampleEnv))).run()
                VisualKind.PANIC -> { val g = Grammar.parse(b.grammar); assertTrue(LL1Table(g).isLL1, ctx) }
                VisualKind.DESCENT_CODE -> { val g = Grammar.parse(b.grammar); assertTrue(LL1Table(g).isLL1, ctx); if (b.input.isNotBlank()) assertTrue(Earley(g).accepts(tokensOf(b.input)), ctx) }
                else -> {
                    val g = Grammar.parse(b.grammar)
                    if (b.input.isNotBlank()) assertTrue(Earley(g).accepts(tokensOf(b.input)), "entrada fora da linguagem: $ctx")
                }
            }
        }
    }

    @Test fun predictionsWellFormed() {
        for (l in Library.lessons) for (b in l.beats) if (b is Beat.Predict) {
            assertTrue(b.correct in b.options.indices, "${l.skill}/${l.style}: ${b.question}")
        }
        for (c in Library.guide) for (b in c.beats) if (b is Beat.Predict) assertTrue(b.correct in b.options.indices, "guia ${c.number}")
    }

    @Test fun coverage() {
        if (Library.lessons.isEmpty()) return
        for (s in Skill.entries) {
            assertTrue(Library.card(s) != null, "sem ficha: $s")
            for (st in Style.entries) assertTrue(Library.lesson(s, st) != null, "falta aula $s/$st")
        }
    }
}
