package com.compilandia.engine

import com.compilandia.textos.tx

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
        fun display(): String = "$pattern   { ${if (token == null) tx("miniflex_ignora") else "return $token;"} }"
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
            if (best < 0) return Result(out, tx("miniflex_caractere_inesperado_1_na_posicao", input[i], i))
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
                    require(end > i) { tx("miniflex_aspas_sem_fechar_em_1", p) }
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
            "f1", tx("miniflex_palavra_chave_sequestrada"),
            tx("miniflex_scanner_esta_chamando_if_id"),
            listOf(WS, ID, MiniFlex.Rule("\"if\"", "IF"), MiniFlex.Rule("\"(\"", "LPAR"), MiniFlex.Rule("\")\"", "RPAR"), NUM),
            emptyList(),
            tx("miniflex_if_x1_iffy"),
            listOf("IF", "LPAR", "ID", "RPAR", "ID"),
            tx("miniflex_suba_regra_if_para_antes"),
        ),
        Challenge(
            "f2", tx("miniflex_maior_ou_igual"),
            tx("miniflex_b_deveria_virar_id_ge"),
            listOf(WS, ID, MiniFlex.Rule("\">\"", "GT"), MiniFlex.Rule("\"=\"", "ASSIGN")),
            listOf(MiniFlex.Rule("\">=\"", "GE"), MiniFlex.Rule("\"==\"", "EQ")),
            tx("miniflex_b_c_d_f"),
            listOf("ID", "GE", "ID", "ID", "ASSIGN", "ID", "ID", "GT", "ID"),
            tx("miniflex_adicione_regra_posicao_dela_nem"),
        ),
        Challenge(
            "f3", tx("miniflex_numeros_com_virgula_ponto"),
            tx("miniflex_3_14_esta_virando_num"),
            listOf(WS, NUM, MiniFlex.Rule("\"+\"", "MAIS")),
            listOf(MiniFlex.Rule("[0-9]+\".\"[0-9]+", "REAL"), MiniFlex.Rule("\".\"", "PONTO")),
            "3.14 + 42",
            listOf("REAL", "MAIS", "NUM"),
            tx("miniflex_adicione_0_9_0_9"),
        ),
        Challenge(
            "f4", tx("miniflex_comentarios_fantasmas"),
            tx("miniflex_comentarios_devem_sumir_sem_token"),
            listOf(WS, ID, MiniFlex.Rule("\"/\"", "DIV")),
            listOf(MiniFlex.Rule("\"//\".*", null), MiniFlex.Rule("\"/*\"", null)),
            tx("miniflex_b_divide_por_b_c"),
            listOf("ID", "DIV", "ID", "ID"),
            tx("miniflex_adicione_regra_sem_return_ignora"),
        ),
        Challenge(
            "f5", tx("miniflex_token_inutil"),
            tx("miniflex_regra_identificadores_aceita_digitos_no"),
            listOf(WS, MiniFlex.Rule("[a-z0-9]+", "ID"), NUM),
            listOf(ID),
            "9x x9 42",
            listOf("NUM", "ID", "ID", "NUM"),
            tx("miniflex_troque_regra_z0_9_pela"),
        ),
    )

    fun check(ch: Challenge, rules: List<MiniFlex.Rule>): Pair<Boolean, MiniFlex.Result> {
        val r = MiniFlex(rules).scan(ch.input)
        return (r.error == null && r.tokens.map { it.token } == ch.expected) to r
    }
}
