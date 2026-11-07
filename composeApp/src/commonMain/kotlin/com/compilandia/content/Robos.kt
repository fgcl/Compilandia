package com.compilandia.content

import com.compilandia.content.Speaker.BYTE
import com.compilandia.engine.Carteiro
import com.compilandia.engine.Fabrica
import com.compilandia.engine.Guindaste
import com.compilandia.engine.RoboMips
import com.compilandia.engine.RoboPilha
import com.compilandia.engine.Trilho
import com.compilandia.learning.Learner
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/**
 * J1: os jogos de montar da Bancada do Byte. Cada um mora no assunto que ele exercita
 * e entra na trilha desse assunto como a etapa "Programar o Byte".
 */
enum class Robo(val skill: Skill) {
    TRILHO(Skill.REGEX_DFA),
    /** J6: a fábrica de frases, no Vale das Gramáticas. */
    FABRICA(Skill.CFG_DERIVATION),
    /** J4: o Byte com a pilha, no Templo Top-Down. */
    PILHA(Skill.LL1_TABLE),
    /** J5: o guindaste empilha-reduz, na Fortaleza Bottom-Up. */
    GUINDASTE(Skill.SHIFT_REDUCE),
    /** J7: o carteiro do escopo, na Cidade Semântica. */
    CARTEIRO(Skill.SCOPE_SYMBOLS),
    MIPS(Skill.CODEGEN_MIPS);

    val titulo: String get() = when (this) {
        TRILHO -> tx("robo_trilho_titulo"); FABRICA -> tx("robo_fabrica_titulo"); PILHA -> tx("robo_pilha_titulo")
        GUINDASTE -> tx("robo_guindaste_titulo"); CARTEIRO -> tx("robo_carteiro_titulo"); MIPS -> tx("robo_mips_titulo")
    }
    val subtitulo: String get() = when (this) {
        TRILHO -> tx("robo_trilho_sub"); FABRICA -> tx("robo_fabrica_sub"); PILHA -> tx("robo_pilha_sub")
        GUINDASTE -> tx("robo_guindaste_sub"); CARTEIRO -> tx("robo_carteiro_sub"); MIPS -> tx("robo_mips_sub")
    }

    /** id, nome e pedido de cada fase. */
    private fun fase(i: Int): Triple<String, String, String> = when (this) {
        TRILHO -> Trilho.fases[i].let { Triple(it.id, it.nome, it.pedido) }
        FABRICA -> Fabrica.fases[i].let { Triple(it.id, it.nome, it.pedido) }
        PILHA -> RoboPilha.fases[i].let { Triple(it.id, it.nome, it.pedido) }
        GUINDASTE -> Guindaste.fases[i].let { Triple(it.id, it.nome, it.pedido) }
        CARTEIRO -> Carteiro.fases[i].let { Triple(it.id, it.nome, it.pedido) }
        MIPS -> RoboMips.fases[i].let { Triple(it.id, it.nome, it.pedido) }
    }

    val fases: Int get() = when (this) {
        TRILHO -> Trilho.fases.size; FABRICA -> Fabrica.fases.size; PILHA -> RoboPilha.fases.size
        GUINDASTE -> Guindaste.fases.size; CARTEIRO -> Carteiro.fases.size; MIPS -> RoboMips.fases.size
    }

    fun idFase(i: Int): String = fase(i).first
    fun nomeFase(i: Int): String = fase(i).second
    fun pedido(i: Int): String = fase(i).third

    companion object {
        fun de(s: Skill): Robo? = entries.firstOrNull { it.skill == s }
    }
}

/** O progresso nas fases fica nas marcas de etapa do assunto, como as outras etapas da trilha. */
object Robos {
    fun chave(r: Robo, fase: Int) = "ROBO:${r.idFase(fase)}"

    fun feita(l: Learner, r: Robo, fase: Int): Boolean = l.etapa(r.skill, chave(r, fase)) == "feita"

    fun feitas(l: Learner, r: Robo): Int = (0 until r.fases).count { feita(l, r, it) }

    /** A fase que abre quando a pessoa entra: a primeira por fazer, ou a primeira de todas. */
    fun primeiraAberta(l: Learner, r: Robo): Int = (0 until r.fases).firstOrNull { !feita(l, r, it) } ?: 0

    /** Fase feita. Com todas feitas, a etapa da trilha fica feita também. */
    fun marca(l: Learner, r: Robo, fase: Int, nowMs: Long) {
        l.marcaEtapa(r.skill, chave(r, fase), feita = true, nowMs)
        if (feitas(l, r) == r.fases) l.marcaEtapa(r.skill, Etapa.PROGRAMAR.name, feita = true, nowMs)
    }

    /** O pedido de cada fase é falado pelo Byte. */
    fun falas(): List<Fala> = Robo.entries.flatMap { r -> (0 until r.fases).map { Fala(BYTE, r.pedido(it)) } }
}
