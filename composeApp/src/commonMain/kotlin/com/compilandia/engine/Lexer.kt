package com.compilandia.engine

import kotlin.random.Random

enum class TokenClass(val label: String, val emoji: String) {
    KEYWORD("palavra-chave", "🔑"),
    ID("identificador", "🏷️"),
    NUM("número", "🔢"),
    OP("operador", "➕"),
    DELIM("delimitador", "🧱"),
    STRING("string", "💬"),
}

data class Token(val cls: TokenClass, val lexeme: String, val start: Int) {
    val end get() = start + lexeme.length
}

/**
 * Analisador léxico de uma mini-linguagem estilo C, com a regra do
 * "maior casamento" (maximal munch) e prioridade de palavras-chave.
 */
object MiniLexer {
    val keywords = setOf("if", "else", "while", "for", "int", "float", "return", "void", "char")
    private val ops = listOf("==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "+", "-", "*", "/", "%", "=", "<", ">", "!")
    private val delims = setOf('(', ')', '{', '}', ';', ',', '[', ']')

    class LexError(val pos: Int, msg: String) : Exception(msg)

    fun tokenize(src: String): List<Token> {
        val out = mutableListOf<Token>()
        var i = 0
        while (i < src.length) {
            val c = src[i]
            when {
                c.isWhitespace() -> i++
                src.startsWith("//", i) -> { while (i < src.length && src[i] != '\n') i++ }
                c.isLetter() || c == '_' -> {
                    val s = i
                    while (i < src.length && (src[i].isLetterOrDigit() || src[i] == '_')) i++
                    val lex = src.substring(s, i)
                    out += Token(if (lex in keywords) TokenClass.KEYWORD else TokenClass.ID, lex, s)
                }
                c.isDigit() -> {
                    val s = i
                    while (i < src.length && src[i].isDigit()) i++
                    if (i + 1 < src.length && src[i] == '.' && src[i + 1].isDigit()) {
                        i++; while (i < src.length && src[i].isDigit()) i++
                    }
                    out += Token(TokenClass.NUM, src.substring(s, i), s)
                }
                c == '"' -> {
                    val s = i; i++
                    while (i < src.length && src[i] != '"') i++
                    if (i >= src.length) throw LexError(s, "string sem fechar aspas")
                    i++
                    out += Token(TokenClass.STRING, src.substring(s, i), s)
                }
                c in delims -> { out += Token(TokenClass.DELIM, c.toString(), i); i++ }
                else -> {
                    val op = ops.firstOrNull { src.startsWith(it, i) } ?: throw LexError(i, "caractere inválido '$c'")
                    out += Token(TokenClass.OP, op, i); i += op.length
                }
            }
        }
        return out
    }

    /** Linhas de código geradas para o jogo "Fatiador de Tokens" — com armadilhas didáticas. */
    fun randomLine(rnd: Random, level: Int): String {
        val ids = listOf("x", "y", "total", "i", "count2", "iff", "whilex", "_tmp", "media", "n1")
        val nums = listOf("0", "1", "42", "3.14", "10", "2.5", "100")
        fun id() = ids[rnd.nextInt(ids.size)]
        fun num() = nums[rnd.nextInt(nums.size)]
        fun cmp() = listOf("<", "<=", ">=", "==", "!=", ">")[rnd.nextInt(6)]
        fun ar() = listOf("+", "-", "*", "/")[rnd.nextInt(4)]
        val templates: List<() -> String> = listOf(
            { "${id()}=${id()}${ar()}${num()};" },
            { "if(${id()}${cmp()}${num()}){${id()}++;}" },
            { "int ${id()},${id()};" },
            { "while(${id()}${cmp()}${id()})${id()}+=${num()};" },
            { "float ${id()}=${num()}${ar()}${id()};" },
            { "return ${id()}${ar()}${id()}${ar()}${num()};" },
            { "if(${id()}>=${num()}&&${id()}!=${id()})x=1;" },
            { "for(i=0;i<${num()};i++)${id()}=${id()};" },
        )
        val pool = when {
            level <= 1 -> templates.take(3)
            level == 2 -> templates.take(6)
            else -> templates
        }
        return pool[rnd.nextInt(pool.size)]()
    }
}
