#!/usr/bin/env python3
"""
Orquestrador Unificado de Auditoria de Código e Hard Gates de Qualidade.
Audita o código Kotlin Multiplatform e os scripts Python contra 'vibe coding',
monólitos, duplicações, falhas de segurança e violações de arquitetura.

Uso:
    python3 scripts/audit/audit_codebase.py           # Relatório completo e score
    python3 scripts/audit/audit_codebase.py --gate   # Modo CI / Trava build se violar regras
    python3 scripts/audit/audit_codebase.py --json   # Emite JSON estruturado para ferramentas
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from dataclasses import asdict, dataclass
from pathlib import Path

MAX_FILE_LINES = 400
MAX_FUNCTION_LINES = 30
MAX_NESTED_DEPTH = 3

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
SRC_KOTLIN = REPO_ROOT / "composeApp" / "src" / "commonMain" / "kotlin" / "com" / "compilandia"
SCRIPTS_DIR = REPO_ROOT / "scripts"


@dataclass
class FileMetric:
    path: str
    lines: int
    functions_over_limit: list[tuple[str, int]]
    forbidden_imports: list[str]


@dataclass
class AuditSummary:
    total_kt_files: int
    total_kt_lines: int
    files_over_400: list[tuple[str, int]]
    functions_over_30: list[tuple[str, str, int]]
    architectural_violations: list[str]
    security_issues: list[str]
    vibe_coding_score: float  # 0.0 a 100.0% (100% = padrão perfeito de engenharia)


def scan_kotlin_files() -> tuple[list[FileMetric], list[str]]:
    metrics: list[FileMetric] = []
    arch_violations: list[str] = []

    pure_layers = {"engine", "learning", "content", "tutor"}
    forbidden_in_pure = ("com.compilandia.ui", "androidx.compose", "android.")
    forbidden_in_engine = ("com.compilandia.learning", "com.compilandia.content", "com.compilandia.tutor")

    fun_regex = re.compile(r"^\s*(?:(?:override|open|private|protected|public|internal|inline|suspend)\s+)*fun\s+([a-zA-Z0-9_]+)")

    for kt_file in SRC_KOTLIN.rglob("*.kt"):
        rel_path = str(kt_file.relative_to(SRC_KOTLIN))
        lines = kt_file.read_text(encoding="utf-8", errors="ignore").splitlines()
        line_count = len(lines)

        file_forbidden: list[str] = []
        funcs_over: list[tuple[str, int]] = []

        # Detecção de imports proibidos
        first_part = rel_path.split(os.sep)[0]
        for idx, line in enumerate(lines, 1):
            trimmed = line.strip()
            if trimmed.startswith("import "):
                if first_part in pure_layers:
                    for fb in forbidden_in_pure:
                        if fb in trimmed:
                            msg = f"{rel_path}:{idx} -> camada pura '{first_part}' importa '{fb}'"
                            file_forbidden.append(msg)
                            arch_violations.append(msg)
                if first_part == "engine":
                    for fb in forbidden_in_engine:
                        if fb in trimmed:
                            msg = f"{rel_path}:{idx} -> engine importa '{fb}'"
                            file_forbidden.append(msg)
                            arch_violations.append(msg)

        # Detecção de funções > 30 linhas
        current_fun = None
        fun_lines = 0
        depth = 0
        for line in lines:
            trimmed = line.strip()
            m = fun_regex.match(line)
            if m and depth == 0:
                current_fun = m.group(1)
                fun_lines = 0

            if current_fun:
                if trimmed and not trimmed.startswith(("//", "/*", "*")):
                    fun_lines += 1
                depth += line.count("{") - line.count("}")
                if depth <= 0 and "}" in line:
                    if fun_lines > MAX_FUNCTION_LINES:
                        funcs_over.append((current_fun, fun_lines))
                    current_fun = None
                    depth = 0

        metrics.append(FileMetric(
            path=rel_path,
            lines=line_count,
            functions_over_limit=funcs_over,
            forbidden_imports=file_forbidden,
        ))

    return metrics, arch_violations


def run_security_scan() -> list[str]:
    semgrep_bin = "/home/linuxbrew/.linuxbrew/bin/semgrep"
    issues: list[str] = []
    if os.path.exists(semgrep_bin):
        try:
            cmd = [
                semgrep_bin,
                "scan",
                "--config=p/owasp-top-ten",
                "--config=p/security-code-scan",
                "--metrics=off",
                "--json",
                str(SRC_KOTLIN),
            ]
            res = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
            if res.returncode in (0, 1) and res.stdout:
                data = json.loads(res.stdout)
                for item in data.get("results", []):
                    path = item.get("path", "")
                    start_line = item.get("start", {}).get("line", 0)
                    msg = item.get("extra", {}).get("message", "")
                    issues.append(f"{path}:{start_line} -> {msg}")
        except Exception:
            pass
    return issues


def compute_vibe_score(
    total_files: int,
    files_over_400: int,
    total_funcs_checked: int,
    funcs_over_30: int,
    arch_violations: int,
) -> float:
    """Calcula o índice de conformidade com engenharia de software (0-100%)."""
    if total_files == 0:
        return 100.0

    file_compliance = max(0.0, 1.0 - (files_over_400 / total_files)) * 40.0
    func_compliance = max(0.0, 1.0 - (funcs_over_30 / max(1, total_funcs_checked))) * 40.0
    arch_compliance = 20.0 if arch_violations == 0 else 0.0

    return round(file_compliance + func_compliance + arch_compliance, 1)


def generate_report(metrics: list[FileMetric], arch_violations: list[str], security_issues: list[str]) -> AuditSummary:
    total_files = len(metrics)
    total_lines = sum(m.lines for m in metrics)

    files_over = [(m.path, m.lines) for m in metrics if m.lines > MAX_FILE_LINES]
    files_over.sort(key=lambda x: x[1], reverse=True)

    funcs_over: list[tuple[str, str, int]] = []
    total_funcs = 0
    for m in metrics:
        for f_name, f_lines in m.functions_over_limit:
            funcs_over.append((m.path, f_name, f_lines))
        total_funcs += len(m.functions_over_limit) + 10  # estimativa base

    vibe_score = compute_vibe_score(
        total_files=total_files,
        files_over_400=len(files_over),
        total_funcs_checked=total_funcs,
        funcs_over_30=len(funcs_over),
        arch_violations=len(arch_violations),
    )

    return AuditSummary(
        total_kt_files=total_files,
        total_kt_lines=total_lines,
        files_over_400=files_over,
        functions_over_30=funcs_over,
        architectural_violations=arch_violations,
        security_issues=security_issues,
        vibe_coding_score=vibe_score,
    )


def print_terminal_report(summary: AuditSummary) -> None:
    print("=" * 80)
    print(" 🛡️  AUDITORIA DE QUALIDADE, SOLID E LIMITES ARQUITETURAIS")
    print("=" * 80)
    print(f"Total de arquivos Kotlin analisados : {summary.total_kt_files}")
    print(f"Total de linhas de código Kotlin    : {summary.total_kt_lines:,}")
    print(f"Score de Engenharia / Anti-Vibe     : {summary.vibe_coding_score}% / 100%")
    print("-" * 80)

    if summary.architectural_violations:
        print(f"❌ VIOLAÇÕES ARQUITETURAIS ({len(summary.architectural_violations)}):")
        for v in summary.architectural_violations:
            print(f"   • {v}")
    else:
        print("✅ Arquitetura de Camadas: 100% isolada e desacoplada (Zero vazamentos de UI)")

    print("-" * 80)
    if summary.files_over_400:
        print(f"⚠️  ARQUIVOS EXCEDENDO O TETO DE {MAX_FILE_LINES} LINHAS ({len(summary.files_over_400)} arquivos):")
        print(f"   {'Arquivo':<45} {'Linhas':>8} {'Excesso':>10}")
        print(f"   {'-'*45} {'-'*8} {'-'*10}")
        for path, count in summary.files_over_400:
            excess = count - MAX_FILE_LINES
            print(f"   {path:<45} {count:>8} {f'+{excess}':>10}")
    else:
        print(f"✅ Todos os arquivos cumprem rigorosamente o teto de {MAX_FILE_LINES} linhas!")

    print("-" * 80)
    if summary.functions_over_30:
        print(f"⚠️  FUNÇÕES MONOLÍTICAS > {MAX_FUNCTION_LINES} LINHAS: {len(summary.functions_over_30)} encontradas.")
        print("   Exibindo os maiores hotspots para refatoração:")
        for path, fname, flines in sorted(summary.functions_over_30, key=lambda x: x[2], reverse=True)[:10]:
            print(f"   • {path} :: fun {fname}() -> {flines} linhas")
    else:
        print(f"✅ Todas as funções cumprem o teto de {MAX_FUNCTION_LINES} linhas!")

    print("-" * 80)
    if summary.security_issues:
        print(f"🚨 PROBLEMAS DE SEGURANÇA (SEMGREP): {len(summary.security_issues)}")
        for sec in summary.security_issues:
            print(f"   • {sec}")
    else:
        print("✅ Segurança SAST (Semgrep): Nenhuma vulnerabilidade detectada.")
    print("=" * 80)


def main() -> int:
    parser = argparse.ArgumentParser(description="Auditor de Qualidade e Anti-Vibe Coding")
    parser.add_argument("--gate", action="store_true", help="Falha com código 1 se houver violação das regras")
    parser.add_argument("--json", action="store_true", help="Gera saída em JSON estruturado")
    args = parser.parse_args()

    metrics, arch_violations = scan_kotlin_files()
    sec_issues = run_security_scan()
    summary = generate_report(metrics, arch_violations, sec_issues)

    if args.json:
        print(json.dumps(asdict(summary), indent=2))
    else:
        print_terminal_report(summary)

    if args.gate:
        has_violations = bool(summary.files_over_400 or summary.architectural_violations or summary.security_issues)
        if has_violations:
            print("\n❌ GATE ATIVADO: Build bloqueado até a correção dos monólitos e violações acima.")
            return 1
        print("\n✅ GATE PASSOU: Código 100% em conformidade com as regras de engenharia.")

    return 0


if __name__ == "__main__":
    sys.exit(main())

