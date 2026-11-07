package com.compilandia.engine

import com.compilandia.textos.tx

/*
 * J7, o Carteiro do escopo: a pessoa põe as declarações nas salas (os blocos do programa em C).
 * O Byte entrega cada uso de variável: procura na sala do uso e, se não acha, sobe para a sala de fora,
 * até o arquivo inteiro. É a busca da tabela de símbolos em pilha de escopos.
 */
object Carteiro {
    /** Um bloco do programa. [cabeca] é a linha que abre o bloco ("int main() {"); vazio no arquivo inteiro. */
    data class Sala(val id: String, val cabeca: String, val pai: String?)

    /** Uma carta de declaração. [texto] é a linha em C: "int x;". */
    data class Carta(val id: String, val nome: String, val texto: String) {
        /** Sem o ponto e vírgula, para caber no meio de uma frase. */
        val curta: String get() = texto.removeSuffix(";")
    }

    /** Um uso de variável numa sala. [linha] é a linha em C onde ele aparece. */
    data class Uso(val id: String, val nome: String, val sala: String, val linha: String)

    class Fase(
        val id: String,
        val nome: String,
        val pedido: String,
        val salas: List<Sala>,
        val cartas: List<Carta>,
        val usos: List<Uso>,
        /** Para cada uso, a carta que ele deve achar; null quando o compilador deve acusar "não declarada". */
        val esperado: Map<String, String?>,
        /** Uma resposta certa: carta → sala. Os testes garantem que ela passa. */
        val solucao: Map<String, String>,
        val dicas: List<String>,
    ) {
        fun sala(id: String) = salas.first { it.id == id }
        fun carta(id: String) = cartas.first { it.id == id }
        fun filhas(id: String?) = salas.filter { it.pai == id }

        /** A sala e as de fora, da mais funda até o arquivo inteiro. */
        fun subida(sala: String): List<String> = generateSequence(sala) { sala(it).pai }.toList()

        /** Quantas salas a sala tem por fora: 0 no arquivo inteiro. */
        fun fundura(sala: String): Int = subida(sala).size - 1
    }

