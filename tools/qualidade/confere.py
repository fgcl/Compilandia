#!/usr/bin/env python3
"""Confere UM arquivo logo depois de cada edição (AGENTS.md, "Código limpo"), em menos de 1 s.

Só cobra o que a edição trouxe: linhas novas (git diff contra o HEAD) e funções que nasceram agora ou que pioraram. O código
antigo que já estava fora da regra não aparece aqui; ele é do detekt, do ruff ou do eslint, no fim.
Os limites ficam em config/qualidade/limites.json (do projeto; o kit não sobrescreve). Limite 0 desliga a conferência
(útil quando outro verificador do projeto já cuida dela, para não avisar duas vezes).

uso: tools/qualidade/confere.py ARQUIVO         sai com 1 e lista o que achou (o hook do Claude Code chama sozinho)
     tools/qualidade/confere.py --hook          lê o JSON do hook na entrada (PostToolUse de Edit/Write)
"""
import json
import os
import re
import subprocess
import sys

PADRAO = {"arquivo": 400, "funcao": 50, "complexidade": 12, "aninhamento": 3, "parametros": 6, "colunas": 120,
          "returns": 4, "condicao": 4, "ramos_por_tipo": 5}
KOTLIN, PYTHON, JS = (".kt", ".kts"), (".py",), (".js", ".jsx", ".ts", ".tsx", ".mjs")
CONTROLE = re.compile(r"^\s*(\}\s*)?(if|else if|else|for|while|when|try|catch|do|elif|except|with|match|switch)\b")
DECISAO = re.compile(r"\b(if|for|while|catch|case|elif|except)\b|&&|\|\||\?:| and | or ")
BOTAO_LARGO = re.compile(r"\b(BigButton|Button|OutlinedButton|TextButton)\(|\.clickable\b")
LARGURA_LIMITADA = re.compile(r"widthIn\(|larguraDeLeitura\(|\.width\(")
TEMA = re.compile(r"(?i)(/theme/|tema|paleta|palette|cores|colors?\.kt|/art/)")


def raiz_e_relativo(caminho):
    pasta = os.path.dirname(os.path.abspath(caminho))
    r = subprocess.run(["git", "-C", pasta, "rev-parse", "--show-toplevel"], capture_output=True, text=True)
    raiz = r.stdout.strip() or pasta
    return raiz, os.path.relpath(os.path.abspath(caminho), raiz)


def limites(raiz):
    lim = dict(PADRAO)
    try:
        lim.update(json.load(open(os.path.join(raiz, "config/qualidade/limites.json"), encoding="utf-8")))
    except (OSError, ValueError):
        pass
    return lim


def versao_antiga(raiz, rel):
    r = subprocess.run(["git", "-C", raiz, "show", f"HEAD:{rel}"], capture_output=True, text=True)
    return r.stdout if r.returncode == 0 else None


def linhas_novas(raiz, rel, n_linhas):
    """Números (a partir de 1) das linhas que a edição criou ou mudou."""
    r = subprocess.run(["git", "-C", raiz, "diff", "-U0", "HEAD", "--", rel], capture_output=True, text=True)
    if r.returncode != 0 or not r.stdout:
        rastreado = subprocess.run(["git", "-C", raiz, "ls-files", "--error-unmatch", rel], capture_output=True)
        return set() if rastreado.returncode == 0 else set(range(1, n_linhas + 1))
    novas = set()
    for m in re.finditer(r"^@@ -\S+ \+(\d+)(?:,(\d+))? @@", r.stdout, re.M):
        ini, qtd = int(m.group(1)), int(m.group(2) or 1)
        novas.update(range(ini, ini + qtd))
    return novas


def sem_strings_e_comentarios(linha, ext):
    linha = re.sub(r'"(?:\\.|[^"\\])*"', '""', linha)
    linha = re.sub(r"'(?:\\.|[^'\\])*'", "''", linha)
    return re.split(r"#" if ext in PYTHON else r"//", linha)[0]


