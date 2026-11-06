package com.compilandia.content

import com.compilandia.content.episodes.Aberturas
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Learner
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** O tipo de cada etapa da trilha de um assunto, na ordem em que se aprende. */
enum class Etapa { BUG, VER, FAZER, PRATICA, PEGADINHA, DESAFIO,
    /** A7: depois da trilha completa, quando a memória cai, um clipe de 30 segundos e um desafio. Não fica na lista. */
    REVISAO }

/** Uma etapa da trilha. As de Prática e Pegadinha apontam para a aula da temporada. */
data class PassoTrilha(val etapa: Etapa, val episodio: Episode? = null) {
    /** Como a etapa fica guardada no save. */
    val chave: String get() = if (episodio != null && etapa != Etapa.VER) "${etapa.name}:${episodio.id}" else etapa.name

    val titulo: String get() = when (etapa) {
        Etapa.BUG -> tx("trilha_etapa_bug")
        Etapa.VER -> tx("trilha_etapa_ver")
        Etapa.FAZER -> tx("trilha_etapa_fazer")
        Etapa.PRATICA -> tx("trilha_etapa_pratica")
        Etapa.PEGADINHA -> tx("trilha_etapa_pegadinha")
        Etapa.DESAFIO -> tx("trilha_etapa_desafio")
        Etapa.REVISAO -> tx("trilha_etapa_revisao")
    }

    val subtitulo: String get() = when (etapa) {
        Etapa.BUG -> tx("trilha_etapa_bug_sub")
        Etapa.VER -> episodio?.title ?: tx("trilha_etapa_ver_sub")
        Etapa.FAZER -> tx("trilha_etapa_fazer_sub")
        Etapa.PRATICA, Etapa.PEGADINHA -> episodio?.title.orEmpty()
        Etapa.DESAFIO -> tx("trilha_etapa_desafio_sub")
        Etapa.REVISAO -> tx("trilha_etapa_revisao_sub")
    }
}

enum class EstadoEtapa { FEITA, PULADA, PENDENTE }

/**
 * A trilha de um assunto (A1): tudo o que o jogo tem sobre ele, numa linha só.
 * Errar ou pular nunca tranca nada; a trilha só sugere a ordem.
 */
object Trilha {
    fun passos(s: Skill): List<PassoTrilha> = buildList {
        if (Aberturas.of(s) != null) add(PassoTrilha(Etapa.BUG))
        Episodes.of(s)?.let { add(PassoTrilha(Etapa.VER, it)) }
        if (Guiado.disponivel(s)) add(PassoTrilha(Etapa.FAZER))
        for (e in Episodes.all.filter { it.skill == s && it.temporada == Temporada.PRATICA }) add(PassoTrilha(Etapa.PRATICA, e))
        for (e in Episodes.all.filter { it.skill == s && it.temporada == Temporada.PEGADINHA }) add(PassoTrilha(Etapa.PEGADINHA, e))
        add(PassoTrilha(Etapa.DESAFIO))
    }

    /** A aula de tabelas do assunto, oferecida como outro jeito de ver dentro da etapa Ver. */
    fun outroJeito(s: Skill): Episode? = Episodes.all.firstOrNull { it.skill == s && it.temporada == Temporada.TABELAS }

    fun estado(l: Learner, s: Skill, p: PassoTrilha): EstadoEtapa {
        val ls = l.state
        val marca = l.etapa(s, p.chave)
        val feita = marca == "feita" || when (p.etapa) {
            Etapa.BUG, Etapa.VER -> s.name in ls.episodesSeen
            Etapa.PRATICA, Etapa.PEGADINHA -> p.episodio?.id in ls.episodesSeen
            Etapa.DESAFIO -> l.skill(s).attempts > 0
            Etapa.FAZER -> false
            Etapa.REVISAO -> false
        }
        return when {
            feita -> EstadoEtapa.FEITA
            marca == "pulada" -> EstadoEtapa.PULADA
            else -> EstadoEtapa.PENDENTE
        }
    }

