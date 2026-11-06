#!/usr/bin/env python3
"""Tira as frases do código Kotlin e põe nos arquivos de textos (formato strings.xml).

Cada frase vira uma chave em composeApp/src/commonMain/textos/pt/<área>.xml, e o código
passa a chamar tx("chave"). Frases com ${...} viram marcadores %1$s, %2$s.

Uso:
  scripts/textos/extrair.py --plano            # só conta e lista, sem mexer em nada
  scripts/textos/extrair.py --aplicar          # troca no código e grava os XML
  scripts/textos/extrair.py --soltas           # lista frases que ainda estão no código (sai 1 se houver)
  ... [arquivo.kt ...]                         # limita a estes arquivos
"""
import argparse
import re
import sys
import unicodedata
from pathlib import Path
from xml.sax.saxutils import escape as xml_escape

RAIZ = Path(__file__).resolve().parent.parent.parent
FONTES = RAIZ / "composeApp/src/commonMain/kotlin/com/compilandia"
TEXTOS = RAIZ / "composeApp/src/commonMain/textos"
IMPORT = "import com.compilandia.textos.tx"

PARADAS = set("""de do da dos das o a os as um uma uns umas e é que para pra por com não no na nos nas se em ao aos à às mais menos
sua seu suas seus você vocês isso isto este esta esse essa ele ela eles elas como quando onde qual quais quem só já vem tem foi ser
está estão são pelo pela pelos pelas cada todo toda todos todas depois antes sem até mas ou aqui ali ainda outro outra outros nada
tudo agora faz fica vai pode deu dá vez também muito pouco bem mal sim nunca sempre hoje amanhã ontem me te lhe nosso nossa meu minha
porque porquê então já lá aí cá há sobre entre desde contra num numa dum duma pelo esse""".split())
CODIGO = set("""inttofloat float while return main printf scanf getchar putchar struct case break else then true false null void char
token yylex yyparse yylval yytext stdio stdlib include define switch default static const public class goto closure shift reduce accept
lookahead nonassoc prec start type union extern malloc sizeof enum typedef lexema""".split())
TECNICAS_MAIUSC = re.compile(r"^[A-Z0-9_()\[\]$#*+.,:/|'-]+$")

UI_CHAMADAS = re.compile(r"(?:\b(?:Text|RichText|BigButton|Chip|ToolTag|Header|SpeechBubble|say|Drawer|OpenDrawer|DrawerBin|Mono|Callout|Reveal|Say|"
                         r"SmallButton|TextButton|OutlinedButton|Tag|Pill|Badge|Title|Label|Section|Hint|Toast|Fx\.porta|Dica|Aviso|BigText)\s*\(\s*)$"
                         r"|(?:\b(?:label|title|titulo|subtitle|text|texto|hint|placeholder|note|nota|tagline|caption|legenda)\s*=\s*)$")


def area_de(caminho: Path) -> str:
    rel = caminho.relative_to(FONTES).as_posix()
    if rel.startswith("ui/") or rel.startswith("platform/") or "/" not in rel:
        return "ui"
    if rel.startswith("content/episodes/"):
        return "aulas"
    if rel.startswith("content/Lessons"):
        return "textos"
    if rel.startswith("content/TpGuide"):
        return "guia"
    if rel.startswith("content/"):
        return "conteudo"
    if rel.startswith("engine/"):
        return "motor"
    if rel.startswith("tutor/"):
        return "tutor"
    if rel.startswith("learning/"):
        return "aprendizado"
    return "ui"


# ------------------------------------------------------------------ leitura do Kotlin
class Lit:
    """Uma string do Kotlin: partes de texto e expressões ${...}."""
    def __init__(self, ini, fim, cru):
        self.ini, self.fim, self.cru = ini, fim, cru
        self.partes = []  # ("t", texto) ou ("e", ini, fim, filhos) ou ("n", nome)


