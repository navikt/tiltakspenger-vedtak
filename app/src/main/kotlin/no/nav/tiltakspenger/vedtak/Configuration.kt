package no.nav.tiltakspenger.vedtak

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import java.util.UUID

private const val APPLICATION_NAME = "tiltakspenger-vedtak"

enum class Profile {
    LOCAL,
    DEV,
    PROD,
}

data class AdRolle(
    val name: Rolle,
    val objectId: UUID,
)

object Configuration {
    val rapidsAndRivers =
        mapOf(
            "RAPID_APP_NAME" to APPLICATION_NAME,
            "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
            "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
            "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
            "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
            "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-vedtak-v1",
            "HTTP_PORT" to "8080",
        )

    private val otherDefaultProperties =
        mapOf(
            "application.httpPort" to 8080.toString(),
            "SERVICEUSER_TPTS_USERNAME" to System.getenv("SERVICEUSER_TPTS_USERNAME"),
            "SERVICEUSER_TPTS_PASSWORD" to System.getenv("SERVICEUSER_TPTS_PASSWORD"),
            "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
            "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
            "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
            "ROLE_SAKSBEHANDLER" to System.getenv("ROLE_SAKSBEHANDLER"),
            "ROLE_BESLUTTER" to System.getenv("ROLE_BESLUTTER"),
            "ROLE_FORTROLIG" to System.getenv("ROLE_FORTROLIG"),
            "ROLE_STRENGT_FORTROLIG" to System.getenv("ROLE_STRENGT_FORTROLIG"),
            "ROLE_SKJERMING" to System.getenv("ROLE_SKJERMING"),
            "ROLE_DRIFT" to System.getenv("ROLE_DRIFT"),
            "logback.configurationFile" to "logback.xml",
            "SKJERMING_SCOPE" to System.getenv("SCOPE_SKJERMING"),
            "SKJERMING_URL" to System.getenv("SKJERMING_URL"),
            "PDL_SCOPE" to System.getenv("PDL_SCOPE"),
            "PDL_ENDPOINT_URL" to System.getenv("PDL_ENDPOINT_URL"),
            "TILTAK_SCOPE" to System.getenv("TILTAK_SCOPE"),
            "TILTAK_URL" to System.getenv("TILTAK_URL"),
            "MELDEKORT_SCOPE" to System.getenv("MELDEKORT_SCOPE"),
            "MELDEKORT_URL" to System.getenv("MELDEKORT_URL"),
            "ELECTOR_PATH" to System.getenv("ELECTOR_PATH"),
        )

    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties =
        ConfigurationMap(
            mapOf(
                "application.profile" to Profile.LOCAL.toString(),
                "logback.configurationFile" to "logback.local.xml",
                "ROLE_SAKSBEHANDLER" to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
                "ROLE_BESLUTTER" to "79985315-b2de-40b8-a740-9510796993c6",
                "ROLE_FORTROLIG" to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
                "ROLE_STRENGT_FORTROLIG" to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
                "ROLE_SKJERMING" to "dbe4ad45-320b-4e9a-aaa1-73cca4ee124d",
                "ROLE_DRIFT" to "c511113e-5b22-49e7-b9c4-eeb23b01f518",
                "PDL_SCOPE" to "api://localhost:8091/.default",
                "PDL_ENDPOINT_URL" to "https://localhost:8091/graphql",
                "SKJERMING_SCOPE" to "localhost",
                "SKJERMING_URL" to "http://host.docker.internal:8091",
                "TILTAK_SCOPE" to "localhost",
                "TILTAK_URL" to "http://host.docker.internal:8091",
                "MELDEKORT_SCOPE" to "localhost",
                "MELDEKORT_URL" to "http://host.docker.internal:8081",
                "UTBETALING_SCOPE" to "localhost",
                "UTBETALING_URL" to "http://localhost:8091",
            ),
        )
    private val devProperties =
        ConfigurationMap(
            mapOf(
                "application.profile" to Profile.DEV.toString(),
                "PDL_SCOPE" to "api://dev-fss.pdl.pdl-api/.default",
                "PDL_ENDPOINT_URL" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
                "SKJERMING_SCOPE" to "api://dev-gcp.nom.skjermede-personer-pip/.default",
                "SKJERMING_URL" to "https://skjermede-personer-pip.intern.dev.nav.no",
                "TILTAK_SCOPE" to "api://dev-gcp.tpts.tiltakspenger-tiltak/.default",
                "TILTAK_URL" to "http://tiltakspenger-tiltak",
                "MELDEKORT_SCOPE" to "api://dev-gcp.tpts.tiltakspenger-meldekort-api/.default",
                "MELDEKORT_URL" to "http://tiltakspenger-meldekort-api",
                "UTBETALING_SCOPE" to "api://dev-gcp.helved.utsjekk/.default",
                "UTBETALING_URL" to "http://utsjekk.helved",
            ),
        )
    private val prodProperties =
        ConfigurationMap(
            mapOf(
                "application.profile" to Profile.PROD.toString(),
                "PDL_SCOPE" to "api://prod-fss.pdl.pdl-api/.default",
                "PDL_ENDPOINT_URL" to "https://pdl-api.prod-fss-pub.nais.io/graphql",
                "SKJERMING_SCOPE" to "api://prod-gcp.nom.skjermede-personer-pip/.default",
                "SKJERMING_URL" to "https://skjermede-personer-pip.intern.nav.no",
                "TILTAK_SCOPE" to "api://prod-gcp.tpts.tiltakspenger-tiltak/.default",
                "TILTAK_URL" to "http://tiltakspenger-tiltak",
                "MELDEKORT_SCOPE" to "api://prod-gcp.tpts.tiltakspenger-meldekort-api/.default",
                "MELDEKORT_URL" to "http://tiltakspenger-meldekort-api",
                "UTBETALING_SCOPE" to "api://prod-gcp.helved.utsjekk/.default",
                "UTBETALING_URL" to "http://utsjekk.helved",
            ),
        )

