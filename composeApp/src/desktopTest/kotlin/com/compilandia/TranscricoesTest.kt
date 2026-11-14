package com.compilandia

import com.compilandia.content.Album
import com.compilandia.content.Beat
import com.compilandia.content.Box
import com.compilandia.content.Episode
import com.compilandia.content.Glossario
import com.compilandia.content.Library
import com.compilandia.content.Look
import com.compilandia.content.Passo
import com.compilandia.content.Temporada
import com.compilandia.content.Trilha
import com.compilandia.content.episodes.Aberturas
import com.compilandia.content.episodes.Episodes
import com.compilandia.learning.GameId
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * As transcrições que os revisores leem (docs/revisao/transcricoes): cada aula animada cena a cena,
 * as aulas de texto, a ficha, os Bugs, o glossário, a série Em C e o Guia do TP.
 * Sempre grava em build/transcricoes, o que também serve de teste. Para atualizar as de docs/:
 *   TRANSCRICOES=docs ./gradlew :composeApp:desktopTest --tests '*TranscricoesTest*'
 */
class TranscricoesTest {
    private val slug = mapOf(
        Region.PORTO to "porto", Region.FLORESTA_LEXICA to "floresta_lexica", Region.VALE_GRAMATICAS to "vale_gramaticas",
        Region.PONTE_TRANSFORMACAO to "ponte_transformacao", Region.TEMPLO_TOPDOWN to "templo_topdown",
        Region.FORTALEZA_BOTTOMUP to "fortaleza_bottomup", Region.OFICINA_BISON to "oficina_bison",
        Region.CIDADE_SEMANTICA to "cidade_semantica", Region.FORJA_CODIGO to "forja_codigo",
    )

    @Test fun geraTranscricoes() {
        val destino = if (System.getenv("TRANSCRICOES") == "docs") File("../docs/revisao/transcricoes") else File("build/transcricoes")
        destino.mkdirs()
        val arquivos = linkedMapOf<String, String>()
        arquivos["00_indice.md"] = indice()
        Region.entries.forEachIndexed { i, r ->
            val base = "regiao${i + 1}_${slug.getValue(r)}"
            arquivos["${base}_A_trilha.md"] = trilhaDaRegiao(r)
            arquivos["${base}_B_texto.md"] = textosDaRegiao(r)
        }
        arquivos["seu_compilador_emC_e_guiaTP.md"] = seuCompilador()
        for ((nome, texto) in arquivos) File(destino, nome).writeText(texto)
        assertTrue(arquivos.values.all { it.isNotBlank() })
    }

    private fun prereqs(s: Skill) = s.prereqs.joinToString(", ") { it.name }.ifEmpty { "nenhum" }

    private fun indice() = buildString {
        appendLine("# Índice")
        for (s in Skill.entries) appendLine("- ${s.region.title} / ${s.name}: ${s.title} (pré-requisitos: ${prereqs(s)})")
    }

    // ───────────── Parte A: a trilha ─────────────

    private fun trilhaDaRegiao(r: Region) = buildString {
        appendLine("# Região ${r.ordinal + 1}: ${r.title} — ${r.tagline}")
        appendLine("(Parte A: trilha de cada assunto — abertura, aula animada, pegadinhas, práticas, clipe, Bugs do álbum, ficha)")
        for (s in Skill.entries.filter { it.region == r }) {
            appendLine(); appendLine()
            appendLine("## ASSUNTO ${s.name}: ${s.title}")
            appendLine("Pré-requisitos no jogo: ${s.prereqs.joinToString(", ") { it.title }.ifEmpty { "nenhum" }}")
            appendLine("Trilha (ordem das etapas): " + Trilha.passos(s).joinToString(" → ") { p ->
                if (p.episodio != null) "${p.titulo} «${p.subtitulo}»" else p.titulo
            })
            appendLine("Jogos que treinam: " + GameId.entries.filter { s in it.skills }.joinToString(", ") { "${it.title} (${it.mechanic})" })
            appendLine("Tem \"Fazer com ajuda\" (4 problemas gerados, ajuda decrescente): ${com.compilandia.content.Guiado.disponivel(s)} ; " +
                "robô da Bancada: ${com.compilandia.content.Robo.de(s)?.titulo ?: "não"}")
            abertura(s)
            for (e in Episodes.all.filter { it.skill == s && it.temporada != Temporada.EM_C }) episodio(e)
            for (e in Episodes.clipes.filter { it.skill == s }) episodio(e)
            bugs(s); ficha(s); glossario(s)
        }
    }

