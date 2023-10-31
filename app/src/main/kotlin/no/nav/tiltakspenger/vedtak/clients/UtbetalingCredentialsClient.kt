package no.nav.tiltakspenger.vedtak.clients

    import no.nav.tiltakspenger.vedtak.Configuration
    import io.ktor.client.HttpClient
    import io.ktor.server.config.ApplicationConfig
    import no.nav.tiltakspenger.utbetaling.auth.ClientConfig
    import no.nav.tiltakspenger.utbetaling.auth.OAuth2Client

class UtbetalingCredentialsClient(
    config: ApplicationConfig,
    httpClient: HttpClient = httpClientWithRetry(timeout = 30L),
) {
        val utbetalingScope = Configuration.UtbetalingTokenConfig().scope
        private val oauth2CredentialsClient = OAuth2Client(
            httpClient = httpClient,
            wellKnownUrl = wellKnownUrl,
            clientAuthProperties = clientAuth,)

        suspend fun getToken(): String {
            val clientCredentialsGrant = oauth2CredentialsClient.clientCredentials(utbetalingScope)
            return clientCredentialsGrant.accessToken
        }
}
