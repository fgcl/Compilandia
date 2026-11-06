// Gera os mapas de textos a partir de composeApp/src/commonMain/textos/<idioma>/*.xml.
// Usado pelo composeApp e pelo verify. O código lê os textos com tx("chave").
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

val pastaTextos = file("../composeApp/src/commonMain/textos")
val saidaTextos = layout.buildDirectory.dir("generated/textos/kotlin")

/** Lê um strings.xml: chave → (texto, traduzível). Desfaz \\n, \\t e \\\\. */
fun lerTextos(arq: File): List<Triple<String, String, Boolean>> {
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(arq)
    val nos = doc.documentElement.getElementsByTagName("string")
    return (0 until nos.length).map { i ->
        val e = nos.item(i) as Element
        val bruto = e.textContent
        val sb = StringBuilder()
        var k = 0
        while (k < bruto.length) {
            val c = bruto[k]
            if (c == '\\' && k + 1 < bruto.length) {
                when (bruto[k + 1]) {
                    'n' -> sb.append('\n'); 't' -> sb.append('\t'); '\\' -> sb.append('\\')
                    else -> { sb.append(c); sb.append(bruto[k + 1]) }
                }
                k += 2
            } else { sb.append(c); k++ }
        }
        Triple(e.getAttribute("name"), sb.toString(), e.getAttribute("translatable") != "false")
    }
}

fun literalKotlin(s: String): String {
    val sb = StringBuilder("\"")
    for (c in s) when (c) {
        '\\' -> sb.append("\\\\"); '"' -> sb.append("\\\""); '$' -> sb.append("\\$")
        '\n' -> sb.append("\\n"); '\t' -> sb.append("\\t"); '\r' -> sb.append("\\r")
        else -> sb.append(c)
    }
    return sb.append('"').toString()
}

val marcador = Regex("%(\\d+)\\\$s")

tasks.register("gerarTextos") {
    group = "build"
    description = "Gera os mapas Kotlin dos textos a partir dos strings.xml."
    inputs.dir(pastaTextos)
    outputs.dir(saidaTextos)
    doLast {
        val pt = linkedMapOf<String, String>()
        val naoTraduz = mutableSetOf<String>()
        for (arq in pastaTextos.resolve("pt").listFiles { f -> f.extension == "xml" }.orEmpty().sortedBy { it.name }) {
            for ((k, v, t) in lerTextos(arq)) {
                require(k !in pt) { "Chave repetida nos textos em português: $k (${arq.name})" }
                pt[k] = v
                if (!t) naoTraduz += k
            }
        }
        val en = linkedMapOf<String, String>()
        for (arq in pastaTextos.resolve("en").listFiles { f -> f.extension == "xml" }.orEmpty().sortedBy { it.name }) {
            for ((k, v, _) in lerTextos(arq)) {
                require(k in pt) { "A chave $k existe em inglês (${arq.name}) mas não em português." }
                require(k !in en) { "Chave repetida nos textos em inglês: $k" }
                val a = marcador.findAll(pt.getValue(k)).map { it.value }.toSortedSet()
                val b = marcador.findAll(v).map { it.value }.toSortedSet()
                require(a == b) { "Os marcadores de $k não batem: português $a, inglês $b." }
                en[k] = v
            }
        }
        val dir = saidaTextos.get().asFile.resolve("com/compilandia/textos")
        dir.deleteRecursively(); dir.mkdirs()
        fun objeto(nome: String, mapa: Map<String, String>) {
            val sb = StringBuilder("// Gerado por gradle/textos.gradle.kts a partir de textos/*.xml. Não edite.\npackage com.compilandia.textos\n\n")
            sb.append("internal object $nome {\n    val total = ${mapa.size}\n")
            val partes = mapa.entries.chunked(700)
            sb.append("    fun encher(m: HashMap<String, String>) {\n")
            partes.indices.forEach { sb.append("        p$it(m)\n") }
            sb.append("    }\n")
            partes.forEachIndexed { i, parte ->
                sb.append("    private fun p$i(m: HashMap<String, String>) {\n")
                for ((k, v) in parte) sb.append("        m[\"").append(k).append("\"] = ").append(literalKotlin(v)).append('\n')
                sb.append("    }\n")
            }
            sb.append("}\n")
            dir.resolve("$nome.kt").writeText(sb.toString())
        }
        objeto("TextosPt", pt)
        objeto("TextosEn", en)
        dir.resolve("TextosInfo.kt").writeText(
            "// Gerado. Não edite.\npackage com.compilandia.textos\n\ninternal object TextosInfo {\n    val naoTraduz: Set<String> = setOf(" +
                naoTraduz.joinToString(", ") { literalKotlin(it) } + ")\n}\n",
        )
        logger.lifecycle("Textos: ${pt.size} em português, ${en.size} em inglês.")
    }
}