def nulo_forcado(n, cod):
    if "!!" in cod:
        return [f"linha {n}: '!!' derruba o app se vier nulo; use ?.let, ?: ou requireNotNull com mensagem"]
    return []


def botao_esticado(n, cod):
    """Botão com fillMaxWidth() e sem teto de largura: no monitor vira uma faixa de borda a borda."""
    if BOTAO_LARGO.search(cod) and "fillMaxWidth()" in cod and not LARGURA_LIMITADA.search(cod):
        return [f"linha {n}: botão com fillMaxWidth() estica de borda a borda no monitor; limite ele "
                "(ou a coluna/gaveta dele) com widthIn(max = LISTA_MAX) e rode o BotaoEsticadoTest"]
    return []


# ---------- funções ----------

def funcoes(linhas, ext):
    """[(nome, início, fim, assinatura, anotada_composable)] com índices de linha a partir de 0."""
    achadas = []
    if ext in PYTHON:
        for i, l in enumerate(linhas):
            m = re.match(r"^(\s*)(async\s+)?def\s+(\w+)\s*\(", l)
            if not m:
                continue
            ind, fim = len(m.group(1)), i
            for j in range(i + 1, len(linhas)):
                s = linhas[j]
                if s.strip() and (len(s) - len(s.lstrip())) <= ind:
                    break
                if s.strip():
                    fim = j
            achadas.append((m.group(3), i, fim, assinatura(linhas, i), False))
        return achadas
    padrao = (re.compile(r"\bfun\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)\s*\(") if ext in KOTLIN else
              re.compile(r"(?:\bfunction\s+(\w+)\s*\(|^\s*(?:async\s+)?(\w+)\s*\([^)]*\)\s*\{|\b(?:const|let)\s+(\w+)\s*=\s*"
                         r"(?:async\s*)?\([^)]*\)\s*=>\s*\{)"))
    for i, l in enumerate(linhas):
        m = padrao.search(sem_strings_e_comentarios(l, ext))
        if not m or re.match(r"^\s*(if|for|while|switch|catch)\b", l):
            continue
        nome = next(g for g in m.groups() if g)
        profundidade, comecou, fim = 0, False, None
        for j in range(i, min(len(linhas), i + 2000)):
            limpo = sem_strings_e_comentarios(linhas[j], ext)
            if not comecou and j > i + 8:
                break
            for c in limpo:
                if c == "{":
                    profundidade += 1; comecou = True
                elif c == "}":
                    profundidade -= 1
            if comecou and profundidade <= 0:
                fim = j
                break
            if not comecou and j == i and re.search(r"\)\s*(:\s*[\w<>?, .]+)?\s*=", limpo):
                break  # função de expressão (fun x() = ...): não tem corpo para medir
        if fim is None:
            continue
        composable = any("@Composable" in linhas[k] for k in range(max(0, i - 3), i + 1))
        achadas.append((nome, i, fim, assinatura(linhas, i), composable))
    return achadas


def assinatura(linhas, i):
    texto, nivel, dentro = "", 0, False
    for l in linhas[i:i + 15]:
        for c in l:
            if c == "(":
                nivel += 1; dentro = True
                if nivel == 1:
                    continue
            elif c == ")":
                nivel -= 1
                if dentro and nivel == 0:
                    return texto
            if dentro:
                texto += c
        texto += " "
    return texto


