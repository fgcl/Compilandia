package com.compilandia.content

import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.Learner
import com.compilandia.learning.Memory
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/**
 * R2: os jeitos de revisar um assunto. A revisão escolhe o que a pessoa ainda não usou,
 * ou usou há mais tempo. Nunca repete a aula inteira.
 */
enum class FormatoRevisao {
    /** Prever o próximo passo num exemplo novo gerado pelo motor (o Exemplo do Laboratório). */
    PREVER,
    /** Julgar as frases de um Bug do Álbum. */
    BUG,
    /** Uma fase do robô da Bancada. */
    ROBO,
    /** O clipe de 30 segundos (A7). */
    CLIPE,
    /** Um jogo do assunto: existe para todo assunto, então é o último da fila. */
    JOGO,
    /** A pegadinha do assunto. */
    PEGADINHA;

    val titulo: String get() = when (this) {
        PREVER -> tx("revisao_formato_prever"); BUG -> tx("revisao_formato_bug"); ROBO -> tx("revisao_formato_robo")
        CLIPE -> tx("revisao_formato_clipe"); JOGO -> tx("revisao_formato_jogo"); PEGADINHA -> tx("revisao_formato_pegadinha")
    }
}

object Revisao {
    private val temExemplo = mutableMapOf<Skill, Boolean>()

    private fun exemplo(s: Skill): Boolean = temExemplo.getOrPut(s) {
        try { com.compilandia.tutor.Sims.exemplo(s, kotlin.random.Random(1)) != null } catch (e: Exception) { false }
    }

    /** Os formatos que o assunto tem, na ordem de preferência para o empate. */
    fun formatos(s: Skill): List<FormatoRevisao> = buildList {
        if (exemplo(s)) add(FormatoRevisao.PREVER)
        if (Album.especies.any { it.skill == s }) add(FormatoRevisao.BUG)
        if (Robo.de(s) != null) add(FormatoRevisao.ROBO)
        if (Episodes.clipes.any { it.skill == s }) add(FormatoRevisao.CLIPE)
        if (Episodes.all.any { it.skill == s && it.temporada == Temporada.PEGADINHA }) add(FormatoRevisao.PEGADINHA)
        add(FormatoRevisao.JOGO)
    }

    /** O formato da próxima revisão: o nunca usado primeiro; depois o usado há mais tempo. */
    fun escolhe(l: Learner, s: Skill): FormatoRevisao =
        formatos(s).withIndex().minWith(compareBy<IndexedValue<FormatoRevisao>> { l.formatoUsadoEm(s, it.value.name) }.thenBy { it.index }).value

    /** O formato já foi aberto nesta rodada de revisão (depois da última vez que a memória mudou). */
    fun formatoFeito(l: Learner, s: Skill): Boolean {
        val ultima = Revisao.formatos(s).maxOfOrNull { l.formatoUsadoEm(s, it.name) } ?: 0L
        return ultima > 0 && ultima >= l.skill(s).memory.lastReviewMs
    }

    /** O clipe da vez: muda a cada revisão. */
    fun clipe(l: Learner, s: Skill): Episode? {
        val clipes = Episodes.clipes.filter { it.skill == s }
        return clipes.getOrNull(l.skill(s).memory.reviews % clipes.size.coerceAtLeast(1))
    }

    fun pegadinha(l: Learner, s: Skill): Episode? {
        val eps = Episodes.all.filter { it.skill == s && it.temporada == Temporada.PEGADINHA }
        return eps.getOrNull(l.skill(s).memory.reviews % eps.size.coerceAtLeast(1))
    }
}

/**
 * R3: uma ideia é uma frase que se julga sozinha: uma das frases de um Bug do Álbum.
 * Cada assunto tem de 3 a 6. A retomada revisa a ideia esquecida, não a aula toda.
 */
data class Ideia(val id: String, val skill: Skill, val especie: Especie, val afirma: Afirma)

