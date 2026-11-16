#!/bin/bash
# Erros do celular (AGENTS.md, "Celular"): lê pelo adb os travamentos do app com.compilandia e anota os novos em
# docs/ERROS_DO_CELULAR.md, com a data e o começo do erro. Erro já anotado não se repete.
# Só lê: não instala, não toca na tela e não mexe no save.
# uso: tools/erros-do-celular.sh            (com mais de um aparelho: ANDROID_SERIAL=<serial> tools/erros-do-celular.sh)
cd "$(dirname "$0")/.." || exit 1
PACOTE="com.compilandia"
[ -n "$PACOTE" ] && [ "${PACOTE#\{}" = "$PACOTE" ] || { echo "erros-do-celular: falta o pacote do app (kit define PACOTE=com.exemplo.app e kit atualiza)"; exit 2; }
ARQ=docs/ERROS_DO_CELULAR.md
command -v adb >/dev/null || { echo "erros-do-celular: adb não instalado"; exit 2; }
serial=${ANDROID_SERIAL:-$(adb devices | awk 'NR > 1 && $2 == "device" {print $1; exit}')}
[ -n "$serial" ] || { echo "erros-do-celular: nenhum celular no adb"; exit 2; }
[ -f "$ARQ" ] || printf '# Erros do celular\n\nTravamentos do app lidos pelo adb (tools/erros-do-celular.sh), o mais novo no topo.\nQuando um for consertado, troque "aberto" por "consertado (commit)".\n\n' > "$ARQ"
adb -s "$serial" logcat -b crash -d -v time 2>/dev/null | python3 -I -c '
import hashlib, re, sys
pacote, arq = sys.argv[1], sys.argv[2]
texto = sys.stdin.read()
blocos = re.split(r"\n(?=\S+ \S+ E/AndroidRuntime\(\s*\d+\): FATAL EXCEPTION)", texto)
antigos = open(arq, encoding="utf-8").read()
novos = []
for b in blocos:
    if "FATAL EXCEPTION" not in b or ("Process: " + pacote + ",") not in b:
        continue
    linhas = [re.sub(r"^\S+ \S+ E/AndroidRuntime\(\s*\d+\): ", "", l) for l in b.splitlines()]
    quando = b.split(" E/")[0]
    erro = next((l for l in linhas if re.match(r"[\w.$]+(Exception|Error)", l.strip())), linhas[0]).strip()
    pilha = [l.strip() for l in linhas if l.strip().startswith("at ")][:8]
    marca = hashlib.sha1((erro.split(":")[0] + "".join(pilha[:3])).encode()).hexdigest()[:10]
    if marca in antigos or any(marca in n for n in novos):
        continue
    novos.append(f"### {quando}: {erro[:120]}\n- marca: {marca}\n- status: aberto\n\n```\n" + "\n".join(pilha) + "\n```\n\n")
if novos:
    i = antigos.find("### ")
    topo = antigos if i < 0 else antigos[:i]
    open(arq, "w", encoding="utf-8").write(topo + "".join(reversed(novos)) + (antigos[i:] if i >= 0 else ""))
print(f"erros-do-celular: {len(novos)} erro(s) novo(s) de {pacote} anotado(s) em {arq}")
' "$PACOTE" "$ARQ"
