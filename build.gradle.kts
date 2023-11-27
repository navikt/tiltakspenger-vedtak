val javaVersion = JavaVersion.VERSION_21

plugins {
    kotlin("jvm") version "1.9.20"
    id("com.diffplug.spotless") version "6.23.0"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            ktlint("0.48.2")
        }
    }

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
