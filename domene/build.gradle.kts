val jacksonVersion = "2.18.0"
val kotestVersion = "5.9.1"
val mockkVersion = "1.13.12"
val felleslibVersion = "0.0.229"
val kotlinxCoroutinesVersion = "1.9.0"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.aallam.ulid:ulid-kotlin:1.3.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")

    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:personklient-domene:$felleslibVersion")

    implementation("io.arrow-kt:arrow-core:1.2.4")

    testImplementation(platform("org.junit:junit-bom:5.11.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("ch.qos.logback:logback-classic:1.5.8")

    testImplementation(kotlin("test"))
    testImplementation(project(":test-common"))
    testApi("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
}