    private fun config() =
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp" ->
                systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

            "prod-gcp" ->
                systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

            else -> {
                systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
            }
        }

    fun applicationProfile() =
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> Profile.DEV
            "prod-gcp" -> Profile.PROD
            else -> Profile.LOCAL
        }

    fun alleAdRoller(): List<AdRolle> =
        listOf(
            AdRolle(Rolle.SAKSBEHANDLER, UUID.fromString(config()[Key("ROLE_SAKSBEHANDLER", stringType)])),
            AdRolle(Rolle.BESLUTTER, UUID.fromString(config()[Key("ROLE_BESLUTTER", stringType)])),
            AdRolle(Rolle.FORTROLIG_ADRESSE, UUID.fromString(config()[Key("ROLE_FORTROLIG", stringType)])),
            AdRolle(
                Rolle.STRENGT_FORTROLIG_ADRESSE,
                UUID.fromString(config()[Key("ROLE_STRENGT_FORTROLIG", stringType)]),
            ),
            AdRolle(Rolle.SKJERMING, UUID.fromString(config()[Key("ROLE_SKJERMING", stringType)])),
            AdRolle(Rolle.DRIFT, UUID.fromString(config()[Key("ROLE_DRIFT", stringType)])),
        )

    data class ClientConfig(
        val baseUrl: String,
    )

    fun logbackConfigurationFile() = config()[Key("logback.configurationFile", stringType)]

    data class TokenVerificationConfig(
        val jwksUri: String = config()[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)],
        val issuer: String = config()[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val leeway: Long = 1000,
        val roles: List<AdRolle> = alleAdRoller(),
    )

    fun pdlClientConfig(baseUrl: String = config()[Key("PDL_ENDPOINT_URL", stringType)]) = ClientConfig(baseUrl = baseUrl)

    fun ouathConfigPdl(
        scope: String = config()[Key("PDL_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun oauthConfigSkjerming(
        scope: String = config()[Key("SKJERMING_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun oauthConfigTiltak(
        scope: String = config()[Key("TILTAK_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun oauthConfigMeldekort(
        scope: String = config()[Key("MELDEKORT_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun oauthConfigUtbetaling(
        scope: String = config()[Key("UTBETALING_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun skjermingClientConfig(baseUrl: String = config()[Key("SKJERMING_URL", stringType)]) = ClientConfig(baseUrl = baseUrl)

    fun tiltakClientConfig(baseUrl: String = config()[Key("TILTAK_URL", stringType)]) = ClientConfig(baseUrl = baseUrl)

    fun meldekortClientConfig(baseUrl: String = config()[Key("MELDEKORT_URL", stringType)]) = ClientConfig(baseUrl = baseUrl)

    fun utbetalingClientConfig(baseUrl: String = config()[Key("UTBETALING_URL", stringType)]) = ClientConfig(baseUrl = baseUrl)

    fun kafkaBootstrapLocal(): String = config()[Key("KAFKA_BROKERS", stringType)]

    fun isNais() = applicationProfile() != Profile.LOCAL

    fun electorPath(): String = config()[Key("ELECTOR_PATH", stringType)]
}
