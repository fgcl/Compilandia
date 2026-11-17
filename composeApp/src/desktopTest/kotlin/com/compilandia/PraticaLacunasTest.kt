package com.compilandia

import com.compilandia.content.episodes.DAG_BLOCO
import com.compilandia.content.episodes.EMC4_SEGUNDA
import com.compilandia.content.episodes.Episodes
import com.compilandia.content.episodes.TIPOS_ATRIB
import com.compilandia.content.episodes.TIPOS_COND
import com.compilandia.content.episodes.TIPOS_ENV
import com.compilandia.content.episodes.TIPOS_FUNC
import com.compilandia.content.episodes.VIVAS_LACO
import com.compilandia.content.episodes.linhaDoFimDoCorpo
import com.compilandia.content.episodes.primeiraQueLe
import com.compilandia.engine.DagDoBloco
import com.compilandia.engine.ExprParser
import com.compilandia.engine.MiniLang
import com.compilandia.engine.Mips
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.TacGen
import com.compilandia.engine.TiposComandos
import com.compilandia.engine.TiposComandos.Veredito
import com.compilandia.engine.VivacidadeComLaco
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** As lacunas do professor do bloco 5 e o segundo exemplo da Em C 4: o que as falas dizem, o motor confere. */
class PraticaLacunasTest {
    private fun v(g: List<Pair<String, TiposComandos.Comando>>) = g.map { TiposComandos.verifica(it.second, TIPOS_ENV) }

    /** Um erro por grupo, e as falas de acerto citam a coerção do int para float. */
    @Test fun tiposNosComandos() {
        assertEquals(listOf(Veredito.ERRO, Veredito.COERCAO, Veredito.OK), v(TIPOS_ATRIB))
        assertEquals(listOf(Veredito.OK, Veredito.OK, Veredito.ERRO), v(TIPOS_COND))
        assertEquals(listOf(Veredito.COERCAO, Veredito.ERRO, Veredito.OK), v(TIPOS_FUNC))
        assertEquals(Veredito.ERRO, TiposComandos.verifica(TiposComandos.Chamada(listOf(com.compilandia.engine.Type.INT), listOf("1", "2")), TIPOS_ENV), "quantidade errada de argumentos")
        assertEquals(Veredito.ERRO, TiposComandos.verifica(TiposComandos.Retorno(com.compilandia.engine.Type.INT, "x"), TIPOS_ENV))
    }

    /** O bloco do Dragão: três contas, a linha 4 reaproveita a 2, e a 3 não reaproveita a 1 porque o b mudou. */
    @Test fun dagDoBloco() {
        val d = DagDoBloco.monta(DAG_BLOCO)
        assertEquals(3, d.internos.size)
        assertEquals(mapOf(3 to 1), d.reusos)
        assertEquals(listOf("b", "d"), d.nos[d.noDaLinha[3]].nomes)
        assertEquals(mapOf(1 to 0), DagDoBloco.monta(listOf("t1 = a * b", "t2 = a * b")).reusos, "a subexpressão comum sem mudança no meio")
    }

    /** O laço: depois de i = t3 vivem i, n e s; ler sem o goto dá só s; duas passadas mudam algo; o teste é quem lê o i. */
    @Test fun vivasNoLaco() {
        val code = MiniLang.tac(VIVAS_LACO)
        val r = VivacidadeComLaco.analisa(code, emptySet())
        val k = linhaDoFimDoCorpo(code)
        assertEquals("i = t3", code[k])
        assertEquals(setOf("i", "n", "s"), r.liveOut[k])
        assertEquals(setOf("s"), VivacidadeComLaco.semVoltar(code, emptySet())[k])
        assertEquals(2, r.passadasQueMudam, "a fala diz duas passadas que mudam e a terceira que confere")
        assertTrue(code[primeiraQueLe(code, k, "i")].startsWith("t1 = i < n"))
        val topo = code.indexOfFirst { it.endsWith(":") }
        assertEquals(setOf("i", "n", "s"), r.liveIn[topo])
        // Sem desvio, a análise com laço dá o mesmo que a do RegAlloc, que só olha os temporários.
        val reta = TacGen.generate(ExprParser.parse("( a + b ) * ( a - c )"), "x")
        val lv = RegAlloc.liveness(reta)
        val nova = VivacidadeComLaco.analisa(reta.map { it.toString() }, setOf("x"))
        reta.indices.forEach { i -> assertEquals(lv.liveIn[i], nova.liveIn[i].filter { RegAlloc.isTemp(it) }.toSet()) }
    }

    /** Em C 4: o MIPS trocado lê o tmp1 antes de guardar, e o segundo exemplo tem o vezes como filho da esquerda. */
    @Test fun emC4() {
        val certo = Mips.nomeiaTemporarios(Mips.fromTac(TacGen.generate(ExprParser.parse("a + b * c"))))
        assertTrue(Mips.leAntesDeGuardar(certo).isEmpty())
        val trocado = certo.chunked(4).reversed().flatten()
        assertEquals(listOf(1), Mips.leAntesDeGuardar(trocado))
        assertEquals("sw \$t3, tmp1", trocado.last())
        val e = ExprParser.parse(EMC4_SEGUNDA)
        assertEquals("*", e.children[0].text)
        val ep = Episodes.all.single { it.temporada == com.compilandia.content.Temporada.EM_C && it.skill == Skill.CODEGEN_MIPS }
        assertTrue(ep.passos.size <= 10, "a aula da série tem até 10 cenas: ${ep.passos.size}")
    }

    @Test fun aulasDePraticaNovas() {
        val p = Episodes.all.filter { it.temporada == com.compilandia.content.Temporada.PRATICA }.map { it.skill }
        for (s in listOf(Skill.ATTRIBUTES_TYPES, Skill.OPTIMIZATION, Skill.REG_ALLOC)) assertTrue(s in p, "$s sem prática")
    }
}
