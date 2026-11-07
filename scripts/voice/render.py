#!/usr/bin/env python3
"""Renderiza as falas do jogo com o Kokoro e grava áudio e boca sincronizada.

Entrada: build/voice-lines.tsv (id, personagem, texto), gerado pelo teste VoiceExportTest.
Saída, em composeResources/files/voice:
  <id>.ogg   a fala
  index.txt  uma linha por fala: id duração_ms tempo:boca,tempo:boca,...

O Kokoro devolve a duração de cada fonema. É dessa duração que sai a boca: cada fonema
vira uma das 7 formas (ver Rig.kt), como o Duolingo faz com visemas.

Falas já renderizadas são puladas: o id muda quando o texto muda.
Ver scripts/voice/README.md para preparar o ambiente.
"""
import argparse
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np
import soundfile as sf

from misaki import espeak as _espeak  # noqa: F401  (precisa vir antes do ajuste abaixo)
from phonemizer.backend.espeak.wrapper import EspeakWrapper

# O pacote espeakng_loader aponta para um caminho que só existe na máquina onde foi compilado.
if os.path.exists('/usr/lib64/libespeak-ng.so.1'):
    EspeakWrapper.set_library('/usr/lib64/libespeak-ng.so.1')
    EspeakWrapper.set_data_path('/usr/share/espeak-ng-data')

from kokoro import KPipeline  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
VOZES = {'BYTE': 'pm_santa', 'NARRADOR': 'pm_santa', 'PROF': 'pf_dora', 'LEXI': 'pf_dora', 'PILHA': 'pm_alex'}
AMOSTRAS = 24000
QUADRO = 600  # uma unidade de pred_dur são 600 amostras (25 ms)

# 0 fechada, 1 A, 2 E, 3 O, 4 F, 5 L/T, 6 CH
BOCA = {}
# O espeak junta ditongos numa letra só: I é "ai", W é "au", A é "ei", O é "ou", Y é "ói".
for chars, forma in (
    ('mbp', 0), ('aæɐIW', 1), ('eɛiɪjyA', 2), ('oɔuʊwOY', 3), ('fv', 4), ('tdnlszɾrhxkɡgɲʎŋ', 5), ('ʃʒʧʤ', 6),
):
    for c in chars:
        BOCA[c] = forma


def lexico():
    """lexicon.tsv: termo<TAB>como falar. Termos com letras maiúsculas diferenciam caixa."""
    regras = []
    for linha in (Path(__file__).parent / 'lexicon.tsv').read_text(encoding='utf8').splitlines():
        if not linha.strip() or linha.startswith('#'):
            continue
        de, para = linha.split('\t')[:2]
        regras.append((re.compile(r'(?<![\w$])' + re.escape(de) + r'(?![\w])'), para))
    regras.sort(key=lambda r: -len(r[0].pattern))
    return regras


def falar(texto, regras):
    t = texto.replace('**', '').replace('`', '')
    # E e A maiúsculos no meio da frase são letras; no começo são conjunção e artigo.
    # Grafia "éh": o espeak lê "ê" sozinho como "e circunflexo".
    t = re.sub(r'(?<=[a-zà-ÿ,] )E(?=[ ,.?!]|$)', 'éh', t)
    t = re.sub(r'(?<=[a-zà-ÿ,] )A(?=[ ,.?!]|$)', 'á', t)
    for rx, para in regras:
        t = rx.sub(para, t)
    return t


def trilha(fonemas, duracoes, inicio_ms):
    """Transforma fonemas e durações em (ms, boca), juntando repetições."""
    saida = []
    t = duracoes[0] * QUADRO  # o primeiro valor é a borda de início
    for ch, d in zip(fonemas, duracoes[1:-1]):
        forma = BOCA.get(ch)
        if forma is None:
            forma = 0 if ch in ' .,!?;:—' else None
        if forma is not None and (not saida or saida[-1][1] != forma):
            saida.append((inicio_ms + int(t * 1000 / AMOSTRAS), forma))
        t += d * QUADRO
    return saida


def render(pipe, texto, voz):
    partes, marcas, total = [], [], 0
    for r in pipe(texto, voice=voz, speed=1.0):
        a = r.audio.numpy() if hasattr(r.audio, 'numpy') else np.asarray(r.audio)
        a = a.astype(np.float32)
        dur = r.pred_dur.tolist() if hasattr(r.pred_dur, 'tolist') else list(r.pred_dur)
        marcas += trilha(r.phonemes, dur, int(total * 1000 / AMOSTRAS))
        partes.append(a)
        total += len(a)
    return np.concatenate(partes), marcas


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--tsv', default=str(ROOT / 'composeApp/build/voice-lines.tsv'))
    ap.add_argument('--out', default=str(ROOT / 'composeApp/src/commonMain/composeResources/files/voice'))
    ap.add_argument('--qualidade', default='1.5', help='qualidade do Vorbis (-1 a 10)')
    ap.add_argument('--limpar', action='store_true', help='apaga áudios que nenhuma fala usa mais')
    ap.add_argument('--refazer', help='regex: renderiza de novo as falas que casam (ex.: depois de mudar o lexicon)')
    a = ap.parse_args()

    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    linhas = [l.rstrip('\n').split('\t') for l in open(a.tsv, encoding='utf8') if l.strip()]
    regras = lexico()
    velho = {}
    idx = out / 'index.txt'
    if idx.exists():
        for l in idx.read_text().splitlines():
            if l.strip():
                velho[l.split(' ')[0]] = l

    refazer = re.compile(a.refazer) if a.refazer else None
    faltam = [l for l in linhas if l[0] not in velho or not (out / f'{l[0]}.ogg').exists() or (refazer and refazer.search(l[2]))]
    print(f'{len(linhas)} falas, {len(faltam)} para renderizar', flush=True)
    pipe = KPipeline(lang_code='p', repo_id='hexgrad/Kokoro-82M') if faltam else None
    with tempfile.TemporaryDirectory() as tmp:
        for k, (fid, quem, texto) in enumerate(faltam):
            audio, marcas = render(pipe, falar(texto, regras), VOZES.get(quem, 'pm_santa'))
            w = Path(tmp) / 'x.wav'
            sf.write(w, audio, AMOSTRAS)
            subprocess.run(
                ['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-i', str(w),
                 '-af', 'loudnorm=I=-18:TP=-1.5:LRA=7', '-ar', str(AMOSTRAS), '-ac', '1',
                 '-c:a', 'libvorbis', '-q:a', a.qualidade, str(out / f'{fid}.ogg')],
                check=True,
            )
            ms = int(len(audio) * 1000 / AMOSTRAS)
            velho[fid] = f"{fid} {ms} " + ','.join(f'{t}:{b}' for t, b in marcas)
            if k % 20 == 0:
                print(f'  {k + 1}/{len(faltam)}', flush=True)
                # Grava o índice aos poucos: se o processo morrer (falta de memória), a próxima rodada continua daqui.
                idx.write_text('\n'.join(velho[i] for i in sorted(velho)) + '\n')

    usados = {l[0] for l in linhas}
    idx.write_text('\n'.join(velho[i] for i in sorted(usados) if i in velho) + '\n')
    if a.limpar:
        for f in out.glob('*.ogg'):
            if f.stem not in usados:
                f.unlink()
    tam = sum(f.stat().st_size for f in out.glob('*.ogg'))
    print(f'pronto: {len(usados)} falas, {tam / 1e6:.1f} MB')


if __name__ == '__main__':
    sys.exit(main())
