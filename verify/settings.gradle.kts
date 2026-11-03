// Build auxiliar: compila e testa SÓ a lógica em Kotlin puro (motor de compiladores,
// modelo de aprendizagem e conteúdo) na JVM, sem Android/Compose.
// Útil em máquinas sem Android SDK:  cd verify && ../gradlew test
rootProject.name = "compilandia-verify"
pluginManagement { repositories { mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { mavenCentral() } }
