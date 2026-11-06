#!/usr/bin/env python3
"""Tira dos arquivos de textos as chaves que nenhum código usa mais. Uso: scripts/textos/podar.py [--plano]"""
import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent.parent
FONTES = RAIZ / "composeApp/src/commonMain/kotlin"
TEXTOS = RAIZ / "composeApp/src/commonMain/textos"
usadas = set()
for f in FONTES.rglob("*.kt"):
    if "/textos/" in f.as_posix():
        continue
    usadas |= set(re.findall(r'\btx\("([a-z0-9_]+)"', f.read_text(encoding="utf-8")))
tiradas = []
for x in sorted(TEXTOS.glob("*/*.xml")):
    linhas = x.read_text(encoding="utf-8").splitlines()
    fica = []
    for l in linhas:
        m = re.match(r'\s*<string name="([a-z0-9_]+)"', l)
        if m and m.group(1) not in usadas:
            tiradas.append(f"{x.parent.name}/{x.name}: {m.group(1)}")
            continue
        fica.append(l)
    # comentário de arquivo que ficou sem nenhuma string embaixo
    limpo = [l for i, l in enumerate(fica) if not (l.strip().startswith("<!-- ") and l.strip().endswith(".kt -->") and (i + 1 >= len(fica) or not fica[i + 1].strip().startswith("<string")))]
    if "--plano" not in sys.argv:
        x.write_text("\n".join(limpo) + "\n", encoding="utf-8")
print("\n".join(tiradas) or "nada a podar")
