package com.compilandia.tutor

/** O que a pessoa colou no Laboratório. O Byte descobre sozinho: nada de escolher ferramenta. */
enum class Tipo { VAZIO, GRAMATICA, BISON, FLEX, MIPS, REGEX, EXPRESSAO, CODIGO, PERGUNTA }

object Entrada {
    private val pergunta = Regex(
        "^(o que|oque|oq|como|por que|porque|pq|qual|quais|quando|onde|quem|pra que|para que|explica|explique|me explica|me ensina|ensina|diferença|diferenca|define|defina|significa|cade|cadê)\\b",
        RegexOption.IGNORE_CASE,
    )
    private val instrucao = Regex("^\\s*(li|la|lw|sw|add|addi|sub|mul|move|syscall|jal|jr|beq|bne|ble|j)\\b", RegexOption.MULTILINE)
    private val palavraChave = Regex("\\b(if|else|while|for|int|float|return|void|char)\\b")

    fun tipo(bruto: String): Tipo {
        val t = bruto.trim()
        if (t.isEmpty()) return Tipo.VAZIO
        if ("%%" in t) return if (Regex("%token|%left|%right|%start|\\$\\$").containsMatchIn(t)) Tipo.BISON else Tipo.FLEX
        if (".text" in t || "main:" in t || instrucao.findAll(t).count() >= 2) return Tipo.MIPS
        if (t.endsWith("?") || pergunta.containsMatchIn(t)) return Tipo.PERGUNTA
        if ("->" in t || "→" in t || "::=" in t) return Tipo.GRAMATICA
        if (pareceRegex(t)) return Tipo.REGEX
        if (';' in t || '{' in t || palavraChave.containsMatchIn(t)) return Tipo.CODIGO
        if (pareceExpressao(t)) return Tipo.EXPRESSAO
        return Tipo.PERGUNTA
    }

    private fun pareceRegex(t: String): Boolean {
        if (' ' in t || !t.all { it.isLetterOrDigit() || it in "|*+?()ε" }) return false
        if ('|' in t) return true
        // ab*, (ab)+: sem operador entre letras não é expressão de código
        return Regex("[a-z0-9)][a-z0-9(]|[*+?)][a-z0-9(]|\\)[*+?]|[a-z0-9][*?]$").containsMatchIn(t) && t.any { it in "*+?(" }
    }

    private fun pareceExpressao(t: String): Boolean {
        val ok = t.all { it.isLetterOrDigit() || it.isWhitespace() || it in "+-*/()<>=!&|._" }
        return ok && t.any { it in "+-*/<>" } && t.count { it.isWhitespace() } <= 16 && !Regex("[A-Za-zÀ-ÿ]{3,} [A-Za-zÀ-ÿ]{3,}").containsMatchIn(t)
    }
}
