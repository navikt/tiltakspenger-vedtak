val felleslibVersion = "0.0.229"
val kotestVersion = "5.9.1"
val kotlinxCoroutinesVersion = "1.9.0"

dependencies {
    api(project(":domene"))
    api(project(":app"))
    api("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:personklient-domene:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:person-dtos:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:personklient-infrastruktur:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    api("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")
    api("io.arrow-kt:arrow-core:1.2.4")
    api("io.kotest:kotest-assertions-core:$kotestVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}