def ler_char(src, i):
    """Pula um literal de caractere começando em i (src[i] == "'"). Devolve o índice depois dele."""
    j = i + 1
    if j < len(src) and src[j] == "\\":
        j += 2
        while j < len(src) and src[j] != "'" and j - i < 10:
            j += 1
    else:
        j += 1
    if j < len(src) and src[j] == "'":
        return j + 1
    return i + 1


ESC = {"t": "\t", "b": "\b", "n": "\n", "r": "\r", "'": "'", '"': '"', "\\": "\\", "$": "$"}


def ler_codigo(src, i, fim_char=None):
    """Lê código a partir de i até achar fim_char no nível 0 (ou o fim). Devolve (índice, literais de nível 0)."""
    lits = []
    prof = 0
    n = len(src)
    while i < n:
        c = src[i]
        if src.startswith("//", i):
            j = src.find("\n", i)
            i = n if j < 0 else j
            continue
        if src.startswith("/*", i):
            d, i = 1, i + 2
            while i < n and d:
                if src.startswith("/*", i):
                    d += 1; i += 2
                elif src.startswith("*/", i):
                    d -= 1; i += 2
                else:
                    i += 1
            continue
        if c == "'":
            i = ler_char(src, i)
            continue
        if c == '"':
            lit, i = ler_string(src, i)
            lits.append(lit)
            continue
        if fim_char and c in "({[":
            prof += 1
        elif fim_char and c in ")}]":
            if prof == 0 and c == fim_char:
                return i, lits
            prof -= 1
        i += 1
    return i, lits


def ler_string(src, i):
    cru = src.startswith('"""', i)
    # Kotlin 2: $$"..." pede $$ para interpolar; um $ sozinho é texto.
    dol = 0
    while i - dol - 1 >= 0 and src[i - dol - 1] == "$":
        dol += 1
    ini = i - dol
    i += 3 if cru else 1
    lit = Lit(ini, None, cru)
    pref = "$" * max(dol, 1)
    buf = []
    n = len(src)

    def solta():
        if buf:
            lit.partes.append(("t", "".join(buf)))
            buf.clear()

    while i < n:
        c = src[i]
        if cru and src.startswith('"""', i):
            # aspas extras no fim fazem parte do texto
            k = i
            while k + 3 < n and src[k + 3] == '"':
                buf.append('"'); k += 1
            i = k + 3
            break
        if not cru and c == '"':
            i += 1
            break
        if not cru and c == "\\":
            d = src[i + 1]
            if d == "u":
                buf.append(chr(int(src[i + 2:i + 6], 16))); i += 6
            else:
                buf.append(ESC.get(d, d)); i += 2
            continue
        if src.startswith(pref + "{", i):
            solta()
            e_ini = i + len(pref) + 1
            e_fim, filhos = ler_codigo(src, e_ini, "}")
            expr = src[e_ini:e_fim]
            if cru and expr.strip() == "'$'":
                buf.append("$")
            else:
                lit.partes.append(("e", e_ini, e_fim, filhos))
            i = e_fim + 1
            continue
        if src.startswith(pref, i) and i + len(pref) < n and (src[i + len(pref)].isalpha() or src[i + len(pref)] == "_"):
            solta()
            j = i + len(pref)
            while j < n and (src[j].isalnum() or src[j] == "_"):
                j += 1
            lit.partes.append(("n", src[i + len(pref):j]))
            i = j
            continue
        buf.append(c)
        i += 1
    solta()
    lit.fim = i
    return lit, i


# ------------------------------------------------------------------ decidir o que é frase
def texto_modelo(lit):
    """Texto com marcadores %1$s no lugar das expressões."""
    out, k = [], 0
    for p in lit.partes:
        if p[0] == "t":
            out.append(p[1])
        else:
            k += 1
            out.append(f"%{k}$s")
    return "".join(out)


def so_texto(lit):
    return "".join(p[1] for p in lit.partes if p[0] == "t")


def tem_acento(s):
    return any(ord(ch) > 127 and ch.isalpha() for ch in s)


