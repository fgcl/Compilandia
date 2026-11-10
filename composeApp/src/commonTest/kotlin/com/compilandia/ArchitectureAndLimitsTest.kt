package com.compilandia

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hard Gate de Arquitetura, SOLID e Limites Estruturais.
 * Executa em 'verify' e em 'desktopTest'.
 * Falha o build se:
 * 1. Módulos puros (engine, learning, content, tutor) importarem UI/Compose/Android.
 * 2. Engine importar camadas de nível superior (content, learning, tutor).
 * 3. Qualquer arquivo Kotlin exceder 400 linhas (sem supressão temporária).
 * 4. Qualquer função exceder 30 linhas de código lógico.
 */
class ArchitectureAndLimitsTest {
    private val rootCommon: File = listOf(
        File("src/commonMain/kotlin/com/compilandia"),
        File("../composeApp/src/commonMain/kotlin/com/compilandia"),
        File("composeApp/src/commonMain/kotlin/com/compilandia")
    ).firstOrNull { it.exists() } ?: File("src/commonMain/kotlin/com/compilandia")

    companion object {
        const val MAX_FILE_LINES = 400
        const val MAX_FUNCTION_LINES = 30
    }

    @Test
    fun camadasPurasNaoImportamUiNemCompose() {
        val pureDirs = listOf("engine", "learning", "content", "tutor")
        val forbiddenImports = listOf(
            "com.compilandia.ui",
            "androidx.compose",
            "android."
        )

        val violations = mutableListOf<String>()

        for (dir in pureDirs) {
            val targetDir = File(rootCommon, dir)
            if (!targetDir.exists()) continue

            targetDir.walkTopDown()
                .filter { it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { idx, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import ")) {
                            for (forbidden in forbiddenImports) {
                                if (trimmed.contains(forbidden)) {
                                    violations += "${file.name}:${idx + 1} -> importa $forbidden proibido na camada $dir"
                                }
                            }
                        }
                    }
                }
        }

        assertTrue(
            violations.isEmpty(),
            "VIOLAÇÃO DE ARQUITETURA: Domínio puro violando isolamento e acoplando em UI/Compose:\n" +
                violations.joinToString("\n")
        )
    }

    @Test
    fun engineNaoDependeDeCamadasSuperiores() {
        val engineDir = File(rootCommon, "engine")
        val forbiddenInEngine = listOf(
            "com.compilandia.learning",
            "com.compilandia.content",
            "com.compilandia.tutor"
        )

        val violations = mutableListOf<String>()
        if (engineDir.exists()) {
            engineDir.walkTopDown()
                .filter { it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { idx, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import ")) {
                            for (forbidden in forbiddenInEngine) {
                                if (trimmed.contains(forbidden)) {
                                    violations += "${file.name}:${idx + 1} -> importa $forbidden proibido no core engine"
                                }
                            }
                        }
                    }
                }
        }

        assertTrue(
            violations.isEmpty(),
            "VIOLAÇÃO DE ARQUITETURA: Core engine dependendo de camadas externas:\n" +
                violations.joinToString("\n")
        )
    }

    @Test
    fun todosOsArquivosCumpremTetoDe400LinhasSemSupressao() {
        val violations = mutableListOf<Pair<String, Int>>()

        rootCommon.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                val lineCount = file.readLines().size
                if (lineCount > MAX_FILE_LINES) {
                    violations += Pair(file.relativeTo(rootCommon).path, lineCount)
                }
            }

        violations.sortByDescending { it.second }

        val relatorio = violations.joinToString("\n") { (caminho, linhas) ->
            "- $caminho ($linhas linhas > teto de $MAX_FILE_LINES). Excesso: +${linhas - MAX_FILE_LINES} linhas."
        }

        assertTrue(
            violations.isEmpty(),
            "HARD GATE DE TAMANHO DE ARQUIVO VIOLADO (${violations.size} arquivos > $MAX_FILE_LINES linhas):\n" +
                "Decomponha os arquivos acima extraindo classes e funções para manter arquivos coesos (< 400 linhas):\n$relatorio"
        )
    }

    @Test
    fun funcoesNaoExcedemTetoDe30LinhasDeLogica() {
        val funStartRegex = Regex("""^\s*(?:(?:override|open|private|protected|public|internal|inline|suspend)\s+)*fun\s+([a-zA-Z0-9_]+)""")
        val violations = mutableListOf<String>()

        rootCommon.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                var currentFun: String? = null
                var funBodyLineCount = 0
                var braceDepth = 0

                lines.forEachIndexed { idx, rawLine ->
                    val trimmed = rawLine.trim()
                    val match = funStartRegex.find(rawLine)

                    if (match != null && braceDepth == 0) {
                        currentFun = match.groupValues[1]
                        funBodyLineCount = 0
                    }

                    if (currentFun != null) {
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                            funBodyLineCount++
                        }

                        braceDepth += rawLine.count { it == '{' }
                        braceDepth -= rawLine.count { it == '}' }

                        if (braceDepth <= 0 && rawLine.contains("}")) {
                            if (funBodyLineCount > MAX_FUNCTION_LINES) {
                                violations += "${file.name}:${idx + 1} -> fun $currentFun possui $funBodyLineCount linhas de código (teto: $MAX_FUNCTION_LINES)"
                            }
                            currentFun = null
                            braceDepth = 0
                        }
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "HARD GATE DE FUNÇÕES LONGAS VIOLADO (${violations.size} funções > $MAX_FUNCTION_LINES linhas):\n" +
                violations.take(20).joinToString("\n") +
                if (violations.size > 20) "\n... e mais ${violations.size - 20} funções." else ""
        )
    }
}