def mede(linhas, f, ext):
    nome, ini, fim, sig, composable = f
    corpo = linhas[ini:fim + 1]
    limpo = [sem_strings_e_comentarios(l, ext) for l in corpo]
    codigo = [l for l in limpo if l.strip() and l.strip() not in ("{", "}")]
    params = 0
    if sig.strip():
        nivel = 0
        params = 1
        for c in sig:
            nivel += c in "(<[{"
            nivel -= c in ")>]}"
            params += c == "," and nivel == 0
        params -= 1 if sig.strip().endswith(",") else 0
        if ext in PYTHON:
            params -= len(re.findall(r"^\s*(self|cls)\s*(,|$)", sig))
    decisoes = sum(len(DECISAO.findall(l)) for l in limpo[1:])
    if ext in KOTLIN:  # cada ramo de when conta
        decisoes += sum(1 for l in limpo[1:] if re.search(r"->", l) and not re.search(r"\{[^}]*->", l)
                        and not l.strip().startswith("else"))
    pilha, maximo = [], 0
    for l in limpo[1:]:
        if not l.strip():
            continue
        ind = len(l) - len(l.lstrip())
        while pilha and pilha[-1] >= ind:
            pilha.pop()
        if CONTROLE.match(l) and not re.match(r"^\s*\}?\s*(else|catch|elif|except)\b", l):
            pilha.append(ind)
            maximo = max(maximo, len(pilha))
    returns = sum(len(re.findall(r"\breturn\b(?!@)", l)) for l in limpo)
    return {"nome": nome, "linhas": len(codigo), "parametros": params, "complexidade": 1 + decisoes,
            "aninhamento": maximo, "returns": returns, "composable": composable}


# ---------- conferência ----------

