package com.compilandia

import com.compilandia.content.Beat
import com.compilandia.content.TAC_OU
import com.compilandia.content.TP_BYTEIRO_BNF
import com.compilandia.content.TP_REC_COMPILA
import com.compilandia.content.TP_REC_ESPERADO
import com.compilandia.content.TP_REC_PROGRAMA
import com.compilandia.content.TP_REC_REDE
import com.compilandia.content.episodes.MIPS_E
import com.compilandia.content.episodes.MIPS_E_VALORES
import com.compilandia.content.episodes.MIPS_IF
import com.compilandia.content.episodes.MIPS_WHILE
import com.compilandia.content.tpCap1
import com.compilandia.content.tpCap5
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.MiniLang
import com.compilandia.engine.MipsVM
import com.compilandia.textos.tx
import java.io.File
import java.net.URLClassLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A segunda leva dos pedidos dos professores 5 e 6: desvios num esquema só, curto-circuito, Estenda a Byteiro e modo pânico. */
class PedidosProfessores2Test {
    private fun roda(src: String, valores: Map<String, Long>) = MipsVM(MiniLang.asmText(MiniLang.compile(src, valores))).run()

    @Test fun umEsquemaDeDesvioSo() {
        val todos = MiniLang.samples + listOf(MIPS_IF, MIPS_WHILE, MIPS_E, TAC_OU)
        for (src in todos) for (l in MiniLang.compile(src, MiniLang.sampleEnv).map { it.text }) {
            assertTrue(l.split(" ").first() !in setOf("bge", "blt", "bgt", "ble"), "$src: $l")
            if (l.startsWith("beq") || l.startsWith("bne")) assertTrue("\$zero" in l, l)
        }
        assertEquals(listOf("t1 = a < b", "ifFalse t1 goto fimse0"), MiniLang.tac(MIPS_IF).take(2))
        val se = MiniLang.corpo(MIPS_IF)
        assertEquals(listOf("slt", "beq"), se.subList(2, 4).map { it.substringBefore(" ") })
        for ((a, x) in listOf(1L to 1L, 3L to 0L)) assertEquals(x, roda(MIPS_IF, mapOf("a" to a, "b" to 2L)).word("x"))
        val enq = MiniLang.corpo(MIPS_WHILE)
        assertEquals("enquanto0:", enq.first()); assertEquals("j enquanto0", enq[enq.lastIndex - 1]); assertEquals("fimenq0:", enq.last())
        assertEquals(3L, roda(MIPS_WHILE, mapOf("i" to 0L, "n" to 3L)).word("i"))
    }

    /** if (a < b && c < d) e com ||: o resultado bate com o do Kotlin em todas as combinações. */
    @Test fun eEOuComCurtoCircuito() {
        val vs = listOf(1L, 2L)
        for (a in vs) for (b in vs) for (c in vs) for (d in vs) {
            val m = mapOf("a" to a, "b" to b, "c" to c, "d" to d)
            assertEquals(if (a < b && c < d) 1L else 0L, roda(MIPS_E, m).word("x"), "e $m")
            assertEquals(if (a < b || c < d) 1L else 0L, roda(TAC_OU, m).word("x"), "ou $m")
        }
        assertEquals(listOf("t1 = a < b", "ifFalse t1 goto fimse0", "t2 = c < d", "ifFalse t2 goto fimse0", "x = 1", "fimse0:"), MiniLang.tac(MIPS_E))
        assertEquals(listOf("t1 = a < b", "if t1 goto entra1", "t2 = c < d", "ifFalse t2 goto fimse0", "entra1:", "x = 1", "fimse0:"), MiniLang.tac(TAC_OU))
    }

    /** A aula diz: com a = 9 e b = 2, nenhuma das quatro linhas de c < d (c4 a c7) roda. */
    @Test fun nenhumaLinhaDeCMenorQueDRoda() {
        val e = MiniLang.corpo(MIPS_E)
        assertEquals("lw \$t0, c", e[4]); assertTrue(e[7].startsWith("beq"))
        val base = MiniLang.compile(MIPS_E).indexOfFirst { it.text == "main:" } + 1
        fun linhas(v: Map<String, Long>) = MipsVM(MiniLang.asmText(MiniLang.compile(MIPS_E, v))).run().trace.map { it.lastLine - base }.toSet()
        assertTrue((4..7).none { it in linhas(MIPS_E_VALORES) })
        assertTrue((4..7).all { it in linhas(MIPS_E_VALORES + ("a" to 1L)) }, "com a < b, as quatro rodam")
    }

    /** Cap. 4 do Estenda: a Byteiro com o repita continua LL(1). */
    @Test fun repitaContinuaLl1() {
        val g = Grammar.parse(TP_BYTEIRO_BNF.replace("    | leia ( id ) ;", "    | leia ( id ) ;\n    | repita Bloco ate ( Expr ) ;"))
        val t = LL1Table(g)
        assertTrue("repita" in g.terminals && t.isLL1, t.conflicts.toString())
        val ff = FirstFollow(g)
        assertTrue("ate" in ff.follow.getValue("Bloco"))
        for (n in ff.nullable) assertTrue("ate" !in ff.follow.getValue(n), n)
    }

