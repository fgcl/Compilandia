package com.compilandia.engine

/*
 * Vivacidade num TAC com rótulos e desvios (o que o MiniLang.tac gera), pedida pelo revisor professor do bloco 5.
 * Num bloco sem desvio, uma passada de baixo para cima basta. Com laço, o "depois" do goto é lá em cima,
 * e o in do topo ainda não está pronto na primeira passada. A análise repete as passadas até nada mudar:
 *   out[i] = união dos in dos sucessores;  in[i] = use[i] ∪ (out[i] − def[i]).
 * Aqui toda variável conta (não só os temporários): a do laço é a que o exemplo quer mostrar.
 */
object VivacidadeComLaco {
    data class Resultado(val liveIn: List<Set<String>>, val liveOut: List<Set<String>>, /** Passadas que mudaram algo, sem a última, que só confere. */ val passadasQueMudam: Int)

    private fun nomes(s: String) = s.split(" ").filter { it.firstOrNull()?.isLetter() == true }
    private fun rotulo(l: String) = l.endsWith(":")

    fun use(l: String): Set<String> = when {
        rotulo(l) || l.startsWith("goto ") -> emptySet()
        l.startsWith("ifFalse ") || l.startsWith("if ") -> setOf(l.split(" ")[1])
        l.startsWith("print ") -> nomes(l.removePrefix("print ")).toSet()
        " = " in l -> nomes(l.substringAfter(" = ")).toSet()
        else -> emptySet()
    }

    fun def(l: String): Set<String> = if (" = " in l && !l.startsWith("if")) setOf(l.substringBefore(" = ")) else emptySet()

    fun sucessores(code: List<String>, i: Int): List<Int> {
        val l = code[i]
        fun alvo() = code.indexOf(l.substringAfter("goto ") + ":")
        return when {
            l.startsWith("goto ") -> listOf(alvo())
            l.startsWith("ifFalse ") || l.startsWith("if ") -> listOfNotNull(alvo(), (i + 1).takeIf { it < code.size })
            else -> listOfNotNull((i + 1).takeIf { it < code.size })
        }
    }

    fun analisa(code: List<String>, vivasNoFim: Set<String>): Resultado {
        val n = code.size
        val ins = MutableList(n) { emptySet<String>() }
        val outs = MutableList(n) { emptySet<String>() }
        var passadas = 0
        while (true) {
            var mudou = false
            for (i in n - 1 downTo 0) {
                val suc = sucessores(code, i)
                val out = if (suc.isEmpty()) vivasNoFim else suc.flatMap { ins[it] }.toSet()
                val inn = use(code[i]) + (out - def(code[i]))
                if (out != outs[i] || inn != ins[i]) mudou = true
                outs[i] = out; ins[i] = inn
            }
            if (!mudou) break
            passadas++
        }
        return Resultado(ins, outs, passadas)
    }

    /** Uma passada só, de baixo para cima, sem olhar o desvio para trás: o engano que o exemplo quer mostrar. */
    fun semVoltar(code: List<String>, vivasNoFim: Set<String>): List<Set<String>> {
        val outs = MutableList(code.size) { emptySet<String>() }
        var vivas = vivasNoFim
        for (i in code.indices.reversed()) {
            outs[i] = vivas
            vivas = use(code[i]) + (vivas - def(code[i]))
        }
        return outs
    }
}
