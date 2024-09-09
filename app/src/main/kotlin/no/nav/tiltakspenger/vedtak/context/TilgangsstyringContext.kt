package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingClientImpl
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingGatewayImpl

@Suppress("unused")
internal open class TilgangsstyringContext(
    val tilgangsstyringService: TilgangsstyringService,
    val tokenProviderSkjerming: AzureTokenProvider,
    val skjermingGateway: SkjermingGatewayImpl,
) {
    companion object {
        fun create(
            tokenProviderPdl: AzureTokenProvider,
        ): TilgangsstyringContext {
            val tokenProviderSkjerming = AzureTokenProvider(config = Configuration.oauthConfigSkjerming())
            val tilgangsstyringService = TilgangsstyringServiceImpl.create(
                skjermingBaseUrl = Configuration.skjermingClientConfig().baseUrl,
                getPdlPipToken = tokenProviderPdl::getToken,
                pdlPipUrl = Configuration.pdlClientConfig().baseUrl,
                getSkjermingToken = tokenProviderSkjerming::getToken,
            )
            val skjermingClient = SkjermingClientImpl(getToken = tokenProviderSkjerming::getToken)
            val skjermingGateway = SkjermingGatewayImpl(skjermingClient)
            return TilgangsstyringContext(
                tilgangsstyringService = tilgangsstyringService,
                tokenProviderSkjerming = tokenProviderSkjerming,
                skjermingGateway = skjermingGateway,
            )
        }
    }
}