    private fun StringBuilder.abertura(s: Skill) {
        val a = Aberturas.of(s) ?: return
        appendLine(); appendLine("#### ABERTURA COM O BUG (10 s, antes da aula)")
        appendLine("Manchete: ${a.manchete}")
        appendLine("Na tela: ${a.desafio}")
        appendLine("${a.fala.who.name}: \"${a.fala.text}\"")
        appendLine("Opções: " + a.opcoes.mapIndexed { i, o -> if (i == a.certa) "✔$o" else o }.joinToString(" | "))
        appendLine("Depois: \"${a.depois.text}\"")
    }

    private fun nomeDaTemporada(e: Episode) = when (e.temporada) {
        Temporada.BASICO -> "AULA ANIMADA BÁSICA"
        Temporada.PEGADINHA -> "PEGADINHA"
        Temporada.PRATICA -> "PRATICA"
        Temporada.CLIPE -> "CLIPE 30s"
        Temporada.TABELAS -> "DIAGRAMAS DE TABELA"
        Temporada.EM_C -> "EM_C / ${e.skill.name}"
    }

    private fun StringBuilder.episodio(e: Episode) {
        appendLine()
        val inicio = e.inicioDoExemplo?.let { " [1º exemplo começa na cena ${it + 1}]" }.orEmpty()
        appendLine("#### ${nomeDaTemporada(e)}: \"${e.title}\" (${e.passos.size} cenas, ~${e.segundos}s)$inicio")
        e.passos.forEachIndexed { i, p -> appendLine(); cena(i + 1, p) }
    }

    /** Uma peça do palco em texto: título com #, palavra grande entre !!, registro com as linhas, peça acesa entre *. */
    private fun peca(b: Box): String {
        val t = when (b.look) {
            Look.TITLE -> "# ${b.text}"
            Look.BANG -> "!!${b.text}!!"
            Look.RECORD -> "${b.text}{${b.rows.joinToString("; ")}}"
            else -> b.text
        }.replace("\n", " ")
        return if (b.hot) "*$t*" else t
    }

    private fun StringBuilder.cena(n: Int, p: Passo) {
        val pecas = p.shot.boxes.filter { it.text.isNotBlank() || it.rows.isNotEmpty() }
        appendLine("$n. TELA: " + pecas.joinToString(" ⏎ ") { peca(it) })
        appendLine("   ${p.fala.who.name}: \"${p.fala.text}\"")
        val ask = p.ask ?: return run { p.brinquedo?.let { appendLine("   BRINQUEDO: dica \"${it.dica.text}\" / acerto \"${it.acerto.text}\"") } }
        val porId = p.shot.boxes.associateBy { it.id }
        fun textos(ids: Set<String>) = ids.joinToString(" | ") { porId[it]?.let(::peca) ?: it }
        appendLine("   PERGUNTA (toque): certo = ${textos(ask.right)} ; errado = ${textos(ask.wrong)}")
        appendLine("   se errar: \"${ask.hint.text}\"")
        appendLine("   se acertar: \"${ask.got.text}\"")
    }

    private fun StringBuilder.bugs(s: Skill) {
        val especies = Album.especies.filter { it.skill == s }
        if (especies.isEmpty()) return
        appendLine(); appendLine("#### BUGS DO ÁLBUM (julgar verdade/mentira)")
        for (b in especies) {
            appendLine("- ${b.nome} — mentira: \"${b.mentira}\" / verdade: \"${b.verdade}\"")
            for (a in b.testes) appendLine("    · [${if (a.verdade) "V" else "F"}] ${a.texto} → ${a.porque}")
        }
    }

