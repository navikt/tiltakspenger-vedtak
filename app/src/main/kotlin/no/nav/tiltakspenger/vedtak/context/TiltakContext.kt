package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakClientImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl

internal open class TiltakContext(
    val tiltakGateway: TiltakGateway,
) {
    companion object {
        fun create(): TiltakContext {
            val tokenProviderTiltak = AzureTokenProvider(config = Configuration.oauthConfigTiltak())
            val tiltakClient = TiltakClientImpl(getToken = tokenProviderTiltak::getToken)
            val tiltakGateway = TiltakGatewayImpl(tiltakClient)
            return TiltakContext(tiltakGateway = tiltakGateway)
        }
    }
}
