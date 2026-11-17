#!/bin/bash
# Ao abrir uma sessão (hook SessionStart do Claude Code), mostra o que ficou pendente no git: mudanças sem commit, ramos
# sem merge ou já juntados, worktrees abertas e stash esquecido. Só lê; não apaga nada.
# motivo: duas sessões mexendo no mesmo projeto deixaram mudanças sem commit e ramos esquecidos (pedido do dono, 15/11/2026).
# uso: tools/pendencias.sh            (sem argumento: texto para pessoa; com --hook: JSON para o Claude Code)
cd "$(dirname "$0")/.." || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0
principal=$(git symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null | sed 's#^origin/##')
[ -n "$principal" ] || principal=$(git rev-parse --verify -q main >/dev/null && echo main || echo master)
linhas=()

sujos=$(git status --porcelain 2>/dev/null | grep -v ' \.ar-board/' | grep -vc '^?? ')
novos=$(git status --porcelain 2>/dev/null | grep -v ' \.ar-board/' | grep -c '^?? ')
[ "$sujos" -gt 0 ] && linhas+=("$sujos arquivo(s) mudado(s) sem commit em $(git branch --show-current)")
[ "$novos" -gt 0 ] && linhas+=("$novos arquivo(s) novo(s) fora do git")

ramos=$(git branch --no-merged "$principal" --format='%(refname:short) (%(committerdate:relative))' 2>/dev/null)
[ -n "$ramos" ] && linhas+=("ramos sem merge no $principal: $(echo "$ramos" | paste -sd ';' | sed 's/;/; /g')")
juntos=$(git branch --merged "$principal" --format='%(refname:short)' 2>/dev/null | grep -vx "$principal")
[ -n "$juntos" ] && linhas+=("ramos já juntados que podem ser apagados: $(echo $juntos | tr ' ' ',')")

wts=$(git worktree list --porcelain 2>/dev/null | awk '/^worktree/ {print $2}' | tail -n +2)
for w in $wts; do
    n=$(git -C "$w" status --porcelain 2>/dev/null | wc -l)
    linhas+=("worktree aberta: $w ($(git -C "$w" branch --show-current 2>/dev/null), $n arquivo(s) sem commit)")
done
stash=$(git stash list 2>/dev/null | wc -l)
[ "$stash" -gt 0 ] && linhas+=("$stash stash(es) guardado(s)")

[ ${#linhas[@]} -eq 0 ] && exit 0
texto="Pendências do git em $(basename "$PWD"): $(printf '%s; ' "${linhas[@]}")"
if [ "$1" != "--hook" ]; then printf '%s\n' "${linhas[@]}"; exit 0; fi
contexto="$texto Antes de começar, mostre isso ao dono e ofereça resolver (commit, merge, apagar o que já foi juntado). Mudança sem commit pode ser de outra sessão trabalhando agora: não faça commit, stash nem descarte dela sem perguntar."
python3 -I -c 'import json,sys; print(json.dumps({"systemMessage": sys.argv[1], "hookSpecificOutput": {"hookEventName": "SessionStart", "additionalContext": sys.argv[2]}}, ensure_ascii=False))' "$texto" "$contexto"
