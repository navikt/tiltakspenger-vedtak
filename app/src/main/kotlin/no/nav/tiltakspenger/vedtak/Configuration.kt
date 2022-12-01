package no.nav.tiltakspenger.vedtak


import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.tiltakspenger.felles.Rolle
import java.util.*

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
    )

    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
        "SERVICEUSER_TPTS_USERNAME" to System.getenv("SERVICEUSER_TPTS_USERNAME"),
        "SERVICEUSER_TPTS_PASSWORD" to System.getenv("SERVICEUSER_TPTS_PASSWORD"),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
        "logback.configurationFile" to "logback.xml"
    )

    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            "logback.configurationFile" to "logback.local.xml",
            Rolle.SAKSBEHANDLER.name to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            Rolle.FORTROLIG_ADRESSE.name to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
            Rolle.STRENGT_FORTROLIG_ADRESSE.name to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
            Rolle.SKJERMING.name to "dbe4ad45-320b-4e9a-aaa1-73cca4ee124d"
        )
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            Rolle.SAKSBEHANDLER.name to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            Rolle.FORTROLIG_ADRESSE.name to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
            Rolle.STRENGT_FORTROLIG_ADRESSE.name to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
            Rolle.SKJERMING.name to "dbe4ad45-320b-4e9a-aaa1-73cca4ee124d"
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            Rolle.SAKSBEHANDLER.name to "6c6ce2e8-b2e2-4c4b-8194-215c8e27a5c7",
            Rolle.FORTROLIG_ADRESSE.name to "9ec6487d-f37a-4aad-a027-cd221c1ac32b",
            Rolle.STRENGT_FORTROLIG_ADRESSE.name to "ad7b87a6-9180-467c-affc-20a566b0fec0",
            Rolle.SKJERMING.name to "e750ceb5-b70b-4d94-b4fa-9d22467b786b"
        )
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
        AdRolle(Rolle.SAKSBEHANDLER, UUID.fromString(config()[Key(Rolle.SAKSBEHANDLER.name, stringType)])),
        AdRolle(Rolle.FORTROLIG_ADRESSE, UUID.fromString(config()[Key(Rolle.FORTROLIG_ADRESSE.name, stringType)])),
        AdRolle(
            Rolle.STRENGT_FORTROLIG_ADRESSE,
            UUID.fromString(config()[Key(Rolle.STRENGT_FORTROLIG_ADRESSE.name, stringType)])
        ),
        AdRolle(Rolle.SKJERMING, UUID.fromString(config()[Key(Rolle.SKJERMING.name, stringType)]))
    )

    fun logbackConfigurationFile() = config()[Key("logback.configurationFile", stringType)]

    data class TokenVerificationConfig(
        val jwksUri: String = config()[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)],
        val issuer: String = config()[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val leeway: Long = 1000,
        val roles: List<AdRolle> = alleAdRoller()
    )
}
