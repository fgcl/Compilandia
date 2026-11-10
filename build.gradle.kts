plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

tasks.register<Exec>("audit") {
    group = "verification"
    description = "Executa auditoria de qualidade, hotspots, SOLID e limites arquiteturais."
    commandLine("python3", "scripts/audit/audit_codebase.py")
}

tasks.register<Exec>("gate") {
    group = "verification"
    description = "Trava o build se houver arquivos > 400 linhas, funções monolíticas ou quebra de arquitetura."
    commandLine("python3", "scripts/audit/audit_codebase.py", "--gate")
}

