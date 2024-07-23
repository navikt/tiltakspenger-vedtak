val felleslibVersion = "0.0.156"

dependencies {
    implementation(project(":domene"))
    implementation(project(":app"))
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("io.arrow-kt:arrow-core:1.2.4")
}
