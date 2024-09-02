val felleslibVersion = "0.0.208"
val kotestVersion = "5.9.1"

dependencies {
    implementation(project(":domene"))
    implementation(project(":app"))
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
