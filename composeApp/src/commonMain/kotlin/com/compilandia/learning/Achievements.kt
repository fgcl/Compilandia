package com.compilandia.learning

data class Achievement(val emoji: String, val title: String, val description: String, val unlocked: Boolean)

/** Conquistas calculadas a partir do estado (nada a mais para salvar). */
object Achievements {
    fun all(l: Learner): List<Achievement> {
        val s = l.state
        val cal = l.calibration()
        fun m(sk: Skill) = l.mastery(sk) >= 0.8
        return listOf(
            Achievement("👣", "Primeiro passo", "Jogar a primeira partida", s.history.isNotEmpty()),
            Achievement("📖", "Leitor curioso", "Ver aulas em 3 estilos diferentes", s.styleArms.size >= 3),
            Achievement("✍️", "Explicador", "Escrever uma explicação com suas palavras", s.journal.isNotEmpty()),
            Achievement("🔥", "Constância", "3 dias seguidos estudando", s.streakDays >= 3),
            Achievement("🔥🔥", "Hábito de ferro", "7 dias seguidos", s.streakDays >= 7),
            Achievement("🎯", "Calibrado", "10+ respostas de confiança com Brier < 0,2", cal.samples >= 10 && cal.brier < 0.2),
            Achievement("😕➡️💡", "Não desisti", "Apertar 'Não entendi' e depois acertar o assunto", s.feedback.any { f -> f.kind == "nao_entendi" && s.history.any { it.skill == f.skill && it.ts > f.ts && it.score >= 0.8 } }),
            Achievement("🔪", "Ninja léxico", "Dominar tokens e autômatos", m(Skill.TOKENS) && m(Skill.REGEX_DFA)),
            Achievement("⚗️", "Alquimista", "Dominar FIRST e FOLLOW", m(Skill.FIRST) && m(Skill.FOLLOW)),
            Achievement("🛕", "Sacerdote LL(1)", "Dominar tabela e analisador LL(1)", m(Skill.LL1_TABLE) && m(Skill.LL1_PARSING)),
            Achievement("🏰", "Senhor do LR", "Dominar shift-reduce, itens e SLR", m(Skill.SHIFT_REDUCE) && m(Skill.LR0_ITEMS) && m(Skill.SLR_TABLE)),
            Achievement("🦬", "Domador de Bison", "Dominar Yacc/Bison", m(Skill.YACC_BISON)),
            Achievement("🔩", "Montador", "Dominar geração de código MIPS", m(Skill.CODEGEN_MIPS)),
            Achievement("⚔️", "Caçador de chefões", "Derrotar 3 chefões", s.bossesDefeated.size >= 3),
            Achievement("👑", "Compilador humano", "Derrotar todos os chefões", s.bossesDefeated.size >= Region.entries.size),
            Achievement("🛠️", "Engenheiro do TP", "Ler todo o Guia do Trabalho Prático", s.guideRead.size >= 9),
        )
    }
}

/** Chefões de cada região (personagens originais). */
enum class Boss(val region: Region, val emoji: String, val bossName: String, val taunt: String) {
    B_PORTO(Region.PORTO, "🐙", "Polvo Desordenado", "Eu embaralho as fases do compilador! Quero ver você pôr na ordem."),
    B_FLORESTA(Region.FLORESTA_LEXICA, "👻", "Token Fantasma", "Sou um lexema que ninguém consegue cortar direito… hihihi."),
    B_VALE(Region.VALE_GRAMATICAS, "🐉", "Dragão Ambíguo", "Toda frase minha tem DUAS árvores. Escolha… se puder!"),
    B_PONTE(Region.PONTE_TRANSFORMACAO, "🌀", "Recursão Infinita", "E → E + T → E + T + T → E + T + T + T → …"),
    B_TEMPLO(Region.TEMPLO_TOPDOWN, "🗿", "Guardião da Tabela", "Uma célula com duas produções e o templo desaba!"),
    B_FORTALEZA(Region.FORTALEZA_BOTTOMUP, "⚔️", "Cavaleiro Shift/Reduce", "Empilho ou reduzo? Nem eu sei… e você?"),
    B_OFICINA(Region.OFICINA_BISON, "🦬", "Bisão Conflituoso", "bison: 4 shift/reduce conflicts. Boa sorte!"),
    B_CIDADE(Region.CIDADE_SEMANTICA, "🦹", "Tipo Errado", "Somo inteiro com bool e escondo variáveis em escopos!"),
    B_FORJA(Region.FORJA_CODIGO, "💀", "Segmentation Fault", "Esqueceu um sw? Sua pilha estourou? Core dumped!"),
    ;
    companion object { fun of(r: Region) = entries.first { it.region == r } }
}