    fun opcional(l: Learner, p: PassoTrilha): Boolean = p.etapa != Etapa.VER && p.etapa != Etapa.DESAFIO && l.etapaOpcional(p.etapa.name)

    /**
     * A etapa que o Continuar abre. Null quando não sobra nada pendente.
     * Com [nowMs], a trilha completa de um assunto que está sendo esquecido oferece a revisão rápida (A7).
     */
    fun proximo(l: Learner, s: Skill, nowMs: Long? = null): PassoTrilha? =
        passos(s).firstOrNull { estado(l, s, it) == EstadoEtapa.PENDENTE && !opcional(l, it) }
            ?: if (nowMs != null && l.skill(s).memory.isDue(nowMs)) revisao(l, s) else null

    /** O clipe da revisão: muda a cada revisão, para não repetir. */
    fun revisao(l: Learner, s: Skill): PassoTrilha? {
        val clipes = Episodes.clipes.filter { it.skill == s }
        if (clipes.isEmpty()) return null
        return PassoTrilha(Etapa.REVISAO, clipes[l.skill(s).memory.reviews % clipes.size])
    }

    fun feitas(l: Learner, s: Skill): Int = passos(s).count { estado(l, s, it) == EstadoEtapa.FEITA }

    fun completa(l: Learner, s: Skill): Boolean = proximo(l, s) == null

    /** O primeiro assunto da região com trilha por terminar, depois de [s]. */
    fun proximoAssunto(l: Learner, s: Skill): Skill? {
        val daRegiao = Skill.entries.filter { it.region == s.region }
        val depois = daRegiao.dropWhile { it != s }.drop(1) + daRegiao.takeWhile { it != s }
        return depois.firstOrNull { !completa(l, it) }
    }
}

/** Uma parada da trilha "Seu compilador" (A5): aula em C ou capítulo do Guia do TP. */
data class ParadaCompilador(val episodio: Episode? = null, val capitulo: GuideChapter? = null) {
    val skill: Skill get() = episodio?.skill ?: capitulo!!.skill
    val titulo: String get() = episodio?.title ?: capitulo!!.title
}

object SeuCompilador {
    /**
     * As 5 aulas em C e os capítulos do guia intercalados pela fase do compilador.
     * Cada série mantém a própria ordem; no empate, a aula vem antes do capítulo.
     */
    fun paradas(): List<ParadaCompilador> {
        val aulas = ArrayDeque(Episodes.all.filter { it.temporada == Temporada.EM_C }.map { ParadaCompilador(episodio = it) })
        val caps = ArrayDeque(Library.guide.sortedBy { it.number }.map { ParadaCompilador(capitulo = it) })
        return buildList {
            while (aulas.isNotEmpty() || caps.isNotEmpty()) {
                val a = aulas.firstOrNull()
                val c = caps.firstOrNull()
                add(if (c == null || (a != null && a.skill.ordinal <= c.skill.ordinal)) aulas.removeFirst() else caps.removeFirst())
            }
        }
    }

    /** Abre depois da Floresta Léxica: as aulas básicas de tokens e de autômatos vistas. */
    fun aberta(l: Learner): Boolean = Skill.TOKENS.name in l.state.episodesSeen && Skill.REGEX_DFA.name in l.state.episodesSeen
}

/**
 * Fazer com ajuda (A3): quatro problemas gerados pelo motor, com a ajuda sumindo aos poucos.
 * Cada problema é uma simulação do Laboratório; as perguntas dela são os passos que o Byte faz ou que a pessoa faz.
 */
object Guiado {
    const val PROBLEMAS = 4
    private val cache = mutableMapOf<Skill, Boolean>()

