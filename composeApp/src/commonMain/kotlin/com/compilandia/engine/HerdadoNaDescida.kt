package com.compilandia.engine

/*
 * O herdado como parâmetro da descida recursiva, e a ação no meio da regra (pedido do revisor professor do bloco 5).
 * Gramática sem recursão à esquerda:  D → T L ;    L → id R    R → , id R | ε
 * Esquema de tradução:  D → T { L.h = T.tipo } L ;   L → id { addtipo(id, L.h) } R   (R.h = L.h)
 * Cada não-terminal vira uma função. O sintetizado (T.tipo) volta no return; o herdado (L.h, R.h) vai como parâmetro.
 */
object HerdadoNaDescida {
    /** Onde a ação L.h = T.tipo fica na regra do D. */
    enum class Lugar { ANTES_DO_T, ENTRE_T_E_L, DEPOIS_DO_L }

    /** Uma chamada do rastro: a função, o herdado que ela recebeu e o id que ela pôs na tabela (se pôs). */
    data class Passo(val funcao: String, val herdado: String?, val id: String? = null)

    /** O rastro das chamadas e a tabela no fim: id → tipo (nulo quando o id entrou sem tipo). */
    data class Resultado(val rastro: List<Passo>, val tabela: Map<String, String?>)

    fun tokens(decl: String): List<String> = Regex("[A-Za-z_]\\w*|[,;]").findAll(decl).map { it.value }.toList()

    /** Roda a descida em [decl] (ex.: "float a, b;") com a ação do D no [lugar] pedido. */
    fun roda(decl: String, lugar: Lugar = Lugar.ENTRE_T_E_L): Resultado {
        val toks = tokens(decl)
        var pos = 0
        val rastro = mutableListOf<Passo>()
        val tabela = linkedMapOf<String, String?>()
        fun addtipo(funcao: String, h: String?) { val id = toks[pos++]; tabela[id] = h; rastro += Passo(funcao, h, id) }
        fun r(h: String?) {
            rastro += Passo("R", h)
            if (toks.getOrNull(pos) != ",") return
            pos++
            addtipo("R", h)
            r(h)
        }
        var h: String? = null                       // L.h: antes de a ação rodar, ainda não tem valor
        val tipoT = toks[pos++].also { rastro += Passo("T", null) }
        if (lugar == Lugar.ENTRE_T_E_L) h = tipoT
        rastro += Passo("L", h)
        addtipo("L", h)
        r(h)
        require(toks.getOrNull(pos) == ";") { "faltou o ; em $decl" }
        return Resultado(rastro, tabela)
    }
}
