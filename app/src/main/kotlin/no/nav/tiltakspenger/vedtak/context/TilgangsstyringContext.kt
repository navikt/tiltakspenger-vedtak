package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.TilgangGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingClient
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingClientImpl
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tilgang.TilgangClient

@Suppress("unused")
open class TilgangsstyringContext(
    getPdlPipToken: suspend () -> AccessToken,
) {
    open val tilgangsstyringService: TilgangsstyringService by lazy {
        TilgangsstyringServiceImpl.create(
            skjermingBaseUrl = Configuration.skjermingClientConfig().baseUrl,
            getPdlPipToken = getPdlPipToken,
            pdlPipUrl = Configuration.pdlClientConfig().baseUrl,
            getSkjermingToken = tokenProviderSkjerming::getToken,
        )
    }
    private val tokenProviderSkjerming: AzureTokenProvider by lazy { AzureTokenProvider(config = Configuration.oauthConfigSkjerming()) }
    private val skjermingClient: SkjermingClient by lazy { SkjermingClientImpl(getToken = tokenProviderSkjerming::getToken) }
    val skjermingGateway: SkjermingGateway by lazy { SkjermingGatewayImpl(skjermingClient) }

    private val tokenProviderTilgang: AzureTokenProvider by lazy { AzureTokenProvider(config = Configuration.oauthConfigTilgang()) }
    private val getTilgangToken: suspend () -> AccessToken = { tokenProviderTilgang.getToken() }

    val tilgangGateway: TilgangGateway by lazy {
        TilgangClient(
            baseUrl = Configuration.tilgangClientConfig().baseUrl,
            getToken = { getTilgangToken.toString() },
        )
    }
}
