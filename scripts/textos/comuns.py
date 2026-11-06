#!/usr/bin/env python3
"""Renomeia para comum_<...> as chaves usadas em mais de um arquivo .kt, e move o texto para textos/<idioma>/comum.xml.

Assim "Continuar" vira tx("comum_continuar"), e não fica com o nome do primeiro arquivo que a usou.
Uso: scripts/textos/comuns.py
"""
import re
from collections import defaultdict
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent.parent
FONTES = RAIZ / "composeApp/src/commonMain/kotlin"
TEXTOS = RAIZ / "composeApp/src/commonMain/textos"
CHAMADA = re.compile(r'\btx\("([a-z0-9_]+)"')
LINHA = re.compile(r'^(\s*)<string name="([a-z0-9_]+)"(.*?)>(.*)</string>\s*$')


def main():
    usos = defaultdict(set)
    arquivos = list(FONTES.rglob("*.kt"))
    for f in arquivos:
        for m in CHAMADA.finditer(f.read_text(encoding="utf-8")):
            usos[m.group(1)].add(f)
    todas = set()
    for x in TEXTOS.glob("*/*.xml"):
        for linha in x.read_text(encoding="utf-8").splitlines():
            m = LINHA.match(linha)
            if m:
                todas.add(m.group(2))
    novo = {}
    for k, fs in sorted(usos.items()):
        if len(fs) < 2 or k.startswith("comum_"):
            continue
        resto = k.split("_", 1)[1] if "_" in k else k
        base = "comum_" + resto
        n, alvo = 2, base
        while alvo in todas or alvo in novo.values():
            alvo = f"{base}_{n}"; n += 1
        novo[k] = alvo
    if not novo:
        print("nada a renomear")
        return
    # código
    for f in arquivos:
        s = f.read_text(encoding="utf-8")
        t = CHAMADA.sub(lambda m: f'tx("{novo.get(m.group(1), m.group(1))}"', s)
        if t != s:
            f.write_text(t, encoding="utf-8")
    # XML: tira dos arquivos de área e põe em comum.xml, por idioma
    for lingua in sorted(p.name for p in TEXTOS.iterdir() if p.is_dir()):
        movidas = []
        for x in sorted((TEXTOS / lingua).glob("*.xml")):
            if x.name == "comum.xml":
                continue
            saida = []
            for linha in x.read_text(encoding="utf-8").splitlines():
                m = LINHA.match(linha)
                if m and m.group(2) in novo:
                    movidas.append((novo[m.group(2)], m.group(3), m.group(4)))
                else:
                    saida.append(linha)
            x.write_text("\n".join(saida) + "\n", encoding="utf-8")
        if not movidas:
            continue
        comum = TEXTOS / lingua / "comum.xml"
        velhas = comum.read_text(encoding="utf-8").splitlines()[3:-1] if comum.exists() else []
        linhas = ['<?xml version="1.0" encoding="utf-8"?>',
                  "<!-- Textos usados em mais de um lugar do jogo. -->",
                  "<resources>"] + velhas + [f'    <string name="{k}"{attr}>{v}</string>' for k, attr, v in sorted(movidas)] + ["</resources>"]
        comum.write_text("\n".join(linhas) + "\n", encoding="utf-8")
    print(f"{len(novo)} chaves agora são comuns")


if __name__ == "__main__":
    main()
