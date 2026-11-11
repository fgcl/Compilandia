package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.engine.Production

/**
 * Fábrica de Gramáticas: cada regra é uma máquina numa grade vista de cima.
 *
 * A planta é livre. O jogador arrasta as máquinas e desenha as esteiras célula a célula.
 * Uma caixa (não terminal) ou peça (terminal) segue a esteira em que está e só entra
 * no bloco para onde a esteira aponta, como no Mindustry. O separador tem uma saída por regra
 * e carimba na caixa o número da máquina; o roteador recebe o que as máquinas soltam e manda
 * cada peça para a montadora e cada caixa para o separador da letra dela.
 * Se a esteira acaba, bate em outro bloco ou fecha um círculo, o item para ali.
 *
 * [planta] monta uma fábrica que já funciona; a corrida está em FabGramCorrida.kt.
 */
object FabGram {

    /** O desenho da máquina segue o papel da regra. */
    enum class Modelo(val w: Int, val h: Int) {
        /** ε ou regra de uma peça só. */
        COFRE(1, 1),
        /** Regra que só troca a letra: A -> B. */
        BRACO(1, 2),
        /** Regra com duas ou três peças. */
        PRENSA(2, 2),
        /** Regra longa, com quatro peças ou mais. */
        ARMARIO(2, 2),
    }

    /** O roteador é o sorter do Mindustry: recebe tudo e manda cada item pela esteira que leva ao destino dele. */
    enum class TipoBloco { FONTE, SEPARADOR, MAQUINA, MONTADORA, CESTO, ROTEADOR }

    enum class Dir(val dx: Int, val dy: Int) {
        DIREITA(1, 0), BAIXO(0, 1), ESQUERDA(-1, 0), CIMA(0, -1);

        /** Um quarto de volta no sentido do relógio. */
        fun gira(): Dir = entries[(ordinal + 1) % 4]

        companion object {
            fun de(dx: Int, dy: Int): Dir? = entries.firstOrNull { it.dx == dx && it.dy == dy }
        }
    }

    data class Cel(val x: Int, val y: Int) {
        operator fun plus(d: Dir) = Cel(x + d.dx, y + d.dy)
        operator fun minus(d: Dir) = Cel(x - d.dx, y - d.dy)
    }

    data class Bloco(
        val id: Int,
        val tipo: TipoBloco,
        val x: Int, val y: Int, val w: Int, val h: Int,
        val nt: String? = null,
        val prod: Production? = null,
        val modelo: Modelo? = null,
    ) {
        val cx: Float get() = x + w / 2f
        val cy: Float get() = y + h / 2f
        fun contem(c: Cel) = c.x in x until x + w && c.y in y until y + h

        /** A célula encosta num lado do bloco (sem contar a diagonal). */
        fun encosta(c: Cel): Boolean = !contem(c) && Dir.entries.any { contem(c + it) }

        fun celulas(): List<Cel> = (x until x + w).flatMap { cx -> (y until y + h).map { Cel(cx, it) } }
    }

    /** Um retângulo de células: [x0], [y0] inclusivos; [x1], [y1] exclusivos. */
    data class Area(val x0: Int, val y0: Int, val x1: Int, val y1: Int)

    /** Uma fábrica: os blocos e, em cada célula de esteira, para onde ela empurra. */
    class Planta(val g: Grammar, val blocos: List<Bloco>, val esteiras: Map<Cel, Dir>) {
        /** O retângulo que a fábrica ocupa: [x0], [y0] inclusivos; [x1], [y1] exclusivos. A grade não tem borda. */
        val x0: Int = minOf(blocos.minOf { it.x }, esteiras.keys.minOfOrNull { it.x } ?: 0)
        val y0: Int = minOf(blocos.minOf { it.y }, esteiras.keys.minOfOrNull { it.y } ?: 0)
        val x1: Int = maxOf(blocos.maxOf { it.x + it.w }, (esteiras.keys.maxOfOrNull { it.x } ?: 0) + 1)
        val y1: Int = maxOf(blocos.maxOf { it.y + it.h }, (esteiras.keys.maxOfOrNull { it.y } ?: 0) + 1)
        val largura: Int get() = x1 - x0
        val altura: Int get() = y1 - y0

