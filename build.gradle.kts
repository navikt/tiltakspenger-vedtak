val javaVersion = JavaVersion.VERSION_17

plugins {
    kotlin("jvm") version "1.7.21"
    id("io.gitlab.arturbosch.detekt").version("1.21.0")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
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
            // https://phauer.com/2018/best-practices-unit-testing-kotlin/
            systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
        }
    }
    configurations.all {
        // exclude JUnit 4
        exclude(group = "junit", module = "junit")
    }
}