object Ideias {
    val todas: List<Ideia> by lazy {
        Album.especies.flatMap { e -> e.testes.mapIndexed { k, a -> Ideia(id(e, k), e.skill, e, a) } }
    }

    fun id(e: Especie, k: Int) = "${e.id}#$k"

    fun de(s: Skill): List<Ideia> = todas.filter { it.skill == s }

    /** O assunto já foi estudado: aula vista ou jogo feito. */
    fun estudado(l: Learner, s: Skill) = s.name in l.state.episodesSeen || l.skill(s).attempts > 0

    fun retencao(l: Learner, i: Ideia, nowMs: Long): Double {
        val m: Memory = l.memoriaIdeia(i.id, i.skill)
        return if (m.reviews == 0) 0.5 else m.retrievability(nowMs)
    }

    /** As [n] ideias mais esquecidas dos assuntos estudados, uma por assunto enquanto der. */
    fun maisFracas(l: Learner, nowMs: Long, n: Int = 3): List<Ideia> {
        val candidatas = todas.filter { estudado(l, it.skill) }.sortedWith(compareBy<Ideia> { retencao(l, it, nowMs) }.thenBy { it.id })
        val umaPorAssunto = candidatas.distinctBy { it.skill }.take(n)
        return (umaPorAssunto + candidatas.filter { it !in umaPorAssunto }).take(n)
    }
}

/** R1: o que aparece quando a pessoa volta depois de 2 dias ou mais. */
data class Retomada(val ultimos: List<Skill>, val itens: List<Ideia>)

object Retomadas {
    const val DIAS = 2.0

    /** Null quando não é hora: primeira visita, volta no mesmo dia, ou nada estudado ainda. */
    fun monta(l: Learner, nowMs: Long): Retomada? {
        val longe = l.diasLonge(nowMs) ?: return null
        if (longe < DIAS) return null
        val itens = Ideias.maisFracas(l, nowMs)
        if (itens.isEmpty()) return null
        return Retomada(ultimos(l), itens)
    }

    /** Os dois últimos assuntos em que a pessoa mexeu: jogos e aulas. */
    fun ultimos(l: Learner): List<Skill> {
        val nomes = (l.state.history.map { it.skill } + l.state.episodesSeen.filter { n -> Skill.entries.any { it.name == n } }).asReversed()
        return nomes.distinct().mapNotNull { n -> Skill.entries.firstOrNull { it.name == n } }.take(2)
    }

    /** A frase de abertura do Byte. */
    fun abertura(r: Retomada): String = when (r.ultimos.size) {
        0 -> tx("retomada_abre_nada")
        1 -> tx("retomada_abre_um", r.ultimos[0].title)
        else -> tx("retomada_abre_dois", r.ultimos[0].title, r.ultimos[1].title)
    }
}

/**
 * R4: o texto do lembrete muda de um dia para o outro.
 * Sempre cita o assunto mais esquecido; o molde gira pelo dia do ano.
 */
object Lembretes {
    const val MOLDES = 5

    /** O nome do canal de notificação do Android. */
    val canal: String get() = tx("lembrete_canal")

    private fun molde(i: Int, assunto: String) = when (i) {
        0 -> tx("lembrete_1", assunto)
        1 -> tx("lembrete_2", assunto)
        2 -> tx("lembrete_3", assunto)
        3 -> tx("lembrete_4", assunto)
        else -> tx("lembrete_5", assunto)
    }

    /** Null quando não há revisão pendente: nesse dia o celular fica quieto. */
    fun texto(l: Learner, nowMs: Long): String? {
        val pendente = Skill.entries.filter { l.skill(it).memory.isDue(nowMs) }
            .minWithOrNull(compareBy<Skill>({ !l.skill(it).memory.reaprender }, { l.skill(it).memory.retrievability(nowMs) })) ?: return null
        val dia = (nowMs / 86_400_000L).toInt()
        return molde(dia % MOLDES, pendente.title)
    }
}
