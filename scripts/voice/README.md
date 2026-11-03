# Vozes do jogo

As falas são geradas antes e guardadas no APK. O jogo não leva modelo de IA e funciona offline.

## Preparar o ambiente (uma vez)

```bash
uv venv --python 3.12 ~/.cache/compilandia-voz
. ~/.cache/compilandia-voz/bin/activate
uv pip install torch --index-url https://download.pytorch.org/whl/cpu
uv pip install kokoro soundfile numpy faster-whisper
```

Precisa também de `ffmpeg` e `espeak-ng` no sistema. Neste computador, o ambiente já pronto fica em `~/.local/share/pluginbot-voz`.

## Depois de mudar qualquer fala

```bash
./gradlew :composeApp:desktopTest --tests '*VoiceExportTest*'   # gera build/voice-lines.tsv
python3 scripts/text_lint.py                                    # frase longa, vício de IA, gramática
python scripts/voice/render.py --limpar                         # só renderiza o que mudou
python scripts/voice/qa.py                                      # escuta tudo com o Whisper
```

`VoiceCoverageTest` falha se alguma fala ficou sem áudio. Palavra mal pronunciada se corrige em `lexicon.tsv`.

## Vozes

| Personagem | Voz |
|---|---|
| Byte, narrador | `pm_santa` |
| Profª Gramática | `pf_dora` |

Modelo: [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M), licença Apache 2.0.
