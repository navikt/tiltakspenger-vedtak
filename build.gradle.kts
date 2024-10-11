val javaVersion = JavaVersion.VERSION_21

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.diffplug.spotless") version "6.25.0"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://packages.confluent.io/maven/")
        maven {
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_max-line-length" to "off",
                    ),
                )
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

            if (javaVersion.isCompatibleWith(JavaVersion.VERSION_21)) {
                // https://github.com/mockito/mockito/issues/3037#issuecomment-1588199599
                jvmArgs("-XX:+EnableDynamicAgentLoading")
            }
        }
    }
    configurations.all {
        // exclude JUnit 4
        exclude(group = "junit", module = "junit")
    }
}
tasks.register<Copy>("gitHooks") {
    from(file(".scripts/pre-commit"))
    into(file(".git/hooks"))
}
tasks.named("build") {
    dependsOn("gitHooks")
}
tasks.register("checkFlywayMigrationNames") {
    doLast {
        val migrationDir = project.file("app/src/main/resources/db/migration")
        val invalidFiles = migrationDir.walk()
            .filter { it.isFile && it.extension == "sql" }
            .filterNot { it.name.matches(Regex("V[0-9]+__[\\w]+\\.sql")) }
            .map { it.name }
            .toList()

        if (invalidFiles.isNotEmpty()) {
            throw GradleException("Invalid migration filenames:\n${invalidFiles.joinToString("\n")}")
        } else {
            println("All migration filenames are valid.")
        }
    }
}

tasks.named("check") {
    dependsOn("checkFlywayMigrationNames")
}