    val fases: List<Fase> by lazy {
        listOf(
            Fase(
                "escopo_global", tx("carteiro_fase_global"), tx("carteiro_pedido_global"),
                salas = listOf(
                    Sala("arq", "", null),
                    Sala("soma", "void soma() {", "arq"),
                    Sala("main", "int main() {", "arq"),
                ),
                cartas = listOf(Carta("total", "total", "int total = 0;")),
                usos = listOf(
                    Uso("u1", "total", "soma", "total = total + 1;"),
                    Uso("u2", "total", "main", "printf(\"%d\", total);"),
                ),
                esperado = mapOf("u1" to "total", "u2" to "total"),
                solucao = mapOf("total" to "arq"),
                dicas = listOf(tx("carteiro_dica_global_1"), tx("carteiro_dica_global_2")),
            ),
            Fase(
                "escopo_sombra", tx("carteiro_fase_sombra"), tx("carteiro_pedido_sombra"),
                salas = listOf(
                    Sala("arq", "", null),
                    Sala("main", "int main() {", "arq"),
                    Sala("if", "if (ok) {", "main"),
                ),
                cartas = listOf(Carta("xi", "x", "int x = 1;"), Carta("xf", "x", "float x = 2.5;")),
                usos = listOf(
                    Uso("u1", "x", "main", "printf(\"%d\", x);"),
                    Uso("u2", "x", "if", "printf(\"%f\", x);"),
                ),
                esperado = mapOf("u1" to "xi", "u2" to "xf"),
                solucao = mapOf("xi" to "main", "xf" to "if"),
                dicas = listOf(tx("carteiro_dica_sombra_1"), tx("carteiro_dica_sombra_2")),
            ),
            Fase(
                "escopo_irmaos", tx("carteiro_fase_irmaos"), tx("carteiro_pedido_irmaos"),
                salas = listOf(
                    Sala("arq", "", null),
                    Sala("main", "int main() {", "arq"),
                    Sala("if", "if (ok) {", "main"),
                    Sala("else", "else {", "main"),
                ),
                cartas = listOf(Carta("i", "i", "int i = 0;"), Carta("j", "j", "int j = 5;")),
                usos = listOf(
                    Uso("u1", "i", "if", "i = i + 1;"),
                    Uso("u2", "j", "else", "j = j - 1;"),
                    Uso("u3", "j", "main", "printf(\"%d\", j);"),
                    Uso("u4", "i", "else", "i = 2;"),
                ),
                esperado = mapOf("u1" to "i", "u2" to "j", "u3" to "j", "u4" to null),
                solucao = mapOf("i" to "if", "j" to "main"),
                dicas = listOf(tx("carteiro_dica_irmaos_1"), tx("carteiro_dica_irmaos_2")),
            ),
            Fase(
                "escopo_laco", tx("carteiro_fase_laco"), tx("carteiro_pedido_laco"),
                salas = listOf(
                    Sala("arq", "", null),
                    Sala("f", "void conta() {", "arq"),
                    Sala("for", "for (...) {", "f"),
                    Sala("main", "int main() {", "arq"),
                ),
                cartas = listOf(
                    Carta("ni", "n", "int n = 3;"),
                    Carta("nf", "n", "float n = 0.5;"),
                    Carta("i", "i", "int i;"),
                    Carta("s", "soma", "int soma = 0;"),
                ),
                usos = listOf(
                    Uso("u1", "i", "for", "soma = soma + i;"),
                    Uso("u2", "soma", "for", "soma = soma + i;"),
                    Uso("u3", "n", "for", "printf(\"%f\", n);"),
                    Uso("u4", "soma", "f", "printf(\"%d\", soma);"),
                    Uso("u5", "n", "main", "printf(\"%d\", n);"),
                    Uso("u6", "i", "main", "i = 0;"),
                ),
                esperado = mapOf("u1" to "i", "u2" to "s", "u3" to "nf", "u4" to "s", "u5" to "ni", "u6" to null),
                solucao = mapOf("ni" to "arq", "nf" to "f", "i" to "for", "s" to "f"),
                dicas = listOf(tx("carteiro_dica_laco_1"), tx("carteiro_dica_laco_2")),
            ),
        )
    }

    /** Um quadro da entrega: o Byte numa sala, com a carta do uso na mão. */
    data class Quadro(val sala: String, val uso: String, val achou: String?, val nota: String)

    data class Entrega(val uso: Uso, val quadros: List<Quadro>, val achou: String?)

    /**
     * O Byte leva a carta de [u]: olha a sala do uso e sobe até achar uma declaração com o mesmo nome.
     * [montagem] diz em que sala está cada carta; carta fora da montagem ficou na mão.
     */
    fun entrega(f: Fase, montagem: Map<String, String>, u: Uso): Entrega {
        val q = mutableListOf<Quadro>()
        val subida = f.subida(u.sala)
        for ((k, s) in subida.withIndex()) {
            val aqui = f.cartas.firstOrNull { it.nome == u.nome && montagem[it.id] == s }
            if (aqui != null) {
                q += Quadro(s, u.id, aqui.id, tx("carteiro_nota_achou", u.nome, nomeSala(f, s), aqui.curta))
                return Entrega(u, q, aqui.id)
            }
            val nota = if (k == subida.lastIndex) tx("carteiro_nota_nao_declarada", u.nome)
            else tx("carteiro_nota_sobe", nomeSala(f, s), u.nome, nomeSala(f, subida[k + 1]))
            q += Quadro(s, u.id, null, nota)
        }
        return Entrega(u, q, null)
    }

    /** O nome da sala para a fala: a primeira palavra da cabeça, ou "o arquivo". */
    fun nomeSala(f: Fase, id: String): String {
        val c = f.sala(id).cabeca
        if (c.isEmpty()) return tx("carteiro_sala_arquivo")
        val semChave = c.removeSuffix("{").trim()
        return if ("(" in semChave && !semChave.startsWith("if") && !semChave.startsWith("for")) semChave.substringBefore("(").substringAfterLast(" ") + "()"
        else semChave.substringBefore(" (").substringBefore("(").trim()
    }

