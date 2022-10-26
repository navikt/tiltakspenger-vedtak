package no.nav.tiltakspenger.vedtak


import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import java.util.*

private const val applicationName = "tiltakspenger-vedtak"

enum class Profile {
    LOCAL, DEV, PROD
}

data class Role(
    val name: RoleName,
    val objectId: UUID,
)

enum class RoleName {
    SAKSBEHANDLER, FORTROLIG_ADRESSE, STRENGT_FORTROLIG_ADRESSE, SKJERMING
}

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
    )
    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
            RoleName.SAKSBEHANDLER.name to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            RoleName.FORTROLIG_ADRESSE.name to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
            RoleName.STRENGT_FORTROLIG_ADRESSE.name to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
            RoleName.SKJERMING.name to "b523b8a7-a103-41a6-8a88-becc3be0f499"
        )
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            RoleName.SAKSBEHANDLER.name to "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            RoleName.FORTROLIG_ADRESSE.name to "ea930b6b-9397-44d9-b9e6-f4cf527a632a",
            RoleName.STRENGT_FORTROLIG_ADRESSE.name to "5ef775f2-61f8-4283-bf3d-8d03f428aa14",
            RoleName.SKJERMING.name to "b523b8a7-a103-41a6-8a88-becc3be0f499"
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            RoleName.SAKSBEHANDLER.name to "6c6ce2e8-b2e2-4c4b-8194-215c8e27a5c7",
            RoleName.FORTROLIG_ADRESSE.name to "9ec6487d-f37a-4aad-a027-cd221c1ac32b",
            RoleName.STRENGT_FORTROLIG_ADRESSE.name to "ad7b87a6-9180-467c-affc-20a566b0fec0",
            RoleName.SKJERMING.name to "ff5b9d0d-3948-4a9d-b4bb-7a97fec3041c"
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

    fun allRoles(): List<Role> = listOf(
        Role(RoleName.SAKSBEHANDLER, UUID.fromString(config()[Key(RoleName.SAKSBEHANDLER.name, stringType)])),
        Role(RoleName.FORTROLIG_ADRESSE, UUID.fromString(config()[Key(RoleName.FORTROLIG_ADRESSE.name, stringType)])),
        Role(
            RoleName.STRENGT_FORTROLIG_ADRESSE,
            UUID.fromString(config()[Key(RoleName.STRENGT_FORTROLIG_ADRESSE.name, stringType)])
        ),
        Role(RoleName.SKJERMING, UUID.fromString(config()[Key(RoleName.SKJERMING.name, stringType)]))
    )

    data class TokenVerificationConfig(
        val jwksUri: String = config()[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)],
        val issuer: String = config()[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val leeway: Long = 1000,
        val roles: List<Role> = allRoles()
    )
}