def confere(caminho):
    ext = os.path.splitext(caminho)[1]
    if ext not in KOTLIN + PYTHON + JS or not os.path.isfile(caminho):
        return [], []
    raiz, rel = raiz_e_relativo(caminho)
    lim = limites(raiz)
    texto = open(caminho, encoding="utf-8", errors="replace").read()
    linhas = texto.split("\n")
    teste = bool(re.search(r"(?i)(^|/)(test|tests|__tests__|\w*test)/|_test\.py$|Test\.kt$|\.(test|spec)\.[jt]sx?$", rel))
    novas = linhas_novas(raiz, rel, len(linhas))
    if not novas:
        return [], []
    antigo = versao_antiga(raiz, rel)
    erros, avisos = [], []

    # tamanho do arquivo
    n_antes = len(antigo.split("\n")) if antigo is not None else 0
    if lim["arquivo"] and not teste and len(linhas) > lim["arquivo"] and len(linhas) > n_antes:
        erros.append(f"o arquivo tem {len(linhas)} linhas (limite {lim['arquivo']})"
                     + (f" e cresceu {len(linhas) - n_antes}" if n_antes > lim["arquivo"] else "")
                     + ": o código novo vai para outro arquivo, com uma responsabilidade só")

    # funções novas ou que pioraram
    medidas_antigas = {}
    if antigo is not None:
        la = antigo.split("\n")
        medidas_antigas = {f[0]: mede(la, f, ext) for f in funcoes(la, ext)}
    for f in ([] if teste else funcoes(linhas, ext)):
        if not novas.intersection(range(f[1] + 1, f[2] + 2)):
            continue
        m, velha = mede(linhas, f, ext), medidas_antigas.get(f[0])
        regras = [("linhas", "funcao", "linhas", "extraia partes com nome que diz o que fazem"),
                  ("complexidade", "complexidade", "de complexidade",
                   "troque cadeias de if/when por tabela, mapa ou método no tipo; extraia condições com nome"),
                  ("aninhamento", "aninhamento", "blocos aninhados", "use retorno antecipado e extraia o bloco de dentro"),
                  ("returns", "returns", "returns", "junte as saídas ou extraia funções")]
        if not m["composable"]:
            regras.append(("parametros", "parametros", "parâmetros", "parâmetros que andam juntos viram um objeto (data class, dataclass)"))
        for chave, limite, rotulo, dica in regras:
            valor = m[chave]
            if valor > lim[limite] and (velha is None or valor > velha[chave]):
                antes = f" (era {velha[chave]})" if velha else ""
                erros.append(f"{m['nome']}() tem {valor} {rotulo}{antes}, limite {lim[limite]}: {dica}")

    # linha a linha, só nas linhas novas
    for n in sorted(novas):
        if n > len(linhas):
            continue
        l = linhas[n - 1]
        s = l.strip()
        comentario = s.startswith(("//", "*", "/*", "#"))
        if lim["colunas"] and len(l) > lim["colunas"] and not comentario:
            erros.append(f"linha {n} tem {len(l)} colunas (limite {lim['colunas']}): quebre depois da vírgula ou do ponto")
        if re.search(r"\b(TODO|FIXME)\b", l) and not teste:
            erros.append(f"linha {n}: TODO ou FIXME no código; faça agora ou anote em docs/LACUNAS.md")
        if comentario or teste:
            continue
        cod = sem_strings_e_comentarios(l, ext)
        if len(re.findall(r"&&|\|\|| and | or ", cod)) + 1 > lim["condicao"]:
            erros.append(f"linha {n}: condição com mais de {lim['condicao']} partes; dê nome a ela numa val ou função")
        if ext in KOTLIN:
            erros += nulo_forcado(n, cod)
            if not TEMA.search(rel):
                if re.search(r"\bText\(\s*\"[^\"$]*[A-Za-zÀ-ÿ]{2}", l) or re.search(r"contentDescription\s*=\s*\"[^\"]*[A-Za-z]", l):
                    erros.append(f"linha {n}: texto escrito na tela; ponha no arquivo de textos e chame pela chave")
                if re.search(r"\bColor\(\s*0x|\bColor\.(White|Black|Red|Green|Blue|Gray|Yellow|Cyan|Magenta|DarkGray|LightGray)\b", l):
                    erros.append(f"linha {n}: cor fixa; use a paleta do tema (um papel novo nela, se faltar)")
            avisos += botao_esticado(n, cod)
        if re.search(r"catch\s*\([^)]*\)\s*\{\s*\}|except[^:]*:\s*pass\b", cod) or (ext in PYTHON and re.match(r"except\s*:", s)):
            erros.append(f"linha {n}: erro engolido; registre no log ou trate")
        if not re.search(r"\b(const val|val [A-Z_][A-Z0-9_]*\s*[:=]|[A-Z_][A-Z0-9_]*\s*=|import|package|@)", cod):
            numeros = [x for x in re.findall(r"(?<![\w.])-?\d+(?:\.\d+)?(?:[fFL])?\b(?!\s*\.(?:dp|sp|em|px)\b)", cod)
                       if abs(float(re.sub(r"[fFL]$", "", x))) not in (0, 1, 2, 0.5, 100)]
            if numeros and not re.search(r"\.(dp|sp)\b|Color\(|Offset\(|Size\(|padding|alpha|weight", cod):
                avisos.append(f"linha {n}: número solto ({', '.join(numeros[:3])}); se é regra (tempo, limite, nível), "
                              "vira constante com nome")

    # when que cresce por tipo, nas linhas novas
    if ext in KOTLIN and not teste:
        ramos = [n for n in novas if n <= len(linhas) and re.match(r"^\s*is\s+[A-Z]\w*.*->", linhas[n - 1])]
        if len(ramos) >= lim["ramos_por_tipo"]:
            erros.append(f"when com {len(ramos)} ramos 'is Tipo ->': o tipo novo deveria entrar por polimorfismo "
                         "(método na interface ou registro), não por mais um ramo")
    return erros, avisos


def main():
    if "--hook" in sys.argv:
        try:
            caminho = json.load(sys.stdin).get("tool_input", {}).get("file_path", "")
        except ValueError:
            return 0
    else:
        caminho = sys.argv[1] if len(sys.argv) > 1 else ""
    try:
        erros, avisos = confere(caminho)
    except Exception as e:  # o hook nunca trava o trabalho
        print(f"confere.py falhou em {caminho}: {e}", file=sys.stderr)
        return 0
    if "--hook" in sys.argv:
        if erros:
            print(f"Código limpo ({caminho}): conserte agora, enquanto o arquivo está na mão:", file=sys.stderr)
            for e in erros + avisos:
                print(f"  - {e}", file=sys.stderr)
            return 2
        if avisos:
            print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse",
                                                     "additionalContext": "Avisos de código limpo: " + "; ".join(avisos)}}))
        return 0
    for e in erros:
        print(f"ERRO   {e}")
    for a in avisos:
        print(f"AVISO  {a}")
    return 1 if erros else 0


if __name__ == "__main__":
    sys.exit(main())