def parece_codigo(s):
    t = s.strip()
    if re.match(r"^[A-Z][A-Za-z0-9_']*\s*(->|→|::=)", t):
        return True  # produção de gramática
    if re.search(r"\$t\d|\$s\d|\$a\d|\$v\d|\$zero|\$ra|\$sp", t):
        return True  # MIPS
    if re.match(r"^%[a-z]+", t) and not tem_acento(t):
        return True  # diretiva de Yacc/Bison
    if re.search(r"[;{}]", t) and not tem_acento(t):
        return True
    if re.search(r"(^|\s)(int|float|void|return|while|printf|struct|#include|%token|%left|%right|%%)\b", t) and not tem_acento(t) and not set(re.findall(r"[a-zà-ú]+", t.lower())) & (PARADAS - {"a", "e", "o", "se"}):
        return True
    return False


def contexto_antes(src, ini, n=80):
    return src[max(0, ini - n):ini]


def contexto_depois(src, fim, n=30):
    return src[fim:fim + n]


def linha_de(src, ini):
    a = src.rfind("\n", 0, ini) + 1
    b = src.find("\n", ini)
    return src[a:b if b >= 0 else len(src)]


def e_frase(src, lit):
    texto = so_texto(lit)
    modelo = texto_modelo(lit)
    if not re.search(r"[A-Za-zÀ-ú]", texto):
        return False, "sem letras"
    antes = contexto_antes(src, lit.ini)
    depois = contexto_depois(src, lit.fim)
    linha = linha_de(src, lit.ini).strip()
    if re.search(r"(==|!=)\s*$", antes) or re.match(r"\s*(==|!=)", depois):
        return False, "comparação"
    if re.match(r"\s*->", depois) and not re.search(r"\bto\s*$", antes):
        return False, "ramo de when"
    if re.search(r"@\w+(\.\w+)*\(\s*[^)]*$", linha[:max(0, linha.find('"'))]) and linha.lstrip().startswith("@"):
        return False, "anotação"
    if re.search(r"\bconst\s+val\b", linha):
        return False, "const"
    if re.search(r"(Regex|toRegex|require|check|error|println|print|TODO|assert\w*|key|remember\w*|Exception|IllegalStateException|IllegalArgumentException)\s*\(\s*$", antes) or re.match(r"\s*\.toRegex\(", depois):
        return False, "código"
    if re.search(r"\b(startsWith|endsWith|contains|split|replace|indexOf|substringBefore|substringAfter|removePrefix|removeSuffix|matches|get|containsKey|getValue|getOrDefault|getOrElse)\s*\(\s*$", antes):
        return False, "operação de texto"
    if re.search(r"\b(id|key|chave|tipo|kind|name|nome_interno|prefix|prefixo)\s*=\s*$", antes):
        return False, "identificador nomeado"
    if parece_codigo(modelo):
        return False, "parece código"
    palavras = re.findall(r"[A-Za-zÀ-ú]+", texto)
    tem_espaco = bool(re.search(r"\s", modelo.strip()))
    na_ui = bool(UI_CHAMADAS.search(antes))
    if not tem_espaco:
        t = texto.strip()
        if len(re.findall(r"[A-Za-zÀ-ú]", t)) < 3:
            return False, "curto"
        if TECNICAS_MAIUSC.match(t):
            return False, "sigla ou termo técnico"
        if na_ui:
            return True, "rótulo de tela"
        if tem_acento(t) or re.match(r"^[A-ZÀ-Ú][a-zà-ú]+[!?.:…]*$", t):
            return True, "palavra"
        return False, "identificador"
    low = {p.lower() for p in palavras}
    if tem_acento(texto) or low & PARADAS or na_ui:
        return True, "frase"
    comuns = [p for p in palavras if len(p) >= 4 and p.lower() not in CODIGO and (p.islower() or (p[0].isupper() and p[1:].islower()))]
    if comuns:
        return True, "frase sem acento"
    return False, "sem sinal de português"


# ------------------------------------------------------------------ chaves
def ascii(s):
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))


