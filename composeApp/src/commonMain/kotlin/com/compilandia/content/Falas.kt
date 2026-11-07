package com.compilandia.content

import com.compilandia.content.Speaker.BYTE
import com.compilandia.content.Speaker.PROF
import com.compilandia.textos.tx

/** Falas soltas: as que não pertencem a um episódio. Tudo aqui passa pelo text_lint e vira áudio. */
object Falas {
    val toqueNoByte = listOf(
        Fala(BYTE, tx("falas_ei_isso_faz_cocegas_no")),
        Fala(BYTE, tx("falas_bora_consertar_esse_compilador")),
        Fala(BYTE, tx("falas_hoje_eu_acordei_com_bateria")),
        Fala(BYTE, tx("falas_tem_bug_roendo_gavetas_novo")),
    )
    val acerto = listOf(
        Fala(BYTE, tx("falas_boa_pegou_jeito")),
        Fala(BYTE, tx("falas_isso_tres_seguidas")),
        Fala(BYTE, tx("falas_mandou_bem_demais")),
        Fala(BYTE, tx("falas_bug_nem_viu_onde_veio")),
        Fala(BYTE, tx("falas_assim_compilador_volta_funcionar")),
    )
    val erro = listOf(
        Fala(BYTE, tx("falas_quase_olha_novo_com_calma")),
        Fala(BYTE, tx("falas_esse_pegou_errar_aqui_ajuda")),
        Fala(BYTE, tx("falas_sem_problema_tenta_outra_vez")),
    )
    val vitoria = listOf(
        Fala(BYTE, tx("falas_bug_eliminado_pode_abrir_caixa")),
        Fala(BYTE, tx("falas_acabou_com_ele_vem_ver")),
    )
    val derrota = listOf(
        Fala(BYTE, tx("falas_esse_bug_fugiu_mas_gente")),
    )
    val confianca = Fala(BYTE, tx("falas_antes_comecar_voce_sabe_isso"))
    /** R1: a abertura da retomada. */
    val retomada = Fala(BYTE, tx("retomada_fala"))
    /** A abertura de cada problema do Fazer com ajuda (A3). O resto dele é gerado na hora e fica em legenda. */
    val guiado = listOf(
        Fala(BYTE, tx("guiado_nivel_0")),
        Fala(BYTE, tx("guiado_nivel_1")),
        Fala(BYTE, tx("guiado_nivel_2")),
        Fala(BYTE, tx("guiado_nivel_3")),
    )
    val boas = listOf(
        Fala(BYTE, tx("falas_oi_eu_sou_byte_eu")),
        Fala(BYTE, tx("falas_mas_grande_compilador_quebrou_bugs")),
        Fala(PROF, tx("falas_quem_entende_compiladores_consegue_consertar")),
        Fala(PROF, tx("falas_como_posso_te_chamar")),
        Fala(BYTE, tx("falas_bora_oficina_esta_quase_vazia")),
    )

    /** Toda fala solta, para gerar os áudios. */
    fun todas(): List<Fala> = toqueNoByte + acerto + erro + vitoria + derrota + confianca + retomada + guiado + boas + comoJogar.values + Robos.falas()
}
