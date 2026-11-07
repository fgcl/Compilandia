package com.compilandia

import com.compilandia.content.episodes.AFN_1
import com.compilandia.content.episodes.AFN_2
import com.compilandia.content.episodes.FLEX_2
import com.compilandia.content.episodes.FLEX_2_CERTO
import com.compilandia.content.episodes.FLEX_2_ENTRADA
import com.compilandia.content.episodes.REGEX_2
import com.compilandia.content.episodes.REGEX_NOMES
import com.compilandia.content.episodes.TOKENS_LINHA_2
import com.compilandia.content.episodes.TOKENS_LINHA_PROBLEMA
import com.compilandia.content.episodes.TOKENS_PEDACO_2
import com.compilandia.engine.DFA
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.TokenClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A9: o que as aulas longas do léxico afirmam, o motor confirma. */
class A9LexicoFactsTest {

    @Test fun tokens() {
        // "Seis palavras no lugar de treze caracteres."
        assertEquals(6, MiniLexer.tokenize(TOKENS_LINHA_PROBLEMA).size)
        assertEquals(13, TOKENS_LINHA_PROBLEMA.length)
        // "Passo três: if é palavra reservada. Já ifx é um nome comum."
        assertEquals(listOf(TokenClass.KEYWORD, TokenClass.ID), MiniLexer.tokenize("if ifx").map { it.cls })
        // "conta2 virou um token só"
        assertEquals(listOf("conta2"), MiniLexer.tokenize("conta2").map { it.lexeme })
        // Segundo exemplo: while é palavra-chave, i++; corta em três, a linha dá nove tokens.
        val t = MiniLexer.tokenize(TOKENS_LINHA_2)
        assertEquals(TokenClass.KEYWORD, t[0].cls)
        assertEquals(listOf("i", "++", ";"), MiniLexer.tokenize(TOKENS_PEDACO_2).map { it.lexeme })
        assertTrue(TOKENS_LINHA_2.endsWith(TOKENS_PEDACO_2))
        assertEquals(9, t.size, "${t.map { it.lexeme }}")
        assertEquals(17, TOKENS_LINHA_2.length)
    }

    @Test fun expressoesRegulares() {
        // Só 1x não é nome: o scanner corta em número e nome.
        for (n in REGEX_NOMES) {
            val t = MiniLexer.tokenize(n)
            if (n == "1x") assertEquals(listOf(TokenClass.NUM, TokenClass.ID), t.map { it.cls })
            else assertEquals(listOf(TokenClass.ID), t.map { it.cls }, n)
        }
        // Primeiro exemplo: no segundo estado de a b*, b é um laço; a b a trava no último a.
        val d1 = DFA.fromRegex("ab*")
        assertEquals(1, d1.step(1, 'b'))
        assertEquals(-1, d1.step(d1.step(d1.step(0, 'a'), 'b'), 'a'))
        assertFalse(d1.accepts("aba"))
        // Segundo exemplo: três estados em fila, só o último é final.
        val d = DFA.fromRegex(REGEX_2)
        assertEquals(3, d.stateCount)
        assertEquals(setOf(2), d.accepting)
        assertEquals(mapOf((0 to 'a') to 1, (1 to 'b') to 1, (1 to 'c') to 2), d.delta)
        assertTrue(d.accepts("ac"), "zero b também vale")
        assertFalse(d.accepts("abcb"), "depois do c não sai seta com b")
        assertEquals(-1, d.step(2, 'b'))
    }

    @Test fun subconjuntos() {
        // Primeiro exemplo: {0}, {0,1}, {0,2}; de {0,2}, lendo a, volta para {0,1}.
        val r1 = AFN_1.subsetConstruction()
        assertEquals(listOf(setOf(0), setOf(0, 1), setOf(0, 2)), r1.states)
        assertEquals(setOf(0, 1), AFN_1.epsClosure(AFN_1.move(setOf(0, 2), 'a')))
        assertEquals(setOf(0), AFN_1.epsClosure(AFN_1.move(setOf(0, 2), 'b')))
        assertEquals(setOf(2), r1.accepting)
        // Segundo exemplo: fecho do zero é {0,1,3}; com a vai a {2}, com c vai a {4}; os dois são finais.
        val r2 = AFN_2.subsetConstruction()
        assertEquals(setOf(0, 1, 3), r2.states[0])
        assertEquals(setOf(2), AFN_2.epsClosure(AFN_2.move(r2.states[0], 'a')))
        assertEquals(setOf(4), AFN_2.epsClosure(AFN_2.move(r2.states[0], 'c')))
        assertTrue(AFN_2.move(r2.states[0], 'b').isEmpty())
        assertEquals(listOf(setOf(0, 1, 3), setOf(2), setOf(4)), r2.states, "três estados no AFD")
        assertEquals(setOf(1, 2), r2.accepting)
        assertEquals(setOf(2), AFN_2.epsClosure(AFN_2.move(setOf(2), 'b')), "o laço com b fica no conjunto do dois")
    }

    @Test fun flex() {
        // Com [a-z]+ no topo, while sai como ID; o == casa a regra de dois caracteres, mesmo mais abaixo.
        val errado = MiniFlex.parseSpec(FLEX_2.joinToString("\n")).scan(FLEX_2_ENTRADA)
        assertEquals(null, errado.error)
        assertEquals(listOf("ID", "ID", "IGUAL", "NUM"), errado.tokens.map { it.token })
        assertEquals(0, errado.tokens[0].rule)
        assertEquals(3, errado.tokens.first { it.text == "==" }.rule)
        assertTrue(FLEX_2[2].contains("\"=\"") && FLEX_2[3].contains("\"==\""))
        // Com a regra do while no topo: WHILE, ID, IGUAL e NUM.
        val certo = MiniFlex.parseSpec(FLEX_2_CERTO.joinToString("\n")).scan(FLEX_2_ENTRADA)
        assertEquals(listOf("WHILE", "ID", "IGUAL", "NUM"), certo.tokens.map { it.token })
        // Primeiro exemplo: o espaço casa uma regra sem return e some.
        val primeiro = MiniFlex.parseSpec("\"if\"      return IF;\n[a-z]+    return ID;\n[0-9]+    return NUM;\n[ \\t\\n]  ; /* ignora */")
        assertEquals(listOf("IF", "ID"), primeiro.scan("if ifa").tokens.map { it.token })
        assertEquals(null, primeiro.rules[3].token)
    }
}
