plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

val pureKotlin = listOf("**/engine/**", "**/learning/**", "**/content/**")

kotlin {
    jvmToolchain(21)
    sourceSets {
        main {
            kotlin.srcDir("../composeApp/src/commonMain/kotlin")
            kotlin.include(pureKotlin)
        }
        test {
            kotlin.srcDir("../composeApp/src/commonTest/kotlin")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("failed"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
}
