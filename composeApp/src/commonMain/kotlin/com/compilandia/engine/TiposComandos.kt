package com.compilandia.engine

/*
 * Verificação de tipos nos comandos (pedido do revisor professor do bloco 5).
 * A expressão sobe o tipo pelo TypeChecker; o comando compara esse tipo com o que ele espera.
 * As regras são as do livro e as da Byteiro, "estilo Java":
 *  - atribuição: o tipo da expressão precisa caber na variável; int cabe em float (coerção), float não cabe em int;
 *  - condição do if e do while: precisa ser bool;
 *  - chamada: cada argumento cabe no parâmetro da mesma posição, e a quantidade bate;
 *  - return: a expressão cabe no tipo que a função promete.
 */
object TiposComandos {
    enum class Veredito { OK, COERCAO, ERRO }

    sealed interface Comando
    data class Atribuicao(val nome: String, val expr: String) : Comando
    data class Condicao(val expr: String) : Comando
    data class Chamada(val parametros: List<Type>, val argumentos: List<String>) : Comando
    data class Retorno(val tipoDaFuncao: Type, val expr: String) : Comando

    /** O tipo que a expressão sobe, com a tabela de símbolos [env]. */
    fun tipo(expr: String, env: Map<String, Type>): Type {
        val e = ExprParser.parse(expr)
        val valores = env.mapValues { (_, t) -> t to if (t == Type.BOOL) 1.0 else 3.0 }
        return TypeChecker.evaluate(e, valores).getValue(e.id).type
    }

    /** O valor de tipo [origem] pode ir para um lugar de tipo [destino]? */
    fun cabe(destino: Type, origem: Type): Veredito = when {
        destino == Type.ERROR || origem == Type.ERROR -> Veredito.ERRO
        destino == origem -> Veredito.OK
        destino == Type.FLOAT && origem == Type.INT -> Veredito.COERCAO
        else -> Veredito.ERRO
    }

    fun verifica(c: Comando, env: Map<String, Type>): Veredito = when (c) {
        is Atribuicao -> cabe(env[c.nome] ?: Type.ERROR, tipo(c.expr, env))
        is Condicao -> if (tipo(c.expr, env) == Type.BOOL) Veredito.OK else Veredito.ERRO
        is Chamada ->
            if (c.parametros.size != c.argumentos.size) Veredito.ERRO
            else c.parametros.zip(c.argumentos).map { (p, a) -> cabe(p, tipo(a, env)) }.maxBy { it.ordinal }
        is Retorno -> cabe(c.tipoDaFuncao, tipo(c.expr, env))
    }
}
