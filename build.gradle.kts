val javaVersion = JavaVersion.VERSION_17

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("io.gitlab.arturbosch.detekt").version("1.20.0")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
    }
//    apply(plugin = "io.gitlab.arturbosch.detekt")
//    detekt {
//        buildUponDefaultConfig = true
//        allRules = false
//        config = files("$rootDir/config/detekt.yml")
//    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = javaVersion.toString()
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = javaVersion.toString()
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
        test {
            // JUnit 5 support
            useJUnitPlatform()
        }
    }
    configurations.all {
        // exclude JUnit 4
        exclude(group = "junit", module = "junit")
    }
}
