package com.compilandia.engine

/**
 * Um "Bison de bolso": lê uma especificação no formato Yacc/Bison,
 * monta a tabela LALR(1) com precedência/associatividade e executa as
 * ações semânticas ($$ = $1 + $3;) sobre uma entrada.
 *
 * Suportado:
 *   %token NUM ID          — tokens nomeados (NUM = inteiro; ID = letra/palavra, valor via tabela)
 *   %left / %right / %nonassoc  — precedência (linhas posteriores ligam mais forte)
 *   %start s
 *   regras  a : b 'c' d { $$ = expr; } | ... ;
 *   %prec TOKEN no fim de uma alternativa
 * Ações: `$$ = <expressão>;` com $n, números, + - * / % ^, parênteses e menos unário.
 * Ação vazia equivale a `$$ = $1;` (como no Bison).
 */
class MiniBison private constructor(
    val grammar: Grammar,
    val actions: Map<Int, String?>,           // prodId (na gramática original) -> texto da ação
    val tokenPrec: Map<String, Precedence>,
    val declaredTokens: Set<String>,
    val table: LRTable,
) {
    class SpecError(msg: String) : Exception(msg)

    data class RunResult(
        val accepted: Boolean,
        val value: Long?,
        val steps: List<LRStep>,
        val tree: ParseNode?,
        val error: String?,
        val reductions: List<String>,          // "expr → expr '+' expr   ⇒  $$ = 2 + 12 = 14"
    )

    /**
     * Um quadro do passo a passo: as três pilhas lado a lado (estados, símbolos e valores, como no Bison)
     * e o que vai acontecer agora. [conta] traz a ação semântica já calculada quando o passo reduz.
     */
    data class Quadro(
        val estados: List<Int>,
        val simbolos: List<String>,
        val valores: List<Long?>,
        val entrada: List<String>,
        val acao: LRAction?,
        val texto: String,
        val conta: String?,
    )

    /** Roda a entrada e devolve um quadro por passo. O último quadro diz se aceitou ou onde parou. */
    fun quadros(input: String): List<Quadro> {
        val toks = try { lex(input) } catch (e: Exception) { return emptyList() }
        val steps = LRParser.parse(table, toks.map { it.first }).steps
        val valores = mutableListOf<Long?>()
        val out = mutableListOf<Quadro>()
        var k = 0
        for (st in steps) {
            when (val a = st.action) {
                is LRAction.Shift -> {
                    out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, "empilha ${st.input.first()} e vai para o estado ${a.state}", null)
                    valores += if (toks[k].first == "NUM") toks[k].second else null
                    k++
                }
                is LRAction.Reduce -> {
                    val p = table.grammar.productions[a.prodId]
                    val n = p.rhs.size
                    val args = valores.subList(valores.size - n, valores.size).toList()
                    val act = actions[a.prodId - 1]
                    val v = try {
                        if (act.isNullOrBlank()) args.firstOrNull() else ActionEval.eval(act, args.map { it ?: 0L })
                    } catch (e: Exception) { null }
                    // "$$ = $1 + $3" vira "$$ = 2 + 12 = 14": a conta com os valores que estão na pilha.
                    val conta = if (act.isNullOrBlank()) "\$\$ = \$1 = ${v ?: "?"}" else {
                        val lado = act.trim().removeSuffix(";").substringAfter("=").trim()
                        val trocado = Regex("\\$(\\d+)").replace(lado) { m -> args.getOrNull(m.groupValues[1].toInt() - 1)?.toString() ?: "?" }
                        "\$\$ = $trocado = ${v ?: "?"}"
                    }
                    out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, "reduz $p", conta)
                    repeat(n) { valores.removeAt(valores.lastIndex) }
                    valores += v
                }
                LRAction.Accept -> out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, "aceita: o valor final é ${valores.lastOrNull() ?: "?"}", null)
                null -> out += Quadro(st.states, st.symbols, valores.toList(), st.input, null, "erro de sintaxe: nada a fazer com ${st.input.firstOrNull() ?: "o fim"} no estado ${st.states.last()}", null)
            }
        }
        return out
    }

    val conflicts: List<LRConflict> get() = table.conflicts
    val resolvedByPrecedence: Int get() = table.rawConflictCells.size - table.conflicts.size

    companion object {
        fun compile(spec: String): MiniBison {
            val cleaned = spec
                .replace(Regex("%\\{[\\s\\S]*?%\\}"), "")
                .replace(Regex("%union\\s*\\{[\\s\\S]*?\\}"), "")
            val parts = cleaned.split(Regex("(?m)^\\s*%%\\s*$"))
            if (parts.size < 2) throw SpecError("Faltou o separador %% entre as declarações e as regras")
            val decls = parts[0]
            val rules = parts[1]

            val tokens = mutableSetOf<String>()
            val prec = mutableMapOf<String, Precedence>()
            var level = 0
            var start: String? = null
            for (raw in decls.lines()) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) continue
                val words = splitSymbols(line)
                when (words.firstOrNull()) {
                    "%token" -> tokens += words.drop(1).filter { !it.startsWith("<") }
                    "%left", "%right", "%nonassoc" -> {
                        level++
                        val assoc = when (words[0]) { "%left" -> Assoc.LEFT; "%right" -> Assoc.RIGHT; else -> Assoc.NONASSOC }
                        for (w in words.drop(1)) { prec[w] = Precedence(level, assoc); if (!w.startsWith("'")) tokens += w }
                    }
                    "%start" -> start = words.getOrNull(1)
                    "%type", "%define", "%expect", "%locations", "%error-verbose", "%debug" -> {}
                    else -> throw SpecError("Declaração desconhecida: '$line'")
                }
            }

            // regras
            val prods = mutableListOf<Production>()
            val actions = mutableMapOf<Int, String?>()
            val precOverride = mutableMapOf<Int, String>()
            val body = stripComments(rules)
            var i = 0
            fun skipWs() { while (i < body.length && body[i].isWhitespace()) i++ }
            while (true) {
                skipWs()
                if (i >= body.length) break
                val ls = i
                while (i < body.length && (body[i].isLetterOrDigit() || body[i] == '_')) i++
                val lhs = body.substring(ls, i)
                if (lhs.isEmpty()) throw SpecError("Esperava o nome de uma regra perto de '${body.substring(i).take(15)}'")
                skipWs()
                if (i >= body.length || body[i] != ':') throw SpecError("Faltou ':' depois de '$lhs'")
                i++
                if (start == null) start = lhs
                // alternativas
                while (true) {
                    val syms = mutableListOf<String>()
                    var action: String? = null
                    var precTok: String? = null
                    while (true) {
                        skipWs()
                        if (i >= body.length) throw SpecError("Regra '$lhs' sem ';' no final")
                        val c = body[i]
                        when {
                            c == '|' || c == ';' -> break
                            c == '{' -> {
                                var depth = 0; val s = i
                                while (i < body.length) {
                                    if (body[i] == '{') depth++
                                    if (body[i] == '}') { depth--; if (depth == 0) break }
                                    i++
                                }
                                if (i >= body.length) throw SpecError("Ação sem '}'")
                                action = body.substring(s + 1, i).trim(); i++
                            }
                            c == '\'' -> {
                                if (i + 2 >= body.length || body[i + 2] != '\'') throw SpecError("Literal mal formado perto de ${body.substring(i).take(5)}")
                                syms += body.substring(i, i + 3); i += 3
                            }
                            body.startsWith("%prec", i) -> {
                                i += 5; skipWs(); val s = i
                                while (i < body.length && !body[i].isWhitespace() && body[i] != '{' && body[i] != '|' && body[i] != ';') i++
                                precTok = body.substring(s, i)
                            }
                            c.isLetterOrDigit() || c == '_' -> {
                                val s = i
                                while (i < body.length && (body[i].isLetterOrDigit() || body[i] == '_')) i++
                                syms += body.substring(s, i)
                            }
                            else -> throw SpecError("Caractere inesperado '$c' na regra '$lhs'")
                        }
                    }
                    val pid = prods.size
                    prods += Production(pid, lhs, syms)
                    actions[pid] = action
                    if (precTok != null) precOverride[pid] = precTok
                    if (body[i] == ';') { i++; break } else i++
                }
            }
            if (prods.isEmpty()) throw SpecError("Nenhuma regra encontrada")
            val ordered = prods.filter { it.lhs == start } + prods.filter { it.lhs != start }
            val idMap = ordered.withIndex().associate { it.value.id to it.index }
            val g = Grammar(start!!, ordered)
            val acts = actions.entries.associate { idMap.getValue(it.key) to it.value }
            val ruleNames = g.nonTerminals.toSet()
            for (p in g.productions) for (s in p.rhs) {
                if (s !in ruleNames && !s.startsWith("'") && s !in tokens)
                    throw SpecError("'$s' não é regra nem token declarado (faltou %token $s?)")
            }
            // +1 por causa da produção aumentada S' → S
            val explicit = precOverride.entries.associate { idMap.getValue(it.key) + 1 to it.value }
            val table = LRTable(g, LRKind.LALR1, prec, explicit, resolveConflicts = true)
            return MiniBison(g, acts, prec, tokens, table)
        }

        private fun stripComments(s: String): String =
            s.replace(Regex("/\\*[\\s\\S]*?\\*/"), " ").replace(Regex("//[^\\n]*"), " ")

        private fun splitSymbols(line: String): List<String> {
            val out = mutableListOf<String>()
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when {
                    c.isWhitespace() -> i++
                    c == '\'' && i + 2 < line.length && line[i + 2] == '\'' -> { out += line.substring(i, i + 3); i += 3 }
                    else -> { val s = i; while (i < line.length && !line[i].isWhitespace()) i++; out += line.substring(s, i) }
                }
            }
            return out
        }
    }

    /** Analisador léxico da entrada: números → NUM, palavras → ID (ou token homônimo), símbolos → 'c'. */
    fun lex(input: String): List<Pair<String, Long>> {
        val out = mutableListOf<Pair<String, Long>>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() -> {
                    val s = i; while (i < input.length && input[i].isDigit()) i++
                    out += "NUM" to input.substring(s, i).toLong()
                }
                c.isLetter() -> {
                    val s = i; while (i < input.length && input[i].isLetterOrDigit()) i++
                    val w = input.substring(s, i)
                    out += (if (w in declaredTokens) w else if (w.uppercase() in declaredTokens) w.uppercase() else "ID") to 0L
                }
                else -> { out += "'$c'" to 0L; i++ }
            }
        }
        return out
    }

    fun run(input: String): RunResult {
        val toks = try { lex(input) } catch (e: Exception) {
            return RunResult(false, null, emptyList(), null, e.message, emptyList())
        }
        val terminals = toks.map { it.first }
        val unknown = terminals.firstOrNull { it !in grammar.terminals }
        val result = LRParser.parse(table, terminals)
        if (!result.accepted) {
            val msg = if (unknown != null) "o token $unknown não aparece em nenhuma regra" else
                "syntax error (${result.steps.lastOrNull()?.note ?: ""})"
            return RunResult(false, null, result.steps, null, msg, emptyList())
        }
        // Reexecuta as reduções com valores semânticos (pilha de valores paralela, como no Bison)
        val values = mutableListOf<Long>()
        val reductions = mutableListOf<String>()
        var k = 0
        try {
            for (st in result.steps) {
                when (val a = st.action) {
                    is LRAction.Shift -> { values += toks[k].second; k++ }
                    is LRAction.Reduce -> {
                        val p = table.grammar.productions[a.prodId]
                        val n = p.rhs.size
                        val args = values.subList(values.size - n, values.size).toList()
                        repeat(n) { values.removeAt(values.lastIndex) }
                        val act = actions[a.prodId - 1]
                        val v = if (act.isNullOrBlank()) args.firstOrNull() ?: 0L else ActionEval.eval(act, args)
                        values += v
                        val shown = if (act.isNullOrBlank()) "(padrão) \$\$ = \$1" else act
                        reductions += "$p   ⇒  $shown  →  $v"
                    }
                    else -> {}
                }
            }
        } catch (e: ArithmeticException) {
            return RunResult(false, null, result.steps, result.tree, "erro na ação: ${e.message}", reductions)
        } catch (e: IllegalArgumentException) {
            return RunResult(false, null, result.steps, result.tree, "erro na ação: ${e.message}", reductions)
        }
        return RunResult(true, values.lastOrNull(), result.steps, result.tree, null, reductions)
    }
}

