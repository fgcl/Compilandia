package com.compilandia.engine

/*
 * O laço do getchar da Em C 1, com e sem o ungetc (erro comum pedido pelo revisor professor do bloco 6).
 * O while só para depois de ler um caractere que não é dígito. Esse caractere já saiu da entrada:
 * o ungetc devolve ele. Sem o ungetc, ele some, e o próximo token começa no caractere seguinte.
 */
object LexerC {
    /** O número é escrito na base dez: cada dígito novo empurra os outros uma casa. */
    private const val BASE = 10

    /** Os tokens da [entrada], um por chamada do lexer: "NUM 42" para número e o próprio caractere para o resto. */
    fun tokens(entrada: String, comUngetc: Boolean = true): List<String> {
        val saida = mutableListOf<String>()
        var pos = 0
        while (pos < entrada.length) {
            val c = entrada[pos++]
            if (!c.isDigit()) { saida += c.toString(); continue }
            var v = c.digitToInt()
            while (pos < entrada.length && entrada[pos].isDigit()) v = v * BASE + entrada[pos++].digitToInt()
            // O getchar que parou o while leu um caractere a mais. Sem o ungetc, ele é perdido.
            if (!comUngetc) pos++
            saida += "NUM $v"
        }
        return saida
    }
}
