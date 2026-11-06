package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.learning.Style
import com.compilandia.textos.tx

/** Personagens (originais) que conversam nas aulas, no estilo "livro que fala com você". */
enum class Speaker(val displayName: String, val emoji: String) {
    BYTE(tx("contentmodel_byte"), "🤖"),          // o robozinho aprendiz — pergunta o que VOCÊ pensaria em perguntar
    PROF(tx("comum_profa_gramatica"), "🦉"), // a coruja professora — paciente, adora analogias
    LEXI(tx("contentmodel_lexi"), "🦊"),          // a raposa ninja do léxico — rápida, corta tudo em tokens
    PILHA(tx("contentmodel_pilha"), "🗿"),        // o golem-pilha — só enxerga o topo, fala devagar
    NARRADOR(tx("contentmodel_narrador"), "📜"),
}

enum class CalloutKind(val title: String, val emoji: String) {
    NO_DUMB_QUESTIONS(tx("contentmodel_nao_existem_perguntas_idiotas"), "🙋"),
    BRAIN_POWER(tx("contentmodel_poder_cerebro"), "🧠"),
    WATCH_OUT(tx("contentmodel_cuidado"), "⚠️"),
    MNEMONIC(tx("contentmodel_gancho_memoria"), "🪝"),
    EXAM_TIP(tx("contentmodel_dica_prova"), "📝"),
    FIRESIDE(tx("contentmodel_conversa_ao_pe_lareira"), "🔥"),
    REAL_WORLD(tx("contentmodel_no_mundo_real"), "🌍"),
}

/** Widgets interativos alimentados pelo motor de compiladores (nada de imagem estática). */
enum class VisualKind {
    PIPELINE,          // diagrama das fases (sem parâmetros)
    TOKENIZE,          // input = linha de código; mostra tokens coloridos
    DFA,               // input = expressão regular; desenha o AFD e deixa testar cadeias
    DERIVATION,        // grammar + input; anima a derivação mais à esquerda e a árvore
    PARSE_TREE,        // grammar + input; mostra a(s) árvore(s) — se ambígua, mostra duas
    FIRST_FOLLOW,      // grammar; passo a passo do ponto fixo
    LL1_TABLE,         // grammar; tabela preditiva com conflitos destacados
    LL1_TRACE,         // grammar + input; pilha/entrada/ação passo a passo
    LR0_AUTOMATON,     // grammar; estados com itens e transições
    SLR_TABLE,         // grammar; ACTION/GOTO com conflitos
    LR_TRACE,          // grammar + input; shift-reduce passo a passo (SLR)
    LEFT_RECURSION,    // grammar; antes/depois de eliminar recursão à esquerda
    LEFT_FACTORING,    // grammar; antes/depois da fatoração
    ATTR_TREE,         // input = expressão (ex.: "a + x * 2"); árvore com tipo/valor
    TAC,               // input = expressão; código de três endereços
    SCOPE,             // sem parâmetros; programa com escopos aninhados
    BISON,             // grammar = especificação Bison; input = expressão para rodar
    NFA_SUBSET,        // input = id de um AFN do banco (SmallNFA.bank: abb, ab, aa, abc, eps, pen, ou); construção de subconjuntos passo a passo
    FLEX,              // grammar = especificação .l (seção de regras entre %% e %%); input = texto para escanear
    PANIC,             // grammar (LL(1)) + input COM ERRO; mostra detecção e recuperação em modo pânico
    CALL_STACK,        // input = programa: "fat3", "fat4", "fib3", "chain1", "chain2"; pilha de registros de ativação passo a passo
    MIPS,              // input = expressão só com inteiros a,b,c,d (4,2,3,5) e números; TAC → MIPS → execução
    DESCENT_CODE,      // grammar LL(1) + input opcional; código da descida recursiva (casaToken) e pilha de chamadas
    LALR_TABLE,        // grammar; tabela LALR(1) (ACTION/GOTO) — compare com SLR_TABLE
    RIGHTMOST,         // grammar + input; derivação mais à DIREITA (a que o shift-reduce faz ao contrário)
    MINIMIZE,          // input = índice (0..6) de Minimizer.bank; tabela de pares distinguíveis passo a passo + AFD mínimo
    AST,               // input = expressão; árvore de derivação × AST, contagem de nós e código Java new BinOp(...)
    REGALLOC,          // input = expressão (ints a,b,c,d); vivacidade, grafo de interferência, coloração e MIPS com registradores
    MIPS_RUN,          // grammar = programa MIPS completo (.data/.text/main:); roda passo a passo no "MARS de bolso"
    MINILANG,          // input = programa da minilinguagem (x = ...; if (a < b) {...} else {...}; while (...) {...}; print(x);) com a=4,b=7 → MIPS com desvios, executado
}

/** Um "momento" de uma aula. A UI mostra um por vez (ritmo de conversa, não de apostila). */
sealed class Beat {
    /** Fala de personagem em balão. */
    data class Say(val who: Speaker, val text: String) : Beat()

    /** Parágrafo curto de texto (pode usar **negrito** e `código`). */
    data class Text(val text: String) : Beat()

    /** Bloco de código / gramática com legenda opcional. */
    data class Code(val code: String, val caption: String = "") : Beat()

    /** Caixa de destaque estilo "Use a Cabeça". */
    data class Callout(val kind: CalloutKind, val text: String) : Beat()

    /**
     * Preveja antes de ver (efeito de geração / teste prévio). NÃO é prova:
     * errar aqui é ótimo — cria a "curiosidade" que faz a explicação grudar.
     */
    data class Predict(
        val question: String,
        val options: List<String>,
        val correct: Int,
        val explanation: String,
    ) : Beat()

    /** Toque para revelar — prática de recuperação: pense ANTES de tocar. */
    data class Reveal(val prompt: String, val answer: String) : Beat()

    /** Lista de passos revelados um a um. */
    data class Steps(val title: String, val steps: List<String>) : Beat()

    /** Widget interativo do motor. */
    data class Visual(
        val kind: VisualKind,
        val grammar: String = "",
        val input: String = "",
        val caption: String = "",
    ) : Beat()
}

data class Lesson(
    val skill: Skill,
    val style: Style,
    val title: String,
    val beats: List<Beat>,
)

/** Ficha de cada habilidade: base para revisão espaçada e autoexplicação (técnica Feynman). */
/** Capítulo do Guia do Trabalho Prático: construir um compilador completo. */
data class GuideChapter(val number: Int, val title: String, val summary: String, val skill: Skill, val beats: List<Beat>)

data class SkillCard(
    val skill: Skill,
    val oneLiner: String,                 // a ideia em uma frase
    val hook: String,                     // gancho mnemônico
    val feynmanPrompt: String,            // "explique para um amigo de 12 anos..."
    val keyTerms: List<String>,           // termos que uma boa explicação deve conter
    val commonMistakes: List<String>,
    val flashcards: List<Pair<String, String>>, // pergunta -> resposta (revisão relâmpago)
)
