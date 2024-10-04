package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.saksbehandling.ports.PoaoTilgangGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.poaotilgang.PoaoTilgangClient

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
    private val tokenProviderTilgang: AzureTokenProvider by lazy { AzureTokenProvider(config = Configuration.oauthConfigPoaoTilgang()) }
    private val getPoaoTilgangToken: suspend () -> AccessToken = { tokenProviderTilgang.getToken() }

    val poaoTilgangGateway: PoaoTilgangGateway by lazy {
        PoaoTilgangClient(
            baseUrl = Configuration.poaoTilgangClientConfig().baseUrl,
            getToken = { getPoaoTilgangToken.toString() },
        )
    }
}
