package com.compilandia.engine

/*
 * Tabela de símbolos com funções (pedido do revisor professor do bloco 5).
 * A função entra na tabela global com o tipo de retorno e os tipos dos parâmetros: é o que a chamada confere.
 * Os parâmetros e as variáveis locais entram na tabela da própria função, filha da global.
 * A busca começa na tabela de onde o nome é usado e sobe para o pai, como no escopo de blocos.
 */
object TabelaComFuncoes {
    const val GLOBAL = "global"

    enum class Categoria { FUNCAO, PARAMETRO, VARIAVEL }

    /** Uma linha da tabela. Na função, [tipo] é o retorno (nulo para void) e [parametros], os tipos que ela espera. */
    data class Entrada(
        val nome: String,
        val categoria: Categoria,
        val tipo: Type?,
        val parametros: List<Type> = emptyList(),
    )

    data class Funcao(
        val nome: String,
        val retorno: Type?,
        val parametros: List<Pair<String, Type>>,
        val locais: List<Pair<String, Type>>,
    )

    data class Escopo(val nome: String, val pai: String?, val entradas: List<Entrada>)

    fun monta(fs: List<Funcao>): List<Escopo> {
        val funcoes = fs.map { f -> Entrada(f.nome, Categoria.FUNCAO, f.retorno, f.parametros.map { it.second }) }
        val global = Escopo(GLOBAL, null, funcoes)
        val locais = fs.map { f ->
            val ps = f.parametros.map { (n, t) -> Entrada(n, Categoria.PARAMETRO, t) }
            Escopo(f.nome, GLOBAL, ps + f.locais.map { (n, t) -> Entrada(n, Categoria.VARIAVEL, t) })
        }
        return listOf(global) + locais
    }

    /** Procura [nome] a partir da tabela [de], subindo para o pai. Devolve a tabela onde achou e a entrada, ou nulo. */
    fun busca(t: List<Escopo>, de: String, nome: String): Pair<String, Entrada>? {
        var atual: String? = de
        while (atual != null) {
            val e = t.first { it.nome == atual }
            e.entradas.firstOrNull { it.nome == nome }?.let { return e.nome to it }
            atual = e.pai
        }
        return null
    }

    /** As variáveis e parâmetros que o código da tabela [de] enxerga, com o tipo de cada um. */
    fun visiveis(t: List<Escopo>, de: String): Map<String, Type> =
        generateSequence(t.first { it.nome == de }) { e -> e.pai?.let { p -> t.first { it.nome == p } } }
            .toList().reversed().flatMap { it.entradas }
            .filter { it.categoria != Categoria.FUNCAO }
            .associate { it.nome to requireNotNull(it.tipo) { "variável ${it.nome} sem tipo" } }

    /** Confere uma chamada feita dentro de [de]: a função existe, a quantidade bate e cada argumento cabe no parâmetro. */
    fun confereChamada(t: List<Escopo>, de: String, nome: String, argumentos: List<String>): TiposComandos.Veredito {
        val f = busca(t, de, nome)?.second
        if (f == null || f.categoria != Categoria.FUNCAO) return TiposComandos.Veredito.ERRO
        return TiposComandos.verifica(TiposComandos.Chamada(f.parametros, argumentos), visiveis(t, de))
    }
}