    /**
     * Gramáticas pequenas e com cara de linguagem de verdade para os problemas de gramática LL.
     * O sorteio solto do gerador criava regras como "S → d d" e repetia a gramática reserva.
     * GuiadoTest confere que todas são LL(1).
     */
    val GRAMATICAS_LL = listOf(
        "E -> T X\nX -> + T X | ε\nT -> id | ( E )",
        "L -> id R\nR -> , id R | ε",
        "S -> if C then S | print\nC -> id",
        "D -> T id ;\nT -> int | float",
        "P -> begin L end\nL -> C L | ε\nC -> id ;",
        "E -> num F\nF -> * num F | ε",
        "S -> ( S ) S | ε",
        "A -> id B\nB -> = num | ( )",
    )

    private val ASSUNTOS_LL = setOf(Skill.FIRST, Skill.FOLLOW, Skill.LL1_TABLE, Skill.LL1_PARSING, Skill.RECURSIVE_DESCENT)

    /** O assunto tem simulação com ao menos duas perguntas. */
    fun disponivel(s: Skill): Boolean = cache.getOrPut(s) { problemas(s, 1).size == PROBLEMAS }

    private fun simLL(s: Skill, g: com.compilandia.engine.Grammar, rnd: kotlin.random.Random): com.compilandia.tutor.Sim? = when (s) {
        Skill.FIRST -> com.compilandia.tutor.Sims.conjuntos(g, com.compilandia.engine.SetStep.Kind.FIRST)
        Skill.FOLLOW -> com.compilandia.tutor.Sims.conjuntos(g, com.compilandia.engine.SetStep.Kind.FOLLOW)
        Skill.LL1_TABLE -> com.compilandia.tutor.Sims.tabelaLL(g)
        else -> com.compilandia.engine.SentenceGenerator.generate(g, rnd, maxTokens = 7)?.let { com.compilandia.tutor.Sims.parseLL(g, it) }
    }

    /** Os quatro problemas, todos diferentes. Vazio quando o assunto não tem simulação com perguntas. */
    fun problemas(s: Skill, semente: Long): List<com.compilandia.tutor.Sim> {
        val rnd = kotlin.random.Random(semente)
        val achados = mutableListOf<com.compilandia.tutor.Sim>()
        val vistos = mutableSetOf<String>()
        fun tenta(sim: com.compilandia.tutor.Sim?, marca: String) {
            if (sim == null || achados.size >= PROBLEMAS || marca in vistos) return
            if (sim.passos.count { it.ask != null } < 2) return
            vistos += marca
            achados += sim
        }
        try {
            if (s in ASSUNTOS_LL) {
                for (txt in GRAMATICAS_LL.shuffled(rnd)) tenta(simLL(s, com.compilandia.engine.Grammar.parse(txt), rnd), txt)
            } else {
                for (t in 0 until 24) {
                    val sim = com.compilandia.tutor.Sims.exemplo(s, kotlin.random.Random(semente + t * 7919)) ?: continue
                    tenta(sim, sim.passos.joinToString("|") { it.say })
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return if (achados.size == PROBLEMAS) achados else emptyList()
    }

    /** O problema [p] (0 a 3) de uma série gerada com [semente]. */
    fun problema(s: Skill, p: Int, semente: Long): com.compilandia.tutor.Sim? = problemas(s, semente).getOrNull(p)

    /**
     * Os passos que a pessoa responde no problema [p]: no primeiro, nenhum (o Byte resolve tudo);
     * no segundo, só o último; no terceiro, todos menos o primeiro; no quarto, todos.
     */
    fun exigir(sim: com.compilandia.tutor.Sim, p: Int): Set<Int> {
        val perguntas = sim.passos.indices.filter { sim.passos[it].ask != null }
        val doByte = when (p) { 0 -> perguntas.size; 1 -> perguntas.size - 1; 2 -> 1; else -> 0 }
        return perguntas.drop(doByte.coerceIn(0, perguntas.size)).toSet()
    }
}
