package com.compilandia.engine

import com.compilandia.textos.tx

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
                    out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, tx("minibison_empilha_1_vai_para_estado", st.input.first(), a.state), null)
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
                    out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, tx("minibison_reduz_1", p), conta)
                    repeat(n) { valores.removeAt(valores.lastIndex) }
                    valores += v
                }
                LRAction.Accept -> out += Quadro(st.states, st.symbols, valores.toList(), st.input, a, tx("minibison_aceita_valor_final_1", valores.lastOrNull() ?: "?"), null)
                null -> out += Quadro(st.states, st.symbols, valores.toList(), st.input, null, tx("minibison_erro_sintaxe_nada_fazer_com", st.input.firstOrNull() ?: tx("minibison_fim"), st.states.last()), null)
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
            if (parts.size < 2) throw SpecError(tx("minibison_faltou_separador_entre_declaracoes_regras"))
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
                    else -> throw SpecError(tx("minibison_declaracao_desconhecida_1", line))
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
                if (lhs.isEmpty()) throw SpecError(tx("minibison_esperava_nome_regra_perto_1", body.substring(i).take(15)))
                skipWs()
                if (i >= body.length || body[i] != ':') throw SpecError(tx("minibison_faltou_depois_1", lhs))
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
                                if (i >= body.length) throw SpecError(tx("minibison_acao_sem"))
                                action = body.substring(s + 1, i).trim(); i++
                            }
                            c == '\'' -> {
                                if (i + 2 >= body.length || body[i + 2] != '\'') throw SpecError(tx("minibison_literal_mal_formado_perto_1", body.substring(i).take(5)))
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
                            else -> throw SpecError(tx("minibison_caractere_inesperado_1_na_regra", c, lhs))
                        }
                    }
                    val pid = prods.size
                    prods += Production(pid, lhs, syms)
                    actions[pid] = action
                    if (precTok != null) precOverride[pid] = precTok
                    if (body[i] == ';') { i++; break } else i++
                }
            }
            if (prods.isEmpty()) throw SpecError(tx("minibison_nenhuma_regra_encontrada"))
            val ordered = prods.filter { it.lhs == start } + prods.filter { it.lhs != start }
            val idMap = ordered.withIndex().associate { it.value.id to it.index }
            val g = Grammar(start!!, ordered)
            val acts = actions.entries.associate { idMap.getValue(it.key) to it.value }
            val ruleNames = g.nonTerminals.toSet()
            for (p in g.productions) for (s in p.rhs) {
                if (s !in ruleNames && !s.startsWith("'") && s !in tokens)
                    throw SpecError(tx("minibison_1_nao_regra_nem_token", s, s))
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
            val msg = if (unknown != null) tx("minibison_token_1_nao_aparece_em", unknown) else
                tx("minibison_syntax_error_1", result.steps.lastOrNull()?.note ?: "")
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
                        val shown = if (act.isNullOrBlank()) tx("minibison_padrao_1") else act
                        reductions += "$p   ⇒  $shown  →  $v"
                    }
                    else -> {}
                }
            }
        } catch (e: ArithmeticException) {
            return RunResult(false, null, result.steps, result.tree, tx("minibison_erro_na_acao_1", e.message), reductions)
        } catch (e: IllegalArgumentException) {
            return RunResult(false, null, result.steps, result.tree, tx("minibison_erro_na_acao_1", e.message), reductions)
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
            require(i < src.length) { tx("minibison_acao_incompleta") }
            val c = src[i]
            return when {
                c == '(' -> { i++; val v = expr(); ws(); require(i < src.length && src[i] == ')') { tx("minibison_faltou") }; i++; v }
                c == '-' -> { i++; -atom() }
                c == '$' -> {
                    i++; val s = i
                    while (i < src.length && src[i].isDigit()) i++
                    val n = src.substring(s, i).toIntOrNull() ?: throw IllegalArgumentException("use \$1, \$2, ...")
                    require(n in 1..args.size) { tx("minibison_1_nao_existe_regra_so", n, args.size) }
                    args[n - 1]
                }
                c.isDigit() -> { val s = i; while (i < src.length && src[i].isDigit()) i++; src.substring(s, i).toLong() }
                // pow(a, b) como no math.h do C, para a potência das aulas (em C, ^ é ou exclusivo).
                src.startsWith("pow(", i) -> {
                    i += 4; val b = expr(); ws(); require(i < src.length && src[i] == ',') { tx("minibison_faltou") }; i++
                    val e = expr(); ws(); require(i < src.length && src[i] == ')') { tx("minibison_faltou") }; i++
                    var r = 1L; repeat(e.toInt().coerceIn(0, 62)) { r *= b }; r
                }
                else -> throw IllegalArgumentException(tx("solta_minibison_1", c))
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
        require(i == src.length) { tx("minibison_sobrou_1_na_acao", src.substring(i)) }
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
            "b1", tx("minibison_calculadora_confusa"),
            tx("minibison_calculadora_byte_diz_que_2"),
            "%token NUM\n%%\n$CALC_RULES\n",
            listOf("2+3*4" to 14L, "2*3+4" to 10L, "(2+3)*4" to 20L),
            true,
            tx("minibison_linhas_left_mais_embaixo_ligam")
        ),
        Challenge(
            "b2", tx("minibison_menos_com_menos"),
            tx("minibison_8_3_2_deveria_dar"),
            "%token NUM\n%right '+' '-'\n%right '*' '/'\n%%\n$CALC_RULES\n",
            listOf("8-3-2" to 3L, "100/10/5" to 2L, "1+2*3" to 7L),
            true,
            tx("minibison_right_faz_b_c_virar")
        ),
        Challenge(
            "b3", tx("minibison_torre_potencias"),
            tx("minibison_adicione_operador_potencia_ele_liga"),
            "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n$CALC_RULES\n",
            listOf("2^3^2" to 512L, "2*3^2" to 18L, "(2^3)^2" to 64L),
            true,
            tx("minibison_voce_precisa_linha_right_por")
        ),
        Challenge(
            "b4", tx("minibison_menos_solitario"),
            tx("minibison_faca_5_2_dar_3"),
            "%token NUM\n%left '+' '-'\n%left '*' '/'\n%%\n$CALC_RULES\n",
            listOf("-5+2" to -3L, "-2*3" to -6L, "4--1" to 5L),
            true,
            tx("minibison_declare_right_uminus_por_ultimo")
        ),
        Challenge(
            "b5", tx("minibison_gramatica_sem_ambiguidade"),
            tx("minibison_outro_jeito_resolver_em_vez"),
            "%token NUM\n%%\nexpr : expr '+' expr { $$ = $1 + $3; }\n     | expr '*' expr { $$ = $1 * $3; }\n     | NUM\n     ;\n",
            listOf("2+3*4" to 14L, "2*3+4" to 10L, "1+1+1" to 3L),
            true,
            tx("minibison_expr_expr_term_term_term")
        ),
        Challenge(
            "b6", tx("minibison_erro_proposito"),
            tx("minibison_faca_1_2_3_dar"),
            "%token NUM\n%left '<'\n%left '+'\n%%\nexpr : expr '<' expr { $$ = $1 < $3; }\n     | expr '+' expr { $$ = $1 + $3; }\n     | NUM\n     ;\n",
            listOf("1<2" to 1L, "1+1<3" to 1L, "1<2<3" to null),
            true,
            tx("minibison_left_deixa_1_2_3")
        ),
    )

    data class Verdict(val passed: Boolean, val lines: List<Pair<Boolean, String>>)

    fun check(ch: Challenge, spec: String): Verdict {
        val bison = try { MiniBison.compile(spec) } catch (e: Exception) {
            return Verdict(false, listOf(false to tx("minibison_erro_na_especificacao_1", e.message)))
        }
        val lines = mutableListOf<Pair<Boolean, String>>()
        if (ch.requireNoConflicts) {
            val ok = bison.conflicts.isEmpty()
            lines += ok to (if (ok) tx("minibison_sem_conflitos") else tx("minibison_1_conflito", bison.conflicts.size) +
                bison.conflicts.take(2).joinToString("; ") { tx("minibison_estado_1_com_2_3", it.state, it.terminal, it.kind) })
        }
        for ((input, expected) in ch.tests) {
            val r = bison.run(input)
            val ok = if (expected == null) !r.accepted else r.accepted && r.value == expected
            val got = if (r.accepted) "${r.value}" else tx("minibison_erro_sintaxe")
            val exp = expected?.toString() ?: tx("minibison_erro_sintaxe")
            lines += ok to tx("minibison_1_2_esperado_3", input, got, exp)
        }
        return Verdict(lines.all { it.first }, lines)
    }
}
