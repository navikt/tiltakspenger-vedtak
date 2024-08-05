val felleslibVersion = "0.0.171"

dependencies {
    implementation(project(":domene"))
    implementation(project(":app"))
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    implementation("io.arrow-kt:arrow-core:1.2.4")
}