    /** O que aparece na lista de testes: a declaração achada ou o erro. */
    private fun mostra(f: Fase, carta: String?) = if (carta == null) tx("carteiro_erro_nao_declarada") else f.carta(carta).texto

    /** Duas cartas com o mesmo nome na mesma sala: o compilador acusa antes de rodar. */
    fun redeclarada(f: Fase, montagem: Map<String, String>): Pair<String, String>? {
        for (s in f.salas) {
            val nomes = f.cartas.filter { montagem[it.id] == s.id }.groupBy { it.nome }
            nomes.entries.firstOrNull { it.value.size > 1 }?.let { return it.key to s.id }
        }
        return null
    }

    fun confere(f: Fase, montagem: Map<String, String>): Veredito {
        if (montagem.isEmpty()) return Veredito(emptyList(), -1, tx("carteiro_mesa_vazia"))
        redeclarada(f, montagem)?.let { (nome, sala) ->
            return Veredito(emptyList(), -1, tx("carteiro_redeclarada", nome, nomeSala(f, sala)))
        }
        val testes = f.usos.map { u ->
            val e = entrega(f, montagem, u)
            val certo = f.esperado[u.id]
            ResultadoTeste(tx("carteiro_teste", u.nome, nomeSala(f, u.sala)), e.achou == certo, mostra(f, e.achou), mostra(f, certo))
        }
        val falha = testes.indexOfFirst { !it.passou }.takeIf { it >= 0 }
        val frase = if (falha == null) tx("bancada_passou", testes.size) else {
            val u = f.usos[falha]
            val achou = entrega(f, montagem, u).achou
            val certo = f.esperado[u.id]
            when {
                achou == null -> tx("carteiro_falha_nao_achou", u.nome, nomeSala(f, u.sala), f.carta(certo!!).curta)
                certo == null -> tx("carteiro_falha_devia_errar", u.nome, nomeSala(f, u.sala), f.carta(achou).curta, nomeSala(f, montagem.getValue(achou)))
                else -> tx("carteiro_falha_errada", u.nome, nomeSala(f, u.sala), f.carta(achou).curta, f.carta(certo).curta)
            }
        }
        return Veredito(testes, falha, frase)
    }

    /** A terceira dica: a primeira carta fora da sala da resposta, quando isso explica a falha. */
    fun ondeOlhar(f: Fase, montagem: Map<String, String>): String? {
        val v = confere(f, montagem)
        val u = v.falha?.takeIf { it >= 0 }?.let { f.usos[it] }
        val alvo = u?.let { f.esperado[it.id] }
        if (alvo != null) return tx("carteiro_onde_olhar", f.carta(alvo).curta, nomeSala(f, u.sala))
        // O uso devia dar erro: a carta com esse nome está alta demais.
        if (u != null) {
            val culpada = f.cartas.firstOrNull { it.nome == u.nome && montagem[it.id] in f.subida(u.sala) } ?: return null
            return tx("carteiro_onde_olhar_alta", culpada.curta, nomeSala(f, u.sala))
        }
        return null
    }

    /** O programa em C que sai da montagem, linha por linha, com a fundura de cada linha. */
    data class Linha(val texto: String, val fundura: Int, val sala: String, val uso: String? = null, val carta: String? = null)

    fun programa(f: Fase, montagem: Map<String, String>): List<Linha> = buildList {
        fun bloco(s: Sala, d: Int) {
            val dentro = if (s.cabeca.isEmpty()) d else d + 1
            if (s.cabeca.isNotEmpty()) add(Linha(s.cabeca, d, s.id))
            for (c in f.cartas.filter { montagem[it.id] == s.id }) add(Linha(c.texto, dentro, s.id, carta = c.id))
            // Usos da mesma linha (soma = soma + i) aparecem uma vez só.
            for ((linha, us) in f.usos.filter { it.sala == s.id }.groupBy { it.linha }) add(Linha(linha, dentro, s.id, uso = us.first().id))
            for (filha in f.filhas(s.id)) bloco(filha, dentro)
            if (s.cabeca.isNotEmpty()) add(Linha("}", d, s.id))
        }
        for (s in f.filhas(null)) bloco(s, 0)
    }
}
