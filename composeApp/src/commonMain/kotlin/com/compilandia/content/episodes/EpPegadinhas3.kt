package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.learning.Skill

/** Pegadinhas de prova: semântica e geração de código. */
val pegadinhasFim by lazy {
    listOf(
        episodio(Skill.ATTRIBUTES_TYPES, "Sintetizado sobe, herdado desce", PEGADINHA) {
            val d = Box("d", "D", 180f, 86f, 70f, 40f, Look.NODE, Tint.ORANGE, size = 16f)
            val t = Box("t", "T: int", 90f, 180f, 100f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val l = Box("l", "L", 270f, 180f, 80f, 40f, Look.NODE, Tint.ORANGE, size = 16f)
            val a = Box("a", "a", 220f, 276f, 60f, 40f, Look.NODE, Tint.TEAL, size = 16f)
            val b = Box("b", "b", 320f, 276f, 60f, 40f, Look.NODE, Tint.TEAL, size = 16f)
            cena("Declaração: int a, b. O tipo está num galho, e os nomes estão no outro.") {
                add(titulo("int a, b", y = 30f))
                add(d, t, l, a, b)
                wire("d", "t", arrow = false); wire("d", "l", arrow = false); wire("l", "a", arrow = false); wire("l", "b", arrow = false)
            }
            cena("Em a mais b, o tipo sobe dos filhos para o pai. Isso é sintetizado.", who = PROF) {}
            pergunta(
                "Aqui é diferente. Para saber o tipo de a, o valor vem de qual nó? Toque.",
                setOf("t"), setOf("l", "b"),
                "O int está no galho do lado. Ele precisa atravessar.",
                "Isso. O int passa pelo pai e desce até a. Isso é herdado.",
            ) { hot() }
            cena("Herdado vem do pai ou de um irmão à esquerda. Por isso desce e anda de lado.") { hot("t", "l", "a", "b") }
            palavra("HERDADO", "S-atribuída usa só sintetizado. L-atribuída aceita herdado, da esquerda para a direita.", who = PROF, mood = EpMood.HAPPY)
        },

        episodio(Skill.SCOPE_SYMBOLS, "A sombra da variável", PEGADINHA) {
            cena("Dois x, um fora e um dentro. O de dentro faz sombra no de fora.") {
                add(titulo("Dois x"))
                add(codigo("c", listOf("int x = 1;", "{", "  int x = 2;", "  print(x);", "}", "print(x);"), 76f, x = 90f, passo = 32f, size = 16f))
            }
            pergunta(
                "O print de dentro enxerga qual x? Toque na declaração.",
                setOf("c2"), setOf("c0"),
                "A busca começa no bloco mais interno.",
                "Isso. Ele acha o x de dentro e para ali.",
            ) { hot("c3") }
            pergunta(
                "O bloco fechou. O print de fora enxerga qual x?",
                setOf("c0"), setOf("c2"),
                "Quando o bloco fecha, os nomes dele somem.",
                "Isso. Sobrou só o de fora, que nunca mudou.",
            ) { hot("c5") }
            cena("A pegadinha é achar que x igual a dois mudou o x de fora. É outra variável.", who = PROF) { hot("c2", "c0") }
            cena("Saída do programa: dois, depois um.", mood = EpMood.HAPPY) {
                add(fileira("s", listOf("2" to Tint.GREEN, "1" to Tint.GREEN), 310f, w = 70f, h = 50f, size = 20f))
                hot()
            }
        },

        episodio(Skill.TAC, "A ordem dos temporários", PEGADINHA) {
            cena("Três endereços: no máximo uma conta por linha.") {
                add(titulo("a + b * c"))
                add(fileira("e", listOf("a" to Tint.TEAL, "+" to Tint.ORANGE, "b" to Tint.TEAL, "*" to Tint.ORANGE, "c" to Tint.TEAL), 110f, w = 56f, h = 46f, size = 18f))
            }
            pergunta(
                "Qual conta vira a primeira linha? Toque no operador.",
                setOf("e3"), setOf("e1"),
                "Quem está mais fundo na árvore roda antes.",
                "Isso. O vezes vai primeiro e guarda em t1.",
            ) { hot() }
            cena("Primeira linha: t1 recebe b vezes c.") {
                add(codigo("l", listOf("t1 = b * c"), 190f, x = 110f, passo = 34f, size = 18f))
                hot("l0")
            }
            cena("Segunda linha: t2 recebe a mais t1.") {
                add(codigo("l", listOf("t1 = b * c", "t2 = a + t1"), 190f, x = 110f, passo = 34f, size = 18f))
                hot("l1")
            }
            cena("Se inverter as linhas, o t1 é lido antes de existir.", who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.OPTIMIZATION, "Otimizar sem mudar o resultado", PEGADINHA) {
            cena("Otimizar é trocar código por outro mais rápido, com o mesmo resultado.") {
                add(titulo("Pode trocar?"))
                add(Box("a", "2 * 3   vira   6", 180f, 110f, 280f, 46f, Look.CELL, Tint.GREEN, size = 16f))
            }
            cena("Multiplicar por um não muda nada. Dá para tirar.") {
                add(Box("b", "x * 1   vira   x", 180f, 166f, 280f, 46f, Look.CELL, Tint.GREEN, size = 16f))
            }
            pergunta(
                "Uma destas trocas não vale. Toque nela.",
                setOf("d"), setOf("c"),
                "Pense em x valendo zero.",
                "Isso. Com x zero, o programa dava erro, e a troca sumiria com ele.",
            ) {
                add(Box("c", "x + 0   vira   x", 180f, 222f, 280f, 46f, Look.CELL, size = 16f))
                add(Box("d", "x / x   vira   1", 180f, 278f, 280f, 46f, Look.CELL, size = 16f))
            }
            cena("Otimização não pode mudar o que o programa faz, nem nos casos raros.", who = PROF) { tint("d", Tint.RED); hot("d") }
            cena("Na dúvida, o compilador deixa como está.", mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.RUNTIME, "Cada chamada, um registro", PEGADINHA) {
            cena("Função recursiva não reaproveita o mesmo espaço. Cada chamada empilha um registro.") {
                add(titulo("fat(3)"))
                add(pilha("f", listOf("fat(3)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            cena("fat de três chama fat de dois. Sobe outro registro, com o n dele.") {
                add(pilha("f", listOf("fat(3)", "fat(2)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            cena("fat de dois chama fat de um. Três registros vivos ao mesmo tempo.") {
                add(pilha("f", listOf("fat(3)", "fat(2)", "fat(1)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            pergunta(
                "Quem retorna primeiro? Toque no registro.",
                setOf("f2"), setOf("f0", "f1"),
                "A pilha só tira do topo.",
                "Isso. O último a entrar é o primeiro a sair.",
            ) { hot() }
            cena("Ao retornar, o registro some, com as variáveis locais dele.", who = PROF, mood = EpMood.HAPPY) {
                drop("f2")
            }
        },

        episodio(Skill.CODEGEN_MIPS, "O if vira desvio", PEGADINHA) {
            cena("MIPS não tem if. Tem pulo. O teste sai ao contrário.") {
                add(titulo("if (a < b) x = 1;", size = 22f))
                add(codigo("c", listOf("lw  \$t0, a", "lw  \$t1, b", "bge \$t0, \$t1, fim", "li  \$t2, 1", "sw  \$t2, x", "fim:"), 80f, x = 76f, passo = 32f, size = 15f))
            }
            cena("Carrega a e b nos registradores.") { hot("c0", "c1") }
            cena("Aqui está o truque. O if pede a menor que b, e o desvio testa a maior ou igual.") { hot("c2") }
            pergunta(
                "Se a for maior ou igual a b, para onde o programa pula? Toque.",
                setOf("c5"), setOf("c3", "c4"),
                "Com a condição falsa, o corpo do if não pode rodar.",
                "Isso. Ele pula o corpo e cai em fim.",
            ) { hot() }
            cena("Teste invertido: o desvio descreve quando pular o corpo.", who = PROF, mood = EpMood.HAPPY) { hot("c2", "c5") }
        },

        episodio(Skill.REG_ALLOC, "Quem vive junto briga", PEGADINHA) {
            cena("Dois valores vivos ao mesmo tempo não podem dividir um registrador.") {
                add(titulo("Vivos juntos"))
                add(codigo("c", listOf("t1 = a + b", "t2 = c + d", "t3 = t1 * t2"), 80f, x = 100f, passo = 30f, size = 16f))
            }
            cena("t1 nasce na primeira linha, e a terceira lê esse valor. Fica vivo o tempo todo.") { hot("c0", "c2") }
            pergunta(
                "Quem está vivo junto com t1 na segunda linha? Toque no nó.",
                setOf("n2"), setOf("n3"),
                "Veja quem já nasceu e ainda tem uso pela frente.",
                "Isso. t1 e t2 são vizinhos no grafo. Registradores diferentes.",
            ) {
                hot()
                add(Box("n1", "t1", 90f, 270f, 56f, 56f, Look.STATE, Tint.TEAL, size = 16f))
                add(Box("n2", "t2", 200f, 270f, 56f, 56f, Look.STATE, size = 16f))
                add(Box("n3", "t3", 300f, 270f, 56f, 56f, Look.STATE, size = 16f))
            }
            cena("t3 nasce quando t1 e t2 morrem. Ele pode reaproveitar um dos registradores.") {
                wire("n1", "n2", arrow = false, hot = true)
                tint("n2", Tint.ORANGE); tint("n3", Tint.TEAL)
            }
            palavra("INTERFERÊNCIA", "Dois registradores bastam. Colorir o grafo é só isso.", who = PROF, mood = EpMood.HAPPY)
        },
    )
}

@Suppress("unused") private val keepRotulo = ::rotulo