        fun blocoEm(c: Cel): Bloco? = blocos.firstOrNull { it.contem(c) }
        fun maquinaDe(p: Production): Bloco = blocos.first { it.prod?.id == p.id }
        fun separadorDe(nt: String): Bloco = blocos.first { it.tipo == TipoBloco.SEPARADOR && it.nt == nt }
        val fonte: Bloco get() = blocos.first { it.tipo == TipoBloco.FONTE }
        val montadora: Bloco get() = blocos.first { it.tipo == TipoBloco.MONTADORA }
        val cesto: Bloco get() = blocos.first { it.tipo == TipoBloco.CESTO }

        /** A primeira esteira que sai de [b] (colada nele e empurrando para longe), ou nulo: bloco sem saída. */
        fun saidaDe(b: Bloco): Cel? = esteiras.entries
            .filter { (c, d) -> b.encosta(c) && b.contem(c - d) }
            .minWithOrNull(compareBy({ it.key.y }, { it.key.x }))?.key

        /** Move o bloco [id] para ([x], [y]). Esteira embaixo dele some. Nulo se sair de [area] ou cair em cima de outro bloco. */
        fun move(id: Int, x: Int, y: Int, area: Area? = null): Planta? {
            val b = blocos.firstOrNull { it.id == id } ?: return null
            val n = b.copy(x = x, y = y)
            if (area != null && (x < area.x0 || y < area.y0 || x + b.w > area.x1 || y + b.h > area.y1)) return null
            if (blocos.any { it.id != id && it.celulas().any(n::contem) }) return null
            return Planta(g, blocos.map { if (it.id == id) n else it }, esteiras.filterKeys { !n.contem(it) })
        }

        /** Põe esteira em [c]. Nulo se a célula é de um bloco. */
        fun comEsteira(c: Cel, d: Dir): Planta? {
            if (blocoEm(c) != null) return null
            return Planta(g, blocos, esteiras + (c to d))
        }

        fun semEsteira(c: Cel): Planta = if (c in esteiras) Planta(g, blocos, esteiras - c) else this

        fun gira(c: Cel): Planta = esteiras[c]?.let { Planta(g, blocos, esteiras + (c to it.gira())) } ?: this
    }

    fun modelo(p: Production, g: Grammar): Modelo = when {
        p.rhs.isEmpty() -> Modelo.COFRE
        p.rhs.size == 1 && g.isNonTerminal(p.rhs[0]) -> Modelo.BRACO
        p.rhs.size == 1 -> Modelo.COFRE
        p.rhs.size >= 4 -> Modelo.ARMARIO
        else -> Modelo.PRENSA
    }

    /** O número da máquina entre as regras da mesma letra (1, 2, ...); nulo quando a letra tem uma regra só. */
    fun numero(g: Grammar, p: Production): Int? = g.productionsOf(p.lhs).let { rs -> if (rs.size < 2) null else rs.indexOfFirst { it.id == p.id } + 1 }

    /** As letras na ordem em que a fábrica mostra: a inicial primeiro. */
    fun letras(g: Grammar): List<String> {
        val ordem = mutableListOf(g.start)
        for (p in g.productions) if (p.lhs !in ordem) ordem += p.lhs
        return ordem
    }

    /** Exemplos do menu. As gramáticas não se traduzem. */
    val exemplos: List<Pair<String, String>> = listOf(
        "S -> a S b | ε" to "a a b b",
        "S -> ( S ) S | ε" to "( ) ( )",
        "E -> E + T | T\nT -> id" to "id + id",
        "E -> E + E | id" to "id + id + id",
        "S -> if E then S else S | x\nE -> id" to "if id then x else x",
    )
}
