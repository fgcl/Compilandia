package com.compilandia.tutor

import com.compilandia.content.Episode
import com.compilandia.content.Library
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Skill
import com.compilandia.learning.normalizeForMatch
import com.compilandia.textos.tx

data class Resposta(val texto: String, val clipe: Episode?, val skill: Skill?)

/**
 * Perguntas livres, sem internet. Primeiro procura o clipe do assunto: resposta curta, com desenho.
 * Se não achar, procura nas fichas de revisão, que já estão no jogo. Sem achar, devolve null:
 * melhor dizer que não sabe do que inventar.
 */
object Respostas {
    /** Palavras que levam a cada clipe. O título do clipe também conta. */
    private val termos: Map<String, List<String>> = mapOf(
        tx("comum_que_compilador_faz") to listOf("compilador", "compilar", "compila"),
        tx("comum_compilador_ou_interpretador") to listOf("interpretador", "interpretar", "bytecode"),
        tx("comum_token_lexema") to listOf("token", "tokens", "lexema", "padrao"),
        tx("comum_maior_casamento") to listOf(tx("comum_maior_casamento_2"), tx("respostas_maximal_munch"), "casamento"),
        tx("comum_palavra_chave_ou_nome") to listOf(tx("respostas_palavra_chave"), tx("respostas_palavra_reservada"), "identificador"),
        tx("comum_que_estrela_faz") to listOf("estrela", tx("respostas_fecho_kleene"), "kleene", "regex", tx("respostas_expressao_regular")),
        tx("comum_afn_afd") to listOf("afn", "afd", "automato", tx("respostas_nao_deterministico")),
        tx("comum_epsilon_fecho") to listOf(tx("respostas_epsilon_fecho"), "fecho", "subconjuntos"),
        tx("comum_gramatica_em_30_segundos") to listOf("gramatica", "derivacao", "producao", "regra"),
        tx("comum_terminal_nao_terminal") to listOf("terminal", tx("respostas_nao_terminal"), tx("respostas_nao_terminais"), "variavel"),
        tx("comum_que_ambiguidade") to listOf("ambiguidade", "ambigua", "ambiguo", tx("respostas_duas_arvores")),
        tx("comum_por_que_recursao_trava") to listOf(tx("respostas_recursao_esquerda"), tx("respostas_recursao_esquerda_2"), "recursao"),
        tx("comum_fatorar_adiar_escolha") to listOf("fatorar", "fatoracao", tx("comum_prefixo_comum")),
        tx("comum_que_first") to listOf("first", "primeiro"),
        tx("comum_que_follow") to listOf("follow", "seguinte"),
        tx("comum_para_que_serve_epsilon") to listOf("epsilon", tx("respostas_cadeia_vazia"), "vazio"),
        tx("comum_que_quer_dizer_ll_1") to listOf("ll 1", "ll1", tx("respostas_tabela_preditiva"), "preditivo", "preditiva", tx("respostas_tabela_ll")),
        tx("comum_descida_recursiva") to listOf(tx("respostas_descida_recursiva"), "descendente", "casatoken"),
        tx("comum_modo_panico") to listOf("panico", tx("respostas_recuperacao_erro"), tx("respostas_recuperar_erro"), "sincronizar"),
        tx("comum_que_handle") to listOf("handle", "alca"),
        tx("comum_empilhar_ou_reduzir") to listOf("shift", "reduce", "empilhar", "reduzir", "empilha", tx("respostas_bottom_up"), "ascendente"),
        tx("comum_ponto_item") to listOf("item", "itens", "lr 0", "lr0", "closure", "goto"),
        tx("comum_conflito_shift_reduce") to listOf("conflito", "conflitos", "reduce reduce", "shift reduce"),
        tx("comum_que_lalr_faz") to listOf("lalr", "lr 1", "lr1"),
        tx("comum_precedencia_no_bison") to listOf("bison", "yacc", "precedencia", "associatividade", "left", "right"),
        tx("comum_atributo_sintetizado_herdado") to listOf("atributo", "atributos", "sintetizado", "herdado", tx("respostas_atribuida"), tx("respostas_l_atribuida")),
        tx("comum_coercao_tipo") to listOf("coercao", "tipo", "tipos", "conversao"),
        tx("comum_tabela_simbolos") to listOf(tx("respostas_tabela_simbolos"), "escopo", "escopos", "declaracao"),
        tx("comum_ast_ou_arvore_derivacao") to listOf("ast", tx("respostas_arvore_abstrata"), tx("respostas_arvore_sintatica")),
        tx("comum_tres_enderecos") to listOf(tx("respostas_tres_enderecos"), "tac", tx("respostas_codigo_intermediario"), "intermediario"),
        tx("comum_dobra_constantes") to listOf("dobra", "otimizacao", "otimizar", "constante"),
        tx("comum_registro_ativacao") to listOf(tx("respostas_registro_ativacao"), "ativacao", tx("comum_pilha_chamadas"), "recursiva", "frame"),
        tx("comum_registrador_memoria") to listOf("mips", "registrador", "registradores", "lw", "sw", "assembly"),
        tx("comum_que_spill") to listOf("spill", "derramar", tx("respostas_alocacao_registradores"), "coloracao"),
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