/** Avaliador das ações `$$ = ...;` — ele mesmo um pequeno analisador descendente recursivo. */
object ActionEval {
    fun eval(action: String, args: List<Long>): Long {
        val stmt = action.trim().removeSuffix(";").trim()
        val m = Regex("^\\$\\$\\s*=\\s*(.+)$").find(stmt)
            ?: throw IllegalArgumentException("só entendo ações do tipo \$\$ = expressão;")
        val src = m.groupValues[1]
        var i = 0
        fun ws() { while (i < src.length && src[i].isWhitespace()) i++ }
        lateinit var expr: () -> Long
        fun atom(): Long {
            ws()
            require(i < src.length) { "ação incompleta" }
            val c = src[i]
            return when {
                c == '(' -> { i++; val v = expr(); ws(); require(i < src.length && src[i] == ')') { "faltou ')'" }; i++; v }
                c == '-' -> { i++; -atom() }
                c == '$' -> {
                    i++; val s = i
                    while (i < src.length && src[i].isDigit()) i++
                    val n = src.substring(s, i).toIntOrNull() ?: throw IllegalArgumentException("use \$1, \$2, ...")
                    require(n in 1..args.size) { "\$$n não existe: a regra só tem ${args.size} símbolo(s)" }
                    args[n - 1]
                }
                c.isDigit() -> { val s = i; while (i < src.length && src[i].isDigit()) i++; src.substring(s, i).toLong() }
                else -> throw IllegalArgumentException("símbolo inesperado '$c' na ação")
            }
        }
        fun power(): Long {
            val b = atom(); ws()
            if (i < src.length && src[i] == '^') {
                i++; val e = power()
                var r = 1L; repeat(e.toInt().coerceIn(0, 62)) { r *= b }; return r
            }
            return b
        }
        fun term(): Long {
            var v = power()
            while (true) {
                ws()
                if (i >= src.length) return v
                v = when (src[i]) {
                    '*' -> { i++; v * power() }
                    '/' -> { i++; val d = power(); if (d == 0L) throw ArithmeticException("divisão por zero"); v / d }
                    '%' -> { i++; val d = power(); if (d == 0L) throw ArithmeticException("divisão por zero"); v % d }
                    else -> return v
                }
            }
        }
        fun sum(): Long {
            var v = term()
            while (true) {
                ws()
                if (i >= src.length) break
                v = when (src[i]) { '+' -> { i++; v + term() }; '-' -> { i++; v - term() }; else -> break }
            }
            return v
        }
        expr = {
            val l = sum()
            ws()
            when {
                src.startsWith("==", i) -> { i += 2; if (l == sum()) 1L else 0L }
                src.startsWith("!=", i) -> { i += 2; if (l != sum()) 1L else 0L }
                src.startsWith("<=", i) -> { i += 2; if (l <= sum()) 1L else 0L }
                src.startsWith(">=", i) -> { i += 2; if (l >= sum()) 1L else 0L }
                src.startsWith("<", i) -> { i += 1; if (l < sum()) 1L else 0L }
                src.startsWith(">", i) -> { i += 1; if (l > sum()) 1L else 0L }
                else -> l
            }
        }
        val v = expr()
        ws()
        require(i == src.length) { "sobrou '${src.substring(i)}' na ação" }
        return v
    }
}

