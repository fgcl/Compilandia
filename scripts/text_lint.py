#!/usr/bin/env python3
"""Revisor das falas do jogo.

Lê build/voice-lines.tsv (gerado pelo teste VoiceExportTest) e aponta o que atrapalha quem escuta:
  - frase comprida e frase com ideias demais (Lei 15.263/2025: frase curta, ordem direta);
  - vícios de texto de IA: Wikipedia, "Signs of AI writing", e os 26 padrões do Humanizer;
  - voz passiva escondendo quem faz, e nominalização em excesso;
  - legibilidade pelas fórmulas do ALT (legibilidade.com), adaptadas ao português;
  - gramática no LanguageTool local, sem sair da máquina. O modo exigente (level=picky) fica desligado:
    ele empurra para o formal, trocando "usar" por "utilizar" e "morrem" por "perecer".

O índice é alerta, nunca meta: ele não mede coesão, e o próprio ALT avisa disso.
O que nenhum filtro mede vem do jogo: a pessoa toca em "não entendi" e a fala fica marcada.

Uso: scripts/text_lint.py [--lt http://localhost:8010] [--aulas] [arquivo.tsv]
  --aulas varre as aulas antigas em content/ e só informa, sem reprovar.
  --extra varre também o texto que aparece na tela sem voz (álbum, Laboratório, dicas).
Sai com código 1 se alguma fala nova reprova.
"""
import argparse
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAX_PALAVRAS = 16

# Cada entrada: (expressão, o que há de errado). Vale em minúsculas.
VICIOS = [
    (r"\bnão é (apenas|só|somente|mero)\b", "paralelismo negativo: diga o que é"),
    (r"\bnão apenas\b.*\bmas\b", "paralelismo negativo: diga o que é"),
    (r"\bmergulh(ar|e|amos|ando)\b", "metáfora de IA"),
    (r"\bdesvend(ar|e|amos|ando)\b", "metáfora de IA"),
    (r"\bdesbrav(ar|e|amos|ando)\b", "metáfora de IA"),
    (r"\bjornada\b", "metáfora de IA"),
    (r"\bé importante (notar|ressaltar|destacar|lembrar)\b", "enrolação"),
    (r"\bvale (ressaltar|destacar|lembrar|notar)\b", "enrolação"),
    (r"\bem resumo\b|\bem suma\b|\bem conclusão\b|\bconcluindo\b", "fecho de redação"),
    (r"\bno mundo d[aoe]s?\b", "abertura de IA"),
    (r"\bfundamental\b|\bcrucial\b|\bessencial\b|\bvital\b", "ênfase vazia"),
    (r"\bdesempenha(r|m)? um papel\b", "rodeio: use um verbo direto"),
    (r"\bserve como\b|\batua como\b", "rodeio: use 'é'"),
    (r"\bpoderoso\b|\brobusto\b|\bfascinante\b|\bincrível\b", "elogio vazio"),
    (r"\bvamos (explorar|descobrir|entender)\b", "abertura de IA"),
    (r"\bneste (episódio|módulo|capítulo|tópico)\b", "fala de apostila"),
    (r"\bvocê sabia\b", "abertura de IA"),
    (r"—", "travessão de enfeite"),
    (r"\bou seja\b", "muleta: reescreva sem ela"),
    # Humanizer, grupo A: encenar em vez de dizer
    (r"\b(a verdade é que|o segredo (é|está)|a chave (é|está)|o pulo do gato)\b", "frase de efeito: diga o fato"),
    (r"\be é aí que\b|\bé aí que (entra|mora)\b|\bagora vem a\b", "anúncio antes do assunto"),
    (r"\bpense nisso\b|\bsimples assim\b|\bnada mais, nada menos\b|\bno fim das contas\b|\bno final do dia\b", "fecho de efeito"),
    (r"\bvamos lá\b|\bbora ver\b|\bsem mais delongas\b|\bprimeiro de tudo\b", "anúncio antes do assunto"),
    (r"\bpara ser claro\b|\bvamos ser claros\b|\bisso não é sobre\b", "discute com ninguém"),
    (r"\bna sua essência\b|\bem sua essência\b|\ba grande questão\b|\bo verdadeiro\b", "frase profunda vazia"),
    # Humanizer, grupo C: inflar e fugir do verbo ser
    (r"\bdestaca(-se)?\b|\bsublinha\b|\bressalta\b|\benfatiza\b", "verbo de IA: diga o que acontece"),
    (r"\bcenário\b|\bpanorama\b|\becossistema\b|\bteia\b|\btapeçaria\b", "metáfora de IA"),
    (r"\bse apresenta como\b|\bse configura como\b|\bse trata de\b", "rodeio: use 'é' ou 'tem'"),
    (r"\brelacionad[oa]s? a\b|\bassociad[oa]s? a\b|\bligad[oa]s? a\b", "ligação vaga: diga qual é a relação"),
    (r"\bespecialistas (dizem|afirmam)\b|\bestudos mostram\b", "autoridade sem nome"),
    # Humanizer, grupo E: sobras de conversa
    (r"\bótima pergunta\b|\bboa pergunta\b|\bespero que ajude\b|\bfico feliz\b|\bclaro!", "sobra de chatbot"),
]
VOGAIS = "aeiouáéíóúâêôãõàü"


