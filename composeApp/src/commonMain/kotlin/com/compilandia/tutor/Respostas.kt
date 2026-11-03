package com.compilandia.tutor

import com.compilandia.content.Episode
import com.compilandia.content.Library
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Skill
import com.compilandia.learning.normalizeForMatch

data class Resposta(val texto: String, val clipe: Episode?, val skill: Skill?)

/**
 * Perguntas livres, sem internet. Primeiro procura o clipe do assunto: resposta curta, com desenho.
 * Se não achar, procura nas fichas de revisão, que já estão no jogo. Sem achar, devolve null:
 * melhor dizer que não sabe do que inventar.
 */
object Respostas {
    /** Palavras que levam a cada clipe. O título do clipe também conta. */
    private val termos: Map<String, List<String>> = mapOf(
        "O que um compilador faz" to listOf("compilador", "compilar", "compila"),
        "Compilador ou interpretador" to listOf("interpretador", "interpretar", "bytecode"),
        "Token e lexema" to listOf("token", "tokens", "lexema", "padrao"),
        "Maior casamento" to listOf("maior casamento", "maximal munch", "casamento"),
        "Palavra-chave ou nome" to listOf("palavra chave", "palavra reservada", "identificador"),
        "O que a estrela faz" to listOf("estrela", "fecho de kleene", "kleene", "regex", "expressao regular"),
        "AFN e AFD" to listOf("afn", "afd", "automato", "nao deterministico"),
        "Épsilon-fecho" to listOf("epsilon fecho", "fecho", "subconjuntos"),
        "Gramática em 30 segundos" to listOf("gramatica", "derivacao", "producao", "regra"),
        "Terminal e não terminal" to listOf("terminal", "nao terminal", "nao terminais", "variavel"),
        "O que é ambiguidade" to listOf("ambiguidade", "ambigua", "ambiguo", "duas arvores"),
        "Por que a recursão trava" to listOf("recursao a esquerda", "recursao esquerda", "recursao"),
        "Fatorar é adiar a escolha" to listOf("fatorar", "fatoracao", "prefixo comum"),
        "O que é FIRST" to listOf("first", "primeiro"),
        "O que é FOLLOW" to listOf("follow", "seguinte"),
        "Para que serve o épsilon" to listOf("epsilon", "cadeia vazia", "vazio"),
        "O que quer dizer LL(1)" to listOf("ll 1", "ll1", "tabela preditiva", "preditivo", "preditiva", "tabela ll"),
        "Descida recursiva" to listOf("descida recursiva", "descendente", "casatoken"),
        "Modo pânico" to listOf("panico", "recuperacao de erro", "recuperar erro", "sincronizar"),
        "O que é handle" to listOf("handle", "alca"),
        "Empilhar ou reduzir" to listOf("shift", "reduce", "empilhar", "reduzir", "empilha", "bottom up", "ascendente"),
        "O ponto do item" to listOf("item", "itens", "lr 0", "lr0", "closure", "goto"),
        "Conflito shift-reduce" to listOf("conflito", "conflitos", "reduce reduce", "shift reduce"),
        "O que o LALR faz" to listOf("lalr", "lr 1", "lr1"),
        "Precedência no Bison" to listOf("bison", "yacc", "precedencia", "associatividade", "left", "right"),
        "Atributo sintetizado e herdado" to listOf("atributo", "atributos", "sintetizado", "herdado", "s atribuida", "l atribuida"),
        "Coerção de tipo" to listOf("coercao", "tipo", "tipos", "conversao"),
        "Tabela de símbolos" to listOf("tabela de simbolos", "escopo", "escopos", "declaracao"),
        "AST ou árvore de derivação" to listOf("ast", "arvore abstrata", "arvore sintatica"),
        "Três endereços" to listOf("tres enderecos", "tac", "codigo intermediario", "intermediario"),
        "Dobra de constantes" to listOf("dobra", "otimizacao", "otimizar", "constante"),
        "Registro de ativação" to listOf("registro de ativacao", "ativacao", "pilha de chamadas", "recursiva", "frame"),
        "Registrador e memória" to listOf("mips", "registrador", "registradores", "lw", "sw", "assembly"),
        "O que é spill" to listOf("spill", "derramar", "alocacao de registradores", "coloracao"),
    )

    /** Palavras de pergunta: aparecem em tudo e não dizem o assunto. */
    private val VAZIAS = setOf("qual", "quais", "como", "onde", "quando", "porque", "para", "pelo", "pela", "isso", "esse", "essa", "este", "esta", "sobre", "entre", "significa", "explica", "explique", "serve", "fazer", "faco", "diferenca", "pode", "deve")

    private fun norm(t: String) = " " + normalizeForMatch(t).replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim() + " "

    fun busca(pergunta: String): Resposta? {
        val q = norm(pergunta)
        val melhor = Episodes.clipes.mapNotNull { c ->
            val lista = termos[c.title].orEmpty() + normalizeForMatch(c.title)
            // termo mais longo pesa mais: "maior casamento" ganha de "casamento"
            val pontos = lista.filter { " ${norm(it).trim()} " in q }.sumOf { it.length }
            if (pontos > 0) c to pontos else null
        }.maxByOrNull { it.second }
        if (melhor != null) {
            val c = melhor.first
            return Resposta(c.passos.take(2).joinToString(" ") { it.fala.plain }, c, c.skill)
        }
        return fichas(q)
    }

    /** Fallback: a ficha de revisão que mais divide palavras com a pergunta. */
    private fun fichas(q: String): Resposta? {
        val palavras = q.trim().split(' ').filter { it.length >= 4 && it !in VAZIAS }.toSet()
        if (palavras.isEmpty()) return null
        // Exige acertar a maior parte da pergunta: resposta errada ensina errado.
        val minimo = maxOf(1, (palavras.size * 0.6).toInt() + if (palavras.size > 1) 1 else 0).coerceAtMost(palavras.size)
        val cand = Library.cards.flatMap { c -> c.flashcards.map { (p, r) -> Triple(c.skill, p, r) } }
            .map { t -> t to palavras.count { " $it " in norm(t.second) } }
            .filter { it.second >= minimo }
        val top = cand.maxByOrNull { it.second } ?: return null
        val (skill, _, resp) = top.first
        return Resposta(resp, null, skill)
    }
}