    private fun StringBuilder.ficha(s: Skill) {
        val c = Library.card(s) ?: return
        appendLine(); appendLine("#### FICHA DO ASSUNTO")
        appendLine("Em uma frase: ${c.oneLiner}")
        appendLine("Gancho: ${c.hook}")
        appendLine("Termos-chave: ${c.keyTerms.joinToString(", ")}")
        appendLine("Erros comuns: ${c.commonMistakes.joinToString(" // ")}")
        appendLine("Flashcards:")
        for ((q, r) in c.flashcards) appendLine("    · P: $q → R: $r")
    }

    private fun StringBuilder.glossario(s: Skill) {
        val termos = Glossario.termos.filter { it.skill == s }
        if (termos.isEmpty()) return
        appendLine(); appendLine("#### GLOSSÁRIO (balão ao tocar no termo)")
        for (t in termos) appendLine("- ${t.nome}: ${t.definicao}")
    }

    // ───────────── Parte B: as aulas de texto ─────────────

    private fun textosDaRegiao(r: Region) = buildString {
        appendLine("# Região ${r.ordinal + 1}: ${r.title} — aulas de texto \"Ler com calma\" (6 estilos por assunto)")
        for (s in Skill.entries.filter { it.region == r }) {
            appendLine(); appendLine()
            appendLine("## ASSUNTO ${s.name}: ${s.title}")
            for (style in Style.entries) {
                val l = Library.lesson(s, style) ?: continue
                appendLine(); appendLine("#### ESTILO ${style.name} (${style.title}): \"${l.title}\"")
                for (b in l.beats) beat(b)
            }
        }
    }

    private fun StringBuilder.beat(b: Beat) {
        when (b) {
            is Beat.Say -> appendLine("- ${b.who.name}: ${b.text}")
            is Beat.Text -> appendLine("- TEXTO: ${b.text}")
            is Beat.Code -> { appendLine("- CÓDIGO" + (if (b.caption.isNotBlank()) " (${b.caption})" else "") + ":"); appendLine("```"); appendLine(b.code); appendLine("```") }
            is Beat.Callout -> appendLine("- CAIXA [${b.kind.title}]: ${b.text}")
            is Beat.Predict -> {
                appendLine("- PREVEJA: ${b.question}")
                appendLine("    opções: " + b.options.mapIndexed { i, o -> if (i == b.correct) "✔$o" else o }.joinToString(" | "))
                appendLine("    explicação: ${b.explanation}")
            }
            is Beat.Reveal -> { appendLine("- REVELAR: ${b.prompt}"); appendLine("    → ${b.answer}") }
            is Beat.Steps -> { appendLine("- PASSOS (${b.title}):"); b.steps.forEach { appendLine("    · $it") } }
            is Beat.Visual -> appendLine(
                "- WIDGET ${b.kind.name}" + (if (b.grammar.isNotBlank()) " gramática=«${b.grammar.replace("\n", " ⏎ ")}»" else "") +
                    (if (b.input.isNotBlank()) " entrada=«${b.input}»" else "") + (if (b.caption.isNotBlank()) " legenda: ${b.caption}" else ""),
            )
        }
    }

    // ───────────── Seu compilador ─────────────

    private fun seuCompilador() = buildString {
        appendLine("# Série \"Seu compilador\" em C (aulas animadas EM_C, na ordem)")
        for (e in Episodes.all.filter { it.temporada == Temporada.EM_C }) episodio(e)
        appendLine(); appendLine()
        appendLine("# Guia do Trabalho Prático (capítulos de texto)")
        for (c in Library.guide) {
            appendLine(); appendLine("## Capítulo ${c.number}: ${c.title} [${c.skill.name}]")
            appendLine(c.summary); appendLine()
            for (b in c.beats) beat(b)
        }
    }
}
