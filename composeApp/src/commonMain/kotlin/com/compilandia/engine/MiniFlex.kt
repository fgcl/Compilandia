package com.compilandia.engine

/**
 * Um "Flex de bolso": regras padrão → token, com as DUAS regras de ouro do lex/flex:
 *  1. maior casamento (o lexema mais longo vence);
 *  2. empate no tamanho → vence a regra que aparece PRIMEIRO no arquivo .l.
 *
 * Padrões aceitos: "texto literal", classes [a-z0-9_], ., *, +, ?, |, ( ), \n \t \. etc.
 */
class MiniFlex(val rules: List<Rule>) {
    data class Rule(val pattern: String, val token: String?) {   // token null = ignorar (ex.: espaços)
        val regex: Regex by lazy { Regex(toKotlinRegex(pattern)) }
        fun display(): String = "$pattern   { ${if (token == null) "/* ignora */" else "return $token;"} }"
    }

    data class Lexeme(val token: String, val text: String, val rule: Int)
    data class Result(val tokens: List<Lexeme>, val error: String?)

    fun scan(input: String): Result {
        val out = mutableListOf<Lexeme>()
        var i = 0
        while (i < input.length) {
            var best = -1; var bestLen = 0
            rules.forEachIndexed { k, r ->
                val m = r.regex.matchAt(input, i)
                val len = m?.value?.length ?: 0
                if (len > bestLen) { best = k; bestLen = len }
            }
            if (best < 0) return Result(out, "caractere inesperado '${input[i]}' na posição $i (nenhuma regra casa)")
            val text = input.substring(i, i + bestLen)
            rules[best].token?.let { out += Lexeme(it, text, best) }
            i += bestLen
        }
        return Result(out, null)
    }

    fun toSpec(): String = "%%\n" + rules.joinToString("\n") { it.display() } + "\n%%"

    companion object {
        /** Converte padrão estilo flex para regex Kotlin: "..." vira literal. */
        fun toKotlinRegex(p: String): String {
            val sb = StringBuilder()
            var i = 0
            while (i < p.length) {
                val c = p[i]
                if (c == '"') {
                    val end = p.indexOf('"', i + 1)
                    require(end > i) { "aspas sem fechar em $p" }
                    sb.append(Regex.escape(p.substring(i + 1, end)))
                    i = end + 1
                } else { sb.append(c); i++ }
            }
            return sb.toString()
        }

        /** Lê a seção de regras de um arquivo .l (entre %% e %%). */
        fun parseSpec(spec: String): MiniFlex {
            val parts = spec.split(Regex("(?m)^\\s*%%\\s*$"))
            val body = if (parts.size >= 2) parts[1] else spec
            val rules = mutableListOf<Rule>()
            for (raw in body.lines()) {
                val line = raw.trimEnd()
                if (line.isBlank() || line.trimStart().startsWith("/*") || line.trimStart().startsWith("//")) continue
                // o padrão termina no primeiro espaço fora de aspas/colchetes
                var i = 0; var inQ = false; var inB = false
                while (i < line.length) {
                    val c = line[i]
                    if (c == '\\' && i + 1 < line.length) { i += 2; continue }
                    if (c == '"' && !inB) inQ = !inQ
                    else if (c == '[' && !inQ) inB = true
                    else if (c == ']' && !inQ) inB = false
                    else if (c.isWhitespace() && !inQ && !inB) break
                    i++
                }
                val pat = line.substring(0, i)
                val action = line.substring(i).trim()
                val tok = Regex("return\\s+([A-Za-z_][A-Za-z0-9_]*)").find(action)?.groupValues?.get(1)
                rules += Rule(pat, tok)
            }
            return MiniFlex(rules)
        }
    }
}

/** Desafios da Oficina Flex: arrumar a ORDEM e o CONJUNTO de regras até a saída bater. */
object FlexChallenges {
    data class Challenge(
        val id: String,
        val title: String,
        val story: String,
        val rules: List<MiniFlex.Rule>,       // regras iniciais (ordem errada / faltando)
        val extra: List<MiniFlex.Rule>,       // regras que podem ser adicionadas
        val input: String,
        val expected: List<String>,           // tokens esperados
        val hint: String,
    )

