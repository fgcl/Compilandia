package com.compilandia

import com.compilandia.content.Temporada
import com.compilandia.content.episodes.EMC1_SEGUNDA
import com.compilandia.content.episodes.Episodes
import com.compilandia.content.episodes.FUNCOES
import com.compilandia.content.episodes.HERDADO_CODIGO
import com.compilandia.content.episodes.HERDADO_DECL
import com.compilandia.content.episodes.HERDADO_LUGARES
import com.compilandia.content.episodes.VETOR
import com.compilandia.content.episodes.VETOR_BASE
import com.compilandia.content.episodes.VETOR_I
import com.compilandia.content.episodes.argumentosDasChamadas
import com.compilandia.content.episodes.emC1Opcoes
import com.compilandia.content.episodes.linhaDoAddtipo
import com.compilandia.engine.AcessoVetor
import com.compilandia.engine.HerdadoNaDescida
import com.compilandia.engine.HerdadoNaDescida.Lugar
import com.compilandia.engine.LexerC
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.TabelaComFuncoes
import com.compilandia.engine.TabelaComFuncoes.Categoria
import com.compilandia.engine.TiposComandos.Veredito
import com.compilandia.learning.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** As lacunas que sobravam do professor do bloco 5 e o erro comum da Em C 1: o que as falas dizem, o motor confere. */
class PraticaLacunas2Test {
    /** O b entra pelo R, com o float que desceu como parâmetro; a ação só funciona entre o T e o L. */
    @Test fun herdadoNaDescida() {
        val r = HerdadoNaDescida.roda(HERDADO_DECL)
        assertEquals(mapOf("a" to "float", "b" to "float"), r.tabela)
        assertEquals("R", r.rastro.single { it.id == "b" }.funcao)
        assertEquals("L", r.rastro.single { it.id == "a" }.funcao)
        val comHerdado = r.rastro.filter { it.funcao != "T" }
        assertTrue(comHerdado.all { it.herdado == "float" }, "o mesmo float passa de função em função")
        assertTrue("addtipo" in HERDADO_CODIGO[linhaDoAddtipo("R")] && linhaDoAddtipo("R") > linhaDoAddtipo("L"))
        for ((lugar, _) in HERDADO_LUGARES) {
            val tabela = HerdadoNaDescida.roda(HERDADO_DECL, lugar).tabela
            assertEquals(lugar == Lugar.ENTRE_T_E_L, tabela.values.all { it != null }, "$lugar")
            if (lugar != Lugar.ENTRE_T_E_L) assertEquals(setOf("a", "b"), tabela.keys, "os ids entram, mas sem tipo")
        }
        assertTrue(HERDADO_LUGARES.last().first == Lugar.ENTRE_T_E_L, "a certa não fica no meio")
        assertEquals(3, HerdadoNaDescida.roda("int x, y, z;").rastro.count { it.funcao == "R" && it.id == null })
    }

    /** Os parâmetros moram na tabela da soma; do main, a busca não acha o a; soma(x) é a chamada recusada. */
    @Test fun funcoesNaTabela() {
        val t = TabelaComFuncoes.monta(FUNCOES)
        assertEquals("soma", TabelaComFuncoes.busca(t, "soma", "a")?.first)
        assertEquals(Categoria.PARAMETRO, TabelaComFuncoes.busca(t, "soma", "b")?.second?.categoria)
        assertNull(TabelaComFuncoes.busca(t, "main", "a"))
        assertEquals(TabelaComFuncoes.GLOBAL, TabelaComFuncoes.busca(t, "main", "soma")?.first)
        val vereditos = argumentosDasChamadas().map { TabelaComFuncoes.confereChamada(t, "main", "soma", it) }
        assertEquals(listOf(Veredito.OK, Veredito.ERRO, Veredito.OK), vereditos)
        assertEquals(1, argumentosDasChamadas()[1].size, "a recusada manda um argumento só")
        val semFuncao = TabelaComFuncoes.confereChamada(t, "main", "dobro", listOf("x"))
        assertEquals(Veredito.ERRO, semFuncao, "função que não existe")
    }

    /** v[2] mora no 1008, o sll de 2 bits faz o vezes 4, e o MIPS rodado deixa o x com 30. */
    @Test fun acessoAVetor() {
        assertEquals(1008L, AcessoVetor.endereco(VETOR_BASE, VETOR_I))
        assertEquals(listOf("t1 = i * 4", "t2 = v[t1]", "x = t2"), AcessoVetor.tac("x", "v", "i"))
        assertEquals(30L, AcessoVetor.roda("x", "v", "i", VETOR, VETOR_I))
        for (i in VETOR.indices) assertEquals(VETOR[i], AcessoVetor.roda("x", "v", "i", VETOR, i.toLong()))
        assertTrue(AcessoVetor.mips("x", "v", "i").none { it.startsWith("mul") }, "a fala diz que não há mul")
    }

    /** Sem o ungetc, o vezes some; com ele, sai igual ao lexer do jogo. A Em C 1 segue com até 10 passos. */
    @Test fun emC1SemUngetc() {
        assertEquals(listOf("NUM 7", "NUM 13"), LexerC.tokens(EMC1_SEGUNDA, comUngetc = false))
        assertEquals(listOf("NUM 7", "*", "NUM 13"), LexerC.tokens(EMC1_SEGUNDA))
        val doJogo = MiniLexer.tokenize(EMC1_SEGUNDA).map { it.lexeme }
        assertEquals(doJogo, LexerC.tokens(EMC1_SEGUNDA).map { it.removePrefix("NUM ") })
        assertEquals(listOf("NUM 42", "+", "NUM 7"), LexerC.tokens("42+7"))
        assertEquals(3, emC1Opcoes().distinct().size)
        val ep = Episodes.all.single { it.temporada == Temporada.EM_C && it.skill == Skill.TOKENS }
        assertTrue(ep.passos.size <= 10, "a aula da série tem até 10 cenas: ${ep.passos.size}")
    }

    @Test fun aulasDePraticaNovas() {
        val p = Episodes.all.filter { it.temporada == Temporada.PRATICA }.map { it.skill }
        for (s in listOf(Skill.SCOPE_SYMBOLS, Skill.CODEGEN_MIPS)) assertTrue(s in p, "$s sem prática")
        assertTrue(p.count { it == Skill.ATTRIBUTES_TYPES } >= 2)
    }
}
