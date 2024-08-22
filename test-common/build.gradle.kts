val felleslibVersion = "0.0.196"

dependencies {
    implementation(project(":domene"))
    implementation(project(":app"))
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
    implementation("io.arrow-kt:arrow-core:1.2.4")
}
