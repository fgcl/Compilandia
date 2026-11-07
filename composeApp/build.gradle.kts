import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// Textos do jogo: textos/<idioma>/*.xml viram mapas Kotlin. O código lê com tx("chave").
apply(from = "../gradle/textos.gradle.kts")

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    // Versão web: Kotlin/Wasm no navegador. ./gradlew :composeApp:wasmJsBrowserDistribution
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("compilandia")
        browser {
            commonWebpackConfig { outputFileName = "compilandia.js" }
            // Os testes rodam na JVM (desktopTest): o motor é o mesmo. No navegador eles pediriam um Chrome no CI.
            testTask { enabled = false }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        commonMain { kotlin.srcDir(tasks.named("gerarTextos")) }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // O .jar do desktop leva o Skia de todas as plataformas: o mesmo arquivo roda em Windows, Mac e Linux.
            implementation(compose.desktop.windows_x64)
            implementation(compose.desktop.macos_x64)
            implementation(compose.desktop.macos_arm64)
            implementation(compose.desktop.linux_x64)
            implementation(compose.desktop.linux_arm64)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.vorbisspi)
        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.compilandia.resources"
    generateResClass = always
}

android {
    namespace = "com.compilandia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.compilandia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.compilandia.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Msi, TargetFormat.Dmg)
            packageName = "Compilandia"
            packageVersion = "1.0.0"
        }
    }
}

/**
 * Quantas JVMs de teste rodam juntas. Cada uma renderiza com Skia e ocupa perto de 1,3 GB;
 * com memória curta o earlyoom mata o daemon do Gradle. Fica com 2,5 GB de folga para o resto da máquina.
 */
fun forksQueCabem(): Int {
    val nucleos = (Runtime.getRuntime().availableProcessors() / 3).coerceAtLeast(1)
    val meminfo = File("/proc/meminfo")
    if (!meminfo.exists()) return nucleos.coerceAtMost(2)
    val linha = meminfo.readLines().firstOrNull { l -> l.startsWith("MemAvailable:") } ?: return nucleos.coerceAtMost(2)
    val livreMb: Long = linha.split(Regex("\\s+"))[1].toLong() / 1024
    val porMemoria = ((livreMb - 2500) / 1300).toInt()
    return minOf(nucleos, porMemoria, 5).coerceAtLeast(1)
}

tasks.withType<Test>().configureEach {
    // Cada classe de teste roda numa JVM; o Gradle só paraleliza entre classes, por isso as pesadas
    // (episódios, aulas, fluxo) estão divididas. -Pforks=1 volta a rodar tudo numa JVM só.
    maxParallelForks = (findProperty("forks") as String?)?.toIntOrNull() ?: forksQueCabem()
    // O limite padrão do runTest é 60 s de relógio para o teste inteiro.
    systemProperty("kotlinx.coroutines.test.default_timeout", "10m")
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// S2: o jogo em inglês. As aulas são montadas uma vez por processo,
// então o teste em inglês roda numa JVM própria, logo depois do desktopTest.
val desktopTestIngles by tasks.registering(Test::class) {
    description = "Renderiza telas e aulas em inglês numa JVM separada."
    group = "verification"
    val base = tasks.named<Test>("desktopTest")
    testClassesDirs = base.get().testClassesDirs
    classpath = base.get().classpath
    filter { includeTestsMatching("*InglesTest*") }
    environment("IDIOMA", "en")
    dependsOn("desktopTestClasses")
}
tasks.named("desktopTest") { finalizedBy(desktopTestIngles) }
