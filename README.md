tiltakspenger-vedtak
================

Håndterer vedtak om [tiltakspenger](https://www.nav.no/no/person/arbeid/oppfolging-og-tiltak-for-a-komme-i-jobb/stonader-ved-tiltak). 

En del av satsningen ["Flere i arbeid – P4"](https://memu.no/artikler/stor-satsing-skal-fornye-navs-utdaterte-it-losninger-og-digitale-verktoy/)


# Komme i gang
## Forutsetninger
- [JDK](https://jdk.java.net/)
- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/) brukes som byggeverktøy og er inkludert i oppsettet

For hvilke versjoner som brukes, [se byggefilen](build.gradle.kts)

## Bygging og denslags
For å bygge artifaktene:

```sh
./gradlew build
```
For å gjøre spørringer mot GCP-databasene (DEV eller PROD) fra lokal maskin, må [Cloud SQL Proxy](https://cloud.google.com/sql/docs/postgres/sql-proxy) kjøre:

```sh
cloud_sql_proxy -instances=tpts-dev-6211:europe-north1:tiltakspenger-vedtak=tcp:5432 -enable_iam_login
```

## Arkitekturbeslutninger
Se [docs/adr/index.md](docs/adr/index.md)

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne, tekniske henvendelser kan sendes via Slack i kanalen #tpts-tech.