    /** Cap. 8 do Estenda: o molde de um rótulo só. O corpo roda uma vez com a condição já verdadeira. */
    @Test fun moldeDoRepita() {
        fun prog(limite: Int) = $$"""
            .data
            x_0: .word 0
            .text
            main:
                li $t0, 5
                sw $t0, x_0
            repita0:
                lw $t0, x_0
                move $a0, $t0
                li $v0, 1
                syscall
                lw $t0, x_0
                li $t1, 1
                add $t0, $t0, $t1
                sw $t0, x_0
                lw $t0, x_0
                li $t1, $$limite
                sgt $t0, $t0, $t1
                beq $t0, $zero, repita0
                li $v0, 10
                syscall
        """.trimIndent()
        assertEquals("5", MipsVM(prog(0)).run().output.toString())
        assertEquals("567", MipsVM(prog(7)).run().output.toString())
        assertTrue("beq \$t0, \$zero, repitaN" in tx("tp_estenda_8_r"))
    }

    /** Cap. 10: o Sintatico do cap. 5 com a rede do cap. 10, compilado com o javac de verdade. */
    @Test fun modoPanicoCompilaERodaNoJava() {
        val compilador = assertNotNull(javax.tools.ToolProvider.getSystemJavaCompiler(), "o teste precisa de um JDK")
        val dir = File("build/modo-panico").apply { deleteRecursively(); mkdirs() }
        val codigos = tpCap5.beats.filterIsInstance<Beat.Code>().map { it.code }
        val comandos = codigos.first { "private void programa()" in it }
            .replace(Regex("private void bloco\\(\\) \\{.*?\\n}\\n", RegexOption.DOT_MATCHES_ALL), "")
            .replace("private void comando() {", "private void comandoSemRede() {")
            // O repita do Estenda a Byteiro (cap. 5), com o corpo tirado da própria resposta
            .replace("TipoToken.ABRE_CHAVE);", "TipoToken.ABRE_CHAVE, TipoToken.REPITA);")
            .replace("case ABRE_CHAVE -> bloco();", "case ABRE_CHAVE -> bloco();\n        case REPITA -> repita();")
        val repita = "private void repita() { " + Regex("`(casaToken\\(TipoToken.REPITA\\);.*?)`").find(tx("tp_estenda_5_r"))!!.groupValues[1] + " }"
        val tryCatch = TP_REC_COMPILA.lines().subList(1, 6).joinToString("\n")
        val infra = tx("tpguideb_public_class_sintatico_private_final").replace("        programa();", tryCatch)
        val rede = TP_REC_REDE.lines().filterNot { "tabela." in it || "proxTemp" in it }.joinToString("\n")
        val tipos = tx("tpguidea_public_enum_tipotoken_palavras_reservadas").replace("FALSO(\"falso\"),", "FALSO(\"falso\"), REPITA(\"repita\"), ATE(\"ate\"),")
        val arquivos = mapOf(
            "TipoToken" to tipos.substringBefore("public class Token"),
            "Token" to "public class Token" + tipos.substringAfter("public class Token"),
            "ErroCompilacao" to tpCap1.beats.filterIsInstance<Beat.Code>().first { "class ErroCompilacao" in it.code }.code,
            "Lexico" to listOf("tpguidea_import_java_util_hashmap_import", "tpguidea_public_token_proximo_int_estado", "tpguidea_private_token_simples_char_c").joinToString("\n") { tx(it) }.replace("TipoToken.FALSO.ordinal()", "TipoToken.ATE.ordinal()"),
            "Sintatico" to "import java.util.*;\n" + listOf(infra, comandos, repita, codigos.first { "private void declaracao()" in it }, rede, tx("tpguideb_private_void_expr_expre_ou")).joinToString("\n"),
        )
        val fontes = arquivos.map { (n, c) -> File(dir, "$n.java").apply { writeText(c) }.path }
        assertEquals(0, compilador.run(null, null, null, *(listOf("-encoding", "UTF-8", "-d", dir.path) + fontes).toTypedArray()), "javac falhou")
        val cl = URLClassLoader(arrayOf(dir.toURI().toURL()))
        fun erros(src: String): List<String> {
            val lexico = cl.loadClass("Lexico").getConstructor(String::class.java).newInstance(src)
            val s = cl.loadClass("Sintatico").getConstructor(cl.loadClass("Lexico")).newInstance(lexico)
            s.javaClass.getMethod("compila").invoke(s)
            @Suppress("UNCHECKED_CAST") return s.javaClass.getMethod("erros").invoke(s) as List<String>
        }
        assertEquals(TP_REC_ESPERADO.lines(), erros(TP_REC_PROGRAMA))
        assertEquals(2, erros("programa p {\n  inteiro x;\n  x <- (x + ) x;\n}").size, "a cascata do Preveja")
        assertEquals(1, erros("programa p {\n  repita {\n    x <- ;\n  } ate (x > 3);\n}").size, "o Estenda do cap. 10")
        assertEquals(1, erros("programa p {\n  enquanto (x < 3) {\n    x <- x + 1;\n").size, "sem o } no fim: uma mensagem só")
        assertEquals(emptyList(), erros(com.compilandia.content.TP_PROGRAMA_CONTAGEM))
    }
}