def silabas(palavra: str) -> int:
    """Conta sílabas pelos grupos de vogais, separando hiatos comuns. Boa o bastante para um índice."""
    p = palavra.lower()
    grupos = re.findall(f"[{VOGAIS}]+", p)
    total = len(grupos)
    for g in grupos:
        if re.search(r"[aeo][aeo]|[íú]", g) and len(g) > 1:
            total += 1
    return max(total, 1)


def palavras(t: str):
    return re.findall(r"[\wÀ-ÿ'-]+", t)


def frases(t: str):
    return [f for f in re.split(r"(?<=[.!?])\s+", t.strip()) if f]


def flesch(texto: str) -> float:
    """Índice de legibilidade: acima de 75 é muito fácil, abaixo de 50 é difícil."""
    ws = palavras(texto)
    if not ws:
        return 100.0
    asl = len(ws) / max(len(frases(texto)), 1)
    asw = sum(silabas(w) for w in ws) / len(ws)
    return 248.835 - 1.015 * asl - 84.6 * asw + 42


# ---- fórmulas do ALT (legibilidade.com/sobre.html), adaptadas ao português
def indices(texto: str):
    """Flesch e Gulpease: quanto maior, mais fácil. Os outros dão anos de estudo."""
    ws = palavras(texto)
    if not ws:
        return {}
    p = len(ws)
    f = max(len(frases(texto)), 1)
    s = sum(silabas(w) for w in ws)
    l = sum(len(re.sub(r"[^\wÀ-ÿ]", "", w)) for w in ws)
    return {
        "flesch": 226 - 1.04 * (p / f) - 72 * (s / p),
        "gulpease": 89 + (300 * f - 10 * l) / p,
        "fk": 0.36 * (p / f) + 10.4 * (s / p) - 18,
        "ari": 4.6 * (l / p) + 0.44 * (p / f) - 20,
        "cl": 5.4 * (l / p) - 21 * (f / p) - 14,
    }


# Particípios comuns com "ser": esconde quem faz a ação.
PASSIVA = re.compile(r"\b(é|são|foi|foram|será|serão|sendo|ser)\s+(\w+(?:ado|ada|ados|adas|ido|ida|idos|idas))\b(?! (de|em|a|para) )")
# Adjetivos terminados como particípio, comuns e normais depois de "é".
NAO_PASSIVA = {"ligado", "ligada", "errado", "errada", "certo", "parado", "parada", "cansado", "pronto", "vazio", "cheio", "fechado", "fechada", "aberto", "aberta", "nascido", "válido", "válida", "lido", "lida", "inválido", "inválida", "comprido", "comprida", "rápido", "rápida", "reduzido", "aceso", "acesa", "estado", "sintetizado", "sintetizada", "herdado", "herdada", "lado", "resultado", "ambígua", "ambíguo", "pegadinha", "dúvida"}
NOMINAL = re.compile(r"\b\w{4,}(ção|ções|mento|mentos)\b")


