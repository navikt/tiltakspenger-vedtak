val jacksonVersion = "2.17.1"
val kotestVersion = "5.9.0"
val mockkVersion = "1.13.11"
val felleslibVersion = "0.0.120"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.aallam.ulid:ulid-kotlin:1.3.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("io.micrometer:micrometer-core:1.13.0")
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")

    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    testImplementation(kotlin("test"))
    testImplementation(project(":common"))
}
