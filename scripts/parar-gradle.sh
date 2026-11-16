#!/usr/bin/env bash
# Encerra só os daemons do Gradle deste projeto (AGENTS.md, "Gradle").
# Nunca use ./gradlew --stop: ele derruba o Gradle de todas as sessões abertas na máquina.
# A linha de comando do daemon não diz o projeto, mas o log dele registra "currentDir=" de cada build.
# Daemon que também rodou build de outro projeto fica vivo.
# Daemon trabalhando agora (gastou CPU nos últimos 2 s) também fica: pode ser o build de outra sessão deste projeto.
set -u
projeto=$(cd "$(dirname "$0")/.." && pwd)
casa=${GRADLE_USER_HOME:-$HOME/.gradle}
encerrados=0
cpu() { awk '{ print $14 + $15 }' "/proc/$1/stat" 2>/dev/null || echo 0; }
declare -A antes
pids=$(pgrep -f 'org\.gradle\.launcher\.daemon\.bootstrap\.GradleDaemon')
for pid in $pids; do antes[$pid]=$(cpu "$pid"); done
[ -n "$pids" ] && sleep 2
ocupados=0
for pid in $pids; do
    log=$(ls "$casa"/daemon/*/daemon-"$pid".out.log 2>/dev/null | head -1)
    [ -n "$log" ] || continue
    if grep -oE 'currentDir=[^,}]+' "$log" | sort -u | awk -v p="currentDir=$projeto" '
        { n++; if (index($0, p) != 1 || (length($0) > length(p) && substr($0, length(p) + 1, 1) != "/")) outro = 1 }
        END { exit (n == 0 || outro) }'; then
        if [ $(( $(cpu "$pid") - ${antes[$pid]:-0} )) -gt 20 ]; then
            ocupados=$((ocupados + 1))
        else
            kill "$pid" && encerrados=$((encerrados + 1))
        fi
    fi
done
echo "daemons de Compilandia encerrados: $encerrados; trabalhando agora, deixados vivos: $ocupados"