def slug(s, n=5):
    palavras = re.findall(r"[a-z0-9]+", ascii(s.lower()))
    corte = [p for p in palavras if p not in ("o", "a", "os", "as", "e", "de", "do", "da", "um", "uma", "s")] or palavras
    return "_".join(corte[:n])[:44].strip("_") or "texto"


def prefixo(caminho: Path):
    return re.sub(r"[^a-z0-9]", "", caminho.stem.lower())


# ------------------------------------------------------------------ XML
def xml_texto(s):
    s = s.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")
    return xml_escape(s)


def ler_xml(arq: Path):
    """Lê <string name="k">v</string> mantendo a ordem."""
    if not arq.exists():
        return {}
    from xml.etree import ElementTree as ET
    raiz = ET.parse(arq).getroot()
    out = {}
    for e in raiz.findall("string"):
        v = e.text or ""
        v = re.sub(r"\\(\\|n|t)", lambda m: {"\\": "\\", "n": "\n", "t": "\t"}[m.group(1)], v)
        out[e.get("name")] = v
    return out


def gravar_xml(arq: Path, grupos):
    """grupos: lista de (nome do arquivo .kt, [(chave, texto)])."""
    linhas = ['<?xml version="1.0" encoding="utf-8"?>',
              "<!-- Textos do Compilândia. A chave é o que o código usa em tx(\"chave\"); %1$s, %2$s são trocados pelos valores. -->",
              "<resources>"]
    for nome, itens in grupos:
        if not itens:
            continue
        linhas.append(f"    <!-- {nome} -->")
        for k, v in itens:
            linhas.append(f'    <string name="{k}">{xml_texto(v)}</string>')
    linhas.append("</resources>")
    arq.parent.mkdir(parents=True, exist_ok=True)
    arq.write_text("\n".join(linhas) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ troca
class Extrator:
    def __init__(self):
        self.por_texto = {}   # texto modelo -> chave
        self.chaves = {}      # chave -> texto
        self.novas = {}       # área -> {arquivo -> [(chave, texto)]}
        self.decisoes = []    # (arquivo, linha, sim/não, motivo, texto)
        for arq in sorted(TEXTOS.glob("pt/*.xml")):
            for k, v in ler_xml(arq).items():
                self.chaves[k] = v
                self.por_texto.setdefault(v, k)

    def chave_para(self, modelo, caminho):
        if modelo in self.por_texto:
            return self.por_texto[modelo], False
        base = prefixo(caminho) + "_" + slug(modelo.replace("%", " "))
        k, n = base, 2
        while k in self.chaves:
            k = f"{base}_{n}"; n += 1
        self.chaves[k] = modelo
        self.por_texto[modelo] = k
        return k, True

    def render_codigo(self, src, ini, fim, filhos, caminho, aplicar):
        """Código entre ini e fim, com as strings de dentro já tratadas."""
        out, pos = [], ini
        for lit in filhos:
            out.append(src[pos:lit.ini])
            out.append(self.render_lit(src, lit, caminho, aplicar))
            pos = lit.fim
        out.append(src[pos:fim])
        return "".join(out)

    def render_lit(self, src, lit, caminho, aplicar):
        sim, motivo = e_frase(src, lit)
        linha = src.count("\n", 0, lit.ini) + 1
        self.decisoes.append((caminho, linha, sim, motivo, texto_modelo(lit)))
        if not sim:
            # mantém a string, mas trata as strings dentro de ${...}
            if not any(p[0] == "e" and p[3] for p in lit.partes):
                return src[lit.ini:lit.fim]
            out, pos = [], lit.ini
            for p in lit.partes:
                if p[0] == "e" and p[3]:
                    out.append(src[pos:p[1]])
                    out.append(self.render_codigo(src, p[1], p[2], p[3], caminho, aplicar))
                    pos = p[2]
            out.append(src[pos:lit.fim])
            return "".join(out)
        modelo = texto_modelo(lit)
        sufixo = ""
        depois = src[lit.fim:lit.fim + 20]
        sem_expr = all(p[0] == "t" for p in lit.partes)
        if lit.cru and sem_expr and depois.startswith(".trimIndent()"):
            import textwrap
            modelo = textwrap.dedent(modelo).strip("\n")
            sufixo = "TRIM"
        k, nova = self.chave_para(modelo, caminho)
        if nova:
            self.novas.setdefault(area_de(caminho), {}).setdefault(caminho.name, []).append((k, modelo))
        args = []
        for p in lit.partes:
            if p[0] == "e":
                args.append(self.render_codigo(src, p[1], p[2], p[3], caminho, aplicar).strip())
            elif p[0] == "n":
                args.append(p[1])
        chamada = f'tx("{k}"' + "".join(", " + a for a in args) + ")"
        return chamada + ("\x00TRIM" if sufixo else "")

    def arquivo(self, caminho: Path, aplicar: bool):
        src = caminho.read_text(encoding="utf-8")
        _, lits = ler_codigo(src, 0)
        novo = self.render_codigo(src, 0, len(src), lits, caminho, aplicar)
        novo = novo.replace("\x00TRIM.trimIndent()", "")
        if novo != src and aplicar:
            if IMPORT not in novo:
                m = re.search(r"^package .*$", novo, re.M)
                ultimo = None
                for mm in re.finditer(r"^import .*$", novo, re.M):
                    ultimo = mm
                if ultimo:
                    novo = novo[:ultimo.end()] + "\n" + IMPORT + novo[ultimo.end():]
                elif m:
                    novo = novo[:m.end()] + "\n\n" + IMPORT + novo[m.end():]
            caminho.write_text(novo, encoding="utf-8")
        return novo != src

    def gravar(self):
        por_area = {}
        for arq in sorted(TEXTOS.glob("pt/*.xml")):
            por_area[arq.stem] = ler_xml(arq)
        for area, arquivos in self.novas.items():
            atual = por_area.get(area, {})
            grupos_velhos = [("anteriores", list(atual.items()))] if atual else []
            gravar_xml(TEXTOS / "pt" / f"{area}.xml", grupos_velhos + [(n, itens) for n, itens in arquivos.items()])


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--plano", action="store_true")
    g.add_argument("--aplicar", action="store_true")
    g.add_argument("--soltas", action="store_true")
    ap.add_argument("--relatorio", type=Path, help="grava todas as decisões neste arquivo")
    ap.add_argument("arquivos", nargs="*", type=Path)
    a = ap.parse_args()
    alvos = [p.resolve() for p in a.arquivos] or sorted(FONTES.rglob("*.kt"))
    alvos = [p for p in alvos if "/textos/" not in p.as_posix()]
    ex = Extrator()
    mudou = 0
    for p in alvos:
        if ex.arquivo(p, a.aplicar):
            mudou += 1
    if a.aplicar:
        ex.gravar()
    sim = [d for d in ex.decisoes if d[2]]
    if a.soltas:
        for c, l, _, m, t in sim:
            print(f"{c.relative_to(RAIZ)}:{l}: {t[:90]!r}")
        print(f"{len(sim)} frases soltas no código.", file=sys.stderr)
        sys.exit(1 if sim else 0)
    from collections import Counter
    print(f"arquivos com frases: {mudou} de {len(alvos)}")
    print(f"strings vistas: {len(ex.decisoes)}; frases: {len(sim)}; chaves novas: {sum(len(v) for a_ in ex.novas.values() for v in a_.values())}")
    print("motivos do sim:", dict(Counter(d[3] for d in sim)))
    print("motivos do não:", dict(Counter(d[3] for d in ex.decisoes if not d[2])))
    if a.relatorio:
        with a.relatorio.open("w", encoding="utf-8") as f:
            for c, l, s, m, t in ex.decisoes:
                f.write(f"{'SIM' if s else 'nao'}\t{m}\t{c.relative_to(FONTES)}:{l}\t{t!r}\n")


if __name__ == "__main__":
    main()