def problemas(texto: str):
    out = []
    baixo = texto.lower()
    for rx, msg in VICIOS:
        if re.search(rx, baixo):
            out.append(f"{msg} ({rx.split(chr(92))[1][:14] if chr(92) in rx else rx})")
    for f in frases(texto):
        n = len(palavras(f))
        if n > MAX_PALAVRAS:
            out.append(f"frase com {n} palavras: {f[:60]}")
        if len(re.findall(r"[;:]", f)) >= 2:
            out.append(f"duas pausas fortes na mesma frase: {f[:60]}")
        if len(NOMINAL.findall(f.lower())) >= 3:
            out.append(f"nominalização demais: troque por verbos: {f[:60]}")
    for m in PASSIVA.finditer(baixo):
        if m.group(2) not in NAO_PASSIVA:
            out.append(f"voz passiva: diga quem faz ({m.group(0)})")
    return out


def tela():
    """Texto que aparece sem voz: o catálogo do álbum, as respostas do Byte, as dicas do diagnóstico."""
    base = ROOT / "composeApp/src/commonMain/kotlin/com/compilandia"
    pad = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
    achados = []
    for f in [base / "content/Bugs.kt", base / "tutor/Diagnostico.kt", base / "tutor/Sims.kt", base / "ui/lab/LabConversa.kt"]:
        for m in pad.finditer(f.read_text(encoding="utf8")):
            t = m.group(1)
            if " " in t and len(t) > 14 and re.search(r"[a-zà-ÿ]{3} [a-zà-ÿ]", t):
                achados.append(re.sub(r"\$\{[^}]*\}|\$\w+", "X", t))
    return achados


def languagetool(url: str, textos):
    """Devolve os problemas por texto. Sem servidor, avisa e segue."""
    achados = {}
    for t in textos:
        data = urllib.parse.urlencode({"text": t, "language": "pt-BR", "disabledRules": "WHITESPACE_RULE"}).encode()
        res = None
        for tentativa in range(5):
            try:
                with urllib.request.urlopen(url + "/v2/check", data, timeout=20) as r:
                    res = json.load(r)
                break
            except Exception as e:
                erro = e
                time.sleep(3 + 3 * tentativa)   # o servidor pode estar reiniciando
        if res is None:
            print(f"aviso: LanguageTool indisponível ({erro}); gramática conferida só em parte", file=sys.stderr)
            return achados
        ms = [m for m in res.get("matches", []) if m["rule"]["issueType"] not in ("typographical",)]
        ms = [m for m in ms if not ignorar(t[m["offset"]:m["offset"] + m["length"]])]
        # Registro falado: regra que pede texto formal vai contra a linguagem simples.
        ms = [m for m in ms if not any(x in m["message"] for x in ("contexto formal", "mais clareza", "estrangeirismo", "Substitua por “à", "pleonasmo"))]
        if ms:
            achados[t] = [f'{m["message"][:70]} [{t[m["offset"]:m["offset"] + m["length"]]}]' for m in ms]
    return achados


def ignorar(trecho: str) -> bool:
    """Letra solta é nome de variável; termos da matéria o corretor não conhece."""
    limpo = [w for w in re.split(r"\W+", trecho) if w]
    return trecho in PERMITIDOS or any(len(w) == 1 or w in PERMITIDOS for w in limpo)


