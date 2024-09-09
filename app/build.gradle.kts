val ktorVersion = "2.3.12"
val kotestVersion = "5.9.1"
val mockkVersion = "1.13.12"
val testContainersVersion = "1.20.1"
val felleslibVersion = "0.0.211"
val tokenSupportVersion = "3.2.0"
val iverksettVersjon = "1.0_20240829083905_36ac417"

plugins {
    application
}

application {
    mainClass.set("no.nav.tiltakspenger.vedtak.AppKt")
}

dependencies {
    implementation(project(":domene"))
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    implementation("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:person-dtos:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:skjerming-dtos:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:dokument-dtos:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:periodisering:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:personklient-domene:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:personklient-infrastruktur:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:persistering-infrastruktur:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:jobber:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    implementation("com.github.navikt.tiltakspenger-libs:tiltak-dtos:$felleslibVersion")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-utils:$ktorVersion")
    implementation("com.github.navikt:rapids-and-rivers:2024082313161724411773.1db923f4402d")
    // R&R bruker prometheus direkte men mangler dependency. Tidligere var den tatt inn via micrometer, men de har oppdatert til v1 som er inkompatibel med dagens kode i R&R
    implementation("io.prometheus:simpleclient:0.16.0")

    implementation("com.natpryce:konfig:1.6.10.0")

    // Http
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-http:$ktorVersion")

    // Auth
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("com.auth0:jwks-rsa:0.22.1")

    // TokenX
    implementation("no.nav.security:token-validation-ktor-v2:$tokenSupportVersion")
    implementation("no.nav.security:token-client-core:$tokenSupportVersion")

    // DB
    implementation("org.flywaydb:flyway-database-postgresql:10.17.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.github.seratch:kotliquery:1.9.0")

    // Helved/Utsjekk/Utbetaling
    implementation("no.nav.utsjekk.kontrakter:iverksett:$iverksettVersjon")

    // DIV
    implementation("io.arrow-kt:arrow-core:1.2.4")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-dsl-jvm:$mockkVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    // need quarkus-junit-4-mock because of https://github.com/testcontainers/testcontainers-java/issues/970
    testImplementation("io.quarkus:quarkus-junit4-mock:3.14.2")
    testImplementation(project(":test-common"))
    testApi("com.github.navikt.tiltakspenger-libs:test-common:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:common:$felleslibVersion")
    testImplementation("com.github.navikt.tiltakspenger-libs:persistering-domene:$felleslibVersion")
}