    private val WS = MiniFlex.Rule("[ \\t\\n]+", null)
    private val ID = MiniFlex.Rule("[a-z][a-z0-9]*", "ID")
    private val NUM = MiniFlex.Rule("[0-9]+", "NUM")

    val all = listOf(
        Challenge(
            "f1", "Palavra-chave sequestrada",
            "O scanner está chamando 'if' de ID! Com o mesmo tamanho, quem vence é a regra que vem PRIMEIRO.",
            listOf(WS, ID, MiniFlex.Rule("\"if\"", "IF"), MiniFlex.Rule("\"(\"", "LPAR"), MiniFlex.Rule("\")\"", "RPAR"), NUM),
            emptyList(),
            "if (x1) iffy",
            listOf("IF", "LPAR", "ID", "RPAR", "ID"),
            "Suba a regra \"if\" para ANTES da regra de identificadores. Repare que 'iffy' continua ID: o maior casamento vence a ordem!",
        ),
        Challenge(
            "f2", "Maior ou igual?",
            "a>=b deveria virar ID GE ID. Falta uma regra… e o flex pega o MAIOR lexema.",
            listOf(WS, ID, MiniFlex.Rule("\">\"", "GT"), MiniFlex.Rule("\"=\"", "ASSIGN")),
            listOf(MiniFlex.Rule("\">=\"", "GE"), MiniFlex.Rule("\"==\"", "EQ")),
            "a>=b c=d e>f",
            listOf("ID", "GE", "ID", "ID", "ASSIGN", "ID", "ID", "GT", "ID"),
            "Adicione a regra \">=\". A posição dela nem importa: \">=\" casa 2 caracteres e \">\" só 1.",
        ),
        Challenge(
            "f3", "Números com vírgula (ponto)",
            "3.14 está virando NUM, erro, NUM. Precisamos de uma regra para reais.",
            listOf(WS, NUM, MiniFlex.Rule("\"+\"", "MAIS")),
            listOf(MiniFlex.Rule("[0-9]+\".\"[0-9]+", "REAL"), MiniFlex.Rule("\".\"", "PONTO")),
            "3.14 + 42",
            listOf("REAL", "MAIS", "NUM"),
            "Adicione [0-9]+\".\"[0-9]+ → REAL (entre aspas o ponto é literal). Por que ela vence NUM em '3.14'? Maior casamento!",
        ),
        Challenge(
            "f4", "Comentários fantasmas",
            "Comentários // devem SUMIR (sem token). Hoje eles viram lixo.",
            listOf(WS, ID, MiniFlex.Rule("\"/\"", "DIV")),
            listOf(MiniFlex.Rule("\"//\".*", null), MiniFlex.Rule("\"/*\"", null)),
            "a / b // divide a por b\nc",
            listOf("ID", "DIV", "ID", "ID"),
            "Adicione a regra \"//\".* sem return (ignora). '.' casa tudo menos quebra de linha.",
        ),
        Challenge(
            "f5", "O token inútil",
            "A regra de identificadores aceita dígitos no começo! '9x' está virando um ID só.",
            listOf(WS, MiniFlex.Rule("[a-z0-9]+", "ID"), NUM),
            listOf(ID),
            "9x x9 42",
            listOf("NUM", "ID", "ID", "NUM"),
            "Troque a regra [a-z0-9]+ pela correta [a-z][a-z0-9]* (remova a velha e adicione a nova). Teste: '9x' vira NUM e ID.",
        ),
    )

    fun check(ch: Challenge, rules: List<MiniFlex.Rule>): Pair<Boolean, MiniFlex.Result> {
        val r = MiniFlex(rules).scan(ch.input)
        return (r.error == null && r.tokens.map { it.token } == ch.expected) to r
    }
}