# Termos da matéria que o corretor não conhece.
PERMITIDOS = {
    "scanner", "Flex", "Bison", "token", "tokens", "lexema", "parser", "FIRST", "FOLLOW", "LL", "LR", "LALR", "SLR",
    "AFD", "AFN", "goto", "MIPS", "Byte", "lw", "sw", "mul", "ifa", "fat", "spill", "Bug", "Bugs", "t1", "q1", "q0",
    "shift", "reduce", "int", "float", "vezes", "mais", "Mais", "ifs", "Handle", "while", "Cifrão", "inteiro", "ao mesmo tempo", "épsilon", "fecho", "conta2", "x1", "if", "id", "left", "casa", "handle", "miolo", "else", "If", "AST", "add", "Add", "regex", "Shift", "Float", "aab", "Lw", "Sw", "ifs",
    # Terceira reforma: nomes de código C, de arquivo e das ferramentas, que a fala cita como estão.
    "abb", "closure", "Closure", "ids", "bison", "graph", "main", "getchar", "ungetc", "struct", "structs", "lexer", "expr", "op", "esq", "dir",
    "yyparse", "yylex", "yylval", "gcc", "calc",
}


def aulas_antigas():
    """Texto das aulas antigas, para medir de quanto a reforma melhorou."""
    pad = re.compile(r'(?:Say\(\w+, |Text\(|Callout\(\w+, )"((?:[^"\\]|\\.)*)"')
    for f in sorted((ROOT / "composeApp/src/commonMain/kotlin/com/compilandia/content").glob("LessonsPart*.kt")):
        for m in pad.finditer(f.read_text(encoding="utf8")):
            yield m.group(1)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("arquivo", nargs="?", default=str(ROOT / "composeApp/build/voice-lines.tsv"))
    ap.add_argument("--lt", default="http://localhost:8010")
    ap.add_argument("--aulas", action="store_true")
    ap.add_argument("--extra", action="store_true")
    ap.add_argument("--sem-lt", action="store_true")
    a = ap.parse_args()

    if a.extra:
        textos = tela()
        erros = 0
        for t in textos:
            for p in problemas(t):
                print(f"[tela] {t}\n   -> {p}")
                erros += 1
        junto = " ".join(textos)
        print(f"{len(textos)} textos de tela, {erros} problema(s), {indices(junto)}")
        return 1 if erros else 0

    if a.aulas:
        textos = list(aulas_antigas())
        ruins = Counter()
        for t in textos:
            for p in problemas(t):
                ruins[re.sub(r"[ (:].*", "", p)] += 1
        junto = " ".join(textos)
        print(f"Aulas antigas: {len(textos)} trechos, {len(palavras(junto))} palavras, Flesch {flesch(junto):.0f}")
        for k, v in ruins.most_common(10):
            print(f"  {v:5d} × {k}")
        return 0

    linhas = [l.rstrip("\n").split("\t") for l in open(a.arquivo, encoding="utf8") if l.strip()]
    falas = [(l[0], l[1], l[2].replace("**", "").replace("`", "")) for l in linhas]
    erros = 0
    for _id, who, t in falas:
        for p in problemas(t):
            print(f"[{who}] {t}\n   -> {p}")
            erros += 1
    gram = {} if a.sem_lt else languagetool(a.lt, [t for _, _, t in falas])
    for t, ms in gram.items():
        for m in ms:
            print(f"[gramática] {t}\n   -> {m}")
            erros += 1
    junto = " ".join(t for _, _, t in falas)
    media = len(palavras(junto)) / max(sum(len(frases(t)) for _, _, t in falas), 1)
    print(f"{len(falas)} falas, {len(palavras(junto))} palavras, {media:.1f} palavras por frase, {erros} problema(s)")
    ind = indices(junto)
    print("Índices do ALT (alerta, não meta): " + ", ".join(f"{k} {v:.1f}" for k, v in ind.items()))
    return 1 if erros else 0


if __name__ == "__main__":
    sys.exit(main())