/** Desafios da Oficina Bison: o jogador edita a especificação até a saída bater. */
object BisonChallenges {
    data class Challenge(
        val id: String,
        val title: String,
        val story: String,
        val starterSpec: String,
        val tests: List<Pair<String, Long?>>,   // entrada -> valor esperado (null = deve dar erro de sintaxe)
        val requireNoConflicts: Boolean,
        val hint: String,
    )

    private const val CALC_RULES = """expr : expr '+' expr { $$ = $1 + $3; }
     | expr '-' expr { $$ = $1 - $3; }
     | expr '*' expr { $$ = $1 * $3; }
     | expr '/' expr { $$ = $1 / $3; }
     | '(' expr ')'  { $$ = $2; }
     | NUM
     ;"""

    val all = listOf(
        Challenge(
            "b1", "A calculadora confusa",
            "A calculadora do Byte diz que 2+3*4 = 20. O Bison avisou conflitos, mas ninguém leu. Ensine precedência a ele!",
            "%token NUM\n%%\n$CALC_RULES\n",
            listOf("2+3*4" to 14L, "2*3+4" to 10L, "(2+3)*4" to 20L),
            true,
            "Linhas de %left mais EMBAIXO ligam MAIS FORTE. Coloque '+' '-' numa linha e '*' '/' na de baixo."
        ),
        Challenge(
            "b2", "Menos com menos",
            "8-3-2 deveria dar 3 (associa à esquerda: (8-3)-2). Mas a calculadora está achando 7!",
            "%token NUM\n%right '+' '-'\n%right '*' '/'\n%%\n$CALC_RULES\n",
            listOf("8-3-2" to 3L, "100/10/5" to 2L, "1+2*3" to 7L),
            true,
            "%right faz a-b-c virar a-(b-c). Subtração e divisão associam à ESQUERDA."
        ),
        Challenge(
            "b3", "Torre de potências",
            "Adicione o operador '^' (potência). Ele liga mais forte que * e associa à DIREITA: 2^3^2 = 2^9 = 512.",
            "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n$CALC_RULES\n",
            listOf("2^3^2" to 512L, "2*3^2" to 18L, "(2^3)^2" to 64L),
            true,
            "Você precisa de: uma linha %right '^' (por último!) e uma regra  expr '^' expr { $$ = $1 ^ $3; }"
        ),
        Challenge(
            "b4", "O menos solitário",
            "Faça -5+2 dar -3. O menos unário precisa de precedência própria: use %prec.",
            "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n$CALC_RULES\n",
            listOf("-5+2" to -3L, "-2*3" to -6L, "4--1" to 5L),
            true,
            "Declare %right UMINUS por último e a regra:  '-' expr %prec UMINUS { $$ = -$2; }"
        ),
        Challenge(
            "b5", "Gramática sem ambiguidade",
            "Outro jeito de resolver: em vez de %left, reescreva a gramática em camadas (expr, term, fator). Sem NENHUMA declaração de precedência!",
            "%token NUM\n%%\nexpr : expr '+' expr { $$ = $1 + $3; }\n     | expr '*' expr { $$ = $1 * $3; }\n     | NUM\n     ;\n",
            listOf("2+3*4" to 14L, "2*3+4" to 10L, "1+1+1" to 3L),
            true,
            "expr : expr '+' term | term ;   term : term '*' fator | fator ;   fator : NUM ;  (com as ações!)"
        ),
        Challenge(
            "b6", "Erro de propósito",
            "Faça 1<2<3 dar ERRO de sintaxe (comparações não se encadeiam). Os testes com um único '<' devem funcionar (valor 1 = verdadeiro).",
            "%token NUM\n%left '<'\n%left '+'\n%%\nexpr : expr '<' expr { $$ = $1 < $3; }\n     | expr '+' expr { $$ = $1 + $3; }\n     | NUM\n     ;\n",
            listOf("1<2" to 1L, "1+1<3" to 1L, "1<2<3" to null),
            true,
            "%left '<' deixa 1<2<3 virar (1<2)<3. Troque por %nonassoc '<': aí a<b<c vira erro de sintaxe, como em várias linguagens."
        ),
    )

    data class Verdict(val passed: Boolean, val lines: List<Pair<Boolean, String>>)

    fun check(ch: Challenge, spec: String): Verdict {
        val bison = try { MiniBison.compile(spec) } catch (e: Exception) {
            return Verdict(false, listOf(false to "Erro na especificação: ${e.message}"))
        }
        val lines = mutableListOf<Pair<Boolean, String>>()
        if (ch.requireNoConflicts) {
            val ok = bison.conflicts.isEmpty()
            lines += ok to (if (ok) "Sem conflitos" else "${bison.conflicts.size} conflito(s): " +
                bison.conflicts.take(2).joinToString("; ") { "estado ${it.state} com ${it.terminal} (${it.kind})" })
        }
        for ((input, expected) in ch.tests) {
            val r = bison.run(input)
            val ok = if (expected == null) !r.accepted else r.accepted && r.value == expected
            val got = if (r.accepted) "${r.value}" else "erro de sintaxe"
            val exp = expected?.toString() ?: "erro de sintaxe"
            lines += ok to "$input  →  $got   (esperado: $exp)"
        }
        return Verdict(lines.all { it.first }, lines)
    }
}
