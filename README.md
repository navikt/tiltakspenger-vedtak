tiltakspenger-saksbehandling-api
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
cloud_sql_proxy -instances=tpts-dev-6211:europe-north1:tiltakspenger-saksbehandling-api=tcp:5432 -enable_iam_login
```

### Kjøre opp appen lokalt

For å kjøre opp tiltakspenger-saksbehandling-api lokalt fra et IDE som for eksempel IntelliJ, kan man kjøre opp `main`-funksjonen 
som ligger i `App.kt` ([link](https://github.com/navikt/tiltakspenger-saksbehandling-api/blob/main/app/src/main/kotlin/no/nav/tiltakspenger/vedtak/App.kt)).

For at det skal funke å kjøre opp appen fra IntelliJ eller tilsvarende IDE må man sette opp noen miljøvariabler. I IntelliJ kan
de konfigureres opp i relevant Run Configuration som blir lagd når man kjører opp App.kt for første gang.

Miljøvariabler som må settes (be om hjelp av en annen utvikler på teamet til å få satt riktige miljøvariabler på din maskin):
```
AZURE_APP_CLIENT_ID=
AZURE_APP_CLIENT_SECRET=
AZURE_APP_WELL_KNOWN_URL=
AZURE_OPENID_CONFIG_ISSUER=
AZURE_OPENID_CONFIG_JWKS_URI=
DB_DATABASE=
DB_HOST=
DB_PASSWORD=
DB_PORT=
DB_USERNAME=
KAFKA_BROKERS=
KAFKA_CREDSTORE_PASSWORD=
KAFKA_KEYSTORE_PATH=
KAFKA_TRUSTSTORE_PATH=
NAIS_CLUSTER_NAME=
TPTS_TOPIC=
```

**OBS!** `tiltakspenger-saksbehandling-api` er avhengig av at man har en større verdikjede kjørende i miljø for å kunne kjøres opp 
lokalt, f.eks. ting som Kafka og postgres. Man anbefales å se i [meta-repoet for tiltakspenger](https://github.com/navikt/tiltakspenger) 
for hvordan man kan få kjørt opp de greiene lokalt. Meta-repoet er også behjelpelig med å få kjørt opp `tiltakspenger-saksbehandling-api` i en docker-container.

## Arkitekturbeslutninger
Se [docs/adr/index.md](docs/adr/index.md)

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne, tekniske henvendelser kan sendes via Slack i kanalen #tiltakspenger-værsågod.
