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

private const val applicationName = "tiltakspenger-vedtak"

enum class Profile {
    LOCAL, DEV, PROD
}

data class AdRolle(
    val name: Rolle,
    val objectId: UUID,
)

object Configuration {

    val rapidsAndRivers = mapOf(
        "RAPID_APP_NAME" to applicationName,
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-vedtak-v1",
        "HTTP_PORT" to "8080",
    )

    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
        "SERVICEUSER_TPTS_USERNAME" to System.getenv("SERVICEUSER_TPTS_USERNAME"),
        "SERVICEUSER_TPTS_PASSWORD" to System.getenv("SERVICEUSER_TPTS_PASSWORD"),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
        "ROLE_SAKSBEHANDLER" to System.getenv("ROLE_SAKSBEHANDLER"),
        "ROLE_BESLUTTER" to System.getenv("ROLE_BESLUTTER"),
        "ROLE_ADMINISTRATOR" to System.getenv("ROLE_ADMINISTRATOR"),
        "ROLE_FORTROLIG" to System.getenv("ROLE_FORTROLIG"),
        "ROLE_STRENGT_FORTROLIG" to System.getenv("ROLE_STRENGT_FORTROLIG"),
        "ROLE_SKJERMING" to System.getenv("ROLE_SKJERMING"),
        "ROLE_DRIFT" to System.getenv("ROLE_DRIFT"),
        "logback.configurationFile" to "logback.xml",
        "SCOPE_UTBETALING" to System.getenv("SCOPE_UTBETALING"),
        "UTBETALING_URL" to System.getenv("UTBETALING_URL"),
    )

    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            "logback.configurationFile" to "logback.local.xml",
            "ROLE_SAKSBEHANDLER" to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            "ROLE_BESLUTTER" to "79985315-b2de-40b8-a740-9510796993c6",
            "ROLE_ADMINISTRATOR" to "cbe715d0-6f67-46bf-86b4-688c4419b747",
            "ROLE_FORTROLIG" to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
            "ROLE_STRENGT_FORTROLIG" to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
            "ROLE_SKJERMING" to "dbe4ad45-320b-4e9a-aaa1-73cca4ee124d",
            "ROLE_DRIFT" to "c511113e-5b22-49e7-b9c4-eeb23b01f518",
            "SCOPE_UTBETALING" to "localhost",
            "UTBETALING_URL" to "http://host.docker.internal:8083",
        ),
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "SCOPE_UTBETALING" to "api://dev-gcp.tpts.tiltakspenger-utbetaling/.default",
            "UTBETALING_URL" to "https://tiltakspenger-utbetaling.intern.dev.nav.no",
        ),
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "SCOPE_UTBETALING" to "api://prod-gcp.tpts.tiltakspenger-utbetaling/.default",
            "UTBETALING_URL" to "https://tiltakspenger-utbetaling.intern.nav.no",
        ),
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

        "prod-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

        else -> {
            systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }

    fun applicationProfile() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> Profile.DEV
        "prod-gcp" -> Profile.PROD
        else -> Profile.LOCAL
    }

    fun alleAdRoller(): List<AdRolle> = listOf(
        AdRolle(Rolle.SAKSBEHANDLER, UUID.fromString(config()[Key("ROLE_SAKSBEHANDLER", stringType)])),
        AdRolle(Rolle.BESLUTTER, UUID.fromString(config()[Key("ROLE_BESLUTTER", stringType)])),
        AdRolle(Rolle.ADMINISTRATOR, UUID.fromString(config()[Key("ROLE_ADMINISTRATOR", stringType)])),
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

    data class UtbetalingTokenConfig(
        val scope: String = config()[Key("SCOPE_UTBETALING", stringType)],
    )

    fun utbetalingClientConfig(baseUrl: String = config()[Key("UTBETALING_URL", stringType)]) =
        ClientConfig(baseUrl = baseUrl)

    fun oauthConfigUtbetaling(
        scope: String = config()[Key("SCOPE_UTBETALING", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )
}
